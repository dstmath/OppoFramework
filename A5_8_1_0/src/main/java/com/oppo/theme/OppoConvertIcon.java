package com.oppo.theme;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.VectorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import java.io.IOException;
import java.util.zip.ZipFile;

public final class OppoConvertIcon {
    private static final float COVER_ICON_RADIO = 0.62f;
    private static final boolean DEBUG_ENABLE = false;
    private static final boolean DEBUG_NORMAL = false;
    private static final float ICON_SIZE_RADIO_OVER_DENSITY_400 = 1.055f;
    private static final String IPHONE_STYLE_BG_NAME = "iphone_style_bg.png";
    private static final String IPHONE_STYLE_FG_NAME = "iphone_style_fg.png";
    private static final String LAUNCHER_ZIP_NAME = "com.oppo.launcher";
    private static final String NEW_IPHONE_STYLE_BG_NAME = "new_iphone_style_bg.png";
    private static final String NEW_IPHONE_STYLE_MASK_NAME = "new_iphone_style_mask.png";
    private static final int NORMAL_ICON_SIZE = 168;
    private static final int NORMAL_SCREEN_WIDTH = 1080;
    private static final String TAG = "OppoConvertIcon";
    private static final Canvas sCanvas = new Canvas();
    private static String sCoverBackgroundPic = null;
    private static final String[] sDrawableDirs = new String[]{"res/drawable-hdpi/", "res/drawable-xhdpi/", "res/drawable-xxhdpi/"};
    private static Drawable sIconBackground = null;
    private static IconBgType sIconBgType = IconBgType.MASK;
    private static Drawable sIconForeground = null;
    private static int sIconHeight = -1;
    private static int sIconSize = -1;
    private static int sIconWidth = -1;
    private static String sMaskBackgroundPic = null;
    private static Bitmap sMaskBitmap = null;
    private static String sMaskForegroundPic = null;
    private static boolean sNeedDrawForeground = false;
    private static final Rect sOldBounds = new Rect();
    private static int sThemeParamScale = 128;
    private static int sThemeParamXOffset = 0;
    private static int sThemeParamYOffset = 0;

