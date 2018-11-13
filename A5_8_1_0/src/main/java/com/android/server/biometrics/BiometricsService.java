package com.android.server.biometrics;

import android.content.Context;
import android.os.IBinder.DeathRecipient;
import android.os.Message;
import android.os.PowerManagerInternal;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.view.WindowManagerPolicy;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.biometrics.tool.ExHandler;
import com.android.server.face.FaceService;
import com.android.server.face.power.FaceInternal;
import com.android.server.fingerprint.FingerprintService;
import com.android.server.fingerprint.power.FingerprintInternal;

public class BiometricsService extends SystemService implements DeathRecipient, Monitor {
    public static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String GO_TO_SLEEP_BY_AUTHENTICATE_SUSPEND = "FINGERPRINT_AUTHENTICATE_SUSPEND";
    public static final String KEEP_KEYGUARD_OPAQUE_WHILE_AUTO_UNLOCK = "keepOpaqueWhileAutoUnlock";
    public static final int MSG_TURN_SCREEN_ON_BY_FAIL_RESULT = 1;
    public static final String SET_KEYGUARD_OPAQUE_WHILE_FAIL_UNLOCK = "setOpaqueWhileFailToUnlock";
    public static final String SET_KEYGUARD_OPAQUE_WHILE_WAKE_UP_REASON_NOT_BIOMETRICS = "wakeUpByOther";
    public static final String TAG = "BiometricsService.Main";
    public static final String TAG_NAME = "BiometricsService";
    public static final String UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_FAIL = "unBlockScreenOnByAuthenticateFail";
    public static final String UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_SUCESS = "unBlockScreenOnByAuthenticateSucess";
    public static final String UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_TIMEOUT = "unBlockScreenOnByAuthenticateTimeout";
    public static final String UNBLOCK_SCREEN_ON_BY_ERROR = "unBlockScreenOnByError";
    private int mBrightness = 255;
    private final Context mContext;
    private FaceInternal mFaceInternal = null;
    private FingerprintInternal mFingerprintInternal = null;
    private ExHandler mHandler;
    private Object mLock = new Object();
    private WindowManagerPolicy mPolicy;
    private PowerManagerInternal mPowerManagerInternal;
    private final BiometricsManagerInternal mService = new BiometricsManagerInternal() {
        public void gotoSleepWhenScreenOnBlocked(String srcBiometrics, String reason) {
            BiometricsService.this.mPowerManagerInternal.gotoSleepWhenScreenOnBlocked(reason);
        }

        public void blockScreenOn(String srcBiometrics, String wakeUpReason) {
            BiometricsService.this.mPowerManagerInternal.wakeUpAndBlockScreenOn(wakeUpReason);
        }

        public void unblockScreenOn(String srcBiometrics, String screenOnReason, long delay) {
            if (delay > 0) {
                Message msg = BiometricsService.this.mHandler.obtainMessage(1);
                msg.obj = screenOnReason;
                BiometricsService.this.mHandler.sendMessageDelayed(msg, delay);
                return;
            }
            BiometricsService.this.mPowerManagerInternal.unblockScreenOn(screenOnReason);
        }

        public boolean isFaceAutoUnlockEnabled() {
            if (BiometricsService.this.mFaceInternal != null) {
                return BiometricsService.this.mFaceInternal.isFaceAutoUnlockEnabled();
            }
            return false;
        }

        public void setKeyguardTransparent(String srcBiometrics, String wakeUpReason) {
            if (BiometricsService.this.mPowerManagerInternal.isFingerprintWakeUpReason(wakeUpReason)) {
                Log.d(BiometricsService.TAG, "setKeyguardTransparent wakeUpReason = " + wakeUpReason);
                BiometricsService.this.mHandler.removeMessage(1);
                BiometricsService.this.mPolicy.onWakeUp(wakeUpReason);
            } else if (!BiometricsService.this.mPowerManagerInternal.isFaceWakeUpReason(wakeUpReason)) {
                if (BiometricsService.this.mService.isFaceAutoUnlockEnabled() && BiometricsService.KEEP_KEYGUARD_OPAQUE_WHILE_AUTO_UNLOCK.equals(wakeUpReason) && FaceService.TAG_NAME.equals(srcBiometrics)) {
                    Log.d(BiometricsService.TAG, "setKeyguardTransparent wakeUpReason = " + wakeUpReason);
                    BiometricsService.this.mPolicy.onWakeUp(wakeUpReason);
                }
                if (!"android.policy.wakeup.slient".equals(wakeUpReason) && FingerprintService.TAG.equals(srcBiometrics)) {
                    Log.d(BiometricsService.TAG, "setKeyguardTransparent wakeUpReason = " + wakeUpReason);
                    BiometricsService.this.mPolicy.onWakeUp(BiometricsService.SET_KEYGUARD_OPAQUE_WHILE_WAKE_UP_REASON_NOT_BIOMETRICS);
                }
            } else if (BiometricsService.this.mService.isFaceAutoUnlockEnabled()) {
                if (FaceService.TAG_NAME.equals(srcBiometrics)) {
                    Log.d(BiometricsService.TAG, "setKeyguardTransparent wakeUpReason = " + wakeUpReason);
                    BiometricsService.this.mHandler.removeMessage(1);
                    BiometricsService.this.mPolicy.onWakeUp(wakeUpReason);
                }
            } else if (FingerprintService.TAG.equals(srcBiometrics)) {
                Log.d(BiometricsService.TAG, "setKeyguardTransparent wakeUpReason = " + wakeUpReason);
                BiometricsService.this.mPolicy.onWakeUp(wakeUpReason);
            }
        }

        public void setKeyguardOpaque(String srcBiometrics, String reason) {
            Log.d(BiometricsService.TAG, "setKeyguardOpaque reason = " + reason);
            BiometricsService.this.mPolicy.onWakeUp(reason);
        }

        public void cancelFaceAuthenticateWhileScreenOff(String srcBiometrics, String reason) {
            Log.d(BiometricsService.TAG, "cancelFaceAuthenticateWhileScreenOff reason = " + reason);
            BiometricsService.this.mPolicy.onWakeUp(reason);
        }

        public void notifyPowerKeyPressed() {
            if (BiometricsService.this.mFingerprintInternal != null) {
                BiometricsService.this.mFingerprintInternal.notifyPowerKeyPressed();
            }
        }

        public void onAnimateScreenBrightness(int brightness) {
            if (brightness != 0) {
                onWakeUpFinish();
            }
            BiometricsService.this.mBrightness = brightness;
        }

        public void onWakeUp(String wakeupReason) {
            Log.d(BiometricsService.TAG, "onWakeUp wakeupReason = " + wakeupReason);
            BiometricsService.this.mBrightness = 0;
            if (BiometricsService.this.mFingerprintInternal != null) {
                BiometricsService.this.mFingerprintInternal.onWakeUp(wakeupReason);
            }
            if (BiometricsService.this.mService.isFaceAutoUnlockEnabled() && BiometricsService.this.mPowerManagerInternal.isFaceWakeUpReason(wakeupReason)) {
                BiometricsService.this.mFaceInternal.onWakeUp(wakeupReason);
            }
        }

        public void onGoToSleep() {
            Log.d(BiometricsService.TAG, "onGoToSleep");
            if (BiometricsService.this.mFingerprintInternal != null) {
                BiometricsService.this.mFingerprintInternal.onGoToSleep();
            }
            if (BiometricsService.this.mFaceInternal != null) {
                BiometricsService.this.mFaceInternal.onGoToSleep();
            }
        }

        public void onWakeUpFinish() {
            if (BiometricsService.this.mBrightness != 0) {
                Log.e(BiometricsService.TAG, "return mBrightness != 0");
                return;
            }
            Trace.traceBegin(131072, "BiometricsService.Main.onWakeUpFinish");
            Log.d(BiometricsService.TAG, "onWakeUpFinish");
            BiometricsService.this.mBrightness = 1;
            if (BiometricsService.this.mFingerprintInternal != null) {
                BiometricsService.this.mFingerprintInternal.onWakeUpFinish();
            }
            if (BiometricsService.this.mFaceInternal != null) {
                BiometricsService.this.mFaceInternal.onWakeUpFinish();
            }
            Trace.traceEnd(131072);
        }

        public void onGoToSleepFinish() {
            Log.d(BiometricsService.TAG, "onGoToSleepFinish");
            BiometricsService.this.mBrightness = 0;
            if (BiometricsService.this.mFingerprintInternal != null) {
                BiometricsService.this.mFingerprintInternal.onGoToSleepFinish();
            }
            if (BiometricsService.this.mFaceInternal != null) {
                BiometricsService.this.mFaceInternal.onGoToSleepFinish();
            }
        }

        public void onScreenOnUnBlockedByOther(String unBlockedReason) {
            Log.d(BiometricsService.TAG, "onScreenOnUnBlockedByOther, unBlockedReason = " + unBlockedReason);
            if (BiometricsService.this.mFingerprintInternal != null) {
                BiometricsService.this.mFingerprintInternal.onScreenOnUnBlockedByOther(unBlockedReason);
            }
            if (BiometricsService.this.mService.isFaceAutoUnlockEnabled() && BiometricsService.this.mPowerManagerInternal.isFaceWakeUpReason(unBlockedReason)) {
                BiometricsService.this.mFaceInternal.onScreenOnUnBlockedByOther(unBlockedReason);
            }
        }
    };
    private final ServiceThread mServiceThread;
    private boolean mSupportFace = false;
    private boolean mSupportFingerprint = false;
    private boolean mSystemReady = false;

