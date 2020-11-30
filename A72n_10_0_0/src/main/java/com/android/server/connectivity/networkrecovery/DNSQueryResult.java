package com.android.server.connectivity.networkrecovery;

import java.net.InetAddress;

class DNSQueryResult implements Comparable<DNSQueryResult> {
    private long mCostTimeInMs = 0;
    private InetAddress mDnsAddr;
    private boolean mIsSuccess = false;
    public int mSuccessRate = 0;

    DNSQueryResult(long costtime, InetAddress dnsAddr, boolean flag, int successRate) {
        this.mCostTimeInMs = costtime;
        this.mIsSuccess = flag;
        this.mDnsAddr = dnsAddr;
        this.mSuccessRate = successRate;
    }

    public long getCostTime() {
        return this.mCostTimeInMs;
    }

    public void setCostTime(int time) {
        this.mCostTimeInMs = (long) time;
    }

    public void setResult(boolean flag) {
        this.mIsSuccess = flag;
    }

    public boolean getResult() {
        return this.mIsSuccess;
    }

    public InetAddress getDnsAddr() {
        return this.mDnsAddr;
    }

    public int compareTo(DNSQueryResult to) {
        return new Long(getCostTime()).compareTo(Long.valueOf(to.getCostTime()));
    }
}
