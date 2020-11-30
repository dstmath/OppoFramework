package android.app;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorBaseResources;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.oppo.theme.OppoAppIconInfo;
import com.oppo.theme.OppoConvertIcon;
import com.oppo.theme.OppoThirdPartUtil;
import oppo.content.res.OppoExtraConfiguration;

public class OppoThemeHelper {
    private static final String TAG = "OppoThemeHelper";
    private static boolean sPreLoading = false;

    public static void handleExtraConfigurationChanges(int i) {
        if ((134217728 & i) != 0) {
            Canvas.freeCaches();
        }
    }

    public static void handleExtraConfigurationChanges(int i, Configuration configuration, Context context, Handler handler) {
        if ((134217728 & i) != 0) {
            handleExtraConfigurationChanges(i);
        }
    }

    public static Drawable getDrawable(PackageManager packagemanager, String packageName, int id, ApplicationInfo applicationinfo, PackageItemInfo packageiteminfo, boolean flag) {
        if (!flag || packageiteminfo == null || !(packagemanager instanceof OppoBaseApplicationPackageManager)) {
            return packagemanager.getDrawable(packageName, id, applicationinfo);
        }
        return getDrawable((OppoBaseApplicationPackageManager) packagemanager, packageName, id, applicationinfo, packageiteminfo.name);
    }

