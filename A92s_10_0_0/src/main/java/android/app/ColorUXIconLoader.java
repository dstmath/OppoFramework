package android.app;

import android.app.ColorUxIconConstants;
import android.app.uxicons.CustomAdaptiveIconConfig;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.res.ColorBaseResources;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorPalette;
import android.graphics.Path;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorAdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import oppo.content.res.OppoExtraConfiguration;

public class ColorUXIconLoader {
    private static final String TAG = "ColorUXIconLoader";
    private static ColorIconConfig sIconConfig = new ColorIconConfig();
    private static volatile ColorUXIconLoader sInstance = null;
    private ArrayList<Integer> mCommonStyleConfigArray = new ArrayList<>();
    private String[] mCommonStylePathArray;
    private String[] mCommonStylePrefixArray;
    private Drawable mDefaultBackgroundDrawable;
    private Drawable mDefaultTransparentBackgroundDrawable;
    private boolean mHasInitConfigArray = false;
    private volatile Boolean mIsExpVersion;
    private ArrayList<Integer> mSpecialStyleConfigArray = new ArrayList<>();
    private String[] mSpecialStylePathArray;
    private String[] mSpecialStylePrefixArray;

    public static ColorUXIconLoader getLoader() {
        if (sInstance == null) {
            synchronized (ColorUXIconLoader.class) {
                if (sInstance == null) {
                    sInstance = new ColorUXIconLoader();
                }
            }
        }
        return sInstance;
    }

    private ColorUXIconLoader() {
    }

    public void updateExtraConfig() {
        if (!sIconConfig.isEmpty()) {
            sIconConfig.setNeedUpdate(true);
        }
    }

    public void checkConfig(Resources res, OppoExtraConfiguration extraConfig) {
        if (!this.mHasInitConfigArray) {
            init();
            this.mHasInitConfigArray = true;
            initConfigArray(res);
        }
        ColorUxIconConfigParser.parseConfig(sIconConfig, extraConfig, res, this.mCommonStyleConfigArray, this.mSpecialStyleConfigArray, this.mCommonStylePathArray, this.mSpecialStylePathArray);
        if (ColorUxIconConstants.DEBUG_UX_ICON) {
            Log.v(TAG, "checkConfig sIconConfig =:" + sIconConfig);
        }
    }

    public Drawable getUxIconDrawable(Resources res, ColorBaseResources colorRes, Drawable src, boolean isForegroundDrawable) {
        if (sIconConfig.isEmpty() || sIconConfig.isNeedUpdate()) {
            checkConfig(res, colorRes.getColorImpl().getSystemConfiguration().getOppoExtraConfiguration());
            if (sIconConfig.isEmpty()) {
                return src;
            }
        }
        if (this.mCommonStyleConfigArray.indexOf(Integer.valueOf(sIconConfig.getTheme())) == 1) {
            return buildAdaptiveIconDrawable(res, src, this.mDefaultTransparentBackgroundDrawable, false, false);
        }
        if (isForegroundDrawable) {
            return buildAdaptiveIconDrawable(res, src, getBackgroundDrawable(src), false, false);
        }
        return buildAdaptiveIconDrawable(res, null, src, false, false);
    }

