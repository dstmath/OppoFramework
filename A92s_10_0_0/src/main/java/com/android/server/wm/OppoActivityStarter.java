package com.android.server.wm;

import android.app.ProfilerInfo;
import android.common.OppoFeatureCache;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.util.Slog;
import com.android.server.am.IColorMultiAppManager;
import com.color.util.ColorTypeCastingHelper;

public class OppoActivityStarter extends ActivityStarter {
    private static final String TAG = "OppoActivityStarter";
    private OppoBaseActivityStarter mBase;

    OppoActivityStarter(ActivityStartController controller, ActivityTaskManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
        super(controller, service, supervisor, interceptor);
        OppoBaseActivityTaskManagerService baseAtms = (OppoBaseActivityTaskManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityTaskManagerService.class, service);
        if (baseAtms != null) {
            this.mBase = (OppoBaseActivityStarter) ColorTypeCastingHelper.typeCasting(OppoBaseActivityStarter.class, this);
            OppoBaseActivityStarter oppoBaseActivityStarter = this.mBase;
            if (oppoBaseActivityStarter != null) {
                oppoBaseActivityStarter.mColorStarterEx = baseAtms.mColorAtmsEx.getColorActivityStarterEx(this);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseActivityStarter
    public boolean shouldCorrectActivityInfo(ActivityInfo aInfo, int userId, boolean implicit, String callingPackage) {
        if (callingPackage == null || !OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiAppUserId(userId) || !OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiApp(callingPackage)) {
            return false;
        }
        if (aInfo == null) {
            return true;
        }
        if (!implicit || aInfo.packageName == null || aInfo.packageName.equals(callingPackage) || aInfo.packageName.equals("com.android.permissioncontroller") || aInfo.packageName.equals("com.google.android.permissioncontroller")) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseActivityStarter
    public ActivityInfo getCorrectActivityInfo(ActivityInfo aInfo, int userId, int filterCallingUid, Intent intent, int callingUid, int realCallingUid, ActivityStackSupervisor mSupervisor, String resolvedType, int startFlags, ProfilerInfo profilerInfo) {
        ActivityInfo aInfo2 = mSupervisor.resolveActivity(intent, resolvedType, startFlags, profilerInfo, userId, computeResolveFilterUid(callingUid, realCallingUid, filterCallingUid));
        Slog.i(TAG, "multi app: startActivityMayWait change userId to " + userId + " aInfo = " + aInfo2 + " originInfo = " + aInfo);
        return aInfo2;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseActivityStarter
    public boolean shouldCorrectResolveInfo(ResolveInfo rInfo, int userId, String callingPackage) {
        if (rInfo != null || callingPackage == null || !OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiAppUserId(userId) || !OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiApp(callingPackage)) {
            return false;
        }
        return true;
    }

    private int getChildPackageUserIdIfExist(ActivityDisplay display, String pkgName, int userId) {
        if (display == null || pkgName == null) {
            return userId;
        }
        boolean matchTask = false;
        for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ActivityStack stack = display.getChildAt(stackNdx);
            if (stack != null) {
                int taskNdx = stack.getChildCount() - 1;
                while (true) {
                    if (taskNdx < 0) {
                        break;
                    }
                    TaskRecord task = stack.getChildAt(taskNdx);
                    if (task != null && pkgName.equals(task.affinity)) {
                        matchTask = true;
                        userId = task.userId;
                        break;
                    }
                    taskNdx--;
                }
                if (matchTask) {
                    break;
                }
            }
        }
        Slog.i(TAG, "matchTask userId = " + userId + " matchTask = " + matchTask);
        return userId;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseActivityStarter
    public int checkSpecialApp(int userId, String callingPackage, boolean componentSpecified, Intent intent, RootActivityContainer container) {
        if (intent == null || container == null) {
            return userId;
        }
        if ("com.tencent.mm".equals(callingPackage) && componentSpecified && intent.getComponent().getClassName() != null && "com.sina.weibo.wxapi.WXEntryActivity".equals(intent.getComponent().getClassName())) {
            userId = getChildPackageUserIdIfExist(container.getDefaultDisplay(), "com.sina.weibo", userId);
        }
        if (!"com.eg.android.AlipayGphone".equals(callingPackage) || !componentSpecified || intent.getComponent().getClassName() == null) {
            return userId;
        }
        if ("com.taobao.login4android.activity.AlipaySSOResultActivity".equals(intent.getComponent().getClassName()) || "com.taobao.taobao.apshare.ShareEntryActivity".equals(intent.getComponent().getClassName())) {
            return getChildPackageUserIdIfExist(container.getDefaultDisplay(), "com.taobao.taobao", userId);
        }
        return userId;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseActivityStarter
    public ResolveInfo getCorrectResolveInfo(ResolveInfo rInfo, ActivityStackSupervisor mSupervisor, Intent intent, String resolvedType, int userId, int flags, int callingUid, int realCallingUid, int filterCallingUid) {
        ResolveInfo rInfo2 = mSupervisor.resolveIntent(intent, resolvedType, userId, flags, computeResolveFilterUid(callingUid, realCallingUid, filterCallingUid));
        Slog.i(TAG, "multi app: startActivityMayWait change userId to " + userId + " rInfo = " + rInfo2);
        return rInfo2;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseActivityStarter
    public boolean skipResumeTargetStack(ActivityRecord reusedActivity, ActivityRecord r, ActivityStack targetStack) {
        boolean skip = false;
        ActivityRecord topActivity = null;
        if (!(reusedActivity == null || reusedActivity.getTaskRecord() == null)) {
            topActivity = reusedActivity.getTaskRecord().getTopActivity();
        }
        if (topActivity != null) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STACK || ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.i(TAG, "topActivity.packageName = " + topActivity.packageName);
            }
            if (topActivity.packageName != null && r.packageName != null && r.packageName.equals("com.coloros.safecenter") && !r.packageName.equals(topActivity.packageName) && !topActivity.packageName.equals("com.coloros.securitypermission")) {
                targetStack.destroyActivityLocked(topActivity, true, "secure protect");
                OppoBaseActivityStack base = (OppoBaseActivityStack) ColorTypeCastingHelper.typeCasting(OppoBaseActivityStack.class, targetStack);
                if (!(base == null || base.mColorStackInner == null)) {
                    base.mColorStackInner.removeActivityFromHistoryLocked(topActivity, "secure protect");
                }
                if (!(reusedActivity == null || reusedActivity.getTaskRecord() == null || reusedActivity.getTaskRecord().mActivities == null || !reusedActivity.getTaskRecord().mActivities.isEmpty())) {
                    targetStack.removeTask(reusedActivity.getTaskRecord(), "secure protect", 0);
                }
                skip = true;
                if (ActivityTaskManagerDebugConfig.DEBUG_STACK || ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.i(TAG, "secure protect remove!  r.packageName = " + r.packageName + "  topActivity.packageName = " + topActivity.packageName);
                }
            }
        }
        return skip;
    }
}
