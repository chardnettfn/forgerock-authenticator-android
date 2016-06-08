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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class for helping provide access to the database. Used internally by IdentityDatabase, and should
 * not be used elsewhere. Handles creation and upgrade of the SQLite database as required. Also
 * enables foreign key validation to maintain database consistency.
 */
class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FR_AUTH";

    /**
     * Creates the help for access to the database.
     * @param context The context the database is to be opened from.
     */
    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + IdentityDatabase.IDENTITY_TABLE_NAME + " (" +
                IdentityDatabase.ISSUER + " TEXT, " +
                IdentityDatabase.ACCOUNT_NAME + " TEXT, " +
                IdentityDatabase.IMAGE_URL + " TEXT, " +
                IdentityDatabase.BG_COLOR + " TEXT, " +
                "PRIMARY KEY(" + IdentityDatabase.ISSUER + ", " + IdentityDatabase.ACCOUNT_NAME + "));");

        db.execSQL("CREATE TABLE " + IdentityDatabase.MECHANISM_TABLE_NAME + " (" +
                IdentityDatabase.ID_ISSUER + " TEXT, " +
                IdentityDatabase.ID_ACCOUNT_NAME + " TEXT, " +
                IdentityDatabase.MECHANISM_UID + " TEXT UNIQUE, " +
                IdentityDatabase.TYPE + " TEXT, " +
                IdentityDatabase.VERSION + " INTEGER, " +
                IdentityDatabase.OPTIONS + " TEXT, " +
                "PRIMARY KEY(" + IdentityDatabase.ID_ISSUER + ", " + IdentityDatabase.ID_ACCOUNT_NAME + ", " + IdentityDatabase.TYPE + "), " +
                "FOREIGN KEY(" + IdentityDatabase.ID_ISSUER + ", " + IdentityDatabase.ID_ACCOUNT_NAME + ") " +
                "REFERENCES " + IdentityDatabase.IDENTITY_TABLE_NAME
                + "(" + IdentityDatabase.ISSUER + ", " + IdentityDatabase.ACCOUNT_NAME + "));");

        db.execSQL("CREATE TABLE " + IdentityDatabase.NOTIFICATION_TABLE_NAME + " (" +
                IdentityDatabase.MECHANISM_UID + " TEXT, " +
                IdentityDatabase.TIME_RECEIVED + " TEXT, " +
                IdentityDatabase.TIME_EXPIRED + " TEXT, " +
                IdentityDatabase.DATA + " TEXT, " +
                IdentityDatabase.PENDING + " INT, " +
                IdentityDatabase.APPROVED + " INT, " +
                "PRIMARY KEY(" + IdentityDatabase.MECHANISM_UID + ", " + IdentityDatabase.TIME_RECEIVED + "), " +
                "FOREIGN KEY(" + IdentityDatabase.MECHANISM_UID + ") " +
                "REFERENCES " + IdentityDatabase.MECHANISM_TABLE_NAME
                + "(" + IdentityDatabase.MECHANISM_UID + "));");
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
