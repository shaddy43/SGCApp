
package com.example.shaya.sgcapp;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.example.shaya.sgcapp.Adapters.UserAdapter;
import com.example.shaya.sgcapp.ModelClasses.AllUsers;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddContact extends AppCompatActivity {

    ListView displayList;
    ArrayList<AllUsers> allUserArray = new ArrayList<>();
    ArrayList<AllUsers> searchUserArray = new ArrayList<>();
    UserAdapter adapter;
    UserAdapter searchAdapter;
    DatabaseReference reference;
    SearchView searchView;
    SwipeRefreshLayout refreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        reference =FirebaseDatabase.getInstance().getReference().child("users");
        displayList = findViewById(R.id.user_display_list);
        refreshLayout = findViewById(R.id.add_contact_swipe_refresh);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onStart();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        allUserArray = new ArrayList<>();
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren())
                {
                    for(DataSnapshot d : dataSnapshot.getChildren())
                    {
                        AllUsers data = new AllUsers();
                        data.setName(d.child("Name").getValue().toString());
                        data.setStatus(d.child("Status").getValue().toString());
                        data.setProfile_Pic(d.child("Profile_Pic").getValue().toString());
                        data.setUserId(d.getRef().getKey());
                        allUserArray.add(data);
                    }
                    dataDisplay();
                }
                else
                {
                    refreshLayout.setRefreshing(false);
                    allUserArray = new ArrayList<>();
                    dataDisplay();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void dataDisplay()
    {
        adapter = new UserAdapter(allUserArray,this, false, false);
        displayList.setAdapter(adapter);
        refreshLayout.setRefreshing(false);

        displayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AllUsers data = (AllUsers) parent.getItemAtPosition(position);

                Intent intent = new Intent(AddContact.this,UsersProfileActivity.class);
                intent.putExtra("visit_user_id",data.getUserId());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  //for adding menu on the activity

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_contact_menu, menu);
        MenuItem item = menu.findItem(R.id.searchContact);

        searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUserArray=new ArrayList<>();
                reference.orderByChild("Name").equalTo(query).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot d : dataSnapshot.getChildren())
                        {
                            AllUsers data = new AllUsers();
                            data.setName(d.child("Name").getValue().toString());
                            data.setStatus(d.child("Status").getValue().toString());
                            data.setProfile_Pic(d.child("Profile_Pic").getValue().toString());
                            data.setUserId(d.getRef().getKey());
                            searchUserArray.add(data);
                        }
                        searchDataDisplay();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //adapter.getFilter().filter(newText);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                dataDisplay();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    public void searchDataDisplay()
    {
            searchAdapter = new UserAdapter(searchUserArray,this, false, false);
            displayList.setAdapter(searchAdapter);

        displayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AllUsers data = (AllUsers) parent.getItemAtPosition(position);

                Intent intent = new Intent(AddContact.this,UsersProfileActivity.class);
                intent.putExtra("visit_user_id",data.getUserId());
                startActivity(intent);
            }
        });
    }

}

