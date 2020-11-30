package com.color.darkmode;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.NinePatch;
import android.graphics.OppoBaseBaseCanvas;
import android.graphics.OppoBaseBitmap;
import android.graphics.OppoBaseColorFilter;
import android.graphics.OppoBasePath;
import android.graphics.OppoBaseShader;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.DisplayMetrics;
import com.android.internal.graphics.ColorUtils;
import com.color.util.ColorTypeCastingHelper;
import com.google.android.collect.Sets;
import java.util.Set;

public class ColorSoftDarkModeManager {
    private static final int BACKGROUND_BLACK = 2;
    private static final int BACKGROUND_WHITE = 1;
    private static final Set<String> DARKEN_PACKAGE = Sets.newHashSet(new String[]{"com.facebook.katana", "com.facebook.orca", "com.whatsapp"});
    private static final int FOREGROUND_BLACK = 6;
    private static final int FOREGROUND_COLORFUL = 5;
    private static int FOREGROUND_DOT_SIZE = 16;
    private static final int FOREGROUND_GRAY = 8;
    private static int FOREGROUND_PATH_ICON_SIZE = 92;
    private static final int FOREGROUND_WHITE = 7;
    private static final int HAS_COLORFUL = 3;
    private static int LINE_RANGE = 3;
    private static final ColorFilter LOW_LIGHT_FILTER = new LightingColorFilter(11184810, 0);
    private static final int MOST_COLORFUL = 4;
    private static final int SWITCH_BG_HEIGHT = 22;
    private static final int SWITCH_BG_WIDTH = 42;
    private static final ColorMatrixColorFilter TRANSFORM_TO_BLACK_FILTER = new ColorMatrixColorFilter(TRANSFORM_TO_BLACK_MATRIX);
    private static final ColorMatrix TRANSFORM_TO_BLACK_MATRIX = new ColorMatrix(new float[]{-1.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, -1.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, -1.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    private static final ColorFilter TRANSFORM_TO_DARKEN_FILTER = new LightingColorFilter(5592405, 0);
    private static final ColorMatrixColorFilter TRANSFORM_TO_WHITE_FILTER = new ColorMatrixColorFilter(TRANSFORM_TO_WHITE_MATRIX);
    private static final ColorMatrix TRANSFORM_TO_WHITE_MATRIX = new ColorMatrix(new float[]{-1.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, -1.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, -1.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
    private static float mDpDensity = (((float) DisplayMetrics.DENSITY_DEVICE_STABLE) / 160.0f);
    private static float mScreenWidth = 0.0f;
    private static ColorSoftDarkModeManager sColorSoftDarkModeManager;
    private Application mApplication;
    private final DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    private boolean mIsAppSupportDarkMode = false;
    private boolean mUseHardwareDraw = true;

    public static void initDarkModeStatus(Application application) {
        sColorSoftDarkModeManager = getInstance();
        sColorSoftDarkModeManager.mApplication = application;
    }

    public void setUseHardwareDraw(boolean useHardwareDraw) {
        this.mUseHardwareDraw = useHardwareDraw;
    }

    public static ColorSoftDarkModeManager getInstance() {
        if (sColorSoftDarkModeManager == null) {
            synchronized (ColorSoftDarkModeManager.class) {
                if (sColorSoftDarkModeManager == null) {
                    sColorSoftDarkModeManager = new ColorSoftDarkModeManager();
                }
            }
        }
        return sColorSoftDarkModeManager;
    }

    private boolean hasCalculatedColor(Bitmap bitmap) {
        OppoBaseBitmap baseBitmap;
        if (bitmap == null || (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bitmap)) == null) {
            return true;
        }
        return baseBitmap.hasCalculatedColor();
    }

    private void setHasCalculatedColor(Bitmap bitmap, boolean value) {
        OppoBaseBitmap baseBitmap;
        if (bitmap != null && (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bitmap)) != null) {
            baseBitmap.setHasCalculatedColor(value);
        }
    }

    private boolean isViewSrc(Bitmap bitmap) {
        OppoBaseBitmap baseBitmap;
        if (bitmap == null || (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bitmap)) == null) {
            return false;
        }
        return baseBitmap.isViewSrc();
    }

    /* JADX WARNING: Removed duplicated region for block: B:106:0x0189  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x018b  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x0197  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x025b  */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x026f  */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x0284  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0172  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x0174  */
    private void calculateBitmapColor(Bitmap bitmap, boolean isNinePatch, RectF rectF) {
        boolean isForegroundIcon;
        float allPixelNum;
        int grayPixelNum;
        int whitePixelNum;
        int i;
        int row;
        int grayPixelNum2;
        boolean isBlackState;
        float normalPixelNum;
        int colorfulPixelNum;
        float whitePercent;
        float blackPercent;
        ColorSoftDarkModeManager colorSoftDarkModeManager;
        Bitmap bitmap2;
        boolean z;
        boolean isPathIconArea;
        Bitmap bitmap3;
        boolean z2;
        ColorSoftDarkModeManager colorSoftDarkModeManager2;
        if (bitmap == null) {
            return;
        }
        if (!hasCalculatedColor(bitmap)) {
            if (bitmap.getConfig() != Bitmap.Config.HARDWARE) {
                int whitePixelNum2 = 0;
                int blackPixelNum = 0;
                int colorfulPixelNum2 = 0;
                int alphaPixelNum = 0;
                int grayPixelNum3 = 0;
                float allPixelNum2 = 0.0f;
                int heightSpacing = Math.max(1, bitmap.getHeight() / 10);
                int widthSpacing = Math.max(1, bitmap.getWidth() / 10);
                boolean isForegroundIcon2 = isForegroundIcon(rectF);
                if (!isWeChat(this.mApplication) || !isNinePatch) {
                    isForegroundIcon = isForegroundIcon2;
                } else {
                    float dpDensity = getDpDensity(this.mApplication);
                    isForegroundIcon = rectF.width() <= dpDensity * 30.0f && rectF.height() <= 30.0f * dpDensity;
                }
                boolean isPathIconArea2 = isPathIconArea(rectF);
                int i2 = 0;
                while (true) {
                    float f = 1.0f;
                    if (i2 >= bitmap.getWidth()) {
                        break;
                    }
                    int j = 0;
                    while (j < bitmap.getHeight()) {
                        allPixelNum2 += f;
                        int pixel = bitmap.getPixel(i2, j);
                        int alpha = Color.alpha(pixel);
                        if (alpha == 0) {
                            alphaPixelNum++;
                        } else if (isBitmapColorFulPixel(pixel, isPathIconArea2)) {
                            colorfulPixelNum2++;
                        } else if (isInWhiteRangeWithAlpha(pixel)) {
                            whitePixelNum2++;
                        } else if (isInBlackRangeWithAlpha(pixel)) {
                            blackPixelNum++;
                        } else if (alpha > 48) {
                            grayPixelNum3++;
                        }
                        j += heightSpacing;
                        f = 1.0f;
                    }
                    i2 += widthSpacing;
                }
                float colorfulPercent = 0.0f;
                if (allPixelNum2 == 0.0f) {
                    setHasCalculatedColor(bitmap, true);
                    return;
                }
                if (!isWeChat(this.mApplication) || ((float) alphaPixelNum) != allPixelNum2) {
                    whitePixelNum = whitePixelNum2;
                    i = blackPixelNum;
                    row = alphaPixelNum;
                    grayPixelNum = grayPixelNum3;
                    allPixelNum = allPixelNum2;
                    grayPixelNum2 = colorfulPixelNum2;
                } else {
                    int row2 = bitmap.getHeight() / 2;
                    for (int i3 = 0; i3 < bitmap.getWidth(); i3 += widthSpacing) {
                        allPixelNum2 += 1.0f;
                        int pixel2 = bitmap.getPixel(i3, row2);
                        int alpha2 = Color.alpha(pixel2);
                        if (alpha2 == 0) {
                            alphaPixelNum++;
                        } else if (isBitmapColorFulPixel(pixel2, isPathIconArea2)) {
                            colorfulPixelNum2++;
                        } else if (isInWhiteRangeWithAlpha(pixel2)) {
                            whitePixelNum2++;
                        } else if (isInBlackRangeWithAlpha(pixel2)) {
                            blackPixelNum++;
                        } else if (alpha2 > 48) {
                            grayPixelNum3++;
                        }
                    }
                    whitePixelNum = whitePixelNum2;
                    i = blackPixelNum;
                    row = alphaPixelNum;
                    grayPixelNum = grayPixelNum3;
                    allPixelNum = allPixelNum2;
                    grayPixelNum2 = colorfulPixelNum2;
                }
                float normalPixelNum2 = (float) (whitePixelNum + i + grayPixelNum);
                float notAlphaPixelNum = allPixelNum - ((float) row);
                float whitePercent2 = normalPixelNum2 == 0.0f ? 0.0f : ((float) whitePixelNum) / normalPixelNum2;
                float blackPercent2 = normalPixelNum2 == 0.0f ? 0.0f : ((float) i) / normalPixelNum2;
                if (notAlphaPixelNum != 0.0f) {
                    colorfulPercent = ((float) grayPixelNum2) / notAlphaPixelNum;
                }
                float alphaPercent = ((float) row) / allPixelNum;
                boolean isWhiteState = whitePercent2 > 0.8f && ((double) blackPercent2) <= 0.05d;
                if (blackPercent2 > 0.8f) {
                    normalPixelNum = normalPixelNum2;
                    colorfulPixelNum = grayPixelNum2;
                    if (((double) whitePercent2) <= 0.05d) {
                        isBlackState = true;
                        boolean isAlphaState = alphaPercent <= 0.6f;
                        boolean isGrayState = (i != 0 && whitePixelNum == 0) || (blackPercent2 >= 0.3f && whitePercent2 >= 0.3f);
                        boolean isMoreBlackPixel = i <= whitePixelNum;
                        if (!isWeChat(this.mApplication)) {
                            float dpDensity2 = getDpDensity(this.mApplication);
                            if (rectF.width() != rectF.height() || rectF.width() > 18.0f * dpDensity2) {
                                blackPercent = blackPercent2;
                                whitePercent = whitePercent2;
                                isPathIconArea = isPathIconArea2;
                                bitmap2 = bitmap;
                                colorSoftDarkModeManager = this;
                                z = true;
                            } else {
                                if (colorfulPercent > 0.08f) {
                                    handleColorful(bitmap, isNinePatch, isPathIconArea2, colorfulPercent, isWhiteState, isAlphaState, alphaPercent);
                                    bitmap3 = bitmap;
                                    colorSoftDarkModeManager2 = this;
                                    z2 = true;
                                } else if (isForegroundIcon) {
                                    z2 = true;
                                    bitmap3 = bitmap;
                                    colorSoftDarkModeManager2 = this;
                                    handleForegroundIcon(bitmap, isMoreBlackPixel, whitePercent2, blackPercent2, alphaPercent, isWhiteState, isGrayState);
                                } else {
                                    bitmap3 = bitmap;
                                    colorSoftDarkModeManager2 = this;
                                    z2 = true;
                                    if (isViewSrc(bitmap) || (!isAssetSource(bitmap) && !isNinePatch)) {
                                        handleViewSrcBitmap(bitmap, whitePercent2, blackPercent2, colorfulPercent, alphaPercent, isBlackState);
                                    } else if (isNinePatch) {
                                        handleNinePatch(bitmap, whitePercent2, blackPercent2, alphaPercent, isForegroundIcon);
                                    } else {
                                        handleNormalColor(bitmap, whitePercent2, blackPercent2, alphaPercent, isWhiteState, isBlackState, isGrayState, rectF);
                                    }
                                }
                                colorSoftDarkModeManager2.setHasCalculatedColor(bitmap3, z2);
                                return;
                            }
                        } else {
                            blackPercent = blackPercent2;
                            whitePercent = whitePercent2;
                            isPathIconArea = isPathIconArea2;
                            bitmap2 = bitmap;
                            colorSoftDarkModeManager = this;
                            z = true;
                        }
                        if (colorfulPercent <= 0.08f) {
                            handleColorful(bitmap, isNinePatch, isPathIconArea, colorfulPercent, isWhiteState, isAlphaState, alphaPercent);
                        } else if (isViewSrc(bitmap) || (!isAssetSource(bitmap) && !isNinePatch)) {
                            handleViewSrcBitmap(bitmap, whitePercent, blackPercent, colorfulPercent, alphaPercent, isBlackState);
                        } else if (isNinePatch) {
                            handleNinePatch(bitmap, whitePercent, blackPercent, alphaPercent, isForegroundIcon);
                        } else if (isForegroundIcon) {
                            handleForegroundIcon(bitmap, isMoreBlackPixel, whitePercent, blackPercent, alphaPercent, isWhiteState, isGrayState);
                        } else {
                            handleNormalColor(bitmap, whitePercent, blackPercent, alphaPercent, isWhiteState, isBlackState, isGrayState, rectF);
                        }
                        colorSoftDarkModeManager.setHasCalculatedColor(bitmap2, z);
                    }
                } else {
                    normalPixelNum = normalPixelNum2;
                    colorfulPixelNum = grayPixelNum2;
                }
                isBlackState = false;
                if (alphaPercent <= 0.6f) {
                }
                if (i != 0) {
                }
                if (i <= whitePixelNum) {
                }
                if (!isWeChat(this.mApplication)) {
                }
                if (colorfulPercent <= 0.08f) {
                }
                colorSoftDarkModeManager.setHasCalculatedColor(bitmap2, z);
            }
        }
    }

    private void setColorState(Bitmap bitmap, int colorState) {
        OppoBaseBitmap baseBitmap;
        if (bitmap != null && (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bitmap)) != null) {
            baseBitmap.setColorState(colorState);
        }
    }

