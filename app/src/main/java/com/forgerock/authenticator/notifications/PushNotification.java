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

package com.forgerock.authenticator.notifications;

import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.push.Push;
import com.forgerock.authenticator.utils.MessageUtils;

import org.forgerock.util.encode.Base64;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import roboguice.RoboGuice;

/**
 * Model class which represents a message that was received from an external source. Can provide a
 * generic map of relevant information for storage. Is intended for a given Mechanism.
 */
public class PushNotification extends Notification {
    private static final Logger logger = LoggerFactory.getLogger(PushNotification.class);
    private static final String MESSAGE_ID_KEY = "messageId";
    private static final String RESPONSE_KEY = "response";
    private static final String DENY_KEY = "deny";
    private static final String CHALLENGE_KEY = "challenge";
    private static final String AMLB_COOKIE = "amlbCookie";
    private final String amlbCookie;
    private MessageUtils messageUtils;
    private String messageId;
    private String base64Challenge;

    private PushNotification(Mechanism mechanism, long id, String amlbCookie, Calendar timeAdded, Calendar timeExpired, boolean accepted, boolean active, String messageId, String base64Challenge) {
        super(mechanism, id, timeAdded, timeExpired, accepted, active);
        this.amlbCookie = amlbCookie;
        this.messageId = messageId;
        this.base64Challenge = base64Challenge;
    }

    private MessageUtils getMessageUtils() {
        // Lazy injection used to avoid a loading loop due to an issue with the Roboguice setup.
        if (messageUtils == null) {
            messageUtils = getMechanism().getModel().getInjector().getInstance(MessageUtils.class);
        }
        return messageUtils;
    }

    @Override
    public Map<String, String> getData() {
        Map<String, String> data = new HashMap<>();
        data.put(MESSAGE_ID_KEY, messageId);
        data.put(CHALLENGE_KEY, base64Challenge);
        data.put(AMLB_COOKIE, amlbCookie);
        return data;
    }

    @Override
    protected boolean performAccept() {
        int returnCode = 404;
        try {
            Push push = (Push) getMechanism();
            Map<String, Object> data = new HashMap<>();
            data.put(RESPONSE_KEY, generateChallengeResponse(push.getSecret(), base64Challenge));
            returnCode = getMessageUtils().respond(push.getEndpoint(), amlbCookie, push.getSecret(), messageId, data);
        } catch (IOException | JSONException e) {
            logger.error("Response to server failed.", e);
        }

        return returnCode == 200;
    }

    @Override
    protected boolean performDeny() {
        int returnCode = 404;
        try {
            Push push = (Push) getMechanism();
            Map<String, Object> data = new HashMap<>();
            data.put(RESPONSE_KEY, generateChallengeResponse(push.getSecret(), base64Challenge));
            data.put(DENY_KEY, true);
            returnCode = getMessageUtils().respond(push.getEndpoint(), amlbCookie, push.getSecret(), messageId, data);
        } catch (IOException | JSONException e) {
            logger.error("Response to server failed.", e);
        }

        return returnCode == 200;
    }

    public static String generateChallengeResponse(String base64Secret, String base64Challenge) {
        byte[] secret = Base64.decode(base64Secret);
        byte[] challenge;

        challenge = Base64.decode(base64Challenge);

        Mac hmac = null;
        SecretKey key = new SecretKeySpec(secret, 0, secret.length, "HmacSHA256");
        try {
            hmac = Mac.getInstance("HmacSHA256");
            hmac.init(key);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Failed to generate challenge-response", e);
        }
        byte[] output = hmac.doFinal(challenge);
        return Base64.encode(output);
    }

    /**
     * Get a builder for a PushNotification.
     * @return The Notification builder.
     */
    public static PushNotificationBuilder builder() {
        return new PushNotificationBuilder();
    }

    /**
     * Builder class responsible for producing PushNotifications.
     */
    public static class PushNotificationBuilder extends NotificationBuilder<PushNotificationBuilder> {
        private String messageId;
        private String base64Challenge;
        private String amlbCookie;

        /**
         * Sets the message id that was received with this notification. This will one day be moved
         * to a subclass.
         * @param messageId The messageId that was received.
         */
        public PushNotificationBuilder setMessageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        /**
         * Set the challenge that was sent with this notification.
         * @param challenge The base64 encoded challenge.
         */
        public PushNotificationBuilder setChallenge(String challenge) {
         this.base64Challenge = challenge;
            return this;
        }

        public PushNotificationBuilder setAmlbCookie(String amlbCookie) {
            this.amlbCookie = amlbCookie;
            return this;
        }

        @Override
        protected PushNotificationBuilder getThis() {
            return this;
        }

        @Override
        protected Class<? extends Mechanism> getMechanismClass() {
            return Push.class;
        }

        @Override
        public PushNotificationBuilder setData(Map<String, String> data) {
            this.messageId = data.get(MESSAGE_ID_KEY);
            this.base64Challenge = data.get(CHALLENGE_KEY);
            this.amlbCookie = data.get(AMLB_COOKIE);
            return this;
        }

        @Override
        public PushNotification buildImpl() {
            return new PushNotification(parent, id, amlbCookie, timeAdded, timeExpired, approved, pending, messageId, base64Challenge);
        }
    }
}
