package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.media.AudioManager;
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
import android.util.Log;
import android.util.OppoSafeDbReader;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoAppStartupStatistics.OppoCallActivityEntry;
import com.android.server.coloros.OppoListManager;
import com.android.server.oppo.IElsaManager;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import com.oppo.hypnus.Hypnus;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import oppo.util.OppoStatistics;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoAppStartupManager {
    protected static final String APP_START_BY_ASSOCIATE = "associate";
    protected static final String APP_START_BY_BOOTSTART = "bootstart";
    protected static final String APP_START_BY_CLICK = "click";
    protected static final String APP_START_BY_OTHER = "other";
    protected static final String APP_START_BY_SAMEAPP = "sameapp";
    public static boolean DEBUG_DETAIL = false;
    private static final String PRE_COLOROS = "com.coloros.";
    private static final String PRE_NEARME = "com.nearme.";
    private static final String PRE_OPPO = "com.oppo.";
    public static final String REBIND_FORCE_STOP = "forceStop";
    public static final String REBIND_SETTING_CHANGE = "settingChange";
    public static final String REBIND_USER_SWITCH = "userSwitch";
    public static final String REBIND_USER_UNLOCK = "userUnlock";
    private static final String SEND_BROADCAST_TO_PKG = "com.daemon.shelper";
    private static final String START_MARKET_ACTION = "oppo.intent.action.OPPO_STARTUP_MARKET";
    private static final String START_MARKET_ACTION_INTENT_KEY = "com.tencent.mm.intent";
    private static final String START_MARKET_ACTION_SWITCH_KEY = "switchValue";
    static final String START_PROCESS_FROM_ALARM = "system[alarmManger]";
    static final String START_PROCESS_FROM_JOB = "system[jobScheduler]";
    static final String START_PROCESS_FROM_LOCATION = "system[location]";
    static final String START_PROCESS_FROM_NOTIFICATION_LISTENER = "system[notificationListener]";
    static final String START_PROCESS_FROM_SYNC = "system[syncManager]";
    private static final String SYSTEM_UI_PKG = "com.android.systemui";
    public static String TAG = null;
    private static final String TENCENT_MM_PKG = "com.tencent.mm";
    static final String TYPE_ACTIVITY = "activity";
    static final String TYPE_BIND_SERVICE = "bs";
    static final String TYPE_BIND_SERVICE_ACTION = "bsa";
    static final String TYPE_BIND_SERVICE_FORM_NOTIFICATION = "bsfn";
    static final String TYPE_BIND_SERVICE_FROM_JOB = "bsfj";
    static final String TYPE_BIND_SERVICE_FROM_SYNC = "bsfs";
    static final String TYPE_BIND_SERVICE_FROM_SYSTEMUI = "bs_systemui";
    static final String TYPE_BROADCAST = "broadcast";
    static final String TYPE_BROADCAST_ACTION = "broadcast_action";
    static final String TYPE_DIALOG = "dialog";
    static final String TYPE_NO_NEED = "noNeed";
    static final String TYPE_PROVIDER = "provider";
    static final String TYPE_SERVICE = "service";
    static final String TYPE_START_PROCEESS_LOECKED = "startProcessLocked";
    static final String TYPE_START_SERVICE = "ss";
    static final String TYPE_START_SERVICE_ACTION = "ssa";
    static final String TYPE_START_SERVICE_CALL_NULL = "sscn";
    static final String TYPE_START_SERVICE_FROM_ALARM = "ssfa";
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
    private static OppoAppStartupManager mOppoAppStartupManager;
    final String ACTION_OPPO_ACTIVITY_CALL_MONITOR;
    final String ACTION_OPPO_ACTIVITY_INTERCEPT_MONITOR;
    final String ACTION_OPPO_STARTUP_APP_MONITOR;
    boolean DEBUG_SWITCH;
    boolean DynamicDebug;
    final int LAST_ACTIVITY_CALL_COUNT;
    final String OPPO_SAFE_URL_INTERCEPTION;
    final BroadcastReceiver dateChangedReceiver;
    private ContentObserver dufaultInputMethodObserver;
    List<String> mActionBlackList;
    private final Object mActionLock;
    List<String> mActivityBlackList;
    List<String> mActivityCalledKeyList;
    private final Object mActivityCalledKeyLock;
    List<String> mActivityCalledWhiteCpnList;
    private final Object mActivityCalledWhiteCpnLock;
    List<String> mActivityCalledWhitePkgList;
    private final Object mActivityCalledWhitePkgLock;
    List<String> mActivityCallerWhitePkgList;
    private final Object mActivityCallerWhitePkgLock;
    private final Object mActivityLock;
    List<String> mActivityPkgKeyList;
    private final Object mActivityPkgKeyLock;
    private ActivityStack mActivityStack;
    ActivityManagerService mAms;
    List<String> mAssociateStartWhiteList;
    private final Object mAssociateStartWhiteLock;
    private AudioManager mAudioManager;
    List<String> mAuthorizeCpnList;
    private final Object mAuthorizeCpnListLock;
    List<String> mBindServiceCpnWhiteList;
    private final Object mBindServiceWhiteLock;
    List<String> mBlackguardActivityList;
    private final Object mBlackguardActivityLock;
    List<String> mBlackguardList;
    private final Object mBlackguardLock;
    List<String> mBroadcastActionWhiteList;
    private final Object mBroadcastActionWhiteLock;
    List<String> mBroadcastWhiteList;
    private final Object mBroadcastWhiteLock;
    List<String> mBuildAppBlackList;
    private final Object mBuildAppBlackLock;
    List<String> mCollectAppStartList;
    private final Object mCollectAppStartLock;
    private List<OppoAppMonitorInfo> mCollectBlackListInterceptList;
    private List<OppoAppMonitorInfo> mCollectGamePayList;
    private List<String> mCollectPayCpnList;
    private List<OppoAppMonitorInfo> mCollectProcessInfoList;
    List<String> mCustomizeWhiteList;
    private final Object mCustomizeWhiteLock;
    private String mDefaultInputMethod;
    List<String> mGlobalWhiteList;
    private Handler mHandler;
    Hypnus mHypnus;
    List<String> mJobWhiteList;
    private final Object mJobWhiteLock;
    ArrayList<OppoCallActivityEntry> mLastCalledAcivityList;
    long mLastCheckTime;
    boolean mMonitorAll;
    List<OppoAppMonitorInfo> mMonitorAppInfoList;
    ArrayList<String> mMonitorAppUploadList;
    List<String> mNotifyWhiteList;
    private final Object mNotifyWhiteLock;
    private final Object mPayCpnLitLock;
    private ActivityRecord mPreRecord;
    List<String> mProtectList;
    private final Object mProtectLock;
    List<String> mProviderBlacklist;
    List<String> mProviderCpnWhiteList;
    private final Object mProviderLock;
    private final Object mProviderWhiteLock;
    List<String> mReceiverActionBlackList;
    private final Object mReceiverActionLock;
    List<String> mReceiverBlackList;
    private final Object mReceiverLock;
    private final Object mServiceLock;
    List<String> mSeviceCpnBlacklist;
    final BroadcastReceiver mShelperActionReceiver;
    List<String> mStartServiceWhiteCpnList;
    private final Object mStartServiceWhiteCpnLock;
    List<String> mStartServiceWhiteList;
    private final Object mStartServiceWhiteLock;
    boolean mSwitch;
    private boolean mSwitchBrowserInterceptUpload;
    boolean mSwitchInterceptActivity;
    boolean mSwitchMonitor;
    List<String> mSyncWhiteList;
    private final Object mSyncWhiteLock;

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
            OppoAppStartupManager.this.collectProcessStartInfo(this.mPackageName, this.mProcessName, this.mHostingType, this.mStartMode, this.mHostingNameStr != null ? this.mHostingNameStr : IElsaManager.EMPTY_PACKAGE);
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
            if (!(this.mCallerPkgName == null || this.mCalledPkgName == null || this.mCalledCpn == null || !this.mCallerPkgName.contains("nearme.gamecenter"))) {
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAppStartupManager.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAppStartupManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoAppStartupManager.<clinit>():void");
    }

    public OppoAppStartupManager() {
        this.DynamicDebug = false;
        this.DEBUG_SWITCH = DEBUG_DETAIL | this.DynamicDebug;
        this.mSwitch = SystemProperties.getBoolean("persist.sys.startupmanager", true);
        this.mSwitchMonitor = false;
        this.mSwitchInterceptActivity = false;
        this.mMonitorAll = SystemProperties.getBoolean("persist.sys.monitorall", false);
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
        this.mAms = null;
        this.LAST_ACTIVITY_CALL_COUNT = 5;
        this.mLastCheckTime = 0;
        this.ACTION_OPPO_STARTUP_APP_MONITOR = "android.intent.action.OPPO_STARTUP_APP_MONITOR";
        this.ACTION_OPPO_ACTIVITY_CALL_MONITOR = "android.intent.action.OPPO_ACTIVITY_CALL_MONITOR";
        this.ACTION_OPPO_ACTIVITY_INTERCEPT_MONITOR = "android.intent.action.OPPO_ACTIVITY_INTERCEPT_MONITOR";
        this.mMonitorAppUploadList = new ArrayList();
        this.mMonitorAppInfoList = new ArrayList();
        this.mLastCalledAcivityList = new ArrayList();
        this.mAudioManager = null;
        this.mHypnus = null;
        this.mHandler = null;
        this.mSwitchBrowserInterceptUpload = false;
        this.OPPO_SAFE_URL_INTERCEPTION = "android.intent.action.OPPO_SAFE_URL_INTERCEPTION";
        this.mDefaultInputMethod = null;
        this.mCollectProcessInfoList = new ArrayList();
        this.mCollectBlackListInterceptList = new ArrayList();
        this.mCollectGamePayList = new ArrayList();
        this.mCollectPayCpnList = new ArrayList();
        this.dateChangedReceiver = new BroadcastReceiver() {
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
        this.dufaultInputMethodObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                OppoAppStartupManager.this.mDefaultInputMethod = OppoAppStartupManager.this.getDefaultInputMethod();
            }
        };
    }

    public OppoAppStartupManager(String name) {
        this.DynamicDebug = false;
        this.DEBUG_SWITCH = DEBUG_DETAIL | this.DynamicDebug;
        this.mSwitch = SystemProperties.getBoolean("persist.sys.startupmanager", true);
        this.mSwitchMonitor = false;
        this.mSwitchInterceptActivity = false;
        this.mMonitorAll = SystemProperties.getBoolean("persist.sys.monitorall", false);
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
        this.mAms = null;
        this.LAST_ACTIVITY_CALL_COUNT = 5;
        this.mLastCheckTime = 0;
        this.ACTION_OPPO_STARTUP_APP_MONITOR = "android.intent.action.OPPO_STARTUP_APP_MONITOR";
        this.ACTION_OPPO_ACTIVITY_CALL_MONITOR = "android.intent.action.OPPO_ACTIVITY_CALL_MONITOR";
        this.ACTION_OPPO_ACTIVITY_INTERCEPT_MONITOR = "android.intent.action.OPPO_ACTIVITY_INTERCEPT_MONITOR";
        this.mMonitorAppUploadList = new ArrayList();
        this.mMonitorAppInfoList = new ArrayList();
        this.mLastCalledAcivityList = new ArrayList();
        this.mAudioManager = null;
        this.mHypnus = null;
        this.mHandler = null;
        this.mSwitchBrowserInterceptUpload = false;
        this.OPPO_SAFE_URL_INTERCEPTION = "android.intent.action.OPPO_SAFE_URL_INTERCEPTION";
        this.mDefaultInputMethod = null;
        this.mCollectProcessInfoList = new ArrayList();
        this.mCollectBlackListInterceptList = new ArrayList();
        this.mCollectGamePayList = new ArrayList();
        this.mCollectPayCpnList = new ArrayList();
        this.dateChangedReceiver = /* anonymous class already generated */;
        this.mShelperActionReceiver = /* anonymous class already generated */;
        this.dufaultInputMethodObserver = /* anonymous class already generated */;
        this.mHypnus = new Hypnus();
        registerLogModule();
        HandlerThread thread = new HandlerThread("AppStartupManager");
        thread.start();
        this.mHandler = new Handler(thread.getLooper());
    }

    public static final OppoAppStartupManager getInstance() {
        if (mOppoAppStartupManager == null) {
            mOppoAppStartupManager = new OppoAppStartupManager("OppoAppStartupManager");
        }
        return mOppoAppStartupManager;
    }

    public void initData() {
        Log.d(TAG, "initData");
        this.mSwitchMonitor = OppoAppStartupManagerUtils.getInstance().isForumVersion();
        updateConfiglist();
        updateMonitorlist();
        updateCustomizeWhiteList();
        updateAssociateStartList();
        this.mDefaultInputMethod = getDefaultInputMethod();
        if (this.mAms != null) {
            this.mAms.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("default_input_method"), true, this.dufaultInputMethodObserver);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.DATE_CHANGED");
            this.mAms.mContext.registerReceiver(this.dateChangedReceiver, filter, null, this.mHandler);
            IntentFilter shelperAction = new IntentFilter();
            shelperAction.addAction("oppo.intent.action.SHELPER_FINISH_ACTIVITY");
            this.mAms.mContext.registerReceiver(this.mShelperActionReceiver, shelperAction, null, this.mHandler);
        }
        initPayCpnList();
    }

    /* JADX WARNING: Missing block: B:3:0x0006, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleBroadcastIncludeForceStop(Intent intent, ProcessRecord callerApp) {
        if (this.mSwitch && intent != null && callerApp != null && (intent.getFlags() & 32) == 32 && callerApp.info != null && (callerApp.info.flags & 1) == 0) {
            if ("com.android.cts.robot.ACTION_POST".equals(intent.getAction())) {
                Log.d(TAG, callerApp.processName + " is CTS app. do not remove the flag!");
                return;
            }
            if (this.DEBUG_SWITCH) {
                Log.d(TAG, callerApp.processName + " is the thirdparty app. remove the flag! " + intent);
            }
            intent.setFlags(intent.getFlags() ^ 32);
        }
    }

    public boolean handleStartOrBindService(Intent service, ProcessRecord callerApp) {
        boolean result = false;
        if (!this.mSwitch || service == null || callerApp == null) {
            return false;
        }
        if (this.DynamicDebug) {
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        }
        if (callerApp.uid <= 10000) {
            if (this.DynamicDebug) {
                Log.v(TAG, "handleStartOrBindService callerApp.uid <= 10000 return");
                Log.v(TAG, "handleStartOrBindService callerApp: " + callerApp);
            }
            return false;
        }
        if (this.DynamicDebug) {
            Log.v(TAG, "handleStartOrBindService: " + service + " args=" + service.getExtras());
            Log.v(TAG, "handleStartOrBindService callerApp: " + callerApp);
        }
        ComponentName cpn = service.getComponent();
        if (cpn != null) {
            String cpnPkgName = cpn.getPackageName();
            String cpnClassName = cpn.getClassName();
            if (this.DynamicDebug) {
                Log.v(TAG, "handleStartOrBindService cpnPkgName == " + cpnPkgName);
                Log.v(TAG, "handleStartOrBindService cpnClassName == " + cpnClassName);
                Log.v(TAG, "handleStartOrBindService callerApp.processName == " + callerApp.processName);
            }
            if (!(!inSeviceCpnlist(cpnClassName) || callerApp.processName == null || callerApp.processName.contains(cpnPkgName))) {
                if (this.DEBUG_SWITCH) {
                    Log.v(TAG, "handleStartOrBindService return undo!");
                }
                result = true;
                if (this.mSwitchMonitor) {
                    this.mHandler.post(new CollectBlackListInterceptRunnable(cpnPkgName, cpnClassName, "service"));
                }
            }
        } else {
            if (this.DynamicDebug) {
                Log.v(TAG, "handleStartOrBindService cpn = null!");
            }
            String action = service.getAction();
            if (action != null) {
                if (this.DynamicDebug) {
                    Log.v(TAG, "handleStartOrBindService action == " + action);
                }
                String pkgName = service.getPackage();
                if (pkgName == null) {
                    if (this.DEBUG_SWITCH) {
                        Log.v(TAG, "handleStartOrBindService component = " + service.getComponent());
                    }
                    pkgName = "unknow";
                }
                if (this.DynamicDebug) {
                    Log.v(TAG, "handleStartOrBindService pkgName == " + pkgName);
                }
                if (inActionlist(action) && pkgName != null && callerApp.processName != null && !callerApp.processName.contains(pkgName)) {
                    if (this.DEBUG_SWITCH) {
                        Log.v(TAG, "handleStartOrBindService return undo!");
                    }
                    result = true;
                    if (this.mSwitchMonitor) {
                        this.mHandler.post(new CollectBlackListInterceptRunnable(pkgName, action, "service"));
                    }
                } else if (inBlackguardList(action)) {
                    if (this.DEBUG_SWITCH) {
                        Log.v(TAG, "handleStartOrBindService inBlackguardList return undo!");
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
                        this.mHandler.post(new CollectAppInterceptRunnable(TYPE_NO_NEED, cpnPkgName, TYPE_NO_NEED, "provider"));
                    }
                    Log.v(TAG, "handleStartProvider: " + cpnPkgName + " is forbidden to start");
                    result = true;
                }
                if (callerApp == null && inProviderlist(cpnClassName)) {
                    if (this.DEBUG_SWITCH) {
                        Log.v(TAG, "handleStartProvider return undo!");
                    }
                    result = true;
                } else if (!(callerApp == null || !inProviderlist(cpnClassName) || callerApp.processName == null || callerApp.processName.contains(cpnPkgName))) {
                    if (this.DEBUG_SWITCH) {
                        Log.v(TAG, "handleStartProvider return undo!");
                    }
                    result = true;
                    if (this.mSwitchMonitor) {
                        this.mHandler.post(new CollectBlackListInterceptRunnable(cpnPkgName, cpnClassName, "provider"));
                    }
                }
            }
            return result;
        }
        if (this.DynamicDebug) {
            Log.v(TAG, "handleStartProvider callerApp.uid <= 10000 return");
            Log.v(TAG, "handleStartProvider callerApp: " + callerApp);
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
                if (this.DynamicDebug) {
                    Log.v(TAG, "intent == null || callerApp == null");
                }
                return false;
            }
            if (this.DynamicDebug) {
                Log.v(TAG, "handleSpecialBroadcast: " + intent + " args=" + intent.getExtras());
                Log.v(TAG, "handleSpecialBroadcast callerApp: " + callerApp);
            }
            if (callerApp.uid <= 10000) {
                if (this.DynamicDebug) {
                    Log.v(TAG, "handleSpecialBroadcast callerApp.uid <= 10000 return");
                    Log.v(TAG, "handleSpecialBroadcast callerApp: " + callerApp);
                }
                return false;
            }
            ComponentName cpn = intent.getComponent();
            if (cpn != null) {
                String cpnPkgName = cpn.getPackageName();
                String cpnClassName = cpn.getClassName();
                if (this.DynamicDebug) {
                    Log.v(TAG, "handleSpecialBroadcast cpnPkgName == " + cpnPkgName);
                    Log.v(TAG, "handleSpecialBroadcast cpnClassName == " + cpnClassName);
                    Log.v(TAG, "handleSpecialBroadcast callerApp.processName == " + callerApp.processName);
                }
                if (!(!inReceiverlist(cpnClassName) || callerApp.processName == null || callerApp.processName.contains(cpnPkgName))) {
                    if (this.DEBUG_SWITCH) {
                        Log.v(TAG, "handleSpecialBroadcast return skip!");
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
                    if (this.DynamicDebug) {
                        Log.v(TAG, "handleSpecialBroadcast pkgName == " + pkgName);
                    }
                    if (!(!inReceiverActionlist(action) || pkgName == null || callerApp.processName == null || callerApp.processName.contains(pkgName))) {
                        if (this.DEBUG_SWITCH) {
                            Log.v(TAG, "handleSpecialBroadcast return undo!");
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
            this.mHandler.post(new CollectAppInterceptRunnable(TYPE_NO_NEED, packageName, TYPE_NO_NEED, "broadcast"));
        }
        Log.i(TAG, packageName + " is forbidden to start by broadcast");
        return true;
    }

    public boolean handleStartActivity(Intent intent, ActivityInfo aInfo, String callingPackage, int callingUid, int callingPid, int userId) {
        if (aInfo == null || aInfo.packageName == null) {
            if (this.DEBUG_SWITCH) {
                Log.v(TAG, "handleStartActivity aInfo == null || aInfo.packageName == null return.");
            }
            return false;
        }
        String calledPackageName = aInfo.packageName;
        if (calledPackageName == null || !OppoListManager.getInstance().isAppStartForbidden(calledPackageName)) {
            String cpnClassName = aInfo.name;
            if (OppoListManager.getInstance().getPreventRedundentStartSwitch() && cpnClassName != null && OppoListManager.getInstance().isRedundentActivity(cpnClassName)) {
                Log.v(TAG, "handleStartActivity redundent activity " + cpnClassName);
                return true;
            } else if (!this.mSwitch) {
                return false;
            } else {
                if (isExpVersion()) {
                    return false;
                }
                if (this.DynamicDebug) {
                    Log.v(TAG, "handleStartActivity cpnPkgName = " + calledPackageName);
                    Log.v(TAG, "handleStartActivity cpnClassName = " + cpnClassName);
                    Log.v(TAG, "handleStartActivity callerPkgName = " + callingPackage);
                }
                if (this.mSwitchMonitor || OppoAppStartupManagerUtils.getInstance().getSwitchStatus()) {
                    this.mHandler.post(new CollectGamePayRunnable(callingPackage, calledPackageName, cpnClassName, intent));
                }
                if (callingPackage == null) {
                    if (this.DynamicDebug) {
                        Log.v(TAG, "handleStartActivity callingPackage is empty.");
                    }
                    callingPackage = "empty";
                } else if (callingUid > 0 && callingUid <= 10000) {
                    if (this.DEBUG_SWITCH) {
                        Log.v(TAG, "handleStartActivity callerApp is systemapp return.");
                        Log.v(TAG, "handleStartActivity callerApp: " + callingPackage);
                    }
                    collectLastCallActivityInfo(calledPackageName, cpnClassName);
                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callingPackage, calledPackageName, cpnClassName, TYPE_NO_NEED, TYPE_NO_NEED, "allow_system");
                    return false;
                } else if (ActivityManagerService.OPPO_LAUNCHER.equals(callingPackage)) {
                    if (this.DEBUG_SWITCH) {
                        Log.v(TAG, "handleStartActivity callerApp is oppohome return.");
                        Log.v(TAG, "handleStartActivity callerApp: " + callingPackage);
                    }
                    collectLastCallActivityInfo(calledPackageName, cpnClassName);
                    return false;
                } else if (!(!inActivityPushBlacklist(cpnClassName) || callingPackage == null || callingPackage.contains(calledPackageName))) {
                    if (this.DEBUG_SWITCH) {
                        Log.v(TAG, "handleStartActivity return undo!");
                    }
                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callingPackage, calledPackageName, cpnClassName, TYPE_NO_NEED, TYPE_NO_NEED, "forbid_push");
                    return true;
                }
                int interceptSwitchValue = OppoAppStartupManagerUtils.getInstance().getTencentInterceptSwitchValue();
                if (!(interceptSwitchValue <= 0 || this.mAms == null || callingPackage == null || calledPackageName == null || cpnClassName == null || intent == null)) {
                    if (callingPackage.equals(TENCENT_MM_PKG) && calledPackageName.equals(TENCENT_MM_PKG) && cpnClassName.equals(OppoAppStartupManagerUtils.getInstance().getTencentInterceptCpn())) {
                        if (this.DynamicDebug) {
                            Bundle bundle = intent.getExtras();
                            String string = "Bundle{";
                            for (String key : bundle.keySet()) {
                                string = string + " " + key + " => " + bundle.get(key) + ";";
                            }
                            Log.v(TAG, "handleStartActivity Bundle = " + (string + " }Bundle"));
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
                if (checkAbnormalActivityCall(intent, callingPackage, calledPackageName, cpnClassName, callingPid, callingUid)) {
                    if (this.DEBUG_SWITCH) {
                        Log.v(TAG, "handleStartActivity return checkAbnormalActivityCall undo!");
                    }
                    return true;
                }
                collectLastCallActivityInfo(calledPackageName, cpnClassName);
                return false;
            }
        }
        Log.v(TAG, "handleStartActivity(pkg): " + calledPackageName + " is forbidden to start");
        if (callingPackage != null) {
            if (callingPackage.equals(ActivityManagerService.OPPO_LAUNCHER)) {
                Log.d(TAG, calledPackageName + " is started from launcher: " + callingPackage);
                Message msg = Message.obtain();
                msg.what = 600;
                msg.obj = calledPackageName;
                this.mAms.mUiHandler.sendMessage(msg);
                if (this.mSwitchMonitor) {
                    this.mHandler.post(new CollectAppInterceptRunnable(TYPE_NO_NEED, calledPackageName, TYPE_NO_NEED, TYPE_DIALOG));
                }
                return true;
            }
        }
        if (this.mSwitchMonitor) {
            this.mHandler.post(new CollectAppInterceptRunnable(TYPE_NO_NEED, calledPackageName, TYPE_NO_NEED, TYPE_ACTIVITY));
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:165:0x043c, code:
            if (r31.contains("startActivityFromRecents") != false) goto L_0x043e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkAbnormalActivityCall(Intent intent, String callerPkg, String calledPkg, String cpnName, int callingPid, int callingUid) {
        if (!this.mSwitchInterceptActivity) {
            return false;
        }
        if (this.mAms == null || callerPkg == null || calledPkg == null || cpnName == null) {
            if (this.DEBUG_SWITCH) {
                Log.d(TAG, "checkAbnormalActivityCall callerPkg == null ||calledPkg == null ||cpnName == null return.");
            }
            return false;
        }
        long time = 0;
        if (this.DynamicDebug) {
            time = SystemClock.elapsedRealtime();
        }
        boolean isScreeOn = isScreenOn();
        ComponentName topCpn = getTopComponentName();
        if (topCpn == null) {
            if (this.DEBUG_SWITCH) {
                Log.d(TAG, "checkAbnormalActivityCall topCpn = null return.");
            }
            return false;
        }
        String topPkg = topCpn.getPackageName() != null ? topCpn.getPackageName() : IElsaManager.EMPTY_PACKAGE;
        if (inBlackguardActivityList(cpnName)) {
            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, TYPE_NO_NEED, TYPE_NO_NEED, "forbid_black");
            Log.v(TAG, "InterceptInfo " + callerPkg + "  " + calledPkg + "  " + cpnName + "  " + topPkg + "  " + isScreeOn);
            return true;
        }
        if (!(inActivityCallerWhitePkgList(callerPkg) || inActivityCalledWhitePkgList(calledPkg) || inActivityCalledWhiteCpnList(cpnName))) {
            if (!callerPkg.contains("com.android.cts.")) {
                if (!callerPkg.contains("android.cts.")) {
                    if (!(callerPkg.contains("com.google.android.xts.") || inActivityPkgKeyList(callerPkg) || inActivityPkgKeyList(calledPkg) || inCustomizeWhiteList(callerPkg) || inGlobalWhiteList(callerPkg) || this.mAms.mContext.getPackageManager().isFullFunctionMode())) {
                        if (isScreeOn && topPkg.equals(callerPkg)) {
                            if (this.DEBUG_SWITCH) {
                                Log.d(TAG, callerPkg + " callerPkg is FG!");
                            }
                            return false;
                        } else if ("android".equals(topPkg)) {
                            if (this.DEBUG_SWITCH) {
                                Log.d(TAG, callerPkg + " topPkg is android!");
                            }
                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_topIsAndroid");
                            return false;
                        } else if ("empty".equals(callerPkg)) {
                            if (this.DEBUG_SWITCH) {
                                Log.d(TAG, "checkAbnormalActivityCall callerPkg is empty return.");
                            }
                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsEmpty");
                            return false;
                        } else {
                            if (topPkg.equals("com.coloros.safecenter") && topCpn.getClassName() != null && topCpn.getClassName().contains("AppUnlock")) {
                                if (this.DEBUG_SWITCH) {
                                    Log.d(TAG, "checkAbnormalActivityCall safecenter applock return.");
                                }
                                OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_topIsApplock");
                                return false;
                            }
                            PackageInfo packageInfo = null;
                            PackageInfo calledPkgInfo = null;
                            try {
                                packageInfo = this.mAms.mContext.getPackageManager().getPackageInfo(callerPkg, 0);
                                calledPkgInfo = this.mAms.mContext.getPackageManager().getPackageInfo(calledPkg, 0);
                            } catch (NameNotFoundException e) {
                                e.printStackTrace();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                            if (packageInfo == null || packageInfo.applicationInfo == null) {
                                if (this.DEBUG_SWITCH) {
                                    Log.d(TAG, callerPkg + " callerPkg does not exits! return.");
                                }
                                return false;
                            } else if (calledPkgInfo == null || calledPkgInfo.applicationInfo == null) {
                                if (this.DEBUG_SWITCH) {
                                    Log.d(TAG, calledPkg + " calledPkg does not exits! return.");
                                }
                                return false;
                            } else if ((packageInfo.applicationInfo.flags & 1) != 0) {
                                if (this.DEBUG_SWITCH) {
                                    Log.d(TAG, callerPkg + " callerPkg is system app! return.");
                                }
                                OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsSystem");
                                return false;
                            } else {
                                if ((calledPkgInfo.applicationInfo.flags & 1) != 0) {
                                    if (!calledPkg.equals("com.android.browser")) {
                                        if (this.DEBUG_SWITCH) {
                                            Log.d(TAG, calledPkg + " calledPkg is system app! return.");
                                        }
                                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsSystem");
                                        return false;
                                    }
                                }
                                if (callerPkg != null && callerPkg.contains(calledPkg)) {
                                    String lowerCaseCpnName = cpnName.toLowerCase();
                                    for (String key : this.mActivityCalledKeyList) {
                                        if (lowerCaseCpnName.contains(key)) {
                                            if (this.DEBUG_SWITCH) {
                                                Log.d(TAG, "checkAbnormalActivityCall callingPackage contain key! return.");
                                            }
                                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledContainsKey");
                                            return false;
                                        }
                                    }
                                    if (isScreeOn) {
                                        if (isSameAsLastActivity(calledPkg)) {
                                            if (this.DEBUG_SWITCH) {
                                                Log.v(TAG, "checkAbnormalActivityCall return for special app from launcher! return.");
                                            }
                                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledSameLast");
                                            return false;
                                        } else if (isInputMethodApplication(calledPkg)) {
                                            if (this.DEBUG_SWITCH) {
                                                Log.v(TAG, "checkAbnormalActivityCall return for isInputMethod! return.");
                                            }
                                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsIm");
                                            return false;
                                        } else {
                                            String str = Log.getStackTraceString(new Throwable());
                                            if (this.DynamicDebug) {
                                                Log.d(TAG, str);
                                            }
                                            if (!str.contains("PendingIntentRecord")) {
                                            }
                                            if (this.DEBUG_SWITCH) {
                                                Log.d(TAG, "checkAbnormalActivityCall by PendingIntentRecord or startActivityFromRecents! return.");
                                            }
                                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_pendingOrRecent");
                                            return false;
                                        }
                                    }
                                }
                                if (isScreeOn) {
                                    String focusedPkg = getFocusedPkg();
                                    if (callerPkg.equals(focusedPkg)) {
                                        if (this.DEBUG_SWITCH) {
                                            Log.d(TAG, callerPkg + " callerPkg is focused! return.");
                                        }
                                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_focusIsCaller");
                                        return false;
                                    } else if (SYSTEM_UI_PKG.equals(focusedPkg)) {
                                        if (this.DEBUG_SWITCH) {
                                            Log.d(TAG, callerPkg + " systemui is focused! return.");
                                        }
                                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_focusIsSystemui");
                                        return false;
                                    } else if (OppoListManager.getInstance().isInstalledAppWidget(callerPkg)) {
                                        if (this.DEBUG_SWITCH) {
                                            Log.d(TAG, calledPkg + " callerPkg has widget! return.");
                                        }
                                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsWidget");
                                        return false;
                                    }
                                }
                                boolean isAllowFloat = OppoSafeDbReader.getInstance(this.mAms.mContext).isUserOpen(callerPkg);
                                if (this.DynamicDebug) {
                                    Log.d(TAG, callerPkg + "  debug for floatwindow app isUserOpen = " + isAllowFloat);
                                }
                                if (isAllowFloat) {
                                    if (this.DEBUG_SWITCH) {
                                        Log.d(TAG, calledPkg + " calledPkg is allowed keyguard app! return.");
                                    }
                                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsFloat");
                                    return false;
                                } else if (OppoListManager.getInstance().isFromNotifyPkg(callerPkg)) {
                                    if (this.DEBUG_SWITCH) {
                                        Log.d(TAG, calledPkg + " calledPkg is from notify! return.");
                                    }
                                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_callerIsNotify");
                                    return false;
                                } else if (OppoListManager.getInstance().isInAutoBootWhiteList(callerPkg)) {
                                    if (this.DEBUG_SWITCH) {
                                        Log.d(TAG, calledPkg + " calledPkg is allowed autoboot app! return.");
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
                                                            if (this.DEBUG_SWITCH) {
                                                                Log.v(TAG, "checkAbnormalActivityCall return for audio! return.");
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
                                        if (this.DEBUG_SWITCH) {
                                            Log.v(TAG, "checkAbnormalActivityCall return for accessibility! return.");
                                        }
                                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsAccessibility");
                                        return false;
                                    }
                                    String action = intent.getAction();
                                    String url = intent.getDataString();
                                    if (!(action == null || url == null || !action.equals("android.intent.action.VIEW") || !url.toLowerCase().startsWith("http") || OppoListManager.getInstance().isInBrowserWhiteList(callerPkg))) {
                                        if (calledPkg.equals("com.android.browser") && calledPkg.equals(callerPkg)) {
                                            if (this.DynamicDebug) {
                                                Log.d(TAG, callerPkg + "is OPPO browser and it's calling itself, return");
                                            }
                                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsBrowser");
                                            return false;
                                        } else if (isInputMethodApplication(callerPkg)) {
                                            if (this.DynamicDebug) {
                                                Log.d(TAG, callerPkg + " is starting a web view, we don't intercept it.");
                                            }
                                            OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsBrowser_Im");
                                            return false;
                                        } else {
                                            sendBroadcastForUrlIntercept(callerPkg, url, calledPkg, cpnName);
                                            Log.v(TAG, "Sending Broadcast for url intercept. third browser is called in Bg, undo!");
                                        }
                                    }
                                    if (action != null && url != null && action.equals("android.intent.action.VIEW") && url.toLowerCase().startsWith("http") && OppoListManager.getInstance().isInBrowserWhiteList(callerPkg)) {
                                        if (this.DEBUG_SWITCH) {
                                            Log.d(TAG, callerPkg + " with action= " + action + " is no restric to start a web view");
                                        }
                                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_calledIsBrowserWhite");
                                        return false;
                                    }
                                    ComponentName dockTopCpn = this.mAms.getDockTopAppName();
                                    if (!isScreeOn || dockTopCpn == null) {
                                        if (this.DynamicDebug) {
                                            Log.d(TAG, "handleStartAppInfo cost time ==  " + (SystemClock.elapsedRealtime() - time));
                                        }
                                        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "forbid_popup");
                                        Log.i(TAG, "InterceptInfo " + callerPkg + "  " + calledPkg + "  " + cpnName + "  " + topPkg + "  " + isScreeOn);
                                        return true;
                                    }
                                    if (this.DEBUG_SWITCH) {
                                        Log.d(TAG, callerPkg + ", dockTopPkg " + (dockTopCpn.getPackageName() != null ? dockTopCpn.getPackageName() : IElsaManager.EMPTY_PACKAGE));
                                    }
                                    OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_topIsSplitScreen");
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        OppoAppStartupStatistics.getInstance().collectPopupActivityInfo(callerPkg, calledPkg, cpnName, topPkg, String.valueOf(isScreeOn), "allow_white");
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "checkAbnormalActivityCall callerPkg or calledPkg or cpnName in whitelist return.");
        }
        return false;
    }

    private void sendBroadcastForUrlIntercept(String callingPkg, String url, String pkgName, String className) {
        if (this.mSwitchBrowserInterceptUpload) {
            Log.d(TAG, "sendBroadcastForUrlIntercept callingPkg = " + callingPkg + "  url = " + url + "  pkgName = " + pkgName + "  className= " + className);
            Intent intent = new Intent("android.intent.action.OPPO_SAFE_URL_INTERCEPTION");
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
            if (this.DynamicDebug) {
                Log.d(TAG, "isSameAsLastActivity lastPkg = " + lastPkg);
            }
            return true;
        } catch (Exception e) {
            Log.d(TAG, "isSameAsLastActivity exeption happen!");
            e.printStackTrace();
            return true;
        }
    }

    private boolean checkAccessibilityPkg(String callerPkg) {
        String accessStr = Secure.getString(this.mAms.mContext.getContentResolver(), "enabled_accessibility_services");
        if (accessStr == null || !accessStr.contains(callerPkg)) {
            return false;
        }
        if (this.DEBUG_SWITCH) {
            Log.v(TAG, "checkAccessibilityPkg callerPkg contain!");
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
                Log.e(TAG, "Failed to get default input method");
            }
        }
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "defaultInputMethod " + defaultInput);
        }
        return defaultInput;
    }

    private String getFocusedPkg() {
        String focusedPkg = null;
        if (!(this.mAms == null || this.mAms.mWindowManager == null)) {
            focusedPkg = this.mAms.mWindowManager.getFocusedWindowPkg();
        }
        if (this.DynamicDebug) {
            Log.v(TAG, "isFocusedActivity == " + focusedPkg);
        }
        return focusedPkg != null ? focusedPkg : IElsaManager.EMPTY_PACKAGE;
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
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return res;
        } catch (Throwable th) {
            return res;
        }
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

    public boolean inSeviceCpnlist(String cpnClassName) {
        boolean result;
        synchronized (this.mServiceLock) {
            result = this.mSeviceCpnBlacklist.contains(cpnClassName);
        }
        if (this.DynamicDebug) {
            Log.d(TAG, "inSeviceCpnlist result = " + result);
        }
        return result;
    }

    public boolean inReceiverlist(String cpnClassName) {
        boolean result;
        synchronized (this.mReceiverLock) {
            result = this.mReceiverBlackList.contains(cpnClassName);
        }
        if (this.DynamicDebug) {
            Log.d(TAG, "inReceiverlist result = " + result);
        }
        return result;
    }

    public boolean inReceiverActionlist(String action) {
        boolean result;
        synchronized (this.mReceiverActionLock) {
            result = this.mReceiverActionBlackList.contains(action);
        }
        if (this.DynamicDebug) {
            Log.d(TAG, "inReceiverActionlist result = " + result);
        }
        return result;
    }

    public boolean inProviderlist(String cpnClassName) {
        boolean result;
        synchronized (this.mProviderLock) {
            result = this.mProviderBlacklist.contains(cpnClassName);
        }
        if (this.DynamicDebug) {
            Log.d(TAG, "inProviderlist result = " + result);
        }
        return result;
    }

    public boolean inActivityPushBlacklist(String cpnClassName) {
        boolean result;
        synchronized (this.mActivityLock) {
            result = this.mActivityBlackList.contains(cpnClassName);
        }
        if (this.DynamicDebug) {
            Log.d(TAG, "inActivityPushBlacklist result = " + result);
        }
        return result;
    }

    public boolean inActionlist(String action) {
        boolean result;
        synchronized (this.mActionLock) {
            result = this.mActionBlackList.contains(action);
        }
        if (this.DynamicDebug) {
            Log.d(TAG, "inActionlist result = " + result);
        }
        return result;
    }

    public boolean inBlackguardList(String action) {
        boolean result;
        synchronized (this.mBlackguardLock) {
            result = this.mBlackguardList.contains(action);
        }
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "inBlackguardList result = " + result);
        }
        return result;
    }

    public boolean inActivityCallerWhitePkgList(String pkgName) {
        boolean result;
        synchronized (this.mActivityCallerWhitePkgLock) {
            result = this.mActivityCallerWhitePkgList.contains(pkgName);
        }
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "inActivityCallerWhitePkgList result = " + result);
        }
        return result;
    }

    public boolean inActivityCalledWhitePkgList(String pkgName) {
        boolean result;
        synchronized (this.mActivityCalledWhitePkgLock) {
            result = this.mActivityCalledWhitePkgList.contains(pkgName);
        }
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "inActivityCalledWhitePkgList result = " + result);
        }
        return result;
    }

    public boolean inActivityCalledWhiteCpnList(String pkgName) {
        boolean result;
        synchronized (this.mActivityCalledWhiteCpnLock) {
            result = this.mActivityCalledWhiteCpnList.contains(pkgName);
        }
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "inActivityCalledWhiteCpnList result = " + result);
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
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "inActivityPkgKeyList result = " + result);
        }
        return result;
    }

    public boolean inActivityCalledKeyList(String pkgName) {
        boolean result;
        synchronized (this.mActivityCalledKeyLock) {
            result = this.mActivityCalledKeyList.contains(pkgName);
        }
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "inActivityCalledKeyList result = " + result);
        }
        return result;
    }

    public boolean inBlackguardActivityList(String activityCpn) {
        boolean result;
        synchronized (this.mBlackguardActivityLock) {
            result = this.mBlackguardActivityList.contains(activityCpn);
        }
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "inBlackguardActivityList result = " + result);
        }
        return result;
    }

    public boolean inCustomizeWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mCustomizeWhiteLock) {
            result = this.mCustomizeWhiteList.contains(pkgName);
        }
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "inCustomizeWhiteList result = " + result);
        }
        return result;
    }

    public boolean inBuildBlackList(String pkgName) {
        boolean result;
        synchronized (this.mBuildAppBlackLock) {
            result = this.mBuildAppBlackList.contains(pkgName);
        }
        if (this.DynamicDebug) {
            Log.d(TAG, pkgName + " inBuildBlackList result = " + result);
        }
        return result;
    }

    public boolean inStartServiceWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mStartServiceWhiteLock) {
            result = this.mStartServiceWhiteList.contains(pkgName);
        }
        if (this.DynamicDebug) {
            Log.d(TAG, pkgName + " inStartServiceWhiteList result = " + result);
        }
        return result;
    }

    public boolean inStartServiceWhiteCpnList(ServiceRecord s) {
        boolean result = false;
        if (s.name != null) {
            synchronized (this.mStartServiceWhiteCpnLock) {
                result = this.mStartServiceWhiteCpnList.contains(s.name.getClassName());
            }
            if (this.DynamicDebug) {
                Log.d(TAG, s.name.getClassName() + " mStartServiceWhiteCpnList result = " + result);
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
        if (this.DynamicDebug) {
            Log.d(TAG, s.appInfo.packageName + " inBindServiceCpnWhiteList result = " + result);
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
        if (this.DynamicDebug) {
            Log.d(TAG, pkgName + " inJobWhiteList result = " + result);
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
        if (this.DynamicDebug) {
            Log.d(TAG, pkgName + " inSyncWhiteList result = " + result);
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
        if (this.DynamicDebug) {
            Log.d(TAG, pkgName + " inNotificationWhiteList result = " + result);
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
        if (this.DynamicDebug) {
            Log.d(TAG, cpn.getPackageName() + " inProviderCpnWhiteList result = " + result);
        }
        return result;
    }

    public boolean inBroadCastWhiteList(String pkgName) {
        boolean result;
        synchronized (this.mBroadcastWhiteLock) {
            result = this.mBroadcastWhiteList.contains(pkgName);
        }
        if (this.DynamicDebug) {
            Log.d(TAG, pkgName + " inBroadCastWhiteList result = " + result);
        }
        return result;
    }

    public boolean inBroadCastActionWhiteList(Intent intent) {
        boolean result = false;
        if (!(intent == null || intent.getAction() == null)) {
            synchronized (this.mBroadcastActionWhiteLock) {
                result = this.mBroadcastActionWhiteList.contains(intent.getAction());
            }
            if (this.DynamicDebug) {
                Log.d(TAG, intent.getAction() + " inBroadCastActionWhiteList result = " + result);
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
        if (this.DynamicDebug) {
            Log.d(TAG, pkgName + " inProtectWhiteList result = " + result);
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
        if (this.DynamicDebug) {
            Log.d(TAG, pkgName + " mCollectAppStartList result = " + result);
        }
        return result;
    }

    public void updateConfiglist() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateConfiglist!");
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
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateMonitorlist!");
        }
        updateMonitorAppStartList();
        clearMonitorList();
    }

    protected void updateAssociateStartList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateAssociateStartWhiteList!");
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
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateSeviceCpnBlacklist!");
        }
        synchronized (this.mServiceLock) {
            this.mSeviceCpnBlacklist.clear();
            this.mSeviceCpnBlacklist.addAll(OppoAppStartupManagerUtils.getInstance().getSeviceCpnBlacklist());
        }
    }

    private void updateReceiverBlackList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateReceiverBlackList!");
        }
        synchronized (this.mReceiverLock) {
            this.mReceiverBlackList.clear();
            this.mReceiverBlackList.addAll(OppoAppStartupManagerUtils.getInstance().getReceiverBlackList());
        }
    }

    private void updateReceiverActionBlackList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateReceiverActionBlackList!");
        }
        synchronized (this.mReceiverActionLock) {
            this.mReceiverActionBlackList.clear();
            this.mReceiverActionBlackList.addAll(OppoAppStartupManagerUtils.getInstance().getReceiverActionBlackList());
        }
    }

    private void updateProviderBlackList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateProviderBlackList!");
        }
        synchronized (this.mProviderLock) {
            this.mProviderBlacklist.clear();
            this.mProviderBlacklist.addAll(OppoAppStartupManagerUtils.getInstance().getProviderBlackList());
        }
    }

    private void updateActivityBlackList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateActivityBlackList!");
        }
        synchronized (this.mActivityLock) {
            this.mActivityBlackList.clear();
            this.mActivityBlackList.addAll(OppoAppStartupManagerUtils.getInstance().getActivityBlackList());
        }
    }

    private void updateActionBlackList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateActionBlackList!");
        }
        synchronized (this.mActionLock) {
            this.mActionBlackList.clear();
            this.mActionBlackList.addAll(OppoAppStartupManagerUtils.getInstance().getActionBlackList());
        }
    }

    private void updateBlackguardList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateBlackguardList!");
        }
        synchronized (this.mBlackguardLock) {
            this.mBlackguardList.clear();
            this.mBlackguardList.addAll(OppoAppStartupManagerUtils.getInstance().getBlackguardList());
        }
    }

    private void updateActivityCallerWhitePkgList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateActivityCallerWhitePkgList!");
        }
        synchronized (this.mActivityCallerWhitePkgLock) {
            this.mActivityCallerWhitePkgList.clear();
            this.mActivityCallerWhitePkgList.addAll(OppoAppStartupManagerUtils.getInstance().getActivityCallerWhitePkgList());
        }
    }

    private void updateActivityCalledWhitePkgList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateActivityCalledWhitePkgList!");
        }
        synchronized (this.mActivityCalledWhitePkgLock) {
            this.mActivityCalledWhitePkgList.clear();
            this.mActivityCalledWhitePkgList.addAll(OppoAppStartupManagerUtils.getInstance().getActivityCalledWhitePkgList());
        }
    }

    private void updateActivityCalledWhiteCpnList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateActivityCalledWhiteCpnList!");
        }
        synchronized (this.mActivityCalledWhiteCpnLock) {
            this.mActivityCalledWhiteCpnList.clear();
            this.mActivityCalledWhiteCpnList.addAll(OppoAppStartupManagerUtils.getInstance().getActivityCalledWhiteCpnList());
        }
    }

    private void updateActivityPkgKeyList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateActivityPkgKeyList!");
        }
        synchronized (this.mActivityPkgKeyLock) {
            this.mActivityPkgKeyList.clear();
            this.mActivityPkgKeyList.addAll(OppoAppStartupManagerUtils.getInstance().getActivityPkgKeyList());
        }
    }

    private void updateActivityCalledKeyList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateActivityCalledKeyList!");
        }
        synchronized (this.mActivityCalledKeyLock) {
            this.mActivityCalledKeyList.clear();
            this.mActivityCalledKeyList.addAll(OppoAppStartupManagerUtils.getInstance().getActivityCalledKeyList());
        }
    }

    private void updateBlackguardActivityList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateBlackguardActivityList!");
        }
        synchronized (this.mBlackguardActivityLock) {
            this.mBlackguardActivityList.clear();
            this.mBlackguardActivityList.addAll(OppoAppStartupManagerUtils.getInstance().getBlackguardActivityList());
        }
    }

    private void updateCustomizeWhiteList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateCustomizeWhiteList!");
        }
        synchronized (this.mCustomizeWhiteLock) {
            this.mCustomizeWhiteList.clear();
            this.mCustomizeWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getCustomizeWhiteList());
        }
    }

    private void updateGlobalWhiteList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateGlobalWhiteList!");
        }
        this.mGlobalWhiteList = OppoListManager.getInstance().getGlobalWhiteList(this.mAms.mContext);
    }

    private void updateMonitorAppStartList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateMonitorAppStartList!");
        }
        synchronized (this.mCollectAppStartLock) {
            this.mCollectAppStartList.clear();
            this.mCollectAppStartList.addAll(OppoAppStartupManagerUtils.getInstance().getCollectAppStartList());
        }
    }

    private void updateBuildBlackList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateBuildBlackList!");
        }
        synchronized (this.mBuildAppBlackLock) {
            this.mBuildAppBlackList.clear();
            this.mBuildAppBlackList.addAll(OppoAppStartupManagerUtils.getInstance().getBuildBlackList());
        }
    }

    private void updateStartServiceWhiteList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateStartServiceWhiteList!");
        }
        synchronized (this.mStartServiceWhiteLock) {
            this.mStartServiceWhiteList.clear();
            this.mStartServiceWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getStartServiceWhiteList());
        }
    }

    private void updateStartServiceWhiteCpnList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateStartServiceWhiteCpnList!");
        }
        synchronized (this.mStartServiceWhiteCpnLock) {
            this.mStartServiceWhiteCpnList.clear();
            this.mStartServiceWhiteCpnList.addAll(OppoAppStartupManagerUtils.getInstance().getStartServiceWhiteCpnList());
        }
    }

    private void updateBindServiceWhiteList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateBindServiceWhiteList!");
        }
        synchronized (this.mBindServiceCpnWhiteList) {
            this.mBindServiceCpnWhiteList.clear();
            this.mBindServiceCpnWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getBindServiceWhiteList());
        }
    }

    private void updateJobWhiteList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateJobWhiteList!");
        }
        synchronized (this.mJobWhiteLock) {
            this.mJobWhiteList.clear();
            this.mJobWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getJobWhiteList());
        }
    }

    private void updateSyncWhiteList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateSyncWhiteList!");
        }
        synchronized (this.mSyncWhiteLock) {
            this.mSyncWhiteList.clear();
            this.mSyncWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getSyncWhiteList());
        }
    }

    private void updateNotifyWhiteList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateNotifyWhiteList!");
        }
        synchronized (this.mNotifyWhiteLock) {
            this.mNotifyWhiteList.clear();
            this.mNotifyWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getNotifyWhiteList());
        }
    }

    private void updateProviderWhiteList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateProviderWhiteList!");
        }
        synchronized (this.mProviderWhiteLock) {
            this.mProviderCpnWhiteList.clear();
            this.mProviderCpnWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getProviderWhiteList());
        }
    }

    private void updateBroadcastWhiteList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateBroadcastWhiteList!");
        }
        synchronized (this.mBroadcastWhiteLock) {
            this.mBroadcastWhiteList.clear();
            this.mBroadcastWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getBroadcastWhiteList());
        }
    }

    private void updateBroadcastWhiteActionList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateBroadcastWhiteActionList!");
        }
        synchronized (this.mBroadcastActionWhiteLock) {
            this.mBroadcastActionWhiteList.clear();
            this.mBroadcastActionWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getBroadcastActionWhiteList());
        }
    }

    private void updateProtectList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateProtectList!");
        }
        synchronized (this.mProtectLock) {
            this.mProtectList.clear();
            this.mProtectList.addAll(OppoAppStartupManagerUtils.getInstance().getProtectList());
        }
    }

    private void updateAssociateStartWhiteList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateAssociateStartWhiteList!");
        }
        synchronized (this.mAssociateStartWhiteLock) {
            this.mAssociateStartWhiteList.clear();
            this.mAssociateStartWhiteList.addAll(OppoAppStartupManagerUtils.getInstance().getAssociateStartWhiteList());
        }
    }

    private void updateAuthorizeCpnList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updateAuthorizeCpnList!");
        }
        synchronized (this.mAuthorizeCpnListLock) {
            this.mAuthorizeCpnList.clear();
            this.mAuthorizeCpnList.addAll(OppoAppStartupManagerUtils.getInstance().getAuthorizeCpnList());
        }
    }

    private void updatePayCpnList() {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "updatePayCpnList!");
        }
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
            this.mHandler.post(new CollectAppInterceptRunnable(TYPE_NO_NEED, packageName, TYPE_NO_NEED, "service"));
        }
        Log.i(TAG, packageName + " is forbidden to start by service");
        return true;
    }

    protected boolean isAllowStartFromStartService(ProcessRecord callerApp, int callingPid, int callingUid, ServiceRecord s, Intent intent) {
        if (isExpVersion() || isRootOrShell(callingUid)) {
            return true;
        }
        if (inStartServiceWhiteList(s.appInfo.packageName)) {
            if (this.DynamicDebug) {
                Log.d(TAG, "inStartServiceWhiteList: " + s.appInfo.packageName);
            }
            return true;
        } else if (inStartServiceWhiteCpnList(s)) {
            if (this.DynamicDebug) {
                Log.d(TAG, "inStartServiceWhiteCpnList: " + s.name.getClassName());
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
                isAllowStart = isAllowStartFromService(callerApp, IElsaManager.EMPTY_PACKAGE, callingUid, s, intent, TYPE_START_SERVICE);
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
            if (this.DynamicDebug) {
                Log.d(TAG, "inBindServiceCpnWhiteList: " + s.appInfo.packageName + ", cpnn " + s.name.getClassName());
            }
            return true;
        } else if (callerApp != null || !START_PROCESS_FROM_JOB.equals(callerPkg) || !inJobWhiteList(s.appInfo.packageName)) {
            return isAllowStartFromService(callerApp, callerPkg, callingUid, s, intent, type);
        } else {
            if (this.DynamicDebug) {
                Log.d(TAG, "inJobWhiteList: " + s.appInfo.packageName);
            }
            return true;
        }
    }

    private boolean isAllowStartFromService(ProcessRecord callerApp, String callerPkg, int callingUid, ServiceRecord s, Intent intent, String type) {
        String calleePkg = s.appInfo.packageName;
        if (callerApp == null && (TYPE_START_SERVICE_FROM_ALARM.equals(type) || TYPE_START_SERVICE_CALL_NULL.equals(type))) {
            if (isDefaultAllowStart(s.appInfo) || inProtectWhiteList(calleePkg) || OppoListManager.getInstance().isInAutoBootWhiteList(calleePkg) || isInLruProcessesLocked(s.appInfo.uid)) {
                if (this.DEBUG_SWITCH) {
                    Log.i(TAG, "start " + calleePkg + " by service " + " callingUid " + callingUid + ", intent=" + intent + " type " + type);
                }
                return true;
            }
        } else if (TYPE_BIND_SERVICE_FROM_JOB.equals(type)) {
            if (isDefaultAllowStart(s.appInfo) || inProtectWhiteList(calleePkg) || OppoListManager.getInstance().isInAutoBootWhiteList(calleePkg)) {
                if (this.DEBUG_SWITCH) {
                    Log.i(TAG, "start " + calleePkg + " by service " + " callingUid " + callingUid + ", intent=" + intent + " type " + type);
                }
                return true;
            }
        } else if (isSameApplication(callerApp, callingUid, s.appInfo) || inAssociateStartWhiteList(calleePkg) || isDefaultAllowStart(s.appInfo) || inProtectWhiteList(calleePkg)) {
            if (this.DEBUG_SWITCH) {
                Log.i(TAG, "start " + calleePkg + " by service " + " callingUid " + callingUid + ", intent=" + intent + " type " + type);
            }
            return true;
        }
        if (this.DEBUG_SWITCH || this.mSwitchMonitor) {
            if (callerApp != null) {
                List<String> pkgList = getAppPkgList(callerApp);
                if (pkgList.size() > 0) {
                    callerPkg = (String) pkgList.get(0);
                }
            }
            ComponentName cpn = intent.getComponent();
            if (cpn != null) {
                if (this.mSwitchMonitor) {
                    this.mHandler.post(new CollectAppInterceptRunnable(callerPkg, calleePkg, cpn.getClassName(), type));
                }
                Log.i(TAG, "prevent start " + calleePkg + ", cmp " + cpn.toShortString() + " by " + callerPkg + " callingUid " + callingUid + ", Type " + type);
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
                    Log.i(TAG, "prevent start " + calleePkg + "s.name " + s.name + ", Intent { act=" + action + " } by " + callerPkg + " callingUid " + callingUid + ", Type " + type);
                }
            }
        }
        return false;
    }

    protected void collectAppStartBySystemUI(ProcessRecord callerApp, ServiceRecord s) {
        if (getAppPkgList(callerApp).contains(SYSTEM_UI_PKG) && (s.appInfo.flags & 1) == 0 && s.name != null) {
            if (this.mSwitchMonitor) {
                this.mHandler.post(new CollectAppInterceptRunnable(SYSTEM_UI_PKG, s.appInfo.packageName, s.name.getClassName(), TYPE_BIND_SERVICE_FROM_SYSTEMUI));
            }
            Log.i(TAG, "start " + s.appInfo.packageName + ", cpn " + s.name + " by systemui");
        }
    }

    protected boolean isAllowStartFromBroadCast(ProcessRecord callerApp, String callerPkg, int callingUid, Intent intent, ResolveInfo info) {
        if (isExpVersion() || isRootOrShell(callingUid)) {
            return true;
        }
        String packageName = info.activityInfo.applicationInfo.packageName;
        if (callerApp == null && (callerPkg.equals(START_PROCESS_FROM_ALARM) || callerPkg.equals(START_PROCESS_FROM_LOCATION))) {
            if (isDefaultAllowStart(info.activityInfo.applicationInfo) || OppoListManager.getInstance().isInAutoBootWhiteList(packageName) || inProtectWhiteList(packageName) || inBroadCastWhiteList(packageName) || inBroadCastActionWhiteList(intent) || isInLruProcessesLocked(info.activityInfo.applicationInfo.uid)) {
                if (this.DEBUG_SWITCH) {
                    Log.d(TAG, "start " + packageName + " by broadcast " + " callingUid " + callingUid + ", Intent=" + intent);
                }
                return true;
            }
        } else if (isSameApplication(callerApp, callingUid, info.activityInfo.applicationInfo) || isDefaultAllowStart(info.activityInfo.applicationInfo) || OppoListManager.getInstance().isInAutoBootWhiteList(packageName) || inProtectWhiteList(packageName) || inBroadCastWhiteList(packageName) || inBroadCastActionWhiteList(intent)) {
            if (this.DEBUG_SWITCH) {
                Log.d(TAG, "start " + packageName + " by broadcast " + " callingUid " + callingUid + ", Intent=" + intent);
            }
            return true;
        }
        if (this.DEBUG_SWITCH || this.mSwitchMonitor) {
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
                Log.i(TAG, "prevent start " + packageName + ", cmp " + cpn.toShortString() + " by broadcast " + callerPkg + " callingUid " + callingUid);
            } else {
                String action = intent.getAction();
                if (action != null) {
                    if (this.mSwitchMonitor) {
                        this.mHandler.post(new CollectAppInterceptRunnable(callerPkg, packageName, action, TYPE_BROADCAST_ACTION));
                    }
                    Log.i(TAG, "prevent start " + packageName + ", Intent { act=" + action + " } by broadcast " + callerPkg + " callingUid " + callingUid);
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
        } else if (!(OppoListManager.getInstance().isInAutoBootWhiteList(info.activityInfo.applicationInfo.packageName) || isDefaultAllowStart(info.activityInfo.applicationInfo))) {
            if (!isSameApplication(null, callingUid, info.activityInfo.applicationInfo)) {
                result = false;
            } else if (!isInLruProcessesLocked(info.activityInfo.applicationInfo.uid)) {
                result = false;
            }
        }
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "Broadcast caller pid " + callingPid + " uid " + callingUid + " intent=" + intent + ",applicationInfo=" + info.activityInfo.applicationInfo.packageName + " " + result);
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
            if (this.DEBUG_SWITCH) {
                Log.i(TAG, "start " + packageName + " by provider " + " callingUid " + callingUid + ", cpr=" + cpr.toShortString());
            }
            return true;
        } else if (isDefaultAllowStart(appInfo) || inAssociateStartWhiteList(packageName) || inProtectWhiteList(packageName) || inProviderCpnWhiteList(cpr.name)) {
            if (this.DEBUG_SWITCH) {
                Log.i(TAG, "start " + packageName + " by provider " + " callingUid " + callingUid + ", cpr=" + cpr.toShortString());
            }
            return true;
        } else {
            if (this.DEBUG_SWITCH || this.mSwitchMonitor) {
                String callerPkg = "unknow";
                if (callerApp != null) {
                    List<String> pkgList = getAppPkgList(callerApp);
                    if (pkgList.size() == 0) {
                        return true;
                    }
                    callerPkg = (String) pkgList.get(0);
                }
                ComponentName cpn = cpr.name;
                if (cpn != null) {
                    if (this.mSwitchMonitor) {
                        this.mHandler.post(new CollectAppInterceptRunnable(callerPkg, packageName, cpn.getClassName(), "provider"));
                    }
                    Log.i(TAG, "prevent start " + packageName + ", cmp " + cpn.toShortString() + " by contentprovider " + callerPkg + " callingUid " + callingUid);
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
            if (this.DEBUG_SWITCH) {
                Log.i(TAG, "start " + packageName + " by notification " + " " + rebindType + " ,cpn=" + component.toShortString());
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
        if (this.DEBUG_SWITCH) {
            if (result) {
                Log.i(TAG, "start " + packageName + " by notification " + " " + rebindType + " ,cpn=" + component.toShortString());
            } else {
                Log.i(TAG, "prevent start " + packageName + " by notification " + " " + rebindType + " ,cpn=" + component.toShortString());
                if (this.mSwitchMonitor) {
                    this.mHandler.post(new CollectAppInterceptRunnable(START_PROCESS_FROM_NOTIFICATION_LISTENER, packageName, component.getClassName(), TYPE_BIND_SERVICE_FORM_NOTIFICATION));
                }
            }
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
            if (this.DEBUG_SWITCH) {
                Log.i(TAG, extra + ", isParcelled=" + isParcelled + ", hasExtra=" + hasExtra);
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
        if (this.DynamicDebug && result) {
            Log.d(TAG, callingUid + " sameApplication");
        }
        return result;
    }

    private String composePackage(String pre, String suf) {
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
            if (this.DynamicDebug) {
                time = SystemClock.elapsedRealtime();
            }
            if (app.info != null && (app.info.flags & 1) == 0) {
                String packageName = app.info.packageName;
                String processName = app.processName;
                if (packageName != null && processName != null && hostingType != null) {
                    if (packageName != null && OppoListManager.getInstance().isAppStartForbidden(packageName)) {
                        this.mHandler.post(new CollectAppInterceptRunnable(TYPE_NO_NEED, packageName, hostingType, TYPE_START_PROCEESS_LOECKED));
                    }
                    if (inCollectAppStartList(packageName) && !isInLruProcessesLocked(app.uid)) {
                        String startMode = APP_START_BY_OTHER;
                        if (inAssociateStartWhiteList(packageName)) {
                            startMode = APP_START_BY_ASSOCIATE;
                        } else if (OppoListManager.getInstance().isInAutoBootWhiteList(packageName)) {
                            startMode = APP_START_BY_BOOTSTART;
                        } else if (packageName.equals(processName)) {
                            startMode = APP_START_BY_CLICK;
                        }
                        if (this.DynamicDebug) {
                            Log.d(TAG, "ams_collectAppStartInfo cost time=" + (SystemClock.elapsedRealtime() - time));
                        }
                        if (this.DynamicDebug) {
                            Log.i(TAG, Log.getStackTraceString(new Throwable()));
                        }
                        this.mHandler.post(new CollectAppStartRunnable(packageName, processName, hostingType, startMode, hostingNameStr));
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
        if (callerPkgName == null || calledPkgName == null) {
            if (this.DEBUG_SWITCH) {
                Log.d(TAG, "monitorAppStartInfo callerPkgName == null || calledPkgName == null");
            }
        } else if (callerPkgName.equals(calledPkgName)) {
            if (this.DynamicDebug) {
                Log.d(TAG, "monitorAppStartInfo callerPkgName is the same as calledPkgName!");
            }
        } else if (this.mSwitchMonitor) {
            handleAppMonitorInfo(callerPkgName, calledPkgName, callCpnName, callType);
        }
    }

    public void sendMonitorInfoNotify() {
        Intent intent = new Intent("android.intent.action.OPPO_STARTUP_APP_MONITOR");
        intent.putExtra(SoundModelContract.KEY_TYPE, "appcallinfo");
        intent.putStringArrayListExtra("data", new ArrayList(this.mMonitorAppUploadList));
        if (this.mAms != null) {
            this.mAms.mContext.sendBroadcast(intent);
        }
    }

    private void collectAppInterceptInfo(String callerPkg, String calledPkg, String callCpnName, String callType) {
        OppoAppMonitorInfo appMonitorInfo = getAppInfoInList(callerPkg);
        if (appMonitorInfo == null) {
            this.mMonitorAppInfoList.add(OppoAppMonitorInfo.builder(callerPkg, calledPkg, callCpnName, callType));
            return;
        }
        appMonitorInfo.increaseCallCount(calledPkg, callCpnName, callType);
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
            if (this.DEBUG_SWITCH) {
                Log.d(TAG, "mMonitorAppInfoList is empty!");
            }
            return;
        }
        if (this.DynamicDebug) {
            Log.d(TAG, "--------------------------notifyMonitorCallInfo start---------------------------");
        }
        int length = this.mMonitorAppInfoList.size();
        for (int i = 0; i < length; i++) {
            OppoAppMonitorInfo appInfo = (OppoAppMonitorInfo) this.mMonitorAppInfoList.get(i);
            if (appInfo != null) {
                this.mMonitorAppUploadList.addAll(appInfo.formatCallInfo());
            }
            if (this.DEBUG_SWITCH && appInfo != null) {
                appInfo.dumpInfo();
            }
            if (i == length - 1) {
                appInfo.clearCallList();
            }
        }
        if (this.DynamicDebug) {
            Log.d(TAG, "--------------------------notifyMonitorCallInfo end-----------------------------");
        }
        sendMonitorInfoNotify();
        this.mMonitorAppInfoList.clear();
        this.mMonitorAppUploadList.clear();
    }

    public void handleAppMonitorInfo(String callerPkgName, String calledPkgName, String callCpnName, String callType) {
        if (callCpnName == null) {
            callCpnName = "cpnUnknow";
            Log.d(TAG, "handleAppMonitorInfo cpnName is null.");
        }
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "handleAppMonitorInfo callerPkgName = " + callerPkgName + "  calledPkgName = " + calledPkgName + "  callType = " + callType + "  cpnName = " + callCpnName);
        }
        collectAppInterceptInfo(callerPkgName, calledPkgName, callCpnName, callType);
        if (this.mMonitorAppInfoList.size() >= OppoAppStartupManagerUtils.getInstance().getCallCheckCount()) {
            notifyAppInterceptInfo();
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0059, code:
            r0 = new com.android.server.am.OppoAppStartupStatistics.OppoCallActivityEntry(r5, r6);
     */
    /* JADX WARNING: Missing block: B:17:0x0065, code:
            if (r4.mLastCalledAcivityList.size() < 5) goto L_0x0076;
     */
    /* JADX WARNING: Missing block: B:18:0x0067, code:
            r4.mLastCalledAcivityList.remove(0);
            r4.mLastCalledAcivityList.add(r0);
     */
    /* JADX WARNING: Missing block: B:19:0x0072, code:
            return;
     */
    /* JADX WARNING: Missing block: B:23:0x0076, code:
            r4.mLastCalledAcivityList.add(r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void collectLastCallActivityInfo(String pkgName, String cpnName) {
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, "collectLastCallActivityInfo pkgName = " + pkgName);
            Log.d(TAG, "collectLastCallActivityInfo cpnName = " + cpnName);
        }
        if (pkgName == null || cpnName == null) {
            Log.d(TAG, "collectLastCallActivityInfo pkgName or cpnName is null!");
            return;
        }
        synchronized (this.mAuthorizeCpnListLock) {
            if (this.mAuthorizeCpnList.contains(cpnName)) {
                Log.d(TAG, "collectLastCallActivityInfo cpnName must be filter!");
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
        if (this.DynamicDebug) {
            Log.d(TAG, "getTopPkgName topCpn = " + topCpn);
        }
        return topCpn;
    }

    private boolean isScreenOn() {
        boolean ret = false;
        try {
            return Stub.asInterface(ServiceManager.getService("power")).isInteractive();
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting screen status", e);
            return ret;
        }
    }

    public void init(ActivityManagerService ams) {
        this.mAms = ams;
        initData();
        OppoAppStartupStatistics.getInstance().init(this, this.mHandler, ams);
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.DynamicDebug = on;
        this.DEBUG_SWITCH = DEBUG_DETAIL | this.DynamicDebug;
        OppoAppStartupManagerUtils.getInstance().setDynamicDebugSwitch();
    }

    public void openLog(boolean on) {
        Log.i(TAG, "#####openlog####");
        Log.i(TAG, "DynamicDebug = " + getInstance().DynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Log.i(TAG, "DynamicDebug = " + getInstance().DynamicDebug);
    }

    public void registerLogModule() {
        try {
            Log.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Log.i(TAG, "invoke " + cls);
            Class[] clsArr = new Class[1];
            clsArr[0] = String.class;
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", clsArr);
            Log.i(TAG, "invoke " + m);
            Object newInstance = cls.newInstance();
            Object[] objArr = new Object[1];
            objArr[0] = OppoAppStartupManager.class.getName();
            m.invoke(newInstance, objArr);
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

    protected boolean inAssociateStartWhiteList(String packageName) {
        boolean result;
        synchronized (this.mAssociateStartWhiteLock) {
            if (this.mAssociateStartWhiteList.isEmpty()) {
                result = OppoAppStartupListManager.getInstance().isInAssociateWhiteList(packageName);
            } else {
                result = this.mAssociateStartWhiteList.contains(packageName);
            }
        }
        if (result && this.DEBUG_SWITCH) {
            Log.i(TAG, packageName + " in AssociateStartWhiteList");
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
                isCtsRunning = this.mAms.mContext.getPackageManager().isFullFunctionMode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.DEBUG_SWITCH) {
            Log.v(TAG, "isCtsRunning:" + isCtsRunning);
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
            if (this.DynamicDebug) {
                Log.d(TAG, "--------------------------uploadProcessStartInfo start---------------------------");
            }
            List<Map<String, String>> uploadList = new ArrayList();
            int length = this.mCollectProcessInfoList.size();
            for (int i = 0; i < length; i++) {
                OppoAppMonitorInfo appInfo = (OppoAppMonitorInfo) this.mCollectProcessInfoList.get(i);
                if (appInfo != null) {
                    uploadList.addAll(appInfo.getProcessStartMap());
                }
                if (this.DEBUG_SWITCH && appInfo != null) {
                    appInfo.dumpProcessStartInfo();
                }
                if (i == length - 1) {
                    appInfo.clearProcessStartList();
                }
            }
            if (this.DynamicDebug) {
                Log.d(TAG, "--------------------------uploadProcessStartInfo end-----------------------------");
            }
            if (this.mAms != null) {
                OppoStatistics.onCommon(this.mAms.mContext, UPLOAD_LOGTAG, UPLOAD_THIRD_EVENTID, uploadList, false);
            }
            this.mCollectProcessInfoList.clear();
        }
    }

    private void uploadBlackListIntercept() {
        if (!this.mCollectBlackListInterceptList.isEmpty()) {
            if (this.DEBUG_SWITCH) {
                Log.d(TAG, "--------------------------uploadBlackListIntercept start---------------------------");
            }
            List<Map<String, String>> uploadList = new ArrayList();
            int length = this.mCollectBlackListInterceptList.size();
            for (int i = 0; i < length; i++) {
                OppoAppMonitorInfo appInfo = (OppoAppMonitorInfo) this.mCollectBlackListInterceptList.get(i);
                if (appInfo != null) {
                    uploadList.addAll(appInfo.getBlackListInterceptMap());
                }
                if (this.DEBUG_SWITCH && appInfo != null) {
                    appInfo.dumpBlackListIntercep();
                }
                if (i == length - 1) {
                    appInfo.clearBlackListInterceptList();
                }
            }
            if (this.DEBUG_SWITCH) {
                Log.d(TAG, "--------------------------uploadBlackListIntercept end-----------------------------");
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
            if (this.DynamicDebug) {
                Log.d(TAG, "--------------------------uploadGamePay start---------------------------");
            }
            List<Map<String, String>> uploadList = new ArrayList();
            int length = this.mCollectGamePayList.size();
            for (int i = 0; i < length; i++) {
                OppoAppMonitorInfo appInfo = (OppoAppMonitorInfo) this.mCollectGamePayList.get(i);
                if (appInfo != null) {
                    uploadList.addAll(appInfo.getGamePayMap());
                    if (this.DEBUG_SWITCH) {
                        appInfo.dumpGamePay();
                    }
                }
                if (i == length - 1) {
                    appInfo.clearGamePay();
                }
            }
            if (this.DynamicDebug) {
                Log.d(TAG, "--------------------------uploadGamePay end-----------------------------");
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
            if (this.DynamicDebug) {
                time = SystemClock.elapsedRealtime();
            }
            if (appInfo == null || !inCollectAppStartList(appInfo.packageName)) {
                return;
            }
            if (isInLruProcessesLocked(appInfo.uid)) {
                if (this.DynamicDebug) {
                    Log.d(TAG, "handleProcessStartupInfo1 cost time = " + (SystemClock.elapsedRealtime() - time));
                }
                return;
            }
            if (this.DynamicDebug) {
                Log.d(TAG, "handleProcessStartupInfo2 cost time = " + (SystemClock.elapsedRealtime() - time));
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
        if (this.DynamicDebug) {
            Log.d(TAG, "remove pendingJob  " + packageName + " result " + result);
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
        if (this.DEBUG_SWITCH) {
            Log.d(TAG, packageName + " isFromControlCenterPkg result " + result);
        }
        return result;
    }

    public void handleForceFinish() {
        if (!(this.mActivityStack == null || this.mPreRecord == null)) {
            this.mActivityStack.requestFinishActivityLocked(this.mPreRecord.appToken, 0, null, "resume-exception2", true);
            if (this.DynamicDebug) {
                Log.d(TAG, "handleForceFinish activityRecord " + this.mPreRecord);
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
}
