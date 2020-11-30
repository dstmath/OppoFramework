package com.android.server.wm;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.ColorDeviceIdleHelper;
import com.android.server.ColorListManagerImpl;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.IColorActivityManagerServiceEx;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorCommonConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorAthenaManager implements IColorAthenaManager {
    private static final String ONEKEY_PROTECT_LIST = "onekey_protect_list";
    private static final String RECENT_LOCK_LIST = "recent_lock_list";
    private static final List<String> REMOVE_TASK_NOT_SKIP_LIST = Arrays.asList("com.nearme.instant.platform", "com.heytap.instant.platform");
    public static final String TAG = "ColorAthena";
    private static boolean sDebug = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static long sFirstBootTime = 0;
    boolean DEBUG_SWITCH;
    private ActivityManagerService mAms;
    private ActivityTaskManagerService mAmsTask;
    boolean mDynamicDebug;
    private ColorProcessWhiteListUtils mWhiteListUtils;

    private ColorAthenaManager() {
        this.mDynamicDebug = false;
        this.DEBUG_SWITCH = sDebug | this.mDynamicDebug;
        this.mAms = null;
        this.mAmsTask = null;
        this.mWhiteListUtils = null;
    }

    private static class ColorAthenaManagerInstance {
        private static final ColorAthenaManager sInstance = new ColorAthenaManager();

        private ColorAthenaManagerInstance() {
        }
    }

    public static ColorAthenaManager getInstance() {
        return ColorAthenaManagerInstance.sInstance;
    }

    public void init(IColorActivityManagerServiceEx amsEx) {
        if (amsEx != null) {
            this.mAms = amsEx.getActivityManagerService();
            this.mAmsTask = amsEx.getActivityManagerService().mActivityTaskManager;
        }
        this.mWhiteListUtils = ColorProcessWhiteListUtils.getInstance();
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebug | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        setDynamicDebugSwitch(on);
        Slog.i(TAG, "DEBUG_SWITCH " + this.DEBUG_SWITCH);
    }

    public int getRemoveTaskFilterType(WindowProcessController proc) {
        if (this.mAmsTask == null) {
            return 0;
        }
        int result = 0;
        if (proc.hasForegroundServices()) {
            if (proc.mName == null || proc.mInfo == null || proc.mInfo.packageName == null || !REMOVE_TASK_NOT_SKIP_LIST.contains(proc.mInfo.packageName)) {
                return 1;
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                Slog.d(TAG, "remove kill force to not skip process(with fgs): " + proc.mName);
            }
            result = 3;
        }
        if (!(proc.mName == null || proc.mInfo == null || proc.mInfo.packageName == null)) {
            ArrayList<String> stageProtectList = OppoListManager.getInstance().getStageProtectList();
            if (stageProtectList == null || !stageProtectList.contains(proc.mInfo.packageName)) {
                ArrayList<String> filterList = OppoListManager.getInstance().getRemoveTaskFilterPkgList(this.mAmsTask.mContext);
                if (filterList == null || !filterList.contains(proc.mInfo.packageName)) {
                    ArrayList<String> proFilterList = OppoListManager.getInstance().getRemoveTaskFilterProcessList(this.mAmsTask.mContext);
                    if (proFilterList == null || !proFilterList.contains(proc.mName)) {
                        if ("1".equals(SystemProperties.get("oppo.clear.running", "0")) && isOneKeyProtectPkg(proc.mInfo.packageName)) {
                            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                                Slog.d(TAG, "remove kill skip package for okpt: " + proc.mInfo.packageName);
                            }
                            return 1;
                        }
                    } else if (!ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                        return 2;
                    } else {
                        Slog.d(TAG, "remove kill skip process for rtf: " + proc.mName);
                        return 2;
                    }
                } else {
                    if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                        Slog.d(TAG, "remove kill skip package for rtf: " + proc.mInfo.packageName);
                    }
                    return 1;
                }
            } else {
                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(TAG, "remove kill skip package for sp: " + proc.mInfo.packageName);
                }
                return 1;
            }
        }
        return result;
    }

    public List<String> getProtectList() {
        String pkgName;
        if (this.mAmsTask == null) {
            return null;
        }
        List<String> protectList = new ArrayList<>();
        RecentTasks recentTask = this.mAmsTask.getRecentTasks();
        if (recentTask == null) {
            return null;
        }
        ArrayList<TaskRecord> recentTaskList = recentTask.getRawTasks();
        int size = recentTaskList.size();
        int num = 2;
        if (size <= 2) {
            num = size;
        }
        ComponentName topCpn = getTopComponent();
        if (!(topCpn == null || topCpn.getPackageName() == null)) {
            protectList.add(topCpn.getPackageName());
        }
        for (int i = 0; i < num; i++) {
            TaskRecord record = recentTaskList.get(i);
            if (!(record == null || record.getBaseIntent() == null || record.getBaseIntent().getComponent() == null || (pkgName = record.getBaseIntent().getComponent().getPackageName()) == null || pkgName.equals("com.coloros.recents"))) {
                protectList.add(pkgName);
            }
        }
        return protectList;
    }

    public List<String> getTaskPkgList(int taskId) {
        synchronized (this.mAmsTask.mGlobalLock) {
            TaskRecord tr = this.mAmsTask.getRecentTasks().getTask(taskId);
            if (tr == null) {
                return null;
            }
            return getPkgListInTaskRecord(tr);
        }
    }

    public List<String> getAppAssociatedActivity(String packageName) {
        List<String> res = new ArrayList<>();
        if (packageName == null) {
            return res;
        }
        synchronized (this.mAmsTask.mGlobalLock) {
            List<TaskRecord> list = this.mAmsTask.getRecentTasks().getRawTasks();
            if (list.isEmpty()) {
                return res;
            }
            for (TaskRecord tr : list) {
                if (!(tr.getBaseIntent() == null || tr.getBaseIntent().getComponent() == null || !packageName.equals(tr.getBaseIntent().getComponent().getPackageName()))) {
                    return getPkgListInTaskRecord(tr);
                }
            }
            return null;
        }
    }

    private List<String> getPkgListInTaskRecord(TaskRecord tr) {
        List<String> actList = new ArrayList<>();
        System.currentTimeMillis();
        if (tr.mActivities != null && !tr.mActivities.isEmpty()) {
            for (int i = 0; i < tr.mActivities.size(); i++) {
                ActivityRecord act = (ActivityRecord) tr.mActivities.get(i);
                if (act != null && !act.finishing && !actList.contains(act.packageName)) {
                    actList.add(act.packageName);
                }
            }
        }
        return actList;
    }

    public boolean killPackageProcessesFilter(WindowProcessController app, String packageName, boolean isDep, int appId) {
        if (!isDep || (app.mInfo.flags & 1) == 0 || UserHandle.getAppId(app.mUid) == appId) {
            return false;
        }
        return true;
    }

    public boolean killBackgroundProcessFilter(String packageName, int callingUid) {
        if (this.mWhiteListUtils == null) {
            return false;
        }
        String callingPkg = null;
        try {
            callingPkg = AppGlobals.getPackageManager().getNameForUid(callingUid);
        } catch (RemoteException e) {
        }
        if (this.mWhiteListUtils.getProcessWhiteList().contains(packageName) && callingUid != 1000 && !this.mWhiteListUtils.getAuthorizedProcessList().contains(callingPkg)) {
            Slog.v(TAG, packageName + " won't killed by " + callingPkg);
            return true;
        } else if (!OppoListManager.getInstance().getStageProtectList().contains(packageName) || this.mWhiteListUtils.getAuthorizedProcessList().contains(callingPkg)) {
            Slog.v(TAG, "kill background: " + packageName + " is called by " + callingPkg);
            return false;
        } else {
            Slog.v(TAG, packageName + " being protected, won't killed by " + callingPkg);
            return true;
        }
    }

    public boolean startActivityFilter(Intent intent, String callingPackage, int callingUid, int callingPid) {
        if (!(this.mWhiteListUtils == null || intent == null || intent.getData() == null)) {
            String pkgNameInUri = "";
            try {
                pkgNameInUri = intent.getData().toString().split(":")[1];
            } catch (IndexOutOfBoundsException e) {
            }
            if (this.DEBUG_SWITCH) {
                Slog.v(TAG, "pkgName in Uri: " + pkgNameInUri + "callingPkg: " + callingPackage);
            }
            List<String> processWhiteList = this.mWhiteListUtils.getProcessWhiteList();
            if (!processWhiteList.contains(pkgNameInUri) || callingUid == 1000 || OppoListManager.getInstance().isSystemApp(callingPackage) || (processWhiteList.contains(callingPackage) && pkgNameInUri.equals(callingPackage))) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean skipAmsEmptyKill(WindowProcessController app) {
        List list;
        String procName = app.mName;
        if (TextUtils.isEmpty(procName) || (list = OppoListManager.getInstance().getAmsEmptyKillFilterList()) == null || !list.contains(procName)) {
            String pkgName = app.mInfo != null ? app.mInfo.packageName : null;
            if (TextUtils.isEmpty(pkgName)) {
                return false;
            }
            if (OppoListManager.getInstance().getStageProtectList().contains(pkgName)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(TAG, procName + " kill(empty #) skipped for stage protect");
                }
                return true;
            } else if (OppoListManager.getInstance().isOppoTestTool(pkgName, app.mInfo.uid)) {
                if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                    Slog.d(TAG, procName + " kill(empty #) skipped for test tool");
                }
                return true;
            } else if (!OppoListManager.getInstance().isCustomizeAmsCleanupEnable() || !OppoListManager.getInstance().isInCustomWhiteList(pkgName) || !ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                return false;
            } else {
                Slog.d(TAG, procName + " kill(empty #) skipped for custom whitelist");
                return false;
            }
        } else {
            if (ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
                Slog.d(TAG, procName + " kill(empty #) skipped for protect");
            }
            return true;
        }
    }

    private boolean isInBootupTime() {
        if (sFirstBootTime <= 0) {
            sFirstBootTime = Long.parseLong(SystemProperties.get("ro.runtime.firstboot", "0"));
        }
        return System.currentTimeMillis() - sFirstBootTime < ColorDeviceIdleHelper.ALARM_WINDOW_LENGTH;
    }

    public boolean skipAmsEmptyKillBootUp(WindowProcessController app) {
        List list;
        if (app == null || !isInBootupTime() || (list = OppoListManager.getInstance().getAmsEmptyKillBootUpFilterList()) == null || !list.contains(app.mName)) {
            return false;
        }
        if (!ActivityTaskManagerDebugConfig.DEBUG_RECENTS) {
            return true;
        }
        Slog.d(TAG, app.mName + " kill(empty #) skipped before system start 3 min");
        return true;
    }

    private boolean isOneKeyProtectPkg(String pkgName) {
        Bundle data;
        ArrayList<String> list;
        if (TextUtils.isEmpty(pkgName) || (data = ColorCommonConfig.getInstance().getConfigInfo(ONEKEY_PROTECT_LIST, 1)) == null || (list = data.getStringArrayList(ONEKEY_PROTECT_LIST)) == null || !list.contains(pkgName)) {
            return false;
        }
        return true;
    }

    public boolean isRecentLockTask(TaskRecord task) {
        ArrayList<String> list;
        if (task == null || task.intent == null || task.intent.getComponent() == null) {
            return false;
        }
        String pkgName = task.intent.getComponent().getPackageName();
        int userId = task.userId;
        Bundle data = ColorListManagerImpl.getInstance().getConfigInfo(RECENT_LOCK_LIST, UserHandle.getCallingUserId());
        if (data == null || (list = data.getStringArrayList(RECENT_LOCK_LIST)) == null) {
            return false;
        }
        if (!list.contains(pkgName + "#" + userId)) {
            return false;
        }
        Slog.d(TAG, pkgName + " in recent lock list");
        return true;
    }

    private ComponentName getTopComponent() {
        ActivityRecord top;
        ActivityTaskManagerService activityTaskManagerService = this.mAmsTask;
        if (activityTaskManagerService == null || (top = activityTaskManagerService.mRootActivityContainer.topRunningActivity()) == null) {
            return null;
        }
        return top.mActivityComponent;
    }
}
