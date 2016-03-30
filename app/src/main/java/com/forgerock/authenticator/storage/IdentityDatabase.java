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
    static final String ACCOUNT_NAME = "accountName";
    /** The IDP image column */
    static final String IMAGE = "image";

    // Mechanism columns
    /** The IDP name column (Foreign key) */
    static final String ID_ISSUER = "idIssuer";
    /** The identity name column (Foreign key) */
    static final String ID_ACCOUNT_NAME = "idAccountName";
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
        Cursor cursor = database.rawQuery("SELECT rowid, * FROM " + IDENTITY_TABLE_NAME + " ORDER BY "
                + ISSUER + " ASC, " + ACCOUNT_NAME + " ASC", null);
        List<Identity> result = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Identity newIdentity = cursorToIdentity(cursor);
            result.add(newIdentity);
            cursor.moveToNext();
        }
        return result;
    }

    /**
     * Gets the mechanism identified uniquely by the provided row ID.
     * @param mechanismId The id of the row to get.
     * @return The mechanism at the provided row.
     * @throws MechanismCreationException If the mechanism failed to be created.
     */
    public Mechanism getMechanism(long mechanismId) throws MechanismCreationException {
        Cursor cursor = database.rawQuery("SELECT rowid, * FROM " + MECHANISM_TABLE_NAME +
                " WHERE rowid = " + mechanismId, null);
        cursor.moveToFirst();
        return cursorToMechanism(cursor);
    }

    /**
     * Get the mechanisms associated with an owning identity (currently gets all mechanisms).
     * @param owner
     * @return
     */
    public List<Mechanism> getMechanisms(Identity owner) {
        String[] selectionArgs = { owner.getIssuer(), owner.getAccountName() };
        Cursor cursor = database.rawQuery("SELECT rowid, * FROM " + MECHANISM_TABLE_NAME +
                        " WHERE " + ID_ISSUER + " = ? AND " + ID_ACCOUNT_NAME + " = ? ORDER BY " +
                        TYPE + " ASC"
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

    private Identity cursorToIdentity(Cursor cursor) {
        int rowid = cursor.getInt(cursor.getColumnIndex("rowid"));
        String issuer = cursor.getString(cursor.getColumnIndex(ISSUER));
        String accountName = cursor.getString(cursor.getColumnIndex(ACCOUNT_NAME));
        String image = cursor.getString(cursor.getColumnIndex(IMAGE));
        Identity identity = Identity.builder()
                .setIssuer(issuer)
                .setAccountName(accountName)
                .setImage(image)
                .build();
        identity.setId(rowid);
        return identity;
    }

    private Mechanism cursorToMechanism(Cursor cursor) throws MechanismCreationException {
        String type = cursor.getString(cursor.getColumnIndex(TYPE));
        int version = cursor.getInt(cursor.getColumnIndex(VERSION));
        Type mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> options =
                gson.fromJson(cursor.getString(cursor.getColumnIndex(OPTIONS)), mapType);

        String issuer = cursor.getString(cursor.getColumnIndex(ID_ISSUER));
        String accountName = cursor.getString(cursor.getColumnIndex(ID_ACCOUNT_NAME));
        Identity owner = getIdentity(issuer, accountName);

        Mechanism mechanism = coreMechanismFactory.createFromParameters(type, version, owner, options);
        mechanism.setId(cursor.getLong(cursor.getColumnIndex("rowid")));
        return mechanism;
    }

    /**
     * Gets the identity uniquely identified by the issuer and accountName provided (primary key).
     * @param issuer The issuer of the identity.
     * @param accountName The accountName of the identity.
     * @return The identity that was stored, or null if the identity was not found.
     */
    public Identity getIdentity(String issuer, String accountName) {
        String[] selectionArgs = { issuer, accountName };
        Cursor cursor = database.rawQuery("SELECT rowid, * FROM " + IDENTITY_TABLE_NAME +
                " WHERE " + ISSUER + " = ? AND " + ACCOUNT_NAME + " = ?", selectionArgs);
        if (cursor.getCount() == 0) {
            return null;
        }
        cursor.moveToFirst();

        Identity identity = cursorToIdentity(cursor);

        return identity;
    }

    /**
     * Returns the Identity that is identified by the id provided.
     * @param identityId The id of the Identity.
     * @return The stored Identity.
     */
    public Identity getIdentity(long identityId) {
        Cursor cursor = database.rawQuery("SELECT rowid, * FROM " + IDENTITY_TABLE_NAME +
                " WHERE rowId = " + identityId, null);
        if (cursor.getCount() == 0) {
            return null;
        }
        cursor.moveToFirst();

        Identity identity = cursorToIdentity(cursor);

        return identity;
    }

    /**
     * Add the identity to the database.
     * @param id The identity to add.
     */
    public void addIdentity(Identity id) {
        String issuer = id.getIssuer();
        String accountName = id.getAccountName();
        String image = id.getImage() == null ? null : id.getImage().toString();

        ContentValues values = new ContentValues();
        values.put(ISSUER, issuer);
        values.put(ACCOUNT_NAME, accountName);
        values.put(IMAGE, image);

        long rowid = database.insert(IDENTITY_TABLE_NAME, null, values);
        id.setId(rowid);
        onDatabaseChange();
    }

    private boolean isIdentityStored(Identity id) {
        String[] selectionArgs = {id.getIssuer(), id.getAccountName()};

        Cursor cursor = database.rawQuery("SELECT rowid FROM " + IDENTITY_TABLE_NAME +
                " WHERE " + ISSUER + " = ? AND " + ACCOUNT_NAME + " = ?", selectionArgs);
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
        String accountName = mechanism.getOwner().getAccountName();
        String type = mechanism.getInfo().getMechanismString();
        int version = mechanism.getVersion();
        String options = gson.toJson(mechanism.asMap());

        ContentValues values = new ContentValues();
        values.put(ID_ISSUER, issuer);
        values.put(ID_ACCOUNT_NAME, accountName);
        values.put(TYPE, type);
        values.put(VERSION, version);
        values.put(OPTIONS, options);

        long rowId = database.insert(MECHANISM_TABLE_NAME, null, values);
        mechanism.setId(rowId);
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
            String[] selectionArgs = { Long.toString(mechanism.getId()) };
            database.update(MECHANISM_TABLE_NAME, values, "rowId = ?", selectionArgs);
            onDatabaseChange();
        } catch (NotStoredException e) {
            logger.error("Tried to update mechanism that hadn't been stored", e);
        }
    }

    /**
     * Delete the mechanism uniquely identified by an id.
     * @param mechanismId The id of the mechanism to delete.
     */
    public void deleteMechanism(long mechanismId) {
        Mechanism mechanism;
        try {
            mechanism = getMechanism(mechanismId);
        } catch (MechanismCreationException e) {
            return;
        }
        int mechanismCount = countMechanisms(mechanism.getOwner());

        database.beginTransaction();
        database.delete(MECHANISM_TABLE_NAME, "rowId = " + mechanismId, null);

        if(mechanismCount == 1) {
            deleteIdentitySuppressListeners(mechanism.getOwner());
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        onDatabaseChange();
    }

    private void deleteIdentitySuppressListeners(Identity identity) {
        String[] whereArgs = { identity.getIssuer(), identity.getAccountName() };
        database.delete(IDENTITY_TABLE_NAME, ISSUER + " = ? AND " + ACCOUNT_NAME + " = ?", whereArgs);
    }

    private int countMechanisms(Identity owner) {
        String[] selectionArgs = { owner.getIssuer(), owner.getAccountName() };
        Cursor cursor = database.rawQuery("SELECT rowid, * FROM " + MECHANISM_TABLE_NAME +
                        " WHERE " + ID_ISSUER + " = ? AND " + ID_ACCOUNT_NAME + " = ?"
                , selectionArgs);
        return cursor.getCount();
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
