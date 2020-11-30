package com.android.server.job;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobWorkItem;
import android.app.job.OppoBaseJobInfo;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.OppoPackageManager;
import android.content.pm.PackageManager;
import android.os.BatteryManagerInternal;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.ColorLocalServices;
import com.android.server.IColorAlarmManagerHelper;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.SystemService;
import com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.job.controllers.JobStatus;
import com.android.server.job.controllers.OppoBaseJobStatus;
import com.color.util.ColorTypeCastingHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public class OppoBaseJobSchedulerService extends SystemService {
    private static final String BATTERY_MANAGER_INTERNAL = "android.os.BatteryManagerInternal";
    private static final boolean BATTIDLE_DCS_ENABLED = false;
    protected static final int MAX_JOBS_PER_APP = 100;
    private static final int MAX_JOBS_SYSTEM = 200;
    private static final String METHOD_GET_BATTERY_TEMPERATURE = "getBatteryTemperature";
    static final int MSG_CHECK_JOB = 1;
    static final int MSG_CHECK_JOB_GREEDY = 3;
    static final int MSG_JOB_EXPIRED = 0;
    static final int MSG_STOP_JOB = 2;
    static final int MSG_UID_ACTIVE = 6;
    static final int MSG_UID_GONE = 5;
    static final int MSG_UID_IDLE = 7;
    static final int MSG_UID_STATE_CHANGED = 4;
    private static final long ONE_DAY = 86400000;
    public static final boolean OPPODEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int PENDING_FAIL = 1;
    private static final int PENDING_PROCESSING = 2;
    static final String REASON_PENDING_JOB = "frozen_pending_internal";
    private static final int RESTORE_IGNORE = 1;
    private static final int RESTORE_SUCCESS = 2;
    public static final String TAG = "OppoBaseJobSchedulerService";
    static final List<String> THERMAL_TYPE = Arrays.asList("tsens_tz_sensor2", "tsens_tz_sensor10", "tsens_tz_sensor9", "tsens_tz_sensor4");
    private static HashMap<String, String> mThermalPath = new HashMap<>();
    BattIdleJobStartRcd mBattIdleJobStartRcd;
    protected BatteryManagerInternal mBatteryManagerInternal;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.job.OppoBaseJobSchedulerService.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            boolean changed;
            if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                synchronized (OppoBaseJobSchedulerService.this.mJobSchedulerService.mLock) {
                    changed = OppoBaseJobSchedulerService.this.mFrozenCacheJobs.size() > 0;
                }
                if (changed) {
                    OppoBaseJobSchedulerService.this.mHandler.post(new Runnable() {
                        /* class com.android.server.job.OppoBaseJobSchedulerService.AnonymousClass1.AnonymousClass1 */

                        public void run() {
                            ArrayList<JobStatus> restores = new ArrayList<>();
                            synchronized (OppoBaseJobSchedulerService.this.mJobSchedulerService.mLock) {
                                restores.addAll(OppoBaseJobSchedulerService.this.mFrozenCacheJobs);
                                OppoBaseJobSchedulerService.this.mFrozenCacheJobs.clear();
                            }
                            Iterator<JobStatus> it = restores.iterator();
                            while (it.hasNext()) {
                                JobStatus js = it.next();
                                Slog.d("BaseJob", " restoreSpecialJobs when screen on");
                                OppoBaseJobSchedulerService.this.mJobSchedulerService.scheduleAsPackage(js.getJob(), null, js.getUid(), null, js.getUserId(), null);
                            }
                        }
                    });
                }
            }
        }
    };
    IColorJobSchedulerServiceEx mColorJssEx = null;
    protected ColorJobSchedulerServiceInner mColorJssInner = null;
    private Context mContext;
    private ArrayList<JobStatus> mFrozenCacheJobs = new ArrayList<>();
    private Handler mHandler = null;
    private boolean mIsSystemMaxJobsDetected = false;
    protected IJobCountPolicy mJobCountLimit = null;
    private JobSchedulerService mJobSchedulerService = null;
    protected ArrayList<JobServiceContext> mListBattIdleJobStart = new ArrayList<>();
    private SparseArray<String> mListSystemJob = new SparseArray<>();
    private long mTimeSystemMaxJobsDetected;

    public OppoBaseJobSchedulerService(Context context) {
        super(context);
        this.mContext = context;
    }

    public void acceptForMaybeReadyJobQueueFunctor(JobStatus job) {
    }

    public boolean readyForPostProcess() {
        return false;
    }

    public void resetForMaybeReadyJobQueueFunctor() {
    }

    @Override // com.android.server.SystemService
    public void onStart() {
    }

    /* access modifiers changed from: protected */
    public IJobCountPolicy getJobCountPolicy() {
        if (this.mJobCountLimit == null) {
            this.mJobCountLimit = (IJobCountPolicy) ColorLocalServices.getService(IJobCountPolicy.class);
        }
        return this.mJobCountLimit;
    }

    /* access modifiers changed from: protected */
    public void onOppoStart() {
        IColorJobSchedulerServiceEx iColorJobSchedulerServiceEx = this.mColorJssEx;
        if (iColorJobSchedulerServiceEx != null) {
            iColorJobSchedulerServiceEx.onStart();
        }
    }

    /* access modifiers changed from: protected */
    public void onOppoSystemReady() {
        IColorJobSchedulerServiceEx iColorJobSchedulerServiceEx = this.mColorJssEx;
        if (iColorJobSchedulerServiceEx != null) {
            iColorJobSchedulerServiceEx.systemReady();
        }
    }

    /* access modifiers changed from: protected */
    public void handleOppoMessage(Message msg, int whichHandler) {
        IColorJobSchedulerServiceEx iColorJobSchedulerServiceEx = this.mColorJssEx;
        if (iColorJobSchedulerServiceEx != null) {
            iColorJobSchedulerServiceEx.handleMessage(msg, whichHandler);
        }
    }

    public final class ColorJobSchedulerServiceInner implements IColorJobSchedulerServiceInner {
        public ColorJobSchedulerServiceInner() {
        }

        @Override // com.android.server.job.IColorJobSchedulerServiceInner
        public int getMsgJobExpiredValue() {
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void minIntervalConstraint(JobInfo job, int uId) {
        String jobPkg;
        OppoBaseJobInfo baseJobInfo;
        if (uId >= 10000) {
            PackageManager packageManager = getContext().getPackageManager();
            if (!new OppoPackageManager().isClosedSuperFirewall() && job.getService() != null && (jobPkg = job.getService().getPackageName()) != null) {
                ApplicationInfo appInfo = null;
                try {
                    appInfo = packageManager.getApplicationInfo(jobPkg, 8192);
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.d(TAG, "getApplicationInfo NameNotFoundException. pkg = " + jobPkg);
                }
                if (appInfo != null && (appInfo.flags & 1) == 0) {
                    long minInterval = OppoGuardElfConfigUtil.getInstance().getThreshJobMinInterval() * 1000;
                    if (!job.isPeriodic() && job.hasLateConstraint() && job.getMaxExecutionDelayMillis() < minInterval && (baseJobInfo = typeCastingJobInfo(job)) != null) {
                        baseJobInfo.setMaxExecutionDelayMillis(minInterval);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void rcdBattIdleStart(Object mLock, List<JobServiceContext> list) {
    }

    private String getJobPkgName(JobStatus js) {
        JobInfo jobInfo = js.getJob();
        if (jobInfo == null || jobInfo.getService() == null || jobInfo.getService().getPackageName() == null) {
            return js.getBatteryName();
        }
        return jobInfo.getService().getPackageName();
    }

    /* access modifiers changed from: protected */
    public void uploadScheduleBattIdleJob(JobStatus job) {
        OppoBaseJobInfo oppoBaseJobInfo = typeCastingJobInfo(job.getJob());
        if (oppoBaseJobInfo != null && oppoBaseJobInfo.isRequireBattIdle()) {
            String pkgName = getJobPkgName(job);
            if (OPPODEBUG) {
                Slog.d(TAG, "ScheduleBattIdleJob: pkgName=" + pkgName + ", job: " + job);
            }
        }
    }

    private void getCpuThermalPath() {
    }

    public static HashMap<String, Integer> getCpuThermal() {
        synchronized (mThermalPath) {
            if (mThermalPath != null) {
                if (!mThermalPath.isEmpty()) {
                    HashMap<String, Integer> cpuThermal = new HashMap<>();
                    for (Map.Entry<String, String> ent : mThermalPath.entrySet()) {
                        String type = ent.getKey();
                        int thermal = readIntFromFile(ent.getValue());
                        if (type.equals(THERMAL_TYPE.get(0))) {
                            type = "PoPMem";
                        } else if (type.equals(THERMAL_TYPE.get(1))) {
                            type = "Gpu";
                        } else if (type.equals(THERMAL_TYPE.get(2))) {
                            type = "Cpu0123";
                        } else if (type.equals(THERMAL_TYPE.get(3))) {
                            type = "Cpu4";
                        }
                        cpuThermal.put(type, Integer.valueOf(thermal));
                    }
                    return cpuThermal;
                }
            }
            return null;
        }
    }

    private static String readStrFromFile(String path) {
        String tempString;
        StringBuilder sb;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            tempString = reader.readLine();
            try {
                reader.close();
            } catch (IOException e) {
                e1 = e;
                sb = new StringBuilder();
            }
        } catch (IOException e2) {
            tempString = null;
            Slog.e(TAG, "readStrFromFile io exception:" + e2.getMessage());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    e1 = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    Slog.e(TAG, "readStrFromFile io close exception :" + e1.getMessage());
                }
            }
            throw th;
        }
        return tempString;
        sb.append("readStrFromFile io close exception :");
        sb.append(e1.getMessage());
        Slog.e(TAG, sb.toString());
        return tempString;
    }

    private static int readIntFromFile(String path) {
        String str = readStrFromFile(path);
        if (str == null || "".equals(str)) {
            return 0;
        }
        try {
            return Integer.valueOf(str).intValue();
        } catch (NumberFormatException e) {
            Slog.e(TAG, "readIntFromFile NumberFormatException:" + e.getMessage());
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void systemMaxJobsMonitor(int uId, JobStore mJobs) {
        if (mJobs.countJobsForUid(uId) > 200) {
            Slog.w(TAG, "Too many jobs for uid " + uId);
            throw new IllegalStateException("Apps may not schedule more than 200 distinct jobs");
        } else if (!this.mIsSystemMaxJobsDetected || SystemClock.elapsedRealtime() - this.mTimeSystemMaxJobsDetected >= 86400000) {
            Slog.w(TAG, "Too many jobs for uid " + uId);
            this.mIsSystemMaxJobsDetected = true;
            this.mTimeSystemMaxJobsDetected = SystemClock.elapsedRealtime();
            List<JobStatus> listJobs = mJobs.getJobsByUid(uId);
            ArrayMap<String, Integer> jobStatistics = new ArrayMap<>();
            for (int i = 0; i < listJobs.size(); i++) {
                JobInfo jobInfo = listJobs.get(i).getJob();
                if (jobInfo != null) {
                    if (OPPODEBUG) {
                        Slog.d(TAG, "systemMaxJobs: Index(" + i + "), jobId=" + jobInfo.getId() + ", compName=" + jobInfo.getService().flattenToShortString());
                    }
                    String pkgName = jobInfo.getService().getPackageName();
                    if (pkgName != null) {
                        Integer count = jobStatistics.get(pkgName);
                        if (count == null) {
                            jobStatistics.put(pkgName, 1);
                        } else {
                            jobStatistics.put(pkgName, Integer.valueOf(count.intValue() + 1));
                        }
                    }
                }
            }
            Map<String, String> eventMap = new HashMap<>();
            for (int i2 = 0; i2 < jobStatistics.size(); i2++) {
                String pkgName2 = jobStatistics.keyAt(i2);
                Integer count2 = jobStatistics.valueAt(i2);
                Slog.d(TAG, "systemMaxJobs: pkgName(" + pkgName2 + "), jobCount=" + count2);
                StringBuilder sb = new StringBuilder();
                sb.append("job");
                sb.append(i2);
                sb.append("pkgName");
                eventMap.put(sb.toString(), pkgName2);
                eventMap.put("job" + i2 + "count", String.valueOf(count2));
            }
            OppoStatistics.onCommon(getContext(), DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "systemMaxJobs", eventMap, false);
            if (OPPODEBUG) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new FastPrintWriter(sw, false, 1024);
                sw.write("system uid schedule more than ");
                sw.write(String.valueOf(100));
                sw.write(" jobs\r\n");
                new RemoteException().printStackTrace(pw);
                pw.flush();
                sw.write(StringUtils.LF);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void systemJobSameIdMonitor(JobInfo job, int uId) {
        if (uId == 1000) {
            String jobCompName = job.getService().flattenToShortString();
            if (jobCompName == null) {
                Slog.d(TAG, "systemJobSameId: jobCompName is null");
                return;
            }
            synchronized (this.mListSystemJob) {
                String compName = this.mListSystemJob.get(job.getId());
                if (compName == null) {
                    compName = jobCompName;
                    this.mListSystemJob.put(job.getId(), jobCompName);
                }
                if (!compName.equals(jobCompName)) {
                    if (OPPODEBUG) {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new FastPrintWriter(sw, false, 1024);
                        sw.write("system uid job use same jobid(");
                        sw.write(String.valueOf(job.getId()));
                        sw.write(")\r\n");
                        new RemoteException().printStackTrace(pw);
                        pw.flush();
                        sw.write("\r\n\r\n\r\n");
                        sw.write("compNameOld: ");
                        sw.write(compName);
                        sw.write("\r\n");
                        sw.write("compNameNew: ");
                        sw.write(jobCompName);
                        sw.write("\r\n");
                    }
                    Map<String, String> eventMap = new HashMap<>();
                    eventMap.put("jobId", String.valueOf(job.getId()));
                    eventMap.put("compNameOld", compName);
                    eventMap.put("compNameNew", jobCompName);
                    OppoStatistics.onCommon(getContext(), DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "systemJobSameId", eventMap, false);
                    Slog.d(TAG, "systemJobSameId: jobId = " + job.getId() + ", compNameOld=" + compName + ", compNameNew=" + jobCompName);
                }
            }
        }
    }

    private void uploadBattIdleJob(JobServiceContext jc) {
        if (this.mBattIdleJobStartRcd != null) {
            Clock sNowElapsedClock = SystemClock.elapsedRealtimeClock();
            long nowElapsed = sNowElapsedClock.millis();
            long executeTime = (nowElapsed - jc.getExecutionStartTimeElapsed()) / 1000;
            HashMap<String, String> eventMap = new HashMap<>();
            ArrayList<String> listBattIdleJob = this.mBattIdleJobStartRcd.mListBattIdleJob;
            for (int i = 0; i < listBattIdleJob.size(); i++) {
                eventMap.put("battIdleRun_" + i, listBattIdleJob.get(i));
            }
            ArrayList<String> listOtherJob = this.mBattIdleJobStartRcd.mListOtherJob;
            for (int i2 = 0; i2 < listOtherJob.size(); i2++) {
                eventMap.put("OtherJobRun_" + i2, listOtherJob.get(i2));
            }
            eventMap.put("numBattIdleJob", String.valueOf(listBattIdleJob.size()));
            eventMap.put("numOtherJob", String.valueOf(listOtherJob.size()));
            eventMap.put("executeTime", String.valueOf(executeTime));
            eventMap.put("battIdleJob", this.mBattIdleJobStartRcd.mBattIdleJob);
            eventMap.put("battIdleSatisfied", String.valueOf(this.mBattIdleJobStartRcd.mIsBattIdleSatisfied));
            eventMap.put("temperatureStart", String.valueOf(this.mBattIdleJobStartRcd.mBattTemperature / 10));
            int battTemperatureNow = -3000;
            BatteryManagerInternal batteryManagerInternal = this.mBatteryManagerInternal;
            if (batteryManagerInternal != null) {
                battTemperatureNow = getBatteryTemperatureReflect(batteryManagerInternal);
            }
            eventMap.put("temperatureEnd", String.valueOf(battTemperatureNow / 10));
            HashMap<String, Integer> cpuThermalStart = this.mBattIdleJobStartRcd.mCpuThermal;
            if (cpuThermalStart != null && !cpuThermalStart.isEmpty()) {
                int thermalTotal = 0;
                for (Map.Entry<String, Integer> ent : cpuThermalStart.entrySet()) {
                    int thermal = ent.getValue().intValue();
                    thermalTotal += thermal;
                    eventMap.put("cpuTempStart" + ent.getKey(), String.valueOf(thermal / 10));
                    sNowElapsedClock = sNowElapsedClock;
                    nowElapsed = nowElapsed;
                }
                if (cpuThermalStart.size() > 0) {
                    eventMap.put("cpuTempAverStart", String.valueOf((thermalTotal / cpuThermalStart.size()) / 10));
                }
            }
            HashMap<String, Integer> cpuThermal = getCpuThermal();
            if (cpuThermal != null && !cpuThermal.isEmpty()) {
                int thermalTotal2 = 0;
                for (Map.Entry<String, Integer> ent2 : cpuThermal.entrySet()) {
                    int thermal2 = ent2.getValue().intValue();
                    thermalTotal2 += thermal2;
                    eventMap.put("cpuTempEnd" + ent2.getKey(), String.valueOf(thermal2 / 10));
                }
                if (cpuThermal.size() > 0) {
                    eventMap.put("cpuTempAverEnd", String.valueOf((thermalTotal2 / cpuThermal.size()) / 10));
                }
            }
            Slog.d(TAG, "BattIdleJob:, TemperatureStart=" + this.mBattIdleJobStartRcd.mBattTemperature + ", TemperatureNow=" + battTemperatureNow + ", executeTime=" + executeTime);
            OppoStatistics.onCommon(this.mContext, DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "battIdle_job_rcd", eventMap, false);
            this.mBattIdleJobStartRcd = null;
        }
    }

    private void setBattIdleJobStartRcd(ArrayList<String> listBattIdleJob, ArrayList<String> listOtherJob, HashMap<String, Integer> cpuThermal, String battIdleJob, int battTemperature, boolean isBattIdleSatisfied) {
        this.mBattIdleJobStartRcd = new BattIdleJobStartRcd(listBattIdleJob, listOtherJob, cpuThermal, battIdleJob, battTemperature, isBattIdleSatisfied);
    }

    class BattIdleJobStartRcd {
        String mBattIdleJob;
        int mBattTemperature;
        HashMap<String, Integer> mCpuThermal;
        boolean mIsBattIdleSatisfied;
        ArrayList<String> mListBattIdleJob;
        ArrayList<String> mListOtherJob;

        BattIdleJobStartRcd(ArrayList<String> listBattIdleJob, ArrayList<String> listOtherJob, HashMap<String, Integer> cpuThermal, String battIdleJob, int battTemperature, boolean isBattIdleSatisfied) {
            this.mListBattIdleJob = listBattIdleJob;
            this.mListOtherJob = listOtherJob;
            this.mCpuThermal = cpuThermal;
            this.mBattIdleJob = battIdleJob;
            this.mBattTemperature = battTemperature;
            this.mIsBattIdleSatisfied = isBattIdleSatisfied;
        }
    }

    public boolean stopTrackingJobExported(JobStatus jobStatus, JobStatus incomingJob, boolean writeBack) {
        return false;
    }

    private static OppoBaseJobInfo typeCastingJobInfo(JobInfo jobInfo) {
        return (OppoBaseJobInfo) ColorTypeCastingHelper.typeCasting(OppoBaseJobInfo.class, jobInfo);
    }

    private static OppoBaseJobStatus typeCastingJobStatus(JobStatus jobStatus) {
        return (OppoBaseJobStatus) ColorTypeCastingHelper.typeCasting(OppoBaseJobStatus.class, jobStatus);
    }

    private int getBatteryTemperatureReflect(BatteryManagerInternal batteryManagerInternal) {
        try {
            return ((Integer) Class.forName(BATTERY_MANAGER_INTERNAL).getDeclaredMethod(METHOD_GET_BATTERY_TEMPERATURE, new Class[0]).invoke(batteryManagerInternal, new Object[0])).intValue();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return -3000;
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
            return -3000;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return -3000;
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
            return -3000;
        }
    }

    /* access modifiers changed from: protected */
    public void initArgs(Context context, Handler handler, JobSchedulerService service) {
        this.mJobSchedulerService = service;
        this.mHandler = handler;
        context.registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.SCREEN_ON"));
    }

    public int pendingJobs(int uid) {
        if (this.mJobSchedulerService == null) {
            return 1;
        }
        return pendingJobs(getRunningJobsForBlackPackage(uid), REASON_PENDING_JOB);
    }

    public int restoreSpecialJobs(int uid) {
        if (this.mJobSchedulerService == null) {
            return 2;
        }
        ArrayList<JobStatus> toRemove = new ArrayList<>();
        synchronized (this.mJobSchedulerService.mLock) {
            for (int i = 0; i < this.mFrozenCacheJobs.size(); i++) {
                JobStatus js = this.mFrozenCacheJobs.get(i);
                if (js.getUid() == uid) {
                    toRemove.add(js);
                }
            }
            this.mFrozenCacheJobs.removeAll(toRemove);
        }
        for (int i2 = 0; i2 < toRemove.size(); i2++) {
            JobStatus js2 = toRemove.get(i2);
            this.mJobSchedulerService.scheduleAsPackage(js2.getJob(), null, js2.getUid(), null, js2.getUserId(), null);
        }
        return 2;
    }

    /* access modifiers changed from: protected */
    public void interceptCancelJob(int uid, int jobId) {
        if (this.mJobSchedulerService != null) {
            ArrayList<JobStatus> toremove = new ArrayList<>();
            for (int i = 0; i < this.mFrozenCacheJobs.size(); i++) {
                JobStatus js = this.mFrozenCacheJobs.get(i);
                if (js.matches(uid, jobId)) {
                    Slog.d("BaseJob", " interceptCancelJob remove " + js);
                    toremove.add(js);
                }
            }
            this.mFrozenCacheJobs.removeAll(toremove);
        }
    }

    /* access modifiers changed from: protected */
    public boolean interceptScheduleJobLocked(JobStatus scheduleJob, JobWorkItem work) {
        if (this.mJobSchedulerService == null) {
            return false;
        }
        if (scheduleJob == null) {
            Slog.d("BaseJob", " interceptScheduleJobLocked but jobstatus is null");
            return false;
        }
        boolean result = false;
        ArrayList<JobStatus> toRemove = new ArrayList<>();
        for (int i = 0; i < this.mFrozenCacheJobs.size(); i++) {
            JobStatus js = this.mFrozenCacheJobs.get(i);
            if (js.matches(scheduleJob.getUid(), scheduleJob.getJobId())) {
                boolean black = isBlackList(scheduleJob);
                Slog.d("BaseJob", " interceptScheduleJobLocked js is black " + black);
                if (black) {
                    result = true;
                    if (work != null) {
                        js.enqueueWorkLocked(ActivityManager.getService(), work);
                    } else {
                        this.mFrozenCacheJobs.set(i, scheduleJob);
                    }
                } else {
                    toRemove.add(js);
                }
                Slog.d("BaseJob", " interceptScheduleJobLocked js = " + js);
            }
        }
        this.mFrozenCacheJobs.removeAll(toRemove);
        return result;
    }

    /* access modifiers changed from: protected */
    public void dumpCacheJobs(IndentingPrintWriter pw) {
        pw.println("cache jobs");
        synchronized (this.mJobSchedulerService.mLock) {
            Iterator<JobStatus> it = this.mFrozenCacheJobs.iterator();
            while (it.hasNext()) {
                pw.println(it.next());
            }
        }
    }

    private int pendingJobs(ArrayList<JobStatus> jobs, String reason) {
        if (jobs.size() == 0) {
            return 1;
        }
        for (int i = 0; i < jobs.size(); i++) {
            JobStatus toRemove = jobs.get(i);
            if (!isBlackList(toRemove)) {
                Slog.d("BaseJob", " pendingJobs fail js = " + toRemove);
                return 1;
            }
        }
        synchronized (this.mJobSchedulerService.mLock) {
            for (int i2 = 0; i2 < jobs.size(); i2++) {
                JobStatus toRemove2 = jobs.get(i2);
                Slog.d("BaseJob", " colorCancelJobImplLocked js = " + toRemove2);
                colorCancelJobImplLocked(toRemove2, null, reason);
            }
            this.mFrozenCacheJobs.addAll(jobs);
        }
        return 2;
    }

    private ArrayList<JobStatus> getRunningJobsForBlackPackage(int uid) {
        ArrayList<JobStatus> result = new ArrayList<>();
        synchronized (this.mJobSchedulerService.mLock) {
            for (int i = 0; i < this.mJobSchedulerService.mActiveServices.size(); i++) {
                JobServiceContext jsc = this.mJobSchedulerService.mActiveServices.get(i);
                if (jsc.getRunningJobLocked() != null) {
                    JobStatus js = jsc.getRunningJobLocked();
                    if (js.getUid() == uid) {
                        result.add(js);
                    }
                }
            }
        }
        return result;
    }

    private boolean isBlackList(JobStatus js) {
        return OppoFeatureCache.get(IColorAlarmManagerHelper.DEFAULT).isBlackJobList(js.getJob().getService().getPackageName(), js.getJob().getService().flattenToShortString());
    }

    /* access modifiers changed from: package-private */
    public void colorCancelJobImplLocked(JobStatus cancelled, JobStatus incomingJob, String reason) {
    }
}
