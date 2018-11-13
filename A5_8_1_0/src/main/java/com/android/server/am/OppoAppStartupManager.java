package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.arp.OppoArpPeer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IPowerManager.Stub;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.util.OppoSafeDbReader;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoAppStartupStatistics.OppoCallActivityEntry;
import com.android.server.coloros.OppoListManager;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import com.oppo.app.IOppoAppStartController;
import com.oppo.hypnus.Hypnus;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

public class OppoAppStartupManager {
    private static final String ACTION_OPPO_STARTUP_APP_MONITOR = "oppo.intent.action.OPPO_STARTUP_APP_MONITOR";
    protected static final String APP_START_BY_ASSOCIATE = "associate";
    protected static final String APP_START_BY_BOOTSTART = "bootstart";
    protected static final String APP_START_BY_CLICK = "click";
    protected static final String APP_START_BY_OTHER = "other";
    protected static final String APP_START_BY_SAMEAPP = "sameapp";
    protected static final String APP_START_BY_WIDGET = "widget";
    private static final String CALL_ARG_CRASH = "crash";
    private static final String CALL_ARG_STARTUP = "startup";
    public static final boolean DEBUG_DETAIL = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int HYPNUS_VALUE = 1000;
    private static final int HYPNUS_VALUE_SERVICE = 4000;
    private static final int LAST_ACTIVITY_CALL_COUNT = 5;
    private static final String OPPO_SAFE_URL_INTERCEPTION = "oppo.intent.action.OPPO_SAFE_URL_INTERCEPTION";
    private static final String PRE_COLOROS = "com.coloros.";
    private static final String PRE_NEARME = "com.nearme.";
    private static final String PRE_OPPO = "com.oppo.";
    public static final String REBIND_FORCE_STOP = "forceStop";
    public static final String REBIND_SETTING_CHANGE = "settingChange";
    public static final String REBIND_USER_SWITCH = "userSwitch";
    public static final String REBIND_USER_UNLOCK = "userUnlock";
    private static final String RECORD_ACTIVITY_LAUNCH_MODE = "2";
    private static final String RECORD_ALLOW_LAUNCH_TYPE = "0";
    private static final String RECORD_ASSOCIATE_LAUNCH_MODE = "1";
    private static final String RECORD_AUTO_LAUNCH_MODE = "0";
    private static final String RECORD_CALLER_ANDROID = "Android";
    private static final String RECORD_PREVENT_LAUNCH_TYPE = "1";
    private static final String SEND_BROADCAST_TO_PKG = "com.daemon.shelper";
    private static final String START_MARKET_ACTION = "oppo.intent.action.OPPO_STARTUP_MARKET";
    private static final String START_MARKET_ACTION_INTENT_KEY = "com.tencent.mm.intent";
    private static final String START_MARKET_ACTION_SWITCH_KEY = "switchValue";
    public static final String START_PROCESS_FROM_ALARM = "system[alarmManger]";
    public static final String START_PROCESS_FROM_JOB = "system[jobScheduler]";
    public static final String START_PROCESS_FROM_LOCATION = "system[location]";
    public static final String START_PROCESS_FROM_NOTIFICATION_LISTENER = "system[notificationListener]";
    private static final String SYSTEM_UI_PKG = "com.android.systemui";
    public static final String TAG = "OppoAppStartupManager";
    private static final String TENCENT_MM_PKG = "com.tencent.mm";
    public static final String TYPE_ACTIVITY = "activity";
    public static final String TYPE_BIND_SERVICE = "bs";
    public static final String TYPE_BIND_SERVICE_ACTION = "bsa";
    public static final String TYPE_BIND_SERVICE_FORM_NOTIFICATION = "bsfn";
    public static final String TYPE_BIND_SERVICE_FROM_JOB = "bsfj";
    public static final String TYPE_BIND_SERVICE_FROM_SYNC = "bsfs";
    public static final String TYPE_BIND_SERVICE_FROM_SYSTEMUI = "bs_systemui";
    public static final String TYPE_BROADCAST = "broadcast";
    public static final String TYPE_BROADCAST_ACTION = "broadcast_action";
    public static final String TYPE_DIALOG = "dialog";
    public static final String TYPE_PROVIDER = "provider";
    public static final String TYPE_SERVICE = "service";
    public static final String TYPE_START_PROCEESS_LOECKED = "startProcessLocked";
    public static final String TYPE_START_SERVICE = "ss";
    public static final String TYPE_START_SERVICE_ACTION = "ssa";
    public static final String TYPE_START_SERVICE_CALL_NULL = "sscn";
    public static final String TYPE_START_SERVICE_FROM_ALARM = "ssfa";
    protected static final String UPLOAD_BLACK_EVENTID = "intercept_black";
    protected static final String UPLOAD_GMAE_PAY = "game_pay";
    protected static final String UPLOAD_KEY_CALLED_CPN = "cpn";
    protected static final String UPLOAD_KEY_CALLED_PKG = "calledPkg";
    protected static final String UPLOAD_KEY_CALLER_APP = "callerapp";
    protected static final String UPLOAD_KEY_CALLER_PID = "callerpid";
    protected static final String UPLOAD_KEY_CALLER_PKG = "callerPkg";
    protected static final String UPLOAD_KEY_HOST_TYPE = "host_type";
    protected static final String UPLOAD_KEY_POPUP_TYPE = "popup_type";
    protected static final String UPLOAD_KEY_PROC_COUNT = "count";
    protected static final String UPLOAD_KEY_PROC_NAME = "proc";
    protected static final String UPLOAD_KEY_SCREEN_STATE = "screenState";
    protected static final String UPLOAD_KEY_START_MODE = "start_mode";
    protected static final String UPLOAD_KEY_TOP_PKG = "topPkg";
    protected static final String UPLOAD_LOGTAG = "20089";
    protected static final String UPLOAD_THIRD_EVENTID = "startup_third_app";
    private static OppoAppStartupManager sOppoAppStartupManager = null;
    private List<String> mActionBlackList;
    private final Object mActionLock;
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
    private ActivityStack mActivityStack;
    protected ActivityManagerService mAms;
    private List<String> mAssociateStartWhiteList;
    private final Object mAssociateStartWhiteLock;
    private AudioManager mAudioManager;
    private List<String> mAuthorizeCpnList;
    private final Object mAuthorizeCpnListLock;
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
    private List<OppoAppMonitorInfo> mCollectBlackListInterceptList;
    private List<OppoAppMonitorInfo> mCollectGamePayList;
    private List<String> mCollectPayCpnList;
    private List<OppoAppMonitorInfo> mCollectProcessInfoList;
    private IOppoAppStartController mController;
    private List<String> mCustomizeWhiteList;
    private final Object mCustomizeWhiteLock;
    final BroadcastReceiver mDateChangedReceiver;
    protected boolean mDebugSwitch;
    private String mDefaultInputMethod;
    private ContentObserver mDufaultInputMethodObserver;
    protected boolean mDynamicDebug;
    private List<String> mGlobalWhiteList;
    private Handler mHandler;
    private Hypnus mHypnus;
    private boolean mIsTenIntercept;
    private List<String> mJobWhiteList;
    private final Object mJobWhiteLock;
    private ArrayList<OppoCallActivityEntry> mLastCalledAcivityList;
    protected boolean mMonitorAll;
    private List<OppoAppMonitorInfo> mMonitorAppInfoList;
    private ArrayList<String> mMonitorAppUploadList;
    private List<String> mNotifyWhiteList;
    private final Object mNotifyWhiteLock;
    private final Object mPayCpnLitLock;
    private ActivityRecord mPreRecord;
    private List<String> mProtectList;
    private final Object mProtectLock;
    private List<String> mProviderBlacklist;
    private List<String> mProviderCpnWhiteList;
    private final Object mProviderLock;
    private final Object mProviderWhiteLock;
    private List<String> mReceiverActionBlackList;
    private final Object mReceiverActionLock;
    private List<String> mReceiverBlackList;
    private final Object mReceiverLock;
    private final Object mServiceLock;
    private List<String> mSeviceCpnBlacklist;
    final BroadcastReceiver mShelperActionReceiver;
    private List<String> mStartActivityReasonList;
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
            OppoAppStartupManager.this.notifyAppStartMonitorInfo(this.mPkg, this.mExceptionClass, this.mExceptionMsg, this.mExceptionTrace, this.mType);
        }
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
            OppoAppStartupManager.this.monitorAppStartInfo(this.mCallerPkgName, this.mCalleePkgName, this.mCallCpnOrAction, this.mCallType);
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
            OppoAppStartupManager.this.collectProcessStartInfo(this.mPackageName, this.mProcessName, this.mHostingType, this.mStartMode, this.mHostingNameStr != null ? this.mHostingNameStr : "");
            if (OppoAppStartupManager.this.mCollectProcessInfoList.size() >= OppoAppStartupManagerUtils.getInstance().getCallCheckCount()) {
                OppoAppStartupManager.this.uploadProcessStartInfo();
            }
        }
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
            if (this.mCalleePkg != null && this.mCalleeCpn != null && this.mStartMode != null) {
                OppoAppStartupManager.this.collectBlackListInterceptInfo(this.mCalleePkg, this.mCalleeCpn, this.mStartMode);
                if (OppoAppStartupManager.this.mCollectBlackListInterceptList.size() >= OppoAppStartupManagerUtils.getInstance().getCallCheckCount()) {
                    OppoAppStartupManager.this.uploadBlackListIntercept();
                }
            }
        }
    }

    private class CollectGamePayRunnable implements Runnable {
        private String mCalledCpn;
        private String mCalledPkgName;
        private String mCallerPkgName;
        private Intent mIntent;

        public CollectGamePayRunnable(String callerPkg, String calledPkg, String calledCpn, Intent intent) {
            this.mCallerPkgName = callerPkg;
            this.mCalledPkgName = calledPkg;
            this.mCalledCpn = calledCpn;
            this.mIntent = intent;
        }

        /* JADX WARNING: Missing block: B:4:0x0008, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            if (!(this.mCallerPkgName == null || this.mCalledPkgName == null || this.mCalledCpn == null || !this.mCallerPkgName.contains(AlertWindowNotification.PKG_SUFFIX_NEARME_GAMECENTER))) {
                boolean isCollected = false;
                synchronized (OppoAppStartupManager.this.mPayCpnLitLock) {
                    if (OppoAppStartupManager.this.mCollectPayCpnList.contains(this.mCalledCpn)) {
                        isCollected = true;
                    }
                }
                String action = this.mIntent.getAction();
                String url = this.mIntent.getDataString();
                if (action != null && url != null && action.equals("android.intent.action.VIEW") && url.toLowerCase().startsWith("http")) {
                    this.mCalledCpn = OppoAppStartupManager.this.composePackage(this.mCalledCpn, url);
                    isCollected = true;
                }
                if (isCollected) {
                    OppoAppStartupManager.this.collectGamePayProcessInfo(this.mCallerPkgName, this.mCalledPkgName, this.mCalledCpn);
                    if (OppoAppStartupManager.this.mCollectGamePayList.size() >= OppoAppStartupManagerUtils.getInstance().getCheckCount()) {
                        OppoAppStartupManager.this.uploadGamePayProcessInfo();
                    }
                }
            }
        }
    }

    private class SendBroadCastToMarket implements Runnable {
        Context mContext;
        Intent mIntent;
        int mInterceptSwitchValue;

        public SendBroadCastToMarket(Context context, Intent intent, int interceptSwitchValue) {
            this.mContext = context;
            this.mIntent = intent;
            this.mInterceptSwitchValue = interceptSwitchValue;
        }

        public void run() {
            Intent intent = new Intent(OppoAppStartupManager.START_MARKET_ACTION);
            intent.putExtra(OppoAppStartupManager.START_MARKET_ACTION_INTENT_KEY, this.mIntent);
            intent.putExtra(OppoAppStartupManager.START_MARKET_ACTION_SWITCH_KEY, this.mInterceptSwitchValue);
            intent.setPackage(OppoAppStartupManager.SEND_BROADCAST_TO_PKG);
            intent.setFlags(32);
            this.mContext.sendBroadcast(intent);
        }
    }

    public OppoAppStartupManager() {
        this.mDynamicDebug = false;
        this.mDebugSwitch = DEBUG_DETAIL | this.mDynamicDebug;
        this.mSwitch = SystemProperties.getBoolean("persist.sys.startupmanager", true);
        this.mSwitchMonitor = false;
        this.mSwitchInterceptActivity = false;
        this.mAms = null;
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
        this.mPayCpnLitLock = new Object();
        this.mCollectAppStartLock = new Object();
        this.mActivityCallerWhitePkgLock = new Object();
        this.mActivityCalledWhitePkgLock = new Object();
        this.mActivityCalledWhiteCpnLock = new Object();
        this.mActivityPkgKeyLock = new Object();
        this.mActivityCalledKeyLock = new Object();
        this.mBlackguardActivityLock = new Object();
        this.mCustomizeWhiteLock = new Object();
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
        this.mAssociateStartWhiteList = new ArrayList();
        this.mAuthorizeCpnList = new ArrayList();
        this.mGlobalWhiteList = new ArrayList();
        this.mMonitorAppUploadList = new ArrayList();
        this.mMonitorAppInfoList = new ArrayList();
        this.mLastCalledAcivityList = new ArrayList();
        this.mCollectProcessInfoList = new ArrayList();
        this.mCollectBlackListInterceptList = new ArrayList();
        this.mCollectGamePayList = new ArrayList();
        this.mCollectPayCpnList = new ArrayList();
        this.mStartActivityReasonList = new ArrayList();
        this.mAudioManager = null;
        this.mHypnus = null;
        this.mHandler = null;
        this.mSwitchBrowserInterceptUpload = false;
        this.mDefaultInputMethod = null;
        this.mIsTenIntercept = false;
        this.mController = null;
        this.mDateChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (OppoAppStartupManager.this.mSwitchMonitor) {
                    OppoAppStartupManager.this.notifyAppInterceptInfo();
                    OppoAppStartupManager.this.uploadBlackListIntercept();
                    OppoAppStartupManager.this.uploadProcessStartInfo();
                    OppoAppStartupStatistics.getInstance().uploadAppStartupList();
                    OppoAppStartupStatistics.getInstance().uploadPopupActivityList();
                }
                if (OppoAppStartupManager.this.mSwitchMonitor || OppoAppStartupManagerUtils.getInstance().getSwitchStatus()) {
                    OppoAppStartupManager.this.uploadGamePayProcessInfo();
                }
            }
        };
        this.mShelperActionReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals("oppo.intent.action.SHELPER_FINISH_ACTIVITY")) {
                    OppoAppStartupManager.this.handleForceFinish();
                }
            }
        };
        this.mDufaultInputMethodObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                OppoAppStartupManager.this.mDefaultInputMethod = OppoAppStartupManager.this.getDefaultInputMethod();
            }
        };
    }

    public OppoAppStartupManager(String name) {
        this.mDynamicDebug = false;
        this.mDebugSwitch = DEBUG_DETAIL | this.mDynamicDebug;
        this.mSwitch = SystemProperties.getBoolean("persist.sys.startupmanager", true);
        this.mSwitchMonitor = false;
        this.mSwitchInterceptActivity = false;
        this.mAms = null;
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
        this.mPayCpnLitLock = new Object();
        this.mCollectAppStartLock = new Object();
        this.mActivityCallerWhitePkgLock = new Object();
        this.mActivityCalledWhitePkgLock = new Object();
        this.mActivityCalledWhiteCpnLock = new Object();
        this.mActivityPkgKeyLock = new Object();
        this.mActivityCalledKeyLock = new Object();
        this.mBlackguardActivityLock = new Object();
        this.mCustomizeWhiteLock = new Object();
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
        this.mAssociateStartWhiteList = new ArrayList();
        this.mAuthorizeCpnList = new ArrayList();
        this.mGlobalWhiteList = new ArrayList();
        this.mMonitorAppUploadList = new ArrayList();
        this.mMonitorAppInfoList = new ArrayList();
        this.mLastCalledAcivityList = new ArrayList();
        this.mCollectProcessInfoList = new ArrayList();
        this.mCollectBlackListInterceptList = new ArrayList();
        this.mCollectGamePayList = new ArrayList();
        this.mCollectPayCpnList = new ArrayList();
        this.mStartActivityReasonList = new ArrayList();
        this.mAudioManager = null;
        this.mHypnus = null;
        this.mHandler = null;
        this.mSwitchBrowserInterceptUpload = false;
        this.mDefaultInputMethod = null;
        this.mIsTenIntercept = false;
        this.mController = null;
        this.mDateChangedReceiver = /* anonymous class already generated */;
        this.mShelperActionReceiver = /* anonymous class already generated */;
        this.mDufaultInputMethodObserver = /* anonymous class already generated */;
        this.mHypnus = Hypnus.getHypnus();
        registerLogModule();
        HandlerThread thread = new HandlerThread("AppStartupManager");
        thread.start();
        this.mHandler = new Handler(thread.getLooper());
    }

    public static final OppoAppStartupManager getInstance() {
        if (sOppoAppStartupManager == null) {
            sOppoAppStartupManager = new OppoAppStartupManager("OppoAppStartupManager");
        }
        return sOppoAppStartupManager;
    }

    public void initData() {
        Log.d("OppoAppStartupManager", "initData");
        this.mSwitchMonitor = OppoAppStartupManagerUtils.getInstance().isForumVersion();
        updateConfiglist();
        updateMonitorlist();
        updateCustomizeWhiteList();
        updateAssociateStartList();
        this.mDefaultInputMethod = getDefaultInputMethod();
        if (this.mAms != null) {
            this.mAms.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("default_input_method"), true, this.mDufaultInputMethodObserver);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.DATE_CHANGED");
            this.mAms.mContext.registerReceiver(this.mDateChangedReceiver, filter, null, this.mHandler);
            IntentFilter shelperAction = new IntentFilter();
            shelperAction.addAction("oppo.intent.action.SHELPER_FINISH_ACTIVITY");
            this.mAms.mContext.registerReceiver(this.mShelperActionReceiver, shelperAction, null, this.mHandler);
        }
        initPayCpnList();
        initStartActivityReason();
    }

    /* JADX WARNING: Missing block: B:3:0x0006, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleBroadcastIncludeForceStop(Intent intent, ProcessRecord callerApp) {
        if (this.mSwitch && intent != null && callerApp != null && (intent.getFlags() & 32) == 32 && callerApp.info != null && (callerApp.info.flags & 1) == 0) {
            if ("com.android.cts.robot.ACTION_POST".equals(intent.getAction())) {
                Log.d("OppoAppStartupManager", callerApp.processName + " is CTS app. do not remove the flag!");
                return;
            }
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", callerApp.processName + " is the thirdparty app. remove the flag! " + intent);
            }
            intent.setFlags(intent.getFlags() ^ 32);
        }
    }

    public boolean handleStartOrBindService(Intent service, ProcessRecord callerApp) {
        boolean result = false;
        if (!this.mSwitch || service == null || callerApp == null) {
            return false;
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", Log.getStackTraceString(new Throwable()));
        }
        if (callerApp.uid <= 10000) {
            if (this.mDynamicDebug) {
                Log.v("OppoAppStartupManager", "handleStartOrBindService callerApp.uid <= 10000 return");
                Log.v("OppoAppStartupManager", "handleStartOrBindService callerApp: " + callerApp);
            }
            return false;
        }
        if (this.mDynamicDebug) {
            Log.v("OppoAppStartupManager", "handleStartOrBindService: " + service + " args=" + service.getExtras());
            Log.v("OppoAppStartupManager", "handleStartOrBindService callerApp: " + callerApp);
        }
        ComponentName cpn = service.getComponent();
        if (cpn != null) {
            String cpnPkgName = cpn.getPackageName();
            String cpnClassName = cpn.getClassName();
            if (this.mDynamicDebug) {
                Log.v("OppoAppStartupManager", "handleStartOrBindService cpnPkgName == " + cpnPkgName);
                Log.v("OppoAppStartupManager", "handleStartOrBindService cpnClassName == " + cpnClassName);
                Log.v("OppoAppStartupManager", "handleStartOrBindService callerApp.processName == " + callerApp.processName);
            }
            if (!(!inSeviceCpnlist(cpnClassName) || callerApp.processName == null || (callerApp.processName.contains(cpnPkgName) ^ 1) == 0)) {
                if (this.mDebugSwitch) {
                    Log.v("OppoAppStartupManager", "handleStartOrBindService return undo!");
                }
                result = true;
                if (this.mSwitchMonitor) {
                    this.mHandler.post(new CollectBlackListInterceptRunnable(cpnPkgName, cpnClassName, "service"));
                }
            }
        } else {
            if (this.mDynamicDebug) {
                Log.v("OppoAppStartupManager", "handleStartOrBindService cpn = null!");
            }
            String action = service.getAction();
            if (action != null) {
                if (this.mDynamicDebug) {
                    Log.v("OppoAppStartupManager", "handleStartOrBindService action == " + action);
                }
                String pkgName = service.getPackage();
                if (pkgName == null) {
                    if (this.mDebugSwitch) {
                        Log.v("OppoAppStartupManager", "handleStartOrBindService component = " + service.getComponent());
                    }
                    pkgName = "unknow";
                }
                if (this.mDynamicDebug) {
                    Log.v("OppoAppStartupManager", "handleStartOrBindService pkgName == " + pkgName);
                }
                if (inActionlist(action) && pkgName != null && callerApp.processName != null && (callerApp.processName.contains(pkgName) ^ 1) != 0) {
                    if (this.mDebugSwitch) {
                        Log.v("OppoAppStartupManager", "handleStartOrBindService return undo!");
                    }
                    result = true;
                    if (this.mSwitchMonitor) {
                        this.mHandler.post(new CollectBlackListInterceptRunnable(pkgName, action, "service"));
                    }
                } else if (inBlackguardList(action)) {
                    if (this.mDebugSwitch) {
                        Log.v("OppoAppStartupManager", "handleStartOrBindService inBlackguardList return undo!");
                    }
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean handleStartProvider(ContentProviderRecord cpr, ProcessRecord callerApp) {
        boolean result = false;
        if (!this.mSwitch || cpr == null) {
            return false;
        }
        if (callerApp == null || callerApp.uid > 10000) {
            ComponentName cpn = cpr.name;
            if (cpn != null) {
                String cpnPkgName = cpn.getPackageName();
                String cpnClassName = cpn.getClassName();
                if (OppoListManager.getInstance().isAppStartForbidden(cpnPkgName)) {
                    if (this.mSwitchMonitor) {
                        this.mHandler.post(new CollectAppInterceptRunnable("noNeed", cpnPkgName, "noNeed", "provider"));
                    }
                    Log.v("OppoAppStartupManager", "handleStartProvider: " + cpnPkgName + " is forbidden to start");
                    result = true;
                }
                if (callerApp == null && inProviderlist(cpnClassName)) {
                    if (this.mDebugSwitch) {
                        Log.v("OppoAppStartupManager", "handleStartProvider return undo!");
                    }
                    result = true;
                } else if (!(callerApp == null || !inProviderlist(cpnClassName) || callerApp.processName == null || (callerApp.processName.contains(cpnPkgName) ^ 1) == 0)) {
                    if (this.mDebugSwitch) {
                        Log.v("OppoAppStartupManager", "handleStartProvider return undo!");
                    }
                    result = true;
                    if (this.mSwitchMonitor) {
                        this.mHandler.post(new CollectBlackListInterceptRunnable(cpnPkgName, cpnClassName, "provider"));
                    }
                }
            }
            return result;
        }
        if (this.mDynamicDebug) {
            Log.v("OppoAppStartupManager", "handleStartProvider callerApp.uid <= 10000 return");
            Log.v("OppoAppStartupManager", "handleStartProvider callerApp: " + callerApp);
        }
        if (this.mHypnus != null) {
            this.mHypnus.hypnusSetAction(12, 1000);
        }
        return false;
    }

    public boolean handleSpecialBroadcast(Intent intent, ProcessRecord callerApp, String packageName) {
        if (packageName == null || !OppoListManager.getInstance().isAppStartForbidden(packageName)) {
            boolean result = false;
            if (!this.mSwitch) {
                return false;
            }
            if (intent == null || callerApp == null) {
                if (this.mDynamicDebug) {
                    Log.v("OppoAppStartupManager", "intent == null || callerApp == null");
                }
                return false;
            }
            if (this.mDynamicDebug) {
                Log.v("OppoAppStartupManager", "handleSpecialBroadcast: " + intent + " args=" + intent.getExtras());
                Log.v("OppoAppStartupManager", "handleSpecialBroadcast callerApp: " + callerApp);
            }
            if (callerApp.uid <= 10000) {
                if (this.mDynamicDebug) {
                    Log.v("OppoAppStartupManager", "handleSpecialBroadcast callerApp.uid <= 10000 return");
                    Log.v("OppoAppStartupManager", "handleSpecialBroadcast callerApp: " + callerApp);
                }
                return false;
            }
            ComponentName cpn = intent.getComponent();
            if (cpn != null) {
                String cpnPkgName = cpn.getPackageName();
                String cpnClassName = cpn.getClassName();
                if (this.mDynamicDebug) {
                    Log.v("OppoAppStartupManager", "handleSpecialBroadcast cpnPkgName == " + cpnPkgName);
                    Log.v("OppoAppStartupManager", "handleSpecialBroadcast cpnClassName == " + cpnClassName);
                    Log.v("OppoAppStartupManager", "handleSpecialBroadcast callerApp.processName == " + callerApp.processName);
                }
                if (!(!inReceiverlist(cpnClassName) || callerApp.processName == null || (callerApp.processName.contains(cpnPkgName) ^ 1) == 0)) {
                    if (this.mDebugSwitch) {
                        Log.v("OppoAppStartupManager", "handleSpecialBroadcast return skip!");
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
                        Log.v("OppoAppStartupManager", "handleSpecialBroadcast pkgName == " + pkgName);
                    }
                    if (!(!inReceiverActionlist(action) || pkgName == null || callerApp.processName == null || (callerApp.processName.contains(pkgName) ^ 1) == 0)) {
                        if (this.mDebugSwitch) {
                            Log.v("OppoAppStartupManager", "handleSpecialBroadcast return undo!");
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
        Log.i("OppoAppStartupManager", packageName + " is forbidden to start by broadcast");
        return true;
    }

    public boolean handleStartActivity(Intent intent, ActivityInfo aInfo, String callingPackage, int callingUid, int callingPid, String reason, int userId) {
        if (aInfo == null || aInfo.packageName == null) {
            if (this.mDebugSwitch) {
                Log.v("OppoAppStartupManager", "handleStartActivity aInfo == null || aInfo.packageName == null return.");
            }
            return false;
        }
        String calledPackageName = aInfo.packageName;
        if (calledPackageName == null || !OppoListManager.getInstance().isAppStartForbidden(calledPackageName)) {
            String cpnClassName = aInfo.name;
            if (OppoListManager.getInstance().getPreventRedundentStartSwitch() && cpnClassName != null && OppoListManager.getInstance().isRedundentActivity(cpnClassName)) {
                Log.v("OppoAppStartupManager", "handleStartActivity redundent activity " + cpnClassName);
                return true;
            } else if (!this.mSwitch) {
                return false;
            } else {
                if (isExpVersion()) {
                    return false;
                }
                if (this.mDynamicDebug) {
                    Log.v("OppoAppStartupManager", "handleStartActivity cpnPkgName = " + calledPackageName);
                    Log.v("OppoAppStartupManager", "handleStartActivity cpnClassName = " + cpnClassName);
                    Log.v("OppoAppStartupManager", "handleStartActivity callerPkgName = " + callingPackage);
                }
                if (this.mSwitchMonitor || OppoAppStartupManagerUtils.getInstance().getSwitchStatus()) {
                    this.mHandler.post(new CollectGamePayRunnable(callingPackage, calledPackageName, cpnClassName, intent));
                }
                if (callingPackage == null) {
                    String[] pkgList = null;
                    try {
                        if (this.mAms != null) {
                            pkgList = this.mAms.mContext.getPackageManager().getPackagesForUid(callingUid);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (pkgList == null || pkgList.length != 1) {
                        callingPackage = "empty";
                    } else {
                        callingPackage = pkgList[0];
                    }
                    if (this.mDynamicDebug) {
                        Log.v("OppoAppStartupManager", "handleStartActivity callingPackage is empty, " + callingPackage);
                    }
                } else if (callingUid > 0 && callingUid <= 10000) {
                    if (this.mDebugSwitch) {
                        Log.v("OppoAppStartupManager", "handleStartActivity callerApp is systemapp return.");
                        Log.v("OppoAppStartupManager", "handleStartActivity callerApp: " + callingPackage);
                    }
                    collectLastCallActivityInfo(calledPackageName, cpnClassName);
                    String str = callingPackage;
                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(str, calledPackageName, cpnClassName, "noNeed", "noNeed", composePackage("allow_system", String.valueOf(callingUid)));
                    return false;
                } else if (ActivityManagerService.OPPO_LAUNCHER.equals(callingPackage)) {
                    if (this.mDebugSwitch) {
                        Log.v("OppoAppStartupManager", "handleStartActivity callerApp is oppohome return.");
                    }
                    collectLastCallActivityInfo(calledPackageName, cpnClassName);
                    return false;
                } else if (!(!inActivityPushBlacklist(cpnClassName) || callingPackage == null || (callingPackage.contains(calledPackageName) ^ 1) == 0)) {
                    if (this.mDebugSwitch) {
                        Log.v("OppoAppStartupManager", "handleStartActivity return undo!");
                    }
                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callingPackage, calledPackageName, cpnClassName, "noNeed", "noNeed", "forbid_push");
                    return true;
                }
                int interceptSwitchValue = OppoAppStartupManagerUtils.getInstance().getTencentInterceptSwitchValue();
                if (!(interceptSwitchValue <= 0 || this.mAms == null || callingPackage == null || calledPackageName == null || cpnClassName == null || intent == null)) {
                    if (callingPackage.equals(TENCENT_MM_PKG) && calledPackageName.equals(TENCENT_MM_PKG) && cpnClassName.equals(OppoAppStartupManagerUtils.getInstance().getTencentInterceptCpn())) {
                        if (this.mDynamicDebug) {
                            Bundle bundle = intent.getExtras();
                            String string = "Bundle{";
                            for (String key : bundle.keySet()) {
                                string = string + " " + key + " => " + bundle.get(key) + ";";
                            }
                            Log.v("OppoAppStartupManager", "handleStartActivity Bundle = " + (string + " }Bundle"));
                        }
                        ActivityStack foucedStack = this.mAms.getFocusedStack();
                        if (foucedStack != null) {
                            ArrayList<ActivityRecord> list = foucedStack.mLRUActivities;
                            if (list.size() > 0) {
                                this.mPreRecord = (ActivityRecord) list.get(list.size() - 1);
                                if (!(this.mPreRecord == null || this.mPreRecord.intent == null)) {
                                    if (this.mPreRecord.intent.getComponent().flattenToShortString().contains(TENCENT_MM_PKG)) {
                                        this.mActivityStack = foucedStack;
                                        intent.setOppoUserId(userId);
                                        this.mHandler.post(new SendBroadCastToMarket(this.mAms.mContext, intent, interceptSwitchValue));
                                        return true;
                                    }
                                    this.mPreRecord = null;
                                }
                            }
                        }
                    }
                }
                if (checkAbnormalActivityCall(intent, callingPackage, calledPackageName, cpnClassName, callingPid, callingUid, reason)) {
                    if (this.mDebugSwitch) {
                        Log.v("OppoAppStartupManager", "handleStartActivity return checkAbnormalActivityCall undo!");
                    }
                    updateLaunchRecord(callingPackage, calledPackageName, RECORD_ACTIVITY_LAUNCH_MODE, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, TYPE_ACTIVITY);
                    return true;
                }
                collectLastCallActivityInfo(calledPackageName, cpnClassName);
                return false;
            }
        }
        Log.v("OppoAppStartupManager", "handleStartActivity(pkg): " + calledPackageName + " is forbidden to start");
        if (callingPackage != null) {
            if (callingPackage.equals(ActivityManagerService.OPPO_LAUNCHER)) {
                Log.d("OppoAppStartupManager", calledPackageName + " is started from launcher: " + callingPackage);
                Message msg = Message.obtain();
                msg.what = 600;
                msg.obj = calledPackageName;
                this.mAms.mUiHandler.sendMessage(msg);
                if (this.mSwitchMonitor) {
                    this.mHandler.post(new CollectAppInterceptRunnable("noNeed", calledPackageName, "noNeed", TYPE_DIALOG));
                }
                return true;
            }
        }
        if (this.mSwitchMonitor) {
            this.mHandler.post(new CollectAppInterceptRunnable("noNeed", calledPackageName, "noNeed", TYPE_ACTIVITY));
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:184:0x04ed, code:
            if (r27.contains("startActivityFromRecents") != false) goto L_0x04ef;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkAbnormalActivityCall(Intent intent, String callerPkg, String calledPkg, String cpnName, int callingPid, int callingUid, String reason) {
        if (!this.mSwitchInterceptActivity) {
            return false;
        }
        if (this.mAms == null || callerPkg == null || calledPkg == null || cpnName == null) {
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", "checkAbnormalActivityCall callerPkg == null ||calledPkg == null ||cpnName == null return.");
            }
            return false;
        }
        long time = 0;
        if (this.mDynamicDebug) {
            time = SystemClock.elapsedRealtime();
        }
        boolean isScreeOn = isScreenOn();
        ComponentName topCpn = getTopComponentName();
        if (topCpn == null) {
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", "checkAbnormalActivityCall topCpn = null return.");
            }
            return false;
        }
        String topPkg = topCpn.getPackageName() != null ? topCpn.getPackageName() : "";
        if (inBlackguardActivityList(cpnName)) {
            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, "noNeed", "noNeed", "forbid_black");
            Log.v("OppoAppStartupManager", "InterceptInfo " + callerPkg + "  " + calledPkg + "  " + cpnName + "  " + topPkg + "  " + isScreeOn);
            return true;
        } else if (inActivityCallerWhitePkgList(callerPkg) || inActivityCalledWhitePkgList(calledPkg) || inActivityCalledWhiteCpnList(cpnName) || inCustomizeWhiteList(callerPkg) || inGlobalWhiteList(callerPkg)) {
            if (this.mSwitchMonitor) {
                if (TENCENT_MM_PKG.equals(callerPkg) || TENCENT_MM_PKG.equals(calledPkg)) {
                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(TENCENT_MM_PKG, TENCENT_MM_PKG, TENCENT_MM_PKG, TENCENT_MM_PKG, String.valueOf(isScreeOn), "allow_mm_qq");
                } else if ("com.tencent.mobileqq".equals(callerPkg) || "com.tencent.mobileqq".equals(calledPkg)) {
                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo("com.tencent.mobileqq", "com.tencent.mobileqq", "com.tencent.mobileqq", "com.tencent.mobileqq", String.valueOf(isScreeOn), "allow_mm_qq");
                } else {
                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_white");
                }
            }
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", "checkAbnormalActivityCall callerPkg or calledPkg or cpnName in whitelist return.");
            }
            return false;
        } else if (inActivityPkgKeyList(callerPkg) || inActivityPkgKeyList(calledPkg)) {
            if (this.mSwitchMonitor) {
                if (!callerPkg.startsWith("com.oppo.")) {
                    if (!calledPkg.startsWith("com.oppo.")) {
                        if (!callerPkg.startsWith("com.coloros.")) {
                            if (!calledPkg.startsWith("com.coloros.")) {
                                if (!callerPkg.startsWith("com.nearme.")) {
                                    if (!calledPkg.startsWith("com.nearme.")) {
                                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_pkg_key");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", "checkAbnormalActivityCall callerPkg or calledPkg in whitelist return.");
            }
            return false;
        } else if (this.mAms.mContext.getPackageManager().isClosedSuperFirewall()) {
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", "checkAbnormalActivityCall callerPkg in cts whitelist return.");
            }
            return false;
        } else if (isScreeOn && topPkg.equals(callerPkg)) {
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", callerPkg + " callerPkg is FG!");
            }
            return false;
        } else if ("android".equals(topPkg)) {
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", callerPkg + " topPkg is android!");
            }
            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_topIsAndroid");
            return false;
        } else if ("empty".equals(callerPkg)) {
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", "checkAbnormalActivityCall callerPkg is empty return.");
            }
            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsEmpty");
            return false;
        } else if (isScreeOn && (TextUtils.isEmpty(reason) ^ 1) != 0 && this.mStartActivityReasonList.contains(reason)) {
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", "checkAbnormalActivityCall " + reason + " return.");
            }
            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), reason);
            return false;
        } else {
            if (topPkg.equals("com.coloros.safecenter") && topCpn.getClassName() != null && topCpn.getClassName().contains("AppUnlock")) {
                if (this.mDebugSwitch) {
                    Log.d("OppoAppStartupManager", "checkAbnormalActivityCall safecenter applock return.");
                }
                OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_topIsApplock");
                return false;
            } else if (OppoListManager.getInstance().isSystemApp(callerPkg)) {
                if (this.mDebugSwitch) {
                    Log.d("OppoAppStartupManager", callerPkg + " callerPkg is system app! return.");
                }
                OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsSystem");
                return false;
            } else {
                if (OppoListManager.getInstance().isSystemApp(calledPkg)) {
                    if ((calledPkg.equals("com.android.browser") ^ 1) != 0) {
                        if (this.mDebugSwitch) {
                            Log.d("OppoAppStartupManager", calledPkg + " calledPkg is system app! return.");
                        }
                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsSystem");
                        return false;
                    }
                }
                if (callerPkg != null && callerPkg.contains(calledPkg)) {
                    String lowerCaseCpnName = cpnName.toLowerCase();
                    for (String key : this.mActivityCalledKeyList) {
                        if (lowerCaseCpnName.contains(key)) {
                            if (this.mDebugSwitch) {
                                Log.d("OppoAppStartupManager", "checkAbnormalActivityCall callingPackage contain key! return.");
                            }
                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledContainsKey");
                            return false;
                        }
                    }
                    if (isScreeOn) {
                        if (isSameAsLastActivity(calledPkg)) {
                            if (this.mDebugSwitch) {
                                Log.v("OppoAppStartupManager", "checkAbnormalActivityCall return for special app from launcher! return.");
                            }
                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledSameLast");
                            return false;
                        } else if (isInputMethodApplication(calledPkg)) {
                            if (this.mDebugSwitch) {
                                Log.v("OppoAppStartupManager", "checkAbnormalActivityCall return for isInputMethod! return.");
                            }
                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsIm");
                            return false;
                        } else {
                            String str = Log.getStackTraceString(new Throwable());
                            if (this.mDynamicDebug) {
                                Log.d("OppoAppStartupManager", str);
                            }
                            if (!str.contains("PendingIntentRecord")) {
                            }
                            if (this.mDebugSwitch) {
                                Log.d("OppoAppStartupManager", "checkAbnormalActivityCall by PendingIntentRecord or startActivityFromRecents! return.");
                            }
                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_pendingOrRecent");
                            return false;
                        }
                    }
                }
                if (isScreeOn) {
                    if (callerPkg.equals(getFocusedPkg())) {
                        if (this.mDebugSwitch) {
                            Log.d("OppoAppStartupManager", callerPkg + " callerPkg is focused! return.");
                        }
                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_focusIsCaller");
                        return false;
                    } else if (isFromControlCenterPkg(callerPkg)) {
                        if (this.mDebugSwitch) {
                            Log.d("OppoAppStartupManager", callerPkg + " from control center! return.");
                        }
                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsControlCenter");
                        return false;
                    } else if (OppoListManager.getInstance().isInstalledAppWidget(callerPkg)) {
                        if (this.mDebugSwitch) {
                            Log.d("OppoAppStartupManager", calledPkg + " callerPkg has widget! return.");
                        }
                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsWidget");
                        return false;
                    }
                }
                boolean isAllowFloat = OppoSafeDbReader.getInstance(this.mAms.mContext).isUserOpen(callerPkg);
                if (this.mDynamicDebug) {
                    Log.d("OppoAppStartupManager", callerPkg + "  debug for floatwindow app isUserOpen = " + isAllowFloat);
                }
                if (isAllowFloat) {
                    if (this.mDebugSwitch) {
                        Log.d("OppoAppStartupManager", calledPkg + " calledPkg is allowed keyguard app! return.");
                    }
                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsFloat");
                    return false;
                } else if (OppoListManager.getInstance().isFromNotifyPkg(callerPkg)) {
                    if (this.mDebugSwitch) {
                        Log.d("OppoAppStartupManager", calledPkg + " calledPkg is from notify! return.");
                    }
                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsNotify");
                    return false;
                } else if (OppoListManager.getInstance().isInAutoBootWhiteList(callerPkg)) {
                    if (this.mDebugSwitch) {
                        Log.d("OppoAppStartupManager", calledPkg + " calledPkg is allowed autoboot app! return.");
                    }
                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsBootWhite");
                    return false;
                } else {
                    if (callerPkg != null && callerPkg.contains(calledPkg)) {
                        String[] mTrackPids = getActiveAudioPids();
                        if (mTrackPids != null) {
                            List<ProcessRecord> apps = getProcessForUidLocked(callingUid);
                            int i = 0;
                            while (i < mTrackPids.length) {
                                if (!mTrackPids[i].isEmpty()) {
                                    for (ProcessRecord procApp : apps) {
                                        if (procApp != null && procApp.pid == Integer.parseInt(mTrackPids[i])) {
                                            if (this.mDebugSwitch) {
                                                Log.v("OppoAppStartupManager", "checkAbnormalActivityCall return for audio! return.");
                                            }
                                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsPlaying");
                                            return false;
                                        }
                                    }
                                    continue;
                                }
                                i++;
                            }
                        }
                    }
                    if (checkAccessibilityPkg(calledPkg)) {
                        if (this.mDebugSwitch) {
                            Log.v("OppoAppStartupManager", "checkAbnormalActivityCall return for accessibility! return.");
                        }
                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsAccessibility");
                        return false;
                    }
                    String action = intent.getAction();
                    String url = intent.getDataString();
                    if (!(action == null || url == null || !action.equals("android.intent.action.VIEW") || !url.toLowerCase().startsWith("http") || (OppoListManager.getInstance().isInBrowserWhiteList(callerPkg) ^ 1) == 0)) {
                        if (calledPkg.equals("com.android.browser") && calledPkg.equals(callerPkg)) {
                            if (this.mDynamicDebug) {
                                Log.d("OppoAppStartupManager", callerPkg + "is OPPO browser and it's calling itself, return");
                            }
                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsBrowser");
                            return false;
                        } else if (isInputMethodApplication(callerPkg)) {
                            if (this.mDynamicDebug) {
                                Log.d("OppoAppStartupManager", callerPkg + " is starting a web view, we don't intercept it.");
                            }
                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsBrowser_Im");
                            return false;
                        } else {
                            sendBroadcastForUrlIntercept(callerPkg, url, calledPkg, cpnName);
                            Log.v("OppoAppStartupManager", "Sending Broadcast for url intercept. third browser is called in Bg, undo!");
                        }
                    }
                    if (action != null && url != null && action.equals("android.intent.action.VIEW") && url.toLowerCase().startsWith("http") && OppoListManager.getInstance().isInBrowserWhiteList(callerPkg)) {
                        if (this.mDebugSwitch) {
                            Log.d("OppoAppStartupManager", callerPkg + " with action= " + action + " is no restric to start a web view");
                        }
                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsBrowserWhite");
                        return false;
                    }
                    ComponentName dockTopCpn = this.mAms.getDockTopAppName();
                    if (!isScreeOn || dockTopCpn == null) {
                        if (this.mDynamicDebug) {
                            Log.d("OppoAppStartupManager", "handleStartAppInfo cost time ==  " + (SystemClock.elapsedRealtime() - time));
                        }
                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "forbid_popup");
                        Log.i("OppoAppStartupManager", "InterceptInfo " + callerPkg + "  " + calledPkg + "  " + cpnName + "  " + topPkg + "  " + isScreeOn);
                        return true;
                    }
                    if (this.mDebugSwitch) {
                        Log.d("OppoAppStartupManager", callerPkg + ", dockTopPkg " + (dockTopCpn.getPackageName() != null ? dockTopCpn.getPackageName() : ""));
                    }
                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_topIsSplitScreen");
                    return false;
                }
            }
        }
    }

    private void initStartActivityReason() {
        this.mStartActivityReasonList.add("startActivityInPackage");
    }

    private void sendBroadcastForUrlIntercept(String callingPkg, String url, String pkgName, String className) {
        if (this.mSwitchBrowserInterceptUpload) {
            Log.d("OppoAppStartupManager", "sendBroadcastForUrlIntercept callingPkg = " + callingPkg + "  url = " + url + "  pkgName = " + pkgName + "  className= " + className);
            Intent intent = new Intent(OPPO_SAFE_URL_INTERCEPTION);
            intent.putExtra("caller", callingPkg);
            intent.putExtra("url", url);
            intent.putExtra("pkgName", pkgName);
            intent.putExtra("className", className);
            if (this.mAms != null) {
                this.mAms.mContext.sendBroadcast(intent);
            }
        }
    }

    private boolean isSameAsLastActivity(String calledPkg) {
        try {
            String lastPkg = ((OppoCallActivityEntry) this.mLastCalledAcivityList.get(this.mLastCalledAcivityList.size() - 1)).mPkgName;
            if (lastPkg == null || !lastPkg.contains(calledPkg)) {
                return false;
            }
            if (this.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "isSameAsLastActivity lastPkg = " + lastPkg);
            }
            return true;
        } catch (Exception e) {
            Log.d("OppoAppStartupManager", "isSameAsLastActivity exeption happen!");
            e.printStackTrace();
            return true;
        }
    }

    private boolean checkAccessibilityPkg(String callerPkg) {
        String accessStr = Secure.getString(this.mAms.mContext.getContentResolver(), "enabled_accessibility_services");
        if (accessStr == null || !accessStr.contains(callerPkg)) {
            return false;
        }
        if (this.mDebugSwitch) {
            Log.v("OppoAppStartupManager", "checkAccessibilityPkg callerPkg contain!");
        }
        return true;
    }

    private boolean isInputMethodApplication(String calledPkg) {
        if (this.mDefaultInputMethod == null || calledPkg == null || !this.mDefaultInputMethod.equals(calledPkg)) {
            return false;
        }
        return true;
    }

    private String getDefaultInputMethod() {
        String defaultInput = null;
        if (this.mAms != null) {
            try {
                String inputMethod = Secure.getString(this.mAms.mContext.getContentResolver(), "default_input_method");
                if (inputMethod != null) {
                    defaultInput = inputMethod.substring(0, inputMethod.indexOf("/"));
                }
            } catch (Exception e) {
                Log.e("OppoAppStartupManager", "Failed to get default input method");
            }
        }
        if (this.mDebugSwitch) {
            Log.i("OppoAppStartupManager", "defaultInputMethod " + defaultInput);
        }
        return defaultInput;
    }

    private String getFocusedPkg() {
        String focusedPkg = null;
        if (!(this.mAms == null || this.mAms.mWindowManager == null)) {
            focusedPkg = this.mAms.mWindowManager.getFocusedWindowPkg();
        }
        if (this.mDynamicDebug) {
            Log.v("OppoAppStartupManager", "isFocusedActivity == " + focusedPkg);
        }
        return focusedPkg != null ? focusedPkg : "";
    }

    private String[] getActiveAudioPids() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) this.mAms.mContext.getSystemService("audio");
        }
        if (this.mAudioManager == null) {
            return null;
        }
        return getActiveAudioPids(this.mAudioManager.getParameters("get_pid"));
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String[] getActiveAudioPids(String pids) {
        if (pids == null || pids.length() == 0 || !pids.contains(":")) {
            return null;
        }
        return pids.split(":");
    }

    private ArrayList<ProcessRecord> getProcessForUidLocked(int uid) {
        ArrayList<ProcessRecord> res = new ArrayList();
        try {
            for (int i = this.mAms.mLruProcesses.size() - 1; i >= 0; i--) {
                ProcessRecord rec = (ProcessRecord) this.mAms.mLruProcesses.get(i);
                if (rec.thread != null && rec.uid == uid) {
                    res.add(rec);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    protected boolean isInLruProcessesLocked(int uid) {
        try {
            for (int i = this.mAms.mLruProcesses.size() - 1; i >= 0; i--) {
                ProcessRecord rec = (ProcessRecord) this.mAms.mLruProcesses.get(i);
                if (rec.thread != null && rec.uid == uid) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkAppExist(String pkg) {
        boolean result = false;
        if (this.mAms == null || pkg == null || pkg.isEmpty()) {
            return false;
        }
        synchronized (this.mAms) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                for (int i = this.mAms.mLruProcesses.size() - 1; i >= 0; i--) {
                    ProcessRecord proc = (ProcessRecord) this.mAms.mLruProcesses.get(i);
                    if (proc != null && proc.thread != null && proc.processName != null && proc.processName.startsWith(pkg)) {
                        result = true;
                        break;
                    }
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
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
            Log.d("OppoAppStartupManager", "inSeviceCpnlist result = " + result);
        }
        return result;
    }

    public boolean inReceiverlist(String cpnClassName) {
        boolean result;
        synchronized (this.mReceiverLock) {
            result = this.mReceiverBlackList.contains(cpnClassName);
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", "inReceiverlist result = " + result);
        }
        return result;
    }

    public boolean inReceiverActionlist(String action) {
        boolean result;
        synchronized (this.mReceiverActionLock) {
            result = this.mReceiverActionBlackList.contains(action);
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", "inReceiverActionlist result = " + result);
        }
        return result;
    }

    public boolean inProviderlist(String cpnClassName) {
        boolean result;
        synchronized (this.mProviderLock) {
            result = this.mProviderBlacklist.contains(cpnClassName);
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", "inProviderlist result = " + result);
        }
        return result;
    }

    public boolean inActivityPushBlacklist(String cpnClassName) {
        boolean result;
        synchronized (this.mActivityLock) {
            result = this.mActivityBlackList.contains(cpnClassName);
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", "inActivityPushBlacklist result = " + result);
        }
        return result;
    }

    public boolean inActionlist(String action) {
        boolean result;
        synchronized (this.mActionLock) {
            result = this.mActionBlackList.contains(action);
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", "inActionlist result = " + result);
        }
        return result;
    }

    public boolean inBlackguardList(String action) {
        boolean result;
        synchronized (this.mBlackguardLock) {
            result = this.mBlackguardList.contains(action);
        }
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "inBlackguardList result = " + result);
        }
        return result;
    }

    public boolean inActivityCallerWhitePkgList(String pkgName) {
        boolean result;
        synchronized (this.mActivityCallerWhitePkgLock) {
            result = this.mActivityCallerWhitePkgList.contains(pkgName);
        }
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "inActivityCallerWhitePkgList result = " + result);
        }
        return result;
    }

    public boolean inActivityCalledWhitePkgList(String pkgName) {
        boolean result;
        synchronized (this.mActivityCalledWhitePkgLock) {
            result = this.mActivityCalledWhitePkgList.contains(pkgName);
        }
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "inActivityCalledWhitePkgList result = " + result);
        }
        return result;
    }

    public boolean inActivityCalledWhiteCpnList(String pkgName) {
        boolean result;
        synchronized (this.mActivityCalledWhiteCpnLock) {
            result = this.mActivityCalledWhiteCpnList.contains(pkgName);
        }
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "inActivityCalledWhiteCpnList result = " + result);
        }
        return result;
    }

    public boolean inActivityPkgKeyList(String pkgName) {
        boolean result = false;
        synchronized (this.mActivityPkgKeyLock) {
            for (String pkgKey : this.mActivityPkgKeyList) {
                if (pkgName.contains(pkgKey)) {
                    result = true;
                    break;
                }
            }
        }
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "inActivityPkgKeyList result = " + result);
        }
        return result;
    }

    public boolean inActivityCalledKeyList(String pkgName) {
        boolean result;
        synchronized (this.mActivityCalledKeyLock) {
            result = this.mActivityCalledKeyList.contains(pkgName);
        }
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "inActivityCalledKeyList result = " + result);
        }
        return result;
    }

    public boolean inBlackguardActivityList(String activityCpn) {
        boolean result;
        synchronized (this.mBlackguardActivityLock) {
            result = this.mBlackguardActivityList.contains(activityCpn);
        }
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "inBlackguardActivityList result = " + result);
        }
        return result;
    }

    public boolean inCustomizeWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mCustomizeWhiteLock) {
            result = this.mCustomizeWhiteList.contains(pkgName);
        }
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "inCustomizeWhiteList result = " + result);
        }
        return result;
    }

    public boolean inBuildBlackList(String pkgName) {
        boolean result;
        synchronized (this.mBuildAppBlackLock) {
            result = this.mBuildAppBlackList.contains(pkgName);
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", pkgName + " inBuildBlackList result = " + result);
        }
        return result;
    }

    public boolean inStartServiceWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mStartServiceWhiteLock) {
            result = this.mStartServiceWhiteList.contains(pkgName);
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", pkgName + " inStartServiceWhiteList result = " + result);
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
                Log.d("OppoAppStartupManager", s.name.getClassName() + " mStartServiceWhiteCpnList result = " + result);
            }
        }
        return result;
    }

    public boolean inBindServiceCpnWhiteList(ServiceRecord s) {
        boolean result = false;
        if (s.name != null) {
            synchronized (this.mBindServiceWhiteLock) {
                if (this.mBindServiceCpnWhiteList.isEmpty()) {
                    result = OppoAppStartupListManager.getInstance().isInBindServiceCpnList(s.name.getClassName());
                } else {
                    result = this.mBindServiceCpnWhiteList.contains(s.name.getClassName());
                }
            }
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", s.appInfo.packageName + " inBindServiceCpnWhiteList result = " + result);
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
            Log.d("OppoAppStartupManager", pkgName + " inJobWhiteList result = " + result);
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
            Log.d("OppoAppStartupManager", pkgName + " inSyncWhiteList result = " + result);
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
            Log.d("OppoAppStartupManager", pkgName + " inNotificationWhiteList result = " + result);
        }
        return result;
    }

    public boolean inProviderCpnWhiteList(ComponentName cpn) {
        boolean result = false;
        if (cpn != null) {
            synchronized (this.mProviderWhiteLock) {
                if (this.mProviderCpnWhiteList.isEmpty()) {
                    result = OppoAppStartupListManager.getInstance().isInProviderCpnList(cpn.getClassName());
                } else {
                    result = this.mProviderCpnWhiteList.contains(cpn.getClassName());
                }
            }
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", cpn.getPackageName() + " inProviderCpnWhiteList result = " + result);
        }
        return result;
    }

    public boolean inBroadCastWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mBroadcastWhiteLock) {
            result = this.mBroadcastWhiteList.contains(pkgName);
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", pkgName + " inBroadCastWhiteList result = " + result);
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
                Log.d("OppoAppStartupManager", intent.getAction() + " inBroadCastActionWhiteList result = " + result);
            }
        }
        return result;
    }

    public boolean inProtectWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mProtectLock) {
            if (this.mProtectList.isEmpty()) {
                result = OppoAppStartupListManager.getInstance().isInProtectWhiteList(pkgName);
            } else {
                result = this.mProtectList.contains(pkgName);
            }
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", pkgName + " inProtectWhiteList result = " + result);
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
            Log.d("OppoAppStartupManager", pkgName + " mCollectAppStartList result = " + result);
        }
        return result;
    }

    public void updateConfiglist() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateConfiglist!");
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
        updateAuthorizeCpnList();
        updatePayCpnList();
        if (!this.mAms.mContext.getPackageManager().hasSystemFeature("oppo.customize.auto.start.disabled")) {
            this.mSwitchInterceptActivity = OppoAppStartupManagerUtils.getInstance().getSwitchInterceptActivity();
        }
        this.mSwitchBrowserInterceptUpload = OppoAppStartupManagerUtils.getInstance().getSwitchBrowserInterceptUpload();
        clearStartManagerList();
    }

    public void updateMonitorlist() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateMonitorlist!");
        }
        updateMonitorAppStartList();
        clearMonitorList();
    }

    protected void updateAssociateStartList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateAssociateStartWhiteList!");
        }
        updateAssociateStartWhiteList();
        OppoAppStartupManagerUtils.getInstance().cleanAssociateStartWhiteList();
    }

    private void clearStartManagerList() {
        OppoAppStartupManagerUtils.getInstance().cleanConfigList();
    }

    private void clearMonitorList() {
        OppoAppStartupManagerUtils.getInstance().cleanMonitorList();
    }

    private void updateSeviceCpnBlacklist() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateSeviceCpnBlacklist!");
        }
        synchronized (this.mServiceLock) {
            this.mSeviceCpnBlacklist.clear();
            this.mSeviceCpnBlacklist.addAll(OppoAppStartupManagerUtils.getInstance().getSeviceCpnBlacklist());
        }
    }

    private void updateReceiverBlackList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateReceiverBlackList!");
        }
        synchronized (this.mReceiverLock) {
            this.mReceiverBlackList.clear();
            this.mReceiverBlackList.addAll(OppoAppStartupManagerUtils.getInstance().getReceiverBlackList());
        }
    }

    private void updateReceiverActionBlackList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateReceiverActionBlackList!");
        }
        synchronized (this.mReceiverActionLock) {
            this.mReceiverActionBlackList.clear();
            this.mReceiverActionBlackList.addAll(OppoAppStartupManagerUtils.getInstance().getReceiverActionBlackList());
        }
    }

    private void updateProviderBlackList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateProviderBlackList!");
        }
        synchronized (this.mProviderLock) {
            this.mProviderBlacklist.clear();
            this.mProviderBlacklist.addAll(OppoAppStartupManagerUtils.getInstance().getProviderBlackList());
        }
    }

    private void updateActivityBlackList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateActivityBlackList!");
        }
        synchronized (this.mActivityLock) {
            this.mActivityBlackList.clear();
            this.mActivityBlackList.addAll(OppoAppStartupManagerUtils.getInstance().getActivityBlackList());
        }
    }

    private void updateActionBlackList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateActionBlackList!");
        }
        synchronized (this.mActionLock) {
            this.mActionBlackList.clear();
            this.mActionBlackList.addAll(OppoAppStartupManagerUtils.getInstance().getActionBlackList());
        }
    }

    private void updateBlackguardList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateBlackguardList!");
        }
        synchronized (this.mBlackguardLock) {
            this.mBlackguardList.clear();
            this.mBlackguardList.addAll(OppoAppStartupManagerUtils.getInstance().getBlackguardList());
        }
    }

    private void updateActivityCallerWhitePkgList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateActivityCallerWhitePkgList!");
        }
        synchronized (this.mActivityCallerWhitePkgLock) {
            this.mActivityCallerWhitePkgList.clear();
            this.mActivityCallerWhitePkgList.addAll(OppoAppStartupManagerUtils.getInstance().getActivityCallerWhitePkgList());
        }
    }

    private void updateActivityCalledWhitePkgList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateActivityCalledWhitePkgList!");
        }
        synchronized (this.mActivityCalledWhitePkgLock) {
            this.mActivityCalledWhitePkgList.clear();
            this.mActivityCalledWhitePkgList.addAll(OppoAppStartupManagerUtils.getInstance().getActivityCalledWhitePkgList());
        }
    }

    private void updateActivityCalledWhiteCpnList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateActivityCalledWhiteCpnList!");
        }
        synchronized (this.mActivityCalledWhiteCpnLock) {
            this.mActivityCalledWhiteCpnList.clear();
            this.mActivityCalledWhiteCpnList.addAll(OppoAppStartupManagerUtils.getInstance().getActivityCalledWhiteCpnList());
        }
    }

    private void updateActivityPkgKeyList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateActivityPkgKeyList!");
        }
        synchronized (this.mActivityPkgKeyLock) {
            this.mActivityPkgKeyList.clear();
            this.mActivityPkgKeyList.addAll(OppoAppStartupManagerUtils.getInstance().getActivityPkgKeyList());
        }
    }

    private void updateActivityCalledKeyList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateActivityCalledKeyList!");
        }
        synchronized (this.mActivityCalledKeyLock) {
            this.mActivityCalledKeyList.clear();
            this.mActivityCalledKeyList.addAll(OppoAppStartupManagerUtils.getInstance().getActivityCalledKeyList());
        }
    }

    private void updateBlackguardActivityList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateBlackguardActivityList!");
        }
        synchronized (this.mBlackguardActivityLock) {
            this.mBlackguardActivityList.clear();
            this.mBlackguardActivityList.addAll(OppoAppStartupManagerUtils.getInstance().getBlackguardActivityList());
        }
    }

    private void updateCustomizeWhiteList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateCustomizeWhiteList!");
        }
        synchronized (this.mCustomizeWhiteLock) {
            this.mCustomizeWhiteList.clear();
            this.mCustomizeWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getCustomizeWhiteList());
        }
    }

    private void updateGlobalWhiteList() {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "updateGlobalWhiteList!");
        }
        this.mGlobalWhiteList = OppoListManager.getInstance().getGlobalWhiteList(this.mAms.mContext);
    }

    private void updateMonitorAppStartList() {
        synchronized (this.mCollectAppStartLock) {
            this.mCollectAppStartList.clear();
            this.mCollectAppStartList.addAll(OppoAppStartupManagerUtils.getInstance().getCollectAppStartList());
        }
    }

    private void updateBuildBlackList() {
        synchronized (this.mBuildAppBlackLock) {
            this.mBuildAppBlackList.clear();
            this.mBuildAppBlackList.addAll(OppoAppStartupManagerUtils.getInstance().getBuildBlackList());
        }
    }

    private void updateStartServiceWhiteList() {
        synchronized (this.mStartServiceWhiteLock) {
            this.mStartServiceWhiteList.clear();
            this.mStartServiceWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getStartServiceWhiteList());
        }
    }

    private void updateStartServiceWhiteCpnList() {
        synchronized (this.mStartServiceWhiteCpnLock) {
            this.mStartServiceWhiteCpnList.clear();
            this.mStartServiceWhiteCpnList.addAll(OppoAppStartupManagerUtils.getInstance().getStartServiceWhiteCpnList());
        }
    }

    private void updateBindServiceWhiteList() {
        synchronized (this.mBindServiceCpnWhiteList) {
            this.mBindServiceCpnWhiteList.clear();
            this.mBindServiceCpnWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getBindServiceWhiteList());
        }
    }

    private void updateJobWhiteList() {
        synchronized (this.mJobWhiteLock) {
            this.mJobWhiteList.clear();
            this.mJobWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getJobWhiteList());
        }
    }

    private void updateSyncWhiteList() {
        synchronized (this.mSyncWhiteLock) {
            this.mSyncWhiteList.clear();
            this.mSyncWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getSyncWhiteList());
        }
    }

    private void updateNotifyWhiteList() {
        synchronized (this.mNotifyWhiteLock) {
            this.mNotifyWhiteList.clear();
            this.mNotifyWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getNotifyWhiteList());
        }
    }

    private void updateProviderWhiteList() {
        synchronized (this.mProviderWhiteLock) {
            this.mProviderCpnWhiteList.clear();
            this.mProviderCpnWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getProviderWhiteList());
        }
    }

    private void updateBroadcastWhiteList() {
        synchronized (this.mBroadcastWhiteLock) {
            this.mBroadcastWhiteList.clear();
            this.mBroadcastWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getBroadcastWhiteList());
        }
    }

    private void updateBroadcastWhiteActionList() {
        synchronized (this.mBroadcastActionWhiteLock) {
            this.mBroadcastActionWhiteList.clear();
            this.mBroadcastActionWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getBroadcastActionWhiteList());
        }
    }

    private void updateProtectList() {
        synchronized (this.mProtectLock) {
            this.mProtectList.clear();
            this.mProtectList.addAll(OppoAppStartupManagerUtils.getInstance().getProtectList());
        }
    }

    private void updateAssociateStartWhiteList() {
        synchronized (this.mAssociateStartWhiteLock) {
            this.mAssociateStartWhiteList.clear();
            this.mAssociateStartWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getAssociateStartWhiteList());
        }
    }

    private void updateAuthorizeCpnList() {
        synchronized (this.mAuthorizeCpnListLock) {
            this.mAuthorizeCpnList.clear();
            this.mAuthorizeCpnList.addAll(OppoAppStartupManagerUtils.getInstance().getAuthorizeCpnList());
        }
    }

    private void updatePayCpnList() {
        synchronized (this.mPayCpnLitLock) {
            if (!OppoAppStartupManagerUtils.getInstance().getPayCpnList().isEmpty()) {
                this.mCollectPayCpnList.clear();
                this.mCollectPayCpnList.addAll(OppoAppStartupManagerUtils.getInstance().getPayCpnList());
            }
        }
    }

    protected boolean isAppStartForbidden(String packageName) {
        if (!OppoListManager.getInstance().isAppStartForbidden(packageName)) {
            return false;
        }
        if (this.mSwitchMonitor) {
            this.mHandler.post(new CollectAppInterceptRunnable("noNeed", packageName, "noNeed", "service"));
        }
        Log.i("OppoAppStartupManager", packageName + " is forbidden to start by service");
        return true;
    }

    protected boolean isAllowStartFromStartService(ProcessRecord callerApp, int callingPid, int callingUid, ServiceRecord s, Intent intent) {
        if (isExpVersion() || isRootOrShell(callingUid)) {
            return true;
        }
        if (inStartServiceWhiteList(s.appInfo.packageName)) {
            if (this.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "inStartServiceWhiteList: " + s.appInfo.packageName);
            }
            return true;
        } else if (inStartServiceWhiteCpnList(s)) {
            if (this.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "inStartServiceWhiteCpnList: " + s.name.getClassName());
            }
            return true;
        } else {
            boolean isAllowStart;
            if (callingPid == -1) {
                if (hasExtra(intent, "android.intent.extra.ALARM_COUNT")) {
                    String callerPkg = getPackageNameForUid(callingUid);
                    if (callerPkg == null) {
                        callerPkg = START_PROCESS_FROM_ALARM;
                    } else {
                        callerPkg = composePackage(START_PROCESS_FROM_ALARM, callerPkg);
                    }
                    isAllowStart = isAllowStartFromService(null, callerPkg, callingUid, s, intent, TYPE_START_SERVICE_FROM_ALARM);
                    return isAllowStart;
                }
            }
            if (callerApp != null || callingPid <= 0 || s.appInfo == null || callingUid != s.appInfo.uid) {
                isAllowStart = isAllowStartFromService(callerApp, "", callingUid, s, intent, TYPE_START_SERVICE);
            } else {
                isAllowStart = isAllowStartFromService(null, getPackageNameForUid(callingUid), callingUid, s, intent, TYPE_START_SERVICE_CALL_NULL);
            }
            return isAllowStart;
        }
    }

    protected boolean isAllowStartFromBindService(ProcessRecord callerApp, String callerPkg, int callingUid, ServiceRecord s, Intent intent, String type) {
        if (isExpVersion() || isRootOrShell(callingUid)) {
            return true;
        }
        if (inBindServiceCpnWhiteList(s)) {
            if (this.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "inBindServiceCpnWhiteList: " + s.appInfo.packageName + ", cpnn " + s.name.getClassName());
            }
            return true;
        } else if (callerApp != null || !START_PROCESS_FROM_JOB.equals(callerPkg) || !inJobWhiteList(s.appInfo.packageName)) {
            return isAllowStartFromService(callerApp, callerPkg, callingUid, s, intent, type);
        } else {
            if (this.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "inJobWhiteList: " + s.appInfo.packageName);
            }
            return true;
        }
    }

    private boolean isAllowStartFromService(ProcessRecord callerApp, String callerPkg, int callingUid, ServiceRecord s, Intent intent, String type) {
        String calleePkg = s.appInfo.packageName;
        if (callerApp == null && (TYPE_START_SERVICE_FROM_ALARM.equals(type) || TYPE_START_SERVICE_CALL_NULL.equals(type))) {
            if (isDefaultAllowStart(s.appInfo) || inProtectWhiteList(calleePkg) || OppoListManager.getInstance().isInAutoBootWhiteList(calleePkg) || isInLruProcessesLocked(s.appInfo.uid)) {
                if (this.mDebugSwitch) {
                    Log.i("OppoAppStartupManager", "start " + calleePkg + " by service " + " callingUid " + callingUid + ", intent=" + intent);
                }
                return true;
            }
        } else if (TYPE_BIND_SERVICE_FROM_JOB.equals(type)) {
            if (isDefaultAllowStart(s.appInfo) || inProtectWhiteList(calleePkg) || OppoListManager.getInstance().isInAutoBootWhiteList(calleePkg)) {
                if (this.mDebugSwitch) {
                    Log.i("OppoAppStartupManager", "start " + calleePkg + " by service " + " callingUid " + callingUid + ", intent=" + intent);
                }
                return true;
            }
        } else if (isSameApplication(callerApp, callingUid, s.appInfo) || inAssociateStartWhiteList(calleePkg) || isDefaultAllowStart(s.appInfo) || inProtectWhiteList(calleePkg)) {
            if (this.mDebugSwitch) {
                Log.i("OppoAppStartupManager", "start " + calleePkg + " by service " + " callingUid " + callingUid + ", intent=" + intent);
            }
            return true;
        }
        if (callerApp != null) {
            List<String> pkgList = getAppPkgList(callerApp);
            if (pkgList.size() > 0) {
                callerPkg = (String) pkgList.get(0);
            }
        }
        if (TYPE_BIND_SERVICE_FROM_JOB.equals(type) || TYPE_START_SERVICE_FROM_ALARM.equals(type) || TYPE_START_SERVICE_CALL_NULL.equals(type) || isSameApplication(callerApp, callingUid, s.appInfo)) {
            updateLaunchRecord(RECORD_CALLER_ANDROID, calleePkg, "0", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, type);
        } else {
            updateLaunchRecord(callerPkg, calleePkg, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, type);
        }
        if (this.mSwitchMonitor || this.mDebugSwitch) {
            if (TYPE_START_SERVICE_FROM_ALARM.equals(type)) {
                if (callerPkg == null) {
                    callerPkg = START_PROCESS_FROM_ALARM;
                } else {
                    callerPkg = composePackage(START_PROCESS_FROM_ALARM, callerPkg);
                }
            }
            ComponentName cpn = intent.getComponent();
            if (cpn != null) {
                if (this.mSwitchMonitor) {
                    this.mHandler.post(new CollectAppInterceptRunnable(callerPkg, calleePkg, cpn.getClassName(), type));
                }
                Log.i("OppoAppStartupManager", "prevent start " + calleePkg + ", cmp " + cpn.toShortString() + " by " + callerPkg + " callingUid " + callingUid + ", Type " + type);
            } else {
                String action = intent.getAction();
                if (action != null) {
                    if (this.mSwitchMonitor) {
                        if (type.equals(TYPE_START_SERVICE)) {
                            this.mHandler.post(new CollectAppInterceptRunnable(callerPkg, calleePkg, action, TYPE_START_SERVICE_ACTION));
                        }
                        if (type.equals(TYPE_BIND_SERVICE)) {
                            this.mHandler.post(new CollectAppInterceptRunnable(callerPkg, calleePkg, action, TYPE_BIND_SERVICE_ACTION));
                        }
                    }
                    Log.i("OppoAppStartupManager", "prevent start " + calleePkg + "s.name " + s.name + ", Intent { act=" + action + " } by " + callerPkg + " callingUid " + callingUid + ", Type " + type);
                }
            }
        }
        return false;
    }

    protected void collectAppStartBySystemUI(ProcessRecord callerApp, ServiceRecord s) {
        if (getAppPkgList(callerApp).contains("com.android.systemui") && (s.appInfo.flags & 1) == 0 && s.name != null) {
            if (this.mSwitchMonitor) {
                this.mHandler.post(new CollectAppInterceptRunnable("com.android.systemui", s.appInfo.packageName, s.name.getClassName(), TYPE_BIND_SERVICE_FROM_SYSTEMUI));
            }
            Log.i("OppoAppStartupManager", "start " + s.appInfo.packageName + ", cpn " + s.name + " by systemui");
        }
    }

    protected boolean isAllowStartFromBroadCast(ProcessRecord callerApp, String callerPkg, int callingUid, Intent intent, ResolveInfo info) {
        if (isExpVersion() || isRootOrShell(callingUid)) {
            return true;
        }
        String packageName = info.activityInfo.applicationInfo.packageName;
        if (callerApp == null && (callerPkg.equals(START_PROCESS_FROM_ALARM) || callerPkg.equals(START_PROCESS_FROM_LOCATION))) {
            if (isDefaultAllowStart(info.activityInfo.applicationInfo) || OppoListManager.getInstance().isInAutoBootWhiteList(packageName) || inProtectWhiteList(packageName) || inBroadCastWhiteList(packageName) || inBroadCastActionWhiteList(intent) || isInLruProcessesLocked(info.activityInfo.applicationInfo.uid)) {
                if (this.mDebugSwitch) {
                    Log.d("OppoAppStartupManager", "start " + packageName + " by broadcast " + " callingUid " + callingUid + ", Intent=" + intent);
                }
                return true;
            }
        } else if (isSameApplication(callerApp, callingUid, info.activityInfo.applicationInfo) || isDefaultAllowStart(info.activityInfo.applicationInfo) || OppoListManager.getInstance().isInAutoBootWhiteList(packageName) || inProtectWhiteList(packageName) || inBroadCastWhiteList(packageName) || inBroadCastActionWhiteList(intent)) {
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", "start " + packageName + " by broadcast " + " callingUid " + callingUid + ", Intent=" + intent);
            }
            return true;
        }
        updateLaunchRecord(RECORD_CALLER_ANDROID, packageName, "0", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, "broadcast");
        if (this.mSwitchMonitor || this.mDebugSwitch) {
            if (callerApp != null) {
                List<String> pkgList = getAppPkgList(callerApp);
                if (pkgList.size() > 0) {
                    callerPkg = (String) pkgList.get(0);
                }
            } else if (callerApp == null && START_PROCESS_FROM_ALARM.equals(callerPkg)) {
                callerPkg = getPackageNameForUid(callingUid);
                if (callerPkg == null) {
                    callerPkg = START_PROCESS_FROM_ALARM;
                } else {
                    callerPkg = composePackage(START_PROCESS_FROM_ALARM, callerPkg);
                }
            } else if (callerApp == null && callerPkg == null) {
                callerPkg = getPackageNameForUid(callingUid);
                if (callerPkg == null) {
                    callerPkg = "unknow";
                } else {
                    callerPkg = composePackage("unknow", callerPkg);
                }
            }
            ComponentName cpn = intent.getComponent();
            if (cpn != null) {
                if (this.mSwitchMonitor) {
                    this.mHandler.post(new CollectAppInterceptRunnable(callerPkg, packageName, cpn.getClassName(), "broadcast"));
                }
                Log.i("OppoAppStartupManager", "prevent start " + packageName + ", cmp " + cpn.toShortString() + " by broadcast " + callerPkg + " callingUid " + callingUid);
            } else {
                String action = intent.getAction();
                if (action != null) {
                    if (this.mSwitchMonitor) {
                        this.mHandler.post(new CollectAppInterceptRunnable(callerPkg, packageName, action, TYPE_BROADCAST_ACTION));
                    }
                    Log.i("OppoAppStartupManager", "prevent start " + packageName + ", Intent { act=" + action + " } by broadcast " + callerPkg + " callingUid " + callingUid);
                }
            }
        }
        return false;
    }

    protected boolean isAllowStartFromBroadCast(int callingUid, int callingPid, Intent intent, ResolveInfo info) {
        if (isExpVersion() || isRootOrShell(callingUid)) {
            return true;
        }
        boolean result = true;
        if (callingPid == -1) {
            if (hasExtra(intent, "android.intent.extra.ALARM_COUNT")) {
                result = isAllowStartFromBroadCast(null, START_PROCESS_FROM_ALARM, callingUid, intent, info);
            } else if (hasExtra(intent, "location")) {
                result = isAllowStartFromBroadCast(null, START_PROCESS_FROM_LOCATION, callingUid, intent, info);
            }
        } else if (!(OppoListManager.getInstance().isInAutoBootWhiteList(info.activityInfo.applicationInfo.packageName) || (isDefaultAllowStart(info.activityInfo.applicationInfo) ^ 1) == 0)) {
            if (!isSameApplication(null, callingUid, info.activityInfo.applicationInfo)) {
                result = false;
                updateLaunchRecord(RECORD_CALLER_ANDROID, info.activityInfo.applicationInfo.packageName, "0", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, "broadcast");
            } else if (!isInLruProcessesLocked(info.activityInfo.applicationInfo.uid)) {
                result = false;
                updateLaunchRecord(RECORD_CALLER_ANDROID, info.activityInfo.applicationInfo.packageName, "0", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, "broadcast");
            }
        }
        if (this.mDebugSwitch) {
            Log.i("OppoAppStartupManager", "Broadcast caller pid " + callingPid + " uid " + callingUid + " intent=" + intent + ",applicationInfo=" + info.activityInfo.applicationInfo.packageName + " " + result);
        }
        return result;
    }

    protected boolean isAllowStartFromProvider(ProcessRecord callerApp, ContentProviderRecord cpr, ApplicationInfo appInfo) {
        int callingUid = -1;
        if (callerApp != null) {
            callingUid = callerApp.uid;
        }
        if (isExpVersion() || isRootOrShell(callingUid)) {
            return true;
        }
        String packageName = appInfo.packageName;
        if (callerApp != null && isSameApplication(callerApp, callingUid, appInfo)) {
            if (this.mDebugSwitch) {
                Log.i("OppoAppStartupManager", "start " + packageName + " by provider " + " callingUid " + callingUid + ", cpr=" + cpr.toShortString());
            }
            return true;
        } else if (isDefaultAllowStart(appInfo) || inAssociateStartWhiteList(packageName) || inProtectWhiteList(packageName) || inProviderCpnWhiteList(cpr.name)) {
            if (this.mDebugSwitch) {
                Log.i("OppoAppStartupManager", "start " + packageName + " by provider " + " callingUid " + callingUid + ", cpr=" + cpr.toShortString());
            }
            return true;
        } else {
            String callerPkg = "unknow";
            if (callerApp != null) {
                List<String> pkgList = getAppPkgList(callerApp);
                if (pkgList.size() == 0) {
                    return true;
                }
                String callerPkg2 = (String) pkgList.get(0);
                if (isSameApplication(callerApp, callingUid, appInfo)) {
                    updateLaunchRecord(RECORD_CALLER_ANDROID, packageName, "0", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, "provider");
                    callerPkg = callerPkg2;
                } else {
                    updateLaunchRecord(callerPkg2, packageName, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, "provider");
                    callerPkg = callerPkg2;
                }
            }
            if (this.mSwitchMonitor || this.mDebugSwitch) {
                ComponentName cpn = cpr.name;
                if (cpn != null) {
                    if (this.mSwitchMonitor) {
                        this.mHandler.post(new CollectAppInterceptRunnable(callerPkg, packageName, cpn.getClassName(), "provider"));
                    }
                    Log.i("OppoAppStartupManager", "prevent start " + packageName + ", cmp " + cpn.toShortString() + " by contentprovider " + callerPkg + " callingUid " + callingUid);
                }
            }
            return false;
        }
    }

    public boolean isAllowStartFromNotificationService(ComponentName component, String rebindType) {
        if (isExpVersion()) {
            return true;
        }
        String packageName = component.getPackageName();
        if (packageName == null || rebindType == null) {
            return true;
        }
        if (inNotificationWhiteList(packageName) || inProtectWhiteList(packageName)) {
            if (this.mDebugSwitch) {
                Log.i("OppoAppStartupManager", "start " + packageName + " by notification " + " " + rebindType + " ,cpn=" + component.toShortString());
            }
            return true;
        }
        boolean result = false;
        ApplicationInfo info;
        if (rebindType.equals(REBIND_FORCE_STOP) || rebindType.equals(REBIND_SETTING_CHANGE)) {
            if (OppoListManager.getInstance().isInAutoBootWhiteList(packageName)) {
                result = true;
            } else {
                info = getApplicationInfo(packageName);
                if (info != null && isDefaultAllowStart(info)) {
                    result = true;
                }
            }
        } else if (rebindType.equals(REBIND_USER_UNLOCK)) {
            info = getApplicationInfo(packageName);
            if (info != null && isDefaultAllowStart(info)) {
                result = true;
            }
        } else if (rebindType.equals(REBIND_USER_SWITCH)) {
            result = true;
        }
        if (this.mDebugSwitch) {
            if (result) {
                Log.i("OppoAppStartupManager", "start " + packageName + " by notification " + " " + rebindType + " ,cpn=" + component.toShortString());
            } else {
                Log.i("OppoAppStartupManager", "prevent start " + packageName + " by notification " + " " + rebindType + " ,cpn=" + component.toShortString());
                if (this.mSwitchMonitor) {
                    this.mHandler.post(new CollectAppInterceptRunnable(START_PROCESS_FROM_NOTIFICATION_LISTENER, packageName, component.getClassName(), TYPE_BIND_SERVICE_FORM_NOTIFICATION));
                }
            }
        }
        if (!result) {
            updateLaunchRecord(RECORD_CALLER_ANDROID, packageName, "0", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON, START_PROCESS_FROM_NOTIFICATION_LISTENER);
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
                Log.i("OppoAppStartupManager", extra + ", isParcelled=" + isParcelled + ", hasExtra=" + hasExtra);
            }
        } catch (Exception e) {
        }
        return hasExtra;
    }

    private boolean isRootOrShell(int uid) {
        return uid == 0 || uid == OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT;
    }

    private boolean isSameApplication(ProcessRecord callerApp, int callingUid, ApplicationInfo appInfo) {
        boolean result = false;
        if (callingUid == appInfo.uid || !(callerApp == null || callerApp.pkgList == null || !callerApp.pkgList.containsKey(appInfo.packageName))) {
            result = true;
        }
        if (this.mDynamicDebug && result) {
            Log.d("OppoAppStartupManager", callingUid + " sameApplication");
        }
        return result;
    }

    protected String composePackage(String pre, String suf) {
        StringBuilder stringBuilder = new StringBuilder(100);
        stringBuilder.append(pre);
        stringBuilder.append(LocationManagerService.OPPO_FAKE_LOCATION_SPLIT);
        stringBuilder.append(suf);
        return stringBuilder.toString();
    }

    private boolean isDefaultAllowStart(ApplicationInfo info) {
        if (inBuildBlackList(info.packageName)) {
            return false;
        }
        if (info.uid <= 10000 || (info.flags & 1) != 0 || (info.flags & 128) != 0 || inCustomizeWhiteList(info.packageName) || inGlobalWhiteList(info.packageName) || OppoListManager.getInstance().isCtaPackage(info.packageName) || isCtsRunning()) {
            return true;
        }
        return false;
    }

    protected void collectAppStartInfo(ProcessRecord app, String hostingNameStr, String hostingType) {
        if (this.mSwitchMonitor) {
            long time = 0;
            if (this.mDynamicDebug) {
                time = SystemClock.elapsedRealtime();
            }
            if (app.info != null && (app.info.flags & 1) == 0) {
                String packageName = app.info.packageName;
                String processName = app.processName;
                if (packageName != null && processName != null && hostingType != null) {
                    if (packageName != null && OppoListManager.getInstance().isAppStartForbidden(packageName)) {
                        this.mHandler.post(new CollectAppInterceptRunnable("noNeed", packageName, hostingNameStr != null ? hostingNameStr : "", TYPE_START_PROCEESS_LOECKED));
                    }
                    if (inCollectAppStartList(packageName)) {
                        if (!isInLruProcessesLocked(app.uid)) {
                            String startMode = APP_START_BY_OTHER;
                            boolean isAssociateSwitch = inAssociateStartWhiteList(packageName);
                            boolean isBootStartSwitch = OppoListManager.getInstance().isInAutoBootWhiteList(packageName);
                            if (isAssociateSwitch && isBootStartSwitch) {
                                startMode = composePackage(APP_START_BY_BOOTSTART, APP_START_BY_ASSOCIATE);
                            } else if (isAssociateSwitch) {
                                startMode = APP_START_BY_ASSOCIATE;
                            } else if (isBootStartSwitch) {
                                startMode = APP_START_BY_BOOTSTART;
                            } else if (OppoListManager.getInstance().isInstalledAppWidget(packageName) && "broadcast".equals(hostingType)) {
                                startMode = APP_START_BY_WIDGET;
                            } else if (packageName.equals(processName) && TYPE_ACTIVITY.equals(hostingType)) {
                                startMode = APP_START_BY_CLICK;
                            }
                            if (this.mDynamicDebug) {
                                Log.d("OppoAppStartupManager", "ams_collectAppStartInfo cost time=" + (SystemClock.elapsedRealtime() - time));
                            }
                            if (this.mDynamicDebug) {
                                Log.i("OppoAppStartupManager", Log.getStackTraceString(new Throwable()));
                            }
                            this.mHandler.post(new CollectAppStartRunnable(packageName, processName, hostingType, startMode, hostingNameStr));
                        }
                    }
                }
            }
        }
    }

    private void collectProcessStartInfo(String packageName, String processName, String hostingType, String startMode, String hostingNameStr) {
        OppoAppMonitorInfo processStartInfo = getProcessInfoInList(processName);
        if (processStartInfo == null) {
            this.mCollectProcessInfoList.add(OppoAppMonitorInfo.builderPorcessInfo(packageName, processName, hostingType, startMode, hostingNameStr));
            return;
        }
        processStartInfo.increaseProcessStartCount(packageName, processName, hostingType, startMode, hostingNameStr);
    }

    private OppoAppMonitorInfo getProcessInfoInList(String processName) {
        for (OppoAppMonitorInfo appinfo : this.mCollectProcessInfoList) {
            if (appinfo.getProcesName().equals(processName)) {
                return appinfo;
            }
        }
        return null;
    }

    private void collectBlackListInterceptInfo(String calleePkg, String calleeCpn, String startMode) {
        OppoAppMonitorInfo interceptInfo = getBlackListInterceptInList(calleePkg);
        if (interceptInfo == null) {
            this.mCollectBlackListInterceptList.add(OppoAppMonitorInfo.builder(calleePkg, calleeCpn, startMode));
            return;
        }
        interceptInfo.increaseBlackListInterceptCount(calleePkg, calleeCpn, startMode);
    }

    private OppoAppMonitorInfo getBlackListInterceptInList(String packageName) {
        for (OppoAppMonitorInfo appinfo : this.mCollectBlackListInterceptList) {
            if (appinfo.getCalledPkgName().equals(packageName)) {
                return appinfo;
            }
        }
        return null;
    }

    private void monitorAppStartInfo(String callerPkgName, String calledPkgName, String callCpnName, String callType) {
        if (!this.mSwitchMonitor) {
            return;
        }
        if (callerPkgName == null || calledPkgName == null || callType == null) {
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", "monitorAppStartInfo callerPkgName == null || calledPkgName == null");
            }
            return;
        }
        String calledAppExist = "false";
        if (!(callerPkgName.equals("noNeed") || (callType.equals("broadcast") ^ 1) == 0 || (callType.equals(TYPE_BROADCAST_ACTION) ^ 1) == 0 || !checkAppExist(calledPkgName))) {
            calledAppExist = "true";
        }
        handleAppMonitorInfo(callerPkgName, calledPkgName, callCpnName, callType, calledAppExist);
    }

    public void sendMonitorInfoNotify() {
        Intent intent = new Intent(ACTION_OPPO_STARTUP_APP_MONITOR);
        intent.putExtra(SoundModelContract.KEY_TYPE, "appcallinfo");
        intent.putStringArrayListExtra("data", new ArrayList(this.mMonitorAppUploadList));
        if (this.mAms != null) {
            this.mAms.mContext.sendBroadcast(intent);
        }
    }

    private void collectAppInterceptInfo(String callerPkg, String calledPkg, String callCpnName, String callType, String calledAppExist) {
        OppoAppMonitorInfo appMonitorInfo = getAppInfoInList(callerPkg);
        if (appMonitorInfo == null) {
            this.mMonitorAppInfoList.add(OppoAppMonitorInfo.builder(callerPkg, calledPkg, callCpnName, callType, calledAppExist));
            return;
        }
        appMonitorInfo.increaseCallCount(calledPkg, callCpnName, callType, calledAppExist);
    }

    public OppoAppMonitorInfo getAppInfoInList(String callerPkg) {
        for (OppoAppMonitorInfo appinfo : this.mMonitorAppInfoList) {
            if (appinfo.getCallerPkgName().equals(callerPkg)) {
                return appinfo;
            }
        }
        return null;
    }

    public void notifyAppInterceptInfo() {
        if (this.mMonitorAppInfoList.isEmpty()) {
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", "mMonitorAppInfoList is empty!");
            }
            return;
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", "--------------------------notifyMonitorCallInfo start---------------------------");
        }
        int length = this.mMonitorAppInfoList.size();
        for (int i = 0; i < length; i++) {
            OppoAppMonitorInfo appInfo = (OppoAppMonitorInfo) this.mMonitorAppInfoList.get(i);
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
            Log.d("OppoAppStartupManager", "--------------------------notifyMonitorCallInfo end-----------------------------");
        }
        sendMonitorInfoNotify();
        this.mMonitorAppInfoList.clear();
        this.mMonitorAppUploadList.clear();
    }

    public void handleAppMonitorInfo(String callerPkgName, String calledPkgName, String callCpnName, String callType, String calledAppExist) {
        if (callCpnName == null) {
            callCpnName = "cpnUnknow";
            Log.d("OppoAppStartupManager", "handleAppMonitorInfo cpnName is null.");
        }
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "handleAppMonitorInfo callerPkgName = " + callerPkgName + "  calledPkgName = " + calledPkgName + "  callType = " + callType + "  cpnName = " + callCpnName + "  calledAppExist = " + calledAppExist);
        }
        collectAppInterceptInfo(callerPkgName, calledPkgName, callCpnName, callType, calledAppExist);
        if (this.mMonitorAppInfoList.size() >= OppoAppStartupManagerUtils.getInstance().getCallCheckCount()) {
            notifyAppInterceptInfo();
        }
    }

    /* JADX WARNING: Missing block: B:16:0x005d, code:
            r0 = new com.android.server.am.OppoAppStartupStatistics.OppoCallActivityEntry(r5, r6);
     */
    /* JADX WARNING: Missing block: B:17:0x0069, code:
            if (r4.mLastCalledAcivityList.size() < 5) goto L_0x007a;
     */
    /* JADX WARNING: Missing block: B:18:0x006b, code:
            r4.mLastCalledAcivityList.remove(0);
            r4.mLastCalledAcivityList.add(r0);
     */
    /* JADX WARNING: Missing block: B:19:0x0076, code:
            return;
     */
    /* JADX WARNING: Missing block: B:23:0x007a, code:
            r4.mLastCalledAcivityList.add(r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void collectLastCallActivityInfo(String pkgName, String cpnName) {
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", "collectLastCallActivityInfo pkgName = " + pkgName);
            Log.d("OppoAppStartupManager", "collectLastCallActivityInfo cpnName = " + cpnName);
        }
        if (pkgName == null || cpnName == null) {
            Log.d("OppoAppStartupManager", "collectLastCallActivityInfo pkgName or cpnName is null!");
            return;
        }
        synchronized (this.mAuthorizeCpnListLock) {
            if (this.mAuthorizeCpnList.contains(cpnName)) {
                Log.d("OppoAppStartupManager", "collectLastCallActivityInfo cpnName must be filter!");
            }
        }
    }

    public List<String> getAppPkgList(ProcessRecord app) {
        ArrayList<String> pkgList = new ArrayList();
        String[] list = app.getPackageList();
        if (list == null) {
            return pkgList;
        }
        for (Object add : list) {
            pkgList.add(add);
        }
        return pkgList;
    }

    public ComponentName getTopComponentName() {
        ComponentName topCpn = null;
        if (this.mAms != null) {
            topCpn = this.mAms.getTopAppName();
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", "getTopPkgName topCpn = " + topCpn);
        }
        return topCpn;
    }

    private boolean isScreenOn() {
        boolean ret = false;
        try {
            return Stub.asInterface(ServiceManager.getService("power")).isInteractive();
        } catch (RemoteException e) {
            Log.e("OppoAppStartupManager", "Error getting screen status", e);
            return ret;
        }
    }

    public void init(ActivityManagerService ams) {
        this.mAms = ams;
        initData();
        OppoAppStartupStatistics.getInstance().init(this, this.mHandler, ams);
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.mDebugSwitch = DEBUG_DETAIL | this.mDynamicDebug;
        OppoAppStartupManagerUtils.getInstance().setDynamicDebugSwitch();
    }

    public void openLog(boolean on) {
        Log.i("OppoAppStartupManager", "#####openlog####");
        Log.i("OppoAppStartupManager", "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Log.i("OppoAppStartupManager", "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Log.i("OppoAppStartupManager", "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Log.i("OppoAppStartupManager", "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", new Class[]{String.class});
            Log.i("OppoAppStartupManager", "invoke " + m);
            m.invoke(cls.newInstance(), new Object[]{OppoAppStartupManager.class.getName()});
            Log.i("OppoAppStartupManager", "invoke end!");
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

    protected boolean inAssociateStartWhiteList(String packageName) {
        boolean result;
        synchronized (this.mAssociateStartWhiteLock) {
            if (this.mAssociateStartWhiteList.isEmpty()) {
                result = OppoAppStartupListManager.getInstance().isInAssociateWhiteList(packageName);
            } else {
                result = this.mAssociateStartWhiteList.contains(packageName);
            }
        }
        if (result && this.mDebugSwitch) {
            Log.i("OppoAppStartupManager", packageName + " in AssociateStartWhiteList");
        }
        return result;
    }

    private ApplicationInfo getApplicationInfo(String packageName) {
        try {
            if (this.mAms != null) {
                return this.mAms.mContext.getPackageManager().getApplicationInfo(packageName, 0);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String getPackageNameForUid(int uid) {
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
            if (!(this.mAms == null || this.mAms.mContext == null)) {
                isCtsRunning = this.mAms.mContext.getPackageManager().isClosedSuperFirewall();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.mDebugSwitch) {
            Log.v("OppoAppStartupManager", "isCtsRunning:" + isCtsRunning);
        }
        return isCtsRunning;
    }

    private boolean isExpVersion() {
        try {
            if (this.mAms == null || this.mAms.mContext == null) {
                return false;
            }
            return this.mAms.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void uploadProcessStartInfo() {
        if (!this.mCollectProcessInfoList.isEmpty()) {
            if (this.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "--------------------------uploadProcessStartInfo start---------------------------");
            }
            List<Map<String, String>> uploadList = new ArrayList();
            int length = this.mCollectProcessInfoList.size();
            for (int i = 0; i < length; i++) {
                OppoAppMonitorInfo appInfo = (OppoAppMonitorInfo) this.mCollectProcessInfoList.get(i);
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
                Log.d("OppoAppStartupManager", "--------------------------uploadProcessStartInfo end-----------------------------");
            }
            if (this.mAms != null) {
                OppoStatistics.onCommon(this.mAms.mContext, UPLOAD_LOGTAG, UPLOAD_THIRD_EVENTID, uploadList, false);
            }
            this.mCollectProcessInfoList.clear();
        }
    }

    private void uploadBlackListIntercept() {
        if (!this.mCollectBlackListInterceptList.isEmpty()) {
            if (this.mDebugSwitch) {
                Log.d("OppoAppStartupManager", "--------------------------uploadBlackListIntercept start---------------------------");
            }
            List<Map<String, String>> uploadList = new ArrayList();
            int length = this.mCollectBlackListInterceptList.size();
            for (int i = 0; i < length; i++) {
                OppoAppMonitorInfo appInfo = (OppoAppMonitorInfo) this.mCollectBlackListInterceptList.get(i);
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
                Log.d("OppoAppStartupManager", "--------------------------uploadBlackListIntercept end-----------------------------");
            }
            if (this.mAms != null) {
                OppoStatistics.onCommon(this.mAms.mContext, UPLOAD_LOGTAG, UPLOAD_BLACK_EVENTID, uploadList, false);
            }
            this.mCollectBlackListInterceptList.clear();
        }
    }

    private void initPayCpnList() {
        if (this.mCollectPayCpnList.isEmpty()) {
            this.mCollectPayCpnList.add("com.nearme.plugin.pay.activity.MainChargeActivity");
            this.mCollectPayCpnList.add("com.tencent.mm.plugin.base.stub.WXPayEntryActivity");
            this.mCollectPayCpnList.add("com.alipay.android.app.TransProcessPayActivity");
        }
    }

    private void collectGamePayProcessInfo(String callerPkg, String calledPkg, String calledCpn) {
        OppoAppMonitorInfo interceptInfo = getGamePayProcessInList(callerPkg);
        if (interceptInfo == null) {
            this.mCollectGamePayList.add(OppoAppMonitorInfo.builderGamePay(callerPkg, calledPkg, calledCpn));
            return;
        }
        interceptInfo.increaseGamePayCount(callerPkg, calledPkg, calledCpn);
    }

    private OppoAppMonitorInfo getGamePayProcessInList(String packageName) {
        for (OppoAppMonitorInfo appInfo : this.mCollectGamePayList) {
            if (appInfo.getCallerPkgName().equals(packageName)) {
                return appInfo;
            }
        }
        return null;
    }

    private void uploadGamePayProcessInfo() {
        if (!this.mCollectGamePayList.isEmpty()) {
            if (this.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "--------------------------uploadGamePay start---------------------------");
            }
            List<Map<String, String>> uploadList = new ArrayList();
            int length = this.mCollectGamePayList.size();
            for (int i = 0; i < length; i++) {
                OppoAppMonitorInfo appInfo = (OppoAppMonitorInfo) this.mCollectGamePayList.get(i);
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
                Log.d("OppoAppStartupManager", "--------------------------uploadGamePay end-----------------------------");
            }
            if (this.mAms != null) {
                OppoStatistics.onCommon(this.mAms.mContext, UPLOAD_LOGTAG, UPLOAD_GMAE_PAY, uploadList, false);
            }
            this.mCollectGamePayList.clear();
        }
    }

    protected void handleProcessStartupInfo(int callingPid, int callingUid, ProcessRecord callerApp, Intent intent, ApplicationInfo appInfo, String hostType) {
        if (this.mSwitchMonitor) {
            long time = 0;
            if (this.mDynamicDebug) {
                time = SystemClock.elapsedRealtime();
            }
            if (appInfo == null || !inCollectAppStartList(appInfo.packageName)) {
                return;
            }
            if (isInLruProcessesLocked(appInfo.uid)) {
                if (this.mDynamicDebug) {
                    Log.d("OppoAppStartupManager", "handleProcessStartupInfo1 cost time = " + (SystemClock.elapsedRealtime() - time));
                }
                return;
            }
            if (this.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "handleProcessStartupInfo2 cost time = " + (SystemClock.elapsedRealtime() - time));
            }
            OppoAppStartupStatistics.getInstance().collectAppStartInfo(callingPid, callingUid, callerApp, intent, appInfo, hostType);
        }
    }

    public boolean isRemovePendingJob(String packageName) {
        boolean result = true;
        if (packageName != null) {
            ApplicationInfo appInfo = getApplicationInfo(packageName);
            if (appInfo != null && (isDefaultAllowStart(appInfo) || OppoListManager.getInstance().isInAutoBootWhiteList(packageName) || isInLruProcessesLocked(appInfo.uid))) {
                result = false;
            }
        }
        if (this.mDynamicDebug) {
            Log.d("OppoAppStartupManager", "remove pendingJob  " + packageName + " result " + result);
        }
        return result;
    }

    public void handleAppStartForbidden(String packageName) {
        OppoAppStartupManagerUtils.getInstance().handleAppStartForbidden(packageName);
    }

    public String getDialogTitleText() {
        return OppoAppStartupManagerUtils.getInstance().getDialogTitleText();
    }

    public String getDialogContentText() {
        return OppoAppStartupManagerUtils.getInstance().getDialogContentText();
    }

    public String getDialogButtonText() {
        return OppoAppStartupManagerUtils.getInstance().getDialogButtonText();
    }

    public void resetDialogShowText() {
        OppoAppStartupManagerUtils.getInstance().resetDialogShowText();
    }

    public boolean isFromControlCenterPkg(String packageName) {
        boolean result = OppoListManager.getInstance().isFromControlCenterPkg(packageName);
        if (this.mDebugSwitch) {
            Log.d("OppoAppStartupManager", packageName + " isFromControlCenterPkg result " + result);
        }
        return result;
    }

    private void updateLaunchRecord(String callerPkg, String calledPkg, String launchMode, String launchType, String reason) {
        OppoAutostartManager.getInstance().updateLaunchRecord(callerPkg, calledPkg, launchMode, launchType, reason);
    }

    public void handleForceFinish() {
        if (!(this.mActivityStack == null || this.mPreRecord == null)) {
            this.mActivityStack.requestFinishActivityLocked(this.mPreRecord.appToken, 0, null, "resume-exception2", true);
            if (this.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "handleForceFinish activityRecord " + this.mPreRecord);
            }
        }
        this.mPreRecord = null;
    }

    public boolean isTenIntencept(String callingPackage, Intent intent) {
        if (intent == null || intent.getComponent() == null || intent.getComponent().getClassName() == null) {
            return false;
        }
        String cpnName = intent.getComponent().getClassName().trim();
        if (SEND_BROADCAST_TO_PKG.equals(callingPackage) && cpnName != null && cpnName.equals(OppoAppStartupManagerUtils.getInstance().getTencentInterceptCpn())) {
            return true;
        }
        return false;
    }

    public void handleApplicationCrash(ProcessRecord app, String exceptionClassName, String exceptionMessage, String exceptionTrace) {
        String prePkg = OppoAppSwitchManager.getInstance().mPre_PkgName;
        String nextPkg = OppoAppSwitchManager.getInstance().mNext_PkgName;
        if (app.info != null && app.info.packageName != null) {
            if ((prePkg != null && prePkg.equals(app.info.packageName)) || (nextPkg != null && nextPkg.equals(app.info.packageName))) {
                this.mHandler.post(new AppAbnormalMonitor(app.info.packageName, exceptionClassName, exceptionMessage, exceptionTrace, CALL_ARG_CRASH));
                if (this.mDynamicDebug) {
                    Log.d("OppoAppStartupManager", "handleApplicationCrash clasName=" + exceptionClassName + " msg=" + exceptionMessage);
                }
            }
        }
    }

    public void appStartupMonitor(ProcessRecord app, String hostingType) {
        if (!(isExpVersion() || !hostingType.equals(TYPE_ACTIVITY) || app == null || app.info == null || !ActivityManagerService.OPPO_LAUNCHER.equals(app.callingPkg) || (isDefaultAllowStart(app.info) ^ 1) == 0)) {
            this.mHandler.post(new AppAbnormalMonitor(app.info.packageName, "", "", "", CALL_ARG_STARTUP));
            if (this.mDynamicDebug) {
                Log.d("OppoAppStartupManager", "appStartupMonitor app=" + app + " callingPkg=" + app.callingPkg);
            }
        }
    }

    public void setAppStartMonitorController(IOppoAppStartController controller) {
        this.mController = controller;
    }

    public void notifyAppStartMonitorInfo(String packageName, String exceptionClass, String exceptionMsg, String exceptionTrace, String type) {
        if (this.mController != null) {
            try {
                this.mController.appStartMonitor(packageName, exceptionClass, exceptionMsg, exceptionTrace, type);
            } catch (Exception e) {
                this.mController = null;
                Log.e("OppoAppStartupManager", "notifyAppStartMonitorInfo failed!!");
            }
        }
    }
}
