package com.example.savefoodapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "SaveFoodSession";

    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {

        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    public void createSession(
            int userId,
            String name,
            String email,
            String role
    ) {

        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);

        editor.apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    public String getUserRole() {
        return preferences.getString(KEY_USER_ROLE, "");
    }

    public void logout() {

        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();
        editor.apply();
    }
}