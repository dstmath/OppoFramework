package com.android.server.wifi;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.IDnsResolver;
import android.net.IpConfiguration;
import android.net.LinkProperties;
import android.net.Network;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.OppoManager;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Display;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;
import oppo.util.OppoStatistics;

public class OppoWifiDisconnectionStatistics {
    private static final long AVERAGE_RSSI_CALCULATE_TIME = 60000;
    private static final int BAD_RSSI_THRESHOLD = -75;
    private static final String COMMON_MESSAGE = "common_message";
    private static final String CONNECT_MESSAGE = "connect_message";
    private static final String DATA_STALL_EVENT_ID = "060202";
    private static boolean DEBUG = false;
    private static final int DHCP_RENEW_FAILED = 3;
    private static final int DHCP_RENEW_INIT = 0;
    private static final int DHCP_RENEW_START = 1;
    private static final int DHCP_RENEW_SUCCESS = 2;
    private static final String DISCONNECTION_REASONCODE = "disconnection_reasoncode";
    private static final String DISCONNECTION_TYPE = "disconnection_type";
    private static final int DISCONNECTION_TYPE_COMMON = 1;
    private static final int DISCONNECTION_TYPE_DHCP = 3;
    private static final int DISCONNECTION_TYPE_MAX = 5;
    private static final int DISCONNECTION_TYPE_MIN = 0;
    private static final int DISCONNECTION_TYPE_NORMAL = 4;
    private static final int DISCONNECTION_TYPE_ROTATION = 2;
    private static final int MAXNS = 4;
    private static final int RESOLVER_PARAMS_COUNT = 6;
    private static final String ROTATION_MESSAGE = "rotation_message";
    private static final int ROTATION_STATE_HOR_LEFT = 1;
    private static final int ROTATION_STATE_HOR_RIGHT = 3;
    private static final int ROTATION_STATE_PERP = 0;
    private static final int STATS_COUNT = 7;
    private static final int STATS_ERRORS = 1;
    private static final int STATS_INTERNAL_ERRORS = 3;
    private static final int STATS_LAST_SAMPLE_TIME = 5;
    private static final int STATS_RTT_AVG = 4;
    private static final int STATS_SUCCESSES = 0;
    private static final int STATS_TIMEOUTS = 2;
    private static final int STATS_USABLE = 6;
    private static final String TAG = "OppoWifiDisconnectionStatistics";
    private boolean isHiddenAp = false;
    private boolean isStaticIp = false;
    private String mApBssLoad = null;
    /* access modifiers changed from: private */
    public String mApName = null;
    private String mApVhtOperation = null;
    private Handler mAsyncHtmlHandler;
    private String mBssid = null;
    private final ClientModeImpl mClientModeImpl;
    private final Clock mClock;
    private String mConPkgName = null;
    private int mConRssi = -127;
    private String mConfigKey = null;
    private long mConnectedTime = 0;
    private final Context mContext;
    private int mCurChannelApNum = 0;
    private int mCurrentRssi = -127;
    private int mDhcpRenewState = 0;
    private int mDisAverageRotationRssi = -127;
    private WifiConfiguration mDisConfig = null;
    private int mDisLastRotationRssi = -127;
    private int mDisRotationState = 0;
    private int mDisRssi = -127;
    private WifiInfo mDisWifiInfo = null;
    /* access modifiers changed from: private */
    public Display mDisplay;
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.android.server.wifi.OppoWifiDisconnectionStatistics.AnonymousClass1 */

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            OppoWifiDisconnectionStatistics oppoWifiDisconnectionStatistics = OppoWifiDisconnectionStatistics.this;
            int unused = oppoWifiDisconnectionStatistics.mRotation = oppoWifiDisconnectionStatistics.mDisplay.getRotation();
            OppoWifiDisconnectionStatistics oppoWifiDisconnectionStatistics2 = OppoWifiDisconnectionStatistics.this;
            oppoWifiDisconnectionStatistics2.logd("wifi rt:" + OppoWifiDisconnectionStatistics.this.mRotation);
            if (OppoWifiDisconnectionStatistics.this.mRotation == 0 || OppoWifiDisconnectionStatistics.this.mRotation == 2) {
                int unused2 = OppoWifiDisconnectionStatistics.this.mRotationState = 0;
                OppoWifiDisconnectionStatistics.this.initRotationMessage();
            } else if (OppoWifiDisconnectionStatistics.this.mRotation == 1) {
                if (OppoWifiDisconnectionStatistics.this.mRotationState != 1) {
                    OppoWifiDisconnectionStatistics.this.resetRotationHorMessage();
                }
                int unused3 = OppoWifiDisconnectionStatistics.this.mRotationState = 1;
            } else if (OppoWifiDisconnectionStatistics.this.mRotation == 3) {
                if (OppoWifiDisconnectionStatistics.this.mRotationState != 3) {
                    OppoWifiDisconnectionStatistics.this.resetRotationHorMessage();
                }
                int unused4 = OppoWifiDisconnectionStatistics.this.mRotationState = 3;
            }
        }
    };
    private int[] mDnsErrors = new int[4];
    private int[] mDnsSuccesses = new int[4];
    private int[] mDnsUsable = new int[4];
    private String[] mDnses = new String[4];
    private int mFrequency = 0;
    private String mInterfaceName = OppoWifiAssistantUtils.IFACE_NAME_WLAN0;
    private String mIpConflictMac = null;
    private long mLastRotationHorTime = 0;
    private int mLastRotationRssi = -127;
    private int mLocalDisconnect = 0;
    private int mLocalGenerate = -1;
    private int mReasonCode = -1;
    /* access modifiers changed from: private */
    public int mRotation = 0;
    private int mRotationRssiNum = 0;
    private int mRotationRssiSum = 0;
    /* access modifiers changed from: private */
    public int mRotationState = 0;
    private final ScanRequestProxy mScanRequestProxy;
    private boolean mScreenOn = false;
    private String mVendorInfo = null;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiNative mWifiNative;

    OppoWifiDisconnectionStatistics(Context mCtxt, ClientModeImpl mWsm, WifiConfigManager mWcm, WifiNative mWnt, Clock clock, ScanRequestProxy mSrp) {
        this.mContext = mCtxt;
        this.mClientModeImpl = mWsm;
        this.mWifiConfigManager = mWcm;
        this.mWifiNative = mWnt;
        this.mClock = clock;
        this.mScanRequestProxy = mSrp;
        HandlerThread htmlThread = new HandlerThread("GetApProductVersion");
        htmlThread.start();
        this.mAsyncHtmlHandler = new Handler(htmlThread.getLooper());
        DisplayManager dm = (DisplayManager) this.mContext.getSystemService("display");
        dm.registerDisplayListener(this.mDisplayListener, null);
        this.mDisplay = dm.getDisplay(0);
        this.mRotation = this.mDisplay.getRotation();
        init();
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DEBUG = true;
        } else {
            DEBUG = false;
        }
    }

    /* access modifiers changed from: private */
    public void logd(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public void setInterfaceName(String interfaceName) {
        this.mInterfaceName = interfaceName;
    }

    private void init() {
        this.mDhcpRenewState = 0;
        this.mCurrentRssi = -127;
        this.mReasonCode = -1;
        this.mLocalGenerate = -1;
        this.mConfigKey = null;
        this.mDisRssi = -127;
        this.mFrequency = 0;
        this.mBssid = null;
        this.mVendorInfo = "";
        this.mConnectedTime = 0;
        this.mApBssLoad = "";
        this.mCurChannelApNum = 0;
        this.mLocalDisconnect = 0;
        this.mDisLastRotationRssi = -127;
        this.mDisAverageRotationRssi = -127;
        this.mApVhtOperation = null;
        this.mConRssi = -127;
        this.isHiddenAp = false;
        this.isStaticIp = false;
        this.mConPkgName = null;
        this.mIpConflictMac = null;
    }

    public void connectedInit(WifiConfiguration config, WifiInfo wifiInfo) {
        if (config != null && wifiInfo != null) {
            this.mDisConfig = new WifiConfiguration(config);
            this.mDisWifiInfo = new WifiInfo(wifiInfo);
            boolean z = false;
            this.mDhcpRenewState = 0;
            this.mReasonCode = -1;
            this.mLocalGenerate = -1;
            this.mConfigKey = this.mDisConfig.configKey();
            this.mDisRssi = -127;
            this.mFrequency = this.mDisWifiInfo.getFrequency();
            this.mBssid = this.mDisWifiInfo.getBSSID();
            this.mVendorInfo = this.mWifiNative.getApVendorSpec(this.mInterfaceName);
            this.mConnectedTime = 0;
            this.mApBssLoad = this.mWifiNative.getApBssLoad(this.mInterfaceName);
            this.mCurChannelApNum = 0;
            this.mCurrentRssi = getRssiFromScanResult(this.mBssid);
            this.mLocalDisconnect = 0;
            this.mApName = null;
            this.mDisLastRotationRssi = -127;
            this.mDisAverageRotationRssi = -127;
            if (this.mDisRotationState == 0) {
                initRotationMessage();
            } else {
                resetRotationHorMessage();
            }
            this.mApVhtOperation = this.mWifiNative.getApVhtOperation(this.mInterfaceName);
            this.mConRssi = this.mCurrentRssi;
            this.isHiddenAp = this.mDisConfig.hiddenSSID;
            if (IpConfiguration.IpAssignment.STATIC == this.mDisConfig.getIpAssignment()) {
                z = true;
            }
            this.isStaticIp = z;
            if (OppoManuConnectManager.getInstance() != null) {
                PackageManager pm = this.mContext.getPackageManager();
                int conUid = OppoManuConnectManager.getInstance().getManuConnectUid();
                if (pm != null) {
                    this.mConPkgName = pm.getNameForUid(conUid);
                }
            }
        }
    }

    public void startDisconnectionDetect() {
        if (this.mConfigKey != null) {
            syncCommonMessage();
            syncRotationMessage();
            syncResolverInfo();
            logd("Disconnection detect: reasonCode: " + this.mReasonCode + " local: " + this.mLocalGenerate + " rssi: " + this.mDisRssi + " dhcpState: " + this.mDhcpRenewState + " rotationState: " + this.mRotationState);
            boolean isDisconnectionType = false;
            if (this.mLocalGenerate == 1 && this.mReasonCode == 3) {
                if (this.mDhcpRenewState == 3) {
                    wifiDisconnectionStatistics(3);
                    isDisconnectionType = true;
                } else {
                    this.mLocalDisconnect = 1;
                }
            } else if (!(this.mReasonCode == -1 || this.mLocalGenerate == -1 || this.mDisRssi < BAD_RSSI_THRESHOLD)) {
                wifiDisconnectionStatistics(1);
                isDisconnectionType = true;
            }
            if (!isDisconnectionType) {
                wifiDisconnectionStatistics(4);
            }
            init();
        }
    }

    public void handleNetworkDisconnectionEvent(Message msg) {
        if (msg != null) {
            this.mLocalGenerate = msg.arg1;
            this.mReasonCode = msg.arg2;
            logd("network disconnection event, reasonCode: " + this.mReasonCode + " local: " + this.mLocalGenerate);
        }
    }

    public void updateDisconnectionReason(int locallyGenerated, int reasonCode) {
        this.mLocalGenerate = locallyGenerated;
        this.mReasonCode = reasonCode;
    }

    public void handleDhcpRenewFailed() {
        this.mDhcpRenewState = 3;
        this.mLocalGenerate = 1;
        this.mReasonCode = 3;
        logd("Dhcp renew failed!");
    }

    public void syncScreenState(boolean screenOn) {
        this.mScreenOn = screenOn;
    }

    public void syncRssi(int rssi) {
        if (rssi > -127 && rssi < 200) {
            this.mCurrentRssi = rssi;
            if (this.mConRssi == -127) {
                this.mConRssi = rssi;
            }
            if (this.mLastRotationHorTime != 0 && this.mClock.getWallClockMillis() - this.mLastRotationHorTime <= 60000) {
                this.mRotationRssiSum += rssi;
                this.mRotationRssiNum++;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r2v3 int[]: [D('cm' android.net.ConnectivityManager), D('params' int[])] */
    private void syncResolverInfo() {
        LinkProperties lp = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getLinkProperties(1);
        if (lp != null) {
            Network nw = this.mClientModeImpl.getCurrentNetwork();
            IDnsResolver dnsResolver = IDnsResolver.Stub.asInterface(ServiceManager.getService("dnsresolver"));
            Collection<InetAddress> curDnses = lp.getDnsServers();
            String curDomains = lp.getDomains();
            if (nw != null && dnsResolver != null) {
                if (curDnses != null) {
                    int netId = nw.netId;
                    int dnsNum = curDnses.size() > 4 ? 4 : curDnses.size();
                    String[] dnses = new String[dnsNum];
                    String[] strArr = new String[dnsNum];
                    int[] stats = new int[(dnsNum * 7)];
                    try {
                        dnsResolver.getResolverInfo(netId, dnses, curDomains == null ? new String[0] : curDomains.split(" "), strArr, new int[6], stats, new int[4]);
                        for (int i = 0; i < dnses.length; i++) {
                            if (dnses[i] != null) {
                                this.mDnses[i] = new String(dnses[i]);
                                this.mDnsSuccesses[i] = stats[(i * 7) + 0];
                                this.mDnsErrors[i] = stats[(i * 7) + 1] + stats[(i * 7) + 2] + stats[(i * 7) + 3];
                                this.mDnsUsable[i] = stats[(i * 7) + 6];
                                logd("dns" + i + ": " + this.mDnses[i] + " suc: " + this.mDnsSuccesses[i] + " err: " + this.mDnsErrors[i] + " usable: " + this.mDnsUsable[i]);
                            }
                        }
                    } catch (Exception e) {
                        logd("getResolverInfo error:" + e);
                    }
                }
            }
        }
    }

    public void setIpConflictMac(String peerMac) {
        this.mIpConflictMac = peerMac;
    }

    private int getRssiFromScanResult(String bssid) {
        if (bssid == null) {
            return -127;
        }
        for (ScanResult sr : this.mScanRequestProxy.syncGetScanResultsList()) {
            if (bssid.equals(sr.BSSID)) {
                return sr.level;
            }
        }
        return -127;
    }

    private void wifiDisconnectionStatistics(int disconType) {
        HashMap<String, String> map = new HashMap<>();
        if (disconType < 5 && disconType > 0 && this.mDisConfig != null && this.mDisWifiInfo != null) {
            map.put(DISCONNECTION_TYPE, Integer.toString(disconType));
            generateCommonMessage(map);
            generateRotationMessage(map);
            generateConnectMessage(map);
            logd("Disconnection Statistics map: " + map);
            OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", "wifi_disconnection", map, false);
            wifiDisconnectCollectDataStallData();
        }
    }

    private void wifiDisconnectCollectDataStallData() {
        HashMap<String, String> map = new HashMap<>();
        if (OppoDataStallHelper.getInstance() != null) {
            map.put("config_key", this.mConfigKey);
            map.put("AP bssid", this.mBssid);
            map.put("Connection time", String.valueOf(this.mConnectedTime));
            map.put("DUT fault count", String.valueOf(OppoDataStallHelper.getInstance().getDataStallDutFaultCount()));
            map.put("AP fault count", String.valueOf(OppoDataStallHelper.getInstance().getDataStallApFaultCount()));
            map.put("Env fault count", String.valueOf(OppoDataStallHelper.getInstance().getDataStallEnvFaultCount()));
            OppoManager.onStamp(DATA_STALL_EVENT_ID, map);
            logd("Data Stall statistics for customer service: " + map);
        }
    }

    private void syncCommonMessage() {
        if (this.mDisConfig != null && this.mDisWifiInfo != null) {
            this.mDisRssi = this.mCurrentRssi;
            this.mConnectedTime = this.mClock.getWallClockMillis() - this.mDisConfig.lastConnected;
            this.mCurChannelApNum = getChannelApNum();
        }
    }

    private void syncRotationMessage() {
        int i;
        this.mDisRotationState = this.mRotationState;
        this.mDisLastRotationRssi = this.mLastRotationRssi;
        int i2 = this.mRotationRssiSum;
        if (i2 == 0 || (i = this.mRotationRssiNum) == 0) {
            this.mDisAverageRotationRssi = this.mDisLastRotationRssi;
        } else {
            this.mDisAverageRotationRssi = i2 / i;
        }
    }

    /* access modifiers changed from: private */
    public void initRotationMessage() {
        this.mLastRotationRssi = -127;
        this.mLastRotationHorTime = 0;
        this.mRotationRssiSum = 0;
        this.mRotationRssiNum = 0;
        logd("init rotation msg");
    }

    /* access modifiers changed from: private */
    public void resetRotationHorMessage() {
        this.mLastRotationRssi = this.mCurrentRssi;
        this.mLastRotationHorTime = this.mClock.getWallClockMillis();
        this.mRotationRssiSum = 0;
        this.mRotationRssiNum = 0;
        logd("reset rotation hor msg, rssi: " + this.mLastRotationRssi + " time: " + this.mLastRotationHorTime);
    }

    private void generateCommonMessage(HashMap<String, String> map) {
        if (map != null) {
            map.put("config_key", this.mConfigKey);
            map.put("dis_rssi", String.valueOf(this.mDisRssi));
            map.put("dis_rea", String.valueOf(this.mReasonCode));
            map.put("loc_gen", String.valueOf(this.mLocalGenerate));
            map.put("loc_dis", String.valueOf(this.mLocalDisconnect));
            map.put("freq", String.valueOf(this.mFrequency));
            map.put("vendor_info", this.mVendorInfo);
            map.put("connected_time", String.valueOf(this.mConnectedTime));
            map.put("screen_on", String.valueOf(this.mScreenOn));
            map.put("bss_load", this.mApBssLoad);
            map.put("ap_num", String.valueOf(this.mCurChannelApNum));
            map.put("ap_name", this.mApName);
        }
    }

    private void generateRotationMessage(HashMap<String, String> map) {
        if (map != null) {
            map.put("ro_state", String.valueOf(this.mDisRotationState));
            map.put("last_ro_rssi", String.valueOf(this.mDisLastRotationRssi));
            map.put("ave_ro_rssi", String.valueOf(this.mDisAverageRotationRssi));
        }
    }

    private void generateConnectMessage(HashMap<String, String> map) {
        float sucRate;
        if (map != null) {
            map.put("con_rssi", String.valueOf(this.mConRssi));
            map.put("hidden_ap", String.valueOf(this.isHiddenAp));
            map.put("11ac", this.mApVhtOperation);
            map.put("static_ip", String.valueOf(this.isStaticIp));
            map.put("con_pkg_name", this.mConPkgName);
            ClientModeImpl clientModeImpl = this.mClientModeImpl;
            if (!ClientModeImpl.isNotChineseOperator()) {
                for (int i = 0; i < 4; i++) {
                    if (this.mDnses[i] != null) {
                        int[] iArr = this.mDnsSuccesses;
                        int dnsSum = iArr[i] + this.mDnsErrors[i];
                        if (dnsSum > 0) {
                            sucRate = (float) (iArr[i] / dnsSum);
                        } else {
                            sucRate = -1.0f;
                        }
                        DecimalFormat dFormat = new DecimalFormat("#.##");
                        map.put("dns" + i, this.mDnses[i]);
                        map.put("dns_srate" + i, dFormat.format((double) sucRate));
                        map.put("dns_sum" + i, String.valueOf(dnsSum));
                    }
                }
                map.put("ip_conflict", this.mIpConflictMac);
            }
        }
    }

    private int getChannelApNum() {
        int apNum = 0;
        for (ScanResult sr : this.mScanRequestProxy.syncGetScanResultsList()) {
            if (this.mFrequency == sr.frequency) {
                apNum++;
            }
        }
        return apNum;
    }

    public void getApProductVersion(final DhcpResults result) {
        Handler handler = this.mAsyncHtmlHandler;
        if (handler != null) {
            handler.post(new Runnable() {
                /* class com.android.server.wifi.OppoWifiDisconnectionStatistics.AnonymousClass2 */

                public void run() {
                    HttpURLConnection urlConnection = null;
                    InputStream inStream = null;
                    DhcpResults dhcpResults = result;
                    if (dhcpResults != null) {
                        try {
                            if (dhcpResults.gateway != null) {
                                URL url = new URL("http:/" + dhcpResults.gateway.toString());
                                OppoWifiDisconnectionStatistics oppoWifiDisconnectionStatistics = OppoWifiDisconnectionStatistics.this;
                                oppoWifiDisconnectionStatistics.logd("probe gatewayAddress: " + url.toString());
                                HttpURLConnection urlConnection2 = (HttpURLConnection) url.openConnection();
                                urlConnection2.setInstanceFollowRedirects(false);
                                urlConnection2.setConnectTimeout(10000);
                                urlConnection2.setReadTimeout(10000);
                                urlConnection2.setUseCaches(false);
                                InputStream inStream2 = urlConnection2.getInputStream();
                                if (inStream2 == null) {
                                    OppoWifiDisconnectionStatistics.this.logd("the inputStream is null");
                                } else {
                                    Scanner sc = new Scanner(inStream2);
                                    while (true) {
                                        if (!sc.hasNextLine()) {
                                            break;
                                        }
                                        String line = sc.nextLine();
                                        OppoWifiDisconnectionStatistics.this.logd(line);
                                        if (line.contains("<title>")) {
                                            int indexFront = line.indexOf(">");
                                            int indexEnd = line.lastIndexOf("<");
                                            OppoWifiDisconnectionStatistics oppoWifiDisconnectionStatistics2 = OppoWifiDisconnectionStatistics.this;
                                            oppoWifiDisconnectionStatistics2.logd("indexFront = " + String.valueOf(indexFront) + " , indexEnd = " + String.valueOf(indexEnd));
                                            if (indexFront < indexEnd) {
                                                String unused = OppoWifiDisconnectionStatistics.this.mApName = line.substring(indexFront + 1, indexEnd);
                                                OppoWifiDisconnectionStatistics oppoWifiDisconnectionStatistics3 = OppoWifiDisconnectionStatistics.this;
                                                oppoWifiDisconnectionStatistics3.logd("AP product version is: " + OppoWifiDisconnectionStatistics.this.mApName);
                                            } else {
                                                OppoWifiDisconnectionStatistics.this.logd("unvaliable html title");
                                            }
                                        }
                                    }
                                    sc.close();
                                }
                                try {
                                    urlConnection2.disconnect();
                                    if (inStream2 != null) {
                                        inStream2.close();
                                        return;
                                    }
                                    return;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return;
                                }
                            }
                        } catch (IOException e2) {
                            e2.printStackTrace();
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            if (inStream != null) {
                                inStream.close();
                                return;
                            }
                            return;
                        } catch (Throwable th) {
                            if (urlConnection != null) {
                                try {
                                    urlConnection.disconnect();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                    throw th;
                                }
                            }
                            if (inStream != null) {
                                inStream.close();
                            }
                            throw th;
                        }
                    }
                    OppoWifiDisconnectionStatistics.this.logd("dhcpResults is null or gateway is null");
                    if (urlConnection != null) {
                        try {
                            urlConnection.disconnect();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                            return;
                        }
                    }
                    if (inStream != null) {
                        inStream.close();
                    }
                }
            });
        }
    }
}
