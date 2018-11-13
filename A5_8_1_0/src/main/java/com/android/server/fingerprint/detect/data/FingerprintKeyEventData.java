package com.android.server.fingerprint.detect.data;

import com.android.server.fingerprint.detect.FingerprintKeyEventType;

public class FingerprintKeyEventData {
    private long mKeyEventTime;
    private FingerprintKeyEventType mKeyEventType;

    public FingerprintKeyEventData(FingerprintKeyEventType fingerprintKeyEventType, long time) {
        this.mKeyEventTime = time;
        this.mKeyEventType = fingerprintKeyEventType;
    }

    public long getKeyEventTime() {
        return this.mKeyEventTime;
    }

    public void setKeyEventTime(long keyEventTime) {
        this.mKeyEventTime = keyEventTime;
    }

    public FingerprintKeyEventType getKeyEventType() {
        return this.mKeyEventType;
    }

    public void setKeyEventType(FingerprintKeyEventType keyEventType) {
        this.mKeyEventType = keyEventType;
    }
}
