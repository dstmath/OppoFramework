package com.android.server;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IAlarmListener;
import android.app.PendingIntent;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.AlarmManagerService;
import com.android.server.AlarmUpdateHelper;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.am.IColorHansManager;
import com.android.server.am.IColorResourcePreloadManager;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.IColorFullmodeManager;
import com.android.server.utils.PriorityDump;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class OppoBaseAlarmManagerService extends SystemService {
    protected static boolean DEBUG_DETAIL = false;
    protected static boolean DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    protected static boolean DEBUG_PENDING = false;
    public static final int DELAY_UNBLOCK_UNRESTRICTED_ALARMS = 300;
    private static final long MIN_FUZZABLE_INTERVAL = 10000;
    public static final int MSG_PEND_WHILE_IDLE_DEEPSLEEP_ALARM = 102;
    public static final int MSG_RESTORE_DEEPSLEEP_ALARM = 103;
    public static final int MSG_UNBLOCK_UNRESTRICTED_ALARMS = 104;
    public static final int MSG_UPDATE_NETSTATE = 101;
    public static final int MSG_VENDOR_BEGIN_INDEX = 100;
    private static final String TAG = "OppoBaseAlarmManagerService";
    private AlarmUpdateHelper.Callbacks mAlarmUpdateCallbacks;
    protected AlarmUpdateHelper mAlarmUpdateHelper;
    protected OppoAlarmWakeupDetection mAlarmWakeupDetection;
    IColorAlarmManagerServiceEx mColorAlarmEx;
    IColorAlarmManagerServiceInner mColorAlarmMSInner;
    private Context mContext;
    protected int mDelayAlarmMode;
    private boolean mIsHighTemperatureProtectIn;
    protected volatile boolean mIsRestoring;
    private boolean mNetstateInteractive;
    protected boolean mOppoGuardElfFeature;
    OppoLocalService mOppoLocalService;
    protected ArrayList<AlarmManagerService.Alarm> mPendingImportantNonWakeupAlarms;
    SparseArray<ArrayList<AlarmManagerService.Alarm>> mProxyAlarmsForHans;
    private Intent mTimeTickIntentLocal;

    /* access modifiers changed from: package-private */
    public static class BaseBroadcastStats {
        long lastTimeWakeup;
        int numCanceledWakeup;
        int numRealWakeupScreeoff;
        int numRealWakeupWhenReset;
        int numWakeupScreeoff;
        int numWakeupWhenReset;
        long timestampWakupCountReset;
    }

    /* access modifiers changed from: package-private */
    public static class BaseFilterStats {
        int numWakeupWhenReset;
        int numWakeupWhenScreenoff;
    }

    /* access modifiers changed from: protected */
    public abstract boolean findPendingNonWakeupAlarmWithAction(String str);

    /* access modifiers changed from: protected */
    public abstract Object getAlarmGlobalLock();

    /* access modifiers changed from: protected */
    public abstract Handler getHandlerInstance();

    public abstract AlarmManagerService.BroadcastStats getStatsLockedForGuardElf(PendingIntent pendingIntent);

    /* access modifiers changed from: protected */
    public abstract void mergeAndClearPendingImportantNonWakeupAlarms();

    /* access modifiers changed from: protected */
    public abstract boolean onDeliverPendingNonWakeupAlarms();

    /* access modifiers changed from: protected */
    public abstract boolean onRebatchAllAlarms();

    /* access modifiers changed from: protected */
    public abstract void onSendAllUnrestrictedPendingBackgroundAlarms();

    /* access modifiers changed from: protected */
    public abstract void onSetAlarmImpl(AlarmParams alarmParams);

    /* access modifiers changed from: protected */
    public abstract void onWakeLockAcquire();

    /* access modifiers changed from: protected */
    public abstract void onWakeLockRelease();

    public OppoBaseAlarmManagerService(Context context) {
        super(context);
        this.mContext = null;
        this.mNetstateInteractive = true;
        this.mColorAlarmEx = null;
        this.mColorAlarmMSInner = null;
        this.mOppoLocalService = null;
        this.mProxyAlarmsForHans = new SparseArray<>();
        this.mIsHighTemperatureProtectIn = false;
        this.mAlarmWakeupDetection = null;
        this.mOppoGuardElfFeature = false;
        this.mAlarmUpdateCallbacks = new AlarmUpdateHelper.Callbacks() {
            /* class com.android.server.OppoBaseAlarmManagerService.AnonymousClass1 */

            @Override // com.android.server.AlarmUpdateHelper.Callbacks
            public void netStateChanged(boolean stateChanged) {
                Slog.d(OppoBaseAlarmManagerService.TAG, "mAlarmUpdateCallbacks.netStateChanged, stateChanged:" + stateChanged);
                OppoBaseAlarmManagerService.this.sendNetStateChangedMessage(stateChanged);
            }
        };
        this.mAlarmUpdateHelper = null;
        this.mTimeTickIntentLocal = null;
        this.mPendingImportantNonWakeupAlarms = new ArrayList<>();
        this.mDelayAlarmMode = 0;
        this.mIsRestoring = false;
        this.mOppoLocalService = new OppoLocalService();
        this.mContext = context;
        initTimeTickIntent();
        initAlarmUpdateHelper();
        initOppoGuardElf();
    }

    public void onColorInit(IColorAlarmManagerServiceInner inner) {
        this.mColorAlarmMSInner = inner;
        LocalServices.addService(OppoAlarmManagerServiceInternal.class, this.mOppoLocalService);
    }

    /* access modifiers changed from: protected */
    public void filterAlarmForHans(ArrayList<AlarmManagerService.Alarm> arrayList) {
    }

    /* access modifiers changed from: protected */
    public boolean filterAlarmForHans(AlarmManagerService.Alarm alarm) {
        if (alarm == null || alarm.operation == null) {
            return false;
        }
        String action = null;
        Intent intent = alarm.operation.getIntent();
        if (intent != null) {
            action = intent.getAction();
        }
        if (OppoFeatureCache.get(IColorHansManager.DEFAULT).hansAlarmIfNeeded(action, alarm.creatorUid, alarm.sourcePackage)) {
            return false;
        }
        ArrayList<AlarmManagerService.Alarm> alarmsForUid = this.mProxyAlarmsForHans.get(alarm.creatorUid);
        if (alarmsForUid == null) {
            alarmsForUid = new ArrayList<>();
            this.mProxyAlarmsForHans.put(alarm.creatorUid, alarmsForUid);
        }
        alarmsForUid.add(alarm);
        return true;
    }

    final class OppoLocalService extends OppoAlarmManagerServiceInternal {
        OppoLocalService() {
        }

        @Override // com.android.server.OppoAlarmManagerServiceInternal
        public void unproxyAlarmsForHans(int uid, String packageName) {
            ArrayList<AlarmManagerService.Alarm> alarmsToDeliver;
            if (OppoBaseAlarmManagerService.this.mColorAlarmMSInner != null) {
                Object alarmGlobalLock = OppoBaseAlarmManagerService.this.getAlarmGlobalLock();
                if (alarmGlobalLock == null) {
                    Slog.e(OppoBaseAlarmManagerService.TAG, "unproxyAlarmsForHans failed for lock");
                    return;
                }
                synchronized (alarmGlobalLock) {
                    ArrayList<AlarmManagerService.Alarm> alarmsForUid = OppoBaseAlarmManagerService.this.mProxyAlarmsForHans.get(uid);
                    if (alarmsForUid != null) {
                        if (alarmsForUid.size() != 0) {
                            if (packageName != null) {
                                alarmsToDeliver = new ArrayList<>();
                                for (int i = alarmsForUid.size() - 1; i >= 0; i--) {
                                    if (alarmsForUid.get(i).matches(packageName)) {
                                        alarmsToDeliver.add(alarmsForUid.remove(i));
                                    }
                                }
                                if (alarmsForUid.size() == 0) {
                                    OppoBaseAlarmManagerService.this.mProxyAlarmsForHans.remove(uid);
                                }
                            } else {
                                alarmsToDeliver = alarmsForUid;
                                OppoBaseAlarmManagerService.this.mProxyAlarmsForHans.remove(uid);
                            }
                            OppoBaseAlarmManagerService.this.mColorAlarmMSInner.deliverPendingAlarmsForHans(alarmsToDeliver);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void filterAlarmForPreload(ArrayList<AlarmManagerService.Alarm> triggerList) {
        for (int i = triggerList.size() - 1; i >= 0; i--) {
            AlarmManagerService.Alarm alarm = triggerList.get(i);
            if (!(alarm == null || alarm.operation == null)) {
                String action = null;
                Intent intent = alarm.operation.getIntent();
                if (intent != null) {
                    action = intent.getAction();
                }
                if (OppoFeatureCache.get(IColorResourcePreloadManager.DEFAULT).preloadAlarmBlock(action, alarm.creatorUid, alarm.sourcePackage)) {
                    triggerList.remove(i);
                }
            }
        }
    }

    private void initOppoGuardElf() {
    }

    /* access modifiers changed from: package-private */
    public boolean hasPackageAsUser(AlarmManagerService.Batch batch, String packageName, int userId) {
        int N = batch.alarms.size();
        for (int i = 0; i < N; i++) {
            AlarmManagerService.Alarm a = batch.alarms.get(i);
            if (a.matches(packageName) && UserHandle.getUserId(a.uid) == userId) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean lookForPackageLocked(Intent intent, ArrayList<AlarmManagerService.Batch> batches, ArrayList<AlarmManagerService.Alarm> alarms, String packageName) {
        int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
        if (userId < 0) {
            Slog.w(TAG, "multi app: ACTION_QUERY_PACKAGE_RESTART get EXTRA_USER_HANDLE = " + userId);
            userId = 0;
        }
        for (int i = 0; i < batches.size(); i++) {
            if (hasPackageAsUser(batches.get(i), packageName, userId)) {
                return true;
            }
        }
        for (int i2 = 0; i2 < alarms.size(); i2++) {
            if (alarms.get(i2).matches(packageName)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void setIsHighTempFlag(boolean isHighTemp) {
        this.mIsHighTemperatureProtectIn = isHighTemp;
    }

    static class BaseAlarm {
        public int callingPid = Binder.getCallingPid();
        public int callingUid = Binder.getCallingUid();
        public int expectedType;
        public IAlarmListener listener;
        public String listenerTag;
        public PendingIntent operation;
        public long origWhen;
        public String statsTag;
        public int type;
        public boolean wakeup;
        public long whenElapsedAllowWhileIdle = 0;

        public BaseAlarm(int _type) {
            this.expectedType = _type;
        }
    }

    /* access modifiers changed from: package-private */
    public void reScheduleAlignTickIfNeed(AlarmManagerService.Batch b) {
        if (OppoFeatureCache.get(IColorAlarmAlignment.DEFAULT).isAlignTick(b.flags) && !b.hasWakeups()) {
            OppoFeatureCache.get(IColorAlarmAlignment.DEFAULT).reScheduleAlignTick();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isSystemApp(int uid, String pkgName) {
        PackageManager packageManager;
        if (uid < 10000 || (packageManager = getContext().getPackageManager()) == null || isClosedSuperFirewall()) {
            return true;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = packageManager.getApplicationInfo(pkgName, 8192);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.d(TAG, "getApplicationInfo NameNotFoundException. pkg = " + pkgName);
        }
        if (appInfo == null || (appInfo.flags & 1) != 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void removeAlarmWhenInHighTemperatureProtectMode(ArrayList<AlarmManagerService.Alarm> triggerList) {
        if (this.mIsHighTemperatureProtectIn) {
            for (int i = triggerList.size() - 1; i >= 0; i--) {
                AlarmManagerService.Alarm alarm = triggerList.get(i);
                if (alarm.creatorUid >= 10000) {
                    if (DEBUG_PANIC) {
                        Slog.d(TAG, "InHighTemperatureProtectMode.Remove alarm = " + alarm);
                    }
                    triggerList.remove(i);
                    IColorAlarmManagerServiceInner iColorAlarmManagerServiceInner = this.mColorAlarmMSInner;
                    if (iColorAlarmManagerServiceInner != null) {
                        iColorAlarmManagerServiceInner.decrementAlarmCount(alarm.creatorUid, 1);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isProcessNotRunning(String pkgName) {
        if (pkgName == null || pkgName.equals("")) {
            return false;
        }
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = ((ActivityManager) getContext().getSystemService(IColorAppStartupManager.TYPE_ACTIVITY)).getRunningAppProcesses();
        PackageManager pm = getContext().getPackageManager();
        for (ActivityManager.RunningAppProcessInfo processInfo : appProcessInfoList) {
            String packageName = "";
            try {
                packageName = pm.getPackageInfo(processInfo.processName, 0).packageName;
            } catch (PackageManager.NameNotFoundException e) {
            }
            if (pkgName.equals(packageName)) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public int setFlagsWakeFromIdle(int callingUid, String callingPackage, int flags) {
        if (callingPackage == null) {
            return flags;
        }
        if ((flags & 2) != 0) {
            if (!isSystemApp(callingUid, callingPackage)) {
                return (flags & -3) | 8;
            }
            return flags;
        } else if (!OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).inPackageNameWhiteList(callingPackage) || (flags & 24) != 0) {
            return flags;
        } else {
            return (flags & -5) | 8;
        }
    }

    /* access modifiers changed from: protected */
    public boolean appNotRunAndFilterRemovePackage(Intent intent) {
        if ("android.intent.action.QUERY_PACKAGE_RESTART".equals(intent.getAction())) {
            String[] packageList = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
            if (packageList.length == 1) {
                for (String packageName : packageList) {
                    if (OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).isFilterRemovePackage(packageName) && isProcessNotRunning(packageName)) {
                        return true;
                    }
                }
            } else {
                Slog.d(TAG, "ACTION_QUERY_PACKAGE_RESTART package list length > 1");
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void suportOppoGuardElf(HandlerThread hd, Context context, Object mLock, SparseArray<ArrayMap<String, AlarmManagerService.BroadcastStats>> mBroadcastStats, AlarmManagerService ams) {
        hd.start();
        this.mOppoGuardElfFeature = context.getPackageManager().hasSystemFeature("oppo.guard.elf.support");
        boolean hasAlignFeature = context.getPackageManager().hasSystemFeature("oppo.Align.alarm.support");
        if (this.mOppoGuardElfFeature) {
            this.mAlarmWakeupDetection = new OppoAlarmWakeupDetection(context, mLock, ams, mBroadcastStats, hd.getLooper());
        }
        this.mColorAlarmEx = ColorServiceFactory.getInstance().getColorAlarmManagerServiceEx(context, ams);
        OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).init(context, ams, hd.getLooper());
        OppoFeatureCache.get(IColorStrictModeManager.DEFAULT).init(context, mLock, ams, hd.getLooper(), this.mColorAlarmMSInner);
        if (hasAlignFeature) {
            OppoFeatureCache.get(IColorAlarmAlignment.DEFAULT).initArgs(context, mLock, ams, hd.getLooper());
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAlarmTempWhitelist(PendingIntent operation, String callingPackage, int flags, AlarmManager.AlarmClockInfo alarmClock) {
        return OppoFeatureCache.get(IColorAlarmTempWhitelist.DEFAULT).isAlarmTempWhitelist(operation, callingPackage, flags, alarmClock);
    }

    /* access modifiers changed from: package-private */
    public boolean isAlarmTempWhitelist(AlarmManagerService.Alarm a, boolean interactive) {
        return OppoFeatureCache.get(IColorAlarmTempWhitelist.DEFAULT).isAlarmTempWhitelist(a, interactive);
    }

    private void dumpTempWhiteListIfNeed(FileDescriptor fd, PrintWriter pw, String[] args) {
        Slog.d(TAG, "dumpTempWhiteListIfNeed , tempInstance = " + OppoFeatureCache.get(IColorAlarmTempWhitelist.DEFAULT));
        OppoFeatureCache.get(IColorAlarmTempWhitelist.DEFAULT).dump(fd, pw, args);
    }

    /* access modifiers changed from: protected */
    public boolean onAlarmMessageHandle(Message msg) {
        boolean netstateInteractive = false;
        if (msg == null) {
            return false;
        }
        switch (msg.what) {
            case 101:
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "MSG_UPDATE_NETSTATE");
                }
                if (msg.arg1 == 1) {
                    netstateInteractive = true;
                }
                onNetStateChanged(netstateInteractive);
                return true;
            case 102:
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "MSG_PEND_WHILE_IDLE_DEEPSLEEP_ALARM");
                }
                boolean rebatchRes = onRebatchAllAlarms();
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "deepsleep, msg rebatch all res:" + rebatchRes);
                }
                return true;
            case 103:
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "MSG_RESTORE_DEEPSLEEP_ALARM");
                }
                Object alarmGlobalLock = getAlarmGlobalLock();
                if (alarmGlobalLock != null) {
                    synchronized (alarmGlobalLock) {
                        OppoFeatureCache.get(IColorDeepSleepHelper.DEFAULT).restoreDeepSleepPendingWhileIdleAlarmsLocked();
                    }
                }
                return true;
            case 104:
                if (DEBUG_PANIC) {
                    Slog.d(TAG, "MSG_UNBLOCK_UNRESTRICTED_ALARMS");
                }
                onSendAllUnrestrictedPendingBackgroundAlarms();
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: protected */
    public void onNetStateChanged(boolean netstateInteractive) {
        Object alarmGlobalLock = getAlarmGlobalLock();
        if (alarmGlobalLock == null) {
            Slog.e(TAG, "Fatal exception: sub class doesn't provide global lock!");
            return;
        }
        onWakeLockAcquire();
        synchronized (alarmGlobalLock) {
            netStateChangedLocked(netstateInteractive);
        }
        onWakeLockRelease();
    }

    private void initAlarmUpdateHelper() {
        this.mAlarmUpdateHelper = new AlarmUpdateHelper(this.mAlarmUpdateCallbacks, this.mContext);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNetStateChangedMessage(boolean stateChanged) {
        Handler handler = getHandlerInstance();
        if (handler == null) {
            Slog.e(TAG, "netStateChanged: Sub class doesn't implement an instance of Handler!");
            return;
        }
        Message msg = handler.obtainMessage();
        msg.what = 101;
        msg.arg1 = stateChanged ? 1 : 0;
        msg.sendToTarget();
    }

    private void initTimeTickIntent() {
        this.mTimeTickIntentLocal = new Intent("android.intent.action.TIME_TICK").addFlags(1344274432);
    }

    /* access modifiers changed from: protected */
    public void sendTimeTickRightNow() {
        Handler handler = getHandlerInstance();
        if (handler == null) {
            Slog.e(TAG, "Fatal exception! sendTimeTickRightNow: Sub class doesn't implement an instance of Handler!");
        } else {
            handler.postDelayed(new Runnable() {
                /* class com.android.server.OppoBaseAlarmManagerService.AnonymousClass2 */

                public void run() {
                    OppoBaseAlarmManagerService.this.getContext().sendBroadcastAsUser(OppoBaseAlarmManagerService.this.mTimeTickIntentLocal, UserHandle.ALL);
                }
            }, 150);
        }
    }

    /* access modifiers changed from: protected */
    public int getPendingImportantNonWakeupAlarmsListSize() {
        int size;
        Object alarmGlobalLock = getAlarmGlobalLock();
        if (alarmGlobalLock == null) {
            Slog.e(TAG, "getPendingImportantNonWakeupAlarmsListSize failed for lock.");
            return 0;
        }
        synchronized (alarmGlobalLock) {
            size = this.mPendingImportantNonWakeupAlarms.size();
        }
        return size;
    }

    /* access modifiers changed from: protected */
    public void onInteractiveStateChanged(long nonInteractiveStartTime) {
        boolean hasTimeTick;
        mergeAndClearPendingImportantNonWakeupAlarms();
        if (SystemClock.elapsedRealtime() - nonInteractiveStartTime > 60000) {
            hasTimeTick = findPendingNonWakeupAlarmWithAction("android.intent.action.TIME_TICK");
        } else {
            hasTimeTick = true;
        }
        if (!hasTimeTick) {
            if (DEBUG_PANIC) {
                Slog.d(TAG, "Sechdule a TIME_TICK");
            }
            sendTimeTickRightNow();
        }
    }

    class AlarmParams {
        AlarmManager.AlarmClockInfo alarmClock;
        String callingPackage;
        int callingUid;
        IAlarmListener directReceiver;
        boolean doValidate;
        int flags;
        long interval;
        String listenerTag;
        long maxWhen;
        PendingIntent operation;
        int type;
        long when;
        long whenElapsed;
        long windowLength;
        WorkSource workSource;

        AlarmParams() {
        }
    }

    /* access modifiers changed from: protected */
    public void onPendingNonWakeupAlarmRescheduleLocked(long nowELAPSED, ArrayList<AlarmManagerService.Alarm> pendingAlarms) {
        if (pendingAlarms != null) {
            while (pendingAlarms.size() > 0) {
                AlarmManagerService.Alarm alarm = pendingAlarms.get(0);
                if (alarm.repeatInterval > 0) {
                    alarm.count = (int) (((long) alarm.count) + ((nowELAPSED - alarm.whenElapsed) / alarm.repeatInterval));
                    long delta = ((long) alarm.count) * alarm.repeatInterval;
                    long nextElapsed = alarm.whenElapsed + delta;
                    long maxTriggerTime = maxTriggerTime(nowELAPSED, nextElapsed, alarm.repeatInterval);
                    AlarmParams alarmParms = new AlarmParams();
                    alarmParms.type = alarm.type;
                    alarmParms.when = alarm.when + delta;
                    alarmParms.whenElapsed = nextElapsed;
                    alarmParms.windowLength = alarm.windowLength;
                    alarmParms.maxWhen = maxTriggerTime;
                    alarmParms.interval = alarm.repeatInterval;
                    alarmParms.operation = alarm.operation;
                    alarmParms.directReceiver = null;
                    alarmParms.listenerTag = null;
                    alarmParms.flags = alarm.flags;
                    alarmParms.doValidate = true;
                    alarmParms.workSource = alarm.workSource;
                    alarmParms.alarmClock = alarm.alarmClock;
                    alarmParms.callingUid = alarm.uid;
                    alarmParms.callingPackage = alarm.packageName;
                    onSetAlarmImpl(alarmParms);
                }
                pendingAlarms.remove(0);
            }
            pendingAlarms.clear();
        }
    }

    private void netStateChangedLocked(boolean netstateInteractive) {
        if (this.mNetstateInteractive != netstateInteractive) {
            this.mNetstateInteractive = netstateInteractive;
            if (netstateInteractive) {
                mergeAndClearPendingImportantNonWakeupAlarms();
                boolean handled = onDeliverPendingNonWakeupAlarms();
                Slog.d(TAG, "netStateChangedLocked, onDeliverPendingNonWakeupAlarms:" + handled);
            }
        }
    }

    private static long maxTriggerTime(long now, long triggerAtTime, long interval) {
        long futurity;
        if (interval == 0) {
            futurity = triggerAtTime - now;
        } else {
            futurity = interval;
        }
        if (futurity < 10000) {
            futurity = 0;
        }
        return clampPositive(((long) (((double) futurity) * 0.75d)) + triggerAtTime);
    }

    private static long clampPositive(long val) {
        return val >= 0 ? val : JobStatus.NO_LATEST_RUNTIME;
    }

    /* access modifiers changed from: protected */
    public boolean dumpImpl(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (dynamicallyConfigLogTag(pw, args)) {
            return true;
        }
        if (DEBUG_PANIC) {
            pw.println("  highTemp=" + String.valueOf(this.mIsHighTemperatureProtectIn));
        }
        if (args.length < 1) {
            return false;
        }
        if ("frequentAlarm".equals(args[0])) {
            OppoAlarmWakeupDetection oppoAlarmWakeupDetection = this.mAlarmWakeupDetection;
            if (oppoAlarmWakeupDetection != null) {
                oppoAlarmWakeupDetection.dumpFrequentAlarm(pw);
            }
            return true;
        } else if ("alignWhiteList".equals(args[0])) {
            OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).dump(pw);
            return true;
        } else if (!"tempWhiteList".equals(args[0])) {
            return false;
        } else {
            dumpTempWhiteListIfNeed(fd, pw, args);
            return true;
        }
    }

    private boolean dynamicallyConfigLogTag(PrintWriter pw, String[] args) {
        String opt;
        if (0 >= args.length || (opt = args[0]) == null || opt.length() <= 0 || opt.charAt(0) != '-') {
            if (0 < args.length) {
                String cmd = args[0];
                int opti = 0 + 1;
                if ("log".equals(cmd)) {
                    configLogTag(pw, args, opti);
                    return true;
                } else if ("whitelist".equals(cmd)) {
                    pw.println(this.mAlarmUpdateHelper.dumpToString());
                    return true;
                }
            }
            return false;
        }
        int opti2 = 0 + 1;
        if (PriorityDump.PROTO_ARG.equals(opt)) {
            return false;
        }
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

    /* access modifiers changed from: protected */
    public void configLogTag(PrintWriter pw, String[] args, int opti) {
        boolean openLog;
        if (opti >= args.length) {
            pw.println("  Invalid argument!");
            return;
        }
        if ("on".equals(args[opti])) {
            openLog = true;
        } else if ("off".equals(args[opti])) {
            openLog = false;
        } else {
            pw.println("  Invalid argument!");
            return;
        }
        DEBUG_PENDING = openLog;
        DEBUG_PANIC = openLog;
        DEBUG_DETAIL = openLog;
    }

    public void schedulePendWhileIdleDeepSleepAlarms() {
        Handler alarmHandler = getHandlerInstance();
        if (alarmHandler == null) {
            Slog.e(TAG, "DeepSleepAlarms Fatal exception: lack of alarmHandler instance.");
            return;
        }
        alarmHandler.removeMessages(102);
        alarmHandler.sendEmptyMessage(102);
    }

    public void scheduleRestoreDeepSleepPendingWhileIdleAlarms() {
        Handler alarmHandler = getHandlerInstance();
        if (alarmHandler == null) {
            Slog.e(TAG, "IdleAlarms Fatal exception: lack of alarmHandler instance.");
            return;
        }
        alarmHandler.removeMessages(103);
        alarmHandler.sendEmptyMessage(103);
    }

    public void rescheduleDeepSleepTask(Runnable deepSleepTask, long delayTime) {
        Handler alarmHandler = getHandlerInstance();
        if (alarmHandler == null) {
            Slog.e(TAG, "rescheduleDeepSleepTask Fatal exception: lack of alarmHandler instance.");
            return;
        }
        if (deepSleepTask == null) {
            Slog.e(TAG, "rescheduleDeepSleepTask: deepSleepTask empty.");
        }
        alarmHandler.removeCallbacks(deepSleepTask);
        if (delayTime > 0) {
            alarmHandler.postDelayed(deepSleepTask, delayTime);
        } else {
            alarmHandler.post(deepSleepTask);
        }
    }

    /* access modifiers changed from: protected */
    public int getAlarmConvertType(int type, PendingIntent operation, String callingPackage) {
        return this.mAlarmUpdateHelper.convertType(type, operation, callingPackage);
    }

    public void setRestoringFlagForAlarmAlignment(boolean flag) {
        this.mIsRestoring = flag;
    }

    public boolean getRestoringFlagForAlarmAlignment() {
        return this.mIsRestoring;
    }

    /* access modifiers changed from: protected */
    public boolean disableKernelTimeAdjustment() {
        return true;
    }

    /* access modifiers changed from: protected */
    public int adjustAlarmFlagsWhenSetImpl(PendingIntent operation, String callingPackage, int flags, AlarmManager.AlarmClockInfo alarmClock, long windowLength, int callingUid) {
        PackageManager packageManager = this.mContext.getPackageManager();
        if (!isAlarmTempWhitelist(operation, callingPackage, flags, alarmClock) && packageManager != null && !isClosedSuperFirewall() && alarmClock == null && OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).setInexactAlarm(windowLength) != 0) {
            flags &= -2;
        }
        return setFlagsWakeFromIdle(callingUid, callingPackage, flags);
    }

    /* access modifiers changed from: protected */
    public void logoutAlarmBatchInfo(AlarmManagerService.Batch batchInfo, String prefixTag) {
        if (DEBUG_DETAIL && batchInfo != null) {
            AlarmManagerService.Alarm a = batchInfo.get(0);
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date((batchInfo.start + System.currentTimeMillis()) - SystemClock.elapsedRealtime()));
            Slog.v(TAG, prefixTag + " pkg " + a.packageName + ", when " + time);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isClosedSuperFirewall() {
        return OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall();
    }
}
