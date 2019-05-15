package com.example.shaya.sgcapp.UI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shaya.sgcapp.LocalDatabaseHelper;
import com.example.shaya.sgcapp.domain.modelClasses.Users;
import com.example.shaya.sgcapp.GroupsConfig;
import com.example.shaya.sgcapp.GroupChat;
import com.example.shaya.sgcapp.R;
import com.example.shaya.sgcapp.adapters.UserAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GroupSettings extends AppCompatActivity {

    private String groupId;
    private ImageView group_pic;
    private EditText group_name;
    private Button group_btn, submit;
    private ListView groupMembersDisplayList;
    private ArrayList<Users> groupUsers = new ArrayList<>();
    private UserAdapter adapter;
    private DatabaseReference groupRef, userRef, groupUserRef, rootRef;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ProgressDialog loadingBar;
    private Menu group_settings_menu;
    private GroupsConfig config;

    private LocalDatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        group_pic = findViewById(R.id.group_settings_image);
        group_name = findViewById(R.id.group_settings_name);
        group_btn = findViewById(R.id.group_settings_btn);
        submit = findViewById(R.id.group_settings_btn_submit);

        groupMembersDisplayList = findViewById(R.id.group_settings_members_display);

        rootRef = FirebaseDatabase.getInstance().getReference();
        groupRef = FirebaseDatabase.getInstance().getReference().child("groups");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        groupUserRef = FirebaseDatabase.getInstance().getReference().child("group-users");
        storageRef = FirebaseStorage.getInstance().getReference().child("group_profile_images");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        loadingBar = new ProgressDialog(this);
        config = new GroupsConfig();

        db = new LocalDatabaseHelper(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  //for adding menu on the activity

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_settings_menu, menu);

        group_settings_menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();

        if(id==R.id.group_settings_add_members)
        {
            Intent intent = new Intent(this, AddGroupMembers.class);
            intent.putExtra("groupKey", groupId);
            startActivity(intent);
        }
        else if(id==R.id.group_settings_delete_members)
        {
            Intent intent = new Intent(this, DeleteGroupMembers.class);
            intent.putExtra("groupKey", groupId);
            startActivity(intent);
        }
        else if(id==R.id.group_settings_advance)
        {
            Intent intent = new Intent(this, GroupSettingsAdvance.class);
            intent.putExtra("groupKey", groupId);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupUsers = new ArrayList<>();

        groupUserRef.child(groupId).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String groupUserStatus = dataSnapshot.child("groupStatus").getValue().toString();

                if(groupUserStatus.equals("admin"))
                {
                    group_name.setEnabled(true);
                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            changeGroupName();
                        }
                    });


                    group_pic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pickImage();
                        }
                    });
                    group_btn.setText("Delete Group");
                    group_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            loadingBar.setTitle("Deleting Group");
                            loadingBar.setMessage("Please wait while we remove residue components");
                            loadingBar.show();

                            config.deleteGroup(groupId,GroupSettings.this);
                            Intent intent = new Intent(GroupSettings.this, Main2Activity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                    if(group_settings_menu!=null)
                    {
                        group_settings_menu.findItem(R.id.group_settings_add_members).setVisible(true);
                        group_settings_menu.findItem(R.id.group_settings_delete_members).setVisible(true);
                        group_settings_menu.findItem(R.id.group_settings_advance).setVisible(true);
                    }
                }
                else if(groupUserStatus.equals("member"))
                {
                    submit.setVisibility(View.GONE);
                    group_name.setEnabled(false);
                    group_btn.setText("Leave Group");
                    group_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            config.leaveGroup(groupId,currentUserId,GroupSettings.this);
                            Intent intent = new Intent(GroupSettings.this, Main2Activity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        groupRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("Name").getValue().toString();
                String pic = dataSnapshot.child("Group_Pic").getValue().toString();

                group_name.setText(name);

                Picasso.get().load(pic).into(group_pic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        groupUserRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    for(DataSnapshot d: dataSnapshot.getChildren())
                    {
                        final String user = d.getKey();
                        final String userStatus = d.child("groupStatus").getValue().toString();

                        userRef.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists())
                                {
                                    String userName = dataSnapshot.child("Name").getValue().toString();
                                    String userProfile = dataSnapshot.child("Profile_Pic").getValue().toString();

                                    Users data = new Users();
                                    data.setName(userName);
                                    data.setProfile_Pic(userProfile);
                                    data.setStatus(userStatus);
                                    data.setUserId(user);

                                    groupUsers.add(data);
                                    dataDisplay();
                                }
                                else
                                {
                                    groupUsers = new ArrayList<>();
                                    dataDisplay();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
                else
                {
                    groupUsers = new ArrayList<>();
                    dataDisplay();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        rootRef.child("groups").child(groupId).child("Security").child("key").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    String GK = "";
                    final String keyVersion = dataSnapshot.getValue().toString();

                    Cursor res = db.getData(groupId,keyVersion);
                    if(res.getCount() == 0)
                    {
                        Toast.makeText(GroupSettings.this, "No Data Found", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()) {
                            //buffer.append("Key Value : " + res.getString(3) + "\n");
                            String grp = res.getString(1);
                            if(grp.equals(groupId))
                            {
                                String ver = res.getString(2);
                                if(ver.equals(keyVersion))
                                {
                                    GK = res.getString(3);
                                }
                            }
                        }
                        Toast.makeText(GroupSettings.this, ""+GK, Toast.LENGTH_LONG).show();
                    }
                }
                /*rootRef.child("groups").child(groupId).child("Security").child("keyVersions").child(keyVersion).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        GK = dataSnapshot.getValue().toString();
                        GKview.setText(GK);
                        //db.insertData(keyVersion,GK);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void dataDisplay(){

        adapter = new UserAdapter(groupUsers,this,false,false);
        groupMembersDisplayList.setAdapter(adapter);

        groupMembersDisplayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Users data = (Users) parent.getItemAtPosition(position);

                Intent intent = new Intent(GroupSettings.this, UsersProfileActivity.class);
                intent.putExtra("visit_user_id",data.getUserId());
                startActivity(intent);
            }
        });
    }

    public void pickImage()
    {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK && data != null)
        {
            loadingBar.setTitle("Uploading Image");
            loadingBar.setMessage("Please wait while your image is uploading");
            loadingBar.show();

            final Uri imageUri = data.getData();

            /*String downloadUrl = config.changeGroupImage(groupId, imageUri);
            Toast.makeText(this, ""+downloadUrl, Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();*/
            final StorageReference filePath = storageRef.child(groupId+".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful())
                    {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl = uri.toString();
                                groupRef.child(groupId).child("Group_Pic").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(!task.isSuccessful())
                                                {
                                                    Toast.makeText(GroupSettings.this, "Error uploading pic. Check internet", Toast.LENGTH_SHORT).show();
                                                }
                                                loadingBar.dismiss();
                                                Picasso.get().load(downloadUrl).into(group_pic);
                                            }
                                        });
                            }
                        });
                    }
                    else
                    {
                        loadingBar.dismiss();
                        Toast.makeText(GroupSettings.this, "Error uploading pic. Check internet", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void changeGroupName()
    {
        final String groupNameChanged = group_name.getText().toString();
        boolean validateGroupName = config.changeGroupName(groupNameChanged,groupId);
        if(validateGroupName)
        {
            group_name.setText(groupNameChanged);
            Toast.makeText(GroupSettings.this, "Group Name updated successfully", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(GroupSettings.this, "Please enter a valid group name", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, GroupChat.class);
        intent.putExtra("group_id", groupId);
        startActivity(intent);
        finish();
    }
}

