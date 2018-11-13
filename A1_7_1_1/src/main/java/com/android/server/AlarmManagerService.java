package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.AppOpsManager;
import android.app.BroadcastOptions;
import android.app.IAlarmCompleteListener.Stub;
import android.app.IAlarmListener;
import android.app.IAlarmManager;
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
import android.hardware.display.WifiDisplayStatus;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.ArrayMap;
import android.util.KeyValueListParser;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseLongArray;
import android.util.TimeUtils;
import com.android.internal.util.LocalLog;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.OppoProcessManagerHelper;
import com.android.server.am.OppoProtectEyeManagerService;
import com.android.server.am.OppoProtectEyeManagerService.ActivityChangedListener;
import com.android.server.display.OppoBrightUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oppo.IElsaManager;
import com.mediatek.amplus.AlarmManagerPlus;
import com.mediatek.common.dm.DmAgent;
import com.mediatek.datashaping.IDataShapingManager;
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
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class AlarmManagerService extends SystemService implements Callbacks {
    static final int ALARM_EVENT = 1;
    static final String ClockReceiver_TAG = "ClockReceiver";
    static final boolean DEBUG_ALARM_CLOCK = false;
    static boolean DEBUG_BATCH = false;
    static boolean DEBUG_DETAIL = false;
    static final boolean DEBUG_LISTENER_CALLBACK = false;
    static boolean DEBUG_PANIC = false;
    static boolean DEBUG_PENDING = false;
    static boolean DEBUG_VALIDATE = false;
    private static final int ELAPSED_REALTIME_MASK = 8;
    private static final int ELAPSED_REALTIME_WAKEUP_MASK = 4;
    static final int IS_WAKEUP_MASK = 5;
    static final long MIN_FUZZABLE_INTERVAL = 10000;
    private static final Intent NEXT_ALARM_CLOCK_CHANGED_INTENT = null;
    static final long PRE_BOOT_TIME = 60000;
    static final int PRIO_NORMAL = 2;
    static final int PRIO_TICK = 0;
    static final int PRIO_WAKEUP = 1;
    static final boolean RECORD_ALARMS_IN_HISTORY = true;
    static final boolean RECORD_DEVICE_IDLE_ALARMS = false;
    private static final int RTC_MASK = 2;
    private static final int RTC_WAKEUP_MASK = 1;
    static final String TAG = "AlarmManager";
    static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    static final int TIME_CHANGED_MASK = 65536;
    static final int TYPE_NONWAKEUP_MASK = 1;
    static final boolean WAKEUP_STATS = false;
    static boolean localLOGV;
    private static int mAlarmMode;
    private static boolean mSupportAlarmGrouping;
    static final BatchTimeOrder sBatchOrder = null;
    static final IncreasingTimeOrder sIncreasingTimeOrder = null;
    private final int MAX_TRIGGERED_ALARM_SIZE_LIMIT;
    final long RECENT_WAKEUP_PERIOD;
    private IDataShapingManager dataShapingManager;
    private ActivityChangedListener mActivityChangedListener;
    final ArrayList<Batch> mAlarmBatches;
    final Comparator<Alarm> mAlarmDispatchComparator;
    private ArrayList<String> mAlarmIconPackageList;
    AlarmUpdateHelper mAlarmUpdateHelper;
    private OppoAlarmWakeupDetection mAlarmWakeupDetection;
    final ArrayList<IdleDispatchEntry> mAllowWhileIdleDispatches;
    long mAllowWhileIdleMinTime;
    private AlarmManagerPlus mAmPlus;
    private AppKillReceiver mAppKillReceiver;
    AppOpsManager mAppOps;
    private final Intent mBackgroundIntent;
    int mBroadcastRefCount;
    final SparseArray<ArrayMap<String, BroadcastStats>> mBroadcastStats;
    ClockReceiver mClockReceiver;
    private AtomicBoolean mColorOsLowPowerModeEnabled;
    final Constants mConstants;
    int mCurrentSeq;
    private boolean mDMEnable;
    private Object mDMLock;
    private DMReceiver mDMReceiver;
    PendingIntent mDateChangeSender;
    int mDelayAlarmMode;
    ArrayList<Alarm> mDelayedForPurBackgroundAlarms;
    final DeliveryTracker mDeliveryTracker;
    int[] mDeviceIdleUserWhitelist;
    private ArrayList<PendingIntent> mDmFreeList;
    private ArrayList<Alarm> mDmResendList;
    final AlarmHandler mHandler;
    private final SparseArray<AlarmClockInfo> mHandlerSparseAlarmClockArray;
    private boolean mIPOShutdown;
    Bundle mIdleOptions;
    ArrayList<InFlight> mInFlight;
    boolean mInteractive;
    InteractiveStateReceiver mInteractiveStateReceiver;
    boolean mIsWFDConnected;
    long mLastAlarmDeliveryTime;
    final SparseLongArray mLastAllowWhileIdleDispatch;
    long mLastTimeChangeClockTime;
    long mLastTimeChangeRealtime;
    boolean mLastWakeLockUnimportantForLogging;
    private long mLastWakeup;
    private long mLastWakeupSet;
    ArrayList<Alarm> mListPowerSaveSuspend;
    ArrayList<String> mListTopPkg;
    com.android.server.DeviceIdleController.LocalService mLocalDeviceIdleController;
    final Object mLock;
    final LocalLog mLog;
    long mMaxDelayTime;
    long mNativeData;
    private boolean mNeedGrouping;
    boolean mNeedRebatchForRepeatingAlarm;
    boolean mNetstateInteractive;
    private final SparseArray<AlarmClockInfo> mNextAlarmClockForUser;
    private boolean mNextAlarmClockMayChange;
    private long mNextNonWakeup;
    long mNextNonWakeupDeliveryTime;
    Alarm mNextWakeFromIdle;
    private long mNextWakeup;
    long mNonInteractiveStartTime;
    long mNonInteractiveTime;
    int mNumDelayedAlarms;
    int mNumTimeChanged;
    private OppoAlarmAlignment mOppoAlarmAlignment;
    private boolean mOppoGuardElfFeature;
    private boolean mPPLEnable;
    Alarm mPendingIdleUntil;
    ArrayList<Alarm> mPendingImportantNonWakeupAlarms;
    ArrayList<Alarm> mPendingNonWakeupAlarms;
    private final SparseBooleanArray mPendingSendNextAlarmClockChangedForUser;
    ArrayList<Alarm> mPendingWhileIdleAlarms;
    private PowerManagerInternal mPowerManagerInternal;
    private Object mPowerOffAlarmLock;
    private final ArrayList<Alarm> mPoweroffAlarms;
    final HashMap<String, PriorityClass> mPriorities;
    Random mRandom;
    final LinkedList<WakeupEvent> mRecentWakeups;
    private final IBinder mService;
    long mStartCurrentDelayTime;
    PendingIntent mTimeTickSender;
    private final SparseArray<AlarmClockInfo> mTmpSparseAlarmClockArray;
    private volatile String mTopPkg;
    long mTotalDelayTime;
    private Map<String, Integer> mTriggeredAlarmMap;
    private UninstallReceiver mUninstallReceiver;
    WFDStatusChangedReceiver mWFDStatusChangedReceiver;
    private Object mWaitThreadlock;
    WakeLock mWakeLock;

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
        public boolean needGrouping;
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

        public Alarm(int _type, long _when, long _whenElapsed, long _windowLength, long _maxWhen, long _interval, PendingIntent _op, IAlarmListener _rec, String _listenerTag, WorkSource _ws, int _flags, AlarmClockInfo _info, int _uid, String _pkgName, boolean mNeedGrouping) {
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
            this.needGrouping = mNeedGrouping;
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
            if (this.listenerTag != null) {
                sb.append(" tag ");
                sb.append(this.listenerTag);
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

    private class AlarmThread extends Thread {
        public AlarmThread() {
            super(AlarmManagerService.TAG);
        }

        public void run() {
            ArrayList<Alarm> triggerList = new ArrayList();
            ArrayList<Alarm> triggerListNonWakeup = new ArrayList();
            while (true) {
                AlarmManagerService alarmManagerService;
                if (SystemProperties.get("ro.mtk_ipo_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && AlarmManagerService.this.mIPOShutdown) {
                    try {
                        if (AlarmManagerService.this.mNativeData != -1) {
                            synchronized (AlarmManagerService.this.mLock) {
                                AlarmManagerService.this.mAlarmBatches.clear();
                            }
                        }
                        synchronized (AlarmManagerService.this.mWaitThreadlock) {
                            AlarmManagerService.this.mWaitThreadlock.wait();
                        }
                    } catch (InterruptedException e) {
                        Slog.v(AlarmManagerService.TAG, "InterruptedException ");
                    }
                }
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
                    if (lastTimeChangeClockTime == 0 || nowRTC < expectedClockTime - 500 || nowRTC > 500 + expectedClockTime) {
                        if (AlarmManagerService.DEBUG_BATCH) {
                            Slog.v(AlarmManagerService.TAG, "Time changed notification from kernel; rebatching");
                        }
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
                        intent.addFlags(603979776);
                        AlarmManagerService.this.getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
                        result |= 5;
                    }
                }
                Object obj;
                if (result != 65536) {
                    obj = AlarmManagerService.this.mLock;
                    synchronized (obj) {
                        if (AlarmManagerService.localLOGV || AlarmManagerService.DEBUG_DETAIL) {
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
                            AlarmManagerService.this.filterTriggerListForStrictMode(triggerList);
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
                            AlarmManagerService.this.filterTriggerListForStrictMode(triggerList);
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

    private class AppKillReceiver extends BroadcastReceiver {
        static final String ACTION_APP_KILL = "oppo.action.appkilled";

        public AppKillReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_APP_KILL);
            AlarmManagerService.this.getContext().registerReceiver(this, filter, "android.permission.FORCE_STOP_PACKAGES", null);
        }

        public void onReceive(Context context, Intent intent) {
            if (ACTION_APP_KILL.equals(intent.getAction())) {
                String[] pkgList = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                if (pkgList != null && pkgList.length > 0) {
                    synchronized (AlarmManagerService.this.mLock) {
                        for (String pkg : pkgList) {
                            if (!OppoAlarmManagerHelper.isFilterRemovePackage(pkg)) {
                                AlarmManagerService.this.removeLocked(pkg);
                                AlarmManagerService.this.mPriorities.remove(pkg);
                                for (int i = AlarmManagerService.this.mBroadcastStats.size() - 1; i >= 0; i--) {
                                    ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) AlarmManagerService.this.mBroadcastStats.valueAt(i);
                                    if (uidStats.remove(pkg) != null && uidStats.size() <= 0) {
                                        AlarmManagerService.this.mBroadcastStats.removeAt(i);
                                    }
                                }
                                if (AlarmManagerService.DEBUG_PANIC) {
                                    Log.d(AlarmManagerService.TAG, "alarm removed for: " + pkg);
                                }
                            }
                        }
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
            if (AlarmManagerService.DEBUG_BATCH) {
                Slog.v(AlarmManagerService.TAG, "Adding " + alarm + " to " + this);
            }
            if (alarm.whenElapsed > this.start) {
                this.start = alarm.whenElapsed;
                newStart = true;
            }
            if (alarm.maxWhenElapsed < this.end) {
                this.end = alarm.maxWhenElapsed;
            }
            this.flags |= alarm.flags;
            if (AlarmManagerService.DEBUG_BATCH) {
                Slog.v(AlarmManagerService.TAG, "    => now " + this);
            }
            return newStart;
        }

        boolean remove(PendingIntent operation, IAlarmListener listener) {
            if (operation == null && listener == null) {
                if (AlarmManagerService.localLOGV) {
                    Slog.w(AlarmManagerService.TAG, "requested remove() of null operation", new RuntimeException("here"));
                }
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
                if (AlarmManagerService.localLOGV) {
                    Slog.w(AlarmManagerService.TAG, "requested remove() of null packageName", new RuntimeException("here"));
                }
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
                    if (alarm.uid == uid && ActivityManagerNative.getDefault().getAppStartMode(uid, alarm.packageName) == 2) {
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
                if (AlarmManagerService.DEBUG_BATCH) {
                    Slog.v(AlarmManagerService.TAG, "Received TIME_TICK alarm; rescheduling");
                }
                if (AlarmManagerService.DEBUG_DETAIL) {
                    Slog.v(AlarmManagerService.TAG, "mSupportAlarmGrouping = " + AlarmManagerService.mSupportAlarmGrouping + "  mAmPlus = " + AlarmManagerService.this.mAmPlus);
                }
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
        public long ALLOW_WHILE_IDLE_SHORT_TIME = 5000;
        public long ALLOW_WHILE_IDLE_WHITELIST_DURATION = 10000;
        public long LISTENER_TIMEOUT = 5000;
        public long MIN_FUTURITY = 5000;
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
                    Slog.e(AlarmManagerService.TAG, "Bad device idle settings", e);
                }
                this.MIN_FUTURITY = this.mParser.getLong(KEY_MIN_FUTURITY, 5000);
                this.MIN_INTERVAL = this.mParser.getLong(KEY_MIN_INTERVAL, 60000);
                this.ALLOW_WHILE_IDLE_SHORT_TIME = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_SHORT_TIME, 5000);
                this.ALLOW_WHILE_IDLE_LONG_TIME = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_LONG_TIME, DEFAULT_ALLOW_WHILE_IDLE_LONG_TIME);
                this.ALLOW_WHILE_IDLE_WHITELIST_DURATION = this.mParser.getLong(KEY_ALLOW_WHILE_IDLE_WHITELIST_DURATION, 10000);
                this.LISTENER_TIMEOUT = this.mParser.getLong(KEY_LISTENER_TIMEOUT, 5000);
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

    class DMReceiver extends BroadcastReceiver {
        public DMReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(NotificationManagerService.OMADM_LAWMO_LOCK);
            filter.addAction(NotificationManagerService.OMADM_LAWMO_UNLOCK);
            filter.addAction(NotificationManagerService.PPL_LOCK);
            filter.addAction(NotificationManagerService.PPL_UNLOCK);
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(NotificationManagerService.OMADM_LAWMO_LOCK)) {
                AlarmManagerService.this.mDMEnable = false;
            } else if (action.equals(NotificationManagerService.OMADM_LAWMO_UNLOCK)) {
                AlarmManagerService.this.mDMEnable = true;
                AlarmManagerService.this.enableDm();
            } else if (action.equals(NotificationManagerService.PPL_LOCK)) {
                AlarmManagerService.this.mPPLEnable = false;
            } else if (action.equals(NotificationManagerService.PPL_UNLOCK)) {
                AlarmManagerService.this.mPPLEnable = true;
                AlarmManagerService.this.enableDm();
            }
        }
    }

    class DeliveryTracker extends Stub implements OnFinished {
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
                ActivityManagerNative.noteAlarmFinish(inflight.mPendingIntent, inflight.mUid, inflight.mTag);
                return;
            }
            for (int wi = 0; wi < inflight.mWorkSource.size(); wi++) {
                ActivityManagerNative.noteAlarmFinish(inflight.mPendingIntent, inflight.mWorkSource.get(wi), inflight.mTag);
            }
        }

        private void updateTrackingLocked(InFlight inflight) {
            if (inflight != null) {
                updateStatsLocked(inflight);
            }
            AlarmManagerService alarmManagerService = AlarmManagerService.this;
            alarmManagerService.mBroadcastRefCount--;
            if (AlarmManagerService.this.mBroadcastRefCount == 0) {
                AlarmManagerService.this.mHandler.obtainMessage(4, Integer.valueOf(0)).sendToTarget();
                AlarmManagerService.this.mWakeLock.release();
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
                Slog.w(AlarmManagerService.TAG, "Invalid alarmComplete: uid=" + Binder.getCallingUid() + " pid=" + Binder.getCallingPid());
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (AlarmManagerService.this.mLock) {
                    AlarmManagerService.this.mHandler.removeMessages(3, who);
                    InFlight inflight = removeLocked(who);
                    if (inflight != null) {
                        if (AlarmManagerService.DEBUG_LISTENER_CALLBACK) {
                            Slog.i(AlarmManagerService.TAG, "alarmComplete() from " + who);
                        }
                        updateTrackingLocked(inflight);
                    } else if (AlarmManagerService.DEBUG_LISTENER_CALLBACK) {
                        Slog.i(AlarmManagerService.TAG, "Late alarmComplete() from " + who);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void onSendFinished(PendingIntent pi, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
            if (AlarmManagerService.DEBUG_PANIC) {
                Slog.d(AlarmManagerService.TAG, "onSendFinished begin");
            }
            synchronized (AlarmManagerService.this.mLock) {
                updateTrackingLocked(removeLocked(pi, intent));
            }
        }

        public void alarmTimedOut(IBinder who) {
            synchronized (AlarmManagerService.this.mLock) {
                InFlight inflight = removeLocked(who);
                if (inflight != null) {
                    if (AlarmManagerService.DEBUG_LISTENER_CALLBACK) {
                        Slog.i(AlarmManagerService.TAG, "Alarm listener " + who + " timed out in delivery");
                    }
                    updateTrackingLocked(inflight);
                } else if (AlarmManagerService.DEBUG_LISTENER_CALLBACK) {
                    Slog.i(AlarmManagerService.TAG, "Spurious timeout of listener " + who);
                }
            }
        }

        public void deliverLocked(Alarm alarm, long nowELAPSED, boolean allowWhileIdle) {
            if (alarm.operation != null) {
                try {
                    alarm.operation.send(AlarmManagerService.this.getContext(), 0, AlarmManagerService.this.mBackgroundIntent.putExtra("android.intent.extra.ALARM_COUNT", alarm.count), AlarmManagerService.this.mDeliveryTracker, AlarmManagerService.this.mHandler, null, allowWhileIdle ? AlarmManagerService.this.mIdleOptions : null);
                    if (AlarmManagerService.DEBUG_PANIC) {
                        Slog.v(AlarmManagerService.TAG, "sending alarm " + alarm + " success");
                    }
                } catch (CanceledException e) {
                    if (AlarmManagerService.this.mAlarmWakeupDetection != null) {
                        AlarmManagerService.this.mAlarmWakeupDetection.canceledPendingIntentDetection(alarm, nowELAPSED);
                    }
                    if (alarm.repeatInterval > 0) {
                        boolean z;
                        AlarmManagerService alarmManagerService = AlarmManagerService.this;
                        if (AlarmManagerService.this.removeInvalidAlarmLocked(alarm.operation, alarm.listener)) {
                            z = true;
                        } else {
                            z = AlarmManagerService.this.mNeedRebatchForRepeatingAlarm;
                        }
                        alarmManagerService.mNeedRebatchForRepeatingAlarm = z;
                    }
                    return;
                }
            }
            try {
                if (AlarmManagerService.DEBUG_LISTENER_CALLBACK) {
                    Slog.v(AlarmManagerService.TAG, "Alarm to uid=" + alarm.uid + " listener=" + alarm.listener.asBinder());
                }
                alarm.listener.doAlarm(this);
                AlarmManagerService.this.mHandler.sendMessageDelayed(AlarmManagerService.this.mHandler.obtainMessage(3, alarm.listener.asBinder()), AlarmManagerService.this.mConstants.LISTENER_TIMEOUT);
            } catch (Throwable e2) {
                if (AlarmManagerService.DEBUG_LISTENER_CALLBACK) {
                    Slog.i(AlarmManagerService.TAG, "Alarm undeliverable to listener " + alarm.listener.asBinder(), e2);
                }
                return;
            }
            if (AlarmManagerService.this.mBroadcastRefCount == 0) {
                AlarmManagerService.this.setWakelockWorkSource(alarm.operation, alarm.workSource, alarm.type, alarm.statsTag, alarm.operation == null ? alarm.uid : -1, true);
                AlarmManagerService.this.mWakeLock.acquire();
                AlarmManagerService.this.mHandler.obtainMessage(4, Integer.valueOf(1)).sendToTarget();
            }
            InFlight inflight = new InFlight(AlarmManagerService.this, alarm.operation, alarm.listener, alarm.workSource, alarm.uid, alarm.packageName, alarm.type, alarm.statsTag, nowELAPSED);
            AlarmManagerService.this.mInFlight.add(inflight);
            AlarmManagerService alarmManagerService2 = AlarmManagerService.this;
            alarmManagerService2.mBroadcastRefCount++;
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
                    ActivityManagerNative.noteWakeupAlarm(alarm.operation, alarm.uid, alarm.packageName, alarm.statsTag);
                } else {
                    for (int wi = 0; wi < alarm.workSource.size(); wi++) {
                        String wsName = alarm.workSource.getName(wi);
                        PendingIntent pendingIntent = alarm.operation;
                        int i = alarm.workSource.get(wi);
                        if (wsName == null) {
                            wsName = alarm.packageName;
                        }
                        ActivityManagerNative.noteWakeupAlarm(pendingIntent, i, wsName, alarm.statsTag);
                    }
                }
            }
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
        final WorkSource mWorkSource;

        InFlight(AlarmManagerService service, PendingIntent pendingIntent, IAlarmListener listener, WorkSource workSource, int uid, String alarmPkg, int alarmType, String tag, long nowELAPSED) {
            BroadcastStats -wrap1;
            IBinder iBinder = null;
            this.mPendingIntent = pendingIntent;
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
    }

    public static class IncreasingTimeOrder implements Comparator<Alarm> {
        public int compare(Alarm a1, Alarm a2) {
            long when1 = a1.when;
            long when2 = a2.when;
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

        public void onUidStateChanged(int uid, int procState) throws RemoteException {
        }

        public void onUidGone(int uid) throws RemoteException {
        }

        public void onUidActive(int uid) throws RemoteException {
        }

        public void onUidIdle(int uid) throws RemoteException {
            synchronized (AlarmManagerService.this.mLock) {
                AlarmManagerService.this.removeForStoppedLocked(uid);
            }
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

        /* JADX WARNING: Missing block: B:66:0x011f, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            synchronized (AlarmManagerService.this.mLock) {
                Slog.d(AlarmManagerService.TAG, "UninstallReceiver  action = " + intent.getAction());
                String action = intent.getAction();
                String[] pkgList = null;
                int i;
                int length;
                if ("android.intent.action.QUERY_PACKAGE_RESTART".equals(action)) {
                    pkgList = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                    i = 0;
                    length = pkgList.length;
                    while (i < length) {
                        String packageName = pkgList[i];
                        if (!AlarmManagerService.this.lookForPackageLocked(packageName) || "android".equals(packageName)) {
                            i++;
                        } else {
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
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action) && intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    return;
                } else {
                    Uri data = intent.getData();
                    if (data != null) {
                        pkg = data.getSchemeSpecificPart();
                        if (!(pkg == null || OppoAlarmManagerHelper.isFilterRemovePackage(pkg))) {
                            pkgList = new String[1];
                            pkgList[0] = pkg;
                        }
                    }
                }
                if (pkgList != null && pkgList.length > 0) {
                    for (String pkg2 : pkgList) {
                        if (!"android".equals(pkg2)) {
                            AlarmManagerService.this.removeLocked(pkg2);
                            AlarmManagerService.this.mPriorities.remove(pkg2);
                            for (int i2 = AlarmManagerService.this.mBroadcastStats.size() - 1; i2 >= 0; i2--) {
                                ArrayMap<String, BroadcastStats> uidStats = (ArrayMap) AlarmManagerService.this.mBroadcastStats.valueAt(i2);
                                if (uidStats.remove(pkg2) != null && uidStats.size() <= 0) {
                                    AlarmManagerService.this.mBroadcastStats.removeAt(i2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    class WFDStatusChangedReceiver extends BroadcastReceiver {
        public WFDStatusChangedReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED");
            AlarmManagerService.this.getContext().registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.hardware.display.action.WIFI_DISPLAY_STATUS_CHANGED".equals(intent.getAction())) {
                WifiDisplayStatus wfdStatus = (WifiDisplayStatus) intent.getParcelableExtra("android.hardware.display.extra.WIFI_DISPLAY_STATUS");
                AlarmManagerService.this.mIsWFDConnected = 2 == wfdStatus.getActiveDisplayState();
                Slog.v(AlarmManagerService.TAG, "Wfd Status changed new  = " + AlarmManagerService.this.mIsWFDConnected);
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.AlarmManagerService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.AlarmManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.AlarmManagerService.<clinit>():void");
    }

    private native boolean bootFromAlarm(int i);

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
        this.MAX_TRIGGERED_ALARM_SIZE_LIMIT = 1000;
        this.mTriggeredAlarmMap = new ArrayMap();
        this.mBackgroundIntent = new Intent().addFlags(4);
        this.mLog = new LocalLog(TAG);
        this.mLock = new Object();
        this.mBroadcastRefCount = 0;
        this.mPendingNonWakeupAlarms = new ArrayList();
        this.mDelayedForPurBackgroundAlarms = new ArrayList();
        this.mInFlight = new ArrayList();
        this.mHandler = new AlarmHandler();
        this.mPendingImportantNonWakeupAlarms = new ArrayList();
        this.mNetstateInteractive = true;
        this.mDelayAlarmMode = 0;
        this.mDeliveryTracker = new DeliveryTracker();
        this.mInteractive = true;
        this.mNeedRebatchForRepeatingAlarm = false;
        this.mIsWFDConnected = false;
        this.mDeviceIdleUserWhitelist = new int[0];
        this.mLastAllowWhileIdleDispatch = new SparseLongArray();
        this.mAllowWhileIdleDispatches = new ArrayList();
        this.mNextAlarmClockForUser = new SparseArray();
        this.mTmpSparseAlarmClockArray = new SparseArray();
        this.mPendingSendNextAlarmClockChangedForUser = new SparseBooleanArray();
        this.mHandlerSparseAlarmClockArray = new SparseArray();
        this.mDMReceiver = null;
        this.mDMEnable = true;
        this.mPPLEnable = true;
        this.mDMLock = new Object();
        this.mDmFreeList = null;
        this.mAlarmIconPackageList = null;
        this.mDmResendList = null;
        this.mNeedGrouping = true;
        this.mPriorities = new HashMap();
        this.mCurrentSeq = 0;
        this.mRecentWakeups = new LinkedList();
        this.RECENT_WAKEUP_PERIOD = 86400000;
        this.mAlarmDispatchComparator = new Comparator<Alarm>() {
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
        this.mAlarmBatches = new ArrayList();
        this.mPendingIdleUntil = null;
        this.mNextWakeFromIdle = null;
        this.mPendingWhileIdleAlarms = new ArrayList();
        this.mWaitThreadlock = new Object();
        this.mIPOShutdown = false;
        this.mPowerOffAlarmLock = new Object();
        this.mPoweroffAlarms = new ArrayList();
        this.mBroadcastStats = new SparseArray();
        this.mNumDelayedAlarms = 0;
        this.mTotalDelayTime = 0;
        this.mMaxDelayTime = 0;
        this.mService = new IAlarmManager.Stub() {
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
                    } else if (workSource == null && (callingUid < 10000 || Arrays.binarySearch(AlarmManagerService.this.mDeviceIdleUserWhitelist, UserHandle.getAppId(callingUid)) >= 0)) {
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
                if (AlarmManagerService.this.mNativeData == 0 || AlarmManagerService.this.mNativeData == -1) {
                    Slog.w(AlarmManagerService.TAG, "Not setting time since no alarm driver is available.");
                    return false;
                }
                synchronized (AlarmManagerService.this.mLock) {
                    Slog.d(AlarmManagerService.TAG, "setKernelTime  setTime = " + millis);
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
                    if (AlarmManagerService.DEBUG_ALARM_CLOCK) {
                        Slog.d(AlarmManagerService.TAG, "manual remove option = " + operation);
                    }
                    AlarmManagerService.this.removeLocked(operation, listener);
                }
            }

            public long getNextWakeFromIdleTime() {
                return AlarmManagerService.this.getNextWakeFromIdleTimeImpl();
            }

            public void cancelPoweroffAlarm(String name) {
                AlarmManagerService.this.cancelPoweroffAlarmImpl(name);
            }

            public void removeFromAms(String packageName) {
                AlarmManagerService.this.removeFromAmsImpl(packageName);
            }

            public boolean lookForPackageFromAms(String packageName) {
                return AlarmManagerService.this.lookForPackageFromAmsImpl(packageName);
            }

            public AlarmClockInfo getNextAlarmClock(int userId) {
                return AlarmManagerService.this.getNextAlarmClockImpl(ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, "getNextAlarmClock", null));
            }

            protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
                if (AlarmManagerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                    pw.println("Permission Denial: can't dump AlarmManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                } else if (!AlarmManagerService.this.dynamicallyConfigAlarmManagerServiceLogTag(pw, args)) {
                    int opti = 0;
                    while (opti < args.length) {
                        String opt = args[opti];
                        if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                            break;
                        }
                        opti++;
                        if ("-h".equals(opt)) {
                            pw.println("alarm manager dump options:");
                            pw.println("  log  [on/off]");
                            pw.println("  Example:");
                            pw.println("  $adb shell dumpsys alarm log on");
                            pw.println("  $adb shell dumpsys alarm log off");
                            return;
                        }
                        pw.println("Unknown argument: " + opt + "; use -h for help");
                    }
                    if (opti < args.length) {
                        String cmd = args[opti];
                        opti++;
                        if ("log".equals(cmd)) {
                            AlarmManagerService.this.configLogTag(pw, args, opti);
                            return;
                        }
                    }
                    AlarmManagerService.this.dumpImpl(pw, args);
                    if (args.length >= 1 && "alignWhiteList".equals(args[0])) {
                        OppoAlarmManagerHelper.dump(pw);
                    }
                }
            }

            public Map<String, Integer> getTriggeredAlarms() {
                return AlarmManagerService.this.mTriggeredAlarmMap;
            }
        };
        this.mAlarmWakeupDetection = null;
        this.mOppoGuardElfFeature = false;
        this.mOppoAlarmAlignment = null;
        this.mColorOsLowPowerModeEnabled = new AtomicBoolean(false);
        this.mTopPkg = IElsaManager.EMPTY_PACKAGE;
        this.mListPowerSaveSuspend = new ArrayList();
        this.mListTopPkg = new ArrayList();
        this.mActivityChangedListener = new ActivityChangedListener() {
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
        DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        this.mAlarmUpdateHelper = new AlarmUpdateHelper(this, context);
        this.mConstants = new Constants(this.mHandler);
        OppoBPMHelper.setAlarmService(this);
    }

    static long convertToElapsed(long when, int type) {
        boolean isRtc = true;
        if (!(type == 1 || type == 0)) {
            isRtc = false;
        }
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
            if (!mSupportAlarmGrouping || this.mAmPlus == null) {
                if ((b.flags & 1) == 0 && b.canHold(whenElapsed, maxWhen) && b.canHold(whenElapsed, maxWhen)) {
                    return i;
                }
            } else if (b.canHold(whenElapsed, maxWhen)) {
                return i;
            }
        }
        return -2;
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
        if (DEBUG_BATCH) {
            Slog.d(TAG, "rebatchAllAlarmsLocked begin oldBatches count = " + oldBatches);
        }
        for (int batchNum = 0; batchNum < oldBatches; batchNum++) {
            Batch batch = (Batch) oldSet.get(batchNum);
            int N = batch.size();
            if (DEBUG_BATCH) {
                Slog.d(TAG, "rebatchAllAlarmsLocked  batch.size() = " + batch.size());
            }
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
        if (mSupportAlarmGrouping && this.mAmPlus != null) {
            maxElapsed = this.mAmPlus.getMaxTriggerTime(a.type, whenElapsed, a.windowLength, a.repeatInterval, a.operation, mAlarmMode, true);
            if (maxElapsed < 0) {
                maxElapsed = 0 - maxElapsed;
                a.needGrouping = false;
            } else {
                a.needGrouping = true;
            }
        } else if (a.windowLength == 0) {
            maxElapsed = whenElapsed;
        } else if (a.windowLength < 0) {
            maxElapsed = maxTriggerTime(nowElapsed, whenElapsed, a.repeatInterval);
            a.windowLength = maxElapsed - whenElapsed;
        } else {
            maxElapsed = whenElapsed + a.windowLength;
        }
        a.whenElapsed = whenElapsed;
        a.maxWhenElapsed = maxElapsed;
        if (DEBUG_BATCH) {
            Slog.d(TAG, "reAddAlarmLocked a.whenElapsed  = " + a.whenElapsed + " a.maxWhenElapsed = " + a.maxWhenElapsed);
        }
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
        if (mSupportAlarmGrouping && this.mAmPlus == null) {
            try {
                this.mAmPlus = new AlarmManagerPlus(getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mWakeLock = ((PowerManager) getContext().getSystemService("power")).newWakeLock(1, "*alarm*");
        this.mTimeTickSender = PendingIntent.getBroadcastAsUser(getContext(), 0, new Intent("android.intent.action.TIME_TICK").addFlags(1342177280), 0, UserHandle.ALL);
        Intent intent = new Intent("android.intent.action.DATE_CHANGED");
        intent.addFlags(536870912);
        this.mDateChangeSender = PendingIntent.getBroadcastAsUser(getContext(), 0, intent, 67108864, UserHandle.ALL);
        this.mClockReceiver = new ClockReceiver();
        this.mClockReceiver.scheduleTimeTickEvent();
        this.mClockReceiver.scheduleDateChangedEvent();
        this.mInteractiveStateReceiver = new InteractiveStateReceiver();
        this.mWFDStatusChangedReceiver = new WFDStatusChangedReceiver();
        this.mUninstallReceiver = new UninstallReceiver();
        this.mAppKillReceiver = new AppKillReceiver();
        this.mAlarmIconPackageList = new ArrayList();
        this.mAlarmIconPackageList.add("com.android.deskclock");
        try {
            IBinder binder = ServiceManager.getService("DmAgent");
            if (binder != null) {
                boolean locked = DmAgent.Stub.asInterface(binder).isLockFlagSet();
                Slog.i(TAG, "dm state lock is " + locked);
                this.mDMEnable = !locked;
            } else {
                Slog.e(TAG, "dm binder is null!");
            }
        } catch (RemoteException e2) {
            Slog.e(TAG, "remote error");
        }
        this.mDMReceiver = new DMReceiver();
        this.mDmFreeList = new ArrayList();
        this.mDmFreeList.add(this.mTimeTickSender);
        this.mDmFreeList.add(this.mDateChangeSender);
        this.mDmResendList = new ArrayList();
        if (this.mNativeData != 0) {
            new AlarmThread().start();
        } else {
            Slog.w(TAG, "Failed to open alarm driver. Falling back to a handler.");
        }
        try {
            ActivityManagerNative.getDefault().registerUidObserver(new UidObserver(), 4);
        } catch (RemoteException e3) {
        }
        if (SystemProperties.get("ro.mtk_ipo_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.ACTION_BOOT_IPO");
            filter.addAction("android.intent.action.ACTION_SHUTDOWN");
            filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
            getContext().registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction()) || "android.intent.action.ACTION_SHUTDOWN_IPO".equals(intent.getAction())) {
                        AlarmManagerService.this.shutdownCheckPoweroffAlarm();
                        AlarmManagerService.this.mIPOShutdown = true;
                        if (AlarmManagerService.this.mNativeData != -1 && "android.intent.action.ACTION_SHUTDOWN_IPO".equals(intent.getAction())) {
                            Slog.d(AlarmManagerService.TAG, "receive ACTION_SHUTDOWN_IPO , so close the fd ");
                            AlarmManagerService.this.close(AlarmManagerService.this.mNativeData);
                            AlarmManagerService.this.mNativeData = -1;
                        }
                    } else if ("android.intent.action.ACTION_BOOT_IPO".equals(intent.getAction())) {
                        AlarmManagerService.this.mIPOShutdown = false;
                        AlarmManagerService.this.mNativeData = AlarmManagerService.this.init();
                        AlarmManagerService.this.mNextWakeup = AlarmManagerService.this.mNextNonWakeup = 0;
                        Intent timeChangeIntent = new Intent("android.intent.action.TIME_SET");
                        timeChangeIntent.addFlags(536870912);
                        context.sendBroadcast(timeChangeIntent);
                        AlarmManagerService.this.mClockReceiver.scheduleTimeTickEvent();
                        AlarmManagerService.this.mClockReceiver.scheduleDateChangedEvent();
                        synchronized (AlarmManagerService.this.mWaitThreadlock) {
                            AlarmManagerService.this.mWaitThreadlock.notify();
                        }
                    }
                }
            }, filter);
        }
        publishBinderService("alarm", this.mService);
        publishLocalService(LocalService.class, new LocalService());
    }

    public boolean bootFromPoweroffAlarm() {
        String bootReason = SystemProperties.get("sys.boot.reason");
        return bootReason != null && bootReason.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mConstants.start(getContext().getContentResolver());
            this.mAppOps = (AppOpsManager) getContext().getSystemService("appops");
            this.mLocalDeviceIdleController = (com.android.server.DeviceIdleController.LocalService) LocalServices.getService(com.android.server.DeviceIdleController.LocalService.class);
            this.dataShapingManager = (IDataShapingManager) ServiceManager.getService("data_shaping");
            this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
            this.mPowerManagerInternal.registerColorOsLowPowerModeObserver(new LowPowerModeListener() {
                public void onLowPowerModeChanged(boolean enabled) {
                    AlarmManagerService.this.onLowPowerModeChangedInternal(enabled);
                }
            });
            this.mColorOsLowPowerModeEnabled.set(this.mPowerManagerInternal.getColorOsLowPowerModeEnabled());
            OppoProtectEyeManagerService.setActivityChangedListener(this.mActivityChangedListener);
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
                if (current == null || !current.equals(zone.getID())) {
                    if (localLOGV) {
                        Slog.v(TAG, "timezone changed: " + current + ", new=" + zone.getID());
                    }
                    timeZoneWasChanged = true;
                    SystemProperties.set(TIMEZONE_PROPERTY, zone.getID());
                }
                setKernelTimezone(this.mNativeData, -(zone.getOffset(System.currentTimeMillis()) / OppoBrightUtils.SPECIAL_AMBIENT_LIGHT_HORIZON));
            }
            TimeZone.setDefault(null);
            if (timeZoneWasChanged) {
                Intent intent = new Intent("android.intent.action.TIMEZONE_CHANGED");
                intent.addFlags(536870912);
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
        if ((operation == null && directReceiver == null) || (operation != null && directReceiver != null)) {
            Slog.w(TAG, "Alarms must either supply a PendingIntent or an AlarmReceiver");
        } else if (this.mIPOShutdown && this.mNativeData == -1) {
            Slog.w(TAG, "IPO Shutdown so drop the alarm");
        } else {
            long maxElapsed;
            PackageManager packageManager = getContext().getPackageManager();
            if (!(packageManager == null || packageManager.isFullFunctionMode())) {
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
            if (triggerAtTime < 0) {
                Slog.w(TAG, "Invalid alarm trigger time! " + triggerAtTime + " from uid=" + callingUid + " pid=" + ((long) Binder.getCallingPid()));
                triggerAtTime = 0;
            }
            if (type == 7 || type == 8) {
                if (this.mNativeData == -1) {
                    Slog.w(TAG, "alarm driver not open ,return!");
                    return;
                }
                Slog.d(TAG, "alarm set type 7 8, package name " + operation.getTargetPackage());
                String packageName = operation.getTargetPackage();
                long nowTime = System.currentTimeMillis();
                if (triggerAtTime < nowTime) {
                    Slog.w(TAG, "power off alarm set time is wrong! nowTime = " + nowTime + " ; triggerAtTime = " + triggerAtTime);
                    return;
                }
                synchronized (this.mPowerOffAlarmLock) {
                    long j;
                    removePoweroffAlarmLocked(operation.getTargetPackage());
                    int poweroffAlarmUserId = UserHandle.getCallingUserId();
                    if (triggerAtTime - nowTime > 60000) {
                        j = triggerAtTime - 60000;
                    } else {
                        j = triggerAtTime;
                    }
                    addPoweroffAlarmLocked(new Alarm(type, Long.valueOf(j).longValue(), 0, 0, 0, interval, operation, directReceiver, listenerTag, workSource, 0, alarmClock, poweroffAlarmUserId, callingPackage, true));
                    if (this.mPoweroffAlarms.size() > 0) {
                        resetPoweroffAlarm((Alarm) this.mPoweroffAlarms.get(0));
                    }
                }
                type = 0;
            }
            long nowElapsed = SystemClock.elapsedRealtime();
            long nominalTrigger = convertToElapsed(triggerAtTime, type);
            long minTrigger = nowElapsed + this.mConstants.MIN_FUTURITY;
            long triggerElapsed = nominalTrigger > minTrigger ? nominalTrigger : minTrigger;
            if (mSupportAlarmGrouping && this.mAmPlus != null) {
                maxElapsed = this.mAmPlus.getMaxTriggerTime(type, triggerElapsed, windowLength, interval, operation, mAlarmMode, true);
                if (maxElapsed < 0) {
                    maxElapsed = 0 - maxElapsed;
                    this.mNeedGrouping = false;
                } else {
                    this.mNeedGrouping = true;
                }
            } else if (windowLength == 0) {
                maxElapsed = triggerElapsed;
            } else if (windowLength < 0) {
                maxElapsed = maxTriggerTime(nowElapsed, triggerElapsed, interval);
                windowLength = maxElapsed - triggerElapsed;
            } else {
                maxElapsed = triggerElapsed + windowLength;
            }
            synchronized (this.mLock) {
                if (DEBUG_BATCH || DEBUG_DETAIL) {
                    if (operation == null) {
                        Slog.v(TAG, "APP set with listener(" + listenerTag + ") : type=" + type + " triggerAtTime=" + triggerAtTime + " win=" + windowLength + " tElapsed=" + triggerElapsed + " maxElapsed=" + maxElapsed + " interval=" + interval + " flags=0x" + Integer.toHexString(flags) + " hasPendingIdle = " + (this.mPendingIdleUntil != null) + " pkg = " + callingPackage);
                    } else {
                        Slog.v(TAG, "APP set(" + operation + ") : type=" + type + " triggerAtTime=" + triggerAtTime + " win=" + windowLength + " tElapsed=" + triggerElapsed + " maxElapsed=" + maxElapsed + " interval=" + interval + " flags=0x" + Integer.toHexString(flags) + " hasPendingIdle = " + (this.mPendingIdleUntil != null) + " pkg = " + callingPackage);
                    }
                }
                setImplLocked(type, triggerAtTime, triggerElapsed, windowLength, maxElapsed, interval, operation, directReceiver, listenerTag, flags, true, workSource, alarmClock, callingUid, callingPackage, this.mNeedGrouping);
            }
        }
    }

    private void setImplLocked(int type, long when, long whenElapsed, long windowLength, long maxWhen, long interval, PendingIntent operation, IAlarmListener directReceiver, String listenerTag, int flags, boolean doValidate, WorkSource workSource, AlarmClockInfo alarmClock, int callingUid, String callingPackage, boolean mNeedGrouping) {
        if (this.mAlarmWakeupDetection != null) {
            type = this.mAlarmWakeupDetection.SyncAlarmHandle(type, operation);
        }
        Alarm a = new Alarm(type, when, whenElapsed, windowLength, maxWhen, interval, operation, directReceiver, listenerTag, workSource, flags, alarmClock, callingUid, callingPackage, mNeedGrouping);
        if (1000 != callingUid) {
            try {
                if (ActivityManagerNative.getDefault().getAppStartMode(callingUid, callingPackage) == 2) {
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
        if (DEBUG_BATCH) {
            Slog.d(TAG, "a.whenElapsed =" + a.whenElapsed + " a.maxWhenElapsed= " + a.maxWhenElapsed + " a.needGrouping= " + a.needGrouping + "  a.flags= " + a.flags);
        }
        int whichBatch = (!mSupportAlarmGrouping || this.mAmPlus == null) ? (a.flags & 1) != 0 ? -1 : attemptCoalesceLocked(a.whenElapsed, a.maxWhenElapsed) : !a.needGrouping ? (a.flags & 1) != 0 ? -1 : attemptCoalesceLocked(a.whenElapsed, a.maxWhenElapsed) : attemptCoalesceLocked(a.whenElapsed, a.maxWhenElapsed);
        if (DEBUG_BATCH) {
            Slog.d(TAG, " whichBatch = " + whichBatch);
        }
        if (whichBatch < 0) {
            addBatchLocked(this.mAlarmBatches, new Batch(a));
        } else {
            Batch batch = (Batch) this.mAlarmBatches.get(whichBatch);
            if (DEBUG_BATCH) {
                Slog.d(TAG, " alarm = " + a + " add to " + batch);
            }
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
            this.mPendingIdleUntil = a;
            this.mConstants.updateAllowWhileIdleMinTimeLocked();
            needRebatch = true;
        } else if ((a.flags & 2) != 0 && (this.mNextWakeFromIdle == null || this.mNextWakeFromIdle.whenElapsed > a.whenElapsed)) {
            this.mNextWakeFromIdle = a;
            if (this.mPendingIdleUntil != null) {
                needRebatch = true;
            }
        }
        if (!(this.mOppoAlarmAlignment == null || !this.mOppoAlarmAlignment.mNeedRebatch || rebatching)) {
            this.mOppoAlarmAlignment.mNeedRebatch = false;
            rebatchAllAlarmsLocked(false);
            needRebatch = false;
        }
        if (!rebatching) {
            if (DEBUG_VALIDATE && doValidate && !validateConsistencyLocked()) {
                Slog.v(TAG, "Tipping-point operation: type=" + a.type + " when=" + a.when + " when(hex)=" + Long.toHexString(a.when) + " whenElapsed=" + a.whenElapsed + " maxWhenElapsed=" + a.maxWhenElapsed + " interval=" + a.repeatInterval + " op=" + a.operation + " flags=0x" + Integer.toHexString(a.flags));
                rebatchAllAlarmsLocked(false);
                needRebatch = false;
            }
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
            localLOGV = true;
            DEBUG_BATCH = true;
            DEBUG_VALIDATE = true;
            DEBUG_PENDING = true;
            DEBUG_DETAIL = true;
        } else if ("off".equals(args[opti])) {
            localLOGV = false;
            DEBUG_BATCH = false;
            DEBUG_VALIDATE = false;
            DEBUG_PENDING = false;
            DEBUG_DETAIL = false;
        } else if ("0".equals(args[opti])) {
            mAlarmMode = 0;
            Slog.v(TAG, "mAlarmMode = " + mAlarmMode);
        } else if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[opti])) {
            mAlarmMode = 1;
            Slog.v(TAG, "mAlarmMode = " + mAlarmMode);
        } else if ("2".equals(args[opti])) {
            mAlarmMode = 2;
            Slog.v(TAG, "mAlarmMode = " + mAlarmMode);
        } else {
            pw.println("  Invalid argument!");
        }
    }

    void dumpImpl(PrintWriter pw, String[] args) {
        int opti = 0;
        while (opti < args.length) {
            String opt = args[opti];
            if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                break;
            }
            opti++;
            if ("-h".equals(opt)) {
                pw.println("alarm manager dump options:");
                pw.println("  log  [on/off]");
                pw.println("  Example:");
                pw.println("  $adb shell dumpsys alarm log on");
                pw.println("  $adb shell dumpsys alarm log off");
                return;
            }
            pw.println("Unknown argument: " + opt + "; use -h for help");
        }
        if (opti < args.length) {
            String cmd = args[opti];
            opti++;
            if ("log".equals(cmd)) {
                configLogTag(pw, args, opti);
                return;
            }
        }
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
            Comparator<FilterStats> anonymousClass6 = new Comparator<FilterStats>() {
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
                        int pos = len > 0 ? Arrays.binarySearch(topFilters, 0, len, fs, anonymousClass6) : 0;
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
                    Collections.sort(tmpFilters, anonymousClass6);
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
        if (DEBUG_VALIDATE) {
            long lastTime = Long.MIN_VALUE;
            int N = this.mAlarmBatches.size();
            int i = 0;
            while (i < N) {
                Batch b = (Batch) this.mAlarmBatches.get(i);
                if (b.start >= lastTime) {
                    lastTime = b.start;
                    i++;
                } else {
                    Slog.e(TAG, "CONSISTENCY FAILURE: Batch " + i + " is out of order");
                    logBatchesLocked(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                    return false;
                }
            }
        }
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
        Slog.d(TAG, "getNextAlarmClockImpl is called before Lock ");
        synchronized (this.mLock) {
            Slog.d(TAG, "getNextAlarmClockImpl is called in Lock ");
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
                        if (DEBUG_ALARM_CLOCK) {
                            Log.v(TAG, "Found AlarmClockInfo " + a.alarmClock + " at " + formatNextAlarm(getContext(), a.alarmClock, userId) + " for user " + userId);
                        }
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
            if (DEBUG_ALARM_CLOCK) {
                Log.v(TAG, "Next AlarmClockInfoForUser(" + userId + "): " + formatNextAlarm(getContext(), alarmClock, userId));
            }
            this.mNextAlarmClockForUser.put(userId, alarmClock);
        } else {
            if (DEBUG_ALARM_CLOCK) {
                Log.v(TAG, "Next AlarmClockInfoForUser(" + userId + "): None");
            }
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
        Slog.w(TAG, "sendNextAlarmClockChanged begin");
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
        Slog.w(TAG, "sendNextAlarmClockChanged end");
    }

    private static String formatNextAlarm(Context context, AlarmClockInfo info, int userId) {
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context, userId) ? "EHm" : "Ehma");
        if (info == null) {
            return IElsaManager.EMPTY_PACKAGE;
        }
        return DateFormat.format(pattern, info.getTriggerTime()).toString();
    }

    void rescheduleKernelAlarmsLocked() {
        if (this.mIPOShutdown && this.mNativeData == -1) {
            Slog.w(TAG, "IPO Shutdown so drop the repeating alarm");
            return;
        }
        long nextNonWakeup = 0;
        if (this.mAlarmBatches.size() > 0) {
            Batch firstWakeup = findFirstWakeupBatchLocked();
            Batch firstBatch = (Batch) this.mAlarmBatches.get(0);
            if (!(firstWakeup == null || this.mNextWakeup == firstWakeup.start)) {
                this.mNextWakeup = firstWakeup.start;
                this.mLastWakeupSet = SystemClock.elapsedRealtime();
                setLocked(2, firstWakeup.start);
            }
            if (firstBatch != firstWakeup) {
                nextNonWakeup = firstBatch.start;
            }
        }
        if ((this.mPendingNonWakeupAlarms.size() > 0 || this.mPendingImportantNonWakeupAlarms.size() > 0) && (nextNonWakeup == 0 || this.mNextNonWakeupDeliveryTime < nextNonWakeup)) {
            nextNonWakeup = this.mNextNonWakeupDeliveryTime;
        }
        if (!(nextNonWakeup == 0 || this.mNextNonWakeup == nextNonWakeup)) {
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
            if (DEBUG_BATCH) {
                Slog.d(TAG, "remove(operation) changed bounds; rebatching operation = " + operation);
            }
            boolean restorePending = false;
            if (this.mPendingIdleUntil != null && this.mPendingIdleUntil.matches(operation, directReceiver)) {
                this.mPendingIdleUntil = null;
                restorePending = true;
            }
            if (this.mNextWakeFromIdle != null && this.mNextWakeFromIdle.matches(operation, directReceiver)) {
                this.mNextWakeFromIdle = null;
            }
            if (this.mAlarmBatches.size() < 300) {
                rebatchAllAlarmsLocked(true);
            } else {
                Slog.d(TAG, "mAlarmBatches.size() is larger than 300 , do not rebatch");
            }
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
            Slog.v(TAG, "remove(package) changed bounds; rebatching");
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
            Alarm a = (Alarm) this.mPendingWhileIdleAlarms.get(i);
            try {
                if (a.uid == uid && ActivityManagerNative.getDefault().getAppStartMode(uid, a.packageName) == 2) {
                    this.mPendingWhileIdleAlarms.remove(i);
                }
            } catch (RemoteException e) {
            }
        }
        if (didRemove != 0) {
            if (DEBUG_BATCH) {
                Slog.v(TAG, "remove(package) changed bounds; rebatching");
            }
            rebatchAllAlarmsLocked(true);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
        }
    }

    boolean removeInvalidAlarmLocked(PendingIntent operation, IAlarmListener listener) {
        boolean didRemove = false;
        for (int i = this.mAlarmBatches.size() - 1; i >= 0; i--) {
            Batch b = (Batch) this.mAlarmBatches.get(i);
            didRemove |= b.remove(operation, listener);
            if (b.size() == 0) {
                this.mAlarmBatches.remove(i);
            }
        }
        return didRemove;
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
            if (DEBUG_BATCH) {
                Slog.v(TAG, "remove(user) changed bounds; rebatching");
            }
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
                setImplLocked(i, alarm.when + delta, j, alarm.windowLength, maxTriggerTime(nowELAPSED, nextElapsed, alarm.repeatInterval), alarm.repeatInterval, alarm.operation, null, null, alarm.flags, true, alarm.workSource, alarm.alarmClock, alarm.uid, alarm.packageName, alarm.needGrouping);
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
        if (this.mNativeData == 0 || this.mNativeData == -1) {
            Slog.d(TAG, "the mNativeData from RTC is abnormal,  mNativeData = " + this.mNativeData);
            Message msg = Message.obtain();
            msg.what = 1;
            this.mHandler.removeMessages(1);
            this.mHandler.sendMessageAtTime(msg, when);
            return;
        }
        long alarmSeconds;
        long alarmNanoseconds;
        if (when < 0) {
            alarmSeconds = 0;
            alarmNanoseconds = 0;
        } else {
            alarmSeconds = when / 1000;
            alarmNanoseconds = ((when % 1000) * 1000) * 1000;
        }
        if (DEBUG_DETAIL) {
            Slog.d(TAG, "set alarm to RTC " + when + " Type: " + type);
        }
        set(this.mNativeData, type, alarmSeconds, alarmNanoseconds);
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
        int i;
        boolean hasWakeup = false;
        while (this.mAlarmBatches.size() > 0) {
            Batch batch = (Batch) this.mAlarmBatches.get(0);
            if (batch.start > nowELAPSED) {
                break;
            }
            this.mAlarmBatches.remove(0);
            int N = batch.size();
            for (i = 0; i < N; i++) {
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
                        alarm.whenElapsedAllowWhileIdle = minTime;
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
                    setImplLocked(alarm.type, alarm.when + delta, nextElapsed, alarm.windowLength, maxTriggerTime(nowELAPSED, nextElapsed, alarm.repeatInterval), alarm.repeatInterval, alarm.operation, null, null, alarm.flags, true, alarm.workSource, alarm.alarmClock, alarm.uid, alarm.packageName, alarm.needGrouping);
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
        if (DEBUG_DETAIL) {
            Slog.v(TAG, "triggerAlarms: nowELAPSED=" + nowELAPSED);
            for (i = 0; i < triggerList.size(); i++) {
                Slog.v(TAG, "triggerAlarms: Triggering alarm #" + i + ": " + triggerList.get(i));
            }
            for (i = 0; i < triggerListNonWakeup.size(); i++) {
                Slog.v(TAG, "triggerAlarms: NonWakeup Triggering alarm #" + i + ": " + triggerListNonWakeup.get(i));
            }
        }
        return hasWakeup;
    }

    boolean triggerAlarmsLocked(ArrayList<Alarm> triggerList, long nowELAPSED, long nowRTC) {
        int i;
        boolean hasWakeup = false;
        while (this.mAlarmBatches.size() > 0) {
            Batch batch = (Batch) this.mAlarmBatches.get(0);
            if (batch.start > nowELAPSED) {
                break;
            }
            this.mAlarmBatches.remove(0);
            int N = batch.size();
            for (i = 0; i < N; i++) {
                Alarm alarm = batch.get(i);
                if ((alarm.flags & 4) != 0) {
                    long lastTime = this.mLastAllowWhileIdleDispatch.get(alarm.uid, 0);
                    long minTime = lastTime + this.mAllowWhileIdleMinTime;
                    if (nowELAPSED < minTime) {
                        if (DEBUG_PANIC) {
                            Slog.d(TAG, "triggerAlarmsLocked: " + alarm + " too frequent, last = " + lastTime + ", now = " + nowELAPSED);
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
                    long maxElapsed;
                    alarm.count = (int) (((long) alarm.count) + ((nowELAPSED - alarm.whenElapsed) / alarm.repeatInterval));
                    long delta = ((long) alarm.count) * alarm.repeatInterval;
                    long nextElapsed = alarm.whenElapsed + delta;
                    if (!mSupportAlarmGrouping || this.mAmPlus == null) {
                        maxElapsed = maxTriggerTime(nowELAPSED, nextElapsed, alarm.repeatInterval);
                    } else {
                        maxElapsed = this.mAmPlus.getMaxTriggerTime(alarm.type, nextElapsed, alarm.windowLength, alarm.repeatInterval, alarm.operation, mAlarmMode, true);
                    }
                    alarm.needGrouping = true;
                    setImplLocked(alarm.type, alarm.when + delta, nextElapsed, alarm.windowLength, maxElapsed, alarm.repeatInterval, alarm.operation, null, null, alarm.flags, true, alarm.workSource, alarm.alarmClock, alarm.uid, alarm.packageName, alarm.needGrouping);
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
        if (localLOGV) {
            for (i = 0; i < triggerList.size(); i++) {
                Slog.v(TAG, "Triggering alarm #" + i + ": " + triggerList.get(i));
            }
        }
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
        if (timeSinceOn < 300000) {
            return JobStatus.DEFAULT_TRIGGER_MAX_DELAY;
        }
        if (timeSinceOn < ColorOSDeviceIdleHelper.DEFAULT_TOTAL_INTERVAL_TO_IDLE) {
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
        if (this.mIsWFDConnected) {
            Slog.v(TAG, "[Digvijay]checkAllowNonWakeupDelayLocked return FALSE WFD connected");
            return false;
        } else if ((this.mPendingNonWakeupAlarms.size() > 0 || this.mPendingImportantNonWakeupAlarms.size() > 0) && this.mNextNonWakeupDeliveryTime < nowELAPSED) {
            return false;
        } else {
            if (nowELAPSED - this.mLastAlarmDeliveryTime <= currentNonWakeupFuzzLocked(nowELAPSED)) {
                z = true;
            }
            return z;
        }
    }

    /* JADX WARNING: Missing block: B:31:0x00da, code:
            if (android.os.SystemProperties.get("ro.mtk_ipo_support").equals(com.android.server.LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) == false) goto L_0x00e9;
     */
    /* JADX WARNING: Missing block: B:33:0x00e0, code:
            if (r22.mIPOShutdown == false) goto L_0x00e9;
     */
    /* JADX WARNING: Missing block: B:40:0x00eb, code:
            if (DEBUG_DETAIL == false) goto L_0x0176;
     */
    /* JADX WARNING: Missing block: B:41:0x00ed, code:
            android.util.Slog.v(TAG, "sending alarm " + r4);
     */
    /* JADX WARNING: Missing block: B:43:0x0109, code:
            if (r4.type == 0) goto L_0x0110;
     */
    /* JADX WARNING: Missing block: B:45:0x010e, code:
            if (r4.type != 2) goto L_0x014c;
     */
    /* JADX WARNING: Missing block: B:47:0x0112, code:
            if (r4.operation != null) goto L_0x01a6;
     */
    /* JADX WARNING: Missing block: B:49:0x0116, code:
            if (DEBUG_PANIC == false) goto L_0x014c;
     */
    /* JADX WARNING: Missing block: B:50:0x0118, code:
            android.util.Slog.d(TAG, "wakeup alarm = " + r4 + "; listener package = " + r4.listenerTag + "needGrouping = " + r4.needGrouping);
     */
    /* JADX WARNING: Missing block: B:52:0x014e, code:
            if (r4.workSource == null) goto L_0x01e4;
     */
    /* JADX WARNING: Missing block: B:54:0x0156, code:
            if (r4.workSource.size() <= 0) goto L_0x01e4;
     */
    /* JADX WARNING: Missing block: B:55:0x0158, code:
            r20 = 0;
     */
    /* JADX WARNING: Missing block: B:57:0x0162, code:
            if (r20 >= r4.workSource.size()) goto L_0x01ed;
     */
    /* JADX WARNING: Missing block: B:58:0x0164, code:
            android.app.ActivityManagerNative.noteAlarmStart(r4.operation, r4.workSource.get(r20), r4.statsTag);
            r20 = r20 + 1;
     */
    /* JADX WARNING: Missing block: B:60:0x0178, code:
            if (DEBUG_PANIC == false) goto L_0x0107;
     */
    /* JADX WARNING: Missing block: B:61:0x017a, code:
            android.util.Slog.v(TAG, "sending " + r4.toStringLite());
     */
    /* JADX WARNING: Missing block: B:62:0x019a, code:
            r13 = move-exception;
     */
    /* JADX WARNING: Missing block: B:63:0x019b, code:
            android.util.Slog.w(TAG, "Failure sending alarm.", r13);
     */
    /* JADX WARNING: Missing block: B:66:0x01a8, code:
            if (DEBUG_PANIC == false) goto L_0x014c;
     */
    /* JADX WARNING: Missing block: B:67:0x01aa, code:
            android.util.Slog.d(TAG, "wakeup alarm = " + r4 + "; package = " + r4.operation.getTargetPackage() + "needGrouping = " + r4.needGrouping);
     */
    /* JADX WARNING: Missing block: B:68:0x01e4, code:
            android.app.ActivityManagerNative.noteAlarmStart(r4.operation, r4.uid, r4.statsTag);
     */
    /* JADX WARNING: Missing block: B:70:0x01ef, code:
            if (DEBUG_PANIC == false) goto L_0x01f7;
     */
    /* JADX WARNING: Missing block: B:72:0x01f5, code:
            if (r22.mInteractive == false) goto L_0x0202;
     */
    /* JADX WARNING: Missing block: B:73:0x01f7, code:
            r22.mDeliveryTracker.deliverLocked(r4, r24, r11);
     */
    /* JADX WARNING: Missing block: B:75:0x0205, code:
            if (r4.type == 2) goto L_0x020b;
     */
    /* JADX WARNING: Missing block: B:77:0x0209, code:
            if (r4.type != 0) goto L_0x01f7;
     */
    /* JADX WARNING: Missing block: B:79:0x020d, code:
            if (r4.operation == null) goto L_0x0264;
     */
    /* JADX WARNING: Missing block: B:81:0x0219, code:
            if (r4.operation.getIntent().getAction() == null) goto L_0x0264;
     */
    /* JADX WARNING: Missing block: B:82:0x021b, code:
            r15 = r4.operation.getIntent().getAction();
     */
    /* JADX WARNING: Missing block: B:83:0x022d, code:
            if (r22.mTriggeredAlarmMap.containsKey(r15) == false) goto L_0x024b;
     */
    /* JADX WARNING: Missing block: B:84:0x022f, code:
            r22.mTriggeredAlarmMap.put(r15, java.lang.Integer.valueOf(((java.lang.Integer) r22.mTriggeredAlarmMap.get(r15)).intValue() + 1));
     */
    /* JADX WARNING: Missing block: B:86:0x0255, code:
            if (r22.mTriggeredAlarmMap.size() >= 1000) goto L_0x01f7;
     */
    /* JADX WARNING: Missing block: B:87:0x0257, code:
            r22.mTriggeredAlarmMap.put(r15, java.lang.Integer.valueOf(1));
     */
    /* JADX WARNING: Missing block: B:89:0x0266, code:
            if (r4.operation == null) goto L_0x01f7;
     */
    /* JADX WARNING: Missing block: B:91:0x026e, code:
            if (r4.operation.getTargetPackage() == null) goto L_0x01f7;
     */
    /* JADX WARNING: Missing block: B:92:0x0270, code:
            r15 = r4.operation.getTargetPackage();
     */
    /* JADX WARNING: Missing block: B:93:0x027e, code:
            if (r22.mTriggeredAlarmMap.containsKey(r15) == false) goto L_0x029d;
     */
    /* JADX WARNING: Missing block: B:94:0x0280, code:
            r22.mTriggeredAlarmMap.put(r15, java.lang.Integer.valueOf(((java.lang.Integer) r22.mTriggeredAlarmMap.get(r15)).intValue() + 1));
     */
    /* JADX WARNING: Missing block: B:96:0x02a7, code:
            if (r22.mTriggeredAlarmMap.size() >= 1000) goto L_0x01f7;
     */
    /* JADX WARNING: Missing block: B:97:0x02a9, code:
            r22.mTriggeredAlarmMap.put(r15, java.lang.Integer.valueOf(1));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void deliverAlarmsLocked(ArrayList<Alarm> triggerList, long nowELAPSED) {
        suspendAlarmPowerSaveModeLocked(triggerList);
        this.mLastAlarmDeliveryTime = nowELAPSED;
        long nowRTC = System.currentTimeMillis();
        this.mNeedRebatchForRepeatingAlarm = false;
        boolean openLteGateSuccess = false;
        if (this.dataShapingManager != null) {
            try {
                openLteGateSuccess = this.dataShapingManager.openLteDataUpLinkGate(false);
            } catch (Exception e) {
                Log.e(TAG, "Error openLteDataUpLinkGate false" + e);
            }
        } else {
            Slog.v(TAG, "dataShapingManager is null");
        }
        if (DEBUG_PANIC) {
            Slog.v(TAG, "openLteGateSuccess = " + openLteGateSuccess);
        }
        int i = 0;
        while (i < triggerList.size()) {
            Alarm alarm = (Alarm) triggerList.get(i);
            boolean allowWhileIdle = (alarm.flags & 4) != 0;
            updatePoweroffAlarm(nowRTC);
            synchronized (this.mDMLock) {
                if (!(this.mDMEnable && this.mPPLEnable)) {
                    FreeDmIntent(triggerList, this.mDmFreeList, nowELAPSED, this.mDmResendList);
                }
            }
        }
        if (this.mNeedRebatchForRepeatingAlarm) {
            Slog.v(TAG, " deliverAlarmsLocked removeInvalidAlarmLocked then rebatch ");
            rebatchAllAlarmsLocked(true);
            rescheduleKernelAlarmsLocked();
            updateNextAlarmClockLocked();
            return;
        }
        return;
        i++;
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
                uid = ActivityManagerNative.getDefault().getUidForIntentSender(pi.getTarget());
            }
            if (uid >= 0) {
                this.mWakeLock.setWorkSource(new WorkSource(uid));
                return;
            }
            this.mWakeLock.setWorkSource(null);
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
        setImplLocked(type, when, whenElapsed, windowLength, maxWhen, interval, operation, null, null, flags, doValidate, workSource, alarmClock, Binder.getCallingUid(), "android", this.mNeedGrouping);
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
            if (localLOGV || DEBUG_PANIC) {
                Slog.v("OppoProcessManager", "Befor delivering alarm check purbackground state : Now is in StrictMode !");
            }
            if (triggerList.size() > 0) {
                Iterator<Alarm> triggerListIter = triggerList.iterator();
                Boolean isHasSameRepeatAlarm = Boolean.valueOf(false);
                while (triggerListIter.hasNext()) {
                    Alarm alarm = (Alarm) triggerListIter.next();
                    if (OppoProcessManagerHelper.isDelayAppAlarm(alarm.callingPid, alarm.callingUid, alarm.creatorUid, alarm.operation != null ? alarm.operation.getTargetPackage() : alarm.packageName)) {
                        Slog.v("OppoProcessManager", "The alarm delayed because of purbackground is " + alarm);
                        if (alarm.repeatInterval <= 0 || alarm.listener != null || alarm.operation == null || this.mDelayedForPurBackgroundAlarms.size() <= 0) {
                            this.mDelayedForPurBackgroundAlarms.add(alarm);
                        } else {
                            isHasSameRepeatAlarm = Boolean.valueOf(false);
                            for (int i = 0; i < this.mDelayedForPurBackgroundAlarms.size(); i++) {
                                Alarm delayedAlarm = (Alarm) this.mDelayedForPurBackgroundAlarms.get(i);
                                if (delayedAlarm.repeatInterval > 0 && delayedAlarm.repeatInterval == alarm.repeatInterval && delayedAlarm.operation != null && delayedAlarm.operation.equals(alarm.operation) && delayedAlarm.type == alarm.type && delayedAlarm.packageName.equals(alarm.packageName)) {
                                    isHasSameRepeatAlarm = Boolean.valueOf(true);
                                    Slog.v("OppoProcessManager", "The alarm is a repeat alarm and it is already in DelayedForPurBackgroundAlarms list !");
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
            } else if (localLOGV || DEBUG_PANIC) {
                Slog.v("OppoProcessManager", "Befor delivering alarm check purbackground state: Now is in StrictMode,But the triggerList is Null !");
            }
        }
    }

    public void stopStrictMode() {
        this.mHandler.removeMessages(6);
        this.mHandler.sendEmptyMessage(6);
    }

    private void stopStrictModeLocked() {
        if (this.mDelayedForPurBackgroundAlarms.size() > 0) {
            if (localLOGV || DEBUG_PANIC) {
                Slog.v("OppoProcessManager", "StopStrictMode ! And the mDelayedForPurBackgroundAlarms is not null,so deliver these Alarms !");
            }
            deliverAlarmsLocked(this.mDelayedForPurBackgroundAlarms, SystemClock.elapsedRealtime());
            this.mDelayedForPurBackgroundAlarms.clear();
        } else if (localLOGV || DEBUG_PANIC) {
            Slog.v("OppoProcessManager", "StopStrictMode ! And the mDelayedForPurBackgroundAlarms is null !");
        }
    }

    private boolean isSystemApp(int uid, String pkgName) {
        if (uid < 10000) {
            return true;
        }
        PackageManager packageManager = getContext().getPackageManager();
        if (packageManager == null || packageManager.isFullFunctionMode()) {
            return true;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = packageManager.getApplicationInfo(pkgName, DumpState.DUMP_PREFERRED_XML);
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
                    if (!(isSameAlarm(alarm) || this.mListPowerSaveSuspend.contains(alarm))) {
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

    public int enableDm() {
        synchronized (this.mDMLock) {
            if (this.mDMEnable && this.mPPLEnable) {
                resendDmPendingList(this.mDmResendList);
                this.mDmResendList = null;
                this.mDmResendList = new ArrayList();
            }
        }
        return -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x0006 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0176 A:{Catch:{ CanceledException -> 0x018c }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void FreeDmIntent(ArrayList<Alarm> triggerList, ArrayList<PendingIntent> mDmFreeList, long nowELAPSED, ArrayList<Alarm> resendList) {
        Iterator<Alarm> it = triggerList.iterator();
        while (it.hasNext()) {
            boolean isFreeIntent = false;
            Alarm alarm = (Alarm) it.next();
            if (alarm.operation == null) {
                Slog.v(TAG, "FreeDmIntent skip with null operation APP listener(" + alarm.listenerTag + ") : type = " + alarm.type + " triggerAtTime = " + alarm.when);
            } else {
                int i = 0;
                while (i < mDmFreeList.size()) {
                    try {
                        if (alarm.operation.equals(mDmFreeList.get(i))) {
                            if (localLOGV) {
                                Slog.v(TAG, "sending alarm " + alarm);
                            }
                            alarm.operation.send(getContext(), 0, this.mBackgroundIntent.putExtra("android.intent.extra.ALARM_COUNT", alarm.count), this.mDeliveryTracker, this.mHandler);
                            if (this.mBroadcastRefCount == 0) {
                                setWakelockWorkSource(alarm.operation, alarm.workSource, alarm.type, alarm.statsTag, alarm.uid, true);
                                this.mWakeLock.acquire();
                            }
                            InFlight inflight = new InFlight(this, alarm.operation, alarm.listener, alarm.workSource, alarm.uid, alarm.packageName, alarm.type, alarm.statsTag, 0);
                            this.mInFlight.add(inflight);
                            this.mBroadcastRefCount++;
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
                            }
                            isFreeIntent = true;
                            if (isFreeIntent) {
                                resendList.add(alarm);
                            }
                        } else {
                            i++;
                        }
                    } catch (CanceledException e) {
                        if (alarm.repeatInterval > 0) {
                        }
                    }
                }
                if (isFreeIntent) {
                }
            }
        }
    }

    private void resendDmPendingList(ArrayList<Alarm> DmResendList) {
        Iterator<Alarm> it = DmResendList.iterator();
        while (it.hasNext()) {
            Alarm alarm = (Alarm) it.next();
            if (alarm.operation == null) {
                Slog.v(TAG, "resendDmPendingList skip with null operation, APP listener(" + alarm.listenerTag + ") : type = " + alarm.type + " triggerAtTime = " + alarm.when);
            } else {
                try {
                    if (localLOGV) {
                        Slog.v(TAG, "sending alarm " + alarm);
                    }
                    alarm.operation.send(getContext(), 0, this.mBackgroundIntent.putExtra("android.intent.extra.ALARM_COUNT", alarm.count), this.mDeliveryTracker, this.mHandler);
                    if (this.mBroadcastRefCount == 0) {
                        setWakelockWorkSource(alarm.operation, alarm.workSource, alarm.type, alarm.statsTag, alarm.uid, true);
                        this.mWakeLock.acquire();
                    }
                    InFlight inflight = new InFlight(this, alarm.operation, alarm.listener, alarm.workSource, alarm.uid, alarm.packageName, alarm.type, alarm.statsTag, 0);
                    this.mInFlight.add(inflight);
                    this.mBroadcastRefCount++;
                    BroadcastStats bs = inflight.mBroadcastStats;
                    bs.count++;
                    if (bs.nesting == 0) {
                        bs.nesting = 1;
                        bs.startTime = SystemClock.elapsedRealtime();
                    } else {
                        bs.nesting++;
                    }
                    FilterStats fs = inflight.mFilterStats;
                    fs.count++;
                    if (fs.nesting == 0) {
                        fs.nesting = 1;
                        fs.startTime = SystemClock.elapsedRealtime();
                    } else {
                        fs.nesting++;
                    }
                    if (alarm.type == 2 || alarm.type == 0) {
                        bs.numWakeup++;
                        fs.numWakeup++;
                    }
                } catch (CanceledException e) {
                    if (alarm.repeatInterval > 0) {
                    }
                }
            }
        }
    }

    private boolean isBootFromAlarm(int fd) {
        return bootFromAlarm(fd);
    }

    /* JADX WARNING: Missing block: B:23:0x004b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updatePoweroffAlarm(long nowRTC) {
        synchronized (this.mPowerOffAlarmLock) {
            if (this.mPoweroffAlarms.size() == 0) {
            } else if (((Alarm) this.mPoweroffAlarms.get(0)).when > nowRTC) {
            } else {
                Iterator<Alarm> it = this.mPoweroffAlarms.iterator();
                while (it.hasNext() && ((Alarm) it.next()).when <= nowRTC) {
                    Slog.w(TAG, "power off alarm update deleted");
                    it.remove();
                }
                if (this.mPoweroffAlarms.size() > 0) {
                    resetPoweroffAlarm((Alarm) this.mPoweroffAlarms.get(0));
                }
            }
        }
    }

    private int addPoweroffAlarmLocked(Alarm alarm) {
        ArrayList<Alarm> alarmList = this.mPoweroffAlarms;
        int index = Collections.binarySearch(alarmList, alarm, sIncreasingTimeOrder);
        if (index < 0) {
            index = (0 - index) - 1;
        }
        if (localLOGV) {
            Slog.v(TAG, "Adding alarm " + alarm + " at " + index);
        }
        alarmList.add(index, alarm);
        if (localLOGV) {
            Slog.v(TAG, "alarms: " + alarmList.size() + " type: " + alarm.type);
            int position = 0;
            for (Alarm a : alarmList) {
                Time time = new Time();
                time.set(a.when);
                Slog.v(TAG, position + ": " + time.format("%b %d %I:%M:%S %p") + " " + a.operation.getTargetPackage());
                position++;
            }
        }
        return index;
    }

    private void removePoweroffAlarmLocked(String packageName) {
        ArrayList<Alarm> alarmList = this.mPoweroffAlarms;
        if (alarmList.size() > 0) {
            Iterator<Alarm> it = alarmList.iterator();
            while (it.hasNext()) {
                if (((Alarm) it.next()).operation.getTargetPackage().equals(packageName)) {
                    it.remove();
                }
            }
        }
    }

    private void resetPoweroffAlarm(Alarm alarm) {
        String setPackageName = alarm.operation.getTargetPackage();
        long latestTime = alarm.when;
        if (this.mNativeData == 0 || this.mNativeData == -1) {
            Slog.i(TAG, " do not set alarm to RTC when fd close ");
            return;
        }
        if (setPackageName.equals("com.android.deskclock")) {
            Slog.i(TAG, "mBootPackage = " + setPackageName + " set Prop 1");
            SystemProperties.set("persist.sys.bootpackage", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            set(this.mNativeData, 6, latestTime / 1000, ((latestTime % 1000) * 1000) * 1000);
        } else if (setPackageName.equals("com.mediatek.schpwronoff")) {
            Slog.i(TAG, "mBootPackage = " + setPackageName + " set Prop 2");
            SystemProperties.set("persist.sys.bootpackage", "2");
            set(this.mNativeData, 7, latestTime / 1000, ((latestTime % 1000) * 1000) * 1000);
        } else if (setPackageName.equals("com.mediatek.poweronofftest")) {
            Slog.i(TAG, "mBootPackage = " + setPackageName + " set Prop 2");
            SystemProperties.set("persist.sys.bootpackage", "2");
            set(this.mNativeData, 7, latestTime / 1000, ((latestTime % 1000) * 1000) * 1000);
        } else if (setPackageName.equals("com.coloros.alarmclock")) {
            Slog.i(TAG, "mBootPackage = " + setPackageName + " set Prop 1");
            SystemProperties.set("persist.sys.bootpackage", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            set(this.mNativeData, 7, latestTime / 1000, ((latestTime % 1000) * 1000) * 1000);
        } else if (setPackageName.equals("com.android.settings") || setPackageName.equals("com.oppo.engineermode")) {
            Slog.i(TAG, "mBootPackage = " + setPackageName + " set Prop 2");
            SystemProperties.set("persist.sys.bootpackage", "2");
            set(this.mNativeData, 7, latestTime / 1000, ((latestTime % 1000) * 1000) * 1000);
        } else {
            Slog.w(TAG, "unknown package (" + setPackageName + ") to set power off alarm");
        }
        Slog.i(TAG, "reset power off alarm is " + setPackageName);
        SystemProperties.set("sys.power_off_alarm", Long.toString(latestTime / 1000));
        Slog.i(TAG, "sys.power_off_alarm is " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(latestTime)));
    }

    public void cancelPoweroffAlarmImpl(String name) {
        Slog.i(TAG, "remove power off alarm pacakge name " + name);
        synchronized (this.mPowerOffAlarmLock) {
            removePoweroffAlarmLocked(name);
            String bootReason = SystemProperties.get("persist.sys.bootpackage");
            if (!(bootReason == null || this.mNativeData == 0 || this.mNativeData == -1)) {
                if (bootReason.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && name.equals("com.android.deskclock")) {
                    set(this.mNativeData, 6, 0, 0);
                    SystemProperties.set("sys.power_off_alarm", Long.toString(0));
                } else if (bootReason.equals("2") && (name.equals("com.mediatek.schpwronoff") || name.equals("com.mediatek.poweronofftest"))) {
                    set(this.mNativeData, 7, 0, 0);
                    SystemProperties.set("sys.power_off_alarm", Long.toString(0));
                } else if (name.equals("com.coloros.alarmclock")) {
                    set(this.mNativeData, 6, 0, 0);
                    SystemProperties.set("sys.power_off_alarm", Long.toString(0));
                } else if (name.equals("com.android.settings") || name.equals("com.oppo.engineermode")) {
                    set(this.mNativeData, 7, 0, 0);
                    SystemProperties.set("sys.power_off_alarm", Long.toString(0));
                }
            }
            if (this.mPoweroffAlarms.size() > 0) {
                resetPoweroffAlarm((Alarm) this.mPoweroffAlarms.get(0));
            }
        }
    }

    private void shutdownCheckPoweroffAlarm() {
        Slog.i(TAG, "into shutdownCheckPoweroffAlarm()!!");
        long nowTime = System.currentTimeMillis();
        synchronized (this.mPowerOffAlarmLock) {
            Alarm alarm;
            long latestTime;
            Iterator<Alarm> it = this.mPoweroffAlarms.iterator();
            ArrayList<Alarm> mTempPoweroffAlarms = new ArrayList();
            while (it.hasNext()) {
                alarm = (Alarm) it.next();
                latestTime = alarm.when;
                String setPackageName = alarm.operation.getTargetPackage();
                if (latestTime - 30000 <= nowTime) {
                    Slog.i(TAG, "get target latestTime < 30S!!");
                    mTempPoweroffAlarms.add(alarm);
                }
            }
            Iterator<Alarm> tempIt = mTempPoweroffAlarms.iterator();
            while (tempIt.hasNext()) {
                alarm = (Alarm) tempIt.next();
                latestTime = alarm.when;
                if (!(this.mNativeData == 0 || this.mNativeData == -1)) {
                    set(this.mNativeData, alarm.type, latestTime / 1000, ((latestTime % 1000) * 1000) * 1000);
                }
            }
        }
        Slog.i(TAG, "away shutdownCheckPoweroffAlarm()!!");
    }

    public void removeFromAmsImpl(String packageName) {
        if (packageName != null) {
            synchronized (this.mLock) {
                removeLocked(packageName);
            }
        }
    }

    public boolean lookForPackageFromAmsImpl(String packageName) {
        if (packageName == null) {
            return false;
        }
        boolean lookForPackageLocked;
        synchronized (this.mLock) {
            lookForPackageLocked = lookForPackageLocked(packageName);
        }
        return lookForPackageLocked;
    }
}
