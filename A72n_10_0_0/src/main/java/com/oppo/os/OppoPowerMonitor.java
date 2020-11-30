package com.oppo.os;

import android.content.Context;
import android.os.ServiceManager;
import android.util.Log;
import com.oppo.os.IOppoPowerMonitor;

public class OppoPowerMonitor {
    public static final String POWER_MONITOR_SERVICE_NAME = "power_monitor";
    private static final String TAG = "OppoPowerMonitor";
    private static OppoPowerMonitor mInstance = null;
    private Context mContext;
    private IOppoPowerMonitor mOppoPowerMonitorService;

    private OppoPowerMonitor(Context context) {
        this.mOppoPowerMonitorService = null;
        this.mContext = null;
        this.mOppoPowerMonitorService = IOppoPowerMonitor.Stub.asInterface(ServiceManager.getService("power_monitor"));
        this.mContext = context;
    }

    public static OppoPowerMonitor getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new OppoPowerMonitor(context);
        }
        return mInstance;
    }

    public void recordAlarmWakeupEvent() {
        IOppoPowerMonitor iOppoPowerMonitor = this.mOppoPowerMonitorService;
        if (iOppoPowerMonitor != null) {
            try {
                iOppoPowerMonitor.recordAlarmWakeupEvent();
            } catch (Exception e) {
                Log.e(TAG, "recordAlarmWakeupEvent failed.", e);
            }
        } else {
            Log.e(TAG, "recordAlarmWakeupEvent failed: service unavailable");
        }
    }

    public void recordAppWakeupEvent(int alarmType, String alarmPackageName) {
        IOppoPowerMonitor iOppoPowerMonitor = this.mOppoPowerMonitorService;
        if (iOppoPowerMonitor != null) {
            try {
                iOppoPowerMonitor.recordAppWakeupEvent(alarmType, alarmPackageName);
            } catch (Exception e) {
                Log.e(TAG, "recordAppWakeupEvent failed.", e);
            }
        } else {
            Log.e(TAG, "recordAppWakeupEvent failed: service unavailable");
        }
    }

    public void resetWakeupEventRecords() {
        IOppoPowerMonitor iOppoPowerMonitor = this.mOppoPowerMonitorService;
        if (iOppoPowerMonitor != null) {
            try {
                iOppoPowerMonitor.resetWakeupEventRecords();
            } catch (Exception e) {
                Log.e(TAG, "resetWakeupEventRecords failed.", e);
            }
        } else {
            Log.e(TAG, "resetWakeupEventRecords failed: service unavailable");
        }
    }
}
