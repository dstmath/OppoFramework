package com.android.server.job;

import android.common.OppoFeatureCache;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Slog;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.job.controllers.JobStatus;
import com.color.util.ColorTypeCastingHelper;

public class ColorJobSchedulerHelper {
    private static final String TAG = "ColorJobSchedulerHelper";
    private static ColorJobSchedulerHelper sJobScheduler = null;
    /* access modifiers changed from: private */
    public JobSchedulerService mJobSchedulerService = null;
    private Handler mJobServiceHandler = null;

    public static ColorJobSchedulerHelper getInstance() {
        if (sJobScheduler == null) {
            synchronized (ColorJobSchedulerHelper.class) {
                if (sJobScheduler == null) {
                    sJobScheduler = new ColorJobSchedulerHelper();
                }
            }
        }
        return sJobScheduler;
    }

    private ColorJobSchedulerHelper() {
    }

    /* access modifiers changed from: package-private */
    public void init(JobSchedulerService service) {
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mJobServiceHandler = new Handler(thread.getLooper());
        this.mJobSchedulerService = service;
    }

    /* access modifiers changed from: package-private */
    public void removePendingJob(JobStatus job) {
        this.mJobServiceHandler.post(new RemovePendingJobRunnable(job));
    }

    private class RemovePendingJobRunnable implements Runnable {
        private JobStatus mPendingJob;

        public RemovePendingJobRunnable(JobStatus pendingJob) {
            this.mPendingJob = pendingJob;
        }

        public void run() {
            try {
                OppoBaseJobSchedulerService baseJobss = ColorJobSchedulerHelper.typeCasting(ColorJobSchedulerHelper.this.mJobSchedulerService);
                if (ColorJobSchedulerHelper.this.mJobSchedulerService != null && this.mPendingJob != null && this.mPendingJob.getServiceComponent() != null && OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).isRemovePendingJob(this.mPendingJob.getServiceComponent().getPackageName(), this.mPendingJob.getUserId())) {
                    synchronized (ColorJobSchedulerHelper.this.mJobSchedulerService.mLock) {
                        baseJobss.stopTrackingJobExported(this.mPendingJob, null, false);
                    }
                }
            } catch (Exception e) {
                Slog.w(ColorJobSchedulerHelper.TAG, "removePendingJob failed");
            }
        }
    }

    /* access modifiers changed from: private */
    public static OppoBaseJobSchedulerService typeCasting(JobSchedulerService jss) {
        return (OppoBaseJobSchedulerService) ColorTypeCastingHelper.typeCasting(OppoBaseJobSchedulerService.class, jss);
    }
}
