package com.android.server.pm;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageParser;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.operator.OppoOperatorManager;
import android.operator.OppoOperatorManagerInternal;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.oppo.OppoCustomizeNotificationHelper;
import com.android.server.oppo.TemperatureProvider;
import com.android.server.pm.PackageManagerService;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.color.util.ColorTypeCastingHelper;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class OppoPackageManagerService extends PackageManagerService {
    private static final String TAG = "OppoPackageManagerService";
    private IColorPackageManagerServiceInner colorPmsInner;
    private OppoBasePackageManagerService mBasePms;
    private final Object mLock = new Object();

    public OppoPackageManagerService(Context context, Installer installer, boolean factoryTest, boolean onlyCore) {
        super(context, installer, factoryTest, onlyCore);
        tryAssignImprtantVariables();
    }

    @Override // com.android.server.pm.PackageManagerService
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (super.onTransact(code, data, reply, flags)) {
            return true;
        }
        if (tryAssignImprtantVariables()) {
            OppoBasePackageManagerService oppoBasePackageManagerService = this.mBasePms;
            if (OppoBasePackageManagerService.mColorPmsEx != null) {
                OppoBasePackageManagerService oppoBasePackageManagerService2 = this.mBasePms;
                if (OppoBasePackageManagerService.mColorPmsEx.onTransact(code, data, reply, flags)) {
                    return true;
                }
            }
        }
        OppoBasePackageManagerService oppoBasePackageManagerService3 = this.mBasePms;
        if (oppoBasePackageManagerService3 == null || oppoBasePackageManagerService3.mPswPmsEx == null || !this.mBasePms.mPswPmsEx.onTransact(code, data, reply, flags)) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.pm.PackageManagerService
    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(39, Binder.getCallingUid());
        if (!tryAssignImprtantVariables() || !OppoFeatureCache.get(IColorDefaultAppPolicyManager.DEFAULT).isGotDefaultAppBeforeAddPreferredActivity(filter, match, set, activity, userId)) {
            super.addPreferredActivity(filter, match, set, activity, userId);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.pm.OppoBasePackageManagerService
    public void preInitialize() {
        Slog.d(TAG, "startDataFree");
    }

    @Override // com.android.server.pm.PackageManagerService
    public boolean hasSystemFeature(String name, int version) {
        OppoOperatorManagerInternal opm;
        synchronized (this.mAvailableFeatures) {
            if ((name.equals("android.hardware.fingerprint") || name.equals("android.hardware.biometrics.face")) && SystemProperties.getInt("persist.sys.ban.mode", 1) == 0) {
                return false;
            }
            if (tryAssignImprtantVariables() && OppoFeatureCache.get(IColorDynamicFeatureManager.DEFAULT).hasOppoSystemFeature(name)) {
                Slog.d(TAG, "feature " + name + " version " + version + " is oppo system feature");
                return true;
            } else if (!OppoOperatorManager.SERVICE_ENABLED) {
                return super.hasSystemFeature(name, version);
            } else {
                boolean result = super.hasSystemFeature(name, version);
                if (!result && (opm = (OppoOperatorManagerInternal) LocalServices.getService(OppoOperatorManagerInternal.class)) != null && ActivityManagerNative.isSystemReady()) {
                    result = opm.hasFeatureDynamiclyEnabeld(name);
                }
                return result;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.pm.OppoBasePackageManagerService
    public void clearSellModeIfNeeded() {
        if (tryAssignImprtantVariables()) {
            OppoFeatureCache.get(IColorSellModeManager.DEFAULT).clearSellModeIfNeeded();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.pm.OppoBasePackageManagerService
    public void interceptScanSellModeIfNeeded(String name) throws PackageManagerException {
        if (tryAssignImprtantVariables()) {
            OppoFeatureCache.get(IColorSellModeManager.DEFAULT).interceptScanSellModeIfNeeded(name);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.pm.OppoBasePackageManagerService
    public boolean interceptUninstallSellModeIfNeeded(PackageParser.Package pkg, IPackageDeleteObserver2 observer) {
        if (tryAssignImprtantVariables()) {
            return OppoFeatureCache.get(IColorSellModeManager.DEFAULT).interceptUninstallSellModeIfNeeded(pkg, observer);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.pm.OppoBasePackageManagerService
    public void setExpDefaultBrowser() {
        if (tryAssignImprtantVariables()) {
            OppoFeatureCache.get(IColorExpDefaultBrowserManager.DEFAULT).setExpDefaultBrowser();
        }
    }

    @Override // com.android.server.pm.OppoBasePackageManagerService
    public void uploadInstallAppInfos(Context context, PackageParser.Package pkg, String installerPackageName) {
        if (tryAssignImprtantVariables()) {
            OppoBasePackageManagerService oppoBasePackageManagerService = this.mBasePms;
            if (OppoBasePackageManagerService.mColorPmsEx != null) {
                OppoBasePackageManagerService oppoBasePackageManagerService2 = this.mBasePms;
                OppoBasePackageManagerService.mColorPmsEx.uploadInstallAppInfos(context, pkg, installerPackageName);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.pm.PackageManagerService
    public void installStage(PackageManagerService.ActiveInstallSession activeInstallSession) {
        int userId;
        if (!tryAssignImprtantVariables()) {
            super.installStage(activeInstallSession);
        }
        activeInstallSession.getUser();
        if (OppoFeatureCache.get(IColorChildrenModeInstallManager.DEFAULT).prohibitChildInstallation(UserHandle.myUserId(), true)) {
            try {
                if (activeInstallSession.getObserver() != null) {
                    activeInstallSession.getObserver().onPackageInstalled(activeInstallSession.getPackageName(), -111, "forbidden install in childmode", (Bundle) null);
                }
            } catch (RemoteException e) {
            }
        } else {
            int installerUid = activeInstallSession.getInstallerUid();
            PackageInstaller.SessionParams sessionParams = activeInstallSession.getSessionParams();
            File stagedDir = activeInstallSession.getStagedDir();
            String packageName = activeInstallSession.getPackageName();
            IPackageInstallObserver2 observer = activeInstallSession.getObserver();
            String installerPackageName = activeInstallSession.getInstallerPackageName();
            if (OppoFeatureCache.get(IColorPackageInstallInterceptManager.DEFAULT).allowInterceptAdbInstallInInstallStage(installerUid, sessionParams, stagedDir, packageName, observer)) {
                Slog.d(TAG, "installStage allowInterceptAdbInstallInInstallStage pkg:" + packageName + ";stagedDir " + stagedDir);
                return;
            }
            String installerPackageName2 = installerPackageName;
            if (OppoFeatureCache.get(IColorPackageInstallInterceptManager.DEFAULT).allowInterceptSilentInstallerInStallStage(installerPackageName, sessionParams, stagedDir, packageName, observer, getNameForUid(installerUid))) {
                Slog.d(TAG, "installStage allowInterceptSilentInstallerInStallStage pkg:" + packageName + ";installerPackageName " + installerPackageName2);
                return;
            }
            activeInstallSession.getInstallerUid();
            activeInstallSession.getStagedDir();
            activeInstallSession.getPackageName();
            activeInstallSession.getInstallerPackageName();
            boolean isFullmode = OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall();
            PackageManagerService.OriginInfo origin = PackageManagerService.OriginInfo.fromStagedFile(stagedDir);
            if (!"com.android.cts.permissiondeclareapp".equals(packageName)) {
                installerPackageName2 = OppoFeatureCache.get(IColorPackageInstallStatisticManager.DEFAULT).addRunningInstallerPackageName(origin, installerPackageName2, installerUid, isFullmode);
            }
            Slog.d(TAG, "installStage " + packageName + ", dir=" + stagedDir + ", installerPackageName=" + installerPackageName2 + ", installerUid=" + installerUid + ", full=" + isFullmode);
            IColorPackageManagerServiceInner iColorPackageManagerServiceInner = this.colorPmsInner;
            if (iColorPackageManagerServiceInner != null) {
                iColorPackageManagerServiceInner.setInstallerPackageName(activeInstallSession, installerPackageName2);
            }
            activeInstallSession.getStagedDir();
            activeInstallSession.getPackageName();
            activeInstallSession.getInstallerPackageName();
            activeInstallSession.getInstallerUid();
            if (activeInstallSession.getUser() == null) {
                userId = 0;
            } else {
                userId = activeInstallSession.getUser().getIdentifier();
            }
            OppoFeatureCache.get(IColorAppInstallProgressManager.DEFAULT).sendOppoStartInstallBro(stagedDir, packageName, installerPackageName2, userId, "com.android.packageinstaller".equals(getNameForUid(installerUid)));
            super.installStage(activeInstallSession);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.pm.PackageManagerService
    public void installStage(List<PackageManagerService.ActiveInstallSession> children) throws PackageManagerException {
        int userId;
        if (!tryAssignImprtantVariables()) {
            super.installStage(children);
        }
        Iterator<PackageManagerService.ActiveInstallSession> iterator = children.iterator();
        while (iterator.hasNext()) {
            PackageManagerService.ActiveInstallSession activeSession = iterator.next();
            activeSession.getUser();
            if (OppoFeatureCache.get(IColorChildrenModeInstallManager.DEFAULT).prohibitChildInstallation(UserHandle.myUserId(), true)) {
                try {
                    if (activeSession.getObserver() != null) {
                        activeSession.getObserver().onPackageInstalled(activeSession.getPackageName(), -111, "forbidden install in childmode", (Bundle) null);
                    }
                } catch (RemoteException e) {
                }
                iterator.remove();
            }
        }
        if (children.size() >= 1) {
            Iterator<PackageManagerService.ActiveInstallSession> iterator1 = children.iterator();
            while (iterator1.hasNext()) {
                PackageManagerService.ActiveInstallSession activeSession2 = iterator1.next();
                int installerUid = activeSession2.getInstallerUid();
                PackageInstaller.SessionParams sessionParams = activeSession2.getSessionParams();
                File stagedDir = activeSession2.getStagedDir();
                String packageName = activeSession2.getPackageName();
                IPackageInstallObserver2 observer = activeSession2.getObserver();
                String installerPackageName = activeSession2.getInstallerPackageName();
                if (OppoFeatureCache.get(IColorPackageInstallInterceptManager.DEFAULT).allowInterceptAdbInstallInInstallStage(installerUid, sessionParams, stagedDir, packageName, observer)) {
                    Slog.d(TAG, "installStage allowInterceptAdbInstallInInstallStage pkg:" + packageName + ";stagedDir " + stagedDir);
                    iterator1.remove();
                }
                if (OppoFeatureCache.get(IColorPackageInstallInterceptManager.DEFAULT).allowInterceptSilentInstallerInStallStage(installerPackageName, sessionParams, stagedDir, packageName, observer, getNameForUid(installerUid))) {
                    Slog.d(TAG, "installStage allowInterceptSilentInstallerInStallStage pkg:" + packageName + ";installerPackageName " + installerPackageName);
                    iterator1.remove();
                }
            }
            if (children.size() >= 1) {
                for (int i = 0; i < children.size(); i++) {
                    PackageManagerService.ActiveInstallSession activeSession3 = children.get(i);
                    int installerUid2 = activeSession3.getInstallerUid();
                    File stagedDir2 = activeSession3.getStagedDir();
                    String packageName2 = activeSession3.getPackageName();
                    String installerPackageName2 = activeSession3.getInstallerPackageName();
                    boolean isFullmode = OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall();
                    String installerPackageName3 = OppoFeatureCache.get(IColorPackageInstallStatisticManager.DEFAULT).addRunningInstallerPackageName(PackageManagerService.OriginInfo.fromStagedFile(stagedDir2), installerPackageName2, installerUid2, isFullmode);
                    Slog.d(TAG, "installStage " + packageName2 + ", dir=" + stagedDir2 + ", installerPackageName=" + installerPackageName3 + ", installerUid=" + installerUid2 + ", full=" + isFullmode);
                    IColorPackageManagerServiceInner iColorPackageManagerServiceInner = this.colorPmsInner;
                    if (iColorPackageManagerServiceInner != null) {
                        iColorPackageManagerServiceInner.setInstallerPackageName(activeSession3, installerPackageName3);
                    }
                }
                for (int i2 = 0; i2 < children.size(); i2++) {
                    PackageManagerService.ActiveInstallSession activeSession4 = children.get(i2);
                    File stagedDir3 = activeSession4.getStagedDir();
                    String packageName3 = activeSession4.getPackageName();
                    String installerPackageName4 = activeSession4.getInstallerPackageName();
                    int installerUid3 = activeSession4.getInstallerUid();
                    if (activeSession4.getUser() == null) {
                        userId = 0;
                    } else {
                        userId = activeSession4.getUser().getIdentifier();
                    }
                    OppoFeatureCache.get(IColorAppInstallProgressManager.DEFAULT).sendOppoStartInstallBro(stagedDir3, packageName3, installerPackageName4, userId, "com.android.packageinstaller".equals(getNameForUid(installerUid3)));
                }
                super.installStage(children);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.pm.OppoBasePackageManagerService
    public void postDoSendBroadcast(IActivityManager am, IIntentReceiver finishedReceiver, String targetPkg, String pkgAction, Intent intent, int id) throws RemoteException {
        if (targetPkg == null) {
            String[] requiredOPPOPermissions = {"oppo.permission.OPPO_COMPONENT_SAFE"};
            if ("android.intent.action.PACKAGE_ADDED".equals(pkgAction)) {
                intent.setAction("oppo.intent.action.PACKAGE_ADDED");
                intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
                am.broadcastIntent((IApplicationThread) null, intent, (String) null, finishedReceiver, 0, (String) null, (Bundle) null, requiredOPPOPermissions, -1, (Bundle) null, finishedReceiver != null, false, id);
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(pkgAction)) {
                intent.setAction("oppo.intent.action.PACKAGE_REMOVED");
                intent.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
                am.broadcastIntent((IApplicationThread) null, intent, (String) null, finishedReceiver, 0, (String) null, (Bundle) null, requiredOPPOPermissions, -1, (Bundle) null, finishedReceiver != null, false, id);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.pm.OppoBasePackageManagerService
    public boolean deleteFailedByPolicy() {
        if (!SystemProperties.get("persist.sys.oppo.region", "CN").equals("CN") || hasSystemFeature(OppoCustomizeNotificationHelper.Constants.FEATURE_BUSINESS_CUSTOM, 0)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.pm.OppoBasePackageManagerService
    public void operatorAppCopy(String operatorDir, File appInstallDir) {
        File apkPath = new File(Environment.getRootDirectory(), operatorDir);
        if (apkPath.exists()) {
            File[] listFiles = apkPath.listFiles();
            for (File apkFile : listFiles) {
                try {
                    Slog.d(TAG, "copy apk to " + appInstallDir + ":" + apkFile);
                    PackageParser.PackageLite pkg = PackageParser.parsePackageLite(apkFile, Integer.MIN_VALUE);
                    if (pkg == null) {
                        Slog.i(TAG, "reserve package null, error!!!");
                    } else if (this.mSettings.mPackages.containsKey(pkg.packageName)) {
                        Slog.i(TAG, "apk:" + pkg.packageName + " has been installed, skip");
                    } else {
                        File destFile = new File(appInstallDir, apkFile.getName());
                        Slog.i(TAG, "apk:" + pkg.packageName + " has NOT been installed, copy it to " + destFile.getPath() + "......");
                        FileUtils.copyFile(apkFile, destFile);
                        FileUtils.setPermissions(destFile.getPath(), TemperatureProvider.HIGH_TEMPERATURE_THRESHOLD, -1, -1);
                    }
                } catch (PackageParser.PackageParserException e) {
                    Slog.e(TAG, "copy file to " + appInstallDir + " error!!!");
                }
            }
        }
    }

    @Override // com.android.server.pm.PackageManagerService
    public ParceledListSlice<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
        if (!tryAssignImprtantVariables()) {
            return super.queryIntentActivities(intent, resolvedType, flags, userId);
        }
        try {
            Trace.traceBegin(262144, "queryIntentActivities");
            List<ResolveInfo> filterList = OppoFeatureCache.get(IColorDefaultAppPolicyManager.DEFAULT).queryColorFilteredIntentActivities(intent, resolvedType, flags, userId);
            if (filterList != null) {
                return new ParceledListSlice<>(filterList);
            }
            ParceledListSlice<ResolveInfo> queryIntentActivities = super.queryIntentActivities(intent, resolvedType, flags, userId);
            Trace.traceEnd(262144);
            return queryIntentActivities;
        } catch (Exception e) {
            e.printStackTrace();
            return new ParceledListSlice<>(Collections.emptyList());
        } finally {
            Trace.traceEnd(262144);
        }
    }

    @Override // com.android.server.pm.PackageManagerService
    public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        if (!tryAssignImprtantVariables() || !OppoFeatureCache.get(IColorDefaultAppPolicyManager.DEFAULT).forbiddenSetPreferredActivity(filter)) {
            super.replacePreferredActivity(filter, match, set, activity, userId);
        }
    }

    @Override // com.android.server.pm.PackageManagerService
    public void clearPackagePreferredActivities(String packageName) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(40, Binder.getCallingUid());
        super.clearPackagePreferredActivities(packageName);
    }

    @Override // com.android.server.pm.PackageManagerService
    public void setApplicationEnabledSetting(String appPackageName, int newState, int flags, int userId, String callingPackage) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(37, Binder.getCallingUid());
        super.setApplicationEnabledSetting(appPackageName, newState, flags, userId, callingPackage);
    }

    @Override // com.android.server.pm.PackageManagerService
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags, int userId) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setComponentEnabledSetting(newState, Binder.getCallingUid());
        super.setComponentEnabledSetting(componentName, newState, flags, userId);
    }

    private OppoBasePackageManagerService typeCasting(OppoPackageManagerService pms) {
        return (OppoBasePackageManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePackageManagerService.class, pms);
    }

    private boolean tryAssignImprtantVariables() {
        if (this.mBasePms != null) {
            return true;
        }
        this.mBasePms = typeCasting(this);
        OppoBasePackageManagerService oppoBasePackageManagerService = this.mBasePms;
        if (oppoBasePackageManagerService == null) {
            return false;
        }
        this.colorPmsInner = oppoBasePackageManagerService.getColorPackageManagerServiceInner();
        return true;
    }
}
