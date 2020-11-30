package android.content.pm;

import android.content.ComponentName;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import com.color.content.ColorRemovableAppInfo;
import com.color.content.ColorRuleInfo;
import java.util.List;
import java.util.Map;

public interface IColorPackageManager extends IColorBasePackageManager {
    public static final int ACTION_CHECK_MDM_PERM = 10026;
    public static final int GET_ACTIVITY_ICONS_CACHE_TRANSACTION = 10006;
    public static final int GET_APP_ICONS_CACHE_TRANSACTION = 10005;
    public static final int GET_APP_ICON_BITMAP_TRANSACTION = 10004;
    public static final int GET_INTERCEPT_RULE_INFOS = 10019;
    public static final int GET_OPPO_FREEZED_PACKAGE_LIST = 10012;
    public static final int GET_OPPO_FREEZE_PACKAGE_STATE = 10010;
    public static final int GET_OPPO_PACKAGE_FREEZE_FLAG = 10013;
    public static final int GET_OPPO_SYSTEM_AVAILABLE_FEATURES_TRANSACTION = 10015;
    public static final int GET_REMOVABLE_APP_INFO = 10024;
    public static final int GET_REMOVABLE_APP_INFOS = 10023;
    public static final int GET_REMOVABLE_APP_LIST = 10021;
    public static final int GET_REMOVED_APP_INFOS = 10022;
    public static final int IN_OPPO_FREEZE_PACKAGE_LIST = 10011;
    public static final int IN_PMS_WHITE_LIST = 10020;
    public static final int IS_CLOSE_SUPER_FIREWALL_TRANSACTION = 10002;
    public static final int IS_SECURE_PAY_APP = 10016;
    public static final int IS_SUPPORT_SESSION_WRITE = 10027;
    public static final int IS_SYSTEM_DATA_APP = 10017;
    public static final int LOAD_REGION_FEATURE_TRANSACTION = 10014;
    public static final int OPPO_FREEZE_PACKAGE = 10008;
    public static final int OPPO_UNFREEZE_PACKAGE = 10009;
    public static final int PROHIBIT_CHILD_INSTALLATION = 10007;
    public static final int RESTORE_REMOVABLE_APP = 10025;
    public static final int SET_CLOSE_SUPER_FIREWALL_TRANSACTION = 10003;
    public static final int SET_INTERCEPT_RULE_INFOS = 10018;

    void checkEMMApkRuntimePermission(ComponentName componentName) throws SecurityException;

    Drawable getActivityIconCache(ComponentName componentName) throws PackageManager.NameNotFoundException;

    Map getActivityIconsCache(IPackageDeleteObserver iPackageDeleteObserver) throws RemoteException;

    Bitmap getAppIconBitmap(String str) throws RemoteException;

    Map getAppIconsCache(boolean z) throws RemoteException;

    Drawable getApplicationIconCache(ApplicationInfo applicationInfo);

    Drawable getApplicationIconCache(String str) throws PackageManager.NameNotFoundException;

    Drawable getApplicationIconCacheAll(ApplicationInfo applicationInfo);

    Drawable getApplicationIconCacheOrignal(ApplicationInfo applicationInfo);

    Drawable getApplicationIconCacheOrignal(String str) throws PackageManager.NameNotFoundException;

    List<ColorRuleInfo> getInterceptRuleInfos() throws RemoteException;

    int getOppoFreezePackageState(String str, int i) throws RemoteException;

    List<String> getOppoFreezedPackageList(int i) throws RemoteException;

    int getOppoPackageFreezeFlag(String str, int i) throws RemoteException;

    FeatureInfo[] getOppoSystemAvailableFeatures() throws RemoteException;

    ColorRemovableAppInfo getRemovableAppInfo(String str) throws RemoteException;

    List<ColorRemovableAppInfo> getRemovableAppInfos() throws RemoteException;

    List<String> getRemovableAppList() throws RemoteException;

    List<ColorRemovableAppInfo> getRemovedAppInfos() throws RemoteException;

    boolean inOppoFreezePackageList(String str, int i) throws RemoteException;

    boolean inPmsWhiteList(int i, String str, List<String> list) throws RemoteException;

    boolean isClosedSuperFirewall() throws RemoteException;

    boolean isFullFunctionMode() throws RemoteException;

    boolean isSecurePayApp(String str) throws RemoteException;

    boolean isSupportSessionWrite() throws RemoteException;

    boolean isSystemDataApp(String str) throws RemoteException;

    boolean loadRegionFeature(String str) throws RemoteException;

    int oppoFreezePackage(String str, int i, int i2, int i3, String str2) throws RemoteException;

    int oppoUnFreezePackage(String str, int i, int i2, int i3, String str2) throws RemoteException;

    boolean prohibitChildInstallation(int i, boolean z) throws RemoteException;

    boolean restoreRemovableApp(String str, IntentSender intentSender, Bundle bundle) throws RemoteException;

    boolean setInterceptRuleInfos(List<ColorRuleInfo> list) throws RemoteException;
}
