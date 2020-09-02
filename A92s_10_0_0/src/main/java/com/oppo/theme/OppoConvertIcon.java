package com.oppo.theme;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
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
    private static final int NORMAL_ICON_SIZE = 156;
    private static final int NORMAL_SCREEN_WIDTH = 1080;
    private static final String TAG = "OppoConvertIcon";
    private static int mUserId;
    private static final Canvas sCanvas = new Canvas();
    private static String sCoverBackgroundPic = null;
    private static int sDetectMaskBorderOffset = 10;
    private static final String[] sDrawableDirs = {"res/drawable-hdpi/", "res/drawable-xhdpi/", "res/drawable-xxhdpi/"};
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

    /* JADX WARNING: Code restructure failed: missing block: B:60:0x011c, code lost:
        return r7;
     */
    public static Bitmap convertIconBitmap(Drawable icon, Resources res, boolean isThirdPart, boolean forceCutAndScale) {
        Bitmap.Config bitmapConfig;
        if (icon == null) {
            return null;
        }
        synchronized (sCanvas) {
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
                Bitmap bitmap = computeDesity(((BitmapDrawable) icon).getBitmap(), res);
                if (isThirdPart && ((bitmapConfig = (originalBitmap = bitmap).getConfig()) == null || Bitmap.Config.RGBA_F16.equals(bitmapConfig))) {
                    Log.i(TAG, "convertIconBitmap...set the bitmap config to ARGB_8888. bitmapConfig = " + bitmapConfig);
                    originalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                }
            } else if ((icon instanceof NinePatchDrawable) || (icon instanceof VectorDrawable) || (icon instanceof AdaptiveIconDrawable)) {
                originalBitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(originalBitmap);
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                icon.draw(canvas);
            }
            if (sIconWidth <= 0) {
                return null;
            }
            Bitmap bitmap2 = Bitmap.createBitmap(sIconWidth, sIconHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas2 = sCanvas;
            canvas2.setBitmap(bitmap2);
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
                icon.draw(canvas2);
                icon.setBounds(sOldBounds);
                canvas2.setBitmap(null);
            } else if (forceCutAndScale) {
                cutAndScaleBitmap(icon, originalBitmap, res, canvas2);
            } else if (sIconBgType == IconBgType.COVER) {
                coverBitmap(icon, originalBitmap, res, canvas2);
                if (sNeedDrawForeground) {
                    drawForeground(res, canvas2);
                }
            } else if (sIconBgType != IconBgType.MASK || sMaskBitmap == null) {
                cutAndScaleBitmap(icon, originalBitmap, res, canvas2);
            } else {
                maskBitmap(icon, originalBitmap, res, canvas2);
                if (sNeedDrawForeground) {
                    drawForeground(res, canvas2);
                }
            }
        }
    }

    static void coverBitmapNoCut(Drawable icon, Bitmap originalBitmap, Resources res, Canvas canvas) {
        if (originalBitmap != null) {
            Drawable sIconBackground2 = OppoThirdPartUtil.getLauncherDrawableByName(res, sCoverBackgroundPic);
            if (sIconBackground2 != null) {
                sOldBounds.set(sIconBackground2.getBounds());
                sIconBackground2.setBounds(0, 0, sIconWidth, sIconHeight);
                sIconBackground2.draw(canvas);
                sIconBackground2.setBounds(sOldBounds);
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
                sIconBackground = OppoThirdPartUtil.getLauncherDrawableByNameForUser(res, sCoverBackgroundPic, mUserId);
            }
            Drawable drawable = sIconBackground;
            if (drawable != null) {
                sOldBounds.set(drawable.getBounds());
                sIconBackground.setBounds(0, 0, sIconWidth, sIconHeight);
                sIconBackground.draw(canvas);
                sIconBackground.setBounds(sOldBounds);
            }
            Bitmap scale = originalBitmap.getConfig() != null ? OppoMaskBitmapUtilities.getInstance().cutAndScaleBitmap(originalBitmap) : originalBitmap;
            if (scale != null) {
                canvas.drawBitmap(scale, (float) (((sIconWidth - scale.getWidth()) / 2) + sThemeParamXOffset), (float) (((sIconHeight - scale.getHeight()) / 2) + sThemeParamYOffset), (Paint) null);
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
                canvas.drawBitmap(scale, (float) ((sIconWidth - scale.getWidth()) / 2), (float) ((sIconHeight - scale.getHeight()) / 2), (Paint) null);
            } else {
                Log.i(TAG, "cutAndScaleBitmap -- scale == null");
            }
        }
    }

    static void maskBitmap(Drawable icon, Bitmap originalBitmap, Resources res, Canvas canvas) {
        if (originalBitmap == null) {
            originalBitmap = Bitmap.createBitmap(sIconWidth, sIconHeight, Bitmap.Config.ARGB_8888);
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
                sIconBackground = OppoThirdPartUtil.getLauncherDrawableByNameForUser(res, sMaskBackgroundPic, mUserId);
            }
            Drawable drawable = sIconBackground;
            if (drawable != null) {
                sOldBounds.set(drawable.getBounds());
                sIconBackground.setBounds(0, 0, sIconWidth, sIconHeight);
                sIconBackground.draw(canvas);
                sIconBackground.setBounds(sOldBounds);
            } else {
                Log.i(TAG, "maskBitmap -- sIconBackground == null");
            }
            if (scale != null) {
                int w = scale.getWidth();
                int h = scale.getHeight();
                int i = sIconWidth;
                int i2 = sThemeParamXOffset;
                if (((w - i) / 2) + i2 > -1) {
                    canvas.drawBitmap(scale, (float) ((i - w) / 2), (float) ((sIconHeight - h) / 2), (Paint) null);
                } else {
                    canvas.drawBitmap(scale, (float) (((i - w) / 2) + i2), (float) (((sIconHeight - h) / 2) + sThemeParamYOffset), (Paint) null);
                }
            } else {
                Log.i(TAG, "maskBitmap -- scale == null");
            }
        } else {
            Log.i(TAG, "maskBitmap -- originalBitmap == null");
        }
    }

    public static void drawForeground(Resources res, Canvas canvas) {
        if (sIconForeground == null) {
            sIconForeground = OppoThirdPartUtil.getLauncherDrawableByNameForUser(res, sMaskForegroundPic, mUserId);
        }
        Drawable drawable = sIconForeground;
        if (drawable != null) {
            sOldBounds.set(drawable.getBounds());
            sIconForeground.setBounds(0, 0, sIconWidth, sIconHeight);
            sIconForeground.draw(canvas);
            sIconForeground.setBounds(sOldBounds);
        }
    }

    public static void initThemeParamForUser(Resources res, String maskBg, String maskFg, String coverBg, int useId) {
        OppoIconParam oppoIconParam = new OppoIconParam("themeInfo.xml");
        oppoIconParam.parseXmlForUser(useId);
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
        sDetectMaskBorderOffset = (int) (((float) sIconSize) * oppoIconParam.getDetectMaskBorderOffset());
        setIconBgFgRes(maskBg, maskFg, coverBg);
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
        sDetectMaskBorderOffset = (int) (((float) sIconSize) * oppoIconParam.getDetectMaskBorderOffset());
        setIconBgFgRes(maskBg, maskFg, coverBg);
    }

    public static IconBgType getIconBgType(Resources resources) {
        String path = "/data/theme/";
        if (OppoThirdPartUtil.mIsDefaultTheme) {
            path = "/system/media/theme/default/";
        }
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(path + "com.oppo.launcher");
            if (judgePicExist(zipFile, IPHONE_STYLE_BG_NAME)) {
                if (judgePicExist(zipFile, IPHONE_STYLE_FG_NAME)) {
                    sNeedDrawForeground = true;
                }
                zipFile.close();
                IconBgType iconBgType = IconBgType.COVER;
                sIconBgType = iconBgType;
                try {
                    zipFile.close();
                } catch (IOException e) {
                    Log.e(TAG, "getIconBgType: e = " + e);
                }
                return iconBgType;
            } else if (judgePicExist(zipFile, NEW_IPHONE_STYLE_MASK_NAME)) {
                if (judgePicExist(zipFile, IPHONE_STYLE_FG_NAME)) {
                    sNeedDrawForeground = true;
                }
                zipFile.close();
                IconBgType iconBgType2 = IconBgType.MASK;
                sIconBgType = iconBgType2;
                try {
                    zipFile.close();
                } catch (IOException e2) {
                    Log.e(TAG, "getIconBgType: e = " + e2);
                }
                return iconBgType2;
            } else {
                try {
                    zipFile.close();
                } catch (IOException e3) {
                    Log.e(TAG, "getIconBgType: e = " + e3);
                }
                IconBgType iconBgType3 = IconBgType.SCALE;
                sIconBgType = iconBgType3;
                return iconBgType3;
            }
        } catch (IOException e4) {
            Log.e(TAG, "getIconBgType: e = " + e4);
            IconBgType iconBgType4 = IconBgType.MASK;
            sIconBgType = iconBgType4;
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e5) {
                    Log.e(TAG, "getIconBgType: e = " + e5);
                }
            }
            return iconBgType4;
        } catch (Throwable th) {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e6) {
                    Log.e(TAG, "getIconBgType: e = " + e6);
                }
            }
            throw th;
        }
    }

    public static IconBgType getIconBgTypeForUser(Resources resources, int userId) {
        String path = OppoThirdPartUtil.getThemePathForUser(userId);
        if (OppoThirdPartUtil.mIsDefaultTheme) {
            path = "/system/media/theme/default/";
        }
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(path + "com.oppo.launcher");
            if (judgePicExist(zipFile, IPHONE_STYLE_BG_NAME)) {
                if (judgePicExist(zipFile, IPHONE_STYLE_FG_NAME)) {
                    sNeedDrawForeground = true;
                }
                zipFile.close();
                IconBgType iconBgType = IconBgType.COVER;
                sIconBgType = iconBgType;
                try {
                    zipFile.close();
                } catch (IOException e) {
                    Log.e(TAG, "getIconBgType: e = " + e);
                }
                return iconBgType;
            } else if (judgePicExist(zipFile, NEW_IPHONE_STYLE_MASK_NAME)) {
                if (judgePicExist(zipFile, IPHONE_STYLE_FG_NAME)) {
                    sNeedDrawForeground = true;
                }
                zipFile.close();
                IconBgType iconBgType2 = IconBgType.MASK;
                sIconBgType = iconBgType2;
                try {
                    zipFile.close();
                } catch (IOException e2) {
                    Log.e(TAG, "getIconBgType: e = " + e2);
                }
                return iconBgType2;
            } else {
                try {
                    zipFile.close();
                } catch (IOException e3) {
                    Log.e(TAG, "getIconBgType: e = " + e3);
                }
                IconBgType iconBgType3 = IconBgType.SCALE;
                sIconBgType = iconBgType3;
                return iconBgType3;
            }
        } catch (IOException e4) {
            Log.e(TAG, "getIconBgType: e = " + e4);
            IconBgType iconBgType4 = IconBgType.MASK;
            sIconBgType = iconBgType4;
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e5) {
                    Log.e(TAG, "getIconBgType: e = " + e5);
                }
            }
            return iconBgType4;
        } catch (Throwable th) {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e6) {
                    Log.e(TAG, "getIconBgType: e = " + e6);
                }
            }
            throw th;
        }
    }

    public static boolean judgePicExist(String zipFilePath, String picName) {
        StringBuilder sb;
        boolean exist = false;
        ZipFile file = null;
        try {
            file = new ZipFile(zipFilePath);
            exist = judgePicExist(file, picName);
            try {
                file.close();
            } catch (Exception e) {
                e = e;
                sb = new StringBuilder();
            }
        } catch (IOException e2) {
            Log.e(TAG, "judgePicExist: e " + e2);
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e4) {
                    Log.e(TAG, "judgePicExist: Closing e " + e4);
                }
            }
            throw th;
        }
        return exist;
        sb.append("judgePicExist: Closing e ");
        sb.append(e);
        Log.e(TAG, sb.toString());
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

    public static Bitmap getMaskBitmapForUser(Resources res, String picName, int userId) {
        Bitmap bitmap = sMaskBitmap;
        if (bitmap != null) {
            bitmap.recycle();
            sMaskBitmap = null;
        }
        Drawable mask = OppoThirdPartUtil.getLauncherDrawableByNameForUser(res, picName, userId);
        if (sIconWidth == -1) {
            initIconSize(res);
        }
        sMaskBitmap = Bitmap.createBitmap(sIconWidth, sIconHeight, Bitmap.Config.ARGB_8888);
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

    public static Bitmap getMaskBitmap(Resources res, String picName) {
        Bitmap bitmap = sMaskBitmap;
        if (bitmap != null) {
            bitmap.recycle();
            sMaskBitmap = null;
        }
        Drawable mask = OppoThirdPartUtil.getLauncherDrawableByName(res, picName);
        if (sIconWidth == -1) {
            initIconSize(res);
        }
        sMaskBitmap = Bitmap.createBitmap(sIconWidth, sIconHeight, Bitmap.Config.ARGB_8888);
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

    public static void initConvertIconForUser(Resources res, int userId) {
        mUserId = userId;
        OppoThirdPartUtil.setDefaultTheme(userId);
        IconBgType iconBgType = getIconBgTypeForUser(res, userId);
        initThemeParamForUser(res, NEW_IPHONE_STYLE_BG_NAME, IPHONE_STYLE_FG_NAME, IPHONE_STYLE_BG_NAME, userId);
        if (iconBgType == IconBgType.MASK) {
            OppoMaskBitmapUtilities.getInstance().setMaskBitmap(getMaskBitmapForUser(res, NEW_IPHONE_STYLE_MASK_NAME, userId), sDetectMaskBorderOffset);
        }
        OppoMaskBitmapUtilities.getInstance().setCutAndScalePram(getIconSize(), getThemeParamScale());
    }

    public static void initConvertIcon(Resources res) {
    }

    private static void setIconBgFgRes(String maskBg, String maskFg, String coverBg) {
        sMaskBackgroundPic = maskBg;
        sMaskForegroundPic = maskFg;
        sCoverBackgroundPic = coverBg;
        sIconBackground = null;
        sIconForeground = null;
    }

    private static void initIconSize(Resources res) {
        DisplayMetrics dm;
        int width = 156;
        if (!(res == null || (dm = res.getDisplayMetrics()) == null)) {
            width = (dm.widthPixels * 156) / 1080;
        }
        sIconSize = width;
        sIconWidth = width;
        sIconHeight = width;
    }

    private static Bitmap computeDesity(Bitmap bitmap, Resources resources) {
        if (resources == null) {
            return bitmap;
        }
        int dstDensity = resources.getDisplayMetrics().densityDpi;
        int density = bitmap.getDensity();
        float scale = 0.0f;
        if (density != 0) {
            scale = ((float) dstDensity) / ((float) density);
        }
        bitmap.setDensity(dstDensity);
        if (scale <= 1.0f) {
            return bitmap;
        }
        return Bitmap.createScaledBitmap(bitmap, (int) ((((float) bitmap.getWidth()) * scale) + 0.5f), (int) ((((float) bitmap.getHeight()) * scale) + 0.5f), true);
    }
}
