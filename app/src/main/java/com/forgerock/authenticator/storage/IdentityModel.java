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

import android.content.Context;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.model.ModelObject;
import com.forgerock.authenticator.notifications.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class which represents the data model, and handles deciding when the database should be updated.
 * Loads the full data from the database on initialisation.
 */
public class IdentityModel {
    private List<Identity> identities;
    private List<IdentityModelListener> listeners;

    /**
     * Load the model from the database.
     * @param context The context that the model is being loaded from.
     */
    public IdentityModel(Context context) {
        IdentityDatabase database = new IdentityDatabase(context);
        listeners = new ArrayList<>();
        identities = database.getModel();
        validateModel(identities);
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
    public int getNewMechanismUID() {
        int uid = 0; //TODO: Make UID larger, and random
        while (isExistingMechanismUID(uid)) {
            uid++;
        }
        return uid;
    }

    private boolean isExistingMechanismUID(int uid) {
        for (Mechanism mechanism : getMechanisms()) {
            if (mechanism.getMechanismUID() == uid) {
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
     * @param context The context the identity is being added from.
     * @param newIdentity The identity to add.
     */
    public void addIdentity(Context context, Identity newIdentity) {
        if (!identities.contains(newIdentity)) {
            identities.add(newIdentity);
        }
        newIdentity.save(context);
    }

    /**
     * Delete an identity from the model, and delete them from the database.
     * @param context The context the identity is being removed from.
     * @param identity The identity to delete.
     */
    public void removeIdentity(Context context, Identity identity) {
        identity.delete(context);
        identities.remove(identity);
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
