package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.IApplicationThread;
import android.app.OppoThemeHelper;
import android.common.ColorFrameworkFactory;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManagerInternal;
import android.content.res.ColorBaseConfiguration;
import android.content.res.Configuration;
import android.content.res.IColorThemeManager;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import android.view.IColorBurmeseZgHooks;
import com.android.server.ColorServiceFactory;
import com.android.server.PswServiceFactory;
import com.android.server.UiThread;
import com.android.server.pm.DumpState;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.color.font.IColorFontManager;
import com.color.util.ColorTypeCastingHelper;
import java.io.File;
import java.util.List;

public class OppoActivityTaskManagerService extends ActivityTaskManagerService {
    private static final String TAG = "OppoActivityTaskManagerService";
    private final int ICON_CONFIG_VERSION_BIT = 60;
    private final int ICON_RADIUS_BIT_LEFT = 16;
    private final int ICON_RADIUS_BIT_RIGHT = 48;
    private final int MAX_ICON_RADIUS = 75;
    private final String THEME_ICONS = "data/theme/icons";
    private OppoBaseActivityTaskManagerService mBase = ((OppoBaseActivityTaskManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityTaskManagerService.class, this));
    ThreadHandler mThreadHandler;

    public OppoActivityTaskManagerService(Context context) {
        super(context);
        OppoBaseActivityTaskManagerService oppoBaseActivityTaskManagerService = this.mBase;
        if (oppoBaseActivityTaskManagerService != null) {
            oppoBaseActivityTaskManagerService.mColorAtmsEx = ColorServiceFactory.getInstance().getFeature(IColorActivityTaskManagerServiceEx.DEFAULT, new Object[]{context, this});
            this.mBase.mPswAtmsEx = PswServiceFactory.getInstance().getFeature(IPswActivityTaskManagerServiceEx.DEFAULT, new Object[]{context, this});
        }
        this.mThreadHandler = new ThreadHandler();
    }

    final class ThreadHandler extends Handler {
        static final int SKIN_CHANGE_MSG = 10;

        public ThreadHandler() {
            super(UiThread.get().getLooper(), null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                OppoActivityTaskManagerService.this.mAmInternal.broadcastIntentInPackage((String) null, 1000, Binder.getCallingUid(), Binder.getCallingPid(), new Intent("oppo.intent.action.SKIN_CHANGED"), (String) null, (IIntentReceiver) null, 0, (String) null, (Bundle) null, (String) null, (Bundle) null, false, false, ((Integer) msg.obj).intValue(), false);
            }
        }
    }

