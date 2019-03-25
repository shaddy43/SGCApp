package com.example.shaya.sgcapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shaya.sgcapp.UI.Main2Activity;
import com.example.shaya.sgcapp.domain.sharedPreferences.SharedPreferencesConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class Authentication extends AppCompatActivity {

    private Button sendVerificationCode;
    private Button verifyAccount;
    private EditText inputPhoneNumber;
    private EditText inputVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;
    private DatabaseReference userDefaultData;

    private String defaultPicUrl;

    private ProgressDialog loadingBar;

    private SharedPreferencesConfig sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_sign_in);

        sp = new SharedPreferencesConfig(this);

        if(sp.readLoginStatus())
        {
            Intent intent=new Intent(this,Main2Activity.class);
            startActivity(intent);
            this.finish();
        }

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        defaultPicUrl = "https://firebasestorage.googleapis.com/v0/b/sgcapp-8dcbb.appspot.com/o/profile_images%2Fred_john.jpg?alt=media&token=93d427e1-8943-4b5c-bf86-09579a52f5ba";

        sendVerificationCode = findViewById(R.id.phone_input_button);
        verifyAccount = findViewById(R.id.verify_code_button);
        inputPhoneNumber = findViewById(R.id.phone_number_input);
        inputVerificationCode = findViewById(R.id.verification_code_input);

        sendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = inputPhoneNumber.getText().toString();

                if(phoneNumber.isEmpty())
                {
                    Toast.makeText(Authentication.this, "Please enter a phone number", Toast.LENGTH_LONG).show();
                }
                else
                {
                    inputPhoneNumber.setVisibility(View.GONE);
                    sendVerificationCode.setVisibility(View.GONE);
                    inputVerificationCode.setVisibility(View.VISIBLE);
                    verifyAccount.setVisibility(View.VISIBLE);
                    loadingBar.setTitle("Phone Validation");
                    loadingBar.setMessage("Please wait...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            Authentication.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        verifyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendVerificationCode.setVisibility(View.GONE);
                //inputPhoneNumber.setVisibility(View.GONE);

                String verificationCode = inputVerificationCode.getText().toString();

                if(verificationCode.isEmpty())
                {
                    Toast.makeText(Authentication.this, "Please enter the verification code", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Code Validation");
                    loadingBar.setMessage("Please wait...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Toast.makeText(Authentication.this, "Invalid Phone Number", Toast.LENGTH_LONG).show();
                Toast.makeText(Authentication.this, "Check Internet Connection", Toast.LENGTH_LONG).show();

                sendVerificationCode.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);

                verifyAccount.setVisibility(View.GONE);
                inputVerificationCode.setVisibility(View.GONE);

                loadingBar.dismiss();
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(Authentication.this, "Validation code sent successfully", Toast.LENGTH_LONG).show();

                sendVerificationCode.setVisibility(View.GONE);
                inputPhoneNumber.setVisibility(View.GONE);

                verifyAccount.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            goToNext();
                        }
                        else {

                            String msg = task.getException().toString();
                            Toast.makeText(Authentication.this, ""+msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void goToNext()
    {
        final Intent intent = new Intent(this,Main2Activity.class);
        sp.writeLoginStatus(true);

        String userId = mAuth.getCurrentUser().getUid();
        final String phoneNumber = mAuth.getCurrentUser().getPhoneNumber();
        userDefaultData = FirebaseDatabase.getInstance().getReference().child("users").child(userId);



        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        userDefaultData.child("Device_Token").setValue(deviceToken);
        userDefaultData.child("Phone_Number").setValue(phoneNumber);
        userDefaultData.child("Name").setValue("User Name");
        userDefaultData.child("Profile_Pic").setValue(defaultPicUrl);
        userDefaultData.child("Status").setValue("Hey there i am using SGCApp")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(Authentication.this, "SignIn Successful", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(Authentication.this, "Sign in failed. Check your connection...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void goToEmailSignIn(View view)
    {
        startActivity(new Intent(Authentication.this,SignUp.class));
    }
}
