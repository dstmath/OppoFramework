package android.app;

import android.app.ColorUxIconConstants;
import android.text.TextUtils;
import com.google.android.collect.Sets;
import java.util.HashSet;
import java.util.Set;

public class ColorUxIconAppCheckUtils {
    private static final Set<String> DESK_ACTIVITY_LIST = Sets.newHashSet(new String[]{ColorUxIconConstants.IconLoader.COM_HEYTAP_MATKET});
    public static final HashSet<String> PRESET_APPS_LIST = new HashSet<String>() {
        /* class android.app.ColorUxIconAppCheckUtils.AnonymousClass1 */

        {
            add("com.android.settings");
            add("com.finshell.wallet");
            add("com.coloros.encryptiont");
            add("com.redteamobile.roaming");
            add("com.coloros.operationtips");
            add("com.coloros.shortcuts");
            add("com.coloros.gamespaceui");
            add("com.coloros.phonemanager");
            add("com.coloros.familyguard");
            add("com.coloros.gallery3d");
            add("com.coloros.video");
            add("com.coloros.aruler");
            add("com.oppo.camera");
            add("com.coloros.note");
            add("com.coloros.soundrecorder");
            add("com.coloros.filemanager");
            add("com.coloros.calculator");
            add("com.coloros.findmyphone");
            add("com.coloros.findphone.client2");
            add("com.coloros.compass2");
            add("com.android.fmradio");
            add("com.caf.fmradio");
            add("com.coloros.backuprestore");
            add("com.coloros.weather2");
            add("com.coloros.alarmclock");
            add("com.coloros.calendar");
            add("com.android.mms");
            add(ColorUxIconConstants.IconLoader.COM_ANDROID_CONTACTS);
            add("com.heytap.browser");
            add(ColorUxIconConstants.IconLoader.COM_HEYTAP_MATKET);
            add("com.heytap.themestore");
            add("com.nearme.play");
            add("com.nearme.gamecenter");
            add("com.heytap.smarthome");
            add("com.heytap.usercenter");
            add("com.heytap.speechassist");
            add("com.oppo.music");
            add("com.coloros.musiclink");
            add("com.coloros.onekeylockscreen");
            add("com.oppo.store");
            add("com.oppo.community");
            add("com.coloros.bbs");
            add("com.heytap.book");
            add("com.android.stk");
            add("com.heytap.reader");
            add("com.android.email");
            add("com.heytap.cloud");
            add("com.heytap.yoli");
            add("com.realmecomm.app");
            add("com.realmestore.app");
        }
    };
    private static final Set<String> SYSTEM_APP_LIST = Sets.newHashSet(new String[]{"com.android.systemui", "com.android.settings", "com.android.browser", "com.android.calculator2", "com.android.calendar", ColorUxIconConstants.IconLoader.COM_ANDROID_CONTACTS, "com.android.mms", "com.android.packageinstaller", "com.android.permissioncontroller", "com.color.eyeprotect"});

    public static boolean isDeskActivity(String packageName) {
        return DESK_ACTIVITY_LIST.contains(packageName);
    }

    public static boolean isPresetApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        return PRESET_APPS_LIST.contains(packageName);
    }

    public static boolean isSystemApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        return SYSTEM_APP_LIST.contains(packageName);
    }
}
