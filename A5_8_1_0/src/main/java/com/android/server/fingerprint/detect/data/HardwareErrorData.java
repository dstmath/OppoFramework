package com.android.server.fingerprint.detect.data;

import com.android.server.fingerprint.detect.FingerprintKeyEventType;

public class HardwareErrorData extends FingerprintKeyEventData {
    private int mErrorCode;
    private int mPid;

    public HardwareErrorData(FingerprintKeyEventType fingerprintKeyEventType, long time) {
        super(fingerprintKeyEventType, time);
    }

    public HardwareErrorData(FingerprintKeyEventType fingerprintKeyEventType, long time, int pid, int errorCode) {
        super(fingerprintKeyEventType, time);
        this.mPid = pid;
        this.mErrorCode = errorCode;
    }
}
