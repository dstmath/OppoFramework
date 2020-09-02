package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColorThirdPartyAppSignCheckManager implements IColorThirdPartyAppSignCheckManager {
    public static final String TAG = "ColorThirdPartyAppSignCheckManager";
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    private static ColorThirdPartyAppSignCheckManager sThirdPartyAppSignCheckManager = null;
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    Context mContext;
    boolean mDynamicDebug = false;
    private PackageManagerService mPms = null;

    private ColorThirdPartyAppSignCheckManager() {
    }

    public static ColorThirdPartyAppSignCheckManager getInstance() {
        if (sThirdPartyAppSignCheckManager == null) {
            sThirdPartyAppSignCheckManager = new ColorThirdPartyAppSignCheckManager();
        }
        return sThirdPartyAppSignCheckManager;
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        this.mPms = pmsEx.getPackageManagerService();
        this.mContext = pmsEx.getContext();
    }

    public boolean isIllegalAppNameAsOppoPackage(PackageParser.Package pkg, String installerPackageName, int installFlags) {
        boolean isDebugMode = SystemProperties.getBoolean("persist.sys.oppo.debug", false);
        boolean isOppoApp = ColorPackageManagerHelper.isOppoApkListEmpty() || ColorPackageManagerHelper.isOppoApkList(pkg.packageName);
        if (pkg.mSigningDetails != null && !isDebugMode && pkg.packageName != null && !isOppoApp && !ColorPackageManagerHelper.isShopPackageName(installerPackageName) && ColorPackageManagerHelper.isOppoPackageName(pkg.packageName) && PackageManagerServiceUtils.compareSignatures(this.mPms.mPlatformPackage.mSigningDetails.signatures, pkg.mSigningDetails.signatures) != 0 && !ColorPackageManagerHelper.isOppoApkSignature(pkg)) {
            synchronized (this.mPms.mPackages) {
                boolean isNewInstall = true;
                if ((installFlags & 2) != 0) {
                    try {
                        String oldName = this.mPms.mSettings.getRenamedPackageLPr(pkg.packageName);
                        if (pkg.mOriginalPackages != null && pkg.mOriginalPackages.contains(oldName) && this.mPms.mPackages.containsKey(oldName)) {
                            isNewInstall = false;
                        } else if (this.mPms.mPackages.containsKey(pkg.packageName)) {
                            isNewInstall = false;
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                if (isNewInstall) {
                    boolean matchSignature = false;
                    int size = this.mPms.mSettings.mPackages.size();
                    int i = 0;
                    while (true) {
                        if (i >= size) {
                            break;
                        }
                        PackageSetting ps = (PackageSetting) this.mPms.mSettings.mPackages.valueAt(i);
                        if (!(ps == null || ps.pkg == null)) {
                            if (ps.pkg.mSigningDetails != null) {
                                if (ColorPackageManagerHelper.isOppoPackageName(ps.pkg.packageName) && PackageManagerServiceUtils.compareSignatures(ps.pkg.mSigningDetails.signatures, pkg.mSigningDetails.signatures) == 0) {
                                    if (PackageManagerService.DEBUG_INSTALL) {
                                        Slog.d(TAG, "matchedPackage:" + ps.pkg.packageName + ",matchedSignature:" + ColorPackageManagerHelper.computePackageCertDigest(ps.pkg));
                                    }
                                    matchSignature = true;
                                }
                            }
                        }
                        i++;
                    }
                    if (!matchSignature) {
                        if (PackageManagerService.DEBUG_INSTALL) {
                            Slog.d(TAG, "match platform app Signature check error :" + pkg.packageName + ",isOppoApp:" + isOppoApp + ",isNewInstall:" + isNewInstall);
                        }
                        ColorPackageManagerHelper.uploadForbiddenInstallDcs(this.mContext, pkg);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isForbidInstallAppByCert(PackageParser.Package pkg) {
        return ColorPackageManagerHelper.isForbidInstallAppByCert(pkg);
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog#### mDynamicDebug = " + this.mDynamicDebug);
        setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + this.mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorThirdPartyAppSignCheckManager.class.getName());
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }
}
