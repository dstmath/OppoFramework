package com.android.server.job.controllers;

import android.app.OppoActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.OppoGuardElfConfigUtil;
import com.android.server.am.OppoAppSwitchManager;
import com.android.server.am.OppoAppSwitchManager.ActivityChangedListener;
import com.android.server.coloros.OppoListManager;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OppoAppChangeController extends StateController {
    private static final int FLAG_PROTECT_FORE_FRAME = 1;
    private static final int FLAG_PROTECT_FORE_NET = 2;
    private static final int MSG_START_MONITOR_FORE_STATE = 1003;
    private static final int MSG_STOP_MONITOR_FORE_STATE = 1004;
    private static final int MSG_UPDATE_FORE_PKG = 1001;
    private static final int MSG_UPDATE_FORE_STATE = 1002;
    private static boolean OPPODEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String TAG = "JobScheduler.AppChange";
    private static volatile OppoAppChangeController sController;
    private static final Object sCreationLock = new Object();
    private ActivityChangedListener mActivityChangedListener = new ActivityChangedListener() {
        public void onActivityChanged(String pre, String next) {
            if (next != null) {
                OppoAppChangeController.this.mCurrentIdleValue = OppoAppChangeController.this.getIdleValue(next);
                if (OppoAppChangeController.OPPODEBUG) {
                    Slog.d(OppoAppChangeController.TAG, "onActivityChanged pre=" + pre + ", next=" + next + ", current=" + OppoAppChangeController.this.mCurrentIdleValue + ", old=" + OppoAppChangeController.this.mOldIdleValue);
                }
                if (OppoAppChangeController.this.mCurrentIdleValue != OppoAppChangeController.this.mOldIdleValue) {
                    OppoAppChangeController.this.mOldIdleValue = OppoAppChangeController.this.mCurrentIdleValue;
                    OppoAppChangeController.this.mChangeHandler.removeMessages(1002);
                    OppoAppChangeController.this.mChangeHandler.sendEmptyMessageDelayed(1002, OppoGuardElfConfigUtil.getInstance().getTimeForeAppStable());
                }
            }
        }
    };
    private AppChangeTraker mAppChangeTraker;
    private AppChangeHandler mChangeHandler;
    private int mCurrentIdleValue = 0;
    private boolean mForeInited = false;
    private int mOldIdleValue = 0;
    private List<JobStatus> mTrackedTasks = new ArrayList();

    private class AppChangeHandler extends Handler {
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
                case OppoAppChangeController.MSG_START_MONITOR_FORE_STATE /*1003*/:
                    String fore = OppoAppChangeController.this.getForegroundPackage();
                    if (fore != null) {
                        OppoAppChangeController.this.mCurrentIdleValue = OppoAppChangeController.this.getIdleValue(fore);
                    }
                    if (OppoAppChangeController.OPPODEBUG) {
                        Slog.d(TAG, "screen on, start monitor app change, fore=" + fore + ", idleValue=" + OppoAppChangeController.this.mCurrentIdleValue);
                    }
                    OppoAppChangeController.this.maybeReportNewAppChangeState();
                    return;
                case OppoAppChangeController.MSG_STOP_MONITOR_FORE_STATE /*1004*/:
                    OppoAppChangeController.this.resetIdleValue();
                    OppoAppChangeController.this.maybeReportNewAppChangeState();
                    return;
                default:
                    return;
            }
        }
    }

    private class AppChangeTraker extends BroadcastReceiver {
        /* synthetic */ AppChangeTraker(OppoAppChangeController this$0, AppChangeTraker -this1) {
            this();
        }

        private AppChangeTraker() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.SCREEN_ON")) {
                OppoAppChangeController.this.mChangeHandler.sendEmptyMessage(OppoAppChangeController.MSG_START_MONITOR_FORE_STATE);
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                OppoAppChangeController.this.mChangeHandler.sendEmptyMessage(OppoAppChangeController.MSG_STOP_MONITOR_FORE_STATE);
            }
        }

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_ON");
            filter.addAction("android.intent.action.SCREEN_OFF");
            OppoAppChangeController.this.mContext.registerReceiver(this, filter);
        }
    }

    public static OppoAppChangeController get(JobSchedulerService taskManagerService) {
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new OppoAppChangeController(taskManagerService, taskManagerService.getContext(), taskManagerService.getLock());
            }
        }
        return sController;
    }

    public static OppoAppChangeController getForTesting(StateChangedListener stateChangedListener, Context context) {
        return new OppoAppChangeController(stateChangedListener, context, new Object());
    }

    private OppoAppChangeController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        this.mChangeHandler = new AppChangeHandler(context.getMainLooper());
        OppoAppSwitchManager.getInstance().setActivityChangedListener(this.mActivityChangedListener);
        this.mAppChangeTraker = new AppChangeTraker(this, null);
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
        if (taskStatus != null && taskStatus.hasProtectForeConstraint()) {
            synchronized (this.mTrackedTasks) {
                if (OPPODEBUG) {
                    Slog.d(TAG, "maybeStartTrackingJob job " + taskStatus.getJob() + ", mCurrentIdleValue=" + this.mCurrentIdleValue);
                }
                this.mTrackedTasks.add(taskStatus);
                taskStatus.setProtectForeConstraintSatisfied(getSatisfyValueForJs(this.mCurrentIdleValue, taskStatus.getProtectForeType()));
            }
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        if (taskStatus != null && taskStatus.hasProtectForeConstraint()) {
            synchronized (this.mTrackedTasks) {
                if (OPPODEBUG) {
                    Slog.d(TAG, "maybeStopTrackingJob job " + taskStatus.getJob() + ", mCurrentIdleValue=" + this.mCurrentIdleValue);
                }
                this.mTrackedTasks.remove(taskStatus);
            }
        }
    }

    private void maybeReportNewAppChangeState() {
        if (DEBUG) {
            Slog.d(TAG, "maybeReportNewAppChangeState:  " + this.mCurrentIdleValue);
        }
        boolean reportChange = false;
        synchronized (this.mTrackedTasks) {
            for (JobStatus ts : this.mTrackedTasks) {
                if (ts.setProtectForeConstraintSatisfied(getSatisfyValueForJs(this.mCurrentIdleValue, ts.getProtectForeType()))) {
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

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.println("OppoForegroundIdle.");
        pw.println("OppoForegroundIdle : " + this.mCurrentIdleValue);
        synchronized (this.mTrackedTasks) {
            Iterator<JobStatus> it = this.mTrackedTasks.iterator();
            if (it.hasNext()) {
                pw.print(String.valueOf(((JobStatus) it.next()).hashCode()));
            }
            while (it.hasNext()) {
                pw.print("," + String.valueOf(((JobStatus) it.next()).hashCode()));
            }
            pw.println();
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

    private int getIdleValue(String pkg) {
        int idle = 0;
        if (pkg == null || pkg.isEmpty()) {
            return 0;
        }
        if (!OppoListManager.getInstance().getProtectForeList().contains(pkg)) {
            idle = 1;
        }
        if (!OppoListManager.getInstance().getProtectForeNetList().contains(pkg)) {
            idle |= 2;
        }
        return idle;
    }

    private boolean getSatisfyValueForJs(int idleValue, int type) {
        boolean z = true;
        if (type == 0) {
            if ((idleValue & 1) == 0) {
                z = false;
            }
            return z;
        } else if (type != 1) {
            return false;
        } else {
            if ((idleValue & 2) == 0) {
                z = false;
            }
            return z;
        }
    }

    private void resetIdleValue() {
        this.mCurrentIdleValue |= 1;
        this.mCurrentIdleValue |= 2;
    }
}
