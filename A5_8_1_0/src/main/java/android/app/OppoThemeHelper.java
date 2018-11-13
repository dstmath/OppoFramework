package android.app;

import android.app.backup.FullBackup;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.ResourcesImpl;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import com.oppo.theme.OppoAppIconInfo;
import com.oppo.theme.OppoConvertIcon;
import com.oppo.theme.OppoThirdPartUtil;

public class OppoThemeHelper {
    private static final int COMPLEX_UNIT_DIP = 1;
    private static final int COMPLEX_UNIT_PX = 0;
    private static final int COMPLEX_UNIT_SP = 2;
    private static final String TAG = "OppoThemeHelper";

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
        if (!flag || packageiteminfo == null) {
            return packagemanager.getDrawable(packageName, id, applicationinfo);
        }
        return getDrawable(packagemanager, packageName, id, applicationinfo, packageiteminfo.name);
    }

    private static boolean isGoogleApps(String packageName, boolean isDefaultTheme) {
        if (isDefaultTheme && (TextUtils.isEmpty(packageName) ^ 1) != 0 && (packageName.startsWith("com.google.android") || packageName.equals("com.googlesuit.ggkj") || packageName.equals("com.google.earth") || packageName.equals("net.eeekeie.kekegdleiedec") || packageName.equals("com.jsoh.GoogleService") || packageName.equals("lin.wang.allspeak") || packageName.equals("com.android.vending") || packageName.equals("com.android.chrome"))) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x00ff A:{SYNTHETIC, Splitter: B:49:0x00ff} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00ff A:{SYNTHETIC, Splitter: B:49:0x00ff} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00ff A:{SYNTHETIC, Splitter: B:49:0x00ff} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00ff A:{SYNTHETIC, Splitter: B:49:0x00ff} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized Drawable getDrawable(PackageManager packagemanager, String packageName, int id, ApplicationInfo applicationinfo, String name) {
        NotFoundException e;
        RuntimeException e2;
        synchronized (OppoThemeHelper.class) {
            ResourceName rName = new ResourceName(packageName, id);
            Drawable drawable = ((ApplicationPackageManager) packagemanager).getCachedIcon(rName);
            if (drawable != null) {
                return drawable;
            }
            boolean parseSucceed = true;
            if (applicationinfo == null) {
                try {
                    applicationinfo = packagemanager.getApplicationInfo(packageName, 0);
                } catch (NameNotFoundException e3) {
                    return null;
                }
            }
            try {
                Resources res = packagemanager.getResourcesForApplication(applicationinfo);
                if (res == null || !res.getThemeChanged()) {
                    if (res != null) {
                        if ((OppoConvertIcon.hasInit() ^ 1) != 0) {
                            OppoConvertIcon.initConvertIcon(res);
                        }
                    }
                    if (OppoAppIconInfo.getAppsNumbers() <= 0) {
                        parseSucceed = OppoAppIconInfo.parseIconXml();
                    }
                } else {
                    res.setIsThemeChanged(false);
                    OppoConvertIcon.initConvertIcon(res);
                    parseSucceed = OppoAppIconInfo.parseIconXml();
                }
                String pString = null;
                if (res != null) {
                    Drawable drawable2;
                    boolean isThirdPart = OppoAppIconInfo.isThirdPart(applicationinfo);
                    if (isThirdPart) {
                        drawable = packagemanager.getDrawable(packageName, id, applicationinfo);
                    } else {
                        boolean useWrap = OppoThirdPartUtil.mIsDefaultTheme;
                        StringBuffer stringBuffer = new StringBuffer(res.getResourceName(id));
                        int index = stringBuffer.lastIndexOf("/");
                        if (index >= 0) {
                            pString = stringBuffer.substring(index + 1) + ".png";
                        }
                        if (pString != null) {
                            CharSequence tempStr;
                            boolean isThirdPartByIconName = OppoAppIconInfo.isThirdPartbyIconName(pString);
                            int iconIndex = OppoAppIconInfo.indexOfPackageName(applicationinfo.packageName);
                            if (iconIndex >= 0) {
                                tempStr = OppoAppIconInfo.getIconName(iconIndex);
                            } else {
                                tempStr = null;
                            }
                            if (TextUtils.isEmpty(tempStr) || !isThirdPartByIconName || (pString.equalsIgnoreCase(tempStr) ^ 1) == 0) {
                                drawable = res.loadIcon(id, useWrap);
                            } else {
                                drawable = res.loadIcon(id, tempStr, useWrap);
                            }
                        } else {
                            drawable = res.loadIcon(id, useWrap);
                        }
                        if (drawable == null) {
                            isThirdPart = true;
                            drawable = packagemanager.getDrawable(packageName, id, applicationinfo);
                        }
                    }
                    if (drawable == null) {
                        drawable2 = packagemanager.getDefaultActivityIcon();
                    } else {
                        drawable2 = drawable;
                    }
                    if (drawable2 == null || !parseSucceed) {
                        drawable = drawable2;
                    } else {
                        try {
                            if (((drawable2 instanceof LayerDrawable) ^ 1) == 0) {
                                drawable = drawable2;
                            } else if (isGoogleApps(packageName, OppoThirdPartUtil.mIsDefaultTheme)) {
                                drawable = new BitmapDrawable(res, OppoConvertIcon.convertIconBitmap(drawable2, res, isThirdPart, true));
                            } else {
                                drawable = new BitmapDrawable(res, OppoConvertIcon.convertIconBitmap(drawable2, res, isThirdPart));
                            }
                        } catch (NameNotFoundException e4) {
                            drawable = drawable2;
                        } catch (NotFoundException e5) {
                            e = e5;
                            drawable = drawable2;
                            Log.w(TAG, "getDrawable. Failure retrieving resources for " + applicationinfo.packageName + ": " + e.getMessage());
                            if (drawable != null) {
                            }
                            return drawable;
                        } catch (RuntimeException e6) {
                            e2 = e6;
                            drawable = drawable2;
                            Log.w(TAG, "getDrawable. Failure retrieving icon 0x" + Integer.toHexString(id) + " in package " + packageName, e2);
                            if (drawable != null) {
                            }
                            return drawable;
                        }
                    }
                }
            } catch (NameNotFoundException e7) {
                return null;
            } catch (NotFoundException e8) {
                e = e8;
                Log.w(TAG, "getDrawable. Failure retrieving resources for " + applicationinfo.packageName + ": " + e.getMessage());
                if (drawable != null) {
                }
                return drawable;
            } catch (RuntimeException e9) {
                e2 = e9;
                Log.w(TAG, "getDrawable. Failure retrieving icon 0x" + Integer.toHexString(id) + " in package " + packageName, e2);
                if (drawable != null) {
                }
                return drawable;
            }
            if (drawable != null) {
                try {
                    ((ApplicationPackageManager) packagemanager).putCachedIcon(rName, drawable);
                } catch (NotFoundException e10) {
                    Log.w(TAG, "getDrawable. Failure retrieving resources for " + applicationinfo.packageName + ": " + e10.getMessage());
                    return null;
                } catch (RuntimeException e22) {
                    Log.w(TAG, "getDrawable. Failure retrieving icon 0x" + Integer.toHexString(id) + " in package " + packageName, e22);
                    return null;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:5:0x0008, code:
            return r9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized Drawable getDrawableByConvert(Resources res, Drawable drawable) {
        Drawable dr;
        synchronized (OppoThemeHelper.class) {
            if (res == null || drawable == null) {
            } else {
                boolean parseSucceed = true;
                dr = drawable;
                try {
                    if (res.getThemeChanged()) {
                        res.setIsThemeChanged(false);
                        OppoConvertIcon.initConvertIcon(res);
                        parseSucceed = OppoAppIconInfo.parseIconXml();
                    } else {
                        if (!OppoConvertIcon.hasInit()) {
                            OppoConvertIcon.initConvertIcon(res);
                        }
                        if (OppoAppIconInfo.getAppsNumbers() <= 0) {
                            parseSucceed = OppoAppIconInfo.parseIconXml();
                        }
                    }
                    if (parseSucceed && ((drawable instanceof LayerDrawable) ^ 1) != 0) {
                        dr = new BitmapDrawable(res, OppoConvertIcon.convertIconBitmap(drawable, res, true));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "getDrawableByConvert. e = " + e);
                }
            }
        }
        return dr;
    }

    public static boolean isCustomizedIcon(IntentFilter intentfilter) {
        return false;
    }

    public static Integer parseDimension(ResourcesImpl res, String dimension) {
        if (dimension != null) {
            int index = dimension.lastIndexOf(".");
            if (index <= 0) {
                return null;
            }
            String complexString = dimension.substring(index + 1);
            String numberString = dimension.substring(0, index);
            if (!(TextUtils.isEmpty(complexString) || (TextUtils.isEmpty(numberString) ^ 1) == 0)) {
                int dim = Integer.valueOf(numberString).intValue() << 8;
                DisplayMetrics metrics = res.getDisplayMetrics();
                if ("px".equalsIgnoreCase(complexString)) {
                    return new Integer(dim + 0);
                }
                if ("dp".equalsIgnoreCase(complexString) || "dip".equalsIgnoreCase(complexString)) {
                    return new Integer(dim + 1);
                }
                if (FullBackup.SHAREDPREFS_TREE_TOKEN.equalsIgnoreCase(complexString)) {
                    return new Integer(dim + 2);
                }
            }
        }
        return null;
    }
}
