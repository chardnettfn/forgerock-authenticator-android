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
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.oath.Oath;
import com.forgerock.authenticator.mechanisms.push.Push;
import com.forgerock.authenticator.notifications.Notification;
import com.forgerock.authenticator.notifications.PushNotification;
import com.forgerock.authenticator.storage.IdentityModel;
import com.jraska.falcon.FalconSpoon;
import com.squareup.spoon.Spoon;

import org.forgerock.util.encode.Base64;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import roboguice.RoboGuice;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.anything;

/**
 * To interact with a UI element: onView(withId(R.id.action_scan)).perform(click());
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NotificationActivityTest {

    @Rule
    public ActivityTestRule<NotificationActivity> mActivityRule = new ActivityTestRule<>(
            NotificationActivity.class, false, false);
    private IdentityModel model;
    private Identity identity;
    private Mechanism mechanism;
    private String base64value;
    private PushNotification.PushNotificationBuilder baseNotificationBuilder;
    private Notification notification;

    @Before
    public void setup() throws Exception {
        model = RoboGuice.getInjector(InstrumentationRegistry.getTargetContext().getApplicationContext()).getInstance(IdentityModel.class);
        for (Identity identity : new ArrayList<>(model.getIdentities())) {
            model.removeIdentity(identity);
        }
        byte[] random = new byte[32];
        new Random().nextBytes(random);
        base64value = Base64.encode(random);

        identity = model.addIdentity(Identity.builder().setIssuer("TestIssuer").setAccountName("Acct Name"));
        mechanism = identity.addMechanism(Push.builder().setMechanismUID("1").setBase64Secret(base64value).setAuthEndpoint("http://www.example.com"));
        Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MINUTE, 30);


        baseNotificationBuilder = PushNotification.builder().setMessageId("1").setTimeExpired(expiry).setChallenge(base64value);
    }

    @Test
    public void noContent() {
        mActivityRule.launchActivity(new Intent());

        assertTrue(mActivityRule.getActivity().isFinishing());
    }

    @Test
    public void activeNotification() throws Exception {
        notification = mechanism.addNotification(baseNotificationBuilder);
        startActivity();

        FalconSpoon.screenshot(mActivityRule.getActivity(), "activeNotification");
    }

    @Test
    public void approvedNotification() throws Exception {
        baseNotificationBuilder.setPending(false).setApproved(true);
        notification = mechanism.addNotification(baseNotificationBuilder);
        startActivity();

        FalconSpoon.screenshot(mActivityRule.getActivity(), "approvedNotification");
    }

    @Test
    public void deniedNotification() throws Exception {
        baseNotificationBuilder.setPending(false).setApproved(false);
        notification = mechanism.addNotification(baseNotificationBuilder);
        startActivity();

        FalconSpoon.screenshot(mActivityRule.getActivity(), "deniedNotification");
    }

    @Test
    public void expiredNotification() throws Exception {
        Calendar outOfTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        outOfTime.add(Calendar.HOUR, -1);
        baseNotificationBuilder.setTimeExpired(outOfTime);
        notification = mechanism.addNotification(baseNotificationBuilder);
        startActivity();

        FalconSpoon.screenshot(mActivityRule.getActivity(), "expiredNotification");
    }

    @Test
    public void manyNotifications() throws Exception {
        for (int i = 0; i < 10; i++) {
            Calendar timeAdded = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            timeAdded.add(Calendar.MINUTE, -i);
            baseNotificationBuilder.setPending(i < 5).setApproved(i >= 8).setTimeAdded(timeAdded);
            mechanism.addNotification(baseNotificationBuilder);
        }

        startActivity();

        FalconSpoon.screenshot(mActivityRule.getActivity(), "manyNotificationsBase");

        onView(withId(R.id.notification_list)).perform(swipeUp());

        FalconSpoon.screenshot(mActivityRule.getActivity(), "manyNotificationsScroll");
    }


    private void startActivity() {
        Intent intent = new Intent();
        intent.putExtra("mechanismReference", mechanism.getOpaqueReference());
        mActivityRule.launchActivity(intent);
    }
}

