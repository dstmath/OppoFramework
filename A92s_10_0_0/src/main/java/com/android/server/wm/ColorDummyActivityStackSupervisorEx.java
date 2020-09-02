package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import java.util.List;

public class ColorDummyActivityStackSupervisorEx implements IColorActivityStackSupervisorEx {
    final ActivityStackSupervisor mSupervisor;

    public ColorDummyActivityStackSupervisorEx(ActivityStackSupervisor supervisor) {
        this.mSupervisor = supervisor;
    }

    @Override // com.android.server.wm.IColorActivityStackSupervisorEx
    public void moveFreeformStackToFrontIfNeed(int windowingMode, IColorActivityStackSupervisorInner inner) {
    }

    @Override // com.android.server.wm.IColorActivityStackSupervisorEx
    public boolean isResumeFreeformActivity(ActivityStack targetStack, ActivityRecord target) {
        return false;
    }

    @Override // com.android.server.wm.IColorActivityStackSupervisorEx
    public boolean resumeTopActivityUncheckedLocked(ActivityRecord target, ActivityOptions targetOptions) {
        return false;
    }

    @Override // com.android.server.wm.IColorActivityStackSupervisorEx
    public int startActivityForFreeform(Intent intent, Bundle bOptions, int userId, int callPid, int callUid, String callPkg, IColorActivityStackSupervisorInner inner) {
        return -1;
    }

    @Override // com.android.server.wm.IColorActivityStackSupervisorEx
    public void exitColorosFreeform(Bundle bOptions, IColorActivityStackSupervisorInner inner) {
    }

    @Override // com.android.server.wm.IColorActivityStackSupervisorEx
    public List<String> getAllTopPkgName(IColorActivityStackSupervisorInner inner) {
        return null;
    }

    @Override // com.android.server.wm.IColorActivityStackSupervisorEx
    public ApplicationInfo getFreeFormAppInfo(IColorActivityStackSupervisorInner inner) {
        return null;
    }

    @Override // com.android.server.wm.IColorActivityStackSupervisorEx
    public List<ApplicationInfo> getAllTopAppInfo(IColorActivityStackSupervisorInner inner) {
        return null;
    }

    @Override // com.android.server.wm.IColorActivityStackSupervisorEx
    public void shouldStart(ActivityStack sourceStack, ActivityStack toStack, ActivityRecord r) {
    }
}
