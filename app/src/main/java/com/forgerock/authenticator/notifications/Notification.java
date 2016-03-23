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

import android.content.Context;

import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.model.ModelObject;
import com.forgerock.authenticator.storage.IdentityDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import roboguice.RoboGuice;

/**
 * Model class which represents a message that was received from an external source. Can provide a
 * generic map of relevant information for storage. Is intended for a given Mechanism.
 */
public class Notification extends ModelObject {
    private final Mechanism parent;
    private final Calendar timeAdded;
    private final Calendar timeExpired;
    private boolean wasSuccessful;
    private static final String MESSAGE_ID = "messageId";
    private long id = NOT_STORED;
    private final String messageId;

    private Notification(Mechanism mechanism, long id, Calendar timeAdded, Calendar timeExpired, boolean wasSuccessful, String messageId) {
        parent = mechanism;
        this.timeAdded = timeAdded;
        this.timeExpired = timeExpired;
        this.wasSuccessful = wasSuccessful;
        this.messageId = messageId;
        this.id = id;
    }

    /**
     * Get the mechanism that this notification was intended for.
     * @return The receiving Mechanism.
     */
    public Mechanism getMechanism() {
        return parent;
    }

    /**
     * Get the time that this notification was received.
     * @return The date the notification was receuved.
     */
    public Calendar getTimeAdded() {
        return timeAdded;
    }

    /**
     * Get the time that the notification will or did expire.
     * @return The expiry date.
     */
    public Calendar getTimeExpired() {
        return timeExpired;
    }

    /**
     * Determine whether the authentication the notification is related to succeeded.
     * @return True if the authentication succeeded, false otherwise.
     */
    public boolean wasSuccessful() {
        return wasSuccessful;
    }

    /**
     * Get the message id that was received with this notification. This will one day be moved
     * to a subclass.
     * @return The messageId used by GCM.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Get all data related to this notification that does not have assocaited fields in the database.
     * The map passed out should be accepted by the builder to recreate this object.
     * @return The map of data.
     */
    public Map<String, String> getData() {
        Map<String, String> data = new HashMap<>();
        data.put(MESSAGE_ID, messageId);
        return data;
    }

    @Override
    public boolean isStored() {
        return id != NOT_STORED;
    }

    @Override
    public void save(Context context) {
        if (id == NOT_STORED) {
            id = RoboGuice.getInjector(context).getInstance(IdentityDatabase.class).addNotification(this);
        } else {
            // handle updates
        }
    }

    @Override
    public void delete(Context context) {
        if (id != NOT_STORED) {
            throw new RuntimeException("Not implemented.");
        }
    }

    @Override
    public boolean validate() {
        return isStored();
    }

    @Override
    public ArrayList<String> getOpaqueReference() {
        ArrayList<String> mechanismReference = getMechanism().getOpaqueReference();
        mechanismReference.add(Long.toString(timeAdded.getTimeInMillis()));
        return mechanismReference;
    }

    @Override
    public boolean consumeOpaqueReference(ArrayList<String> reference) {
        if (reference.size() > 0 && Long.toString(timeAdded.getTimeInMillis()).equals(reference.get(0))) {
            reference.remove(0);
            return true;
        }
        return false;
    }

    /**
     * Get a builder for a Notification.
     * @return The Notification builder.
     */
    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    /**
     * Builder class responsible for producing Notifications.
     */
    public static class NotificationBuilder {
        private Mechanism parent;
        private Calendar timeAdded = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        private boolean wasSuccessful = false;
        private Calendar timeExpired = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        private String messageId;
        private long id = NOT_STORED;

        /**
         * Sets the mechanism which this notification in intended for.
         * @param mechanism The mechanism.
         */
        public NotificationBuilder setMechanism(Mechanism mechanism) {
            this.parent = mechanism;
            return this;
        }

        /**
         * Sets the date that the notification was received.
         * @param timeAdded The date received in UTC.
         */
        public NotificationBuilder setTimeAdded(Calendar timeAdded) {
            this.timeAdded = timeAdded;
            return this;
        }

        /**
         * Sets the date that the notification will automatically fail.
         * @param timeExpired The expiry date.
         */
        public NotificationBuilder setTimeExpired(Calendar timeExpired) {
            this.timeExpired = timeExpired;
            return this;
        }

        /**
         * Sets whether the authentication the notification is related to succeeded.
         * @param successful True if the authentication succeeded, false otherwise.
         */
        public NotificationBuilder setSuccessful(boolean successful) {
            this.wasSuccessful = successful;
            return this;
        }

        /**
         * Sets the message id that was received with this notification. This will one day be moved
         * to a subclass.
         * @param messageId The messageId that was received.
         */
        public NotificationBuilder setMessageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        /**
         * Sets of the miscellaneous data that was stored in the database. At the moment, this
         * consists of messageId.
         * @param data The data from the database.
         */
        public NotificationBuilder setData(Map<String, String> data) {
            this.messageId = data.get(MESSAGE_ID);
            return this;
        }

        /**
         * Sets the storage id of this Identity. Should not be set manually, or if the Identity is
         * not stored.
         * @param id The storage id.
         */
        public NotificationBuilder setId(long id) {
            this.id = id;
            return this;
        }

        /**
         * Build the notification.
         * @return The final notification.
         */
        public Notification build() {
            return new Notification(parent, id, timeAdded, timeExpired, wasSuccessful, messageId);
        }
    }
}
