package com.example.shaya.sgcapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class TabsPagerAdapter extends FragmentPagerAdapter {
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {

        switch(i)
        {
            case 0:
                RequestsFragment requestsFragment=new RequestsFragment();
                return requestsFragment;
            case 1:
                ChatFragment chatFragment=new ChatFragment();
                return chatFragment;
            case 2:
                GroupsFragment groupsFragment =new GroupsFragment();
                return groupsFragment;
            case 3:
                ContactsFragment contactsFragment =new ContactsFragment();
                return contactsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    public CharSequence getPageTitle(int i)
    {
        switch(i)
        {
            case 0:
                return "Requests";
            case 1:
                return "Chats";
            case 2:
                return "Groups";
            case 3:
                return "Contacts";
            default:
                return null;
        }
    }
}
