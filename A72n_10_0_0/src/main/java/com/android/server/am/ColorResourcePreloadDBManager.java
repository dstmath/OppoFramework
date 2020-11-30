package com.android.server.am;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.HandlerThread;

public class ColorResourcePreloadDBManager {
    private static final String PRELOAD_DB_THREAD_NAME = "RPDBHandler";
    public static final Object mDBLock = new Object();
    private static volatile ColorResourcePreloadDBManager mInstance;
    private SQLiteDatabase database;
    private ColorResourcePreloadDatabaseHelper dbHelper;
    private Context mContext;
    private Handler mDBHandler = null;

    private ColorResourcePreloadDBManager() {
    }

    public static synchronized ColorResourcePreloadDBManager getInstance() {
        ColorResourcePreloadDBManager colorResourcePreloadDBManager;
        synchronized (ColorResourcePreloadDBManager.class) {
            if (mInstance == null) {
                mInstance = new ColorResourcePreloadDBManager();
            }
            colorResourcePreloadDBManager = mInstance;
        }
        return colorResourcePreloadDBManager;
    }

    public void init(Context context) {
        this.mContext = context;
        this.dbHelper = new ColorResourcePreloadDatabaseHelper(this.mContext);
        this.database = this.dbHelper.getWritableDatabase();
        HandlerThread thread = new HandlerThread(PRELOAD_DB_THREAD_NAME, -2);
        thread.start();
        this.mDBHandler = new Handler(thread.getLooper());
    }

    public void close() {
        this.dbHelper.close();
    }

    public Handler getDBHandler() {
        return this.mDBHandler;
    }

    public boolean isDBOpen() {
        synchronized (mDBLock) {
            if (this.database == null) {
                return false;
            }
            return this.database.isOpen();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isTableExist() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void beginTransaction() {
        SQLiteDatabase sQLiteDatabase = this.database;
        if (sQLiteDatabase != null) {
            sQLiteDatabase.beginTransaction();
        }
    }

    /* access modifiers changed from: protected */
    public void setTransactionSuccessful() {
        SQLiteDatabase sQLiteDatabase = this.database;
        if (sQLiteDatabase != null) {
            sQLiteDatabase.setTransactionSuccessful();
        }
    }

    /* access modifiers changed from: protected */
    public void endTransaction() {
        SQLiteDatabase sQLiteDatabase = this.database;
        if (sQLiteDatabase != null) {
            sQLiteDatabase.endTransaction();
        }
    }

    public void insert(String name, ContentValues values) {
        this.database.insert(name, null, values);
    }

    /* access modifiers changed from: protected */
    public Cursor rawQuery(String sql, String[] args) {
        Cursor rawQuery;
        if (this.database == null) {
            return null;
        }
        synchronized (mDBLock) {
            rawQuery = this.database.rawQuery(sql, args);
        }
        return rawQuery;
    }

    public Cursor fetch(String tableName) {
        return this.database.query(tableName, new String[]{ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_NAME, ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_UID, ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_HOTNESS_COUNT, ColorResourcePreloadDatabaseHelper.PRELOAD_COLUMN_MEMORY}, null, null, null, null, null);
    }

    public Cursor fetch(String tableName, String pkgName, int uid) {
        return this.database.query(tableName, null, "packageName=? AND uid=?", new String[]{pkgName, Integer.toString(uid)}, null, null, null);
    }

    public int update(String tableName, ContentValues values, String pkgName, int uid) {
        return this.database.update(tableName, values, "packageName=? AND uid=?", new String[]{pkgName, Integer.toString(uid)});
    }

    public void delete(String tableName, String pkgName, int uid) {
        this.database.delete(tableName, "packageName=? AND uid=?", new String[]{pkgName, Integer.toString(uid)});
    }
}
