package com.android.server.am;

import android.os.Build.VERSION;
import android.os.Environment;
import android.os.FileObserver;
import android.os.LocaleList;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import com.android.server.LocationManagerService;
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
    private static final String OPPO_ASSOCIATE_START_WHITE_FILE_PATH = "/data/oppo/coloros/startup/associate_startup_whitelist.txt";
    private static final String OPPO_CUSTOMIZE_WHITE_FILE_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final String OPPO_ROM_BLACK_LIST_FILE = "/data/oppo/coloros/startup/sys_rom_black_list.xml";
    private static final String OPPO_STARTUP_MANAGER_FILE_PATH = "/data/oppo/coloros/startup/startup_manager.xml";
    private static final String OPPO_STARTUP_MONITOR_FILE_PATH = "/data/oppo/coloros/startup/sys_startupmanager_monitor_list.xml";
    private static final String OPPO_TENCENT_INTERCEPT_PATH = "/data/oppo/coloros/startup/tenIntercept.xml";
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
    private static final String TAG = "OppoAppStartupManager";
    private static OppoAppStartupManagerUtils sAsmUtils = null;
    private List<String> mAamActivityWhiteList = new ArrayList();
    private final Object mAamActivityWhiteListLock = new Object();
    private List<String> mAamProviderWhiteList = new ArrayList();
    private final Object mAamProviderWhiteListLock = new Object();
    private List<String> mActionBlackList = new ArrayList();
    private List<String> mActivityBlackList = new ArrayList();
    private List<String> mActivityCalledKeyList = new ArrayList();
    private List<String> mActivityCalledWhiteCpnList = new ArrayList();
    private List<String> mActivityCalledWhitePkgList = new ArrayList();
    private List<String> mActivityCallerWhitePkgList = new ArrayList();
    private List<String> mActivityPkgKeyList = new ArrayList();
    private List<String> mAssociateDefaultList = new ArrayList();
    private List<String> mAssociateStartWhiteList = new ArrayList();
    private FileObserverPolicy mAssociateStartupFileObserver = null;
    private List<String> mAuthorizeCpnList = new ArrayList();
    private List<String> mBindServiceWhiteCpnList = new ArrayList();
    private List<String> mBlackguardActivityList = new ArrayList();
    private List<String> mBlackguardList = new ArrayList();
    private List<String> mBroadcastActionWhiteList = new ArrayList();
    private List<String> mBroadcastWhitePkgList = new ArrayList();
    private List<String> mBuildAppBlackList = new ArrayList();
    private int mCallCheckCount = 50;
    private int mCheckCount = 200;
    private List<String> mCollectAppStartList = new ArrayList();
    private List<String> mCustomizeWhiteList = new ArrayList();
    private boolean mDebugSwitch = this.mDegugDetail;
    private boolean mDegugDetail = OppoAppStartupManager.DEBUG_DETAIL;
    private String mDialogButtonText = null;
    private String mDialogContentText = null;
    private String mDialogTitleText = null;
    private FileObserverPolicy mInterceptTenFileObserver = null;
    private List<String> mJobWhiteList = new ArrayList();
    private List<String> mNotifyWhiteList = new ArrayList();
    private List<String> mPayCpnList = new ArrayList();
    private List<String> mProtectWhiteList = new ArrayList();
    private List<String> mProviderBlackList = new ArrayList();
    private List<String> mProviderWhiteCpnList = new ArrayList();
    private List<String> mReceiverActionBlackList = new ArrayList();
    private List<String> mReceiverBlackList = new ArrayList();
    private List<String> mSeviceCpnBlacklist = new ArrayList();
    private List<String> mSougouSiteList = new ArrayList();
    private List<String> mStartServiceWhiteCpnList = new ArrayList();
    private List<String> mStartServiceWhiteList = new ArrayList();
    private FileObserverPolicy mStartupManagerFileObserver = null;
    private FileObserverPolicy mStartupMonitorFileObserver = null;
    private boolean mSwitchBrowserInterceptUpload = false;
    private boolean mSwitchInterceptActivity = false;
    private boolean mSwitchStatus = false;
    private List<String> mSyncWhiteList = new ArrayList();
    private String mTencentInterceptCpn = "com.tencent.mm.plugin.webview.ui.tools.WebViewDownloadUI";
    private int mTencentInterceptSwitchValue = 0;

    private class FileObserverPolicy extends FileObserver {
        private String mFocusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.mFocusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event != 8) {
                return;
            }
            if (this.mFocusPath.equals(OppoAppStartupManagerUtils.OPPO_STARTUP_MANAGER_FILE_PATH)) {
                Log.i("OppoAppStartupManager", "focusPath OPPO_STARTUP_MANAGER_FILE_PATH!");
                OppoAppStartupManagerUtils.this.readStartupManagerFile();
                OppoAppStartupManager.getInstance().updateConfiglist();
            } else if (this.mFocusPath.equals(OppoAppStartupManagerUtils.OPPO_STARTUP_MONITOR_FILE_PATH)) {
                Log.i("OppoAppStartupManager", "focusPath OPPO_STARTUP_MONITOR_FILE_PATH!");
                OppoAppStartupManagerUtils.this.readStartupMonitorFile();
                OppoAppStartupManager.getInstance().updateMonitorlist();
            } else if (this.mFocusPath.equals(OppoAppStartupManagerUtils.OPPO_ASSOCIATE_START_WHITE_FILE_PATH)) {
                Log.i("OppoAppStartupManager", "focusPath OPPO_ASSOCIATE_START_WHITE_FILE_PATH!");
                OppoAppStartupManagerUtils.this.readAssociateStartWhiteFile();
                OppoAppStartupManager.getInstance().updateAssociateStartList();
            } else if (this.mFocusPath.equals(OppoAppStartupManagerUtils.OPPO_TENCENT_INTERCEPT_PATH)) {
                Log.i("OppoAppStartupManager", "focusPath OPPO_TEN_INTERCEPT_PATH!");
                OppoAppStartupManagerUtils.this.readTenInterceptFile();
            }
        }
    }

    private OppoAppStartupManagerUtils() {
        initDir();
        initFileObserver();
        readStartupManagerFile();
        readStartupMonitorFile();
        readCustomizeWhiteList();
        readAssociateStartWhiteFile();
        readTenInterceptFile();
    }

    public static OppoAppStartupManagerUtils getInstance() {
        if (sAsmUtils == null) {
            sAsmUtils = new OppoAppStartupManagerUtils();
        }
        return sAsmUtils;
    }

    private void initDir() {
        Log.i("OppoAppStartupManager", "initDir start");
        File startupManagerFile = new File(OPPO_STARTUP_MANAGER_FILE_PATH);
        File startupMonitorFile = new File(OPPO_STARTUP_MONITOR_FILE_PATH);
        File associateStartupFile = new File(OPPO_ASSOCIATE_START_WHITE_FILE_PATH);
        File interceptTenFile = new File(OPPO_TENCENT_INTERCEPT_PATH);
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
            if (!associateStartupFile.exists()) {
                associateStartupFile.createNewFile();
            }
            if (!interceptTenFile.exists()) {
                interceptTenFile.createNewFile();
            }
        } catch (IOException e) {
            Log.e("OppoAppStartupManager", "initDir failed!!!");
            e.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003f A:{SYNTHETIC, Splitter: B:17:0x003f} */
    /* JADX WARNING: Removed duplicated region for block: B:50:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0044 A:{Catch:{ Exception -> 0x0074 }} */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x003f A:{SYNTHETIC, Splitter: B:17:0x003f} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0044 A:{Catch:{ Exception -> 0x0074 }} */
    /* JADX WARNING: Removed duplicated region for block: B:50:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0093 A:{SYNTHETIC, Splitter: B:35:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0098 A:{Catch:{ Exception -> 0x009c }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0093 A:{SYNTHETIC, Splitter: B:35:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0098 A:{Catch:{ Exception -> 0x009c }} */
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
                            Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e2);
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
                        Log.e("OppoAppStartupManager", "initStartupManagerFile Failed " + e2);
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
                Log.e("OppoAppStartupManager", "initStartupManagerFile Failed " + e2);
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (Exception e22) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e22);
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
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e222);
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
            Log.e("OppoAppStartupManager", "initStartupManagerFile Failed " + e222);
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
        if (this.mDebugSwitch) {
            Log.i("OppoAppStartupManager", "readStartupManagerFile start");
        }
        readConfigFromFileLocked(new File(OPPO_STARTUP_MANAGER_FILE_PATH));
    }

    /* JADX WARNING: Removed duplicated region for block: B:355:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x01ce A:{SYNTHETIC, Splitter: B:59:0x01ce} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x012e A:{Splitter: B:9:0x0034, ExcHandler: java.lang.NullPointerException (e java.lang.NullPointerException)} */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0247 A:{Splitter: B:9:0x0034, ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException)} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x02ce A:{Splitter: B:9:0x0034, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0355 A:{Splitter: B:9:0x0034, ExcHandler: java.lang.IndexOutOfBoundsException (e java.lang.IndexOutOfBoundsException)} */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x03dc A:{Splitter: B:9:0x0034, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:37:0x012e, code:
            r21 = e;
     */
    /* JADX WARNING: Missing block: B:38:0x012f, code:
            r40 = r0;
     */
    /* JADX WARNING: Missing block: B:54:0x01ba, code:
            r22 = e;
     */
    /* JADX WARNING: Missing block: B:55:0x01bb, code:
            r40 = r0;
     */
    /* JADX WARNING: Missing block: B:71:0x0247, code:
            r23 = e;
     */
    /* JADX WARNING: Missing block: B:72:0x0248, code:
            r40 = r0;
     */
    /* JADX WARNING: Missing block: B:88:0x02ce, code:
            r19 = e;
     */
    /* JADX WARNING: Missing block: B:89:0x02cf, code:
            r40 = r0;
     */
    /* JADX WARNING: Missing block: B:105:0x0355, code:
            r20 = e;
     */
    /* JADX WARNING: Missing block: B:106:0x0356, code:
            r40 = r0;
     */
    /* JADX WARNING: Missing block: B:122:0x03dc, code:
            r46 = th;
     */
    /* JADX WARNING: Missing block: B:123:0x03dd, code:
            r40 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readConfigFromFileLocked(File file) {
        if (this.mDebugSwitch) {
            Log.i("OppoAppStartupManager", "readConfigFromFileLocked start");
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
                        if (this.mDebugSwitch) {
                            Log.i("OppoAppStartupManager", " readFromFileLocked tagName=" + tagName);
                        }
                        if (SEVICECPN_NAME.equals(tagName)) {
                            String sevicecpn = parser.nextText();
                            if (!sevicecpn.equals("")) {
                                this.mSeviceCpnBlacklist.add(sevicecpn);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked sevicecpn = " + sevicecpn);
                                }
                            }
                        } else if (RECEIVER_NAME.equals(tagName)) {
                            String receiver = parser.nextText();
                            if (!receiver.equals("")) {
                                this.mReceiverBlackList.add(receiver);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked receiver = " + receiver);
                                }
                            }
                        } else if (RECEIVER_ACTION_NAME.equals(tagName)) {
                            String receiverAction = parser.nextText();
                            if (!receiverAction.equals("")) {
                                this.mReceiverActionBlackList.add(receiverAction);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked receiverAction = " + receiverAction);
                                }
                            }
                        } else if ("provider".equals(tagName)) {
                            String provider = parser.nextText();
                            if (!provider.equals("")) {
                                this.mProviderBlackList.add(provider);
                                if (this.mDegugDetail) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked provider = " + provider);
                                }
                            }
                        } else if ("activity".equals(tagName)) {
                            String activity = parser.nextText();
                            if (!activity.equals("")) {
                                this.mActivityBlackList.add(activity);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked activity = " + activity);
                                }
                            }
                        } else if (ACTION_NAME.equals(tagName)) {
                            String action = parser.nextText();
                            if (!action.equals("")) {
                                this.mActionBlackList.add(action);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked action = " + action);
                                }
                            }
                        } else if (BLACKGUARD_NAME.equals(tagName)) {
                            String blackguard = parser.nextText();
                            if (!blackguard.equals("")) {
                                this.mBlackguardList.add(blackguard);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked blackguard = " + blackguard);
                                }
                            }
                        } else if (ACTIVITY_INTERCEPT_SWITCH.equals(tagName)) {
                            String isIntercept = parser.nextText();
                            if (!isIntercept.equals("")) {
                                setSwitchInterceptActivity(isIntercept);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked isIntercept = " + isIntercept);
                                }
                            }
                        } else if (BROWSER_INTERCEPT_UPLOAD_SWITCH.equals(tagName)) {
                            String isAllowed = parser.nextText();
                            if (!isAllowed.equals("")) {
                                setSwitchBrowserInterceptUpload(isAllowed);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked isBrowserInterceptUploadAllowed = " + isAllowed);
                                }
                            }
                        } else if (ACTIVITY_CALLER_WHITE_PKG.equals(tagName)) {
                            String callerWhitePkg = parser.nextText();
                            if (!callerWhitePkg.equals("")) {
                                this.mActivityCallerWhitePkgList.add(callerWhitePkg);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked activityCallerWhitePkg = " + callerWhitePkg);
                                }
                            }
                        } else if (ACTIVITY_CALLED_WHITE_CPN.equals(tagName)) {
                            String calledWhiteCpn = parser.nextText();
                            if (!calledWhiteCpn.equals("")) {
                                this.mActivityCalledWhiteCpnList.add(calledWhiteCpn);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked activityCalledWhiteCpn = " + calledWhiteCpn);
                                }
                            }
                        } else if (ACTIVITY_CALLED_WHITE_PKG.equals(tagName)) {
                            String calledWhitePkg = parser.nextText();
                            if (!calledWhitePkg.equals("")) {
                                this.mActivityCalledWhitePkgList.add(calledWhitePkg);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked activityCalledWhitePkg = " + calledWhitePkg);
                                }
                            }
                        } else if (ACTIVITY_PKG_KEY.equals(tagName)) {
                            String pkgKey = parser.nextText();
                            if (!pkgKey.equals("")) {
                                this.mActivityPkgKeyList.add(pkgKey);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked activityPkgKey = " + pkgKey);
                                }
                            }
                        } else if (ACTIVITY_CALLED_KEY.equals(tagName)) {
                            String calledKey = parser.nextText();
                            if (!calledKey.equals("")) {
                                this.mActivityCalledKeyList.add(calledKey);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked activityCalledKey = " + calledKey);
                                }
                            }
                        } else if (BLACKGUARD_ACTIVITY.equals(tagName)) {
                            String blackguardActivity = parser.nextText();
                            if (!blackguardActivity.equals("")) {
                                this.mBlackguardActivityList.add(blackguardActivity);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked blackguardActivity = " + blackguardActivity);
                                }
                            }
                        } else if (SITE_SOUGOU.equals(tagName)) {
                            String sougouSite = parser.nextText();
                            if (!sougouSite.equals("")) {
                                this.mSougouSiteList.add(sougouSite);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked sougouSite = " + sougouSite);
                                }
                            }
                        } else if (BUILD_BLACK_PKG.equals(tagName)) {
                            String buildBlackPkg = parser.nextText();
                            if (!buildBlackPkg.equals("")) {
                                this.mBuildAppBlackList.add(buildBlackPkg);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked buildBlackPkg = " + buildBlackPkg);
                                }
                            }
                        } else if (START_SERVICE_WHITE_PKG.equals(tagName)) {
                            String startServicePkg = parser.nextText();
                            if (!startServicePkg.equals("")) {
                                this.mStartServiceWhiteList.add(startServicePkg);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked startServicePkg = " + startServicePkg);
                                }
                            }
                        } else if (START_SERVICE_WHITE_CPN.equals(tagName)) {
                            String startServiceCpn = parser.nextText();
                            if (!startServiceCpn.equals("")) {
                                this.mStartServiceWhiteCpnList.add(startServiceCpn);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked startServiceCpn = " + startServiceCpn);
                                }
                            }
                        } else if (BIND_SERVICE_WHITE_CPN.equals(tagName)) {
                            String bindServiceCpn = parser.nextText();
                            if (!bindServiceCpn.equals("")) {
                                this.mBindServiceWhiteCpnList.add(bindServiceCpn);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked bindServiceCpn = " + bindServiceCpn);
                                }
                            }
                        } else if (JOB_WHITE_PKG.equals(tagName)) {
                            String jobWhitepkg = parser.nextText();
                            if (!jobWhitepkg.equals("")) {
                                this.mJobWhiteList.add(jobWhitepkg);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked jobWhitepkg = " + jobWhitepkg);
                                }
                            }
                        } else if (SYNC_WHITE_PKG.equals(tagName)) {
                            String syncWhitePkg = parser.nextText();
                            if (!syncWhitePkg.equals("")) {
                                this.mSyncWhiteList.add(syncWhitePkg);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked syncWhitePkg = " + syncWhitePkg);
                                }
                            }
                        } else if (NOTIFY_WHITE_PKG.equals(tagName)) {
                            String notifyWhitePkg = parser.nextText();
                            if (!notifyWhitePkg.equals("")) {
                                this.mNotifyWhiteList.add(notifyWhitePkg);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked notifyWhitePkg = " + notifyWhitePkg);
                                }
                            }
                        } else if (PROVIDER_WHITE_CPN.equals(tagName)) {
                            String providerCpn = parser.nextText();
                            if (!providerCpn.equals("")) {
                                this.mProviderWhiteCpnList.add(providerCpn);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked providerCpn = " + providerCpn);
                                }
                            }
                        } else if (BROADCAST_WHITE_PKG.equals(tagName)) {
                            String broadcastPkg = parser.nextText();
                            if (!broadcastPkg.equals("")) {
                                this.mBroadcastWhitePkgList.add(broadcastPkg);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked broadcastPkg = " + broadcastPkg);
                                }
                            }
                        } else if (BROADCAST_WHITE_CPN.equals(tagName)) {
                            String broadcastAction = parser.nextText();
                            if (!broadcastAction.equals("")) {
                                this.mBroadcastActionWhiteList.add(broadcastAction);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked broadcastaction = " + broadcastAction);
                                }
                            }
                        } else if (PROTECT_PKG.equals(tagName)) {
                            String protectPkg = parser.nextText();
                            if (!protectPkg.equals("")) {
                                this.mProtectWhiteList.add(protectPkg);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked protectPkg = " + protectPkg);
                                }
                            }
                        } else if (CHECK_COUNT.equals(tagName)) {
                            checkCount = parser.nextText();
                            if (!checkCount.equals("")) {
                                this.mCheckCount = Integer.parseInt(checkCount);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked checkCount = " + this.mCheckCount);
                                }
                            }
                        } else if (SWITCH_STATUS.equals(tagName)) {
                            String switchStatus = parser.nextText();
                            if (!switchStatus.equals("")) {
                                this.mSwitchStatus = Boolean.parseBoolean(switchStatus);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked switchStatus = " + this.mSwitchStatus);
                                }
                            }
                        } else if (PAY_CPN.equals(tagName)) {
                            String payPkg = parser.nextText();
                            if (!payPkg.equals("")) {
                                this.mPayCpnList.add(payPkg);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked payPkg = " + payPkg);
                                }
                            }
                        } else if (AUTHORIZE_COMPONENT_NAME.equals(tagName)) {
                            String authorizeCpnName = parser.nextText();
                            if (!authorizeCpnName.equals("")) {
                                this.mAuthorizeCpnList.add(authorizeCpnName);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked authorizeCpnName = " + authorizeCpnName);
                                }
                            }
                        } else if (AAM_ACTIVITY_WHITE_PKG.equals(tagName)) {
                            String aamActivityWhitePkg = parser.nextText();
                            if (!aamActivityWhitePkg.equals("")) {
                                synchronized (this.mAamActivityWhiteListLock) {
                                    this.mAamActivityWhiteList.add(aamActivityWhitePkg);
                                }
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked aamActivityWhitePkg = " + aamActivityWhitePkg);
                                }
                            }
                        } else if (AAM_PROVIDER_WHITE_PKG.equals(tagName)) {
                            String aamProviderWhitePkg = parser.nextText();
                            if (!aamProviderWhitePkg.equals("")) {
                                synchronized (this.mAamProviderWhiteListLock) {
                                    this.mAamProviderWhiteList.add(aamProviderWhitePkg);
                                }
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked aamProviderWhitePkg = " + aamProviderWhitePkg);
                                }
                            }
                        }
                    }
                } while (type != 1);
            } catch (NumberFormatException e) {
                Log.i("OppoAppStartupManager", " readFromFileLocked checkCount failed, checkCount= " + checkCount);
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
                    Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e6);
                }
            }
            InputStream inputStream = fileInputStream2;
        } catch (NullPointerException e7) {
            Throwable e8 = e7;
            try {
                Log.e("OppoAppStartupManager", "failed parsing ", e8);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e62) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e62);
                    }
                }
            } catch (Throwable th2) {
                Throwable th3 = th2;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e622) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e622);
                    }
                }
                throw th3;
            }
        } catch (NumberFormatException e9) {
            Throwable e10 = e9;
            Log.e("OppoAppStartupManager", "failed parsing ", e10);
            if (fileInputStream == null) {
                try {
                    fileInputStream.close();
                } catch (IOException e6222) {
                    Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e6222);
                }
            }
        } catch (XmlPullParserException e11) {
            Throwable e12 = e11;
            Log.e("OppoAppStartupManager", "failed parsing ", e12);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e62222) {
                    Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e62222);
                }
            }
        } catch (IOException e13) {
            Throwable e14 = e13;
            Log.e("OppoAppStartupManager", "failed IOException ", e14);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e622222) {
                    Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e622222);
                }
            }
        } catch (IndexOutOfBoundsException e15) {
            Throwable e16 = e15;
            Log.e("OppoAppStartupManager", "failed parsing ", e16);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e6222222) {
                    Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e6222222);
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
        if (this.mDebugSwitch) {
            Log.i("OppoAppStartupManager", "readStartupMonitorFile start");
        }
        readMonitorFromFileLocked(new File(OPPO_STARTUP_MONITOR_FILE_PATH));
    }

    /* JADX WARNING: Removed duplicated region for block: B:79:0x01d6 A:{SYNTHETIC, Splitter: B:79:0x01d6} */
    /* JADX WARNING: Removed duplicated region for block: B:97:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00de A:{SYNTHETIC, Splitter: B:39:0x00de} */
    /* JADX WARNING: Removed duplicated region for block: B:105:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x01b1 A:{SYNTHETIC, Splitter: B:73:0x01b1} */
    /* JADX WARNING: Removed duplicated region for block: B:103:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0183 A:{SYNTHETIC, Splitter: B:65:0x0183} */
    /* JADX WARNING: Removed duplicated region for block: B:101:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0155 A:{SYNTHETIC, Splitter: B:57:0x0155} */
    /* JADX WARNING: Removed duplicated region for block: B:99:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0127 A:{SYNTHETIC, Splitter: B:49:0x0127} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readMonitorFromFileLocked(File file) {
        IOException e;
        NullPointerException e2;
        Throwable th;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        if (this.mDebugSwitch) {
            Log.i("OppoAppStartupManager", "readMonitorFromFileLocked start");
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
                        if (this.mDebugSwitch) {
                            Log.i("OppoAppStartupManager", " readFromFileLocked tagName=" + tagName);
                        }
                        if (CALL_CHECK_COUNT.equals(tagName)) {
                            String checkCount = parser.nextText();
                            if (!checkCount.equals("")) {
                                setCallCheckCount(checkCount);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked checkCount = " + checkCount);
                                }
                            }
                        } else if (COLLECT_APP_START_PKG.equals(tagName)) {
                            String monitorApp = parser.nextText();
                            if (!monitorApp.equals("")) {
                                this.mCollectAppStartList.add(monitorApp);
                                if (this.mDebugSwitch) {
                                    Log.i("OppoAppStartupManager", " readFromFileLocked collect app pkg = " + monitorApp);
                                }
                            }
                        }
                    }
                } while (type != 1);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e6) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e6);
                    }
                }
                stream = stream2;
            } catch (NullPointerException e7) {
                e2 = e7;
                stream = stream2;
                try {
                    Log.e("OppoAppStartupManager", "failed parsing ", e2);
                    if (stream == null) {
                        try {
                            stream.close();
                        } catch (IOException e62) {
                            Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e62);
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e622) {
                            Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e622);
                        }
                    }
                    throw th;
                }
            } catch (NumberFormatException e8) {
                e3 = e8;
                stream = stream2;
                Log.e("OppoAppStartupManager", "failed parsing ", e3);
                if (stream == null) {
                    try {
                        stream.close();
                    } catch (IOException e6222) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e6222);
                    }
                }
            } catch (XmlPullParserException e9) {
                e4 = e9;
                stream = stream2;
                Log.e("OppoAppStartupManager", "failed parsing ", e4);
                if (stream == null) {
                    try {
                        stream.close();
                    } catch (IOException e62222) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e62222);
                    }
                }
            } catch (IOException e10) {
                e62222 = e10;
                stream = stream2;
                Log.e("OppoAppStartupManager", "failed IOException ", e62222);
                if (stream == null) {
                    try {
                        stream.close();
                    } catch (IOException e622222) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e622222);
                    }
                }
            } catch (IndexOutOfBoundsException e11) {
                e5 = e11;
                stream = stream2;
                Log.e("OppoAppStartupManager", "failed parsing ", e5);
                if (stream == null) {
                    try {
                        stream.close();
                    } catch (IOException e6222222) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e6222222);
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                stream = stream2;
                if (stream != null) {
                }
                throw th;
            }
        } catch (NullPointerException e12) {
            e2 = e12;
            Log.e("OppoAppStartupManager", "failed parsing ", e2);
            if (stream == null) {
            }
        } catch (NumberFormatException e13) {
            e3 = e13;
            Log.e("OppoAppStartupManager", "failed parsing ", e3);
            if (stream == null) {
            }
        } catch (XmlPullParserException e14) {
            e4 = e14;
            Log.e("OppoAppStartupManager", "failed parsing ", e4);
            if (stream == null) {
            }
        } catch (IOException e15) {
            e6222222 = e15;
            Log.e("OppoAppStartupManager", "failed IOException ", e6222222);
            if (stream == null) {
            }
        } catch (IndexOutOfBoundsException e16) {
            e5 = e16;
            Log.e("OppoAppStartupManager", "failed parsing ", e5);
            if (stream == null) {
            }
        }
    }

    protected void cleanMonitorList() {
        if (!this.mCollectAppStartList.isEmpty()) {
            this.mCollectAppStartList.clear();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:66:0x014a A:{SYNTHETIC, Splitter: B:66:0x014a} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x011c A:{SYNTHETIC, Splitter: B:58:0x011c} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00ee A:{SYNTHETIC, Splitter: B:50:0x00ee} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00c2 A:{SYNTHETIC, Splitter: B:42:0x00c2} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0096 A:{SYNTHETIC, Splitter: B:34:0x0096} */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x016f A:{SYNTHETIC, Splitter: B:72:0x016f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readCustomizeWhiteList() {
        IOException e;
        NullPointerException e2;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        Throwable th;
        if (this.mDebugSwitch) {
            Log.i("OppoAppStartupManager", "readCustomizeWhiteList start");
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
                            Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e6);
                        }
                    }
                    stream = stream2;
                } catch (NullPointerException e7) {
                    e2 = e7;
                    stream = stream2;
                    Log.e("OppoAppStartupManager", "failed parsing ", e2);
                    if (stream != null) {
                    }
                    return;
                } catch (NumberFormatException e8) {
                    e3 = e8;
                    stream = stream2;
                    Log.e("OppoAppStartupManager", "failed parsing ", e3);
                    if (stream != null) {
                    }
                    return;
                } catch (XmlPullParserException e9) {
                    e4 = e9;
                    stream = stream2;
                    Log.e("OppoAppStartupManager", "failed parsing ", e4);
                    if (stream != null) {
                    }
                    return;
                } catch (IOException e10) {
                    e6 = e10;
                    stream = stream2;
                    Log.e("OppoAppStartupManager", "failed IOException ", e6);
                    if (stream != null) {
                    }
                    return;
                } catch (IndexOutOfBoundsException e11) {
                    e5 = e11;
                    stream = stream2;
                    try {
                        Log.e("OppoAppStartupManager", "failed parsing ", e5);
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
                            Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e62);
                        }
                    }
                    throw th;
                }
            } catch (NullPointerException e12) {
                e2 = e12;
                Log.e("OppoAppStartupManager", "failed parsing ", e2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e622) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e622);
                    }
                }
                return;
            } catch (NumberFormatException e13) {
                e3 = e13;
                Log.e("OppoAppStartupManager", "failed parsing ", e3);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e6222);
                    }
                }
                return;
            } catch (XmlPullParserException e14) {
                e4 = e14;
                Log.e("OppoAppStartupManager", "failed parsing ", e4);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e62222) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e62222);
                    }
                }
                return;
            } catch (IOException e15) {
                e62222 = e15;
                Log.e("OppoAppStartupManager", "failed IOException ", e62222);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e622222) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e622222);
                    }
                }
                return;
            } catch (IndexOutOfBoundsException e16) {
                e5 = e16;
                Log.e("OppoAppStartupManager", "failed parsing ", e5);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222222) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e6222222);
                    }
                }
                return;
            }
            return;
        }
        if (this.mDebugSwitch) {
            Log.e("OppoAppStartupManager", "readCustomizeWhiteList failed: file doesn't exist!");
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
                Log.e("OppoAppStartupManager", "associateStartFile read execption: " + e);
            }
            return;
        }
        if (this.mDebugSwitch) {
            Log.e("OppoAppStartupManager", "associateStartFile failed: file doesn't exist!");
        }
    }

    protected void cleanAssociateStartWhiteList() {
        if (!this.mAssociateStartWhiteList.isEmpty()) {
            this.mAssociateStartWhiteList.clear();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:63:0x0139 A:{SYNTHETIC, Splitter: B:63:0x0139} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00fa A:{SYNTHETIC, Splitter: B:50:0x00fa} */
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
                                    if (this.mDebugSwitch) {
                                        Log.i("OppoAppStartupManager", "mTencentInterceptSwitchValue " + this.mTencentInterceptSwitchValue);
                                    }
                                }
                            } else if ("label".equals(tag)) {
                                interceptSwitch = parser.getAttributeValue(null, OppoProcessManager.RESUME_REASON_SWITCH_CHANGE_STR);
                                intereptType = parser.getAttributeValue(null, SoundModelContract.KEY_TYPE);
                                if (interceptSwitch != null && intereptType != null && interceptSwitch.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && intereptType.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                                    this.mTencentInterceptSwitchValue += 2;
                                    if (this.mDebugSwitch) {
                                        Log.i("OppoAppStartupManager", "mTencentInterceptSwitchValue " + this.mTencentInterceptSwitchValue);
                                    }
                                }
                            } else if ("cpn".equals(tag)) {
                                String cpn = parser.nextText();
                                if (!cpn.equals("")) {
                                    this.mTencentInterceptCpn = cpn;
                                }
                            }
                        }
                    } while (type != 1);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e2) {
                            Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e2);
                        }
                    }
                    fileInputStream = stream;
                } catch (Exception e3) {
                    e = e3;
                    fileInputStream = stream;
                    try {
                        Log.e("OppoAppStartupManager", "failed parsing ", e);
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e22) {
                                Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e22);
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
                            Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e222);
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                Log.e("OppoAppStartupManager", "failed parsing ", e);
                if (fileInputStream != null) {
                }
                return;
            }
            return;
        }
        if (this.mDebugSwitch) {
            Log.e("OppoAppStartupManager", "tenInterceptFile failed: file doesn't exist!");
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
        this.mDebugSwitch = this.mDegugDetail | OppoAppStartupManager.getInstance().mDynamicDebug;
    }

    public boolean getSwitchStatus() {
        return this.mSwitchStatus;
    }

    public int getCheckCount() {
        return this.mCheckCount;
    }

    /* JADX WARNING: Removed duplicated region for block: B:76:0x0099 A:{SYNTHETIC, EDGE_INSN: B:76:0x0099->B:22:0x0099 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x014d A:{LOOP_END, LOOP:0: B:5:0x001e->B:57:0x014d} */
    /* JADX WARNING: Removed duplicated region for block: B:79:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0111 A:{SYNTHETIC, Splitter: B:45:0x0111} */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x017e A:{SYNTHETIC, Splitter: B:68:0x017e} */
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
                        if (this.mDebugSwitch) {
                            Log.i("OppoAppStartupManager", " readRomBlackListFile tagName=" + tagName);
                        }
                        if (SoundModelContract.KEY_TYPE.equals(tagName)) {
                            value = parser.getAttributeValue(null, "value");
                            if (this.mDebugSwitch) {
                                Log.i("OppoAppStartupManager", " readRomBlackListFile value=" + value);
                            }
                        } else if ("startForbidden".equals(tagName)) {
                            String pkgName = parser.nextText();
                            if (packageName.equals(pkgName)) {
                                isPopup = true;
                            }
                            if (this.mDebugSwitch) {
                                Log.i("OppoAppStartupManager", " readRomBlackListFile pkgName=" + pkgName);
                            }
                        }
                        if (isPopup && currentLanguage != null) {
                            String content;
                            if (currentLanguage.equals(tagName)) {
                                content = parser.nextText();
                                if (!content.equals("")) {
                                    str = content;
                                    break;
                                }
                            } else if ("en-US".equals(tagName)) {
                                content = parser.nextText();
                                if (!content.equals("")) {
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
                    if (this.mDebugSwitch) {
                        Log.i("OppoAppStartupManager", " readRomBlackListFile contentText=" + str);
                    }
                    parseForbidText(str);
                }
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e2) {
                        Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e2);
                    }
                }
                stream = stream2;
            } catch (Exception e3) {
                e = e3;
                stream = stream2;
                try {
                    Log.e("OppoAppStartupManager", "failed parsing ", e);
                    if (stream == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e22) {
                            Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e22);
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
            Log.e("OppoAppStartupManager", "failed parsing ", e);
            if (stream == null) {
                try {
                    stream.close();
                } catch (IOException e222) {
                    Log.e("OppoAppStartupManager", "Failed to close state FileInputStream " + e222);
                }
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