    private void handleViewSrcBitmap(Bitmap bitmap, float whitePercent, float blackPercent, float colorfulPercent, float alphaPercent, boolean isBlackState) {
        if (alphaPercent == 1.0f) {
            setColorState(bitmap, 0);
        } else if (colorfulPercent >= 0.05f) {
            setColorState(bitmap, 5);
        } else if (alphaPercent <= 0.4f || alphaPercent > 0.8f) {
            if (alphaPercent > 0.8f) {
                if (blackPercent > 0.8f) {
                    setColorState(bitmap, 6);
                } else if (blackPercent >= whitePercent) {
                    setColorState(bitmap, 8);
                }
            }
            if (isTaoBao(this.mApplication) && whitePercent == 1.0f) {
                setColorState(bitmap, 1);
            }
        } else if (isBlackState) {
            setColorState(bitmap, 6);
        } else if (blackPercent >= whitePercent) {
            setColorState(bitmap, 8);
        }
    }

    private void handleColorful(Bitmap bitmap, boolean isNinePatch, boolean isPathIconArea, float colorfulPercent, boolean isWhiteState, boolean isAlphaState, float alphaPercent) {
        if (isNinePatch && isWhiteState && colorfulPercent <= 0.6f) {
            setColorState(bitmap, 1);
        } else if (isNinePatch && colorfulPercent >= 0.5f) {
            setColorState(bitmap, 4);
        } else if (isPathIconArea || isAlphaState) {
            setColorState(bitmap, 5);
        } else {
            setColorState(bitmap, 3);
        }
    }

