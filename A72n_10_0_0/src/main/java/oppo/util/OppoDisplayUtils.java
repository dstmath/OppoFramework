package oppo.util;

import com.android.internal.content.NativeLibraryHelper;

public class OppoDisplayUtils {
    private static final int[] DENSITIES = {480, 320, 1, 0};
    public static final int DENSITY_NONE = 1;

    public static int[] getBestDensityOrder(int density) {
        if (density <= 1) {
            return DENSITIES;
        }
        int i = 0;
        int k = -1;
        int j = 0;
        while (true) {
            int[] iArr = DENSITIES;
            if (j >= iArr.length) {
                break;
            } else if (density > iArr[j]) {
                i = 0 + 1;
                k = j;
                break;
            } else if (density == iArr[j]) {
                k = j;
                break;
            } else {
                j++;
            }
        }
        int[] iArr2 = DENSITIES;
        int[] array = new int[(iArr2.length + i)];
        if (k != 0) {
            if (k == 1 || k == 2) {
                int len = array.length;
                array[0] = density;
                array[len - 1] = 0;
                array[len - 2] = 1;
                if (i == 0) {
                    array[i + 1] = DENSITIES[i];
                } else {
                    int[] iArr3 = DENSITIES;
                    array[i] = iArr3[i];
                    array[i + 1] = iArr3[i - 1];
                }
            }
        } else if (i == 0) {
            return iArr2;
        } else {
            array[k] = density;
            while (i < array.length) {
                array[i] = DENSITIES[i - 1];
                i++;
            }
        }
        return array;
    }

    public static String getDensityName(int density) {
        if (density == 1) {
            return "nodpi";
        }
        if (density == 120) {
            return "ldpi";
        }
        if (density == 160) {
            return "mdpi";
        }
        if (density == 240) {
            return "hdpi";
        }
        if (density == 320) {
            return "xhdpi";
        }
        if (density == 480) {
            return "xxhdpi";
        }
        if (density != 640) {
            return "";
        }
        return "xxxhdpi";
    }

    public static String getDensitySuffix(int i) {
        String s = getDensityName(i);
        if (s.equals("")) {
            return s;
        }
        return NativeLibraryHelper.CLEAR_ABI_OVERRIDE + s;
    }

    public static String getDrawbleDensityFolder(int i) {
        return "res/" + getDrawbleDensityName(i);
    }

    public static String getDrawbleDensityName(int i) {
        return "drawable" + getDensitySuffix(i);
    }
}
