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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.CoreMechanismFactory;
import com.forgerock.authenticator.mechanisms.Mechanism;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Data Access Object which can store and load both Identities and Mechanisms. Encapsulates the
 * specific storage mechanism.
 */
public class IdentityDatabase {
    /** The name of the table the identities are stored in */
    static final String IDENTITY_TABLE_NAME = "identity";
    /**The name of the table the mechanisms are stored in */
    static final String MECHANISM_TABLE_NAME = "mechanism";

    // Identity columns
    /** The IDP name column */
    static final String ISSUER = "issuer";
    /** The identity name column */
    static final String LABEL = "label";
    /** The IDP image column */
    static final String IMAGE = "image";

    // Mechanism columns
    /** The IDP name column (Foreign key) */
    static final String ID_ISSUER = "idIssuer";
    /** The identity name column (Foreign key) */
    static final String ID_LABEL = "idLabel";
    /** The mechanism type column */
    static final String TYPE = "type";
    /** The mechanism version column */
    static final String VERSION = "version";
    /** The mechanism options column */
    static final String OPTIONS = "options";

    private final Gson gson = new Gson();
    private final SQLiteDatabase database;
    private final CoreMechanismFactory coreMechanismFactory;
    private final List<DatabaseListener> listeners;
    private static final Logger logger = LoggerFactory.getLogger(IdentityDatabase.class);

    /**
     * Creates a connection to the database using the provided Context.
     * @param context The context that requested the connection.
     */
    public IdentityDatabase(Context context) {
        DatabaseOpenHelper databaseOpeHelper = new DatabaseOpenHelper(context);
        database = databaseOpeHelper.getWritableDatabase();
        coreMechanismFactory = new CoreMechanismFactory();
        listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Gets all of the identities which are stored.
     * @return The list of identities.
     */
    public List<Identity> getIdentities() {
        Cursor cursor = database.rawQuery("SELECT * FROM " + IDENTITY_TABLE_NAME, null);
        List<Identity> result = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Identity newIdentity = Identity.builder()
                    .setIssuer(cursor.getString(cursor.getColumnIndex(ISSUER)))
                    .setLabel(cursor.getString(cursor.getColumnIndex(LABEL)))
                    .setImage(cursor.getString(cursor.getColumnIndex(IMAGE)))
                    .build();
            result.add(newIdentity);
            cursor.moveToNext();
        }
        return result;
    }

    /**
     * Gets the mechanism identified uniquely by the provided row ID.
     * @param rowId The id of the row to get.
     * @return The mechanism at the provided row.
     * @throws MechanismCreationException If the mechanism failed to be created.
     */
    public Mechanism getMechanism(long rowId) throws MechanismCreationException {
        Cursor cursor = database.rawQuery("SELECT rowid, * FROM " + MECHANISM_TABLE_NAME +
                " WHERE rowid = " + rowId, null);
        cursor.moveToFirst();
        return cursorToMechanism(cursor);
    }

