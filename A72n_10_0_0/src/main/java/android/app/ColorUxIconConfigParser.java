package android.app;

import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.PathParser;
import com.color.util.ColorRoundRectUtil;
import java.util.ArrayList;
import oppo.content.res.OppoExtraConfiguration;

public class ColorUxIconConfigParser {
    public static void parseConfig(ColorIconConfig config, OppoExtraConfiguration extraConfiguration, Resources res, ArrayList<Integer> commonStyleConfigArray, ArrayList<Integer> specialStyleConfigArray, String[] commonStylePathArray, String[] specialStlytPathArray) {
        Path path;
        int themeCustomPos;
        boolean isForeign;
        Resources resources = res;
        ArrayList<Integer> arrayList = commonStyleConfigArray;
        if (extraConfiguration != null) {
            Long uxIconConfig = Long.valueOf(extraConfiguration.mUxIconConfig);
            if (uxIconConfig.longValue() == -1) {
                config.setEmpty(true);
                return;
            }
            boolean isForeign2 = (uxIconConfig.intValue() & 15) == 1;
            config.setForeign(isForeign2);
            Long uxIconConfig2 = Long.valueOf(uxIconConfig.longValue() >> 4);
            int theme = uxIconConfig2.intValue() & 15;
            config.setTheme(theme);
            Long valueOf = Long.valueOf(uxIconConfig2.longValue() >> 4);
            Long uxIconConfig3 = valueOf;
            config.setArtPlusOn((valueOf.intValue() & 15) == 1);
            int defaultIconSizeDp = getDpFromIconConfigPx(resources, resources.getDimensionPixelSize(201655817));
            config.setIconSize(defaultIconSizeDp);
            config.setForegroundSize(defaultIconSizeDp);
            int themeCustomPos2 = commonStyleConfigArray.size() - 1;
            Path path2 = null;
            int i = 0;
            while (i < commonStyleConfigArray.size()) {
                if (arrayList.get(i).intValue() == theme && i == themeCustomPos2) {
                    Long uxIconConfig4 = Long.valueOf(uxIconConfig3.longValue() >> 4);
                    int iconShape = uxIconConfig4.intValue() & 15;
                    config.setIconShape(iconShape);
                    Long uxIconConfig5 = Long.valueOf(uxIconConfig4.longValue() >> 4);
                    config.setIconSize(uxIconConfig5.intValue() & 65535);
                    Long valueOf2 = Long.valueOf(uxIconConfig5.longValue() >> 16);
                    uxIconConfig3 = valueOf2;
                    config.setForegroundSize(valueOf2.intValue() & 65535);
                    if (iconShape == 0) {
                        Long uxIconConfig6 = Long.valueOf(uxIconConfig3.longValue() >> 16);
                        int iconRadiusPx = uxIconConfig6.intValue() & 4095;
                        if (iconRadiusPx > 75) {
                            iconRadiusPx = getPxFromIconConfigDp(resources, iconRadiusPx);
                        }
                        if (iconRadiusPx == 75) {
                            path2 = PathParser.createPathFromPathData(resources.getString(201590235));
                            isForeign = isForeign2;
                            themeCustomPos = themeCustomPos2;
                            uxIconConfig3 = uxIconConfig6;
                        } else {
                            isForeign = isForeign2;
                            themeCustomPos = themeCustomPos2;
                            path2 = ColorRoundRectUtil.getInstance().getPath(new Rect(0, 0, 150, 150), (float) iconRadiusPx);
                            uxIconConfig3 = uxIconConfig6;
                        }
                    } else if (iconShape == 1) {
                        path2 = PathParser.createPathFromPathData(resources.getString(201590232));
                        isForeign = isForeign2;
                        themeCustomPos = themeCustomPos2;
                    } else if (iconShape == 2) {
                        path2 = PathParser.createPathFromPathData(resources.getString(201590233));
                        isForeign = isForeign2;
                        themeCustomPos = themeCustomPos2;
                    } else if (iconShape != 3) {
                        isForeign = isForeign2;
                        themeCustomPos = themeCustomPos2;
                    } else {
                        path2 = PathParser.createPathFromPathData(resources.getString(201590234));
                        isForeign = isForeign2;
                        themeCustomPos = themeCustomPos2;
                    }
                } else {
                    isForeign = isForeign2;
                    themeCustomPos = themeCustomPos2;
                    if (arrayList.get(i).intValue() == theme && i < commonStylePathArray.length) {
                        if (i == 1) {
                            Long valueOf3 = Long.valueOf(uxIconConfig3.longValue() >> 24);
                            uxIconConfig3 = valueOf3;
                            int foregroundSize = valueOf3.intValue() & 65535;
                            int defaultMaterialIconSizeDp = getDpFromIconConfigPx(resources, resources.getDimensionPixelSize(201655819));
                            if (foregroundSize > defaultMaterialIconSizeDp) {
                                foregroundSize = defaultMaterialIconSizeDp;
                            }
                            config.setForegroundSize(foregroundSize);
                            path2 = ColorRoundRectUtil.getInstance().getPath(new Rect(0, 0, 150, 150), 8.0f);
                        } else {
                            path2 = PathParser.createPathFromPathData(commonStylePathArray[i]);
                        }
                    }
                }
                i++;
                resources = res;
                arrayList = commonStyleConfigArray;
                isForeign2 = isForeign;
                themeCustomPos2 = themeCustomPos;
            }
            if (i == commonStyleConfigArray.size()) {
                for (int j = 0; j < specialStyleConfigArray.size(); j++) {
                    if (specialStyleConfigArray.get(j).intValue() == theme && j < specialStlytPathArray.length) {
                        path2 = PathParser.createPathFromPathData(specialStlytPathArray[j]);
                    }
                }
                path = path2;
            } else {
                path = path2;
            }
            config.setShapePath(path);
            config.setEmpty(false);
            config.setNeedUpdate(false);
        }
    }

    public static int getDpFromIconConfigPx(Resources resources, int px) {
        return float2int(((((float) px) * 1.0f) / resources.getDisplayMetrics().density) * 100.0f);
    }

    public static int getPxFromIconConfigDp(Resources resources, int dp) {
        return float2int(resources.getDisplayMetrics().density * ((((float) dp) * 1.0f) / 100.0f));
    }

    public static long getDefaultIconConfig(boolean isForeign, Resources resources) {
        long uxIconConfig = 0 | ((long) (getDpFromIconConfigPx(resources, resources.getDimensionPixelSize(201655818)) & 65535));
        int defaultIconSizeDp = getDpFromIconConfigPx(resources, resources.getDimensionPixelSize(201655817));
        return (((((((((((uxIconConfig << 16) | ((long) (defaultIconSizeDp & 65535))) << 16) | ((long) (65535 & defaultIconSizeDp))) << 4) | 0) << 4) | 0) << 4) | ((long) 2)) << 4) | (isForeign ? 1 : 0);
    }

    private static int float2int(float f) {
        int i = (int) f;
        if (((double) Math.abs(f - ((float) i))) > 0.5d) {
            return i + 1;
        }
        return i;
    }
}
