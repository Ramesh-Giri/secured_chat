package com.us.ramesh.securedchat;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by ramesh on 3/14/18.
 */

public class SecuredChat extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
