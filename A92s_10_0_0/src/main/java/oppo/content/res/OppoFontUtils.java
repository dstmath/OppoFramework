package oppo.content.res;

import android.graphics.ColorTypefaceInjector;
import android.graphics.Typeface;

public class OppoFontUtils {
    @Deprecated
    public static Typeface COLOROSUI_MEDIUM = null;
    public static final String DEFAULT_FONT_CONFIG_FILE = "/system/etc/fonts.xml";
    public static boolean FLIP_APP_ALL_FONTS = false;
    public static final String MEDIUM_PATH = "/system/fonts/ColorOSUI-Medium.ttf";
    public static final String MEDIUM_PATH2 = "/system/fonts/NotoSansSC-Medium.otf";
    public static final String SECOND_FONT_CONFIG_FILE = ColorTypefaceInjector.SECOND_FONT_CONFIG_FILE;
    public static final String XTHIN_PATH = "/system/fonts/ColorOSUI-XThin.ttf";
    public static boolean isFlipFontUsed = false;
    public static int sFlipFont;
    public static boolean sIsCheckCTS = false;
    public static final boolean sIsROM6d0FlipFont = ColorTypefaceInjector.sIsFBESupport;
}
