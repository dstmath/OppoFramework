package com.android.server.wifi;

import android.net.NetworkInfo.DetailedState;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiSsid;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pProvDiscEvent;
import android.net.wifi.p2p.nsd.WifiP2pServiceResponse;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.wifi.WifiStateMachine.SimAuthRequestData;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.p2p.WifiP2pServiceImpl.P2pStatus;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class WifiMonitor {
    private static final String ADDR_STRING = "addr=";
    public static final int ANQP_DONE_EVENT = 147500;
    private static final String ANQP_DONE_STR = "ANQP-QUERY-DONE";
    public static final int AP_STA_CONNECTED_EVENT = 147498;
    private static final String AP_STA_CONNECTED_STR = "AP-STA-CONNECTED";
    public static final int AP_STA_DISCONNECTED_EVENT = 147497;
    private static final String AP_STA_DISCONNECTED_STR = "AP-STA-DISCONNECTED";
    private static final String ASSOCIATED_WITH_STR = "Associated with ";
    public static final int ASSOCIATION_REJECTION_EVENT = 147499;
    private static final int ASSOC_REJECT = 9;
    private static final String ASSOC_REJECT_STR = "ASSOC-REJECT";
    public static final int AUTHENTICATION_FAILURE_EVENT = 147463;
    private static final String AUTHENTICATION_TIMEOUT_PREFIX_STR = "Authentication with";
    private static final String AUTHENTICATION_TIMEOUT_STR = "timed out";
    private static final String AUTH_EVENT_PREFIX_STR = "Authentication with";
    public static final int AUTH_FAIL_REASON_PWD_ERROR = 1;
    private static final String AUTH_TIMEOUT_STR = "timed out.";
    private static final int BASE = 147456;
    private static final int BSS_ADDED = 12;
    private static final String BSS_ADDED_STR = "BSS-ADDED";
    private static final int BSS_REMOVED = 13;
    private static final String BSS_REMOVED_STR = "BSS-REMOVED";
    private static final int CONFIG_AUTH_FAILURE = 18;
    private static final int CONFIG_MULTIPLE_PBC_DETECTED = 12;
    private static final int CONNECTED = 1;
    private static final String CONNECTED_STR = "CONNECTED";
    private static final String ConnectPrefix = "Connection to ";
    private static final String ConnectSuffix = " completed";
    private static boolean DBG = false;
    private static final int DISCONNECTED = 2;
    private static final String DISCONNECTED_STR = "DISCONNECTED";
    public static final int DRIVER_HUNG_EVENT = 147468;
    private static final int DRIVER_STATE = 7;
    private static final String DRIVER_STATE_STR = "DRIVER-STATE";
    private static final String EAP_AUTH_FAILURE_STR = "EAP authentication failed";
    private static final int EAP_FAILURE = 8;
    private static final String EAP_FAILURE_STR = "EAP-FAILURE";
    private static final String EAP_FAST_NEW_PAC_UPDATED = "EAP-FAST-NEW-PAC-UPDATED";
    private static final int EVENT_PREFIX_LEN_STR = 0;
    private static final String EVENT_PREFIX_STR = "CTRL-EVENT-";
    public static final int GAS_QUERY_DONE_EVENT = 147508;
    private static final String GAS_QUERY_DONE_STR = "GAS-QUERY-DONE";
    private static final String GAS_QUERY_PREFIX_STR = "GAS-QUERY-";
    public static final int GAS_QUERY_START_EVENT = 147507;
    private static final String GAS_QUERY_START_STR = "GAS-QUERY-START";
    private static final String HOST_AP_EVENT_PREFIX_STR = "AP";
    public static final String HS20_DEAUTH_STR = "HS20-DEAUTH-IMMINENT-NOTICE";
    private static final String HS20_ICON_STR = "RX-HS20-ICON";
    private static final String HS20_PREFIX_STR = "HS20-";
    public static final int HS20_REMEDIATION_EVENT = 147517;
    public static final String HS20_SUB_REM_STR = "HS20-SUBSCRIPTION-REMEDIATION";
    private static final String IDENTITY_STR = "IDENTITY";
    private static final int LINK_SPEED = 5;
    private static final String LINK_SPEED_STR = "LINK-SPEED";
    private static final int MAX_RECV_ERRORS = 10;
    public static final int NETWORK_CONNECTION_EVENT = 147459;
    public static final int NETWORK_DISCONNECTION_EVENT = 147460;
    public static final int NEW_PAC_UPDATED_EVENT = 147537;
    private static final int NO_CERTIFICATION = 16;
    public static final int P2P_DEVICE_FOUND_EVENT = 147477;
    private static final String P2P_DEVICE_FOUND_STR = "P2P-DEVICE-FOUND";
    public static final int P2P_DEVICE_LOST_EVENT = 147478;
    private static final String P2P_DEVICE_LOST_STR = "P2P-DEVICE-LOST";
    private static final String P2P_EVENT_PREFIX_STR = "P2P";
    public static final int P2P_FIND_STOPPED_EVENT = 147493;
    private static final String P2P_FIND_STOPPED_STR = "P2P-FIND-STOPPED";
    public static final int P2P_GO_NEGOTIATION_FAILURE_EVENT = 147482;
    public static final int P2P_GO_NEGOTIATION_REQUEST_EVENT = 147479;
    public static final int P2P_GO_NEGOTIATION_SUCCESS_EVENT = 147481;
    private static final String P2P_GO_NEG_FAILURE_STR = "P2P-GO-NEG-FAILURE";
    private static final String P2P_GO_NEG_REQUEST_STR = "P2P-GO-NEG-REQUEST";
    private static final String P2P_GO_NEG_SUCCESS_STR = "P2P-GO-NEG-SUCCESS";
    public static final int P2P_GROUP_FORMATION_FAILURE_EVENT = 147484;
    private static final String P2P_GROUP_FORMATION_FAILURE_STR = "P2P-GROUP-FORMATION-FAILURE";
    public static final int P2P_GROUP_FORMATION_SUCCESS_EVENT = 147483;
    private static final String P2P_GROUP_FORMATION_SUCCESS_STR = "P2P-GROUP-FORMATION-SUCCESS";
    public static final int P2P_GROUP_REMOVED_EVENT = 147486;
    private static final String P2P_GROUP_REMOVED_STR = "P2P-GROUP-REMOVED";
    public static final int P2P_GROUP_STARTED_EVENT = 147485;
    private static final String P2P_GROUP_STARTED_STR = "P2P-GROUP-STARTED";
    public static final int P2P_INVITATION_RECEIVED_EVENT = 147487;
    private static final String P2P_INVITATION_RECEIVED_STR = "P2P-INVITATION-RECEIVED";
    public static final int P2P_INVITATION_RESULT_EVENT = 147488;
    private static final String P2P_INVITATION_RESULT_STR = "P2P-INVITATION-RESULT";
    public static final int P2P_PEER_DISCONNECT_EVENT = 147496;
    public static final int P2P_PROV_DISC_ENTER_PIN_EVENT = 147491;
    private static final String P2P_PROV_DISC_ENTER_PIN_STR = "P2P-PROV-DISC-ENTER-PIN";
    public static final int P2P_PROV_DISC_FAILURE_EVENT = 147495;
    private static final String P2P_PROV_DISC_FAILURE_STR = "P2P-PROV-DISC-FAILURE";
    public static final int P2P_PROV_DISC_PBC_REQ_EVENT = 147489;
    private static final String P2P_PROV_DISC_PBC_REQ_STR = "P2P-PROV-DISC-PBC-REQ";
    public static final int P2P_PROV_DISC_PBC_RSP_EVENT = 147490;
    private static final String P2P_PROV_DISC_PBC_RSP_STR = "P2P-PROV-DISC-PBC-RESP";
    public static final int P2P_PROV_DISC_SHOW_PIN_EVENT = 147492;
    private static final String P2P_PROV_DISC_SHOW_PIN_STR = "P2P-PROV-DISC-SHOW-PIN";
    public static final int P2P_SERV_DISC_RESP_EVENT = 147494;
    private static final String P2P_SERV_DISC_RESP_STR = "P2P-SERV-DISC-RESP";
    private static final String PASSWORD_MAY_BE_INCORRECT_STR = "pre-shared key may be incorrect";
    private static final int REASON_TKIP_ONLY_PROHIBITED = 1;
    private static final int REASON_WEP_PROHIBITED = 2;
    private static final String REENABLED_STR = "SSID-REENABLED";
    private static final int REQUEST_PREFIX_LEN_STR = 0;
    private static final String REQUEST_PREFIX_STR = "CTRL-REQ-";
    private static final String RESULT_STRING = "result=";
    public static final int RX_HS20_ANQP_ICON_EVENT = 147509;
    private static final String RX_HS20_ANQP_ICON_STR = "RX-HS20-ANQP-ICON";
    private static final int RX_HS20_ANQP_ICON_STR_LEN = 0;
    private static final int SCAN_FAILED = 15;
    public static final int SCAN_FAILED_EVENT = 147473;
    private static final String SCAN_FAILED_STR = "SCAN-FAILED";
    private static final int SCAN_RESULTS = 4;
    public static final int SCAN_RESULTS_EVENT = 147461;
    private static final String SCAN_RESULTS_STR = "SCAN-RESULTS";
    private static final int SELECT_NETWORK = 147658;
    public static final int SELECT_NETWORK_EVENT = 147649;
    private static final String SELECT_NETWORK_STR = "SELECT-NETWORK";
    private static final String SIM_STR = "SIM";
    private static final int SSID_REENABLE = 11;
    public static final int SSID_REENABLED = 147470;
    private static final int SSID_TEMP_DISABLE = 10;
    public static final int SSID_TEMP_DISABLED = 147469;
    private static final int STATE_CHANGE = 3;
    private static final String STATE_CHANGE_STR = "STATE-CHANGE";
    public static final int SUPPLICANT_STATE_CHANGE_EVENT = 147462;
    public static final int SUP_CONNECTION_EVENT = 147457;
    public static final int SUP_DISCONNECTION_EVENT = 147458;
    public static final int SUP_REQUEST_IDENTITY = 147471;
    public static final int SUP_REQUEST_SIM_AUTH = 147472;
    private static final String TAG = "WifiMonitor";
    private static final String TARGET_BSSID_STR = "Trying to associate with ";
    public static final int TDLS_CONNECTED_EVENT = 147539;
    private static final String TDLS_CONNECTED_EVENT_STR = "TDLS-PEER-CONNECTED";
    public static final int TDLS_DISCONNECTED_EVENT = 147540;
    private static final String TDLS_DISCONNECTED_EVENT_STR = "TDLS-PEER-DISCONNECTED";
    private static final String TEMP_DISABLED_STR = "SSID-TEMP-DISABLED";
    private static final int TERMINATING = 6;
    private static final String TERMINATING_STR = "TERMINATING";
    private static final int UNKNOWN = 14;
    private static final boolean VDBG = false;
    public static final int WAPI_NO_CERTIFICATION_EVENT = 147536;
    private static final String WAPI_NO_CERTIFICATION_STRING = "EAP-NO-CERTIFICATION";
    public static final int WHOLE_CHIP_RESET_FAIL_EVENT = 147538;
    private static final String WHOLE_CHIP_RESET_FAIL_STRING = "WHOLE-CHIP-RESET-FAIL";
    private static final String WPA_EVENT_PREFIX_STR = "WPA:";
    private static final String WPA_RECV_ERROR_STR = "recv error";
    private static final String WPS_ER_AP_ADD_STR = "WPS-ER-AP-ADD";
    private static final String WPS_ER_ENROLLEE_ADD_STR = "WPS-ER-ENROLLEE-ADD-PBC";
    public static final int WPS_FAIL_EVENT = 147465;
    private static final String WPS_FAIL_PATTERN = "WPS-FAIL msg=\\d+(?: config_error=(\\d+))?(?: reason=(\\d+))?";
    private static final String WPS_FAIL_STR = "WPS-FAIL";
    public static final int WPS_OVERLAP_EVENT = 147466;
    private static final String WPS_OVERLAP_STR = "WPS-OVERLAP-DETECTED";
    public static final int WPS_SUCCESS_EVENT = 147464;
    private static final String WPS_SUCCESS_STR = "WPS-SUCCESS";
    public static final int WPS_TIMEOUT_EVENT = 147467;
    private static final String WPS_TIMEOUT_STR = "WPS-TIMEOUT";
    public static final int WRONG_KEY_EVENT = 147648;
    private static int eventLogCounter;
    private static Pattern mAssocRejectEventPattern;
    private static Pattern mAssociatedPattern;
    private static Pattern mConnectedEventPattern;
    private static Pattern mDisconnectedEventPattern;
    private static Pattern mRequestGsmAuthPattern;
    private static Pattern mRequestIdentityPattern;
    private static Pattern mRequestUmtsAuthPattern;
    private static Pattern mTargetBSSIDPattern;
    private static final Pattern p2pErrorExPattern1 = null;
    private static final Pattern p2pErrorExPattern2 = null;
    private static final Pattern p2pErrorExPattern3 = null;
    private static int sP2pOperFreq;
    private static WifiMonitor sWifiMonitor;
    private boolean mConnected;
    private final Map<String, SparseArray<Set<Handler>>> mHandlerMap;
    private Map<String, Long> mLastConnectBSSIDs;
    private final Map<String, Boolean> mMonitoringMap;
    private int mRecvErrors;
    private final WifiNative mWifiNative;

    private class MonitorThread extends Thread {
        private final LocalLog mLocalLog;

        public MonitorThread(LocalLog localLog) {
            super(WifiMonitor.TAG);
            this.mLocalLog = localLog;
        }

        public void run() {
            if (WifiMonitor.DBG) {
                Log.d(WifiMonitor.TAG, "MonitorThread start with mConnected=" + WifiMonitor.this.mConnected);
            }
            while (WifiMonitor.this.mConnected) {
                String eventStr = WifiMonitor.this.mWifiNative.waitForEvent();
                if (!(eventStr.contains(WifiMonitor.BSS_ADDED_STR) || eventStr.contains(WifiMonitor.BSS_REMOVED_STR))) {
                    if (WifiMonitor.DBG) {
                        Log.d(WifiMonitor.TAG, "Event [" + eventStr + "]");
                    }
                    this.mLocalLog.log("Event [" + eventStr + "]");
                }
                if (WifiMonitor.this.dispatchEvent(eventStr)) {
                    if (WifiMonitor.DBG) {
                        Log.d(WifiMonitor.TAG, "Disconnecting from the supplicant, no more events");
                        return;
                    }
                    return;
                }
            }
            if (WifiMonitor.DBG) {
                Log.d(WifiMonitor.TAG, "MonitorThread exit because mConnected is false");
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.wifi.WifiMonitor.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.wifi.WifiMonitor.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiMonitor.<clinit>():void");
    }

    public static WifiMonitor getInstance() {
        return sWifiMonitor;
    }

    private WifiMonitor() {
        this.mRecvErrors = 0;
        this.mConnected = false;
        this.mHandlerMap = new HashMap();
        this.mMonitoringMap = new HashMap();
        this.mLastConnectBSSIDs = new HashMap<String, Long>() {
            public Long get(String iface) {
                Long value = (Long) super.get(iface);
                if (value != null) {
                    return value;
                }
                return Long.valueOf(0);
            }
        };
        this.mWifiNative = WifiNative.getWlanNativeInterface();
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
    }

    public synchronized void registerHandler(String iface, int what, Handler handler) {
        SparseArray<Set<Handler>> ifaceHandlers = (SparseArray) this.mHandlerMap.get(iface);
        if (ifaceHandlers == null) {
            ifaceHandlers = new SparseArray();
            this.mHandlerMap.put(iface, ifaceHandlers);
        }
        Set<Handler> ifaceWhatHandlers = (Set) ifaceHandlers.get(what);
        if (ifaceWhatHandlers == null) {
            ifaceWhatHandlers = new ArraySet();
            ifaceHandlers.put(what, ifaceWhatHandlers);
        }
        ifaceWhatHandlers.add(handler);
    }

    private boolean isMonitoring(String iface) {
        Boolean val = (Boolean) this.mMonitoringMap.get(iface);
        if (val == null) {
            return false;
        }
        return val.booleanValue();
    }

    private void setMonitoring(String iface, boolean enabled) {
        this.mMonitoringMap.put(iface, Boolean.valueOf(enabled));
    }

    private void setMonitoringNone() {
        for (String iface : this.mMonitoringMap.keySet()) {
            setMonitoring(iface, false);
        }
    }

    private boolean ensureConnectedLocked() {
        if (this.mConnected) {
            return true;
        }
        if (DBG) {
            Log.d(TAG, "connecting to supplicant");
        }
        int connectTries = 0;
        while (!this.mWifiNative.connectToSupplicant()) {
            int connectTries2 = connectTries + 1;
            if (connectTries >= 5) {
                return false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            connectTries = connectTries2;
        }
        this.mConnected = true;
        new MonitorThread(this.mWifiNative.getLocalLog()).start();
        return true;
    }

    public synchronized void startMonitoring(String iface) {
        Log.d(TAG, "startMonitoring(" + iface + ") with mConnected = " + this.mConnected);
        if (ensureConnectedLocked()) {
            setMonitoring(iface, true);
            sendMessage(iface, (int) SUP_CONNECTION_EVENT);
        } else {
            boolean originalMonitoring = isMonitoring(iface);
            setMonitoring(iface, true);
            sendMessage(iface, (int) SUP_DISCONNECTION_EVENT);
            setMonitoring(iface, originalMonitoring);
            Log.e(TAG, "startMonitoring(" + iface + ") failed!");
        }
    }

    public synchronized void stopMonitoring(String iface) {
        if (DBG) {
            Log.d(TAG, "stopMonitoring(" + iface + ")");
        }
        setMonitoring(iface, true);
        sendMessage(iface, (int) SUP_DISCONNECTION_EVENT);
        setMonitoring(iface, false);
    }

    public synchronized void stopSupplicant() {
        this.mWifiNative.stopSupplicant();
    }

    public synchronized void killSupplicant(boolean p2pSupported) {
        String suppState = System.getProperty("init.svc.wpa_supplicant");
        if (suppState == null) {
            suppState = "unknown";
        }
        String p2pSuppState = System.getProperty("init.svc.p2p_supplicant");
        if (p2pSuppState == null) {
            p2pSuppState = "unknown";
        }
        Log.e(TAG, "killSupplicant p2p" + p2pSupported + " init.svc.wpa_supplicant=" + suppState + " init.svc.p2p_supplicant=" + p2pSuppState);
        this.mWifiNative.killSupplicant(p2pSupported);
        this.mConnected = false;
        setMonitoringNone();
    }

    private void sendMessage(String iface, int what) {
        sendMessage(iface, Message.obtain(null, what));
    }

    private void sendMessage(String iface, int what, Object obj) {
        sendMessage(iface, Message.obtain(null, what, obj));
    }

    private void sendMessage(String iface, int what, int arg1) {
        sendMessage(iface, Message.obtain(null, what, arg1, 0));
    }

    private void sendMessage(String iface, int what, int arg1, int arg2) {
        sendMessage(iface, Message.obtain(null, what, arg1, arg2));
    }

    private void sendMessage(String iface, int what, int arg1, int arg2, Object obj) {
        sendMessage(iface, Message.obtain(null, what, arg1, arg2, obj));
    }

    private void sendMessage(String iface, Message message) {
        SparseArray<Set<Handler>> ifaceHandlers = (SparseArray) this.mHandlerMap.get(iface);
        boolean firstHandler;
        if (iface == null || ifaceHandlers == null) {
            if (DBG) {
                Log.d(TAG, "Sending to all monitors because there's no matching iface");
            }
            firstHandler = true;
            for (Entry<String, SparseArray<Set<Handler>>> entry : this.mHandlerMap.entrySet()) {
                if (isMonitoring((String) entry.getKey())) {
                    for (Handler handler : (Set) ((SparseArray) entry.getValue()).get(message.what)) {
                        if (firstHandler) {
                            firstHandler = false;
                            sendMessage(handler, message);
                        } else {
                            sendMessage(handler, Message.obtain(message));
                        }
                    }
                }
            }
        } else if (isMonitoring(iface)) {
            firstHandler = true;
            Set<Handler> ifaceWhatHandlers = (Set) ifaceHandlers.get(message.what);
            if (ifaceWhatHandlers != null) {
                for (Handler handler2 : ifaceWhatHandlers) {
                    if (firstHandler) {
                        firstHandler = false;
                        sendMessage(handler2, message);
                    } else {
                        sendMessage(handler2, Message.obtain(message));
                    }
                }
            }
        } else if (DBG) {
            Log.d(TAG, "Dropping event because (" + iface + ") is stopped");
        }
    }

    private void sendMessage(Handler handler, Message message) {
        if (handler != null) {
            message.setTarget(handler);
            message.sendToTarget();
        }
    }

    private synchronized boolean dispatchEvent(String eventStr) {
        String iface;
        if (eventStr.startsWith("IFNAME=")) {
            int space = eventStr.indexOf(32);
            if (space != -1) {
                iface = eventStr.substring(7, space);
                if (!this.mHandlerMap.containsKey(iface) && iface.startsWith("p2p-")) {
                    iface = "p2p0";
                }
                eventStr = eventStr.substring(space + 1);
            } else {
                Log.e(TAG, "Dropping malformed event (unparsable iface): " + eventStr);
                return false;
            }
        }
        iface = "p2p0";
        if (!dispatchEvent(eventStr, iface)) {
            return false;
        }
        this.mConnected = false;
        return true;
    }

    private boolean dispatchEvent(String eventStr, String iface) {
        if (DBG && eventStr != null) {
            if (!eventStr.contains("CTRL-EVENT-BSS-ADDED")) {
                Log.d(TAG, iface + " cnt=" + Integer.toString(eventLogCounter) + " dispatchEvent: " + eventStr);
            }
        }
        if (eventStr.startsWith(EVENT_PREFIX_STR)) {
            String eventName = eventStr.substring(EVENT_PREFIX_LEN_STR);
            int nameEnd = eventName.indexOf(32);
            if (nameEnd != -1) {
                eventName = eventName.substring(0, nameEnd);
            }
            if (eventName.length() == 0) {
                if (DBG) {
                    Log.i(TAG, "Received wpa_supplicant event with empty event name");
                }
                eventLogCounter++;
                return false;
            }
            int event;
            int ind;
            if (eventName.equals(CONNECTED_STR)) {
                event = 1;
                long bssid = -1;
                int prefix = eventStr.indexOf(ConnectPrefix);
                if (prefix >= 0) {
                    int suffix = eventStr.indexOf(ConnectSuffix);
                    if (suffix > prefix) {
                        try {
                            bssid = Utils.parseMac(eventStr.substring(ConnectPrefix.length() + prefix, suffix));
                        } catch (IllegalArgumentException e) {
                            bssid = -1;
                        }
                    }
                }
                this.mLastConnectBSSIDs.put(iface, Long.valueOf(bssid));
                if (bssid == -1) {
                    Log.w(TAG, "Failed to parse out BSSID from '" + eventStr + "'");
                }
            } else {
                if (eventName.equals(DISCONNECTED_STR)) {
                    event = 2;
                    handleP2pEvents(eventStr, iface);
                } else {
                    if (eventName.equals(STATE_CHANGE_STR)) {
                        event = 3;
                    } else {
                        if (eventName.equals(SCAN_RESULTS_STR)) {
                            event = 4;
                        } else {
                            if (eventName.equals(SCAN_FAILED_STR)) {
                                event = 15;
                            } else {
                                if (eventName.equals(LINK_SPEED_STR)) {
                                    event = 5;
                                } else {
                                    if (eventName.equals(TERMINATING_STR)) {
                                        event = 6;
                                    } else {
                                        if (eventName.equals(DRIVER_STATE_STR)) {
                                            event = 7;
                                        } else {
                                            if (eventName.equals(EAP_FAILURE_STR)) {
                                                event = 8;
                                            } else {
                                                if (eventName.equals(ASSOC_REJECT_STR)) {
                                                    event = 9;
                                                } else {
                                                    if (eventName.equals(TEMP_DISABLED_STR)) {
                                                        event = 10;
                                                    } else {
                                                        if (eventName.equals(REENABLED_STR)) {
                                                            event = 11;
                                                        } else {
                                                            if (eventName.equals(BSS_ADDED_STR)) {
                                                                event = 12;
                                                            } else {
                                                                if (eventName.equals(BSS_REMOVED_STR)) {
                                                                    event = 13;
                                                                } else {
                                                                    if (eventName.equals(SELECT_NETWORK_STR)) {
                                                                        event = SELECT_NETWORK;
                                                                    } else {
                                                                        event = eventName.equals(WAPI_NO_CERTIFICATION_STRING) ? 16 : 14;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            String eventData = eventStr;
            if (event == 7 || event == 5) {
                eventData = eventStr.split(" ")[1];
            } else if (event == 3 || event == 8) {
                ind = eventStr.indexOf(" ");
                if (ind != -1) {
                    eventData = eventStr.substring(ind + 1);
                }
            } else {
                ind = eventStr.indexOf(" - ");
                if (ind != -1) {
                    eventData = eventStr.substring(ind + 3);
                }
            }
            int i;
            if (event == 10 || event == 11) {
                String substr = null;
                int netId = -1;
                ind = eventStr.indexOf(" ");
                if (ind != -1) {
                    substr = eventStr.substring(ind + 1);
                }
                if (substr != null && substr.contains("AUTH_FAILED")) {
                    Log.e(TAG, "contains AUTH_FAILED, then send suth failure message");
                    sendMessage(iface, (int) AUTHENTICATION_FAILURE_EVENT);
                }
                if (substr != null) {
                    for (String key : substr.split(" ")) {
                        if (key.regionMatches(0, "id=", 0, 3)) {
                            netId = 0;
                            for (int idx = 3; idx < key.length(); idx++) {
                                char c = key.charAt(idx);
                                if (c < '0' || c > '9') {
                                    break;
                                }
                                netId = (netId * 10) + (c - 48);
                            }
                        }
                    }
                }
                sendMessage(iface, event == 10 ? SSID_TEMP_DISABLED : SSID_REENABLED, netId, 0, substr);
            } else if (event == 3) {
                handleSupplicantStateChange(eventData, iface);
            } else if (event == 7) {
                handleDriverEvent(eventData, iface);
            } else if (event == 6) {
                if (eventData.startsWith(WPA_RECV_ERROR_STR)) {
                    i = this.mRecvErrors + 1;
                    this.mRecvErrors = i;
                    if (i <= 10) {
                        eventLogCounter++;
                        return false;
                    } else if (DBG) {
                        Log.d(TAG, "too many recv errors, closing connection");
                    }
                }
                sendMessage(iface, (int) SUP_DISCONNECTION_EVENT, eventLogCounter);
                if (!iface.equals("p2p0")) {
                    return true;
                }
                Log.d(TAG, "Ignore p2p0 CTRL-EVENT-TERMINATING");
                return false;
            } else if (event == 8) {
                if (eventData.startsWith(EAP_AUTH_FAILURE_STR)) {
                    sendMessage(iface, (int) AUTHENTICATION_FAILURE_EVENT, eventLogCounter);
                }
            } else if (event == 9) {
                Matcher match = mAssocRejectEventPattern.matcher(eventData);
                Object BSSID = "";
                int status = -1;
                if (match.find()) {
                    int statusGroupNumber;
                    if (match.groupCount() == 2) {
                        BSSID = match.group(1);
                        statusGroupNumber = 2;
                    } else {
                        BSSID = null;
                        statusGroupNumber = 1;
                    }
                    try {
                        status = Integer.parseInt(match.group(statusGroupNumber));
                    } catch (NumberFormatException e2) {
                        status = -1;
                    }
                } else if (DBG) {
                    Log.d(TAG, "Assoc Reject: Could not parse assoc reject string");
                }
                sendMessage(iface, ASSOCIATION_REJECTION_EVENT, eventLogCounter, status, BSSID);
            } else if (!(event == 12 || event == 13)) {
                if (event == SELECT_NETWORK) {
                    sendMessage(iface, (int) SELECT_NETWORK_EVENT, Integer.parseInt(eventData.split(" ")[1]));
                } else {
                    handleEvent(event, eventData, iface);
                }
            }
            this.mRecvErrors = 0;
            eventLogCounter++;
            return false;
        }
        if (eventStr.startsWith(WPS_SUCCESS_STR)) {
            sendMessage(iface, (int) WPS_SUCCESS_EVENT);
        } else {
            if (eventStr.startsWith(WPS_FAIL_STR)) {
                handleWpsFailEvent(eventStr, iface);
            } else {
                if (eventStr.startsWith(WPS_OVERLAP_STR)) {
                    sendMessage(iface, (int) WPS_OVERLAP_EVENT);
                } else {
                    if (eventStr.startsWith(WPS_TIMEOUT_STR)) {
                        sendMessage(iface, (int) WPS_TIMEOUT_EVENT);
                    } else {
                        if (eventStr.startsWith(P2P_EVENT_PREFIX_STR)) {
                            handleP2pEvents(eventStr, iface);
                        } else {
                            if (eventStr.startsWith(HOST_AP_EVENT_PREFIX_STR)) {
                                handleHostApEvents(eventStr, iface);
                            } else {
                                if (eventStr.startsWith(ANQP_DONE_STR)) {
                                    try {
                                        handleAnqpResult(eventStr, iface);
                                    } catch (IllegalArgumentException iae) {
                                        Log.e(TAG, "Bad ANQP event string: '" + eventStr + "': " + iae);
                                    }
                                } else {
                                    if (eventStr.startsWith(HS20_ICON_STR)) {
                                        try {
                                            handleIconResult(eventStr, iface);
                                        } catch (IllegalArgumentException iae2) {
                                            Log.e(TAG, "Bad Icon event string: '" + eventStr + "': " + iae2);
                                        }
                                    } else {
                                        Object[] objArr;
                                        if (eventStr.startsWith(HS20_SUB_REM_STR)) {
                                            objArr = new Object[2];
                                            objArr[0] = this.mLastConnectBSSIDs.get(iface);
                                            objArr[1] = eventStr;
                                            handleWnmFrame(String.format("%012x %s", objArr), iface);
                                        } else {
                                            if (eventStr.startsWith(HS20_DEAUTH_STR)) {
                                                objArr = new Object[2];
                                                objArr[0] = this.mLastConnectBSSIDs.get(iface);
                                                objArr[1] = eventStr;
                                                handleWnmFrame(String.format("%012x %s", objArr), iface);
                                            } else {
                                                if (eventStr.startsWith(REQUEST_PREFIX_STR)) {
                                                    handleRequests(eventStr, iface);
                                                } else {
                                                    if (eventStr.startsWith(TARGET_BSSID_STR)) {
                                                        handleTargetBSSIDEvent(eventStr, iface);
                                                    } else {
                                                        if (eventStr.startsWith(ASSOCIATED_WITH_STR)) {
                                                            handleAssociatedBSSIDEvent(eventStr, iface);
                                                        } else {
                                                            if (eventStr.startsWith("Authentication with")) {
                                                                if (eventStr.endsWith(AUTH_TIMEOUT_STR)) {
                                                                    sendMessage(iface, (int) AUTHENTICATION_FAILURE_EVENT);
                                                                }
                                                            }
                                                            if (eventStr.startsWith(WPA_EVENT_PREFIX_STR)) {
                                                                if (eventStr.indexOf(PASSWORD_MAY_BE_INCORRECT_STR) > 0) {
                                                                    sendMessage(iface, (int) WRONG_KEY_EVENT);
                                                                }
                                                            }
                                                            if (eventStr.startsWith(EAP_FAST_NEW_PAC_UPDATED)) {
                                                                sendMessage(iface, (int) NEW_PAC_UPDATED_EVENT);
                                                            } else {
                                                                if (eventStr.startsWith(WHOLE_CHIP_RESET_FAIL_STRING)) {
                                                                    sendMessage(iface, (int) WHOLE_CHIP_RESET_FAIL_EVENT);
                                                                } else {
                                                                    if (eventStr.startsWith(TDLS_CONNECTED_EVENT_STR)) {
                                                                        Log.i(TAG, "TDLS_CONNECTED_EVENT");
                                                                        sendMessage(iface, (int) TDLS_CONNECTED_EVENT, (Object) eventStr);
                                                                    } else {
                                                                        if (eventStr.startsWith(TDLS_DISCONNECTED_EVENT_STR)) {
                                                                            Log.i(TAG, "TDLS_DISCONNECTED_EVENT");
                                                                            sendMessage(iface, (int) TDLS_DISCONNECTED_EVENT, (Object) eventStr);
                                                                        } else if (DBG) {
                                                                            Log.w(TAG, "couldn't identify event type - " + eventStr);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        eventLogCounter++;
        return false;
    }

    private void handleDriverEvent(String state, String iface) {
        if (state != null && state.equals("HANGED")) {
            sendMessage(iface, (int) DRIVER_HUNG_EVENT);
        }
    }

    private void handleEvent(int event, String remainder, String iface) {
        if (DBG) {
            Log.d(TAG, "handleEvent " + Integer.toString(event) + " " + remainder);
        }
        switch (event) {
            case 1:
                handleNetworkStateChange(DetailedState.CONNECTED, remainder, iface);
                return;
            case 2:
                handleNetworkStateChange(DetailedState.DISCONNECTED, remainder, iface);
                return;
            case 4:
                sendMessage(iface, (int) SCAN_RESULTS_EVENT);
                return;
            case 14:
                if (DBG) {
                    Log.w(TAG, "handleEvent unknown: " + Integer.toString(event) + " " + remainder);
                    return;
                }
                return;
            case 15:
                sendMessage(iface, (int) SCAN_FAILED_EVENT);
                return;
            case 16:
                sendMessage(iface, (int) WAPI_NO_CERTIFICATION_EVENT);
                return;
            default:
                return;
        }
    }

    private void handleTargetBSSIDEvent(String eventStr, String iface) {
        Object BSSID = null;
        Matcher match = mTargetBSSIDPattern.matcher(eventStr);
        if (match.find()) {
            BSSID = match.group(1);
        }
        sendMessage(iface, 131213, eventLogCounter, 0, BSSID);
    }

    private void handleAssociatedBSSIDEvent(String eventStr, String iface) {
        Object BSSID = null;
        Matcher match = mAssociatedPattern.matcher(eventStr);
        if (match.find()) {
            BSSID = match.group(1);
        }
        sendMessage(iface, 131219, eventLogCounter, 0, BSSID);
    }

    private void handleWpsFailEvent(String dataString, String iface) {
        Matcher match = Pattern.compile(WPS_FAIL_PATTERN).matcher(dataString);
        int reason = 0;
        if (match.find()) {
            String cfgErrStr = match.group(1);
            String reasonStr = match.group(2);
            if (reasonStr != null) {
                int reasonInt = Integer.parseInt(reasonStr);
                switch (reasonInt) {
                    case 1:
                        sendMessage(iface, (int) WPS_FAIL_EVENT, 5);
                        return;
                    case 2:
                        sendMessage(iface, (int) WPS_FAIL_EVENT, 4);
                        return;
                    default:
                        reason = reasonInt;
                        break;
                }
            }
            if (cfgErrStr != null) {
                int cfgErrInt = Integer.parseInt(cfgErrStr);
                switch (cfgErrInt) {
                    case 12:
                        sendMessage(iface, (int) WPS_FAIL_EVENT, 3);
                        return;
                    case 18:
                        sendMessage(iface, (int) WPS_FAIL_EVENT, 6);
                        return;
                    default:
                        if (reason == 0) {
                            reason = cfgErrInt;
                            break;
                        }
                        break;
                }
            }
        }
        sendMessage(iface, WPS_FAIL_EVENT, 0, reason);
    }

    private P2pStatus p2pError(String dataString) {
        P2pStatus err = P2pStatus.UNKNOWN;
        String[] tokens = dataString.split(" ");
        if (tokens.length < 2) {
            return err;
        }
        String[] nameValue = tokens[1].split("=");
        if (nameValue.length != 2) {
            return err;
        }
        if (nameValue[1].equals("FREQ_CONFLICT")) {
            return P2pStatus.NO_COMMON_CHANNEL;
        }
        try {
            err = P2pStatus.valueOf(Integer.parseInt(nameValue[1]));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return err;
    }

    private P2pStatus p2pError_GroupRemove(String dataString) {
        P2pStatus err = P2pStatus.UNKNOWN;
        String[] tokens = dataString.split(" ");
        if (tokens.length < 3) {
            return err;
        }
        String[] nameValue = tokens[3].split("=");
        if (Character.isDigit(nameValue[1].charAt(0))) {
            try {
                err = P2pStatus.valueOf(Integer.parseInt(nameValue[1]));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (err == P2pStatus.NO_COMMON_CHANNEL && tokens.length > 4) {
            String[] freqNameValue = tokens[4].split("=");
            if (!tokens[4].startsWith("peer_oper_freq")) {
                Log.e(TAG, "Incorrect peer_oper_freq format" + tokens[4]);
            } else if (freqNameValue.length != 2) {
                Log.e(TAG, "Incorrect peer_oper_freq format" + tokens[4]);
            } else if (freqNameValue[1] == null) {
                Log.e(TAG, "Incorrect peer_oper_freq format" + tokens[4]);
            } else {
                sP2pOperFreq = Integer.valueOf(freqNameValue[1]).intValue();
            }
            Log.d(TAG, "sP2pOperFreq@p2pError_GroupRemove() = " + sP2pOperFreq);
        }
        return err;
    }

    private P2pStatus p2pErrorEx(String dataString) {
        P2pStatus status = P2pStatus.UNKNOWN;
        Matcher match = p2pErrorExPattern2.matcher(dataString);
        if (match.find()) {
            status = P2pStatus.valueOf(Integer.valueOf(match.group(1)).intValue());
            sP2pOperFreq = Integer.valueOf(match.group(2)).intValue();
        } else {
            match = p2pErrorExPattern1.matcher(dataString);
            if (match.find()) {
                status = P2pStatus.valueOf(Integer.valueOf(match.group(1)).intValue());
                sP2pOperFreq = -1;
            }
        }
        match = p2pErrorExPattern3.matcher(dataString);
        if (match.find()) {
            status = P2pStatus.NO_COMMON_CHANNEL;
            sP2pOperFreq = Integer.valueOf(match.group(1)).intValue();
        }
        Log.d(TAG, "p2pErrorEx status=" + status + ", peer_oper_freq=" + sP2pOperFreq);
        return status;
    }

    private WifiP2pDevice getWifiP2pDevice(String dataString) {
        try {
            return new WifiP2pDevice(dataString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private WifiP2pGroup getWifiP2pGroup(String dataString) {
        try {
            return new WifiP2pGroup(dataString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void handleP2pEvents(String dataString, String iface) {
        Object device;
        if (dataString.startsWith(P2P_DEVICE_FOUND_STR)) {
            device = getWifiP2pDevice(dataString);
            if (device != null) {
                sendMessage(iface, (int) P2P_DEVICE_FOUND_EVENT, device);
            }
        } else {
            if (dataString.startsWith(P2P_DEVICE_LOST_STR)) {
                device = getWifiP2pDevice(dataString);
                if (device != null) {
                    sendMessage(iface, (int) P2P_DEVICE_LOST_EVENT, device);
                }
            } else {
                if (dataString.startsWith(P2P_FIND_STOPPED_STR)) {
                    sendMessage(iface, (int) P2P_FIND_STOPPED_EVENT);
                } else {
                    if (dataString.startsWith(P2P_GO_NEG_REQUEST_STR)) {
                        sendMessage(iface, (int) P2P_GO_NEGOTIATION_REQUEST_EVENT, new WifiP2pConfig(dataString));
                    } else {
                        if (dataString.startsWith(P2P_GO_NEG_SUCCESS_STR)) {
                            sendMessage(iface, (int) P2P_GO_NEGOTIATION_SUCCESS_EVENT);
                        } else {
                            String str;
                            if (dataString.startsWith(P2P_GO_NEG_FAILURE_STR)) {
                                str = iface;
                                sendMessage(str, P2P_GO_NEGOTIATION_FAILURE_EVENT, sP2pOperFreq, 0, p2pErrorEx(dataString));
                                sP2pOperFreq = -1;
                            } else {
                                if (dataString.startsWith(P2P_GROUP_FORMATION_SUCCESS_STR)) {
                                    sendMessage(iface, (int) P2P_GROUP_FORMATION_SUCCESS_EVENT);
                                } else {
                                    if (dataString.startsWith(P2P_GROUP_FORMATION_FAILURE_STR)) {
                                        str = iface;
                                        sendMessage(str, P2P_GROUP_FORMATION_FAILURE_EVENT, sP2pOperFreq, 0, p2pErrorEx(dataString));
                                        sP2pOperFreq = -1;
                                    } else {
                                        if (dataString.startsWith(P2P_GROUP_STARTED_STR)) {
                                            WifiP2pGroup group = getWifiP2pGroup(dataString);
                                            if (group != null) {
                                                sendMessage(iface, (int) P2P_GROUP_STARTED_EVENT, (Object) group);
                                            }
                                        } else {
                                            if (dataString.startsWith(P2P_GROUP_REMOVED_STR)) {
                                                str = iface;
                                                sendMessage(str, P2P_GROUP_REMOVED_EVENT, sP2pOperFreq, 0, p2pError_GroupRemove(dataString));
                                                sP2pOperFreq = -1;
                                            } else {
                                                if (dataString.startsWith(P2P_INVITATION_RECEIVED_STR)) {
                                                    sendMessage(iface, (int) P2P_INVITATION_RECEIVED_EVENT, new WifiP2pGroup(dataString));
                                                } else {
                                                    if (dataString.startsWith(P2P_INVITATION_RESULT_STR)) {
                                                        str = iface;
                                                        sendMessage(str, P2P_INVITATION_RESULT_EVENT, sP2pOperFreq, 0, p2pErrorEx(dataString));
                                                        sP2pOperFreq = -1;
                                                    } else {
                                                        if (dataString.startsWith(P2P_PROV_DISC_PBC_REQ_STR)) {
                                                            sendMessage(iface, (int) P2P_PROV_DISC_PBC_REQ_EVENT, new WifiP2pProvDiscEvent(dataString));
                                                        } else {
                                                            if (dataString.startsWith(P2P_PROV_DISC_PBC_RSP_STR)) {
                                                                sendMessage(iface, (int) P2P_PROV_DISC_PBC_RSP_EVENT, new WifiP2pProvDiscEvent(dataString));
                                                            } else {
                                                                if (dataString.startsWith(P2P_PROV_DISC_ENTER_PIN_STR)) {
                                                                    sendMessage(iface, (int) P2P_PROV_DISC_ENTER_PIN_EVENT, new WifiP2pProvDiscEvent(dataString));
                                                                } else {
                                                                    if (dataString.startsWith(P2P_PROV_DISC_SHOW_PIN_STR)) {
                                                                        sendMessage(iface, (int) P2P_PROV_DISC_SHOW_PIN_EVENT, new WifiP2pProvDiscEvent(dataString));
                                                                    } else {
                                                                        if (dataString.startsWith(P2P_PROV_DISC_FAILURE_STR)) {
                                                                            sendMessage(iface, (int) P2P_PROV_DISC_FAILURE_EVENT);
                                                                        } else {
                                                                            if (dataString.startsWith(P2P_SERV_DISC_RESP_STR)) {
                                                                                List<WifiP2pServiceResponse> list = WifiP2pServiceResponse.newInstance(dataString);
                                                                                if (list != null) {
                                                                                    sendMessage(iface, (int) P2P_SERV_DISC_RESP_EVENT, (Object) list);
                                                                                } else {
                                                                                    Log.e(TAG, "Null service resp " + dataString);
                                                                                }
                                                                            } else {
                                                                                if (dataString.startsWith("CTRL-EVENT-DISCONNECTED")) {
                                                                                    String[] tokens = dataString.split(" ");
                                                                                    if (tokens.length >= 3) {
                                                                                        String[] nameValue = tokens[2].split("=");
                                                                                        if (nameValue.length >= 2) {
                                                                                            String[] freqNameValue;
                                                                                            if (tokens.length > 4 && tokens[4].startsWith("peer_oper_freq")) {
                                                                                                freqNameValue = tokens[4].split("=");
                                                                                                if (freqNameValue[1] != null) {
                                                                                                    sP2pOperFreq = Integer.valueOf(freqNameValue[1]).intValue();
                                                                                                    Log.d(TAG, "sP2pOperFreq@CTRL-EVENT-DISCONNECTED-1 = " + sP2pOperFreq);
                                                                                                }
                                                                                            }
                                                                                            if (tokens.length > 3 && tokens[3].startsWith("peer_oper_freq")) {
                                                                                                freqNameValue = tokens[3].split("=");
                                                                                                if (freqNameValue[1] != null) {
                                                                                                    sP2pOperFreq = Integer.valueOf(freqNameValue[1]).intValue();
                                                                                                    Log.d(TAG, "sP2pOperFreq@CTRL-EVENT-DISCONNECTED-2 = " + sP2pOperFreq);
                                                                                                }
                                                                                            }
                                                                                            sendMessage(iface, P2P_PEER_DISCONNECT_EVENT, sP2pOperFreq, 0, nameValue[1]);
                                                                                            sP2pOperFreq = -1;
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleHostApEvents(String dataString, String iface) {
        String[] tokens = dataString.split(" ");
        if (tokens[0].equals(AP_STA_CONNECTED_STR)) {
            sendMessage(iface, (int) AP_STA_CONNECTED_EVENT, new WifiP2pDevice(dataString));
        } else if (tokens[0].equals(AP_STA_DISCONNECTED_STR)) {
            sendMessage(iface, (int) AP_STA_DISCONNECTED_EVENT, new WifiP2pDevice(dataString));
        }
    }

    private void handleAnqpResult(String eventStr, String iface) {
        int addrPos = eventStr.indexOf(ADDR_STRING);
        int resPos = eventStr.indexOf(RESULT_STRING);
        if (addrPos < 0 || resPos < 0) {
            throw new IllegalArgumentException("Unexpected ANQP result notification");
        }
        int eoaddr = eventStr.indexOf(32, ADDR_STRING.length() + addrPos);
        if (eoaddr < 0) {
            eoaddr = eventStr.length();
        }
        int eoresult = eventStr.indexOf(32, RESULT_STRING.length() + resPos);
        if (eoresult < 0) {
            eoresult = eventStr.length();
        }
        try {
            sendMessage(iface, ANQP_DONE_EVENT, eventStr.substring(RESULT_STRING.length() + resPos, eoresult).equalsIgnoreCase("success") ? 1 : 0, 0, Long.valueOf(Utils.parseMac(eventStr.substring(ADDR_STRING.length() + addrPos, eoaddr))));
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Bad MAC address in ANQP response: " + iae.getMessage());
        }
    }

    private void handleIconResult(String eventStr, String iface) {
        String[] segments = eventStr.split(" ");
        if (segments.length != 4) {
            throw new IllegalArgumentException("Incorrect number of segments");
        }
        try {
            String bssid = segments[1];
            sendMessage(iface, (int) RX_HS20_ANQP_ICON_EVENT, new IconEvent(Utils.parseMac(bssid), segments[2], Integer.parseInt(segments[3])));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad numeral");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:2:0x000b A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:3:0x000c, code:
            android.util.Log.w(TAG, "Bad WNM event: '" + r6 + "'");
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleWnmFrame(String eventStr, String iface) {
        try {
            sendMessage(iface, (int) HS20_REMEDIATION_EVENT, WnmData.buildWnmData(eventStr));
        } catch (IOException e) {
        }
    }

    private void handleRequests(String dataString, String iface) {
        String SSID = null;
        int reason = -2;
        String requestName = dataString.substring(REQUEST_PREFIX_LEN_STR);
        if (!TextUtils.isEmpty(requestName)) {
            if (requestName.startsWith(IDENTITY_STR)) {
                Matcher match = mRequestIdentityPattern.matcher(requestName);
                if (match.find()) {
                    SSID = match.group(2);
                    try {
                        reason = Integer.parseInt(match.group(1));
                    } catch (NumberFormatException e) {
                        reason = -1;
                    }
                } else {
                    Log.e(TAG, "didn't find SSID " + requestName);
                }
                sendMessage(iface, SUP_REQUEST_IDENTITY, eventLogCounter, reason, SSID);
            } else if (requestName.startsWith("SIM")) {
                Matcher matchGsm = mRequestGsmAuthPattern.matcher(requestName);
                Matcher matchUmts = mRequestUmtsAuthPattern.matcher(requestName);
                Object data = new SimAuthRequestData();
                if (matchGsm.find()) {
                    data.networkId = Integer.parseInt(matchGsm.group(1));
                    data.protocol = 4;
                    data.ssid = matchGsm.group(4);
                    data.data = matchGsm.group(2).split(":");
                    sendMessage(iface, (int) SUP_REQUEST_SIM_AUTH, data);
                } else if (matchUmts.find()) {
                    data.networkId = Integer.parseInt(matchUmts.group(1));
                    data.protocol = 5;
                    data.ssid = matchUmts.group(4);
                    data.data = new String[2];
                    data.data[0] = matchUmts.group(2);
                    data.data[1] = matchUmts.group(3);
                    sendMessage(iface, (int) SUP_REQUEST_SIM_AUTH, data);
                } else {
                    Log.e(TAG, "couldn't parse SIM auth request - " + requestName);
                }
            } else if (DBG) {
                Log.w(TAG, "couldn't identify request type - " + dataString);
            }
        }
    }

    private void handleSupplicantStateChange(String dataString, String iface) {
        WifiSsid wifiSsid = null;
        int index = dataString.lastIndexOf("SSID=");
        if (index != -1) {
            wifiSsid = WifiSsid.createFromAsciiEncoded(dataString.substring(index + 5));
        }
        String BSSID = null;
        int networkId = -1;
        int newState = -1;
        for (String split : dataString.split(" ")) {
            String[] nameValue = split.split("=");
            if (nameValue.length == 2) {
                if (nameValue[0].equals("BSSID")) {
                    BSSID = nameValue[1];
                } else {
                    try {
                        int value = Integer.parseInt(nameValue[1]);
                        if (nameValue[0].equals("id")) {
                            networkId = value;
                        } else if (nameValue[0].equals("state")) {
                            newState = value;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        if (newState != -1) {
            SupplicantState newSupplicantState = SupplicantState.INVALID;
            for (SupplicantState state : SupplicantState.values()) {
                if (state.ordinal() == newState) {
                    newSupplicantState = state;
                    break;
                }
            }
            if (newSupplicantState == SupplicantState.INVALID) {
                Log.w(TAG, "Invalid supplicant state: " + newState);
            }
            StateChangeResult stateChangeResult = new StateChangeResult(networkId, wifiSsid, BSSID, newSupplicantState);
            if (stateChangeResult != null) {
                sendMessage(iface, SUPPLICANT_STATE_CHANGE_EVENT, eventLogCounter, 0, stateChangeResult);
            } else {
                Log.d(TAG, "stateChangeResult, don't send message");
            }
        }
    }

    private void handleNetworkStateChange(DetailedState newState, String data, String iface) {
        String BSSID = null;
        int networkId = -1;
        int reason = 0;
        int local = 0;
        Matcher match;
        if (newState == DetailedState.CONNECTED) {
            Object BSSID2;
            match = mConnectedEventPattern.matcher(data);
            if (match.find()) {
                BSSID2 = match.group(1);
                try {
                    networkId = Integer.parseInt(match.group(2));
                } catch (NumberFormatException e) {
                    networkId = -1;
                }
            } else if (DBG) {
                Log.d(TAG, "handleNetworkStateChange: Couldnt find BSSID in event string");
            }
            sendMessage(iface, NETWORK_CONNECTION_EVENT, networkId, 0, BSSID2);
        } else if (newState == DetailedState.DISCONNECTED) {
            match = mDisconnectedEventPattern.matcher(data);
            if (match.find()) {
                BSSID = match.group(1);
                try {
                    reason = Integer.parseInt(match.group(2));
                } catch (NumberFormatException e2) {
                    reason = -1;
                }
                try {
                    local = Integer.parseInt(match.group(4));
                } catch (NumberFormatException e3) {
                    local = -1;
                }
            } else if (DBG) {
                Log.d(TAG, "handleNetworkStateChange: Could not parse disconnect string");
            }
            if (DBG) {
                Log.d(TAG, "WifiMonitor notify network disconnect: " + BSSID + " reason=" + Integer.toString(reason));
            }
            sendMessage(iface, NETWORK_DISCONNECTION_EVENT, local, reason, BSSID);
        }
    }
}
