package com.android.server.job;

import android.app.job.JobInfo;
import android.content.Context;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.util.FastPrintWriter;
import com.android.server.job.controllers.JobStatus;
import com.android.server.policy.OppoPhoneWindowManager;
import com.color.util.ColorTypeCastingHelper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public class ColorJobCountPolicy extends ColorDummyJobCountPolicy {
    private static final int MAX_JOBS_SYSTEM = 200;
    private static final long ONE_DAY = 86400000;
    private boolean mIsSystemMaxJobsDetected = false;
    private SparseArray<String> mListSystemJob = new SparseArray<>();
    private long mTimeSystemMaxJobsDetected;

    public void handleAbnormalJobCount(Context context, JobStore jobs, int uid, int maxCount) {
        if (uid == 1000) {
            systemMaxJobsMonitor(context, jobs, uid, maxCount);
            return;
        }
        Slog.w("JobScheduler", "Too many jobs for uid " + uid);
        throw new IllegalStateException("Apps may not schedule more than " + maxCount + " distinct jobs");
    }

    public void startSystemJobSameIdMonitor(Context context, JobInfo job, int uid) {
        systemJobSameIdMonitor(context, job, uid);
    }

    private void systemMaxJobsMonitor(Context context, JobStore jobs, int uId, int maxJobCount) {
        if (jobs.countJobsForUid(uId) > MAX_JOBS_SYSTEM) {
            Slog.w("JobScheduler", "Too many jobs for uid " + uId);
            throw new IllegalStateException("Apps may not schedule more than 200 distinct jobs");
        } else if (!this.mIsSystemMaxJobsDetected || SystemClock.elapsedRealtime() - this.mTimeSystemMaxJobsDetected >= ONE_DAY) {
            Slog.w("JobScheduler", "Too many jobs for uid " + uId);
            this.mIsSystemMaxJobsDetected = true;
            this.mTimeSystemMaxJobsDetected = SystemClock.elapsedRealtime();
            List<JobStatus> listJobs = jobs.getJobsByUid(uId);
            ArrayMap<String, Integer> jobStatistics = new ArrayMap<>();
            for (int i = 0; i < listJobs.size(); i++) {
                JobInfo jobInfo = listJobs.get(i).getJob();
                if (jobInfo != null) {
                    if (OppoBaseJobSchedulerService.OPPODEBUG) {
                        Slog.d("JobScheduler", "systemMaxJobs: Index(" + i + "), jobId=" + jobInfo.getId() + ", compName=" + jobInfo.getService().flattenToShortString());
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
                Slog.d("JobScheduler", "systemMaxJobs: pkgName(" + pkgName2 + "), jobCount=" + count2);
                StringBuilder sb = new StringBuilder();
                sb.append("job");
                sb.append(i2);
                sb.append("pkgName");
                eventMap.put(sb.toString(), pkgName2);
                eventMap.put("job" + i2 + "count", String.valueOf(count2));
            }
            OppoStatistics.onCommon(context, "20120", "systemMaxJobs", eventMap, false);
            if (OppoBaseJobSchedulerService.OPPODEBUG) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new FastPrintWriter(sw, false, (int) OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE);
                sw.write("system uid schedule more than ");
                sw.write(String.valueOf(maxJobCount));
                sw.write(" jobs\r\n");
                new RemoteException().printStackTrace(pw);
                pw.flush();
                sw.write("\n");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void systemJobSameIdMonitor(Context context, JobInfo job, int uId) {
        if (uId == 1000) {
            String jobCompName = job.getService().flattenToShortString();
            if (jobCompName == null) {
                Slog.d("JobScheduler", "systemJobSameId: jobCompName is null");
                return;
            }
            synchronized (this.mListSystemJob) {
                String compName = this.mListSystemJob.get(job.getId());
                if (compName == null) {
                    compName = jobCompName;
                    this.mListSystemJob.put(job.getId(), jobCompName);
                }
                if (!compName.equals(jobCompName)) {
                    if (OppoBaseJobSchedulerService.OPPODEBUG) {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new FastPrintWriter(sw, false, (int) OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE);
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
                    OppoStatistics.onCommon(context, "20120", "systemJobSameId", eventMap, false);
                    Slog.d("JobScheduler", "systemJobSameId: jobId = " + job.getId() + ", compNameOld=" + compName + ", compNameNew=" + jobCompName);
                }
            }
        }
    }

    private static OppoBaseJobSchedulerService typeCasting(JobSchedulerService js) {
        if (js != null) {
            return (OppoBaseJobSchedulerService) ColorTypeCastingHelper.typeCasting(OppoBaseJobSchedulerService.class, js);
        }
        return null;
    }
}
