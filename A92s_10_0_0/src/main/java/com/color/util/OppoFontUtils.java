package com.color.util;

import android.app.ColorUxIconConstants;
import android.content.res.ColorBaseConfiguration;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.ColorTypefaceInjector;
import android.graphics.OppoBaseTypeface;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Process;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.widget.TextView;
import com.color.util.OppoBaseFontUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

public class OppoFontUtils extends OppoBaseFontUtils {
    public static void setFlipFont(Configuration configuration) {
        SetFlipFont(configuration);
    }

    public static void SetFlipFont(Configuration configuration) {
        ColorBaseConfiguration baseConfiguration = (ColorBaseConfiguration) ColorTypeCastingHelper.typeCasting(ColorBaseConfiguration.class, configuration);
        logd("SetFlipFont -- sFlipFont=" + sFlipFont + ", sIsROM6d0FlipFont= " + sIsROM6d0FlipFont + " --> mFlipFont=" + baseConfiguration.mOppoExtraConfiguration.mFlipFont);
        if (baseConfiguration.mOppoExtraConfiguration != null && sFlipFont != baseConfiguration.mOppoExtraConfiguration.mFlipFont) {
            sFlipFont = baseConfiguration.mOppoExtraConfiguration.mFlipFont;
            initFont(false);
            doUpdateTypeface(configuration);
        }
    }

    public static void setFlipFontWhenUserChange(Configuration configuration) {
        ColorBaseConfiguration baseConfiguration = (ColorBaseConfiguration) ColorTypeCastingHelper.typeCasting(ColorBaseConfiguration.class, configuration);
        int currentUserId = baseConfiguration.mOppoExtraConfiguration.mFontUserId;
        logd("setFlipFontWhenUserChange -- mUserId in mOppoExtraConfiguration = " + currentUserId + ", sUserId = " + sUserId);
        if (currentUserId >= 0) {
            sFlipFont = baseConfiguration.mOppoExtraConfiguration.mFlipFont;
            sUserId = currentUserId;
            initFont(true);
            doUpdateTypeface(configuration);
        }
    }

    public static void updateTypefaceInCurrProcess(Configuration configuration) {
        ColorBaseConfiguration baseConfiguration = (ColorBaseConfiguration) ColorTypeCastingHelper.typeCasting(ColorBaseConfiguration.class, configuration);
        if (baseConfiguration != null && baseConfiguration.mOppoExtraConfiguration.mFontUserId == -1) {
            logd("invalid mFontUserId in extraConfiguration -1, abandon");
        } else if (baseConfiguration == null || sFlipFont != baseConfiguration.mOppoExtraConfiguration.mFlipFont || sUserId != baseConfiguration.mOppoExtraConfiguration.mFontUserId) {
            logd("<updateTypefaceInCurrProcess> myTid = " + Process.myTid() + ", myUid = " + Process.myUid() + ", myPid = " + Process.myPid() + ", mFontUserId = " + baseConfiguration.mOppoExtraConfiguration.mFontUserId);
            sFlipFont = baseConfiguration.mOppoExtraConfiguration.mFlipFont;
            sUserId = baseConfiguration.mOppoExtraConfiguration.mFontUserId;
            doUpdateTypeface(configuration);
        }
    }

    private static void doUpdateTypeface(Configuration configuration) {
        Typeface tf = null;
        if (new File(getCurrentUserDir()).exists()) {
            isFlipFontUsed = true;
        } else {
            isFlipFontUsed = false;
        }
        oppo.content.res.OppoFontUtils.isFlipFontUsed = isFlipFontUsed;
        try {
            String path = Os.readlink(FONTINFOARRAY_ROM6D0[4].mDataFontName);
            if (!TextUtils.isEmpty(path)) {
                tf = Typeface.createFromFile(new File(path));
            }
        } catch (RuntimeException e) {
            loge("RuntimeException initFont() createFromFile fail", e);
        } catch (ErrnoException e2) {
            loge("Could not update selinux policy initFont createFromFile ", e2);
        }
        boolean needTypefaceInit = false;
        if (sFlipFont == 10001) {
            needTypefaceInit = true;
        }
        freeCaches();
        if (needTypefaceInit) {
            String str = null;
            Typeface.DEFAULT.native_instance = Typeface.create(str, 0).native_instance;
            Typeface.DEFAULT_BOLD.native_instance = Typeface.create(str, 1).native_instance;
            Typeface.SANS_SERIF.native_instance = Typeface.create("sans-serif", 0).native_instance;
            Typeface.MONOSPACE.native_instance = Typeface.create("monospace", 0).native_instance;
            if (ColorTypefaceInjector.getSystemDefaultTypefaces() != null) {
                Typeface[] systemDefaults = {Typeface.DEFAULT, Typeface.DEFAULT_BOLD, Typeface.create(str, 2), Typeface.create(str, 3)};
            }
        }
        if (sCurrentTypefaces == null) {
            sCurrentTypefaces = new Typeface[4];
        }
        if (!isFlipFontUsed) {
            if (sCurrentTypefaces != null) {
                if (sCurrentTypefaces[0] != null) {
                    sCurrentTypefaces[0].native_instance = Typeface.DEFAULT.native_instance;
                }
                if (sCurrentTypefaces[1] != null) {
                    sCurrentTypefaces[1].native_instance = Typeface.DEFAULT_BOLD.native_instance;
                }
                if (sCurrentTypefaces[2] != null) {
                    sCurrentTypefaces[2].native_instance = defaultFromStyle(2).native_instance;
                }
                if (sCurrentTypefaces[3] != null) {
                    sCurrentTypefaces[3].native_instance = defaultFromStyle(3).native_instance;
                }
            }
            logd("Not using flip font");
        } else if (tf != null) {
            sCurrentTypefaces[0] = Typeface.create(tf, 0);
            sCurrentTypefaces[1] = Typeface.create(tf, 1);
            sCurrentTypefaces[2] = Typeface.create(tf, 2);
            sCurrentTypefaces[3] = Typeface.create(tf, 3);
            sCurrentTypefacesArray = new ArrayList(Arrays.asList(sCurrentTypefaces));
        }
    }

