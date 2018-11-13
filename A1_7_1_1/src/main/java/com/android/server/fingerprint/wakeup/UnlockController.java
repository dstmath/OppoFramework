package com.android.server.fingerprint.wakeup;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.System;
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

public class UnlockController implements IFingerprintSensorEventListener {
    /* renamed from: -com-android-server-fingerprint-wakeup-UnlockController$ORDERED_EVENTSSwitchesValues */
    private static final /* synthetic */ int[] f15x2c9fa261 = null;
    public static final int FRAME_REFRESH_WAITING_TIME = 16;
    public static final int GOTO_SLEEP_WAITING_TIME = 500;
    public static final int MSG_GOTO_SLEEP_FROM_WAKE0 = 3;
    public static final int MSG_HIDE_KEYGUARD = 17;
    public static final int MSG_KEEP_SCREEN_ON = 1;
    public static final int MSG_RESET_HOMEKEY_DISPATCH = 4;
    public static final int MSG_SEND_TOUCH_DOWN_TIME = 9;
    public static final int MSG_SHOW_KEYGUARD = 16;
    public static final int MSG_TURN_SCREEN_ON_BY_BAD_ACQUIRE = 2;
    public static final int MSG_TURN_SCREEN_ON_BY_FAIL_RESULT = 8;
    public static final int MSG_TURN_SCREEN_ON_BY_RIGHT_RESULT = 7;
    public static final int MSG_WAIT_KEYGUARD_HIDDEN = 6;
    public static final int MSG_WAIT_KEYGUARD_SHOWED = 5;
    public static final int RESET_HOMEKEY_DISPATCH_WAITING_TIME = 300;
    public static final int SCREEN_ON_WAITING_TIME = 300;
    private final String TAG = "FingerprintService.UnlockController";
    private final String[] UNLOCKSTATES = new String[]{"ScreenOFFUnlock", "WakeUp", "ScreenOn", "ScreenOnUnlock", "DisableAuthentication"};
    private boolean mAcquiredBad = false;
    private Context mContext;
    private DcsFingerprintStatisticsUtil mDcsStatisticsUtil;
    private boolean mEnableKeyguardHide;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHomeKeyDown = false;
    private IUnLocker mIUnLocker;
    private boolean mIsFingerprintUnlockSwitchOpened = false;
    private boolean mIsScreenOff = false;
    private boolean mIsScreenOnUnBlockedByOther = false;
    private boolean mIsSettingOn;
    private boolean mIsWaitingScreenOnByFailResult = false;
    private boolean mIsWakeUpByFingerprint = false;
    private boolean mIsWakeUpFinishedByOther = true;
    private KeyguardPolicy mKeyguardPolicy;
    private int mKeyguardShowedReTryTime = 0;
    private Looper mLooper;
    private ORDERED_EVENTS mOrderedEvent = ORDERED_EVENTS.NONE;
    private boolean mSystemReady = false;
    private long mTouchDownTime;
    private UnlockState mUnlockState;
    private UnlockStateFactory mUnlockStateFactory;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum ORDERED_EVENTS {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.wakeup.UnlockController.ORDERED_EVENTS.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.wakeup.UnlockController.ORDERED_EVENTS.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.wakeup.UnlockController.ORDERED_EVENTS.<clinit>():void");
        }
    }

    /* renamed from: -getcom-android-server-fingerprint-wakeup-UnlockController$ORDERED_EVENTSSwitchesValues */
    private static /* synthetic */ int[] m28x2659d905() {
        if (f15x2c9fa261 != null) {
            return f15x2c9fa261;
        }
        int[] iArr = new int[ORDERED_EVENTS.values().length];
        try {
            iArr[ORDERED_EVENTS.HOMEKEY.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ORDERED_EVENTS.HOMEKEY_TOUCHDOWN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ORDERED_EVENTS.HOMEKEY_TOUCHDOWN_TOUCHUP.ordinal()] = 11;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ORDERED_EVENTS.NONE.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ORDERED_EVENTS.POWERKEY.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ORDERED_EVENTS.POWERKEY_TOUCHDOWN.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ORDERED_EVENTS.POWERKEY_TOUCHDOWN_TOUCHUP.ordinal()] = 12;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ORDERED_EVENTS.TOUCHDOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ORDERED_EVENTS.TOUCHDOWN_HOMEKEY.ordinal()] = 7;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ORDERED_EVENTS.TOUCHDOWN_HOMEKEY_TOUCHUP.ordinal()] = 13;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ORDERED_EVENTS.TOUCHDOWN_POWERKEY.ordinal()] = 8;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ORDERED_EVENTS.TOUCHDOWN_POWERKEY_TOUCHUP.ordinal()] = 14;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ORDERED_EVENTS.TOUCHDOWN_TOUCHUP.ordinal()] = 9;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ORDERED_EVENTS.TOUCHUP.ordinal()] = 10;
        } catch (NoSuchFieldError e14) {
        }
        f15x2c9fa261 = iArr;
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
        this.mEnableKeyguardHide = this.mContext.getPackageManager().hasSystemFeature("oppo.fingerprint.enable.keyguard.hide");
        this.mKeyguardPolicy = KeyguardPolicy.getKeyguardPolicy();
        this.mDcsStatisticsUtil = DcsFingerprintStatisticsUtil.getDcsFingerprintStatisticsUtil(this.mContext);
        this.mUnlockStateFactory = new UnlockStateFactory(this.mContext, this.mIUnLocker);
        updateUnlockSetting(true);
    }

    private void updateUnlockSetting(boolean isOn) {
        LogUtil.d("FingerprintService.UnlockController", "updateUnlockSetting  isOn = " + isOn);
        this.mIsSettingOn = isOn;
        int i;
        if (isOn) {
            for (i = 0; i < this.UNLOCKSTATES.length; i++) {
                this.mUnlockState = null;
                this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get(this.UNLOCKSTATES[i]);
                if (this.mUnlockState != null) {
                    LogUtil.e("FingerprintService.UnlockController", "error in delete logic");
                } else {
                    this.mUnlockState = this.mUnlockStateFactory.createUnlockState(this.UNLOCKSTATES[i]);
                }
            }
            return;
        }
        for (i = 0; i < this.UNLOCKSTATES.length; i++) {
            this.mUnlockState = null;
            this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get(this.UNLOCKSTATES[i]);
            if (this.mUnlockState != null) {
                this.mUnlockStateFactory.deleteUnlockState(this.UNLOCKSTATES[i]);
            } else {
                LogUtil.d("FingerprintService.UnlockController", "this action has been diabled");
            }
        }
    }

    private synchronized void setOrderedEvent(ORDERED_EVENTS event) {
        this.mOrderedEvent = event;
        LogUtil.d("FingerprintService.UnlockController", "set current orderedEvent = " + this.mOrderedEvent);
    }

    private synchronized void handlerUpdateOrderedEvent() {
        for (Object obj : this.UNLOCKSTATES) {
            this.mUnlockState = null;
            this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get(obj);
            if (this.mUnlockState != null) {
                this.mUnlockState.dispatchOrderedEvent(this.mOrderedEvent);
            }
        }
    }

    private synchronized void updateOrderedEvent(ORDERED_EVENTS event, boolean needUpdate, boolean needDispatch) {
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
            if (this.mEnableKeyguardHide) {
                this.mHandler.sendSyncMessage(16);
            } else {
                this.mKeyguardPolicy.dispatchWakeUp(false);
                this.mKeyguardPolicy.setKeyguardVisibility(true);
                this.mKeyguardPolicy.forceRefresh();
            }
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
                    case 5:
                        UnlockController.this.handleWaitKeyguardShowed();
                        break;
                    case 6:
                        UnlockController.this.handleWaitKeyguardHidden();
                        break;
                    case 7:
                        UnlockController.this.trunScreenOnByRightFinger();
                        break;
                    case 8:
                        LogUtil.d("FingerprintService.UnlockController", "trun screen on by fail result");
                        UnlockController.this.mIsWaitingScreenOnByFailResult = false;
                        UnlockController.this.getFPMS().wakeup(-1);
                        break;
                    case 9:
                        UnlockController.this.handleSendTouchDownTime(msg.arg1, ((Long) msg.obj).longValue());
                        break;
                    case 16:
                        UnlockController.this.mKeyguardPolicy.showKeyguard();
                        break;
                    case 17:
                        UnlockController.this.mKeyguardPolicy.hideKeyguard();
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
        switch (m28x2659d905()[this.mOrderedEvent.ordinal()]) {
            case 1:
                updateOrderedEvent(ORDERED_EVENTS.HOMEKEY_TOUCHDOWN, true, true);
                break;
            case 3:
            case 8:
            case 9:
            case 10:
                if (!this.mIsWaitingScreenOnByFailResult && this.mIsWakeUpFinishedByOther && this.mIsScreenOff && !this.mIsScreenOnUnBlockedByOther) {
                    if (this.mEnableKeyguardHide) {
                        this.mHandler.sendMessage(17);
                    } else {
                        this.mKeyguardPolicy.dispatchWakeUp(true);
                    }
                }
                updateOrderedEvent(ORDERED_EVENTS.TOUCHDOWN, true, true);
                if (!this.mIsScreenOff) {
                    this.mHandler.obtainMessage(9, 1001, 0, Long.valueOf(this.mTouchDownTime)).sendToTarget();
                    break;
                }
                break;
            case 4:
                updateOrderedEvent(ORDERED_EVENTS.POWERKEY_TOUCHDOWN, true, true);
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
            switch (m28x2659d905()[this.mOrderedEvent.ordinal()]) {
                case 1:
                    updateOrderedEvent(ORDERED_EVENTS.HOMEKEY, false, true);
                    break;
                case 2:
                case 5:
                case 8:
                    break;
                case 3:
                case 4:
                case 9:
                case 10:
                    updateOrderedEvent(ORDERED_EVENTS.HOMEKEY, true, true);
                    break;
                case 6:
                    if (this.mAcquiredBad) {
                        LogUtil.d("FingerprintService.UnlockController", "dispatchHomeKeyDown acquired bad, just screen on");
                        this.mHandler.sendMessage(2);
                    }
                    updateOrderedEvent(ORDERED_EVENTS.TOUCHDOWN_HOMEKEY, true, true);
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
            if (ORDERED_EVENTS.HOMEKEY == this.mOrderedEvent) {
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
        switch (m28x2659d905()[this.mOrderedEvent.ordinal()]) {
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
                updateOrderedEvent(ORDERED_EVENTS.POWERKEY, true, false);
                return;
            case 6:
                updateOrderedEvent(ORDERED_EVENTS.TOUCHDOWN_POWERKEY, true, true);
                return;
            default:
                LogUtil.w("FingerprintService.UnlockController", "Undefined last orderedEvent = " + this.mOrderedEvent);
                return;
        }
    }

    public void dispatchTouchUp() {
        LogUtil.d("FingerprintService.UnlockController", "dispatchTouchUp last orderedEvent = " + this.mOrderedEvent + ", mIsScreenOnUnBlockedByOther = " + this.mIsScreenOnUnBlockedByOther);
        if (this.mOrderedEvent == ORDERED_EVENTS.TOUCHDOWN) {
            setOrderedEvent(ORDERED_EVENTS.TOUCHDOWN_TOUCHUP);
        } else {
            setOrderedEvent(ORDERED_EVENTS.TOUCHUP);
        }
        handlerUpdateOrderedEvent();
        resetAuthenticated();
        if (this.mIsScreenOff && !this.mIsScreenOnUnBlockedByOther) {
            this.mHandler.sendSyncMessageDelayed(3, 500);
        }
    }

    public synchronized void dispatchScreenOff(boolean isScreenOff) {
        if (this.mOrderedEvent == ORDERED_EVENTS.POWERKEY || this.mOrderedEvent == ORDERED_EVENTS.HOMEKEY) {
            LogUtil.d("FingerprintService.UnlockController", "Screen state changed before touch down, reset lastorderedEvent = " + this.mOrderedEvent);
            updateOrderedEvent(ORDERED_EVENTS.NONE, true, true);
        }
        if (this.mIsScreenOff != isScreenOff) {
            LogUtil.d("FingerprintService.UnlockController", "dispatchScreenOff mIsScreenOff = " + isScreenOff);
            this.mIsScreenOff = isScreenOff;
            for (Object obj : this.UNLOCKSTATES) {
                this.mUnlockState = null;
                this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get(obj);
                if (this.mUnlockState != null) {
                    this.mUnlockState.dispatchScreenOff(isScreenOff);
                }
            }
            this.mIUnLocker.onScreenOff(isScreenOff);
        }
    }

    public void onWakeUp(boolean isWakeUpByFingerprint) {
        LogUtil.d("FingerprintService.UnlockController", "onWakeUp, isWakeUpByFingerprint = " + isWakeUpByFingerprint);
        if (isWakeUpByFingerprint) {
            this.mIsWakeUpByFingerprint = true;
            return;
        }
        this.mIsWakeUpByFingerprint = false;
        this.mIsWakeUpFinishedByOther = false;
        if (this.mEnableKeyguardHide) {
            this.mHandler.sendSyncMessage(16);
        } else {
            this.mKeyguardPolicy.dispatchWakeUp(false);
        }
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

    public void onScreenOnUnBlockedByOther() {
        LogUtil.d("FingerprintService.UnlockController", "onScreenOnUnBlockedByOther");
        this.mIsScreenOnUnBlockedByOther = true;
        this.mHandler.removeMessage(3);
        if (this.mEnableKeyguardHide) {
            this.mHandler.sendSyncMessage(16);
            return;
        }
        this.mKeyguardPolicy.setKeyguardVisibility(true);
        this.mKeyguardPolicy.dispatchWakeUp(false);
    }

    public void onScreenOnUnBlockedByFingerprint(boolean authenticated) {
        LogUtil.d("FingerprintService.UnlockController", "onScreenOnUnBlockedByFingerprint authenticated = " + authenticated);
        if (authenticated) {
            handleWaitKeyguardHidden();
            return;
        }
        if (!this.mEnableKeyguardHide) {
            this.mKeyguardPolicy.setKeyguardVisibility(true);
            this.mKeyguardPolicy.forceRefresh();
        }
        handleWaitKeyguardShowed();
    }

    private void handleWaitKeyguardShowed() {
        LogUtil.d("FingerprintService.UnlockController", "handleWaitKeyguardShowed");
        if (this.mEnableKeyguardHide) {
            this.mIsWaitingScreenOnByFailResult = true;
            this.mHandler.sendMessageDelayed(8, 150);
            return;
        }
        int result = this.mKeyguardPolicy.hasKeyguard();
        if ((result == 0 || result == -1) && this.mKeyguardShowedReTryTime < 15) {
            LogUtil.d("FingerprintService.UnlockController", "keyguard hidden");
            this.mKeyguardShowedReTryTime++;
            this.mHandler.sendMessageDelayed(5, 16);
            return;
        }
        LogUtil.d("FingerprintService.UnlockController", "keyguard showed");
        this.mKeyguardShowedReTryTime = 0;
        this.mIsWaitingScreenOnByFailResult = true;
        this.mHandler.sendMessageDelayed(8, 150);
    }

    private void handleWaitKeyguardHidden() {
        this.mHandler.sendMessage(7);
    }

    public void dispatchAcquired(int acquiredInfo) {
        if (ORDERED_EVENTS.TOUCHDOWN_HOMEKEY == this.mOrderedEvent && acquiredInfo != 0) {
            LogUtil.d("FingerprintService.UnlockController", "dispatchAcquired acquired bad, just screen on");
            this.mHandler.sendMessage(2);
        } else if (ORDERED_EVENTS.TOUCHDOWN == this.mOrderedEvent && acquiredInfo != 0) {
            this.mAcquiredBad = true;
        } else if (acquiredInfo == 0 && !this.mIsScreenOff) {
            if (ORDERED_EVENTS.POWERKEY_TOUCHDOWN == this.mOrderedEvent || ORDERED_EVENTS.TOUCHDOWN_POWERKEY == this.mOrderedEvent) {
                updateOrderedEvent(ORDERED_EVENTS.TOUCHDOWN, true, true);
            }
        }
    }

    public void dispatchAuthenticated(AuthenticatedInfo authenticatedInfo) {
        LogUtil.d("FingerprintService.UnlockController", "dispatchAuthenticated");
        if (authenticatedInfo.fingerId != 0) {
            this.mHandler.removeMessage(3);
        } else if (this.mEnableKeyguardHide) {
            this.mHandler.sendSyncMessage(16);
        }
        for (Object obj : this.UNLOCKSTATES) {
            this.mUnlockState = null;
            this.mUnlockState = (UnlockState) this.mUnlockStateFactory.mStateFactoryInstance.get(obj);
            if (this.mUnlockState != null) {
                this.mUnlockState.dispatchAuthenticated(authenticatedInfo);
            }
        }
    }

    public void dispatchImageDirtyAuthenticated() {
        LogUtil.d("FingerprintService.UnlockController", "dispatchImageDirtyAuthenticated mEnableKeyguardHide = " + this.mEnableKeyguardHide);
        if (this.mEnableKeyguardHide) {
            this.mHandler.sendSyncMessage(16);
        }
    }

    public void reset() {
        LogUtil.d("FingerprintService.UnlockController", "reset last orderedEvent = " + this.mOrderedEvent);
        setOrderedEvent(ORDERED_EVENTS.NONE);
        handlerUpdateOrderedEvent();
        resetAuthenticated();
    }

    private void resetAuthenticated() {
        LogUtil.d("FingerprintService.UnlockController", "reset Authenticated");
        for (Object obj : this.UNLOCKSTATES) {
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
            this.mIUnLocker.sendUnlockTime(1002, SystemClock.uptimeMillis());
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
        getFPMS().wakeup(1);
    }

    private FingerprintPowerManager getFPMS() {
        return FingerprintPowerManager.getFingerprintPowerManager();
    }

    public void dispatchAuthForDropHomeKey() {
        if (this.mIsScreenOff || !this.mHomeKeyDown) {
            if (!(this.mIsScreenOff || this.mHomeKeyDown)) {
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
