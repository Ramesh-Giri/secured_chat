package com.us.ramesh.securedchat.login.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.us.ramesh.securedchat.DashboardActivity;
import com.us.ramesh.securedchat.Utils.NetworkUtils;
import com.us.ramesh.securedchat.Utils.SecureChatPreference;
import  com.us.ramesh.securedchat.R;
import com.us.ramesh.securedchat.login.ResetPasswordActivity;


public class LoginFragment extends Fragment {


    private SecureChatPreference mPrefs;
    private String UserId, userThumbnail, userName,userEmail;

    private DatabaseReference mUserDatabase;

    Button uLogin; //login button
    TextView uSignup, password_reset;
    EditText mEmail, mPassword; //text for signup

    String lEmail, lPassword; //strings to store the username and password


    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth = FirebaseAuth.getInstance();

        mPrefs = new SecureChatPreference(getActivity());

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        uSignup = v.findViewById(R.id.textSignup);
        mEmail = v.findViewById(R.id.et_loginEmail);
        mPassword = v.findViewById(R.id.et_loginPassword);
        uLogin = v.findViewById(R.id.btn_Login);
        password_reset = v.findViewById(R.id.tv_forgetPassword);

        password_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ResetPasswordActivity.class);
                startActivity(intent);
                getActivity().finish();

            }
        });

        uLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                lEmail = mEmail.getText().toString();
                lPassword = mPassword.getText().toString();

                if (!lEmail.isEmpty() && !lPassword.isEmpty()) {

                    if (NetworkUtils.isConnected(getContext())) {
                        showProgressDialog();

                        mAuth.signInWithEmailAndPassword(lEmail, lPassword)
                                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (!task.isSuccessful()) {

                                            Toast.makeText(getActivity(), " Invalid Email or Password", Toast.LENGTH_SHORT).show();

                                        } else {

                                            UserId = mAuth.getCurrentUser().getUid();
                                            mUserDatabase = FirebaseDatabase.getInstance()
                                                    .getReference()
                                                    .child("Users")
                                                    .child(UserId);
                                            mUserDatabase.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    userName = dataSnapshot.child("fullname").getValue().toString();
                                                    userThumbnail = dataSnapshot.child("thumbImage").getValue().toString();
                                                    userEmail = dataSnapshot.child("email").getValue().toString();


                                                    mPrefs.setHasLoggedIn(true);
                                                    mPrefs.setAccountId(UserId);
                                                    mPrefs.setAccountProfileImage(userThumbnail);
                                                    mPrefs.setAccountName(userName);
                                                    mPrefs.setAccountEmail(userEmail);

                                                    Intent dashboard = new Intent(getActivity(), DashboardActivity.class);
                                                    startActivity(dashboard);

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }

                                            });


                                       }
                                        progressDialog.dismiss();


                                    }
                                });


                    } else {

                        Snackbar.make(view, "Please Connect to internet", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
                if (lEmail.isEmpty() && lPassword.isEmpty()) {
                    mEmail.setError("All fields are necessary.");
                }

            }
        });

        uSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.container_Main, new SignupFragment()).addToBackStack(null).commitAllowingStateLoss();
            }
        });

        return v;

    }


    public void showProgressDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }


}