    private static boolean isGoogleApps(String packageName, boolean isDefaultTheme) {
        if (!isDefaultTheme || TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (packageName.startsWith("com.google.android") || packageName.equals("com.googlesuit.ggkj") || packageName.equals("com.google.earth") || packageName.equals("net.eeekeie.kekegdleiedec") || packageName.equals("com.jsoh.GoogleService") || packageName.equals("lin.wang.allspeak") || packageName.equals("com.android.vending") || packageName.equals("com.android.chrome")) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0126  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x017d A[SYNTHETIC, Splitter:B:89:0x017d] */
    public static synchronized Drawable getDrawable(OppoBaseApplicationPackageManager packagemanager, String packageName, int id, ApplicationInfo applicationinfo, String name) {
        ApplicationInfo applicationinfo2;
        Resources.NotFoundException e;
        RuntimeException e2;
        String tempStr;
        Drawable drawable;
        synchronized (OppoThemeHelper.class) {
            Drawable drawable2 = packagemanager.getCachedIconForThemeHelper(packageName, id);
            if (drawable2 != null) {
                return drawable2;
            }
            boolean parseSucceed = true;
            int userId = 0;
            if (applicationinfo == null) {
                try {
                    applicationinfo2 = packagemanager.getApplicationInfo(packageName, 0);
                } catch (PackageManager.NameNotFoundException e3) {
                    return null;
                }
            } else {
                applicationinfo2 = applicationinfo;
            }
            try {
                ColorBaseResources colorRes = packagemanager.getColorBaseResourcesForThemeHelper(applicationinfo2);
                Resources res = packagemanager.getResourcesForApplication(applicationinfo2);
                OppoExtraConfiguration exConfig = getExtraConfig(colorRes, applicationinfo2.packageName);
                if (exConfig != null) {
                    userId = exConfig.mUserId;
                }
                if (res != null) {
                    try {
                        if (!sPreLoading || needUpdateTheme(exConfig)) {
                            sPreLoading = true;
                            OppoConvertIcon.initConvertIconForUser(res, userId);
                            parseSucceed = OppoAppIconInfo.parseIconXmlForUser(userId);
                            String pString = null;
                            if (res == null) {
                                boolean isThirdPart = OppoAppIconInfo.isThirdPart(applicationinfo2);
                                if (!isThirdPart) {
                                    boolean useWrap = OppoThirdPartUtil.mIsDefaultTheme;
                                    StringBuffer sB = new StringBuffer(res.getResourceName(id));
                                    int index = sB.lastIndexOf("/");
                                    if (index >= 0) {
                                        try {
                                            pString = sB.substring(index + 1) + ".png";
                                        } catch (PackageManager.NameNotFoundException e4) {
                                            return null;
                                        } catch (Resources.NotFoundException e5) {
                                            e = e5;
                                            drawable2 = drawable2;
                                            Log.w(TAG, "getDrawable. Failure retrieving resources for " + applicationinfo2.packageName + ": " + e.getMessage());
                                            if (drawable2 != null) {
                                            }
                                            return drawable2;
                                        } catch (RuntimeException e6) {
                                            e2 = e6;
                                            drawable2 = drawable2;
                                            Log.w(TAG, "getDrawable. Failure retrieving icon 0x" + Integer.toHexString(id) + " in package " + packageName, e2);
                                            if (drawable2 != null) {
                                            }
                                            return drawable2;
                                        }
                                    }
                                    if (pString != null) {
                                        boolean isThirdPartByIconName = OppoAppIconInfo.isThirdPartbyIconName(pString);
                                        int iconIndex = OppoAppIconInfo.indexOfPackageName(applicationinfo2.packageName);
                                        if (iconIndex >= 0) {
                                            tempStr = OppoAppIconInfo.getIconName(iconIndex);
                                        } else {
                                            tempStr = null;
                                        }
                                        if (!TextUtils.isEmpty(tempStr) && isThirdPartByIconName) {
                                            if (!pString.equalsIgnoreCase(tempStr)) {
                                                drawable = colorRes.loadIcon(id, tempStr, useWrap);
                                                drawable2 = drawable;
                                            }
                                        }
                                        drawable = colorRes.loadIcon(id, useWrap);
                                        drawable2 = drawable;
                                    } else {
                                        drawable2 = colorRes.loadIcon(id, useWrap);
                                    }
                                    if (drawable2 == null) {
                                        drawable2 = packagemanager.getDrawable(packageName, id, applicationinfo2);
                                        isThirdPart = true;
                                    }
                                } else {
                                    drawable2 = packagemanager.getDrawable(packageName, id, applicationinfo2);
                                }
                                if (drawable2 != null && parseSucceed && !(drawable2 instanceof LayerDrawable)) {
                                    drawable2 = new BitmapDrawable(res, OppoConvertIcon.convertIconBitmap(drawable2, res, isThirdPart));
                                }
                            }
                            if (drawable2 != null) {
                                try {
                                    packagemanager.putCachedIconForThemeHelper(packageName, id, drawable2);
                                } catch (Resources.NotFoundException e7) {
                                    Log.w(TAG, "getDrawable. Failure retrieving resources for " + applicationinfo2.packageName + ": " + e7.getMessage());
                                } catch (RuntimeException e8) {
                                    Log.w(TAG, "getDrawable. Failure retrieving icon 0x" + Integer.toHexString(id) + " in package " + packageName, e8);
                                }
                            }
                            return drawable2;
                        }
                    } catch (PackageManager.NameNotFoundException e9) {
                        return null;
                    } catch (Resources.NotFoundException e10) {
                        e = e10;
                        Log.w(TAG, "getDrawable. Failure retrieving resources for " + applicationinfo2.packageName + ": " + e.getMessage());
                        if (drawable2 != null) {
                        }
                        return drawable2;
                    } catch (RuntimeException e11) {
                        e2 = e11;
                        Log.w(TAG, "getDrawable. Failure retrieving icon 0x" + Integer.toHexString(id) + " in package " + packageName, e2);
                        if (drawable2 != null) {
                        }
                        return drawable2;
                    }
                }
                if (res != null && !OppoConvertIcon.hasInit()) {
                    OppoConvertIcon.initConvertIconForUser(res, userId);
                }
                if (OppoAppIconInfo.getAppsNumbers() <= 0) {
                    parseSucceed = OppoAppIconInfo.parseIconXmlForUser(userId);
                }
                String pString2 = null;
                if (res == null) {
                }
            } catch (PackageManager.NameNotFoundException e12) {
                return null;
            } catch (Resources.NotFoundException e13) {
                e = e13;
                Log.w(TAG, "getDrawable. Failure retrieving resources for " + applicationinfo2.packageName + ": " + e.getMessage());
                if (drawable2 != null) {
                }
                return drawable2;
            } catch (RuntimeException e14) {
                e2 = e14;
                Log.w(TAG, "getDrawable. Failure retrieving icon 0x" + Integer.toHexString(id) + " in package " + packageName, e2);
                if (drawable2 != null) {
                }
                return drawable2;
            }
            if (drawable2 != null) {
            }
            return drawable2;
        }
        return null;
    }

    public static synchronized Drawable getDrawableByConvert(ColorBaseResources colorRes, Resources res, Drawable drawable) {
        synchronized (OppoThemeHelper.class) {
            if (res == null || drawable == null) {
                return drawable;
            }
            boolean parseSucceed = true;
            Drawable dr = drawable;
            try {
                OppoExtraConfiguration exConfig = getExtraConfig(colorRes, null);
                int userId = exConfig == null ? 0 : exConfig.mUserId;
                if (sPreLoading) {
                    if (!needUpdateTheme(exConfig)) {
                        if (!OppoConvertIcon.hasInit()) {
                            OppoConvertIcon.initConvertIconForUser(res, userId);
                        }
                        if (OppoAppIconInfo.getAppsNumbers() <= 0) {
                            parseSucceed = OppoAppIconInfo.parseIconXmlForUser(userId);
                        }
                        if (parseSucceed && !(drawable instanceof LayerDrawable)) {
                            dr = new BitmapDrawable(res, OppoConvertIcon.convertIconBitmap(drawable, res, true));
                        }
                        return dr;
                    }
                }
                sPreLoading = true;
                OppoConvertIcon.initConvertIconForUser(res, userId);
                parseSucceed = OppoAppIconInfo.parseIconXmlForUser(userId);
                dr = new BitmapDrawable(res, OppoConvertIcon.convertIconBitmap(drawable, res, true));
            } catch (Exception e) {
                Log.e(TAG, "getDrawableByConvert. e = " + e);
            }
            return dr;
        }
    }

    public static boolean isCustomizedIcon(IntentFilter intentfilter) {
        return false;
    }

    public static void reset() {
        sPreLoading = false;
    }

    private static boolean needUpdateTheme(OppoExtraConfiguration configuration) {
        boolean z = false;
        if (configuration == null) {
            return false;
        }
        if ((1 & configuration.mThemeChangedFlags) == 0) {
            z = true;
        }
        return z ^ OppoThirdPartUtil.mIsDefaultTheme;
    }

    private static OppoExtraConfiguration getExtraConfig(ColorBaseResources colorRes, String packageName) {
        if ("system".equals(packageName)) {
            return colorRes.getConfiguration().getOppoExtraConfiguration();
        }
        return colorRes.getColorImpl().getSystemConfiguration().getOppoExtraConfiguration();
    }
}
