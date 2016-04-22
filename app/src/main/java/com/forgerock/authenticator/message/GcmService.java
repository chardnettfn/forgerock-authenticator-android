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

import com.forgerock.authenticator.baseactivities.BaseNotificationActivity;
import com.forgerock.authenticator.mechanisms.InvalidNotificationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.push.Push;
import com.forgerock.authenticator.mechanisms.push.PushAuthActivity;
import com.forgerock.authenticator.notifications.PushNotification;
import com.forgerock.authenticator.utils.ContextService;
import com.forgerock.authenticator.utils.NotificationFactory;

import org.forgerock.json.jose.common.JwtReconstruction;
import org.forgerock.json.jose.exceptions.JwtReconstructionException;
import org.forgerock.json.jose.jws.SignedJwt;
import org.forgerock.json.jose.jws.SigningManager;
import org.forgerock.json.jose.jws.handlers.SigningHandler;
import org.forgerock.util.encode.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


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
    private final NotificationFactory notificationFactory;
    private final ContextService contextService;

    private static final String MESSAGE = "message";
    private static final String CHALLENGE = "c";
    private static final String MESSAGE_ID = "messageId";
    private static final String MECHANISM_UID = "u";

    /**
     * Default instance of GcmService expected to be instantiated by Android framework.
     */
    public GcmService() {
        logger = LoggerFactory.getLogger(GcmService.class);
        notificationFactory = new NotificationFactory();
        contextService = new ContextService();
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String messageId = data.getString(MESSAGE_ID);
        handleMessage(messageId, data.getString(MESSAGE));
    }

    private boolean verify(String base64Secret, SignedJwt signedJwt) {

        byte[] secret = Base64.decode(base64Secret);
        SigningHandler signingHandler = new SigningManager().newHmacSigningHandler(secret);
        return signedJwt.verify(signingHandler);
    }

    private void handleMessage(String messageId, String jwtString) {
        SignedJwt signedJwt;
        try {
            signedJwt = new JwtReconstruction().reconstructJwt(jwtString, SignedJwt.class);
        } catch (JwtReconstructionException e) {
            logger.error("Failed to reconstruct JWT.");
            return;
        }
        String mechanismUid = (String) signedJwt.getClaimsSet().getClaim(MECHANISM_UID);
        String base64Challenge = (String) signedJwt.getClaimsSet().getClaim(CHALLENGE);

        if (messageId == null || mechanismUid == null || base64Challenge == null) {
            logger.error("Message did not contain required fields.");
            return;
        }

        int id = messageCount++;
        // TODO: Change activity a list of "unread" messages when there is more than one

        List<Mechanism> mechanismList = identityModel.getMechanisms();

        Push push = null;

        for (Mechanism current : mechanismList) {
            if (current.getMechanismUID().equals(mechanismUid)) {
                push = (Push) current;
                break;
            }
        }

        if (push == null) {
            return;
        }

        if (!verify(push.getSecret(), signedJwt)) {
            logger.error("Failed to validate jwt.");
            return;
        }

        Calendar timeReceived = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar timeExpired = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        timeExpired.setTimeInMillis(timeReceived.getTimeInMillis() + 3600000);

        PushNotification.PushNotificationBuilder notificationBuilder =
                PushNotification.builder()
                        .setTimeAdded(timeReceived)
                        .setTimeExpired(timeExpired)
                        .setMessageId(messageId)
                        .setChallenge(base64Challenge);
        com.forgerock.authenticator.notifications.Notification notificationData;
        try {
            notificationData = push.addNotification(notificationBuilder);
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
        Intent intent = BaseNotificationActivity.setupIntent(this, PushAuthActivity.class, notificationData);

        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        String title = "Login Detected";
        Notification notification = notificationFactory.generatePending(this, id, title, "Login Detected", intent); //TODO: update the notification strings

        NotificationManager notificationManager = contextService.getService(this, Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }
}
