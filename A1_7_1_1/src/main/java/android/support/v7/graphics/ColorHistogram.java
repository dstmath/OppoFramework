package android.support.v7.graphics;

import java.util.Arrays;

final class ColorHistogram {
    private final int[] mColorCounts = new int[this.mNumberColors];
    private final int[] mColors = new int[this.mNumberColors];
    private final int mNumberColors;

    ColorHistogram(int[] pixels) {
        Arrays.sort(pixels);
        this.mNumberColors = countDistinctColors(pixels);
        countFrequencies(pixels);
    }

    int getNumberOfColors() {
        return this.mNumberColors;
    }

    int[] getColors() {
        return this.mColors;
    }

    int[] getColorCounts() {
        return this.mColorCounts;
    }

    private static int countDistinctColors(int[] pixels) {
        if (pixels.length < 2) {
            return pixels.length;
        }
        int colorCount = 1;
        int currentColor = pixels[0];
        for (int i = 1; i < pixels.length; i++) {
            if (pixels[i] != currentColor) {
                currentColor = pixels[i];
                colorCount++;
            }
        }
        return colorCount;
    }

    private void countFrequencies(int[] pixels) {
        if (pixels.length != 0) {
            int currentColorIndex = 0;
            int currentColor = pixels[0];
            this.mColors[0] = currentColor;
            this.mColorCounts[0] = 1;
            if (pixels.length != 1) {
                for (int i = 1; i < pixels.length; i++) {
                    if (pixels[i] == currentColor) {
                        int[] iArr = this.mColorCounts;
                        iArr[currentColorIndex] = iArr[currentColorIndex] + 1;
                    } else {
                        currentColor = pixels[i];
                        currentColorIndex++;
                        this.mColors[currentColorIndex] = currentColor;
                        this.mColorCounts[currentColorIndex] = 1;
                    }
                }
            }
        }
    }
}