    public Drawable loadUxIcon(PackageManager packageManager, String packageName, int id, ApplicationInfo applicationInfo, boolean loadByResolver) {
        ApplicationInfo applicationInfo2;
        boolean loadUxIconFirstly;
        String iconNamePrefix;
        int pos;
        String iconNamePrefix2;
        boolean isAdaptiveIconDrawable;
        Drawable background;
        Drawable drawable;
        Drawable drawable2;
        Drawable foreground;
        OppoExtraConfiguration extraConfig;
        if (ColorUxIconConstants.DEBUG_UX_ICON) {
            Log.v(TAG, "loadIcon packageName = " + packageName + ",applicationInfo =:" + applicationInfo + "; loadByResolver = " + loadByResolver);
        }
        if (applicationInfo == null) {
            try {
                applicationInfo2 = packageManager.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        } else {
            applicationInfo2 = applicationInfo;
        }
        if (applicationInfo2 == null || TextUtils.isEmpty(packageName)) {
            return null;
        }
        try {
            Resources res = packageManager.getResourcesForApplication(applicationInfo2);
            ColorBaseResources colorRes = ((OppoBaseApplicationPackageManager) packageManager).getColorBaseResourcesForThemeHelper(applicationInfo2);
            if (res == null || colorRes == null) {
                return null;
            }
            if (sIconConfig.isEmpty() || sIconConfig.isNeedUpdate()) {
                if ("system".equals(packageName)) {
                    extraConfig = colorRes.getConfiguration().getOppoExtraConfiguration();
                } else {
                    extraConfig = colorRes.getColorImpl().getSystemConfiguration().getOppoExtraConfiguration();
                }
                checkConfig(res, extraConfig);
                if (sIconConfig.isEmpty()) {
                    return null;
                }
            }
            Drawable foreground2 = null;
            Drawable background2 = null;
            String iconNamePrefix3 = "";
            if (id != applicationInfo2.icon) {
                if (ColorUxIconConstants.IconLoader.COM_ANDROID_CONTACTS.equals(packageName)) {
                    iconNamePrefix3 = ColorUxIconConstants.IconLoader.DIALER_PREFIX;
                }
                if (loadByResolver || ColorUxIconConstants.IconLoader.COM_HEYTAP_MATKET.equals(packageName)) {
                    iconNamePrefix = iconNamePrefix3;
                    loadUxIconFirstly = false;
                } else {
                    iconNamePrefix = iconNamePrefix3;
                    loadUxIconFirstly = true;
                }
            } else {
                iconNamePrefix = iconNamePrefix3;
                loadUxIconFirstly = true;
            }
            boolean isPlatformDrawable = false;
            int pos2 = this.mCommonStyleConfigArray.indexOf(Integer.valueOf(sIconConfig.getTheme()));
            int customThemeConfigPos = this.mCommonStyleConfigArray.size() - 1;
            if (!loadUxIconFirstly) {
                pos = pos2;
                iconNamePrefix2 = iconNamePrefix;
            } else if (pos2 < 0 || pos2 >= this.mCommonStyleConfigArray.size()) {
                pos = pos2;
                iconNamePrefix2 = iconNamePrefix;
                int specialPos = this.mSpecialStyleConfigArray.indexOf(Integer.valueOf(sIconConfig.getTheme()));
                if (specialPos >= 0 && specialPos < this.mSpecialStylePrefixArray.length) {
                    if (ColorUxIconAppCheckUtils.isPresetApp(packageName)) {
                        Drawable foreground3 = findAppDrawable(packageName, iconNamePrefix2 + getSpecialStylePrefix(specialPos), res, true, true);
                        if (foreground3 != null) {
                            return foreground3;
                        }
                        foreground2 = foreground3;
                    } else {
                        foreground2 = findAppDrawable(packageName, iconNamePrefix2 + getRectFgPrefix(), res, true, false);
                        background2 = findAppDrawable(packageName, iconNamePrefix2 + getRectBgPrefix(), res, true, false);
                        isPlatformDrawable = true;
                    }
                }
            } else if (pos2 == 2) {
                boolean isSystemApp = ColorUxIconAppCheckUtils.isPresetApp(packageName);
                if (isSystemApp) {
                    pos = pos2;
                    iconNamePrefix2 = iconNamePrefix;
                    foreground = findAppDrawable(packageName, iconNamePrefix + getCommonStylePrefixExceptRect(pos2), res, false, isSystemApp);
                    if (foreground != null) {
                        return foreground;
                    }
                } else {
                    pos = pos2;
                    iconNamePrefix2 = iconNamePrefix;
                    Drawable foreground4 = findAppDrawable(packageName, iconNamePrefix2 + getCommonStylePrefixExceptRect(pos), res, false, false);
                    if (foreground4 != null) {
                        return foreground4;
                    }
                    Drawable foreground5 = findAppDrawable(packageName, iconNamePrefix2 + getRectFgPrefix(), res, false, false);
                    background2 = findAppDrawable(packageName, iconNamePrefix2 + getRectBgPrefix(), res, false, false);
                    isPlatformDrawable = true;
                    foreground = foreground5;
                }
                foreground2 = foreground;
            } else {
                pos = pos2;
                iconNamePrefix2 = iconNamePrefix;
                if (pos == 0 || pos == customThemeConfigPos) {
                    boolean isSystemApp2 = ColorUxIconAppCheckUtils.isPresetApp(packageName);
                    foreground2 = findAppDrawable(packageName, iconNamePrefix2 + getRectFgPrefix(), res, false, isSystemApp2);
                    background2 = findAppDrawable(packageName, iconNamePrefix2 + getRectBgPrefix(), res, false, isSystemApp2);
                    isPlatformDrawable = true;
                } else if (pos == 1) {
                    Drawable foreground6 = findAppDrawable(packageName, iconNamePrefix2 + getCommonStylePrefixExceptRect(pos), res, false, ColorUxIconAppCheckUtils.isPresetApp(packageName));
                    if (foreground6 != null) {
                        return foreground6;
                    }
                    foreground2 = foreground6;
                }
            }
            if (ColorUxIconConstants.DEBUG_UX_ICON) {
                Log.v(TAG, "loadIcon packageName = " + packageName + ",sIconConfig =:" + sIconConfig);
            }
            boolean isAdaptiveIconDrawable2 = false;
            if (foreground2 == null) {
                Drawable drawable3 = packageManager.getDrawable(packageName, id, applicationInfo2);
                if (drawable3 == null) {
                    return null;
                }
                if (!ColorUxIconAppCheckUtils.isDeskActivity(packageName)) {
                    int sizeThreshold = (int) ((res.getDisplayMetrics().density * 40.0f) + 0.5f);
                    if (pos != 1) {
                        if (drawable3 instanceof AdaptiveIconDrawable) {
                            foreground2 = ((AdaptiveIconDrawable) drawable3).getForeground();
                            background = ((AdaptiveIconDrawable) drawable3).getBackground();
                            isAdaptiveIconDrawable = true;
                        } else {
                            if (pos == 2) {
                                drawable = drawable3;
                            } else if (pos == 0) {
                                drawable = drawable3;
                            } else if (drawable3.getIntrinsicWidth() < sizeThreshold || hasTransparentPixels(drawable3)) {
                                return buildAdaptiveIconDrawableForThirdParty(res, drawable3, this.mDefaultBackgroundDrawable, (int) (((res.getDisplayMetrics().density * 34.0f) + 0.5f) * ((((float) ColorUxIconConfigParser.getPxFromIconConfigDp(res, sIconConfig.getForegroundSize())) * 1.0f) / ((float) res.getDimensionPixelSize(201655817)))), ColorUxIconConfigParser.getPxFromIconConfigDp(res, sIconConfig.getIconSize()));
                            } else {
                                background = drawable3;
                                isAdaptiveIconDrawable = false;
                            }
                            if (drawable.getIntrinsicWidth() >= sizeThreshold) {
                                drawable2 = drawable;
                                if (!hasTransparentPixels(drawable2)) {
                                    background = drawable2;
                                    isAdaptiveIconDrawable = false;
                                }
                            } else {
                                drawable2 = drawable;
                            }
                            return buildAdaptiveIconDrawableForThirdParty(res, drawable2, this.mDefaultBackgroundDrawable, (int) ((res.getDisplayMetrics().density * 34.0f) + 0.5f), ColorUxIconConfigParser.getPxFromIconConfigDp(res, sIconConfig.getIconSize()));
                        }
                        background2 = background;
                        isAdaptiveIconDrawable2 = isAdaptiveIconDrawable;
                        isPlatformDrawable = false;
                    } else if (hasTransparentPixels(drawable3)) {
                        return buildAdaptiveIconDrawableForThirdParty(res, drawable3, this.mDefaultBackgroundDrawable, (int) (((res.getDisplayMetrics().density * 34.0f) + 0.5f) * 1.25f), ColorUxIconConfigParser.getPxFromIconConfigDp(res, sIconConfig.getForegroundSize()));
                    } else {
                        return buildAdaptiveIconDrawableForThirdParty(res, foreground2, drawable3, ColorUxIconConfigParser.getPxFromIconConfigDp(res, sIconConfig.getForegroundSize()), ColorUxIconConfigParser.getPxFromIconConfigDp(res, sIconConfig.getForegroundSize()));
                    }
                } else if (pos == 1) {
                    return buildAdaptiveIconDrawable(res, drawable3, null, false, false);
                } else {
                    return buildAdaptiveIconDrawable(res, null, drawable3, false, false);
                }
            }
            return buildAdaptiveIconDrawable(res, foreground2, background2, isPlatformDrawable, isAdaptiveIconDrawable2);
        } catch (PackageManager.NameNotFoundException e2) {
            return null;
        }
    }

    private void init() {
        this.mDefaultBackgroundDrawable = new ColorDrawable(Color.parseColor(ColorUxIconConstants.IconLoader.DEFAULT_BACKGROUND_COLOR));
        this.mDefaultTransparentBackgroundDrawable = new ColorDrawable(0);
    }

    private boolean isExpVersion() {
        if (this.mIsExpVersion == null) {
            synchronized (ColorUXIconLoader.class) {
                if (this.mIsExpVersion == null) {
                    initIsExpVersionValues();
                    if (this.mIsExpVersion == null) {
                        return false;
                    }
                }
            }
        }
        return this.mIsExpVersion.booleanValue();
    }

    private void initIsExpVersionValues() {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (packageManager != null) {
            try {
                this.mIsExpVersion = Boolean.valueOf(packageManager.hasSystemFeature(ColorUxIconConstants.SystemProperty.FEATURE_OPPO_VERSION_EXP, 0));
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException --> " + e.getMessage());
            }
        }
        if (ColorUxIconConstants.DEBUG_UX_ICON) {
            Log.v(TAG, "mIsExpVersion = " + this.mIsExpVersion);
        }
    }

    private void initConfigArray(Resources res) {
        int[] commonThemeConfigArray;
        int[] specialThemeConfigArray;
        for (int i : res.getIntArray(201786403)) {
            this.mCommonStyleConfigArray.add(Integer.valueOf(i));
        }
        for (int i2 : res.getIntArray(201786407)) {
            this.mSpecialStyleConfigArray.add(Integer.valueOf(i2));
        }
        this.mCommonStylePrefixArray = res.getStringArray(201786402);
        this.mSpecialStylePrefixArray = res.getStringArray(201786406);
        if (isExpVersion()) {
            this.mCommonStylePathArray = res.getStringArray(201786408);
        } else {
            this.mCommonStylePathArray = res.getStringArray(201786400);
        }
        this.mSpecialStylePathArray = res.getStringArray(201786404);
    }

    private String buildIconPath(String baseFilePath, String packageName, String targetIconName) {
        StringBuilder sb = new StringBuilder(baseFilePath);
        sb.append(packageName);
        sb.append(ColorUxIconConstants.IconLoader.FILE_SEPARATOR);
        sb.append(targetIconName);
        String iconPath = sb.append(ColorUxIconConstants.IconLoader.PNG_REG).toString();
        if (ColorUxIconConstants.DEBUG_UX_ICON) {
            Log.v(TAG, "loadUXIconByPath iconPath =:" + packageName + ",iconPath =:" + iconPath);
        }
        return iconPath;
    }

    private Drawable findAppDrawable(String packageName, String iconName, Resources res, boolean isSpecialStyle, boolean isSystemApp) {
        Trace.traceBegin(8192, "#UxIcon.getDrawable.findAppDrawable");
        Drawable drawable = null;
        String str = ColorUxIconConstants.IconLoader.BASE_SYSTEM_DEFAULT_THEME_FILE_PATH;
        if (isSystemApp) {
            drawable = loadUXIconByPath(buildIconPath(ColorUxIconConstants.IconLoader.BASE_UX_ICONS_FILE_PATH, packageName, iconName), res);
            if (drawable == null) {
                if (isSpecialStyle) {
                    str = ColorUxIconConstants.IconLoader.BASE_PRODUCT_DEFAULT_THEME_FILE_PATH;
                }
                drawable = loadUXIconByPath(buildIconPath(str, packageName, iconName), res);
            }
        } else if (sIconConfig.isArtPlusOn() && (drawable = loadUXIconByPath(buildIconPath(ColorUxIconConstants.IconLoader.BASE_UX_ICONS_FILE_PATH, packageName, iconName), res)) == null) {
            drawable = loadUXIconByPath(buildIconPath(str, packageName, iconName), res);
        }
        Trace.traceEnd(8192);
        return drawable;
    }

    private Drawable buildAdaptiveIconDrawable(Resources res, Drawable foreground, Drawable background, boolean isPlatformDrawable, boolean isAdaptiveIconDrawable) {
        if (foreground == null && background == null) {
            return null;
        }
        CustomAdaptiveIconConfig config = new CustomAdaptiveIconConfig.Builder(res).setCustomIconSize(ColorUxIconConfigParser.getPxFromIconConfigDp(res, sIconConfig.getIconSize())).setCustomIconFgSize(ColorUxIconConfigParser.getPxFromIconConfigDp(res, sIconConfig.getForegroundSize())).setCustomMask(new Path(sIconConfig.getShapePath())).setIsPlatformDrawable(isPlatformDrawable).setIsAdaptiveIconDrawable(isAdaptiveIconDrawable).create();
        ColorAdaptiveIconDrawable iconDrawable = new ColorAdaptiveIconDrawable(background, foreground, config);
        if (ColorUxIconConstants.DEBUG_UX_ICON) {
            Log.v(TAG, "buildAdaptiveIconDrawable foreground =:" + foreground + ",background =:" + background + ",config =:" + config);
        }
        return iconDrawable;
    }

    private Drawable buildAdaptiveIconDrawableForThirdParty(Resources res, Drawable foreground, Drawable background, int foregroundSize, int backgroundSize) {
        if (foreground == null && background == null) {
            return null;
        }
        CustomAdaptiveIconConfig config = new CustomAdaptiveIconConfig.Builder(res).setCustomIconSize(backgroundSize).setCustomIconFgSize(foregroundSize).setCustomMask(new Path(sIconConfig.getShapePath())).setIsPlatformDrawable(false).setIsAdaptiveIconDrawable(false).create();
        ColorAdaptiveIconDrawable iconDrawable = new ColorAdaptiveIconDrawable(background, foreground, config);
        if (ColorUxIconConstants.DEBUG_UX_ICON) {
            Log.v(TAG, "buildAdaptiveIconDrawable foreground =:" + foreground + ",background =:" + background + ",config =:" + config);
        }
        return iconDrawable;
    }

    private Drawable loadUXIconByPath(String path, Resources res) {
        if (!TextUtils.isEmpty(path)) {
            if (ColorUxIconConstants.DEBUG_UX_ICON) {
                Log.v(TAG, "loadUXIconByPath path = " + path);
            }
            return getDrawableFromPath(path, res);
        } else if (!ColorUxIconConstants.DEBUG_UX_ICON) {
            return null;
        } else {
            Log.v(TAG, "loadUXIconByPath isEmpty(path).");
            return null;
        }
    }

    private Drawable getBackgroundDrawable(Drawable src) {
        Bitmap temp = getBitmapFromDrawable(src);
        int backgroundColor = ColorPalette.from(temp).generateEdageWithStep(10, 20).getTransMaxColor(-1);
        if (backgroundColor == -1) {
            return this.mDefaultBackgroundDrawable;
        }
        Drawable drawable = new ColorDrawable(backgroundColor);
        temp.recycle();
        return drawable;
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    private String getSpecialStylePrefix(int pos) {
        String[] strArr = this.mSpecialStylePrefixArray;
        if (pos < strArr.length) {
            return strArr[pos];
        }
        return "";
    }

    private String getCommonStylePrefixExceptRect(int pos) {
        int i = pos + 1;
        String[] strArr = this.mCommonStylePrefixArray;
        if (i < strArr.length) {
            return strArr[pos + 1];
        }
        return "";
    }

    private String getRectFgPrefix() {
        return this.mCommonStylePrefixArray[0];
    }

    private String getRectBgPrefix() {
        return this.mCommonStylePrefixArray[1];
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0022, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0023, code lost:
        r4.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0026, code lost:
        throw r5;
     */
    private Drawable getDrawableFromPath(String pathName, Resources res) {
        if (pathName == null || res == null) {
            return null;
        }
        Trace.traceBegin(8192, pathName);
        try {
            FileInputStream stream = new FileInputStream(pathName);
            Drawable createFromResourceStream = Drawable.createFromResourceStream(res, null, stream, pathName, null);
            stream.close();
            return createFromResourceStream;
        } catch (IOException e) {
            return null;
        } finally {
            Trace.traceEnd(8192);
        }
    }

    private boolean hasTransparentPixels(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            bitmap = getBitmapFromDrawable(drawable);
        }
        int transparentCount = 0;
        int pixels = 0;
        int xStep = (int) Math.ceil((double) ((((float) bitmap.getWidth()) * 1.0f) / 4.0f));
        int yStep = (int) Math.ceil((double) ((((float) bitmap.getHeight()) * 1.0f) / 4.0f));
        int i = xStep;
        while (i < bitmap.getWidth()) {
            try {
                for (int j = 1; j < 4; j++) {
                    if (Color.alpha(bitmap.getPixel(i, j)) < 220) {
                        pixels++;
                    }
                    if (j == 3 && pixels > 1) {
                        transparentCount++;
                    }
                }
                pixels = 0;
                i += xStep;
            } catch (IllegalArgumentException e) {
            }
        }
        for (int i2 = xStep; i2 < bitmap.getWidth(); i2 += xStep) {
            for (int j2 = bitmap.getHeight() - 2; j2 > (bitmap.getHeight() - 4) - 1; j2--) {
                if (Color.alpha(bitmap.getPixel(i2, j2)) < 220) {
                    pixels++;
                }
                if (j2 == bitmap.getHeight() - 4 && pixels > 1) {
                    transparentCount++;
                }
            }
            pixels = 0;
        }
        for (int i3 = yStep; i3 < bitmap.getHeight(); i3 += yStep) {
            for (int j3 = 1; j3 < 4; j3++) {
                if (Color.alpha(bitmap.getPixel(j3, i3)) < 220) {
                    pixels++;
                }
                if (j3 == 3 && pixels > 1) {
                    transparentCount++;
                }
            }
            pixels = 0;
        }
        for (int i4 = yStep; i4 < bitmap.getHeight(); i4 += yStep) {
            for (int j4 = bitmap.getWidth() - 2; j4 > (bitmap.getWidth() - 4) - 1; j4--) {
                if (Color.alpha(bitmap.getPixel(j4, i4)) < 220) {
                    pixels++;
                }
                if (j4 == bitmap.getWidth() - 4 && pixels > 1) {
                    transparentCount++;
                }
            }
            pixels = 0;
        }
        if (transparentCount >= 6) {
            return true;
        }
        return false;
    }
}
