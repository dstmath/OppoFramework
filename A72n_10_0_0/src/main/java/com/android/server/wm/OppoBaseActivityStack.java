package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.text.format.Time;
import com.android.server.am.IColorMultiAppManager;
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
    ComponentName mLastRecordCmpNameForUsage = null;
    String mLastRecordPkgNameForUsage = null;
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
        boolean isSkip = true;
        if (activityRecord != null) {
            isSkip = activityRecord.mUserId != userId;
        }
        if ((taskRecord == null || !OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiApp(taskRecord.affinity)) && (activityRecord == null || !OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiApp(activityRecord.packageName))) {
            return isSkip;
        }
        return false;
    }

    public IColorActivityStackInner createColorActivityStackInner() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean moveTaskToBackForSplitScreenMode() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public int getWindowModeForHans(ActivityRecord next) {
        if (next == null) {
            return -1;
        }
        if (next.getTaskRecord() == null || next.getTaskRecord().getWindowingMode() != ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            return next.getWindowingMode();
        }
        return ColorZoomWindowManager.WINDOWING_MODE_ZOOM;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void setWindowingMode(int windowingMode) {
        if (getResolvedOverrideConfiguration().windowConfiguration.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            getRequestedOverrideConfiguration().orientation = 0;
        }
        super.setWindowingMode(windowingMode);
    }

    /* access modifiers changed from: package-private */
    public UsageRecorderRunnable saveAppUsage(ComponentName componentName) {
        ComponentName componentName2 = this.mLastRecordCmpNameForUsage;
        if (componentName2 == null || !componentName2.equals(componentName)) {
            this.mLastRecordCmpNameForUsage = componentName;
            ComponentName componentName3 = this.mLastRecordCmpNameForUsage;
            String pkgName = componentName3 != null ? componentName3.getPackageName() : null;
            if (pkgName != null && !pkgName.equals(this.mLastRecordPkgNameForUsage)) {
                this.mLastRecordPkgNameForUsage = pkgName;
                Time tobj = new Time();
                tobj.set(System.currentTimeMillis());
                return new UsageRecorderRunnable(pkgName, tobj.format("%Y-%m-%d %H:%M:%S"));
            }
        }
        return null;
    }
}
