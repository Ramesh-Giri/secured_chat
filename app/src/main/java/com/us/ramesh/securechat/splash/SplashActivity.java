package com.us.ramesh.securechat.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.jaredrummler.android.widget.AnimatedSvgView;
import com.us.ramesh.securechat.MainActivity;
import com.us.ramesh.securechat.R;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AnimatedSvgView svgView = (AnimatedSvgView) findViewById(R.id.animated_svg_view);
        svgView.start();

        new Timer().schedule(new TimerTask() {
            public void run() {


                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        }, 2000);


    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }

}
