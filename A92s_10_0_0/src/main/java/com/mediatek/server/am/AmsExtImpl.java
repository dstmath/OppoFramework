package com.mediatek.server.am;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.am.ActivityManagerDebugConfig;
import com.android.server.am.ProcessRecord;
import com.android.server.wm.ActivityRecord;
import com.android.server.wm.ActivityTaskManagerDebugConfig;
import com.mediatek.amsAal.AalUtils;
import com.mediatek.cta.CtaManager;
import com.mediatek.cta.CtaManagerFactory;
import com.mediatek.duraspeed.manager.IDuraSpeedNative;
import com.mediatek.duraspeed.suppress.ISuppressAction;
import com.mediatek.omadm.PalConstDefs;
import com.mediatek.server.dx.DexOptExtImpl;
import com.mediatek.server.powerhal.PowerHalManagerImpl;
import dalvik.system.PathClassLoader;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AmsExtImpl extends AmsExt {
    private static final String CASE_PREF = "case";
    private static final String INKERNEL_MINFREE_PATH = "/sys/module/lowmemorykiller/parameters/minfree";
    private static final String SANITY_CASE_PREF = "sanity";
    private static final String TAG = "AmsExtImpl";
    private static final String[] WHITE_LIST = new String[0];
    public static PathClassLoader sClassLoader;
    private final String amsLogProp;
    private boolean isDebug;
    private boolean isDuraSpeedSupport;
    private boolean isHeavyLoadingSupport;
    private AalUtils mAalUtils;
    private ActivityManagerInternal mActivityManagerInternal;
    private ActivityManager mAm;
    private Context mContext;
    private CtaManager mCtaManager;
    private String mCurrentPackage;
    private DexOptExtImpl mDexOptExtImpl;
    /* access modifiers changed from: private */
    public IDuraSpeedNative mDuraSpeedService;
    private String mLastActivityName;
    private String mLastPackageName;
    private PackageManagerInternal mPackageManagerInternal;
    public PowerHalManagerImpl mPowerHalManagerImpl;
    private Field mProcessNamesField;
    private Method mStartProcessMethod;
    private ISuppressAction mSuppressAction;
    private ArrayList<String> mSuppressList;

    public AmsExtImpl() {
        this.isDebug = false;
        this.isDuraSpeedSupport = "1".equals(SystemProperties.get("persist.vendor.duraspeed.support"));
        this.isHeavyLoadingSupport = "1".equals(SystemProperties.get("persist.vendor.heavy.loading.support"));
        this.mSuppressList = new ArrayList<>();
        this.mPowerHalManagerImpl = null;
        this.mAalUtils = null;
        this.amsLogProp = "persist.vendor.sys.activitylog";
        this.mCtaManager = null;
        this.mAm = null;
        this.mPowerHalManagerImpl = new PowerHalManagerImpl();
        this.mDexOptExtImpl = DexOptExtImpl.getInstance();
        if (this.isDuraSpeedSupport) {
            try {
                sClassLoader = new PathClassLoader("/system/framework/duraspeed.jar", AmsExtImpl.class.getClassLoader());
                this.mDuraSpeedService = (IDuraSpeedNative) Class.forName("com.mediatek.duraspeed.manager.DuraSpeedService", false, sClassLoader).getConstructor(new Class[0]).newInstance(new Object[0]);
                this.mSuppressAction = (ISuppressAction) Class.forName("com.mediatek.duraspeed.suppress.SuppressAction", false, sClassLoader).getConstructor(new Class[0]).newInstance(new Object[0]);
            } catch (Exception e) {
                Slog.e(TAG, e.toString());
            }
        }
        if (this.mAalUtils == null && AalUtils.isSupported()) {
            this.mAalUtils = AalUtils.getInstance();
        }
    }

    public void onAddErrorToDropBox(String dropboxTag, String info, int pid) {
        if (this.isDebug) {
            Slog.d(TAG, "onAddErrorToDropBox, dropboxTag=" + dropboxTag + ", info=" + info + ", pid=" + pid);
        }
    }

    public void onSystemReady(Context context) {
        IDuraSpeedNative iDuraSpeedNative;
        Slog.d(TAG, "onSystemReady");
        if (this.isDuraSpeedSupport && (iDuraSpeedNative = this.mDuraSpeedService) != null) {
            iDuraSpeedNative.onSystemReady();
        }
        this.mContext = context;
    }

    public void onBeforeActivitySwitch(ActivityRecord lastResumedActivity, ActivityRecord nextResumedActivity, boolean pausing, int nextResumedActivityType) {
        IDuraSpeedNative iDuraSpeedNative;
        if (nextResumedActivity != null && nextResumedActivity.info != null && lastResumedActivity != null) {
            if (nextResumedActivity.packageName != lastResumedActivity.packageName || nextResumedActivity.info.name != lastResumedActivity.info.name) {
                String lastResumedPackageName = lastResumedActivity.packageName;
                String nextResumedPackageName = nextResumedActivity.packageName;
                if (this.isDebug) {
                    Slog.d(TAG, "onBeforeActivitySwitch, lastResumedPackageName=" + lastResumedPackageName + ", nextResumedPackageName=" + nextResumedPackageName);
                }
                PowerHalManagerImpl powerHalManagerImpl = this.mPowerHalManagerImpl;
                if (powerHalManagerImpl != null) {
                    powerHalManagerImpl.amsBoostResume(lastResumedPackageName, nextResumedPackageName);
                }
                if (this.isDuraSpeedSupport && (iDuraSpeedNative = this.mDuraSpeedService) != null) {
                    iDuraSpeedNative.onBeforeActivitySwitch(lastResumedActivity, nextResumedActivity, pausing, nextResumedActivityType);
                }
                checkSuppressInfo(lastResumedPackageName, nextResumedPackageName);
            }
        }
    }

    public void onAfterActivityResumed(ActivityRecord resumedActivity) {
        if (resumedActivity.app != null) {
            int pid = resumedActivity.app.mPid;
            int uid = resumedActivity.app.mUid;
            String activityName = resumedActivity.info.name;
            String packageName = resumedActivity.info.packageName;
            if (this.isDebug) {
                Slog.d(TAG, "onAfterActivityResumed, pid=" + pid + ", activityName=" + activityName + ", packageName=" + packageName);
            }
            amsBoostNotify(pid, activityName, packageName, uid);
            AalUtils aalUtils = this.mAalUtils;
            if (aalUtils != null) {
                aalUtils.onAfterActivityResumed(packageName, activityName);
            }
        }
    }

    public void onTopResumedActivityChanged(ActivityRecord resumedActivity) {
        if (resumedActivity.app != null) {
            int pid = resumedActivity.app.mPid;
            int uid = resumedActivity.app.mUid;
            String activityName = resumedActivity.info.name;
            String packageName = resumedActivity.info.packageName;
            if (this.isDebug) {
                Slog.d(TAG, "onTopResumedActivityChanged, pid=" + pid + ", activityName=" + activityName + ", packageName=" + packageName);
            }
            amsBoostNotify(pid, activityName, packageName, uid);
        }
    }

    private void amsBoostNotify(int pid, String activityName, String packageName, int uid) {
        if (this.mPowerHalManagerImpl != null) {
            String str = this.mLastPackageName;
            if (!(str == null || this.mLastActivityName == null || (str.equals(packageName) && this.mLastActivityName.equals(activityName)))) {
                try {
                    PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(packageName, 0);
                    if (packageInfo != null) {
                        this.mPowerHalManagerImpl.setVersionInfo(packageInfo.versionName);
                    } else {
                        Slog.w(TAG, "amsBoostNotify packageInfo is null.");
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "amsBoostNotify getCallerProcessName exception :" + e);
                }
            }
            this.mPowerHalManagerImpl.amsBoostNotify(pid, activityName, packageName, uid);
            this.mLastPackageName = packageName;
            this.mLastActivityName = activityName;
            return;
        }
        Slog.w(TAG, "amsBoostNotify mPowerHalManagerImpl is null.");
    }

    public void onUpdateSleep(boolean wasSleeping, boolean isSleepingAfterUpdate) {
        if (this.isDebug) {
            Slog.d(TAG, "onUpdateSleep, wasSleeping=" + wasSleeping + ", isSleepingAfterUpdate=" + isSleepingAfterUpdate);
        }
        AalUtils aalUtils = this.mAalUtils;
        if (aalUtils != null) {
            aalUtils.onUpdateSleep(wasSleeping, isSleepingAfterUpdate);
        }
    }

    public void setAalMode(int mode) {
        AalUtils aalUtils = this.mAalUtils;
        if (aalUtils != null) {
            aalUtils.setAalMode(mode);
        }
    }

    public void setAalEnabled(boolean enabled) {
        AalUtils aalUtils = this.mAalUtils;
        if (aalUtils != null) {
            aalUtils.setEnabled(enabled);
        }
    }

    public int amsAalDump(PrintWriter pw, String[] args, int opti) {
        AalUtils aalUtils = this.mAalUtils;
        if (aalUtils != null) {
            return aalUtils.dump(pw, args, opti);
        }
        return opti;
    }

    public void onStartProcess(String hostingType, String packageName) {
        if (this.isDebug) {
            Slog.d(TAG, "onStartProcess, hostingType=" + hostingType + ", packageName=" + packageName);
        }
        PowerHalManagerImpl powerHalManagerImpl = this.mPowerHalManagerImpl;
        if (powerHalManagerImpl != null) {
            powerHalManagerImpl.amsBoostProcessCreate(hostingType, packageName);
        }
        DexOptExtImpl dexOptExtImpl = this.mDexOptExtImpl;
        if (dexOptExtImpl != null) {
            dexOptExtImpl.onStartProcess(hostingType, packageName);
        }
    }

    public void onNotifyAppCrash(int pid, int uid, String packageName) {
        if (this.isDebug) {
            Slog.d(TAG, "onNotifyAppCrash, packageName=" + packageName + ", pid=" + pid);
        }
        PowerHalManagerImpl powerHalManagerImpl = this.mPowerHalManagerImpl;
        if (powerHalManagerImpl != null) {
            powerHalManagerImpl.NotifyAppCrash(pid, uid, packageName);
        }
    }

    public void onEndOfActivityIdle(Context context, Intent idleIntent) {
        IDuraSpeedNative iDuraSpeedNative;
        if (this.isDebug) {
            Slog.d(TAG, "onEndOfActivityIdle, idleIntent=" + idleIntent);
        }
        PowerHalManagerImpl powerHalManagerImpl = this.mPowerHalManagerImpl;
        if (powerHalManagerImpl != null) {
            powerHalManagerImpl.amsBoostStop();
        }
        if (this.isDuraSpeedSupport && (iDuraSpeedNative = this.mDuraSpeedService) != null) {
            iDuraSpeedNative.onActivityIdle(context, idleIntent);
        }
    }

    public void enableAmsLog(ArrayList<ProcessRecord> lruProcesses) {
        String activitylog = SystemProperties.get("persist.vendor.sys.activitylog", (String) null);
        if (activitylog != null && !activitylog.equals(PalConstDefs.EMPTY_STRING)) {
            if (activitylog.indexOf(" ") == -1 || activitylog.indexOf(" ") + 1 > activitylog.length()) {
                SystemProperties.set("persist.vendor.sys.activitylog", PalConstDefs.EMPTY_STRING);
                return;
            }
            enableAmsLog(null, new String[]{activitylog.substring(0, activitylog.indexOf(" ")), activitylog.substring(activitylog.indexOf(" ") + 1, activitylog.length())}, 0, lruProcesses);
        }
    }

    public void enableAmsLog(PrintWriter pw, String[] args, int opti, ArrayList<ProcessRecord> lruProcesses) {
        int indexLast = opti + 1;
        if (indexLast >= args.length) {
            if (pw != null) {
                pw.println("  Invalid argument!");
            }
            SystemProperties.set("persist.vendor.sys.activitylog", PalConstDefs.EMPTY_STRING);
            return;
        }
        String option = args[opti];
        boolean isEnable = "on".equals(args[indexLast]);
        SystemProperties.set("persist.vendor.sys.activitylog", args[opti] + " " + args[indexLast]);
        if (option.equals("x")) {
            enableAmsLog(isEnable, lruProcesses);
            return;
        }
        if (pw != null) {
            pw.println("  Invalid argument!");
        }
        SystemProperties.set("persist.vendor.sys.activitylog", PalConstDefs.EMPTY_STRING);
    }

    private void enableAmsLog(boolean isEnable, ArrayList<ProcessRecord> lruProcesses) {
        this.isDebug = isEnable;
        ActivityManagerDebugConfig.APPEND_CATEGORY_NAME = isEnable;
        ActivityManagerDebugConfig.DEBUG_ALL = isEnable;
        ActivityManagerDebugConfig.DEBUG_ANR = isEnable;
        ActivityManagerDebugConfig.DEBUG_BACKGROUND_CHECK = isEnable;
        ActivityManagerDebugConfig.DEBUG_BACKUP = isEnable;
        ActivityManagerDebugConfig.DEBUG_BROADCAST = isEnable;
        ActivityManagerDebugConfig.DEBUG_BROADCAST_BACKGROUND = isEnable;
        ActivityManagerDebugConfig.DEBUG_BROADCAST_LIGHT = isEnable;
        ActivityManagerDebugConfig.DEBUG_BROADCAST_DEFERRAL = isEnable;
        ActivityManagerDebugConfig.DEBUG_LRU = isEnable;
        ActivityManagerDebugConfig.DEBUG_MU = isEnable;
        ActivityManagerDebugConfig.DEBUG_NETWORK = isEnable;
        ActivityManagerDebugConfig.DEBUG_POWER = isEnable;
        ActivityManagerDebugConfig.DEBUG_POWER_QUICK = isEnable;
        ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS = isEnable;
        ActivityManagerDebugConfig.DEBUG_PROCESSES = isEnable;
        ActivityManagerDebugConfig.DEBUG_PROVIDER = isEnable;
        ActivityManagerDebugConfig.DEBUG_PSS = isEnable;
        ActivityManagerDebugConfig.DEBUG_SERVICE = isEnable;
        ActivityManagerDebugConfig.DEBUG_FOREGROUND_SERVICE = isEnable;
        ActivityManagerDebugConfig.DEBUG_SERVICE_EXECUTING = isEnable;
        ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS = isEnable;
        ActivityManagerDebugConfig.DEBUG_USAGE_STATS = isEnable;
        ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW = isEnable;
        ActivityManagerDebugConfig.DEBUG_WHITELISTS = isEnable;
        ActivityTaskManagerDebugConfig.APPEND_CATEGORY_NAME = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_ALL = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_ALL_ACTIVITIES = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_ADD_REMOVE = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_CONTAINERS = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_FOCUS = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_IMMERSIVE = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_LOCKTASK = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_PAUSE = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_RECENTS = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_RECENTS_TRIM_TASKS = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_SAVED_STATE = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_STACK = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_STATES = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_SWITCH = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_TASKS = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_TRANSITION = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_APP = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_IDLE = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_RELEASE = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_USER_LEAVING = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_RESULTS = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_CLEANUP = isEnable;
        ActivityTaskManagerDebugConfig.DEBUG_METRICS = isEnable;
        for (int i = 0; i < lruProcesses.size(); i++) {
            ProcessRecord app = lruProcesses.get(i);
            if (!(app == null || app.thread == null)) {
                try {
                    app.thread.enableActivityThreadLog(isEnable);
                } catch (Exception e) {
                    Slog.e(TAG, "Error happens when enableActivityThreadLog", e);
                }
            }
        }
    }

    public void onWakefulnessChanged(int wakefulness) {
        IDuraSpeedNative iDuraSpeedNative;
        if (this.isDuraSpeedSupport && (iDuraSpeedNative = this.mDuraSpeedService) != null) {
            iDuraSpeedNative.onWakefulnessChanged(wakefulness);
        }
    }

    public void addDuraSpeedService() {
        IDuraSpeedNative iDuraSpeedNative;
        if (this.isDuraSpeedSupport && (iDuraSpeedNative = this.mDuraSpeedService) != null) {
            ServiceManager.addService("duraspeed", (IBinder) iDuraSpeedNative, true);
        }
    }

    public void startDuraSpeedService(Context context) {
        IDuraSpeedNative iDuraSpeedNative;
        if (this.isDuraSpeedSupport && (iDuraSpeedNative = this.mDuraSpeedService) != null) {
            iDuraSpeedNative.startDuraSpeedService(context);
            if (!new File(INKERNEL_MINFREE_PATH).exists()) {
                new MemoryServerThread().start();
            }
        }
    }

    public String onReadyToStartComponent(String packageName, int uid, String suppressReason, String className) {
        IDuraSpeedNative iDuraSpeedNative = this.mDuraSpeedService;
        if (iDuraSpeedNative == null || !iDuraSpeedNative.isDuraSpeedEnabled()) {
            return null;
        }
        return this.mSuppressAction.onReadyToStartComponent(packageName, uid, suppressReason, className);
    }

    public boolean onBeforeStartProcessForStaticReceiver(String packageName) {
        IDuraSpeedNative iDuraSpeedNative = this.mDuraSpeedService;
        if (iDuraSpeedNative == null || !iDuraSpeedNative.isDuraSpeedEnabled()) {
            return false;
        }
        return this.mSuppressAction.onBeforeStartProcessForStaticReceiver(packageName);
    }

    public void addToSuppressRestartList(String packageName) {
        Context context;
        IDuraSpeedNative iDuraSpeedNative = this.mDuraSpeedService;
        if (iDuraSpeedNative != null && iDuraSpeedNative.isDuraSpeedEnabled() && (context = this.mContext) != null) {
            this.mSuppressAction.addToSuppressRestartList(context, packageName);
        }
    }

    public boolean notRemoveAlarm(String packageName) {
        IDuraSpeedNative iDuraSpeedNative = this.mDuraSpeedService;
        if (iDuraSpeedNative == null || !iDuraSpeedNative.isDuraSpeedEnabled()) {
            return false;
        }
        return this.mSuppressAction.notRemoveAlarm(packageName);
    }

    public boolean IsBuildInApp() {
        IPackageManager pm = AppGlobals.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(pm.getNameForUid(Binder.getCallingUid()), 0, UserHandle.getCallingUserId());
            if (appInfo == null || ((appInfo.flags & 1) == 0 && (appInfo.flags & 128) == 0)) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            Slog.e(TAG, "getCallerProcessName exception :" + e);
        }
    }

    public boolean preLaunchApplication(String callingPackage, Intent intent, String resolvedType, int startFlags) {
        int modifiedFlags;
        ActivityInfo aInfo;
        if (!"com.mediatek.duraspeedml".equals(callingPackage)) {
            return false;
        }
        int modifiedFlags2 = startFlags | 65536 | 1024;
        if (intent.isWebIntent() || (intent.getFlags() & 2048) != 0) {
            modifiedFlags = modifiedFlags2 | 8388608;
        } else {
            modifiedFlags = modifiedFlags2;
        }
        if (this.mActivityManagerInternal == null) {
            this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        }
        if (this.mPackageManagerInternal == null) {
            this.mPackageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        }
        ResolveInfo rInfo = this.mPackageManagerInternal.resolveIntent(intent, resolvedType, modifiedFlags, UserHandle.getCallingUserId(), true, Binder.getCallingUid());
        if (rInfo == null || (aInfo = rInfo.activityInfo) == null || aInfo.applicationInfo == null) {
            return true;
        }
        if (this.mStartProcessMethod == null) {
            try {
                this.mStartProcessMethod = ActivityManagerInternal.class.getDeclaredMethod("startProcess", String.class, ApplicationInfo.class, Boolean.TYPE, String.class, ComponentName.class);
                this.mStartProcessMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                Slog.e(TAG, "preLaunchApplication, no such method" + e.getMessage());
            }
        }
        Method method = this.mStartProcessMethod;
        if (method != null) {
            try {
                method.invoke(this.mActivityManagerInternal, aInfo.processName, aInfo.applicationInfo, true, "activity", intent.getComponent());
            } catch (IllegalAccessException e2) {
                Slog.e(TAG, "preLaunchApplication, IllegalAccessException:" + e2.getMessage());
            } catch (IllegalArgumentException e3) {
                Slog.e(TAG, "preLaunchApplication, IllegalArgumentException:" + e3.getMessage());
            } catch (InvocationTargetException e4) {
                Slog.e(TAG, "preLaunchApplication, InvocationTargetException:" + e4.getMessage());
            }
        }
        return true;
    }

    public boolean checkAutoBootPermission(Context context, String packageName, int userId, ArrayList<ProcessRecord> runningProcess, int callingPid) {
        if (this.mCtaManager == null) {
            this.mCtaManager = CtaManagerFactory.getInstance().makeCtaManager();
        }
        if (!this.mCtaManager.isCtaSupported()) {
            return true;
        }
        for (int i = runningProcess.size() - 1; i >= 0; i--) {
            ProcessRecord processRecord = runningProcess.get(i);
            if (processRecord.pid == callingPid && processRecord.curAdj > 200) {
                boolean result = this.mCtaManager.checkAutoBootPermission(context, packageName, userId);
                Slog.e(TAG, "check result:" + result);
                if (!result) {
                    Slog.e(TAG, "can't start procss because auto boot permission. calling package:" + processRecord.processName + " start process:" + packageName);
                    if (this.mAm == null) {
                        this.mAm = (ActivityManager) context.getSystemService("activity");
                    }
                    this.mAm.forceStopPackageAsUser(packageName, userId);
                    return false;
                }
            }
        }
        return true;
    }

    public void forceStopRelatedApps(Context context, ProcessRecord app, ApplicationInfo appInfo, int userId, ArrayList<ProcessRecord> lruProcesses) {
        if (!this.isDuraSpeedSupport && this.isHeavyLoadingSupport) {
            int minAdj = app.curAdj;
            if (!appInfo.isSystemApp() && app.curAdj > 200 && !isDefaultWhitelistAPP(appInfo.packageName) && !this.mSuppressList.contains(appInfo.packageName)) {
                Iterator<ProcessRecord> it = lruProcesses.iterator();
                while (it.hasNext()) {
                    ProcessRecord pr = it.next();
                    for (String packageName : pr.getPackageList()) {
                        if (appInfo.packageName.equals(packageName)) {
                            minAdj = Math.min(pr.curAdj, minAdj);
                        }
                    }
                }
                if (minAdj > 200) {
                    if (this.mAm == null) {
                        this.mAm = (ActivityManager) context.getSystemService("activity");
                    }
                    Slog.d(TAG, "force stop process: " + app.processName + " curAdj=" + app.curAdj + " packageName: " + appInfo.packageName + " minAdj= " + minAdj);
                    this.mSuppressList.add(appInfo.packageName);
                    this.mAm.forceStopPackageAsUser(appInfo.packageName, userId);
                }
            }
        }
    }

    public boolean isComponentNeedsStart(String packageName, String suppressReason) {
        if (this.isDuraSpeedSupport || !this.isHeavyLoadingSupport || !this.mSuppressList.contains(packageName)) {
            return true;
        }
        Slog.d(TAG, "the " + suppressReason + " can't start, related packageName: " + packageName + " is in the suppress list");
        return false;
    }

    private void checkSuppressInfo(String lastResumedPackageName, String nextResumedPackageName) {
        if (!this.isDuraSpeedSupport && this.isHeavyLoadingSupport) {
            if (lastResumedPackageName == null || !lastResumedPackageName.equals(nextResumedPackageName)) {
                String str = this.mCurrentPackage;
                if (str == null || !str.equals(nextResumedPackageName)) {
                    this.mCurrentPackage = nextResumedPackageName;
                    if (this.mSuppressList.contains(this.mCurrentPackage)) {
                        Slog.d(TAG, "maybe user start the app, packageName: " + this.mCurrentPackage + ", need remove it from suppress List");
                        this.mSuppressList.remove(this.mCurrentPackage);
                    }
                }
            }
        }
    }

    private boolean isDefaultWhitelistAPP(String packageName) {
        if (packageName.startsWith("android") || packageName.startsWith("com.android") || packageName.startsWith("com.google.android") || packageName.toLowerCase().contains(SANITY_CASE_PREF) || packageName.toLowerCase().contains(CASE_PREF)) {
            return true;
        }
        for (String whitelist : WHITE_LIST) {
            if (packageName.equals(whitelist)) {
                return true;
            }
        }
        return false;
    }

    private class MemoryServerThread extends Thread {
        public static final String HOST_NAME = "duraspeed_memory";

        private MemoryServerThread() {
        }

        public void run() {
            LocalServerSocket serverSocket = null;
            ExecutorService threadExecutor = Executors.newCachedThreadPool();
            try {
                Slog.d(AmsExtImpl.TAG, "Crate local socket: duraspeed_memory");
                LocalServerSocket serverSocket2 = new LocalServerSocket(HOST_NAME);
                while (true) {
                    Slog.d(AmsExtImpl.TAG, "Waiting Client connected...");
                    LocalSocket socket = serverSocket2.accept();
                    socket.setReceiveBufferSize(256);
                    socket.setSendBufferSize(256);
                    Slog.i(AmsExtImpl.TAG, "There is a client is accepted: " + socket.toString());
                    threadExecutor.execute(new ConnectionHandler(socket));
                }
            } catch (Exception e) {
                Slog.w(AmsExtImpl.TAG, "listenConnection catch Exception");
                e.printStackTrace();
                Slog.d(AmsExtImpl.TAG, "listenConnection finally shutdown!!");
                if (threadExecutor != null) {
                    threadExecutor.shutdown();
                }
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                Slog.d(AmsExtImpl.TAG, "listenConnection() - end");
            } catch (Throwable th) {
                Slog.d(AmsExtImpl.TAG, "listenConnection finally shutdown!!");
                if (threadExecutor != null) {
                    threadExecutor.shutdown();
                }
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }

    public class ConnectionHandler implements Runnable {
        private InputStreamReader mInput = null;
        private boolean mIsContinue = true;
        private DataOutputStream mOutput = null;
        private LocalSocket mSocket;

        public ConnectionHandler(LocalSocket clientSocket) {
            this.mSocket = clientSocket;
        }

        public void terminate() {
            Slog.d(AmsExtImpl.TAG, "DuraSpeed memory trigger process terminate.");
            this.mIsContinue = false;
        }

        public void run() {
            Slog.i(AmsExtImpl.TAG, "DuraSpeed new connection: " + this.mSocket.toString());
            try {
                this.mInput = new InputStreamReader(this.mSocket.getInputStream());
                this.mOutput = new DataOutputStream(this.mSocket.getOutputStream());
                try {
                    BufferedReader bufferedReader = new BufferedReader(this.mInput);
                    while (this.mIsContinue) {
                        String[] result = bufferedReader.readLine().split(":");
                        if (result[0] != null) {
                            if (result[1] != null) {
                                Integer.parseInt(result[0].trim());
                                int minFree = Integer.parseInt(result[1].trim());
                                String level = result[2].trim();
                                String memPressrue = result[3].trim();
                                Integer.parseInt(level);
                                int memPressrueResult = Integer.parseInt(memPressrue);
                                if (AmsExtImpl.this.mDuraSpeedService.isDuraSpeedEnabled()) {
                                    AmsExtImpl.this.mDuraSpeedService.triggerMemory(minFree * 4, memPressrueResult);
                                }
                            }
                        }
                        Slog.e(AmsExtImpl.TAG, "Received lmkdData error");
                    }
                } catch (Exception e) {
                    Slog.w(AmsExtImpl.TAG, "duraSpeed: memory Exception.");
                    e.printStackTrace();
                    terminate();
                }
                Slog.w(AmsExtImpl.TAG, "duraSpeed: New connection running ending ");
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
}
