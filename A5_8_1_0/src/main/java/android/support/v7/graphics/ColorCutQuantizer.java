package android.support.v7.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.graphics.Palette.Swatch;
import android.util.SparseIntArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

final class ColorCutQuantizer {
    private static final float BLACK_MAX_LIGHTNESS = 0.05f;
    private static final int COMPONENT_BLUE = -1;
    private static final int COMPONENT_GREEN = -2;
    private static final int COMPONENT_RED = -3;
    private static final String LOG_TAG = ColorCutQuantizer.class.getSimpleName();
    private static final Comparator<Vbox> VBOX_COMPARATOR_VOLUME = new Comparator<Vbox>() {
        public int compare(Vbox lhs, Vbox rhs) {
            return rhs.getVolume() - lhs.getVolume();
        }
    };
    private static final float WHITE_MIN_LIGHTNESS = 0.95f;
    private final SparseIntArray mColorPopulations;
    private final int[] mColors;
    private final List<Swatch> mQuantizedColors;
    private final float[] mTempHsl = new float[3];

    private class Vbox {
        private int mLowerIndex;
        private int mMaxBlue;
        private int mMaxGreen;
        private int mMaxRed;
        private int mMinBlue;
        private int mMinGreen;
        private int mMinRed;
        private int mUpperIndex;

        Vbox(int lowerIndex, int upperIndex) {
            this.mLowerIndex = lowerIndex;
            this.mUpperIndex = upperIndex;
            fitBox();
        }

        int getVolume() {
            return (((this.mMaxRed - this.mMinRed) + 1) * ((this.mMaxGreen - this.mMinGreen) + 1)) * ((this.mMaxBlue - this.mMinBlue) + 1);
        }

        boolean canSplit() {
            return getColorCount() > 1;
        }

        int getColorCount() {
            return (this.mUpperIndex - this.mLowerIndex) + 1;
        }

        void fitBox() {
            this.mMinBlue = MotionEventCompat.ACTION_MASK;
            this.mMinGreen = MotionEventCompat.ACTION_MASK;
            this.mMinRed = MotionEventCompat.ACTION_MASK;
            this.mMaxBlue = 0;
            this.mMaxGreen = 0;
            this.mMaxRed = 0;
            for (int i = this.mLowerIndex; i <= this.mUpperIndex; i++) {
                int color = ColorCutQuantizer.this.mColors[i];
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                if (r > this.mMaxRed) {
                    this.mMaxRed = r;
                }
                if (r < this.mMinRed) {
                    this.mMinRed = r;
                }
                if (g > this.mMaxGreen) {
                    this.mMaxGreen = g;
                }
                if (g < this.mMinGreen) {
                    this.mMinGreen = g;
                }
                if (b > this.mMaxBlue) {
                    this.mMaxBlue = b;
                }
                if (b < this.mMinBlue) {
                    this.mMinBlue = b;
                }
            }
        }

        Vbox splitBox() {
            if (canSplit()) {
                int splitPoint = findSplitPoint();
                Vbox newBox = new Vbox(splitPoint + 1, this.mUpperIndex);
                this.mUpperIndex = splitPoint;
                fitBox();
                return newBox;
            }
            throw new IllegalStateException("Can not split a box with only 1 color");
        }

        int getLongestColorDimension() {
            int redLength = this.mMaxRed - this.mMinRed;
            int greenLength = this.mMaxGreen - this.mMinGreen;
            int blueLength = this.mMaxBlue - this.mMinBlue;
            if (redLength >= greenLength && redLength >= blueLength) {
                return ColorCutQuantizer.COMPONENT_RED;
            }
            if (greenLength < redLength || greenLength < blueLength) {
                return -1;
            }
            return -2;
        }

        int findSplitPoint() {
            int longestDimension = getLongestColorDimension();
            ColorCutQuantizer.this.modifySignificantOctet(longestDimension, this.mLowerIndex, this.mUpperIndex);
            Arrays.sort(ColorCutQuantizer.this.mColors, this.mLowerIndex, this.mUpperIndex + 1);
            ColorCutQuantizer.this.modifySignificantOctet(longestDimension, this.mLowerIndex, this.mUpperIndex);
            int dimensionMidPoint = midPoint(longestDimension);
            for (int i = this.mLowerIndex; i <= this.mUpperIndex; i++) {
                int color = ColorCutQuantizer.this.mColors[i];
                switch (longestDimension) {
                    case ColorCutQuantizer.COMPONENT_RED /*-3*/:
                        if (Color.red(color) < dimensionMidPoint) {
                            break;
                        }
                        return i;
                    case -2:
                        if (Color.green(color) < dimensionMidPoint) {
                            break;
                        }
                        return i;
                    case -1:
                        if (Color.blue(color) <= dimensionMidPoint) {
                            break;
                        }
                        return i;
                    default:
                        break;
                }
            }
            return this.mLowerIndex;
        }

        Swatch getAverageColor() {
            int redSum = 0;
            int greenSum = 0;
            int blueSum = 0;
            int totalPopulation = 0;
            for (int i = this.mLowerIndex; i <= this.mUpperIndex; i++) {
                int color = ColorCutQuantizer.this.mColors[i];
                int colorPopulation = ColorCutQuantizer.this.mColorPopulations.get(color);
                totalPopulation += colorPopulation;
                redSum += Color.red(color) * colorPopulation;
                greenSum += Color.green(color) * colorPopulation;
                blueSum += Color.blue(color) * colorPopulation;
            }
            return new Swatch(Math.round(((float) redSum) / ((float) totalPopulation)), Math.round(((float) greenSum) / ((float) totalPopulation)), Math.round(((float) blueSum) / ((float) totalPopulation)), totalPopulation);
        }

