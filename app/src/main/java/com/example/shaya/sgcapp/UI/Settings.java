package com.example.shaya.sgcapp.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.shaya.sgcapp.DatabaseHelper;
import com.example.shaya.sgcapp.R;
import com.example.shaya.sgcapp.domain.sharedPreferences.SharedPreferencesConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Settings extends AppCompatActivity {

    private Button deletAccount;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private SharedPreferencesConfig sp;
    private DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        deletAccount = findViewById(R.id.deleteAccount);

        mAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        sp = new SharedPreferencesConfig(this);
        helper = new DatabaseHelper();

        deletAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletAccount();
            }
        });

    }

    private void deletAccount() {

        /*final String currentUserId = mAuth.getCurrentUser().getUid();

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
                                                                                        Intent i = getBaseContext().getPackageManager()
                                                                                                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                                                                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                        startActivity(i);
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
                });*/

        helper.deleteAccount(this);
        if(!sp.readLoginStatus())
        {
            Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage( getBaseContext().getPackageName() );
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }

    }
}