    private void handleNormalColor(Bitmap bitmap, float whitePercent, float blackPercent, float alphaPercent, boolean isWhiteState, boolean isBlackState, boolean isGrayState, RectF rectF) {
        boolean isMaybeBackground = isMaybeBackground(rectF);
        int i = 1;
        if (alphaPercent > 0.25f) {
            int i2 = 8;
            int i3 = 6;
            if (alphaPercent <= 0.25f || alphaPercent > 0.5f) {
                if (alphaPercent <= 0.5f) {
                    return;
                }
                if (whitePercent >= 0.8f) {
                    setColorState(bitmap, FOREGROUND_WHITE);
                } else if (blackPercent >= 0.8f) {
                    setColorState(bitmap, 6);
                } else if (isGrayState) {
                    setColorState(bitmap, 8);
                }
            } else if (isWhiteState) {
                if (!isMaybeBackground) {
                    i = FOREGROUND_WHITE;
                }
                setColorState(bitmap, i);
            } else if (isBlackState) {
                if (isMaybeBackground) {
                    i3 = 2;
                }
                setColorState(bitmap, i3);
            } else if (isGrayState) {
                if (isMaybeBackground) {
                    i2 = 0;
                }
                setColorState(bitmap, i2);
            }
        } else if (isWhiteState) {
            setColorState(bitmap, 1);
        } else if (isBlackState) {
            setColorState(bitmap, 2);
        }
    }

    private void handleForegroundIcon(Bitmap bitmap, boolean isMoreBlackPixel, float whitePercent, float blackPercent, float alphaPercent, boolean isWhiteState, boolean isGrayState) {
        if (alphaPercent < 0.02f && whitePercent >= 0.95f) {
            setColorState(bitmap, 1);
        } else if (alphaPercent < 0.02f && blackPercent >= 0.95f) {
            setColorState(bitmap, 2);
        } else if (alphaPercent >= 0.5f) {
            if (whitePercent >= 0.5f) {
                setColorState(bitmap, FOREGROUND_WHITE);
            } else if (isGrayState) {
                setColorState(bitmap, 8);
            } else if (isMoreBlackPixel) {
                setColorState(bitmap, 6);
            }
        } else if (alphaPercent < 0.1f) {
        } else {
            if (isWhiteState) {
                setColorState(bitmap, FOREGROUND_WHITE);
            } else if (isGrayState) {
                setColorState(bitmap, 8);
            } else if (isMoreBlackPixel) {
                setColorState(bitmap, 6);
            }
        }
    }

