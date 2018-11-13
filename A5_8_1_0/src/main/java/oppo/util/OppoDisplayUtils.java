package oppo.util;

public class OppoDisplayUtils {
    private static final int[] DENSITIES = new int[]{640, 480, 320, 240, 160, 120, 1, 0};
    public static final int DENSITY_NONE = 1;

    public static int[] getBestDensityOrder(int density) {
        int i = 1;
        for (int i2 : DENSITIES) {
            if (density == i2) {
                i = 0;
            }
        }
        int[] array = new int[(DENSITIES.length + i)];
        array[0] = density;
        int m = 1;
        while (i < DENSITIES.length) {
            if (density != DENSITIES[i]) {
                int n = m + 1;
                array[m] = DENSITIES[i];
                m = n;
            }
            i++;
        }
        return array;
    }

    public static String getDensityName(int density) {
        switch (density) {
            case 1:
                return "nodpi";
            case 120:
                return "ldpi";
            case 160:
                return "mdpi";
            case 240:
                return "hdpi";
            case 320:
                return "xhdpi";
            case 480:
                return "xxhdpi";
            case 640:
                return "xxxhdpi";
            default:
                return "";
        }
    }

    public static String getDensitySuffix(int i) {
        String s = getDensityName(i);
        if (s.equals("")) {
            return s;
        }
        return "-" + s;
    }

    public static String getDrawbleDensityFolder(int i) {
        return "res/" + getDrawbleDensityName(i) + "/";
    }

    public static String getDrawbleDensityName(int i) {
        return "drawable" + getDensitySuffix(i);
    }
}
