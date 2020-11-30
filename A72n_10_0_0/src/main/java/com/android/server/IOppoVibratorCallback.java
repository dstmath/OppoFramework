package com.android.server;

public interface IOppoVibratorCallback {
    void informVibrationFinished();

    boolean isInputDeviceVibratorsEmpty();

    void onAcquireVibratorWakelock(int i);

    void onDebugFlagSwitch(boolean z);

    void onNoteVibratorOnLocked(int i, long j);

    void onReleaseVibratorWakelock();

    void onVibrationEndLocked(long j);
}
