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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import com.forgerock.authenticator.baseactivities.BaseActivity;
import com.forgerock.authenticator.storage.IdentityModel;
import com.forgerock.authenticator.storage.Settings;

import roboguice.RoboGuice;

/**
 * Activity for displaying an animated splash screen to the user. Skips itself if any identities are present.
 */
public class SplashActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IdentityModel model = RoboGuice.getInjector(this).getInstance(IdentityModel.class);
        if (!model.getIdentities().isEmpty()) {
            proceed();
        }

        setContentView(R.layout.splash);

        VideoView video = (VideoView) findViewById(R.id.video);

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/"
                + R.raw.splashvideo);

        video.setVideoURI(uri);
        video.setZOrderOnTop(true);

        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                proceed();
            }
        });
        
        // In the event of an error we have decided the best course of action is to skip the video
        video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                proceed();
                return true;
            }
        });

        video.start();
    }

    private void proceed() {
        Intent intent = new Intent(this, IdentityActivity.class);
        startActivity(intent);
        finish();
    }

}
