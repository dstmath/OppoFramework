package com.android.server.policy;

import android.os.Handler;
import android.os.OppoManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.UiModeManagerService;
import com.android.server.am.IColorGameSpaceManager;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.oppo.TemperatureProvider;
import java.io.FileWriter;

public class HwShutdownRecord {
    private static final int HARDWARE_RESET_RECORD_FLAG_INDEX = 77;
    private static final int MAX_BLOCK_BYTE = 512;
    private static final int SHUTDOWN_COUNT_BYTE = 16;
    private static final int SHUTDOWN_EACH_TIME_STR_BYTE = 14;
    private static final int SHUTDOWN_TIME_STR_LEN_BYTE = 4;
    private static final String STR_FORMAT = "%-16d%03d\n%s";
    private static final String STR_FORMAT_HEAD = "%-16d%03d\n";
    private static final String TAG = "HwShutdownRecord";
    private static final String mHwShutdownSqlPropStr = "sys.oppo.sqlctrl_hwsd";
    private final Handler mHandler;
    private Runnable mHardwarShutdownRunnable = new Runnable() {
        /* class com.android.server.policy.HwShutdownRecord.AnonymousClass1 */

        public void run() {
            synchronized (HwShutdownRecord.this.mLock) {
                HwShutdownRecord.this.recordHwShutdownFlag();
            }
        }
    };
    private boolean mHwShutdown = false;
    private int mHwShutdownCount = -1;
    private String mHwShutdownTimeStr;
    private int mHwShutdownTimeStrLen = 0;
    private final Object mLock = new Object();
    private Runnable mUrgentDiableSqlFuncRunnable = new Runnable() {
        /* class com.android.server.policy.HwShutdownRecord.AnonymousClass3 */

        public void run() {
            synchronized (HwShutdownRecord.this.mLock) {
                HwShutdownRecord.this.UrgentDiableSqlFunc();
            }
        }
    };
    private Runnable mUrgentFlushRunnable = new Runnable() {
        /* class com.android.server.policy.HwShutdownRecord.AnonymousClass2 */

        public void run() {
            HwShutdownRecord.this.urgent_sync();
        }
    };

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void urgent_sync() {
        try {
            Slog.i(TAG, "urgent_sync running start");
            FileWriter sysrq_trigger = new FileWriter("/proc/sysrq-trigger");
            sysrq_trigger.write(IColorGameSpaceManager.MSG_SCREEN_OFF);
            sysrq_trigger.close();
        } catch (Exception e) {
            Slog.i(TAG, "urgent_sync Exception=" + e);
        }
    }

    public HwShutdownRecord(Handler handler) {
        this.mHandler = handler;
    }

    public void recordHwShutdownFlag() {
        if (!this.mHwShutdown) {
            this.mHwShutdown = true;
            Log.i(TAG, "recordHwShutdownFlag");
            SystemProperties.set("persist.sys.oppo.longpwk", UiModeManagerService.Shell.NIGHT_MODE_STR_YES);
            SystemProperties.set("persist.sys.oppo.longpwkts", String.valueOf(System.currentTimeMillis()));
            OppoManager.syncCacheToEmmc();
            LoadHwShutdownCountIfNeed();
            storeHwShutdownCount(false);
        }
    }

    public void UrgentDiableSqlFunc() {
        try {
            SystemProperties.set(mHwShutdownSqlPropStr, TemperatureProvider.SWITCH_ON);
            Slog.i(TAG, "UrgentDiableSqlFunc running start, setprop " + SystemProperties.get(mHwShutdownSqlPropStr));
        } catch (Exception e) {
            Slog.i(TAG, "UrgentDiableSqlFunc Exception=" + e);
        }
    }

    public void RecoverSqlFunc() {
        try {
            SystemProperties.set(mHwShutdownSqlPropStr, TemperatureProvider.SWITCH_OFF);
            Slog.i(TAG, "RecoverSqlFunc running start, setprop " + SystemProperties.get(mHwShutdownSqlPropStr));
        } catch (Exception e) {
            Slog.i(TAG, "RecoverSqlFunc Exception=" + e);
        }
    }

