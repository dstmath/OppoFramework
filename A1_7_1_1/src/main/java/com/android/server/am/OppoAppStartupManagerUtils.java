package com.android.server.am;

import android.os.Build.VERSION;
import android.os.Environment;
import android.os.FileObserver;
import android.os.LocaleList;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import com.android.server.LocationManagerService;
import com.android.server.display.DisplayTransformManager;
import com.android.server.oppo.IElsaManager;
import com.android.server.voiceinteraction.DatabaseHelper.SoundModelContract;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
public class OppoAppStartupManagerUtils {
    private static final String AAM_ACTIVITY_WHITE_PKG = "AamActivityWhitePkg";
    private static final String AAM_PROVIDER_WHITE_PKG = "AamProviderWhitePkg";
    private static final String ACTION_NAME = "action";
    private static final String ACTIVITY_CALLED_KEY = "activityCalledKey";
    private static final String ACTIVITY_CALLED_WHITE_CPN = "activityCalledWhiteCpn";
    private static final String ACTIVITY_CALLED_WHITE_PKG = "activityCalledWhitePkg";
    private static final String ACTIVITY_CALLER_WHITE_PKG = "activityCallerWhitePkg";
    private static final String ACTIVITY_INTERCEPT_SWITCH = "activityInterceptSwitch";
    private static final String ACTIVITY_NAME = "activity";
    private static final String ACTIVITY_PKG_KEY = "activityPkgKey";
    private static final String AUTHORIZE_COMPONENT_NAME = "authorizeCpnName";
    private static final String BIND_SERVICE_WHITE_CPN = "bindServiceCpn";
    private static final String BLACKGUARD_ACTIVITY = "blackguardActivity";
    private static final String BLACKGUARD_NAME = "blackguard";
    private static final String BROADCAST_WHITE_CPN = "broadcastAction";
    private static final String BROADCAST_WHITE_PKG = "broadcastPkg";
    private static final String BROWSER_INTERCEPT_UPLOAD_SWITCH = "browserInterceptUploadSwitch";
    private static final String BUILD_BLACK_PKG = "sysblackPkg";
    private static final String CALL_CHECK_COUNT = "callCheckCount";
    private static final String CHECK_COUNT = "checkCount";
    private static final String COLLECT_APP_START_PKG = "monitorAppStartPkg";
    private static final String INTERCEPT_INTERVAL_TIME = "interceptIntervalTime";
    private static final String JOB_WHITE_PKG = "jobWhitePkg";
    private static final String NOTIFY_WHITE_PKG = "notifyWhitePkg";
    private static final String OPPO_ASSOCIATE_START_WHITE_FILE_PATH = "/data/system/associate_startup_whitelist.txt";
    private static final String OPPO_CUSTOMIZE_WHITE_FILE_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final String OPPO_ROM_BLACK_LIST_FILE = "data/system/sys_rom_black_list.xml";
    private static final String OPPO_STARTUP_MANAGER_FILE_PATH = "/data/system/startup_manager.xml";
    private static final String OPPO_STARTUP_MONITOR_FILE_PATH = "/data/system/sys_startupmanager_monitor_list.xml";
    private static final String OPPO_TENCENT_INTERCEPT_PATH = "/data/system/tenIntercept.xml";
    private static final String PAY_CPN = "payCpn";
    private static final String PROTECT_PKG = "protectPkg";
    private static final String PROVIDER_NAME = "provider";
    private static final String PROVIDER_WHITE_CPN = "providerCpn";
    private static final String RECEIVER_ACTION_NAME = "receiverAction";
    private static final String RECEIVER_NAME = "receiver";
    private static final String SEVICECPN_NAME = "sevicecpn";
    private static final String SITE_SOUGOU = "sougouSite";
    private static final String START_SERVICE_WHITE_CPN = "startServiceCpn";
    private static final String START_SERVICE_WHITE_PKG = "startServicePkg";
    private static final String SWITCH_STATUS = "switchStatus";
    private static final String SYNC_WHITE_PKG = "syncWhitePkg";
    private static final String TAG = null;
    private static OppoAppStartupManagerUtils mAsmUtils;
    private boolean DEBUG_DETAIL;
    boolean DEBUG_SWITCH;
    List<String> mAamActivityWhiteList;
    private final Object mAamActivityWhiteListLock;
    List<String> mAamProviderWhiteList;
    private final Object mAamProviderWhiteListLock;
    List<String> mActionBlackList;
    List<String> mActivityBlackList;
    List<String> mActivityCalledKeyList;
    List<String> mActivityCalledWhiteCpnList;
    List<String> mActivityCalledWhitePkgList;
    List<String> mActivityCallerWhitePkgList;
    List<String> mActivityPkgKeyList;
    List<String> mAssociateDefaultList;
    List<String> mAssociateStartWhiteList;
    private FileObserverPolicy mAssociateStartupFileObserver;
    List<String> mAuthorizeCpnList;
    List<String> mBindServiceWhiteCpnList;
    List<String> mBlackguardActivityList;
    List<String> mBlackguardList;
    List<String> mBroadcastActionWhiteList;
    List<String> mBroadcastWhitePkgList;
    List<String> mBuildAppBlackList;
    public int mCallCheckCount;
    private int mCheckCount;
    List<String> mCollectAppStartList;
    List<String> mCustomizeWhiteList;
    private String mDialogButtonText;
    private String mDialogContentText;
    private String mDialogTitleText;
    private FileObserverPolicy mInterceptTenFileObserver;
    List<String> mJobWhiteList;
    List<String> mNotifyWhiteList;
    List<String> mPayCpnList;
    List<String> mProtectWhiteList;
    List<String> mProviderBlackList;
    List<String> mProviderWhiteCpnList;
    List<String> mReceiverActionBlackList;
    List<String> mReceiverBlackList;
    List<String> mSeviceCpnBlacklist;
    List<String> mSougouSiteList;
    List<String> mStartServiceWhiteCpnList;
    List<String> mStartServiceWhiteList;
    private FileObserverPolicy mStartupManagerFileObserver;
    private FileObserverPolicy mStartupMonitorFileObserver;
    private boolean mSwitchBrowserInterceptUpload;
    public boolean mSwitchInterceptActivity;
    private boolean mSwitchStatus;
    List<String> mSyncWhiteList;
    private String mTencentInterceptCpn;
    private int mTencentInterceptSwitchValue;

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event != 8) {
                return;
            }
            if (this.focusPath.equals(OppoAppStartupManagerUtils.OPPO_STARTUP_MANAGER_FILE_PATH)) {
                Log.i(OppoAppStartupManagerUtils.TAG, "focusPath OPPO_STARTUP_MANAGER_FILE_PATH!");
                OppoAppStartupManagerUtils.this.readStartupManagerFile();
                OppoAppStartupManager.getInstance().updateConfiglist();
            } else if (this.focusPath.equals(OppoAppStartupManagerUtils.OPPO_STARTUP_MONITOR_FILE_PATH)) {
                Log.i(OppoAppStartupManagerUtils.TAG, "focusPath OPPO_STARTUP_MONITOR_FILE_PATH!");
                OppoAppStartupManagerUtils.this.readStartupMonitorFile();
                OppoAppStartupManager.getInstance().updateMonitorlist();
            } else if (this.focusPath.equals(OppoAppStartupManagerUtils.OPPO_ASSOCIATE_START_WHITE_FILE_PATH)) {
                Log.i(OppoAppStartupManagerUtils.TAG, "focusPath OPPO_ASSOCIATE_START_WHITE_FILE_PATH!");
                OppoAppStartupManagerUtils.this.readAssociateStartWhiteFile();
                OppoAppStartupManager.getInstance().updateAssociateStartList();
            } else if (this.focusPath.equals(OppoAppStartupManagerUtils.OPPO_TENCENT_INTERCEPT_PATH)) {
                Log.i(OppoAppStartupManagerUtils.TAG, "focusPath OPPO_TEN_INTERCEPT_PATH!");
                OppoAppStartupManagerUtils.this.readTenInterceptFile();
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAppStartupManagerUtils.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAppStartupManagerUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoAppStartupManagerUtils.<clinit>():void");
    }

    private OppoAppStartupManagerUtils() {
        this.DEBUG_DETAIL = OppoAppStartupManager.DEBUG_DETAIL;
        this.DEBUG_SWITCH = this.DEBUG_DETAIL;
        this.mStartupManagerFileObserver = null;
        this.mStartupMonitorFileObserver = null;
        this.mAssociateStartupFileObserver = null;
        this.mInterceptTenFileObserver = null;
        this.mSwitchBrowserInterceptUpload = false;
        this.mCheckCount = DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE;
        this.mSwitchStatus = false;
        this.mSwitchInterceptActivity = false;
        this.mCallCheckCount = 50;
        this.mSeviceCpnBlacklist = new ArrayList();
        this.mReceiverBlackList = new ArrayList();
        this.mReceiverActionBlackList = new ArrayList();
        this.mProviderBlackList = new ArrayList();
        this.mActivityBlackList = new ArrayList();
        this.mActionBlackList = new ArrayList();
        this.mBuildAppBlackList = new ArrayList();
        this.mStartServiceWhiteList = new ArrayList();
        this.mStartServiceWhiteCpnList = new ArrayList();
        this.mBindServiceWhiteCpnList = new ArrayList();
        this.mJobWhiteList = new ArrayList();
        this.mSyncWhiteList = new ArrayList();
        this.mNotifyWhiteList = new ArrayList();
        this.mProviderWhiteCpnList = new ArrayList();
        this.mBroadcastWhitePkgList = new ArrayList();
        this.mBroadcastActionWhiteList = new ArrayList();
        this.mProtectWhiteList = new ArrayList();
        this.mAssociateDefaultList = new ArrayList();
        this.mBlackguardList = new ArrayList();
        this.mCollectAppStartList = new ArrayList();
        this.mActivityCallerWhitePkgList = new ArrayList();
        this.mActivityCalledWhitePkgList = new ArrayList();
        this.mActivityCalledWhiteCpnList = new ArrayList();
        this.mActivityPkgKeyList = new ArrayList();
        this.mActivityCalledKeyList = new ArrayList();
        this.mBlackguardActivityList = new ArrayList();
        this.mSougouSiteList = new ArrayList();
        this.mCustomizeWhiteList = new ArrayList();
        this.mAssociateStartWhiteList = new ArrayList();
        this.mAuthorizeCpnList = new ArrayList();
        this.mPayCpnList = new ArrayList();
        this.mAamActivityWhiteList = new ArrayList();
        this.mAamProviderWhiteList = new ArrayList();
        this.mAamActivityWhiteListLock = new Object();
        this.mAamProviderWhiteListLock = new Object();
        this.mDialogTitleText = null;
        this.mDialogContentText = null;
        this.mDialogButtonText = null;
        this.mTencentInterceptSwitchValue = 0;
        this.mTencentInterceptCpn = "com.tencent.mm.plugin.webview.ui.tools.WebViewDownloadUI";
        initDir();
        initFileObserver();
        readStartupManagerFile();
        readStartupMonitorFile();
        readCustomizeWhiteList();
        readAssociateStartWhiteFile();
        readTenInterceptFile();
    }

    public static OppoAppStartupManagerUtils getInstance() {
        if (mAsmUtils == null) {
            mAsmUtils = new OppoAppStartupManagerUtils();
        }
        return mAsmUtils;
    }

    private void initDir() {
        Log.i(TAG, "initDir start");
        File startupManagerFile = new File(OPPO_STARTUP_MANAGER_FILE_PATH);
        File startupMonitorFile = new File(OPPO_STARTUP_MONITOR_FILE_PATH);
        File associateStartupFile = new File(OPPO_ASSOCIATE_START_WHITE_FILE_PATH);
        File interceptTenFile = new File(OPPO_TENCENT_INTERCEPT_PATH);
        try {
            if (!startupManagerFile.exists()) {
                startupManagerFile.createNewFile();
                File permFile = new File(Environment.getRootDirectory(), "etc/startup_manager.xml");
                if (permFile.exists() && startupManagerFile.exists()) {
                    initStartupManagerFile(permFile, startupManagerFile);
                }
            }
            if (!startupMonitorFile.exists()) {
                startupMonitorFile.createNewFile();
            }
            if (!associateStartupFile.exists()) {
                associateStartupFile.createNewFile();
            }
            if (!interceptTenFile.exists()) {
                interceptTenFile.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG, "initDir failed!!!");
            e.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003e A:{SYNTHETIC, Splitter: B:17:0x003e} */
    /* JADX WARNING: Removed duplicated region for block: B:50:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0043 A:{Catch:{ Exception -> 0x0072 }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0090 A:{SYNTHETIC, Splitter: B:35:0x0090} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0095 A:{Catch:{ Exception -> 0x0099 }} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x003e A:{SYNTHETIC, Splitter: B:17:0x003e} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0043 A:{Catch:{ Exception -> 0x0072 }} */
    /* JADX WARNING: Removed duplicated region for block: B:50:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0090 A:{SYNTHETIC, Splitter: B:35:0x0090} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0095 A:{Catch:{ Exception -> 0x0099 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initStartupManagerFile(File srcFile, File newFile) {
        Exception e;
        Throwable th;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            FileInputStream inStream2 = new FileInputStream(srcFile);
            try {
                FileOutputStream outStream2 = new FileOutputStream(newFile);
                try {
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int byteRead = inStream2.read(buffer);
                        if (byteRead == -1) {
                            break;
                        }
                        outStream2.write(buffer, 0, byteRead);
                    }
                    inStream2.close();
                    if (inStream2 != null) {
                        try {
                            inStream2.close();
                        } catch (Exception e2) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e2);
                        }
                    }
                    if (outStream2 != null) {
                        outStream2.close();
                    }
                    outStream = outStream2;
                } catch (Exception e3) {
                    e2 = e3;
                    outStream = outStream2;
                    inStream = inStream2;
                    try {
                        e2.printStackTrace();
                        Log.e(TAG, "initStartupManagerFile Failed " + e2);
                        if (inStream != null) {
                        }
                        if (outStream != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (inStream != null) {
                        }
                        if (outStream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    outStream = outStream2;
                    inStream = inStream2;
                    if (inStream != null) {
                    }
                    if (outStream != null) {
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e2 = e4;
                inStream = inStream2;
                e2.printStackTrace();
                Log.e(TAG, "initStartupManagerFile Failed " + e2);
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (Exception e22) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e22);
                        return;
                    }
                }
                if (outStream != null) {
                    outStream.close();
                }
            } catch (Throwable th4) {
                th = th4;
                inStream = inStream2;
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (Exception e222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e222);
                        throw th;
                    }
                }
                if (outStream != null) {
                    outStream.close();
                }
                throw th;
            }
        } catch (Exception e5) {
            e222 = e5;
            e222.printStackTrace();
            Log.e(TAG, "initStartupManagerFile Failed " + e222);
            if (inStream != null) {
            }
            if (outStream != null) {
            }
        }
    }

    private void initFileObserver() {
        this.mStartupManagerFileObserver = new FileObserverPolicy(OPPO_STARTUP_MANAGER_FILE_PATH);
        this.mStartupManagerFileObserver.startWatching();
        this.mStartupMonitorFileObserver = new FileObserverPolicy(OPPO_STARTUP_MONITOR_FILE_PATH);
        this.mStartupMonitorFileObserver.startWatching();
        this.mAssociateStartupFileObserver = new FileObserverPolicy(OPPO_ASSOCIATE_START_WHITE_FILE_PATH);
        this.mAssociateStartupFileObserver.startWatching();
        this.mInterceptTenFileObserver = new FileObserverPolicy(OPPO_TENCENT_INTERCEPT_PATH);
        this.mInterceptTenFileObserver.startWatching();
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

    protected List<String> getBuildBlackList() {
        return this.mBuildAppBlackList;
    }

    protected List<String> getStartServiceWhiteList() {
        return this.mStartServiceWhiteList;
    }

    protected List<String> getStartServiceWhiteCpnList() {
        return this.mStartServiceWhiteCpnList;
    }

    protected List<String> getBindServiceWhiteList() {
        return this.mBindServiceWhiteCpnList;
    }

    protected List<String> getJobWhiteList() {
        return this.mJobWhiteList;
    }

    protected List<String> getSyncWhiteList() {
        return this.mSyncWhiteList;
    }

    protected List<String> getNotifyWhiteList() {
        return this.mNotifyWhiteList;
    }

    protected List<String> getProviderWhiteList() {
        return this.mProviderWhiteCpnList;
    }

    protected List<String> getBroadcastWhiteList() {
        return this.mBroadcastWhitePkgList;
    }

    protected List<String> getBroadcastActionWhiteList() {
        return this.mBroadcastActionWhiteList;
    }

    protected List<String> getProtectList() {
        return this.mProtectWhiteList;
    }

    protected List<String> getAssociateStartWhiteList() {
        return this.mAssociateStartWhiteList;
    }

    protected List<String> getCollectAppStartList() {
        return this.mCollectAppStartList;
    }

    protected List<String> getAuthorizeCpnList() {
        return this.mAuthorizeCpnList;
    }

    protected List<String> getPayCpnList() {
        return this.mPayCpnList;
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

    public int getTencentInterceptSwitchValue() {
        return this.mTencentInterceptSwitchValue;
    }

    public String getTencentInterceptCpn() {
        return this.mTencentInterceptCpn;
    }

    public void readStartupManagerFile() {
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readStartupManagerFile start");
        }
        readConfigFromFileLocked(new File(OPPO_STARTUP_MANAGER_FILE_PATH));
    }

    /* JADX WARNING: Removed duplicated region for block: B:355:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01c4 A:{SYNTHETIC, Splitter: B:59:0x01c4} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0128 A:{Splitter: B:9:0x0033, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x023b A:{Splitter: B:9:0x0033, ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException)} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x02bf A:{Splitter: B:9:0x0033, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0343 A:{Splitter: B:9:0x0033, ExcHandler: java.lang.IndexOutOfBoundsException (e java.lang.IndexOutOfBoundsException)} */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x03c7 A:{Splitter: B:9:0x0033, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:37:0x0128, code:
            r21 = e;
     */
    /* JADX WARNING: Missing block: B:38:0x0129, code:
            r40 = r0;
     */
    /* JADX WARNING: Missing block: B:54:0x01b1, code:
            r22 = e;
     */
    /* JADX WARNING: Missing block: B:55:0x01b2, code:
            r40 = r0;
     */
    /* JADX WARNING: Missing block: B:60:?, code:
            r40.close();
     */
    /* JADX WARNING: Missing block: B:61:0x01c9, code:
            r19 = move-exception;
     */
    /* JADX WARNING: Missing block: B:62:0x01ca, code:
            android.util.Log.e(TAG, "Failed to close state FileInputStream " + r19);
     */
    /* JADX WARNING: Missing block: B:71:0x023b, code:
            r23 = e;
     */
    /* JADX WARNING: Missing block: B:72:0x023c, code:
            r40 = r0;
     */
    /* JADX WARNING: Missing block: B:88:0x02bf, code:
            r19 = e;
     */
    /* JADX WARNING: Missing block: B:89:0x02c0, code:
            r40 = r0;
     */
    /* JADX WARNING: Missing block: B:105:0x0343, code:
            r20 = e;
     */
    /* JADX WARNING: Missing block: B:106:0x0344, code:
            r40 = r0;
     */
    /* JADX WARNING: Missing block: B:122:0x03c7, code:
            r46 = th;
     */
    /* JADX WARNING: Missing block: B:123:0x03c8, code:
            r40 = r0;
     */
    /* JADX WARNING: Missing block: B:355:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:356:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:364:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFromFileLocked(File file) {
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readConfigFromFileLocked start");
        }
        cleanConfigList();
        if (!this.mSougouSiteList.isEmpty()) {
            this.mSougouSiteList.clear();
        }
        FileInputStream fileInputStream = null;
        try {
            InputStream fileInputStream2 = new FileInputStream(file);
            String checkCount;
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileInputStream2, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tagName = parser.getName();
                        if (this.DEBUG_SWITCH) {
                            Log.i(TAG, " readFromFileLocked tagName=" + tagName);
                        }
                        if (SEVICECPN_NAME.equals(tagName)) {
                            String sevicecpn = parser.nextText();
                            if (!sevicecpn.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mSeviceCpnBlacklist.add(sevicecpn);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked sevicecpn = " + sevicecpn);
                                }
                            }
                        } else if (RECEIVER_NAME.equals(tagName)) {
                            String receiver = parser.nextText();
                            if (!receiver.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mReceiverBlackList.add(receiver);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked receiver = " + receiver);
                                }
                            }
                        } else if (RECEIVER_ACTION_NAME.equals(tagName)) {
                            String receiverAction = parser.nextText();
                            if (!receiverAction.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mReceiverActionBlackList.add(receiverAction);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked receiverAction = " + receiverAction);
                                }
                            }
                        } else if ("provider".equals(tagName)) {
                            String provider = parser.nextText();
                            if (!provider.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mProviderBlackList.add(provider);
                                if (this.DEBUG_DETAIL) {
                                    Log.i(TAG, " readFromFileLocked provider = " + provider);
                                }
                            }
                        } else if (ACTIVITY_NAME.equals(tagName)) {
                            String activity = parser.nextText();
                            if (!activity.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mActivityBlackList.add(activity);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked activity = " + activity);
                                }
                            }
                        } else if (ACTION_NAME.equals(tagName)) {
                            String action = parser.nextText();
                            if (!action.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mActionBlackList.add(action);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked action = " + action);
                                }
                            }
                        } else if (BLACKGUARD_NAME.equals(tagName)) {
                            String blackguard = parser.nextText();
                            if (!blackguard.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mBlackguardList.add(blackguard);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked blackguard = " + blackguard);
                                }
                            }
                        } else if (ACTIVITY_INTERCEPT_SWITCH.equals(tagName)) {
                            String isIntercept = parser.nextText();
                            if (!isIntercept.equals(IElsaManager.EMPTY_PACKAGE)) {
                                setSwitchInterceptActivity(isIntercept);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked isIntercept = " + isIntercept);
                                }
                            }
                        } else if (BROWSER_INTERCEPT_UPLOAD_SWITCH.equals(tagName)) {
                            String isAllowed = parser.nextText();
                            if (!isAllowed.equals(IElsaManager.EMPTY_PACKAGE)) {
                                setSwitchBrowserInterceptUpload(isAllowed);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked isBrowserInterceptUploadAllowed = " + isAllowed);
                                }
                            }
                        } else if (ACTIVITY_CALLER_WHITE_PKG.equals(tagName)) {
                            String callerWhitePkg = parser.nextText();
                            if (!callerWhitePkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mActivityCallerWhitePkgList.add(callerWhitePkg);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked activityCallerWhitePkg = " + callerWhitePkg);
                                }
                            }
                        } else if (ACTIVITY_CALLED_WHITE_CPN.equals(tagName)) {
                            String calledWhiteCpn = parser.nextText();
                            if (!calledWhiteCpn.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mActivityCalledWhiteCpnList.add(calledWhiteCpn);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked activityCalledWhiteCpn = " + calledWhiteCpn);
                                }
                            }
                        } else if (ACTIVITY_CALLED_WHITE_PKG.equals(tagName)) {
                            String calledWhitePkg = parser.nextText();
                            if (!calledWhitePkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mActivityCalledWhitePkgList.add(calledWhitePkg);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked activityCalledWhitePkg = " + calledWhitePkg);
                                }
                            }
                        } else if (ACTIVITY_PKG_KEY.equals(tagName)) {
                            String pkgKey = parser.nextText();
                            if (!pkgKey.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mActivityPkgKeyList.add(pkgKey);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked activityPkgKey = " + pkgKey);
                                }
                            }
                        } else if (ACTIVITY_CALLED_KEY.equals(tagName)) {
                            String calledKey = parser.nextText();
                            if (!calledKey.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mActivityCalledKeyList.add(calledKey);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked activityCalledKey = " + calledKey);
                                }
                            }
                        } else if (BLACKGUARD_ACTIVITY.equals(tagName)) {
                            String blackguardActivity = parser.nextText();
                            if (!blackguardActivity.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mBlackguardActivityList.add(blackguardActivity);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked blackguardActivity = " + blackguardActivity);
                                }
                            }
                        } else if (SITE_SOUGOU.equals(tagName)) {
                            String sougouSite = parser.nextText();
                            if (!sougouSite.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mSougouSiteList.add(sougouSite);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked sougouSite = " + sougouSite);
                                }
                            }
                        } else if (BUILD_BLACK_PKG.equals(tagName)) {
                            String buildBlackPkg = parser.nextText();
                            if (!buildBlackPkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mBuildAppBlackList.add(buildBlackPkg);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked buildBlackPkg = " + buildBlackPkg);
                                }
                            }
                        } else if (START_SERVICE_WHITE_PKG.equals(tagName)) {
                            String startServicePkg = parser.nextText();
                            if (!startServicePkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mStartServiceWhiteList.add(startServicePkg);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked startServicePkg = " + startServicePkg);
                                }
                            }
                        } else if (START_SERVICE_WHITE_CPN.equals(tagName)) {
                            String startServiceCpn = parser.nextText();
                            if (!startServiceCpn.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mStartServiceWhiteCpnList.add(startServiceCpn);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked startServiceCpn = " + startServiceCpn);
                                }
                            }
                        } else if (BIND_SERVICE_WHITE_CPN.equals(tagName)) {
                            String bindServiceCpn = parser.nextText();
                            if (!bindServiceCpn.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mBindServiceWhiteCpnList.add(bindServiceCpn);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked bindServiceCpn = " + bindServiceCpn);
                                }
                            }
                        } else if (JOB_WHITE_PKG.equals(tagName)) {
                            String jobWhitepkg = parser.nextText();
                            if (!jobWhitepkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mJobWhiteList.add(jobWhitepkg);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked jobWhitepkg = " + jobWhitepkg);
                                }
                            }
                        } else if (SYNC_WHITE_PKG.equals(tagName)) {
                            String syncWhitePkg = parser.nextText();
                            if (!syncWhitePkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mSyncWhiteList.add(syncWhitePkg);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked syncWhitePkg = " + syncWhitePkg);
                                }
                            }
                        } else if (NOTIFY_WHITE_PKG.equals(tagName)) {
                            String notifyWhitePkg = parser.nextText();
                            if (!notifyWhitePkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mNotifyWhiteList.add(notifyWhitePkg);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked notifyWhitePkg = " + notifyWhitePkg);
                                }
                            }
                        } else if (PROVIDER_WHITE_CPN.equals(tagName)) {
                            String providerCpn = parser.nextText();
                            if (!providerCpn.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mProviderWhiteCpnList.add(providerCpn);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked providerCpn = " + providerCpn);
                                }
                            }
                        } else if (BROADCAST_WHITE_PKG.equals(tagName)) {
                            String broadcastPkg = parser.nextText();
                            if (!broadcastPkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mBroadcastWhitePkgList.add(broadcastPkg);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked broadcastPkg = " + broadcastPkg);
                                }
                            }
                        } else if (BROADCAST_WHITE_CPN.equals(tagName)) {
                            String broadcastAction = parser.nextText();
                            if (!broadcastAction.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mBroadcastActionWhiteList.add(broadcastAction);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked broadcastaction = " + broadcastAction);
                                }
                            }
                        } else if (PROTECT_PKG.equals(tagName)) {
                            String protectPkg = parser.nextText();
                            if (!protectPkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mProtectWhiteList.add(protectPkg);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked protectPkg = " + protectPkg);
                                }
                            }
                        } else if (CHECK_COUNT.equals(tagName)) {
                            checkCount = parser.nextText();
                            if (!checkCount.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mCheckCount = Integer.parseInt(checkCount);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked checkCount = " + this.mCheckCount);
                                }
                            }
                        } else if (SWITCH_STATUS.equals(tagName)) {
                            String switchStatus = parser.nextText();
                            if (!switchStatus.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mSwitchStatus = Boolean.parseBoolean(switchStatus);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked switchStatus = " + this.mSwitchStatus);
                                }
                            }
                        } else if (PAY_CPN.equals(tagName)) {
                            String payPkg = parser.nextText();
                            if (!payPkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mPayCpnList.add(payPkg);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked payPkg = " + payPkg);
                                }
                            }
                        } else if (AUTHORIZE_COMPONENT_NAME.equals(tagName)) {
                            String authorizeCpnName = parser.nextText();
                            if (!authorizeCpnName.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mAuthorizeCpnList.add(authorizeCpnName);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked authorizeCpnName = " + authorizeCpnName);
                                }
                            }
                        } else if (AAM_ACTIVITY_WHITE_PKG.equals(tagName)) {
                            String aamActivityWhitePkg = parser.nextText();
                            if (!aamActivityWhitePkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                synchronized (this.mAamActivityWhiteListLock) {
                                    this.mAamActivityWhiteList.add(aamActivityWhitePkg);
                                }
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked aamActivityWhitePkg = " + aamActivityWhitePkg);
                                }
                            }
                        } else if (AAM_PROVIDER_WHITE_PKG.equals(tagName)) {
                            String aamProviderWhitePkg = parser.nextText();
                            if (!aamProviderWhitePkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                synchronized (this.mAamProviderWhiteListLock) {
                                    this.mAamProviderWhiteList.add(aamProviderWhitePkg);
                                }
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked aamProviderWhitePkg = " + aamProviderWhitePkg);
                                }
                            }
                        }
                    }
                } while (type != 1);
            } catch (NumberFormatException e) {
                Log.i(TAG, " readFromFileLocked checkCount failed, checkCount= " + checkCount);
            } catch (NullPointerException e2) {
            } catch (XmlPullParserException e3) {
            } catch (IOException e4) {
            } catch (IndexOutOfBoundsException e5) {
            } catch (Throwable th) {
            }
            if (fileInputStream2 != null) {
                try {
                    fileInputStream2.close();
                } catch (IOException e6) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e6);
                }
            }
            InputStream inputStream = fileInputStream2;
        } catch (NullPointerException e7) {
            Throwable e8 = e7;
            try {
                Log.e(TAG, "failed parsing ", e8);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e62) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e62);
                    }
                }
            } catch (Throwable th2) {
                Throwable th3 = th2;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e622) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e622);
                    }
                }
                throw th3;
            }
        } catch (NumberFormatException e9) {
            Throwable e10 = e9;
            Log.e(TAG, "failed parsing ", e10);
            if (fileInputStream == null) {
            }
        } catch (XmlPullParserException e11) {
            Throwable e12 = e11;
            Log.e(TAG, "failed parsing ", e12);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e6222) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e6222);
                }
            }
        } catch (IOException e13) {
            Throwable e14 = e13;
            Log.e(TAG, "failed IOException ", e14);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e62222) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e62222);
                }
            }
        } catch (IndexOutOfBoundsException e15) {
            Throwable e16 = e15;
            Log.e(TAG, "failed parsing ", e16);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e622222) {
                    Log.e(TAG, "Failed to close state FileInputStream " + e622222);
                }
            }
        }
    }

    protected void cleanConfigList() {
        if (!this.mSeviceCpnBlacklist.isEmpty()) {
            this.mSeviceCpnBlacklist.clear();
        }
        if (!this.mReceiverBlackList.isEmpty()) {
            this.mReceiverBlackList.clear();
        }
        if (!this.mReceiverActionBlackList.isEmpty()) {
            this.mReceiverActionBlackList.clear();
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
        if (!this.mPayCpnList.isEmpty()) {
            this.mPayCpnList.clear();
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
    }

    public void readStartupMonitorFile() {
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readStartupMonitorFile start");
        }
        readMonitorFromFileLocked(new File(OPPO_STARTUP_MONITOR_FILE_PATH));
    }

    /* JADX WARNING: Removed duplicated region for block: B:79:0x01c6 A:{SYNTHETIC, Splitter: B:79:0x01c6} */
    /* JADX WARNING: Removed duplicated region for block: B:97:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00d8 A:{SYNTHETIC, Splitter: B:39:0x00d8} */
    /* JADX WARNING: Removed duplicated region for block: B:105:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x01a2 A:{SYNTHETIC, Splitter: B:73:0x01a2} */
    /* JADX WARNING: Removed duplicated region for block: B:103:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0176 A:{SYNTHETIC, Splitter: B:65:0x0176} */
    /* JADX WARNING: Removed duplicated region for block: B:101:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x014a A:{SYNTHETIC, Splitter: B:57:0x014a} */
    /* JADX WARNING: Removed duplicated region for block: B:99:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x011e A:{SYNTHETIC, Splitter: B:49:0x011e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readMonitorFromFileLocked(File file) {
        IOException e;
        NullPointerException e2;
        Throwable th;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readMonitorFromFileLocked start");
        }
        cleanMonitorList();
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
                        String tagName = parser.getName();
                        if (this.DEBUG_SWITCH) {
                            Log.i(TAG, " readFromFileLocked tagName=" + tagName);
                        }
                        if (CALL_CHECK_COUNT.equals(tagName)) {
                            String checkCount = parser.nextText();
                            if (!checkCount.equals(IElsaManager.EMPTY_PACKAGE)) {
                                setCallCheckCount(checkCount);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked checkCount = " + checkCount);
                                }
                            }
                        } else if (COLLECT_APP_START_PKG.equals(tagName)) {
                            String monitorApp = parser.nextText();
                            if (!monitorApp.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mCollectAppStartList.add(monitorApp);
                                if (this.DEBUG_SWITCH) {
                                    Log.i(TAG, " readFromFileLocked collect app pkg = " + monitorApp);
                                }
                            }
                        }
                    }
                } while (type != 1);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e6) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e6);
                    }
                }
                stream = stream2;
            } catch (NullPointerException e7) {
                e2 = e7;
                stream = stream2;
                try {
                    Log.e(TAG, "failed parsing ", e2);
                    if (stream == null) {
                        try {
                            stream.close();
                        } catch (IOException e62) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e62);
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                    }
                    throw th;
                }
            } catch (NumberFormatException e8) {
                e3 = e8;
                stream = stream2;
                Log.e(TAG, "failed parsing ", e3);
                if (stream == null) {
                    try {
                        stream.close();
                    } catch (IOException e622) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e622);
                    }
                }
            } catch (XmlPullParserException e9) {
                e4 = e9;
                stream = stream2;
                Log.e(TAG, "failed parsing ", e4);
                if (stream == null) {
                    try {
                        stream.close();
                    } catch (IOException e6222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e6222);
                    }
                }
            } catch (IOException e10) {
                e6222 = e10;
                stream = stream2;
                Log.e(TAG, "failed IOException ", e6222);
                if (stream == null) {
                    try {
                        stream.close();
                    } catch (IOException e62222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e62222);
                    }
                }
            } catch (IndexOutOfBoundsException e11) {
                e5 = e11;
                stream = stream2;
                Log.e(TAG, "failed parsing ", e5);
                if (stream == null) {
                    try {
                        stream.close();
                    } catch (IOException e622222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e622222);
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e6222222);
                    }
                }
                throw th;
            }
        } catch (NullPointerException e12) {
            e2 = e12;
            Log.e(TAG, "failed parsing ", e2);
            if (stream == null) {
            }
        } catch (NumberFormatException e13) {
            e3 = e13;
            Log.e(TAG, "failed parsing ", e3);
            if (stream == null) {
            }
        } catch (XmlPullParserException e14) {
            e4 = e14;
            Log.e(TAG, "failed parsing ", e4);
            if (stream == null) {
            }
        } catch (IOException e15) {
            e6222222 = e15;
            Log.e(TAG, "failed IOException ", e6222222);
            if (stream == null) {
            }
        } catch (IndexOutOfBoundsException e16) {
            e5 = e16;
            Log.e(TAG, "failed parsing ", e5);
            if (stream == null) {
            }
        }
    }

    protected void cleanMonitorList() {
        if (!this.mCollectAppStartList.isEmpty()) {
            this.mCollectAppStartList.clear();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:66:0x013c A:{SYNTHETIC, Splitter: B:66:0x013c} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0110 A:{SYNTHETIC, Splitter: B:58:0x0110} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00e5 A:{SYNTHETIC, Splitter: B:50:0x00e5} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00bb A:{SYNTHETIC, Splitter: B:42:0x00bb} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0091 A:{SYNTHETIC, Splitter: B:34:0x0091} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0160 A:{SYNTHETIC, Splitter: B:72:0x0160} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readCustomizeWhiteList() {
        IOException e;
        NullPointerException e2;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        Throwable th;
        if (this.DEBUG_SWITCH) {
            Log.i(TAG, "readCustomizeWhiteList start");
        }
        File customizeFile = new File(OPPO_CUSTOMIZE_WHITE_FILE_PATH);
        if (customizeFile.exists()) {
            cleanCustomizeList();
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(customizeFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream2, null);
                    int type;
                    do {
                        type = parser.next();
                        if (type == 2) {
                            if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                                String pkgName = parser.getAttributeValue(null, "att");
                                if (pkgName != null) {
                                    this.mCustomizeWhiteList.add(pkgName);
                                }
                            }
                        }
                    } while (type != 1);
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e6) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e6);
                        }
                    }
                    stream = stream2;
                } catch (NullPointerException e7) {
                    e2 = e7;
                    stream = stream2;
                    Log.e(TAG, "failed parsing ", e2);
                    if (stream != null) {
                    }
                    return;
                } catch (NumberFormatException e8) {
                    e3 = e8;
                    stream = stream2;
                    Log.e(TAG, "failed parsing ", e3);
                    if (stream != null) {
                    }
                    return;
                } catch (XmlPullParserException e9) {
                    e4 = e9;
                    stream = stream2;
                    Log.e(TAG, "failed parsing ", e4);
                    if (stream != null) {
                    }
                    return;
                } catch (IOException e10) {
                    e6 = e10;
                    stream = stream2;
                    Log.e(TAG, "failed IOException ", e6);
                    if (stream != null) {
                    }
                    return;
                } catch (IndexOutOfBoundsException e11) {
                    e5 = e11;
                    stream = stream2;
                    try {
                        Log.e(TAG, "failed parsing ", e5);
                        if (stream != null) {
                        }
                        return;
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
                        } catch (IOException e62) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e62);
                        }
                    }
                    throw th;
                }
            } catch (NullPointerException e12) {
                e2 = e12;
                Log.e(TAG, "failed parsing ", e2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e622) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e622);
                    }
                }
                return;
            } catch (NumberFormatException e13) {
                e3 = e13;
                Log.e(TAG, "failed parsing ", e3);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e6222);
                    }
                }
                return;
            } catch (XmlPullParserException e14) {
                e4 = e14;
                Log.e(TAG, "failed parsing ", e4);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e62222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e62222);
                    }
                }
                return;
            } catch (IOException e15) {
                e62222 = e15;
                Log.e(TAG, "failed IOException ", e62222);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e622222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e622222);
                    }
                }
                return;
            } catch (IndexOutOfBoundsException e16) {
                e5 = e16;
                Log.e(TAG, "failed parsing ", e5);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222222) {
                        Log.e(TAG, "Failed to close state FileInputStream " + e6222222);
                    }
                }
                return;
            }
            return;
        }
        if (this.DEBUG_SWITCH) {
            Log.e(TAG, "readCustomizeWhiteList failed: file doesn't exist!");
        }
    }

    protected void cleanCustomizeList() {
        if (!this.mCustomizeWhiteList.isEmpty()) {
            this.mCustomizeWhiteList.clear();
        }
    }

    private void readAssociateStartWhiteFile() {
        File associateStartFile = new File(OPPO_ASSOCIATE_START_WHITE_FILE_PATH);
        if (associateStartFile.exists()) {
            cleanAssociateStartWhiteList();
            try {
                FileReader fr = new FileReader(associateStartFile);
                BufferedReader reader = new BufferedReader(fr);
                while (true) {
                    String strT = reader.readLine();
                    if (strT == null) {
                        break;
                    }
                    this.mAssociateStartWhiteList.add(strT.trim());
                }
                reader.close();
                fr.close();
            } catch (Exception e) {
                Log.e(TAG, "associateStartFile read execption: " + e);
            }
            return;
        }
        if (this.DEBUG_SWITCH) {
            Log.e(TAG, "associateStartFile failed: file doesn't exist!");
        }
    }

    protected void cleanAssociateStartWhiteList() {
        if (!this.mAssociateStartWhiteList.isEmpty()) {
            this.mAssociateStartWhiteList.clear();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x00f6 A:{SYNTHETIC, Splitter: B:50:0x00f6} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0133 A:{SYNTHETIC, Splitter: B:63:0x0133} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readTenInterceptFile() {
        Exception e;
        Throwable th;
        File tenInterceptFile = new File(OPPO_TENCENT_INTERCEPT_PATH);
        if (tenInterceptFile.exists()) {
            FileInputStream fileInputStream = null;
            this.mTencentInterceptSwitchValue = 0;
            try {
                FileInputStream stream = new FileInputStream(tenInterceptFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream, null);
                    int type;
                    do {
                        type = parser.next();
                        if (type == 2) {
                            String tag = parser.getName();
                            String interceptSwitch;
                            String intereptType;
                            if ("url".equals(tag)) {
                                interceptSwitch = parser.getAttributeValue(null, OppoProcessManager.RESUME_REASON_SWITCH_CHANGE_STR);
                                intereptType = parser.getAttributeValue(null, SoundModelContract.KEY_TYPE);
                                if (interceptSwitch != null && intereptType != null && interceptSwitch.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && intereptType.equals("0")) {
                                    this.mTencentInterceptSwitchValue++;
                                    if (this.DEBUG_SWITCH) {
                                        Log.i(TAG, "mTencentInterceptSwitchValue " + this.mTencentInterceptSwitchValue);
                                    }
                                }
                            } else if ("label".equals(tag)) {
                                interceptSwitch = parser.getAttributeValue(null, OppoProcessManager.RESUME_REASON_SWITCH_CHANGE_STR);
                                intereptType = parser.getAttributeValue(null, SoundModelContract.KEY_TYPE);
                                if (interceptSwitch != null && intereptType != null && interceptSwitch.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && intereptType.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                                    this.mTencentInterceptSwitchValue += 2;
                                    if (this.DEBUG_SWITCH) {
                                        Log.i(TAG, "mTencentInterceptSwitchValue " + this.mTencentInterceptSwitchValue);
                                    }
                                }
                            } else if ("cpn".equals(tag)) {
                                String cpn = parser.nextText();
                                if (!cpn.equals(IElsaManager.EMPTY_PACKAGE)) {
                                    this.mTencentInterceptCpn = cpn;
                                }
                            }
                        }
                    } while (type != 1);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e2) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e2);
                        }
                    }
                    fileInputStream = stream;
                } catch (Exception e3) {
                    e = e3;
                    fileInputStream = stream;
                    try {
                        Log.e(TAG, "failed parsing ", e);
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e22) {
                                Log.e(TAG, "Failed to close state FileInputStream " + e22);
                            }
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileInputStream = stream;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e222) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e222);
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                Log.e(TAG, "failed parsing ", e);
                if (fileInputStream != null) {
                }
                return;
            }
            return;
        }
        if (this.DEBUG_SWITCH) {
            Log.e(TAG, "tenInterceptFile failed: file doesn't exist!");
        }
    }

    public boolean isForumVersion() {
        String ver = SystemProperties.get("ro.build.version.opporom");
        if (ver == null) {
            return false;
        }
        ver = ver.toLowerCase();
        if (ver.endsWith("alpha") || ver.endsWith("beta")) {
            return true;
        }
        return false;
    }

    public void setDynamicDebugSwitch() {
        this.DEBUG_SWITCH = this.DEBUG_DETAIL | OppoAppStartupManager.getInstance().DynamicDebug;
    }

    public boolean getSwitchStatus() {
        return this.mSwitchStatus;
    }

    public int getCheckCount() {
        return this.mCheckCount;
    }

    /* JADX WARNING: Removed duplicated region for block: B:76:0x0096 A:{SYNTHETIC, EDGE_INSN: B:76:0x0096->B:22:0x0096 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0146 A:{LOOP_END, LOOP:0: B:5:0x001d->B:57:0x0146} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0176 A:{SYNTHETIC, Splitter: B:68:0x0176} */
    /* JADX WARNING: Removed duplicated region for block: B:79:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x010b A:{SYNTHETIC, Splitter: B:45:0x010b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleAppStartForbidden(String packageName) {
        Exception e;
        Throwable th;
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(new File(OPPO_ROM_BLACK_LIST_FILE));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream2, null);
                String value = null;
                String currentLanguage = getCurrentLanguage();
                boolean isPopup = false;
                String str = null;
                while (true) {
                    int type = parser.next();
                    String tagName = parser.getName();
                    if (type == 2) {
                        if (this.DEBUG_SWITCH) {
                            Log.i(TAG, " readRomBlackListFile tagName=" + tagName);
                        }
                        if (SoundModelContract.KEY_TYPE.equals(tagName)) {
                            value = parser.getAttributeValue(null, "value");
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readRomBlackListFile value=" + value);
                            }
                        } else if ("startForbidden".equals(tagName)) {
                            String pkgName = parser.nextText();
                            if (packageName.equals(pkgName)) {
                                isPopup = true;
                            }
                            if (this.DEBUG_SWITCH) {
                                Log.i(TAG, " readRomBlackListFile pkgName=" + pkgName);
                            }
                        }
                        if (isPopup && currentLanguage != null) {
                            String content;
                            if (currentLanguage.equals(tagName)) {
                                content = parser.nextText();
                                if (!content.equals(IElsaManager.EMPTY_PACKAGE)) {
                                    str = content;
                                    break;
                                }
                            } else if ("en-US".equals(tagName)) {
                                content = parser.nextText();
                                if (!content.equals(IElsaManager.EMPTY_PACKAGE)) {
                                    str = content;
                                }
                            }
                        }
                        if (type != 1) {
                            break;
                        }
                    } else {
                        if (type == 3 && value != null && value.equals(tagName)) {
                            isPopup = false;
                        }
                        if (type != 1) {
                        }
                    }
                }
                if (str != null) {
                    if (this.DEBUG_SWITCH) {
                        Log.i(TAG, " readRomBlackListFile contentText=" + str);
                    }
                    parseForbidText(str);
                }
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
                    if (stream == null) {
                        try {
                            stream.close();
                        } catch (IOException e22) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e22);
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e222) {
                            Log.e(TAG, "Failed to close state FileInputStream " + e222);
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
            Log.e(TAG, "failed parsing ", e);
            if (stream == null) {
            }
        }
    }

    private String getCurrentLanguage() {
        Locale locale;
        if (VERSION.SDK_INT >= 24) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        if (locale != null) {
            return locale.getLanguage() + "-" + locale.getCountry();
        }
        return null;
    }

    private void parseForbidText(String text) {
        int length = text.length();
        int firstIndex = text.indexOf("#");
        if (firstIndex >= 0 && firstIndex + 1 < length) {
            int secondIndex = text.indexOf("#", firstIndex + 1);
            if (secondIndex >= 0 && secondIndex + 1 < length) {
                this.mDialogTitleText = text.substring(0, firstIndex);
                this.mDialogContentText = text.substring(firstIndex + 1, secondIndex);
                this.mDialogButtonText = text.substring(secondIndex + 1, text.length());
            }
        }
    }

    public String getDialogTitleText() {
        if (this.mDialogTitleText == null || this.mDialogTitleText.length() <= 0) {
            return null;
        }
        return this.mDialogTitleText;
    }

    public String getDialogContentText() {
        if (this.mDialogContentText == null || this.mDialogContentText.length() <= 0) {
            return null;
        }
        return this.mDialogContentText;
    }

    public String getDialogButtonText() {
        if (this.mDialogButtonText == null || this.mDialogButtonText.length() <= 0) {
            return null;
        }
        return this.mDialogButtonText;
    }

    public void resetDialogShowText() {
        this.mDialogTitleText = null;
        this.mDialogContentText = null;
        this.mDialogButtonText = null;
    }
}
