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
}
