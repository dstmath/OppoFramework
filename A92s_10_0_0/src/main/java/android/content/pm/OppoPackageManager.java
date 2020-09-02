package android.content.pm;

import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import com.color.content.ColorRemovableAppInfo;
import com.color.content.ColorRuleInfo;
import java.util.List;
import java.util.Map;

public class OppoPackageManager implements IOppoPackageManager {
    public static final int INSTALL_FROM_OPPO_ADB_INSTALLER = 268435456;
    public static final int INSTALL_SPEED_BACKGROUND = Integer.MIN_VALUE;
    public static final int INSTALL_SPEED_CPU_HIGH = 1073741824;
    public static final int INSTALL_SPEED_CPU_MID = 536870912;
    public static final int MATCH_OPPO_FREEZE_APP = 1073741824;
    public static final int OPPO_DONT_KILL_APP = 268435456;
    public static final int OPPO_FREEZE_FLAG_AUTO = 2;
    public static final int OPPO_FREEZE_FLAG_MANUAL = 1;
    public static final int OPPO_UNFREEZE_FLAG_NORMAL = 1;
    public static final int OPPO_UNFREEZE_FLAG_TEMP = 2;
    public static final int RE_INSTALL_DUPLICATE_PERMISSION = 1;
    public static final int STATE_OPPO_FREEZE_FREEZED = 2;
    public static final int STATE_OPPO_FREEZE_NORMAL = 0;
    public static final int STATE_OPPO_FREEZE_TEMP_UNFREEZED = 1;
    private static final String TAG = "OppoPackageManager";
    private final ColorPackageManager mColorPm;

    public OppoPackageManager(Context ctx) {
        this.mColorPm = new ColorPackageManager(ctx);
    }

