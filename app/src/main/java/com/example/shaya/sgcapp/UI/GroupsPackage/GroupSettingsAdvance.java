package com.example.shaya.sgcapp.UI.GroupsPackage;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.example.shaya.sgcapp.UI.Main2Activity;
import com.example.shaya.sgcapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.security.MessageDigest;

public class GroupSettingsAdvance extends AppCompatActivity {

    private String groupId;
    private Spinner spinner;
    private EditText encryption_key_editText;
    private TextView algoDisplay;
    private Button set_key;
    private DatabaseReference rootRef;
    private String GK;
    private TextView GKview;
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
                            builder.setTitle("Warning, All previous messages will be deleted");

                            builder.setPositiveButton("Change Key", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String encryptionKey = encryption_key_editText.getText().toString();

                                    if(!encryptionKey.equals(""))
                                    {
                                        rootRef.child("groups").child(groupId).child("Security").child("Algo").setValue(algo);
                                        rootRef.child("groups").child(groupId).child("Security").child("keyVersions").removeValue();
                                        rootRef.child("group-messages").child(groupId).removeValue();

                                        rootRef.child("groups").child(groupId).child("Security").child("keyVersions").child("v").setValue(encryptionKey);
                                        rootRef.child("groups").child(groupId).child("Security").child("key").setValue("v");

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

                String keyVersion = dataSnapshot.getValue().toString();

                rootRef.child("groups").child(groupId).child("Security").child("keyVersions").child(keyVersion).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        GK = dataSnapshot.getValue().toString();
                        GKview.setText(GK);
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
    }

}
