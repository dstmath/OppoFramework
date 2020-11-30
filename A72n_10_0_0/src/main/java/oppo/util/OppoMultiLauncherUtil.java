package oppo.util;

import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings;
import com.color.multiapp.ColorMultiAppManager;
import java.util.Arrays;
import java.util.List;

@Deprecated
public class OppoMultiLauncherUtil {
    public static final String ACTION_MULTI_APP_RENAME = "oppo.intent.action.MULTI_APP_RENAME";
    public static final String ACTION_PACKAGE_ADDED = "oppo.intent.action.MULTI_APP_PACKAGE_ADDED";
    public static final String ACTION_PACKAGE_REMOVED = "oppo.intent.action.MULTI_APP_PACKAGE_REMOVED";
    public static boolean DEBUG_MULTI_APP = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final List<String> MULTIAPP_SETTINGS = Arrays.asList(Settings.System.TEXT_SHOW_PASSWORD, Settings.System.SCREEN_OFF_TIMEOUT, Settings.System.TIME_12_24, Settings.System.SOUND_EFFECTS_ENABLED);
    public static final String MULTI_APP_ADDED = "multi_app_add";
    public static final String MULTI_APP_EXTRA = "user_id";
    public static final String MULTI_APP_REMOVED = "multi_app_remove";
    public static final String MULTI_CHANGE_USERID = "multi.change.userid";
    public static final String MULTI_TAG = "com.multiple.launcher";
    public static final String OPPO_ALLOWED_APP_FILE = "/data/oppo/coloros/multiapp/oppo_allowed_app.xml";
    public static final String OPPO_MULTI_APP_ALIAS_FILE = "/data/oppo/coloros/multiapp/oppo_multi_app_alias.xml";
    public static final String OPPO_MULTI_APP_CREATED_FILE = "/data/oppo/coloros/multiapp/oppo_multi_app.xml";
    private static final String TAG = "OppoMultiLauncherUtil";
    public static final int USER_ID = 999;
    public static final int USER_ORIGINAL = 0;
    private static volatile OppoMultiLauncherUtil sMultiUtil = null;

    public static OppoMultiLauncherUtil getInstance() {
        if (sMultiUtil == null) {
            sMultiUtil = new OppoMultiLauncherUtil();
        }
        return sMultiUtil;
    }

    private OppoMultiLauncherUtil() {
    }

    public boolean isCrossUserSetting(String name) {
        return MULTIAPP_SETTINGS.contains(name);
    }

    public List<String> getAllowedMultiApp() {
        return ColorMultiAppManager.getInstance().getAllowedMultiApp();
    }

    public List<String> getCreatedMultiApp() {
        return ColorMultiAppManager.getInstance().getCreatedMultiApp();
    }

    public String getAliasByPackage(String pkgName) {
        return ColorMultiAppManager.getInstance().getAliasMultiApp(pkgName);
    }

    public boolean isMultiApp(String pkgName) {
        return ColorMultiAppManager.getInstance().isCreatedMultiApp(pkgName);
    }

    public boolean isMultiApp(int userId, String pkgName) {
        return false;
    }

    public boolean isMultiAppUri(Intent intent, String pkgName) {
        return ColorMultiAppManager.getInstance().isMultiAppUri(intent, pkgName);
    }

    public boolean isMainApp(int userId, String pkgName) {
        return false;
    }
}
