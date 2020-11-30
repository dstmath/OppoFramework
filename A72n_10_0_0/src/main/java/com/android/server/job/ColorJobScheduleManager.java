package com.android.server.job;

import android.app.job.IJobService;
import android.app.job.JobParameters;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.OppoBaseJobParameters;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.IColorStrictModeManager;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.job.controllers.JobStatus;
import com.android.server.job.controllers.OppoAppChangeController;
import com.android.server.job.controllers.OppoBaseJobStatus;
import com.android.server.job.controllers.OppoCpuController;
import com.android.server.job.controllers.StateController;
import com.color.util.ColorTypeCastingHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import oppo.util.OppoStatistics;

public class ColorJobScheduleManager implements IColorJobScheduleManager {
    public static final String TAG = "ColorJobScheduleManager";
    private static ColorJobScheduleManager sColorJobScheduleManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    Context mContext;
    boolean mDynamicDebug = false;
    JobSchedulerService mJobSchedulerService;

    public static ColorJobScheduleManager getInstance() {
        if (sColorJobScheduleManager == null) {
            sColorJobScheduleManager = new ColorJobScheduleManager();
        }
        return sColorJobScheduleManager;
    }

    private ColorJobScheduleManager() {
    }

    public boolean needCheck() {
        return true;
    }

    public void init(IColorJobSchedulerServiceEx jssEx) {
        this.mContext = jssEx.getContext();
        this.mJobSchedulerService = jssEx.getJobSchedulerService();
    }

    public void initConstructor(List<StateController> stateControllers, JobSchedulerService service) {
        stateControllers.add(new OppoAppChangeController(service));
        stateControllers.add(new OppoCpuController(service));
    }

    public long getJobScheduleTimeoutIfNeed(JobStatus job, int verb) {
        String pkg = null;
        if (!(job == null || job.getJob() == null || job.getJob().getService() == null)) {
            pkg = job.getJob().getService().getPackageName();
        }
        if (job == null || pkg == null || verb != 2 || !OppoListManager.getInstance().getJobScheduleTimeoutWhiteList().contains(pkg)) {
            return 0;
        }
        Slog.d(TAG, "scheduleOpTimeOut set oppo job timeout to 2 hour");
        return 7200000;
    }

    public boolean updateExecutingParameter(IJobService service, Object lock, JobParameters params, JobStatus runningJob, int level, int verb) {
        synchronized (lock) {
            if (verb != 2) {
                Slog.e(TAG, "can only update running parameters");
                return false;
            }
            OppoBaseJobParameters baseParams = typeCasting(params);
            if (!(params == null || runningJob == null)) {
                if (baseParams != null) {
                    baseParams.setCpuLevel(level);
                    handleUpdateParamH(runningJob, verb, params, service);
                    return true;
                }
            }
            Slog.e(TAG, "updateRunningParameters mParams or mRunningJob is null.");
            return false;
        }
    }

    public boolean updateParamterOnServiceContextLocked(JobStatus job) {
        OppoBaseJobStatus baseJobStatus = typeCasting(job);
        if (!isReadyToUpdateCpuParameter(job, baseJobStatus)) {
            return false;
        }
        for (int i = 0; i < this.mJobSchedulerService.mActiveServices.size(); i++) {
            JobServiceContext jsc = (JobServiceContext) this.mJobSchedulerService.mActiveServices.get(i);
            JobStatus executing = jsc.getRunningJobLocked();
            if (baseJobStatus != null && executing != null && executing.matches(job.getUid(), job.getJobId())) {
                Slog.d(TAG, "update for task " + job.getTag() + ", level=" + baseJobStatus.cpuLevel.get());
                OppoBaseJobServiceContext baseJsc = typeCasting(jsc);
                if (baseJsc == null) {
                    return true;
                }
                baseJsc.updateExecutingParameter(baseJobStatus.cpuLevel.get());
                return true;
            }
        }
        return false;
    }

