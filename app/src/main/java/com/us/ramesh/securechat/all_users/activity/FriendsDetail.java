package com.us.ramesh.securechat.all_users.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.us.ramesh.securechat.R;

public class FriendsDetail extends AppCompatActivity {

    SimpleDraweeView user_ProfileImage;
    TextView user_ProfileName, user_ProfileStatus, user_ProfileEmail, user_ProfileAddress, user_ProfileNumber, user_ProfileGender, user_ProfileInterest;

    Button btn_EditProfile;
    String userName;
    Toolbar toolbar;
    DatabaseReference mDatabaseRefrence;

    String receiverId;
    String profileImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_detail);
        toolbar = findViewById(R.id.toolbar);
        receiverId = getIntent().getStringExtra("receiverId");

        user_ProfileImage = findViewById(R.id.user_ProfileImage);
        user_ProfileName = findViewById(R.id.user_ProfileName);
        user_ProfileEmail = findViewById(R.id.user_ProfileEmail);

        user_ProfileStatus = findViewById(R.id.user_ProfileStatus);
        user_ProfileAddress = findViewById(R.id.user_ProfileAddress);
        user_ProfileNumber = findViewById(R.id.user_ProfileNumber);
        user_ProfileGender = findViewById(R.id.user_ProfileGender);
        user_ProfileInterest = findViewById(R.id.user_ProfileInterest);

        getUserData();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void initToolbar(String userName) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(userName);
    }

    public void getUserData() {

        mDatabaseRefrence = FirebaseDatabase.getInstance().getReference("Users").child(receiverId);
        mDatabaseRefrence.keepSynced(true);

        mDatabaseRefrence.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    userName = dataSnapshot.child("fullname").getValue().toString();
                    initToolbar(userName);
                    user_ProfileName.setText(userName);
                    user_ProfileEmail.setText(dataSnapshot.child("email").getValue().toString());

                    profileImg = dataSnapshot.child("thumbImage").getValue().toString();

                    if (profileImg != null && !profileImg.equals("")) {
                        Uri profileUri = Uri.parse(profileImg);
                        user_ProfileImage.setImageURI(profileUri);

                    }
                    user_ProfileAddress.setText(dataSnapshot.child("address").getValue().toString());
                    user_ProfileGender.setText(dataSnapshot.child("gender").getValue().toString());
                    user_ProfileInterest.setText(dataSnapshot.child("interests").getValue().toString());
                    user_ProfileNumber.setText(dataSnapshot.child("mobno").getValue().toString());
                    user_ProfileStatus.setText(dataSnapshot.child("status").getValue().toString());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}


