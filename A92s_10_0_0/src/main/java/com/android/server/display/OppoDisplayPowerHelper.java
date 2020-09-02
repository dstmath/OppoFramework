package com.android.server.display;

import android.animation.Animator;
import android.content.Context;
import android.hardware.display.DisplayManagerInternal;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManagerInternal;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.biometrics.BiometricsManagerInternal;
import com.android.server.oppo.ScreenOnCpuBoostHelper;
import com.android.server.policy.WindowManagerPolicy;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

class OppoDisplayPowerHelper {
    static boolean DEBUG_PANIC = false;
    public static final int EVENT_ANIMATION_END = 2;
    public static final int EVENT_ANIMATION_STARTED = 1;
    private static final int MSG_FIRST_INDEX = 1000;
    private static final int MSG_SCREEN_OFF_COLOR_FADE_TIMEOUT = 1002;
    private static final int MSG_SCREEN_ON_UNBLOCKED_BY_BIOMETRICS = 1001;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static final String SCREEN_ON_BLOCKED_BY_BIOMETRICS_TRACE_NAME = "Screen on blocked by biometrics";
    private static final String TAG = "DisplayPowerController_Helper";
    private static final String TAG_BIOMETRICS = "Biometrics_DEBUG";
    private BiometricsManagerInternal mBiometricsManager;
    private final ArrayList<String> mBlockReasonList = new ArrayList<>();
    /* access modifiers changed from: private */
    public final DisplayManagerInternal.DisplayPowerCallbacks mCallbacks;
    private Context mContext;
    /* access modifiers changed from: private */
    public final DisplayPowerController mDpc;
    /* access modifiers changed from: private */
    public final OppoDisplayPowerHelperHandler mHandler;
    private final Object mLock;
    private final Runnable mOnProximityNegativeSuspendRunnable = new Runnable() {
        /* class com.android.server.display.OppoDisplayPowerHelper.AnonymousClass2 */

        public void run() {
            OppoDisplayPowerHelper.this.mCallbacks.onProximityNegativeForceSuspend();
            OppoDisplayPowerHelper.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private final Runnable mOnProximityPositiveSuspendRunnable = new Runnable() {
        /* class com.android.server.display.OppoDisplayPowerHelper.AnonymousClass1 */

        public void run() {
            OppoDisplayPowerHelper.this.mCallbacks.onProximityPositiveForceSuspend();
            OppoDisplayPowerHelper.this.mCallbacks.releaseSuspendBlocker();
        }
    };
    private boolean mOppoScreenOffBecauseOfProximity;
    private boolean mOppoWaitingForNegativeProximity;
    private boolean mPendingDisplayReadyBlocker = false;
    /* access modifiers changed from: private */
    public ScreenOnUnblockerByBiometrics mPendingScreenOnUnblockerFromBiometrics;
    private final PowerManagerInternal mPowerManagerInternal;
    private DisplayPowerState mPowerState;
    private boolean mProximityEventHandled = true;
    private boolean mProximitySensorEnabled = false;
    private boolean mScreenOnBlockedByFace = false;
    private ScreenOnCpuBoostHelper mScreenOnCpuBoostHelper;
    private boolean useProximityForceSuspend = false;

    public OppoDisplayPowerHelper(DisplayPowerController controller, Context context, Object lock, Handler handler, DisplayManagerInternal.DisplayPowerCallbacks callbacks) {
        Slog.d(TAG, "OppoDisplayPowerHelper ...");
        this.mDpc = controller;
        this.mHandler = new OppoDisplayPowerHelperHandler(handler.getLooper());
        this.mContext = context;
        this.mLock = lock;
        this.mCallbacks = callbacks;
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mBiometricsManager = (BiometricsManagerInternal) LocalServices.getService(BiometricsManagerInternal.class);
        this.mScreenOnCpuBoostHelper = new ScreenOnCpuBoostHelper();
        DEBUG_PANIC = DisplayPowerController.DEBUG_PANIC;
    }

    public void blockScreenOnByBiometrics(String reason) {
        if (DEBUG_PANIC) {
            Slog.d(TAG_BIOMETRICS, "blockScreenOnByBiometrics, mPendingScreenOnUnblockerFromBiometrics = " + this.mPendingScreenOnUnblockerFromBiometrics);
        }
        if (this.mPendingScreenOnUnblockerFromBiometrics == null) {
            Trace.asyncTraceBegin(131072, SCREEN_ON_BLOCKED_BY_BIOMETRICS_TRACE_NAME, 0);
            this.mPendingScreenOnUnblockerFromBiometrics = new ScreenOnUnblockerByBiometrics();
        }
        this.mPendingDisplayReadyBlocker = true;
        synchronized (this.mBlockReasonList) {
            Slog.e(TAG, "execuce mBlockReasonList.add");
            this.mBlockReasonList.add(reason);
        }
    }

    public void unblockScreenOnByBiometrics(String reason) {
        if (DEBUG_PANIC) {
            Slog.d(TAG_BIOMETRICS, "unblockScreen(reason = " + reason + ") , mPendingScreenOnUnblockerFromBiometrics = " + this.mPendingScreenOnUnblockerFromBiometrics);
        }
        if (this.mPendingScreenOnUnblockerFromBiometrics != null) {
            this.mPendingScreenOnUnblockerFromBiometrics = null;
            Trace.asyncTraceEnd(131072, SCREEN_ON_BLOCKED_BY_BIOMETRICS_TRACE_NAME, 0);
        }
        this.mPendingDisplayReadyBlocker = false;
        if (!"UNBLOCK_REASON_GO_TO_SLEEP".equals(reason)) {
            this.mDpc.notifySendUpdatePowerState();
        }
        if (reason.contains("blackGestureWake") || reason.contains("android.server.wm:SCREEN_ON_FLAG")) {
            Slog.d(TAG, "unblockScreen reason is blackGestureWake or android.server.wm:SCREEN_ON_FLAG");
            this.mDpc.setBlackGestureWakeUp(true);
            SystemProperties.set("sys.power.screenon.delay", "1");
        }
        synchronized (this.mBlockReasonList) {
            Slog.e(TAG, "execute mBlockReasonList.clear");
            this.mBlockReasonList.clear();
        }
    }

    public void unblockDisplayReady() {
        PowerManagerInternal powerManagerInternal = this.mPowerManagerInternal;
        if (powerManagerInternal != null) {
            if (powerManagerInternal.isStartGoToSleep()) {
                this.mPendingDisplayReadyBlocker = false;
            }
            if (DEBUG_PANIC) {
                Slog.d(TAG_BIOMETRICS, "unblockDisplayReady, mPendingDisplayReadyBlocker = " + this.mPendingDisplayReadyBlocker);
            }
        }
    }

    public void removeFaceBlockReasonFromBlockReasonList() {
        if (DEBUG_PANIC) {
            Slog.d(TAG_BIOMETRICS, "removeFaceBlockReasonFromBlockReasonList");
        }
        synchronized (this.mBlockReasonList) {
            Slog.i(TAG, "execuce listIterator.remove!");
            Iterator<String> listIterator = this.mBlockReasonList.iterator();
            while (listIterator.hasNext()) {
                String reason = listIterator.next();
                if (reason.equals("android.policy:POWER") || reason.equals("oppo.wakeup.gesture:DOUBLE_TAP_SCREEN") || reason.equals("oppo.wakeup.gesture:LIFT_HAND")) {
                    listIterator.remove();
                }
            }
        }
    }

    public boolean isBlockScreenOnByBiometrics() {
        return this.mPendingScreenOnUnblockerFromBiometrics != null;
    }

    public boolean isBlockDisplayByBiometrics() {
        return this.mPendingDisplayReadyBlocker;
    }

    public boolean hasBiometricsBlockedReason(String reason) {
        return this.mBlockReasonList.contains(reason);
    }

    private int getScreenStateInternal(DisplayPowerState powerState) {
        int state = powerState != null ? powerState.getScreenState() : 0;
        if (DEBUG_PANIC) {
            StringBuilder sb = new StringBuilder();
            sb.append("ScreenState = ");
            sb.append(state);
            sb.append(", fingerPrint block = ");
            sb.append(this.mPendingScreenOnUnblockerFromBiometrics != null);
            sb.append(", keyguard block = ");
            sb.append(this.mDpc.getPendingScreenOnUnblocker() != null);
            Slog.d(TAG_BIOMETRICS, sb.toString());
        }
        if (state == 2 && this.mPendingScreenOnUnblockerFromBiometrics == null && this.mDpc.getPendingScreenOnUnblocker() == null) {
            return 1;
        }
        return 0;
    }

    public int getScreenState() {
        return getScreenStateInternal(this.mPowerState);
    }

    public void setPowerState(DisplayPowerState powerState) {
        if (DisplayPowerController.DEBUG_PANIC) {
            Slog.d(TAG, "setPowerState:" + powerState.toString());
        }
        this.mPowerState = powerState;
    }

    public boolean isScreenOnBlockedByFace() {
        return this.mScreenOnBlockedByFace;
    }

    public void updateScreenOnBlockedState(boolean isBlockedScreenOn) {
        this.mScreenOnBlockedByFace = isBlockedScreenOn;
        if (DEBUG_PANIC) {
            Slog.d(TAG, "updateScreenOnBlockedState, isBlockedScreenOn = " + isBlockedScreenOn);
        }
        this.mDpc.sendMsgUnblockScreenOn(isBlockedScreenOn);
    }

    /* access modifiers changed from: private */
    public final class ScreenOnUnblockerByBiometrics implements WindowManagerPolicy.ScreenOnListener {
        private ScreenOnUnblockerByBiometrics() {
        }

        @Override // com.android.server.policy.WindowManagerPolicy.ScreenOnListener
        public void onScreenOn() {
            Message msg = OppoDisplayPowerHelper.this.mHandler.obtainMessage(1001, this);
            msg.setAsynchronous(true);
            OppoDisplayPowerHelper.this.mHandler.sendMessage(msg);
        }
    }

    public void onAnimationChanged(Animator animation, int eventCode) {
        BiometricsManagerInternal biometricsManagerInternal = this.mBiometricsManager;
        if (biometricsManagerInternal != null && eventCode == 2) {
            biometricsManagerInternal.onGoToSleepFinish();
        }
    }

    public void onUpdatePowerState(int state, int policy, int brightness) {
        boolean isOff = true;
        if (!(state == 1 || state == 3 || state == 4)) {
            isOff = false;
        }
        BiometricsManagerInternal biometricsManagerInternal = this.mBiometricsManager;
        if (biometricsManagerInternal != null && !isOff && policy != 0) {
            biometricsManagerInternal.onAnimateScreenBrightness(brightness);
        }
    }

    public void setUseProximityForceSuspend(boolean enable) {
        this.useProximityForceSuspend = enable;
    }

    public boolean getUseProximityForceSuspendState() {
        return this.useProximityForceSuspend;
    }

    public void onProximitySensorEnabled(boolean enable, boolean proximitySensorEnabled) {
        if (!enable && proximitySensorEnabled) {
            this.useProximityForceSuspend = false;
        }
    }

    public void onProximityDebounceTimeArrived() {
        Slog.d(TAG, "onProximityDebounceTimeArrived");
        this.mProximityEventHandled = false;
        this.mScreenOnCpuBoostHelper.acquireCpuBoost(500);
    }

    public void onProximityDebounceTimeWaitting() {
        Slog.d(TAG, "onProximityDebounceTimeWaitting");
    }

    public void sendOnProximityPositiveSuspendWithWakelock() {
        this.mCallbacks.acquireSuspendBlocker();
        this.mHandler.post(this.mOnProximityPositiveSuspendRunnable);
    }

    public void sendOnProximityNegativeSuspendWithWakelock() {
        this.mCallbacks.acquireSuspendBlocker();
        this.mHandler.post(this.mOnProximityNegativeSuspendRunnable);
    }

    public void applyOppoProximitySensorLocked(DisplayManagerInternal.DisplayPowerRequest powerRequest, int proximity, boolean proximitySensorEnabled, boolean waitingForNegativeProximity, boolean screenOffBecauseOfProximity, int state) {
        this.mOppoWaitingForNegativeProximity = waitingForNegativeProximity;
        this.mOppoScreenOffBecauseOfProximity = screenOffBecauseOfProximity;
        if (!this.useProximityForceSuspend) {
            if (powerRequest.useProximitySensor && state != 1 && state != 3 && state != 4) {
                this.mDpc.setOppoProximitySensorEnabled(true);
                this.mProximitySensorEnabled = true;
                if (!this.mOppoScreenOffBecauseOfProximity && proximity == 1) {
                    this.mOppoScreenOffBecauseOfProximity = true;
                    this.mDpc.setScreenOffBecauseOfProximityState(this.mOppoScreenOffBecauseOfProximity);
                    this.mDpc.sendOnProximityStateChangedWithWakelock(true);
                }
            } else if (!this.mOppoWaitingForNegativeProximity || !this.mOppoScreenOffBecauseOfProximity || proximity != 1 || state == 1 || state == 3 || state == 4) {
                this.mDpc.setOppoProximitySensorEnabled(false);
                this.mProximitySensorEnabled = false;
                this.mOppoWaitingForNegativeProximity = false;
                this.mDpc.setWaitingForNegativeProximityState(this.mOppoWaitingForNegativeProximity);
            } else {
                this.mDpc.setOppoProximitySensorEnabled(true);
                this.mProximitySensorEnabled = true;
            }
            if ((this.mOppoScreenOffBecauseOfProximity && proximity != 1) || (this.mOppoScreenOffBecauseOfProximity && !this.mProximitySensorEnabled)) {
                this.mOppoScreenOffBecauseOfProximity = false;
                this.mDpc.setScreenOffBecauseOfProximityState(this.mOppoScreenOffBecauseOfProximity);
                this.mDpc.sendOnProximityStateChangedWithWakelock(false);
            }
        } else if (powerRequest.useProximitySensor) {
            this.mDpc.setOppoProximitySensorEnabled(true);
            this.mProximitySensorEnabled = true;
            if (!this.mProximityEventHandled) {
                Slog.i(TAG, "mProximity = " + proximityToString(proximity));
                if (proximity == 1) {
                    sendOnProximityPositiveSuspendWithWakelock();
                } else if (proximity == 0) {
                    sendOnProximityNegativeSuspendWithWakelock();
                }
                this.mProximityEventHandled = true;
            } else if (DisplayPowerController.DEBUG) {
                Slog.i(TAG, "the last proximity event has been handled");
            }
        } else if (proximitySensorEnabled) {
            Slog.i(TAG, "mPowerRequest.useProximitySensor = " + powerRequest.useProximitySensor + ", mWaitingForNegativeProximity = " + this.mOppoWaitingForNegativeProximity + ", state = " + state);
            if (this.mOppoWaitingForNegativeProximity && proximity == 1 && (state == 1 || state == 3 || state == 4 || getScreenState() != 1)) {
                this.mDpc.setOppoProximitySensorEnabled(true);
                this.mProximitySensorEnabled = true;
                return;
            }
            Slog.i(TAG, "mPowerRequest.useProximitySensor = " + powerRequest.useProximitySensor + ", mWaitingForNegativeProximity = " + this.mOppoWaitingForNegativeProximity + ", state = " + state);
            this.mDpc.setOppoProximitySensorEnabled(false);
            this.mProximitySensorEnabled = false;
            if (state == 1 || state == 3 || state == 4 || getScreenState() != 1) {
                Slog.i(TAG, "turn on lcd light due to proximity released");
                sendOnProximityNegativeSuspendWithWakelock();
            }
            this.mOppoScreenOffBecauseOfProximity = false;
            this.mOppoWaitingForNegativeProximity = false;
            this.mDpc.setScreenOffBecauseOfProximityState(this.mOppoScreenOffBecauseOfProximity);
            this.mDpc.setWaitingForNegativeProximityState(this.mOppoWaitingForNegativeProximity);
            this.mProximityEventHandled = true;
        }
    }

    /* access modifiers changed from: private */
    public class OppoDisplayPowerHelperHandler extends Handler {
        public OppoDisplayPowerHelperHandler(Looper looper) {
            super(looper);
            Slog.d(OppoDisplayPowerHelper.TAG, "OppoDisplayPowerHelperHandler init");
        }

        public void handleMessage(Message msg) {
            Slog.d(OppoDisplayPowerHelper.TAG, "handleMessage:" + msg.what);
            if (msg.what == 1001 && OppoDisplayPowerHelper.this.mPendingScreenOnUnblockerFromBiometrics == msg.obj) {
                OppoDisplayPowerHelper.this.unblockScreenOnByBiometrics("MSG_SCREEN_ON_UNBLOCKED_BY_BIOMETRICS");
                OppoDisplayPowerHelper.this.mDpc.callUpdatePowerState();
            }
        }
    }

    private static String proximityToString(int state) {
        if (state == -1) {
            return "Unknown";
        }
        if (state == 0) {
            return "Negative";
        }
        if (state != 1) {
            return Integer.toString(state);
        }
        return "Positive";
    }

    public void dump(PrintWriter pw) {
        pw.println();
        pw.println("OppoDisplayPowerHelper:");
        pw.println("  mPendingScreenOnUnblockerFromBiometrics=" + this.mPendingScreenOnUnblockerFromBiometrics);
        pw.println("  mScreenOnBlockedByFace=" + this.mScreenOnBlockedByFace);
        pw.println("  mPendingDisplayReadyBlocker=" + this.mPendingDisplayReadyBlocker);
    }
}
