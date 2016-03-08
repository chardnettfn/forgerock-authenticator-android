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

package com.forgerock.authenticator.message;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import com.forgerock.authenticator.R;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The GCMRegistrationService registers for GCM message events and retrieves the device token.
 */
public class GcmRegistrationService extends IntentService {
    private final Logger logger;

    /**
     * Default instance of GcmRegistrationService expected to be instantiated
     * by Android framework.
     */
    public GcmRegistrationService() {
        this(GcmRegistrationService.class.getSimpleName(), LoggerFactory.getLogger(GcmRegistrationService.class));
    }

    /**
     * Dependencies exposed version to support unit testing.
     *
     * @param serviceName Non null service name for IntentService
     * @param logger Non null logging instance
     */
    @VisibleForTesting
    public GcmRegistrationService(final String serviceName, final Logger logger) {
        super(serviceName);
        this.logger = logger;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            String token = getToken();
            logger.info("GCM Registration Token: {}", token);

            GcmPubSub.getInstance(this).subscribe(token, "/topics/global", null);
            sharedPreferences.edit().putBoolean(MessageConstants.TOKEN_SENT_TO_SERVER, true).apply();
            handleToken(token);

        } catch (IOException e) {
            logger.warn("Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(MessageConstants.TOKEN_SENT_TO_SERVER, false).apply();
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(
                new Intent(MessageConstants.REGISTRATION_COMPLETE));
    }

    private String getToken() throws IOException {
        InstanceID instanceId = InstanceID.getInstance(this);
        return instanceId.getToken(getString(R.string.gcm_defaultSenderId),
                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
    }

    private void handleToken(String token) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Token");
        intent.putExtra(Intent.EXTRA_TEXT, token);
        intent.setType("text/plain");

        PendingIntent pendIntent = PendingIntent.getActivity(
                this, 1, Intent.createChooser(intent, "Send Email"),
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.forgerock_notification)
                .setContentTitle("New Device Message token received:")
                .setContentText(token)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendIntent)
                .build();

        NotificationManager noteMan =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        noteMan.notify(1, notification);

    }
}
