package com.example.shaya.sgcapp.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.shaya.sgcapp.R;
import com.example.shaya.sgcapp.domain.modelClasses.Groups;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupAdapter extends BaseAdapter {

    private ArrayList<Groups> mList;
    private Context mContext;

    public GroupAdapter(ArrayList<Groups> mList, Context mContext) {
        this.mList = mList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = View.inflate(mContext, R.layout.group_display_layout,null);

        TextView name = view.findViewById(R.id.group_display_layout_name);
        TextView members = view.findViewById(R.id.group_display_layout_members);
        CircleImageView image = view.findViewById(R.id.groups_display_layout_image);

        name.setText(mList.get(position).getName());
        members.setText(mList.get(position).getTotal_Members());
        Picasso.get().load(mList.get(position).getGroup_Pic()).into(image);

        return view;
    }
}
