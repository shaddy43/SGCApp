package com.example.shaya.sgcapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

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
public class RequestsFragment extends Fragment {


    View view;

    ListView myReqList;

    DatabaseReference reqRef, userRef;
    FirebaseAuth mAuth;
    String currentUserId;

    ArrayList<AllUsers> chatRequests;
    UserAdapter adapter;

    SwipeRefreshLayout refreshLayout;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_requests, container, false);

        refreshLayout = view.findViewById(R.id.request_swipe_refresh);

        myReqList = view.findViewById(R.id.requests_list);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        reqRef = FirebaseDatabase.getInstance().getReference().child("chat req").child(currentUserId);
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

        chatRequests = new ArrayList<>();
        reqRef.orderByChild("req_type").equalTo("received").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChildren())
                    {
                        for(DataSnapshot d : dataSnapshot.getChildren())
                        {
                            String usersId = d.getRef().getKey();
                            userRef.child(usersId).addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    AllUsers data = new AllUsers();
                                    data.setName(dataSnapshot.child("Name").getValue().toString());
                                    data.setStatus(dataSnapshot.child("Status").getValue().toString());
                                    data.setProfile_Pic(dataSnapshot.child("Profile_Pic").getValue().toString());
                                    data.setUserId(dataSnapshot.getRef().getKey());
                                    chatRequests.add(data);
                                    dataDisplay();
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
                        chatRequests = new ArrayList<>();
                        dataDisplay();
                    }
                }
                else
                {
                    refreshLayout.setRefreshing(false);
                    chatRequests = new ArrayList<>();
                    dataDisplay();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void dataDisplay()
    {
        adapter = new UserAdapter(chatRequests, getContext(), true, false);
        adapter.notifyDataSetChanged();
        myReqList.setAdapter(adapter);
        refreshLayout.setRefreshing(false);
    }
}
