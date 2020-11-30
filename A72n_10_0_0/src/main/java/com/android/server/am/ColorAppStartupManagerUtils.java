package com.android.server.am;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.LocaleList;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import com.android.server.ColorDeviceIdleHelper;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.policy.OppoPhoneWindowManager;
import com.android.server.wm.ColorAppSwitchManager;
import com.android.server.wm.ColorFreeformManagerService;
import com.android.server.wm.startingwindow.ColorStartingWindowContants;
import com.color.settings.ColorSettings;
import com.color.settings.ColorSettingsChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorAppStartupManagerUtils {
    private static final String AAM_ACTIVITY_WHITE_PKG = "AamActivityWhitePkg";
    private static final String AAM_PROVIDER_WHITE_PKG = "AamProviderWhitePkg";
    private static final String ACTION_NAME = "action";
    private static final String ACTIVITY_ASSOCIATE_BLACK = "activityAssociateBlack";
    private static final String ACTIVITY_ASSOCIATE_WHITE = "activityAssociateWhite";
    private static final String ACTIVITY_CALLED_KEY = "activityCalledKey";
    private static final String ACTIVITY_CALLED_WHITE_CPN = "activityCalledCpnWhite";
    private static final String ACTIVITY_CALLED_WHITE_PKG = "activityCalledPkgWhite";
    private static final String ACTIVITY_CALLER_WHITE_PKG = "activityCallerPkgWhite";
    private static final String ACTIVITY_INTERCEPT_SWITCH = "activityInterceptSwitch";
    private static final String ACTIVITY_NAME = "activity";
    private static final String ACTIVITY_PKG_KEY = "activityPkgKey";
    private static final String ACTIVITY_SELFCALL_PKG_WHITE = "activitySelfCallPkgWhite";
    private static final String ACTIVITY_SLEEPCPN_BLACK = "activitySleepCpnBlack";
    private static final String AUTHORIZE_COMPONENT_NAME = "authorizeCpnName";
    private static final String BIND_SERVICE_WHITE_CPN = "bindServiceCpn";
    private static final String BLACKGUARD_ACTIVITY = "blackguardActivity";
    private static final String BLACKGUARD_NAME = "blackguard";
    private static final String BOOT_BROADCAST_OPTIMIZE_CONFIG = "bootBroadcastOptimizeConfig";
    private static final String BROADCAST_WHITE_CPN = "broadcastAction";
    private static final String BROADCAST_WHITE_PKG = "broadcastPkg";
    private static final String BROWSER_INTERCEPT_UPLOAD_SWITCH = "browserInterceptUploadSwitch";
    private static final String BUILD_BLACK_PKG = "sysblackPkg";
    private static final String CALL_CHECK_COUNT = "callCheckCount";
    private static final String CHECK_COUNT = "checkCount";
    private static final String COLLECT_APP_START_PKG = "monitorAppStartPkg";
    private static final String CPN_UPPER_LIMIT_COUNT_INTERVAL = "cpnUpperLimitCountInterval";
    private static final String CPN_UPPER_LIMIT_COUNT_STEP = "cpnUpperLimitCountStep";
    private static final String CPN_UPPER_LIMIT_SWITCH = "cpnUpperLimitSwitch";
    private static final String CPN_UPPER_LIMIT_THRESHOLD_CONNECTED = "cpnUpperLimitThresholdConnected";
    private static final String CPN_UPPER_LIMIT_THRESHOLD_TIME = "cpnUpperLimitThresholdTime";
    private static final String EXP_FEATURE = "expFeature";
    private static final String EXP_GLOBAL_BLACK_LIST_FILE_PATH = "/data/oppo/coloros/startup/exp_global_blacklist.txt";
    private static final String GAME_MONITOR_CHECKCOUNT_NAME = "gameMonitorCheckCount";
    private static final String GAME_MONITOR_PACKAGE_NAME = "gameMonitorPackage";
    private static final String GAME_MONITOR_SWITCH_NAME = "gameMonitorSwitch";
    private static final String GAME_PAY_ABNORMAL_BLACK_URL_KEY = "gamePayAbnormalBlackUrlKey";
    private static final String GAME_PAY_ABNORMAL_CALLED_BLACK_PKG_CPN = "gamePayAbnormalCalledBlackPkgCpn";
    private static final String GAME_PAY_ABNORMAL_CALLED_WHITE_KEY = "gamePayAbnormalCalledWhiteKey";
    private static final String GAME_PAY_ABNORMAL_CALLED_WHITE_PKG_CPN = "gamePayAbnormalCalledWhitePkgCpn";
    private static final String GAME_PAY_ABNORMAL_CALLER_BALCK_KEY = "gamePayAbnormalCallerBlackKey";
    private static final String GAME_PAY_ABNORMAL_CALLER_BLACK_PKG = "gamePayAbnormalCallerBlackPkg";
    private static final String GAME_PAY_ABNORMAL_CALLER_WHITE_CPN = "gamePayAbnormalCallerWhiteCpn";
    private static final String GAME_PAY_ABNORMAL_CALLER_WHITE_KEY = "gamePayAbnormalCallerWhiteKey";
    private static final String GAME_PAY_ABNORMAL_CALLER_WHITE_PKG_CPN = "gamePayAbnormalCallerWhitePkgCpn";
    private static final String GAME_PAY_ABNORMAL_CHECK_COUNT = "gamePayAbnormalCheckCount";
    private static final String GAME_PAY_ABNORMAL_RULE_SEPARATOR = "#";
    private static final String GAME_PAY_ABNORMAL_SWITCH = "gamePayAbnormalSwitch";
    private static final String GAME_PAY_MONITOR_COMPONENT_NAME = "payMonitorComponent";
    private static final String GAME_PAY_MONITOR_FUZZY_COMPONENT_NAME = "payMonitorFuzzyComponent";
    private static final String INTERCEPT_INTERVAL_TIME = "interceptIntervalTime";
    private static final String JOB_WHITE_PKG = "jobWhitePkg";
    public static final String KEY_BOOT_BROADCAST_OPTIMIZE_ENABLE = "switch";
    public static final String KEY_DISPATCH_TIME_PERIOD = "dispatchTimePeriod";
    public static final String KEY_NO_DELAY_APP = "noDelayApp";
    public static final String KEY_QUEUE_MAX_NUM = "queueMaxNum";
    public static final String KEY_SHORT_DELAY_APP = "shortDelayApp";
    public static final String KEY_SHORT_DELAY_APP_DISPATCH_TIME = "shortDelayAppDispatchTime";
    public static final String KEY_SYS_APP_DELAY_ENABLE = "sysAppDelay";
    public static final String KEY_SYS_APP_DISPATCH_TIME = "sysAppDispatchTime";
    public static final String KEY_SYS_LONG_DELAY_APP = "sysLongDelayApp";
    public static final String KEY_SYS_SHORT_DELAY_APP = "sysShortDelayApp";
    public static final String KEY_THIRD_APP_DELAY_ENABLE = "thirdAppDelay";
    public static final String KEY_THIRD_APP_DISPATCH_TIME = "thirdAppDispatchTime";
    private static final int MASK_BACKGROUND_RESTRICT = 1;
    private static final int MASK_BIND_SERVICE = 4;
    private static final int MASK_START_SERVICE = 2;
    private static final String NOTIFY_WHITE_PKG = "notifyWhitePkg";
    private static final String OPPO_ASSOCIATE_START_WHITE_FILE_PATH_PART = "/startup/associate_startup_whitelist.txt";
    private static final String OPPO_CUSTOMIZE_WHITE_FILE_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final String OPPO_GAME_PAY_MONITOR_CONFIG_LIST_FILE = "/data/oppo/coloros/startup/game_pay_monitor_config.xml";
    private static final String OPPO_ROM_BLACK_LIST_FILE = "/data/oppo/coloros/startup/sys_rom_black_list.xml";
    private static final String OPPO_STARTUP_MANAGER_FILE_PATH = "/data/oppo/coloros/startup/startup_manager.xml";
    private static final String OPPO_STARTUP_MONITOR_FILE_PATH = "/data/oppo/coloros/startup/sys_startupmanager_monitor_list.xml";
    private static final String PROTECT_PKG = "protectPkg";
    private static final String PROVIDER_CALLER_PKG = "providerCallerPkg";
    private static final String PROVIDER_NAME = "provider";
    private static final String PROVIDER_WHITE_CPN = "providerCpn";
    private static final String RECEIVER_ACTION_EXCLUDE_PKG = "receiverActionExcludePkg";
    private static final String RECEIVER_ACTION_NAME = "receiverAction";
    private static final String RECEIVER_NAME = "receiver";
    private static final String SERVICE_CALLER_PKG_ACTION = "serviceCallerPkgAction";
    private static final String SEVICECPN_NAME = "sevicecpn";
    private static final String SITE_SOUGOU = "sougouSite";
    private static final String START_SERVICE_WHITE_CPN = "startServiceCpn";
    private static final String START_SERVICE_WHITE_PKG = "startServicePkg";
    private static final String SYNC_WHITE_PKG = "syncWhitePkg";
    private static final String TAG = "ColorAppStartupManager";
    private static final String TAG_RESTART_SERVICE_IN_RECENT = "restartServiceInRecent";
    private static final String TAG_RESTART_SERVICE_WHITE = "restartServiceWhite";
    private static final ArrayMap<String, List<String>> mBlackListReceiverExcludePkg = new ArrayMap<>();
    private static final List<String> mDefaultRestartServiceWhiteList = Arrays.asList("com.tencent.mobileqq", ColorStartingWindowContants.WECHAT_PACKAGE_NAME);
    private static final List<String> mDefaultRestartServiceWhiteListExp = Arrays.asList(ColorStartingWindowContants.WECHAT_PACKAGE_NAME, "com.tencent.mobileqq", "com.zing.zalo", "com.facebook.orca", "com.facebook.katana", "com.instagram.android", "jp.naver.line.android", "com.whatsapp", "com.bbm", "com.skype.raider", "com.viber.voip", "com.path", "com.facebook.lite", "com.truecaller", "com.bsb.hike", "com.snapchat.android", "com.twitter.android", "com.imo.android.imoim", "com.google.android.gm");
    private static ColorAppStartupManagerUtils sAsmUtils = null;
    private boolean isBootBroadcastOptimizeEnable = true;
    private boolean isExpVersion = true;
    private boolean isNeedReadConfig = true;
    private boolean isSysAppDelayEnable = true;
    private boolean isThirdAppDelayEnable = false;
    private List<String> mAamActivityWhiteList = new ArrayList();
    private final Object mAamActivityWhiteListLock = new Object();
    private List<String> mAamProviderWhiteList = new ArrayList();
    private final Object mAamProviderWhiteListLock = new Object();
    private List<String> mActionBlackList = new ArrayList();
    private ArrayMap<String, List<String>> mActivityAssociateBlackPkgMap = new ArrayMap<>();
    private ArrayMap<String, List<String>> mActivityAssociateWhitePkgMap = new ArrayMap<>();
    private List<String> mActivityBlackList = new ArrayList();
    private List<String> mActivityCalledKeyList = new ArrayList();
    private List<String> mActivityCalledWhiteCpnList = new ArrayList();
    private List<String> mActivityCalledWhitePkgList = new ArrayList();
    private List<String> mActivityCallerWhitePkgList = new ArrayList();
    private List<String> mActivityPkgKeyList = new ArrayList();
    private List<String> mActivitySelfCallWhitePkgList = new ArrayList();
    private List<String> mActivitySleepCpnBlackList = new ArrayList();
    private List<String> mAssociateDefaultList = new ArrayList();
    private SparseArray<List<String>> mAssociateStartWhiteListContainer = new SparseArray<>();
    private FileObserverPolicy mAssociateStartupFileObserver = null;
    private List<String> mAuthorizeCpnList = new ArrayList();
    private ArrayMap<String, List<String>> mBackgroundRestrictCallerPkgMap = new ArrayMap<>();
    private ArrayMap<String, List<String>> mBindServiceCallerPkgMap = new ArrayMap<>();
    private List<String> mBindServiceWhiteCpnList = new ArrayList();
    private List<String> mBlackguardActivityList = new ArrayList();
    private List<String> mBlackguardList = new ArrayList();
    private List<String> mBroadcastActionWhiteList = new ArrayList();
    private List<String> mBroadcastWhitePkgList = new ArrayList();
    private List<String> mBuildAppBlackList = new ArrayList();
    private int mCallCheckCount = 50;
    private int mCheckCount = 50;
    private List<String> mCollectAppStartList = new ArrayList();
    ColorSettingsChangeListener mColorConfigChangeListener = new ColorSettingsChangeListener(null) {
        /* class com.android.server.am.ColorAppStartupManagerUtils.AnonymousClass1 */

        public void onSettingsChange(boolean selfChange, String path, int userId) {
            if (ColorAppStartupManagerUtils.this.mDebugSwitch) {
                Log.v("ColorAppStartupManager", "on config change and maybe read config, path=" + path + ", userId=" + userId);
            }
            if (path != null && path.equals(ColorAppStartupManagerUtils.OPPO_ASSOCIATE_START_WHITE_FILE_PATH_PART)) {
                OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).associateWhiteFileChanage(userId);
            }
        }
    };
    private Context mContext;
    private int mCpnUpperLimitCountInterval = 500;
    private int mCpnUpperLimitCountStep = 50;
    private boolean mCpnUpperLimitSwitch = false;
    private int mCpnUpperLimitThresholdConnected = ColorFreeformManagerService.FREEFORM_CALLER_UID;
    private long mCpnUpperLimitThresholdTime = 1000;
    private List<String> mCustomizeWhiteList = new ArrayList();
    private boolean mDebugSwitch = this.mDegugDetail;
    private boolean mDegugDetail = IColorAppStartupManager.DEBUG_DETAIL;
    private String mDialogButtonText = null;
    private String mDialogContentText = null;
    private String mDialogTitleText = null;
    private long mDispatchTimePeriod = ColorAppSwitchManager.INTERVAL;
    private List<String> mExpGlobalBlackList = new ArrayList();
    private List<String> mGameMonitorPackageList = new ArrayList();
    private List<String> mGamePayAbnormalBlackUrlKeyList = new ArrayList();
    private ArrayMap<String, List<String>> mGamePayAbnormalCalledBlackFirstCpnMap = new ArrayMap<>();
    private List<String> mGamePayAbnormalCalledBlackPkgList = new ArrayList();
    private ArrayMap<String, ArrayMap<String, List<String>>> mGamePayAbnormalCalledBlackSecondCpnMap = new ArrayMap<>();
    private List<String> mGamePayAbnormalCalledWhiteKeyList = new ArrayList();
    private ArrayMap<String, List<String>> mGamePayAbnormalCalledWhitePkgCpnMap = new ArrayMap<>();
    private List<String> mGamePayAbnormalCalledWhitePkgList = new ArrayList();
    private List<String> mGamePayAbnormalCallerBlackKeyList = new ArrayList();
    private List<String> mGamePayAbnormalCallerBlackPkgList = new ArrayList();
    private List<String> mGamePayAbnormalCallerWhiteCpnList = new ArrayList();
    private List<String> mGamePayAbnormalCallerWhiteKeyList = new ArrayList();
    private ArrayMap<String, List<String>> mGamePayAbnormalCallerWhitePkgCpnMap = new ArrayMap<>();
    private List<String> mGamePayAbnormalCallerWhitePkgList = new ArrayList();
    private int mGamePayAbnormalCheckCount = 2;
    private boolean mGamePayAbnormalSwitch = false;
    private final Object mGamePayLock = new Object();
    private int mGamePayMonitorCheckCount = 2;
    private FileObserverPolicy mGamePayMonitorConfigFileObserver = null;
    private List<String> mGamePayMonitorCpnList = new ArrayList();
    private List<String> mGamePayMonitorFuzzyCpnList = new ArrayList();
    private boolean mGamePayMonitorSwitch = false;
    private List<String> mJobWhiteList = new ArrayList();
    private ArrayList<String> mNoDelayList = new ArrayList<>();
    private List<String> mNotifyWhiteList = new ArrayList();
    private List<String> mProtectWhiteList = new ArrayList();
    private List<String> mProviderBlackList = new ArrayList();
    private List<String> mProviderCallerPkgList = new ArrayList();
    private List<String> mProviderWhiteCpnList = new ArrayList();
    private int mQueueMaxNum = 20;
    private List<String> mReceiverActionBlackList = new ArrayList();
    private List<String> mReceiverBlackList = new ArrayList();
    private List<String> mRestartServiceInRecentList = new ArrayList();
    private FileObserverPolicy mRestartServiceWhiteFileObserver = null;
    private List<String> mRestartServiceWhiteList = new ArrayList();
    private List<String> mSeviceCpnBlacklist = new ArrayList();
    private long mShortDelayAppDispatchTime = ColorAppSwitchManager.INTERVAL;
    private ArrayList<String> mShortDelayList = new ArrayList<>();
    private List<String> mSougouSiteList = new ArrayList();
    private ArrayMap<String, List<String>> mStartServiceCallerPkgMap = new ArrayMap<>();
    private List<String> mStartServiceWhiteCpnList = new ArrayList();
    private List<String> mStartServiceWhiteList = new ArrayList();
    private FileObserverPolicy mStartupManagerFileObserver = null;
    private FileObserverPolicy mStartupMonitorFileObserver = null;
    private boolean mSwitchBrowserInterceptUpload = false;
    private boolean mSwitchInterceptActivity = false;
    private List<String> mSyncWhiteList = new ArrayList();
    private long mSysAppDispatchTime = 60000;
    private ArrayList<String> mSysLongDelayList = new ArrayList<>();
    private ArrayList<String> mSysShortDelayList = new ArrayList<>();
    private long mThirdAppDispatchTime = ColorDeviceIdleHelper.ALARM_WINDOW_LENGTH;

    private ColorAppStartupManagerUtils() {
        initDir();
        initFileObserver();
        readStartupManagerFile();
        readStartupMonitorFile();
        readCustomizeWhiteList();
        readExpGlobalBlackFile();
        readGamePayMonitorConfigFile();
    }

    public static ColorAppStartupManagerUtils getInstance() {
        if (sAsmUtils == null) {
            synchronized (ColorAppStartupManagerUtils.class) {
                if (sAsmUtils == null) {
                    sAsmUtils = new ColorAppStartupManagerUtils();
                }
            }
        }
        return sAsmUtils;
    }

    private void initDir() {
        Log.i("ColorAppStartupManager", "initDir start");
        File startupManagerFile = new File(OPPO_STARTUP_MANAGER_FILE_PATH);
        File startupMonitorFile = new File(OPPO_STARTUP_MONITOR_FILE_PATH);
        try {
            if (!startupManagerFile.exists()) {
                startupManagerFile.createNewFile();
                File permFile = new File(Environment.getRootDirectory(), "oppo/startup_manager.xml");
                if (permFile.exists() && startupManagerFile.exists()) {
                    initStartupManagerFile(permFile, startupManagerFile);
                }
            }
            if (!startupMonitorFile.exists()) {
                startupMonitorFile.createNewFile();
            }
        } catch (IOException e) {
            Log.e("ColorAppStartupManager", "initDir failed!!!");
            e.printStackTrace();
        }
    }

    private void initStartupManagerFile(File srcFile, File newFile) {
        StringBuilder sb;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            FileInputStream inStream2 = new FileInputStream(srcFile);
            FileOutputStream outStream2 = new FileOutputStream(newFile);
            byte[] buffer = new byte[OppoPhoneWindowManager.SPEECH_START_TYPE_VALUE];
            while (true) {
                int byteRead = inStream2.read(buffer);
                if (byteRead != -1) {
                    outStream2.write(buffer, 0, byteRead);
                } else {
                    inStream2.close();
                    try {
                        inStream2.close();
                        outStream2.close();
                        return;
                    } catch (Exception e) {
                        e = e;
                        sb = new StringBuilder();
                    }
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            Log.e("ColorAppStartupManager", "initStartupManagerFile Failed " + e2);
            if (0 != 0) {
                try {
                    inStream.close();
                } catch (Exception e3) {
                    e = e3;
                    sb = new StringBuilder();
                    sb.append("Failed to close state FileInputStream ");
                    sb.append(e);
                    Log.e("ColorAppStartupManager", sb.toString());
                    return;
                }
            }
            if (0 != 0) {
                outStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inStream.close();
                } catch (Exception e4) {
                    Log.e("ColorAppStartupManager", "Failed to close state FileInputStream " + e4);
                    throw th;
                }
            }
            if (0 != 0) {
                outStream.close();
            }
            throw th;
        }
    }

    private void initFileObserver() {
        this.mStartupManagerFileObserver = new FileObserverPolicy(OPPO_STARTUP_MANAGER_FILE_PATH);
        this.mStartupManagerFileObserver.startWatching();
        this.mStartupMonitorFileObserver = new FileObserverPolicy(OPPO_STARTUP_MONITOR_FILE_PATH);
        this.mStartupMonitorFileObserver.startWatching();
        this.mGamePayMonitorConfigFileObserver = new FileObserverPolicy(OPPO_GAME_PAY_MONITOR_CONFIG_LIST_FILE);
        this.mGamePayMonitorConfigFileObserver.startWatching();
    }

    public void init(Context context) {
        this.mContext = context;
        registerConfigChangeListener();
        readAssociateStartWhiteFile(0);
    }

    private void registerConfigChangeListener() {
        Context context = this.mContext;
        if (context == null) {
            Log.e("ColorAppStartupManager", "registerConfigChangeListener failed!");
        } else {
            ColorSettings.registerChangeListenerForAll(context, OPPO_ASSOCIATE_START_WHITE_FILE_PATH_PART, 0, this.mColorConfigChangeListener);
        }
    }

    public List<String> getSeviceCpnBlacklist() {
        return this.mSeviceCpnBlacklist;
    }

    public List<String> getReceiverBlackList() {
        return this.mReceiverBlackList;
    }

    public List<String> getReceiverActionBlackList() {
        return this.mReceiverActionBlackList;
    }

    public ArrayMap<String, List<String>> getReceiverExcludeBlackList() {
        return mBlackListReceiverExcludePkg;
    }

    public List<String> getProviderBlackList() {
        return this.mProviderBlackList;
    }

    public List<String> getActivityBlackList() {
        return this.mActivityBlackList;
    }

    public List<String> getActionBlackList() {
        return this.mActionBlackList;
    }

    public List<String> getBlackguardList() {
        return this.mBlackguardList;
    }

    public List<String> getActivitySelfCallWhitePkgList() {
        return this.mActivitySelfCallWhitePkgList;
    }

    public ArrayMap<String, List<String>> getActivityAssociateWhitePkgMap() {
        return this.mActivityAssociateWhitePkgMap;
    }

    public ArrayMap<String, List<String>> getActivityAssociateBlackPkgMap() {
        return this.mActivityAssociateBlackPkgMap;
    }

    public List<String> getActivitySleepCpnBlackList() {
        return this.mActivitySleepCpnBlackList;
    }

    public void setCallCheckCount(String checkCount) {
        this.mCallCheckCount = Integer.parseInt(checkCount);
    }

    public int getCallCheckCount() {
        return this.mCallCheckCount;
    }

    public boolean getSwitchInterceptActivity() {
        return this.mSwitchInterceptActivity;
    }

    public void setSwitchInterceptActivity(String isIntercept) {
        this.mSwitchInterceptActivity = Boolean.parseBoolean(isIntercept);
    }

    public void setSwitchBrowserInterceptUpload(String isAllowed) {
        this.mSwitchBrowserInterceptUpload = Boolean.parseBoolean(isAllowed);
    }

    public boolean getSwitchBrowserInterceptUpload() {
        return this.mSwitchBrowserInterceptUpload;
    }

    public List<String> getActivityCallerWhitePkgList() {
        return this.mActivityCallerWhitePkgList;
    }

    public List<String> getActivityCalledWhitePkgList() {
        return this.mActivityCalledWhitePkgList;
    }

    public List<String> getActivityCalledWhiteCpnList() {
        return this.mActivityCalledWhiteCpnList;
    }

    public List<String> getActivityPkgKeyList() {
        return this.mActivityPkgKeyList;
    }

    public List<String> getActivityCalledKeyList() {
        return this.mActivityCalledKeyList;
    }

    public List<String> getBlackguardActivityList() {
        return this.mBlackguardActivityList;
    }

    public List<String> getSougouSite() {
        return this.mSougouSiteList;
    }

    public List<String> getCustomizeWhiteList() {
        return this.mCustomizeWhiteList;
    }

    /* access modifiers changed from: protected */
    public List<String> getBuildBlackList() {
        return this.mBuildAppBlackList;
    }

    /* access modifiers changed from: protected */
    public List<String> getStartServiceWhiteList() {
        return this.mStartServiceWhiteList;
    }

    /* access modifiers changed from: protected */
    public List<String> getStartServiceWhiteCpnList() {
        return this.mStartServiceWhiteCpnList;
    }

    /* access modifiers changed from: protected */
    public List<String> getBindServiceWhiteList() {
        return this.mBindServiceWhiteCpnList;
    }

    /* access modifiers changed from: protected */
    public List<String> getJobWhiteList() {
        return this.mJobWhiteList;
    }

    /* access modifiers changed from: protected */
    public List<String> getSyncWhiteList() {
        return this.mSyncWhiteList;
    }

    /* access modifiers changed from: protected */
    public List<String> getNotifyWhiteList() {
        return this.mNotifyWhiteList;
    }

    /* access modifiers changed from: protected */
    public List<String> getProviderWhiteList() {
        return this.mProviderWhiteCpnList;
    }

    /* access modifiers changed from: protected */
    public List<String> getBroadcastWhiteList() {
        return this.mBroadcastWhitePkgList;
    }

    /* access modifiers changed from: protected */
    public List<String> getBroadcastActionWhiteList() {
        return this.mBroadcastActionWhiteList;
    }

    /* access modifiers changed from: protected */
    public List<String> getProtectList() {
        return this.mProtectWhiteList;
    }

    /* access modifiers changed from: protected */
    public SparseArray<List<String>> getAssociateStartWhiteListContainer() {
        SparseArray<List<String>> sparseArray;
        synchronized (this.mAssociateStartWhiteListContainer) {
            sparseArray = this.mAssociateStartWhiteListContainer;
        }
        return sparseArray;
    }

    private List<String> getAssociateStartWhiteListNotNull(int userId) {
        List<String> list = this.mAssociateStartWhiteListContainer.get(userId);
        if (list != null) {
            return list;
        }
        List<String> list2 = new ArrayList<>();
        this.mAssociateStartWhiteListContainer.put(userId, list2);
        return list2;
    }

    /* access modifiers changed from: protected */
    public List<String> getCollectAppStartList() {
        return this.mCollectAppStartList;
    }

    /* access modifiers changed from: protected */
    public List<String> getAuthorizeCpnList() {
        return this.mAuthorizeCpnList;
    }

    /* access modifiers changed from: protected */
    public List<String> getProviderCallerPkgList() {
        return this.mProviderCallerPkgList;
    }

    /* access modifiers changed from: protected */
    public ArrayMap<String, List<String>> getBackgroundRestrictMap() {
        return this.mBackgroundRestrictCallerPkgMap;
    }

    /* access modifiers changed from: protected */
    public ArrayMap<String, List<String>> getStartServiceMap() {
        return this.mStartServiceCallerPkgMap;
    }

    /* access modifiers changed from: protected */
    public ArrayMap<String, List<String>> getBindServiceMap() {
        return this.mBindServiceCallerPkgMap;
    }

    public int getCheckCount() {
        return this.mCheckCount;
    }

    public boolean isInAamActivityWhitePkgList(String pkgName) {
        boolean result;
        synchronized (this.mAamActivityWhiteListLock) {
            result = this.mAamActivityWhiteList.contains(pkgName);
        }
        return result;
    }

    public boolean isInAamProviderWhitePkgList(String pkgName) {
        boolean result;
        synchronized (this.mAamProviderWhiteListLock) {
            result = this.mAamProviderWhiteList.contains(pkgName);
        }
        return result;
    }

    public void readStartupManagerFile() {
        if (this.mDebugSwitch) {
            Log.i("ColorAppStartupManager", "readStartupManagerFile start");
        }
        readConfigFromFileLocked(new File(OPPO_STARTUP_MANAGER_FILE_PATH));
    }

    /* JADX WARNING: Removed duplicated region for block: B:530:0x0d96 A[SYNTHETIC, Splitter:B:530:0x0d96] */
    private void readConfigFromFileLocked(File file) {
        FileInputStream stream;
        Throwable th;
        IOException e;
        StringBuilder sb;
        List<String> calledPkgList;
        List<String> calledPkgList2;
        List<String> actionList;
        List<String> actionList2;
        List<String> actionList3;
        if (this.mDebugSwitch) {
            Log.i("ColorAppStartupManager", "readConfigFromFileLocked start");
        }
        cleanConfigList();
        if (!this.mSougouSiteList.isEmpty()) {
            this.mSougouSiteList.clear();
        }
        FileInputStream stream2 = null;
        try {
            FileInputStream stream3 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            String mask = null;
            parser.setInput(stream3, null);
            while (true) {
                int type = parser.next();
                if (type == 2) {
                    String tagName = parser.getName();
                    if (this.mDebugSwitch) {
                        Log.i("ColorAppStartupManager", " readFromFileLocked tagName=" + tagName);
                    }
                    if (SEVICECPN_NAME.equals(tagName)) {
                        String sevicecpn = parser.nextText();
                        if (!sevicecpn.equals("")) {
                            this.mSeviceCpnBlacklist.add(sevicecpn);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked sevicecpn = " + sevicecpn);
                            }
                        }
                    } else if (RECEIVER_NAME.equals(tagName)) {
                        String receiver = parser.nextText();
                        if (!receiver.equals("")) {
                            this.mReceiverBlackList.add(receiver);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked receiver = " + receiver);
                            }
                        }
                    } else if (RECEIVER_ACTION_NAME.equals(tagName)) {
                        String receiverAction = parser.nextText();
                        if (!receiverAction.equals("")) {
                            this.mReceiverActionBlackList.add(receiverAction);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked receiverAction = " + receiverAction);
                            }
                        }
                    } else if (RECEIVER_ACTION_EXCLUDE_PKG.equals(tagName)) {
                        String action = parser.getAttributeValue(mask, ACTION_NAME);
                        String pkgs = parser.getAttributeValue(mask, BrightnessConstants.AppSplineXml.TAG_PACKAGE);
                        if (!TextUtils.isEmpty(pkgs) && !TextUtils.isEmpty(action)) {
                            List<String> newPackages = parseAppRestrictedText(pkgs);
                            List<String> orignalPackages = new ArrayList<>();
                            synchronized (mBlackListReceiverExcludePkg) {
                                if (mBlackListReceiverExcludePkg != null && !mBlackListReceiverExcludePkg.isEmpty()) {
                                    orignalPackages = mBlackListReceiverExcludePkg.get(action);
                                }
                                if (orignalPackages != null) {
                                    orignalPackages.addAll(newPackages);
                                } else {
                                    orignalPackages = newPackages;
                                }
                                mBlackListReceiverExcludePkg.put(action, orignalPackages);
                            }
                            if (this.mDebugSwitch) {
                                Log.d("ColorAppStartupManager", "receiverActionExcludePkg get " + action + " = " + orignalPackages.toString());
                            }
                        }
                    } else if (PROVIDER_NAME.equals(tagName)) {
                        String provider = parser.nextText();
                        if (!provider.equals("")) {
                            this.mProviderBlackList.add(provider);
                            if (this.mDegugDetail) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked provider = " + provider);
                            }
                        }
                    } else if (ACTIVITY_NAME.equals(tagName)) {
                        String activity = parser.nextText();
                        if (!activity.equals("")) {
                            this.mActivityBlackList.add(activity);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked activity = " + activity);
                            }
                        }
                    } else if (ACTION_NAME.equals(tagName)) {
                        String action2 = parser.nextText();
                        if (!action2.equals("")) {
                            this.mActionBlackList.add(action2);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked action = " + action2);
                            }
                        }
                    } else if (BLACKGUARD_NAME.equals(tagName)) {
                        String blackguard = parser.nextText();
                        if (!blackguard.equals("")) {
                            this.mBlackguardList.add(blackguard);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked blackguard = " + blackguard);
                            }
                        }
                    } else if (ACTIVITY_INTERCEPT_SWITCH.equals(tagName)) {
                        String isIntercept = parser.nextText();
                        if (!isIntercept.equals("")) {
                            setSwitchInterceptActivity(isIntercept);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked isIntercept = " + isIntercept);
                            }
                        }
                    } else if (BROWSER_INTERCEPT_UPLOAD_SWITCH.equals(tagName)) {
                        String isAllowed = parser.nextText();
                        if (!isAllowed.equals("")) {
                            setSwitchBrowserInterceptUpload(isAllowed);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked isBrowserInterceptUploadAllowed = " + isAllowed);
                            }
                        }
                    } else if (ACTIVITY_CALLER_WHITE_PKG.equals(tagName)) {
                        String callerWhitePkg = parser.nextText();
                        if (!callerWhitePkg.equals("")) {
                            this.mActivityCallerWhitePkgList.add(callerWhitePkg);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked activityCallerWhitePkg = " + callerWhitePkg);
                            }
                        }
                    } else if (ACTIVITY_CALLED_WHITE_CPN.equals(tagName)) {
                        String calledWhiteCpn = parser.nextText();
                        if (!calledWhiteCpn.equals("")) {
                            this.mActivityCalledWhiteCpnList.add(calledWhiteCpn);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked activityCalledWhiteCpn = " + calledWhiteCpn);
                            }
                        }
                    } else if (ACTIVITY_CALLED_WHITE_PKG.equals(tagName)) {
                        String calledWhitePkg = parser.nextText();
                        if (!calledWhitePkg.equals("")) {
                            this.mActivityCalledWhitePkgList.add(calledWhitePkg);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked activityCalledWhitePkg = " + calledWhitePkg);
                            }
                        }
                    } else if (ACTIVITY_PKG_KEY.equals(tagName)) {
                        String pkgKey = parser.nextText();
                        if (!pkgKey.equals("")) {
                            this.mActivityPkgKeyList.add(pkgKey);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked activityPkgKey = " + pkgKey);
                            }
                        }
                    } else if (ACTIVITY_CALLED_KEY.equals(tagName)) {
                        String calledKey = parser.nextText();
                        if (!calledKey.equals("")) {
                            this.mActivityCalledKeyList.add(calledKey);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked activityCalledKey = " + calledKey);
                            }
                        }
                    } else if (BLACKGUARD_ACTIVITY.equals(tagName)) {
                        String blackguardActivity = parser.nextText();
                        if (!blackguardActivity.equals("")) {
                            this.mBlackguardActivityList.add(blackguardActivity);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked blackguardActivity = " + blackguardActivity);
                            }
                        }
                    } else if (SITE_SOUGOU.equals(tagName)) {
                        String sougouSite = parser.nextText();
                        if (!sougouSite.equals("")) {
                            this.mSougouSiteList.add(sougouSite);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked sougouSite = " + sougouSite);
                            }
                        }
                    } else if (BUILD_BLACK_PKG.equals(tagName)) {
                        String buildBlackPkg = parser.nextText();
                        if (!buildBlackPkg.equals("")) {
                            this.mBuildAppBlackList.add(buildBlackPkg);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked buildBlackPkg = " + buildBlackPkg);
                            }
                        }
                    } else if (START_SERVICE_WHITE_PKG.equals(tagName)) {
                        String startServicePkg = parser.nextText();
                        if (!startServicePkg.equals("")) {
                            this.mStartServiceWhiteList.add(startServicePkg);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked startServicePkg = " + startServicePkg);
                            }
                        }
                    } else if (START_SERVICE_WHITE_CPN.equals(tagName)) {
                        String startServiceCpn = parser.nextText();
                        if (!startServiceCpn.equals("")) {
                            this.mStartServiceWhiteCpnList.add(startServiceCpn);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked startServiceCpn = " + startServiceCpn);
                            }
                        }
                    } else if (BIND_SERVICE_WHITE_CPN.equals(tagName)) {
                        String bindServiceCpn = parser.nextText();
                        if (!bindServiceCpn.equals("")) {
                            this.mBindServiceWhiteCpnList.add(bindServiceCpn);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked bindServiceCpn = " + bindServiceCpn);
                            }
                        }
                    } else if (JOB_WHITE_PKG.equals(tagName)) {
                        String jobWhitepkg = parser.nextText();
                        if (!jobWhitepkg.equals("")) {
                            this.mJobWhiteList.add(jobWhitepkg);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked jobWhitepkg = " + jobWhitepkg);
                            }
                        }
                    } else if (SYNC_WHITE_PKG.equals(tagName)) {
                        String syncWhitePkg = parser.nextText();
                        if (!syncWhitePkg.equals("")) {
                            this.mSyncWhiteList.add(syncWhitePkg);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked syncWhitePkg = " + syncWhitePkg);
                            }
                        }
                    } else if (NOTIFY_WHITE_PKG.equals(tagName)) {
                        String notifyWhitePkg = parser.nextText();
                        if (!notifyWhitePkg.equals("")) {
                            this.mNotifyWhiteList.add(notifyWhitePkg);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked notifyWhitePkg = " + notifyWhitePkg);
                            }
                        }
                    } else if (PROVIDER_WHITE_CPN.equals(tagName)) {
                        String providerCpn = parser.nextText();
                        if (!providerCpn.equals("")) {
                            this.mProviderWhiteCpnList.add(providerCpn);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked providerCpn = " + providerCpn);
                            }
                        }
                    } else if (BROADCAST_WHITE_PKG.equals(tagName)) {
                        String broadcastPkg = parser.nextText();
                        if (!broadcastPkg.equals("")) {
                            this.mBroadcastWhitePkgList.add(broadcastPkg);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked broadcastPkg = " + broadcastPkg);
                            }
                        }
                    } else if (BROADCAST_WHITE_CPN.equals(tagName)) {
                        String broadcastAction = parser.nextText();
                        if (!broadcastAction.equals("")) {
                            this.mBroadcastActionWhiteList.add(broadcastAction);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked broadcastaction = " + broadcastAction);
                            }
                        }
                    } else if (PROTECT_PKG.equals(tagName)) {
                        String protectPkg = parser.nextText();
                        if (!protectPkg.equals("")) {
                            this.mProtectWhiteList.add(protectPkg);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked protectPkg = " + protectPkg);
                            }
                        }
                    } else if (AUTHORIZE_COMPONENT_NAME.equals(tagName)) {
                        String authorizeCpnName = parser.nextText();
                        if (!authorizeCpnName.equals("")) {
                            this.mAuthorizeCpnList.add(authorizeCpnName);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked authorizeCpnName = " + authorizeCpnName);
                            }
                        }
                    } else if (AAM_ACTIVITY_WHITE_PKG.equals(tagName)) {
                        String aamActivityWhitePkg = parser.nextText();
                        if (!aamActivityWhitePkg.equals("")) {
                            synchronized (this.mAamActivityWhiteListLock) {
                                this.mAamActivityWhiteList.add(aamActivityWhitePkg);
                            }
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked aamActivityWhitePkg = " + aamActivityWhitePkg);
                            }
                        }
                    } else if (AAM_PROVIDER_WHITE_PKG.equals(tagName)) {
                        String aamProviderWhitePkg = parser.nextText();
                        if (!aamProviderWhitePkg.equals("")) {
                            synchronized (this.mAamProviderWhiteListLock) {
                                this.mAamProviderWhiteList.add(aamProviderWhitePkg);
                            }
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked aamProviderWhitePkg = " + aamProviderWhitePkg);
                            }
                        }
                    } else if (CPN_UPPER_LIMIT_SWITCH.equals(tagName)) {
                        String cpnUpperLimitSwitch = parser.nextText();
                        if (!cpnUpperLimitSwitch.equals("")) {
                            this.mCpnUpperLimitSwitch = Boolean.parseBoolean(cpnUpperLimitSwitch);
                            Log.i("ColorAppStartupManager", " readFromFileLocked cpnUpperLimitSwitch = " + this.mCpnUpperLimitSwitch);
                        }
                    } else if (CPN_UPPER_LIMIT_THRESHOLD_CONNECTED.equals(tagName)) {
                        String cpnUpperLimitThresholdConnected = parser.nextText();
                        if (!cpnUpperLimitThresholdConnected.equals("")) {
                            this.mCpnUpperLimitThresholdConnected = Integer.parseInt(cpnUpperLimitThresholdConnected);
                            Log.i("ColorAppStartupManager", " readFromFileLocked cpnUpperLimitThresholdConnected = " + this.mCpnUpperLimitThresholdConnected);
                        }
                    } else if (CPN_UPPER_LIMIT_COUNT_INTERVAL.equals(tagName)) {
                        String cpnUpperLimitCountInterval = parser.nextText();
                        if (!cpnUpperLimitCountInterval.equals("")) {
                            this.mCpnUpperLimitCountInterval = Integer.parseInt(cpnUpperLimitCountInterval);
                            Log.i("ColorAppStartupManager", " readFromFileLocked cpnUpperLimitCountInterval = " + this.mCpnUpperLimitCountInterval);
                        }
                    } else if (CPN_UPPER_LIMIT_COUNT_STEP.equals(tagName)) {
                        String cpnUpperLimitCountStep = parser.nextText();
                        if (!cpnUpperLimitCountStep.equals("")) {
                            this.mCpnUpperLimitCountStep = Integer.parseInt(cpnUpperLimitCountStep);
                            Log.i("ColorAppStartupManager", " readFromFileLocked cpnUpperLimitCountStep = " + this.mCpnUpperLimitCountStep);
                        }
                    } else if (CPN_UPPER_LIMIT_THRESHOLD_TIME.equals(tagName)) {
                        String cpnUpperLimitThresholdTime = parser.nextText();
                        if (!cpnUpperLimitThresholdTime.equals("")) {
                            this.mCpnUpperLimitThresholdTime = Long.parseLong(cpnUpperLimitThresholdTime);
                            Log.i("ColorAppStartupManager", " readFromFileLocked cpnUpperLimitThresholdTime = " + this.mCpnUpperLimitThresholdTime);
                        }
                    } else if (BOOT_BROADCAST_OPTIMIZE_CONFIG.equals(tagName)) {
                        if (this.isNeedReadConfig) {
                            String bootBroadcastOptimizeEnable = parser.getAttributeValue(mask, "switch");
                            String sysAppDelayEnable = parser.getAttributeValue(mask, "sysAppDelay");
                            String thirdAppDelayEnable = parser.getAttributeValue(mask, "thirdAppDelay");
                            String sysAppQueueMaxNum = parser.getAttributeValue(mask, "queueMaxNum");
                            String shortDelayAppDispatchTime = parser.getAttributeValue(mask, "shortDelayAppDispatchTime");
                            String sysAppDispatchTime = parser.getAttributeValue(mask, "sysAppDispatchTime");
                            String thirdAppDispatchTime = parser.getAttributeValue(mask, "thirdAppDispatchTime");
                            String dispatchTimePeriod = parser.getAttributeValue(mask, "dispatchTimePeriod");
                            try {
                                this.isBootBroadcastOptimizeEnable = Boolean.parseBoolean(bootBroadcastOptimizeEnable);
                                this.isSysAppDelayEnable = Boolean.parseBoolean(sysAppDelayEnable);
                                this.isThirdAppDelayEnable = Boolean.parseBoolean(thirdAppDelayEnable);
                                this.mQueueMaxNum = Integer.parseInt(sysAppQueueMaxNum);
                                this.mShortDelayAppDispatchTime = Long.parseLong(shortDelayAppDispatchTime);
                                this.mSysAppDispatchTime = Long.parseLong(sysAppDispatchTime);
                                this.mThirdAppDispatchTime = Long.parseLong(thirdAppDispatchTime);
                                this.mDispatchTimePeriod = Long.parseLong(dispatchTimePeriod);
                            } catch (Exception e2) {
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", "read BOOT_BROADCAST_OPTIMIZE_CONFIG failed!");
                                }
                            }
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", "isBootBroadcastOptimizeEnable = " + this.isBootBroadcastOptimizeEnable + ", isSysAppDelayEnable = " + this.isSysAppDelayEnable + ", isThirdAppDelayEnable = " + this.isThirdAppDelayEnable + ", mQueueMaxNum = " + this.mQueueMaxNum + ", mShortDelayAppDispatchTime = " + this.mShortDelayAppDispatchTime + ", mSysAppDispatchTime = " + this.mSysAppDispatchTime + ", mThirdAppDispatchTime = " + this.mThirdAppDispatchTime + ", mDispatchTimePeriod = " + this.mDispatchTimePeriod);
                            }
                            mask = null;
                        } else {
                            mask = null;
                        }
                    } else if ("noDelayApp".equals(tagName)) {
                        if (this.isNeedReadConfig) {
                            String noDelayApp = parser.nextText();
                            if (!TextUtils.isEmpty(noDelayApp)) {
                                this.mNoDelayList.add(noDelayApp);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readFromFileLocked noDelayApp = " + noDelayApp);
                                }
                            }
                            mask = null;
                        } else {
                            mask = null;
                        }
                    } else if ("shortDelayApp".equals(tagName)) {
                        if (this.isNeedReadConfig) {
                            String shortDelayApp = parser.nextText();
                            if (!TextUtils.isEmpty(shortDelayApp)) {
                                this.mShortDelayList.add(shortDelayApp);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readFromFileLocked shortDelayApp = " + shortDelayApp);
                                }
                            }
                            mask = null;
                        } else {
                            mask = null;
                        }
                    } else if ("sysShortDelayApp".equals(tagName)) {
                        if (this.isNeedReadConfig) {
                            String sysShortDelayApp = parser.nextText();
                            if (!TextUtils.isEmpty(sysShortDelayApp)) {
                                this.mSysShortDelayList.add(sysShortDelayApp);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readFromFileLocked sysShortDelayApp = " + sysShortDelayApp);
                                }
                            }
                            mask = null;
                        } else {
                            mask = null;
                        }
                    } else if ("sysLongDelayApp".equals(tagName)) {
                        if (this.isNeedReadConfig) {
                            String sysLongDelayApp = parser.nextText();
                            if (!TextUtils.isEmpty(sysLongDelayApp)) {
                                this.mSysLongDelayList.add(sysLongDelayApp);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readFromFileLocked sysLongDelayApp = " + sysLongDelayApp);
                                }
                            }
                            mask = null;
                        } else {
                            mask = null;
                        }
                    } else if (EXP_FEATURE.equals(tagName)) {
                        String expFeature = parser.nextText();
                        if (!TextUtils.isEmpty(expFeature)) {
                            try {
                                this.isExpVersion = Boolean.parseBoolean(expFeature);
                            } catch (Exception e3) {
                                e3.printStackTrace();
                            }
                        }
                        mask = null;
                    } else if (PROVIDER_CALLER_PKG.equals(tagName)) {
                        String providerCallerPkg = parser.nextText();
                        if (!TextUtils.isEmpty(providerCallerPkg)) {
                            this.mProviderCallerPkgList.add(providerCallerPkg);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked providerCallerPkg = " + providerCallerPkg);
                            }
                        }
                        mask = null;
                    } else if (SERVICE_CALLER_PKG_ACTION.equals(tagName)) {
                        String pkg = parser.getAttributeValue(null, "callerPkg");
                        String action3 = parser.getAttributeValue(null, ACTION_NAME);
                        String mask2 = parser.getAttributeValue(null, "mask");
                        if (!TextUtils.isEmpty(pkg) && !TextUtils.isEmpty(action3) && !TextUtils.isEmpty(mask2)) {
                            try {
                                int maskValue = Integer.parseInt(mask2, 2);
                                if ((maskValue & 1) != 0) {
                                    if (this.mBackgroundRestrictCallerPkgMap.containsKey(pkg)) {
                                        actionList3 = this.mBackgroundRestrictCallerPkgMap.get(pkg);
                                    } else {
                                        actionList3 = new ArrayList<>();
                                    }
                                    actionList3.add(action3);
                                    this.mBackgroundRestrictCallerPkgMap.put(pkg, actionList3);
                                }
                                if ((maskValue & 2) != 0) {
                                    if (this.mStartServiceCallerPkgMap.containsKey(pkg)) {
                                        actionList2 = this.mStartServiceCallerPkgMap.get(pkg);
                                    } else {
                                        actionList2 = new ArrayList<>();
                                    }
                                    actionList2.add(action3);
                                    this.mStartServiceCallerPkgMap.put(pkg, actionList2);
                                }
                                if ((maskValue & 4) != 0) {
                                    if (this.mBindServiceCallerPkgMap.containsKey(pkg)) {
                                        actionList = this.mBindServiceCallerPkgMap.get(pkg);
                                    } else {
                                        actionList = new ArrayList<>();
                                    }
                                    actionList.add(action3);
                                    this.mBindServiceCallerPkgMap.put(pkg, actionList);
                                }
                            } catch (Exception e4) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked service_action_pkg failed");
                            }
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked service_action_pkg: pkg=" + pkg + "action=" + action3 + "mask=" + mask2);
                            }
                        }
                        mask = null;
                    } else if (ACTIVITY_ASSOCIATE_WHITE.equals(tagName)) {
                        String callerPkg = parser.getAttributeValue(null, "callerPkg");
                        String calledPkg = parser.getAttributeValue(null, "calledPkg");
                        if (!TextUtils.isEmpty(callerPkg) && !TextUtils.isEmpty(calledPkg)) {
                            if (this.mActivityAssociateWhitePkgMap.containsKey(callerPkg)) {
                                calledPkgList2 = this.mActivityAssociateWhitePkgMap.get(callerPkg);
                            } else {
                                calledPkgList2 = new ArrayList<>();
                            }
                            calledPkgList2.add(calledPkg);
                            this.mActivityAssociateWhitePkgMap.put(callerPkg, calledPkgList2);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked activityAssociateWhite : callerPkg =" + callerPkg + "  calledPkg = " + calledPkg);
                            }
                        }
                        mask = null;
                    } else if (ACTIVITY_SELFCALL_PKG_WHITE.equals(tagName)) {
                        String selfcallpkgwhite = parser.nextText();
                        if (!TextUtils.isEmpty(selfcallpkgwhite)) {
                            this.mActivitySelfCallWhitePkgList.add(selfcallpkgwhite);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked activitySelfCallPkgWhite = " + selfcallpkgwhite);
                            }
                        }
                        mask = null;
                    } else if (ACTIVITY_ASSOCIATE_BLACK.equals(tagName)) {
                        mask = null;
                        String callerPkg2 = parser.getAttributeValue(null, "callerPkg");
                        String calledPkg2 = parser.getAttributeValue(null, "calledPkg");
                        if (!TextUtils.isEmpty(callerPkg2) && !TextUtils.isEmpty(calledPkg2)) {
                            if (this.mActivityAssociateBlackPkgMap.containsKey(callerPkg2)) {
                                calledPkgList = this.mActivityAssociateBlackPkgMap.get(callerPkg2);
                            } else {
                                calledPkgList = new ArrayList<>();
                            }
                            calledPkgList.add(calledPkg2);
                            this.mActivityAssociateBlackPkgMap.put(callerPkg2, calledPkgList);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked activityAssociateBlack : callerPkg =" + callerPkg2 + "  calledPkg = " + calledPkg2);
                            }
                        }
                    } else {
                        mask = null;
                        if (ACTIVITY_SLEEPCPN_BLACK.equals(tagName)) {
                            String activitySleepCpnBlack = parser.nextText();
                            if (!TextUtils.isEmpty(activitySleepCpnBlack)) {
                                this.mActivitySleepCpnBlackList.add(activitySleepCpnBlack);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readFromFileLocked activitySleepCpnBlack = " + activitySleepCpnBlack);
                                }
                            }
                        } else if (TAG_RESTART_SERVICE_WHITE.equals(tagName)) {
                            String restartServiceWhite = parser.nextText();
                            if (!TextUtils.isEmpty(restartServiceWhite)) {
                                this.mRestartServiceWhiteList.add(restartServiceWhite);
                            }
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked restartServiceWhite = " + restartServiceWhite);
                            }
                        } else if (TAG_RESTART_SERVICE_IN_RECENT.equals(tagName)) {
                            String restartServiceInRecent = parser.nextText();
                            if (!TextUtils.isEmpty(restartServiceInRecent)) {
                                this.mRestartServiceInRecentList.add(restartServiceInRecent);
                            }
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked restartServiceInRecent = " + restartServiceInRecent);
                            }
                        } else if (CHECK_COUNT.equals(tagName)) {
                            String checkCount = parser.nextText();
                            if (!TextUtils.isEmpty(checkCount)) {
                                try {
                                    this.mCheckCount = Integer.parseInt(checkCount);
                                } catch (Exception e5) {
                                    if (this.mDebugSwitch) {
                                        Log.i("ColorAppStartupManager", "parse checkCount exception");
                                    }
                                }
                            }
                        }
                    }
                }
                if (type == 1) {
                    try {
                        stream3.close();
                    } catch (IOException e6) {
                        Log.e("ColorAppStartupManager", "Failed to close state FileInputStream " + e6);
                    }
                    return;
                }
            }
            sb.append("Failed to close state FileInputStream ");
            sb.append(e);
            Log.e("ColorAppStartupManager", sb.toString());
        } catch (NullPointerException e7) {
            Log.e("ColorAppStartupManager", "failed parsing ", e7);
            if (0 != 0) {
                try {
                    stream2.close();
                } catch (IOException e8) {
                    e = e8;
                    sb = new StringBuilder();
                }
            }
        } catch (NumberFormatException e9) {
            Log.e("ColorAppStartupManager", "failed parsing ", e9);
            if (0 != 0) {
                try {
                    stream2.close();
                } catch (IOException e10) {
                    e = e10;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e11) {
            Log.e("ColorAppStartupManager", "failed parsing ", e11);
            if (0 != 0) {
                try {
                    stream2.close();
                } catch (IOException e12) {
                    e = e12;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e13) {
            Log.e("ColorAppStartupManager", "failed IOException ", e13);
            if (0 != 0) {
                try {
                    stream2.close();
                } catch (IOException e14) {
                    e = e14;
                    sb = new StringBuilder();
                }
            }
        } catch (IndexOutOfBoundsException e15) {
            stream = null;
            Log.e("ColorAppStartupManager", "failed parsing ", e15);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e16) {
                    e = e16;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th2) {
            th = th2;
            if (stream != null) {
            }
            throw th;
        }
    }

    private List<String> parseAppRestrictedText(String contentText) {
        String[] values;
        List<String> whiteList = new ArrayList<>();
        if (!TextUtils.isEmpty(contentText) && (values = contentText.split(GAME_PAY_ABNORMAL_RULE_SEPARATOR)) != null) {
            for (String str : values) {
                whiteList.add(str.trim());
            }
        }
        return whiteList;
    }

    /* access modifiers changed from: protected */
    public void cleanConfigList() {
        if (!this.mSeviceCpnBlacklist.isEmpty()) {
            this.mSeviceCpnBlacklist.clear();
        }
        if (!this.mReceiverBlackList.isEmpty()) {
            this.mReceiverBlackList.clear();
        }
        if (!this.mReceiverActionBlackList.isEmpty()) {
            this.mReceiverActionBlackList.clear();
        }
        if (!mBlackListReceiverExcludePkg.isEmpty()) {
            mBlackListReceiverExcludePkg.clear();
        }
        if (!this.mProviderBlackList.isEmpty()) {
            this.mProviderBlackList.clear();
        }
        if (!this.mActivityBlackList.isEmpty()) {
            this.mActivityBlackList.clear();
        }
        if (!this.mActionBlackList.isEmpty()) {
            this.mActionBlackList.clear();
        }
        if (!this.mBlackguardList.isEmpty()) {
            this.mBlackguardList.clear();
        }
        if (!this.mActivityCallerWhitePkgList.isEmpty()) {
            this.mActivityCallerWhitePkgList.clear();
        }
        if (!this.mActivityCalledWhitePkgList.isEmpty()) {
            this.mActivityCalledWhitePkgList.clear();
        }
        if (!this.mActivityCalledWhiteCpnList.isEmpty()) {
            this.mActivityCalledWhiteCpnList.clear();
        }
        if (!this.mActivityPkgKeyList.isEmpty()) {
            this.mActivityPkgKeyList.clear();
        }
        if (!this.mActivityCalledKeyList.isEmpty()) {
            this.mActivityCalledKeyList.clear();
        }
        if (!this.mBlackguardActivityList.isEmpty()) {
            this.mBlackguardActivityList.clear();
        }
        if (!this.mBuildAppBlackList.isEmpty()) {
            this.mBuildAppBlackList.clear();
        }
        if (!this.mStartServiceWhiteList.isEmpty()) {
            this.mStartServiceWhiteList.clear();
        }
        if (!this.mStartServiceWhiteCpnList.isEmpty()) {
            this.mStartServiceWhiteCpnList.clear();
        }
        if (!this.mBindServiceWhiteCpnList.isEmpty()) {
            this.mBindServiceWhiteCpnList.clear();
        }
        if (!this.mJobWhiteList.isEmpty()) {
            this.mJobWhiteList.clear();
        }
        if (!this.mSyncWhiteList.isEmpty()) {
            this.mSyncWhiteList.clear();
        }
        if (!this.mNotifyWhiteList.isEmpty()) {
            this.mNotifyWhiteList.clear();
        }
        if (!this.mProviderWhiteCpnList.isEmpty()) {
            this.mProviderWhiteCpnList.clear();
        }
        if (!this.mBroadcastWhitePkgList.isEmpty()) {
            this.mBroadcastWhitePkgList.clear();
        }
        if (!this.mBroadcastActionWhiteList.isEmpty()) {
            this.mBroadcastActionWhiteList.clear();
        }
        if (!this.mProtectWhiteList.isEmpty()) {
            this.mProtectWhiteList.clear();
        }
        if (!this.mAuthorizeCpnList.isEmpty()) {
            this.mAuthorizeCpnList.clear();
        }
        synchronized (this.mAamActivityWhiteListLock) {
            if (!this.mAamActivityWhiteList.isEmpty()) {
                this.mAamActivityWhiteList.clear();
            }
        }
        synchronized (this.mAamProviderWhiteListLock) {
            if (!this.mAamProviderWhiteList.isEmpty()) {
                this.mAamProviderWhiteList.clear();
            }
        }
        if (!this.mProviderCallerPkgList.isEmpty()) {
            this.mProviderCallerPkgList.clear();
        }
        if (!this.mBackgroundRestrictCallerPkgMap.isEmpty()) {
            this.mBackgroundRestrictCallerPkgMap.clear();
        }
        if (!this.mStartServiceCallerPkgMap.isEmpty()) {
            this.mStartServiceCallerPkgMap.clear();
        }
        if (!this.mBindServiceCallerPkgMap.isEmpty()) {
            this.mBindServiceCallerPkgMap.clear();
        }
        if (!this.mActivitySelfCallWhitePkgList.isEmpty()) {
            this.mActivitySelfCallWhitePkgList.clear();
        }
        if (!this.mActivityAssociateWhitePkgMap.isEmpty()) {
            this.mActivityAssociateWhitePkgMap.clear();
        }
        if (!this.mActivitySleepCpnBlackList.isEmpty()) {
            this.mActivitySleepCpnBlackList.clear();
        }
        if (!this.mActivityAssociateBlackPkgMap.isEmpty()) {
            this.mActivityAssociateBlackPkgMap.clear();
        }
        if (!this.mRestartServiceWhiteList.isEmpty()) {
            this.mRestartServiceWhiteList.clear();
        }
        if (!this.mRestartServiceInRecentList.isEmpty()) {
            this.mRestartServiceInRecentList.clear();
        }
    }

    public void readStartupMonitorFile() {
        if (this.mDebugSwitch) {
            Log.i("ColorAppStartupManager", "readStartupMonitorFile start");
        }
        readMonitorFromFileLocked(new File(OPPO_STARTUP_MONITOR_FILE_PATH));
    }

    private void readMonitorFromFileLocked(File file) {
        StringBuilder sb;
        int type;
        if (this.mDebugSwitch) {
            Log.i("ColorAppStartupManager", "readMonitorFromFileLocked start");
        }
        cleanMonitorList();
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2) {
                    String tagName = parser.getName();
                    if (this.mDebugSwitch) {
                        Log.i("ColorAppStartupManager", " readFromFileLocked tagName=" + tagName);
                    }
                    if (CALL_CHECK_COUNT.equals(tagName)) {
                        String checkCount = parser.nextText();
                        if (!checkCount.equals("")) {
                            setCallCheckCount(checkCount);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked checkCount = " + checkCount);
                            }
                        }
                    } else if (COLLECT_APP_START_PKG.equals(tagName)) {
                        String monitorApp = parser.nextText();
                        if (!monitorApp.equals("")) {
                            this.mCollectAppStartList.add(monitorApp);
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readFromFileLocked collect app pkg = " + monitorApp);
                            }
                        }
                    }
                }
            } while (type != 1);
            try {
                stream2.close();
                return;
            } catch (IOException e) {
                e = e;
                sb = new StringBuilder();
            }
            sb.append("Failed to close state FileInputStream ");
            sb.append(e);
            Log.e("ColorAppStartupManager", sb.toString());
        } catch (NullPointerException e2) {
            Log.e("ColorAppStartupManager", "failed parsing ", e2);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (NumberFormatException e4) {
            Log.e("ColorAppStartupManager", "failed parsing ", e4);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e6) {
            Log.e("ColorAppStartupManager", "failed parsing ", e6);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    e = e7;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e8) {
            Log.e("ColorAppStartupManager", "failed IOException ", e8);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e9) {
                    e = e9;
                    sb = new StringBuilder();
                }
            }
        } catch (IndexOutOfBoundsException e10) {
            Log.e("ColorAppStartupManager", "failed parsing ", e10);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e11) {
                    e = e11;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e12) {
                    Log.e("ColorAppStartupManager", "Failed to close state FileInputStream " + e12);
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void cleanMonitorList() {
        if (!this.mCollectAppStartList.isEmpty()) {
            this.mCollectAppStartList.clear();
        }
    }

    private void readCustomizeWhiteList() {
        StringBuilder sb;
        int type;
        String pkgName;
        if (this.mDebugSwitch) {
            Log.i("ColorAppStartupManager", "readCustomizeWhiteList start");
        }
        File customizeFile = new File(OPPO_CUSTOMIZE_WHITE_FILE_PATH);
        if (customizeFile.exists()) {
            cleanCustomizeList();
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(customizeFile);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                do {
                    type = parser.next();
                    if (type == 2 && ColorAppCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName()) && (pkgName = parser.getAttributeValue(null, "att")) != null) {
                        this.mCustomizeWhiteList.add(pkgName);
                    }
                } while (type != 1);
                try {
                    stream2.close();
                    return;
                } catch (IOException e) {
                    e = e;
                    sb = new StringBuilder();
                }
            } catch (NullPointerException e2) {
                Log.e("ColorAppStartupManager", "failed parsing ", e2);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e3) {
                        e = e3;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (NumberFormatException e4) {
                Log.e("ColorAppStartupManager", "failed parsing ", e4);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e5) {
                        e = e5;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (XmlPullParserException e6) {
                Log.e("ColorAppStartupManager", "failed parsing ", e6);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e7) {
                        e = e7;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IOException e8) {
                Log.e("ColorAppStartupManager", "failed IOException ", e8);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e9) {
                        e = e9;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (IndexOutOfBoundsException e10) {
                Log.e("ColorAppStartupManager", "failed parsing ", e10);
                if (0 != 0) {
                    try {
                        stream.close();
                        return;
                    } catch (IOException e11) {
                        e = e11;
                        sb = new StringBuilder();
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e12) {
                        Log.e("ColorAppStartupManager", "Failed to close state FileInputStream " + e12);
                    }
                }
                throw th;
            }
        } else if (this.mDebugSwitch) {
            Log.e("ColorAppStartupManager", "readCustomizeWhiteList failed: file doesn't exist!");
            return;
        } else {
            return;
        }
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Log.e("ColorAppStartupManager", sb.toString());
    }

    /* access modifiers changed from: protected */
    public void cleanCustomizeList() {
        if (!this.mCustomizeWhiteList.isEmpty()) {
            this.mCustomizeWhiteList.clear();
        }
    }

    public void readAssociateStartWhiteFile(int userId) {
        if (this.mDebugSwitch) {
            Log.i("ColorAppStartupManager", "readAutoBootListFile start, userId=" + userId);
        }
        List<String> associateList = new ArrayList<>();
        InputStream is = null;
        InputStreamReader isReader = null;
        BufferedReader reader = null;
        try {
            InputStream is2 = ColorSettings.readConfigAsUser(this.mContext, OPPO_ASSOCIATE_START_WHITE_FILE_PATH_PART, userId, 0);
            InputStreamReader isReader2 = new InputStreamReader(is2);
            BufferedReader reader2 = new BufferedReader(isReader2);
            while (true) {
                String strT = reader2.readLine();
                if (strT != null) {
                    associateList.add(strT.trim());
                } else {
                    try {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            reader2.close();
            isReader2.close();
            if (is2 != null) {
                is2.close();
            }
        } catch (Exception e2) {
            associateList.clear();
            Log.e("ColorAppStartupManager", "associateStartFile read execption: " + e2);
            if (0 != 0) {
                reader.close();
            }
            if (0 != 0) {
                isReader.close();
            }
            if (0 != 0) {
                is.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (Exception e3) {
                    e3.printStackTrace();
                    throw th;
                }
            }
            if (0 != 0) {
                isReader.close();
            }
            if (0 != 0) {
                is.close();
            }
            throw th;
        }
        if (!associateList.isEmpty()) {
            synchronized (this.mAssociateStartWhiteListContainer) {
                List<String> list = getAssociateStartWhiteListNotNull(userId);
                if (!list.isEmpty()) {
                    list.clear();
                }
                list.addAll(associateList);
            }
        }
    }

    /* access modifiers changed from: private */
    public class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event != 8) {
                return;
            }
            if (this.mFocusPath.equals(ColorAppStartupManagerUtils.OPPO_STARTUP_MANAGER_FILE_PATH)) {
                Log.i("ColorAppStartupManager", "focusPath OPPO_STARTUP_MANAGER_FILE_PATH!");
                OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).startupManagerFileChange();
            } else if (this.mFocusPath.equals(ColorAppStartupManagerUtils.OPPO_STARTUP_MONITOR_FILE_PATH)) {
                Log.i("ColorAppStartupManager", "focusPath OPPO_STARTUP_MONITOR_FILE_PATH!");
                OppoFeatureCache.get(IColorAppStartupManager.DEFAULT).startupMonitorFileChange();
            } else if (this.mFocusPath.equals(ColorAppStartupManagerUtils.OPPO_GAME_PAY_MONITOR_CONFIG_LIST_FILE)) {
                Log.i("ColorAppStartupManager", "focusPath OPPO_GAME_PAY_MONITOR_CONFIG_FILE_PATH!");
                ColorAppStartupManagerUtils.this.readGamePayMonitorConfigFile();
            }
        }
    }

    public boolean isForumVersion() {
        String ver = SystemProperties.get("ro.build.version.opporom");
        if (ver == null) {
            return false;
        }
        String ver2 = ver.toLowerCase();
        if (ver2.endsWith("alpha") || ver2.endsWith("beta")) {
            return true;
        }
        return false;
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDebugSwitch = this.mDegugDetail | on;
    }

    /* JADX WARNING: Removed duplicated region for block: B:68:0x0131 A[SYNTHETIC, Splitter:B:68:0x0131] */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0143 A[SYNTHETIC, Splitter:B:74:0x0143] */
    /* JADX WARNING: Removed duplicated region for block: B:83:? A[RETURN, SYNTHETIC] */
    public void handleAppStartForbidden(String packageName) {
        Exception e;
        IOException e2;
        StringBuilder sb;
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(new File(OPPO_ROM_BLACK_LIST_FILE));
            XmlPullParser parser = Xml.newPullParser();
            String str = null;
            parser.setInput(stream, null);
            String value = null;
            String currentLanguage = getCurrentLanguage();
            boolean isPopup = false;
            String contentText = null;
            while (true) {
                int type = parser.next();
                String tagName = parser.getName();
                if (type == 2) {
                    if (this.mDebugSwitch) {
                        Log.i("ColorAppStartupManager", " readRomBlackListFile tagName=" + tagName);
                    }
                    if ("type".equals(tagName)) {
                        value = parser.getAttributeValue(str, "value");
                        if (this.mDebugSwitch) {
                            Log.i("ColorAppStartupManager", " readRomBlackListFile value=" + value);
                        }
                    } else if ("startForbidden".equals(tagName)) {
                        String pkgName = parser.nextText();
                        try {
                            if (packageName.equals(pkgName)) {
                                isPopup = true;
                            }
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readRomBlackListFile pkgName=" + pkgName);
                            }
                        } catch (Exception e3) {
                            e = e3;
                            try {
                                Log.e("ColorAppStartupManager", "failed parsing ", e);
                                if (stream == null) {
                                    try {
                                        stream.close();
                                        return;
                                    } catch (IOException e4) {
                                        e2 = e4;
                                        sb = new StringBuilder();
                                    }
                                } else {
                                    return;
                                }
                            } catch (Throwable th) {
                                th = th;
                                if (stream != null) {
                                }
                                throw th;
                            }
                        }
                    }
                    if (isPopup && currentLanguage != null) {
                        if (currentLanguage.equals(tagName)) {
                            String content = parser.nextText();
                            if (!content.equals("")) {
                                contentText = content;
                                break;
                            }
                        } else if ("en-US".equals(tagName)) {
                            String content2 = parser.nextText();
                            if (!content2.equals("")) {
                                contentText = content2;
                            }
                        }
                    }
                } else if (type == 3 && value != null && value.equals(tagName)) {
                    isPopup = false;
                }
                if (type == 1) {
                    break;
                }
                str = null;
            }
            if (contentText != null) {
                if (this.mDebugSwitch) {
                    Log.i("ColorAppStartupManager", " readRomBlackListFile contentText=" + contentText);
                }
                parseForbidText(contentText);
            }
            try {
                stream.close();
                return;
            } catch (IOException e5) {
                e2 = e5;
                sb = new StringBuilder();
            }
            sb.append("Failed to close state FileInputStream ");
            sb.append(e2);
            Log.e("ColorAppStartupManager", sb.toString());
        } catch (Exception e6) {
            e = e6;
            Log.e("ColorAppStartupManager", "failed parsing ", e);
            if (stream == null) {
            }
        } catch (Throwable th2) {
            th = th2;
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    Log.e("ColorAppStartupManager", "Failed to close state FileInputStream " + e7);
                }
            }
            throw th;
        }
    }

    private String getCurrentLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= 24) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        if (locale == null) {
            return null;
        }
        return locale.getLanguage() + "-" + locale.getCountry();
    }

    private void parseForbidText(String text) {
        int secondIndex;
        int length = text.length();
        int firstIndex = text.indexOf(GAME_PAY_ABNORMAL_RULE_SEPARATOR);
        if (firstIndex >= 0 && firstIndex + 1 < length && (secondIndex = text.indexOf(GAME_PAY_ABNORMAL_RULE_SEPARATOR, firstIndex + 1)) >= 0 && secondIndex + 1 < length) {
            this.mDialogTitleText = text.substring(0, firstIndex);
            this.mDialogContentText = text.substring(firstIndex + 1, secondIndex);
            this.mDialogButtonText = text.substring(secondIndex + 1, text.length());
        }
    }

    public String getDialogTitleText() {
        String str = this.mDialogTitleText;
        if (str == null || str.length() <= 0) {
            return null;
        }
        return this.mDialogTitleText;
    }

    public String getDialogContentText() {
        String str = this.mDialogContentText;
        if (str == null || str.length() <= 0) {
            return null;
        }
        return this.mDialogContentText;
    }

    public String getDialogButtonText() {
        String str = this.mDialogButtonText;
        if (str == null || str.length() <= 0) {
            return null;
        }
        return this.mDialogButtonText;
    }

    public void resetDialogShowText() {
        this.mDialogTitleText = null;
        this.mDialogContentText = null;
        this.mDialogButtonText = null;
    }

    public boolean getCpnUpperLimitSwitch() {
        return this.mCpnUpperLimitSwitch;
    }

    public int getCpnUpperLimitThresholdConnected() {
        return this.mCpnUpperLimitThresholdConnected;
    }

    public int getCpnUpperLimitCountInterval() {
        return this.mCpnUpperLimitCountInterval;
    }

    public int getCpnUpperLimitCountStep() {
        return this.mCpnUpperLimitCountStep;
    }

    public long getCpnUpperLimitThresholdTime() {
        return this.mCpnUpperLimitThresholdTime;
    }

    public boolean getExpFeature() {
        return this.isExpVersion;
    }

    /* access modifiers changed from: protected */
    public List<String> getExpGlobalBlackFile() {
        return this.mExpGlobalBlackList;
    }

    private void readExpGlobalBlackFile() {
        List<String> exGlobalBlackList = readCommonConfigFile(EXP_GLOBAL_BLACK_LIST_FILE_PATH);
        if (exGlobalBlackList != null) {
            this.mExpGlobalBlackList.clear();
            this.mExpGlobalBlackList.addAll(exGlobalBlackList);
        }
    }

    private List<String> readCommonConfigFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e("ColorAppStartupManager", filePath + " file isn't exist!");
            return null;
        }
        if (this.mDebugSwitch) {
            Log.i("ColorAppStartupManager", "readFile " + filePath);
        }
        List<String> fileList = new ArrayList<>();
        FileReader fr = null;
        try {
            FileReader fr2 = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr2);
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    try {
                        break;
                    } catch (Exception e) {
                        Log.e("ColorAppStartupManager", filePath + " close failed! " + e);
                    }
                } else if (!TextUtils.isEmpty(line)) {
                    fileList.add(line.trim());
                }
            }
            fr2.close();
            if (0 == 0) {
                return fileList;
            }
            return null;
        } catch (Exception e2) {
            Log.e("ColorAppStartupManager", " read file " + filePath + " failed! " + e2);
            if (0 != 0) {
                try {
                    fr.close();
                } catch (Exception e3) {
                    Log.e("ColorAppStartupManager", filePath + " close failed! " + e3);
                }
            }
            if (1 == 0) {
                return fileList;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fr.close();
                } catch (Exception e4) {
                    Log.e("ColorAppStartupManager", filePath + " close failed! " + e4);
                }
            }
            if (1 == 0) {
                return fileList;
            }
            throw th;
        }
    }

    public boolean getGamePayMonitorSwitch() {
        boolean z;
        synchronized (this.mGamePayLock) {
            z = this.mGamePayMonitorSwitch;
        }
        return z;
    }

    public int getGamePayMonitorCheckCount() {
        int i;
        synchronized (this.mGamePayLock) {
            i = this.mGamePayMonitorCheckCount;
        }
        return i;
    }

    public List<String> getGameMonitorPackageList() {
        List<String> list;
        synchronized (this.mGamePayLock) {
            list = this.mGameMonitorPackageList;
        }
        return list;
    }

    public List<String> getGamePayMonitorCpnList() {
        List<String> list;
        synchronized (this.mGamePayLock) {
            list = this.mGamePayMonitorCpnList;
        }
        return list;
    }

    public List<String> getGamePayMonitorFuzzyCpnList() {
        List<String> list;
        synchronized (this.mGamePayLock) {
            list = this.mGamePayMonitorFuzzyCpnList;
        }
        return list;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readGamePayMonitorConfigFile() {
        String str;
        String str2;
        char c;
        String[] splits;
        synchronized (this.mGamePayLock) {
            if (this.mDebugSwitch) {
                Log.i("ColorAppStartupManager", "readGamePayMonitorConfigFile start");
            }
            File gamePayMonitorConfigFile = new File(OPPO_GAME_PAY_MONITOR_CONFIG_LIST_FILE);
            if (!gamePayMonitorConfigFile.exists()) {
                if (this.mDebugSwitch) {
                    Log.e("ColorAppStartupManager", "readGamePayMonitorConfigFile failed: file doesn't exist!");
                }
                return;
            }
            char c2 = 0;
            this.mGamePayMonitorSwitch = false;
            int i = 2;
            this.mGamePayMonitorCheckCount = 2;
            this.mGameMonitorPackageList.clear();
            this.mGamePayMonitorCpnList.clear();
            this.mGamePayMonitorFuzzyCpnList.clear();
            this.mGamePayAbnormalSwitch = false;
            this.mGamePayAbnormalCheckCount = 2;
            this.mGamePayAbnormalCallerWhiteCpnList.clear();
            this.mGamePayAbnormalCallerWhitePkgList.clear();
            this.mGamePayAbnormalCallerWhitePkgCpnMap.clear();
            this.mGamePayAbnormalCallerWhiteKeyList.clear();
            this.mGamePayAbnormalCalledWhitePkgList.clear();
            this.mGamePayAbnormalCalledWhitePkgCpnMap.clear();
            this.mGamePayAbnormalCalledWhiteKeyList.clear();
            this.mGamePayAbnormalCallerBlackPkgList.clear();
            this.mGamePayAbnormalCallerBlackKeyList.clear();
            this.mGamePayAbnormalCalledBlackPkgList.clear();
            this.mGamePayAbnormalCalledBlackFirstCpnMap.clear();
            this.mGamePayAbnormalCalledBlackSecondCpnMap.clear();
            this.mGamePayAbnormalBlackUrlKeyList.clear();
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(gamePayMonitorConfigFile);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                while (true) {
                    int type = parser.next();
                    if (type == i) {
                        String tagName = parser.getName();
                        if (this.mDebugSwitch) {
                            Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile tagName=" + tagName);
                        }
                        if (GAME_MONITOR_PACKAGE_NAME.equals(tagName)) {
                            String gameMonitorPkg = parser.nextText();
                            if (!TextUtils.isEmpty(gameMonitorPkg)) {
                                this.mGameMonitorPackageList.add(gameMonitorPkg);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gameMPkg = " + gameMonitorPkg);
                                }
                            }
                            c = c2;
                        } else if (GAME_PAY_MONITOR_COMPONENT_NAME.equals(tagName)) {
                            String gamePayMonitorCpn = parser.nextText();
                            if (!TextUtils.isEmpty(gamePayMonitorCpn)) {
                                this.mGamePayMonitorCpnList.add(gamePayMonitorCpn);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayCpn = " + gamePayMonitorCpn);
                                }
                            }
                            c = c2;
                        } else if (GAME_PAY_MONITOR_FUZZY_COMPONENT_NAME.equals(tagName)) {
                            String gamePayMonitorFuzzyCpn = parser.nextText();
                            if (!TextUtils.isEmpty(gamePayMonitorFuzzyCpn)) {
                                this.mGamePayMonitorFuzzyCpnList.add(gamePayMonitorFuzzyCpn);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayMFuzzyCpn = " + gamePayMonitorFuzzyCpn);
                                }
                            }
                            c = c2;
                        } else if (GAME_MONITOR_SWITCH_NAME.equals(tagName)) {
                            String gPayMonitorSwitch = parser.nextText();
                            if (!TextUtils.isEmpty(gPayMonitorSwitch)) {
                                this.mGamePayMonitorSwitch = Boolean.parseBoolean(gPayMonitorSwitch);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayMSwitch = " + gPayMonitorSwitch);
                                }
                            }
                            c = c2;
                        } else if (GAME_MONITOR_CHECKCOUNT_NAME.equals(tagName)) {
                            String gamePayMonitorCheckCount = parser.nextText();
                            if (!TextUtils.isEmpty(gamePayMonitorCheckCount)) {
                                this.mGamePayMonitorCheckCount = Integer.parseInt(gamePayMonitorCheckCount);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayMCheckCount = " + this.mGamePayMonitorCheckCount);
                                }
                            }
                            c = c2;
                        } else if (GAME_PAY_ABNORMAL_SWITCH.equals(tagName)) {
                            String abnormalSwitch = parser.nextText();
                            if (!TextUtils.isEmpty(abnormalSwitch)) {
                                this.mGamePayAbnormalSwitch = Boolean.parseBoolean(abnormalSwitch);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayAbnormalSwitch = " + abnormalSwitch);
                                }
                            }
                            c = c2;
                        } else if (GAME_PAY_ABNORMAL_CHECK_COUNT.equals(tagName)) {
                            String checkCount = parser.nextText();
                            if (!TextUtils.isEmpty(checkCount)) {
                                this.mGamePayAbnormalCheckCount = Integer.parseInt(checkCount);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayAbnormalCheckCount = " + checkCount);
                                }
                            }
                            c = c2;
                        } else if (GAME_PAY_ABNORMAL_CALLER_WHITE_PKG_CPN.equals(tagName)) {
                            String callerWhitePkgCpn = parser.nextText();
                            if (!TextUtils.isEmpty(callerWhitePkgCpn)) {
                                String[] splits2 = callerWhitePkgCpn.split(GAME_PAY_ABNORMAL_RULE_SEPARATOR);
                                if (splits2 != null) {
                                    if (splits2.length == 1) {
                                        this.mGamePayAbnormalCallerWhitePkgList.add(callerWhitePkgCpn);
                                    } else if (splits2.length == i) {
                                        List<String> cpnList = this.mGamePayAbnormalCallerWhitePkgCpnMap.get(splits2[c2]);
                                        if (cpnList == null) {
                                            cpnList = new ArrayList();
                                        }
                                        cpnList.add(splits2[1]);
                                        this.mGamePayAbnormalCallerWhitePkgCpnMap.put(splits2[c2], cpnList);
                                    }
                                }
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayAbnormalCallerWhitePkgCpn = " + callerWhitePkgCpn);
                                }
                            }
                            c = c2;
                        } else if (GAME_PAY_ABNORMAL_CALLER_WHITE_KEY.equals(tagName)) {
                            String callerWhiteKey = parser.nextText();
                            if (!TextUtils.isEmpty(callerWhiteKey)) {
                                this.mGamePayAbnormalCallerWhiteKeyList.add(callerWhiteKey);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayAbnormalCallerWhiteKey = " + callerWhiteKey);
                                }
                            }
                            c = c2;
                        } else if (GAME_PAY_ABNORMAL_CALLED_WHITE_PKG_CPN.equals(tagName)) {
                            String calledWhitePkgCpn = parser.nextText();
                            if (!TextUtils.isEmpty(calledWhitePkgCpn) && (splits = calledWhitePkgCpn.split(GAME_PAY_ABNORMAL_RULE_SEPARATOR)) != null) {
                                if (splits.length == 1) {
                                    this.mGamePayAbnormalCalledWhitePkgList.add(splits[c2]);
                                } else if (splits.length == i) {
                                    List<String> cpnWhiteList = this.mGamePayAbnormalCalledWhitePkgCpnMap.get(splits[c2]);
                                    if (cpnWhiteList == null) {
                                        cpnWhiteList = new ArrayList();
                                    }
                                    cpnWhiteList.add(splits[1]);
                                    this.mGamePayAbnormalCalledWhitePkgCpnMap.put(splits[c2], cpnWhiteList);
                                }
                            }
                            if (this.mDebugSwitch) {
                                Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayAbnormalCalledWhitePkgCpn = " + calledWhitePkgCpn);
                            }
                            c = c2;
                        } else if (GAME_PAY_ABNORMAL_CALLED_WHITE_KEY.equals(tagName)) {
                            String calledWhiteKey = parser.nextText();
                            if (!TextUtils.isEmpty(calledWhiteKey)) {
                                this.mGamePayAbnormalCalledWhiteKeyList.add(calledWhiteKey);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayAbnormalCalledWhiteKey = " + calledWhiteKey);
                                }
                            }
                            c = c2;
                        } else if (GAME_PAY_ABNORMAL_CALLER_BLACK_PKG.equals(tagName)) {
                            String callerBlackPkg = parser.nextText();
                            if (!TextUtils.isEmpty(callerBlackPkg)) {
                                this.mGamePayAbnormalCallerBlackPkgList.add(callerBlackPkg);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayAbnormalCallerBlackPkg = " + callerBlackPkg);
                                }
                            }
                            c = c2;
                        } else if (GAME_PAY_ABNORMAL_CALLER_BALCK_KEY.equals(tagName)) {
                            String callerBlackKey = parser.nextText();
                            if (!TextUtils.isEmpty(callerBlackKey)) {
                                this.mGamePayAbnormalCallerBlackKeyList.add(callerBlackKey);
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayAbnormalCallerBlackKey = " + callerBlackKey);
                                }
                            }
                            c = c2;
                        } else if (GAME_PAY_ABNORMAL_CALLED_BLACK_PKG_CPN.equals(tagName)) {
                            String payPkgCpn = parser.nextText();
                            if (!TextUtils.isEmpty(payPkgCpn)) {
                                String[] splits3 = payPkgCpn.split(GAME_PAY_ABNORMAL_RULE_SEPARATOR);
                                if (splits3.length == 1) {
                                    this.mGamePayAbnormalCalledBlackPkgList.add(splits3[c2]);
                                    c = c2;
                                } else if (splits3.length == i) {
                                    if (this.mGamePayAbnormalCalledBlackFirstCpnMap.containsKey(splits3[c2])) {
                                        List<String> firstCpnList = this.mGamePayAbnormalCalledBlackFirstCpnMap.get(splits3[c2]);
                                        if (firstCpnList == null) {
                                            firstCpnList = new ArrayList();
                                        }
                                        firstCpnList.add(splits3[1]);
                                        this.mGamePayAbnormalCalledBlackFirstCpnMap.put(splits3[c2], firstCpnList);
                                        c = c2;
                                    } else {
                                        List<String> firstCpnList2 = new ArrayList<>();
                                        firstCpnList2.add(splits3[1]);
                                        this.mGamePayAbnormalCalledBlackFirstCpnMap.put(splits3[c2], firstCpnList2);
                                        c = c2;
                                    }
                                } else if (splits3.length != 3) {
                                    c = c2;
                                } else if (this.mGamePayAbnormalCalledBlackSecondCpnMap.containsKey(splits3[c2])) {
                                    ArrayMap<String, List<String>> secondCpnMap = this.mGamePayAbnormalCalledBlackSecondCpnMap.get(splits3[c2]);
                                    if (secondCpnMap == null) {
                                        secondCpnMap = new ArrayMap<>();
                                    }
                                    List<String> secondCpnList = secondCpnMap.get(splits3[1]);
                                    if (secondCpnList == null) {
                                        secondCpnList = new ArrayList();
                                    }
                                    secondCpnList.add(splits3[i]);
                                    secondCpnMap.put(splits3[1], secondCpnList);
                                    this.mGamePayAbnormalCalledBlackSecondCpnMap.put(splits3[0], secondCpnMap);
                                    c = 0;
                                } else {
                                    ArrayMap<String, List<String>> secondCpnMap2 = new ArrayMap<>();
                                    ArrayList secondCpnList2 = new ArrayList();
                                    secondCpnList2.add(splits3[i]);
                                    secondCpnMap2.put(splits3[1], secondCpnList2);
                                    c = 0;
                                    this.mGamePayAbnormalCalledBlackSecondCpnMap.put(splits3[0], secondCpnMap2);
                                }
                                if (this.mDebugSwitch) {
                                    Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayAbnormalBlackPayPkgCpn = " + payPkgCpn);
                                }
                            } else {
                                c = c2;
                            }
                        } else {
                            c = c2;
                            if (GAME_PAY_ABNORMAL_BLACK_URL_KEY.equals(tagName)) {
                                String blackUrlKey = parser.nextText();
                                if (!TextUtils.isEmpty(blackUrlKey)) {
                                    this.mGamePayAbnormalBlackUrlKeyList.add(blackUrlKey);
                                    if (this.mDebugSwitch) {
                                        Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayAbnormalCallerBlackKey = " + blackUrlKey);
                                    }
                                }
                            } else if (GAME_PAY_ABNORMAL_CALLER_WHITE_CPN.equals(tagName)) {
                                String callerWhiteCpn = parser.nextText();
                                if (!TextUtils.isEmpty(callerWhiteCpn)) {
                                    this.mGamePayAbnormalCallerWhiteCpnList.add(callerWhiteCpn);
                                    if (this.mDebugSwitch) {
                                        Log.i("ColorAppStartupManager", " readGamePayMonitorConfigFile gamePayAbnormalCallerWhiteCpn = " + callerWhiteCpn);
                                    }
                                }
                            }
                        }
                    } else {
                        c = c2;
                    }
                    if (type == 1) {
                        try {
                            break;
                        } catch (IOException e) {
                            str = "ColorAppStartupManager";
                            str2 = "Failed to close state FileInputStream " + e;
                        }
                    } else {
                        c2 = c;
                        i = 2;
                    }
                }
                stream2.close();
            } catch (NullPointerException e2) {
                Log.e("ColorAppStartupManager", "failed parsing ", e2);
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e3) {
                        str = "ColorAppStartupManager";
                        str2 = "Failed to close state FileInputStream " + e3;
                    }
                }
            } catch (NumberFormatException e4) {
                Log.e("ColorAppStartupManager", "failed parsing ", e4);
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e5) {
                        str = "ColorAppStartupManager";
                        str2 = "Failed to close state FileInputStream " + e5;
                    }
                }
            } catch (XmlPullParserException e6) {
                Log.e("ColorAppStartupManager", "failed parsing ", e6);
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e7) {
                        str = "ColorAppStartupManager";
                        str2 = "Failed to close state FileInputStream " + e7;
                    }
                }
            } catch (IOException e8) {
                Log.e("ColorAppStartupManager", "failed IOException ", e8);
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e9) {
                        str = "ColorAppStartupManager";
                        str2 = "Failed to close state FileInputStream " + e9;
                    }
                }
            } catch (IndexOutOfBoundsException e10) {
                Log.e("ColorAppStartupManager", "failed parsing ", e10);
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e11) {
                        str = "ColorAppStartupManager";
                        str2 = "Failed to close state FileInputStream " + e11;
                    }
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        stream.close();
                    } catch (IOException e12) {
                        Log.e("ColorAppStartupManager", "Failed to close state FileInputStream " + e12);
                    }
                }
                throw th;
            }
        }
        Log.e(str, str2);
    }

    /* access modifiers changed from: protected */
    public List<String> getRestartServiceWhiteList() {
        return this.mRestartServiceWhiteList;
    }

    /* access modifiers changed from: protected */
    public List<String> getRestartServiceInRecentList() {
        ArrayList<String> list = new ArrayList<>();
        List<String> list2 = this.mRestartServiceInRecentList;
        if (list2 == null || list2.isEmpty()) {
            Context context = this.mContext;
            if (context == null || !context.getPackageManager().hasSystemFeature("oppo.version.exp")) {
                list.addAll(mDefaultRestartServiceWhiteList);
            } else {
                list.addAll(mDefaultRestartServiceWhiteListExp);
            }
        } else {
            list.addAll(this.mRestartServiceWhiteList);
        }
        return list;
    }

    public List<String> getGamePayAbnormalCallerWhitePkgList() {
        List<String> list;
        synchronized (this.mGamePayLock) {
            list = this.mGamePayAbnormalCallerWhitePkgList;
        }
        return list;
    }

    public List<String> getGamePayAbnormalCallerWhiteCpnList() {
        List<String> list;
        synchronized (this.mGamePayLock) {
            list = this.mGamePayAbnormalCallerWhiteCpnList;
        }
        return list;
    }

    public ArrayMap<String, List<String>> getGamePayAbnormalCallerWhitePkgCpnMap() {
        ArrayMap<String, List<String>> arrayMap;
        synchronized (this.mGamePayLock) {
            arrayMap = this.mGamePayAbnormalCallerWhitePkgCpnMap;
        }
        return arrayMap;
    }

    public List<String> getGamePayAbnormalCallerWhiteKeyList() {
        List<String> list;
        synchronized (this.mGamePayLock) {
            list = this.mGamePayAbnormalCallerWhiteKeyList;
        }
        return list;
    }

    public List<String> getGamePayAbnormalCalledWhitePkgList() {
        List<String> list;
        synchronized (this.mGamePayLock) {
            list = this.mGamePayAbnormalCalledWhitePkgList;
        }
        return list;
    }

    public ArrayMap<String, List<String>> getGamePayAbnormalCalledWhitePkgCpnMap() {
        ArrayMap<String, List<String>> arrayMap;
        synchronized (this.mGamePayLock) {
            arrayMap = this.mGamePayAbnormalCalledWhitePkgCpnMap;
        }
        return arrayMap;
    }

    public List<String> getGamePayAbnormalCalledWhiteKeyList() {
        List<String> list;
        synchronized (this.mGamePayLock) {
            list = this.mGamePayAbnormalCalledWhiteKeyList;
        }
        return list;
    }

    public List<String> getGamePayAbnormalCallerBlackPkgList() {
        List<String> list;
        synchronized (this.mGamePayLock) {
            list = this.mGamePayAbnormalCallerBlackPkgList;
        }
        return list;
    }

    public List<String> getGamePayAbnormalCallerBlackKeyList() {
        List<String> list;
        synchronized (this.mGamePayLock) {
            list = this.mGamePayAbnormalCallerBlackKeyList;
        }
        return list;
    }

    public List<String> getGamePayAbnormalCalledBlackPkgList() {
        List<String> list;
        synchronized (this.mGamePayLock) {
            list = this.mGamePayAbnormalCalledBlackPkgList;
        }
        return list;
    }

    public ArrayMap<String, List<String>> getGamePayAbnormalCalledBlackFirstCpnMap() {
        ArrayMap<String, List<String>> arrayMap;
        synchronized (this.mGamePayLock) {
            arrayMap = this.mGamePayAbnormalCalledBlackFirstCpnMap;
        }
        return arrayMap;
    }

    public ArrayMap<String, ArrayMap<String, List<String>>> getGamePayAbnormalCalledBlackSecondCpnMap() {
        ArrayMap<String, ArrayMap<String, List<String>>> arrayMap;
        synchronized (this.mGamePayLock) {
            arrayMap = this.mGamePayAbnormalCalledBlackSecondCpnMap;
        }
        return arrayMap;
    }

    public boolean getGamePayAbnormalSwitch() {
        boolean z;
        synchronized (this.mGamePayLock) {
            z = this.mGamePayAbnormalSwitch;
        }
        return z;
    }

    public int getGamePayAbnormalCheckCount() {
        int i;
        synchronized (this.mGamePayLock) {
            i = this.mGamePayAbnormalCheckCount;
        }
        return i;
    }

    public List<String> getmGamePayAbnormalBlackUrlKeyList() {
        List<String> list;
        synchronized (this.mGamePayLock) {
            list = this.mGamePayAbnormalBlackUrlKeyList;
        }
        return list;
    }

    public Bundle getBootBroadcastOptimizeConfig() {
        this.isNeedReadConfig = false;
        Bundle bundle = new Bundle();
        bundle.putBoolean("switch", this.isBootBroadcastOptimizeEnable);
        bundle.putBoolean("sysAppDelay", this.isSysAppDelayEnable);
        bundle.putBoolean("thirdAppDelay", this.isThirdAppDelayEnable);
        bundle.putInt("queueMaxNum", this.mQueueMaxNum);
        bundle.putLong("shortDelayAppDispatchTime", this.mShortDelayAppDispatchTime);
        bundle.putLong("sysAppDispatchTime", this.mSysAppDispatchTime);
        bundle.putLong("thirdAppDispatchTime", this.mThirdAppDispatchTime);
        bundle.putLong("dispatchTimePeriod", this.mDispatchTimePeriod);
        bundle.putStringArrayList("noDelayApp", new ArrayList<>(this.mNoDelayList));
        bundle.putStringArrayList("shortDelayApp", new ArrayList<>(this.mShortDelayList));
        bundle.putStringArrayList("sysShortDelayApp", new ArrayList<>(this.mSysShortDelayList));
        bundle.putStringArrayList("sysLongDelayApp", new ArrayList<>(this.mSysLongDelayList));
        if (!this.mNoDelayList.isEmpty()) {
            this.mNoDelayList.clear();
        }
        if (!this.mShortDelayList.isEmpty()) {
            this.mShortDelayList.clear();
        }
        if (!this.mSysShortDelayList.isEmpty()) {
            this.mSysShortDelayList.clear();
        }
        if (!this.mSysLongDelayList.isEmpty()) {
            this.mSysLongDelayList.clear();
        }
        return bundle;
    }
}
