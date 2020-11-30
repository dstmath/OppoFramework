package com.android.server.wifi;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import java.util.HashMap;
import java.util.List;
import oppo.util.OppoStatistics;

public class OppoSapScanCoexistManager {
    public static final String ACTION_SCAN_SAP_COEXIST_CHANGED = "oppo.intent.action.ACTION_SCAN_SAP_COEXIST_CHANGED";
    public static final int COEXIST_OFF = 0;
    public static final int COEXIST_ON = 1;
    public static final int COEXIST_SAP_SCAN_STATUS = 2;
    public static final int COEXIST_SCANONLY_STATUS = 1;
    public static final int COEXIST_SOFTAP_STATUS = 0;
    private static boolean DBG = true;
    private static final String DEFAULT_LOCATION_SCAN_APP = "com.baidu.map.location,com.amap.android.location,com.amap.android.ams,com.autonavi.minimap,com.baidu.BaiduMap";
    private static final String EVENT_ID_SAP_SCAN_COEXIST = "wifi_hotspot_scan_coexist";
    public static final String EXTRA_SCAN_SAP_COEXIST_FAILED_REASON = "scan_sap_failed_reason";
    public static final String EXTRA_SCAN_SAP_COEXIST_STATE = "scan_sap_state";
    private static final int MAX_THREAD_ID = 10000;
    private static final int SAP_SCAN_COEXIST_DETECT_INTERVAL = 1000;
    private static final int SAP_SCAN_COEXIST_HIGH_TRAFFIC = 3;
    private static final int SAP_SCAN_COEXIST_INVALID = 0;
    private static final int SAP_SCAN_COEXIST_LOW_TRAFFIC = 2;
    private static final int SAP_SCAN_COEXIST_NO_ASSOCIATED_STA = 1;
    private static final int SAP_SCAN_COEXIST_TRAFFIC_DETECT_INTERVAL = 5;
    private static final int SAP_SCAN_COEXIST_TRAFFIC_RX_LOWEST = 625000;
    private static final int SAP_SCAN_COEXIST_TRAFFIC_TX_LOWEST = 625000;
    public static final int SCAN_SAP_COEXIST_NUKOWN_FAILED = 3;
    public static final int SCAN_SAP_COEXIST_SAP_FAILED = 2;
    public static final int SCAN_SAP_COEXIST_SCAN_FAILED = 1;
    public static final int SCAN_SAP_COEXIST_STATE_DISENABLED = 1;
    public static final int SCAN_SAP_COEXIST_STATE_ENABLED = 3;
    public static final int SCAN_SAP_COEXIST_STATE_ENABLING = 2;
    public static final int SCAN_SAP_COEXIST_STATE_FAILED = 4;
    private static final String TAG = "OppoSapScanCoexistManager";
    private static final String WIFI_STATISTIC = "wifi_fool_proof";
    private static OppoSapScanCoexistManager sInstance = null;
    private SapScanCoexistDetectThread mCoexistDetectThread = null;
    public int mCoexistDetectThreadId = 0;
    public int mCoexistSwitchStatus = 0;
    private Context mContext;
    public SceneModeInfo mCurSceneModeInfo = null;
    public String mLocationScanPkg;
    private Object mLock = new Object();
    public SceneModeInfo mPreSceneModeInfo = null;
    public int mSapAssociatedStations = 0;
    public int mSapStatus = 11;
    private boolean mScanAndApCoexistSupported = false;
    public int mScanOnlyStatus = 1;
    private WifiNative mWifiNative = null;
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;

    public OppoSapScanCoexistManager(Context context) {
        this.mContext = context;
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        this.mScanAndApCoexistSupported = this.mWifiRomUpdateHelper.getBooleanValue("OPPO_BASIC_SCAN_AND_SOFTAP_CONCURRENCY_ENABLED", false);
        this.mLocationScanPkg = this.mWifiRomUpdateHelper.getValue("OPPO_BASIC_SCAN_AND_SOFTAP_CONCURRENCY_LOCATION_PKG", DEFAULT_LOCATION_SCAN_APP);
        this.mPreSceneModeInfo = new SceneModeInfo();
        this.mCurSceneModeInfo = new SceneModeInfo();
    }

