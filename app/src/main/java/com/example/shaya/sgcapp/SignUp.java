package com.example.shaya.sgcapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shaya.sgcapp.UI.Main2Activity;
import com.example.shaya.sgcapp.domain.sharedPreferences.SharedPreferencesConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class SignUp extends AppCompatActivity {

    EditText eName;
    EditText eEmail;
    EditText ePassword;
    ProgressBar pBar;

    SharedPreferencesConfig sp;
    FirebaseAuth mAuth;
    DatabaseReference userDefaultData;

    String name;
    String email;
    String password;

    String defaultPicUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        defaultPicUrl = "https://firebasestorage.googleapis.com/v0/b/sgcapp-8dcbb.appspot.com/o/profile_images%2Fred_john.jpg?alt=media&token=93d427e1-8943-4b5c-bf86-09579a52f5ba";
        eName = findViewById(R.id.editText_name);
        eEmail = findViewById(R.id.editText_email);
        ePassword =findViewById(R.id.editText_password);
        pBar = findViewById(R.id.progressBar);

        sp = new SharedPreferencesConfig(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();


        if(sp.readLoginStatus())
        {
            Intent intent=new Intent(this,Main2Activity.class);
            startActivity(intent);
            this.finish();
        }


    }

    public void signUp(View view)
    {
        name = eName.getText().toString();
        email = eEmail.getText().toString();
        password = ePassword.getText().toString();

        pBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    String userId = mAuth.getCurrentUser().getUid();
                    userDefaultData = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    userDefaultData.child("Device_Token").setValue(deviceToken);
                    userDefaultData.child("Phone_Number").setValue(email);
                    userDefaultData.child("Name").setValue(name);
                    userDefaultData.child("Profile_Pic").setValue(defaultPicUrl);
                    userDefaultData.child("Status").setValue("Hey there i am using SGCApp")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {
                                        goToNext();
                                    }
                                    else
                                    {
                                        pBar.setVisibility(View.GONE);
                                        Toast.makeText(SignUp.this, "Data not Inserted", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }
                else
                {
                    pBar.setVisibility(View.GONE);
                    Toast.makeText(SignUp.this, "Not Authenticated", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void goToNext()
    {
        sp.writeLoginStatus(true);
        Toast.makeText(this, "Data Inserted Successfully", Toast.LENGTH_SHORT).show();
        Intent intent =new Intent(this,Main2Activity.class);
        startActivity(intent);
        finish();
    }

    public void goToPhoneSignIn(View view)
    {
        Intent intent = new Intent(this, Authentication.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }
}
