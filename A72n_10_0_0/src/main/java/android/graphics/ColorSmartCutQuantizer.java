package android.graphics;

import java.util.HashMap;

public final class ColorSmartCutQuantizer {
    private static final String LOG_TAG = "ColorSmartCutQuantizer";
    private static final boolean LOG_TIMINGS = false;
    private static final int QUANTIZE_WORD_MASK = 255;
    private static final int QUANTIZE_WORD_WIDTH = 8;
    private HashMap<Integer, Integer> mColorMap = new HashMap<>();
    int mDominantColor = 0;
    private final float[] mTempHsl = new float[3];

    public ColorSmartCutQuantizer(int[] pixels) {
        int maxCnt = 0;
        int maxColor = 0;
        for (int i = 0; i < pixels.length; i += 3) {
            int quantizedColor = quantizeFromRgb888(pixels[i]);
            pixels[i] = quantizedColor;
            if (this.mColorMap.containsKey(Integer.valueOf(pixels[i]))) {
                int temp = this.mColorMap.get(Integer.valueOf(pixels[i])).intValue();
                this.mColorMap.put(Integer.valueOf(pixels[i]), Integer.valueOf(temp + 1));
                if (temp + 1 > maxCnt) {
                    maxCnt = temp + 1;
                    maxColor = quantizedColor;
                }
            } else {
                this.mColorMap.put(Integer.valueOf(pixels[i]), 1);
            }
        }
        this.mDominantColor = maxColor;
    }

    public HashMap<Integer, Integer> getQuantizedColors() {
        return this.mColorMap;
    }

    public int getDominantColor() {
        return this.mDominantColor;
    }

    private static int quantizeFromRgb888(int color) {
        int r = modifyWordWidth(Color.red(color), 8, 8);
        int g = modifyWordWidth(Color.green(color), 8, 8);
        return (r << 16) | (g << 8) | modifyWordWidth(Color.blue(color), 8, 8);
    }

    static int approximateToRgb888(int r, int g, int b) {
        return Color.rgb(modifyWordWidth(r, 8, 8), modifyWordWidth(g, 8, 8), modifyWordWidth(b, 8, 8));
    }

    private static int approximateToRgb888(int color) {
        return approximateToRgb888(quantizedRed(color), quantizedGreen(color), quantizedBlue(color));
    }

    static int quantizedRed(int color) {
        return (color >> 16) & 255;
    }

    static int quantizedGreen(int color) {
        return (color >> 8) & 255;
    }

    static int quantizedBlue(int color) {
        return color & 255;
    }

    private static int modifyWordWidth(int value, int currentWidth, int targetWidth) {
        int newValue;
        if (targetWidth > currentWidth) {
            newValue = value << (targetWidth - currentWidth);
        } else {
            newValue = value >> (currentWidth - targetWidth);
        }
        return newValue & ((1 << targetWidth) - 1);
    }
}
