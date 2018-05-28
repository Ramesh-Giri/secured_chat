package com.us.ramesh.securechat.dashboard;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.us.ramesh.securechat.MainActivity;
import com.us.ramesh.securechat.R;
import com.us.ramesh.securechat.Utils.NetworkUtils;
import com.us.ramesh.securechat.Utils.SecureChatPreference;
import com.us.ramesh.securechat.user_profile.UserFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;


/**
 * Created by ramesh on 3/14/18.
 */

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public NavigationView navigationView;
    public static Toolbar toolbar;
    private SecureChatPreference mPrefs;
    private RelativeLayout rv_userDetails;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseAuth mAuth;

    private DrawerLayout drawer;


    private TextView profileName;
    private SimpleDraweeView profileImage;
    private TextView profileEmail;

    private String userId;

    SwitchCompat status_switch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        drawer = findViewById(R.id.drawer_layout);


        mAuth = FirebaseAuth.getInstance();
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setItemIconTintList(null);



        mPrefs = new SecureChatPreference(this);
        userId = mPrefs.getAccountId();

        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFirebaseDatabase.keepSynced(true);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));

        setNavHeader();

        getSupportFragmentManager().beginTransaction().add(R.id.container_Dash, new HomeFragment()).addToBackStack(null).commitAllowingStateLoss();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        setStatusSwitch();


    }

    @Override
    public void onBackPressed() {
        toolbar.setTitle(R.string.app_name);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container_Dash, new HomeFragment()).addToBackStack(null).commitAllowingStateLoss();

        } else if (id == R.id.nav_logout) {

            Logout();  //Logout code


        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    public void setNavHeader() {

        View headerView = navigationView.getHeaderView(0);

        rv_userDetails = headerView.findViewById(R.id.rv_userDetails);
        profileName = headerView.findViewById(R.id.tv_ProfileName);
        profileImage = headerView.findViewById(R.id.tv_ProfileImage);
        profileEmail = headerView.findViewById(R.id.tv_ProfileEmail);


        rv_userDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().add(R.id.container_Dash, new UserFragment()).addToBackStack(null).commitAllowingStateLoss();
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        String profileImg = mPrefs.getAccountProfileImage();
        if (profileImg != null && !profileImg.equals("")) {
            Uri profileUri = Uri.parse(mPrefs.getAccountProfileImage());
            profileImage.setImageURI(profileUri);
        }
        profileName.setText(mPrefs.getAccountName());
        profileEmail.setText(mPrefs.getAccountEmail());
    }



    public void setStatusSwitch() {
        status_switch = findViewById(R.id.status_switch);
        Menu menu_nav = navigationView.getMenu();
        MenuItem item = menu_nav.findItem(R.id.nav_status);
        status_switch = item.getActionView().findViewById(R.id.status_switch);

        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    Boolean status = Boolean.valueOf(dataSnapshot.child("active").getValue().toString());
                    status_switch.setChecked(status);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        status_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mFirebaseDatabase.child("active").setValue(true);

                } else {
                    mFirebaseDatabase.child("active").setValue(false);

                }
            }
        });
    }

    public void Logout() {

        if (NetworkUtils.isConnected(DashboardActivity.this)) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(DashboardActivity.this);

            alertDialog.setMessage("Do you want to logout?");

            alertDialog.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            // Write your code here to execute after dialog
                            dialog.cancel();
                        }
                    });

            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {


                    mFirebaseDatabase.child("active").setValue(false);

                    mPrefs.setHasLoggedIn(false);
                    mPrefs.setAccountName(null);
                    mPrefs.setAccountId(null);
                    mPrefs.setAccountProfileImage(null);
                    mAuth.signOut();

                    Intent dashboard = new Intent(DashboardActivity.this, MainActivity.class);
                    startActivity(dashboard);
                    DashboardActivity.this.finish();

                }
            });
            alertDialog.show();


        } else {

            Toast.makeText(DashboardActivity.this, "There is a problem signing out!!!", Toast.LENGTH_SHORT).show();
        }


    }
}



