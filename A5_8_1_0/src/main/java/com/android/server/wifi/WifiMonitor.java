package com.android.server.wifi;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiSsid;
import android.os.Handler;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.wifi.hotspot2.AnqpEvent;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.WnmData;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.TelephonyUtil.SimAuthRequestData;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class WifiMonitor {
    public static final int ANQP_DONE_EVENT = 147500;
    public static final int ASSOCIATION_REJECTION_EVENT = 147499;
    public static final int AUTHENTICATION_FAILURE_EVENT = 147463;
    private static final int BASE = 147456;
    private static final int CONFIG_AUTH_FAILURE = 18;
    private static final int CONFIG_MULTIPLE_PBC_DETECTED = 12;
    public static final int FILS_NETWORK_CONNECTION_EVENT = 147519;
    public static final int GAS_QUERY_DONE_EVENT = 147508;
    public static final int GAS_QUERY_START_EVENT = 147507;
    public static final int HS20_REMEDIATION_EVENT = 147517;
    public static final int NETWORK_CONNECTION_EVENT = 147459;
    public static final int NETWORK_DISCONNECTION_EVENT = 147460;
    public static final int PNO_SCAN_RESULTS_EVENT = 147474;
    private static final int REASON_TKIP_ONLY_PROHIBITED = 1;
    private static final int REASON_WEP_PROHIBITED = 2;
    public static final int RX_HS20_ANQP_ICON_EVENT = 147509;
    public static final int SAVE_CONFIG_FAILED_EVENT = 147650;
    public static final int SCAN_FAILED_EVENT = 147473;
    public static final int SCAN_RESULTS_EVENT = 147461;
    private static final int SELECT_NETWORK = 147658;
    public static final int SELECT_NETWORK_EVENT = 147649;
    private static final String SELECT_NETWORK_STR = "SELECT-NETWORK";
    public static final int SSID_TEMP_DISABLED = 147651;
    public static final int SUPPLICANT_STATE_CHANGE_EVENT = 147462;
    public static final int SUP_CONNECTION_EVENT = 147457;
    public static final int SUP_DISCONNECTION_EVENT = 147458;
    public static final int SUP_REQUEST_IDENTITY = 147471;
    public static final int SUP_REQUEST_SIM_AUTH = 147472;
    private static final String TAG = "WifiMonitor";
    public static final int WPS_FAIL_EVENT = 147465;
    public static final int WPS_OVERLAP_EVENT = 147466;
    public static final int WPS_SUCCESS_EVENT = 147464;
    public static final int WPS_TIMEOUT_EVENT = 147467;
    public static final int WRONG_KEY_EVENT = 147648;
    private boolean mConnected = false;
    private final Map<String, SparseArray<Set<Handler>>> mHandlerMap = new HashMap();
    private final Map<String, Boolean> mMonitoringMap = new HashMap();
    private boolean mVerboseLoggingEnabled = false;
    private final WifiInjector mWifiInjector;

    public WifiMonitor(WifiInjector wifiInjector) {
        this.mWifiInjector = wifiInjector;
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
        } else {
            this.mVerboseLoggingEnabled = false;
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

    public void setMonitoring(String iface, boolean enabled) {
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
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "connecting to supplicant");
        }
        int connectTries = 0;
        while (true) {
            this.mConnected = this.mWifiInjector.getWifiNative().connectToSupplicant();
            if (this.mConnected) {
                return true;
            }
            int connectTries2 = connectTries + 1;
            if (connectTries >= 50) {
                return false;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            connectTries = connectTries2;
        }
    }

    public synchronized void startMonitoring(String iface, boolean isStaIface) {
        if (ensureConnectedLocked()) {
            setMonitoring(iface, true);
            broadcastSupplicantConnectionEvent(iface);
        } else {
            boolean originalMonitoring = isMonitoring(iface);
            setMonitoring(iface, true);
            broadcastSupplicantDisconnectionEvent(iface);
            setMonitoring(iface, originalMonitoring);
            Log.e(TAG, "startMonitoring(" + iface + ") failed!");
        }
    }

    public synchronized void stopMonitoring(String iface) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "stopMonitoring(" + iface + ")");
        }
        setMonitoring(iface, true);
        broadcastSupplicantDisconnectionEvent(iface);
        setMonitoring(iface, false);
    }

    public synchronized void stopAllMonitoring() {
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
        if (iface == null || ifaceHandlers == null) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "Sending to all monitors because there's no matching iface");
            }
            for (Entry<String, SparseArray<Set<Handler>>> entry : this.mHandlerMap.entrySet()) {
                if (isMonitoring((String) entry.getKey())) {
                    for (Handler handler : (Set) ((SparseArray) entry.getValue()).get(message.what)) {
                        if (handler != null) {
                            sendMessage(handler, Message.obtain(message));
                        }
                    }
                }
            }
        } else if (isMonitoring(iface)) {
            Set<Handler> ifaceWhatHandlers = (Set) ifaceHandlers.get(message.what);
            if (ifaceWhatHandlers != null) {
                for (Handler handler2 : ifaceWhatHandlers) {
                    if (handler2 != null) {
                        sendMessage(handler2, Message.obtain(message));
                    }
                }
            }
        } else if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "Dropping event because (" + iface + ") is stopped");
        }
        message.recycle();
    }

    private void sendMessage(Handler handler, Message message) {
        message.setTarget(handler);
        message.sendToTarget();
    }

    public void broadcastWpsFailEvent(String iface, int cfgError, int vendorErrorCode) {
        switch (vendorErrorCode) {
            case 1:
                sendMessage(iface, (int) WPS_FAIL_EVENT, 5);
                return;
            case 2:
                sendMessage(iface, (int) WPS_FAIL_EVENT, 4);
                return;
            default:
                int reason = vendorErrorCode;
                switch (cfgError) {
                    case 12:
                        sendMessage(iface, (int) WPS_FAIL_EVENT, 3);
                        return;
                    case 18:
                        sendMessage(iface, (int) WPS_FAIL_EVENT, 6);
                        return;
                    default:
                        if (vendorErrorCode == 0) {
                            reason = cfgError;
                        }
                        sendMessage(iface, WPS_FAIL_EVENT, 0, reason);
                        return;
                }
        }
    }

    public void broadcastWpsSuccessEvent(String iface) {
        sendMessage(iface, (int) WPS_SUCCESS_EVENT);
    }

    public void broadcastWpsOverlapEvent(String iface) {
        sendMessage(iface, (int) WPS_OVERLAP_EVENT);
    }

    public void broadcastWpsTimeoutEvent(String iface) {
        sendMessage(iface, (int) WPS_TIMEOUT_EVENT);
    }

    public void broadcastAnqpDoneEvent(String iface, AnqpEvent anqpEvent) {
        sendMessage(iface, (int) ANQP_DONE_EVENT, (Object) anqpEvent);
    }

    public void broadcastIconDoneEvent(String iface, IconEvent iconEvent) {
        sendMessage(iface, (int) RX_HS20_ANQP_ICON_EVENT, (Object) iconEvent);
    }

    public void broadcastWnmEvent(String iface, WnmData wnmData) {
        sendMessage(iface, (int) HS20_REMEDIATION_EVENT, (Object) wnmData);
    }

    public void broadcastNetworkIdentityRequestEvent(String iface, int networkId, String ssid) {
        sendMessage(iface, SUP_REQUEST_IDENTITY, 0, networkId, this.mWifiInjector.getWifiNative().ssidStrFromGbkHistory(ssid));
    }

    public void broadcastNetworkGsmAuthRequestEvent(String iface, int networkId, String ssid, String[] data) {
        sendMessage(iface, (int) SUP_REQUEST_SIM_AUTH, new SimAuthRequestData(networkId, 4, this.mWifiInjector.getWifiNative().ssidStrFromGbkHistory(ssid), data));
    }

    public void broadcastNetworkUmtsAuthRequestEvent(String iface, int networkId, String ssid, String[] data) {
        sendMessage(iface, (int) SUP_REQUEST_SIM_AUTH, new SimAuthRequestData(networkId, 5, this.mWifiInjector.getWifiNative().ssidStrFromGbkHistory(ssid), data));
    }

    public void broadcastScanResultEvent(String iface) {
        sendMessage(iface, (int) SCAN_RESULTS_EVENT);
    }

    public void broadcastPnoScanResultEvent(String iface) {
        sendMessage(iface, (int) PNO_SCAN_RESULTS_EVENT);
    }

    public void broadcastScanFailedEvent(String iface) {
        sendMessage(iface, (int) SCAN_FAILED_EVENT);
    }

    public void broadcastAuthenticationFailureEvent(String iface, int reason) {
        sendMessage(iface, AUTHENTICATION_FAILURE_EVENT, 0, reason);
    }

    public void broadcastAssociationRejectionEvent(String iface, int status, boolean timedOut, String bssid) {
        sendMessage(iface, ASSOCIATION_REJECTION_EVENT, timedOut ? 1 : 0, status, bssid);
    }

    public void broadcastAssociatedBssidEvent(String iface, String bssid) {
        sendMessage(iface, 131219, 0, 0, bssid);
    }

    public void broadcastTargetBssidEvent(String iface, String bssid) {
        sendMessage(iface, 131213, 0, 0, bssid);
    }

    public void broadcastNetworkConnectionEvent(String iface, int networkId, String bssid) {
        sendMessage(iface, NETWORK_CONNECTION_EVENT, networkId, 0, bssid);
    }

    public void broadcastFilsNetworkConnectionEvent(String iface, int networkId, String bssid) {
        sendMessage(iface, FILS_NETWORK_CONNECTION_EVENT, networkId, 0, bssid);
    }

    public void broadcastNetworkDisconnectionEvent(String iface, int local, int reason, String bssid) {
        sendMessage(iface, NETWORK_DISCONNECTION_EVENT, local, reason, bssid);
    }

    public void broadcastSupplicantStateChangeEvent(String iface, int networkId, WifiSsid wifiSsid, String bssid, SupplicantState newSupplicantState) {
        if (!(wifiSsid == null || (NativeUtil.isUtf(wifiSsid.getOctets()) ^ 1) == 0)) {
            wifiSsid = this.mWifiInjector.getWifiNative().wifiSsidFromGbkHistory(wifiSsid);
        }
        sendMessage(iface, SUPPLICANT_STATE_CHANGE_EVENT, 0, 0, new StateChangeResult(networkId, wifiSsid, bssid, newSupplicantState));
    }

    public void broadcastSupplicantConnectionEvent(String iface) {
        sendMessage(iface, 147457);
    }

    public void broadcastSupplicantDisconnectionEvent(String iface) {
        sendMessage(iface, 147458);
    }

    public void broadcastSelectNetworkEvent(String iface, int networkId) {
        sendMessage(iface, (int) SELECT_NETWORK_EVENT, networkId);
    }

    public void broadcastSaveConfigFailed(String iface, int networkId) {
        sendMessage(iface, (int) SAVE_CONFIG_FAILED_EVENT, networkId);
    }

    public void broadcastSsidTempDisabled(String iface, int networkId, String reason) {
        sendMessage(iface, SSID_TEMP_DISABLED, networkId, 0, reason);
    }
}
