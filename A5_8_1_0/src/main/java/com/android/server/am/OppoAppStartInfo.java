package com.android.server.am;

import android.os.SystemClock;
import java.util.HashMap;
import java.util.Map.Entry;

public class OppoAppStartInfo {
    int mCrashCount = 0;
    long mCurStartTime = 0;
    long mFirstStartTime = 0;
    boolean mIsThird = false;
    String mLaunchedFromPackage;
    String mPkgName = "";
    String mProcessName = "";
    int mStartCount = 0;
    HashMap<String, Integer> mStartTypeMap = new HashMap();

    static OppoAppStartInfo builder(String processName, String pkgName, String startType, boolean isThird) {
        OppoAppStartInfo appStartInfo = new OppoAppStartInfo();
        appStartInfo.setProcessName(processName);
        appStartInfo.setPkgName(pkgName);
        appStartInfo.setIsThird(isThird);
        long time = SystemClock.elapsedRealtime();
        appStartInfo.setFirstStartTime(time);
        appStartInfo.setCurStartTime(time);
        appStartInfo.increaseStartCount(startType);
        return appStartInfo;
    }

    public String getProcessName() {
        return this.mProcessName;
    }

    public void setProcessName(String processName) {
        this.mProcessName = processName;
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public void setPkgName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public long getFirstStartTime() {
        return this.mFirstStartTime;
    }

    public void setFirstStartTime(long firstStartTime) {
        this.mFirstStartTime = firstStartTime;
    }

    public long getCurStartTime() {
        return this.mCurStartTime;
    }

    public void setCurStartTime(long curStartTime) {
        this.mCurStartTime = curStartTime;
    }

    public boolean getIsThird() {
        return this.mIsThird;
    }

    public void setIsThird(boolean isThird) {
        this.mIsThird = isThird;
    }

    public int getStartCount() {
        return this.mStartCount;
    }

    public void setCrashCount(int count) {
        this.mCrashCount = count;
    }

    public int getCrashCount() {
        return this.mCrashCount;
    }

    public void increaseStartCount(String startType) {
        this.mStartCount++;
        if (this.mStartTypeMap.containsKey(startType)) {
            int count = ((Integer) this.mStartTypeMap.get(startType)).intValue();
            this.mStartTypeMap.remove(startType);
            this.mStartTypeMap.put(startType, Integer.valueOf(count + 1));
            return;
        }
        this.mStartTypeMap.put(startType, Integer.valueOf(1));
    }

    public void cleanStartCount() {
        this.mStartCount = 0;
    }

    public void cleanup() {
        this.mStartCount = 0;
        this.mFirstStartTime = 0;
        this.mCurStartTime = 0;
        this.mIsThird = false;
        this.mPkgName = "";
        this.mProcessName = "";
        this.mStartTypeMap.clear();
    }

    public void dumpInfo(String abnormalType) {
    }

    public String infoToString(String abnormalType) {
        String str = "";
        StringBuilder sb = new StringBuilder(256);
        sb.append("[ ").append(getPkgName()).append(" ]    ").append(abnormalType).append("    ").append(getProcessName()).append("    ").append(Integer.toString(getStartCount(), 10)).append("    ");
        for (Entry<String, Integer> entry : this.mStartTypeMap.entrySet()) {
            sb.append((String) entry.getKey()).append("    ").append(Integer.toString(((Integer) entry.getValue()).intValue(), 10)).append("    ");
        }
        return sb.toString();
    }
}