    public OppoPackageManager() {
        this.mColorPm = new ColorPackageManager();
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean isClosedSuperFirewall() {
        try {
            return this.mColorPm.isClosedSuperFirewall();
        } catch (RemoteException e) {
            Log.e(TAG, "isClosedSuperFirewall failed");
            return false;
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public Bitmap getAppIconBitmap(String packageName) throws RemoteException {
        return this.mColorPm.getAppIconBitmap(packageName);
    }

    @Override // android.content.pm.IColorPackageManager
    public Map getAppIconsCache(boolean compress) throws RemoteException {
        return this.mColorPm.getAppIconsCache(compress);
    }

    @Override // android.content.pm.IColorPackageManager
    public Map getActivityIconsCache(IPackageDeleteObserver observer) throws RemoteException {
        return this.mColorPm.getActivityIconsCache(observer);
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean prohibitChildInstallation(int userId, boolean isInstall) throws RemoteException {
        return this.mColorPm.prohibitChildInstallation(userId, isInstall);
    }

    @Override // android.content.pm.IColorPackageManager
    public int oppoFreezePackage(String pkgName, int userId, int freezeFlag, int flag, String callingPkg) throws RemoteException {
        return this.mColorPm.oppoFreezePackage(pkgName, userId, freezeFlag, flag, callingPkg);
    }

    @Override // android.content.pm.IColorPackageManager
    public int oppoUnFreezePackage(String pkgName, int userId, int freezeFlag, int flag, String callingPkg) throws RemoteException {
        return this.mColorPm.oppoUnFreezePackage(pkgName, userId, freezeFlag, flag, callingPkg);
    }

    @Override // android.content.pm.IColorPackageManager
    public int getOppoFreezePackageState(String pkgName, int userId) throws RemoteException {
        return this.mColorPm.getOppoFreezePackageState(pkgName, userId);
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean inOppoFreezePackageList(String pkgName, int userId) throws RemoteException {
        return this.mColorPm.inOppoFreezePackageList(pkgName, userId);
    }

    @Override // android.content.pm.IColorPackageManager
    public List<String> getOppoFreezedPackageList(int userId) throws RemoteException {
        return this.mColorPm.getOppoFreezedPackageList(userId);
    }

    @Override // android.content.pm.IColorPackageManager
    public int getOppoPackageFreezeFlag(String pkgName, int userId) throws RemoteException {
        return this.mColorPm.getOppoPackageFreezeFlag(pkgName, userId);
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean loadRegionFeature(String name) throws RemoteException {
        return this.mColorPm.loadRegionFeature(name);
    }

    @Override // android.content.pm.IColorPackageManager
    public FeatureInfo[] getOppoSystemAvailableFeatures() throws RemoteException {
        return this.mColorPm.getOppoSystemAvailableFeatures();
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean isSecurePayApp(String name) throws RemoteException {
        return this.mColorPm.isSecurePayApp(name);
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean isSystemDataApp(String packageName) throws RemoteException {
        return this.mColorPm.isSystemDataApp(packageName);
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean inPmsWhiteList(int type, String verifyStr, List<String> defaultList) {
        try {
            return this.mColorPm.inPmsWhiteList(type, verifyStr, defaultList);
        } catch (RemoteException e) {
            Log.e(TAG, "inPmsWhiteList failed");
            return false;
        }
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean setInterceptRuleInfos(List<ColorRuleInfo> infos) throws RemoteException {
        return this.mColorPm.setInterceptRuleInfos(infos);
    }

    @Override // android.content.pm.IColorPackageManager
    public List<ColorRuleInfo> getInterceptRuleInfos() throws RemoteException {
        return this.mColorPm.getInterceptRuleInfos();
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean isFullFunctionMode() throws RemoteException {
        return this.mColorPm.isFullFunctionMode();
    }

    @Override // android.content.pm.IColorPackageManager
    public Drawable getApplicationIconCacheAll(ApplicationInfo info) {
        return this.mColorPm.getApplicationIconCacheAll(info);
    }

    @Override // android.content.pm.IColorPackageManager
    public Drawable getApplicationIconCache(ApplicationInfo info) {
        return this.mColorPm.getApplicationIconCache(info);
    }

    @Override // android.content.pm.IColorPackageManager
    public Drawable getApplicationIconCache(String packageName) throws PackageManager.NameNotFoundException {
        return this.mColorPm.getApplicationIconCache(packageName);
    }

    @Override // android.content.pm.IColorPackageManager
    public Drawable getApplicationIconCacheOrignal(ApplicationInfo info) {
        return this.mColorPm.getApplicationIconCacheOrignal(info);
    }

    @Override // android.content.pm.IColorPackageManager
    public Drawable getApplicationIconCacheOrignal(String packageName) throws PackageManager.NameNotFoundException {
        return this.mColorPm.getApplicationIconCacheOrignal(packageName);
    }

    @Override // android.content.pm.IColorPackageManager
    public Drawable getActivityIconCache(ComponentName componentName) throws PackageManager.NameNotFoundException {
        return this.mColorPm.getActivityIconCache(componentName);
    }

    @Override // android.content.pm.IColorPackageManager
    public List<String> getRemovableAppList() throws RemoteException {
        return this.mColorPm.getRemovableAppList();
    }

    @Override // android.content.pm.IColorPackageManager
    public List<ColorRemovableAppInfo> getRemovedAppInfos() throws RemoteException {
        return this.mColorPm.getRemovedAppInfos();
    }

    @Override // android.content.pm.IColorPackageManager
    public List<ColorRemovableAppInfo> getRemovableAppInfos() throws RemoteException {
        return this.mColorPm.getRemovableAppInfos();
    }

    @Override // android.content.pm.IColorPackageManager
    public ColorRemovableAppInfo getRemovableAppInfo(String packageName) throws RemoteException {
        return this.mColorPm.getRemovableAppInfo(packageName);
    }

    @Override // android.content.pm.IColorPackageManager
    public boolean restoreRemovableApp(String packageName, IntentSender sender, Bundle bundle) throws RemoteException {
        return this.mColorPm.restoreRemovableApp(packageName, sender, bundle);
    }

    @Override // android.content.pm.IColorPackageManager
    public void checkEMMApkRuntimePermission(ComponentName cn2) throws SecurityException {
        this.mColorPm.checkEMMApkRuntimePermission(cn2);
    }
}
