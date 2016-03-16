package com.forgerock.authenticator.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.Mechanism;
import com.forgerock.authenticator.mechanisms.CoreMechanismFactory;
import com.forgerock.authenticator.utils.MechanismCreationException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a connection to a SQLite database. Ideally, there should only be one of these existing,
 * in order to properly support listeners and avoid stray connections.
 */
public class IdentityDatabase {
    public static final String IDENTITY_TABLE_NAME = "identity";
    public static final String MECHANISM_TABLE_NAME = "mechanism";

    // Common columns
    public static final String ORDER_NUMBER = "orderNumber";

    // Identity columns
    public static final String ISSUER = "issuer";
    public static final String LABEL = "label";
    public static final String IMAGE = "image";

    // Mechanism columns
    public static final String ID_ISSUER = "idIssuer";
    public static final String ID_LABEL = "idLabel";
    public static final String TYPE = "type";
    public static final String VERSION = "version";
    public static final String OPTIONS = "options";

    private Gson gson = new Gson();
    private SQLiteDatabase database;
    private CoreMechanismFactory coreMechanismFactory;
    private List<DatabaseListener> listeners;


    public IdentityDatabase(Context context) {
        DatabaseOpenHelper databaseOpeHelper = new DatabaseOpenHelper(context);
        database = databaseOpeHelper.getWritableDatabase();
        coreMechanismFactory = new CoreMechanismFactory();
        listeners = new ArrayList<>();
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
            Identity newIdentity = new Identity();
            newIdentity.setIssuer(cursor.getString(cursor.getColumnIndex(ISSUER)));
            newIdentity.setLabel(cursor.getString(cursor.getColumnIndex(LABEL)));
            newIdentity.setImage(cursor.getString(cursor.getColumnIndex(IMAGE)));
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
                e.printStackTrace();
                // Don't add the mechanism that failed to load.
            }
            cursor.moveToNext();
        }
        return result;
    }

    private Mechanism cursorToMechanism(Cursor cursor) throws MechanismCreationException {
        String type = cursor.getString(cursor.getColumnIndex(TYPE));
        int version = cursor.getInt(cursor.getColumnIndex(VERSION));
        Type mapType = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> options =
                gson.fromJson(cursor.getString(cursor.getColumnIndex(OPTIONS)), mapType);

        String issuer = cursor.getString(cursor.getColumnIndex(ID_ISSUER));
        String label = cursor.getString(cursor.getColumnIndex(ID_LABEL));
        Identity owner = getIdentity(issuer, label);

        Mechanism mechanism = coreMechanismFactory.get(type, version, owner, options);
        mechanism.setRowId(cursor.getLong(cursor.getColumnIndex("rowid")));
        return mechanism;
    }

    /**
     * Gets the identity uniquely identified by the issuer and label provided (primary key).
     * @param issuer The issuer of the identity.
     * @param label The label of the identity.
     * @return The identity that was stored.
     */
    public Identity getIdentity(String issuer, String label) {
        String[] selectionArgs = { issuer, label };
        Cursor cursor = database.rawQuery("SELECT * FROM " + IDENTITY_TABLE_NAME +
                " WHERE " + ISSUER + " = ? AND " + LABEL + " = ?", selectionArgs);
        if (cursor.getCount() == 0) {
            return null;
        }
        cursor.moveToFirst();

        Identity identity = new Identity();
        identity.setIssuer(cursor.getString(cursor.getColumnIndex(ISSUER)));
        identity.setLabel(cursor.getString(cursor.getColumnIndex(LABEL)));
        identity.setImage(cursor.getString(cursor.getColumnIndex(IMAGE)));

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
        if (mechanism.getRowId() != -1) {
            String[] selectionArgs = {Long.toString(mechanism.getRowId())};
            database.update(MECHANISM_TABLE_NAME, values, "rowId = ?", selectionArgs);
            onDatabaseChange();
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
