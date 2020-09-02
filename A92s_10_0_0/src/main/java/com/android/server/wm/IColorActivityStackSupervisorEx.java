package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import java.util.List;

public interface IColorActivityStackSupervisorEx {
    void exitColorosFreeform(Bundle bundle, IColorActivityStackSupervisorInner iColorActivityStackSupervisorInner);

    List<ApplicationInfo> getAllTopAppInfo(IColorActivityStackSupervisorInner iColorActivityStackSupervisorInner);

    List<String> getAllTopPkgName(IColorActivityStackSupervisorInner iColorActivityStackSupervisorInner);

    ApplicationInfo getFreeFormAppInfo(IColorActivityStackSupervisorInner iColorActivityStackSupervisorInner);

    boolean isResumeFreeformActivity(ActivityStack activityStack, ActivityRecord activityRecord);

    void moveFreeformStackToFrontIfNeed(int i, IColorActivityStackSupervisorInner iColorActivityStackSupervisorInner);

    boolean resumeTopActivityUncheckedLocked(ActivityRecord activityRecord, ActivityOptions activityOptions);

    void shouldStart(ActivityStack activityStack, ActivityStack activityStack2, ActivityRecord activityRecord);

    int startActivityForFreeform(Intent intent, Bundle bundle, int i, int i2, int i3, String str, IColorActivityStackSupervisorInner iColorActivityStackSupervisorInner);
}
