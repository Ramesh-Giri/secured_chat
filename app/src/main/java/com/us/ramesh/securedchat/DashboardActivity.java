package com.us.ramesh.securedchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.us.ramesh.securedchat.Utils.SecureChatPreference;

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

    private DatabaseReference mFirebaseDatabase;
    private FirebaseAuth mAuth;

    private StorageReference storageRef;

    private static final int SELECT_PHOTO = 1;

    private TextView profileName;
    private CircularImageView profileImage;
    private TextView profileEmail;

    private String userId;

    public static ArrayList<String> profList;



    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        profList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        navigationView = findViewById(R.id.nav_view);
        mPrefs = new SecureChatPreference(this);
        userId = mPrefs.getAccountId();

        storageRef = FirebaseStorage.getInstance().getReference();
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFirebaseDatabase.keepSynced(true);


        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        View headerView = navigationView.getHeaderView(0);

        profileName = headerView.findViewById(R.id.tv_ProfileName);
        profileImage = headerView.findViewById(R.id.tv_ProfileImage);
        profileEmail = headerView.findViewById(R.id.tv_ProfileEmail);

        setNavHeader();
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProfileImage();
            }
        });

        //getSupportFragmentManager().beginTransaction().add(R.id.container_Dash, new HomeFragment()).addToBackStack(null).commitAllowingStateLoss();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        toolbar.setTitle("ProFinder");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
           // getSupportFragmentManager().beginTransaction().replace(R.id.container_Dash, new HomeFragment()).addToBackStack(null).commitAllowingStateLoss();

        } else if (id == R.id.nav_logout) {

            mPrefs.setHasLoggedIn(false);
            mPrefs.setAccountName(null);
            mPrefs.setAccountId(null);
            mPrefs.setAccountProfileImage(null);
            mAuth.signOut();

            Intent dashboard = new Intent(DashboardActivity.this, MainActivity.class);
            startActivity(dashboard);
            this.finish();

        } else if (id == R.id.nav_profile) {
            toolbar.setTitle("My Profile");
            //getSupportFragmentManager().beginTransaction().add(R.id.container_Dash, new UserDetailFragment()).addToBackStack(null).commitAllowingStateLoss();

        } else if (id == R.id.nav_aboutUs) {

        }else if (id == R.id.nav_rate) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }


    public void setNavHeader() {
        String profileImg = mPrefs.getAccountProfileImage();
        if (profileImg != null && !profileImg.equals("")) {
            Uri profileUri = Uri.parse(mPrefs.getAccountProfileImage());
            Picasso.with(this)
                    .load(profileUri)
                    .into(profileImage);
        }
        profileName.setText(mPrefs.getAccountName());
        profileEmail.setText(mPrefs.getAccountEmail());
    }


    public void setProfileImage() {

        //Pick Image from gallery
        Intent photoPickerIntent = new Intent();
        photoPickerIntent.setType("image/*");
        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(photoPickerIntent, "SELECT IMAGE"), SELECT_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            CropImage.activity(selectedImage)
                    .setAspectRatio(4, 3)
                    .start(DashboardActivity.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setMessage("Please wait while we upload and process the image.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                final File thumb_filePath = new File(resultUri.getPath());
                Bitmap thumb_bitmap = new Compressor(this)
                        .setMaxWidth(350)
                        .setMaxHeight(250)
                        .setQuality(80)
                        .compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference imageRef = storageRef.child("profile_images").child(userId + ".jpg");
                final StorageReference thumb_filepath = storageRef.child("profile_images").child("thumbnails").child(userId + ".jpg");

                imageRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadThumb = thumb_filepath.putBytes(thumb_byte);
                            uploadThumb.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> thumbTask) {

                                    final String thumb_downloadURL = thumbTask.getResult().getDownloadUrl().toString();

                                    if (thumbTask.isSuccessful()) {
                                        Map update_hashmap = new HashMap();

                                        update_hashmap.put("userImage", downloadUrl);
                                        update_hashmap.put("thumbImage", thumb_downloadURL);

                                        mFirebaseDatabase.updateChildren(update_hashmap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                if (thumbTask.isSuccessful()) {

                                                    mProgressDialog.dismiss();
                                                    mPrefs.setAccountProfileImage(thumb_downloadURL);
                                                    Toast.makeText(DashboardActivity.this, "Successful!!!", Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(DashboardActivity.this, "Error in uploading thumbnail.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        } else {
                            Toast.makeText(DashboardActivity.this, "ERROR!!!", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }


                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
