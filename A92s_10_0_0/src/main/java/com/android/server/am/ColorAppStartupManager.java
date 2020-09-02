package com.android.server.am;

import android.app.ApplicationErrorReport;
import android.app.BroadcastOptions;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.server.am.ColorAppStartupStatistics;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.pm.IColorFullmodeManager;
import com.android.server.wm.ColorAppStartupManagerHelper;
import com.android.server.wm.IColorActivityRecordEx;
import com.android.server.wm.IColorAppStoreTraffic;
import com.android.server.wm.IColorAppSwitchManager;
import com.color.util.ColorAccessibilityUtil;
import com.color.util.ColorCommonConfig;
import com.color.util.ColorTypeCastingHelper;
import com.oppo.app.IOppoAppStartController;
import com.oppo.hypnus.Hypnus;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import oppo.util.OppoStatistics;

public class ColorAppStartupManager implements IColorAppStartupManager {
    private static final String ACTION_OPPO_STARTUP_APP_MONITOR = "oppo.intent.action.OPPO_STARTUP_APP_MONITOR";
    public static final List<String> BROWSER_LIST = Arrays.asList("com.android.browser", "com.coloros.browser", "com.heytap.browser");
    private static final String CALL_ARG_CRASH = "crash";
    private static final String CALL_ARG_STARTUP = "startup";
    private static final String COLOROS_ACTION_INTERCEPT_REVIEW = "coloros.intent.action.COLOROS_INTERCEPT_REVIEW";
    private static final String COLOROS_EXTRA_INTENT = "coloros.intent.extra.COLOROS_INTENT";
    private static final String COLOROS_EXTRA_INTENT_SENDER = "coloros.intent.extra.COLOROS_INTENT_SENDER";
    private static final String COLOROS_EXTRA_SOURCE_NAME = "coloros.intent.extra.COLOROS_SOURCE_NAME";
    private static final String COLOROS_EXTRA_TARGET_NAME = "coloros.intent.extra.COLOROS_TARGET_NAME";
    private static final String COLOR_EXTRA_RESULT_NEEDED = "coloros.intent.extra.COLOROS_RESULT_NEEDED";
    private static final String EX_GLOBAL_BLACK_LIST = "ex_global_black_list";
    private static final String GAME_PAY_ABNORMAL_FLAG = "gamePayAbnormalFlag";
    private static final String GAME_PAY_MONITOR_FLAG = "gamePayMonitorFlag";
    private static final int HYPNUS_VALUE = 1000;
    private static final int HYPNUS_VALUE_SERVICE = 4000;
    private static final String INTENT_QUERY_EXTRA = "query";
    private static final int LAST_ACTIVITY_CALL_COUNT = 5;
    private static final String OPPO_COOPERATION_GAME = "nearme.gamecenter";
    private static final String OPPO_SAFE_URL_INTERCEPTION = "oppo.intent.action.OPPO_SAFE_URL_INTERCEPTION";
    private static final String PRE_COLOROS = "com.coloros.";
    private static final String PRE_NEARME = "com.nearme.";
    private static final String PRE_OPPO = "com.oppo.";
    private static final int PR_COUNT_SIZE = 20;
    public static final String REBIND_BIND_DIED = "bindDied";
    private static final String RECENT_LOCK_CONFIG_NAME = "recent_lock_list";
    private static final String RECORD_ACTIVITY_LAUNCH_MODE = "2";
    private static final String RECORD_ALLOW_LAUNCH_TYPE = "0";
    public static final String RECORD_ASSOCIATE_LAUNCH_ALLOW_MODE = "4";
    private static final String RECORD_ASSOCIATE_LAUNCH_MODE = "1";
    public static final String RECORD_AUTO_LAUNCH_ALLOW_MODE = "3";
    private static final String RECORD_AUTO_LAUNCH_MODE = "0";
    private static final String RECORD_CALLER_ANDROID = "Android";
    private static final String RECORD_PREVENT_LAUNCH_TYPE = "1";
    private static final String SYSTEM_UI_PKG = "com.android.systemui";
    private static final String UPLOAD_CPN_UPPTER_LIMIT_EVENTID = "cpn_uppper_limit";
    private static final String UPLOAD_KEY_MAX_CONNECT_INTENT = "service_max_connect_intent";
    private static final String UPLOAD_KEY_PACKAGE_NAME = "package_name";
    private static final String UPLOAD_KEY_PROCESS_NAME = "process_name";
    private static final String UPLOAD_KEY_REMOVE_SERVICE_CONNECTION = "remove_service_connection_time";
    private static final String UPLOAD_KEY_SERVICE_CONNECT_COUNT = "service_connect_count";
    private static final String UPLOAD_KEY_SERVICE_CONNECT_INTERVAL = "service_connect_interval";
    private static final String UPLOAD_KEY_TYPE = "type";
    private static final String UPLOAD_TYPE_REMOVE_SERVICE_CONNECTION = "remove_service_connection";
    private static final String UPLOAD_TYPE_SERVICE_CONNECTED = "service_connected";
    private static final String WX_EVENT = "wx_mini_program_launch";
    private static final String WX_TAG = "20109";
    private static final ArrayMap<String, List<String>> mBlackListReceiver = new ArrayMap<>();
    /* access modifiers changed from: private */
    public static String mGamePayAbnormalCallerCpn;
    /* access modifiers changed from: private */
    public static String mGamePayAbnormalCallerPkg;
    /* access modifiers changed from: private */
    public static String mGamePayAbnormalFirstCpn;
    /* access modifiers changed from: private */
    public static boolean mGamePayAbnormalSecondFlag = false;
    private static ColorAppStartupManager sColorAppStartupManager = null;
    private boolean isExpVersion;
    private List<String> mActionBlackList;
    private final Object mActionLock;
    private ArrayMap<String, List<String>> mActivityAssociateBlackPkgMap;
    private ArrayMap<String, List<String>> mActivityAssociateWhitePkgMap;
    private List<String> mActivityBlackList;
    private List<String> mActivityCalledKeyList;
    private final Object mActivityCalledKeyLock;
    private List<String> mActivityCalledWhiteCpnList;
    private final Object mActivityCalledWhiteCpnLock;
    private List<String> mActivityCalledWhitePkgList;
    private final Object mActivityCalledWhitePkgLock;
    private List<String> mActivityCallerWhitePkgList;
    private final Object mActivityCallerWhitePkgLock;
    private final Object mActivityLock;
    private List<String> mActivityPkgKeyList;
    private final Object mActivityPkgKeyLock;
    private List<String> mActivitySelfCallWhitePkgList;
    private List<String> mActivitySleepCpnBlackList;
    protected ActivityManagerService mAms;
    private ColorAppStartupManagerHelper mAppStartupHelper;
    private SparseArray<List<String>> mAssociateStartWhiteListContainer;
    private final Object mAssociateStartWhiteLock;
    private AudioManager mAudioManager;
    private List<String> mAuthorizeCpnList;
    private final Object mAuthorizeCpnListLock;
    private final Object mBackgroundRestrictCallerPkgLock;
    private ArrayMap<String, List<String>> mBackgroundRestrictCallerPkgMap;
    private final Object mBindServiceCallerPkgLock;
    private ArrayMap<String, List<String>> mBindServiceCallerPkgMap;
    private List<String> mBindServiceCpnWhiteList;
    private final Object mBindServiceWhiteLock;
    private List<String> mBlackguardActivityList;
    private final Object mBlackguardActivityLock;
    private List<String> mBlackguardList;
    private final Object mBlackguardLock;
    private List<String> mBroadcastActionWhiteList;
    private final Object mBroadcastActionWhiteLock;
    private List<String> mBroadcastWhiteList;
    private final Object mBroadcastWhiteLock;
    private List<String> mBuildAppBlackList;
    private final Object mBuildAppBlackLock;
    private List<String> mCollectAppStartList;
    private final Object mCollectAppStartLock;
    /* access modifiers changed from: private */
    public List<ColorAppStartupMonitorInfo> mCollectBlackListInterceptList;
    /* access modifiers changed from: private */
    public List<ColorAppStartupMonitorInfo> mCollectGamePayAbnormalList;
    /* access modifiers changed from: private */
    public List<ColorAppStartupMonitorInfo> mCollectGamePayMonitorList;
    /* access modifiers changed from: private */
    public List<ColorAppStartupMonitorInfo> mCollectProcessInfoList;
    protected IColorActivityManagerServiceEx mColorAmsEx;
    protected IColorActivityManagerServiceInner mColorAmsInner;
    private IOppoAppStartController mController;
    /* access modifiers changed from: private */
    public int mCpnUpperLimitCountInterval;
    private int mCpnUpperLimitCountStep;
    /* access modifiers changed from: private */
    public List<Map<String, String>> mCpnUpperLimitMap;
    private boolean mCpnUpperLimitSwitch;
    /* access modifiers changed from: private */
    public int mCpnUpperLimitThresholdConnected;
    private long mCpnUpperLimitThresholdTime;
    private String mCurrentResumeApp;
    private String mCurrentStartActivityCallerPackage;
    private Intent mCurrentStartActivityIntent;
    private List<String> mCustomizeWhiteList;
    private final Object mCustomizeWhiteLock;
    final BroadcastReceiver mDateChangedReceiver;
    protected boolean mDebugSwitch;
    private final BroadcastReceiver mDefaultAppReceiver;
    /* access modifiers changed from: private */
    public String mDefaultDialerApp;
    /* access modifiers changed from: private */
    public String mDefaultInputMethod;
    private ContentObserver mDufaultInputMethodObserver;
    protected boolean mDynamicDebug;
    private List<String> mGlobalWhiteList;
    private Handler mHandler;
    private Hypnus mHypnus;
    private boolean mIsAllowRecentAppStart;
    private List<String> mJobWhiteList;
    private final Object mJobWhiteLock;
    private ArrayList<ColorAppStartupStatistics.OppoCallActivityEntry> mLastCalledAcivityList;
    protected boolean mMonitorAll;
    private List<ColorAppStartupMonitorInfo> mMonitorAppInfoList;
    private ArrayList<String> mMonitorAppUploadList;
    private List<String> mNotifyWhiteList;
    private final Object mNotifyWhiteLock;
    private Map<ProcessRecord, Integer> mPrCountMap;
    private IOppoAppStartController mPreventIndulgeController;
    private List<String> mPreventIndulgeList;
    private List<String> mProtectList;
    private final Object mProtectLock;
    private List<String> mProviderBlacklist;
    private List<String> mProviderCallerPkgList;
    private final Object mProviderCallerPkgLock;
    private List<String> mProviderCpnWhiteList;
    private final Object mProviderLock;
    private final Object mProviderWhiteLock;
    private List<String> mReceiverActionBlackList;
    private final Object mReceiverActionLock;
    private List<String> mReceiverBlackList;
    private final Object mReceiverLock;
    private final Object mServiceLock;
    private List<String> mSeviceCpnBlacklist;
    private List<String> mStartActivityReasonList;
    private final Object mStartServiceCallerPkgLock;
    private ArrayMap<String, List<String>> mStartServiceCallerPkgMap;
    private List<String> mStartServiceWhiteCpnList;
    private final Object mStartServiceWhiteCpnLock;
    private List<String> mStartServiceWhiteList;
    private final Object mStartServiceWhiteLock;
    protected boolean mSwitch;
    private boolean mSwitchBrowserInterceptUpload;
    protected boolean mSwitchInterceptActivity;
    protected boolean mSwitchMonitor;
    private List<String> mSyncWhiteList;
    private final Object mSyncWhiteLock;

    private ColorAppStartupManager() {
        this.mDynamicDebug = false;
        this.mDebugSwitch = DEBUG_DETAIL | this.mDynamicDebug;
        this.mSwitch = SystemProperties.getBoolean("persist.sys.startupmanager", true);
        this.mSwitchMonitor = false;
        this.mSwitchInterceptActivity = false;
        this.mAms = null;
        this.mColorAmsEx = null;
        this.mColorAmsInner = null;
        this.mMonitorAll = SystemProperties.getBoolean("persist.sys.monitorall", false);
        this.mServiceLock = new Object();
        this.mReceiverLock = new Object();
        this.mReceiverActionLock = new Object();
        this.mProviderLock = new Object();
        this.mActivityLock = new Object();
        this.mActionLock = new Object();
        this.mBlackguardLock = new Object();
        this.mBuildAppBlackLock = new Object();
        this.mStartServiceWhiteLock = new Object();
        this.mStartServiceWhiteCpnLock = new Object();
        this.mBindServiceWhiteLock = new Object();
        this.mJobWhiteLock = new Object();
        this.mSyncWhiteLock = new Object();
        this.mNotifyWhiteLock = new Object();
        this.mProviderWhiteLock = new Object();
        this.mBroadcastWhiteLock = new Object();
        this.mBroadcastActionWhiteLock = new Object();
        this.mProtectLock = new Object();
        this.mAssociateStartWhiteLock = new Object();
        this.mAuthorizeCpnListLock = new Object();
        this.mCollectAppStartLock = new Object();
        this.mActivityCallerWhitePkgLock = new Object();
        this.mActivityCalledWhitePkgLock = new Object();
        this.mActivityCalledWhiteCpnLock = new Object();
        this.mActivityPkgKeyLock = new Object();
        this.mActivityCalledKeyLock = new Object();
        this.mBlackguardActivityLock = new Object();
        this.mCustomizeWhiteLock = new Object();
        this.mProviderCallerPkgLock = new Object();
        this.mBackgroundRestrictCallerPkgLock = new Object();
        this.mStartServiceCallerPkgLock = new Object();
        this.mBindServiceCallerPkgLock = new Object();
        this.mSeviceCpnBlacklist = new ArrayList();
        this.mReceiverBlackList = new ArrayList();
        this.mReceiverActionBlackList = new ArrayList();
        this.mProviderBlacklist = new ArrayList();
        this.mActivityBlackList = new ArrayList();
        this.mActionBlackList = new ArrayList();
        this.mBlackguardList = new ArrayList();
        this.mCollectAppStartList = new ArrayList();
        this.mActivityCallerWhitePkgList = new ArrayList();
        this.mActivityCalledWhitePkgList = new ArrayList();
        this.mActivityCalledWhiteCpnList = new ArrayList();
        this.mActivityPkgKeyList = new ArrayList();
        this.mActivityCalledKeyList = new ArrayList();
        this.mBlackguardActivityList = new ArrayList();
        this.mCustomizeWhiteList = new ArrayList();
        this.mActivitySelfCallWhitePkgList = new ArrayList();
        this.mActivityAssociateWhitePkgMap = new ArrayMap<>();
        this.mActivityAssociateBlackPkgMap = new ArrayMap<>();
        this.mActivitySleepCpnBlackList = new ArrayList();
        this.mBuildAppBlackList = new ArrayList();
        this.mStartServiceWhiteList = new ArrayList();
        this.mStartServiceWhiteCpnList = new ArrayList();
        this.mBindServiceCpnWhiteList = new ArrayList();
        this.mJobWhiteList = new ArrayList();
        this.mSyncWhiteList = new ArrayList();
        this.mNotifyWhiteList = new ArrayList();
        this.mProviderCpnWhiteList = new ArrayList();
        this.mBroadcastWhiteList = new ArrayList();
        this.mBroadcastActionWhiteList = new ArrayList();
        this.mProtectList = new ArrayList();
        this.mAssociateStartWhiteListContainer = new SparseArray<>();
        this.mAuthorizeCpnList = new ArrayList();
        this.mGlobalWhiteList = new ArrayList();
        this.mProviderCallerPkgList = new ArrayList();
        this.mBackgroundRestrictCallerPkgMap = new ArrayMap<>();
        this.mStartServiceCallerPkgMap = new ArrayMap<>();
        this.mBindServiceCallerPkgMap = new ArrayMap<>();
        this.mMonitorAppUploadList = new ArrayList<>();
        this.mMonitorAppInfoList = new ArrayList();
        this.mLastCalledAcivityList = new ArrayList<>();
        this.mCollectProcessInfoList = new ArrayList();
        this.mCollectBlackListInterceptList = new ArrayList();
        this.mCollectGamePayAbnormalList = new ArrayList();
        this.mCollectGamePayMonitorList = new ArrayList();
        this.mStartActivityReasonList = new ArrayList();
        this.mAudioManager = null;
        this.mHypnus = null;
        this.mHandler = null;
        this.mSwitchBrowserInterceptUpload = false;
        this.mDefaultInputMethod = null;
        this.mCpnUpperLimitSwitch = false;
        this.mCpnUpperLimitThresholdConnected = 1000;
        this.mCpnUpperLimitCountInterval = 500;
        this.mCpnUpperLimitCountStep = 50;
        this.mCpnUpperLimitThresholdTime = 1000;
        this.mCpnUpperLimitMap = new ArrayList();
        this.mPrCountMap = new HashMap();
        this.mDefaultDialerApp = null;
        this.mController = null;
        this.mAppStartupHelper = null;
        this.mPreventIndulgeController = null;
        this.mPreventIndulgeList = new ArrayList();
        this.mCurrentResumeApp = "";
        this.isExpVersion = true;
        this.mIsAllowRecentAppStart = true;
        this.mCurrentStartActivityIntent = null;
        this.mCurrentStartActivityCallerPackage = null;
        this.mDateChangedReceiver = new BroadcastReceiver() {
            /* class com.android.server.am.ColorAppStartupManager.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if (ColorAppStartupManager.this.mSwitchMonitor) {
                    ColorAppStartupManager.this.notifyAppInterceptInfo();
                    ColorAppStartupManager.this.uploadBlackListIntercept();
                    ColorAppStartupManager.this.uploadProcessStartInfo();
                    ColorAppStartupStatistics.getInstance().uploadAppStartupList();
                    ColorAppStartupStatistics.getInstance().uploadPopupActivityList();
                }
                if (ColorAppStartupManager.this.mSwitchMonitor || ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalSwitch()) {
                    synchronized (ColorAppStartupManager.this.mCollectGamePayAbnormalList) {
                        ColorAppStartupManager.this.uploadProcessInfo(ColorAppStartupManager.this.mCollectGamePayAbnormalList, "game_pay");
                    }
                }
                ColorAppStartupManager.this.uploadCpnUpperLimit();
                if (ColorAppStartupManager.this.mSwitchMonitor || ColorAppStartupManagerUtils.getInstance().getGamePayMonitorSwitch()) {
                    synchronized (ColorAppStartupManager.this.mCollectGamePayMonitorList) {
                        ColorAppStartupManager.this.uploadProcessInfo(ColorAppStartupManager.this.mCollectGamePayMonitorList, "game_pay_monitor");
                    }
                }
            }
        };
        this.mDufaultInputMethodObserver = new ContentObserver(this.mHandler) {
            /* class com.android.server.am.ColorAppStartupManager.AnonymousClass2 */

            public void onChange(boolean selfChange) {
                ColorAppStartupManager colorAppStartupManager = ColorAppStartupManager.this;
                String unused = colorAppStartupManager.mDefaultInputMethod = colorAppStartupManager.getDefaultInputMethod();
            }
        };
        this.mDefaultAppReceiver = new BroadcastReceiver() {
            /* class com.android.server.am.ColorAppStartupManager.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                ColorAppStartupManager colorAppStartupManager = ColorAppStartupManager.this;
                String unused = colorAppStartupManager.mDefaultDialerApp = colorAppStartupManager.getDefaultDialerApp();
                if (ColorAppStartupManager.this.mDebugSwitch) {
                    Log.d(ColorAppStartupMonitorInfo.TAG, "DefaultAppReceiver: " + ColorAppStartupManager.this.mDefaultDialerApp);
                }
            }
        };
    }

    private ColorAppStartupManager(String name) {
        this.mDynamicDebug = false;
        this.mDebugSwitch = DEBUG_DETAIL | this.mDynamicDebug;
        this.mSwitch = SystemProperties.getBoolean("persist.sys.startupmanager", true);
        this.mSwitchMonitor = false;
        this.mSwitchInterceptActivity = false;
        this.mAms = null;
        this.mColorAmsEx = null;
        this.mColorAmsInner = null;
        this.mMonitorAll = SystemProperties.getBoolean("persist.sys.monitorall", false);
        this.mServiceLock = new Object();
        this.mReceiverLock = new Object();
        this.mReceiverActionLock = new Object();
        this.mProviderLock = new Object();
        this.mActivityLock = new Object();
        this.mActionLock = new Object();
        this.mBlackguardLock = new Object();
        this.mBuildAppBlackLock = new Object();
        this.mStartServiceWhiteLock = new Object();
        this.mStartServiceWhiteCpnLock = new Object();
        this.mBindServiceWhiteLock = new Object();
        this.mJobWhiteLock = new Object();
        this.mSyncWhiteLock = new Object();
        this.mNotifyWhiteLock = new Object();
        this.mProviderWhiteLock = new Object();
        this.mBroadcastWhiteLock = new Object();
        this.mBroadcastActionWhiteLock = new Object();
        this.mProtectLock = new Object();
        this.mAssociateStartWhiteLock = new Object();
        this.mAuthorizeCpnListLock = new Object();
        this.mCollectAppStartLock = new Object();
        this.mActivityCallerWhitePkgLock = new Object();
        this.mActivityCalledWhitePkgLock = new Object();
        this.mActivityCalledWhiteCpnLock = new Object();
        this.mActivityPkgKeyLock = new Object();
        this.mActivityCalledKeyLock = new Object();
        this.mBlackguardActivityLock = new Object();
        this.mCustomizeWhiteLock = new Object();
        this.mProviderCallerPkgLock = new Object();
        this.mBackgroundRestrictCallerPkgLock = new Object();
        this.mStartServiceCallerPkgLock = new Object();
        this.mBindServiceCallerPkgLock = new Object();
        this.mSeviceCpnBlacklist = new ArrayList();
        this.mReceiverBlackList = new ArrayList();
        this.mReceiverActionBlackList = new ArrayList();
        this.mProviderBlacklist = new ArrayList();
        this.mActivityBlackList = new ArrayList();
        this.mActionBlackList = new ArrayList();
        this.mBlackguardList = new ArrayList();
        this.mCollectAppStartList = new ArrayList();
        this.mActivityCallerWhitePkgList = new ArrayList();
        this.mActivityCalledWhitePkgList = new ArrayList();
        this.mActivityCalledWhiteCpnList = new ArrayList();
        this.mActivityPkgKeyList = new ArrayList();
        this.mActivityCalledKeyList = new ArrayList();
        this.mBlackguardActivityList = new ArrayList();
        this.mCustomizeWhiteList = new ArrayList();
        this.mActivitySelfCallWhitePkgList = new ArrayList();
        this.mActivityAssociateWhitePkgMap = new ArrayMap<>();
        this.mActivityAssociateBlackPkgMap = new ArrayMap<>();
        this.mActivitySleepCpnBlackList = new ArrayList();
        this.mBuildAppBlackList = new ArrayList();
        this.mStartServiceWhiteList = new ArrayList();
        this.mStartServiceWhiteCpnList = new ArrayList();
        this.mBindServiceCpnWhiteList = new ArrayList();
        this.mJobWhiteList = new ArrayList();
        this.mSyncWhiteList = new ArrayList();
        this.mNotifyWhiteList = new ArrayList();
        this.mProviderCpnWhiteList = new ArrayList();
        this.mBroadcastWhiteList = new ArrayList();
        this.mBroadcastActionWhiteList = new ArrayList();
        this.mProtectList = new ArrayList();
        this.mAssociateStartWhiteListContainer = new SparseArray<>();
        this.mAuthorizeCpnList = new ArrayList();
        this.mGlobalWhiteList = new ArrayList();
        this.mProviderCallerPkgList = new ArrayList();
        this.mBackgroundRestrictCallerPkgMap = new ArrayMap<>();
        this.mStartServiceCallerPkgMap = new ArrayMap<>();
        this.mBindServiceCallerPkgMap = new ArrayMap<>();
        this.mMonitorAppUploadList = new ArrayList<>();
        this.mMonitorAppInfoList = new ArrayList();
        this.mLastCalledAcivityList = new ArrayList<>();
        this.mCollectProcessInfoList = new ArrayList();
        this.mCollectBlackListInterceptList = new ArrayList();
        this.mCollectGamePayAbnormalList = new ArrayList();
        this.mCollectGamePayMonitorList = new ArrayList();
        this.mStartActivityReasonList = new ArrayList();
        this.mAudioManager = null;
        this.mHypnus = null;
        this.mHandler = null;
        this.mSwitchBrowserInterceptUpload = false;
        this.mDefaultInputMethod = null;
        this.mCpnUpperLimitSwitch = false;
        this.mCpnUpperLimitThresholdConnected = 1000;
        this.mCpnUpperLimitCountInterval = 500;
        this.mCpnUpperLimitCountStep = 50;
        this.mCpnUpperLimitThresholdTime = 1000;
        this.mCpnUpperLimitMap = new ArrayList();
        this.mPrCountMap = new HashMap();
        this.mDefaultDialerApp = null;
        this.mController = null;
        this.mAppStartupHelper = null;
        this.mPreventIndulgeController = null;
        this.mPreventIndulgeList = new ArrayList();
        this.mCurrentResumeApp = "";
        this.isExpVersion = true;
        this.mIsAllowRecentAppStart = true;
        this.mCurrentStartActivityIntent = null;
        this.mCurrentStartActivityCallerPackage = null;
        this.mDateChangedReceiver = new BroadcastReceiver() {
            /* class com.android.server.am.ColorAppStartupManager.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if (ColorAppStartupManager.this.mSwitchMonitor) {
                    ColorAppStartupManager.this.notifyAppInterceptInfo();
                    ColorAppStartupManager.this.uploadBlackListIntercept();
                    ColorAppStartupManager.this.uploadProcessStartInfo();
                    ColorAppStartupStatistics.getInstance().uploadAppStartupList();
                    ColorAppStartupStatistics.getInstance().uploadPopupActivityList();
                }
                if (ColorAppStartupManager.this.mSwitchMonitor || ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalSwitch()) {
                    synchronized (ColorAppStartupManager.this.mCollectGamePayAbnormalList) {
                        ColorAppStartupManager.this.uploadProcessInfo(ColorAppStartupManager.this.mCollectGamePayAbnormalList, "game_pay");
                    }
                }
                ColorAppStartupManager.this.uploadCpnUpperLimit();
                if (ColorAppStartupManager.this.mSwitchMonitor || ColorAppStartupManagerUtils.getInstance().getGamePayMonitorSwitch()) {
                    synchronized (ColorAppStartupManager.this.mCollectGamePayMonitorList) {
                        ColorAppStartupManager.this.uploadProcessInfo(ColorAppStartupManager.this.mCollectGamePayMonitorList, "game_pay_monitor");
                    }
                }
            }
        };
        this.mDufaultInputMethodObserver = new ContentObserver(this.mHandler) {
            /* class com.android.server.am.ColorAppStartupManager.AnonymousClass2 */

            public void onChange(boolean selfChange) {
                ColorAppStartupManager colorAppStartupManager = ColorAppStartupManager.this;
                String unused = colorAppStartupManager.mDefaultInputMethod = colorAppStartupManager.getDefaultInputMethod();
            }
        };
        this.mDefaultAppReceiver = new BroadcastReceiver() {
            /* class com.android.server.am.ColorAppStartupManager.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                ColorAppStartupManager colorAppStartupManager = ColorAppStartupManager.this;
                String unused = colorAppStartupManager.mDefaultDialerApp = colorAppStartupManager.getDefaultDialerApp();
                if (ColorAppStartupManager.this.mDebugSwitch) {
                    Log.d(ColorAppStartupMonitorInfo.TAG, "DefaultAppReceiver: " + ColorAppStartupManager.this.mDefaultDialerApp);
                }
            }
        };
        this.mHypnus = Hypnus.getHypnus();
        registerLogModule();
        HandlerThread thread = new HandlerThread(ColorAppStartupMonitorInfo.TAG);
        thread.start();
        this.mHandler = new Handler(thread.getLooper());
    }

    public static final ColorAppStartupManager getInstance() {
        if (sColorAppStartupManager == null) {
            synchronized (ColorAppStartupManager.class) {
                if (sColorAppStartupManager == null) {
                    sColorAppStartupManager = new ColorAppStartupManager(ColorAppStartupMonitorInfo.TAG);
                }
            }
        }
        return sColorAppStartupManager;
    }

    public boolean getSwitchMonitor() {
        return this.mSwitchMonitor;
    }

    /* access modifiers changed from: package-private */
    public void initData() {
        Log.d(ColorAppStartupMonitorInfo.TAG, "initData");
        this.mSwitchMonitor = ColorAppStartupManagerUtils.getInstance().isForumVersion();
        updateConfiglist();
        updateMonitorlist();
        updateCustomizeWhiteList();
        updateAssociateStartList();
        this.mDefaultInputMethod = getDefaultInputMethod();
        this.mDefaultDialerApp = getDefaultDialerApp();
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            activityManagerService.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("default_input_method"), true, this.mDufaultInputMethodObserver);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.DATE_CHANGED");
            this.mAms.mContext.registerReceiver(this.mDateChangedReceiver, filter, null, this.mHandler);
            IntentFilter defaultAppFilter = new IntentFilter();
            defaultAppFilter.addAction("android.telecom.action.DEFAULT_DIALER_CHANGED");
            this.mAms.mContext.registerReceiver(this.mDefaultAppReceiver, defaultAppFilter, null, this.mHandler);
        }
        initStartActivityReason();
    }

