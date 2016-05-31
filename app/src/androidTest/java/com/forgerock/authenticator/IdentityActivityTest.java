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

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.test.suitebuilder.annotation.LargeTest;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.identity.IdentityLayout;
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
import java.util.Collection;

import roboguice.RoboGuice;
import roboguice.util.RoboContext;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;

/**
 * To interact with a UI element: onView(withId(R.id.action_scan)).perform(click());
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class IdentityActivityTest {

    @Rule
    public ActivityTestRule<IdentityActivity> mActivityRule = new ActivityTestRule<>(
            IdentityActivity.class, false, false);
    private IdentityModel model;

    @Before
    public void setup() {
        model = RoboGuice.getInjector(InstrumentationRegistry.getTargetContext().getApplicationContext()).getInstance(IdentityModel.class);
        for (Identity identity : new ArrayList<>(model.getIdentities())) {
            model.removeIdentity(identity);
        }
        //        onView(withId(R.id.action_scan)).perform(click());

    }

    @Test
    public void noIdentities() {
        mActivityRule.launchActivity(new Intent());

        Spoon.screenshot(mActivityRule.getActivity(), "no_identities", getClass().getName(), "noIdentities");
    }

    @Test
    public void oneIdentity() {
        model.addIdentity(Identity.builder().setIssuer("TestIssuer").setAccountName("Acct Name"));
        mActivityRule.launchActivity(new Intent());

        Spoon.screenshot(mActivityRule.getActivity(), "one_identity");

    }

    @Test
    public void oneIdentityOneMechanism() throws Exception {
        Identity identity = model.addIdentity(Identity.builder().setIssuer("TestIssuer").setAccountName("Acct Name"));
        identity.addMechanism(Oath.builder().setType("totp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID("1"));

        mActivityRule.launchActivity(new Intent());

        Spoon.screenshot(mActivityRule.getActivity(), "one_identity_one_mechanism");

    }

    @Test
    public void oneIdentityTwoMechanisms() throws Exception {
        Identity identity = model.addIdentity(Identity.builder().setIssuer("TestIssuer").setAccountName("Acct Name"));
        identity.addMechanism(Oath.builder().setType("totp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID("1"));
        identity.addMechanism(Push.builder().setMechanismUID("1"));

        mActivityRule.launchActivity(new Intent());

        Spoon.screenshot(mActivityRule.getActivity(), "one_identity_two_mechanisms");

    }

    @Test
    public void twoIdentities() {
        model.addIdentity(Identity.builder().setIssuer("TestIssuer A").setAccountName("Acct Name 1"));
        model.addIdentity(Identity.builder().setIssuer("TestIssuer B").setAccountName("Acct Name 2"));
        mActivityRule.launchActivity(new Intent());
        Spoon.screenshot(mActivityRule.getActivity(), "twoIdentities");
    }

    @Test
    public void manyIdentities() {
        for (int i = 0; i < 10; i++) {
            model.addIdentity(Identity.builder().setIssuer("TestIssuer " + i).setAccountName("Acct Name " + i));
        }
        mActivityRule.launchActivity(new Intent());

        Spoon.screenshot(mActivityRule.getActivity(), "manyIdentitiesBase");

        onView(withId(R.id.grid)).perform(swipeUp());

        Spoon.screenshot(mActivityRule.getActivity(), "manyIdentitiesScrollDown");

    }

    @Test
    public void longIssuer() {
        model.addIdentity(Identity.builder().setIssuer("ReallyRatherExcessivelyLongAndYetStillContinuingIssuerWhichShouldHaveEndedByNowButWhichHasn't").setAccountName("Acct Name"));
        mActivityRule.launchActivity(new Intent());

        Spoon.screenshot(mActivityRule.getActivity(), "long_issuer");
    }

    @Test
    public void longAccount() {
        model.addIdentity(Identity.builder().setIssuer("Issuer").setAccountName("ReallyRatherExcessivelyLongAndYetStillContinuingAccountNameWhichShouldHaveEndedByNowButWhichHasn't"));
        mActivityRule.launchActivity(new Intent());

        Spoon.screenshot(mActivityRule.getActivity(), "long_account");
    }

    @Test
    public void longIssuerAndAccount() {
        model.addIdentity(Identity.builder().setIssuer("ReallyRatherExcessivelyLongAndYetStillContinuingIssuerWhichShouldHaveEndedByNowButWhichHasn't")
                .setAccountName("ReallyRatherExcessivelyLongAndYetStillContinuingAccountNameWhichShouldHaveEndedByNowButWhichHasn't"));
        mActivityRule.launchActivity(new Intent());

        Spoon.screenshot(mActivityRule.getActivity(), "long_account_issuer");
    }

    @Test
    public void longIssuerAndAccountOneMechanism() throws Exception {
        Identity identity = model.addIdentity(Identity.builder().setIssuer("ReallyRatherExcessivelyLongAndYetStillContinuingIssuerWhichShouldHaveEndedByNowButWhichHasn't")
                .setAccountName("ReallyRatherExcessivelyLongAndYetStillContinuingAccountNameWhichShouldHaveEndedByNowButWhichHasn't"));
        identity.addMechanism(Oath.builder().setType("totp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID("1"));

        mActivityRule.launchActivity(new Intent());

        Spoon.screenshot(mActivityRule.getActivity(), "long_account_issuer_one_mechanism");
    }

    @Test
    public void longIssuerAndAccountTwoMechanism() throws Exception {
        Identity identity = model.addIdentity(Identity.builder().setIssuer("ReallyRatherExcessivelyLongAndYetStillContinuingIssuerWhichShouldHaveEndedByNowButWhichHasn't")
                .setAccountName("ReallyRatherExcessivelyLongAndYetStillContinuingAccountNameWhichShouldHaveEndedByNowButWhichHasn't"));
        identity.addMechanism(Oath.builder().setType("totp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID("1"));
        identity.addMechanism(Push.builder().setMechanismUID("1"));
        mActivityRule.launchActivity(new Intent());

        Spoon.screenshot(mActivityRule.getActivity(), "long_account_issuer_two_mechanism");
    }

    @Test
    public void longPress() throws Exception {
        Identity identity = model.addIdentity(Identity.builder().setIssuer("ReallyRatherExcessivelyLongAndYetStillContinuingIssuerWhichShouldHaveEndedByNowButWhichHasn't")
                .setAccountName("ReallyRatherExcessivelyLongAndYetStillContinuingAccountNameWhichShouldHaveEndedByNowButWhichHasn't"));

        mActivityRule.launchActivity(new Intent());


        onData(anything()).inAdapterView(withId(R.id.grid))
                .atPosition(0).perform(longClick());

        FalconSpoon.screenshot(mActivityRule.getActivity(), "identityLongPress");

        onView(withId(R.id.action_delete)).perform(click());

        FalconSpoon.screenshot(mActivityRule.getActivity(), "identityLongPressClick");


    }

}

