package android.app;

import android.os.Environment;
import android.os.SystemProperties;
import java.io.File;
import java.lang.reflect.Method;

public class ColorUxIconConstants {
    public static final boolean DEBUG_UX_ICON = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final boolean DEBUG_UX_ICON_TRACE = true;

    public static class IconTheme {
        public static final int ART_PLUS_BIT_LENGTH = 4;
        public static final String COLOROS_UXIOCN_META_DATA = "com.coloros.support_uxonline";
        public static final int FOREGROUND_SIZE_BIT_LENGTH = 16;
        public static final int FOREIGN_BIT_LENGTH = 4;
        public static final int ICON_RADIUS_BIT_LENGTH = 12;
        public static final int ICON_SHAPE_BIT_LENGTH = 4;
        public static final int ICON_SHAPE_CUSTOM_SQUARE = 0;
        public static final int ICON_SHAPE_DESIGNED_LEAF = 2;
        public static final int ICON_SHAPE_DESIGNED_OCTAGON = 1;
        public static final int ICON_SHAPE_DESIGNED_STICKER = 3;
        public static final int ICON_SIZE_BIT_LENGTH = 16;
        public static final int PREFIX_MATERIAL_POS = 2;
        public static final int PREFIX_PEBBLE_POS = 3;
        public static final int PREFIX_RECTANGLE_BG_POS = 1;
        public static final int PREFIX_RECTANGLE_FG_POS = 0;
        public static final int THEME_BIT_LENGTH = 4;
        public static final int THEME_CUSTOM = 3;
        public static final int THEME_MATERIAL = 1;
        public static final int THEME_MATERIAL_POS = 1;
        public static final int THEME_MATERIAL_RADIUS_PX = 8;
        public static final int THEME_PEBBLE = 4;
        public static final int THEME_PEBBLE_POS = 2;
        public static final int THEME_RECTANGLE = 2;
        public static final int THEME_RECTANGLE_POS = 0;
    }

    public static class SystemProperty {
        public static final String FEATURE_OPPO_VERSION_EXP = "oppo.version.exp";
        public static final String FEATURE_UX_ICON_DISABLE = "oppo.uxicons.disable.uxicons";
        public static final String FEATURE_UX_ICON_MATERIAL = "com.oppo.feature.uxicon.theme.material";
        public static final String FEATURE_UX_ICON_PEBBLE = "com.oppo.feature.uxicon.theme.pebble";
        public static final String KEY_THEME_FLAG = "persist.sys.themeflag";
        public static final String KEY_UX_ICON_CONFIG = "key_ux_icon_config";
        public static final String KEY_UX_ICON_THEME_FLAG = "persist.sys.themeflag.uxicon";
    }

    public static class IconLoader {
        public static final String BASE_PRODUCT_DEFAULT_THEME_FILE_PATH = (getOppoProductDirectory() + "/media/theme/default/");
        public static final String BASE_SYSTEM_DEFAULT_THEME_FILE_PATH = "/system/media/theme/default/";
        public static final String BASE_UX_ICONS_FILE_PATH = "/data/oppo/uxicons/";
        public static final String COM_ANDROID_CONTACTS = "com.android.contacts";
        public static final String COM_HEYTAP_MATKET = "com.heytap.market";
        public static final String DEFAULT_BACKGROUND_COLOR = "#FFFBFBFB";
        public static final String DIALER_PREFIX = "dialer_";
        public static final String FILE_SEPARATOR = "/";
        public static final int ICON_SIZE_THRESHOLD = 40;
        public static final float MATERIAL_FOREGROUND_SCALE = 1.25f;
        public static final String OPPO_PRODUCT_ROOT_PATH = "/oppo_product";
        public static final int PIXEL_ALPHA_THRESHOLD = 220;
        public static final int PIXEL_SAMPLE = 4;
        public static final int PIXEL_THRESHOLD = 6;
        public static final String PNG_REG = ".png";
        public static final int TRANSPARENT_ICON_FG_SIZE_DP = 34;

        static String getOppoProductDirectory() {
            try {
                Method method = Environment.class.getMethod("getOppoProductDirectory", new Class[0]);
                method.setAccessible(true);
                Object product = method.invoke(null, new Object[0]);
                if (product != null) {
                    return ((File) product).getAbsolutePath();
                }
                return OPPO_PRODUCT_ROOT_PATH;
            } catch (Exception e) {
                e.printStackTrace();
                return OPPO_PRODUCT_ROOT_PATH;
            }
        }
    }
}
