package com.example.shaya.sgcapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shaya.sgcapp.adapters.PrivateMessageAdapter;
import com.example.shaya.sgcapp.domain.modelClasses.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends AppCompatActivity {

    private String messageReceiverName;
    private String messageReceiverId;
    private String messageReceiverImage;
    private String messageReceiverState;
    private String messageSenderId;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private ImageButton sendMsg;
    private EditText msgInput;
    private ImageButton selectImageButton;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference firebaseStorage;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    //private MessageAdapter messageAdapter;
    private PrivateMessageAdapter privateMessageAdapter;
    private RecyclerView userMessageList;

    private ProgressDialog loadingBar;

    //private Security aes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getSupportActionBar().hide();

        Intent intent = getIntent();
        messageReceiverName = intent.getStringExtra("visit_user_name");
        messageReceiverId = intent.getStringExtra("visit_user_id");
        messageReceiverState = intent.getStringExtra("visit_user_state");
        messageReceiverImage = intent.getStringExtra("visit_user_image");

        initializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).into(userImage);
        userLastSeen.setText(messageReceiverState);

        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImage();
            }
        });
    }

    private void initializeControllers() {

        ActionBar actionBar = getSupportActionBar();
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = findViewById(R.id.custom_chat_bar_name);
        userLastSeen = findViewById(R.id.custom_chat_bar_status);
        userImage = findViewById(R.id.custom_chat_bar_image);
        sendMsg = findViewById(R.id.private_chat_send_message_btn);
        msgInput = findViewById(R.id.private_chat_input_messages);
        selectImageButton = findViewById(R.id.private_chat_send_image);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance().getReference().child("private_messages_images").child(messageSenderId).child(messageReceiverId);

        privateMessageAdapter = new PrivateMessageAdapter(messagesList);
        userMessageList = findViewById(R.id.private_chat_messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(privateMessageAdapter);

        loadingBar = new ProgressDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        displayLaseSeen();

        rootRef.child("chats").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        privateMessageAdapter.notifyDataSetChanged();
                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage()
    {
        String messageText = msgInput.getText().toString();
        msgInput.setText("");
        /*aes = new Security();
        String encryptedVal = "";
        try
        {
            encryptedVal = aes.encrypt(messageText, messageSenderId);

        }catch (Exception e)
        {
            Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
        }*/

        if(!messageText.isEmpty())
        {
            String messageSenderRef = "chats/" + messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef = "chats/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("chats")
                    .child(messageSenderId).child(messageReceiverId).push();

            String msgPushId = userMessageKeyRef.getKey();

            Map msgTextBody = new HashMap();
            msgTextBody.put("message", messageText);
            msgTextBody.put("type", "text");
            msgTextBody.put("from", messageSenderId);
            //msgTextBody.put("msgKey", msgPushId);

            Map msgBodyDetails = new HashMap();
            msgBodyDetails.put(messageSenderRef + "/" + msgPushId, msgTextBody);
            msgBodyDetails.put(messageReceiverRef + "/" + msgPushId, msgTextBody);

            rootRef.updateChildren(msgBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(!task.isSuccessful())
                    {
                        Toast.makeText(Chat.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void displayLaseSeen()
    {
        rootRef.child("users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild("user-state"))
                    {
                        String state = dataSnapshot.child("user-state").child("state").getValue().toString();
                        String date = dataSnapshot.child("user-state").child("date").getValue().toString();
                        String time = dataSnapshot.child("user-state").child("time").getValue().toString();

                        if(state.equals("online"))
                        {
                            userLastSeen.setText("online");
                        }
                        else if(state.equals("offline"))
                        {
                            userLastSeen.setText("Last Seen: " + date + ", "+time);
                        }
                    }
                    else
                    {
                        userLastSeen.setText("offline");
                    }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        displayLaseSeen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        displayLaseSeen();
    }

    public void sendImage()
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

            final String messageSenderRef = "chats/" + messageSenderId + "/" + messageReceiverId;
            final String messageReceiverRef = "chats/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("chats")
                    .child(messageSenderId).child(messageReceiverId).push();

            final String msgPushId = userMessageKeyRef.getKey();

            final StorageReference filePath = firebaseStorage.child(msgPushId+".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();

                                Map msgTextBody = new HashMap();
                                msgTextBody.put("message", downloadUrl);
                                msgTextBody.put("type", "image");
                                msgTextBody.put("from", messageSenderId);
                                //msgTextBody.put("msgKey", msgPushId);

                                Map msgBodyDetails = new HashMap();
                                msgBodyDetails.put(messageSenderRef + "/" + msgPushId, msgTextBody);
                                msgBodyDetails.put(messageReceiverRef + "/" + msgPushId, msgTextBody);

                                rootRef.updateChildren(msgBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {

                                        if(!task.isSuccessful())
                                        {
                                            Toast.makeText(Chat.this, "Error sending image", Toast.LENGTH_SHORT).show();
                                        }
                                        msgInput.setText("");
                                        loadingBar.dismiss();
                                        finish();
                                        startActivity(getIntent());
                                    }
                                });
                            }
                        });

                    }
                    else
                    {
                        Toast.makeText(Chat.this, "Picture not sent. Please try again", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }

                }
            });
        }
    }
}
