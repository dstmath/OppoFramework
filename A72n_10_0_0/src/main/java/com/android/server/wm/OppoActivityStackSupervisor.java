package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.IApplicationThread;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Looper;
import android.util.Slog;
import com.color.util.ColorTypeCastingHelper;

public class OppoActivityStackSupervisor extends ActivityStackSupervisor {
    private static final String TAG = "OppoActivityStackSupervisor";
    final String ACTION_OPPO_SAFE_COUNT_START_URL = "oppo.intent.action.OPPO_SAFE_COUNT_START_URL";
    private OppoBaseActivityStackSupervisor mOppoBaseActivityStackSupervisor;

    public OppoActivityStackSupervisor(ActivityTaskManagerService service, Looper looper) {
        super(service, looper);
        Slog.i(TAG, "create OppoActivityStackSupervisor");
        this.mOppoBaseActivityStackSupervisor = (OppoBaseActivityStackSupervisor) ColorTypeCastingHelper.typeCasting(OppoBaseActivityStackSupervisor.class, this);
        OppoBaseActivityTaskManagerService oppoBaseActivityTaskManagerService = (OppoBaseActivityTaskManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityTaskManagerService.class, service);
        OppoBaseActivityStackSupervisor oppoBaseActivityStackSupervisor = this.mOppoBaseActivityStackSupervisor;
        if (oppoBaseActivityStackSupervisor != null && oppoBaseActivityTaskManagerService != null) {
            oppoBaseActivityStackSupervisor.mColorSupervisorEx = oppoBaseActivityTaskManagerService.mColorAtmsEx.getColorActivityStackSupervisorEx(this);
            OppoBaseActivityStackSupervisor oppoBaseActivityStackSupervisor2 = this.mOppoBaseActivityStackSupervisor;
            oppoBaseActivityStackSupervisor2.mColorSupervisorInner = oppoBaseActivityStackSupervisor2.createColorActivityStackSupervisorInner();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.OppoBaseActivityStackSupervisor
    public void collectionStartUrlInfo(IApplicationThread caller, WindowProcessController callerApp, int pid, int uid, String callingPackage, Intent intent) {
        String cpnClassName;
        String cpnPkgName;
        boolean isFore;
        if (intent != null) {
            String action = intent.getAction();
            String url = intent.getDataString();
            if (action != null && action.equals("android.intent.action.VIEW") && url != null && url.contains("http")) {
                ComponentName cpn = intent.getComponent();
                if (cpn != null) {
                    cpnPkgName = cpn.getPackageName();
                    cpnClassName = cpn.getClassName();
                } else {
                    cpnPkgName = "";
                    cpnClassName = "";
                }
                if (callingPackage == null || !callingPackage.equals(getTopPackageName())) {
                    isFore = false;
                } else {
                    isFore = true;
                }
                if (callingPackage != null && !callingPackage.equals(cpnPkgName)) {
                    sendBroadcastForStartUrlInfo(callingPackage, url, cpnPkgName, cpnClassName, isFore);
                }
            }
        }
    }

    private void sendBroadcastForStartUrlInfo(String callingPkg, String url, String pkgName, String className, boolean isFore) {
        Slog.d(TAG, "sendBroadcastForStartUrlInfo callingPkg = " + callingPkg + "  url = " + url + "  pkgName = " + pkgName + "  className= " + className + "  isFore = " + isFore);
        Intent intent = new Intent("oppo.intent.action.OPPO_SAFE_COUNT_START_URL");
        intent.putExtra("caller", callingPkg);
        intent.putExtra("url", url);
        intent.putExtra("pkgName", pkgName);
        intent.putExtra("className", className);
        intent.putExtra("isFore", isFore);
        this.mService.mContext.sendBroadcast(intent);
    }

    private String getTopPackageName() {
        OppoBaseActivityStack oppoBaseActivityStack = (OppoBaseActivityStack) ColorTypeCastingHelper.typeCasting(OppoBaseActivityStack.class, this.mRootActivityContainer.getDefaultDisplay().getFocusedStack());
        ComponentName topCpn = null;
        if (oppoBaseActivityStack != null) {
            topCpn = oppoBaseActivityStack.mComponentName;
        }
        if (topCpn != null) {
            return topCpn.getPackageName();
        }
        return "";
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ActivityStackSupervisor
    public int startActivityFromRecents(int callingPid, int callingUid, int taskId, SafeActivityOptions options) {
        ActivityOptions activityOptions;
        TaskRecord detectTask = this.mRootActivityContainer.anyTaskForId(taskId, 0);
        OppoBaseActivityTaskManagerService oppoBaseActivityTaskManagerService = (OppoBaseActivityTaskManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityTaskManagerService.class, this.mService);
        int windowingMode = 0;
        if (options != null) {
            activityOptions = options.getOptions(this);
        } else {
            activityOptions = null;
        }
        if (activityOptions != null) {
            windowingMode = activityOptions.getLaunchWindowingMode();
        }
        if (windowingMode != 3 || detectTask == null || !OppoFeatureCache.get(IColorSplitWindowManager.DEFAULT).isInForbidActivityList(detectTask)) {
            return super.startActivityFromRecents(callingPid, callingUid, taskId, options);
        }
        Slog.d(TAG, "startActivityFromRecents: Task " + taskId + " topRunningActivity in ForbidActivityList.");
        return -96;
    }
}
