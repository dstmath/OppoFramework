package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;

public class RilMessage {
    @UnsupportedAppUsage
    public Object mData;
    @UnsupportedAppUsage
    public int mId;
    public ResultCode mResCode;

    @UnsupportedAppUsage
    public RilMessage(int msgId, String rawData) {
        this.mId = msgId;
        this.mData = rawData;
    }

    public RilMessage(RilMessage other) {
        if (other != null) {
            this.mId = other.mId;
            this.mData = other.mData;
            this.mResCode = other.mResCode;
        }
    }
}
