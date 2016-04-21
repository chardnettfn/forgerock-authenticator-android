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

import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.push.Push;
import com.forgerock.authenticator.utils.MessageUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class which represents a message that was received from an external source. Can provide a
 * generic map of relevant information for storage. Is intended for a given Mechanism.
 */
public class PushNotification extends Notification {
    private static final Logger logger = LoggerFactory.getLogger(PushNotification.class);
    private static final String MESSAGE_ID = "messageId";
    private String messageId;

    private PushNotification(Mechanism mechanism, long id, Calendar timeAdded, Calendar timeExpired, boolean accepted, boolean active, String messageId) {
        super(mechanism, id, timeAdded, timeExpired, accepted, active);
        this.messageId = messageId;
    }

    @Override
    public Map<String, String> getData() {
        Map<String, String> data = new HashMap<>();
        data.put(MESSAGE_ID, messageId);
        return data;
    }

    @Override
    protected boolean acceptImpl() {
        int returnCode = 404;
        try {
            returnCode = MessageUtils.respond(((Push) getMechanism()).getEndpoint(), messageId, new HashMap<String, String>());
        } catch (IOException | JSONException e) {
            logger.error("Response to server failed.", e);
        }

        return returnCode == 200;
    }

    @Override
    protected boolean denyImpl() {
        return true;
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

        /**
         * Sets the message id that was received with this notification. This will one day be moved
         * to a subclass.
         * @param messageId The messageId that was received.
         */
        public PushNotificationBuilder setMessageId(String messageId) {
            this.messageId = messageId;
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
            this.messageId = data.get(MESSAGE_ID);
            return this;
        }

        @Override
        public PushNotification buildImpl() {
            return new PushNotification(parent, id, timeAdded, timeExpired, approved, pending, messageId);
        }
    }
}
