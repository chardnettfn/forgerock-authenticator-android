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

package com.forgerock.authenticator.storage;

import android.support.annotation.VisibleForTesting;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.notifications.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which represents the data model, and handles deciding when the database should be updated.
 * Loads the full data from the database on initialisation.
 */
public class IdentityModel {
    private List<Identity> identities;
    private List<IdentityModelListener> listeners;
    private StorageSystem storageSystem;

    /**
     * Load the model from the database.
     */
    public IdentityModel() {
        listeners = new ArrayList<>();
    }

    /**
     * Set the initial storage system to load data from. Can only be used once for a given Model.
     * @param storageSystem The storage system to use.
     */
    public void loadFromStorageSystem(StorageSystem storageSystem) {
        if (this.storageSystem == null) {
            this.storageSystem = storageSystem;
            identities = this.storageSystem.getModel(this);
            validateModel(identities);
        }
    }

    @VisibleForTesting
    public IdentityModel(IdentityDatabase database) {
        storageSystem = database;
        listeners = new ArrayList<>();
        identities = storageSystem.getModel(this);
        validateModel(identities);
    }

    /**
     * Returns the storage system currently backing this model.
     * @return The storage system being used.
     */
    public StorageSystem getStorageSystem() {
        return storageSystem;
    }

    /**
     * Check that all loaded entries contain ids. Can be extended to provide additional verification.
     */
    private void validateModel(List<Identity> identities) {
        boolean valid = true;
        for (Identity identity : identities) {
            valid = valid && identity.validate();
        }
        if (!valid) {
            throw new RuntimeException("Model Object failed to validate. See logs for more information.");
        }
    }

    /**
     * Get an identity based on the opaque reference provided.
     * @param opaqueReference The opaque reference of the identity to get.
     * @return The identity that matches the opaque reference.
     */
    public Identity getIdentity(ArrayList<String> opaqueReference) {
        for (Identity identity : identities) {
            if (identity.consumeOpaqueReference(opaqueReference)) {
                return identity;
            }
        }
        return null;
    }

    /**
     * Get an mechanism based on the opaque reference provided.
     * @param opaqueReference The opaque reference of the mechanism to get.
     * @return The mechanism that matches the opaque reference.
     */
    public Mechanism getMechanism(ArrayList<String> opaqueReference) {
        Identity identity = getIdentity(opaqueReference);
        if (identity == null) {
            return null;
        }
        for (Mechanism mechanism : identity.getMechanisms()) {
            if (mechanism.consumeOpaqueReference(opaqueReference)) {
                return mechanism;
            }
        }
        return null;
    }

    /**
     * Get an notification based on the opaque reference provided.
     * @param opaqueReference The opaque reference of the notification to get.
     * @return The notification that matches the opaque reference.
     */
    public Notification getNotification(ArrayList<String> opaqueReference) {
        Mechanism mechanism = getMechanism(opaqueReference);
        if (mechanism == null) {
            return null;
        }
        for (Notification notification : mechanism.getNotifications()) {
            if (notification.consumeOpaqueReference(opaqueReference)) {
                return notification;
            }
        }
        return null;
    }

    /**
     * Get an identity based on the issuer and account name provided.
     * @param issuer The issuer of the identity to retrieve.
     * @param accountName The account name of the identity to retrieve.
     * @return The identity that matches the values provided.
     */
    public Identity getIdentity(String issuer, String accountName) {
        for (Identity identity : identities) {
            if (identity.getIssuer().equals(issuer) && identity.getAccountName().equals(accountName)) {
                return identity;
            }
        }
        return null;
    }

    /**
     * Get all identities stored in the model.
     * @return The complete list of identities.
     */
    public List<Identity> getIdentities() {
        return identities;
    }

    /**
     * Get all mechanisms stored in the model.
     * @return The complete list of mechanisms.
     */
    public List<Mechanism> getMechanisms() {
        List<Mechanism> result = new ArrayList<>();
        for (Identity identity : identities) {
            result.addAll(identity.getMechanisms());
        }
        return result;
    }

    /**
     * Generate a new, unique ID for a Mechanism.
     * @return The new mechanism UID.
     */
    public String getNewMechanismUID() {
        int uid = 0; //TODO: Make UID larger, and random
        while (isExistingMechanismUID(Integer.toString(uid))) {
            uid++;
        }
        return Integer.toString(uid);
    }

    private boolean isExistingMechanismUID(String uid) {
        for (Mechanism mechanism : getMechanisms()) {
            if (mechanism.getMechanismUID().equals(uid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all notifications stored in the model.
     * @return The complete list of notifications.
     */
    public List<Notification> getNotifications() {
        List<Notification> result = new ArrayList<>();
        for (Mechanism mechanism : getMechanisms()) {
            result.addAll(mechanism.getNotifications());
        }
        return result;
    }

    /**
     * Add an identity to the model, and save them to the database.
     * @param newIdentity The identity to add.
     */
    public Identity addIdentity(Identity.IdentityBuilder newIdentity) {
        Identity identity = newIdentity.build(this);
        if (!identities.contains(identity)) {
            identities.add(identity);
        }
        identity.save();
        return identity;
    }

    /**
     * Delete an identity from the model, and delete them from the database.
     * @param identity The identity to delete.
     */
    public void removeIdentity(Identity identity) {
        identity.delete();
        identities.remove(identity);
    }

    /**
     * Transfer the current data to a new storage system.
     * @param newStorage The storage system to transfer the data to.
     */
    public void transferStorage(StorageSystem newStorage) {
        storageSystem = newStorage;
        for (Identity identity : identities) {
            identity.forceSave();
        }

        for (Mechanism mechanism : getMechanisms()) {
            mechanism.forceSave();
        }

        for (Notification notification : getNotifications()) {
            notification.forceSave();
        }
    }

    /**
     * Add a listener to this model.
     * @param listener The listener to add.
     */
    public void addListener(IdentityModelListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from the model.
     * @param listener The listener to remove.
     */
    public void removeListener(IdentityModelListener listener) {
        listeners.remove(listener);
    }

    /**
     * Used to notify all listeners that a notification has been added or removed.
     */
    public void notifyNotificationChanged() {
        for (IdentityModelListener listener : listeners) {
            listener.notificationChanged();
        }
    }
}
