package com.android.server.fingerprint.wakeup;

import com.android.server.fingerprint.FingerprintService.AuthenticatedInfo;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface IFingerprintSensorEventListener {
    void dispatchAcquired(int i);

    void dispatchAuthForDropHomeKey();

    void dispatchAuthenticated(AuthenticatedInfo authenticatedInfo);

    void dispatchHomeKeyDown();

    void dispatchHomeKeyUp();

    void dispatchImageDirtyAuthenticated();

    void dispatchPowerKeyPressed();

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
