package com.us.ramesh.securechat.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;
import com.us.ramesh.securechat.R;
import com.us.ramesh.securechat.Utils.AESEncryption;
import com.us.ramesh.securechat.all_users.activity.ShowUsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class ChatActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    String receiver_id, receiver_name, receiver_Image;
    private Toolbar toolbar;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private SwipeRefreshLayout mSwipeRefreshLayout;  //object for swipe refresh

    private String mCurrentUserId;

    private ImageButton btn_add_images;
    private ImageView btn_encrypt;
    private boolean enc;
    private EditText et_message;
    private Button btn_send;


    private RecyclerView rv_messagesList;

    private MessageModel mMessageModel;
    private MessageAdapter mMessageAdapter;
    private final List<MessageModel> messageList = new ArrayList<>();

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private LinearLayoutManager mLinearLayoutmanager;

    private int itemPosition = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

    String message;
    String encMessage;

    /**
     * AES class variable
     **/
    private AESEncryption aesEncryption = null;


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

        try {
            aesEncryption = new AESEncryption(receiver_id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        rv_messagesList = findViewById(R.id.rv_Chatlist);

        mMessageAdapter = new MessageAdapter(messageList, this);

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


                }
            }
        });

        loadmessages();


        if (receiver_Image != null && !receiver_Image.equals("")) {
            Uri profileUri = Uri.parse(receiver_Image);
            Picasso.with(ChatActivity.this)
                    .load(profileUri)
                    .into(userImage);
        }


        getSupportActionBar().setTitle(receiver_name.substring(0, receiver_name.indexOf(" ")));


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
                        encMessage = aesEncryption.encrypt(message);
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

            DatabaseReference user_msg_push = mRootRef.child("Messages").child(mCurrentUserId).child(receiver_id).push();

            String push_id = user_msg_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", msg);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);


            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            et_message.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {


                    }
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
}
