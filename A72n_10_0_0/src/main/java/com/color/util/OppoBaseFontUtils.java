package com.color.util;

import android.graphics.ColorTypefaceInjector;
import android.graphics.Typeface;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OppoBaseFontUtils {
    public static String DATA_FONT_DIRECTORY = (sIsROM6d0FlipFont ? DATA_FONT_DIRECTORY_6D0 : DATA_FONT_DIRECTORY_5D0);
    protected static final String DATA_FONT_DIRECTORY_5D0 = "/data/system/font/";
    public static final String DATA_FONT_DIRECTORY_6D0 = "/data/format_unclear/font/";
    protected static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    protected static final List<String> DEFAULT_COLOR_FONT_SYSTEM_LINKS = new ArrayList(Arrays.asList("/system/fonts/ColorFont-Thin.ttf", "/system/fonts/ColorFont-ThinItalic.ttf", "/system/fonts/ColorFont-Light.ttf", "/system/fonts/ColorFont-LightItalic.ttf", "/system/fonts/ColorFont-Regular.ttf", "/system/fonts/ColorFont-Italic.ttf", "/system/fonts/ColorFont-Medium.ttf", "/system/fonts/ColorFont-MediumItalic.ttf", "/system/fonts/ColorFont-Black.ttf", "/system/fonts/ColorFont-BlackItalic.ttf", "/system/fonts/ColorFont-Bold.ttf", "/system/fonts/ColorFont-BoldItalic.ttf", "/system/fonts/MyanmarZg_exp.ttf"));
    public static final String DEFAULT_FONT_CONFIG_FILE = "/system/etc/fonts.xml";
    protected static final String FLIPED_COLOROS_FONT_NAME = (DATA_FONT_DIRECTORY + "ColorOS-Regular.ttf");
    public static boolean FLIP_APP_ALL_FONTS = false;
    protected static final int FLIP_FONT_FLAG_MAX = 10001;
    public static final List<String> FLITER_CTS_APP_PKG_LIST = new ArrayList(Arrays.asList("android.theme.app", "android.graphics.cts", "android.widget.cts", "android.uirendering.cts", "android.text.cts"));
    public static final List<String> FLITER_NOT_REPLACEFONT_APP_PKG_LIST = new ArrayList(Arrays.asList("com.eterno"));
    protected static final FontLinkInfo[] FONTINFOARRAY_ROM6D0 = {new FontLinkInfo(DATA_FONT_DIRECTORY + "ColorOSUI-Thin.ttf", "/system/fonts/Roboto-Thin.ttf"), new FontLinkInfo(DATA_FONT_DIRECTORY + "ColorOSUI-ThinItalic.ttf", "/system/fonts/Roboto-ThinItalic.ttf"), new FontLinkInfo(DATA_FONT_DIRECTORY + "ColorOSUI-Light.ttf", "/system/fonts/Roboto-Light.ttf"), new FontLinkInfo(DATA_FONT_DIRECTORY + "ColorOSUI-LightItalic.ttf", "/system/fonts/Roboto-LightItalic.ttf"), new FontLinkInfo(DATA_FONT_DIRECTORY + "ColorOSUI-Regular.ttf", "/system/fonts/Roboto-Regular.ttf"), new FontLinkInfo(DATA_FONT_DIRECTORY + "ColorOSUI-Italic.ttf", "/system/fonts/Roboto-Italic.ttf"), new FontLinkInfo(DATA_FONT_DIRECTORY + "ColorOSUI-Medium.ttf", "/system/fonts/Roboto-Medium.ttf"), new FontLinkInfo(DATA_FONT_DIRECTORY + "ColorOSUI-MediumItalic.ttf", "/system/fonts/Roboto-MediumItalic.ttf"), new FontLinkInfo(DATA_FONT_DIRECTORY + "ColorOSUI-Black.ttf", "/system/fonts/Roboto-Black.ttf"), new FontLinkInfo(DATA_FONT_DIRECTORY + "ColorOSUI-BlackItalic.ttf", "/system/fonts/Roboto-BlackItalic.ttf"), new FontLinkInfo(DATA_FONT_DIRECTORY + "ColorOSUI-Bold.ttf", "/system/fonts/Roboto-Bold.ttf"), new FontLinkInfo(DATA_FONT_DIRECTORY + "ColorOSUI-BoldItalic.ttf", "/system/fonts/Roboto-BoldItalic.ttf")};
    protected static final int INVALID_FLIP_FONT = 0;
    protected static final int LINK_TARGET_FLIPFONT = 2;
    protected static final int LINK_TARGET_SYSTEM = 1;
    public static final String MEDIUM_PATH = "/system/fonts/ColorOSUI-Medium.ttf";
    public static final String MEDIUM_PATH2 = "/system/fonts/NotoSansSC-Medium.otf";
    public static final String SECOND_FONT_CONFIG_FILE = ColorTypefaceInjector.SECOND_FONT_CONFIG_FILE;
    public static final String[] SUPPORT_FLIP_FONT_FAMILIES = {"sans-serif-thin", "sans-serif-light", "sans-serif-medium", "sans-serif-black", "sans-serif-condensed", "sans-serif", "monospace"};
    public static final List<String> SUPPORT_FLIP_FONT_FAMILIES_LIST = new ArrayList(Arrays.asList(SUPPORT_FLIP_FONT_FAMILIES));
    protected static final List<String> SUPPORT_MEDIUM_FONT_LANGUAGE_LIST = new ArrayList(Arrays.asList("en", "zh", "ja", "ko", "fr", "it", "de", "sv", "nl", "es", "ru", "kk"));
    protected static final String SYSTEM_FONT_DIRECTORY = "/system/fonts/";
    protected static final String TAG = "OppoFontUtils";
    public static final String XTHIN_PATH = "/system/fonts/ColorOSUI-XThin.ttf";
    public static boolean isFlipFontUsed = false;
    protected static Typeface[] sCurrentTypefaces = null;
    protected static List<Typeface> sCurrentTypefacesArray = null;
    public static int sFlipFont;
    protected static List<FontLinkInfo> sFontLinkInfos = new ArrayList(Arrays.asList(FONTINFOARRAY_ROM6D0));
    public static boolean sIsCheckCTS = false;
    public static boolean sIsExp;
    public static final boolean sIsROM6d0FlipFont = ColorTypefaceInjector.sIsFBESupport;
    protected static boolean sNeedReplaceAllTypefaceApp = false;
    public static boolean sReplaceFont = true;
    public static int sUserId;

    /* access modifiers changed from: protected */
    public static class FontLinkInfo {
        String mDataFontName;
        String mSystemFontName;

        FontLinkInfo(String dataFontName, String robotoFontName) {
            this.mDataFontName = dataFontName;
            this.mSystemFontName = robotoFontName;
        }
    }

    public static void deleteFontLink(String pkgName) {
    }

    public static void createFontLink(String pkgName) {
    }

    protected static void logd(String content) {
        if (DEBUG) {
            Log.d(TAG, content);
        }
    }

    protected static void loge(String content, Throwable e) {
        if (DEBUG) {
            Log.e(TAG, content + ":" + e.getMessage(), e);
        }
    }
}
