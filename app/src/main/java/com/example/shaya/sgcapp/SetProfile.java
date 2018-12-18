package com.example.shaya.sgcapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class SetProfile extends AppCompatActivity {

    EditText name;
    EditText status;
    TextView phoneNum;
    CircleImageView imageView;
    Button editProfile;
    Button submit;

    FirebaseAuth mAuth;
    DatabaseReference userData;
    StorageReference userDataStorage;

    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        mAuth = FirebaseAuth.getInstance();
        String uId = mAuth.getCurrentUser().getUid();
        userData = FirebaseDatabase.getInstance().getReference().child("users").child(uId);
        userDataStorage = FirebaseStorage.getInstance().getReference().child("profile_images");

        name = findViewById(R.id.name_setProfile);
        status = findViewById(R.id.status_setProfile);
        phoneNum = findViewById(R.id.email_setProfile);
        imageView = findViewById(R.id.profile_image);
        editProfile = findViewById(R.id.editProfile_setProfile);
        submit = findViewById(R.id.submit_setProfile);

        name.setEnabled(false);
        status.setEnabled(false);
        imageView.setEnabled(false);
        submit.setVisibility(View.GONE);

        loadingBar = new ProgressDialog(this);

        userData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name.setText(dataSnapshot.child("Name").getValue().toString());
                status.setText(dataSnapshot.child("Status").getValue().toString());
                phoneNum.setText(dataSnapshot.child("Phone_Number").getValue().toString());
                Picasso.get().load(dataSnapshot.child("Profile_Pic").getValue().toString()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void editProfile(View view)
    {
        name.setEnabled(true);
        status.setEnabled(true);
        imageView.setEnabled(true);

        submit.setVisibility(View.VISIBLE);
        editProfile.setVisibility(View.GONE);

    }

    public void submit(View view)
    {
        name.setEnabled(false);
        status.setEnabled(false);
        imageView.setEnabled(false);

        submit.setVisibility(View.GONE);
        editProfile.setVisibility(View.VISIBLE);

        userData.child("Name").setValue(name.getText().toString());
        userData.child("Status").setValue(status.getText().toString());

    }

    public void changeImage(View view)
    {
        /*Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("Profile_Pic/*");
        startActivityForResult(intent,1);*/

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

            final Uri imageUri = data.getData();
            final StorageReference filePath = userDataStorage.child(mAuth.getCurrentUser().getUid() + ".jpg");
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                           String downloadUrl = uri.toString();
                            userData.child("Profile_Pic").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(SetProfile.this, "Profile pic updated", Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(SetProfile.this, "Not updated", Toast.LENGTH_LONG).show();
                                    }
                                    loadingBar.dismiss();
                                }
                            });
                        }
                    });



                }
            });

        }
    }
}
