package com.example.shaya.sgcapp.UI;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shaya.sgcapp.domain.modelClasses.Users;
import com.example.shaya.sgcapp.GroupsConfig;
import com.example.shaya.sgcapp.R;
import com.example.shaya.sgcapp.adapters.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DeleteGroupMembers extends AppCompatActivity {

    private ListView groupMembersList;
    private ArrayList<Users> groupMembers;
    private UserAdapter adapter;
    private ArrayList<String> deleteMembers;
    private int count = 0;
    private String groupId;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String GK = "";
    private GroupsConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_group_members);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        deleteMembers = new ArrayList<>();
        groupId = getIntent().getStringExtra("groupKey");

        groupMembers = new ArrayList<>();
        groupMembersList = findViewById(R.id.delete_group_members_listView);

        rootRef = FirebaseDatabase.getInstance().getReference();
        config = new GroupsConfig();


        rootRef.child("group-users").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren())
                {
                    for(DataSnapshot d : dataSnapshot.getChildren())
                    {
                        final String usersId = d.getKey();

                        if(!usersId.equals(currentUserId))
                        {
                            rootRef.child("users").child(usersId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    Users data = new Users();
                                    data.setName(dataSnapshot.child("Name").getValue().toString());
                                    data.setStatus(dataSnapshot.child("Status").getValue().toString());
                                    data.setProfile_Pic(dataSnapshot.child("Profile_Pic").getValue().toString());
                                    data.setUserId(usersId);
                                    groupMembers.add(data);
                                    dataDisplay();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void dataDisplay() {

        adapter = new UserAdapter(groupMembers, this, false, false);
        groupMembersList.setAdapter(adapter);
        groupMembersList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

            groupMembersList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                    count++;
                    Users data = groupMembers.get(position);
                    deleteMembers.add(data.getUserId());
                    mode.setTitle(count + " items selected");

                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.member_deletion_menu, menu);

                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                    int id = item.getItemId();

                    if (id == R.id.done_selection_for_deletion) {

                        config.deleteGroupMembers(deleteMembers,groupId,DeleteGroupMembers.this);
                        //config.updateGroup(groupId,"");
                        startActivity(new Intent(DeleteGroupMembers.this,Main2Activity.class));
                        Toast.makeText(DeleteGroupMembers.this, "Group Updated Successfully", Toast.LENGTH_SHORT).show();
                        finish();

                        return true;
                    }
                    else
                    {
                        return false;
                    }

                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {

                }
            });

        }
}