    private void initHandler() {
        this.mHandler = new ExHandler(this.mServiceThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Log.d(BiometricsService.TAG, "trun screen on by fail result");
                        BiometricsService.this.mPowerManagerInternal.unblockScreenOn((String) msg.obj);
                        return;
                    default:
                        Log.w(BiometricsService.TAG, "Unknown message:" + msg.what);
                        return;
                }
            }
        };
    }

    public BiometricsService(Context context) {
        super(context);
        Log.d(TAG, TAG_NAME);
        this.mContext = context;
        Watchdog.getInstance().addMonitor(this);
        this.mServiceThread = new ServiceThread(TAG, -2, true);
        this.mServiceThread.start();
        initHandler();
        this.mPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
        this.mPowerManagerInternal = (PowerManagerInternal) getLocalService(PowerManagerInternal.class);
        this.mSupportFace = context.getPackageManager().hasSystemFeature("oppo.hardware.face.support");
        this.mSupportFingerprint = context.getPackageManager().hasSystemFeature("android.hardware.fingerprint");
    }

    public void onStart() {
        Log.d(TAG, "onStart");
        publishLocalService(BiometricsManagerInternal.class, this.mService);
    }

    public void binderDied() {
        Log.d(TAG, "binderDied");
    }

    public void monitor() {
    }

    public void systemReady() {
        Log.d(TAG, "systemReady");
        this.mSystemReady = true;
        if (this.mSupportFace) {
            this.mFaceInternal = (FaceInternal) LocalServices.getService(FaceInternal.class);
        }
        if (this.mSupportFingerprint) {
            this.mFingerprintInternal = (FingerprintInternal) LocalServices.getService(FingerprintInternal.class);
        }
    }
}
