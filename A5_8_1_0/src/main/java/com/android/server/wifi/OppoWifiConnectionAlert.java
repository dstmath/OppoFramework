package com.android.server.wifi;

import android.content.Context;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.StatusCode;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.util.List;

public class OppoWifiConnectionAlert {
    /* renamed from: -android-net-wifi-SupplicantStateSwitchesValues */
    private static final /* synthetic */ int[] f183-android-net-wifi-SupplicantStateSwitchesValues = null;
    private static final String ANY_BSSID = "any";
    private static final int AUTHENTICATION_FAILURE_EVENT_TIMEOUT = 1002;
    private static boolean DEBUG = false;
    private static final int EVENT_ASSOC_REJECT_TIMEOUT = 1000;
    private static final int EVENT_AUTO_CONNECT_TIMEOUT = 1004;
    private static final int EVENT_CONNECT_NETWORK_TIMEOUT = 1001;
    private static final int EVENT_SAVE_CONFIG_FAILED = 1006;
    private static final int EVENT_SELECT_TIMEOUT = 1003;
    private static final String INVALID_BSSID = "00:00:00:00:00:00";
    private static final int KEY_CERT = 5;
    private static final int KEY_EAP = 3;
    private static final int KEY_NONE = 0;
    private static final int KEY_WAPI = 4;
    private static final int KEY_WEP = 1;
    private static final int KEY_WPA = 2;
    private static final int MAX_RETRIES_AUTHENTICATION_LIMIT = 4;
    private static final int MAX_RETRIES_MANUAL_ASSOCIATION_REJECT = 5;
    private static final int MAX_RETRIES_MANUAL_WRONG_KEY_COUNT = 2;
    private static final int MAX_RETRIES_ON_ASSOCIATION_REJECT = 12;
    private static final int MAX_RETRIES_ON_AUTHENTICATION_FAILURE = 1;
    private static final int MAX_RETRIES_ON_WRONG_KEY_COUNT = 4;
    private static final String TAG = "OppoWifiConnectionAlert";
    private static final int TIMEOUT_ASSOC_REJECT = 15000;
    private static final int TIMEOUT_AUTH_FAILURE = 15000;
    private static final int TIMEOUT_AUTO_CONNECT = 20000;
    private static final int TIMEOUT_MANUAL_CONNECT = 80000;
    private static final int TIMEOUT_P2P_CONNECTED_SELECT = 14000;
    private static final int TIMEOUT_SELECT = 9500;
    private static final int TIMEOUT_WRONG_KEY = 15000;
    private static final int WEP_KEY_LENGTH_1 = 10;
    private static final int WEP_KEY_LENGTH_2 = 26;
    private static final int WEP_KEY_LENGTH_3 = 58;
    private static final int WEP_WRONG_KEY_1 = 13;
    private static final int WEP_WRONG_KEY_2 = 14;
    private static final int WEP_WRONG_KEY_3 = 15;
    private static final int WPA_KEY_LENGTH = 8;
    private static final int WRONG_KEY_EVENT_TIMEOUT = 1005;
    private boolean mAddAndConnectHiddenAP = false;
    private WifiConfiguration mBackupWifiConfiguration = null;
    private boolean mConnectAlreadyExistConfigByAdd = false;
    private int mConnectNetworkId = -1;
    private int mConnectingNetId = -1;
    private final Context mContext;
    private SupplicantState mCurrentState = SupplicantState.INVALID;
    private Handler mHandler;
    private boolean mIsSelectingNetwork = false;
    private boolean mNetworksDisabledDuringAutoConnect = false;
    private boolean mNetworksDisabledDuringConnect = false;
    private Integer mNewNetIdCreateByManuConnect = Integer.valueOf(-1);
    private Integer mOldNetIdCreatedByManuConnect = Integer.valueOf(-1);
    private WifiConfiguration mOldWifiConfiguration = null;
    private int mThirdAPKConnectNetworkId = -1;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiStateMachine mWifiStateMachine;

    private final class ConnectTimeoutHandler extends Handler {
        public ConnectTimeoutHandler(Looper lp) {
            super(lp);
        }

