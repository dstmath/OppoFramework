package com.android.server.biometrics.fingerprint.wakeup;

import android.content.Context;
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
import com.android.server.biometrics.fingerprint.tool.ExHandler;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import com.android.server.biometrics.fingerprint.wakeup.state.UnlockState;
import com.android.server.biometrics.fingerprint.wakeup.state.UnlockStateFactory;
import com.android.server.biometrics.fingerprint.wakeup.state.WakeUp;
import com.android.server.display.color.DisplayTransformManager;
import com.android.server.policy.PhoneWindowManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class UnlockController implements IFingerprintSensorEventListener {
    public static final int GOTO_SLEEP_WAITING_TIME = 500;
    public static final int MSG_GOTO_SLEEP_FROM_WAKE0 = 3;
    public static final int MSG_KEEP_SCREEN_ON = 1;
    public static final int MSG_RESET_HOMEKEY_DISPATCH = 4;
    public static final int MSG_SEND_TOUCH_DOWN_TIME = 9;
    public static final int MSG_TURN_SCREEN_ON_BY_BAD_ACQUIRE = 2;
    public static final int MSG_TURN_SCREEN_ON_BY_RIGHT_RESULT = 7;
    public static final int MSG_WAIT_KEYGUARD_HIDDEN = 6;
    public static final int RESET_HOMEKEY_DISPATCH_WAITING_TIME = 300;
    public static final int SCREEN_ON_WAITING_TIME = 300;
    private static final String[] UNLOCKSTATES = {"ScreenOFFUnlock", "WakeUp", "ScreenOn", "ScreenOnUnlock", "DisableAuthentication"};
    private final String TAG = "FingerprintService.UnlockController";
    private boolean mAcquiredBad = false;
    private BiometricsManagerInternal mBiometricsManager;
    private Context mContext;
    private DcsFingerprintStatisticsUtil mDcsStatisticsUtil;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHomeKeyDown = false;
    private FingerprintService.IUnLocker mIUnLocker;
    private boolean mIsFingerprintUnlockSwitchOpened = false;
    private boolean mIsScreenOff = false;
    private boolean mIsScreenOnUnBlockedByOther = false;
    private boolean mIsSettingOn;
    private boolean mIsWakeUpByFingerprint = false;
    private boolean mIsWakeUpFinishedByOther = true;
    private KeyguardPolicy mKeyguardPolicy;
    private Looper mLooper;
    private OrderedEvents mOrderedEvent = OrderedEvents.NONE;
    private boolean mSystemReady = false;
    private long mTouchDownTime;
    private UnlockState mUnlockState;
    private UnlockStateFactory mUnlockStateFactory;

    public enum OrderedEvents {
        TOUCHDOWN,
        HOMEKEY,
        POWERKEY,
        TOUCHUP,
        NONE,
        TOUCHDOWN_TOUCHUP,
        TOUCHDOWN_HOMEKEY_TOUCHUP,
        TOUCHDOWN_POWERKEY_TOUCHUP,
        HOMEKEY_TOUCHDOWN_TOUCHUP,
        POWERKEY_TOUCHDOWN_TOUCHUP,
        TOUCHDOWN_HOMEKEY,
        TOUCHDOWN_POWERKEY,
        HOMEKEY_TOUCHDOWN,
        POWERKEY_TOUCHDOWN
    }

    public UnlockController(Context context, FingerprintService.IUnLocker unLocker) {
        LogUtil.d("FingerprintService.UnlockController", "Controller construction");
        this.mContext = context;
        this.mHandlerThread = new HandlerThread("Controller thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            LogUtil.e("FingerprintService.UnlockController", "mLooper null");
        }
        initHandler();
        this.mIUnLocker = unLocker;
        this.mKeyguardPolicy = KeyguardPolicy.getKeyguardPolicy();
        this.mDcsStatisticsUtil = DcsFingerprintStatisticsUtil.getDcsFingerprintStatisticsUtil(this.mContext);
        this.mUnlockStateFactory = new UnlockStateFactory(this.mContext, this.mIUnLocker);
        updateUnlockSetting(true);
        this.mBiometricsManager = (BiometricsManagerInternal) LocalServices.getService(BiometricsManagerInternal.class);
    }

    private void updateUnlockSetting(boolean isOn) {
        LogUtil.d("FingerprintService.UnlockController", "updateUnlockSetting  isOn = " + isOn);
        this.mIsSettingOn = isOn;
        if (isOn) {
            for (int i = 0; i < UNLOCKSTATES.length; i++) {
                this.mUnlockState = null;
                this.mUnlockState = this.mUnlockStateFactory.mStateFactoryInstance.get(UNLOCKSTATES[i]);
                if (this.mUnlockState != null) {
                    LogUtil.e("FingerprintService.UnlockController", "error in delete logic");
                } else {
                    this.mUnlockState = this.mUnlockStateFactory.createUnlockState(UNLOCKSTATES[i]);
                }
            }
            return;
        }
        for (int i2 = 0; i2 < UNLOCKSTATES.length; i2++) {
            this.mUnlockState = null;
            this.mUnlockState = this.mUnlockStateFactory.mStateFactoryInstance.get(UNLOCKSTATES[i2]);
            if (this.mUnlockState != null) {
                this.mUnlockStateFactory.deleteUnlockState(UNLOCKSTATES[i2]);
            } else {
                LogUtil.d("FingerprintService.UnlockController", "this action has been diabled");
            }
        }
    }

    private synchronized void setOrderedEvent(OrderedEvents event) {
        this.mOrderedEvent = event;
        LogUtil.d("FingerprintService.UnlockController", "set current orderedEvent = " + this.mOrderedEvent);
    }

    private synchronized void handlerUpdateOrderedEvent() {
        for (int i = 0; i < UNLOCKSTATES.length; i++) {
            this.mUnlockState = null;
            this.mUnlockState = this.mUnlockStateFactory.mStateFactoryInstance.get(UNLOCKSTATES[i]);
            if (this.mUnlockState != null) {
                this.mUnlockState.dispatchOrderedEvent(this.mOrderedEvent);
            }
        }
    }

    private synchronized void updateOrderedEvent(OrderedEvents event, boolean needUpdate, boolean needDispatch) {
        if (needUpdate) {
            try {
                setOrderedEvent(event);
            } catch (Throwable th) {
                throw th;
            }
        }
        if (needDispatch) {
            handlerUpdateOrderedEvent();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void turnScreenOnByBadAcquire() {
        LogUtil.d("FingerprintService.UnlockController", "turnScreenOnByBadAcquire");
        if (this.mIsScreenOff) {
            this.mBiometricsManager.setKeyguardOpaque("FingerprintService", BiometricWakeupManagerService.SET_KEYGUARD_OPAQUE_WHILE_FAIL_UNLOCK);
            this.mKeyguardPolicy.forceRefresh();
            handleWaitKeyguardShowed();
        }
    }

    public void stopController() {
        this.mUnlockState = null;
        this.mUnlockStateFactory = null;
        this.mLooper.quit();
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            /* class com.android.server.biometrics.fingerprint.wakeup.UnlockController.AnonymousClass1 */

            @Override // com.android.server.biometrics.fingerprint.tool.ExHandler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    UnlockController.this.getFPMS().userActivity();
                } else if (i == 2) {
                    UnlockController.this.turnScreenOnByBadAcquire();
                } else if (i == 3) {
                    UnlockController.this.getFPMS().gotoSleep();
                } else if (i == 4) {
                    LogUtil.d("FingerprintService.UnlockController", "reset drop home key setting 0");
                    Settings.System.putInt(UnlockController.this.mContext.getContentResolver(), "drop_home_key_when_use_fingerprint", 0);
                } else if (i == 6) {
                    UnlockController.this.handleWaitKeyguardHidden();
                } else if (i == 7) {
                    UnlockController.this.trunScreenOnByRightFinger();
                } else if (i == 9) {
                    UnlockController.this.handleSendTouchDownTime(msg.arg1, ((Long) msg.obj).longValue());
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
        LogUtil.d("FingerprintService.UnlockController", "dispatchTouchDown last orderedEvent = " + this.mOrderedEvent + ", mIsWakeUpFinishedByOther = " + this.mIsWakeUpFinishedByOther);
        this.mTouchDownTime = SystemClock.uptimeMillis();
        this.mHandler.removeMessage(3);
        this.mAcquiredBad = false;
        switch (this.mOrderedEvent) {
            case TOUCHUP:
            case TOUCHDOWN_TOUCHUP:
            case NONE:
            case TOUCHDOWN_POWERKEY:
                updateOrderedEvent(OrderedEvents.TOUCHDOWN, true, true);
                if (!this.mIsScreenOff) {
                    this.mHandler.obtainMessage(9, 1001, 0, Long.valueOf(this.mTouchDownTime)).sendToTarget();
                    break;
                }
                break;
            case HOMEKEY:
                updateOrderedEvent(OrderedEvents.HOMEKEY_TOUCHDOWN, true, true);
                break;
            case POWERKEY:
                updateOrderedEvent(OrderedEvents.POWERKEY_TOUCHDOWN, true, true);
                break;
            case TOUCHDOWN:
                break;
            default:
                LogUtil.w("FingerprintService.UnlockController", "Undefined last orderedEvent = " + this.mOrderedEvent);
                break;
        }
        if (!this.mIsScreenOff) {
            this.mHandler.sendSyncMessage(1);
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchHomeKeyDown() {
        LogUtil.d("FingerprintService.UnlockController", "dispatchHomeKeyDown");
        this.mHandler.removeMessage(4);
        this.mHomeKeyDown = true;
        if (!this.mSystemReady) {
            LogUtil.d("FingerprintService.UnlockController", "System is not ready, return");
        } else if (!this.mIsScreenOff) {
            LogUtil.d("FingerprintService.UnlockController", "Screen is on, ScreenOnUnlock don't need HomeKey Event");
        } else {
            LogUtil.d("FingerprintService.UnlockController", "last orderedEvent = " + this.mOrderedEvent);
            switch (this.mOrderedEvent) {
                case TOUCHUP:
                case TOUCHDOWN_TOUCHUP:
                case NONE:
                case POWERKEY:
                    updateOrderedEvent(OrderedEvents.HOMEKEY, true, true);
                    break;
                case TOUCHDOWN_POWERKEY:
                case POWERKEY_TOUCHDOWN:
                case HOMEKEY_TOUCHDOWN:
                    break;
                case HOMEKEY:
                    updateOrderedEvent(OrderedEvents.HOMEKEY, false, true);
                    break;
                case TOUCHDOWN:
                    if (this.mAcquiredBad) {
                        LogUtil.d("FingerprintService.UnlockController", "dispatchHomeKeyDown acquired bad, just screen on");
                        this.mHandler.sendMessage(2);
                    }
                    updateOrderedEvent(OrderedEvents.TOUCHDOWN_HOMEKEY, true, true);
                    this.mUnlockState = this.mUnlockStateFactory.mStateFactoryInstance.get("WakeUp");
                    UnlockState unlockState = this.mUnlockState;
                    if (unlockState != null) {
                        ((WakeUp) unlockState).resetWakeupMode(PhoneWindowManager.SYSTEM_DIALOG_REASON_HOME_KEY);
                        break;
                    }
                    break;
                case TOUCHDOWN_HOMEKEY:
                    this.mUnlockState = this.mUnlockStateFactory.mStateFactoryInstance.get("WakeUp");
                    UnlockState unlockState2 = this.mUnlockState;
                    if (unlockState2 != null) {
                        ((WakeUp) unlockState2).resetWakeupMode(PhoneWindowManager.SYSTEM_DIALOG_REASON_HOME_KEY);
                        break;
                    }
                    break;
                default:
                    LogUtil.w("FingerprintService.UnlockController", "Undefined last orderedEvent = " + this.mOrderedEvent);
                    break;
            }
            if (OrderedEvents.HOMEKEY == this.mOrderedEvent) {
                this.mHandler.removeMessage(3);
            }
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchHomeKeyUp() {
        LogUtil.d("FingerprintService.UnlockController", "dispatchHomeKeyUp");
        this.mHomeKeyDown = false;
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public boolean dispatchPowerKeyPressedForPressTouch(String reason) {
        LogUtil.d("FingerprintService.UnlockController", " dispatchPowerKeyPressedForPressTouch, reason = " + reason);
        return false;
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchPowerKeyPressed() {
        LogUtil.d("FingerprintService.UnlockController", " dispatchPowerKeyPressed last orderedEvent = " + this.mOrderedEvent);
        this.mHandler.removeMessage(3);
        if (this.mIsWakeUpByFingerprint) {
            userActivity();
        }
        switch (this.mOrderedEvent) {
            case TOUCHUP:
            case TOUCHDOWN_TOUCHUP:
            case NONE:
                updateOrderedEvent(OrderedEvents.POWERKEY, true, false);
                return;
            case TOUCHDOWN_POWERKEY:
            case HOMEKEY:
            case POWERKEY:
            case POWERKEY_TOUCHDOWN:
            case HOMEKEY_TOUCHDOWN:
            case TOUCHDOWN_HOMEKEY:
                return;
            case TOUCHDOWN:
                updateOrderedEvent(OrderedEvents.TOUCHDOWN_POWERKEY, true, true);
                return;
            default:
                LogUtil.w("FingerprintService.UnlockController", "Undefined last orderedEvent = " + this.mOrderedEvent);
                return;
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchTouchUp() {
        LogUtil.d("FingerprintService.UnlockController", "dispatchTouchUp last orderedEvent = " + this.mOrderedEvent + ", mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther);
        if (this.mOrderedEvent == OrderedEvents.TOUCHDOWN) {
            setOrderedEvent(OrderedEvents.TOUCHDOWN_TOUCHUP);
        } else {
            setOrderedEvent(OrderedEvents.TOUCHUP);
        }
        handlerUpdateOrderedEvent();
        resetAuthenticated();
        if (this.mIsScreenOff && !this.mIsScreenOnUnBlockedByOther) {
            this.mHandler.sendSyncMessageDelayed(3, 500);
        }
    }

    public synchronized void dispatchScreenOff(boolean isScreenOff) {
        if (this.mOrderedEvent == OrderedEvents.POWERKEY || this.mOrderedEvent == OrderedEvents.HOMEKEY) {
            LogUtil.d("FingerprintService.UnlockController", "Screen state changed before touch down, reset lastorderedEvent = " + this.mOrderedEvent);
            updateOrderedEvent(OrderedEvents.NONE, true, true);
        }
        if (this.mIsScreenOff != isScreenOff) {
            LogUtil.d("FingerprintService.UnlockController", "dispatchScreenOff mIsScreenOff = " + isScreenOff);
            this.mIsScreenOff = isScreenOff;
            for (int i = 0; i < UNLOCKSTATES.length; i++) {
                this.mUnlockState = null;
                this.mUnlockState = this.mUnlockStateFactory.mStateFactoryInstance.get(UNLOCKSTATES[i]);
                if (this.mUnlockState != null) {
                    this.mUnlockState.dispatchScreenOff(isScreenOff);
                }
            }
            this.mIUnLocker.onScreenOff(isScreenOff);
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onWakeUp(String wakeupReason) {
        LogUtil.d("FingerprintService.UnlockController", "onWakeUp, wakeupReason = " + wakeupReason);
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
        LogUtil.d("FingerprintService.UnlockController", "onWakeUpFinish");
        this.mIsWakeUpFinishedByOther = true;
        this.mIsScreenOnUnBlockedByOther = false;
        dispatchScreenOff(false);
        this.mHandler.removeMessage(3);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onGoToSleep() {
        LogUtil.d("FingerprintService.UnlockController", "onGoToSleep");
        this.mIsWakeUpByFingerprint = false;
        dispatchScreenOff(true);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onGoToSleepFinish() {
        LogUtil.d("FingerprintService.UnlockController", "onGoToSleepFinish");
        this.mIsScreenOnUnBlockedByOther = false;
        dispatchScreenOff(true);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onScreenOnUnBlockedByOther(String unBlockedReason) {
        LogUtil.d("FingerprintService.UnlockController", "onScreenOnUnBlockedByOther unBlockedReason = " + unBlockedReason);
        this.mIsScreenOnUnBlockedByOther = true;
        this.mHandler.removeMessage(3);
        this.mBiometricsManager.setKeyguardTransparent("FingerprintService", unBlockedReason);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onScreenOnUnBlockedByFingerprint(boolean authenticated) {
        LogUtil.d("FingerprintService.UnlockController", "onScreenOnUnBlockedByFingerprint authenticated = " + authenticated);
        if (!authenticated) {
            this.mKeyguardPolicy.forceRefresh();
            handleWaitKeyguardShowed();
            return;
        }
        handleWaitKeyguardHidden();
    }

    private void handleWaitKeyguardShowed() {
        LogUtil.d("FingerprintService.UnlockController", "handleWaitKeyguardShowed");
        getFPMS().unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_FAIL, (long) DisplayTransformManager.LEVEL_COLOR_MATRIX_SATURATION);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWaitKeyguardHidden() {
        this.mHandler.sendMessage(7);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchAcquired(int acquiredInfo) {
        if (OrderedEvents.TOUCHDOWN_HOMEKEY == this.mOrderedEvent && acquiredInfo != 0) {
            LogUtil.d("FingerprintService.UnlockController", "dispatchAcquired acquired bad, just screen on");
            this.mHandler.sendMessage(2);
        } else if (OrderedEvents.TOUCHDOWN == this.mOrderedEvent && acquiredInfo != 0) {
            this.mAcquiredBad = true;
        } else if (acquiredInfo == 0 && !this.mIsScreenOff) {
            if (OrderedEvents.POWERKEY_TOUCHDOWN == this.mOrderedEvent || OrderedEvents.TOUCHDOWN_POWERKEY == this.mOrderedEvent) {
                updateOrderedEvent(OrderedEvents.TOUCHDOWN, true, true);
            }
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchAuthenticated(Fingerprint fingerInfo, ArrayList<Byte> token) {
        LogUtil.d("FingerprintService.UnlockController", "dispatchAuthenticated");
        if (fingerInfo.getBiometricId() != 0) {
            this.mHandler.removeMessage(3);
        }
        for (int i = 0; i < UNLOCKSTATES.length; i++) {
            this.mUnlockState = null;
            this.mUnlockState = this.mUnlockStateFactory.mStateFactoryInstance.get(UNLOCKSTATES[i]);
            UnlockState unlockState = this.mUnlockState;
            if (unlockState != null) {
                unlockState.dispatchAuthenticated(fingerInfo, token);
            }
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchImageDirtyAuthenticated() {
        LogUtil.d("FingerprintService.UnlockController", "dispatchImageDirtyAuthenticated");
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void reset() {
        LogUtil.d("FingerprintService.UnlockController", "reset last orderedEvent = " + this.mOrderedEvent);
        setOrderedEvent(OrderedEvents.NONE);
        handlerUpdateOrderedEvent();
        resetAuthenticated();
    }

    private void resetAuthenticated() {
        LogUtil.d("FingerprintService.UnlockController", "reset Authenticated");
        for (int i = 0; i < UNLOCKSTATES.length; i++) {
            this.mUnlockState = null;
            this.mUnlockState = this.mUnlockStateFactory.mStateFactoryInstance.get(UNLOCKSTATES[i]);
            UnlockState unlockState = this.mUnlockState;
            if (unlockState != null) {
                unlockState.resetAuthenticated();
            }
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void userActivity() {
        this.mHandler.sendSyncMessage(1);
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchScreenOnTimeOut() {
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onLightScreenOnFinish() {
        LogUtil.d("FingerprintService.UnlockController", "onLightScreenOnFinish mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther + ", mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        if (!this.mIsScreenOnUnBlockedByOther && this.mIsWakeUpByFingerprint && this.mIsFingerprintUnlockSwitchOpened) {
            this.mIUnLocker.sendUnlockTime(1002, SystemClock.uptimeMillis());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSendTouchDownTime(int type, long time) {
        LogUtil.d("FingerprintService.UnlockController", "handleSendTouchDownTime");
        this.mIUnLocker.sendUnlockTime(type, time);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void trunScreenOnByRightFinger() {
        LogUtil.d("FingerprintService.UnlockController", "trunScreenOnByRightFinger mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther + ", mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        if (!this.mIsScreenOnUnBlockedByOther && this.mIsWakeUpByFingerprint && this.mIsScreenOff) {
            this.mHandler.obtainMessage(9, 1000, 0, Long.valueOf(this.mTouchDownTime)).sendToTarget();
        }
        getFPMS().unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_SUCESS, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private FingerprintPowerManager getFPMS() {
        return FingerprintPowerManager.getFingerprintPowerManager();
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dispatchAuthForDropHomeKey() {
        if (!this.mIsScreenOff && this.mHomeKeyDown) {
            LogUtil.d("FingerprintService.UnlockController", "drop home key");
            Settings.System.putInt(this.mContext.getContentResolver(), "drop_home_key_when_use_fingerprint", 1);
        } else if (!this.mIsScreenOff && !this.mHomeKeyDown) {
            LogUtil.d("FingerprintService.UnlockController", "drop home key within 300ms");
            this.mHandler.removeMessage(4);
            Settings.System.putInt(this.mContext.getContentResolver(), "drop_home_key_when_use_fingerprint", 1);
            this.mHandler.sendMessageDelayed(4, 300);
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onSettingChanged(String settingName, boolean isOn) {
        if ("coloros_fingerprint_unlock_switch".equals(settingName)) {
            this.mIsFingerprintUnlockSwitchOpened = isOn;
        }
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void onProximitySensorChanged(boolean isNear) {
    }

    @Override // com.android.server.biometrics.fingerprint.wakeup.IFingerprintSensorEventListener
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }
}
