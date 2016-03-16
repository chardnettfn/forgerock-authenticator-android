package com.forgerock.authenticator.identity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.forgerock.authenticator.mechanisms.Mechanism;
import com.forgerock.authenticator.mechanisms.MechanismFactory;
import com.forgerock.authenticator.utils.MechanismCreationException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private MechanismFactory mechanismFactory;


    public IdentityDatabase(Context context) {
        DatabaseOpenHelper databaseOpeHelper = new DatabaseOpenHelper(context);
        database = databaseOpeHelper.getWritableDatabase();
        mechanismFactory = new MechanismFactory();
    }

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

    public Mechanism getMechanism(long rowId) throws MechanismCreationException {
        Cursor cursor = database.rawQuery("SELECT rowid, * FROM " + MECHANISM_TABLE_NAME +
                        " WHERE rowid = " + rowId, null);
        cursor.moveToFirst();
        return cursorToMechanism(cursor);
    }

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

        Mechanism mechanism = mechanismFactory.get(type, version, owner, options);
        mechanism.setRowId(cursor.getLong(cursor.getColumnIndex("rowid")));
        return mechanism;
    }

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

    public void addIdentity(Identity id) {
        String issuer = id.getIssuer();
        String label = id.getLabel();
        String image = id.getImage() == null ? null : id.getImage().toString();

        ContentValues values = new ContentValues();
        values.put(ISSUER, issuer);
        values.put(LABEL, label);
        values.put(IMAGE, image);

        database.insert(IDENTITY_TABLE_NAME, null, values);
    }

    private boolean isIdentityStored(Identity id) {
        String[] selectionArgs = {id.getIssuer(), id.getLabel()};

        Cursor cursor = database.rawQuery("SELECT rowid FROM " + IDENTITY_TABLE_NAME +
                " WHERE " + ISSUER + " = ? AND " + LABEL + " = ?", selectionArgs);
        return cursor.getCount() == 1;
    }

    public void addMechanism(Mechanism mechanism) {
        if (!isIdentityStored(mechanism.getOwner())) {
            addIdentity(mechanism.getOwner());
        }
        String issuer = mechanism.getOwner().getIssuer();
        String label = mechanism.getOwner().getLabel();
        String type = mechanism.getFactory().getMechanismString();
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
    }

    public void updateMechanism(Mechanism mechanism) {
        ContentValues values = new ContentValues();
        String options = gson.toJson(mechanism.asMap());
        values.put(OPTIONS, options);
        String issuer = mechanism.getOwner().getIssuer();
        String label = mechanism.getOwner().getLabel();
        String[] selectionArgs = { issuer, label };


        database.update(MECHANISM_TABLE_NAME, values, ID_ISSUER + " = ? AND " + ID_LABEL + " = ?", selectionArgs);
    }

    public void deleteMechanism(long rowId) {
        database.delete(MECHANISM_TABLE_NAME, "rowId = " + rowId, null);
    }
}
