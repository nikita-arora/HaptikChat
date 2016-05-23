package com.nikitaarora.haptikchat;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Nikita on 23-05-2016.
 */
public class Utils {
    public static final String PREF_FILE = "PrefsFile";
    private static SharedPreferences sharedPreferences;

    public static SharedPreferences getSharedPreferences(Context context) {
        if (sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREF_FILE, 0);
        return sharedPreferences;
    }

    public static String getStringPreferences(Context context, String key) {
        sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences != null)
            return sharedPreferences.getString(key, "");
        else
            return "";
    }

    public static void saveSharedPref(Context context, String key, String string) {
        sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences == null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, string);
            editor.commit();
        }
    }
}
