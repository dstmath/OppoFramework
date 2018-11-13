package com.android.server.job.controllers;

import android.app.usage.UsageStatsManagerInternal;
import android.content.Context;
import android.os.UserHandle;
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
public class AppIdleController extends StateController {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "AppIdleController";
    private static volatile AppIdleController sController;
    private static Object sCreationLock;
    boolean mAppIdleParoleOn;
    private boolean mInitializedParoleOn;
    private final JobSchedulerService mJobSchedulerService;
    private final UsageStatsManagerInternal mUsageStatsInternal;

    private class AppIdleStateChangeListener extends android.app.usage.UsageStatsManagerInternal.AppIdleStateChangeListener {
        /* synthetic */ AppIdleStateChangeListener(AppIdleController this$0, AppIdleStateChangeListener appIdleStateChangeListener) {
            this();
        }

        private AppIdleStateChangeListener() {
        }

        /* JADX WARNING: Missing block: B:12:0x0026, code:
            if (r0 == false) goto L_0x002f;
     */
        /* JADX WARNING: Missing block: B:13:0x0028, code:
            r4.this$0.mStateChangedListener.onControllerStateChanged();
     */
        /* JADX WARNING: Missing block: B:14:0x002f, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onAppIdleStateChanged(String packageName, int userId, boolean idle) {
            boolean changed = false;
            synchronized (AppIdleController.this.mLock) {
                if (AppIdleController.this.mAppIdleParoleOn) {
                    return;
                }
                PackageUpdateFunc update = new PackageUpdateFunc(userId, packageName, idle);
                AppIdleController.this.mJobSchedulerService.getJobStore().forEachJob(update);
                if (update.mChanged) {
                    changed = true;
                }
            }
        }

        public void onParoleStateChanged(boolean isParoleOn) {
            AppIdleController.this.setAppIdleParoleOn(isParoleOn);
        }
    }

    final class GlobalUpdateFunc implements JobStatusFunctor {
        boolean mChanged;

        GlobalUpdateFunc() {
        }

        public void process(JobStatus jobStatus) {
            boolean appIdle;
            boolean z = false;
            String packageName = jobStatus.getSourcePackageName();
            if (AppIdleController.this.mAppIdleParoleOn) {
                appIdle = false;
            } else {
                appIdle = AppIdleController.this.mUsageStatsInternal.isAppIdle(packageName, jobStatus.getSourceUid(), jobStatus.getSourceUserId());
            }
            if (!appIdle) {
                z = true;
            }
            if (jobStatus.setAppNotIdleConstraintSatisfied(z)) {
                this.mChanged = true;
            }
        }
    }

    static final class PackageUpdateFunc implements JobStatusFunctor {
        boolean mChanged;
        final boolean mIdle;
        final String mPackage;
        final int mUserId;

        PackageUpdateFunc(int userId, String pkg, boolean idle) {
            this.mUserId = userId;
            this.mPackage = pkg;
            this.mIdle = idle;
        }

        public void process(JobStatus jobStatus) {
            if (jobStatus.getSourcePackageName().equals(this.mPackage) && jobStatus.getSourceUserId() == this.mUserId) {
                boolean z;
                if (this.mIdle) {
                    z = false;
                } else {
                    z = true;
                }
                if (jobStatus.setAppNotIdleConstraintSatisfied(z)) {
                    this.mChanged = true;
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.job.controllers.AppIdleController.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.job.controllers.AppIdleController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.controllers.AppIdleController.<clinit>():void");
    }

    public static AppIdleController get(JobSchedulerService service) {
        AppIdleController appIdleController;
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new AppIdleController(service, service.getContext(), service.getLock());
            }
            appIdleController = sController;
        }
        return appIdleController;
    }

    private AppIdleController(JobSchedulerService service, Context context, Object lock) {
        super(service, context, lock);
        this.mJobSchedulerService = service;
        this.mUsageStatsInternal = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        this.mAppIdleParoleOn = true;
        this.mUsageStatsInternal.addAppIdleStateChangeListener(new AppIdleStateChangeListener(this, null));
    }

    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        boolean appIdle;
        boolean z = false;
        if (!this.mInitializedParoleOn) {
            this.mInitializedParoleOn = true;
            this.mAppIdleParoleOn = this.mUsageStatsInternal.isAppIdleParoleOn();
        }
        String packageName = jobStatus.getSourcePackageName();
        if (this.mAppIdleParoleOn) {
            appIdle = false;
        } else {
            appIdle = this.mUsageStatsInternal.isAppIdle(packageName, jobStatus.getSourceUid(), jobStatus.getSourceUserId());
        }
        if (!appIdle) {
            z = true;
        }
        jobStatus.setAppNotIdleConstraintSatisfied(z);
    }

    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
    }

    public void dumpControllerStateLocked(final PrintWriter pw, final int filterUid) {
        pw.print("AppIdle: parole on = ");
        pw.println(this.mAppIdleParoleOn);
        this.mJobSchedulerService.getJobStore().forEachJob(new JobStatusFunctor() {
            public void process(JobStatus jobStatus) {
                if (jobStatus.shouldDump(filterUid)) {
                    pw.print("  #");
                    jobStatus.printUniqueId(pw);
                    pw.print(" from ");
                    UserHandle.formatUid(pw, jobStatus.getSourceUid());
                    pw.print(": ");
                    pw.print(jobStatus.getSourcePackageName());
                    if ((jobStatus.satisfiedConstraints & 64) != 0) {
                        pw.println(" RUNNABLE");
                    } else {
                        pw.println(" WAITING");
                    }
                }
            }
        });
    }

    /* JADX WARNING: Missing block: B:12:0x0020, code:
            if (r0 == false) goto L_0x0027;
     */
    /* JADX WARNING: Missing block: B:13:0x0022, code:
            r4.mStateChangedListener.onControllerStateChanged();
     */
    /* JADX WARNING: Missing block: B:14:0x0027, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void setAppIdleParoleOn(boolean isAppIdleParoleOn) {
        boolean changed = false;
        synchronized (this.mLock) {
            if (this.mAppIdleParoleOn == isAppIdleParoleOn) {
                return;
            }
            this.mAppIdleParoleOn = isAppIdleParoleOn;
            GlobalUpdateFunc update = new GlobalUpdateFunc();
            this.mJobSchedulerService.getJobStore().forEachJob(update);
            if (update.mChanged) {
                changed = true;
            }
        }
    }
}
