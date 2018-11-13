package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.UserHandle;
import com.android.internal.util.ArrayUtils;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.JobStore.JobStatusFunctor;
import java.io.PrintWriter;

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
public class DeviceIdleJobsController extends StateController {
    private static final boolean LOG_DEBUG = false;
    private static final String LOG_TAG = "DeviceIdleJobsController";
    private static DeviceIdleJobsController sController;
    private static Object sCreationLock;
    private final BroadcastReceiver mBroadcastReceiver;
    private boolean mDeviceIdleMode;
    private int[] mDeviceIdleWhitelistAppIds;
    private final JobSchedulerService mJobSchedulerService;
    private final LocalService mLocalDeviceIdleController;
    private final PowerManager mPowerManager;
    final JobStatusFunctor mUpdateFunctor;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.job.controllers.DeviceIdleJobsController.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.job.controllers.DeviceIdleJobsController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.controllers.DeviceIdleJobsController.<clinit>():void");
    }

    public static DeviceIdleJobsController get(JobSchedulerService service) {
        DeviceIdleJobsController deviceIdleJobsController;
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new DeviceIdleJobsController(service, service.getContext(), service.getLock());
            }
            deviceIdleJobsController = sController;
        }
        return deviceIdleJobsController;
    }

    private DeviceIdleJobsController(JobSchedulerService jobSchedulerService, Context context, Object lock) {
        super(jobSchedulerService, context, lock);
        this.mUpdateFunctor = new JobStatusFunctor() {
            public void process(JobStatus jobStatus) {
                DeviceIdleJobsController.this.updateTaskStateLocked(jobStatus);
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED".equals(action) || "android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(action)) {
                    boolean z;
                    DeviceIdleJobsController deviceIdleJobsController = DeviceIdleJobsController.this;
                    if (DeviceIdleJobsController.this.mPowerManager == null) {
                        z = false;
                    } else if (DeviceIdleJobsController.this.mPowerManager.isDeviceIdleMode()) {
                        z = true;
                    } else {
                        z = DeviceIdleJobsController.this.mPowerManager.isLightDeviceIdleMode();
                    }
                    deviceIdleJobsController.updateIdleMode(z);
                } else if ("android.os.action.POWER_SAVE_WHITELIST_CHANGED".equals(action)) {
                    DeviceIdleJobsController.this.updateWhitelist();
                }
            }
        };
        this.mJobSchedulerService = jobSchedulerService;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mLocalDeviceIdleController = (LocalService) LocalServices.getService(LocalService.class);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        filter.addAction("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");
        filter.addAction("android.os.action.POWER_SAVE_WHITELIST_CHANGED");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
    }

    void updateIdleMode(boolean enabled) {
        boolean changed = false;
        if (this.mDeviceIdleWhitelistAppIds == null) {
            updateWhitelist();
        }
        synchronized (this.mLock) {
            if (this.mDeviceIdleMode != enabled) {
                changed = true;
            }
            this.mDeviceIdleMode = enabled;
            this.mJobSchedulerService.getJobStore().forEachJob(this.mUpdateFunctor);
        }
        if (changed) {
            this.mStateChangedListener.onDeviceIdleStateChanged(enabled);
        }
    }

    void updateWhitelist() {
        synchronized (this.mLock) {
            if (this.mLocalDeviceIdleController != null) {
                this.mDeviceIdleWhitelistAppIds = this.mLocalDeviceIdleController.getPowerSaveWhitelistUserAppIds();
            }
        }
    }

    boolean isWhitelistedLocked(JobStatus job) {
        if (this.mDeviceIdleWhitelistAppIds == null || !ArrayUtils.contains(this.mDeviceIdleWhitelistAppIds, UserHandle.getAppId(job.getSourceUid()))) {
            return false;
        }
        return true;
    }

    private void updateTaskStateLocked(JobStatus task) {
        boolean whitelisted = isWhitelistedLocked(task);
        task.setDeviceNotDozingConstraintSatisfied(this.mDeviceIdleMode ? whitelisted : true, whitelisted);
    }

    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        synchronized (this.mLock) {
            updateTaskStateLocked(jobStatus);
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
    }

    public void dumpControllerStateLocked(final PrintWriter pw, final int filterUid) {
        pw.println(LOG_TAG);
        this.mJobSchedulerService.getJobStore().forEachJob(new JobStatusFunctor() {
            public void process(JobStatus jobStatus) {
                if (jobStatus.shouldDump(filterUid)) {
                    pw.print("  #");
                    jobStatus.printUniqueId(pw);
                    pw.print(" from ");
                    UserHandle.formatUid(pw, jobStatus.getSourceUid());
                    pw.print(": ");
                    pw.print(jobStatus.getSourcePackageName());
                    pw.print((jobStatus.satisfiedConstraints & 256) != 0 ? " RUNNABLE" : " WAITING");
                    if (jobStatus.dozeWhitelisted) {
                        pw.print(" WHITELISTED");
                    }
                    pw.println();
                }
            }
        });
    }
}
