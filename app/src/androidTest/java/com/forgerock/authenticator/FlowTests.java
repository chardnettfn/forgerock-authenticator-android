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

package com.forgerock.authenticator;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.InvalidNotificationException;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.oath.Oath;
import com.forgerock.authenticator.mechanisms.push.Push;
import com.forgerock.authenticator.notifications.Notification;
import com.forgerock.authenticator.notifications.PushNotification;
import com.forgerock.authenticator.storage.IdentityModel;

import org.forgerock.util.encode.Base64;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

import roboguice.RoboGuice;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;

/**
 * Generates screenshots for use on the Play Store.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FlowTests {
    @Rule
    public ActivityTestRule<IdentityActivity> mActivityRule = new ActivityTestRule<>(
            IdentityActivity.class, false, false);
    private IdentityModel model;
    private Mechanism push;

    @Before
    public void setup() throws Exception {

        model = RoboGuice.getInjector(InstrumentationRegistry.getTargetContext().getApplicationContext()).getInstance(IdentityModel.class);
        for (Identity identity : new ArrayList<>(model.getIdentities())) {
            model.removeIdentity(identity);
        }

        // Note: These links must be updated to point to a valid location the files are hosted.
        generateUser("ACME", "joe.bloggs", "http://172.16.101.174:8040/images/acme-flower-logo.png", false, true);
        generateUser("ForgeRock", "jbloggs", "", true, true);
        generateUser("Google", "joebloggs82", "http://172.16.101.174:8040/images/G-symbol.png", true, false);
    }

    @Test
    public void generateScreenshots() throws Exception {
        mActivityRule.launchActivity(new Intent());

        Thread.sleep(5000);
        TestDevice.screenshot(mActivityRule.getActivity(), "store_accounts");

        onData(anything()).inAdapterView(withId(R.id.grid)).atPosition(1).perform(click());
        onView(withId(R.id.refresh)).perform(click());

        Thread.sleep(5000);
        TestDevice.screenshot(mActivityRule.getActivity(), "store_account");

        generateNotification(30);

        onData(anything()).inAdapterView(withId(R.id.grid)).atPosition(1).perform(click());

        onData(anything()).inAdapterView(withId(R.id.notification_list)).atPosition(1).perform(click());

        Thread.sleep(5000);
        TestDevice.screenshot(mActivityRule.getActivity(), "store_swipe");
    }

    @Test
    public void deletePushTest() throws Exception {
        mActivityRule.launchActivity(new Intent());

        TestDevice.screenshot(mActivityRule.getActivity(), "deletePushTestBaseCase");

        onData(anything()).inAdapterView(withId(R.id.grid)).atPosition(1).perform(click());
        TestDevice.screenshot(mActivityRule.getActivity(), "deletePushTestMechanism");

        onData(anything()).inAdapterView(withId(R.id.grid)).atPosition(1).perform(longClick());
        TestDevice.screenshot(mActivityRule.getActivity(), "deletePushTestLongClick");

        onView(withId(R.id.action_delete)).perform(click());
        TestDevice.screenshot(mActivityRule.getActivity(), "deletePushTestApprove");

        onView(withId(R.id.delete)).perform(click());
        TestDevice.screenshot(mActivityRule.getActivity(), "deletePushTestPostDelete");

    }

    private void generateUser(String issuer, String name, String imageUrl, boolean hasOath, boolean hasPush) throws MechanismCreationException {
        Identity identity = model.addIdentity(Identity.builder().setIssuer(issuer).setAccountName(name).setImageURL(imageUrl));
        if (hasOath) {
            identity.addMechanism(Oath.builder().setType("hotp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID(UUID.randomUUID().toString()));
        }
        if (hasPush) {
            byte[] random = new byte[32];
            new Random().nextBytes(random);
            String base64value = Base64.encode(random);
            push = identity.addMechanism(Push.builder().setMechanismUID(UUID.randomUUID().toString()).setBase64Secret(base64value));
        }
    }

    private void generateNotification(int expiryDelay) throws InvalidNotificationException {
        Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MINUTE, expiryDelay);
        byte[] random = new byte[32];
        new Random().nextBytes(random);
        String base64value = Base64.encode(random);
        push.addNotification(PushNotification.builder().setMessageId("1").setTimeExpired(expiry).setChallenge(base64value));
    }

}
