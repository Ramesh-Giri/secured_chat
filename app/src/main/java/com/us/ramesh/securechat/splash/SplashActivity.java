package com.us.ramesh.securechat.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.us.ramesh.securechat.MainActivity;
import com.us.ramesh.securechat.R;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Timer().schedule(new TimerTask() {
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        }, 1000);


    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }

}
