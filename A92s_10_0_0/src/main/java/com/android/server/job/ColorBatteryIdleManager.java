package com.android.server.job;

import android.app.job.JobInfo;
import android.app.job.OppoBaseJobInfo;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Slog;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.job.controllers.BatteryIdleController;
import com.android.server.job.controllers.StateController;
import com.android.server.pm.IColorFullmodeManager;
import com.color.util.ColorTypeCastingHelper;
import java.util.List;

public class ColorBatteryIdleManager implements IBatteryIdleController {
    private static final String TAG = "ColorBatteryIdleManager";

    public void addController(List<StateController> controllers, JobSchedulerService service) {
        controllers.add(new BatteryIdleController(service));
    }

    public void updateSystemFlag(Context context, JobInfo job, int uid) {
        boolean isSysApp = minIntervalConstraint(context, job, uid);
        OppoBaseJobInfo baseJob = typeCasting(job);
        if (baseJob != null) {
            baseJob.setSysApp(isSysApp);
        }
    }

    private boolean minIntervalConstraint(Context context, JobInfo job, int uId) {
        String jobPkg;
        if (uId < 10000) {
            return true;
        }
        OppoBaseJobInfo baseJobInfo = typeCasting(job);
        PackageManager packageManager = context.getPackageManager();
        if (OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall() || job.getService() == null || (jobPkg = job.getService().getPackageName()) == null) {
            return true;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = packageManager.getApplicationInfo(jobPkg, 8192);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.d(TAG, "getApplicationInfo NameNotFoundException. pkg = " + jobPkg);
        }
        if (appInfo == null || (appInfo.flags & 1) != 0) {
            return true;
        }
        long minInterval = OppoGuardElfConfigUtil.getInstance().getThreshJobMinInterval() * 1000;
        if (job.isPeriodic()) {
            if (job.getIntervalMillis() >= minInterval || baseJobInfo == null) {
                return false;
            }
            baseJobInfo.setIntervalMillis(minInterval);
            return false;
        } else if (!job.hasLateConstraint() || job.getMaxExecutionDelayMillis() >= minInterval || baseJobInfo == null) {
            return false;
        } else {
            baseJobInfo.setMaxExecutionDelayMillis(minInterval);
            return false;
        }
    }

    private static OppoBaseJobInfo typeCasting(JobInfo info) {
        if (info != null) {
            return (OppoBaseJobInfo) ColorTypeCastingHelper.typeCasting(OppoBaseJobInfo.class, info);
        }
        return null;
    }
}
