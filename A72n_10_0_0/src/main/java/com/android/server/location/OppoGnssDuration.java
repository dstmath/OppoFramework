package com.android.server.location;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.location.interfaces.IPswGnssDuration;
import java.util.HashMap;
import oppo.util.OppoStatistics;

public class OppoGnssDuration implements IPswGnssDuration {
    public static final long GPS_DURATION_THRESHOLD = 60000;
    public static final String LOGTAG_GPSLOCATION = "30101";
    private static final String TAG = "OppoGnssDuration";
    public static final String USER_ACTION_REQUEST_GPSLOCATION = "requestGpsLocation";
    private static boolean mIsDebug = false;
    private Context mContext = null;

    public OppoGnssDuration(Context context) {
        this.mContext = context;
    }

    public boolean isExpROM() {
        return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
    }

    public boolean isFeedBackGnssDuration(long gpsDuration, boolean isRecord, String packageName) {
        if (isRecord || gpsDuration < GPS_DURATION_THRESHOLD) {
            return false;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("duration", Long.toString(gpsDuration / 1000));
        map.put("pack_name", packageName);
        OppoStatistics.onCommon(this.mContext, "30101", USER_ACTION_REQUEST_GPSLOCATION, map, false);
        if (!mIsDebug) {
            return true;
        }
        Log.d(TAG, "ST pkg:" + packageName + ",duration:" + gpsDuration);
        return true;
    }

    public static void setDebug(boolean debug) {
        mIsDebug = debug;
    }
}
