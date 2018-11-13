package com.android.server.am;

public class OppoAnrAppInfo {
    boolean isGuardTimeout;
    String mBroadcastName;
    long mBroadcastReceiverTime;
    String mBroadcastType;
    String mCallerPackage = "";
    int mChangeCount;
    int mCount = 0;
    long mCpu;
    boolean mFirstPro;
    boolean mForeground;
    boolean mOrdered;
    int mPid;
    String mProcessName = "";
    int mUid;

    public void setProcessName(String processName) {
        this.mProcessName = processName;
    }

    public void setCallerPackage(String pkgName) {
        this.mCallerPackage = pkgName;
    }

    public long getBroadcastReceiverTime() {
        return this.mBroadcastReceiverTime;
    }

    public void setBroadcastReceiverTime(long receiverTime) {
        this.mBroadcastReceiverTime = receiverTime;
    }

    public void setBroadcastName(String broadcastName) {
        this.mBroadcastName = broadcastName;
    }

    public String getBroadcastName() {
        return this.mBroadcastName;
    }

    public void setBroadcastType(String broadcastType) {
        this.mBroadcastType = broadcastType;
    }

    public boolean getGuardTimeout() {
        return this.isGuardTimeout;
    }

    public void setGuardTimeout(boolean timeout) {
        this.isGuardTimeout = timeout;
    }

    public int getPid() {
        return this.mPid;
    }

    public void setPid(int pid) {
        this.mPid = pid;
    }

    public int getUid() {
        return this.mUid;
    }

    public void setUid(int uid) {
        this.mUid = uid;
    }

    public int getCount() {
        return this.mCount;
    }

    public void setCount(int count) {
        this.mCount = count;
    }

    public boolean getForeground() {
        return this.mForeground;
    }

    public void setForeground(boolean fg) {
        this.mForeground = fg;
    }

    public String broadcastToString() {
        String str = "";
        StringBuilder sb = new StringBuilder(256);
        sb.append("[broadcast ").append(this.mProcessName).append("  ").append(this.mBroadcastName).append("  ").append(this.mBroadcastType).append("  ").append(this.isGuardTimeout).append("  ").append(this.mBroadcastReceiverTime).append("  ").append(isFg(this.mForeground)).append(" mCallerPkg:").append(this.mCallerPackage).append(" ]");
        return sb.toString();
    }

    public String broadcastReceiverToString() {
        String str = "";
        StringBuilder sb = new StringBuilder(256);
        sb.append("[broadcast ").append(this.mProcessName).append("  ").append(this.mBroadcastName).append(" mFirstPro:").append(this.mFirstPro).append("  ").append(this.mCount).append("  ").append(this.mChangeCount).append("  ").append(isFg(this.mForeground)).append("  ").append(this.mOrdered).append(" mCallerPkg:").append(this.mCallerPackage).append(" ]");
        return sb.toString();
    }

    public String serviceToString() {
        String str = "";
        StringBuilder sb = new StringBuilder(256);
        sb.append("[service ").append(this.mProcessName).append("  ").append(this.isGuardTimeout).append("  ").append(this.mBroadcastReceiverTime).append("  ").append(isFg(this.mForeground)).append(" cpu:").append(this.mCpu).append(" ]");
        return sb.toString();
    }

    public String topBroadcastToString() {
        String str = "";
        StringBuilder sb = new StringBuilder(256);
        sb.append("[top ").append(this.mCallerPackage).append("  ").append(this.mPid).append("  ").append(this.mUid).append("  ").append(this.mBroadcastName).append("  ").append(this.mBroadcastType).append("  ").append(this.mCount).append("  ").append(isFg(this.mForeground)).append(" ]");
        return sb.toString();
    }

    public String isFg(boolean foreground) {
        String fg = "";
        if (foreground) {
            return "FG";
        }
        return "BG";
    }
}
