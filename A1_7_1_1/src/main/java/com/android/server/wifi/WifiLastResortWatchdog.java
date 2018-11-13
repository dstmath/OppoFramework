package com.android.server.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;
import android.util.Pair;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
public class WifiLastResortWatchdog {
    public static final String BSSID_ANY = "any";
    private static final boolean DBG = true;
    public static final int FAILURE_CODE_ASSOCIATION = 1;
    public static final int FAILURE_CODE_AUTHENTICATION = 2;
    public static final int FAILURE_CODE_DHCP = 3;
    public static final int FAILURE_THRESHOLD = 7;
    public static final int MAX_BSSID_AGE = 10;
    private static final String TAG = "WifiLastResortWatchdog";
    private static boolean VDBG;
    private Map<String, AvailableNetworkFailureCount> mRecentAvailableNetworks;
    private Map<String, Pair<AvailableNetworkFailureCount, Integer>> mSsidFailureCount;
    private boolean mWatchdogAllowedToTrigger;
    private WifiController mWifiController;
    private boolean mWifiIsConnected;
    private WifiMetrics mWifiMetrics;

    public static class AvailableNetworkFailureCount {
        public int age = 0;
        public int associationRejection = 0;
        public int authenticationFailure = 0;
        public WifiConfiguration config;
        public int dhcpFailure = 0;
        public String ssid = "";

        AvailableNetworkFailureCount(WifiConfiguration configParam) {
            this.config = configParam;
        }

        public void incrementFailureCount(int reason) {
            switch (reason) {
                case 1:
                    this.associationRejection++;
                    return;
                case 2:
                    this.authenticationFailure++;
                    return;
                case 3:
                    this.dhcpFailure++;
                    return;
                default:
                    return;
            }
        }

        void resetCounts() {
            this.associationRejection = 0;
            this.authenticationFailure = 0;
            this.dhcpFailure = 0;
        }

        public String toString() {
            return this.ssid + ", HasEverConnected: " + (this.config != null ? Boolean.valueOf(this.config.getNetworkSelectionStatus().getHasEverConnected()) : "null_config") + ", Failures: {" + "Assoc: " + this.associationRejection + ", Auth: " + this.authenticationFailure + ", Dhcp: " + this.dhcpFailure + "}" + ", Age: " + this.age;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiLastResortWatchdog.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiLastResortWatchdog.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiLastResortWatchdog.<clinit>():void");
    }

    WifiLastResortWatchdog(WifiMetrics wifiMetrics) {
        this.mRecentAvailableNetworks = new HashMap();
        this.mSsidFailureCount = new HashMap();
        this.mWifiIsConnected = false;
        this.mWatchdogAllowedToTrigger = true;
        this.mWifiController = null;
        this.mWifiMetrics = wifiMetrics;
    }

