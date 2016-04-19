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

package com.forgerock.authenticator.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class for wrapping the settings that can be applied to the app. Designed to be passed around
 * using Guice.
 */
public class Settings {
    private static final String SETTINGS_NAME = "fr_auth_settings";
    private static final String CAMERA_ENABLED_SETTING = "camera_enabled";

    private final SharedPreferences sharedPreferences;

    private boolean cameraEnabled;

    /**
     * Load the settings from SharedPreferences, or set default values if they are not there.
     * @param context The context for the SharedPreferences.
     */
    public Settings(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
        // Load settings from SharedPreferences, or initialise to defaults otherwise
        cameraEnabled = sharedPreferences.getBoolean(CAMERA_ENABLED_SETTING, true);

        if (!sharedPreferences.contains(CAMERA_ENABLED_SETTING)) {
            sharedPreferences.edit().putBoolean(CAMERA_ENABLED_SETTING, cameraEnabled).apply();
        }
    }

    /**
     * Set whether use of the camera is enabled.
     * @param enabled True if the camera should be enabled, false otherwise.
     */
    public void setCameraEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(CAMERA_ENABLED_SETTING, enabled).apply();
        cameraEnabled = enabled;
    }

    /**
     * Returns whether the camera is enabled.
     * @return True if the camera is enabled, false otherwise.
     */
    public boolean isCameraEnabled() {
        return cameraEnabled;
    }
}
