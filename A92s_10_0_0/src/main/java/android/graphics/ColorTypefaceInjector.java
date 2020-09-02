package android.graphics;

import android.content.ContentResolver;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.util.Map;

public final class ColorTypefaceInjector {
    public static Typeface COLOROSUI_MEDIUM = null;
    public static final String DEFAULT_FONT_CONFIG_FILE = "/system/etc/fonts.xml";
    public static final String FBE_FONT_CONFIG_FILE = "/system/etc/fonts_base.xml";
    public static final String LMT_FONT_CONFIG_FILE = "/system/etc/fonts_original.xml";
    public static final String SECOND_FONT_CONFIG_FILE;
    public static final boolean sIsFBESupport;

    static {
        boolean equals = ContentResolver.SCHEME_FILE.equals(SystemProperties.get("ro.crypto.type", ""));
        String str = FBE_FONT_CONFIG_FILE;
        sIsFBESupport = equals || new File(str).exists();
        if (!sIsFBESupport) {
            str = LMT_FONT_CONFIG_FILE;
        }
        SECOND_FONT_CONFIG_FILE = str;
    }

    public static boolean isSystemTypeface(Typeface typeface) {
        if (typeface == null) {
            typeface = Typeface.DEFAULT;
        }
        if (typeface == null || Typeface.sSystemFontMap == null || !Typeface.sSystemFontMap.containsValue(typeface)) {
            return false;
        }
        return true;
    }

    public static boolean isSystemTypeface(String fontFamily) {
        if (TextUtils.isEmpty(fontFamily) || Typeface.sSystemFontMap == null || !Typeface.sSystemFontMap.containsKey(fontFamily)) {
            return false;
        }
        return true;
    }

    public static Typeface[] getSystemDefaultTypefaces() {
        return Typeface.sDefaults;
    }

    public static void dumpSysTypeface() {
        Map<String, Typeface> sSystemFontMap = Typeface.sSystemFontMap;
        if (sSystemFontMap != null) {
            for (Map.Entry<String, Typeface> entry : sSystemFontMap.entrySet()) {
                Log.d("OppoFontUtilsCTI", "System typeface  family = " + entry.getKey() + " : " + entry.getValue());
            }
        }
    }
}
