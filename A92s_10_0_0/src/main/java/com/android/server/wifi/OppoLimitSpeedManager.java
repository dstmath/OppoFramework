package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.INetd;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import oppo.util.OppoStatistics;

public class OppoLimitSpeedManager {
    private static final long AGE_TEST_INTERVAL_TIME_MS = 300000;
    private static boolean DBG = true;
    private static final int EVENT_LIMIT_SPEED_AGE_TEST = 4;
    private static final int EVENT_LIMIT_SPEED_FORNT_UID_MONITOR = 1;
    private static final int EVENT_LIMIT_SPEED_FUNCTION_SWITCH = 0;
    private static final int EVENT_LIMIT_SPEED_RSSI_MONITOR = 3;
    private static final int EVENT_LIMIT_SPEED_UPDATE_WHITE_LIST_UID = 2;
    private static final int FW_LIMIT_SPEED_OFF_STATUS = 0;
    private static final int FW_LIMIT_SPEED_ON_STATUS = 1;
    private static final String FW_LIMIT_SPEED_STATUS = "oppo_fw_limit_speed_status";
    private static final String LIMIT_SPEED_DISABLE_RTT_FROM_SLA_PATH = "/proc/sys/net/oppo_sla/disable_rtt";
    private static final String LIMIT_SPEED_ENABLE_RTT_FROM_SLA_PATH = "/proc/sys/net/oppo_sla/enable_rtt";
    private static final String LIMIT_SPEED_SPEED_SUPPORT = "oppo.traffic.limit.support";
    private static final long MONITOR_RSSI_INTERVAL_TIME_MS = 1000;
    private static final String ROM_SET_LIMIT_SPEED_FRONT_UID = "oppo_comm_traffic_change_uid";
    private static final String ROM_SET_LIMIT_SPEED_SPEED = "oppo.traffic.limitSpeed";
    private static final String ROM_SET_LIMIT_SPEED_SWITCH = "oppo_comm_traffic_limit_speed";
    private static final String ROM_SET_LIMIT_SPEED_UID = "oppo.traffic.limitUid";
    /* access modifiers changed from: private */
    public static String TAG = "OppoLimitSpeedManager";
    private static final String WIFI_LIMIT_SPEED_APPLICATION = "key_limit_app";
    private static final String WIFI_LIMIT_SPEED_DIFF_RTT = "key_diff_rtt";
    private static final String WIFI_LIMIT_SPEED_DISABLE_RTT = "key_disable_rtt";
    private static final String WIFI_LIMIT_SPEED_ENABLE_RTT = "key_enable_rtt";
    private static final String WIFI_LIMIT_SPEED_NETWORK_TYPE = "key_limit_type";
    private static final String WIFI_LIMIT_SPEED_RTT = "wifi_limit_speed";
    private static final String WIFI_LIMIT_SPEED_STATUS = "key_limit_status";
    private static final String WIFI_STATISTIC_KEY = "wifi_fool_proof";
    /* access modifiers changed from: private */
    public int STATIC_MONITOR_RSSI_COUNTOUR = 5;
    /* access modifiers changed from: private */
    public int STATIC_MONITOR_RSSI_THRESHOLD = -78;
    private boolean agingTest = false;
    private int curUid = 0;
    /* access modifiers changed from: private */
    public ClientModeImpl mClientModeImpl;
    /* access modifiers changed from: private */
    public Context mContext;
    private String[] mDeafultWhiteList = {"com.tencent.mm", "com.tencent.mobileqq", "com.immomo.momo", "com.p1.mobile.putong", "com.zenmen.palmchat", "com.sankuai.meituan", "me.ele", "com.sankuai.meituan.takeoutnew", "com.xiachufang", "com.taobao.mobile.dipei", "com.baidu.BaiduMap", "com.autonavi.minimap", "com.tencent.map", "com.sogou.map.android.maps", "com.mapbar.android.mapbarmap"};
    private FrameworkFacade mFacade;
    /* access modifiers changed from: private */
    public int mFwStatus = 0;
    /* access modifiers changed from: private */
    public InternalHandler mHandler;
    /* access modifiers changed from: private */
    public HashMap<String, Integer> mLimitSpeedPackagesAndUids = new HashMap<>();
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final INetd mNetd;
    private BroadcastReceiver mReceiver;
    private SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public int mUserId = 0;
    private WifiRomUpdateHelper mWifiRomUpdateForLimitSpeed;
    private PackageManager pkManager;
    private List<Integer> validWhiteListUid = new ArrayList();

