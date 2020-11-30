package com.android.server.power;

import java.util.ArrayList;

public abstract class OppoPowerManagerInternal {
    public abstract String getScreenOnReason();

    public abstract String getShortScreenOnStatus();

    public abstract String getSleepReason();

    public abstract int[] getWakeLockedPids();

    public abstract void gotoSleepWhenScreenOnBlocked(String str);

    public abstract boolean isBiometricsWakeUpReason(String str);

    public abstract boolean isBlockedByFace();

    public abstract boolean isBlockedByFingerprint();

    public abstract boolean isFaceWakeUpReason(String str);

    public abstract boolean isFingerprintWakeUpReason(String str);

    public abstract boolean isStartGoToSleep();

    public abstract void notifyMotionGameAppForeground(String str, boolean z);

    public abstract int pendingJobs(int i, boolean z, boolean z2, String str);

    public abstract int restoreJobs(int i, boolean z, boolean z2, String str);

    public abstract int setWakeLockStateForHans(int i, boolean z);

    public abstract void unblockScreenOn(String str);

    public abstract void wakeUpAndBlockScreenOn(String str);

    public ArrayList<Integer> getMusicPlayerList() {
        return new ArrayList<>();
    }
}
