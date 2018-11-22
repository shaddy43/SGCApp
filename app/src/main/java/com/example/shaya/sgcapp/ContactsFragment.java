package com.example.shaya.sgcapp;


import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {


    public ContactsFragment() {
        // Required empty public constructor
    }

    FirebaseAuth mAuth;
    View view;
    //ListView listView;
    //ArrayList<String> phoneList;
    ArrayList<AllUsers> data;
    //String phone;
    //Cursor cursor;
    AllUsers d;
    DatabaseReference ref;
    TextView name,status;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contacts, container, false);
        mAuth = FirebaseAuth.getInstance();
        //listView = view.findViewById(R.id.contact_view);
        //phoneList = new ArrayList<>();
        data = new ArrayList<AllUsers>();
        //cursor = null;

        String uId = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("users").child(uId);

        d = new AllUsers();
        name = view.findViewById(R.id.user_display_name_contact);
        status = view.findViewById(R.id.user_display_status_contact);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name.setText(dataSnapshot.child("Name").getValue().toString());
                status.setText(dataSnapshot.child("Status").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //getContacts();

        return view;
    }

    /*public void getContacts()
    {

        ContentResolver contentResolver = getActivity().getContentResolver();
        try
        {
            cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        }catch (Exception e)
        {
            Log.e(TAG, "Error " + e.getMessage());
        }

        if(cursor.getCount() > 0)
        {
            while(cursor.moveToNext())
            {
                String contact_id = cursor.getString((cursor.getColumnIndex(ContactsContract.Contacts._ID)));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                if(hasPhoneNumber > 0)
                {
                    Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI
                            ,null
                            , ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?"
                            , new String[]{contact_id}
                            , null);

                    while(phoneCursor.moveToNext())
                    {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        phone = phoneNumber;
                    }
                    phoneCursor.close();
                }
                phoneList.add(phone);
            }

            ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,phoneList);
            listView.setAdapter(listViewAdapter);
        }
    }*/

}