    public void updateAvailableNetworks(List<Pair<ScanDetail, WifiConfiguration>> availableNetworks) {
        String ssid;
        if (VDBG) {
            Log.v(TAG, "updateAvailableNetworks: size = " + availableNetworks.size());
        }
        if (availableNetworks != null) {
            for (Pair<ScanDetail, WifiConfiguration> pair : availableNetworks) {
                ScanDetail scanDetail = pair.first;
                WifiConfiguration config = pair.second;
                ScanResult scanResult = scanDetail.getScanResult();
                if (scanResult != null) {
                    String bssid = scanResult.BSSID;
                    ssid = "\"" + scanDetail.getSSID() + "\"";
                    if (VDBG) {
                        Log.v(TAG, " " + bssid + ": " + scanDetail.getSSID());
                    }
                    AvailableNetworkFailureCount availableNetworkFailureCount = (AvailableNetworkFailureCount) this.mRecentAvailableNetworks.get(bssid);
                    if (availableNetworkFailureCount == null) {
                        availableNetworkFailureCount = new AvailableNetworkFailureCount(config);
                        availableNetworkFailureCount.ssid = ssid;
                        Pair<AvailableNetworkFailureCount, Integer> ssidFailsAndApCount = (Pair) this.mSsidFailureCount.get(ssid);
                        if (ssidFailsAndApCount == null) {
                            ssidFailsAndApCount = Pair.create(new AvailableNetworkFailureCount(config), Integer.valueOf(1));
                            setWatchdogTriggerEnabled(true);
                        } else {
                            ssidFailsAndApCount = Pair.create((AvailableNetworkFailureCount) ssidFailsAndApCount.first, Integer.valueOf(ssidFailsAndApCount.second.intValue() + 1));
                        }
                        this.mSsidFailureCount.put(ssid, ssidFailsAndApCount);
                    }
                    if (config != null) {
                        availableNetworkFailureCount.config = config;
                    }
                    availableNetworkFailureCount.age = -1;
                    this.mRecentAvailableNetworks.put(bssid, availableNetworkFailureCount);
                }
            }
        }
        Iterator<Entry<String, AvailableNetworkFailureCount>> it = this.mRecentAvailableNetworks.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, AvailableNetworkFailureCount> entry = (Entry) it.next();
            if (((AvailableNetworkFailureCount) entry.getValue()).age < 9) {
                AvailableNetworkFailureCount availableNetworkFailureCount2 = (AvailableNetworkFailureCount) entry.getValue();
                availableNetworkFailureCount2.age++;
            } else {
                ssid = ((AvailableNetworkFailureCount) entry.getValue()).ssid;
                Pair<AvailableNetworkFailureCount, Integer> ssidFails = (Pair) this.mSsidFailureCount.get(ssid);
                if (ssidFails != null) {
                    Integer apCount = Integer.valueOf(((Integer) ssidFails.second).intValue() - 1);
                    if (apCount.intValue() > 0) {
                        this.mSsidFailureCount.put(ssid, Pair.create((AvailableNetworkFailureCount) ssidFails.first, apCount));
                    } else {
                        this.mSsidFailureCount.remove(ssid);
                    }
                } else {
                    Log.d(TAG, "updateAvailableNetworks: SSID to AP count mismatch for " + ssid);
                }
                it.remove();
            }
        }
        if (VDBG) {
            Log.v(TAG, toString());
        }
    }

    public boolean noteConnectionFailureAndTriggerIfNeeded(String ssid, String bssid, int reason) {
        if (VDBG) {
            Log.v(TAG, "noteConnectionFailureAndTriggerIfNeeded: [" + ssid + ", " + bssid + ", " + reason + "]");
        }
        updateFailureCountForNetwork(ssid, bssid, reason);
        boolean isRestartNeeded = checkTriggerCondition();
        if (VDBG) {
            Log.v(TAG, "isRestartNeeded = " + isRestartNeeded);
        }
        if (isRestartNeeded) {
            setWatchdogTriggerEnabled(false);
            restartWifiStack();
            incrementWifiMetricsTriggerCounts();
            clearAllFailureCounts();
        }
        return isRestartNeeded;
    }

    public void connectedStateTransition(boolean isEntering) {
        if (VDBG) {
            Log.v(TAG, "connectedStateTransition: isEntering = " + isEntering);
        }
        this.mWifiIsConnected = isEntering;
        if (!this.mWatchdogAllowedToTrigger) {
            this.mWifiMetrics.incrementNumLastResortWatchdogSuccesses();
        }
        if (isEntering) {
            clearAllFailureCounts();
            setWatchdogTriggerEnabled(true);
        }
    }

    private void updateFailureCountForNetwork(String ssid, String bssid, int reason) {
        if (VDBG) {
            Log.v(TAG, "updateFailureCountForNetwork: [" + ssid + ", " + bssid + ", " + reason + "]");
        }
        if (BSSID_ANY.equals(bssid)) {
            incrementSsidFailureCount(ssid, reason);
        } else {
            incrementBssidFailureCount(ssid, bssid, reason);
        }
    }

    private void incrementSsidFailureCount(String ssid, int reason) {
        Pair<AvailableNetworkFailureCount, Integer> ssidFails = (Pair) this.mSsidFailureCount.get(ssid);
        if (ssidFails == null) {
            Log.v(TAG, "updateFailureCountForNetwork: No networks for ssid = " + ssid);
        } else {
            ssidFails.first.incrementFailureCount(reason);
        }
    }

