package com.forgerock.authenticator.identity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FR_AUTH";

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + IdentityDatabase.IDENTITY_TABLE_NAME + " (" +
                IdentityDatabase.ISSUER + " TEXT, " +
                IdentityDatabase.LABEL + " TEXT, " +
                IdentityDatabase.IMAGE + " TEXT, " +
                IdentityDatabase.ORDER_NUMBER + " INTEGER, " +
                "PRIMARY KEY(" + IdentityDatabase.ISSUER + ", " + IdentityDatabase.LABEL + "));");

        db.execSQL("CREATE TABLE " + IdentityDatabase.MECHANISM_TABLE_NAME + " (" +
                IdentityDatabase.ID_ISSUER + " TEXT, " +
                IdentityDatabase.ID_LABEL + " TEXT, " +
                IdentityDatabase.TYPE + " TEXT, " +
                IdentityDatabase.VERSION + " INTEGER, " +
                IdentityDatabase.OPTIONS + " TEXT, " +
                IdentityDatabase.ORDER_NUMBER + " INTEGER, " +
                "FOREIGN KEY(" + IdentityDatabase.ID_ISSUER + ", " + IdentityDatabase.ID_LABEL + ") " +
                "REFERENCES " + IdentityDatabase.IDENTITY_TABLE_NAME
                + "(" + IdentityDatabase.ISSUER + ", " + IdentityDatabase.LABEL + "));");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // SHOULD never be necessary
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}
