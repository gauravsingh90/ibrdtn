package com.group5.networking.slowpoll;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.TextView;

/**
 * Created by Joe on 4/22/2018.
 */

public class PollController extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "poll.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE poll (" +
                    DatabaseContract.PollEntry._ID + " TEXT PRIMARY KEY," +
                    DatabaseContract.PollEntry.COLUMN_NAME_TITLE + " TEXT,"+ DatabaseContract.PollEntry.COLUMN_NAME_OPTIONONE + " TEXT,"
                    + DatabaseContract.PollEntry.COLUMN_NAME_OPTIONTWO + " TEXT," + DatabaseContract.PollEntry.COLUMN_NAME_RESPONSEONE + " INTEGER,"
                    + DatabaseContract.PollEntry.COLUMN_NAME_RESPONSETWO + " INTEGER," + DatabaseContract.PollEntry.COLUMN_NAME_INCENTIVE + " TEXT,"
                    + DatabaseContract.PollEntry.COLUMN_NAME_ANSWER + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseContract.PollEntry.TABLE_NAME;

    public PollController(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}