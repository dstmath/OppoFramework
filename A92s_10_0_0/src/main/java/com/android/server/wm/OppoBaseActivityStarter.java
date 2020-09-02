package com.android.server.wm;

import android.app.ProfilerInfo;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;

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
}
