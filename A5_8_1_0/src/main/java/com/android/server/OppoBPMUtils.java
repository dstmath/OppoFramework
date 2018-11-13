package com.android.server;

import android.net.arp.OppoArpPeer;
import android.os.FileObserver;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.Xml;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.am.OppoProcessManager;
import com.android.server.display.OppoBrightUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class OppoBPMUtils {
    private static final String APP_WIDGET_PATH = "/data/oppo/coloros/bpm/appwidgets.xml";
    private static final String BPM_PATH = "/data/oppo/coloros/bpm/bpm.xml";
    private static final String CUSTOMIZE_APP_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final long FIRSTMASK_FREEZE = 1;
    private static final int FREEZE_BROADCAST_ACTION_BLACK_LIST = 7;
    private static final int FREEZE_BROADCAST_ACTION_WHITE_LIST = 6;
    private static final int FREEZE_INVISIBLE_WHITE = 1;
    private static final int FREEZE_NEW_POLICY_BROADCAST_ACTION_WHITEL_LIST = 9;
    private static final int FREEZE_NEW_POLICY_BROADCAST_PKG_WHITEL_LIST = 8;
    private static final int FREEZE_STRICT_MODE_WHITE = 3;
    private static final int FREEZE_SYSTEM_APP_BLACK = 4;
    private static final int FREEZE_THIRD_APP_BLACK = 5;
    private static final int FREEZE_VISIBLE_WHITE = 2;
    private static final String POWER_CONN_STATUS_PATH = "/data/oppo/coloros/bpm/power_connection_status.xml";
    private static final String SYS_ELSA_CONFIG_FILE = "/data/oppo/coloros/bpm/sys_elsa_config_list.xml";
    private static final String SYS_ELSA_DIR = "/data/oppo/coloros/bpm";
    private static final String TAG = "OppoProcessManager";
    private static OppoBPMUtils sBpmUtils;
    private static final List<Integer> sFreezeTypeList = Arrays.asList(new Integer[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(9)});
    private int mAppChangeCheckTime = OppoBrightUtils.HIGH_BRIGHTNESS_MAX_LUX;
    private ArrayList<String> mAppWidgetList = new ArrayList();
    private FileObserver mAppWidgetObserver = null;
    private ArrayList<String> mAssociateKeyList = new ArrayList();
    private ArrayMap<String, String> mAssociateKeyMap = new ArrayMap();
    private FileObserver mBpmFileObserver = null;
    private ArrayList<String> mBrdBlackList = new ArrayList();
    private ArrayList<String> mBrdWhiteKeyList = new ArrayList();
    private ArrayList<String> mBrdWhiteList = new ArrayList();
    private ArrayList<String> mCustomizeAppList = new ArrayList();
    private ArrayList<String> mDisplayDeviceList = new ArrayList();
    private FileObserver mElsaConfigObserver = null;
    private boolean mFreezeSwitch = true;
    private ArrayList<String> mGlobalWhiteList = new ArrayList();
    private ArrayList<String> mInVisibleList = new ArrayList();
    private boolean mLogSwitch = OppoProcessManager.sDebugDetail;
    private ArrayList<String> mNewPolicyBrdActionWhiteList = new ArrayList();
    private boolean mNewPolicyBrdEnable = false;
    private ArrayList<String> mNewPolicyBrdPkgWhiteList = new ArrayList();
    private OppoProcessManager mOppoProcessManager = null;
    private int mPayModeEnterTime = OppoArpPeer.ARP_DUP_RESPONSE_TIMEOUT;
    private boolean mPayModeSwitch = true;
    private int mPeriodCheckTime = 180000;
    private boolean mPowerConnectStatus;
    private FileObserver mPowerStatusObserver = null;
    private int mRecentTaskNum = 3;
    private int mRecentTaskStore = 9;
    private int mScreenOffCheckTime = OppoBrightUtils.HIGH_BRIGHTNESS_MAX_LUX;
    private long mStartFromNotityTime = 10000;
    private boolean mStatisticsSwitch = false;
    private int mStrictModeEnterTime = OppoBrightUtils.SPECIAL_AMBIENT_LIGHT_HORIZON;
    private ArrayList<String> mStrictModeList = new ArrayList();
    private boolean mStrictModeSwitch = true;
    private ArrayList<String> mSysAppBlackList = new ArrayList();
    private ArrayList<String> mThirdAppBlackList = new ArrayList();
    private ArrayList<String> mVisibleList = new ArrayList();

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
            if (OppoBPMUtils.SYS_ELSA_CONFIG_FILE.equals(this.mFocusPath)) {
                OppoProcessManager.getInstance().sendBpmEmptyMessage(100, 0);
            } else if (OppoBPMUtils.BPM_PATH.equals(this.mFocusPath)) {
                OppoProcessManager.getInstance().sendBpmEmptyMessage(101, 0);
            } else if (OppoBPMUtils.POWER_CONN_STATUS_PATH.equals(this.mFocusPath)) {
                OppoProcessManager.getInstance().sendBpmEmptyMessage(103, 0);
            }
        }
    }

    private OppoBPMUtils() {
    }

    public static final synchronized OppoBPMUtils getInstance() {
        OppoBPMUtils oppoBPMUtils;
        synchronized (OppoBPMUtils.class) {
            if (sBpmUtils == null) {
                sBpmUtils = new OppoBPMUtils();
            }
            oppoBPMUtils = sBpmUtils;
        }
        return oppoBPMUtils;
    }

    public void initData(OppoProcessManager oppoBpmManager) {
        this.mOppoProcessManager = oppoBpmManager;
        initFileData();
        initFileObserver();
    }

    private void initFileData() {
        try {
            File elsaDir = new File(SYS_ELSA_DIR);
            if (!elsaDir.exists()) {
                elsaDir.mkdirs();
            }
            File elsaFile = new File(SYS_ELSA_CONFIG_FILE);
            if (elsaFile.exists()) {
                readElsaConfigFile();
            } else {
                elsaFile.createNewFile();
            }
            File bpmFile = new File(BPM_PATH);
            if (bpmFile.exists()) {
                readBpmFile();
            } else {
                bpmFile.createNewFile();
            }
            File appWidget = new File(APP_WIDGET_PATH);
            if (appWidget.exists()) {
                readWidgetFile();
            } else {
                appWidget.createNewFile();
            }
            File powerStateFile = new File(POWER_CONN_STATUS_PATH);
            if (powerStateFile.exists()) {
                readPowerStateFile();
            } else {
                powerStateFile.createNewFile();
            }
            if (new File(CUSTOMIZE_APP_PATH).exists()) {
                readCustomWhiteList();
            }
        } catch (Exception e) {
            Slog.e("OppoProcessManager", "initDir failed " + e);
        }
    }

    private void initFileObserver() {
        this.mElsaConfigObserver = new FileObserverPolicy(SYS_ELSA_CONFIG_FILE);
        this.mElsaConfigObserver.startWatching();
        this.mBpmFileObserver = new FileObserverPolicy(BPM_PATH);
        this.mBpmFileObserver.startWatching();
        this.mPowerStatusObserver = new FileObserverPolicy(POWER_CONN_STATUS_PATH);
        this.mPowerStatusObserver.startWatching();
    }

    private void clearList() {
        this.mInVisibleList.clear();
        this.mStrictModeList.clear();
        this.mSysAppBlackList.clear();
        this.mThirdAppBlackList.clear();
        this.mBrdWhiteList.clear();
        this.mBrdBlackList.clear();
        this.mNewPolicyBrdActionWhiteList.clear();
        this.mNewPolicyBrdPkgWhiteList.clear();
        this.mBrdWhiteKeyList.clear();
        this.mAssociateKeyList.clear();
        this.mAssociateKeyMap.clear();
    }

    public void readElsaConfigFile() {
        Exception e;
        File file = new File(SYS_ELSA_CONFIG_FILE);
        clearList();
        try {
            InputStream fileInputStream = new FileInputStream(file);
            InputStream inputStream;
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileInputStream, null);
                int type;
                do {
                    type = parser.next();
                    if (type == 2) {
                        String tagName = parser.getName();
                        if (this.mLogSwitch) {
                            Slog.d("OppoProcessManager", " readConfigFromFileLocked tagName=" + tagName);
                        }
                        if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(tagName)) {
                            String key = parser.getAttributeValue(null, "k");
                            String value = parser.getAttributeValue(null, "v");
                            if (!(TextUtils.isEmpty(key) || (TextUtils.isEmpty(value) ^ 1) == 0)) {
                                addWhiteList(key, value);
                            }
                        } else if ("recentTast".equals(tagName)) {
                            setRecentTaskValue(parser.getAttributeValue(null, "num"), parser.getAttributeValue(null, "store"));
                        } else if ("checkTime".equals(tagName)) {
                            setCheckTime(parser.getAttributeValue(null, "screenOff"), parser.getAttributeValue(null, "appChange"), parser.getAttributeValue(null, "period"));
                        } else if ("modeSwitch".equals(tagName)) {
                            setModeSwitch(parser.getAttributeValue(null, "strict"), parser.getAttributeValue(null, "pay"));
                        } else if ("modeEnterTime".equals(tagName)) {
                            setModeEnterTime(parser.getAttributeValue(null, "strict"), parser.getAttributeValue(null, "pay"));
                        } else if (OppoProcessManager.RESUME_REASON_SWITCH_CHANGE_STR.equals(tagName)) {
                            setSwitch(parser.getAttributeValue(null, "freeze"), parser.getAttributeValue(null, "statistic"), parser.getAttributeValue(null, "newPolicyBrd"));
                        } else if ("brdWhiteKey".equals(tagName)) {
                            setBrdWhiteKeyList(parser.getAttributeValue(null, "k"));
                        } else if ("associateKey".equals(tagName)) {
                            setAssocationKeyList(parser.getAttributeValue(null, "k"));
                        }
                    }
                } while (type != 1);
                inputStream = fileInputStream;
            } catch (Exception e2) {
                e = e2;
                inputStream = fileInputStream;
                Slog.e("OppoProcessManager", "readElsaConfigFile failed " + e);
            }
        } catch (Exception e3) {
            e = e3;
            Slog.e("OppoProcessManager", "readElsaConfigFile failed " + e);
        }
    }

    public void readBpmFile() {
        this.mVisibleList.clear();
        this.mVisibleList.addAll(loadListFile(BPM_PATH));
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x0098 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0071 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x008f A:{SYNTHETIC, Splitter: B:37:0x008f} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0083 A:{SYNTHETIC, Splitter: B:31:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0071 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0098 A:{RETURN} */
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
                        Slog.e("OppoProcessManager", "failed parsing ", e);
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
                Slog.e("OppoProcessManager", "failed parsing ", e);
                if (stream != null) {
                }
                if (success) {
                }
            }
            if (success) {
                return ret;
            }
            return emptyList;
        }
        Slog.e("OppoProcessManager", path + " file don't exist!");
        return emptyList;
    }

    public void readPowerStateFile() {
        List<String> tempList = loadListFile(POWER_CONN_STATUS_PATH);
        if (tempList != null && tempList.size() == 1) {
            boolean z;
            if (((String) tempList.get(0)).equals("true")) {
                z = true;
            } else {
                z = false;
            }
            this.mPowerConnectStatus = z;
        }
    }

    public void readWidgetFile() {
        List<String> appWidgetList = loadListFile(APP_WIDGET_PATH);
        synchronized (this.mAppWidgetList) {
            this.mAppWidgetList.clear();
            this.mAppWidgetList.addAll(loadListFile(APP_WIDGET_PATH));
        }
    }

    public void readCustomWhiteList() {
        this.mCustomizeAppList.clear();
        this.mCustomizeAppList.addAll(loadListFile(CUSTOMIZE_APP_PATH));
    }

    private void addWhiteList(String key, String value) {
        long maskInvisibleWhite = getFreezeListMask(1);
        long maskVisibleWhite = getFreezeListMask(2);
        long maskStrictModeWhite = getFreezeListMask(3);
        long maskSysAppBlack = getFreezeListMask(4);
        long maskThirdAppBlack = getFreezeListMask(5);
        long maskBrdWhite = getFreezeListMask(6);
        long maskBrdBlack = getFreezeListMask(7);
        long maskNewPolicyBrdPkgWhite = getFreezeListMask(8);
        long maskNewPolicyBrdAcionWhite = getFreezeListMask(9);
        long maskValue = Long.parseLong(value);
        if (!((maskValue & maskInvisibleWhite) == 0 || this.mInVisibleList.contains(key))) {
            this.mInVisibleList.add(key);
            if (this.mLogSwitch) {
                Slog.d("OppoProcessManager", "addWhiteList invisible " + key);
            }
        }
        if (!((maskValue & maskStrictModeWhite) == 0 || this.mStrictModeList.contains(key))) {
            this.mStrictModeList.add(key);
            if (this.mLogSwitch) {
                Slog.d("OppoProcessManager", "addWhiteList strict " + key);
            }
        }
        if (!((maskValue & maskSysAppBlack) == 0 || this.mSysAppBlackList.contains(key))) {
            this.mSysAppBlackList.add(key);
            if (this.mLogSwitch) {
                Slog.d("OppoProcessManager", "addWhiteList sysApp black " + key);
            }
        }
        if (!((maskValue & maskThirdAppBlack) == 0 || this.mThirdAppBlackList.contains(key))) {
            this.mThirdAppBlackList.add(key);
            Slog.d("OppoProcessManager", "addWhiteList third black " + key);
        }
        if (!((maskValue & maskBrdWhite) == 0 || this.mBrdWhiteList.contains(key))) {
            this.mBrdWhiteList.add(key);
            if (this.mLogSwitch) {
                Slog.d("OppoProcessManager", "addWhiteList brd white " + key);
            }
        }
        if (!((maskValue & maskBrdBlack) == 0 || this.mBrdBlackList.contains(key))) {
            this.mBrdBlackList.add(key);
            if (this.mLogSwitch) {
                Slog.d("OppoProcessManager", "addWhiteList brd black " + key);
            }
        }
        if (!((maskValue & maskNewPolicyBrdPkgWhite) == 0 || this.mNewPolicyBrdPkgWhiteList.contains(key))) {
            this.mNewPolicyBrdPkgWhiteList.add(key);
            if (this.mLogSwitch) {
                Slog.d("OppoProcessManager", "addWhiteList new policy brd pkg " + key);
            }
        }
        if ((maskValue & maskNewPolicyBrdAcionWhite) != 0 && !this.mNewPolicyBrdActionWhiteList.contains(key)) {
            this.mNewPolicyBrdActionWhiteList.add(key);
            if (this.mLogSwitch) {
                Slog.d("OppoProcessManager", "addWhiteList new policy brd action " + key);
            }
        }
    }

    private void setRecentTaskValue(String num, String store) {
        if (!TextUtils.isEmpty(num) && (TextUtils.isEmpty(store) ^ 1) != 0) {
            try {
                this.mRecentTaskNum = Integer.parseInt(num);
                this.mRecentTaskStore = Integer.parseInt(store);
            } catch (Exception e) {
                if (this.mLogSwitch) {
                    Slog.e("OppoProcessManager", "recentTask value failed " + e);
                }
            }
        }
    }

    private void setCheckTime(String screenOffTime, String appChangeTime, String periodTime) {
        if (!TextUtils.isEmpty(screenOffTime) && (TextUtils.isEmpty(appChangeTime) ^ 1) != 0 && (TextUtils.isEmpty(periodTime) ^ 1) != 0) {
            try {
                this.mScreenOffCheckTime = Integer.parseInt(screenOffTime);
                this.mAppChangeCheckTime = Integer.parseInt(appChangeTime);
                this.mPeriodCheckTime = Integer.parseInt(periodTime);
            } catch (Exception e) {
                if (this.mLogSwitch) {
                    Slog.e("OppoProcessManager", "setCheckTime value failed " + e);
                }
            }
        }
    }

    private void setModeSwitch(String strict, String pay) {
        if (!TextUtils.isEmpty(strict) && (TextUtils.isEmpty(pay) ^ 1) != 0) {
            try {
                this.mStrictModeSwitch = Boolean.parseBoolean(strict);
                this.mPayModeSwitch = Boolean.parseBoolean(pay);
            } catch (Exception e) {
                this.mStrictModeSwitch = true;
                this.mPayModeSwitch = true;
                if (this.mLogSwitch) {
                    Slog.e("OppoProcessManager", "setModeSwitch value failed " + e);
                }
            }
        }
    }

    private void setModeEnterTime(String strict, String pay) {
        if (!TextUtils.isEmpty(strict) && (TextUtils.isEmpty(pay) ^ 1) != 0) {
            try {
                this.mStrictModeEnterTime = Integer.parseInt(strict);
                this.mPayModeEnterTime = Integer.parseInt(pay);
            } catch (Exception e) {
                if (this.mLogSwitch) {
                    Slog.e("OppoProcessManager", "setModeEnterTime value failed " + e);
                }
            }
        }
    }

    private void setSwitch(String freezeSwitch, String statisticSwitch, String newPolicyBrdSwitch) {
        if (!TextUtils.isEmpty(freezeSwitch) && (TextUtils.isEmpty(statisticSwitch) ^ 1) != 0 && (TextUtils.isEmpty(newPolicyBrdSwitch) ^ 1) != 0) {
            try {
                this.mFreezeSwitch = Boolean.parseBoolean(freezeSwitch);
                this.mStatisticsSwitch = Boolean.parseBoolean(statisticSwitch);
                this.mNewPolicyBrdEnable = Boolean.parseBoolean(newPolicyBrdSwitch);
            } catch (Exception e) {
                this.mFreezeSwitch = true;
                this.mStatisticsSwitch = false;
                this.mNewPolicyBrdEnable = false;
                Slog.e("OppoProcessManager", "setSwitch value failed " + e);
            }
        }
    }

    private void setBrdWhiteKeyList(String key) {
        if (!TextUtils.isEmpty(key)) {
            this.mBrdWhiteKeyList.add(key);
        }
    }

    private void setAssocationKeyList(String key) {
        if (!TextUtils.isEmpty(key)) {
            try {
                int index = key.indexOf("#");
                String pkgName = key.substring(0, index);
                String cpnName = key.substring(index + 1, key.length());
                if (!TextUtils.isEmpty(pkgName) && (TextUtils.isEmpty(cpnName) ^ 1) != 0) {
                    if (cpnName.equals("all")) {
                        this.mAssociateKeyList.add(pkgName);
                    } else {
                        this.mAssociateKeyMap.put(cpnName, pkgName);
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public int getRecentTaskNum() {
        return this.mRecentTaskNum;
    }

    public int getRecentTaskStore() {
        return this.mRecentTaskStore;
    }

    public int getScreenOffCheckTime() {
        return this.mScreenOffCheckTime;
    }

    public int getAppChangeCheckTime() {
        return this.mAppChangeCheckTime;
    }

    public int getPeriodCheckTime() {
        return this.mPeriodCheckTime;
    }

    public boolean getStrictModeSwitch() {
        return this.mStrictModeSwitch;
    }

    public int getStrictModeEnterTime() {
        return this.mStrictModeEnterTime;
    }

    public boolean getPayModeSwitch() {
        return this.mPayModeSwitch;
    }

    public int getPayModeEnterTime() {
        return this.mPayModeEnterTime;
    }

    public ArrayList<String> getInVisibleList() {
        return this.mInVisibleList;
    }

    public ArrayList<String> getVisibleList() {
        return this.mVisibleList;
    }

    public ArrayList<String> getStrictModeList() {
        return this.mStrictModeList;
    }

    public ArrayList<String> getSysBlackList() {
        return this.mSysAppBlackList;
    }

    public ArrayList<String> getThirdAppBlackList() {
        return this.mThirdAppBlackList;
    }

    public ArrayList<String> getBrdWhiteList() {
        return this.mBrdWhiteList;
    }

    public ArrayList<String> getBrdBlackList() {
        return this.mBrdBlackList;
    }

    public ArrayList<String> getNewPolicyBrdPkgWhiteList() {
        return this.mNewPolicyBrdPkgWhiteList;
    }

    public ArrayList<String> getNewPolicyBrdActionWhiteList() {
        return this.mNewPolicyBrdActionWhiteList;
    }

    public ArrayList<String> getAppWidgetList() {
        return this.mAppWidgetList;
    }

    public ArrayList<String> getCustomizeAppList() {
        return this.mCustomizeAppList;
    }

    public boolean getPowerConnectStatus() {
        return this.mPowerConnectStatus;
    }

    public ArrayList<String> getGlobalWhiteList() {
        return this.mGlobalWhiteList;
    }

    public ArrayList<String> getDisplayDeviceList() {
        return this.mDisplayDeviceList;
    }

    public long getStartFromNotityTime() {
        return this.mStartFromNotityTime;
    }

    public boolean getFreezeSwitch() {
        return this.mFreezeSwitch;
    }

    public boolean getStatisticsSwitch() {
        return this.mStatisticsSwitch;
    }

    public boolean getNewPolicyBrdEnable() {
        return this.mNewPolicyBrdEnable;
    }

    public ArrayList<String> getBrdWhiteKeyList() {
        return this.mBrdWhiteKeyList;
    }

    public ArrayList<String> getAssociateKeyList() {
        return this.mAssociateKeyList;
    }

    public ArrayMap<String, String> getAssociateKeyMap() {
        return this.mAssociateKeyMap;
    }

    public boolean addPkgToAppWidgetList(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        synchronized (this.mAppWidgetList) {
            this.mAppWidgetList.add(pkgName);
            saveAppWidgetLocked(this.mAppWidgetList);
        }
        OppoProcessManager.getInstance().sendBpmEmptyMessage(102, 0);
        return true;
    }

    public boolean removePkgFromAppWidgetList(String pkgName) {
        boolean result = false;
        if (pkgName == null) {
            return false;
        }
        synchronized (this.mAppWidgetList) {
            for (String pkg : this.mAppWidgetList) {
                if (pkgName.equals(pkg)) {
                    this.mAppWidgetList.remove(pkg);
                    saveAppWidgetLocked(this.mAppWidgetList);
                    result = true;
                    break;
                }
            }
        }
        OppoProcessManager.getInstance().sendBpmEmptyMessage(102, 0);
        return result;
    }

    public void addPkgToDisplayDeviceList(String pkgName) {
        if (pkgName != null) {
            synchronized (this.mDisplayDeviceList) {
                if (!this.mDisplayDeviceList.contains(pkgName)) {
                    this.mDisplayDeviceList.add(pkgName);
                }
            }
            OppoProcessManager.getInstance().sendBpmEmptyMessage(110, 0);
        }
    }

    public void removePkgFromDisplayDeviceList(String pkgName) {
        if (pkgName != null) {
            synchronized (this.mDisplayDeviceList) {
                if (this.mDisplayDeviceList.contains(pkgName)) {
                    this.mDisplayDeviceList.remove(pkgName);
                }
            }
            OppoProcessManager.getInstance().sendBpmEmptyMessage(110, 0);
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

    /* JADX WARNING: Removed duplicated region for block: B:56:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0088 A:{SYNTHETIC, Splitter: B:22:0x0088} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00e5 A:{SYNTHETIC, Splitter: B:42:0x00e5} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void saveAppWidgetLocked(List<String> appWidgetList) {
        Exception e;
        Throwable th;
        File file = new File(APP_WIDGET_PATH);
        FileOutputStream stream = null;
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
                XmlSerializer out = Xml.newSerializer();
                out.setOutput(stream2, "utf-8");
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "gs");
                for (String pkg : appWidgetList) {
                    if (pkg != null) {
                        out.startTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                        out.attribute(null, "att", pkg);
                        out.endTag(null, OppoCrashClearManager.CRASH_CLEAR_NAME);
                    }
                }
                out.endTag(null, "gs");
                out.endDocument();
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (Exception e3) {
                        if (this.mLogSwitch) {
                            Slog.e("OppoProcessManager", "Failed to close state FileInputStream " + e3);
                        }
                    }
                }
            } catch (Exception e4) {
                e3 = e4;
                stream = stream2;
                try {
                    Slog.e("OppoProcessManager", "saveAppWidgetLocked failed " + e3);
                    if (stream == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (Exception e32) {
                            if (this.mLogSwitch) {
                                Slog.e("OppoProcessManager", "Failed to close state FileInputStream " + e32);
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
        } catch (Exception e5) {
            e32 = e5;
            Slog.e("OppoProcessManager", "saveAppWidgetLocked failed " + e32);
            if (stream == null) {
                try {
                    stream.close();
                } catch (Exception e322) {
                    if (this.mLogSwitch) {
                        Slog.e("OppoProcessManager", "Failed to close state FileInputStream " + e322);
                    }
                }
            }
        }
    }

    public static long getFreezeListMask(int type) {
        int index = sFreezeTypeList.indexOf(Integer.valueOf(type));
        if (index != -1) {
            return 1 << index;
        }
        return 1;
    }
}
