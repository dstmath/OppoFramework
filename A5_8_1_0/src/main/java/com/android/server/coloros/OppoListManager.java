package com.android.server.coloros;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.util.Xml;
import com.android.server.OppoBPMHelper;
import com.android.server.OppoBPMUtils;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.am.OppoFreeFormManagerService;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoListManager {
    private static final String ACCOUNTSYNC_MANGER_FILE = "/data/oppo/coloros/startup/autoSyncWhiteList.txt";
    private static final String ACCOUNTSYNC_MANGER_PATH = "/data/oppo/coloros/startup";
    private static final String ALLOW_MANIFEST_NET_BRO_TAG = "AllowManifestNetBro";
    private static final String APP_PHONE_REFUSE_CPN = "appPhoneRefuseCpn";
    private static final String AUTOBOOT_MANGER_FILE = "/data/oppo/coloros/startup/bootallow.txt";
    private static final String AUTOBOOT_MANGER_PATH = "/data/oppo/coloros/startup";
    private static final String BACK_CLIP_INTERCEPT_WHITE_LIST = "BackClipInterceptWhiteList";
    private static final String BACK_KEY_FILTER_FILE = "/data/oppo/coloros/config/bkfilters.txt";
    private static final String BACK_KEY_FILTER_LIST = "BackKeyFilterList";
    private static final String BACK_KEY_FILTER_PATH = "/data/oppo/coloros/config/";
    private static final String BACK_KEY_KILL_SWITCH = "BackKeyKillSwitch";
    private static final String BROWSER_WHITE_LIST_FILE = "/data/oppo/coloros/startup/browserWhiteList.txt";
    private static final String CUSTOMIZE_APP_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final boolean DEFAULT_BACK_KEY_SWITCH = true;
    private static final boolean DEFAULT_BACK_KEY_SWITCH_EXP = false;
    private static final String DEFAULT_MAX_VERSION_CODE = "max";
    private static final int DEFAULT_MIN_VERSION_CODE = 0;
    private static final int DEFAULT_MIN_VERSION_CODE_LIST_LENGTH = 2;
    private static final long DEFAULT_PROTECT_SELF_TIMEOUT = 7200000;
    private static final String DUMP_FORBID_LIST = "forbidList";
    private static final String DUMP_SYSTEM_APPS_LIST = "systemAppList";
    private static final String EVENTID_ADD_PROTECT_INFO = "self_protect_add";
    private static final String EVENTID_REMOVE_PROTECT_INFO = "self_protect_remove";
    private static final String EVENTID_TIMEOUT_PROTECT_INFO = "self_protect_timeout";
    private static final String FAST_APP_PKG = "com.nearme.instant.platform";
    private static final ArrayList<String> FILTER_SEPCIAL_SECURE_APP = new ArrayList();
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
    private static final String REMOVE_TASK_FILTER_PKG_LIST = "RemoveTaskFilterPkgNew";
    private static final String REMOVE_TASK_FILTER_PROCESS_LIST = "RemoveTaskFilterProcessNew";
    private static final String SECUREPAY_ACTIVITY_TAG = "SecurePayActivity";
    private static final String SYSTEM_CONFIG_LIST_FILE = "/data/oppo/coloros/config/systemConfigList.xml";
    private static final String TAG = "OppoListManager";
    private static final String TAG_DO_CLEAR_REDUNDENT_TASK = "ClearRedundentTaskSwitch";
    private static final String TAG_PREVENT_REDUNDENT_START = "PreventRedundentStart";
    private static final String TAG_REDUNDENT_TASK_CLASS = "RedundentTaskClass";
    private static final Object mCustomLock = new Object();
    private static final List<String> mDefaultBackClipInterceptWhiteList = Arrays.asList(new String[]{"com.tencent.mobileqq", "com.tencent.mm"});
    private static final List<String> mDefaultBackClipInterceptWhiteListExp = Arrays.asList(new String[]{"com.tencent.mobileqq", "com.tencent.mm"});
    private static final List<String> mDefaultBackKeyFilterList = Arrays.asList(new String[]{"com.tencent.mobileqq", "com.tencent.mm", "com.oppo.im"});
    private static final List<String> mDefaultBackKeyFilterListExp = Arrays.asList(new String[]{"com.tencent.mobileqq", "com.tencent.mm", "com.oppo.im"});
    private static final List<String> mDefaultCmccPkgList = Arrays.asList(new String[]{"com.greenpoint.android.mc10086.activity"});
    private static final List<String> mDefaultCmccProcessList = Arrays.asList(new String[]{"com.greenpoint.android.mc10086.activity:McPushservice"});
    private static final List<String> mDefaultJobScheduleTimeoutWhiteList = Arrays.asList(new String[]{"com.coloros.gallery3d", "com.android.providers.media", "com.coloros.cloud", "com.oppo.ota", "com.coloros.assistantscreen", "com.android.providers.downloads"});
    private static final List<String> mDefaultKillRestartServiceProList = Arrays.asList(new String[]{"com.tencent.mobileqq", "com.tencent.mm"});
    private static final List<String> mDefaultKillRestartServiceProListExp = Arrays.asList(new String[]{"com.tencent.mm", "com.tencent.mobileqq", "com.zing.zalo", "com.facebook.orca", "com.facebook.katana", "com.instagram.android", "jp.naver.line.android", "com.whatsapp", "com.bbm", "com.skype.raider", "com.viber.voip", "com.path", "com.facebook.lite", "com.truecaller", "com.bsb.hike", "com.snapchat.android", "com.twitter.android", "com.imo.android.imoim", "com.google.android.gm"});
    private static final List<String> mDefaultProtectSelfWhiteList = Arrays.asList(new String[]{"com.coloros.alarmclock", "com.coloros.backuprestore", "com.coloros.backuprestore.remoteservice", "com.redteamobile.roaming", "com.coloros.screenshot", "com.oppo.ctautoregist", "com.android.keychain", "com.coloros.safesdkproxy", "com.cleanmaster.sdk", "com.coloros.oshare", "com.coloros.filemanager", "com.coloros.gallery3d", "com.coloros.safecenter", "com.oppo.customize.service", "com.coloros.blacklist", "com.coloros.healthcheck", "com.android.contacts", "com.android.mms", "com.coloros.weather.service", "com.coloros.blacklistapp", ActivityManagerService.OPPO_LAUNCHER, "com.android.settings", OppoFreeFormManagerService.FREEFORM_CALLER_PKG, "com.coloros.gesture", "com.coloros.notificationmanager", "com.coloros.simsettings", "com.coloros.wifibackuprestore", "com.ted.number", "com.android.calendar", "com.coloros.providers.fileinfo", "com.oppo.camera", "com.nearme.sync", "com.nearme.ocloud", "com.oppo.ota", "com.coloros.cloud", "com.coloros.oppoguardelf", "com.coloros.screenrecorder", "com.android.cellbroadcastreceiver", "com.oppo.videocallfloat", "com.redteamobile.oppo.roaming", "com.coloros.smartdrive", "com.coloros.oppopods", "com.coloros.wirelesssettings", "com.coloros.oppopods", "com.coloros.translate"});
    private static final List<String> mDefaultRedundentTaskClassList = Arrays.asList(new String[]{"com.tencent.mm.plugin.appbrand.ui.AppBrandUI"});
    private static final List<String> mDefaultRemoveTaskFilterPkgList = Arrays.asList(new String[0]);
    private static final List<String> mDefaultRemoveTaskFilterProListExp = Arrays.asList(new String[0]);
    private static final List<String> mDefaultRemoveTaskFilterProcessList = Arrays.asList(new String[]{"com.coloros.safecenter:clear_filter", "com.redteamobile.virtual.softsim"});
    private static final List<String> mProtectFromPkgWhiteList = Arrays.asList(new String[]{"OppoCustomizeService"});
    private static final Object mSFLock = new Object();
    private static OppoListManager sOppoListManager = null;
    private boolean DEBUG_SWITCH = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private List<String> mAccountSyncWhiteList = new ArrayList();
    private FileObserverPolicy mAccountSyncWhiteListFileObserver = null;
    private final Object mAccountSyncWhiteListLock = new Object();
    private List<String> mAllowManifestNetBroList = new ArrayList();
    private List<String> mAppPhoneCpnList = new ArrayList();
    private final Object mAppPhoneCpnListLock = new Object();
    private FileObserverPolicy mAutoBootFileObserver = null;
    private List<String> mAutoBootWhiteList = new ArrayList();
    private final Object mAutoBootWhiteListLock = new Object();
    private List<String> mBackClipInterceptWhiteList = new ArrayList();
    private FileObserverPolicy mBackKeyCleanupFilterFileObserver = null;
    private final Object mBackKeyCleanupFilterListLock = new Object();
    private List<String> mBackKeyFilterList = new ArrayList();
    private boolean mBackKeyInitFromFile = false;
    private boolean mBackKeyKillSwitch = true;
    private FileObserverPolicy mBrowserInterceptFileObserver = null;
    private List<String> mBrowserWhiteList = new ArrayList();
    private final Object mBrowserWhiteListLock = new Object();
    private Context mContext;
    private List<String> mCustomAppList = new ArrayList();
    private List<String> mDefaultAccountSyncWhiteList = Arrays.asList(new String[]{"com.android.email", "com.google.android.gm"});
    private List<String> mDefaultAllowManifestNetBroList = Arrays.asList(new String[]{"com.nearme.romupdate", "com.coloros.sau", "com.coloros.sauhelper", "com.oppo.ota", "com.nearme.statistics.rom"});
    private List<String> mDefaultAppPhoneCpnList = Arrays.asList(new String[]{"com.tencent.mm.plugin.voip.ui.VideoActivity", "com.tencent.av.ui.VideoInviteFull", "com.tencent.av.ui.AVActivity", "com.tencent.av.ui.VideoInviteLock", "com.facebook.rtc.activities.WebrtcIncallFragmentHostActivity"});
    private List<String> mDefaultNotiServiceAppList = Arrays.asList(new String[]{"com.tencent.mm", "com.tencent.research.drop", "com.letv.android.client", "com.qiyi.video", "com.sohu.sohuvideo", "com.tencent.qqlive"});
    private List<String> mDefaultProtectForeList = Arrays.asList(new String[]{"com.tencent.mm", "com.tencent.mobileqq", "com.tencent.tmgp.sgame"});
    private List<String> mDefaultProtectForeNetList = Arrays.asList(new String[]{"com.tencent.tmgp.sgame"});
    private List<String> mDefaultSecurePayActivityList = Arrays.asList(new String[]{"com.tencent.mm.plugin.wallet.pay.ui.WalletPayUI", "com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI"});
    private List<String> mDefaultWhiteList = Arrays.asList(new String[]{"com.sohu.inputmethod.sogou", "com.oppo.mp_battery_autotest", "com.oppo.autotest.agingautotesttool", "com.oppo.PhenixTestServer", "com.oppo.networkautotest", "com.oppo.community", "com.nearme.note", "com.tencent.tvoem", "com.oppo.ubeauty", "com.facebook.orca", "com.android.email"});
    private boolean mDoClearRedundentSwitch = true;
    private boolean mDoPreventRedundentStartSwitch = false;
    private boolean mExpVersion = false;
    private ArrayMap<String, String> mFastAppWechatMap = new ArrayMap();
    private final Object mFastAppWechatPayMapLock = new Object();
    private OppoFloatWindowListManager mFloatWindowListManager;
    private List<String> mGlobalCmccPkgList = new ArrayList();
    private List<String> mGlobalCmccProcessList = new ArrayList();
    private Handler mHandler = null;
    private List<String> mJobScheduleTimeoutWhiteList = new ArrayList();
    private List<String> mKillRestartServiceProList = new ArrayList();
    private ArrayMap<String, String> mMiniProgramShareMap = new ArrayMap();
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
    private ArrayMap<String, List<String>> mStartForbiddenList = new ArrayMap();
    private FileObserverPolicy mStartForbiddenObserver = null;
    private List<String> mStartFromControlCenterPkgList = new ArrayList();
    private final Object mStartFromControlCenterPkgLock = new Object();
    private List<String> mStartFromNotifyPkgList = new ArrayList();
    private final Object mStartFromNotifyPkgLock = new Object();
    private HashSet<String> mSystemAppList = new HashSet(300);
    private final Object mSystemAppListLock = new Object();
    private FileObserverPolicy mSystemConfigFileObserver = null;
    private final Object mSystemConfigListLock = new Object();
    private boolean mUploadSelfProtectSwitch = true;

    private class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8) {
                if (this.mFocusPath.equals(OppoListManager.AUTOBOOT_MANGER_FILE)) {
                    Log.i(OppoListManager.TAG, "focusPath AUTOBOOT_MANGER_FILE!");
                    OppoListManager.this.readAutoBootListFile();
                }
                if (this.mFocusPath.equals(OppoListManager.BROWSER_WHITE_LIST_FILE)) {
                    Log.i(OppoListManager.TAG, "focusPath BROWSER_WHITE_LIST_FILE!");
                    OppoListManager.this.readBrowserWhiteListFile();
                }
                if (this.mFocusPath.equals(OppoListManager.SYSTEM_CONFIG_LIST_FILE)) {
                    Log.i(OppoListManager.TAG, "/data/oppo/coloros/config/systemConfigList.xml changed!");
                    synchronized (OppoListManager.this.mSystemConfigListLock) {
                        OppoListManager.this.readSystemConfigListLocked();
                    }
                }
                if (this.mFocusPath.equals(OppoListManager.OPPO_ROM_BLACK_LIST_FILE)) {
                    Log.i(OppoListManager.TAG, "/data/oppo/coloros/startup/sys_rom_black_list.xml changed!");
                    synchronized (OppoListManager.mSFLock) {
                        OppoListManager.this.readRomListFileLocked();
                    }
                }
                if (this.mFocusPath.equals(OppoListManager.BACK_KEY_FILTER_FILE)) {
                    Log.i(OppoListManager.TAG, "focusPath BACK_KEY_FILTER_FILE!");
                    OppoListManager.this.readBackKeyCleanupFilterFile();
                }
                if (this.mFocusPath.equals(OppoListManager.ACCOUNTSYNC_MANGER_FILE)) {
                    Log.i(OppoListManager.TAG, "focusPath ACCOUNTSYNC_MANGER_FILE!");
                    OppoListManager.this.readAccountSyncWhiteListFile();
                }
            }
        }
    }

    private class OppoListManagerHandler extends Handler {
        public OppoListManagerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    OppoListManager.this.handleLoadSystemAppMsg();
                    return;
                case 10:
                    OppoListManager.this.handleAppFromControlCenterMsg(msg);
                    return;
                case 11:
                    OppoListManager.this.handleAppFromNotityMsg(msg);
                    return;
                default:
                    return;
            }
        }
    }

    class ProtectSelfInfo {
        String mFromPkg;
        String mPackageName;
        long mStartTime;
        long mTimeout;

        ProtectSelfInfo() {
        }
    }

    static {
        FILTER_SEPCIAL_SECURE_APP.add("com.eg.android.AlipayGphone");
        FILTER_SEPCIAL_SECURE_APP.add("com.tencent.mm");
    }

    public OppoListManager() {
        initDir();
        initFileObserver();
        readAutoBootListFile();
        readBrowserWhiteListFile();
        initAppPhoneCpnList();
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

    public static OppoListManager getInstance() {
        if (sOppoListManager == null) {
            sOppoListManager = new OppoListManager();
        }
        return sOppoListManager;
    }

    public void init() {
    }

    public void initCtx(Context context) {
        Log.i(TAG, "initCtx");
        this.mContext = context;
        this.mExpVersion = context.getPackageManager().hasSystemFeature("oppo.version.exp");
        if (!this.mBackKeyInitFromFile) {
            if (this.mExpVersion) {
                this.mBackKeyKillSwitch = false;
            } else {
                this.mBackKeyKillSwitch = true;
            }
        }
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new OppoListManagerHandler(thread.getLooper());
        sendMessage(1, 1000);
        registerLogModule();
        this.mFloatWindowListManager = new OppoFloatWindowListManager(context);
        this.mFloatWindowListManager.initBuildInAppsFloatWindowPermission();
    }

    private void initDir() {
        File autobootPath = new File("/data/oppo/coloros/startup");
        File autobootFile = new File(AUTOBOOT_MANGER_FILE);
        File browserWhiteListFile = new File(BROWSER_WHITE_LIST_FILE);
        File systemConfigListFile = new File(SYSTEM_CONFIG_LIST_FILE);
        File romBlackListFile = new File(OPPO_ROM_BLACK_LIST_FILE);
        File backKeyCleanupFilterPath = new File(BACK_KEY_FILTER_PATH);
        File backKeyCleanupFilterFile = new File(BACK_KEY_FILTER_FILE);
        File accountSyncWhiteListPath = new File("/data/oppo/coloros/startup");
        File accountSyncWhiteListFile = new File(ACCOUNTSYNC_MANGER_FILE);
        try {
            if (!autobootPath.exists()) {
                autobootPath.mkdirs();
            }
            if (!autobootFile.exists()) {
                autobootFile.createNewFile();
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
        if (sourceFile.exists() && (targetFile.exists() ^ 1) == 0) {
            try {
                FileUtils.copyFile(sourceFile, targetFile);
            } catch (Exception e) {
                Log.e(TAG, "copyFile failed!!!");
                e.printStackTrace();
            }
        }
    }

    private void initFileObserver() {
        this.mAutoBootFileObserver = new FileObserverPolicy(AUTOBOOT_MANGER_FILE);
        this.mAutoBootFileObserver.startWatching();
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

    public boolean isAppStartForbidden(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        boolean result = false;
        synchronized (mSFLock) {
            int length = this.mStartForbiddenList.size();
            for (int i = 0; i < length; i++) {
                if (pkgName.equals(this.mStartForbiddenList.keyAt(i))) {
                    List<String> codeList = (List) this.mStartForbiddenList.valueAt(i);
                    if (codeList != null && codeList.size() >= 2) {
                        result = inAppStartForbiddenCodeList(codeList, pkgName);
                    }
                }
            }
        }
        return result;
    }

    private PackageInfo getPackageInfo(String pkgName) {
        PackageInfo packageInfo = null;
        if (this.mContext == null || this.mContext.getPackageManager() == null) {
            return packageInfo;
        }
        try {
            return this.mContext.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return packageInfo;
        } catch (Exception e2) {
            e2.printStackTrace();
            return packageInfo;
        }
    }

    private boolean inAppStartForbiddenCodeList(List<String> codeList, String pkgName) {
        int i = 0;
        while (i < codeList.size() - 1) {
            try {
                if (Integer.parseInt((String) codeList.get(i)) == 0 && ((String) codeList.get(i + 1)).equals(DEFAULT_MAX_VERSION_CODE)) {
                    return true;
                }
                PackageInfo info = getPackageInfo(pkgName);
                if (info != null) {
                    if (Integer.parseInt((String) codeList.get(i)) <= info.versionCode && ((String) codeList.get(i + 1)).equals(DEFAULT_MAX_VERSION_CODE)) {
                        return true;
                    }
                    if (Integer.parseInt((String) codeList.get(i)) <= info.versionCode && info.versionCode <= Integer.parseInt((String) codeList.get(i + 1))) {
                        return true;
                    }
                }
                i++;
            } catch (Exception e) {
                Log.e(TAG, "appstart forbindden exception!", e);
            }
        }
        return false;
    }

    public List<String> getAutoBootWhiteList() {
        List<String> list;
        synchronized (this.mAutoBootWhiteListLock) {
            list = this.mAutoBootWhiteList;
        }
        return list;
    }

    public boolean isInAutoBootWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mAutoBootWhiteListLock) {
            result = this.mAutoBootWhiteList.contains(pkgName);
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0052 A:{SYNTHETIC, Splitter: B:23:0x0052} */
    /* JADX WARNING: Removed duplicated region for block: B:75:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0090 A:{SYNTHETIC, Splitter: B:53:0x0090} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0095  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readAutoBootListFile() {
        Exception e;
        Throwable th;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readAutoBootListFile start");
        }
        File autoBootListFile = new File(AUTOBOOT_MANGER_FILE);
        if (!autoBootListFile.exists()) {
            Log.e(TAG, "autoBootListFile isn't exist!");
        }
        boolean isException = false;
        List<String> allowList = new ArrayList();
        FileReader fr = null;
        try {
            FileReader fr2 = new FileReader(autoBootListFile);
            try {
                BufferedReader reader = new BufferedReader(fr2);
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    } else if (!TextUtils.isEmpty(line)) {
                        allowList.add(line.trim());
                    }
                }
                if (fr2 != null) {
                    try {
                        fr2.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                if (null == null) {
                    synchronized (this.mAutoBootWhiteListLock) {
                        this.mAutoBootWhiteList.clear();
                        this.mAutoBootWhiteList.addAll(allowList);
                    }
                }
            } catch (Exception e3) {
                e2 = e3;
                fr = fr2;
                isException = true;
                try {
                    e2.printStackTrace();
                    if (fr != null) {
                    }
                    if (1 != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fr != null) {
                        try {
                            fr.close();
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (!isException) {
                        synchronized (this.mAutoBootWhiteListLock) {
                            this.mAutoBootWhiteList.clear();
                            this.mAutoBootWhiteList.addAll(allowList);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fr = fr2;
                if (fr != null) {
                }
                if (isException) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e22 = e4;
            isException = true;
            e22.printStackTrace();
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
            if (1 != null) {
                synchronized (this.mAutoBootWhiteListLock) {
                    this.mAutoBootWhiteList.clear();
                    this.mAutoBootWhiteList.addAll(allowList);
                }
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
        boolean result = false;
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
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0052 A:{SYNTHETIC, Splitter: B:23:0x0052} */
    /* JADX WARNING: Removed duplicated region for block: B:75:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0090 A:{SYNTHETIC, Splitter: B:53:0x0090} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0095  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readBrowserWhiteListFile() {
        Exception e;
        Throwable th;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readBrowserWhiteList start");
        }
        File browserWhiteListFile = new File(BROWSER_WHITE_LIST_FILE);
        if (!browserWhiteListFile.exists()) {
            Log.e(TAG, "browserWhiteListFile isn't exist!");
        }
        boolean isException = false;
        List<String> allowList = new ArrayList();
        FileReader fr = null;
        try {
            FileReader fr2 = new FileReader(browserWhiteListFile);
            try {
                BufferedReader reader = new BufferedReader(fr2);
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    } else if (!TextUtils.isEmpty(line)) {
                        allowList.add(line.trim());
                    }
                }
                if (fr2 != null) {
                    try {
                        fr2.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                if (null == null) {
                    synchronized (this.mBrowserWhiteListLock) {
                        this.mBrowserWhiteList.clear();
                        this.mBrowserWhiteList.addAll(allowList);
                    }
                }
            } catch (Exception e3) {
                e2 = e3;
                fr = fr2;
                isException = true;
                try {
                    e2.printStackTrace();
                    if (fr != null) {
                    }
                    if (1 != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fr != null) {
                        try {
                            fr.close();
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (!isException) {
                        synchronized (this.mBrowserWhiteListLock) {
                            this.mBrowserWhiteList.clear();
                            this.mBrowserWhiteList.addAll(allowList);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fr = fr2;
                if (fr != null) {
                }
                if (isException) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e22 = e4;
            isException = true;
            e22.printStackTrace();
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
            if (1 != null) {
                synchronized (this.mBrowserWhiteListLock) {
                    this.mBrowserWhiteList.clear();
                    this.mBrowserWhiteList.addAll(allowList);
                }
            }
        }
    }

    public boolean inProtectForeList(String pkg) {
        if (pkg == null || pkg.equals("")) {
            return false;
        }
        boolean contains;
        synchronized (this.mSystemConfigListLock) {
            contains = getProtectForeList().contains(pkg);
        }
        return contains;
    }

    public boolean inProtectForeNetList(String pkg) {
        if (pkg == null || pkg.equals("")) {
            return false;
        }
        boolean contains;
        synchronized (this.mSystemConfigListLock) {
            contains = getProtectForeNetList().contains(pkg);
        }
        return contains;
    }

    public List<String> getProtectForeList() {
        synchronized (this.mSystemConfigListLock) {
            List<String> list;
            if (this.mProtectForeList == null || this.mProtectForeList.isEmpty()) {
                list = this.mDefaultProtectForeList;
                return list;
            }
            list = this.mProtectForeList;
            return list;
        }
    }

    public List<String> getProtectForeNetList() {
        synchronized (this.mSystemConfigListLock) {
            List<String> list;
            if (this.mProtectForeNetList == null || this.mProtectForeNetList.isEmpty()) {
                list = this.mDefaultProtectForeNetList;
                return list;
            }
            list = this.mProtectForeNetList;
            return list;
        }
    }

    public List<String> getSecurePayActivityList() {
        synchronized (this.mSystemConfigListLock) {
            List<String> list;
            if (this.mSecurePayActivityList == null || this.mSecurePayActivityList.isEmpty()) {
                list = this.mDefaultSecurePayActivityList;
                return list;
            }
            list = this.mSecurePayActivityList;
            return list;
        }
    }

    public List<String> getNotificationServiceList() {
        synchronized (this.mSystemConfigListLock) {
            List<String> list;
            if (this.mNotificationServiceApp == null || this.mNotificationServiceApp.isEmpty()) {
                list = this.mDefaultNotiServiceAppList;
                return list;
            }
            list = this.mNotificationServiceApp;
            return list;
        }
    }

    public ArrayList<String> getGlobalWhiteList(Context context) {
        return getGlobalWhiteList(context, 1);
    }

    public ArrayList<String> getGlobalWhiteList(Context context, int type) {
        ArrayList<String> list = new ArrayList();
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
        ArrayList<String> list = new ArrayList();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "LOOK OUT!! PMS is not ready yet! Can't get white process list!");
            return list;
        }
        if (pm.hasSystemFeature("oppo.system.cmcc.filter")) {
            if (this.mGlobalCmccPkgList == null || this.mGlobalCmccPkgList.isEmpty()) {
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
        ArrayList<String> list = new ArrayList();
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
        ArrayList<String> list = new ArrayList();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Log.e(TAG, "LOOK OUT!! PMS is not ready yet! Can't get white process list!");
            return list;
        }
        if (pm.hasSystemFeature("oppo.system.cmcc.filter")) {
            if (this.mGlobalCmccProcessList == null || this.mGlobalCmccProcessList.isEmpty()) {
                list.addAll(mDefaultCmccProcessList);
            } else {
                list.addAll(this.mGlobalCmccProcessList);
            }
        }
        return list;
    }

    public ArrayList<String> getOppoTestToolList(Context context) {
        ArrayList<String> tempList = new ArrayList();
        tempList.add("com.oppo.qetest");
        tempList.add("com.oppo.qemonitor");
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
                if (pkg != null && (pkg.startsWith("com.oppo.autotest.") || pkg.equals("com.oppo.ScoreAppMonitor"))) {
                    tempList.add(pkg);
                }
            }
        }
        return tempList;
    }

    public boolean isOppoTestTool(String pkgName, int uid) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        if (pkgName.equals("com.oppo.qetest") || pkgName.equals("com.oppo.qemonitor")) {
            return true;
        }
        if (!(uid == 1000)) {
            return false;
        }
        return !pkgName.startsWith("com.oppo.autotest.") ? pkgName.equals("com.oppo.ScoreAppMonitor") : true;
    }

    public ArrayList<String> getRemoveTaskFilterProcessList(Context context) {
        ArrayList<String> list = new ArrayList();
        if (this.mRemoveTaskFilterProcessList == null || this.mRemoveTaskFilterProcessList.isEmpty()) {
            list.addAll(mDefaultRemoveTaskFilterProcessList);
        } else {
            list.addAll(this.mRemoveTaskFilterProcessList);
        }
        return list;
    }

    public ArrayList<String> getRemoveTaskFilterPkgList(Context context) {
        ArrayList<String> list = new ArrayList();
        if (this.mRemoveTaskFilterPkgList != null && !this.mRemoveTaskFilterPkgList.isEmpty()) {
            list.addAll(this.mRemoveTaskFilterPkgList);
        } else if (context.getPackageManager().hasSystemFeature("oppo.version.exp")) {
            list.addAll(mDefaultRemoveTaskFilterProListExp);
        } else {
            list.addAll(mDefaultRemoveTaskFilterPkgList);
        }
        list.addAll(getOppoTestToolList(context));
        return list;
    }

    public ArrayList<String> getKillRestartServicePkgList(Context context) {
        ArrayList<String> list = new ArrayList();
        if (this.mKillRestartServiceProList != null && !this.mKillRestartServiceProList.isEmpty()) {
            list.addAll(this.mKillRestartServiceProList);
        } else if (context.getPackageManager().hasSystemFeature("oppo.version.exp")) {
            list.addAll(mDefaultKillRestartServiceProListExp);
        } else {
            list.addAll(mDefaultKillRestartServiceProList);
        }
        return list;
    }

    public ArrayList<String> getRedundentTaskClassList() {
        ArrayList<String> list = new ArrayList();
        if (this.mRedundentTaskClassList == null || this.mRedundentTaskClassList.isEmpty()) {
            list.addAll(mDefaultRedundentTaskClassList);
        } else {
            list.addAll(this.mRedundentTaskClassList);
        }
        return list;
    }

    public boolean isRedundentActivity(String activity) {
        if (activity == null || activity.isEmpty()) {
            return false;
        }
        for (String str : getRedundentTaskClassList()) {
            if (activity.contains(str)) {
                return true;
            }
        }
        return false;
    }

    public boolean getClearRedundentTaskSwitch() {
        return this.mDoClearRedundentSwitch;
    }

    public boolean getPreventRedundentStartSwitch() {
        return this.mDoPreventRedundentStartSwitch;
    }

    public ArrayList<String> getBackKeyFilterList() {
        ArrayList<String> list = new ArrayList();
        if (this.mBackKeyFilterList != null && !this.mBackKeyFilterList.isEmpty()) {
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
        ArrayList<String> list = new ArrayList();
        if (this.mBackClipInterceptWhiteList != null && !this.mBackClipInterceptWhiteList.isEmpty()) {
            list.addAll(this.mBackClipInterceptWhiteList);
        } else if (this.mExpVersion) {
            list.addAll(mDefaultBackClipInterceptWhiteListExp);
        } else {
            list.addAll(mDefaultBackClipInterceptWhiteList);
        }
        return list;
    }

    public ArrayList<String> getJobScheduleTimeoutWhiteList() {
        ArrayList<String> list = new ArrayList();
        if (this.mJobScheduleTimeoutWhiteList == null || this.mJobScheduleTimeoutWhiteList.isEmpty()) {
            list.addAll(mDefaultJobScheduleTimeoutWhiteList);
        } else {
            list.addAll(this.mJobScheduleTimeoutWhiteList);
        }
        return list;
    }

    /* JADX WARNING: Removed duplicated region for block: B:238:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x025e A:{SYNTHETIC, Splitter: B:86:0x025e} */
    /* JADX WARNING: Removed duplicated region for block: B:240:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x02df A:{SYNTHETIC, Splitter: B:103:0x02df} */
    /* JADX WARNING: Removed duplicated region for block: B:242:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0362 A:{SYNTHETIC, Splitter: B:120:0x0362} */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x03d8 A:{SYNTHETIC, Splitter: B:135:0x03d8} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readSystemConfigListLocked() {
        IOException e;
        NullPointerException e2;
        Throwable th;
        XmlPullParserException e3;
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
        List<String> appPhoneCpnList = new ArrayList();
        FileInputStream fileInputStream = null;
        try {
            InputStream fileInputStream2 = new FileInputStream(new File(SYSTEM_CONFIG_LIST_FILE));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileInputStream2, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tagName = parser.getName();
                        if (this.DEBUG_SWITCH) {
                            Log.i(TAG, " readSystemConfigListLocked tagName=" + tagName);
                        }
                        String packgeName;
                        String activityName;
                        if (PROTECT_FORE_TAG.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals("")) {
                                this.mProtectForeList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked protect fore pkg = " + packgeName);
                                }
                            }
                        } else if (PROTECT_FORE_NET_TAG.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals("")) {
                                this.mProtectForeNetList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked protect fore net pkg = " + packgeName);
                                }
                            }
                        } else if (SECUREPAY_ACTIVITY_TAG.equals(tagName)) {
                            activityName = parser.nextText();
                            if (!activityName.equals("")) {
                                this.mSecurePayActivityList.add(activityName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked securepay activity = " + activityName);
                                }
                            }
                        } else if (NOTIFICATION_SERVICE_TAG.equals(tagName)) {
                            activityName = parser.nextText();
                            if (!activityName.equals("")) {
                                this.mNotificationServiceApp.add(activityName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked notifation service activity = " + activityName);
                                }
                            }
                        } else if (ALLOW_MANIFEST_NET_BRO_TAG.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals("")) {
                                this.mAllowManifestNetBroList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked allow manifest net bro pkg = " + packgeName);
                                }
                            }
                        } else if (GLOBAL_CMCC_PKG.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals("")) {
                                this.mGlobalCmccPkgList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked global cmcc white pkg = " + packgeName);
                                }
                            }
                        } else if (GLOBAL_CMCC_PROCESS.equals(tagName)) {
                            String processName = parser.nextText();
                            if (!processName.equals("")) {
                                this.mGlobalCmccProcessList.add(processName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked global cmcc white process = " + processName);
                                }
                            }
                        } else if (REMOVE_TASK_FILTER_PKG_LIST.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals("")) {
                                this.mRemoveTaskFilterPkgList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked remove task filter pkg = " + packgeName);
                                }
                            }
                        } else if (REMOVE_TASK_FILTER_PROCESS_LIST.equals(tagName)) {
                            String process = parser.nextText();
                            if (!process.equals("")) {
                                this.mRemoveTaskFilterProcessList.add(process);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked remove task filter process = " + process);
                                }
                            }
                        } else if (KILL_RESTART_SERVICE_PRO.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals("")) {
                                this.mKillRestartServiceProList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked kill service restart pkg = " + packgeName);
                                }
                            }
                        } else if (TAG_REDUNDENT_TASK_CLASS.equals(tagName)) {
                            String className = parser.nextText();
                            if (!className.equals("")) {
                                this.mRedundentTaskClassList.add(className);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked redundent class = " + className);
                                }
                            }
                        } else if (TAG_DO_CLEAR_REDUNDENT_TASK.equals(tagName)) {
                            this.mDoClearRedundentSwitch = Boolean.parseBoolean(parser.nextText());
                        } else if (TAG_PREVENT_REDUNDENT_START.equals(tagName)) {
                            this.mDoPreventRedundentStartSwitch = Boolean.parseBoolean(parser.nextText());
                        } else if (BACK_KEY_KILL_SWITCH.equals(tagName)) {
                            this.mBackKeyKillSwitch = Boolean.parseBoolean(parser.nextText());
                            this.mBackKeyInitFromFile = true;
                        } else if (PROTECT_SELF_WHITE_LIST.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals("")) {
                                this.mProtectSelfWhiteList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked self protect pkg = " + packgeName);
                                }
                            }
                        } else if (PROTECT_SELF_TIMEOUT.equals(tagName)) {
                            try {
                                this.mProtectSelfTimeout = Long.parseLong(parser.nextText());
                            } catch (NumberFormatException e4) {
                            }
                        } else if (BACK_CLIP_INTERCEPT_WHITE_LIST.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals("")) {
                                this.mBackClipInterceptWhiteList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked back clip white pkg = " + packgeName);
                                }
                            }
                        } else if (JOB_TIMEOUT_WHITE_LIST.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals("")) {
                                this.mJobScheduleTimeoutWhiteList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked job timeout white pkg = " + packgeName);
                                }
                            }
                        } else if (APP_PHONE_REFUSE_CPN.equals(tagName)) {
                            String appPhoneCpn = parser.nextText();
                            if (!appPhoneCpn.equals("")) {
                                appPhoneCpnList.add(appPhoneCpn);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked appPhoneCpn = " + appPhoneCpn);
                                }
                            }
                        }
                    }
                } while (type != 1);
                synchronized (this.mAppPhoneCpnListLock) {
                    if (!appPhoneCpnList.isEmpty()) {
                        this.mAppPhoneCpnList.clear();
                        this.mAppPhoneCpnList.addAll(appPhoneCpnList);
                    }
                }
                if (fileInputStream2 != null) {
                    try {
                        fileInputStream2.close();
                    } catch (IOException e5) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e5);
                    }
                }
                InputStream inputStream = fileInputStream2;
            } catch (NullPointerException e6) {
                e2 = e6;
                fileInputStream = fileInputStream2;
                try {
                    Log.e(TAG, "failed parsing ", e2);
                    if (fileInputStream == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e52) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e52);
                        }
                    }
                    throw th;
                }
            } catch (XmlPullParserException e7) {
                e3 = e7;
                fileInputStream = fileInputStream2;
                Log.e(TAG, "failed parsing ", e3);
                if (fileInputStream == null) {
                }
            } catch (IOException e8) {
                e52 = e8;
                fileInputStream = fileInputStream2;
                Log.e(TAG, "failed IOException ", e52);
                if (fileInputStream == null) {
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                }
                throw th;
            }
        } catch (NullPointerException e9) {
            e2 = e9;
            Log.e(TAG, "failed parsing ", e2);
            if (fileInputStream == null) {
                try {
                    fileInputStream.close();
                } catch (IOException e522) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e522);
                }
            }
        } catch (XmlPullParserException e10) {
            e3 = e10;
            Log.e(TAG, "failed parsing ", e3);
            if (fileInputStream == null) {
                try {
                    fileInputStream.close();
                } catch (IOException e5222) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e5222);
                }
            }
        } catch (IOException e11) {
            e5222 = e11;
            Log.e(TAG, "failed IOException ", e5222);
            if (fileInputStream == null) {
                try {
                    fileInputStream.close();
                } catch (IOException e52222) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e52222);
                }
            }
        }
    }

    public List<String> getAllowManifestNetBroList() {
        synchronized (this.mSystemConfigListLock) {
            List<String> list;
            if (this.mAllowManifestNetBroList == null || this.mAllowManifestNetBroList.isEmpty()) {
                list = this.mDefaultAllowManifestNetBroList;
                return list;
            }
            list = this.mAllowManifestNetBroList;
            return list;
        }
    }

    public void addStageProtectInfo(String pkg, String fromPkg, long timeout) {
        Log.i(TAG, "addStageProtectInfo from " + fromPkg + " for " + pkg + " in " + timeout);
        if (pkg != null && !pkg.equals("") && fromPkg != null && !fromPkg.equals("")) {
            if (SystemProperties.getBoolean("persist.selfprotect.test", false) || (getProtectSelfWhiteList().contains(pkg) ^ 1) == 0 || (mProtectFromPkgWhiteList.contains(fromPkg) ^ 1) == 0) {
                if (!mProtectFromPkgWhiteList.contains(fromPkg) && timeout > this.mProtectSelfTimeout) {
                    timeout = this.mProtectSelfTimeout;
                }
                removeStageProtectInfoInternal(pkg, fromPkg, false);
                synchronized (this.mProtectSelfInfos) {
                    long now = SystemClock.elapsedRealtime();
                    ProtectSelfInfo info = new ProtectSelfInfo();
                    info.mPackageName = pkg;
                    info.mFromPkg = fromPkg;
                    info.mStartTime = now;
                    info.mTimeout = timeout;
                    this.mProtectSelfInfos.add(info);
                    uploadProtecetSelfInfo(EVENTID_ADD_PROTECT_INFO, info);
                    if (this.DEBUG_SWITCH) {
                        Log.i(TAG, "add protect self info for " + pkg + ", timeout=" + timeout);
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
                    ProtectSelfInfo info = (ProtectSelfInfo) sListIterator.next();
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
        ArrayList<String> tempList = new ArrayList();
        synchronized (this.mProtectSelfInfos) {
            Iterator<ProtectSelfInfo> sListIterator = this.mProtectSelfInfos.iterator();
            long now = SystemClock.elapsedRealtime();
            while (sListIterator.hasNext()) {
                ProtectSelfInfo info = (ProtectSelfInfo) sListIterator.next();
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
        ArrayList<String> tempList = new ArrayList();
        synchronized (this.mProtectSelfInfos) {
            Iterator<ProtectSelfInfo> sListIterator = this.mProtectSelfInfos.iterator();
            long now = SystemClock.elapsedRealtime();
            while (sListIterator.hasNext()) {
                ProtectSelfInfo info = (ProtectSelfInfo) sListIterator.next();
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
        ArrayList<String> list = new ArrayList();
        if (this.mProtectSelfWhiteList == null || this.mProtectSelfWhiteList.isEmpty()) {
            list.addAll(mDefaultProtectSelfWhiteList);
        } else {
            list.addAll(this.mProtectSelfWhiteList);
        }
        return list;
    }

    private void uploadProtecetSelfInfo(String eventId, ProtectSelfInfo info) {
        if (this.mContext != null && this.mUploadSelfProtectSwitch && info != null) {
            HashMap<String, String> map = new HashMap();
            map.put("protectedPkg", info.mPackageName);
            map.put("fromPkg", info.mFromPkg);
            map.put("timeout", String.valueOf(info.mTimeout));
            OppoStatistics.onCommon(this.mContext, DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, eventId, map, false);
        }
    }

    public boolean inPaySafePkgList(String pkg) {
        try {
            if (!FILTER_SEPCIAL_SECURE_APP.contains(pkg) && AppGlobals.getPackageManager().isSecurePayApp(pkg)) {
                Log.d(TAG, "inPaySafePkgList : " + pkg);
                return true;
            }
        } catch (RemoteException e) {
            Log.i(TAG, "Cannot find remote package");
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:66:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00e2  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x011d A:{SYNTHETIC, Splitter: B:46:0x011d} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00e2  */
    /* JADX WARNING: Removed duplicated region for block: B:66:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0140 A:{SYNTHETIC, Splitter: B:52:0x0140} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readRomListFileLocked() {
        Exception e;
        Throwable th;
        ArrayMap<String, List<String>> forbiddenList = new ArrayMap();
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(new File(OPPO_ROM_BLACK_LIST_FILE));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tagName = parser.getName();
                        if (this.DEBUG_SWITCH) {
                            Log.i(TAG, " readRomListFileLocked tagName=" + tagName);
                        }
                        if (FORBIDDEN_TAG.equals(tagName)) {
                            String minCode = parser.getAttributeValue(null, MIN_VERSION_CODE);
                            String maxCode = parser.getAttributeValue(null, MAX_VERSION_CODE);
                            String packageName = parser.nextText();
                            if (minCode == null) {
                                minCode = String.valueOf(0);
                            }
                            if (maxCode == null) {
                                maxCode = DEFAULT_MAX_VERSION_CODE;
                            }
                            if (!(packageName.equals("") || minCode == null || maxCode == null)) {
                                List<String> versionCodeList = new ArrayList();
                                versionCodeList.add(minCode);
                                versionCodeList.add(maxCode);
                                if (forbiddenList.containsKey(packageName)) {
                                    versionCodeList.addAll((Collection) forbiddenList.get(packageName));
                                }
                                forbiddenList.put(packageName, versionCodeList);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readRomListFileLocked pkg= " + packageName + " minCode=" + minCode + " maxCode=" + maxCode);
                                }
                            }
                        }
                    }
                } while (type != 1);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e2);
                    }
                }
                stream = stream2;
            } catch (Exception e3) {
                e = e3;
                stream = stream2;
                try {
                    Log.e(TAG, "failed parsing ", e);
                    if (stream != null) {
                    }
                    if (forbiddenList.isEmpty()) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e22) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e22);
                    }
                }
                throw th;
            }
        } catch (Exception e4) {
            e = e4;
            Log.e(TAG, "failed parsing ", e);
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e222) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e222);
                }
            }
            if (forbiddenList.isEmpty()) {
            }
        }
        if (forbiddenList.isEmpty()) {
            synchronized (mSFLock) {
                this.mStartForbiddenList.clear();
                this.mStartForbiddenList.putAll(forbiddenList);
            }
        }
    }

    public boolean isInstalledAppWidget(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        List<String> appWidgetList = new ArrayList();
        appWidgetList.addAll(OppoBPMHelper.getAppWidgetList());
        for (String widgetPkgName : appWidgetList) {
            if (pkgName.equals(widgetPkgName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFromNotifyPkg(String pkgName) {
        if (pkgName == null || pkgName.equals("")) {
            return false;
        }
        try {
            boolean contains;
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
                AppGlobals.getPackageManager().setPackageStoppedState(pkgName, false, UserHandle.getUserId(uid));
            } catch (RemoteException e) {
            } catch (IllegalArgumentException e2) {
                Log.w(TAG, "handleAppForNotification " + e2);
            }
            addFromNotifyPkgList(pkgName);
            sendMessage(11, pkgName, OppoBPMUtils.getInstance().getStartFromNotityTime());
        }
    }

    private void handleAppFromNotityMsg(Message msg) {
        String pkgName = msg.obj;
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "handleAppFromNotityMsg pkgName " + pkgName);
        }
        removeFromNotifyPkgList(pkgName);
    }

    public boolean isFromControlCenterPkg(String pkgName) {
        if (pkgName == null || pkgName.equals("")) {
            return false;
        }
        try {
            boolean contains;
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
        sendMessage(10, pkgName, OppoBPMUtils.getInstance().getStartFromNotityTime());
    }

    private void handleAppFromControlCenterMsg(Message msg) {
        String pkgName = msg.obj;
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "handleAppFromControlCenterMsg pkgName " + pkgName);
        }
        removeFromControlCenterPkgList(pkgName);
    }

    public boolean isAppPhoneCpn(String cpn) {
        if (cpn == null || cpn.equals("")) {
            return false;
        }
        try {
            boolean contains;
            synchronized (this.mAppPhoneCpnListLock) {
                contains = this.mAppPhoneCpnList.contains(cpn);
            }
            return contains;
        } catch (Exception e) {
            Log.e(TAG, "Failed to judge isAppPhoneCpn!");
            return false;
        }
    }

    private void initAppPhoneCpnList() {
        synchronized (this.mAppPhoneCpnListLock) {
            this.mAppPhoneCpnList.addAll(this.mDefaultAppPhoneCpnList);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0052 A:{SYNTHETIC, Splitter: B:23:0x0052} */
    /* JADX WARNING: Removed duplicated region for block: B:75:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0090 A:{SYNTHETIC, Splitter: B:53:0x0090} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0095  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readBackKeyCleanupFilterFile() {
        Exception e;
        Throwable th;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readBackKeyCleanupFilterFile start");
        }
        File backKeyCleanupFilterFile = new File(BACK_KEY_FILTER_FILE);
        if (!backKeyCleanupFilterFile.exists()) {
            Log.e(TAG, "backKeyCleanupFilterFile isn't exist!");
        }
        boolean isException = false;
        List<String> allowList = new ArrayList();
        FileReader fr = null;
        try {
            FileReader fr2 = new FileReader(backKeyCleanupFilterFile);
            try {
                BufferedReader reader = new BufferedReader(fr2);
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    } else if (!TextUtils.isEmpty(line)) {
                        allowList.add(line.trim());
                    }
                }
                if (fr2 != null) {
                    try {
                        fr2.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                if (null == null) {
                    synchronized (this.mBackKeyCleanupFilterListLock) {
                        this.mBackKeyFilterList.clear();
                        this.mBackKeyFilterList.addAll(allowList);
                    }
                }
            } catch (Exception e3) {
                e2 = e3;
                fr = fr2;
                isException = true;
                try {
                    e2.printStackTrace();
                    if (fr != null) {
                    }
                    if (1 != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fr != null) {
                        try {
                            fr.close();
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (!isException) {
                        synchronized (this.mBackKeyCleanupFilterListLock) {
                            this.mBackKeyFilterList.clear();
                            this.mBackKeyFilterList.addAll(allowList);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fr = fr2;
                if (fr != null) {
                }
                if (isException) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e22 = e4;
            isException = true;
            e22.printStackTrace();
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
            if (1 != null) {
                synchronized (this.mBackKeyCleanupFilterListLock) {
                    this.mBackKeyFilterList.clear();
                    this.mBackKeyFilterList.addAll(allowList);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0052 A:{SYNTHETIC, Splitter: B:23:0x0052} */
    /* JADX WARNING: Removed duplicated region for block: B:75:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0090 A:{SYNTHETIC, Splitter: B:53:0x0090} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0095  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readAccountSyncWhiteListFile() {
        Exception e;
        Throwable th;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readAccountSyncWhiteListFile start");
        }
        File accountSyncWhiteListFile = new File(ACCOUNTSYNC_MANGER_FILE);
        if (!accountSyncWhiteListFile.exists()) {
            Log.e(TAG, "accountSyncWhiteListFile isn't exist!");
        }
        boolean isException = false;
        List<String> allowList = new ArrayList();
        FileReader fr = null;
        try {
            FileReader fr2 = new FileReader(accountSyncWhiteListFile);
            try {
                BufferedReader reader = new BufferedReader(fr2);
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    } else if (!TextUtils.isEmpty(line)) {
                        allowList.add(line.trim());
                    }
                }
                if (fr2 != null) {
                    try {
                        fr2.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                if (null == null) {
                    synchronized (this.mAccountSyncWhiteListLock) {
                        this.mAccountSyncWhiteList.clear();
                        this.mAccountSyncWhiteList.addAll(allowList);
                    }
                }
            } catch (Exception e3) {
                e2 = e3;
                fr = fr2;
                isException = true;
                try {
                    e2.printStackTrace();
                    if (fr != null) {
                    }
                    if (1 != null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fr != null) {
                        try {
                            fr.close();
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (!isException) {
                        synchronized (this.mAccountSyncWhiteListLock) {
                            this.mAccountSyncWhiteList.clear();
                            this.mAccountSyncWhiteList.addAll(allowList);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fr = fr2;
                if (fr != null) {
                }
                if (isException) {
                }
                throw th;
            }
        } catch (Exception e4) {
            e22 = e4;
            isException = true;
            e22.printStackTrace();
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e222) {
                    e222.printStackTrace();
                }
            }
            if (1 != null) {
                synchronized (this.mAccountSyncWhiteListLock) {
                    this.mAccountSyncWhiteList.clear();
                    this.mAccountSyncWhiteList.addAll(allowList);
                }
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

    public boolean isInAccountSyncWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mAccountSyncWhiteListLock) {
            if (this.mAccountSyncWhiteList.contains(pkgName) || this.mDefaultAccountSyncWhiteList.contains(pkgName)) {
                result = true;
            } else {
                result = isInAutoBootWhiteList(pkgName);
            }
        }
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x007f A:{SYNTHETIC, Splitter: B:29:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x008b A:{SYNTHETIC, Splitter: B:35:0x008b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readCustomAppListFile() {
        Exception e;
        Throwable th;
        File file = new File(CUSTOMIZE_APP_PATH);
        if (file.exists()) {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream2, null);
                    int type;
                    do {
                        type = parser.next();
                        if (type == 2) {
                            if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                                String value = parser.getAttributeValue(null, "att");
                                if (value != null) {
                                    this.mCustomAppList.add(value);
                                    Log.i(TAG, "add custom list : " + value);
                                }
                            }
                        }
                    } while (type != 1);
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    stream = stream2;
                } catch (Exception e3) {
                    e = e3;
                    stream = stream2;
                    try {
                        Log.w(TAG, "failed parsing ", e);
                        if (stream != null) {
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    stream = stream2;
                    if (stream != null) {
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                Log.w(TAG, "failed parsing ", e);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
                return;
            }
            return;
        }
        Log.w(TAG, "/system/etc/oppo_customize_whitelist.xml file don't exist!");
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

    private void handleLoadSystemAppMsg() {
        HashSet<String> systemAppList = new HashSet();
        if (this.mContext != null && this.mContext.getPackageManager() != null) {
            List<ApplicationInfo> infos = this.mContext.getPackageManager().getInstalledApplications(8192);
            if (infos != null) {
                for (ApplicationInfo app : infos) {
                    if ((app.flags & 1) != 0 || (app.flags & 128) != 0) {
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
        boolean result = false;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (this.mSystemAppList.size() <= 0) {
            return isBuildApp(packageName);
        }
        synchronized (this.mSystemAppListLock) {
            for (String pkg : this.mSystemAppList) {
                if (packageName.equals(pkg)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean isOppoApp(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (packageName.startsWith("com.oppo.") || packageName.startsWith("com.coloros.") || packageName.startsWith("com.nearme.")) {
            return true;
        }
        return false;
    }

    private boolean isBuildApp(String pkgName) {
        if (TextUtils.isEmpty(pkgName) || this.mContext == null || this.mContext.getPackageManager() == null) {
            return false;
        }
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(pkgName, 0);
            if (packageInfo == null || packageInfo.applicationInfo == null) {
                return false;
            }
            if ((packageInfo.applicationInfo.flags & 1) == 0 && (packageInfo.applicationInfo.flags & 128) == 0) {
                return false;
            }
            return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0011, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addMiniProgramShare(String shareAppPkgName, String miniProgramPkgName, String miniProgramSignature) {
        if (!TextUtils.isEmpty(shareAppPkgName) && (MINI_PROGRAM_KEY.equals(shareAppPkgName) ^ 1) == 0 && !TextUtils.isEmpty(miniProgramPkgName) && !TextUtils.isEmpty(miniProgramSignature)) {
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

    /* JADX WARNING: Missing block: B:4:0x0011, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeMiniProgramShare(String shareAppPkgName, String miniProgramPkgName, String miniProgramSignature) {
        if (!TextUtils.isEmpty(shareAppPkgName) && (MINI_PROGRAM_KEY.equals(shareAppPkgName) ^ 1) == 0 && !TextUtils.isEmpty(miniProgramPkgName)) {
            synchronized (this.mMiniProgramShareMapLock) {
                if (this.mMiniProgramShareMap.containsKey(miniProgramPkgName)) {
                    this.mMiniProgramShareMap.remove(miniProgramPkgName);
                }
            }
        }
    }

    public String getMiniProgramSignature(String pkgName) {
        String str = null;
        if (TextUtils.isEmpty(pkgName)) {
            return null;
        }
        synchronized (this.mMiniProgramShareMapLock) {
            if (this.mMiniProgramShareMap.containsKey(pkgName)) {
                str = (String) this.mMiniProgramShareMap.get(pkgName);
            }
        }
        return str;
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
        boolean result = false;
        if (TextUtils.isEmpty(originAppCpn)) {
            return false;
        }
        synchronized (this.mFastAppWechatPayMapLock) {
            if (this.mFastAppWechatMap.containsKey(originAppCpn)) {
                result = true;
            }
        }
        return result;
    }

    public ComponentName replaceFastAppWechatPayCpn(String originAppCpn) {
        ComponentName componentName = null;
        if (TextUtils.isEmpty(originAppCpn)) {
            return null;
        }
        synchronized (this.mFastAppWechatPayMapLock) {
            if (this.mFastAppWechatMap.containsKey(originAppCpn)) {
                componentName = new ComponentName(FAST_APP_PKG, (String) this.mFastAppWechatMap.get(originAppCpn));
            }
        }
        return componentName;
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

    public void dump(String arg) {
        Log.i(TAG, "OppoListManager dump " + arg);
        if (DUMP_SYSTEM_APPS_LIST.equals(arg)) {
            Log.d(TAG, "SystemAppList: " + getInstance().mSystemAppList);
        }
    }

    public void registerLogModule() {
        try {
            Log.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Log.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", new Class[]{String.class});
            Log.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), new Object[]{OppoListManager.class.getName()});
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
}
