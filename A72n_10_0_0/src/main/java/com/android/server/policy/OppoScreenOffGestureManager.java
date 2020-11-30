package com.android.server.policy;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IOppoExService;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.server.policy.keyguard.KeyguardServiceDelegate;

public class OppoScreenOffGestureManager {
    public static final int GESTURE_CIRCLE = 6;
    public static final int GESTURE_DOUBLE_SWIP = 7;
    public static final int GESTURE_DOUBLE_TAP = 1;
    public static final int GESTURE_DOWN_TO_UP_SWIP = 11;
    public static final int GESTURE_DOWN_VEE = 3;
    public static final int GESTURE_LEFT_TO_RIGHT_SWIP = 8;
    public static final int GESTURE_LEFT_VEE = 4;
    public static final int GESTURE_M = 12;
    public static final int GESTURE_RIGHT_TO_LEFT_SWIP = 9;
    public static final int GESTURE_RIGHT_VEE = 5;
    public static final int GESTURE_UP_TO_DOWN_SWIP = 10;
    public static final int GESTURE_UP_VEE = 2;
    public static final int GESTURE_W = 13;
    public static final int MSG_SCREEN_TURNED_OFF = 10001;
    public static final int MSG_SCREEN_TURNING_ON = 10000;
    private static final String TAG = "OppoScreenOffGestureManager";
    private static final int WAIT_TIME_CPU_LOCK = 1000;
    PhoneStateListener listener = new PhoneStateListener() {
        /* class com.android.server.policy.OppoScreenOffGestureManager.AnonymousClass1 */

        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Log.d(OppoScreenOffGestureManager.TAG, "onCallStateChanged state = " + state);
            if (state == 0) {
                OppoScreenOffGestureManager.this.mIsInOffHook = false;
            } else if (state == 1) {
                OppoScreenOffGestureManager.this.mIsInOffHook = true;
            } else if (state == 2) {
                OppoScreenOffGestureManager.this.mIsInOffHook = true;
            }
        }
    };
    private PowerManager.WakeLock mAnimCpuLock;
    private AudioManager mAudioManager = null;
    private IOppoExService mExManager = null;
    private OppoScreenOffGestureUtil mGestureUtil = null;
    private boolean mIsInOffHook = false;
    private PowerManager mPowerManager;

    private class AnimDataInfo {
        public int mMode = 0;

        AnimDataInfo(int nMode) {
            this.mMode = nMode;
        }
    }

    OppoScreenOffGestureManager(Context context, Handler handler, KeyguardServiceDelegate keyguardMediator, PowerManager.WakeLock broadcastWakeLock) {
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mAnimCpuLock = this.mPowerManager.newWakeLock(1, "OppoScreenOffGestureManager.mAnimCpuLock");
        this.mGestureUtil = new OppoScreenOffGestureUtil(context);
        ((TelephonyManager) context.getSystemService("phone")).listen(this.listener, 32);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
    }

    /* access modifiers changed from: package-private */
    public int dealScreenOffGesture(KeyEvent event, int policyFlags, boolean isScreenOn) {
        int keyCode = event.getKeyCode();
        boolean down = event.getAction() == 0;
        int policyFlags2 = policyFlags & -1073741825;
        if (!isScreenOn || this.mIsInOffHook) {
            if (keyCode == 134 && down) {
                int nGesture = this.mGestureUtil.mGestureType;
                if (!isGestureExist(nGesture)) {
                    return policyFlags2;
                }
                this.mAnimCpuLock.acquire(1000);
                Log.d(TAG, "dealScreenOffGesture is " + gestureTosString(nGesture));
                dealExScreenOffGesture(nGesture);
            }
            return policyFlags2;
        }
        Log.d(TAG, "-----  return");
        return policyFlags2;
    }

    /* access modifiers changed from: package-private */
    public void screenTurnedOff() {
        if (this.mExManager == null) {
            this.mExManager = IOppoExService.Stub.asInterface(ServiceManager.getService("OPPOExService"));
        }
        IOppoExService iOppoExService = this.mExManager;
        if (iOppoExService == null) {
            Slog.e(TAG, "mExManager == null!!!");
            return;
        }
        try {
            iOppoExService.dealScreenoffGesture((int) MSG_SCREEN_TURNED_OFF);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failing screenTurnedOff", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void screenTurningOn() {
        if (this.mExManager == null) {
            this.mExManager = IOppoExService.Stub.asInterface(ServiceManager.getService("OPPOExService"));
        }
        IOppoExService iOppoExService = this.mExManager;
        if (iOppoExService == null) {
            Slog.e(TAG, "mExManager == null!!!");
            return;
        }
        try {
            iOppoExService.dealScreenoffGesture(10000);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failing screenTurningOn", e);
        }
    }

    private String gestureTosString(int nGesture) {
        switch (nGesture) {
            case 1:
                return "GESTURE_DOUBLE_TAP";
            case 2:
                return "GESTURE_UP_VEE";
            case 3:
                return "GESTURE_DOWN_VEE";
            case 4:
                return "GESTURE_LEFT_VEE";
            case 5:
                return "GESTURE_RIGHT_VEE";
            case 6:
                return "GESTURE_CIRCLE";
            case 7:
                return "GESTURE_DOUBLE_SWIP";
            case 8:
                return "GESTURE_LEFT_TO_RIGHT_SWIP";
            case 9:
                return "GESTURE_RIGHT_TO_LEFT_SWIP";
            case 10:
                return "GESTURE_UP_TO_DOWN_SWIP";
            case 11:
                return "GESTURE_DOWN_TO_UP_SWIP";
            case 12:
                return "GESTURE_M";
            case 13:
                return "GESTURE_W";
            default:
                return "";
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isScreenoffGestureKey(int keyCode) {
        return 134 == keyCode;
    }

    private void dealExScreenOffGesture(int nGesture) {
        if (this.mExManager == null) {
            this.mExManager = IOppoExService.Stub.asInterface(ServiceManager.getService("OPPOExService"));
        }
        if (this.mExManager == null) {
            Slog.e(TAG, "mExManager == null!!!");
            return;
        }
        Log.d(TAG, "OppoScreenOffGestureManager  dealScreenoffGesture nGesture = " + nGesture + "  mExManager = " + this.mExManager);
        try {
            this.mExManager.dealScreenoffGesture(nGesture);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failing dealScreenoffGesture", e);
        }
    }

    private boolean isGestureExist(int nGesture) {
        boolean isGestureExist = false;
        if (this.mExManager == null) {
            this.mExManager = IOppoExService.Stub.asInterface(ServiceManager.getService("OPPOExService"));
        }
        IOppoExService iOppoExService = this.mExManager;
        if (iOppoExService == null) {
            Slog.e(TAG, "mExManager == null!!!");
            return false;
        }
        try {
            isGestureExist = iOppoExService.getGestureState(nGesture);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failing getGestureState", e);
        }
        Log.d(TAG, "OppoScreenOffGestureManager isGestureExist = " + isGestureExist);
        return isGestureExist;
    }

    /* access modifiers changed from: package-private */
    public boolean isInOffHook() {
        return this.mIsInOffHook && !this.mAudioManager.isSpeakerphoneOn() && !this.mAudioManager.isWiredHeadsetOn();
    }

    /* access modifiers changed from: package-private */
    public boolean isGestureDoubleTap() {
        return this.mGestureUtil.mGestureType == 1;
    }

    public void updateGestureInfo() {
        this.mGestureUtil.updateGestureInfo();
    }
}
