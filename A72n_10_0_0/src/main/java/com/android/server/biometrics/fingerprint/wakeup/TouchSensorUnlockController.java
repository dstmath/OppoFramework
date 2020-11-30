package com.android.server.biometrics.fingerprint.wakeup;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import com.android.server.LocalServices;
import com.android.server.biometrics.BiometricWakeupManagerService;
import com.android.server.biometrics.BiometricsManagerInternal;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.biometrics.fingerprint.FingerprintUtils;
import com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.biometrics.fingerprint.keyguard.KeyguardPolicy;
import com.android.server.biometrics.fingerprint.power.FingerprintPowerManager;
import com.android.server.biometrics.fingerprint.sensor.ProximitySensorManager;
import com.android.server.biometrics.fingerprint.setting.FingerprintSettings;
import com.android.server.biometrics.fingerprint.tool.ExHandler;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import com.android.server.display.color.DisplayTransformManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TouchSensorUnlockController implements IFingerprintSensorEventListener {
    public static final int GOTO_SLEEP_WAITING_TIME = 500;
    public static final int MSG_GOTO_SLEEP_FROM_WAKE0 = 4;
    public static final int MSG_KEEP_SCREEN_ON = 2;
    public static final int MSG_SEND_TOUCH_DOWN_TIME = 16;
    public static final int MSG_SET_SCREEN_STATE = 5;
    public static final int MSG_TURN_SCREEN_ON_BY_DOUBLE_HOME = 3;
    public static final int MSG_TURN_SCREEN_ON_BY_RIGHT_RESULT = 8;
    public static final int MSG_WAIT_KEYGUARD_HIDDEN = 7;
    public static final int MSG_WAKE_UP_BY_FINGERPRINT = 1;
    public static final int SCREEN_ON_WAITING_TIME = 300;
    private final String TAG = "FingerprintService.TouchSensorUnlockController";
    private BiometricsManagerInternal mBiometricsManager;
    private Context mContext;
    private long mCurrentHomeKeyDownTime = 0;
    private DcsFingerprintStatisticsUtil mDcsStatisticsUtil;
    private boolean mDoubleHomeGestureEnabled = false;
    private int mDoubleTapNum = 0;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private FingerprintService.IUnLocker mIUnLocker;
    private boolean mIsBlackGestureSwitchOpened = false;
    private boolean mIsDoubleHomeKeyDown = false;
    private boolean mIsDoubleHomeSwitchOpened = false;
    private boolean mIsFingerprintUnlockSwitchOpened = false;
    private boolean mIsNearState = false;
    private boolean mIsScreenOff = false;
    private boolean mIsScreenOnUnBlockedByOther = false;
    private boolean mIsWakeUpByFingerprint = false;
    private boolean mIsWakeUpFinishedByOther = true;
    private KeyguardPolicy mKeyguardPolicy;
    private long mLastHomeKeyDownTime = 0;
    private long mLastHomeKeyUpTime = 0;
    private Looper mLooper;
    private boolean mSystemReady = false;
    private long mTouchDownTime;

    public TouchSensorUnlockController(Context context, FingerprintService.IUnLocker unLocker) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "TouchSensorUnlockController construction");
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("TouchSensorUnlockController thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            LogUtil.e("FingerprintService.TouchSensorUnlockController", "mLooper null");
        }
        initHandler();
        this.mIUnLocker = unLocker;
        this.mKeyguardPolicy = KeyguardPolicy.getKeyguardPolicy();
        this.mDcsStatisticsUtil = DcsFingerprintStatisticsUtil.getDcsFingerprintStatisticsUtil(this.mContext);
        this.mBiometricsManager = (BiometricsManagerInternal) LocalServices.getService(BiometricsManagerInternal.class);
    }

    public void turnScreenOnByDoubleHome() {
        this.mLastHomeKeyDownTime = 0;
        this.mLastHomeKeyUpTime = 0;
        if (this.mIsNearState) {
            LogUtil.d("FingerprintService.TouchSensorUnlockController", "turnScreenOnByDoubleHome, near state");
        } else if (this.mIsScreenOff) {
            DcsFingerprintStatisticsUtil dcsFingerprintStatisticsUtil = this.mDcsStatisticsUtil;
            if (dcsFingerprintStatisticsUtil != null) {
                dcsFingerprintStatisticsUtil.sendDoubleHomeTimes();
            }
            this.mDoubleTapNum++;
            LogUtil.d("FingerprintService.TouchSensorUnlockController", "turnScreenOnByDoubleHome, mDoubleTapNum = " + this.mDoubleTapNum);
            getFPMS().blockScreenOn("android.service.fingerprint:DOUBLE_HOME");
            FingerprintUtils.vibrateFingerprintKey(this.mContext);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetScreenState(int state) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "handleSetScreenState ( " + state + " )");
        this.mIUnLocker.setScreenState(state);
    }

    public void stopController() {
        this.mLooper.quit();
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            /* class com.android.server.biometrics.fingerprint.wakeup.TouchSensorUnlockController.AnonymousClass1 */

            @Override // com.android.server.biometrics.fingerprint.tool.ExHandler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    TouchSensorUnlockController.this.getFPMS().blockScreenOn("android.service.fingerprint:WAKEUP");
                } else if (i == 2) {
                    TouchSensorUnlockController.this.getFPMS().userActivity();
                } else if (i == 3) {
                    TouchSensorUnlockController.this.turnScreenOnByDoubleHome();
                } else if (i == 4) {
                    TouchSensorUnlockController.this.getFPMS().gotoSleep();
                } else if (i == 5) {
                    TouchSensorUnlockController.this.handleSetScreenState(msg.arg1);
                } else if (i == 7) {
                    TouchSensorUnlockController.this.handleWaitKeyguardHidden();
                } else if (i == 8) {
                    TouchSensorUnlockController.this.trunScreenOnByRightFinger();
                } else if (i == 16) {
                    TouchSensorUnlockController.this.handleSendTouchDownTime(msg.arg1, ((Long) msg.obj).longValue());
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void notifySystemReady() {
        this.mSystemReady = true;
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchTouchDown() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "dispatchTouchDown, mIsWakeUpFinishedByOther = " + this.mIsWakeUpFinishedByOther);
        this.mTouchDownTime = SystemClock.uptimeMillis();
        getPsensorManager().onAuthenticationStarted(true);
        if (!this.mIsScreenOff) {
            this.mHandler.obtainMessage(16, 1001, 0, Long.valueOf(this.mTouchDownTime)).sendToTarget();
        }
        this.mHandler.removeMessage(4);
        if (this.mIsScreenOff) {
            this.mHandler.sendSyncMessage(1);
        } else {
            this.mHandler.sendSyncMessage(2);
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchHomeKeyDown() {
        LogUtil.w("FingerprintService.TouchSensorUnlockController", "dispatchHomeKeyDown mDoubleHomeGestureEnabled = " + this.mDoubleHomeGestureEnabled);
        if (this.mDoubleHomeGestureEnabled && this.mIsScreenOff) {
            this.mCurrentHomeKeyDownTime = SystemClock.uptimeMillis();
            long j = this.mCurrentHomeKeyDownTime;
            long downInterval = j - this.mLastHomeKeyDownTime;
            long downAndLastUpInterval = j - this.mLastHomeKeyUpTime;
            if (downInterval > ((long) 300)) {
                LogUtil.w("FingerprintService.TouchSensorUnlockController", "downInterval = " + downInterval + ", regard this as first down");
                this.mIsDoubleHomeKeyDown = false;
            } else if (downAndLastUpInterval > ((long) DisplayTransformManager.LEVEL_COLOR_MATRIX_SATURATION)) {
                LogUtil.w("FingerprintService.TouchSensorUnlockController", "downAndLastUpInterval = " + downAndLastUpInterval + ", regard this as first down");
                this.mIsDoubleHomeKeyDown = false;
            } else {
                this.mIsDoubleHomeKeyDown = true;
            }
            this.mLastHomeKeyDownTime = this.mCurrentHomeKeyDownTime;
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchHomeKeyUp() {
        LogUtil.w("FingerprintService.TouchSensorUnlockController", "dispatchHomeKeyUp");
        if (this.mDoubleHomeGestureEnabled && this.mIsScreenOff) {
            long homeKeyUpTime = SystemClock.uptimeMillis();
            long upAndLastdownInterval = homeKeyUpTime - this.mCurrentHomeKeyDownTime;
            long upInterval = homeKeyUpTime - this.mLastHomeKeyUpTime;
            if (upAndLastdownInterval > ((long) DisplayTransformManager.LEVEL_COLOR_MATRIX_SATURATION)) {
                LogUtil.w("FingerprintService.TouchSensorUnlockController", "upAndLastdownInterval = " + upAndLastdownInterval);
                this.mLastHomeKeyUpTime = homeKeyUpTime;
            } else if (upInterval > ((long) 300)) {
                if (this.mIsDoubleHomeKeyDown) {
                    LogUtil.w("FingerprintService.TouchSensorUnlockController", "upInterval = " + upInterval);
                }
                this.mLastHomeKeyUpTime = homeKeyUpTime;
            } else {
                this.mLastHomeKeyUpTime = homeKeyUpTime;
                LogUtil.w("FingerprintService.TouchSensorUnlockController", "mIsDoubleHomeKeyDown = " + this.mIsDoubleHomeKeyDown);
                if (this.mIsDoubleHomeKeyDown) {
                    this.mHandler.sendMessage(3);
                }
            }
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public boolean dispatchPowerKeyPressedForPressTouch(String reason) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", " dispatchPowerKeyPressedForPressTouch, reason = " + reason);
        return false;
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchPowerKeyPressed() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", " dispatchPowerKeyPressed, mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        this.mHandler.removeMessage(4);
        if (this.mIsWakeUpByFingerprint) {
            userActivity();
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchTouchUp() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "dispatchTouchUp, mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther);
        getPsensorManager().onAuthenticationStarted(false);
        if (this.mIsScreenOff && !this.mIsScreenOnUnBlockedByOther) {
            this.mHandler.sendSyncMessageDelayed(4, 500);
        }
    }

    public synchronized void dispatchScreenOff(boolean isScreenOff) {
        if (this.mIsScreenOff != isScreenOff) {
            LogUtil.d("FingerprintService.TouchSensorUnlockController", "dispatchScreenOff mIsScreenOff = " + isScreenOff);
            this.mIsScreenOff = isScreenOff;
            this.mIUnLocker.onScreenOff(isScreenOff);
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onWakeUp(String wakeupReason) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onWakeUp, wakeupReason = " + wakeupReason);
        if ("android.service.fingerprint:WAKEUP".equals(wakeupReason)) {
            if (this.mIsWakeUpFinishedByOther && this.mIsScreenOff && !this.mIsScreenOnUnBlockedByOther) {
                this.mBiometricsManager.setKeyguardTransparent("FingerprintService", wakeupReason);
            }
            this.mIsWakeUpByFingerprint = true;
            return;
        }
        this.mIsWakeUpByFingerprint = false;
        this.mIsWakeUpFinishedByOther = false;
        this.mBiometricsManager.setKeyguardTransparent("FingerprintService", wakeupReason);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onWakeUpFinish() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onWakeUpFinish");
        this.mIsWakeUpFinishedByOther = true;
        this.mIsScreenOnUnBlockedByOther = false;
        dispatchScreenOff(false);
        this.mHandler.removeMessage(4);
        this.mHandler.sendMessageWithArg(5, 1);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onGoToSleep() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onGoToSleep");
        this.mIsWakeUpByFingerprint = false;
        dispatchScreenOff(true);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onGoToSleepFinish() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onGoToSleepFinish");
        this.mHandler.sendMessageWithArg(5, 0);
        this.mIsScreenOnUnBlockedByOther = false;
        dispatchScreenOff(true);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onScreenOnUnBlockedByOther(String unBlockedReason) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onScreenOnUnBlockedByOther unBlockedReason = " + unBlockedReason);
        this.mIsScreenOnUnBlockedByOther = true;
        this.mHandler.removeMessage(4);
        this.mBiometricsManager.setKeyguardTransparent("FingerprintService", unBlockedReason);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onScreenOnUnBlockedByFingerprint(boolean authenticated) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onScreenOnUnBlockedByFingerprint authenticated = " + authenticated);
        if (!authenticated) {
            this.mKeyguardPolicy.forceRefresh();
            handleWaitKeyguardShowed();
            return;
        }
        handleWaitKeyguardHidden();
    }

    private void handleWaitKeyguardShowed() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "handleWaitKeyguardShowed");
        getFPMS().unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_FAIL, (long) DisplayTransformManager.LEVEL_COLOR_MATRIX_SATURATION);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWaitKeyguardHidden() {
        this.mHandler.sendMessage(8);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchAcquired(int acquiredInfo) {
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchAuthenticated(Fingerprint fingerInfo, ArrayList<Byte> token) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "dispatchAuthenticated");
        if (fingerInfo.getBiometricId() != 0) {
            this.mHandler.removeMessage(4);
        }
        this.mHandler.sendSyncMessage(2);
        if (this.mIsScreenOff) {
            this.mIUnLocker.dispatchScreenOffAuthenticatedEvent(fingerInfo, token);
            if (fingerInfo.getBiometricId() == 0) {
                getPsensorManager().onAuthenticationStarted(false);
                return;
            }
            return;
        }
        this.mIUnLocker.dispatchScreenOnAuthenticatedEvent(fingerInfo, token);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchImageDirtyAuthenticated() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "dispatchImageDirtyAuthenticated");
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void reset() {
        getPsensorManager().onAuthenticationStarted(false);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void userActivity() {
        this.mHandler.sendSyncMessage(2);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchScreenOnTimeOut() {
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onLightScreenOnFinish() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onLightScreenOnFinish mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther + ", mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        if (!this.mIsScreenOnUnBlockedByOther && this.mIsWakeUpByFingerprint && this.mIsFingerprintUnlockSwitchOpened) {
            this.mIUnLocker.sendUnlockTime(1002, SystemClock.uptimeMillis());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSendTouchDownTime(int type, long time) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "handleSendTouchDownTime");
        this.mIUnLocker.sendUnlockTime(type, time);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void trunScreenOnByRightFinger() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "trunScreenOnByRightFinger mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther + ", mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        if (!this.mIsScreenOnUnBlockedByOther && this.mIsWakeUpByFingerprint && this.mIsScreenOff) {
            this.mHandler.obtainMessage(16, 1000, 0, Long.valueOf(this.mTouchDownTime)).sendToTarget();
        }
        getFPMS().unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_SUCESS, 0);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onProximitySensorChanged(boolean isNear) {
        this.mIsNearState = isNear;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private FingerprintPowerManager getFPMS() {
        return FingerprintPowerManager.getFingerprintPowerManager();
    }

    private ProximitySensorManager getPsensorManager() {
        return ProximitySensorManager.getProximitySensorManager();
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchAuthForDropHomeKey() {
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onSettingChanged(String settingName, boolean isOn) {
        if (FingerprintSettings.BLACK_GESTURE_MAIN_SETTING.equals(settingName)) {
            this.mIsBlackGestureSwitchOpened = isOn;
        } else if (FingerprintSettings.BLACK_GESTURE_DOUBLE_CLICK_HOME_SETTING.equals(settingName)) {
            this.mIsDoubleHomeSwitchOpened = isOn;
        } else if ("coloros_fingerprint_unlock_switch".equals(settingName)) {
            this.mIsFingerprintUnlockSwitchOpened = isOn;
        }
        this.mDoubleHomeGestureEnabled = this.mIsDoubleHomeSwitchOpened && this.mIsBlackGestureSwitchOpened && !this.mIsFingerprintUnlockSwitchOpened;
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onSettingChanged, mDoubleHomeGestureEnabled = " + this.mDoubleHomeGestureEnabled);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mDoubleTapNum = " + this.mDoubleTapNum);
        pw.println("mIsBlackGestureSwitchOpened = " + this.mIsBlackGestureSwitchOpened);
        pw.println("mIsDoubleHomeSwitchOpened = " + this.mIsDoubleHomeSwitchOpened);
        pw.println("mIsFingerprintUnlockSwitchOpened = " + this.mIsFingerprintUnlockSwitchOpened);
        pw.println("mDoubleHomeGestureEnabled = " + this.mDoubleHomeGestureEnabled);
    }
}
