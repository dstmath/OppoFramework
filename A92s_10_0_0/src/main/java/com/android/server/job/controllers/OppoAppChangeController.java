package com.android.server.job.controllers;

import android.app.OppoActivityManager;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.job.JobSchedulerService;
import com.android.server.wm.IColorAppSwitchManager;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class OppoAppChangeController extends StateController {
    private static final boolean DEBUG = (JobSchedulerService.DEBUG || Log.isLoggable(TAG, 3));
    private static final int FLAG_PROTECT_FORE_FRAME = 1;
    private static final int FLAG_PROTECT_FORE_NET = 2;
    private static final int MSG_START_MONITOR_FORE_STATE = 1003;
    private static final int MSG_STOP_MONITOR_FORE_STATE = 1004;
    private static final int MSG_UPDATE_FORE_PKG = 1001;
    private static final int MSG_UPDATE_FORE_STATE = 1002;
    /* access modifiers changed from: private */
    public static boolean OPPODEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String TAG = "JobScheduler.AppChange";
    private static volatile OppoAppChangeController sController;
    private static final Object sCreationLock = new Object();
    private IColorAppSwitchManager.ActivityChangedListener mActivityChangedListener = new IColorAppSwitchManager.ActivityChangedListener() {
        /* class com.android.server.job.controllers.OppoAppChangeController.AnonymousClass1 */

        public void onActivityChanged(String pre, String next) {
            if (next != null) {
                OppoAppChangeController oppoAppChangeController = OppoAppChangeController.this;
                int unused = oppoAppChangeController.mCurrentIdleValue = oppoAppChangeController.getIdleValue(next);
                if (OppoAppChangeController.OPPODEBUG) {
                    Slog.d(OppoAppChangeController.TAG, "onActivityChanged pre=" + pre + ", next=" + next + ", current=" + OppoAppChangeController.this.mCurrentIdleValue + ", old=" + OppoAppChangeController.this.mOldIdleValue);
                }
                if (OppoAppChangeController.this.mCurrentIdleValue != OppoAppChangeController.this.mOldIdleValue) {
                    OppoAppChangeController oppoAppChangeController2 = OppoAppChangeController.this;
                    int unused2 = oppoAppChangeController2.mOldIdleValue = oppoAppChangeController2.mCurrentIdleValue;
                    OppoAppChangeController.this.mChangeHandler.removeMessages(1002);
                    OppoAppChangeController.this.mChangeHandler.sendEmptyMessageDelayed(1002, OppoGuardElfConfigUtil.getInstance().getTimeForeAppStable());
                }
            }
        }
    };
    private AppChangeTraker mAppChangeTraker;
    /* access modifiers changed from: private */
    public AppChangeHandler mChangeHandler = new AppChangeHandler(this.mContext.getMainLooper());
    /* access modifiers changed from: private */
    public int mCurrentIdleValue = 0;
    private boolean mForeInited = DEBUG;
    /* access modifiers changed from: private */
    public int mOldIdleValue = 0;
    private List<JobStatus> mTrackedTasks = new ArrayList();

    public OppoAppChangeController(JobSchedulerService service) {
        super(service);
        OppoFeatureCache.get(IColorAppSwitchManager.DEFAULT).setActivityChangedListener(this.mActivityChangedListener);
        this.mAppChangeTraker = new AppChangeTraker();
        this.mAppChangeTraker.startTracking();
    }

    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        if (!this.mForeInited) {
            String fore = getForegroundPackage();
            if (fore != null) {
                this.mCurrentIdleValue = getIdleValue(fore);
            }
            this.mForeInited = true;
        }
        OppoBaseJobStatus baseJobStatus = typeCasting(taskStatus);
        if (baseJobStatus != null && baseJobStatus.hasProtectForeConstraint()) {
            synchronized (this.mTrackedTasks) {
                if (OPPODEBUG) {
                    Slog.d(TAG, "maybeStartTrackingJob job " + taskStatus.getJob() + ", mCurrentIdleValue=" + this.mCurrentIdleValue);
                }
                this.mTrackedTasks.add(taskStatus);
                baseJobStatus.setProtectForeConstraintSatisfied(getSatisfyValueForJs(this.mCurrentIdleValue, baseJobStatus.getProtectForeType()));
            }
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        OppoBaseJobStatus baseJobStatus = typeCasting(taskStatus);
        if (baseJobStatus != null && baseJobStatus.hasProtectForeConstraint()) {
            synchronized (this.mTrackedTasks) {
                if (OPPODEBUG) {
                    Slog.d(TAG, "maybeStopTrackingJob job " + taskStatus.getJob() + ", mCurrentIdleValue=" + this.mCurrentIdleValue);
                }
                this.mTrackedTasks.remove(taskStatus);
            }
        }
    }

    /* access modifiers changed from: private */
    public void maybeReportNewAppChangeState() {
        if (DEBUG) {
            Slog.d(TAG, "maybeReportNewAppChangeState:  " + this.mCurrentIdleValue);
        }
        boolean reportChange = DEBUG;
        synchronized (this.mTrackedTasks) {
            for (JobStatus ts : this.mTrackedTasks) {
                OppoBaseJobStatus baseJobStatus = typeCasting(ts);
                if (baseJobStatus != null && baseJobStatus.setProtectForeConstraintSatisfied(getSatisfyValueForJs(this.mCurrentIdleValue, baseJobStatus.getProtectForeType()))) {
                    reportChange = true;
                }
            }
        }
        if (reportChange) {
            if (OPPODEBUG) {
                Slog.d(TAG, "onControllerStateChanged mCurrentIdleValue=" + this.mCurrentIdleValue);
            }
            this.mStateChangedListener.onControllerStateChanged();
        }
    }

    /* access modifiers changed from: private */
    public class AppChangeHandler extends Handler {
        private static final String TAG = "JobScheduler.AppChange";

        public AppChangeHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case 1002:
                    OppoAppChangeController.this.maybeReportNewAppChangeState();
                    return;
                case 1003:
                    String fore = OppoAppChangeController.this.getForegroundPackage();
                    if (fore != null) {
                        OppoAppChangeController oppoAppChangeController = OppoAppChangeController.this;
                        int unused = oppoAppChangeController.mCurrentIdleValue = oppoAppChangeController.getIdleValue(fore);
                    }
                    if (OppoAppChangeController.OPPODEBUG) {
                        Slog.d(TAG, "screen on, start monitor app change, fore=" + fore + ", idleValue=" + OppoAppChangeController.this.mCurrentIdleValue);
                    }
                    OppoAppChangeController.this.maybeReportNewAppChangeState();
                    return;
                case 1004:
                    OppoAppChangeController.this.resetIdleValue();
                    OppoAppChangeController.this.maybeReportNewAppChangeState();
                    return;
                default:
                    return;
            }
        }
    }

    private class AppChangeTraker extends BroadcastReceiver {
        private AppChangeTraker() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                OppoAppChangeController.this.mChangeHandler.sendEmptyMessage(1003);
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                OppoAppChangeController.this.mChangeHandler.sendEmptyMessage(1004);
            }
        }

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            OppoAppChangeController.this.mContext.registerReceiver(this, filter);
        }
    }

    public String getForegroundPackage() {
        ComponentName cn;
        try {
            cn = new OppoActivityManager().getTopActivityComponentName();
        } catch (Exception e) {
            Slog.w(TAG, "getTopActivityComponentName exception");
            cn = null;
        }
        if (cn != null) {
            return cn.getPackageName();
        }
        return null;
    }

    /* access modifiers changed from: private */
    public int getIdleValue(String pkg) {
        int idle = 0;
        if (pkg == null || pkg.isEmpty()) {
            return 0;
        }
        if (!OppoListManager.getInstance().getProtectForeList().contains(pkg)) {
            idle = 0 | 1;
        }
        if (!OppoListManager.getInstance().getProtectForeNetList().contains(pkg)) {
            return idle | 2;
        }
        return idle;
    }

    private boolean getSatisfyValueForJs(int idleValue, int type) {
        if (type == 0) {
            if ((idleValue & 1) != 0) {
                return true;
            }
            return DEBUG;
        } else if (type != 1 || (idleValue & 2) == 0) {
            return DEBUG;
        } else {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void resetIdleValue() {
        this.mCurrentIdleValue |= 1;
        this.mCurrentIdleValue |= 2;
    }

    public void dumpControllerStateLocked(IndentingPrintWriter pw, Predicate<JobStatus> predicate) {
    }

    public void dumpControllerStateLocked(ProtoOutputStream proto, long fieldId, Predicate<JobStatus> predicate) {
    }

    private static OppoBaseJobStatus typeCasting(JobStatus jobStatus) {
        if (jobStatus != null) {
            return (OppoBaseJobStatus) ColorTypeCastingHelper.typeCasting(OppoBaseJobStatus.class, jobStatus);
        }
        return null;
    }
}
