package com.android.server.wm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.view.IApplicationToken;
import com.android.server.am.ActivityManagerService;
import com.color.util.ColorTypeCastingHelper;
import java.util.ArrayList;
import java.util.List;

public class ColorAppStartupManagerHelper {
    private static volatile ColorAppStartupManagerHelper sColorAppStartupManagerHelper = null;

    public static ColorAppStartupManagerHelper getInstance() {
        if (sColorAppStartupManagerHelper == null) {
            synchronized (ColorAppStartupManagerHelper.class) {
                if (sColorAppStartupManagerHelper == null) {
                    sColorAppStartupManagerHelper = new ColorAppStartupManagerHelper();
                }
            }
        }
        return sColorAppStartupManagerHelper;
    }

    public static ActivityRecord getActivityRecordFromToken(IBinder token) {
        return ActivityRecord.forTokenLocked(token);
    }

    public ApplicationInfo getActivityRecordAppInfo(ActivityRecord r) {
        return r.appInfo;
    }

    public String getActivityRecordLaunchedFromPackage(ActivityRecord r) {
        return r.launchedFromPackage;
    }

    public int getActivityRecordLaunchedFromPid(ActivityRecord r) {
        return r.launchedFromPid;
    }

    public Intent getActivityRecordIntent(ActivityRecord r) {
        return r.intent;
    }

    public ActivityRecord getTopActivity(ActivityStack stack) {
        return stack.getTopActivity();
    }

    public ActivityRecord getResumedActivity(ActivityStack stack) {
        return stack.getResumedActivity();
    }

    public void requestFinishActivityLocked(ActivityStack stack, IApplicationToken.Stub appToken, int resultCode, Intent resultData, String reason, boolean oomAdj) {
        stack.requestFinishActivityLocked(appToken, resultCode, resultData, reason, oomAdj);
    }

    public IApplicationToken.Stub getActivityRecordAppToken(ActivityRecord r) {
        return r.appToken;
    }

    public boolean isPkgInRecentTasks(ActivityManagerService ams, String pkg) {
        RecentTasks recentTask;
        ArrayList<TaskRecord> recentTaskList;
        if (pkg == null || (recentTask = ams.mActivityTaskManager.getRecentTasks()) == null || (recentTaskList = recentTask.getRawTasks()) == null) {
            return false;
        }
        synchronized (this) {
            int recentCount = recentTaskList.size();
            for (int i = 0; i < recentCount; i++) {
                TaskRecord tr = recentTaskList.get(i);
                if (tr.getBaseIntent() != null && tr.getBaseIntent().getComponent() != null && tr.getBaseIntent().getComponent().getPackageName() != null && pkg.equals(tr.getBaseIntent().getComponent().getPackageName())) {
                    return true;
                }
            }
            return false;
        }
    }

    public RootActivityContainer getRootActivityContainer(ActivityManagerService ams) {
        return ams.mActivityTaskManager.mRootActivityContainer;
    }

    public List<String> getResumePkgList(ActivityManagerService ams) {
        ActivityDisplay display2;
        List<String> pkgList = new ArrayList<>();
        boolean second = false;
        if (!(ams == null || (display2 = ams.mActivityTaskManager.mRootActivityContainer.getChildAt(0)) == null)) {
            for (int stackNdx = display2.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display2.getChildAt(stackNdx);
                ActivityRecord topActivityRecord = getTopActivity(stack);
                if (stack.getWindowingMode() == 3) {
                    if (topActivityRecord != null) {
                        pkgList.add(topActivityRecord.packageName);
                    }
                } else if (stack.getWindowingMode() == 4 && !second && topActivityRecord != null) {
                    pkgList.add(topActivityRecord.packageName);
                    second = true;
                }
                ActivityRecord record = getResumedActivity(stack);
                if (!(record == null || record.packageName == null)) {
                    pkgList.add(record.packageName);
                }
            }
        }
        return pkgList;
    }

    public boolean canShowDialogs(ActivityManagerService ams) {
        OppoBaseActivityTaskManagerService baseAtms;
        if (ams == null || (baseAtms = typeCasting(ams.mActivityTaskManager)) == null || baseAtms.mColorAtmsInner == null || !baseAtms.mColorAtmsInner.getShowDialogs()) {
            return false;
        }
        return true;
    }

    public ActivityRecord getTopRunningActivityLocked(ActivityManagerService ams) {
        ActivityRecord activityRecord;
        if (ams == null || ams.mActivityTaskManager == null || ams.mWindowManager == null) {
            return null;
        }
        synchronized (ams.mActivityTaskManager.mGlobalLock) {
            activityRecord = ams.mActivityTaskManager.mRootActivityContainer.topRunningActivity();
        }
        return activityRecord;
    }

    public ComponentName getTopActivityComponent(ActivityManagerService ams) {
        ActivityRecord top = getTopRunningActivityLocked(ams);
        if (top != null) {
            return top.mActivityComponent;
        }
        return null;
    }

    private OppoBaseActivityTaskManagerService typeCasting(ActivityTaskManagerService atms) {
        if (atms != null) {
            return (OppoBaseActivityTaskManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityTaskManagerService.class, atms);
        }
        return null;
    }
}
