package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.ClientModeManager;
import com.android.server.wifi.OppoClientModeManager2;
import com.android.server.wifi.ScanOnlyModeManager;
import com.android.server.wifi.WifiController;
import com.android.server.wifi.util.WifiPermissionsUtil;

public class WifiController extends StateMachine {
    private static final int BASE = 155648;
    static final int CMD_AIRPLANE_TOGGLED = 155657;
    static final int CMD_AP_START_FAILURE = 155661;
    static final int CMD_AP_STOPPED = 155663;
    static final int CMD_DEFERRED_RECOVERY_RESTART_WIFI = 155670;
    static final int CMD_DEFERRED_TOGGLE = 155659;
    static final int CMD_EMERGENCY_CALL_STATE_CHANGED = 155662;
    static final int CMD_EMERGENCY_MODE_CHANGED = 155649;
    static final int CMD_RECOVERY_DISABLE_WIFI = 155667;
    static final int CMD_RECOVERY_RESTART_WIFI = 155665;
    private static final int CMD_RECOVERY_RESTART_WIFI_CONTINUE = 155666;
    static final int CMD_SCANNING_STOPPED = 155669;
    static final int CMD_SCAN_ALWAYS_MODE_CHANGED = 155655;
    static final int CMD_SET_AP = 155658;
    static final int CMD_SET_WIFI_SHARING = 155680;
    static final int CMD_STA_START_FAILURE = 155664;
    static final int CMD_STA_STOPPED = 155668;
    static final int CMD_WIFI_TOGGLED = 155656;
    /* access modifiers changed from: private */
    public static boolean DBG = false;
    private static final long DEFAULT_REENABLE_DELAY_MS = 500;
    private static final long DEFER_MARGIN_MS = 5;
    private static final int DUAL_STA_BLACK_LIST_AP = 5;
    private static final int DUAL_STA_CONCURRENCY = 3;
    private static final int DUAL_STA_DISABLED = 2;
    private static final int DUAL_STA_NETWORK_GOOD = 4;
    private static final int DUAL_STA_NOT_SUPPORTED = -1;
    private static final int DUAL_STA_SUCCESS = 0;
    private static final int DUAL_STA_WIFI_DISABLED = 1;
    private static final String KEY_DUAL_STA_SWITCH = "dual_sta_switch_on";
    private static final int MAX_RECOVERY_TIMEOUT_DELAY_MS = 4000;
    private static final String TAG = "WifiController";
    private static String WIFI_PACKEG_NAME = "android";
    /* access modifiers changed from: private */
    public static final SparseArray<String> sGetWhatToString = MessageUtils.findMessageNames(sMessageClasses);
    private static final Class[] sMessageClasses = {WifiController.class};
    /* access modifiers changed from: private */
    public boolean isSoftapStarting = false;
    /* access modifiers changed from: private */
    public boolean isWifiSharingStarting = false;
    /* access modifiers changed from: private */
    public final ActiveModeWarden mActiveModeWarden;
    private ClientModeManager.Listener mClientModeCallback = new ClientModeCallback();
    private OppoClientModeManager2.Listener mClientModeCallback2 = new ClientModeCallback2();
    /* access modifiers changed from: private */
    public final ClientModeImpl mClientModeImpl;
    /* access modifiers changed from: private */
    public final Looper mClientModeImplLooper;
    /* access modifiers changed from: private */
    public Context mContext;
    private DefaultState mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public EcmState mEcmState = new EcmState();
    /* access modifiers changed from: private */
    public final FrameworkFacade mFacade;
    private boolean mFirstUserSignOnSeen = false;
    private boolean mIsOppoSta2Enabled = false;
    private boolean mIsP2pConnecting = false;
    /* access modifiers changed from: private */
    public boolean mIsSapScanCoexistSupported = false;
    /* access modifiers changed from: private */
    public boolean mIsScanClientStarting = false;
    /* access modifiers changed from: private */
    public long mLastOpenTime;
    /* access modifiers changed from: private */
    public final OppoActiveModeWarden2 mOppoActiveModeWarden2;
    /* access modifiers changed from: private */
    public long mReEnableDelayMillis;
    /* access modifiers changed from: private */
    public int mRecoveryDelayMillis;
    /* access modifiers changed from: private */
    public boolean mSapScanCoexistStarting = false;
    private ScanOnlyModeManager.Listener mScanOnlyModeCallback = new ScanOnlyCallback();
    /* access modifiers changed from: private */
    public final WifiSettingsStore mSettingsStore;
    public int mSoftapStatus = 11;
    private boolean mStaAndApConcurrency = false;
    /* access modifiers changed from: private */
    public StaDisabledState mStaDisabledState = new StaDisabledState();
    /* access modifiers changed from: private */
    public StaDisabledWithScanState mStaDisabledWithScanState = new StaDisabledWithScanState();
    /* access modifiers changed from: private */
    public StaEnabledState mStaEnabledState = new StaEnabledState();
    private final WifiPermissionsUtil mWifiPermissionsUtil;

    WifiController(Context context, ClientModeImpl clientModeImpl, Looper clientModeImplLooper, WifiSettingsStore wss, Looper wifiServiceLooper, FrameworkFacade f, ActiveModeWarden amw, WifiPermissionsUtil wifiPermissionsUtil) {
        super(TAG, wifiServiceLooper);
        this.mFacade = f;
        this.mContext = context;
        this.mClientModeImpl = clientModeImpl;
        this.mClientModeImplLooper = clientModeImplLooper;
        this.mActiveModeWarden = amw;
        this.mSettingsStore = wss;
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
        this.mOppoActiveModeWarden2 = WifiInjector.getInstance().getOppoActiveModeWarden2();
        addState(this.mDefaultState);
        addState(this.mStaDisabledState, this.mDefaultState);
        addState(this.mStaEnabledState, this.mDefaultState);
        addState(this.mStaDisabledWithScanState, this.mDefaultState);
        addState(this.mEcmState, this.mDefaultState);
        setLogRecSize(100);
        setLogOnlyTransitions(false);
        this.mActiveModeWarden.registerScanOnlyCallback(this.mScanOnlyModeCallback);
        this.mActiveModeWarden.registerClientModeCallback(this.mClientModeCallback);
        this.mOppoActiveModeWarden2.registerClientModeCallback(this.mClientModeCallback2);
        readWifiReEnableDelay();
        readWifiRecoveryDelay();
    }

