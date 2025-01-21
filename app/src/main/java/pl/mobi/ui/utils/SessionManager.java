package pl.mobi.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String USER_ROLE = "userRole";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(IS_LOGGED_IN, false);
    }

    public void setUserRole(String role) {
        editor.putString(USER_ROLE, role);
        editor.apply();
    }

    public String getUserRole() {
        return prefs.getString(USER_ROLE, null);
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
