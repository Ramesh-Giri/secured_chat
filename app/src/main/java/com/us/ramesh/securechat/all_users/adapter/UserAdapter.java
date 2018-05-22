package com.us.ramesh.securechat.all_users.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;
import com.us.ramesh.securechat.R;
import com.us.ramesh.securechat.all_users.activity.FriendsDetail;
import com.us.ramesh.securechat.chat.ChatActivity;
import com.us.ramesh.securechat.login.model.RegisterModel;

import java.util.ArrayList;


/**
 * Created by ramesh on 11/21/17.
 */

public class UserAdapter extends RecyclerSwipeAdapter<UserAdapter.SimpleStringRecyclerViewAdapter> {

    private Context c;


    private ArrayList<RegisterModel> userData;

    private static LayoutInflater inflater;

    public UserAdapter(Context context) {
        inflater = inflater.from(context);
        this.c = context;

    }

    public void setData(ArrayList<RegisterModel> data) {
        this.userData = data;

        notifyItemRangeChanged(0, data.size());
    }

    @Override
    public SimpleStringRecyclerViewAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
        UserAdapter.SimpleStringRecyclerViewAdapter viewHolder;
        View view = inflater.inflate(R.layout.layout_display_users, parent, false);
        viewHolder = new UserAdapter.SimpleStringRecyclerViewAdapter(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(UserAdapter.SimpleStringRecyclerViewAdapter viewHolder, int position) {

        final RegisterModel data = userData.get(position);


        viewHolder.uname.setText(data.getFullname());
        viewHolder.uStatus.setText(data.getStatus());

        String profileImg = data.getThumbImage();
        if (profileImg != null && !profileImg.equals("-")) {
            Uri profileUri = Uri.parse(profileImg);
            Picasso.with(c)
                    .load(profileUri)
                    .into(viewHolder.usrImage);
        }

        Boolean act = data.getActive();
        if (act)
        {
            viewHolder.iv_active.setImageResource(R.drawable.ic_online);
        }
    }

    @Override
    public int getItemCount() {
        if (userData == null) {
            return 0;
        }
        return userData.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return position;
    }


    public class SimpleStringRecyclerViewAdapter extends RecyclerView.ViewHolder {

        public final View mView;


        TextView uname, uStatus;
        ImageView iv_active;

        CircularImageView usrImage;
        CardView userRow;

        public SimpleStringRecyclerViewAdapter(View itemView) {
            super(itemView);
            mView = itemView;
            userRow = itemView.findViewById(R.id.cv_userDetails);
            usrImage = itemView.findViewById(R.id.cv_ProfileImage);
            uname = itemView.findViewById(R.id.tv_ProfileName);
            uStatus = itemView.findViewById(R.id.tv_ProfileStatus);
            iv_active = itemView.findViewById(R.id.iv_active);


            userRow.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {


                    /* ----------------Take Data to userDetails---------------*//*

                    Intent intent = new Intent(c, FriendsDetail.class);
                    intent.putExtra("receiverId", userData.get(getAdapterPosition()).getId());
                    c.startActivity(intent);
                    ((Activity) c).finish();*/

                    Intent intent = new Intent(c, ChatActivity.class);
                    intent.putExtra("receiverId", userData.get(getAdapterPosition()).getId());
                    intent.putExtra("receiverName", userData.get(getAdapterPosition()).getFullname());
                    intent.putExtra("receiverImage", userData.get(getAdapterPosition()).getThumbImage());

                    c.startActivity(intent);
                    ((Activity) c).finish();
                }
            });

        }
    }

}



