package com.matthias.android.amginori.persistence;

import android.content.Context;
import android.content.SharedPreferences;

public final class SharedPreferencesHelper {

    private static final String PREFS_NAME = "AmgiNoriPrefs";

    private static SharedPreferencesHelper sSharedPreferencesHelper;

    private Context mContext;

    private SharedPreferencesHelper(Context context) {
        mContext = context.getApplicationContext();
    }

    public static SharedPreferencesHelper get(Context context) {
        if (sSharedPreferencesHelper == null) {
            sSharedPreferencesHelper = new SharedPreferencesHelper(context);
        }
        return sSharedPreferencesHelper;
    }

    public int getInt(String key, int defValue) {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt(key, defValue);
    }

    public void putInt(String key, int value) {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void remove(String key) {
        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.apply();
    }
}
