package com.android.server;

import android.app.AppGlobals;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.OppoPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import com.android.server.am.ColorAppCrashClearManager;
import com.android.server.am.ColorAppStartupListManager;
import com.android.server.am.ColorAppStartupManager;
import com.android.server.am.ColorCommonListManager;
import com.android.server.am.ColorHansManager;
import com.android.server.am.ColorHansRestriction;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.am.IColorCommonListManager;
import com.android.server.coloros.OppoFloatWindowListManager;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.wm.ColorAccessController;
import com.android.server.wm.ColorFreeformManagerService;
import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import com.color.settings.ColorSettings;
import com.color.settings.ColorSettingsChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorListManagerImpl extends OppoListManager {
    private static final String ACCOUNTSYNC_MANGER_FILE = "/data/oppo/coloros/startup/autoSyncWhiteList.txt";
    private static final String ACCOUNTSYNC_MANGER_PATH = "/data/oppo/coloros/startup";
    private static final String ALLOW_MANIFEST_NET_BRO_TAG = "AllowManifestNetBro";
    private static final String APP_BROADCAST_RESTRICT_WHITE_LIST = "appBroadcastRestrictedWhite";
    private static final int APP_PHONE_LIST_TYPE_EXP = 1;
    private static final int APP_PHONE_LIST_TYPE_INNER = 0;
    private static final int APP_PHONE_LIST_TYPE_RUS = 2;
    private static final String APP_PHONE_REFUSE_CPN = "appPhoneRefuseCpn";
    private static final String APP_SERVICE_RESTRICT_CONTROL_LIST = "appServiceRestrictedControllist";
    private static final String APP_SERVICE_RESTRICT_PROTECT_LIST = "appServiceRestrictedpProtectlist";
    private static final String AUTOBOOT_MANGER_FILE = "/startup/bootallow.txt";
    private static final String BACK_CLIP_INTERCEPT_WHITE_LIST = "BackClipInterceptWhiteList";
    private static final String BACK_KEY_FILTER_FILE = "/data/oppo/coloros/config/bkfilters.txt";
    private static final String BACK_KEY_FILTER_LIST = "BackKeyFilterList";
    private static final String BACK_KEY_FILTER_PATH = "/data/oppo/coloros/config/";
    private static final String BACK_KEY_KILL_SWITCH = "BackKeyKillSwitch";
    private static final String BOOT_SMART_WHITE_LIST = "boot_smart_white_list";
    private static final String BOOT_START_FORBIDDEN_CLEAR_TAG = "bootStartForbidClear";
    private static final String BOOT_START_FORBIDDEN_TAG = "bootStartForbid";
    private static final String BROWSER_WHITE_LIST_FILE = "/data/oppo/coloros/startup/browserWhiteList.txt";
    private static final String CUSTOMIZE_APP_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final boolean DEFAULT_BACK_KEY_SWITCH = true;
    private static final boolean DEFAULT_BACK_KEY_SWITCH_EXP = false;
    private static final String DEFAULT_MAX_VERSION_CODE = "max";
    private static final int DEFAULT_MIN_VERSION_CODE = 0;
    private static final int DEFAULT_MIN_VERSION_CODE_LIST_LENGTH = 2;
    private static final long DEFAULT_PROTECT_SELF_TIMEOUT = 7200000;
    private static final String DUMP_APP_PHONE_CONFIG = "appphoneconfig";
    private static final String DUMP_FORBID_LIST = "forbidList";
    private static final String DUMP_SYSTEM_APPS_LIST = "systemAppList";
    private static final String EMPTY_PROC_BOOTUP_PROTECT = "EmptyProcBootupProtect";
    private static final String EMPTY_PROC_PROTECT = "EmptyProcProtect";
    private static final String EVENTID_ADD_PROTECT_INFO = "self_protect_add";
    private static final String EVENTID_REMOVE_PROTECT_INFO = "self_protect_remove";
    private static final String EVENTID_TIMEOUT_PROTECT_INFO = "self_protect_timeout";
    private static final String FAST_APP_PKG = "com.nearme.instant.platform";
    private static final ArrayList<String> FILTER_SEPCIAL_SECURE_APP = new ArrayList<>();
    private static final String FORBIDDEN_TAG = "startForbidden";
    private static final String GLOBAL_CMCC_PKG = "GlobalCmccPkg";
    private static final String GLOBAL_CMCC_PROCESS = "GlobalCmccProcess";
    private static final String JOB_TIMEOUT_WHITE_LIST = "JobTimeoutWhiteList";
    private static final String KILL_RESTART_SERVICE_PRO = "KillRestartServiceProNew";
    private static final int LIST_LENGTH_INIT_NUM = 300;
    private static final String MAX_VERSION_CODE = "maxCode";
    private static final String MINI_PROGRAM_KEY = "miniprogram";
    private static final int MINI_PROGRAM_MAX_COUNT = 20;
    private static final String MIN_VERSION_CODE = "minCode";
    private static final int MSG_DELAY_1000_MS = 1000;
    private static final int MSG_HANDLE_APP_FROM_CONTROL_CENTER = 10;
    private static final int MSG_HANDLE_APP_FROM_NOTITY = 11;
    private static final int MSG_LOAD_SYSTEM_APP = 1;
    private static final String NOTIFICATION_SERVICE_TAG = "NotificationServiceApp";
    private static final String OPPO_ROM_BLACK_LIST_FILE = "/data/oppo/coloros/startup/sys_rom_black_list.xml";
    private static final String PRE_COLOROS = "com.coloros.";
    private static final String PRE_NEARME = "com.nearme.";
    private static final String PRE_OPPO = "com.oppo.";
    private static final String PROTECT_FORE_NET_TAG = "ProtectForeNet";
    private static final String PROTECT_FORE_TAG = "ProtectForeApp";
    private static final String PROTECT_SELF_TIMEOUT = "ProtectSelfTimeout";
    private static final String PROTECT_SELF_WHITE_LIST = "ProtectSelfWhiteList";
    private static final String QUICK_RESTART_PROC = "ServiceQuickRestartProc";
    private static final String REMOVE_TASK_FILTER_PKG_LIST = "RemoveTaskFilterPkgNew";
    private static final String REMOVE_TASK_FILTER_PROCESS_LIST = "RemoveTaskFilterProcessNew";
    private static final String SECUREPAY_ACTIVITY_TAG = "SecurePayActivity";
    private static final String SKIP_BROADCAST_FLAG_RESTRICTED = "skipBroadcastFlagRestricted";
    private static final String SYSTEM_CONFIG_LIST_FILE = "/data/oppo/coloros/config/systemConfigList.xml";
    private static final String SYS_STARTUP_ROOT = "/data/oppo/coloros/startup";
    private static final String TAG = "ColorListManagerImpl";
    private static final String TAG_CUSTOMIZE_AMS_CLEANUP_SWITCH = "CustomizeAmsCleanupSwitch";
    private static final String TAG_DO_CLEAR_REDUNDENT_TASK = "ClearRedundentTaskSwitch";
    private static final String TAG_PREVENT_REDUNDENT_START = "PreventRedundentStart";
    private static final String TAG_REDUNDENT_TASK_CLASS = "RedundentTaskClass";
    private static final String TAG_SYSTEM_DUMP_HEAP_ENABLE = "SystemDumpHeapEnable";
    private static final List<String> mAppRestrictedBroadcastList = Arrays.asList(new String[0]);
    private static final List<String> mAppRestrictedServiceControlList = Arrays.asList("com.coloros.weather.service#com.coloros.weather.service", "com.coloros.weather2#com.coloros.weather2", "com.coloros.findmyphone#com.coloros.findmyphone", "com.nearme.themestore#com.nearme.themestore", "com.heytap.themestore#com.heytap.themestore", "com.oppo.daydreamvideo#com.nearme.themestore", "com.oppo.daydreamvideo#com.heytap.themestore", "com.android.systemui#com.nearme.themestore", "com.android.systemui#com.heytap.themestore", "com.coloros.blacklistapp#com.coloros.blacklistapp", "com.oppo.ctautoregist#com.oppo.ctautoregist", "com.coloros.regservice#com.coloros.regservice", "com.coloros.wifisecuredetect#com.coloros.wifisecuredetect", "com.ted.number#com.ted.number", "com.coloros.oppopods#com.coloros.oppopods", "com.coloros.colorconnect#com.coloros.colorconnect", "android#com.coloros.personalassistant", "com.coloros.rcs.service#com.coloros.rcs.service", "com.coloros.apprecover#com.coloros.apprecover", "com.coloros.soundrecorder#com.coloros.soundrecorder", "com.nearme.romupdate#com.nearme.romupdate", "com.coloros.gesture#com.coloros.gesture", "com.coloros.gamespace#com.coloros.gamespaceui", "com.coloros.gamespace#com.coloros.gamedock", "com.coloros.calendar#com.coloros.calendar");
    private static final List<String> mAppServicesRestrictedDefaultProtectlist = Arrays.asList("com.coloros.familyguard", "com.opos.ads", "com.android.permissioncontroller", "com.android.dlna.service");
    private static final Object mCustomLock = new Object();
    private static final List<String> mDefaultBackClipInterceptWhiteList = Arrays.asList("com.tencent.mobileqq", ColorStartingWindowContants.WECHAT_PACKAGE_NAME);
    private static final List<String> mDefaultBackClipInterceptWhiteListExp = Arrays.asList("com.tencent.mobileqq", ColorStartingWindowContants.WECHAT_PACKAGE_NAME);
    private static final List<String> mDefaultBackKeyFilterList = Arrays.asList("com.tencent.mobileqq", ColorStartingWindowContants.WECHAT_PACKAGE_NAME, "com.oppo.im");
    private static final List<String> mDefaultBackKeyFilterListExp = Arrays.asList("com.tencent.mobileqq", ColorStartingWindowContants.WECHAT_PACKAGE_NAME, "com.oppo.im");
    private static final List<String> mDefaultBootStartForbidList = Arrays.asList("com.UCMobile.intl");
    private static final List<String> mDefaultCmccPkgList = Arrays.asList(new String[0]);
    private static final List<String> mDefaultCmccProcessList = Arrays.asList("com.greenpoint.android.mc10086.activity:McPushservice");
    private static final List<String> mDefaultEmptyProcBootupProtectList = Arrays.asList("com.android.mms", "com.android.providers.calendar", "jp.naver.line.android");
    private static final List<String> mDefaultEmptyProcProtectList = Arrays.asList("android.process.acore", "com.coloros.alarmclock", "android.process.contacts", "com.coloros.newsimdetect", "com.coloros.lives", "com.redteamobile.roaming", "com.android.vendors.bridge.softsim", "com.redteamobile.virtual.softsim", "com.mobiletools.systemhelper:register", "com.mobiletools.systemhelper");
    private static final List<String> mDefaultJobScheduleTimeoutWhiteList = Arrays.asList("com.coloros.gallery3d", "com.android.providers.media", "com.coloros.cloud", "com.heytap.cloud", "com.oppo.ota", "com.coloros.assistantscreen", "com.android.providers.downloads", "com.coloros.phonemanager");
    private static final List<String> mDefaultKillRestartServiceProList = Arrays.asList("com.tencent.mobileqq", ColorStartingWindowContants.WECHAT_PACKAGE_NAME);
    private static final List<String> mDefaultKillRestartServiceProListExp = Arrays.asList(ColorStartingWindowContants.WECHAT_PACKAGE_NAME, "com.tencent.mobileqq", "com.zing.zalo", "com.facebook.orca", "com.facebook.katana", "com.instagram.android", "jp.naver.line.android", "com.whatsapp", "com.bbm", "com.skype.raider", "com.viber.voip", "com.path", "com.facebook.lite", "com.truecaller", "com.bsb.hike", "com.snapchat.android", "com.twitter.android", "com.imo.android.imoim", "com.google.android.gm");
    private static final List<String> mDefaultProtectSelfWhiteList = Arrays.asList("com.coloros.alarmclock", "com.coloros.backuprestore", "com.coloros.backuprestore.remoteservice", "com.redteamobile.roaming", "com.coloros.screenshot", "com.oppo.ctautoregist", "com.android.keychain", "com.coloros.safesdkproxy", "com.cleanmaster.sdk", "com.coloros.oshare", "com.coloros.filemanager", "com.coloros.gallery3d", "com.coloros.safecenter", "com.oppo.customize.service", "com.coloros.blacklist", "com.coloros.healthcheck", "com.android.contacts", "com.android.mms", "com.coloros.weather.service", "com.coloros.blacklistapp", "com.oppo.launcher", "com.android.settings", ColorFreeformManagerService.FREEFORM_CALLER_PKG, "com.coloros.gesture", "com.coloros.notificationmanager", "com.coloros.simsettings", "com.coloros.wifibackuprestore", "com.ted.number", "com.android.calendar", "com.coloros.providers.fileinfo", "com.oppo.camera", "com.nearme.sync", "com.nearme.ocloud", "com.oppo.ota", "com.coloros.cloud", "com.heytap.cloud", "com.coloros.oppoguardelf", "com.coloros.screenrecorder", "com.android.cellbroadcastreceiver", "com.coloros.smartdrive", "com.oppo.videocallfloat", "com.coloros.wirelesssettings", "com.coloros.oppopods", "com.coloros.translate", "com.coloros.colorfilestand", "com.coloros.videoeditor", "com.coloros.personalassistant", "com.android.dlna.service");
    private static final List<String> mDefaultQuickRestartProcList = Arrays.asList("com.heytap.accessory", "com.oppo.autotest.cameramonitortest:remote", "com.tencent.mm:push", "com.tencent.mobileqq:MSF");
    private static final List<String> mDefaultRedundentTaskClassList = Arrays.asList("com.tencent.mm.plugin.appbrand.ui.AppBrandUI", "com.tencent.mm.plugin.appbrand.ui.AppBrandInToolsUI");
    private static final List<String> mDefaultRemoveTaskFilterPkgList = Arrays.asList(new String[0]);
    private static final List<String> mDefaultRemoveTaskFilterProListExp = Arrays.asList(new String[0]);
    private static final List<String> mDefaultRemoveTaskFilterProcessList = Arrays.asList("com.coloros.safecenter:clear_filter", "com.redteamobile.virtual.softsim", "com.tencent.mm:push", "com.tencent.mobileqq:MSF", "android.process.contacts");
    private static final List<String> mEmptyProcBootupProtectList = new ArrayList();
    private static final List<String> mEmptyProcProtectList = new ArrayList();
    private static final List<String> mKddiProtectList = Arrays.asList("jp.netstar.familysmile");
    private static final List<String> mProtectFromPkgWhiteList = Arrays.asList("OppoCustomizeService");
    private static final List<String> mQuickRestartProcList = new ArrayList();
    private static final Object mSFLock = new Object();
    private static final List<String> mSkipBroadcastFlagDefaultList = Arrays.asList(new String[0]);
    private static final List<String> mSoftbankDeviceManagerList = Arrays.asList("jp.softbank.mb.parentalcontrols");
    private static final List<String> mSoftbankProtectList = Arrays.asList("jp.softbank.mb.plusmessage", "jp.softbank.mb.parentalcontrols", "jp.softbank.tether.entitlement", "jp.softbank.mb.tdrl", "jp.softbank.mb.flcrlap");
    private static final List<String> mUqProtectList = Arrays.asList("com.kddi.familysmile.mvno", "jp.uqmobile.portal", "com.access_company.android.nfcommunicator", "jp.uqmobile.uqmobileportalapp");
    private static final List<String> mYMProtectList = Arrays.asList("jp.co.yahoo.android.ymobile.mail", "jp.softbank.mb.parentalcontrols", "jp.softbank.tether.entitlement", "jp.softbank.mb.tdrl", "jp.softbank.mb.flcrlap");
    private boolean DEBUG_SWITCH = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEFAULT_BACK_KEY_SWITCH_EXP);
    private List<String> mAccountSyncWhiteList = new ArrayList();
    private FileObserverPolicy mAccountSyncWhiteListFileObserver = null;
    private final Object mAccountSyncWhiteListLock = new Object();
    private List<String> mAllowManifestNetBroList = new ArrayList();
    private ArrayMap<String, List<String>> mAppBroadcastRestrictedWhitelist = new ArrayMap<>();
    private List<String> mAppPhoneCpnList = new ArrayList();
    private final Object mAppPhoneCpnListLock = new Object();
    private int mAppPhoneListType = 0;
    private ArrayMap<String, List<String>> mAppServicesRestrictedControllist = new ArrayMap<>();
    private List<String> mAppServicesRestrictedProtectlist = new ArrayList();
    private ArrayMap<String, String> mAppServicesRestrictedWhitelist = new ArrayMap<>();
    private FileObserverPolicy mAutoBootFileObserver = null;
    private SparseArray<List<String>> mAutoBootWhiteListContainer = new SparseArray<>();
    private final Object mAutoBootWhiteListLock = new Object();
    private List<String> mBackClipInterceptWhiteList = new ArrayList();
    private FileObserverPolicy mBackKeyCleanupFilterFileObserver = null;
    private final Object mBackKeyCleanupFilterListLock = new Object();
    private List<String> mBackKeyFilterList = new ArrayList();
    private boolean mBackKeyInitFromFile = DEFAULT_BACK_KEY_SWITCH_EXP;
    private boolean mBackKeyKillSwitch = true;
    private ArrayList<String> mBootStartForbidList = new ArrayList<>();
    private FileObserverPolicy mBrowserInterceptFileObserver = null;
    private List<String> mBrowserWhiteList = new ArrayList();
    private final Object mBrowserWhiteListLock = new Object();
    ColorSettingsChangeListener mColorConfigChangeListener = new ColorSettingsChangeListener(new Handler()) {
        /* class com.android.server.ColorListManagerImpl.AnonymousClass1 */

        public void onSettingsChange(boolean selfChange, String path, final int userId) {
            if (ColorListManagerImpl.this.DEBUG_SWITCH) {
                Log.v(ColorListManagerImpl.TAG, "on config change and maybe read config, path=" + path + ", userId=" + userId);
            }
            if (ColorListManagerImpl.AUTOBOOT_MANGER_FILE.equals(path)) {
                ColorListManagerImpl.this.mHandler.post(new Runnable() {
                    /* class com.android.server.ColorListManagerImpl.AnonymousClass1.AnonymousClass1 */

                    public void run() {
                        ColorListManagerImpl.this.readAutoBootListFile(userId);
                    }
                });
            }
        }
    };
    private ArrayMap<String, ArrayMap<String, Bundle>> mCommonConfigMap = new ArrayMap<>();
    private Context mContext;
    private ArrayMap<Integer, String> mCurLiveWallpaperMap = new ArrayMap<>();
    private List<String> mCustomAppList = new ArrayList();
    private boolean mCustomizeAmsCleanupSwitch = true;
    private List<String> mDefaultAccountSyncWhiteList = Arrays.asList("com.android.email", "com.google.android.gm");
    private List<String> mDefaultAllowManifestNetBroList = Arrays.asList("com.nearme.romupdate", "com.coloros.sau", "com.coloros.sauhelper", "com.oppo.ota", "com.nearme.statistics.rom");
    private List<String> mDefaultAppPhoneCpnList = Arrays.asList("com.tencent.mm.plugin.voip.ui.VideoActivity", "com.tencent.av.ui.VideoInviteFull", "com.tencent.av.ui.AVActivity", "com.tencent.av.ui.VideoInviteLock", "com.facebook.rtc.activities.WebrtcIncallFragmentHostActivity", "com.tencent.av.ui.VideoInviteActivity", "com.tencent.mm.plugin.multitalk.ui.MultiTalkMainUI");
    private List<String> mDefaultAppPhoneCpnListExp = Arrays.asList("com.tencent.mm.plugin.voip.ui.VideoActivity", "com.facebook.rtc.activities.WebrtcIncallFragmentHostActivity", "com.tencent.mm.plugin.multitalk.ui.MultiTalkMainUI");
    private List<String> mDefaultNotiServiceAppList = Arrays.asList(ColorStartingWindowContants.WECHAT_PACKAGE_NAME, "com.tencent.research.drop", "com.letv.android.client", "com.qiyi.video", "com.sohu.sohuvideo", "com.tencent.qqlive");
    private List<String> mDefaultProtectForeList = Arrays.asList(ColorStartingWindowContants.WECHAT_PACKAGE_NAME, "com.tencent.mobileqq", "com.tencent.tmgp.sgame");
    private List<String> mDefaultProtectForeNetList = Arrays.asList("com.tencent.tmgp.sgame");
    private List<String> mDefaultSecurePayActivityList = Arrays.asList(ColorAccessController.MM_PLUGIN_WALLET_PAY_UI_WALLETPAYUI_ACTIVITY_FILTER, "com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI", "com.tencent.kinda.framework.app.UIPageFragmentActivity");
    private List<String> mDefaultWhiteList = Arrays.asList("com.sohu.inputmethod.sogou", "com.oppo.mp_battery_autotest", "com.oppo.autotest.agingautotesttool", "com.oppo.PhenixTestServer", "com.oppo.networkautotest", "com.oppo.community", "com.nearme.note", "com.coloros.note", "com.tencent.tvoem", "com.oppo.ubeauty", "com.facebook.orca", "com.android.email");
    private boolean mDoClearRedundentSwitch = true;
    private boolean mDoPreventRedundentStartSwitch = DEFAULT_BACK_KEY_SWITCH_EXP;
    private boolean mExpVersion = DEFAULT_BACK_KEY_SWITCH_EXP;
    private ArrayMap<String, String> mFastAppThirdLoginMap = new ArrayMap<>();
    private final Object mFastAppThirdLoginMapLock = new Object();
    private ArrayMap<String, String> mFastAppWechatMap = new ArrayMap<>();
    private final Object mFastAppWechatPayMapLock = new Object();
    private OppoFloatWindowListManager mFloatWindowListManager;
    private List<String> mGlobalCmccPkgList = new ArrayList();
    private List<String> mGlobalCmccProcessList = new ArrayList();
    private Handler mHandler = null;
    private List<String> mJobScheduleTimeoutWhiteList = new ArrayList();
    private List<String> mKillRestartServiceProList = new ArrayList();
    private ArrayMap<String, String> mMiniProgramShareMap = new ArrayMap<>();
    private final Object mMiniProgramShareMapLock = new Object();
    private List<String> mNotificationServiceApp = new ArrayList();
    private List<String> mProtectForeList = new ArrayList();
    private List<String> mProtectForeNetList = new ArrayList();
    private List<ProtectSelfInfo> mProtectSelfInfos = new ArrayList();
    private long mProtectSelfTimeout = DEFAULT_PROTECT_SELF_TIMEOUT;
    private List<String> mProtectSelfWhiteList = new ArrayList();
    private List<String> mRedundentTaskClassList = new ArrayList();
    private List<String> mRemoveTaskFilterPkgList = new ArrayList();
    private List<String> mRemoveTaskFilterProcessList = new ArrayList();
    private List<String> mSecurePayActivityList = new ArrayList();
    private List<String> mSkipBroadcastFlagRestrictedList = new ArrayList();
    private ArrayMap<String, List<String>> mStartForbiddenList = new ArrayMap<>();
    private FileObserverPolicy mStartForbiddenObserver = null;
    private List<String> mStartFromControlCenterPkgList = new ArrayList();
    private final Object mStartFromControlCenterPkgLock = new Object();
    private List<String> mStartFromNotifyPkgList = new ArrayList();
    private final Object mStartFromNotifyPkgLock = new Object();
    private HashSet<String> mSystemAppList = new HashSet<>((int) LIST_LENGTH_INIT_NUM);
    private final Object mSystemAppListLock = new Object();
    private FileObserverPolicy mSystemConfigFileObserver = null;
    private final Object mSystemConfigListLock = new Object();
    private boolean mSystemDumpHeapEnable = DEFAULT_BACK_KEY_SWITCH_EXP;
    private boolean mUploadSelfProtectSwitch = true;

    static {
        FILTER_SEPCIAL_SECURE_APP.add("com.eg.android.AlipayGphone");
        FILTER_SEPCIAL_SECURE_APP.add(ColorStartingWindowContants.WECHAT_PACKAGE_NAME);
    }

    public ColorListManagerImpl() {
    }

    public ColorListManagerImpl(Context context) {
        initDir();
        initFileObserver();
        readBrowserWhiteListFile();
        synchronized (this.mSystemConfigListLock) {
            readSystemConfigListLocked();
        }
        synchronized (mSFLock) {
            readRomListFileLocked();
        }
        readBackKeyCleanupFilterFile();
        readAccountSyncWhiteListFile();
        synchronized (mCustomLock) {
            readCustomAppListFile();
        }
    }

    public void init() {
    }

    public void initCtx(Context context) {
        Log.i(TAG, "initCtx");
        this.mContext = context;
        registerConfigChangeListener();
        readAutoBootListFile(0);
        this.mExpVersion = context.getPackageManager().hasSystemFeature("oppo.version.exp");
        if (!this.mBackKeyInitFromFile) {
            if (this.mExpVersion) {
                this.mBackKeyKillSwitch = DEFAULT_BACK_KEY_SWITCH_EXP;
            } else {
                this.mBackKeyKillSwitch = true;
            }
        }
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new ColorListManagerImplHandler(thread.getLooper());
        sendMessage(1, 1000);
        registerLogModule();
        this.mFloatWindowListManager = new OppoFloatWindowListManager(context);
        this.mFloatWindowListManager.initBuildInAppsFloatWindowPermission();
        initAppPhoneCpnList(context);
        registerMultiUserMonitor();
    }

    private void initDir() {
        File sysStartupRoot = new File("/data/oppo/coloros/startup");
        File browserWhiteListFile = new File(BROWSER_WHITE_LIST_FILE);
        File systemConfigListFile = new File(SYSTEM_CONFIG_LIST_FILE);
        File romBlackListFile = new File(OPPO_ROM_BLACK_LIST_FILE);
        File backKeyCleanupFilterPath = new File(BACK_KEY_FILTER_PATH);
        File backKeyCleanupFilterFile = new File(BACK_KEY_FILTER_FILE);
        File accountSyncWhiteListPath = new File("/data/oppo/coloros/startup");
        File accountSyncWhiteListFile = new File(ACCOUNTSYNC_MANGER_FILE);
        try {
            if (!sysStartupRoot.exists()) {
                sysStartupRoot.mkdirs();
            }
            if (!browserWhiteListFile.exists()) {
                browserWhiteListFile.createNewFile();
            }
            if (!systemConfigListFile.exists()) {
                systemConfigListFile.createNewFile();
            }
            if (!romBlackListFile.exists()) {
                romBlackListFile.createNewFile();
                copyFile("/system/oppo/sys_rom_black_list.xml", OPPO_ROM_BLACK_LIST_FILE);
            }
            if (!backKeyCleanupFilterPath.exists()) {
                backKeyCleanupFilterPath.mkdirs();
            }
            if (!backKeyCleanupFilterFile.exists()) {
                backKeyCleanupFilterFile.createNewFile();
            }
            if (!accountSyncWhiteListPath.exists()) {
                accountSyncWhiteListPath.createNewFile();
            }
            if (!accountSyncWhiteListFile.exists()) {
                accountSyncWhiteListFile.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "initDir failed!!!");
            e.printStackTrace();
        }
    }

    private void copyFile(String fromFile, String toFile) {
        File sourceFile = new File(fromFile);
        File targetFile = new File(toFile);
        if (sourceFile.exists() && targetFile.exists()) {
            try {
                FileUtils.copyFile(sourceFile, targetFile);
            } catch (Exception e) {
                Log.e(TAG, "copyFile failed!!!");
                e.printStackTrace();
            }
        }
    }

    private void initFileObserver() {
        this.mBrowserInterceptFileObserver = new FileObserverPolicy(BROWSER_WHITE_LIST_FILE);
        this.mBrowserInterceptFileObserver.startWatching();
        this.mSystemConfigFileObserver = new FileObserverPolicy(SYSTEM_CONFIG_LIST_FILE);
        this.mSystemConfigFileObserver.startWatching();
        this.mStartForbiddenObserver = new FileObserverPolicy(OPPO_ROM_BLACK_LIST_FILE);
        this.mStartForbiddenObserver.startWatching();
        this.mBackKeyCleanupFilterFileObserver = new FileObserverPolicy(BACK_KEY_FILTER_FILE);
        this.mBackKeyCleanupFilterFileObserver.startWatching();
        this.mAccountSyncWhiteListFileObserver = new FileObserverPolicy(ACCOUNTSYNC_MANGER_FILE);
        this.mAccountSyncWhiteListFileObserver.startWatching();
    }

    private void registerConfigChangeListener() {
        ColorSettings.registerChangeListenerForAll(this.mContext, AUTOBOOT_MANGER_FILE, 0, this.mColorConfigChangeListener);
    }

    public boolean isAppStartForbidden(String pkgName) {
        List<String> codeList;
        if (pkgName == null) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        boolean result = DEFAULT_BACK_KEY_SWITCH_EXP;
        synchronized (mSFLock) {
            int length = this.mStartForbiddenList.size();
            for (int i = 0; i < length; i++) {
                if (pkgName.equals(this.mStartForbiddenList.keyAt(i)) && (codeList = this.mStartForbiddenList.valueAt(i)) != null && codeList.size() >= 2) {
                    result = inAppStartForbiddenCodeList(codeList, pkgName);
                }
            }
        }
        return result;
    }

    private PackageInfo getPackageInfo(String pkgName) {
        Context context = this.mContext;
        if (context == null || context.getPackageManager() == null) {
            return null;
        }
        try {
            return this.mContext.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            return null;
        }
    }

    private boolean inAppStartForbiddenCodeList(List<String> codeList, String pkgName) {
        for (int i = 0; i < codeList.size() - 1; i++) {
            try {
                if (Integer.parseInt(codeList.get(i)) == 0 && codeList.get(i + 1).equals(DEFAULT_MAX_VERSION_CODE)) {
                    return true;
                }
                PackageInfo info = getPackageInfo(pkgName);
                if (info != null) {
                    if (Integer.parseInt(codeList.get(i)) <= info.versionCode && codeList.get(i + 1).equals(DEFAULT_MAX_VERSION_CODE)) {
                        return true;
                    }
                    if (Integer.parseInt(codeList.get(i)) <= info.versionCode && info.versionCode <= Integer.parseInt(codeList.get(i + 1))) {
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "appstart forbindden exception!", e);
            }
        }
        return DEFAULT_BACK_KEY_SWITCH_EXP;
    }

    public boolean isBootStartFirbid(String pkgName) {
        boolean isAllow = DEFAULT_BACK_KEY_SWITCH_EXP;
        if (!TextUtils.isEmpty(pkgName)) {
            synchronized (this.mBootStartForbidList) {
                isAllow = this.mBootStartForbidList.contains(pkgName);
            }
        }
        return isAllow;
    }

    public List<String> getAutoBootWhiteList(int userId) {
        List<String> list;
        synchronized (this.mAutoBootWhiteListLock) {
            list = this.mAutoBootWhiteListContainer.get(userId);
        }
        return list;
    }

    private List<String> getAutoBootWhiteListNotNull(int userId) {
        List<String> list = this.mAutoBootWhiteListContainer.get(userId);
        if (list != null) {
            return list;
        }
        List<String> list2 = new ArrayList<>();
        this.mAutoBootWhiteListContainer.put(userId, list2);
        return list2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0067, code lost:
        if (r7.DEBUG_SWITCH == false) goto L_0x0081;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0069, code lost:
        if (r3 == false) goto L_0x0081;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x006b, code lost:
        android.util.Log.d(com.android.server.ColorListManagerImpl.TAG, r8 + " in autoStart list!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0081, code lost:
        return r3;
     */
    public boolean isInAutoBootWhiteList(String pkgName, int userId) {
        if (userId == 999) {
            userId = 0;
        }
        synchronized (this.mAutoBootWhiteListLock) {
            List<String> autoBootWhiteList = getAutoBootWhiteList(userId);
            boolean result = true;
            if (autoBootWhiteList == null) {
                if (ColorAppStartupListManager.getInstance().isInAutoWhiteList(pkgName)) {
                    if (this.DEBUG_SWITCH) {
                        Log.d(TAG, "isInDefaultList, userId=" + userId + pkgName);
                    }
                    return true;
                }
                if (this.DEBUG_SWITCH) {
                    Log.d(TAG, "isInAutoBootWhiteList, list is null, userId=" + userId);
                }
                return DEFAULT_BACK_KEY_SWITCH_EXP;
            } else if (!autoBootWhiteList.contains(pkgName)) {
                if (!isInBootSmartWhiteList(pkgName)) {
                    result = DEFAULT_BACK_KEY_SWITCH_EXP;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0033, code lost:
        if (r6.DEBUG_SWITCH == false) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0035, code lost:
        if (r3 == false) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0037, code lost:
        android.util.Log.d(com.android.server.ColorListManagerImpl.TAG, r7 + " in autoStart list!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004d, code lost:
        return r3;
     */
    public boolean isAutoStartWhiteList(String pkgName, int userId) {
        if (userId == 999) {
            userId = 0;
        }
        synchronized (this.mAutoBootWhiteListLock) {
            List<String> autoBootWhiteList = getAutoBootWhiteList(userId);
            if (autoBootWhiteList == null) {
                if (this.DEBUG_SWITCH) {
                    Log.d(TAG, "isInAutoBootWhiteList, list is null, userId=" + userId);
                }
                return DEFAULT_BACK_KEY_SWITCH_EXP;
            }
            boolean result = autoBootWhiteList.contains(pkgName);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x009d  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:87:? A[RETURN, SYNTHETIC] */
    public void readAutoBootListFile(int userId) {
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readAutoBootListFile start, userId=" + userId);
        }
        List<String> allowList = new ArrayList<>();
        InputStream is = null;
        InputStreamReader isReader = null;
        BufferedReader reader = null;
        try {
            InputStream is2 = ColorSettings.readConfigAsUser(this.mContext, AUTOBOOT_MANGER_FILE, userId, 0);
            InputStreamReader isReader2 = new InputStreamReader(is2);
            BufferedReader reader2 = new BufferedReader(isReader2);
            while (true) {
                String line = reader2.readLine();
                if (line == null) {
                    try {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (!TextUtils.isEmpty(line)) {
                    allowList.add(line.trim());
                }
            }
            reader2.close();
            isReader2.close();
            if (is2 != null) {
                is2.close();
            }
            if (0 == 0) {
                synchronized (this.mAutoBootWhiteListLock) {
                    List<String> list = getAutoBootWhiteListNotNull(userId);
                    if (!list.isEmpty()) {
                        list.clear();
                    }
                    list.addAll(allowList);
                }
            }
        } catch (Exception e2) {
            try {
                e2.printStackTrace();
                if (0 != 0) {
                    try {
                        reader.close();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                        if (1 != 0) {
                        }
                    }
                }
                if (0 != 0) {
                    isReader.close();
                }
                if (0 != 0) {
                    is.close();
                }
                if (1 != 0) {
                    synchronized (this.mAutoBootWhiteListLock) {
                        List<String> list2 = getAutoBootWhiteListNotNull(userId);
                        if (!list2.isEmpty()) {
                            list2.clear();
                        }
                        list2.addAll(allowList);
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        reader.close();
                    } catch (Exception e4) {
                        e4.printStackTrace();
                        if (1 == 0) {
                            synchronized (this.mAutoBootWhiteListLock) {
                                List<String> list3 = getAutoBootWhiteListNotNull(userId);
                                if (!list3.isEmpty()) {
                                    list3.clear();
                                }
                                list3.addAll(allowList);
                            }
                        }
                        throw th;
                    }
                }
                if (0 != 0) {
                    isReader.close();
                }
                if (0 != 0) {
                    is.close();
                }
                if (1 == 0) {
                }
                throw th;
            }
        }
    }

    public List<String> getBrowserWhiteList() {
        List<String> list;
        synchronized (this.mBrowserWhiteListLock) {
            list = this.mBrowserWhiteList;
        }
        return list;
    }

    public boolean isInBrowserWhiteList(String pkgName) {
        boolean result = DEFAULT_BACK_KEY_SWITCH_EXP;
        synchronized (this.mBrowserWhiteListLock) {
            if (this.mBrowserWhiteList.isEmpty()) {
                if (this.mDefaultWhiteList.contains(pkgName)) {
                    result = true;
                }
            } else if (this.mBrowserWhiteList.contains(pkgName)) {
                result = true;
            }
        }
        return result;
    }

    public boolean isCtaPackage(String pkgName) {
        if (pkgName.contains("com.cttl.")) {
            return true;
        }
        return DEFAULT_BACK_KEY_SWITCH_EXP;
    }

    public void readBrowserWhiteListFile() {
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readBrowserWhiteList start");
        }
        File browserWhiteListFile = new File(BROWSER_WHITE_LIST_FILE);
        if (!browserWhiteListFile.exists()) {
            Log.e(TAG, "browserWhiteListFile isn't exist!");
        }
        List<String> allowList = new ArrayList<>();
        FileReader fr = null;
        try {
            FileReader fr2 = new FileReader(browserWhiteListFile);
            BufferedReader reader = new BufferedReader(fr2);
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    try {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (!TextUtils.isEmpty(line)) {
                    allowList.add(line.trim());
                }
            }
            fr2.close();
            if (0 == 0) {
                synchronized (this.mBrowserWhiteListLock) {
                    this.mBrowserWhiteList.clear();
                    this.mBrowserWhiteList.addAll(allowList);
                }
            }
        } catch (Exception e2) {
            try {
                e2.printStackTrace();
                if (0 != 0) {
                    try {
                        fr.close();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
                if (1 == 0) {
                    synchronized (this.mBrowserWhiteListLock) {
                        this.mBrowserWhiteList.clear();
                        this.mBrowserWhiteList.addAll(allowList);
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        fr.close();
                    } catch (Exception e4) {
                        e4.printStackTrace();
                    }
                }
                if (1 == 0) {
                    synchronized (this.mBrowserWhiteListLock) {
                        this.mBrowserWhiteList.clear();
                        this.mBrowserWhiteList.addAll(allowList);
                    }
                }
                throw th;
            }
        }
    }

    public boolean inProtectForeList(String pkg) {
        boolean contains;
        if (pkg == null || pkg.equals("")) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        synchronized (this.mSystemConfigListLock) {
            contains = getProtectForeList().contains(pkg);
        }
        return contains;
    }

    public boolean inProtectForeNetList(String pkg) {
        boolean contains;
        if (pkg == null || pkg.equals("")) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        synchronized (this.mSystemConfigListLock) {
            contains = getProtectForeNetList().contains(pkg);
        }
        return contains;
    }

    public List<String> getProtectForeList() {
        synchronized (this.mSystemConfigListLock) {
            if (this.mProtectForeList != null) {
                if (!this.mProtectForeList.isEmpty()) {
                    return this.mProtectForeList;
                }
            }
            return this.mDefaultProtectForeList;
        }
    }

    public List<String> getProtectForeNetList() {
        synchronized (this.mSystemConfigListLock) {
            if (this.mProtectForeNetList != null) {
                if (!this.mProtectForeNetList.isEmpty()) {
                    return this.mProtectForeNetList;
                }
            }
            return this.mDefaultProtectForeNetList;
        }
    }

    public List<String> getSecurePayActivityList() {
        synchronized (this.mSystemConfigListLock) {
            if (this.mSecurePayActivityList != null) {
                if (!this.mSecurePayActivityList.isEmpty()) {
                    return this.mSecurePayActivityList;
                }
            }
            return this.mDefaultSecurePayActivityList;
        }
    }

    public List<String> getNotificationServiceList() {
        synchronized (this.mSystemConfigListLock) {
            if (this.mNotificationServiceApp != null) {
                if (!this.mNotificationServiceApp.isEmpty()) {
                    return this.mNotificationServiceApp;
                }
            }
            return this.mDefaultNotiServiceAppList;
        }
    }

    public ArrayList<String> getGlobalWhiteList(Context context) {
        return getGlobalWhiteList(context, 1);
    }

    public ArrayList<String> getGlobalWhiteList(Context context, int type) {
        ArrayList<String> list = new ArrayList<>();
        if (type == 1) {
            list.addAll(getGlobalCmccWhiteList(context));
            list.addAll(getStageProtectList());
            list.addAll(getOppoTestToolList(context));
        } else if (type == 2) {
            list.addAll(getGlobalCmccWhiteList(context));
            list.addAll(getOppoTestToolList(context));
        } else if (type == 4) {
            list.addAll(this.mFloatWindowListManager.getDefaultGrantBuildinApps());
        } else if (type == 5) {
            list.addAll(getGlobalCmccWhiteList(context));
            list.addAll(getOppoTestToolList(context));
        }
        return list;
    }

    public ArrayList<String> getGlobalCmccWhiteList(Context context) {
        ArrayList<String> list = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "LOOK OUT!! PMS is not ready yet! Can't get white process list!");
            return list;
        }
        if (pm.hasSystemFeature("oppo.system.cmcc.filter")) {
            List<String> list2 = this.mGlobalCmccPkgList;
            if (list2 == null || list2.isEmpty()) {
                Log.d(TAG, "no romupdate, use default");
                list.addAll(mDefaultCmccPkgList);
            } else {
                Log.d(TAG, "return romupdate list");
                list.addAll(this.mGlobalCmccPkgList);
            }
        }
        return list;
    }

    public ArrayList<String> getGlobalCmccCdsTestWhiteList(Context context) {
        ArrayList<String> list = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "LOOK OUT!! PMS is not ready yet! Can't get white process list!");
            return list;
        }
        if (pm.hasSystemFeature("oppo.system.cmcctest.cds")) {
            list.add("com.hugeland.cdsplus");
            list.add("com.hugeland.cdsplusmonitor");
        }
        return list;
    }

    public ArrayList<String> getGlobalProcessWhiteList(Context context) {
        ArrayList<String> list = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "LOOK OUT!! PMS is not ready yet! Can't get white process list!");
            return list;
        }
        if (pm.hasSystemFeature("oppo.system.cmcc.filter")) {
            List<String> list2 = this.mGlobalCmccProcessList;
            if (list2 == null || list2.isEmpty()) {
                list.addAll(mDefaultCmccProcessList);
            } else {
                list.addAll(this.mGlobalCmccProcessList);
            }
        }
        return list;
    }

    public ArrayList<String> getOppoTestToolList(Context context) {
        ArrayList<String> tempList = new ArrayList<>();
        if (context == null) {
            return tempList;
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Log.w(TAG, "pm is not ready, can't get test tool list");
            return tempList;
        }
        String[] pkgList = pm.getPackagesForUid(1000);
        if (pkgList != null) {
            for (String pkg : pkgList) {
                if (pkg != null && (pkg.startsWith("com.oppo.autotest.") || pkg.equals("com.oppo.ScoreAppMonitor") || pkg.equals("com.oppo.qetest") || pkg.equals("com.oppo.qemonitor"))) {
                    tempList.add(pkg);
                }
            }
        }
        return tempList;
    }

    public boolean isOppoTestTool(String pkgName, int uid) {
        if (TextUtils.isEmpty(pkgName)) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        if (!(uid == 1000)) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        if (!(pkgName.startsWith("com.oppo.autotest.") || pkgName.equals("com.oppo.ScoreAppMonitor") || pkgName.equals("com.oppo.qetest") || pkgName.equals("com.oppo.qemonitor"))) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        return true;
    }

    public ArrayList<String> getRemoveTaskFilterProcessList(Context context) {
        ArrayList<String> list = new ArrayList<>();
        List<String> list2 = this.mRemoveTaskFilterProcessList;
        if (list2 == null || list2.isEmpty()) {
            list.addAll(mDefaultRemoveTaskFilterProcessList);
        } else {
            list.addAll(this.mRemoveTaskFilterProcessList);
        }
        return list;
    }

    public ArrayList<String> getRemoveTaskFilterPkgList(Context context) {
        ArrayList<String> list = new ArrayList<>();
        List<String> list2 = this.mRemoveTaskFilterPkgList;
        if (list2 != null && !list2.isEmpty()) {
            list.addAll(this.mRemoveTaskFilterPkgList);
        } else if (context.getPackageManager().hasSystemFeature("oppo.version.exp")) {
            list.addAll(mDefaultRemoveTaskFilterProListExp);
        } else {
            list.addAll(mDefaultRemoveTaskFilterPkgList);
        }
        list.addAll(getOppoTestToolList(context));
        Context context2 = this.mContext;
        if (!(context2 == null || context2.getPackageManager() == null || (!this.mContext.getPackageManager().hasSystemFeature("coloros.customize.guardelf.jp.softbank.keepalive") && !this.mContext.getPackageManager().hasSystemFeature("coloros.customize.guardelf.ymobile.keepalive")))) {
            list.addAll(mSoftbankDeviceManagerList);
        }
        return list;
    }

    public ArrayList<String> getKillRestartServicePkgList(Context context) {
        ArrayList<String> list = new ArrayList<>();
        List<String> list2 = this.mKillRestartServiceProList;
        if (list2 != null && !list2.isEmpty()) {
            list.addAll(this.mKillRestartServiceProList);
        } else if (context.getPackageManager().hasSystemFeature("oppo.version.exp")) {
            list.addAll(mDefaultKillRestartServiceProListExp);
        } else {
            list.addAll(mDefaultKillRestartServiceProList);
        }
        return list;
    }

    public ArrayList<String> getRedundentTaskClassList() {
        ArrayList<String> list = new ArrayList<>();
        List<String> list2 = this.mRedundentTaskClassList;
        if (list2 == null || list2.isEmpty()) {
            list.addAll(mDefaultRedundentTaskClassList);
        } else {
            list.addAll(this.mRedundentTaskClassList);
        }
        return list;
    }

    public boolean isRedundentActivity(String activity) {
        if (activity == null || activity.isEmpty()) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        Iterator<String> it = getRedundentTaskClassList().iterator();
        while (it.hasNext()) {
            if (activity.contains(it.next())) {
                return true;
            }
        }
        return DEFAULT_BACK_KEY_SWITCH_EXP;
    }

    public boolean getClearRedundentTaskSwitch() {
        return this.mDoClearRedundentSwitch;
    }

    public boolean getPreventRedundentStartSwitch() {
        return this.mDoPreventRedundentStartSwitch;
    }

    public ArrayList<String> getBackKeyFilterList() {
        ArrayList<String> list = new ArrayList<>();
        List<String> list2 = this.mBackKeyFilterList;
        if (list2 != null && !list2.isEmpty()) {
            list.addAll(this.mBackKeyFilterList);
        } else if (this.mExpVersion) {
            list.addAll(mDefaultBackKeyFilterListExp);
        } else {
            list.addAll(mDefaultBackKeyFilterList);
        }
        return list;
    }

    public boolean getBackKeyKillSwitch() {
        return this.mBackKeyKillSwitch;
    }

    public ArrayList<String> getBackClipInterceptWhiteList() {
        ArrayList<String> list = new ArrayList<>();
        List<String> list2 = this.mBackClipInterceptWhiteList;
        if (list2 != null && !list2.isEmpty()) {
            list.addAll(this.mBackClipInterceptWhiteList);
        } else if (this.mExpVersion) {
            list.addAll(mDefaultBackClipInterceptWhiteListExp);
        } else {
            list.addAll(mDefaultBackClipInterceptWhiteList);
        }
        return list;
    }

    public ArrayList<String> getJobScheduleTimeoutWhiteList() {
        ArrayList<String> list = new ArrayList<>();
        List<String> list2 = this.mJobScheduleTimeoutWhiteList;
        if (list2 == null || list2.isEmpty()) {
            list.addAll(mDefaultJobScheduleTimeoutWhiteList);
        } else {
            list.addAll(this.mJobScheduleTimeoutWhiteList);
        }
        return list;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:323:0x0778, code lost:
        r7 = r20.mAppPhoneCpnListLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:324:0x077a, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:327:0x077f, code lost:
        if (r6.isEmpty() != false) goto L_0x078e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:328:0x0781, code lost:
        r20.mAppPhoneCpnList.clear();
        r20.mAppPhoneCpnList.addAll(r6);
        r20.mAppPhoneListType = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:329:0x078e, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:330:0x078f, code lost:
        r7 = r20.mAppServicesRestrictedControllist;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:331:0x0791, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:333:?, code lost:
        r20.mAppServicesRestrictedControllist.clear();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:334:0x079b, code lost:
        if (r2.isEmpty() != false) goto L_0x07a3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:335:0x079d, code lost:
        r20.mAppServicesRestrictedControllist.putAll((android.util.ArrayMap<? extends java.lang.String, ? extends java.util.List<java.lang.String>>) r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:336:0x07a3, code lost:
        parseLocalWhitelist(com.android.server.ColorListManagerImpl.APP_SERVICE_RESTRICT_CONTROL_LIST);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:337:0x07a8, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:338:0x07a9, code lost:
        r7 = r20.mAppServicesRestrictedProtectlist;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:339:0x07ab, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:341:?, code lost:
        r20.mAppServicesRestrictedProtectlist.clear();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:342:0x07b5, code lost:
        if (r3.isEmpty() != false) goto L_0x07bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:343:0x07b7, code lost:
        r20.mAppServicesRestrictedProtectlist.addAll(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:344:0x07bd, code lost:
        r20.mAppServicesRestrictedProtectlist.addAll(com.android.server.ColorListManagerImpl.mAppServicesRestrictedDefaultProtectlist);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:345:0x07c4, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:346:0x07c5, code lost:
        r7 = r20.mAppBroadcastRestrictedWhitelist;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:347:0x07c7, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:349:?, code lost:
        r20.mAppBroadcastRestrictedWhitelist.clear();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:350:0x07d1, code lost:
        if (r4.isEmpty() != false) goto L_0x07d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:351:0x07d3, code lost:
        r20.mAppBroadcastRestrictedWhitelist.putAll((android.util.ArrayMap<? extends java.lang.String, ? extends java.util.List<java.lang.String>>) r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:352:0x07d9, code lost:
        parseLocalWhitelist(com.android.server.ColorListManagerImpl.APP_BROADCAST_RESTRICT_WHITE_LIST);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:353:0x07de, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:354:0x07df, code lost:
        r7 = r20.mSkipBroadcastFlagRestrictedList;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:355:0x07e1, code lost:
        monitor-enter(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:357:?, code lost:
        r20.mSkipBroadcastFlagRestrictedList.clear();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:358:0x07eb, code lost:
        if (r5.isEmpty() != false) goto L_0x07f3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:359:0x07ed, code lost:
        r20.mSkipBroadcastFlagRestrictedList.addAll(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:360:0x07f3, code lost:
        r20.mSkipBroadcastFlagRestrictedList.addAll(com.android.server.ColorListManagerImpl.mSkipBroadcastFlagDefaultList);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:361:0x07fa, code lost:
        monitor-exit(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:363:?, code lost:
        r8.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:364:0x0801, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:365:0x0802, code lost:
        r7 = r0;
        r9 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:440:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:402:0x084e A[SYNTHETIC, Splitter:B:402:0x084e] */
    /* JADX WARNING: Removed duplicated region for block: B:410:0x0867 A[SYNTHETIC, Splitter:B:410:0x0867] */
    /* JADX WARNING: Removed duplicated region for block: B:418:0x0880 A[SYNTHETIC, Splitter:B:418:0x0880] */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x0893 A[SYNTHETIC, Splitter:B:425:0x0893] */
    /* JADX WARNING: Removed duplicated region for block: B:441:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:442:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:443:? A[RETURN, SYNTHETIC] */
    private void readSystemConfigListLocked() {
        Throwable th;
        NullPointerException e;
        IOException e2;
        StringBuilder sb;
        XmlPullParserException e3;
        IOException e4;
        File file;
        Iterator<Map.Entry<String, List<String>>> it;
        File file2;
        Iterator<Map.Entry<String, List<String>>> it2;
        if (!this.mProtectForeList.isEmpty()) {
            this.mProtectForeList.clear();
        }
        if (!this.mProtectForeNetList.isEmpty()) {
            this.mProtectForeNetList.clear();
        }
        if (!this.mSecurePayActivityList.isEmpty()) {
            this.mSecurePayActivityList.clear();
        }
        if (!this.mNotificationServiceApp.isEmpty()) {
            this.mNotificationServiceApp.clear();
        }
        if (!this.mAllowManifestNetBroList.isEmpty()) {
            this.mAllowManifestNetBroList.clear();
        }
        if (!this.mGlobalCmccPkgList.isEmpty()) {
            this.mGlobalCmccPkgList.clear();
        }
        if (!this.mGlobalCmccProcessList.isEmpty()) {
            this.mGlobalCmccProcessList.clear();
        }
        if (!this.mRemoveTaskFilterPkgList.isEmpty()) {
            this.mRemoveTaskFilterPkgList.clear();
        }
        if (!this.mRemoveTaskFilterProcessList.isEmpty()) {
            this.mRemoveTaskFilterProcessList.clear();
        }
        if (!this.mKillRestartServiceProList.isEmpty()) {
            this.mKillRestartServiceProList.clear();
        }
        if (!this.mRedundentTaskClassList.isEmpty()) {
            this.mRedundentTaskClassList.clear();
        }
        if (!this.mBackKeyFilterList.isEmpty()) {
            this.mBackKeyFilterList.clear();
        }
        if (!this.mBackClipInterceptWhiteList.isEmpty()) {
            this.mBackClipInterceptWhiteList.clear();
        }
        if (!this.mJobScheduleTimeoutWhiteList.isEmpty()) {
            this.mJobScheduleTimeoutWhiteList.clear();
        }
        if (!mQuickRestartProcList.isEmpty()) {
            mQuickRestartProcList.clear();
        }
        if (!mEmptyProcProtectList.isEmpty()) {
            mEmptyProcProtectList.clear();
        }
        if (!mEmptyProcBootupProtectList.isEmpty()) {
            mEmptyProcBootupProtectList.clear();
        }
        ArrayMap<String, List<String>> appServicesRestrictedControllist = new ArrayMap<>();
        List<String> appServicesRestrictedProtectlist = new ArrayList<>();
        ArrayMap<String, List<String>> appBroadcastRestrictedWhitelist = new ArrayMap<>();
        List<String> skipBroadcastFlagRestrictedList = new ArrayList<>();
        List<String> appPhoneCpnList = new ArrayList<>();
        File file3 = new File(SYSTEM_CONFIG_LIST_FILE);
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file3);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);
            while (true) {
                int type = parser.next();
                if (type == 2) {
                    String tagName = parser.getName();
                    if (this.DEBUG_SWITCH) {
                        try {
                            Log.i(TAG, " readSystemConfigListLocked tagName=" + tagName);
                        } catch (NullPointerException e5) {
                            e = e5;
                        } catch (XmlPullParserException e6) {
                            e3 = e6;
                            Log.e(TAG, "failed parsing ", e3);
                            if (stream != null) {
                            }
                        } catch (IOException e7) {
                            e4 = e7;
                            try {
                                Log.e(TAG, "failed IOException ", e4);
                                if (stream != null) {
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                if (stream != null) {
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            if (stream != null) {
                            }
                            throw th;
                        }
                    }
                    if (PROTECT_FORE_TAG.equals(tagName)) {
                        String packgeName = parser.nextText();
                        if (!packgeName.equals("")) {
                            this.mProtectForeList.add(packgeName);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked protect fore pkg = " + packgeName);
                            }
                        }
                        file = file3;
                    } else if (PROTECT_FORE_NET_TAG.equals(tagName)) {
                        String packgeName2 = parser.nextText();
                        if (!packgeName2.equals("")) {
                            this.mProtectForeNetList.add(packgeName2);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked protect fore net pkg = " + packgeName2);
                            }
                        }
                        file = file3;
                    } else if (SECUREPAY_ACTIVITY_TAG.equals(tagName)) {
                        String activityName = parser.nextText();
                        if (!activityName.equals("")) {
                            this.mSecurePayActivityList.add(activityName);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked securepay activity = " + activityName);
                            }
                        }
                        file = file3;
                    } else if (NOTIFICATION_SERVICE_TAG.equals(tagName)) {
                        String activityName2 = parser.nextText();
                        if (!activityName2.equals("")) {
                            this.mNotificationServiceApp.add(activityName2);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked notifation service activity = " + activityName2);
                            }
                        }
                        file = file3;
                    } else if (ALLOW_MANIFEST_NET_BRO_TAG.equals(tagName)) {
                        String packgeName3 = parser.nextText();
                        if (!packgeName3.equals("")) {
                            this.mAllowManifestNetBroList.add(packgeName3);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked allow manifest net bro pkg = " + packgeName3);
                            }
                        }
                        file = file3;
                    } else if (GLOBAL_CMCC_PKG.equals(tagName)) {
                        String packgeName4 = parser.nextText();
                        if (!packgeName4.equals("")) {
                            this.mGlobalCmccPkgList.add(packgeName4);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked global cmcc white pkg = " + packgeName4);
                            }
                        }
                        file = file3;
                    } else if (GLOBAL_CMCC_PROCESS.equals(tagName)) {
                        String processName = parser.nextText();
                        if (!processName.equals("")) {
                            this.mGlobalCmccProcessList.add(processName);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked global cmcc white process = " + processName);
                            }
                        }
                        file = file3;
                    } else if (REMOVE_TASK_FILTER_PKG_LIST.equals(tagName)) {
                        String packgeName5 = parser.nextText();
                        if (!packgeName5.equals("")) {
                            this.mRemoveTaskFilterPkgList.add(packgeName5);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked remove task filter pkg = " + packgeName5);
                            }
                        }
                        file = file3;
                    } else if (REMOVE_TASK_FILTER_PROCESS_LIST.equals(tagName)) {
                        String process = parser.nextText();
                        if (!process.equals("")) {
                            this.mRemoveTaskFilterProcessList.add(process);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked remove task filter process = " + process);
                            }
                        }
                        file = file3;
                    } else if (KILL_RESTART_SERVICE_PRO.equals(tagName)) {
                        String packgeName6 = parser.nextText();
                        if (!packgeName6.equals("")) {
                            this.mKillRestartServiceProList.add(packgeName6);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked kill service restart pkg = " + packgeName6);
                            }
                        }
                        file = file3;
                    } else if (TAG_REDUNDENT_TASK_CLASS.equals(tagName)) {
                        String className = parser.nextText();
                        if (!className.equals("")) {
                            this.mRedundentTaskClassList.add(className);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked redundent class = " + className);
                            }
                        }
                        file = file3;
                    } else if (TAG_DO_CLEAR_REDUNDENT_TASK.equals(tagName)) {
                        this.mDoClearRedundentSwitch = Boolean.parseBoolean(parser.nextText());
                        file = file3;
                    } else if (TAG_PREVENT_REDUNDENT_START.equals(tagName)) {
                        this.mDoPreventRedundentStartSwitch = Boolean.parseBoolean(parser.nextText());
                        file = file3;
                    } else if (BACK_KEY_KILL_SWITCH.equals(tagName)) {
                        this.mBackKeyKillSwitch = Boolean.parseBoolean(parser.nextText());
                        this.mBackKeyInitFromFile = true;
                        file = file3;
                    } else if (PROTECT_SELF_WHITE_LIST.equals(tagName)) {
                        String packgeName7 = parser.nextText();
                        if (!packgeName7.equals("")) {
                            this.mProtectSelfWhiteList.add(packgeName7);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked self protect pkg = " + packgeName7);
                            }
                        }
                        file = file3;
                    } else if (PROTECT_SELF_TIMEOUT.equals(tagName)) {
                        try {
                            this.mProtectSelfTimeout = Long.parseLong(parser.nextText());
                        } catch (NumberFormatException e8) {
                        }
                        file = file3;
                    } else if (BACK_CLIP_INTERCEPT_WHITE_LIST.equals(tagName)) {
                        String packgeName8 = parser.nextText();
                        if (!packgeName8.equals("")) {
                            this.mBackClipInterceptWhiteList.add(packgeName8);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked back clip white pkg = " + packgeName8);
                            }
                        }
                        file = file3;
                    } else if (JOB_TIMEOUT_WHITE_LIST.equals(tagName)) {
                        String packgeName9 = parser.nextText();
                        if (!packgeName9.equals("")) {
                            this.mJobScheduleTimeoutWhiteList.add(packgeName9);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked job timeout white pkg = " + packgeName9);
                            }
                        }
                        file = file3;
                    } else if (APP_PHONE_REFUSE_CPN.equals(tagName)) {
                        String appPhoneCpn = parser.nextText();
                        if (!appPhoneCpn.equals("")) {
                            appPhoneCpnList.add(appPhoneCpn);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked appPhoneCpn = " + appPhoneCpn);
                            }
                        }
                        file = file3;
                    } else if (APP_SERVICE_RESTRICT_CONTROL_LIST.equals(tagName)) {
                        String serviceRestrictWhiteText = parser.nextText();
                        ArrayMap<String, List<String>> newMap = parseAppRestrictedText(serviceRestrictWhiteText);
                        if (!newMap.isEmpty()) {
                            Iterator<Map.Entry<String, List<String>>> it3 = newMap.entrySet().iterator();
                            while (it3.hasNext()) {
                                Map.Entry<String, List<String>> entry = it3.next();
                                String callingPkg = entry.getKey();
                                if (TextUtils.isEmpty(callingPkg)) {
                                    file2 = file3;
                                    it2 = it3;
                                } else if (appServicesRestrictedControllist.containsKey(callingPkg)) {
                                    List<String> newList = entry.getValue();
                                    file2 = file3;
                                    try {
                                        List<String> list = appServicesRestrictedControllist.get(callingPkg);
                                        it2 = it3;
                                        newList.addAll(list);
                                        appServicesRestrictedControllist.put(callingPkg, newList);
                                    } catch (NullPointerException e9) {
                                        e = e9;
                                        Log.e(TAG, "failed parsing ", e);
                                        if (stream == null) {
                                        }
                                    } catch (XmlPullParserException e10) {
                                        e3 = e10;
                                        Log.e(TAG, "failed parsing ", e3);
                                        if (stream != null) {
                                        }
                                    } catch (IOException e11) {
                                        e4 = e11;
                                        Log.e(TAG, "failed IOException ", e4);
                                        if (stream != null) {
                                        }
                                    }
                                } else {
                                    file2 = file3;
                                    it2 = it3;
                                    appServicesRestrictedControllist.putAll((ArrayMap<? extends String, ? extends List<String>>) newMap);
                                }
                                it3 = it2;
                                file3 = file2;
                            }
                            file = file3;
                        } else {
                            file = file3;
                        }
                        if (this.DEBUG_SWITCH) {
                            Log.i(TAG, " readSystemConfigListLocked service restrict control list = " + serviceRestrictWhiteText);
                        }
                    } else {
                        file = file3;
                        if (APP_SERVICE_RESTRICT_PROTECT_LIST.equals(tagName)) {
                            String serviceRestrictProtectPkg = parser.nextText();
                            if (!TextUtils.isEmpty(serviceRestrictProtectPkg)) {
                                appServicesRestrictedProtectlist.add(serviceRestrictProtectPkg);
                            }
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked service restrict protect list = " + serviceRestrictProtectPkg);
                            }
                        } else if (APP_BROADCAST_RESTRICT_WHITE_LIST.equals(tagName)) {
                            String broadcastRestrictWhiteText = parser.nextText();
                            ArrayMap<String, List<String>> newMap2 = parseAppRestrictedText(broadcastRestrictWhiteText);
                            if (!newMap2.isEmpty()) {
                                Iterator<Map.Entry<String, List<String>>> it4 = newMap2.entrySet().iterator();
                                while (it4.hasNext()) {
                                    Map.Entry<String, List<String>> entry2 = it4.next();
                                    String keyAction = entry2.getKey();
                                    if (TextUtils.isEmpty(keyAction)) {
                                        it = it4;
                                    } else if (appBroadcastRestrictedWhitelist.containsKey(keyAction)) {
                                        List<String> newList2 = entry2.getValue();
                                        it = it4;
                                        newList2.addAll(appBroadcastRestrictedWhitelist.get(keyAction));
                                        appBroadcastRestrictedWhitelist.put(keyAction, newList2);
                                    } else {
                                        it = it4;
                                        appBroadcastRestrictedWhitelist.putAll((ArrayMap<? extends String, ? extends List<String>>) newMap2);
                                    }
                                    it4 = it;
                                }
                            }
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked broadcastRestricted = " + broadcastRestrictWhiteText);
                            }
                        } else if (SKIP_BROADCAST_FLAG_RESTRICTED.equals(tagName)) {
                            String skipPackage = parser.nextText();
                            if (!TextUtils.isEmpty(skipPackage)) {
                                skipBroadcastFlagRestrictedList.add(skipPackage);
                            }
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked skip broadcast flag = " + skipPackage);
                            }
                        } else if (QUICK_RESTART_PROC.equals(tagName)) {
                            String proc = parser.nextText();
                            if (!TextUtils.isEmpty(proc)) {
                                mQuickRestartProcList.add(proc);
                            }
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked add quick restart proc = " + proc);
                            }
                        } else if (EMPTY_PROC_PROTECT.equals(tagName)) {
                            String proc2 = parser.nextText();
                            if (!TextUtils.isEmpty(proc2)) {
                                synchronized (mEmptyProcProtectList) {
                                    mEmptyProcProtectList.add(proc2);
                                }
                            }
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked add protect empty proc = " + proc2);
                            }
                        } else if (EMPTY_PROC_BOOTUP_PROTECT.equals(tagName)) {
                            String proc3 = parser.nextText();
                            if (!TextUtils.isEmpty(proc3)) {
                                synchronized (mEmptyProcBootupProtectList) {
                                    mEmptyProcBootupProtectList.add(proc3);
                                }
                            }
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked add bootup protect empty proc = " + proc3);
                            }
                        } else if (TAG_SYSTEM_DUMP_HEAP_ENABLE.equals(tagName)) {
                            this.mSystemDumpHeapEnable = Boolean.parseBoolean(parser.nextText());
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked system_dump_heap = " + this.mSystemDumpHeapEnable);
                            }
                        } else if (TAG_CUSTOMIZE_AMS_CLEANUP_SWITCH.equals(tagName)) {
                            this.mCustomizeAmsCleanupSwitch = Boolean.parseBoolean(parser.nextText());
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readSystemConfigListLocked customizeAmsCleanupSwitch = " + this.mCustomizeAmsCleanupSwitch);
                            }
                        }
                    }
                } else {
                    file = file3;
                }
                if (type == 1) {
                    break;
                }
                file3 = file;
            }
            sb.append("Failed to close state FileInputStream ");
            sb.append(e2);
            Log.e(TAG, sb.toString());
        } catch (NullPointerException e12) {
            e = e12;
            Log.e(TAG, "failed parsing ", e);
            if (stream == null) {
                try {
                    stream.close();
                } catch (IOException e13) {
                    e2 = e13;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e14) {
            e3 = e14;
            Log.e(TAG, "failed parsing ", e3);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e15) {
                    e2 = e15;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e16) {
            e4 = e16;
            Log.e(TAG, "failed IOException ", e4);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e17) {
                    e2 = e17;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th4) {
            th = th4;
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e18) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e18);
                }
            }
            throw th;
        }
    }

    public List<String> getAllowManifestNetBroList() {
        synchronized (this.mSystemConfigListLock) {
            if (this.mAllowManifestNetBroList != null) {
                if (!this.mAllowManifestNetBroList.isEmpty()) {
                    return this.mAllowManifestNetBroList;
                }
            }
            return this.mDefaultAllowManifestNetBroList;
        }
    }

    /* access modifiers changed from: package-private */
    public class ProtectSelfInfo {
        String mFromPkg;
        String mPackageName;
        long mStartTime;
        long mTimeout;

        ProtectSelfInfo() {
        }
    }

    public void addStageProtectInfo(String pkg, String fromPkg, long timeout) {
        long timeout2;
        Log.i(TAG, "addStageProtectInfo from " + fromPkg + " for " + pkg + " in " + timeout);
        if (pkg != null && !pkg.equals("") && fromPkg != null && !fromPkg.equals("")) {
            if (SystemProperties.getBoolean("persist.selfprotect.test", (boolean) DEFAULT_BACK_KEY_SWITCH_EXP) || getProtectSelfWhiteList().contains(pkg) || mProtectFromPkgWhiteList.contains(fromPkg)) {
                if (mProtectFromPkgWhiteList.contains(fromPkg) || timeout <= this.mProtectSelfTimeout) {
                    timeout2 = timeout;
                } else {
                    timeout2 = this.mProtectSelfTimeout;
                }
                removeStageProtectInfoInternal(pkg, fromPkg, DEFAULT_BACK_KEY_SWITCH_EXP);
                synchronized (this.mProtectSelfInfos) {
                    long now = SystemClock.elapsedRealtime();
                    ProtectSelfInfo info = new ProtectSelfInfo();
                    info.mPackageName = pkg;
                    info.mFromPkg = fromPkg;
                    info.mStartTime = now;
                    info.mTimeout = timeout2;
                    this.mProtectSelfInfos.add(info);
                    uploadProtecetSelfInfo(EVENTID_ADD_PROTECT_INFO, info);
                    if (this.DEBUG_SWITCH) {
                        Log.i(TAG, "add protect self info for " + pkg + ", timeout=" + timeout2);
                    }
                }
                return;
            }
            Log.w(TAG, pkg + " not in whitelist, can't add to protect, please check.");
        }
    }

    public void removeStageProtectInfo(String pkg, String fromPkg) {
        removeStageProtectInfoInternal(pkg, fromPkg, true);
    }

    public void removeStageProtectInfoInternal(String pkg, String fromPkg, boolean fromBinder) {
        Log.i(TAG, "removeStageProtectInfo from " + fromPkg + " for " + pkg);
        if (pkg != null && !pkg.equals("") && fromPkg != null && !fromPkg.equals("")) {
            synchronized (this.mProtectSelfInfos) {
                Iterator<ProtectSelfInfo> sListIterator = this.mProtectSelfInfos.iterator();
                while (sListIterator.hasNext()) {
                    ProtectSelfInfo info = sListIterator.next();
                    if (!(info == null || info.mPackageName == null || !info.mPackageName.equals(pkg) || info.mFromPkg == null || !info.mFromPkg.equals(fromPkg))) {
                        sListIterator.remove();
                        if (fromBinder) {
                            uploadProtecetSelfInfo(EVENTID_REMOVE_PROTECT_INFO, info);
                        }
                        if (this.DEBUG_SWITCH) {
                            Log.i(TAG, "remove finished protect self info for " + pkg);
                        }
                    }
                }
            }
        }
    }

    public ArrayList<String> getStageProtectList() {
        ArrayList<String> tempList = new ArrayList<>();
        synchronized (this.mProtectSelfInfos) {
            Iterator<ProtectSelfInfo> sListIterator = this.mProtectSelfInfos.iterator();
            long now = SystemClock.elapsedRealtime();
            while (sListIterator.hasNext()) {
                ProtectSelfInfo info = sListIterator.next();
                long from = now - info.mStartTime;
                if (from < info.mTimeout) {
                    tempList.add(info.mPackageName);
                } else {
                    sListIterator.remove();
                    uploadProtecetSelfInfo(EVENTID_TIMEOUT_PROTECT_INFO, info);
                    if (this.DEBUG_SWITCH) {
                        Log.i(TAG, "remove overtime protect self info for " + info.mPackageName + " added " + from + "ms ago whose timeout is " + info.mTimeout);
                    }
                }
            }
        }
        return tempList;
    }

    public ArrayList<String> getStageProtectListFromPkg(String pkg, int type) {
        ArrayList<String> tempList = new ArrayList<>();
        synchronized (this.mProtectSelfInfos) {
            Iterator<ProtectSelfInfo> sListIterator = this.mProtectSelfInfos.iterator();
            long now = SystemClock.elapsedRealtime();
            while (sListIterator.hasNext()) {
                ProtectSelfInfo info = sListIterator.next();
                long from = now - info.mStartTime;
                if (from >= info.mTimeout) {
                    sListIterator.remove();
                    if (this.DEBUG_SWITCH) {
                        Log.i(TAG, "remove overtime protect self info for " + info.mPackageName + " added " + from + "ms ago whose timeout is " + info.mTimeout);
                    }
                } else if (pkg != null && pkg.equals(info.mFromPkg)) {
                    tempList.add(info.mPackageName);
                }
            }
        }
        return tempList;
    }

    private ArrayList<String> getProtectSelfWhiteList() {
        ArrayList<String> list = new ArrayList<>();
        List<String> list2 = this.mProtectSelfWhiteList;
        if (list2 == null || list2.isEmpty()) {
            list.addAll(mDefaultProtectSelfWhiteList);
        } else {
            list.addAll(this.mProtectSelfWhiteList);
        }
        return list;
    }

    private void uploadProtecetSelfInfo(String eventId, ProtectSelfInfo info) {
        if (this.mContext != null && this.mUploadSelfProtectSwitch && info != null) {
            HashMap<String, String> map = new HashMap<>();
            map.put("protectedPkg", info.mPackageName);
            map.put("fromPkg", info.mFromPkg);
            map.put("timeout", String.valueOf(info.mTimeout));
            OppoStatistics.onCommon(this.mContext, "20120", eventId, map, (boolean) DEFAULT_BACK_KEY_SWITCH_EXP);
        }
    }

    public boolean inPaySafePkgList(String pkg) {
        try {
            if (FILTER_SEPCIAL_SECURE_APP.contains(pkg) || !new OppoPackageManager().isSecurePayApp(pkg)) {
                return DEFAULT_BACK_KEY_SWITCH_EXP;
            }
            Log.d(TAG, "inPaySafePkgList : " + pkg);
            return true;
        } catch (RemoteException e) {
            Log.i(TAG, "Cannot find remote package");
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x014c  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0164  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0178  */
    private void readRomListFileLocked() {
        StringBuilder sb;
        IOException e;
        ArrayMap<String, List<String>> forbiddenList = new ArrayMap<>();
        ArrayList<String> bootStartForibidList = new ArrayList<>();
        boolean isClearBootStartForibidList = DEFAULT_BACK_KEY_SWITCH_EXP;
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(new File(OPPO_ROM_BLACK_LIST_FILE));
            XmlPullParser parser = Xml.newPullParser();
            String str = null;
            parser.setInput(stream, null);
            while (true) {
                int type = parser.next();
                if (type == 2) {
                    String tagName = parser.getName();
                    if (this.DEBUG_SWITCH) {
                        Log.i(TAG, " readRomListFileLocked tagName=" + tagName);
                    }
                    if (FORBIDDEN_TAG.equals(tagName)) {
                        String minCode = parser.getAttributeValue(str, MIN_VERSION_CODE);
                        String maxCode = parser.getAttributeValue(str, MAX_VERSION_CODE);
                        String packageName = parser.nextText();
                        if (minCode == null) {
                            minCode = String.valueOf(0);
                        }
                        if (maxCode == null) {
                            maxCode = DEFAULT_MAX_VERSION_CODE;
                        }
                        if (!packageName.equals("") && minCode != null) {
                            List<String> versionCodeList = new ArrayList<>();
                            versionCodeList.add(minCode);
                            versionCodeList.add(maxCode);
                            if (forbiddenList.containsKey(packageName)) {
                                versionCodeList.addAll(forbiddenList.get(packageName));
                            }
                            forbiddenList.put(packageName, versionCodeList);
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readRomListFileLocked pkg= " + packageName + " minCode=" + minCode + " maxCode=" + maxCode);
                            }
                        }
                    } else if (BOOT_START_FORBIDDEN_TAG.equals(tagName)) {
                        String pkgName = parser.nextText();
                        if (this.DEBUG_SWITCH) {
                            Log.i(TAG, " readRomListFileLocked pkg= " + pkgName);
                        }
                        if (!TextUtils.isEmpty(pkgName)) {
                            bootStartForibidList.add(pkgName);
                            if (pkgName.equals(BOOT_START_FORBIDDEN_CLEAR_TAG)) {
                                isClearBootStartForibidList = true;
                            }
                        }
                    }
                }
                if (type == 1) {
                    try {
                        break;
                    } catch (IOException e2) {
                        e = e2;
                        sb = new StringBuilder();
                    }
                } else {
                    str = null;
                }
            }
            stream.close();
        } catch (Exception e3) {
            Log.e(TAG, "failed parsing ", e3);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e4) {
                    e = e4;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e5);
                }
            }
            throw th;
        }
        if (!forbiddenList.isEmpty()) {
            synchronized (mSFLock) {
                this.mStartForbiddenList.clear();
                this.mStartForbiddenList.putAll((ArrayMap<? extends String, ? extends List<String>>) forbiddenList);
            }
        }
        if (bootStartForibidList.isEmpty()) {
            synchronized (this.mBootStartForbidList) {
                this.mBootStartForbidList.clear();
                if (!isClearBootStartForibidList) {
                    this.mBootStartForbidList.addAll(bootStartForibidList);
                }
            }
            return;
        }
        synchronized (this.mBootStartForbidList) {
            this.mBootStartForbidList.clear();
            this.mBootStartForbidList.addAll(mDefaultBootStartForbidList);
        }
        return;
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Log.e(TAG, sb.toString());
        if (!forbiddenList.isEmpty()) {
        }
        if (bootStartForibidList.isEmpty()) {
        }
    }

    public boolean isInstalledAppWidget(String pkgName, int uid) {
        if (pkgName == null) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        return !OppoFeatureCache.get(IColorCommonListManager.DEFAULT).getAppInfo(ColorCommonListManager.CONFIG_WIDGET, uid).isEmpty();
    }

    public boolean isFromNotifyPkg(String pkgName) {
        boolean contains;
        if (pkgName == null || pkgName.equals("")) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        try {
            synchronized (this.mStartFromNotifyPkgLock) {
                contains = this.mStartFromNotifyPkgList.contains(pkgName);
            }
            return contains;
        } catch (Exception e) {
            Log.e(TAG, "Failed to judge isFromNotifyPkg!");
            e.printStackTrace();
            return true;
        }
    }

    public void addFromNotifyPkgList(String pkgName) {
        if (pkgName != null && !pkgName.equals("")) {
            synchronized (this.mStartFromNotifyPkgLock) {
                this.mStartFromNotifyPkgList.add(pkgName);
            }
        }
    }

    public void removeFromNotifyPkgList(String pkgName) {
        if (pkgName != null && !pkgName.equals("")) {
            synchronized (this.mStartFromNotifyPkgLock) {
                this.mStartFromNotifyPkgList.remove(pkgName);
            }
        }
    }

    public List<String> getFromNotifyPkgList() {
        List<String> list;
        synchronized (this.mStartFromNotifyPkgLock) {
            list = this.mStartFromNotifyPkgList;
        }
        return list;
    }

    public void handleAppForNotification(String pkgName, int uid, int otherInfo) {
        if (!TextUtils.isEmpty(pkgName)) {
            try {
                AppGlobals.getPackageManager().setPackageStoppedState(pkgName, (boolean) DEFAULT_BACK_KEY_SWITCH_EXP, UserHandle.getUserId(uid));
            } catch (RemoteException e) {
            } catch (IllegalArgumentException e2) {
                Log.w(TAG, "handleAppForNotification " + e2);
            }
            addFromNotifyPkgList(pkgName);
            sendMessage(11, pkgName, 10000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAppFromNotityMsg(Message msg) {
        String pkgName = (String) msg.obj;
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "handleAppFromNotityMsg pkgName " + pkgName);
        }
        removeFromNotifyPkgList(pkgName);
    }

    public boolean isFromControlCenterPkg(String pkgName) {
        boolean contains;
        if (pkgName == null || pkgName.equals("")) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        try {
            synchronized (this.mStartFromControlCenterPkgLock) {
                contains = this.mStartFromControlCenterPkgList.contains(pkgName);
            }
            return contains;
        } catch (Exception e) {
            Log.e(TAG, "Failed to judge isFromControlCenterPkg!");
            e.printStackTrace();
            return true;
        }
    }

    private void addFromControlCenterPkgList(String pkgName) {
        if (pkgName != null && !pkgName.equals("")) {
            synchronized (this.mStartFromControlCenterPkgLock) {
                this.mStartFromControlCenterPkgList.add(pkgName);
            }
        }
    }

    private void removeFromControlCenterPkgList(String pkgName) {
        if (pkgName != null && !pkgName.equals("")) {
            synchronized (this.mStartFromControlCenterPkgLock) {
                this.mStartFromControlCenterPkgList.remove(pkgName);
            }
        }
    }

    public void handleAppFromControlCenter(String pkgName, int uid) {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "handleAppFromControlCenter pkgName " + pkgName + "  uid " + uid);
        }
        addFromControlCenterPkgList(pkgName);
        sendMessage(10, pkgName, 10000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAppFromControlCenterMsg(Message msg) {
        String pkgName = (String) msg.obj;
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "handleAppFromControlCenterMsg pkgName " + pkgName);
        }
        removeFromControlCenterPkgList(pkgName);
    }

    public boolean isAppPhoneCpn(String cpn) {
        boolean contains;
        if (cpn == null || cpn.equals("")) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        try {
            synchronized (this.mAppPhoneCpnListLock) {
                contains = this.mAppPhoneCpnList.contains(cpn);
            }
            return contains;
        } catch (Exception e) {
            Log.e(TAG, "Failed to judge isAppPhoneCpn!");
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
    }

    private void initAppPhoneCpnList(Context context) {
        synchronized (this.mAppPhoneCpnListLock) {
            if (context == null) {
                Log.wtf(TAG, "initAppPhoneCpnList null context that's impossible!!! ");
            }
            if (this.mAppPhoneListType != 2) {
                this.mAppPhoneCpnList.clear();
                if (this.mExpVersion) {
                    this.mAppPhoneCpnList.addAll(this.mDefaultAppPhoneCpnListExp);
                    this.mAppPhoneListType = 1;
                } else {
                    this.mAppPhoneCpnList.addAll(this.mDefaultAppPhoneCpnList);
                    this.mAppPhoneListType = 0;
                }
            }
        }
    }

    private String dumpAppPhoneConfig() {
        StringBuilder sb = new StringBuilder();
        sb.append(" mAppPhoneListType = " + this.mAppPhoneListType);
        sb.append('\n');
        sb.append("  mAppPhoneCpnList :");
        sb.append('\n');
        Iterator<String> it = this.mAppPhoneCpnList.iterator();
        while (it.hasNext()) {
            sb.append("  " + it.next());
            sb.append('\n');
        }
        return sb.toString();
    }

    public void readBackKeyCleanupFilterFile() {
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readBackKeyCleanupFilterFile start");
        }
        File backKeyCleanupFilterFile = new File(BACK_KEY_FILTER_FILE);
        if (!backKeyCleanupFilterFile.exists()) {
            Log.e(TAG, "backKeyCleanupFilterFile isn't exist!");
        }
        List<String> allowList = new ArrayList<>();
        FileReader fr = null;
        try {
            FileReader fr2 = new FileReader(backKeyCleanupFilterFile);
            BufferedReader reader = new BufferedReader(fr2);
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    try {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (!TextUtils.isEmpty(line)) {
                    allowList.add(line.trim());
                }
            }
            fr2.close();
            if (0 == 0) {
                synchronized (this.mBackKeyCleanupFilterListLock) {
                    this.mBackKeyFilterList.clear();
                    this.mBackKeyFilterList.addAll(allowList);
                }
            }
        } catch (Exception e2) {
            try {
                e2.printStackTrace();
                if (0 != 0) {
                    try {
                        fr.close();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
                if (1 == 0) {
                    synchronized (this.mBackKeyCleanupFilterListLock) {
                        this.mBackKeyFilterList.clear();
                        this.mBackKeyFilterList.addAll(allowList);
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        fr.close();
                    } catch (Exception e4) {
                        e4.printStackTrace();
                    }
                }
                if (1 == 0) {
                    synchronized (this.mBackKeyCleanupFilterListLock) {
                        this.mBackKeyFilterList.clear();
                        this.mBackKeyFilterList.addAll(allowList);
                    }
                }
                throw th;
            }
        }
    }

    public void readAccountSyncWhiteListFile() {
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readAccountSyncWhiteListFile start");
        }
        File accountSyncWhiteListFile = new File(ACCOUNTSYNC_MANGER_FILE);
        if (!accountSyncWhiteListFile.exists()) {
            Log.e(TAG, "accountSyncWhiteListFile isn't exist!");
        }
        List<String> allowList = new ArrayList<>();
        FileReader fr = null;
        try {
            FileReader fr2 = new FileReader(accountSyncWhiteListFile);
            BufferedReader reader = new BufferedReader(fr2);
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    try {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (!TextUtils.isEmpty(line)) {
                    allowList.add(line.trim());
                }
            }
            fr2.close();
            if (0 == 0) {
                synchronized (this.mAccountSyncWhiteListLock) {
                    this.mAccountSyncWhiteList.clear();
                    this.mAccountSyncWhiteList.addAll(allowList);
                }
            }
        } catch (Exception e2) {
            try {
                e2.printStackTrace();
                if (0 != 0) {
                    try {
                        fr.close();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
                if (1 == 0) {
                    synchronized (this.mAccountSyncWhiteListLock) {
                        this.mAccountSyncWhiteList.clear();
                        this.mAccountSyncWhiteList.addAll(allowList);
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        fr.close();
                    } catch (Exception e4) {
                        e4.printStackTrace();
                    }
                }
                if (1 == 0) {
                    synchronized (this.mAccountSyncWhiteListLock) {
                        this.mAccountSyncWhiteList.clear();
                        this.mAccountSyncWhiteList.addAll(allowList);
                    }
                }
                throw th;
            }
        }
    }

    public List<String> getAccountSyncWhiteList() {
        List<String> list;
        synchronized (this.mAccountSyncWhiteListLock) {
            list = this.mAccountSyncWhiteList;
        }
        return list;
    }

    public boolean isInAccountSyncWhiteList(String pkgName, int userId) {
        boolean result;
        synchronized (this.mAccountSyncWhiteListLock) {
            if (!this.mAccountSyncWhiteList.contains(pkgName) && !this.mDefaultAccountSyncWhiteList.contains(pkgName)) {
                if (!isInAutoBootWhiteList(pkgName, userId)) {
                    result = DEFAULT_BACK_KEY_SWITCH_EXP;
                }
            }
            result = true;
        }
        return result;
    }

    private void readCustomAppListFile() {
        int type;
        String value;
        File file = new File(CUSTOMIZE_APP_PATH);
        if (!file.exists()) {
            Log.w(TAG, "/system/etc/oppo_customize_whitelist.xml file don't exist!");
            return;
        }
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName()) && (value = parser.getAttributeValue(null, "att")) != null) {
                    this.mCustomAppList.add(value);
                    Log.i(TAG, "add custom list : " + value);
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e2) {
            Log.w(TAG, "failed parsing ", e2);
            if (0 != 0) {
                stream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
    }

    public List<String> getCustomWhiteList() {
        List<String> list;
        synchronized (mCustomLock) {
            list = this.mCustomAppList;
        }
        return list;
    }

    public boolean isInCustomWhiteList(String pkgName) {
        boolean result;
        synchronized (mCustomLock) {
            result = this.mCustomAppList.contains(pkgName);
        }
        return result;
    }

    /* access modifiers changed from: private */
    public class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8) {
                if (this.mFocusPath.equals(ColorListManagerImpl.BROWSER_WHITE_LIST_FILE)) {
                    Log.i(ColorListManagerImpl.TAG, "focusPath BROWSER_WHITE_LIST_FILE!");
                    ColorListManagerImpl.this.readBrowserWhiteListFile();
                }
                if (this.mFocusPath.equals(ColorListManagerImpl.SYSTEM_CONFIG_LIST_FILE)) {
                    Log.i(ColorListManagerImpl.TAG, "/data/oppo/coloros/config/systemConfigList.xml changed!");
                    synchronized (ColorListManagerImpl.this.mSystemConfigListLock) {
                        ColorListManagerImpl.this.readSystemConfigListLocked();
                    }
                }
                if (this.mFocusPath.equals(ColorListManagerImpl.OPPO_ROM_BLACK_LIST_FILE)) {
                    Log.i(ColorListManagerImpl.TAG, "/data/oppo/coloros/startup/sys_rom_black_list.xml changed!");
                    synchronized (ColorListManagerImpl.mSFLock) {
                        ColorListManagerImpl.this.readRomListFileLocked();
                    }
                }
                if (this.mFocusPath.equals(ColorListManagerImpl.BACK_KEY_FILTER_FILE)) {
                    Log.i(ColorListManagerImpl.TAG, "focusPath BACK_KEY_FILTER_FILE!");
                    ColorListManagerImpl.this.readBackKeyCleanupFilterFile();
                }
                if (this.mFocusPath.equals(ColorListManagerImpl.ACCOUNTSYNC_MANGER_FILE)) {
                    Log.i(ColorListManagerImpl.TAG, "focusPath ACCOUNTSYNC_MANGER_FILE!");
                    ColorListManagerImpl.this.readAccountSyncWhiteListFile();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLoadSystemAppMsg() {
        HashSet<String> systemAppList = new HashSet<>();
        Context context = this.mContext;
        if (!(context == null || context.getPackageManager() == null)) {
            List<ApplicationInfo> infos = this.mContext.getPackageManager().getInstalledApplications(8192);
            if (infos != null) {
                for (ApplicationInfo app : infos) {
                    if ((app.flags & 1) != 0 || (app.flags & ColorHansRestriction.HANS_RESTRICTION_BLOCK_BINDER) != 0) {
                        systemAppList.add(app.packageName);
                    }
                }
            }
            synchronized (this.mSystemAppListLock) {
                this.mSystemAppList.clear();
                this.mSystemAppList.addAll(systemAppList);
            }
        }
    }

    public boolean isSystemApp(String packageName) {
        boolean result = DEFAULT_BACK_KEY_SWITCH_EXP;
        if (TextUtils.isEmpty(packageName)) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        if (this.mSystemAppList.size() <= 0) {
            return isBuildApp(packageName);
        }
        synchronized (this.mSystemAppListLock) {
            Iterator<String> it = this.mSystemAppList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                } else if (packageName.equals(it.next())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isOppoApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        if (packageName.startsWith("com.oppo.") || packageName.startsWith("com.coloros.") || packageName.startsWith("com.nearme.")) {
            return true;
        }
        return DEFAULT_BACK_KEY_SWITCH_EXP;
    }

    private boolean isBuildApp(String pkgName) {
        Context context;
        if (TextUtils.isEmpty(pkgName) || (context = this.mContext) == null || context.getPackageManager() == null) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(pkgName, 0);
            if (packageInfo == null || packageInfo.applicationInfo == null) {
                return DEFAULT_BACK_KEY_SWITCH_EXP;
            }
            if ((packageInfo.applicationInfo.flags & 1) == 0 && (packageInfo.applicationInfo.flags & ColorHansRestriction.HANS_RESTRICTION_BLOCK_BINDER) == 0) {
                return DEFAULT_BACK_KEY_SWITCH_EXP;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        } catch (Exception e2) {
            e2.printStackTrace();
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
    }

    public void addMiniProgramShare(String shareAppPkgName, String miniProgramPkgName, String miniProgramSignature) {
        if (!TextUtils.isEmpty(shareAppPkgName) && MINI_PROGRAM_KEY.equals(shareAppPkgName) && !TextUtils.isEmpty(miniProgramPkgName) && !TextUtils.isEmpty(miniProgramSignature)) {
            synchronized (this.mMiniProgramShareMapLock) {
                if (!this.mMiniProgramShareMap.containsKey(miniProgramPkgName)) {
                    if (this.mMiniProgramShareMap.size() > 20) {
                        this.mMiniProgramShareMap.clear();
                    }
                    this.mMiniProgramShareMap.put(miniProgramPkgName, miniProgramSignature);
                }
            }
        }
    }

    public void removeMiniProgramShare(String shareAppPkgName, String miniProgramPkgName, String miniProgramSignature) {
        if (!TextUtils.isEmpty(shareAppPkgName) && MINI_PROGRAM_KEY.equals(shareAppPkgName) && !TextUtils.isEmpty(miniProgramPkgName)) {
            synchronized (this.mMiniProgramShareMapLock) {
                if (this.mMiniProgramShareMap.containsKey(miniProgramPkgName)) {
                    this.mMiniProgramShareMap.remove(miniProgramPkgName);
                }
            }
        }
    }

    public String getMiniProgramSignature(String pkgName) {
        String signature = null;
        if (TextUtils.isEmpty(pkgName)) {
            return null;
        }
        synchronized (this.mMiniProgramShareMapLock) {
            if (this.mMiniProgramShareMap.containsKey(pkgName)) {
                signature = this.mMiniProgramShareMap.get(pkgName);
            }
        }
        return signature;
    }

    public void addFastAppWechatPay(String originAppCpn, String fastAppCpn) {
        if (!TextUtils.isEmpty(originAppCpn) && !TextUtils.isEmpty(fastAppCpn)) {
            synchronized (this.mFastAppWechatPayMapLock) {
                if (!this.mFastAppWechatMap.containsKey(originAppCpn)) {
                    if (this.mFastAppWechatMap.size() > 20) {
                        this.mFastAppWechatMap.clear();
                    }
                    this.mFastAppWechatMap.put(originAppCpn, fastAppCpn);
                }
            }
        }
    }

    public void removeFastAppWechatPay(String originAppCpn, String fastAppCpn) {
        if (!TextUtils.isEmpty(originAppCpn)) {
            synchronized (this.mFastAppWechatPayMapLock) {
                if (this.mFastAppWechatMap.containsKey(originAppCpn)) {
                    this.mFastAppWechatMap.remove(originAppCpn);
                }
            }
        }
    }

    public boolean isFastAppWechatPayCpn(String originAppCpn) {
        boolean result = DEFAULT_BACK_KEY_SWITCH_EXP;
        if (TextUtils.isEmpty(originAppCpn)) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        synchronized (this.mFastAppWechatPayMapLock) {
            if (this.mFastAppWechatMap.containsKey(originAppCpn)) {
                result = true;
            }
        }
        return result;
    }

    public ComponentName replaceFastAppWechatPayCpn(String originAppCpn) {
        ComponentName newCpn = null;
        if (TextUtils.isEmpty(originAppCpn)) {
            return null;
        }
        synchronized (this.mFastAppWechatPayMapLock) {
            if (this.mFastAppWechatMap.containsKey(originAppCpn)) {
                newCpn = new ComponentName(FAST_APP_PKG, this.mFastAppWechatMap.get(originAppCpn));
            }
        }
        return newCpn;
    }

    public void addFastAppThirdLogin(String callerPkg, String replacePkg) {
        if (!TextUtils.isEmpty(callerPkg) && !TextUtils.isEmpty(replacePkg)) {
            synchronized (this.mFastAppThirdLoginMapLock) {
                if (!this.mFastAppThirdLoginMap.containsKey(callerPkg)) {
                    if (this.mFastAppThirdLoginMap.size() > 20) {
                        this.mFastAppThirdLoginMap.clear();
                    }
                    this.mFastAppThirdLoginMap.put(callerPkg, replacePkg);
                }
            }
        }
    }

    public void removeFastAppThirdLogin(String callerPkg, String replacePkg) {
        if (!TextUtils.isEmpty(callerPkg)) {
            synchronized (this.mFastAppThirdLoginMapLock) {
                if (this.mFastAppThirdLoginMap.containsKey(callerPkg)) {
                    this.mFastAppThirdLoginMap.remove(callerPkg);
                }
            }
        }
    }

    public boolean isFastAppThirdLoginPkg(String resultPkg) {
        if (!TextUtils.isEmpty(resultPkg) && FAST_APP_PKG.equals(resultPkg)) {
            return true;
        }
        return DEFAULT_BACK_KEY_SWITCH_EXP;
    }

    public String replaceFastAppThirdLoginPkg(String callerPkg) {
        String replacePkg = null;
        if (TextUtils.isEmpty(callerPkg)) {
            return null;
        }
        synchronized (this.mFastAppThirdLoginMapLock) {
            if (this.mFastAppThirdLoginMap.containsKey(callerPkg)) {
                replacePkg = this.mFastAppThirdLoginMap.get(callerPkg);
            }
        }
        return replacePkg;
    }

    private void sendMessage(int what, long delay) {
        Message msg = Message.obtain();
        msg.what = what;
        this.mHandler.sendMessageDelayed(msg, delay);
    }

    private void sendMessage(int what, String packageName, long delay) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = packageName;
        this.mHandler.sendMessageDelayed(msg, delay);
    }

    private class ColorListManagerImplHandler extends Handler {
        public ColorListManagerImplHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                ColorListManagerImpl.this.handleLoadSystemAppMsg();
            } else if (i == 10) {
                ColorListManagerImpl.this.handleAppFromControlCenterMsg(msg);
            } else if (i == 11) {
                ColorListManagerImpl.this.handleAppFromNotityMsg(msg);
            }
        }
    }

    public void dump(String arg) {
        Log.i(TAG, "ColorListManagerImpl dump " + arg);
        if (!DUMP_SYSTEM_APPS_LIST.equals(arg)) {
            DUMP_APP_PHONE_CONFIG.equals(arg);
        }
    }

    public void registerLogModule() {
        try {
            Log.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Log.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Log.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorListManagerImpl.class.getName());
            Log.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }

    public void addBackgroundRestrictedInfo(String callerPkg, List<String> targetPkgList) {
        if (!(TextUtils.isEmpty(callerPkg) || targetPkgList == null)) {
            boolean isAdd = DEFAULT_BACK_KEY_SWITCH_EXP;
            int callingUid = Binder.getCallingUid();
            if (callingUid != 1000) {
                String[] packages = getPackageList(callingUid);
                if (packages != null) {
                    int i = 0;
                    while (true) {
                        if (i >= packages.length) {
                            break;
                        } else if (callerPkg.equals(packages[i])) {
                            isAdd = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                }
            } else {
                isAdd = true;
            }
            if (isAdd) {
                List<String> addWhiteList = new ArrayList<>();
                synchronized (this.mAppServicesRestrictedControllist) {
                    List<String> controllist = this.mAppServicesRestrictedControllist.get(callerPkg);
                    if (controllist != null) {
                        for (String pkg : targetPkgList) {
                            if (!TextUtils.isEmpty(pkg) && controllist.contains(pkg)) {
                                addWhiteList.add(pkg);
                            }
                        }
                    }
                }
                addBackgroundRestrictedWhitelist(addWhiteList);
                if (this.DEBUG_SWITCH) {
                    Log.d(TAG, "addBackgroundRestrictedInfo " + callerPkg + " : " + addWhiteList);
                }
                OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).handleMonitorRestrictedBackgroundWhitelist(callerPkg, addWhiteList);
            }
        }
    }

    public boolean isOnBackgroundServiceWhitelist(String packageName) {
        boolean result = DEFAULT_BACK_KEY_SWITCH_EXP;
        if (!TextUtils.isEmpty(packageName)) {
            synchronized (this.mAppServicesRestrictedProtectlist) {
                if (this.mAppServicesRestrictedProtectlist.contains(packageName)) {
                    result = true;
                }
            }
            if (!result) {
                synchronized (this.mAppServicesRestrictedWhitelist) {
                    result = this.mAppServicesRestrictedWhitelist.get(packageName) != null ? true : DEFAULT_BACK_KEY_SWITCH_EXP;
                }
            }
            if (this.DEBUG_SWITCH && result) {
                Log.d(TAG, packageName + " on oppo background whitelist; not restricted in background");
            }
        }
        return result;
    }

    public boolean isAllowBackgroundBroadcastAction(String action, String pkgName) {
        List<String> pkgList;
        boolean isAllow = DEFAULT_BACK_KEY_SWITCH_EXP;
        if (!TextUtils.isEmpty(action) && !TextUtils.isEmpty(pkgName)) {
            synchronized (this.mAppBroadcastRestrictedWhitelist) {
                if (this.mAppBroadcastRestrictedWhitelist.containsKey(action) && (pkgList = this.mAppBroadcastRestrictedWhitelist.get(action)) != null && pkgList.contains(pkgName)) {
                    isAllow = true;
                }
            }
        }
        if (this.DEBUG_SWITCH && isAllow) {
            Log.d(TAG, action + " " + pkgName + " skip background execution");
        }
        return isAllow;
    }

    public boolean isSkipBroadcastFlagRestricted(int callingUid, String callerPackage, ApplicationInfo info) {
        return DEFAULT_BACK_KEY_SWITCH_EXP;
    }

    private String[] getPackageList(int uid) {
        try {
            if (this.mContext == null || this.mContext.getPackageManager() == null) {
                return null;
            }
            return this.mContext.getPackageManager().getPackagesForUid(uid);
        } catch (Exception e) {
            return null;
        }
    }

    private void addBackgroundRestrictedWhitelist(List<String> addList) {
        synchronized (this.mAppServicesRestrictedWhitelist) {
            for (String targetPkg : addList) {
                this.mAppServicesRestrictedWhitelist.put(targetPkg, targetPkg);
            }
        }
    }

    private ArrayMap<String, List<String>> parseAppRestrictedText(String contentText) {
        String[] values;
        ArrayMap<String, List<String>> whiteListMap = new ArrayMap<>();
        List<String> whiteList = new ArrayList<>();
        String callingPkg = "";
        if (!TextUtils.isEmpty(contentText) && (values = contentText.split("#")) != null) {
            for (int i = 0; i < values.length; i++) {
                if (i == 0) {
                    callingPkg = values[0].trim();
                } else {
                    whiteList.add(values[i].trim());
                }
            }
        }
        if (!TextUtils.isEmpty(callingPkg) && !whiteList.isEmpty()) {
            whiteListMap.put(callingPkg, whiteList);
        }
        return whiteListMap;
    }

    private void parseLocalWhitelist(String type) {
        if (APP_SERVICE_RESTRICT_CONTROL_LIST.equals(type)) {
            ArrayMap<String, List<String>> appRestrictedWhiteList = parseKeyValusText(mAppRestrictedServiceControlList);
            if (!appRestrictedWhiteList.isEmpty()) {
                this.mAppServicesRestrictedControllist.putAll((ArrayMap<? extends String, ? extends List<String>>) appRestrictedWhiteList);
            }
        } else if (APP_BROADCAST_RESTRICT_WHITE_LIST.equals(type)) {
            ArrayMap<String, List<String>> appRestrictedWhiteList2 = parseKeyValusText(mAppRestrictedBroadcastList);
            if (!appRestrictedWhiteList2.isEmpty()) {
                this.mAppBroadcastRestrictedWhitelist.putAll((ArrayMap<? extends String, ? extends List<String>>) appRestrictedWhiteList2);
            }
        }
    }

    private ArrayMap<String, List<String>> parseKeyValusText(List<String> list) {
        ArrayMap<String, List<String>> appRestrictedList = new ArrayMap<>();
        for (String contentText : list) {
            ArrayMap<String, List<String>> newMap = parseAppRestrictedText(contentText);
            for (Map.Entry<String, List<String>> entry : newMap.entrySet()) {
                String callingPkg = entry.getKey();
                if (!TextUtils.isEmpty(callingPkg)) {
                    if (appRestrictedList.containsKey(callingPkg)) {
                        List<String> newList = entry.getValue();
                        newList.addAll(appRestrictedList.get(callingPkg));
                        appRestrictedList.put(callingPkg, newList);
                    } else {
                        appRestrictedList.putAll((ArrayMap<? extends String, ? extends List<String>>) newMap);
                    }
                }
            }
        }
        return appRestrictedList;
    }

    public boolean isExpVersion() {
        return this.mExpVersion;
    }

    public boolean isInBootSmartWhiteList(String pkgName) {
        ArrayList<String> bootSmartWhiteList;
        Bundle data = getConfigInfo(BOOT_SMART_WHITE_LIST);
        if (data == null || (bootSmartWhiteList = data.getStringArrayList(BOOT_SMART_WHITE_LIST)) == null || !bootSmartWhiteList.contains(pkgName)) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        if (!this.DEBUG_SWITCH) {
            return true;
        }
        Log.i(TAG, pkgName + " in boot smart white list");
        return true;
    }

    public boolean putConfigInfo(String configName, Bundle bundle) {
        return putConfigInfo(configName, bundle, 0);
    }

    /* JADX DEBUG: TODO: convert one arg to string using `String.valueOf()`, args: [('u' char), (r7v0 'userId' int A[D('userId' int)])] */
    public boolean putConfigInfo(String configName, Bundle bundle, int userId) {
        if (TextUtils.isEmpty(configName)) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        if (configName.equals("hans_config")) {
            return ColorHansManager.getInstance().updateHansConfig(bundle);
        }
        if (configName.equals("SPS_list")) {
            return ColorAppStartupManager.getInstance().updateSPSPolicy(bundle);
        }
        StringBuilder sb = new StringBuilder();
        sb.append('u');
        sb.append(userId);
        String user = sb.toString();
        synchronized (this.mCommonConfigMap) {
            ArrayMap<String, Bundle> bundleMap = this.mCommonConfigMap.get(user);
            if (bundleMap == null) {
                bundleMap = new ArrayMap<>();
            }
            bundleMap.put(configName, bundle);
            this.mCommonConfigMap.put(user, bundleMap);
        }
        return true;
    }

    public Bundle getConfigInfo(String configName) {
        return getConfigInfo(configName, 0);
    }

    /* JADX DEBUG: TODO: convert one arg to string using `String.valueOf()`, args: [('u' char), (r6v0 'userId' int A[D('userId' int)])] */
    public Bundle getConfigInfo(String configName, int userId) {
        if (TextUtils.isEmpty(configName)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append('u');
        sb.append(userId);
        String user = sb.toString();
        synchronized (this.mCommonConfigMap) {
            ArrayMap<String, Bundle> bundleMap = this.mCommonConfigMap.get(user);
            if (bundleMap == null) {
                return null;
            }
            return bundleMap.get(configName);
        }
    }

    /* access modifiers changed from: private */
    /* JADX DEBUG: TODO: convert one arg to string using `String.valueOf()`, args: [('u' char), (r4v0 'userId' int A[D('userId' int)])] */
    /* access modifiers changed from: public */
    private void removeCommonConfigInfo(int userId) {
        StringBuilder sb = new StringBuilder();
        sb.append('u');
        sb.append(userId);
        String user = sb.toString();
        synchronized (this.mCommonConfigMap) {
            this.mCommonConfigMap.remove(user);
        }
    }

    private void registerMultiUserMonitor() {
        if (this.mContext == null) {
            Log.e(TAG, "registerMultiUserMonitor error!");
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_STOPPED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.ColorListManagerImpl.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                int userId;
                String action = intent.getAction();
                if (action != null && "android.intent.action.USER_STOPPED".equals(action) && (userId = intent.getIntExtra("android.intent.extra.user_handle", -1)) > 0) {
                    Log.i(ColorListManagerImpl.TAG, "stopped user " + userId);
                    ColorListManagerImpl.this.removeCommonConfigInfo(userId);
                }
            }
        }, UserHandle.ALL, filter, null, this.mHandler);
    }

    public ArrayList<String> getQuickRestartProcList() {
        synchronized (mQuickRestartProcList) {
            if (!mQuickRestartProcList.isEmpty()) {
                return new ArrayList<>(mQuickRestartProcList);
            }
            return new ArrayList<>(mDefaultQuickRestartProcList);
        }
    }

    public ArrayList<String> getAmsEmptyKillFilterList() {
        ArrayList<String> customizeEmptyProcProtectList;
        synchronized (mEmptyProcProtectList) {
            customizeEmptyProcProtectList = new ArrayList<>();
            if (!(this.mContext == null || this.mContext.getPackageManager() == null)) {
                if (this.mContext.getPackageManager().hasSystemFeature("coloros.customize.guardelf.jp.kddi.keepalive")) {
                    customizeEmptyProcProtectList.addAll(mKddiProtectList);
                }
                if (this.mContext.getPackageManager().hasSystemFeature("coloros.customize.guardelf.ymobile.keepalive")) {
                    customizeEmptyProcProtectList.addAll(mYMProtectList);
                }
                if (this.mContext.getPackageManager().hasSystemFeature("coloros.customize.guardelf.jp.uqmobile.keepalive")) {
                    customizeEmptyProcProtectList.addAll(mUqProtectList);
                }
                if (this.mContext.getPackageManager().hasSystemFeature("coloros.customize.guardelf.jp.softbank.keepalive")) {
                    customizeEmptyProcProtectList.addAll(mSoftbankProtectList);
                }
            }
            if (!mEmptyProcProtectList.isEmpty()) {
                customizeEmptyProcProtectList.addAll(mEmptyProcProtectList);
            } else {
                customizeEmptyProcProtectList.addAll(mDefaultEmptyProcProtectList);
            }
        }
        return customizeEmptyProcProtectList;
    }

    public ArrayList<String> getAmsEmptyKillBootUpFilterList() {
        synchronized (mEmptyProcBootupProtectList) {
            if (!mEmptyProcBootupProtectList.isEmpty()) {
                return new ArrayList<>(mEmptyProcBootupProtectList);
            }
            return new ArrayList<>(mDefaultEmptyProcBootupProtectList);
        }
    }

    public boolean isSystemDumpHeapEnable() {
        boolean isDebuggable = "1".equals(SystemProperties.get("ro.debuggable", "0"));
        boolean agingTestVersion = "1".equals(SystemProperties.get("SPECIAL_OPPO_CONFIG", "0"));
        if (isDebuggable || agingTestVersion || this.mSystemDumpHeapEnable) {
            return true;
        }
        return DEFAULT_BACK_KEY_SWITCH_EXP;
    }

    public void putCurLiveWallpaper(String packageName, int userId) {
        synchronized (this.mCurLiveWallpaperMap) {
            this.mCurLiveWallpaperMap.put(Integer.valueOf(userId), packageName);
        }
    }

    public boolean isLiveWallpaper(String packageName, int userId) {
        if (TextUtils.isEmpty(packageName)) {
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
        synchronized (this.mCurLiveWallpaperMap) {
            if (packageName.equals(this.mCurLiveWallpaperMap.get(Integer.valueOf(userId)))) {
                return true;
            }
            return DEFAULT_BACK_KEY_SWITCH_EXP;
        }
    }

    public boolean isCustomizeAmsCleanupEnable() {
        return this.mCustomizeAmsCleanupSwitch;
    }
}
