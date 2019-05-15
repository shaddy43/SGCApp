package com.example.shaya.sgcapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class LocalDatabaseHelper extends SQLiteOpenHelper {

    public static final String databaseName = "sgcAppV1.db";
    public static final String tableName = "keys";

    public static final String id = "ID";
    public static final String groupId = "GroupId";
    public static final String keyVersion = "KeyVersion";
    public static final String keyValue = "KeyValue";

    public LocalDatabaseHelper(@Nullable Context context) {
        super(context, databaseName, null, 1);

        //SQLiteDatabase db = this.getWritableDatabase();
        //db.execSQL("create table keys(id INTEGER PRIMARY KEY AUTOINCREMENT, GroupId TEXT, KeyVersion Text, KeyValue Text)");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table "+tableName+"("+id+" INTEGER PRIMARY KEY AUTOINCREMENT, "+groupId+" TEXT, "+keyVersion+" Text, "+keyValue+" Text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS "+tableName);
        onCreate(db);
    }

    public boolean insertData(String group_Id, String keyVer, String keyVal)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(groupId, group_Id);
        contentValues.put(keyVersion,keyVer);
        contentValues.put(keyValue, keyVal);

        long result = db.insert(tableName, null, contentValues);
        if(result != -1)
        {
            return true;
        }
        else
            return false;
    }

    public Cursor getData(String group_Id, String keyVer)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        //Cursor result = db.rawQuery("select KeyValue from keys where GroupId like "+group_Id+" AND KeyVersion like "+keyVer+")", null);
        Cursor result = db.rawQuery("select * from "+tableName+"",null);
        return  result;
    }

    public void deleteData(String group_Id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, groupId + "=" + group_Id, null);
    }
}
