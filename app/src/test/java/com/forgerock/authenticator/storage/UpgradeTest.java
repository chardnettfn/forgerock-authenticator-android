/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 */

package com.forgerock.authenticator.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.forgerock.authenticator.BuildConfig;
import com.forgerock.authenticator.CustomRobolectricTestRunner;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.CoreMechanismFactory;
import com.forgerock.authenticator.mechanisms.oath.Oath;
import com.forgerock.authenticator.mechanisms.push.Push;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static com.forgerock.authenticator.storage.IdentityDatabaseTest.assertNotEquals;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Currently tests the upgrade process from SharedPreferences to SQLite
 * This means:
 * - No push mechanisms
 * - No notifications
 *
 * All of the following aspects must be tested:
 * - save to new storage
 * - update storage version number
 * - delete old storage
 *
 * If possible, faults must be introduced to the following stages:
 * - load from old storage
 * - load from new storage and compare to old storage
 */
@RunWith(CustomRobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class UpgradeTest {

    private static final String NAME  = "tokens";
    private static final String ORDER = "tokenOrder";
    private SharedPreferences prefs;
    private ModelOpenHelper modelOpenHelper;

    private static final String FORGEROCK_ALICE = "{\"algo\":\"SHA1\",\"counter\":5,\"digits\":6,\"issuer\":\"ForgeRock\",\"label\":\"alice\",\"period\":30,\"secret\":[75,9,-99,91,-29,-25,-76,-3,-89,1,25,-76,122,-3,-27,11],\"type\":\"HOTP\"}";
    private static final String OTHERIDP_ALICE = "{\"algo\":\"SHA1\",\"counter\":1,\"digits\":8,\"issuer\":\"OtherIDP\",\"label\":\"alice\",\"period\":30,\"secret\":[75,9,-99,91,-29,-25,-76,-3,-89,1,25,-76,122,-3,-27,11],\"type\":\"HOTP\"}";
    private static final String FORGEROCK_CHARLIE = "{\"algo\":\"SHA256\",\"counter\":0,\"digits\":6,\"issuer\":\"ForgeRock\",\"label\":\"charlie\",\"period\":60,\"secret\":[10,-11,15,84,62,-110,4,7,-93,118,-4,126,13,54,44,-80],\"type\":\"TOTP\"}";
    private SharedPreferences storageInfo;

    @Before
    public void setup() {
        prefs = RuntimeEnvironment.application.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
        storageInfo = RuntimeEnvironment.application.getApplicationContext().getSharedPreferences("applicationInfo", Context.MODE_PRIVATE);


        prefs.edit().putString(ORDER, "[\"OtherIDP:alice\",\"ForgeRock:alice\",\"ForgeRock:charlie\"]")
        .putString("ForgeRock:alice", FORGEROCK_ALICE)
        .putString("OtherIDP:alice", OTHERIDP_ALICE)
        .putString("ForgeRock:charlie", FORGEROCK_CHARLIE)
        .apply();

        modelOpenHelper = new ModelOpenHelper(RuntimeEnvironment.application);
    }

    @Test
    public void dataShouldBeTransferredToNewStorageSystem() {
        IdentityModel model = modelOpenHelper.getModel();
        assertTrue(model.getStorageSystem() instanceof IdentityDatabase);
        assertEquals(model.getMechanisms().size(), 3);
    }

    @Test
    public void storageVersionShouldBeUpdated() {
        assertEquals(-1, storageInfo.getInt("lastVersion", -1));

        modelOpenHelper.getModel();
        assertEquals(2, storageInfo.getInt("lastVersion", -1));
    }

    @Test
    public void dataShouldBeRemovedFromOldStorageSystem() {
        modelOpenHelper.getModel();
        assertEquals(prefs.getString(ORDER, ""), "");
        assertEquals(prefs.getString("ForgeRock:alice", ""), "");
        assertEquals(prefs.getString("OtherIDP:alice", ""), "");
        assertEquals(prefs.getString("ForgeRock:charlie", ""), "");
    }

    @Test
    public void upgradeShouldAllowPartialTransferDueToMutatedValue() {
        final ArrayList<String> opaqueReference = new ArrayList<>();
        opaqueReference.add("ForgeRock:alice");

        ModelOpenHelper modelOpenHelper = new ModelOpenHelper(RuntimeEnvironment.application) {
            @Override
            protected IdentityModel getModelFromLatest(Context context) {
                IdentityModel model = super.getModelFromLatest(context);

                Identity alice = model.getIdentity(opaqueReference);
                if (alice != null) {
                    Oath oath = (Oath) alice.getMechanisms().get(0);
                    oath.generateNextCode(); // Generate new code to mutate the state
                }
                return model;
            }
        };
        IdentityModel model = modelOpenHelper.getModel();
        assertEquals(2, model.getMechanisms().size());
        assertEquals("[\"\",\"ForgeRock:alice\",\"\"]", prefs.getString(ORDER, ""));
        assertEquals(FORGEROCK_ALICE, prefs.getString("ForgeRock:alice", ""));
        assertEquals("", prefs.getString("OtherIDP:alice", ""));
        assertEquals("", prefs.getString("ForgeRock:charlie", ""));
        assertEquals(null, model.getIdentity(opaqueReference));

    }

    @Test
    public void dataFromUpgradeShouldNotOverwriteExistingData() throws Exception {
        //Setup database
        IdentityModel model = new IdentityModel();
        CoreMechanismFactory mechanismFactory = new CoreMechanismFactory(RuntimeEnvironment.application, model);
        StorageSystem storageSystem = new IdentityDatabase(RuntimeEnvironment.application, mechanismFactory);
        model.loadFromStorageSystem(storageSystem);
        Identity identity = model.addIdentity(Identity.builder().setIssuer("ForgeRock").setAccountName("alice"));
        identity.addMechanism(Oath.builder().setMechanismUID("999").setType("HOTP").setCounter("99").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM======"));

        //Run test
        IdentityModel newModel = modelOpenHelper.getModel();
        assertEquals(3, newModel.getIdentities().size());
        assertEquals(3, newModel.getMechanisms().size());
        assertEquals(1, newModel.getIdentity("ForgeRock", "alice").getMechanisms().size());
        validateOath(model, "ForgeRock", "alice", 99, 30, 6, "SHA1", Oath.TokenType.HOTP, "SwmdW+PntP2nARm0ev3lCw==");

        assertEquals("", prefs.getString(ORDER, ""));
        assertEquals("", prefs.getString("ForgeRock:alice", ""));
    }

    @Test
    public void dataFromUpgradeShouldAddToIdentityWithExistingOtherMechanismType() throws Exception {
        //Setup database
        IdentityModel model = new IdentityModel();
        CoreMechanismFactory mechanismFactory = new CoreMechanismFactory(RuntimeEnvironment.application, model);
        StorageSystem storageSystem = new IdentityDatabase(RuntimeEnvironment.application, mechanismFactory);
        model.loadFromStorageSystem(storageSystem);
        Identity identity = model.addIdentity(Identity.builder().setIssuer("ForgeRock").setAccountName("alice"));
        identity.addMechanism(Push.builder().setMechanismUID("999"));

        //Run test
        IdentityModel newModel = modelOpenHelper.getModel();
        assertEquals(3, newModel.getIdentities().size());
        assertEquals(4, newModel.getMechanisms().size());
        assertEquals(2, newModel.getIdentity("ForgeRock", "alice").getMechanisms().size());

        assertEquals("", prefs.getString(ORDER, ""));
        assertEquals("", prefs.getString("ForgeRock:alice", ""));
    }

    @Test
    public void partialTransferShouldNotUpdateStorageValue() {
        String BAD_ALICE = "{\"algo\":\"SHA1\",\"counter\":5,\"digits\":6,\"issuer\":\"ForgeRock\",\"label\":\"alice\",\"period\":30,\"secret\":[75,9,-99,91,-29,-25,-76,-3,-89,1,25,-76,122,-3,-27,11],\"type\":\"ANGRYBADGER\"}";
        prefs.edit().putString("ForgeRock:alice", BAD_ALICE)
                .apply();

        modelOpenHelper.getModel();
        assertNotEquals("", prefs.getString(ORDER, "")); // To confirm we are actually testing against a partial state
        assertEquals(-1, storageInfo.getInt("lastVersion", -1));
    }

    @Test
    public void upgradeShouldAllowPartialTransferDueToMissingIdentity() {
        ModelOpenHelper modelOpenHelper = new ModelOpenHelper(RuntimeEnvironment.application) {
            @Override
            protected IdentityModel getModelFromLatest(Context context) {
                IdentityModel model = super.getModelFromLatest(context);
                ArrayList<String> opaqueReference = new ArrayList<>();
                opaqueReference.add("ForgeRock:alice");
                Identity alice = model.getIdentity(opaqueReference);
                if (alice != null) {
                    model.removeIdentity(alice);
                }
                return model;
            }
        };
        IdentityModel model = modelOpenHelper.getModel();
        assertEquals(2, model.getIdentities().size());
        assertEquals(prefs.getString(ORDER, ""), "[\"\",\"ForgeRock:alice\",\"\"]");
        assertEquals(prefs.getString("ForgeRock:alice", ""), FORGEROCK_ALICE);
        assertEquals(prefs.getString("OtherIDP:alice", ""), "");
        assertEquals(prefs.getString("ForgeRock:charlie", ""), "");
    }

    @Test
    public void upgradeShouldAllowPartialTransferDueToBadOriginalData() {
        String BAD_ALICE = "{\"algo\":\"SHA1\",\"counter\":5,\"digits\":6,\"issuer\":\"ForgeRock\",\"label\":\"alice\",\"period\":30,\"secret\":[75,9,-99,91,-29,-25,-76,-3,-89,1,25,-76,122,-3,-27,11],\"type\":\"ANGRYBADGER\"}";

        prefs.edit().putString("ForgeRock:alice", BAD_ALICE)
            .apply();

        IdentityModel model = modelOpenHelper.getModel();
        assertEquals(2, model.getIdentities().size());
        assertEquals(prefs.getString(ORDER, ""), "[\"\",\"ForgeRock:alice\",\"\"]");
        assertEquals(prefs.getString("ForgeRock:alice", ""), BAD_ALICE);
        assertEquals(prefs.getString("OtherIDP:alice", ""), "");
        assertEquals(prefs.getString("ForgeRock:charlie", ""), "");
    }

    @Test
    public void dataShouldBeCorrectlyStoredInNewStorageSystem() {
        IdentityModel model = modelOpenHelper.getModel();

        validateOath(model, "ForgeRock", "alice", 5, 30, 6, "SHA1", Oath.TokenType.HOTP, "SwmdW+PntP2nARm0ev3lCw==");
        validateOath(model, "OtherIDP", "alice", 1, 30, 8, "SHA1", Oath.TokenType.HOTP, "SwmdW+PntP2nARm0ev3lCw==");
        validateOath(model, "ForgeRock", "charlie", 0, 60, 6, "SHA256", Oath.TokenType.TOTP, "CvUPVD6SBAejdvx+DTYssA==");
    }

    private void validateOath(IdentityModel model, String issuer, String account, int counter, int period,
                              int digits, String algo, Oath.TokenType type, String base64Secret) {
        ArrayList<String> opaqueReference = new ArrayList<>();
        opaqueReference.add(issuer + ":" + account);

        Identity identity = model.getIdentity(opaqueReference);
        assertEquals(1, identity.getMechanisms().size());
        assertEquals(issuer, identity.getIssuer());
        assertEquals(account, identity.getAccountName());

        Oath oath = (Oath) identity.getMechanisms().get(0);
        assertEquals(oath.getCounter(), counter);
        assertEquals(oath.getDigits(), digits);
        assertEquals(oath.getAlgo(), algo);
        assertEquals(oath.getType(), type);
        assertEquals(oath.getPeriod(), period);
        assertTrue(oath.hasBase64Secret(base64Secret));
    }

    @Test
    public void shouldNotUpgradeWhenUpToDateVersionInformationIsPresent() {
        SharedPreferences storageInfo = RuntimeEnvironment.application.getApplicationContext().getSharedPreferences("applicationInfo", Context.MODE_PRIVATE);
        storageInfo.edit().putInt("lastVersion", 2).apply();

        IdentityModel model = modelOpenHelper.getModel();
        assertTrue(model.getStorageSystem() instanceof IdentityDatabase);
        assertEquals(model.getMechanisms().size(), 0);
    }
}