    public OppoLimitSpeedManager(Context context, FrameworkFacade facade, ClientModeImpl mWsm) {
        this.mContext = context;
        this.mFacade = facade;
        this.mClientModeImpl = mWsm;
        this.mWifiRomUpdateForLimitSpeed = WifiRomUpdateHelper.getInstance(this.mContext);
        this.pkManager = this.mContext.getPackageManager();
        this.mFacade.getService("network_management");
        this.mNetd = getNetd(context);
        HandlerThread mHandlerThread = new HandlerThread("OppoLimitSpeedManagerThread");
        mHandlerThread.start();
        this.mHandler = new InternalHandler(mHandlerThread.getLooper());
        registerForBroadcasts();
        registerSettingsCallbacks();
        initOppoLimitSpeedWhiteList();
        agingOppoLimitSpeedTest();
    }

    private void registerForBroadcasts() {
        this.mReceiver = new BroadcastReceiver() {
            /* class com.android.server.wifi.OppoLimitSpeedManager.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (action.equals("oppo.intent.action.WIFI_ROM_UPDATE_CHANGED")) {
                        OppoLimitSpeedManager.this.mHandler.obtainMessage(2).sendToTarget();
                    } else if (action.equals("android.intent.action.PACKAGE_ADDED") || action.equals("android.intent.action.PACKAGE_REPLACED") || action.equals("android.intent.action.PACKAGE_REMOVED")) {
                        String pkgName = intent.getData().getSchemeSpecificPart();
                        synchronized (OppoLimitSpeedManager.this.mLock) {
                            if (!TextUtils.isEmpty(pkgName) && OppoLimitSpeedManager.this.mLimitSpeedPackagesAndUids.get(pkgName) != null) {
                                OppoLimitSpeedManager oppoLimitSpeedManager = OppoLimitSpeedManager.this;
                                oppoLimitSpeedManager.logD("LimitSpeed app changed, pkg=" + pkgName);
                                OppoLimitSpeedManager.this.mHandler.obtainMessage(2).sendToTarget();
                            }
                        }
                    } else if (action.equals("android.intent.action.USER_SWITCHED")) {
                        int unused = OppoLimitSpeedManager.this.mUserId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                        OppoLimitSpeedManager.this.mHandler.obtainMessage(2).sendToTarget();
                        OppoLimitSpeedManager oppoLimitSpeedManager2 = OppoLimitSpeedManager.this;
                        oppoLimitSpeedManager2.logD("LimitSpeed userId changed, userId=" + OppoLimitSpeedManager.this.mUserId);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.WIFI_ROM_UPDATE_CHANGED");
        filter.addAction("android.intent.action.USER_SWITCHED");
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, filter, null, null);
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, packageFilter, null, null);
    }

    private void unRegisterForBroadcasts() {
        BroadcastReceiver broadcastReceiver = this.mReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
        }
    }

    private static class SettingsObserver extends ContentObserver {
        private final Context mContext;
        private final Handler mHandler;
        private final HashMap<Uri, Integer> mUriEventMap = new HashMap<>();

        SettingsObserver(Context context, Handler handler) {
            super(null);
            this.mContext = context;
            this.mHandler = handler;
        }

        /* access modifiers changed from: package-private */
        public void observe(Uri uri, int what) {
            this.mUriEventMap.put(uri, Integer.valueOf(what));
            this.mContext.getContentResolver().registerContentObserver(uri, false, this);
        }

        public void onChange(boolean selfChange) {
            Log.e(OppoLimitSpeedManager.TAG, "Should never be reached.");
        }

        public void onChange(boolean selfChange, Uri uri) {
            Integer what = this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
                return;
            }
            String access$400 = OppoLimitSpeedManager.TAG;
            Log.e(access$400, "No matching event to send for URI= " + uri);
        }
    }

