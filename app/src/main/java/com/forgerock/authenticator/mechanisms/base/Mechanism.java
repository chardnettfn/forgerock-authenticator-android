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

import android.content.Context;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.model.ModelObject;
import com.forgerock.authenticator.notifications.Notification;
import com.forgerock.authenticator.storage.IdentityDatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import roboguice.RoboGuice;

/**
 * A mechanism used for authentication.
 * Encapsulates the related settings, as well as an owning Identity and may have a unique UID set.
 */
public abstract class Mechanism extends ModelObject {
    private long id = NOT_STORED;
    private final int mechanismUID;
    private final Identity owner;
    private final List<Notification> notificationList;

    private static final Logger logger = LoggerFactory.getLogger(Mechanism.class);

    /**
     * Base constructor which encapsulates common elements of all Mechanisms.
     * @param owner The owner of the Mechanism.
     * @param id The storage id of the Mechanism.
     * @param mechanismUID The ID used to identify the Mechanism to external systems.
     */
    protected Mechanism(Identity owner, long id, int mechanismUID) {
        notificationList = new ArrayList<>();
        this.owner = owner;
        this.mechanismUID = mechanismUID;
        this.id = id;
    }

    /**
     * Adds the provided notification to this Mechanism, and therefore to the larger data model.
     * @param context The context that the notification is being added from.
     * @param notificationBuilder An incomplete builder for a non stored notification.
     * @return The notification that has been added to the data model.
     */
    public Notification addNotification(Context context, Notification.NotificationBuilder notificationBuilder) {
        Notification notification = notificationBuilder.setMechanism(this).build();
        if (!notificationList.contains(notification)) {
            notification.save(context);
            notificationList.add(notification);
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
    public int getMechanismUID() {
        return mechanismUID;
    }

    @Override
    public boolean isStored() {
        return id != NOT_STORED;
    }

    @Override
    public void save(Context context) {
        if (!isStored()) {
            id = RoboGuice.getInjector(context).getInstance(IdentityDatabase.class).addMechanism(this);
        } else {
            RoboGuice.getInjector(context).getInstance(IdentityDatabase.class).updateMechanism(id, this);
        }
    }

    @Override
    public void delete(Context context) {
        if (isStored()) {
            RoboGuice.getInjector(context).getInstance(IdentityDatabase.class).deleteMechanism(id);
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
        //TODO: Maybe take a hash of the fixed values being stored? Maybe UID could be generated for all mechanisms?
        ArrayList<String> ownerReference = getOwner().getOpaqueReference();
        ownerReference.add(Long.toString(id));
        return ownerReference;
    }

    @Override
    public boolean consumeOpaqueReference(ArrayList<String> reference) {
        if (reference.size() > 0 && Long.toString(id).equals(reference.get(0))) {
            reference.remove(0);
            return true;
        }
        return false;
    }

    private void populateNotifications(List<Notification.NotificationBuilder> notificationBuilders) {
        for (Notification.NotificationBuilder notificationBuilder : notificationBuilders) {
            Notification notification = notificationBuilder.setMechanism(this).build();
            if (notification.isStored()) {
                notificationList.add(notificationBuilder.setMechanism(this).build());
            } else {
                logger.error("Tried to populate notification list with Notification that has not been stored.");
            }
        }
    }

    /**
     * Base builder class which combines the common aspects of all Mechanisms. Classes that extend
     * Mechanism should also extend this class.
     * @param <T> The extended builder, used for chaining.
     */
    public static abstract class PartialMechanismBuilder<T extends PartialMechanismBuilder> {
        protected int mechanismUID;
        protected Identity owner;
        protected long id = NOT_STORED;
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
        public T setMechanismUID(int uid) {
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
         * Produces the Mechanism object that was being constructed.
         * @return The mechanism.
         */
        public final Mechanism build() throws MechanismCreationException {
            Mechanism newMechanism = buildImpl();
            newMechanism.populateNotifications(notificationBuilders);
            return newMechanism;
        }

        /**
         * Produces the Mechanism object that was being constructed, but does not result in the
         * notifications being set properly, due to bi-directional dependence.
         * @return The mechanism.
         */
        protected abstract Mechanism buildImpl() throws MechanismCreationException;
    }
}
