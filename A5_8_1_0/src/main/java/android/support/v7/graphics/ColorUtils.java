package android.support.v7.graphics;

import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;

final class ColorUtils {
    private static final int MIN_ALPHA_SEARCH_MAX_ITERATIONS = 10;
    private static final int MIN_ALPHA_SEARCH_PRECISION = 10;

    private ColorUtils() {
    }

    private static int compositeColors(int fg, int bg) {
        float alpha1 = ((float) Color.alpha(fg)) / 255.0f;
        float alpha2 = ((float) Color.alpha(bg)) / 255.0f;
        return Color.argb((int) ((alpha1 + alpha2) * (1.0f - alpha1)), (int) ((((float) Color.red(fg)) * alpha1) + ((((float) Color.red(bg)) * alpha2) * (1.0f - alpha1))), (int) ((((float) Color.green(fg)) * alpha1) + ((((float) Color.green(bg)) * alpha2) * (1.0f - alpha1))), (int) ((((float) Color.blue(fg)) * alpha1) + ((((float) Color.blue(bg)) * alpha2) * (1.0f - alpha1))));
    }

    private static double calculateLuminance(int color) {
        double red = ((double) Color.red(color)) / 255.0d;
        double green = ((double) Color.green(color)) / 255.0d;
        double blue = ((double) Color.blue(color)) / 255.0d;
        return ((0.2126d * (red < 0.03928d ? red / 12.92d : Math.pow((0.055d + red) / 1.055d, 2.4d))) + (0.7152d * (green < 0.03928d ? green / 12.92d : Math.pow((0.055d + green) / 1.055d, 2.4d)))) + (0.0722d * (blue < 0.03928d ? blue / 12.92d : Math.pow((0.055d + blue) / 1.055d, 2.4d)));
    }

    private static double calculateContrast(int foreground, int background) {
        if (Color.alpha(background) != MotionEventCompat.ACTION_MASK) {
            throw new IllegalArgumentException("background can not be translucent");
        }
        if (Color.alpha(foreground) < MotionEventCompat.ACTION_MASK) {
            foreground = compositeColors(foreground, background);
        }
        double luminance1 = calculateLuminance(foreground) + 0.05d;
        double luminance2 = calculateLuminance(background) + 0.05d;
        return Math.max(luminance1, luminance2) / Math.min(luminance1, luminance2);
    }

    private static int findMinimumAlpha(int foreground, int background, double minContrastRatio) {
        if (Color.alpha(background) != MotionEventCompat.ACTION_MASK) {
            throw new IllegalArgumentException("background can not be translucent");
        } else if (calculateContrast(modifyAlpha(foreground, MotionEventCompat.ACTION_MASK), background) < minContrastRatio) {
            return -1;
        } else {
            int minAlpha = 0;
            for (int numIterations = 0; numIterations <= 10 && MotionEventCompat.ACTION_MASK - minAlpha > 10; numIterations++) {
                int testAlpha = (minAlpha + MotionEventCompat.ACTION_MASK) / 2;
                if (calculateContrast(modifyAlpha(foreground, testAlpha), background) < minContrastRatio) {
                }
                minAlpha = testAlpha;
            }
            return MotionEventCompat.ACTION_MASK;
        }
    }

    static int getTextColorForBackground(int backgroundColor, float minContrastRatio) {
        int whiteMinAlpha = findMinimumAlpha(-1, backgroundColor, (double) minContrastRatio);
        if (whiteMinAlpha >= 0) {
            return modifyAlpha(-1, whiteMinAlpha);
        }
        int blackMinAlpha = findMinimumAlpha(ViewCompat.MEASURED_STATE_MASK, backgroundColor, (double) minContrastRatio);
        if (blackMinAlpha >= 0) {
            return modifyAlpha(ViewCompat.MEASURED_STATE_MASK, blackMinAlpha);
        }
        return -1;
    }

    static void RGBtoHSL(int r, int g, int b, float[] hsl) {
        float s;
        float h;
        float rf = ((float) r) / 255.0f;
        float gf = ((float) g) / 255.0f;
        float bf = ((float) b) / 255.0f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float deltaMaxMin = max - min;
        float l = (max + min) / 2.0f;
        if (max == min) {
            s = 0.0f;
            h = 0.0f;
        } else {
            if (max == rf) {
                h = ((gf - bf) / deltaMaxMin) % 6.0f;
            } else if (max == gf) {
                h = ((bf - rf) / deltaMaxMin) + 2.0f;
            } else {
                h = ((rf - gf) / deltaMaxMin) + 4.0f;
            }
            s = deltaMaxMin / (1.0f - Math.abs((2.0f * l) - 1.0f));
        }
        hsl[0] = (60.0f * h) % 360.0f;
        hsl[1] = s;
        hsl[2] = l;
    }

    static int HSLtoRGB(float[] hsl) {
        float h = hsl[0];
        float s = hsl[1];
        float l = hsl[2];
        float c = (1.0f - Math.abs((2.0f * l) - 1.0f)) * s;
        float m = l - (0.5f * c);
        float x = c * (1.0f - Math.abs(((h / 60.0f) % 2.0f) - 1.0f));
        int r = 0;
        int g = 0;
        int b = 0;
        switch (((int) h) / 60) {
            case 0:
                r = Math.round((c + m) * 255.0f);
                g = Math.round((x + m) * 255.0f);
                b = Math.round(255.0f * m);
                break;
            case 1:
                r = Math.round((x + m) * 255.0f);
                g = Math.round((c + m) * 255.0f);
                b = Math.round(255.0f * m);
                break;
            case 2:
                r = Math.round(255.0f * m);
                g = Math.round((c + m) * 255.0f);
                b = Math.round((x + m) * 255.0f);
                break;
            case 3:
                r = Math.round(255.0f * m);
                g = Math.round((x + m) * 255.0f);
                b = Math.round((c + m) * 255.0f);
                break;
            case 4:
                r = Math.round((x + m) * 255.0f);
                g = Math.round(255.0f * m);
                b = Math.round((c + m) * 255.0f);
                break;
            case 5:
            case 6:
                r = Math.round((c + m) * 255.0f);
                g = Math.round(255.0f * m);
                b = Math.round((x + m) * 255.0f);
                break;
        }
        return Color.rgb(Math.max(0, Math.min(MotionEventCompat.ACTION_MASK, r)), Math.max(0, Math.min(MotionEventCompat.ACTION_MASK, g)), Math.max(0, Math.min(MotionEventCompat.ACTION_MASK, b)));
    }

    static int modifyAlpha(int color, int alpha) {
        return (ViewCompat.MEASURED_SIZE_MASK & color) | (alpha << 24);
    }
}
