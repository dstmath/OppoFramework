package com.android.server.wm;

import com.color.util.ColorTypeCastingHelper;

public class OppoTaskPositioner extends TaskPositioner {
    private OppoBaseTaskPositioner mOppoBaseTaskPositioner = ((OppoBaseTaskPositioner) ColorTypeCastingHelper.typeCasting(OppoBaseTaskPositioner.class, this));

    @Override // com.android.server.wm.TaskPositioner
    public /* bridge */ /* synthetic */ void binderDied() {
        super.binderDied();
    }

    @Override // com.android.server.wm.TaskPositioner
    public /* bridge */ /* synthetic */ String toShortString() {
        return super.toShortString();
    }

    OppoTaskPositioner(WindowManagerService service) {
        super(service);
        OppoBaseWindowManagerService oppoBaseWindowManagerService = (OppoBaseWindowManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseWindowManagerService.class, service);
        OppoBaseTaskPositioner oppoBaseTaskPositioner = this.mOppoBaseTaskPositioner;
        if (oppoBaseTaskPositioner != null && oppoBaseWindowManagerService != null) {
            oppoBaseTaskPositioner.mColorTaskPositionerEx = oppoBaseWindowManagerService.mColorWmsEx.getColorTaskPositionerEx(service);
        }
    }
}
