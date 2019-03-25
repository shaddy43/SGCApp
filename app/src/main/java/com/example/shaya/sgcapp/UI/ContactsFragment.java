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
public class ContactsFragment extends Fragment {


    public ContactsFragment() {
        // Required empty public constructor
    }
    private View view;
    private ListView myContactList;

    private DatabaseReference contactsRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private ArrayList<Users> contactsUsers;
    private UserAdapter adapter;

    private SwipeRefreshLayout refreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contacts, container, false);

        refreshLayout = view.findViewById(R.id.contacts_swipe_refresh);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        myContactList = view.findViewById(R.id.contacts_list);

        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts").child(currentUserId);
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

        contactsUsers = new ArrayList<>();

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

                                Users data = new Users();
                                data.setName(dataSnapshot.child("Name").getValue().toString());
                                data.setStatus(dataSnapshot.child("Status").getValue().toString());
                                data.setProfile_Pic(dataSnapshot.child("Profile_Pic").getValue().toString());
                                data.setOnlineState(dataSnapshot.child("user-state").child("state").getValue().toString());
                                data.setUserId(dataSnapshot.getRef().getKey());
                                contactsUsers.add(data);

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

    public void dataDisplay()
    {
        adapter = new UserAdapter(contactsUsers,getContext(), false, true);
        adapter.notifyDataSetChanged();
        myContactList.setAdapter(adapter);

        refreshLayout.setRefreshing(false);

        myContactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Users data = (Users) parent.getItemAtPosition(position);

                Intent intent = new Intent(getContext(),UsersProfileActivity.class);
                intent.putExtra("visit_user_id",data.getUserId());
                startActivity(intent);
            }
        });
    }

}
