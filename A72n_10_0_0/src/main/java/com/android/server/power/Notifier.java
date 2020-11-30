package com.android.server.power;

import android.app.ActivityManagerInternal;
import android.app.AppOpsManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManagerInternal;
import android.media.AudioAttributes;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.WorkSource;
import android.provider.Settings;
import android.util.EventLog;
import android.util.Slog;
import android.util.StatsLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.coloros.OppoSysStateManagerInternal;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.inputmethod.InputMethodManagerInternal;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.statusbar.StatusBarManagerInternal;

@VisibleForTesting
public class Notifier {
    static boolean DEBUG = false;
    static boolean DEBUG_PANIC = false;
    private static final int INTERACTIVE_STATE_ASLEEP = 2;
    private static final int INTERACTIVE_STATE_AWAKE = 1;
    private static final int INTERACTIVE_STATE_UNKNOWN = 0;
    private static final int MSG_BROADCAST = 2;
    private static final int MSG_PROFILE_TIMED_OUT = 5;
    private static final int MSG_SCREEN_BRIGHTNESS_BOOST_CHANGED = 4;
    private static final int MSG_USER_ACTIVITY = 1;
    private static final int MSG_WIRED_CHARGING_STARTED = 6;
    private static final int MSG_WIRELESS_CHARGING_STARTED = 3;
    private static final String TAG = "PowerManagerNotifier";
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).build();
    private static final VibrationEffect WIRELESS_CHARGING_VIBRATION_EFFECT = VibrationEffect.createWaveform(WIRELESS_VIBRATION_TIME, WIRELESS_VIBRATION_AMPLITUDE, -1);
    private static final int[] WIRELESS_VIBRATION_AMPLITUDE = {1, 4, 11, 25, 44, 67, 91, HdmiCecKeycode.CEC_KEYCODE_F2_RED, 123, 103, 79, 55, 34, 17, 7, 2};
    private static final long[] WIRELESS_VIBRATION_TIME = {40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40};
    private final ActivityManagerInternal mActivityManagerInternal;
    private final AppOpsManager mAppOps;
    private final IBatteryStats mBatteryStats;
    private boolean mBroadcastInProgress;
    private long mBroadcastStartTime;
    private int mBroadcastedInteractiveState;
    private final Context mContext;
    private final BroadcastReceiver mGoToSleepBroadcastDone = new BroadcastReceiver() {
        /* class com.android.server.power.Notifier.AnonymousClass8 */

        public void onReceive(Context context, Intent intent) {
            if (Notifier.DEBUG_PANIC || Notifier.DEBUG) {
                Slog.d(Notifier.TAG, "mGoToSleepBroadcastDone - sendNextBroadcast");
            }
            EventLog.writeEvent((int) EventLogTags.POWER_SCREEN_BROADCAST_DONE, 0, Long.valueOf(SystemClock.uptimeMillis() - Notifier.this.mBroadcastStartTime), 1);
            Notifier.this.sendNextBroadcast();
        }
    };
    private final NotifierHandler mHandler;
    private final InputManagerInternal mInputManagerInternal;
    private final InputMethodManagerInternal mInputMethodManagerInternal;
    private boolean mInteractive = true;
    private int mInteractiveChangeReason;
    private long mInteractiveChangeStartTime;
    private boolean mInteractiveChanging;
    private final Object mLock = new Object();
    private OppoSysStateManagerInternal mOppoSysStateManagerInternal;
    private boolean mPendingGoToSleepBroadcast;
    private int mPendingInteractiveState;
    private boolean mPendingWakeUpBroadcast;
    private final WindowManagerPolicy mPolicy;
    private final BroadcastReceiver mScreeBrightnessBoostChangedDone = new BroadcastReceiver() {
        /* class com.android.server.power.Notifier.AnonymousClass6 */

        public void onReceive(Context context, Intent intent) {
            Notifier.this.mSuspendBlocker.release();
        }
    };
    private final Intent mScreenBrightnessBoostIntent;
    private final Intent mScreenOffIntent;
    private final Intent mScreenOnIntent;
    private boolean mSkipGoToSleepBroadcast = false;
    private boolean mSkipWakeUpBroadcast = false;
    private final StatusBarManagerInternal mStatusBarManagerInternal;
    private final SuspendBlocker mSuspendBlocker;
    private final boolean mSuspendWhenScreenOffDueToProximityConfig;
    private final TrustManager mTrustManager;
    private boolean mUserActivityPending;
    private final Vibrator mVibrator;
    private final BroadcastReceiver mWakeUpBroadcastDone = new BroadcastReceiver() {
        /* class com.android.server.power.Notifier.AnonymousClass7 */

        public void onReceive(Context context, Intent intent) {
            if (Notifier.DEBUG_PANIC) {
                Slog.d(Notifier.TAG, "mWakeUpBroadcastDone - sendNextBroadcast");
            }
            EventLog.writeEvent((int) EventLogTags.POWER_SCREEN_BROADCAST_DONE, 1, Long.valueOf(SystemClock.uptimeMillis() - Notifier.this.mBroadcastStartTime), 1);
            Notifier.this.sendNextBroadcast();
        }
    };

    public Notifier(Looper looper, Context context, IBatteryStats batteryStats, SuspendBlocker suspendBlocker, WindowManagerPolicy policy) {
        this.mContext = context;
        this.mBatteryStats = batteryStats;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mSuspendBlocker = suspendBlocker;
        this.mPolicy = policy;
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
        this.mInputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
        this.mStatusBarManagerInternal = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
        this.mTrustManager = (TrustManager) this.mContext.getSystemService(TrustManager.class);
        this.mVibrator = (Vibrator) this.mContext.getSystemService(Vibrator.class);
        this.mHandler = new NotifierHandler(looper);
        this.mScreenOnIntent = new Intent("android.intent.action.SCREEN_ON");
        this.mScreenOnIntent.addFlags(1344274432);
        this.mScreenOnIntent.addFlags(134217728);
        this.mScreenOffIntent = new Intent("android.intent.action.SCREEN_OFF");
        this.mScreenOffIntent.addFlags(1344274432);
        this.mScreenOffIntent.addFlags(134217728);
        this.mScreenBrightnessBoostIntent = new Intent("android.os.action.SCREEN_BRIGHTNESS_BOOST_CHANGED");
        this.mScreenBrightnessBoostIntent.addFlags(1342177280);
        this.mSuspendWhenScreenOffDueToProximityConfig = context.getResources().getBoolean(17891544);
        try {
            this.mBatteryStats.noteInteractive(true);
        } catch (RemoteException e) {
        }
        StatsLog.write(33, 1);
        OppoSysStateManager.getInstance();
        this.mOppoSysStateManagerInternal = (OppoSysStateManagerInternal) LocalServices.getService(OppoSysStateManagerInternal.class);
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    }

    public void onWakeLockAcquired(int flags, String tag, String packageName, int ownerUid, int ownerPid, WorkSource workSource, String historyTag) {
        if (DEBUG) {
            Slog.d(TAG, "onWakeLockAcquired: flags=" + flags + ", tag=\"" + tag + "\", packageName=" + packageName + ", ownerUid=" + ownerUid + ", ownerPid=" + ownerPid + ", workSource=" + workSource);
        }
        int monitorType = getBatteryStatsWakeLockMonitorType(flags);
        if (monitorType >= 0) {
            boolean unimportantForLogging = ownerUid == 1000 && (1073741824 & flags) != 0;
            if (workSource != null) {
                try {
                    this.mBatteryStats.noteStartWakelockFromSource(workSource, ownerPid, tag, historyTag, monitorType, unimportantForLogging);
                } catch (RemoteException e) {
                }
            } else {
                this.mBatteryStats.noteStartWakelock(ownerUid, ownerPid, tag, historyTag, monitorType, unimportantForLogging);
                this.mAppOps.startOpNoThrow(40, ownerUid, packageName);
            }
        }
    }

    public void onLongPartialWakeLockStart(String tag, int ownerUid, WorkSource workSource, String historyTag) {
        if (DEBUG) {
            Slog.d(TAG, "onLongPartialWakeLockStart: ownerUid=" + ownerUid + ", workSource=" + workSource);
        }
        if (workSource != null) {
            try {
                this.mBatteryStats.noteLongPartialWakelockStartFromSource(tag, historyTag, workSource);
                StatsLog.write(11, workSource, tag, historyTag, 1);
            } catch (RemoteException e) {
            }
        } else {
            this.mBatteryStats.noteLongPartialWakelockStart(tag, historyTag, ownerUid);
            StatsLog.write_non_chained(11, ownerUid, null, tag, historyTag, 1);
        }
    }

    public void onLongPartialWakeLockFinish(String tag, int ownerUid, WorkSource workSource, String historyTag) {
        if (DEBUG) {
            Slog.d(TAG, "onLongPartialWakeLockFinish: ownerUid=" + ownerUid + ", workSource=" + workSource);
        }
        if (workSource != null) {
            try {
                this.mBatteryStats.noteLongPartialWakelockFinishFromSource(tag, historyTag, workSource);
                StatsLog.write(11, workSource, tag, historyTag, 0);
            } catch (RemoteException e) {
            }
        } else {
            this.mBatteryStats.noteLongPartialWakelockFinish(tag, historyTag, ownerUid);
            StatsLog.write_non_chained(11, ownerUid, null, tag, historyTag, 0);
        }
    }

    public void onWakeLockChanging(int flags, String tag, String packageName, int ownerUid, int ownerPid, WorkSource workSource, String historyTag, int newFlags, String newTag, String newPackageName, int newOwnerUid, int newOwnerPid, WorkSource newWorkSource, String newHistoryTag) {
        int monitorType = getBatteryStatsWakeLockMonitorType(flags);
        int newMonitorType = getBatteryStatsWakeLockMonitorType(newFlags);
        if (workSource == null || newWorkSource == null || monitorType < 0 || newMonitorType < 0) {
            onWakeLockReleased(flags, tag, packageName, ownerUid, ownerPid, workSource, historyTag);
            onWakeLockAcquired(newFlags, newTag, newPackageName, newOwnerUid, newOwnerPid, newWorkSource, newHistoryTag);
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, "onWakeLockChanging: flags=" + newFlags + ", tag=\"" + newTag + "\", packageName=" + newPackageName + ", ownerUid=" + newOwnerUid + ", ownerPid=" + newOwnerPid + ", workSource=" + newWorkSource);
        }
        try {
            this.mBatteryStats.noteChangeWakelockFromSource(workSource, ownerPid, tag, historyTag, monitorType, newWorkSource, newOwnerPid, newTag, newHistoryTag, newMonitorType, newOwnerUid == 1000 && (1073741824 & newFlags) != 0);
        } catch (RemoteException e) {
        }
    }

    public void onWakeLockReleased(int flags, String tag, String packageName, int ownerUid, int ownerPid, WorkSource workSource, String historyTag) {
        if (DEBUG) {
            Slog.d(TAG, "onWakeLockReleased: flags=" + flags + ", tag=\"" + tag + "\", packageName=" + packageName + ", ownerUid=" + ownerUid + ", ownerPid=" + ownerPid + ", workSource=" + workSource);
        }
        int monitorType = getBatteryStatsWakeLockMonitorType(flags);
        if (monitorType < 0) {
            return;
        }
        if (workSource != null) {
            try {
                this.mBatteryStats.noteStopWakelockFromSource(workSource, ownerPid, tag, historyTag, monitorType);
            } catch (RemoteException e) {
            }
        } else {
            this.mBatteryStats.noteStopWakelock(ownerUid, ownerPid, tag, historyTag, monitorType);
            this.mAppOps.finishOp(40, ownerUid, packageName);
        }
    }

    private int getBatteryStatsWakeLockMonitorType(int flags) {
        int i = 65535 & flags;
        if (i == 1) {
            return 0;
        }
        if (i == 6 || i == 10) {
            return 1;
        }
        if (i == 32) {
            return this.mSuspendWhenScreenOffDueToProximityConfig ? -1 : 0;
        }
        if (i == 64 || i != 128) {
            return -1;
        }
        return 18;
    }

    public void onWakefulnessChangeStarted(final int wakefulness, int reason, long eventTime) {
        int i;
        boolean interactive = PowerManagerInternal.isInteractive(wakefulness);
        if (interactive && 98 == reason) {
            this.mSkipWakeUpBroadcast = true;
        }
        if (interactive && 103 == reason) {
            this.mSkipWakeUpBroadcast = true;
        }
        if (!interactive && this.mSkipWakeUpBroadcast) {
            this.mSkipGoToSleepBroadcast = true;
        }
        if (DEBUG_PANIC || DEBUG) {
            Slog.d(TAG, "onWakefulnessChangeStarted: wakefulness=" + wakefulness + ", reason=" + PowerManager.sleepReasonToString(reason) + ", interactive=" + interactive + ", mSkipWakeUpBroadcast = " + this.mSkipWakeUpBroadcast + ", mSkipGoToSleepBroadcast = " + this.mSkipGoToSleepBroadcast);
        }
        this.mHandler.post(new Runnable() {
            /* class com.android.server.power.Notifier.AnonymousClass1 */

            public void run() {
                Notifier.this.mActivityManagerInternal.onWakefulnessChanged(wakefulness);
                if (Notifier.this.mOppoSysStateManagerInternal != null) {
                    Notifier.this.mOppoSysStateManagerInternal.onWakefulnessChanged(wakefulness);
                }
            }
        });
        if (this.mInteractive != interactive) {
            if (this.mInteractiveChanging) {
                handleLateInteractiveChange();
            }
            if (!this.mSkipWakeUpBroadcast && !this.mSkipGoToSleepBroadcast) {
                this.mInputManagerInternal.setInteractive(interactive);
                this.mInputMethodManagerInternal.setInteractive(interactive);
            }
            try {
                this.mBatteryStats.noteInteractive(interactive);
            } catch (RemoteException e) {
            }
            if (interactive) {
                i = 1;
            } else {
                i = 0;
            }
            StatsLog.write(33, i);
            this.mInteractive = interactive;
            this.mInteractiveChangeReason = reason;
            this.mInteractiveChangeStartTime = eventTime;
            this.mInteractiveChanging = true;
            handleEarlyInteractiveChange();
        }
    }

    public void onWakefulnessChangeFinished() {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "onWakefulnessChangeFinished, mSkipWakeUpBroadcast=" + this.mSkipWakeUpBroadcast + ", mSkipGoToSleepBroadcast=" + this.mSkipGoToSleepBroadcast);
        }
        if (this.mSkipWakeUpBroadcast || this.mSkipGoToSleepBroadcast) {
            this.mInputManagerInternal.setInteractive(this.mInteractive);
            this.mInputMethodManagerInternal.setInteractive(this.mInteractive);
        }
        if (this.mInteractiveChanging) {
            this.mInteractiveChanging = false;
            handleLateInteractiveChange(true);
        }
    }

    private void handleEarlyInteractiveChange() {
        synchronized (this.mLock) {
            if (this.mInteractive) {
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.power.Notifier.AnonymousClass2 */

                    public void run() {
                        Notifier.this.mPolicy.startedWakingUp(Notifier.translateOnReason(Notifier.this.mInteractiveChangeReason));
                    }
                });
                this.mPendingInteractiveState = 1;
                if (!this.mSkipWakeUpBroadcast) {
                    this.mPendingWakeUpBroadcast = true;
                    updatePendingBroadcastLocked();
                }
            } else {
                final int why = translateOffReason(this.mInteractiveChangeReason);
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.power.Notifier.AnonymousClass3 */

                    public void run() {
                        Notifier.this.mPolicy.startedGoingToSleep(why);
                    }
                });
            }
        }
    }

    private void handleLateInteractiveChange() {
        handleLateInteractiveChange(false);
    }

    private void handleLateInteractiveChange(boolean fromChangeFinished) {
        synchronized (this.mLock) {
            final int interactiveChangeLatency = (int) (SystemClock.uptimeMillis() - this.mInteractiveChangeStartTime);
            if (this.mInteractive) {
                if (this.mSkipWakeUpBroadcast && fromChangeFinished) {
                    this.mPendingWakeUpBroadcast = true;
                    updatePendingBroadcastLocked();
                }
                final int why = translateOnReason(this.mInteractiveChangeReason);
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.power.Notifier.AnonymousClass4 */

                    public void run() {
                        LogMaker log = new LogMaker(198);
                        log.setType(1);
                        log.setSubtype(why);
                        log.setLatency((long) interactiveChangeLatency);
                        log.addTaggedData(1694, Integer.valueOf(Notifier.this.mInteractiveChangeReason));
                        MetricsLogger.action(log);
                        EventLogTags.writePowerScreenState(1, 0, 0, 0, interactiveChangeLatency);
                        Notifier.this.mPolicy.finishedWakingUp(why);
                    }
                });
            } else {
                if (this.mUserActivityPending) {
                    this.mUserActivityPending = false;
                    this.mHandler.removeMessages(1);
                }
                final int why2 = translateOffReason(this.mInteractiveChangeReason);
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.power.Notifier.AnonymousClass5 */

                    public void run() {
                        LogMaker log = new LogMaker(198);
                        log.setType(2);
                        log.setSubtype(why2);
                        log.setLatency((long) interactiveChangeLatency);
                        log.addTaggedData(1695, Integer.valueOf(Notifier.this.mInteractiveChangeReason));
                        MetricsLogger.action(log);
                        EventLogTags.writePowerScreenState(0, why2, 0, 0, interactiveChangeLatency);
                        Notifier.this.mPolicy.finishedGoingToSleep(why2);
                    }
                });
                this.mPendingInteractiveState = 2;
                if (!this.mSkipGoToSleepBroadcast) {
                    this.mPendingGoToSleepBroadcast = true;
                    updatePendingBroadcastLocked();
                }
            }
            if (fromChangeFinished) {
                this.mSkipWakeUpBroadcast = false;
                this.mSkipGoToSleepBroadcast = false;
            }
        }
    }

    private static int translateOffReason(int reason) {
        if (reason == 1) {
            return 1;
        }
        if (reason == 2) {
            return 3;
        }
        if (reason != 9) {
            return 2;
        }
        return 4;
    }

    /* access modifiers changed from: private */
    public static int translateOnReason(int reason) {
        switch (reason) {
            case 1:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 9:
                return 1;
            case 2:
                return 2;
            case 8:
            default:
                return 3;
        }
    }

    public void onScreenBrightnessBoostChanged() {
        if (DEBUG) {
            Slog.d(TAG, "onScreenBrightnessBoostChanged");
        }
        this.mSuspendBlocker.acquire();
        Message msg = this.mHandler.obtainMessage(4);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    public void onUserActivity(int event, int uid) {
        if (DEBUG) {
            Slog.d(TAG, "onUserActivity: event=" + event + ", uid=" + uid);
        }
        try {
            this.mBatteryStats.noteUserActivity(uid, event);
        } catch (RemoteException e) {
        }
        synchronized (this.mLock) {
            if (!this.mUserActivityPending) {
                this.mUserActivityPending = true;
                Message msg = this.mHandler.obtainMessage(1);
                msg.setAsynchronous(true);
                this.mHandler.sendMessage(msg);
            }
        }
    }

    public void onWakeUp(int reason, String details, int reasonUid, String opPackageName, int opUid) {
        if (DEBUG) {
            Slog.d(TAG, "onWakeUp: reason=" + PowerManager.wakeReasonToString(reason) + ", details=" + details + ", reasonUid=" + reasonUid + " opPackageName=" + opPackageName + " opUid=" + opUid);
        }
        try {
            this.mBatteryStats.noteWakeUp(details, reasonUid);
            if (opPackageName != null) {
                this.mAppOps.noteOpNoThrow(61, opUid, opPackageName);
            }
        } catch (RemoteException e) {
        }
    }

    public void onProfileTimeout(int userId) {
        Message msg = this.mHandler.obtainMessage(5);
        msg.setAsynchronous(true);
        msg.arg1 = userId;
        this.mHandler.sendMessage(msg);
    }

    public void onWirelessChargingStarted(int batteryLevel, int userId) {
        if (DEBUG) {
            Slog.d(TAG, "onWirelessChargingStarted");
        }
        this.mSuspendBlocker.acquire();
        Message msg = this.mHandler.obtainMessage(3);
        msg.setAsynchronous(true);
        msg.arg1 = batteryLevel;
        msg.arg2 = userId;
        this.mHandler.sendMessage(msg);
    }

    public void onWiredChargingStarted(int userId) {
        if (DEBUG) {
            Slog.d(TAG, "onWiredChargingStarted");
        }
        this.mSuspendBlocker.acquire();
        Message msg = this.mHandler.obtainMessage(6);
        msg.setAsynchronous(true);
        msg.arg1 = userId;
        this.mHandler.sendMessage(msg);
    }

    private void updatePendingBroadcastLocked() {
        int i;
        if (DEBUG) {
            Slog.d(TAG, "updatePendingBroadcastLocked mBroadcastInProgress = " + this.mBroadcastInProgress + ", mPendingInteractiveState = " + this.mPendingInteractiveState + ", mPendingWakeUpBroadcast = " + this.mPendingWakeUpBroadcast + ", mPendingGoToSleepBroadcast = " + this.mPendingGoToSleepBroadcast + ", mBroadcastedInteractiveState = " + this.mBroadcastedInteractiveState);
        }
        if (!this.mBroadcastInProgress && (i = this.mPendingInteractiveState) != 0) {
            if (this.mPendingWakeUpBroadcast || this.mPendingGoToSleepBroadcast || i != this.mBroadcastedInteractiveState) {
                this.mBroadcastInProgress = true;
                this.mSuspendBlocker.acquire();
                Message msg = this.mHandler.obtainMessage(2);
                msg.setAsynchronous(true);
                this.mHandler.sendMessage(msg);
            }
        }
    }

    private void finishPendingBroadcastLocked() {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "finishPendingBroadcastLocked");
        }
        this.mBroadcastInProgress = false;
        this.mSuspendBlocker.release();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendUserActivity() {
        synchronized (this.mLock) {
            if (this.mUserActivityPending) {
                this.mUserActivityPending = false;
                this.mPolicy.userActivity();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0082, code lost:
        android.util.EventLog.writeEvent((int) com.android.server.EventLogTags.POWER_SCREEN_BROADCAST_SEND, 1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0087, code lost:
        if (r1 != 1) goto L_0x008d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0089, code lost:
        sendWakeUpBroadcast();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x008d, code lost:
        sendGoToSleepBroadcast();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        return;
     */
    private void sendNextBroadcast() {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "sendNextBroadcast, mBroadcastedInteractiveState=" + this.mBroadcastedInteractiveState + ", mPendingInteractiveState=" + this.mPendingInteractiveState);
        }
        synchronized (this.mLock) {
            if (this.mBroadcastedInteractiveState == 0) {
                if (this.mPendingInteractiveState != 2) {
                    this.mPendingWakeUpBroadcast = false;
                    this.mBroadcastedInteractiveState = 1;
                } else {
                    this.mPendingGoToSleepBroadcast = false;
                    this.mBroadcastedInteractiveState = 2;
                }
            } else if (this.mBroadcastedInteractiveState == 1) {
                if (!this.mPendingWakeUpBroadcast && !this.mPendingGoToSleepBroadcast) {
                    if (this.mPendingInteractiveState != 2) {
                        finishPendingBroadcastLocked();
                        return;
                    }
                }
                this.mPendingGoToSleepBroadcast = false;
                this.mBroadcastedInteractiveState = 2;
            } else {
                if (!this.mPendingWakeUpBroadcast && !this.mPendingGoToSleepBroadcast) {
                    if (this.mPendingInteractiveState != 1) {
                        finishPendingBroadcastLocked();
                        return;
                    }
                }
                this.mPendingWakeUpBroadcast = false;
                this.mBroadcastedInteractiveState = 1;
                if (!this.mSkipWakeUpBroadcast) {
                    if (this.mSkipGoToSleepBroadcast) {
                    }
                }
                finishPendingBroadcastLocked();
                return;
            }
            this.mBroadcastStartTime = SystemClock.uptimeMillis();
            int powerState = this.mBroadcastedInteractiveState;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBrightnessBoostChangedBroadcast() {
        if (DEBUG) {
            Slog.d(TAG, "Sending brightness boost changed broadcast.");
        }
        this.mContext.sendOrderedBroadcastAsUser(this.mScreenBrightnessBoostIntent, UserHandle.ALL, null, this.mScreeBrightnessBoostChangedDone, this.mHandler, 0, null, null);
    }

    private void sendWakeUpBroadcast() {
        if (DEBUG_PANIC || DEBUG) {
            Slog.d(TAG, "Sending wake up broadcast.");
        }
        if (this.mActivityManagerInternal.isSystemReady()) {
            if (DEBUG_PANIC) {
                Slog.d(TAG, "sendWakeUpBroadcast - sendOrderedBroadcastAsUser");
            }
            this.mContext.sendOrderedBroadcastAsUser(this.mScreenOnIntent, UserHandle.ALL, null, this.mWakeUpBroadcastDone, this.mHandler, 0, null, null);
            return;
        }
        EventLog.writeEvent((int) EventLogTags.POWER_SCREEN_BROADCAST_STOP, 2, 1);
        sendNextBroadcast();
    }

    private void sendGoToSleepBroadcast() {
        if (DEBUG_PANIC || DEBUG) {
            Slog.d(TAG, "Sending go to sleep broadcast.");
        }
        if (this.mActivityManagerInternal.isSystemReady()) {
            if (DEBUG_PANIC || DEBUG) {
                Slog.d(TAG, "sendGoToSleepBroadcast - sendOrderedBroadcastAsUser");
            }
            this.mContext.sendOrderedBroadcastAsUser(this.mScreenOffIntent, UserHandle.ALL, null, this.mGoToSleepBroadcastDone, this.mHandler, 0, null, null);
            return;
        }
        EventLog.writeEvent((int) EventLogTags.POWER_SCREEN_BROADCAST_STOP, 3, 1);
        sendNextBroadcast();
    }

    private void playChargingStartedFeedback(int userId) {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showWirelessChargingStarted(int batteryLevel, int userId) {
        playChargingStartedFeedback(userId);
        StatusBarManagerInternal statusBarManagerInternal = this.mStatusBarManagerInternal;
        if (statusBarManagerInternal != null) {
            statusBarManagerInternal.showChargingAnimation(batteryLevel);
        }
        this.mSuspendBlocker.release();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showWiredChargingStarted(int userId) {
        playChargingStartedFeedback(userId);
        this.mSuspendBlocker.release();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void lockProfile(int userId) {
        this.mTrustManager.setDeviceLockedForUser(userId, true);
    }

    private void playChargingStartedVibration(int userId) {
        boolean vibrateEnabled = true;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "charging_vibration_enabled", 1, userId) == 0) {
            vibrateEnabled = false;
        }
        if (vibrateEnabled && isChargingFeedbackEnabled(userId)) {
            this.mVibrator.vibrate(WIRELESS_CHARGING_VIBRATION_EFFECT, VIBRATION_ATTRIBUTES);
        }
    }

    private boolean isChargingFeedbackEnabled(int userId) {
        return (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "charging_sounds_enabled", 1, userId) != 0) && (Settings.Global.getInt(this.mContext.getContentResolver(), "zen_mode", 1) == 0);
    }

    /* access modifiers changed from: private */
    public final class NotifierHandler extends Handler {
        public NotifierHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Notifier.this.sendUserActivity();
                    return;
                case 2:
                    Notifier.this.sendNextBroadcast();
                    return;
                case 3:
                    Notifier.this.showWirelessChargingStarted(msg.arg1, msg.arg2);
                    return;
                case 4:
                    Notifier.this.sendBrightnessBoostChangedBroadcast();
                    return;
                case 5:
                    Notifier.this.lockProfile(msg.arg1);
                    return;
                case 6:
                    Notifier.this.showWiredChargingStarted(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }
}