        public void handleMessage(Message msg) {
            if (OppoWifiConnectionAlert.DEBUG) {
                Log.d(OppoWifiConnectionAlert.TAG, "Connect timeout, msg.event :" + msg.what + ",msg.arg1: " + msg.arg1 + ", netId = " + OppoWifiConnectionAlert.this.mConnectNetworkId + ", enableAP " + OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect);
            }
            boolean isSaveConfigFailed = false;
            switch (msg.what) {
                case 1000:
                    if (OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect || OppoWifiConnectionAlert.this.mNetworksDisabledDuringAutoConnect) {
                        OppoWifiConnectionAlert.this.mWifiConfigManager.enableAllNetworks();
                        if (OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect) {
                            OppoWifiConnectionAlert.this.mWifiConfigManager.disableAndRemoveNetwork(OppoWifiConnectionAlert.this.mConnectNetworkId, 1000, 2);
                        }
                        OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect = false;
                        OppoWifiConnectionAlert.this.mNetworksDisabledDuringAutoConnect = false;
                        return;
                    }
                    return;
                case OppoWifiConnectionAlert.EVENT_CONNECT_NETWORK_TIMEOUT /*1001*/:
                    if (OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect) {
                        OppoWifiConnectionAlert.this.handleNetworkConnectionFailure(OppoWifiConnectionAlert.this.mConnectNetworkId, 3);
                        OppoWifiConnectionAlert.this.mWifiConfigManager.enableAllNetworks();
                        OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect = false;
                        return;
                    }
                    return;
                case OppoWifiConnectionAlert.AUTHENTICATION_FAILURE_EVENT_TIMEOUT /*1002*/:
                    if (OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect || OppoWifiConnectionAlert.this.mNetworksDisabledDuringAutoConnect) {
                        OppoWifiConnectionAlert.this.mWifiConfigManager.enableAllNetworks();
                        if (OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect) {
                            OppoWifiConnectionAlert.this.mWifiConfigManager.disableAndRemoveNetwork(OppoWifiConnectionAlert.this.mConnectNetworkId, 1000, 3);
                        }
                        OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect = false;
                        OppoWifiConnectionAlert.this.mNetworksDisabledDuringAutoConnect = false;
                        return;
                    }
                    return;
                case OppoWifiConnectionAlert.EVENT_SELECT_TIMEOUT /*1003*/:
                    break;
                case OppoWifiConnectionAlert.EVENT_AUTO_CONNECT_TIMEOUT /*1004*/:
                    if (OppoWifiConnectionAlert.this.mNetworksDisabledDuringAutoConnect) {
                        OppoWifiConnectionAlert.this.mWifiConfigManager.enableAllNetworks();
                        if (OppoWifiConnectionAlert.DEBUG) {
                            Log.d(OppoWifiConnectionAlert.TAG, "mThirdAPKConnectNetworkId=" + OppoWifiConnectionAlert.this.mThirdAPKConnectNetworkId);
                        }
                        OppoWifiConnectionAlert.this.mWifiConfigManager.disableNetwork(OppoWifiConnectionAlert.this.mThirdAPKConnectNetworkId, 1000, StatusCode.MCCA_TRACK_LIMIT_EXCEEDED);
                        OppoWifiConnectionAlert.this.mThirdAPKConnectNetworkId = -1;
                        OppoWifiConnectionAlert.this.mNetworksDisabledDuringAutoConnect = false;
                        return;
                    }
                    return;
                case OppoWifiConnectionAlert.WRONG_KEY_EVENT_TIMEOUT /*1005*/:
                    if (OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect) {
                        OppoWifiConnectionAlert.this.mWifiConfigManager.enableAllNetworks();
                        OppoWifiConnectionAlert.this.mWifiConfigManager.disableAndRemoveNetwork(OppoWifiConnectionAlert.this.mConnectNetworkId, 1000, 12);
                        OppoWifiConnectionAlert.this.mWifiConfigManager.clearDisableReasonCounter(OppoWifiConnectionAlert.this.mConnectNetworkId);
                        OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect = false;
                        return;
                    }
                    return;
                case OppoWifiConnectionAlert.EVENT_SAVE_CONFIG_FAILED /*1006*/:
                    isSaveConfigFailed = true;
                    break;
                default:
                    return;
            }
            if (msg != null) {
                OppoWifiConnectionAlert.this.mIsSelectingNetwork = false;
                int netId = msg.arg1;
                WifiConfiguration hidden = OppoWifiConnectionAlert.this.mWifiConfigManager.getConfiguredNetworkWithPassword(netId);
                if (hidden != null) {
                    if (OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect) {
                        OppoWifiConnectionAlert.this.mWifiConfigManager.enableAllNetworks();
                        boolean noNeedRemove = OppoWifiConnectionAlert.this.isConnectingExistNetwork(netId);
                        boolean found = OppoWifiConnectionAlert.this.isNetworkExistInScanResults(netId);
                        int reason;
                        if (!OppoWifiConnectionAlert.this.getAddAndConnectHiddenAp()) {
                            if (isSaveConfigFailed) {
                                reason = 12;
                            } else if (found) {
                                reason = 3;
                            } else {
                                reason = StatusCode.MCCA_TRACK_LIMIT_EXCEEDED;
                            }
                            OppoWifiConnectionAlert.this.mWifiConfigManager.disableAndRemoveNetwork(netId, 1000, reason);
                            OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect = false;
                            return;
                        } else if (OppoWifiConnectionAlert.this.mNewNetIdCreateByManuConnect.intValue() != netId) {
                            if (OppoWifiConnectionAlert.DEBUG) {
                                Log.d(OppoWifiConnectionAlert.TAG, "hiddenap try another keymgmt");
                            }
                            int newNetId = -1;
                            boolean isWep = OppoWifiConnectionAlert.this.isWepAP(hidden);
                            boolean isWpa = OppoWifiConnectionAlert.this.isWpaAP(hidden);
                            WifiConfiguration newConfig = new WifiConfiguration();
                            newConfig.SSID = hidden.SSID;
                            newConfig.hiddenSSID = true;
                            if (isWep || isWpa) {
                                if (isWep) {
                                    newConfig.allowedKeyManagement.set(1);
                                    String psk = hidden.wepKeys[hidden.wepTxKeyIndex];
                                    if (psk == null || psk.length() < 8) {
                                        if (found) {
                                            reason = 3;
                                        } else {
                                            reason = StatusCode.MCCA_TRACK_LIMIT_EXCEEDED;
                                        }
                                        if (noNeedRemove) {
                                            OppoWifiConnectionAlert.this.mWifiConfigManager.disableNetwork(hidden.networkId, 1000, reason);
                                            OppoWifiConnectionAlert.this.mWifiConfigManager.addOrUpdateNetwork(OppoWifiConnectionAlert.this.mBackupWifiConfiguration, 1000);
                                            OppoWifiConnectionAlert.this.mBackupWifiConfiguration = null;
                                            OppoWifiConnectionAlert.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(netId, hidden, reason);
                                        } else {
                                            OppoWifiConnectionAlert.this.mWifiConfigManager.disableAndRemoveNetwork(netId, 1000, reason);
                                        }
                                        OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect = false;
                                        return;
                                    }
                                    int len = psk.length();
                                    if (!((len == 10 || len == 26 || len == 58) && psk.matches("[0-9A-Fa-f]*"))) {
                                        psk = psk.substring(1, psk.length() - 1);
                                    }
                                    if (psk.matches("[0-9A-Fa-f]{64}")) {
                                        newConfig.preSharedKey = psk;
                                    } else {
                                        newConfig.preSharedKey = '\"' + psk + '\"';
                                    }
                                    if (OppoWifiConnectionAlert.DEBUG) {
                                        Log.d(OppoWifiConnectionAlert.TAG, "hiddenap try wpa psk:" + newConfig.preSharedKey);
                                    }
                                } else if (isWpa) {
                                    newConfig.allowedKeyManagement.set(0);
                                    newConfig.allowedAuthAlgorithms.set(0);
                                    newConfig.allowedAuthAlgorithms.set(1);
                                    newConfig.wepTxKeyIndex = 0;
                                    String pwd = hidden.preSharedKey;
                                    if (pwd == null || pwd.length() < 8) {
                                        if (found) {
                                            reason = 3;
                                        } else {
                                            reason = StatusCode.MCCA_TRACK_LIMIT_EXCEEDED;
                                        }
                                        if (noNeedRemove) {
                                            OppoWifiConnectionAlert.this.mWifiConfigManager.disableNetwork(hidden.networkId, 1000, reason);
                                            OppoWifiConnectionAlert.this.mWifiConfigManager.addOrUpdateNetwork(OppoWifiConnectionAlert.this.mBackupWifiConfiguration, 1000);
                                            OppoWifiConnectionAlert.this.mBackupWifiConfiguration = null;
                                            OppoWifiConnectionAlert.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(netId, hidden, reason);
                                        } else {
                                            OppoWifiConnectionAlert.this.mWifiConfigManager.disableAndRemoveNetwork(netId, 1000, reason);
                                        }
                                        OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect = false;
                                        return;
                                    }
                                    pwd = pwd.substring(1, pwd.length() - 1);
                                    int length = pwd.length();
                                    if ((length == 10 || length == 26 || length == 58) && pwd.matches("[0-9A-Fa-f]*")) {
                                        newConfig.wepKeys[newConfig.wepTxKeyIndex] = pwd;
                                    } else {
                                        newConfig.wepKeys[newConfig.wepTxKeyIndex] = '\"' + pwd + '\"';
                                    }
                                    if (OppoWifiConnectionAlert.DEBUG) {
                                        Log.d(OppoWifiConnectionAlert.TAG, "hiddenap try wep wepKeys:" + newConfig.wepKeys[newConfig.wepTxKeyIndex]);
                                    }
                                }
                                if (noNeedRemove) {
                                    OppoWifiConnectionAlert.this.mWifiConfigManager.addOrUpdateNetwork(OppoWifiConnectionAlert.this.mBackupWifiConfiguration, 1000);
                                    OppoWifiConnectionAlert.this.mBackupWifiConfiguration = null;
                                }
                                WifiConfiguration savedConfig = OppoWifiConnectionAlert.this.mWifiConfigManager.getConfiguredNetwork(newConfig.configKey());
                                if (savedConfig != null) {
                                    OppoWifiConnectionAlert.this.setLastUpdatedWifiConfiguration(OppoWifiConnectionAlert.this.mWifiConfigManager.getConfiguredNetworkWithPassword(savedConfig.networkId));
                                    OppoWifiConnectionAlert.this.mWifiConfigManager.disableNetwork(savedConfig.networkId, 1000);
                                }
                                NetworkUpdateResult result = OppoWifiConnectionAlert.this.mWifiConfigManager.addOrUpdateNetwork(newConfig, 1000);
                                if (result != null) {
                                    newNetId = result.getNetworkId();
                                    newConfig.networkId = newNetId;
                                }
                                if (newNetId == -1) {
                                    if (OppoWifiConnectionAlert.DEBUG) {
                                        Log.d(OppoWifiConnectionAlert.TAG, "add new network failed!");
                                    }
                                    if (found) {
                                        reason = 3;
                                    } else {
                                        reason = StatusCode.MCCA_TRACK_LIMIT_EXCEEDED;
                                    }
                                    if (noNeedRemove) {
                                        OppoWifiConnectionAlert.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(netId, hidden, reason);
                                    } else {
                                        if (OppoWifiConnectionAlert.DEBUG) {
                                            Log.d(OppoWifiConnectionAlert.TAG, "netId:" + netId + "not found,remove it");
                                        }
                                        OppoWifiConnectionAlert.this.mWifiConfigManager.disableAndRemoveNetwork(netId, 1000, reason);
                                    }
                                    if (savedConfig != null) {
                                        OppoWifiConnectionAlert.this.mWifiConfigManager.enableNetwork(savedConfig.networkId, false, 1000);
                                    }
                                    OppoWifiConnectionAlert.this.mBackupWifiConfiguration = null;
                                    OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect = false;
                                } else {
                                    Message mMsg = OppoWifiConnectionAlert.this.mHandler.obtainMessage(151553, -1, 0, newConfig);
                                    mMsg.sendingUid = 1000;
                                    OppoWifiConnectionAlert.this.mWifiStateMachine.sendMessage(mMsg);
                                    OppoWifiConnectionAlert.this.mOldNetIdCreatedByManuConnect = Integer.valueOf(netId);
                                    OppoWifiConnectionAlert.this.mOldWifiConfiguration = OppoWifiConnectionAlert.this.mWifiConfigManager.getConfiguredNetwork(OppoWifiConnectionAlert.this.mOldNetIdCreatedByManuConnect.intValue());
                                    OppoWifiConnectionAlert.this.mNewNetIdCreateByManuConnect = Integer.valueOf(newNetId);
                                    if (noNeedRemove) {
                                        if (OppoWifiConnectionAlert.DEBUG) {
                                            Log.d(OppoWifiConnectionAlert.TAG, "disable mOldNetIdCreatedByManuConnect:" + OppoWifiConnectionAlert.this.mOldNetIdCreatedByManuConnect);
                                        }
                                        OppoWifiConnectionAlert.this.mWifiConfigManager.disableNetwork(netId, 1000);
                                    } else {
                                        if (OppoWifiConnectionAlert.DEBUG) {
                                            Log.d(OppoWifiConnectionAlert.TAG, "mOldNetIdCreatedByManuConnect:" + OppoWifiConnectionAlert.this.mOldNetIdCreatedByManuConnect + "not found,forget it");
                                        }
                                        OppoWifiConnectionAlert.this.mWifiConfigManager.removeNetwork(OppoWifiConnectionAlert.this.mOldNetIdCreatedByManuConnect.intValue(), 1000);
                                    }
                                }
                            } else {
                                if (found) {
                                    reason = 3;
                                } else {
                                    reason = StatusCode.MCCA_TRACK_LIMIT_EXCEEDED;
                                }
                                OppoWifiConnectionAlert.this.mWifiConfigManager.disableAndRemoveNetwork(netId, 1000, reason);
                                OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect = false;
                            }
                        } else {
                            Log.d(OppoWifiConnectionAlert.TAG, "hiddenap switch try failed!");
                            if (noNeedRemove) {
                                if (OppoWifiConnectionAlert.DEBUG) {
                                    Log.d(OppoWifiConnectionAlert.TAG, "netId " + netId + " is already in configednetworks,do restore");
                                }
                                OppoWifiConnectionAlert.this.mWifiConfigManager.disableNetwork(OppoWifiConnectionAlert.this.mNewNetIdCreateByManuConnect.intValue(), 1000, StatusCode.MCCA_TRACK_LIMIT_EXCEEDED);
                                OppoWifiConnectionAlert.this.mWifiConfigManager.addOrUpdateNetwork(OppoWifiConnectionAlert.this.mBackupWifiConfiguration, 1000);
                                OppoWifiConnectionAlert.this.mBackupWifiConfiguration = null;
                            } else {
                                if (OppoWifiConnectionAlert.DEBUG) {
                                    Log.d(OppoWifiConnectionAlert.TAG, "do remove netid:" + OppoWifiConnectionAlert.this.mNewNetIdCreateByManuConnect);
                                }
                                OppoWifiConnectionAlert.this.mWifiConfigManager.disableNetwork(OppoWifiConnectionAlert.this.mNewNetIdCreateByManuConnect.intValue(), 1000, StatusCode.MCCA_TRACK_LIMIT_EXCEEDED);
                                OppoWifiConnectionAlert.this.mWifiConfigManager.removeNetwork(OppoWifiConnectionAlert.this.mNewNetIdCreateByManuConnect.intValue(), 1000);
                            }
                            found = OppoWifiConnectionAlert.this.isNetworkExistInScanResults(OppoWifiConnectionAlert.this.mOldWifiConfiguration);
                            if (OppoWifiConnectionAlert.this.isWepAP(OppoWifiConnectionAlert.this.mOldWifiConfiguration)) {
                                if (found) {
                                    reason = 12;
                                } else {
                                    reason = StatusCode.MCCA_TRACK_LIMIT_EXCEEDED;
                                }
                            } else if (found) {
                                reason = 3;
                            } else {
                                reason = StatusCode.MCCA_TRACK_LIMIT_EXCEEDED;
                            }
                            OppoWifiConnectionAlert.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(OppoWifiConnectionAlert.this.mOldNetIdCreatedByManuConnect.intValue(), OppoWifiConnectionAlert.this.mOldWifiConfiguration, reason);
                            OppoWifiConnectionAlert.this.mNewNetIdCreateByManuConnect = Integer.valueOf(-1);
                            OppoWifiConnectionAlert.this.mNetworksDisabledDuringConnect = false;
                        }
                    } else if (OppoWifiConnectionAlert.this.mNetworksDisabledDuringAutoConnect) {
                        OppoWifiConnectionAlert.this.mWifiConfigManager.enableAllNetworks();
                        OppoWifiConnectionAlert.this.mNetworksDisabledDuringAutoConnect = false;
                    }
                    OppoWifiConnectionAlert.this.mWifiStateMachine.tryStartPnoScan();
                } else if (OppoWifiConnectionAlert.DEBUG) {
                    Log.d(OppoWifiConnectionAlert.TAG, "hidden == null");
                }
            }
        }
    }

