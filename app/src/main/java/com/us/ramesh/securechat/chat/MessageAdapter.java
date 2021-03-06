package com.us.ramesh.securechat.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.us.ramesh.securechat.R;
import com.us.ramesh.securechat.Utils.SecureChatPreference;
import com.us.ramesh.securechat.Utils.TimeAgo;
import com.us.ramesh.securechat.chat.Stegonapraphy.ProcessImage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static android.support.constraint.Constraints.TAG;

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

    ProcessImage process_image;
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
        process_image = new ProcessImage();

        if (getItemViewType(position) == TYPE_RECEIVED) {
            String message = data.getMessage();
            String image = data.getSentImage();
            String type = data.getType();

            if(type.equals("text")) {
                if (message != null && message.length() > 0) {

                    viewHolder.receivedMessage.setText(message);
                    viewHolder.senderSentImage.setVisibility(View.GONE);
                    viewHolder.senderSentImageCard.setVisibility(View.GONE);
                    viewHolder.receivedMessage.setVisibility(View.VISIBLE);
                }
            }else {
                if (image != null && image.length() > 0) {
                    viewHolder.senderSentImageCard.setVisibility(View.VISIBLE);
                    viewHolder.senderSentImage.setVisibility(View.VISIBLE);
                    viewHolder.receivedMessage.setVisibility(View.GONE);
                    Uri receivedImage = Uri.parse(image);
                    viewHolder.senderSentImage.setImageURI(receivedImage);
                }
            }

            Long timeStamp = data.getTime();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(new Date(Long.parseLong(String.valueOf(timeStamp))));

            TimeAgo timeAgo = new TimeAgo(ctx);
            try {
                Date  date1 = formatter.parse(dateString.split("\\.")[0]);
                viewHolder.senderTime.setText(timeAgo.timeAgo(date1));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            viewHolder.senderImage.setImageURI(Uri.parse(receiver_image));

        } else {
            String message = data.getMessage();
            String image = data.getSentImage();
            String type = data.getType();

            if(type.equals("text")) {
                if (message != null && message.length() > 0) {
                    viewHolder.sentImageCard.setVisibility(View.GONE);
                    viewHolder.currentMessage.setText(message);
                    viewHolder.sentImage.setVisibility(View.GONE);
                    viewHolder.currentMessage.setVisibility(View.VISIBLE);
                }
            }else {
                if (image != null && image.length() > 0) {
                    viewHolder.sentImage.setVisibility(View.VISIBLE);
                    viewHolder.sentImageCard.setVisibility(View.VISIBLE);
                    viewHolder.currentMessage.setVisibility(View.GONE);

                    Uri sentImage = Uri.parse(image);
                    viewHolder.sentImage.setImageURI(sentImage);
                }
            }

            Long timeStamp = data.getTime();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateString = formatter.format(new Date(Long.parseLong(String.valueOf(timeStamp))));

            TimeAgo timeAgo = new TimeAgo(ctx);
            try {
                Date  date1 = formatter.parse(dateString.split("\\.")[0]);
                viewHolder.uTime.setText(timeAgo.timeAgo(date1));
            } catch (ParseException e) {
                e.printStackTrace();
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

        FROM = model.getFrom();
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


        TextView uTime, currentMessage, senderTime, receivedMessage;
        SimpleDraweeView sentImage, senderSentImage;
        CardView sentImageCard, senderSentImageCard;

        SimpleDraweeView currentImage, senderImage;
        LinearLayout MessageRow;


        public SimpleStringRecyclerViewAdapter(View itemView) {
            super(itemView);


            // Layout for user
            uTime = itemView.findViewById(R.id.uTime);
            currentMessage = itemView.findViewById(R.id.uMessage);
            currentImage = itemView.findViewById(R.id.uImage);
            sentImage = itemView.findViewById(R.id.sentImage);
            sentImageCard = itemView.findViewById(R.id.sentImageCard);
            MessageRow = itemView.findViewById(R.id.userMessagesRow);

            // Layout for friend
            senderTime = itemView.findViewById(R.id.senderTime);
            receivedMessage = itemView.findViewById(R.id.senderMessage);
            senderImage = itemView.findViewById(R.id.senderImage);
            senderSentImage = itemView.findViewById(R.id.senderSentImage);
            senderSentImageCard = itemView.findViewById(R.id.senderSentImageCard);
            MessageRow = itemView.findViewById(R.id.userMessagesRow);


            MessageRow.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence options[] = new CharSequence[]{"Decrypt"};

                    final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Select Options");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {

                            if(mMessageList.get(getAdapterPosition()).getType().equalsIgnoreCase("image")) {
                                String imageUrl = mMessageList.get(getAdapterPosition()).getSentImage();

                                if(imageUrl != null || imageUrl != ""){
                                    final String[] message_to_show = {""};

                                    Picasso.with(ctx).load(imageUrl).into(new Target() {
                                        @Override
                                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                            message_to_show[0] = process_image.displayMessage(bitmap);

                                            decryptText(message_to_show[0]);
                                        }

                                        @Override
                                        public void onBitmapFailed(Drawable errorDrawable) {
                                            Log.e(TAG, "The image was not obtained");
                                        }

                                        @Override
                                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                                            //Here you should place a loading gif in the ImageView to
                                            //while image is being obtained.
                                        }
                                    });


                                }

                            }else {
                                String msz = mMessageList.get(getAdapterPosition()).getMessage();

                                if (i == 0) {

                                    decryptText(msz);


                                }
                            }


                        }

                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return false;
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

    public void decryptText(String msz){

        try {
            decString = decrypt(msz, mCurrentUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (decString != null && decString.length() > 0) {
            Toast.makeText(ctx, decString, Toast.LENGTH_SHORT).show();
            decString = "";
        } else {
            Toast.makeText(ctx, "Cannot decrpyt", Toast.LENGTH_SHORT).show();
        }
    }
}
