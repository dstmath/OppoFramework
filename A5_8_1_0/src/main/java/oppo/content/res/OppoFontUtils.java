package oppo.content.res;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.RemoteException;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import java.io.File;
import java.util.Random;

public class OppoFontUtils {
    private static final String Android_Burmese_FONT_LINK_PATH = "/system/fonts/MyanmarZg.ttf";
    private static final String Android_FONT_LINK_BOLD_PATH = "/system/fonts/Roboto-Bold.ttf";
    private static final String ColorOS_Burmese_FONT_LINK_REGULAR_NAME = "/data/system/font/MyanmarZg.ttf";
    private static final String ColorOS_FONT_DIRECTORY = "/data/system/font/";
    private static final String ColorOS_FONT_FAMILY_NAME = "coloros";
    private static final String ColorOS_FONT_LINK_BOLD_NAME = "/data/system/font/ColorOSUI-Bold.ttf";
    private static final String ColorOS_FONT_LINK_REGULAR_NAME = "/data/system/font/ColorOSUI-Regular.ttf";
    private static final String ColorOS_FONT_LINK_REGULAR_PATH = "/system/fonts/ColorOSUI-Regular.ttf";
    private static final String ColorOS_FONT_NAME = "ColorOS-Regular.ttf";
    private static final boolean DEBUG = false;
    private static Typeface DEFAULT_BOLD_ITALIC = Typeface.create((String) null, 3);
    private static Typeface DEFAULT_ITALIC = Typeface.create((String) null, 2);
    public static boolean FLIP_APP_ALL_FONTS = false;
    private static final String[] FliterCTSList = new String[]{"android.theme.app", "android.graphics.cts", "android.widget.cts", "android.uirendering.cts", "android.text.cts"};
    private static final int INVALID_FLIP_FONT = -1;
    private static final String TAG = "OppoFontUtils";
    public static boolean isFlipFontUsed = false;
    private static Typeface[] sCurrentTypefaces;
    private static int sFlipFont = -1;

    static {
        initFont();
    }

    public static void SetFlipFont(Configuration configuration) {
        if (sFlipFont == -1 || sFlipFont != configuration.mOppoExtraConfiguration.mFlipFont) {
            freeCaches();
            initFont();
            sFlipFont = configuration.mOppoExtraConfiguration.mFlipFont;
        }
    }

    public static Typeface flipTypeface(Typeface typeface) {
        Typeface tf = typeface;
        if (FLIP_APP_ALL_FONTS) {
            return sCurrentTypefaces[typeface == null ? 0 : typeface.getStyle()];
        } else if (typeface != null && !typeface.equals(Typeface.SANS_SERIF) && !typeface.isLikeDefault && !typeface.equals(Typeface.DEFAULT) && !typeface.equals(Typeface.DEFAULT_BOLD) && !typeface.equals(DEFAULT_ITALIC) && !typeface.equals(DEFAULT_BOLD_ITALIC)) {
            return tf;
        } else {
            return sCurrentTypefaces[typeface == null ? 0 : typeface.getStyle()];
        }
    }

