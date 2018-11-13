package com.android.server.fingerprint.detect.data;

import com.android.server.fingerprint.detect.FingerprintKeyEventType;

public class FingerprintResetData extends FingerprintKeyEventData {
    private int mErrorCode;
    private int mPid;

    public FingerprintResetData(FingerprintKeyEventType fingerprintKeyEventType, long time) {
        super(fingerprintKeyEventType, time);
    }

    public FingerprintResetData(FingerprintKeyEventType fingerprintKeyEventType, long time, int pid) {
        super(fingerprintKeyEventType, time);
        this.mPid = pid;
    }
}