    private boolean isReadyToUpdateCpuParameter(JobStatus job, OppoBaseJobStatus baseJob) {
        return baseJob != null && baseJob.hasCpuConstraint() && isCurrentlyActiveLocked(job, this.mJobSchedulerService) && baseJob.oldCpuLevel.get() != baseJob.cpuLevel.get();
    }

    private void handleUpdateParamH(JobStatus mRunningJob, int verb, JobParameters params, IJobService service) {
        if (mRunningJob != null) {
            if (JobSchedulerService.DEBUG) {
                Slog.d(TAG, "Handling update param for: " + mRunningJob.getJobId() + " verb =" + verb);
            }
            if (verb == 2) {
                try {
                    callMethodByReflect(service, "updateJobParameters", JobParameters.class, params);
                } catch (Exception e) {
                    Slog.e(TAG, "Error updating parameter to client.", e);
                }
            } else if (JobSchedulerService.DEBUG) {
                Slog.d(TAG, "Trying to update param for invalid verb, ignoring.");
            }
        } else if (JobSchedulerService.DEBUG) {
            Slog.d(TAG, "Trying to update param for torn-down context, ignoring.");
        }
    }

    private void uploadJobFinishEvent(JobStatus job, boolean reschedule) {
        OppoBaseJobStatus baseJobStatus = typeCasting(job);
        if (job != null && job.getServiceComponent() != null && job.getJob() != null && job.getJob().getService() != null && baseJobStatus != null && baseJobStatus.isOppoJob()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("pkgname", job.getServiceComponent().getPackageName());
            map.put("componentName", job.getJob().getService().flattenToShortString());
            map.put("jobId", String.valueOf(job.getJobId()));
            map.put("reschedule", String.valueOf(reschedule));
            map.put("cpuCons", String.valueOf(baseJobStatus.hasCpuConstraint()));
            map.put("foreCons", String.valueOf(baseJobStatus.hasProtectForeConstraint()));
            map.put("foreType", String.valueOf(baseJobStatus.getProtectForeType()));
            OppoStatistics.onCommon(this.mContext, "20120", "job_finish", map, false);
        }
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog#### mDynamicDebug = " + this.mDynamicDebug);
        setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + this.mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorJobScheduleManager.class.getName());
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }

    public boolean isCurrentlyActiveLocked(JobStatus job, JobSchedulerService jsc) {
        for (int i = 0; i < jsc.mActiveServices.size(); i++) {
            JobStatus running = ((JobServiceContext) jsc.mActiveServices.get(i)).getRunningJobLocked();
            if (running != null && running.matches(job.getUid(), job.getJobId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSatisfied(boolean sat, JobStatus job, String pkgName) {
        if (!sat || !OppoFeatureCache.get(IColorStrictModeManager.DEFAULT).isStrictMode() || !OppoFeatureCache.get(IColorStrictModeManager.DEFAULT).isDelayAppJob(job.getUid(), pkgName)) {
            return sat;
        }
        Slog.i(TAG, "in strict mode. job: " + job);
        return false;
    }

    private static OppoBaseJobParameters typeCasting(JobParameters jobParameters) {
        if (jobParameters != null) {
            return (OppoBaseJobParameters) ColorTypeCastingHelper.typeCasting(OppoBaseJobParameters.class, jobParameters);
        }
        return null;
    }

    private static OppoBaseJobStatus typeCasting(JobStatus jobStatus) {
        if (jobStatus != null) {
            return (OppoBaseJobStatus) ColorTypeCastingHelper.typeCasting(OppoBaseJobStatus.class, jobStatus);
        }
        return null;
    }

    private static OppoBaseJobServiceContext typeCasting(JobServiceContext jc) {
        if (jc != null) {
            return (OppoBaseJobServiceContext) ColorTypeCastingHelper.typeCasting(OppoBaseJobServiceContext.class, jc);
        }
        return null;
    }

    private static Object callMethodByReflect(Object object, String methodName, Class<?> cls, Object args) {
        try {
            Method method = object.getClass().getDeclaredMethod(methodName, new Class[0]);
            method.setAccessible(true);
            return method.invoke(object, new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return null;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return null;
        }
    }
}
