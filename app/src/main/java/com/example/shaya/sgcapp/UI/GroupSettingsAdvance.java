package com.example.shaya.sgcapp.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shaya.sgcapp.GroupsConfig;
import com.example.shaya.sgcapp.LocalDatabaseHelper;
import com.example.shaya.sgcapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupSettingsAdvance extends AppCompatActivity {

    private String groupId;
    private Spinner spinner;
    private EditText encryption_key_editText;
    private TextView algoDisplay;
    private Button set_key;
    private DatabaseReference rootRef;
    private String GK;
    private TextView GKview;
    private GroupsConfig config;
    private LocalDatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings_advance);

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupKey");

        encryption_key_editText = findViewById(R.id.write_encryption_key);
        set_key = findViewById(R.id.set_key_btn);
        algoDisplay = findViewById(R.id.encryptionAlgo);
        GKview = findViewById(R.id.GKView);

        rootRef = FirebaseDatabase.getInstance().getReference();
        spinner = findViewById(R.id.algo_spinner);

        config = new GroupsConfig();
        db = new LocalDatabaseHelper(this);

        /*Cursor res = db.getData(groupId,"v");
        if(res.getCount() == 0)
        {
            Toast.makeText(this, "No data found", Toast.LENGTH_LONG).show();
        }
        else
        {
            StringBuffer buffer = new StringBuffer();
            while(res.moveToNext())
            {
                buffer.append("Key Value : "+res.getString(3)+"\n");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Show");
            builder.setMessage(buffer.toString());
            builder.show();
        }*/

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.algorithms_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        //spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                final String algo = parent.getItemAtPosition(position).toString();

                if(algo.equals("AES"))
                {
                    encryption_key_editText.setVisibility(View.VISIBLE);
                    set_key.setVisibility(view.VISIBLE);

                    set_key.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(GroupSettingsAdvance.this, R.style.AlertDialog);
                            builder.setTitle("Are you sure you want to change group key?");

                            builder.setPositiveButton("Change Key", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String encryptionKey = encryption_key_editText.getText().toString();

                                    if(!encryptionKey.equals(""))
                                    {
                                        config.changeEncryptionKey(groupId, algo, encryptionKey, GroupSettingsAdvance.this);
                                        /*rootRef.child("groups").child(groupId).child("Security").child("Algo").setValue(algo);
                                        rootRef.child("groups").child(groupId).child("Security").child("keyVersions").removeValue();
                                        rootRef.child("group-messages").child(groupId).removeValue();

                                        rootRef.child("groups").child(groupId).child("Security").child("keyVersions").child("v").setValue(encryptionKey);
                                        rootRef.child("groups").child(groupId).child("Security").child("key").setValue("v");*/

                                        startActivity(new Intent(GroupSettingsAdvance.this, Main2Activity.class));
                                        Toast.makeText(GroupSettingsAdvance.this, "Group settings updated successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                    else
                                    {
                                        Toast.makeText(GroupSettingsAdvance.this, "Please write key first", Toast.LENGTH_SHORT).show();
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
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        rootRef.child("groups").child(groupId).child("Security").child("key").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    final String keyVersion = dataSnapshot.getValue().toString();

                    Cursor res = db.getData(groupId,keyVersion);
                    if(res.getCount() == 0)
                    {
                        Toast.makeText(GroupSettingsAdvance.this, "No Data Found", Toast.LENGTH_SHORT).show();
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
                        GKview.setText(GK);
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

}
