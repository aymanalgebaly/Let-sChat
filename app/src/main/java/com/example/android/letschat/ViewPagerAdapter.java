package com.example.android.letschat;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.android.letschat.ChatsFragment;
import com.example.android.letschat.FriendsFragment;
import com.example.android.letschat.RequestsFragment;

import com.example.android.letschat.ChatsFragment;
import com.example.android.letschat.FriendsFragment;
import com.example.android.letschat.RequestsFragment;

class ViewPagerAdapter extends FragmentPagerAdapter {

    //Fragment fragments[] = {new RequestsFragment(), new ChatsFragment(), new FriendsFragment()};

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
                default:
                    return null;

        }
        //return fragments[position];
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Requests";
            case 1:
                return "Chats";
            case 2:
                return "Friends";

        }
        return super.getPageTitle(position);
    }
}
