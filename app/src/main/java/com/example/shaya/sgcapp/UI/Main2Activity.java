package com.example.shaya.sgcapp.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shaya.sgcapp.TechnicalServices.Authentication.PhoneSignIn;
import com.example.shaya.sgcapp.UI.GroupsPackage.GroupMemberSelection;
import com.example.shaya.sgcapp.R;
import com.example.shaya.sgcapp.Domain.SharedPreferences.SharedPreferencesConfig;
import com.example.shaya.sgcapp.Domain.Validation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Main2Activity extends AppCompatActivity {

    private TabLayout tLayout;
    private ViewPager vPager;

    private TabsPagerAdapter tabsPagerAdapter;
    private DatabaseReference ref;

    private FirebaseAuth mAuth;
    private String currentUserId;

    private Validation validation;

    private SharedPreferencesConfig sp;
    private String defaultGroupPicUrl = "https://firebasestorage.googleapis.com/v0/b/sgcapp-8dcbb.appspot.com/o/group_messages_images%2FGroup-icon.png?alt=media&token=3ef7955e-783f-4a71-9224-bba311438fc3";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ref = FirebaseDatabase.getInstance().getReference();
        tLayout = findViewById(R.id.main_tabs);
        vPager = findViewById(R.id.main_tabs_pager);

        tabsPagerAdapter=new TabsPagerAdapter(getSupportFragmentManager());
        vPager.setAdapter(tabsPagerAdapter);
        tLayout.setupWithViewPager(vPager);

        sp = new SharedPreferencesConfig(this);

        mAuth = FirebaseAuth.getInstance();

        validation = new Validation();
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateUserState("online");
    }

    @Override
    protected void onStop() {
        super.onStop();

        updateUserState("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        updateUserState("offline");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  //for adding menu on the activity

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main2_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {  //for doing something when an item is clicked

        int id=item.getItemId();

        if(id==R.id.menu_account)
        {
            startActivity(new Intent(this,SetProfile.class));
        }
        if(id==R.id.menu_settings)
        {
            startActivity(new Intent(this,Settings.class));
        }
        if(id==R.id.menu_find_freinds)
        {
            startActivity(new Intent(this,AddContact.class));
        }
        if(id==R.id.menu_create_group)
        {
            requestNewGroup();
        }
        if(id==R.id.menu_logOut)
        {
            updateUserState("offline");
            sp.writeLoginStatus(false);

            Intent intent = new Intent(this, PhoneSignIn.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

            /*Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);*/
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestNewGroup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name");

        final EditText group = new EditText(this);
        group.setHint("eg : School Friends");
        builder.setView(group);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String groupName = group.getText().toString();
                boolean validateGroupName = validation.validateGroupName(groupName);

                if(validateGroupName)
                {
                    if(TextUtils.isEmpty(groupName))
                    {
                        Toast.makeText(Main2Activity.this, "Please Enter A Group Name ....", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        createNewGroup(groupName);
                    }
                }
                else
                {
                    Toast.makeText(Main2Activity.this, "Please enter a valid group name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        
        builder.show();
    }

    private void createNewGroup(final String groupName) {

        final String user = mAuth.getCurrentUser().getUid();

        DatabaseReference groupIdKeyRef = ref.child("groups").push();
        final String groupKey = groupIdKeyRef.getKey();

        ref.child("groups").child(groupKey).child("Name").setValue(groupName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    //Toast.makeText(Main2Activity.this, groupKey+" is created Successfully", Toast.LENGTH_SHORT).show();
                    ref.child("group-users").child(groupKey).child(user).child("groupStatus").setValue("admin")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    ref.child("users").child(user).child("user-groups").child(groupKey).setValue("true");
                                    ref.child("groups").child(groupKey).child("Total_Members").setValue(1);
                                    ref.child("groups").child(groupKey).child("Security").child("Algo").setValue("");
                                    ref.child("groups").child(groupKey).child("Security").child("key").setValue("");
                                    ref.child("groups").child(groupKey).child("Group_Pic").setValue(defaultGroupPicUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                DatabaseReference userMessageKeyRef = ref.child("group-messages").child(groupKey).push();
                                                String msgPushId = userMessageKeyRef.getKey();

                                                ref.child("group-messages").child(groupKey).child(msgPushId).child("from").setValue(currentUserId);
                                                ref.child("group-messages").child(groupKey).child(msgPushId).child("message").setValue("I am group leader");
                                                ref.child("group-messages").child(groupKey).child(msgPushId).child("msgKey").setValue(msgPushId);
                                                ref.child("group-messages").child(groupKey).child(msgPushId).child("type").setValue("text");

                                                Intent intent = new Intent(Main2Activity.this,GroupMemberSelection.class);
                                                intent.putExtra("groupKey",groupKey);
                                                startActivity(intent);
                                            }
                                            else
                                            {
                                                Toast.makeText(Main2Activity.this, "Group Not Created. Check internet", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                }
                            });
                }
                else
                {
                    Toast.makeText(Main2Activity.this, "Group not created. Check Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void updateUserState(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calender = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd yyyy");
        saveCurrentDate = currentDate.format(calender.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calender.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserId = mAuth.getCurrentUser().getUid();

        ref.child("users").child(currentUserId).child("user-state")
                .updateChildren(onlineStateMap);
    }
}
