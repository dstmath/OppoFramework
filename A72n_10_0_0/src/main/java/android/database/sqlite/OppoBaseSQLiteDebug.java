package android.database.sqlite;

import android.content.Context;
import android.database.DatabaseUtils;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import oppo.util.OppoStatistics;

public final class OppoBaseSQLiteDebug {
    private static final String EVENT_INFO = "sqliteinfo";
    private static final String LOGTAG = "2016101";
    private static final int SQL_CTRL_MODE = 2;
    private static final int SQL_LOG_MODE = 1;
    private static final String TAG = "OppoBaseSQLiteDebug";
    private static Context mContext = null;
    private static final String mHwShutdownSqlPropStr = "sys.oppo.sqlctrl_hwsd";
    private static boolean mLogenable = false;
    private static boolean mLowBattery = true;
    private static final String mLowBatterySqlPropStr = "sys.oppo.sqlctrl_lb";
    private static boolean mSqlctrl = false;
    private static final String mSqlstrlPropStr = "sys.oppo.sqlctrl";
    private static boolean mUrgentDisable = false;

    private static native void nativeSetOSQL(int i);

    private OppoBaseSQLiteDebug() {
    }

    public static void setContext(Context mmContext) {
        mContext = mmContext;
    }

    public static void onAnalyzeSqlctrlEndop() {
        mUrgentDisable = SystemProperties.getBoolean(mHwShutdownSqlPropStr, false);
        if (mUrgentDisable) {
            nativeSetOSQL(0);
        } else if (!mSqlctrl || mLowBattery) {
            nativeSetOSQL(0);
        } else {
            nativeSetOSQL(1);
        }
    }

    public static void onAnalyzeSqlctrl() {
        int propStatus = SystemProperties.getInt(mSqlstrlPropStr, 0);
        if ((propStatus & 1) != 0) {
            mLogenable = true;
        } else {
            mLogenable = false;
        }
        if ((propStatus & 2) != 0) {
            mSqlctrl = true;
        } else {
            mSqlctrl = false;
        }
        mLowBattery = SystemProperties.getBoolean(mLowBatterySqlPropStr, false);
        onAnalyzeSqlctrlEndop();
    }

    public static void onSQLExecuted(String label, String sql, int Cookie, long execTime) {
        if (mLogenable && execTime >= 100) {
            ArrayMap<String, String> uploadData = new ArrayMap<>();
            uploadData.put("Status", "Execute");
            uploadData.put("label", label);
            if (sql != null) {
                uploadData.put("SQL", String.valueOf(DatabaseUtils.getSqlStatementType(sql)));
            } else {
                uploadData.put("SQL", String.valueOf(0));
            }
            uploadData.put("execTime", String.valueOf(execTime));
            Context context = mContext;
            if (context != null) {
                OppoStatistics.onCommon(context, "2016101", EVENT_INFO, uploadData, 1);
            } else {
                Log.i(TAG, "onSQLExecuted, but context is null!");
            }
        }
    }

    public static void onConnectionObtained(String label, String sql, long execTime, boolean isPrimary) {
        if (mLogenable && execTime >= 100) {
            ArrayMap<String, String> uploadData = new ArrayMap<>();
            uploadData.put("Status", "Obtained");
            uploadData.put("label", label);
            if (sql != null) {
                uploadData.put("SQL", String.valueOf(DatabaseUtils.getSqlStatementType(sql)));
            } else {
                uploadData.put("SQL", String.valueOf(0));
            }
            uploadData.put("execTime", String.valueOf(execTime));
            Context context = mContext;
            if (context != null) {
                OppoStatistics.onCommon(context, "2016101", EVENT_INFO, uploadData, 2);
            } else {
                Log.i(TAG, "onConnectionObtained, but context is null!");
            }
        }
    }

    public static void onConnectionPoolBusy(String label, long Tid, String Tname, int activeConnections, int idleConnections, int availableConnections, ArrayList<String> requests, long execTime) {
        if (mLogenable) {
            ArrayMap<String, String> uploadData = new ArrayMap<>();
            uploadData.put("Status", "ConnectionPoolBusy");
            uploadData.put("label", label);
            uploadData.put("Tid", String.valueOf(Tid));
            uploadData.put("Tname", Tname);
            uploadData.put("activeC", String.valueOf(activeConnections));
            uploadData.put("idleC", String.valueOf(idleConnections));
            uploadData.put("availableC", String.valueOf(availableConnections));
            uploadData.put("execTime", String.valueOf(execTime));
            if (!requests.isEmpty()) {
                Log.i(TAG, "ConnectionPoolBusy, requests in progress:");
                Iterator<String> it = requests.iterator();
                while (it.hasNext()) {
                    String request = it.next();
                    Log.i(TAG, "" + request);
                    uploadData.put("request", request);
                }
                Log.i(TAG, "ConnectionPoolBusy, requests end");
            }
            Context context = mContext;
            if (context != null) {
                OppoStatistics.onCommon(context, "2016101", EVENT_INFO, uploadData, 0);
            } else {
                Log.i(TAG, "onConnectionPoolBusy, but context is null!");
            }
        }
    }

    public static void onDatabaseCorrupted(SQLiteDatabase db) {
        if (mLogenable) {
            ArrayMap<String, String> uploadData = new ArrayMap<>();
            uploadData.put("Status", "Corrupted");
            uploadData.put("label", db.getLabel());
            Context context = mContext;
            if (context != null) {
                OppoStatistics.onCommon(context, "2016101", EVENT_INFO, uploadData, 0);
            } else {
                Log.i(TAG, "onDatabaseCorrupted, but context is null!");
            }
        }
    }
}
