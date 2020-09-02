package com.android.server.wm;

import android.content.Context;
import android.os.Binder;
import android.util.Slog;
import android.util.SparseArray;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ColorLockTaskController implements IColorLockTaskController {
    private static final String TAG = "ColorLockTaskController";
    private static ColorLockTaskController sColorLockTaskController;
    private Context mContext;
    private LockTaskController mController;
    private boolean mLockDeviceMode = false;
    private ArrayList<TaskRecord> mLockTaskModeTasks;
    private SparseArray<String[]> mLockTaskPackages;
    protected String mRootLockPackage;
    private ActivityStackSupervisor mSupervisor;

    private ColorLockTaskController() {
    }

    public static final synchronized ColorLockTaskController getInstance() {
        ColorLockTaskController colorLockTaskController;
        synchronized (ColorLockTaskController.class) {
            if (sColorLockTaskController == null) {
                sColorLockTaskController = new ColorLockTaskController();
            }
            colorLockTaskController = sColorLockTaskController;
        }
        return colorLockTaskController;
    }

    public void init(Context context, ActivityStackSupervisor supervisor, LockTaskController taskController, ArrayList<TaskRecord> lockTasks, SparseArray<String[]> lockPackages) {
        this.mContext = context;
        this.mSupervisor = supervisor;
        this.mController = taskController;
        this.mLockTaskModeTasks = lockTasks;
        this.mLockTaskPackages = lockPackages;
    }

    public void startLockDeviceMode(int userId, String rootPkg, String[] packages) {
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mSupervisor.mService) {
                this.mLockDeviceMode = true;
                clearLockedDeviceTask();
                this.mLockTaskModeTasks.clear();
                this.mLockTaskPackages.clear();
                this.mRootLockPackage = rootPkg;
                this.mLockTaskPackages.put(userId, packages);
                updateTaskAuthLocked();
                Slog.w(TAG, "startLockDeviceMode");
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void stopLockDeviceModeBySystem() {
        Slog.w(TAG, "stopLockDeviceModeBySystem: ", new RuntimeException("here").fillInStackTrace());
        stopLockDeviceMode();
    }

    public void stopLockDeviceMode() {
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mSupervisor.mService) {
                this.mLockDeviceMode = false;
                TaskRecord rootLockTask = null;
                if (!this.mLockTaskModeTasks.isEmpty()) {
                    rootLockTask = this.mLockTaskModeTasks.get(0);
                }
                this.mController.clearLockedTasks("stop_lockdevice");
                this.mLockTaskPackages.clear();
                updateTaskAuthLocked();
                if (rootLockTask != null) {
                    Slog.w(TAG, "stopLockDeviceMode: clear rootLockTask=" + rootLockTask);
                    rootLockTask.performClearTaskLocked();
                    this.mSupervisor.mRootActivityContainer.resumeFocusedStacksTopActivities();
                }
                Slog.w(TAG, "stopLockDeviceMode");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
        Binder.restoreCallingIdentity(token);
    }

    public void updateTaskAuthLocked() {
        for (int displayNdx = this.mSupervisor.mRootActivityContainer.getChildCount() - 1; displayNdx >= 0; displayNdx--) {
            this.mSupervisor.mRootActivityContainer.getChildAt(displayNdx).onLockTaskPackagesUpdated();
        }
    }

    private void clearLockedDeviceTask() {
        for (int taskNdx = this.mLockTaskModeTasks.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord lockedTask = this.mLockTaskModeTasks.get(taskNdx);
            if (lockedTask != null) {
                lockedTask.performClearTaskLocked();
            }
        }
    }

    public boolean isLockDeviceMode() {
        return this.mLockDeviceMode;
    }

    public String getRootLockPkgName() {
        return this.mRootLockPackage;
    }

    public boolean canShowInLockDeviceMode(int type) {
        if (!this.mLockDeviceMode) {
            return true;
        }
        if (type == 2002 || type == 2003 || type == 2010 || type == 2038 || type == 2314) {
            return false;
        }
        switch (type) {
            case 2005:
            case 2006:
            case 2007:
                return false;
            default:
                return true;
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "ColorLockTaskController:");
        pw.println(prefix + "  mLockDeviceMode=" + this.mLockDeviceMode);
    }
}
