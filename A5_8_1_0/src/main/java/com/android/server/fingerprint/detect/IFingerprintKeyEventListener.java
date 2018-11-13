package com.android.server.fingerprint.detect;

public interface IFingerprintKeyEventListener {
    void onFingerprintdDied(long j, int i);

    void onFingerprintdResetByHealthMonitor(long j, int i);

    void onHardwareErrorReport(long j, int i, int i2);
}
