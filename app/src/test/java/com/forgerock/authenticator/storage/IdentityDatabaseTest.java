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

import com.forgerock.authenticator.BuildConfig;
import com.forgerock.authenticator.CustomRobolectricTestRunner;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.CoreMechanismFactory;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.oath.Oath;
import com.forgerock.authenticator.mechanisms.push.Push;
import com.forgerock.authenticator.notifications.Notification;
import com.forgerock.authenticator.notifications.PushNotification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Ambition for this class: set of tests for StorageSystems - if you run these tests on a StorageSystem, and they all pass, it is ready for use.
 *
 * For each model layer:
 * -can't save without prerequisites
 * -can save with prerequisites
 * -can't save duplicate (as defined by .equals())
 * -can't duplicate values which should be unique (e.g. mechanismUID)
 * -adding a different object results in a different id
 * -can reload saved
 * -can delete saved
 * -can't delete with invalid id (check number of elements and return value)
 * -can update saved // TODO: create this test for Identity when applicable
 * -can't update with invalid id
 */
@RunWith(CustomRobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class IdentityDatabaseTest {
    IdentityModel model;
    private IdentityDatabase database;
    private final String ISSUER = "issuer";
    private final String OTHER_ISSUER = "otherIssuer";
    private Identity BASIC_IDENTITY;
    private Mechanism BASIC_MECHANISM;
    private Mechanism PREREQUISITE_MECHANISM;
    private Identity SAVED_IDENTITY;
    private Mechanism SAVED_MECHANISM;
    private Notification BASIC_NOTIFICATION;
    private Notification PREREQUISITE_NOTIFICATION;

    @Before
    public void setup() throws Exception {
        model = new IdentityModel();
        database = new IdentityDatabase(RuntimeEnvironment.application, new CoreMechanismFactory(RuntimeEnvironment.application, model));

        model.loadFromStorageSystem(database);

        Identity internalSavedIdentity = Identity.builder().setAccountName("uniqueinternalsavedidentity").build(model);
        database.addIdentity(internalSavedIdentity);

        BASIC_IDENTITY = Identity.builder().build(model);
        SAVED_IDENTITY = Identity.builder().setAccountName("uniquesavedidentity").build(model);
        database.addIdentity(SAVED_IDENTITY);

        BASIC_MECHANISM = Push.builder().setMechanismUID("9999").build(BASIC_IDENTITY);
        PREREQUISITE_MECHANISM = Push.builder().setMechanismUID("9998").build(SAVED_IDENTITY);
        SAVED_MECHANISM = Push.builder().setMechanismUID("9997").build(internalSavedIdentity);
        database.addMechanism(SAVED_MECHANISM);

        BASIC_NOTIFICATION = PushNotification.builder().build(PREREQUISITE_MECHANISM);
        PREREQUISITE_NOTIFICATION = PushNotification.builder().build(SAVED_MECHANISM);
        reloadModel();
    }

    @Test
    public void canSaveIdentity() {
        assertNotEquals(database.addIdentity(BASIC_IDENTITY), -1l);
    }

    @Test
    public void cantSaveDuplicateIdentity() {
        assertNotEquals(database.addIdentity(BASIC_IDENTITY), -1l);
        assertEquals(database.addIdentity(BASIC_IDENTITY), -1l);
    }

    @Test
    public void savingDifferentIdentitiesResultsInDifferentIds() {
        Identity identity = Identity.builder().setIssuer(ISSUER).build(model);
        Identity otherIdentity = Identity.builder().setIssuer(OTHER_ISSUER).build(model);

        long id = database.addIdentity(identity);
        long otherId = database.addIdentity(otherIdentity);
        assertNotEquals(id, -1l);
        assertNotEquals(otherId, -1l);
        assertNotEquals(id, otherId);
    }

    @Test
    public void canLoadSavedIdentity() {
        int initialSize = model.getIdentities().size();

        database.addIdentity(BASIC_IDENTITY);
        reloadModel();

        assertEquals(initialSize + 1, model.getIdentities().size());

        Identity loadedIdentity = model.getIdentity(BASIC_IDENTITY.getOpaqueReference());

        assertEquals(BASIC_IDENTITY, loadedIdentity);
        assertEquals(BASIC_IDENTITY.getImageURL(), loadedIdentity.getImageURL());
    }

    @Test
    public void canDeleteSavedIdentity() {
        int initialSize = model.getIdentities().size();

        long id = database.addIdentity(BASIC_IDENTITY);
        reloadModel();

        assertEquals(model.getIdentities().size(), initialSize + 1);

        assertTrue(database.deleteIdentity(id));
        reloadModel();

        assertEquals(model.getIdentities().size(), initialSize);
    }

    @Test
    public void cantDeleteSavedIdentityWithWrongId() {
        int initialSize = model.getIdentities().size();

        assertFalse(database.deleteIdentity(123456));
        reloadModel();

        assertEquals(model.getIdentities().size(), initialSize);
    }

    @Test
    public void cannotAddMechanismWithoutFirstAddingIdentity() throws Exception {
        assertEquals(database.addMechanism(BASIC_MECHANISM), -1);
    }

    @Test
    public void canAddMechanismAfterAddingIdentity() throws Exception {
        database.addIdentity(BASIC_IDENTITY);
        assertNotEquals(database.addMechanism(BASIC_MECHANISM), -1l);
    }

    @Test
    public void cantAddDuplicateMechanism() throws Exception {
        assertNotEquals(database.addMechanism(PREREQUISITE_MECHANISM), -1l);
        assertEquals(database.addMechanism(PREREQUISITE_MECHANISM), -1l);
    }

    @Test
    public void savingDifferentMechanismsResultsInDifferentIds() throws Exception {
        Mechanism mechanism = Oath.builder().setMechanismUID("1").setType("hotp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").build(SAVED_IDENTITY);
        Mechanism otherMechanism = Push.builder().setMechanismUID("2").build(SAVED_IDENTITY);

        long id = database.addMechanism(mechanism);
        long otherId = database.addMechanism(otherMechanism);
        assertNotEquals(id, -1l);
        assertNotEquals(otherId, -1l);
        assertNotEquals(id, otherId);
    }

    @Test
    public void cannotDuplicateMechanismUIDs() throws Exception {
        Mechanism mechanism = Oath.builder().setMechanismUID("1").setType("hotp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").build(SAVED_IDENTITY);
        Mechanism otherMechanism = Push.builder().setMechanismUID("1").build(SAVED_IDENTITY);

        long id = database.addMechanism(mechanism);
        long otherId = database.addMechanism(otherMechanism);
        assertNotEquals(id, -1l);
        assertEquals(otherId, -1l);
    }

    @Test
    public void canLoadSavedMechanism() {
        int initialSize = model.getMechanisms().size();

        database.addMechanism(PREREQUISITE_MECHANISM);
        reloadModel();

        assertEquals(initialSize + 1, model.getMechanisms().size());

        Mechanism loadedMechanism = model.getMechanism(PREREQUISITE_MECHANISM.getOpaqueReference());

        assertEquals(PREREQUISITE_MECHANISM, loadedMechanism);
        assertEquals(PREREQUISITE_MECHANISM.getMechanismUID(), loadedMechanism.getMechanismUID());
    }

    @Test
    public void canDeleteSavedMechanism() {
        int initialSize = model.getMechanisms().size();

        long id = database.addMechanism(PREREQUISITE_MECHANISM);
        reloadModel();

        assertEquals(initialSize + 1, model.getMechanisms().size());

        assertTrue(database.deleteMechanism(id));
        reloadModel();

        assertEquals(model.getMechanisms().size(), initialSize);
    }

    @Test
    public void cantDeleteSavedMechanismWithWrongId() {
        int initialSize = model.getMechanisms().size();

        assertFalse(database.deleteMechanism(123456));
        reloadModel();

        assertEquals(model.getMechanisms().size(), initialSize);
    }

    @Test
     public void canUpdateMechanism() throws Exception {
        Oath.OathBuilder builder = Oath.builder().setType("totp").setCounter("0").setMechanismUID("6789").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM");
        Mechanism originalValue = builder.build(SAVED_IDENTITY);
        Mechanism newValue = builder.setCounter("99").build(SAVED_IDENTITY);

        long id = database.addMechanism(originalValue);
        assertTrue(database.updateMechanism(id, newValue));

        reloadModel();

        Mechanism loadedMechanism = model.getMechanism(originalValue.getOpaqueReference());
        assertEquals(99, ((Oath) loadedMechanism).getCounter());
    }

    @Test
    public void cantUpdateMechanismWithWrongId() throws Exception {
        Oath.OathBuilder builder = Oath.builder().setType("totp").setCounter("0").setMechanismUID("6789").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM");
        Mechanism newValue = builder.setCounter("99").build(SAVED_IDENTITY);

        assertFalse(database.updateMechanism(345, newValue));
    }

    @Test
    public void cannotAddNotificationWithoutFirstAddingMechanism() throws Exception {
        assertEquals(database.addNotification(BASIC_NOTIFICATION), -1);
    }

    @Test
    public void canSaveNotification() {
        database.addMechanism(PREREQUISITE_MECHANISM);
        assertNotEquals(database.addNotification(BASIC_NOTIFICATION), -1l);
    }

    @Test
    public void cantSaveDuplicateNotification() {
        assertNotEquals(-1l, database.addNotification(PREREQUISITE_NOTIFICATION));
        assertEquals(-1l, database.addNotification(PREREQUISITE_NOTIFICATION));
    }

    @Test
    public void savingDifferentNotificationsResultsInDifferentIds() throws Exception {
        Calendar calendar = Calendar.getInstance();
        long time = calendar.getTimeInMillis();
        Notification notification = PushNotification.builder().setTimeAdded(Calendar.getInstance()).build(SAVED_MECHANISM);
        calendar.setTimeInMillis(time + 100);
        Notification otherNotification = PushNotification.builder().setTimeAdded(calendar).build(SAVED_MECHANISM);

        long id = database.addNotification(notification);
        long otherId = database.addNotification(otherNotification);
        assertNotEquals(id, -1l);
        assertNotEquals(otherId, -1l);
        assertNotEquals(id, otherId);
    }

    @Test
    public void canLoadSavedNotification() {
        int initialSize = model.getNotifications().size();

        database.addNotification(PREREQUISITE_NOTIFICATION);
        reloadModel();

        assertEquals(initialSize + 1, model.getNotifications().size());

        Notification loadedNotification = model.getNotification(PREREQUISITE_NOTIFICATION.getOpaqueReference());

        assertEquals(PREREQUISITE_NOTIFICATION, loadedNotification);
    }

    @Test
    public void canDeleteSavedNotification() {
        int initialSize = model.getNotifications().size();

        long id = database.addNotification(PREREQUISITE_NOTIFICATION);
        reloadModel();

        assertEquals(initialSize + 1, model.getNotifications().size());

        assertTrue(database.deleteNotification(id));
        reloadModel();

        assertEquals(model.getNotifications().size(), initialSize);
    }

    @Test
    public void cantDeleteSavedNotificationWithWrongId() {
        int initialSize = model.getNotifications().size();

        assertFalse(database.deleteNotification(123456));
        reloadModel();

        assertEquals(model.getNotifications().size(), initialSize);
    }

    @Test
    public void canUpdateNotification() throws Exception {
        Notification originalValue = PushNotification.builder().setPending(true).build(SAVED_MECHANISM);
        Notification newValue = PushNotification.builder().setPending(false).build(SAVED_MECHANISM);

        long id = database.addNotification(originalValue);
        assertTrue(database.updateNotification(id, newValue));

        reloadModel();

        Notification loadedNotification = model.getNotification(originalValue.getOpaqueReference());
        assertFalse(loadedNotification.isPending());
    }

    @Test
    public void cantUpdateNotificationWithWrongId() throws Exception {
        Notification newValue = PushNotification.builder().setPending(false).build(SAVED_MECHANISM);
        assertFalse(database.updateNotification(345, newValue));
    }

    private void reloadModel() {
        model = new IdentityModel();
        database = new IdentityDatabase(RuntimeEnvironment.application, new CoreMechanismFactory(RuntimeEnvironment.application, model));

        model.loadFromStorageSystem(database);
    }

    public static void assertNotEquals(Object object, Object other) {
        if (object == null && other == null) {
            fail("Both objects are null");
        }
        if (object == null || other == null) {
            return; // Only one can be null.
        }
        if (!object.getClass().equals(other.getClass())) {
            fail("Tried to compare objects of different classes");
        }
        assertFalse(object.equals(other));
    }
}
