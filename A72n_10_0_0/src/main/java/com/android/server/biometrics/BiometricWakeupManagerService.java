package com.android.server.biometrics;

import android.content.Context;
import android.os.Message;
import android.os.PowerManagerInternal;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.biometrics.face.power.FaceInternal;
import com.android.server.biometrics.fingerprint.power.FingerprintInternal;
import com.android.server.biometrics.fingerprint.tool.ExHandler;
import com.android.server.policy.OppoBasePhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.power.OppoPowerManagerInternal;
import com.color.util.ColorTypeCastingHelper;

public class BiometricWakeupManagerService {
    public static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String GO_TO_SLEEP_BY_AUTHENTICATE_SUSPEND = "FINGERPRINT_AUTHENTICATE_SUSPEND";
    public static final String KEEP_KEYGUARD_OPAQUE_WHILE_AUTO_UNLOCK = "keepOpaqueWhileAutoUnlock";
    public static final int MSG_TURN_SCREEN_ON_BY_FAIL_RESULT = 1;
    public static final String SET_KEYGUARD_OPAQUE_WHILE_FAIL_UNLOCK = "setOpaqueWhileFailToUnlock";
    public static final String SET_KEYGUARD_OPAQUE_WHILE_WAKE_UP_REASON_NOT_BIOMETRICS = "wakeUpByOther";
    public static final String TAG = "BiometricWakeupManagerService";
    public static final String TAG_NAME_FROM_FACESERVICE = "FaceService";
    public static final String TAG_NAME_FROM_FINGERPRINTSERVICE = "FingerprintService";
    public static final String UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_FAIL = "unBlockScreenOnByAuthenticateFail";
    public static final String UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_SUCESS = "unBlockScreenOnByAuthenticateSucess";
    public static final String UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_TIMEOUT = "unBlockScreenOnByAuthenticateTimeout";
    public static final String UNBLOCK_SCREEN_ON_BY_CAMERA_TIMEOUT = "unBlockScreenOnByCameraTimeout";
    public static final String UNBLOCK_SCREEN_ON_BY_ERROR = "unBlockScreenOnByError";
    public static final String UNBLOCK_SCREEN_ON_BY_FACE_AUTHENTICATE_FAIL = "unBlockScreenOnByFaceAuthenticateFail";
    public static final String UNBLOCK_SCREEN_ON_BY_SIDE_FINGERPRINT_POWER = "unBlockScreenOnBySideFingerprintPower";
    private static Object sMutex = new Object();
    private static BiometricWakeupManagerService sSingleInstance;
    private String mBlockReason = "";
    private int mBrightness = 255;
    private final Context mContext;
    private FaceInternal mFaceInternal = null;
    private FingerprintInternal mFingerprintInternal = null;
    private ExHandler mHandler;
    private LocalBiometricsService mLocalService;
    private Object mLock = new Object();
    private OppoBasePhoneWindowManager mOppoBasePhoneWinMgr = null;
    private OppoPowerManagerInternal mOppoLocalPowerManager = null;
    private WindowManagerPolicy mPolicy = null;
    private PowerManagerInternal mPowerManagerInternal;
    private final ServiceThread mServiceThread;
    private boolean mSupportFace = false;
    private boolean mSupportFingerprint = false;
    private boolean mSystemReady = false;

    private void initHandler() {
        this.mHandler = new ExHandler(this.mServiceThread.getLooper()) {
            /* class com.android.server.biometrics.BiometricWakeupManagerService.AnonymousClass1 */

            @Override // com.android.server.biometrics.fingerprint.tool.ExHandler
            public void handleMessage(Message msg) {
                if (msg.what != 1) {
                    Log.w(BiometricWakeupManagerService.TAG, "Unknown message:" + msg.what);
                    return;
                }
                Log.d(BiometricWakeupManagerService.TAG, "trun screen on by fail result");
                if (BiometricWakeupManagerService.this.mOppoLocalPowerManager != null) {
                    BiometricWakeupManagerService.this.mOppoLocalPowerManager.unblockScreenOn((String) msg.obj);
                } else {
                    Log.e(BiometricWakeupManagerService.TAG, "trun screen on by fail result: failed for mOppoLocalPowerManager uninit.");
                }
            }
        };
    }

