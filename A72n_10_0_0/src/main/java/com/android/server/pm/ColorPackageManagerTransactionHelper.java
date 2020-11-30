package com.android.server.pm;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDeleteObserver;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;
import com.android.server.wm.IColorIntentInterceptManager;
import com.color.content.ColorRemovableAppInfo;
import com.color.content.ColorRuleInfo;
import java.util.List;
import java.util.Map;

public final class ColorPackageManagerTransactionHelper extends ColorPackageManagerCommonHelper {
    private static final String TAG = "ColorPackageManagerTransactionHelper";

    public ColorPackageManagerTransactionHelper(Context context, PackageManagerService pms) {
        super(context, pms);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        IntentSender sender;
        Bundle bundle;
        boolean compress = false;
        switch (code) {
            case 10002:
                data.enforceInterface("android.app.IPackageManager");
                boolean isCloseSuperFirewall = isCloseSuperFirewall();
                reply.writeNoException();
                reply.writeString(String.valueOf(isCloseSuperFirewall));
                return true;
            case 10003:
                data.enforceInterface("android.app.IPackageManager");
                setClosedSuperFirewall(Boolean.valueOf(data.readString()).booleanValue());
                reply.writeNoException();
                return true;
            case 10004:
                data.enforceInterface("android.app.IPackageManager");
                Bitmap result = getAppIconBitmap(data.readString());
                reply.writeNoException();
                if (result != null) {
                    reply.writeInt(1);
                    result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case 10005:
                data.enforceInterface("android.app.IPackageManager");
                if (data.readInt() != 0) {
                    compress = true;
                }
                Map result2 = getAppIconsCache(compress);
                reply.writeNoException();
                reply.writeMap(result2);
                return true;
            case 10006:
                data.enforceInterface("android.app.IPackageManager");
                Map result3 = getActivityIconsCache(IPackageDeleteObserver.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeMap(result3);
                return true;
            case 10007:
                data.enforceInterface("android.app.IPackageManager");
                boolean prohibitChildInstallation = prohibitChildInstallation(data.readInt(), data.readBoolean());
                reply.writeNoException();
                reply.writeInt(prohibitChildInstallation ? 1 : 0);
                return true;
            case 10008:
                data.enforceInterface("android.app.IPackageManager");
                int result4 = oppoFreezePackage(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeInt(result4);
                return true;
            case 10009:
                data.enforceInterface("android.app.IPackageManager");
                int result5 = oppoUnFreezePackage(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeInt(result5);
                return true;
            case 10010:
                data.enforceInterface("android.app.IPackageManager");
                int _result = getOppoFreezePackageState(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            case 10011:
                data.enforceInterface("android.app.IPackageManager");
                boolean inOppoFreezePackageList = inOppoFreezePackageList(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(inOppoFreezePackageList ? 1 : 0);
                return true;
            case 10012:
                data.enforceInterface("android.app.IPackageManager");
                List<String> _result2 = getOppoFreezedPackageList(data.readInt());
                reply.writeNoException();
                reply.writeStringList(_result2);
                return true;
            case 10013:
                data.enforceInterface("android.app.IPackageManager");
                int _result3 = getOppoPackageFreezeFlag(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            case 10014:
                data.enforceInterface("android.app.IPackageManager");
                boolean loadRegionFeature = loadRegionFeature(data.readString());
                reply.writeNoException();
                reply.writeInt(loadRegionFeature ? 1 : 0);
                return true;
            case 10015:
                data.enforceInterface("android.app.IPackageManager");
                FeatureInfo[] result6 = getOppoSystemAvailableFeatures();
                reply.writeNoException();
                reply.writeTypedArray(result6, 1);
                return true;
            case 10016:
                data.enforceInterface("android.app.IPackageManager");
                boolean isSecurePayApp = isSecurePayApp(data.readString());
                reply.writeNoException();
                reply.writeInt(isSecurePayApp ? 1 : 0);
                return true;
            case 10017:
                data.enforceInterface("android.app.IPackageManager");
                boolean isSystemDataApp = isSystemDataApp(data.readString());
                reply.writeNoException();
                reply.writeInt(isSystemDataApp ? 1 : 0);
                return true;
            case 10018:
                data.enforceInterface("android.app.IPackageManager");
                boolean interceptRuleInfos = setInterceptRuleInfos(data.createTypedArrayList(ColorRuleInfo.CREATOR));
                reply.writeNoException();
                reply.writeInt(interceptRuleInfos ? 1 : 0);
                return true;
            case 10019:
                data.enforceInterface("android.app.IPackageManager");
                List<ColorRuleInfo> _result4 = getInterceptRuleInfos();
                reply.writeNoException();
                reply.writeTypedList(_result4);
                return true;
            case 10020:
                data.enforceInterface("android.app.IPackageManager");
                boolean inPmsWhiteList = inPmsWhiteList(data.readInt(), data.readString(), data.createStringArrayList());
                reply.writeNoException();
                reply.writeInt(inPmsWhiteList ? 1 : 0);
                return true;
            case 10021:
                data.enforceInterface("android.app.IPackageManager");
                List<String> result7 = getRemovableAppList();
                reply.writeNoException();
                reply.writeStringList(result7);
                return true;
            case 10022:
                data.enforceInterface("android.app.IPackageManager");
                List<ColorRemovableAppInfo> result8 = getRemovedAppInfos();
                reply.writeNoException();
                reply.writeTypedList(result8);
                return true;
            case 10023:
                data.enforceInterface("android.app.IPackageManager");
                List<ColorRemovableAppInfo> result9 = getRemovableAppInfos();
                reply.writeNoException();
                reply.writeTypedList(result9);
                return true;
            case 10024:
                data.enforceInterface("android.app.IPackageManager");
                ColorRemovableAppInfo result10 = getRemovableAppInfo(data.readString());
                reply.writeNoException();
                if (result10 != null) {
                    reply.writeInt(1);
                    result10.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case 10025:
                data.enforceInterface("android.app.IPackageManager");
                String packageName = data.readString();
                if (data.readInt() != 0) {
                    sender = (IntentSender) IntentSender.CREATOR.createFromParcel(data);
                } else {
                    sender = null;
                }
                if (data.readInt() != 0) {
                    bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    bundle = null;
                }
                boolean restoreRemovableApp = restoreRemovableApp(packageName, sender, bundle);
                reply.writeNoException();
                reply.writeInt(restoreRemovableApp ? 1 : 0);
                return true;
            case 10026:
                data.enforceInterface("android.app.IPackageManager");
                data.readString();
                reply.writeNoException();
                reply.writeString("Peimission Denized. Deprecated API");
                return true;
            case 10027:
                data.enforceInterface("android.app.IPackageManager");
                boolean support = isSupportSessionWrite();
                reply.writeNoException();
                reply.writeString(String.valueOf(support));
                return true;
            default:
                return false;
        }
    }

    public boolean isCloseSuperFirewall() {
        return OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall();
    }

    public void setClosedSuperFirewall(boolean mode) {
        if (DEBUG) {
            Slog.v(TAG, "setAppCallRefuseMode mode = " + mode);
        }
        OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).setClosedSuperFirewall(mode);
    }

    public Bitmap getAppIconBitmap(String packageName) {
        if (DEBUG) {
            Slog.v(TAG, "getAppIconBitmap:" + packageName);
        }
        return OppoFeatureCache.get(IColorIconCachesManager.DEFAULT).getAppIconBitmap(packageName);
    }

    public Map getAppIconsCache(boolean compress) {
        return OppoFeatureCache.get(IColorIconCachesManager.DEFAULT).getAppIconsCache(compress);
    }

    public Map getActivityIconsCache(IPackageDeleteObserver observer) {
        if (DEBUG) {
            Slog.v(TAG, "getActivityIconsCache:" + observer);
        }
        return OppoFeatureCache.get(IColorIconCachesManager.DEFAULT).getActivityIconsCache(observer);
    }

    public boolean prohibitChildInstallation(int userId, boolean isInstall) {
        if (DEBUG) {
            Slog.v(TAG, "prohibitChildInstallation");
        }
        return OppoFeatureCache.get(IColorChildrenModeInstallManager.DEFAULT).prohibitChildInstallation(userId, isInstall);
    }

    public int oppoFreezePackage(String pkgName, int userId, int freezeFlag, int flag, String callingPkg) {
        if (DEBUG) {
            Slog.v(TAG, "oppoFreezePackage, callingPkg:" + callingPkg);
        }
        return OppoFeatureCache.get(IColorAppQuickFreezeManager.DEFAULT).oppoFreezePackage(pkgName, userId, freezeFlag, flag, callingPkg);
    }

    public int oppoUnFreezePackage(String pkgName, int userId, int freezeFlag, int flag, String callingPkg) {
        if (DEBUG) {
            Slog.v(TAG, "oppoUnFreezePackage, callingPkg:" + callingPkg);
        }
        return OppoFeatureCache.get(IColorAppQuickFreezeManager.DEFAULT).oppoUnFreezePackage(pkgName, userId, freezeFlag, flag, callingPkg);
    }

    public int getOppoFreezePackageState(String pkgName, int userId) {
        return OppoFeatureCache.get(IColorAppQuickFreezeManager.DEFAULT).getOppoFreezePackageState(pkgName, userId);
    }

    public boolean inOppoFreezePackageList(String pkgName, int userId) {
        return OppoFeatureCache.get(IColorAppQuickFreezeManager.DEFAULT).inOppoFreezePackageList(pkgName, userId);
    }

    public List<String> getOppoFreezedPackageList(int userId) {
        return OppoFeatureCache.get(IColorAppQuickFreezeManager.DEFAULT).getOppoFreezedPackageList(userId);
    }

    public int getOppoPackageFreezeFlag(String pkgName, int userId) {
        return OppoFeatureCache.get(IColorAppQuickFreezeManager.DEFAULT).getOppoPackageFreezeFlag(pkgName, userId);
    }

    public boolean loadRegionFeature(String name) {
        if (DEBUG) {
            Slog.v(TAG, "loadRegionFeature:" + name);
        }
        return OppoFeatureCache.get(IColorDynamicFeatureManager.DEFAULT).loadRegionFeature(name);
    }

    public FeatureInfo[] getOppoSystemAvailableFeatures() {
        if (DEBUG) {
            Slog.v(TAG, "getOppoSystemAvailableFeatures");
        }
        return OppoFeatureCache.get(IColorDynamicFeatureManager.DEFAULT).getOppoSystemAvailableFeatures();
    }

    public boolean isSecurePayApp(String packageName) {
        if (DEBUG) {
            Slog.v(TAG, "isSecurePayApp");
        }
        return OppoFeatureCache.get(IColorSecurePayManager.DEFAULT).isSecurePayApp(packageName);
    }

    public boolean setInterceptRuleInfos(List<ColorRuleInfo> infos) {
        if (DEBUG) {
            Slog.v(TAG, "setInterceptRuleInfos");
        }
        return OppoFeatureCache.get(IColorIntentInterceptManager.DEFAULT).setInterceptRuleInfos(infos);
    }

    public List<ColorRuleInfo> getInterceptRuleInfos() {
        if (DEBUG) {
            Slog.v(TAG, "getInterceptRuleInfos");
        }
        return OppoFeatureCache.get(IColorIntentInterceptManager.DEFAULT).getInterceptRuleInfos();
    }

    public boolean isSystemDataApp(String packageName) {
        return ColorPackageManagerHelper.isSystemDataApp(packageName);
    }

    public boolean inPmsWhiteList(int type, String verifyStr, List<String> defaultList) {
        return ColorPackageManagerHelper.inPmsWhiteList(type, verifyStr, defaultList);
    }

    public List<String> getRemovableAppList() {
        if (DEBUG) {
            Slog.v(TAG, "getRemovableAppList");
        }
        return OppoFeatureCache.get(IColorRemovableAppManager.DEFAULT).getRemovableAppList();
    }

    public List<ColorRemovableAppInfo> getRemovedAppInfos() {
        if (DEBUG) {
            Slog.v(TAG, "not support getRemovedAppInfos");
        }
        return OppoFeatureCache.get(IColorRemovableAppManager.DEFAULT).getRemovedAppInfos();
    }

    public List<ColorRemovableAppInfo> getRemovableAppInfos() {
        if (DEBUG) {
            Slog.v(TAG, "getRemovableAppInfos");
        }
        return OppoFeatureCache.get(IColorRemovableAppManager.DEFAULT).getRemovableAppInfos();
    }

    public ColorRemovableAppInfo getRemovableAppInfo(String packageName) {
        if (DEBUG) {
            Slog.v(TAG, "getRemovableAppInfo");
        }
        return OppoFeatureCache.get(IColorRemovableAppManager.DEFAULT).getRemovableAppInfo(packageName);
    }

    public boolean restoreRemovableApp(String packageName, IntentSender sender, Bundle bundle) {
        if (!DEBUG) {
            return false;
        }
        Slog.v(TAG, "not support restoreRemovableApp");
        return false;
    }

    public boolean isSupportSessionWrite() {
        return OppoFeatureCache.get(IColorPmsSupportedFunctionManager.DEFAULT).isSupportSessionWrite();
    }
}
