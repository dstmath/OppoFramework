package com.android.server;

import android.app.ActivityManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.AppOpsManager;
import android.app.BroadcastOptions;
import android.app.IAlarmCompleteListener;
import android.app.IAlarmListener;
import android.app.IAlarmManager.Stub;
import android.app.IUidObserver;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.PendingIntent.OnFinished;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.PowerSaveState;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.KeyValueListParser;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseLongArray;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.LocalLog;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.OppoAppSwitchManager;
import com.android.server.am.OppoAppSwitchManager.ActivityChangedListener;
import com.android.server.am.OppoProcessManager;
import com.android.server.am.OppoProcessManagerHelper;
import com.android.server.backup.RefactoredBackupManagerService;
import com.android.server.display.OppoBrightUtils;
import com.android.server.face.FaceDaemonWrapper;
import com.android.server.job.controllers.JobStatus;
import com.android.server.usage.UnixCalendar;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

class AlarmManagerService extends SystemService implements Callbacks {
    static final int ALARM_EVENT = 1;
    static final boolean DEBUG_ALARM_CLOCK = false;
    static final boolean DEBUG_BATCH = false;
    static boolean DEBUG_DETAIL = false;
    static final boolean DEBUG_LISTENER_CALLBACK = false;
    static boolean DEBUG_PANIC = false;
    static boolean DEBUG_PENDING = false;
    static final boolean DEBUG_VALIDATE = false;
    static final boolean DEBUG_WAKELOCK = false;
    private static final int ELAPSED_REALTIME_MASK = 8;
    private static final int ELAPSED_REALTIME_WAKEUP_MASK = 4;
    static final int IS_WAKEUP_MASK = 5;
    static final long MIN_FUZZABLE_INTERVAL = 10000;
    private static final Intent NEXT_ALARM_CLOCK_CHANGED_INTENT = new Intent("android.app.action.NEXT_ALARM_CLOCK_CHANGED").addFlags(553648128);
    static final int PRIO_NORMAL = 2;
    static final int PRIO_TICK = 0;
    static final int PRIO_WAKEUP = 1;
    static final boolean RECORD_ALARMS_IN_HISTORY = true;
    static final boolean RECORD_DEVICE_IDLE_ALARMS = false;
    private static final int RTC_MASK = 2;
    private static final int RTC_WAKEUP_MASK = 1;
    private static final String SYSTEM_UI_SELF_PERMISSION = "android.permission.systemui.IDENTITY";
    static final String TAG = "AlarmManager";
    static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    static final int TIME_CHANGED_MASK = 65536;
    static final int TYPE_NONWAKEUP_MASK = 1;
    static final boolean WAKEUP_STATS = false;
    static final boolean localLOGV = false;
    static long mLastPowerOffTime = 0;
    static final BatchTimeOrder sBatchOrder = new BatchTimeOrder();
    static final IncreasingTimeOrder sIncreasingTimeOrder = new IncreasingTimeOrder();
    final long RECENT_WAKEUP_PERIOD = UnixCalendar.DAY_IN_MILLIS;
    private ActivityChangedListener mActivityChangedListener = new ActivityChangedListener() {
        public void onActivityChanged(String prePkg, String nextPkg) {
            if (nextPkg != null && !nextPkg.equals(AlarmManagerService.this.mTopPkg) && !"com.coloros.recents".equals(nextPkg) && !ActivityManagerService.OPPO_LAUNCHER.equals(nextPkg)) {
                AlarmManagerService.this.mTopPkg = nextPkg;
                synchronized (AlarmManagerService.this.mListTopPkg) {
                    if (AlarmManagerService.this.mListTopPkg.size() < 3) {
                        if (!AlarmManagerService.this.mListTopPkg.contains(nextPkg)) {
                            AlarmManagerService.this.mListTopPkg.add(nextPkg);
                        }
                    } else if (AlarmManagerService.this.mListTopPkg.size() == 3) {
                        if (AlarmManagerService.this.mListTopPkg.contains(nextPkg)) {
                            AlarmManagerService.this.mListTopPkg.remove(nextPkg);
                            AlarmManagerService.this.mListTopPkg.add(nextPkg);
                        } else {
                            AlarmManagerService.this.mListTopPkg.remove(0);
                            AlarmManagerService.this.mListTopPkg.add(nextPkg);
                        }
                    }
                }
                if (AlarmManagerService.this.mColorOsLowPowerModeEnabled.get()) {
                    AlarmManagerService.this.mHandler.removeMessages(103);
                    AlarmManagerService.this.mHandler.sendEmptyMessage(103);
                }
            }
        }
    };
    final ArrayList<Batch> mAlarmBatches = new ArrayList();
    final Comparator<Alarm> mAlarmDispatchComparator = new Comparator<Alarm>() {
        public int compare(Alarm lhs, Alarm rhs) {
            if (lhs.priorityClass.priority < rhs.priorityClass.priority) {
                return -1;
            }
            if (lhs.priorityClass.priority > rhs.priorityClass.priority) {
                return 1;
            }
            if (lhs.whenElapsed < rhs.whenElapsed) {
                return -1;
            }
            if (lhs.whenElapsed > rhs.whenElapsed) {
                return 1;
            }
            return 0;
        }
    };
    AlarmMonitor mAlarmMonitor = null;
    AlarmUpdateHelper mAlarmUpdateHelper;
    private OppoAlarmWakeupDetection mAlarmWakeupDetection = null;
    final ArrayList<IdleDispatchEntry> mAllowWhileIdleDispatches = new ArrayList();
    long mAllowWhileIdleMinTime;
    AppOpsManager mAppOps;
    private final Intent mBackgroundIntent = new Intent().addFlags(4);
    int mBroadcastRefCount = 0;
    final SparseArray<ArrayMap<String, BroadcastStats>> mBroadcastStats = new SparseArray();
    ClockReceiver mClockReceiver;
    private AtomicBoolean mColorOsLowPowerModeEnabled = new AtomicBoolean(false);
    final Constants mConstants;
    int mCurrentSeq = 0;
    PendingIntent mDateChangeSender;
    int mDelayAlarmMode = 0;
    ArrayList<Alarm> mDelayedForPurBackgroundAlarms = new ArrayList();
    final DeliveryTracker mDeliveryTracker = new DeliveryTracker();
    int[] mDeviceIdleUserWhitelist = new int[0];
    final AlarmHandler mHandler = new AlarmHandler();
    private final SparseArray<AlarmClockInfo> mHandlerSparseAlarmClockArray = new SparseArray();
    Bundle mIdleOptions;
    ArrayList<InFlight> mInFlight = new ArrayList();
    boolean mInteractive = true;
    InteractiveStateReceiver mInteractiveStateReceiver;
    private boolean mIsHighTemperatureProtectIn = false;
    long mLastAlarmDeliveryTime;
    final SparseLongArray mLastAllowWhileIdleDispatch = new SparseLongArray();
    long mLastTimeChangeClockTime;
    long mLastTimeChangeRealtime;
    boolean mLastWakeLockUnimportantForLogging;
    private long mLastWakeup;
    private long mLastWakeupSet;
    ArrayList<Alarm> mListPowerSaveSuspend = new ArrayList();
    ArrayList<String> mListTopPkg = new ArrayList();
    @GuardedBy("mLock")
    private int mListenerCount = 0;
    @GuardedBy("mLock")
    private int mListenerFinishCount = 0;
    com.android.server.DeviceIdleController.LocalService mLocalDeviceIdleController;
    final Object mLock = new Object();
    final LocalLog mLog = new LocalLog(TAG);
    long mMaxDelayTime = 0;
    long mNativeData;
    boolean mNetstateInteractive = true;
    private final SparseArray<AlarmClockInfo> mNextAlarmClockForUser = new SparseArray();
    private boolean mNextAlarmClockMayChange;
    private long mNextNonWakeup;
    long mNextNonWakeupDeliveryTime;
    Alarm mNextWakeFromIdle = null;
    private long mNextWakeup;
    long mNonInteractiveStartTime;
    long mNonInteractiveTime;
    int mNumDelayedAlarms = 0;
    int mNumTimeChanged;
    private OppoAlarmAlignment mOppoAlarmAlignment = null;
    private boolean mOppoGuardElfFeature = false;
    Alarm mPendingIdleUntil = null;
    ArrayList<Alarm> mPendingImportantNonWakeupAlarms = new ArrayList();
    ArrayList<Alarm> mPendingNonWakeupAlarms = new ArrayList();
    private final SparseBooleanArray mPendingSendNextAlarmClockChangedForUser = new SparseBooleanArray();
    ArrayList<Alarm> mPendingWhileIdleAlarms = new ArrayList();
    private PowerManagerInternal mPowerManagerInternal;
    final HashMap<String, PriorityClass> mPriorities = new HashMap();
    Random mRandom;
    final LinkedList<WakeupEvent> mRecentWakeups = new LinkedList();
    @GuardedBy("mLock")
    private int mSendCount = 0;
    @GuardedBy("mLock")
    private int mSendFinishCount = 0;
    private final IBinder mService = new Stub() {
        public void set(String callingPackage, int type, long triggerAtTime, long windowLength, long interval, int flags, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, WorkSource workSource, AlarmClockInfo alarmClock) {
            int callingUid = Binder.getCallingUid();
            AlarmManagerService.this.mAppOps.checkPackage(callingUid, callingPackage);
            if (interval == 0 || directReceiver == null) {
                if (workSource != null) {
                    AlarmManagerService.this.getContext().enforcePermission("android.permission.UPDATE_DEVICE_STATS", Binder.getCallingPid(), callingUid, "AlarmManager.set");
                }
                flags &= -11;
                if (callingUid != 1000) {
                    flags &= -17;
                }
                if (windowLength == 0) {
                    flags |= 1;
                }
                if (alarmClock != null) {
                    flags |= 3;
                } else if (workSource == null && (callingUid < 10000 || callingUid == AlarmManagerService.this.mSystemUiUid || Arrays.binarySearch(AlarmManagerService.this.mDeviceIdleUserWhitelist, UserHandle.getAppId(callingUid)) >= 0)) {
                    flags = (flags | 8) & -5;
                }
                AlarmManagerService.this.setImpl(AlarmManagerService.this.mAlarmUpdateHelper.convertType(type, operation, callingPackage), triggerAtTime, windowLength, interval, operation, directReceiver, listenerTag, flags, workSource, alarmClock, callingUid, callingPackage);
                return;
            }
            throw new IllegalArgumentException("Repeating alarms cannot use AlarmReceivers");
        }

        public boolean setTime(long millis) {
            boolean z = false;
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME", "setTime");
            if (AlarmManagerService.this.mNativeData == 0) {
                Slog.w(AlarmManagerService.TAG, "Not setting time since no alarm driver is available.");
                return false;
            }
            synchronized (AlarmManagerService.this.mLock) {
                if (AlarmManagerService.this.setKernelTime(AlarmManagerService.this.mNativeData, millis) == 0) {
                    z = true;
                }
            }
            return z;
        }

        public void setTimeZone(String tz) {
            AlarmManagerService.this.getContext().enforceCallingOrSelfPermission("android.permission.SET_TIME_ZONE", "setTimeZone");
            long oldId = Binder.clearCallingIdentity();
            try {
                AlarmManagerService.this.setTimeZoneImpl(tz);
            } finally {
                Binder.restoreCallingIdentity(oldId);
            }
        }

        public void remove(PendingIntent operation, IAlarmListener listener) {
            if (operation == null && listener == null) {
                Slog.w(AlarmManagerService.TAG, "remove() with no intent or listener");
                return;
            }
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.removeLocked(operation, listener);
            }
        }

        public long getNextWakeFromIdleTime() {
            return AlarmManagerService.this.getNextWakeFromIdleTimeImpl();
        }

