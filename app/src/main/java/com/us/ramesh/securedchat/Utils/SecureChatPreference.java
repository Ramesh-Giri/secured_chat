package com.us.ramesh.securedchat.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created on 25/12/2017.
 *
 * @author Ramesh
 */

public class SecureChatPreference {
    private static final String PREFS_NAME = "SecureChatPreference";

    private static final String IS_LOGGED_IN_ANY_ACCOUNT = "accountLogFlag";
    private static final String ACCOUNT_ID = "accountId";
    private static final String ACCOUNT_NAME = "accountName";
    private static final String ACCOUNT_EMAIL = "accountEmail";
    private static final String ACCOUNT_CREATED_DATE = "accountCreatedDate";
    private static final String ACCOUNT_PROFILE_IMAGE = "accountProfileImage";


    private SharedPreferences mSharedPreference;
    private SharedPreferences.Editor mEditor;
    private Context mContext;

    private int sum = 0;

    public SecureChatPreference(Context mContext) {
        this.mContext = mContext;
        mSharedPreference = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getAccountCreatedDate() {
        return mSharedPreference.getString(ACCOUNT_CREATED_DATE, null);
    }

    public void setAccountCreatedDate(String accountId) {
        mEditor = mSharedPreference.edit();
        mEditor.putString(ACCOUNT_CREATED_DATE, accountId);
        mEditor.apply();
    }

    public String getAccountProfileImage() {
        return mSharedPreference.getString(ACCOUNT_PROFILE_IMAGE, null);
    }

    public void setAccountProfileImage(String accountId) {
        mEditor = mSharedPreference.edit();
        mEditor.putString(ACCOUNT_PROFILE_IMAGE, accountId);
        mEditor.apply();
    }


    public String getAccountEmail() {
        return mSharedPreference.getString(ACCOUNT_EMAIL, null);
    }

    public void setAccountEmail(String accountId) {
        mEditor = mSharedPreference.edit();
        mEditor.putString(ACCOUNT_EMAIL, accountId);
        mEditor.apply();
    }



    public void setHasLoggedIn(boolean hasLoggedIn) {
        mEditor = mSharedPreference.edit();
        mEditor.putBoolean(IS_LOGGED_IN_ANY_ACCOUNT, hasLoggedIn);
        mEditor.apply();
    }

    public boolean hasLoggedIn() {
        return mSharedPreference.getBoolean(IS_LOGGED_IN_ANY_ACCOUNT, false);
    }

    public String getAccountName() {
        return mSharedPreference.getString(ACCOUNT_NAME, null);
    }

    public void setAccountName(String accountName) {
        mEditor = mSharedPreference.edit();
        mEditor.putString(ACCOUNT_NAME, accountName);
        mEditor.apply();
    }

    public String getAccountId() {
        return mSharedPreference.getString(ACCOUNT_ID, null);
    }

    public void setAccountId(String accountId) {
        mEditor = mSharedPreference.edit();
        mEditor.putString(ACCOUNT_ID, accountId);
        mEditor.apply();
    }
}
