package com.example.shaya.sgcapp.UI.GroupsPackage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
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

import com.example.shaya.sgcapp.TechnicalServices.Security.AES;
import com.example.shaya.sgcapp.TechnicalServices.Adapters.MessageAdapter;
import com.example.shaya.sgcapp.Domain.ModelClasses.Messages;
import com.example.shaya.sgcapp.R;
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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChat extends AppCompatActivity {

    private EditText inputMessage;
    private ImageButton sendMessageButton;
    private ImageButton sendImageButton;

    private TextView groupDisplayName, groupDisplayMembers;
    private CircleImageView groupDisplayImage;

    private String groupId;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef, rootRef;
    private StorageReference firebaseStorage;

    //RelativeLayout relativeLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView groupChatMessagesList;

    private ProgressDialog loadingBar;

    private AES aes;
    private String key;
    private String keyVal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        getSupportActionBar().hide();

        Intent intent = getIntent();
        groupId = intent.getStringExtra("group_id");

        initializeControllers();

        getGroupData();

        groupDisplayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupInfo();
            }
        });
        groupDisplayMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupInfo();
            }
        });
        groupDisplayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupInfo();
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGroupMessage();
            }
        });

        /*sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImage();
            }
        });*/

    }

    @Override
    protected void onResume() {
        super.onResume();

        getGroupData();
        messagesList.clear();

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        getGroupData();
        messagesList.clear();
    }

    private void getGroupData() {

        rootRef.child("groups").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                groupDisplayName.setText(dataSnapshot.child("Name").getValue().toString());
                groupDisplayMembers.setText("Members: " + dataSnapshot.child("Total_Members").getValue().toString());
                Picasso.get().load(dataSnapshot.child("Group_Pic").getValue().toString()).into(groupDisplayImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        messagesList.clear();

        rootRef.child("group-messages").child(groupId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                //finish();
                //
                // startActivity(getIntent());
                groupChatMessagesList.smoothScrollToPosition(groupChatMessagesList.getAdapter().getItemCount());
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

    private void initializeControllers()
    {
        ActionBar actionBar = getSupportActionBar();
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        //relativeLayout = actionBarView.findViewById(R.id.custom_chat_bar_layout);
        actionBar.setCustomView(actionBarView);

        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        firebaseStorage = FirebaseStorage.getInstance().getReference().child("group_messages_images").child(groupId);

        groupDisplayName = findViewById(R.id.custom_chat_bar_name);
        groupDisplayMembers = findViewById(R.id.custom_chat_bar_status);
        groupDisplayImage = findViewById(R.id.custom_chat_bar_image);
        inputMessage = findViewById(R.id.group_chat_input_messages);
        sendMessageButton = findViewById(R.id.group_chat_send_message_btn);
        sendImageButton = findViewById(R.id.group_chat_send_image);

        groupChatMessagesList = findViewById(R.id.group_chat_messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        groupChatMessagesList.setLayoutManager(linearLayoutManager);

        loadingBar = new ProgressDialog(this);

        messageAdapter = new MessageAdapter(messagesList, groupId);
        groupChatMessagesList.setAdapter(messageAdapter);

        rootRef.child("groups").child(groupId).child("Security").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                key = dataSnapshot.child("key").getValue().toString();

                rootRef.child("groups").child(groupId).child("Security").child("keyVersions").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists())
                        {
                            keyVal = dataSnapshot.getValue().toString();
                            //Toast.makeText(GroupChat.this, ""+keyVal, Toast.LENGTH_SHORT).show();
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
    }

    private void sendGroupMessage() {

        String sendingMsg = inputMessage.getText().toString();
        inputMessage.setText("");
        aes = new AES();
        String encryptedVal = "";
        try
        {
            encryptedVal = aes.encrypt(sendingMsg, keyVal);

        }catch (Exception e)
        {
            Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
        }

        if(!sendingMsg.isEmpty())
        {
            DatabaseReference userMessageKeyRef = rootRef.child("group-messages").child(groupId).push();

            String msgPushId = userMessageKeyRef.getKey();

            Map msgTextBody = new HashMap();
            msgTextBody.put("message", encryptedVal);
            msgTextBody.put("type", "text");
            msgTextBody.put("from", currentUserId);
            msgTextBody.put("msgKey", msgPushId);
            msgTextBody.put("keyVersion", key);

            String messageSenderRef = "group-messages/" + groupId;

            Map msgBodyDetails = new HashMap();
            msgBodyDetails.put(messageSenderRef + "/" + msgPushId, msgTextBody);

            rootRef.updateChildren(msgBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(!task.isSuccessful())
                    {
                        Toast.makeText(GroupChat.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void groupInfo() {

        Intent intent = new Intent(this,GroupSettings.class);
        intent.putExtra("groupId",groupId);
        startActivity(intent);
        finish();
    }

    public void sendImage(View view)
    {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK && data != null)
        {
            loadingBar.setTitle("Sending Image");
            loadingBar.setMessage("Please wait while your image is uploading");
            loadingBar.show();

            Uri encUri;

            aes = new AES();
            final Uri imageUri = data.getData();
            File file = new File(getRealPathFromURI(imageUri));
            File encFile = new File(aes.encryptImage(file,keyVal));
            encUri = Uri.fromFile(encFile);

            //Toast.makeText(this, ""+ imageUri.toString(), Toast.LENGTH_LONG).show();


            final String messageSenderRef = "group-messages/" + groupId;

            DatabaseReference userMessageKeyRef = rootRef.child("group-messages").child(groupId).push();

            final String msgPushId = userMessageKeyRef.getKey();

            final StorageReference filePath = firebaseStorage.child(msgPushId+".jpg");
            filePath.putFile(encUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
                                msgTextBody.put("from", currentUserId);
                                msgTextBody.put("msgKey", msgPushId);
                                msgTextBody.put("keyVersion", key);

                                Map msgBodyDetails = new HashMap();
                                msgBodyDetails.put(messageSenderRef + "/" + msgPushId, msgTextBody);

                                rootRef.updateChildren(msgBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {

                                        if(!task.isSuccessful())
                                        {
                                            Toast.makeText(GroupChat.this, "Error sending image", Toast.LENGTH_SHORT).show();
                                        }
                                        inputMessage.setText("");
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
                        Toast.makeText(GroupChat.this, "Picture not sent. Please try again", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }

                }
            });


        }
    }

    public String getRealPathFromURI(Uri contentUri)
    {
        String[] proj = { MediaStore.Audio.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
