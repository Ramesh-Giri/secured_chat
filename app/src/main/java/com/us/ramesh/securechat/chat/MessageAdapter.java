package com.us.ramesh.securechat.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.us.ramesh.securechat.R;
import com.us.ramesh.securechat.Utils.SecureChatPreference;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MessageAdapter extends RecyclerSwipeAdapter<MessageAdapter.SimpleStringRecyclerViewAdapter> {

    private List<MessageModel> mMessageList;

    private int TYPE_SEND = 10;
    private int TYPE_RECEIVED = 11;
    ArrayList<MessageModel> data;
    String mCurrentUser;

    private SecureChatPreference mPrefs;
    private FirebaseAuth mAuth;
    Context ctx;

    String receiver_image;
    String decString;


    String FROM;
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
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_second, parent, false);
            viewHolder = new MessageAdapter.SimpleStringRecyclerViewAdapter(v);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MessageAdapter.SimpleStringRecyclerViewAdapter viewHolder, int position) {
        MessageModel data = mMessageList.get(position);


        if (getItemViewType(position) == TYPE_RECEIVED) {
            String message = data.getMessage();

            if (message != null) {
                viewHolder.senderSentImageCard.setVisibility(View.GONE);
                viewHolder.receivedMessage.setText(message);
                viewHolder.senderSentImage.setVisibility(View.GONE);
                viewHolder.receivedMessage.setVisibility(View.VISIBLE);
            }

            viewHolder.senderImage.setImageURI(Uri.parse(receiver_image));

        } else {
            String message = data.getMessage();

            if (message != null) {
                viewHolder.sentImageCard.setVisibility(View.GONE);
                viewHolder.currentMessage.setText(message);
                viewHolder.sentImage.setVisibility(View.GONE);
                viewHolder.currentMessage.setVisibility(View.VISIBLE);
            }

            mPrefs = new SecureChatPreference(ctx);
            viewHolder.currentImage.setImageURI(Uri.parse(mPrefs.getAccountProfileImage()));

        }

    }

    @Override
    public int getItemViewType(int position) {
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();
        MessageModel model = mMessageList.get(position);

        FROM= model.getFrom();
        if (model.getFrom().equals(mCurrentUser)) {
            return TYPE_SEND;

        } else {
            return TYPE_RECEIVED;
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


        TextView uId, currentMessage, senderId, receivedMessage;
        ImageView sentImage, senderSentImage;
        CardView sentImageCard, senderSentImageCard;

        SimpleDraweeView currentImage, senderImage;
        LinearLayout MessageRow;


        public SimpleStringRecyclerViewAdapter(View itemView) {
            super(itemView);


            // Layout for user
            uId = itemView.findViewById(R.id.uTime);
            currentMessage = itemView.findViewById(R.id.uMessage);
            currentImage = itemView.findViewById(R.id.uImage);
            sentImage = itemView.findViewById(R.id.sentImage);
            sentImageCard = itemView.findViewById(R.id.sentImageCard);
            MessageRow = itemView.findViewById(R.id.userMessagesRow);

            // Layout for friend
            senderId = itemView.findViewById(R.id.senderTime);
            receivedMessage = itemView.findViewById(R.id.senderMessage);
            senderImage = itemView.findViewById(R.id.senderImage);
            senderSentImage = itemView.findViewById(R.id.senderSentImage);
            senderSentImageCard = itemView.findViewById(R.id.senderSentImageCard);
            MessageRow = itemView.findViewById(R.id.userMessagesRow);


            MessageRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        CharSequence options[] = new CharSequence[]{"Decrypt"};

                        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                String msz = mMessageList.get(getAdapterPosition()).getMessage();

                                if (i == 0) {

                                    try {
                                        decString = decrypt(msz, mCurrentUser);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(ctx, decString, Toast.LENGTH_SHORT).show();

                                    decString= "";
                                }
                            }

                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }

            });
        }
    }

    private String decrypt(String msz, String mCurrentUser) throws Exception {

        String str = "cannot decrypt";

        SecretKeySpec key = ChatActivity.generateKey(mCurrentUser);

        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(msz, Base64.DEFAULT);

        byte[] decValue = c.doFinal(decodedValue);
        String decryptedValue = new String(decValue);
        if (decryptedValue.trim().length() > 0) {
            return decryptedValue.trim();

        } else
            return str;
    }
}
