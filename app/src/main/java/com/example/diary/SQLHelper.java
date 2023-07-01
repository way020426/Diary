package com.example.diary;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLHelper extends SQLiteOpenHelper{

    public static final String DB_NAME="NotePad.db";
    public static final String TABLE_NAME = "Diary";
    //建表语句
    public static final String CREATE_DIARY="create table Diary(" +
            "id integer primary key autoincrement," +
            "title text," +
            "time text," +
            "author text," +
            "content text," +
            "picture text)";
//            "date date)";

    private Context mContext;

    public SQLHelper(Context context, String name,
                     SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
        mContext=context;
    }

    public List<Map<String, String>> getAll() {
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM diary";
        // 执行指定的SQL查询语句，并返回一个Cursor对象，该对象包含查询结果的数据集。
        Cursor cursor = database.rawQuery(selectQuery, null);
        List<Map<String, String>> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Map<String, String> map = new HashMap<>();
                String id = cursor.getString(0);
                String title = cursor.getString(1);
                String time = cursor.getString(2);
                String author = cursor.getString(3);
                String content = cursor.getString(4);
                map.put("id", id);
                map.put("title", title);
                map.put("time", time);
                map.put("author", author);
                map.put("content", content);
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return list;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建名为"Diary"的表
        db.execSQL(CREATE_DIARY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

