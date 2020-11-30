package com.android.server.wm;

public class UsageRecorderRunnable implements Runnable {
    private String mProcName = null;
    private String mTimeStr = null;

    public UsageRecorderRunnable(String procName, String timeStr) {
        this.mProcName = procName;
        this.mTimeStr = timeStr;
    }

    public void run() {
        String str;
        String str2 = this.mProcName;
        if (str2 != null && str2.length() > 0 && (str = this.mTimeStr) != null && str.length() > 0) {
            OppoUsageManager.writeAppUsageHistoryRecord(this.mProcName, this.mTimeStr);
        }
    }
}