    private BiometricWakeupManagerService(Context context) {
        Log.d(TAG, TAG);
        this.mContext = context;
        this.mServiceThread = new ServiceThread(TAG, -2, true);
        this.mServiceThread.start();
        initHandler();
        this.mPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
        this.mOppoBasePhoneWinMgr = typeCastToParent(this.mPolicy);
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mSupportFace = context.getPackageManager().hasSystemFeature("android.hardware.biometrics.face");
        this.mSupportFingerprint = context.getPackageManager().hasSystemFeature("android.hardware.fingerprint");
        this.mLocalService = new LocalBiometricsService();
    }

    public static BiometricWakeupManagerService getBiometricWakeupManagerService(Context context) {
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new BiometricWakeupManagerService(context);
            }
        }
        return sSingleInstance;
    }

    public static BiometricWakeupManagerService getBiometricWakeupManagerService() {
        return sSingleInstance;
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
        getOppoPowermanagerInternal();
    }

    public LocalBiometricsService getBiometricsManagerLocalService() {
        return this.mLocalService;
    }

    /* access modifiers changed from: private */
    public final class LocalBiometricsService extends BiometricsManagerInternal {
        private LocalBiometricsService() {
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void gotoSleepWhenScreenOnBlocked(String srcBiometrics, String reason) {
            Log.d(BiometricWakeupManagerService.TAG, "gotoSleepWhenScreenOnBlocked reason = " + reason + " mBlockReason = " + BiometricWakeupManagerService.this.mBlockReason);
            if ((!BiometricWakeupManagerService.GO_TO_SLEEP_BY_AUTHENTICATE_SUSPEND.equals(reason) || "android.service.fingerprint:WAKEUP".equals(BiometricWakeupManagerService.this.mBlockReason)) && BiometricWakeupManagerService.this.mOppoLocalPowerManager != null) {
                BiometricWakeupManagerService.this.mOppoLocalPowerManager.gotoSleepWhenScreenOnBlocked(reason);
            }
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void blockScreenOn(String srcBiometrics, String wakeUpReason) {
            Log.d(BiometricWakeupManagerService.TAG, "blockScreenOn wakeUpReason = " + wakeUpReason);
            BiometricWakeupManagerService.this.mBlockReason = wakeUpReason;
            if (BiometricWakeupManagerService.this.mOppoLocalPowerManager != null) {
                BiometricWakeupManagerService.this.mOppoLocalPowerManager.wakeUpAndBlockScreenOn(wakeUpReason);
            }
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void unblockScreenOn(String srcBiometrics, String screenOnReason, long delay) {
            if (delay > 0) {
                Message msg = BiometricWakeupManagerService.this.mHandler.obtainMessage(1);
                msg.obj = screenOnReason;
                BiometricWakeupManagerService.this.mHandler.sendMessageDelayed(msg, delay);
            } else if (BiometricWakeupManagerService.this.mOppoLocalPowerManager != null) {
                BiometricWakeupManagerService.this.mOppoLocalPowerManager.unblockScreenOn(screenOnReason);
            }
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public boolean isFaceAutoUnlockEnabled() {
            if (BiometricWakeupManagerService.this.mFaceInternal != null) {
                return BiometricWakeupManagerService.this.mFaceInternal.isFaceAutoUnlockEnabled();
            }
            return false;
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void setKeyguardTransparent(String srcBiometrics, String wakeUpReason) {
            if (BiometricWakeupManagerService.this.mOppoLocalPowerManager != null && BiometricWakeupManagerService.this.mOppoLocalPowerManager.isFingerprintWakeUpReason(wakeUpReason)) {
                Log.d(BiometricWakeupManagerService.TAG, "setKeyguardTransparent wakeUpReason = " + wakeUpReason);
                BiometricWakeupManagerService.this.mHandler.removeMessage(1);
                if (BiometricWakeupManagerService.this.mOppoLocalPowerManager == null || !BiometricWakeupManagerService.this.mOppoLocalPowerManager.isBlockedByFace()) {
                    BiometricWakeupManagerService.this.requestOnWakeUp(wakeUpReason);
                } else {
                    BiometricWakeupManagerService.this.requestOnWakeUp("android.service.fingerprint:BLOCKED_BY_FACE");
                }
            } else if (BiometricWakeupManagerService.this.mOppoLocalPowerManager == null || !BiometricWakeupManagerService.this.mOppoLocalPowerManager.isFaceWakeUpReason(wakeUpReason)) {
                if (isFaceAutoUnlockEnabled() && BiometricWakeupManagerService.KEEP_KEYGUARD_OPAQUE_WHILE_AUTO_UNLOCK.equals(wakeUpReason) && "FaceService".equals(srcBiometrics)) {
                    Log.d(BiometricWakeupManagerService.TAG, "setKeyguardTransparent wakeUpReason = " + wakeUpReason);
                    BiometricWakeupManagerService.this.requestOnWakeUp(wakeUpReason);
                }
                if (!"android.policy.wakeup.slient".equals(wakeUpReason) && "FingerprintService".equals(srcBiometrics)) {
                    Log.d(BiometricWakeupManagerService.TAG, "setKeyguardTransparent wakeUpReason = " + wakeUpReason);
                    BiometricWakeupManagerService.this.requestOnWakeUp(BiometricWakeupManagerService.SET_KEYGUARD_OPAQUE_WHILE_WAKE_UP_REASON_NOT_BIOMETRICS);
                }
            } else if (isFaceAutoUnlockEnabled()) {
                if ("FaceService".equals(srcBiometrics)) {
                    Log.d(BiometricWakeupManagerService.TAG, "setKeyguardTransparent wakeUpReason = " + wakeUpReason);
                    BiometricWakeupManagerService.this.mHandler.removeMessage(1);
                    BiometricWakeupManagerService.this.requestOnWakeUp(wakeUpReason);
                }
            } else if ("FingerprintService".equals(srcBiometrics)) {
                Log.d(BiometricWakeupManagerService.TAG, "setKeyguardTransparent wakeUpReason = " + wakeUpReason);
                BiometricWakeupManagerService.this.requestOnWakeUp(wakeUpReason);
            }
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void setKeyguardOpaque(String srcBiometrics, String reason) {
            Log.d(BiometricWakeupManagerService.TAG, "setKeyguardOpaque reason = " + reason);
            BiometricWakeupManagerService.this.requestOnWakeUp(reason);
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void cancelFaceAuthenticateWhileScreenOff(String srcBiometrics, String reason) {
            Log.d(BiometricWakeupManagerService.TAG, "cancelFaceAuthenticateWhileScreenOff reason = " + reason);
            BiometricWakeupManagerService.this.requestOnWakeUp(reason);
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public boolean isFaceFingerprintCombineUnlockEnabled() {
            Log.d(BiometricWakeupManagerService.TAG, "isFaceFingerprintCombineUnlockEnabled");
            if (BiometricWakeupManagerService.this.mFaceInternal != null) {
                return BiometricWakeupManagerService.this.mFaceInternal.isFaceFingerprintCombineUnlockEnabled();
            }
            return false;
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public boolean isOpticalFingerprintSupport() {
            Log.d(BiometricWakeupManagerService.TAG, "isOpticalFingerprintSupport");
            if (BiometricWakeupManagerService.this.mFaceInternal != null) {
                return BiometricWakeupManagerService.this.mFaceInternal.isOpticalFingerprintSupport();
            }
            return false;
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void notifyPowerKeyPressed() {
            if (BiometricWakeupManagerService.this.mFingerprintInternal != null) {
                BiometricWakeupManagerService.this.mFingerprintInternal.notifyPowerKeyPressed();
            }
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public boolean notifyPowerKeyPressed(String reason) {
            if (BiometricWakeupManagerService.this.mFingerprintInternal != null) {
                return BiometricWakeupManagerService.this.mFingerprintInternal.notifyPowerKeyPressed(reason);
            }
            return false;
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void onAnimateScreenBrightness(int brightness) {
            if (brightness != 0) {
                onWakeUpFinish();
            }
            BiometricWakeupManagerService.this.mBrightness = brightness;
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void onWakeUp(String wakeupReason) {
            Log.d(BiometricWakeupManagerService.TAG, "onWakeUp wakeupReason = " + wakeupReason);
            BiometricWakeupManagerService.this.mBrightness = 0;
            if (BiometricWakeupManagerService.this.mFingerprintInternal != null) {
                BiometricWakeupManagerService.this.mFingerprintInternal.onWakeUp(wakeupReason);
            }
            if (!isFaceAutoUnlockEnabled()) {
                return;
            }
            if ((BiometricWakeupManagerService.this.mOppoLocalPowerManager != null && BiometricWakeupManagerService.this.mOppoLocalPowerManager.isFaceWakeUpReason(wakeupReason)) || (isFaceFingerprintCombineUnlockEnabled() && isOpticalFingerprintSupport())) {
                BiometricWakeupManagerService.this.mFaceInternal.onWakeUp(wakeupReason);
            }
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void onGoToSleep() {
            Log.d(BiometricWakeupManagerService.TAG, "onGoToSleep");
            if (BiometricWakeupManagerService.this.mFingerprintInternal != null) {
                BiometricWakeupManagerService.this.mFingerprintInternal.onGoToSleep();
            }
            if (BiometricWakeupManagerService.this.mFaceInternal != null) {
                BiometricWakeupManagerService.this.mFaceInternal.onGoToSleep();
            }
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void onWakeUpFinish() {
            if (BiometricWakeupManagerService.this.mBrightness != 0) {
                Log.e(BiometricWakeupManagerService.TAG, "return mBrightness != 0");
                return;
            }
            Trace.traceBegin(131072, "BiometricWakeupManagerService.Main.onWakeUpFinish");
            Log.d(BiometricWakeupManagerService.TAG, "onWakeUpFinish");
            BiometricWakeupManagerService.this.mBrightness = 1;
            if (BiometricWakeupManagerService.this.mFingerprintInternal != null) {
                BiometricWakeupManagerService.this.mFingerprintInternal.onWakeUpFinish();
            }
            if (BiometricWakeupManagerService.this.mFaceInternal != null) {
                BiometricWakeupManagerService.this.mFaceInternal.onWakeUpFinish();
            }
            Trace.traceEnd(131072);
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void onGoToSleepFinish() {
            Log.d(BiometricWakeupManagerService.TAG, "onGoToSleepFinish");
            BiometricWakeupManagerService.this.mBrightness = 0;
            if (BiometricWakeupManagerService.this.mFingerprintInternal != null) {
                BiometricWakeupManagerService.this.mFingerprintInternal.onGoToSleepFinish();
            }
            if (BiometricWakeupManagerService.this.mFaceInternal != null) {
                BiometricWakeupManagerService.this.mFaceInternal.onGoToSleepFinish();
            }
        }

        @Override // com.android.server.biometrics.BiometricsManagerInternal
        public void onScreenOnUnBlockedByOther(String unBlockedReason) {
            Log.d(BiometricWakeupManagerService.TAG, "onScreenOnUnBlockedByOther, unBlockedReason = " + unBlockedReason);
            if (BiometricWakeupManagerService.this.mFingerprintInternal != null) {
                BiometricWakeupManagerService.this.mFingerprintInternal.onScreenOnUnBlockedByOther(unBlockedReason);
            }
            if (isFaceAutoUnlockEnabled() && BiometricWakeupManagerService.this.mOppoLocalPowerManager != null && BiometricWakeupManagerService.this.mOppoLocalPowerManager.isFaceWakeUpReason(unBlockedReason)) {
                BiometricWakeupManagerService.this.mFaceInternal.onScreenOnUnBlockedByOther(unBlockedReason);
            }
        }
    }

    private OppoPowerManagerInternal getOppoPowermanagerInternal() {
        if (this.mOppoLocalPowerManager == null) {
            this.mOppoLocalPowerManager = (OppoPowerManagerInternal) LocalServices.getService(OppoPowerManagerInternal.class);
        }
        return this.mOppoLocalPowerManager;
    }

    private OppoBasePhoneWindowManager typeCastToParent(WindowManagerPolicy winPolicy) {
        return (OppoBasePhoneWindowManager) ColorTypeCastingHelper.typeCasting(OppoBasePhoneWindowManager.class, winPolicy);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestOnWakeUp(String reason) {
        OppoBasePhoneWindowManager oppoBasePhoneWindowManager = this.mOppoBasePhoneWinMgr;
        if (oppoBasePhoneWindowManager != null) {
            oppoBasePhoneWindowManager.onWakeUp(reason);
            return;
        }
        Log.e(TAG, "requestOnWakeUp failed for mOppoBasePhoneWinMgr empty. wakeUpReason:" + reason);
    }
}
