package com.android.server.pm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.util.ArraySet;
import android.util.Log;
import java.io.File;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class BackgroundDexOptService extends JobService {
    static final int DEFAULT_MORNING_CLEAR_RANDOM_TIME_END = 5;
    static final int DEFAULT_MORNING_CLEAR_RANDOM_TIME_START = 2;
    static final int JOB_IDLE_OPTIMIZE = 800;
    static final int JOB_MORNING_OPTIMIZE = 802;
    static final int JOB_POST_BOOT_UPDATE = 801;
    static final String MORNING_DEXOPT_START_ACTION = "oppo.intent.action.ACTION_MORNING_DEXOPT";
    static final long RETRY_LATENCY = 14400000;
    static final int SECONDS_ONE_HOUR = 3600;
    static final int SECONDS_ONE_MIN = 60;
    static final String TAG = "BackgroundDexOptService";
    static Receiver mReceiver;
    private static ComponentName sDexoptServiceName;
    static final ArraySet<String> sFailedPackageNames = null;
    private final File dataDir;
    final AtomicBoolean mAbortIdleOptimization;
    final AtomicBoolean mAbortPostBootUpdate;
    final AtomicBoolean mExitPostBootUpdate;

    private static class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            JobScheduler js = (JobScheduler) context.getSystemService("jobscheduler");
            if (BackgroundDexOptService.MORNING_DEXOPT_START_ACTION.equals(intent.getAction())) {
                Log.d(BackgroundDexOptService.TAG, "schedule now");
                js.schedule(new Builder(BackgroundDexOptService.JOB_MORNING_OPTIMIZE, BackgroundDexOptService.sDexoptServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setOverrideDeadline(TimeUnit.MINUTES.toMillis(30)).build());
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.pm.BackgroundDexOptService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.pm.BackgroundDexOptService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.BackgroundDexOptService.<clinit>():void");
    }

    public BackgroundDexOptService() {
        this.mAbortPostBootUpdate = new AtomicBoolean(false);
        this.mAbortIdleOptimization = new AtomicBoolean(false);
        this.mExitPostBootUpdate = new AtomicBoolean(false);
        this.dataDir = Environment.getDataDirectory();
    }

    public static void schedule(final Context context) {
        JobScheduler js = (JobScheduler) context.getSystemService("jobscheduler");
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(60000);
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(BackgroundDexOptService.MORNING_DEXOPT_START_ACTION);
                    context.registerReceiver(BackgroundDexOptService.mReceiver, filter);
                    BackgroundDexOptService.scheduleMorningDexOptAlarm(context);
                } catch (Exception e) {
                    Log.w(BackgroundDexOptService.TAG, "exception = " + e);
                }
            }
        }).start();
        js.schedule(new Builder(JOB_POST_BOOT_UPDATE, sDexoptServiceName).setMinimumLatency(TimeUnit.MINUTES.toMillis(1)).setOverrideDeadline(TimeUnit.MINUTES.toMillis(1)).build());
        if (Build.IS_DEBUGGABLE && SystemProperties.getInt("debug.backgrounddexoptservice", 0) == 1) {
            Log.i(TAG, "Jobs scheduled JOB_IDLE_OPTIMIZE debug mode");
            js.schedule(new Builder(JOB_IDLE_OPTIMIZE, sDexoptServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setPeriodic(TimeUnit.MINUTES.toMillis(3)).build());
        } else {
            js.schedule(new Builder(JOB_IDLE_OPTIMIZE, sDexoptServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setPeriodic(TimeUnit.DAYS.toMillis(1)).build());
        }
        if (PackageManagerService.DEBUG_DEXOPT) {
            Log.i(TAG, "Jobs scheduled");
        }
    }

    public static void notifyPackageChanged(String packageName) {
        synchronized (sFailedPackageNames) {
            sFailedPackageNames.remove(packageName);
        }
    }

    private int getBatteryLevel() {
        Intent intent = registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        int level = intent.getIntExtra("level", -1);
        int scale = intent.getIntExtra("scale", -1);
        if (level < 0 || scale <= 0) {
            return 0;
        }
        return (level * 100) / scale;
    }

    private long getLowStorageThreshold() {
        long lowThreshold = StorageManager.from(this).getStorageLowBytes(this.dataDir);
        if (lowThreshold == 0) {
            Log.e(TAG, "Invalid low storage threshold");
        }
        return lowThreshold;
    }

    private boolean runPostBootUpdate(JobParameters jobParams, PackageManagerService pm, ArraySet<String> pkgs) {
        if (this.mExitPostBootUpdate.get()) {
            return false;
        }
        final int lowBatteryThreshold = getResources().getInteger(17694806);
        final long lowThreshold = getLowStorageThreshold();
        this.mAbortPostBootUpdate.set(false);
        final ArraySet<String> arraySet = pkgs;
        final PackageManagerService packageManagerService = pm;
        final JobParameters jobParameters = jobParams;
        new Thread("BackgroundDexOptService_PostBootUpdate") {
            public void run() {
                for (String pkg : arraySet) {
                    if (!BackgroundDexOptService.this.mAbortPostBootUpdate.get()) {
                        if (BackgroundDexOptService.this.mExitPostBootUpdate.get() || BackgroundDexOptService.this.getBatteryLevel() < lowBatteryThreshold) {
                            break;
                        }
                        long usableSpace = BackgroundDexOptService.this.dataDir.getUsableSpace();
                        if (usableSpace < lowThreshold) {
                            Log.w(BackgroundDexOptService.TAG, "Aborting background dex opt job due to low storage: " + usableSpace);
                            break;
                        }
                        if (PackageManagerService.DEBUG_DEXOPT) {
                            Log.i(BackgroundDexOptService.TAG, "Updating package " + pkg);
                        }
                        packageManagerService.performDexOpt(pkg, false, 1, false);
                    } else {
                        return;
                    }
                }
                BackgroundDexOptService.this.jobFinished(jobParameters, false);
            }
        }.start();
        return true;
    }

    private boolean runIdleOptimization(JobParameters jobParams, PackageManagerService pm, ArraySet<String> pkgs) {
        this.mExitPostBootUpdate.set(true);
        this.mAbortIdleOptimization.set(false);
        final long lowThreshold = getLowStorageThreshold();
        final ArraySet<String> arraySet = pkgs;
        final PackageManagerService packageManagerService = pm;
        final JobParameters jobParameters = jobParams;
        new Thread("BackgroundDexOptService_IdleOptimization") {
            public void run() {
                Log.i(BackgroundDexOptService.TAG, "BackgroundDexOptService_IdleOptimization begin:");
                for (String pkg : arraySet) {
                    if (BackgroundDexOptService.this.mAbortIdleOptimization.get()) {
                        Log.i(BackgroundDexOptService.TAG, "BackgroundDexOptService_IdleOptimization abort for quit idle.");
                        return;
                    } else if (!BackgroundDexOptService.sFailedPackageNames.contains(pkg)) {
                        long usableSpace = BackgroundDexOptService.this.dataDir.getUsableSpace();
                        if (usableSpace < lowThreshold) {
                            Log.w(BackgroundDexOptService.TAG, "Aborting background dex opt job due to low storage: " + usableSpace);
                            break;
                        }
                        int i;
                        synchronized (BackgroundDexOptService.sFailedPackageNames) {
                            BackgroundDexOptService.sFailedPackageNames.add(pkg);
                        }
                        boolean topApp = packageManagerService.inCptWhiteList(CompatibilityHelper.FORCE_DEXOPT_IN_SPEED, pkg);
                        PackageManagerService packageManagerService = packageManagerService;
                        if (topApp) {
                            i = 8;
                        } else {
                            i = 3;
                        }
                        if (packageManagerService.performDexOpt(pkg, true, i, false)) {
                            synchronized (BackgroundDexOptService.sFailedPackageNames) {
                                BackgroundDexOptService.sFailedPackageNames.remove(pkg);
                            }
                        } else {
                            continue;
                        }
                    }
                }
                Log.i(BackgroundDexOptService.TAG, "BackgroundDexOptService_IdleOptimization end as normal.");
                BackgroundDexOptService.this.jobFinished(jobParameters, false);
            }
        }.start();
        return true;
    }

    public boolean onStartJob(JobParameters params) {
        if (PackageManagerService.DEBUG_DEXOPT) {
            Log.i(TAG, "onStartJob");
        }
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService("package");
        if (pm.isStorageLow()) {
            if (PackageManagerService.DEBUG_DEXOPT) {
                Log.i(TAG, "Low storage, skipping this run");
            }
            return false;
        }
        ArraySet<String> pkgs = pm.getOptimizablePackages();
        if (pkgs == null || pkgs.isEmpty()) {
            if (PackageManagerService.DEBUG_DEXOPT) {
                Log.i(TAG, "No packages to optimize");
            }
            return false;
        } else if (params.getJobId() == JOB_POST_BOOT_UPDATE) {
            return runPostBootUpdate(params, pm, pkgs);
        } else {
            return runIdleOptimization(params, pm, pkgs);
        }
    }

    public boolean onStopJob(JobParameters params) {
        if (PackageManagerService.DEBUG_DEXOPT) {
            Log.i(TAG, "onStopJob");
        }
        if (params.getJobId() == JOB_POST_BOOT_UPDATE) {
            this.mAbortPostBootUpdate.set(true);
        } else {
            this.mAbortIdleOptimization.set(true);
        }
        return false;
    }

    public static void scheduleMorningDexOptAlarm(Context context) {
        AlarmManager mAlarm = (AlarmManager) context.getSystemService("alarm");
        PendingIntent morningDexOptIntent = PendingIntent.getBroadcast(context, 0, new Intent(MORNING_DEXOPT_START_ACTION), 0);
        mAlarm.cancel(morningDexOptIntent);
        int ramdomHour = getMorningDexOptRandomHour(context);
        int ramdomMinute = getMorningDexOptRandomMinute(context);
        long ramdomTimeInMillis = (long) (((ramdomHour * 60) + ramdomMinute) * 60);
        Log.d(TAG, "ramdomHour = " + ramdomHour + " ramdomMinute = " + ramdomMinute);
        String ramTime = Integer.valueOf(ramdomHour).toString() + " : " + Integer.valueOf(ramdomMinute).toString();
        if (ramTime != null) {
            SystemProperties.set("debug.oppo.morningdexopt.time", ramTime);
        }
        mAlarm.setExact(0, getTriggerTime(System.currentTimeMillis(), ramdomTimeInMillis), morningDexOptIntent);
    }

    public static long getTriggerTime(long curTime, long secondsSinceMidnight) {
        return computeCalendarTime(Calendar.getInstance(), curTime, secondsSinceMidnight);
    }

    public static long computeCalendarTime(Calendar c, long curTime, long secondsSinceMidnight) {
        c.setTimeInMillis(curTime);
        int val = ((int) secondsSinceMidnight) / SECONDS_ONE_HOUR;
        c.set(11, val);
        secondsSinceMidnight -= (long) (val * SECONDS_ONE_HOUR);
        val = ((int) secondsSinceMidnight) / 60;
        c.set(12, val);
        c.set(13, ((int) secondsSinceMidnight) - (val * 60));
        c.set(14, 0);
        c.add(5, 1);
        return c.getTimeInMillis();
    }

    public static int getMorningDexOptRandomHour(Context context) {
        int random = new Random().nextInt(3) + 2;
        Log.d(TAG, "getMorningDexOptRandomHour=" + random);
        return random;
    }

    public static int getMorningDexOptRandomMinute(Context context) {
        return new Random().nextInt(60);
    }
}
