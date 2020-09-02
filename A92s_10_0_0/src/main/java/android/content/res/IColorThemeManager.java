package android.content.res;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
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

public interface IColorThemeManager extends IOppoCommonFeature {
    public static final IColorThemeManager DEFAULT = new IColorThemeManager() {
        /* class android.content.res.IColorThemeManager.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorThemeManager;
    }

    @Override // android.common.IOppoCommonFeature
    default IColorThemeManager getDefault() {
        return DEFAULT;
    }

    default void getValue(ColorBaseResourcesImpl impl, int id, TypedValue outValue, boolean resolveRefs) {
    }

    default void init(ColorBaseResourcesImpl impl, String name) {
    }

    default InputStream openRawResource(ColorBaseResourcesImpl impl, int id, TypedValue value) {
        return null;
    }

    default int updateExConfiguration(ColorBaseResourcesImpl impl, Configuration config) {
        return 0;
    }

    default void checkUpdate(ColorBaseResourcesImpl impl, int changes, boolean languageChaged) {
    }

    default TypedArray replaceTypedArray(ColorBaseResourcesImpl impl, TypedArray typedarray) {
        return typedarray;
    }

    default void getExValue(ColorBaseResourcesImpl impl, int id, TypedValue outValue, boolean resolveRefs) {
    }

    default Drawable loadOverlayDrawable(ColorBaseResourcesImpl impl, Resources wrapper, TypedValue value, int id) {
        return null;
    }

    default CharSequence getText(ColorBaseResources res, int id, CharSequence text) {
        return text;
    }

    default Drawable getDefaultActivityIcon(Context context, ColorBaseResources colorRes) {
        return context.getDrawable(17301651);
    }

    default Drawable loadPackageItemIcon(PackageItemInfo info, PackageManager pm, ApplicationInfo appInfo, boolean isConvertEnable) {
        return pm.loadItemIcon(info, appInfo);
    }

    default Drawable loadResolveIcon(ResolveInfo info, PackageManager pm, String packageName, int resid, ApplicationInfo appInfo, boolean convert) {
        return pm.getDrawable(packageName, resid, appInfo);
    }

    default Drawable getBadgedIcon(LauncherActivityInfo info, int density, PackageManager pm, UserHandle user, ActivityInfo activity) {
        return pm.getUserBadgedIcon(info.getIcon(density), user);
    }

    default boolean isColorIcons() {
        return false;
    }

    default boolean supportUxIcon(PackageManager pm, ApplicationInfo app, String packageName) {
        return false;
    }

    default Drawable getDrawableFromUxIcon(PackageManager packageManager, String packageName, int id, ApplicationInfo appInfo, boolean loadByResolver) {
        return packageManager.getDrawable(packageName, id, appInfo);
    }

    default Drawable getDrawableForApp(Drawable src, boolean isForegroundDrawable) {
        return src;
    }

    default Drawable getDrawableForApp(Resources res, ColorBaseResources colorRes, Drawable src, boolean isForegroundDrawable) {
        return src;
    }

    default long getIconConfigFromSettings(ContentResolver resolver, Context context, int userId) {
        return -1;
    }

    default void setIconConfigToSettings(ContentResolver resolver, long uxIconConfig, int userId) {
    }

    default void updateExtraConfigForUxIcon(int changes) {
    }

    default boolean supportUxOnline(PackageManager packageManager, String sourcePackageName) {
        return false;
    }

    default void onCleanupUserForTheme(int userId) {
    }
}
