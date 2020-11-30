package com.android.server.pm;

import android.util.Slog;
import java.util.List;

public class ColorLanguageEnableManager implements IColorLanguageEnableManager {
    private static final String TAG = "ColorLanguageEnableManager";
    private static volatile ColorLanguageEnableManager sInstance = null;
    private PackageManagerService mPms = null;
    private IColorPackageManagerServiceEx mPmsEx = null;

    private ColorLanguageEnableManager() {
    }

    public static ColorLanguageEnableManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorLanguageEnableManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorLanguageEnableManager();
                }
            }
        }
        return sInstance;
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        this.mPmsEx = pmsEx;
        IColorPackageManagerServiceEx iColorPackageManagerServiceEx = this.mPmsEx;
        if (iColorPackageManagerServiceEx != null) {
            this.mPms = iColorPackageManagerServiceEx.getPackageManagerService();
        }
    }

    public boolean setEnabledLanguagePackages(int userId, String targetPackageName, List<String> overlayPackagePath) {
        synchronized (this.mPms.mPackages) {
            if (targetPackageName != null) {
                if (this.mPms.mPackages.get(targetPackageName) != null) {
                    ((PackageSetting) this.mPms.mSettings.mPackages.get(targetPackageName)).setOverlayPaths(overlayPackagePath, userId);
                    return true;
                }
            }
            Slog.e(TAG, "failed to find package " + targetPackageName);
            return false;
        }
    }
}
