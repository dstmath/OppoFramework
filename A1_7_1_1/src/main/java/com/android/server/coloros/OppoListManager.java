package com.android.server.coloros;

import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.FileObserver;
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
import com.android.server.am.OppoCrashClearManager;
import com.android.server.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.oppo.IElsaManager;
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
public class OppoListManager {
    private static final String ACCOUNTSYNC_MANGER_FILE = "/data/oppo/boot/autoSyncWhiteList.txt";
    private static final String ACCOUNTSYNC_MANGER_PATH = "/data/oppo/boot";
    private static final String ALLOW_MANIFEST_NET_BRO_TAG = "AllowManifestNetBro";
    private static final String AUTOBOOT_MANGER_FILE = "/data/oppo/boot/bootallow.txt";
    private static final String AUTOBOOT_MANGER_PATH = "/data/oppo/boot";
    private static final String BACK_CLIP_INTERCEPT_WHITE_LIST = "BackClipInterceptWhiteList";
    private static final String BACK_KEY_FILTER_LIST = "BackKeyFilterList";
    private static final String BACK_KEY_KILL_SWITCH = "BackKeyKillSwitch";
    private static final String BROWSER_WHITE_LIST_FILE = "/data/oppo/boot/browserWhiteList.txt";
    private static final String DCIMPROTECT_MANGER_FILE = "/data/oppo/permission/oppo_dcim_allow.txt";
    private static final String DCIMPROTECT_MANGER_PATH = "/data/oppo/permission";
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
    private static final ArrayList<String> FILTER_SEPCIAL_SECURE_APP = null;
    private static final String FORBIDDEN_TAG = "startForbidden";
    private static final String GLOBAL_CMCC_PKG = "GlobalCmccPkg";
    private static final String GLOBAL_CMCC_PROCESS = "GlobalCmccProcess";
    private static final String KILL_RESTART_SERVICE_PRO = "KillRestartServiceProNew";
    private static final int LIST_LENGTH_INIT_NUM = 300;
    private static final String MAX_VERSION_CODE = "maxCode";
    private static final String MINI_PROGRAM_KEY = "miniprogram";
    private static final int MINI_PROGRAM_MAX_COUNT = 20;
    private static final String MIN_VERSION_CODE = "minCode";
    private static final int MSG_HANDLE_APP_FROM_CONTROL_CENTER = 10;
    private static final int MSG_HANDLE_APP_FROM_NOTITY = 11;
    private static final int MSG_LOAD_SYSTEM_APP = 1;
    private static final String NOTIFICATION_SERVICE_TAG = "NotificationServiceApp";
    private static final String OPPO_ROM_BLACK_LIST_FILE = "data/system/sys_rom_black_list.xml";
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
    private static final String SYSTEM_CONFIG_LIST_FILE = "/data/system/config/systemConfigList.xml";
    private static final String TAG = "OppoListManager";
    private static final String TAG_DO_CLEAR_REDUNDENT_TASK = "ClearRedundentTaskSwitch";
    private static final String TAG_PREVENT_REDUNDENT_START = "PreventRedundentStart";
    private static final String TAG_REDUNDENT_TASK_CLASS = "RedundentTaskClass";
    private static final Object mCustomLock = null;
    private static final List<String> mDefaultBackClipInterceptWhiteList = null;
    private static final List<String> mDefaultBackClipInterceptWhiteListExp = null;
    private static final List<String> mDefaultBackKeyFilterList = null;
    private static final List<String> mDefaultBackKeyFilterListExp = null;
    private static final List<String> mDefaultCmccPkgList = null;
    private static final List<String> mDefaultCmccProcessList = null;
    private static final List<String> mDefaultKillRestartServiceProList = null;
    private static final List<String> mDefaultKillRestartServiceProListExp = null;
    private static final List<String> mDefaultProtectSelfWhiteList = null;
    private static final List<String> mDefaultRedundentTaskClassList = null;
    private static final List<String> mDefaultRemoveTaskFilterPkgList = null;
    private static final List<String> mDefaultRemoveTaskFilterProListExp = null;
    private static final List<String> mDefaultRemoveTaskFilterProcessList = null;
    private static OppoListManager mOppoListManager;
    private static final List<String> mProtectFromPkgWhiteList = null;
    private static final Object mSFLock = null;
    private final String CUSTOMIZE_APP_PATH;
    private boolean DEBUG_SWITCH;
    private List<String> mAccountSyncWhiteList;
    private FileObserverPolicy mAccountSyncWhiteListFileObserver;
    private final Object mAccountSyncWhiteListLock;
    private List<String> mAllowManifestNetBroList;
    private FileObserverPolicy mAutoBootFileObserver;
    private List<String> mAutoBootWhiteList;
    private final Object mAutoBootWhiteListLock;
    private List<String> mBackClipInterceptWhiteList;
    private List<String> mBackKeyFilterList;
    private boolean mBackKeyInitFromFile;
    private boolean mBackKeyKillSwitch;
    private FileObserverPolicy mBrowserInterceptFileObserver;
    private List<String> mBrowserWhiteList;
    private final Object mBrowserWhiteListLock;
    private Context mContext;
    private List<String> mCustomAppList;
    private FileObserverPolicy mDCIMProtectAllowFileObserver;
    private List<String> mDCIMProtectAllowList;
    private final Object mDCIMProtectAllowListLock;
    private List<String> mDefaultAccountSyncWhiteList;
    private List<String> mDefaultAllowManifestNetBroList;
    private List<String> mDefaultDCIMProtectAllowList;
    private List<String> mDefaultNotiServiceAppList;
    private List<String> mDefaultProtectForeList;
    private List<String> mDefaultProtectForeNetList;
    private List<String> mDefaultSecurePayActivityList;
    private List<String> mDefaultWhiteList;
    private boolean mDoClearRedundentSwitch;
    private boolean mDoPreventRedundentStartSwitch;
    private boolean mExpVersion;
    private List<String> mGlobalCmccPkgList;
    private List<String> mGlobalCmccProcessList;
    private Handler mHandler;
    private List<String> mKillRestartServiceProList;
    private ArrayMap<String, String> mMiniProgramShareMap;
    private final Object mMiniProgramShareMapLock;
    private List<String> mNotificationServiceApp;
    private List<String> mProtectForeList;
    private List<String> mProtectForeNetList;
    private List<ProtectSelfInfo> mProtectSelfInfos;
    private long mProtectSelfTimeout;
    private List<String> mProtectSelfWhiteList;
    private List<String> mRedundentTaskClassList;
    private List<String> mRemoveTaskFilterPkgList;
    private List<String> mRemoveTaskFilterProcessList;
    private List<String> mSecurePayActivityList;
    private ArrayMap<String, List<String>> mStartForbiddenList;
    private FileObserverPolicy mStartForbiddenObserver;
    private List<String> mStartFromControlCenterPkgList;
    private final Object mStartFromControlCenterPkgLock;
    private List<String> mStartFromNotifyPkgList;
    private final Object mStartFromNotifyPkgLock;
    private HashSet<String> mSystemAppList;
    private final Object mSystemAppListLock;
    private FileObserverPolicy mSystemConfigFileObserver;
    private final Object mSystemConfigListLock;
    private boolean mUploadSelfProtectSwitch;

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8) {
                if (this.focusPath.equals(OppoListManager.AUTOBOOT_MANGER_FILE)) {
                    Log.i(OppoListManager.TAG, "focusPath AUTOBOOT_MANGER_FILE!");
                    OppoListManager.this.readAutoBootListFile();
                }
                if (this.focusPath.equals(OppoListManager.BROWSER_WHITE_LIST_FILE)) {
                    Log.i(OppoListManager.TAG, "focusPath BROWSER_WHITE_LIST_FILE!");
                    OppoListManager.this.readBrowserWhiteListFile();
                }
                if (this.focusPath.equals(OppoListManager.SYSTEM_CONFIG_LIST_FILE)) {
                    Log.i(OppoListManager.TAG, "/data/system/config/systemConfigList.xml changed!");
                    synchronized (OppoListManager.this.mSystemConfigListLock) {
                        OppoListManager.this.readSystemConfigListLocked();
                    }
                }
                if (this.focusPath.equals(OppoListManager.OPPO_ROM_BLACK_LIST_FILE)) {
                    Log.i(OppoListManager.TAG, "data/system/sys_rom_black_list.xml changed!");
                    synchronized (OppoListManager.mSFLock) {
                        OppoListManager.this.readRomListFileLocked();
                    }
                }
                if (this.focusPath.equals(OppoListManager.DCIMPROTECT_MANGER_FILE)) {
                    Log.i(OppoListManager.TAG, "focusPath DCIMPROTECT_MANGER_FILE!");
                    OppoListManager.this.readDCIMProtectAllowListFile();
                }
                if (this.focusPath.equals(OppoListManager.ACCOUNTSYNC_MANGER_FILE)) {
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
        String fromPkg;
        String packageName;
        long startTime;
        long timeout;

        ProtectSelfInfo() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.coloros.OppoListManager.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.coloros.OppoListManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.coloros.OppoListManager.<clinit>():void");
    }

    public OppoListManager() {
        this.DEBUG_SWITCH = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        this.mAutoBootWhiteListLock = new Object();
        this.mAutoBootWhiteList = new ArrayList();
        this.mAutoBootFileObserver = null;
        this.mBrowserInterceptFileObserver = null;
        this.mBrowserWhiteList = new ArrayList();
        this.mBrowserWhiteListLock = new Object();
        String[] strArr = new String[11];
        strArr[0] = "com.sohu.inputmethod.sogou";
        strArr[1] = "com.oppo.mp_battery_autotest";
        strArr[2] = "com.oppo.autotest.agingautotesttool";
        strArr[3] = "com.oppo.PhenixTestServer";
        strArr[4] = "com.oppo.networkautotest";
        strArr[5] = "com.oppo.community";
        strArr[6] = "com.nearme.note";
        strArr[7] = "com.tencent.tvoem";
        strArr[8] = "com.oppo.ubeauty";
        strArr[9] = "com.facebook.orca";
        strArr[10] = "com.android.email";
        this.mDefaultWhiteList = Arrays.asList(strArr);
        this.mSystemConfigFileObserver = null;
        this.mProtectForeList = new ArrayList();
        this.mProtectForeNetList = new ArrayList();
        this.mSystemConfigListLock = new Object();
        strArr = new String[3];
        strArr[0] = "com.tencent.mm";
        strArr[1] = "com.tencent.mobileqq";
        strArr[2] = "com.tencent.tmgp.sgame";
        this.mDefaultProtectForeList = Arrays.asList(strArr);
        strArr = new String[1];
        strArr[0] = "com.tencent.tmgp.sgame";
        this.mDefaultProtectForeNetList = Arrays.asList(strArr);
        this.mSecurePayActivityList = new ArrayList();
        strArr = new String[2];
        strArr[0] = "com.tencent.mm.plugin.wallet.pay.ui.WalletPayUI";
        strArr[1] = "com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI";
        this.mDefaultSecurePayActivityList = Arrays.asList(strArr);
        this.mNotificationServiceApp = new ArrayList();
        strArr = new String[6];
        strArr[0] = "com.tencent.mm";
        strArr[1] = "com.tencent.research.drop";
        strArr[2] = "com.letv.android.client";
        strArr[3] = "com.qiyi.video";
        strArr[4] = "com.sohu.sohuvideo";
        strArr[5] = "com.tencent.qqlive";
        this.mDefaultNotiServiceAppList = Arrays.asList(strArr);
        this.mAllowManifestNetBroList = new ArrayList();
        strArr = new String[5];
        strArr[0] = "com.nearme.romupdate";
        strArr[1] = "com.coloros.sau";
        strArr[2] = "com.coloros.sauhelper";
        strArr[3] = "com.oppo.ota";
        strArr[4] = "com.nearme.statistics.rom";
        this.mDefaultAllowManifestNetBroList = Arrays.asList(strArr);
        this.mStartForbiddenList = new ArrayMap();
        this.mStartForbiddenObserver = null;
        this.mDCIMProtectAllowListLock = new Object();
        this.mDCIMProtectAllowList = new ArrayList();
        strArr = new String[5];
        strArr[0] = "com.android.providers.media";
        strArr[1] = "com.oppo.camera";
        strArr[2] = "com.coloros.gallery3d";
        strArr[3] = "com.android.cts.writeexternalstorageapp";
        strArr[4] = "android.provider.cts";
        this.mDefaultDCIMProtectAllowList = Arrays.asList(strArr);
        this.mDCIMProtectAllowFileObserver = null;
        this.mGlobalCmccPkgList = new ArrayList();
        this.mGlobalCmccProcessList = new ArrayList();
        this.mStartFromNotifyPkgList = new ArrayList();
        this.mStartFromNotifyPkgLock = new Object();
        this.mStartFromControlCenterPkgList = new ArrayList();
        this.mStartFromControlCenterPkgLock = new Object();
        this.mAccountSyncWhiteListLock = new Object();
        this.mAccountSyncWhiteList = new ArrayList();
        strArr = new String[2];
        strArr[0] = "com.android.email";
        strArr[1] = "com.google.android.gm";
        this.mDefaultAccountSyncWhiteList = Arrays.asList(strArr);
        this.mAccountSyncWhiteListFileObserver = null;
        this.CUSTOMIZE_APP_PATH = "/system/etc/oppo_customize_whitelist.xml";
        this.mCustomAppList = new ArrayList();
        this.mRemoveTaskFilterPkgList = new ArrayList();
        this.mRemoveTaskFilterProcessList = new ArrayList();
        this.mKillRestartServiceProList = new ArrayList();
        this.mRedundentTaskClassList = new ArrayList();
        this.mDoClearRedundentSwitch = true;
        this.mDoPreventRedundentStartSwitch = false;
        this.mBackKeyFilterList = new ArrayList();
        this.mBackKeyKillSwitch = true;
        this.mBackKeyInitFromFile = false;
        this.mBackClipInterceptWhiteList = new ArrayList();
        this.mExpVersion = false;
        this.mHandler = null;
        this.mSystemAppList = new HashSet(300);
        this.mSystemAppListLock = new Object();
        this.mMiniProgramShareMapLock = new Object();
        this.mMiniProgramShareMap = new ArrayMap();
        this.mProtectSelfInfos = new ArrayList();
        this.mProtectSelfTimeout = DEFAULT_PROTECT_SELF_TIMEOUT;
        this.mUploadSelfProtectSwitch = true;
        this.mProtectSelfWhiteList = new ArrayList();
        initDir();
        initFileObserver();
        readAutoBootListFile();
        readBrowserWhiteListFile();
        synchronized (this.mSystemConfigListLock) {
            readSystemConfigListLocked();
        }
        synchronized (mSFLock) {
            readRomListFileLocked();
        }
        readDCIMProtectAllowListFile();
        readAccountSyncWhiteListFile();
        synchronized (mCustomLock) {
            readCustomAppListFile();
        }
    }

    public static OppoListManager getInstance() {
        if (mOppoListManager == null) {
            mOppoListManager = new OppoListManager();
        }
        return mOppoListManager;
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
    }

    private void initDir() {
        File autobootPath = new File("/data/oppo/boot");
        File autobootFile = new File(AUTOBOOT_MANGER_FILE);
        File browserWhiteListFile = new File(BROWSER_WHITE_LIST_FILE);
        File systemConfigListFile = new File(SYSTEM_CONFIG_LIST_FILE);
        File romBlackListFile = new File(OPPO_ROM_BLACK_LIST_FILE);
        File dcimProtectPath = new File(DCIMPROTECT_MANGER_PATH);
        File dcimProtectFile = new File(DCIMPROTECT_MANGER_FILE);
        File accountSyncWhiteListPath = new File("/data/oppo/boot");
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
            }
            if (!dcimProtectPath.exists()) {
                dcimProtectPath.mkdirs();
            }
            if (!dcimProtectFile.exists()) {
                dcimProtectFile.createNewFile();
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

    private void initFileObserver() {
        this.mAutoBootFileObserver = new FileObserverPolicy(AUTOBOOT_MANGER_FILE);
        this.mAutoBootFileObserver.startWatching();
        this.mBrowserInterceptFileObserver = new FileObserverPolicy(BROWSER_WHITE_LIST_FILE);
        this.mBrowserInterceptFileObserver.startWatching();
        this.mSystemConfigFileObserver = new FileObserverPolicy(SYSTEM_CONFIG_LIST_FILE);
        this.mSystemConfigFileObserver.startWatching();
        this.mStartForbiddenObserver = new FileObserverPolicy(OPPO_ROM_BLACK_LIST_FILE);
        this.mStartForbiddenObserver.startWatching();
        this.mDCIMProtectAllowFileObserver = new FileObserverPolicy(DCIMPROTECT_MANGER_FILE);
        this.mDCIMProtectAllowFileObserver.startWatching();
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
        if (pkg == null || pkg.equals(IElsaManager.EMPTY_PACKAGE)) {
            return false;
        }
        boolean contains;
        synchronized (this.mSystemConfigListLock) {
            contains = getProtectForeList().contains(pkg);
        }
        return contains;
    }

    public boolean inProtectForeNetList(String pkg) {
        if (pkg == null || pkg.equals(IElsaManager.EMPTY_PACKAGE)) {
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
        } else if (type == 2) {
            list.addAll(getGlobalCmccWhiteList(context));
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

    /* JADX WARNING: Removed duplicated region for block: B:217:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x021f A:{SYNTHETIC, Splitter: B:75:0x021f} */
    /* JADX WARNING: Removed duplicated region for block: B:219:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x02a0 A:{SYNTHETIC, Splitter: B:92:0x02a0} */
    /* JADX WARNING: Removed duplicated region for block: B:221:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x0323 A:{SYNTHETIC, Splitter: B:109:0x0323} */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x0399 A:{SYNTHETIC, Splitter: B:124:0x0399} */
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
                            if (!packgeName.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mProtectForeList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked protect fore pkg = " + packgeName);
                                }
                            }
                        } else if (PROTECT_FORE_NET_TAG.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mProtectForeNetList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked protect fore net pkg = " + packgeName);
                                }
                            }
                        } else if (SECUREPAY_ACTIVITY_TAG.equals(tagName)) {
                            activityName = parser.nextText();
                            if (!activityName.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mSecurePayActivityList.add(activityName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked securepay activity = " + activityName);
                                }
                            }
                        } else if (NOTIFICATION_SERVICE_TAG.equals(tagName)) {
                            activityName = parser.nextText();
                            if (!activityName.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mNotificationServiceApp.add(activityName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked notifation service activity = " + activityName);
                                }
                            }
                        } else if (ALLOW_MANIFEST_NET_BRO_TAG.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mAllowManifestNetBroList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked allow manifest net bro pkg = " + packgeName);
                                }
                            }
                        } else if (GLOBAL_CMCC_PKG.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mGlobalCmccPkgList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked global cmcc white pkg = " + packgeName);
                                }
                            }
                        } else if (GLOBAL_CMCC_PROCESS.equals(tagName)) {
                            String processName = parser.nextText();
                            if (!processName.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mGlobalCmccProcessList.add(processName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked global cmcc white process = " + processName);
                                }
                            }
                        } else if (REMOVE_TASK_FILTER_PKG_LIST.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mRemoveTaskFilterPkgList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked remove task filter pkg = " + packgeName);
                                }
                            }
                        } else if (REMOVE_TASK_FILTER_PROCESS_LIST.equals(tagName)) {
                            String process = parser.nextText();
                            if (!process.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mRemoveTaskFilterProcessList.add(process);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked remove task filter process = " + process);
                                }
                            }
                        } else if (KILL_RESTART_SERVICE_PRO.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mKillRestartServiceProList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked kill service restart pkg = " + packgeName);
                                }
                            }
                        } else if (TAG_REDUNDENT_TASK_CLASS.equals(tagName)) {
                            String className = parser.nextText();
                            if (!className.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mRedundentTaskClassList.add(className);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked redundent class = " + className);
                                }
                            }
                        } else if (TAG_DO_CLEAR_REDUNDENT_TASK.equals(tagName)) {
                            this.mDoClearRedundentSwitch = Boolean.parseBoolean(parser.nextText());
                        } else if (TAG_PREVENT_REDUNDENT_START.equals(tagName)) {
                            this.mDoPreventRedundentStartSwitch = Boolean.parseBoolean(parser.nextText());
                        } else if (BACK_KEY_FILTER_LIST.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mBackKeyFilterList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked back_key filter pkg = " + packgeName);
                                }
                            }
                        } else if (BACK_KEY_KILL_SWITCH.equals(tagName)) {
                            this.mBackKeyKillSwitch = Boolean.parseBoolean(parser.nextText());
                            this.mBackKeyInitFromFile = true;
                        } else if (PROTECT_SELF_WHITE_LIST.equals(tagName)) {
                            packgeName = parser.nextText();
                            if (!packgeName.equals(IElsaManager.EMPTY_PACKAGE)) {
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
                            if (!packgeName.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mBackClipInterceptWhiteList.add(packgeName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readSystemConfigListLocked back clip white pkg = " + packgeName);
                                }
                            }
                        }
                    }
                } while (type != 1);
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
                e5 = e8;
                fileInputStream = fileInputStream2;
                Log.e(TAG, "failed IOException ", e5);
                if (fileInputStream == null) {
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e52) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e52);
                    }
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
        if (pkg != null && !pkg.equals(IElsaManager.EMPTY_PACKAGE) && fromPkg != null && !fromPkg.equals(IElsaManager.EMPTY_PACKAGE)) {
            if (SystemProperties.getBoolean("persist.selfprotect.test", false) || getProtectSelfWhiteList().contains(pkg) || mProtectFromPkgWhiteList.contains(fromPkg)) {
                if (!mProtectFromPkgWhiteList.contains(fromPkg) && timeout > this.mProtectSelfTimeout) {
                    timeout = this.mProtectSelfTimeout;
                }
                removeStageProtectInfoInternal(pkg, fromPkg, false);
                synchronized (this.mProtectSelfInfos) {
                    long now = SystemClock.elapsedRealtime();
                    ProtectSelfInfo info = new ProtectSelfInfo();
                    info.packageName = pkg;
                    info.fromPkg = fromPkg;
                    info.startTime = now;
                    info.timeout = timeout;
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
        if (pkg != null && !pkg.equals(IElsaManager.EMPTY_PACKAGE) && fromPkg != null && !fromPkg.equals(IElsaManager.EMPTY_PACKAGE)) {
            synchronized (this.mProtectSelfInfos) {
                Iterator<ProtectSelfInfo> sListIterator = this.mProtectSelfInfos.iterator();
                while (sListIterator.hasNext()) {
                    ProtectSelfInfo info = (ProtectSelfInfo) sListIterator.next();
                    if (!(info == null || info.packageName == null || !info.packageName.equals(pkg) || info.fromPkg == null || !info.fromPkg.equals(fromPkg))) {
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
                long from = now - info.startTime;
                if (from < info.timeout) {
                    tempList.add(info.packageName);
                } else {
                    sListIterator.remove();
                    uploadProtecetSelfInfo(EVENTID_TIMEOUT_PROTECT_INFO, info);
                    if (this.DEBUG_SWITCH) {
                        Log.i(TAG, "remove overtime protect self info for " + info.packageName + " added " + from + "ms ago whose timeout is " + info.timeout);
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
                long from = now - info.startTime;
                if (from >= info.timeout) {
                    sListIterator.remove();
                    if (this.DEBUG_SWITCH) {
                        Log.i(TAG, "remove overtime protect self info for " + info.packageName + " added " + from + "ms ago whose timeout is " + info.timeout);
                    }
                } else if (pkg != null && pkg.equals(info.fromPkg)) {
                    tempList.add(info.packageName);
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
            map.put("protectedPkg", info.packageName);
            map.put("fromPkg", info.fromPkg);
            map.put("timeout", String.valueOf(info.timeout));
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
                            if (!(packageName.equals(IElsaManager.EMPTY_PACKAGE) || minCode == null || maxCode == null)) {
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
        if (pkgName == null || pkgName.equals(IElsaManager.EMPTY_PACKAGE)) {
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
        if (pkgName != null && !pkgName.equals(IElsaManager.EMPTY_PACKAGE)) {
            synchronized (this.mStartFromNotifyPkgLock) {
                this.mStartFromNotifyPkgList.add(pkgName);
            }
        }
    }

    public void removeFromNotifyPkgList(String pkgName) {
        if (pkgName != null && !pkgName.equals(IElsaManager.EMPTY_PACKAGE)) {
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
        if (pkgName == null || pkgName.equals(IElsaManager.EMPTY_PACKAGE)) {
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
        if (pkgName != null && !pkgName.equals(IElsaManager.EMPTY_PACKAGE)) {
            synchronized (this.mStartFromControlCenterPkgLock) {
                this.mStartFromControlCenterPkgList.add(pkgName);
            }
        }
    }

    private void removeFromControlCenterPkgList(String pkgName) {
        if (pkgName != null && !pkgName.equals(IElsaManager.EMPTY_PACKAGE)) {
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

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0052 A:{SYNTHETIC, Splitter: B:23:0x0052} */
    /* JADX WARNING: Removed duplicated region for block: B:75:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0090 A:{SYNTHETIC, Splitter: B:53:0x0090} */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0095  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readDCIMProtectAllowListFile() {
        Exception e;
        Throwable th;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readDCIMProtectAllowListFile start");
        }
        File dcimProtectAllowFile = new File(DCIMPROTECT_MANGER_FILE);
        if (!dcimProtectAllowFile.exists()) {
            Log.e(TAG, "dcimProtectAllowFile isn't exist!");
        }
        boolean isException = false;
        List<String> allowList = new ArrayList();
        FileReader fr = null;
        try {
            FileReader fr2 = new FileReader(dcimProtectAllowFile);
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
                    synchronized (this.mDCIMProtectAllowListLock) {
                        this.mDCIMProtectAllowList.clear();
                        this.mDCIMProtectAllowList.addAll(allowList);
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
                        synchronized (this.mDCIMProtectAllowListLock) {
                            this.mDCIMProtectAllowList.clear();
                            this.mDCIMProtectAllowList.addAll(allowList);
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
                synchronized (this.mDCIMProtectAllowListLock) {
                    this.mDCIMProtectAllowList.clear();
                    this.mDCIMProtectAllowList.addAll(allowList);
                }
            }
        }
    }

    public List<String> getDCIMProtectAllowList() {
        List<String> list;
        synchronized (this.mDCIMProtectAllowListLock) {
            list = this.mDCIMProtectAllowList;
        }
        return list;
    }

    public boolean isInDCIMProtectAllowList(String pkgName) {
        boolean result;
        synchronized (this.mDCIMProtectAllowListLock) {
            if (this.mDCIMProtectAllowList.contains(pkgName)) {
                result = true;
            } else {
                result = this.mDefaultDCIMProtectAllowList.contains(pkgName);
            }
        }
        return result;
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

    /* JADX WARNING: Removed duplicated region for block: B:29:0x007e A:{SYNTHETIC, Splitter: B:29:0x007e} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x008a A:{SYNTHETIC, Splitter: B:35:0x008a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readCustomAppListFile() {
        Exception e;
        Throwable th;
        File file = new File("/system/etc/oppo_customize_whitelist.xml");
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
            List<ApplicationInfo> infos = this.mContext.getPackageManager().getInstalledApplications(DumpState.DUMP_PREFERRED_XML);
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

    /* JADX WARNING: Missing block: B:9:0x001c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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

    /* JADX WARNING: Missing block: B:7:0x0016, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            Class[] clsArr = new Class[1];
            clsArr[0] = String.class;
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", clsArr);
            Log.i(TAG, "invoke " + m);
            Object newInstance = cls.newInstance();
            Object[] objArr = new Object[1];
            objArr[0] = OppoListManager.class.getName();
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
}
