package com.android.server;

import android.os.FileObserver;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.Xml;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.am.OppoProcessManager;
import com.android.server.oppo.IElsaManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

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
public class OppoBPMUtils {
    private static final String APP_WIDGET_PATH = "/data/data_bpm/appwidgets.xml";
    private static final String BLACK_APP_BRD_ACTION = "blackBrdAction";
    private static final String BLACK_APP_PATH = "/data/data_bpm/pure_background_app_blacklist.xml";
    private static final String BLACK_SYS_APP_PATH = "/data/data_bpm/bad_apps.xml";
    private static final String BPM_DIR = "/data/data_bpm";
    private static final String BPM_PATH = "/data/data_bpm/bpm.xml";
    private static final String BPM_STATUS_PATH = "/data/data_bpm/bpm_sts.xml";
    private static final String BRD_PATH = "/data/data_bpm/brd.xml";
    private static final String CPR_PATH = "/data/data_bpm/cpr.xml";
    private static final String CUSTOMIZE_APP_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final String LOW_POWER_CONFIG_PATH = "/data/data_bpm/low_power_config.xml";
    private static final long MSG_DELAY_TIME = 200;
    private static final String PAYMODE_ENTER_TIME = "payModeEnterTime";
    private static final String PAYSAFE_SWITCH = "paySafeSwitch";
    private static final String PKG_PATH = "/data/data_bpm/pkg.xml";
    private static final String POWER_CONN_STATUS_PATH = "/data/data_bpm/power_connection_status.xml";
    private static final String RECENT_NUM = "recentNum";
    private static final String RECENT_STORE = "recentStore";
    private static final String RECORD_SWITCH = "recordSwitch";
    private static final String SCREEN_OFF_CHECK_TIME = "screenOffCheckTime";
    private static final String SCREEN_ON_CHECK_TIME = "screenOnCheckTime";
    private static final String SMART_LOW_POWER_PATH = "/data/data_bpm/pure_background_smart_low_power.xml";
    private static final String START_FROM_NOTITY_TIME = "startFromNotityTime";
    private static final String STRICTMODE_ENTER_TIME = "strictModeEnterTime";
    private static final String STRICTMODE_SWITCH = "strictModeSwitch";
    private static final String STRICTMODE_WHITE_PKG = "strictModeWhitePkg";
    private static final String SYS_PUREBKG_CONFIG_PATH = "/data/data_bpm/sys_purebkg_config.xml";
    private static final String TAG = "OppoProcessManager";
    private static OppoBPMUtils mOppoBPMUtils;
    private FileObserverPolicy mAppWidgetFileObserver;
    private List<String> mAppWidgetList;
    private final Object mAppWidgetLock;
    private FileObserverPolicy mBPMConfigFileObserver;
    private List<String> mBlackAppBrdList;
    private FileObserverPolicy mBlackAppFileObserver;
    private List<String> mBlackAppList;
    private FileObserverPolicy mBlackSysAppFileObserver;
    private List<String> mBlackSysAppList;
    private FileObserverPolicy mBpmFileObserver;
    private List<String> mBpmList;
    private FileObserverPolicy mBrdFileObserver;
    private List<String> mBrdList;
    private FileObserverPolicy mCprFileObserver;
    private List<String> mCprList;
    private List<String> mCustomizeAppList;
    private boolean mDebugDetail;
    private List<String> mDisplayDeviceList;
    private final Object mDisplayDeviceListLock;
    private boolean mLowPower;
    private FileObserverPolicy mLowPowerFileObserver;
    private OppoProcessManager mOppoBpmManager;
    public long mPayModeEnterTime;
    private boolean mPaySafeSwitch;
    private FileObserverPolicy mPkgFileObserver;
    private List<String> mPkgList;
    private boolean mPowerConnStatus;
    private FileObserverPolicy mPowerConnStsFileObserver;
    private int mRecentNum;
    private int mRecentStore;
    private boolean mRecordSwitch;
    public long mScreenOffCheckTime;
    public long mScreenOnCheckTime;
    private boolean mSmartLowPower;
    private FileObserverPolicy mSmartLowPowerFileObserver;
    public long mStartFromNotityTime;
    public long mStrictModeEnterTime;
    private boolean mStrictModeSwitch;
    private List<String> mStrictWhitePkgList;
    private final Object mStrictWhitePkgListLock;