    public static synchronized OppoSapScanCoexistManager getInstance(Context context) {
        OppoSapScanCoexistManager oppoSapScanCoexistManager;
        synchronized (OppoSapScanCoexistManager.class) {
            if (sInstance == null) {
                synchronized (OppoSapScanCoexistManager.class) {
                    if (sInstance == null) {
                        sInstance = new OppoSapScanCoexistManager(context);
                    }
                }
            }
            oppoSapScanCoexistManager = sInstance;
        }
        return oppoSapScanCoexistManager;
    }

    public class SceneModeInfo {
        public long mRxBytes = 0;
        public int mSceneMode = 0;
        public long mTxBytes = 0;

        public SceneModeInfo() {
        }

        public void setSceneMode(int mode) {
            this.mSceneMode = mode;
        }

        public void setSceneModeTraffic(SceneModeInfo traffic) {
            this.mTxBytes = traffic.mTxBytes;
            this.mRxBytes = traffic.mRxBytes;
        }
    }

    /* access modifiers changed from: package-private */
    public class SapScanCoexistDetectThread extends Thread {
        private boolean isFirstTrafficDetect = true;
        private boolean isThreadDestroy = false;
        private boolean isThreadPause = true;
        public int mBlockedScanCntWhenSapScanCoexist = 0;
        public int mLocationScanCntWhenSapScanCoexist = 0;
        public int mScanCntWhenSapScanCoexist = 0;
        private int threadid = 0;
        private int trafficDetectCnt = 0;

        public SapScanCoexistDetectThread(int id) {
            this.threadid = id;
        }

        public void setResume() {
            this.isThreadPause = false;
            this.isFirstTrafficDetect = true;
            this.trafficDetectCnt = 0;
        }

        public void setPause() {
            this.isThreadPause = true;
        }

        public void setDestroy() {
            this.isThreadDestroy = true;
        }

        public void setSanCnt() {
            this.mScanCntWhenSapScanCoexist++;
        }

        public void setBlockedScanCnt() {
            this.mBlockedScanCntWhenSapScanCoexist++;
        }

        public void setLocationScanCnt() {
            this.mLocationScanCntWhenSapScanCoexist++;
        }

