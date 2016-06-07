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

package com.forgerock.authenticator.activities;

import android.Manifest;
import android.content.Intent;

import com.forgerock.authenticator.BuildConfig;
import com.forgerock.authenticator.IdentityActivity;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.TestGuiceModule;
import com.forgerock.authenticator.add.ScanActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import roboguice.RoboGuice;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class IdentityActivityTest {

    @Before
    public void setUp() {
        RoboGuice.setUseAnnotationDatabases(false);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, RoboGuice.newDefaultRoboModule(RuntimeEnvironment.application), new TestGuiceModule());
    }

    @Test
    public void clickingScanShouldStartScanActivity() {
        IdentityActivity activity = Robolectric.setupActivity(IdentityActivity.class);

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        shadowActivity.grantPermissions(Manifest.permission.CAMERA);

        shadowActivity.clickMenuItem(R.id.action_scan);


        Intent expectedIntent = new Intent(activity, ScanActivity.class);
        Intent actualIntent = shadowOf(activity).getNextStartedActivity();
        assertEquals(actualIntent, expectedIntent);
    }

}
