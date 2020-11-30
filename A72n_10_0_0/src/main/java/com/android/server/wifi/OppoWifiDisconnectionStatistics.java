package com.android.server.wifi;

import android.content.Context;
import android.content.pm.ApplicationInfo;
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
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.OppoManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import oppo.util.OppoStatistics;

public class OppoWifiDisconnectionStatistics {
    private static String[] AP_PRODUCT_TAG = {"prod_madelName\">", "sys_board\">", "modelname\">", "title>", "TITLE>"};
    private static final long AVERAGE_RSSI_CALCULATE_TIME = 60000;
    private static final int BAD_RSSI_THRESHOLD = -75;
    private static final String DATA_STALL_EVENT_ID = "060202";
    private static boolean DEBUG = false;
    private static final int DHCP_RENEW_FAILED = 3;
    private static final int DHCP_RENEW_INIT = 0;
    private static final int DHCP_RENEW_START = 1;
    private static final int DHCP_RENEW_SUCCESS = 2;
    private static final String DISCONNECTION_EVENT_ID = "060205";
    private static final String DISCONNECTION_TYPE = "disconnection_type";
    private static final int DISCONNECTION_TYPE_COREDUMP = 2;
    private static final int DISCONNECTION_TYPE_DHCP = 3;
    private static final int DISCONNECTION_TYPE_GOODRSSI = 6;
    private static final int DISCONNECTION_TYPE_MAX = 7;
    private static final int DISCONNECTION_TYPE_MIN = 0;
    private static final int DISCONNECTION_TYPE_NORMAL = 1;
    private static final int DISCONNECTION_TYPE_THIRDAPK = 4;
    private static final int DISCONNECTION_TYPE_UPLAYER = 5;
    public static final int EVENT_ASSOC = 1;
    public static final int EVENT_AUTH = 2;
    public static final int EVENT_CONNECTED = 5;
    public static final int EVENT_DHCP = 3;
    public static final int EVENT_PROBE = 4;
    public static final int EVENT_START = 0;
    private static final int LOCAL_DISCONNECT_EVENT = 1;
    private static final int LOCAL_DISCONNECT_REASON = 3;
    private static final int MAXNS = 4;
    private static final int RESOLVER_PARAMS_COUNT = 6;
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
    private static final Object mDisconUploadLock = new Object();
    private final int REASON_CODE_DHCP_FAILURE = -4;
    private String[] SYSTEM_DISCONNECT_PKGNAME = {"", "coredump", "dhcp_renew_fail", "wifi_off", "macaddr_Credential_changed", "WN_S", "captivae_portal"};
    private boolean isHiddenAp = false;
    private boolean isStaticIp = false;
    private String mApBssLoad = null;
    private String mApName = null;
    private long mAssocTime;
    private Handler mAsyncHtmlHandler;
    private long mAuthTime;
    private int mBandwidth = -1;
    private String mBssid = null;
    private final ClientModeImpl mClientModeImpl;
    private final Clock mClock;
    private String mConPkgName = null;
    private int mConRssi = -127;
    private String mConfigKey = null;
    private int mConnectedOrRoamTimes = 0;
    private long mConnectedTime = 0;
    private final Context mContext;
    private int mCurChannelApNum = 0;
    private int mCurrentRssi = -127;
    private int mDhcpRenewState = 0;
    private long mDhcpTime;
    private WifiConfiguration mDisConfig = null;
    private int mDisRotationState = 0;
    private int mDisRssi = -127;
    private WifiInfo mDisWifiInfo = null;
    private String mDisconnectAppName = null;
    private OppoWifiDisconnectInfo mDisconnectInfo = null;
    private String mDisconnectPkgName = null;
    private Display mDisplay;
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.android.server.wifi.OppoWifiDisconnectionStatistics.AnonymousClass1 */

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            OppoWifiDisconnectionStatistics oppoWifiDisconnectionStatistics = OppoWifiDisconnectionStatistics.this;
            oppoWifiDisconnectionStatistics.mRotation = oppoWifiDisconnectionStatistics.mDisplay.getRotation();
            OppoWifiDisconnectionStatistics oppoWifiDisconnectionStatistics2 = OppoWifiDisconnectionStatistics.this;
            oppoWifiDisconnectionStatistics2.logd("wifi rt:" + OppoWifiDisconnectionStatistics.this.mRotation);
            if (OppoWifiDisconnectionStatistics.this.mRotation == 0 || OppoWifiDisconnectionStatistics.this.mRotation == 2) {
                OppoWifiDisconnectionStatistics.this.mRotationState = 0;
            } else if (OppoWifiDisconnectionStatistics.this.mRotation == 1) {
                OppoWifiDisconnectionStatistics.this.mRotationState = 1;
            } else if (OppoWifiDisconnectionStatistics.this.mRotation == 3) {
                OppoWifiDisconnectionStatistics.this.mRotationState = 3;
            }
            if (OppoWifiDisconnectionStatistics.this.mRotationState != OppoWifiDisconnectionStatistics.this.mLastRotationState) {
                long[] jArr = OppoWifiDisconnectionStatistics.this.mRotationDistribution;
                int i = OppoWifiDisconnectionStatistics.this.mLastRotationState;
                jArr[i] = jArr[i] + (OppoWifiDisconnectionStatistics.this.mClock.getWallClockMillis() - OppoWifiDisconnectionStatistics.this.mLastRotationTimeStamp);
                OppoWifiDisconnectionStatistics oppoWifiDisconnectionStatistics3 = OppoWifiDisconnectionStatistics.this;
                oppoWifiDisconnectionStatistics3.mLastRotationTimeStamp = oppoWifiDisconnectionStatistics3.mClock.getWallClockMillis();
                OppoWifiDisconnectionStatistics oppoWifiDisconnectionStatistics4 = OppoWifiDisconnectionStatistics.this;
                oppoWifiDisconnectionStatistics4.mLastRotationState = oppoWifiDisconnectionStatistics4.mRotationState;
            }
        }
    };
    private int[] mDnsErrors = new int[4];
    private int[] mDnsSuccesses = new int[4];
    private int[] mDnsUsable = new int[4];
    private String[] mDnses = new String[4];
    private long mEndTime;
    private int mFrequency = 0;
    private String mInterfaceName = OppoWifiAssistantUtils.IFACE_NAME_WLAN0;
    private String mIpConflictMac = null;
    private boolean mIsThirdApp = false;
    private int mLastRotationState = 0;
    private long mLastRotationTimeStamp = 0;
    private int mLocalGenerate = -1;
    private OppoWifiCoredumpObserver mObserver = null;
    private OppoRssiMonitor mOppoRssiMonitor;
    private long mProbeTime;
    private int mRandomMac = 0;
    private int mReasonCode = -1;
    private int mRotation = 0;
    private long[] mRotationDistribution = new long[4];
    private int[] mRotationRssiCnt = new int[4];
    private long[] mRotationRssiSum = new long[4];
    private int mRotationState = 0;
    private final ScanRequestProxy mScanRequestProxy;
    private boolean mScreenOn = false;
    private long mStartTime;
    private int mSystemDisconType = 0;
    private String mVendorInfo = null;
    private final WifiConfigManager mWifiConfigManager;
    private String mWifiCoredumpRecordDir = "/data/oppo/log/DCS/de/network_logs/stp_dump";
    private final WifiNative mWifiNative;
    private String mWifiStandard = "";

    OppoWifiDisconnectionStatistics(Context mCtxt, ClientModeImpl mWsm, WifiConfigManager mWcm, WifiNative mWnt, Clock clock, ScanRequestProxy mSrp) {
        this.mContext = mCtxt;
        this.mClientModeImpl = mWsm;
        this.mWifiConfigManager = mWcm;
        this.mWifiNative = mWnt;
        this.mClock = clock;
        this.mScanRequestProxy = mSrp;
        this.mOppoRssiMonitor = new OppoRssiMonitor(this.mContext);
        HandlerThread htmlThread = new HandlerThread("GetApProductVersion");
        htmlThread.start();
        this.mAsyncHtmlHandler = new Handler(htmlThread.getLooper());
        DisplayManager dm = (DisplayManager) this.mContext.getSystemService("display");
        dm.registerDisplayListener(this.mDisplayListener, null);
        this.mDisplay = dm.getDisplay(0);
        this.mRotation = this.mDisplay.getRotation();
        this.mDisconnectInfo = new OppoWifiDisconnectInfo();
        File dir = new File(this.mWifiCoredumpRecordDir);
        if (!dir.exists()) {
            logd(" mkdirs " + this.mWifiCoredumpRecordDir);
            dir.mkdirs();
        }
        this.mObserver = new OppoWifiCoredumpObserver(this.mWifiCoredumpRecordDir);
        init();
    }

    public void enableVerboseLogging(int verbose) {
        DEBUG = verbose > 0;
        this.mOppoRssiMonitor.enableVerbose(DEBUG);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String msg) {
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
        this.mBandwidth = -1;
        this.mBssid = null;
        this.mVendorInfo = "";
        this.mConnectedTime = 0;
        this.mApBssLoad = "";
        this.mConnectedOrRoamTimes = 0;
        this.mCurChannelApNum = 0;
        initRotationMessage();
        this.mRandomMac = 0;
        this.mWifiStandard = "";
        this.mConRssi = -127;
        this.isHiddenAp = false;
        this.isStaticIp = false;
        this.mConPkgName = null;
        this.mDisconnectPkgName = null;
        this.mDisconnectAppName = null;
        this.mIsThirdApp = false;
        this.mSystemDisconType = 0;
        OppoWifiDisconnectInfo oppoWifiDisconnectInfo = this.mDisconnectInfo;
        if (oppoWifiDisconnectInfo != null) {
            oppoWifiDisconnectInfo.init();
        }
        this.mIpConflictMac = null;
        OppoWifiCoredumpObserver oppoWifiCoredumpObserver = this.mObserver;
        if (oppoWifiCoredumpObserver != null) {
            oppoWifiCoredumpObserver.stopWatching();
        }
        clearConnectTimestamp();
    }

    public void connectedInit(WifiConfiguration config, WifiInfo wifiInfo) {
        if (config != null && wifiInfo != null) {
            updateConnectEvent(5);
            this.mDisConfig = new WifiConfiguration(config);
            this.mDisWifiInfo = new WifiInfo(wifiInfo);
            this.mDhcpRenewState = 0;
            int i = -1;
            this.mReasonCode = -1;
            this.mLocalGenerate = -1;
            this.mConfigKey = this.mDisConfig.configKey();
            this.mDisRssi = -127;
            this.mFrequency = this.mDisWifiInfo.getFrequency();
            this.mBssid = this.mDisWifiInfo.getBSSID();
            boolean z = true;
            this.mConnectedOrRoamTimes++;
            this.mConnectedTime = 0;
            this.mApBssLoad = this.mWifiNative.getApBssLoad(this.mInterfaceName);
            this.mCurChannelApNum = 0;
            this.mCurrentRssi = getRssiFromScanResult(this.mBssid);
            this.mApName = null;
            initRotationMessage();
            ScanResult sr = findApFromScanResults(this.mBssid);
            this.mWifiStandard = OppoInformationElementUtil.getWifiStandard(sr);
            if (sr != null) {
                i = sr.channelWidth;
            }
            this.mBandwidth = i;
            this.mVendorInfo = OppoInformationElementUtil.getVendorSpec(sr);
            this.mRandomMac = this.mDisConfig.macRandomizationSetting;
            this.mConRssi = this.mCurrentRssi;
            this.isHiddenAp = this.mDisConfig.hiddenSSID;
            if (IpConfiguration.IpAssignment.STATIC != this.mDisConfig.getIpAssignment()) {
                z = false;
            }
            this.isStaticIp = z;
            if (OppoManuConnectManager.getInstance() != null) {
                PackageManager pm = this.mContext.getPackageManager();
                int conUid = OppoManuConnectManager.getInstance().getManuConnectUid();
                if (pm != null) {
                    this.mConPkgName = pm.getNameForUid(conUid);
                }
            }
            OppoWifiDisconnectInfo oppoWifiDisconnectInfo = this.mDisconnectInfo;
            if (oppoWifiDisconnectInfo != null) {
                oppoWifiDisconnectInfo.init();
            }
            this.mDisconnectPkgName = null;
            this.mDisconnectAppName = null;
            this.mIsThirdApp = false;
            this.mSystemDisconType = 0;
            OppoWifiCoredumpObserver oppoWifiCoredumpObserver = this.mObserver;
            if (oppoWifiCoredumpObserver != null) {
                oppoWifiCoredumpObserver.mRecordTime = 0;
                oppoWifiCoredumpObserver.startWatching();
            }
            this.mOppoRssiMonitor.sendConnectedEvent(this.mCurrentRssi, config);
        }
    }

    public void startDisconnectionDetect(boolean isBigData, boolean force) {
        synchronized (mDisconUploadLock) {
            if (this.mConfigKey != null) {
                if (isMtkPlatform() && !force) {
                    if ((!isBigData && this.mDisconnectInfo.mReasonCode == -1) || (isBigData && this.mReasonCode == -1)) {
                        return;
                    }
                    if (!(this.mLocalGenerate == 1 || this.mReasonCode == 3)) {
                        this.mReasonCode = this.mDisconnectInfo.mReasonCode;
                    }
                }
                syncCommonMessage();
                syncRotationMessage();
                syncResolverInfo();
                logd("Disconnection detect: reasonCode: " + this.mReasonCode + " local: " + this.mLocalGenerate + " rssi: " + this.mDisRssi + " dhcpState: " + this.mDhcpRenewState + " rotationState: " + this.mDisRotationState + " Dis_Pkt_Name: " + this.mDisconnectPkgName + " coredump time: " + this.mObserver.mRecordTime);
                int disconType = 1;
                if (this.mSystemDisconType == 3) {
                    OppoWifiDisconnectInfo oppoWifiDisconnectInfo = this.mDisconnectInfo;
                    this.mReasonCode = OppoWifiDisconnectInfo.REASONCODE_UPLAYER_BASE + this.mSystemDisconType;
                    if (this.mIsThirdApp) {
                        disconType = 4;
                    } else {
                        disconType = 5;
                    }
                } else if (this.mObserver.mRecordTime != 0) {
                    OppoWifiDisconnectInfo oppoWifiDisconnectInfo2 = this.mDisconnectInfo;
                    this.mReasonCode = OppoWifiDisconnectInfo.REASONCODE_UPLAYER_BASE + 1;
                    disconType = 2;
                } else if (this.mLocalGenerate == 1 && this.mReasonCode == 3) {
                    if (this.mDhcpRenewState == 3) {
                        OppoWifiDisconnectInfo oppoWifiDisconnectInfo3 = this.mDisconnectInfo;
                        this.mReasonCode = OppoWifiDisconnectInfo.REASONCODE_UPLAYER_BASE + 2;
                        disconType = 3;
                    } else if (this.mIsThirdApp) {
                        OppoWifiDisconnectInfo oppoWifiDisconnectInfo4 = this.mDisconnectInfo;
                        this.mReasonCode = OppoWifiDisconnectInfo.REASONCODE_UPLAYER_BASE + this.mSystemDisconType;
                        disconType = 4;
                    } else {
                        if (this.mSystemDisconType > 4) {
                            this.mDisconnectPkgName = this.SYSTEM_DISCONNECT_PKGNAME[this.mSystemDisconType];
                        }
                        OppoWifiDisconnectInfo oppoWifiDisconnectInfo5 = this.mDisconnectInfo;
                        this.mReasonCode = OppoWifiDisconnectInfo.REASONCODE_UPLAYER_BASE + this.mSystemDisconType;
                        disconType = 5;
                    }
                } else if (this.mDisRssi > BAD_RSSI_THRESHOLD) {
                    disconType = 6;
                }
                wifiDisconnectionStatistics(disconType);
                this.mOppoRssiMonitor.sendDisconnectedEvent();
                init();
            }
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
        this.mOppoRssiMonitor.rssiChange(rssi);
        if (rssi > -127 && rssi < 200) {
            this.mCurrentRssi = rssi;
            if (this.mConRssi == -127) {
                this.mConRssi = rssi;
            }
            long[] jArr = this.mRotationRssiSum;
            int i = this.mRotationState;
            jArr[i] = jArr[i] + ((long) rssi);
            int[] iArr = this.mRotationRssiCnt;
            iArr[i] = iArr[i] + 1;
        }
    }

    public void syncFreq(int freq) {
        this.mFrequency = freq;
    }

    public void syncDisconnectionInfo(String data) {
        OppoWifiDisconnectInfo oppoWifiDisconnectInfo = this.mDisconnectInfo;
        if (oppoWifiDisconnectInfo != null) {
            oppoWifiDisconnectInfo.parseData(data);
            startDisconnectionDetect(true, false);
        }
    }

    public void syncDisconnectPktName(String pkgName, int uid, int index) {
        if (this.mDisconnectPkgName == null) {
            if (uid > 0) {
                this.mIsThirdApp = isThirdApp(uid);
                this.mDisconnectPkgName = pkgName != null ? pkgName : this.mContext.getPackageManager().getNameForUid(uid);
            } else {
                this.mDisconnectPkgName = "";
            }
            this.mSystemDisconType = index;
            if (this.mSystemDisconType == 3) {
                startDisconnectionDetect(false, true);
            }
        }
    }

    private boolean isThirdApp(int uid) {
        if (uid < 10000 || uid > 19999) {
            return false;
        }
        this.mDisconnectAppName = statisticsAppName(uid);
        return true;
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
        if (disconType < 7 && disconType > 0 && this.mDisConfig != null && this.mDisWifiInfo != null) {
            map.put(DISCONNECTION_TYPE, Integer.toString(disconType));
            generateCommonMessage(map);
            generateRotationMessage(map);
            generateConnectMessage(map);
            generateDisconnectMessage(map);
            generateTimeStampMessage(map);
            this.mOppoRssiMonitor.generateRssiDistribution(map);
            logd("Disconnection Statistics map: " + map);
            OppoStatistics.onCommon(this.mContext, "wifi_fool_proof", "wifi_disconnection", map, false);
            wifiDisconnectCollectDataStallData();
            wifiDisconnectCollectDisconnectionData();
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
            map.put("Assist Dns fault count", String.valueOf(OppoDataStallHelper.getInstance().getDataStallAssistantDnsFaultCount()));
            map.put("Assist DUT fault count", String.valueOf(OppoDataStallHelper.getInstance().getDataStallAssistantDutFaultCount()));
            map.put("Assist AP fault count", String.valueOf(OppoDataStallHelper.getInstance().getDataStallAssistantApFaultCount()));
            map.put("Assist Other fault count", String.valueOf(OppoDataStallHelper.getInstance().getDataStallAssistantOtherFaultCount()));
            OppoManager.onStamp(DATA_STALL_EVENT_ID, map);
            logd("Data Stall statistics for customer service: " + map);
        }
    }

    private String stampToDate(long timeMillis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timeMillis));
    }

    private void wifiDisconnectCollectDisconnectionData() {
        if (this.mDisConfig != null && this.mDisRssi >= BAD_RSSI_THRESHOLD) {
            int reason = this.mReasonCode;
            int i = this.mReasonCode;
            OppoWifiDisconnectInfo oppoWifiDisconnectInfo = this.mDisconnectInfo;
            if (i >= OppoWifiDisconnectInfo.REASONCODE_UPLAYER_BASE) {
                int i2 = this.mReasonCode;
                OppoWifiDisconnectInfo oppoWifiDisconnectInfo2 = this.mDisconnectInfo;
                if (i2 == OppoWifiDisconnectInfo.REASONCODE_UPLAYER_BASE + 2) {
                    reason = -4;
                } else {
                    return;
                }
            }
            HashMap<String, String> map = new HashMap<>();
            if (OppoDataStallHelper.getInstance() != null) {
                map.put("config_key", this.mConfigKey);
                map.put("AP bssid", this.mBssid);
                map.put("Disconnect reason", Integer.toString(reason));
                map.put("Last connected time", stampToDate(this.mDisConfig.lastConnected));
                map.put("Disconnect time", stampToDate(this.mConnectedTime + this.mDisConfig.lastConnected));
                OppoManager.onStamp(DISCONNECTION_EVENT_ID, map);
                logd("Disconnection statistics for customer service: " + map);
            }
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
        this.mDisRotationState = this.mRotationState;
        long[] jArr = this.mRotationDistribution;
        int i = this.mLastRotationState;
        jArr[i] = jArr[i] + (this.mClock.getWallClockMillis() - this.mLastRotationTimeStamp);
        this.mLastRotationTimeStamp = this.mClock.getWallClockMillis();
    }

    private void initRotationMessage() {
        this.mLastRotationTimeStamp = this.mClock.getWallClockMillis();
        this.mLastRotationState = this.mRotationState;
        for (int i = 0; i < 4; i++) {
            this.mRotationDistribution[i] = 0;
            this.mRotationRssiSum[i] = 0;
            this.mRotationRssiCnt[i] = 0;
        }
    }

    private void generateCommonMessage(HashMap<String, String> map) {
        if (map != null) {
            map.put("config_key", this.mConfigKey);
            map.put("bw", String.valueOf(this.mBandwidth));
            map.put("vendor_info", this.mVendorInfo);
            map.put("connected_time", String.valueOf(this.mConnectedTime));
            map.put("roam_times", String.valueOf(this.mConnectedOrRoamTimes));
            map.put("screen_on", String.valueOf(this.mScreenOn));
            map.put("bss_load", this.mApBssLoad);
            map.put("ap_num", String.valueOf(this.mCurChannelApNum));
            map.put("ap_name", this.mApName);
        }
    }

    private void generateRotationMessage(HashMap<String, String> map) {
        if (map != null) {
            map.put("ro_state", String.valueOf(this.mDisRotationState));
            long[] jArr = this.mRotationDistribution;
            if (jArr[0] > 0) {
                map.put("ro_perp_ms", String.valueOf(jArr[0]));
                int[] iArr = this.mRotationRssiCnt;
                if (iArr[0] > 0) {
                    map.put("ro_perp_rssi", String.valueOf(this.mRotationRssiSum[0] / ((long) iArr[0])));
                }
            }
            long[] jArr2 = this.mRotationDistribution;
            if (jArr2[1] > 0) {
                map.put("ro_left_ms", String.valueOf(jArr2[1]));
                int[] iArr2 = this.mRotationRssiCnt;
                if (iArr2[1] > 0) {
                    map.put("ro_left_rssi", String.valueOf(this.mRotationRssiSum[1] / ((long) iArr2[1])));
                }
            }
            long[] jArr3 = this.mRotationDistribution;
            if (jArr3[3] > 0) {
                map.put("ro_right_ms", String.valueOf(jArr3[3]));
                int[] iArr3 = this.mRotationRssiCnt;
                if (iArr3[3] > 0) {
                    map.put("ro_right_rssi", String.valueOf(this.mRotationRssiSum[3] / ((long) iArr3[3])));
                }
            }
        }
    }

    private void generateConnectMessage(HashMap<String, String> map) {
        float sucRate;
        if (map != null) {
            map.put("con_rssi", String.valueOf(this.mConRssi));
            map.put("random_mac", String.valueOf(this.mRandomMac));
            map.put("hidden_ap", String.valueOf(this.isHiddenAp));
            map.put("wifi_standard", this.mWifiStandard);
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

    private void generateDisconnectMessage(HashMap<String, String> map) {
        if (map != null) {
            map.put("dis_rea", String.valueOf(this.mReasonCode));
            map.put("dis_rssi", String.valueOf(this.mDisRssi));
            map.put("loc_gen", String.valueOf(this.mLocalGenerate));
            map.put("freq", String.valueOf(this.mFrequency));
            map.put("discon_isCoredump", String.valueOf(this.mObserver.mRecordTime != 0));
            map.put("discon_pkgName", String.valueOf(this.mDisconnectPkgName));
            map.put("discon_appName", String.valueOf(this.mDisconnectAppName));
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

    private ScanResult findApFromScanResults(String bssid) {
        List<ScanResult> scanList;
        if (bssid == null || (scanList = this.mScanRequestProxy.syncGetScanResultsList()) == null || scanList.size() <= 0) {
            return null;
        }
        for (int i = 0; i < scanList.size(); i++) {
            ScanResult scanResult = scanList.get(i);
            if (bssid.equals(scanResult.BSSID)) {
                return scanResult;
            }
        }
        return null;
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
                                    OppoWifiDisconnectionStatistics.this.mApName = OppoWifiDisconnectionStatistics.this.parseApName(inStream2);
                                    OppoWifiDisconnectionStatistics oppoWifiDisconnectionStatistics2 = OppoWifiDisconnectionStatistics.this;
                                    oppoWifiDisconnectionStatistics2.logd("AP product version is: " + OppoWifiDisconnectionStatistics.this.mApName);
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
                            if (0 != 0) {
                                urlConnection.disconnect();
                            }
                            if (0 != 0) {
                                inStream.close();
                                return;
                            }
                            return;
                        } catch (Throwable th) {
                            if (0 != 0) {
                                try {
                                    urlConnection.disconnect();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                    throw th;
                                }
                            }
                            if (0 != 0) {
                                inStream.close();
                            }
                            throw th;
                        }
                    }
                    OppoWifiDisconnectionStatistics.this.logd("dhcpResults is null or gateway is null");
                    if (0 != 0) {
                        try {
                            urlConnection.disconnect();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                            return;
                        }
                    }
                    if (0 != 0) {
                        inStream.close();
                    }
                }
            });
        }
    }

    private String statisticsAppName(int uid) {
        String[] pkgs;
        PackageManager mPackageManager = this.mContext.getPackageManager();
        if (mPackageManager == null || (pkgs = mPackageManager.getPackagesForUid(uid)) == null) {
            return null;
        }
        for (String mPkgName : pkgs) {
            if (mPkgName != null) {
                try {
                    ApplicationInfo applicationInfo = mPackageManager.getApplicationInfoAsUser(mPkgName, 0, UserHandle.getUserId(uid));
                    if (applicationInfo != null) {
                        return mPackageManager.getApplicationLabel(applicationInfo).toString();
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public class OppoWifiCoredumpObserver extends FileObserver {
        public long mRecordTime = 0;
        private String mTAG = "OppoWifiCoredumpObserver";

        public OppoWifiCoredumpObserver(String path) {
            super(path, 256);
        }

        public void onEvent(int event, String path) {
            int e = event & 4095;
            String str = this.mTAG;
            Log.e(str, "event=" + event + " path=" + path + " e=" + e);
            if (e == 256 && path.equals("recordWCDTime")) {
                this.mRecordTime = OppoWifiDisconnectionStatistics.this.mClock.getWallClockMillis();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String parseApName(InputStream inStream) {
        int indexFront;
        int indexEnd;
        Scanner sc = new Scanner(inStream);
        List<String> tags = Arrays.asList(AP_PRODUCT_TAG);
        StringBuilder name = new StringBuilder();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            int i = 0;
            while (true) {
                if (i < tags.size()) {
                    String tag = tags.get(i);
                    if (TextUtils.isEmpty(tag) || !line.contains(tag) || indexFront >= (indexEnd = line.indexOf("<", (indexFront = line.indexOf(tag) + tag.length())))) {
                        i++;
                    } else {
                        name.append(line.substring(indexFront, indexEnd));
                        sc.close();
                        return name.toString();
                    }
                }
            }
        }
        sc.close();
        return name.toString();
    }

    public String getApName() {
        String str = this.mApName;
        if (str == null) {
            return "";
        }
        return str;
    }

    private void clearConnectTimestamp() {
        this.mStartTime = 0;
        this.mAssocTime = 0;
        this.mAuthTime = 0;
        this.mDhcpTime = 0;
        this.mProbeTime = 0;
        this.mEndTime = 0;
    }

    public void updateConnectEvent(int event) {
        logd("update connect event:" + event);
        if (event == 0) {
            clearConnectTimestamp();
            this.mStartTime = this.mClock.getWallClockMillis();
        } else if (event != 1) {
            if (event != 2) {
                if (event != 3) {
                    if (event != 4) {
                        if (event == 5 && this.mEndTime == 0) {
                            this.mEndTime = this.mClock.getWallClockMillis();
                        }
                    } else if (this.mProbeTime == 0) {
                        this.mProbeTime = this.mClock.getWallClockMillis();
                    }
                } else if (this.mDhcpTime == 0) {
                    this.mDhcpTime = this.mClock.getWallClockMillis();
                }
            } else if (this.mAuthTime == 0) {
                this.mAuthTime = this.mClock.getWallClockMillis();
            }
        } else if (this.mAssocTime == 0) {
            this.mAssocTime = this.mClock.getWallClockMillis();
        }
    }

    public void generateTimeStampMessage(HashMap<String, String> map) {
        if (map != null) {
            map.put("start_timestamp", String.valueOf(this.mStartTime));
            if (this.mStartTime == 0) {
                clearConnectTimestamp();
            }
            long j = this.mAssocTime;
            String str = "0";
            map.put("assoc_ms", j == 0 ? str : String.valueOf(j - this.mStartTime));
            long j2 = this.mAuthTime;
            map.put("auth_ms", j2 == 0 ? str : String.valueOf(j2 - this.mAssocTime));
            long j3 = this.mDhcpTime;
            map.put("dhcp_ms", j3 == 0 ? str : String.valueOf(j3 - this.mAuthTime));
            long j4 = this.mDhcpTime;
            if (j4 == 0) {
                long j5 = this.mProbeTime;
                if (j5 != 0) {
                    str = String.valueOf(j5 - this.mAuthTime);
                }
                map.put("probe_ms", str);
            } else {
                long j6 = this.mProbeTime;
                if (j6 != 0) {
                    str = String.valueOf(j6 - j4);
                }
                map.put("probe_ms", str);
            }
            map.put("end_timestamp", String.valueOf(this.mEndTime));
        }
    }

    private static boolean isMtkPlatform() {
        return SystemProperties.get("ro.board.platform", "oppo").toLowerCase().startsWith("mt");
    }
}