    /* access modifiers changed from: package-private */
    public void startHwShutdownDectect() {
        synchronized (this.mLock) {
            this.mHandler.postDelayed(this.mHardwarShutdownRunnable, BackupAgentTimeoutParameters.DEFAULT_QUOTA_EXCEEDED_TIMEOUT_MILLIS);
            this.mHandler.postDelayed(this.mUrgentDiableSqlFuncRunnable, 3500);
            this.mHandler.postDelayed(this.mUrgentFlushRunnable, 7000);
        }
    }

    /* access modifiers changed from: package-private */
    public void clearHwShutdownDectect() {
        synchronized (this.mLock) {
            this.mHandler.removeCallbacks(this.mHardwarShutdownRunnable);
            cancelHwShutdownFlag();
            this.mHandler.removeCallbacks(this.mUrgentDiableSqlFuncRunnable);
            RecoverSqlFunc();
            this.mHandler.removeCallbacks(this.mUrgentFlushRunnable);
        }
    }

    public void cancelHwShutdownFlag() {
        if (this.mHwShutdown) {
            this.mHwShutdown = false;
            Log.i(TAG, "cancelHwShutdownFlag");
            SystemProperties.set("persist.sys.oppo.longpwk", "");
            SystemProperties.set("persist.sys.oppo.longpwkts", "");
            storeHwShutdownCount(true);
        }
    }

    private int strToInt(String str, int startIndex, int endIndex) {
        String strSub;
        String strSub2 = str.substring(startIndex, endIndex);
        if (strSub2 == null || (strSub = strSub2.trim()) == null || strSub.length() == 0) {
            return 0;
        }
        return Integer.parseInt(strSub);
    }

    private void LoadHwShutdownCountIfNeed() {
        if (this.mHwShutdownCount == -1) {
            try {
                String strData = OppoManager.readCriticalData(OppoManager.TYPE_HW_SHUTDOWN, 16);
                this.mHwShutdownCount = strToInt(strData, 0, strData.length());
                try {
                    this.mHwShutdownTimeStrLen = strToInt(OppoManager.readCriticalData(OppoManager.TYPE_HW_SHUTDOWN, 20), 16, 19);
                    this.mHwShutdownTimeStr = "";
                    if (this.mHwShutdownTimeStrLen > 0) {
                        this.mHwShutdownTimeStr = OppoManager.readCriticalData(OppoManager.TYPE_HW_SHUTDOWN, this.mHwShutdownTimeStrLen + 16 + 4).substring(20);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "LoadHwShutdownCountIfNeed read time region Exception,set default hardware shutdown time len to 0!");
                    initHwShutdownRegion(this.mHwShutdownCount);
                    this.mHwShutdownTimeStrLen = 0;
                    this.mHwShutdownTimeStr = "";
                }
            } catch (Exception e2) {
                Log.w(TAG, "LoadHwShutdownCountIfNeed read mHwShutdownCount Exception,set default hardware shutdown count to 0!");
                initHwShutdownRegion(0);
                this.mHwShutdownCount = 0;
                this.mHwShutdownTimeStrLen = 0;
                this.mHwShutdownTimeStr = "";
            }
        }
    }

    private void initHwShutdownRegion(int count) {
        OppoManager.writeCriticalData(OppoManager.TYPE_HW_SHUTDOWN, String.format(STR_FORMAT_HEAD, Integer.valueOf(count), 0));
    }

    private void storeHwShutdownCount(boolean cancelFlag) {
        int hwShutdownCount = this.mHwShutdownCount;
        int hwShutdownTimeStrLen = this.mHwShutdownTimeStrLen;
        String hwShutdownTimeStr = this.mHwShutdownTimeStr;
        if (!cancelFlag) {
            hwShutdownCount++;
            if (hwShutdownTimeStrLen + 14 <= 512) {
                hwShutdownTimeStrLen += 14;
                hwShutdownTimeStr = hwShutdownTimeStr + String.valueOf(System.currentTimeMillis()) + StringUtils.LF;
            } else {
                hwShutdownTimeStr = (hwShutdownTimeStr + String.valueOf(System.currentTimeMillis()) + StringUtils.LF).substring(14);
            }
        }
        OppoManager.cleanItem(OppoManager.TYPE_HW_SHUTDOWN);
        OppoManager.writeCriticalData(OppoManager.TYPE_HW_SHUTDOWN, String.format(STR_FORMAT, Integer.valueOf(hwShutdownCount), Integer.valueOf(hwShutdownTimeStrLen), hwShutdownTimeStr));
    }
}
