package com.color.util;

public class ColorChangeTextUtil {
    public static final int G1 = 1;
    public static final int G2 = 2;
    public static final int G3 = 3;
    public static final int G4 = 4;
    public static final int G5 = 5;
    public static final int GN = 6;
    private static final float H1 = 0.9f;
    private static final float H2 = 1.0f;
    private static final float H3 = 1.1f;
    private static final float H4 = 1.25f;
    private static final float H5 = 1.45f;
    private static final float H6 = 1.65f;
    public static final float[] SCALE_LEVEL = {H1, 1.0f, H3, H4, H5, 1.65f};
    private static final String TAG = "ColorChangeTextUtil";

    public static float getSuitableFontSize(float textSize, float scale, int level) {
        if (level < 2) {
            return textSize;
        }
        float[] fArr = SCALE_LEVEL;
        if (level > fArr.length) {
            level = fArr.length;
        }
        float textSizeNoScale = textSize / scale;
        if (level != 2) {
            if (level != 3) {
                float[] fArr2 = SCALE_LEVEL;
                if (scale > fArr2[level - 1]) {
                    return fArr2[level - 1] * textSizeNoScale;
                }
                return textSizeNoScale * scale;
            } else if (scale < H3) {
                return 1.0f * textSizeNoScale;
            } else {
                if (scale < H5) {
                    return H3 * textSizeNoScale;
                }
                return H4 * textSizeNoScale;
            }
        } else if (scale < H3) {
            return 1.0f * textSizeNoScale;
        } else {
            return H3 * textSizeNoScale;
        }
    }

    private static float getSuitableFontScale(float scale, int level) {
        if (level < 2) {
            return scale;
        }
        float[] fArr = SCALE_LEVEL;
        if (level > fArr.length) {
            level = fArr.length;
        }
        if (level != 2) {
            if (level != 3) {
                float[] fArr2 = SCALE_LEVEL;
                if (scale > fArr2[level - 1]) {
                    return fArr2[level - 1];
                }
                return scale;
            } else if (scale < H3) {
                return 1.0f;
            } else {
                if (scale < H5) {
                    return H3;
                }
                return H4;
            }
        } else if (scale < H3) {
            return 1.0f;
        } else {
            return H3;
        }
    }
}
