package com.android.server.am;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ColorResourcePreloadDatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_TABLE = "CREATE TABLE preload (id INTEGER PRIMARY KEY,packageName TEXT,uid INTEGER,hotnessCount INTEGER,bootCount INTEGER,AICount INTEGER,totalCount INTEGER,hitCount INTEGER,preloadLaunchTime INTEGER,normalLaunchTime INTEGER,memory INTEGER,version TEXT)";
    public static final String DATABASE_NAME = "opreload.db";
    public static final int DATABASE_VERSION = 1;
    public static final String PRELOAD_COLUMN_AI_COUNT = "AICount";
    public static final String PRELOAD_COLUMN_BOOT_COUNT = "bootCount";
    public static final String PRELOAD_COLUMN_HIT_COUNT = "hitCount";
    public static final String PRELOAD_COLUMN_HOTNESS_COUNT = "hotnessCount";
    public static final String PRELOAD_COLUMN_ID = "id";
    public static final String PRELOAD_COLUMN_MEMORY = "memory";
    public static final String PRELOAD_COLUMN_NAME = "packageName";
    public static final String PRELOAD_COLUMN_NORMAL_LAUNCH_TIME = "normalLaunchTime";
    public static final String PRELOAD_COLUMN_PRELOAD_LAUNCH_TIME = "preloadLaunchTime";
    public static final String PRELOAD_COLUMN_TOTAL_COUNT = "totalCount";
    public static final String PRELOAD_COLUMN_UID = "uid";
    public static final String PRELOAD_COLUMN_VERSION = "version";
    public static final String PRELOAD_TABLE_NAME = "preload";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS preload";

    public ColorResourcePreloadDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
