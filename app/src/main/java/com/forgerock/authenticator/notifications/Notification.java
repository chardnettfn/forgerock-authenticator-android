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

import com.forgerock.authenticator.mechanisms.InvalidNotificationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.model.ModelObject;
import com.forgerock.authenticator.storage.IdentityDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import roboguice.RoboGuice;

/**
 * Model class which represents a message that was received from an external source. Can provide a
 * generic map of relevant information for storage. Is intended for a given Mechanism.
 */
public abstract class Notification extends ModelObject<Notification> {
    private final Mechanism parent;
    private final Calendar timeAdded;
    private final Calendar timeExpired;

    private boolean accepted;
    private boolean active;
    private long id = NOT_STORED;

    protected Notification(Mechanism mechanism, long id, Calendar timeAdded, Calendar timeExpired, boolean accepted, boolean active) {
        parent = mechanism;
        this.timeAdded = timeAdded;
        this.timeExpired = timeExpired;
        this.accepted = accepted;
        this.active = active;
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
    public boolean wasAccepted() {
        return accepted;
    }

    /**
     * Get all data related to this notification that does not have associated fields in the database.
     * The map passed out should be accepted by the builder to recreate this object.
     * @return The map of data.
     */
    public abstract Map<String, String> getData();

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
            RoboGuice.getInjector(context).getInstance(IdentityDatabase.class).deleteNotification(id);
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


    /**
     * Determines if the Notification is still active.
     * @return True if the Notification is active, false otherwise.
     */
    public final boolean isActive() {
        return active;
    }

    /**
     *
     * @param context The context the notification is being accepted from.
     * @return True if the accept succeeded, false otherwise.
     */
    public final boolean accept(Context context) {
        if (isActive() && acceptImpl()) {
            active = false;
            accepted = true;
            save(context);
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
     * @param context The context the notification is being accepted from.
     * @return True if the deny succeeded, false otherwise.
     */
    public final boolean deny(Context context) {
        if (isActive() && denyImpl()) {
            active = false;
            accepted = false;
            save(context);
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
        protected boolean accepted = false;
        protected Calendar timeExpired = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        protected long id = NOT_STORED;

        protected abstract T getThis();

        protected abstract Class<? extends Mechanism> getMechanismClass();

        /**
         * Sets the mechanism which this notification in intended for.
         * @param mechanism The mechanism.
         */
        public T setMechanism(Mechanism mechanism) throws InvalidNotificationException {
            if (!getMechanismClass().isInstance(mechanism)) {
                throw new InvalidNotificationException("Tried to attach notification to incorrect type of Mechanism");
            }
            this.parent = mechanism;
            return getThis();
        }

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
         * Sets whether the authentication the notification is related to was accepted.
         * @param successful True if the authentication was accepted, false otherwise.
         */
        public T setAccepted(boolean successful) {
            this.accepted = successful;
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
        public abstract Notification build();
    }
}
