package com.android.server.biometrics.fingerprint.power;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import com.android.server.LocalServices;
import com.android.server.biometrics.BiometricWakeupManagerService;
import com.android.server.biometrics.BiometricsManagerInternal;
import com.android.server.biometrics.face.health.HealthState;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener;

public class FingerprintPowerManager {
    private static final int MSG_GOTOSLEEP = 1;
    public static final String PROP_NAME_OPEN_ALL_FRAMES = "debug.screenoff.unlock";
    private static final String TAG = "FingerprintService.PowerManager";
    private static Object sMutex = new Object();
    private static FingerprintPowerManager sSingleInstance;
    private BiometricsManagerInternal mBiometricsManager = ((BiometricsManagerInternal) LocalServices.getService(BiometricsManagerInternal.class));
    /* access modifiers changed from: private */
    public Context mContext;
    private Handler mHandler = new Handler() {
        /* class com.android.server.biometrics.fingerprint.power.FingerprintPowerManager.AnonymousClass1 */

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                FingerprintPowerManager.this.gotoSleep();
            }
        }
    };
    private LocalService mLocalService = new LocalService();
    /* access modifiers changed from: private */
    public IFingerprintSensorEventListener mUnlockController;

    public FingerprintPowerManager(Context context, IFingerprintSensorEventListener controller) {
        this.mContext = context;
        this.mUnlockController = controller;
    }

    public static void initFPM(Context c, IFingerprintSensorEventListener controller) {
        getFingerprintPowerManager(c, controller);
    }

    public static FingerprintPowerManager getFingerprintPowerManager(Context c, IFingerprintSensorEventListener controller) {
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new FingerprintPowerManager(c, controller);
            }
        }
        return sSingleInstance;
    }

    public static FingerprintPowerManager getFingerprintPowerManager() {
        return sSingleInstance;
    }

    public boolean isScreenOFF() {
        return ((PowerManager) this.mContext.getSystemService("power")).getScreenState() == 0;
    }

    /* access modifiers changed from: private */
    public void calculateTime(String mode, long interval) {
    }

    public void userActivity() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.biometrics.fingerprint.power.FingerprintPowerManager.AnonymousClass2 */

            public void run() {
                long now = SystemClock.uptimeMillis();
                LogUtil.d(FingerprintPowerManager.TAG, HealthState.USERACTIVITY);
                ((PowerManager) FingerprintPowerManager.this.mContext.getSystemService("power")).userActivity(now, 2, 0);
            }
        });
    }

    public void unblockScreenOn(String screenOnReason, long delay) {
        long startTime = SystemClock.uptimeMillis();
        LogUtil.d(TAG, "unblockScreenOn, delay = " + delay);
        Trace.traceBegin(4, "FingerprintScreenOn");
        this.mBiometricsManager.unblockScreenOn("FingerprintService", screenOnReason, delay);
        Trace.traceEnd(4);
        calculateTime("unblockScreenOn", SystemClock.uptimeMillis() - startTime);
    }

    public void wakeupNormal() {
        long startTime = SystemClock.uptimeMillis();
        PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
        Trace.traceBegin(4, "FingerprintScreenOnNormal");
        if (pm != null) {
            LogUtil.d(TAG, "wakeupNormal");
            pm.wakeUp(SystemClock.uptimeMillis());
        }
        Trace.traceEnd(4);
        calculateTime("wakeupNormal", SystemClock.uptimeMillis() - startTime);
    }

    public void blockScreenOn(String wakeUpreason) {
        long startTime = SystemClock.uptimeMillis();
        LogUtil.d(TAG, "blockScreenOn wakeUpreason = " + wakeUpreason);
        this.mBiometricsManager.blockScreenOn("FingerprintService", wakeUpreason);
        calculateTime("blockScreenOn", SystemClock.uptimeMillis() - startTime);
    }

    public void gotoSleep() {
        LogUtil.d(TAG, "gotoSleep, isScreenOFF = " + isScreenOFF());
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
        if (isScreenOFF()) {
            LogUtil.d(TAG, "gotoSleepWhenScreenOnBlocked");
            this.mBiometricsManager.gotoSleepWhenScreenOnBlocked("FingerprintService", BiometricWakeupManagerService.GO_TO_SLEEP_BY_AUTHENTICATE_SUSPEND);
        }
    }

    public void gotoSleepDelay(int delay) {
        LogUtil.d(TAG, "gotoSleepDelay, isScreenOFF = " + isScreenOFF() + "  delay = " + delay);
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
        this.mHandler.sendEmptyMessageDelayed(1, (long) delay);
    }

    public void cancelGotoSleep(String reason) {
        if (this.mHandler.hasMessages(1)) {
            LogUtil.d(TAG, "Cancel gotoSleep due to " + reason);
            this.mHandler.removeMessages(1);
        }
    }

    public void openAllFramesDrawForKeyguard() {
        SystemProperties.set(PROP_NAME_OPEN_ALL_FRAMES, "1");
        LogUtil.d(TAG, "openAllFramesDrawForKeyguard, " + SystemProperties.getBoolean(PROP_NAME_OPEN_ALL_FRAMES, false));
    }

    public void clearAllFramesDrawForKeyguard() {
        SystemProperties.set(PROP_NAME_OPEN_ALL_FRAMES, "0");
        LogUtil.d(TAG, "clearAllFramesDrawForKeyguard, " + SystemProperties.getBoolean(PROP_NAME_OPEN_ALL_FRAMES, false));
    }

    public LocalService getFingerprintLocalService() {
        return this.mLocalService;
    }

    /* access modifiers changed from: private */
    public final class LocalService extends FingerprintInternal {
        private LocalService() {
        }

        @Override // com.android.server.biometrics.fingerprint.power.FingerprintInternal
        public void notifyPowerKeyPressed() {
            long startTime = SystemClock.uptimeMillis();
            FingerprintPowerManager.this.mUnlockController.dispatchPowerKeyPressed();
            FingerprintPowerManager.this.clearAllFramesDrawForKeyguard();
            FingerprintPowerManager.this.calculateTime("notifyPowerKeyPressed", SystemClock.uptimeMillis() - startTime);
        }

        @Override // com.android.server.biometrics.fingerprint.power.FingerprintInternal
        public boolean notifyPowerKeyPressed(String reason) {
            SystemClock.uptimeMillis();
            return FingerprintPowerManager.this.mUnlockController.dispatchPowerKeyPressedForPressTouch(reason);
        }

        @Override // com.android.server.biometrics.fingerprint.power.FingerprintInternal
        public void onWakeUpFinish() {
            FingerprintPowerManager.this.clearAllFramesDrawForKeyguard();
            FingerprintPowerManager.this.mUnlockController.onWakeUpFinish();
        }

        @Override // com.android.server.biometrics.fingerprint.power.FingerprintInternal
        public void onGoToSleep() {
            FingerprintPowerManager.this.mUnlockController.onGoToSleep();
        }

        @Override // com.android.server.biometrics.fingerprint.power.FingerprintInternal
        public void onWakeUp(String wakeupReason) {
            FingerprintPowerManager.this.mUnlockController.onWakeUp(wakeupReason);
        }

        @Override // com.android.server.biometrics.fingerprint.power.FingerprintInternal
        public void onGoToSleepFinish() {
            FingerprintPowerManager.this.mUnlockController.onGoToSleepFinish();
        }

        @Override // com.android.server.biometrics.fingerprint.power.FingerprintInternal
        public void onHomeKeyDown() {
            FingerprintPowerManager.this.mUnlockController.dispatchHomeKeyDown();
        }

        @Override // com.android.server.biometrics.fingerprint.power.FingerprintInternal
        public void onHomeKeyUp() {
            FingerprintPowerManager.this.mUnlockController.dispatchHomeKeyUp();
        }

        @Override // com.android.server.biometrics.fingerprint.power.FingerprintInternal
        public void onScreenOnUnBlockedByOther(String unBlockedReason) {
            FingerprintPowerManager.this.mUnlockController.onScreenOnUnBlockedByOther(unBlockedReason);
        }

        @Override // com.android.server.biometrics.fingerprint.power.FingerprintInternal
        public void onLightScreenOnFinish() {
            FingerprintPowerManager.this.mUnlockController.onLightScreenOnFinish();
        }
    }
}