    public static void SetAppTypeFace(String sAppName) {
        setAppTypeFace(sAppName);
    }

    public static void setAppTypeFace(String sAppName) {
        if (FLITER_CTS_APP_PKG_LIST.contains(sAppName)) {
            sIsCheckCTS = true;
        }
        if (FLITER_NOT_REPLACEFONT_APP_PKG_LIST.contains(sAppName)) {
            sReplaceFont = false;
        }
    }

    public static void setNeedReplaceAllTypefaceApp(boolean flag) {
        sNeedReplaceAllTypefaceApp = flag;
    }

    public static boolean getNeedReplaceAllTypefaceApp() {
        return sNeedReplaceAllTypefaceApp;
    }

    public static Typeface flipTypeface(Typeface typeface) {
        OppoBaseTypeface oppoBaseTypeface = (OppoBaseTypeface) ColorTypeCastingHelper.typeCasting(OppoBaseTypeface.class, typeface);
        if (isFlipFontUsed && sReplaceFont && sCurrentTypefaces != null && (typeface == null || ((oppoBaseTypeface != null && oppoBaseTypeface.isLikeDefault) || ColorTypefaceInjector.isSystemTypeface(typeface)))) {
            return sCurrentTypefaces[typeface == null ? 0 : typeface.getStyle()];
        } else if (!isFlipFontUsed && typeface != null && sCurrentTypefacesArray != null && sCurrentTypefacesArray.contains(typeface)) {
            return defaultFromStyle(typeface.getStyle());
        } else {
            if (isFlipFontUsed || typeface == null || ColorTypefaceInjector.COLOROSUI_MEDIUM == null || !typeface.equals(ColorTypefaceInjector.COLOROSUI_MEDIUM) || isCurrentLanguageSupportMediumFont()) {
                return typeface;
            }
            return Typeface.DEFAULT_BOLD;
        }
    }

    public static void replaceFakeBoldToColorMedium(TextView textView, Typeface typeface, int style) {
        if (textView != null) {
            OppoBaseTypeface oppoBaseTypeface = (OppoBaseTypeface) ColorTypeCastingHelper.typeCasting(OppoBaseTypeface.class, typeface);
            if (sIsCheckCTS || (!(style == 1 || style == 3) || ((typeface != null && ((oppoBaseTypeface == null || !oppoBaseTypeface.isLikeDefault) && !Typeface.SANS_SERIF.equals(typeface) && (ColorTypefaceInjector.COLOROSUI_MEDIUM == null || !typeface.equals(ColorTypefaceInjector.COLOROSUI_MEDIUM)))) || !isCurrentLanguageSupportMediumFont()))) {
                textView.setTypeface(typeface, style);
            } else {
                textView.setTypeface(ColorTypefaceInjector.COLOROSUI_MEDIUM, style == 3 ? 2 : 0);
            }
        }
    }

    public static Typeface create(Typeface typeface, String familyName, int style) {
        return Typeface.create(typeface, style);
    }

    public static Typeface defaultFromStyle(int style) {
        Typeface[] systemDefaults = ColorTypefaceInjector.getSystemDefaultTypefaces();
        if (systemDefaults == null || style >= systemDefaults.length || style <= -1) {
            return null;
        }
        return systemDefaults[style];
    }

