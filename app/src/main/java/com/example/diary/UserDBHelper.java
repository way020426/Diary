package com.example.diary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDBHelper extends SQLiteOpenHelper{

    public static final String DB_NAME="User.db";
    public static final String TABLE_NAME = "Users";
    //建表语句
    public static final String CREATE_USERS="create table Users(" +
            "id integer primary key autoincrement," +
            "username text," +
            "salt text," +
            "hashedPassword text)";

    private Context mContext;

    public UserDBHelper(Context context, String name,
                        SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
        mContext=context;
    }

    public List<Map<String, String>> getAll() {
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM Users";
        Cursor cursor = database.rawQuery(selectQuery, null);
        List<Map<String, String>> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Map<String, String> map = new HashMap<>();
                String id = cursor.getString(0);
                String username = cursor.getString(1);
                String salt = cursor.getString(2);
                String hashedPassword = cursor.getString(3);
                map.put("id", id);
                map.put("username", username);
                map.put("salt", salt);
                map.put("hashedPassword", hashedPassword);
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return list;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
