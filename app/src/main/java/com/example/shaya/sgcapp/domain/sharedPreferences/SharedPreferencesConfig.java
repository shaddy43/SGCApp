package com.example.shaya.sgcapp.domain.sharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.shaya.sgcapp.R;

public class SharedPreferencesConfig {

    private SharedPreferences sharedPreferences;
    private Context context;

    public SharedPreferencesConfig(Context c)
    {
        this.context=c;
        sharedPreferences=context.getSharedPreferences(context.getResources().getString(R.string.registration_preference),context.MODE_PRIVATE);
    }

    public void writeLoginStatus(boolean status)
    {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(context.getResources().getString(R.string.registration_status), status);
        editor.apply();
    }

    public boolean readLoginStatus()
    {
        boolean status;
        status=sharedPreferences.getBoolean(context.getResources().getString(R.string.registration_status), false);
        return status;
    }
}
