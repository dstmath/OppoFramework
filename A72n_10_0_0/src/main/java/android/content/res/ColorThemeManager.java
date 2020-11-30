package android.content.res;

import android.app.ColorUXIconLoadHelper;
import android.app.ColorUxIconConfigParser;
import android.app.ColorUxIconConstants;
import android.app.OppoBaseApplicationPackageManager;
import android.app.OppoThemeHelper;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import java.io.File;
import java.io.InputStream;

public class ColorThemeManager implements IColorThemeManager {
    private static final String TAG = "ColorThemeManager";
    private static volatile ColorThemeManager sInstance = null;

    public static ColorThemeManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorThemeManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorThemeManager();
                }
            }
        }
        return sInstance;
    }

    ColorThemeManager() {
    }

    public void getValue(ColorBaseResourcesImpl impl, int id, TypedValue outValue, boolean resolveRefs) {
        impl.getExValue(id, outValue, resolveRefs);
    }

    public void init(ColorBaseResourcesImpl impl, String name) {
        impl.init(name);
    }

    public InputStream openRawResource(ColorBaseResourcesImpl impl, int id, TypedValue value) {
        return impl.openThemeRawResource(id, value);
    }

    public int updateExConfiguration(ColorBaseResourcesImpl impl, Configuration config) {
        return impl.updateExConfiguration(impl, config);
    }

    public void checkUpdate(ColorBaseResourcesImpl impl, int changes, boolean languageChaged) {
        impl.checkUpdate(changes, languageChaged);
    }

    public TypedArray replaceTypedArray(ColorBaseResourcesImpl impl, TypedArray typedarray) {
        return impl.replaceTypedArray(typedarray);
    }

    public Drawable loadOverlayDrawable(ColorBaseResourcesImpl impl, Resources wrapper, TypedValue value, int id) {
        return impl.loadOverlayDrawable(wrapper, value, id);
    }

    public CharSequence getText(ColorBaseResources res, int id, CharSequence text) {
        CharSequence themeChar = res.getColorImpl().getThemeCharSequence(id);
        if (themeChar != null) {
            return themeChar;
        }
        return text;
    }

    public Drawable getDefaultActivityIcon(Context context, ColorBaseResources colorRes) {
        Trace.traceBegin(8192, "#UxIcon.getDefaultActivityIcon");
        if ("android.content.cts".equals(context.getPackageName())) {
            return context.getDrawable(17301651);
        }
        Drawable dr = context.getDrawable(17629184);
        if (dr != null) {
            if (ColorUXIconLoadHelper.supportUxIcon(context.getPackageManager(), context.getApplicationInfo(), context.getPackageName())) {
                dr = ColorUXIconLoadHelper.getUxIconDrawable(context.getResources(), colorRes, dr, false);
            } else {
                dr = OppoThemeHelper.getDrawableByConvert(colorRes, context.getResources(), dr);
            }
        }
        Trace.traceEnd(8192);
        return dr;
    }

    public Drawable loadPackageItemIcon(PackageItemInfo info, PackageManager pm, ApplicationInfo appInfo, boolean isConvertEnable) {
        if (!isConvertEnable || !(pm instanceof OppoBaseApplicationPackageManager)) {
            return pm.loadItemIcon(info, appInfo);
        }
        return ((OppoBaseApplicationPackageManager) pm).loadItemIcon(info, appInfo, true);
    }

    private Drawable loadDrawableFromTheme(ResolveInfo info, PackageManager pm, String packageName, int resid, ApplicationInfo ai, boolean convert) {
        if (!convert) {
            return OppoThemeHelper.getDrawable(pm, packageName, info.icon, ai, info.activityInfo != null ? info.activityInfo : info.serviceInfo, OppoThemeHelper.isCustomizedIcon(info.filter));
        } else if (ColorUXIconLoadHelper.supportUxIcon(pm, ai, packageName)) {
            return ColorUXIconLoadHelper.getDrawable(pm, packageName, resid, ai, true);
        } else {
            return OppoThemeHelper.getDrawable((OppoBaseApplicationPackageManager) pm, packageName, resid, ai, (String) null);
        }
    }

    public Drawable loadResolveIcon(ResolveInfo info, PackageManager pm, String packageName, int resid, ApplicationInfo appInfo, boolean convert) {
        Drawable dr = loadDrawableFromTheme(info, pm, packageName, resid, appInfo, convert);
        if (dr == null) {
            return pm.getDrawable(packageName, resid, appInfo);
        }
        return dr;
    }

    public Drawable getBadgedIcon(LauncherActivityInfo info, int density, PackageManager pm, UserHandle user, ActivityInfo activity) {
        Drawable originalIcon = activity.loadIcon(pm);
        if (originalIcon == null) {
            originalIcon = activity.loadDefaultIcon(pm);
        }
        return pm.getUserBadgedIcon(originalIcon, user);
    }

    public boolean isColorIcons() {
        return true;
    }

    public boolean supportUxIcon(PackageManager pm, ApplicationInfo ai, String packageName) {
        return ColorUXIconLoadHelper.supportUxIcon(pm, ai, packageName);
    }

    public Drawable getDrawableFromUxIcon(PackageManager packageManager, String packageName, int id, ApplicationInfo appInfo, boolean loadByResolver) {
        return ColorUXIconLoadHelper.getDrawable(packageManager, packageName, id, appInfo, loadByResolver);
    }

    public Drawable getDrawableForApp(Resources res, ColorBaseResources colorRes, Drawable src, boolean isForegroundDrawable) {
        return ColorUXIconLoadHelper.getUxIconDrawable(res, colorRes, src, isForegroundDrawable);
    }

    public long getIconConfigFromSettings(ContentResolver resolver, Context context, int userId) {
        long uxIconConfig = Settings.System.getLongForUser(resolver, ColorUxIconConstants.SystemProperty.KEY_UX_ICON_CONFIG, -1, userId);
        if (uxIconConfig != -1) {
            return uxIconConfig;
        }
        if (userId == 0 && SystemProperties.getInt(ColorUxIconConstants.SystemProperty.KEY_UX_ICON_THEME_FLAG, 0) == 0) {
            SystemProperties.set(ColorUxIconConstants.SystemProperty.KEY_UX_ICON_THEME_FLAG, String.valueOf(-1));
        }
        long uxIconConfig2 = ColorUxIconConfigParser.getDefaultIconConfig(context.getPackageManager().hasSystemFeature(ColorUxIconConstants.SystemProperty.FEATURE_OPPO_VERSION_EXP), context.getResources());
        Settings.System.putLongForUser(resolver, ColorUxIconConstants.SystemProperty.KEY_UX_ICON_CONFIG, uxIconConfig2, userId);
        return uxIconConfig2;
    }

    public void setIconConfigToSettings(ContentResolver resolver, long uxIconConfig, int userId) {
        Settings.System.putLongForUser(resolver, ColorUxIconConstants.SystemProperty.KEY_UX_ICON_CONFIG, uxIconConfig, userId);
    }

    public void updateExtraConfigForUxIcon(int changes) {
        ColorUXIconLoadHelper.updateExtraConfig(changes);
        if (ColorUxIconConstants.DEBUG_UX_ICON) {
            Log.i(TAG, "updateExtraConfigForUxIcon changes = " + changes + "; callers:" + Debug.getCallers(10));
        }
    }

    public boolean supportUxOnline(PackageManager packageManager, String sourcePackageName) {
        return ColorUXIconLoadHelper.supportUxOnline(packageManager, sourcePackageName);
    }

    public void onCleanupUserForTheme(int userId) {
        if (userId != 0) {
            File themeFileForUser = new File("/data/theme/" + userId);
            if (themeFileForUser.exists()) {
                FileUtils.deleteContentsAndDir(themeFileForUser);
            }
        }
    }
}
