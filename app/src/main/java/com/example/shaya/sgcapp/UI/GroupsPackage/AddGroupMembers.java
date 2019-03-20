package com.example.shaya.sgcapp.UI.GroupsPackage;

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

import com.example.shaya.sgcapp.UI.Main2Activity;
import com.example.shaya.sgcapp.Domain.ModelClasses.AllUsers;
import com.example.shaya.sgcapp.R;
import com.example.shaya.sgcapp.TechnicalServices.Adapters.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

public class AddGroupMembers extends AppCompatActivity {

    private DatabaseReference rootRef;
    private ArrayList<String> selectedMembers;
    private String groupId;

    private ArrayList<AllUsers> groupMembers = new ArrayList<>();
    private UserAdapter adapter;
    private ListView contactsList;

    private FirebaseAuth mAuth;
    private String currentUserId;

    int count = 0;
    private String GK = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_members);

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupKey");

        rootRef = FirebaseDatabase.getInstance().getReference();
        contactsList = findViewById(R.id.add_group_members_listView);
        selectedMembers = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupMembers = new ArrayList<>();

        rootRef.child("contacts").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot d: dataSnapshot.getChildren())
                    {
                        final String id = d.getKey();

                        if(!id.equals(currentUserId))
                        {
                            rootRef.child("users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    AllUsers data = new AllUsers();
                                    data.setName(dataSnapshot.child("Name").getValue().toString());
                                    data.setStatus(dataSnapshot.child("Status").getValue().toString());
                                    data.setUserId(id);
                                    data.setProfile_Pic(dataSnapshot.child("Profile_Pic").getValue().toString());
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
                else
                {
                    groupMembers = new ArrayList<>();
                    dataDisplay();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void dataDisplay() {

        adapter = new UserAdapter(groupMembers, this, false, false);
        contactsList.setAdapter(adapter);

        contactsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        contactsList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                count++;
                AllUsers data = groupMembers.get(position);
                selectedMembers.add(data.getUserId());
                mode.setTitle(count + " items selected");

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.member_selection_menu, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                int id=item.getItemId();

                if(id==R.id.done_selection)
                {
                    for(int i=0;i<selectedMembers.size();i++)
                    {
                        rootRef.child("group-users").child(groupId).child(selectedMembers.get(i)).child("groupStatus").setValue("member");
                        rootRef.child("users").child(selectedMembers.get(i)).child("user-groups").child(groupId).setValue("true");

                        String hashValue = "";
                        try {
                            hashValue = generateRandom();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        rootRef.child("groups").child(groupId).child("Security").child("GKgeneration").child(selectedMembers.get(i)).setValue(hashValue);
                    }

                    rootRef.child("group-users").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            long c = dataSnapshot.getChildrenCount();
                            rootRef.child("groups").child(groupId).child("Total_Members").setValue(c);

                            generateGroupKey();

                            rootRef.child("groups").child(groupId).child("Security").child("keyVersions").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.exists())
                                    {
                                        int count = (int) dataSnapshot.getChildrenCount();
                                        rootRef.child("groups").child(groupId).child("Security").child("keyVersions").child("v"+count).setValue(GK);
                                        rootRef.child("groups").child(groupId).child("Security").child("key").setValue("v"+count);

                                        startActivity(new Intent(AddGroupMembers.this,Main2Activity.class));
                                        Toast.makeText(AddGroupMembers.this, "Group Updated Successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

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

    private String generateRandom() throws NoSuchAlgorithmException {

        Random rand = new Random();
        String randomDigit = String.valueOf(rand.nextInt(100));

        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(randomDigit.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashText = bigInt.toString(8);

        String endResult = String.valueOf(hashText.charAt(0)).concat(String.valueOf(hashText.charAt(1))).concat(String.valueOf(hashText.charAt(2)));
        return endResult;
    }

    private void generateGroupKey() {

        rootRef.child("groups").child(groupId).child("Security").child("GKgeneration").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    String groupKey = "";
                    for(DataSnapshot d : dataSnapshot.getChildren())
                    {
                        groupKey = groupKey.concat(d.getValue().toString());
                    }

                    try {
                        generateKey(groupKey);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void generateKey(String groupKey) throws Exception {

        MessageDigest m = MessageDigest.getInstance("SHA-256");
        m.reset();
        m.update(groupKey.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashText = bigInt.toString(16);
        GK = hashText;
    }
}
