package com.android.server.power;

import android.app.ActivityManagerInternal;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManagerInternal;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.metrics.LogMaker;
import android.net.Uri;
import android.net.util.NetworkConstants;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManagerInternal;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.util.EventLog;
import android.util.Slog;
import android.view.WindowManagerPolicy;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.coloros.OppoSysStateManager;
import com.android.server.coloros.OppoSysStateManagerInternal;

final class Notifier {
    static boolean DEBUG = false;
    static boolean DEBUG_PANIC = false;
    private static final int INTERACTIVE_STATE_ASLEEP = 2;
    private static final int INTERACTIVE_STATE_AWAKE = 1;
    private static final int INTERACTIVE_STATE_UNKNOWN = 0;
    private static final int MSG_BROADCAST = 2;
    private static final int MSG_SCREEN_BRIGHTNESS_BOOST_CHANGED = 4;
    private static final int MSG_USER_ACTIVITY = 1;
    private static final int MSG_WIRELESS_CHARGING_STARTED = 3;
    private static final String TAG = "PowerManagerNotifier";
    private final ActivityManagerInternal mActivityManagerInternal;
    private final IAppOpsService mAppOps;
    private final IBatteryStats mBatteryStats;
    private boolean mBroadcastInProgress;
    private long mBroadcastStartTime;
    private int mBroadcastedInteractiveState;
    private final Context mContext;
    private final BroadcastReceiver mGoToSleepBroadcastDone = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Notifier.DEBUG_PANIC || Notifier.DEBUG) {
                Slog.d(Notifier.TAG, "mGoToSleepBroadcastDone - sendNextBroadcast");
            }
            EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_DONE, new Object[]{Integer.valueOf(0), Long.valueOf(SystemClock.uptimeMillis() - Notifier.this.mBroadcastStartTime), Integer.valueOf(1)});
            Notifier.this.sendNextBroadcast();
        }
    };
    private final NotifierHandler mHandler;
    private final InputManagerInternal mInputManagerInternal;
    private final InputMethodManagerInternal mInputMethodManagerInternal;
    private boolean mInteractive = true;
    private int mInteractiveChangeReason;
    private boolean mInteractiveChanging;
    private final Object mLock = new Object();
    private OppoSysStateManagerInternal mOppoSysStateManagerInternal;
    private boolean mPendingGoToSleepBroadcast;
    private int mPendingInteractiveState;
    private boolean mPendingWakeUpBroadcast;
    private final WindowManagerPolicy mPolicy;
    private final BroadcastReceiver mScreeBrightnessBoostChangedDone = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Notifier.this.mSuspendBlocker.release();
        }
    };
    private final Intent mScreenBrightnessBoostIntent;
    private final Intent mScreenOffIntent;
    private final Intent mScreenOnIntent;
    private boolean mSkipGoToSleepBroadcast = false;
    private boolean mSkipWakeUpBroadcast = false;
    private final SuspendBlocker mSuspendBlocker;
    private final boolean mSuspendWhenScreenOffDueToProximityConfig;
    private boolean mUserActivityPending;
    private final BroadcastReceiver mWakeUpBroadcastDone = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Notifier.DEBUG_PANIC) {
                Slog.d(Notifier.TAG, "mWakeUpBroadcastDone - sendNextBroadcast");
            }
            EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_DONE, new Object[]{Integer.valueOf(1), Long.valueOf(SystemClock.uptimeMillis() - Notifier.this.mBroadcastStartTime), Integer.valueOf(1)});
            Notifier.this.sendNextBroadcast();
        }
    };

    private final class NotifierHandler extends Handler {
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
                    Notifier.this.playWirelessChargingStartedSound();
                    return;
                case 4:
                    Notifier.this.sendBrightnessBoostChangedBroadcast();
                    return;
                default:
                    return;
            }
        }
    }

    public Notifier(Looper looper, Context context, IBatteryStats batteryStats, IAppOpsService appOps, SuspendBlocker suspendBlocker, WindowManagerPolicy policy) {
        this.mContext = context;
        this.mBatteryStats = batteryStats;
        this.mAppOps = appOps;
        this.mSuspendBlocker = suspendBlocker;
        this.mPolicy = policy;
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
        this.mInputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
        this.mHandler = new NotifierHandler(looper);
        this.mScreenOnIntent = new Intent("android.intent.action.SCREEN_ON");
        this.mScreenOnIntent.addFlags(1344274432);
        this.mScreenOffIntent = new Intent("android.intent.action.SCREEN_OFF");
        this.mScreenOffIntent.addFlags(1344274432);
        this.mScreenBrightnessBoostIntent = new Intent("android.os.action.SCREEN_BRIGHTNESS_BOOST_CHANGED");
        this.mScreenBrightnessBoostIntent.addFlags(1342177280);
        this.mSuspendWhenScreenOffDueToProximityConfig = context.getResources().getBoolean(17957037);
        try {
            this.mBatteryStats.noteInteractive(true);
        } catch (RemoteException e) {
        }
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
            boolean unimportantForLogging = ownerUid == 1000 ? (1073741824 & flags) != 0 : false;
            if (workSource != null) {
                try {
                    this.mBatteryStats.noteStartWakelockFromSource(workSource, ownerPid, tag, historyTag, monitorType, unimportantForLogging);
                    return;
                } catch (RemoteException e) {
                    return;
                }
            }
            this.mBatteryStats.noteStartWakelock(ownerUid, ownerPid, tag, historyTag, monitorType, unimportantForLogging);
            this.mAppOps.startOperation(AppOpsManager.getToken(this.mAppOps), 40, ownerUid, packageName);
        }
    }

    public void onLongPartialWakeLockStart(String tag, int ownerUid, WorkSource workSource, String historyTag) {
        if (DEBUG) {
            Slog.d(TAG, "onLongPartialWakeLockStart: ownerUid=" + ownerUid + ", workSource=" + workSource);
        }
        if (workSource != null) {
            try {
                int N = workSource.size();
                for (int i = 0; i < N; i++) {
                    this.mBatteryStats.noteLongPartialWakelockStart(tag, historyTag, workSource.get(i));
                }
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        this.mBatteryStats.noteLongPartialWakelockStart(tag, historyTag, ownerUid);
    }

    public void onLongPartialWakeLockFinish(String tag, int ownerUid, WorkSource workSource, String historyTag) {
        if (DEBUG) {
            Slog.d(TAG, "onLongPartialWakeLockFinish: ownerUid=" + ownerUid + ", workSource=" + workSource);
        }
        if (workSource != null) {
            try {
                int N = workSource.size();
                for (int i = 0; i < N; i++) {
                    this.mBatteryStats.noteLongPartialWakelockFinish(tag, historyTag, workSource.get(i));
                }
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        this.mBatteryStats.noteLongPartialWakelockFinish(tag, historyTag, ownerUid);
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
        boolean unimportantForLogging = newOwnerUid == 1000 ? (1073741824 & newFlags) != 0 : false;
        try {
            this.mBatteryStats.noteChangeWakelockFromSource(workSource, ownerPid, tag, historyTag, monitorType, newWorkSource, newOwnerPid, newTag, newHistoryTag, newMonitorType, unimportantForLogging);
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
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        this.mBatteryStats.noteStopWakelock(ownerUid, ownerPid, tag, historyTag, monitorType);
        this.mAppOps.finishOperation(AppOpsManager.getToken(this.mAppOps), 40, ownerUid, packageName);
    }

    private int getBatteryStatsWakeLockMonitorType(int flags) {
        switch (NetworkConstants.ARP_HWTYPE_RESERVED_HI & flags) {
            case 1:
                return 0;
            case 6:
            case 10:
                return 1;
            case 32:
                return this.mSuspendWhenScreenOffDueToProximityConfig ? -1 : 0;
            case 64:
                return -1;
            case 128:
                return 18;
            default:
                return -1;
        }
    }

    private String translateInteractiveReason(int reason) {
        switch (reason) {
            case 1:
                return "GO_TO_SLEEP_REASON_DEVICE_ADMIN";
            case 2:
                return "GO_TO_SLEEP_REASON_TIMEOUT";
            case 3:
                return "GO_TO_SLEEP_REASON_LID_SWITCH";
            case 4:
                return "GO_TO_SLEEP_REASON_POWER_BUTTON";
            case 5:
                return "GO_TO_SLEEP_REASON_HDMI";
            case 6:
                return "GO_TO_SLEEP_REASON_SLEEP_BUTTON";
            case 7:
                return "GO_TO_SLEEP_REASON_PROXIMITY";
            case HdmiCecKeycode.CEC_KEYCODE_RECORD_FUNCTION /*98*/:
                return "WAKE_UP_REASON_FINGERPRINT";
            default:
                return "" + reason;
        }
    }

    public void onWakefulnessChangeStarted(final int wakefulness, int reason) {
        boolean interactive = PowerManagerInternal.isInteractive(wakefulness);
        if (interactive && 98 == reason) {
            this.mSkipWakeUpBroadcast = true;
        }
        if (!interactive && this.mSkipWakeUpBroadcast) {
            this.mSkipGoToSleepBroadcast = true;
        }
        if (DEBUG_PANIC || DEBUG) {
            Slog.d(TAG, "onWakefulnessChangeStarted: wakefulness=" + wakefulness + ", reason=" + translateInteractiveReason(reason) + ", interactive=" + interactive + ", mSkipWakeUpBroadcast = " + this.mSkipWakeUpBroadcast + ", mSkipGoToSleepBroadcast = " + this.mSkipGoToSleepBroadcast);
        }
        this.mHandler.post(new Runnable() {
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
            if (!(this.mSkipWakeUpBroadcast || (this.mSkipGoToSleepBroadcast ^ 1) == 0)) {
                this.mInputManagerInternal.setInteractive(interactive);
                this.mInputMethodManagerInternal.setInteractive(interactive);
            }
            try {
                this.mBatteryStats.noteInteractive(interactive);
            } catch (RemoteException e) {
            }
            this.mInteractive = interactive;
            this.mInteractiveChangeReason = reason;
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
                    public void run() {
                        Notifier.this.mPolicy.startedWakingUp();
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
            if (this.mInteractive) {
                if (this.mSkipWakeUpBroadcast && fromChangeFinished) {
                    this.mPendingWakeUpBroadcast = true;
                    updatePendingBroadcastLocked();
                }
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Notifier.this.mPolicy.finishedWakingUp();
                    }
                });
            } else {
                if (this.mUserActivityPending) {
                    this.mUserActivityPending = false;
                    this.mHandler.removeMessages(1);
                }
                final int why = translateOffReason(this.mInteractiveChangeReason);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        LogMaker log = new LogMaker(198);
                        log.setType(2);
                        log.setSubtype(why);
                        MetricsLogger.action(log);
                        EventLogTags.writePowerScreenState(0, why, 0, 0, 0);
                        Notifier.this.mPolicy.finishedGoingToSleep(why);
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
        switch (reason) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 7:
                return 4;
            default:
                return 2;
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

    public void onWakeUp(String reason, int reasonUid, String opPackageName, int opUid) {
        if (DEBUG) {
            Slog.d(TAG, "onWakeUp: event=" + reason + ", reasonUid=" + reasonUid + " opPackageName=" + opPackageName + " opUid=" + opUid);
        }
        try {
            this.mBatteryStats.noteWakeUp(reason, reasonUid);
            if (opPackageName != null) {
                this.mAppOps.noteOperation(61, opUid, opPackageName);
            }
        } catch (RemoteException e) {
        }
    }

    public void onWirelessChargingStarted() {
        if (DEBUG) {
            Slog.d(TAG, "onWirelessChargingStarted");
        }
        this.mSuspendBlocker.acquire();
        Message msg = this.mHandler.obtainMessage(3);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    private void updatePendingBroadcastLocked() {
        if (DEBUG) {
            Slog.d(TAG, "updatePendingBroadcastLocked mBroadcastInProgress = " + this.mBroadcastInProgress + ", mPendingInteractiveState = " + this.mPendingInteractiveState + ", mPendingWakeUpBroadcast = " + this.mPendingWakeUpBroadcast + ", mPendingGoToSleepBroadcast = " + this.mPendingGoToSleepBroadcast + ", mBroadcastedInteractiveState = " + this.mBroadcastedInteractiveState);
        }
        if (!this.mBroadcastInProgress && this.mPendingInteractiveState != 0) {
            if (this.mPendingWakeUpBroadcast || this.mPendingGoToSleepBroadcast || this.mPendingInteractiveState != this.mBroadcastedInteractiveState) {
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

    private void sendUserActivity() {
        synchronized (this.mLock) {
            if (this.mUserActivityPending) {
                this.mUserActivityPending = false;
                this.mPolicy.userActivity();
                return;
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0045, code:
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.POWER_SCREEN_BROADCAST_SEND, 1);
     */
    /* JADX WARNING: Missing block: B:12:0x004a, code:
            if (r0 != 1) goto L_0x0093;
     */
    /* JADX WARNING: Missing block: B:13:0x004c, code:
            sendWakeUpBroadcast();
     */
    /* JADX WARNING: Missing block: B:14:0x004f, code:
            return;
     */
    /* JADX WARNING: Missing block: B:50:0x0093, code:
            sendGoToSleepBroadcast();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendNextBroadcast() {
        if (DEBUG_PANIC) {
            Slog.d(TAG, "sendNextBroadcast, mBroadcastedInteractiveState=" + this.mBroadcastedInteractiveState + ", mPendingInteractiveState=" + this.mPendingInteractiveState);
        }
        synchronized (this.mLock) {
            if (this.mBroadcastedInteractiveState == 0) {
                this.mPendingWakeUpBroadcast = false;
                this.mBroadcastedInteractiveState = 1;
            } else if (this.mBroadcastedInteractiveState == 1) {
                if (this.mPendingWakeUpBroadcast || this.mPendingGoToSleepBroadcast || this.mPendingInteractiveState == 2) {
                    this.mPendingGoToSleepBroadcast = false;
                    this.mBroadcastedInteractiveState = 2;
                } else {
                    finishPendingBroadcastLocked();
                    return;
                }
            } else if (this.mPendingWakeUpBroadcast || this.mPendingGoToSleepBroadcast || this.mPendingInteractiveState == 1) {
                this.mPendingWakeUpBroadcast = false;
                this.mBroadcastedInteractiveState = 1;
                if (this.mSkipWakeUpBroadcast || this.mSkipGoToSleepBroadcast) {
                    finishPendingBroadcastLocked();
                    return;
                }
            } else {
                finishPendingBroadcastLocked();
                return;
            }
            this.mBroadcastStartTime = SystemClock.uptimeMillis();
            int powerState = this.mBroadcastedInteractiveState;
        }
    }

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
        EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_STOP, new Object[]{Integer.valueOf(2), Integer.valueOf(1)});
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
        EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_STOP, new Object[]{Integer.valueOf(3), Integer.valueOf(1)});
        sendNextBroadcast();
    }

    private void playWirelessChargingStartedSound() {
        boolean enabled = Global.getInt(this.mContext.getContentResolver(), "charging_sounds_enabled", 1) != 0;
        String soundPath = Global.getString(this.mContext.getContentResolver(), "wireless_charging_started_sound");
        if (enabled && soundPath != null) {
            Uri soundUri = Uri.parse("file://" + soundPath);
            if (soundUri != null) {
                Ringtone sfx = RingtoneManager.getRingtone(this.mContext, soundUri);
                if (sfx != null) {
                    sfx.setStreamType(1);
                    sfx.play();
                }
            }
        }
        this.mSuspendBlocker.release();
    }
}
