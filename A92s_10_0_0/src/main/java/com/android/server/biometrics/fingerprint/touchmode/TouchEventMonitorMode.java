package com.android.server.biometrics.fingerprint.touchmode;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import com.android.server.biometrics.BiometricWakeupManagerService;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.biometrics.fingerprint.FingerprintSwitchHelper;
import com.android.server.biometrics.fingerprint.power.FingerprintPowerManager;
import com.android.server.biometrics.fingerprint.sensor.ProximitySensorManager;
import com.android.server.biometrics.fingerprint.tool.ExHandler;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import com.android.server.biometrics.fingerprint.util.SupportUtil;

public class TouchEventMonitorMode {
    public static final int MSG_CHECK_TP_PROTECT_RESULT = 17;
    public static final int MSG_GOTO_SLEEP_FROM_WAKE0 = 4;
    public static final int MSG_WAKE_UP_BY_FINGERPRINT = 1;
    public static final int TIME_OUT_FOR_CHECK_TP_PROECT = 30;
    private final String TAG = "FingerprintService.TouchEventMonitorMode";
    private Context mContext;
    private int mCurrentTpProtectRetryCounter = 0;
    private ExHandler mHandler;
    private HandlerThread mHandlerThread;
    private FingerprintService.IUnLocker mIUnLocker;
    private boolean mIsFrontTouchTpProtect = false;
    private boolean mIsNearState = false;
    private boolean mIsScreenOff = false;
    private Looper mLooper;
    private String mSensorType;
    private boolean mTouchDownInKeyguardLockMode = false;

    public TouchEventMonitorMode(Context context, FingerprintService.IUnLocker unLocker) {
        LogUtil.d("FingerprintService.TouchEventMonitorMode", "TouchEventMonitorMode construction");
        this.mContext = context;
        this.mIUnLocker = unLocker;
        this.mSensorType = SupportUtil.getSensorType(this.mContext);
        this.mHandlerThread = new HandlerThread("TouchEventMonitorMode thread");
        this.mHandlerThread.start();
        this.mLooper = this.mHandlerThread.getLooper();
        if (this.mLooper == null) {
            LogUtil.e("FingerprintService.TouchEventMonitorMode", "mLooper null");
        }
        initHandler();
        this.mIsFrontTouchTpProtect = SupportUtil.isFrontTouchFingerprintTpProtect(this.mContext);
        updateTpProtectState();
    }

    public void onFingerprintSwitchUpdate() {
        updateTpProtectState();
    }

    private void updateTpProtectState() {
        boolean defaultValue = SupportUtil.isFrontTouchFingerprintTpProtect(this.mContext);
        if (defaultValue) {
            this.mIsFrontTouchTpProtect = SystemProperties.getBoolean(FingerprintSwitchHelper.PROP_NAME_TP_PROTECT_SWITCH, defaultValue);
            LogUtil.d("FingerprintService.TouchEventMonitorMode", "updateTpProtectState change mIsFrontTouchTpProtect -> " + this.mIsFrontTouchTpProtect + " in tpprotect device");
        }
    }

    public void notifyScreenon() {
        if (SupportUtil.FRONT_TOUCH_SENSOR.equals(this.mSensorType) || SupportUtil.BACK_TOUCH_SENSOR.equals(this.mSensorType)) {
            if (this.mIsFrontTouchTpProtect) {
                getFPMS().unblockScreenOn(BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_AUTHENTICATE_FAIL, 0);
            } else {
                getFPMS().wakeupNormal();
            }
        } else if (SupportUtil.FRONT_PRESS_SENSOR.equals(this.mSensorType)) {
            getFPMS().wakeupNormal();
        }
    }

    public void startTouchMonitor() {
        getPsensorManager().onTouchMonitorStarted(true);
    }

    public void stopTouchMonitor() {
        getPsensorManager().onTouchMonitorStarted(false);
    }

    private ProximitySensorManager getPsensorManager() {
        return ProximitySensorManager.getProximitySensorManager();
    }

    public void dispatchScreenOff(boolean isScreenOff) {
        LogUtil.d("FingerprintService.TouchEventMonitorMode", "dispatchScreenOff isScreenOff = " + isScreenOff);
        this.mIsScreenOff = isScreenOff;
        if (!isScreenOff) {
            this.mHandler.removeMessage(4);
        }
    }

    public void onProximitySensorChanged(boolean isNear) {
        this.mIsNearState = isNear;
    }

    public void dispatchTouchDown() {
        LogUtil.d("FingerprintService.TouchEventMonitorMode", "dispatchTouchDown");
        this.mHandler.removeMessage(4);
        if (this.mIsNearState) {
            this.mHandler.sendSyncMessage(4);
        } else if (SupportUtil.FRONT_TOUCH_SENSOR.equals(this.mSensorType) || SupportUtil.BACK_TOUCH_SENSOR.equals(this.mSensorType)) {
            dispatchTouchDownInKeyguardLockMode();
        } else if (SupportUtil.FRONT_PRESS_SENSOR.equals(this.mSensorType)) {
            this.mIUnLocker.dispatchTouchEventInLockMode();
        }
    }

