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

import com.forgerock.authenticator.baseactivities.BaseIdentityActivity;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.oath.Oath;
import com.forgerock.authenticator.mechanisms.push.Push;
import com.forgerock.authenticator.storage.IdentityModel;
import com.jraska.falcon.FalconSpoon;
import com.squareup.spoon.Spoon;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

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
public class MechanismActivityTest {

    @Rule
    public ActivityTestRule<MechanismActivity> mActivityRule = new ActivityTestRule<>(
            MechanismActivity.class, false, false);
    private IdentityModel model;
    private Identity identity;

    @Before
    public void setup() {
        model = RoboGuice.getInjector(InstrumentationRegistry.getTargetContext().getApplicationContext()).getInstance(IdentityModel.class);
        for (Identity identity : new ArrayList<>(model.getIdentities())) {
            model.removeIdentity(identity);
        }
        identity = model.addIdentity(Identity.builder().setIssuer("TestIssuer").setAccountName("Acct Name"));
    }

    @Test
    public void noContent() {
        startActivity();

        assertTrue(mActivityRule.getActivity().isFinishing());
    }

    @Test
    public void oathTOTP() throws Exception {
        identity.addMechanism(Oath.builder().setType("totp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID("1"));

        startActivity();

        Spoon.screenshot(mActivityRule.getActivity(), "oathTOTP");

    }

    @Test
    public void oathHOTP() throws Exception {
        identity.addMechanism(Oath.builder().setType("hotp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID("1"));

        startActivity();

        Spoon.screenshot(mActivityRule.getActivity(), "oathHOTPDefault");

        onData(anything()).inAdapterView(withId(R.id.grid))
                .atPosition(0).onChildView(withId(R.id.refresh)).perform(click());

        Spoon.screenshot(mActivityRule.getActivity(), "oathHOTPPress");
    }

    @Test
    public void push() throws Exception {
        identity.addMechanism(Push.builder().setMechanismUID("1"));

        startActivity();

        Spoon.screenshot(mActivityRule.getActivity(), "push");
    }

    @Test
    public void twoMechanisms() throws Exception {
        identity.addMechanism(Oath.builder().setType("hotp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID("1"));
        identity.addMechanism(Push.builder().setMechanismUID("2"));

        startActivity();

        Spoon.screenshot(mActivityRule.getActivity(), "twoMechanisms");
    }

    @Test
    public void longPressOath() throws Exception {
        identity.addMechanism(Oath.builder().setType("totp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID("1"));

        startActivity();

        onData(anything()).inAdapterView(withId(R.id.grid))
                .atPosition(0).perform(longClick());

        FalconSpoon.screenshot(mActivityRule.getActivity(), "longPressOath");

        onView(withId(R.id.action_delete)).perform(click());

        FalconSpoon.screenshot(mActivityRule.getActivity(), "longPressOathClickDelete");


    }

    @Test
    public void longPressPush() throws Exception {
        identity.addMechanism(Push.builder().setMechanismUID("1"));

        startActivity();

        onData(anything()).inAdapterView(withId(R.id.grid))
                .atPosition(0).perform(longClick());

        FalconSpoon.screenshot(mActivityRule.getActivity(), "longPressPush");

        onView(withId(R.id.action_delete)).perform(click());

        FalconSpoon.screenshot(mActivityRule.getActivity(), "longPressPushClickDelete");


    }

    private void startActivity() {
        Intent intent = new Intent();
        intent.putExtra("identityReference", identity.getOpaqueReference());
        mActivityRule.launchActivity(intent);
    }
}

