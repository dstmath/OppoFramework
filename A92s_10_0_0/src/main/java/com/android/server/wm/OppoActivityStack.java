package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.util.Slog;
import com.android.server.am.IColorMultiAppManager;
import com.color.util.ColorTypeCastingHelper;

public class OppoActivityStack extends ActivityStack {
    private static final String TAG = "OppoActivityStack";
    private OppoBaseActivityStack mOppoBaseActivityStack = ((OppoBaseActivityStack) ColorTypeCastingHelper.typeCasting(OppoBaseActivityStack.class, this));

    OppoActivityStack(ActivityDisplay display, int stackId, ActivityStackSupervisor supervisor, int windowingMode, int activityType, boolean onTop) {
        super(display, stackId, supervisor, windowingMode, activityType, onTop);
        Slog.i(TAG, "create OppoActivityStack");
        OppoBaseActivityTaskManagerService oBatms = (OppoBaseActivityTaskManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityTaskManagerService.class, supervisor.mService);
        if (this.mOppoBaseActivityStack != null && oBatms != null && oBatms.mColorAtmsEx != null) {
            this.mOppoBaseActivityStack.mColorStackEx = oBatms.mColorAtmsEx.getColorActivityStackEx(this);
            OppoBaseActivityStack oppoBaseActivityStack = this.mOppoBaseActivityStack;
            oppoBaseActivityStack.mColorStackInner = oppoBaseActivityStack.createColorActivityStackInner();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.OppoBaseActivityStack
    public boolean shouldResetTask(TaskRecord targetTask, TaskRecord task) {
        if (targetTask == null || task == null || targetTask.userId == task.userId || targetTask.realActivity == null || !OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isMultiApp(targetTask.realActivity.getPackageName())) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.wm.OppoBaseActivityStack
    public boolean moveTaskToBackForSplitScreenMode() {
        if (getWindowingMode() != 3) {
            return false;
        }
        Slog.i(TAG, "moveTaskToBackForSplitScreenMode stack: " + this);
        try {
            this.mWindowManager.startFreezingScreen(0, 0);
            this.mService.dismissSplitScreenMode(false);
        } catch (Exception e) {
            Slog.d(TAG, "split screen startFreezingScreen error:" + e);
        } catch (Throwable th) {
            this.mWindowManager.mClientFreezingScreen = false;
            this.mWindowManager.mAppsFreezingScreen = 0;
            this.mWindowManager.mWindowsFreezingScreen = 0;
            this.mWindowManager.stopFreezingScreen();
            throw th;
        }
        this.mWindowManager.mClientFreezingScreen = false;
        this.mWindowManager.mAppsFreezingScreen = 0;
        this.mWindowManager.mWindowsFreezingScreen = 0;
        this.mWindowManager.stopFreezingScreen();
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ActivityStack
    public void moveToBack(String reason, TaskRecord task) {
        super.moveToBack(reason, task);
        if (getWindowingMode() == 4) {
            adjustFocusToNextFocusableStack("moveTaskToBackSplitScreenSecondary");
        }
    }
}
