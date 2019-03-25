package com.example.shaya.sgcapp.UI;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shaya.sgcapp.DatabaseHelper;
import com.example.shaya.sgcapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersProfileActivity extends AppCompatActivity {

    private String receiverUserId;
    private String current_state;
    private String currentUserId;

    private CircleImageView imageView;
    private TextView name;
    private TextView status;
    private Button sendMsg;
    private Button declineMsg;

    DatabaseReference ref, chatRequestRef, contactsRef, notificatonsRef;
    FirebaseAuth mAuth;
    DatabaseHelper helper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);

        mAuth = FirebaseAuth.getInstance();

        imageView = findViewById(R.id.visit_profile_image);
        name = findViewById(R.id.visit_profile_name);
        status = findViewById(R.id.visit_profile_status);
        sendMsg = findViewById(R.id.visit_profile_btn);
        declineMsg = findViewById(R.id.visit_profile_btn_decline);

        Intent intent = getIntent();
        receiverUserId = intent.getStringExtra("visit_user_id");

        ref = FirebaseDatabase.getInstance().getReference().child("users");
        current_state="new";
        currentUserId = mAuth.getCurrentUser().getUid();
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("chat req");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");
        notificatonsRef = FirebaseDatabase.getInstance().getReference().child("notifications");

        helper = new DatabaseHelper();

        //retrieveUserInfo();

    }

    @Override
    protected void onStart() {
        super.onStart();

        current_state = "new";
        retrieveUserInfo();
    }

    public void retrieveUserInfo()
    {
        ref.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String n = dataSnapshot.child("Name").getValue().toString();
                String s = dataSnapshot.child("Status").getValue().toString();
                String i = dataSnapshot.child("Profile_Pic").getValue().toString();

                name.setText(n);
                status.setText(s);
                Picasso.get().load(i).into(imageView);

                manageChatRequests();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void manageChatRequests()
    {
        chatRequestRef.child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if(dataSnapshot.hasChild(receiverUserId))
                        {
                            String req_type = dataSnapshot.child(receiverUserId).child("req_type").getValue().toString();

                            if(req_type.equals("sent"))
                            {
                                current_state = "request_sent";
                                sendMsg.setText("Cancel Request");
                            }
                            else if(req_type.equals("received"))
                            {
                                current_state = "request_received";
                                sendMsg.setText("Accept Request");

                                declineMsg.setVisibility(View.VISIBLE);
                                declineMsg.setEnabled(true);

                                declineMsg.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            contactsRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(receiverUserId))
                                    {
                                        current_state = "friends";
                                        sendMsg.setText("Remove Contact");
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

        if(!currentUserId.equals(receiverUserId))
        {
            sendMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMsg.setEnabled(false);

                    if(current_state.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if(current_state.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }
                    if(current_state.equals("request_received"))
                    {
                        acceptChatRequest();
                    }
                    if(current_state.equals("friends"))
                    {
                        removeContact();
                    }
                }
            });
        }
        else
        {
            sendMsg.setVisibility(View.INVISIBLE);
        }
    }

    private void removeContact() {

        /*contactsRef.child(currentUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            contactsRef.child(receiverUserId).child(currentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                sendMsg.setEnabled(true);
                                                current_state = "new";
                                                sendMsg.setText("Send Request");

                                                declineMsg.setVisibility(View.INVISIBLE);
                                                declineMsg.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });*/

        helper.removeContact(currentUserId,receiverUserId);
        sendMsg.setEnabled(true);
        current_state = "new";
        sendMsg.setText("Send Request");

        declineMsg.setVisibility(View.INVISIBLE);
        declineMsg.setEnabled(false);

    }

    private void acceptChatRequest() {

        contactsRef.child(currentUserId).child(receiverUserId).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {

                            contactsRef.child(receiverUserId).child(currentUserId).child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                chatRequestRef.child(currentUserId).child(receiverUserId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful())
                                                        {
                                                            chatRequestRef.child(receiverUserId).child(currentUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    sendMsg.setEnabled(true);
                                                                    current_state = "friends";
                                                                    sendMsg.setText("Remove Contact");
                                                                    declineMsg.setVisibility(View.INVISIBLE);
                                                                }
                                                            });
                                                        }

                                                    }
                                                });
                                            }
                                        }
                                    });

                        }
                    }
                });

        /*helper.acceptChatRequest(currentUserId,receiverUserId);
        sendMsg.setEnabled(true);
        current_state = "friends";
        sendMsg.setText("Remove Contact");
        declineMsg.setVisibility(View.INVISIBLE);*/
    }

    private void cancelChatRequest() {

        /*chatRequestRef.child(currentUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserId).child(currentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                sendMsg.setEnabled(true);
                                                current_state = "new";
                                                sendMsg.setText("Send Request");

                                                declineMsg.setVisibility(View.INVISIBLE);
                                                declineMsg.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });*/

        helper.cancelChatRequest(currentUserId,receiverUserId);
        sendMsg.setEnabled(true);
        current_state = "new";
        sendMsg.setText("Send Request");

        declineMsg.setVisibility(View.INVISIBLE);
        declineMsg.setEnabled(false);

    }

    private void sendChatRequest() {

        chatRequestRef.child(currentUserId).child(receiverUserId).child("req_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserId).child(currentUserId).child("req_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {

                                                sendMsg.setEnabled(true);
                                                current_state = "request_sent";
                                                sendMsg.setText("Cancel request");
                                            }

                                        }
                                    });
                        }

                    }
                });

        /*helper.sendChatRequest(currentUserId,receiverUserId);
        sendMsg.setEnabled(true);
        current_state = "request_sent";
        sendMsg.setText("Cancel request");*/
    }
}