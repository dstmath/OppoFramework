package com.android.server.am;

import android.os.SystemClock;
import java.util.HashMap;
import java.util.Map;

public class ColorAppStartInfo {
    int mCrashCount = 0;
    long mCurStartTime = 0;
    long mFirstStartTime = 0;
    boolean mIsThird = false;
    String mLaunchedFromPackage;
    String mPkgName = "";
    String mProcessName = "";
    int mStartCount = 0;
    HashMap<String, Integer> mStartTypeMap = new HashMap<>();

    static ColorAppStartInfo builder(String processName, String pkgName, String startType, boolean isThird) {
        ColorAppStartInfo appStartInfo = new ColorAppStartInfo();
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
            int count = this.mStartTypeMap.get(startType).intValue();
            this.mStartTypeMap.remove(startType);
            this.mStartTypeMap.put(startType, Integer.valueOf(count + 1));
            return;
        }
        this.mStartTypeMap.put(startType, 1);
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
        StringBuilder sb = new StringBuilder(256);
        sb.append("[ ");
        sb.append(getPkgName());
        sb.append(" ]    ");
        sb.append(abnormalType);
        sb.append("    ");
        sb.append(getProcessName());
        sb.append("    ");
        sb.append(Integer.toString(getStartCount(), 10));
        sb.append("    ");
        for (Map.Entry<String, Integer> entry : this.mStartTypeMap.entrySet()) {
            sb.append(entry.getKey());
            sb.append("    ");
            sb.append(Integer.toString(entry.getValue().intValue(), 10));
            sb.append("    ");
        }
        return sb.toString();
    }
}
