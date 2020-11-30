package com.android.server.wm;

import com.android.server.am.IColorActivityManagerServiceEx;
import com.android.server.wm.ActivityStack;

public class ColorDummyFreeformManager implements IColorFreeformManager {
    @Override // com.android.server.wm.IColorFreeformManager
    public void init(IColorActivityManagerServiceEx amsEx, IColorWindowManagerServiceEx wmsEx) {
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public boolean isSupportFreeform() {
        return false;
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public boolean inFullscreenCpnList(String cpn) {
        return false;
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public boolean inNextNeedFullscreenCpnList(String cpn) {
        return false;
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public void resetParentInfo() {
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public void setParentInfo(ActivityStack stack) {
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public void handleFreeformDied(boolean isFreeform, ActivityRecord r) {
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public void handleParentDied(int pid) {
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public ActivityStack.ActivityState getParentState() {
        return null;
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public void handleApplicationSwitch(String prePkgName, String nextPkgName, String prevActivity, String nextActivity, boolean isPreMultiApp, boolean isNextMultiApp, boolean isPreForFreeForm, boolean isNextForFreeForm) {
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public boolean exitFreeformIfNeed(ActivityStack targetStack) {
        return false;
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public boolean skipPauseBackStackIfNeed(ActivityStack stack, ActivityRecord resuming) {
        return false;
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public void oppoStartFreezingDisplayLocked() {
    }

    @Override // com.android.server.wm.IColorFreeformManager
    public void oppoStopFreezingDisplayLocked() {
    }
}
