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

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;

import com.forgerock.authenticator.mechanisms.InvalidNotificationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.push.PushAuthActivity;
import com.forgerock.authenticator.notifications.PushNotification;
import com.forgerock.authenticator.utils.ContextService;
import com.forgerock.authenticator.utils.IntentFactory;
import com.forgerock.authenticator.utils.NotificationFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


/**
 * GCM Service responds to downstream messages from the Google Cloud Messaging (GCM) framework.
 *
 * Responsible for triggering a Permissive Intent which will invoke the notification screen in
 * this App. The body of the GCM message is included in the Intent.
 */
public class GcmService extends RoboGcmListenerService {
    // Place holder for the moment, to be set to something stable
    private static int messageCount = 2;

    private final Logger logger;
    private final IntentFactory intentFactory;
    private final NotificationFactory notificationFactory;
    private final ContextService contextService;

    /**
     * Default instance of GcmService expected to be instantiated by Android framework.
     */
    public GcmService() {
        this(LoggerFactory.getLogger(GcmService.class), new IntentFactory(), new NotificationFactory(), new ContextService());
    }

    /**
     * Dependencies exposed for unit test purposes.
     *
     * @param logger Non null logging instance.
     * @param intentFactory Non null IntentFactory for generating internal Intents.
     * @param notificationFactory Non null NotificationFactory.
     */
    @VisibleForTesting
    public GcmService(final Logger logger, final IntentFactory intentFactory,
                      final NotificationFactory notificationFactory, final ContextService contextService) {
        this.logger = logger;
        this.intentFactory = intentFactory;
        this.notificationFactory = notificationFactory;
        this.contextService = contextService;
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {

        String messageId = data.getString("messageId");
        String message = data.getString("message");

        // TODO: Message contents should not be printed to system log
        logger.info("From: {}", from);
        logger.info("Message: {}", message);

        // TODO: Validate that the message is a correctly formed message from the server.

        handleMessage(messageId, message);
    }

    private void handleMessage(String messageId, String message) {

        int id = messageCount++;
        // TODO: Change activity a list of "unread" messages when there is more than one


        //TODO: extract id from message
        int mechanismId = 5;

        List<Mechanism> mechanismList = identityModel.getMechanisms();

        Mechanism mechanism = null;

        for (Mechanism current : mechanismList) {
            if (current.getMechanismUID() == mechanismId) {
                mechanism = current;
                break;
            }
        }

        if (mechanism == null) {
            return;
        }

        PushNotification.PushNotificationBuilder notificationBuilder =
                PushNotification.builder()
                        .setTimeAdded(Calendar.getInstance(TimeZone.getTimeZone("UTC")))
                        .setTimeExpired(Calendar.getInstance(TimeZone.getTimeZone("UTC")))
                        .setMessageId(messageId);
        com.forgerock.authenticator.notifications.Notification notificationData;
        try {
            notificationData = mechanism.addNotification(this, notificationBuilder);
        } catch (InvalidNotificationException e) {
            logger.error("Received message mapped invalid Notification to Mechanism. Skipping...");
            return;
        }

        /**
         * TODO: Update ID of Intent to match Notification
         * The ID of the Intent and the Notification should be the same and linked to something
         * stable in the downstream message. This will allow us to possibly clear out a
         * notification from the users device if they decide to cancel the login request.
         */
        Intent intent = intentFactory.generateInternal(this, PushAuthActivity.class);
        intent.putExtra(PushAuthActivity.NOTIFICATION_REFERENCE, notificationData.getOpaqueReference());

        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        String title = "Login Detected";
        Notification notification = notificationFactory.generatePending(this, id, title, message, intent);

        NotificationManager notificationManager = contextService.getService(this, Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }
}
