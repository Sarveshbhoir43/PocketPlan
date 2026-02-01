package utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveUser(int userId) {
        editor.putInt("USER_ID", userId);
        editor.apply();
    }

    public int getUserId() {
        return pref.getInt("USER_ID", -1);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
