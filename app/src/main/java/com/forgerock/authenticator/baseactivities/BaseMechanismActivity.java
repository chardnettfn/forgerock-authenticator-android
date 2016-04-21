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
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.storage.IdentityModel;

import java.util.ArrayList;

import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;

/**
 * Base class for any activity which requires a Mechanism to be passed in.
 */
public class BaseMechanismActivity extends BaseActivity {
    /** The key to use to put the opaque reference into the Intent. */
    private static final String MECHANISM_REFERENCE = "mechanismReference";

    private Mechanism mechanism;

    /**
     * Returns the Mechanism that has been passed into this activity.
     * @return The passed in Mechanism.
     */
    protected final Mechanism getMechanism() {
        return mechanism;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<String> mechanismReference = getIntent().getStringArrayListExtra(MECHANISM_REFERENCE);
        mechanism = identityModel.getMechanism(mechanismReference);
    }

    /**
     * Method used for starting an activity that uses a Mechanism. Handles passing the Mechanism
     * through to the Activity.
     * @param context The context that the activity is being started from.
     * @param mechanismActivity The class of activity to start.
     * @param mechanism The mechanism to pass.
     */
    public static void start(Context context,
                             Class<? extends BaseMechanismActivity> mechanismActivity,
                             Mechanism mechanism) {
        Intent intent = new Intent(context, mechanismActivity);
        intent.putExtra(MECHANISM_REFERENCE, mechanism.getOpaqueReference());
        context.startActivity(intent);
    }
}
