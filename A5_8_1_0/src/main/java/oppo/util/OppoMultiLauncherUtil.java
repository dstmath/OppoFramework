package oppo.util;

import android.app.IOppoActivityManager;
import android.app.OppoActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OppoMultiLauncherUtil {
    public static final String ACTION_MULTI_APP_RENAME = "oppo.intent.action.MULTI_APP_RENAME";
    public static final String ACTION_PACKAGE_ADDED = "oppo.intent.action.MULTI_APP_PACKAGE_ADDED";
    public static final String ACTION_PACKAGE_REMOVED = "oppo.intent.action.MULTI_APP_PACKAGE_REMOVED";
    public static boolean DEBUG_MULTI_APP = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final List<String> MULTIAPP_SETTINGS = Arrays.asList(new String[]{"show_password", "screen_off_timeout", "time_12_24"});
    public static final String MULTI_APP_ADDED = "multi_app_add";
    public static final String MULTI_APP_EXTRA = "user_id";
    public static final String MULTI_APP_REMOVED = "multi_app_remove";
    public static final String MULTI_TAG = "com.multiple.launcher";
    public static final String OPPO_ALLOWED_APP_FILE = "/data/oppo/coloros/multiapp/oppo_allowed_app.xml";
    public static final String OPPO_MULTI_APP_ALIAS_FILE = "/data/oppo/coloros/multiapp/oppo_multi_app_alias.xml";
    public static final String OPPO_MULTI_APP_CREATED_FILE = "/data/oppo/coloros/multiapp/oppo_multi_app.xml";
    private static final String TAG = "OppoMultiLauncherUtil";
    public static final int USER_ID = 999;
    public static final int USER_ORIGINAL = 0;
    private static IOppoActivityManager sAms = null;
    private static volatile OppoMultiLauncherUtil sMultiUtil = null;
    private static OppoActivityManager sOam = new OppoActivityManager();

    public static OppoMultiLauncherUtil getInstance() {
        if (sMultiUtil == null) {
            sMultiUtil = new OppoMultiLauncherUtil();
        }
        return sMultiUtil;
    }

    public void initAms(IOppoActivityManager ams) {
        sAms = ams;
    }

    public boolean isCrossUserSetting(String name) {
        return MULTIAPP_SETTINGS.contains(name);
    }

    public List<String> getAllowedMultiApp() {
        try {
            if (sAms != null) {
                return sAms.getAllowedMultiApp();
            }
            return sOam.getAllowedMultiApp();
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> getCreatedMultiApp() {
        try {
            if (sAms != null) {
                return sAms.getCreatedMultiApp();
            }
            return sOam.getCreatedMultiApp();
        } catch (Exception e) {
            return null;
        }
    }

    public String getAliasByPackage(String pkgName) {
        try {
            if (sAms != null) {
                return sAms.getAliasByPackage(pkgName);
            }
            return sOam.getAliasByPackage(pkgName);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isMultiApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        List<String> createdMultiApp = getCreatedMultiApp();
        if (createdMultiApp == null) {
            return false;
        }
        return createdMultiApp.contains(pkgName);
    }

    public boolean isMultiApp(int userId, String pkgName) {
        return false;
    }

    public boolean isMultiAppUri(Intent intent, String pkgName) {
        if (!(intent == null || pkgName == null)) {
            String targetData = intent.getDataString();
            String targetClip = null;
            Boolean targetSend = Boolean.valueOf(false);
            Boolean targetSendMultiple = Boolean.valueOf(false);
            Boolean targetMedia = Boolean.valueOf(false);
            if (intent.getClipData() != null) {
                targetClip = intent.getClipData().toString();
            }
            String action = intent.getAction();
            if (action != null) {
                if ("android.intent.action.SEND".equals(action)) {
                    Uri stream = (Uri) intent.getParcelableExtra("android.intent.extra.STREAM");
                    if (stream != null) {
                        targetSend = Boolean.valueOf(stream.toString().contains(pkgName));
                    }
                } else if ("android.intent.action.SEND_MULTIPLE".equals(action)) {
                    ArrayList<Uri> streams = intent.getParcelableArrayListExtra("android.intent.extra.STREAM");
                    if (streams != null) {
                        int i = 0;
                        while (i < streams.size()) {
                            if (streams.get(i) != null && ((Uri) streams.get(i)).toString().contains(pkgName)) {
                                targetSendMultiple = Boolean.valueOf(true);
                                break;
                            }
                            i++;
                        }
                    }
                } else if ("android.media.action.IMAGE_CAPTURE".equals(action) || "android.media.action.IMAGE_CAPTURE_SECURE".equals(action) || "android.media.action.VIDEO_CAPTURE".equals(action)) {
                    Uri output = (Uri) intent.getParcelableExtra("output");
                    if (output != null) {
                        targetMedia = Boolean.valueOf(output.toString().contains(pkgName));
                    }
                }
            }
            return (targetData != null && targetData.contains(pkgName)) || ((targetClip != null && targetClip.contains(pkgName)) || targetSend.booleanValue() || targetSendMultiple.booleanValue() || targetMedia.booleanValue());
        }
    }

    public boolean isMainApp(int userId, String pkgName) {
        return false;
    }

    private OppoMultiLauncherUtil() {
    }
}
