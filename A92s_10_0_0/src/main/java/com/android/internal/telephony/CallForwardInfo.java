package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.telecom.Log;

public class CallForwardInfo {
    private static final String TAG = "CallForwardInfo";
    @UnsupportedAppUsage
    public String number;
    @UnsupportedAppUsage
    public int reason;
    @UnsupportedAppUsage
    public int serviceClass;
    @UnsupportedAppUsage
    public int status;
    @UnsupportedAppUsage
    public int timeSeconds;
    @UnsupportedAppUsage
    public int toa;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[CallForwardInfo: status=");
        sb.append(this.status == 0 ? " not active " : " active ");
        sb.append(", reason= ");
        sb.append(this.reason);
        sb.append(", serviceClass= ");
        sb.append(this.serviceClass);
        sb.append(", timeSec= ");
        sb.append(this.timeSeconds);
        sb.append(" seconds, number=");
        sb.append(Log.pii(this.number));
        sb.append("]");
        return sb.toString();
    }
}
