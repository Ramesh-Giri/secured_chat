package com.us.ramesh.securechat.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.us.ramesh.securechat.R;
import com.us.ramesh.securechat.all_users.activity.ShowUsers;
import com.us.ramesh.securechat.user_profile.UserFragment;

public class HomeFragment extends Fragment {

    ImageView img_friends, img_messages, img_users, img_profile;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);

        img_friends = view.findViewById(R.id.img_friends);
        img_messages = view.findViewById(R.id.img_messages);
        img_users = view.findViewById(R.id.img_users);
        img_profile = view.findViewById(R.id.img_profile);


        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.container_Dash, new UserFragment()).addToBackStack(null).commitAllowingStateLoss();
            }
        });

        img_users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShowUsers.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