    private class FileObserverPolicy extends FileObserver {
        private String focusPath;

        public FileObserverPolicy(String path) {
            super(path, 8);
            this.focusPath = path;
        }

        public void onEvent(int event, String path) {
            if (event == 8 && !this.focusPath.equals(OppoBPMUtils.BPM_STATUS_PATH)) {
                if (this.focusPath.equals(OppoBPMUtils.BPM_PATH)) {
                    OppoBPMUtils.this.mOppoBpmManager.sendBpmEmptyMessage(101, OppoBPMUtils.MSG_DELAY_TIME);
                } else if (this.focusPath.equals(OppoBPMUtils.PKG_PATH)) {
                    OppoBPMUtils.this.mOppoBpmManager.sendBpmEmptyMessage(102, 0);
                } else if (this.focusPath.equals(OppoBPMUtils.BRD_PATH)) {
                    OppoBPMUtils.this.mOppoBpmManager.sendBpmEmptyMessage(103, 0);
                } else if (this.focusPath.equals(OppoBPMUtils.CPR_PATH)) {
                    OppoBPMUtils.this.mOppoBpmManager.sendBpmEmptyMessage(112, 0);
                } else if (this.focusPath.equals(OppoBPMUtils.BLACK_SYS_APP_PATH)) {
                    OppoBPMUtils.this.mOppoBpmManager.sendBpmEmptyMessage(104, 0);
                } else if (this.focusPath.equals(OppoBPMUtils.APP_WIDGET_PATH)) {
                    OppoBPMUtils.this.mOppoBpmManager.sendBpmEmptyMessage(105, OppoBPMUtils.MSG_DELAY_TIME);
                } else if (this.focusPath.equals(OppoBPMUtils.BLACK_APP_PATH)) {
                    OppoBPMUtils.this.mOppoBpmManager.sendBpmEmptyMessage(106, 0);
                } else if (this.focusPath.equals(OppoBPMUtils.POWER_CONN_STATUS_PATH)) {
                    OppoBPMUtils.this.mOppoBpmManager.sendBpmEmptyMessage(107, OppoBPMUtils.MSG_DELAY_TIME);
                } else if (this.focusPath.equals(OppoBPMUtils.SMART_LOW_POWER_PATH)) {
                    OppoBPMUtils.this.mOppoBpmManager.sendBpmEmptyMessage(108, 0);
                } else if (this.focusPath.equals(OppoBPMUtils.LOW_POWER_CONFIG_PATH)) {
                    OppoBPMUtils.this.mOppoBpmManager.sendBpmEmptyMessage(109, 0);
                } else if (this.focusPath.equals(OppoBPMUtils.SYS_PUREBKG_CONFIG_PATH)) {
                    OppoBPMUtils.this.mOppoBpmManager.sendBpmEmptyMessage(110, 0);
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.OppoBPMUtils.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.OppoBPMUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.OppoBPMUtils.<clinit>():void");
    }

    private OppoBPMUtils() {
        this.mScreenOnCheckTime = 60000;
        this.mScreenOffCheckTime = 60000;
        this.mStrictModeEnterTime = 60000;
        this.mPayModeEnterTime = 5000;
        this.mStartFromNotityTime = 10000;
        this.mDebugDetail = OppoProcessManager.sDebugDetail;
        this.mAppWidgetLock = new Object();
        this.mDisplayDeviceListLock = new Object();
        this.mStrictWhitePkgListLock = new Object();
        this.mBpmFileObserver = null;
        this.mPkgFileObserver = null;
        this.mBrdFileObserver = null;
        this.mCprFileObserver = null;
        this.mBlackSysAppFileObserver = null;
        this.mBlackAppFileObserver = null;
        this.mAppWidgetFileObserver = null;
        this.mPowerConnStsFileObserver = null;
        this.mSmartLowPowerFileObserver = null;
        this.mLowPowerFileObserver = null;
        this.mBPMConfigFileObserver = null;
        this.mPowerConnStatus = false;
        this.mSmartLowPower = false;
        this.mLowPower = false;
        this.mRecordSwitch = false;
        this.mRecentNum = 3;
        this.mRecentStore = 9;
        this.mStrictModeSwitch = true;
        this.mPaySafeSwitch = false;
        this.mBpmList = new ArrayList();
        this.mPkgList = new ArrayList();
        this.mBrdList = new ArrayList();
        this.mCprList = new ArrayList();
        this.mBlackSysAppList = new ArrayList();
        this.mAppWidgetList = new ArrayList();
        this.mBlackAppList = new ArrayList();
        this.mBlackAppBrdList = new ArrayList();
        this.mCustomizeAppList = new ArrayList();
        this.mDisplayDeviceList = new ArrayList();
        this.mStrictWhitePkgList = new ArrayList();
        this.mOppoBpmManager = null;
        initDir();
        initData();
        initFileObserver();
    }

    public void init(OppoProcessManager oppoBpmManager) {
        this.mOppoBpmManager = oppoBpmManager;
    }

    public static OppoBPMUtils getInstance() {
        if (mOppoBPMUtils == null) {
            mOppoBPMUtils = new OppoBPMUtils();
        }
        return mOppoBPMUtils;
    }

    private void initDir() {
        try {
            File file = new File(BPM_DIR);
            if (!file.exists()) {
                file.mkdirs();
            }
            copyFile("/system/bpm/bpm_sts.xml", BPM_STATUS_PATH);
            copyFile("/system/bpm/bpm.xml", BPM_PATH);
            copyFile("/system/bpm/pkg.xml", PKG_PATH);
            copyFile("/system/bpm/brd.xml", BRD_PATH);
            copyFile("/system/bpm/cpr.xml", CPR_PATH);
            copyFile("/system/bpm/bad_apps.xml", BLACK_SYS_APP_PATH);
            copyFile("/system/bpm/appwidgets.xml", APP_WIDGET_PATH);
            copyFile("/system/bpm/sys_purebkg_config.xml", SYS_PUREBKG_CONFIG_PATH);
            confirmFileExist(BLACK_APP_PATH);
            confirmFileExist(POWER_CONN_STATUS_PATH);
            confirmFileExist(SMART_LOW_POWER_PATH);
            confirmFileExist(LOW_POWER_CONFIG_PATH);
            confirmFileExist(SYS_PUREBKG_CONFIG_PATH);
            changeMod();
        } catch (Exception e) {
            Slog.w("OppoProcessManager", "mkdir failed " + e);
        }
    }

    public void initData() {
        this.mBpmList = loadListFile(BPM_PATH);
        this.mPkgList = loadListFile(PKG_PATH);
        this.mBrdList = loadListFile(BRD_PATH);
        this.mCprList = loadListFile(CPR_PATH);
        this.mBlackSysAppList = loadListFile(BLACK_SYS_APP_PATH);
        this.mAppWidgetList = loadListFile(APP_WIDGET_PATH);
        this.mBlackAppList = loadListFile(BLACK_APP_PATH);
        this.mCustomizeAppList = loadListFile(CUSTOMIZE_APP_PATH);
        this.mPowerConnStatus = loadStatusFile(POWER_CONN_STATUS_PATH);
        this.mSmartLowPower = loadStatusFile(SMART_LOW_POWER_PATH);
        this.mLowPower = loadStatusFile(LOW_POWER_CONFIG_PATH);
        initDefaultStrictWhitePkgList();
        readBPMConfigFile();
    }

    private void initFileObserver() {
        this.mBpmFileObserver = new FileObserverPolicy(BPM_PATH);
        this.mBpmFileObserver.startWatching();
        this.mPkgFileObserver = new FileObserverPolicy(PKG_PATH);
        this.mPkgFileObserver.startWatching();
        this.mBrdFileObserver = new FileObserverPolicy(BRD_PATH);
        this.mBrdFileObserver.startWatching();
        this.mCprFileObserver = new FileObserverPolicy(CPR_PATH);
        this.mCprFileObserver.startWatching();
        this.mBlackSysAppFileObserver = new FileObserverPolicy(BLACK_SYS_APP_PATH);
        this.mBlackSysAppFileObserver.startWatching();
        this.mAppWidgetFileObserver = new FileObserverPolicy(APP_WIDGET_PATH);
        this.mAppWidgetFileObserver.startWatching();
        this.mBlackAppFileObserver = new FileObserverPolicy(BLACK_APP_PATH);
        this.mBlackAppFileObserver.startWatching();
        this.mPowerConnStsFileObserver = new FileObserverPolicy(POWER_CONN_STATUS_PATH);
        this.mPowerConnStsFileObserver.startWatching();
        this.mSmartLowPowerFileObserver = new FileObserverPolicy(SMART_LOW_POWER_PATH);
        this.mSmartLowPowerFileObserver.startWatching();
        this.mLowPowerFileObserver = new FileObserverPolicy(LOW_POWER_CONFIG_PATH);
        this.mLowPowerFileObserver.startWatching();
        this.mBPMConfigFileObserver = new FileObserverPolicy(SYS_PUREBKG_CONFIG_PATH);
        this.mBPMConfigFileObserver.startWatching();
    }

    private void changeMod() {
        try {
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm/bpm_sts.xml");
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm");
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm/bpm.xml");
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm/pkg.xml");
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm/brd.xml");
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm/cpr.xml");
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm/bad_apps.xml");
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm/appwidgets.xml");
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm/pure_background_app_blacklist.xml");
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm/power_connection_status.xml");
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm/pure_background_smart_low_power.xml");
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm/low_power_config.xml");
            Runtime.getRuntime().exec("chmod 744 /data/data_bpm/sys_purebkg_config.xml");
        } catch (IOException e) {
            Slog.w("OppoProcessManager", " " + e);
        }
    }

    private void copyFile(String fromFile, String toFile) throws IOException {
        File targetFile = new File(toFile);
        if (!targetFile.exists()) {
            FileUtils.copyFile(new File(fromFile), targetFile);
        }
    }

    private void confirmFileExist(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public boolean getPowerConnStatus() {
        return this.mPowerConnStatus;
    }

    public boolean getSmartLowPower() {
        return this.mSmartLowPower;
    }

    public boolean getLowPower() {
        return this.mLowPower;
    }

    public boolean getRecordSwitch() {
        return this.mRecordSwitch;
    }

    public int getRecentTaskNum() {
        return this.mRecentNum;
    }

    public int getRecentTaskStore() {
        return this.mRecentStore;
    }

    public long getScreenOnCheckTime() {
        return this.mScreenOnCheckTime;
    }

    public long getScreenOffCheckTime() {
        return this.mScreenOffCheckTime;
    }

    public boolean getStrictModeSwitch() {
        return this.mStrictModeSwitch;
    }

    public long getStrictModeEnterTime() {
        return this.mStrictModeEnterTime;
    }

    public long getPayModeEnterTime() {
        return this.mPayModeEnterTime;
    }

    public boolean getPaySafeSwitch() {
        return this.mPaySafeSwitch;
    }

    public long getStartFromNotityTime() {
        return this.mStartFromNotityTime;
    }

    public List<String> getBpmList() {
        return this.mBpmList;
    }

    public List<String> getPkgList() {
        return this.mPkgList;
    }

    public List<String> getBrdList() {
        return this.mBrdList;
    }

    public List<String> getCprList() {
        return this.mCprList;
    }

    public List<String> getBlackSysAppList() {
        return this.mBlackSysAppList;
    }

    public List<String> getAppWidgetList() {
        return this.mAppWidgetList;
    }

    public List<String> getBlackAppList() {
        return this.mBlackAppList;
    }

    public List<String> getBlackAppBrdList() {
        return this.mBlackAppBrdList;
    }

    public List<String> getCustomizeAppList() {
        return this.mCustomizeAppList;
    }

    public List<String> getDisplayDeviceList() {
        return this.mDisplayDeviceList;
    }

    public List<String> getStrictWhitePkgList() {
        List<String> list;
        synchronized (this.mStrictWhitePkgListLock) {
            list = this.mStrictWhitePkgList;
        }
        return list;
    }

    public void reLoadStatusAndListFile(int updateMsg) {
        switch (updateMsg) {
            case 101:
                this.mBpmList = loadListFile(BPM_PATH);
                return;
            case 102:
                this.mPkgList = loadListFile(PKG_PATH);
                return;
            case 103:
                this.mBrdList = loadListFile(BRD_PATH);
                return;
            case 104:
                this.mBlackSysAppList = loadListFile(BLACK_SYS_APP_PATH);
                return;
            case 105:
                synchronized (this.mAppWidgetLock) {
                    this.mAppWidgetList = loadListFile(APP_WIDGET_PATH);
                }
                return;
            case 106:
                this.mBlackAppList = loadListFile(BLACK_APP_PATH);
                return;
            case 107:
                this.mPowerConnStatus = loadStatusFile(POWER_CONN_STATUS_PATH);
                return;
            case 108:
                this.mSmartLowPower = loadStatusFile(SMART_LOW_POWER_PATH);
                return;
            case 109:
                this.mLowPower = loadStatusFile(LOW_POWER_CONFIG_PATH);
                return;
            case 112:
                this.mCprList = loadListFile(CPR_PATH);
                return;
            default:
                return;
        }
    }

    public void reLoadBpmConfigFile() {
        readBPMConfigFile();
    }

    public void readBPMConfigFile() {
        if (this.mDebugDetail) {
            Slog.i("OppoProcessManager", "readBPMConfigFile start");
        }
        readConfigFromFileLocked(new File(SYS_PUREBKG_CONFIG_PATH));
    }

    private void readConfigFromFileLocked(File file) {
        Exception e;
        Throwable th;
        if (this.mDebugDetail) {
            Slog.i("OppoProcessManager", "readConfigFromFileLocked start");
        }
        if (!this.mBlackAppBrdList.isEmpty()) {
            this.mBlackAppBrdList.clear();
        }
        List<String> strictWhitePkgList = new ArrayList();
        FileInputStream fileInputStream = null;
        try {
            FileInputStream stream = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tagName = parser.getName();
                        if (this.mDebugDetail) {
                            Slog.i("OppoProcessManager", " readConfigFromFileLocked tagName=" + tagName);
                        }
                        if (RECENT_NUM.equals(tagName)) {
                            try {
                                this.mRecentNum = Integer.parseInt(parser.nextText());
                                if (this.mDebugDetail) {
                                    Slog.i("OppoProcessManager", "mRecentNum read: " + this.mRecentNum);
                                }
                            } catch (NumberFormatException e2) {
                                Slog.w("OppoProcessManager", "recentNum:Failed to translate the string to int" + e2);
                            }
                        } else if (RECENT_STORE.equals(tagName)) {
                            try {
                                this.mRecentStore = Integer.parseInt(parser.nextText());
                                if (this.mDebugDetail) {
                                    Slog.i("OppoProcessManager", "recentStore read: " + this.mRecentStore);
                                }
                            } catch (NumberFormatException e22) {
                                Slog.w("OppoProcessManager", "recentStore:Failed to translate the string to int" + e22);
                            }
                        } else if (BLACK_APP_BRD_ACTION.equals(tagName)) {
                            String action = parser.nextText();
                            if (!action.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mBlackAppBrdList.add(action);
                                if (this.mDebugDetail) {
                                    Slog.i("OppoProcessManager", " readConfigFromFileLocked brdaction = " + action);
                                }
                            }
                        } else if (RECORD_SWITCH.equals(tagName)) {
                            String isRecord = parser.nextText();
                            if (!isRecord.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mRecordSwitch = Boolean.parseBoolean(isRecord);
                                if (this.mDebugDetail) {
                                    Slog.i("OppoProcessManager", " readFromFileLocked isRecord = " + isRecord);
                                }
                            }
                        } else if (SCREEN_ON_CHECK_TIME.equals(tagName)) {
                            try {
                                this.mScreenOnCheckTime = Long.valueOf(parser.nextText()).longValue();
                                if (this.mDebugDetail) {
                                    Slog.i("OppoProcessManager", "screenOnCheckTime read: " + this.mScreenOnCheckTime);
                                }
                            } catch (NumberFormatException e222) {
                                Slog.w("OppoProcessManager", "screenOnCheckTime:Failed to translate the string to int" + e222);
                            }
                        } else if (SCREEN_OFF_CHECK_TIME.equals(tagName)) {
                            try {
                                this.mScreenOffCheckTime = Long.valueOf(parser.nextText()).longValue();
                                if (this.mDebugDetail) {
                                    Slog.i("OppoProcessManager", "screenOffCheckTime read: " + this.mScreenOffCheckTime);
                                }
                            } catch (NumberFormatException e2222) {
                                Slog.w("OppoProcessManager", "screenOffCheckTime:Failed to translate the string to int" + e2222);
                            }
                        } else if (STRICTMODE_SWITCH.equals(tagName)) {
                            String strictMode = parser.nextText();
                            if (!strictMode.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mStrictModeSwitch = Boolean.parseBoolean(strictMode);
                                if (this.mDebugDetail) {
                                    Slog.i("OppoProcessManager", " readFromFileLocked strictMode = " + strictMode);
                                }
                            }
                        } else if (STRICTMODE_ENTER_TIME.equals(tagName)) {
                            try {
                                this.mStrictModeEnterTime = Long.valueOf(parser.nextText()).longValue();
                                if (this.mDebugDetail) {
                                    Slog.i("OppoProcessManager", "strictModeEnterTime read: " + this.mStrictModeEnterTime);
                                }
                            } catch (NumberFormatException e22222) {
                                Slog.w("OppoProcessManager", "strictModeEnterTime:Failed to translate the string to int" + e22222);
                            }
                        } else if (PAYMODE_ENTER_TIME.equals(tagName)) {
                            try {
                                this.mPayModeEnterTime = Long.valueOf(parser.nextText()).longValue();
                                if (this.mDebugDetail) {
                                    Slog.i("OppoProcessManager", "payModeEnterTime read: " + this.mPayModeEnterTime);
                                }
                            } catch (NumberFormatException e222222) {
                                Slog.w("OppoProcessManager", "payModeEnterTime:Failed to translate the string to int" + e222222);
                            }
                        } else if (PAYSAFE_SWITCH.equals(tagName)) {
                            String paySafe = parser.nextText();
                            if (!paySafe.equals(IElsaManager.EMPTY_PACKAGE)) {
                                this.mPaySafeSwitch = Boolean.parseBoolean(paySafe);
                                if (this.mDebugDetail) {
                                    Slog.i("OppoProcessManager", " readFromFileLocked paySafe = " + paySafe);
                                }
                            }
                        } else if (START_FROM_NOTITY_TIME.equals(tagName)) {
                            try {
                                this.mStartFromNotityTime = Long.valueOf(parser.nextText()).longValue();
                                if (this.mDebugDetail) {
                                    Slog.i("OppoProcessManager", "startFromNotityTime read: " + this.mStartFromNotityTime);
                                }
                            } catch (NumberFormatException e2222222) {
                                Slog.w("OppoProcessManager", "startFromNotityTime:Failed to translate the string to int" + e2222222);
                            }
                        } else if (STRICTMODE_WHITE_PKG.equals(tagName)) {
                            String strictWhitePkg = parser.nextText();
                            if (!strictWhitePkg.equals(IElsaManager.EMPTY_PACKAGE)) {
                                strictWhitePkgList.add(strictWhitePkg);
                                if (this.mDebugDetail) {
                                    Slog.i("OppoProcessManager", " readFromFileLocked strictWhitePkg = " + strictWhitePkg);
                                }
                            }
                        }
                    }
                } while (type != 1);
                synchronized (this.mStrictWhitePkgListLock) {
                    if (!strictWhitePkgList.isEmpty()) {
                        this.mStrictWhitePkgList.clear();
                        this.mStrictWhitePkgList.addAll(strictWhitePkgList);
                    }
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e3) {
                        Slog.e("OppoProcessManager", "Failed to close state FileInputStream " + e3);
                    }
                }
                fileInputStream = stream;
            } catch (Exception e4) {
                e = e4;
                fileInputStream = stream;
            } catch (Throwable th2) {
                th = th2;
                fileInputStream = stream;
            }
        } catch (Exception e5) {
            e = e5;
            try {
                Slog.e("OppoProcessManager", "failed parsing ", e);
                initDefaultStrictWhitePkgList();
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e32) {
                        Slog.e("OppoProcessManager", "Failed to close state FileInputStream " + e32);
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e322) {
                        Slog.e("OppoProcessManager", "Failed to close state FileInputStream " + e322);
                    }
                }
                throw th;
            }
        }
    }

    private boolean loadStatusFile(String path) {
        List<String> tempList = loadListFile(path);
        if (tempList == null || tempList.size() != 1) {
            return false;
        }
        return ((String) tempList.get(0)).equals("true");
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x0098  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0071 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x008f A:{SYNTHETIC, Splitter: B:37:0x008f} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0083 A:{SYNTHETIC, Splitter: B:31:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0071 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0098  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<String> loadListFile(String path) {
        Exception e;
        Throwable th;
        ArrayList<String> emptyList = new ArrayList();
        File file = new File(path);
        if (file.exists()) {
            ArrayList<String> ret = new ArrayList();
            FileInputStream stream = null;
            boolean success = false;
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
                                    ret.add(value);
                                }
                            }
                        }
                    } while (type != 1);
                    success = true;
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
                        Slog.w("OppoProcessManager", "failed parsing ", e);
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        if (success) {
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
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                Slog.w("OppoProcessManager", "failed parsing ", e);
                if (stream != null) {
                }
                if (success) {
                }
            }
            if (success) {
                return ret;
            }
            Slog.w("OppoProcessManager", path + " file failed parsing!");
            return emptyList;
        }
        Slog.w("OppoProcessManager", path + " file don't exist!");
        return emptyList;
    }

    public boolean addPkgToAppWidgetList(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        synchronized (this.mAppWidgetLock) {
            this.mAppWidgetList.add(pkgName);
            saveAppWidgetLocked();
        }
        return true;
    }

    public boolean removePkgFromAppWidgetList(String pkgName) {
        boolean result = false;
        if (pkgName == null) {
            return false;
        }
        synchronized (this.mAppWidgetLock) {
            for (String pkg : this.mAppWidgetList) {
                if (pkgName.equals(pkg)) {
                    this.mAppWidgetList.remove(pkg);
                    saveAppWidgetLocked();
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public void addPkgToDisplayDeviceList(String pkgName) {
        if (pkgName != null) {
            synchronized (this.mDisplayDeviceListLock) {
                if (!this.mDisplayDeviceList.contains(pkgName)) {
                    this.mDisplayDeviceList.add(pkgName);
                }
            }
            this.mOppoBpmManager.sendBpmEmptyMessage(111, 0);
        }
    }

    public void removePkgFromDisplayDeviceList(String pkgName) {
        if (pkgName != null) {
            synchronized (this.mDisplayDeviceListLock) {
                this.mDisplayDeviceList.remove(pkgName);
            }
            this.mOppoBpmManager.sendBpmEmptyMessage(111, 0);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0081 A:{SYNTHETIC, Splitter: B:28:0x0081} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00a8 A:{SYNTHETIC, Splitter: B:36:0x00a8} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveAppWidgetLocked() {
        FileNotFoundException e;
        Throwable th;
        File file = new File(APP_WIDGET_PATH);
        FileOutputStream stream = null;
        if (this.mDebugDetail) {
            Slog.i("OppoProcessManager", "saveAppWidgetLocked mAppWidgetList.size is " + this.mAppWidgetList.size());
        }
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        try {
            FileOutputStream stream2 = new FileOutputStream(file);
            try {
                writeAppWidgetToFileLocked(stream2);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e22) {
                        if (this.mDebugDetail) {
                            Slog.e("OppoProcessManager", "Failed to close state FileInputStream " + e22);
                        }
                    }
                }
                stream = stream2;
            } catch (FileNotFoundException e3) {
                e = e3;
                stream = stream2;
                try {
                    e.printStackTrace();
                    if (stream == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e222) {
                            if (this.mDebugDetail) {
                                Slog.e("OppoProcessManager", "Failed to close state FileInputStream " + e222);
                            }
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
        } catch (FileNotFoundException e4) {
            e = e4;
            e.printStackTrace();
            if (stream == null) {
                try {
                    stream.close();
                } catch (IOException e2222) {
                    if (this.mDebugDetail) {
                        Slog.e("OppoProcessManager", "Failed to close state FileInputStream " + e2222);
                    }
                }
            }
        }
    }

    private boolean writeAppWidgetToFileLocked(FileOutputStream stream) {
        try {
            XmlSerializer out = Xml.newSerializer();
            out.setOutput(stream, "utf-8");
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, "gs");
            for (String pkg : this.mAppWidgetList) {
                if (pkg != null) {
                    out.startTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                    out.attribute(null, "att", pkg);
                    out.endTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                }
            }
            out.endTag(null, "gs");
            out.endDocument();
            return true;
        } catch (IOException e) {
            if (this.mDebugDetail) {
                Slog.e("OppoProcessManager", "Failed to write state: " + e);
            }
            return false;
        }
    }

    private void initDefaultStrictWhitePkgList() {
        synchronized (this.mStrictWhitePkgListLock) {
            this.mStrictWhitePkgList.clear();
            this.mStrictWhitePkgList.add("com.alibaba.android.rimet");
            this.mStrictWhitePkgList.add("com.tencent.mm");
            this.mStrictWhitePkgList.add("com.tencent.mobileqq");
            this.mStrictWhitePkgList.add("com.immomo.momo");
            this.mStrictWhitePkgList.add("jp.naver.line.android");
            this.mStrictWhitePkgList.add("com.coloros.gamespacesdk");
            this.mStrictWhitePkgList.add("com.zing.zalo");
            this.mStrictWhitePkgList.add("com.facebook.orca");
            this.mStrictWhitePkgList.add("com.facebook.katana");
            this.mStrictWhitePkgList.add("com.instagram.android");
            this.mStrictWhitePkgList.add("jp.naver.line.android");
            this.mStrictWhitePkgList.add("com.whatsapp");
            this.mStrictWhitePkgList.add("com.bbm");
            this.mStrictWhitePkgList.add("com.skype.raider");
            this.mStrictWhitePkgList.add("com.viber.voip");
            this.mStrictWhitePkgList.add("com.path");
            this.mStrictWhitePkgList.add("com.facebook.lite");
            this.mStrictWhitePkgList.add("com.truecaller");
            this.mStrictWhitePkgList.add("com.bsb.hike");
            this.mStrictWhitePkgList.add("com.snapchat.android");
            this.mStrictWhitePkgList.add("com.twitter.android");
            this.mStrictWhitePkgList.add("com.imo.android.imoim");
            this.mStrictWhitePkgList.add("com.google.android.gm");
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
}
