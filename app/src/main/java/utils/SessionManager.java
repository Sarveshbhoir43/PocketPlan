package utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Single source of truth for the logged-in user.
 * Stores userId in SharedPreferences under the key "user_session".
 * All activities read userId from here to scope their database queries.
 */
public class SessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID   = "USER_ID";
    private static final String KEY_USER_NAME  = "USER_NAME";
    private static final String KEY_USER_EMAIL = "USER_EMAIL";
    private static final int    NO_USER = -1;

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref   = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /** Call after successful login or registration. */
    public void saveSession(int userId, String name, String email) {
        editor.putInt(KEY_USER_ID,    userId);
        editor.putString(KEY_USER_NAME,  name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    /** Returns the logged-in user's database ID, or -1 if not logged in. */
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, NO_USER);
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "User");
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    /** True when a user is actively logged in. */
    public boolean isLoggedIn() {
        return getUserId() != NO_USER;
    }

    /** Clear session on logout. Does NOT delete the user's data from the DB. */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}