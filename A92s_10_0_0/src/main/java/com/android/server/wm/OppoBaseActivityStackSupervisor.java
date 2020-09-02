package com.android.server.wm;

import android.app.IApplicationThread;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Intent;
import android.content.OppoBaseIntent;
import android.os.Binder;
import com.android.server.pm.OppoPackageManagerHelper;
import com.color.util.ColorTypeCastingHelper;

public abstract class OppoBaseActivityStackSupervisor {
    public IColorActivityStackSupervisorEx mColorSupervisorEx = null;
    public IColorActivityStackSupervisorInner mColorSupervisorInner = null;
    protected ActivityRecord mResumeLostActivity;

    /* access modifiers changed from: protected */
    public boolean isOppoSafeActivity() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void resetOppoSafeActivity(ActivityRecord r, String reason) {
    }

    /* access modifiers changed from: protected */
    public void setOppoCallingUid(Intent intent) {
        int oppoRealCallingUid = Binder.getCallingUid();
        try {
            OppoBaseIntent oBaseIntent = (OppoBaseIntent) ColorTypeCastingHelper.typeCasting(OppoBaseIntent.class, intent);
            if (oBaseIntent != null) {
                oBaseIntent.setCallingUid(oppoRealCallingUid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public void dataCollectionInfo(IApplicationThread caller, WindowProcessController callerApp, int pid, int uid, String callingPackage, Intent intent) {
        ComponentName realActivity;
        if (intent != null && intent.getComponent() != null && (realActivity = intent.getComponent()) != null) {
            if ("com.android.packageinstaller.PackageInstallerActivity".equals(realActivity.getClassName()) || "com.android.packageinstaller.UninstallerActivity".equals(realActivity.getClassName())) {
                intent.putExtra(OppoPackageManagerHelper.OPPO_EXTRA_PID, callerApp != null ? callerApp.getPid() : pid);
                intent.putExtra(OppoPackageManagerHelper.OPPO_EXTRA_UID, callerApp != null ? callerApp.mInfo.uid : uid);
                intent.putExtra(OppoPackageManagerHelper.OPPO_EXTRA_PKG_NAME, callingPackage != null ? callingPackage : "");
                StringBuffer stringBuffer = new StringBuffer("dataCollection debug info ");
                if (caller == null) {
                    stringBuffer.append(" caller is null,");
                }
                if (callerApp == null) {
                    stringBuffer.append(" callerApp is null");
                }
                stringBuffer.append(" pid ");
                stringBuffer.append(pid);
                stringBuffer.append(" uid ");
                stringBuffer.append(uid);
                stringBuffer.append(" callingPackage ");
                stringBuffer.append(callingPackage);
                intent.putExtra(OppoPackageManagerHelper.OPPO_EXTRA_DEBUG_INFO, stringBuffer.toString());
            }
        }
    }

    public ComponentName getDockTopAppName() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public void collectionStartUrlInfo(IApplicationThread caller, WindowProcessController callerApp, int pid, int uid, String callingPackage, Intent intent) {
    }

    public IColorActivityStackSupervisorInner createColorActivityStackSupervisorInner() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void notifyAppSwitch(ActivityRecord resumeGainActivity, ActivityTaskManagerService atms, boolean userLeaving) {
        if (resumeGainActivity != null) {
            if (this.mResumeLostActivity != null) {
                ColorAppSwitchManagerService.getInstance().handleActivityPaused(this.mResumeLostActivity, resumeGainActivity);
                OppoFeatureCache.get(IColorAppSwitchManager.DEFAULT).handleActivitySwitch(atms.mContext, this.mResumeLostActivity, resumeGainActivity, userLeaving);
            }
            ColorAppSwitchManagerService.getInstance().handleActivityResumed(resumeGainActivity);
            this.mResumeLostActivity = resumeGainActivity;
        }
    }
}