    private static boolean checkAndCorrectFlipFontLink(boolean userChange) {
        boolean needTypefaceInit = false;
        if (new File(getCurrentUserDir()).exists()) {
            isFlipFontUsed = relinkDataFontToTarget(2);
            oppo.content.res.OppoFontUtils.isFlipFontUsed = isFlipFontUsed;
        } else {
            logd("checkAndCorreectFlipFontLink flipedFontFile NOT exists");
            if (sFlipFont == 10001) {
                logd("checkAndCorrectFlipFontLink seems running CTS " + sFlipFont);
                needTypefaceInit = true;
            }
            isFlipFontUsed = false;
            oppo.content.res.OppoFontUtils.isFlipFontUsed = isFlipFontUsed;
        }
        if (!isFlipFontUsed) {
            relinkDataFontToTarget(1);
        }
        return needTypefaceInit;
    }

    private static boolean relinkDataFontToTarget(int target) {
        for (int i = 0; i < sFontLinkInfos.size(); i++) {
            OppoBaseFontUtils.FontLinkInfo fontLinkInfo = (OppoBaseFontUtils.FontLinkInfo) sFontLinkInfos.get(i);
            File dataFontLinkFile = new File(fontLinkInfo.mDataFontName);
            String targetFont = "";
            if (target == 1) {
                try {
                    targetFont = fontLinkInfo.mSystemFontName;
                } catch (IllegalArgumentException e) {
                    loge("SELinux policy update check and correct flipfont", e);
                    return false;
                } catch (ErrnoException e2) {
                    loge("Could not update selinux policy check and correct flipfont: " + fontLinkInfo.mDataFontName, e2);
                    return false;
                }
            } else if (target == 2) {
                targetFont = getCurrentUserDir();
            }
            if (!dataFontLinkFile.exists()) {
                dataFontLinkFile.delete();
                logd("relink font targetFont = " + targetFont + ", " + fontLinkInfo.mDataFontName);
                Os.symlink(targetFont, fontLinkInfo.mDataFontName);
            } else if (dataFontLinkFile.exists() && !Os.readlink(fontLinkInfo.mDataFontName).equals(targetFont)) {
                dataFontLinkFile.delete();
                Os.symlink(targetFont, fontLinkInfo.mDataFontName);
            }
        }
        return true;
    }

    private static void initFont(boolean userChange) {
        checkAndCorrectFlipFontLink(userChange);
    }

    public static void handleFactoryReset() {
        if (sIsROM6d0FlipFont) {
            try {
                File flipFontFile = new File(getCurrentUserDir());
                if (flipFontFile.exists()) {
                    flipFontFile.delete();
                }
            } catch (Exception e) {
                loge("Failed handleFactoryReset", e);
            }
        }
    }

    public static boolean handleTextViewSettypeface(Paint paint, Typeface tf) {
        Typeface replacedTypeface;
        if (paint == null || paint.getTypeface() == (replacedTypeface = flipTypeface(tf))) {
            return false;
        }
        paint.setTypeface(replacedTypeface);
        return true;
    }

    public static boolean isCurrentLanguageSupportMediumFont() {
        Locale currentLcale = Locale.getDefault();
        if (currentLcale == null || !SUPPORT_MEDIUM_FONT_LANGUAGE_LIST.contains(currentLcale.getLanguage())) {
            return false;
        }
        return true;
    }

    private static void freeCaches() {
        Canvas.freeCaches();
        Canvas.freeTextLayoutCaches();
    }

    private static boolean isROM6d0FlipFont() {
        return new File(SECOND_FONT_CONFIG_FILE).exists();
    }

    private static String getCurrentUserDir() {
        String userIdStr = sUserId + ColorUxIconConstants.IconLoader.FILE_SEPARATOR;
        if (sUserId == 0) {
            userIdStr = "";
        }
        return DATA_FONT_DIRECTORY + userIdStr + "ColorOS-Regular.ttf";
    }

    private static void killRunningProcess(boolean userChange) {
        try {
            new Thread() {
                /* class com.color.util.OppoFontUtils.AnonymousClass1 */

                public void run() {
                    InstallFont installfont = new InstallFont();
                    installfont.killRecentPackage();
                    installfont.killAppProcess();
                    super.run();
                }
            }.start();
        } catch (Exception e) {
            loge("<killRunningProcess>:", e);
        } catch (Error e2) {
            loge("<killRunningProcess>:", e2);
        }
    }

    static {
        File fontFile = new File(getCurrentUserDir());
        sFlipFont = -1;
        if (fontFile.exists()) {
            sFlipFont = new Random(System.currentTimeMillis()).nextInt((10000 - 0) + 1) + 0;
            isFlipFontUsed = true;
            oppo.content.res.OppoFontUtils.isFlipFontUsed = isFlipFontUsed;
        }
        sUserId = Process.myUserHandle().hashCode();
        initFont(false);
        if (new File("/system/etc/permissions/com.oppo.features_expCommon.xml").exists()) {
            sIsExp = true;
        } else {
            sIsExp = false;
        }
    }
}
