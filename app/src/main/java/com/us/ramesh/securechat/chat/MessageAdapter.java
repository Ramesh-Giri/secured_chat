package com.us.ramesh.securechat.chat;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;
import com.us.ramesh.securechat.R;

import java.util.List;

public class MessageAdapter extends RecyclerSwipeAdapter<MessageAdapter.SimpleStringRecyclerViewAdapter> {

    private List<MessageModel> mMessageList;
    private DatabaseReference mUserDatabase;

    private int TYPE_SEND = 10;
    private int TYPE_RECEIVED = 11;

    private FirebaseAuth mAuth;
    Context ctx;

    String receiver_image;

    public MessageAdapter(List<MessageModel> mMessageList, Context context) {
        this.mMessageList = mMessageList;
        this.ctx = context;
    }

    public void setImage(String img) {
        this.receiver_image = img;
    }


    @Override
    public SimpleStringRecyclerViewAdapter onCreateViewHolder(ViewGroup parent, int viewType) {

        SimpleStringRecyclerViewAdapter viewHolder;

        if (viewType == TYPE_SEND) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_first, parent, false);
            viewHolder = new MessageAdapter.SimpleStringRecyclerViewAdapter(v);
        }else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_second, parent, false);
            viewHolder = new MessageAdapter.SimpleStringRecyclerViewAdapter(v);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MessageAdapter.SimpleStringRecyclerViewAdapter viewHolder, int position) {
        MessageModel c = mMessageList.get(position);
        mAuth = FirebaseAuth.getInstance();

        String current_user_id = mAuth.getCurrentUser().getUid();

        String from_user = c.getFrom();
        viewHolder.messageText.setText(c.getMessage());

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String image = dataSnapshot.child("thumbImage").getValue().toString();

                Picasso.with(viewHolder.profImage.getContext()).load(image)
                        .placeholder(R.drawable.ic_user).into(viewHolder.profImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if (from_user != null && from_user.equals(current_user_id)) {

            viewHolder.messageText.setBackgroundResource(R.drawable.bg_sent_chat);
            viewHolder.messageText.setTextColor(Color.parseColor("#3E4143"));

            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) viewHolder.profImage.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

            RelativeLayout.LayoutParams layoutParams1 =
                    (RelativeLayout.LayoutParams) viewHolder.messageText.getLayoutParams();
            layoutParams1.addRule(RelativeLayout.LEFT_OF, R.id.cv_current_image);


        } else {
            viewHolder.messageText.setBackgroundResource(R.drawable.bg_received_chat);
            viewHolder.messageText.setTextColor(Color.parseColor("#FFFFFF"));

            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) viewHolder.profImage.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

            RelativeLayout.LayoutParams layoutParams1 =
                    (RelativeLayout.LayoutParams) viewHolder.messageText.getLayoutParams();
            layoutParams1.addRule(RelativeLayout.RIGHT_OF, R.id.cv_current_image);


        }

    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return 0;
    }

    public class SimpleStringRecyclerViewAdapter extends RecyclerView.ViewHolder {


        public TextView messageText;
        public CircularImageView profImage;

        public SimpleStringRecyclerViewAdapter(View itemView) {
            super(itemView);


            messageText = itemView.findViewById(R.id.tv_message);
            profImage = itemView.findViewById(R.id.cv_current_image);
        }
    }
}
