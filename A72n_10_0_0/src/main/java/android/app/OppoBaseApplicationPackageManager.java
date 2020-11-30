package android.app;

import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IOppoPackageManager;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageManager;
import android.content.pm.OppoPackageManager;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorBaseResources;
import android.content.res.IColorThemeManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.util.UserIcons;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.color.content.ColorRuleInfo;
import com.color.multiapp.ColorMultiAppManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class OppoBaseApplicationPackageManager extends PackageManager {
    private static final String TAG = "OppoBaseApplicationPackageManager";
    private static HashMap<String, Bitmap> mActivityIconsCache = new HashMap<>();
    private static HashMap<String, Bitmap> mAppIconsCache = new HashMap<>();
    private static boolean mIconCacheDirty = false;
    private static final int sDefaultFlags = 1024;
    private final ContextImpl mContext;
    private final IOppoPackageManager mOppoPm;
    private final PackageDeleteObserver mPackageDeleleteObserver = new PackageDeleteObserver();

    public abstract Drawable getCachedIconForThemeHelper(String str, int i);

    public abstract ColorBaseResources getColorBaseResourcesForThemeHelper(ApplicationInfo applicationInfo) throws PackageManager.NameNotFoundException;

    /* access modifiers changed from: package-private */
    public abstract UserManager getUserManager();

    public abstract void putCachedIconForThemeHelper(String str, int i, Drawable drawable);

    protected OppoBaseApplicationPackageManager(ContextImpl context, IPackageManager pm) {
        this.mContext = context;
        this.mOppoPm = new OppoPackageManager(context);
    }

    /* access modifiers changed from: protected */
    public void checkAndLogMultiApp(boolean print, Context context, Object name, String tag) {
        ColorMultiAppManager.getInstance().checkAndLogMultiApp(print, context, name, tag);
    }

    /* access modifiers changed from: protected */
    public Drawable getMultiAppUserBadgedIcon(UserHandle user) {
        if (user == null || 999 != user.getIdentifier()) {
            return null;
        }
        return this.mContext.getResources().getDrawable(201851147);
    }

    public Bitmap getAppIconBitmap(String packageName) {
        try {
            return this.mOppoPm.getAppIconBitmap(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Map<String, Bitmap> getAppIconsCache(boolean compress) {
        try {
            return this.mOppoPm.getAppIconsCache(compress);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationIconCacheAll(ApplicationInfo info) {
        return info.loadIcon(this);
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationIconCache(ApplicationInfo info) {
        return info.loadIcon(this);
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationIconCache(String packageName) throws PackageManager.NameNotFoundException {
        return getApplicationIcon(getApplicationInfo(packageName, 1024));
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationIconCacheOrignal(ApplicationInfo info) {
        return info.loadIcon(this);
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationIconCacheOrignal(String packageName) throws PackageManager.NameNotFoundException {
        return getApplicationIcon(getApplicationInfo(packageName, 1024));
    }

    @Override // android.content.pm.PackageManager
    public Drawable getActivityIconCache(ComponentName componentName) throws PackageManager.NameNotFoundException {
        return getActivityInfo(componentName, 1024).loadIcon(this);
    }

    public Map<String, Bitmap> getActivityIconsCache(IPackageDeleteObserver observer) {
        try {
            return this.mOppoPm.getActivityIconsCache(observer);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    private class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        private PackageDeleteObserver() {
        }

        @Override // android.content.pm.IPackageDeleteObserver
        public void packageDeleted(String packageName, int returnCode) {
            if (packageName != null) {
                try {
                    if (OppoBaseApplicationPackageManager.mAppIconsCache.get(packageName) != null) {
                        OppoBaseApplicationPackageManager.mAppIconsCache.remove(packageName);
                    }
                    ArrayList<String> deleteList = new ArrayList<>();
                    for (Map.Entry entry : OppoBaseApplicationPackageManager.mActivityIconsCache.entrySet()) {
                        String key = (String) entry.getKey();
                        if (packageName.equals(key.split("/")[0])) {
                            deleteList.add(key);
                        }
                    }
                    Iterator<String> it = deleteList.iterator();
                    while (it.hasNext()) {
                        OppoBaseApplicationPackageManager.mActivityIconsCache.remove(it.next());
                    }
                    boolean unused = OppoBaseApplicationPackageManager.mIconCacheDirty = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override // android.content.pm.PackageManager
    public Drawable getApplicationIcon(ApplicationInfo info) {
        try {
            Bitmap bitmap = getAppIconBitmap(info.packageName);
            if (bitmap != null) {
                return new BitmapDrawable(this.mContext.getResources(), bitmap);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override // android.content.pm.PackageManager
    public int oppoFreezePackage(String pkgName, int userId, int freezeFlag, int flag) {
        try {
            return this.mOppoPm.oppoFreezePackage(pkgName, userId, freezeFlag, flag, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    @Override // android.content.pm.PackageManager
    public int oppoUnFreezePackage(String pkgName, int userId, int freezeFlag, int flag) {
        try {
            return this.mOppoPm.oppoUnFreezePackage(pkgName, userId, freezeFlag, flag, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    @Override // android.content.pm.PackageManager
    public int getOppoFreezePackageState(String pkgName, int userId) {
        try {
            return this.mOppoPm.getOppoFreezePackageState(pkgName, userId);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean inOppoFreezePackageList(String pkgName, int userId) {
        try {
            return this.mOppoPm.inOppoFreezePackageList(pkgName, userId);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    @Override // android.content.pm.PackageManager
    public List<String> getOppoFreezedPackageList(int userId) {
        try {
            return this.mOppoPm.getOppoFreezedPackageList(userId);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    @Override // android.content.pm.PackageManager
    public int getOppoPackageFreezeFlag(String pkgName, int userId) {
        try {
            return this.mOppoPm.getOppoPackageFreezeFlag(pkgName, userId);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    static void oppoConfigurationChanged() {
        if (!mAppIconsCache.isEmpty()) {
            mAppIconsCache.clear();
        }
        if (!mActivityIconsCache.isEmpty()) {
            mActivityIconsCache.clear();
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isSecurePayApp(String pkg) {
        try {
            return this.mOppoPm.isSecurePayApp(pkg);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean setInterceptRuleInfos(List<ColorRuleInfo> infos) {
        try {
            return this.mOppoPm.setInterceptRuleInfos(infos);
        } catch (RemoteException e) {
            Log.e(TAG, "setInterceptRuleInfos failed");
            return false;
        }
    }

    @Override // android.content.pm.PackageManager
    public List<ColorRuleInfo> getInterceptRuleInfos() {
        try {
            return this.mOppoPm.getInterceptRuleInfos();
        } catch (RemoteException e) {
            Log.e(TAG, "getInterceptRuleInfos failed");
            return null;
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isFullFunctionMode() {
        try {
            return this.mOppoPm.isClosedSuperFirewall();
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isClosedSuperFirewall() {
        try {
            return this.mOppoPm.isClosedSuperFirewall();
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean loadRegionFeature(String name) {
        try {
            return this.mOppoPm.loadRegionFeature(name);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    @Override // android.content.pm.PackageManager
    public FeatureInfo[] getOppoSystemAvailableFeatures() {
        try {
            return this.mOppoPm.getOppoSystemAvailableFeatures();
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    @Override // android.content.pm.PackageManager
    public boolean isSystemDataApp(String packageName) {
        try {
            return this.mOppoPm.isSystemDataApp(packageName);
        } catch (RemoteException e) {
            throw new RuntimeException("Package manager has died", e);
        }
    }

    @Override // android.content.pm.PackageManager
    public Drawable loadItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo, boolean isConvertEnable) {
        if (itemInfo.showUserIcon != -10000) {
            Bitmap bitmap = getUserManager().getUserIcon(itemInfo.showUserIcon);
            if (bitmap == null) {
                return UserIcons.getDefaultUserIcon(this.mContext.getResources(), itemInfo.showUserIcon, false);
            }
            return new BitmapDrawable(bitmap);
        }
        Drawable dr = null;
        if (!(itemInfo.packageName == null || itemInfo.icon == 0)) {
            dr = (!isConvertEnable || !((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).supportUxOnline(this, this.mContext.getPackageName())) ? getDrawable(itemInfo.packageName, itemInfo.icon, appInfo) : ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).supportUxIcon(this, appInfo, itemInfo.packageName) ? ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).getDrawableFromUxIcon(this, itemInfo.packageName, itemInfo.icon, appInfo, false) : OppoThemeHelper.getDrawable(this, itemInfo.packageName, itemInfo.icon, appInfo, itemInfo.name);
        }
        if (dr == null) {
            dr = itemInfo.loadDefaultIcon(this);
        }
        return getUserBadgedIcon(dr, new UserHandle(this.mContext.getUserId()));
    }

    /* access modifiers changed from: protected */
    public IPackageInstaller antiVirusQihooReplaceInstaller(IPackageInstaller tmpObj) {
        Field fieldMRemote;
        try {
            if (((IColorAntiVirusBehaviorManager) OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0])).checkNeedReplace("packageinstaller") && (fieldMRemote = tmpObj.getClass().getDeclaredField("mRemote")) != null) {
                fieldMRemote.setAccessible(true);
                fieldMRemote.set(tmpObj, ((IColorAntiVirusBehaviorManager) OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0])).getOrCreateFakeBinder((IBinder) fieldMRemote.get(tmpObj), "packageinstaller"));
                fieldMRemote.setAccessible(false);
            }
            return tmpObj;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } catch (Throwable th) {
        }
        return tmpObj;
    }

    @Override // android.content.pm.PackageManager
    public Drawable getUxIconDrawable(Drawable src, boolean isForegroundDrawable) {
        return ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).getDrawableForApp(src, isForegroundDrawable);
    }

    @Override // android.content.pm.PackageManager
    public Drawable getUxIconDrawable(String packageName, Drawable src, boolean isForegroundDrawable) {
        try {
            return ((IColorThemeManager) OppoFeatureCache.getOrCreate(IColorThemeManager.DEFAULT, new Object[0])).getDrawableForApp(getResourcesForApplication(packageName), getColorBaseResourcesForThemeHelper(getApplicationInfo(packageName, 1024)), src, isForegroundDrawable);
        } catch (PackageManager.NameNotFoundException ex) {
            Log.e(TAG, "getResourcesForApplication error: " + ex.getMessage());
            return src;
        }
    }

    @Override // android.content.pm.PackageManager
    public void checkEMMApkRuntimePermission(ComponentName cn2) throws SecurityException {
        if (cn2.getPackageName() == null) {
            throw new SecurityException("Package name is null");
        }
    }
}
