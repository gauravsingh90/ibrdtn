package com.group5.networking.slowpoll;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.TextView;

/**
 * Created by Joe on 4/10/2018.
 */

public class DB_Controller extends SQLiteOpenHelper {

    public DB_Controller(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "TEST.db", factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE POLLS(ID INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, optionOne TEXT, optionTwo TEXT, responseOne INTEGER, responseTwo INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS POLLS");
        onCreate(db);
    }
    public void insert_poll(String title, String optionOne, String optionTwo){
        ContentValues cv = new ContentValues();
        cv.put("title",title);
        cv.put("optionOne",optionOne);
        cv.put("optionTwo",optionTwo);
        cv.put("responseOne", 0);
        cv.put("responseTwo", 0);
        this.getWritableDatabase().insertOrThrow("POLLS","",cv);
    }
    public void delete_poll(int id){
        this.getWritableDatabase().delete("POLLS","id='"+id+"'", null);
    }
    public void list_all_polls(TextView textView){
        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT * FROM POLLS",null);
        textView.setText("");
        int i = 1;
        while(cursor.moveToNext()){
            textView.append(i + ". " + cursor.getString(1) + "\n\nOption 1: "+cursor.getString(2)+ "\nResponses: " + cursor.getString(4) + "\nOption 2: "+cursor.getString(3)+ "\nResponses: " + cursor.getString(5) + "\n\n");
            i++;
        }
    }
    public void delete_all_polls(){
        this.getWritableDatabase().execSQL("delete from POLLS");
    }
}
