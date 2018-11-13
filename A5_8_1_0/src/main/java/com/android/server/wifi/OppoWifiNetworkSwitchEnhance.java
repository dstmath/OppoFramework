package com.android.server.wifi;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.RouteInfo;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class OppoWifiNetworkSwitchEnhance {
    private static final String ACTION_WIFI_NETWORK_AVAILABLE = "android.net.wifi.OPPO_WIFI_VALID";
    private static final String ACTION_WIFI_NETWORK_CONNECT = "android.net.wifi.OPPO_WIFI_CONNECT";
    private static final String ACTION_WIFI_NETWORK_NOT_AVAILABLE = "android.net.wifi.OPPO_WIFI_INVALID";
    private static final String ACTION_WIFI_NETWORK_STATE = "android.net.wifi.OPPO_WIFI_NET_STATE";
    private static final int CMD_RSSI_FETCH = 3003;
    private static final int DECT_TIME = 2;
    private static final int EVENT_ADD_UPDATE_NETWORK = 2000;
    private static final int EVENT_AUTO_CONNECT_AP = 2001;
    private static final int EVENT_DETECT_RSSI = 2004;
    private static final int EVENT_DETECT_SCAN_RESULT = 2005;
    private static final int EVENT_NETWORK_NOT_AVAILABLE = 2002;
    private static final int EVENT_NETWORK_STATE_CHANGE = 3000;
    private static final int EVENT_REOVE_UPDATE_NETWORK = 2003;
    private static final int EVENT_SCREEN_OFF = 3002;
    private static final int EVENT_SCREEN_ON = 3001;
    private static final String EXTRA_NETWORK_STATE = "netState";
    private static final String EXTRA_WIFI_LINK = "linkProperties";
    private static final String EXTRA_WIFI_MANUAL = "manualConnect";
    private static final String EXTRA_WIFI_NETWORK = "network";
    private static final String EXTRA_WIFI_SSID = "ssid";
    private static final int GOOD_RSSI_SWITCH_VALUE = -70;
    private static final int INVALID_RSSI = -127;
    private static final int LOSS_PKT = 2;
    private static final int LOW_RSSI = -78;
    private static final int RSSI_DELTA = 5;
    private static final long SCAN_RESULT_AGE = 15000;
    private static final String SECURITY_EAP = "WPA_EAP";
    private static final String SECURITY_NONE = "NONE";
    private static final String SECURITY_PSK = "WPA_PSK";
    private static final String SECURITY_WAPI_CERT = "WAPI_CERT";
    private static final String SECURITY_WAPI_PSK = "WAPI_PSK";
    private static final String SECURITY_WEP = "WEP";
    private static final String SECURITY_WPA2_PSK = "WPA2_PSK";
    private static final int SWITCH_CONNECT_DELAY = 5000;
    private static final String TAG = "OppoWifiNetworkSwitchEnhance";
    private static final int TYPE_AUTO_AVAILABLE_RSSI = 1;
    private static final int TYPE_AUTO_UNAVAILABLE_CAPTIVE = 0;
    private static final int TYPE_AUTO_UNAVAILABLE_SCAN = 2;
    private static final String WIFI_NETWORK_AVAILABLE = (Environment.getDataDirectory() + "/misc/wifi/network_available");
    private static final String WIFI_PACKEG_NAME = "com.android.server.wifi";
    private AlertDialog mAlertDialog = null;
    private boolean mAutoSwitch = true;
    private CharSequence mAvailableAP;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean mCaptivePortal = false;
    private Context mContext;
    private boolean mDebug = true;
    private int mDetectTime;
    private boolean mFeature = true;
    private CharSequence mGoodAvailableAP;
    private Handler mHandler;
    private boolean mInitAutoConnect = true;
    private boolean mIsSoftAP = false;
    private String mLastSSID = null;
    private long mLastScanTime = 0;
    private boolean mLossPktDetect = false;
    private int mLossPktTime;
    private boolean mManualConnect = false;
    private List<NetworkAvailableConfig> mNetworkAvailables = new ArrayList();
    private NetworkInfo mNetworkInfo = null;
    private NetworkLinkMonitor mNetworkLinkMonitor;
    private String mNewBssid = " ";
    private String mNewSsid = " ";
    private String mOldBssid = " ";
    private String mOldSsid = " ";
    private List<WifiConfiguration> mSortWifiConfig = new ArrayList();
    private SupplicantStateTracker mSupplicantTracker;
    private String mUnavailableSsid = " ";
    private WifiConfigManager mWifiConfigManager;
    private WifiNative mWifiNative;
    private WifiStateMachine mWifiStateMachine;

    private final class H extends Handler {
        public H(Looper lp) {
            super(lp);
        }

        public void handleMessage(Message msg) {
            WifiInfo handlerCurrentInfo = OppoWifiNetworkSwitchEnhance.this.mWifiStateMachine.syncRequestConnectionInfo(OppoWifiNetworkSwitchEnhance.WIFI_PACKEG_NAME);
            OppoWifiNetworkSwitchEnhance.this.logD("handleMessage handlerCurrentInfo: " + handlerCurrentInfo);
            switch (msg.what) {
                case 2000:
                    Intent available = msg.obj;
                    if (!OppoWifiNetworkSwitchEnhance.this.mIsSoftAP && available != null && handlerCurrentInfo != null && !"<unknown ssid>".equals(handlerCurrentInfo.getSSID())) {
                        String avaSsid = available.getStringExtra("ssid");
                        OppoWifiNetworkSwitchEnhance.this.logD("EVENT_ADD_UPDATE_NETWORK avaSsid" + avaSsid + ", getcurrent ssid: " + handlerCurrentInfo.getSSID());
                        if (avaSsid != null && (avaSsid.equals(handlerCurrentInfo.getSSID()) ^ 1) == 0) {
                            boolean netValid = available.getBooleanExtra(OppoWifiNetworkSwitchEnhance.EXTRA_NETWORK_STATE, false);
                            OppoWifiNetworkSwitchEnhance.this.updateNetworkAvailables(OppoWifiNetworkSwitchEnhance.this.getWifiConfig(avaSsid, handlerCurrentInfo.getBSSID()), handlerCurrentInfo.getRssi(), netValid);
                            if (!netValid) {
                                OppoWifiNetworkSwitchEnhance.this.autoConnectAP(0, -100, avaSsid);
                            }
                            OppoWifiNetworkSwitchEnhance.this.mCaptivePortal = true;
                            OppoWifiNetworkSwitchEnhance.this.mLastSSID = avaSsid;
                            return;
                        }
                        return;
                    }
                    return;
                case OppoWifiNetworkSwitchEnhance.EVENT_AUTO_CONNECT_AP /*2001*/:
                    OppoWifiNetworkSwitchEnhance.this.autoConnectAP(msg.arg1, msg.arg2, msg.obj);
                    return;
                case OppoWifiNetworkSwitchEnhance.EVENT_NETWORK_NOT_AVAILABLE /*2002*/:
                    Intent unavailable = msg.obj;
                    if (unavailable != null && handlerCurrentInfo != null && !"<unknown ssid>".equals(handlerCurrentInfo.getSSID())) {
                        String unavaSsid = unavailable.getStringExtra("ssid");
                        OppoWifiNetworkSwitchEnhance.this.logD("EVENT_NETWORK_NOT_AVAILABLE removeSsid = " + unavaSsid + ", getcurrent ssid: " + handlerCurrentInfo.getSSID());
                        if (unavaSsid != null && (unavaSsid.equals(handlerCurrentInfo.getSSID()) ^ 1) == 0) {
                            OppoWifiNetworkSwitchEnhance.this.updateNetworkAvailables(OppoWifiNetworkSwitchEnhance.this.getWifiConfig(unavaSsid, handlerCurrentInfo.getBSSID()), WifiConfiguration.INVALID_RSSI, false);
                            OppoWifiNetworkSwitchEnhance.this.mCaptivePortal = true;
                            OppoWifiNetworkSwitchEnhance.this.mLastSSID = unavaSsid;
                            if (!OppoWifiNetworkSwitchEnhance.this.mIsSoftAP) {
                                OppoWifiNetworkSwitchEnhance.this.autoConnectAP(0, -100, unavaSsid);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                case OppoWifiNetworkSwitchEnhance.EVENT_REOVE_UPDATE_NETWORK /*2003*/:
                    if (msg.obj != null) {
                        int netd = ((Integer) msg.obj).intValue();
                        OppoWifiNetworkSwitchEnhance.this.logD("EVENT_REOVE_UPDATE_NETWORK: netid= " + netd);
                        OppoWifiNetworkSwitchEnhance.this.removeNetworkAvailable(netd);
                        return;
                    }
                    return;
                case OppoWifiNetworkSwitchEnhance.EVENT_DETECT_RSSI /*2004*/:
                    OppoWifiNetworkSwitchEnhance.this.detectRssi(msg.arg1 == 1);
                    return;
                default:
                    return;
            }
        }
    }

    private class NetworkAvailableConfig {
        private String mBssid;
        private String mKeymgmt;
        private int mNetid;
        private int mRssi;
        private String mSsid;

        public NetworkAvailableConfig(int netid, int rssi, String ssid, String bssid, String keymgmt) {
            this.mNetid = netid;
            this.mRssi = rssi;
            this.mSsid = ssid;
            this.mBssid = bssid;
            this.mKeymgmt = keymgmt;
        }
    }

    private class NetworkLinkMonitor extends StateMachine {
        private static final double EXP_COEFFICIENT_MONITOR = 0.5d;
        private static final int LINK_SAMPLING_INTERVAL_MS = 1000;
        private static final double POOR_LINK_LOSS_THRESHOLD = 0.5d;
        private static final double POOR_LINK_MIN_VOLUME = 2.0d;
        private static final int POOR_LINK_SAMPLE_COUNT = 2;
        private ConnectedState mConnectedState = new ConnectedState();
        private VolumeWeightedEMA mCurrentLoss;
        private DefaultState mDefaultState = new DefaultState();
        private DisConnectedState mDisConnectedState = new DisConnectedState();
        private boolean mIsScreenOn = true;
        private LinkMonitoringState mLinkMonitoringState = new LinkMonitoringState();
        private int mRssiFetchToken = 0;
        private AsyncChannel mWsmChannel = new AsyncChannel();

        class ConnectedState extends State {
            ConnectedState() {
            }

            public void enter() {
                OppoWifiNetworkSwitchEnhance.this.logD(getName());
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case OppoWifiNetworkSwitchEnhance.EVENT_SCREEN_ON /*3001*/:
                        NetworkLinkMonitor.this.mIsScreenOn = true;
                        NetworkLinkMonitor.this.transitionTo(NetworkLinkMonitor.this.mLinkMonitoringState);
                        return true;
                    default:
                        return false;
                }
            }
        }

        class DefaultState extends State {
            /* renamed from: -android-net-NetworkInfo$DetailedStateSwitchesValues */
            private static final /* synthetic */ int[] f184-android-net-NetworkInfo$DetailedStateSwitchesValues = null;
            final /* synthetic */ int[] $SWITCH_TABLE$android$net$NetworkInfo$DetailedState;

            /* renamed from: -getandroid-net-NetworkInfo$DetailedStateSwitchesValues */
            private static /* synthetic */ int[] m97-getandroid-net-NetworkInfo$DetailedStateSwitchesValues() {
                if (f184-android-net-NetworkInfo$DetailedStateSwitchesValues != null) {
                    return f184-android-net-NetworkInfo$DetailedStateSwitchesValues;
                }
                int[] iArr = new int[DetailedState.values().length];
                try {
                    iArr[DetailedState.AUTHENTICATING.ordinal()] = 3;
                } catch (NoSuchFieldError e) {
                }
                try {
                    iArr[DetailedState.BLOCKED.ordinal()] = 4;
                } catch (NoSuchFieldError e2) {
                }
                try {
                    iArr[DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = 5;
                } catch (NoSuchFieldError e3) {
                }
                try {
                    iArr[DetailedState.CONNECTED.ordinal()] = 1;
                } catch (NoSuchFieldError e4) {
                }
                try {
                    iArr[DetailedState.CONNECTING.ordinal()] = 6;
                } catch (NoSuchFieldError e5) {
                }
                try {
                    iArr[DetailedState.DISCONNECTED.ordinal()] = 2;
                } catch (NoSuchFieldError e6) {
                }
                try {
                    iArr[DetailedState.DISCONNECTING.ordinal()] = 7;
                } catch (NoSuchFieldError e7) {
                }
                try {
                    iArr[DetailedState.FAILED.ordinal()] = 8;
                } catch (NoSuchFieldError e8) {
                }
                try {
                    iArr[DetailedState.IDLE.ordinal()] = 9;
                } catch (NoSuchFieldError e9) {
                }
                try {
                    iArr[DetailedState.OBTAINING_IPADDR.ordinal()] = 10;
                } catch (NoSuchFieldError e10) {
                }
                try {
                    iArr[DetailedState.SCANNING.ordinal()] = 11;
                } catch (NoSuchFieldError e11) {
                }
                try {
                    iArr[DetailedState.SUSPENDED.ordinal()] = 12;
                } catch (NoSuchFieldError e12) {
                }
                try {
                    iArr[DetailedState.VERIFYING_POOR_LINK.ordinal()] = 13;
                } catch (NoSuchFieldError e13) {
                }
                f184-android-net-NetworkInfo$DetailedStateSwitchesValues = iArr;
                return iArr;
            }

            DefaultState() {
            }

            public void enter() {
                OppoWifiNetworkSwitchEnhance.this.logD(getName());
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case OppoWifiNetworkSwitchEnhance.EVENT_NETWORK_STATE_CHANGE /*3000*/:
                        if (OppoWifiNetworkSwitchEnhance.this.mNetworkInfo != null) {
                            OppoWifiNetworkSwitchEnhance.this.logD("Network state change " + OppoWifiNetworkSwitchEnhance.this.mNetworkInfo.getDetailedState());
                            switch (m97-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[OppoWifiNetworkSwitchEnhance.this.mNetworkInfo.getDetailedState().ordinal()]) {
                                case 1:
                                    if (OppoWifiNetworkSwitchEnhance.this.mHandler != null) {
                                        OppoWifiNetworkSwitchEnhance.this.mHandler.removeMessages(OppoWifiNetworkSwitchEnhance.EVENT_AUTO_CONNECT_AP);
                                    }
                                    NetworkLinkMonitor.this.transitionTo(NetworkLinkMonitor.this.mLinkMonitoringState);
                                    break;
                                case 2:
                                    OppoWifiNetworkSwitchEnhance.this.mUnavailableSsid = " ";
                                    if (OppoWifiNetworkSwitchEnhance.this.mIsSoftAP) {
                                        OppoWifiNetworkSwitchEnhance.this.mIsSoftAP = false;
                                    }
                                    NetworkLinkMonitor.this.transitionTo(NetworkLinkMonitor.this.mDisConnectedState);
                                    break;
                                default:
                                    NetworkLinkMonitor.this.transitionTo(NetworkLinkMonitor.this.mDisConnectedState);
                                    break;
                            }
                        }
                        break;
                    case OppoWifiNetworkSwitchEnhance.EVENT_SCREEN_ON /*3001*/:
                        NetworkLinkMonitor.this.mIsScreenOn = true;
                        break;
                    case OppoWifiNetworkSwitchEnhance.EVENT_SCREEN_OFF /*3002*/:
                        NetworkLinkMonitor.this.mIsScreenOn = false;
                        break;
                }
                return true;
            }
        }

        class DisConnectedState extends State {
            DisConnectedState() {
            }

            public void enter() {
                OppoWifiNetworkSwitchEnhance.this.logD(getName());
            }
        }

        class LinkMonitoringState extends State {
            private int mLastRssi;
            private long mLastTimeSample;
            private int mLastTxBad;
            private int mLastTxGood;
            private int mSampleCount;

            LinkMonitoringState() {
            }

            public void enter() {
                OppoWifiNetworkSwitchEnhance.this.logD(getName());
                this.mSampleCount = 0;
                NetworkLinkMonitor.this.mCurrentLoss = new VolumeWeightedEMA(0.5d);
                NetworkLinkMonitor networkLinkMonitor = NetworkLinkMonitor.this;
                NetworkLinkMonitor networkLinkMonitor2 = NetworkLinkMonitor.this;
                NetworkLinkMonitor networkLinkMonitor3 = NetworkLinkMonitor.this;
                networkLinkMonitor.sendMessage(networkLinkMonitor2.obtainMessage(OppoWifiNetworkSwitchEnhance.CMD_RSSI_FETCH, networkLinkMonitor3.mRssiFetchToken = networkLinkMonitor3.mRssiFetchToken + 1, 0));
            }

            public boolean processMessage(Message msg) {
                switch (msg.what) {
                    case OppoWifiNetworkSwitchEnhance.CMD_RSSI_FETCH /*3003*/:
                        if (!NetworkLinkMonitor.this.mIsScreenOn) {
                            NetworkLinkMonitor.this.transitionTo(NetworkLinkMonitor.this.mConnectedState);
                            break;
                        }
                        if (msg.arg1 == NetworkLinkMonitor.this.mRssiFetchToken) {
                            NetworkLinkMonitor.this.mWsmChannel.sendMessage(151572);
                            NetworkLinkMonitor networkLinkMonitor = NetworkLinkMonitor.this;
                            NetworkLinkMonitor networkLinkMonitor2 = NetworkLinkMonitor.this;
                            NetworkLinkMonitor networkLinkMonitor3 = NetworkLinkMonitor.this;
                            networkLinkMonitor.sendMessageDelayed(networkLinkMonitor2.obtainMessage(OppoWifiNetworkSwitchEnhance.CMD_RSSI_FETCH, networkLinkMonitor3.mRssiFetchToken = networkLinkMonitor3.mRssiFetchToken + 1, 0), 1000);
                            break;
                        }
                        break;
                    case 151573:
                        RssiPacketCountInfo info = msg.obj;
                        if (info != null) {
                            int rssi = info.rssi;
                            int mrssi = (this.mLastRssi + rssi) / 2;
                            int txbad = info.txbad;
                            int txgood = info.txgood;
                            OppoWifiNetworkSwitchEnhance.this.logD("Fetch RSSI succeed, rssi=" + rssi + " mrssi=" + mrssi + " txbad=" + txbad + " txgood=" + txgood);
                            long now = SystemClock.elapsedRealtime();
                            if (now - this.mLastTimeSample < 2000) {
                                int dbad = txbad - this.mLastTxBad;
                                int dtotal = dbad + (txgood - this.mLastTxGood);
                                OppoWifiNetworkSwitchEnhance.this.logD("RSSI_PKTCNT_FETCH_SUCCEEDED--dtotal= " + dtotal);
                                if (dtotal > 0) {
                                    NetworkLinkMonitor.this.mCurrentLoss.update(((double) dbad) / ((double) dtotal), dtotal);
                                    if (OppoWifiNetworkSwitchEnhance.this.mDebug) {
                                        DecimalFormat df = new DecimalFormat("#.####");
                                        OppoWifiNetworkSwitchEnhance.this.logD("Incremental loss=" + dbad + "/" + dtotal + " Current loss=" + df.format(NetworkLinkMonitor.this.mCurrentLoss.mValue) + " volume=" + df.format(NetworkLinkMonitor.this.mCurrentLoss.mVolume));
                                    }
                                    if (NetworkLinkMonitor.this.mCurrentLoss.mValue <= 0.5d || NetworkLinkMonitor.this.mCurrentLoss.mVolume <= NetworkLinkMonitor.POOR_LINK_MIN_VOLUME) {
                                        this.mSampleCount = 0;
                                        if (OppoWifiNetworkSwitchEnhance.this.mLossPktDetect) {
                                            OppoWifiNetworkSwitchEnhance.this.mLossPktDetect = false;
                                            OppoWifiNetworkSwitchEnhance.this.detectRssi(OppoWifiNetworkSwitchEnhance.this.mLossPktDetect);
                                        }
                                    } else {
                                        int i = this.mSampleCount + 1;
                                        this.mSampleCount = i;
                                        if (i >= 2) {
                                            OppoWifiNetworkSwitchEnhance.this.mLossPktDetect = true;
                                            OppoWifiNetworkSwitchEnhance.this.detectRssi(OppoWifiNetworkSwitchEnhance.this.mLossPktDetect);
                                            this.mSampleCount = 0;
                                        }
                                    }
                                }
                            }
                            this.mLastTimeSample = now;
                            this.mLastTxBad = txbad;
                            this.mLastTxGood = txgood;
                            this.mLastRssi = rssi;
                            break;
                        }
                        break;
                    case 151574:
                        OppoWifiNetworkSwitchEnhance.this.logD("RSSI_FETCH_FAILED");
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }

        private class VolumeWeightedEMA {
            private double mAlpha;
            private double mProduct = 0.0d;
            private double mValue = 0.0d;
            private double mVolume = 0.0d;

            public VolumeWeightedEMA(double coefficient) {
                this.mAlpha = coefficient;
            }

            public void update(double newValue, int newVolume) {
                if (newVolume > 0) {
                    this.mProduct = (this.mAlpha * (newValue * ((double) newVolume))) + ((1.0d - this.mAlpha) * this.mProduct);
                    this.mVolume = (this.mAlpha * ((double) newVolume)) + ((1.0d - this.mAlpha) * this.mVolume);
                    this.mValue = this.mProduct / this.mVolume;
                }
            }
        }

        public NetworkLinkMonitor(Context context, Messenger dstMessenger) {
            super("NetworkLinkMonitor");
            this.mWsmChannel.connectSync(OppoWifiNetworkSwitchEnhance.this.mContext, getHandler(), dstMessenger);
            addState(this.mDefaultState);
            addState(this.mDisConnectedState, this.mDefaultState);
            addState(this.mConnectedState, this.mDefaultState);
            addState(this.mLinkMonitoringState, this.mConnectedState);
            setInitialState(this.mDefaultState);
        }
    }

    public OppoWifiNetworkSwitchEnhance(Context c, WifiStateMachine wsm, WifiConfigManager wcs, WifiNative wnt, SupplicantStateTracker wst) {
        this.mContext = c;
        this.mWifiStateMachine = wsm;
        this.mWifiConfigManager = wcs;
        this.mWifiNative = wnt;
        this.mSupplicantTracker = wst;
        this.mNetworkLinkMonitor = new NetworkLinkMonitor(this.mContext, wsm.getMessenger());
        this.mNetworkLinkMonitor.start();
        this.mWifiConfigManager.setWifiNetwork(this);
        this.mAvailableAP = this.mContext.getText(17039585);
        this.mGoodAvailableAP = this.mContext.getText(17039584);
        setupNetworkReceiver();
        this.mHandler = new H(wsm.getHandler().getLooper());
    }

    private void setupNetworkReceiver() {
        IntentFilter netWorkFilter = new IntentFilter();
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_CONNECT);
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_STATE);
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_AVAILABLE);
        netWorkFilter.addAction(ACTION_WIFI_NETWORK_NOT_AVAILABLE);
        netWorkFilter.addAction("android.net.wifi.STATE_CHANGE");
        netWorkFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        netWorkFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        netWorkFilter.addAction("android.intent.action.SCREEN_ON");
        netWorkFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (OppoWifiNetworkSwitchEnhance.this.mFeature) {
                    OppoWifiNetworkSwitchEnhance.this.logD("get----action: " + action);
                    if (action.equals(OppoWifiNetworkSwitchEnhance.ACTION_WIFI_NETWORK_CONNECT)) {
                        WifiInfo actionConnectInfo = OppoWifiNetworkSwitchEnhance.this.mWifiStateMachine.syncRequestConnectionInfo(OppoWifiNetworkSwitchEnhance.WIFI_PACKEG_NAME);
                        String curSsid = intent.getStringExtra("ssid");
                        if (actionConnectInfo != null && curSsid != null && (curSsid.equals(actionConnectInfo.getSSID()) ^ 1) == 0) {
                            OppoWifiNetworkSwitchEnhance.this.logD("conn ssid" + curSsid + ", current ssid: " + actionConnectInfo.getSSID());
                            OppoWifiNetworkSwitchEnhance.this.mIsSoftAP = OppoWifiNetworkSwitchEnhance.this.isSoftAp((LinkProperties) intent.getExtra(OppoWifiNetworkSwitchEnhance.EXTRA_WIFI_LINK));
                            OppoWifiNetworkSwitchEnhance.this.mLastSSID = curSsid;
                        } else {
                            return;
                        }
                    } else if (action.equals(OppoWifiNetworkSwitchEnhance.ACTION_WIFI_NETWORK_STATE)) {
                        OppoWifiNetworkSwitchEnhance.this.mHandler.sendMessage(OppoWifiNetworkSwitchEnhance.this.mHandler.obtainMessage(2000, intent));
                    } else if (!(action.equals("EVENT_NETWORK_AVAILABLE") || action.equals("EVENT_NETWORK_NOT_AVAILABLE"))) {
                        if (action.equals("android.net.wifi.STATE_CHANGE")) {
                            OppoWifiNetworkSwitchEnhance.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                            if ((OppoWifiNetworkSwitchEnhance.this.mNetworkInfo == null || OppoWifiNetworkSwitchEnhance.this.mNetworkInfo.getDetailedState() != DetailedState.CONNECTED) && OppoWifiNetworkSwitchEnhance.this.mCaptivePortal) {
                                OppoWifiNetworkSwitchEnhance.this.mCaptivePortal = false;
                            }
                            OppoWifiNetworkSwitchEnhance.this.mNetworkLinkMonitor.sendMessage(OppoWifiNetworkSwitchEnhance.EVENT_NETWORK_STATE_CHANGE, intent);
                        } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                            if (intent.getIntExtra("wifi_state", 1) == 1) {
                                OppoWifiNetworkSwitchEnhance.this.mInitAutoConnect = true;
                                OppoWifiNetworkSwitchEnhance.this.mIsSoftAP = false;
                            }
                        } else if (action.equals("android.intent.action.SCREEN_ON")) {
                            OppoWifiNetworkSwitchEnhance.this.mNetworkLinkMonitor.sendMessage(OppoWifiNetworkSwitchEnhance.EVENT_SCREEN_ON);
                        } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                            OppoWifiNetworkSwitchEnhance.this.mNetworkLinkMonitor.sendMessage(OppoWifiNetworkSwitchEnhance.EVENT_SCREEN_OFF);
                        }
                    }
                    return;
                }
                OppoWifiNetworkSwitchEnhance.this.logD("mf dis");
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, netWorkFilter);
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mDebug = true;
        } else {
            this.mDebug = false;
        }
    }

    public void reportRssi() {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_DETECT_RSSI, 0, 0));
    }

    public void removeNetwork(int netId) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(EVENT_REOVE_UPDATE_NETWORK, Integer.valueOf(netId)));
    }

    public void setManualConnect(boolean isManualConnect) {
        this.mManualConnect = isManualConnect;
    }

    private boolean getIsOppoManuConnect() {
        if (OppoManuConnectManager.getInstance() != null) {
            return OppoManuConnectManager.getInstance().isManuConnect();
        }
        return false;
    }

    public void setAutoSwitch(boolean isAutoSwitch) {
        logD("setAutoSwitch: " + isAutoSwitch);
        this.mWifiConfigManager.setWifiAutoSwitch(isAutoSwitch);
        this.mAutoSwitch = isAutoSwitch;
    }

    public void setFeature(boolean enable) {
        logD("sf=" + enable);
        this.mFeature = enable;
        if (this.mFeature) {
            this.mWifiConfigManager.setWifiNetwork(this);
            return;
        }
        if (this.mBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        }
        this.mWifiConfigManager.setWifiNetwork(null);
    }

    public void detectScanResult(long time) {
        Object obj = null;
        updateSortConfigByRssi();
        this.mLastScanTime = time;
        WifiInfo scanCurrentInfo = this.mWifiStateMachine.syncRequestConnectionInfo(WIFI_PACKEG_NAME);
        String currentSsid = scanCurrentInfo != null ? scanCurrentInfo.getSSID() : " ";
        if (this.mWifiStateMachine == null || this.mWifiStateMachine.syncGetWifiState() != 3) {
            logE("wifi is not enable.");
        } else if (!this.mIsSoftAP) {
            if (this.mNetworkInfo == null || this.mNetworkInfo.getDetailedState() != DetailedState.CONNECTED || scanCurrentInfo == null || !detectSSID(currentSsid, scanCurrentInfo.getNetworkId())) {
                StringBuilder append = new StringBuilder().append("detectScanResult currentSsid: ").append(currentSsid).append(",DetailedState: ");
                if (this.mNetworkInfo != null) {
                    obj = this.mNetworkInfo.getDetailedState();
                }
                logD(append.append(obj).toString());
                autoConnectAP(2, -100, currentSsid);
                return;
            }
            logD("current ssid: " + scanCurrentInfo.getSSID());
        }
    }

    public List<WifiConfiguration> getValidSortConfigByRssi() {
        List<WifiConfiguration> mSortConfig = new ArrayList();
        synchronized (this.mSortWifiConfig) {
            for (WifiConfiguration config : this.mSortWifiConfig) {
                mSortConfig.add(new WifiConfiguration(config));
            }
        }
        return mSortConfig;
    }

    private void updateSortConfigByRssi() {
        List<ScanResult> currentScan = this.mWifiStateMachine.syncGetScanResultsList();
        List<NetworkAvailableConfig> mSortValidConfig = new ArrayList();
        synchronized (this.mNetworkAvailables) {
            for (NetworkAvailableConfig nc : this.mNetworkAvailables) {
                int referRssi = WifiConfiguration.INVALID_RSSI;
                for (ScanResult result : currentScan) {
                    String ssid = "\"" + result.SSID + "\"";
                    String bssid = result.BSSID;
                    String capabilitie = result.capabilities;
                    if (ssid.equals(nc.mSsid)) {
                        if (matchKeymgmt(nc.mKeymgmt, capabilitie) && result.level > referRssi) {
                            referRssi = result.level;
                        }
                    }
                }
                nc.mRssi = referRssi;
                if (referRssi != WifiConfiguration.INVALID_RSSI) {
                    mSortValidConfig.add(nc);
                }
            }
        }
        Collections.sort(mSortValidConfig, new Comparator<NetworkAvailableConfig>() {
            public int compare(NetworkAvailableConfig b1, NetworkAvailableConfig b2) {
                return b2.mRssi - b1.mRssi;
            }
        });
        synchronized (this.mSortWifiConfig) {
            this.mSortWifiConfig.clear();
            for (NetworkAvailableConfig ms1 : mSortValidConfig) {
                String configKey = getConfigKey(ms1);
                WifiConfiguration temp = this.mWifiConfigManager.getConfiguredNetwork(configKey);
                if (temp != null) {
                    logD("getSortNetwokConfByRssi temp = " + configKey + ", id=" + temp.networkId + ", state= " + temp.status);
                    this.mSortWifiConfig.add(temp);
                }
            }
        }
    }

    private void detectRssi(boolean hasLossPkt) {
        WifiInfo rssiCurrentInfo = this.mWifiStateMachine.syncRequestConnectionInfo(WIFI_PACKEG_NAME);
        long nowMs = System.currentTimeMillis();
        if (rssiCurrentInfo != null) {
            int currentId = rssiCurrentInfo.getNetworkId();
            int currentRssi = rssiCurrentInfo.getRssi();
            this.mNewSsid = rssiCurrentInfo.getSSID();
            this.mNewBssid = rssiCurrentInfo.getBSSID();
            if (this.mNewSsid != null && (detectSSID(this.mNewSsid, currentId) ^ 1) == 0) {
                logD("detectRssi: currentRssi= " + currentRssi + ",mNewSsid= " + this.mNewSsid + ", mNewBssid= " + this.mNewBssid + ",mOldSsid= " + this.mOldSsid + ", mOldBssid= " + this.mOldBssid);
                if (this.mOldSsid != null && this.mNewSsid != null && (this.mNewSsid.equals(this.mOldSsid) ^ 1) != 0) {
                    this.mOldSsid = this.mNewSsid;
                    this.mOldBssid = this.mNewBssid;
                    this.mDetectTime = 0;
                    this.mLossPktTime = 0;
                    this.mHandler.removeMessages(EVENT_AUTO_CONNECT_AP);
                } else if (!(this.mOldBssid == null || this.mNewBssid == null || (this.mNewBssid.equals(this.mOldBssid) ^ 1) == 0)) {
                    this.mOldBssid = this.mNewBssid;
                    this.mDetectTime = 0;
                    this.mLossPktTime = 0;
                    this.mHandler.removeMessages(EVENT_AUTO_CONNECT_AP);
                }
                if (currentRssi >= LOW_RSSI || currentRssi <= -127) {
                    this.mHandler.removeMessages(EVENT_AUTO_CONNECT_AP);
                    this.mDetectTime = 0;
                } else {
                    this.mDetectTime++;
                }
                if (hasLossPkt) {
                    this.mLossPktTime++;
                } else {
                    this.mLossPktTime = 0;
                }
                if ((this.mDetectTime > 1 || this.mLossPktTime > 1) && nowMs - this.mLastScanTime > SCAN_RESULT_AGE) {
                    this.mWifiStateMachine.startScan(OppoManuConnectManager.UID_DEFAULT, -1, null, WifiStateMachine.WIFI_WORK_SOURCE);
                }
                if (this.mDetectTime > 2 || this.mLossPktTime > 2) {
                    if (detectInEss(this.mNewSsid, this.mNewBssid, currentId, currentRssi)) {
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(EVENT_AUTO_CONNECT_AP, 1, currentRssi, this.mNewSsid), 5000);
                    } else {
                        autoConnectAP(1, currentRssi, this.mNewSsid);
                    }
                }
            }
        }
    }

    private boolean detectInEss(String ssid, String bssid, int netId, int rssi) {
        boolean willRoam = false;
        String key = " ";
        List<ScanResult> scanList = this.mWifiStateMachine.syncGetScanResultsList();
        if (ssid == null || scanList == null) {
            return false;
        }
        for (ScanResult result : scanList) {
            String scanSsid = "\"" + result.SSID + "\"";
            String scanBssid = result.BSSID;
            int delta = result.level - rssi;
            if (scanSsid.equals(ssid) && delta > 5 && (scanBssid.equals(bssid) ^ 1) != 0) {
                if (this.mNetworkAvailables.size() > 0) {
                    synchronized (this.mNetworkAvailables) {
                        for (NetworkAvailableConfig network : this.mNetworkAvailables) {
                            if (network.mSsid.equals(ssid) && network.mNetid == netId) {
                                key = network.mKeymgmt;
                                break;
                            }
                        }
                    }
                }
                if (matchKeymgmt(key, result.capabilities)) {
                    willRoam = true;
                    break;
                }
            }
        }
        logD("detectInEss: willRoam " + willRoam);
        return willRoam;
    }

    private boolean detectSSID(String ssid, int netId) {
        boolean contain = false;
        if (ssid == null) {
            return false;
        }
        WifiConfiguration detectconfig = this.mWifiConfigManager.getConfiguredNetwork(netId);
        if (detectconfig == null) {
            logE("get [id:" + netId + "] config is null");
            return false;
        }
        synchronized (this.mNetworkAvailables) {
            if (this.mNetworkAvailables.size() > 0) {
                for (NetworkAvailableConfig network : this.mNetworkAvailables) {
                    if (network.mSsid.equals(ssid) && detectconfig.SSID != null && network.mSsid.equals(detectconfig.SSID) && network.mKeymgmt.equals(parseKeymgmt(detectconfig))) {
                        contain = true;
                        break;
                    }
                }
            }
        }
        logD("detectSSID: return = " + contain);
        return contain;
    }

    private WifiConfiguration getWifiConfig(String ssid, String bssid) {
        if (ssid == null || bssid == null) {
            return null;
        }
        WifiConfiguration currentconfig = null;
        for (ScanResult result : this.mWifiStateMachine.syncGetScanResultsList()) {
            String scanSsid = "\"" + result.SSID + "\"";
            String scanBssid = result.BSSID;
            String capabilitie = result.capabilities;
            if (scanSsid.equals(ssid) && scanBssid.equals(bssid)) {
                String configKey = WifiConfiguration.configKey(result);
                logE("getWifiConfig configKey= " + configKey);
                currentconfig = this.mWifiConfigManager.getConfiguredNetwork(configKey);
                break;
            }
        }
        if (currentconfig != null) {
            logD("getWifiConfig currentconfig: " + currentconfig.networkId + ",SSID:" + currentconfig.SSID + ",BSSID:" + currentconfig.BSSID);
            if (currentconfig.BSSID == null || currentconfig.BSSID.equals("any")) {
                currentconfig.BSSID = bssid;
            }
        }
        return currentconfig;
    }

    private void updateNetworkAvailables(WifiConfiguration wfg, int rssi, boolean add) {
        List<NetworkAvailableConfig> updateNetworks = new ArrayList();
        boolean remove = false;
        if (wfg == null) {
            logE("wfg is null, do nothing");
        } else if (wfg.SSID == null || wfg.BSSID == null) {
            logE("wfg SSID: " + wfg.SSID + " BSSID: " + wfg.BSSID);
        } else {
            synchronized (this.mNetworkAvailables) {
                for (NetworkAvailableConfig network : this.mNetworkAvailables) {
                    if (wfg.SSID.equals(network.mSsid) && parseKeymgmt(wfg).equals(network.mKeymgmt)) {
                        updateNetworks.add(network);
                    }
                }
                for (NetworkAvailableConfig mUpdateNetworks : updateNetworks) {
                    this.mNetworkAvailables.remove(mUpdateNetworks);
                    remove = true;
                }
                updateNetworks.clear();
                logD("remove networks= " + remove + ", add networks = " + add);
                if (remove || (add ^ 1) == 0) {
                    if (add) {
                        WifiConfiguration networks = this.mWifiConfigManager.getConfiguredNetwork(wfg.configKey());
                        if (networks != null) {
                            List list = this.mNetworkAvailables;
                            list.add(new NetworkAvailableConfig(networks.networkId, rssi, networks.SSID, networks.BSSID, parseKeymgmt(networks)));
                        } else {
                            logE("getConfig is null");
                        }
                    }
                    updateAndWriteConfig();
                    return;
                }
            }
        }
    }

    public void removeNetworkAvailable(int netId) {
        List<NetworkAvailableConfig> removeNetworks = new ArrayList();
        WifiConfiguration rmconfig = this.mWifiConfigManager.getConfiguredNetwork(netId);
        if (rmconfig != null) {
            synchronized (this.mNetworkAvailables) {
                if (this.mNetworkAvailables.size() > 0) {
                    for (NetworkAvailableConfig network : this.mNetworkAvailables) {
                        if (rmconfig.SSID != null && network.mSsid.equals(rmconfig.SSID) && network.mKeymgmt.equals(parseKeymgmt(rmconfig))) {
                            removeNetworks.add(network);
                        }
                    }
                    for (NetworkAvailableConfig mRemoveNetworks : removeNetworks) {
                        logD("removeNetworkAvailable, network.mSsid= " + mRemoveNetworks.mSsid);
                        this.mNetworkAvailables.remove(mRemoveNetworks);
                    }
                    removeNetworks.clear();
                    updateAndWriteConfig();
                }
            }
        }
    }

    private boolean isSoftAp(LinkProperties lp) {
        if (lp == null) {
            logE("LinkProperties is null, return");
            return false;
        }
        LinkProperties mLp = lp;
        String result = "";
        InetAddress mCurrentGateway = null;
        for (RouteInfo route : lp.getRoutes()) {
            if (route.hasGateway()) {
                mCurrentGateway = route.getGateway();
            }
        }
        if (mCurrentGateway == null) {
            logE("InetAddress getGateway is null, return");
            return false;
        }
        boolean isSoft;
        logD("mCurrentGateway : " + mCurrentGateway.toString());
        if (mCurrentGateway.toString().equals("/192.168.43.1") || mCurrentGateway.toString().equals("/172.20.10.1")) {
            isSoft = true;
        } else {
            isSoft = false;
        }
        return isSoft;
    }

    private String inStream2String(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (true) {
            int len = is.read(buf);
            if (len == -1) {
                return new String(baos.toByteArray());
            }
            baos.write(buf, 0, len);
        }
    }

    private void autoConnectAP(int autoType, int rssi, String currentssid) {
        int mNetworkConfigLen = this.mNetworkAvailables.size();
        int bestRssi = -100;
        int connectNetid = -1;
        int currentNetid = -1;
        boolean available = autoType == 1;
        List<ScanResult> scanResultsList = this.mWifiStateMachine.syncGetScanResultsList();
        if (!this.mAutoSwitch || (this.mFeature ^ 1) != 0) {
            logE("mAutoSwitch is off!");
        } else if (scanResultsList != null && this.mWifiStateMachine != null && this.mWifiStateMachine.syncGetWifiState() == 3) {
            WifiInfo autoConnectCurrentInfo = this.mWifiStateMachine.syncRequestConnectionInfo(WIFI_PACKEG_NAME);
            if (autoConnectCurrentInfo != null) {
                currentNetid = autoConnectCurrentInfo.getNetworkId();
            }
            logE("autoConnectAP: " + (available ? "switch GOOD AP" : "switch VALID AP") + ",currentNetid = " + currentNetid);
            if (this.mAutoSwitch) {
                synchronized (this.mNetworkAvailables) {
                    String ssid;
                    String bssid;
                    String capabilitie;
                    StringBuilder append;
                    String str;
                    if (available) {
                        for (NetworkAvailableConfig nc : this.mNetworkAvailables) {
                            if (currentssid == null || !currentssid.equals(nc.mSsid) || currentNetid != nc.mNetid) {
                                for (ScanResult result : scanResultsList) {
                                    ssid = "\"" + result.SSID + "\"";
                                    bssid = result.BSSID;
                                    capabilitie = result.capabilities;
                                    if (ssid.equals(nc.mSsid) && matchKeymgmt(nc.mKeymgmt, capabilitie) && result.level > bestRssi && result.level - rssi > 5) {
                                        String aConfigKey = WifiConfiguration.configKey(result);
                                        WifiConfiguration aConfig = this.mWifiConfigManager.getConfiguredNetwork(aConfigKey);
                                        append = new StringBuilder().append("a compare config key:").append(aConfigKey).append(",aConfig: ");
                                        if (aConfig != null) {
                                            str = aConfig.SSID + ",status = " + aConfig.status + ",id= " + aConfig.networkId;
                                        } else {
                                            str = "null";
                                        }
                                        logD(append.append(str).toString());
                                        if (!(aConfig == null || aConfig.SSID == null)) {
                                            if (ssid.equals(aConfig.SSID) && aConfig.status != 1) {
                                                bestRssi = result.level;
                                                connectNetid = aConfig.networkId;
                                            }
                                        }
                                    }
                                }
                                continue;
                            }
                        }
                    } else {
                        for (NetworkAvailableConfig nc2 : this.mNetworkAvailables) {
                            for (ScanResult result2 : scanResultsList) {
                                ssid = "\"" + result2.SSID + "\"";
                                bssid = result2.BSSID;
                                capabilitie = result2.capabilities;
                                if (ssid.equals(nc2.mSsid) && matchKeymgmt(nc2.mKeymgmt, capabilitie) && result2.level > bestRssi) {
                                    String uConfigKey = WifiConfiguration.configKey(result2);
                                    WifiConfiguration uConfig = this.mWifiConfigManager.getConfiguredNetwork(uConfigKey);
                                    append = new StringBuilder().append("u compare config key:").append(uConfigKey).append(",uConfig: ");
                                    if (uConfig != null) {
                                        str = uConfig.SSID + ",status = " + uConfig.status + ",id= " + uConfig.networkId;
                                    } else {
                                        str = "null";
                                    }
                                    logD(append.append(str).toString());
                                    if (!(uConfig == null || uConfig.SSID == null)) {
                                        if (ssid.equals(uConfig.SSID) && uConfig.status != 1) {
                                            bestRssi = result2.level;
                                            connectNetid = uConfig.networkId;
                                        }
                                    }
                                }
                            }
                        }
                        if (connectNetid == -1 && autoType == 0) {
                            logE("ALERT! this is a ap cannot acess internet");
                            showDialog(currentssid);
                        }
                    }
                }
            } else {
                logE("mAutoSwitch is off!");
            }
            if (connectNetid == -1 && (this.mInitAutoConnect || currentNetid == -1)) {
                List<WifiConfiguration> mWificonfiguration = this.mWifiConfigManager.getSavedNetworks();
                List<WifiConfiguration> mToBeEnabledConfiguration = new ArrayList();
                boolean mAllDisabled = true;
                if (!(getIsOppoManuConnect() || mWificonfiguration == null || mWificonfiguration.size() <= 0)) {
                    for (ScanResult sr : scanResultsList) {
                        for (WifiConfiguration wc : mWificonfiguration) {
                            if (WifiConfiguration.configKey(sr).equals(wc.configKey())) {
                                if (wc.status != 1) {
                                    mAllDisabled = false;
                                    break;
                                }
                                int level = this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_GOOD_RSSI_SWITCH_VALUE", Integer.valueOf(GOOD_RSSI_SWITCH_VALUE)).intValue();
                                logD("switch value=" + level + ",sr.level=" + sr.level);
                                if (!(wc.disableReason == 3 || wc.disableReason == 12 || sr.level < level)) {
                                    mToBeEnabledConfiguration.add(wc);
                                }
                            }
                        }
                    }
                    if (mAllDisabled) {
                        logD("all configured network are disabled!");
                        if (mToBeEnabledConfiguration != null && mToBeEnabledConfiguration.size() > 0) {
                            logD("enabled networks that not disabled by wrongkey or auth failure!");
                            for (WifiConfiguration wc2 : mToBeEnabledConfiguration) {
                                logD("enable netId:" + wc2.networkId + " SSID:" + wc2.SSID);
                                this.mWifiConfigManager.enableNetwork(wc2.networkId, false, OppoManuConnectManager.UID_DEFAULT);
                            }
                        }
                    }
                }
                this.mWifiNative.reconnect();
                this.mInitAutoConnect = false;
            }
            if (connectNetid != -1 && currentNetid != connectNetid) {
                if (getIsOppoManuConnect()) {
                    logE("manual connect, do not auto connect");
                    return;
                }
                logE("auto conntect id:" + connectNetid);
                WifiConfiguration selectConf = this.mWifiConfigManager.getConfiguredNetwork(connectNetid);
                if (selectConf == null) {
                    logE("select config is null");
                } else if (this.mWifiStateMachine.isNetworkAutoConnectingOrConnected(selectConf.networkId)) {
                    logD("network: " + selectConf.SSID + " is connecting or connected, do nothing!");
                } else {
                    this.mWifiConfigManager.enableNetwork(selectConf.networkId, false, OppoManuConnectManager.UID_DEFAULT);
                    this.mWifiStateMachine.prepareForForcedConnection(selectConf.networkId);
                    this.mWifiStateMachine.startConnectToNetwork(selectConf.networkId, OppoManuConnectManager.UID_DEFAULT, "any");
                    this.mSupplicantTracker.sendMessage(131372);
                }
            }
        }
    }

    private void showDialog(String ssid) {
        logD("showDialog mUnavailableSsid: " + this.mUnavailableSsid);
        if (this.mUnavailableSsid != null && this.mUnavailableSsid.equals(ssid)) {
            logE("name[" + ssid + "] is same");
        } else if (this.mNetworkInfo == null || this.mNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
            this.mUnavailableSsid = ssid;
            if (this.mAlertDialog != null) {
                this.mAlertDialog.dismiss();
            }
            this.mAlertDialog = new Builder(this.mContext).setTitle(ssid + this.mContext.getText(17041116)).setMessage(this.mContext.getText(17039584)).setPositiveButton(17041115, new OnClickListener() {
                public void onClick(DialogInterface d, int w) {
                    if (OppoWifiNetworkSwitchEnhance.this.mAlertDialog != null) {
                        OppoWifiNetworkSwitchEnhance.this.mAlertDialog.dismiss();
                    }
                }
            }).create();
            this.mAlertDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    OppoWifiNetworkSwitchEnhance.this.mAlertDialog = null;
                }
            });
            this.mAlertDialog.getWindow().setType(EVENT_REOVE_UPDATE_NETWORK);
            this.mAlertDialog.getWindow().addFlags(2);
            this.mAlertDialog.show();
            TextView msg = (TextView) this.mAlertDialog.findViewById(16908299);
            if (msg != null) {
                msg.setGravity(17);
            } else {
                logE("textview is null");
            }
        } else {
            logD("not CONNECTED,so shouldn't showDialog for the ap can't go internet");
        }
    }

    private boolean matchKeymgmt(String validKey, String scanKey) {
        boolean match = false;
        if (validKey == null || scanKey == null) {
            return false;
        }
        if (validKey.equals(SECURITY_PSK) || validKey.equals(SECURITY_WPA2_PSK)) {
            if (scanKey.contains("WPA-PSK") || scanKey.contains("WPA2-PSK")) {
                match = true;
            }
        } else if (validKey.equals(SECURITY_EAP)) {
            if (scanKey.contains("EAP")) {
                match = true;
            }
        } else if (validKey.equals("IEEE8021X")) {
            if (scanKey.contains("IEEE8021X")) {
                match = true;
            }
        } else if (validKey.equals(SECURITY_WAPI_PSK)) {
            if (scanKey.contains("WAPI-KEY") || scanKey.contains("WAPI-PSK")) {
                match = true;
            }
        } else if (validKey.equals(SECURITY_WAPI_CERT)) {
            if (scanKey.contains("WAPI-CERT")) {
                match = true;
            }
        } else if (validKey.equals(SECURITY_WEP)) {
            if (scanKey.contains(SECURITY_WEP)) {
                match = true;
            }
        } else if (!validKey.equals(SECURITY_NONE)) {
            logE("matchKeymgmt default");
        } else if (!(scanKey.contains("PSK") || (scanKey.contains("EAP") ^ 1) == 0 || (scanKey.contains(SECURITY_WEP) ^ 1) == 0 || (scanKey.contains("WAPI") ^ 1) == 0 || (scanKey.contains("IEEE8021X") ^ 1) == 0)) {
            match = true;
        }
        return match;
    }

    private String parseKeymgmt(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(1)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            return SECURITY_EAP;
        }
        if (config.allowedKeyManagement.get(190)) {
            return SECURITY_WAPI_PSK;
        }
        if (config.allowedKeyManagement.get(191)) {
            return SECURITY_WAPI_CERT;
        }
        if (config.wepTxKeyIndex >= 0 && config.wepTxKeyIndex < config.wepKeys.length && config.wepKeys[config.wepTxKeyIndex] != null) {
            return SECURITY_WEP;
        }
        if (config.allowedKeyManagement.get(4)) {
            return SECURITY_WPA2_PSK;
        }
        return SECURITY_NONE;
    }

    private String getConfigKey(NetworkAvailableConfig nc) {
        if (nc == null) {
            return null;
        }
        String key = nc.mSsid;
        String -get1 = nc.mKeymgmt;
        if (-get1.equals(SECURITY_PSK) || -get1.equals(SECURITY_WPA2_PSK)) {
            key = nc.mSsid + "-" + SECURITY_PSK;
        } else if (-get1.equals(SECURITY_EAP) || -get1.equals("IEEE8021X")) {
            key = nc.mSsid + "-" + SECURITY_EAP;
        } else if (-get1.equals(SECURITY_WAPI_PSK)) {
            key = nc.mSsid + "-" + SECURITY_WAPI_PSK;
        } else if (-get1.equals(SECURITY_WAPI_CERT)) {
            key = nc.mSsid + "-" + SECURITY_WAPI_CERT;
        } else if (-get1.equals(SECURITY_WEP)) {
            key = nc.mSsid + "-" + SECURITY_WEP;
        } else if (-get1.equals(SECURITY_NONE)) {
            key = nc.mSsid + "-" + SECURITY_NONE;
        }
        return key;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0095 A:{SYNTHETIC, Splitter: B:16:0x0095} */
    /* JADX WARNING: Removed duplicated region for block: B:67:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x009a A:{SYNTHETIC, Splitter: B:19:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00cf A:{SYNTHETIC, Splitter: B:36:0x00cf} */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00d4 A:{SYNTHETIC, Splitter: B:39:0x00d4} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0095 A:{SYNTHETIC, Splitter: B:16:0x0095} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x009a A:{SYNTHETIC, Splitter: B:19:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:67:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00cf A:{SYNTHETIC, Splitter: B:36:0x00cf} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00d4 A:{SYNTHETIC, Splitter: B:39:0x00d4} */
    /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00e3 A:{SYNTHETIC, Splitter: B:47:0x00e3} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00e8 A:{SYNTHETIC, Splitter: B:50:0x00e8} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00e3 A:{SYNTHETIC, Splitter: B:47:0x00e3} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00e8 A:{SYNTHETIC, Splitter: B:50:0x00e8} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateAndWriteConfig() {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        FileWriter config = null;
        BufferedWriter out = null;
        try {
            FileWriter config2 = new FileWriter(WIFI_NETWORK_AVAILABLE);
            try {
                BufferedWriter out2 = new BufferedWriter(config2);
                try {
                    for (NetworkAvailableConfig na : this.mNetworkAvailables) {
                        out2.write((na.mNetid + "\t" + na.mBssid + "\t" + na.mKeymgmt + "\t" + na.mSsid) + "\n");
                        out2.flush();
                    }
                    out2.close();
                    if (out2 != null) {
                        try {
                            out2.close();
                        } catch (IOException e3) {
                        }
                    }
                    if (config2 != null) {
                        try {
                            config2.close();
                        } catch (IOException e4) {
                        }
                    }
                } catch (FileNotFoundException e5) {
                    e = e5;
                    out = out2;
                    config = config2;
                    try {
                        logE("FileNotFoundException: " + e);
                        if (out != null) {
                        }
                        if (config != null) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (out != null) {
                        }
                        if (config != null) {
                        }
                        throw th;
                    }
                } catch (IOException e6) {
                    e2 = e6;
                    out = out2;
                    config = config2;
                    logE("IOException: " + e2);
                    e2.printStackTrace();
                    if (out != null) {
                    }
                    if (config != null) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    out = out2;
                    config = config2;
                    if (out != null) {
                    }
                    if (config != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e7) {
                e = e7;
                config = config2;
                logE("FileNotFoundException: " + e);
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e8) {
                    }
                }
                if (config != null) {
                    try {
                        config.close();
                    } catch (IOException e9) {
                    }
                }
            } catch (IOException e10) {
                e2 = e10;
                config = config2;
                logE("IOException: " + e2);
                e2.printStackTrace();
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e11) {
                    }
                }
                if (config != null) {
                    try {
                        config.close();
                    } catch (IOException e12) {
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                config = config2;
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e13) {
                    }
                }
                if (config != null) {
                    try {
                        config.close();
                    } catch (IOException e14) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e15) {
            e = e15;
            logE("FileNotFoundException: " + e);
            if (out != null) {
            }
            if (config != null) {
            }
        } catch (IOException e16) {
            e2 = e16;
            logE("IOException: " + e2);
            e2.printStackTrace();
            if (out != null) {
            }
            if (config != null) {
            }
        }
    }

    /* JADX WARNING: Missing block: B:50:0x0164, code:
            if (r0 == null) goto L_0x0169;
     */
    /* JADX WARNING: Missing block: B:52:?, code:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:64:0x0179, code:
            throw r2;
     */
    /* JADX WARNING: Missing block: B:66:0x017c, code:
            r2 = th;
     */
    /* JADX WARNING: Missing block: B:67:0x017d, code:
            r20 = r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void readConfigAndUpdate() {
        BufferedReader bufferedReader;
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        BufferedReader reader = null;
        if (this.mNetworkAvailables == null) {
            logE("networkAvailables exception, re-init it");
            this.mNetworkAvailables = new ArrayList();
        }
        synchronized (this.mNetworkAvailables) {
            try {
                this.mNetworkAvailables.clear();
                try {
                    bufferedReader = new BufferedReader(new FileReader(WIFI_NETWORK_AVAILABLE));
                    try {
                        String str = "";
                        while (true) {
                            str = bufferedReader.readLine();
                            if (str == null) {
                                break;
                            }
                            if (str.split("\t").length == 4) {
                                int idIndex = str.indexOf(9);
                                if (idIndex >= 0) {
                                    int id = -1;
                                    try {
                                        id = Integer.parseInt(str.substring(0, idIndex));
                                    } catch (NumberFormatException e3) {
                                        logD("NumberFormatException e:" + e3);
                                    }
                                    String bssidString = str.substring(idIndex + 1, str.length());
                                    int bssidIndex = bssidString.indexOf(9);
                                    if (bssidIndex >= 0) {
                                        String bssid = bssidString.substring(0, bssidIndex);
                                        String keymgmtString = bssidString.substring(bssidIndex + 1, bssidString.length());
                                        int keymgmtIndex = keymgmtString.indexOf(9);
                                        if (keymgmtIndex >= 0) {
                                            if (!keymgmtString.startsWith("\"")) {
                                                String keyMgmt = keymgmtString.substring(0, keymgmtIndex);
                                                String ssid = keymgmtString.substring(keymgmtIndex + 1, keymgmtString.length());
                                                logD("readConfigAndUpdate: id= " + id + ",bssid= " + bssid + ",keyMgmt= " + keyMgmt + ",ssid= " + ssid);
                                                List list = this.mNetworkAvailables;
                                                list.add(new NetworkAvailableConfig(id, WifiConfiguration.INVALID_RSSI, ssid, bssid, keyMgmt));
                                            }
                                        }
                                    }
                                } else {
                                    continue;
                                }
                            }
                        }
                    } catch (FileNotFoundException e4) {
                        e = e4;
                        reader = bufferedReader;
                    } catch (IOException e5) {
                        e2 = e5;
                        reader = bufferedReader;
                    } catch (Throwable th2) {
                        th = th2;
                        reader = bufferedReader;
                    }
                } catch (FileNotFoundException e6) {
                    e = e6;
                    try {
                        logE("readConfigAndUpdate: FileNotFoundException: " + e);
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e7) {
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e8) {
                            }
                        }
                        throw th;
                    }
                } catch (IOException e9) {
                    e2 = e9;
                    logE("readConfigAndUpdate: IOException: " + e2);
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e10) {
                        }
                    }
                }
            } catch (Throwable th4) {
                th = th4;
            }
        }
        reader = bufferedReader;
    }

    private void logD(String log) {
        if (this.mDebug) {
            Log.d(TAG, "" + log);
        }
    }

    private void logE(String log) {
        Log.e(TAG, "" + log);
    }
}
