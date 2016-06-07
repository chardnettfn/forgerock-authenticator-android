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

package com.forgerock.authenticator.identity;

import android.app.Activity;
import android.content.Intent;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.forgerock.authenticator.BuildConfig;
import com.forgerock.authenticator.MechanismActivity;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.TestGuiceModule;
import com.forgerock.authenticator.delete.DeleteIdentityActivity;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.base.MechanismInfo;
import com.forgerock.authenticator.storage.IdentityModel;
import com.forgerock.authenticator.ui.MechanismIcon;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowImageView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import roboguice.RoboGuice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class IdentityLayoutTest {
    private final String ISSUER = "issuer";
    private final String ACCOUNT_NAME = "account";
    private final int IMAGE_A = R.drawable.forgerock_icon_oath;
    private final int IMAGE_B = R.drawable.forgerock_icon_notification;
    private IdentityModel model;

    @Before
    public void setUp() {
        RoboGuice.setUseAnnotationDatabases(false);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, RoboGuice.newDefaultRoboModule(RuntimeEnvironment.application), new TestGuiceModule());

        model = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(IdentityModel.class);
    }

    @Test
    public void shouldHandleClickCorrectly() {
        Identity identity = Identity.builder().setIssuer(ISSUER).setAccountName(ACCOUNT_NAME).build(model);
        Activity activity = Robolectric.setupActivity(Activity.class);
        IdentityLayout layout = (IdentityLayout) LayoutInflater.from(activity).inflate(R.layout.identitycell, null);
        layout.bind(identity);

        layout.performClick();

        Intent expectedIntent = new Intent(activity, MechanismActivity.class);
        expectedIntent.putExtra("identityReference", identity.getOpaqueReference());
        assertEquals(shadowOf(activity).getNextStartedActivity(), expectedIntent);
    }

    @Test
    public void shouldDisplayInformationCorrectly() {

        Identity identity = Identity.builder().setIssuer(ISSUER).setAccountName(ACCOUNT_NAME).build(model);
        IdentityLayout layout = setupLayout(identity);

        assertEquals(((TextView) layout.findViewById(R.id.issuer)).getText(), ISSUER);
        assertEquals(((TextView) layout.findViewById(R.id.label)).getText(), ACCOUNT_NAME);
        assertEquals((layout.findViewById(R.id.iconA)).getVisibility(), View.GONE);
        assertEquals((layout.findViewById(R.id.iconB)).getVisibility(), View.GONE);
    }

    @Test
    public void shouldHandleOneMechanismCorrectly() {
        Identity identity = mock(Identity.class);
        List<Mechanism> mechanismList = Collections.singletonList(setupMockMechanism(IMAGE_A));
        given(identity.getMechanisms()).willReturn(mechanismList);
        IdentityLayout layout = setupLayout(identity);

        MechanismIcon iconA = (MechanismIcon) layout.findViewById(R.id.iconA);
        ShadowImageView iconAimage = shadowOf((ImageView) iconA.findViewById(R.id.icon_image));
        assertEquals(iconA.getVisibility(), View.VISIBLE);
        assertEquals(iconAimage.getImageResourceId(), IMAGE_A);

        MechanismIcon iconB = (MechanismIcon) layout.findViewById(R.id.iconB);
        assertEquals(iconB.getVisibility(), View.GONE);
    }

    @Test
    public void shouldHandleTwoMechanismsCorrectly() {
        Identity identity = mock(Identity.class);
        List<Mechanism> mechanismList = Arrays.asList(setupMockMechanism(IMAGE_A), setupMockMechanism(IMAGE_B));
        given(identity.getMechanisms()).willReturn(mechanismList);
        IdentityLayout layout = setupLayout(identity);

        MechanismIcon iconA = (MechanismIcon) layout.findViewById(R.id.iconA);
        ShadowImageView iconAImage = shadowOf((ImageView) iconA.findViewById(R.id.icon_image));
        assertEquals(iconA.getVisibility(), View.VISIBLE);
        assertEquals(iconAImage.getImageResourceId(), IMAGE_A);

        MechanismIcon iconB = (MechanismIcon) layout.findViewById(R.id.iconB);
        ShadowImageView iconBImage = shadowOf((ImageView) iconB.findViewById(R.id.icon_image));
        assertEquals(iconB.getVisibility(), View.VISIBLE);
        assertEquals(iconBImage.getImageResourceId(), IMAGE_B);
    }

    @Test
    public void shouldBeAbleToDeleteViaContextualActionBar() {
        Identity identity = Identity.builder().setIssuer(ISSUER).setAccountName(ACCOUNT_NAME).build(model);
        Activity activity = Robolectric.setupActivity(Activity.class);

        // ActionModes can only be created when the View creating it has a parent. Therefore, we
        // must create a dummy FrameLayout to contain our test Views whenever we wish to test
        // ActionModes or Contextual Action Bars.
        FrameLayout frameLayout = new FrameLayout(activity);
        activity.addContentView(frameLayout,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        IdentityLayout layout = (IdentityLayout) LayoutInflater.from(activity).inflate(R.layout.identitycell, null);
        layout.bind(identity);

        frameLayout.addView(layout);

        layout.performLongClick();

        ActionMode actionMode = layout.getActionMode();
        actionMode.getMenu().performIdentifierAction(R.id.action_delete, 0);

        Intent expectedIntent = new Intent(activity, DeleteIdentityActivity.class);
        expectedIntent.putExtra("identityReference", identity.getOpaqueReference());
        assertEquals(shadowOf(activity).getNextStartedActivity(), expectedIntent);
    }

    private IdentityLayout setupLayout(Identity identity) {
        Activity activity = Robolectric.setupActivity(Activity.class);
        IdentityLayout layout = (IdentityLayout) LayoutInflater.from(activity).inflate(R.layout.identitycell, null);
        layout.bind(identity);
        return layout;
    }

    private Mechanism setupMockMechanism(int imageId) {
        Mechanism mechanism = mock(Mechanism.class);
        MechanismInfo mechanismInfo = mock(MechanismInfo.class);
        when(mechanism.getInfo()).thenReturn(mechanismInfo);
        given(mechanismInfo.getIcon()).willReturn(imageId);
        return mechanism;
    }
}
