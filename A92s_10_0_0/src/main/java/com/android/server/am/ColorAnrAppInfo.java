package com.android.server.am;

public class ColorAnrAppInfo {
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
        StringBuilder sb = new StringBuilder(256);
        String isFg = isFg(this.mForeground);
        sb.append("[broadcast ");
        sb.append(this.mProcessName);
        sb.append("  ");
        sb.append(this.mBroadcastName);
        sb.append("  ");
        sb.append(this.mBroadcastType);
        sb.append("  ");
        sb.append(this.isGuardTimeout);
        sb.append("  ");
        sb.append(this.mBroadcastReceiverTime);
        sb.append("  ");
        sb.append(isFg);
        sb.append(" mCallerPkg:");
        sb.append(this.mCallerPackage);
        sb.append(" ]");
        return sb.toString();
    }

    public String broadcastReceiverToString() {
        StringBuilder sb = new StringBuilder(256);
        String isFg = isFg(this.mForeground);
        sb.append("[broadcast ");
        sb.append(this.mProcessName);
        sb.append("  ");
        sb.append(this.mBroadcastName);
        sb.append(" mFirstPro:");
        sb.append(this.mFirstPro);
        sb.append("  ");
        sb.append(this.mCount);
        sb.append("  ");
        sb.append(this.mChangeCount);
        sb.append("  ");
        sb.append(isFg);
        sb.append("  ");
        sb.append(this.mOrdered);
        sb.append(" mCallerPkg:");
        sb.append(this.mCallerPackage);
        sb.append(" ]");
        return sb.toString();
    }

    public String serviceToString() {
        StringBuilder sb = new StringBuilder(256);
        String isFg = isFg(this.mForeground);
        sb.append("[service ");
        sb.append(this.mProcessName);
        sb.append("  ");
        sb.append(this.isGuardTimeout);
        sb.append("  ");
        sb.append(this.mBroadcastReceiverTime);
        sb.append("  ");
        sb.append(isFg);
        sb.append(" cpu:");
        sb.append(this.mCpu);
        sb.append(" ]");
        return sb.toString();
    }

    public String topBroadcastToString() {
        StringBuilder sb = new StringBuilder(256);
        String isFg = isFg(this.mForeground);
        sb.append("[top ");
        sb.append(this.mCallerPackage);
        sb.append("  ");
        sb.append(this.mPid);
        sb.append("  ");
        sb.append(this.mUid);
        sb.append("  ");
        sb.append(this.mBroadcastName);
        sb.append("  ");
        sb.append(this.mBroadcastType);
        sb.append("  ");
        sb.append(this.mCount);
        sb.append("  ");
        sb.append(isFg);
        sb.append(" ]");
        return sb.toString();
    }

    public String isFg(boolean foreground) {
        if (foreground) {
            return "FG";
        }
        return "BG";
    }
}
