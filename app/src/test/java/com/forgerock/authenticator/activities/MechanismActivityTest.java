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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import com.forgerock.authenticator.BuildConfig;
import com.forgerock.authenticator.IdentityActivity;
import com.forgerock.authenticator.MechanismActivity;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.TestGuiceModule;
import com.forgerock.authenticator.add.ScanActivity;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.storage.IdentityModel;
import com.forgerock.authenticator.support.MockIdentityBuilder;
import com.squareup.picasso.MockPicasso;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.util.ArrayList;

import roboguice.RoboGuice;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MechanismActivityTest {

    private IdentityModel model;
    private final String ISSUER = "testIssuer";
    private final String ACCOUNT_NAME = "testAccount";
    private final String BG_COLOR = "#AABBCC";
    private final Uri IMAGE_URI = Uri.parse("http://www.example.com/image.png");

    @Before
    public void setUp() {
        RoboGuice.setUseAnnotationDatabases(false);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, RoboGuice.newDefaultRoboModule(RuntimeEnvironment.application), new TestGuiceModule());
        model = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(IdentityModel.class);
        MockPicasso.init();
    }

    @Test
    public void identityAttributesShouldBeCorrectlyUsed() {
        // Given
        ArrayList<String> opaqueReference = new ArrayList<>();
        opaqueReference.add("thisisanopaquereference");

        Identity identity = new MockIdentityBuilder()
                .withIssuer(ISSUER)
                .withAccountName(ACCOUNT_NAME)
                .withBackgroundColor(BG_COLOR)
                .withImageUri(IMAGE_URI).build();
        given(model.getIdentity(opaqueReference)).willReturn(identity);

        Intent intent = new Intent();
        intent.putExtra("identityReference", opaqueReference);

        // When
        MechanismActivity activity = Robolectric.buildActivity(MechanismActivity.class).withIntent(intent).create().get();

        // Expect
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        assertEquals(ISSUER, ((TextView) shadowActivity.findViewById(R.id.issuer)).getText());
        assertEquals(ACCOUNT_NAME, ((TextView) shadowActivity.findViewById(R.id.account_name)).getText());

        int backgroundColor = ((ColorDrawable) shadowActivity.findViewById(R.id.header).getBackground()).getColor();
        assertEquals(Color.parseColor(BG_COLOR), backgroundColor);

        assertEquals(IMAGE_URI, MockPicasso.loadedUris.get(0));

    }
}
