package com.example.shaya.sgcapp.TechnicalServices.Adapters;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shaya.sgcapp.Domain.ModelClasses.Messages;
import com.example.shaya.sgcapp.R;
import com.example.shaya.sgcapp.TechnicalServices.Security.AES;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private DatabaseReference rootRef;
    private String groupId;
    String rootPath;
    String singleMsgKey;

    public MessageAdapter(List<Messages> userMessagesList, String groupId)
    {
        this.userMessagesList = userMessagesList;
        this.groupId = groupId;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView senderMsgText, receiverMsgText;
        public CircleImageView receiverProfileImage;
        public ImageView receiverMsgImage, senderMsgImage;
        public TextView senderName, receiverName;
        public LinearLayout receiverLinearLayout, senderLinearLayout;

        public MessageViewHolder(@NonNull View itemView) {
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
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();
        //String rootPath = null;

        return new MessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {

        final String messageSenderId;
        final Messages messages;
        final String fromUserId;
        final String fromMessageType;
        final String[] decryptMsg = {""};
        String keyVersion;

        messageSenderId = mAuth.getCurrentUser().getUid();
        messages = userMessagesList.get(i);
        fromUserId = messages.getFrom();
        fromMessageType = messages.getType();
        keyVersion = messages.getKeyVersion();

        //getKey(keyVersion);

        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("groups").child(groupId).child("Security").child("keyVersions").child(keyVersion).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    singleMsgKey = dataSnapshot.getValue().toString();

                    if(fromMessageType.equals("text"))
                    {
                        decryptMsg[0] = "";
                        AES aes = new AES();
                        try {
                            decryptMsg[0] = aes.decrypt(messages.getMessage(), singleMsgKey);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else if(fromMessageType.equals("image"))
                    {
                        decryptMsg[0] = "";
                        AES aes = new AES();
                        try {

                            rootPath = Environment.getExternalStorageDirectory()
                                    .getAbsolutePath() + "/apps/sgcapp/temp/";
                            File root = new File(rootPath);

                            if (!root.exists()) {
                                root.mkdirs();
                            }

                            new Downloader().execute(messages.getMessage(), rootPath, messages.getMsgKey());

                            File downloadedFile = new File(rootPath+messages.getMsgKey()+".crypt");

                            if(downloadedFile.exists())
                            {
                                decryptMsg[0] = aes.decryptImage(new File(rootPath.concat(messages.getMsgKey())), singleMsgKey);
                            }

                            //File dltFile = new File(rootPath+messages.getMsgKey()+".crypt");
                            //dltFile.delete();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    userRef = FirebaseDatabase.getInstance().getReference().child("users").child(fromUserId);

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists())
                            {
                                String image = dataSnapshot.child("Profile_Pic").getValue().toString();
                                Picasso.get().load(image).into(messageViewHolder.receiverProfileImage);

                                String name = dataSnapshot.child("Name").getValue().toString();

                                if(!fromUserId.equals(messageSenderId))
                                {
                                    messageViewHolder.receiverLinearLayout.setVisibility(View.VISIBLE);
                                    messageViewHolder.receiverName.setVisibility(View.VISIBLE);
                                    messageViewHolder.receiverName.setText(name);
                                }
                                else
                                {
                                    messageViewHolder.receiverLinearLayout.setVisibility(View.GONE);
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    if(fromMessageType.equals("text"))
                    {
                        messageViewHolder.receiverName.setVisibility(View.GONE);
                        messageViewHolder.senderName.setVisibility(View.GONE);
                        messageViewHolder.receiverMsgText.setVisibility(View.GONE);
                        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
                        messageViewHolder.senderMsgText.setVisibility(View.GONE);
                        messageViewHolder.receiverMsgImage.setVisibility(View.GONE);
                        messageViewHolder.senderMsgImage.setVisibility(View.GONE);
                        messageViewHolder.senderLinearLayout.setVisibility(View.GONE);
                        messageViewHolder.receiverLinearLayout.setVisibility(View.GONE);

                        if(fromUserId.equals(messageSenderId))
                        {
                            messageViewHolder.senderLinearLayout.setVisibility(View.VISIBLE);
                            messageViewHolder.senderMsgText.setVisibility(View.VISIBLE);
                            messageViewHolder.senderMsgText.setText(decryptMsg[0]);
                            messageViewHolder.senderMsgText.setTextColor(Color.BLACK);
                        }
                        else
                        {
                            messageViewHolder.receiverLinearLayout.setVisibility(View.VISIBLE);
                            messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                            messageViewHolder.receiverMsgText.setVisibility(View.VISIBLE);
                            messageViewHolder.receiverMsgText.setText(decryptMsg[0]);
                            messageViewHolder.receiverMsgText.setTextColor(Color.BLACK);
                        }
                    }
                    else if(fromMessageType.equals("image")) {
                        messageViewHolder.receiverName.setVisibility(View.GONE);
                        messageViewHolder.senderName.setVisibility(View.GONE);
                        messageViewHolder.senderLinearLayout.setVisibility(View.GONE);
                        messageViewHolder.receiverLinearLayout.setVisibility(View.GONE);
                        messageViewHolder.receiverMsgText.setVisibility(View.GONE);
                        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
                        messageViewHolder.senderMsgText.setVisibility(View.GONE);
                        messageViewHolder.receiverMsgImage.setVisibility(View.GONE);
                        messageViewHolder.senderMsgImage.setVisibility(View.GONE);

                        if (fromUserId.equals(messageSenderId)) {
                            File imgFile = new File(decryptMsg[0]);

                            if (imgFile.exists()) {

                                //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                //messageViewHolder.senderMsgImage.setImageBitmap(myBitmap);

                                messageViewHolder.senderLinearLayout.setVisibility(View.VISIBLE);
                                messageViewHolder.senderMsgImage.setVisibility(View.VISIBLE);

                                Picasso.get().load(imgFile).into(messageViewHolder.senderMsgImage);
                            }

                        } else {
                            File imgFile = new File(decryptMsg[0]);

                            if (imgFile.exists()) {
                                messageViewHolder.receiverLinearLayout.setVisibility(View.VISIBLE);
                                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                                messageViewHolder.receiverMsgImage.setVisibility(View.VISIBLE);

                                //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                //messageViewHolder.senderMsgImage.setImageBitmap(myBitmap);
                                Picasso.get().load(imgFile).into(messageViewHolder.receiverMsgImage);
                            }
                        }

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getKey(String keyVersion) {

        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("groups").child(groupId).child("Security").child("keyVersions").child(keyVersion).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    singleMsgKey = dataSnapshot.getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

}

class Downloader extends AsyncTask<String, String, String>
{

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("Starting download");
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            URL u = new URL(strings[0]);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.connect();

            File tempFile;

            tempFile = new File(strings[1].concat(strings[2]).concat(".crypt"));

            if (!tempFile.exists()) {
                tempFile.createNewFile();

                FileOutputStream out = new FileOutputStream(tempFile);

                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1)
                {
                    out.write(buffer, 0, len1);//Write new file
                }

                out.close();
                out.flush();
                is.close();
            }

            /*URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();
            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));

            fos.write(buffer);
            fos.flush();
            fos.close();*/

        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        System.out.println("Download completed");
    }
}


