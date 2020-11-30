package com.android.server.wm;

import android.content.Context;

public class ColorDummyActivityTaskManagerServiceEx extends OppoDummyActivityTaskManagerServiceEx implements IColorActivityTaskManagerServiceEx {
    public ColorDummyActivityTaskManagerServiceEx(Context context, ActivityTaskManagerService atms) {
        super(context, atms);
    }

    @Override // com.android.server.wm.IColorActivityTaskManagerServiceEx
    public IColorActivityRecordEx getColorActivityRecordEx(ActivityRecord ar) {
        return new ColorDummyActivityRecordEx(ar);
    }

    @Override // com.android.server.wm.IColorActivityTaskManagerServiceEx
    public IColorActivityStackEx getColorActivityStackEx(ActivityStack stack) {
        return new ColorDummyActivityStackEx(stack);
    }

    @Override // com.android.server.wm.IColorActivityTaskManagerServiceEx
    public IColorActivityStarterEx getColorActivityStarterEx(ActivityStarter starter) {
        return new ColorDummyActivityStarterEx(starter);
    }

    @Override // com.android.server.wm.IColorActivityTaskManagerServiceEx
    public IColorActivityStackSupervisorEx getColorActivityStackSupervisorEx(ActivityStackSupervisor supervisor) {
        return new ColorDummyActivityStackSupervisorEx(supervisor);
    }

    @Override // com.android.server.wm.IColorActivityTaskManagerServiceEx
    public IColorActivityStackSupervisorInner getColorActivityStackSupervisorInner(ActivityStackSupervisor supervisor) {
        return null;
    }

    @Override // com.android.server.wm.IColorActivityTaskManagerServiceEx
    public IColorActivityStackInner getColorActivityStackInner(ActivityStack stack) {
        return null;
    }

    @Override // com.android.server.wm.IColorActivityTaskManagerServiceEx
    public boolean execInterceptWindow(Context context, ActivityRecord r, boolean keyguardLocked, boolean showWhenLocked, boolean dismissKeyguard, boolean showDialog) {
        return false;
    }

    @Override // com.android.server.wm.IColorActivityTaskManagerServiceEx
    public boolean execInterceptWindow(Context context, String packageName, boolean showDialog) {
        return false;
    }

    @Override // com.android.server.wm.IColorActivityTaskManagerServiceEx
    public boolean execInterceptFloatWindow(WindowManagerService ws, Context context, WindowState win, boolean keyguardLocked, boolean showDialog) {
        return false;
    }

    @Override // com.android.server.wm.IColorActivityTaskManagerServiceEx
    public boolean execInterceptDisableKeyguard(Context context, int uid) {
        return false;
    }

    @Override // com.android.server.wm.IColorActivityTaskManagerServiceEx
    public void execResolveScreenOnFlag(ActivityRecord record, boolean turnScreenOn) {
    }

    @Override // com.android.server.wm.IColorActivityTaskManagerServiceEx
    public void execHandleKeyguardGoingAway(boolean keyguardGoingAway) {
    }
}
