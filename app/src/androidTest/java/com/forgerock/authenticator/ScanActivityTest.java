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

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.forgerock.authenticator.add.ScanActivity;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.storage.IdentityModel;
import com.jraska.falcon.FalconSpoon;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import roboguice.RoboGuice;

/**
 * To interact with a UI element: onView(withId(R.id.action_scan)).perform(click());
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ScanActivityTest {

    @Rule
    public ActivityTestRule<ScanActivity> mActivityRule = new ActivityTestRule<>(
            ScanActivity.class);
    private IdentityModel model;

    @Before
    public void setup() {
        model = RoboGuice.getInjector(InstrumentationRegistry.getTargetContext().getApplicationContext()).getInstance(IdentityModel.class);
        for (Identity identity : new ArrayList<>(model.getIdentities())) {
            model.removeIdentity(identity);
        }
    }

    @Test
    public void loadCamera() throws Exception {
        TestDevice.screenshot(mActivityRule.getActivity(), "loadCamera");
    }


}