    public static void createFontLink(String pkgName) {
        int i = 0;
        while (i < FliterCTSList.length) {
            if (pkgName != null && pkgName.equals(FliterCTSList[i])) {
                try {
                    if (new File(Android_Burmese_FONT_LINK_PATH).exists()) {
                        new File(ColorOS_Burmese_FONT_LINK_REGULAR_NAME).delete();
                        Os.symlink(Android_Burmese_FONT_LINK_PATH, ColorOS_Burmese_FONT_LINK_REGULAR_NAME);
                    }
                    Os.symlink(ColorOS_FONT_LINK_REGULAR_PATH, ColorOS_FONT_LINK_REGULAR_NAME);
                    Os.symlink(Android_FONT_LINK_BOLD_PATH, ColorOS_FONT_LINK_BOLD_NAME);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "SELinux policy update malformed: " + e.getMessage());
                } catch (ErrnoException e2) {
                    Log.d(TAG, "Could not update selinux policy: " + e2.getMessage());
                }
            }
            i++;
        }
    }

    public static void deleteFontLink(String pkgName) {
        for (Object equals : FliterCTSList) {
            if (pkgName != null) {
                if (pkgName.equals(equals)) {
                    if (new File(Android_Burmese_FONT_LINK_PATH).exists()) {
                        try {
                            new File(ColorOS_Burmese_FONT_LINK_REGULAR_NAME).delete();
                            Os.symlink(ColorOS_FONT_LINK_REGULAR_PATH, ColorOS_Burmese_FONT_LINK_REGULAR_NAME);
                        } catch (IllegalArgumentException e) {
                            Log.d(TAG, "SELinux policy update malformed: " + e.getMessage());
                        } catch (Exception e2) {
                            Log.d(TAG, "Could not update selinux policy: " + e2.getMessage());
                        }
                    }
                    try {
                        IActivityManager am = ActivityManagerNative.getDefault();
                        if (am != null) {
                            Configuration config = am.getConfiguration();
                            config.mOppoExtraConfiguration.mFlipFont = new Random(System.currentTimeMillis()).nextInt(10001) + 0;
                            am.updateConfiguration(config);
                        }
                    } catch (RemoteException e3) {
                        Log.e(TAG, "flipTypeface() RemoteException");
                    }
                    if (!"android.text.cts".equals(pkgName)) {
                        File FontDir = new File(ColorOS_FONT_LINK_REGULAR_NAME);
                        File FontDir2 = new File(ColorOS_FONT_LINK_BOLD_NAME);
                        FontDir.delete();
                        FontDir2.delete();
                    }
                }
            }
        }
    }

    private static void initFont() {
        if (sCurrentTypefaces == null) {
            sCurrentTypefaces = new Typeface[4];
        }
        File fontFile = new File("/data/system/font/ColorOS-Regular.ttf");
        File current_regular = new File(ColorOS_FONT_LINK_REGULAR_NAME);
        File current_bold = new File(ColorOS_FONT_LINK_BOLD_NAME);
        boolean needCallTypeFaceInit = false;
        if (fontFile.exists()) {
            try {
                isFlipFontUsed = true;
                if (!current_regular.exists()) {
                    Os.symlink("/data/system/font/ColorOS-Regular.ttf", ColorOS_FONT_LINK_REGULAR_NAME);
                } else if (!Os.readlink(ColorOS_FONT_LINK_REGULAR_NAME).equals("/data/system/font/ColorOS-Regular.ttf")) {
                    needCallTypeFaceInit = true;
                    Os.remove(ColorOS_FONT_LINK_REGULAR_NAME);
                    Os.symlink("/data/system/font/ColorOS-Regular.ttf", ColorOS_FONT_LINK_REGULAR_NAME);
                }
                if (!current_bold.exists()) {
                    Os.symlink("/data/system/font/ColorOS-Regular.ttf", ColorOS_FONT_LINK_BOLD_NAME);
                } else if (!Os.readlink(ColorOS_FONT_LINK_BOLD_NAME).equals("/data/system/font/ColorOS-Regular.ttf")) {
                    needCallTypeFaceInit = true;
                    Os.remove(ColorOS_FONT_LINK_BOLD_NAME);
                    Os.symlink("/data/system/font/ColorOS-Regular.ttf", ColorOS_FONT_LINK_BOLD_NAME);
                }
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "SELinux policy update malformed: " + e.getMessage());
                isFlipFontUsed = false;
            } catch (ErrnoException e2) {
                Log.d(TAG, "Could not update selinux policy: " + e2.getMessage());
                isFlipFontUsed = false;
            }
        } else {
            isFlipFontUsed = false;
        }
        if (!(isFlipFontUsed || (current_regular.exists() ^ 1) == 0)) {
            try {
                current_regular.delete();
                Os.symlink(ColorOS_FONT_LINK_REGULAR_PATH, ColorOS_FONT_LINK_REGULAR_NAME);
                current_bold.delete();
                Os.symlink(Android_FONT_LINK_BOLD_PATH, ColorOS_FONT_LINK_BOLD_NAME);
            } catch (IllegalArgumentException e3) {
                Log.d(TAG, "SELinux policy update malformed: " + e3.getMessage());
            } catch (ErrnoException e22) {
                Log.d(TAG, "Could not update selinux policy: " + e22.getMessage());
            }
        }
        boolean isMyanmarZgLinkChanged = false;
        if (new File(Android_Burmese_FONT_LINK_PATH).exists()) {
            try {
                if (!Os.readlink(ColorOS_Burmese_FONT_LINK_REGULAR_NAME).equals(Android_Burmese_FONT_LINK_PATH)) {
                    isMyanmarZgLinkChanged = true;
                }
                File burmese_font = new File(ColorOS_Burmese_FONT_LINK_REGULAR_NAME);
                if (!(burmese_font.exists() && (burmese_font.canRead() ^ 1) == 0)) {
                    burmese_font.delete();
                    Os.symlink(Android_Burmese_FONT_LINK_PATH, ColorOS_Burmese_FONT_LINK_REGULAR_NAME);
                }
            } catch (IllegalArgumentException e32) {
                Log.d(TAG, "SELinux policy update malformed: " + e32.getMessage());
            } catch (Exception e4) {
                Log.d(TAG, "Could not update selinux policy: " + e4.getMessage());
            }
        }
        if (!current_regular.exists() || (current_bold.exists() ^ 1) != 0 || isMyanmarZgLinkChanged || needCallTypeFaceInit) {
            Typeface.init();
        }
        try {
            Typeface tf = Typeface.createFromFile(new File(Os.readlink(ColorOS_FONT_LINK_REGULAR_NAME)));
            sCurrentTypefaces[0] = Typeface.create(tf, 0);
            sCurrentTypefaces[1] = Typeface.create(tf, 1);
            sCurrentTypefaces[2] = Typeface.create(tf, 2);
            sCurrentTypefaces[3] = Typeface.create(tf, 3);
        } catch (RuntimeException e5) {
            Log.e(TAG, "initFont() createFromFile fail");
        } catch (ErrnoException e222) {
            Log.e(TAG, "Could not update selinux policy: " + e222.getMessage());
        }
    }

    private static void freeCaches() {
        Canvas.freeCaches();
        Canvas.freeTextLayoutCaches();
    }
}
