package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.OppoManager;
import android.util.Log;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.location.interfaces.IPswGnssDiagnosticTool;
import java.util.HashMap;

public class OppoGnssDiagnosticTool implements IPswGnssDiagnosticTool {
    private static final String ACTION_DATE_CHANGED = "android.intent.action.DATE_CHANGED";
    private static final String GNSS_DIAGNOSTIC_EVENT_ID = "060101";
    private static final String KEY_GNSS_REQUEST_TIMER = "gnssTimer";
    private static final String KEY_MAX_CN0 = "maxCn0";
    private static final String KEY_MAX_SV_COUNT = "mSvCount";
    private static final String KEY_MAX_USED_COUNT = "maxUsedCount";
    private static final String TAG = "OppoGnssDiagnosticTool";
    private static boolean mDebug = false;
    private static OppoGnssDiagnosticTool mInstall = null;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.OppoGnssDiagnosticTool.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            OppoGnssDiagnosticTool oppoGnssDiagnosticTool = OppoGnssDiagnosticTool.this;
            oppoGnssDiagnosticTool.logD("receive broadcast intent, action: " + action);
            if (action != null && action.equals(OppoGnssDiagnosticTool.ACTION_DATE_CHANGED)) {
                OppoGnssDiagnosticTool.this.updateGnssSvDataByDay();
                OppoGnssDiagnosticTool.this.resetValue();
            }
        }
    };
    private Context mContext = null;
    private int mGnssRequestTimer = 0;
    private final Object mLock = new Object();
    private int mMaxCN0 = 0;
    private int mMaxSvCount = 0;
    private int mMaxUsedSvCount = 0;

    public static OppoGnssDiagnosticTool getInstall(Context context) {
        if (mInstall == null) {
            mInstall = new OppoGnssDiagnosticTool(context);
        }
        return mInstall;
    }

    private OppoGnssDiagnosticTool(Context context) {
        this.mContext = context;
        initValue();
    }

    private void initValue() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DATE_CHANGED);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, null);
    }

    public void storeSatellitesInfo(int svCount, int usedSvcount, int cn0) {
        synchronized (this.mLock) {
            if (this.mMaxSvCount < svCount) {
                logD("Change SvCount to : " + this.mMaxSvCount);
                this.mMaxSvCount = svCount;
            }
            if (this.mMaxUsedSvCount < usedSvcount) {
                logD("Change usedSvCount to : " + usedSvcount);
                this.mMaxUsedSvCount = usedSvcount;
            }
            if (this.mMaxCN0 < cn0) {
                logD("Change maxCn0 to : " + this.mMaxCN0);
                this.mMaxCN0 = cn0;
            }
        }
    }

    public void refreshRequestTimer() {
        synchronized (this.mLock) {
            this.mGnssRequestTimer++;
        }
    }

    /* access modifiers changed from: private */
    public void updateGnssSvDataByDay() {
        HashMap<String, String> map = new HashMap<>();
        synchronized (this.mLock) {
            map.put(KEY_MAX_CN0, StringUtils.EMPTY + this.mMaxCN0);
            map.put(KEY_MAX_SV_COUNT, StringUtils.EMPTY + this.mMaxSvCount);
            map.put(KEY_MAX_USED_COUNT, StringUtils.EMPTY + this.mMaxUsedSvCount);
            map.put(KEY_GNSS_REQUEST_TIMER, StringUtils.EMPTY + this.mGnssRequestTimer);
        }
        OppoManager.onStamp(GNSS_DIAGNOSTIC_EVENT_ID, map);
        logD("Data Stall statistics for GNSS DATA : " + map);
    }

    /* access modifiers changed from: private */
    public void resetValue() {
        synchronized (this.mLock) {
            this.mMaxCN0 = 0;
            this.mMaxSvCount = 0;
            this.mMaxUsedSvCount = 0;
            this.mGnssRequestTimer = 0;
        }
    }

    /* access modifiers changed from: private */
    public void logD(String sencent) {
        if (mDebug) {
            Log.d(TAG, sencent);
        }
    }

    public static void setDebug(boolean debug) {
        mDebug = debug;
    }
}
