package com.oppo.internal.telephony.imsphone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.ArrayMap;
import android.util.Log;
import com.oppo.internal.telephony.utils.OppoPhoneUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OppoImsDatabaseHelper extends SQLiteOpenHelper {
    private static final String COLUMN_MODULE = "module";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_VALUE = "value";
    private static final String DATABASE_NAME = "imsConfig.db";
    private static final String TAG = "OppoImsDatabaseHelper";
    private static Object lockObj = new Object();
    private static OppoImsDatabaseHelper sDbHelper = null;
    private static final String[] sOpArray = {"CMCC", "CT", "CU", "FET", "APTG", "CHT", "TWM", "TST", "DTAC", "TRUE", "AIS", "JIO", "AIRTEL", "SINGTEL", "STARHUB", "M1", "TELSTRA", "OPTUS", "VDF", "MGFN", "TwoDGREE", "KDDI", "SOFTBANK", "DOCOMO", "TEST", OppoPhoneUtil.OP_DEFAULT, "OPX1", "OPX2", "OPX3", "OPX4", "OPX5", "OPX6", "OPX7"};
    private final Context mContext;

    public static OppoImsDatabaseHelper getDbHelper(Context context) {
        if (sDbHelper == null) {
            sDbHelper = new OppoImsDatabaseHelper(context);
            Log.d(TAG, "creat DatabaseHelper");
        }
        return sDbHelper;
    }

    public OppoImsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
        this.mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        int i = 0;
        while (true) {
            String[] strArr = sOpArray;
            if (i < strArr.length) {
                createTable(db, strArr[i]);
                i++;
            } else {
                return;
            }
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "dbh, onUpgrade table oldVersion " + oldVersion + ", newVersion " + newVersion);
    }

    private void createTable(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE " + tableName + "(" + COLUMN_NAME + " TEXT PRIMARY KEY," + COLUMN_VALUE + " TEXT NOT NULL," + COLUMN_MODULE + " INTEGER DEFAULT 0);");
        StringBuilder sb = new StringBuilder();
        sb.append("dbh, creat table : ");
        sb.append(tableName);
        Log.d(TAG, sb.toString());
    }

    public void updateConfig(String op, String name, String value, String module) {
        synchronized (lockObj) {
            try {
                SQLiteDatabase db = getWritableDatabase();
                if (isNewOp(db, op)) {
                    Log.e(TAG, "dbh, unknow op " + op);
                    return;
                }
                Log.d(TAG, "updateConfig name " + name + ", value " + value + "module : " + module);
                ContentValues cv = new ContentValues();
                cv.put(COLUMN_NAME, name);
                cv.put(COLUMN_VALUE, value);
                cv.put(COLUMN_MODULE, module);
                db.insertWithOnConflict(op, null, cv, 5);
                db.close();
            } catch (Exception ex) {
                Log.w(TAG, "updateConfig: " + ex.getMessage());
            }
        }
    }

    public void deleteConfig(String op, String module) {
        synchronized (lockObj) {
            try {
                SQLiteDatabase db = getWritableDatabase();
                Log.d(TAG, "deleteConfig op" + op + ", module " + module);
                db.delete(op, "module=?", new String[]{module});
                db.close();
            } catch (Exception ex) {
                Log.w(TAG, "updateConfig: " + ex.getMessage());
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0074, code lost:
        if (r2 == null) goto L_0x0077;
     */
    public Map<String, String> getConfig(String op, int module) {
        Map<String, String> map = new ArrayMap<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            String[] columns = {COLUMN_NAME, COLUMN_VALUE};
            cursor = db.query(true, op, columns, "module=" + module, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String value = cursor.getString(1);
                map.put(name, value);
                Log.d(TAG, "getConfig name " + name + ", value " + value);
            }
            db.close();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return map;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0030, code lost:
        if (r1 == null) goto L_0x0033;
     */
    private List<String> getTableNames(SQLiteDatabase db) {
        List<String> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(0));
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
        return list;
    }

    private boolean isNewOp(SQLiteDatabase db, String op) {
        if (getTableNames(db).contains(op)) {
            return false;
        }
        return true;
    }
}
