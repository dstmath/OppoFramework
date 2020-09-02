package com.android.server.biometrics.fingerprint.wakeup;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.fingerprint.Fingerprint;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import com.android.server.LocalServices;
import com.android.server.biometrics.BiometricWakeupManagerService;
import com.android.server.biometrics.BiometricsManagerInternal;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.biometrics.fingerprint.keyguard.KeyguardPolicy;
import com.android.server.biometrics.fingerprint.power.FingerprintPowerManager;
import com.android.server.biometrics.fingerprint.sensor.ProximitySensorManager;
import com.android.server.biometrics.fingerprint.setting.FingerprintSettings;
import com.android.server.biometrics.fingerprint.tool.ExHandler;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class BackTouchSensorUnlockController implements IFingerprintSensorEventListener {
    public static final int GOTO_SLEEP_WAITING_TIME = 500;
    public static final int MSG_GOTO_SLEEP_FROM_WAKE0 = 4;
    public static final int MSG_KEEP_SCREEN_ON = 2;
    public static final int MSG_SEND_TOUCH_DOWN_TIME = 16;
    public static final int MSG_SET_SCREEN_STATE = 5;
    public static final int MSG_TURN_SCREEN_ON_BY_RIGHT_RESULT = 8;
    public static final int MSG_WAIT_KEYGUARD_HIDDEN = 7;
    public static final int MSG_WAKE_UP_BY_FINGERPRINT = 1;
    public static final int MSG_WAKE_UP_BY_SIDE_FINGERPRINT_POWER = 17;
    private static final int PRESS = 1;
    public static final int SCREEN_ON_WAITING_TIME = 300;
    private static final int TOUCH = 0;
    public static boolean mInWakeupByPower = false;
    public static boolean mIsScreenOff = false;
    public static String mPressTouchReason = "init";
    private final String TAG = "FingerprintService.BackTouchSensorUnlockController";
    private BiometricsManagerInternal mBiometricsManager;
    /* access modifiers changed from: private */
    public Context mContext;
    private DcsFingerprintStatisticsUtil mDcsStatisticsUtil;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private FingerprintService.IUnLocker mIUnLocker;
    private boolean mIsFingerprintUnlockSwitchOpened = false;
    private boolean mIsNearState = false;
    private boolean mIsScreenOnUnBlockedByOther = false;
    private boolean mIsWakeUpByFingerprint = false;
    private boolean mIsWakeUpFinishedByOther = true;
    private KeyguardPolicy mKeyguardPolicy;
    private Looper mLooper;
    private boolean mSystemReady = false;
    private long mTouchDownTime;
    private final BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        /* class com.android.server.biometrics.fingerprint.wakeup.BackTouchSensorUnlockController.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                boolean enabled = true;
                if (Settings.Secure.getIntForUser(BackTouchSensorUnlockController.this.mContext.getContentResolver(), FingerprintSettings.SIDE_FINGERPRINT_PRESS_TOUCH_MODE, 0, ActivityManager.getCurrentUser()) != 1) {
                    enabled = false;
                }
                FingerprintService.mPressTouchEnable = enabled;
                LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "ACTION_USER_SWITCHED, mPressTouchEnable = " + enabled + "  ActivityManager.getCurrentUser()= " + ActivityManager.getCurrentUser());
            }
        }
    };

    public BackTouchSensorUnlockController(Context context, FingerprintService.IUnLocker unLocker) {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "BackTouchSensorUnlockController construction");
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("BackTouchSensorUnlockController thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            LogUtil.e("FingerprintService.BackTouchSensorUnlockController", "mLooper null");
        }
        initHandler();
        this.mIUnLocker = unLocker;
        this.mKeyguardPolicy = KeyguardPolicy.getKeyguardPolicy();
        this.mDcsStatisticsUtil = DcsFingerprintStatisticsUtil.getDcsFingerprintStatisticsUtil(this.mContext);
        this.mBiometricsManager = (BiometricsManagerInternal) LocalServices.getService(BiometricsManagerInternal.class);
        this.mContext.registerReceiver(this.mUserReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"));
    }

    /* access modifiers changed from: private */
    public void handleSetScreenState(int state) {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "handleSetScreenState ( " + state + " )");
        this.mIUnLocker.setScreenState(state);
    }

    public void stopController() {
        this.mLooper.quit();
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            /* class com.android.server.biometrics.fingerprint.wakeup.BackTouchSensorUnlockController.AnonymousClass2 */

            @Override // com.android.server.biometrics.fingerprint.tool.ExHandler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i != 1) {
                    if (i != 2) {
                        if (i == 4) {
                            BackTouchSensorUnlockController.this.getFPMS().gotoSleep();
                        } else if (i == 5) {
                            BackTouchSensorUnlockController.this.handleSetScreenState(msg.arg1);
                        } else if (i == 7) {
                            BackTouchSensorUnlockController.this.handleWaitKeyguardHidden();
                        } else if (i == 8) {
                            BackTouchSensorUnlockController.this.trunScreenOnByRightFinger();
                        } else if (i == 16) {
                            BackTouchSensorUnlockController.this.handleSendTouchDownTime(msg.arg1, ((Long) msg.obj).longValue());
                        } else if (i == 17) {
                            BackTouchSensorUnlockController.this.getFPMS().unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_SIDE_FINGERPRINT_POWER, 0);
                        }
                    }
                    BackTouchSensorUnlockController.this.getFPMS().userActivity();
                } else {
                    BackTouchSensorUnlockController.this.getFPMS().blockScreenOn("android.service.fingerprint:WAKEUP");
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
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "dispatchTouchDown, mIsWakeUpFinishedByOther = " + this.mIsWakeUpFinishedByOther);
        this.mTouchDownTime = SystemClock.uptimeMillis();
        getPsensorManager().onAuthenticationStarted(true);
        if (!mIsScreenOff) {
            this.mHandler.obtainMessage(16, 1001, 0, Long.valueOf(this.mTouchDownTime)).sendToTarget();
        }
        this.mHandler.removeMessage(4);
        getFPMS().cancelGotoSleep("TouchDown");
        if (mIsScreenOff) {
            this.mHandler.sendSyncMessage(1);
        } else {
            this.mHandler.sendSyncMessage(2);
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchHomeKeyDown() {
        LogUtil.w("FingerprintService.BackTouchSensorUnlockController", "dispatchHomeKeyDown");
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchHomeKeyUp() {
        LogUtil.w("FingerprintService.BackTouchSensorUnlockController", "dispatchHomeKeyUp");
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public boolean dispatchPowerKeyPressedForPressTouch(String reason) {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", " dispatchPowerKeyPressedForPressTouch, reason = " + reason);
        if (mPressTouchReason != reason) {
            mPressTouchReason = reason;
        }
        if (reason.equals("wakeup")) {
            mInWakeupByPower = true;
            this.mHandler.sendSyncMessage(17);
            if (!(!FingerprintService.mPressTouchAuthenticated || FingerprintService.mPressTouchFp == null || FingerprintService.mPressTouchTokenByte == null)) {
                dispatchAuthenticated(FingerprintService.mPressTouchFp, FingerprintService.mPressTouchTokenByte);
                FingerprintService.mPressTouchAuthenticated = false;
                FingerprintService.mPressTouchFp = null;
                FingerprintService.mPressTouchTokenByte = null;
            }
        } else if (reason.equals("gotosleep")) {
            mInWakeupByPower = false;
            if (FingerprintService.mPressTouchApp) {
                this.mIUnLocker.pressTouchChannel();
            }
            return FingerprintService.mPressTouchEnrolling || FingerprintService.mPressTouchApp;
        } else if (reason.equals("gotosleepbyother")) {
            mInWakeupByPower = false;
        }
        return false;
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchPowerKeyPressed() {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", " dispatchPowerKeyPressed, mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        this.mHandler.removeMessage(4);
        if (this.mIsWakeUpByFingerprint) {
            userActivity();
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchTouchUp() {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "dispatchTouchUp, mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther);
        getPsensorManager().onAuthenticationStarted(false);
        if (mIsScreenOff && !this.mIsScreenOnUnBlockedByOther) {
            this.mHandler.sendSyncMessageDelayed(4, 500);
        }
    }

    public synchronized void dispatchScreenOff(boolean isScreenOff) {
        if (mIsScreenOff != isScreenOff) {
            LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "dispatchScreenOff mIsScreenOff = " + isScreenOff);
            mIsScreenOff = isScreenOff;
            this.mIUnLocker.onScreenOff(isScreenOff);
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onWakeUp(String wakeupReason) {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "onWakeUp, wakeupReason = " + wakeupReason);
        if ("android.service.fingerprint:WAKEUP".equals(wakeupReason)) {
            if (this.mIsWakeUpFinishedByOther && mIsScreenOff && !this.mIsScreenOnUnBlockedByOther) {
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
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "onWakeUpFinish");
        this.mIsWakeUpFinishedByOther = true;
        this.mIsScreenOnUnBlockedByOther = false;
        dispatchScreenOff(false);
        this.mHandler.removeMessage(4);
        this.mHandler.sendMessageWithArg(5, 1);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onGoToSleep() {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "onGoToSleep");
        this.mIsWakeUpByFingerprint = false;
        dispatchScreenOff(true);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onGoToSleepFinish() {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "onGoToSleepFinish");
        this.mHandler.sendMessageWithArg(5, 0);
        this.mIsScreenOnUnBlockedByOther = false;
        dispatchScreenOff(true);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onScreenOnUnBlockedByOther(String unBlockedReason) {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "onScreenOnUnBlockedByOther unBlockedReason = " + unBlockedReason);
        this.mIsScreenOnUnBlockedByOther = true;
        this.mHandler.removeMessage(4);
        this.mBiometricsManager.setKeyguardTransparent("FingerprintService", unBlockedReason);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onScreenOnUnBlockedByFingerprint(boolean authenticated) {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "onScreenOnUnBlockedByFingerprint authenticated = " + authenticated);
        if (!authenticated) {
            this.mKeyguardPolicy.forceRefresh();
            handleWaitKeyguardShowed();
            return;
        }
        handleWaitKeyguardHidden();
    }

    private void handleWaitKeyguardShowed() {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "handleWaitKeyguardShowed");
        getFPMS().unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_FAIL, (long) 100);
    }

    /* access modifiers changed from: private */
    public void handleWaitKeyguardHidden() {
        this.mHandler.sendMessage(8);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchAcquired(int acquiredInfo) {
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchAuthenticated(Fingerprint fingerInfo, ArrayList<Byte> token) {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "dispatchAuthenticated");
        if (fingerInfo.getBiometricId() != 0) {
            this.mHandler.removeMessage(4);
        }
        this.mHandler.sendSyncMessage(2);
        if (mIsScreenOff) {
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
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "dispatchImageDirtyAuthenticated");
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
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "onLightScreenOnFinish mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther + ", mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        if (!this.mIsScreenOnUnBlockedByOther && this.mIsWakeUpByFingerprint && this.mIsFingerprintUnlockSwitchOpened) {
            this.mIUnLocker.sendUnlockTime(1002, SystemClock.uptimeMillis());
        }
    }

    /* access modifiers changed from: private */
    public void handleSendTouchDownTime(int type, long time) {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "handleSendTouchDownTime");
        this.mIUnLocker.sendUnlockTime(type, time);
    }

    /* access modifiers changed from: private */
    public void trunScreenOnByRightFinger() {
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "trunScreenOnByRightFinger mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther + ", mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        if (!this.mIsScreenOnUnBlockedByOther && this.mIsWakeUpByFingerprint && mIsScreenOff) {
            this.mHandler.obtainMessage(16, 1000, 0, Long.valueOf(this.mTouchDownTime)).sendToTarget();
        }
        getFPMS().unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_SUCESS, 0);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onProximitySensorChanged(boolean isNear) {
        this.mIsNearState = isNear;
    }

    /* access modifiers changed from: private */
    public FingerprintPowerManager getFPMS() {
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
        if ("coloros_fingerprint_unlock_switch".equals(settingName)) {
            this.mIsFingerprintUnlockSwitchOpened = isOn;
        }
        LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "onSettingChanged, mIsFingerprintUnlockSwitchOpened = " + this.mIsFingerprintUnlockSwitchOpened);
        if (FingerprintSettings.SIDE_FINGERPRINT_PRESS_TOUCH_MODE.equals(settingName)) {
            boolean enabled = true;
            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), FingerprintSettings.SIDE_FINGERPRINT_PRESS_TOUCH_MODE, 0, ActivityManager.getCurrentUser()) != 1) {
                enabled = false;
            }
            FingerprintService.mPressTouchEnable = enabled;
            LogUtil.d("FingerprintService.BackTouchSensorUnlockController", "onSettingChanged, mPressTouchEnable = " + enabled + "  ActivityManager.getCurrentUser()= " + ActivityManager.getCurrentUser() + " isOn = " + isOn);
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mIsFingerprintUnlockSwitchOpened = " + this.mIsFingerprintUnlockSwitchOpened);
    }
}
