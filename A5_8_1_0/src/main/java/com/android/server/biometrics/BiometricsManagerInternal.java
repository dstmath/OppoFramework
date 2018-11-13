package com.android.server.biometrics;

public abstract class BiometricsManagerInternal {
    public abstract void blockScreenOn(String str, String str2);

    public abstract void cancelFaceAuthenticateWhileScreenOff(String str, String str2);

    public abstract void gotoSleepWhenScreenOnBlocked(String str, String str2);

    public abstract boolean isFaceAutoUnlockEnabled();

    public abstract void notifyPowerKeyPressed();

    public abstract void onAnimateScreenBrightness(int i);

    public abstract void onGoToSleep();

    public abstract void onGoToSleepFinish();

    public abstract void onScreenOnUnBlockedByOther(String str);

    public abstract void onWakeUp(String str);

    public abstract void onWakeUpFinish();

    public abstract void setKeyguardOpaque(String str, String str2);

    public abstract void setKeyguardTransparent(String str, String str2);

    public abstract void unblockScreenOn(String str, String str2, long j);
}
