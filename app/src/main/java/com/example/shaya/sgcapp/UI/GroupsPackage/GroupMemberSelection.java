package com.example.shaya.sgcapp.UI.GroupsPackage;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shaya.sgcapp.Domain.ModelClasses.AllUsers;
import com.example.shaya.sgcapp.UI.Main2Activity;
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
import java.util.ArrayList;
import java.util.Random;

public class GroupMemberSelection extends AppCompatActivity {

    private ListView groupSelectionList;

    private DatabaseReference contactsRef, userRef, groupRef, ref;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private ArrayList<AllUsers> groupMembers;
    private UserAdapter adapter;

    private SwipeRefreshLayout refreshLayout;

    private ArrayList<String> members;
    private int count = 0;
    private String groupKey;
    private String GK = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member_selection);

        refreshLayout = findViewById(R.id.group_member_selection_swipe_refresher);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        groupSelectionList = findViewById(R.id.member_selection_list);

        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts").child(currentUserId);
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        groupRef = FirebaseDatabase.getInstance().getReference().child("group-users");
        ref = FirebaseDatabase.getInstance().getReference();

        members = new ArrayList<>();
        groupKey = getIntent().getStringExtra("groupKey");

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onStart();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupMembers = new ArrayList<>();

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

                                AllUsers data = new AllUsers();
                                data.setName(dataSnapshot.child("Name").getValue().toString());
                                data.setStatus(dataSnapshot.child("Status").getValue().toString());
                                data.setProfile_Pic(dataSnapshot.child("Profile_Pic").getValue().toString());
                                data.setOnlineState(dataSnapshot.child("user-state").child("state").getValue().toString());
                                data.setUserId(dataSnapshot.getRef().getKey());
                                groupMembers.add(data);

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
        adapter = new UserAdapter(groupMembers, this, false, false);
        adapter.notifyDataSetChanged();
        groupSelectionList.setAdapter(adapter);
        groupSelectionList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        refreshLayout.setRefreshing(false);

        groupSelectionList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                count++;
                AllUsers data = groupMembers.get(position);
                members.add(data.getUserId());
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
                    for(int i=0;i<members.size();i++)
                    {
                        groupRef.child(groupKey).child(members.get(i)).child("groupStatus").setValue("member");
                        ref.child("users").child(members.get(i)).child("user-groups").child(groupKey).setValue("true");

                        /*DatabaseReference userMessageKeyRef = ref.child("group-messages").child(groupKey).push();
                        String msgPushId = userMessageKeyRef.getKey();

                        ref.child("group-messages").child(groupKey).child(msgPushId).child("from").setValue(members.get(i));
                        ref.child("group-messages").child(groupKey).child(msgPushId).child("message").setValue("I am a member");
                        ref.child("group-messages").child(groupKey).child(msgPushId).child("msgKey").setValue(msgPushId);
                        ref.child("group-messages").child(groupKey).child(msgPushId).child("type").setValue("text");*/

                        String hashValue = "";
                        try {
                            hashValue = generateRandom();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        ref.child("groups").child(groupKey).child("Security").child("GKgeneration").child(members.get(i)).setValue(hashValue);
                    }

                    ref.child("groups").child(groupKey).child("Total_Members").setValue(members.size()+1);


                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupMemberSelection.this, R.style.AlertDialog);
                    builder.setTitle("Enter Group Key");

                    final EditText group = new EditText(GroupMemberSelection.this);
                    group.setHint("enter key here");
                    builder.setView(group);

                    builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        String gK = group.getText().toString();
                        if(gK.isEmpty())
                        {
                            Toast.makeText(GroupMemberSelection.this, "Please enter key", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            //ref.child("groups").child(groupKey).child("Security").child("key").setValue(gK);
                            //ref.child("groups").child(groupKey).child("Security").child("keyVersions").child("v").setValue(gK);
                            //ref.child("groups").child(groupKey).child("Security").child("GKgeneration").child(currentUserId).setValue(gK);

                            //generateGroupKey();
                            ref.child("groups").child(groupKey).child("Security").child("keyVersions").child("v").setValue(gK);
                            ref.child("groups").child(groupKey).child("Security").child("key").setValue("v");

                            startActivity(new Intent(GroupMemberSelection.this,Main2Activity.class));
                            finish();
                            Toast.makeText(GroupMemberSelection.this, "Group Created Successfully", Toast.LENGTH_SHORT).show();

                        }

                        }
                    });

                    builder.show();
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

    private void generateGroupKey() {

        ref.child("groups").child(groupKey).child("Security").child("GKgeneration").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    final String leaderKey = dataSnapshot.getValue().toString();

                    ref.child("groups").child(groupKey).child("Security").child("GKgeneration").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists())
                            {
                                String groupKey = leaderKey;
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private String generateRandom() throws Exception {

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
