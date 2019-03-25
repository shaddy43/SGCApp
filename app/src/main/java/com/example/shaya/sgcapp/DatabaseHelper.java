package com.example.shaya.sgcapp;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.shaya.sgcapp.domain.modelClasses.Users;
import com.example.shaya.sgcapp.domain.sharedPreferences.SharedPreferencesConfig;
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

import java.util.ArrayList;

public class DatabaseHelper {

    private FirebaseAuth mAuth;
    private String uId;
    private DatabaseReference userData;
    private StorageReference userDataStorage;
    private DatabaseReference reference;
    private DatabaseReference chatRequestRef;
    private DatabaseReference contactsRef;
    private DatabaseReference ref;
    private SharedPreferencesConfig sp;
    private ArrayList<Users> allUserArray;

    public DatabaseHelper() {
    }

    public void updateProfile(String name, String status)
    {
        mAuth = FirebaseAuth.getInstance();
        uId = mAuth.getCurrentUser().getUid();
        userData = FirebaseDatabase.getInstance().getReference().child("users").child(uId);

        userData.child("Name").setValue(name);
        userData.child("Status").setValue(status);
    }

    public ArrayList<Users> getData()
    {
        allUserArray = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren())
                {
                    for(DataSnapshot d : dataSnapshot.getChildren())
                    {
                        Users data = new Users();
                        data.setName(d.child("Name").getValue().toString());
                        data.setStatus(d.child("Status").getValue().toString());
                        data.setProfile_Pic(d.child("Profile_Pic").getValue().toString());
                        data.setUserId(d.getRef().getKey());
                        allUserArray.add(data);
                    }
                }
                else
                {
                    allUserArray = new ArrayList<>();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return allUserArray;
    }

    public void updateProfileImage(Uri imageUri)
    {
        //final boolean[] result = new boolean[1];
        mAuth = FirebaseAuth.getInstance();
        userDataStorage = FirebaseStorage.getInstance().getReference().child("profile_images");
        final StorageReference filePath = userDataStorage.child(mAuth.getCurrentUser().getUid() + ".jpg");

        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        userData.child("Profile_Pic").setValue(downloadUrl);
                    }
                });
            }
        });
    }

    public void sendChatRequest(final String currentUserId, final String receiverUserId)
    {
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("chat req");
        chatRequestRef.child(currentUserId).child(receiverUserId).child("req_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserId).child(currentUserId).child("req_type").setValue("received");
                        }

                    }
                });
    }

    public void cancelChatRequest(final String senderUserId, final String receiverUserId)
    {

        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("chat req");
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue();
                        }

                    }
                });
    }

    public void acceptChatRequest(final String senderUserId, final String receiverUserId)
    {
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("chat req");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");
        //final boolean[] result = new boolean[1];

        contactsRef.child(senderUserId).child(receiverUserId).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {

                            contactsRef.child(receiverUserId).child(senderUserId).child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                chatRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful())
                                                        {
                                                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                                                    .removeValue();
                                                        }

                                                    }
                                                });
                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    public void removeContact(final String senderUserId, final String receiverUserId)
    {
        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");
        contactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue();
                        }

                    }
                });
    }

    public void deleteAccount(Context context)
    {
        mAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        final String currentUserId = mAuth.getCurrentUser().getUid();
        sp = new SharedPreferencesConfig(context);


        ref.child("users").child(currentUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            ref.child("contacts").child(currentUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                ref.child("chats").child(currentUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful())
                                                                {
                                                                    ref.child("chat req").child(currentUserId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        mAuth.getCurrentUser().delete();
                                                                                        sp.writeLoginStatus(false);
                                                                                    }
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
    }
}
