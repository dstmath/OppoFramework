package com.android.server.fingerprint.wakeup;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import com.android.server.LocalServices;
import com.android.server.biometrics.BiometricsManagerInternal;
import com.android.server.biometrics.BiometricsService;
import com.android.server.fingerprint.FingerprintService;
import com.android.server.fingerprint.FingerprintService.AuthenticatedInfo;
import com.android.server.fingerprint.FingerprintService.IUnLocker;
import com.android.server.fingerprint.FingerprintUtils;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.fingerprint.keyguard.KeyguardPolicy;
import com.android.server.fingerprint.power.FingerprintPowerManager;
import com.android.server.fingerprint.sensor.ProximitySensorManager;
import com.android.server.fingerprint.setting.FingerprintSettings;
import com.android.server.fingerprint.tool.ExHandler;
import com.android.server.fingerprint.util.LogUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import vendor.oppo.hardware.biometrics.fingerprint.V2_1.FingerprintAcquiredInfo;

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
    private IUnLocker mIUnLocker;
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

    public TouchSensorUnlockController(Context context, IUnLocker unLocker) {
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
            return;
        }
        if (this.mIsScreenOff) {
            if (this.mDcsStatisticsUtil != null) {
                this.mDcsStatisticsUtil.sendDoubleHomeTimes();
            }
            this.mDoubleTapNum++;
            LogUtil.d("FingerprintService.TouchSensorUnlockController", "turnScreenOnByDoubleHome, mDoubleTapNum = " + this.mDoubleTapNum);
            getFPMS().blockScreenOn("android.service.fingerprint:DOUBLE_HOME");
            FingerprintUtils.vibrateFingerprintKey(this.mContext);
        }
    }

    private void handleSetScreenState(int state) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "handleSetScreenState ( " + state + " )");
        this.mIUnLocker.setScreenState(state);
    }

    public void stopController() {
        this.mLooper.quit();
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        TouchSensorUnlockController.this.getFPMS().blockScreenOn("android.service.fingerprint:WAKEUP");
                        break;
                    case 2:
                        TouchSensorUnlockController.this.getFPMS().userActivity();
                        break;
                    case 3:
                        TouchSensorUnlockController.this.turnScreenOnByDoubleHome();
                        break;
                    case 4:
                        TouchSensorUnlockController.this.getFPMS().gotoSleep();
                        break;
                    case 5:
                        TouchSensorUnlockController.this.handleSetScreenState(msg.arg1);
                        break;
                    case 7:
                        TouchSensorUnlockController.this.handleWaitKeyguardHidden();
                        break;
                    case 8:
                        TouchSensorUnlockController.this.trunScreenOnByRightFinger();
                        break;
                    case 16:
                        TouchSensorUnlockController.this.handleSendTouchDownTime(msg.arg1, ((Long) msg.obj).longValue());
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public void notifySystemReady() {
        this.mSystemReady = true;
    }

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

    public void dispatchHomeKeyDown() {
        LogUtil.w("FingerprintService.TouchSensorUnlockController", "dispatchHomeKeyDown mDoubleHomeGestureEnabled = " + this.mDoubleHomeGestureEnabled);
        if (this.mDoubleHomeGestureEnabled && this.mIsScreenOff) {
            this.mCurrentHomeKeyDownTime = SystemClock.uptimeMillis();
            long downInterval = this.mCurrentHomeKeyDownTime - this.mLastHomeKeyDownTime;
            long downAndLastUpInterval = this.mCurrentHomeKeyDownTime - this.mLastHomeKeyUpTime;
            if (downInterval > 300) {
                LogUtil.w("FingerprintService.TouchSensorUnlockController", "downInterval = " + downInterval + ", regard this as first down");
                this.mIsDoubleHomeKeyDown = false;
            } else if (downAndLastUpInterval > 150) {
                LogUtil.w("FingerprintService.TouchSensorUnlockController", "downAndLastUpInterval = " + downAndLastUpInterval + ", regard this as first down");
                this.mIsDoubleHomeKeyDown = false;
            } else {
                this.mIsDoubleHomeKeyDown = true;
            }
            this.mLastHomeKeyDownTime = this.mCurrentHomeKeyDownTime;
        }
    }

    public void dispatchHomeKeyUp() {
        LogUtil.w("FingerprintService.TouchSensorUnlockController", "dispatchHomeKeyUp");
        if (this.mDoubleHomeGestureEnabled && this.mIsScreenOff) {
            long homeKeyUpTime = SystemClock.uptimeMillis();
            long upAndLastdownInterval = homeKeyUpTime - this.mCurrentHomeKeyDownTime;
            long upInterval = homeKeyUpTime - this.mLastHomeKeyUpTime;
            if (upAndLastdownInterval > 150) {
                LogUtil.w("FingerprintService.TouchSensorUnlockController", "upAndLastdownInterval = " + upAndLastdownInterval);
                this.mLastHomeKeyUpTime = homeKeyUpTime;
            } else if (upInterval > 300) {
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

    public void dispatchPowerKeyPressed() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", " dispatchPowerKeyPressed, mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        this.mHandler.removeMessage(4);
        if (this.mIsWakeUpByFingerprint) {
            userActivity();
        }
    }

    public void dispatchTouchUp() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "dispatchTouchUp, mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther);
        getPsensorManager().onAuthenticationStarted(false);
        if (this.mIsScreenOff && (this.mIsScreenOnUnBlockedByOther ^ 1) != 0) {
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

    public void onWakeUp(String wakeupReason) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onWakeUp, wakeupReason = " + wakeupReason);
        if ("android.service.fingerprint:WAKEUP".equals(wakeupReason)) {
            if (this.mIsWakeUpFinishedByOther && this.mIsScreenOff && (this.mIsScreenOnUnBlockedByOther ^ 1) != 0) {
                this.mBiometricsManager.setKeyguardTransparent(FingerprintService.TAG, wakeupReason);
            }
            this.mIsWakeUpByFingerprint = true;
            return;
        }
        this.mIsWakeUpByFingerprint = false;
        this.mIsWakeUpFinishedByOther = false;
        this.mBiometricsManager.setKeyguardTransparent(FingerprintService.TAG, wakeupReason);
    }

    public void onWakeUpFinish() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onWakeUpFinish");
        this.mIsWakeUpFinishedByOther = true;
        this.mIsScreenOnUnBlockedByOther = false;
        dispatchScreenOff(false);
        this.mHandler.removeMessage(4);
        this.mHandler.sendMessageWithArg(5, 1);
    }

    public void onGoToSleep() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onGoToSleep");
        this.mIsWakeUpByFingerprint = false;
        dispatchScreenOff(true);
    }

    public void onGoToSleepFinish() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onGoToSleepFinish");
        this.mHandler.sendMessageWithArg(5, 0);
        this.mIsScreenOnUnBlockedByOther = false;
        dispatchScreenOff(true);
    }

    public void onScreenOnUnBlockedByOther(String unBlockedReason) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onScreenOnUnBlockedByOther unBlockedReason = " + unBlockedReason);
        this.mIsScreenOnUnBlockedByOther = true;
        this.mHandler.removeMessage(4);
        this.mBiometricsManager.setKeyguardTransparent(FingerprintService.TAG, unBlockedReason);
    }

    public void onScreenOnUnBlockedByFingerprint(boolean authenticated) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onScreenOnUnBlockedByFingerprint authenticated = " + authenticated);
        if (authenticated) {
            handleWaitKeyguardHidden();
            return;
        }
        this.mKeyguardPolicy.forceRefresh();
        handleWaitKeyguardShowed();
    }

    private void handleWaitKeyguardShowed() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "handleWaitKeyguardShowed");
        getFPMS().unblockScreenOn(BiometricsService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_FAIL, 150);
    }

    private void handleWaitKeyguardHidden() {
        this.mHandler.sendMessage(8);
    }

    public void dispatchAcquired(int acquiredInfo) {
    }

    public void dispatchAuthenticated(AuthenticatedInfo authenticatedInfo) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "dispatchAuthenticated");
        if (authenticatedInfo.fingerId != 0) {
            this.mHandler.removeMessage(4);
        }
        this.mHandler.sendSyncMessage(2);
        if (this.mIsScreenOff) {
            this.mIUnLocker.dispatchScreenOffAuthenticatedEvent(authenticatedInfo.fingerId, authenticatedInfo.groupId);
            if (authenticatedInfo.fingerId == 0) {
                getPsensorManager().onAuthenticationStarted(false);
                return;
            }
            return;
        }
        this.mIUnLocker.dispatchScreenOnAuthenticatedEvent(authenticatedInfo.fingerId, authenticatedInfo.groupId);
    }

    public void dispatchImageDirtyAuthenticated() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "dispatchImageDirtyAuthenticated");
    }

    public void reset() {
        getPsensorManager().onAuthenticationStarted(false);
    }

    public void userActivity() {
        this.mHandler.sendSyncMessage(2);
    }

    public void dispatchScreenOnTimeOut() {
    }

    public void onLightScreenOnFinish() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onLightScreenOnFinish mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther + ", mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        if (!this.mIsScreenOnUnBlockedByOther && this.mIsWakeUpByFingerprint && this.mIsFingerprintUnlockSwitchOpened) {
            this.mIUnLocker.sendUnlockTime(FingerprintAcquiredInfo.ACQUIRED_ALREADY_ENROLLED, SystemClock.uptimeMillis());
        }
    }

    private void handleSendTouchDownTime(int type, long time) {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "handleSendTouchDownTime");
        this.mIUnLocker.sendUnlockTime(type, time);
    }

    private void trunScreenOnByRightFinger() {
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "trunScreenOnByRightFinger mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther + ", mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        if (!this.mIsScreenOnUnBlockedByOther && this.mIsWakeUpByFingerprint && this.mIsScreenOff) {
            this.mHandler.obtainMessage(16, 1000, 0, Long.valueOf(this.mTouchDownTime)).sendToTarget();
        }
        getFPMS().unblockScreenOn(BiometricsService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_SUCESS, 0);
    }

    public void onProximitySensorChanged(boolean isNear) {
        this.mIsNearState = isNear;
    }

    private FingerprintPowerManager getFPMS() {
        return FingerprintPowerManager.getFingerprintPowerManager();
    }

    private ProximitySensorManager getPsensorManager() {
        return ProximitySensorManager.getProximitySensorManager();
    }

    public void dispatchAuthForDropHomeKey() {
    }

    public void onSettingChanged(String settingName, boolean isOn) {
        boolean z;
        if (FingerprintSettings.BLACK_GESTURE_MAIN_SETTING.equals(settingName)) {
            this.mIsBlackGestureSwitchOpened = isOn;
        } else if (FingerprintSettings.BLACK_GESTURE_DOUBLE_CLICK_HOME_SETTING.equals(settingName)) {
            this.mIsDoubleHomeSwitchOpened = isOn;
        } else if (FingerprintSettings.FINGERPRINT_UNLOCK_SWITCH.equals(settingName)) {
            this.mIsFingerprintUnlockSwitchOpened = isOn;
        }
        if (this.mIsDoubleHomeSwitchOpened && this.mIsBlackGestureSwitchOpened) {
            z = this.mIsFingerprintUnlockSwitchOpened ^ 1;
        } else {
            z = false;
        }
        this.mDoubleHomeGestureEnabled = z;
        LogUtil.d("FingerprintService.TouchSensorUnlockController", "onSettingChanged, mDoubleHomeGestureEnabled = " + this.mDoubleHomeGestureEnabled);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("mDoubleTapNum = " + this.mDoubleTapNum);
        pw.println("mIsBlackGestureSwitchOpened = " + this.mIsBlackGestureSwitchOpened);
        pw.println("mIsDoubleHomeSwitchOpened = " + this.mIsDoubleHomeSwitchOpened);
        pw.println("mIsFingerprintUnlockSwitchOpened = " + this.mIsFingerprintUnlockSwitchOpened);
        pw.println("mDoubleHomeGestureEnabled = " + this.mDoubleHomeGestureEnabled);
    }
}
