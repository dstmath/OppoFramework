package android.app;

import android.app.ColorUxIconConstants;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorBaseResources;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.os.SystemProperties;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Log;
import oppo.content.res.OppoExtraConfiguration;

public class ColorUXIconLoadHelper {
    private static final String TAG = "ColorUXIconLoadHelper";
    private static final ColorUXIconLoader sIconLoader = ColorUXIconLoader.getLoader();
    private static int sSupportUxOnline = -1;

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0039, code lost:
        return r1;
     */
    public static synchronized Drawable getDrawable(PackageManager packageManager, String packageName, int id, ApplicationInfo app, boolean loadByResolver) {
        synchronized (ColorUXIconLoadHelper.class) {
            OppoBaseApplicationPackageManager applicationPackageManager = (OppoBaseApplicationPackageManager) packageManager;
            Drawable drawable = applicationPackageManager.getCachedIconForThemeHelper(packageName, id);
            if (drawable == null) {
                Trace.traceBegin(8192, "#UxIcon.getDrawable");
                Drawable drawable2 = sIconLoader.loadUxIcon(packageManager, packageName, id, app, loadByResolver);
                Trace.traceEnd(8192);
                if (drawable2 == null) {
                    return null;
                }
                applicationPackageManager.putCachedIconForThemeHelper(packageName, id, drawable2);
                return drawable2;
            } else if (ColorUxIconConstants.DEBUG_UX_ICON) {
                Log.v(TAG, "getDrawable drawable in cached =:" + drawable + "; callers:" + Debug.getCallers(10));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0015, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000c, code lost:
        if (android.text.TextUtils.isEmpty(r20) == false) goto L_0x0012;
     */
    public static synchronized boolean supportUxIcon(PackageManager pm, ApplicationInfo ai, String packageName) {
        ApplicationInfo ai2;
        OppoExtraConfiguration extraConfig;
        synchronized (ColorUXIconLoadHelper.class) {
            boolean z = false;
            if (ai == null) {
            }
            if (pm != null) {
                boolean supportUxIcon = false;
                if (ai == null) {
                    try {
                        ai2 = pm.getApplicationInfo(packageName, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e = e;
                    }
                } else {
                    ai2 = ai;
                }
                if (ai2 == null) {
                    return false;
                }
                try {
                    ColorBaseResources colorRes = ((OppoBaseApplicationPackageManager) pm).getColorBaseResourcesForThemeHelper(ai2);
                    if ("system".equals(ai2.packageName)) {
                        extraConfig = colorRes.getConfiguration().getOppoExtraConfiguration();
                    } else {
                        extraConfig = colorRes.getColorImpl().getSystemConfiguration().getOppoExtraConfiguration();
                    }
                    int userId = extraConfig == null ? 0 : extraConfig.mUserId;
                    String key = ColorUxIconConstants.SystemProperty.KEY_THEME_FLAG;
                    if (userId > 0) {
                        key = ColorUxIconConstants.SystemProperty.KEY_THEME_FLAG + "." + userId;
                    }
                    Long themeFlag = Long.valueOf(extraConfig == null ? SystemProperties.getLong(key, 0) : extraConfig.mThemeChangedFlags);
                    if ((themeFlag.longValue() & 16) == 0) {
                        z = true;
                    }
                    supportUxIcon = z;
                    if (ColorUxIconConstants.DEBUG_UX_ICON) {
                        Log.v(TAG, "supportUxIcon themeFlag =:" + themeFlag + "; supportUxIcon = " + supportUxIcon);
                    }
                } catch (PackageManager.NameNotFoundException e2) {
                    e = e2;
                    if (ColorUxIconConstants.DEBUG_UX_ICON) {
                        Log.e(TAG, "supportUxIcon NameNotFoundException =:" + e.toString());
                    }
                    return supportUxIcon;
                }
            }
        }
    }

    public static synchronized Drawable getUxIconDrawable(Resources res, ColorBaseResources colorRes, Drawable src, boolean isForegroundDrawable) {
        Drawable uxIconDrawable;
        synchronized (ColorUXIconLoadHelper.class) {
            uxIconDrawable = sIconLoader.getUxIconDrawable(res, colorRes, src, isForegroundDrawable);
        }
        return uxIconDrawable;
    }

    public static void updateExtraConfig(int changes) {
        if ((Integer.MIN_VALUE & changes) != 0) {
            sIconLoader.updateExtraConfig();
        }
    }

    public static synchronized boolean supportUxOnline(PackageManager packageManager, String sourcePackageName) {
        boolean z;
        synchronized (ColorUXIconLoadHelper.class) {
            z = false;
            if (sSupportUxOnline == -1) {
                if (TextUtils.isEmpty(sourcePackageName)) {
                    sSupportUxOnline = 0;
                }
                if (ColorUxIconConstants.DEBUG_UX_ICON) {
                    Log.i(TAG, "supportUxOnline sourcePackageName:" + sourcePackageName);
                }
                if (sSupportUxOnline == -1 && ColorUxIconAppCheckUtils.isSystemApp(sourcePackageName)) {
                    sSupportUxOnline = 1;
                }
                if (sSupportUxOnline == -1 && (sourcePackageName.startsWith("com.oppo.") || sourcePackageName.startsWith("com.coloros.") || sourcePackageName.startsWith("com.nearme.") || sourcePackageName.startsWith("com.heytap."))) {
                    sSupportUxOnline = 1;
                }
                if (sSupportUxOnline == -1) {
                    ApplicationInfo appInfo = null;
                    try {
                        appInfo = packageManager.getApplicationInfo(sourcePackageName, 128);
                    } catch (PackageManager.NameNotFoundException ex) {
                        Log.e(TAG, "supportUxOnline ex :" + ex.getMessage());
                    }
                    if (appInfo == null || appInfo.metaData == null) {
                        sSupportUxOnline = 0;
                    } else {
                        try {
                            Boolean supportUxOnline = (Boolean) appInfo.metaData.get(ColorUxIconConstants.IconTheme.COLOROS_UXIOCN_META_DATA);
                            if (ColorUxIconConstants.DEBUG_UX_ICON) {
                                Log.i(TAG, "supportUxOnline :" + supportUxOnline);
                            }
                            sSupportUxOnline = (supportUxOnline == null || !supportUxOnline.booleanValue()) ? 0 : 1;
                        } catch (Exception ex2) {
                            sSupportUxOnline = 0;
                            Log.e(TAG, "supportUxOnline error:" + ex2.getMessage());
                        }
                    }
                }
            }
            if (sSupportUxOnline == 1) {
                z = true;
            }
        }
        return z;
    }
}