        public void run() {
            super.run();
            String sapIfaceName = OppoSapScanCoexistManager.this.mWifiNative.getSoftApInterfaceName();
            if (TextUtils.isEmpty(sapIfaceName)) {
                OppoSapScanCoexistManager.this.logD("could not get softap ifacename for checking network access!");
                return;
            }
            OppoSapScanCoexistManager.this.logD("sap scan coexist detect Thread start, thread id = " + this.threadid + " soft ap iface = " + sapIfaceName);
            while (!this.isThreadDestroy) {
                try {
                    if (!this.isThreadPause) {
                        if (this.trafficDetectCnt == 0) {
                            SceneModeInfo traffic = new SceneModeInfo();
                            traffic.mTxBytes = TrafficStats.getTxBytes(sapIfaceName);
                            traffic.mRxBytes = TrafficStats.getRxBytes(sapIfaceName);
                            OppoSapScanCoexistManager.this.mCurSceneModeInfo.setSceneModeTraffic(traffic);
                            if (this.isFirstTrafficDetect) {
                                traffic.mTxBytes = 0;
                                traffic.mRxBytes = 0;
                                this.isFirstTrafficDetect = false;
                            } else {
                                traffic.mTxBytes = OppoSapScanCoexistManager.this.mCurSceneModeInfo.mTxBytes - OppoSapScanCoexistManager.this.mPreSceneModeInfo.mTxBytes;
                                traffic.mRxBytes = OppoSapScanCoexistManager.this.mCurSceneModeInfo.mRxBytes - OppoSapScanCoexistManager.this.mPreSceneModeInfo.mRxBytes;
                            }
                            OppoSapScanCoexistManager.this.mPreSceneModeInfo.setSceneModeTraffic(OppoSapScanCoexistManager.this.mCurSceneModeInfo);
                            OppoSapScanCoexistManager.this.mPreSceneModeInfo.setSceneMode(OppoSapScanCoexistManager.this.mCurSceneModeInfo.mSceneMode);
                            if (traffic.mTxBytes <= 625000) {
                                if (traffic.mRxBytes <= 625000) {
                                    OppoSapScanCoexistManager.this.mCurSceneModeInfo.setSceneMode(2);
                                    OppoSapScanCoexistManager.this.logD(" txBytes = " + traffic.mTxBytes + " rxBytes = " + traffic.mRxBytes);
                                }
                            }
                            OppoSapScanCoexistManager.this.mCurSceneModeInfo.setSceneMode(3);
                            OppoSapScanCoexistManager.this.logD(" txBytes = " + traffic.mTxBytes + " rxBytes = " + traffic.mRxBytes);
                        }
                        this.trafficDetectCnt++;
                        if (this.trafficDetectCnt == 5) {
                            this.trafficDetectCnt = 0;
                        }
                        OppoSapScanCoexistManager.this.logD("SceneMode = " + OppoSapScanCoexistManager.this.mCurSceneModeInfo.mSceneMode + "PreSceneMode = " + OppoSapScanCoexistManager.this.mPreSceneModeInfo.mSceneMode);
                    } else {
                        OppoSapScanCoexistManager.this.logD("thread id = " + this.threadid + " is pause, SceneMode = " + OppoSapScanCoexistManager.this.mCurSceneModeInfo.mSceneMode);
                    }
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            OppoSapScanCoexistManager.this.logD("sap scan coexist detect thread break, thread id = " + this.threadid);
        }
    }

    public boolean getSapScanCoexistSupported() {
        this.mScanAndApCoexistSupported = this.mWifiRomUpdateHelper.getBooleanValue("OPPO_BASIC_SCAN_AND_SOFTAP_CONCURRENCY_ENABLED", false);
        return this.mScanAndApCoexistSupported;
    }

    public void updateScanOnlyStatus(int status) {
        logD("updateScanOnlyModeStatus: mScanOnlyStatus=" + status);
        this.mScanOnlyStatus = status;
        updateCoexistDetectThread();
    }

    public void updateSapStatus(int status) {
        logD("updateSapStatus: mSapStatus=" + status);
        this.mSapStatus = status;
        updateCoexistDetectThread();
    }

    public void updateCoexistSwitchStatus(int status) {
        logD("updateSapScanCoexistStatus: mCoexistSwitchStatus=" + status);
        this.mCoexistSwitchStatus = status;
        updateCoexistDetectThread();
    }

    public void updateCoexistState(int newState, int reason) {
        Intent intent = new Intent(ACTION_SCAN_SAP_COEXIST_CHANGED);
        intent.addFlags(67108864);
        intent.putExtra(EXTRA_SCAN_SAP_COEXIST_STATE, newState);
        if (newState == 4) {
            intent.putExtra(EXTRA_SCAN_SAP_COEXIST_FAILED_REASON, reason);
        }
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void updateSapAssociatedStations(int numStations) {
        logD("updateSapAssociatedStations : " + numStations);
        this.mSapAssociatedStations = numStations;
        synchronized (this.mLock) {
            if (this.mCoexistDetectThread != null) {
                if (this.mSapAssociatedStations != 0) {
                    this.mCurSceneModeInfo.setSceneMode(2);
                    this.mCoexistDetectThread.setResume();
                } else {
                    this.mCurSceneModeInfo.setSceneMode(1);
                    this.mCoexistDetectThread.setPause();
                }
            }
        }
    }

    public void updateCoexistDetectThread() {
        logD("updateCoexistDetectThread: mCoexistSwitchStatus=" + this.mCoexistSwitchStatus + " mScanOnlyStatus=" + this.mScanOnlyStatus + " mSapStatus=" + this.mSapStatus + " mSapAssociatedStations=" + this.mSapAssociatedStations);
        if (this.mCoexistSwitchStatus == 1 && this.mScanOnlyStatus == 3 && this.mSapStatus == 13) {
            this.mCoexistDetectThreadId++;
            if (this.mCoexistDetectThreadId > 10000) {
                this.mCoexistDetectThreadId = 0;
            }
            synchronized (this.mLock) {
                this.mCoexistDetectThread = new SapScanCoexistDetectThread(this.mCoexistDetectThreadId);
                this.mCurSceneModeInfo.setSceneMode(1);
                if (this.mSapAssociatedStations != 0) {
                    this.mCurSceneModeInfo.setSceneMode(2);
                    this.mCoexistDetectThread.setResume();
                }
                this.mCoexistDetectThread.start();
            }
            updateCoexistState(3, 0);
            return;
        }
        boolean coexistfailflag = false;
        int scancnt = 0;
        int blockedscan = 0;
        int locationscan = 0;
        if (this.mCoexistSwitchStatus == 1 && (this.mScanOnlyStatus == 14 || this.mSapStatus == 14)) {
            updateCoexistState(4, 0);
        } else if (this.mCoexistSwitchStatus == 1 && (this.mScanOnlyStatus == 12 || this.mSapStatus == 12)) {
            updateCoexistState(2, 0);
        }
        synchronized (this.mLock) {
            if (this.mCoexistDetectThread != null) {
                this.mCoexistDetectThread.setDestroy();
                this.mCurSceneModeInfo.setSceneMode(0);
                scancnt = this.mCoexistDetectThread.mScanCntWhenSapScanCoexist;
                blockedscan = this.mCoexistDetectThread.mBlockedScanCntWhenSapScanCoexist;
                locationscan = this.mCoexistDetectThread.mLocationScanCntWhenSapScanCoexist;
                this.mCoexistDetectThread = null;
                coexistfailflag = true;
            }
        }
        if (coexistfailflag) {
            updateCoexistState(1, 0);
            setStatistics(scancnt, blockedscan, locationscan);
        }
    }

    public boolean isNeedBlockScanWhenSapScene() {
        boolean isNeedBlock = false;
        synchronized (this.mLock) {
            if (this.mCoexistDetectThread != null) {
                if (this.mCurSceneModeInfo.mSceneMode == 3) {
                    isNeedBlock = true;
                }
                logD("isNeedBlockScanWhenSapScene block scan: " + isNeedBlock);
                this.mCoexistDetectThread.setSanCnt();
                if (isNeedBlock) {
                    this.mCoexistDetectThread.setBlockedScanCnt();
                }
                if (isLoactionScan()) {
                    this.mCoexistDetectThread.setLocationScanCnt();
                }
            }
        }
        return isNeedBlock;
    }

    public boolean isLoactionScan() {
        String pkgName = " ";
        List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        if (!tasks.isEmpty()) {
            pkgName = tasks.get(0).topActivity.getPackageName();
        }
        String str = this.mLocationScanPkg;
        if (str == null) {
            logD("mLocationScanPkg is null.");
            return false;
        }
        for (String name : str.split(",")) {
            if (pkgName.contains(name.trim())) {
                return true;
            }
        }
        return false;
    }

    private void setStatistics(int scancnt, int blockedscan, int locationscan) {
        HashMap<String, String> map = new HashMap<>();
        map.put("ScanCnt", Integer.toString(scancnt));
        map.put("BlockedScanCnt", Integer.toString(blockedscan));
        map.put("LocationScanCnt", Integer.toString(locationscan));
        OppoStatistics.onCommon(this.mContext, WIFI_STATISTIC, EVENT_ID_SAP_SCAN_COEXIST, map, false);
        logD("isNeedBlockScanWhenSapScene lay point:" + map.toString());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logD(String s) {
        if (DBG) {
            Log.d(TAG, s);
        }
    }
}
