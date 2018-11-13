package com.android.server.fingerprint.power;

public abstract class FingerprintInternal {
    public abstract void notifyPowerKeyPressed();

    public abstract void onGoToSleep();

    public abstract void onGoToSleepFinish();

    public abstract void onHomeKeyDown();

    public abstract void onHomeKeyUp();

    public abstract void onLightScreenOnFinish();

    public abstract void onScreenOnUnBlockedByOther(String str);

    public abstract void onWakeUp(String str);

    public abstract void onWakeUpFinish();
}
