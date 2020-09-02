package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.net.INetd;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.INetworkManagementService;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import com.android.server.connectivity.DnsManager;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.OppoPrivateDnsHelper;
import com.android.server.connectivity.gatewayconflict.OppoArpPeer;
import com.android.server.connectivity.networkrecovery.OPPODnsSelfrecoveryEngine;
import com.android.server.oppo.TemperatureProvider;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class OppoConnectivityServiceHelper {
    private static final boolean DBG = true;
    private static final String DEFAULT_EVALUATION_BLACKLIST = "com.google.android";
    private static final String EXTRA_WIFI_TO_DATA = "wifi_to_data";
    private static final String EXTRA_WIFI_VALID = "wifi_valid";
    private static final boolean IS_LAB_TEST = "1".equalsIgnoreCase(SystemProperties.get("persist.sys.nw_lab_test", "0"));
    private static final String KEEP_CELL_NETWORK_FOR_WIFI_ASSISTANT = "keep_celluar_network_wifi_assistant";
    private static final String OPPO_DUAL_WIFI_STA2_IFNAME = "wlan1";
    private static final String TAG = "OppoConnectivityService";
    private static final int TRIGER_TYPE_UPDATE_DNS = 3;
    private static final int TRIGER_TYPE_WIFI_CONNTED = 1;
    private static final int TRIGER_TYPE_WIFI_INTERNET_DETECTED_INVAILED = 2;
    private static final int VALID_SCORE_THRESHOLD = 20;
    private static final String WIFI_AUTO_CHANGE_ACCESS_POINT = "wifi_auto_change_access_point";
    private static final String WIFI_NETWORK_CHANGE = "android.net.wifi.WIFI_TO_DATA";
    private static final String WLAN0_IFACE_NAME = "wlan0";
    private static final String WLAN1_IFACE_NAME = "wlan1";
    public boolean hasWifiAssistant = false;
    /* access modifiers changed from: private */
    public String mCellIfName = "";
    private final Context mContext;
    /* access modifiers changed from: private */
    public ConnectivityService mCs;
    protected OPPODnsSelfrecoveryEngine mDnsRecoverEngine;
    private OppoArpPeer.ArpPeerChangeCallback mGatewayDectectCallback = new OppoArpPeer.ArpPeerChangeCallback() {
        /* class com.android.server.OppoConnectivityServiceHelper.AnonymousClass1 */

        @Override // com.android.server.connectivity.gatewayconflict.OppoArpPeer.ArpPeerChangeCallback
        public void onArpReponseChanged(int arpResponseReceieved, Network network) {
            if (arpResponseReceieved == 1) {
                OppoConnectivityServiceHelper.this.mCs.sendNetworkConnectedMsg(network);
            }
        }
    };
    private INetd mNetd;
    private boolean mSlaCellularUp = false;
    boolean mSlaEnabled = false;
    private SlaTrafficRecord mSlaTrafficRecord;
    private boolean mSlaWlan0Up = false;
    private boolean mSlaWlan1Up = false;
    private WifiRomUpdateHelper mWifiRomUpdateHelper;

    public OppoConnectivityServiceHelper(Context context, INetworkManagementService netManager, DnsManager dnsManager, INetd netd, ConnectivityService cs) {
        this.mContext = (Context) Preconditions.checkNotNull(context, "missing Context");
        this.mCs = cs;
        this.mNetd = netd;
        this.mWifiRomUpdateHelper = WifiRomUpdateHelper.getInstance(this.mContext);
        if (this.mWifiRomUpdateHelper.getBooleanValue("NETWORK_DNS_RECOVERY_ENGINE_ENABLE", false) && isChineseOperator()) {
            try {
                this.mDnsRecoverEngine = new OPPODnsSelfrecoveryEngine(this.mContext, netManager, dnsManager);
            } catch (Exception e) {
                this.mDnsRecoverEngine = null;
                log("mDnsRecoverEngine init error" + e);
            }
        }
        OppoPrivateDnsHelper.getInstance(this.mContext);
        this.hasWifiAssistant = isWlanAssistantEnable();
        this.mSlaTrafficRecord = new SlaTrafficRecord();
    }

    public void handleNetworkTestedMsg(NetworkAgentInfo nai, boolean valid) {
        if (nai != null && isWifiTransportNetwork(nai)) {
            if (valid) {
                log("wifi valid");
                nai.mGatewayState.setDuplicateGatewayStatics();
                nai.mGatewayState.setGatewayStateDone();
                if (nai.networkCapabilities != null && !nai.networkCapabilities.hasTransport(8)) {
                    this.mCs.handleUpdateNetworkScore(nai, 79);
                }
            } else if (nai.mGatewayState.needReevaluateNetwork()) {
                nai.mGatewayState.reevaluateNetwork();
                this.mCs.reportNetworkConnectivity(nai.network, false);
            } else if (!nai.mGatewayState.isGatewayStateDone()) {
                nai.mGatewayState.restoreLastGatewayState();
            }
            startDnsRecoverEngine(nai);
        }
    }

    public void startDnsRecoverEngine(NetworkAgentInfo nai) {
        if (this.mDnsRecoverEngine != null && isWifiNetwork(nai) && isChineseOperator()) {
            this.mDnsRecoverEngine.onNetworkConnected(nai, 3);
        }
    }

    public static boolean isWifiTransportNetwork(NetworkAgentInfo nai) {
        if (nai == null || nai.networkCapabilities == null || ((!nai.networkCapabilities.hasTransport(1) && !nai.networkCapabilities.hasTransport(8)) || nai.networkInfo == null || nai.networkInfo.getState() != NetworkInfo.State.CONNECTED)) {
            return false;
        }
        return true;
    }

    public static boolean isWifiNetwork(NetworkAgentInfo nai) {
        if (nai == null || nai.networkInfo == null || nai.networkInfo.getType() != 1) {
            return false;
        }
        return true;
    }

    public static boolean isDualWifiSta2Network(NetworkAgentInfo nai) {
        if (!isWifiNetwork(nai) || nai.linkProperties == null || !"wlan1".equals(nai.linkProperties.getInterfaceName())) {
            return false;
        }
        return true;
    }

    private Collection<InetAddress> chooseDefaultDns(LinkProperties newLp, NetworkAgentInfo networkAgent) {
        InetAddress defaultDns;
        Collection<InetAddress> dnses = new ArrayList<>(newLp.getDnsServers());
        String dns = "114.114.114.114";
        int invalidNSThreshold = 0;
        boolean isChineseOper = isChineseOperator();
        WifiRomUpdateHelper helper = WifiRomUpdateHelper.getInstance(this.mContext);
        if (helper != null) {
            invalidNSThreshold = helper.getIntegerValue("NETWORK_INVALID_NS_THRESHOLD", 2).intValue();
            if (isChineseOper) {
                dns = helper.getValue("NETWORK_DEFAULT_DNS", "114.114.114.114");
            } else {
                dns = helper.getValue("NETWORK_EXP_DEFAULT_DNS", "8.8.8.8");
            }
        }
        if (isChineseOper) {
            ArrayList<InetAddress> sortedDnses = new ArrayList<>();
            if (newLp.hasIPv4Address()) {
                for (InetAddress ia : dnses) {
                    if (ia instanceof Inet4Address) {
                        sortedDnses.add(ia);
                    }
                }
                for (InetAddress ia2 : dnses) {
                    if (ia2 instanceof Inet6Address) {
                        sortedDnses.add(ia2);
                    }
                }
            } else {
                for (InetAddress ia3 : dnses) {
                    if (ia3 instanceof Inet6Address) {
                        sortedDnses.add(ia3);
                    }
                }
                for (InetAddress ia4 : dnses) {
                    if (ia4 instanceof Inet4Address) {
                        sortedDnses.add(ia4);
                    }
                }
            }
            dnses = sortedDnses;
        }
        try {
            defaultDns = NetworkUtils.numericToInetAddress(dns);
        } catch (IllegalArgumentException e) {
            loge("Error setting defaultDns using " + dns);
            defaultDns = null;
        }
        if (isSoftAp(newLp)) {
            loge("Don't add defaultDns for soft ap");
            defaultDns = null;
        }
        if (IS_LAB_TEST) {
            loge("skip changing default dns");
            defaultDns = null;
        }
        if (defaultDns != null && dnses.size() <= invalidNSThreshold && !dnses.contains(defaultDns)) {
            dnses.add(defaultDns);
            synchronized (networkAgent) {
                newLp.addDnsServer(defaultDns);
            }
        }
        return dnses;
    }

    public boolean isChineseOperator() {
        String mcc = SystemProperties.get("android.telephony.mcc_change", "");
        String mcc2 = SystemProperties.get("android.telephony.mcc_change2", "");
        if (!TextUtils.isEmpty(mcc) || !TextUtils.isEmpty(mcc2)) {
            return mcc.equals("460") || mcc2.equals("460");
        }
        return SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
    }

    private boolean isSoftAp(LinkProperties lp) {
        if (lp == null) {
            log("LinkProperties is null, return");
            return false;
        }
        InetAddress currentGateway = null;
        for (RouteInfo route : lp.getRoutes()) {
            if (route.hasGateway()) {
                currentGateway = route.getGateway();
            }
        }
        if (currentGateway == null) {
            log("InetAddress getGateway is null, return");
            return false;
        }
        log(TAG, "currentGateway : " + currentGateway.toString());
        if (currentGateway.toString().contains("/192.168.43") || currentGateway.toString().equals("/192.168.49.1") || currentGateway.toString().equals("/172.20.10.1")) {
            return true;
        }
        return false;
    }

    private boolean hasKeepCelluarNetwork() {
        boolean hasCheck = true;
        if (Settings.System.getInt(this.mContext.getContentResolver(), KEEP_CELL_NETWORK_FOR_WIFI_ASSISTANT, 0) != 1) {
            hasCheck = false;
        }
        log("WLAN+ hasCheck = " + hasCheck);
        if (hasCheck) {
            Settings.System.putInt(this.mContext.getContentResolver(), KEEP_CELL_NETWORK_FOR_WIFI_ASSISTANT, 0);
        }
        return hasCheck;
    }

    public void startGatewayDetector(NetworkAgentInfo nai) {
        nai.startGatewayDetector(this.mGatewayDectectCallback);
    }

    public void notifyNetworkConnected(NetworkAgentInfo networkAgent) {
        networkAgent.networkMonitor().notifyNetworkConnected(networkAgent.linkProperties, networkAgent.networkCapabilities);
    }

    public void handleNetworkConnected(NetworkAgentInfo networkAgent) {
        if (networkAgent != null && networkAgent.networkInfo != null && networkAgent.networkInfo.isConnected()) {
            networkAgent.mGatewayState.startGatewayProbe();
            notifyNetworkConnected(networkAgent);
        }
    }

    public boolean needForceReevaluation(NetworkAgentInfo nai, int uid) {
        if (isWifiNetwork(nai)) {
            String[] pkgs = this.mContext.getPackageManager().getPackagesForUid(uid);
            String evaluationblacklist = this.mWifiRomUpdateHelper.getValue("EVALUATION_BLACKLIST", DEFAULT_EVALUATION_BLACKLIST);
            if (pkgs == null || pkgs.length < 1 || pkgs[0] == null || TextUtils.isEmpty(evaluationblacklist) || !pkgs[0].contains(evaluationblacklist)) {
                return true;
            }
            return false;
        }
        return true;
    }

    public void updateOppoSlaIface(NetworkAgentInfo nai, boolean up) {
        String slaDns;
        String slaDns2;
        String networkType;
        if (nai != null && nai.linkProperties != null && nai.networkInfo != null && nai.networkCapabilities != null) {
            String slaIpaddr = null;
            String ipMask = null;
            String slaIface = nai.linkProperties.getInterfaceName();
            LinkProperties slaNewLp = nai.linkProperties;
            Iterator<LinkAddress> it = slaNewLp.getLinkAddresses().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                LinkAddress linkAddr = it.next();
                InetAddress slaAddress = linkAddr.getAddress();
                if (linkAddr.getAddress() instanceof Inet4Address) {
                    slaIpaddr = slaAddress.getHostAddress();
                    ipMask = "" + linkAddr.getPrefixLength();
                    break;
                }
            }
            Iterator<InetAddress> it2 = slaNewLp.getDnsServers().iterator();
            while (true) {
                if (!it2.hasNext()) {
                    slaDns = null;
                    break;
                }
                InetAddress ia = it2.next();
                if (ia instanceof Inet4Address) {
                    slaDns = ia.getHostAddress();
                    break;
                }
            }
            if (TextUtils.isEmpty(slaIface) || TextUtils.isEmpty(slaIpaddr)) {
                slaDns2 = slaDns;
            } else if (TextUtils.isEmpty(slaDns)) {
                slaDns2 = slaDns;
            } else {
                String extraStr = nai.networkInfo.getExtraInfo();
                boolean isImsApn = extraStr == null ? false : "ims".equals(extraStr.toLowerCase());
                boolean isMms = nai.networkCapabilities.hasCapability(0);
                boolean isCellular = nai.networkCapabilities.hasTransport(0);
                boolean hasInternet = nai.networkCapabilities.hasCapability(12);
                if (nai.networkInfo.getType() == 1) {
                    if (WLAN0_IFACE_NAME.equals(slaIface)) {
                        if (this.mSlaWlan0Up != up) {
                            this.mSlaWlan0Up = up;
                            networkType = "0";
                        } else {
                            return;
                        }
                    } else if (!"wlan1".equals(slaIface)) {
                        loge("oppo_sla:setOppoSlaIfaceUp:invalid WiFi interface name=" + slaIface);
                        return;
                    } else if (this.mSlaWlan1Up != up) {
                        this.mSlaWlan1Up = up;
                        networkType = "1";
                    } else {
                        return;
                    }
                } else if (isCellular && hasInternet && !isImsApn && !isMms) {
                    if (!this.mSlaCellularUp || !up) {
                        this.mCellIfName = slaIface;
                        this.mSlaCellularUp = up;
                        sendCellConnChangedBroadcast(this.mSlaCellularUp);
                        if (up) {
                            this.mSlaTrafficRecord.startCellRecord();
                        } else {
                            this.mSlaTrafficRecord.stopCellRecord();
                        }
                        networkType = "2";
                    } else {
                        return;
                    }
                } else {
                    return;
                }
                if (up) {
                    try {
                        log("oppo_sla:setOppoSlaIfaceUp:" + slaIface);
                        try {
                            this.mNetd.setOppoSlaIfaceUp(networkType, slaIface, slaIpaddr, ipMask, slaDns);
                        } catch (Exception e) {
                            e = e;
                        }
                    } catch (Exception e2) {
                        e = e2;
                        loge("Exception setOppoSlaIfaceUp: " + slaIface + e);
                        updateSlaState();
                        return;
                    }
                } else {
                    try {
                        log("oppo_sla:setOppoSlaIfaceDown:" + slaIface);
                        this.mNetd.setOppoSlaIfaceDown(networkType, slaIface);
                    } catch (Exception e3) {
                        loge("Exception setOppoSlaIfaceDown: " + slaIface + e3);
                    }
                }
                updateSlaState();
                return;
            }
            log(TAG, "oppo_sla:setOppoSlaIfaceUp:warning...slaIface=" + slaIface + " slaIpaddr=" + slaIpaddr + " slaDns=" + slaDns2);
        }
    }

    private void updateSlaState() {
        log("oppo_sla:updateSlaState: mSlaWlan0Up=" + this.mSlaWlan0Up + " mSlaWlan1Up=" + this.mSlaWlan1Up + " mSlaCellularUp=" + this.mSlaCellularUp);
        if ((this.mSlaWlan0Up || this.mSlaWlan1Up) && this.mSlaCellularUp) {
            if (!this.mWifiRomUpdateHelper.getBooleanValue("NETWORK_SLA_ENABLED", true)) {
                log("oppo_sla:updateSlaState SLA disabled by xml.");
            }
            if (!(Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_sla_switch_on", 0) == 1)) {
                log("oppo_sla:updateSlaState SLA disabled by Settings.");
            }
        }
        if ((this.mSlaWlan0Up || this.mSlaWlan1Up) && this.mSlaCellularUp && !this.mSlaEnabled) {
            this.mSlaEnabled = true;
            this.mSlaTrafficRecord.startRecord();
        } else if (!this.mSlaEnabled) {
        } else {
            if ((!this.mSlaWlan0Up && !this.mSlaWlan1Up) || !this.mSlaCellularUp) {
                this.mSlaEnabled = false;
                this.mSlaTrafficRecord.stopRecord();
            }
        }
    }

    private void sendCellConnChangedBroadcast(boolean cellConnected) {
        Intent intent = new Intent("android.net.cell.CONNECTION_CHANGE");
        intent.putExtra("cell_connected", cellConnected);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public class SlaTrafficRecord {
        long beginCellRxBytes;
        long beginCellTxBytes;
        long beginRawCellRxBytes;
        long beginRawCellTxBytes;
        long beginWlanRxBytes;
        long beginWlanTxBytes;
        long endCellRxBytes;
        long endCellTxBytes;
        long endRawCellRxBytes;
        long endRawCellTxBytes;
        long endWlanRxBytes;
        long endWlanTxBytes;
        long rawCellRxBytes;
        long rawCellTxBytes;
        long totalCellRxBytes;
        long totalCellTxBytes;
        long totalWlanRxBytes;
        long totalWlanTxBytes;

        public SlaTrafficRecord() {
            String[] params = SystemProperties.get("persist.sys.sla.traffic", "").split("-");
            if (params != null && params.length == 6) {
                this.totalWlanRxBytes = Long.parseLong(params[0]);
                this.totalWlanTxBytes = Long.parseLong(params[1]);
                this.totalCellRxBytes = Long.parseLong(params[2]);
                this.totalCellTxBytes = Long.parseLong(params[3]);
                this.rawCellRxBytes = Long.parseLong(params[4]);
                this.rawCellTxBytes = Long.parseLong(params[5]);
            }
        }

        public void startRecord() {
            this.beginWlanRxBytes = TrafficStats.getRxBytes(OppoConnectivityServiceHelper.WLAN0_IFACE_NAME);
            this.beginWlanTxBytes = TrafficStats.getTxBytes(OppoConnectivityServiceHelper.WLAN0_IFACE_NAME);
            this.beginCellRxBytes = TrafficStats.getRxBytes(OppoConnectivityServiceHelper.this.mCellIfName);
            this.beginCellTxBytes = TrafficStats.getTxBytes(OppoConnectivityServiceHelper.this.mCellIfName);
        }

        public void stopRecord() {
            this.endWlanRxBytes = TrafficStats.getRxBytes(OppoConnectivityServiceHelper.WLAN0_IFACE_NAME);
            this.endWlanTxBytes = TrafficStats.getTxBytes(OppoConnectivityServiceHelper.WLAN0_IFACE_NAME);
            this.endCellRxBytes = TrafficStats.getRxBytes(OppoConnectivityServiceHelper.this.mCellIfName);
            this.endCellTxBytes = TrafficStats.getTxBytes(OppoConnectivityServiceHelper.this.mCellIfName);
            this.totalWlanRxBytes += this.endWlanRxBytes - this.beginWlanRxBytes;
            this.totalWlanTxBytes += this.endWlanTxBytes - this.beginWlanTxBytes;
            this.totalCellRxBytes += this.endCellRxBytes - this.beginCellRxBytes;
            this.totalCellTxBytes += this.endCellTxBytes - this.beginCellTxBytes;
            updateRecord();
        }

        public void startCellRecord() {
            this.beginRawCellRxBytes = TrafficStats.getRxBytes(OppoConnectivityServiceHelper.this.mCellIfName);
            this.beginRawCellTxBytes = TrafficStats.getTxBytes(OppoConnectivityServiceHelper.this.mCellIfName);
            OppoConnectivityServiceHelper.log("oppo_sla:startCellRecord beginRawCellRxBytes:" + this.beginRawCellRxBytes + " beginRawCellTxBytes:" + this.beginRawCellTxBytes);
        }

        public void stopCellRecord() {
            this.endRawCellRxBytes = TrafficStats.getRxBytes(OppoConnectivityServiceHelper.this.mCellIfName);
            this.endRawCellTxBytes = TrafficStats.getTxBytes(OppoConnectivityServiceHelper.this.mCellIfName);
            this.rawCellRxBytes += this.endRawCellRxBytes - this.beginRawCellRxBytes;
            this.rawCellTxBytes += this.endRawCellTxBytes - this.beginRawCellTxBytes;
            OppoConnectivityServiceHelper.log("oppo_sla:stopCellRecord endRawCellRxBytes:" + this.endRawCellRxBytes + " endRawCellTxBytes:" + this.endRawCellTxBytes + " rawCellRxBytes:" + this.rawCellRxBytes + " rawCellTxBytes:" + this.rawCellTxBytes);
            updateRecord();
        }

        private void updateRecord() {
            SystemProperties.set("persist.sys.sla.traffic", this.totalWlanRxBytes + "-" + this.totalWlanTxBytes + "-" + this.totalCellRxBytes + "-" + this.totalCellTxBytes + "-" + this.rawCellRxBytes + "-" + this.rawCellTxBytes);
        }

        public String toString() {
            return "Wlan_Rx " + ((this.totalWlanRxBytes / 1024) / 1024) + "    Wlan_Tx " + ((this.totalWlanTxBytes / 1024) / 1024) + "    Cell_Rx " + ((this.totalCellRxBytes / 1024) / 1024) + "    Cell_Tx " + ((this.totalCellTxBytes / 1024) / 1024) + "    Raw_Cell_Rx " + ((this.rawCellRxBytes / 1024) / 1024) + "    Raw_Cell_Tx " + ((this.rawCellTxBytes / 1024) / 1024);
        }
    }

    public void addDefaultDns(LinkProperties newLp, int netId, NetworkAgentInfo networkAgent) {
        if (networkAgent == null || networkAgent.networkInfo == null) {
            log("Invaild NetworkAgentInfo.");
            return;
        }
        Collection<InetAddress> dnses = newLp.getDnsServers();
        if (!(dnses == null || networkAgent.networkInfo == null)) {
            dnses = chooseDefaultDns(newLp, networkAgent);
            try {
                newLp.setDnsServers(dnses);
            } catch (Exception e) {
                log("setDnsServers:" + e);
            }
        }
        log(TAG, "Setting DNS servers for network " + netId + " to " + dnses);
    }

    public void handleNetworkTypeChange(NetworkAgentInfo oldDefaultNetwork, NetworkAgentInfo newNetwork) {
        if (this.hasWifiAssistant) {
            if (oldDefaultNetwork == null || oldDefaultNetwork.networkInfo == null || newNetwork.networkInfo == null) {
                if (oldDefaultNetwork == null && newNetwork.networkInfo != null && newNetwork.networkInfo.getType() == 1) {
                    if (!hasKeepCelluarNetwork()) {
                        this.mCs.releaseCelluarNetworkRequest();
                    }
                    sendNetworkChangeBroadcast(false, newNetwork.getCurrentScore());
                }
            } else if (oldDefaultNetwork.networkInfo.getType() == 1 && newNetwork.networkInfo.getType() == 0) {
                sendNetworkChangeBroadcast(true, oldDefaultNetwork.getCurrentScore());
            } else if (oldDefaultNetwork.networkInfo.getType() == 0 && newNetwork.networkInfo.getType() == 1) {
                this.mCs.releaseCelluarNetworkRequest();
                sendNetworkChangeBroadcast(false, newNetwork.getCurrentScore());
            }
        }
    }

    private void sendNetworkChangeBroadcast(boolean toData, int score) {
        Intent netIntent = new Intent(WIFI_NETWORK_CHANGE);
        netIntent.putExtra(EXTRA_WIFI_TO_DATA, toData);
        boolean wifiValid = false;
        if (score >= 20) {
            wifiValid = true;
        }
        netIntent.putExtra(EXTRA_WIFI_VALID, wifiValid);
        this.mContext.sendStickyBroadcastAsUser(netIntent, UserHandle.ALL);
    }

    private boolean isSwitchEnable() {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        boolean isSwitchEnable = true;
        if (Settings.Global.getInt(context.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) != 1) {
            isSwitchEnable = false;
        }
        return isSwitchEnable;
    }

    public boolean isWlanAssistantEnable() {
        int romUpdate = Settings.Global.getInt(this.mContext.getContentResolver(), "rom.update.wifi.assistant", 1);
        boolean wlanAssistantFeature = this.mContext.getPackageManager().hasSystemFeature("oppo.common_center.wlan.assistant");
        boolean romUpdateWlanAssistant = romUpdate == 1;
        if (!wlanAssistantFeature || !romUpdateWlanAssistant) {
            return false;
        }
        return true;
    }

    public boolean noNeedLinger(NetworkAgentInfo nai) {
        return this.hasWifiAssistant && isWifiNetwork(nai) && isSwitchEnable();
    }

    public void setIPv6DnsConfiguation(int netId) {
        boolean enableSimpleIPv6Query = false;
        int ipv6Retries = 2;
        int ipv6Timeout = 5;
        WifiRomUpdateHelper helper = WifiRomUpdateHelper.getInstance(this.mContext);
        if (helper != null) {
            enableSimpleIPv6Query = helper.getBooleanValue("NETWORK_IPV6_SIMEPLE_QUERY", false);
            ipv6Retries = helper.getIntegerValue("NETWORK_IPV6_RETRIES", 2).intValue();
            ipv6Timeout = helper.getIntegerValue("NETWORK_IPV6_TIMEOUT", 5).intValue();
        }
        if (isChineseOperator() && enableSimpleIPv6Query) {
            try {
                this.mNetd.setIPv6DnsConfiguation(netId, enableSimpleIPv6Query, ipv6Retries, ipv6Timeout);
            } catch (Exception e) {
                loge("Exception in setIPv6DnsConfiguationForNetwork: " + e);
            }
        }
    }

    private void notifyPrivateDnsEvaluationFailure() {
        log("notifyPrivateDnsEvaluationFailure");
        OppoPrivateDnsHelper.getInstance(this.mContext).showDialog();
        OppoPrivateDnsHelper.getInstance(this.mContext).oppoShowPrivateDnsNotification();
    }

    public void handleNotifyPrivateDnsStatus(int netId, boolean isFailure, NetworkAgentInfo nai, DnsManager dnsManager) {
        log("handle private dns status from NetworkMonitor, isFailure " + isFailure);
        if (isFailure) {
            Network activeNetwork = this.mCs.getActiveNetwork();
            if (activeNetwork != null && activeNetwork.netId == netId) {
                log("handlePrivateDnsEvaluationFailure, netId:" + netId);
                notifyPrivateDnsEvaluationFailure();
                dnsManager.updatePrivateDnsFailureStatus();
            }
        } else if (nai != null && this.mCs.isDefaultNetwork(nai)) {
            dnsManager.updatePrivateDnsValidatingStatus(netId, nai.linkProperties);
        }
    }

    public void clearPrivateDns(NetworkAgentInfo nai, DnsManager dnsManager) {
        if (nai != null && nai.linkProperties != null && nai.linkProperties.isPrivateDnsActive()) {
            log("disconnectAndDestroyNetwork updatePrivateDnsValidation false ");
            for (InetAddress inetAddress : nai.linkProperties.getValidatedPrivateDnsServers()) {
                dnsManager.updatePrivateDnsValidation(new DnsManager.PrivateDnsValidationUpdate(nai.network.netId, inetAddress, nai.linkProperties.getPrivateDnsServerName(), false));
            }
        }
    }

    public void notifyDefaultNetork(boolean defaultNetCell) {
        Intent intent = new Intent("android.net.wifi.DEFAULT_NETWORK_CHANGED");
        intent.putExtra("default_net_cell", defaultNetCell);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean enableWifiVerboseLogging() {
        if (TemperatureProvider.SWITCH_ON.equals(SystemProperties.get("persist.sys.wifipacketlog.state", TemperatureProvider.SWITCH_OFF))) {
            return true;
        }
        return false;
    }

    private void log(String TAG2, String s) {
        if (enableWifiVerboseLogging()) {
            Log.d(TAG2, s);
        }
    }

    /* access modifiers changed from: private */
    public static void log(String s) {
        Slog.d(TAG, s);
    }

    private static void loge(String s) {
        Slog.e(TAG, s);
    }
}
