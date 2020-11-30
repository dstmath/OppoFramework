package com.android.server.connectivity.networkrecovery;

/* access modifiers changed from: package-private */
public class DnsStatisticsResult {
    public int mDnsSelfRecoveryConnectedCount = 0;
    public int mDnsSelfRecoveryDnsupdateCount = 0;
    public int mDnsSelfRecoveryInternetInvailedCount = 0;
    public int mDnsSelfRecoverySuccessCount = 0;
    public int mDnsSelfRecoveryTotalCount = 0;
    public int mDoubleDnsInvailed = 0;
    public int mFristDnsInvailed = 0;
    public int mQuadraDnsInvailed = 0;
    public int mTripleDnsInvailed = 0;

    DnsStatisticsResult() {
    }

    public void resetDnsStatistics() {
        this.mDnsSelfRecoveryTotalCount = 0;
        this.mDnsSelfRecoverySuccessCount = 0;
        this.mDnsSelfRecoveryDnsupdateCount = 0;
        this.mDnsSelfRecoveryInternetInvailedCount = 0;
        this.mDnsSelfRecoveryConnectedCount = 0;
        this.mFristDnsInvailed = 0;
        this.mDoubleDnsInvailed = 0;
        this.mTripleDnsInvailed = 0;
        this.mQuadraDnsInvailed = 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<total-connect-internetinvailed-updatedns|" + Integer.toString(this.mDnsSelfRecoveryTotalCount) + ":" + Integer.toString(this.mDnsSelfRecoveryConnectedCount) + ":" + Integer.toString(this.mDnsSelfRecoveryInternetInvailedCount) + ":" + Integer.toString(this.mDnsSelfRecoveryDnsupdateCount) + "><frist-double-triple-quadra|:" + Integer.toString(this.mFristDnsInvailed) + ":" + Integer.toString(this.mDoubleDnsInvailed) + ":" + Integer.toString(this.mTripleDnsInvailed) + ":" + Integer.toString(this.mQuadraDnsInvailed) + ">");
        return sb.toString();
    }
}
