package com.example.shaya.sgcapp.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shaya.sgcapp.R;
import com.example.shaya.sgcapp.domain.modelClasses.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrivateMessageAdapter extends RecyclerView.Adapter<PrivateMessageAdapter.PrivateMessageViewHolder>{

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    public PrivateMessageAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    public class PrivateMessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMsgText, receiverMsgText;
        public CircleImageView receiverProfileImage;
        public ImageView receiverMsgImage, senderMsgImage;
        public TextView senderName, receiverName;
        public LinearLayout receiverLinearLayout;
        public LinearLayout senderLinearLayout;

        public PrivateMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMsgText = itemView.findViewById(R.id.sender_messages_text);
            receiverMsgText = itemView.findViewById(R.id.receiver_messages_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            receiverMsgImage = itemView.findViewById(R.id.receiver_messages_image);
            senderMsgImage = itemView.findViewById(R.id.sender_messages_image);
            senderName = itemView.findViewById(R.id.sender_name);
            receiverName = itemView.findViewById(R.id.receiver_name);

            receiverLinearLayout = itemView.findViewById(R.id.receiver_linear_layout);
            senderLinearLayout = itemView.findViewById(R.id.sender_linear_layout);

        }
    }

    @NonNull
    @Override
    public PrivateMessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        return new PrivateMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PrivateMessageViewHolder privateMessageViewHolder, int i) {

        final String messageSenderId;
        Messages messages;
        final String fromUserId;
        String fromMessageType;

        messageSenderId = mAuth.getCurrentUser().getUid();
        messages = userMessagesList.get(i);
        fromUserId = messages.getFrom();
        fromMessageType = messages.getType();

        rootRef.child("users").child(fromUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    String image = dataSnapshot.child("Profile_Pic").getValue().toString();
                    Picasso.get().load(image).into(privateMessageViewHolder.receiverProfileImage);

                    String name = dataSnapshot.child("Name").getValue().toString();

                    if(!fromUserId.equals(messageSenderId))
                    {
                        privateMessageViewHolder.receiverLinearLayout.setVisibility(View.VISIBLE);
                        privateMessageViewHolder.receiverName.setVisibility(View.VISIBLE);
                        privateMessageViewHolder.receiverName.setText(name);
                    }
                    else
                    {
                        privateMessageViewHolder.receiverLinearLayout.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(fromMessageType.equals("text"))
        {
            privateMessageViewHolder.receiverName.setVisibility(View.GONE);
            privateMessageViewHolder.senderName.setVisibility(View.GONE);
            privateMessageViewHolder.receiverMsgText.setVisibility(View.GONE);
            privateMessageViewHolder.receiverProfileImage.setVisibility(View.GONE);
            privateMessageViewHolder.senderMsgText.setVisibility(View.GONE);
            privateMessageViewHolder.receiverMsgImage.setVisibility(View.GONE);
            privateMessageViewHolder.senderMsgImage.setVisibility(View.GONE);
            privateMessageViewHolder.senderLinearLayout.setVisibility(View.GONE);
            privateMessageViewHolder.receiverLinearLayout.setVisibility(View.GONE);

            if(fromUserId.equals(messageSenderId))
            {
                privateMessageViewHolder.senderLinearLayout.setVisibility(View.VISIBLE);
                privateMessageViewHolder.senderMsgText.setVisibility(View.VISIBLE);
                privateMessageViewHolder.senderMsgText.setText(messages.getMessage());
                privateMessageViewHolder.senderMsgText.setTextColor(Color.BLACK);
            }
            else
            {
                privateMessageViewHolder.receiverLinearLayout.setVisibility(View.VISIBLE);
                privateMessageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                privateMessageViewHolder.receiverMsgText.setVisibility(View.VISIBLE);
                privateMessageViewHolder.receiverMsgText.setText(messages.getMessage());
                privateMessageViewHolder.receiverMsgText.setTextColor(Color.BLACK);
            }
        }
        else if(fromMessageType.equals("image"))
        {
            privateMessageViewHolder.receiverName.setVisibility(View.GONE);
            privateMessageViewHolder.senderName.setVisibility(View.GONE);
            privateMessageViewHolder.receiverMsgText.setVisibility(View.GONE);
            privateMessageViewHolder.receiverProfileImage.setVisibility(View.GONE);
            privateMessageViewHolder.senderMsgText.setVisibility(View.GONE);
            privateMessageViewHolder.receiverMsgImage.setVisibility(View.GONE);
            privateMessageViewHolder.senderMsgImage.setVisibility(View.GONE);
            privateMessageViewHolder.senderLinearLayout.setVisibility(View.GONE);
            privateMessageViewHolder.receiverLinearLayout.setVisibility(View.GONE);

            if(fromUserId.equals(messageSenderId))
            {
                privateMessageViewHolder.senderLinearLayout.setVisibility(View.VISIBLE);
                privateMessageViewHolder.senderMsgImage.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(privateMessageViewHolder.senderMsgImage);
            }
            else
            {
                privateMessageViewHolder.receiverLinearLayout.setVisibility(View.VISIBLE);
                privateMessageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                privateMessageViewHolder.receiverMsgImage.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(privateMessageViewHolder.receiverMsgImage);
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}