    private void handleNinePatch(Bitmap bitmap, float whitePercent, float blackPercent, float alphaPercent, boolean isForeground) {
        int i = (whitePercent > 0.8f ? 1 : (whitePercent == 0.8f ? 0 : -1));
        int i2 = FOREGROUND_WHITE;
        if (i > 0) {
            if (!isForeground) {
                i2 = 1;
            }
            setColorState(bitmap, i2);
            return;
        }
        int i3 = 6;
        if (((double) alphaPercent) > 0.6d && blackPercent > 0.8f) {
            setColorState(bitmap, 6);
        } else if (blackPercent > 0.8f) {
            if (!isForeground) {
                i3 = 2;
            }
            setColorState(bitmap, i3);
        } else if (whitePercent + blackPercent > 0.9f && whitePercent >= 0.4f) {
            if (!isForeground) {
                i2 = 1;
            }
            setColorState(bitmap, i2);
        } else if (alphaPercent > 0.95f && blackPercent == 0.0f) {
            if (!isForeground) {
                i2 = 1;
            }
            setColorState(bitmap, i2);
        } else if (isFaceBook(this.mApplication) && whitePercent > 0.75f) {
            if (!isForeground) {
                i2 = 1;
            }
            setColorState(bitmap, i2);
        }
    }

    public void setIsSupportDarkModeStatus(int isSupportDarkMode) {
        boolean z = true;
        if (isSupportDarkMode != 1) {
            z = false;
        }
        this.mIsAppSupportDarkMode = z;
    }

    public boolean isInDarkMode() {
        return this.mIsAppSupportDarkMode;
    }

    public boolean isInDarkMode(boolean isHardware) {
        if (!this.mIsAppSupportDarkMode) {
            return false;
        }
        if (!isHardware || this.mUseHardwareDraw) {
            return true;
        }
        return false;
    }

    public void changePaintWhenDrawText(Paint paint) {
        if (isInDarkMode()) {
            int color = paint.getColor();
            if (isColorfulText(color) || !isInBlackRange(color)) {
                return;
            }
            if (isWeChat(this.mApplication) || isTaoBao(this.mApplication)) {
                paint.setColor(makeColorOSWeChatLight(color));
            } else {
                paint.setColor(makeColorOSLight(color));
            }
        }
    }

    public int changeWhenDrawColor(int color, boolean isDarkMode) {
        if (!isDarkMode || isColorfulColor(color) || !isInWhiteRange(color)) {
            return color;
        }
        return makeColorOSDark(color);
    }

    public void changePaintWhenDrawArea(Paint paint, RectF rectF, Path path) {
        if (isInDarkMode()) {
            Shader shader = paint.getShader();
            if (shader != null) {
                handleShader(paint, rectF, shader);
                return;
            }
            if (paint.getColorFilter() instanceof PorterDuffColorFilter) {
                if (canHandlePorterDuffColorFilter(paint, rectF, (PorterDuffColorFilter) paint.getColorFilter())) {
                    return;
                }
            } else if (paint.getColorFilter() instanceof BlendModeColorFilter) {
                BlendModeColorFilter colorFilter = (BlendModeColorFilter) paint.getColorFilter();
                if (colorFilter.getMode() == BlendMode.SRC_IN && canHandleBlendModeColorFilter(paint, rectF, colorFilter)) {
                    return;
                }
            }
            handleAreaColor(paint, rectF, path);
        }
    }

    public void changePaintWhenDrawArea(Paint paint, RectF rectF) {
        changePaintWhenDrawArea(paint, rectF, null);
    }

