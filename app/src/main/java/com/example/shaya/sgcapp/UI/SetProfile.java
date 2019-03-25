package com.example.shaya.sgcapp.UI;

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

import com.example.shaya.sgcapp.DatabaseHelper;
import com.example.shaya.sgcapp.R;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetProfile extends AppCompatActivity {

    private EditText name;
    private EditText status;
    private TextView phoneNum;
    private CircleImageView imageView;
    private Button editProfile;
    private Button submit;

    private FirebaseAuth mAuth;
    private DatabaseReference userData;
    private StorageReference userDataStorage;

    private ProgressDialog loadingBar;

    private Validation validation;
    private DatabaseHelper helper;
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
        validation = new Validation();

        helper = new DatabaseHelper();

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
        boolean validateName = validation.validateName(name.getText().toString());
        //boolean validateStatus = validation.validateStatus(status.getText().toString());

        if(validateName )
        {
            name.setEnabled(false);
            status.setEnabled(false);
            imageView.setEnabled(false);

            String userName = name.getText().toString();
            String userStatus = status.getText().toString();
            helper.updateProfile(userName,userStatus);

            submit.setVisibility(View.GONE);
            editProfile.setVisibility(View.VISIBLE);
        }
        else
        {
            Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_LONG).show();
        }

    }

    public void changeImage(View view)
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
            /*helper.updateProfileImage(imageUri);
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();*/

            final StorageReference filePath = userDataStorage.child(mAuth.getCurrentUser().getUid() + ".jpg");

            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String downloadUrl = uri.toString();
                            userData.child("Profile_Pic").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(SetProfile.this, "profile pic updated", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                        Picasso.get().load(downloadUrl).into(imageView);
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(SetProfile.this, "Not successful. Please check internet", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }
}
