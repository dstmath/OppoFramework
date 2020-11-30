package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.OppoBaseActivityOptions;
import android.app.ProfilerInfo;
import android.common.OppoFeatureCache;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import com.android.server.am.IColorResourcePreloadManager;
import com.color.util.ColorTypeCastingHelper;

public abstract class OppoBaseActivityStarter {
    IColorActivityStarterEx mColorStarterEx = null;

    /* access modifiers changed from: protected */
    public boolean shouldCorrectActivityInfo(ActivityInfo aInfo, int userId, boolean implicit, String callingPackage) {
        return false;
    }

    /* access modifiers changed from: protected */
    public ActivityInfo getCorrectActivityInfo(ActivityInfo aInfo, int userId, int filterCallingUid, Intent intent, int callingUid, int realCallingUid, ActivityStackSupervisor mSupervisor, String resolvedType, int startFlags, ProfilerInfo profilerInfo) {
        return aInfo;
    }

    /* access modifiers changed from: protected */
    public boolean shouldCorrectResolveInfo(ResolveInfo rInfo, int userId, String callingPackage) {
        return false;
    }

    /* access modifiers changed from: protected */
    public ResolveInfo getCorrectResolveInfo(ResolveInfo rInfo, ActivityStackSupervisor mSupervisor, Intent intent, String resolvedType, int userId, int flags, int callingUid, int realCallingUid, int filterCallingUid) {
        return rInfo;
    }

    /* access modifiers changed from: protected */
    public int checkSpecialApp(int userId, String callingPackage, boolean componentSpecified, Intent intent, RootActivityContainer container) {
        return userId;
    }

    /* access modifiers changed from: protected */
    public boolean skipResumeTargetStack(ActivityRecord reusedActivity, ActivityRecord r, ActivityStack targetStack) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean shouldPreloadApp(ActivityOptions launchOptions, ActivityInfo aInfo, Intent intent) {
        OppoBaseActivityOptions baseLaunchOptions = typeCasting(launchOptions);
        if (baseLaunchOptions == null || !baseLaunchOptions.isRPLaunch() || aInfo == null) {
            return false;
        }
        baseLaunchOptions.setRPLaunch(false);
        OppoFeatureCache.get(IColorResourcePreloadManager.DEFAULT).launchEmptyProcess(aInfo, intent);
        return true;
    }

    static OppoBaseActivityOptions typeCasting(ActivityOptions activityOptions) {
        if (activityOptions != null) {
            return (OppoBaseActivityOptions) ColorTypeCastingHelper.typeCasting(OppoBaseActivityOptions.class, activityOptions);
        }
        return null;
    }
}
