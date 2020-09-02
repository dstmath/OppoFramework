package com.android.server.wm;

import android.content.ComponentName;
import com.color.zoomwindow.ColorZoomWindowManager;

public abstract class OppoBaseActivityStack extends ConfigurationContainer {
    static final String SWAP_DOCKED_STACK = "swapDockedAndFullscreenStack";
    IColorActivityStackEx mColorStackEx = null;
    IColorActivityStackInner mColorStackInner = null;
    ComponentName mComponentName;
    ComponentName mDockComponentName;
    protected boolean mHasRunningActivity = false;
    boolean mIsClearTask = false;
    boolean mIsSwapTask = false;
    boolean mStackShown;
    boolean mStackVisibleChange;
    boolean mWindowModeHide;
    boolean mZoomBubble;
    String mZoomPkg;
    int mZoomUserId;

    public ComponentName getTopAppName() {
        return this.mComponentName;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldResetTask(TaskRecord targetTask, TaskRecord task) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldSkipMultiAppUser(TaskRecord taskRecord, ActivityRecord activityRecord, int userId) {
        return activityRecord == null || activityRecord.mUserId != userId;
    }

    public IColorActivityStackInner createColorActivityStackInner() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean moveTaskToBackForSplitScreenMode() {
        return false;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void setWindowingMode(int windowingMode) {
        if (getResolvedOverrideConfiguration().windowConfiguration.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            getRequestedOverrideConfiguration().orientation = 0;
        }
        super.setWindowingMode(windowingMode);
    }
}
