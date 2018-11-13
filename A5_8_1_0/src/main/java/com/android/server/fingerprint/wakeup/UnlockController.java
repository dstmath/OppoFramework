package com.android.server.fingerprint.wakeup;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.System;
import com.android.server.LocalServices;
import com.android.server.biometrics.BiometricsManagerInternal;
import com.android.server.biometrics.BiometricsService;
import com.android.server.fingerprint.FingerprintService;
import com.android.server.fingerprint.FingerprintService.AuthenticatedInfo;
import com.android.server.fingerprint.FingerprintService.IUnLocker;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.fingerprint.keyguard.KeyguardPolicy;
import com.android.server.fingerprint.power.FingerprintPowerManager;
import com.android.server.fingerprint.setting.FingerprintSettings;
import com.android.server.fingerprint.tool.ExHandler;
import com.android.server.fingerprint.util.LogUtil;
import com.android.server.fingerprint.wakeup.state.UnlockState;
import com.android.server.fingerprint.wakeup.state.UnlockStateFactory;
import com.android.server.fingerprint.wakeup.state.WakeUp;
import com.android.server.policy.PhoneWindowManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import vendor.oppo.hardware.biometrics.fingerprint.V2_1.FingerprintAcquiredInfo;

public class UnlockController implements IFingerprintSensorEventListener {
    /* renamed from: -com-android-server-fingerprint-wakeup-UnlockController$OrderedEventsSwitchesValues */
    private static final /* synthetic */ int[] f248xb1b98438 = null;
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
    private static final String[] UNLOCKSTATES = new String[]{"ScreenOFFUnlock", "WakeUp", "ScreenOn", "ScreenOnUnlock", "DisableAuthentication"};
    private final String TAG = "FingerprintService.UnlockController";
    private boolean mAcquiredBad = false;
    private BiometricsManagerInternal mBiometricsManager;
    private Context mContext;
    private DcsFingerprintStatisticsUtil mDcsStatisticsUtil;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHomeKeyDown = false;
    private IUnLocker mIUnLocker;
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

