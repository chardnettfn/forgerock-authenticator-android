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
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.push.Push;
import com.forgerock.authenticator.mechanisms.push.PushAuthActivity;
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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.actionWithAssertions;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * To interact with a UI element: onView(withId(R.id.action_scan)).perform(click());
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PushAuthActivityTest {

    @Rule
    public ActivityTestRule<PushAuthActivity> mActivityRule = new ActivityTestRule<>(
            PushAuthActivity.class, false, false);
    private IdentityModel model;
    private Identity identity;
    private Mechanism mechanism;
    private Notification notification;
    private PushNotification.PushNotificationBuilder baseNotificationBuilder;
    private String base64value;

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
        notification = mechanism.addNotification(baseNotificationBuilder);
    }

    @Test
     public void noContent() {
        mActivityRule.launchActivity(new Intent());
        assertTrue(mActivityRule.getActivity().isFinishing());
    }

    @Test
    public void expired() throws Exception {
        Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MINUTE, -30);
        notification = mechanism.addNotification(PushNotification.builder().setMessageId("1").setTimeExpired(expiry));
        startActivity();
        assertTrue(mActivityRule.getActivity().isFinishing());
    }

    @Test
    public void baseInfo() {
        startActivity();
        FalconSpoon.screenshot(mActivityRule.getActivity(), "baseInfo");
    }

    @Test
    public void approve() {
        startActivity();
        FalconSpoon.screenshot(mActivityRule.getActivity(), "approve");

        onView(withId(R.id.slideToConfirm)).perform(swipeConfirmView());

        assertTrue(mActivityRule.getActivity().isFinishing());
        assertFalse(notification.isPending());
        assertTrue(notification.wasApproved());
    }

    /**
     * Demonstrates the slider behaviour on the far right. Uses different screenshot technique to hide
     * the failed connection popup. Replace when possible by adding to approve()
     */
    @Test
    public void hackyApproveSwipedRight() throws Exception {
        Calendar receive = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        identity = model.addIdentity(Identity.builder().setIssuer("TestIssuerNoServer").setAccountName("Acct Name"));

        mechanism = identity.addMechanism(Push.builder().setMechanismUID("2").setBase64Secret(base64value));

        baseNotificationBuilder = baseNotificationBuilder.setTimeAdded(receive);
        notification = mechanism.addNotification(baseNotificationBuilder);

        startActivity();
        FalconSpoon.screenshot(mActivityRule.getActivity(), "approveWithNoServerHack");

        onView(withId(R.id.slideToConfirm)).perform(swipeConfirmView());

        // Used to ignore the dialog in the screenshot
        Spoon.screenshot(mActivityRule.getActivity(), "approveWithNoServerPostSwipeHack");


    }

    @Test
    public void approveWithNoServer() throws Exception {
        Calendar receive = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        identity = model.addIdentity(Identity.builder().setIssuer("TestIssuerNoServer").setAccountName("Acct Name"));

        mechanism = identity.addMechanism(Push.builder().setMechanismUID("2").setBase64Secret(base64value));

        baseNotificationBuilder = baseNotificationBuilder.setTimeAdded(receive);
        notification = mechanism.addNotification(baseNotificationBuilder);

        startActivity();
        FalconSpoon.screenshot(mActivityRule.getActivity(), "approveWithNoServer");

        onView(withId(R.id.slideToConfirm)).perform(swipeConfirmView());

        assertTrue(notification.isPending());
        FalconSpoon.screenshot(mActivityRule.getActivity(), "approveWithNoServerPostSwipe");


    }

    @Test
    public void deny() {
        startActivity();
        FalconSpoon.screenshot(mActivityRule.getActivity(), "deny");

        onView(withId(R.id.cancel)).perform(click());

        assertTrue(mActivityRule.getActivity().isFinishing());
        assertFalse(notification.isPending());
        assertFalse(notification.wasApproved());
    }

    @Test
    public void denyWithNoServer() throws Exception {
        Calendar receive = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        identity = model.addIdentity(Identity.builder().setIssuer("TestIssuerNoServer").setAccountName("Acct Name"));

        mechanism = identity.addMechanism(Push.builder().setMechanismUID("2").setBase64Secret(base64value));

        baseNotificationBuilder = baseNotificationBuilder.setTimeAdded(receive);
        notification = mechanism.addNotification(baseNotificationBuilder);

        startActivity();
        FalconSpoon.screenshot(mActivityRule.getActivity(), "denyWithNoServer");

        onView(withId(R.id.cancel)).perform(click()).perform();

        assertTrue(notification.isPending());
        FalconSpoon.screenshot(mActivityRule.getActivity(), "denyWithNoServerPostSwipe");


    }

    private void startActivity() {
        Intent intent = new Intent();
        intent.putExtra("notificationReference", notification.getOpaqueReference());
        mActivityRule.launchActivity(intent);
    }

    // Reproduced from GeneralSwipeAction.java
    private ViewAction swipeConfirmView() {
        return actionWithAssertions(new GeneralSwipeAction(Swipe.FAST,
                GeneralLocation.CENTER_LEFT,
                translate(GeneralLocation.CENTER_RIGHT, 10, 0), Press.FINGER));
    }

    // Reproduced from GeneralLocation.java
    private CoordinatesProvider translate(final GeneralLocation location, final float x, final float y) {
        return new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float xy[] = location.calculateCoordinates(view);
                xy[0] += x * view.getWidth();
                xy[1] += y * view.getHeight();
                return xy;
            }
        };
    }
}