    /* renamed from: -getandroid-net-wifi-SupplicantStateSwitchesValues */
    private static /* synthetic */ int[] m96-getandroid-net-wifi-SupplicantStateSwitchesValues() {
        if (f183-android-net-wifi-SupplicantStateSwitchesValues != null) {
            return f183-android-net-wifi-SupplicantStateSwitchesValues;
        }
        int[] iArr = new int[SupplicantState.values().length];
        try {
            iArr[SupplicantState.ASSOCIATED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SupplicantState.ASSOCIATING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SupplicantState.AUTHENTICATING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SupplicantState.COMPLETED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SupplicantState.DISCONNECTED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SupplicantState.DORMANT.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[SupplicantState.FOUR_WAY_HANDSHAKE.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[SupplicantState.GROUP_HANDSHAKE.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[SupplicantState.INACTIVE.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[SupplicantState.INTERFACE_DISABLED.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[SupplicantState.INVALID.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[SupplicantState.SCANNING.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[SupplicantState.UNINITIALIZED.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        f183-android-net-wifi-SupplicantStateSwitchesValues = iArr;
        return iArr;
    }

    public void enableVerboseLogging(int verbose) {
        Log.d(TAG, "enableVerboseLogging verbose = " + verbose);
        if (verbose > 0) {
            DEBUG = true;
        } else {
            DEBUG = false;
        }
    }

    OppoWifiConnectionAlert(Context mCtxt, WifiStateMachine mWsm, WifiConfigManager mWcm) {
        this.mContext = mCtxt;
        this.mWifiStateMachine = mWsm;
        this.mWifiConfigManager = mWcm;
        Handler hldler = this.mWifiStateMachine.getHandler();
        if (hldler != null) {
            this.mHandler = new ConnectTimeoutHandler(hldler.getLooper());
        }
    }

    public void setAddAndConnectHiddenAp(boolean val) {
        if (DEBUG) {
            Log.d(TAG, "setAddAndConnectHiddenAp=" + val);
        }
        this.mAddAndConnectHiddenAP = val;
    }

    public boolean getAddAndConnectHiddenAp() {
        if (DEBUG) {
            Log.d(TAG, "getAddAndConnectHiddenAp=" + this.mAddAndConnectHiddenAP);
        }
        return this.mAddAndConnectHiddenAP;
    }

    public void setConnectAlreadyExistConfigByAdd(boolean val) {
        if (DEBUG) {
            Log.d(TAG, "setConnectAlreadyExistConfigByManual:" + val);
        }
        this.mConnectAlreadyExistConfigByAdd = val;
    }

    public void setLastUpdatedWifiConfiguration(WifiConfiguration wc) {
        if (DEBUG) {
            Log.d(TAG, "setLastUpdatedWifiConfiguration");
        }
        this.mBackupWifiConfiguration = new WifiConfiguration(wc);
    }

    public boolean isSelectingNetwork() {
        return this.mIsSelectingNetwork;
    }

    private boolean isConnectingExistNetwork(int netid) {
        if (this.mConnectAlreadyExistConfigByAdd && this.mBackupWifiConfiguration != null && this.mBackupWifiConfiguration.networkId == netid) {
            return true;
        }
        return false;
    }

    public boolean isNetworkExistInScanResults(WifiConfiguration wc) {
        if (wc == null) {
            return false;
        }
        String configKey = wc.configKey();
        List<ScanResult> list = this.mWifiStateMachine.syncGetScanResultsList();
        if (list == null) {
            return false;
        }
        for (ScanResult sr : list) {
            if (WifiConfiguration.configKey(sr).equals(configKey)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNetworkExistInScanResults(int netid) {
        return isNetworkExistInScanResults(this.mWifiConfigManager.getConfiguredNetwork(netid));
    }

    private boolean isWepAP(WifiConfiguration wc) {
        if (wc != null && wc.allowedKeyManagement.get(0) && wc.wepTxKeyIndex >= 0 && wc.wepTxKeyIndex < wc.wepKeys.length && wc.wepKeys[wc.wepTxKeyIndex] != null) {
            return true;
        }
        return false;
    }

    private boolean isWepAP(int netid) {
        return isWepAP(this.mWifiConfigManager.getConfiguredNetwork(netid));
    }

    private boolean isWpaAP(WifiConfiguration wc) {
        if (wc == null) {
            return false;
        }
        return wc.allowedKeyManagement.get(1) || wc.allowedKeyManagement.get(4);
    }

    private boolean isWpaAP(int netid) {
        return isWpaAP(this.mWifiConfigManager.getConfiguredNetwork(netid));
    }

    public boolean needSaveAsHiddenAP(WifiConfiguration wconf) {
        if (wconf == null) {
            if (DEBUG) {
                Log.d(TAG, "unexpected: wconf is null");
            }
            return false;
        } else if (wconf.hiddenSSID) {
            if (DEBUG) {
                Log.d(TAG, wconf.SSID + " is already set as hidden AP");
            }
            return false;
        } else if (isNetworkExistInScanResults(wconf)) {
            String bssid = wconf.BSSID;
            if (bssid == null || INVALID_BSSID.equals(bssid) || "any".equals(bssid)) {
                if (DEBUG) {
                    Log.d(TAG, "bssid is null or invalid!");
                }
                return false;
            }
            String configKey = wconf.configKey();
            if (configKey == null) {
                if (DEBUG) {
                    Log.d(TAG, "configKey is null!!");
                }
                return false;
            }
            List<ScanResult> scanList = this.mWifiStateMachine.syncGetScanResultsList();
            if (scanList != null) {
                String ssid = wconf.SSID.substring(1, wconf.SSID.length() - 1);
                for (ScanResult sr : scanList) {
                    if (sr.SSID == "" && sr.BSSID != null && sr.BSSID.equals(bssid)) {
                        sr.SSID = ssid;
                        String srConfigKey = WifiConfiguration.configKey(sr);
                        if (srConfigKey != null && srConfigKey.equals(configKey)) {
                            if (DEBUG) {
                                Log.d(TAG, "BSID:" + bssid + " is a hidden ap!");
                            }
                            return true;
                        }
                    }
                }
            } else if (DEBUG) {
                Log.d(TAG, "unexpected:scanList is null");
            }
            return false;
        } else {
            if (DEBUG) {
                Log.d(TAG, "network is not exist in scan results!");
            }
            return true;
        }
    }

    public void handleSSIDStateChangedCB(int netId, int reason) {
        if (DEBUG) {
            Log.d(TAG, "handleSSIDStateChangedCB netId:" + netId + " reason:" + reason);
        }
        if (this.mNetworksDisabledDuringConnect) {
            switch (reason) {
                case 2:
                    this.mHandler.removeMessages(1000);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1000), 15000);
                    return;
                case 3:
                    this.mHandler.removeMessages(AUTHENTICATION_FAILURE_EVENT_TIMEOUT);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(AUTHENTICATION_FAILURE_EVENT_TIMEOUT), 15000);
                    return;
                case 12:
                    this.mHandler.removeMessages(WRONG_KEY_EVENT_TIMEOUT);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(WRONG_KEY_EVENT_TIMEOUT), 15000);
                    return;
                default:
                    return;
            }
        }
    }

    public void setWifiState(int state) {
        if (DEBUG) {
            Log.d(TAG, "setWifiState to: " + state);
        }
        if (state == 1) {
            this.mNetworksDisabledDuringConnect = false;
            this.mNetworksDisabledDuringAutoConnect = false;
            this.mThirdAPKConnectNetworkId = -1;
        }
    }

    public boolean isManuConnect() {
        return this.mNetworksDisabledDuringConnect;
    }

    public int getManuConnectNetId() {
        return this.mConnectNetworkId;
    }

    public void setManuConnect(boolean isManualConnect) {
        this.mNetworksDisabledDuringConnect = isManualConnect;
    }

    private int getKeymgmtType(String bssid) {
        int key = 0;
        if (bssid == null) {
            return 0;
        }
        for (ScanResult result : this.mWifiStateMachine.syncGetScanResultsList()) {
            String scanBssid = result.BSSID;
            String capabilitie = result.capabilities;
            if (scanBssid.equals(bssid)) {
                if (capabilitie.contains("WEP")) {
                    key = 1;
                } else if (capabilitie.contains("PSK")) {
                    key = 2;
                } else if (capabilitie.contains("EAP") || capabilitie.contains("IEEE8021X")) {
                    key = 3;
                } else if (capabilitie.contains("WAPI-KEY")) {
                    key = 4;
                } else if (capabilitie.contains("WAPI-CERT")) {
                    key = 5;
                } else {
                    key = 0;
                }
            }
        }
        return key;
    }

    private void removeTimeoutEvent() {
        this.mHandler.removeMessages(AUTHENTICATION_FAILURE_EVENT_TIMEOUT);
        this.mHandler.removeMessages(1000);
        this.mHandler.removeMessages(EVENT_SELECT_TIMEOUT);
        this.mHandler.removeMessages(WRONG_KEY_EVENT_TIMEOUT);
    }

    public void sendSupplicantStateChangeEvent(StateChangeResult stateChangeResult) {
        handleSupplicantStateChange(stateChangeResult);
    }

    public void sendAuthFailedEvent(Message message) {
        handleAuthFailedEvent(message);
    }

    public void sendWrongKeyEvent() {
        handleWrongKeyEvent();
    }

    public void sendAssociationRejectionEvent(Message message) {
        handleAssociationRejectionEvent(message);
    }

    public void sendSelectNetworkEvent(Message message) {
        handleSelectNetworkEvent(message);
    }

    public void sendSaveConfigFailed(int netId) {
        removeTimeoutEvent();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_SAVE_CONFIG_FAILED, netId, 0));
    }

    public void sendSupRequestIdentityEvent(int netid) {
        handleSupRequestIdentityEvent(netid);
    }

    public void sendEnableNetworkEvent(int netid) {
        handleEnableNetworkEvent(netid);
    }

    public void sendAutoJoinOptimiazeEvent() {
        handleAutoJoinOptimiazeEvent();
    }

    public void sendConnectNetworkEvent(int netid) {
        handleConnectNetworkEvent(netid);
    }

    private void handleSupplicantStateChange(StateChangeResult stateChangeResult) {
        SupplicantState supState = stateChangeResult.state;
        if (DEBUG) {
            Log.d(TAG, "Supplicant state: " + supState.toString() + "\n");
        }
        if (supState != null) {
            this.mCurrentState = supState;
        }
        if (stateChangeResult != null) {
            if (stateChangeResult.networkId != -1) {
                this.mConnectingNetId = stateChangeResult.networkId;
                if (DEBUG) {
                    Log.d(TAG, "set conncting net id=" + this.mConnectingNetId);
                }
            } else if (this.mNetworksDisabledDuringConnect) {
                Log.d(TAG, "invalid netid for manu connect,ignore!!");
            } else {
                this.mConnectingNetId = stateChangeResult.networkId;
                if (DEBUG) {
                    Log.d(TAG, "set conncting net id=" + this.mConnectingNetId);
                }
            }
        }
        if (!(supState == SupplicantState.INACTIVE || supState == SupplicantState.SCANNING || supState == SupplicantState.DISCONNECTED || supState == SupplicantState.INTERFACE_DISABLED)) {
            this.mHandler.removeMessages(1000);
        }
        switch (m96-getandroid-net-wifi-SupplicantStateSwitchesValues()[supState.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 7:
            case 8:
                this.mIsSelectingNetwork = false;
                if (!this.mNetworksDisabledDuringConnect || this.mConnectingNetId == this.mConnectNetworkId) {
                    this.mHandler.removeMessages(EVENT_SELECT_TIMEOUT);
                    this.mHandler.removeMessages(WRONG_KEY_EVENT_TIMEOUT);
                    return;
                } else if (DEBUG) {
                    Log.d(TAG, "manual debug:got autoconnect supplicant evt,do not remove timeout timer");
                    return;
                } else {
                    return;
                }
            case 4:
                this.mIsSelectingNetwork = false;
                handleConnectCompleted();
                return;
            case 5:
                handleDisconnected(stateChangeResult);
                return;
            case 6:
            case 9:
            case 10:
            case 12:
                return;
            case 11:
            case 13:
                removeTimeoutEvent();
                return;
            default:
                Log.e(TAG, "Unknown supplicant state " + supState);
                return;
        }
    }

    private void handleAuthFailedEvent(Message message) {
        removeTimeoutEvent();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(AUTHENTICATION_FAILURE_EVENT_TIMEOUT, message.obj), 15000);
    }

    private void handleWrongKeyEvent() {
        removeTimeoutEvent();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(WRONG_KEY_EVENT_TIMEOUT), 15000);
    }

    private void handleAssociationRejectionEvent(Message message) {
        int status = message.arg2;
        int keyMgmt = getKeymgmtType(message.obj);
        WifiConfiguration wnassoc = this.mWifiConfigManager.getConfiguredNetwork(this.mConnectNetworkId);
        if (wnassoc != null && this.mNetworksDisabledDuringConnect && this.mConnectNetworkId == this.mConnectingNetId && this.mConnectNetworkId != -1) {
            if (DEBUG) {
                Log.d(TAG, "ManualConnect status: " + status);
            }
            if (keyMgmt == 1 && (status == 13 || status == 14 || status == 15)) {
                this.mWifiConfigManager.updateNetworkSelectionStatus(wnassoc.networkId, 12);
                this.mWifiConfigManager.clearDisableReasonCounter(3, wnassoc.networkId);
                this.mWifiConfigManager.clearDisableReasonCounter(2, wnassoc.networkId);
            } else {
                this.mWifiConfigManager.clearDisableReasonCounter(3, wnassoc.networkId);
                this.mWifiConfigManager.clearDisableReasonCounter(12, wnassoc.networkId);
            }
        }
        removeTimeoutEvent();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1000, message.obj), (long) this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_TIMEOUT_ASSOC_REJECT", Integer.valueOf(WifiConnectivityManager.PERIODIC_SCAN_INTERVAL_MS)).intValue());
    }

    private void handleSelectNetworkEvent(Message message) {
        removeTimeoutEvent();
        if (this.mNetworksDisabledDuringConnect || !SupplicantState.isConnecting(this.mCurrentState)) {
            this.mIsSelectingNetwork = true;
        } else if (DEBUG) {
            Log.d(TAG, "supplicant already in auto connecting state!!");
        }
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_SELECT_TIMEOUT, message.arg1, 0), (long) (message.arg2 == 1 ? TIMEOUT_P2P_CONNECTED_SELECT : TIMEOUT_SELECT));
    }

    private void handleSupRequestIdentityEvent(int netid) {
        handleNetworkConnectionFailure(netid, 12);
        removeTimeoutEvent();
    }

    private void handleEnableNetworkEvent(int netid) {
        if (DEBUG) {
            Log.d(TAG, "CMD_ENABLE_NETWORK");
        }
        this.mNetworksDisabledDuringAutoConnect = true;
        this.mThirdAPKConnectNetworkId = netid;
        this.mHandler.removeMessages(EVENT_CONNECT_NETWORK_TIMEOUT);
        this.mHandler.removeMessages(1000);
        this.mHandler.removeMessages(AUTHENTICATION_FAILURE_EVENT_TIMEOUT);
        this.mHandler.removeMessages(EVENT_AUTO_CONNECT_TIMEOUT);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_AUTO_CONNECT_TIMEOUT), (long) this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_TIMEOUT_AUTO_CONNECT", Integer.valueOf(TIMEOUT_AUTO_CONNECT)).intValue());
    }

    private void handleAutoJoinOptimiazeEvent() {
        this.mNetworksDisabledDuringAutoConnect = true;
        this.mHandler.removeMessages(EVENT_CONNECT_NETWORK_TIMEOUT);
        this.mHandler.removeMessages(1000);
        this.mHandler.removeMessages(AUTHENTICATION_FAILURE_EVENT_TIMEOUT);
        this.mHandler.removeMessages(EVENT_AUTO_CONNECT_TIMEOUT);
    }

    private void handleConnectNetworkEvent(int netid) {
        this.mNetworksDisabledDuringConnect = true;
        this.mConnectNetworkId = netid;
        this.mThirdAPKConnectNetworkId = -1;
        Log.d(TAG, "mConnectNetworkId=" + this.mConnectNetworkId + ",mConnectingNetId=" + this.mConnectingNetId);
        if (this.mConnectNetworkId != -1) {
            this.mWifiConfigManager.clearDisableReasonCounter(this.mConnectNetworkId);
        }
        this.mNetworksDisabledDuringAutoConnect = false;
        this.mHandler.removeMessages(EVENT_AUTO_CONNECT_TIMEOUT);
        this.mHandler.removeMessages(EVENT_CONNECT_NETWORK_TIMEOUT);
        this.mHandler.removeMessages(1000);
        this.mHandler.removeMessages(AUTHENTICATION_FAILURE_EVENT_TIMEOUT);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_CONNECT_NETWORK_TIMEOUT, Integer.valueOf(netid)), (long) this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_TIMEOUT_MANUAL_CONNECT", Integer.valueOf(TIMEOUT_MANUAL_CONNECT)).intValue());
    }

