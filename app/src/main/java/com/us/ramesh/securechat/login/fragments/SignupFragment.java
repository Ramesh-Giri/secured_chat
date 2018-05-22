package com.us.ramesh.securechat.login.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.us.ramesh.securechat.R;
import com.us.ramesh.securechat.Utils.NetworkUtils;
import com.us.ramesh.securechat.login.model.RegisterModel;


/**
 * Created by ramesh on 3/13/18.
 */

public class SignupFragment extends Fragment {

    private com.google.firebase.auth.FirebaseAuth mAuth;

    String UserId;
    EditText mFullName, mEmail, mPassword, mCpasswrod;

    ProgressDialog progressDialog;

    RegisterModel mRegisterModel;
    TextView txtSignIn;

    String fullname, email, password, cPassword;
    Button btnRegister;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        mAuth = FirebaseAuth.getInstance();

        mFullName = view.findViewById(R.id.et_fullname);
        mEmail = view.findViewById(R.id.et_email);
        mPassword = view.findViewById(R.id.et_password);
        mCpasswrod = view.findViewById(R.id.et_cPassword);
        txtSignIn = view.findViewById(R.id.textSignin);


        btnRegister = view.findViewById(R.id.btn_register);


        mRegisterModel = new RegisterModel();


        btnRegister.setOnClickListener(new View.OnClickListener() {

            // [Start btnRegister.setOnClickListener]
            @Override
            public void onClick(View view) {

                fullname = mFullName.getText().toString();
                email = mEmail.getText().toString();
                password = mPassword.getText().toString();
                cPassword = mCpasswrod.getText().toString();


                if (fullname.isEmpty() || email.isEmpty() || password.isEmpty() || cPassword.isEmpty()) {

                    Toast.makeText(getActivity(), "Fill all the fields", Toast.LENGTH_SHORT).show();


                } else {

                    if (validForm()) {
                        if (NetworkUtils.isConnected(getContext())) {
                            showProgressDialog();

                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            progressDialog.dismiss();

                                            if (!task.isSuccessful()) {
                                                Toast.makeText(getActivity(), " Email already exist", Toast.LENGTH_SHORT).show();


                                            } else {
                                                UserId = mAuth.getCurrentUser().getUid();
                                                setToModel();
                                                saveToFirebase(UserId);
                                                Toast.makeText(getActivity(), "Signup Successful!!!", Toast.LENGTH_SHORT).show();
                                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container_Main, new LoginFragment()).commitAllowingStateLoss();
                                            }


                                        }
                                    });

                        } else {

                            Snackbar.make(view, "Please Connect to internet", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    }

                }


            }
        });    // [End btnRegister.setOnClickListener]

        txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container_Main, new LoginFragment()).commitAllowingStateLoss();
            }
        });

        return view;
    }

    public void setToModel() {
        mRegisterModel.setId(UserId);
        mRegisterModel.setFullname(fullname);
        mRegisterModel.setEmail(email);
        mRegisterModel.setUserImage("-");
        mRegisterModel.setThumbImage("-");
        mRegisterModel.setAddress("-");
        mRegisterModel.setMobno("-");
        mRegisterModel.setStatus("Hey! I am using Secured chat");
        mRegisterModel.setGender("-");
        mRegisterModel.setInterests("Chating");
        mRegisterModel.setActive(false);


    }

    // [Start saveToFirebase]
    public void saveToFirebase(String UserId) {


        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'id' node
        DatabaseReference mFirebaseDatabase = mFirebaseInstance.getReference("Users");


        mFirebaseDatabase.child(UserId).setValue(mRegisterModel);


    }

    // [End saveToFirebase]

    // [Start validFrom]
    public boolean validForm() {

        boolean valid = true;
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (email.matches(emailPattern)) {
            mEmail.setError(null);
        } else {
            mEmail.setError("Enter a valid email address");
            valid = false;
        }

        if (password.length() < 8) {
            mPassword.setError("Password should be greater than 8 characters!");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        if (password.equals(cPassword)) {
            mCpasswrod.setError(null);
        } else {
            mCpasswrod.setError("Password doesnot match");
            valid = false;
        }

        return valid;
    }

    public void showProgressDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
}
