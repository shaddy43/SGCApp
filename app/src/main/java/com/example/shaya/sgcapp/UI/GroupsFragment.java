package com.example.shaya.sgcapp.UI;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.shaya.sgcapp.adapters.GroupAdapter;
import com.example.shaya.sgcapp.GroupChat;
import com.example.shaya.sgcapp.domain.modelClasses.Groups;
import com.example.shaya.sgcapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View view;

    private ArrayList<Groups> userGroups = new ArrayList<>();
    private GroupAdapter adapter;
    private ListView groupList;

    private DatabaseReference groupRef;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private SwipeRefreshLayout refreshLayout;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_groups, container, false);

        refreshLayout = view.findViewById(R.id.group_fragment_swipe_refresh);
        groupList = view. findViewById(R.id.groups_display_list);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        groupRef = FirebaseDatabase.getInstance().getReference().child("groups");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onStart();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        userGroups = new ArrayList<>();

        userRef.child(currentUserId).child("user-groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChildren())
                    {
                        for(DataSnapshot d: dataSnapshot.getChildren())
                        {
                            final String groupId = d.getKey();

                            groupRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    Groups data = new Groups();
                                    String name = dataSnapshot.child("Name").getValue().toString();
                                    String members = dataSnapshot.child("Total_Members").getValue().toString();
                                    String group_pic = dataSnapshot.child("Group_Pic").getValue().toString();

                                    data.setName(name);
                                    data.setTotal_Members(members);
                                    data.setGroup_Pic(group_pic);
                                    data.setGroupKey(groupId);

                                    userGroups.add(data);
                                    displayList();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                    else
                    {
                        refreshLayout.setRefreshing(false);
                        userGroups = new ArrayList<>();
                        displayList();
                    }
                }
                else
                {
                    refreshLayout.setRefreshing(false);
                    userGroups = new ArrayList<>();
                    displayList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayList() {

        refreshLayout.setRefreshing(false);
        adapter = new GroupAdapter(userGroups, getContext());
        groupList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Groups data = (Groups) parent.getItemAtPosition(position);
                Intent intent = new Intent(getContext(), GroupChat.class);
                intent.putExtra("group_id", data.getGroupKey());
                startActivity(intent);

            }
        });
    }
}
