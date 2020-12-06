package com.us.ramesh.securechat.all_users.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.us.ramesh.securechat.R;
import com.us.ramesh.securechat.Utils.NetworkUtils;
import com.us.ramesh.securechat.Utils.SecureChatPreference;
import com.us.ramesh.securechat.all_users.adapter.UserAdapter;
import com.us.ramesh.securechat.login.model.RegisterModel;

import java.util.ArrayList;


public class ShowUsers extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private DatabaseReference mFirebaseDatabase;

    ProgressDialog progressDialog;
    private SecureChatPreference mPrefs;

    RegisterModel mRegisterModel;
    RecyclerView UserLayout;
    Toolbar toolbar;

    String userId;

    UserAdapter userAdapter;

    ArrayList<RegisterModel> userArrayList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;  //object for swipe refresh

    private LinearLayoutManager linearLayoutManager;  //layout manager for RecyclerView


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_users);
        mPrefs = new SecureChatPreference(this);
        userId = mPrefs.getAccountId();

        toolbar = findViewById(R.id.toolbar);
        initToolbar();
        UserLayout = findViewById(R.id.userLayout);

        mSwipeRefreshLayout = findViewById(R.id.swipeUsers);
        mSwipeRefreshLayout.setOnRefreshListener(this);


        linearLayoutManager = new LinearLayoutManager(this);
        UserLayout.setLayoutManager(linearLayoutManager);


        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mFirebaseDatabase.keepSynced(true);

        userAdapter = new UserAdapter(this);
        UserLayout.setAdapter(userAdapter);


        userId = mPrefs.getAccountId();

        showData();

        if (NetworkUtils.isConnected(this)) {
        } else {
            Toast.makeText(this, "You are currently offline", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onRefresh() {

        if (NetworkUtils.isConnected(this)) {
            showData();
        } else {
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "You are currently offline.", Toast.LENGTH_LONG).show();
            }

        }
    }

    public void showData() {
        showProgressDialog();

        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    userArrayList.clear();
                    for (DataSnapshot userSnap : dataSnapshot.getChildren()) {

                        mRegisterModel = userSnap.getValue(RegisterModel.class);

                        if (!userSnap.getKey().equals(userId)) {
                            userArrayList.add(mRegisterModel);
                        }


                    }
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    userAdapter.setData(userArrayList);
                    userAdapter.notifyDataSetChanged();
                }
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }


    public void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("All Users");
    }
}
