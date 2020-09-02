package com.android.server.wm.startingwindow;

import android.util.ArrayMap;
import com.android.server.policy.OppoPhoneWindowManager;
import com.google.android.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ColorStartingWindowContants {
    public static final String APP_TOKEN_DIALER = "com.android.contacts/.DialtactsActivityAlias";
    public static final String COLOR_OS_FLOATASSISTANT = "com.coloros.floatassistant";
    public static final int DEFAULT_STARTING_WINDOW_EXIT_ANIMATION_DURATION = 150;
    public static final String DIALER_PREFIX = "dialer_";
    public static final String FEATURE_STARINGWINDOW_PREVIEW_SUPPORT = "oppo.startingwindow.preview.support";
    public static final Set<String> FORCE_CLEAR_SPLASH_PACKAGES_START_FROM_LAUNCHER = Sets.newHashSet();
    public static final Set<String> FORCE_CLEAR_SPLASH_TOKENS = Sets.newHashSet(new String[]{"com.oppo.camera/.Camera", "com.tencent.mm/.plugin.voip.ui.VideoActivity"});
    public static final String OPPO_LAUNCHER_PACKAGE_NAME = "com.oppo.launcher";
    public static final Set<String> SNAPSHOT_FORCE_CLEAR_WHEN_DIFF_ORIENTATION = Sets.newArraySet(new String[]{"com.tencent.tmgp.sgame", "com.tencent.qqlive"});
    public static final ArrayMap<String, Integer> SPECIAL_APP_DRAWABLE_RES_KEY_MAP = new ArrayMap<>();
    public static final ArrayMap<String, String> SPECIAL_APP_SNAPSHOTS_KEY_MAP = new ArrayMap<>();
    public static final Set<String> SPLASH_BLACK_LIST_PACKAGES_FOR_SYSTEM_APPS = Sets.newArraySet(new String[]{"com.oppo.logkit", "com.oppo.im", "com.oppo.engineermode"});
    public static final Set<String> SPLASH_BLACK_LIST_PACKAGES_START_FROM_LAUNCHER = Sets.newHashSet(new String[]{"com.coloros.gallery3d", "com.oppo.camera", "com.coloros.safecenter", "com.coloros.onekeylockscreen", "org.mozilla.firefox", "com.google.android.googlequicksearchbox", "cn.kuwo.sing", "com.google.android.documentsui", "com.camerasideas.instashot", "com.microsoft.emmx", "com.tencent.FileManager"});
    public static final Set<String> SPLASH_BLACK_LIST_TOKENS_FOR_SYSTEM_APPS = Sets.newArraySet(new String[]{"com.oppo.camera/.Camera", "com.oppo.camera/.VideoCamera", "com.android.phone/.ColorMultipleSimActivity", "com.android.certinstaller/.CertInstallerMain", "com.android.settings/.Settings$ChannelNotificationSettingsActivity", "com.android.settings/.Settings$AppNotificationSettingsActivity"});
    public static final Set<String> SPLASH_BLACK_LIST_TOKENS_START_FROM_LAUNCHER = Sets.newArraySet(new String[]{"com.tencent.mm/.plugin.base.stub.WXShortcutEntryActivity", "com.coloros.encryption/com.coloros.safe.password.FileSafeActivity", "com.android.settings/.applications.InstalledAppDetails", "com.oppo.launcher/com.android.launcher3.proxy.ProxyActivityStarter"});
    public static final Set<String> SPLASH_SNAPSHOT_BLACK_PACKAGES_FOR_PROCESSRUNNING = Sets.newArraySet(new String[]{WECHAT_PACKAGE_NAME, "com.UCMobile", "com.ucmobile.lite"});
    public static final Set<String> SPLASH_SNAPSHOT_BLACK_SYSTEM_APP = Sets.newHashSet(new String[]{"com.coloros.gamespaceui"});
    public static final Set<String> SPLASH_SNAPSHOT_WHITE_THIRD_PARTY_APP = Sets.newHashSet(new String[]{WECHAT_PACKAGE_NAME, "com.eg.android.AlipayGphone", "com.UCMobile", "com.ucmobile.lite", "com.tencent.mobileqq", "com.tencent.mtt", "com.qzone", "cn.wps.moffice_eng", "com.tencent.karaoke", "com.tencent.feedsfast", "com.tencent.qqpim", "com.qihoo360.mobilesafe", "com.cubic.autohome", "com.tencent.token", "com.qihoo.loan", "com.taobao.mobile.dipei", "com.chinalife.ebz", "sogou.mobile.explorer", "com.campmobile.snowcamera", "com.qihoo.cleandroid_cn", "com.shoujiduoduo.ringtone", "com.ymnet.lajiclean", "com.gorgeous.lite", "bubei.tingshu", "com.baidu.baidutranslate", "com.sf.activity", "cn.eclicks.wzsearch", "com.duoduo.child.story", "com.glodon.drawingexplorer", "com.seebaby", "com.icbc.elife", "com.yek.android.kfc.activitys", "performance.oppo.com.helloworld"});
    public static final Map<String, Long> STARTING_DELAY_MAP = new HashMap();
    public static final Set<String> STARTING_WINDOW_EXIT_LONG_PACKAGE = Sets.newArraySet(new String[]{"com.sf.activity"});
    public static final Set<String> SYSTEM_APPS = Sets.newHashSet(new String[]{"com.android.settings", "com.android.browser", "com.android.phone", "com.android.wallpaper.livepicker", "com.android.calculator2", "com.android.calendar", "com.android.contacts", "com.android.mms", "com.android.stk", "com.android.packageinstaller", "com.android.permissioncontroller", "com.color.eyeprotect", OppoPhoneWindowManager.WALLET_PACKAGE_NAME, "com.redteamobile.roaming"});
    public static final Set<String> TASK_SNAPSHOT_BLACK_PACKAGES = Sets.newHashSet(new String[]{"com.oppo.camera", "com.android.contacts", "com.android.incallui", "com.heytap.quicksearchbox", "com.tudou.android", "com.google.android.dialer"});
    public static final Set<String> TASK_SNAPSHOT_BLACK_TOKENS = Sets.newHashSet(new String[]{"com.coloros.gallery3d/com.oppo.gallery3d.app.ViewGallery", "com.coloros.safecenter/.privacy.view.password.AppUnlockPasswordActivity"});
    public static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";

    static {
        SPECIAL_APP_SNAPSHOTS_KEY_MAP.put("com.tencent.mm/.app.WeChatSplashActivity", "com.tencent.mm/.ui.LauncherUI");
        SPECIAL_APP_SNAPSHOTS_KEY_MAP.put("com.UCMobile/com.uc.browser.InnerUCMobile", "com.UCMobile/.main.UCMobile");
        SPECIAL_APP_SNAPSHOTS_KEY_MAP.put("com.ucmobile.lite/com.uc.browser.InnerUCMobile", "com.ucmobile.lite/com.UCMobile.main.UCMobile");
        SPECIAL_APP_SNAPSHOTS_KEY_MAP.put("com.tencent.mtt/.MainActivity", "com.tencent.mtt/.SplashActivity");
        STARTING_DELAY_MAP.put("com.tencent.mtt", 150L);
        STARTING_DELAY_MAP.put("com.UCMobile", 400L);
        STARTING_DELAY_MAP.put("com.ucmobile.lite", 400L);
        SPECIAL_APP_DRAWABLE_RES_KEY_MAP.put("com.tencent.mm/.ui.LauncherUI", 201852334);
        SPECIAL_APP_DRAWABLE_RES_KEY_MAP.put("com.UCMobile/.main.UCMobile", 201852335);
        SPECIAL_APP_DRAWABLE_RES_KEY_MAP.put("com.ucmobile.lite/com.UCMobile.main.UCMobile", 201852335);
        SPECIAL_APP_DRAWABLE_RES_KEY_MAP.put("com.tencent.mtt/.SplashActivity", 201852333);
    }
}
