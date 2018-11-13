package com.android.server.face.power;

public abstract class FaceInternal {
    public abstract boolean isFaceAutoUnlockEnabled();

    public abstract void onGoToSleep();

    public abstract void onGoToSleepFinish();

    public abstract void onScreenOnUnBlockedByOther(String str);

    public abstract void onWakeUp(String str);

    public abstract void onWakeUpFinish();
}