    private void incrementBssidFailureCount(String ssid, String bssid, int reason) {
        AvailableNetworkFailureCount availableNetworkFailureCount = (AvailableNetworkFailureCount) this.mRecentAvailableNetworks.get(bssid);
        if (availableNetworkFailureCount == null) {
            Log.d(TAG, "updateFailureCountForNetwork: Unable to find Network [" + ssid + ", " + bssid + "]");
        } else if (availableNetworkFailureCount.ssid.equals(ssid)) {
            if (availableNetworkFailureCount.config == null && VDBG) {
                Log.v(TAG, "updateFailureCountForNetwork: network has no config [" + ssid + ", " + bssid + "]");
            }
            availableNetworkFailureCount.incrementFailureCount(reason);
            incrementSsidFailureCount(ssid, reason);
        } else {
            Log.d(TAG, "updateFailureCountForNetwork: Failed connection attempt has wrong ssid. Failed [" + ssid + ", " + bssid + "], buffered [" + availableNetworkFailureCount.ssid + ", " + bssid + "]");
        }
    }

    private boolean checkTriggerCondition() {
        if (VDBG) {
            Log.v(TAG, "checkTriggerCondition.");
        }
        if (this.mWifiIsConnected || !this.mWatchdogAllowedToTrigger) {
            return false;
        }
        boolean atleastOneNetworkHasEverConnected = false;
        for (Entry<String, AvailableNetworkFailureCount> entry : this.mRecentAvailableNetworks.entrySet()) {
            if (((AvailableNetworkFailureCount) entry.getValue()).config != null && ((AvailableNetworkFailureCount) entry.getValue()).config.getNetworkSelectionStatus().getHasEverConnected()) {
                atleastOneNetworkHasEverConnected = true;
            }
            if (!isOverFailureThreshold((String) entry.getKey())) {
                return false;
            }
        }
        if (VDBG) {
            Log.v(TAG, "checkTriggerCondition: return = " + atleastOneNetworkHasEverConnected);
        }
        return atleastOneNetworkHasEverConnected;
    }

    private void restartWifiStack() {
        if (VDBG) {
            Log.v(TAG, "restartWifiStack.");
        }
        if (this.mWifiController == null) {
            Log.e(TAG, "WifiLastResortWatchdog unable to trigger: WifiController is null");
            return;
        }
        Log.d(TAG, toString());
        this.mWifiController.sendMessage(155665);
        Log.i(TAG, "Triggered WiFi stack restart.");
    }

    private void incrementWifiMetricsTriggerCounts() {
        if (VDBG) {
            Log.v(TAG, "incrementWifiMetricsTriggerCounts.");
        }
        this.mWifiMetrics.incrementNumLastResortWatchdogTriggers();
        this.mWifiMetrics.addCountToNumLastResortWatchdogAvailableNetworksTotal(this.mSsidFailureCount.size());
        int badAuth = 0;
        int badAssoc = 0;
        int badDhcp = 0;
        for (Entry<String, Pair<AvailableNetworkFailureCount, Integer>> entry : this.mSsidFailureCount.entrySet()) {
            int i;
            if (((AvailableNetworkFailureCount) ((Pair) entry.getValue()).first).authenticationFailure >= 7) {
                i = 1;
            } else {
                i = 0;
            }
            badAuth += i;
            if (((AvailableNetworkFailureCount) ((Pair) entry.getValue()).first).associationRejection >= 7) {
                i = 1;
            } else {
                i = 0;
            }
            badAssoc += i;
            if (((AvailableNetworkFailureCount) ((Pair) entry.getValue()).first).dhcpFailure >= 7) {
                i = 1;
            } else {
                i = 0;
            }
            badDhcp += i;
        }
        if (badAuth > 0) {
            this.mWifiMetrics.addCountToNumLastResortWatchdogBadAuthenticationNetworksTotal(badAuth);
            this.mWifiMetrics.incrementNumLastResortWatchdogTriggersWithBadAuthentication();
        }
        if (badAssoc > 0) {
            this.mWifiMetrics.addCountToNumLastResortWatchdogBadAssociationNetworksTotal(badAssoc);
            this.mWifiMetrics.incrementNumLastResortWatchdogTriggersWithBadAssociation();
        }
        if (badDhcp > 0) {
            this.mWifiMetrics.addCountToNumLastResortWatchdogBadDhcpNetworksTotal(badDhcp);
            this.mWifiMetrics.incrementNumLastResortWatchdogTriggersWithBadDhcp();
        }
    }