        public AlarmClockInfo getNextAlarmClock(int userId) {
            return AlarmManagerService.this.getNextAlarmClockImpl(ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, "getNextAlarmClock", null));
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (!DumpUtils.checkDumpAndUsageStatsPermission(AlarmManagerService.this.getContext(), AlarmManagerService.TAG, pw)) {
                return;
            }
            if (args.length >= 1 && "frequentAlarm".equals(args[0])) {
                if (AlarmManagerService.this.mAlarmWakeupDetection != null) {
                    AlarmManagerService.this.mAlarmWakeupDetection.dumpFrequentAlarm(pw);
                }
            } else if (!AlarmManagerService.this.dynamicallyConfigAlarmManagerServiceLogTag(pw, args)) {
                AlarmManagerService.this.dumpImpl(pw);
                if (args.length >= 1 && "alignWhiteList".equals(args[0])) {
                    OppoAlarmManagerHelper.dump(pw);
                }
                if (args.length >= 1) {
                    if ("highTemperatureProtectIn".equals(args[0])) {
                        AlarmManagerService.this.mIsHighTemperatureProtectIn = true;
                    } else if ("highTemperatureProtectOut".equals(args[0])) {
                        AlarmManagerService.this.mIsHighTemperatureProtectIn = false;
                    }
                }
            }
        }

        public void updateBlockedUids(int uid, boolean isBlocked) {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.qcNsrmExt.processBlockedUids(uid, isBlocked, AlarmManagerService.this.mWakeLock);
            }
        }
    };
    long mStartCurrentDelayTime;
    int mSystemUiUid;
    PendingIntent mTimeTickSender;
    private final SparseArray<AlarmClockInfo> mTmpSparseAlarmClockArray = new SparseArray();
    private volatile String mTopPkg = "";
    long mTotalDelayTime = 0;
    private UninstallReceiver mUninstallReceiver;
    WakeLock mWakeLock;
    private QCNsrmAlarmExtension qcNsrmExt = new QCNsrmAlarmExtension(this);

    protected static class Alarm {
        public final AlarmClockInfo alarmClock;
        public int callingPid;
        public int callingUid;
        public int count;
        public final int creatorUid;
        public final int flags;
        public final IAlarmListener listener;
        public final String listenerTag;
        public long maxWhenElapsed;
        public final PendingIntent operation;
        public final long origWhen;
        public final String packageName;
        public PriorityClass priorityClass;
        public long repeatInterval;
        public final String statsTag;
        public final int type;
        public final int uid;
        public final boolean wakeup;
        public long when;
        public long whenElapsed;
        public long whenElapsedAllowWhileIdle = 0;
        public long windowLength;
        public final WorkSource workSource;

        public Alarm(int _type, long _when, long _whenElapsed, long _windowLength, long _maxWhen, long _interval, PendingIntent _op, IAlarmListener _rec, String _listenerTag, WorkSource _ws, int _flags, AlarmClockInfo _info, int _uid, String _pkgName) {
            this.type = _type;
            this.origWhen = _when;
            boolean z = _type != 2 ? _type == 0 : true;
            this.wakeup = z;
            this.when = _when;
            this.whenElapsed = _whenElapsed;
            this.windowLength = _windowLength;
            this.maxWhenElapsed = _maxWhen;
            this.repeatInterval = _interval;
            this.operation = _op;
            this.listener = _rec;
            this.listenerTag = _listenerTag;
            this.statsTag = makeTag(_op, _listenerTag, _type);
            this.workSource = _ws;
            this.flags = _flags;
            this.alarmClock = _info;
            this.uid = _uid;
            this.packageName = _pkgName;
            this.creatorUid = this.operation != null ? this.operation.getCreatorUid() : this.uid;
            this.callingPid = Binder.getCallingPid();
            this.callingUid = Binder.getCallingUid();
        }

        public static String makeTag(PendingIntent pi, String tag, int type) {
            String alarmString = (type == 2 || type == 0) ? "*walarm*:" : "*alarm*:";
            return pi != null ? pi.getTag(alarmString) : alarmString + tag;
        }

        public WakeupEvent makeWakeupEvent(long nowRTC) {
            String action;
            int i = this.creatorUid;
            if (this.operation != null) {
                action = this.operation.getIntent().getAction();
            } else {
                action = "<listener>:" + this.listenerTag;
            }
            return new WakeupEvent(nowRTC, i, action);
        }

        public boolean matches(PendingIntent pi, IAlarmListener rec) {
            if (this.operation != null) {
                return this.operation.equals(pi);
            }
            return rec != null ? this.listener.asBinder().equals(rec.asBinder()) : false;
        }

        public boolean matches(String packageName) {
            if (this.operation != null) {
                return packageName.equals(this.operation.getTargetPackage());
            }
            return packageName.equals(this.packageName);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Alarm{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" type ");
            sb.append(this.type);
            sb.append(" when ");
            sb.append(this.when);
            sb.append(" ");
            if (this.operation != null) {
                sb.append(this.operation.getTargetPackage());
            } else {
                sb.append(this.packageName);
            }
            sb.append(" whenElapsed ");
            sb.append(this.whenElapsed);
            sb.append(" windowLength ");
            sb.append(this.windowLength);
            sb.append(" maxWhenElapsed ");
            sb.append(this.maxWhenElapsed);
            sb.append(" repeatInterval ");
            sb.append(this.repeatInterval);
            if (this.operation != null) {
                try {
                    sb.append(" action ");
                    sb.append(this.operation.getIntent().getAction());
                    sb.append(" component ");
                    sb.append(this.operation.getIntent().getComponent());
                } catch (SecurityException e) {
                }
            }
            if (this.listenerTag != null) {
                sb.append(" tag ");
                sb.append(this.listenerTag);
            }
            sb.append(" whenElapsed ");
            sb.append(this.whenElapsed);
            sb.append(" windowLength ");
            sb.append(this.windowLength);
            sb.append(" maxWhenElapsed ");
            sb.append(this.maxWhenElapsed);
            sb.append(" repeatInterval ");
            sb.append(this.repeatInterval);
            if (this.operation != null) {
                try {
                    sb.append(" action ");
                    sb.append(this.operation.getIntent().getAction());
                    sb.append(" component ");
                    sb.append(this.operation.getIntent().getComponent());
                } catch (SecurityException e2) {
                    Slog.d(AlarmManagerService.TAG, "Alarm.toString()", new Throwable());
                }
            }
            sb.append('}');
            return sb.toString();
        }

        public String toStringLite() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Alarm{");
            sb.append("t ");
            sb.append(this.type);
            sb.append(" when ");
            sb.append(this.when);
            sb.append(" ");
            if (this.operation != null) {
                sb.append(this.operation.getTargetPackage());
            } else {
                sb.append(this.packageName);
            }
            sb.append(" win ");
            sb.append(this.windowLength);
            sb.append(" re ");
            sb.append(this.repeatInterval);
            sb.append(" act ");
            if (this.operation != null) {
                try {
                    sb.append(this.operation.getIntent().getAction());
                } catch (SecurityException e) {
                }
            }
            sb.append('}');
            return sb.toString();
        }

        public void dump(PrintWriter pw, String prefix, long nowRTC, long nowELAPSED, SimpleDateFormat sdf) {
            boolean isRtc = this.type == 1 || this.type == 0;
            pw.print(prefix);
            pw.print("tag=");
            pw.println(this.statsTag);
            pw.print(prefix);
            pw.print("type=");
            pw.print(this.type);
            pw.print(" whenElapsed=");
            TimeUtils.formatDuration(this.whenElapsed, nowELAPSED, pw);
            pw.print(" when=");
            if (isRtc) {
                pw.print(sdf.format(new Date(this.when)));
            } else {
                TimeUtils.formatDuration(this.when, nowELAPSED, pw);
            }
            pw.println();
            pw.print(prefix);
            pw.print("window=");
            TimeUtils.formatDuration(this.windowLength, pw);
            pw.print(" repeatInterval=");
            pw.print(this.repeatInterval);
            pw.print(" count=");
            pw.print(this.count);
            pw.print(" flags=0x");
            pw.println(Integer.toHexString(this.flags));
            if (this.alarmClock != null) {
                pw.print(prefix);
                pw.println("Alarm clock:");
                pw.print(prefix);
                pw.print("  triggerTime=");
                pw.println(sdf.format(new Date(this.alarmClock.getTriggerTime())));
                pw.print(prefix);
                pw.print("  showIntent=");
                pw.println(this.alarmClock.getShowIntent());
            }
            pw.print(prefix);
            pw.print("operation=");
            pw.println(this.operation);
            if (this.listener != null) {
                pw.print(prefix);
                pw.print("listener=");
                pw.println(this.listener.asBinder());
            }
        }
    }

    public class AlarmCount {
        public int mCount = 0;
        public String mPackageName;
    }

    private class AlarmHandler extends Handler {
        public static final int ALARM_EVENT = 1;
        public static final int LISTENER_TIMEOUT = 3;
        public static final int POWER_SAVE_ENABLE = 101;
        public static final int POWER_SAVE_EXIT = 102;
        public static final int REPORT_ALARMS_ACTIVE = 4;
        public static final int SEND_NEXT_ALARM_CLOCK_CHANGED = 2;
        public static final int STOP_STRICTMODE = 6;
        public static final int TOP_APP_CHANGED = 103;
        public static final int UPDATE_NETSTATE = 5;

        public void handleMessage(Message msg) {
            boolean z = true;
            Object obj;
            switch (msg.what) {
                case 1:
                    ArrayList<Alarm> triggerList = new ArrayList();
                    synchronized (AlarmManagerService.this.mLock) {
                        long nowRTC = System.currentTimeMillis();
                        AlarmManagerService.this.triggerAlarmsLocked(triggerList, SystemClock.elapsedRealtime(), nowRTC);
                        AlarmManagerService.this.updateNextAlarmClockLocked();
                    }
                    for (int i = 0; i < triggerList.size(); i++) {
                        Alarm alarm = (Alarm) triggerList.get(i);
                        try {
                            alarm.operation.send();
                        } catch (CanceledException e) {
                            if (alarm.repeatInterval > 0) {
                                AlarmManagerService.this.removeImpl(alarm.operation);
                            }
                        }
                    }
                    return;
                case 2:
                    AlarmManagerService.this.sendNextAlarmClockChanged();
                    return;
                case 3:
                    AlarmManagerService.this.mDeliveryTracker.alarmTimedOut((IBinder) msg.obj);
                    return;
                case 4:
                    if (AlarmManagerService.this.mLocalDeviceIdleController != null) {
                        com.android.server.DeviceIdleController.LocalService localService = AlarmManagerService.this.mLocalDeviceIdleController;
                        if (msg.arg1 == 0) {
                            z = false;
                        }
                        localService.setAlarmsActive(z);
                        return;
                    }
                    return;
                case 5:
                    synchronized (AlarmManagerService.this.mLock) {
                        AlarmManagerService alarmManagerService = AlarmManagerService.this;
                        if (msg.arg1 != 1) {
                            z = false;
                        }
                        alarmManagerService.netStateChangedLocked(z);
                    }
                    return;
                case 6:
                    obj = AlarmManagerService.this.mLock;
                    synchronized (obj) {
                        AlarmManagerService.this.stopStrictModeLocked();
                        break;
                    }
                case 101:
                    obj = AlarmManagerService.this.mLock;
                    synchronized (obj) {
                        AlarmManagerService.this.mColorOsLowPowerModeEnabled.set(true);
                        break;
                    }
                case 102:
                    obj = AlarmManagerService.this.mLock;
                    synchronized (obj) {
                        AlarmManagerService.this.mColorOsLowPowerModeEnabled.set(false);
                        AlarmManagerService.this.powerSaveExitHandleLocked();
                        break;
                    }
                case 103:
                    obj = AlarmManagerService.this.mLock;
                    synchronized (obj) {
                        AlarmManagerService.this.topAppChangeHandleLocked();
                        break;
                    }
                default:
                    return;
            }
        }
    }

    class AlarmMonitor {
        private final int MAX_APP_RECORDED_LIMIT = 100;
        private List<AlarmCount> mAlarmCounts = new ArrayList();
        private int mAlarmWakeupCount = 0;

        AlarmMonitor() {
        }

        public synchronized void alarmWakeupIncrease() {
            this.mAlarmWakeupCount++;
        }

        /* JADX WARNING: Missing block: B:15:0x0018, code:
            return;
     */
        /* JADX WARNING: Missing block: B:29:0x005c, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized void appWakeupIncrease(Alarm alarm) {
            if (this.mAlarmCounts.size() <= 100) {
                if (alarm.type != 1 && alarm.type != 3) {
                    AlarmCount alarmCount;
                    String packageName = "";
                    if (alarm.packageName.length() > 0) {
                        packageName = alarm.packageName;
                    } else if (!(alarm.operation == null || alarm.operation.getIntent() == null)) {
                        packageName = alarm.operation.getIntent().getAction();
                    }
                    boolean updated = false;
                    for (AlarmCount alarmCount2 : this.mAlarmCounts) {
                        if (alarmCount2.mPackageName.equals(packageName)) {
                            alarmCount2.mCount++;
                            updated = true;
                            break;
                        }
                    }
                    if (!updated) {
                        alarmCount2 = new AlarmCount();
                        alarmCount2.mPackageName = packageName;
                        alarmCount2.mCount = 1;
                        this.mAlarmCounts.add(alarmCount2);
                    }
                }
            }
        }

        public synchronized void clear() {
            this.mAlarmWakeupCount = 0;
            this.mAlarmCounts.clear();
        }

        public synchronized int getFrameworkAlarmWakeupTimes() {
            return this.mAlarmWakeupCount;
        }

        public synchronized List getAppsAlarmWakeupTimes() {
            Collections.sort(this.mAlarmCounts, new Comparator<AlarmCount>() {
                public int compare(AlarmCount arg0, AlarmCount arg1) {
                    if (arg1.mCount > arg0.mCount) {
                        return 1;
                    }
                    if (arg1.mCount < arg0.mCount) {
                        return -1;
                    }
                    return 0;
                }
            });
            return new ArrayList(this.mAlarmCounts);
        }
    }

    private class AlarmThread extends Thread {
        public AlarmThread() {
            super(AlarmManagerService.TAG);
        }

        public void run() {
            ArrayList<Alarm> triggerList = new ArrayList();
            ArrayList<Alarm> triggerListNonWakeup = new ArrayList();
            while (true) {
                AlarmManagerService alarmManagerService;
                int result = AlarmManagerService.this.waitForAlarm(AlarmManagerService.this.mNativeData);
                AlarmManagerService.this.mLastWakeup = SystemClock.elapsedRealtime();
                triggerList.clear();
                triggerListNonWakeup.clear();
                long nowRTC = System.currentTimeMillis();
                long nowELAPSED = SystemClock.elapsedRealtime();
                if ((65536 & result) != 0) {
                    long lastTimeChangeClockTime;
                    synchronized (AlarmManagerService.this.mLock) {
                        lastTimeChangeClockTime = AlarmManagerService.this.mLastTimeChangeClockTime;
                        long expectedClockTime = lastTimeChangeClockTime + (nowELAPSED - AlarmManagerService.this.mLastTimeChangeRealtime);
                    }
                    if (lastTimeChangeClockTime == 0 || nowRTC < expectedClockTime - 1000 || nowRTC > 1000 + expectedClockTime) {
                        AlarmManagerService.this.removeImpl(AlarmManagerService.this.mTimeTickSender);
                        AlarmManagerService.this.removeImpl(AlarmManagerService.this.mDateChangeSender);
                        AlarmManagerService.this.rebatchAllAlarms();
                        AlarmManagerService.this.mClockReceiver.scheduleTimeTickEvent();
                        AlarmManagerService.this.mClockReceiver.scheduleDateChangedEvent();
                        synchronized (AlarmManagerService.this.mLock) {
                            alarmManagerService = AlarmManagerService.this;
                            alarmManagerService.mNumTimeChanged++;
                            AlarmManagerService.this.mLastTimeChangeClockTime = nowRTC;
                            AlarmManagerService.this.mLastTimeChangeRealtime = nowELAPSED;
                        }
                        Intent intent = new Intent("android.intent.action.TIME_SET");
                        intent.addFlags(622854144);
                        AlarmManagerService.this.getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
                        result |= 5;
                    }
                }
                Object obj;
                if (result != 65536) {
                    obj = AlarmManagerService.this.mLock;
                    synchronized (obj) {
                        if (AlarmManagerService.DEBUG_DETAIL) {
                            boolean z;
                            String str = AlarmManagerService.TAG;
                            StringBuilder append = new StringBuilder().append("Checking for alarms... rtc=").append(nowRTC).append(", elapsed=").append(nowELAPSED).append(", nextNoWakeup = ").append(AlarmManagerService.this.mNextNonWakeupDeliveryTime).append(", pending = ").append(AlarmManagerService.this.mPendingNonWakeupAlarms.size()).append(", pendingIm = ").append(AlarmManagerService.this.mPendingImportantNonWakeupAlarms.size()).append(", result = 0x").append(Integer.toHexString(result)).append(", hasPendingIdle = ");
                            if (AlarmManagerService.this.mPendingIdleUntil != null) {
                                z = true;
                            } else {
                                z = false;
                            }
                            Slog.v(str, append.append(z).toString());
                        } else if (AlarmManagerService.DEBUG_PANIC) {
                            Slog.v(AlarmManagerService.TAG, "Checking... rtc=" + nowRTC + ", elapsed=" + nowELAPSED + ", nonW=" + AlarmManagerService.this.mNextNonWakeupDeliveryTime + ", r=0x" + Integer.toHexString(result));
                        }
                        if (AlarmManagerService.DEBUG_PENDING) {
                            Slog.d(AlarmManagerService.TAG, "mPendingNonWakeupAlarms = " + AlarmManagerService.this.mPendingNonWakeupAlarms + "mPendingImportantNonWakeupAlarms = " + AlarmManagerService.this.mPendingImportantNonWakeupAlarms);
                        }
                        AlarmManagerService.this.mAlarmMonitor.alarmWakeupIncrease();
                        boolean hasWakeup = AlarmManagerService.this.triggerAlarmsOptLocked(triggerList, triggerListNonWakeup, nowELAPSED, nowRTC);
                        boolean bAllowNonWakeupDelay = AlarmManagerService.this.checkAllowNonWakeupDelayLocked(nowELAPSED);
                        if (bAllowNonWakeupDelay && AlarmManagerService.this.mPendingNonWakeupAlarms.size() == 0 && AlarmManagerService.this.mPendingImportantNonWakeupAlarms.size() == 0) {
                            AlarmManagerService.this.mStartCurrentDelayTime = nowELAPSED;
                            AlarmManagerService.this.mNextNonWakeupDeliveryTime = ((AlarmManagerService.this.currentNonWakeupFuzzLocked(nowELAPSED) * 3) / 2) + nowELAPSED;
                        }
                        AlarmManagerService.this.mPendingNonWakeupAlarms.addAll(triggerListNonWakeup);
                        alarmManagerService = AlarmManagerService.this;
                        alarmManagerService.mNumDelayedAlarms += triggerListNonWakeup.size();
                        long thisDelayTime;
                        if (hasWakeup) {
                            AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                            AlarmManagerService.this.updateNextAlarmClockLocked();
                            if (AlarmManagerService.this.mPendingNonWakeupAlarms.size() > 0 && (!bAllowNonWakeupDelay || AlarmManagerService.this.mDelayAlarmMode < 2)) {
                                AlarmManagerService.this.calculateDeliveryPriorities(AlarmManagerService.this.mPendingNonWakeupAlarms);
                                triggerList.addAll(AlarmManagerService.this.mPendingNonWakeupAlarms);
                                AlarmManagerService.this.clearPendingNonWakeupAlarmLocked(nowELAPSED, AlarmManagerService.this.mPendingNonWakeupAlarms);
                            }
                            if (AlarmManagerService.this.mPendingImportantNonWakeupAlarms.size() > 0) {
                                AlarmManagerService.this.calculateDeliveryPriorities(AlarmManagerService.this.mPendingImportantNonWakeupAlarms);
                                triggerList.addAll(AlarmManagerService.this.mPendingImportantNonWakeupAlarms);
                                AlarmManagerService.this.mPendingImportantNonWakeupAlarms.clear();
                            }
                            if (triggerList.size() > 0) {
                                Collections.sort(triggerList, AlarmManagerService.this.mAlarmDispatchComparator);
                                thisDelayTime = nowELAPSED - AlarmManagerService.this.mStartCurrentDelayTime;
                                alarmManagerService = AlarmManagerService.this;
                                alarmManagerService.mTotalDelayTime += thisDelayTime;
                                if (AlarmManagerService.this.mMaxDelayTime < thisDelayTime) {
                                    AlarmManagerService.this.mMaxDelayTime = thisDelayTime;
                                }
                            }
                            AlarmManagerService.this.deliverAlarmsLocked(triggerList, nowELAPSED);
                        } else {
                            if (bAllowNonWakeupDelay) {
                                if (AlarmManagerService.this.mPendingNonWakeupAlarms.size() <= 30) {
                                    if (triggerList.size() > 0) {
                                        AlarmManagerService.this.mPendingImportantNonWakeupAlarms.addAll(triggerList);
                                    }
                                    AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                                    AlarmManagerService.this.updateNextAlarmClockLocked();
                                }
                            }
                            if (AlarmManagerService.this.mPendingNonWakeupAlarms.size() > 0) {
                                AlarmManagerService.this.calculateDeliveryPriorities(AlarmManagerService.this.mPendingNonWakeupAlarms);
                                triggerList.addAll(AlarmManagerService.this.mPendingNonWakeupAlarms);
                                AlarmManagerService.this.clearPendingNonWakeupAlarmLocked(nowELAPSED, AlarmManagerService.this.mPendingNonWakeupAlarms);
                            }
                            if (AlarmManagerService.this.mPendingImportantNonWakeupAlarms.size() > 0) {
                                AlarmManagerService.this.calculateDeliveryPriorities(AlarmManagerService.this.mPendingImportantNonWakeupAlarms);
                                triggerList.addAll(AlarmManagerService.this.mPendingImportantNonWakeupAlarms);
                                AlarmManagerService.this.mPendingImportantNonWakeupAlarms.clear();
                            }
                            if (triggerList.size() > 0) {
                                Collections.sort(triggerList, AlarmManagerService.this.mAlarmDispatchComparator);
                                thisDelayTime = nowELAPSED - AlarmManagerService.this.mStartCurrentDelayTime;
                                alarmManagerService = AlarmManagerService.this;
                                alarmManagerService.mTotalDelayTime += thisDelayTime;
                                if (AlarmManagerService.this.mMaxDelayTime < thisDelayTime) {
                                    AlarmManagerService.this.mMaxDelayTime = thisDelayTime;
                                }
                            }
                            AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                            AlarmManagerService.this.updateNextAlarmClockLocked();
                            AlarmManagerService.this.deliverAlarmsLocked(triggerList, nowELAPSED);
                        }
                    }
                } else {
                    obj = AlarmManagerService.this.mLock;
                    synchronized (obj) {
                        AlarmManagerService.this.rescheduleKernelAlarmsLocked();
                    }
                }
            }
        }
    }

    final class Batch {
        final ArrayList<Alarm> alarms;
        long end;
        int flags;
        long start;

        Batch() {
            this.alarms = new ArrayList();
            this.start = 0;
            this.end = JobStatus.NO_LATEST_RUNTIME;
            this.flags = 0;
        }

        Batch(Alarm seed) {
            this.alarms = new ArrayList();
            this.start = seed.whenElapsed;
            this.end = seed.maxWhenElapsed;
            this.flags = seed.flags;
            this.alarms.add(seed);
        }

        int size() {
            return this.alarms.size();
        }

        Alarm get(int index) {
            return (Alarm) this.alarms.get(index);
        }

        boolean canHold(long whenElapsed, long maxWhen) {
            return this.end >= whenElapsed && this.start <= maxWhen;
        }

        boolean add(Alarm alarm) {
            boolean newStart = false;
            int index = Collections.binarySearch(this.alarms, alarm, AlarmManagerService.sIncreasingTimeOrder);
            if (index < 0) {
                index = (0 - index) - 1;
            }
            this.alarms.add(index, alarm);
            if (alarm.whenElapsed > this.start) {
                this.start = alarm.whenElapsed;
                newStart = true;
            }
            if (alarm.maxWhenElapsed < this.end) {
                this.end = alarm.maxWhenElapsed;
            }
            this.flags |= alarm.flags;
            return newStart;
        }

        boolean remove(PendingIntent operation, IAlarmListener listener) {
            if (operation == null && listener == null) {
                return false;
            }
            boolean didRemove = false;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int newFlags = 0;
            int i = 0;
            while (i < this.alarms.size()) {
                Alarm alarm = (Alarm) this.alarms.get(i);
                if (alarm.matches(operation, listener)) {
                    this.alarms.remove(i);
                    didRemove = true;
                    if (alarm.alarmClock != null) {
                        AlarmManagerService.this.mNextAlarmClockMayChange = true;
                    }
                } else {
                    if (alarm.whenElapsed > newStart) {
                        newStart = alarm.whenElapsed;
                    }
                    if (alarm.maxWhenElapsed < newEnd) {
                        newEnd = alarm.maxWhenElapsed;
                    }
                    newFlags |= alarm.flags;
                    i++;
                }
            }
            if (didRemove) {
                this.start = newStart;
                this.end = newEnd;
                this.flags = newFlags;
                AlarmManagerService.this.reScheduleAlignTickIfNeed(this);
            }
            return didRemove;
        }

        boolean remove(String packageName) {
            if (packageName == null) {
                return false;
            }
            boolean didRemove = false;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int newFlags = 0;
            for (int i = this.alarms.size() - 1; i >= 0; i--) {
                Alarm alarm = (Alarm) this.alarms.get(i);
                if (alarm.matches(packageName)) {
                    this.alarms.remove(i);
                    didRemove = true;
                    if (alarm.alarmClock != null) {
                        AlarmManagerService.this.mNextAlarmClockMayChange = true;
                    }
                } else {
                    if (alarm.whenElapsed > newStart) {
                        newStart = alarm.whenElapsed;
                    }
                    if (alarm.maxWhenElapsed < newEnd) {
                        newEnd = alarm.maxWhenElapsed;
                    }
                    newFlags |= alarm.flags;
                }
            }
            if (didRemove) {
                this.start = newStart;
                this.end = newEnd;
                this.flags = newFlags;
                AlarmManagerService.this.reScheduleAlignTickIfNeed(this);
            }
            return didRemove;
        }

        boolean removeForStopped(int uid) {
            boolean didRemove = false;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int newFlags = 0;
            int i = this.alarms.size() - 1;
            while (i >= 0) {
                Alarm alarm = (Alarm) this.alarms.get(i);
                try {
                    if (alarm.uid == uid && ActivityManager.getService().isAppStartModeDisabled(uid, alarm.packageName)) {
                        this.alarms.remove(i);
                        didRemove = true;
                        if (alarm.alarmClock != null) {
                            AlarmManagerService.this.mNextAlarmClockMayChange = true;
                        }
                        i--;
                    } else {
                        if (alarm.whenElapsed > newStart) {
                            newStart = alarm.whenElapsed;
                        }
                        if (alarm.maxWhenElapsed < newEnd) {
                            newEnd = alarm.maxWhenElapsed;
                        }
                        newFlags |= alarm.flags;
                        i--;
                    }
                } catch (RemoteException e) {
                }
            }
            if (didRemove) {
                this.start = newStart;
                this.end = newEnd;
                this.flags = newFlags;
                AlarmManagerService.this.reScheduleAlignTickIfNeed(this);
            }
            return didRemove;
        }

        boolean remove(int userHandle) {
            boolean didRemove = false;
            long newStart = 0;
            long newEnd = JobStatus.NO_LATEST_RUNTIME;
            int i = 0;
            while (i < this.alarms.size()) {
                Alarm alarm = (Alarm) this.alarms.get(i);
                if (UserHandle.getUserId(alarm.creatorUid) == userHandle) {
                    this.alarms.remove(i);
                    didRemove = true;
                    if (alarm.alarmClock != null) {
                        AlarmManagerService.this.mNextAlarmClockMayChange = true;
                    }
                } else {
                    if (alarm.whenElapsed > newStart) {
                        newStart = alarm.whenElapsed;
                    }
                    if (alarm.maxWhenElapsed < newEnd) {
                        newEnd = alarm.maxWhenElapsed;
                    }
                    i++;
                }
            }
            if (didRemove) {
                this.start = newStart;
                this.end = newEnd;
                AlarmManagerService.this.reScheduleAlignTickIfNeed(this);
            }
            return didRemove;
        }

        boolean hasPackage(String packageName) {
            int N = this.alarms.size();
            for (int i = 0; i < N; i++) {
                if (((Alarm) this.alarms.get(i)).matches(packageName)) {
                    return true;
                }
            }
            return false;
        }

        boolean hasWakeups() {
            int N = this.alarms.size();
            for (int i = 0; i < N; i++) {
                if ((((Alarm) this.alarms.get(i)).type & 1) == 0) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            StringBuilder b = new StringBuilder(40);
            b.append("Batch{");
            b.append(Integer.toHexString(hashCode()));
            b.append(" num=");
            b.append(size());
            b.append(" start=");
            b.append(this.start);
            b.append(" end=");
            b.append(this.end);
            if (this.flags != 0) {
                b.append(" flgs=0x");
                b.append(Integer.toHexString(this.flags));
            }
            b.append('}');
            return b.toString();
        }
    }

    static class BatchTimeOrder implements Comparator<Batch> {
        BatchTimeOrder() {
        }

        public int compare(Batch b1, Batch b2) {
            long when1 = b1.start;
            long when2 = b2.start;
            if (when1 > when2) {
                return 1;
            }
            if (when1 < when2) {
                return -1;
            }
            return 0;
        }
    }

    static final class BroadcastStats {
        long aggregateTime;
        int count;
        final ArrayMap<String, FilterStats> filterStats = new ArrayMap();
        long lastTimeTrigger;
        long lastTimeWakeup;
        final String mPackageName;
        final int mUid;
        int nesting;
        int numAllTrigger;
        int numAllWakeup;
        int numCanceledWakeup;
        int numWakeup;
        int numWakeupWhenScreenoff;
        int numWhenScreenoff;
        long startTime;
        long triggerCountStartTime;
        long wakeupCountStartTime;

        BroadcastStats(int uid, String packageName) {
            this.mUid = uid;
            this.mPackageName = packageName;
        }

        public String toString() {
            return "BroadcastStats{uid=" + this.mUid + ", packageName=" + this.mPackageName + ", aggregateTime=" + this.aggregateTime + ", count=" + this.count + ", numWakeup=" + this.numWakeup + ", startTime=" + this.startTime + ", nesting=" + this.nesting + "}";
        }
    }

    class ClockReceiver extends BroadcastReceiver {
        public ClockReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_TICK");
            filter.addAction("android.intent.action.DATE_CHANGED");
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.TIME_TICK")) {
                scheduleTimeTickEvent();
            } else if (intent.getAction().equals("android.intent.action.DATE_CHANGED")) {
                AlarmManagerService.this.setKernelTimezone(AlarmManagerService.this.mNativeData, -(TimeZone.getTimeZone(SystemProperties.get(AlarmManagerService.TIMEZONE_PROPERTY)).getOffset(System.currentTimeMillis()) / OppoBrightUtils.SPECIAL_AMBIENT_LIGHT_HORIZON));
                scheduleDateChangedEvent();
            }
        }

        public void scheduleTimeTickEvent() {
            long currentTime = System.currentTimeMillis();
            AlarmManagerService.this.setImpl(3, SystemClock.elapsedRealtime() + ((60000 * ((currentTime / 60000) + 1)) - currentTime), 0, 0, AlarmManagerService.this.mTimeTickSender, null, null, 1, null, null, Process.myUid(), "android");
        }

        public void scheduleDateChangedEvent() {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(11, 0);
            calendar.set(12, 0);
            calendar.set(13, 0);
            calendar.set(14, 0);
            calendar.add(5, 1);
            AlarmManagerService.this.setImpl(1, calendar.getTimeInMillis(), 0, 0, AlarmManagerService.this.mDateChangeSender, null, null, 1, null, null, Process.myUid(), "android");
        }
    }

    private final class Constants extends ContentObserver {
        private static final long DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME = 540000;
        private static final long DEFAULT_ALLOW_WHILE_IDLE_SHORT_TIME = 5000;
        private static final long DEFAULT_ALLOW_WHILE_IDLE_WHITELIST_DURATION = 10000;
        private static final long DEFAULT_LISTENER_TIMEOUT = 5000;
        private static final long DEFAULT_MIN_FUTURITY = 5000;
        private static final long DEFAULT_MIN_INTERVAL = 60000;
        private static final String KEY_ALLOW_WHILE_IDLE_LONG_TIME = "allow_while_idle_long_time";
        private static final String KEY_ALLOW_WHILE_IDLE_SHORT_TIME = "allow_while_idle_short_time";
        private static final String KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION = "allow_while_idle_whitelist_duration";
        private static final String KEY_LISTENER_TIMEOUT = "listener_timeout";
        private static final String KEY_MIN_FUTURITY = "min_futurity";
        private static final String KEY_MIN_INTERVAL = "min_interval";
        public long ALLOW_WHILE_IDLE_LONG_TIME = DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME;
        public long ALLOW_WHILE_IDLE_SHORT_TIME = FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK;
        public long ALLOW_WHILE_IDLE_WHITELIST_DURATION = 10000;
        public long LISTENER_TIMEOUT = FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK;
        public long MIN_FUTURITY = FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK;
        public long MIN_INTERVAL = 60000;
        private long mLastAllowWhileIdleWhitelistDuration = -1;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private ContentResolver mResolver;

        public Constants(Handler handler) {
            super(handler);
            updateAllowWhileIdleMinTimeLocked();
            updateAllowWhileIdleWhitelistDurationLocked();
        }

        public void start(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Global.getUriFor("alarm_manager_constants"), false, this);
            updateConstants();
        }

        public void updateAllowWhileIdleMinTimeLocked() {
            AlarmManagerService.this.mAllowWhileIdleMinTime = AlarmManagerService.this.mPendingIdleUntil != null ? this.ALLOW_WHILE_IDLE_LONG_TIME : this.ALLOW_WHILE_IDLE_SHORT_TIME;
        }

        public void updateAllowWhileIdleWhitelistDurationLocked() {
            if (this.mLastAllowWhileIdleWhitelistDuration != this.ALLOW_WHILE_IDLE_WHITELIST_DURATION) {
                this.mLastAllowWhileIdleWhitelistDuration = this.ALLOW_WHILE_IDLE_WHITELIST_DURATION;
                BroadcastOptions opts = BroadcastOptions.makeBasic();
                opts.setTemporaryAppWhitelistDuration(this.ALLOW_WHILE_IDLE_WHITELIST_DURATION);
                AlarmManagerService.this.mIdleOptions = opts.toBundle();
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (AlarmManagerService.this.mLock) {
                try {
                    this.mParser.setString(Global.getString(this.mResolver, "alarm_manager_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(AlarmManagerService.TAG, "Bad alarm manager settings", e);
                }
                this.MIN_FUTURITY = this.mParser.getLong(KEY_MIN_FUTURITY, FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK);
                this.MIN_INTERVAL = this.mParser.getLong(KEY_MIN_INTERVAL, 60000);
                this.ALLOW_WHILE_IDLE_SHORT_TIME = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_SHORT_TIME, FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK);
                this.ALLOW_WHILE_IDLE_LONG_TIME = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_LONG_TIME, DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME);
                this.ALLOW_WHILE_IDLE_WHITELIST_DURATION = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION, 10000);
                this.LISTENER_TIMEOUT = this.mParser.getLong(KEY_LISTENER_TIMEOUT, FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK);
                updateAllowWhileIdleMinTimeLocked();
                updateAllowWhileIdleWhitelistDurationLocked();
            }
            return;
        }

        void dump(PrintWriter pw) {
            pw.println("  Settings:");
            pw.print("    ");
            pw.print(KEY_MIN_FUTURITY);
            pw.print("=");
            TimeUtils.formatDuration(this.MIN_FUTURITY, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_MIN_INTERVAL);
            pw.print("=");
            TimeUtils.formatDuration(this.MIN_INTERVAL, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_LISTENER_TIMEOUT);
            pw.print("=");
            TimeUtils.formatDuration(this.LISTENER_TIMEOUT, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_ALLOW_WHILE_IDLE_SHORT_TIME);
            pw.print("=");
            TimeUtils.formatDuration(this.ALLOW_WHILE_IDLE_SHORT_TIME, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_ALLOW_WHILE_IDLE_LONG_TIME);
            pw.print("=");
            TimeUtils.formatDuration(this.ALLOW_WHILE_IDLE_LONG_TIME, pw);
            pw.println();
            pw.print("    ");
            pw.print(KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION);
            pw.print("=");
            TimeUtils.formatDuration(this.ALLOW_WHILE_IDLE_WHITELIST_DURATION, pw);
            pw.println();
        }
    }

    class DeliveryTracker extends IAlarmCompleteListener.Stub implements OnFinished {
        DeliveryTracker() {
        }

        private InFlight removeLocked(PendingIntent pi, Intent intent) {
            for (int i = 0; i < AlarmManagerService.this.mInFlight.size(); i++) {
                if (((InFlight) AlarmManagerService.this.mInFlight.get(i)).mPendingIntent == pi) {
                    return (InFlight) AlarmManagerService.this.mInFlight.remove(i);
                }
            }
            AlarmManagerService.this.mLog.w("No in-flight alarm for " + pi + " " + intent);
            return null;
        }

        private InFlight removeLocked(IBinder listener) {
            for (int i = 0; i < AlarmManagerService.this.mInFlight.size(); i++) {
                if (((InFlight) AlarmManagerService.this.mInFlight.get(i)).mListener == listener) {
                    return (InFlight) AlarmManagerService.this.mInFlight.remove(i);
                }
            }
            AlarmManagerService.this.mLog.w("No in-flight alarm for listener " + listener);
            return null;
        }

        private void updateStatsLocked(InFlight inflight) {
            long nowELAPSED = SystemClock.elapsedRealtime();
            BroadcastStats bs = inflight.mBroadcastStats;
            bs.nesting--;
            if (bs.nesting <= 0) {
                bs.nesting = 0;
                bs.aggregateTime += nowELAPSED - bs.startTime;
            }
            FilterStats fs = inflight.mFilterStats;
            fs.nesting--;
            if (fs.nesting <= 0) {
                fs.nesting = 0;
                fs.aggregateTime += nowELAPSED - fs.startTime;
            }
            if (inflight.mWorkSource == null || inflight.mWorkSource.size() <= 0) {
                ActivityManager.noteAlarmFinish(inflight.mPendingIntent, inflight.mUid, inflight.mTag);
                return;
            }
            for (int wi = 0; wi < inflight.mWorkSource.size(); wi++) {
                ActivityManager.noteAlarmFinish(inflight.mPendingIntent, inflight.mWorkSource.get(wi), inflight.mTag);
            }
        }

        private void updateTrackingLocked(InFlight inflight) {
            if (inflight != null) {
                updateStatsLocked(inflight);
            }
            AlarmManagerService alarmManagerService = AlarmManagerService.this;
            alarmManagerService.mBroadcastRefCount--;
            AlarmManagerService.this.qcNsrmExt.removeTriggeredUid(inflight.mUid);
            if (AlarmManagerService.this.mBroadcastRefCount == 0) {
                AlarmManagerService.this.mHandler.obtainMessage(4, Integer.valueOf(0)).sendToTarget();
                if (AlarmManagerService.this.mWakeLock.isHeld()) {
                    AlarmManagerService.this.mWakeLock.release();
                }
                if (AlarmManagerService.this.mInFlight.size() > 0) {
                    AlarmManagerService.this.mLog.w("Finished all dispatches with " + AlarmManagerService.this.mInFlight.size() + " remaining inflights");
                    for (int i = 0; i < AlarmManagerService.this.mInFlight.size(); i++) {
                        AlarmManagerService.this.mLog.w("  Remaining #" + i + ": " + AlarmManagerService.this.mInFlight.get(i));
                    }
                    AlarmManagerService.this.mInFlight.clear();
                }
            } else if (AlarmManagerService.this.mInFlight.size() > 0) {
                InFlight inFlight = (InFlight) AlarmManagerService.this.mInFlight.get(0);
                AlarmManagerService.this.setWakelockWorkSource(inFlight.mPendingIntent, inFlight.mWorkSource, inFlight.mAlarmType, inFlight.mTag, -1, false);
            } else {
                AlarmManagerService.this.mLog.w("Alarm wakelock still held but sent queue empty");
                AlarmManagerService.this.mWakeLock.setWorkSource(null);
            }
        }

        public void alarmComplete(IBinder who) {
            if (who == null) {
                AlarmManagerService.this.mLog.w("Invalid alarmComplete: uid=" + Binder.getCallingUid() + " pid=" + Binder.getCallingPid());
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (AlarmManagerService.this.mLock) {
                    AlarmManagerService.this.mHandler.removeMessages(3, who);
                    InFlight inflight = removeLocked(who);
                    if (inflight != null) {
                        updateTrackingLocked(inflight);
                        AlarmManagerService alarmManagerService = AlarmManagerService.this;
                        alarmManagerService.mListenerFinishCount = alarmManagerService.mListenerFinishCount + 1;
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void onSendFinished(PendingIntent pi, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService alarmManagerService = AlarmManagerService.this;
                alarmManagerService.mSendFinishCount = alarmManagerService.mSendFinishCount + 1;
                updateTrackingLocked(removeLocked(pi, intent));
            }
        }

        public void alarmTimedOut(IBinder who) {
            synchronized (AlarmManagerService.this.mLock) {
                InFlight inflight = removeLocked(who);
                if (inflight != null) {
                    updateTrackingLocked(inflight);
                    AlarmManagerService alarmManagerService = AlarmManagerService.this;
                    alarmManagerService.mListenerFinishCount = alarmManagerService.mListenerFinishCount + 1;
                } else {
                    AlarmManagerService.this.mLog.w("Spurious timeout of listener " + who);
                }
            }
        }

        public void deliverLocked(Alarm alarm, long nowELAPSED, boolean allowWhileIdle) {
            AlarmManagerService alarmManagerService;
            int creatorUid;
            if (alarm.operation != null) {
                alarmManagerService = AlarmManagerService.this;
                alarmManagerService.mSendCount = alarmManagerService.mSendCount + 1;
                try {
                    alarm.operation.send(AlarmManagerService.this.getContext(), 0, AlarmManagerService.this.mBackgroundIntent.putExtra("android.intent.extra.ALARM_COUNT", alarm.count), AlarmManagerService.this.mDeliveryTracker, AlarmManagerService.this.mHandler, null, allowWhileIdle ? AlarmManagerService.this.mIdleOptions : null);
                } catch (CanceledException e) {
                    if (AlarmManagerService.this.mAlarmWakeupDetection != null) {
                        AlarmManagerService.this.mAlarmWakeupDetection.canceledPendingIntentDetection(alarm, nowELAPSED);
                    }
                    if (alarm.repeatInterval > 0) {
                        AlarmManagerService.this.removeImpl(alarm.operation);
                    }
                    alarmManagerService = AlarmManagerService.this;
                    alarmManagerService.mSendFinishCount = alarmManagerService.mSendFinishCount + 1;
                    return;
                }
            }
            alarmManagerService = AlarmManagerService.this;
            alarmManagerService.mListenerCount = alarmManagerService.mListenerCount + 1;
            try {
                alarm.listener.doAlarm(this);
                AlarmManagerService.this.mHandler.sendMessageDelayed(AlarmManagerService.this.mHandler.obtainMessage(3, alarm.listener.asBinder()), AlarmManagerService.this.mConstants.LISTENER_TIMEOUT);
            } catch (Exception e2) {
                alarmManagerService = AlarmManagerService.this;
                alarmManagerService.mListenerFinishCount = alarmManagerService.mListenerFinishCount + 1;
                return;
            }
            if (AlarmManagerService.this.mBroadcastRefCount == 0) {
                AlarmManagerService.this.setWakelockWorkSource(alarm.operation, alarm.workSource, alarm.type, alarm.statsTag, alarm.operation == null ? alarm.uid : -1, true);
                if (!AlarmManagerService.this.mWakeLock.isHeld()) {
                    AlarmManagerService.this.mWakeLock.acquire();
                }
                AlarmManagerService.this.mHandler.obtainMessage(4, Integer.valueOf(1)).sendToTarget();
            }
            InFlight inflight = new InFlight(AlarmManagerService.this, alarm.operation, alarm.listener, alarm.workSource, alarm.uid, alarm.packageName, alarm.type, alarm.statsTag, nowELAPSED);
            AlarmManagerService.this.mInFlight.add(inflight);
            alarmManagerService = AlarmManagerService.this;
            alarmManagerService.mBroadcastRefCount++;
            QCNsrmAlarmExtension -get8 = AlarmManagerService.this.qcNsrmExt;
            if (alarm.operation != null) {
                creatorUid = alarm.operation.getCreatorUid();
            } else {
                creatorUid = alarm.uid;
            }
            -get8.addTriggeredUid(creatorUid);
            if (allowWhileIdle) {
                AlarmManagerService.this.mLastAllowWhileIdleDispatch.put(alarm.uid, nowELAPSED);
            }
            BroadcastStats bs = inflight.mBroadcastStats;
            bs.count++;
            if (bs.nesting == 0) {
                bs.nesting = 1;
                bs.startTime = nowELAPSED;
            } else {
                bs.nesting++;
            }
            FilterStats fs = inflight.mFilterStats;
            fs.count++;
            if (fs.nesting == 0) {
                fs.nesting = 1;
                fs.startTime = nowELAPSED;
            } else {
                fs.nesting++;
            }
            if (alarm.type == 2 || alarm.type == 0) {
                bs.numWakeup++;
                fs.numWakeup++;
                if (alarm.workSource == null || alarm.workSource.size() <= 0) {
                    ActivityManager.noteWakeupAlarm(alarm.operation, alarm.uid, alarm.packageName, alarm.statsTag);
                } else {
                    for (int wi = 0; wi < alarm.workSource.size(); wi++) {
                        String wsName = alarm.workSource.getName(wi);
                        PendingIntent pendingIntent = alarm.operation;
                        int i = alarm.workSource.get(wi);
                        if (wsName == null) {
                            wsName = alarm.packageName;
                        }
                        ActivityManager.noteWakeupAlarm(pendingIntent, i, wsName, alarm.statsTag);
                    }
                }
            }
            AlarmManagerService.this.mAlarmMonitor.appWakeupIncrease(alarm);
            if (AlarmManagerService.this.mAlarmWakeupDetection != null) {
                AlarmManagerService.this.mAlarmWakeupDetection.alarmTriggerFrequentDetection(alarm, bs, nowELAPSED, false);
            }
        }
    }

    static final class FilterStats {
        long aggregateTime;
        int count;
        long lastTime;
        final BroadcastStats mBroadcastStats;
        final String mTag;
        int nesting;
        int numWakeup;
        int numWakeupWhenScreenoff;
        int numWhenScreenoff;
        long startTime;

        FilterStats(BroadcastStats broadcastStats, String tag) {
            this.mBroadcastStats = broadcastStats;
            this.mTag = tag;
        }

        public String toString() {
            return "FilterStats{tag=" + this.mTag + ", lastTime=" + this.lastTime + ", aggregateTime=" + this.aggregateTime + ", count=" + this.count + ", numWakeup=" + this.numWakeup + ", startTime=" + this.startTime + ", nesting=" + this.nesting + "}";
        }
    }

    static final class IdleDispatchEntry {
        long argRealtime;
        long elapsedRealtime;
        String op;
        String pkg;
        String tag;
        int uid;

        IdleDispatchEntry() {
        }
    }

    static final class InFlight {
        final int mAlarmType;
        final BroadcastStats mBroadcastStats;
        final FilterStats mFilterStats;
        final IBinder mListener;
        final PendingIntent mPendingIntent;
        final String mTag;
        final int mUid;
        final long mWhenElapsed;
        final WorkSource mWorkSource;

        InFlight(AlarmManagerService service, PendingIntent pendingIntent, IAlarmListener listener, WorkSource workSource, int uid, String alarmPkg, int alarmType, String tag, long nowELAPSED) {
            BroadcastStats -wrap1;
            IBinder iBinder = null;
            this.mPendingIntent = pendingIntent;
            this.mWhenElapsed = nowELAPSED;
            if (listener != null) {
                iBinder = listener.asBinder();
            }
            this.mListener = iBinder;
            this.mWorkSource = workSource;
            this.mUid = uid;
            this.mTag = tag;
            if (pendingIntent != null) {
                -wrap1 = service.getStatsLocked(pendingIntent);
            } else {
                -wrap1 = service.getStatsLocked(uid, alarmPkg);
            }
            this.mBroadcastStats = -wrap1;
            FilterStats fs = (FilterStats) this.mBroadcastStats.filterStats.get(this.mTag);
            if (fs == null) {
                fs = new FilterStats(this.mBroadcastStats, this.mTag);
                this.mBroadcastStats.filterStats.put(this.mTag, fs);
            }
            fs.lastTime = nowELAPSED;
            this.mFilterStats = fs;
            this.mAlarmType = alarmType;
        }

        public String toString() {
            return "InFlight{pendingIntent=" + this.mPendingIntent + ", when=" + this.mWhenElapsed + ", workSource=" + this.mWorkSource + ", uid=" + this.mUid + ", tag=" + this.mTag + ", broadcastStats=" + this.mBroadcastStats + ", filterStats=" + this.mFilterStats + ", alarmType=" + this.mAlarmType + "}";
        }
    }

    public static class IncreasingTimeOrder implements Comparator<Alarm> {
        public int compare(Alarm a1, Alarm a2) {
            long when1 = a1.whenElapsed;
            long when2 = a2.whenElapsed;
            if (when1 > when2) {
                return 1;
            }
            if (when1 < when2) {
                return -1;
            }
            return 0;
        }
    }

    class InteractiveStateReceiver extends BroadcastReceiver {
        public InteractiveStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_OFF");
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.setPriority(1000);
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.interactiveStateChangedLocked("android.intent.action.SCREEN_ON".equals(intent.getAction()));
            }
        }
    }

    public final class LocalService {
        public void setDeviceIdleUserWhitelist(int[] appids) {
            AlarmManagerService.this.setDeviceIdleUserWhitelistImpl(appids);
        }

        public int getFrameworkAlarmWakeupTimes() {
            return AlarmManagerService.this.mAlarmMonitor.getFrameworkAlarmWakeupTimes();
        }

        public List getAppsAlarmWakeupTimes() {
            return AlarmManagerService.this.mAlarmMonitor.getAppsAlarmWakeupTimes();
        }
    }

    final class PriorityClass {
        int priority = 2;
        int seq;

        PriorityClass() {
            this.seq = AlarmManagerService.this.mCurrentSeq - 1;
        }
    }

    final class UidObserver extends IUidObserver.Stub {
        UidObserver() {
        }

        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
        }

        public void onUidGone(int uid, boolean disabled) {
            if (disabled) {
                synchronized (AlarmManagerService.this.mLock) {
                    AlarmManagerService.this.removeForStoppedLocked(uid);
                }
            }
        }

        public void onUidActive(int uid) {
        }

        public void onUidIdle(int uid, boolean disabled) {
            if (disabled) {
                synchronized (AlarmManagerService.this.mLock) {
                    AlarmManagerService.this.removeForStoppedLocked(uid);
                }
            }
        }

        public void onUidCachedChanged(int uid, boolean cached) {
        }
    }

    class UninstallReceiver extends BroadcastReceiver {
        public UninstallReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_RESTARTED");
            filter.addAction("android.intent.action.QUERY_PACKAGE_RESTART");
            filter.addDataScheme("package");
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
            sdFilter.addAction("android.intent.action.USER_STOPPED");
            sdFilter.addAction("android.intent.action.UID_REMOVED");
            AlarmManagerService.this.getContext().registerReceiver(this, sdFilter);
        }

        /* JADX WARNING: Missing block: B:64:0x0108, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            synchronized (AlarmManagerService.this.mLock) {
                String action = intent.getAction();
                String[] pkgList = null;
                if ("android.intent.action.QUERY_PACKAGE_RESTART".equals(action)) {
                    for (String packageName : intent.getStringArrayExtra("android.intent.extra.PACKAGES")) {
                        if (AlarmManagerService.this.lookForPackageLocked(packageName)) {
                            setResultCode(-1);
                            return;
                        }
                    }
                    return;
                }
                String pkg;
                if ("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE".equals(action)) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
                } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                    int userHandle = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    if (userHandle >= 0) {
                        AlarmManagerService.this.removeUserLocked(userHandle);
                    }
                } else if ("android.intent.action.UID_REMOVED".equals(action)) {
                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                    if (uid >= 0) {
                        AlarmManagerService.this.mLastAllowWhileIdleDispatch.delete(uid);
                    }
                } else {
                    if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                        if (intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                            return;
                        }
                    }
                    Uri data = intent.getData();
                    if (data != null) {
                        pkg = data.getSchemeSpecificPart();
                        boolean isPkgRestartAction = "android.intent.action.PACKAGE_RESTARTED".equals(action);
                        if (pkg != null && (!isPkgRestartAction || (isPkgRestartAction && (OppoAlarmManagerHelper.isFilterRemovePackage(pkg) ^ 1) != 0))) {
                            pkgList = new String[]{pkg};
                        }
                    }
                }
                if (pkgList != null && pkgList.length > 0) {
                    for (String pkg2 : pkgList) {
                        AlarmManagerService.this.removeLocked(pkg2);
                        AlarmManagerService.this.mPriorities.remove(pkg2);
                        for (int i = AlarmManagerService.this.mBroadcastStats.size() - 1; i >= 0; i--) {
                            ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) AlarmManagerService.this.mBroadcastStats.valueAt(i);
                            if (uidStats.remove(pkg2) != null && uidStats.size() <= 0) {
                                AlarmManagerService.this.mBroadcastStats.removeAt(i);
                            }
                        }
                    }
                }
            }
        }
    }

    static final class WakeupEvent {
        public String action;
        public int uid;
        public long when;

        public WakeupEvent(long theTime, int theUid, String theAction) {
            this.when = theTime;
            this.uid = theUid;
            this.action = theAction;
        }
    }

    private native void close(long j);

    private native long init();

    private native void set(long j, int i, long j2, long j3);

    private native int setKernelTime(long j, long j2);

    private native int setKernelTimezone(long j, int i);

    private native int waitForAlarm(long j);

    void calculateDeliveryPriorities(ArrayList<Alarm> alarms) {
        int N = alarms.size();
        for (int i = 0; i < N; i++) {
            int alarmPrio;
            String alarmPackage;
            Alarm a = (Alarm) alarms.get(i);
            if (a.operation != null && "android.intent.action.TIME_TICK".equals(a.operation.getIntent().getAction())) {
                alarmPrio = 0;
            } else if (a.wakeup) {
                alarmPrio = 1;
            } else {
                alarmPrio = 2;
            }
            PriorityClass packagePrio = a.priorityClass;
            if (a.operation != null) {
                alarmPackage = a.operation.getCreatorPackage();
            } else {
                alarmPackage = a.packageName;
            }
            if (packagePrio == null) {
                packagePrio = (PriorityClass) this.mPriorities.get(alarmPackage);
            }
            if (packagePrio == null) {
                packagePrio = new PriorityClass();
                a.priorityClass = packagePrio;
                this.mPriorities.put(alarmPackage, packagePrio);
            }
            a.priorityClass = packagePrio;
            if (packagePrio.seq != this.mCurrentSeq) {
                packagePrio.priority = alarmPrio;
                packagePrio.seq = this.mCurrentSeq;
            } else if (alarmPrio < packagePrio.priority) {
                packagePrio.priority = alarmPrio;
            }
        }
    }

    public AlarmManagerService(Context context) {
        super(context);
        OppoAlarmManagerHelper.init(context, this);
        this.mOppoGuardElfFeature = context.getPackageManager().hasSystemFeature("oppo.guard.elf.support");
        boolean hasAlignFeature = context.getPackageManager().hasSystemFeature("oppo.Align.alarm.support");
        if (hasAlignFeature || this.mOppoGuardElfFeature) {
            HandlerThread hd = new HandlerThread("OppoAlarmHandler");
            hd.start();
            if (this.mOppoGuardElfFeature) {
                this.mAlarmWakeupDetection = new OppoAlarmWakeupDetection(context, this.mLock, this, this.mBroadcastStats, hd.getLooper());
            }
            if (hasAlignFeature) {
                this.mOppoAlarmAlignment = new OppoAlarmAlignment(context, this.mLock, this, hd.getLooper());
            }
        }
        Slog.d(TAG, "mOppoGuardElfFeature: " + this.mOppoGuardElfFeature + ", hasAlignFeature:" + hasAlignFeature);
        this.mConstants = new Constants(this.mHandler);
        OppoBPMHelper.setAlarmService(this);
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        this.mAlarmUpdateHelper = new AlarmUpdateHelper(this, context);
        this.mAlarmMonitor = new AlarmMonitor();
    }

    static long convertToElapsed(long when, int type) {
        boolean isRtc = type == 1 || type == 0;
        if (isRtc) {
            return when - (System.currentTimeMillis() - SystemClock.elapsedRealtime());
        }
        return when;
    }

    static long maxTriggerTime(long now, long triggerAtTime, long interval) {
        long futurity;
        if (interval == 0) {
            futurity = triggerAtTime - now;
        } else {
            futurity = interval;
        }
        if (futurity < 10000) {
            futurity = 0;
        }
        return ((long) (((double) futurity) * 0.75d)) + triggerAtTime;
    }

    static boolean addBatchLocked(ArrayList<Batch> list, Batch newBatch) {
        int index = Collections.binarySearch(list, newBatch, sBatchOrder);
        if (index < 0) {
            index = (0 - index) - 1;
        }
        list.add(index, newBatch);
        if (index == 0) {
            return true;
        }
        return false;
    }

    int attemptCoalesceLocked(long whenElapsed, long maxWhen) {
        int N = this.mAlarmBatches.size();
        for (int i = 0; i < N; i++) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            if ((b.flags & 1) == 0 && b.canHold(whenElapsed, maxWhen)) {
                return i;
            }
        }
        return -1;
    }

    void rebatchAllAlarms() {
        synchronized (this.mLock) {
            rebatchAllAlarmsLocked(true);
        }
    }

    void rebatchAllAlarmsLocked(boolean doValidate) {
        ArrayList<Batch> oldSet = (ArrayList) this.mAlarmBatches.clone();
        this.mAlarmBatches.clear();
        Alarm oldPendingIdleUntil = this.mPendingIdleUntil;
        long nowElapsed = SystemClock.elapsedRealtime();
        int oldBatches = oldSet.size();
        for (int batchNum = 0; batchNum < oldBatches; batchNum++) {
            Batch batch = (Batch) oldSet.get(batchNum);
            int N = batch.size();
            for (int i = 0; i < N; i++) {
                reAddAlarmLocked(batch.get(i), nowElapsed, doValidate);
            }
        }
        if (!(oldPendingIdleUntil == null || oldPendingIdleUntil == this.mPendingIdleUntil)) {
            Slog.wtf(TAG, "Rebatching: idle until changed from " + oldPendingIdleUntil + " to " + this.mPendingIdleUntil);
            if (this.mPendingIdleUntil == null) {
                restorePendingWhileIdleAlarmsLocked();
            }
        }
        rescheduleKernelAlarmsLocked();
        updateNextAlarmClockLocked();
    }

    void reAddAlarmLocked(Alarm a, long nowElapsed, boolean doValidate) {
        long maxElapsed;
        a.when = a.origWhen;
        long whenElapsed = convertToElapsed(a.when, a.type);
        if (a.windowLength == 0) {
            maxElapsed = whenElapsed;
        } else if (a.windowLength > 0) {
            maxElapsed = whenElapsed + a.windowLength;
        } else {
            maxElapsed = maxTriggerTime(nowElapsed, whenElapsed, a.repeatInterval);
        }
        a.whenElapsed = whenElapsed;
        a.maxWhenElapsed = maxElapsed;
        setImplLocked(a, true, doValidate);
    }

    void restorePendingWhileIdleAlarmsLocked() {
        if (this.mPendingWhileIdleAlarms.size() > 0) {
            ArrayList<Alarm> alarms = this.mPendingWhileIdleAlarms;
            this.mPendingWhileIdleAlarms = new ArrayList();
            long nowElapsed = SystemClock.elapsedRealtime();
            for (int i = alarms.size() - 1; i >= 0; i--) {
                Alarm a = (Alarm) alarms.get(i);
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "restore Alarm { type " + a.type + " when " + a.when + " pid " + a.callingPid + " }");
                }
                reAddAlarmLocked(a, nowElapsed, false);
            }
        }
        this.mConstants.updateAllowWhileIdleMinTimeLocked();
        rescheduleKernelAlarmsLocked();
        updateNextAlarmClockLocked();
        try {
            this.mTimeTickSender.send();
        } catch (CanceledException e) {
        }
    }

    public void onStart() {
        this.mNativeData = init();
        this.mNextNonWakeup = 0;
        this.mNextWakeup = 0;
        setTimeZoneImpl(SystemProperties.get(TIMEZONE_PROPERTY));
        if (this.mNativeData != 0) {
            long systemBuildTime = Environment.getRootDirectory().lastModified();
            if (System.currentTimeMillis() < systemBuildTime) {
                Slog.i(TAG, "Current time only " + System.currentTimeMillis() + ", advancing to build time " + systemBuildTime);
                setKernelTime(this.mNativeData, systemBuildTime);
            }
        }
        PackageManager packMan = getContext().getPackageManager();
        try {
            ApplicationInfo sysUi = packMan.getApplicationInfo(packMan.getPermissionInfo(SYSTEM_UI_SELF_PERMISSION, 0).packageName, 0);
            if ((sysUi.privateFlags & 8) != 0) {
                this.mSystemUiUid = sysUi.uid;
            } else {
                Slog.e(TAG, "SysUI permission android.permission.systemui.IDENTITY defined by non-privileged app " + sysUi.packageName + " - ignoring");
            }
        } catch (NameNotFoundException e) {
        }
        if (this.mSystemUiUid <= 0) {
            Slog.wtf(TAG, "SysUI package not found!");
        }
        this.mWakeLock = ((PowerManager) getContext().getSystemService("power")).newWakeLock(1, "*alarm*");
        this.mTimeTickSender = PendingIntent.getBroadcastAsUser(getContext(), 0, new Intent("android.intent.action.TIME_TICK").addFlags(1344274432), 0, UserHandle.ALL);
        Intent intent = new Intent("android.intent.action.DATE_CHANGED");
        intent.addFlags(538968064);
        this.mDateChangeSender = PendingIntent.getBroadcastAsUser(getContext(), 0, intent, 67108864, UserHandle.ALL);
        this.mClockReceiver = new ClockReceiver();
        this.mClockReceiver.scheduleTimeTickEvent();
        this.mClockReceiver.scheduleDateChangedEvent();
        this.mInteractiveStateReceiver = new InteractiveStateReceiver();
        this.mUninstallReceiver = new UninstallReceiver();
        if (this.mNativeData != 0) {
            new AlarmThread().start();
        } else {
            Slog.w(TAG, "Failed to open alarm driver. Falling back to a handler.");
        }
        try {
            ActivityManager.getService().registerUidObserver(new UidObserver(), 4, -1, null);
        } catch (RemoteException e2) {
        }
        publishBinderService("alarm", this.mService);
        publishLocalService(LocalService.class, new LocalService());
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mConstants.start(getContext().getContentResolver());
            this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
            this.mLocalDeviceIdleController = (com.android.server.DeviceIdleController.LocalService) LocalServices.getService(com.android.server.DeviceIdleController.LocalService.class);
            this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
            this.mPowerManagerInternal.registerColorOsLowPowerModeObserver(new LowPowerModeListener() {
                public int getServiceType() {
                    return 0;
                }

                public void onLowPowerModeChanged(PowerSaveState state) {
                    AlarmManagerService.this.onLowPowerModeChangedInternal(state.batterySaverEnabled);
                }
            });
            this.mColorOsLowPowerModeEnabled.set(this.mPowerManagerInternal.getColorOsLowPowerModeEnabled());
            OppoAppSwitchManager.getInstance().setActivityChangedListener(this.mActivityChangedListener);
        }
    }

    protected void finalize() throws Throwable {
        try {
            close(this.mNativeData);
        } finally {
            super.finalize();
        }
    }

    void setTimeZoneImpl(String tz) {
        if (!TextUtils.isEmpty(tz)) {
            TimeZone zone = TimeZone.getTimeZone(tz);
            boolean timeZoneWasChanged = false;
            synchronized (this) {
                String current = SystemProperties.get(TIMEZONE_PROPERTY);
                if (current == null || (current.equals(zone.getID()) ^ 1) != 0) {
                    timeZoneWasChanged = true;
                    SystemProperties.set(TIMEZONE_PROPERTY, zone.getID());
                }
                setKernelTimezone(this.mNativeData, -(zone.getOffset(System.currentTimeMillis()) / OppoBrightUtils.SPECIAL_AMBIENT_LIGHT_HORIZON));
            }
            TimeZone.setDefault(null);
            if (timeZoneWasChanged) {
                Intent intent = new Intent("android.intent.action.TIMEZONE_CHANGED");
                intent.addFlags(555745280);
                intent.putExtra("time-zone", zone.getID());
                getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        }
    }

    void removeImpl(PendingIntent operation) {
        if (operation != null) {
            synchronized (this.mLock) {
                removeLocked(operation, null);
            }
        }
    }

    void setImpl(int type, long triggerAtTime, long windowLength, long interval, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, int flags, WorkSource workSource, AlarmClockInfo alarmClock, int callingUid, String callingPackage) {
        if (!(operation == null && directReceiver == null) && (operation == null || directReceiver == null)) {
            PackageManager packageManager = getContext().getPackageManager();
            if (!(packageManager == null || (packageManager.isClosedSuperFirewall() ^ 1) == 0)) {
                windowLength = OppoAlarmManagerHelper.setInexactAlarm(windowLength);
                if (windowLength != 0) {
                    flags &= -2;
                }
            }
            if (callingPackage != null) {
                if ((flags & 2) != 0) {
                    if (!isSystemApp(callingUid, callingPackage)) {
                        flags = (flags & -3) | 8;
                    }
                } else if (OppoAlarmManagerHelper.inPackageNameWhiteList(callingPackage) && (flags & 24) == 0) {
                    flags = (flags & -5) | 8;
                }
            }
            if (windowLength > 43200000) {
                Slog.w(TAG, "Window length " + windowLength + "ms suspiciously long; limiting to 1 hour");
                windowLength = 3600000;
            }
            long minInterval = this.mConstants.MIN_INTERVAL;
            if (interval > 0 && interval < minInterval) {
                Slog.w(TAG, "Suspiciously short interval " + interval + " millis; expanding to " + (minInterval / 1000) + " seconds");
                interval = minInterval;
            }
            if (type < 0 || type > 3) {
                throw new IllegalArgumentException("Invalid alarm type " + type);
            }
            long maxElapsed;
            if (triggerAtTime < 0) {
                Slog.w(TAG, "Invalid alarm trigger time! " + triggerAtTime + " from uid=" + callingUid + " pid=" + ((long) Binder.getCallingPid()));
                triggerAtTime = 0;
            }
            long nowElapsed = SystemClock.elapsedRealtime();
            long nominalTrigger = convertToElapsed(triggerAtTime, type);
            long minTrigger = nowElapsed + this.mConstants.MIN_FUTURITY;
            long triggerElapsed = nominalTrigger > minTrigger ? nominalTrigger : minTrigger;
            if (windowLength == 0) {
                maxElapsed = triggerElapsed;
            } else if (windowLength < 0) {
                maxElapsed = maxTriggerTime(nowElapsed, triggerElapsed, interval);
                windowLength = maxElapsed - triggerElapsed;
            } else {
                maxElapsed = triggerElapsed + windowLength;
            }
            synchronized (this.mLock) {
                if (DEBUG_DETAIL) {
                    Slog.v(TAG, "set(" + operation + ") : type=" + type + " triggerAtTime=" + triggerAtTime + " win=" + windowLength + " tElapsed=" + triggerElapsed + " maxElapsed=" + maxElapsed + " interval=" + interval + " flags=0x" + Integer.toHexString(flags) + " hasPendingIdle = " + (this.mPendingIdleUntil != null) + " pkg = " + callingPackage);
                }
                setImplLocked(type, triggerAtTime, triggerElapsed, windowLength, maxElapsed, interval, operation, directReceiver, listenerTag, flags, true, workSource, alarmClock, callingUid, callingPackage);
            }
            return;
        }
        Slog.w(TAG, "Alarms must either supply a PendingIntent or an AlarmReceiver");
    }

    private void setImplLocked(int type, long when, long whenElapsed, long windowLength, long maxWhen, long interval, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, int flags, boolean doValidate, WorkSource workSource, AlarmClockInfo alarmClock, int callingUid, String callingPackage) {
        if (this.mAlarmWakeupDetection != null) {
            type = this.mAlarmWakeupDetection.SyncAlarmHandle(type, operation);
        }
        Alarm a = new Alarm(type, when, whenElapsed, windowLength, maxWhen, interval, operation, directReceiver, listenerTag, workSource, flags, alarmClock, callingUid, callingPackage);
        if (1000 != callingUid) {
            try {
                if (ActivityManager.getService().isAppStartModeDisabled(callingUid, callingPackage)) {
                    Slog.w(TAG, "Not setting alarm from " + callingUid + ":" + a + " -- package not allowed to start");
                    return;
                }
            } catch (RemoteException e) {
            }
        }
        removeLocked(operation, directReceiver);
        setImplLocked(a, false, doValidate);
    }

    private void setImplLocked(Alarm a, boolean rebatching, boolean doValidate) {
        if ((a.flags & 16) != 0) {
            long j;
            if (this.mNextWakeFromIdle != null && a.whenElapsed > this.mNextWakeFromIdle.whenElapsed) {
                j = this.mNextWakeFromIdle.whenElapsed;
                a.maxWhenElapsed = j;
                a.whenElapsed = j;
                a.when = j;
            }
            int fuzz = fuzzForDuration(a.whenElapsed - SystemClock.elapsedRealtime());
            if (fuzz > 0) {
                if (this.mRandom == null) {
                    this.mRandom = new Random();
                }
                a.whenElapsed -= (long) this.mRandom.nextInt(fuzz);
                j = a.whenElapsed;
                a.maxWhenElapsed = j;
                a.when = j;
            }
        } else if (this.mPendingIdleUntil != null && (a.flags & 14) == 0) {
            this.mPendingWhileIdleAlarms.add(a);
            if (DEBUG_PANIC) {
                Slog.d(TAG, "PendingWhileIdleAlarms = " + this.mPendingWhileIdleAlarms.size() + ", delay Alarm { type " + a.type + " when " + a.when + " pid " + a.callingPid + " }");
            }
            return;
        }
        if (this.mOppoAlarmAlignment != null) {
            this.mOppoAlarmAlignment.alignWithSys(a);
        }
        int whichBatch = (a.flags & 1) != 0 ? -1 : attemptCoalesceLocked(a.whenElapsed, a.maxWhenElapsed);
        if (whichBatch < 0) {
            addBatchLocked(this.mAlarmBatches, new Batch(a));
        } else {
            Batch batch = (Batch) this.mAlarmBatches.get(whichBatch);
            if (batch.add(a)) {
                this.mAlarmBatches.remove(whichBatch);
                addBatchLocked(this.mAlarmBatches, batch);
            }
        }
        if (a.alarmClock != null) {
            this.mNextAlarmClockMayChange = true;
        }
        boolean needRebatch = false;
        if ((a.flags & 16) != 0) {
            if (!(this.mPendingIdleUntil == a || this.mPendingIdleUntil == null)) {
                Slog.wtfStack(TAG, "setImplLocked: idle until changed from " + this.mPendingIdleUntil + " to " + a);
            }
            this.mPendingIdleUntil = a;
            this.mConstants.updateAllowWhileIdleMinTimeLocked();
            needRebatch = true;
        } else if ((a.flags & 2) != 0 && (this.mNextWakeFromIdle == null || this.mNextWakeFromIdle.whenElapsed > a.whenElapsed)) {
            this.mNextWakeFromIdle = a;
            if (this.mPendingIdleUntil != null) {
                needRebatch = true;
            }
        }
        if (!(this.mOppoAlarmAlignment == null || !this.mOppoAlarmAlignment.mNeedRebatch || (rebatching ^ 1) == 0)) {
            this.mOppoAlarmAlignment.mNeedRebatch = false;
            rebatchAllAlarmsLocked(false);
            needRebatch = false;
        }
        if (!rebatching) {
            if (needRebatch) {
                rebatchAllAlarmsLocked(false);
            }
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    private boolean dynamicallyConfigAlarmManagerServiceLogTag(PrintWriter pw, String[] args) {
        if (args.length > 0) {
            String opt = args[0];
            if (opt != null && opt.length() > 0 && opt.charAt(0) == '-') {
                if ("-h".equals(opt)) {
                    pw.println("alarm manager dump options:");
                    pw.println("  log  [on/off]");
                    pw.println("  Example:");
                    pw.println("  $adb shell dumpsys alarm log on");
                    pw.println("  $adb shell dumpsys alarm log off");
                    return true;
                }
                pw.println("Unknown argument: " + opt + "; use -h for help");
                return true;
            }
        }
        if (args.length > 0) {
            String cmd = args[0];
            if ("log".equals(cmd)) {
                configLogTag(pw, args, 1);
                return true;
            } else if ("whitelist".equals(cmd)) {
                pw.println(this.mAlarmUpdateHelper.dumpToString());
                return true;
            }
        }
        return false;
    }

    protected void configLogTag(PrintWriter pw, String[] args, int opti) {
        if (opti >= args.length) {
            pw.println("  Invalid argument!");
        } else if ("on".equals(args[opti])) {
            DEBUG_PENDING = true;
            DEBUG_PANIC = true;
            DEBUG_DETAIL = true;
        } else if ("off".equals(args[opti])) {
            DEBUG_PANIC = false;
            DEBUG_PENDING = false;
            DEBUG_DETAIL = false;
        } else {
            pw.println("  Invalid argument!");
        }
    }

    void dumpImpl(PrintWriter pw) {
        synchronized (this.mLock) {
            int i;
            int iu;
            ArrayMap<String, BroadcastStats> uidStats;
            int ip;
            BroadcastStats bs;
            int is;
            FilterStats fs;
            pw.println("Current Alarm Manager state:");
            this.mConstants.dump(pw);
            pw.println();
            long nowRTC = System.currentTimeMillis();
            long nowELAPSED = SystemClock.elapsedRealtime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pw.print("  nowRTC=");
            pw.print(nowRTC);
            pw.print("=");
            pw.print(sdf.format(new Date(nowRTC)));
            pw.print(" nowELAPSED=");
            pw.print(nowELAPSED);
            pw.println();
            pw.print("  mLastTimeChangeClockTime=");
            pw.print(this.mLastTimeChangeClockTime);
            pw.print("=");
            pw.println(sdf.format(new Date(this.mLastTimeChangeClockTime)));
            pw.print("  mLastTimeChangeRealtime=");
            TimeUtils.formatDuration(this.mLastTimeChangeRealtime, pw);
            pw.println();
            if (!this.mInteractive) {
                pw.print("  Time since non-interactive: ");
                TimeUtils.formatDuration(nowELAPSED - this.mNonInteractiveStartTime, pw);
                pw.println();
                pw.print("  Max wakeup delay: ");
                TimeUtils.formatDuration(currentNonWakeupFuzzLocked(nowELAPSED), pw);
                pw.println();
                pw.print("  Time since last dispatch: ");
                TimeUtils.formatDuration(nowELAPSED - this.mLastAlarmDeliveryTime, pw);
                pw.println();
                pw.print("  Next non-wakeup delivery time: ");
                TimeUtils.formatDuration(nowELAPSED - this.mNextNonWakeupDeliveryTime, pw);
                pw.println();
            }
            long nextWakeupRTC = this.mNextWakeup + (nowRTC - nowELAPSED);
            long nextNonWakeupRTC = this.mNextNonWakeup + (nowRTC - nowELAPSED);
            pw.print("  Next non-wakeup alarm: ");
            TimeUtils.formatDuration(this.mNextNonWakeup, nowELAPSED, pw);
            pw.print(" = ");
            pw.println(sdf.format(new Date(nextNonWakeupRTC)));
            pw.print("  Next wakeup: ");
            TimeUtils.formatDuration(this.mNextWakeup, nowELAPSED, pw);
            pw.print(" = ");
            pw.println(sdf.format(new Date(nextWakeupRTC)));
            pw.print("  Last wakeup: ");
            TimeUtils.formatDuration(this.mLastWakeup, nowELAPSED, pw);
            pw.print(" set at ");
            TimeUtils.formatDuration(this.mLastWakeupSet, nowELAPSED, pw);
            pw.println();
            pw.print("  Num time change events: ");
            pw.println(this.mNumTimeChanged);
            pw.println("  mDeviceIdleUserWhitelist=" + Arrays.toString(this.mDeviceIdleUserWhitelist));
            pw.println();
            pw.println("  Next alarm clock information: ");
            TreeSet<Integer> users = new TreeSet();
            for (i = 0; i < this.mNextAlarmClockForUser.size(); i++) {
                users.add(Integer.valueOf(this.mNextAlarmClockForUser.keyAt(i)));
            }
            for (i = 0; i < this.mPendingSendNextAlarmClockChangedForUser.size(); i++) {
                users.add(Integer.valueOf(this.mPendingSendNextAlarmClockChangedForUser.keyAt(i)));
            }
            for (Integer intValue : users) {
                int user = intValue.intValue();
                AlarmClockInfo next = (AlarmClockInfo) this.mNextAlarmClockForUser.get(user);
                long time = next != null ? next.getTriggerTime() : 0;
                boolean pendingSend = this.mPendingSendNextAlarmClockChangedForUser.get(user);
                pw.print("    user:");
                pw.print(user);
                pw.print(" pendingSend:");
                pw.print(pendingSend);
                pw.print(" time:");
                pw.print(time);
                if (time > 0) {
                    pw.print(" = ");
                    pw.print(sdf.format(new Date(time)));
                    pw.print(" = ");
                    TimeUtils.formatDuration(time, nowRTC, pw);
                }
                pw.println();
            }
            if (this.mAlarmBatches.size() > 0) {
                pw.println();
                pw.print("  Pending alarm batches: ");
                pw.println(this.mAlarmBatches.size());
                for (Batch b : this.mAlarmBatches) {
                    pw.print(b);
                    pw.println(':');
                    dumpAlarmList(pw, b.alarms, "    ", nowELAPSED, nowRTC, sdf);
                }
            }
            if (this.mPendingIdleUntil != null || this.mPendingWhileIdleAlarms.size() > 0) {
                pw.println();
                pw.println("    Idle mode state:");
                pw.print("      Idling until: ");
                if (this.mPendingIdleUntil != null) {
                    pw.println(this.mPendingIdleUntil);
                    this.mPendingIdleUntil.dump(pw, "        ", nowRTC, nowELAPSED, sdf);
                } else {
                    pw.println("null");
                }
                pw.println("      Pending alarms:");
                dumpAlarmList(pw, this.mPendingWhileIdleAlarms, "      ", nowELAPSED, nowRTC, sdf);
            }
            if (this.mNextWakeFromIdle != null) {
                pw.println();
                pw.print("  Next wake from idle: ");
                pw.println(this.mNextWakeFromIdle);
                this.mNextWakeFromIdle.dump(pw, "    ", nowRTC, nowELAPSED, sdf);
            }
            pw.println();
            pw.print("  Power Save Mode suspended alarms: ");
            if (this.mListPowerSaveSuspend.size() > 0) {
                pw.println(this.mListPowerSaveSuspend.size());
                dumpAlarmList(pw, this.mListPowerSaveSuspend, "    ", nowELAPSED, nowRTC, sdf);
            } else {
                pw.println("(none)");
            }
            pw.print("    isPowerSaveMode: ");
            pw.println(this.mColorOsLowPowerModeEnabled.get());
            pw.print("    top pkg: ");
            pw.println(this.mTopPkg);
            pw.println();
            pw.print("  Past-due non-wakeup alarms: ");
            if (this.mPendingNonWakeupAlarms.size() > 0) {
                pw.println(this.mPendingNonWakeupAlarms.size());
                dumpAlarmList(pw, this.mPendingNonWakeupAlarms, "    ", nowELAPSED, nowRTC, sdf);
            } else {
                pw.println("(none)");
            }
            pw.print("    Number of delayed alarms: ");
            pw.print(this.mNumDelayedAlarms);
            pw.print(", total delay time: ");
            TimeUtils.formatDuration(this.mTotalDelayTime, pw);
            pw.println();
            pw.print("    Max delay time: ");
            TimeUtils.formatDuration(this.mMaxDelayTime, pw);
            pw.print(", max non-interactive time: ");
            TimeUtils.formatDuration(this.mNonInteractiveTime, pw);
            pw.println();
            pw.println();
            pw.print("  Broadcast ref count: ");
            pw.println(this.mBroadcastRefCount);
            pw.print("  PendingIntent send count: ");
            pw.println(this.mSendCount);
            pw.print("  PendingIntent finish count: ");
            pw.println(this.mSendFinishCount);
            pw.print("  Listener send count: ");
            pw.println(this.mListenerCount);
            pw.print("  Listener finish count: ");
            pw.println(this.mListenerFinishCount);
            pw.println();
            if (this.mInFlight.size() > 0) {
                pw.println("Outstanding deliveries:");
                for (i = 0; i < this.mInFlight.size(); i++) {
                    pw.print("   #");
                    pw.print(i);
                    pw.print(": ");
                    pw.println(this.mInFlight.get(i));
                }
                pw.println();
            }
            pw.print("  mAllowWhileIdleMinTime=");
            TimeUtils.formatDuration(this.mAllowWhileIdleMinTime, pw);
            pw.println();
            if (this.mLastAllowWhileIdleDispatch.size() > 0) {
                pw.println("  Last allow while idle dispatch times:");
                for (i = 0; i < this.mLastAllowWhileIdleDispatch.size(); i++) {
                    pw.print("  UID ");
                    UserHandle.formatUid(pw, this.mLastAllowWhileIdleDispatch.keyAt(i));
                    pw.print(": ");
                    TimeUtils.formatDuration(this.mLastAllowWhileIdleDispatch.valueAt(i), nowELAPSED, pw);
                    pw.println();
                }
            }
            pw.println();
            if (this.mLog.dump(pw, "  Recent problems", "    ")) {
                pw.println();
            }
            Object topFilters = new FilterStats[10];
            Comparator<FilterStats> anonymousClass5 = new Comparator<FilterStats>() {
                public int compare(FilterStats lhs, FilterStats rhs) {
                    if (lhs.aggregateTime < rhs.aggregateTime) {
                        return 1;
                    }
                    if (lhs.aggregateTime > rhs.aggregateTime) {
                        return -1;
                    }
                    return 0;
                }
            };
            int len = 0;
            for (iu = 0; iu < this.mBroadcastStats.size(); iu++) {
                uidStats = (ArrayMap) this.mBroadcastStats.valueAt(iu);
                for (ip = 0; ip < uidStats.size(); ip++) {
                    bs = (BroadcastStats) uidStats.valueAt(ip);
                    for (is = 0; is < bs.filterStats.size(); is++) {
                        fs = (FilterStats) bs.filterStats.valueAt(is);
                        int pos = len > 0 ? Arrays.binarySearch(topFilters, 0, len, fs, anonymousClass5) : 0;
                        if (pos < 0) {
                            pos = (-pos) - 1;
                        }
                        if (pos < topFilters.length) {
                            int copylen = (topFilters.length - pos) - 1;
                            if (copylen > 0) {
                                System.arraycopy(topFilters, pos, topFilters, pos + 1, copylen);
                            }
                            topFilters[pos] = fs;
                            if (len < topFilters.length) {
                                len++;
                            }
                        }
                    }
                }
            }
            if (len > 0) {
                pw.println("  Top Alarms:");
                for (i = 0; i < len; i++) {
                    fs = topFilters[i];
                    pw.print("    ");
                    if (fs.nesting > 0) {
                        pw.print("*ACTIVE* ");
                    }
                    TimeUtils.formatDuration(fs.aggregateTime, pw);
                    pw.print(" running, ");
                    pw.print(fs.numWakeup);
                    pw.print(" wakeups, ");
                    pw.print(fs.count);
                    pw.print(" alarms: ");
                    UserHandle.formatUid(pw, fs.mBroadcastStats.mUid);
                    pw.print(":");
                    pw.print(fs.mBroadcastStats.mPackageName);
                    pw.println();
                    pw.print("      ");
                    pw.print(fs.mTag);
                    pw.println();
                }
            }
            pw.println(" ");
            pw.println("  Alarm Stats:");
            ArrayList<FilterStats> tmpFilters = new ArrayList();
            for (iu = 0; iu < this.mBroadcastStats.size(); iu++) {
                uidStats = (ArrayMap) this.mBroadcastStats.valueAt(iu);
                for (ip = 0; ip < uidStats.size(); ip++) {
                    bs = (BroadcastStats) uidStats.valueAt(ip);
                    pw.print("  ");
                    if (bs.nesting > 0) {
                        pw.print("*ACTIVE* ");
                    }
                    UserHandle.formatUid(pw, bs.mUid);
                    pw.print(":");
                    pw.print(bs.mPackageName);
                    pw.print(" ");
                    TimeUtils.formatDuration(bs.aggregateTime, pw);
                    pw.print(" running, ");
                    pw.print(bs.numWakeup);
                    pw.println(" wakeups:");
                    tmpFilters.clear();
                    for (is = 0; is < bs.filterStats.size(); is++) {
                        tmpFilters.add((FilterStats) bs.filterStats.valueAt(is));
                    }
                    Collections.sort(tmpFilters, anonymousClass5);
                    for (i = 0; i < tmpFilters.size(); i++) {
                        fs = (FilterStats) tmpFilters.get(i);
                        pw.print("    ");
                        if (fs.nesting > 0) {
                            pw.print("*ACTIVE* ");
                        }
                        TimeUtils.formatDuration(fs.aggregateTime, pw);
                        pw.print(" ");
                        pw.print(fs.numWakeup);
                        pw.print(" wakes ");
                        pw.print(fs.count);
                        pw.print(" alarms, last ");
                        TimeUtils.formatDuration(fs.lastTime, nowELAPSED, pw);
                        pw.println(":");
                        pw.print("      ");
                        pw.print(fs.mTag);
                        pw.println();
                    }
                }
            }
        }
    }

    private void logBatchesLocked(SimpleDateFormat sdf) {
        ByteArrayOutputStream bs = new ByteArrayOutputStream(2048);
        PrintWriter pw = new PrintWriter(bs);
        long nowRTC = System.currentTimeMillis();
        long nowELAPSED = SystemClock.elapsedRealtime();
        int NZ = this.mAlarmBatches.size();
        for (int iz = 0; iz < NZ; iz++) {
            Batch bz = (Batch) this.mAlarmBatches.get(iz);
            pw.append("Batch ");
            pw.print(iz);
            pw.append(": ");
            pw.println(bz);
            dumpAlarmList(pw, bz.alarms, "  ", nowELAPSED, nowRTC, sdf);
            pw.flush();
            Slog.v(TAG, bs.toString());
            bs.reset();
        }
    }

    private boolean validateConsistencyLocked() {
        return true;
    }

    private Batch findFirstWakeupBatchLocked() {
        int N = this.mAlarmBatches.size();
        for (int i = 0; i < N; i++) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            if (b.hasWakeups()) {
                return b;
            }
        }
        return null;
    }

    long getNextWakeFromIdleTimeImpl() {
        long j;
        synchronized (this.mLock) {
            j = this.mNextWakeFromIdle != null ? this.mNextWakeFromIdle.whenElapsed : JobStatus.NO_LATEST_RUNTIME;
        }
        return j;
    }

    void setDeviceIdleUserWhitelistImpl(int[] appids) {
        synchronized (this.mLock) {
            this.mDeviceIdleUserWhitelist = appids;
        }
    }

    AlarmClockInfo getNextAlarmClockImpl(int userId) {
        AlarmClockInfo alarmClockInfo;
        synchronized (this.mLock) {
            alarmClockInfo = (AlarmClockInfo) this.mNextAlarmClockForUser.get(userId);
        }
        return alarmClockInfo;
    }

    private void updateNextAlarmClockLocked() {
        if (this.mNextAlarmClockMayChange) {
            int i;
            int userId;
            this.mNextAlarmClockMayChange = false;
            SparseArray<AlarmClockInfo> nextForUser = this.mTmpSparseAlarmClockArray;
            nextForUser.clear();
            int N = this.mAlarmBatches.size();
            for (i = 0; i < N; i++) {
                ArrayList<Alarm> alarms = ((Batch) this.mAlarmBatches.get(i)).alarms;
                int M = alarms.size();
                for (int j = 0; j < M; j++) {
                    Alarm a = (Alarm) alarms.get(j);
                    if (a.alarmClock != null) {
                        userId = UserHandle.getUserId(a.uid);
                        AlarmClockInfo current = (AlarmClockInfo) this.mNextAlarmClockForUser.get(userId);
                        if (nextForUser.get(userId) == null) {
                            nextForUser.put(userId, a.alarmClock);
                        } else if (a.alarmClock.equals(current) && current.getTriggerTime() <= ((AlarmClockInfo) nextForUser.get(userId)).getTriggerTime()) {
                            nextForUser.put(userId, current);
                        }
                    }
                }
            }
            int NN = nextForUser.size();
            for (i = 0; i < NN; i++) {
                AlarmClockInfo newAlarm = (AlarmClockInfo) nextForUser.valueAt(i);
                userId = nextForUser.keyAt(i);
                if (!newAlarm.equals((AlarmClockInfo) this.mNextAlarmClockForUser.get(userId))) {
                    updateNextAlarmInfoForUserLocked(userId, newAlarm);
                }
            }
            for (i = this.mNextAlarmClockForUser.size() - 1; i >= 0; i--) {
                userId = this.mNextAlarmClockForUser.keyAt(i);
                if (nextForUser.get(userId) == null) {
                    updateNextAlarmInfoForUserLocked(userId, null);
                }
            }
        }
    }

    private void updateNextAlarmInfoForUserLocked(int userId, AlarmClockInfo alarmClock) {
        if (alarmClock != null) {
            this.mNextAlarmClockForUser.put(userId, alarmClock);
        } else {
            this.mNextAlarmClockForUser.remove(userId);
        }
        this.mPendingSendNextAlarmClockChangedForUser.put(userId, true);
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessage(2);
    }

    private void sendNextAlarmClockChanged() {
        int N;
        int i;
        int userId;
        SparseArray<AlarmClockInfo> pendingUsers = this.mHandlerSparseAlarmClockArray;
        pendingUsers.clear();
        synchronized (this.mLock) {
            N = this.mPendingSendNextAlarmClockChangedForUser.size();
            for (i = 0; i < N; i++) {
                userId = this.mPendingSendNextAlarmClockChangedForUser.keyAt(i);
                pendingUsers.append(userId, (AlarmClockInfo) this.mNextAlarmClockForUser.get(userId));
            }
            this.mPendingSendNextAlarmClockChangedForUser.clear();
        }
        N = pendingUsers.size();
        for (i = 0; i < N; i++) {
            userId = pendingUsers.keyAt(i);
            System.putStringForUser(getContext().getContentResolver(), "next_alarm_formatted", formatNextAlarm(getContext(), (AlarmClockInfo) pendingUsers.valueAt(i), userId), userId);
            getContext().sendBroadcastAsUser(NEXT_ALARM_CLOCK_CHANGED_INTENT, new UserHandle(userId));
        }
    }

    private static String formatNextAlarm(Context context, AlarmClockInfo info, int userId) {
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context, userId) ? "EHm" : "Ehma");
        if (info == null) {
            return "";
        }
        return DateFormat.format(pattern, info.getTriggerTime()).toString();
    }

    void rescheduleKernelAlarmsLocked() {
        long nextNonWakeup = 0;
        if (this.mAlarmBatches.size() > 0) {
            Alarm a;
            Batch firstWakeup = findFirstWakeupBatchLocked();
            Batch firstBatch = (Batch) this.mAlarmBatches.get(0);
            if (!(firstWakeup == null || this.mNextWakeup == firstWakeup.start)) {
                this.mNextWakeup = firstWakeup.start;
                this.mLastWakeupSet = SystemClock.elapsedRealtime();
                setLocked(2, firstWakeup.start);
                if (DEBUG_PANIC) {
                    a = firstWakeup.get(0);
                    Slog.v(TAG, "setlocked ELAPSED_REALTIME_WAKEUP pkg " + a.packageName + ", when " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((firstWakeup.start + System.currentTimeMillis()) - SystemClock.elapsedRealtime())));
                }
            }
            if (firstBatch != firstWakeup) {
                nextNonWakeup = firstBatch.start;
                if (!(!DEBUG_PANIC || nextNonWakeup == 0 || this.mNextNonWakeup == nextNonWakeup)) {
                    a = firstBatch.get(0);
                    Slog.v(TAG, "setlocked ELAPSED_REALTIME pkg " + a.packageName + ", when " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((firstBatch.start + System.currentTimeMillis()) - SystemClock.elapsedRealtime())));
                }
            }
        }
        if ((this.mPendingNonWakeupAlarms.size() > 0 || this.mPendingImportantNonWakeupAlarms.size() > 0) && (nextNonWakeup == 0 || this.mNextNonWakeupDeliveryTime < nextNonWakeup)) {
            nextNonWakeup = this.mNextNonWakeupDeliveryTime;
        }
        if (nextNonWakeup != 0 && this.mNextNonWakeup != nextNonWakeup) {
            this.mNextNonWakeup = nextNonWakeup;
            setLocked(3, nextNonWakeup);
        }
    }

    private void removeLocked(PendingIntent operation, IAlarmListener directReceiver) {
        int i;
        int didRemove = 0;
        for (i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            didRemove |= b.remove(operation, directReceiver);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (i = this.mPendingWhileIdleAlarms.size() - 1; i >= 0; i--) {
            if (((Alarm) this.mPendingWhileIdleAlarms.get(i)).matches(operation, directReceiver)) {
                this.mPendingWhileIdleAlarms.remove(i);
            }
        }
        for (i = this.mListPowerSaveSuspend.size() - 1; i >= 0; i--) {
            if (((Alarm) this.mListPowerSaveSuspend.get(i)).matches(operation, directReceiver)) {
                this.mListPowerSaveSuspend.remove(i);
            }
        }
        if (didRemove != 0) {
            boolean restorePending = false;
            if (this.mPendingIdleUntil != null && this.mPendingIdleUntil.matches(operation, directReceiver)) {
                this.mPendingIdleUntil = null;
                restorePending = true;
            }
            if (this.mNextWakeFromIdle != null && this.mNextWakeFromIdle.matches(operation, directReceiver)) {
                this.mNextWakeFromIdle = null;
            }
            rebatchAllAlarmsLocked(true);
            if (restorePending) {
                restorePendingWhileIdleAlarmsLocked();
            }
            updateNextAlarmClockLocked();
        }
    }

    void removeLocked(String packageName) {
        int i;
        int didRemove = 0;
        for (i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            didRemove |= b.remove(packageName);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (i = this.mPendingWhileIdleAlarms.size() - 1; i >= 0; i--) {
            if (((Alarm) this.mPendingWhileIdleAlarms.get(i)).matches(packageName)) {
                this.mPendingWhileIdleAlarms.remove(i);
            }
        }
        for (i = this.mListPowerSaveSuspend.size() - 1; i >= 0; i--) {
            if (((Alarm) this.mListPowerSaveSuspend.get(i)).matches(packageName)) {
                this.mListPowerSaveSuspend.remove(i);
            }
        }
        if (didRemove != 0) {
            rebatchAllAlarmsLocked(true);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    void removeForStoppedLocked(int uid) {
        int i;
        int didRemove = 0;
        for (i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            didRemove |= b.removeForStopped(uid);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (i = this.mPendingWhileIdleAlarms.size() - 1; i >= 0; i--) {
            if (((Alarm) this.mPendingWhileIdleAlarms.get(i)).uid == uid) {
                this.mPendingWhileIdleAlarms.remove(i);
            }
        }
        if (didRemove != 0) {
            rebatchAllAlarmsLocked(true);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    void removeUserLocked(int userHandle) {
        int i;
        int didRemove = 0;
        for (i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            didRemove |= b.remove(userHandle);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        for (i = this.mPendingWhileIdleAlarms.size() - 1; i >= 0; i--) {
            if (UserHandle.getUserId(((Alarm) this.mPendingWhileIdleAlarms.get(i)).creatorUid) == userHandle) {
                this.mPendingWhileIdleAlarms.remove(i);
            }
        }
        for (i = this.mLastAllowWhileIdleDispatch.size() - 1; i >= 0; i--) {
            if (UserHandle.getUserId(this.mLastAllowWhileIdleDispatch.keyAt(i)) == userHandle) {
                this.mLastAllowWhileIdleDispatch.removeAt(i);
            }
        }
        if (didRemove != 0) {
            rebatchAllAlarmsLocked(true);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    void interactiveStateChangedLocked(boolean interactive) {
        if (this.mInteractive != interactive) {
            this.mInteractive = interactive;
            long nowELAPSED = SystemClock.elapsedRealtime();
            if (interactive) {
                this.mPendingNonWakeupAlarms.addAll(this.mPendingImportantNonWakeupAlarms);
                this.mPendingImportantNonWakeupAlarms.clear();
                boolean hasTimeTick = false;
                if (nowELAPSED - this.mNonInteractiveStartTime > 60000) {
                    for (Alarm a : this.mPendingNonWakeupAlarms) {
                        if (a.operation != null && "android.intent.action.TIME_TICK".equals(a.operation.getIntent().getAction())) {
                            hasTimeTick = true;
                            break;
                        }
                    }
                } else {
                    hasTimeTick = true;
                }
                if (!hasTimeTick) {
                    if (DEBUG_PANIC) {
                        Slog.d(TAG, "Sechdule a TIME_TICK");
                    }
                    this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            try {
                                if (AlarmManagerService.this.mTimeTickSender != null) {
                                    AlarmManagerService.this.mTimeTickSender.send();
                                }
                            } catch (CanceledException e) {
                            }
                        }
                    }, 150);
                }
                if (this.mPendingNonWakeupAlarms.size() > 0) {
                    long thisDelayTime = nowELAPSED - this.mStartCurrentDelayTime;
                    this.mTotalDelayTime += thisDelayTime;
                    if (this.mMaxDelayTime < thisDelayTime) {
                        this.mMaxDelayTime = thisDelayTime;
                    }
                    deliverAlarmsLocked(this.mPendingNonWakeupAlarms, nowELAPSED);
                    if (DEBUG_PENDING) {
                        Slog.d(TAG, "mPendingNonWakeupAlarms = " + this.mPendingNonWakeupAlarms);
                    }
                    clearPendingNonWakeupAlarmLocked(nowELAPSED, this.mPendingNonWakeupAlarms);
                }
                if (this.mNonInteractiveStartTime > 0) {
                    long dur = nowELAPSED - this.mNonInteractiveStartTime;
                    if (dur > this.mNonInteractiveTime) {
                        this.mNonInteractiveTime = dur;
                    }
                }
                if (this.mOppoAlarmAlignment != null) {
                    this.mOppoAlarmAlignment.onScreenOn();
                }
            } else {
                this.mNonInteractiveStartTime = nowELAPSED;
                this.mAlarmMonitor.clear();
                this.mDelayAlarmMode = this.mAlarmUpdateHelper.getMode();
                if (this.mOppoAlarmAlignment != null) {
                    this.mOppoAlarmAlignment.onScreenOff();
                }
            }
            this.mAlarmUpdateHelper.interactiveStateChangedLocked(interactive, nowELAPSED);
        }
    }

    void netStateChangedLocked(boolean b) {
        if (this.mNetstateInteractive != b) {
            this.mNetstateInteractive = b;
            if (b) {
                long nowELAPSED = SystemClock.elapsedRealtime();
                this.mPendingNonWakeupAlarms.addAll(this.mPendingImportantNonWakeupAlarms);
                this.mPendingImportantNonWakeupAlarms.clear();
                if (this.mPendingNonWakeupAlarms.size() > 0) {
                    deliverAlarmsLocked(this.mPendingNonWakeupAlarms, nowELAPSED);
                    if (DEBUG_PANIC) {
                        Slog.v(TAG, "pendingAlarms = " + this.mPendingNonWakeupAlarms);
                    }
                    clearPendingNonWakeupAlarmLocked(nowELAPSED, this.mPendingNonWakeupAlarms);
                }
            }
        }
    }

    void clearPendingNonWakeupAlarmLocked(long nowELAPSED, ArrayList<Alarm> pendingAlarms) {
        while (pendingAlarms.size() > 0) {
            Alarm alarm = (Alarm) pendingAlarms.get(0);
            if (alarm.repeatInterval > 0) {
                alarm.count = (int) (((long) alarm.count) + ((nowELAPSED - alarm.whenElapsed) / alarm.repeatInterval));
                long delta = ((long) alarm.count) * alarm.repeatInterval;
                long nextElapsed = alarm.whenElapsed + delta;
                int i = alarm.type;
                long j = nextElapsed;
                setImplLocked(i, alarm.when + delta, j, alarm.windowLength, maxTriggerTime(nowELAPSED, nextElapsed, alarm.repeatInterval), alarm.repeatInterval, alarm.operation, null, null, alarm.flags, true, alarm.workSource, alarm.alarmClock, alarm.uid, alarm.packageName);
            }
            pendingAlarms.remove(0);
        }
        pendingAlarms.clear();
    }

    boolean lookForPackageLocked(String packageName) {
        int i;
        for (i = 0; i < this.mAlarmBatches.size(); i++) {
            if (((Batch) this.mAlarmBatches.get(i)).hasPackage(packageName)) {
                return true;
            }
        }
        for (i = 0; i < this.mPendingWhileIdleAlarms.size(); i++) {
            if (((Alarm) this.mPendingWhileIdleAlarms.get(i)).matches(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void setLocked(int type, long when) {
        if (this.mNativeData != 0) {
            long alarmSeconds;
            long alarmNanoseconds;
            if (when < 0) {
                alarmSeconds = 0;
                alarmNanoseconds = 0;
            } else {
                alarmSeconds = when / 1000;
                alarmNanoseconds = ((when % 1000) * 1000) * 1000;
            }
            set(this.mNativeData, type, alarmSeconds, alarmNanoseconds);
            return;
        }
        Message msg = Message.obtain();
        msg.what = 1;
        this.mHandler.removeMessages(1);
        this.mHandler.sendMessageAtTime(msg, when);
    }

    private static final void dumpAlarmList(PrintWriter pw, ArrayList<Alarm> list, String prefix, String label, long nowRTC, long nowELAPSED, SimpleDateFormat sdf) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Alarm a = (Alarm) list.get(i);
            pw.print(prefix);
            pw.print(label);
            pw.print(" #");
            pw.print(i);
            pw.print(": ");
            pw.println(a);
            a.dump(pw, prefix + "  ", nowRTC, nowELAPSED, sdf);
        }
    }

    private static final String labelForType(int type) {
        switch (type) {
            case 0:
                return "RTC_WAKEUP";
            case 1:
                return "RTC";
            case 2:
                return "ELAPSED_WAKEUP";
            case 3:
                return "ELAPSED";
            default:
                return "--unknown--";
        }
    }

    private static final void dumpAlarmList(PrintWriter pw, ArrayList<Alarm> list, String prefix, long nowELAPSED, long nowRTC, SimpleDateFormat sdf) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Alarm a = (Alarm) list.get(i);
            String label = labelForType(a.type);
            pw.print(prefix);
            pw.print(label);
            pw.print(" #");
            pw.print(i);
            pw.print(": ");
            pw.println(a);
            a.dump(pw, prefix + "  ", nowRTC, nowELAPSED, sdf);
        }
    }

    boolean triggerAlarmsOptLocked(ArrayList<Alarm> triggerList, ArrayList<Alarm> triggerListNonWakeup, long nowELAPSED, long nowRTC) {
        boolean hasWakeup = false;
        while (this.mAlarmBatches.size() > 0) {
            Batch batch = (Batch) this.mAlarmBatches.get(0);
            if (batch.start > nowELAPSED) {
                break;
            }
            this.mAlarmBatches.remove(0);
            int N = batch.size();
            for (int i = 0; i < N; i++) {
                Alarm alarm = batch.get(i);
                if ((alarm.flags & 4) != 0) {
                    long lastTime = this.mLastAllowWhileIdleDispatch.get(alarm.uid, 0);
                    long minTime = lastTime + this.mAllowWhileIdleMinTime;
                    if (nowELAPSED < minTime) {
                        if (DEBUG_PANIC) {
                            Slog.d(TAG, alarm + " too frequent, last = " + lastTime + ", now = " + nowELAPSED);
                        }
                        alarm.whenElapsed = minTime;
                        if (alarm.maxWhenElapsed < minTime) {
                            alarm.maxWhenElapsed = minTime;
                        }
                        setImplLocked(alarm, true, false);
                    }
                }
                alarm.count = 1;
                boolean needRepeat;
                if (alarm.wakeup) {
                    hasWakeup = true;
                    triggerList.add(alarm);
                    needRepeat = true;
                } else if (this.mAlarmUpdateHelper.isImportantAlarm(alarm)) {
                    needRepeat = true;
                    triggerList.add(alarm);
                } else {
                    needRepeat = false;
                    triggerListNonWakeup.add(alarm);
                }
                if ((alarm.flags & 2) != 0) {
                    EventLogTags.writeDeviceIdleWakeFromIdle(this.mPendingIdleUntil != null ? 1 : 0, alarm.statsTag);
                }
                if (this.mPendingIdleUntil == alarm) {
                    this.mPendingIdleUntil = null;
                    rebatchAllAlarmsLocked(false);
                    restorePendingWhileIdleAlarmsLocked();
                }
                if (this.mNextWakeFromIdle == alarm) {
                    this.mNextWakeFromIdle = null;
                    rebatchAllAlarmsLocked(false);
                }
                if (alarm.repeatInterval > 0 && needRepeat) {
                    alarm.count = (int) (((long) alarm.count) + ((nowELAPSED - alarm.whenElapsed) / alarm.repeatInterval));
                    long delta = ((long) alarm.count) * alarm.repeatInterval;
                    long nextElapsed = alarm.whenElapsed + delta;
                    setImplLocked(alarm.type, alarm.when + delta, nextElapsed, alarm.windowLength, maxTriggerTime(nowELAPSED, nextElapsed, alarm.repeatInterval), alarm.repeatInterval, alarm.operation, null, null, alarm.flags, true, alarm.workSource, alarm.alarmClock, alarm.uid, alarm.packageName);
                }
                if (alarm.wakeup) {
                    hasWakeup = true;
                }
                if (alarm.alarmClock != null) {
                    this.mNextAlarmClockMayChange = true;
                }
            }
        }
        this.mCurrentSeq++;
        calculateDeliveryPriorities(triggerList);
        Collections.sort(triggerList, this.mAlarmDispatchComparator);
        return hasWakeup;
    }

    boolean triggerAlarmsLocked(ArrayList<Alarm> triggerList, long nowELAPSED, long nowRTC) {
        boolean hasWakeup = false;
        while (this.mAlarmBatches.size() > 0) {
            Batch batch = (Batch) this.mAlarmBatches.get(0);
            if (batch.start > nowELAPSED) {
                break;
            }
            this.mAlarmBatches.remove(0);
            int N = batch.size();
            for (int i = 0; i < N; i++) {
                Alarm alarm = batch.get(i);
                if ((alarm.flags & 4) != 0) {
                    long lastTime = this.mLastAllowWhileIdleDispatch.get(alarm.uid, 0);
                    long minTime = lastTime + this.mAllowWhileIdleMinTime;
                    if (nowELAPSED < minTime) {
                        if (DEBUG_PANIC) {
                            Slog.d(TAG, alarm + " too frequent, last = " + lastTime + ", now = " + nowELAPSED);
                        }
                        alarm.whenElapsed = minTime;
                        if (alarm.maxWhenElapsed < minTime) {
                            alarm.maxWhenElapsed = minTime;
                        }
                        setImplLocked(alarm, true, false);
                    }
                }
                alarm.count = 1;
                triggerList.add(alarm);
                if ((alarm.flags & 2) != 0) {
                    EventLogTags.writeDeviceIdleWakeFromIdle(this.mPendingIdleUntil != null ? 1 : 0, alarm.statsTag);
                }
                if (this.mPendingIdleUntil == alarm) {
                    this.mPendingIdleUntil = null;
                    rebatchAllAlarmsLocked(false);
                    restorePendingWhileIdleAlarmsLocked();
                }
                if (this.mNextWakeFromIdle == alarm) {
                    this.mNextWakeFromIdle = null;
                    rebatchAllAlarmsLocked(false);
                }
                if (alarm.repeatInterval > 0) {
                    alarm.count = (int) (((long) alarm.count) + ((nowELAPSED - alarm.whenElapsed) / alarm.repeatInterval));
                    long delta = ((long) alarm.count) * alarm.repeatInterval;
                    long nextElapsed = alarm.whenElapsed + delta;
                    setImplLocked(alarm.type, alarm.when + delta, nextElapsed, alarm.windowLength, maxTriggerTime(nowELAPSED, nextElapsed, alarm.repeatInterval), alarm.repeatInterval, alarm.operation, null, null, alarm.flags, true, alarm.workSource, alarm.alarmClock, alarm.uid, alarm.packageName);
                }
                if (alarm.wakeup) {
                    hasWakeup = true;
                }
                if (alarm.alarmClock != null) {
                    this.mNextAlarmClockMayChange = true;
                }
            }
        }
        this.mCurrentSeq++;
        calculateDeliveryPriorities(triggerList);
        Collections.sort(triggerList, this.mAlarmDispatchComparator);
        return hasWakeup;
    }

    void recordWakeupAlarms(ArrayList<Batch> batches, long nowELAPSED, long nowRTC) {
        int numBatches = batches.size();
        int nextBatch = 0;
        while (nextBatch < numBatches) {
            Batch b = (Batch) batches.get(nextBatch);
            if (b.start <= nowELAPSED) {
                int numAlarms = b.alarms.size();
                for (int nextAlarm = 0; nextAlarm < numAlarms; nextAlarm++) {
                    this.mRecentWakeups.add(((Alarm) b.alarms.get(nextAlarm)).makeWakeupEvent(nowRTC));
                }
                nextBatch++;
            } else {
                return;
            }
        }
    }

    long currentNonWakeupFuzzLocked(long nowELAPSED) {
        long timeSinceOn = nowELAPSED - this.mNonInteractiveStartTime;
        if (timeSinceOn < RefactoredBackupManagerService.TIMEOUT_FULL_BACKUP_INTERVAL) {
            return JobStatus.DEFAULT_TRIGGER_MAX_DELAY;
        }
        if (timeSinceOn < 1800000) {
            return 900000;
        }
        return 3600000;
    }

    static int fuzzForDuration(long duration) {
        if (duration < 900000) {
            return (int) duration;
        }
        if (duration < 5400000) {
            return 900000;
        }
        return ProcessList.PSS_MAX_INTERVAL;
    }

    boolean checkAllowNonWakeupDelayLocked(long nowELAPSED) {
        boolean z = false;
        if (this.mInteractive || this.mLastAlarmDeliveryTime <= 0) {
            return false;
        }
        if ((this.mPendingNonWakeupAlarms.size() > 0 || this.mPendingImportantNonWakeupAlarms.size() > 0) && this.mNextNonWakeupDeliveryTime < nowELAPSED) {
            return false;
        }
        if (nowELAPSED - this.mLastAlarmDeliveryTime <= currentNonWakeupFuzzLocked(nowELAPSED)) {
            z = true;
        }
        return z;
    }

    void deliverAlarmsLocked(ArrayList<Alarm> triggerList, long nowELAPSED) {
        removeAlarmWhenInHighTemperatureProtectMode(triggerList);
        suspendAlarmPowerSaveModeLocked(triggerList);
        this.mLastAlarmDeliveryTime = nowELAPSED;
        for (int i = 0; i < triggerList.size(); i++) {
            Alarm alarm = (Alarm) triggerList.get(i);
            boolean allowWhileIdle = (alarm.flags & 4) != 0;
            if (DEBUG_DETAIL) {
                Slog.v(TAG, "sending alarm " + alarm);
            } else if (DEBUG_PANIC) {
                Slog.v(TAG, "sending " + alarm.toStringLite());
            }
            if (alarm.workSource == null || alarm.workSource.size() <= 0) {
                try {
                    ActivityManager.noteAlarmStart(alarm.operation, alarm.uid, alarm.statsTag);
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Failure sending alarm.", e);
                }
            } else {
                for (int wi = 0; wi < alarm.workSource.size(); wi++) {
                    ActivityManager.noteAlarmStart(alarm.operation, alarm.workSource.get(wi), alarm.statsTag);
                }
            }
            this.mDeliveryTracker.deliverLocked(alarm, nowELAPSED, allowWhileIdle);
        }
    }

    void setWakelockWorkSource(PendingIntent pi, WorkSource ws, int type, String tag, int knownUid, boolean first) {
        try {
            boolean unimportant = pi == this.mTimeTickSender;
            this.mWakeLock.setUnimportantForLogging(unimportant);
            if (first || this.mLastWakeLockUnimportantForLogging) {
                this.mWakeLock.setHistoryTag(tag);
            } else {
                this.mWakeLock.setHistoryTag(null);
            }
            this.mLastWakeLockUnimportantForLogging = unimportant;
            if (ws != null) {
                this.mWakeLock.setWorkSource(ws);
                return;
            }
            int uid;
            if (knownUid >= 0) {
                uid = knownUid;
            } else {
                uid = ActivityManager.getService().getUidForIntentSender(pi.getTarget());
            }
            if (uid >= 0) {
                this.mWakeLock.setWorkSource(new WorkSource(uid));
            } else {
                this.mWakeLock.setWorkSource(null);
            }
        } catch (Exception e) {
        }
    }

    private final BroadcastStats getStatsLocked(PendingIntent pi) {
        return getStatsLocked(pi.getCreatorUid(), pi.getCreatorPackage());
    }

    private final BroadcastStats getStatsLocked(int uid, String pkgName) {
        ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) this.mBroadcastStats.get(uid);
        if (uidStats == null) {
            uidStats = new ArrayMap();
            this.mBroadcastStats.put(uid, uidStats);
        }
        BroadcastStats bs = (BroadcastStats) uidStats.get(pkgName);
        if (bs != null) {
            return bs;
        }
        bs = new BroadcastStats(uid, pkgName);
        uidStats.put(pkgName, bs);
        return bs;
    }

    final BroadcastStats getStatsLockedForGuardElf(PendingIntent pi) {
        return getStatsLocked(pi);
    }

    void setImplIntelnalLocked(int type, long when, long whenElapsed, long windowLength, long maxWhen, long interval, PendingIntent operation, int flags, boolean doValidate, WorkSource workSource, AlarmClockInfo alarmClock, int userId) {
        setImplLocked(type, when, whenElapsed, windowLength, maxWhen, interval, operation, null, null, flags, doValidate, workSource, alarmClock, Binder.getCallingUid(), "android");
    }

    void reScheduleAlignTickIfNeed(Batch b) {
        if (this.mOppoAlarmAlignment != null && this.mOppoAlarmAlignment.isAlignTick(b.flags) && !b.hasWakeups()) {
            this.mOppoAlarmAlignment.reScheduleAlignTick();
        }
    }

    public void netStateChanged(boolean state) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = 5;
        msg.arg1 = state ? 1 : 0;
        msg.sendToTarget();
    }

    public void filterTriggerListForStrictMode(ArrayList<Alarm> triggerList) {
        if (OppoProcessManagerHelper.isInStrictMode()) {
            if (DEBUG_PANIC) {
                Slog.v(OppoProcessManager.TAG, "Befor delivering alarm check purbackground state : Now is in StrictMode !");
            }
            if (triggerList.size() > 0) {
                Iterator<Alarm> triggerListIter = triggerList.iterator();
                Boolean isHasSameRepeatAlarm = Boolean.valueOf(false);
                while (triggerListIter.hasNext()) {
                    Alarm alarm = (Alarm) triggerListIter.next();
                    if (OppoProcessManagerHelper.isDelayAppAlarm(alarm.callingPid, alarm.callingUid, alarm.creatorUid, alarm.operation != null ? alarm.operation.getTargetPackage() : alarm.packageName)) {
                        Slog.v(OppoProcessManager.TAG, "The alarm delayed because of purbackground is " + alarm);
                        if (alarm.repeatInterval <= 0 || alarm.listener != null || alarm.operation == null || this.mDelayedForPurBackgroundAlarms.size() <= 0) {
                            this.mDelayedForPurBackgroundAlarms.add(alarm);
                        } else {
                            isHasSameRepeatAlarm = Boolean.valueOf(false);
                            for (int i = 0; i < this.mDelayedForPurBackgroundAlarms.size(); i++) {
                                Alarm delayedAlarm = (Alarm) this.mDelayedForPurBackgroundAlarms.get(i);
                                if (delayedAlarm.repeatInterval > 0 && delayedAlarm.repeatInterval == alarm.repeatInterval && delayedAlarm.operation != null && delayedAlarm.operation.equals(alarm.operation) && delayedAlarm.type == alarm.type && delayedAlarm.packageName.equals(alarm.packageName)) {
                                    isHasSameRepeatAlarm = Boolean.valueOf(true);
                                    Slog.v(OppoProcessManager.TAG, "The alarm is a repeat alarm and it is already in DelayedForPurBackgroundAlarms list !");
                                    break;
                                }
                            }
                            if (!isHasSameRepeatAlarm.booleanValue()) {
                                this.mDelayedForPurBackgroundAlarms.add(alarm);
                            }
                        }
                        triggerListIter.remove();
                    }
                }
            } else if (DEBUG_PANIC) {
                Slog.v(OppoProcessManager.TAG, "Befor delivering alarm check purbackground state: Now is in StrictMode,But the triggerList is Null !");
            }
        }
    }

    public void stopStrictMode() {
        this.mHandler.removeMessages(6);
        this.mHandler.sendEmptyMessage(6);
    }

    private void stopStrictModeLocked() {
        if (this.mDelayedForPurBackgroundAlarms.size() > 0) {
            if (DEBUG_PANIC) {
                Slog.v(OppoProcessManager.TAG, "StopStrictMode ! And the mDelayedForPurBackgroundAlarms is not null,so deliver these Alarms !");
            }
            deliverAlarmsLocked(this.mDelayedForPurBackgroundAlarms, SystemClock.elapsedRealtime());
            this.mDelayedForPurBackgroundAlarms.clear();
        } else if (DEBUG_PANIC) {
            Slog.v(OppoProcessManager.TAG, "StopStrictMode ! And the mDelayedForPurBackgroundAlarms is null !");
        }
    }

    private boolean isSystemApp(int uid, String pkgName) {
        if (uid < 10000) {
            return true;
        }
        PackageManager packageManager = getContext().getPackageManager();
        if (packageManager == null || packageManager.isClosedSuperFirewall()) {
            return true;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = packageManager.getApplicationInfo(pkgName, 8192);
        } catch (NameNotFoundException e) {
            Slog.d(TAG, "getApplicationInfo NameNotFoundException. pkg = " + pkgName);
        }
        return appInfo == null || (appInfo.flags & 1) != 0;
    }

    private void onLowPowerModeChangedInternal(boolean enabled) {
        if (enabled) {
            this.mHandler.removeMessages(101);
            this.mHandler.sendEmptyMessage(101);
            return;
        }
        this.mHandler.removeMessages(102);
        this.mHandler.sendEmptyMessage(102);
    }

    private boolean isLpmFilterPkg(String pkg) {
        boolean contains;
        synchronized (this.mListTopPkg) {
            contains = this.mListTopPkg.contains(pkg);
        }
        return contains;
    }

    private void suspendAlarmPowerSaveModeLocked(ArrayList<Alarm> triggerList) {
        if (this.mColorOsLowPowerModeEnabled.get()) {
            for (int i = triggerList.size() - 1; i >= 0; i--) {
                Alarm alarm = (Alarm) triggerList.get(i);
                String pkgName = alarm.packageName;
                if (!(isSystemApp(alarm.creatorUid, pkgName) || OppoAlarmManagerHelper.inPackageNameWhiteList(pkgName) || OppoAlarmManagerHelper.containKeyWord(pkgName) || isLpmFilterPkg(pkgName))) {
                    if (!(isSameAlarm(alarm) || (this.mListPowerSaveSuspend.contains(alarm) ^ 1) == 0)) {
                        this.mListPowerSaveSuspend.add(alarm);
                        if (DEBUG_PANIC) {
                            Slog.d(TAG, "Power save mode. a=" + alarm);
                        }
                    }
                    triggerList.remove(i);
                }
            }
        }
    }

    private void removeAlarmWhenInHighTemperatureProtectMode(ArrayList<Alarm> triggerList) {
        if (this.mIsHighTemperatureProtectIn) {
            for (int i = triggerList.size() - 1; i >= 0; i--) {
                Alarm alarm = (Alarm) triggerList.get(i);
                if (alarm.creatorUid >= 10000) {
                    if (DEBUG_PANIC) {
                        Slog.d(TAG, "InHighTemperatureProtectMode.Remove alarm = " + alarm);
                    }
                    triggerList.remove(i);
                }
            }
        }
    }

    private void topAppChangeHandleLocked() {
        ArrayList<Alarm> listTopAppAlarm = new ArrayList();
        for (int i = this.mListPowerSaveSuspend.size() - 1; i >= 0; i--) {
            Alarm a = (Alarm) this.mListPowerSaveSuspend.get(i);
            if (isLpmFilterPkg(a.packageName)) {
                listTopAppAlarm.add(a);
                this.mListPowerSaveSuspend.remove(i);
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "Top app. a=" + a);
                }
            }
        }
        if (!listTopAppAlarm.isEmpty()) {
            deliverAlarmsLocked(listTopAppAlarm, SystemClock.elapsedRealtime());
        }
    }

    private void powerSaveExitHandleLocked() {
        if (!this.mListPowerSaveSuspend.isEmpty()) {
            if (DEBUG_PANIC) {
                Slog.d(TAG, "Exit Power save mode, send...");
            }
            deliverAlarmsLocked(this.mListPowerSaveSuspend, SystemClock.elapsedRealtime());
            this.mListPowerSaveSuspend.clear();
        }
    }

    private boolean isSameAlarm(Alarm alarm) {
        for (int i = 0; i < this.mListPowerSaveSuspend.size(); i++) {
            Alarm delayedAlarm = (Alarm) this.mListPowerSaveSuspend.get(i);
            if (delayedAlarm.matches(alarm.operation, alarm.listener)) {
                return true;
            }
            if (alarm.operation != null && delayedAlarm.operation != null && delayedAlarm.type == alarm.type && delayedAlarm.packageName.equals(alarm.packageName)) {
                try {
                    String actionAlarm = alarm.operation.getIntent().getAction();
                    if (actionAlarm != null && actionAlarm.equals(delayedAlarm.operation.getIntent().getAction())) {
                        return true;
                    }
                } catch (Exception e) {
                    Slog.i(TAG, "isSameAlarm: ", e);
                }
            }
        }
        return false;
    }
}