    @Override // com.android.server.wm.ActivityTaskManagerService
    public void onSystemReady() {
        super.onSystemReady();
        OppoBaseActivityTaskManagerService oppoBaseActivityTaskManagerService = this.mBase;
        if (oppoBaseActivityTaskManagerService != null) {
            if (oppoBaseActivityTaskManagerService.mColorAtmsEx != null) {
                this.mBase.mColorAtmsEx.systemReady();
            }
            if (this.mBase.mPswAtmsEx != null) {
                this.mBase.mPswAtmsEx.systemReady();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public void onOppoStart() {
        OppoBaseActivityTaskManagerService oppoBaseActivityTaskManagerService = this.mBase;
        if (oppoBaseActivityTaskManagerService != null) {
            if (oppoBaseActivityTaskManagerService.mColorAtmsEx != null) {
                this.mBase.mColorAtmsEx.onStart();
            }
            if (this.mBase.mPswAtmsEx != null) {
                this.mBase.mPswAtmsEx.onStart();
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (super.onTransact(code, data, reply, flags)) {
            return true;
        }
        OppoBaseActivityTaskManagerService oppoBaseActivityTaskManagerService = this.mBase;
        if (oppoBaseActivityTaskManagerService == null) {
            return false;
        }
        if (oppoBaseActivityTaskManagerService.mColorAtmsEx != null && this.mBase.mColorAtmsEx.onTransact(code, data, reply, flags)) {
            return true;
        }
        if (this.mBase.mPswAtmsEx == null || !this.mBase.mPswAtmsEx.onTransact(code, data, reply, flags)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public ActivityRecord createActivityRecord(ActivityTaskManagerService _service, WindowProcessController _caller, int _launchedFromPid, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityOptions options, ActivityRecord sourceRecord) {
        return new ActivityRecord(_service, _caller, _launchedFromPid, _launchedFromUid, _launchedFromPackage, _intent, _resolvedType, aInfo, _configuration, _resultTo, _resultWho, _reqCode, _componentSpecified, _rootVoiceInteraction, supervisor, options, sourceRecord);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public ActivityStack createActivityStack(ActivityDisplay display, int stackId, ActivityStackSupervisor supervisor, int windowingMode, int activityType, boolean onTop) {
        return new OppoActivityStack(display, stackId, supervisor, windowingMode, activityType, onTop);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public ActivityStarter createActivityStarter(ActivityStartController controller, ActivityTaskManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
        return new OppoActivityStarter(controller, service, supervisor, interceptor);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public ActivityStackSupervisor createActivityStackSupervisor(ActivityTaskManagerService service, Looper looper) {
        return new OppoActivityStackSupervisor(service, looper);
    }

    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public List<ApplicationInfo> getAllTopAppInfo() {
        OppoBaseActivityStackSupervisor baseSupervisor = typeCasting(this.mStackSupervisor);
        if (baseSupervisor == null || baseSupervisor.mColorSupervisorEx == null) {
            return null;
        }
        return baseSupervisor.mColorSupervisorEx.getAllTopAppInfo(baseSupervisor.mColorSupervisorInner);
    }

    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public ComponentName getDockTopAppName() {
        OppoBaseActivityStackSupervisor baseSupervisor = typeCasting(this.mStackSupervisor);
        if (baseSupervisor != null) {
            return baseSupervisor.getDockTopAppName();
        }
        return null;
    }

    @Override // com.android.server.wm.ActivityTaskManagerService
    public void setRequestedOrientation(IBinder token, int requestedOrientation) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(5, Binder.getCallingUid());
        super.setRequestedOrientation(token, requestedOrientation);
    }

    @Override // com.android.server.wm.ActivityTaskManagerService
    public boolean navigateUpTo(IBinder token, Intent destIntent, int resultCode, Intent resultData) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(1, Binder.getCallingUid());
        return super.navigateUpTo(token, destIntent, resultCode, resultData);
    }

    @Override // com.android.server.wm.ActivityTaskManagerService
    public void moveTaskToFront(IApplicationThread appThread, String callingPackage, int taskId, int flags, Bundle bOptions) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(3, Binder.getCallingUid());
        super.moveTaskToFront(appThread, callingPackage, taskId, flags, bOptions);
    }

    @Override // com.android.server.wm.ActivityTaskManagerService
    public List<IBinder> getAppTasks(String callingPackage) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(78, Binder.getCallingUid());
        return super.getAppTasks(callingPackage);
    }

    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public int getWindowMode(IBinder token) throws RemoteException {
        synchronized (this) {
            ActivityStack stack = ActivityRecord.getStackLocked(token);
            if (stack == null) {
                return -1;
            }
            int windowingMode = stack.getWindowingMode();
            return windowingMode;
        }
    }

    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public boolean shouldIgnore(int changes, String name) {
        return false;
    }

    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public void handleExtraConfigurationChanges(int changes, Configuration configuration, Context context, Handler handler, int userId) {
        ContentResolver resolver = context.getContentResolver();
        ColorBaseConfiguration baseConfig = typeCasting(configuration);
        if (baseConfig == null) {
            Slog.i(TAG, "handleExtraConfigurationChanges config can not cating to base");
            return;
        }
        if ((134217728 & changes) != 0) {
            String strThemeFlag = Long.toString(baseConfig.mOppoExtraConfiguration.mThemeChangedFlags);
            SystemProperties.set(getThemeKeyForUser(userId), strThemeFlag);
            ThreadHandler threadHandler = this.mThreadHandler;
            if (threadHandler != null) {
                threadHandler.obtainMessage(10, Integer.valueOf(userId)).sendToTarget();
            }
            if (OppoActivityTaskManagerDebugConfig.DEBUG_AMS) {
                Slog.d(TAG, "skin change: " + strThemeFlag + ", userId: " + userId);
            }
        }
        if ((67108864 & changes) != 0) {
            int accessChange = baseConfig.mOppoExtraConfiguration.mAccessibleChanged;
            Settings.System.putIntForUser(resolver, "access_color_setting", accessChange, userId);
            if (OppoActivityTaskManagerDebugConfig.DEBUG_AMS) {
                Slog.d(TAG, "color access mode change: " + accessChange + ", userId: " + userId);
            }
        }
        baseConfig.mOppoExtraConfiguration.mUserId = userId;
        if ((Integer.MIN_VALUE & changes) != 0) {
            long uxiconConfig = baseConfig.mOppoExtraConfiguration.mUxIconConfig;
            OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0]).setIconConfigToSettings(resolver, uxiconConfig, userId);
            ThreadHandler threadHandler2 = this.mThreadHandler;
            if (threadHandler2 != null) {
                threadHandler2.obtainMessage(10, Integer.valueOf(userId)).sendToTarget();
            }
            if (OppoActivityTaskManagerDebugConfig.DEBUG_AMS) {
                Slog.d(TAG, "color uxicons config change: " + uxiconConfig + ", userId: " + userId);
            }
        }
        OppoThemeHelper.handleExtraConfigurationChanges(changes, configuration, context, handler);
    }

    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public void updateExtraConfigurationForUser(Context context, Configuration target, int userId) {
        ContentResolver resolver = context.getContentResolver();
        ColorBaseConfiguration baseConfig = typeCasting(target);
        int i = -1;
        if (baseConfig != null) {
            baseConfig.mOppoExtraConfiguration.mUxIconConfig = OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0]).getIconConfigFromSettings(resolver, context, userId);
            calculateIconFlag(context, baseConfig);
            baseConfig.mOppoExtraConfiguration.mAccessibleChanged = Settings.System.getIntForUser(resolver, "access_color_setting", -1, userId);
            baseConfig.mOppoExtraConfiguration.mThemeChangedFlags = resolveThemeFlag(userId);
            baseConfig.mOppoExtraConfiguration.mThemeChanged++;
            baseConfig.mOppoExtraConfiguration.mUserId = userId;
            if (OppoActivityTaskManagerDebugConfig.DEBUG_AMS) {
                Slog.d(TAG, "updateExtraConfigurationForUser: " + baseConfig.mOppoExtraConfiguration + ", userId: " + userId);
            }
            baseConfig.mOppoExtraConfiguration.mFontUserId = userId;
        }
        OppoFeatureCache.getOrCreate(IColorFontManager.DEFAULT, new Object[0]).setFlipFontWhenUserChange(target, 536870912);
        if (OppoActivityTaskManagerDebugConfig.DEBUG_AMS) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateExtraConfigurationForUser set mFontUserId = ");
            if (baseConfig != null) {
                i = baseConfig.mOppoExtraConfiguration.mFontUserId;
            }
            sb.append(i);
            Slog.d(TAG, sb.toString());
        }
    }

    private void calculateIconFlag(Context context, ColorBaseConfiguration baseConfig) {
        Long iconflag = Long.valueOf(baseConfig.mOppoExtraConfiguration.mUxIconConfig);
        int isRadiusInPixel = ((int) (iconflag.longValue() >> 60)) & 1;
        if (isRadiusInPixel == 0) {
            Long iconflag2 = Long.valueOf(Long.valueOf(iconflag.longValue() << 16).longValue() >> 16);
            int iconRadius = (int) ((((((float) (((int) (iconflag.longValue() >> 48)) & 4095)) * 1.0f) / 100.0f) * context.getResources().getDisplayMetrics().density) + 0.5f);
            if (iconRadius > 75) {
                iconRadius = 75;
            }
            Long isRadiusPixel = Long.valueOf(((long) isRadiusInPixel) + 1);
            Long radius = Long.valueOf(Long.valueOf((long) iconRadius).longValue() << 48);
            Long iconflag3 = Long.valueOf(Long.valueOf(isRadiusPixel.longValue() << 60).longValue() | Long.valueOf(radius.longValue() | iconflag2.longValue()).longValue());
            baseConfig.mOppoExtraConfiguration.mUxIconConfig = iconflag3.longValue();
        }
    }

    private long resolveThemeFlag(int userId) {
        int uxflag = SystemProperties.getInt("persist.sys.themeflag.uxicon", 0);
        long themeflag = SystemProperties.getLong(getThemeKeyForUser(userId), 0);
        File icons = new File("data/theme/icons");
        if (userId == 0 && uxflag == -1 && ((themeflag == 1 || themeflag == 3) && icons.exists())) {
            themeflag |= 16;
            SystemProperties.set("persist.sys.themeflag.uxicon", String.valueOf(0));
            if (OppoActivityTaskManagerDebugConfig.DEBUG_AMS) {
                Slog.d(TAG, "upgrade Q: " + themeflag + ", uxflag: " + uxflag);
            }
        }
        return themeflag;
    }

    private String getThemeKeyForUser(int userId) {
        if (userId <= 0) {
            return "persist.sys.themeflag";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("persist.sys.themeflag");
        sb.append(".");
        return sb.append(userId).toString();
    }

    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public void handleUiModeChanged(int changes) {
        if (this.mBase != null) {
            OppoBaseActivityTaskManagerService.mUiModeChanged = false;
            if ((changes & 512) != 0) {
                OppoBaseActivityTaskManagerService.mUiModeChanged = true;
                if (WindowManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.d(TAG, "handleUiModeChanged-->is uiMode change");
                }
            }
        }
    }

    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public void updateBurmeseFontLinkForUser(Configuration values, int userId, Context context) {
        ColorFrameworkFactory.getInstance().getFeature(IColorBurmeseZgHooks.DEFAULT, new Object[0]).updateBurmeseEncodingForUser(context, values, userId);
    }

    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public void updateUserIdInExtraConfiguration(Configuration target, int userId) {
        OppoFeatureCache.getOrCreate(IColorFontManager.DEFAULT, new Object[0]).setFlipFont(target, (int) DumpState.DUMP_APEX);
    }

    /* access modifiers changed from: package-private */
    public OppoBaseActivityStackSupervisor typeCasting(ActivityStackSupervisor supervisor) {
        return (OppoBaseActivityStackSupervisor) ColorTypeCastingHelper.typeCasting(OppoBaseActivityStackSupervisor.class, supervisor);
    }

    /* access modifiers changed from: package-private */
    public ColorBaseConfiguration typeCasting(Configuration config) {
        return (ColorBaseConfiguration) ColorTypeCastingHelper.typeCasting(ColorBaseConfiguration.class, config);
    }

    @Override // com.android.server.wm.ActivityTaskManagerService
    public boolean setTaskWindowingModeSplitScreenPrimary(int taskId, int createMode, boolean toTop, boolean animate, Rect initialBounds, boolean showRecents) {
        synchronized (this.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                TaskRecord detectTask = this.mRootActivityContainer.anyTaskForId(taskId, 0);
                if (detectTask == null || !OppoFeatureCache.get(IColorSplitWindowManager.DEFAULT).isInForbidActivityList(detectTask)) {
                    boolean taskWindowingModeSplitScreenPrimary = super.setTaskWindowingModeSplitScreenPrimary(taskId, createMode, toTop, animate, initialBounds, showRecents);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return taskWindowingModeSplitScreenPrimary;
                }
                Slog.w(TAG, "setTaskWindowingModeSplitScreenPrimary: task for id=" + taskId + " topRunningActivity in ForbidActivityList ");
                return false;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public ApplicationInfo getDefaultLauncherInfo() {
        PackageManagerInternal pkgInternal = getPackageManagerInternalLocked();
        ComponentName componentName = pkgInternal.getDefaultHomeActivity(this.mContext.getUserId());
        if (componentName == null) {
            return null;
        }
        ApplicationInfo applicationInfo = pkgInternal.getApplicationInfo(componentName.getPackageName(), 0, Binder.getCallingUid(), this.mContext.getUserId());
        Slog.d(TAG, "getDefaultLauncherInfo = " + componentName.toString());
        return applicationInfo;
    }

    @Override // com.android.server.wm.OppoBaseActivityTaskManagerService
    public boolean isDefaultOppoLauncher() {
        ApplicationInfo info = getDefaultLauncherInfo();
        if (info == null) {
            Slog.d(TAG, "getDefaultLauncherInfo error!");
            return true;
        } else if ((info.flags & 1) != 0) {
            return true;
        } else {
            return false;
        }
    }
}
