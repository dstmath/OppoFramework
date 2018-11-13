package com.android.internal.telephony;

import android.telecom.Log;

public class CallForwardInfo {
    private static final String TAG = "CallForwardInfo";
    public String number;
    public int reason;
    public int serviceClass;
    public int status;
    public int timeSeconds;
    public int toa;

    public String toString() {
        return "[CallForwardInfo: status=" + (this.status == 0 ? " not active " : " active ") + ", reason= " + this.reason + ", serviceClass= " + this.serviceClass + ", timeSec= " + this.timeSeconds + " seconds" + ", number=" + Log.pii(this.number) + "]";
    }
}
