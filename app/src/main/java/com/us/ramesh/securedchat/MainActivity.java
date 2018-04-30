package com.us.ramesh.securedchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.us.ramesh.securedchat.Utils.SecureChatPreference;
import com.us.ramesh.securedchat.login.fragments.LoginFragment;

public class MainActivity extends AppCompatActivity{

    private SecureChatPreference mPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = new SecureChatPreference(this);

        if (mPrefs.hasLoggedIn()){
            gotoDashboard();

        }
        else {
            getSupportFragmentManager().beginTransaction().add(R.id.container_Main, new LoginFragment()).addToBackStack(null).commitAllowingStateLoss();

        }



    }

    public void gotoDashboard(){
        Intent dashboard = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(dashboard);
        this.finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}

