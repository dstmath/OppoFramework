package android.content.res;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.util.TypedValue;
import java.io.InputStream;

public class ColorDummyThemeManager implements IColorThemeManager {
    private static volatile ColorDummyThemeManager sInstance = null;

    public static ColorDummyThemeManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorDummyThemeManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorDummyThemeManager();
                }
            }
        }
        return sInstance;
    }

    ColorDummyThemeManager() {
    }

    @Override // android.content.res.IColorThemeManager
    public void getValue(ColorBaseResourcesImpl impl, int id, TypedValue outValue, boolean resolveRefs) {
    }

    @Override // android.content.res.IColorThemeManager
    public void init(ColorBaseResourcesImpl impl, String name) {
    }

    @Override // android.content.res.IColorThemeManager
    public InputStream openRawResource(ColorBaseResourcesImpl impl, int id, TypedValue value) {
        return null;
    }

    @Override // android.content.res.IColorThemeManager
    public int updateExConfiguration(ColorBaseResourcesImpl impl, Configuration config) {
        return 0;
    }

    @Override // android.content.res.IColorThemeManager
    public void checkUpdate(ColorBaseResourcesImpl impl, int changes, boolean languageChaged) {
    }

    @Override // android.content.res.IColorThemeManager
    public TypedArray replaceTypedArray(ColorBaseResourcesImpl impl, TypedArray typedarray) {
        return typedarray;
    }

    @Override // android.content.res.IColorThemeManager
    public void getExValue(ColorBaseResourcesImpl impl, int id, TypedValue outValue, boolean resolveRefs) {
    }

    @Override // android.content.res.IColorThemeManager
    public Drawable loadOverlayDrawable(ColorBaseResourcesImpl impl, Resources wrapper, TypedValue value, int id) {
        return null;
    }

    @Override // android.content.res.IColorThemeManager
    public CharSequence getText(ColorBaseResources res, int id, CharSequence text) {
        return text;
    }

    @Override // android.content.res.IColorThemeManager
    public Drawable getDefaultActivityIcon(Context context, ColorBaseResources colorRes) {
        return context.getDrawable(17301651);
    }

    @Override // android.content.res.IColorThemeManager
    public Drawable loadPackageItemIcon(PackageItemInfo info, PackageManager pm, ApplicationInfo appInfo, boolean isConvertEnable) {
        return pm.loadItemIcon(info, appInfo);
    }

    @Override // android.content.res.IColorThemeManager
    public Drawable loadResolveIcon(ResolveInfo info, PackageManager pm, String packageName, int resid, ApplicationInfo appInfo, boolean convert) {
        return pm.getDrawable(packageName, resid, appInfo);
    }

    @Override // android.content.res.IColorThemeManager
    public Drawable getBadgedIcon(LauncherActivityInfo info, int density, PackageManager pm, UserHandle user, ActivityInfo activity) {
        return pm.getUserBadgedIcon(info.getIcon(density), user);
    }

    @Override // android.content.res.IColorThemeManager
    public boolean isColorIcons() {
        return false;
    }

    @Override // android.content.res.IColorThemeManager
    public boolean supportUxIcon(PackageManager pm, ApplicationInfo app, String packageName) {
        return false;
    }

    @Override // android.content.res.IColorThemeManager
    public Drawable getDrawableFromUxIcon(PackageManager packageManager, String packageName, int id, ApplicationInfo appInfo, boolean loadByResolver) {
        return packageManager.getDrawable(packageName, id, appInfo);
    }

    @Override // android.content.res.IColorThemeManager
    public Drawable getDrawableForApp(Drawable src, boolean isForegroundDrawable) {
        return src;
    }

    @Override // android.content.res.IColorThemeManager
    public Drawable getDrawableForApp(Resources res, ColorBaseResources colorRes, Drawable src, boolean isForegroundDrawable) {
        return src;
    }

    @Override // android.content.res.IColorThemeManager
    public long getIconConfigFromSettings(ContentResolver resolver, Context context, int userId) {
        return -1;
    }

    @Override // android.content.res.IColorThemeManager
    public void setIconConfigToSettings(ContentResolver resolver, long uxIconConfig, int userId) {
    }

    @Override // android.content.res.IColorThemeManager
    public void updateExtraConfigForUxIcon(int changes) {
    }

    @Override // android.content.res.IColorThemeManager
    public boolean supportUxOnline(PackageManager packageManager, String sourcePackageName) {
        return false;
    }

    @Override // android.content.res.IColorThemeManager
    public void onCleanupUserForTheme(int userId) {
    }
}
