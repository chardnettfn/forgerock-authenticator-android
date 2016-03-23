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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.forgerock.authenticator.baseactivities.BaseActivity;
import com.forgerock.authenticator.storage.Settings;

import roboguice.RoboGuice;
import roboguice.activity.RoboActivity;

/**
 * Page for viewing and changing settings related to the app, as well as displaying extra information.
 */
public class SettingsActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings);

        final Switch cameraButton = (Switch) findViewById(R.id.camera_button);
        cameraButton.setChecked(settings.isCameraEnabled());
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setCameraEnabled(cameraButton.isChecked());
            }
        });


        // Todo: detect if touch id is available - if not, hide the button
        final Switch touchIDButton = (Switch) findViewById(R.id.touch_id_button);
        touchIDButton.setChecked(settings.isTouchIdEnabled());
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setTouchIdEnabled(touchIDButton.isChecked());
            }
        });

        final Context context = this;
        final Button about_button = (Button) findViewById(R.id.about_button);
        about_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, AboutActivity.class));
            }
        });
    }
}