    public void dispatchTouchUp() {
        LogUtil.d("FingerprintService.TouchEventMonitorMode", "dispatchTouchUp");
        if (SupportUtil.FRONT_TOUCH_SENSOR.equals(this.mSensorType) || SupportUtil.BACK_TOUCH_SENSOR.equals(this.mSensorType)) {
            dispatchTouchUpInKeyguardLockMode();
        } else if (!SupportUtil.FRONT_PRESS_SENSOR.equals(this.mSensorType)) {
        }
    }

    private void dispatchTouchUpInKeyguardLockMode() {
        LogUtil.d("FingerprintService.TouchEventMonitorMode", "mIsFrontTouchTpProtect = " + this.mIsFrontTouchTpProtect);
        if (this.mIsFrontTouchTpProtect) {
            this.mHandler.removeMessage(17);
            this.mCurrentTpProtectRetryCounter = 0;
        }
        updateFingerStateInKeyguardLockMode(false);
    }

    private void dispatchTouchDownInKeyguardLockMode() {
        LogUtil.d("FingerprintService.TouchEventMonitorMode", "mIsFrontTouchTpProtect = " + this.mIsFrontTouchTpProtect);
        if (!this.mIsFrontTouchTpProtect || !this.mIsScreenOff) {
            this.mIUnLocker.dispatchTouchEventInLockMode();
        } else {
            this.mHandler.removeMessage(4);
            this.mHandler.sendSyncMessage(1);
            this.mHandler.sendSyncMessageDelayed(17, 30);
            this.mCurrentTpProtectRetryCounter = 0;
        }
        updateFingerStateInKeyguardLockMode(true);
    }

    private void updateFingerStateInKeyguardLockMode(boolean touchDown) {
        synchronized (this) {
            this.mTouchDownInKeyguardLockMode = touchDown;
        }
        LogUtil.d("FingerprintService.TouchEventMonitorMode", "mTouchDownInKeyguardLockMode = " + this.mTouchDownInKeyguardLockMode);
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mLooper) {
            /* class com.android.server.biometrics.fingerprint.touchmode.TouchEventMonitorMode.AnonymousClass1 */

            @Override // com.android.server.biometrics.fingerprint.tool.ExHandler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    TouchEventMonitorMode.this.getFPMS().blockScreenOn("android.service.fingerprint:WAKEUP");
                } else if (i == 4) {
                    TouchEventMonitorMode.this.getFPMS().gotoSleep();
                } else if (i == 17) {
                    TouchEventMonitorMode.this.checkTpProtectResult();
                }
                super.handleMessage(msg);
            }
        };
    }

    /* access modifiers changed from: private */
    public void checkTpProtectResult() {
        int tpProtectResult = SupportUtil.getTpProtectResult();
        LogUtil.d("FingerprintService.TouchEventMonitorMode", "tpProtectResult = " + tpProtectResult + ", mCurrentTpProtectRetryCounter = " + this.mCurrentTpProtectRetryCounter);
        if (1 == tpProtectResult) {
            this.mCurrentTpProtectRetryCounter = 0;
            this.mIUnLocker.dispatchMonitorEvent(2, String.valueOf(3));
            this.mHandler.sendSyncMessage(4);
            LogUtil.d("FingerprintService.TouchEventMonitorMode", "goto sleep when tp protected");
        } else if (tpProtectResult == 0) {
            this.mIUnLocker.dispatchTouchEventInLockMode();
            this.mCurrentTpProtectRetryCounter = 0;
            LogUtil.d("FingerprintService.TouchEventMonitorMode", "dispatchTouchEvent to keyguard when tp not protected");
        } else if (2 != tpProtectResult) {
        } else {
            if (!getFingerStateInKeyguardLockMode()) {
                LogUtil.d("FingerprintService.TouchEventMonitorMode", "do not notify keyguard this touch event");
                this.mHandler.sendSyncMessage(4);
            } else if (this.mCurrentTpProtectRetryCounter < 15) {
                this.mHandler.sendSyncMessageDelayed(17, 30);
                this.mCurrentTpProtectRetryCounter++;
                LogUtil.d("FingerprintService.TouchEventMonitorMode", "need check tpprotect result again");
            } else {
                this.mIUnLocker.dispatchTouchEventInLockMode();
                this.mCurrentTpProtectRetryCounter = 0;
                LogUtil.d("FingerprintService.TouchEventMonitorMode", "dispatchTouchEvent to keyguard when tpprotect result not found");
            }
        }
    }

    private boolean getFingerStateInKeyguardLockMode() {
        boolean fingerTouchDown;
        synchronized (this) {
            fingerTouchDown = this.mTouchDownInKeyguardLockMode;
        }
        return fingerTouchDown;
    }

    /* access modifiers changed from: private */
    public FingerprintPowerManager getFPMS() {
        return FingerprintPowerManager.getFingerprintPowerManager();
    }
}
