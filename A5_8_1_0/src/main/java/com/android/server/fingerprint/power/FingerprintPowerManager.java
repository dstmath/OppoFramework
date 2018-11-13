package com.android.server.fingerprint.power;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.biometrics.BiometricsManagerInternal;
import com.android.server.biometrics.BiometricsService;
import com.android.server.fingerprint.FingerprintService;
import com.android.server.fingerprint.util.LogUtil;
import com.android.server.fingerprint.wakeup.IFingerprintSensorEventListener;

public class FingerprintPowerManager {
    public static final String PROP_NAME_OPEN_ALL_FRAMES = "debug.screenoff.unlock";
    private static final String TAG = "FingerprintService.PowerManager";
    private static Object sMutex = new Object();
    private static FingerprintPowerManager sSingleInstance;
    private BiometricsManagerInternal mBiometricsManager = ((BiometricsManagerInternal) LocalServices.getService(BiometricsManagerInternal.class));
    private Context mContext;
    private Handler mHandler = new Handler();
    private LocalService mLocalService = new LocalService(this, null);
    private IFingerprintSensorEventListener mUnlockController;

    private final class LocalService extends FingerprintInternal {
        /* synthetic */ LocalService(FingerprintPowerManager this$0, LocalService -this1) {
            this();
        }

        private LocalService() {
        }

        public void notifyPowerKeyPressed() {
            long startTime = SystemClock.uptimeMillis();
            FingerprintPowerManager.this.mUnlockController.dispatchPowerKeyPressed();
            FingerprintPowerManager.this.clearAllFramesDrawForKeyguard();
            FingerprintPowerManager.this.calculateTime("notifyPowerKeyPressed", SystemClock.uptimeMillis() - startTime);
        }

        public void onWakeUpFinish() {
            FingerprintPowerManager.this.clearAllFramesDrawForKeyguard();
            FingerprintPowerManager.this.mUnlockController.onWakeUpFinish();
        }

        public void onGoToSleep() {
            FingerprintPowerManager.this.mUnlockController.onGoToSleep();
        }

        public void onWakeUp(String wakeupReason) {
            FingerprintPowerManager.this.mUnlockController.onWakeUp(wakeupReason);
        }

        public void onGoToSleepFinish() {
            FingerprintPowerManager.this.mUnlockController.onGoToSleepFinish();
        }

        public void onHomeKeyDown() {
            FingerprintPowerManager.this.mUnlockController.dispatchHomeKeyDown();
        }

        public void onHomeKeyUp() {
            FingerprintPowerManager.this.mUnlockController.dispatchHomeKeyUp();
        }

        public void onScreenOnUnBlockedByOther(String unBlockedReason) {
            FingerprintPowerManager.this.mUnlockController.onScreenOnUnBlockedByOther(unBlockedReason);
        }

        public void onLightScreenOnFinish() {
            FingerprintPowerManager.this.mUnlockController.onLightScreenOnFinish();
        }
    }

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
        if (((PowerManager) this.mContext.getSystemService("power")).getScreenState() == 0) {
            return true;
        }
        return false;
    }

    private void calculateTime(String mode, long interval) {
    }

    public void userActivity() {
        this.mHandler.post(new Runnable() {
            public void run() {
                PowerManager pm = (PowerManager) FingerprintPowerManager.this.mContext.getSystemService("power");
                long now = SystemClock.uptimeMillis();
                LogUtil.d(FingerprintPowerManager.TAG, "userActivity");
                pm.userActivity(now, 2, 0);
            }
        });
    }

    public void unblockScreenOn(String screenOnReason, long delay) {
        long startTime = SystemClock.uptimeMillis();
        LogUtil.d(TAG, "unblockScreenOn, delay = " + delay);
        Trace.traceBegin(4, "FingerprintScreenOn");
        this.mBiometricsManager.unblockScreenOn(FingerprintService.TAG, screenOnReason, delay);
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
        this.mBiometricsManager.blockScreenOn(FingerprintService.TAG, wakeUpreason);
        calculateTime("blockScreenOn", SystemClock.uptimeMillis() - startTime);
    }

    public void gotoSleep() {
        LogUtil.d(TAG, "gotoSleep, isScreenOFF = " + isScreenOFF());
        if (isScreenOFF()) {
            LogUtil.d(TAG, "gotoSleepWhenScreenOnBlocked");
            this.mBiometricsManager.gotoSleepWhenScreenOnBlocked(FingerprintService.TAG, BiometricsService.GO_TO_SLEEP_BY_AUTHENTICATE_SUSPEND);
        }
    }

    public void openAllFramesDrawForKeyguard() {
        SystemProperties.set(PROP_NAME_OPEN_ALL_FRAMES, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        LogUtil.d(TAG, "openAllFramesDrawForKeyguard, " + SystemProperties.getBoolean(PROP_NAME_OPEN_ALL_FRAMES, false));
    }

    public void clearAllFramesDrawForKeyguard() {
        SystemProperties.set(PROP_NAME_OPEN_ALL_FRAMES, "0");
        LogUtil.d(TAG, "clearAllFramesDrawForKeyguard, " + SystemProperties.getBoolean(PROP_NAME_OPEN_ALL_FRAMES, false));
    }

    public LocalService getFingerprintLocalService() {
        return this.mLocalService;
    }
}
