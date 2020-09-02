package com.mediatek.server.anr;

import android.app.IActivityController;
import android.app.IApplicationThread;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SELinux;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ProcessList;
import com.android.server.am.ProcessRecord;
import com.mediatek.aee.ExceptionLog;
import com.mediatek.anr.AnrManagerNative;
import com.mediatek.omadm.PalConstDefs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnrManagerService extends AnrManagerNative {
    private static final String ACTIVE_SERVICES = "com.android.server.am.ActiveServices";
    private static final String ACTIVITY_MANAGER = "com.android.server.am.ActivityManagerService";
    private static final String ACTIVITY_RECORD = "com.android.server.am.ActivityRecord";
    private static final long ANR_BOOT_DEFER_TIME = 30000;
    private static final long ANR_CPU_DEFER_TIME = 8000;
    private static final float ANR_CPU_THRESHOLD = 90.0f;
    private static final String APP_ERRORS = "com.android.server.am.AppErrors";
    private static final String ATM_SERVICE = "com.android.server.wm.ActivityTaskManagerService";
    private static final String BATTERY_STATS = "com.android.server.am.BatteryStatsService";
    private static final int DISABLE_ALL_ANR_MECHANISM = 0;
    private static final int DISABLE_ANR_DUMP_FOR_3RD_APP = 0;
    private static final int DISABLE_PARTIAL_ANR_MECHANISM = 1;
    private static final int ENABLE_ALL_ANR_MECHANISM = 2;
    private static final int ENABLE_ANR_DUMP_FOR_3RD_APP = 1;
    private static final int EVENT_BOOT_COMPLETED = 9001;
    private static final int INVALID_ANR_FLOW = -1;
    private static final int INVALID_ANR_OPTION = -1;
    private static final boolean IS_PREVERSION_LOAD = SystemProperties.getBoolean("ro.sys.engineering.pre", false);
    private static final boolean IS_USER_BUILD;
    private static final boolean IS_USER_LOAD = "user".equals(Build.TYPE);
    private static final int MAX_MTK_TRACE_COUNT = 10;
    private static final int MESSAGE_MAP_BUFFER_COUNT_MAX = 5;
    private static final int MESSAGE_MAP_BUFFER_SIZE_MAX = 50000;
    private static final long MONITOR_CPU_MIN_TIME = 2500;
    /* access modifiers changed from: private */
    public static String[] NATIVE_STACKS_OF_INTEREST = {"/system/bin/netd", "/system/bin/audioserver", "/system/bin/cameraserver", "/system/bin/drmserver", "/system/bin/mediadrmserver", "/system/bin/mediaserver", "/system/bin/sdcard", "/system/bin/surfaceflinger", "vendor/bin/hw/camerahalserver", "media.extractor", "media.codec", "com.android.bluetooth"};
    private static final int NORMAL_ANR_FLOW = 0;
    private static final String PROCESS_LIST = "com.android.server.am.ProcessList";
    private static final String PROCESS_RECORD = "com.android.server.am.ProcessRecord";
    private static final int REMOVE_KEYDISPATCHING_TIMEOUT_MSG = 1005;
    private static final int SERVICE_TIMEOUT = 20000;
    private static final int SKIP_ANR_FLOW = 1;
    private static final int SKIP_ANR_FLOW_AND_KILL = 2;
    private static final int START_ANR_DUMP_MSG = 1003;
    private static final int START_MONITOR_BROADCAST_TIMEOUT_MSG = 1001;
    private static final int START_MONITOR_KEYDISPATCHING_TIMEOUT_MSG = 1004;
    private static final int START_MONITOR_SERVICE_TIMEOUT_MSG = 1002;
    private static final String TAG = "AnrManager";
    private static Object lock = new Object();
    private static final ProcessCpuTracker mAnrProcessStats = new ProcessCpuTracker(false);
    private static final Object mDumpStackTraces = new Object();
    private static ConcurrentHashMap<Integer, String> mMessageMap = new ConcurrentHashMap<>();
    private static int[] mZygotePids = null;
    private static boolean sEnhanceEnable = true;
    private static AnrManagerService sInstance = null;
    private ExceptionLog exceptionLog = null;
    /* access modifiers changed from: private */
    public Class<?> mAMS = getActivityManagerService();
    private Field mActiveServicesField = getAMSField("mServices");
    /* access modifiers changed from: private */
    public int mAmsPid;
    /* access modifiers changed from: private */
    public AnrDumpManager mAnrDumpManager;
    private int mAnrFlow = -1;
    /* access modifiers changed from: private */
    public AnrMonitorHandler mAnrHandler;
    private int mAnrOption = -1;
    private Field mBatteryStatsServiceField = getAMSField("mBatteryStatsService");
    private Field mControllerField = getATMField("mController");
    private long mCpuDeferred = 0;
    private Field mCrashingField = getProcessRecordField("crashing");
    private long mEventBootCompleted = 0;
    private Field mInfoField = getProcessRecordField("info");
    private Method mKill = getProcessRecordMethod("kill", new Class[]{String.class, Boolean.TYPE});
    private final AtomicLong mLastCpuUpdateTime = new AtomicLong(0);
    /* access modifiers changed from: private */
    public Field mLruProcessesField = getPLField("mLruProcesses");
    private Method mMakeAppNotRespondingLocked = getProcessRecordMethod("makeAppNotRespondingLocked", new Class[]{String.class, String.class, String.class});
    /* access modifiers changed from: private */
    public Field mMonitorCpuUsageField = getAMSField("MONITOR_CPU_USAGE");
    private Field mNotRespondingField = getProcessRecordField("notResponding");
    private Method mNoteProcessANR = getBatteryStatsServiceMethod("noteProcessAnr", new Class[]{String.class, Integer.TYPE});
    private Field mParentAppField = getActivityRecordField("app");
    private Field mParentPidField = getProcessRecordField("pid");
    /* access modifiers changed from: private */
    public Field mPersistentField = getProcessRecordField("mPersistent");
    /* access modifiers changed from: private */
    public Field mPidField = getProcessRecordField("pid");
    /* access modifiers changed from: private */
    public Field mProcessCpuTrackerField = getAMSField("mProcessCpuTracker");
    /* access modifiers changed from: private */
    public Field mProcessListField = getAMSField("mProcessList");
    private Field mProcessNameField = getProcessRecordField("processName");
    private Class<?> mProcessRecord = getProcessRecord();
    private Method mScheduleServiceTimeoutLocked = getActiveServicesMethod("scheduleServiceTimeoutLocked", new Class[]{ProcessRecord.class});
    /* access modifiers changed from: private */
    public ActivityManagerService mService;
    private Field mShowNotRespondingUiMsgField = getAMSField("SHOW_NOT_RESPONDING_UI_MSG");
    /* access modifiers changed from: private */
    public Field mThreadField = getProcessRecordField("thread");
    /* access modifiers changed from: private */
    public File mTracesFile = null;
    private Field mUiHandlerField = getAMSField("mUiHandler");
    private Field mUidField = getProcessRecordField("uid");
    /* access modifiers changed from: private */
    public Method mUpdateCpuStatsNow = getAMSMethod("updateCpuStatsNow");
    private Field mUserIdField = getProcessRecordField("userId");

    static {
        boolean z = true;
        if (!"user".equals(Build.TYPE) && !"userdebug".equals(Build.TYPE)) {
            z = false;
        }
        IS_USER_BUILD = z;
    }

    private Class<?> getProcessRecord() {
        try {
            return Class.forName(PROCESS_RECORD);
        } catch (Exception e) {
            return null;
        }
    }

    private Class<?> getActivityManagerService() {
        try {
            return Class.forName(ACTIVITY_MANAGER);
        } catch (Exception e) {
            return null;
        }
    }

    private Method getProcessRecordMethod(String func, Class[] cls) {
        try {
            Method method = this.mProcessRecord.getDeclaredMethod(func, cls);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            Slog.w(TAG, "getProcessRecordMethod Exception: " + e);
            return null;
        }
    }

    private Method getAMSMethod(String func) {
        try {
            Method method = this.mAMS.getDeclaredMethod(func, new Class[0]);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            return null;
        }
    }

    private Method getBatteryStatsServiceMethod(String func, Class[] cls) {
        try {
            Method method = Class.forName(BATTERY_STATS).getDeclaredMethod(func, cls);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            return null;
        }
    }

    private Method getActiveServicesMethod(String func, Class[] cls) {
        try {
            return Class.forName(ACTIVE_SERVICES).getDeclaredMethod(func, cls);
        } catch (Exception e) {
            return null;
        }
    }

    private Method getAppErrorsMethod(String func, Class[] cls) {
        try {
            Method method = Class.forName(APP_ERRORS).getDeclaredMethod(func, cls);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            return null;
        }
    }

    private Field getProcessRecordField(String var) {
        try {
            Field field = this.mProcessRecord.getDeclaredField(var);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            return null;
        }
    }

    private Field getActivityRecordField(String var) {
        try {
            Field field = Class.forName(ACTIVITY_RECORD).getDeclaredField(var);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            return null;
        }
    }

    private Field getAMSField(String var) {
        try {
            Field field = this.mAMS.getDeclaredField(var);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            return null;
        }
    }

    private Field getATMField(String var) {
        try {
            Field field = Class.forName(ATM_SERVICE).getDeclaredField(var);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            return null;
        }
    }

    private Field getPLField(String var) {
        try {
            Field field = Class.forName(PROCESS_LIST).getDeclaredField(var);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            return null;
        }
    }

    public static AnrManagerService getInstance() {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    sInstance = new AnrManagerService();
                }
            }
        }
        return sInstance;
    }

    public void startAnrManagerService(int pid) {
        Slog.i(TAG, "startAnrManagerService");
        this.mAmsPid = pid;
        HandlerThread handlerThread = new HandlerThread("AnrMonitorThread");
        handlerThread.start();
        this.mAnrHandler = new AnrMonitorHandler(handlerThread.getLooper());
        this.mAnrDumpManager = new AnrDumpManager();
        mAnrProcessStats.init();
        prepareStackTraceFile(SystemProperties.get("dalvik.vm.stack-trace-file", (String) null));
        File traceDir = new File(SystemProperties.get("dalvik.vm.stack-trace-file", (String) null)).getParentFile();
        if (traceDir != null && !SELinux.restoreconRecursive(traceDir)) {
            Slog.i(TAG, "startAnrManagerService SELinux.restoreconRecursive fail dir = " + traceDir.toString());
        }
        if (SystemProperties.get("ro.vendor.have_aee_feature").equals("1")) {
            this.exceptionLog = ExceptionLog.getInstance();
        }
        this.mKill.setAccessible(true);
        this.mUpdateCpuStatsNow.setAccessible(true);
        this.mNoteProcessANR.setAccessible(true);
        this.mScheduleServiceTimeoutLocked.setAccessible(true);
        this.mMakeAppNotRespondingLocked.setAccessible(true);
    }

    public void sendBroadcastMonitorMessage(long timeoutTime, long mTimeoutPeriod) {
        if (2 == checkAnrDebugMechanism()) {
            this.mAnrHandler.sendMessageAtTime(this.mAnrHandler.obtainMessage(START_MONITOR_BROADCAST_TIMEOUT_MSG), timeoutTime - (mTimeoutPeriod / 2));
        }
    }

    public void removeBroadcastMonitorMessage() {
        if (2 == checkAnrDebugMechanism()) {
            this.mAnrHandler.removeMessages(START_MONITOR_BROADCAST_TIMEOUT_MSG);
        }
    }

    public void sendServiceMonitorMessage() {
        long now = SystemClock.uptimeMillis();
        if (2 == checkAnrDebugMechanism()) {
            this.mAnrHandler.sendMessageAtTime(this.mAnrHandler.obtainMessage(START_MONITOR_SERVICE_TIMEOUT_MSG), 13333 + now);
        }
    }

    public void removeServiceMonitorMessage() {
        if (2 == checkAnrDebugMechanism()) {
            this.mAnrHandler.removeMessages(START_MONITOR_SERVICE_TIMEOUT_MSG);
        }
    }

    /* JADX DEBUG: Additional 4 move instruction added to help type inference */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v5, types: [boolean] */
    /* JADX WARN: Type inference failed for: r6v6 */
    /* JADX WARN: Type inference failed for: r6v9 */
    /* JADX WARN: Type inference failed for: r6v10, types: [boolean] */
    /* JADX WARN: Type inference failed for: r6v12 */
    /* JADX WARN: Type inference failed for: r6v17 */
    /* JADX WARN: Type inference failed for: r6v18 */
    /* JADX WARN: Type inference failed for: r6v20 */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x024c, code lost:
        r1.addErrorToDropBox("anr", r28, r20, r29, r31, r32, r34, r0, r26.mTracesFile, (android.app.ApplicationErrorReport.CrashInfo) null);
        android.util.Slog.i(com.mediatek.server.anr.AnrManagerService.TAG, " controller = " + r24);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0283, code lost:
        if (r24 == null) goto L_0x030c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x0286, code lost:
        if (r10 == null) goto L_0x0295;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:?, code lost:
        r0 = r10.mInfo.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x0290, code lost:
        r6 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x0295, code lost:
        r0 = com.mediatek.omadm.PalConstDefs.EMPTY_STRING;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:?, code lost:
        r0 = r24.appNotResponding(r20, r21, r0);
        android.util.Slog.i(com.mediatek.server.anr.AnrManagerService.TAG, " res = " + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x02b4, code lost:
        if (r0 == 0) goto L_0x02ee;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x02b6, code lost:
        if (r0 >= 0) goto L_0x02d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x02ba, code lost:
        if (r21 == r26.mAmsPid) goto L_0x02d1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x02bc, code lost:
        r0 = r26.mKill;
        r5 = new java.lang.Object[2];
        r5[r11] = "anr";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x02c9, code lost:
        r6 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:?, code lost:
        r5[r6] = java.lang.Boolean.valueOf(r17);
        r0.invoke(r28, r5);
        r6 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x02d1, code lost:
        r6 = r17;
        r2 = r26.mService;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:120:0x02d5, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:?, code lost:
        r0 = r26.mScheduleServiceTimeoutLocked;
        r5 = r26.mActiveServicesField.get(r26.mService);
        r7 = new java.lang.Object[r6];
        r7[r11] = r28;
        r0.invoke(r5, r7);
        r6 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x02e7, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x02e8, code lost:
        return r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x02ec, code lost:
        r6 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x02ee, code lost:
        r6 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:131:0x02f2, code lost:
        r6 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:132:0x02fa, code lost:
        r26.mControllerField.set(r26.mService.mActivityTaskManager, null);
        com.android.server.Watchdog.getInstance().setActivityController((android.app.IActivityController) null);
        r6 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x030c, code lost:
        r6 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x0312, code lost:
        monitor-enter(r26.mService);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:?, code lost:
        r0 = r26.mNoteProcessANR;
        r2 = r26.mBatteryStatsServiceField.get(r26.mService);
        r7 = new java.lang.Object[2];
        r7[r11] = r20;
        r7[r6] = java.lang.Integer.valueOf(((java.lang.Integer) r26.mUidField.get(r28)).intValue());
        r0.invoke(r2, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x0337, code lost:
        if (r15 != false) goto L_0x0339;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:?, code lost:
        r0 = r26.mKill;
        r2 = new java.lang.Object[2];
        r2[r11] = "bg anr";
        r2[r6] = java.lang.Boolean.valueOf((boolean) r6);
        r0.invoke(r28, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x034c, code lost:
        return r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x034d, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x0357, code lost:
        r0 = r26.mMakeAppNotRespondingLocked;
        r2 = new java.lang.Object[3];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:147:?, code lost:
        r2[r11] = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:149:0x0361, code lost:
        if (r34 != null) goto L_0x0363;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:?, code lost:
        r8 = "ANR " + r34;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x0375, code lost:
        r8 = "ANR";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x0377, code lost:
        r2[r6] = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x0379, code lost:
        if (r10 != null) goto L_0x037b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x037b, code lost:
        r8 = r10.mInfo.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x0382, code lost:
        r8 = com.mediatek.omadm.PalConstDefs.EMPTY_STRING;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x0384, code lost:
        r2[2] = r8;
        r0.invoke(r28, r2);
        r0 = android.os.Message.obtain();
        r0.what = ((java.lang.Integer) r26.mShowNotRespondingUiMsgField.get(r26.mAMS)).intValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:?, code lost:
        r0.obj = new com.android.server.am.AppNotRespondingDialog.Data(r28, r30, r33);
        ((android.os.Handler) r26.mUiHandlerField.get(r26.mService)).sendMessage(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x03b8, code lost:
        return r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x03b9, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x03bf, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x03c7, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:0x03d0, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x03d1, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00c2, code lost:
        if (needAnrDump(r5) == false) goto L_0x0198;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00c4, code lost:
        enableTraceLog(false);
        new com.mediatek.server.anr.AnrManagerService.BinderDumpThread(r26, r7).start();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00d7, code lost:
        if (r26.mAnrDumpManager.mDumpList.containsKey(r28) != false) goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00d9, code lost:
        r19 = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00dd, code lost:
        if (r28 == null) goto L_0x00e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00df, code lost:
        r20 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00e2, code lost:
        r20 = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00e6, code lost:
        if (r28 == null) goto L_0x00eb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00e8, code lost:
        r22 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00eb, code lost:
        r22 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ed, code lost:
        if (r28 == null) goto L_0x00f4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00ef, code lost:
        r23 = r28.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00f4, code lost:
        r23 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00f6, code lost:
        if (r32 == null) goto L_0x00fa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00f8, code lost:
        r19 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00fa, code lost:
        r15 = r3;
        r17 = true;
        r24 = r4;
        r25 = r5;
        r20 = r6;
        r21 = r7;
        r0 = new com.mediatek.server.anr.AnrManagerService.AnrDumpRecord(r26, r20, false, r22, r23, r29, r19, r31, r34, r36);
        r12 = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0122, code lost:
        if (2 != checkAnrDebugMechanism()) goto L_0x014e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0124, code lost:
        updateProcessStats();
        r1 = getAndroidTime() + getProcessState() + "\n";
        r0.mCpuInfo = r1;
        android.util.Slog.i(com.mediatek.server.anr.AnrManagerService.TAG, r1.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x014e, code lost:
        r26.mAnrDumpManager.startAsyncDump(r0, r15);
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0155, code lost:
        r12 = 2;
        r15 = r3;
        r24 = r4;
        r25 = r5;
        r20 = r6;
        r21 = r7;
        r17 = true;
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0163, code lost:
        if (r1 == null) goto L_0x0171;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0165, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:?, code lost:
        r11 = 0;
        r26.mAnrDumpManager.dumpAnrDebugInfo(r1, false, r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x016c, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0171, code lost:
        r11 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0172, code lost:
        r26.mAnrDumpManager.removeDumpRecord(r1);
        r1.mCpuInfo += com.mediatek.server.anr.AnrManagerService.mMessageMap.get(java.lang.Integer.valueOf(r21));
        r10 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0198, code lost:
        r12 = 2;
        r15 = r3;
        r24 = r4;
        r25 = r5;
        r20 = r6;
        r21 = r7;
        r11 = 0;
        r17 = true;
        r10 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x01a7, code lost:
        r2 = ((java.lang.Integer) r26.mUidField.get(r28)).intValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x01b7, code lost:
        if (r25 == null) goto L_0x01c4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01bd, code lost:
        if (r25.isInstantApp() == false) goto L_0x01c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x01bf, code lost:
        r6 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x01c1, code lost:
        r6 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x01c4, code lost:
        r6 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01c5, code lost:
        if (r28 == null) goto L_0x01d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x01cb, code lost:
        if (r28.isInterestingToUserLocked() == false) goto L_0x01cf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01cd, code lost:
        r7 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x01cf, code lost:
        r7 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x01d2, code lost:
        r7 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x01d3, code lost:
        if (r28 == null) goto L_0x01db;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x01d5, code lost:
        r8 = r28.getProcessClassEnum();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x01db, code lost:
        r8 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01dc, code lost:
        if (r25 == null) goto L_0x01e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x01de, code lost:
        r0 = r25.packageName;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x01e1, code lost:
        r0 = com.mediatek.omadm.PalConstDefs.EMPTY_STRING;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x01e3, code lost:
        android.util.StatsLog.write(79, r2, r20, r29, r34, r6, r7, r8, r0);
        android.util.Slog.i(com.mediatek.server.anr.AnrManagerService.TAG, "addErrorToDropBox app = " + r28 + " processName = " + r20 + " activityShortComponentName = " + r29 + " parentShortComponentName = " + r31 + " parentProcess = " + r32 + " annotation = " + r34 + " mTracesFile = " + r26.mTracesFile);
        r1 = r26.mService;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0245, code lost:
        if (r10 == null) goto L_0x024a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x0247, code lost:
        r0 = r10.mCpuInfo;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x024a, code lost:
        r0 = com.mediatek.omadm.PalConstDefs.EMPTY_STRING;
     */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x0313 A[SYNTHETIC, Splitter:B:136:0x0313] */
    /* JADX WARNING: Unknown variable types count: 3 */
    public boolean startAnrDump(ActivityManagerService service, ProcessRecord app, String activityShortComponentName, ApplicationInfo aInfo, String parentShortComponentName, ProcessRecord parentProcess, boolean aboveSystem, String annotation, boolean showBackground, long anrTime) throws Exception {
        int parentPid;
        boolean isSilentANR;
        boolean isSilentANR2;
        Slog.i(TAG, "startAnrDump");
        if (checkAnrDebugMechanism() == 0) {
            return false;
        }
        this.mService = service;
        int pid = ((Integer) this.mPidField.get(app)).intValue();
        String processName = (String) this.mProcessNameField.get(app);
        ApplicationInfo appInfo = (ApplicationInfo) this.mInfoField.get(app);
        IActivityController controller = (IActivityController) this.mControllerField.get(this.mService.mActivityTaskManager);
        if (parentProcess != null) {
            parentPid = ((Integer) this.mParentPidField.get(parentProcess)).intValue();
        } else {
            parentPid = -1;
        }
        if (isAnrFlowSkipped(pid, processName, annotation)) {
            return true;
        }
        if (!IS_USER_LOAD) {
            try {
                ((IApplicationThread) this.mThreadField.get(app)).dumpMessage(pid == this.mAmsPid);
            } catch (Exception e) {
                Slog.e(TAG, "Error happens when dumping message history", e);
            }
        }
        synchronized (this.mService) {
            if (!showBackground) {
                try {
                    if (!app.isInterestingToUserLocked() && pid != this.mAmsPid) {
                        isSilentANR = true;
                        if (IS_PREVERSION_LOAD || aInfo == null || !"com.sohu.inputmethod.sogouoem".equals(aInfo.packageName)) {
                            isSilentANR2 = isSilentANR;
                        } else {
                            isSilentANR2 = true;
                        }
                        try {
                        } catch (Throwable th) {
                            th = th;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            }
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
            isSilentANR = false;
            try {
                if (IS_PREVERSION_LOAD) {
                }
                isSilentANR2 = isSilentANR;
            } catch (Throwable th4) {
                th = th4;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    public class AnrMonitorHandler extends Handler {
        public AnrMonitorHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AnrManagerService.START_MONITOR_BROADCAST_TIMEOUT_MSG /*{ENCODED_INT: 1001}*/:
                case AnrManagerService.START_MONITOR_SERVICE_TIMEOUT_MSG /*{ENCODED_INT: 1002}*/:
                case AnrManagerService.START_MONITOR_KEYDISPATCHING_TIMEOUT_MSG /*{ENCODED_INT: 1004}*/:
                    AnrManagerService.this.updateProcessStats();
                    return;
                case AnrManagerService.START_ANR_DUMP_MSG /*{ENCODED_INT: 1003}*/:
                    AnrDumpRecord adp = (AnrDumpRecord) msg.obj;
                    boolean isSilentANR = msg.arg1 == 1;
                    Slog.i(AnrManagerService.TAG, "START_ANR_DUMP_MSG: " + adp + ", isSilentANR = " + isSilentANR);
                    AnrManagerService.this.mAnrDumpManager.dumpAnrDebugInfo(adp, true, isSilentANR);
                    return;
                default:
                    return;
            }
        }
    }

    protected static final class BinderWatchdog {
        private static final int MAX_LINES = 64;
        private static final int MAX_TIMEOUT_PIDS = 5;

        protected BinderWatchdog() {
        }

        protected static class BinderInfo {
            protected static final int INDEX_FROM = 1;
            protected static final int INDEX_TO = 3;
            protected int mDstPid;
            protected int mDstTid;
            protected int mSrcPid;
            protected int mSrcTid;
            protected String mText;

            protected BinderInfo(String text) {
                if (text != null && text.length() > 0) {
                    this.mText = new String(text);
                    String[] tokens = text.split(" ");
                    String[] from = tokens[1].split(":");
                    if (from != null && from.length == 2) {
                        this.mSrcPid = Integer.parseInt(from[0]);
                        this.mSrcTid = Integer.parseInt(from[1]);
                    }
                    String[] to = tokens[3].split(":");
                    if (to != null && to.length == 2) {
                        this.mDstPid = Integer.parseInt(to[0]);
                        this.mDstTid = Integer.parseInt(to[1]);
                    }
                }
            }
        }

        public static final ArrayList<Integer> getTimeoutBinderPidList(int pid, int tid) {
            if (pid <= 0) {
                return null;
            }
            ArrayList<BinderInfo> binderList = readTimeoutBinderListFromFile();
            int count = 0;
            ArrayList<Integer> pidList = new ArrayList<>();
            for (BinderInfo next = getBinderInfo(pid, tid, binderList); next != null; next = getBinderInfo(next.mDstPid, next.mDstTid, binderList)) {
                if (next.mDstPid > 0) {
                    count++;
                    if (!pidList.contains(Integer.valueOf(next.mDstPid))) {
                        Slog.i(AnrManagerService.TAG, "getTimeoutBinderPidList pid added: " + next.mDstPid + " " + next.mText);
                        pidList.add(Integer.valueOf(next.mDstPid));
                    } else {
                        Slog.i(AnrManagerService.TAG, "getTimeoutBinderPidList pid existed: " + next.mDstPid + " " + next.mText);
                    }
                    if (count >= MAX_TIMEOUT_PIDS) {
                        break;
                    }
                }
            }
            if (pidList.size() == 0) {
                return getTimeoutBinderFromPid(pid, binderList);
            }
            return pidList;
        }

        public static final ArrayList<Integer> getTimeoutBinderFromPid(int pid, ArrayList<BinderInfo> binderList) {
            if (pid <= 0 || binderList == null) {
                return null;
            }
            Slog.i(AnrManagerService.TAG, "getTimeoutBinderFromPid " + pid + " list size: " + binderList.size());
            int count = 0;
            ArrayList<Integer> pidList = new ArrayList<>();
            Iterator<BinderInfo> it = binderList.iterator();
            while (it.hasNext()) {
                BinderInfo bi = it.next();
                if (bi != null && bi.mSrcPid == pid) {
                    count++;
                    if (!pidList.contains(Integer.valueOf(bi.mDstPid))) {
                        Slog.i(AnrManagerService.TAG, "getTimeoutBinderFromPid pid added: " + bi.mDstPid + " " + bi.mText);
                        pidList.add(Integer.valueOf(bi.mDstPid));
                    } else {
                        Slog.i(AnrManagerService.TAG, "getTimeoutBinderFromPid pid existed: " + bi.mDstPid + " " + bi.mText);
                    }
                    if (count >= MAX_TIMEOUT_PIDS) {
                        break;
                    }
                }
            }
            return pidList;
        }

        private static BinderInfo getBinderInfo(int pid, int tid, ArrayList<BinderInfo> binderList) {
            if (binderList == null || binderList.size() == 0 || pid == 0) {
                return null;
            }
            binderList.size();
            Iterator<BinderInfo> it = binderList.iterator();
            while (it.hasNext()) {
                BinderInfo bi = it.next();
                if (bi.mSrcPid == pid && bi.mSrcTid == tid) {
                    Slog.i(AnrManagerService.TAG, "Timeout binder pid found: " + bi.mDstPid + " " + bi.mText);
                    return bi;
                }
            }
            return null;
        }

        private static final ArrayList<BinderInfo> readTimeoutBinderListFromFile() {
            BufferedReader br = null;
            try {
                File file = new File("/sys/kernel/debug/binder/timeout_log");
                if (!file.exists()) {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException ioe) {
                            Slog.e(AnrManagerService.TAG, "IOException when close buffer reader:", ioe);
                        }
                    }
                    return null;
                }
                BufferedReader br2 = new BufferedReader(new FileReader(file));
                ArrayList<BinderInfo> binderList = new ArrayList<>();
                do {
                    String line = br2.readLine();
                    if (line != null) {
                        BinderInfo bi = new BinderInfo(line);
                        if (bi.mSrcPid > 0) {
                            binderList.add(bi);
                        }
                    }
                    break;
                } while (binderList.size() <= MAX_LINES);
                try {
                    break;
                    br2.close();
                } catch (IOException ioe2) {
                    Slog.e(AnrManagerService.TAG, "IOException when close buffer reader:", ioe2);
                }
                return binderList;
            } catch (FileNotFoundException e) {
                Slog.e(AnrManagerService.TAG, "FileNotFoundException", e);
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ioe3) {
                        Slog.e(AnrManagerService.TAG, "IOException when close buffer reader:", ioe3);
                    }
                }
                return null;
            } catch (IOException e2) {
                Slog.e(AnrManagerService.TAG, "IOException when gettting Binder. ", e2);
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ioe4) {
                        Slog.e(AnrManagerService.TAG, "IOException when close buffer reader:", ioe4);
                    }
                }
                return null;
            } catch (Throwable th) {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ioe5) {
                        Slog.e(AnrManagerService.TAG, "IOException when close buffer reader:", ioe5);
                    }
                }
                return null;
            }
        }

        protected static class TransactionInfo {
            protected String atime;
            protected String direction;
            protected String ktime;
            protected String rcv_pid;
            protected String rcv_tid;
            protected String snd_pid;
            protected String snd_tid;
            protected long spent_time;

            protected TransactionInfo() {
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:71:0x0207  */
        /* JADX WARNING: Removed duplicated region for block: B:76:0x0218  */
        /* JADX WARNING: Removed duplicated region for block: B:80:0x0221 A[SYNTHETIC, Splitter:B:80:0x0221] */
        /* JADX WARNING: Removed duplicated region for block: B:95:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:96:? A[RETURN, SYNTHETIC] */
        private static final void readTransactionInfoFromFile(int pid, ArrayList<Integer> binderList) {
            Throwable th;
            String patternStr = "(\\S+.+transaction).+from\\s+(\\d+):(\\d+)\\s+to\\s+(\\d+):(\\d+).+start\\s+(\\d+\\.+\\d+).+android\\s+(\\d+-\\d+-\\d+\\s+\\d+:\\d+:\\d+\\.\\d+)";
            Pattern pattern = Pattern.compile(patternStr);
            BufferedReader br = null;
            ArrayList<TransactionInfo> transactionList = new ArrayList<>();
            ArrayList<Integer> pidList = new ArrayList<>();
            try {
                String filepath = "/sys/kernel/debug/binder/proc/" + Integer.toString(pid);
                File file = new File(filepath);
                if (!file.exists()) {
                    try {
                        Slog.d(AnrManagerService.TAG, "Filepath isn't exist");
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException ioe) {
                                Slog.e(AnrManagerService.TAG, "IOException when close buffer reader:", ioe);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e = e;
                        Slog.e(AnrManagerService.TAG, "FileNotFoundException", e);
                        if (br == null) {
                            br.close();
                        }
                    } catch (IOException e2) {
                        e = e2;
                        try {
                            Slog.e(AnrManagerService.TAG, "IOException when gettting Binder. ", e);
                            if (br == null) {
                                br.close();
                            }
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException ioe2) {
                                Slog.e(AnrManagerService.TAG, "IOException when close buffer reader:", ioe2);
                            }
                        }
                        throw th;
                    }
                } else {
                    br = new BufferedReader(new FileReader(file));
                    while (true) {
                        String line = br.readLine();
                        if (line == null) {
                            break;
                        } else if (line.contains("transaction")) {
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find()) {
                                TransactionInfo tmpInfo = new TransactionInfo();
                                tmpInfo.direction = matcher.group(1);
                                tmpInfo.snd_pid = matcher.group(2);
                                tmpInfo.snd_tid = matcher.group(3);
                                tmpInfo.rcv_pid = matcher.group(4);
                                tmpInfo.rcv_tid = matcher.group(MAX_TIMEOUT_PIDS);
                                tmpInfo.ktime = matcher.group(6);
                                tmpInfo.atime = matcher.group(7);
                                try {
                                    try {
                                        tmpInfo.spent_time = SystemClock.uptimeMillis() - ((long) (Float.valueOf(tmpInfo.ktime).floatValue() * 1000.0f));
                                        transactionList.add(tmpInfo);
                                        if (tmpInfo.spent_time >= 1000 && !binderList.contains(Integer.valueOf(tmpInfo.rcv_pid))) {
                                            binderList.add(Integer.valueOf(tmpInfo.rcv_pid));
                                            if (!pidList.contains(Integer.valueOf(tmpInfo.rcv_pid))) {
                                                pidList.add(Integer.valueOf(tmpInfo.rcv_pid));
                                                Slog.i(AnrManagerService.TAG, "Transcation binderList pid=" + tmpInfo.rcv_pid);
                                            }
                                        }
                                        Slog.i(AnrManagerService.TAG, tmpInfo.direction + " from " + tmpInfo.snd_pid + ":" + tmpInfo.snd_tid + " to " + tmpInfo.rcv_pid + ":" + tmpInfo.rcv_tid + " start " + tmpInfo.ktime + " android time " + tmpInfo.atime + " spent time " + tmpInfo.spent_time + " ms");
                                        filepath = filepath;
                                        file = file;
                                        patternStr = patternStr;
                                        pattern = pattern;
                                    } catch (FileNotFoundException e3) {
                                        e = e3;
                                        Slog.e(AnrManagerService.TAG, "FileNotFoundException", e);
                                        if (br == null) {
                                        }
                                    } catch (IOException e4) {
                                        e = e4;
                                        Slog.e(AnrManagerService.TAG, "IOException when gettting Binder. ", e);
                                        if (br == null) {
                                        }
                                    }
                                } catch (FileNotFoundException e5) {
                                    e = e5;
                                    Slog.e(AnrManagerService.TAG, "FileNotFoundException", e);
                                    if (br == null) {
                                    }
                                } catch (IOException e6) {
                                    e = e6;
                                    Slog.e(AnrManagerService.TAG, "IOException when gettting Binder. ", e);
                                    if (br == null) {
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    if (br != null) {
                                    }
                                    throw th;
                                }
                            } else {
                                pattern = pattern;
                            }
                        } else if (line.indexOf("node") == -1) {
                            filepath = filepath;
                            file = file;
                            patternStr = patternStr;
                            pattern = pattern;
                        } else if (line.indexOf("node") < 20) {
                            break;
                        } else {
                            filepath = filepath;
                            file = file;
                            patternStr = patternStr;
                            pattern = pattern;
                        }
                    }
                    Iterator<Integer> it = pidList.iterator();
                    while (it.hasNext()) {
                        readTransactionInfoFromFile(it.next().intValue(), binderList);
                    }
                    try {
                        br.close();
                    } catch (IOException ioe3) {
                        Slog.e(AnrManagerService.TAG, "IOException when close buffer reader:", ioe3);
                    }
                }
            } catch (FileNotFoundException e7) {
                e = e7;
                Slog.e(AnrManagerService.TAG, "FileNotFoundException", e);
                if (br == null) {
                }
            } catch (IOException e8) {
                e = e8;
                Slog.e(AnrManagerService.TAG, "IOException when gettting Binder. ", e);
                if (br == null) {
                }
            } catch (Throwable th5) {
                th = th5;
                if (br != null) {
                }
                throw th;
            }
        }

        /* access modifiers changed from: private */
        public static final void setTransactionTimeoutPids(int pid, ArrayList<Integer> desList, SparseArray<Boolean> lastPids) {
            int pidValue;
            ArrayList<Integer> tmpPidList = new ArrayList<>();
            readTransactionInfoFromFile(pid, tmpPidList);
            if (tmpPidList.size() > 0) {
                Iterator<Integer> it = tmpPidList.iterator();
                while (it.hasNext()) {
                    Integer bpid = it.next();
                    if (!(bpid == null || (pidValue = bpid.intValue()) == pid || desList.contains(Integer.valueOf(pidValue)))) {
                        desList.add(Integer.valueOf(pidValue));
                        if (lastPids != null) {
                            lastPids.remove(pidValue);
                        }
                    }
                }
            }
        }
    }

    public void prepareStackTraceFile(String filePath) {
        Slog.i(TAG, "prepareStackTraceFile: " + filePath);
        if (filePath != null && filePath.length() != 0) {
            File traceFile = new File(filePath);
            try {
                File traceDir = traceFile.getParentFile();
                if (traceDir != null) {
                    if (!traceDir.exists()) {
                        traceDir.mkdirs();
                    }
                    FileUtils.setPermissions(traceDir.getPath(), 509, -1, -1);
                }
                if (!traceFile.exists()) {
                    traceFile.createNewFile();
                }
                FileUtils.setPermissions(traceFile.getPath(), 438, -1, -1);
            } catch (IOException e) {
                Slog.e(TAG, "Unable to prepare stack trace file: " + filePath, e);
            }
        }
    }

    public class AnrDumpRecord {
        protected String mAnnotation;
        protected long mAnrTime;
        protected boolean mAppCrashing;
        protected int mAppPid;
        protected String mAppString;
        public String mCpuInfo = null;
        public StringBuilder mInfo = new StringBuilder(256);
        protected boolean mIsCancelled;
        protected boolean mIsCompleted;
        protected int mParentAppPid;
        protected String mParentShortComponentName;
        protected String mProcessName;
        protected String mShortComponentName;

        public AnrDumpRecord(int appPid, boolean appCrashing, String processName, String appString, String shortComponentName, int parentAppPid, String parentShortComponentName, String annotation, long anrTime) {
            this.mAppPid = appPid;
            this.mAppCrashing = appCrashing;
            this.mProcessName = processName;
            this.mAppString = appString;
            this.mShortComponentName = shortComponentName;
            this.mParentAppPid = parentAppPid;
            this.mParentShortComponentName = parentShortComponentName;
            this.mAnnotation = annotation;
            this.mAnrTime = anrTime;
        }

        /* access modifiers changed from: private */
        public boolean isValid() {
            if (this.mAppPid > 0 && !this.mIsCancelled && !this.mIsCompleted) {
                return true;
            }
            Slog.e(AnrManagerService.TAG, "isValid! mAppPid: " + this.mAppPid + "mIsCancelled: " + this.mIsCancelled + "mIsCompleted: " + this.mIsCompleted);
            return false;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("AnrDumpRecord{ ");
            sb.append(this.mAnnotation);
            sb.append(" ");
            sb.append(this.mAppString);
            sb.append(" IsCompleted:" + this.mIsCompleted);
            sb.append(" IsCancelled:" + this.mIsCancelled);
            sb.append(" }");
            return sb.toString();
        }
    }

    public class AnrDumpManager {
        public HashMap<Integer, AnrDumpRecord> mDumpList = new HashMap<>();

        public AnrDumpManager() {
        }

        public void cancelDump(AnrDumpRecord dumpRecord) {
            if (dumpRecord != null && dumpRecord.mAppPid != -1) {
                synchronized (this.mDumpList) {
                    AnrDumpRecord value = this.mDumpList.remove(Integer.valueOf(dumpRecord.mAppPid));
                    if (value != null) {
                        value.mIsCancelled = true;
                    }
                }
            }
        }

        public void removeDumpRecord(AnrDumpRecord dumpRecord) {
            if (dumpRecord != null && dumpRecord.mAppPid != -1) {
                synchronized (this.mDumpList) {
                    this.mDumpList.remove(Integer.valueOf(dumpRecord.mAppPid));
                }
            }
        }

        public void startAsyncDump(AnrDumpRecord dumpRecord, boolean isSilentANR) {
            Slog.i(AnrManagerService.TAG, "startAsyncDump: " + dumpRecord + ", isSilentANR = " + isSilentANR);
            if (dumpRecord != null && dumpRecord.mAppPid != -1) {
                int appPid = dumpRecord.mAppPid;
                synchronized (this.mDumpList) {
                    if (!this.mDumpList.containsKey(Integer.valueOf(appPid))) {
                        this.mDumpList.put(Integer.valueOf(appPid), dumpRecord);
                        Message msg = AnrManagerService.this.mAnrHandler.obtainMessage(AnrManagerService.START_ANR_DUMP_MSG, dumpRecord);
                        msg.arg1 = isSilentANR ? 1 : 0;
                        AnrManagerService.this.mAnrHandler.sendMessageAtTime(msg, SystemClock.uptimeMillis() + 500);
                    }
                }
            }
        }

        private boolean isDumpable(AnrDumpRecord dumpRecord) {
            synchronized (this.mDumpList) {
                if (dumpRecord != null) {
                    if (this.mDumpList.containsKey(Integer.valueOf(dumpRecord.mAppPid)) && dumpRecord.isValid()) {
                        return true;
                    }
                }
                return false;
            }
        }

        public void dumpAnrDebugInfo(AnrDumpRecord dumpRecord, boolean isAsyncDump, boolean isSilentANR) {
            Slog.i(AnrManagerService.TAG, "dumpAnrDebugInfo begin: " + dumpRecord + ", isAsyncDump = " + isAsyncDump + ", isSilentANR = " + isSilentANR);
            if (dumpRecord != null) {
                try {
                    if (!isDumpable(dumpRecord)) {
                        Slog.i(AnrManagerService.TAG, "dumpAnrDebugInfo dump stopped: " + dumpRecord);
                        return;
                    }
                    dumpAnrDebugInfoLocked(dumpRecord, isAsyncDump, isSilentANR);
                    Slog.i(AnrManagerService.TAG, "dumpAnrDebugInfo end: " + dumpRecord + ", isAsyncDump = " + isAsyncDump + " , isSilentANR = " + isSilentANR);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Code restructure failed: missing block: B:111:0x0282, code lost:
            com.mediatek.server.anr.AnrManagerService.access$1600(r19.this$0).invoke(com.mediatek.server.anr.AnrManagerService.access$400(r19.this$0), new java.lang.Object[0]);
            r12.append(r0.printCurrentLoad());
            r12.append(r0);
         */
        public void dumpAnrDebugInfoLocked(AnrDumpRecord dumpRecord, boolean isAsyncDump, boolean isSilentANR) throws Exception {
            int parentPid;
            String cpuInfo;
            int pid;
            synchronized (dumpRecord) {
                Slog.i(AnrManagerService.TAG, "dumpAnrDebugInfoLocked: " + dumpRecord + ", isAsyncDump = " + isAsyncDump + ", isSilentANR = " + isSilentANR);
                if (isDumpable(dumpRecord)) {
                    int appPid = dumpRecord.mAppPid;
                    int parentAppPid = dumpRecord.mParentAppPid;
                    ArrayList<Integer> firstPids = new ArrayList<>();
                    SparseArray<Boolean> lastPids = new SparseArray<>(20);
                    firstPids.add(Integer.valueOf(appPid));
                    if (parentAppPid > 0) {
                        parentPid = parentAppPid;
                    } else {
                        parentPid = appPid;
                    }
                    if (!isSilentANR && parentPid != appPid) {
                        firstPids.add(Integer.valueOf(parentPid));
                    }
                    if (AnrManagerService.this.mAmsPid != appPid && (isSilentANR || AnrManagerService.this.mAmsPid != parentPid)) {
                        firstPids.add(Integer.valueOf(AnrManagerService.this.mAmsPid));
                    }
                    if (!isAsyncDump && !isSilentANR) {
                        synchronized (AnrManagerService.this.mService) {
                            ArrayList<ProcessRecord> mLruProcesses = (ArrayList) AnrManagerService.this.mLruProcessesField.get((ProcessList) AnrManagerService.this.mProcessListField.get(AnrManagerService.this.mService));
                            for (int i = mLruProcesses.size() - 1; i >= 0; i--) {
                                ProcessRecord r = mLruProcesses.get(i);
                                if (!(r == null || ((IApplicationThread) AnrManagerService.this.mThreadField.get(r)) == null || (pid = ((Integer) AnrManagerService.this.mPidField.get(r)).intValue()) <= 0 || pid == appPid || pid == parentPid || pid == AnrManagerService.this.mAmsPid)) {
                                    if (((Boolean) AnrManagerService.this.mPersistentField.get(r)).booleanValue()) {
                                        firstPids.add(Integer.valueOf(pid));
                                    } else {
                                        lastPids.put(pid, Boolean.TRUE);
                                    }
                                }
                            }
                        }
                    }
                    ArrayList<Integer> remotePids = new ArrayList<>();
                    if (appPid != -1) {
                        BinderWatchdog.setTransactionTimeoutPids(appPid, remotePids, lastPids);
                    }
                    String annotation = dumpRecord.mAnnotation;
                    StringBuilder info = dumpRecord.mInfo;
                    info.setLength(0);
                    info.append("ANR in ");
                    info.append(dumpRecord.mProcessName);
                    if (dumpRecord.mShortComponentName != null) {
                        info.append(" (");
                        info.append(dumpRecord.mShortComponentName);
                        info.append(")");
                    }
                    info.append(", time=");
                    info.append(dumpRecord.mAnrTime);
                    info.append("\n");
                    if (annotation != null) {
                        info.append("Reason: ");
                        info.append(annotation);
                        info.append("\n");
                    }
                    if (!(dumpRecord.mParentAppPid == -1 || dumpRecord.mParentAppPid == dumpRecord.mAppPid)) {
                        info.append("Parent: ");
                        info.append(dumpRecord.mParentShortComponentName);
                        info.append("\n");
                    }
                    ProcessCpuTracker processStats = new ProcessCpuTracker(true);
                    if (isDumpable(dumpRecord)) {
                        int[] pids = Process.getPidsForCommands(AnrManagerService.NATIVE_STACKS_OF_INTEREST);
                        ArrayList<Integer> nativePids = null;
                        if (pids != null) {
                            nativePids = new ArrayList<>(pids.length);
                            int length = pids.length;
                            int i2 = 0;
                            while (i2 < length) {
                                nativePids.add(Integer.valueOf(pids[i2]));
                                i2++;
                                parentAppPid = parentAppPid;
                            }
                        }
                        ArrayList<Integer> nativePids2 = nativePids;
                        for (Iterator<Integer> it = remotePids.iterator(); it.hasNext(); it = it) {
                            Integer remotePid = it.next();
                            if (!AnrManagerService.this.isJavaProcess(remotePid.intValue())) {
                                if (nativePids2 == null) {
                                    nativePids2 = new ArrayList<>();
                                }
                                if (!nativePids2.contains(remotePid)) {
                                    nativePids2.add(remotePid);
                                }
                            } else if (!firstPids.contains(remotePid)) {
                                firstPids.add(remotePid);
                            }
                        }
                        Slog.i(AnrManagerService.TAG, "dumpStackTraces begin!");
                        AnrManagerService anrManagerService = AnrManagerService.this;
                        ActivityManagerService unused = AnrManagerService.this.mService;
                        SparseArray<Boolean> sparseArray = null;
                        ProcessCpuTracker processCpuTracker = isSilentANR ? null : processStats;
                        if (!isSilentANR) {
                            sparseArray = lastPids;
                        }
                        File unused2 = anrManagerService.mTracesFile = ActivityManagerService.dumpStackTraces(firstPids, processCpuTracker, sparseArray, nativePids2);
                        Slog.i(AnrManagerService.TAG, "dumpStackTraces end!");
                        if (isDumpable(dumpRecord)) {
                            if (((Boolean) AnrManagerService.this.mMonitorCpuUsageField.get(AnrManagerService.this.mAMS)).booleanValue()) {
                                ProcessCpuTracker mProcessCpuTracker = (ProcessCpuTracker) AnrManagerService.this.mProcessCpuTrackerField.get(AnrManagerService.this.mService);
                                synchronized (mProcessCpuTracker) {
                                    try {
                                        StringBuilder sb = new StringBuilder();
                                        try {
                                            sb.append(AnrManagerService.this.getAndroidTime());
                                            sb.append(mProcessCpuTracker.printCurrentState(dumpRecord.mAnrTime));
                                            cpuInfo = sb.toString();
                                        } catch (Throwable th) {
                                            th = th;
                                            while (true) {
                                                try {
                                                    break;
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                }
                                            }
                                            throw th;
                                        }
                                        try {
                                            dumpRecord.mCpuInfo += cpuInfo;
                                        } catch (Throwable th3) {
                                            th = th3;
                                            while (true) {
                                                break;
                                            }
                                            throw th;
                                        }
                                    } catch (Throwable th4) {
                                        th = th4;
                                        while (true) {
                                            break;
                                        }
                                        throw th;
                                    }
                                }
                            }
                            Slog.i(AnrManagerService.TAG, info.toString());
                            if (isDumpable(dumpRecord)) {
                                if (AnrManagerService.this.mTracesFile == null) {
                                    Process.sendSignal(appPid, 3);
                                }
                                dumpRecord.mIsCompleted = true;
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isJavaProcess(int pid) {
        if (pid <= 0) {
            return false;
        }
        if (mZygotePids == null) {
            mZygotePids = Process.getPidsForCommands(new String[]{"zygote64", "zygote"});
        }
        if (mZygotePids != null) {
            int parentPid = Process.getParentPid(pid);
            for (int zygotePid : mZygotePids) {
                if (parentPid == zygotePid) {
                    return true;
                }
            }
        }
        Slog.i(TAG, "pid: " + pid + " is not a Java process");
        return false;
    }

    private Boolean isException() {
        try {
            if ("free".equals(SystemProperties.get("vendor.debug.mtk.aee.status", "free")) && "free".equals(SystemProperties.get("vendor.debug.mtk.aee.status64", "free")) && "free".equals(SystemProperties.get("vendor.debug.mtk.aee.vstatus", "free")) && "free".equals(SystemProperties.get("vendor.debug.mtk.aee.vstatus64", "free"))) {
                return false;
            }
        } catch (Exception e) {
            Slog.e(TAG, "isException: " + e.toString());
        }
        return true;
    }

    public void informMessageDump(String MessageInfo, int pid) {
        if (mMessageMap.containsKey(Integer.valueOf(pid))) {
            String tmpString = mMessageMap.get(Integer.valueOf(pid));
            if (tmpString.length() > MESSAGE_MAP_BUFFER_SIZE_MAX) {
                tmpString = PalConstDefs.EMPTY_STRING;
            }
            mMessageMap.put(Integer.valueOf(pid), tmpString + MessageInfo);
        } else {
            if (mMessageMap.size() > MESSAGE_MAP_BUFFER_COUNT_MAX) {
                mMessageMap.clear();
            }
            mMessageMap.put(Integer.valueOf(pid), MessageInfo);
        }
        Slog.i(TAG, "informMessageDump pid= " + pid);
    }

    public int checkAnrDebugMechanism() {
        if (!sEnhanceEnable) {
            return 0;
        }
        if (-1 == this.mAnrOption) {
            int option = 2;
            if (IS_USER_BUILD) {
                option = 1;
            }
            this.mAnrOption = SystemProperties.getInt("persist.vendor.anr.enhancement", option);
        }
        int option2 = this.mAnrOption;
        if (option2 == 0) {
            return 0;
        }
        if (option2 != 1) {
            return option2 != 2 ? 2 : 2;
        }
        return 1;
    }

    public void writeEvent(int event) {
        if (event == EVENT_BOOT_COMPLETED) {
            this.mEventBootCompleted = SystemClock.uptimeMillis();
        }
    }

    public boolean isAnrDeferrable() {
        if (checkAnrDebugMechanism() == 0) {
            return false;
        }
        if ("dexopt".equals(SystemProperties.get("vendor.anr.autotest"))) {
            Slog.i(TAG, "We are doing TestDexOptSkipANR; return true in this case");
            return true;
        } else if ("enable".equals(SystemProperties.get("vendor.anr.autotest"))) {
            Slog.i(TAG, "Do Auto Test, don't skip ANR");
            return false;
        } else {
            long now = SystemClock.uptimeMillis();
            if (!IS_USER_BUILD) {
                long j = this.mEventBootCompleted;
                if (j == 0 || now - j < ANR_BOOT_DEFER_TIME) {
                    Slog.i(TAG, "isAnrDeferrable(): true since mEventBootCompleted = " + this.mEventBootCompleted + " now = " + now);
                    return true;
                } else if (isException().booleanValue()) {
                    Slog.i(TAG, "isAnrDeferrable(): true since exception");
                    return true;
                } else {
                    float lastCpuUsage = mAnrProcessStats.getTotalCpuPercent();
                    updateProcessStats();
                    float currentCpuUsage = mAnrProcessStats.getTotalCpuPercent();
                    if (lastCpuUsage > ANR_CPU_THRESHOLD && currentCpuUsage > ANR_CPU_THRESHOLD) {
                        long j2 = this.mCpuDeferred;
                        if (j2 == 0) {
                            this.mCpuDeferred = now;
                            Slog.i(TAG, "isAnrDeferrable(): true since CpuUsage = " + currentCpuUsage + ", mCpuDeferred = " + this.mCpuDeferred);
                            return true;
                        } else if (now - j2 < ANR_CPU_DEFER_TIME) {
                            Slog.i(TAG, "isAnrDeferrable(): true since CpuUsage = " + currentCpuUsage + ", mCpuDeferred = " + this.mCpuDeferred + ", now = " + now);
                            return true;
                        }
                    }
                    this.mCpuDeferred = 0;
                }
            }
            return false;
        }
    }

    public boolean isAnrFlowSkipped(int appPid, String appProcessName, String annotation) {
        if (-1 == this.mAnrFlow) {
            this.mAnrFlow = SystemProperties.getInt("persist.vendor.dbg.anrflow", 0);
        }
        Slog.i(TAG, "isANRFlowSkipped() AnrFlow = " + this.mAnrFlow);
        int i = this.mAnrFlow;
        if (i == 0) {
            return false;
        }
        if (i == 1) {
            Slog.i(TAG, "Skipping ANR flow: " + appPid + " " + appProcessName + " " + annotation);
            return true;
        } else if (i != 2) {
            return false;
        } else {
            if (appPid != Process.myPid()) {
                Slog.i(TAG, "Skipping ANR flow: " + appPid + " " + appProcessName + " " + annotation);
                StringBuilder sb = new StringBuilder();
                sb.append("Kill process (");
                sb.append(appPid);
                sb.append(") due to ANR");
                Slog.w(TAG, sb.toString());
                Process.killProcess(appPid);
            }
            return true;
        }
    }

    public void updateProcessStats() {
        synchronized (mAnrProcessStats) {
            long now = SystemClock.uptimeMillis();
            if (now - this.mLastCpuUpdateTime.get() > MONITOR_CPU_MIN_TIME) {
                this.mLastCpuUpdateTime.set(now);
                mAnrProcessStats.update();
            }
        }
    }

    public String getProcessState() {
        String printCurrentState;
        synchronized (mAnrProcessStats) {
            printCurrentState = mAnrProcessStats.printCurrentState(SystemClock.uptimeMillis());
        }
        return printCurrentState;
    }

    public String getAndroidTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
        Date date = new Date(System.currentTimeMillis());
        Formatter formatter = new Formatter();
        return "Android time :[" + simpleDateFormat.format(date) + "] [" + formatter.format("%.3f", Float.valueOf(((float) SystemClock.uptimeMillis()) / 1000.0f)) + "]\n";
    }

    public File createFile(String filepath) {
        File file = new File(filepath);
        if (file.exists()) {
            return file;
        }
        Slog.i(TAG, filepath + " isn't exist");
        return null;
    }

    public boolean copyFile(File srcFile, File destFile) {
        try {
            if (!srcFile.exists()) {
                return false;
            }
            if (!destFile.exists()) {
                destFile.createNewFile();
                FileUtils.setPermissions(destFile.getPath(), 438, -1, -1);
            }
            InputStream in = new FileInputStream(srcFile);
            try {
                return copyToFile(in, destFile);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            Slog.e(TAG, "createFile fail");
            return false;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.io.FileOutputStream.<init>(java.io.File, boolean):void throws java.io.FileNotFoundException}
     arg types: [java.io.File, int]
     candidates:
      ClspMth{java.io.FileOutputStream.<init>(java.lang.String, boolean):void throws java.io.FileNotFoundException}
      ClspMth{java.io.FileOutputStream.<init>(java.io.File, boolean):void throws java.io.FileNotFoundException} */
    public boolean copyToFile(InputStream inputStream, File destFile) {
        FileOutputStream out = null;
        try {
            FileOutputStream out2 = new FileOutputStream(destFile, true);
            byte[] buffer = new byte[4096];
            while (true) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead < 0) {
                    break;
                }
                out2.write(buffer, 0, bytesRead);
            }
            out2.flush();
            out2.getFD().sync();
            try {
                out2.close();
            } catch (IOException e) {
                Slog.w(TAG, "close failed..");
            }
            return true;
        } catch (IOException e2) {
            Slog.w(TAG, "copyToFile fail", e2);
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e3) {
                    Slog.w(TAG, "close failed..");
                }
            }
            return false;
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e4) {
                    Slog.w(TAG, "close failed..");
                }
            }
            throw th;
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.io.FileWriter.<init>(java.lang.String, boolean):void throws java.io.IOException}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{java.io.FileWriter.<init>(java.io.File, boolean):void throws java.io.IOException}
      ClspMth{java.io.FileWriter.<init>(java.lang.String, boolean):void throws java.io.IOException} */
    public void stringToFile(String filename, String string) throws IOException {
        FileWriter out = new FileWriter(filename, true);
        try {
            out.write(string);
        } finally {
            out.close();
        }
    }

    public class BinderDumpThread extends Thread {
        private int mPid;

        public BinderDumpThread(int pid) {
            this.mPid = pid;
        }

        public void run() {
            AnrManagerService.this.dumpBinderInfo(this.mPid);
        }
    }

    public void dumpBinderInfo(int pid) {
        try {
            File binderinfo = new File("/data/anr/binderinfo");
            if (binderinfo.exists()) {
                if (!binderinfo.delete()) {
                    Slog.e(TAG, "dumpBinderInfo fail due to file likely to be locked by others");
                    return;
                } else if (!binderinfo.createNewFile()) {
                    Slog.e(TAG, "dumpBinderInfo fail due to file cannot be created");
                    return;
                } else {
                    FileUtils.setPermissions(binderinfo.getPath(), 438, -1, -1);
                }
            }
            File file = createFile("/sys/kernel/debug/binder/failed_transaction_log");
            if (file != null) {
                stringToFile("/data/anr/binderinfo", "------ BINDER FAILED TRANSACTION LOG ------\n");
                copyFile(file, binderinfo);
            }
            File file2 = createFile("sys/kernel/debug/binder/timeout_log");
            if (file2 != null) {
                stringToFile("/data/anr/binderinfo", "------ BINDER TIMEOUT LOG ------\n");
                copyFile(file2, binderinfo);
            }
            File file3 = createFile("/sys/kernel/debug/binder/transaction_log");
            if (file3 != null) {
                stringToFile("/data/anr/binderinfo", "------ BINDER TRANSACTION LOG ------\n");
                copyFile(file3, binderinfo);
            }
            File file4 = createFile("/sys/kernel/debug/binder/transactions");
            if (file4 != null) {
                stringToFile("/data/anr/binderinfo", "------ BINDER TRANSACTIONS ------\n");
                copyFile(file4, binderinfo);
            }
            File file5 = createFile("/sys/kernel/debug/binder/stats");
            if (file5 != null) {
                stringToFile("/data/anr/binderinfo", "------ BINDER STATS ------\n");
                copyFile(file5, binderinfo);
            }
            File file6 = new File("/sys/kernel/debug/binder/proc/" + Integer.toString(pid));
            stringToFile("/data/anr/binderinfo", "------ BINDER PROCESS STATE: $i ------\n");
            copyFile(file6, binderinfo);
        } catch (IOException e) {
            Slog.e(TAG, "dumpBinderInfo fail");
        }
    }

    public void enableTraceLog(boolean enable) {
        Slog.i(TAG, "enableTraceLog: " + enable);
        ExceptionLog exceptionLog2 = this.exceptionLog;
        if (exceptionLog2 != null) {
            exceptionLog2.switchFtrace(enable ? 1 : 0);
        }
    }

    private void writeStringToFile(String filepath, String string) {
        StringBuilder sb;
        if (filepath != null) {
            File file = new File(filepath);
            FileOutputStream out = null;
            StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
            StrictMode.allowThreadDiskWrites();
            try {
                out = new FileOutputStream(file);
                out.write(string.getBytes());
                out.flush();
                try {
                    out.close();
                } catch (IOException e) {
                    ioe = e;
                    sb = new StringBuilder();
                }
            } catch (IOException e2) {
                Slog.e(TAG, "writeStringToFile error: " + filepath + " " + e2.toString());
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e3) {
                        ioe = e3;
                        sb = new StringBuilder();
                    }
                }
            } catch (Throwable th) {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ioe) {
                        Slog.e(TAG, "writeStringToFile close error: " + filepath + " " + ioe.toString());
                    }
                }
                StrictMode.setThreadPolicy(oldPolicy);
                throw th;
            }
            StrictMode.setThreadPolicy(oldPolicy);
        }
        return;
        sb.append("writeStringToFile close error: ");
        sb.append(filepath);
        sb.append(" ");
        sb.append(ioe.toString());
        Slog.e(TAG, sb.toString());
        StrictMode.setThreadPolicy(oldPolicy);
    }

    private boolean isBuiltinApp(ApplicationInfo appInfo) {
        return ((appInfo.flags & 1) == 0 && (appInfo.flags & 128) == 0) ? false : true;
    }

    private boolean needAnrDump(ApplicationInfo appInfo) {
        return isBuiltinApp(appInfo) || SystemProperties.getInt("persist.vendor.anr.dumpthr", 1) != 0;
    }
}
