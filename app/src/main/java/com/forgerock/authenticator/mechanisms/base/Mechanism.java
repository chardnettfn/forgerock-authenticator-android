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

package com.forgerock.authenticator.mechanisms.base;

import android.support.annotation.VisibleForTesting;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.InvalidNotificationException;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.model.ModelObject;
import com.forgerock.authenticator.model.SortedList;
import com.forgerock.authenticator.notifications.Notification;
import com.forgerock.authenticator.utils.TimeKeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A mechanism used for authentication.
 * Encapsulates the related settings, as well as an owning Identity and may have a unique UID set.
 */
public abstract class Mechanism extends ModelObject<Mechanism> {
    private long id = NOT_STORED;
    private final String mechanismUID;
    private final Identity owner;
    private final List<Notification> notificationList;

    private static final Logger logger = LoggerFactory.getLogger(Mechanism.class);

    /**
     * Base constructor which encapsulates common elements of all Mechanisms.
     * @param owner The owner of the Mechanism.
     * @param id The storage id of the Mechanism.
     * @param mechanismUID The ID used to identify the Mechanism to external systems.
     */
    protected Mechanism(Identity owner, long id, String mechanismUID) {
        super(owner.getModel());
        notificationList = new SortedList<>();
        this.owner = owner;
        this.mechanismUID = mechanismUID;
        this.id = id;
    }

    /**
     * Adds the provided notification to this Mechanism, and therefore to the larger data model.
     * @param notificationBuilder An incomplete builder for a non stored notification.
     * @return The notification that has been added to the data model.
     */
    public Notification addNotification(Notification.NotificationBuilder notificationBuilder)
            throws InvalidNotificationException {
        Notification notification = notificationBuilder.build(this);
        if (!notificationList.contains(notification)) {
            notification.save();
            notificationList.add(notification);
            getModel().notifyNotificationChanged();
        }
        return notification;
    }

    /**
     * Gets all of the notifications that belong to this Mechanism.
     * @return The list of notifications.
     */
    public List<Notification> getNotifications() {
        return Collections.unmodifiableList(notificationList);
    }

    /**
     * Delete inactive notifications from this Mechanism.
     */
    public void clearInactiveNotifications() {
        List<Notification> deleteList = new ArrayList<>(notificationList);
        for (Notification notification : deleteList) {
            if (!notification.isActive()){
                notification.delete();
                notificationList.remove(notification);
            }
        }
        getModel().notifyNotificationChanged();
    }

    /**
     * Delete a single notification from this Mechanism.
     * @param notification The notification to delete.
     */
    public void removeNotification(Notification notification) {
        notification.delete();
        notificationList.remove(notification);
        getModel().notifyNotificationChanged();
    }

    /**
     * Gets the version number for this mechanism.
     * @return The Mechanism version number.
     */
    public abstract int getVersion();

    /**
     * Returns the Mechanism's properties as a map of properties. The factory should be able to use
     * this map to recreate the Mechanism.
     * @return The Mechanism's properties.
     */
    public abstract Map<String, String> asMap();

    /**
     * Gets the MechanismInfo which describes this Mechanism.
     * @return The related MechanismInfo.
     */
    public abstract MechanismInfo getInfo();

    /**
     * Returns the identity which owns this Mechanism.
     * @return The owning identity.
     */
    public Identity getOwner() {
        return owner;
    }

    /**
     * Returns the unique mechanism id used to identify this mechanism with the server.
     * @return The mechanism UID if one is set, 0 otherwise.
     */
    public String getMechanismUID() {
        return mechanismUID;
    }

    @Override
    public boolean isStored() {
        return id != NOT_STORED;
    }

    @Override
    public void save() {
        if (!isStored()) {
            id = getModel().getStorageSystem().addMechanism(this);
        } else {
            getModel().getStorageSystem().updateMechanism(id, this);
        }
    }

    @Override
    public boolean forceSave() {
        id = getModel().getStorageSystem().addMechanism(this);
        return id != NOT_STORED;
    }

    @Override
    public void delete() {
        for (Notification notification : notificationList) {
            notification.delete();
        }
        if (isStored()) {
            getModel().getStorageSystem().deleteMechanism(id);
        }
    }

    @Override
    public boolean validate() {
        boolean valid = true;
        for (Notification notification : notificationList) {
            valid = valid && notification.validate();
        }
        return isStored() && valid;
    }

    @Override
    public ArrayList<String> getOpaqueReference() {
        ArrayList<String> ownerReference = getOwner().getOpaqueReference();
        ownerReference.add(mechanismUID);
        return ownerReference;
    }

    @Override
    public boolean consumeOpaqueReference(ArrayList<String> reference) {
        if (reference != null && reference.size() > 0 && mechanismUID.equals(reference.get(0))) {
            reference.remove(0);
            return true;
        }
        return false;
    }

