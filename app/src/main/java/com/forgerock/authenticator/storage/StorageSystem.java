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

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.notifications.Notification;

import java.util.List;

/**
 * Data Access Object which can store and load Identities, Mechanisms and Notifications.
 * Encapsulates a backing storage mechanism, and provides a standard set of functions for operating
 * on the data.
 */
public interface StorageSystem {

    /**
     * Loads the complete list of Identities, loaded with the mechanisms and notifications from the database.
     * @return The complete set of data.
     */
    List<Identity> getModel(IdentityModel model);

    /**
     * Add the identity to the database.
     * @param id The identity to add.
     */
    long addIdentity(Identity id);

    /**
     * Add the mechanism to the database. If the owning identity is not yet stored, store that as well.
     * @param mechanism The mechanism to store.
     */
    long addMechanism(Mechanism mechanism);

    /**
     * Add the notification to the database.
     * @param notification The notification to store.
     */
    long addNotification(Notification notification);

    /**
     * Update the mechanism in the database. Does not create it if it does not exist.
     * @param mechanismId The id of the mechanism to update.
     * @param mechanism The mechanism to update it with.
     */
    boolean updateMechanism(long mechanismId, Mechanism mechanism);

    /**
     * Update the notification in the database. Does not create it if it does not exist.
     * @param notificationId The id of the notification to update.
     * @param notification The notification to update it with.
     */
    boolean updateNotification(long notificationId, Notification notification);

    /**
     * Delete the mechanism uniquely identified by an id.
     * @param mechanismId The id of the mechanism to delete.
     */
    boolean deleteMechanism(long mechanismId);

    /**
     * Delete the identity that was passed in.
     * @param identityId The if of the identity to delete.
     */
    boolean deleteIdentity(long identityId);

    /**
     * Delete the notification uniquely identified by an id.
     * @param notificationId The id of the notification to delete.
     */
    boolean deleteNotification(long notificationId);

    /**
     * Whether the storage system currently contains any data.
     * @return True if the storage system is empty, false otherwise.
     */
    boolean isEmpty();
}