    private void registerSettingsCallbacks() {
        this.mSettingsObserver = new SettingsObserver(this.mContext, this.mHandler);
        this.mSettingsObserver.observe(Settings.Global.getUriFor(ROM_SET_LIMIT_SPEED_SWITCH), 0);
        this.mSettingsObserver.observe(Settings.Global.getUriFor(ROM_SET_LIMIT_SPEED_FRONT_UID), 1);
    }

    /* access modifiers changed from: private */
    public class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (OppoLimitSpeedManager.this.isSupportOppoLimitSpeedFeature() && OppoLimitSpeedManager.this.isEnableOppoLimitSpeedFeature()) {
                int i = msg.what;
                boolean mRomSelectSwith = false;
                if (i == 0) {
                    if (Settings.Global.getInt(OppoLimitSpeedManager.this.mContext.getContentResolver(), OppoLimitSpeedManager.ROM_SET_LIMIT_SPEED_SWITCH, -1) != 0) {
                        mRomSelectSwith = true;
                    }
                    boolean isEnabled = OppoLimitSpeedManager.this.isOppoLimitSpeedEnabled();
                    OppoLimitSpeedManager.this.logD("onChange limit speed switch = " + mRomSelectSwith);
                    if (mRomSelectSwith && !isEnabled) {
                        String uidString = SystemProperties.get(OppoLimitSpeedManager.ROM_SET_LIMIT_SPEED_UID, "0");
                        String limitspeedString = SystemProperties.get(OppoLimitSpeedManager.ROM_SET_LIMIT_SPEED_SPEED, "0");
                        if (uidString != null && limitspeedString != null) {
                            int uid = Integer.parseInt(uidString);
                            int limitspeed = Integer.parseInt(limitspeedString);
                            OppoLimitSpeedManager.this.logD("Set [" + uid + "] [" + limitspeed + "]");
                            OppoLimitSpeedManager.this.startOppoLimitSpeed();
                            OppoLimitSpeedManager.this.updateOppoLimitSpeedRule(uid, limitspeed);
                        }
                    } else if (!mRomSelectSwith && isEnabled) {
                        OppoLimitSpeedManager.this.stopOppoLimitSpeed();
                    }
                } else if (i == 1) {
                    int frontuid = Settings.Global.getInt(OppoLimitSpeedManager.this.mContext.getContentResolver(), OppoLimitSpeedManager.ROM_SET_LIMIT_SPEED_FRONT_UID, 0);
                    if (frontuid > 0 && OppoLimitSpeedManager.this.isEnableOppoLimitSpeedStatistic()) {
                        OppoLimitSpeedManager.this.setOppoLimitSpeedStatisticFrontUid(frontuid);
                    }
                } else if (i == 2) {
                    OppoLimitSpeedManager.this.updateOppoLimitSpeedPackagesAndUids();
                    OppoLimitSpeedManager.this.setOppoLimitSpeedWhiteApkList();
                } else if (i != 3) {
                    if (i == 4) {
                        if (!OppoLimitSpeedManager.this.isOppoLimitSpeedEnabled()) {
                            int speed = new Random().nextInt(400) + 600;
                            OppoLimitSpeedManager.this.startOppoLimitSpeed();
                            OppoLimitSpeedManager.this.logD("Set [9999] [" + speed + "]!");
                            OppoLimitSpeedManager.this.updateOppoLimitSpeedRule(9999, speed * 8);
                        } else {
                            OppoLimitSpeedManager.this.stopOppoLimitSpeed();
                        }
                        OppoLimitSpeedManager.this.mHandler.sendMessageDelayed(OppoLimitSpeedManager.this.mHandler.obtainMessage(4), OppoLimitSpeedManager.AGE_TEST_INTERVAL_TIME_MS);
                    }
                } else if (OppoLimitSpeedManager.this.mFwStatus == 1) {
                    int totalRssi = msg.arg1;
                    int count = msg.arg2;
                    WifiInfo curWifiInfo = OppoLimitSpeedManager.this.mClientModeImpl.getWifiInfo();
                    if (curWifiInfo != null) {
                        int tempRssi = curWifiInfo.getRssi();
                        if (tempRssi > -127 && tempRssi < 200) {
                            totalRssi += tempRssi;
                            count++;
                        }
                        OppoLimitSpeedManager oppoLimitSpeedManager = OppoLimitSpeedManager.this;
                        if (count == oppoLimitSpeedManager.getRomUpdateIntegerValue("OPPO_LIMIT_SPEED_MONITOR_RSSI_COUNTOUR", Integer.valueOf(oppoLimitSpeedManager.STATIC_MONITOR_RSSI_COUNTOUR)).intValue()) {
                            int avgRssi = totalRssi / count;
                            OppoLimitSpeedManager oppoLimitSpeedManager2 = OppoLimitSpeedManager.this;
                            if (avgRssi < oppoLimitSpeedManager2.getRomUpdateIntegerValue("OPPO_LIMIT_SPEED_MONITOR_RSSI_THRESHOLD", Integer.valueOf(oppoLimitSpeedManager2.STATIC_MONITOR_RSSI_THRESHOLD)).intValue()) {
                                OppoLimitSpeedManager.this.logD("[WifiRssiMonitor] is Exit RSSI [" + avgRssi + "]!");
                                OppoLimitSpeedManager.this.stopOppoLimitSpeed();
                                return;
                            }
                            count = 0;
                            totalRssi = 0;
                        }
                    }
                    OppoLimitSpeedManager.this.mHandler.sendMessageDelayed(OppoLimitSpeedManager.this.mHandler.obtainMessage(3, totalRssi, count), 1000);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void startOppoLimitSpeed() {
        try {
            this.mNetd.oppoLimitSpeedEnable();
            if (isOppoLimitSpeedEnabled()) {
                setLimitSpeedFwStatus(1);
                if (isConnectedWifi()) {
                    startWifiRssiMonitor();
                    return;
                }
                return;
            }
            logE("startOppoLimitSpeed fail!");
        } catch (Exception e) {
            logE("startOppoLimitSpeed fail : " + e);
        }
    }

    /* access modifiers changed from: private */
    public void stopOppoLimitSpeed() {
        try {
            stopWifiRssiMonitor();
            this.mNetd.oppoLimitSpeedDisable();
            if (!isOppoLimitSpeedEnabled()) {
                setLimitSpeedFwStatus(0);
                if (isEnableOppoLimitSpeedStatistic()) {
                    triggerLimitSpeedResultBack();
                    return;
                }
                return;
            }
            logE("stopOppoLimitSpeed fail!");
        } catch (Exception e) {
            logE("stopOppoLimitSpeed fail : " + e);
        }
    }

    /* access modifiers changed from: private */
    public boolean isOppoLimitSpeedEnabled() {
        boolean limitSpeedStarted = false;
        try {
            limitSpeedStarted = this.mNetd.oppoLimitSpeedIsEnable();
        } catch (Exception e) {
            logE("isOppoLimitSpeedEnabled fail : " + e);
        }
        logD("isOppoLimitSpeedEnabled is " + limitSpeedStarted);
        return limitSpeedStarted;
    }

    /* access modifiers changed from: private */
    public void updateOppoLimitSpeedRule(int uid, int limitspeed) {
        if (uid < 0 || UserHandle.getAppId(uid) > 100000) {
            logE("updateOppoLimitSpeedRule uid isn't valid!");
        } else if (limitspeed <= 0) {
            logE("updateOppoLimitSpeedRule limitspeed isn't valid!");
        } else {
            try {
                this.curUid = uid;
                this.mNetd.oppoLimitSpeedSetRule(uid, limitspeed);
            } catch (Exception e) {
                logE("updateOppoLimitSpeedRule fail : " + e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setOppoLimitSpeedWhiteApkList() {
        synchronized (this.mLock) {
            int[] arrayUids = new int[this.validWhiteListUid.size()];
            for (int i = 0; i < this.validWhiteListUid.size(); i++) {
                arrayUids[i] = this.validWhiteListUid.get(i).intValue();
            }
            if (arrayUids.length > 0) {
                try {
                    this.mNetd.oppoLimitSpeedSetWhiteList(arrayUids);
                } catch (Exception e) {
                    logE("updateOppoLimitWhiteList fail : " + e);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setOppoLimitSpeedStatisticFrontUid(int uid) {
        try {
            this.mNetd.oppoLimitSpeedetFrontUid(uid);
        } catch (Exception e) {
            logE("setOppoLimitSpeedStatisticFrontUid fail : " + e);
        }
    }

    /* access modifiers changed from: private */
    public void updateOppoLimitSpeedPackagesAndUids() {
        String whiteAppListText = getRomUpdateValue("NETWORK_LIMIT_SPEED_WHITE_APPS", "");
        String[] mLimitSpeedWhiteList = null;
        if (!whiteAppListText.equals("")) {
            mLimitSpeedWhiteList = whiteAppListText.split(",");
        }
        synchronized (this.mLock) {
            this.validWhiteListUid.clear();
            if (mLimitSpeedWhiteList != null && mLimitSpeedWhiteList.length > 0) {
                for (String whiteApp : mLimitSpeedWhiteList) {
                    try {
                        ApplicationInfo ai = this.pkManager.getApplicationInfoAsUser(whiteApp, 0, this.mUserId);
                        if (ai != null) {
                            this.mLimitSpeedPackagesAndUids.put(whiteApp, new Integer(ai.uid));
                            this.validWhiteListUid.add(Integer.valueOf(ai.uid));
                        } else {
                            this.mLimitSpeedPackagesAndUids.put(whiteApp, new Integer(-1));
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        this.mLimitSpeedPackagesAndUids.put(whiteApp, new Integer(-1));
                    }
                }
            }
            logD("updateOppoLimitSpeedPackagesAndUids: " + this.mLimitSpeedPackagesAndUids);
        }
    }

    public boolean isSupportOppoLimitSpeedFeature() {
        boolean lsfEnabled = true;
        PackageManager pm = this.mContext.getPackageManager();
        if (pm != null) {
            lsfEnabled = pm.hasSystemFeature(LIMIT_SPEED_SPEED_SUPPORT);
        }
        if (!lsfEnabled) {
            logE("OppoLimitSpeed System Feature not support");
        }
        return lsfEnabled;
    }

    /* access modifiers changed from: private */
    public boolean isEnableOppoLimitSpeedFeature() {
        boolean lsfEnabled = getRomUpdateBooleanValue("OPPO_SPEED_LIMIT_FEATURE", true).booleanValue();
        if (!lsfEnabled) {
            logE("OppoLimitSpeed RUS is off");
        }
        return lsfEnabled;
    }

    /* access modifiers changed from: private */
    public boolean isEnableOppoLimitSpeedStatistic() {
        return getRomUpdateBooleanValue("OPPO_LIMIT_SPEED_STATISTIC_ENABLE", false).booleanValue();
    }

    private void setLimitSpeedFwStatus(int flag) {
        try {
            this.mFwStatus = flag;
            Settings.Global.putInt(this.mContext.getContentResolver(), FW_LIMIT_SPEED_STATUS, flag);
        } catch (Exception e) {
            logE("[setLimitSpeedFWstatus] > " + e.getMessage());
        }
    }

    private boolean isConnectedWifi() {
        NetworkInfo info = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (info == null || info.getType() != 1) {
            return false;
        }
        return true;
    }

    private void startWifiRssiMonitor() {
        this.mHandler.removeMessages(3);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, 0, 0));
    }

    private void stopWifiRssiMonitor() {
        this.mHandler.removeMessages(3);
    }

    private void initOppoLimitSpeedWhiteList() {
        this.mHandler.removeMessages(2);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
    }

    private int readRTTFromFile(String mPath) {
        File path = new File(mPath);
        int rtt = 0;
        if (!path.exists()) {
            logE("[readRTTFromFile] > " + mPath + " not exist!");
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(path);
            BufferedReader in = new BufferedReader(new InputStreamReader(is2));
            new StringBuffer();
            String mRTT = in.readLine();
            if (mRTT == null || mRTT.length() <= 0) {
                rtt = 0;
            } else {
                rtt = Integer.parseInt(mRTT);
            }
            try {
                is2.close();
                writeToFile(mPath, "0".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (is != null) {
                is.close();
            }
            writeToFile(mPath, "0".getBytes());
        } catch (IOException e3) {
            e3.printStackTrace();
            if (is != null) {
                is.close();
            }
            writeToFile(mPath, "0".getBytes());
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                    throw th;
                }
            }
            writeToFile(mPath, "0".getBytes());
            throw th;
        }
        return rtt;
    }

    private void writeToFile(String mPath, byte[] buf) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(mPath);
            outStream.write(buf, 0, buf.length);
            outStream.flush();
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable th) {
                throw th;
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                } catch (Throwable th2) {
                    throw th2;
                }
            }
        } catch (Throwable th3) {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                } catch (Throwable th4) {
                    throw th4;
                }
            }
            throw th3;
        }
    }

    private void triggerLimitSpeedResultBack() {
        HashMap<String, String> map = new HashMap<>();
        int disablertt = readRTTFromFile(LIMIT_SPEED_DISABLE_RTT_FROM_SLA_PATH);
        int enablertt = readRTTFromFile(LIMIT_SPEED_ENABLE_RTT_FROM_SLA_PATH);
        int diffrtt = enablertt - disablertt;
        String callPackage = this.mContext.getPackageManager().getNameForUid(this.curUid);
        this.curUid = 0;
        if (disablertt <= 0 || enablertt <= 0) {
            logD("[RTTStatistics] <enable rtt> or <disable rtt> is ivalid!");
            return;
        }
        if (isConnectedWifi()) {
            map.put(WIFI_LIMIT_SPEED_NETWORK_TYPE, "WIFI");
        } else {
            map.put(WIFI_LIMIT_SPEED_NETWORK_TYPE, "MOBILE");
        }
        map.put(WIFI_LIMIT_SPEED_ENABLE_RTT, Integer.toString(disablertt));
        map.put(WIFI_LIMIT_SPEED_DISABLE_RTT, Integer.toString(enablertt));
        map.put(WIFI_LIMIT_SPEED_DIFF_RTT, Integer.toString(diffrtt));
        if (diffrtt < -10) {
            map.put(WIFI_LIMIT_SPEED_STATUS, "GOOD");
        } else if (diffrtt > 10) {
            map.put(WIFI_LIMIT_SPEED_STATUS, "POOR");
        } else {
            map.put(WIFI_LIMIT_SPEED_STATUS, "SAME");
        }
        map.put(WIFI_LIMIT_SPEED_APPLICATION, callPackage);
        logD("[RTTStatistics] <enable rtt> " + enablertt + " <disable rtt> " + disablertt + " <application> " + callPackage);
        OppoStatistics.onCommon(this.mContext, WIFI_STATISTIC_KEY, WIFI_LIMIT_SPEED_RTT, map, false);
    }

    private void agingOppoLimitSpeedTest() {
        if (this.agingTest) {
            this.mHandler.removeMessages(4);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(4));
        }
    }

    private String getRomUpdateValue(String key, String defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateForLimitSpeed;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    /* access modifiers changed from: private */
    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateForLimitSpeed;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    private Boolean getRomUpdateBooleanValue(String key, Boolean defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateForLimitSpeed;
        if (wifiRomUpdateHelper != null) {
            return Boolean.valueOf(wifiRomUpdateHelper.getBooleanValue(key, defaultVal.booleanValue()));
        }
        return defaultVal;
    }

    public void logD(String log) {
        if (DBG) {
            String str = TAG;
            Log.d(str, "" + log);
        }
    }

    public void logE(String log) {
        String str = TAG;
        Log.e(str, "" + log);
    }

    public INetd getNetd(Context context) {
        return INetd.Stub.asInterface((IBinder) context.getSystemService("netd"));
    }
}
