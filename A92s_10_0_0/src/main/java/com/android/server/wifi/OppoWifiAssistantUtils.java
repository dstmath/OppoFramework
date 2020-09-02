package com.android.server.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/* access modifiers changed from: package-private */
public class OppoWifiAssistantUtils {
    private static final String ANT_STR = "ant=";
    private static final boolean ASSISTANT_FOUR_VERSION_ENABLE = true;
    private static final String BL_CAP2_STR = "bl_cap2=";
    private static final String BRS_STR = "brs=";
    private static final String BSSID_STR = "bssid=";
    private static final String CAP2_STR = "cap2=";
    private static final String CAP_STR = "cap=";
    public static final int CELLULAR_INDEX = 2;
    private static final String CE_STR = "ce=";
    private static final String CFC_STR = "cfc=";
    private static final String CONKEY_STR = "conkey=";
    private static final String CSC_STR = "csc=";
    private static final String EXTRA_ENALE_DATA = "enableData";
    private static final String EXTRA_SCORE = "score";
    private static final String HF_STR = "hf=";
    private static final String HLS_STR = "hls=";
    public static final String IFACE_NAME_WLAN0 = "wlan0";
    public static final String IFACE_NAME_WLAN1 = "wlan1";
    private static final String INT_STR = "int=";
    private static final String LST_STR = "lst=";
    public static final long MILLIS_OF_A_DAY = 86400000;
    public static final int MSG_DISABLE_DUAL_STA = 1;
    private static final String MS_STR = "ms=";
    private static final String NFC_STR = "nfc=";
    private static final String NID_STR = "id=";
    private static final String NQL_STR = "nql=";
    private static final String NV2_STR = "nv2=";
    private static final String NV_STR = "nv=";
    private static final String SCORE_STR = "sc=";
    public static final String SLA_CANCEL_COUNT = "SLA_CANCEL_COUNT";
    public static final String SLA_DIALOG_COUNT = "SLA_DIALOG_COUNT";
    public static final int SLA_DIALOG_COUNT_MAX = 3;
    public static final String SLA_LAST_DIALOG_TIMESTAMP = "SLA_LAST_DIALOG_TIMESTAMP";
    private static final int STATIC_BAD_RSSI_24 = -83;
    private static final int STATIC_BAD_RSSI_5 = -80;
    private static final int STATIC_GOOD_RSSI_24 = -65;
    private static final int STATIC_GOOD_RSSI_5 = -65;
    private static final int STATIC_LOW_RSSI_24 = -75;
    private static final int STATIC_LOW_RSSI_5 = -72;
    private static final String TAG = "OppoWifiAssistantUtils";
    private static final String WIFI_ASSISTANT_FILE = "/data/misc/wifi/wifi_assistant";
    private static final String WIFI_SCROE_CHANGE = "android.net.wifi.WIFI_SCORE_CHANGE";
    public static final int WLAN0_INDEX = 0;
    public static final int WLAN1_INDEX = 1;
    private static boolean sDebug = true;
    private static OppoWifiAssistantUtils sInstance;
    private final String WLAN0_ARP_ANNOUNCE = "/proc/sys/net/ipv4/conf/wlan0/arp_announce";
    private final String WLAN0_ARP_IGNORE = "/proc/sys/net/ipv4/conf/wlan0/arp_ignore";
    private final String WLAN1_ARP_ANNOUNCE = "/proc/sys/net/ipv4/conf/wlan1/arp_announce";
    private final String WLAN1_ARP_IGNORE = "/proc/sys/net/ipv4/conf/wlan1/arp_ignore";
    private AlertDialog mAlertDialog = null;
    private AsyncHandler mAsyncHandler;
    private boolean mBothWifiConnected = false;
    private HashMap<String, Boolean> mCanChangeToCell = new HashMap<>();
    private HashMap<String, Boolean> mCanShowDialog = new HashMap<>();
    private boolean mCanTriggerData = true;
    private Context mContext;
    private boolean mEnableRoamingInBand = false;
    private Handler mHander;
    private int mNetId = -1;
    private int mNetId2 = -1;
    private boolean mNetworkRequested = false;
    private String mPrimaryWifi = IFACE_NAME_WLAN0;
    private ScanRequestProxy mScanRequestProxy;
    private AlertDialog mSlaDialog = null;
    private List<OppoWifiAssistantRecord> mSortNetworkRecord = new ArrayList();
    private List<WifiConfiguration> mSortWifiConfig = new ArrayList();
    private WifiConfigManager mWifiConfigManager;
    private HashMap<String, WifiInfo> mWifiInfos = new HashMap<>();
    private WifiNative mWifiNative;
    private HashMap<String, OppoWifiAssistantRecord> mWifiNetworkRecord = new HashMap<>();
    private WifiRomUpdateHelper mWifiRomUpdateHelper = null;
    private HashMap<String, Integer> mWifiScores = new HashMap<>();
    private String mWlan0GatewayIp = "";
    private String mWlan0GatewayMac = "";
    private String mWlan1GatewayIp = "";
    private String mWlan1GatewayMac = "";

    private OppoWifiAssistantUtils(Context context) {
        this.mContext = context;
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(context);
        this.mWifiConfigManager = WifiInjector.getInstance().getWifiConfigManager();
        this.mScanRequestProxy = WifiInjector.getInstance().getScanRequestProxy();
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
        HandlerThread asyncThread = new HandlerThread("WiFi.LocalAsyncThread");
        asyncThread.start();
        this.mAsyncHandler = new AsyncHandler(asyncThread.getLooper());
    }

