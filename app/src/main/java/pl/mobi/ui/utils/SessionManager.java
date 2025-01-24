package pl.mobi.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.logging.Logger;

public class SessionManager {
    private final Logger log = Logger.getLogger(SessionManager.class.getName());
    private static final String PREF_NAME = "MyAppPrefs";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String USER_ROLE = "userRole";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        log.info("Session manager initialized. Getting context...");
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        log.info("Context: " + prefs.toString());
        editor = prefs.edit();
    }

    public void createSession(String role) {
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(USER_ROLE, role);
        editor.commit();
    }

    public boolean isLoggedIn() {
        log.info("Checking login status");
        return prefs.getBoolean(IS_LOGGED_IN, false);
    }

    public String getUserRole() {
        return prefs.getString(USER_ROLE, "");
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
