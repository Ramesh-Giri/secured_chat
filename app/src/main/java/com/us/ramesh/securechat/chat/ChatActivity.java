package com.us.ramesh.securechat.chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.us.ramesh.securechat.R;
import com.us.ramesh.securechat.all_users.activity.ShowUsers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    String receiver_id, receiver_name, receiver_Image;
    private Toolbar toolbar;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout mSwipeRefreshLayout;  //object for swipe refresh

    private String mCurrentUserId;
    private static final int SELECT_PHOTO = 100;

    DatabaseReference user_msg_push;

    private ImageButton btn_add_images;
    private ImageView btn_encrypt;
    private boolean enc;
    private EditText et_message;
    private Button btn_send;
    Uri resultUri;

    private RecyclerView rv_messagesList;

    private MessageModel mMessageModel;
    private MessageAdapter mMessageAdapter;
    private final List<MessageModel> messageList = new ArrayList<>();

    

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    FirebaseStorage storage;
    StorageReference storageRef, imageRef;
    String urlIMAGE;

    UploadTask uploadTask;

    ProgressDialog progressDialog;


    private LinearLayoutManager mLinearLayoutmanager;

    private int itemPosition = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    String message;
    String encMessage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CircularImageView userImage = toolbar.findViewById(R.id.iv_logo);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUserId = mAuth.getUid();

        receiver_id = getIntent().getStringExtra("receiverId");
        receiver_name = getIntent().getStringExtra("receiverName");
        receiver_Image = getIntent().getStringExtra("receiverImage");

        btn_add_images = findViewById(R.id.btn_add_images);
        btn_encrypt = findViewById(R.id.btn_encrypt);
        et_message = findViewById(R.id.et_message);
        btn_send = findViewById(R.id.btn_send);
        mSwipeRefreshLayout = findViewById(R.id.swipeChatLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        rv_messagesList = findViewById(R.id.rv_Chatlist);

        mMessageAdapter = new MessageAdapter(messageList, this);

        //creates a storage reference
        storageRef = FirebaseStorage.getInstance().getReference();


        mLinearLayoutmanager = new LinearLayoutManager(this);
        rv_messagesList.setLayoutManager(mLinearLayoutmanager);
        mMessageAdapter.setImage(receiver_Image);
        rv_messagesList.setAdapter(mMessageAdapter);

        btn_encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_encrypt.isSelected()) {
                    btn_encrypt.setSelected(false);
                    enc = false;

                } else {
                    btn_encrypt.setSelected(true);
                    enc = true;
                    Toast.makeText(ChatActivity.this, "Your message will be encrypted", Toast.LENGTH_SHORT).show();

                }
            }
        });

        btn_add_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addImage();
            }
        });


        loadmessages();

        if (receiver_Image != null && !receiver_Image.equals("")) {
            Uri profileUri = Uri.parse(receiver_Image);
            Picasso.with(ChatActivity.this)
                    .load(profileUri)
                    .into(userImage);
        }




        getSupportActionBar().setTitle(receiver_name);


        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(receiver_id)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);


                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + receiver_id, chatAddMap);
                    chatUserMap.put("Chat/" + receiver_id + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {


                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = et_message.getText().toString();

                if (enc) {
                    /* Encrypted message is send */

                    try {
                        encMessage = encrypt(message, receiver_id);

                        //CODE TO STEGO, WRITE HERE

                        sendmessage(encMessage);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    /* Normal message is send */
                    sendmessage(message);
                }
            }
        });

    }


    public void sendmessage(String msg) {

        if (!TextUtils.isEmpty(msg)) {

            String current_user_ref = "Messages/" + mCurrentUserId + "/" + receiver_id;
            String chat_user_ref = "Messages/" + receiver_id + "/" + mCurrentUserId;

            user_msg_push = mRootRef.child("Messages").child(mCurrentUserId).child(receiver_id).push();

            String push_id = user_msg_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", msg);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);
            messageMap.put("sentImage", "");


            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            et_message.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                }
            });

        }


    }


    public void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserId).child(receiver_id);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                MessageModel mModel = dataSnapshot.getValue(MessageModel.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {
                    messageList.add(itemPosition++, mModel);
                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPosition == 1) {
                    mLastKey = messageKey;
                }


                mMessageAdapter.notifyDataSetChanged();
                mLinearLayoutmanager.scrollToPositionWithOffset(itemPosition - 1, 0);


                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void loadmessages() {

        messageList.clear();

        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserId).child(receiver_id);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                if (dataSnapshot != null && dataSnapshot.getValue() != null) {

                    MessageModel mModel = dataSnapshot.getValue(MessageModel.class);

                    itemPosition++;


                    if (itemPosition == 1) {
                        String messageKey = dataSnapshot.getKey();
                        mLastKey = messageKey;
                        mPrevKey = messageKey;

                    }

                    messageList.add(mModel);
                    mMessageAdapter.notifyDataSetChanged();

                    rv_messagesList.scrollToPosition(messageList.size() - 1); //to show recently add item in recycler view


                    if (mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(ChatActivity.this, ShowUsers.class);
        startActivity(intent);
        this.finish();
    }


    @Override
    public void onRefresh() {

        mCurrentPage++;
        itemPosition = 0;
        loadMoreMessages();

    }

    public String encrypt(String msz, String pwd) throws Exception {

        SecretKeySpec key = generateKey(pwd);
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);

        byte[] encVal = c.doFinal(msz.getBytes());
        String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);

        return encryptedValue;

    }

    public static SecretKeySpec generateKey(String pwd) throws Exception {

        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = pwd.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;

    }


    public void addImage() {

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
                    .setAspectRatio(2, 3)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Sending...");
                progressDialog.setMax(100);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(true);
                progressDialog.show();

                resultUri = result.getUri();

                    final File thumb_filePath = new File(resultUri.getPath());
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(350)
                            .setMaxHeight(350)
                            .setQuality(80)
                            .compressToBitmap(thumb_filePath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                    Calendar c = Calendar.getInstance();


                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String mydate = format.format(c.getTime());

                    final StorageReference thumb_filepath = storageRef.child("sent_images").child(mydate + ".jpg");

                    thumb_filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {

                                UploadTask uploadThumb = thumb_filepath.putBytes(thumb_byte);
                                uploadThumb.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> thumbTask) {

                                        final String thumb_downloadURL = thumbTask.getResult().getDownloadUrl().toString();

                                        if (thumbTask.isSuccessful()) {
                                            String current_user_ref = "Messages/" + mCurrentUserId + "/" + receiver_id;
                                            String chat_user_ref = "Messages/" + receiver_id + "/" + mCurrentUserId;

                                            user_msg_push = mRootRef.child("Messages").child(mCurrentUserId).child(receiver_id).push();
                                            String push_id = user_msg_push.getKey();


                                            Map sentImage = new HashMap();
                                            sentImage.put("sentImage", thumb_downloadURL);
                                            sentImage.put("message", "");
                                            sentImage.put("seen", false);
                                            sentImage.put("type", "image");
                                            sentImage.put("time", ServerValue.TIMESTAMP);
                                            sentImage.put("from", mCurrentUserId);

                                            Map messageUserMap = new HashMap();
                                            messageUserMap.put(current_user_ref + "/" + push_id, sentImage);
                                            messageUserMap.put(chat_user_ref + "/" + push_id, sentImage);


                                            mRootRef.updateChildren(messageUserMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    if (thumbTask.isSuccessful()) {

                                                        progressDialog.dismiss();
                                                        resultUri=null;


                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Error in uploading thumbnail.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });


                            } else {
                                Toast.makeText(getApplicationContext(), "ERROR!!!", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }


                        }
                    });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
