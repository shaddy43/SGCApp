package com.example.shaya.sgcapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

public class Main2Activity extends AppCompatActivity {

    TabLayout tLayout;
    ViewPager vPager;

    TabsPagerAdapter tabsPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        tLayout = findViewById(R.id.main_tabs);
        vPager = findViewById(R.id.main_tabs_pager);

        tabsPagerAdapter=new TabsPagerAdapter(getSupportFragmentManager());
        vPager.setAdapter(tabsPagerAdapter);
        tLayout.setupWithViewPager(vPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  //for adding menu on the activity

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main2_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {  //for doing something when an item is clicked

        int id=item.getItemId();

        if(id==R.id.menu_account)
        {
            startActivity(new Intent(this,SetProfile.class));
        }
        if(id==R.id.menu_settings)
        {
            Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
        }
        if(id==R.id.menu_chat)
        {
            Toast.makeText(this, "chat", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
