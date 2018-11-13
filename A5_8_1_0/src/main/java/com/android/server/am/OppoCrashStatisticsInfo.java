package com.android.server.am;

public class OppoCrashStatisticsInfo {
    public static boolean DEBUG = false;
    public static final String TAG = "OppoCrashStatisticsInfo";
    protected long mCurStartTime;
    protected long mFirstCrashTime;
    protected String mPkgName;
    protected String mProcessName;

    public OppoCrashStatisticsInfo(String processName, String pkgName, long firstTime) {
        this.mProcessName = processName;
        this.mPkgName = pkgName;
        this.mFirstCrashTime = firstTime;
    }

    public OppoCrashStatisticsInfo(String processName, long firstTime) {
        this.mProcessName = processName;
        this.mFirstCrashTime = firstTime;
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public void setPkgName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public String getProcessName() {
        return this.mProcessName;
    }

    public void setProcessName(String processName) {
        this.mProcessName = processName;
    }

    public long getFirstStartTime() {
        return this.mFirstCrashTime;
    }

    public void setFirstStartTime(long firstStartTime) {
        this.mFirstCrashTime = firstStartTime;
    }

    public long getCurStartTime() {
        return this.mCurStartTime;
    }

    public void setCurStartTime(long curStartTime) {
        this.mCurStartTime = curStartTime;
    }

    public String toString() {
        return "OppoCrashStatisticsInfo { mProcessName=" + this.mProcessName + " ,mFirstCrashTime=" + this.mFirstCrashTime;
    }
}