    public static OppoWifiAssistantUtils getInstance(Context context) {
        synchronized (OppoWifiAssistantUtils.class) {
            if (sInstance == null) {
                sInstance = new OppoWifiAssistantUtils(context);
            }
        }
        return sInstance;
    }

    private class AsyncHandler extends Handler {
        public AsyncHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                OppoWifiAssistantUtils oppoWifiAssistantUtils = OppoWifiAssistantUtils.this;
                oppoWifiAssistantUtils.logD("Unknow message:" + msg.what);
                return;
            }
            OppoWifiAssistantUtils.this.logD("MSG_DISABLE_DUAL_STA");
            OppoWifiAssistantUtils.this.disableDualSta();
        }
    }

    public class InternetResult {
        long avgRtt = 0;
        boolean captivePortal = false;
        boolean detecting = false;
        long rtt1 = 0;
        long rtt2 = 0;
        long rtt3 = 0;
        long sequence = 0;
        int serverCount = 0;
        long timestamp = 0;
        boolean valid = false;

        public InternetResult() {
        }

        public void set(InternetResult result) {
            this.detecting = result.detecting;
            this.valid = result.valid;
            this.captivePortal = result.captivePortal;
            this.serverCount = result.serverCount;
            this.rtt1 = result.rtt1;
            this.rtt2 = result.rtt2;
            this.rtt3 = result.rtt3;
            this.avgRtt = result.avgRtt;
            this.timestamp = result.timestamp;
            this.sequence = result.sequence;
        }
    }

    private boolean isRusEnableWifiAssistantFourVersion() {
        return getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FOUR_VERSION_ENABLE", true).booleanValue();
    }

    /* access modifiers changed from: package-private */
    public void readWifiNetworkRecord() {
        this.mAsyncHandler.post(new Runnable() {
            /* class com.android.server.wifi.$$Lambda$OppoWifiAssistantUtils$dArZA546s3jyC_JvCAKaj2aosBo */

            public final void run() {
                OppoWifiAssistantUtils.this.lambda$readWifiNetworkRecord$0$OppoWifiAssistantUtils();
            }
        });
    }

    /* JADX WARNING: Removed duplicated region for block: B:315:0x05b8 A[SYNTHETIC, Splitter:B:315:0x05b8] */
    public /* synthetic */ void lambda$readWifiNetworkRecord$0$OppoWifiAssistantUtils() {
        Throwable th;
        BufferedReader reader;
        Throwable th2;
        int dataLength;
        String[] data;
        int connFailCount;
        int bestRssi;
        int connSuccCout;
        int score;
        int maxSpeed;
        int netFailCount;
        int connSuccCout2;
        long lastuseTime;
        int bestRssi2;
        int netid;
        int netid2;
        int netid3;
        long lastuseTime2;
        int netFailCount2;
        int maxSpeed2;
        int score2;
        if (this.mWifiNetworkRecord == null) {
            logD("mWifiNetworkRecord exception, re-init it");
            this.mWifiNetworkRecord = new HashMap<>();
        }
        synchronized (this.mWifiNetworkRecord) {
            try {
                this.mWifiNetworkRecord.clear();
                try {
                    BufferedReader reader2 = new BufferedReader(new FileReader(WIFI_ASSISTANT_FILE));
                    int dataLength2 = 16;
                    logD("readWifiNetworkRecord start...");
                    while (true) {
                        String line = reader2.readLine();
                        if (line == null) {
                            break;
                        }
                        OppoWifiAssistantRecord mwr = new OppoWifiAssistantRecord();
                        String[] data2 = line.split("\t");
                        int bestRssi3 = WifiConfiguration.INVALID_RSSI;
                        int connSuccCout3 = 0;
                        int connFailCount2 = 0;
                        long accessNetTime = 0;
                        long internetTime = 0;
                        long lastuseTime3 = 0;
                        int netFailCount3 = 0;
                        int maxSpeed3 = 0;
                        int score3 = 0;
                        int netid4 = -1;
                        if (data2.length == dataLength2) {
                            try {
                                netid2 = Integer.parseInt(data2[0]);
                                dataLength = dataLength2;
                            } catch (NumberFormatException e) {
                                StringBuilder sb = new StringBuilder();
                                dataLength = dataLength2;
                                sb.append("NumberFormatException e:");
                                sb.append(e);
                                logD(sb.toString());
                                netid2 = -1;
                            }
                            mwr.mNetid = netid2;
                            mwr.mConfigkey = data2[1];
                            mwr.mBssid = data2[2];
                            try {
                                bestRssi3 = Integer.parseInt(data2[3]);
                            } catch (NumberFormatException e2) {
                            }
                            mwr.mBestRssi = bestRssi3;
                            try {
                                connSuccCout3 = Integer.parseInt(data2[4]);
                            } catch (NumberFormatException e3) {
                            }
                            mwr.mConnSuccCout = connSuccCout3;
                            try {
                                connFailCount2 = Integer.parseInt(data2[5]);
                            } catch (NumberFormatException e4) {
                            }
                            mwr.mConnFailCount = connFailCount2;
                            try {
                                accessNetTime = (long) Integer.parseInt(data2[6]);
                            } catch (NumberFormatException e5) {
                            }
                            mwr.mAccessNetTime = accessNetTime;
                            try {
                                internetTime = (long) Integer.parseInt(data2[7]);
                            } catch (NumberFormatException e6) {
                            }
                            mwr.mInternetTime = internetTime;
                            try {
                                netid3 = netid2;
                                lastuseTime2 = (long) Integer.parseInt(data2[8]);
                            } catch (NumberFormatException e7) {
                                netid3 = netid2;
                                lastuseTime2 = 0;
                            }
                            mwr.mLastuseTime = lastuseTime2;
                            try {
                                netFailCount2 = Integer.parseInt(data2[9]);
                            } catch (NumberFormatException e8) {
                                netFailCount2 = 0;
                            }
                            mwr.mNetFailCount = netFailCount2;
                            try {
                                maxSpeed2 = Integer.parseInt(data2[10]);
                            } catch (NumberFormatException e9) {
                                maxSpeed2 = 0;
                            }
                            mwr.mMaxSpeed = maxSpeed2;
                            mwr.mIs5G = data2[11].equals("true");
                            mwr.mNetworkValid = data2[12].equals("true");
                            mwr.mConnExp = data2[13].equals("true");
                            if (mwr.mConnExp) {
                                mwr.mConnExp = false;
                            }
                            try {
                                score2 = Integer.parseInt(data2[14]);
                            } catch (NumberFormatException e10) {
                                score2 = 0;
                            }
                            mwr.mScore = score2;
                            String[] netQulity = data2[15].split(",");
                            if (netQulity.length == mwr.mNetQualitys.length) {
                                int i = 0;
                                while (i < mwr.mNetQualitys.length) {
                                    int quality = -1;
                                    try {
                                        quality = Integer.parseInt(netQulity[i]);
                                    } catch (NumberFormatException e11) {
                                    }
                                    mwr.mNetQualitys[i] = quality;
                                    if (i >= 1) {
                                        if (mwr.mNetQualitys[i] < mwr.mNetQualitys[i - 1]) {
                                            mwr.mNetQualitys[i] = mwr.mNetQualitys[i - 1];
                                        }
                                    }
                                    i++;
                                    netQulity = netQulity;
                                }
                            }
                            WifiConfiguration config = this.mWifiConfigManager.getConfiguredNetwork(mwr.mConfigkey);
                            if (config != null) {
                                mwr.mWifiConfiguration = config;
                            }
                        } else {
                            dataLength = dataLength2;
                            int length = data2.length;
                            int i2 = 0;
                            while (i2 < length) {
                                String subData = data2[i2];
                                if (subData.startsWith(NID_STR)) {
                                    try {
                                        netid = Integer.parseInt(subData.substring(NID_STR.length()));
                                    } catch (NumberFormatException e12) {
                                        netid = netid4;
                                    }
                                    mwr.mNetid = netid;
                                    netid4 = netid;
                                    data = data2;
                                } else {
                                    if (subData.startsWith(CONKEY_STR)) {
                                        mwr.mConfigkey = subData.substring(CONKEY_STR.length());
                                        data = data2;
                                        bestRssi = bestRssi3;
                                        connSuccCout = connSuccCout3;
                                        connFailCount = connFailCount2;
                                    } else if (subData.startsWith(BSSID_STR)) {
                                        mwr.mBssid = subData.substring(BSSID_STR.length());
                                        data = data2;
                                        bestRssi = bestRssi3;
                                        connSuccCout = connSuccCout3;
                                        connFailCount = connFailCount2;
                                    } else if (subData.startsWith(BRS_STR)) {
                                        try {
                                            bestRssi3 = Integer.parseInt(subData.substring(BRS_STR.length()));
                                        } catch (NumberFormatException e13) {
                                        }
                                        mwr.mBestRssi = bestRssi3;
                                        data = data2;
                                    } else if (subData.startsWith(CSC_STR)) {
                                        try {
                                            connSuccCout3 = Integer.parseInt(subData.substring(CSC_STR.length()));
                                        } catch (NumberFormatException e14) {
                                        }
                                        mwr.mConnSuccCout = connSuccCout3;
                                        data = data2;
                                    } else if (subData.startsWith(CFC_STR)) {
                                        try {
                                            connFailCount2 = Integer.parseInt(subData.substring(CFC_STR.length()));
                                        } catch (NumberFormatException e15) {
                                        }
                                        mwr.mConnFailCount = connFailCount2;
                                        data = data2;
                                    } else if (subData.startsWith(ANT_STR)) {
                                        try {
                                            accessNetTime = Long.parseLong(subData.substring(ANT_STR.length()));
                                        } catch (NumberFormatException e16) {
                                        }
                                        mwr.mAccessNetTime = accessNetTime;
                                        data = data2;
                                    } else if (subData.startsWith(INT_STR)) {
                                        try {
                                            internetTime = Long.parseLong(subData.substring(INT_STR.length()));
                                        } catch (NumberFormatException e17) {
                                        }
                                        mwr.mInternetTime = internetTime;
                                        data = data2;
                                    } else if (subData.startsWith(LST_STR)) {
                                        try {
                                            bestRssi2 = bestRssi3;
                                            connSuccCout2 = connSuccCout3;
                                            lastuseTime = Long.parseLong(subData.substring(LST_STR.length()));
                                        } catch (NumberFormatException e18) {
                                            bestRssi2 = bestRssi3;
                                            connSuccCout2 = connSuccCout3;
                                            lastuseTime = lastuseTime3;
                                        }
                                        mwr.mLastuseTime = lastuseTime;
                                        data = data2;
                                        lastuseTime3 = lastuseTime;
                                        connSuccCout3 = connSuccCout2;
                                        bestRssi3 = bestRssi2;
                                    } else {
                                        connSuccCout = connSuccCout3;
                                        if (subData.startsWith(NFC_STR)) {
                                            try {
                                                netFailCount = Integer.parseInt(subData.substring(NFC_STR.length()));
                                            } catch (NumberFormatException e19) {
                                                netFailCount = netFailCount3;
                                            }
                                            mwr.mNetFailCount = netFailCount;
                                            data = data2;
                                            netFailCount3 = netFailCount;
                                            connSuccCout3 = connSuccCout;
                                            bestRssi3 = bestRssi3;
                                        } else if (subData.startsWith(MS_STR)) {
                                            try {
                                                maxSpeed = Integer.parseInt(subData.substring(MS_STR.length()));
                                            } catch (NumberFormatException e20) {
                                                maxSpeed = maxSpeed3;
                                            }
                                            mwr.mMaxSpeed = maxSpeed;
                                            data = data2;
                                            maxSpeed3 = maxSpeed;
                                            connSuccCout3 = connSuccCout;
                                            bestRssi3 = bestRssi3;
                                        } else if (subData.startsWith(HF_STR)) {
                                            mwr.mIs5G = subData.substring(HF_STR.length()).equals("true");
                                            bestRssi = bestRssi3;
                                            data = data2;
                                            connFailCount = connFailCount2;
                                        } else if (subData.startsWith(NV_STR)) {
                                            mwr.mNetworkValid = subData.substring(NV_STR.length()).equals("true");
                                            bestRssi = bestRssi3;
                                            data = data2;
                                            connFailCount = connFailCount2;
                                        } else if (subData.startsWith(NV2_STR)) {
                                            mwr.mNetworkValid2 = subData.substring(NV2_STR.length()).equals("true");
                                            bestRssi = bestRssi3;
                                            data = data2;
                                            connFailCount = connFailCount2;
                                        } else if (subData.startsWith(CE_STR)) {
                                            mwr.mConnExp = subData.substring(CE_STR.length()).equals("true");
                                            bestRssi = bestRssi3;
                                            data = data2;
                                            connFailCount = connFailCount2;
                                        } else if (subData.startsWith(SCORE_STR)) {
                                            try {
                                                score = Integer.parseInt(subData.substring(SCORE_STR.length()));
                                            } catch (NumberFormatException e21) {
                                                score = score3;
                                            }
                                            mwr.mScore = score;
                                            data = data2;
                                            score3 = score;
                                            connSuccCout3 = connSuccCout;
                                            bestRssi3 = bestRssi3;
                                        } else if (subData.startsWith(NQL_STR)) {
                                            String newNetQulity = subData.substring(NQL_STR.length());
                                            String[] netQulitys = newNetQulity.split(",");
                                            bestRssi = bestRssi3;
                                            data = data2;
                                            if (netQulitys.length == mwr.mNetQualitys.length) {
                                                int i3 = 0;
                                                while (i3 < mwr.mNetQualitys.length) {
                                                    int newQuality = -1;
                                                    try {
                                                        newQuality = Integer.parseInt(netQulitys[i3]);
                                                    } catch (NumberFormatException e22) {
                                                    }
                                                    mwr.mNetQualitys[i3] = newQuality;
                                                    if (i3 >= 1) {
                                                        if (mwr.mNetQualitys[i3] < mwr.mNetQualitys[i3 - 1]) {
                                                            mwr.mNetQualitys[i3] = mwr.mNetQualitys[i3 - 1];
                                                        }
                                                    }
                                                    i3++;
                                                    newNetQulity = newNetQulity;
                                                }
                                            }
                                            connFailCount = connFailCount2;
                                        } else {
                                            bestRssi = bestRssi3;
                                            data = data2;
                                            if (subData.startsWith(CAP_STR)) {
                                                mwr.mIsCaptive = subData.substring(CAP_STR.length()).equals("true");
                                                connFailCount = connFailCount2;
                                            } else if (subData.startsWith(HLS_STR)) {
                                                String historyDataString = subData.substring(HLS_STR.length());
                                                if (!isRusEnableWifiAssistantFourVersion() || mwr.mHistoryRecordQueue != null) {
                                                    connFailCount = connFailCount2;
                                                    logD("mHistoryRecordQueue is already existed!");
                                                } else {
                                                    connFailCount = connFailCount2;
                                                    mwr.mHistoryRecordQueue = new OppoHistoryRecordQueue(this.mContext, mwr.mBssid, mwr.mIs5G);
                                                    mwr.mHistoryRecordQueue.readStorageData(historyDataString);
                                                }
                                            } else {
                                                connFailCount = connFailCount2;
                                                if (subData.startsWith(CAP2_STR)) {
                                                    mwr.mIsCaptive2 = subData.substring(CAP2_STR.length()).equals("true");
                                                } else if (subData.startsWith(BL_CAP2_STR)) {
                                                    mwr.mIsBlacklistCap2 = subData.substring(BL_CAP2_STR.length()).equals("true");
                                                }
                                            }
                                        }
                                    }
                                    connSuccCout3 = connSuccCout;
                                    bestRssi3 = bestRssi;
                                    connFailCount2 = connFailCount;
                                }
                                i2++;
                                length = length;
                                data2 = data;
                            }
                            WifiConfiguration config2 = this.mWifiConfigManager.getConfiguredNetwork(mwr.mConfigkey);
                            if (config2 != null) {
                                mwr.mWifiConfiguration = config2;
                            }
                        }
                        if (mwr.mConfigkey != null) {
                            logD("mWifiNetworkRecord.put --> " + mwr.mConfigkey);
                            this.mWifiNetworkRecord.put(mwr.mConfigkey, mwr);
                        }
                        dataLength2 = dataLength;
                    }
                    logD("readWifiNetworkRecord end");
                    try {
                        reader2.close();
                    } catch (IOException e23) {
                    }
                } catch (FileNotFoundException e24) {
                    reader = null;
                    logD("readWifiNetworkRecord: FileNotFoundException: " + e24);
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e25) {
                        }
                    }
                } catch (IOException e26) {
                    reader = null;
                    logD("readWifiNetworkRecord: IOException: " + e26);
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e27) {
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    if (reader != null) {
                    }
                    throw th2;
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
    }

    public void saveWifiNetworkRecord() {
        this.mAsyncHandler.post(new Runnable() {
            /* class com.android.server.wifi.$$Lambda$OppoWifiAssistantUtils$v2g02XQLdvcyLgj_615BcPGw8A */

            public final void run() {
                OppoWifiAssistantUtils.this.lambda$saveWifiNetworkRecord$1$OppoWifiAssistantUtils();
            }
        });
    }

    public /* synthetic */ void lambda$saveWifiNetworkRecord$1$OppoWifiAssistantUtils() {
        List<OppoWifiAssistantRecord> wifiRecords = getWifiNetworkRecords();
        FileWriter config = null;
        BufferedWriter out = null;
        try {
            FileWriter config2 = new FileWriter(WIFI_ASSISTANT_FILE);
            BufferedWriter out2 = new BufferedWriter(config2);
            logD("saveWifiNetworkRecord length = " + wifiRecords.size());
            Iterator<OppoWifiAssistantRecord> it = wifiRecords.iterator();
            while (it.hasNext()) {
                out2.write(it.next().toTagString() + "\n");
                out2.flush();
            }
            out2.close();
            try {
                out2.close();
            } catch (IOException e) {
            }
            try {
                config2.close();
            } catch (IOException e2) {
            }
        } catch (FileNotFoundException e3) {
            logD("FileNotFoundException: " + e3);
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e4) {
                }
            }
            if (config != null) {
                config.close();
            }
        } catch (IOException e5) {
            logD("IOException: " + e5);
            e5.printStackTrace();
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e6) {
                }
            }
            if (config != null) {
                config.close();
            }
        } catch (Throwable th) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e7) {
                }
            }
            if (config != null) {
                try {
                    config.close();
                } catch (IOException e8) {
                }
            }
            throw th;
        }
    }

    public void addNetworkRecord(String configKey, OppoWifiAssistantRecord record, boolean save) {
        logD("addOrUpdateRecord: configKey=" + configKey);
        if (configKey != null) {
            synchronized (this.mWifiNetworkRecord) {
                this.mWifiNetworkRecord.put(configKey, record);
            }
            if (save) {
                saveWifiNetworkRecord();
            }
        }
    }

    public void removeNetworkRecord(String configKey) {
        synchronized (this.mWifiNetworkRecord) {
            this.mWifiNetworkRecord.remove(configKey);
        }
    }

    public void sortNetworkRecords() {
        List<ScanResult> currentScan = this.mScanRequestProxy.syncGetScanResultsList();
        List<OppoWifiAssistantRecord> tempRecords = getWifiNetworkRecords();
        List<OppoWifiAssistantRecord> sortValidRecords = new ArrayList<>();
        for (OppoWifiAssistantRecord tRecord : tempRecords) {
            if (tRecord.mNetworkValid && this.mWifiConfigManager.getConfiguredNetwork(tRecord.mConfigkey) != null) {
                int refeRssi = WifiConfiguration.INVALID_RSSI;
                ScanResult refScan = null;
                for (ScanResult result : currentScan) {
                    if ((WifiConfiguration.configKey(result).equals(tRecord.mConfigkey) || result.BSSID.equals(tRecord.mBssid)) && result.level > refeRssi) {
                        refeRssi = result.level;
                        refScan = result;
                    }
                }
                if (!(refeRssi == WifiConfiguration.INVALID_RSSI || refScan == null)) {
                    tRecord.mRssi = refeRssi;
                    int index = getQualityIndex(refScan.is5GHz(), refeRssi);
                    logD("snr:" + tRecord.mConfigkey + ",mri:" + refeRssi + ",mnq[" + index + "]=" + tRecord.mNetQualitys[index]);
                    if (tRecord.mNetQualitys[index] > 0) {
                        tRecord.mScore = tRecord.mNetQualitys[index];
                    }
                    sortValidRecords.add(tRecord);
                }
            }
        }
        Collections.sort(sortValidRecords, new Comparator<OppoWifiAssistantRecord>() {
            /* class com.android.server.wifi.OppoWifiAssistantUtils.AnonymousClass1 */

            public int compare(OppoWifiAssistantRecord b1, OppoWifiAssistantRecord b2) {
                if (b2.mScore == 0 || b1.mScore == 0 || b2.mScore == b1.mScore) {
                    return b2.mRssi - b1.mRssi;
                }
                return b2.mScore - b1.mScore;
            }
        });
        synchronized (this.mSortNetworkRecord) {
            this.mSortNetworkRecord.clear();
            for (OppoWifiAssistantRecord sRecord : sortValidRecords) {
                if (this.mWifiConfigManager.getConfiguredNetwork(sRecord.mConfigkey) != null) {
                    this.mSortNetworkRecord.add(sRecord);
                }
            }
        }
    }

    public OppoWifiAssistantRecord getNetworkRecord(String configKey) {
        synchronized (this.mWifiNetworkRecord) {
            if (configKey != null) {
                if (this.mWifiNetworkRecord.containsKey(configKey)) {
                    OppoWifiAssistantRecord oppoWifiAssistantRecord = new OppoWifiAssistantRecord(this.mWifiNetworkRecord.get(configKey));
                    return oppoWifiAssistantRecord;
                }
            }
            logE("getNetworkRecord invalid configKey!");
            return null;
        }
    }

    public List<OppoWifiAssistantRecord> getWifiNetworkRecords() {
        List<OppoWifiAssistantRecord> wifiRecord = new ArrayList<>();
        synchronized (this.mWifiNetworkRecord) {
            for (OppoWifiAssistantRecord wnr : this.mWifiNetworkRecord.values()) {
                wifiRecord.add(new OppoWifiAssistantRecord(wnr));
            }
        }
        return wifiRecord;
    }

    public List<OppoWifiAssistantRecord> getSortNetworkRecords() {
        List<OppoWifiAssistantRecord> sortRecord = new ArrayList<>();
        synchronized (this.mSortNetworkRecord) {
            for (OppoWifiAssistantRecord wnr : this.mSortNetworkRecord) {
                sortRecord.add(new OppoWifiAssistantRecord(wnr));
            }
        }
        return sortRecord;
    }

    private int getQualityIndex(boolean is5G, int rssi) {
        if (is5G) {
            int goodRssi5 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_5", -65).intValue();
            int lowRssi5 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_5", Integer.valueOf((int) STATIC_LOW_RSSI_5)).intValue();
            int badRssi5 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_5", Integer.valueOf((int) STATIC_BAD_RSSI_5)).intValue();
            if (rssi >= goodRssi5) {
                return 3;
            }
            if (rssi >= lowRssi5 && rssi < goodRssi5) {
                return 2;
            }
            if (rssi <= badRssi5 || rssi >= lowRssi5) {
                return 0;
            }
            return 1;
        }
        int goodRssi24 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_GOOD_RSSI_24", -65).intValue();
        int lowRssi24 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_LOW_RSSI_24", Integer.valueOf((int) STATIC_LOW_RSSI_24)).intValue();
        int badRssi24 = getRomUpdateIntegerValue("OPPO_WIFI_ASSISTANT_BAD_RSSI_24", Integer.valueOf((int) STATIC_BAD_RSSI_24)).intValue();
        if (rssi >= goodRssi24) {
            return 3;
        }
        if (rssi >= lowRssi24 && rssi < goodRssi24) {
            return 2;
        }
        if (rssi <= badRssi24 || rssi >= lowRssi24) {
            return 0;
        }
        return 1;
    }

    public void setNetworkRqeusted(boolean requested) {
        this.mNetworkRequested = requested;
        logD("setNetworkRqeusted -> " + this.mNetworkRequested);
    }

    public boolean is1x1IotRouter() {
        return WifiInjector.getInstance().getClientModeImpl().is1x1IotRouter();
    }

    public int enableDualSta(boolean forceEnable) {
        logD("enableDualSta() force=" + forceEnable);
        WifiInfo info = WifiInjector.getInstance().getClientModeImpl().syncRequestConnectionInfo();
        int lowRssiThreshold = getRomUpdateIntegerValue("OPPO_DUAL_STA_LOW_RSSI", -70).intValue();
        if (info == null || info.getRssi() <= lowRssiThreshold) {
            return -1;
        }
        this.mAsyncHandler.removeMessages(1);
        logD("enableDualSta() -> enableOppoWifiSta2...");
        return WifiInjector.getInstance().getWifiController().enableOppoWifiSta2(forceEnable);
    }

    public void disableDualSta() {
        logD("disableDualSta(), mNetworkRequested=" + this.mNetworkRequested);
        if (!this.mNetworkRequested) {
            WifiInjector.getInstance().getWifiController().disableOppoWifiSta2();
        }
    }

    public void disableDualStaWithDelay(int delayMilis) {
        logD("disableDualStaWithDelay disconnect wlan1 first.");
        WifiInjector.getInstance().getOppoClientModeImpl2().disconnectCommand();
        if (this.mAsyncHandler.hasMessages(1)) {
            logD("disableDualStaWithDelay already scheduled!!");
            return;
        }
        AsyncHandler asyncHandler = this.mAsyncHandler;
        asyncHandler.sendMessageDelayed(asyncHandler.obtainMessage(1), (long) delayMilis);
    }

    public boolean isDualStaDisablingWithDelay() {
        if (this.mAsyncHandler.hasMessages(1)) {
            return true;
        }
        return false;
    }

    public void setCanShowDialog(String ifName, boolean canShowDialog) {
        if (!TextUtils.isEmpty(ifName)) {
            logD("setCanShowDialog " + ifName + " -->" + canShowDialog);
            synchronized (this.mCanShowDialog) {
                this.mCanShowDialog.put(ifName, new Boolean(canShowDialog));
            }
        }
    }

    public boolean getOtherIfCanShowDialog(String ifName) {
        boolean otherIfCanShowDialog = true;
        if (!TextUtils.isEmpty(ifName)) {
            synchronized (this.mCanShowDialog) {
                for (String name : this.mCanShowDialog.keySet()) {
                    if (!ifName.equals(name)) {
                        if (this.mCanShowDialog.get(name) == null || this.mCanShowDialog.get(name).booleanValue()) {
                            otherIfCanShowDialog = true;
                        } else {
                            logD("getOtherIfCanShowDialog " + ifName + ", otherIfCanShowDialog:" + false);
                            return false;
                        }
                    }
                }
            }
        }
        logD("getOtherIfCanShowDialog " + ifName + " otherIfCanShowDialog:" + otherIfCanShowDialog);
        return otherIfCanShowDialog;
    }

    public void setCanChangeToCell(String ifName, boolean canChangeToCell) {
        if (!TextUtils.isEmpty(ifName)) {
            logD("setCanChangeToCell " + ifName + " -->" + canChangeToCell);
            synchronized (this.mCanChangeToCell) {
                this.mCanChangeToCell.put(ifName, new Boolean(canChangeToCell));
            }
        }
    }

    public boolean getOtherIfCanChangeToCell(String ifName) {
        boolean otherIfCanChangeToCell = true;
        if (!TextUtils.isEmpty(ifName)) {
            synchronized (this.mCanChangeToCell) {
                for (String name : this.mCanChangeToCell.keySet()) {
                    if (!ifName.equals(name)) {
                        if (this.mCanChangeToCell.get(name) == null || this.mCanChangeToCell.get(name).booleanValue()) {
                            otherIfCanChangeToCell = true;
                        } else {
                            logD("getOtherIfCanChangeToCell " + ifName + ", otherIfCanChangeToCell:" + false);
                            return false;
                        }
                    }
                }
            }
        }
        logD("getOtherIfCanChangeToCell " + ifName + " otherIfCanChangeToCell:" + otherIfCanChangeToCell);
        return otherIfCanChangeToCell;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003c, code lost:
        r0 = getOtherIfWifiInfo(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0040, code lost:
        if (r0 != null) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0042, code lost:
        if (r5 == null) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004a, code lost:
        if (r3.mPrimaryWifi.equals(r4) != false) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004c, code lost:
        com.android.server.wifi.OppoSlaManager.getInstance(r3.mContext).notifyPrimaryWifi(r3.mPrimaryWifi);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0058, code lost:
        if (r0 == null) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005a, code lost:
        if (r5 != null) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0062, code lost:
        if (r3.mPrimaryWifi.equals(r4) != false) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0064, code lost:
        com.android.server.wifi.OppoSlaManager.getInstance(r3.mContext).notifyPrimaryWifi(r3.mPrimaryWifi);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006f, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0070, code lost:
        if (r5 == null) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0072, code lost:
        if (r0 == null) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0074, code lost:
        r1 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0077, code lost:
        if (r3.mEnableRoamingInBand == r1) goto L_0x0093;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0079, code lost:
        r3.mEnableRoamingInBand = r1;
     */
    public synchronized void setWifiInfo(String ifName, WifiInfo info) {
        if (!TextUtils.isEmpty(ifName)) {
            logD("setWifiInfo " + ifName + " -->" + info);
            synchronized (this.mWifiInfos) {
                if (info == null) {
                    try {
                        this.mWifiInfos.put(ifName, null);
                    } catch (Throwable th) {
                        th = th;
                    }
                } else {
                    try {
                        this.mWifiInfos.put(ifName, new WifiInfo(info));
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            }
        } else {
            logE("setWifiInfo invalid interface name:" + ifName);
        }
    }

    public WifiInfo getOtherIfWifiInfo(String ifName) {
        WifiInfo wifiInfo = null;
        if (!TextUtils.isEmpty(ifName)) {
            synchronized (this.mWifiInfos) {
                for (String name : this.mWifiInfos.keySet()) {
                    if (!ifName.equals(name)) {
                        wifiInfo = this.mWifiInfos.get(name);
                    }
                }
            }
        }
        logD("getOtherIfWifiInfo (" + ifName + ") -> wifiInfo:" + wifiInfo);
        return wifiInfo;
    }

    private String getOtherIfName(String ifName) {
        if (IFACE_NAME_WLAN0.equals(ifName)) {
            return IFACE_NAME_WLAN1;
        }
        return IFACE_NAME_WLAN0;
    }

    public boolean isPrimaryWifi(String ifName) {
        return this.mPrimaryWifi.equals(ifName);
    }

    public void setWifiScore(String ifName, int score) {
        if (!TextUtils.isEmpty(ifName)) {
            logD("setWifiScore " + ifName + " -->" + score);
            synchronized (this.mWifiScores) {
                this.mWifiScores.put(ifName, new Integer(score));
            }
        }
    }

    public int getWifiScore(String ifName) {
        Integer score = null;
        if (!TextUtils.isEmpty(ifName)) {
            synchronized (this.mWifiScores) {
                score = this.mWifiScores.get(ifName);
            }
        }
        if (score == null) {
            score = new Integer(-1);
        }
        logD("getWifiScore " + ifName + " score:" + score);
        return score.intValue();
    }

    public int getWifiBestScore() {
        int bestScore = 0;
        synchronized (this.mWifiScores) {
            for (Integer score : this.mWifiScores.values()) {
                if (bestScore < score.intValue()) {
                    bestScore = score.intValue();
                }
            }
        }
        return bestScore;
    }

    public void resetTriggerData() {
        this.mCanTriggerData = true;
    }

    public void setupDataNetwork() {
        logD("setupDataNetwork mCanTriggerData=" + this.mCanTriggerData);
        this.mAsyncHandler.post(new Runnable() {
            /* class com.android.server.wifi.$$Lambda$OppoWifiAssistantUtils$l0urQTnDCPmV9BZhDfNiGgF53g */

            public final void run() {
                OppoWifiAssistantUtils.this.lambda$setupDataNetwork$2$OppoWifiAssistantUtils();
            }
        });
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
    public /* synthetic */ void lambda$setupDataNetwork$2$OppoWifiAssistantUtils() {
        if (this.mCanTriggerData) {
            this.mCanTriggerData = false;
            logD("setupDataNetwork...");
            Intent intent = new Intent(WIFI_SCROE_CHANGE);
            intent.addFlags(67108864);
            intent.putExtra(EXTRA_ENALE_DATA, true);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    public void releaseDataNetwork() {
        logD("releaseDataNetwork mCanTriggerData=" + this.mCanTriggerData);
        this.mAsyncHandler.post(new Runnable() {
            /* class com.android.server.wifi.$$Lambda$OppoWifiAssistantUtils$Pi_UFC0x1OZz0okBzmxhfUv97mM */

            public final void run() {
                OppoWifiAssistantUtils.this.lambda$releaseDataNetwork$3$OppoWifiAssistantUtils();
            }
        });
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
    public /* synthetic */ void lambda$releaseDataNetwork$3$OppoWifiAssistantUtils() {
        if (!this.mCanTriggerData) {
            this.mCanTriggerData = true;
            logD("releaseDataNetwork...");
            Intent intent = new Intent(WIFI_SCROE_CHANGE);
            intent.addFlags(67108864);
            intent.putExtra(EXTRA_ENALE_DATA, false);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    public void updateArpParams(boolean bothWifiConnected) {
        if (this.mBothWifiConnected == bothWifiConnected) {
            logD("updateArpParams bothWifiConnected:" + bothWifiConnected + " duplicate calling..");
            return;
        }
        this.mBothWifiConnected = bothWifiConnected;
        this.mAsyncHandler.post(new Runnable(bothWifiConnected) {
            /* class com.android.server.wifi.$$Lambda$OppoWifiAssistantUtils$l49YpVCDZ5KGIoQ4hBU6OOvagA */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                OppoWifiAssistantUtils.this.lambda$updateArpParams$4$OppoWifiAssistantUtils(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$updateArpParams$4$OppoWifiAssistantUtils(boolean bothWifiConnected) {
        String arpIgnore = "0";
        String arpAnnounce = "0";
        if (bothWifiConnected) {
            arpIgnore = "1";
            arpAnnounce = "2";
        }
        try {
            FileUtils.stringToFile("/proc/sys/net/ipv4/conf/wlan0/arp_ignore", arpIgnore);
            FileUtils.stringToFile("/proc/sys/net/ipv4/conf/wlan0/arp_announce", arpAnnounce);
            FileUtils.stringToFile("/proc/sys/net/ipv4/conf/wlan1/arp_ignore", arpIgnore);
            FileUtils.stringToFile("/proc/sys/net/ipv4/conf/wlan1/arp_announce", arpAnnounce);
        } catch (Exception e) {
            logE("updateArpParams unable to write file" + e);
        }
        logD("After updateArpParams(" + bothWifiConnected + ")");
    }

    public void setIfGatewayInfo(String ifName, String ipAddr, String macAddr) {
        logD("setIfGatewayInfo ifName=" + ifName + " macAddr=" + macAddr);
        if (IFACE_NAME_WLAN0.equals(ifName)) {
            this.mWlan0GatewayIp = ipAddr;
            this.mWlan0GatewayMac = macAddr;
        } else if (IFACE_NAME_WLAN1.equals(ifName)) {
            this.mWlan1GatewayIp = ipAddr;
            this.mWlan1GatewayMac = macAddr;
        } else {
            logE("setIfGatewayInfo invalid ifName:" + ifName);
        }
        logD("After setIfGatewayInfo wlan0:" + this.mWlan0GatewayIp + "/" + this.mWlan0GatewayMac + " wlan1:" + this.mWlan1GatewayIp + "/" + this.mWlan1GatewayMac);
    }

    public boolean isConflictGatewayIpAndDiffMac() {
        return !TextUtils.isEmpty(this.mWlan0GatewayIp) && !TextUtils.isEmpty(this.mWlan0GatewayMac) && !TextUtils.isEmpty(this.mWlan1GatewayIp) && !TextUtils.isEmpty(this.mWlan1GatewayMac) && this.mWlan0GatewayIp.equals(this.mWlan1GatewayIp) && !this.mWlan0GatewayMac.equals(this.mWlan1GatewayMac);
    }

    public boolean hasWlanAssistant() {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        boolean hasFeature = true;
        int romUpdate = Settings.Global.getInt(context.getContentResolver(), "rom.update.wifi.assistant", 1);
        boolean wlanAssistantFeature = this.mContext.getPackageManager().hasSystemFeature("oppo.common_center.wlan.assistant");
        boolean romUpdateWlanAssistant = romUpdate == 1;
        if (!wlanAssistantFeature || !romUpdateWlanAssistant) {
            hasFeature = false;
        }
        return hasFeature;
    }

    public boolean isSwitchEnable() {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        boolean isSwitchEnable = true;
        if (Settings.Global.getInt(context.getContentResolver(), "wifi_auto_change_access_point", 1) != 1) {
            isSwitchEnable = false;
        }
        return isSwitchEnable;
    }

    public boolean getEnabledState() {
        if (!getRomUpdateBooleanValue("OPPO_WIFI_ASSISTANT_FEATURE", true).booleanValue() || !hasWlanAssistant() || !isSwitchEnable()) {
            return false;
        }
        return true;
    }

    public boolean getIsOppoManuConnect() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().isManuConnect();
        }
        return false;
    }

    public String getRomUpdateValue(String key, String defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Double getRomUpdateFloatValue(String key, Double defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getFloatValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Long getRomUpdateLongValue(String key, Long defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return wifiRomUpdateHelper.getLongValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Boolean getRomUpdateBooleanValue(String key, Boolean defaultVal) {
        WifiRomUpdateHelper wifiRomUpdateHelper = this.mWifiRomUpdateHelper;
        if (wifiRomUpdateHelper != null) {
            return Boolean.valueOf(wifiRomUpdateHelper.getBooleanValue(key, defaultVal.booleanValue()));
        }
        return defaultVal;
    }

    /* access modifiers changed from: private */
    public void logD(String log) {
        if (sDebug) {
            Log.d(TAG, "" + log);
        }
    }

    private void logE(String log) {
        Log.e(TAG, "" + log);
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            sDebug = true;
        } else {
            sDebug = false;
        }
    }
}
