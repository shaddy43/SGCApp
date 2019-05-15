package com.example.shaya.sgcapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.shaya.sgcapp.UI.GroupSettingsAdvance;
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
    private Security enc;
    private ArrayList<String> members;

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
                                        ref.child("groups").child(finalGroupKey).child("Security").child("distribution").setValue("unicast");
                                        ref.child("groups").child(finalGroupKey).child("Group_Pic").setValue(defaultGroupPicUrl);

                                    }
                                });
                    }
                }
            });
        }
        return groupKey;
    }

    public void addGroupMembers(final ArrayList<String> members, final String groupId, String status, final Context context)
    {
        if(status.equals("new"))
        {
            groupRef = FirebaseDatabase.getInstance().getReference().child("group-users");
            ref = FirebaseDatabase.getInstance().getReference();

            for(int i=0;i<members.size();i++)
            {
                groupRef.child(groupId).child(members.get(i)).child("groupStatus").setValue("member");
                groupRef.child(groupId).child(members.get(i)).child("memberStatus").setValue("new");
                groupRef.child(groupId).child(members.get(i)).child("keyChange").setValue("unsuccessful");
                ref.child("users").child(members.get(i)).child("user-groups").child(groupId).setValue("true");
            }
            ref.child("groups").child(groupId).child("Total_Members").setValue(members.size()+1);
        }
        else
        {
            groupRef = FirebaseDatabase.getInstance().getReference().child("group-users");
            ref = FirebaseDatabase.getInstance().getReference();
            enc = new Security();
            db = new LocalDatabaseHelper(context);

            addGroupMembers(members,groupId,"new",context);

            ref.child("group-users").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {
                        for(DataSnapshot d: dataSnapshot.getChildren())
                        {
                            final String member = d.getKey();
                            for(int i=0;i<members.size();i++)
                            {
                                if(!member.equals(members.get(i)))
                                {
                                    ref.child("group-users").child(groupId).child(member).child("groupStatus").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists())
                                            {
                                                String status = dataSnapshot.getValue().toString();

                                                if(!status.equals("admin"))
                                                {
                                                    ref.child("group-users").child(groupId).child(member).child("memberStatus").setValue("old");
                                                    ref.child("group-users").child(groupId).child(member).child("keyChange").setValue("unsuccessful");
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                                else
                                {
                                    ref.child("group-users").child(groupId).child(member).child("memberStatus").setValue("new");
                                    ref.child("group-users").child(groupId).child(member).child("keyChange").setValue("unsuccessful");
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            /*for(int i=0;i<members.size();i++)
            {
                groupRef.child(groupId).child(members.get(i)).child("groupStatus").setValue("member");
                groupRef.child(groupId).child(members.get(i)).child("memberStatus").setValue("new");
                ref.child("group-users").child(groupId).child(members.get(i)).child("keyChange").setValue("unsuccessful");

                ref.child("users").child(members.get(i)).child("user-groups").child(groupId).setValue("true");
            }*/

/*            ref.child("groups").child(groupId).child("Total_Members").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists())
                    {
                        int mem = Integer.parseInt(dataSnapshot.getValue().toString());

                        ref.child("groups").child(groupId).child("Total_Members").setValue(mem+members.size());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });*/

            ref.child("groups").child(groupId).child("Security").child("key").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists())
                    {
                        String keyVersion = dataSnapshot.getValue().toString();
                        String key = "";

                        Cursor res = db.getData(groupId,keyVersion);
                        if(res.getCount() != 0)
                        {
                            //StringBuffer buffer = new StringBuffer();
                            while (res.moveToNext()) {
                                //buffer.append("Key Value : " + res.getString(3) + "\n");
                                String grp = res.getString(1);
                                if(grp.equals(groupId))
                                {
                                    String ver = res.getString(2);
                                    if(ver.equals(keyVersion))
                                    {
                                        key = res.getString(3);
                                    }
                                }
                            }
                        }

                        if(!key.equals(""))
                        {
                            try {

                                final String newKey = generateNewKey(key);
                                updateGroup(groupId,newKey,context);

                                ref.child("groups").child(groupId).child("Security").child("distribution").setValue("broadcast");
                                String encKey = enc.encrypt(newKey,key);
                                ref.child("groups").child(groupId).child("Security").child("encKey").setValue(encKey);

                                for(int i=0;i<members.size();i++)
                                {
                                    String userEncKey = enc.encrypt(newKey,members.get(i));
                                    ref.child("group-users").child(groupId).child(members.get(i)).child("encKey").setValue(userEncKey);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void setKey(String gK, String groupKey, Context context, ArrayList<String> members) throws Exception {
        db = new LocalDatabaseHelper(context);
        ref = FirebaseDatabase.getInstance().getReference();
        enc = new Security();

        ref.child("groups").child(groupKey).child("Security").child("keyVersions").child("0").setValue(gK);
        ref.child("groups").child(groupKey).child("Security").child("key").setValue("0");
        db.insertData(groupKey,"0",gK);

        for(int i=0;i<members.size();i++)
        {
            String encKey = enc.encrypt(gK,members.get(i));
            ref.child("group-users").child(groupKey).child(members.get(i)).child("encKey").setValue(encKey);
        }
    }

    public void updateGroup(final String groupId, final String groupKey, Context context)
    {
        db = new LocalDatabaseHelper(context);
        ref = FirebaseDatabase.getInstance().getReference();

        ref.child("group-users").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long c = dataSnapshot.getChildrenCount();
                ref.child("groups").child(groupId).child("Total_Members").setValue(c);


                ref.child("groups").child(groupId).child("Security").child("keyVersions").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists())
                        {
                            int count = (int) dataSnapshot.getChildrenCount();
                            ref.child("groups").child(groupId).child("Security").child("keyVersions").child(String.valueOf(count)).setValue(groupKey);
                            ref.child("groups").child(groupId).child("Security").child("key").setValue(String.valueOf(count));
                            db.insertData(groupId,String.valueOf(count),groupKey);
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

    public void deleteGroupMembers(final ArrayList<String> deleteMembers, final String groupId, final Context context)
    {
        enc = new Security();
        ref = FirebaseDatabase.getInstance().getReference();
        members = new ArrayList<>();
        db = new LocalDatabaseHelper(context);

        for (int i=0;i<deleteMembers.size();i++)
        {
            ref.child("group-users").child(groupId).child(deleteMembers.get(i)).removeValue();
            ref.child("users").child(deleteMembers.get(i)).child("user-groups").child(groupId).removeValue();
        }

        ref.child("groups").child(groupId).child("Security").child("key").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String keyVersion = dataSnapshot.getValue().toString();

                    String key = "";

                    Cursor res = db.getData(groupId,keyVersion);
                    if(res.getCount() != 0)
                    {
                        //StringBuffer buffer = new StringBuffer();
                        while (res.moveToNext()) {
                            //buffer.append("Key Value : " + res.getString(3) + "\n");
                            String grp = res.getString(1);
                            if(grp.equals(groupId))
                            {
                                String ver = res.getString(2);
                                if(ver.equals(keyVersion))
                                {
                                    key = res.getString(3);
                                }
                            }
                        }
                    }

                    if(!key.equals(""))
                    {
                        try
                        {
                            final String newKey = generateNewKey(key);
                            updateGroup(groupId,newKey,context);

                            ref.child("groups").child(groupId).child("Security").child("distribution").setValue("unicast");
                            ref.child("group-users").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.exists())
                                    {
                                        for(DataSnapshot d : dataSnapshot.getChildren())
                                        {
                                            final String userId = d.getKey();
                                            ref.child("group-users").child(groupId).child(userId).child("groupStatus").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.exists())
                                                    {
                                                        String status = dataSnapshot.getValue().toString();

                                                        if(status.equals("member"))
                                                        {
                                                            members.add(userId);
                                                        }

                                                        for(int i=0; i<members.size();i++)
                                                        {
                                                            ref.child("group-users").child(groupId).child(members.get(i)).child("keyChange").setValue("unsuccessful");

                                                            try
                                                            {
                                                                String encKey = enc.encrypt(newKey,members.get(i));
                                                                ref.child("group-users").child(groupId).child(members.get(i)).child("encKey").setValue(encKey);
                                                            }catch (Exception e)
                                                            {

                                                            }
                                                        }

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }catch (Exception e)
                        {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void leaveGroup(final String groupId, final String currentUserId, final Context context)
    {
        ref = FirebaseDatabase.getInstance().getReference();
        groupUserRef = FirebaseDatabase.getInstance().getReference().child("group-users");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        enc = new Security();
        db = new LocalDatabaseHelper(context);
        //members = new ArrayList<>();

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
                                        ref.child("groups").child(groupId).child("Security").child("key").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists())
                                                {
                                                    String keyVersion = dataSnapshot.getValue().toString();

                                                    String key = "";

                                                    Cursor res = db.getData(groupId,keyVersion);
                                                    if(res.getCount() != 0)
                                                    {
                                                        //StringBuffer buffer = new StringBuffer();
                                                        while (res.moveToNext()) {
                                                            //buffer.append("Key Value : " + res.getString(3) + "\n");
                                                            String grp = res.getString(1);
                                                            if(grp.equals(groupId))
                                                            {
                                                                String ver = res.getString(2);
                                                                if(ver.equals(keyVersion))
                                                                {
                                                                    key = res.getString(3);
                                                                }
                                                            }
                                                        }
                                                    }

                                                    if(!key.equals(""))
                                                    {
                                                        try
                                                        {
                                                            final String newKey = generateNewKey(key);
                                                            updateGroup(groupId,newKey,context);

                                                            ref.child("groups").child(groupId).child("Security").child("distribution").setValue("unicast");
                                                            ref.child("group-users").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                    if(dataSnapshot.exists())
                                                                    {
                                                                        for(DataSnapshot d : dataSnapshot.getChildren())
                                                                        {
                                                                            final String userId = d.getKey();

                                                                            ref.child("group-users").child(groupId).child(userId).child("keyChange").setValue("unsuccessful");

                                                                            try
                                                                            {
                                                                                String encKey = enc.encrypt(newKey,userId);
                                                                                ref.child("group-users").child(groupId).child(userId).child("encKey").setValue(encKey);
                                                                            }catch (Exception e)
                                                                            {

                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });

                                                        }catch (Exception e)
                                                        {

                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
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

    public void deleteGroup(final String groupId, Context context)
    {
        ref = FirebaseDatabase.getInstance().getReference();
        db = new LocalDatabaseHelper(context);
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
                                        //db.deleteData(groupId);

                                    }
                                }
                            });
                        }
                    }
                });

    }

    public void changeEncryptionKey(final String groupId, final String algo, final String encryptionKey, Context context)
    {
        db = new LocalDatabaseHelper(context);
        ref = FirebaseDatabase.getInstance().getReference();

        /*ref.child("groups").child(groupId).child("Security").child("Algo").setValue(algo);
        ref.child("groups").child(groupId).child("Security").child("keyVersions").removeValue();
        ref.child("group-messages").child(groupId).removeValue();

        ref.child("groups").child(groupId).child("Security").child("keyVersions").child("v").setValue(encryptionKey);
        ref.child("groups").child(groupId).child("Security").child("key").setValue("v");*/

        ref.child("groups").child(groupId).child("Security").child("keyVersions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    int count = (int) dataSnapshot.getChildrenCount();
                    ref.child("groups").child(groupId).child("Security").child("Algo").setValue(algo);
                    ref.child("groups").child(groupId).child("Security").child("keyVersions").child("v"+count).setValue(encryptionKey);
                    ref.child("groups").child(groupId).child("Security").child("key").setValue("v"+count);
                    db.insertData(groupId,"v"+count,encryptionKey);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //db.deleteData(groupId);
        //db.insertData(groupId,"v",encryptionKey);
    }

    private String generateNewKey(String groupKey) throws Exception {
        Random rand = new Random();
        String randomDigit = String.valueOf(rand.nextLong());

        Security aes = new Security();

        String encryptedVal = aes.encrypt(randomDigit, groupKey);
        return encryptedVal;
    }
}
