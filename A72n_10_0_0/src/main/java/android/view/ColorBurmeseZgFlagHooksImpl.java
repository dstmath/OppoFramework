package android.view;

import android.app.ColorUxIconConstants;
import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings;
import android.system.Os;
import android.util.Log;
import com.color.util.OppoFontUtils;
import java.io.File;
import java.util.Locale;

public class ColorBurmeseZgFlagHooksImpl implements IColorBurmeseZgHooks {
    private static final String BURMESE_FONT_LINK_ON_DATA = "/data/format_unclear/font/ColorOSUI-Myanmar.ttf";
    private static final String CURRENT_FONT_BURMESE = "current_typeface_burmese";
    private static final String CURRENT_FONT_BURMESE_OLD = "current_typeface";
    private static final int FLIP_FONT_FLAG_FOR_BURMESE_UNICODE = 10003;
    private static final int FLIP_FONT_FLAG_FOR_BURMESE_ZG = 10002;
    private static final String SYSTEM_BURMESE_UNICODE_REAL_FONT_FILE = "/system/fonts/Roboto-Regular.ttf";
    private static final String SYSTEM_BURMESE_ZG_REAL_FONT_FILE = "/system/fonts/MyanmarZg.ttf";
    private static final String SYSTEM_DEFAULT_FONT = "system.default.font";
    private static final String TAG = "android.view.ColorBurmeseZgFlagHooksImpl";
    private boolean sIsZgFlagInited;
    private boolean sIsZgFlagOn;

    public void initBurmeseZgFlag(Context context) {
        if (OppoFontUtils.sIsExp) {
            if (!this.sIsZgFlagInited) {
                this.sIsZgFlagOn = isCurrentUseZgEncoding(context, null);
                this.sIsZgFlagInited = true;
            }
            OppoFontUtils.setNeedReplaceAllTypefaceApp(this.sIsZgFlagOn);
        }
    }

    public void updateBurmeseZgFlag(Context context) {
        if (OppoFontUtils.sIsExp) {
            this.sIsZgFlagOn = isCurrentUseZgEncoding(context, null);
            OppoFontUtils.setNeedReplaceAllTypefaceApp(this.sIsZgFlagOn);
        }
    }

    public boolean getZgFlag() {
        return OppoFontUtils.getNeedReplaceAllTypefaceApp();
    }

    public void updateBurmeseEncodingForUser(Context context, Configuration config, int userId) {
        if (context != null) {
            flipBurmeseEncoding(context, config);
        } else {
            Log.d(TAG, "updateBurmeseEncodingForUser : WARNING context == null");
        }
    }

    private boolean flipBurmeseEncoding(Context context, Configuration config) {
        if (context.getPackageManager().hasSystemFeature(ColorUxIconConstants.SystemProperty.FEATURE_OPPO_VERSION_EXP)) {
            if (relinkFontFile(BURMESE_FONT_LINK_ON_DATA, isCurrentUseZgEncoding(context, config) ? SYSTEM_BURMESE_ZG_REAL_FONT_FILE : SYSTEM_BURMESE_UNICODE_REAL_FONT_FILE)) {
                return true;
            }
        }
        return false;
    }

    private boolean relinkFontFile(String dataLink, String targetFont) {
        try {
            if (Os.readlink(dataLink).equals(targetFont)) {
                return false;
            }
            new File(dataLink).delete();
            Os.symlink(targetFont, dataLink);
            return true;
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "SELinux policy update malformed: " + e.getMessage());
            return false;
        } catch (Exception e2) {
            Log.d(TAG, "Could not update selinux policy: " + e2.getMessage());
            return false;
        }
    }

    private boolean isCurrentUseZgEncoding(Context context, Configuration config) {
        Locale defaultLocale;
        if (config != null) {
            if (!config.getLocales().isEmpty() && config.getLocales().get(0) != null && config.getLocales().get(0).getCountry().equals("ZG")) {
                return true;
            }
            if (config.getLocales().isEmpty() && (defaultLocale = Locale.getDefault()) != null && defaultLocale.getCountry().equals("ZG")) {
                return true;
            }
        }
        if (context != null) {
            String currentFontStr = Settings.System.getString(context.getContentResolver(), CURRENT_FONT_BURMESE);
            String currentOldFontStr = Settings.System.getString(context.getContentResolver(), CURRENT_FONT_BURMESE_OLD);
            if (currentFontStr == null || currentFontStr == "") {
                if (SYSTEM_BURMESE_ZG_REAL_FONT_FILE.equals(currentOldFontStr)) {
                    return true;
                }
            } else if (SYSTEM_BURMESE_ZG_REAL_FONT_FILE.equals(currentFontStr)) {
                return true;
            }
        }
        return false;
    }
}
