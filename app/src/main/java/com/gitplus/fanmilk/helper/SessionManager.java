package com.gitplus.fanmilk.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class SessionManager {
    private SharedPreferences pref;
    private Editor editor;
    private Context _context;
    // Shared preferences file name
    private static final String PREF_NAME = "fanmilk";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private String MYPREFS = "MySharePreferences";

    public final String AGENT_CODE = "AGENT_CODE";
    public final String AGENT_NAME = "AGENT_NAME";


    public SessionManager(Context context) {
        this._context = context;
        int PRIVATE_MODE = 0;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    public void setLogin(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.commit();
    }

    public void saveUserDetails(String name, String code) {
        SharedPreferences mySharedPreferences = _context.getSharedPreferences(MYPREFS, Activity.MODE_PRIVATE);
        Editor edit = mySharedPreferences.edit();

        edit.putString(AGENT_NAME, name);
        edit.putString(AGENT_CODE, code);
        edit.apply();
    }

    public HashMap<String, String> readUserDetails() {
        SharedPreferences mySharedPreferences = _context.getSharedPreferences(MYPREFS, Activity.MODE_PRIVATE);
        HashMap<String, String> user = new HashMap<>();
        String name = mySharedPreferences.getString(AGENT_NAME, null);
        String code = mySharedPreferences.getString(AGENT_CODE, null);

        user.put(AGENT_NAME, name);
        user.put(AGENT_CODE, code);
        return user;
    }

    public void deleteUserDetails() {
        SharedPreferences mySharedPreferences = _context.getSharedPreferences(MYPREFS, Activity.MODE_PRIVATE);
        Editor edit = mySharedPreferences.edit().clear();
        edit.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
}