    public void init(IColorActivityManagerServiceEx amsEx) {
        if (amsEx != null) {
            this.mColorAmsEx = amsEx;
            this.mAms = amsEx.getActivityManagerService();
            this.mColorAmsInner = amsEx.getColorActivityManagerServiceInner();
        }
        ColorAppStartupManagerUtils.getInstance().init(this.mAms.mContext);
        ColorAutostartManager.getInstance().initBootList(this.mAms, false);
        initData();
        ColorAppStartupStatistics.getInstance().init(this, this.mHandler, this.mAms);
        this.mAppStartupHelper = ColorAppStartupManagerHelper.getInstance();
        ColorBootCompleteBroadcastManager.getInstance().init(this.mAms);
    }

    public boolean shouldPreventProcessBroadcast(BroadcastRecord record, ResolveInfo info, String type) {
        if (record == null || record.intent == null || info == null) {
            return false;
        }
        if ("startProcess".equals(type)) {
            if (record.callingUid == 1000) {
                return ColorAutostartManager.getInstance().checkAutoBootForbiddenStart(record, info);
            }
            if (record.callerApp != null) {
                return !isAllowStartFromBroadCast(record.callerApp, null, record.callingUid, record.intent, info);
            }
            return !isAllowStartFromBroadCast(record.callingUid, record.callingPid, record.intent, info);
        } else if ("skipSpecialBroadcast".equals(type)) {
            return handleSpecialBroadcast(record.intent, record.callerApp, info.activityInfo.packageName);
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isRecentLockedApps(String packageName, int userId) {
        ArrayList<String> list;
        boolean result = false;
        if (!this.mIsAllowRecentAppStart) {
            return false;
        }
        String nameWithUserId = packageName + "#" + userId;
        Bundle bundle = ColorCommonConfig.getInstance().getConfigInfoAsUser(RECENT_LOCK_CONFIG_NAME, 1, userId);
        if (bundle != null && (list = bundle.getStringArrayList(RECENT_LOCK_CONFIG_NAME)) != null) {
            Iterator<String> it = list.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                } else if (it.next().equals(nameWithUserId)) {
                    result = true;
                    if (this.mDynamicDebug) {
                        Log.d(ColorAppStartupMonitorInfo.TAG, packageName + "is recent locked app, so let it to startup!");
                    }
                }
            }
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, packageName + "isn't recent locked app, get result:" + result);
        }
        return result;
    }

    public boolean shouldPreventStartProvider(ProcessRecord proc, ContentProviderRecord providerRecord, ApplicationInfo appInfo) {
        boolean result = handleStartProvider(providerRecord, proc);
        if (result || providerRecord == null) {
            return result;
        }
        return !isAllowStartFromProvider(proc, providerRecord, appInfo);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004a, code lost:
        r10 = r0;
     */
    public boolean shouldPreventStartService(ServiceRecord record, int callingPid, int callingUid, ProcessRecord callerApp, String callerPkg, Intent service, String type) {
        boolean bindFromJob;
        ProcessRecord callerApp2 = callerApp;
        if ("actionBlk".equals(type)) {
            return handleStartOrBindService(service, callerApp2);
        }
        if (record != null && record.appInfo != null) {
            if (service != null) {
                if (isAppStartForbidden(record.appInfo.packageName)) {
                    return true;
                }
                if ("startService".equals(type)) {
                    if (callerApp2 == null) {
                        synchronized (this.mAms.mPidsSelfLocked) {
                            try {
                                try {
                                    ProcessRecord callerApp3 = this.mAms.mPidsSelfLocked.get(callingPid);
                                } catch (Throwable th) {
                                    th = th;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th2) {
                                            th = th2;
                                        }
                                    }
                                    throw th;
                                }
                                try {
                                } catch (Throwable th3) {
                                    th = th3;
                                    while (true) {
                                        break;
                                    }
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                    }
                    if (record.app == null && record.processName != null && this.mAms.getProcessRecordLocked(record.processName, record.appInfo.uid, false) == null) {
                        return !isAllowStartFromStartService(callerApp2, callingPid, callingUid, callerPkg, record, service);
                    }
                } else if ("bindService".equals(type) && record.app == null && record.processName != null && callerApp2 != null && callerApp2.info != null && this.mAms.getProcessRecordLocked(record.processName, record.appInfo.uid, false) == null) {
                    if ((callerApp2.info.flags & 1) == 0 || callerApp2.info.uid % 100000 == 1002) {
                        return !isAllowStartFromBindService(callerApp, callerPkg, callerApp2.info.uid, record, service, "bs");
                    }
                    if (callerApp2.info.uid == 1000) {
                        try {
                            bindFromJob = service.getBooleanExtra("BINDSERVICE_FROM_JOB", false);
                        } catch (Exception e) {
                            bindFromJob = false;
                        }
                        if (bindFromJob) {
                            return !isAllowStartFromBindService(callerApp, "system[jobScheduler]", callerApp2.info.uid, record, service, "bsfj");
                        }
                    }
                }
                return false;
            }
        }
        return false;
    }

    public boolean shouldPreventStartActivity(Intent intent, Intent ephemeralIntent, ActivityInfo activityInfo, String callingPackage, int callingUid, int callingPid, String reason, int userId) {
        return handleStartActivity(intent, ephemeralIntent, activityInfo, callingPackage, callingUid, callingPid, reason, userId);
    }

    public boolean shouldPreventNotification(ComponentName component, String rebindType, int userId) {
        return !isAllowStartFromNotificationService(component, rebindType, userId);
    }

    public void monitorAppStartupInfo(int callingPid, int callingUid, ProcessRecord callerApp, Intent intent, ApplicationInfo appInfo, String hostType) {
        handleProcessStartupInfo(callingPid, callingUid, callerApp, intent, appInfo, hostType);
    }

    public void handleBroadcastIncludeForceStop(Intent intent, ProcessRecord callerApp) {
        if (this.mSwitch && intent != null && callerApp != null && (intent.getFlags() & 32) == 32 && callerApp.info != null && (callerApp.info.flags & 1) == 0) {
            if ("com.android.cts.robot.ACTION_POST".equals(intent.getAction())) {
                Log.d(ColorAppStartupMonitorInfo.TAG, callerApp.processName + " is CTS app. do not remove the flag!");
            } else if (!isCtsRunning()) {
                if (this.mDebugSwitch) {
                    Log.d(ColorAppStartupMonitorInfo.TAG, callerApp.processName + " is the thirdparty app. remove the flag! " + intent);
                }
                intent.setFlags(intent.getFlags() ^ 32);
            }
        }
    }

    public List<String> getSougouSite() {
        return ColorAppStartupManagerUtils.getInstance().getSougouSite();
    }

    public void handleResumeActivity(IColorActivityRecordEx record) {
        ApplicationInfo appInfo;
        String callingPkg;
        ProcessRecord callerApp;
        if (!isExpVersion() && record != null && (appInfo = record.getAppliationInfo()) != null) {
            String currentPkg = appInfo.packageName;
            if (!TextUtils.isEmpty(currentPkg) && !currentPkg.equals(this.mCurrentResumeApp)) {
                this.mCurrentResumeApp = currentPkg;
                String callingPkg2 = record.getLaunchedFromPackage();
                if (TextUtils.isEmpty(callingPkg2)) {
                    int callingPid = record.getLaunchedFromPid();
                    synchronized (this.mAms.mPidsSelfLocked) {
                        callerApp = this.mAms.mPidsSelfLocked.get(callingPid);
                    }
                    if (callerApp != null) {
                        List<String> callingPkgList = getAppPkgList(callerApp);
                        if (!callingPkgList.isEmpty()) {
                            callingPkg = callingPkgList.get(0);
                            if (callingPkg != null && "com.oppo.launcher".equals(callingPkg) && !isDefaultAllowStart(appInfo)) {
                                this.mHandler.post(new AppAbnormalMonitor(currentPkg, "", "", "", CALL_ARG_STARTUP));
                                if (this.mDynamicDebug) {
                                    Log.d(ColorAppStartupMonitorInfo.TAG, "appStartupMonitor app=" + appInfo + " callingPkg=" + callingPkg);
                                    return;
                                }
                                return;
                            }
                            return;
                        }
                    }
                }
                callingPkg = callingPkg2;
                if (callingPkg != null) {
                }
            }
        }
    }

    private boolean handleStartOrBindService(Intent service, ProcessRecord callerApp) {
        if (!this.mSwitch || service == null || callerApp == null) {
            return false;
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, Log.getStackTraceString(new Throwable()));
        }
        if (callerApp.uid <= 10000) {
            if (this.mDynamicDebug) {
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService callerApp.uid <= 10000 return");
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService callerApp: " + callerApp);
            }
            return false;
        }
        if (this.mDynamicDebug) {
            Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService: " + service + " args=" + service.getExtras());
            StringBuilder sb = new StringBuilder();
            sb.append("handleStartOrBindService callerApp: ");
            sb.append(callerApp);
            Log.v(ColorAppStartupMonitorInfo.TAG, sb.toString());
        }
        ComponentName cpn = service.getComponent();
        if (cpn != null) {
            String cpnPkgName = cpn.getPackageName();
            String cpnClassName = cpn.getClassName();
            if (this.mDynamicDebug) {
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService cpnPkgName == " + cpnPkgName);
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService cpnClassName == " + cpnClassName);
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService callerApp.processName == " + callerApp.processName);
            }
            if (!inSeviceCpnlist(cpnClassName) || callerApp.processName == null || callerApp.processName.contains(cpnPkgName)) {
                return false;
            }
            if (this.mDebugSwitch) {
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService return undo!");
            }
            if (!this.mSwitchMonitor) {
                return true;
            }
            this.mHandler.post(new CollectBlackListInterceptRunnable(cpnPkgName, cpnClassName, "service"));
            return true;
        }
        if (this.mDynamicDebug) {
            Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService cpn = null!");
        }
        String action = service.getAction();
        if (action == null) {
            return false;
        }
        if (this.mDynamicDebug) {
            Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService action == " + action);
        }
        String pkgName = service.getPackage();
        if (pkgName == null) {
            if (this.mDebugSwitch) {
                ComponentName component = service.getComponent();
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService component = " + component);
            }
            pkgName = "unknow";
        }
        if (this.mDynamicDebug) {
            Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService pkgName == " + pkgName);
        }
        if (inActionlist(action) && callerApp.processName != null && !callerApp.processName.contains(pkgName)) {
            if (this.mDebugSwitch) {
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService return undo!");
            }
            if (!this.mSwitchMonitor) {
                return true;
            }
            this.mHandler.post(new CollectBlackListInterceptRunnable(pkgName, action, "service"));
            return true;
        } else if (!inBlackguardList(action)) {
            return false;
        } else {
            if (this.mDebugSwitch) {
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartOrBindService inBlackguardList return undo!");
            }
            return true;
        }
    }

    public boolean handleStartProvider(ContentProviderRecord cpr, ProcessRecord callerApp) {
        boolean result = false;
        if (!this.mSwitch || cpr == null) {
            return false;
        }
        if (callerApp == null || callerApp.uid > 10000) {
            ComponentName cpn = cpr.name;
            if (cpn == null) {
                return false;
            }
            String cpnPkgName = cpn.getPackageName();
            String cpnClassName = cpn.getClassName();
            if (OppoListManager.getInstance().isAppStartForbidden(cpnPkgName)) {
                if (this.mSwitchMonitor) {
                    this.mHandler.post(new CollectAppInterceptRunnable("noNeed", cpnPkgName, "noNeed", "provider"));
                }
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartProvider: " + cpnPkgName + " is forbidden to start");
                result = true;
            }
            if (callerApp == null && inProviderlist(cpnClassName)) {
                if (this.mDebugSwitch) {
                    Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartProvider return undo!");
                }
                return true;
            } else if (callerApp == null || !inProviderlist(cpnClassName) || callerApp.processName == null || callerApp.processName.contains(cpnPkgName)) {
                return result;
            } else {
                if (this.mDebugSwitch) {
                    Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartProvider return undo!");
                }
                if (!this.mSwitchMonitor) {
                    return true;
                }
                this.mHandler.post(new CollectBlackListInterceptRunnable(cpnPkgName, cpnClassName, "provider"));
                return true;
            }
        } else {
            if (this.mDynamicDebug) {
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartProvider callerApp.uid <= 10000 return");
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartProvider callerApp: " + callerApp);
            }
            Hypnus hypnus = this.mHypnus;
            if (hypnus != null) {
                hypnus.hypnusSetAction(12, 1000);
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkIsBroadcastexcludePkg(Intent intent, String packageName) {
        boolean result = false;
        if (isExpVersion()) {
            return false;
        }
        if (packageName == null || intent == null) {
            if (this.mDynamicDebug) {
                Log.v(ColorAppStartupMonitorInfo.TAG, "checkIsBroadcastexcludePkg:packageName == null || intent == null");
            }
            return false;
        }
        if (this.mDynamicDebug) {
            Log.v(ColorAppStartupMonitorInfo.TAG, "checkIsBroadcastexcludePkg: " + intent + " packageName=" + packageName);
        }
        String action = intent.getAction();
        if (action != null && inReceiverActionExcludelist(action, packageName)) {
            if (this.mDynamicDebug) {
                Log.v(ColorAppStartupMonitorInfo.TAG, "--" + packageName + " in blacklist with action " + action + ", return undo!");
            }
            result = true;
            if (this.mSwitchMonitor) {
                this.mHandler.post(new CollectBlackListInterceptRunnable(packageName, action, "broadcast"));
            }
        }
        return result;
    }

    private boolean handleSpecialBroadcast(Intent intent, ProcessRecord callerApp, String packageName) {
        if (packageName == null || !OppoListManager.getInstance().isAppStartForbidden(packageName)) {
            boolean result = false;
            if (!this.mSwitch) {
                return false;
            }
            if (intent == null || callerApp == null) {
                if (this.mDynamicDebug) {
                    Log.v(ColorAppStartupMonitorInfo.TAG, "intent == null || callerApp == null");
                }
                return false;
            }
            if (this.mDynamicDebug) {
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleSpecialBroadcast: " + intent + " args=" + intent.getExtras());
                StringBuilder sb = new StringBuilder();
                sb.append("handleSpecialBroadcast callerApp: ");
                sb.append(callerApp);
                Log.v(ColorAppStartupMonitorInfo.TAG, sb.toString());
            }
            if (callerApp.uid <= 10000) {
                if (this.mDynamicDebug) {
                    Log.v(ColorAppStartupMonitorInfo.TAG, "handleSpecialBroadcast callerApp.uid <= 10000 return");
                    Log.v(ColorAppStartupMonitorInfo.TAG, "handleSpecialBroadcast callerApp: " + callerApp);
                }
                return false;
            }
            ComponentName cpn = intent.getComponent();
            if (cpn != null) {
                String cpnPkgName = cpn.getPackageName();
                String cpnClassName = cpn.getClassName();
                if (this.mDynamicDebug) {
                    Log.v(ColorAppStartupMonitorInfo.TAG, "handleSpecialBroadcast cpnPkgName == " + cpnPkgName);
                    Log.v(ColorAppStartupMonitorInfo.TAG, "handleSpecialBroadcast cpnClassName == " + cpnClassName);
                    Log.v(ColorAppStartupMonitorInfo.TAG, "handleSpecialBroadcast callerApp.processName == " + callerApp.processName);
                }
                if (inReceiverlist(cpnClassName) && callerApp.processName != null && !callerApp.processName.contains(cpnPkgName)) {
                    if (this.mDebugSwitch) {
                        Log.v(ColorAppStartupMonitorInfo.TAG, "handleSpecialBroadcast return skip!");
                    }
                    result = true;
                    if (this.mSwitchMonitor) {
                        this.mHandler.post(new CollectBlackListInterceptRunnable(cpnPkgName, cpnClassName, "broadcast"));
                    }
                }
            } else {
                String action = intent.getAction();
                if (action != null) {
                    String pkgName = intent.getPackage();
                    if (pkgName == null) {
                        pkgName = "unknow";
                    }
                    if (this.mDynamicDebug) {
                        Log.v(ColorAppStartupMonitorInfo.TAG, "handleSpecialBroadcast pkgName == " + pkgName);
                    }
                    if (inReceiverActionlist(action) && callerApp.processName != null && !callerApp.processName.contains(pkgName)) {
                        if (this.mDebugSwitch) {
                            Log.v(ColorAppStartupMonitorInfo.TAG, "handleSpecialBroadcast return undo!");
                        }
                        result = true;
                        if (this.mSwitchMonitor) {
                            this.mHandler.post(new CollectBlackListInterceptRunnable(pkgName, action, "broadcast"));
                        }
                    }
                }
            }
            return result;
        }
        if (this.mSwitchMonitor) {
            this.mHandler.post(new CollectAppInterceptRunnable("noNeed", packageName, "noNeed", "broadcast"));
        }
        Log.i(ColorAppStartupMonitorInfo.TAG, packageName + " is forbidden to start by broadcast");
        return true;
    }

    private boolean handleStartActivity(Intent intent, Intent ephemeralIntent, ActivityInfo aInfo, String callingPackage, int callingUid, int callingPid, String reason, int userId) {
        long time;
        String calledPackageName;
        ComponentName topCpn;
        ActivityManagerService activityManagerService;
        if (this.mDynamicDebug) {
            time = SystemClock.elapsedRealtime();
        } else {
            time = 0;
        }
        if (!TextUtils.isEmpty(reason)) {
            if (reason.equals("check_white")) {
                if (this.mDynamicDebug) {
                    Log.v(ColorAppStartupMonitorInfo.TAG, "handlestartactivity check_white");
                }
                if (intent == null || intent.getComponent() == null) {
                    return true;
                }
                this.mCurrentStartActivityIntent = intent;
                this.mCurrentStartActivityCallerPackage = callingPackage;
                String calledPkg = intent.getComponent().getPackageName();
                String calledCpnName = intent.getComponent().getClassName();
                if (callingPackage != null && inActivityCallerWhitePkgList(callingPackage)) {
                    if (this.mDebugSwitch) {
                        Log.d(ColorAppStartupMonitorInfo.TAG, "callingPackage in whitelist return. callingPackage = " + callingPackage);
                    }
                    return false;
                } else if (calledPkg != null && inActivityCalledWhitePkgList(calledPkg)) {
                    if (this.mDebugSwitch) {
                        Log.d(ColorAppStartupMonitorInfo.TAG, "calledPkg in whitelist return. calledPkg = " + calledPkg);
                    }
                    return false;
                } else if (calledCpnName == null || !inActivityCalledWhiteCpnList(calledCpnName)) {
                    if (!(callingPackage == null || calledPkg == null)) {
                        if (callingPackage.equals(calledPkg) && inActivitySelfCallWhitePkgList(callingPackage)) {
                            if (this.mDebugSwitch) {
                                Log.d(ColorAppStartupMonitorInfo.TAG, "callingPackage = calledPkg in whitelist return. callingPackage = " + callingPackage);
                            }
                            return false;
                        } else if (inActivityAssociateWhitePkgMap(callingPackage, calledPkg)) {
                            if (this.mDebugSwitch) {
                                Log.d(ColorAppStartupMonitorInfo.TAG, "callingPackage = calledPkg in whitelist return. callingPackage = " + callingPackage);
                            }
                            return false;
                        }
                    }
                    if (this.mDynamicDebug) {
                        Log.d(ColorAppStartupMonitorInfo.TAG, "shouldPreventStartActivity check_white cost time ==  " + (SystemClock.elapsedRealtime() - time));
                    }
                    return true;
                } else {
                    if (this.mDebugSwitch) {
                        Log.d(ColorAppStartupMonitorInfo.TAG, "calledCpnName in whitelist return. calledCpnName = " + calledCpnName);
                    }
                    return false;
                }
            }
        }
        if (this.mDynamicDebug) {
            Log.v(ColorAppStartupMonitorInfo.TAG, "handlestartactivity check_black");
        }
        if (aInfo != null && aInfo.packageName != null) {
            String calledPackageName2 = aInfo.packageName;
            if (calledPackageName2 == null || !OppoListManager.getInstance().isAppStartForbidden(calledPackageName2)) {
                String cpnClassName = aInfo.name;
                if (callingPackage != null) {
                    if (cpnClassName == null || !inActivityPushBlacklist(cpnClassName)) {
                        calledPackageName = calledPackageName2;
                    } else if (calledPackageName2 == null) {
                        calledPackageName = calledPackageName2;
                    } else if (!callingPackage.contains(calledPackageName2)) {
                        if (this.mDebugSwitch) {
                            Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartActivity return undo!");
                        }
                        ColorAppStartupStatistics.getInstance().collectPopupActivityInfo(callingPackage, calledPackageName2, cpnClassName, "noNeed", "noNeed", "forbid_push");
                        return true;
                    } else {
                        calledPackageName = calledPackageName2;
                    }
                    if (calledPackageName != null && inActivityAssociateBlackPkgMap(callingPackage, calledPackageName)) {
                        if (!this.mDebugSwitch) {
                            return true;
                        }
                        Log.v(ColorAppStartupMonitorInfo.TAG, "InterceptInfo  inActivityAssociateBlackPkgMap return undo!");
                        return true;
                    }
                } else {
                    calledPackageName = calledPackageName2;
                }
                if (cpnClassName == null || !inBlackguardActivityList(cpnClassName)) {
                    if (!(calledPackageName == null || (activityManagerService = this.mAms) == null || activityManagerService.mAtmInternal == null || intent == null)) {
                        boolean isSleep = this.mAms.mAtmInternal.isSleeping();
                        ComponentName componentName = intent.getComponent();
                        if (this.mDynamicDebug) {
                            Log.v(ColorAppStartupMonitorInfo.TAG, "isSleep = " + isSleep);
                            StringBuilder sb = new StringBuilder();
                            sb.append("componentName.flattenToShortString() = ");
                            sb.append(componentName != null ? componentName.flattenToShortString() : "");
                            Log.v(ColorAppStartupMonitorInfo.TAG, sb.toString());
                        }
                        if (componentName != null && isSleep && inActivitySleepCpnBlackList(componentName.flattenToShortString())) {
                            if (!this.mDebugSwitch) {
                                return true;
                            }
                            Log.v(ColorAppStartupMonitorInfo.TAG, "InterceptInfo  ActivitySleepCpnBlack return undo!");
                            return true;
                        }
                    }
                    if (OppoListManager.getInstance().getPreventRedundentStartSwitch() && cpnClassName != null && OppoListManager.getInstance().isRedundentActivity(cpnClassName)) {
                        Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartActivity redundent activity " + cpnClassName);
                        return true;
                    } else if (calledPackageName == null || !isInPreventIndulgeList(calledPackageName)) {
                        OppoFeatureCache.get(IColorAppStoreTraffic.DEFAULT).collectJumpStoreTracking(callingPackage, calledPackageName, ephemeralIntent, callingUid, cpnClassName);
                        if (isExpVersion()) {
                            return false;
                        }
                        if (OppoFeatureCache.get(IColorAppStoreTraffic.DEFAULT).interceptForStoreTraffic(intent, callingPackage, calledPackageName, cpnClassName, userId)) {
                            return true;
                        }
                        if ((this.mSwitchMonitor || ColorAppStartupManagerUtils.getInstance().getGamePayMonitorSwitch()) && (topCpn = getTopComponentName()) != null) {
                            this.mHandler.post(new CollectGamePayRunnable(callingPackage, topCpn.getClassName(), calledPackageName, cpnClassName, intent));
                        }
                        if (this.mSwitchMonitor || ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalSwitch()) {
                            ComponentName topCpn2 = getTopComponentName();
                            if (topCpn2 != null) {
                                this.mHandler.post(new CollectGamePayAbnormalRunnable(callingPackage, topCpn2.getClassName(), calledPackageName, cpnClassName, intent));
                            }
                        }
                        if (!this.mDynamicDebug) {
                            return false;
                        }
                        Log.d(ColorAppStartupMonitorInfo.TAG, "shouldPreventStartActivity check_black cost time ==  " + (SystemClock.elapsedRealtime() - time));
                        return false;
                    } else {
                        if (this.mDebugSwitch) {
                            Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartActivity prevent indulge: " + calledPackageName);
                        }
                        this.mHandler.post(new PreventIndulgeRunnable(calledPackageName));
                        return true;
                    }
                } else {
                    ColorAppStartupStatistics.getInstance().collectPopupActivityInfo(callingPackage, calledPackageName, cpnClassName, "noNeed", "noNeed", "forbid_black");
                    Log.v(ColorAppStartupMonitorInfo.TAG, "InterceptInfo one pixel" + cpnClassName);
                    return true;
                }
            } else {
                Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartActivity(pkg): " + calledPackageName2 + " is forbidden to start");
                if (callingPackage != null && callingPackage.equals("com.oppo.launcher")) {
                    Log.d(ColorAppStartupMonitorInfo.TAG, calledPackageName2 + " is started from launcher: " + callingPackage);
                    Message msg = Message.obtain();
                    msg.what = 1002;
                    msg.obj = calledPackageName2;
                    if (this.mAms.getCurrentUser().id == 0) {
                        this.mAms.mUiHandler.sendMessage(msg);
                    }
                    if (!this.mSwitchMonitor) {
                        return true;
                    }
                    this.mHandler.post(new CollectAppInterceptRunnable("noNeed", calledPackageName2, "noNeed", "dialog"));
                    return true;
                } else if (!this.mSwitchMonitor) {
                    return true;
                } else {
                    this.mHandler.post(new CollectAppInterceptRunnable("noNeed", calledPackageName2, "noNeed", "activity"));
                    return true;
                }
            }
        } else if (!this.mDebugSwitch) {
            return false;
        } else {
            Log.v(ColorAppStartupMonitorInfo.TAG, "handleStartActivity aInfo == null || aInfo.packageName == null return.");
            return false;
        }
    }

    public boolean checkAbnormalActivityCall(Intent intent, String callerPkg, String calledPkg, String cpnName, int callingPid, int callingUid, String reason) {
        long time;
        if (!this.mSwitchInterceptActivity) {
            return false;
        }
        if (this.mAms == null || callerPkg == null || calledPkg == null || cpnName == null) {
            if (this.mDebugSwitch) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "checkAbnormalActivityCall callerPkg == null ||calledPkg == null ||cpnName == null return.");
            }
            return false;
        }
        if (this.mDynamicDebug) {
            time = SystemClock.elapsedRealtime();
        } else {
            time = 0;
        }
        boolean isScreeOn = isScreenOn();
        ComponentName topCpn = getTopComponentName();
        if (topCpn == null) {
            if (this.mDebugSwitch) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "checkAbnormalActivityCall topCpn = null return.");
            }
            return false;
        }
        String topPkg = topCpn.getPackageName() != null ? topCpn.getPackageName() : "";
        if (OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall()) {
            if (this.mDebugSwitch) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "checkAbnormalActivityCall callerPkg in cts whitelist return.");
            }
            return false;
        } else if (isScreeOn && topPkg.equals(callerPkg)) {
            if (this.mDebugSwitch) {
                Log.d(ColorAppStartupMonitorInfo.TAG, callerPkg + " callerPkg is FG!");
            }
            return false;
        } else if ("android".equals(topPkg)) {
            if (this.mDebugSwitch) {
                Log.d(ColorAppStartupMonitorInfo.TAG, callerPkg + " topPkg is android!");
            }
            return false;
        } else if (isScreeOn && !TextUtils.isEmpty(reason) && this.mStartActivityReasonList.contains(reason)) {
            if (this.mDebugSwitch) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "checkAbnormalActivityCall " + reason + " return.");
            }
            return false;
        } else if (!isScreeOn || !getResumePkgList().contains(callerPkg)) {
            String action = intent.getAction();
            String url = intent.getDataString();
            if (action != null && url != null) {
                if (action.equals("android.intent.action.VIEW")) {
                    if (url.toLowerCase().startsWith("http")) {
                        if (!OppoListManager.getInstance().isInBrowserWhiteList(callerPkg)) {
                            if (BROWSER_LIST.contains(calledPkg) && calledPkg.equals(callerPkg)) {
                                if (this.mDynamicDebug) {
                                    Log.d(ColorAppStartupMonitorInfo.TAG, callerPkg + "is OPPO browser and it's calling itself, return");
                                }
                                ColorAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsBrowser");
                                return false;
                            } else if (isInputMethodApplication(callerPkg)) {
                                if (this.mDynamicDebug) {
                                    Log.d(ColorAppStartupMonitorInfo.TAG, callerPkg + " is starting a web view, we don't intercept it.");
                                }
                                ColorAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsBrowser_Im");
                                return false;
                            } else {
                                sendBroadcastForUrlIntercept(callerPkg, url, calledPkg, cpnName);
                                Log.v(ColorAppStartupMonitorInfo.TAG, "Sending Broadcast for internet intercept. third browser is called in Bg, undo!");
                                return true;
                            }
                        }
                    }
                }
            }
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "handleStartAppInfo cost time ==  " + (SystemClock.elapsedRealtime() - time));
            }
            return false;
        } else {
            if (this.mDebugSwitch) {
                Log.d(ColorAppStartupMonitorInfo.TAG, callerPkg + " callerPkg is focused! return.");
            }
            return false;
        }
    }

    private void initStartActivityReason() {
        this.mStartActivityReasonList.add("startActivityInPackage");
        this.mStartActivityReasonList.add("startActivityFromRecents");
    }

    private void sendBroadcastForUrlIntercept(String callingPkg, String url, String pkgName, String className) {
        if (this.mSwitchBrowserInterceptUpload) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "sendBroadcastForUrlIntercept callingPkg = " + callingPkg + "  pkgName = " + pkgName + "  className= " + className);
            Intent intent = new Intent(OPPO_SAFE_URL_INTERCEPTION);
            intent.putExtra("caller", callingPkg);
            intent.putExtra("url", url);
            intent.putExtra("pkgName", pkgName);
            intent.putExtra("className", className);
            ActivityManagerService activityManagerService = this.mAms;
            if (activityManagerService != null && activityManagerService.mContext != null) {
                this.mHandler.post(new Runnable(intent) {
                    /* class com.android.server.am.$$Lambda$ColorAppStartupManager$yydTnPpw7gzb2mBB7xBjlBjedg8 */
                    private final /* synthetic */ Intent f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ColorAppStartupManager.this.lambda$sendBroadcastForUrlIntercept$0$ColorAppStartupManager(this.f$1);
                    }
                });
            }
        }
    }

    public /* synthetic */ void lambda$sendBroadcastForUrlIntercept$0$ColorAppStartupManager(Intent intent) {
        this.mAms.mContext.sendBroadcast(intent, "oppo.permission.OPPO_COMPONENT_SAFE");
    }

    private boolean isSameAsLastActivity(String calledPkg) {
        try {
            String lastPkg = this.mLastCalledAcivityList.get(this.mLastCalledAcivityList.size() - 1).mPkgName;
            if (lastPkg == null || !lastPkg.contains(calledPkg)) {
                return false;
            }
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "isSameAsLastActivity lastPkg = " + lastPkg);
            }
            return true;
        } catch (Exception e) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "isSameAsLastActivity exeption happen!");
            e.printStackTrace();
            return true;
        }
    }

    private boolean checkAccessibilityPkg(String callerPkg) {
        String accessStr = Settings.Secure.getString(this.mAms.mContext.getContentResolver(), "enabled_accessibility_services");
        if (accessStr == null || !accessStr.contains(callerPkg)) {
            return false;
        }
        if (this.mDebugSwitch) {
            Log.v(ColorAppStartupMonitorInfo.TAG, "checkAccessibilityPkg callerPkg contain!");
        }
        return true;
    }

    private boolean isInputMethodApplication(String calledPkg) {
        String str = this.mDefaultInputMethod;
        if (str == null || calledPkg == null || !str.equals(calledPkg)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public String getDefaultInputMethod() {
        String defaultInput = null;
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            try {
                String inputMethod = Settings.Secure.getString(activityManagerService.mContext.getContentResolver(), "default_input_method");
                if (inputMethod != null) {
                    defaultInput = inputMethod.substring(0, inputMethod.indexOf("/"));
                }
            } catch (Exception e) {
                Log.e(ColorAppStartupMonitorInfo.TAG, "Failed to get default input method");
            }
        }
        if (this.mDebugSwitch) {
            Log.i(ColorAppStartupMonitorInfo.TAG, "defaultInputMethod " + defaultInput);
        }
        return defaultInput;
    }

    private List<String> getResumePkgList() {
        return this.mAppStartupHelper.getResumePkgList(this.mAms);
    }

    private String[] getActiveAudioPids() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mAms.mContext.getSystemService("audio");
        }
        AudioManager audioManager = this.mAudioManager;
        if (audioManager == null) {
            return null;
        }
        return getActiveAudioPids(audioManager.getParameters("get_pid"));
    }

    private String[] getActiveAudioPids(String pids) {
        if (pids == null || pids.length() == 0 || !pids.contains(":")) {
            return null;
        }
        return pids.split(":");
    }

    private ArrayList<ProcessRecord> getProcessForUidLocked(int uid) {
        ArrayList<ProcessRecord> res = new ArrayList<>();
        try {
            for (int i = this.mAms.mProcessList.mLruProcesses.size() - 1; i >= 0; i--) {
                ProcessRecord rec = (ProcessRecord) this.mAms.mProcessList.mLruProcesses.get(i);
                if (rec.thread != null && rec.uid == uid) {
                    res.add(rec);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /* access modifiers changed from: protected */
    public boolean isInLruProcessesLocked(int uid) {
        boolean z;
        synchronized (this.mAms) {
            z = this.mAms.mOomAdjuster.mActiveUids.get(uid) != null;
        }
        return z;
    }

    private boolean checkAppExist(String pkg) {
        boolean result = false;
        if (this.mAms == null || pkg == null || pkg.isEmpty()) {
            return false;
        }
        synchronized (this.mAms) {
            int i = this.mAms.mProcessList.mLruProcesses.size() - 1;
            while (true) {
                if (i >= 0) {
                    ProcessRecord proc = (ProcessRecord) this.mAms.mProcessList.mLruProcesses.get(i);
                    if (proc != null && proc.thread != null && proc.processName != null && proc.processName.startsWith(pkg)) {
                        result = true;
                        break;
                    }
                    i--;
                } else {
                    break;
                }
            }
        }
        return result;
    }

    public boolean inSeviceCpnlist(String cpnClassName) {
        boolean result;
        synchronized (this.mServiceLock) {
            result = this.mSeviceCpnBlacklist.contains(cpnClassName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inSeviceCpnlist result = " + result);
        }
        return result;
    }

    public boolean inReceiverlist(String cpnClassName) {
        boolean result;
        synchronized (this.mReceiverLock) {
            result = this.mReceiverBlackList.contains(cpnClassName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inReceiverlist result = " + result);
        }
        return result;
    }

    public boolean inReceiverActionlist(String action) {
        boolean result;
        synchronized (this.mReceiverActionLock) {
            result = this.mReceiverActionBlackList.contains(action);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inReceiverActionlist result = " + result);
        }
        return result;
    }

    private boolean inReceiverActionExcludelist(String action, String packageName) {
        boolean result = false;
        synchronized (mBlackListReceiver) {
            List<String> packages = mBlackListReceiver.get(action);
            if (packages != null) {
                Iterator<String> it = packages.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (it.next().equals(packageName)) {
                        result = !"com.google.android.marvin.talkback".equals(packageName) || this.mAms == null || !ColorAccessibilityUtil.isTalkbackEnabled(this.mAms.mContext);
                    }
                }
            }
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inReceiverActionExcludelist result = " + result);
        }
        return result;
    }

    public boolean inProviderlist(String cpnClassName) {
        boolean result;
        synchronized (this.mProviderLock) {
            result = this.mProviderBlacklist.contains(cpnClassName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inProviderlist result = " + result);
        }
        return result;
    }

    public boolean inActivityPushBlacklist(String cpnClassName) {
        boolean result;
        synchronized (this.mActivityLock) {
            result = this.mActivityBlackList.contains(cpnClassName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inActivityPushBlacklist result = " + result);
        }
        return result;
    }

    public boolean inActionlist(String action) {
        boolean result;
        synchronized (this.mActionLock) {
            result = this.mActionBlackList.contains(action);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inActionlist result = " + result);
        }
        return result;
    }

    public boolean inBlackguardList(String action) {
        boolean result;
        synchronized (this.mBlackguardLock) {
            result = this.mBlackguardList.contains(action);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inBlackguardList result = " + result);
        }
        return result;
    }

    public boolean inActivityCallerWhitePkgList(String pkgName) {
        boolean result;
        synchronized (this.mActivityCallerWhitePkgLock) {
            result = this.mActivityCallerWhitePkgList.contains(pkgName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inActivityCallerWhitePkgList result = " + result);
        }
        return result;
    }

    public boolean inActivityCalledWhitePkgList(String pkgName) {
        boolean result;
        synchronized (this.mActivityCalledWhitePkgLock) {
            result = this.mActivityCalledWhitePkgList.contains(pkgName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inActivityCalledWhitePkgList result = " + result);
        }
        return result;
    }

    public boolean inActivityCalledWhiteCpnList(String pkgName) {
        boolean result;
        synchronized (this.mActivityCalledWhiteCpnLock) {
            result = this.mActivityCalledWhiteCpnList.contains(pkgName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inActivityCalledWhiteCpnList result = " + result);
        }
        return result;
    }

    public boolean inActivityPkgKeyList(String pkgName) {
        boolean result = false;
        synchronized (this.mActivityPkgKeyLock) {
            Iterator<String> it = this.mActivityPkgKeyList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                } else if (pkgName.contains(it.next())) {
                    result = true;
                    break;
                }
            }
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inActivityPkgKeyList result = " + result);
        }
        return result;
    }

    public boolean inActivityCalledKeyList(String pkgName) {
        boolean result;
        synchronized (this.mActivityCalledKeyLock) {
            result = this.mActivityCalledKeyList.contains(pkgName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inActivityCalledKeyList result = " + result);
        }
        return result;
    }

    public boolean inBlackguardActivityList(String activityCpn) {
        boolean result;
        synchronized (this.mBlackguardActivityLock) {
            result = this.mBlackguardActivityList.contains(activityCpn);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inBlackguardActivityList result = " + result);
        }
        return result;
    }

    public boolean inCustomizeWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mCustomizeWhiteLock) {
            result = this.mCustomizeWhiteList.contains(pkgName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inCustomizeWhiteList result = " + result);
        }
        return result;
    }

    public boolean inBuildBlackList(String pkgName) {
        boolean result;
        synchronized (this.mBuildAppBlackLock) {
            result = this.mBuildAppBlackList.contains(pkgName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, pkgName + " inBuildBlackList result = " + result);
        }
        return result;
    }

    public boolean inStartServiceWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mStartServiceWhiteLock) {
            result = this.mStartServiceWhiteList.contains(pkgName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, pkgName + " inStartServiceWhiteList result = " + result);
        }
        return result;
    }

    public boolean inStartServiceWhiteCpnList(ServiceRecord s) {
        boolean result = false;
        if (s.name != null) {
            synchronized (this.mStartServiceWhiteCpnLock) {
                result = this.mStartServiceWhiteCpnList.contains(s.name.getClassName());
            }
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, s.name.getClassName() + " mStartServiceWhiteCpnList result = " + result);
            }
        }
        return result;
    }

    public boolean inBindServiceCpnWhiteList(String cpnName) {
        boolean result;
        synchronized (this.mBindServiceWhiteLock) {
            if (this.mBindServiceCpnWhiteList.isEmpty()) {
                result = ColorAppStartupListManager.getInstance().isInBindServiceCpnList(cpnName);
            } else {
                result = this.mBindServiceCpnWhiteList.contains(cpnName);
            }
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, cpnName + " inBindServiceCpnWhiteList result = " + result);
        }
        return result;
    }

    public boolean inJobWhiteList(String pkgName) {
        boolean result = false;
        if (pkgName != null) {
            synchronized (this.mJobWhiteLock) {
                result = this.mJobWhiteList.contains(pkgName);
            }
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, pkgName + " inJobWhiteList result = " + result);
        }
        return result;
    }

    public boolean inSyncWhiteList(String pkgName) {
        boolean result = false;
        if (pkgName != null) {
            synchronized (this.mSyncWhiteLock) {
                result = this.mSyncWhiteList.contains(pkgName);
            }
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, pkgName + " inSyncWhiteList result = " + result);
        }
        return result;
    }

    public boolean inNotificationWhiteList(String pkgName) {
        boolean result = false;
        if (pkgName != null) {
            synchronized (this.mNotifyWhiteLock) {
                result = this.mNotifyWhiteList.contains(pkgName);
            }
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, pkgName + " inNotificationWhiteList result = " + result);
        }
        return result;
    }

    public boolean inProviderCpnWhiteList(String providerName) {
        boolean result;
        synchronized (this.mProviderWhiteLock) {
            if (this.mProviderCpnWhiteList.isEmpty()) {
                result = ColorAppStartupListManager.getInstance().isInProviderCpnList(providerName);
            } else {
                result = this.mProviderCpnWhiteList.contains(providerName);
            }
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, providerName + " inProviderCpnWhiteList result = " + result);
        }
        return result;
    }

    public boolean inBroadCastWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mBroadcastWhiteLock) {
            result = this.mBroadcastWhiteList.contains(pkgName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, pkgName + " inBroadCastWhiteList result = " + result);
        }
        return result;
    }

    public boolean inBroadCastActionWhiteList(Intent intent) {
        boolean result = false;
        if (!(intent == null || intent.getAction() == null)) {
            synchronized (this.mBroadcastActionWhiteLock) {
                result = this.mBroadcastActionWhiteList.contains(intent.getAction());
            }
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, intent.getAction() + " inBroadCastActionWhiteList result = " + result);
            }
        }
        return result;
    }

    public boolean inProtectWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mProtectLock) {
            if (this.mProtectList.isEmpty()) {
                result = ColorAppStartupListManager.getInstance().isInProtectWhiteList(pkgName);
            } else {
                result = this.mProtectList.contains(pkgName);
            }
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, pkgName + " inProtectWhiteList result = " + result);
        }
        return result;
    }

    public boolean inGlobalWhiteList(String pkgName) {
        if (this.mGlobalWhiteList.contains(pkgName)) {
            return true;
        }
        return false;
    }

    public boolean inCollectAppStartList(String pkgName) {
        boolean result;
        synchronized (this.mCollectAppStartLock) {
            result = this.mCollectAppStartList.contains(pkgName);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, pkgName + " mCollectAppStartList result = " + result);
        }
        return result;
    }

    public boolean inProviderCallerPkgList(String callerPkg) {
        boolean result;
        synchronized (this.mProviderCallerPkgLock) {
            if (this.mProviderCallerPkgList.isEmpty()) {
                result = ColorAppStartupListManager.getInstance().isInProviderCallerPkgList(callerPkg);
            } else {
                result = this.mProviderCallerPkgList.contains(callerPkg);
            }
        }
        if (this.mDynamicDebug) {
            Log.i(ColorAppStartupMonitorInfo.TAG, "inProviderCallerPkgList: result=" + result + ", callerPkg=" + callerPkg);
        }
        return result;
    }

    public boolean inBackgroundRestrictCallerPkgList(String callerPkgName, String action) {
        List<String> actionList;
        List<String> actionList2;
        boolean z;
        boolean result = false;
        synchronized (this.mBackgroundRestrictCallerPkgLock) {
            if (this.mBackgroundRestrictCallerPkgMap.containsKey(callerPkgName) && (actionList2 = this.mBackgroundRestrictCallerPkgMap.get(callerPkgName)) != null) {
                if (!actionList2.contains(action)) {
                    if (!actionList2.contains("all")) {
                        z = false;
                        result = z;
                    }
                }
                z = true;
                result = z;
            }
            if (!result && this.mBackgroundRestrictCallerPkgMap.containsKey("all") && (actionList = this.mBackgroundRestrictCallerPkgMap.get("all")) != null) {
                result = actionList.contains(action);
            }
        }
        if (this.mDynamicDebug) {
            Log.i(ColorAppStartupMonitorInfo.TAG, "inBackgroundRestrictCallerPkgList: result=" + result + ", callerPkg=" + callerPkgName + ", action=" + action);
        }
        return result;
    }

    public boolean inStartServiceCallerPkgList(String callerPkgName, String action) {
        List<String> actionList;
        List<String> actionList2;
        boolean z;
        boolean result = false;
        synchronized (this.mStartServiceCallerPkgLock) {
            if (this.mStartServiceCallerPkgMap.containsKey(callerPkgName) && (actionList2 = this.mStartServiceCallerPkgMap.get(callerPkgName)) != null) {
                if (!actionList2.contains(action)) {
                    if (!actionList2.contains("all")) {
                        z = false;
                        result = z;
                    }
                }
                z = true;
                result = z;
            }
            if (!result && this.mStartServiceCallerPkgMap.containsKey("all") && (actionList = this.mStartServiceCallerPkgMap.get("all")) != null) {
                result = actionList.contains(action);
            }
        }
        if (this.mDynamicDebug) {
            Log.i(ColorAppStartupMonitorInfo.TAG, "inStartServiceCallerPkgList: result=" + result + ", callerPkg=" + callerPkgName + ", action=" + action);
        }
        return result;
    }

    public boolean inBindServiceCallerPkgList(String callerPkgName, String action) {
        List<String> actionList;
        List<String> actionList2;
        boolean z;
        boolean result = false;
        synchronized (this.mBindServiceCallerPkgLock) {
            if (this.mBindServiceCallerPkgMap.containsKey(callerPkgName) && (actionList2 = this.mBindServiceCallerPkgMap.get(callerPkgName)) != null) {
                if (!actionList2.contains(action)) {
                    if (!actionList2.contains("all")) {
                        z = false;
                        result = z;
                    }
                }
                z = true;
                result = z;
            }
            if (!result && this.mBindServiceCallerPkgMap.containsKey("all") && (actionList = this.mBindServiceCallerPkgMap.get("all")) != null) {
                result = actionList.contains(action);
            }
        }
        if (this.mDynamicDebug) {
            Log.i(ColorAppStartupMonitorInfo.TAG, "inBindServiceCallerPkgList: result=" + result + ", callerPkg=" + callerPkgName + ", action=" + action);
        }
        return result;
    }

    public void updateConfiglist() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateConfiglist!");
        }
        updateSeviceCpnBlacklist();
        updateReceiverBlackList();
        updateReceiverActionBlackList();
        updateProviderBlackList();
        updateActivityBlackList();
        updateActionBlackList();
        updateBlackguardList();
        updateActivityCallerWhitePkgList();
        updateActivityCalledWhitePkgList();
        updateActivityCalledWhiteCpnList();
        updateActivityPkgKeyList();
        updateActivityCalledKeyList();
        updateBlackguardActivityList();
        updateActivitySelfCallWhitePkgList();
        updateActivityAssociateWhitePkgMap();
        updateActivityAssociateBlackPkgMap();
        updateActivitySleepCpnBlackList();
        updateBuildBlackList();
        updateStartServiceWhiteList();
        updateStartServiceWhiteCpnList();
        updateBindServiceWhiteList();
        updateJobWhiteList();
        updateSyncWhiteList();
        updateNotifyWhiteList();
        updateProviderWhiteList();
        updateBroadcastWhiteList();
        updateBroadcastWhiteActionList();
        updateProtectList();
        updateGlobalWhiteList();
        updateBlackListReceiver();
        updateAuthorizeCpnList();
        updateProviderCallerPkgList();
        updateBackgrounRestrictMap();
        updateStartServiceMap();
        updateBindServiceMap();
        updateCpnUpperLimitConfig();
        if (!this.mAms.mContext.getPackageManager().hasSystemFeature("oppo.customize.auto.start.disabled")) {
            this.mSwitchInterceptActivity = ColorAppStartupManagerUtils.getInstance().getSwitchInterceptActivity();
        }
        this.mSwitchBrowserInterceptUpload = ColorAppStartupManagerUtils.getInstance().getSwitchBrowserInterceptUpload();
        clearStartManagerList();
        updateExpFeature();
    }

    private void updateExpFeature() {
        this.isExpVersion = ColorAppStartupManagerUtils.getInstance().getExpFeature();
    }

    public void updateMonitorlist() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateMonitorlist!");
        }
        updateMonitorAppStartList();
        clearMonitorList();
    }

    /* access modifiers changed from: protected */
    public void updateAssociateStartList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateAssociateStartWhiteList!");
        }
        updateAssociateStartWhiteList();
    }

    private void clearStartManagerList() {
        ColorAppStartupManagerUtils.getInstance().cleanConfigList();
    }

    private void clearMonitorList() {
        ColorAppStartupManagerUtils.getInstance().cleanMonitorList();
    }

    private void updateSeviceCpnBlacklist() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateSeviceCpnBlacklist!");
        }
        synchronized (this.mServiceLock) {
            this.mSeviceCpnBlacklist.clear();
            this.mSeviceCpnBlacklist.addAll(ColorAppStartupManagerUtils.getInstance().getSeviceCpnBlacklist());
        }
    }

    private void updateReceiverBlackList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateReceiverBlackList!");
        }
        synchronized (this.mReceiverLock) {
            this.mReceiverBlackList.clear();
            this.mReceiverBlackList.addAll(ColorAppStartupManagerUtils.getInstance().getReceiverBlackList());
        }
    }

    private void updateReceiverActionBlackList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateReceiverActionBlackList!");
        }
        synchronized (this.mReceiverActionLock) {
            this.mReceiverActionBlackList.clear();
            this.mReceiverActionBlackList.addAll(ColorAppStartupManagerUtils.getInstance().getReceiverActionBlackList());
        }
    }

    private void updateProviderBlackList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateProviderBlackList!");
        }
        synchronized (this.mProviderLock) {
            this.mProviderBlacklist.clear();
            this.mProviderBlacklist.addAll(ColorAppStartupManagerUtils.getInstance().getProviderBlackList());
        }
    }

    private void updateActivityBlackList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateActivityBlackList!");
        }
        synchronized (this.mActivityLock) {
            this.mActivityBlackList.clear();
            this.mActivityBlackList.addAll(ColorAppStartupManagerUtils.getInstance().getActivityBlackList());
        }
    }

    private void updateActionBlackList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateActionBlackList!");
        }
        synchronized (this.mActionLock) {
            this.mActionBlackList.clear();
            this.mActionBlackList.addAll(ColorAppStartupManagerUtils.getInstance().getActionBlackList());
        }
    }

    private void updateBlackguardList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateBlackguardList!");
        }
        synchronized (this.mBlackguardLock) {
            this.mBlackguardList.clear();
            this.mBlackguardList.addAll(ColorAppStartupManagerUtils.getInstance().getBlackguardList());
        }
    }

    private void updateActivityCallerWhitePkgList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateActivityCallerWhitePkgList!");
        }
        synchronized (this.mActivityCallerWhitePkgLock) {
            this.mActivityCallerWhitePkgList.clear();
            this.mActivityCallerWhitePkgList.addAll(ColorAppStartupManagerUtils.getInstance().getActivityCallerWhitePkgList());
        }
    }

    private void updateActivityCalledWhitePkgList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateActivityCalledWhitePkgList!");
        }
        synchronized (this.mActivityCalledWhitePkgLock) {
            this.mActivityCalledWhitePkgList.clear();
            this.mActivityCalledWhitePkgList.addAll(ColorAppStartupManagerUtils.getInstance().getActivityCalledWhitePkgList());
        }
    }

    private void updateActivityCalledWhiteCpnList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateActivityCalledWhiteCpnList!");
        }
        synchronized (this.mActivityCalledWhiteCpnLock) {
            this.mActivityCalledWhiteCpnList.clear();
            this.mActivityCalledWhiteCpnList.addAll(ColorAppStartupManagerUtils.getInstance().getActivityCalledWhiteCpnList());
        }
    }

    private void updateActivityPkgKeyList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateActivityPkgKeyList!");
        }
        synchronized (this.mActivityPkgKeyLock) {
            this.mActivityPkgKeyList.clear();
            this.mActivityPkgKeyList.addAll(ColorAppStartupManagerUtils.getInstance().getActivityPkgKeyList());
        }
    }

    private void updateActivityCalledKeyList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateActivityCalledKeyList!");
        }
        synchronized (this.mActivityCalledKeyLock) {
            this.mActivityCalledKeyList.clear();
            this.mActivityCalledKeyList.addAll(ColorAppStartupManagerUtils.getInstance().getActivityCalledKeyList());
        }
    }

    private void updateBlackguardActivityList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateBlackguardActivityList!");
        }
        synchronized (this.mBlackguardActivityLock) {
            this.mBlackguardActivityList.clear();
            this.mBlackguardActivityList.addAll(ColorAppStartupManagerUtils.getInstance().getBlackguardActivityList());
        }
    }

    private void updateActivitySelfCallWhitePkgList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateActivitySelfCallWhitePkgList!");
        }
        synchronized (this.mActivitySelfCallWhitePkgList) {
            this.mActivitySelfCallWhitePkgList.clear();
            this.mActivitySelfCallWhitePkgList.addAll(ColorAppStartupManagerUtils.getInstance().getActivitySelfCallWhitePkgList());
        }
    }

    private void updateActivityAssociateWhitePkgMap() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateActivityAssociateWhitePkgMap!");
        }
        synchronized (this.mActivityAssociateWhitePkgMap) {
            this.mActivityAssociateWhitePkgMap.clear();
            this.mActivityAssociateWhitePkgMap.putAll((ArrayMap<? extends String, ? extends List<String>>) ColorAppStartupManagerUtils.getInstance().getActivityAssociateWhitePkgMap());
        }
    }

    private void updateActivityAssociateBlackPkgMap() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateActivityAssociateBlackPkgMap!");
        }
        synchronized (this.mActivityAssociateBlackPkgMap) {
            this.mActivityAssociateBlackPkgMap.clear();
            this.mActivityAssociateBlackPkgMap.putAll((ArrayMap<? extends String, ? extends List<String>>) ColorAppStartupManagerUtils.getInstance().getActivityAssociateBlackPkgMap());
        }
    }

    private void updateActivitySleepCpnBlackList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateActivitySleepCpnBlackList!");
        }
        synchronized (this.mActivitySleepCpnBlackList) {
            this.mActivitySleepCpnBlackList.clear();
            this.mActivitySleepCpnBlackList.addAll(ColorAppStartupManagerUtils.getInstance().getActivitySleepCpnBlackList());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0064, code lost:
        return true;
     */
    private boolean inActivityAssociateWhitePkgMap(String callerPkg, String calledPkg) {
        synchronized (this.mActivityAssociateWhitePkgMap) {
            for (int i = 0; i < this.mActivityAssociateWhitePkgMap.size(); i++) {
                String key = this.mActivityAssociateWhitePkgMap.keyAt(i);
                List<String> calledPkgList = this.mActivityAssociateWhitePkgMap.valueAt(i);
                if (!TextUtils.isEmpty(key) && calledPkgList != null && key.equals(callerPkg)) {
                    for (String called : calledPkgList) {
                        if (called.equals(calledPkg)) {
                            if (this.mDynamicDebug) {
                                Log.d(ColorAppStartupMonitorInfo.TAG, "inActivityAssociateWhitePkgMap callerPkg = " + key + "calledPkg = " + called);
                            }
                        }
                    }
                    continue;
                }
            }
            return false;
        }
    }

    private boolean inActivitySelfCallWhitePkgList(String callerPkg) {
        boolean result;
        synchronized (this.mActivitySelfCallWhitePkgList) {
            result = this.mActivitySelfCallWhitePkgList.contains(callerPkg);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inActivitySelfCallWhitePkgList result = " + result);
        }
        return result;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0064, code lost:
        return true;
     */
    private boolean inActivityAssociateBlackPkgMap(String callerPkg, String calledPkg) {
        synchronized (this.mActivityAssociateBlackPkgMap) {
            for (int i = 0; i < this.mActivityAssociateBlackPkgMap.size(); i++) {
                String key = this.mActivityAssociateBlackPkgMap.keyAt(i);
                List<String> calledPkgList = this.mActivityAssociateBlackPkgMap.valueAt(i);
                if (!TextUtils.isEmpty(key) && calledPkgList != null && key.equals(callerPkg)) {
                    for (String called : calledPkgList) {
                        if (called.equals(calledPkg)) {
                            if (this.mDynamicDebug) {
                                Log.d(ColorAppStartupMonitorInfo.TAG, "inActivityAssociateBlackPkgMap callerPkg = " + key + "calledPkg = " + called);
                            }
                        }
                    }
                    continue;
                }
            }
            return false;
        }
    }

    private boolean inActivitySleepCpnBlackList(String calledCpn) {
        boolean result;
        synchronized (this.mActivitySleepCpnBlackList) {
            result = this.mActivitySleepCpnBlackList.contains(calledCpn);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "inActivitySleepCpnBlackList result = " + result);
        }
        return result;
    }

    private void updateCustomizeWhiteList() {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "updateCustomizeWhiteList!");
        }
        synchronized (this.mCustomizeWhiteLock) {
            this.mCustomizeWhiteList.clear();
            this.mCustomizeWhiteList.addAll(ColorAppStartupManagerUtils.getInstance().getCustomizeWhiteList());
        }
    }

    private void updateGlobalWhiteList() {
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null && activityManagerService.mContext != null) {
            if (this.mDebugSwitch) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "updateGlobalWhiteList!");
            }
            this.mGlobalWhiteList = OppoListManager.getInstance().getGlobalWhiteList(this.mAms.mContext);
        }
    }

    private void updateMonitorAppStartList() {
        synchronized (this.mCollectAppStartLock) {
            this.mCollectAppStartList.clear();
            this.mCollectAppStartList.addAll(ColorAppStartupManagerUtils.getInstance().getCollectAppStartList());
        }
    }

    private void updateBlackListReceiver() {
        synchronized (mBlackListReceiver) {
            mBlackListReceiver.clear();
            mBlackListReceiver.putAll((ArrayMap<? extends String, ? extends List<String>>) ColorAppStartupManagerUtils.getInstance().getReceiverExcludeBlackList());
        }
        if (mBlackListReceiver.isEmpty()) {
            mBlackListReceiver.put(BrightnessConstants.ACTION_BOOT_COMPLETED, Arrays.asList("com.android.vending", "com.google.android.marvin.talkback"));
            mBlackListReceiver.put("android.intent.action.LOCKED_BOOT_COMPLETED", Arrays.asList("com.google.android.marvin.talkback"));
        }
    }

    private void updateBuildBlackList() {
        synchronized (this.mBuildAppBlackLock) {
            this.mBuildAppBlackList.clear();
            this.mBuildAppBlackList.addAll(ColorAppStartupManagerUtils.getInstance().getBuildBlackList());
        }
    }

    private void updateStartServiceWhiteList() {
        synchronized (this.mStartServiceWhiteLock) {
            this.mStartServiceWhiteList.clear();
            this.mStartServiceWhiteList.addAll(ColorAppStartupManagerUtils.getInstance().getStartServiceWhiteList());
        }
    }

    private void updateStartServiceWhiteCpnList() {
        synchronized (this.mStartServiceWhiteCpnLock) {
            this.mStartServiceWhiteCpnList.clear();
            this.mStartServiceWhiteCpnList.addAll(ColorAppStartupManagerUtils.getInstance().getStartServiceWhiteCpnList());
        }
    }

    private void updateBindServiceWhiteList() {
        synchronized (this.mBindServiceCpnWhiteList) {
            this.mBindServiceCpnWhiteList.clear();
            this.mBindServiceCpnWhiteList.addAll(ColorAppStartupManagerUtils.getInstance().getBindServiceWhiteList());
        }
    }

    private void updateJobWhiteList() {
        synchronized (this.mJobWhiteLock) {
            this.mJobWhiteList.clear();
            this.mJobWhiteList.addAll(ColorAppStartupManagerUtils.getInstance().getJobWhiteList());
        }
    }

    private void updateSyncWhiteList() {
        synchronized (this.mSyncWhiteLock) {
            this.mSyncWhiteList.clear();
            this.mSyncWhiteList.addAll(ColorAppStartupManagerUtils.getInstance().getSyncWhiteList());
        }
    }

    private void updateNotifyWhiteList() {
        synchronized (this.mNotifyWhiteLock) {
            this.mNotifyWhiteList.clear();
            this.mNotifyWhiteList.addAll(ColorAppStartupManagerUtils.getInstance().getNotifyWhiteList());
        }
    }

    private void updateProviderWhiteList() {
        synchronized (this.mProviderWhiteLock) {
            this.mProviderCpnWhiteList.clear();
            this.mProviderCpnWhiteList.addAll(ColorAppStartupManagerUtils.getInstance().getProviderWhiteList());
        }
    }

    private void updateBroadcastWhiteList() {
        synchronized (this.mBroadcastWhiteLock) {
            this.mBroadcastWhiteList.clear();
            this.mBroadcastWhiteList.addAll(ColorAppStartupManagerUtils.getInstance().getBroadcastWhiteList());
        }
    }

    private void updateBroadcastWhiteActionList() {
        synchronized (this.mBroadcastActionWhiteLock) {
            this.mBroadcastActionWhiteList.clear();
            this.mBroadcastActionWhiteList.addAll(ColorAppStartupManagerUtils.getInstance().getBroadcastActionWhiteList());
        }
    }

    private void updateProtectList() {
        synchronized (this.mProtectLock) {
            this.mProtectList.clear();
            this.mProtectList.addAll(ColorAppStartupManagerUtils.getInstance().getProtectList());
        }
    }

    private void updateAssociateStartWhiteList() {
        synchronized (this.mAssociateStartWhiteLock) {
            this.mAssociateStartWhiteListContainer.clear();
            SparseArray<List<String>> container = ColorAppStartupManagerUtils.getInstance().getAssociateStartWhiteListContainer();
            int size = container.size();
            for (int i = 0; i < size; i++) {
                this.mAssociateStartWhiteListContainer.put(container.keyAt(i), container.valueAt(i));
            }
        }
    }

    private void updateAuthorizeCpnList() {
        synchronized (this.mAuthorizeCpnListLock) {
            this.mAuthorizeCpnList.clear();
            this.mAuthorizeCpnList.addAll(ColorAppStartupManagerUtils.getInstance().getAuthorizeCpnList());
        }
    }

    private void updateProviderCallerPkgList() {
        synchronized (this.mProviderCallerPkgLock) {
            this.mProviderCallerPkgList.clear();
            this.mProviderCallerPkgList.addAll(ColorAppStartupManagerUtils.getInstance().getProviderCallerPkgList());
        }
    }

    private void updateBackgrounRestrictMap() {
        synchronized (this.mBackgroundRestrictCallerPkgLock) {
            this.mBackgroundRestrictCallerPkgMap.clear();
            this.mBackgroundRestrictCallerPkgMap.putAll((ArrayMap<? extends String, ? extends List<String>>) ColorAppStartupManagerUtils.getInstance().getBackgroundRestrictMap());
        }
    }

    private void updateStartServiceMap() {
        synchronized (this.mStartServiceCallerPkgLock) {
            this.mStartServiceCallerPkgMap.clear();
            this.mStartServiceCallerPkgMap.putAll((ArrayMap<? extends String, ? extends List<String>>) ColorAppStartupManagerUtils.getInstance().getStartServiceMap());
        }
    }

    private void updateBindServiceMap() {
        synchronized (this.mBindServiceCallerPkgLock) {
            this.mBindServiceCallerPkgMap.clear();
            this.mBindServiceCallerPkgMap.putAll((ArrayMap<? extends String, ? extends List<String>>) ColorAppStartupManagerUtils.getInstance().getBindServiceMap());
        }
    }

    /* access modifiers changed from: private */
    public boolean isInGamePayAbnormalBlackUrlKeyList(String url) {
        for (String key : ColorAppStartupManagerUtils.getInstance().getmGamePayAbnormalBlackUrlKeyList()) {
            if (url.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isAppStartForbidden(String packageName) {
        if (!OppoListManager.getInstance().isAppStartForbidden(packageName)) {
            return false;
        }
        if (this.mSwitchMonitor) {
            this.mHandler.post(new CollectAppInterceptRunnable("noNeed", packageName, "noNeed", "service"));
        }
        Log.i(ColorAppStartupMonitorInfo.TAG, packageName + " is forbidden to start by service");
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isAllowStartFromStartService(ProcessRecord callerApp, int callingPid, int callingUid, String callerPkgName, ServiceRecord s, Intent intent) {
        String callerPkg;
        if ((isExpVersion() && !isExGlobalBlackList(s.appInfo.packageName)) || isRootOrShell(callingUid)) {
            return true;
        }
        if (inStartServiceWhiteList(s.appInfo.packageName)) {
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "inStartServiceWhiteList: " + s.appInfo.packageName);
            }
            return true;
        } else if (inStartServiceWhiteCpnList(s)) {
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "inStartServiceWhiteCpnList: " + s.name.getClassName());
            }
            return true;
        } else {
            String action = intent.getAction();
            if (callerPkgName == null || action == null || !inStartServiceCallerPkgList(callerPkgName, action)) {
                if (callingPid == -1) {
                    if (hasExtra(intent, "android.intent.extra.ALARM_COUNT")) {
                        String callerPkg2 = getPackageNameForUid(callingUid);
                        if (callerPkg2 == null) {
                            callerPkg = "system[alarmManger]";
                        } else {
                            callerPkg = composePackage("system[alarmManger]", callerPkg2);
                        }
                        return isAllowStartFromService(null, callerPkg, callingUid, s, intent, "ssfa");
                    }
                }
                if (callerApp == null && s.appInfo != null && callingUid == s.appInfo.uid) {
                    return isAllowStartFromService(null, getPackageNameForUid(callingUid), callingUid, s, intent, "sscn");
                }
                return isAllowStartFromService(callerApp, "", callingUid, s, intent, "ss");
            }
            if (this.mDynamicDebug) {
                Log.i(ColorAppStartupMonitorInfo.TAG, "inStartServiceCallerPkgList: caller=" + callerPkgName + ", action=" + action);
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isAllowStartFromBindService(ProcessRecord callerApp, String callerPkg, int callingUid, ServiceRecord s, Intent intent, String type) {
        if ((isExpVersion() && !isExGlobalBlackList(s.appInfo.packageName)) || isRootOrShell(callingUid)) {
            return true;
        }
        String action = intent.getAction();
        if (callerPkg != null && action != null && inBindServiceCallerPkgList(callerPkg, action)) {
            if (this.mDynamicDebug) {
                Log.i(ColorAppStartupMonitorInfo.TAG, "inBindServiceCallerPkgList: caller=" + callerPkg + ", action=" + action);
            }
            return true;
        } else if (s != null && s.name != null && inBindServiceCpnWhiteList(s.name.getClassName())) {
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "inBindServiceCpnWhiteList: " + s.appInfo.packageName + ", cpnn " + s.name.getClassName());
            }
            return true;
        } else if (!"system[jobScheduler]".equals(callerPkg) || !inJobWhiteList(s.appInfo.packageName)) {
            return isAllowStartFromService(callerApp, callerPkg, callingUid, s, intent, type);
        } else {
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "inJobWhiteList: " + s.appInfo.packageName);
            }
            return true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x01c9  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x023a  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x024c  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x025f  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x02b7  */
    private boolean isAllowStartFromService(ProcessRecord callerApp, String callerPkg, int callingUid, ServiceRecord s, Intent intent, String type) {
        ColorAppStartupManager colorAppStartupManager;
        String calleePkg;
        String str;
        String calleePkg2;
        Intent intent2;
        String str2;
        String actionStr;
        String calleePkg3;
        String calleePkg4;
        ProcessRecord processRecord;
        String actionStr2;
        String str3;
        String callerPkg2;
        String str4;
        String callerPkg3;
        String str5;
        String str6;
        ColorAppStartupManager colorAppStartupManager2;
        ComponentName cpn;
        String str7;
        String str8;
        ColorAppStartupManager colorAppStartupManager3;
        String str9;
        ColorAppStartupManager colorAppStartupManager4;
        String calleePkg5;
        String str10;
        String str11;
        String calleePkg6;
        String calleePkg7 = s.appInfo.packageName;
        if (isDefaultAllowStart(s.appInfo)) {
            intent2 = intent;
            str = ", intent=";
            colorAppStartupManager = this;
            calleePkg2 = calleePkg7;
            calleePkg = ColorAppStartupMonitorInfo.TAG;
        } else if (isInLruProcessesLocked(s.appInfo.uid)) {
            intent2 = intent;
            str = ", intent=";
            colorAppStartupManager = this;
            calleePkg2 = calleePkg7;
            calleePkg = ColorAppStartupMonitorInfo.TAG;
        } else {
            if (callerApp != null) {
                str9 = " by service  callingUid ";
                str2 = ColorAppStartupMonitorInfo.TAG;
            } else if (!"ssfa".equals(type) && !"sscn".equals(type)) {
                str9 = " by service  callingUid ";
                str2 = ColorAppStartupMonitorInfo.TAG;
            } else if (inProtectWhiteList(calleePkg7) || OppoListManager.getInstance().isInAutoBootWhiteList(calleePkg7, s.userId) || OppoListManager.getInstance().isInstalledAppWidget(calleePkg7, s.appInfo.uid) || ("sscn".equals(type) && OppoListManager.getInstance().isFromNotifyPkg(calleePkg7))) {
                updateLaunchRecord(RECORD_CALLER_ANDROID, calleePkg7, RECORD_AUTO_LAUNCH_ALLOW_MODE, "0", type);
                if (!this.mDebugSwitch) {
                    return true;
                }
                Log.i(ColorAppStartupMonitorInfo.TAG, "start " + calleePkg7 + " by service  callingUid " + callingUid + ", intent=" + intent);
                return true;
            } else {
                str2 = ColorAppStartupMonitorInfo.TAG;
                actionStr = intent.getAction();
                if (actionStr != null || !actionStr.equals("android.media.browser.MediabrowserService")) {
                    processRecord = callerApp;
                    str3 = ", Type ";
                    calleePkg3 = calleePkg7;
                    callerPkg2 = callerPkg;
                    actionStr2 = "prevent start ";
                    calleePkg4 = " callingUid ";
                } else {
                    processRecord = callerApp;
                    if (processRecord.info.uid != 1002) {
                        str3 = ", Type ";
                        actionStr2 = "prevent start ";
                        callerPkg2 = callerPkg;
                        calleePkg3 = calleePkg7;
                        calleePkg4 = " callingUid ";
                    } else if (!isInLruProcessesLocked(s.appInfo.uid)) {
                        updateLaunchRecord(callerPkg, s.appInfo.packageName, "1", "1", type);
                        ComponentName cpnName = intent.getComponent();
                        String cmpStr = cpnName == null ? "null" : cpnName.toShortString();
                        Log.i(str2, "prevent start " + s.appInfo.packageName + ", cmp " + cmpStr + " by " + callerPkg + " callingUid " + callingUid + ", Type " + type);
                        return false;
                    } else {
                        str3 = ", Type ";
                        actionStr2 = "prevent start ";
                        callerPkg2 = callerPkg;
                        calleePkg3 = calleePkg7;
                        calleePkg4 = " callingUid ";
                    }
                }
                if (processRecord != null) {
                    List<String> pkgList = getAppPkgList(callerApp);
                    if (pkgList.size() > 0) {
                        callerPkg2 = pkgList.get(0);
                    }
                }
                if (!"bsfj".equals(type) || "ssfa".equals(type)) {
                    str5 = "ssfa";
                    str6 = str3;
                    str4 = str2;
                    colorAppStartupManager2 = this;
                    callerPkg3 = callerPkg2;
                } else if (!"sscn".equals(type)) {
                    colorAppStartupManager2 = this;
                    if (colorAppStartupManager2.isSameApplication(processRecord, callingUid, s.appInfo)) {
                        str5 = "ssfa";
                        str6 = str3;
                        str4 = str2;
                        callerPkg3 = callerPkg2;
                    } else {
                        str5 = "ssfa";
                        str6 = str3;
                        str4 = str2;
                        callerPkg3 = callerPkg2;
                        updateLaunchRecord(callerPkg2, calleePkg3, "1", "1", type);
                        if (colorAppStartupManager2.mSwitchMonitor && !colorAppStartupManager2.mDebugSwitch) {
                            return false;
                        }
                        if (str5.equals(type)) {
                            if (callerPkg3 == null) {
                                callerPkg3 = "system[alarmManger]";
                            } else {
                                callerPkg3 = colorAppStartupManager2.composePackage("system[alarmManger]", callerPkg3);
                            }
                        }
                        cpn = intent.getComponent();
                        if (cpn != null) {
                            if (colorAppStartupManager2.mSwitchMonitor) {
                                colorAppStartupManager2.mHandler.post(new CollectAppInterceptRunnable(callerPkg3, calleePkg3, cpn.getClassName(), type));
                            }
                            Log.i(str4, actionStr2 + calleePkg3 + ", cmp " + cpn.toShortString() + " by " + callerPkg3 + calleePkg4 + callingUid + str6 + type);
                            return false;
                        }
                        String action = intent.getAction();
                        if (action == null) {
                            return false;
                        }
                        if (this.mSwitchMonitor) {
                            if (type.equals("ss")) {
                                str8 = str6;
                                str7 = str4;
                                colorAppStartupManager3 = this;
                                this.mHandler.post(new CollectAppInterceptRunnable(callerPkg3, calleePkg3, action, "ssa"));
                            } else {
                                str8 = str6;
                                str7 = str4;
                                colorAppStartupManager3 = this;
                            }
                            if (type.equals("bs")) {
                                colorAppStartupManager3.mHandler.post(new CollectAppInterceptRunnable(callerPkg3, calleePkg3, action, "bsa"));
                            }
                        } else {
                            str8 = str6;
                            str7 = str4;
                        }
                        Log.i(str7, actionStr2 + calleePkg3 + "s.name " + s.name + ", Intent { act=" + action + " } by " + callerPkg3 + calleePkg4 + callingUid + str8 + type);
                        return false;
                    }
                } else {
                    str5 = "ssfa";
                    str6 = str3;
                    str4 = str2;
                    colorAppStartupManager2 = this;
                    callerPkg3 = callerPkg2;
                }
                updateLaunchRecord(RECORD_CALLER_ANDROID, calleePkg3, "0", "1", type);
                if (colorAppStartupManager2.mSwitchMonitor) {
                }
                if (str5.equals(type)) {
                }
                cpn = intent.getComponent();
                if (cpn != null) {
                }
            }
            if (!"bsfj".equals(type)) {
                if (inAssociateStartWhiteList(calleePkg7, s.userId)) {
                    str10 = ", intent=";
                    colorAppStartupManager4 = this;
                    calleePkg6 = calleePkg7;
                    calleePkg5 = str2;
                    str11 = str9;
                } else if (inProtectWhiteList(calleePkg7)) {
                    str10 = ", intent=";
                    colorAppStartupManager4 = this;
                    calleePkg6 = calleePkg7;
                    calleePkg5 = str2;
                    str11 = str9;
                }
                updateLaunchRecord(RECORD_CALLER_ANDROID, calleePkg6, RECORD_ASSOCIATE_LAUNCH_ALLOW_MODE, "0", type);
                if (!colorAppStartupManager4.mDebugSwitch) {
                    return true;
                }
                Log.i(calleePkg5, "start " + calleePkg6 + str11 + callingUid + str10 + intent);
                return true;
            } else if (inProtectWhiteList(calleePkg7) || OppoListManager.getInstance().isInAutoBootWhiteList(calleePkg7, s.userId)) {
                updateLaunchRecord(RECORD_CALLER_ANDROID, calleePkg7, RECORD_AUTO_LAUNCH_ALLOW_MODE, "0", type);
                if (!this.mDebugSwitch) {
                    return true;
                }
                Log.i(str2, "start " + calleePkg7 + str9 + callingUid + ", intent=" + intent);
                return true;
            }
            actionStr = intent.getAction();
            if (actionStr != null) {
            }
            processRecord = callerApp;
            str3 = ", Type ";
            calleePkg3 = calleePkg7;
            callerPkg2 = callerPkg;
            actionStr2 = "prevent start ";
            calleePkg4 = " callingUid ";
            if (processRecord != null) {
            }
            if (!"bsfj".equals(type)) {
            }
            str5 = "ssfa";
            str6 = str3;
            str4 = str2;
            colorAppStartupManager2 = this;
            callerPkg3 = callerPkg2;
            updateLaunchRecord(RECORD_CALLER_ANDROID, calleePkg3, "0", "1", type);
            if (colorAppStartupManager2.mSwitchMonitor) {
            }
            if (str5.equals(type)) {
            }
            cpn = intent.getComponent();
            if (cpn != null) {
            }
        }
        if (!colorAppStartupManager.mDebugSwitch) {
            return true;
        }
        Log.i(calleePkg, "start " + calleePkg2 + " by service callingUid " + callingUid + str + intent2);
        return true;
    }

    /* access modifiers changed from: protected */
    public void collectAppStartBySystemUI(ProcessRecord callerApp, ServiceRecord s) {
        if (getAppPkgList(callerApp).contains("com.android.systemui") && (s.appInfo.flags & 1) == 0 && s.name != null) {
            if (this.mSwitchMonitor) {
                this.mHandler.post(new CollectAppInterceptRunnable("com.android.systemui", s.appInfo.packageName, s.name.getClassName(), "bs_systemui"));
            }
            Log.i(ColorAppStartupMonitorInfo.TAG, "start " + s.appInfo.packageName + ", cpn " + s.name + " by systemui");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isAllowStartFromBroadCast(ProcessRecord callerApp, String callerPkg, int callingUid, Intent intent, ResolveInfo info) {
        String str;
        String str2;
        Intent intent2;
        String str3;
        String str4;
        String str5;
        String callerPkg2;
        String callerPkg3 = callerPkg;
        if ((isExpVersion() && !isExGlobalBlackList(info.activityInfo.applicationInfo.packageName)) || isRootOrShell(callingUid)) {
            return true;
        }
        String packageName = info.activityInfo.applicationInfo.packageName;
        int uid = info.activityInfo.applicationInfo.uid;
        if (isDefaultAllowStart(info.activityInfo.applicationInfo)) {
            intent2 = intent;
            str2 = ColorAppStartupMonitorInfo.TAG;
            str = "start ";
        } else if (isInLruProcessesLocked(uid)) {
            intent2 = intent;
            str2 = ColorAppStartupMonitorInfo.TAG;
            str = "start ";
        } else {
            if (OppoListManager.getInstance().isInAutoBootWhiteList(packageName, UserHandle.getUserId(uid))) {
                str3 = ColorAppStartupMonitorInfo.TAG;
            } else if (inProtectWhiteList(packageName)) {
                str3 = ColorAppStartupMonitorInfo.TAG;
            } else if (inBroadCastWhiteList(packageName)) {
                str3 = ColorAppStartupMonitorInfo.TAG;
            } else if (inBroadCastActionWhiteList(intent)) {
                str3 = ColorAppStartupMonitorInfo.TAG;
            } else {
                updateLaunchRecord(RECORD_CALLER_ANDROID, packageName, "0", "1", "broadcast");
                if (!this.mSwitchMonitor && !this.mDebugSwitch) {
                    return false;
                }
                if (callerApp != null) {
                    List<String> pkgList = getAppPkgList(callerApp);
                    if (pkgList.size() > 0) {
                        callerPkg2 = pkgList.get(0);
                    } else {
                        callerPkg2 = callerPkg3;
                    }
                    callerPkg3 = callerPkg2;
                } else if (callerApp == null && "system[alarmManger]".equals(callerPkg3)) {
                    String callerPkg4 = getPackageNameForUid(callingUid);
                    callerPkg3 = callerPkg4 == null ? "system[alarmManger]" : callerPkg4;
                } else if (callerApp == null && callerPkg3 == null) {
                    String callerPkg5 = getPackageNameForUid(callingUid);
                    callerPkg3 = callerPkg5 == null ? "unknow" : composePackage("unknow", callerPkg5);
                }
                ComponentName cpn = intent.getComponent();
                if (cpn != null) {
                    if (this.mSwitchMonitor) {
                        str4 = "prevent start ";
                        str5 = " callingUid ";
                        this.mHandler.post(new CollectAppInterceptRunnable(callerPkg3, packageName, cpn.getClassName(), "broadcast"));
                    } else {
                        str5 = " callingUid ";
                        str4 = "prevent start ";
                    }
                    Log.i(ColorAppStartupMonitorInfo.TAG, str4 + packageName + ", cmp " + cpn.toShortString() + " by broadcast " + callerPkg3 + str5 + callingUid);
                    return false;
                }
                String action = intent.getAction();
                if (action == null) {
                    return false;
                }
                if (this.mSwitchMonitor) {
                    this.mHandler.post(new CollectAppInterceptRunnable(callerPkg3, packageName, action, "broadcast_action"));
                }
                Log.i(ColorAppStartupMonitorInfo.TAG, "prevent start " + packageName + ", Intent { act=" + action + " } by broadcast " + callerPkg3 + " callingUid " + callingUid);
                return false;
            }
            updateLaunchRecord(RECORD_CALLER_ANDROID, packageName, RECORD_AUTO_LAUNCH_ALLOW_MODE, "0", "broadcast");
            if (this.mDebugSwitch) {
                Log.d(str3, "start " + packageName + " by broadcast  callingUid " + callingUid + ", Intent=" + intent);
            }
            return true;
        }
        if (this.mDebugSwitch) {
            Log.d(str2, str + packageName + " by broadcast  callingUid " + callingUid + ", Intent=" + intent2);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isAllowStartFromBroadCast(int callingUid, int callingPid, Intent intent, ResolveInfo info) {
        if ((isExpVersion() && !isExGlobalBlackList(info.activityInfo.applicationInfo.packageName)) || isRootOrShell(callingUid)) {
            return true;
        }
        boolean result = true;
        if (callingPid == -1) {
            if (hasExtra(intent, "android.intent.extra.ALARM_COUNT")) {
                result = isAllowStartFromBroadCast(null, "system[alarmManger]", callingUid, intent, info);
            } else if (hasExtra(intent, "location")) {
                result = isAllowStartFromBroadCast(null, "system[location]", callingUid, intent, info);
            }
        } else if (!OppoListManager.getInstance().isInAutoBootWhiteList(info.activityInfo.applicationInfo.packageName, UserHandle.getUserId(info.activityInfo.applicationInfo.uid)) && !isDefaultAllowStart(info.activityInfo.applicationInfo)) {
            if (!isSameApplication(null, callingUid, info.activityInfo.applicationInfo)) {
                result = false;
                updateLaunchRecord(RECORD_CALLER_ANDROID, info.activityInfo.applicationInfo.packageName, "0", "1", "broadcast");
            } else if (!isInLruProcessesLocked(info.activityInfo.applicationInfo.uid)) {
                result = false;
                updateLaunchRecord(RECORD_CALLER_ANDROID, info.activityInfo.applicationInfo.packageName, "0", "1", "broadcast");
            }
        }
        if (this.mDebugSwitch) {
            Log.i(ColorAppStartupMonitorInfo.TAG, "Broadcast caller pid " + callingPid + " uid " + callingUid + " intent=" + intent + ",applicationInfo=" + info.activityInfo.applicationInfo.packageName + " " + result);
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public boolean isAllowStartFromProvider(ProcessRecord callerApp, ContentProviderRecord cpr, ApplicationInfo appInfo) {
        int callingUid;
        String str;
        String str2;
        String callerPkg;
        String str3;
        ComponentName cpn;
        if (cpr == null) {
            return true;
        }
        if (appInfo == null) {
            return true;
        }
        if (callerApp != null) {
            callingUid = callerApp.uid;
        } else {
            callingUid = -1;
        }
        if (isRootOrShell(callingUid)) {
            return true;
        }
        String packageName = appInfo.packageName;
        if (isExpVersion() && !isExGlobalBlackList(packageName)) {
            return true;
        }
        if (isDefaultAllowStart(appInfo)) {
            str = ColorAppStartupMonitorInfo.TAG;
        } else if (isInLruProcessesLocked(appInfo.uid)) {
            str = ColorAppStartupMonitorInfo.TAG;
        } else {
            if (inAssociateStartWhiteList(packageName, UserHandle.getUserId(appInfo.uid))) {
                str2 = ColorAppStartupMonitorInfo.TAG;
            } else if (inProtectWhiteList(packageName)) {
                str2 = ColorAppStartupMonitorInfo.TAG;
            } else if (cpr.name == null || !inProviderCpnWhiteList(cpr.name.getClassName())) {
                if (callerApp != null) {
                    List<String> pkgList = getAppPkgList(callerApp);
                    if (pkgList.size() == 0) {
                        return true;
                    }
                    String callerPkg2 = pkgList.get(0);
                    if (callerPkg2 == null || !inProviderCallerPkgList(callerPkg2)) {
                        str3 = ColorAppStartupMonitorInfo.TAG;
                        if (isSameApplication(callerApp, callingUid, appInfo)) {
                            updateLaunchRecord(RECORD_CALLER_ANDROID, packageName, "0", "1", "provider");
                        } else {
                            updateLaunchRecord(callerPkg2, packageName, "1", "1", "provider");
                        }
                        callerPkg = callerPkg2;
                    } else {
                        updateLaunchRecord(RECORD_CALLER_ANDROID, packageName, RECORD_ASSOCIATE_LAUNCH_ALLOW_MODE, "0", "provider");
                        if (!this.mDebugSwitch) {
                            return true;
                        }
                        Log.i(ColorAppStartupMonitorInfo.TAG, "start " + packageName + " by provider  callingUid " + callingUid + ", cpr=" + cpr.toShortString() + ", inProviderCallerPkgList");
                        return true;
                    }
                } else {
                    str3 = ColorAppStartupMonitorInfo.TAG;
                    callerPkg = "unknow";
                }
                if ((!this.mSwitchMonitor && !this.mDebugSwitch) || (cpn = cpr.name) == null) {
                    return false;
                }
                if (this.mSwitchMonitor) {
                    this.mHandler.post(new CollectAppInterceptRunnable(callerPkg, packageName, cpn.getClassName(), "provider"));
                }
                Log.i(str3, "prevent start " + packageName + ", cmp " + cpn.toShortString() + " by contentprovider " + callerPkg + " callingUid " + callingUid);
                return false;
            } else {
                str2 = ColorAppStartupMonitorInfo.TAG;
            }
            updateLaunchRecord(RECORD_CALLER_ANDROID, packageName, RECORD_ASSOCIATE_LAUNCH_ALLOW_MODE, "0", "provider");
            if (!this.mDebugSwitch) {
                return true;
            }
            Log.i(str2, "start " + packageName + " by provider  callingUid " + callingUid + ", cpr=" + cpr.toShortString());
            return true;
        }
        if (!this.mDebugSwitch) {
            return true;
        }
        Log.i(str, "start " + packageName + " by provider  callingUid " + callingUid + ", cpr=" + cpr.toShortString());
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x0094  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00f8  */
    private boolean isAllowStartFromNotificationService(ComponentName component, String rebindType, int userId) {
        boolean result;
        String packageName = component.getPackageName();
        if (packageName == null || rebindType == null) {
            return true;
        }
        if (isExpVersion() && !isExGlobalBlackList(packageName)) {
            return true;
        }
        if (inNotificationWhiteList(packageName) || inProtectWhiteList(packageName)) {
            if (this.mDebugSwitch) {
                Log.i(ColorAppStartupMonitorInfo.TAG, "start " + packageName + " by notification  " + rebindType + " ,cpn=" + component.toShortString());
            }
            return true;
        }
        boolean result2 = false;
        if (rebindType.equals("otherAllow")) {
            result2 = true;
        }
        if (!result2) {
            ApplicationInfo info = getApplicationInfo(packageName, userId);
            if (info != null && isInLruProcessesLocked(info.uid)) {
                result = true;
                if (this.mDebugSwitch) {
                }
                if (!result) {
                }
                return result;
            } else if (rebindType.equals("forceStop") || rebindType.equals("settingChange") || rebindType.equals(REBIND_BIND_DIED)) {
                if (OppoListManager.getInstance().isInAutoBootWhiteList(packageName, userId)) {
                    result = true;
                } else if (info != null && isDefaultAllowStart(info)) {
                    result = true;
                }
                if (this.mDebugSwitch) {
                    if (result) {
                        Log.i(ColorAppStartupMonitorInfo.TAG, "start " + packageName + " by notification  " + rebindType + " ,cpn=" + component.toShortString());
                    } else {
                        Log.i(ColorAppStartupMonitorInfo.TAG, "prevent start " + packageName + " by notification  " + rebindType + " ,cpn=" + component.toShortString());
                        if (this.mSwitchMonitor) {
                            this.mHandler.post(new CollectAppInterceptRunnable("system[notificationListener]", packageName, component.getClassName(), "bsfn"));
                        }
                    }
                }
                if (!result) {
                    updateLaunchRecord(RECORD_CALLER_ANDROID, packageName, "0", "1", "system[notificationListener]");
                }
                return result;
            } else if (rebindType.equals("userUnlock") && info != null && isDefaultAllowStart(info)) {
                result = true;
                if (this.mDebugSwitch) {
                }
                if (!result) {
                }
                return result;
            }
        }
        result = result2;
        if (this.mDebugSwitch) {
        }
        if (!result) {
        }
        return result;
    }

    private boolean hasExtra(Intent intent, String extra) {
        boolean hasExtra = false;
        boolean isParcelled = true;
        try {
            if (intent.getExtras() != null) {
                isParcelled = intent.getExtras().isParcelled();
            }
            if (!isParcelled) {
                hasExtra = intent.hasExtra(extra);
            }
            if (this.mDebugSwitch) {
                Log.i(ColorAppStartupMonitorInfo.TAG, extra + ", isParcelled=" + isParcelled + ", hasExtra=" + hasExtra);
            }
        } catch (Exception e) {
        }
        return hasExtra;
    }

    private boolean isRootOrShell(int uid) {
        return uid == 0 || uid == 2000;
    }

    private boolean isSameApplication(ProcessRecord callerApp, int callingUid, ApplicationInfo appInfo) {
        boolean result = false;
        if (callingUid == appInfo.uid || !(callerApp == null || callerApp.pkgList == null || !callerApp.pkgList.containsKey(appInfo.packageName))) {
            result = true;
        }
        if (this.mDynamicDebug && result) {
            Log.d(ColorAppStartupMonitorInfo.TAG, callingUid + " sameApplication");
        }
        return result;
    }

    public String composePackage(String pre, String suf) {
        StringBuilder stringBuilder = new StringBuilder(100);
        stringBuilder.append(pre);
        stringBuilder.append("_");
        stringBuilder.append(suf);
        return stringBuilder.toString();
    }

    private boolean isDefaultAllowStart(ApplicationInfo info) {
        if (inBuildBlackList(info.packageName)) {
            return false;
        }
        if (info.uid <= 10000 || (info.flags & 1) != 0 || (info.flags & 128) != 0 || inCustomizeWhiteList(info.packageName) || inGlobalWhiteList(info.packageName) || isCtsRunning()) {
            return true;
        }
        return false;
    }

    private class CollectAppInterceptRunnable implements Runnable {
        private String mCallCpnOrAction;
        private String mCallType;
        private String mCalleePkgName;
        private String mCallerPkgName;

        public CollectAppInterceptRunnable(String callerPkgName, String calledPkgName, String callCpnNameOrAction, String callType) {
            this.mCallerPkgName = callerPkgName;
            this.mCalleePkgName = calledPkgName;
            this.mCallCpnOrAction = callCpnNameOrAction;
            this.mCallType = callType;
        }

        public void run() {
            ColorAppStartupManager.this.monitorAppStartInfo(this.mCallerPkgName, this.mCalleePkgName, this.mCallCpnOrAction, this.mCallType);
        }
    }

    /* access modifiers changed from: protected */
    public void collectAppStartInfo(ProcessRecord app, String hostingNameStr, String hostingType) {
        long time;
        String startMode;
        if (this.mSwitchMonitor) {
            if (this.mDynamicDebug) {
                time = SystemClock.elapsedRealtime();
            } else {
                time = 0;
            }
            if (app.info != null && (app.info.flags & 1) == 0) {
                String packageName = app.info.packageName;
                String processName = app.processName;
                if (packageName != null && processName != null && hostingType != null) {
                    if (OppoListManager.getInstance().isAppStartForbidden(packageName)) {
                        this.mHandler.post(new CollectAppInterceptRunnable("noNeed", packageName, hostingNameStr != null ? hostingNameStr : "", "startProcessLocked"));
                    }
                    if (inCollectAppStartList(packageName) && !isInLruProcessesLocked(app.uid)) {
                        boolean isAssociateSwitch = inAssociateStartWhiteList(packageName, app.userId);
                        boolean isBootStartSwitch = OppoListManager.getInstance().isInAutoBootWhiteList(packageName, app.userId);
                        if (isAssociateSwitch && isBootStartSwitch) {
                            startMode = composePackage("bootstart", "associate");
                        } else if (isAssociateSwitch) {
                            startMode = "associate";
                        } else if (isBootStartSwitch) {
                            startMode = "bootstart";
                        } else if (OppoListManager.getInstance().isInstalledAppWidget(packageName, app.uid) && "broadcast".equals(hostingType)) {
                            startMode = ColorCommonListManager.CONFIG_WIDGET;
                        } else if (!packageName.equals(processName) || !"activity".equals(hostingType)) {
                            startMode = "other";
                        } else {
                            startMode = "click";
                        }
                        if (this.mDynamicDebug) {
                            Log.d(ColorAppStartupMonitorInfo.TAG, "ams_collectAppStartInfo cost time=" + (SystemClock.elapsedRealtime() - time));
                        }
                        if (this.mDynamicDebug) {
                            Log.i(ColorAppStartupMonitorInfo.TAG, Log.getStackTraceString(new Throwable()));
                        }
                        this.mHandler.post(new CollectAppStartRunnable(packageName, processName, hostingType, startMode, hostingNameStr));
                    }
                }
            }
        }
    }

    private class CollectAppStartRunnable implements Runnable {
        String mHostingNameStr;
        String mHostingType;
        String mPackageName;
        String mProcessName;
        String mStartMode;

        public CollectAppStartRunnable(String packageName, String processName, String hostingType, String startMode, String hostingNameStr) {
            this.mPackageName = packageName;
            this.mProcessName = processName;
            this.mHostingType = hostingType;
            this.mStartMode = startMode;
            this.mHostingNameStr = hostingNameStr;
        }

        public void run() {
            ColorAppStartupManager colorAppStartupManager = ColorAppStartupManager.this;
            String str = this.mPackageName;
            String str2 = this.mProcessName;
            String str3 = this.mHostingType;
            String str4 = this.mStartMode;
            String str5 = this.mHostingNameStr;
            if (str5 == null) {
                str5 = "";
            }
            colorAppStartupManager.collectProcessStartInfo(str, str2, str3, str4, str5);
            if (ColorAppStartupManager.this.mCollectProcessInfoList.size() >= ColorAppStartupManagerUtils.getInstance().getCallCheckCount()) {
                ColorAppStartupManager.this.uploadProcessStartInfo();
            }
        }
    }

    /* access modifiers changed from: private */
    public void collectProcessStartInfo(String packageName, String processName, String hostingType, String startMode, String hostingNameStr) {
        ColorAppStartupMonitorInfo processStartInfo = getProcessInfoInList(processName);
        if (processStartInfo == null) {
            this.mCollectProcessInfoList.add(ColorAppStartupMonitorInfo.builderProcessInfo(packageName, processName, hostingType, startMode, hostingNameStr));
            return;
        }
        processStartInfo.increaseProcessStartCount(packageName, processName, hostingType, startMode, hostingNameStr);
    }

    private ColorAppStartupMonitorInfo getProcessInfoInList(String processName) {
        for (ColorAppStartupMonitorInfo appinfo : this.mCollectProcessInfoList) {
            if (appinfo.getProcessName().equals(processName)) {
                return appinfo;
            }
        }
        return null;
    }

    private class CollectBlackListInterceptRunnable implements Runnable {
        String mCalleeCpn;
        String mCalleePkg;
        String mStartMode;

        public CollectBlackListInterceptRunnable(String calleePkg, String calleeCpn, String startMode) {
            this.mCalleePkg = calleePkg;
            this.mCalleeCpn = calleeCpn;
            this.mStartMode = startMode;
        }

        public void run() {
            String str;
            String str2;
            String str3 = this.mCalleePkg;
            if (str3 != null && (str = this.mCalleeCpn) != null && (str2 = this.mStartMode) != null) {
                ColorAppStartupManager.this.collectBlackListInterceptInfo(str3, str, str2);
                if (ColorAppStartupManager.this.mCollectBlackListInterceptList.size() >= ColorAppStartupManagerUtils.getInstance().getCallCheckCount()) {
                    ColorAppStartupManager.this.uploadBlackListIntercept();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void collectBlackListInterceptInfo(String calleePkg, String calleeCpn, String startMode) {
        ColorAppStartupMonitorInfo interceptInfo = getBlackListInterceptInList(calleePkg);
        if (interceptInfo == null) {
            this.mCollectBlackListInterceptList.add(ColorAppStartupMonitorInfo.builder(calleePkg, calleeCpn, startMode));
            return;
        }
        interceptInfo.increaseBlackListInterceptCount(calleePkg, calleeCpn, startMode);
    }

    private ColorAppStartupMonitorInfo getBlackListInterceptInList(String packageName) {
        for (ColorAppStartupMonitorInfo appinfo : this.mCollectBlackListInterceptList) {
            if (appinfo.getCalledPkgName().equals(packageName)) {
                return appinfo;
            }
        }
        return null;
    }

    public void collectWechatInfo(String name, String duration, String activiteTime) {
        this.mHandler.post(new CollectWechatRunnable(name, duration, activiteTime));
    }

    private class CollectWechatRunnable implements Runnable {
        String mActiviteTime;
        String mDuration;
        String mName;

        public CollectWechatRunnable(String name, String duration, String activiteTime) {
            this.mName = name;
            this.mDuration = duration;
            this.mActiviteTime = activiteTime;
        }

        public void run() {
            Map<String, String> staticEventMap = new HashMap<>();
            staticEventMap.put(BrightnessConstants.AppSplineXml.TAG_NAME, this.mName);
            staticEventMap.put("duration", this.mDuration);
            staticEventMap.put("activiteTime", this.mActiviteTime);
            OppoStatistics.onCommon(ColorAppStartupManager.this.mAms.mContext, ColorAppStartupManager.WX_TAG, ColorAppStartupManager.WX_EVENT, staticEventMap, false);
        }
    }

    /* access modifiers changed from: private */
    public void monitorAppStartInfo(String callerPkgName, String calledPkgName, String callCpnName, String callType) {
        if (this.mSwitchMonitor) {
            if (callerPkgName != null && calledPkgName != null && callType != null) {
                String calledAppExist = "false";
                if (!callerPkgName.equals("noNeed") && !callType.equals("broadcast") && !callType.equals("broadcast_action") && checkAppExist(calledPkgName)) {
                    calledAppExist = "true";
                }
                handleAppMonitorInfo(callerPkgName, calledPkgName, callCpnName, callType, calledAppExist);
            } else if (this.mDebugSwitch) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "monitorAppStartInfo callerPkgName == null || calledPkgName == null");
            }
        }
    }

    public void sendMonitorInfoNotify() {
        Intent intent = new Intent(ACTION_OPPO_STARTUP_APP_MONITOR);
        intent.putExtra(UPLOAD_KEY_TYPE, "appcallinfo");
        intent.putStringArrayListExtra("data", new ArrayList<>(this.mMonitorAppUploadList));
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            activityManagerService.mContext.sendBroadcast(intent);
        }
    }

    private void collectAppInterceptInfo(String callerPkg, String calledPkg, String callCpnName, String callType, String calledAppExist) {
        ColorAppStartupMonitorInfo appMonitorInfo = getAppInfoInList(callerPkg);
        if (appMonitorInfo == null) {
            this.mMonitorAppInfoList.add(ColorAppStartupMonitorInfo.builder(callerPkg, calledPkg, callCpnName, callType, calledAppExist));
            return;
        }
        appMonitorInfo.increaseCallCount(calledPkg, callCpnName, callType, calledAppExist);
    }

    public ColorAppStartupMonitorInfo getAppInfoInList(String callerPkg) {
        for (ColorAppStartupMonitorInfo appinfo : this.mMonitorAppInfoList) {
            if (appinfo.getCallerPkgName().equals(callerPkg)) {
                return appinfo;
            }
        }
        return null;
    }

    public void notifyAppInterceptInfo() {
        if (!this.mMonitorAppInfoList.isEmpty()) {
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "--------------------------notifyMonitorCallInfo start---------------------------");
            }
            int length = this.mMonitorAppInfoList.size();
            for (int i = 0; i < length; i++) {
                ColorAppStartupMonitorInfo appInfo = this.mMonitorAppInfoList.get(i);
                if (appInfo != null) {
                    this.mMonitorAppUploadList.addAll(appInfo.formatCallInfo());
                }
                if (this.mDebugSwitch && appInfo != null) {
                    appInfo.dumpInfo();
                }
                if (i == length - 1) {
                    appInfo.clearCallList();
                }
            }
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "--------------------------notifyMonitorCallInfo end-----------------------------");
            }
            sendMonitorInfoNotify();
            this.mMonitorAppInfoList.clear();
            this.mMonitorAppUploadList.clear();
        } else if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "mMonitorAppInfoList is empty!");
        }
    }

    public void handleAppMonitorInfo(String callerPkgName, String calledPkgName, String callCpnName, String callType, String calledAppExist) {
        if (callCpnName == null) {
            callCpnName = "cpnUnknow";
            Log.d(ColorAppStartupMonitorInfo.TAG, "handleAppMonitorInfo cpnName is null.");
        }
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "handleAppMonitorInfo callerPkgName = " + callerPkgName + "  calledPkgName = " + calledPkgName + "  callType = " + callType + "  cpnName = " + callCpnName + "  calledAppExist = " + calledAppExist);
        }
        collectAppInterceptInfo(callerPkgName, calledPkgName, callCpnName, callType, calledAppExist);
        if (this.mMonitorAppInfoList.size() >= ColorAppStartupManagerUtils.getInstance().getCallCheckCount()) {
            notifyAppInterceptInfo();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004a, code lost:
        r0 = new com.android.server.am.ColorAppStartupStatistics.OppoCallActivityEntry(r4, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0056, code lost:
        if (r3.mLastCalledAcivityList.size() < 5) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0058, code lost:
        r3.mLastCalledAcivityList.remove(0);
        r3.mLastCalledAcivityList.add(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0064, code lost:
        r3.mLastCalledAcivityList.add(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        return;
     */
    public void collectLastCallActivityInfo(String pkgName, String cpnName) {
        if (this.mDebugSwitch) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "collectLastCallActivityInfo pkgName = " + pkgName);
            Log.d(ColorAppStartupMonitorInfo.TAG, "collectLastCallActivityInfo cpnName = " + cpnName);
        }
        if (pkgName == null || cpnName == null) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "collectLastCallActivityInfo pkgName or cpnName is null!");
            return;
        }
        synchronized (this.mAuthorizeCpnListLock) {
            if (this.mAuthorizeCpnList.contains(cpnName)) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "collectLastCallActivityInfo cpnName must be filter!");
            }
        }
    }

    public List<String> getAppPkgList(ProcessRecord app) {
        ArrayList<String> pkgList = new ArrayList<>();
        String[] list = app.getPackageList();
        if (list == null) {
            return pkgList;
        }
        for (String str : list) {
            pkgList.add(str);
        }
        return pkgList;
    }

    public ComponentName getTopComponentName() {
        ComponentName topCpn = null;
        if (this.mAms != null) {
            topCpn = ColorAppStartupManagerHelper.getInstance().getTopActivityComponent(this.mAms);
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "getTopPkgName topCpn = " + topCpn);
        }
        return topCpn;
    }

    private boolean isScreenOn() {
        try {
            return IPowerManager.Stub.asInterface(ServiceManager.getService("power")).isInteractive();
        } catch (RemoteException e) {
            Log.e(ColorAppStartupMonitorInfo.TAG, "Error getting screen status", e);
            return false;
        }
    }

    public boolean getDynamicDebug() {
        return this.mDynamicDebug;
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.mDebugSwitch = DEBUG_DETAIL | this.mDynamicDebug;
        ColorAppStartupManagerUtils.getInstance().setDynamicDebugSwitch(this.mDynamicDebug);
        OppoFeatureCache.get(IColorAppStoreTraffic.DEFAULT).setDynamicDebugSwitch(on);
    }

    public void openLog(boolean on) {
        Log.i(ColorAppStartupMonitorInfo.TAG, "#####openlog####");
        Log.i(ColorAppStartupMonitorInfo.TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Log.i(ColorAppStartupMonitorInfo.TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Log.i(ColorAppStartupMonitorInfo.TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Log.i(ColorAppStartupMonitorInfo.TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Log.i(ColorAppStartupMonitorInfo.TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorAppStartupManager.class.getName());
            Log.i(ColorAppStartupMonitorInfo.TAG, "invoke end!");
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

    public boolean inAssociateStartWhiteList(String packageName, int userId) {
        boolean result;
        if (userId == 999) {
            userId = 0;
        }
        synchronized (this.mAssociateStartWhiteLock) {
            List<String> list = this.mAssociateStartWhiteListContainer.get(userId);
            if (list == null) {
                result = ColorAppStartupListManager.getInstance().isInAssociateWhiteList(packageName);
            } else {
                result = list.contains(packageName);
            }
        }
        if (result && this.mDebugSwitch) {
            Log.i(ColorAppStartupMonitorInfo.TAG, packageName + " in AssociateStartWhiteList");
        }
        return result;
    }

    private ApplicationInfo getApplicationInfo(String packageName, int userId) {
        try {
            if (this.mAms != null) {
                return this.mAms.mContext.getPackageManager().getApplicationInfo(packageName, userId);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getPackageNameForUid(int uid) {
        try {
            if (this.mAms != null) {
                return this.mAms.mContext.getPackageManager().getNameForUid(uid);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isCtsRunning() {
        boolean isCtsRunning = false;
        try {
            isCtsRunning = OppoFeatureCache.get(IColorFullmodeManager.DEFAULT).isClosedSuperFirewall();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mDynamicDebug) {
            Log.v(ColorAppStartupMonitorInfo.TAG, "isCtsRunning:" + isCtsRunning);
        }
        return isCtsRunning;
    }

    private boolean isExpVersion() {
        return isExpVersion(true);
    }

    private boolean isExpVersion(boolean expConfig) {
        boolean result = false;
        try {
            if (!(this.mAms == null || this.mAms.mContext == null)) {
                result = this.mAms.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (expConfig) {
            return this.isExpVersion & result;
        }
        return result;
    }

    /* access modifiers changed from: private */
    public void uploadProcessStartInfo() {
        if (!this.mCollectProcessInfoList.isEmpty()) {
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "--------------------------uploadProcessStartInfo start---------------------------");
            }
            List<Map<String, String>> uploadList = new ArrayList<>();
            int length = this.mCollectProcessInfoList.size();
            for (int i = 0; i < length; i++) {
                ColorAppStartupMonitorInfo appInfo = this.mCollectProcessInfoList.get(i);
                if (appInfo != null) {
                    uploadList.addAll(appInfo.getProcessStartMap());
                }
                if (this.mDebugSwitch && appInfo != null) {
                    appInfo.dumpProcessStartInfo();
                }
                if (i == length - 1) {
                    appInfo.clearProcessStartList();
                }
            }
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "--------------------------uploadProcessStartInfo end-----------------------------");
            }
            ActivityManagerService activityManagerService = this.mAms;
            if (activityManagerService != null) {
                OppoStatistics.onCommon(activityManagerService.mContext, "20089", "startup_third_app", uploadList, false);
            }
            this.mCollectProcessInfoList.clear();
        }
    }

    /* access modifiers changed from: private */
    public void uploadBlackListIntercept() {
        if (!this.mCollectBlackListInterceptList.isEmpty()) {
            if (this.mDebugSwitch) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "--------------------------uploadBlackListIntercept start---------------------------");
            }
            List<Map<String, String>> uploadList = new ArrayList<>();
            int length = this.mCollectBlackListInterceptList.size();
            for (int i = 0; i < length; i++) {
                ColorAppStartupMonitorInfo appInfo = this.mCollectBlackListInterceptList.get(i);
                if (appInfo != null) {
                    uploadList.addAll(appInfo.getBlackListInterceptMap());
                }
                if (this.mDebugSwitch && appInfo != null) {
                    appInfo.dumpBlackListIntercep();
                }
                if (i == length - 1) {
                    appInfo.clearBlackListInterceptList();
                }
            }
            if (this.mDebugSwitch) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "--------------------------uploadBlackListIntercept end-----------------------------");
            }
            ActivityManagerService activityManagerService = this.mAms;
            if (activityManagerService != null) {
                OppoStatistics.onCommon(activityManagerService.mContext, "20089", "intercept_black", uploadList, false);
            }
            this.mCollectBlackListInterceptList.clear();
        }
    }

    private class CollectGamePayRunnable implements Runnable {
        private String mCalledCpn;
        private String mCalledPkgName;
        private String mCallerCpn;
        private String mCallerPkgName;
        private Intent mIntent;

        public CollectGamePayRunnable(String callerPkg, String callerCpn, String calledPkg, String calledCpn, Intent intent) {
            this.mCallerPkgName = callerPkg;
            this.mCallerCpn = callerCpn;
            this.mCalledPkgName = calledPkg;
            this.mCalledCpn = calledCpn;
            this.mIntent = intent;
        }

        public void run() {
            if (this.mCallerPkgName != null && this.mCallerCpn != null && this.mCalledPkgName != null && this.mCalledCpn != null) {
                if (ColorAppStartupManager.this.mDynamicDebug) {
                    Log.i(ColorAppStartupMonitorInfo.TAG, "gamePay mCallerPkgName=" + this.mCallerPkgName + " mCallerCpn=" + this.mCallerCpn + " mCalledPkgName=" + this.mCalledPkgName + " mCalledCpn=" + this.mCalledCpn);
                }
                if (!ColorAppStartupManager.this.isInGameMonitorPackageList(this.mCallerPkgName)) {
                    return;
                }
                if (ColorAppStartupManager.this.isInGamePayMonitorCpnList(this.mCalledCpn) || ColorAppStartupManager.this.isInGamePayMonitorFuzzyCpnList(this.mCalledPkgName, this.mCalledCpn)) {
                    if (ColorAppStartupManager.this.mDynamicDebug) {
                        Log.i(ColorAppStartupMonitorInfo.TAG, "gamePayMonitor mCallerPkgName =" + this.mCallerPkgName + ", mCalledPkgName= " + this.mCalledPkgName);
                    }
                    synchronized (ColorAppStartupManager.this.mCollectGamePayMonitorList) {
                        ColorAppStartupManager.this.collectProcessInfo(ColorAppStartupManager.this.mCollectGamePayMonitorList, this.mCallerPkgName, this.mCallerCpn, this.mCalledPkgName, this.mCalledCpn);
                        if (ColorAppStartupManager.this.mCollectGamePayMonitorList.size() >= ColorAppStartupManagerUtils.getInstance().getGamePayMonitorCheckCount()) {
                            ColorAppStartupManager.this.uploadProcessInfo(ColorAppStartupManager.this.mCollectGamePayMonitorList, "game_pay_monitor");
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0079, code lost:
        r0 = r0;
     */
    private void handleProcessStartupInfo(int callingPid, int callingUid, ProcessRecord callerApp, Intent intent, ApplicationInfo appInfo, String hostType) {
        long time;
        ProcessRecord callerApp2;
        int callingUid2;
        int callingPid2;
        if (this.mSwitchMonitor) {
            if (this.mDynamicDebug) {
                time = SystemClock.elapsedRealtime();
            } else {
                time = 0;
            }
            if (appInfo == null || !inCollectAppStartList(appInfo.packageName)) {
                return;
            }
            if (!isInLruProcessesLocked(appInfo.uid)) {
                if (this.mDynamicDebug) {
                    Log.d(ColorAppStartupMonitorInfo.TAG, "handleProcessStartupInfo2 cost time = " + (SystemClock.elapsedRealtime() - time));
                }
                if (callerApp == null) {
                    synchronized (this.mAms.mPidsSelfLocked) {
                        try {
                            ProcessRecord callerApp3 = this.mAms.mPidsSelfLocked.get(callingPid);
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } else {
                    callerApp2 = callerApp;
                }
                if (callingPid == 0 && callingUid == 0 && callerApp2 != null) {
                    callingPid2 = callerApp2.pid;
                    callingUid2 = callerApp2.uid;
                } else {
                    callingUid2 = callingUid;
                    callingPid2 = callingPid;
                }
                ColorAppStartupStatistics.getInstance().collectAppStartInfo(callingPid2, callingUid2, callerApp2, intent, appInfo, hostType);
            } else if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "handleProcessStartupInfo1 cost time = " + (SystemClock.elapsedRealtime() - time));
            }
        }
    }

    public boolean isRemovePendingJob(String packageName, int userId) {
        ApplicationInfo appInfo;
        boolean result = true;
        if (!(packageName == null || (appInfo = getApplicationInfo(packageName, userId)) == null || (!isDefaultAllowStart(appInfo) && !OppoListManager.getInstance().isInAutoBootWhiteList(packageName, userId) && !isInLruProcessesLocked(appInfo.uid)))) {
            result = false;
        }
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "remove pendingJob  " + packageName + " result " + result);
        }
        return result;
    }

    public void handleAppStartForbidden(String packageName) {
        ColorAppStartupManagerUtils.getInstance().handleAppStartForbidden(packageName);
    }

    public String getDialogTitleText() {
        return ColorAppStartupManagerUtils.getInstance().getDialogTitleText();
    }

    public String getDialogContentText() {
        return ColorAppStartupManagerUtils.getInstance().getDialogContentText();
    }

    public String getDialogButtonText() {
        return ColorAppStartupManagerUtils.getInstance().getDialogButtonText();
    }

    public void resetDialogShowText() {
        ColorAppStartupManagerUtils.getInstance().resetDialogShowText();
    }

    public boolean isFromControlCenterPkg(String packageName) {
        boolean result = OppoListManager.getInstance().isFromControlCenterPkg(packageName);
        if (this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, packageName + " isFromControlCenterPkg result " + result);
        }
        return result;
    }

    private void updateLaunchRecord(String callerPkg, String calledPkg, String launchMode, String launchType, String reason) {
        ColorAutostartManager.getInstance().updateLaunchRecord(callerPkg, calledPkg, launchMode, launchType, reason);
    }

    public void handleApplicationCrash(ProcessRecord app, ApplicationErrorReport.CrashInfo crashInfo) {
        String prePkg = OppoFeatureCache.get(IColorAppSwitchManager.DEFAULT).getPrePkgName();
        String nextPkg = OppoFeatureCache.get(IColorAppSwitchManager.DEFAULT).getNextPkgName();
        if (app != null && app.info != null && app.info.packageName != null && crashInfo != null) {
            if ((prePkg != null && prePkg.equals(app.info.packageName)) || (nextPkg != null && nextPkg.equals(app.info.packageName))) {
                this.mHandler.post(new AppAbnormalMonitor(app.info.packageName, crashInfo.exceptionClassName, crashInfo.exceptionMessage, crashInfo.stackTrace, CALL_ARG_CRASH));
                if (this.mDynamicDebug) {
                    Log.d(ColorAppStartupMonitorInfo.TAG, "handleApplicationCrash clasName=" + crashInfo.exceptionClassName + " msg=" + crashInfo.exceptionMessage);
                }
            }
        }
    }

    private class AppAbnormalMonitor implements Runnable {
        private String mExceptionClass;
        private String mExceptionMsg;
        private String mExceptionTrace;
        private String mPkg;
        private String mType;

        public AppAbnormalMonitor(String pkg, String exceptionClass, String exceptionMSg, String exceptionTrace, String type) {
            this.mPkg = pkg;
            this.mExceptionClass = exceptionClass;
            this.mExceptionMsg = exceptionMSg;
            this.mExceptionTrace = exceptionTrace;
            this.mType = type;
        }

        public void run() {
            ColorAppStartupManager.this.notifyAppStartMonitorInfo(this.mPkg, this.mExceptionClass, this.mExceptionMsg, this.mExceptionTrace, this.mType);
        }
    }

    public void setAppStartMonitorController(IOppoAppStartController controller) {
        this.mController = controller;
    }

    public void notifyAppStartMonitorInfo(String packageName, String exceptionClass, String exceptionMsg, String exceptionTrace, String type) {
        IOppoAppStartController iOppoAppStartController = this.mController;
        if (iOppoAppStartController != null) {
            try {
                iOppoAppStartController.appStartMonitor(packageName, exceptionClass, exceptionMsg, exceptionTrace, type);
            } catch (Exception e) {
                this.mController = null;
                Log.e(ColorAppStartupMonitorInfo.TAG, "notifyAppStartMonitorInfo failed!!");
            }
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    public Intent handleInterceptActivity(ActivityInfo aInfo, String callingPackage, int callingUid, int userId, Intent intent, String resolvedType, IColorActivityRecordEx resultRecord) {
        IIntentSender target = this.mAms.mPendingIntentController.getIntentSender(2, callingPackage, callingUid, userId, (IBinder) null, (String) null, 0, new Intent[]{intent}, new String[]{resolvedType}, 1342177280, (Bundle) null);
        int flags = intent.getFlags();
        Intent newIntent = new Intent(COLOROS_ACTION_INTERCEPT_REVIEW);
        newIntent.setFlags(8388608 | flags);
        newIntent.putExtra(COLOROS_EXTRA_TARGET_NAME, aInfo.packageName);
        newIntent.putExtra(COLOROS_EXTRA_SOURCE_NAME, callingPackage);
        newIntent.putExtra(COLOROS_EXTRA_INTENT_SENDER, new IntentSender(target));
        newIntent.putExtra(COLOROS_EXTRA_INTENT, intent);
        if (resultRecord != null) {
            newIntent.putExtra(COLOR_EXTRA_RESULT_NEEDED, true);
        }
        return newIntent;
    }

    public void handleMonitorRestrictedBackgroundWhitelist(String callerPkg, List<String> targetPkgList) {
        if (this.mSwitchMonitor) {
            ColorAppStartupStatistics.getInstance().collectRestrictedBackgroundWhitelist(callerPkg, targetPkgList);
        }
    }

    private void updateCpnUpperLimitConfig() {
        this.mCpnUpperLimitSwitch = this.mSwitchMonitor | ColorAppStartupManagerUtils.getInstance().getCpnUpperLimitSwitch();
        this.mCpnUpperLimitThresholdConnected = ColorAppStartupManagerUtils.getInstance().getCpnUpperLimitThresholdConnected();
        this.mCpnUpperLimitThresholdTime = ColorAppStartupManagerUtils.getInstance().getCpnUpperLimitThresholdTime();
        this.mCpnUpperLimitCountInterval = ColorAppStartupManagerUtils.getInstance().getCpnUpperLimitCountInterval();
        this.mCpnUpperLimitCountStep = ColorAppStartupManagerUtils.getInstance().getCpnUpperLimitCountStep();
    }

    /* access modifiers changed from: private */
    public void uploadCpnUpperLimit() {
        ActivityManagerService activityManagerService;
        if (!isExpVersion() && this.mCpnUpperLimitSwitch && this.mCpnUpperLimitMap.size() > 0 && (activityManagerService = this.mAms) != null) {
            OppoStatistics.onCommon(activityManagerService.mContext, "20089", UPLOAD_CPN_UPPTER_LIMIT_EVENTID, this.mCpnUpperLimitMap, false);
            this.mCpnUpperLimitMap.clear();
            if (this.mDynamicDebug) {
                Log.i(ColorAppStartupMonitorInfo.TAG, "cpn upper limit upload success.");
            }
        }
    }

    public void uploadServiceConnected(ProcessRecord pr) {
        int count;
        if (!isExpVersion() && this.mCpnUpperLimitSwitch && pr != null && (count = pr.connections.size()) >= this.mCpnUpperLimitThresholdConnected) {
            if (this.mPrCountMap.get(pr) == null) {
                this.mPrCountMap.put(pr, Integer.valueOf(count));
                this.mHandler.post(new CpnUpperLimitRunnable(pr, 0, UPLOAD_TYPE_SERVICE_CONNECTED));
            } else if (count >= this.mCpnUpperLimitCountStep + this.mPrCountMap.get(pr).intValue()) {
                this.mPrCountMap.put(pr, Integer.valueOf(count));
                this.mHandler.post(new CpnUpperLimitRunnable(pr, 0, UPLOAD_TYPE_SERVICE_CONNECTED));
            }
            if (this.mPrCountMap.size() > PR_COUNT_SIZE) {
                this.mPrCountMap.clear();
            }
        }
    }

    public void uploadRemoveServiceConnection(ProcessRecord pr, long time) {
        if (!isExpVersion() && this.mCpnUpperLimitSwitch && pr != null && time >= this.mCpnUpperLimitThresholdTime) {
            this.mHandler.post(new CpnUpperLimitRunnable(pr, time, UPLOAD_TYPE_REMOVE_SERVICE_CONNECTION));
        }
    }

    private class CpnUpperLimitRunnable implements Runnable {
        private ProcessRecord pr;
        private long time;
        private String type;

        public CpnUpperLimitRunnable(ProcessRecord pr2, long time2, String type2) {
            this.pr = pr2;
            this.time = time2;
            this.type = type2;
        }

        public void run() {
            try {
                if (ColorAppStartupManager.UPLOAD_TYPE_SERVICE_CONNECTED.equals(this.type)) {
                    int maxCount = 1;
                    Map<AppBindRecord, Integer> appBindRecordMap = new HashMap<>();
                    Iterator it = this.pr.connections.iterator();
                    while (it.hasNext()) {
                        AppBindRecord b = ((ConnectionRecord) it.next()).binding;
                        if (appBindRecordMap.containsKey(b)) {
                            int newCount = appBindRecordMap.get(b).intValue() + 1;
                            appBindRecordMap.put(b, Integer.valueOf(newCount));
                            maxCount = newCount > maxCount ? newCount : maxCount;
                        } else {
                            appBindRecordMap.put(b, 1);
                        }
                    }
                    List<AppBindRecord> appBindRecordList = new ArrayList<>();
                    for (AppBindRecord b2 : appBindRecordMap.keySet()) {
                        if (appBindRecordMap.get(b2).intValue() == maxCount) {
                            appBindRecordList.add(b2);
                        }
                    }
                    HashMap hashMap = new HashMap();
                    hashMap.put(ColorAppStartupManager.UPLOAD_KEY_TYPE, ColorAppStartupManager.UPLOAD_TYPE_SERVICE_CONNECTED);
                    hashMap.put(ColorAppStartupManager.UPLOAD_KEY_PROCESS_NAME, this.pr.processName);
                    hashMap.put(ColorAppStartupManager.UPLOAD_KEY_SERVICE_CONNECT_COUNT, String.valueOf(this.pr.connections.size()));
                    StringBuilder sb = new StringBuilder();
                    sb.append("maxCount:");
                    sb.append(maxCount);
                    sb.append('{');
                    for (AppBindRecord b3 : appBindRecordList) {
                        sb.append(b3.toString());
                        sb.append(b3.intent.toString());
                        sb.append(';');
                    }
                    sb.append('}');
                    hashMap.put(ColorAppStartupManager.UPLOAD_KEY_MAX_CONNECT_INTENT, sb.toString());
                    hashMap.put(ColorAppStartupManager.UPLOAD_KEY_SERVICE_CONNECT_INTERVAL, String.valueOf((this.pr.connections.size() - ColorAppStartupManager.this.mCpnUpperLimitThresholdConnected) / ColorAppStartupManager.this.mCpnUpperLimitCountInterval));
                    ColorAppStartupManager.this.mCpnUpperLimitMap.add(hashMap);
                    ColorAppStartupManager.this.uploadCpnUpperLimit();
                } else if (ColorAppStartupManager.UPLOAD_TYPE_REMOVE_SERVICE_CONNECTION.equals(this.type)) {
                    HashMap hashMap2 = new HashMap();
                    hashMap2.put(ColorAppStartupManager.UPLOAD_KEY_TYPE, ColorAppStartupManager.UPLOAD_TYPE_REMOVE_SERVICE_CONNECTION);
                    hashMap2.put(ColorAppStartupManager.UPLOAD_KEY_PROCESS_NAME, this.pr.processName);
                    hashMap2.put(ColorAppStartupManager.UPLOAD_KEY_REMOVE_SERVICE_CONNECTION, String.valueOf(this.time));
                    ColorAppStartupManager.this.mCpnUpperLimitMap.add(hashMap2);
                    ColorAppStartupManager.this.uploadCpnUpperLimit();
                }
            } catch (Exception e) {
            }
        }
    }

    public void setPreventIndulgeController(IOppoAppStartController controller) {
        this.mPreventIndulgeController = controller;
    }

    public void addPreventIndulgeList(List<String> pkgNames) {
        if (pkgNames != null) {
            synchronized (this.mPreventIndulgeList) {
                this.mPreventIndulgeList.clear();
                this.mPreventIndulgeList.addAll(pkgNames);
            }
        }
    }

    private boolean isInPreventIndulgeList(String pkgName) {
        boolean result = false;
        synchronized (this.mPreventIndulgeList) {
            if (!(this.mPreventIndulgeList == null || pkgName == null)) {
                result = this.mPreventIndulgeList.contains(pkgName);
            }
        }
        return result;
    }

    /* access modifiers changed from: private */
    public void notifyPreventIndulge(String pkgName) {
        IOppoAppStartController iOppoAppStartController = this.mPreventIndulgeController;
        if (iOppoAppStartController != null) {
            try {
                iOppoAppStartController.notifyPreventIndulge(pkgName);
            } catch (Exception e) {
                this.mPreventIndulgeController = null;
                Log.e(ColorAppStartupMonitorInfo.TAG, "notifyPreventIndulgeInfo failed!!");
            }
        }
    }

    private class PreventIndulgeRunnable implements Runnable {
        private String mPkgName;

        public PreventIndulgeRunnable(String pkgName) {
            this.mPkgName = pkgName;
        }

        public void run() {
            ColorAppStartupManager.this.notifyPreventIndulge(this.mPkgName);
        }
    }

    public boolean checkPreventIndulge(IColorActivityRecordEx r) {
        if (r == null || !isInPreventIndulgeList(r.getPackageName())) {
            return false;
        }
        if (this.mDebugSwitch) {
            Log.v(ColorAppStartupMonitorInfo.TAG, "startActivityFromRecents return undo, because of checkPreventIndulge: " + r.getPackageName());
        }
        this.mHandler.post(new PreventIndulgeRunnable(r.getPackageName()));
        return true;
    }

    /* access modifiers changed from: private */
    public String getDefaultDialerApp() {
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService == null) {
            return "";
        }
        try {
            return ((TelecomManager) activityManagerService.mContext.getSystemService("telecom")).getDefaultDialerPackage();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void startupManagerFileChange() {
        this.mHandler.postDelayed(new Runnable() {
            /* class com.android.server.am.ColorAppStartupManager.AnonymousClass4 */

            public void run() {
                ColorAppStartupManagerUtils.getInstance().readStartupManagerFile();
                ColorAppStartupManager.this.updateConfiglist();
                if (ColorAppStartupManager.this.mDebugSwitch) {
                    Log.i(ColorAppStartupMonitorInfo.TAG, "startupManagerFileChange update success");
                }
            }
        }, 5000);
    }

    public void startupMonitorFileChange() {
        this.mHandler.postDelayed(new Runnable() {
            /* class com.android.server.am.ColorAppStartupManager.AnonymousClass5 */

            public void run() {
                ColorAppStartupManagerUtils.getInstance().readStartupMonitorFile();
                ColorAppStartupManager.this.updateMonitorlist();
                if (ColorAppStartupManager.this.mDebugSwitch) {
                    Log.i(ColorAppStartupMonitorInfo.TAG, "startupMonitorFileChange  update success");
                }
            }
        }, 5000);
    }

    public void associateWhiteFileChanage(final int userId) {
        this.mHandler.postDelayed(new Runnable() {
            /* class com.android.server.am.ColorAppStartupManager.AnonymousClass6 */

            public void run() {
                ColorAppStartupManagerUtils.getInstance().readAssociateStartWhiteFile(userId);
                ColorAppStartupManager.this.updateAssociateStartList();
                if (ColorAppStartupManager.this.mDebugSwitch) {
                    Log.i(ColorAppStartupMonitorInfo.TAG, "associateWhiteFileChanage  update success");
                }
            }
        }, 5000);
    }

    /* access modifiers changed from: private */
    public boolean isInGamePayMonitorCpnList(String pkgName) {
        return ColorAppStartupManagerUtils.getInstance().getGamePayMonitorCpnList().contains(pkgName);
    }

    /* access modifiers changed from: private */
    public boolean isInGamePayMonitorFuzzyCpnList(String pkgName, String cpnName) {
        List<String> gamePayMonitorFuzzyCpnList = ColorAppStartupManagerUtils.getInstance().getGamePayMonitorFuzzyCpnList();
        int length = gamePayMonitorFuzzyCpnList.size();
        for (int i = 0; i < length; i++) {
            String payCpnName = gamePayMonitorFuzzyCpnList.get(i);
            int index = payCpnName.indexOf("#");
            if (index >= 0 && index + 1 < payCpnName.length()) {
                String pkg = payCpnName.substring(0, index);
                String cpnKey = payCpnName.substring(index + 1, payCpnName.length());
                if (!TextUtils.isEmpty(pkg) && !TextUtils.isEmpty(cpnKey)) {
                    if (cpnKey.equals("all")) {
                        if (pkg.equals(pkgName)) {
                            return true;
                        }
                    } else if (cpnName.contains(pkg) && cpnName.contains(cpnKey)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isInGameMonitorPackageList(String pkgName) {
        return ColorAppStartupManagerUtils.getInstance().getGameMonitorPackageList().contains(pkgName);
    }

    private boolean isExGlobalBlackList(String packgeName) {
        Bundle data = OppoListManager.getInstance().getConfigInfo(EX_GLOBAL_BLACK_LIST);
        if (data != null) {
            ArrayList<String> list = data.getStringArrayList(EX_GLOBAL_BLACK_LIST);
            if (list == null || !list.contains(packgeName)) {
                return false;
            }
            return true;
        } else if (ColorAppStartupManagerUtils.getInstance().getExpGlobalBlackFile().contains(packgeName)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAllowBackgroundRestrict(String callerPkgName, Intent intent) {
        String action;
        if (intent == null || callerPkgName == null || (action = intent.getAction()) == null || !inBackgroundRestrictCallerPkgList(callerPkgName, action)) {
            return false;
        }
        if (!this.mDebugSwitch) {
            return true;
        }
        Log.i(ColorAppStartupMonitorInfo.TAG, "allow background restrict: caller=" + callerPkgName + ", action=" + action);
        return true;
    }

    public void monitorActivityStartInfo(String allowStartActivityType) {
        Intent intent;
        if (this.mSwitchMonitor && (intent = this.mCurrentStartActivityIntent) != null && intent.getComponent() != null) {
            boolean isScreeOn = isScreenOn();
            String calledPkg = this.mCurrentStartActivityIntent.getComponent().getPackageName();
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "monitorActivityStartInfo: callerPkg=" + this.mCurrentStartActivityCallerPackage + " calledPkg=" + calledPkg + " calledCpnName=" + this.mCurrentStartActivityIntent.getComponent().getClassName() + " isScreeOn=" + isScreeOn + " type=" + allowStartActivityType);
            }
            ColorAppStartupStatistics.getInstance().collectPopupActivityInfo(this.mCurrentStartActivityCallerPackage, calledPkg, this.mCurrentStartActivityIntent.getComponent().getClassName(), "Q_noNeed", String.valueOf(isScreeOn), allowStartActivityType);
        }
    }

    public void resetSpecServiceRestartTime(ServiceRecord r, long now, long minDuration) {
        List<String> quickProcList;
        if (r != null && r.processName != null && (quickProcList = OppoListManager.getInstance().getQuickRestartProcList()) != null && quickProcList.contains(r.processName) && r.restartDelay > (this.mAms.mConstants.SERVICE_MIN_RESTART_TIME_BETWEEN / 2) + minDuration) {
            r.restartDelay = (this.mAms.mConstants.SERVICE_MIN_RESTART_TIME_BETWEEN / 2) + minDuration;
            r.nextRestartTime = r.restartDelay + now;
            if (this.mDynamicDebug) {
                Slog.d(ColorAppStartupMonitorInfo.TAG, "Adjust restart mm:push in " + r.restartDelay);
            }
        }
    }

    private boolean isPkgInRecentTasks(String pkg) {
        return this.mAppStartupHelper.isPkgInRecentTasks(this.mAms, pkg);
    }

    public boolean isCoreApp(ProcessRecord app) {
        if (isExpVersion()) {
            return true;
        }
        if (!(app == null || app.info == null || app.info.packageName == null)) {
            if (isDefaultAllowStart(app.info)) {
                return true;
            }
            if ((app.processName != null && OppoListManager.getInstance().getGlobalProcessWhiteList(this.mAms.mContext).contains(app.processName)) || OppoListManager.getInstance().isInAutoBootWhiteList(app.info.packageName, UserHandle.getUserId(app.info.uid)) || app.getCurProcState() <= 2 || ColorAppStartupManagerUtils.getInstance().getRestartServiceWhiteList().contains(app.info.packageName)) {
                return true;
            }
            List<String> restartServiceInRecentList = ColorAppStartupManagerUtils.getInstance().getRestartServiceInRecentList();
            if (restartServiceInRecentList != null) {
                for (String str : restartServiceInRecentList) {
                    if (str != null && app.info.packageName.equals(str)) {
                        boolean inRecent = isPkgInRecentTasks(app.info.packageName);
                        if (this.mDynamicDebug) {
                            Log.d(ColorAppStartupMonitorInfo.TAG, app.processName + " iR=" + inRecent);
                        }
                        if (!inRecent) {
                            return false;
                        }
                        if (this.mDynamicDebug) {
                            Log.d(ColorAppStartupMonitorInfo.TAG, "allow restart service in proc " + app.processName);
                        }
                        return true;
                    }
                }
            }
            if (isRecentLockedApps(app.info.packageName, UserHandle.getUserId(app.info.uid)) || OppoListManager.getInstance().isLiveWallpaper(app.info.packageName, UserHandle.getUserId(app.info.uid)) || isInclude(this.mDefaultInputMethod, app.getPackageList()) || isHomeProcess(this.mAms.mContext, app.getPackageList())) {
                return true;
            }
        }
        if (app != null && this.mDynamicDebug) {
            Log.d(ColorAppStartupMonitorInfo.TAG, "this is not core app" + app.processName);
        }
        return false;
    }

    public void sendOppoSiteMsg(String keyword) {
        ActivityManagerService activityManagerService = this.mAms;
        if (activityManagerService != null) {
            this.mAms.mHandler.sendMessageAtFrontOfQueue(activityManagerService.mHandler.obtainMessage(1003, keyword));
        }
    }

    public void handOppoSiteMsg(Message msg) {
        if (this.mAms != null) {
            Intent tmpIntent = new Intent("android.intent.action.WEB_SEARCH");
            tmpIntent.addFlags(268435456);
            tmpIntent.putExtra(INTENT_QUERY_EXTRA, (String) msg.obj);
            this.mAms.mContext.startActivity(tmpIntent);
        }
    }

    public void cleanProviders(ProcessRecord app) {
        if (this.mDynamicDebug) {
            Log.i(ColorAppStartupMonitorInfo.TAG, app + " died but not restart......");
        }
        if (!app.pubProviders.isEmpty()) {
            for (ContentProviderRecord cpr : app.pubProviders.values()) {
                IColorActivityManagerServiceInner iColorActivityManagerServiceInner = this.mColorAmsInner;
                if (iColorActivityManagerServiceInner != null) {
                    iColorActivityManagerServiceInner.removeDyingProviderLocked(app, cpr, true);
                }
                cpr.provider = null;
                cpr.proc = null;
            }
            app.pubProviders.clear();
        }
        int length = ((ActivityManagerService) this.mAms).mLaunchingProviders.size();
        for (int i = 0; i < length; i++) {
            ContentProviderRecord cpr2 = (ContentProviderRecord) this.mAms.mLaunchingProviders.get(i);
            if (cpr2.launchingApp == app) {
                IColorActivityManagerServiceInner iColorActivityManagerServiceInner2 = this.mColorAmsInner;
                if (iColorActivityManagerServiceInner2 != null) {
                    iColorActivityManagerServiceInner2.removeDyingProviderLocked(app, cpr2, true);
                }
                length = this.mAms.mLaunchingProviders.size();
            }
        }
    }

    private String getLivePackageForLiveWallPaper() {
        return null;
    }

    public boolean isHomeProcess(Context context, String[] pkgList) {
        if (context == null || pkgList == null) {
            return false;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo defaultLaucher = context.getPackageManager().resolveActivity(intent, 65536);
        if (defaultLaucher == null || defaultLaucher.activityInfo == null) {
            List<ResolveInfo> homeList = context.getPackageManager().queryIntentActivities(intent, 270532608);
            if (homeList == null || homeList.isEmpty()) {
                return false;
            }
            for (ResolveInfo ri : homeList) {
                if (isInclude(ri.activityInfo.packageName, pkgList)) {
                    return true;
                }
            }
            return false;
        } else if (!isInclude(defaultLaucher.activityInfo.packageName, pkgList)) {
            return false;
        } else {
            if (this.mDynamicDebug) {
                Log.i(ColorAppStartupMonitorInfo.TAG, "defaultLaucher= " + defaultLaucher.activityInfo.packageName);
            }
            return true;
        }
    }

    private boolean isInclude(String value, String[] list) {
        if (list == null || list.length <= 0 || value == null) {
            return false;
        }
        try {
            for (String str : list) {
                if (str.equals(value)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.w(ColorAppStartupMonitorInfo.TAG, "isInclude has exception! ", e);
            return true;
        }
    }

    private class CollectGamePayAbnormalRunnable implements Runnable {
        private String mCalledCpn;
        private String mCalledPkgName;
        private String mCallerCpn;
        private String mCallerPkgName;
        private Intent mIntent;

        public CollectGamePayAbnormalRunnable(String callerPkg, String callerCpn, String calledPkg, String calledCpn, Intent intent) {
            this.mCallerPkgName = callerPkg;
            this.mCallerCpn = callerCpn;
            this.mCalledPkgName = calledPkg;
            this.mCalledCpn = calledCpn;
            this.mIntent = intent;
        }

        public void run() {
            Intent intent;
            if (this.mCallerPkgName != null && this.mCallerCpn != null && this.mCalledPkgName != null && this.mCalledCpn != null) {
                if (ColorAppStartupManager.this.mDynamicDebug) {
                    Log.i(ColorAppStartupMonitorInfo.TAG, "gamePayAbnormal mCallerPkgName=" + this.mCallerPkgName + " mCallerCpn=" + this.mCallerCpn + " mCalledPkgName=" + this.mCalledPkgName + " mCalledCpn=" + this.mCalledCpn);
                }
                if (!ColorAppStartupManager.this.isInGamePayAbnormalWhiteList(this.mCallerPkgName, this.mCallerCpn, this.mCalledPkgName, this.mCalledCpn)) {
                    if (ColorAppStartupManager.this.isInGamePayAbnormalCallerBlackList(this.mCallerPkgName)) {
                        boolean isCollected = false;
                        if (ColorAppStartupManager.this.isInGamePayAbnormalCalledBlackFirstMap(this.mCalledPkgName, this.mCalledCpn)) {
                            isCollected = true;
                        }
                        if (!isCollected && ColorAppStartupManager.this.isInGamePayAbnormalCalledBlackSecondMap(this.mCalledPkgName, this.mCalledCpn, null, false)) {
                            boolean unused = ColorAppStartupManager.mGamePayAbnormalSecondFlag = true;
                            String unused2 = ColorAppStartupManager.mGamePayAbnormalFirstCpn = this.mCalledCpn;
                            String unused3 = ColorAppStartupManager.mGamePayAbnormalCallerPkg = this.mCallerPkgName;
                            String unused4 = ColorAppStartupManager.mGamePayAbnormalCallerCpn = this.mCallerCpn;
                        } else if (!isCollected && (intent = this.mIntent) != null) {
                            String action = intent.getAction();
                            String url = this.mIntent.getDataString();
                            if (action != null && url != null && action.equals("android.intent.action.VIEW") && url.toLowerCase().startsWith("http") && ColorAppStartupManager.this.isInGamePayAbnormalBlackUrlKeyList(url)) {
                                this.mCalledCpn = ColorAppStartupManager.this.composePackage(this.mCalledCpn, url);
                                isCollected = true;
                            }
                        }
                        if (ColorAppStartupManager.this.mDynamicDebug) {
                            Log.i(ColorAppStartupMonitorInfo.TAG, "gamePayAbnormal mCallerPkgName =" + this.mCallerPkgName + ", mCallerCpn= " + this.mCallerCpn + ", mCalledPkgName= " + this.mCalledPkgName + ", mCalledCpn= " + this.mCalledCpn + "isCollected = " + isCollected);
                        }
                        if (isCollected) {
                            synchronized (ColorAppStartupManager.this.mCollectGamePayAbnormalList) {
                                ColorAppStartupManager.this.collectProcessInfo(ColorAppStartupManager.this.mCollectGamePayAbnormalList, this.mCallerPkgName, this.mCallerCpn, this.mCalledPkgName, this.mCalledCpn);
                                if (ColorAppStartupManager.this.mCollectGamePayAbnormalList.size() >= ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCheckCount()) {
                                    ColorAppStartupManager.this.uploadProcessInfo(ColorAppStartupManager.this.mCollectGamePayAbnormalList, "game_pay");
                                }
                            }
                        }
                    } else if (!ColorAppStartupManager.mGamePayAbnormalSecondFlag || !this.mCallerPkgName.equals(this.mCalledPkgName) || !ColorAppStartupManager.this.isInGamePayAbnormalCalledBlackSecondMap(this.mCalledPkgName, ColorAppStartupManager.mGamePayAbnormalFirstCpn, this.mCalledCpn, true)) {
                        ColorAppStartupManager.this.resetGamePayAbnormalStatus();
                    } else {
                        if (ColorAppStartupManager.this.mDynamicDebug) {
                            Log.i(ColorAppStartupMonitorInfo.TAG, "gamePayAbnormal mCallerPkgName =" + this.mCallerPkgName + ", mCallerCpn= " + this.mCallerCpn + ", mCalledPkgName= " + this.mCalledPkgName + ", mCalledCpn= " + this.mCalledCpn + "mGamePayAbnormalSecondFlag = " + ColorAppStartupManager.mGamePayAbnormalSecondFlag);
                        }
                        synchronized (ColorAppStartupManager.this.mCollectGamePayAbnormalList) {
                            ColorAppStartupManager colorAppStartupManager = ColorAppStartupManager.this;
                            List access$200 = ColorAppStartupManager.this.mCollectGamePayAbnormalList;
                            String access$2900 = ColorAppStartupManager.mGamePayAbnormalCallerPkg;
                            String access$3000 = ColorAppStartupManager.mGamePayAbnormalCallerCpn;
                            String str = this.mCalledPkgName;
                            colorAppStartupManager.collectProcessInfo(access$200, access$2900, access$3000, str, ColorAppStartupManager.mGamePayAbnormalFirstCpn + "," + this.mCalledCpn);
                            if (ColorAppStartupManager.this.mCollectGamePayAbnormalList.size() >= ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCheckCount()) {
                                ColorAppStartupManager.this.uploadProcessInfo(ColorAppStartupManager.this.mCollectGamePayAbnormalList, "game_pay");
                            }
                        }
                        ColorAppStartupManager.this.resetGamePayAbnormalStatus();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void resetGamePayAbnormalStatus() {
        mGamePayAbnormalSecondFlag = false;
        mGamePayAbnormalFirstCpn = null;
        mGamePayAbnormalCallerPkg = null;
        mGamePayAbnormalCallerCpn = null;
    }

    /* access modifiers changed from: private */
    public boolean isInGamePayAbnormalCalledBlackSecondMap(String calledPkgName, String calledFirstCpn, String calledSecondCpn, boolean isLookSecondCpn) {
        ArrayMap<String, List<String>> payCpnMap;
        Set<String> payKeySet;
        ArrayMap<String, ArrayMap<String, List<String>>> paySecondCpnMap = ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCalledBlackSecondCpnMap();
        if (paySecondCpnMap == null || !paySecondCpnMap.containsKey(calledPkgName) || (payCpnMap = paySecondCpnMap.get(calledPkgName)) == null || (payKeySet = payCpnMap.keySet()) == null || (iterator = payKeySet.iterator()) == null) {
            return false;
        }
        for (String firstCpnKey : payKeySet) {
            if (!TextUtils.isEmpty(firstCpnKey) && calledFirstCpn.contains(firstCpnKey)) {
                if (!isLookSecondCpn) {
                    return true;
                }
                List<String> secondCpnList = payCpnMap.get(firstCpnKey);
                if (secondCpnList != null) {
                    int size = secondCpnList.size();
                    for (int i = 0; i < size; i++) {
                        if (calledSecondCpn.contains(secondCpnList.get(i))) {
                            return true;
                        }
                    }
                    continue;
                } else {
                    continue;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isInGamePayAbnormalCalledBlackFirstMap(String calledPkgName, String calledCpn) {
        List<String> calledBlackPkgList = ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCalledBlackPkgList();
        if (calledBlackPkgList != null && isInGamePayAbnormalFilterList(calledPkgName, calledBlackPkgList, true)) {
            if (this.mDynamicDebug) {
                Log.i(ColorAppStartupMonitorInfo.TAG, "gamePayAbnormal isInGamePayAbnormalCalledBlackFirstMap BlackPkgList");
            }
            return true;
        } else if (!isInGamePayAbnormalPkgCpnMap(calledPkgName, calledCpn, ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCalledBlackFirstCpnMap())) {
            return false;
        } else {
            if (this.mDynamicDebug) {
                Log.i(ColorAppStartupMonitorInfo.TAG, "gamePayAbnormal isInGamePayAbnormalCalledBlackFirstMap BlackFirstCpnMap");
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public boolean isInGamePayAbnormalWhiteList(String callerPkg, String callerCpn, String calledPkg, String calledCpn) {
        if (!ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCallerWhiteCpnList().contains(callerCpn) && !isInGamePayAbnormalFilterList(callerPkg, ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCallerWhitePkgList(), true) && !isInGamePayAbnormalPkgCpnMap(callerPkg, callerCpn, ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCallerWhitePkgCpnMap()) && !isInGamePayAbnormalFilterList(callerPkg, ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCallerWhiteKeyList(), false) && !isInGamePayAbnormalFilterList(calledPkg, ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCalledWhitePkgList(), true) && !isInGamePayAbnormalFilterList(calledPkg, ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCalledWhiteKeyList(), false) && !isInGamePayAbnormalPkgCpnMap(calledPkg, calledCpn, ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCalledWhitePkgCpnMap())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public boolean isInGamePayAbnormalCallerBlackList(String pkgName) {
        if (!isInGamePayAbnormalFilterList(pkgName, ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCallerBlackPkgList(), true) && !isInGamePayAbnormalFilterList(pkgName, ColorAppStartupManagerUtils.getInstance().getGamePayAbnormalCallerBlackKeyList(), false)) {
            return false;
        }
        return true;
    }

    private boolean isInGamePayAbnormalFilterList(String pkgCpnName, List<String> filterList, boolean isAccurate) {
        if (filterList == null || filterList.size() <= 0) {
            return false;
        }
        int size = filterList.size();
        for (int i = 0; i < size; i++) {
            String filterName = filterList.get(i);
            if (isAccurate) {
                if (pkgCpnName.equals(filterName)) {
                    return true;
                }
            } else if (pkgCpnName.contains(filterName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInGamePayAbnormalPkgCpnMap(String pkgName, String cpnName, ArrayMap<String, List<String>> whitePkgCpnMap) {
        if (TextUtils.isEmpty(cpnName)) {
            return false;
        }
        int i = 0;
        while (i < whitePkgCpnMap.size()) {
            String key = whitePkgCpnMap.keyAt(i);
            List<String> cpnList = whitePkgCpnMap.valueAt(i);
            if (TextUtils.isEmpty(key) || cpnList == null || !key.equals(pkgName)) {
                i++;
            } else {
                for (String cpn : cpnList) {
                    if (cpnName.contains(cpn)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void uploadProcessInfo(List<ColorAppStartupMonitorInfo> infoList, String eventId) {
        if (infoList != null && !infoList.isEmpty()) {
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "--------------------------uploadGamePay start---------------------------");
            }
            List<Map<String, String>> uploadList = new ArrayList<>();
            int length = infoList.size();
            for (int i = 0; i < length; i++) {
                ColorAppStartupMonitorInfo appInfo = infoList.get(i);
                if (appInfo != null) {
                    uploadList.addAll(appInfo.getGamePayMap());
                    if (this.mDebugSwitch) {
                        appInfo.dumpGamePay();
                    }
                }
                if (i == length - 1) {
                    appInfo.clearGamePay();
                }
            }
            if (this.mDynamicDebug) {
                Log.d(ColorAppStartupMonitorInfo.TAG, "--------------------------uploadGamePay end-----------------------------");
            }
            ActivityManagerService activityManagerService = this.mAms;
            if (activityManagerService != null) {
                OppoStatistics.onCommon(activityManagerService.mContext, "20089", eventId, uploadList, false);
            }
            infoList.clear();
        }
    }

    /* access modifiers changed from: private */
    public void collectProcessInfo(List<ColorAppStartupMonitorInfo> infoList, String callerPkg, String callerCpn, String calledPkg, String calledCpn) {
        ColorAppStartupMonitorInfo interceptInfo = null;
        Iterator<ColorAppStartupMonitorInfo> it = infoList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ColorAppStartupMonitorInfo appInfo = it.next();
            if (appInfo.getCallerPkgName().equals(callerPkg)) {
                interceptInfo = appInfo;
                break;
            }
        }
        if (interceptInfo == null) {
            infoList.add(ColorAppStartupMonitorInfo.builderGamePay(callerPkg, callerCpn, calledPkg, calledCpn));
        } else {
            interceptInfo.increaseGamePayCount(callerPkg, callerCpn, calledPkg, calledCpn);
        }
    }

    public List adjustQueueOrderedBroadcastLocked(BroadcastQueue _queue, Intent _intent, ProcessRecord _callerApp, String _callerPackage, int _callingPid, int _callingUid, boolean _callerInstantApp, String _resolvedType, String[] _requiredPermissions, int _appOp, BroadcastOptions _options, List _receivers, IIntentReceiver _resultTo, int _resultCode, String _resultData, Bundle _resultExtras, boolean _serialized, boolean _sticky, boolean _initialSticky, int _userId, boolean _allowBackgroundActivityStarts, boolean _timeoutExempt) {
        return ColorBootCompleteBroadcastManager.getInstance().adjustQueueOrderedBroadcastLocked(_queue, _intent, _callerApp, _callerPackage, _callingPid, _callingUid, _callerInstantApp, _resolvedType, _requiredPermissions, _appOp, _options, _receivers, _resultTo, _resultCode, _resultData, _resultExtras, _serialized, _sticky, _initialSticky, _userId, _allowBackgroundActivityStarts, _timeoutExempt);
    }

    public void scheduleNextDispatch(Intent intent) {
        ColorBootCompleteBroadcastManager.getInstance().scheduleNextDispatch(intent);
    }

    private OppoBaseActivityManagerService typeCasting(ActivityManagerService ams) {
        if (ams != null) {
            return (OppoBaseActivityManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseActivityManagerService.class, ams);
        }
        return null;
    }
}