    private void handleDisconnected(StateChangeResult stateChangeResult) {
        WifiConfiguration config = null;
        int mAuthenticationFailuresCount = 0;
        int mAssociationRejectCount = 0;
        int mWrongkeyCount = 0;
        if (this.mNetworksDisabledDuringConnect) {
            config = this.mWifiConfigManager.getConfiguredNetwork(this.mConnectNetworkId);
        } else if (stateChangeResult != null) {
            if (DEBUG) {
                Log.d(TAG, "auto connecting id:" + stateChangeResult.networkId);
            }
            config = this.mWifiConfigManager.getConfiguredNetwork(stateChangeResult.networkId);
        }
        if (config != null) {
            NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
            if (networkStatus != null) {
                mAuthenticationFailuresCount = networkStatus.getDisableReasonCounter(3);
                mAssociationRejectCount = networkStatus.getDisableReasonCounter(2);
                mWrongkeyCount = networkStatus.getDisableReasonCounter(12);
            }
        }
        Log.d(TAG, "MC=" + this.mNetworksDisabledDuringConnect + ",mwk=" + mWrongkeyCount + ",maf=" + mAuthenticationFailuresCount + ",mar=" + mAssociationRejectCount);
        int stateChangeNetId = -1;
        int configNetId = -1;
        if (stateChangeResult != null) {
            stateChangeNetId = stateChangeResult.networkId;
        }
        if (config != null) {
            configNetId = config.networkId;
        }
        if (this.mNetworksDisabledDuringConnect) {
            if (mWrongkeyCount >= this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_MAX_RETRIES_MANUAL_WRONG_KEY_COUNT", Integer.valueOf(2)).intValue()) {
                handleNetworkConnectionFailure(stateChangeNetId, 12);
                this.mWifiConfigManager.clearDisableReasonCounter(configNetId);
            } else if (mAuthenticationFailuresCount >= this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_MAX_RETRIES_ON_AUTHENTICATION_FAILURE", Integer.valueOf(1)).intValue()) {
                handleNetworkConnectionFailure(stateChangeNetId, 3);
                this.mWifiConfigManager.clearDisableReasonCounter(configNetId);
            } else if (mAssociationRejectCount >= this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_MAX_RETRIES_MANUAL_ASSOCIATION_REJECT", Integer.valueOf(5)).intValue()) {
                handleNetworkConnectionFailure(stateChangeNetId, 2);
                this.mWifiConfigManager.clearDisableReasonCounter(configNetId);
            }
        } else if (mWrongkeyCount >= this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_MAX_RETRIES_ON_WRONG_KEY_COUNT", Integer.valueOf(4)).intValue()) {
            handleNetworkConnectionFailure(stateChangeNetId, 12);
            this.mWifiConfigManager.clearDisableReasonCounter(12, configNetId);
        } else if (mAuthenticationFailuresCount >= this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_MAX_RETRIES_ON_AUTHENTICATION_FAILURE", Integer.valueOf(1)).intValue() * 4) {
            handleNetworkConnectionFailure(stateChangeNetId, 3);
            this.mWifiConfigManager.clearDisableReasonCounter(3, configNetId);
        } else if (mAssociationRejectCount >= this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_MAX_RETRIES_ON_ASSOCIATION_REJECT", Integer.valueOf(12)).intValue()) {
            if (DEBUG) {
                Log.d(TAG, "Association getting rejected, disabling network " + stateChangeNetId);
            }
            handleNetworkConnectionFailure(stateChangeNetId, 2);
            this.mWifiConfigManager.clearDisableReasonCounter(2, configNetId);
        }
    }

