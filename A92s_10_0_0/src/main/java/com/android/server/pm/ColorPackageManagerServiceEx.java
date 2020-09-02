package com.android.server.pm;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.pm.OppoApplicationInfoEx;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.ColorServiceRegistry;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.pm.OppoBasePackageManagerService;
import com.android.server.pm.OppoMarketHelper;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.permission.DefaultPermissionGrantPolicy;
import com.android.server.pm.permission.IColorDefaultPermissionGrantPolicyInner;
import com.android.server.pm.permission.OppoBaseDefaultPermissionGrantPolicy;
import com.color.util.ColorTypeCastingHelper;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class ColorPackageManagerServiceEx extends ColorDummyPackageManagerServiceEx {
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    protected static final String PROPERTY_OPPO_REGION = "persist.sys.oppo.region";
    private static final String TAG = "ColorPackageManagerServiceEx";
    private static final Comparator<ResolveInfo> mSystemAppPrioritySorter = new Comparator<ResolveInfo>() {
        /* class com.android.server.pm.ColorPackageManagerServiceEx.AnonymousClass2 */

        public int compare(ResolveInfo r1, ResolveInfo r2) {
            int v1 = r1.priority;
            int v2 = r2.priority;
            if (v1 != v2) {
                return v1 > v2 ? -1 : 1;
            }
            int v12 = r1.preferredOrder;
            int v22 = r2.preferredOrder;
            if (v12 != v22) {
                return v12 > v22 ? -1 : 1;
            }
            if (r1.isDefault != r2.isDefault) {
                return r1.isDefault ? -1 : 1;
            }
            boolean isSystemApp1 = r1.system;
            boolean isSystemApp2 = r2.system;
            if (r1.activityInfo != null && !r1.system && ColorPackageManagerHelper.isSystemDataApp(r1.activityInfo.packageName)) {
                isSystemApp1 = true;
            }
            if (r2.activityInfo != null && !r2.system && ColorPackageManagerHelper.isSystemDataApp(r2.activityInfo.packageName)) {
                isSystemApp2 = true;
            }
            if (isSystemApp1 != isSystemApp2) {
                return isSystemApp1 ? -1 : 1;
            }
            return 0;
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private OppoMarketHelper.ExtraApplicationInfoManager mExtraApplicationInfoManager;
    private ColorPackageManagerMessageHelper mMessageHelper;
    private final Runnable mReadColorPackageMangerConfig = new Runnable() {
        /* class com.android.server.pm.ColorPackageManagerServiceEx.AnonymousClass1 */

        public void run() {
            Slog.d(ColorPackageManagerServiceEx.TAG, "Run Read System default app");
            ColorPackageManagerHelper.oppoReadDefaultPkg(ColorPackageManagerServiceEx.this.mContext);
            Slog.d(ColorPackageManagerServiceEx.TAG, "init forbid uninstall data app");
            ColorPackageManagerHelper.oppoReadForbidUninstallPkg();
            AppFrozenWhiteListHelper.getInstance(ColorPackageManagerServiceEx.this.mContext).initUpdateBroadcastReceiver();
        }
    };
    private ColorPackageManagerTransactionHelper mTransactionHelper;

    public ColorPackageManagerServiceEx(Context context, PackageManagerService pms) {
        super(context, pms);
        this.mContext = context;
        init(context, pms);
    }

    public Context getContext() {
        return this.mContext;
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        if (ColorPackageManagerHelper.dataAppContainCtsPkgBySig(this.mPms)) {
            OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).setClosedSuperFirewall(true);
        }
        ColorServiceRegistry.getInstance().serviceReady(24);
        this.mPms.mHandler.post(this.mReadColorPackageMangerConfig);
        OppoFeatureCache.get(IColorIconCachesManager.DEFAULT).systemReady();
        OppoFeatureCache.get(IColorRuntimePermGrantPolicyManager.DEFAULT).systemReady();
        OppoFeatureCache.get(IColorSecurePayManager.DEFAULT).systemReady();
        OppoFeatureCache.get(IColorPackageInstallInterceptManager.DEFAULT).systemReady();
        OppoFeatureCache.get(IColorOtaDataManager.DEFAULT).systemReady();
    }

    public void onStart() {
        Slog.i(TAG, "onStart");
        Slog.d(TAG, "startDataFree");
        OppoFeatureCache.get(IColorDataFreeManager.DEFAULT).startDataFree();
        ColorPackageManagerHelper.readPermKeyBlackList();
        if (ColorPackageManagerHelper.readEncryptFiles() != 0) {
            Slog.w(TAG, "fatal error:read encrypt file fail, try again");
            ColorPackageManagerHelper.readEncryptFiles();
        }
        loadOtherFeatures();
        ColorPackageManagerHelper.initCtsToolList();
        if (this.mPms.hasSystemFeature("oppo.customize.function.allow_launcher_settings", 0)) {
            ColorPackageManagerHelper.removeForceLauncher();
        }
        ColorPackageManagerHelper.replaceDefaultLauncherCustom(this.mPms);
        OppoFeatureCache.get(IColorDefaultAppPolicyManager.DEFAULT).addBrowserToDefaultPackageList();
        OppoFeatureCache.get(IColorAppListInterceptManager.DEFAULT).loadHideAppConfigurations();
        OppoFeatureCache.get(IColorOtaDataManager.DEFAULT).initAppList();
        OppoFeatureCache.get(IColorRemovableAppManager.DEFAULT).updateConfigurations();
        this.mExtraApplicationInfoManager = new OppoMarketHelper.ExtraApplicationInfoManager();
    }

    /* access modifiers changed from: package-private */
    public void loadOtherFeatures() {
        String regionName = SystemProperties.get(PROPERTY_OPPO_REGION);
        if (!TextUtils.isEmpty(regionName)) {
            Slog.w(TAG, "load region" + regionName + "features!");
            if (!OppoFeatureCache.get(IColorDynamicFeatureManager.DEFAULT).loadRegionFeature(regionName)) {
                Slog.w(TAG, "load region feature failed!");
            }
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        PackageManagerService packageManagerService = this.mPms;
        if (PackageManagerService.DEBUG_INSTALL) {
            Slog.i(TAG, "onTransact code = " + code);
        }
        ColorPackageManagerTransactionHelper colorPackageManagerTransactionHelper = this.mTransactionHelper;
        if (colorPackageManagerTransactionHelper != null) {
            return colorPackageManagerTransactionHelper.onTransact(code, data, reply, flags);
        }
        return ColorPackageManagerServiceEx.super.onTransact(code, data, reply, flags);
    }

    public void handleMessage(Message msg, int whichHandler) {
        if (DEBUG) {
            Slog.i(TAG, "handleMessage msg = " + msg + " handler = " + whichHandler);
        }
        ColorPackageManagerMessageHelper colorPackageManagerMessageHelper = this.mMessageHelper;
        if (colorPackageManagerMessageHelper != null) {
            colorPackageManagerMessageHelper.handleMessage(msg, whichHandler);
        }
    }

    private void init(Context context, PackageManagerService pms) {
        this.mMessageHelper = new ColorPackageManagerMessageHelper(context, pms);
        this.mTransactionHelper = new ColorPackageManagerTransactionHelper(context, pms);
        ColorServiceRegistry.getInstance().serviceInit(4, this);
    }

    public IColorPackageManagerServiceInner getColorPackageManagerServiceInner() {
        OppoBasePackageManagerService basePms = (OppoBasePackageManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePackageManagerService.class, this.mPms);
        if (basePms != null) {
            return basePms.mColorPmsInner;
        }
        return null;
    }

    public IColorDefaultPermissionGrantPolicyInner getColorRuntimePermGrantPolicyManagerInner(DefaultPermissionGrantPolicy defaultPermissionGrantPolicy) {
        OppoBaseDefaultPermissionGrantPolicy basePolicy = (OppoBaseDefaultPermissionGrantPolicy) ColorTypeCastingHelper.typeCasting(OppoBaseDefaultPermissionGrantPolicy.class, defaultPermissionGrantPolicy);
        if (basePolicy != null) {
            return basePolicy.mColorDefaultPermissionGrantPolicyInner;
        }
        return null;
    }

    public void extendApplyPolicy(PackageParser.Package pkg, int parseFlags, int scanFlags, PackageParser.Package platformPkg) {
        if (ColorPackageManagerHelper.isOppoSignature(pkg)) {
            Slog.d(TAG, "found oppo key app :" + pkg.applicationInfo.packageName);
            OppoApplicationInfoEx oppoAppInfoEx = OppoApplicationInfoEx.getOppoAppInfoExFromAppInfoRef(pkg.applicationInfo);
            if (oppoAppInfoEx != null) {
                oppoAppInfoEx.oemPrivateFlags |= 1;
            }
        }
    }

    public boolean inPmsWhiteList(int type, String verifyStr, List<String> defaultList) {
        return ColorPackageManagerHelper.inPmsWhiteList(type, verifyStr, defaultList);
    }

    public boolean isSystemDataApp(String packageName) {
        return ColorPackageManagerHelper.isSystemDataApp(packageName);
    }

    public boolean needHideApp(String pkgName, boolean isSystemCaller, boolean debug) {
        if (ColorPackageManagerHelper.isPrivilegedHideApp(pkgName)) {
            return true;
        }
        String callerName = this.mPms.getNameForUid(Binder.getCallingUid());
        if (isSystemCaller || !ColorPackageManagerHelper.isSafeCenterApp(callerName, pkgName)) {
            return DEBUG;
        }
        return true;
    }

    public void uploadInstallAppInfos(Context context, PackageParser.Package pkg, String installerPackageName) {
        ColorPackageManagerHelper.uploadInstallAppInfos(context, pkg, installerPackageName);
    }

    public boolean isNewUserInstallPkg(String packageName) {
        return ColorPackageManagerHelper.isNewUserInstallPkg(packageName);
    }

    public boolean isNewUserNotInstallPkg(String packageName) {
        return ColorPackageManagerHelper.isNewUserNotInstallPkg(packageName);
    }

    public void sortSystemAppInData(List<ResolveInfo> result) {
        if (result.size() > 1) {
            Collections.sort(result, mSystemAppPrioritySorter);
        }
    }

    public boolean duplicatePermCheck(OppoBasePackageManagerService.InstallArgsEx installArgsEx) {
        boolean duplicatePermInstall = true;
        if ((installArgsEx.getExtraInstallFlags() & 1) != 0 && this.mPms.hasSystemFeature("oppo.feature.duplicate.permission.install", 0)) {
            duplicatePermInstall = false;
        }
        return duplicatePermInstall;
    }

    public void sendUpdateExtraAppInfoMessage(String packageName, Handler handler) {
        OppoMarketHelper.ExtraApplicationInfo extraAppInfo = new OppoMarketHelper.ExtraApplicationInfo();
        extraAppInfo.setPackageName(packageName);
        extraAppInfo.setOperateFlag("del");
        Message mExtraAppMsg = handler.obtainMessage(199);
        mExtraAppMsg.obj = extraAppInfo;
        handler.sendMessage(mExtraAppMsg);
    }

    public void sendUpdateExtraAppInfoMessage(PackageManagerService.PackageInstalledInfo res, OppoBasePackageManagerService.InstallArgsEx installArgsEx, PackageManagerService.InstallArgs args, Handler handler) {
        if (res.returnCode == 1 && args != null) {
            OppoMarketHelper.ExtraApplicationInfo extraAppInfo = new OppoMarketHelper.ExtraApplicationInfo();
            String packageName = res.name;
            if (packageName != null) {
                if (PackageManagerService.DEBUG_INSTALL) {
                    Slog.d(TAG, "processPendingInstall packageName= " + packageName);
                    Slog.d(TAG, "processPendingInstall args.installerPackageName= " + args.installerPackageName);
                }
                extraAppInfo.setPackageName(packageName);
            }
            try {
                String installerName = args.installerPackageName;
                String extraInfo = installArgsEx != null ? installArgsEx.extraSessionInfo : "";
                extraAppInfo.setInstallerName(installerName);
                extraAppInfo.setExtraAppInfo(extraInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Message mExtraAppMsg = handler.obtainMessage(199);
            extraAppInfo.setOperateFlag("add");
            mExtraAppMsg.obj = extraAppInfo;
            handler.sendMessage(mExtraAppMsg);
        }
    }

    public void handleOpooMarketMesage(Message msg) {
        OppoMarketHelper.ExtraApplicationInfoManager extraApplicationInfoManager = this.mExtraApplicationInfoManager;
        if (extraApplicationInfoManager != null) {
            extraApplicationInfoManager.handleMessage(msg);
        }
    }
}
