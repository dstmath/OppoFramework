package com.color.util;

public class ColorChangeTextUtil {
    public static final int G1 = 1;
    public static final int G2 = 2;
    public static final int G3 = 3;
    public static final int G4 = 4;
    public static final int G5 = 5;
    private static final float[] SCALE_LEVEL = new float[]{0.9f, 1.0f, 1.15f, 1.35f, 1.6f};
    private static final String TAG = "ColorChangeTextUtil";

    public static float getSuitableFontSize(float textSize, float scale, int level) {
        if (level < 2) {
            return textSize;
        }
        if (level > SCALE_LEVEL.length) {
            level = SCALE_LEVEL.length;
        }
        float textSizeNoScale = textSize / scale;
        switch (level) {
            case 2:
                if (scale <= SCALE_LEVEL[1]) {
                    return textSizeNoScale;
                }
                return SCALE_LEVEL[level] * textSizeNoScale;
            case 3:
                if (scale <= SCALE_LEVEL[1]) {
                    return textSizeNoScale;
                }
                if (scale <= SCALE_LEVEL[level]) {
                    return SCALE_LEVEL[level - 1] * textSizeNoScale;
                }
                return SCALE_LEVEL[level] * textSizeNoScale;
            default:
                if (scale > SCALE_LEVEL[level - 1]) {
                    return SCALE_LEVEL[level - 1] * textSizeNoScale;
                }
                return textSizeNoScale * scale;
        }
    }

    public static float getSuitableFontSize(float textSize, float scale) {
        return textSize;
    }
}
