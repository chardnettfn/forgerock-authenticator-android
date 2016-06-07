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
import android.content.SharedPreferences;
import android.support.annotation.VisibleForTesting;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.CoreMechanismFactory;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.model.ModelObject;
import com.forgerock.authenticator.notifications.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Used by the app to update the storage system to the latest version.
 * To use externally, simply create a new instance and call getModel().
 * Makes a best attempt to upgrade the data. If any entries fail for any reason, the original data
 * is left intact, and the last value is not updated.
 */
public class ModelOpenHelper {
    private final int SHARED_PREFERENCES = 1;
    private final int DATABASE_V1 = 2;

    private final int STORAGE_VERSION = DATABASE_V1;

    private final String INFO_NAME = "applicationInfo";
    private final String LAST_VERSION = "lastVersion";
    private final Context context;
    private SharedPreferences sharedPreferences;

    /**
     * Create a new ModelOpenHelper.
     * @param context The context this is being created from.
     */
    public ModelOpenHelper(Context context) {
        this.context = context;
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(INFO_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Return a model backed by an up to date storage system.
     * @return The Identity model.
     */
    public IdentityModel getModel() {
        processVersion();
        return getModelFromLatest(context);
    }

    private void processVersion() {
        int lastVersion;
        if (!sharedPreferences.contains(LAST_VERSION)) {
            // Determine whether this has not been set due to this being the first run, or an old version.
            if (context.getApplicationContext()
                    .getSharedPreferences("tokens", Context.MODE_PRIVATE)
                    .contains("tokenOrder")) {
                lastVersion = 1;
            } else {
                lastVersion = STORAGE_VERSION;
            }
        } else {
            lastVersion = sharedPreferences.getInt(LAST_VERSION, STORAGE_VERSION);
        }
        handleUpgrade(lastVersion);
    }

    private void handleUpgrade(int lastVersion) {
        if (lastVersion == STORAGE_VERSION) {
            return;
        }

        IdentityModel oldModel = getModelFromSystem(context, lastVersion);
        List<ModelObject> oldData = extractNonIdentityData(oldModel);
        StorageSystem oldStorage = oldModel.getStorageSystem();

        IdentityModel latestModel = getModelFromLatest(context);
        List<ModelObject> originalData = extractNonIdentityData(getModelFromLatest(context));

        removeOutdatedDataFromOldStorage(oldData, originalData);
        oldModel.transferStorage(latestModel.getStorageSystem());
        removeUnexpectedDataFromNewStorage(oldData, originalData);
        clearSuccessfullyTransferredValuesFromOldStorage(lastVersion);

        if (oldStorage.isEmpty()) {
            sharedPreferences.edit().putInt(LAST_VERSION, STORAGE_VERSION).apply();
        }
    }

    /**
     * Establish which values already exist in the new storage, and throw those away in the old storage
     * (prevents overwrite)
     */
    private void removeOutdatedDataFromOldStorage(List<ModelObject> oldData, List<ModelObject> originalData) {
        List<ModelObject> matching = intersectMatching(oldData, originalData);
        for (ModelObject current : matching) {
            current.delete();
        }
    }

    /**
     * Establish what data is expected to be present after the transfer
     * Compare expected data with actual data, delete any unexpected from new storage
     * (prevents incorrect transfer)
     */
    private void removeUnexpectedDataFromNewStorage(List<ModelObject> oldData, List<ModelObject> originalData) {
        List<ModelObject> expectedData = new ArrayList<>();
        expectedData.addAll(oldData);
        expectedData.removeAll(intersectMatching(oldData, originalData));
        expectedData.addAll(originalData);

        List<ModelObject> postAddData = extractNonIdentityData(getModelFromLatest(context));

        List<ModelObject> unexpectedData = new ArrayList<>();
        unexpectedData.addAll(postAddData);
        unexpectedData.removeAll(expectedData);
        for (ModelObject current : unexpectedData) {
            current.delete();
        }
    }

    private List<ModelObject> extractNonIdentityData(IdentityModel model) {
        List<ModelObject> result = new ArrayList<>();
        result.addAll(model.getMechanisms());
        result.addAll(model.getNotifications());
        return result;
    }

    private void clearSuccessfullyTransferredValuesFromOldStorage(int lastVersion) {

        List<Identity> oldModel = getModelFromSystem(context, lastVersion).getIdentities();
        List<Identity> newModel = getModelFromLatest(context).getIdentities();

        for (Identity oldIdentity : new ArrayList<>(oldModel)) {
            if (containsMatching(newModel, oldIdentity)) {
                Identity newIdentity = getMatching(newModel, oldIdentity);
                for (Mechanism oldMechanism : new ArrayList<>(oldIdentity.getMechanisms())) {
                    if (newIdentity.getMechanisms().contains(oldMechanism)) {
                        for (Notification oldNotification : new ArrayList<>(oldMechanism.getNotifications())) {
                            oldMechanism.removeNotification(oldNotification);
                        }
                        oldIdentity.removeMechanism(oldMechanism);
                    }
                }
            }
            // Note that the Identity is not explicitly deleted, as it will be implicitly deleted if it has 0 mechanisms.
        }
    }

    private <T extends ModelObject<T>> List<T> intersectMatching(List<T> groupA, List<T> groupB) {
        List<T> result = new ArrayList<>(groupA);
        for (T oldObject : groupA) {
            if (!containsMatching(groupB, oldObject)) {
                result.remove(oldObject);
            }
        }
        return result;
    }

    private <T extends ModelObject<T>> T getMatching(List<T> list, T modelObject) {
        for (T current : list) {
            if (modelObject.getClass().equals(current.getClass()) && modelObject.matches(current)) {
                return current;
            }
        }
        return null;
    }

    private <T extends ModelObject<T>> boolean containsMatching(List<T> list, T modelObject) {
        for (T current : list) {
            if (modelObject.getClass().equals(current.getClass()) && modelObject.matches(current)) {
                return true;
            }
        }
        return false;
    }

    private StorageSystem getStorageSystem(int version, CoreMechanismFactory mechanismFactory) {
        switch (version) {
            case STORAGE_VERSION:
                return new IdentityDatabase(context, mechanismFactory);
            case SHARED_PREFERENCES:
                return new SharedPreferencesStorage(context, mechanismFactory);
        }
        return null;
    }

    private IdentityModel getModelFromSystem(Context context, int systemVersion) { //TODO: replace this with getStorageSystem where possible?
        IdentityModel model = new IdentityModel(context);
        CoreMechanismFactory mechanismFactory = new CoreMechanismFactory(context, model);
        model.loadFromStorageSystem(getStorageSystem(systemVersion, mechanismFactory));
        return model;
    }

    /**
     * Used for deliberately introducing errors for testing.
     */
    @VisibleForTesting
    protected IdentityModel getModelFromLatest(Context context) {
        return getModelFromSystem(context, STORAGE_VERSION);
    }
}