    /* renamed from: -getcom-android-server-fingerprint-wakeup-UnlockController$OrderedEventsSwitchesValues */
    private static /* synthetic */ int[] m173xca4be914() {
        if (f248xb1b98438 != null) {
            return f248xb1b98438;
        }
        int[] iArr = new int[OrderedEvents.values().length];
        try {
            iArr[OrderedEvents.HOMEKEY.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[OrderedEvents.HOMEKEY_TOUCHDOWN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[OrderedEvents.HOMEKEY_TOUCHDOWN_TOUCHUP.ordinal()] = 11;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[OrderedEvents.NONE.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[OrderedEvents.POWERKEY.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[OrderedEvents.POWERKEY_TOUCHDOWN.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[OrderedEvents.POWERKEY_TOUCHDOWN_TOUCHUP.ordinal()] = 12;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[OrderedEvents.TOUCHDOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[OrderedEvents.TOUCHDOWN_HOMEKEY.ordinal()] = 7;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[OrderedEvents.TOUCHDOWN_HOMEKEY_TOUCHUP.ordinal()] = 13;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[OrderedEvents.TOUCHDOWN_POWERKEY.ordinal()] = 8;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[OrderedEvents.TOUCHDOWN_POWERKEY_TOUCHUP.ordinal()] = 14;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[OrderedEvents.TOUCHDOWN_TOUCHUP.ordinal()] = 9;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[OrderedEvents.TOUCHUP.ordinal()] = 10;
        } catch (NoSuchFieldError e14) {
        }
        f248xb1b98438 = iArr;
        return iArr;
    }

    public UnlockController(Context context, IUnLocker unLocker) {
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
        int i;
        if (isOn) {
            for (i = 0; i < UNLOCKSTATES.length; i++) {
                this.mUnlockState = null;
                this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get(UNLOCKSTATES[i]);
                if (this.mUnlockState != null) {
                    LogUtil.e("FingerprintService.UnlockController", "error in delete logic");
                } else {
                    this.mUnlockState = this.mUnlockStateFactory.createUnlockState(UNLOCKSTATES[i]);
                }
            }
            return;
        }
        for (i = 0; i < UNLOCKSTATES.length; i++) {
            this.mUnlockState = null;
            this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get(UNLOCKSTATES[i]);
            if (this.mUnlockState != null) {
                this.mUnlockStateFactory.deleteUnlockState(UNLOCKSTATES[i]);
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
        for (Object obj : UNLOCKSTATES) {
            this.mUnlockState = null;
            this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get(obj);
            if (this.mUnlockState != null) {
                this.mUnlockState.dispatchOrderedEvent(this.mOrderedEvent);
            }
        }
    }

    private synchronized void updateOrderedEvent(OrderedEvents event, boolean needUpdate, boolean needDispatch) {
        if (needUpdate) {
            setOrderedEvent(event);
        }
        if (needDispatch) {
            handlerUpdateOrderedEvent();
        }
    }

    private void turnScreenOnByBadAcquire() {
        LogUtil.d("FingerprintService.UnlockController", "turnScreenOnByBadAcquire");
        if (this.mIsScreenOff) {
            this.mBiometricsManager.setKeyguardOpaque(FingerprintService.TAG, BiometricsService.SET_KEYGUARD_OPAQUE_WHILE_FAIL_UNLOCK);
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
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        UnlockController.this.getFPMS().userActivity();
                        break;
                    case 2:
                        UnlockController.this.turnScreenOnByBadAcquire();
                        break;
                    case 3:
                        UnlockController.this.getFPMS().gotoSleep();
                        break;
                    case 4:
                        LogUtil.d("FingerprintService.UnlockController", "reset drop home key setting 0");
                        System.putInt(UnlockController.this.mContext.getContentResolver(), "drop_home_key_when_use_fingerprint", 0);
                        break;
                    case 6:
                        UnlockController.this.handleWaitKeyguardHidden();
                        break;
                    case 7:
                        UnlockController.this.trunScreenOnByRightFinger();
                        break;
                    case 9:
                        UnlockController.this.handleSendTouchDownTime(msg.arg1, ((Long) msg.obj).longValue());
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
        LogUtil.d("FingerprintService.UnlockController", "dispatchTouchDown last orderedEvent = " + this.mOrderedEvent + ", mIsWakeUpFinishedByOther = " + this.mIsWakeUpFinishedByOther);
        this.mTouchDownTime = SystemClock.uptimeMillis();
        this.mHandler.removeMessage(3);
        this.mAcquiredBad = false;
        switch (m173xca4be914()[this.mOrderedEvent.ordinal()]) {
            case 1:
                updateOrderedEvent(OrderedEvents.HOMEKEY_TOUCHDOWN, true, true);
                break;
            case 3:
            case 8:
            case 9:
            case 10:
                updateOrderedEvent(OrderedEvents.TOUCHDOWN, true, true);
                if (!this.mIsScreenOff) {
                    this.mHandler.obtainMessage(9, 1001, 0, Long.valueOf(this.mTouchDownTime)).sendToTarget();
                    break;
                }
                break;
            case 4:
                updateOrderedEvent(OrderedEvents.POWERKEY_TOUCHDOWN, true, true);
                break;
            case 6:
                break;
            default:
                LogUtil.w("FingerprintService.UnlockController", "Undefined last orderedEvent = " + this.mOrderedEvent);
                break;
        }
        if (!this.mIsScreenOff) {
            this.mHandler.sendSyncMessage(1);
        }
    }

    public void dispatchHomeKeyDown() {
        LogUtil.d("FingerprintService.UnlockController", "dispatchHomeKeyDown");
        this.mHandler.removeMessage(4);
        this.mHomeKeyDown = true;
        if (!this.mSystemReady) {
            LogUtil.d("FingerprintService.UnlockController", "System is not ready, return");
        } else if (this.mIsScreenOff) {
            LogUtil.d("FingerprintService.UnlockController", "last orderedEvent = " + this.mOrderedEvent);
            switch (m173xca4be914()[this.mOrderedEvent.ordinal()]) {
                case 1:
                    updateOrderedEvent(OrderedEvents.HOMEKEY, false, true);
                    break;
                case 2:
                case 5:
                case 8:
                    break;
                case 3:
                case 4:
                case 9:
                case 10:
                    updateOrderedEvent(OrderedEvents.HOMEKEY, true, true);
                    break;
                case 6:
                    if (this.mAcquiredBad) {
                        LogUtil.d("FingerprintService.UnlockController", "dispatchHomeKeyDown acquired bad, just screen on");
                        this.mHandler.sendMessage(2);
                    }
                    updateOrderedEvent(OrderedEvents.TOUCHDOWN_HOMEKEY, true, true);
                    this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get("WakeUp");
                    if (this.mUnlockState != null) {
                        ((WakeUp) this.mUnlockState).resetWakeupMode(PhoneWindowManager.SYSTEM_DIALOG_REASON_HOME_KEY);
                        break;
                    }
                    break;
                case 7:
                    this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get("WakeUp");
                    if (this.mUnlockState != null) {
                        ((WakeUp) this.mUnlockState).resetWakeupMode(PhoneWindowManager.SYSTEM_DIALOG_REASON_HOME_KEY);
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
        } else {
            LogUtil.d("FingerprintService.UnlockController", "Screen is on, ScreenOnUnlock don't need HomeKey Event");
        }
    }

    public void dispatchHomeKeyUp() {
        LogUtil.d("FingerprintService.UnlockController", "dispatchHomeKeyUp");
        this.mHomeKeyDown = false;
    }

    public void dispatchPowerKeyPressed() {
        LogUtil.d("FingerprintService.UnlockController", " dispatchPowerKeyPressed last orderedEvent = " + this.mOrderedEvent);
        this.mHandler.removeMessage(3);
        if (this.mIsWakeUpByFingerprint) {
            userActivity();
        }
        switch (m173xca4be914()[this.mOrderedEvent.ordinal()]) {
            case 1:
            case 2:
            case 4:
            case 5:
            case 7:
            case 8:
                return;
            case 3:
            case 9:
            case 10:
                updateOrderedEvent(OrderedEvents.POWERKEY, true, false);
                return;
            case 6:
                updateOrderedEvent(OrderedEvents.TOUCHDOWN_POWERKEY, true, true);
                return;
            default:
                LogUtil.w("FingerprintService.UnlockController", "Undefined last orderedEvent = " + this.mOrderedEvent);
                return;
        }
    }

    public void dispatchTouchUp() {
        LogUtil.d("FingerprintService.UnlockController", "dispatchTouchUp last orderedEvent = " + this.mOrderedEvent + ", mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther);
        if (this.mOrderedEvent == OrderedEvents.TOUCHDOWN) {
            setOrderedEvent(OrderedEvents.TOUCHDOWN_TOUCHUP);
        } else {
            setOrderedEvent(OrderedEvents.TOUCHUP);
        }
        handlerUpdateOrderedEvent();
        resetAuthenticated();
        if (this.mIsScreenOff && (this.mIsScreenOnUnBlockedByOther ^ 1) != 0) {
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
            for (Object obj : UNLOCKSTATES) {
                this.mUnlockState = null;
                this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get(obj);
                if (this.mUnlockState != null) {
                    this.mUnlockState.dispatchScreenOff(isScreenOff);
                }
            }
            this.mIUnLocker.onScreenOff(isScreenOff);
        }
    }

    public void onWakeUp(String wakeupReason) {
        LogUtil.d("FingerprintService.UnlockController", "onWakeUp, wakeupReason = " + wakeupReason);
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
        LogUtil.d("FingerprintService.UnlockController", "onWakeUpFinish");
        this.mIsWakeUpFinishedByOther = true;
        this.mIsScreenOnUnBlockedByOther = false;
        dispatchScreenOff(false);
        this.mHandler.removeMessage(3);
    }

    public void onGoToSleep() {
        LogUtil.d("FingerprintService.UnlockController", "onGoToSleep");
        this.mIsWakeUpByFingerprint = false;
        dispatchScreenOff(true);
    }

    public void onGoToSleepFinish() {
        LogUtil.d("FingerprintService.UnlockController", "onGoToSleepFinish");
        this.mIsScreenOnUnBlockedByOther = false;
        dispatchScreenOff(true);
    }

    public void onScreenOnUnBlockedByOther(String unBlockedReason) {
        LogUtil.d("FingerprintService.UnlockController", "onScreenOnUnBlockedByOther unBlockedReason = " + unBlockedReason);
        this.mIsScreenOnUnBlockedByOther = true;
        this.mHandler.removeMessage(3);
        this.mBiometricsManager.setKeyguardTransparent(FingerprintService.TAG, unBlockedReason);
    }

    public void onScreenOnUnBlockedByFingerprint(boolean authenticated) {
        LogUtil.d("FingerprintService.UnlockController", "onScreenOnUnBlockedByFingerprint authenticated = " + authenticated);
        if (authenticated) {
            handleWaitKeyguardHidden();
            return;
        }
        this.mKeyguardPolicy.forceRefresh();
        handleWaitKeyguardShowed();
    }

    private void handleWaitKeyguardShowed() {
        LogUtil.d("FingerprintService.UnlockController", "handleWaitKeyguardShowed");
        getFPMS().unblockScreenOn(BiometricsService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_FAIL, 150);
    }

    private void handleWaitKeyguardHidden() {
        this.mHandler.sendMessage(7);
    }

    public void dispatchAcquired(int acquiredInfo) {
        if (OrderedEvents.TOUCHDOWN_HOMEKEY == this.mOrderedEvent && acquiredInfo != 0) {
            LogUtil.d("FingerprintService.UnlockController", "dispatchAcquired acquired bad, just screen on");
            this.mHandler.sendMessage(2);
        } else if (OrderedEvents.TOUCHDOWN == this.mOrderedEvent && acquiredInfo != 0) {
            this.mAcquiredBad = true;
        } else if (acquiredInfo == 0 && (this.mIsScreenOff ^ 1) != 0) {
            if (OrderedEvents.POWERKEY_TOUCHDOWN == this.mOrderedEvent || OrderedEvents.TOUCHDOWN_POWERKEY == this.mOrderedEvent) {
                updateOrderedEvent(OrderedEvents.TOUCHDOWN, true, true);
            }
        }
    }

    public void dispatchAuthenticated(AuthenticatedInfo authenticatedInfo) {
        LogUtil.d("FingerprintService.UnlockController", "dispatchAuthenticated");
        if (authenticatedInfo.fingerId != 0) {
            this.mHandler.removeMessage(3);
        }
        for (Object obj : UNLOCKSTATES) {
            this.mUnlockState = null;
            this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get(obj);
            if (this.mUnlockState != null) {
                this.mUnlockState.dispatchAuthenticated(authenticatedInfo);
            }
        }
    }

    public void dispatchImageDirtyAuthenticated() {
        LogUtil.d("FingerprintService.UnlockController", "dispatchImageDirtyAuthenticated");
    }

    public void reset() {
        LogUtil.d("FingerprintService.UnlockController", "reset last orderedEvent = " + this.mOrderedEvent);
        setOrderedEvent(OrderedEvents.NONE);
        handlerUpdateOrderedEvent();
        resetAuthenticated();
    }

    private void resetAuthenticated() {
        LogUtil.d("FingerprintService.UnlockController", "reset Authenticated");
        for (Object obj : UNLOCKSTATES) {
            this.mUnlockState = null;
            this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get(obj);
            if (this.mUnlockState != null) {
                this.mUnlockState.resetAuthenticated();
            }
        }
    }

    public void userActivity() {
        this.mHandler.sendSyncMessage(1);
    }

    public void dispatchScreenOnTimeOut() {
    }

    public void onLightScreenOnFinish() {
        LogUtil.d("FingerprintService.UnlockController", "onLightScreenOnFinish mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther + ", mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        if (!this.mIsScreenOnUnBlockedByOther && this.mIsWakeUpByFingerprint && this.mIsFingerprintUnlockSwitchOpened) {
            this.mIUnLocker.sendUnlockTime(FingerprintAcquiredInfo.ACQUIRED_ALREADY_ENROLLED, SystemClock.uptimeMillis());
        }
    }

    private void handleSendTouchDownTime(int type, long time) {
        LogUtil.d("FingerprintService.UnlockController", "handleSendTouchDownTime");
        this.mIUnLocker.sendUnlockTime(type, time);
    }

    private void trunScreenOnByRightFinger() {
        LogUtil.d("FingerprintService.UnlockController", "trunScreenOnByRightFinger mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther + ", mIsWakeUpByFingerprint = " + this.mIsWakeUpByFingerprint);
        if (!this.mIsScreenOnUnBlockedByOther && this.mIsWakeUpByFingerprint && this.mIsScreenOff) {
            this.mHandler.obtainMessage(9, 1000, 0, Long.valueOf(this.mTouchDownTime)).sendToTarget();
        }
        getFPMS().unblockScreenOn(BiometricsService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_SUCESS, 0);
    }

    private FingerprintPowerManager getFPMS() {
        return FingerprintPowerManager.getFingerprintPowerManager();
    }

    public void dispatchAuthForDropHomeKey() {
        if (this.mIsScreenOff || !this.mHomeKeyDown) {
            if (!(this.mIsScreenOff || (this.mHomeKeyDown ^ 1) == 0)) {
                LogUtil.d("FingerprintService.UnlockController", "drop home key within 300ms");
                this.mHandler.removeMessage(4);
                System.putInt(this.mContext.getContentResolver(), "drop_home_key_when_use_fingerprint", 1);
                this.mHandler.sendMessageDelayed(4, 300);
            }
            return;
        }
        LogUtil.d("FingerprintService.UnlockController", "drop home key");
        System.putInt(this.mContext.getContentResolver(), "drop_home_key_when_use_fingerprint", 1);
    }

    public void onSettingChanged(String settingName, boolean isOn) {
        if (FingerprintSettings.FINGERPRINT_UNLOCK_SWITCH.equals(settingName)) {
            this.mIsFingerprintUnlockSwitchOpened = isOn;
        }
    }

    public void onProximitySensorChanged(boolean isNear) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }
}