    private void handleAreaColor(Paint paint, RectF rectF, Path path) {
        int color = paint.getColor();
        if (color != 0 && !isColorfulColor(color)) {
            if ((path != null) && canHandlePathAreaColor(paint, rectF, path, color)) {
                return;
            }
            if (isLineStroke(paint, rectF)) {
                handleLineStroke(paint, color);
            } else if (isForegroundDot(rectF)) {
                if (isInBlackRange(color)) {
                    paint.setColor(makeColorOSLight(color));
                }
            } else if (isWeChat(this.mApplication) && isSwitchBg(paint, rectF)) {
                paint.setColor(makeColorOSLight(color));
            } else if (isInWhiteRange(color)) {
                if (isWhatsapp(this.mApplication)) {
                    float dpDensity = getDpDensity(this.mApplication);
                    if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() > ((float) LINE_RANGE) * dpDensity) {
                        paint.setColor(paint.getColor());
                        return;
                    }
                }
                paint.setColor(makeColorOSDark(color));
            }
        }
    }

    private void handleLineStroke(Paint paint, int color) {
        if (isInBlackRange(color)) {
            paint.setColor(makeColorOSLight(color));
        } else if (isInWhiteRange(color)) {
            paint.setColor(makeColorOSDark(color));
        }
    }

    private int getLinePorterDuffColor(int color) {
        if (isInBlackRange(color)) {
            return makeColorOSLight(color);
        }
        if (isInWhiteRange(color)) {
            return makeColorOSDark(color);
        }
        return color;
    }

    private boolean canHandlePathAreaColor(Paint paint, RectF rectF, Path path, int color) {
        if (!isPathForeGroundArea(paint, rectF, path)) {
            return false;
        }
        if (isLineStroke(paint, rectF)) {
            if (isWeChat(this.mApplication)) {
                return true;
            }
            handleLineStroke(paint, color);
        } else if (!isInBlackRange(color) || isWeChat(this.mApplication)) {
            return true;
        } else {
            paint.setColor(makeColorOSLight(color));
        }
        return true;
    }

    private boolean canHandlePorterDuffColorFilter(Paint paint, RectF rectF, PorterDuffColorFilter colorFilter) {
        int porterDuffColor = colorFilter.getColor();
        if (porterDuffColor == 0) {
            return false;
        }
        if (isColorfulColor(porterDuffColor)) {
            return true;
        }
        if (isLineStroke(paint, rectF)) {
            setPorterDuffColor(colorFilter, getLinePorterDuffColor(porterDuffColor));
            return true;
        } else if (isForegroundDot(rectF)) {
            if (isInBlackRange(porterDuffColor)) {
                setPorterDuffColor(colorFilter, makeColorOSLight(porterDuffColor));
            }
            return true;
        } else {
            if (isInWhiteRange(porterDuffColor)) {
                setPorterDuffColor(colorFilter, makeColorOSDark(porterDuffColor));
            }
            return true;
        }
    }

    private boolean canHandleBlendModeColorFilter(Paint paint, RectF rectF, BlendModeColorFilter colorFilter) {
        int color = colorFilter.getColor();
        if (color == 0) {
            return false;
        }
        if (isColorfulColor(color)) {
            return true;
        }
        if (isLineStroke(paint, rectF)) {
            changeBlendModeColorFilter(paint, colorFilter, getLinePorterDuffColor(color));
            return true;
        } else if (isForegroundDot(rectF)) {
            if (isInBlackRange(color)) {
                changeBlendModeColorFilter(paint, colorFilter, makeColorOSLight(color));
            }
            return true;
        } else {
            if (isInWhiteRange(color)) {
                changeBlendModeColorFilter(paint, colorFilter, makeColorOSDark(color));
            }
            return true;
        }
    }

    private void changeBlendModeColorFilter(Paint paint, BlendModeColorFilter oldFilter, int color) {
        if (oldFilter != null) {
            setPorterDuffColor(oldFilter, color);
        }
    }

    private void handleShader(Paint paint, RectF rectF, Shader shader) {
        OppoBaseShader baseShader;
        if (shader instanceof BitmapShader) {
            changePaintWhenDrawBitmap(paint, ((BitmapShader) shader).mBitmap, rectF);
        } else if (shader != null && (baseShader = (OppoBaseShader) ColorTypeCastingHelper.typeCasting(OppoBaseShader.class, shader)) != null) {
            paint.setShader(baseShader.getDarkModeShader());
        }
    }

    private boolean isCanvasBaseBitmap(Bitmap bitmap) {
        OppoBaseBitmap baseBitmap;
        if (bitmap == null || (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bitmap)) == null) {
            return false;
        }
        return baseBitmap.isCanvasBaseBitmap();
    }

    private boolean isAssetSource(Bitmap bitmap) {
        OppoBaseBitmap baseBitmap;
        if (bitmap == null || (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bitmap)) == null) {
            return false;
        }
        return baseBitmap.isAssetSource();
    }

    private int getColorState(Bitmap bitmap) {
        OppoBaseBitmap baseBitmap;
        if (bitmap == null || (baseBitmap = (OppoBaseBitmap) ColorTypeCastingHelper.typeCasting(OppoBaseBitmap.class, bitmap)) == null) {
            return 0;
        }
        return baseBitmap.getColorState();
    }

    private Paint changePaintWhenDrawBitmap(Paint paint, Bitmap bitmap, RectF rectF, boolean isNinePatch) {
        if (!isInDarkMode() || bitmap == null || bitmap.isRecycled()) {
            return paint;
        }
        if (isCanvasBaseBitmap(bitmap)) {
            if (canHandleColorFilterWhenIsBaseCanvas(paint, rectF)) {
                return paint;
            }
            if (!isNinePatch && isWeChatIcon(bitmap, rectF)) {
                return paint;
            }
        } else if (!isNinePatch && !isAssetSource(bitmap) && !isInAssetSourceBitmapRange(rectF) && 1 != 0 && !isTaoBao(this.mApplication)) {
            return paint;
        }
        boolean handleColorFilter = false;
        int color = 0;
        boolean isNeedToHandlePorter = false;
        boolean isPathIconArea = isPathIconArea(rectF);
        if (paint != null) {
            if (paint.getColorFilter() instanceof PorterDuffColorFilter) {
                color = ((PorterDuffColorFilter) paint.getColorFilter()).getColor();
                handleColorFilter = true;
            } else if (paint.getColorFilter() instanceof BlendModeColorFilter) {
                BlendModeColorFilter blendModeColorFilter = (BlendModeColorFilter) paint.getColorFilter();
                if (blendModeColorFilter.getMode() == BlendMode.SRC_IN) {
                    color = blendModeColorFilter.getColor();
                    handleColorFilter = true;
                }
            }
            if (handleColorFilter) {
                if (color != 0) {
                    isNeedToHandlePorter = true;
                }
                if (isWhatsapp(this.mApplication)) {
                    if (!isInBlackRange(color) && isBitmapColorFulPixel(color, isPathIconArea)) {
                        return paint;
                    }
                } else if (isBitmapColorFulPixel(color, isPathIconArea)) {
                    return paint;
                }
            }
        }
        calculateBitmapColor(bitmap, isNinePatch, rectF);
        switch (getColorState(bitmap)) {
            case 1:
                if (isNeedToHandlePorter && isInWhiteRange(color)) {
                    setBitmapPorterDuffColor(paint.getColorFilter(), makeColorOSDark(color), paint);
                    break;
                } else {
                    if (paint == null) {
                        paint = new Paint();
                    }
                    paint.setColorFilter(TRANSFORM_TO_BLACK_FILTER);
                    break;
                }
                break;
            case 2:
                if (isNeedToHandlePorter && isInWhiteRange(color)) {
                    setBitmapPorterDuffColor(paint.getColorFilter(), makeColorOSDark(color), paint);
                    break;
                }
            case 3:
                if (0 != 0) {
                    if (paint == null) {
                        paint = new Paint();
                    }
                    paint.setColorFilter(TRANSFORM_TO_DARKEN_FILTER);
                    break;
                }
                break;
            case 4:
                if (paint == null) {
                    paint = new Paint();
                }
                paint.setColorFilter(LOW_LIGHT_FILTER);
                break;
            case 5:
                if (isNeedToHandlePorter && isInBlackRange(color)) {
                    setBitmapPorterDuffColor(paint.getColorFilter(), makeColorOSLight(color), paint);
                    break;
                }
            case 6:
                if (isNeedToHandlePorter) {
                    if (isInBlackRange(color)) {
                        setBitmapPorterDuffColor(paint.getColorFilter(), makeColorOSLight(color), paint);
                        break;
                    }
                } else {
                    if (paint == null) {
                        paint = new Paint();
                    }
                    paint.setColorFilter(TRANSFORM_TO_WHITE_FILTER);
                    break;
                }
                break;
            case FOREGROUND_WHITE /* 7 */:
                if (isNeedToHandlePorter) {
                    if (!isInBlackRange(color)) {
                        if (isFaceBookMessenger(this.mApplication) && isInWhiteRange(color)) {
                            setBitmapPorterDuffColor(paint.getColorFilter(), makeColorOSDark(color), paint);
                            break;
                        }
                    } else {
                        setBitmapPorterDuffColor(paint.getColorFilter(), makeColorOSLight(color), paint);
                        break;
                    }
                }
                break;
            case 8:
                if (isNeedToHandlePorter && isInBlackRange(color)) {
                    setBitmapPorterDuffColor(paint.getColorFilter(), makeColorOSLight(color), paint);
                    break;
                }
        }
        return paint;
    }

    private boolean canHandleColorFilterWhenIsBaseCanvas(Paint paint, RectF rectF) {
        if (paint == null || !(paint.getColorFilter() instanceof PorterDuffColorFilter)) {
            return false;
        }
        PorterDuffColorFilter colorFilter = (PorterDuffColorFilter) paint.getColorFilter();
        boolean isPathForegroundArea = isPathIconArea(rectF);
        int color = colorFilter.getColor();
        if (color == 0) {
            return false;
        }
        if (!isBitmapColorFulPixel(color, isPathForegroundArea) && isPathForegroundArea && isInBlackRange(color)) {
            setPorterDuffColor(colorFilter, makeColorOSLight(color));
        }
        return true;
    }

    public Paint changePaintWhenDrawBitmap(Paint paint, Bitmap bitmap, RectF rectF) {
        return changePaintWhenDrawBitmap(paint, bitmap, rectF, false);
    }

    public Paint changePaintWhenDrawPatch(NinePatch ninePatch, Paint paint, RectF rectF) {
        return changePaintWhenDrawBitmap(paint, ninePatch.getBitmap(), rectF, true);
    }

    public static int getVectorColor(int color) {
        if (!isColorfulColor(color) && isInBlackRange(color)) {
            return makeColorOSLight(color);
        }
        return color;
    }

    public void changeColorFilterInDarkMode(ColorFilter colorFilter) {
        if (colorFilter instanceof PorterDuffColorFilter) {
            PorterDuffColorFilter porterDuffColorFilter = (PorterDuffColorFilter) colorFilter;
            int color = porterDuffColorFilter.getColor();
            if (isInBlackRange(color)) {
                setPorterDuffColor(porterDuffColorFilter, makeColorOSLight(color));
            }
        } else if (colorFilter instanceof BlendModeColorFilter) {
            BlendModeColorFilter blendModeColorFilter = (BlendModeColorFilter) colorFilter;
            if (blendModeColorFilter.getMode() == BlendMode.SRC_IN) {
                int color2 = blendModeColorFilter.getColor();
                if (isInBlackRange(color2)) {
                    setPorterDuffColor(blendModeColorFilter, makeColorOSLight(color2));
                }
            }
        }
    }

    private static boolean isInWhiteRange(int color) {
        return Color.red(color) > 185 && Color.green(color) > 185 && Color.blue(color) > 185;
    }

    private static boolean isInWhiteRangeWithAlpha(int color) {
        return Color.red(color) > 185 && Color.green(color) > 185 && Color.blue(color) > 185 && Color.alpha(color) > 48;
    }

    private static boolean isInBlackRangeWithAlpha(int color) {
        return Color.red(color) < 70 && Color.green(color) < 70 && Color.blue(color) < 70 && Color.alpha(color) > 48;
    }

    private static boolean isInBlackRange(int color) {
        return Color.red(color) < 70 && Color.green(color) < 70 && Color.blue(color) < 70;
    }

    private static boolean isInGrayRange(int color) {
        return Color.red(color) >= 128 && Color.green(color) >= 128 && Color.blue(color) >= 128;
    }

    private static boolean isColorfulColor(int pixel) {
        if (Color.alpha(pixel) == 0) {
            return false;
        }
        float[] hsv = new float[3];
        Color.colorToHSV(pixel, hsv);
        if (hsv[1] < 0.2f || hsv[2] < 0.2f) {
            return false;
        }
        return true;
    }

    private boolean isBitmapColorFulPixel(int pixel, boolean isPathForegroundArea) {
        if (Color.alpha(pixel) == 0) {
            return false;
        }
        float[] hsv = new float[3];
        Color.colorToHSV(pixel, hsv);
        if (isPathForegroundArea) {
            if (isWhatsapp(this.mApplication)) {
                if (hsv[1] < 0.2f || hsv[2] < 0.2f) {
                    return false;
                }
                return true;
            } else if (hsv[1] < 0.3f || hsv[2] < 0.3f) {
                return false;
            } else {
                return true;
            }
        } else if (hsv[1] <= 0.18f || hsv[2] <= 0.18f) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean isColorfulText(int color) {
        if (Color.alpha(color) == 0) {
            return false;
        }
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (hsv[1] < 0.3f || hsv[2] < 0.3f) {
            return false;
        }
        return true;
    }

    public static boolean isColorfulShader(int[] colors) {
        if (colors == null) {
            return false;
        }
        for (int color : colors) {
            if (isColorfulColor(color)) {
                return true;
            }
        }
        return false;
    }

    public static int[] getDarkModeColors(int[] colors) {
        if (colors == null) {
            return null;
        }
        int[] darkModeColor = (int[]) colors.clone();
        for (int i = 0; i < colors.length; i++) {
            darkModeColor[i] = getDarkModeColor(darkModeColor[i]);
        }
        return darkModeColor;
    }

    public static int getDarkModeColor(int color) {
        if (isInWhiteRange(color)) {
            return makeColorOSDark(color);
        }
        return color;
    }

    private static boolean isDividingLine(RectF rectF) {
        if (rectF == null) {
            return false;
        }
        float screenWidth = ensureScreenWidth(getInstance().mApplication);
        float dpDensity = getDpDensity(getInstance().mApplication);
        if (rectF.height() <= ((float) LINE_RANGE) * dpDensity && rectF.width() == screenWidth) {
            return false;
        }
        if (rectF.width() <= ((float) LINE_RANGE) * dpDensity || rectF.height() <= ((float) LINE_RANGE) * dpDensity) {
            return true;
        }
        return false;
    }

    private boolean isSwitchBg(Paint paint, RectF rectF) {
        if (rectF == null) {
            return false;
        }
        float dpDensity = getDpDensity(this.mApplication);
        if (((double) dpDensity) >= 3.5d && dpDensity <= 4.0f) {
            dpDensity = 4.0f;
        }
        if (rectF.width() > 42.0f * dpDensity || rectF.height() > 22.0f * dpDensity) {
            return false;
        }
        return true;
    }

    private boolean isLineStroke(Paint paint, RectF rectF) {
        if (paint.getStyle() != Paint.Style.STROKE) {
            return isDividingLine(rectF);
        }
        return paint.getStrokeWidth() <= ((float) LINE_RANGE) * getDpDensity(this.mApplication);
    }

    private boolean isAddArea(Path path) {
        OppoBasePath basePath;
        if (path == null || (basePath = (OppoBasePath) ColorTypeCastingHelper.typeCasting(OppoBasePath.class, path)) == null) {
            return false;
        }
        return basePath.isAddArea();
    }

    private boolean isPathForeGroundArea(Paint paint, RectF rectF, Path path) {
        if (isLineStroke(paint, rectF) || isForegroundDot(rectF)) {
            return true;
        }
        if (isPathIconArea(rectF)) {
            return !isAddArea(path);
        }
        return false;
    }

    private boolean isMaybeBackground(RectF rectF) {
        if (rectF == null || isPathIconArea(rectF) || rectF.height() == 0.0f || rectF.width() == 0.0f) {
            return false;
        }
        if (rectF.width() / rectF.height() >= 3.0f || rectF.height() / rectF.width() >= 3.0f) {
            return true;
        }
        return false;
    }

    private static boolean isForegroundIcon(RectF rectF) {
        if (rectF == null) {
            return false;
        }
        float dpDensity = getDpDensity(getInstance().mApplication);
        if (rectF.width() > dpDensity * 50.0f || rectF.height() > 50.0f * dpDensity) {
            return false;
        }
        return true;
    }

    private boolean isPathIconArea(RectF rectF) {
        if (rectF == null) {
            return false;
        }
        float dpDensity = getDpDensity(this.mApplication);
        if (dpDensity >= 3.5f && dpDensity <= 4.0f && isWeChat(this.mApplication)) {
            dpDensity = 4.0f;
        }
        if (rectF.width() > ((float) FOREGROUND_PATH_ICON_SIZE) * dpDensity || rectF.height() > ((float) FOREGROUND_PATH_ICON_SIZE) * dpDensity) {
            return false;
        }
        return true;
    }

    private static boolean isInAssetSourceBitmapRange(RectF rectF) {
        if (rectF == null) {
            return false;
        }
        float dpDensity = getDpDensity(getInstance().mApplication);
        if (rectF.width() > dpDensity * 48.0f || rectF.height() > 48.0f * dpDensity) {
            return false;
        }
        return true;
    }

    private static boolean isForegroundDot(RectF rectF) {
        if (rectF == null) {
            return false;
        }
        try {
            if (isWeChat(getInstance().mApplication)) {
                FOREGROUND_DOT_SIZE = 23;
            }
        } catch (Exception e) {
        }
        float density = getDpDensity(getInstance().mApplication);
        if (rectF.width() >= ((float) FOREGROUND_DOT_SIZE) * density || rectF.height() >= ((float) FOREGROUND_DOT_SIZE) * density) {
            return false;
        }
        return true;
    }

    public OppoBaseBaseCanvas.RealPaintState getRealPaintState(Paint paint) {
        if (paint == null || !isInDarkMode()) {
            return null;
        }
        OppoBaseBaseCanvas.RealPaintState realPaintState = new OppoBaseBaseCanvas.RealPaintState();
        realPaintState.color = paint.getColor();
        realPaintState.colorFilter = paint.getColorFilter();
        realPaintState.shader = paint.getShader();
        if (paint.getColorFilter() instanceof PorterDuffColorFilter) {
            realPaintState.porterDuffColor = ((PorterDuffColorFilter) paint.getColorFilter()).getColor();
        } else if (paint.getColorFilter() instanceof BlendModeColorFilter) {
            realPaintState.porterDuffColor = ((BlendModeColorFilter) paint.getColorFilter()).getColor();
        }
        return realPaintState;
    }

    public void resetRealPaintIfNeed(Paint paint, OppoBaseBaseCanvas.RealPaintState realPaintState) {
        if (paint != null && realPaintState != null && isInDarkMode()) {
            paint.setColor(realPaintState.color);
            paint.setColorFilter(realPaintState.colorFilter);
            paint.setShader(realPaintState.shader);
            if (paint.getColorFilter() instanceof PorterDuffColorFilter) {
                setPorterDuffColor(paint.getColorFilter(), realPaintState.porterDuffColor);
            } else if (paint.getColorFilter() instanceof BlendModeColorFilter) {
                setPorterDuffColor(paint.getColorFilter(), realPaintState.porterDuffColor);
            }
        }
    }

    private Paint setBitmapPorterDuffColor(ColorFilter filter, int color, Paint paint) {
        if (filter instanceof PorterDuffColorFilter) {
            OppoBaseColorFilter baseColorFilter = (OppoBaseColorFilter) ColorTypeCastingHelper.typeCasting(OppoBaseColorFilter.class, filter);
            if (baseColorFilter != null) {
                baseColorFilter.setColor(color);
            }
        } else if (filter instanceof BlendModeColorFilter) {
            BlendModeColorFilter oldFilter = (BlendModeColorFilter) filter;
            if (oldFilter.getMode() == BlendMode.SRC_IN && oldFilter.getColor() != color) {
                paint.setColorFilter(new BlendModeColorFilter(color, oldFilter.getMode()));
            }
        }
        return paint;
    }

    private void setPorterDuffColor(ColorFilter filter, int color) {
        OppoBaseColorFilter baseColorFilter;
        if (filter != null && (baseColorFilter = (OppoBaseColorFilter) ColorTypeCastingHelper.typeCasting(OppoBaseColorFilter.class, filter)) != null) {
            baseColorFilter.setColor(color);
        }
    }

    private static float ensureScreenWidth(Application application) {
        if (!(mScreenWidth != 0.0f || application == null || application.getApplicationContext() == null || application.getApplicationContext().getResources() == null || application.getApplicationContext().getResources().getDisplayMetrics() == null)) {
            mScreenWidth = (float) application.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        }
        return mScreenWidth;
    }

    private static float getDpDensity(Application application) {
        if (!(mDpDensity != 1.0f || application == null || application.getApplicationContext() == null || application.getApplicationContext().getResources() == null || application.getApplicationContext().getResources().getDisplayMetrics() == null)) {
            mDpDensity = ((float) application.getApplicationContext().getResources().getDisplayMetrics().densityDpi) / 160.0f;
        }
        return mDpDensity;
    }

    public static int makeColorOSDark(int color) {
        double[] lab = new double[3];
        ColorUtils.colorToLAB(color, lab);
        double newL = 100.0d - lab[0];
        if (useDarkenStyle(sColorSoftDarkModeManager.mApplication)) {
            newL = Math.max(20.0d, newL);
        }
        if (newL >= lab[0]) {
            return color;
        }
        lab[0] = newL;
        int newColor = ColorUtils.LABToColor(lab[0], lab[1], lab[2]);
        return Color.argb(Color.alpha(color), Color.red(newColor), Color.green(newColor), Color.blue(newColor));
    }

    public static int makeColorOSLight(int color) {
        double[] lab = new double[3];
        ColorUtils.colorToLAB(color, lab);
        double newL = 100.0d - lab[0];
        if (newL <= lab[0]) {
            return color;
        }
        lab[0] = newL;
        int newColor = ColorUtils.LABToColor(lab[0], lab[1], lab[2]);
        return Color.argb(Color.alpha(color), Color.red(newColor), Color.green(newColor), Color.blue(newColor));
    }

    public static int makeColorOSWeChatLight(int color) {
        double newL;
        double[] lab = new double[3];
        ColorUtils.colorToLAB(color, lab);
        double l = lab[0];
        if (l < 10.0d) {
            newL = 80.0d;
        } else {
            newL = Math.min(100.0d, 110.0d - l);
        }
        if (newL <= l) {
            return color;
        }
        lab[0] = newL;
        int newColor = ColorUtils.LABToColor(lab[0], lab[1], lab[2]);
        return Color.argb(Color.alpha(color), Color.red(newColor), Color.green(newColor), Color.blue(newColor));
    }

    private static boolean isFaceBook(Application mApplication2) {
        return (mApplication2 == null || mApplication2.getApplicationContext() == null || !"com.facebook.katana".equals(mApplication2.getApplicationContext().getPackageName())) ? false : true;
    }

    private static boolean isFaceBookMessenger(Application mApplication2) {
        return (mApplication2 == null || mApplication2.getApplicationContext() == null || !"com.facebook.orca".equals(mApplication2.getApplicationContext().getPackageName())) ? false : true;
    }

    private static boolean isWeChat(Application mApplication2) {
        return (mApplication2 == null || mApplication2.getApplicationContext() == null || !"com.tencent.mm".equals(mApplication2.getApplicationContext().getPackageName())) ? false : true;
    }

    private static boolean isWhatsapp(Application mApplication2) {
        return (mApplication2 == null || mApplication2.getApplicationContext() == null || !"com.whatsapp".equals(mApplication2.getApplicationContext().getPackageName())) ? false : true;
    }

    private static boolean isTaoBao(Application mApplication2) {
        return (mApplication2 == null || mApplication2.getApplicationContext() == null || !"com.taobao.taobao".equals(mApplication2.getApplicationContext().getPackageName())) ? false : true;
    }

    private static boolean useDarkenStyle(Application mApplication2) {
        return (mApplication2 == null || mApplication2.getApplicationContext() == null || !DARKEN_PACKAGE.contains(mApplication2.getApplicationContext().getPackageName())) ? false : true;
    }

    private boolean isWeChatIcon(Bitmap bitmap, RectF rectF) {
        if (!isWeChat(this.mApplication) || isViewSrc(bitmap) || isAssetSource(bitmap) || rectF.width() != rectF.height()) {
            return false;
        }
        float density = getDpDensity(this.mApplication);
        float f = mDpDensity;
        if (f >= 3.5f && f <= 4.0f) {
            density = 4.0f;
        }
        float size = rectF.width();
        if (size >= 13.0f * density && size <= 14.0f * density) {
            return true;
        }
        if (size >= 10.0f * density && size <= 11.0f * density) {
            return true;
        }
        if (size >= 46.0f * density && size <= 47.0f * density) {
            return true;
        }
        if (size >= 43.0f * density && size <= 44.0f * density) {
            return true;
        }
        if (size < 35.0f * density || size > 36.0f * density) {
            return false;
        }
        return true;
    }
}