    /**
     * Get the mechanisms associated with an owning identity (currently gets all mechanisms).
     * @param owner
     * @return
     */
    public List<Mechanism> getMechanisms(Identity owner) {
        String[] selectionArgs = {};//{ owner.getIssuer(), owner.getLabel() };
        Cursor cursor = database.rawQuery("SELECT rowid, * FROM " + MECHANISM_TABLE_NAME
                //        " WHERE " + ID_ISSUER + " = ? AND " + ID_LABEL + " = ?" + //Todo: change when using identities
                , selectionArgs);
        List<Mechanism> result = new ArrayList<>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            try {
                result.add(cursorToMechanism(cursor));
            } catch (MechanismCreationException e) {
                logger.error("Failed to load mechanism. This may be caused by invalid data, or data " +
                        "that has not been upgraded.", e);
                // Don't add the mechanism that failed to load.
            }
            cursor.moveToNext();
        }
        return result;
    }

    private Mechanism cursorToMechanism(Cursor cursor) throws MechanismCreationException {
        String type = cursor.getString(cursor.getColumnIndex(TYPE));
        int version = cursor.getInt(cursor.getColumnIndex(VERSION));
        Type mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> options =
                gson.fromJson(cursor.getString(cursor.getColumnIndex(OPTIONS)), mapType);

        String issuer = cursor.getString(cursor.getColumnIndex(ID_ISSUER));
        String label = cursor.getString(cursor.getColumnIndex(ID_LABEL));
        Identity owner = getIdentity(issuer, label);

        Mechanism mechanism = coreMechanismFactory.createFromParameters(type, version, owner, options);
        mechanism.setRowId(cursor.getLong(cursor.getColumnIndex("rowid")));
        return mechanism;
    }

    /**
     * Gets the identity uniquely identified by the issuer and label provided (primary key).
     * @param issuer The issuer of the identity.
     * @param label The label of the identity.
     * @return The identity that was stored, or null if the identity was not found.
     */
    public Identity getIdentity(String issuer, String label) {
        String[] selectionArgs = { issuer, label };
        Cursor cursor = database.rawQuery("SELECT * FROM " + IDENTITY_TABLE_NAME +
                " WHERE " + ISSUER + " = ? AND " + LABEL + " = ?", selectionArgs);
        if (cursor.getCount() == 0) {
            return null;
        }
        cursor.moveToFirst();

        Identity identity = Identity.builder()
                .setIssuer(cursor.getString(cursor.getColumnIndex(ISSUER)))
                .setLabel(cursor.getString(cursor.getColumnIndex(LABEL)))
                .setImage(cursor.getString(cursor.getColumnIndex(IMAGE)))
                .build();

        return identity;
    }

    /**
     * Add the identity to the database.
     * @param id The identity to add.
     */
    public void addIdentity(Identity id) {
        String issuer = id.getIssuer();
        String label = id.getLabel();
        String image = id.getImage() == null ? null : id.getImage().toString();

        ContentValues values = new ContentValues();
        values.put(ISSUER, issuer);
        values.put(LABEL, label);
        values.put(IMAGE, image);

        database.insert(IDENTITY_TABLE_NAME, null, values);
        onDatabaseChange();
    }

    private boolean isIdentityStored(Identity id) {
        String[] selectionArgs = {id.getIssuer(), id.getLabel()};

        Cursor cursor = database.rawQuery("SELECT rowid FROM " + IDENTITY_TABLE_NAME +
                " WHERE " + ISSUER + " = ? AND " + LABEL + " = ?", selectionArgs);
        return cursor.getCount() == 1;
    }

    /**
     * Add the mechanism to the database. If the owning identity is not yet stored, store that as well.
     * @param mechanism The mechanism to store.
     */
    public void addMechanism(Mechanism mechanism) {
        if (!isIdentityStored(mechanism.getOwner())) {
            addIdentity(mechanism.getOwner());
        }
        String issuer = mechanism.getOwner().getIssuer();
        String label = mechanism.getOwner().getLabel();
        String type = mechanism.getInfo().getMechanismString();
        int version = mechanism.getVersion();
        String options = gson.toJson(mechanism.asMap());

        ContentValues values = new ContentValues();
        values.put(ID_ISSUER, issuer);
        values.put(ID_LABEL, label);
        values.put(TYPE, type);
        values.put(VERSION, version);
        values.put(OPTIONS, options);

        long rowId = database.insert(MECHANISM_TABLE_NAME, null, values);
        mechanism.setRowId(rowId);
        onDatabaseChange();
    }

    /**
     * Update the mechanism in the database. Does not create it if it does not exist.
     * @param mechanism The mechanism to update.
     */
    public void updateMechanism(Mechanism mechanism) {
        ContentValues values = new ContentValues();
        String options = gson.toJson(mechanism.asMap());
        values.put(OPTIONS, options);
        try {
            String[] selectionArgs = { Long.toString(mechanism.getRowId()) };
            database.update(MECHANISM_TABLE_NAME, values, "rowId = ?", selectionArgs);
            onDatabaseChange();
        } catch (NotStoredException e) {
            logger.error("Tried to update mechanism that hadn't been stored", e);
        }
    }

    /**
     * Delete the mechanism uniquely identified by a rowId.
     * @param rowId The rowId of the mechanism to delete.
     */
    public void deleteMechanism(long rowId) {
        database.delete(MECHANISM_TABLE_NAME, "rowId = " + rowId, null);
        onDatabaseChange();
    }

    /**
     * Add a listener to this connection.
     * @param listener The listener to add.
     */
    public void addListener(DatabaseListener listener) {
        listeners.add(listener);
    }

    private void onDatabaseChange() {
        for (DatabaseListener listener : listeners) {
            listener.onUpdate();
        }
    }
}