    private void handleConnectCompleted() {
        if (this.mConnectingNetId != -1 && this.mConnectingNetId == this.mNewNetIdCreateByManuConnect.intValue()) {
            if (DEBUG) {
                Log.d(TAG, "mNewNetIdCreateByManuConnect:" + this.mNewNetIdCreateByManuConnect + "connected");
            }
            this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(this.mOldNetIdCreatedByManuConnect.intValue(), this.mOldWifiConfiguration, StatusCode.MCCA_TRACK_LIMIT_EXCEEDED, SupplicantState.COMPLETED);
            this.mNewNetIdCreateByManuConnect = Integer.valueOf(-1);
        }
        removeTimeoutEvent();
        this.mHandler.removeMessages(EVENT_CONNECT_NETWORK_TIMEOUT);
        this.mHandler.removeMessages(EVENT_AUTO_CONNECT_TIMEOUT);
        if (this.mNetworksDisabledDuringAutoConnect && this.mThirdAPKConnectNetworkId == this.mConnectingNetId) {
            if (DEBUG) {
                Log.d(TAG, "reset mThirdAPKConnectNetworkId!!");
            }
            this.mThirdAPKConnectNetworkId = -1;
        }
        if (this.mNetworksDisabledDuringConnect || this.mNetworksDisabledDuringAutoConnect) {
            if (this.mNetworksDisabledDuringConnect) {
                if (this.mConnectNetworkId == this.mConnectingNetId && this.mConnectNetworkId != -1) {
                    this.mWifiConfigManager.enableAllNetworks();
                    this.mNetworksDisabledDuringConnect = false;
                } else if (DEBUG) {
                    Log.d(TAG, "manual debug:munal connect in progress,should not enable all networks");
                }
            }
            if (this.mNetworksDisabledDuringAutoConnect) {
                this.mWifiConfigManager.enableAllNetworks();
                this.mNetworksDisabledDuringAutoConnect = false;
            }
        }
    }

