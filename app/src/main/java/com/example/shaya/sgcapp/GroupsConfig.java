package com.example.shaya.sgcapp;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import com.example.shaya.sgcapp.domain.Validation;
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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Random;

public class GroupsConfig {

    private String defaultGroupPicUrl = "https://firebasestorage.googleapis.com/v0/b/sgcapp-8dcbb.appspot.com/o/group_messages_images%2FGroup-icon.png?alt=media&token=3ef7955e-783f-4a71-9224-bba311438fc3";
    private DatabaseReference ref;
    private FirebaseAuth mAuth;
    private Validation validation;
    private DatabaseReference groupRef;
    private DatabaseReference groupUserRef;
    private DatabaseReference userRef;
    private String GK = "";
    private StorageReference storageRef;
    private LocalDatabaseHelper db;

    public GroupsConfig() {
    }

    public String createGroup(String groupName)
    {
        mAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        validation = new Validation();
        final String user = mAuth.getCurrentUser().getUid();
        DatabaseReference groupIdKeyRef = ref.child("groups").push();
        String groupKey = groupIdKeyRef.getKey();
        boolean validateGroupName = validation.validateGroupName(groupName);

        if(validateGroupName)
        {
            groupIdKeyRef = ref.child("groups").push();
            groupKey = groupIdKeyRef.getKey();

            final String finalGroupKey = groupKey;
            ref.child("groups").child(groupKey).child("Name").setValue(groupName).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        //Toast.makeText(Main2Activity.this, groupKey+" is created Successfully", Toast.LENGTH_SHORT).show();
                        ref.child("group-users").child(finalGroupKey).child(user).child("groupStatus").setValue("admin")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        ref.child("users").child(user).child("user-groups").child(finalGroupKey).setValue("true");
                                        ref.child("groups").child(finalGroupKey).child("Total_Members").setValue(1);
                                        ref.child("groups").child(finalGroupKey).child("Security").child("Algo").setValue("AES");
                                        ref.child("groups").child(finalGroupKey).child("Security").child("key").setValue("");
                                        ref.child("groups").child(finalGroupKey).child("Group_Pic").setValue(defaultGroupPicUrl);

                                    }
                                });
                    }
                }
            });
        }
        return groupKey;
    }

    public void addGroupMembers(ArrayList<String> members, String groupKey)
    {
        groupRef = FirebaseDatabase.getInstance().getReference().child("group-users");
        ref = FirebaseDatabase.getInstance().getReference();

        for(int i=0;i<members.size();i++)
        {
            groupRef.child(groupKey).child(members.get(i)).child("groupStatus").setValue("member");
            ref.child("users").child(members.get(i)).child("user-groups").child(groupKey).setValue("true");

            String hashValue = "";
            try {
                hashValue = generateRandom();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ref.child("groups").child(groupKey).child("Security").child("GKgeneration").child(members.get(i)).setValue(hashValue);
        }
        ref.child("groups").child(groupKey).child("Total_Members").setValue(members.size()+1);
    }

    public void setKey(String gK, String groupKey, Context context)
    {
        db = new LocalDatabaseHelper(context);
        ref = FirebaseDatabase.getInstance().getReference();

        ref.child("groups").child(groupKey).child("Security").child("keyVersions").child("v").setValue(gK);
        ref.child("groups").child(groupKey).child("Security").child("key").setValue("v");
        db.insertData(groupKey,"v",gK);
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

    public void updateGroup(final String groupId, Context context)
    {
        db = new LocalDatabaseHelper(context);
        ref = FirebaseDatabase.getInstance().getReference();

        ref.child("group-users").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long c = dataSnapshot.getChildrenCount();
                ref.child("groups").child(groupId).child("Total_Members").setValue(c);

                generateGroupKey(groupId);

                ref.child("groups").child(groupId).child("Security").child("keyVersions").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists())
                        {
                            int count = (int) dataSnapshot.getChildrenCount();
                            ref.child("groups").child(groupId).child("Security").child("keyVersions").child("v"+count).setValue(GK);
                            ref.child("groups").child(groupId).child("Security").child("key").setValue("v"+count);
                            db.insertData(groupId,"v"+count,GK);
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

    private void generateGroupKey(String groupId)
    {
        ref = FirebaseDatabase.getInstance().getReference();

        ref.child("groups").child(groupId).child("Security").child("GKgeneration").addValueEventListener(new ValueEventListener() {
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

                        MessageDigest m = MessageDigest.getInstance("SHA-256");
                        m.reset();
                        m.update(groupKey.getBytes());
                        byte[] digest = m.digest();
                        BigInteger bigInt = new BigInteger(1, digest);
                        String hashText = bigInt.toString(16);
                        GK = hashText;

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

    public void deleteGroupMembers(ArrayList<String> deleteMembers, String groupId)
    {
        ref = FirebaseDatabase.getInstance().getReference();

        for (int i=0;i<deleteMembers.size();i++)
        {
            ref.child("group-users").child(groupId).child(deleteMembers.get(i)).removeValue();
            ref.child("users").child(deleteMembers.get(i)).child("user-groups").child(groupId).removeValue();
            ref.child("groups").child(groupId).child("Security").child("GKgeneration").child(deleteMembers.get(i)).removeValue();
        }
    }

    public void leaveGroup(final String groupId, final String currentUserId, final Context context)
    {
        ref = FirebaseDatabase.getInstance().getReference();
        groupUserRef = FirebaseDatabase.getInstance().getReference().child("group-users");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");


        groupUserRef.child(groupId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    userRef.child(currentUserId).child("user-groups").child(groupId).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {
                                        ref.child("groups").child(groupId).child("Security").child("GKgeneration").child(currentUserId).removeValue();
                                        updateGroup(groupId,context);
                                    }
                                }
                            });
                }
            }
        });
    }

    public boolean changeGroupName(String groupName, String groupId)
    {
        groupRef = FirebaseDatabase.getInstance().getReference().child("groups");
        validation = new Validation();
        boolean validateGroupName = validation.validateGroupName(groupName);

        if(validateGroupName)
        {
            groupRef.child(groupId).child("Name").setValue(groupName);
            return true;
        }
        else
            return false;
    }

    public String changeGroupImage(final String groupId, Uri imageUri)
    {
        storageRef = FirebaseStorage.getInstance().getReference().child("group_profile_images");
        final String[] url = new String[1];

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
                            url[0] = downloadUrl;
                            groupRef.child(groupId).child("Group_Pic").setValue(downloadUrl);
                        }
                    });
                }
            }
        });
        return url[0];
    }

    public void deleteGroup(final String groupId)
    {
        ref = FirebaseDatabase.getInstance().getReference();

        /*rootStorage.child("group_profile_images").child(groupId+".jpg").delete();

        groupMessagesRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot d: dataSnapshot.getChildren())
                    {
                        String msgKey = d.getKey()+".jpg";

                        rootStorage.child("group_messages_images").child(groupId).child(msgKey).delete();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        ref.child("group-users").child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    for(DataSnapshot d : dataSnapshot.getChildren())
                    {
                        String user = d.getKey();

                        ref.child("users").child(user).child("user-groups").child(groupId).removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ref.child("group-messages").child(groupId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            ref.child("group-users").child(groupId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {
                                        ref.child("groups").child(groupId).removeValue();

                                    }
                                }
                            });
                        }
                    }
                });

    }

    public void changeEncryptionKey(String groupId, String algo, String encryptionKey, Context context)
    {
        db = new LocalDatabaseHelper(context);
        ref = FirebaseDatabase.getInstance().getReference();

        ref.child("groups").child(groupId).child("Security").child("Algo").setValue(algo);
        ref.child("groups").child(groupId).child("Security").child("keyVersions").removeValue();
        ref.child("group-messages").child(groupId).removeValue();

        ref.child("groups").child(groupId).child("Security").child("keyVersions").child("v").setValue(encryptionKey);
        ref.child("groups").child(groupId).child("Security").child("key").setValue("v");

        db.deleteData(groupId);
        db.insertData(groupId,"v",encryptionKey);
    }
}
