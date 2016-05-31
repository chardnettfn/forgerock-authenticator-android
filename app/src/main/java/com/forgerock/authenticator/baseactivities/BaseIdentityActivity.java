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

package com.forgerock.authenticator.baseactivities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.forgerock.authenticator.MechanismActivity;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.storage.IdentityModel;

import java.util.ArrayList;

import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;

/**
 * Base class for any activity which requires an Identity to be passed in.
 */
public class BaseIdentityActivity extends BaseActivity {
    /** The key to use to put the opaque reference into the Intent. */
    private static final String IDENTITY_REFERENCE = "identityReference";

    private Identity identity;

    /**
     * Returns the Identity that has been passed into this activity.
     * @return The passed in Identity.
     */
    protected final Identity getIdentity() {
        return identity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<String> identityReference = getIntent().getStringArrayListExtra(IDENTITY_REFERENCE);
        identity = identityModel.getIdentity(identityReference);
        if (identity == null) {
            finish();
        }
    }

    /**
     * Method used for starting an activity that uses an Identity. Handles passing the Identity
     * through to the Activity.
     * @param context The context that the activity is being started from.
     * @param identityActivity The class of activity to start.
     * @param identity The identity to pass.
     */
    public static void start(Context context,
                             Class<? extends BaseIdentityActivity> identityActivity,
                             Identity identity) {
        Intent intent = new Intent(context, identityActivity);
        intent.putExtra(IDENTITY_REFERENCE, identity.getOpaqueReference());
        context.startActivity(intent);
    }
}
