package com.android.server.wm;

import android.app.ProfilerInfo;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;

public interface IColorActivityStarterInner {
    default ResolveInfo getResolveInfo(Intent intent, String resolvedType, int userId, int callingPid, int realCallingUid, int filterCallingUid) {
        return null;
    }

    default ActivityInfo getActivityInfo(Intent intent, ResolveInfo rInfo, int startFlags, ProfilerInfo profilerInfo) {
        return null;
    }
}
