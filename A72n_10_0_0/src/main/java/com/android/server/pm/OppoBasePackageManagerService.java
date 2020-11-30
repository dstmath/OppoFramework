package com.android.server.pm;

import android.app.IActivityManager;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageManager;
import android.content.pm.OppoBaseSessionParams;
import android.content.pm.OppoCutomizeManagerInternal;
import android.content.pm.OppoPackageManagerInternal;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageParser;
import android.os.Message;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.ColorLocalServices;
import com.android.server.LocalServices;
import com.android.server.wm.OppoUsageManager;
import com.color.util.ColorTypeCastingHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class OppoBasePackageManagerService extends IPackageManager.Stub {
    static final int COLOR_PMS_BG_HANDLER = 3;
    static final int COLOR_PMS_KILL_HANDLER = 4;
    static final int COLOR_PMS_MAIN_HANDLER = 1;
    static final int COLOR_PMS_MSG_INDEX = 500;
    static final int COLOR_PMS_UI_HANDLER = 2;
    protected static final String PROPERTY_OPPO_REGION = "persist.sys.oppo.region";
    private static final String TAG = "PackageManager";
    static final int UPDATE_EXTRA_APP_INFO = 199;
    static IColorMergedProcessSplitManager mColorMergedProcessSplitManager = null;
    static IColorPackageManagerServiceEx mColorPmsEx = null;
    IColorPackageManagerServiceInner mColorPmsInner = null;
    private OppoCutomizeManagerInternal mOppoCutomizeManagerInternal = null;
    IPswPackageManagerServiceEx mPswPmsEx = null;

    public IColorPackageManagerServiceInner getColorPackageManagerServiceInner() {
        return this.mColorPmsInner;
    }

    protected static IColorMergedProcessSplitManager getColorMergedProcessSplitManager() {
        if (mColorMergedProcessSplitManager == null) {
            mColorMergedProcessSplitManager = (IColorMergedProcessSplitManager) ColorLocalServices.getService(IColorMergedProcessSplitManager.class);
        }
        return mColorMergedProcessSplitManager;
    }

    /* access modifiers changed from: protected */
    public OppoCutomizeManagerInternal getOppoCutomizeManagerInternal() {
        if (this.mOppoCutomizeManagerInternal == null) {
            this.mOppoCutomizeManagerInternal = (OppoCutomizeManagerInternal) LocalServices.getService(OppoCutomizeManagerInternal.class);
        }
        return this.mOppoCutomizeManagerInternal;
    }

    /* access modifiers changed from: protected */
    public void onOppoStart() {
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = mColorPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            iColorPackageManagerServiceEx.onStart();
        }
        IPswPackageManagerServiceEx iPswPackageManagerServiceEx = this.mPswPmsEx;
        if (iPswPackageManagerServiceEx != null) {
            iPswPackageManagerServiceEx.onStart();
        }
    }

    /* access modifiers changed from: protected */
    public void onOppoSystemReady() {
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = mColorPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            iColorPackageManagerServiceEx.systemReady();
        }
        IPswPackageManagerServiceEx iPswPackageManagerServiceEx = this.mPswPmsEx;
        if (iPswPackageManagerServiceEx != null) {
            iPswPackageManagerServiceEx.systemReady();
        }
    }

    /* access modifiers changed from: protected */
    public void handleOppoMessage(Message msg, int whichHandler) {
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = mColorPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            iColorPackageManagerServiceEx.handleMessage(msg, whichHandler);
        }
        IPswPackageManagerServiceEx iPswPackageManagerServiceEx = this.mPswPmsEx;
        if (iPswPackageManagerServiceEx != null) {
            iPswPackageManagerServiceEx.handleMessage(msg, whichHandler);
        }
    }

    /* access modifiers changed from: protected */
    public void doSendBroadcastBase(IActivityManager am, Intent intent, String action, String pkgAction, String pkg, String targetPkg, PackageManagerService pms, IIntentReceiver finishedReceiver, int id) throws RemoteException {
    }

    /* access modifiers changed from: protected */
    public void preInitialize() {
    }

    /* access modifiers changed from: protected */
    public void clearSellModeIfNeeded() {
    }

    /* access modifiers changed from: protected */
    public void interceptScanSellModeIfNeeded(String name) throws PackageManagerException {
    }

    /* access modifiers changed from: protected */
    public boolean interceptUninstallSellModeIfNeeded(PackageParser.Package pkg, IPackageDeleteObserver2 observer) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void setExpDefaultBrowser() {
    }

    /* access modifiers changed from: protected */
    public boolean needHideAppList(String pkgName, boolean isSystemCaller, boolean debug) {
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = mColorPmsEx;
        if (iColorPackageManagerServiceEx == null || !iColorPackageManagerServiceEx.needHideApp(pkgName, isSystemCaller, debug)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void postDoSendBroadcast(IActivityManager am, IIntentReceiver finishedReceiver, String targetPkg, String pkgAction, Intent intent, int id) throws RemoteException {
    }

    /* access modifiers changed from: protected */
    public boolean deleteFailedByPolicy() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void operatorAppCopy(String operatorDir, File appInstallDir) {
    }

    public void uploadInstallAppInfos(Context context, PackageParser.Package pkg, String installerPackageName) {
    }

    public void registerOppoPackageManagerInternalImpl() {
        LocalServices.addService(OppoPackageManagerInternal.class, new OppoPackageManagerInternalImpl());
    }

    public boolean isFullFunctionMode() {
        return OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall();
    }

    public boolean isClosedSuperFirewall() {
        return OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall();
    }

    public boolean loadRegionFeature(String name) {
        return OppoFeatureCache.get(IColorDynamicFeatureManager.DEFAULT).loadRegionFeature(name);
    }

    public FeatureInfo[] getOppoSystemAvailableFeatures() {
        return OppoFeatureCache.get(IColorDynamicFeatureManager.DEFAULT).getOppoSystemAvailableFeatures();
    }

    public boolean isSystemDataApp(String packageName) {
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = mColorPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            return iColorPackageManagerServiceEx.isSystemDataApp(packageName);
        }
        return false;
    }

    public boolean isNewUserInstallPkg(String packageName) {
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = mColorPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            return iColorPackageManagerServiceEx.isNewUserInstallPkg(packageName);
        }
        return false;
    }

    public boolean isNewUserNotInstallPkg(String packageName) {
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = mColorPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            return iColorPackageManagerServiceEx.isNewUserNotInstallPkg(packageName);
        }
        return false;
    }

    public boolean inPmsWhiteList(int type, String verifyStr, List<String> defaultList) {
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = mColorPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            return iColorPackageManagerServiceEx.inPmsWhiteList(type, verifyStr, defaultList);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean shouldInterceptInScanStage(PackageParser.Package pkg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void scanExtraDirs(int scanFlags) {
    }

    public static class InstallParamsEx {
        private int extraInstallFlags;
        private String mExtraSessionInfo;

        public static InstallParamsEx initInstallParamsEx(PackageInstaller.SessionParams sessionParams) {
            InstallParamsEx installParamsEx = new InstallParamsEx();
            installParamsEx.init(sessionParams);
            return installParamsEx;
        }

        public void init(PackageInstaller.SessionParams sessionParams) {
            OppoBaseSessionParams baseSessionParams = OppoBasePackageManagerService.typeCastingBaseSessionParams(sessionParams);
            if (baseSessionParams != null) {
                if (baseSessionParams != null) {
                    this.extraInstallFlags = baseSessionParams.extraInstallFlags;
                    if (PackageManagerService.DEBUG_INSTALL) {
                        Slog.d(OppoBasePackageManagerService.TAG, "installStage extraInstallFlags= " + this.extraInstallFlags);
                    }
                }
                if (PackageManagerService.DEBUG_INSTALL) {
                    Slog.d(OppoBasePackageManagerService.TAG, "installStage extraSessionInfo= " + baseSessionParams.extraSessionInfo);
                }
                if (baseSessionParams != null) {
                    try {
                        if (baseSessionParams.extraSessionInfo != null) {
                            if (baseSessionParams.extraSessionInfo.length() > 1024) {
                                this.mExtraSessionInfo = baseSessionParams.extraSessionInfo.substring(0, 1024);
                                return;
                            } else {
                                this.mExtraSessionInfo = baseSessionParams.extraSessionInfo;
                                return;
                            }
                        }
                    } catch (Exception e) {
                        return;
                    }
                }
                this.mExtraSessionInfo = "";
            }
        }
    }

    public static class InstallArgsEx {
        private int extraInstallFlags;
        String extraSessionInfo;
        String packageName;

        public static InstallArgsEx initInstallArgsEx(InstallParamsEx installParamsEx) {
            InstallArgsEx installArgsEx = new InstallArgsEx();
            installArgsEx.init(installParamsEx);
            return installArgsEx;
        }

        public void init(InstallParamsEx installParamsEx) {
            if (installParamsEx != null) {
                this.extraInstallFlags = installParamsEx.extraInstallFlags;
                if (installParamsEx.mExtraSessionInfo != null) {
                    this.extraSessionInfo = installParamsEx.mExtraSessionInfo;
                    return;
                }
                this.extraSessionInfo = "";
                if (PackageManagerService.DEBUG_INSTALL) {
                    Slog.i(OppoBasePackageManagerService.TAG, "handleStartCopy() mExtraSessionParams == null");
                }
            }
        }

        public int getExtraInstallFlags() {
            return this.extraInstallFlags;
        }
    }

    private class OppoPackageManagerInternalImpl extends OppoPackageManagerInternal {
        private OppoPackageManagerInternalImpl() {
        }

        public void clearIconCache() {
            OppoFeatureCache.get(IColorIconCachesManager.DEFAULT).clearIconCache();
        }

        public void autoUnfreezePackage(String pkgName, int userId, String reason) {
            OppoFeatureCache.get(IColorAppQuickFreezeManager.DEFAULT).autoUnfreezePackage(pkgName, userId, reason);
        }

        public void interceptClearUserDataIfNeeded(String packageName) throws SecurityException {
            OppoFeatureCache.get(IColorClearDataProtectManager.DEFAULT).interceptClearUserDataIfNeeded(packageName);
        }

        public boolean grantPermissionOppoPolicy(PackageParser.Package pkg, String perm, boolean allowed) {
            return OppoFeatureCache.get(IColorSensitivePermGrantPolicyManager.DEFAULT).grantPermissionOppoPolicy(pkg, perm, allowed);
        }

        public void grantOppoPermissionByGroup(PackageParser.Package pkg, String permName, String packageName, int callingUid) {
            OppoFeatureCache.get(IColorRuntimePermGrantPolicyManager.DEFAULT).grantOppoPermissionByGroup(pkg, permName, packageName, callingUid);
        }

        public void revokeOppoPermissionByGroup(PackageParser.Package pkg, String permName, String packageName, int callingUid) {
            OppoFeatureCache.get(IColorRuntimePermGrantPolicyManager.DEFAULT).revokeOppoPermissionByGroup(pkg, permName, packageName, callingUid);
        }

        public void grantOppoPermissionByGroupAsUser(PackageParser.Package pkg, String permName, String packageName, int callingUid, int userId) {
            OppoFeatureCache.get(IColorRuntimePermGrantPolicyManager.DEFAULT).grantOppoPermissionByGroupAsUser(pkg, permName, packageName, callingUid, userId);
        }

        public void revokeOppoPermissionByGroupAsUser(PackageParser.Package pkg, String permName, String packageName, int callingUid, int userId) {
            OppoFeatureCache.get(IColorRuntimePermGrantPolicyManager.DEFAULT).revokeOppoPermissionByGroupAsUser(pkg, permName, packageName, callingUid, userId);
        }

        public ArrayList<String> getIgnoreAppList() {
            return OppoFeatureCache.get(IColorRuntimePermGrantPolicyManager.DEFAULT).getIgnoreAppList();
        }

        public boolean allowAddInstallPermForDataApp(String packageName) {
            return OppoFeatureCache.get(IColorSensitivePermGrantPolicyManager.DEFAULT).allowAddInstallPermForDataApp(packageName);
        }

        public boolean onPermissionRevoked(ApplicationInfo applicationInfo, int userId) {
            return OppoFeatureCache.get(IColorRuntimePermGrantPolicyManager.DEFAULT).onPermissionRevoked(applicationInfo, userId);
        }

        public boolean isRuntimePermissionFingerprintNew(int userId) {
            return OppoFeatureCache.get(IColorRuntimePermGrantPolicyManager.DEFAULT).isRuntimePermissionFingerprintNew(userId);
        }
    }

    private final void warn(String methodName) {
        Slog.w(TAG, methodName + " not implemented");
    }

    /* access modifiers changed from: private */
    public static OppoBaseSessionParams typeCastingBaseSessionParams(PackageInstaller.SessionParams sessionParams) {
        return (OppoBaseSessionParams) ColorTypeCastingHelper.typeCasting(OppoBaseSessionParams.class, sessionParams);
    }

    /* access modifiers changed from: protected */
    public void saveAppDeleteRecorder(String packageName, String delCallerPkg, boolean needUpload) {
        OppoUsageManager.recordApkDeleteEvent(packageName, delCallerPkg, null);
    }
}
