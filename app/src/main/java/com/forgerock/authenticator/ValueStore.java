package com.forgerock.authenticator;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for abstracting SharedPreferences (which can store strings) away from code which saves
 * objects. Gson is used for converting objects to strings and back.
 */
public class ValueStore {

    private final SharedPreferences prefs;
    private SharedPreferences.Editor currentOperation;
    private final Gson gson;

    // Don't like this. new ValueStore() doesn't make sense. It's still the same value store!
    public ValueStore(Context ctx, String group) {
        prefs = ctx.getApplicationContext().getSharedPreferences(group, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public ValueStore put(String key, Object value) {
        startOperation();
        currentOperation.putString(key, gson.toJson(value));
        return this;
    }

    public String getString(String key) {
        return prefs.getString(key, null);
    }

    public <T> T getAsClass(String key, Class<T> clazz) throws JsonSyntaxException{
        String str = prefs.getString(key, null);
        return gson.fromJson(str, clazz);
    }

    public List<String> getList(String key) {
        Type type = new TypeToken<List<String>>(){}.getType();
        String str = prefs.getString(key, "[]");
        List<String> order = gson.fromJson(str, type);
        return order == null ? new LinkedList<String>() : order;
    }

    private void startOperation() {
        if (currentOperation == null) {
            currentOperation = prefs.edit();
        }
    }

    public void apply() {
        currentOperation.apply();
        currentOperation = null;
    }

    public boolean contains(String key) {
        return prefs.contains(key);
    }

    public ValueStore remove(String key) {
        startOperation();
        currentOperation.remove(key);
        return this;
    }
}