    public enum IconBgType {
        MASK,
        COVER,
        SCALE
    }

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
    }

    public static boolean hasInit() {
        if (sCoverBackgroundPic == null && sMaskBackgroundPic == null && sMaskForegroundPic == null) {
            return false;
        }
        return true;
    }

    public static Bitmap convertIconBitmap(Drawable icon, Resources res, boolean isThirdPart) {
        return convertIconBitmap(icon, res, isThirdPart, false);
    }

    /* JADX WARNING: Missing block: B:43:0x00fb, code:
            return r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Bitmap convertIconBitmap(Drawable icon, Resources res, boolean isThirdPart, boolean forceCutAndScale) {
        if (icon == null) {
            return null;
        }
        synchronized (sCanvas) {
            Bitmap bitmap;
            Canvas canvas;
            if (sIconWidth == -1) {
                initIconSize(res);
            }
            int width = sIconWidth;
            int height = sIconHeight;
            Bitmap originalBitmap = null;
            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) icon).getBitmap();
                if (isThirdPart) {
                    originalBitmap = bitmap;
                    Config bitmapConfig = bitmap.getConfig();
                    if (bitmapConfig == null || Config.RGBA_F16.equals(bitmapConfig)) {
                        Log.i(TAG, "convertIconBitmap...set the bitmap config to ARGB_8888. bitmapConfig = " + bitmapConfig);
                        originalBitmap = bitmap.copy(Config.ARGB_8888, true);
                    }
                }
            } else if ((icon instanceof NinePatchDrawable) || (icon instanceof VectorDrawable) || (icon instanceof AdaptiveIconDrawable)) {
                originalBitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Config.ARGB_8888);
                canvas = new Canvas(originalBitmap);
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                icon.draw(canvas);
            }
            if (sIconWidth <= 0) {
                return null;
            }
            bitmap = Bitmap.createBitmap(sIconWidth, sIconHeight, Config.ARGB_8888);
            canvas = sCanvas;
            canvas.setBitmap(bitmap);
            if (!isThirdPart) {
                int sourceWidth = icon.getIntrinsicWidth();
                int sourceHeight = icon.getIntrinsicHeight();
                if (sourceWidth > 0 && sourceHeight > 0) {
                    float ratio = ((float) sourceWidth) / ((float) sourceHeight);
                    if (sourceWidth > sourceHeight) {
                        height = (int) (((float) width) / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (((float) height) * ratio);
                    }
                }
                int left = (sIconWidth - width) / 2;
                int top = (sIconHeight - height) / 2;
                sOldBounds.set(icon.getBounds());
                icon.setBounds(left, top, left + width, top + height);
                icon.draw(canvas);
                icon.setBounds(sOldBounds);
                canvas.setBitmap(null);
            } else if (forceCutAndScale) {
                cutAndScaleBitmap(icon, originalBitmap, res, canvas);
            } else {
                if (sIconBgType == IconBgType.COVER) {
                    coverBitmap(icon, originalBitmap, res, canvas);
                    if (sNeedDrawForeground) {
                        drawForeground(res, canvas);
                    }
                } else {
                    if (sIconBgType != IconBgType.MASK || sMaskBitmap == null) {
                        cutAndScaleBitmap(icon, originalBitmap, res, canvas);
                    } else {
                        maskBitmap(icon, originalBitmap, res, canvas);
                        if (sNeedDrawForeground) {
                            drawForeground(res, canvas);
                        }
                    }
                }
            }
        }
    }

    static void coverBitmapNoCut(Drawable icon, Bitmap originalBitmap, Resources res, Canvas canvas) {
        if (originalBitmap != null) {
            Drawable sIconBackground = OppoThirdPartUtil.getLauncherDrawableByName(res, sCoverBackgroundPic);
            if (sIconBackground != null) {
                sOldBounds.set(sIconBackground.getBounds());
                sIconBackground.setBounds(0, 0, sIconWidth, sIconHeight);
                sIconBackground.draw(canvas);
                sIconBackground.setBounds(sOldBounds);
            }
            float f = 1.0f;
            if (res.getDisplayMetrics().xdpi > 400.0f && OppoThirdPartUtil.mIsDefaultTheme) {
                f = ICON_SIZE_RADIO_OVER_DENSITY_400;
            }
            int width = (int) (((float) icon.getIntrinsicWidth()) * f);
            int height = (int) (((float) icon.getIntrinsicHeight()) * f);
            int l = (sIconWidth - width) / 2;
            int t = (sIconHeight - height) / 2;
            icon.setBounds(l, t, l + width, t + height);
            icon.draw(canvas);
        }
    }

    static void coverBitmap(Drawable icon, Bitmap originalBitmap, Resources res, Canvas canvas) {
        if (originalBitmap != null) {
            if (!originalBitmap.hasAlpha()) {
                originalBitmap.setHasAlpha(true);
            }
            if (sIconBackground == null) {
                sIconBackground = OppoThirdPartUtil.getLauncherDrawableByName(res, sCoverBackgroundPic);
            }
            if (sIconBackground != null) {
                sOldBounds.set(sIconBackground.getBounds());
                sIconBackground.setBounds(0, 0, sIconWidth, sIconHeight);
                sIconBackground.draw(canvas);
                sIconBackground.setBounds(sOldBounds);
            }
            Bitmap scale = originalBitmap.getConfig() != null ? OppoMaskBitmapUtilities.getInstance().cutAndScaleBitmap(originalBitmap) : originalBitmap;
            if (scale != null) {
                canvas.drawBitmap(scale, (float) (((sIconWidth - scale.getWidth()) / 2) + sThemeParamXOffset), (float) (((sIconHeight - scale.getHeight()) / 2) + sThemeParamYOffset), null);
            } else {
                Log.i(TAG, "coverBitmap -- scale == null");
            }
        }
    }

    static void cutAndScaleBitmap(Drawable icon, Bitmap originalBitmap, Resources res, Canvas canvas) {
        if (originalBitmap != null) {
            if (!originalBitmap.hasAlpha()) {
                originalBitmap.setHasAlpha(true);
            }
            Bitmap scale = originalBitmap.getConfig() != null ? OppoMaskBitmapUtilities.getInstance().cutAndScaleBitmap(originalBitmap) : originalBitmap;
            if (scale != null) {
                canvas.drawBitmap(scale, (float) ((sIconWidth - scale.getWidth()) / 2), (float) ((sIconHeight - scale.getHeight()) / 2), null);
            } else {
                Log.i(TAG, "cutAndScaleBitmap -- scale == null");
            }
        }
    }

    static void maskBitmap(Drawable icon, Bitmap originalBitmap, Resources res, Canvas canvas) {
        if (originalBitmap == null) {
            originalBitmap = Bitmap.createBitmap(sIconWidth, sIconHeight, Config.ARGB_8888);
            canvas.setBitmap(originalBitmap);
            sOldBounds.set(icon.getBounds());
            icon.setBounds(0, 0, sIconWidth, sIconHeight);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
        }
        if (originalBitmap != null) {
            if (!originalBitmap.hasAlpha()) {
                originalBitmap.setHasAlpha(true);
            }
            Bitmap scale = OppoMaskBitmapUtilities.getInstance().scaleAndMaskBitmap(originalBitmap);
            if (sIconBackground == null) {
                sIconBackground = OppoThirdPartUtil.getLauncherDrawableByName(res, sMaskBackgroundPic);
            }
            if (sIconBackground != null) {
                sOldBounds.set(sIconBackground.getBounds());
                sIconBackground.setBounds(0, 0, sIconWidth, sIconHeight);
                sIconBackground.draw(canvas);
                sIconBackground.setBounds(sOldBounds);
            } else {
                Log.i(TAG, "maskBitmap -- sIconBackground == null");
            }
            if (scale != null) {
                int w = scale.getWidth();
                int h = scale.getHeight();
                if (((w - sIconWidth) / 2) + sThemeParamXOffset > -1) {
                    canvas.drawBitmap(scale, (float) ((sIconWidth - w) / 2), (float) ((sIconHeight - h) / 2), null);
                    return;
                } else {
                    canvas.drawBitmap(scale, (float) (((sIconWidth - w) / 2) + sThemeParamXOffset), (float) (((sIconHeight - h) / 2) + sThemeParamYOffset), null);
                    return;
                }
            }
            Log.i(TAG, "maskBitmap -- scale == null");
            return;
        }
        Log.i(TAG, "maskBitmap -- originalBitmap == null");
    }

    public static void drawForeground(Resources res, Canvas canvas) {
        if (sIconForeground == null) {
            sIconForeground = OppoThirdPartUtil.getLauncherDrawableByName(res, sMaskForegroundPic);
        }
        if (sIconForeground != null) {
            sOldBounds.set(sIconForeground.getBounds());
            sIconForeground.setBounds(0, 0, sIconWidth, sIconHeight);
            sIconForeground.draw(canvas);
            sIconForeground.setBounds(sOldBounds);
        }
    }

    private static void setIconBgFgRes(String maskBg, String maskFg, String coverBg) {
        sMaskBackgroundPic = maskBg;
        sMaskForegroundPic = maskFg;
        sCoverBackgroundPic = coverBg;
        sIconBackground = null;
        sIconForeground = null;
    }

    private static void initIconSize(Resources res) {
        int width = NORMAL_ICON_SIZE;
        if (res != null) {
            DisplayMetrics dm = res.getDisplayMetrics();
            if (dm != null) {
                width = (dm.widthPixels * NORMAL_ICON_SIZE) / NORMAL_SCREEN_WIDTH;
            }
        }
        sIconSize = width;
        sIconWidth = width;
        sIconHeight = width;
    }

    public static void initThemeParam(Resources res, String maskBg, String maskFg, String coverBg) {
        OppoIconParam oppoIconParam = new OppoIconParam("themeInfo.xml");
        oppoIconParam.parseXml();
        float tempRatio = oppoIconParam.getScale();
        if (tempRatio <= 0.0f) {
            if (sIconBgType == IconBgType.COVER) {
                tempRatio = COVER_ICON_RADIO;
            } else if (sIconBgType == IconBgType.SCALE) {
                tempRatio = 1.0f;
            } else if (sIconBgType == IconBgType.MASK) {
                tempRatio = 1.0f;
            }
        }
        if (sIconSize == -1) {
            initIconSize(res);
        }
        sThemeParamScale = (int) (((float) sIconSize) * tempRatio);
        sThemeParamXOffset = (int) (((float) sIconSize) * oppoIconParam.getXOffset());
        sThemeParamYOffset = (int) (((float) sIconSize) * oppoIconParam.getYOffset());
        setIconBgFgRes(maskBg, maskFg, coverBg);
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x00e7 A:{SYNTHETIC, Splitter: B:43:0x00e7} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x010a A:{SYNTHETIC, Splitter: B:50:0x010a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static IconBgType getIconBgType() {
        IOException e;
        Throwable th;
        String path = "/data/theme/";
        if (OppoThirdPartUtil.mIsDefaultTheme) {
            path = "/system/media/theme/default/";
        }
        ZipFile zipFile = null;
        IconBgType iconBgType;
        try {
            ZipFile zipFile2 = new ZipFile(path + "com.oppo.launcher");
            try {
                if (judgePicExist(zipFile2, IPHONE_STYLE_BG_NAME)) {
                    if (judgePicExist(zipFile2, IPHONE_STYLE_FG_NAME)) {
                        sNeedDrawForeground = true;
                    }
                    zipFile2.close();
                    iconBgType = IconBgType.COVER;
                    sIconBgType = iconBgType;
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "getIconBgType: e = " + e2);
                        }
                    }
                    return iconBgType;
                } else if (judgePicExist(zipFile2, NEW_IPHONE_STYLE_MASK_NAME)) {
                    if (judgePicExist(zipFile2, IPHONE_STYLE_FG_NAME)) {
                        sNeedDrawForeground = true;
                    }
                    zipFile2.close();
                    iconBgType = IconBgType.MASK;
                    sIconBgType = iconBgType;
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e22) {
                            Log.e(TAG, "getIconBgType: e = " + e22);
                        }
                    }
                    return iconBgType;
                } else {
                    if (zipFile2 != null) {
                        try {
                            zipFile2.close();
                        } catch (IOException e222) {
                            Log.e(TAG, "getIconBgType: e = " + e222);
                        }
                    }
                    iconBgType = IconBgType.SCALE;
                    sIconBgType = iconBgType;
                    return iconBgType;
                }
            } catch (IOException e3) {
                e222 = e3;
                zipFile = zipFile2;
                try {
                    Log.e(TAG, "getIconBgType: e = " + e222);
                    iconBgType = IconBgType.MASK;
                    sIconBgType = iconBgType;
                    if (zipFile != null) {
                    }
                    return iconBgType;
                } catch (Throwable th2) {
                    th = th2;
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e2222) {
                            Log.e(TAG, "getIconBgType: e = " + e2222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                zipFile = zipFile2;
                if (zipFile != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e2222 = e4;
            Log.e(TAG, "getIconBgType: e = " + e2222);
            iconBgType = IconBgType.MASK;
            sIconBgType = iconBgType;
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e22222) {
                    Log.e(TAG, "getIconBgType: e = " + e22222);
                }
            }
            return iconBgType;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x006e A:{SYNTHETIC, Splitter: B:22:0x006e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean judgePicExist(String zipFilePath, String picName) {
        IOException e;
        Throwable th;
        boolean exist = false;
        ZipFile file = null;
        try {
            ZipFile file2 = new ZipFile(zipFilePath);
            try {
                exist = judgePicExist(file2, picName);
                if (file2 != null) {
                    try {
                        file2.close();
                    } catch (Exception e2) {
                        Log.e(TAG, "judgePicExist: Closing e " + e2);
                    }
                }
                file = file2;
            } catch (IOException e3) {
                e = e3;
                file = file2;
            } catch (Throwable th2) {
                th = th2;
                file = file2;
                if (file != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e = e4;
            try {
                Log.e(TAG, "judgePicExist: e " + e);
                if (file != null) {
                    try {
                        file.close();
                    } catch (Exception e22) {
                        Log.e(TAG, "judgePicExist: Closing e " + e22);
                    }
                }
                return exist;
            } catch (Throwable th3) {
                th = th3;
                if (file != null) {
                    try {
                        file.close();
                    } catch (Exception e222) {
                        Log.e(TAG, "judgePicExist: Closing e " + e222);
                    }
                }
                throw th;
            }
        }
        return exist;
    }

    public static boolean judgePicExist(ZipFile zipFile, String picName) {
        for (int i = sDrawableDirs.length - 1; i >= 0; i--) {
            if (zipFile.getEntry(sDrawableDirs[i] + picName) != null) {
                return true;
            }
        }
        return false;
    }

    public static Bitmap getMaskBitmap(Resources res, String picName) {
        if (sMaskBitmap != null) {
            sMaskBitmap.recycle();
            sMaskBitmap = null;
        }
        Drawable mask = OppoThirdPartUtil.getLauncherDrawableByName(res, picName);
        if (sIconWidth == -1) {
            initIconSize(res);
        }
        sMaskBitmap = Bitmap.createBitmap(sIconWidth, sIconHeight, Config.ARGB_8888);
        Canvas canvas = sCanvas;
        canvas.setBitmap(sMaskBitmap);
        if (mask != null) {
            mask.setBounds(0, 0, sIconWidth, sIconHeight);
            mask.draw(canvas);
        } else {
            canvas.drawColor(-16777216);
        }
        canvas.setBitmap(null);
        return sMaskBitmap;
    }

    public static int getThemeParamScale() {
        return sThemeParamScale;
    }

    public static int getIconSize() {
        return sIconSize;
    }

    public static void initConvertIcon(Resources res) {
        OppoThirdPartUtil.setDefaultTheme();
        if (getIconBgType() == IconBgType.MASK) {
            OppoMaskBitmapUtilities.getInstance().setMaskBitmap(getMaskBitmap(res, NEW_IPHONE_STYLE_MASK_NAME));
        }
        initThemeParam(res, NEW_IPHONE_STYLE_BG_NAME, IPHONE_STYLE_FG_NAME, IPHONE_STYLE_BG_NAME);
        OppoMaskBitmapUtilities.getInstance().setCutAndScalePram(getIconSize(), getThemeParamScale());
    }
}
