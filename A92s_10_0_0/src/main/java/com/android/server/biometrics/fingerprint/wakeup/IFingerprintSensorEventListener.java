package com.android.server.biometrics.fingerprint.wakeup;

import android.hardware.fingerprint.Fingerprint;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public interface IFingerprintSensorEventListener {
    void dispatchAcquired(int i);

    void dispatchAuthForDropHomeKey();

    void dispatchAuthenticated(Fingerprint fingerprint, ArrayList<Byte> arrayList);

    void dispatchHomeKeyDown();

    void dispatchHomeKeyUp();

    void dispatchImageDirtyAuthenticated();

    void dispatchPowerKeyPressed();

    boolean dispatchPowerKeyPressedForPressTouch(String str);

    void dispatchScreenOnTimeOut();

    void dispatchTouchDown();

    void dispatchTouchUp();

    void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    void notifySystemReady();

    void onGoToSleep();

    void onGoToSleepFinish();

    void onLightScreenOnFinish();

    void onProximitySensorChanged(boolean z);

    void onScreenOnUnBlockedByFingerprint(boolean z);

    void onScreenOnUnBlockedByOther(String str);

    void onSettingChanged(String str, boolean z);

    void onWakeUp(String str);

    void onWakeUpFinish();

    void reset();

    void userActivity();
}
