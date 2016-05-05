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

import com.forgerock.authenticator.mechanisms.InvalidNotificationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.model.ModelObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Model class which represents a message that was received from an external source. Can provide a
 * generic map of relevant information for storage. Is intended for a given Mechanism.
 */
public abstract class Notification extends ModelObject<Notification> {
    private final Mechanism parent;
    private final Calendar timeAdded;
    private final Calendar timeExpired;

    private boolean approved;
    private boolean pending;
    private long id = NOT_STORED;

    protected Notification(Mechanism mechanism, long id, Calendar timeAdded, Calendar timeExpired, boolean approved, boolean pending) {
        super(mechanism.getModel());
        parent = mechanism;
        this.timeAdded = timeAdded;
        this.timeExpired = timeExpired;
        this.approved = approved;
        this.pending = pending;
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
    public boolean wasApproved() {
        return approved;
    }

    /**
     * Get all data related to this notification that does not have associated fields in the database.
     * The map passed out should be approved by the builder to recreate this object.
     * @return The map of data.
     */
    public abstract Map<String, String> getData();

    @Override
    public boolean isStored() {
        return id != NOT_STORED;
    }

    @Override
    public void save() {
        if (id == NOT_STORED) {
            id = getModel().getStorageSystem().addNotification(this);
        } else {
            getModel().getStorageSystem().updateNotification(id, this);
        }
    }

    @Override
    public boolean forceSave() {
        id = getModel().getStorageSystem().addNotification(this);
        return id != NOT_STORED;
    }

    @Override
    public void delete() {
        if (id != NOT_STORED) {
            getModel().getStorageSystem().deleteNotification(id);
            id = NOT_STORED;
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

    @Override
    public boolean matches(Notification other) {
        if (other == null) {
            return false;
        }
        return getMechanism().equals(other.getMechanism()) && timeAdded.getTimeInMillis() == other.timeAdded.getTimeInMillis();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Notification)) {
            return false;
        }
        Notification otherNotification = (Notification) other;

        boolean dataMatches = true;

        Map<String, String> data = getData();
        Map<String, String> otherData = otherNotification.getData();

        for (String key: data.keySet()) {
            if (data.get(key) != null || otherData.get(key) != null) {
                dataMatches &= data.get(key) != null && data.get(key).equals(otherData.get(key));
            }
        }

        return parent.equals(otherNotification.parent)
                && timeAdded.equals(otherNotification.timeAdded)
                && timeExpired.equals(otherNotification.timeExpired)
                && dataMatches;
    }

    @Override
    public int hashCode() {
        List<Object> values = new ArrayList<>();
        values.add(parent);
        values.add(timeAdded);

        Map<String, String> data = getData();

        for (String key: data.keySet()) {
            values.add(data.get(key));
        }

        return Arrays.hashCode(values.toArray());
    }

    /**
     * Determines if the Notification has been interacted with by the user.
     * @return True if the Notification has not been interacted with, false otherwise.
     */
    public final boolean isPending() {
        return pending;
    }

    /**
     * Determine if the notification is active or not.
     * @return True if the notification is active, false if it is a history element.
     */
    public final boolean isActive() {
        return isPending() && !isExpired();
    }

    /**
     * Determine if the notification has expired.
     * @return True if the notification has expired, false otherwise.
     */
    public final boolean isExpired() {
        return timeExpired.getTimeInMillis() < Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                .getTimeInMillis();
    }

    /**
     *
     * @return True if the accept succeeded, false otherwise.
     */
    public final boolean accept() {
        if (isPending() && acceptImpl()) {
            pending = false;
            approved = true;
            save();
            return true;
        }
        return false;
    }

    /**
     * Implementation of the behaviour to perform upon accepting the authentication request.
     * @return True if the operation successfully completed, false otherwise.
     */
    protected abstract boolean acceptImpl();

    /**
     *
     * @return True if the deny succeeded, false otherwise.
     */
    public final boolean deny() {
        if (isPending() && denyImpl()) {
            pending = false;
            approved = false;
            save();
            return true;
        }
        return false;
    }

    /**
     * Implementation of the behaviour to perform upon denying the authentication request.
     * @return True if the operation successfully completed, false otherwise.
     */
    protected abstract boolean denyImpl();

    @Override
    public int compareTo(Notification another) {
        return Long.compare(another.timeAdded.getTimeInMillis(), timeAdded.getTimeInMillis());
    }

    /**
     * Builder class responsible for producing Notifications.
     */
    public abstract static class NotificationBuilder<T extends NotificationBuilder> {
        protected Mechanism parent;
        protected Calendar timeAdded = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        protected boolean approved = false;
        protected Calendar timeExpired = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        protected long id = NOT_STORED;
        protected boolean pending = true;

        protected abstract T getThis();

        protected abstract Class<? extends Mechanism> getMechanismClass();

        /**
         * Sets the date that the notification was received.
         * @param timeAdded The date received in UTC.
         */
        public T setTimeAdded(Calendar timeAdded) {
            this.timeAdded = timeAdded;
            return getThis();
        }

        /**
         * Sets the date that the notification will automatically fail.
         * @param timeExpired The expiry date.
         */
        public T setTimeExpired(Calendar timeExpired) {
            this.timeExpired = timeExpired;
            return getThis();
        }

        /**
         * Sets whether the authentication the notification is related to was approved.
         * @param approved True if the authentication was approved, false otherwise.
         */
        public T setApproved(boolean approved) {
            this.approved = approved;
            return getThis();
        }

        /**
         * Sets whether the authentication the notification is related to has been handled.
         * @param pending True if the authentication has not been handled, false otherwise.
         */
        public T setPending(boolean pending) {
            this.pending = pending;
            return getThis();
        }

        /**
         * Sets of the miscellaneous data that was stored in the database. At the moment, this
         * consists of messageId.
         * @param data The data from the database.
         */
        public abstract T setData(Map<String, String> data);

        /**
         * Sets the storage id of this Identity. Should not be set manually, or if the Identity is
         * not stored.
         * @param id The storage id.
         */
        public T setId(long id) {
            this.id = id;
            return getThis();
        }

        /**
         * Build the notification.
         * @return The final notification.
         */
        public Notification build(Mechanism mechanism) throws InvalidNotificationException {
            if (mechanism == null) {
                throw new InvalidNotificationException("Tried to create notification without a Mechanism");
            }
            if (!getMechanismClass().isInstance(mechanism)) {
                throw new InvalidNotificationException("Tried to attach notification to incorrect type of Mechanism");
            }
            this.parent = mechanism;
            return buildImpl();
        }

        public abstract Notification buildImpl();
    }
}
