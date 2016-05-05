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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.identity.Identity;
import com.forgerock.authenticator.mechanisms.CoreMechanismFactory;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.oath.Oath;
import com.forgerock.authenticator.notifications.Notification;
import com.forgerock.authenticator.notifications.PushNotification;
import com.google.android.apps.authenticator.Base32String;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object which can store and load both Identities and Mechanisms. Encapsulates a storage
 * system based on the SharedPreferences. Only able to load and delete data.
 */
public class SharedPreferencesStorage implements StorageSystem {

    // Identity columns
    /** The IDP name column */
    private static final String ISSUER = "issuer";
    /** The identity name column */
    private static final String ACCOUNT_NAME = "label";
    /** The IDP image url column */
    private static final String IMAGE_URL = "image";

    /** The mechanism type column */
    private static final String TYPE = "type";
    private static final String ALGORITHM = "algo";
    private static final String COUNTER = "counter";
    private static final String DIGITS = "digits";
    private static final String PERIOD = "period";
    private static final String SECRET = "secret";

    private final Gson gson = new Gson();
    private final CoreMechanismFactory coreMechanismFactory;

    private static final String NAME  = "tokens";
    private static final String ORDER = "tokenOrder";

    private final SharedPreferences prefs;
    private static final Logger logger = LoggerFactory.getLogger(SharedPreferencesStorage.class);

    /**
     * Creates a connection to the database using the provided Context.
     * @param context The context that requested the connection.
     */
    SharedPreferencesStorage(Context context, CoreMechanismFactory factory) {
        prefs = context.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
        coreMechanismFactory = factory;
    }

    @Override
    public List<Identity> getModel(IdentityModel model) {
        List<Identity> identities = new ArrayList<>();

        for (int i = 0; i < length(); i++) {
            try {
                identities.add(getIdentityFromPosition(model, i));
            } catch (MechanismCreationException e) {
                logger.error("Failed to load mechanism");
            }
        }

        return identities;
    }

    private Identity getIdentityFromPosition(IdentityModel model, int position) throws MechanismCreationException{
        JSONObject data = getMapFromPosition(position);

        if (data == null) {
            throw new MechanismCreationException("Mechanism entry has been deleted");
        }

        String issuer = getStringWithDefault(data, ISSUER, "");
        String accountName = getStringWithDefault(data, ACCOUNT_NAME, "");
        String imageURL = getStringWithDefault(data, IMAGE_URL, "");

        String type = getStringWithDefault(data, TYPE, "");
        String algorithm = getStringWithDefault(data, ALGORITHM, "");
        int counter = getIntWithDefault(data, COUNTER, 0);
        int digits = getIntWithDefault(data, DIGITS, 6);
        int period = getIntWithDefault(data, PERIOD, 30);
        byte[] secret = getByteArray(data, SECRET);

        Mechanism.PartialMechanismBuilder builder = Oath
                .builder()
                .setAlgorithm(algorithm)
                .setType(type)
                .setCounter(Integer.toString(counter))
                .setDigits(Integer.toString(digits))
                .setPeriod(Integer.toString(period))
                .setSecret(Base32String.encode(secret))
                .setId(position)
                .setMechanismUID(Integer.toString(position));

        return Identity.builder()
            .setIssuer(issuer)
            .setAccountName(accountName)
            .setImageURL(imageURL)
            .setMechanisms(Collections.singletonList(builder))
            .setId(position)
            .build(model);
    }

    private String getStringWithDefault(JSONObject values, String key, String defaultValue) {
        try {
            return values.getString(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    private int getIntWithDefault(JSONObject values, String key, int defaultValue) {
        try {
            return values.getInt(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    private byte[] getByteArray(JSONObject values, String key) {
        try {
            JSONArray array = values.getJSONArray(key);
            byte[] byteArray = new byte[array.length()];

            for (int i = 0; i < array.length(); i++) {
                byteArray[i] = (byte) array.getInt(i);
            }
            return byteArray;
        } catch (JSONException e) {
            return null;
        }
    }

    private List<String> getTokenOrder() {
        Type type = new TypeToken<List<String>>(){}.getType();
        String str = prefs.getString(ORDER, "[]");
        List<String> order = gson.fromJson(str, type);
        return order == null ? new LinkedList<String>() : order;
    }

    private int length() {
        return getTokenOrder().size();
    }

    private JSONObject getMapFromPosition(int position) {
        String key = getTokenOrder().get(position);
        if (key.equals("")) {
            return null;
        }
        String str = prefs.getString(key, null);
        try {
            return new JSONObject(str);
        } catch (JSONException e) {
            logger.error("Failed to restore previously saved Mechanism", e);
        }
        return null;
    }

    public boolean isEmpty() {
        return !prefs.contains(ORDER);
    }

    @Override
    public long addIdentity(Identity id) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public long addMechanism(Mechanism mechanism) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public long addNotification(Notification notification) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean updateMechanism(long mechanismId, Mechanism mechanism) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean updateNotification(long notificationId, Notification notification) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean deleteMechanism(long mechanismId) {
        deleteEntry((int) mechanismId);
        return true;
    }

    @Override
    public boolean deleteIdentity(long identityId) {
        deleteEntry((int) identityId);
        return true;
    }

    @Override
    public boolean deleteNotification(long notificationId) {
        throw new RuntimeException("Not implemented");
    }

    private void deleteEntry(int id) {
        List<String> order = getTokenOrder();
        if (id > order.size()) {
            return;
        }
        String key = order.get(id);

        if (key.equals("")) {
            return;
        }

        prefs.edit().remove(key).apply();

        order.set(id, "");

        boolean containsValue = false;
        for (String value : order) {
            containsValue |= !value.equals("");
        }
        if (!containsValue) {
            prefs.edit().remove(ORDER).apply();
        } else {
            prefs.edit().putString(ORDER, gson.toJson(order)).apply();
        }

    }
}
