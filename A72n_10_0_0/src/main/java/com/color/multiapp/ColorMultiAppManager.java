package com.color.multiapp;

import android.app.AppGlobals;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Singleton;
import com.android.internal.app.ResolverActivity;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class ColorMultiAppManager {
    public static final String ACTION_MULTI_APP_PACKAGE_ADDED = "oppo.intent.action.MULTI_APP_PACKAGE_ADDED";
    public static final String ACTION_MULTI_APP_PACKAGE_REMOVED = "oppo.intent.action.MULTI_APP_PACKAGE_REMOVED";
    public static final String ACTION_MULTI_APP_USER_UNLOCKED = "oppo.intent.action.MULTI_APP_USER_UNLOCKED";
    public static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String EXTERNAL_PRIMARY_MULTIAPP_PATH = "/storage/emulated/999";
    public static final String FEATURE_OPPO_MULTIAPP = "oppo.multiapp.support";
    public static final String MEDIA_PROVIDER_PACKAGE_NAME = "com.android.providers.media";
    public static final String MULTI_TAG = "com.multiple.launcher";
    private static final String TAG = ColorMultiAppManager.class.getSimpleName();
    public static final int USER_FLAG_MULTI_APP = 67108864;
    public static final int USER_ID_MULTI_APP = 999;
    public static final int USER_ID_ORIGINAL = 0;
    private static final Singleton<ColorMultiAppManager> sColorMultiAppManagerSingleton = new Singleton<ColorMultiAppManager>() {
        /* class com.color.multiapp.ColorMultiAppManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public ColorMultiAppManager create() {
            return new ColorMultiAppManager();
        }
    };
    private IColorMultiApp colorMultiApp;

    private ColorMultiAppManager() {
        this.colorMultiApp = ColorMultiAppFactory.getInstance().getColorMultiApp();
    }

    public static ColorMultiAppManager getInstance() {
        return sColorMultiAppManagerSingleton.get();
    }

    public List<String> getAllowedMultiApp() {
        return this.colorMultiApp.getAllowedMultiApp();
    }

    public List<String> getCreatedMultiApp() {
        return this.colorMultiApp.getCreatedMultiApp();
    }

    public String getAliasMultiApp(String pkgName) {
        return this.colorMultiApp.getAliasMultiApp(pkgName);
    }

    public boolean isCreatedMultiApp(String pkgName) {
        return this.colorMultiApp.isCreatedMultiApp(pkgName);
    }

    public boolean isMultiAppSupport() {
        return this.colorMultiApp.isSupportMultiApp();
    }

    public boolean isMultiAppUserId(int userId) {
        return this.colorMultiApp.isMultiAppUserId(userId);
    }

    public boolean isMultiAppUri(Intent intent, String pkgName) {
        return this.colorMultiApp.isMultiAppUri(intent, pkgName);
    }

    public int getCorrectUserId(int userId) {
        return this.colorMultiApp.getCorrectUserId(userId);
    }

    public boolean isSupportMultiApp() {
        return this.colorMultiApp.isSupportMultiApp();
    }

    public void checkAndLogMultiApp(boolean print, Context context, Object name, String tag) {
        if (print && isSupportMultiApp() && context != null && isMultiAppUserId(context.getUserId())) {
            String str = TAG;
            Log.i(str, "multi app: " + tag + " is null! " + name + " ,pkg:" + context.getPackageName());
        }
    }

    public List<ApplicationInfo> getInstalledApplications(IPackageManager packageManager, int flags, int userId) throws RemoteException {
        return this.colorMultiApp.getInstalledApplications(packageManager, flags, userId);
    }

    public ProviderInfo resolveContentProviderAsUser(IPackageManager packageManager, Context context, String name, int flags, int userId) throws RemoteException {
        return this.colorMultiApp.resolveContentProviderAsUser(packageManager, context, name, flags, userId);
    }

    public UserHandle getCorrectUserHandle(UserHandle user, String packageName) {
        return this.colorMultiApp.getCorrectUserHandle(user, packageName);
    }

    public int fixApplicationInfo(int userId, PackageParser.Package pkg) {
        return this.colorMultiApp.fixApplicationInfo(userId, pkg);
    }

    public boolean addMultiAppInfo(Intent intent, List<ResolveInfo> from, List<ResolverActivity.ResolvedComponentInfo> into) {
        return this.colorMultiApp.addMultiAppInfo(intent, from, into);
    }

    public static File getMultiAppVolumePath(String volumeName) throws FileNotFoundException {
        if (MediaStore.VOLUME_EXTERNAL_PRIMARY.equals(volumeName)) {
            for (VolumeInfo volume : ((StorageManager) AppGlobals.getInitialApplication().getSystemService(StorageManager.class)).getVolumes()) {
                File path = volume.getPathForUser(999);
                if (path != null && path.toString().startsWith(EXTERNAL_PRIMARY_MULTIAPP_PATH)) {
                    return path;
                }
            }
        }
        throw new FileNotFoundException("Failed to find path for " + volumeName);
    }
}
