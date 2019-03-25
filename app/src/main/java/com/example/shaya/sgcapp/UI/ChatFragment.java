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

import com.example.shaya.sgcapp.Chat;
import com.example.shaya.sgcapp.domain.modelClasses.Users;
import com.example.shaya.sgcapp.R;
import com.example.shaya.sgcapp.adapters.UserAdapter;
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
public class ChatFragment extends Fragment {

    private View view;
    private ListView chatUsersListView;
    private ArrayList<Users> chatUsersList;
    private UserAdapter adapter;
    private DatabaseReference userRef, contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private SwipeRefreshLayout refreshLayout;

    //private DatabaseHelper helper;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);

        chatUsersListView = view.findViewById(R.id.chat_fragment_list);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts").child(currentUserId);
        refreshLayout = view.findViewById(R.id.chat_fragment_swipe_refresh);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onStart();
            }
        });

        //helper = new DatabaseHelper();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        chatUsersList = new ArrayList<>();

        contactsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren())
                {
                    for(DataSnapshot d : dataSnapshot.getChildren())
                    {
                        String usersId = d.getRef().getKey();

                        userRef.child(usersId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String date, time, state;
                                Users data = new Users();

                                if(dataSnapshot.hasChild("user-state"))
                                {
                                    state = dataSnapshot.child("user-state").child("state").getValue().toString();
                                    date = dataSnapshot.child("user-state").child("date").getValue().toString();
                                    time = dataSnapshot.child("user-state").child("time").getValue().toString();

                                    data.setName(dataSnapshot.child("Name").getValue().toString());
                                    data.setProfile_Pic(dataSnapshot.child("Profile_Pic").getValue().toString());
                                    data.setUserId(dataSnapshot.getRef().getKey());

                                    if(state.equals("online"))
                                    {
                                        data.setStatus("online");
                                    }
                                    else if(state.equals("offline"))
                                    {
                                        data.setStatus("Last Seen: " + date + ", "+time);
                                    }

                                    chatUsersList.add(data);
                                }
                                else
                                {
                                    data.setStatus("offline");
                                    data.setName(dataSnapshot.child("Name").getValue().toString());
                                    data.setProfile_Pic(dataSnapshot.child("Profile_Pic").getValue().toString());
                                    data.setUserId(dataSnapshot.getRef().getKey());
                                    chatUsersList.add(data);
                                }

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
                    //Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_SHORT).show();
                    refreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void dataDisplay() {

        adapter = new UserAdapter(chatUsersList, getContext(), false, false);
        adapter.notifyDataSetChanged();
        chatUsersListView.setAdapter(adapter);

        refreshLayout.setRefreshing(false);

        chatUsersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Users data = (Users) parent.getItemAtPosition(position);

                Intent intent = new Intent(getContext(), Chat.class);
                intent.putExtra("visit_user_id", data.getUserId());
                intent.putExtra("visit_user_state", data.getStatus());
                intent.putExtra("visit_user_name", data.getName());
                intent.putExtra("visit_user_image", data.getProfile_Pic());
                startActivity(intent);
            }
        });

    }


}
