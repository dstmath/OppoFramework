package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

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
public class SupplicantStateTracker extends StateMachine {
    /* renamed from: -android-net-wifi-SupplicantStateSwitchesValues */
    private static final /* synthetic */ int[] f3-android-net-wifi-SupplicantStateSwitchesValues = null;
    private static final String ANY_BSSID = "any";
    private static final int AUTHENTICATION_FAILURE_EVENT_TIMEOUT = 1002;
    private static boolean DBG = false;
    private static final int EVENT_ASSOC_REJECT_TIMEOUT = 1000;
    private static final int EVENT_AUTO_CONNECT_TIMEOUT = 1004;
    private static final int EVENT_CONNECT_NETWORK_TIMEOUT = 1001;
    private static final int EVENT_SELECT_TIMEOUT = 1003;
    private static final String INVALID_BSSID = "00:00:00:00:00:00";
    private static int KEY_CERT = 0;
    private static int KEY_EAP = 0;
    private static int KEY_NONE = 0;
    private static int KEY_WAPI = 0;
    private static int KEY_WEP = 0;
    private static int KEY_WPA = 0;
    private static final int MAX_RETRIES_MANUAL_ASSOCIATION_REJECT = 5;
    private static final int MAX_RETRIES_ON_ASSOCIATION_REJECT = 12;
    private static final int MAX_RETRIES_ON_AUTHENTICATION_FAILURE = 1;
    private static final int MAX_WRONG_KEY_COUNT = 2;
    private static final String TAG = "SupplicantStateTracker";
    private static final int TIMEOUT_ASSOC_REJECT = 15000;
    private static final int TIMEOUT_AUTH_FAILURE = 15000;
    private static final int TIMEOUT_AUTO_CONNECT = 20000;
    private static final int TIMEOUT_MANUAL_CONNECT = 20000;
    private static final int TIMEOUT_P2P_CONNECTED_SELECT = 14000;
    private static final int TIMEOUT_SELECT = 9500;
    private static final int TIMEOUT_WRONG_KEY = 15000;
    private static final int WRONG_KEY_EVENT_TIMEOUT = 1005;
    private boolean mAddAndConnectHiddenAP;
    private boolean mAuthFailureInSupplicantBroadcast;
    private WifiConfiguration mBackupWifiConfiguration;
    private final IBatteryStats mBatteryStats;
    private final State mCompletedState;
    private boolean mConnectAlreadyExistConfigByAdd;
    private int mConnectNetworkId;
    int mConnectingNetId;
    private final Context mContext;
    private final State mDefaultState;
    private final State mDisconnectState;
    private final State mDormantState;
    private Handler mHandler;
    private final State mHandshakeState;
    private final State mInactiveState;
    private boolean mNetworksDisabledDuringAutoConnect;
    private boolean mNetworksDisabledDuringConnect;
    private Integer mNewNetIdCreateByManuConnect;
    private Integer mOldNetIdCreatedByManuConnect;
    private WifiConfiguration mOldWifiConfiguration;
    private final State mScanState;
    private int mThirdAPKConnectNetworkId;
    private final State mUninitializedState;
    private final WifiConfigManager mWifiConfigManager;
    private WifiStateMachine mWifiStateMachine;
    private int mWrongkeyCount;

    class CompletedState extends State {
        CompletedState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
            if (SupplicantStateTracker.this.mConnectingNetId == SupplicantStateTracker.this.mNewNetIdCreateByManuConnect.intValue()) {
                if (SupplicantStateTracker.DBG) {
                    Log.d(SupplicantStateTracker.TAG, "mNewNetIdCreateByManuConnect:" + SupplicantStateTracker.this.mNewNetIdCreateByManuConnect + "connected");
                }
                SupplicantStateTracker.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(SupplicantStateTracker.this.mOldNetIdCreatedByManuConnect.intValue(), SupplicantStateTracker.this.mOldWifiConfiguration, 102, SupplicantState.COMPLETED);
                SupplicantStateTracker.this.mNewNetIdCreateByManuConnect = Integer.valueOf(-1);
            }
            SupplicantStateTracker.this.mWrongkeyCount = 0;
            SupplicantStateTracker.this.removeTimeoutEvent();
            SupplicantStateTracker.this.mHandler.removeMessages(SupplicantStateTracker.EVENT_AUTO_CONNECT_TIMEOUT);
            if (SupplicantStateTracker.this.mNetworksDisabledDuringAutoConnect && SupplicantStateTracker.this.mThirdAPKConnectNetworkId == SupplicantStateTracker.this.mConnectingNetId) {
                if (SupplicantStateTracker.DBG) {
                    Log.d(SupplicantStateTracker.TAG, "reset mThirdAPKConnectNetworkId!!");
                }
                SupplicantStateTracker.this.mThirdAPKConnectNetworkId = -1;
            }
            if (SupplicantStateTracker.this.mNetworksDisabledDuringConnect || SupplicantStateTracker.this.mNetworksDisabledDuringAutoConnect) {
                if (SupplicantStateTracker.this.mNetworksDisabledDuringConnect) {
                    if (SupplicantStateTracker.this.mWifiStateMachine.getManuConnectNetId() == SupplicantStateTracker.this.mConnectingNetId) {
                        SupplicantStateTracker.this.mWifiStateMachine.setManuConnect(false);
                        SupplicantStateTracker.this.mWifiConfigManager.enableAllNetworks();
                        SupplicantStateTracker.this.mNetworksDisabledDuringConnect = false;
                    } else if (SupplicantStateTracker.DBG) {
                        Log.d(SupplicantStateTracker.TAG, "manual debug:munal connect in progress,should not enable all networks");
                    }
                }
                if (SupplicantStateTracker.this.mNetworksDisabledDuringAutoConnect) {
                    SupplicantStateTracker.this.mWifiConfigManager.enableAllNetworks();
                    SupplicantStateTracker.this.mNetworksDisabledDuringAutoConnect = false;
                }
            }
        }

