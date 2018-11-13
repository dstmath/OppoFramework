package com.android.server.policy;

import android.os.OppoManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.AgingCriticalEvent;

public class HwShutdownRecord {
    private static final int MAX_BLOCK_BYTE = 512;
    private static final int SHUTDOWN_COUNT_BYTE = 16;
    private static final int SHUTDOWN_EACH_TIME_STR_BYTE = 14;
    private static final int SHUTDOWN_TIME_STR_LEN_BYTE = 4;
    private static final String STR_FORMAT = "%-16d%03d\n%s";
    private static final String STR_FORMAT_HEAD = "%-16d%03d\n";
    private static final String TAG = "HwShutdownRecord";
    private static HwShutdownRecord instance = null;
    private boolean mHwShutdown = false;
    private int mHwShutdownCount = -1;
    private String mHwShutdownTimeStr;
    private int mHwShutdownTimeStrLen = 0;

    public static HwShutdownRecord getInstance() {
        if (instance == null) {
            instance = new HwShutdownRecord();
        }
        return instance;
    }

    public void recordHwShutdownFlag() {
        if (!this.mHwShutdown) {
            this.mHwShutdown = true;
            Log.i(TAG, "recordHwShutdownFlag");
            SystemProperties.set("persist.sys.oppo.longpwk", Shell.NIGHT_MODE_STR_YES);
            OppoManager.syncCacheToEmmc();
            LoadHwShutdownCountIfNeed();
            storeHwShutdownCount(false);
            AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_POWERKEY_LONG_PRESSED, new String[0]);
        }
    }

    public void cancelHwShutdownFlag() {
        if (this.mHwShutdown) {
            this.mHwShutdown = false;
            Log.i(TAG, "cancelHwShutdownFlag");
            SystemProperties.set("persist.sys.oppo.longpwk", "");
            storeHwShutdownCount(true);
            AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_POWERKEY_LONGPRESSED_RELEASE, new String[0]);
        }
    }

    private int strToInt(String str, int startIndex, int endIndex) {
        String strSub = str.substring(startIndex, endIndex);
        if (strSub == null) {
            return 0;
        }
        strSub = strSub.trim();
        if (strSub == null || strSub.length() == 0) {
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
                        this.mHwShutdownTimeStr = OppoManager.readCriticalData(OppoManager.TYPE_HW_SHUTDOWN, (this.mHwShutdownTimeStrLen + 16) + 4).substring(20);
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
        OppoManager.writeCriticalData(OppoManager.TYPE_HW_SHUTDOWN, String.format(STR_FORMAT_HEAD, new Object[]{Integer.valueOf(count), Integer.valueOf(0)}));
    }

    private void storeHwShutdownCount(boolean cancelFlag) {
        int hwShutdownCount = this.mHwShutdownCount;
        int hwShutdownTimeStrLen = this.mHwShutdownTimeStrLen;
        String hwShutdownTimeStr = this.mHwShutdownTimeStr;
        if (!cancelFlag) {
            hwShutdownCount++;
            if (hwShutdownTimeStrLen + 14 <= 512) {
                hwShutdownTimeStrLen += 14;
                hwShutdownTimeStr = hwShutdownTimeStr + String.valueOf(System.currentTimeMillis()) + "\n";
            } else {
                hwShutdownTimeStr = (hwShutdownTimeStr + String.valueOf(System.currentTimeMillis()) + "\n").substring(14);
            }
        }
        OppoManager.cleanItem(OppoManager.TYPE_HW_SHUTDOWN);
        OppoManager.writeCriticalData(OppoManager.TYPE_HW_SHUTDOWN, String.format(STR_FORMAT, new Object[]{Integer.valueOf(hwShutdownCount), Integer.valueOf(hwShutdownTimeStrLen), hwShutdownTimeStr}));
    }
}
