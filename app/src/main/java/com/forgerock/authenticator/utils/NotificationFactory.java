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
package com.forgerock.authenticator.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import com.forgerock.authenticator.R;

/**
 * Responsible for generating Notifications within the App.
 *
 * Encapsulates the detail of generating an notification, and provide a single point for all notification generation.
 */
public class NotificationFactory {

    private final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    /**
     * Generate a default pending Notification which will trigger an Intent when the user responds to the
     * notification.
     *
     * A one-shot notification with automatic cancellation and default sound effect.
     *
     * @param context Required for Android operations.
     * @param requestCode The ID of the request, allowing multiple notifications to be grouped.
     * @param title Non null title of the notification.
     * @param message Non null body of the notification.
     * @param intent Non null Intent to trigger when the user acknowledges the Intent.
     * @return Non null Notification.
     */
    public Notification generatePending(Context context, int requestCode, String title, String message, Intent intent) {
        PendingIntent pendIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.forgerock_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendIntent)
                .build();

        return notification;
    }
}