        public boolean processMessage(Message message) {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + message.toString() + "\n");
            }
            switch (message.what) {
                case 131183:
                    SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(SupplicantState.DISCONNECTED, false);
                    SupplicantStateTracker.this.transitionTo(SupplicantStateTracker.this.mUninitializedState);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    SupplicantState state = stateChangeResult.state;
                    SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(state, SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast);
                    if (!SupplicantState.isConnecting(state)) {
                        SupplicantStateTracker.this.transitionOnSupplicantStateChange(stateChangeResult);
                        break;
                    }
                    break;
                case WifiMonitor.SELECT_NETWORK_EVENT /*147649*/:
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
        }

        /* JADX WARNING: Missing block: B:5:0x0035, code:
            android.util.Log.e(com.android.server.wifi.SupplicantStateTracker.TAG, "Ignoring " + r22);
     */
        /* JADX WARNING: Missing block: B:7:0x0055, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:43:0x02b6, code:
            com.android.server.wifi.SupplicantStateTracker.-set3(r21.this$0, true);
            com.android.server.wifi.SupplicantStateTracker.-get5(r21.this$0).removeMessages(com.android.server.wifi.SupplicantStateTracker.EVENT_CONNECT_NETWORK_TIMEOUT);
            com.android.server.wifi.SupplicantStateTracker.-get5(r21.this$0).removeMessages(com.android.server.wifi.SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT);
            com.android.server.wifi.SupplicantStateTracker.-get5(r21.this$0).removeMessages(com.android.server.wifi.SupplicantStateTracker.AUTHENTICATION_FAILURE_EVENT_TIMEOUT);
            com.android.server.wifi.SupplicantStateTracker.-get5(r21.this$0).removeMessages(com.android.server.wifi.SupplicantStateTracker.EVENT_AUTO_CONNECT_TIMEOUT);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(Message message) {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + message.toString() + "\n");
            }
            SupplicantStateTracker supplicantStateTracker;
            switch (message.what) {
                case 131126:
                    if (SupplicantStateTracker.DBG) {
                        Log.d(SupplicantStateTracker.TAG, "CMD_ENABLE_NETWORK");
                    }
                    SupplicantStateTracker.this.mNetworksDisabledDuringAutoConnect = true;
                    SupplicantStateTracker.this.mThirdAPKConnectNetworkId = message.arg1;
                    SupplicantStateTracker.this.mHandler.removeMessages(SupplicantStateTracker.EVENT_CONNECT_NETWORK_TIMEOUT);
                    SupplicantStateTracker.this.mHandler.removeMessages(SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT);
                    SupplicantStateTracker.this.mHandler.removeMessages(SupplicantStateTracker.AUTHENTICATION_FAILURE_EVENT_TIMEOUT);
                    SupplicantStateTracker.this.mHandler.removeMessages(SupplicantStateTracker.EVENT_AUTO_CONNECT_TIMEOUT);
                    SupplicantStateTracker.this.mHandler.sendMessageDelayed(SupplicantStateTracker.this.mHandler.obtainMessage(SupplicantStateTracker.EVENT_AUTO_CONNECT_TIMEOUT), (long) SupplicantStateTracker.this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_TIMEOUT_AUTO_CONNECT", Integer.valueOf(20000)).intValue());
                    break;
                case 131183:
                    SupplicantStateTracker.this.transitionTo(SupplicantStateTracker.this.mUninitializedState);
                    break;
                case 131372:
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    SupplicantState state = stateChangeResult.state;
                    if (!(state == SupplicantState.INACTIVE || state == SupplicantState.SCANNING || state == SupplicantState.DISCONNECTED || state == SupplicantState.INTERFACE_DISABLED)) {
                        SupplicantStateTracker.this.mHandler.removeMessages(SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT);
                    }
                    SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(state, SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast);
                    SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast = false;
                    SupplicantStateTracker.this.transitionOnSupplicantStateChange(stateChangeResult);
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT /*147463*/:
                    SupplicantStateTracker.this.removeTimeoutEvent();
                    SupplicantStateTracker.this.mHandler.sendMessageDelayed(SupplicantStateTracker.this.mHandler.obtainMessage(SupplicantStateTracker.AUTHENTICATION_FAILURE_EVENT_TIMEOUT, message.obj), 15000);
                    SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast = true;
                    break;
                case WifiMonitor.SUP_REQUEST_IDENTITY /*147471*/:
                    SupplicantStateTracker.this.handleNetworkConnectionFailure(message.arg1, 101);
                    SupplicantStateTracker.this.removeTimeoutEvent();
                    break;
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT /*147499*/:
                    int status = message.arg2;
                    int keyMgmt = SupplicantStateTracker.this.getKeymgmtType(message.obj);
                    WifiConfiguration wnassoc = SupplicantStateTracker.this.mWifiConfigManager.getWifiConfiguration(SupplicantStateTracker.this.mConnectNetworkId);
                    wnassoc = SupplicantStateTracker.this.mWifiConfigManager.getWifiConfiguration(SupplicantStateTracker.this.mConnectNetworkId);
                    if (wnassoc != null) {
                        NetworkSelectionStatus wcassoc = wnassoc.getNetworkSelectionStatus();
                        if (wcassoc != null && SupplicantStateTracker.this.mNetworksDisabledDuringConnect && SupplicantStateTracker.this.mConnectNetworkId == SupplicantStateTracker.this.mConnectingNetId && SupplicantStateTracker.this.mConnectNetworkId != -1) {
                            if (SupplicantStateTracker.DBG) {
                                Log.d(SupplicantStateTracker.TAG, "ManualConnect status: " + status + ",mWrongkeyCount= " + SupplicantStateTracker.this.mWrongkeyCount);
                            }
                            if (keyMgmt == SupplicantStateTracker.KEY_WEP && (status == 13 || status == 14 || status == 15)) {
                                supplicantStateTracker = SupplicantStateTracker.this;
                                supplicantStateTracker.mWrongkeyCount = supplicantStateTracker.mWrongkeyCount + 1;
                                wcassoc.setDisableReasonCounter(3, 0);
                                wcassoc.setDisableReasonCounter(2, 0);
                            } else {
                                wcassoc.setDisableReasonCounter(3, 0);
                                SupplicantStateTracker.this.mWrongkeyCount = 0;
                            }
                        }
                        SupplicantStateTracker.this.removeTimeoutEvent();
                        SupplicantStateTracker.this.mHandler.sendMessageDelayed(SupplicantStateTracker.this.mHandler.obtainMessage(SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT, message.obj), (long) SupplicantStateTracker.this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_TIMEOUT_ASSOC_REJECT", Integer.valueOf(15000)).intValue());
                        break;
                    }
                    break;
                case WifiMonitor.WRONG_KEY_EVENT /*147648*/:
                    if (SupplicantStateTracker.DBG) {
                        Log.d(SupplicantStateTracker.TAG, "event: wk. mmc " + SupplicantStateTracker.this.mNetworksDisabledDuringConnect);
                    }
                    SupplicantStateTracker.this.removeTimeoutEvent();
                    if (!SupplicantStateTracker.this.mNetworksDisabledDuringConnect) {
                        WifiConfiguration wc = SupplicantStateTracker.this.mWifiConfigManager.getWifiConfiguration(SupplicantStateTracker.this.mConnectingNetId);
                        if (wc == null) {
                            if (SupplicantStateTracker.DBG) {
                                Log.d(SupplicantStateTracker.TAG, "mWifiConfigManager.getWifiConfiguration(mConnectNetworkId) is null");
                                break;
                            }
                        }
                        NetworkSelectionStatus wn = wc.getNetworkSelectionStatus();
                        if (wn == null) {
                            if (SupplicantStateTracker.DBG) {
                                Log.d(SupplicantStateTracker.TAG, "don't incrementDisableReasonCounter DISABLED_AUTHENTICATION_FAILURE");
                                break;
                            }
                        }
                        wn.incrementDisableReasonCounter(3);
                        break;
                    }
                    if (SupplicantStateTracker.this.mConnectNetworkId == SupplicantStateTracker.this.mConnectingNetId && SupplicantStateTracker.this.mConnectNetworkId != -1) {
                        supplicantStateTracker = SupplicantStateTracker.this;
                        supplicantStateTracker.mWrongkeyCount = supplicantStateTracker.mWrongkeyCount + 1;
                        if (SupplicantStateTracker.DBG) {
                            Log.d(SupplicantStateTracker.TAG, "event: wkc " + SupplicantStateTracker.this.mWrongkeyCount);
                        }
                        SupplicantStateTracker.this.mHandler.sendMessageDelayed(SupplicantStateTracker.this.mHandler.obtainMessage(SupplicantStateTracker.WRONG_KEY_EVENT_TIMEOUT), 15000);
                        break;
                    }
                    break;
                case WifiMonitor.SELECT_NETWORK_EVENT /*147649*/:
                    SupplicantStateTracker.this.removeTimeoutEvent();
                    SupplicantStateTracker.this.mHandler.sendMessageDelayed(SupplicantStateTracker.this.mHandler.obtainMessage(SupplicantStateTracker.EVENT_SELECT_TIMEOUT, message.arg1, 0), (long) (message.arg2 == 1 ? SupplicantStateTracker.TIMEOUT_P2P_CONNECTED_SELECT : SupplicantStateTracker.TIMEOUT_SELECT));
                    break;
                case 151553:
                    SupplicantStateTracker.this.mNetworksDisabledDuringConnect = true;
                    SupplicantStateTracker.this.mConnectNetworkId = Integer.valueOf(message.arg1).intValue();
                    SupplicantStateTracker.this.mThirdAPKConnectNetworkId = -1;
                    Log.d(SupplicantStateTracker.TAG, "mConnectNetworkId=" + SupplicantStateTracker.this.mConnectNetworkId + ",mConnectingNetId=" + SupplicantStateTracker.this.mConnectingNetId);
                    if (SupplicantStateTracker.this.mConnectNetworkId != -1) {
                        WifiConfiguration wna = SupplicantStateTracker.this.mWifiConfigManager.getWifiConfiguration(SupplicantStateTracker.this.mConnectNetworkId);
                        if (wna != null) {
                            NetworkSelectionStatus wnaState = wna.getNetworkSelectionStatus();
                            if (wnaState != null) {
                                wnaState.clearDisableReasonCounter();
                            }
                        }
                    }
                    SupplicantStateTracker.this.mWrongkeyCount = 0;
                    SupplicantStateTracker.this.mNetworksDisabledDuringAutoConnect = false;
                    SupplicantStateTracker.this.mHandler.removeMessages(SupplicantStateTracker.EVENT_AUTO_CONNECT_TIMEOUT);
                    SupplicantStateTracker.this.mHandler.removeMessages(SupplicantStateTracker.EVENT_CONNECT_NETWORK_TIMEOUT);
                    SupplicantStateTracker.this.mHandler.removeMessages(SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT);
                    SupplicantStateTracker.this.mHandler.removeMessages(SupplicantStateTracker.AUTHENTICATION_FAILURE_EVENT_TIMEOUT);
                    SupplicantStateTracker.this.mHandler.sendMessageDelayed(SupplicantStateTracker.this.mHandler.obtainMessage(SupplicantStateTracker.EVENT_CONNECT_NETWORK_TIMEOUT, Integer.valueOf(message.arg1)), (long) SupplicantStateTracker.this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_TIMEOUT_MANUAL_CONNECT", Integer.valueOf(20000)).intValue());
                    break;
            }
        }
    }

    class DisconnectedState extends State {
        DisconnectedState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
            WifiConfiguration config = null;
            StateChangeResult stateChangeResult = SupplicantStateTracker.this.getCurrentMessage().obj;
            NetworkSelectionStatus networkStatus = null;
            int mAuthenticationFailuresCount = 0;
            int mAssociationRejectCount = 0;
            if (SupplicantStateTracker.this.mNetworksDisabledDuringConnect) {
                config = SupplicantStateTracker.this.mWifiConfigManager.getWifiConfiguration(SupplicantStateTracker.this.mConnectNetworkId);
            } else if (stateChangeResult != null) {
                if (SupplicantStateTracker.DBG) {
                    Log.d(SupplicantStateTracker.TAG, "auto connecting id:" + stateChangeResult.networkId);
                }
                config = SupplicantStateTracker.this.mWifiConfigManager.getWifiConfiguration(stateChangeResult.networkId);
            }
            if (config != null) {
                networkStatus = config.getNetworkSelectionStatus();
                if (networkStatus != null) {
                    mAuthenticationFailuresCount = networkStatus.getDisableReasonCounter(3);
                    mAssociationRejectCount = networkStatus.getDisableReasonCounter(2);
                }
            }
            Log.d(SupplicantStateTracker.TAG, "MC=" + SupplicantStateTracker.this.mNetworksDisabledDuringConnect + ",mwk=" + SupplicantStateTracker.this.mWrongkeyCount + ",maf=" + mAuthenticationFailuresCount + ",mar=" + mAssociationRejectCount);
            if (SupplicantStateTracker.this.mNetworksDisabledDuringConnect) {
                if (SupplicantStateTracker.this.mWrongkeyCount >= SupplicantStateTracker.this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_MAX_WRONG_KEY_COUNT", Integer.valueOf(2)).intValue()) {
                    SupplicantStateTracker.this.handleNetworkConnectionFailure(stateChangeResult.networkId, 101);
                    if (networkStatus != null) {
                        networkStatus.clearDisableReasonCounter();
                    }
                } else if (mAuthenticationFailuresCount >= SupplicantStateTracker.this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_MAX_RETRIES_ON_AUTHENTICATION_FAILURE", Integer.valueOf(1)).intValue()) {
                    SupplicantStateTracker.this.handleNetworkConnectionFailure(stateChangeResult.networkId, 3);
                    if (networkStatus != null) {
                        networkStatus.clearDisableReasonCounter();
                    }
                } else if (mAssociationRejectCount >= SupplicantStateTracker.this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_MAX_RETRIES_MANUAL_ASSOCIATION_REJECT", Integer.valueOf(5)).intValue()) {
                    SupplicantStateTracker.this.handleNetworkConnectionFailure(stateChangeResult.networkId, 2);
                    if (networkStatus != null) {
                        networkStatus.clearDisableReasonCounter();
                    }
                } else if (SupplicantStateTracker.this.mWifiStateMachine.isOtherNetworksEnabledDuringManuConnect()) {
                    if (SupplicantStateTracker.DBG) {
                        Log.d(SupplicantStateTracker.TAG, "other networks enabled,disable all others and reconnect");
                    }
                    SupplicantStateTracker.this.mWifiConfigManager.disableAllNetworksNative();
                    SupplicantStateTracker.this.mWifiConfigManager.enableNetwork(SupplicantStateTracker.this.mWifiConfigManager.getConfiguredNetwork(SupplicantStateTracker.this.mWifiStateMachine.getManuConnectNetId()), true, SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT);
                    SupplicantStateTracker.this.mWifiStateMachine.reconnectCommand();
                }
            } else if (mAuthenticationFailuresCount >= SupplicantStateTracker.this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_MAX_RETRIES_ON_AUTHENTICATION_FAILURE", Integer.valueOf(1)).intValue() * 4) {
                SupplicantStateTracker.this.handleNetworkConnectionFailure(stateChangeResult.networkId, 3);
                if (networkStatus != null) {
                    networkStatus.clearDisableReasonCounter(3);
                }
            } else if (mAssociationRejectCount >= SupplicantStateTracker.this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_MAX_RETRIES_ON_ASSOCIATION_REJECT", Integer.valueOf(12)).intValue()) {
                if (stateChangeResult != null && SupplicantStateTracker.DBG) {
                    Log.d(SupplicantStateTracker.TAG, "Association getting rejected, disabling network " + stateChangeResult.networkId);
                }
                SupplicantStateTracker.this.handleNetworkConnectionFailure(stateChangeResult.networkId, 2);
                if (networkStatus != null) {
                    networkStatus.clearDisableReasonCounter(2);
                }
            }
        }
    }

    class DormantState extends State {
        DormantState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
        }
    }

    private final class H extends Handler {
        public H(Looper lp) {
            super(lp);
        }

        public void handleMessage(Message msg) {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, "Connect timeout, msg.event :" + msg.what + ",msg.arg1: " + msg.arg1 + ", netId = " + SupplicantStateTracker.this.mConnectNetworkId + ", enableAP " + SupplicantStateTracker.this.mNetworksDisabledDuringConnect);
            }
            boolean assocReject = false;
            switch (msg.what) {
                case SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT /*1000*/:
                    assocReject = true;
                    break;
                case SupplicantStateTracker.EVENT_CONNECT_NETWORK_TIMEOUT /*1001*/:
                    if (SupplicantStateTracker.this.mNetworksDisabledDuringConnect) {
                        SupplicantStateTracker.this.mWifiConfigManager.enableAllNetworks();
                        SupplicantStateTracker.this.mNetworksDisabledDuringConnect = false;
                        SupplicantStateTracker.this.mWifiStateMachine.setManuConnect(false);
                        return;
                    }
                    return;
                case SupplicantStateTracker.AUTHENTICATION_FAILURE_EVENT_TIMEOUT /*1002*/:
                    break;
                case SupplicantStateTracker.EVENT_SELECT_TIMEOUT /*1003*/:
                    if (msg != null) {
                        int netId = msg.arg1;
                        WifiConfiguration hidden = SupplicantStateTracker.this.mWifiConfigManager.getWifiConfiguration(netId);
                        if (hidden == null) {
                            if (SupplicantStateTracker.DBG) {
                                Log.d(SupplicantStateTracker.TAG, "hidden == null");
                                return;
                            }
                            return;
                        } else if (SupplicantStateTracker.this.mNetworksDisabledDuringConnect) {
                            SupplicantStateTracker.this.mWifiConfigManager.enableAllNetworks();
                            boolean noNeedRemove = SupplicantStateTracker.this.isConnectingExistNetwork(netId);
                            boolean found = SupplicantStateTracker.this.isNetworkExistInScanResults(netId);
                            int reason;
                            if (!SupplicantStateTracker.this.getAddAndConnectHiddenAp()) {
                                if (found) {
                                    reason = 3;
                                } else {
                                    reason = 102;
                                }
                                SupplicantStateTracker.this.mWifiConfigManager.disableAndRemoveNetwork(netId, reason);
                                SupplicantStateTracker.this.mNetworksDisabledDuringConnect = false;
                                SupplicantStateTracker.this.mWifiStateMachine.setManuConnect(false);
                                return;
                            } else if (SupplicantStateTracker.this.mNewNetIdCreateByManuConnect.intValue() != netId) {
                                if (SupplicantStateTracker.DBG) {
                                    Log.d(SupplicantStateTracker.TAG, "hiddenap try another keymgmt");
                                }
                                boolean isWep = SupplicantStateTracker.this.isWepAP(hidden);
                                boolean isWpa = SupplicantStateTracker.this.isWpaAP(hidden);
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
                                                reason = 102;
                                            }
                                            if (noNeedRemove) {
                                                SupplicantStateTracker.this.mWifiConfigManager.disableNetwork(hidden.networkId, reason);
                                                SupplicantStateTracker.this.mWifiConfigManager.addOrUpdateNetwork(SupplicantStateTracker.this.mBackupWifiConfiguration, SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT);
                                                SupplicantStateTracker.this.mBackupWifiConfiguration = null;
                                                SupplicantStateTracker.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(netId, hidden, reason);
                                            } else {
                                                SupplicantStateTracker.this.mWifiConfigManager.disableAndRemoveNetwork(netId, reason);
                                            }
                                            SupplicantStateTracker.this.mNetworksDisabledDuringConnect = false;
                                            SupplicantStateTracker.this.mWifiStateMachine.setManuConnect(false);
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
                                        if (SupplicantStateTracker.DBG) {
                                            Log.d(SupplicantStateTracker.TAG, "hiddenap try wpa psk:" + newConfig.preSharedKey);
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
                                                reason = 102;
                                            }
                                            if (noNeedRemove) {
                                                SupplicantStateTracker.this.mWifiConfigManager.disableNetwork(hidden.networkId, reason);
                                                SupplicantStateTracker.this.mWifiConfigManager.addOrUpdateNetwork(SupplicantStateTracker.this.mBackupWifiConfiguration, SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT);
                                                SupplicantStateTracker.this.mBackupWifiConfiguration = null;
                                                SupplicantStateTracker.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(netId, hidden, reason);
                                            } else {
                                                SupplicantStateTracker.this.mWifiConfigManager.disableAndRemoveNetwork(netId, reason);
                                            }
                                            SupplicantStateTracker.this.mNetworksDisabledDuringConnect = false;
                                            SupplicantStateTracker.this.mWifiStateMachine.setManuConnect(false);
                                            return;
                                        }
                                        pwd = pwd.substring(1, pwd.length() - 1);
                                        int length = pwd.length();
                                        if ((length == 10 || length == 26 || length == 58) && pwd.matches("[0-9A-Fa-f]*")) {
                                            newConfig.wepKeys[newConfig.wepTxKeyIndex] = pwd;
                                        } else {
                                            newConfig.wepKeys[newConfig.wepTxKeyIndex] = '\"' + pwd + '\"';
                                        }
                                        if (SupplicantStateTracker.DBG) {
                                            Log.d(SupplicantStateTracker.TAG, "hiddenap try wep wepKeys:" + newConfig.wepKeys[newConfig.wepTxKeyIndex]);
                                        }
                                    }
                                    if (noNeedRemove) {
                                        SupplicantStateTracker.this.mWifiConfigManager.addOrUpdateNetwork(SupplicantStateTracker.this.mBackupWifiConfiguration, SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT);
                                        SupplicantStateTracker.this.mBackupWifiConfiguration = null;
                                    }
                                    WifiConfiguration savedConfig = SupplicantStateTracker.this.mWifiConfigManager.getWifiConfiguration(newConfig.configKey());
                                    if (savedConfig != null) {
                                        SupplicantStateTracker.this.setLastUpdatedWifiConfiguration(savedConfig);
                                        SupplicantStateTracker.this.mWifiConfigManager.disableNetwork(savedConfig.networkId);
                                    }
                                    int newNetId = SupplicantStateTracker.this.mWifiConfigManager.addOrUpdateNetwork(newConfig, SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT);
                                    if (newNetId == -1) {
                                        if (SupplicantStateTracker.DBG) {
                                            Log.d(SupplicantStateTracker.TAG, "add new network failed!");
                                        }
                                        if (found) {
                                            reason = 3;
                                        } else {
                                            reason = 102;
                                        }
                                        if (noNeedRemove) {
                                            SupplicantStateTracker.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(netId, hidden, reason);
                                        } else {
                                            if (SupplicantStateTracker.DBG) {
                                                Log.d(SupplicantStateTracker.TAG, "netId:" + netId + "not found,remove it");
                                            }
                                            SupplicantStateTracker.this.mWifiConfigManager.disableAndRemoveNetwork(netId, reason);
                                        }
                                        if (savedConfig != null) {
                                            SupplicantStateTracker.this.mWifiConfigManager.enableNetwork(savedConfig, false, SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT);
                                        }
                                        SupplicantStateTracker.this.mBackupWifiConfiguration = null;
                                        SupplicantStateTracker.this.mNetworksDisabledDuringConnect = false;
                                        SupplicantStateTracker.this.mWifiStateMachine.setManuConnect(false);
                                        return;
                                    }
                                    Message mMsg = SupplicantStateTracker.this.mHandler.obtainMessage(151553, -1, 0, newConfig);
                                    mMsg.sendingUid = SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT;
                                    SupplicantStateTracker.this.mWifiStateMachine.sendMessage(mMsg);
                                    SupplicantStateTracker.this.mOldNetIdCreatedByManuConnect = Integer.valueOf(netId);
                                    SupplicantStateTracker.this.mOldWifiConfiguration = SupplicantStateTracker.this.mWifiConfigManager.getWifiConfiguration(SupplicantStateTracker.this.mOldNetIdCreatedByManuConnect.intValue());
                                    SupplicantStateTracker.this.mNewNetIdCreateByManuConnect = Integer.valueOf(newNetId);
                                    if (noNeedRemove) {
                                        if (SupplicantStateTracker.DBG) {
                                            Log.d(SupplicantStateTracker.TAG, "disable mOldNetIdCreatedByManuConnect:" + SupplicantStateTracker.this.mOldNetIdCreatedByManuConnect);
                                        }
                                        SupplicantStateTracker.this.mWifiConfigManager.disableNetwork(netId);
                                        return;
                                    }
                                    if (SupplicantStateTracker.DBG) {
                                        Log.d(SupplicantStateTracker.TAG, "mOldNetIdCreatedByManuConnect:" + SupplicantStateTracker.this.mOldNetIdCreatedByManuConnect + "not found,forget it");
                                    }
                                    SupplicantStateTracker.this.mWifiConfigManager.forgetNetwork(SupplicantStateTracker.this.mOldNetIdCreatedByManuConnect.intValue());
                                    return;
                                }
                                if (found) {
                                    reason = 3;
                                } else {
                                    reason = 102;
                                }
                                SupplicantStateTracker.this.mWifiConfigManager.disableAndRemoveNetwork(netId, reason);
                                SupplicantStateTracker.this.mNetworksDisabledDuringConnect = false;
                                SupplicantStateTracker.this.mWifiStateMachine.setManuConnect(false);
                                return;
                            } else {
                                Log.d(SupplicantStateTracker.TAG, "hiddenap switch try failed!");
                                if (noNeedRemove) {
                                    if (SupplicantStateTracker.DBG) {
                                        Log.d(SupplicantStateTracker.TAG, "netId " + netId + " is already in configednetworks,do restore");
                                    }
                                    SupplicantStateTracker.this.mWifiConfigManager.disableNetwork(SupplicantStateTracker.this.mNewNetIdCreateByManuConnect.intValue(), 102);
                                    SupplicantStateTracker.this.mWifiConfigManager.addOrUpdateNetwork(SupplicantStateTracker.this.mBackupWifiConfiguration, SupplicantStateTracker.EVENT_ASSOC_REJECT_TIMEOUT);
                                    SupplicantStateTracker.this.mBackupWifiConfiguration = null;
                                } else {
                                    if (SupplicantStateTracker.DBG) {
                                        Log.d(SupplicantStateTracker.TAG, "do remove netid:" + SupplicantStateTracker.this.mNewNetIdCreateByManuConnect);
                                    }
                                    SupplicantStateTracker.this.mWifiConfigManager.disableNetwork(SupplicantStateTracker.this.mNewNetIdCreateByManuConnect.intValue(), 102);
                                    SupplicantStateTracker.this.mWifiConfigManager.forgetNetwork(SupplicantStateTracker.this.mNewNetIdCreateByManuConnect.intValue());
                                }
                                found = SupplicantStateTracker.this.isNetworkExistInScanResults(SupplicantStateTracker.this.mOldWifiConfiguration);
                                if (SupplicantStateTracker.this.isWepAP(SupplicantStateTracker.this.mOldWifiConfiguration)) {
                                    if (found) {
                                        reason = 101;
                                    } else {
                                        reason = 102;
                                    }
                                } else if (found) {
                                    reason = 3;
                                } else {
                                    reason = 102;
                                }
                                SupplicantStateTracker.this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(SupplicantStateTracker.this.mOldNetIdCreatedByManuConnect.intValue(), SupplicantStateTracker.this.mOldWifiConfiguration, reason);
                                SupplicantStateTracker.this.mNewNetIdCreateByManuConnect = Integer.valueOf(-1);
                                SupplicantStateTracker.this.mNetworksDisabledDuringConnect = false;
                                SupplicantStateTracker.this.mWifiStateMachine.setManuConnect(false);
                                return;
                            }
                        } else if (SupplicantStateTracker.this.mNetworksDisabledDuringAutoConnect) {
                            SupplicantStateTracker.this.mWifiConfigManager.enableAllNetworks();
                            SupplicantStateTracker.this.mNetworksDisabledDuringAutoConnect = false;
                            return;
                        } else {
                            return;
                        }
                    }
                    return;
                case SupplicantStateTracker.EVENT_AUTO_CONNECT_TIMEOUT /*1004*/:
                    if (SupplicantStateTracker.this.mNetworksDisabledDuringAutoConnect) {
                        SupplicantStateTracker.this.mWifiConfigManager.enableAllNetworks();
                        if (SupplicantStateTracker.DBG) {
                            Log.d(SupplicantStateTracker.TAG, "mThirdAPKConnectNetworkId=" + SupplicantStateTracker.this.mThirdAPKConnectNetworkId);
                        }
                        SupplicantStateTracker.this.mWifiConfigManager.disableNetwork(SupplicantStateTracker.this.mThirdAPKConnectNetworkId, 102);
                        SupplicantStateTracker.this.mThirdAPKConnectNetworkId = -1;
                        SupplicantStateTracker.this.mNetworksDisabledDuringAutoConnect = false;
                        return;
                    }
                    return;
                case SupplicantStateTracker.WRONG_KEY_EVENT_TIMEOUT /*1005*/:
                    if (SupplicantStateTracker.this.mNetworksDisabledDuringConnect) {
                        SupplicantStateTracker.this.mWifiConfigManager.enableAllNetworks();
                        SupplicantStateTracker.this.mWifiConfigManager.disableAndRemoveNetwork(SupplicantStateTracker.this.mConnectNetworkId, 101);
                        SupplicantStateTracker.this.mWrongkeyCount = 0;
                        WifiConfiguration config = SupplicantStateTracker.this.mWifiConfigManager.getWifiConfiguration(SupplicantStateTracker.this.mConnectNetworkId);
                        if (config != null) {
                            NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
                            if (networkStatus != null) {
                                networkStatus.clearDisableReasonCounter();
                            }
                        }
                        SupplicantStateTracker.this.mNetworksDisabledDuringConnect = false;
                        SupplicantStateTracker.this.mWifiStateMachine.setManuConnect(false);
                        return;
                    }
                    return;
                default:
                    return;
            }
            if (SupplicantStateTracker.this.mNetworksDisabledDuringConnect || SupplicantStateTracker.this.mNetworksDisabledDuringAutoConnect) {
                SupplicantStateTracker.this.mWifiConfigManager.enableAllNetworks();
                if (SupplicantStateTracker.this.mNetworksDisabledDuringConnect) {
                    if (assocReject) {
                        SupplicantStateTracker.this.mWifiConfigManager.disableAndRemoveNetwork(SupplicantStateTracker.this.mConnectNetworkId, 2);
                    } else {
                        SupplicantStateTracker.this.mWifiConfigManager.disableAndRemoveNetwork(SupplicantStateTracker.this.mConnectNetworkId, 3);
                    }
                }
                SupplicantStateTracker.this.mNetworksDisabledDuringConnect = false;
                SupplicantStateTracker.this.mNetworksDisabledDuringAutoConnect = false;
                SupplicantStateTracker.this.mWifiStateMachine.setManuConnect(false);
            }
        }
    }

    class HandshakeState extends State {
        private static final int MAX_SUPPLICANT_LOOP_ITERATIONS = 4;
        private int mLoopDetectCount;
        private int mLoopDetectIndex;

        HandshakeState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
            this.mLoopDetectIndex = 0;
            this.mLoopDetectCount = 0;
        }

        public boolean processMessage(Message message) {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + message.toString() + "\n");
            }
            switch (message.what) {
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT /*147462*/:
                    StateChangeResult stateChangeResult = message.obj;
                    SupplicantState state = stateChangeResult.state;
                    if (SupplicantState.isHandshakeState(state)) {
                        if (this.mLoopDetectIndex > state.ordinal()) {
                            this.mLoopDetectCount++;
                        }
                        if (this.mLoopDetectCount > 4) {
                            Log.d(SupplicantStateTracker.TAG, "Supplicant loop detected, disabling network " + stateChangeResult.networkId);
                            SupplicantStateTracker.this.handleNetworkConnectionFailure(stateChangeResult.networkId, 3);
                        }
                        this.mLoopDetectIndex = state.ordinal();
                        SupplicantStateTracker.this.sendSupplicantStateChangedBroadcast(state, SupplicantStateTracker.this.mAuthFailureInSupplicantBroadcast);
                        break;
                    }
                    return false;
                case WifiMonitor.SELECT_NETWORK_EVENT /*147649*/:
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    class InactiveState extends State {
        InactiveState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
        }
    }

    class ScanState extends State {
        ScanState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
        }
    }

    class UninitializedState extends State {
        UninitializedState() {
        }

        public void enter() {
            if (SupplicantStateTracker.DBG) {
                Log.d(SupplicantStateTracker.TAG, getName() + "\n");
            }
            SupplicantStateTracker.this.removeTimeoutEvent();
        }
    }

    /* renamed from: -getandroid-net-wifi-SupplicantStateSwitchesValues */
    private static /* synthetic */ int[] m3-getandroid-net-wifi-SupplicantStateSwitchesValues() {
        if (f3-android-net-wifi-SupplicantStateSwitchesValues != null) {
            return f3-android-net-wifi-SupplicantStateSwitchesValues;
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
        f3-android-net-wifi-SupplicantStateSwitchesValues = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.SupplicantStateTracker.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.SupplicantStateTracker.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.SupplicantStateTracker.<clinit>():void");
    }

    public void setAddAndConnectHiddenAp(boolean val) {
        if (DBG) {
            Log.d(TAG, "setAddAndConnectHiddenAp=" + val);
        }
        this.mAddAndConnectHiddenAP = val;
    }

    public boolean getAddAndConnectHiddenAp() {
        if (DBG) {
            Log.d(TAG, "getAddAndConnectHiddenAp=" + this.mAddAndConnectHiddenAP);
        }
        return this.mAddAndConnectHiddenAP;
    }

    public void setConnectAlreadyExistConfigByAdd(boolean val) {
        if (DBG) {
            Log.d(TAG, "setConnectAlreadyExistConfigByManual:" + val);
        }
        this.mConnectAlreadyExistConfigByAdd = val;
    }

    public void setLastUpdatedWifiConfiguration(WifiConfiguration wc) {
        if (DBG) {
            Log.d(TAG, "setLastUpdatedWifiConfiguration");
        }
        this.mBackupWifiConfiguration = new WifiConfiguration(wc);
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
        return isNetworkExistInScanResults(this.mWifiConfigManager.getWifiConfiguration(netid));
    }

    private boolean isWepAP(WifiConfiguration wc) {
        if (wc != null && wc.allowedKeyManagement.get(0) && wc.wepTxKeyIndex >= 0 && wc.wepTxKeyIndex < wc.wepKeys.length && wc.wepKeys[wc.wepTxKeyIndex] != null) {
            return true;
        }
        return false;
    }

    private boolean isWepAP(int netid) {
        return isWepAP(this.mWifiConfigManager.getWifiConfiguration(netid));
    }

    private boolean isWpaAP(WifiConfiguration wc) {
        if (wc == null) {
            return false;
        }
        return wc.allowedKeyManagement.get(1) || wc.allowedKeyManagement.get(4);
    }

    private boolean isWpaAP(int netid) {
        return isWpaAP(this.mWifiConfigManager.getWifiConfiguration(netid));
    }

    public boolean needSaveAsHiddenAP(WifiConfiguration wconf) {
        if (wconf == null) {
            if (DBG) {
                Log.d(TAG, "unexpected: wconf is null");
            }
            return false;
        } else if (wconf.hiddenSSID) {
            if (DBG) {
                Log.d(TAG, wconf.SSID + " is already set as hidden AP");
            }
            return false;
        } else if (isNetworkExistInScanResults(wconf)) {
            String bssid = wconf.BSSID;
            if (bssid == null || INVALID_BSSID.equals(bssid) || "any".equals(bssid)) {
                if (DBG) {
                    Log.d(TAG, "bssid is null or invalid!");
                }
                return false;
            }
            String configKey = wconf.configKey();
            if (configKey == null) {
                if (DBG) {
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
                            if (DBG) {
                                Log.d(TAG, "BSID:" + bssid + " is a hidden ap!");
                            }
                            return true;
                        }
                    }
                }
            } else if (DBG) {
                Log.d(TAG, "unexpected:scanList is null");
            }
            return false;
        } else {
            if (DBG) {
                Log.d(TAG, "network is not exist in scan results!");
            }
            return true;
        }
    }

    public void handleSSIDStateChangedCB(int netId, int reason) {
        if (DBG) {
            Log.d(TAG, "handleSSIDStateChangedCB netId:" + netId + " reason:" + reason);
        }
        if (this.mNetworksDisabledDuringConnect) {
            switch (reason) {
                case 2:
                    this.mHandler.removeMessages(EVENT_ASSOC_REJECT_TIMEOUT);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_ASSOC_REJECT_TIMEOUT), 15000);
                    return;
                case 3:
                    this.mHandler.removeMessages(AUTHENTICATION_FAILURE_EVENT_TIMEOUT);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(AUTHENTICATION_FAILURE_EVENT_TIMEOUT), 15000);
                    return;
                case 101:
                    this.mHandler.removeMessages(WRONG_KEY_EVENT_TIMEOUT);
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(WRONG_KEY_EVENT_TIMEOUT), 15000);
                    return;
                default:
                    return;
            }
        }
    }

    public void setWifiState(int state) {
        if (DBG) {
            Log.d(TAG, "setWifiState to: " + state);
        }
        if (state == 1) {
            this.mNetworksDisabledDuringConnect = false;
            this.mNetworksDisabledDuringAutoConnect = false;
            this.mThirdAPKConnectNetworkId = -1;
        }
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
    }

    public String getSupplicantStateName() {
        return getCurrentState().getName();
    }

    public SupplicantStateTracker(Context c, WifiConfigManager wcs, Handler t) {
        super(TAG, t.getLooper());
        this.mAuthFailureInSupplicantBroadcast = false;
        this.mConnectNetworkId = -1;
        this.mThirdAPKConnectNetworkId = -1;
        this.mWrongkeyCount = 0;
        this.mNetworksDisabledDuringAutoConnect = false;
        this.mNetworksDisabledDuringConnect = false;
        this.mConnectAlreadyExistConfigByAdd = false;
        this.mBackupWifiConfiguration = null;
        this.mOldNetIdCreatedByManuConnect = Integer.valueOf(-1);
        this.mOldWifiConfiguration = null;
        this.mNewNetIdCreateByManuConnect = Integer.valueOf(-1);
        this.mAddAndConnectHiddenAP = false;
        this.mUninitializedState = new UninitializedState();
        this.mDefaultState = new DefaultState();
        this.mInactiveState = new InactiveState();
        this.mDisconnectState = new DisconnectedState();
        this.mScanState = new ScanState();
        this.mHandshakeState = new HandshakeState();
        this.mCompletedState = new CompletedState();
        this.mDormantState = new DormantState();
        this.mConnectingNetId = -1;
        this.mContext = c;
        this.mWifiConfigManager = wcs;
        this.mBatteryStats = (IBatteryStats) ServiceManager.getService("batterystats");
        this.mHandler = new H(t.getLooper());
        addState(this.mDefaultState);
        addState(this.mUninitializedState, this.mDefaultState);
        addState(this.mInactiveState, this.mDefaultState);
        addState(this.mDisconnectState, this.mDefaultState);
        addState(this.mScanState, this.mDefaultState);
        addState(this.mHandshakeState, this.mDefaultState);
        addState(this.mCompletedState, this.mDefaultState);
        addState(this.mDormantState, this.mDefaultState);
        setInitialState(this.mUninitializedState);
        setLogRecSize(50);
        setLogOnlyTransitions(true);
        start();
    }

    public void setSupplicantWifistatemachine(WifiStateMachine wsm) {
        this.mWifiStateMachine = wsm;
    }

    private void handleNetworkConnectionFailure(int netId, int disableReason) {
        if (DBG) {
            Log.d(TAG, "handleNetworkConnectionFailure netId=" + Integer.toString(netId) + " reason " + Integer.toString(disableReason) + " mNetworksDisabledDuringConnect=" + this.mNetworksDisabledDuringConnect);
        }
        if (this.mNetworksDisabledDuringConnect) {
            this.mWifiConfigManager.enableAllNetworks();
            if (this.mNewNetIdCreateByManuConnect.intValue() == netId) {
                if (DBG) {
                    Log.d(TAG, "hiddenap switch try failed!");
                }
                if (isConnectingExistNetwork(this.mNewNetIdCreateByManuConnect.intValue())) {
                    if (DBG) {
                        Log.d(TAG, "do restore network:" + netId);
                    }
                    this.mWifiConfigManager.addOrUpdateNetwork(this.mBackupWifiConfiguration, EVENT_ASSOC_REJECT_TIMEOUT);
                    this.mBackupWifiConfiguration = null;
                } else {
                    if (DBG) {
                        Log.d(TAG, "remove netid:" + this.mNewNetIdCreateByManuConnect);
                    }
                    this.mWifiConfigManager.disableNetwork(this.mNewNetIdCreateByManuConnect.intValue(), 102);
                    this.mWifiConfigManager.forgetNetwork(this.mNewNetIdCreateByManuConnect.intValue());
                }
                this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(this.mOldNetIdCreatedByManuConnect.intValue(), this.mOldWifiConfiguration, 102);
                this.mNewNetIdCreateByManuConnect = Integer.valueOf(-1);
            } else if (isConnectingExistNetwork(netId)) {
                this.mWifiConfigManager.addOrUpdateNetwork(this.mBackupWifiConfiguration, EVENT_ASSOC_REJECT_TIMEOUT);
                this.mWifiConfigManager.disableNetwork(netId, 102);
                this.mWifiConfigManager.sendAlertNetworksChangedBroadcast(netId, this.mBackupWifiConfiguration, disableReason);
                this.mBackupWifiConfiguration = null;
            } else {
                this.mWifiConfigManager.disableAndRemoveNetwork(netId, disableReason);
            }
            this.mNetworksDisabledDuringConnect = false;
            this.mWifiStateMachine.setManuConnect(false);
            removeTimeoutEvent();
        } else if (this.mNetworksDisabledDuringAutoConnect) {
            if (DBG) {
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

    private void transitionOnSupplicantStateChange(StateChangeResult stateChangeResult) {
        SupplicantState supState = stateChangeResult.state;
        if (DBG) {
            Log.d(TAG, "Supplicant state: " + supState.toString() + "\n");
        }
        if (stateChangeResult != null) {
            if (stateChangeResult.networkId != -1) {
                this.mConnectingNetId = stateChangeResult.networkId;
                if (DBG) {
                    Log.d(TAG, "set conncting net id=" + this.mConnectingNetId);
                }
            } else if (this.mNetworksDisabledDuringConnect) {
                Log.d(TAG, "invalid netid for manu connect,ignore!!");
            } else {
                this.mConnectingNetId = stateChangeResult.networkId;
                if (DBG) {
                    Log.d(TAG, "set conncting net id=" + this.mConnectingNetId);
                }
            }
        }
        switch (m3-getandroid-net-wifi-SupplicantStateSwitchesValues()[supState.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 7:
            case 8:
                if (!this.mWifiStateMachine.getManuConnect() || this.mConnectingNetId == this.mWifiStateMachine.getManuConnectNetId()) {
                    this.mHandler.removeMessages(EVENT_CONNECT_NETWORK_TIMEOUT);
                    this.mHandler.removeMessages(EVENT_SELECT_TIMEOUT);
                    this.mHandler.removeMessages(WRONG_KEY_EVENT_TIMEOUT);
                    transitionTo(this.mHandshakeState);
                    return;
                }
                if (DBG) {
                    Log.d(TAG, "manual debug:got autoconnect supplicant evt,do not remove timeout timer");
                }
                transitionTo(this.mHandshakeState);
                return;
            case 4:
                transitionTo(this.mCompletedState);
                return;
            case 5:
                transitionTo(this.mDisconnectState);
                return;
            case 6:
                transitionTo(this.mDormantState);
                return;
            case 9:
                transitionTo(this.mInactiveState);
                return;
            case 10:
                return;
            case 11:
            case 13:
                transitionTo(this.mUninitializedState);
                return;
            case 12:
                transitionTo(this.mScanState);
                return;
            default:
                Log.e(TAG, "Unknown supplicant state " + supState);
                return;
        }
    }

    private void sendSupplicantStateChangedBroadcast(SupplicantState state, boolean failedAuth) {
        int supplState;
        switch (m3-getandroid-net-wifi-SupplicantStateSwitchesValues()[state.ordinal()]) {
            case 1:
                supplState = 7;
                break;
            case 2:
                supplState = 6;
                break;
            case 3:
                supplState = 5;
                break;
            case 4:
                supplState = 10;
                break;
            case 5:
                supplState = 1;
                break;
            case 6:
                supplState = 11;
                break;
            case 7:
                supplState = 8;
                break;
            case 8:
                supplState = 9;
                break;
            case 9:
                supplState = 3;
                break;
            case 10:
                supplState = 2;
                break;
            case 11:
                supplState = 0;
                break;
            case 12:
                supplState = 4;
                break;
            case 13:
                supplState = 12;
                break;
            default:
                Slog.w(TAG, "Unknown supplicant state " + state);
                supplState = 0;
                break;
        }
        try {
            this.mBatteryStats.noteWifiSupplicantStateChanged(supplState, failedAuth);
        } catch (RemoteException e) {
        }
        Intent intent = new Intent("android.net.wifi.supplicant.STATE_CHANGE");
        intent.addFlags(603979776);
        intent.putExtra("newState", state);
        if (failedAuth) {
            intent.putExtra("supplicantError", 1);
        }
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        pw.println("mAuthFailureInSupplicantBroadcast " + this.mAuthFailureInSupplicantBroadcast);
        pw.println("mNetworksDisabledDuringConnect " + this.mNetworksDisabledDuringConnect);
        pw.println();
    }

    boolean isNetworksDisabledDuringConnect() {
        return this.mNetworksDisabledDuringConnect;
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
                    key = KEY_WEP;
                } else if (capabilitie.contains("PSK")) {
                    key = KEY_WPA;
                } else if (capabilitie.contains("EAP") || capabilitie.contains("IEEE8021X")) {
                    key = KEY_EAP;
                } else if (capabilitie.contains("WAPI-KEY")) {
                    key = KEY_WAPI;
                } else if (capabilitie.contains("WAPI-CERT")) {
                    key = KEY_CERT;
                } else {
                    key = KEY_NONE;
                }
            }
        }
        return key;
    }

    private void removeTimeoutEvent() {
        this.mHandler.removeMessages(AUTHENTICATION_FAILURE_EVENT_TIMEOUT);
        this.mHandler.removeMessages(EVENT_CONNECT_NETWORK_TIMEOUT);
        this.mHandler.removeMessages(EVENT_ASSOC_REJECT_TIMEOUT);
        this.mHandler.removeMessages(EVENT_SELECT_TIMEOUT);
        this.mHandler.removeMessages(WRONG_KEY_EVENT_TIMEOUT);
    }
}