        int midPoint(int dimension) {
            switch (dimension) {
                case -2:
                    return (this.mMinGreen + this.mMaxGreen) / 2;
                case -1:
                    return (this.mMinBlue + this.mMaxBlue) / 2;
                default:
                    return (this.mMinRed + this.mMaxRed) / 2;
            }
        }
    }

    static ColorCutQuantizer fromBitmap(Bitmap bitmap, int maxColors) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[(width * height)];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        return new ColorCutQuantizer(new ColorHistogram(pixels), maxColors);
    }

    private ColorCutQuantizer(ColorHistogram colorHistogram, int maxColors) {
        int color;
        int i = 0;
        int rawColorCount = colorHistogram.getNumberOfColors();
        int[] rawColors = colorHistogram.getColors();
        int[] rawColorCounts = colorHistogram.getColorCounts();
        this.mColorPopulations = new SparseIntArray(rawColorCount);
        for (int i2 = 0; i2 < rawColors.length; i2++) {
            this.mColorPopulations.append(rawColors[i2], rawColorCounts[i2]);
        }
        this.mColors = new int[rawColorCount];
        int length = rawColors.length;
        int i3 = 0;
        int validColorCount = 0;
        while (i3 < length) {
            int validColorCount2;
            color = rawColors[i3];
            if (shouldIgnoreColor(color)) {
                validColorCount2 = validColorCount;
            } else {
                validColorCount2 = validColorCount + 1;
                this.mColors[validColorCount] = color;
            }
            i3++;
            validColorCount = validColorCount2;
        }
        if (validColorCount <= maxColors) {
            this.mQuantizedColors = new ArrayList();
            int[] iArr = this.mColors;
            length = iArr.length;
            while (i < length) {
                color = iArr[i];
                this.mQuantizedColors.add(new Swatch(color, this.mColorPopulations.get(color)));
                i++;
            }
            return;
        }
        this.mQuantizedColors = quantizePixels(validColorCount - 1, maxColors);
    }

    List<Swatch> getQuantizedColors() {
        return this.mQuantizedColors;
    }

    private List<Swatch> quantizePixels(int maxColorIndex, int maxColors) {
        PriorityQueue<Vbox> pq = new PriorityQueue(maxColors, VBOX_COMPARATOR_VOLUME);
        pq.offer(new Vbox(0, maxColorIndex));
        splitBoxes(pq, maxColors);
        return generateAverageColors(pq);
    }

    private void splitBoxes(PriorityQueue<Vbox> queue, int maxSize) {
        while (queue.size() < maxSize) {
            Vbox vbox = (Vbox) queue.poll();
            if (vbox != null && vbox.canSplit()) {
                queue.offer(vbox.splitBox());
                queue.offer(vbox);
            } else {
                return;
            }
        }
    }

    private List<Swatch> generateAverageColors(Collection<Vbox> vboxes) {
        ArrayList<Swatch> colors = new ArrayList(vboxes.size());
        for (Vbox vbox : vboxes) {
            Swatch color = vbox.getAverageColor();
            if (!shouldIgnoreColor(color)) {
                colors.add(color);
            }
        }
        return colors;
    }

    private void modifySignificantOctet(int dimension, int lowerIndex, int upperIndex) {
        int i;
        int color;
        switch (dimension) {
            case -2:
                for (i = lowerIndex; i <= upperIndex; i++) {
                    color = this.mColors[i];
                    this.mColors[i] = Color.rgb((color >> 8) & MotionEventCompat.ACTION_MASK, (color >> 16) & MotionEventCompat.ACTION_MASK, color & MotionEventCompat.ACTION_MASK);
                }
                return;
            case -1:
                for (i = lowerIndex; i <= upperIndex; i++) {
                    color = this.mColors[i];
                    this.mColors[i] = Color.rgb(color & MotionEventCompat.ACTION_MASK, (color >> 8) & MotionEventCompat.ACTION_MASK, (color >> 16) & MotionEventCompat.ACTION_MASK);
                }
                return;
            default:
                return;
        }
    }

    private boolean shouldIgnoreColor(int color) {
        ColorUtils.RGBtoHSL(Color.red(color), Color.green(color), Color.blue(color), this.mTempHsl);
        return shouldIgnoreColor(this.mTempHsl);
    }

    private static boolean shouldIgnoreColor(Swatch color) {
        return shouldIgnoreColor(color.getHsl());
    }

    private static boolean shouldIgnoreColor(float[] hslColor) {
        return (isWhite(hslColor) || isBlack(hslColor)) ? true : isNearRedILine(hslColor);
    }

    private static boolean isBlack(float[] hslColor) {
        return hslColor[2] <= BLACK_MAX_LIGHTNESS;
    }

    private static boolean isWhite(float[] hslColor) {
        return hslColor[2] >= WHITE_MIN_LIGHTNESS;
    }

    private static boolean isNearRedILine(float[] hslColor) {
        return hslColor[0] >= 10.0f && hslColor[0] <= 37.0f && hslColor[1] <= 0.82f;
    }
}
