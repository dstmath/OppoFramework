package com.android.server.om;

import android.app.ActivityManager;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Slog;
import com.android.server.om.OppoBaseOverlayManagerService;
import com.android.server.pm.IColorLanguageEnableManager;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorLanguageManager implements IColorLanguageManager {
    private static final String APP_NAME_PATH = "/data/oppo/common/appNameChange";
    private static final String LANGUAGE_PATH = "/data/oppo/language";
    private static final String TAG = "ColorLanguageManager";
    private static volatile ColorLanguageManager sInstance = null;
    private Map<String, OppoBaseOverlayManagerService.Language> mAppNameMap = new HashMap();
    private Context mContext;
    private OverlayManagerServiceImpl mImpl;
    private Map<String, OppoBaseOverlayManagerService.Language> mLanguageMap = new HashMap();
    private Object mLock = null;
    private OppoBaseOverlayManagerService.IOppoOMSPackageCache mPackageManager;

    private ColorLanguageManager() {
    }

    public static ColorLanguageManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorLanguageManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorLanguageManager();
                }
            }
        }
        return sInstance;
    }

    public void init(OverlayManagerServiceImpl overlayManagerService, OppoBaseOverlayManagerService.IOppoOMSPackageCache packageManager, Object lock, Context context) {
        this.mImpl = overlayManagerService;
        this.mPackageManager = packageManager;
        this.mContext = context;
        this.mLock = lock;
        checkLanguageDir(ActivityManager.getCurrentUser());
    }

    private void checkLanguageDir(int userId) {
        checkLanguageDirForPath(userId, LANGUAGE_PATH);
        checkLanguageDirForPath(userId, APP_NAME_PATH);
    }

    private void checkLanguageDirForPath(int userId, String path) {
        File[] files;
        File dir = new File(path);
        if (dir.exists() && (files = dir.listFiles()) != null && files.length != 0) {
            for (File file : files) {
                addLanguagePath(file.getAbsolutePath(), userId);
            }
        }
    }

    private void addLanguagePath(String path, int userId) {
        synchronized (this.mLock) {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageArchiveInfo(path, 0);
            if (packageInfo == null) {
                Slog.d(TAG, "addLanguagePath packageInfo == null path: " + path);
                return;
            }
            packageInfo.applicationInfo.setBaseCodePath(path);
            this.mPackageManager.cachePackageInfo(packageInfo.packageName, userId, packageInfo);
            OppoBaseOverlayManagerService.Language language = new OppoBaseOverlayManagerService.Language();
            language.flag = true;
            language.overlay = packageInfo.packageName;
            if (path.startsWith(LANGUAGE_PATH)) {
                this.mLanguageMap.put(packageInfo.overlayTarget, language);
            } else if (path.startsWith(APP_NAME_PATH)) {
                this.mAppNameMap.put(packageInfo.overlayTarget, language);
            }
            this.mImpl.onOverlayPackageAdded(packageInfo.packageName, userId);
        }
    }

    public void setLanguageEnable(String path, int userId) {
        addLanguagePath(path, userId);
    }

    public void updateLanguagePath(String targetPackageName, int userId, Map<String, List<String>> pendingChanges) {
        updateLanguageForPath(this.mLanguageMap, targetPackageName, userId, pendingChanges);
        updateLanguageForPath(this.mAppNameMap, targetPackageName, userId, pendingChanges);
    }

    public int checkSignaturesMatching(String overlay, String target, int fulfilledPolicies, int flag) {
        if (this.mLanguageMap.get(target) != null && this.mLanguageMap.get(target).overlay.equals(overlay)) {
            fulfilledPolicies |= flag;
        }
        if (this.mAppNameMap.get(target) == null || !this.mAppNameMap.get(target).overlay.equals(overlay)) {
            return fulfilledPolicies;
        }
        return fulfilledPolicies | flag;
    }

    private void updateLanguageForPath(Map<String, OppoBaseOverlayManagerService.Language> map, String targetPackageName, int userId, Map<String, List<String>> pendingChanges) {
        if (!map.keySet().contains(targetPackageName)) {
            return;
        }
        if (map.get(targetPackageName).flag) {
            this.mImpl.setEnabled(map.get(targetPackageName).overlay, true, userId);
            map.get(targetPackageName).flag = false;
            return;
        }
        List<String> overlayPath = new ArrayList<>();
        for (String overlay : pendingChanges.get(targetPackageName)) {
            if (this.mPackageManager.getPackageInfo(overlay, userId) != null) {
                overlayPath.add(this.mPackageManager.getPackageInfo(overlay, userId).applicationInfo.getBaseCodePath());
            }
        }
        if (!OppoFeatureCache.get(IColorLanguageEnableManager.DEFAULT).setEnabledLanguagePackages(userId, targetPackageName, overlayPath)) {
            Slog.e(TAG, String.format("Failed to change lauguage enabled overlays for %s package %s user %d", overlayPath, targetPackageName, Integer.valueOf(userId)));
        }
    }
}