    private void clearAllFailureCounts() {
        if (VDBG) {
            Log.v(TAG, "clearAllFailureCounts.");
        }
        for (Entry<String, AvailableNetworkFailureCount> entry : this.mRecentAvailableNetworks.entrySet()) {
            AvailableNetworkFailureCount failureCount = (AvailableNetworkFailureCount) entry.getValue();
            ((AvailableNetworkFailureCount) entry.getValue()).resetCounts();
        }
        for (Entry<String, Pair<AvailableNetworkFailureCount, Integer>> entry2 : this.mSsidFailureCount.entrySet()) {
            ((AvailableNetworkFailureCount) ((Pair) entry2.getValue()).first).resetCounts();
        }
    }

    Map<String, AvailableNetworkFailureCount> getRecentAvailableNetworks() {
        return this.mRecentAvailableNetworks;
    }

    private void setWatchdogTriggerEnabled(boolean enable) {
        if (VDBG) {
            Log.v(TAG, "setWatchdogTriggerEnabled: enable = " + enable);
        }
        this.mWatchdogAllowedToTrigger = enable;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mWatchdogAllowedToTrigger: ").append(this.mWatchdogAllowedToTrigger);
        sb.append("\nmWifiIsConnected: ").append(this.mWifiIsConnected);
        sb.append("\nmRecentAvailableNetworks: ").append(this.mRecentAvailableNetworks.size());
        for (Entry<String, AvailableNetworkFailureCount> entry : this.mRecentAvailableNetworks.entrySet()) {
            sb.append("\n ").append((String) entry.getKey()).append(": ").append(entry.getValue());
        }
        sb.append("\nmSsidFailureCount:");
        for (Entry<String, Pair<AvailableNetworkFailureCount, Integer>> entry2 : this.mSsidFailureCount.entrySet()) {
            Integer apCount = ((Pair) entry2.getValue()).second;
            sb.append("\n").append((String) entry2.getKey()).append(": ").append(apCount).append(", ").append(((Pair) entry2.getValue()).first.toString());
        }
        return sb.toString();
    }

    public boolean isOverFailureThreshold(String bssid) {
        if (getFailureCount(bssid, 1) >= 7 || getFailureCount(bssid, 2) >= 7 || getFailureCount(bssid, 3) >= 7) {
            return true;
        }
        return false;
    }

    public int getFailureCount(String bssid, int reason) {
        AvailableNetworkFailureCount availableNetworkFailureCount = (AvailableNetworkFailureCount) this.mRecentAvailableNetworks.get(bssid);
        if (availableNetworkFailureCount == null) {
            return 0;
        }
        String ssid = availableNetworkFailureCount.ssid;
        Pair<AvailableNetworkFailureCount, Integer> ssidFails = (Pair) this.mSsidFailureCount.get(ssid);
        if (ssidFails == null) {
            Log.d(TAG, "getFailureCount: Could not find SSID count for " + ssid);
            return 0;
        }
        AvailableNetworkFailureCount failCount = ssidFails.first;
        switch (reason) {
            case 1:
                return failCount.associationRejection;
            case 2:
                return failCount.authenticationFailure;
            case 3:
                return failCount.dhcpFailure;
            default:
                return 0;
        }
    }

    public void setWifiController(WifiController wifiController) {
        this.mWifiController = wifiController;
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            VDBG = true;
        } else {
            VDBG = false;
        }
    }
}
