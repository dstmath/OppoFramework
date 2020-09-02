package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.IActivityTaskManager;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.OppoPackageManagerInternal;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.ColorLocalServices;
import com.android.server.LocalServices;
import com.android.server.pm.IColorSecurePayManager;
import java.util.List;

public abstract class OppoBaseActivityTaskManagerService extends IActivityTaskManager.Stub {
    private static final String TAG = "ActivityTaskManager";
    public static boolean mUiModeChanged = false;
    IColorActivityTaskManagerServiceEx mColorAtmsEx = null;
    IColorActivityTaskManagerServiceInner mColorAtmsInner = null;
    IColorFreeformManager mColorFreeformManager = null;
    OppoPackageManagerInternal mOppoPackageManagerInt;
    IPswActivityTaskManagerServiceEx mPswAtmsEx = null;

    /* access modifiers changed from: protected */
    public void onOppoStart() {
        warn("onStart");
    }

    /* access modifiers changed from: protected */
    public ActivityRecord createActivityRecord(ActivityTaskManagerService _service, WindowProcessController _caller, int _launchedFromPid, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityOptions options, ActivityRecord sourceRecord) {
        return new ActivityRecord(_service, _caller, _launchedFromPid, _launchedFromUid, _launchedFromPackage, _intent, _resolvedType, aInfo, _configuration, _resultTo, _resultWho, _reqCode, _componentSpecified, _rootVoiceInteraction, supervisor, options, sourceRecord);
    }

    /* access modifiers changed from: protected */
    public ActivityStack createActivityStack(ActivityDisplay display, int stackId, ActivityStackSupervisor supervisor, int windowingMode, int activityType, boolean onTop) {
        return new ActivityStack(display, stackId, supervisor, windowingMode, activityType, onTop);
    }

    /* access modifiers changed from: protected */
    public ActivityStarter createActivityStarter(ActivityStartController controller, ActivityTaskManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
        return new ActivityStarter(controller, service, supervisor, interceptor);
    }

    /* access modifiers changed from: protected */
    public ActivityStackSupervisor createActivityStackSupervisor(ActivityTaskManagerService service, Looper looper) {
        return new ActivityStackSupervisor(service, looper);
    }

    /* access modifiers changed from: package-private */
    public OppoPackageManagerInternal getOppoPackageManagerInternalLocked() {
        if (this.mOppoPackageManagerInt == null) {
            this.mOppoPackageManagerInt = (OppoPackageManagerInternal) LocalServices.getService(OppoPackageManagerInternal.class);
        }
        return this.mOppoPackageManagerInt;
    }

    /* access modifiers changed from: protected */
    public IColorFreeformManager getColorFreeformManager() {
        if (this.mColorFreeformManager == null) {
            this.mColorFreeformManager = (IColorFreeformManager) ColorLocalServices.getService(IColorFreeformManager.class);
        }
        return this.mColorFreeformManager;
    }

    private final void warn(String methodName) {
        Slog.w(TAG, methodName + " not implemented");
    }

    public List<ApplicationInfo> getAllTopAppInfo() {
        return null;
    }

    public ComponentName getDockTopAppName() {
        return null;
    }

    public int getWindowMode(IBinder token) throws RemoteException {
        return 0;
    }

    public int startActivityForFreeform(Intent intent, Bundle bOptions, int userId, String callPkg) {
        return -1;
    }

    public void exitColorosFreeform(Bundle bOptions) {
    }

    public void startSecurityPayService(ActivityRecord prev, ActivityRecord next) {
        if (next != null) {
            String prevPkg = "";
            String preClsName = "";
            if (prev != null) {
                prevPkg = prev.packageName;
                preClsName = prev.mActivityComponent.getClassName();
            }
            OppoFeatureCache.get(IColorSecurePayManager.DEFAULT).startSecurityPayService(prevPkg, next.packageName, preClsName, next.mActivityComponent.getClassName());
        }
    }

    public boolean shouldIgnore(int changes, String name) {
        return false;
    }

    public void handleExtraConfigurationChanges(int changes, Configuration configuration, Context context, Handler handler, int userId) {
    }

    public void updateExtraConfigurationForUser(Context context, Configuration configuration, int userId) {
    }

    public void updateUserIdInExtraConfiguration(Configuration target, int userId) {
    }

    public void updateBurmeseFontLinkForUser(Configuration values, int userId, Context context) {
    }

    public IColorActivityTaskManagerServiceInner createColorActivityTaskManagerServiceInner() {
        return null;
    }

    public int handleConfigChange(Configuration current, Configuration delta) {
        return -1;
    }

    public void handleUiModeChanged(int changes) {
    }

    public ApplicationInfo getDefaultLauncherInfo() {
        return null;
    }

    public boolean isDefaultOppoLauncher() {
        return false;
    }
}