    private void handleNetworkConnectionFailure(int netId, int disableReason) {
        if (DEBUG) {
            Log.d(TAG, "handleNetworkConnectionFailure netId=" + Integer.toString(netId) + " reason " + Integer.toString(disableReason) + " mNetworksDisabledDuringConnect=" + this.mNetworksDisabledDuringConnect);
        }
        if (this.mNetworksDisabledDuringConnect) {
            this.mWifiConfigManager.enableAllNetworks();
            if (netId == -1) {
                Log.d(TAG, "sync netId to " + this.mConnectNetworkId);
                netId = this.mConnectNetworkId;
            }
            if (this.mNewNetIdCreateByManuConnect.intValue() == netId) {
                if (DEBUG) {
                    Log.d(TAG, "hiddenap switch try failed!");
                }
                if (isConnectingExistNetwork(this.mNewNetIdCreateByManuConnect.intValue())) {
                    if (DEBUG) {
                        Log.d(TAG, "do restore network:" + netId);
                    }
                    this.mWifiConfigManager.addOrUpdateNetwork(this.mBackupWifiConfiguration, 1000);
                    this.mBackupWifiConfiguration = null;
                } else {
                    if (DEBUG) {
                        Log.d(TAG, "remove netid:" + this.mNewNetIdCreateByManuConnect);
                    }
                    this.mWifiConfigManager.disableNetwork(this.mNewNetIdCreateByManuConnect.intValue(), 1000, StatusCode.MCCA_TRACK_LIMIT_EXCEEDED);
                    this.mWifiConfigManager.removeNetwork(this.mNewNetIdCreateByManuConnect.intValue(), 1000);
                }
                this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(this.mOldNetIdCreatedByManuConnect.intValue(), this.mOldWifiConfiguration, StatusCode.MCCA_TRACK_LIMIT_EXCEEDED);
                this.mNewNetIdCreateByManuConnect = Integer.valueOf(-1);
            } else if (isConnectingExistNetwork(netId)) {
                this.mWifiConfigManager.addOrUpdateNetwork(this.mBackupWifiConfiguration, 1000);
                this.mWifiConfigManager.disableNetwork(netId, 1000, StatusCode.MCCA_TRACK_LIMIT_EXCEEDED);
                this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(netId, this.mBackupWifiConfiguration, disableReason);
                this.mBackupWifiConfiguration = null;
            } else {
                this.mWifiConfigManager.disableAndRemoveNetwork(netId, 1000, disableReason);
            }
            this.mNetworksDisabledDuringConnect = false;
            removeTimeoutEvent();
            this.mHandler.removeMessages(EVENT_CONNECT_NETWORK_TIMEOUT);
        } else if (this.mNetworksDisabledDuringAutoConnect) {
            if (DEBUG) {
                Log.d(TAG, "auto connect fail");
            }
            this.mWifiConfigManager.enableAllNetworks();
            this.mNetworksDisabledDuringAutoConnect = false;
            this.mWifiConfigManager.updateNetworkSelectionStatus(netId, disableReason);
            removeTimeoutEvent();
        } else {
            this.mWifiConfigManager.updateNetworkSelectionStatus(netId, disableReason);
        }
    }
}
