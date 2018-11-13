package android.net.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkRequest.Builder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.util.AsyncChannel;
import com.android.server.net.NetworkPinner;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public class WifiManager {
    public static final String ACTION_PICK_WIFI_NETWORK = "android.net.wifi.PICK_WIFI_NETWORK";
    public static final String ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE = "android.net.wifi.action.REQUEST_SCAN_ALWAYS_AVAILABLE";
    private static final int BASE = 151552;
    @Deprecated
    public static final String BATCHED_SCAN_RESULTS_AVAILABLE_ACTION = "android.net.wifi.BATCHED_RESULTS";
    public static final int BUSY = 2;
    public static final int CANCEL_WPS = 151566;
    public static final int CANCEL_WPS_FAILED = 151567;
    public static final int CANCEL_WPS_SUCCEDED = 151568;
    public static final int CHANGE_REASON_ADDED = 0;
    public static final int CHANGE_REASON_CONFIG_CHANGE = 2;
    public static final int CHANGE_REASON_REMOVED = 1;
    public static final String CONFIGURED_NETWORKS_CHANGED_ACTION = "android.net.wifi.CONFIGURED_NETWORKS_CHANGE";
    public static final int CONNECT_NETWORK = 151553;
    public static final int CONNECT_NETWORK_FAILED = 151554;
    public static final int CONNECT_NETWORK_SUCCEEDED = 151555;
    public static final int DATA_ACTIVITY_IN = 1;
    public static final int DATA_ACTIVITY_INOUT = 3;
    public static final int DATA_ACTIVITY_NONE = 0;
    public static final int DATA_ACTIVITY_NOTIFICATION = 1;
    public static final int DATA_ACTIVITY_OUT = 2;
    public static final boolean DEFAULT_POOR_NETWORK_AVOIDANCE_ENABLED = false;
    public static final int DISABLE_NETWORK = 151569;
    public static final int DISABLE_NETWORK_FAILED = 151570;
    public static final int DISABLE_NETWORK_SUCCEEDED = 151571;
    public static final int ERROR = 0;
    public static final int ERROR_AUTHENTICATING = 1;
    public static final String EXTRA_BSSID = "bssid";
    public static final String EXTRA_CHANGE_REASON = "changeReason";
    public static final String EXTRA_HOTSPOT_CLIENTS_NUM = "HotspotClientNum";
    public static final String EXTRA_LINK_PROPERTIES = "linkProperties";
    public static final String EXTRA_MULTIPLE_NETWORKS_CHANGED = "multipleChanges";
    public static final String EXTRA_NETWORK_CAPABILITIES = "networkCapabilities";
    public static final String EXTRA_NETWORK_INFO = "networkInfo";
    public static final String EXTRA_NEW_RSSI = "newRssi";
    public static final String EXTRA_NEW_STATE = "newState";
    public static final String EXTRA_PASSPOINT_ICON_BSSID = "bssid";
    public static final String EXTRA_PASSPOINT_ICON_DATA = "icon";
    public static final String EXTRA_PASSPOINT_ICON_FILE = "file";
    public static final String EXTRA_PASSPOINT_WNM_BSSID = "bssid";
    public static final String EXTRA_PASSPOINT_WNM_DELAY = "delay";
    public static final String EXTRA_PASSPOINT_WNM_ESS = "ess";
    public static final String EXTRA_PASSPOINT_WNM_METHOD = "method";
    public static final String EXTRA_PASSPOINT_WNM_PPOINT_MATCH = "match";
    public static final String EXTRA_PASSPOINT_WNM_URL = "url";
    public static final String EXTRA_PPPOE_ERROR = "pppoe_result_error_code";
    public static final String EXTRA_PPPOE_STATE = "pppoe_state";
    public static final String EXTRA_PPPOE_STATUS = "pppoe_result_status";
    public static final String EXTRA_PREVIOUS_WIFI_AP_STATE = "previous_wifi_state";
    public static final String EXTRA_PREVIOUS_WIFI_STATE = "previous_wifi_state";
    public static final String EXTRA_RESULTS_UPDATED = "resultsUpdated";
    public static final String EXTRA_SCAN_AVAILABLE = "scan_enabled";
    public static final String EXTRA_SUPPLICANT_CONNECTED = "connected";
    public static final String EXTRA_SUPPLICANT_ERROR = "supplicantError";
    public static final String EXTRA_TDLS_BSSID = "tdls_bssid";
    public static final String EXTRA_TRIGGERED_BY_NOTIFICATION = "notification";
    public static final String EXTRA_WIFI_AP_FAILURE_REASON = "wifi_ap_error_code";
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
    public static final String EXTRA_WIFI_CONFIGURATION = "wifiConfiguration";
    public static final String EXTRA_WIFI_CREDENTIAL_EVENT_TYPE = "et";
    public static final String EXTRA_WIFI_CREDENTIAL_SSID = "ssid";
    public static final String EXTRA_WIFI_INFO = "wifiInfo";
    public static final String EXTRA_WIFI_OFF_REASON = "wifi_off_reason";
    public static final String EXTRA_WIFI_STATE = "wifi_state";
    public static final int FORGET_NETWORK = 151556;
    public static final int FORGET_NETWORK_FAILED = 151557;
    public static final int FORGET_NETWORK_SUCCEEDED = 151558;
    public static final int GET_CRED_FROM_NFC = 151608;
    public static final int GET_CRED_FROM_NFC_FAILED = 151609;
    public static final int GET_CRED_FROM_NFC_SUCCEEDED = 151610;
    public static final int GET_PIN_FROM_NFC = 151605;
    public static final int GET_PIN_FROM_NFC_FAILED = 151606;
    public static final int GET_PIN_FROM_NFC_SUCCEEDED = 151607;
    public static final int GET_WPS_CRED_AND_CONNECT = 151596;
    public static final int GET_WPS_CRED_AND_CONNECT_FAILED = 151597;
    public static final int GET_WPS_CRED_AND_CONNECT_SUCCEEDED = 151598;
    public static final int GET_WPS_PIN_AND_CONNECT = 151595;
    public static final int INVALID_ARGS = 8;
    private static final int INVALID_KEY = 0;
    public static final int IN_PROGRESS = 1;
    public static final String LINK_CONFIGURATION_CHANGED_ACTION = "android.net.wifi.LINK_CONFIGURATION_CHANGED";
    private static final int MAX_ACTIVE_LOCKS = 50;
    private static final int MAX_RSSI = -55;
    private static final int MIN_RSSI = -100;
    public static final String NETWORK_IDS_CHANGED_ACTION = "android.net.wifi.NETWORK_IDS_CHANGED";
    public static final String NETWORK_STATE_CHANGED_ACTION = "android.net.wifi.STATE_CHANGE";
    public static final String NEW_PAC_UPDATED_ACTION = "android.net.wifi.NEW_PAC_UPDATED";
    public static final int NOT_AUTHORIZED = 9;
    public static final String NO_CERTIFICATION_ACTION = "android.net.wifi.NO_CERTIFICATION";
    private static int OPPO_MAX_RSSI = 0;
    private static int OPPO_MIDDLE1_RSSI = 0;
    private static int OPPO_MIDDLE2_RSSI = 0;
    private static int OPPO_MIN_RSSI = 0;
    public static final String PASSPOINT_ICON_RECEIVED_ACTION = "android.net.wifi.PASSPOINT_ICON_RECEIVED";
    public static final String PASSPOINT_WNM_FRAME_RECEIVED_ACTION = "android.net.wifi.PASSPOINT_WNM_FRAME_RECEIVED";
    public static final String PPPOE_STATE_CONNECTED = "PPPOE_STATE_CONNECTED";
    public static final String PPPOE_STATE_CONNECTING = "PPPOE_STATE_CONNECTING";
    public static final String PPPOE_STATE_DISCONNECTED = "PPPOE_STATE_DISCONNECTED";
    public static final String PPPOE_STATE_DISCONNECTING = "PPPOE_STATE_DISCONNECTING";
    public static final String PPPOE_STATUS_ALREADY_ONLINE = "ALREADY_ONLINE";
    public static final String PPPOE_STATUS_FAILURE = "FAILURE";
    public static final String PPPOE_STATUS_SUCCESS = "SUCCESS";
    public static final String RSSI_CHANGED_ACTION = "android.net.wifi.RSSI_CHANGED";
    public static final int RSSI_LEVELS = 5;
    public static final int RSSI_PKTCNT_FETCH = 151572;
    public static final int RSSI_PKTCNT_FETCH_FAILED = 151574;
    public static final int RSSI_PKTCNT_FETCH_SUCCEEDED = 151573;
    public static final int SAP_START_FAILURE_GENERAL = 0;
    public static final int SAP_START_FAILURE_NO_CHANNEL = 1;
    public static final int SAVE_NETWORK = 151559;
    public static final int SAVE_NETWORK_FAILED = 151560;
    public static final int SAVE_NETWORK_SUCCEEDED = 151561;
    public static final String SCAN_RESULTS_AVAILABLE_ACTION = "android.net.wifi.SCAN_RESULTS";
    public static final int SET_WIFI_NOT_RECONNECT_AND_SCAN = 151612;
    public static final int START_PPPOE = 151575;
    public static final int START_PPPOE_FAILED = 151577;
    public static final int START_PPPOE_SUCCEEDED = 151576;
    public static final int START_WPS = 151562;
    public static final int START_WPS_ER = 151594;
    public static final int START_WPS_REG = 151593;
    public static final int START_WPS_SUCCEEDED = 151563;
    public static final int STOP_PPPOE = 151578;
    public static final int STOP_PPPOE_FAILED = 151580;
    public static final int STOP_PPPOE_SUCCEEDED = 151579;
    public static final String SUPPLICANT_CONNECTION_CHANGE_ACTION = "android.net.wifi.supplicant.CONNECTION_CHANGE";
    public static final String SUPPLICANT_STATE_CHANGED_ACTION = "android.net.wifi.supplicant.STATE_CHANGE";
    private static final String TAG = "WifiManager";
    public static final String TDLS_CONNECTED_ACTION = "android.net.wifi.TDLS_CONNECTED";
    public static final String TDLS_DISCONNECTED_ACTION = "android.net.wifi.TDLS_DISCONNECTED";
    public static final int TOKEN_TYPE_NDEF = 1;
    public static final int TOKEN_TYPE_WPS = 2;
    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_DISABLING = 10;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_FAILED = 14;
    public static final String WIFI_CLEAR_NOTIFICATION_SHOW_FLAG_ACTION = "android.net.wifi.WIFI_CLEAR_NOTIFICATION_SHOW_FLAG_ACTION";
    public static final String WIFI_CREDENTIAL_CHANGED_ACTION = "android.net.wifi.WIFI_CREDENTIAL_CHANGED";
    public static final int WIFI_CREDENTIAL_FORGOT = 1;
    public static final int WIFI_CREDENTIAL_SAVED = 0;
    public static final int WIFI_FEATURE_ADDITIONAL_STA = 2048;
    public static final int WIFI_FEATURE_AP_STA = 32768;
    public static final int WIFI_FEATURE_BATCH_SCAN = 512;
    public static final int WIFI_FEATURE_D2AP_RTT = 256;
    public static final int WIFI_FEATURE_D2D_RTT = 128;
    public static final int WIFI_FEATURE_EPR = 16384;
    public static final int WIFI_FEATURE_HAL_EPNO = 262144;
    public static final int WIFI_FEATURE_INFRA = 1;
    public static final int WIFI_FEATURE_INFRA_5G = 2;
    public static final int WIFI_FEATURE_LINK_LAYER_STATS = 65536;
    public static final int WIFI_FEATURE_LOGGER = 131072;
    public static final int WIFI_FEATURE_MOBILE_HOTSPOT = 16;
    public static final int WIFI_FEATURE_NAN = 64;
    public static final int WIFI_FEATURE_P2P = 8;
    public static final int WIFI_FEATURE_PASSPOINT = 4;
    public static final int WIFI_FEATURE_PNO = 1024;
    public static final int WIFI_FEATURE_SCANNER = 32;
    public static final int WIFI_FEATURE_TDLS = 4096;
    public static final int WIFI_FEATURE_TDLS_OFFCHANNEL = 8192;
    public static final int WIFI_FREQUENCY_BAND_2GHZ = 2;
    public static final int WIFI_FREQUENCY_BAND_5GHZ = 1;
    public static final int WIFI_FREQUENCY_BAND_AUTO = 0;
    public static final String WIFI_HOTSPOT_CLIENTS_CHANGED_ACTION = "android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED";
    public static final String WIFI_HOTSPOT_OVERLAP_ACTION = "android.net.wifi.WIFI_HOTSPOT_OVERLAP";
    public static final int WIFI_MODE_FULL = 1;
    public static final int WIFI_MODE_FULL_HIGH_PERF = 3;
    public static final int WIFI_MODE_NO_LOCKS_HELD = 0;
    public static final int WIFI_MODE_SCAN_ONLY = 2;
    public static final String WIFI_OFF_NOTIFY = "com.mediatek.android.wifi_off_notify";
    public static final String WIFI_PPPOE_COMPLETED_ACTION = "android.net.wifi.PPPOE_COMPLETED_ACTION";
    public static final String WIFI_PPPOE_STATE_CHANGED_ACTION = "android.net.wifi.PPPOE_STATE_CHANGED";
    public static final String WIFI_SCAN_AVAILABLE = "wifi_scan_available";
    private static long WIFI_SIGNAL = 0;
    public static final String WIFI_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_STATE_CHANGED";
    public static final int WIFI_STATE_DISABLED = 1;
    public static final int WIFI_STATE_DISABLING = 0;
    public static final int WIFI_STATE_ENABLED = 3;
    public static final int WIFI_STATE_ENABLING = 2;
    public static final int WIFI_STATE_UNKNOWN = 4;
    public static final String WIFI_WPS_CHECK_PIN_FAIL_ACTION = "android.net.wifi.WIFI_WPS_CHECK_PIN_FAIL";
    public static final int WPS_AUTH_FAILURE = 6;
    public static final int WPS_COMPLETED = 151565;
    public static final int WPS_FAILED = 151564;
    public static final int WPS_INVALID_PIN = 10;
    public static final int WPS_OVERLAP_ERROR = 3;
    public static final int WPS_TIMED_OUT = 7;
    public static final int WPS_TKIP_ONLY_PROHIBITED = 5;
    public static final int WPS_WEP_PROHIBITED = 4;
    public static final int WRITE_CRED_TO_NFC = 151599;
    public static final int WRITE_CRED_TO_NFC_FAILED = 151600;
    public static final int WRITE_CRED_TO_NFC_SUCCEEDED = 151601;
    public static final int WRITE_PIN_TO_NFC = 151602;
    public static final int WRITE_PIN_TO_NFC_FAILED = 151603;
    public static final int WRITE_PIN_TO_NFC_SUCCEEDED = 151604;
    private static final Object sServiceHandlerDispatchLock = null;
    private int mActiveLockCount;
    private AsyncChannel mAsyncChannel;
    private CountDownLatch mConnected;
    private Context mContext;
    private int mListenerKey;
    private final SparseArray mListenerMap;
    private final Object mListenerMapLock;
    private Looper mLooper;
    IWifiManager mService;
    private final int mTargetSdkVersion;
    private ArrayList<WifiOffListener> mWifiOffListenerList;
    BroadcastReceiver mWifiOffNotifyReceiver;

    /* renamed from: android.net.wifi.WifiManager$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ WifiManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.WifiManager.1.<init>(android.net.wifi.WifiManager):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(android.net.wifi.WifiManager r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.WifiManager.1.<init>(android.net.wifi.WifiManager):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiManager.1.<init>(android.net.wifi.WifiManager):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.WifiManager.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.WifiManager.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiManager.1.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    public interface ActionListener {
        void onFailure(int i);

        void onSuccess();
    }

    public class MulticastLock {
        private final IBinder mBinder;
        private boolean mHeld;
        private int mRefCount;
        private boolean mRefCounted;
        private String mTag;
        final /* synthetic */ WifiManager this$0;

        /* synthetic */ MulticastLock(WifiManager this$0, String tag, MulticastLock multicastLock) {
            this(this$0, tag);
        }

        private MulticastLock(WifiManager this$0, String tag) {
            this.this$0 = this$0;
            this.mTag = tag;
            this.mBinder = new Binder();
            this.mRefCount = 0;
            this.mRefCounted = true;
            this.mHeld = false;
        }

        /* JADX WARNING: Missing block: B:6:0x000e, code:
            if (r1 == 1) goto L_0x0010;
     */
        /* JADX WARNING: Missing block: B:28:0x0046, code:
            if (r5.mHeld != false) goto L_0x0048;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void acquire() {
            synchronized (this.mBinder) {
                if (this.mRefCounted) {
                    int i = this.mRefCount + 1;
                    this.mRefCount = i;
                }
                try {
                    this.this$0.mService.acquireMulticastLock(this.mBinder, this.mTag);
                    synchronized (this.this$0) {
                        if (this.this$0.mActiveLockCount >= 50) {
                            this.this$0.mService.releaseMulticastLock();
                            throw new UnsupportedOperationException("Exceeded maximum number of wifi locks");
                        }
                        WifiManager wifiManager = this.this$0;
                        wifiManager.mActiveLockCount = wifiManager.mActiveLockCount + 1;
                    }
                    this.mHeld = true;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }

        /* JADX WARNING: Missing block: B:6:0x000d, code:
            if (r1 == 0) goto L_0x000f;
     */
        /* JADX WARNING: Missing block: B:26:0x004d, code:
            if (r5.mHeld != false) goto L_0x000f;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void release() {
            synchronized (this.mBinder) {
                if (this.mRefCounted) {
                    int i = this.mRefCount - 1;
                    this.mRefCount = i;
                }
                try {
                    this.this$0.mService.releaseMulticastLock();
                    synchronized (this.this$0) {
                        WifiManager wifiManager = this.this$0;
                        wifiManager.mActiveLockCount = wifiManager.mActiveLockCount - 1;
                    }
                    this.mHeld = false;
                    if (this.mRefCount < 0) {
                        throw new RuntimeException("MulticastLock under-locked " + this.mTag);
                    }
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }

        public void setReferenceCounted(boolean refCounted) {
            this.mRefCounted = refCounted;
        }

        public boolean isHeld() {
            boolean z;
            synchronized (this.mBinder) {
                z = this.mHeld;
            }
            return z;
        }

        public String toString() {
            String str;
            synchronized (this.mBinder) {
                String s3;
                String s1 = Integer.toHexString(System.identityHashCode(this));
                String s2 = this.mHeld ? "held; " : "";
                if (this.mRefCounted) {
                    s3 = "refcounted: refcount = " + this.mRefCount;
                } else {
                    s3 = "not refcounted";
                }
                str = "MulticastLock{ " + s1 + "; " + s2 + s3 + " }";
            }
            return str;
        }

        protected void finalize() throws Throwable {
            super.finalize();
            setReferenceCounted(false);
            release();
        }
    }

    private class ServiceHandler extends Handler {
        final /* synthetic */ WifiManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.WifiManager.ServiceHandler.<init>(android.net.wifi.WifiManager, android.os.Looper):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        ServiceHandler(android.net.wifi.WifiManager r1, android.os.Looper r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.wifi.WifiManager.ServiceHandler.<init>(android.net.wifi.WifiManager, android.os.Looper):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiManager.ServiceHandler.<init>(android.net.wifi.WifiManager, android.os.Looper):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.WifiManager.ServiceHandler.dispatchMessageToListeners(android.os.Message):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private void dispatchMessageToListeners(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.wifi.WifiManager.ServiceHandler.dispatchMessageToListeners(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiManager.ServiceHandler.dispatchMessageToListeners(android.os.Message):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.WifiManager.ServiceHandler.handleMessage(android.os.Message):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        public void handleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.WifiManager.ServiceHandler.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiManager.ServiceHandler.handleMessage(android.os.Message):void");
        }
    }

    public interface TxPacketCountListener {
        void onFailure(int i);

        void onSuccess(int i);
    }

    public class WifiLock {
        private final IBinder mBinder;
        private boolean mHeld;
        int mLockType;
        private int mRefCount;
        private boolean mRefCounted;
        private String mTag;
        private WorkSource mWorkSource;
        final /* synthetic */ WifiManager this$0;

        /* synthetic */ WifiLock(WifiManager this$0, int lockType, String tag, WifiLock wifiLock) {
            this(this$0, lockType, tag);
        }

        private WifiLock(WifiManager this$0, int lockType, String tag) {
            this.this$0 = this$0;
            this.mTag = tag;
            this.mLockType = lockType;
            this.mBinder = new Binder();
            this.mRefCount = 0;
            this.mRefCounted = true;
            this.mHeld = false;
        }

        /* JADX WARNING: Missing block: B:6:0x000e, code:
            if (r1 == 1) goto L_0x0010;
     */
        /* JADX WARNING: Missing block: B:28:0x004c, code:
            if (r7.mHeld != false) goto L_0x004e;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void acquire() {
            synchronized (this.mBinder) {
                if (this.mRefCounted) {
                    int i = this.mRefCount + 1;
                    this.mRefCount = i;
                }
                try {
                    this.this$0.mService.acquireWifiLock(this.mBinder, this.mLockType, this.mTag, this.mWorkSource);
                    synchronized (this.this$0) {
                        if (this.this$0.mActiveLockCount >= 50) {
                            this.this$0.mService.releaseWifiLock(this.mBinder);
                            throw new UnsupportedOperationException("Exceeded maximum number of wifi locks");
                        }
                        WifiManager wifiManager = this.this$0;
                        wifiManager.mActiveLockCount = wifiManager.mActiveLockCount + 1;
                    }
                    this.mHeld = true;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }

        /* JADX WARNING: Missing block: B:6:0x000d, code:
            if (r1 == 0) goto L_0x000f;
     */
        /* JADX WARNING: Missing block: B:26:0x004f, code:
            if (r5.mHeld != false) goto L_0x000f;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void release() {
            synchronized (this.mBinder) {
                if (this.mRefCounted) {
                    int i = this.mRefCount - 1;
                    this.mRefCount = i;
                }
                try {
                    this.this$0.mService.releaseWifiLock(this.mBinder);
                    synchronized (this.this$0) {
                        WifiManager wifiManager = this.this$0;
                        wifiManager.mActiveLockCount = wifiManager.mActiveLockCount - 1;
                    }
                    this.mHeld = false;
                    if (this.mRefCount < 0) {
                        throw new RuntimeException("WifiLock under-locked " + this.mTag);
                    }
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }

        public void setReferenceCounted(boolean refCounted) {
            this.mRefCounted = refCounted;
        }

        public boolean isHeld() {
            boolean z;
            synchronized (this.mBinder) {
                z = this.mHeld;
            }
            return z;
        }

        public void setWorkSource(WorkSource ws) {
            synchronized (this.mBinder) {
                if (ws != null) {
                    if (ws.size() == 0) {
                        ws = null;
                    }
                }
                boolean changed = true;
                if (ws == null) {
                    this.mWorkSource = null;
                } else {
                    ws.clearNames();
                    if (this.mWorkSource == null) {
                        changed = this.mWorkSource != null;
                        this.mWorkSource = new WorkSource(ws);
                    } else {
                        changed = this.mWorkSource.diff(ws);
                        if (changed) {
                            this.mWorkSource.set(ws);
                        }
                    }
                }
                if (changed && this.mHeld) {
                    try {
                        this.this$0.mService.updateWifiLockWorkSource(this.mBinder, this.mWorkSource);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }

        public String toString() {
            String str;
            synchronized (this.mBinder) {
                String s3;
                String s1 = Integer.toHexString(System.identityHashCode(this));
                String s2 = this.mHeld ? "held; " : "";
                if (this.mRefCounted) {
                    s3 = "refcounted: refcount = " + this.mRefCount;
                } else {
                    s3 = "not refcounted";
                }
                str = "WifiLock{ " + s1 + "; " + s2 + s3 + " }";
            }
            return str;
        }

        protected void finalize() throws Throwable {
            super.finalize();
            synchronized (this.mBinder) {
                if (this.mHeld) {
                    try {
                        this.this$0.mService.releaseWifiLock(this.mBinder);
                        synchronized (this.this$0) {
                            WifiManager wifiManager = this.this$0;
                            wifiManager.mActiveLockCount = wifiManager.mActiveLockCount - 1;
                        }
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }
    }

    public interface WifiOffListener {
        void onWifiOff(int i);
    }

    public static abstract class WpsCallback {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.WifiManager.WpsCallback.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        public WpsCallback() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.WifiManager.WpsCallback.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiManager.WpsCallback.<init>():void");
        }

        public abstract void onFailed(int i);

        public abstract void onStarted(String str);

        public abstract void onSucceeded();
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.WifiManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.WifiManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.WifiManager.<clinit>():void");
    }

    public WifiManager(Context context, IWifiManager service, Looper looper) {
        this.mListenerKey = 1;
        this.mListenerMap = new SparseArray();
        this.mListenerMapLock = new Object();
        this.mWifiOffListenerList = new ArrayList();
        this.mContext = context;
        this.mService = service;
        this.mLooper = looper;
        this.mTargetSdkVersion = context.getApplicationInfo().targetSdkVersion;
    }

    public List<WifiConfiguration> getConfiguredNetworks() {
        try {
            return this.mService.getConfiguredNetworks();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<WifiConfiguration> getPrivilegedConfiguredNetworks() {
        try {
            return this.mService.getPrivilegedConfiguredNetworks();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public WifiConnectionStatistics getConnectionStatistics() {
        try {
            return this.mService.getConnectionStatistics();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public WifiConfiguration getMatchingWifiConfig(ScanResult scanResult) {
        try {
            return this.mService.getMatchingWifiConfig(scanResult);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int addNetwork(WifiConfiguration config) {
        if (config == null) {
            return -1;
        }
        config.networkId = -1;
        return addOrUpdateNetwork(config);
    }

    public int updateNetwork(WifiConfiguration config) {
        if (config == null || config.networkId < 0) {
            return -1;
        }
        return addOrUpdateNetwork(config);
    }

    private int addOrUpdateNetwork(WifiConfiguration config) {
        try {
            return this.mService.addOrUpdateNetwork(config);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int addPasspointManagementObject(String mo) {
        try {
            return this.mService.addPasspointManagementObject(mo);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int modifyPasspointManagementObject(String fqdn, List<PasspointManagementObjectDefinition> mos) {
        try {
            return this.mService.modifyPasspointManagementObject(fqdn, mos);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void queryPasspointIcon(long bssid, String fileName) {
        try {
            this.mService.queryPasspointIcon(bssid, fileName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int matchProviderWithCurrentNetwork(String fqdn) {
        try {
            return this.mService.matchProviderWithCurrentNetwork(fqdn);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void deauthenticateNetwork(long holdoff, boolean ess) {
        try {
            this.mService.deauthenticateNetwork(holdoff, ess);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean removeNetwork(int netId) {
        try {
            return this.mService.removeNetwork(netId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean enableNetwork(int netId, boolean disableOthers) {
        boolean pin = false;
        if (disableOthers && this.mTargetSdkVersion < 21) {
            pin = true;
        }
        if (pin) {
            NetworkPinner.pin(this.mContext, new Builder().clearCapabilities().addTransportType(1).build());
        }
        try {
            boolean success = this.mService.enableNetwork(netId, disableOthers);
            if (pin && !success) {
                NetworkPinner.unpin();
            }
            return success;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean disableNetwork(int netId) {
        try {
            return this.mService.disableNetwork(netId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean disconnect() {
        try {
            this.mService.disconnect();
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean reconnect() {
        try {
            this.mService.reconnect();
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean reassociate() {
        try {
            this.mService.reassociate();
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean pingSupplicant() {
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.pingSupplicant();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private int getSupportedFeatures() {
        try {
            return this.mService.getSupportedFeatures();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private boolean isFeatureSupported(int feature) {
        return (getSupportedFeatures() & feature) == feature;
    }

    public boolean is5GHzBandSupported() {
        return isFeatureSupported(2);
    }

    public boolean isPasspointSupported() {
        return isFeatureSupported(4);
    }

    public boolean isP2pSupported() {
        return isFeatureSupported(8);
    }

    public boolean isPortableHotspotSupported() {
        return isFeatureSupported(16);
    }

    public boolean isWifiScannerSupported() {
        return isFeatureSupported(32);
    }

    public boolean isNanSupported() {
        return isFeatureSupported(64);
    }

    public boolean isDeviceToDeviceRttSupported() {
        return isFeatureSupported(128);
    }

    public boolean isDeviceToApRttSupported() {
        return isFeatureSupported(256);
    }

    public boolean isPreferredNetworkOffloadSupported() {
        return isFeatureSupported(1024);
    }

    public boolean isAdditionalStaSupported() {
        return isFeatureSupported(2048);
    }

    public boolean isTdlsSupported() {
        return isFeatureSupported(4096);
    }

    public boolean isOffChannelTdlsSupported() {
        return isFeatureSupported(8192);
    }

    public boolean isEnhancedPowerReportingSupported() {
        return isFeatureSupported(65536);
    }

    public WifiActivityEnergyInfo getControllerActivityEnergyInfo(int updateType) {
        if (this.mService == null) {
            return null;
        }
        try {
            WifiActivityEnergyInfo reportActivityInfo;
            synchronized (this) {
                reportActivityInfo = this.mService.reportActivityInfo();
            }
            return reportActivityInfo;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean startScan() {
        try {
            this.mService.startScan(null, null);
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean startScan(WorkSource workSource) {
        try {
            this.mService.startScan(null, workSource);
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean startLocationRestrictedScan(WorkSource workSource) {
        return false;
    }

    @Deprecated
    public boolean isBatchedScanSupported() {
        return false;
    }

    @Deprecated
    public List<BatchedScanResult> getBatchedScanResults() {
        return null;
    }

    public String getWpsNfcConfigurationToken(int netId) {
        try {
            return this.mService.getWpsNfcConfigurationToken(netId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public WifiInfo getConnectionInfo() {
        try {
            return this.mService.getConnectionInfo();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ScanResult> getScanResults() {
        try {
            return this.mService.getScanResults(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isScanAlwaysAvailable() {
        try {
            return this.mService.isScanAlwaysAvailable();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean saveConfiguration() {
        try {
            return this.mService.saveConfiguration();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setCountryCode(String country, boolean persist) {
        try {
            this.mService.setCountryCode(country, persist);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getCountryCode() {
        try {
            return this.mService.getCountryCode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setFrequencyBand(int band, boolean persist) {
        try {
            this.mService.setFrequencyBand(band, persist);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getFrequencyBand() {
        try {
            return this.mService.getFrequencyBand();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isDualBandSupported() {
        try {
            return this.mService.isDualBandSupported();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public DhcpInfo getDhcpInfo() {
        try {
            return this.mService.getDhcpInfo();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setWifiEnabled(boolean enabled) {
        try {
            return this.mService.setWifiEnabled(enabled);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getWifiState() {
        try {
            return this.mService.getWifiEnabledState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isWifiEnabled() {
        return getWifiState() == 3;
    }

    public void getTxPacketCount(TxPacketCountListener listener) {
        getChannel().sendMessage(RSSI_PKTCNT_FETCH, 0, putListener(listener));
    }

    public static int calculateSignalLevel(int rssi, int numLevels) {
        if (numLevels == 5) {
            if (rssi <= OPPO_MIDDLE1_RSSI) {
                return 1;
            }
            if (rssi > OPPO_MIDDLE1_RSSI && rssi <= OPPO_MIDDLE2_RSSI) {
                return 2;
            }
            if (rssi <= OPPO_MIDDLE2_RSSI || rssi > OPPO_MAX_RSSI) {
                return 4;
            }
            return 3;
        } else if (rssi <= -100) {
            return 0;
        } else {
            if (rssi >= -55) {
                return numLevels - 1;
            }
            return (int) ((((float) (rssi + 100)) * ((float) (numLevels - 1))) / 45.0f);
        }
    }

    public static int compareSignalLevel(int rssiA, int rssiB) {
        return rssiA - rssiB;
    }

    public boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        try {
            this.mService.setWifiApEnabled(wifiConfig, enabled);
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getWifiApState() {
        try {
            return this.mService.getWifiApEnabledState();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isWifiApEnabled() {
        return getWifiApState() == 13;
    }

    public WifiConfiguration getWifiApConfiguration() {
        try {
            return this.mService.getWifiApConfiguration();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public WifiConfiguration buildWifiConfig(String uriString, String mimeType, byte[] data) {
        try {
            return this.mService.buildWifiConfig(uriString, mimeType, data);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
        try {
            this.mService.setWifiApConfiguration(wifiConfig);
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean addToBlacklist(String bssid) {
        try {
            this.mService.addToBlacklist(bssid);
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean clearBlacklist() {
        try {
            this.mService.clearBlacklist();
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setTdlsEnabled(InetAddress remoteIPAddress, boolean enable) {
        try {
            this.mService.enableTdls(remoteIPAddress.getHostAddress(), enable);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setTdlsEnabledWithMacAddress(String remoteMacAddress, boolean enable) {
        try {
            this.mService.enableTdlsWithMacAddress(remoteMacAddress, enable);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private int putListener(Object listener) {
        if (listener == null) {
            return 0;
        }
        int key;
        synchronized (this.mListenerMapLock) {
            do {
                key = this.mListenerKey;
                this.mListenerKey = key + 1;
            } while (key == 0);
            this.mListenerMap.put(key, listener);
        }
        return key;
    }

    private Object removeListener(int key) {
        if (key == 0) {
            return null;
        }
        Object listener;
        synchronized (this.mListenerMapLock) {
            listener = this.mListenerMap.get(key);
            this.mListenerMap.remove(key);
        }
        return listener;
    }

    private synchronized AsyncChannel getChannel() {
        if (this.mAsyncChannel == null) {
            Messenger messenger = getWifiServiceMessenger();
            if (messenger == null) {
                throw new IllegalStateException("getWifiServiceMessenger() returned null!  This is invalid.");
            }
            this.mAsyncChannel = new AsyncChannel();
            this.mConnected = new CountDownLatch(1);
            this.mAsyncChannel.connect(this.mContext, new ServiceHandler(this, this.mLooper), messenger);
            try {
                this.mConnected.await();
            } catch (InterruptedException e) {
                Log.e(TAG, "interrupted wait at init");
            }
        }
        return this.mAsyncChannel;
    }

    public void connect(WifiConfiguration config, ActionListener listener) {
        Log.d(TAG, "connect, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }
        getChannel().sendMessage(CONNECT_NETWORK, -1, putListener(listener), config);
    }

    public void connect(int networkId, ActionListener listener) {
        Log.d(TAG, "connect, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        if (networkId < 0) {
            throw new IllegalArgumentException("Network id cannot be negative");
        }
        getChannel().sendMessage(CONNECT_NETWORK, networkId, putListener(listener));
    }

    public void save(WifiConfiguration config, ActionListener listener) {
        Log.d(TAG, "save, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }
        getChannel().sendMessage(SAVE_NETWORK, 0, putListener(listener), config);
    }

    public void forget(int netId, ActionListener listener) {
        Log.d(TAG, "forget, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        if (netId < 0) {
            throw new IllegalArgumentException("Network id cannot be negative");
        }
        getChannel().sendMessage(FORGET_NETWORK, netId, putListener(listener));
    }

    public void disable(int netId, ActionListener listener) {
        Log.d(TAG, "disable, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        if (netId < 0) {
            throw new IllegalArgumentException("Network id cannot be negative");
        }
        getChannel().sendMessage(DISABLE_NETWORK, netId, putListener(listener));
    }

    public void disableEphemeralNetwork(String SSID) {
        if (SSID == null) {
            throw new IllegalArgumentException("SSID cannot be null");
        }
        try {
            this.mService.disableEphemeralNetwork(SSID);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startWps(WpsInfo config, WpsCallback listener) {
        Log.d(TAG, "startWps, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }
        getChannel().sendMessage(START_WPS, 0, putListener(listener), config);
    }

    public void cancelWps(WpsCallback listener) {
        Log.d(TAG, "cancelWps, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        getChannel().sendMessage(CANCEL_WPS, 0, putListener(listener));
    }

    public Messenger getWifiServiceMessenger() {
        try {
            return this.mService.getWifiServiceMessenger();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getConfigFile() {
        try {
            return this.mService.getConfigFile();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public WifiLock createWifiLock(int lockType, String tag) {
        return new WifiLock(this, lockType, tag, null);
    }

    public WifiLock createWifiLock(String tag) {
        return new WifiLock(this, 1, tag, null);
    }

    public MulticastLock createMulticastLock(String tag) {
        return new MulticastLock(this, tag, null);
    }

    public boolean isMulticastEnabled() {
        try {
            return this.mService.isMulticastEnabled();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean initializeMulticastFiltering() {
        try {
            this.mService.initializeMulticastFiltering();
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mAsyncChannel != null) {
                this.mAsyncChannel.disconnect();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public void enableVerboseLogging(int verbose) {
        try {
            this.mService.enableVerboseLogging(verbose);
        } catch (Exception e) {
            Log.e(TAG, "enableVerboseLogging " + e.toString());
        }
    }

    public int getVerboseLoggingLevel() {
        try {
            return this.mService.getVerboseLoggingLevel();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void enableAggressiveHandover(int enabled) {
        try {
            this.mService.enableAggressiveHandover(enabled);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getAggressiveHandover() {
        try {
            return this.mService.getAggressiveHandover();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setAllowScansWithTraffic(int enabled) {
        try {
            this.mService.setAllowScansWithTraffic(enabled);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getAllowScansWithTraffic() {
        try {
            return this.mService.getAllowScansWithTraffic();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void factoryReset() {
        try {
            this.mService.factoryReset();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Network getCurrentNetwork() {
        try {
            return this.mService.getCurrentNetwork();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setEnableAutoJoinWhenAssociated(boolean enabled) {
        try {
            return this.mService.setEnableAutoJoinWhenAssociated(enabled);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean getEnableAutoJoinWhenAssociated() {
        try {
            return this.mService.getEnableAutoJoinWhenAssociated();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        try {
            this.mService.enableWifiConnectivityManager(enabled);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<HotspotClient> getHotspotClients() {
        try {
            return this.mService.getHotspotClients();
        } catch (RemoteException e) {
            return null;
        }
    }

    public String getClientIp(String deviceAddress) {
        try {
            return this.mService.getClientIp(deviceAddress);
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean doCtiaTestOn() {
        try {
            return this.mService.doCtiaTestOn();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean doCtiaTestOff() {
        try {
            return this.mService.doCtiaTestOff();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean doCtiaTestRate(int rate) {
        try {
            return this.mService.doCtiaTestRate(rate);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean setTxPowerEnabled(boolean enabled) {
        try {
            return this.mService.setTxPowerEnabled(enabled);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean setTxPower(int offset) {
        try {
            return this.mService.setTxPower(offset);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean suspendNotification(int type) {
        try {
            this.mService.suspendNotification(type);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public String getWifiStatus() {
        if (this.mService == null) {
            Log.d(TAG, "getWifiStatus, fail, null == mService");
            return "";
        }
        try {
            return this.mService.getWifiStatus();
        } catch (RemoteException e) {
            return "";
        }
    }

    public void setPowerSavingMode(boolean mode) {
        if (this.mService == null) {
            Log.d(TAG, "setPowerSavingMode, fail, null == mService");
            return;
        }
        try {
            this.mService.setPowerSavingMode(mode);
        } catch (RemoteException e) {
        }
    }

    public void setTdlsPowerSave(boolean enable) {
        if (this.mService == null) {
            Log.d(TAG, "setTdlsPowerSave, fail, null == mService");
            return;
        }
        try {
            this.mService.setTdlsPowerSave(enable);
        } catch (RemoteException e) {
        }
    }

    public void startPPPOE(PPPOEConfig config) {
        Log.d(TAG, "DEBUG", new Throwable());
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }
        getChannel().sendMessage(START_PPPOE, 0, putListener(null), config);
    }

    public void stopPPPOE() {
        Log.d(TAG, "DEBUG", new Throwable());
        getChannel().sendMessage(STOP_PPPOE, 0, putListener(null));
    }

    public PPPOEInfo getPPPOEInfo() {
        Log.d(TAG, "DEBUG", new Throwable());
        try {
            return this.mService.getPPPOEInfo();
        } catch (RemoteException e) {
            return null;
        }
    }

    public void startWpsRegistrar(WpsInfo config, WpsCallback listener) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }
        getChannel().sendMessage(START_WPS_REG, 0, putListener(listener), config);
    }

    public void startWpsExternalRegistrar(WpsInfo config, WpsCallback listener) {
        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }
        getChannel().sendMessage(START_WPS_ER, 0, putListener(listener), config);
    }

    public void connectWithWpsPin(int tokenType, WpsCallback listener) {
        getChannel().sendMessage(GET_WPS_PIN_AND_CONNECT, tokenType, putListener(listener));
    }

    public void getWpsCredAndConnect(int tokenType, ActionListener listener) {
        getChannel().sendMessage(GET_WPS_CRED_AND_CONNECT, tokenType, putListener(listener));
    }

    public void writePinToNfc(int tokenType, ActionListener listener) {
        getChannel().sendMessage(WRITE_PIN_TO_NFC, tokenType, putListener(listener));
    }

    public void writeCredToNfc(int tokenType, ActionListener listener) {
        getChannel().sendMessage(WRITE_CRED_TO_NFC, tokenType, putListener(listener));
    }

    public void getPinFromNfc(int tokenType, ActionListener listener) {
        getChannel().sendMessage(GET_PIN_FROM_NFC, tokenType, putListener(listener));
    }

    public void getCredFromNfc(ActionListener listener) {
        getChannel().sendMessage(GET_CRED_FROM_NFC, 0, putListener(listener));
    }

    public boolean setWoWlanNormalMode() {
        if (this.mService == null) {
            Log.d(TAG, "setWoWlanNormalMode, fail, null == mService");
            return false;
        }
        try {
            return this.mService.setWoWlanNormalMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean setWoWlanMagicMode() {
        if (this.mService == null) {
            Log.d(TAG, "setWoWlanMagicMode, fail, null == mService");
            return false;
        }
        try {
            return this.mService.setWoWlanMagicMode();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean stopReconnectAndScan(boolean enable, int period) {
        stopReconnectAndScan(enable, period, false);
        return true;
    }

    public boolean stopReconnectAndScan(boolean enable, int period, boolean isAllowReconnect) {
        Log.d(TAG, "stopReconnectAndScan, " + enable + " period=" + period + " isAllowReconnect=" + isAllowReconnect);
        Log.d(TAG, "stopReconnectAndScan, pid:" + Process.myPid() + ", tid:" + Process.myTid() + ", uid:" + Process.myUid());
        if (enable && isAllowReconnect) {
            getChannel().sendMessage(SET_WIFI_NOT_RECONNECT_AND_SCAN, 1, period);
        } else if (!enable || isAllowReconnect) {
            getChannel().sendMessage(SET_WIFI_NOT_RECONNECT_AND_SCAN, 0, 0);
        } else {
            getChannel().sendMessage(SET_WIFI_NOT_RECONNECT_AND_SCAN, 2, period);
        }
        return true;
    }

    public boolean is5gBandSupported() {
        if (this.mService == null) {
            Log.d(TAG, "is5gBandSupported, fail, null == mService");
            return false;
        }
        try {
            return this.mService.is5gBandSupported();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean setHotspotOptimization(boolean enable) {
        if (this.mService == null) {
            Log.d(TAG, "setHotspotOptimization, fail, null == mService");
            return false;
        }
        try {
            return this.mService.setHotspotOptimization(enable);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isSuitableForTest(int channel, HashMap<Integer, Integer> result) {
        boolean testResult = false;
        if (this.mService == null) {
            Log.e(TAG, "isSuitableForTest fail, mService is null!");
            return false;
        }
        try {
            String env = this.mService.getTestEnv(channel);
            if (env == null) {
                return false;
            }
            String[] lines = env.split("\n");
            if (lines.length > 1) {
                String[] tmp = lines[1].split(":");
                if (tmp.length == 2 && tmp[1].equals(WifiEnterpriseConfig.ENGINE_ENABLE)) {
                    testResult = true;
                }
            }
            if (lines.length > 2 && result != null) {
                for (int i = 2; i < lines.length; i++) {
                    String[] nameValue = lines[i].split(",");
                    if (nameValue.length == 2) {
                        try {
                            result.put(Integer.valueOf(Integer.parseInt(nameValue[0].substring(6))), Integer.valueOf(Integer.parseInt(nameValue[1].substring(6))));
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "NumberFormatException, lines[" + i + "]:" + lines[i]);
                        }
                    }
                }
            }
            Log.d(TAG, "isSuitableForTest result:" + testResult);
            return testResult;
        } catch (RemoteException e2) {
            return false;
        }
    }

    public boolean setWifiDisabled(int flag) {
        Log.d(TAG, "setWifiDisabled, flag = " + flag);
        if (this.mService == null) {
            Log.d(TAG, "setWifiDisabled, fail, null == mService");
            return false;
        }
        try {
            return this.mService.setWifiDisabled(flag);
        } catch (RemoteException e) {
            return false;
        }
    }

    public void addWifiOffListener(WifiOffListener listener) {
        if (this.mWifiOffListenerList.size() == 0) {
            this.mWifiOffNotifyReceiver = new AnonymousClass1(this);
            this.mContext.registerReceiver(this.mWifiOffNotifyReceiver, new IntentFilter(WIFI_OFF_NOTIFY));
        }
        this.mWifiOffListenerList.add(listener);
        try {
            this.mService.registerWifiOffListener();
        } catch (RemoteException e) {
        }
    }

    public void removeWifiOffListener(WifiOffListener listener) {
        this.mWifiOffListenerList.remove(listener);
        if (this.mWifiOffListenerList.size() == 0) {
            try {
                this.mContext.unregisterReceiver(this.mWifiOffNotifyReceiver);
            } catch (IllegalArgumentException e) {
            }
        }
        try {
            this.mService.unregisterWifiOffListener();
        } catch (RemoteException e2) {
        }
    }

    public String getWifiPowerEventCode() {
        try {
            return this.mService.getWifiPowerEventCode();
        } catch (RemoteException e) {
            return null;
        }
    }
}
