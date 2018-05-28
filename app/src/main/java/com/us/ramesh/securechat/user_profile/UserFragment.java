package com.us.ramesh.securechat.user_profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
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
import com.us.ramesh.securechat.R;
import com.us.ramesh.securechat.Utils.SecureChatPreference;
import com.us.ramesh.securechat.dashboard.DashboardActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;

public class UserFragment extends Fragment {

    SimpleDraweeView user_ProfileImage;
    TextView user_ProfileName, user_ProfileStatus, user_ProfileEmail, user_ProfileAddress, user_ProfileNumber, user_ProfileGender, user_ProfileInterest;

    Button btn_EditProfile;

    private SecureChatPreference mPrefs;
    private String userId;

    private StorageReference storageRef;

    private static final int SELECT_PHOTO = 1;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_user_profile, container, false);

        mPrefs = new SecureChatPreference(getActivity());
        userId = mPrefs.getAccountId();

        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
        mFirebaseDatabase.keepSynced(true);


        user_ProfileImage = view.findViewById(R.id.user_ProfileImage);
        user_ProfileName = view.findViewById(R.id.user_ProfileName);
        user_ProfileEmail = view.findViewById(R.id.user_ProfileEmail);

        user_ProfileStatus = view.findViewById(R.id.user_ProfileStatus);
        user_ProfileAddress = view.findViewById(R.id.user_ProfileAddress);
        user_ProfileNumber = view.findViewById(R.id.user_ProfileNumber);
        user_ProfileGender = view.findViewById(R.id.user_ProfileGender);
        user_ProfileInterest = view.findViewById(R.id.user_ProfileInterest);

        btn_EditProfile = view.findViewById(R.id.btn_EditProfile);

        setTopView();

        setBottomView();

        user_ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProfileImage();
            }
        });

        return view;
    }

    public void setTopView() {

        String profileImg = mPrefs.getAccountProfileImage();
        if (profileImg != null && !profileImg.equals("")) {
            Uri profileUri = Uri.parse(mPrefs.getAccountProfileImage());


            user_ProfileImage.setImageURI(profileUri);

        }
        user_ProfileName.setText(mPrefs.getAccountName());
        user_ProfileEmail.setText(mPrefs.getAccountEmail());
    }

    public void setBottomView() {
        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

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
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                final File thumb_filePath = new File(resultUri.getPath());
                Bitmap thumb_bitmap = new Compressor(getActivity())
                        .setMaxWidth(350)
                        .setMaxHeight(350)
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
                                                    Toast.makeText(getActivity(), "Successful!!!", Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(getActivity(), "Error in uploading thumbnail.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        } else {
                            Toast.makeText(getActivity(), "ERROR!!!", Toast.LENGTH_SHORT).show();
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