    public void start() {
        boolean isAirplaneModeOn = this.mSettingsStore.isAirplaneModeOn();
        boolean isWifiEnabled = this.mSettingsStore.isWifiToggleEnabled();
        boolean isScanningAlwaysAvailable = this.mSettingsStore.isScanAlwaysAvailable();
        boolean isLocationModeActive = this.mWifiPermissionsUtil.isLocationModeEnabled();
        log("isAirplaneModeOn = " + isAirplaneModeOn + ", isWifiEnabled = " + isWifiEnabled + ", isScanningAvailable = " + isScanningAlwaysAvailable + ", isLocationModeActive = " + isLocationModeActive);
        if (checkScanOnlyModeAvailable()) {
            setInitialState(this.mStaDisabledWithScanState);
        } else {
            setInitialState(this.mStaDisabledState);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.location.MODE_CHANGED");
        filter.addAction("oppo.intent.action.wifi.WIFI_SHARING_STATE_CHANGED");
        filter.addAction(OppoSapScanCoexistManager.ACTION_SCAN_SAP_COEXIST_CHANGED);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.WifiController.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED") || action.equals("oppo.intent.action.wifi.WIFI_SHARING_STATE_CHANGED")) {
                    int state = intent.getIntExtra("wifi_state", 14);
                    if (state == 14 || state == 114) {
                        Log.e(WifiController.TAG, "SoftAP start failed");
                        boolean unused = WifiController.this.isSoftapStarting = false;
                        boolean unused2 = WifiController.this.isWifiSharingStarting = false;
                        WifiController.this.sendMessage(WifiController.CMD_AP_START_FAILURE);
                    } else if (state == 11) {
                        WifiController.this.sendMessage(WifiController.CMD_AP_STOPPED);
                    } else if (state == 13) {
                        boolean unused3 = WifiController.this.isSoftapStarting = false;
                    } else if (state == 113) {
                        boolean unused4 = WifiController.this.isWifiSharingStarting = false;
                    }
                } else if (action.equals(OppoSapScanCoexistManager.ACTION_SCAN_SAP_COEXIST_CHANGED)) {
                    int state2 = intent.getIntExtra(OppoSapScanCoexistManager.EXTRA_SCAN_SAP_COEXIST_STATE, 4);
                    if (state2 == 3) {
                        Log.d(WifiController.TAG, "scan sap coexist enabled!");
                        boolean unused5 = WifiController.this.mSapScanCoexistStarting = false;
                    } else if (state2 == 1) {
                        Log.d(WifiController.TAG, "scan sap coexist disable!");
                        if (!WifiController.this.mSapScanCoexistStarting) {
                            OppoSapScanCoexistManager.getInstance(WifiController.this.mContext).updateCoexistSwitchStatus(0);
                        }
                    }
                } else if (action.equals("android.location.MODE_CHANGED")) {
                    WifiController.this.sendMessage(WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED);
                }
            }
        }, new IntentFilter(filter));
        WifiController.super.start();
    }

    public void setStaSoftApConcurrencyForSharing(boolean enable) {
        if (DBG) {
            logd("set setStaSoftApConcurrencyForSharing = " + enable);
        }
        this.mStaAndApConcurrency = enable;
    }

    public boolean getStaSoftApConcurrencyForSharing() {
        return this.mStaAndApConcurrency;
    }

    public void updateSoftApState(int state, int reason) {
        this.mSoftapStatus = state;
        if (DBG) {
            Log.d(TAG, "updateSoftApState, Current SoftapStatus = " + this.mSoftapStatus);
        }
    }

    public int getSoftApState() {
        return this.mSoftapStatus;
    }

    public boolean getSapScanCoexistStartingStatus() {
        return this.mSapScanCoexistStarting;
    }

    /* access modifiers changed from: private */
    public boolean checkScanOnlyModeAvailable() {
        return this.mSettingsStore.isScanAlwaysAvailable();
    }

    private class ScanOnlyCallback implements ScanOnlyModeManager.Listener {
        private ScanOnlyCallback() {
        }

        @Override // com.android.server.wifi.ScanOnlyModeManager.Listener
        public void onStateChanged(int state) {
            if (state == 4) {
                Log.d(WifiController.TAG, "ScanOnlyMode unexpected failure: state unknown");
                OppoWifiSwitchStats.getInstance(WifiController.this.mContext).informWifiTurnOnResult(4);
                if (WifiController.this.mSapScanCoexistStarting) {
                    Log.d(WifiController.TAG, "ScanOnlyMode failed, scan sap coexist failed!");
                    boolean unused = WifiController.this.mSapScanCoexistStarting = false;
                    OppoSapScanCoexistManager.getInstance(WifiController.this.mContext).updateCoexistSwitchStatus(0);
                }
            } else if (state == 1) {
                Log.d(WifiController.TAG, "ScanOnlyMode stopped");
                WifiController.this.sendMessage(WifiController.CMD_SCANNING_STOPPED);
            } else if (state == 3) {
                Log.d(WifiController.TAG, "scan mode active");
                OppoWifiSwitchStats.getInstance(WifiController.this.mContext).scanonlySuccessStatus();
            } else {
                Log.d(WifiController.TAG, "unexpected state update: " + state);
            }
        }
    }

    private class ClientModeCallback implements ClientModeManager.Listener {
        private ClientModeCallback() {
        }

        @Override // com.android.server.wifi.ClientModeManager.Listener
        public void onStateChanged(int state) {
            if (state == 4) {
                WifiController.this.logd("ClientMode unexpected failure: state unknown");
                OppoWifiSwitchStats.getInstance(WifiController.this.mContext).informWifiTurnOnResult(2);
                WifiController.this.sendMessage(WifiController.CMD_STA_START_FAILURE);
            } else if (state == 1) {
                WifiController.this.logd("ClientMode stopped");
                WifiController.this.sendMessage(WifiController.CMD_STA_STOPPED);
            } else if (state == 3) {
                WifiController.this.logd("client mode active");
                OppoWifiSwitchStats.getInstance(WifiController.this.mContext).informWifiTurnOnResult(0);
            } else {
                WifiController wifiController = WifiController.this;
                wifiController.logd("unexpected state update: " + state);
            }
        }
    }

    private void readWifiReEnableDelay() {
        this.mReEnableDelayMillis = this.mFacade.getLongSetting(this.mContext, "wifi_reenable_delay", DEFAULT_REENABLE_DELAY_MS);
    }

    private void readWifiRecoveryDelay() {
        this.mRecoveryDelayMillis = this.mContext.getResources().getInteger(17694940);
        if (this.mRecoveryDelayMillis > MAX_RECOVERY_TIMEOUT_DELAY_MS) {
            this.mRecoveryDelayMillis = MAX_RECOVERY_TIMEOUT_DELAY_MS;
            Log.w(TAG, "Overriding timeout delay with maximum limit value");
        }
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                Log.d(WifiController.TAG, getName() + " " + ((String) WifiController.sGetWhatToString.get(msg.what)) + msg.toString());
            }
            int i = msg.what;
            if (i != WifiController.CMD_EMERGENCY_MODE_CHANGED) {
                if (i != WifiController.CMD_SET_WIFI_SHARING) {
                    switch (i) {
                        case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*{ENCODED_INT: 155655}*/:
                        case WifiController.CMD_WIFI_TOGGLED /*{ENCODED_INT: 155656}*/:
                            break;
                        case WifiController.CMD_AIRPLANE_TOGGLED /*{ENCODED_INT: 155657}*/:
                            if (!WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                                WifiController.this.log("Airplane mode disabled, determine next state");
                                if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                                    if (WifiController.this.checkScanOnlyModeAvailable()) {
                                        WifiController wifiController = WifiController.this;
                                        wifiController.transitionTo(wifiController.mStaDisabledWithScanState);
                                        break;
                                    }
                                } else {
                                    WifiController wifiController2 = WifiController.this;
                                    wifiController2.transitionTo(wifiController2.mStaEnabledState);
                                    break;
                                }
                            } else {
                                WifiController.this.log("Airplane mode toggled, shutdown all modes");
                                WifiController.this.mOppoActiveModeWarden2.shutdownWifi();
                                WifiController.this.mActiveModeWarden.shutdownWifi();
                                WifiController wifiController3 = WifiController.this;
                                wifiController3.transitionTo(wifiController3.mStaDisabledState);
                                boolean unused = WifiController.this.isSoftapStarting = false;
                                OppoWifiSwitchStats.getInstance(WifiController.this.mContext).clearWifiStateCheckMsg();
                                break;
                            }
                            break;
                        case WifiController.CMD_SET_AP /*{ENCODED_INT: 155658}*/:
                            if (!WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                                if (msg.arg1 != 1) {
                                    WifiController.this.mActiveModeWarden.stopSoftAPMode(msg.arg2);
                                    boolean unused2 = WifiController.this.isSoftapStarting = false;
                                    break;
                                } else {
                                    WifiController.this.disableOppoWifiSta2();
                                    if (WifiController.this.isSoftapStarting || WifiController.this.mSoftapStatus == 13) {
                                        if (WifiController.DBG) {
                                            Log.d(WifiController.TAG, "break isSoftapStarting = " + WifiController.this.isSoftapStarting + " mSoftapStatus = " + WifiController.this.mSoftapStatus);
                                            break;
                                        }
                                    } else {
                                        WifiController wifiController4 = WifiController.this;
                                        boolean unused3 = wifiController4.mIsSapScanCoexistSupported = OppoSapScanCoexistManager.getInstance(wifiController4.mContext).getSapScanCoexistSupported();
                                        if (WifiController.this.mIsSapScanCoexistSupported) {
                                            if (WifiController.this.mIsScanClientStarting) {
                                                Log.d(WifiController.TAG, "ScanAndAPConcurrency: start softap when scan only mode, do nothing");
                                                OppoSapScanCoexistManager.getInstance(WifiController.this.mContext).updateCoexistSwitchStatus(1);
                                                boolean unused4 = WifiController.this.mSapScanCoexistStarting = true;
                                            } else if (WifiController.this.checkScanOnlyModeAvailable()) {
                                                OppoSapScanCoexistManager.getInstance(WifiController.this.mContext).updateCoexistSwitchStatus(1);
                                                boolean unused5 = WifiController.this.mSapScanCoexistStarting = true;
                                                WifiController wifiController5 = WifiController.this;
                                                wifiController5.transitionTo(wifiController5.mStaDisabledWithScanState);
                                            } else {
                                                OppoSapScanCoexistManager.getInstance(WifiController.this.mContext).updateCoexistSwitchStatus(0);
                                                boolean unused6 = WifiController.this.mSapScanCoexistStarting = false;
                                                WifiController.this.mActiveModeWarden.disableWifi();
                                                WifiController wifiController6 = WifiController.this;
                                                wifiController6.transitionTo(wifiController6.mStaDisabledState);
                                            }
                                            Log.d(WifiController.TAG, "ScanAndAPConcurrency: start softap, mIsScanClientStarting=" + WifiController.this.mIsScanClientStarting + " ,checkScanOnlyModeAvailable=" + WifiController.this.checkScanOnlyModeAvailable());
                                        } else {
                                            Log.d(WifiController.TAG, "ScanAndAPConcurrency: origal start softap and transition to disable mode");
                                            OppoSapScanCoexistManager.getInstance(WifiController.this.mContext).updateCoexistSwitchStatus(0);
                                            boolean unused7 = WifiController.this.mSapScanCoexistStarting = false;
                                            WifiController.this.mActiveModeWarden.disableWifi();
                                            WifiController wifiController7 = WifiController.this;
                                            wifiController7.transitionTo(wifiController7.mStaDisabledState);
                                        }
                                        SoftApModeConfiguration softApModeConfiguration = (SoftApModeConfiguration) msg.obj;
                                        WifiController.this.mActiveModeWarden.enterSoftAPMode((SoftApModeConfiguration) msg.obj);
                                        boolean unused8 = WifiController.this.isSoftapStarting = true;
                                        OppoWifiSwitchStats.getInstance(WifiController.this.mContext).clearWifiStateCheckMsg();
                                        break;
                                    }
                                }
                            } else {
                                WifiController.this.log("drop softap requests when in airplane mode");
                                break;
                            }
                            break;
                        case WifiController.CMD_DEFERRED_TOGGLE /*{ENCODED_INT: 155659}*/:
                            WifiController.this.log("DEFERRED_TOGGLE ignored due to state change");
                            break;
                        default:
                            switch (i) {
                                case WifiController.CMD_AP_START_FAILURE /*{ENCODED_INT: 155661}*/:
                                case WifiController.CMD_STA_START_FAILURE /*{ENCODED_INT: 155664}*/:
                                case WifiController.CMD_RECOVERY_RESTART_WIFI_CONTINUE /*{ENCODED_INT: 155666}*/:
                                case WifiController.CMD_STA_STOPPED /*{ENCODED_INT: 155668}*/:
                                case WifiController.CMD_SCANNING_STOPPED /*{ENCODED_INT: 155669}*/:
                                case WifiController.CMD_DEFERRED_RECOVERY_RESTART_WIFI /*{ENCODED_INT: 155670}*/:
                                    break;
                                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /*{ENCODED_INT: 155662}*/:
                                    break;
                                case WifiController.CMD_AP_STOPPED /*{ENCODED_INT: 155663}*/:
                                    WifiController.this.log("SoftAp mode disabled, determine next state");
                                    if (!WifiController.this.isSoftapStarting) {
                                        if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                                            if (WifiController.this.checkScanOnlyModeAvailable()) {
                                                WifiController wifiController8 = WifiController.this;
                                                wifiController8.transitionTo(wifiController8.mStaDisabledWithScanState);
                                                break;
                                            }
                                        } else {
                                            WifiController wifiController9 = WifiController.this;
                                            wifiController9.transitionTo(wifiController9.mStaEnabledState);
                                            break;
                                        }
                                    } else if (WifiController.DBG) {
                                        Log.d(WifiController.TAG, "isSoftapStarting CMD_AP_STOPPED break");
                                        break;
                                    }
                                    break;
                                case WifiController.CMD_RECOVERY_RESTART_WIFI /*{ENCODED_INT: 155665}*/:
                                    WifiController wifiController10 = WifiController.this;
                                    wifiController10.deferMessage(wifiController10.obtainMessage(WifiController.CMD_DEFERRED_RECOVERY_RESTART_WIFI));
                                    WifiController.this.mActiveModeWarden.shutdownWifi();
                                    WifiController.this.mOppoActiveModeWarden2.shutdownWifi();
                                    WifiController wifiController11 = WifiController.this;
                                    wifiController11.transitionTo(wifiController11.mStaDisabledState);
                                    break;
                                case WifiController.CMD_RECOVERY_DISABLE_WIFI /*{ENCODED_INT: 155667}*/:
                                    WifiController.this.log("Recovery has been throttled, disable wifi");
                                    WifiController.this.mActiveModeWarden.shutdownWifi();
                                    WifiController.this.mOppoActiveModeWarden2.shutdownWifi();
                                    WifiController wifiController12 = WifiController.this;
                                    wifiController12.transitionTo(wifiController12.mStaDisabledState);
                                    break;
                                default:
                                    throw new RuntimeException("WifiController.handleMessage " + msg.what);
                            }
                    }
                } else if (msg.arg1 != 1) {
                    WifiController.this.mActiveModeWarden.stopSoftAPMode(-1);
                    boolean unused9 = WifiController.this.isWifiSharingStarting = false;
                } else if (!WifiController.this.isWifiSharingStarting && WifiController.this.mSoftapStatus != 113) {
                    WifiController.this.disableOppoWifiSta2();
                    boolean unused10 = WifiController.this.isWifiSharingStarting = true;
                    SoftApModeConfiguration softApModeConfiguration2 = (SoftApModeConfiguration) msg.obj;
                    WifiController.this.mActiveModeWarden.enterSoftAPMode((SoftApModeConfiguration) msg.obj);
                } else if (WifiController.DBG) {
                    Log.d(WifiController.TAG, "break isWifiSharingStarting = " + WifiController.this.isWifiSharingStarting + " mSoftapStatus = " + WifiController.this.mSoftapStatus);
                }
                return true;
            }
            if (msg.arg1 == 1) {
                WifiController wifiController13 = WifiController.this;
                wifiController13.transitionTo(wifiController13.mEcmState);
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public class StaDisabledState extends State {
        private int mDeferredEnableSerialNumber = 0;
        private long mDisabledTimestamp;
        private boolean mHaveDeferredEnable = false;

        StaDisabledState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log("StaDisabledState.enter()");
            }
            WifiController.this.disableOppoWifiSta2();
            WifiController.this.mActiveModeWarden.disableWifi();
            this.mDisabledTimestamp = SystemClock.elapsedRealtime();
            this.mDeferredEnableSerialNumber++;
            this.mHaveDeferredEnable = false;
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                Log.d(WifiController.TAG, getName() + " " + ((String) WifiController.sGetWhatToString.get(msg.what)) + msg.toString());
            }
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*{ENCODED_INT: 155655}*/:
                    if (WifiController.this.checkScanOnlyModeAvailable()) {
                        if (WifiController.this.mSoftapStatus != 13) {
                            WifiController wifiController = WifiController.this;
                            wifiController.transitionTo(wifiController.mStaDisabledWithScanState);
                            break;
                        } else {
                            Log.d(WifiController.TAG, "SAP is on, do not transTo scan mode");
                            break;
                        }
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*{ENCODED_INT: 155656}*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (WifiController.this.checkScanOnlyModeAvailable() && WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                            WifiController wifiController2 = WifiController.this;
                            wifiController2.transitionTo(wifiController2.mStaDisabledWithScanState);
                            break;
                        }
                    } else if (!doDeferEnable(msg)) {
                        WifiController.this.mActiveModeWarden.stopSoftAPMode(-1);
                        boolean unused = WifiController.this.isSoftapStarting = false;
                        WifiController wifiController3 = WifiController.this;
                        wifiController3.transitionTo(wifiController3.mStaEnabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_SET_AP /*{ENCODED_INT: 155658}*/:
                    if (msg.arg1 == 1) {
                        WifiController.this.mSettingsStore.setWifiSavedState(0);
                    }
                    return false;
                case WifiController.CMD_DEFERRED_TOGGLE /*{ENCODED_INT: 155659}*/:
                    if (msg.arg1 == this.mDeferredEnableSerialNumber) {
                        WifiController.this.log("DEFERRED_TOGGLE handled");
                        WifiController.this.sendMessage((Message) msg.obj);
                        break;
                    } else {
                        WifiController.this.log("DEFERRED_TOGGLE ignored due to serial mismatch");
                        break;
                    }
                case WifiController.CMD_RECOVERY_RESTART_WIFI_CONTINUE /*{ENCODED_INT: 155666}*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (WifiController.this.checkScanOnlyModeAvailable()) {
                            WifiController wifiController4 = WifiController.this;
                            wifiController4.transitionTo(wifiController4.mStaDisabledWithScanState);
                            break;
                        }
                    } else {
                        WifiController wifiController5 = WifiController.this;
                        wifiController5.transitionTo(wifiController5.mStaEnabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_DEFERRED_RECOVERY_RESTART_WIFI /*{ENCODED_INT: 155670}*/:
                    WifiController wifiController6 = WifiController.this;
                    wifiController6.sendMessageDelayed(WifiController.CMD_RECOVERY_RESTART_WIFI_CONTINUE, (long) wifiController6.mRecoveryDelayMillis);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private boolean doDeferEnable(Message msg) {
            long delaySoFar = SystemClock.elapsedRealtime() - this.mDisabledTimestamp;
            if (delaySoFar >= WifiController.this.mReEnableDelayMillis) {
                return false;
            }
            WifiController wifiController = WifiController.this;
            wifiController.log("WifiController msg " + msg + " deferred for " + (WifiController.this.mReEnableDelayMillis - delaySoFar) + "ms");
            Message deferredMsg = WifiController.this.obtainMessage(WifiController.CMD_DEFERRED_TOGGLE);
            deferredMsg.obj = Message.obtain(msg);
            int i = this.mDeferredEnableSerialNumber + 1;
            this.mDeferredEnableSerialNumber = i;
            deferredMsg.arg1 = i;
            WifiController wifiController2 = WifiController.this;
            wifiController2.sendMessageDelayed(deferredMsg, (wifiController2.mReEnableDelayMillis - delaySoFar) + WifiController.DEFER_MARGIN_MS);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public class StaEnabledState extends State {
        StaEnabledState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log("StaEnabledState.enter()");
            }
            long unused = WifiController.this.mLastOpenTime = System.currentTimeMillis();
            WifiController.this.mActiveModeWarden.enterClientMode();
        }

        public boolean processMessage(Message msg) {
            String bugTitle;
            String bugDetail;
            if (WifiController.DBG) {
                Log.d(WifiController.TAG, getName() + " " + ((String) WifiController.sGetWhatToString.get(msg.what)) + msg.toString());
            }
            switch (msg.what) {
                case WifiController.CMD_WIFI_TOGGLED /*{ENCODED_INT: 155656}*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (!WifiController.this.checkScanOnlyModeAvailable()) {
                            WifiController wifiController = WifiController.this;
                            wifiController.transitionTo(wifiController.mStaDisabledState);
                            break;
                        } else {
                            int romUpdateWOT = WifiController.this.mClientModeImpl.getRomUpdateIntegerValue("BASIC_WIFI_OPEN_TIME", 300).intValue();
                            int wifiFtmTest = SystemProperties.getInt("oppo.wifi.ftmtest", 0);
                            if (System.currentTimeMillis() - WifiController.this.mLastOpenTime > ((long) romUpdateWOT) * 1000 && wifiFtmTest == 0) {
                                WifiController wifiController2 = WifiController.this;
                                wifiController2.transitionTo(wifiController2.mStaDisabledState);
                                WifiController wifiController3 = WifiController.this;
                                wifiController3.sendMessageDelayed(wifiController3.obtainMessage(WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED), 2000);
                                break;
                            } else {
                                WifiController wifiController4 = WifiController.this;
                                wifiController4.transitionTo(wifiController4.mStaDisabledWithScanState);
                                break;
                            }
                        }
                    } else {
                        int mWifiState = WifiController.this.mClientModeImpl.syncGetWifiState();
                        Log.e(WifiController.TAG, "Calling setWifiEnabled(true) in StaEnabledState!! mWifiState=" + mWifiState);
                        if (!(mWifiState == 2 || mWifiState == 3)) {
                            if (WifiController.DBG) {
                                Log.d(WifiController.TAG, "Mismatch in the state " + mWifiState);
                            }
                            Handler mHandler = WifiController.this.getHandler();
                            if (mHandler != null && !mHandler.hasMessages(WifiController.CMD_RECOVERY_RESTART_WIFI)) {
                                if (WifiController.DBG) {
                                    WifiController.this.logd("enqueue CMD_RESTART_WIFI");
                                }
                                WifiController.this.sendMessage(WifiController.CMD_RECOVERY_RESTART_WIFI);
                                break;
                            }
                        }
                    }
                case WifiController.CMD_AIRPLANE_TOGGLED /*{ENCODED_INT: 155657}*/:
                    if (WifiController.this.mSettingsStore.isAirplaneModeOn()) {
                        return false;
                    }
                    WifiController.this.log("airplane mode toggled - and airplane mode is off.  return handled");
                    return true;
                case WifiController.CMD_SET_AP /*{ENCODED_INT: 155658}*/:
                    if (msg.arg1 == 1) {
                        WifiController.this.mSettingsStore.setWifiSavedState(1);
                    }
                    return false;
                case WifiController.CMD_DEFERRED_TOGGLE /*{ENCODED_INT: 155659}*/:
                case 155660:
                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /*{ENCODED_INT: 155662}*/:
                case WifiController.CMD_RECOVERY_RESTART_WIFI_CONTINUE /*{ENCODED_INT: 155666}*/:
                case WifiController.CMD_RECOVERY_DISABLE_WIFI /*{ENCODED_INT: 155667}*/:
                default:
                    return false;
                case WifiController.CMD_AP_START_FAILURE /*{ENCODED_INT: 155661}*/:
                case WifiController.CMD_AP_STOPPED /*{ENCODED_INT: 155663}*/:
                    break;
                case WifiController.CMD_STA_START_FAILURE /*{ENCODED_INT: 155664}*/:
                    if (WifiController.this.checkScanOnlyModeAvailable()) {
                        WifiController wifiController5 = WifiController.this;
                        wifiController5.transitionTo(wifiController5.mStaDisabledWithScanState);
                        break;
                    } else {
                        WifiController wifiController6 = WifiController.this;
                        wifiController6.transitionTo(wifiController6.mStaDisabledState);
                        break;
                    }
                case WifiController.CMD_RECOVERY_RESTART_WIFI /*{ENCODED_INT: 155665}*/:
                    if (msg.arg1 >= SelfRecovery.REASON_STRINGS.length || msg.arg1 < 0) {
                        bugDetail = "";
                        bugTitle = "Wi-Fi BugReport";
                    } else {
                        bugDetail = SelfRecovery.REASON_STRINGS[msg.arg1];
                        bugTitle = "Wi-Fi BugReport: " + bugDetail;
                    }
                    if (msg.arg1 != 0) {
                        new Handler(WifiController.this.mClientModeImplLooper).post(new Runnable(bugTitle, bugDetail) {
                            /* class com.android.server.wifi.$$Lambda$WifiController$StaEnabledState$8UmuHOrDhEe90LG8fQyHFz8Png */
                            private final /* synthetic */ String f$1;
                            private final /* synthetic */ String f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void run() {
                                WifiController.StaEnabledState.this.lambda$processMessage$0$WifiController$StaEnabledState(this.f$1, this.f$2);
                            }
                        });
                    }
                    return false;
                case WifiController.CMD_STA_STOPPED /*{ENCODED_INT: 155668}*/:
                    WifiController wifiController7 = WifiController.this;
                    wifiController7.transitionTo(wifiController7.mStaDisabledState);
                    break;
            }
            return true;
        }

        public /* synthetic */ void lambda$processMessage$0$WifiController$StaEnabledState(String bugTitle, String bugDetail) {
            WifiController.this.mClientModeImpl.takeBugReport(bugTitle, bugDetail);
        }
    }

    /* access modifiers changed from: package-private */
    public class StaDisabledWithScanState extends State {
        private int mDeferredEnableSerialNumber = 0;
        private long mDisabledTimestamp;
        private boolean mHaveDeferredEnable = false;

        StaDisabledWithScanState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log("StaDisabledWithScanState.enter()");
            }
            WifiController.this.disableOppoWifiSta2();
            boolean unused = WifiController.this.mIsScanClientStarting = true;
            Log.d(WifiController.TAG, "ScanAndAPConcurrency: enter scan Only mode");
            WifiController.this.mActiveModeWarden.enterScanOnlyMode();
            this.mDisabledTimestamp = SystemClock.elapsedRealtime();
            this.mDeferredEnableSerialNumber++;
            this.mHaveDeferredEnable = false;
        }

        public void exit() {
            boolean unused = WifiController.this.mIsScanClientStarting = false;
            Log.d(WifiController.TAG, "ScanAndAPConcurrency: exit scan Only mode");
        }

        public boolean processMessage(Message msg) {
            if (WifiController.DBG) {
                Log.d(WifiController.TAG, getName() + " " + ((String) WifiController.sGetWhatToString.get(msg.what)) + msg.toString());
            }
            switch (msg.what) {
                case WifiController.CMD_SCAN_ALWAYS_MODE_CHANGED /*{ENCODED_INT: 155655}*/:
                    if (!WifiController.this.checkScanOnlyModeAvailable()) {
                        WifiController.this.log("StaDisabledWithScanState: scan no longer available");
                        WifiController wifiController = WifiController.this;
                        wifiController.transitionTo(wifiController.mStaDisabledState);
                        break;
                    }
                    break;
                case WifiController.CMD_WIFI_TOGGLED /*{ENCODED_INT: 155656}*/:
                    if (!WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        if (WifiController.this.hasMessages(WifiController.CMD_DEFERRED_TOGGLE)) {
                            if (WifiController.DBG) {
                                WifiController.this.log("wifi should be disabled, remove CMD_DEFERRED_TOGGLE msg");
                            }
                            WifiController.this.removeMessages(WifiController.CMD_DEFERRED_TOGGLE);
                            break;
                        }
                    } else if (!doDeferEnable(msg)) {
                        WifiController.this.mActiveModeWarden.stopSoftAPMode(-1);
                        WifiController wifiController2 = WifiController.this;
                        wifiController2.transitionTo(wifiController2.mStaEnabledState);
                        boolean unused = WifiController.this.isSoftapStarting = false;
                        break;
                    }
                    break;
                case WifiController.CMD_SET_AP /*{ENCODED_INT: 155658}*/:
                    if (msg.arg1 == 1) {
                        WifiController.this.mSettingsStore.setWifiSavedState(0);
                    }
                    return false;
                case WifiController.CMD_DEFERRED_TOGGLE /*{ENCODED_INT: 155659}*/:
                    if (msg.arg1 == this.mDeferredEnableSerialNumber) {
                        WifiController.this.logd("DEFERRED_TOGGLE handled");
                        WifiController.this.sendMessage((Message) msg.obj);
                        break;
                    } else {
                        WifiController.this.log("DEFERRED_TOGGLE ignored due to serial mismatch");
                        break;
                    }
                case WifiController.CMD_AP_START_FAILURE /*{ENCODED_INT: 155661}*/:
                case WifiController.CMD_AP_STOPPED /*{ENCODED_INT: 155663}*/:
                    if (!WifiController.this.mSapScanCoexistStarting && WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                        WifiController wifiController3 = WifiController.this;
                        wifiController3.transitionTo(wifiController3.mStaEnabledState);
                        break;
                    }
                case WifiController.CMD_SCANNING_STOPPED /*{ENCODED_INT: 155669}*/:
                    WifiController.this.log("WifiController: SCANNING_STOPPED when in scan mode -> StaDisabled");
                    WifiController wifiController4 = WifiController.this;
                    wifiController4.transitionTo(wifiController4.mStaDisabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        private boolean doDeferEnable(Message msg) {
            long delaySoFar = SystemClock.elapsedRealtime() - this.mDisabledTimestamp;
            if (delaySoFar >= WifiController.this.mReEnableDelayMillis) {
                return false;
            }
            WifiController wifiController = WifiController.this;
            wifiController.log("WifiController msg " + msg + " deferred for " + (WifiController.this.mReEnableDelayMillis - delaySoFar) + "ms");
            Message deferredMsg = WifiController.this.obtainMessage(WifiController.CMD_DEFERRED_TOGGLE);
            deferredMsg.obj = Message.obtain(msg);
            int i = this.mDeferredEnableSerialNumber + 1;
            this.mDeferredEnableSerialNumber = i;
            deferredMsg.arg1 = i;
            WifiController wifiController2 = WifiController.this;
            wifiController2.sendMessageDelayed(deferredMsg, (wifiController2.mReEnableDelayMillis - delaySoFar) + WifiController.DEFER_MARGIN_MS);
            return true;
        }
    }

    private State getNextWifiState() {
        if (this.mSettingsStore.getWifiSavedState() == 1) {
            return this.mStaEnabledState;
        }
        if (checkScanOnlyModeAvailable()) {
            return this.mStaDisabledWithScanState;
        }
        return this.mStaDisabledState;
    }

    /* access modifiers changed from: package-private */
    public class EcmState extends State {
        private int mEcmEntryCount;

        EcmState() {
        }

        public void enter() {
            if (WifiController.DBG) {
                WifiController.this.log("EcmState.enter()");
            }
            WifiController.this.mActiveModeWarden.stopSoftAPMode(-1);
            boolean configWiFiDisableInECBM = WifiController.this.mFacade.getConfigWiFiDisableInECBM(WifiController.this.mContext);
            WifiController wifiController = WifiController.this;
            wifiController.log("WifiController msg getConfigWiFiDisableInECBM " + configWiFiDisableInECBM);
            if (configWiFiDisableInECBM) {
                WifiController.this.mOppoActiveModeWarden2.shutdownWifi();
                WifiController.this.mActiveModeWarden.shutdownWifi();
            }
            this.mEcmEntryCount = 1;
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case WifiController.CMD_EMERGENCY_MODE_CHANGED /*{ENCODED_INT: 155649}*/:
                    if (msg.arg1 == 1) {
                        this.mEcmEntryCount++;
                    } else if (msg.arg1 == 0) {
                        decrementCountAndReturnToAppropriateState();
                    }
                    return true;
                case WifiController.CMD_SET_AP /*{ENCODED_INT: 155658}*/:
                    return true;
                case WifiController.CMD_EMERGENCY_CALL_STATE_CHANGED /*{ENCODED_INT: 155662}*/:
                    if (msg.arg1 == 1) {
                        this.mEcmEntryCount++;
                    } else if (msg.arg1 == 0) {
                        decrementCountAndReturnToAppropriateState();
                    }
                    return true;
                case WifiController.CMD_AP_STOPPED /*{ENCODED_INT: 155663}*/:
                case WifiController.CMD_STA_STOPPED /*{ENCODED_INT: 155668}*/:
                case WifiController.CMD_SCANNING_STOPPED /*{ENCODED_INT: 155669}*/:
                    return true;
                case WifiController.CMD_RECOVERY_RESTART_WIFI /*{ENCODED_INT: 155665}*/:
                case WifiController.CMD_RECOVERY_DISABLE_WIFI /*{ENCODED_INT: 155667}*/:
                    return true;
                default:
                    return false;
            }
        }

        private void decrementCountAndReturnToAppropriateState() {
            boolean exitEcm = false;
            int i = this.mEcmEntryCount;
            if (i == 0) {
                WifiController.this.loge("mEcmEntryCount is 0; exiting Ecm");
                exitEcm = true;
            } else {
                int i2 = i - 1;
                this.mEcmEntryCount = i2;
                if (i2 == 0) {
                    exitEcm = true;
                }
            }
            if (!exitEcm) {
                return;
            }
            if (WifiController.this.mSettingsStore.isWifiToggleEnabled()) {
                WifiController wifiController = WifiController.this;
                wifiController.transitionTo(wifiController.mStaEnabledState);
            } else if (WifiController.this.checkScanOnlyModeAvailable()) {
                WifiController wifiController2 = WifiController.this;
                wifiController2.transitionTo(wifiController2.mStaDisabledWithScanState);
            } else {
                WifiController wifiController3 = WifiController.this;
                wifiController3.transitionTo(wifiController3.mStaDisabledState);
            }
        }
    }

    private boolean isDualStaEnabled() {
        return (Settings.Global.getInt(this.mContext.getContentResolver(), "dual_sta_switch_on", 1) == 1) && WifiRomUpdateHelper.getInstance(this.mContext).getBooleanValue("NETWORK_DUAL_STA_ENABLED", true);
    }

    public int enableOppoWifiSta2(boolean forceEnable) {
        if (DBG) {
            Log.d(TAG, "*****start to enable oppo wifi sta2, mIsOppoSta2Enabled = " + this.mIsOppoSta2Enabled + " mSoftapStatus = " + this.mSoftapStatus + " mIsP2pConnecting = " + this.mIsP2pConnecting);
        }
        if (!OppoSlaManager.getInstance(this.mContext).isDualStaSupported()) {
            Log.e(TAG, "can't enable oppo wifi sta2 ! Dual STA NOT supported!!");
            return -1;
        } else if (!this.mSettingsStore.isWifiToggleEnabled() || !"ClientModeActiveState".equals(this.mActiveModeWarden.getCurrentMode())) {
            Log.d(TAG, "wifi is disabled or wlan0 is down,just return");
            return 1;
        } else if (!this.mClientModeImpl.isConnected()) {
            Log.d(TAG, "can't enable oppo wifi sta2 !, because wlan0 is not connected.");
            return -1;
        } else {
            boolean networkGoodEnabled = WifiRomUpdateHelper.getInstance(this.mContext).getBooleanValue("OPPO_DUAL_STA_NET_GOOD", false);
            if (!forceEnable && networkGoodEnabled && OppoSlaManager.getInstance(this.mContext).isMainWifiGoodEnough()) {
                Log.d(TAG, "can't enable oppo wifi sta2 ! Main wifi is good enough!!");
                return 4;
            } else if (this.mIsOppoSta2Enabled) {
                Log.d(TAG, "can't enable oppo wifi sta2 !, because wlan1 has been enabled.");
                if (WifiInjector.getInstance().getScanRequestProxy() != null) {
                    WifiInjector.getInstance().getScanRequestProxy().startScan(1000, WIFI_PACKEG_NAME);
                }
                return 0;
            } else if (!isDualStaEnabled()) {
                Log.d(TAG, "can't enable oppo wifi sta2 !, because dual sta switch is disabled.");
                return 2;
            } else if (this.mOppoActiveModeWarden2 == null) {
                return -1;
            } else {
                int i = this.mSoftapStatus;
                if ((i == 11 || i == 111) && !this.mIsP2pConnecting) {
                    this.mIsOppoSta2Enabled = true;
                    this.mOppoActiveModeWarden2.enterClientMode();
                    return 0;
                }
                Log.d(TAG, "can't enable oppo wifi sta2 !, because softAp/P2p is working.");
                return 3;
            }
        }
    }

    public int enableOppoWifiSta2() {
        return enableOppoWifiSta2(false);
    }

    public void disableOppoWifiSta2() {
        if (DBG) {
            Log.d(TAG, "*****shutdown oppo wifi sta2 !, the mIsOppoSta2Enabled = " + this.mIsOppoSta2Enabled);
        }
        if (!this.mIsOppoSta2Enabled) {
            Log.d(TAG, "can't disable oppo wifi sta2 !, because wlan1 has been disabled.");
            return;
        }
        OppoActiveModeWarden2 oppoActiveModeWarden2 = this.mOppoActiveModeWarden2;
        if (oppoActiveModeWarden2 != null) {
            this.mIsOppoSta2Enabled = false;
            oppoActiveModeWarden2.shutdownWifi();
        }
    }

    public void setP2pIsConnectingFlag(boolean enable) {
        if (DBG) {
            Log.d(TAG, "*****setP2pIsConnectingFlag, enable = " + enable + " mIsOppoSta2Enabled = " + this.mIsOppoSta2Enabled);
        }
        this.mIsP2pConnecting = enable;
        if (enable && this.mIsOppoSta2Enabled) {
            disableOppoWifiSta2();
        }
    }

    private class ClientModeCallback2 implements OppoClientModeManager2.Listener {
        private ClientModeCallback2() {
        }

        @Override // com.android.server.wifi.OppoClientModeManager2.Listener
        public void onStateChanged(int state) {
            if (state == 4) {
                WifiController.this.logd("ClientMode unexpected failure: state unknown");
            } else if (state == 1) {
                WifiController.this.logd("ClientMode stopped");
            } else if (state == 3) {
                WifiController.this.logd("client mode active");
            } else {
                WifiController wifiController = WifiController.this;
                wifiController.logd("unexpected state update: " + state);
            }
        }
    }

    public void enableVerboseLogging(int level) {
        if (level > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
    }
}
