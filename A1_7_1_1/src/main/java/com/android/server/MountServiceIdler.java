package com.android.server;

import android.app.ActivityManagerNative;
import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
public class MountServiceIdler extends JobService {
    private static final String DEVICE_INFO_PATH = "/proc/devinfo/emmc";
    private static final String DEVICE_MANUFACTURE = "manufacture";
    private static final String DEVICE_MANUFACTURE_MICRON = "MICRON";
    private static final String DEVICE_VERSION = "version";
    private static final String DEVICE_VERSION_MICRON = "S0J9F8";
    private static int MOUNT_JOB_ID = 0;
    private static final String TAG = "MountServiceIdler";
    private static ComponentName sIdleService;
    private Runnable mFinishCallback;
    private JobParameters mJobParams;
    private boolean mStarted;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.MountServiceIdler.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.MountServiceIdler.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.MountServiceIdler.<clinit>():void");
    }

    public MountServiceIdler() {
        this.mFinishCallback = new Runnable() {
            public void run() {
                Slog.i(MountServiceIdler.TAG, "Got mount service completion callback");
                synchronized (MountServiceIdler.this.mFinishCallback) {
                    if (MountServiceIdler.this.mStarted) {
                        MountServiceIdler.this.jobFinished(MountServiceIdler.this.mJobParams, false);
                        MountServiceIdler.this.mStarted = false;
                    }
                }
                MountServiceIdler.scheduleIdlePass(MountServiceIdler.this);
            }
        };
    }

    public boolean onStartJob(JobParameters params) {
        try {
            ActivityManagerNative.getDefault().performIdleMaintenance();
        } catch (RemoteException e) {
        }
        this.mJobParams = params;
        MountService ms = MountService.sSelf;
        if (ms != null) {
            synchronized (this.mFinishCallback) {
                this.mStarted = true;
            }
            ms.runIdleMaintenance(this.mFinishCallback);
        }
        if (ms != null) {
            return true;
        }
        return false;
    }

    public boolean onStopJob(JobParameters params) {
        synchronized (this.mFinishCallback) {
            this.mStarted = false;
        }
        return false;
    }

    private static Map<String, String> getDeviceInfo() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(DEVICE_INFO_PATH), 256);
            Map<String, String> results = new HashMap();
            while (true) {
                String str = bufferedReader.readLine();
                if (str == null) {
                    bufferedReader.close();
                    Log.d(TAG, "getDeviceInfo,map=" + results.toString());
                    return results;
                } else if (str.contains(DEVICE_MANUFACTURE)) {
                    results.put(DEVICE_MANUFACTURE, str.split("\\s+")[2]);
                } else if (str.contains("version")) {
                    results.put("version", str.split("\\s+")[2]);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private static boolean isMicronMcp() {
        String deviceVersion = null;
        String deviceManufacture = null;
        for (Entry<String, String> entry : getDeviceInfo().entrySet()) {
            String key = (String) entry.getKey();
            if (key.equals(DEVICE_MANUFACTURE)) {
                deviceManufacture = (String) entry.getValue();
            } else if (key.equals("version")) {
                deviceVersion = (String) entry.getValue();
            }
        }
        if (deviceVersion == null || deviceManufacture == null || !deviceVersion.equalsIgnoreCase(DEVICE_VERSION_MICRON) || !deviceManufacture.equalsIgnoreCase(DEVICE_MANUFACTURE_MICRON)) {
            return false;
        }
        return true;
    }

    public static void scheduleIdlePass(Context context) {
        JobScheduler tm = (JobScheduler) context.getSystemService("jobscheduler");
        long timeToMidnight = tomorrowMidnight().getTimeInMillis() - System.currentTimeMillis();
        Builder builder = new Builder(MOUNT_JOB_ID, sIdleService);
        builder.setRequiresDeviceIdle(true);
        if (isMicronMcp()) {
            Slog.i(TAG, "scheduleIdlePass, not require charging");
        } else {
            builder.setRequiresCharging(true);
        }
        builder.setMinimumLatency(timeToMidnight);
        tm.schedule(builder.build());
    }

    private static Calendar tomorrowMidnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(11, 3);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        calendar.add(5, 1);
        return calendar;
    }
}
