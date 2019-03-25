package com.example.shaya.sgcapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shaya.sgcapp.R;
import com.example.shaya.sgcapp.domain.modelClasses.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends BaseAdapter {

    private DatabaseReference contactsRef;
    private DatabaseReference userRef;
    private DatabaseReference chatRequestRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private ArrayList<Users> mList;
    private Context mContext;
    private boolean visibilityStat;
    private boolean onlineState;

    public UserAdapter(ArrayList<Users> mList, Context mContext, boolean visibilityStat, boolean onlineState) {
        this.mList = mList;
        this.mContext = mContext;
        this.visibilityStat = visibilityStat;
        this.onlineState = onlineState;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = View.inflate(mContext, R.layout.user_display_layout,null);

        TextView name = view.findViewById(R.id.user_display_name);
        TextView status = view.findViewById(R.id.user_display_status);
        CircleImageView image = view.findViewById(R.id.users_display_profile);

        name.setText(mList.get(position).getName());
        status.setText(mList.get(position).getStatus());
        Picasso.get().load(mList.get(position).getProfile_Pic()).into(image);

        if(visibilityStat)
        {
            Button accept = view.findViewById(R.id.req_accept_btn);
            Button cancel = view.findViewById(R.id.req_cancel_btn);

            accept.setVisibility(View.VISIBLE);
            accept.setEnabled(true);
            cancel.setVisibility(View.VISIBLE);
            cancel.setEnabled(true);

            final String senderId = mList.get(position).getUserId();
            contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");
            userRef = FirebaseDatabase.getInstance().getReference().child("users");
            chatRequestRef = FirebaseDatabase.getInstance().getReference().child("chat req");
            mAuth = FirebaseAuth.getInstance();
            currentUserId = mAuth.getCurrentUser().getUid();

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    contactsRef.child(currentUserId).child(senderId).child("Contacts").setValue("Saved")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {

                                        contactsRef.child(senderId).child(currentUserId).child("Contacts").setValue("Saved")
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful())
                                                        {
                                                            chatRequestRef.child(currentUserId).child(senderId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful())
                                                                    {
                                                                        chatRequestRef.child(senderId).child(currentUserId)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                Toast.makeText(mContext, "Contact Added Successfully. Swipe to refresh", Toast.LENGTH_LONG).show();
                                                                                //mList.remove(position);
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
            });


            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    chatRequestRef.child(currentUserId).child(senderId)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {
                                        chatRequestRef.child(senderId).child(currentUserId)
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful())
                                                        {
                                                            Toast.makeText(mContext, "Request Deleted Successfully. Swipe to refresh", Toast.LENGTH_LONG).show();
                                                            //mList.remove(position);
                                                        }

                                                    }
                                                });
                                    }

                                }
                            });

                }
            });
        }

        if(onlineState && mList.get(position).getOnlineState().equals("online"))
        {
            CircleImageView onlineState = view.findViewById(R.id.user_display_online_state);
            onlineState.setVisibility(View.VISIBLE);
        }

        return view;
    }
}