    private void populateNotifications(List<Notification.NotificationBuilder> notificationBuilders) {
        for (Notification.NotificationBuilder notificationBuilder : notificationBuilders) {
            Notification notification = null;
            try {
                notification = notificationBuilder.build(this);
            } catch (InvalidNotificationException e) {
                logger.error("Tried to load incorrectly assigned Notification from storage. This should never happen.");
            }

            if (notification.isStored()) {
                notificationList.add(notification);
            } else {
                logger.error("Tried to populate notification list with Notification that has not been stored.");
            }
        }
    }

    @Override
    public final boolean matches(Mechanism other) {
        if (other == null) {
            return false;
        }
        return owner.matches(other.getOwner())
                && getInfo().getMechanismString().equals(other.getInfo().getMechanismString());
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Mechanism)) {
            return false;
        }
        Mechanism otherMechanism = (Mechanism) other;

        boolean dataMatches = true;

        Map<String, String> data = asMap();
        Map<String, String> otherData = otherMechanism.asMap();

        for (String key : data.keySet()) {
            if (data.get(key) != null || otherData.get(key) != null) {
                dataMatches &= data.get(key) != null && data.get(key).equals(otherData.get(key));
            }
        }

        return owner.matches(otherMechanism.getOwner())
                && getInfo().getMechanismString().equals(otherMechanism.getInfo().getMechanismString())
                && mechanismUID.equals(otherMechanism.mechanismUID)
                && dataMatches;
    }

    @Override
    public int hashCode() {
        List<Object> values = new ArrayList<>();
        values.add(owner);
        values.add(mechanismUID);
        values.add(getInfo().getMechanismString());

        Map<String, String> data = asMap();

        for (String key: data.keySet()) {
            values.add(data.get(key));
        }

        return Arrays.hashCode(values.toArray());
    }

    @Override
    public int compareTo(Mechanism another) {
        return getInfo().getMechanismString().compareTo(another.getInfo().getMechanismString());
    }

    /**
     * Base builder class which combines the common aspects of all Mechanisms. Classes that extend
     * Mechanism should also extend this class.
     * @param <T> The extended builder, used for chaining.
     */
    public static abstract class PartialMechanismBuilder<T extends PartialMechanismBuilder> {
        protected String mechanismUID;
        protected Identity owner;
        protected long id = NOT_STORED;
        protected TimeKeeper timeKeeper = new TimeKeeper();
        private List<Notification.NotificationBuilder> notificationBuilders = new ArrayList<>();

        /**
         * The instance of the extended builder, used for chaining.
         * @return The current builder.
         */
        protected abstract T getThis();

        /**
         * Sets the UID for this mechanism.
         * @param uid The UID used by external systems to identify this mechanism.
         * @return This builder.
         */
        public T setMechanismUID(String uid) {
            mechanismUID = uid;
            return getThis();
        }

        /**
         * Sets the owner of this mechanism. This must be called.
         * @param owner The identity which this mechanism belongs to.
         * @return This builder.
         */
        public T setOwner(Identity owner) {
            this.owner = owner;
            return getThis();
        }

        /**
         * Sets the storage id of this Mechanism. Should not be set manually, or if the Identity is
         * not stored.
         * @param id The storage id.
         */
        public T setId(long id) {
            this.id = id;
            return getThis();
        }

        /**
         * Sets the notifications that are currently associated with this Mechanism.
         * @param notificationBuilders A list of incomplete notification builders.
         */
        public T setNotifications(List<Notification.NotificationBuilder> notificationBuilders) {
            this.notificationBuilders = notificationBuilders;
            return getThis();
        }

        /**
         * Used for Time Travel during testing.
         * @param timeKeeper The TimeKeeper implementation this Mechanism should use.
         * @return This builder.
         */
        @VisibleForTesting
        public T setTimeKeeper(TimeKeeper timeKeeper) {
            this.timeKeeper = timeKeeper;
            return getThis();
        }

        /**
         * Produces the Mechanism object that was being constructed.
         * @return The mechanism.
         */
        public final Mechanism build(Identity owner) throws MechanismCreationException {
            if (mechanismUID == null) {
                throw new MechanismCreationException("The mechanism UID must be set to a valid value");
            }
            Mechanism newMechanism = buildImpl(owner);
            newMechanism.populateNotifications(notificationBuilders);
            return newMechanism;
        }

        /**
         * Produces the Mechanism object that was being constructed, but does not result in the
         * notifications being set properly, due to bi-directional dependence.
         * @return The mechanism.
         */
        protected abstract Mechanism buildImpl(Identity owner) throws MechanismCreationException;
    }
}
