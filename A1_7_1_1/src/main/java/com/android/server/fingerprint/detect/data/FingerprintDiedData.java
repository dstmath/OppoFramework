package com.android.server.fingerprint.detect.data;

import com.android.server.fingerprint.detect.FingerprintKeyEventType;

public class FingerprintDiedData extends FingerprintKeyEventData {
    private int mErrorCode;
    private int mPid;

    public FingerprintDiedData(FingerprintKeyEventType fingerprintKeyEventType, long time) {
        super(fingerprintKeyEventType, time);
    }

    public FingerprintDiedData(FingerprintKeyEventType fingerprintKeyEventType, long time, int pid) {
        super(fingerprintKeyEventType, time);
        this.mPid = pid;
    }
}
