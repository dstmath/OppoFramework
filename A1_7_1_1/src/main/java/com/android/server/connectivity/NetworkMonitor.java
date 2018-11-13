package com.android.server.connectivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.CaptivePortal;
import android.net.DhcpInfo;
import android.net.ICaptivePortal.Stub;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.ProxyInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.arp.ArpPeer;
import android.net.metrics.IpConnectivityLog;
import android.net.metrics.NetworkEvent;
import android.net.metrics.ValidationProbeEvent;
import android.net.util.Stopwatch;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.ColorOSTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.LocalLog.ReadOnlyLocalLog;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.LocationManagerService;
import com.android.server.WifiRomUpdateHelper;
import com.android.server.display.DisplayTransformManager;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import oppo.util.OppoStatistics;

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
public class NetworkMonitor extends StateMachine {
    public static final String ACTION_NETWORK_CONDITIONS_MEASURED = "android.net.conn.NETWORK_CONDITIONS_MEASURED";
    private static final String ACTION_WIFI_NETWORK_AVAILABLE = "android.net.wifi.OPPO_WIFI_VALID";
    private static final String ACTION_WIFI_NETWORK_CONNECT = "android.net.wifi.OPPO_WIFI_CONNECT";
    private static final String ACTION_WIFI_NETWORK_NOT_AVAILABLE = "android.net.wifi.OPPO_WIFI_INVALID";
    private static final String ACTION_WIFI_NETWORK_STATE = "android.net.wifi.OPPO_WIFI_NET_STATE";
    private static final int BASE = 532480;
    private static final int BLAME_FOR_EVALUATION_ATTEMPTS = 5;
    private static final int CAPTIVE_DELAY_MS = 10000;
    private static final int CAPTIVE_PORTAL_REEVALUATE_DELAY_MS = 600000;
    private static final int CMD_CAPTIVE_PORTAL_APP_FINISHED = 532489;
    private static final int CMD_CAPTIVE_PORTAL_RECHECK = 532492;
    public static final int CMD_FORCE_REEVALUATION = 532488;
    private static final int CMD_LAUNCH_CAPTIVE_PORTAL_APP = 532491;
    public static final int CMD_NETWORK_CONNECTED = 532481;
    public static final int CMD_NETWORK_DISCONNECTED = 532487;
    private static final int CMD_REECAPTIVE = 532580;
    private static final int CMD_REEVALUATE = 532486;
    private static final String CONNECT_MODE_CHANGE_ACTION = "android.net.wifi.CONNECT_MODE_CHANGE";
    private static final boolean DBG = true;
    private static final String DEFAULT_FALLBACK_URL = "http://www.google.com/gen_204";
    private static final String DEFAULT_HTTPS_URL = "https://www.google.com/generate_204";
    private static final String DEFAULT_HTTP_URL = "http://connectivitycheck.gstatic.com/generate_204";
    private static final String DEFAULT_SERVER = "connectivitycheck.gstatic.com";
    private static final String DEFAULT_SPECIAL_URL = "360.cn";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36";
    public static final int EVENT_NETWORK_TESTED = 532482;
    public static final int EVENT_PROVISIONING_NOTIFICATION = 532490;
    public static final String EXTRA_BSSID = "extra_bssid";
    public static final String EXTRA_CELL_ID = "extra_cellid";
    public static final String EXTRA_CONNECTIVITY_TYPE = "extra_connectivity_type";
    private static final String EXTRA_CONNECT_MODE = "connectMode";
    public static final String EXTRA_IS_CAPTIVE_PORTAL = "extra_is_captive_portal";
    private static final String EXTRA_NETWORK_STATE = "netState";
    public static final String EXTRA_NETWORK_TYPE = "extra_network_type";
    public static final String EXTRA_REQUEST_TIMESTAMP_MS = "extra_request_timestamp_ms";
    public static final String EXTRA_RESPONSE_RECEIVED = "extra_response_received";
    public static final String EXTRA_RESPONSE_TIMESTAMP_MS = "extra_response_timestamp_ms";
    public static final String EXTRA_SSID = "extra_ssid";
    private static final String EXTRA_WIFI_LINK = "linkProperties";
    private static final String EXTRA_WIFI_MANUAL = "manualConnect";
    private static final String EXTRA_WIFI_NETWORK = "network";
    private static final String EXTRA_WIFI_SSID = "ssid";
    private static final int IGNORE_REEVALUATE_ATTEMPTS = 5;
    private static final int INITIAL_REEVALUATE_DELAY_MS = 1000;
    private static final int INVALID_UID = -1;
    private static final String KEY_CAPTIVE = "key_captive";
    private static final String KEY_NETWORK = "key_network";
    private static final int MAX_REEVALUATE_DELAY_MS = 600000;
    public static final int NETWORK_TEST_RESULT_INVALID = 1;
    public static final int NETWORK_TEST_RESULT_VALID = 0;
    private static final String PERMISSION_ACCESS_NETWORK_CONDITIONS = "android.permission.ACCESS_NETWORK_CONDITIONS";
    private static final int PROBE_TIMEOUT_MS = 3000;
    private static final String SECONDARY_HTTP_URL = "http://captive.apple.com";
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final String TAG = null;
    private static final String WIFI_ASSISTANT = "wifi_assistant";
    private static final int WIFI_ATTEMPTS = 1;
    private static final String WIFI_AUTO_CHANGE_ACCESS_POINT = "wifi_auto_change_access_point";
    private static final String WIFI_STATISTIC_KEY = "wifi_fool_proof";
    private static final String captiveServer1 = "conn1.oppomobile.com";
    private static final String captiveServer2 = "conn2.oppomobile.com";
    private static boolean mSkipNetworkValidation;
    private boolean bManualConnect;
    private String[] capServer;
    private String[] capServerExp;
    private boolean haveSendmsg;
    private final AlarmManager mAlarmManager;
    private final int mAutoCaptiveMax;
    private final int mCaptiveMax;
    private final State mCaptivePortalState;
    private final Handler mConnectivityServiceHandler;
    private final Context mContext;
    private final NetworkRequest mDefaultRequest;
    private final State mDefaultState;
    private boolean mDontDisplaySigninNotification;
    private final State mEvaluatingState;
    private final Stopwatch mEvaluationTimer;
    private boolean mIsCaptivePortalCheckEnabled;
    private CaptivePortalProbeResult mLastPortalProbeResult;
    private CustomIntentReceiver mLaunchCaptivePortalAppBroadcastReceiver;
    private BroadcastReceiver mManualReceiver;
    private final int mManulCaptiveMax;
    private final State mMaybeNotifyState;
    private final IpConnectivityLog mMetricsLog;
    private final int mNetId;
    private final NetworkAgentInfo mNetworkAgentInfo;
    private String[] mPublicServers;
    private Random mRandom;
    private int mReecaptiveToken;
    private int mReevaluateToken;
    private final TelephonyManager mTelephonyManager;
    private int mUidResponsibleForReeval;
    private boolean mUseHttps;
    private boolean mUserDoesNotWant;
    private final State mValidatedState;
    private final WifiManager mWifiManager;
    private WifiRomUpdateHelper mWifiRomUpdateForNet;
    public boolean systemReady;
    private final LocalLog validationLogs;

    /* renamed from: com.android.server.connectivity.NetworkMonitor$1ProbeThread */
    final class AnonymousClass1ProbeThread extends Thread {
        private volatile CaptivePortalProbeResult mResult = CaptivePortalProbeResult.FAILED;
        private final URL mUrl;
        final /* synthetic */ CountDownLatch val$latch;

        public AnonymousClass1ProbeThread(URL url, CountDownLatch val$latch) {
            this.val$latch = val$latch;
            this.mUrl = url;
        }

        public CaptivePortalProbeResult result() {
            return this.mResult;
        }

        public void run() {
            this.mResult = NetworkMonitor.this.sendHttpProbe(this.mUrl, 1);
            if (this.mResult.isSuccessful()) {
                this.val$latch.countDown();
                this.val$latch.countDown();
            } else if (this.mResult.isPortal()) {
                this.val$latch.countDown();
                try {
                    AnonymousClass1ProbeThread.sleep(2000);
                } catch (InterruptedException e) {
                    NetworkMonitor.this.log("Probe sleep interrupted!");
                }
                NetworkMonitor.this.log("Probe sleep finished!");
            }
            this.val$latch.countDown();
        }
    }

    /* renamed from: com.android.server.connectivity.NetworkMonitor$2ProbeThread */
    final class AnonymousClass2ProbeThread extends Thread {
        private final boolean mIsHttps;
        private volatile CaptivePortalProbeResult mResult = CaptivePortalProbeResult.FAILED;
        final /* synthetic */ URL val$httpUrl;
        final /* synthetic */ URL val$httpsUrl;
        final /* synthetic */ CountDownLatch val$latch;

        public AnonymousClass2ProbeThread(boolean isHttps, URL val$httpsUrl, URL val$httpUrl, CountDownLatch val$latch) {
            this.val$httpsUrl = val$httpsUrl;
            this.val$httpUrl = val$httpUrl;
            this.val$latch = val$latch;
            this.mIsHttps = isHttps;
        }

        public CaptivePortalProbeResult result() {
            return this.mResult;
        }

        public void run() {
            if (this.mIsHttps) {
                this.mResult = NetworkMonitor.this.sendHttpProbe(this.val$httpsUrl, 2);
            } else {
                this.mResult = NetworkMonitor.this.sendHttpProbe(this.val$httpUrl, 1);
            }
            if ((this.mIsHttps && this.mResult.isSuccessful()) || (!this.mIsHttps && this.mResult.isPortal())) {
                while (this.val$latch.getCount() > 0) {
                    this.val$latch.countDown();
                }
            }
            this.val$latch.countDown();
        }
    }

    public static final class CaptivePortalProbeResult {
        static final CaptivePortalProbeResult FAILED = null;
        final String detectUrl;
        private final int mHttpResponseCode;
        final String redirectUrl;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.NetworkMonitor.CaptivePortalProbeResult.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.connectivity.NetworkMonitor.CaptivePortalProbeResult.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.NetworkMonitor.CaptivePortalProbeResult.<clinit>():void");
        }

        public CaptivePortalProbeResult(int httpResponseCode, String redirectUrl, String detectUrl) {
            this.mHttpResponseCode = httpResponseCode;
            this.redirectUrl = redirectUrl;
            this.detectUrl = detectUrl;
        }

        public CaptivePortalProbeResult(int httpResponseCode) {
            this(httpResponseCode, null, null);
        }

        boolean isSuccessful() {
            return this.mHttpResponseCode == 204;
        }

        boolean isPortal() {
            return !isSuccessful() && this.mHttpResponseCode >= DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE && this.mHttpResponseCode <= 399;
        }

        boolean isGetFromGate() {
            return this.mHttpResponseCode == VoldResponseCode.OpFailedVolNotMounted;
        }

        boolean isRouteNotReady() {
            return this.mHttpResponseCode == 444;
        }

        boolean isRedirection() {
            return this.mHttpResponseCode >= 300 && this.mHttpResponseCode <= 399;
        }
    }

    private class CaptivePortalState extends State {
        private static final String ACTION_LAUNCH_CAPTIVE_PORTAL_APP = "android.net.netmon.launchCaptivePortalApp";
        int mAtempCaptive;
        int mDetectCount;

        /* synthetic */ CaptivePortalState(NetworkMonitor this$0, CaptivePortalState captivePortalState) {
            this();
        }

        private CaptivePortalState() {
            this.mAtempCaptive = 1;
            this.mDetectCount = 0;
        }

        public void enter() {
            NetworkMonitor.this.maybeLogEvaluationResult(4);
            if (!NetworkMonitor.this.mDontDisplaySigninNotification) {
                String url;
                if (!TextUtils.isEmpty(NetworkMonitor.this.mLastPortalProbeResult.redirectUrl) && NetworkMonitor.this.mLastPortalProbeResult.isRedirection()) {
                    url = NetworkMonitor.this.mLastPortalProbeResult.redirectUrl;
                } else if (NetworkMonitor.this.isNotChineseOperator()) {
                    url = NetworkMonitor.DEFAULT_HTTP_URL;
                } else {
                    url = "http://www.baidu.com";
                }
                Intent intent = new Intent("com.oppo.browser.action.WIFI_LOGIN", Uri.parse(url));
                boolean bWizardEnd = Global.getInt(NetworkMonitor.this.mContext.getContentResolver(), "device_provisioned", 0) == 1;
                if (intent.resolveActivity(NetworkMonitor.this.mContext.getPackageManager()) == null || bWizardEnd) {
                    intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                }
                intent.setFlags(272629760);
                String targetPackage = "com.android.browser";
                String region = SystemProperties.get("ro.oppo.region.netlock", IElsaManager.EMPTY_PACKAGE);
                boolean isExp = NetworkMonitor.this.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
                boolean hasFeature = NetworkMonitor.this.mContext.getPackageManager().hasSystemFeature("oppo.exp.default.browser");
                if (isExp && hasFeature && !"IN".equals(region)) {
                    targetPackage = "com.android.chrome";
                }
                boolean hasTargetPackage = false;
                try {
                    hasTargetPackage = NetworkMonitor.this.mContext.getPackageManager().getPackageInfo(targetPackage, 0) != null;
                } catch (NameNotFoundException e) {
                    NetworkMonitor.this.log("targetPackage " + targetPackage + " not found");
                }
                if (hasTargetPackage) {
                    intent.setPackage(targetPackage);
                }
                WifiInfo wifiInfo = NetworkMonitor.this.mWifiManager.getConnectionInfo();
                intent.putExtra("ap_name", wifiInfo == null ? IElsaManager.EMPTY_PACKAGE : wifiInfo.getSSID());
                NetworkMonitor.this.log("CaptivePortalState visit with URL:" + url + " action=" + intent.getAction() + " SSID=" + (wifiInfo == null ? IElsaManager.EMPTY_PACKAGE : wifiInfo.getSSID()));
                if (NetworkMonitor.this.mNetworkAgentInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() != 1) {
                    NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, 1, NetworkMonitor.this.mNetworkAgentInfo.network.netId, PendingIntent.getActivity(NetworkMonitor.this.mContext, 0, intent, 0)));
                } else if (NetworkMonitor.this.bManualConnect) {
                    NetworkMonitor.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                } else {
                    if (!(NetworkMonitor.this.hasWlanAssistant() ? NetworkMonitor.this.isSwitchEnable() : false)) {
                        NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, 1, NetworkMonitor.this.mNetworkAgentInfo.network.netId, PendingIntent.getActivity(NetworkMonitor.this.mContext, 0, intent, 0)));
                    }
                }
                if (NetworkMonitor.this.bManualConnect) {
                    this.mDetectCount = NetworkMonitor.this.getRomUpdateIntegerValue(WifiRomUpdateHelper.OPPO_WIFI_ASSISTANT_PORTAL_MANUL_DETECT_COUNT, Integer.valueOf(12)).intValue();
                } else {
                    this.mDetectCount = NetworkMonitor.this.getRomUpdateIntegerValue(WifiRomUpdateHelper.OPPO_WIFI_ASSISTANT_PORTAL_AUTO_DETECT_COUNT, Integer.valueOf(3)).intValue();
                }
                NetworkMonitor networkMonitor = NetworkMonitor.this;
                NetworkMonitor networkMonitor2 = NetworkMonitor.this;
                networkMonitor.sendMessageDelayed(NetworkMonitor.CMD_REECAPTIVE, networkMonitor2.mReecaptiveToken = networkMonitor2.mReecaptiveToken + 1, 10000);
            }
        }

        public boolean processMessage(Message message) {
            NetworkMonitor.this.log(getName() + message.toString());
            switch (message.what) {
                case NetworkMonitor.CMD_REECAPTIVE /*532580*/:
                    if (message.arg1 != NetworkMonitor.this.mReecaptiveToken) {
                        return true;
                    }
                    CaptivePortalProbeResult probeResult = NetworkMonitor.this.isCaptivePortal();
                    if (probeResult.isGetFromGate()) {
                        NetworkMonitor.this.sendNetworkAvailable(false, probeResult);
                        return true;
                    } else if (probeResult.isSuccessful()) {
                        NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 0, NetworkMonitor.this.mNetworkAgentInfo.network.netId, null));
                        NetworkMonitor.this.sendNetworkAvailable(true);
                        return true;
                    } else {
                        int i = this.mAtempCaptive + 1;
                        this.mAtempCaptive = i;
                        if (i > this.mDetectCount) {
                            NetworkMonitor.this.sendNetworkAvailable(false, probeResult);
                            return true;
                        }
                        NetworkMonitor networkMonitor = NetworkMonitor.this;
                        NetworkMonitor networkMonitor2 = NetworkMonitor.this;
                        NetworkMonitor.this.sendMessageDelayed(networkMonitor.obtainMessage(NetworkMonitor.CMD_REECAPTIVE, networkMonitor2.mReecaptiveToken = networkMonitor2.mReecaptiveToken + 1, 0), 10000);
                        return true;
                    }
                default:
                    return false;
            }
        }

        public void exit() {
            NetworkMonitor.this.removeMessages(NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK);
        }
    }

    private class CustomIntentReceiver extends BroadcastReceiver {
        private final String mAction;
        private final int mToken;
        private final int mWhat;

        CustomIntentReceiver(String action, int token, int what) {
            this.mToken = token;
            this.mWhat = what;
            this.mAction = action + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + NetworkMonitor.this.mNetworkAgentInfo.network.netId + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + token;
            NetworkMonitor.this.mContext.registerReceiver(this, new IntentFilter(this.mAction));
        }

        public PendingIntent getPendingIntent() {
            Intent intent = new Intent(this.mAction);
            intent.setPackage(NetworkMonitor.this.mContext.getPackageName());
            return PendingIntent.getBroadcast(NetworkMonitor.this.mContext, 0, intent, 0);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(this.mAction)) {
                NetworkMonitor.this.sendMessage(NetworkMonitor.this.obtainMessage(this.mWhat, this.mToken));
            }
        }
    }

    private class DefaultState extends State {
        /* synthetic */ DefaultState(NetworkMonitor this$0, DefaultState defaultState) {
            this();
        }

        private DefaultState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_NETWORK_CONNECTED /*532481*/:
                    NetworkMonitor.this.logNetworkEvent(1);
                    NetworkMonitor.this.sendNetworkAgentInfo();
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return true;
                case NetworkMonitor.CMD_NETWORK_DISCONNECTED /*532487*/:
                    NetworkMonitor.this.logNetworkEvent(7);
                    if (NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver != null) {
                        NetworkMonitor.this.mContext.unregisterReceiver(NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver);
                        NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver = null;
                    }
                    if (NetworkMonitor.this.mNetworkAgentInfo.networkInfo != null && NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() == 1) {
                        try {
                            NetworkMonitor.this.mContext.unregisterReceiver(NetworkMonitor.this.mManualReceiver);
                        } catch (IllegalArgumentException e) {
                        }
                    }
                    NetworkMonitor.this.quit();
                    return true;
                case NetworkMonitor.CMD_FORCE_REEVALUATION /*532488*/:
                case NetworkMonitor.CMD_CAPTIVE_PORTAL_RECHECK /*532492*/:
                    NetworkMonitor.this.log("Forcing reevaluation for UID " + message.arg1);
                    NetworkMonitor.this.mUidResponsibleForReeval = message.arg1;
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return true;
                case NetworkMonitor.CMD_CAPTIVE_PORTAL_APP_FINISHED /*532489*/:
                    NetworkMonitor.this.log("CaptivePortal App responded with " + message.arg1);
                    NetworkMonitor.this.mUseHttps = false;
                    switch (message.arg1) {
                        case 0:
                            NetworkMonitor.this.sendMessage(NetworkMonitor.CMD_FORCE_REEVALUATION, 0, 0);
                            break;
                        case 1:
                            NetworkMonitor.this.mDontDisplaySigninNotification = true;
                            NetworkMonitor.this.mUserDoesNotWant = true;
                            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, null));
                            NetworkMonitor.this.mUidResponsibleForReeval = 0;
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                            break;
                        case 2:
                            NetworkMonitor.this.mDontDisplaySigninNotification = true;
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                            break;
                    }
                    return true;
                default:
                    return true;
            }
        }
    }

    private class EvaluatingState extends State {
        private int mAttempts;
        private int mReevaluateDelayMs;

        /* synthetic */ EvaluatingState(NetworkMonitor this$0, EvaluatingState evaluatingState) {
            this();
        }

        private EvaluatingState() {
        }

        public void enter() {
            if (!NetworkMonitor.this.mEvaluationTimer.isStarted()) {
                NetworkMonitor.this.mEvaluationTimer.start();
            }
            NetworkMonitor networkMonitor = NetworkMonitor.this;
            NetworkMonitor networkMonitor2 = NetworkMonitor.this;
            networkMonitor.sendMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0);
            if (NetworkMonitor.this.mUidResponsibleForReeval != -1) {
                TrafficStats.setThreadStatsUid(NetworkMonitor.this.mUidResponsibleForReeval);
                NetworkMonitor.this.mUidResponsibleForReeval = -1;
            }
            this.mReevaluateDelayMs = 1000;
            this.mAttempts = 0;
            NetworkMonitor.this.haveSendmsg = false;
        }

        public boolean processMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case NetworkMonitor.CMD_REEVALUATE /*532486*/:
                    if (message.arg1 != NetworkMonitor.this.mReevaluateToken) {
                        return true;
                    }
                    if (NetworkMonitor.this.mDefaultRequest.networkCapabilities.satisfiedByNetworkCapabilities(NetworkMonitor.this.mNetworkAgentInfo.networkCapabilities)) {
                        if (SystemProperties.getInt("gsm.sim.ril.testsim", 0) == 1 || SystemProperties.getInt("gsm.sim.ril.testsim.2", 0) == 1) {
                            NetworkMonitor.this.log("test sim enabled");
                            NetworkMonitor.mSkipNetworkValidation = true;
                        }
                        if (NetworkMonitor.this.mNetworkAgentInfo.networkCapabilities.hasTransport(0) && NetworkMonitor.mSkipNetworkValidation) {
                            NetworkMonitor.this.log("consider Mobile validated directly");
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                            return true;
                        }
                        this.mAttempts++;
                        CaptivePortalProbeResult probeResult = NetworkMonitor.this.isCaptivePortal();
                        if (!(NetworkMonitor.this.mNetworkAgentInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() != 1)) {
                            Network network = NetworkMonitor.this.mWifiManager.getCurrentNetwork();
                            if (network == null || !network.equals(NetworkMonitor.this.mNetworkAgentInfo.network)) {
                                NetworkMonitor.this.log("networkmonitor net id has changed");
                                return true;
                            }
                        }
                        NetworkMonitor networkMonitor;
                        NetworkMonitor networkMonitor2;
                        if (probeResult.isSuccessful()) {
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                        } else if (probeResult.isPortal()) {
                            if (NetworkMonitor.this.isDupGateway()) {
                                return true;
                            }
                            if (NetworkMonitor.this.bManualConnect || this.mAttempts > 1) {
                                NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, probeResult.redirectUrl));
                                NetworkMonitor.this.mLastPortalProbeResult = probeResult;
                                NetworkMonitor.this.transitionTo(NetworkMonitor.this.mCaptivePortalState);
                            } else {
                                networkMonitor = NetworkMonitor.this;
                                networkMonitor2 = NetworkMonitor.this;
                                NetworkMonitor.this.sendMessageDelayed(networkMonitor.obtainMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0), (long) this.mReevaluateDelayMs);
                            }
                        } else if (probeResult.isRouteNotReady() && this.mAttempts <= 3) {
                            networkMonitor = NetworkMonitor.this;
                            networkMonitor2 = NetworkMonitor.this;
                            NetworkMonitor.this.sendMessageDelayed(networkMonitor.obtainMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0), (long) this.mReevaluateDelayMs);
                        } else if (NetworkMonitor.this.isDupGateway()) {
                            return true;
                        } else {
                            if (!(NetworkMonitor.this.mNetworkAgentInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() != 1)) {
                                if (NetworkMonitor.this.hasWlanAssistant() && !NetworkMonitor.this.haveSendmsg) {
                                    if (NetworkMonitor.this.bManualConnect || this.mAttempts > 3) {
                                        NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, probeResult.redirectUrl));
                                        NetworkMonitor.this.sendNetworkAvailable(false, probeResult);
                                        NetworkMonitor.this.haveSendmsg = true;
                                    } else {
                                        networkMonitor = NetworkMonitor.this;
                                        networkMonitor2 = NetworkMonitor.this;
                                        NetworkMonitor.this.sendMessageDelayed(networkMonitor.obtainMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0), (long) this.mReevaluateDelayMs);
                                        this.mReevaluateDelayMs *= 2;
                                    }
                                    return true;
                                } else if (!NetworkMonitor.this.hasWlanAssistant()) {
                                    NetworkMonitor.this.sendNetworkAvailable(false);
                                    return true;
                                }
                            }
                            networkMonitor = NetworkMonitor.this;
                            networkMonitor2 = NetworkMonitor.this;
                            NetworkMonitor.this.sendMessageDelayed(networkMonitor.obtainMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0), (long) this.mReevaluateDelayMs);
                            NetworkMonitor.this.logNetworkEvent(3);
                            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, probeResult.redirectUrl));
                            if (this.mAttempts >= 5) {
                                TrafficStats.clearThreadStatsUid();
                            }
                            this.mReevaluateDelayMs *= 2;
                            if (this.mReevaluateDelayMs > 600000) {
                                this.mReevaluateDelayMs = 600000;
                            }
                        }
                        return true;
                    }
                    NetworkMonitor.this.validationLog("Network would not satisfy default request, not validating");
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                    return true;
                case NetworkMonitor.CMD_FORCE_REEVALUATION /*532488*/:
                    if (this.mAttempts >= 5) {
                        z = false;
                    }
                    return z;
                default:
                    return false;
            }
        }

        public void exit() {
            TrafficStats.clearThreadStatsUid();
        }
    }

    private class MaybeNotifyState extends State {
        /* synthetic */ MaybeNotifyState(NetworkMonitor this$0, MaybeNotifyState maybeNotifyState) {
            this();
        }

        private MaybeNotifyState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_LAUNCH_CAPTIVE_PORTAL_APP /*532491*/:
                    Intent intent = new Intent("android.net.conn.CAPTIVE_PORTAL");
                    intent.putExtra("android.net.extra.NETWORK", NetworkMonitor.this.mNetworkAgentInfo.network);
                    intent.putExtra("android.net.extra.CAPTIVE_PORTAL", new CaptivePortal(new Stub() {
                        public void appResponse(int response) {
                            if (response == 2) {
                                NetworkMonitor.this.mContext.enforceCallingPermission("android.permission.CONNECTIVITY_INTERNAL", "CaptivePortal");
                            }
                            NetworkMonitor.this.sendMessage(NetworkMonitor.CMD_CAPTIVE_PORTAL_APP_FINISHED, response);
                        }
                    }));
                    intent.putExtra("android.net.extra.CAPTIVE_PORTAL_URL", NetworkMonitor.this.mLastPortalProbeResult.detectUrl);
                    intent.setFlags(272629760);
                    NetworkMonitor.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, 0, NetworkMonitor.this.mNetworkAgentInfo.network.netId, null));
        }
    }

    private class ValidatedState extends State {
        /* synthetic */ ValidatedState(NetworkMonitor this$0, ValidatedState validatedState) {
            this();
        }

        private ValidatedState() {
        }

        public void enter() {
            NetworkMonitor.this.maybeLogEvaluationResult(2);
            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 0, NetworkMonitor.this.mNetworkAgentInfo.network.netId, null));
            NetworkMonitor.this.sendNetworkAvailable(true);
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_NETWORK_CONNECTED /*532481*/:
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                    return true;
                default:
                    return false;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.NetworkMonitor.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.connectivity.NetworkMonitor.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.NetworkMonitor.<clinit>():void");
    }

    public NetworkMonitor(Context context, Handler handler, NetworkAgentInfo networkAgentInfo, NetworkRequest defaultRequest) {
        this(context, handler, networkAgentInfo, defaultRequest, new IpConnectivityLog());
    }

    protected NetworkMonitor(Context context, Handler handler, NetworkAgentInfo networkAgentInfo, NetworkRequest defaultRequest, IpConnectivityLog logger) {
        boolean z = false;
        super(TAG + networkAgentInfo.name());
        this.mReevaluateToken = 0;
        this.mUidResponsibleForReeval = -1;
        this.mUserDoesNotWant = false;
        this.mDontDisplaySigninNotification = false;
        this.systemReady = false;
        this.mDefaultState = new DefaultState(this, null);
        this.mValidatedState = new ValidatedState(this, null);
        this.mMaybeNotifyState = new MaybeNotifyState(this, null);
        this.mEvaluatingState = new EvaluatingState(this, null);
        this.mCaptivePortalState = new CaptivePortalState(this, null);
        this.mLaunchCaptivePortalAppBroadcastReceiver = null;
        this.validationLogs = new LocalLog(20);
        this.mEvaluationTimer = new Stopwatch();
        this.mLastPortalProbeResult = CaptivePortalProbeResult.FAILED;
        this.mReecaptiveToken = 0;
        this.mCaptiveMax = 3;
        this.mAutoCaptiveMax = 3;
        this.mManulCaptiveMax = 12;
        String[] strArr = new String[2];
        strArr[0] = captiveServer1;
        strArr[1] = captiveServer2;
        this.capServer = strArr;
        strArr = new String[4];
        strArr[0] = "https://m.baidu.com";
        strArr[1] = "http://info.3g.qq.com";
        strArr[2] = "https://sina.cn";
        strArr[3] = "https://m.sohu.com";
        this.mPublicServers = strArr;
        this.mRandom = new Random(Calendar.getInstance().getTimeInMillis());
        this.haveSendmsg = false;
        this.mWifiRomUpdateForNet = null;
        strArr = new String[2];
        strArr[0] = DEFAULT_SERVER;
        strArr[1] = captiveServer1;
        this.capServerExp = strArr;
        this.bManualConnect = false;
        this.mManualReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkMonitor.this.bManualConnect = intent.getBooleanExtra(NetworkMonitor.EXTRA_CONNECT_MODE, false);
            }
        };
        this.mContext = context;
        this.mMetricsLog = logger;
        this.mConnectivityServiceHandler = handler;
        this.mNetworkAgentInfo = networkAgentInfo;
        this.mNetId = this.mNetworkAgentInfo.network.netId;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mDefaultRequest = defaultRequest;
        addState(this.mDefaultState);
        addState(this.mValidatedState, this.mDefaultState);
        addState(this.mMaybeNotifyState, this.mDefaultState);
        addState(this.mEvaluatingState, this.mMaybeNotifyState);
        addState(this.mCaptivePortalState, this.mMaybeNotifyState);
        setInitialState(this.mDefaultState);
        this.mIsCaptivePortalCheckEnabled = Global.getInt(this.mContext.getContentResolver(), "captive_portal_detection_enabled", 1) == 1;
        mSkipNetworkValidation = this.mContext.getResources().getBoolean(135004173);
        if (Global.getInt(this.mContext.getContentResolver(), "captive_portal_use_https", 0) == 1) {
            z = true;
        }
        this.mUseHttps = z;
        this.mWifiRomUpdateForNet = new WifiRomUpdateHelper(this.mContext);
        if (this.mNetworkAgentInfo.networkInfo != null && this.mNetworkAgentInfo.networkInfo.getType() == 1) {
            this.mContext.registerReceiverAsUser(this.mManualReceiver, UserHandle.ALL, new IntentFilter(CONNECT_MODE_CHANGE_ACTION), null, null);
        }
        start();
    }

    protected void log(String s) {
        Log.d(TAG + "/" + this.mNetworkAgentInfo.name(), s);
    }

    private void validationLog(String s) {
        log(s);
        this.validationLogs.log(s);
    }

    public ReadOnlyLocalLog getValidationLogs() {
        return this.validationLogs.readOnlyLocalLog();
    }

    private static String getCaptivePortalServerHttpsUrl(Context context) {
        return getSetting(context, "captive_portal_https_url", DEFAULT_HTTPS_URL);
    }

    public static String getCaptivePortalServerHttpUrl(Context context) {
        return getSetting(context, "captive_portal_http_url", SECONDARY_HTTP_URL);
    }

    private static String getCaptivePortalFallbackUrl(Context context) {
        return getSetting(context, "captive_portal_fallback_url", DEFAULT_FALLBACK_URL);
    }

    private static String getCaptivePortalUserAgent(Context context) {
        return getSetting(context, "captive_portal_user_agent", DEFAULT_USER_AGENT);
    }

    private static String getSetting(Context context, String symbol, String defaultValue) {
        String value = Global.getString(context.getContentResolver(), symbol);
        return value != null ? value : defaultValue;
    }

    protected CaptivePortalProbeResult isCaptivePortal() {
        if (!this.mIsCaptivePortalCheckEnabled) {
            return new CaptivePortalProbeResult(204);
        }
        CaptivePortalProbeResult result;
        log("isCaptivePortal");
        URL pacUrl = null;
        URL httpsUrl = null;
        URL httpUrl = null;
        URL fallbackUrl = null;
        ProxyInfo proxyInfo = this.mNetworkAgentInfo.linkProperties.getHttpProxy();
        if (!(proxyInfo == null || Uri.EMPTY.equals(proxyInfo.getPacFileUrl()))) {
            pacUrl = makeURL(proxyInfo.getPacFileUrl().toString());
            if (pacUrl == null) {
                return CaptivePortalProbeResult.FAILED;
            }
        }
        if (pacUrl == null) {
            if (this.mNetworkAgentInfo == null || this.mNetworkAgentInfo.networkInfo == null || this.mNetworkAgentInfo.networkInfo.getType() != 1) {
                httpsUrl = makeURL(getCaptivePortalServerHttpsUrl(this.mContext));
                httpUrl = makeURL(getCaptivePortalServerHttpUrl(this.mContext));
                fallbackUrl = makeURL(getCaptivePortalFallbackUrl(this.mContext));
            } else {
                List<String> publicServers = getPublicServers();
                int index = this.mRandom.nextInt(publicServers.size());
                Collections.shuffle(publicServers);
                httpsUrl = makeURL((String) publicServers.get(0));
                fallbackUrl = makeURL((String) publicServers.get(1));
                httpUrl = makeURL("http://" + this.capServer[index % 2] + "/generate_204");
            }
            if (httpUrl == null || httpsUrl == null) {
                return CaptivePortalProbeResult.FAILED;
            }
        }
        long startTime = SystemClock.elapsedRealtime();
        if (pacUrl != null) {
            result = sendHttpProbe(pacUrl, 3);
        } else if (this.mUseHttps) {
            result = sendParallelHttpProbes(httpsUrl, httpUrl, fallbackUrl);
        } else if (isNotChineseOperator()) {
            result = sendOPPOParallelHttpProbes(httpsUrl, httpUrl, makeURL(DEFAULT_HTTP_URL));
        } else {
            result = sendOPPOParallelHttpProbes(httpsUrl, httpUrl, fallbackUrl);
        }
        sendNetworkConditionsBroadcast(true, result.isPortal(), startTime, SystemClock.elapsedRealtime());
        return result;
    }

    protected CaptivePortalProbeResult sendHttpProbe(URL url, int probeType) {
        HttpURLConnection httpURLConnection = null;
        int httpResponseCode = 599;
        String redirectUrl = null;
        Stopwatch probeTimer = new Stopwatch().start();
        Stopwatch dnsTimer = new Stopwatch().start();
        try {
            String hostToResolve = url.getHost();
            if (!TextUtils.isEmpty(hostToResolve)) {
                InetAddress[] addresses = this.mNetworkAgentInfo.network.getAllByName(hostToResolve);
                DhcpInfo di = this.mWifiManager.getDhcpInfo();
                StringBuffer sb = new StringBuffer();
                sb.append(di.gateway & 255).append(".");
                sb.append((di.gateway >> 8) & 255).append(".");
                sb.append((di.gateway >> 16) & 255).append(".");
                sb.append((di.gateway >> 24) & 255);
                Log.d(TAG, "gateway:" + sb.toString());
                if (addresses.length == 1 && sb.toString().equals(addresses[0].getHostAddress())) {
                    Log.d(TAG, "got response from gateway but not from server:" + url.toString());
                    dnsTimer.stop();
                    logValidationProbe(probeTimer.stop(), probeType, VoldResponseCode.OpFailedVolNotMounted);
                    return new CaptivePortalProbeResult(VoldResponseCode.OpFailedVolNotMounted, null, url.toString());
                }
            }
            httpURLConnection = (HttpURLConnection) this.mNetworkAgentInfo.network.openConnection(url);
            httpURLConnection.setInstanceFollowRedirects(probeType == 3);
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setUseCaches(false);
            long requestTimestamp = SystemClock.elapsedRealtime();
            httpResponseCode = httpURLConnection.getResponseCode();
            redirectUrl = httpURLConnection.getHeaderField("location");
            validationLog(ValidationProbeEvent.getProbeName(probeType) + " " + url + " time=" + (SystemClock.elapsedRealtime() - requestTimestamp) + "ms" + " ret=" + httpResponseCode + " headers=" + httpURLConnection.getHeaderFields());
            if (httpResponseCode == 200 && httpURLConnection.getContentLength() == 0) {
                validationLog("Empty 200 response interpreted as 204 response.");
                httpResponseCode = 204;
            }
            if (httpResponseCode == 200 && probeType == 3) {
                validationLog("PAC fetch 200 response interpreted as 204 response.");
                httpResponseCode = 204;
            }
            String contentType = httpURLConnection.getContentType();
            if (contentType == null) {
                log("contentType is null, httpResponseCode = " + httpResponseCode);
            } else if (contentType.contains("text/html")) {
                String line = new BufferedReader(new InputStreamReader((InputStream) httpURLConnection.getContent())).readLine();
                if (line != null) {
                    validationLog("urlConnection.getContent() = " + line);
                    if (httpResponseCode == 200 && line.contains("Success")) {
                        httpResponseCode = 204;
                        log("Internet detected!");
                    }
                } else {
                    log("line is null, httpResponseCode = " + httpResponseCode);
                }
            }
            if (httpResponseCode == 200 && httpURLConnection.getHeaderField("Connection") != null && httpURLConnection.getHeaderField("Connection").equalsIgnoreCase("Keep-Alive")) {
                Log.d(TAG, "response 200 Connection - Alive.");
                httpResponseCode = 204;
            }
            if (httpResponseCode >= 300 && httpResponseCode <= 399 && redirectUrl != null && inSpecialUrlList(redirectUrl)) {
                Log.d(TAG, "response 302 with special redirect url.");
                httpResponseCode = 204;
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (UnknownHostException e) {
            if (dnsTimer.stop() < 300) {
                httpResponseCode = 444;
                log("DNS UnknownHostException immediately, Probably route not ready!");
            }
            validationLog("Probably not a portal: exception " + e);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (NoRouteToHostException e2) {
            if (dnsTimer.stop() < 300) {
                httpResponseCode = 444;
                log("NoRouteToHostException, Probably route not ready!");
            }
            validationLog("Probably not a portal: exception " + e2);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (IOException e3) {
            validationLog("Probably not a portal: exception " + e3);
            if (httpResponseCode == 599) {
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (IllegalArgumentException e4) {
            log("this is not right: exception " + e4);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        dnsTimer.stop();
        logValidationProbe(probeTimer.stop(), probeType, httpResponseCode);
        return new CaptivePortalProbeResult(httpResponseCode, redirectUrl, url.toString());
    }

    private CaptivePortalProbeResult sendOPPOParallelHttpProbes(URL httpsUrl, URL httpUrl, URL fallbackUrl) {
        CountDownLatch latch = new CountDownLatch(3);
        log("ProbeThread httpUrl= " + httpUrl + ", httpsUrl= " + httpsUrl + ", fallbackUrl= " + fallbackUrl);
        AnonymousClass1ProbeThread httpProbe1 = new AnonymousClass1ProbeThread(httpUrl, latch);
        AnonymousClass1ProbeThread httpProbe2 = new AnonymousClass1ProbeThread(httpsUrl, latch);
        AnonymousClass1ProbeThread httpProbe3 = new AnonymousClass1ProbeThread(fallbackUrl, latch);
        try {
            httpProbe1.start();
            httpProbe2.start();
            httpProbe3.start();
            latch.await(30000, TimeUnit.MILLISECONDS);
            CaptivePortalProbeResult httpProbe1Result = httpProbe1.result();
            CaptivePortalProbeResult httpProbe2Result = httpProbe2.result();
            CaptivePortalProbeResult httpProbe3Result = httpProbe3.result();
            log("httpProbe1Result= " + httpProbe1Result.mHttpResponseCode + ", httpProbe2Result= " + httpProbe2Result.mHttpResponseCode + ", httpProbe3Result= " + httpProbe3Result.mHttpResponseCode);
            if (httpProbe1Result.isSuccessful()) {
                return httpProbe1Result;
            }
            if (httpProbe2Result.isSuccessful()) {
                return httpProbe2Result;
            }
            if (httpProbe3Result.isSuccessful()) {
                return httpProbe3Result;
            }
            if (httpProbe1Result.isPortal()) {
                return httpProbe1Result;
            }
            if (httpProbe2Result.isPortal()) {
                return httpProbe2Result;
            }
            if (httpProbe3Result.isPortal()) {
                return httpProbe3Result;
            }
            return httpProbe1Result;
        } catch (InterruptedException e) {
            log("Error: probe wait interrupted!");
            return CaptivePortalProbeResult.FAILED;
        }
    }

    private CaptivePortalProbeResult sendParallelHttpProbes(URL httpsUrl, URL httpUrl, URL fallbackUrl) {
        CountDownLatch latch = new CountDownLatch(2);
        AnonymousClass2ProbeThread httpsProbe = new AnonymousClass2ProbeThread(true, httpsUrl, httpUrl, latch);
        AnonymousClass2ProbeThread httpProbe = new AnonymousClass2ProbeThread(false, httpsUrl, httpUrl, latch);
        try {
            httpsProbe.start();
            httpProbe.start();
            latch.await(3000, TimeUnit.MILLISECONDS);
            CaptivePortalProbeResult httpsResult = httpsProbe.result();
            CaptivePortalProbeResult httpResult = httpProbe.result();
            if (httpResult.isPortal()) {
                return httpResult;
            }
            if (httpsResult.isPortal() || httpsResult.isSuccessful()) {
                return httpsResult;
            }
            if (fallbackUrl != null) {
                CaptivePortalProbeResult result = sendHttpProbe(fallbackUrl, 4);
                if (result.isPortal()) {
                    return result;
                }
            }
            try {
                httpsProbe.join();
                return httpsProbe.result();
            } catch (InterruptedException e) {
                validationLog("Error: https probe wait interrupted!");
                return CaptivePortalProbeResult.FAILED;
            }
        } catch (InterruptedException e2) {
            validationLog("Error: probes wait interrupted!");
            return CaptivePortalProbeResult.FAILED;
        }
    }

    private URL makeURL(String url) {
        if (url != null) {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                validationLog("Bad URL: " + url);
            }
        }
        return null;
    }

    private void sendNetworkConditionsBroadcast(boolean responseReceived, boolean isCaptivePortal, long requestTimestampMs, long responseTimestampMs) {
        if (this.systemReady) {
            Intent latencyBroadcast = new Intent(ACTION_NETWORK_CONDITIONS_MEASURED);
            switch (this.mNetworkAgentInfo.networkInfo.getType()) {
                case 0:
                    latencyBroadcast.putExtra(EXTRA_NETWORK_TYPE, this.mTelephonyManager.getNetworkType());
                    List<CellInfo> info = this.mTelephonyManager.getAllCellInfo();
                    if (info != null) {
                        int numRegisteredCellInfo = 0;
                        for (CellInfo cellInfo : info) {
                            if (cellInfo.isRegistered()) {
                                numRegisteredCellInfo++;
                                if (numRegisteredCellInfo > 1) {
                                    log("more than one registered CellInfo.  Can't tell which is active.  Bailing.");
                                    return;
                                } else if (cellInfo instanceof CellInfoCdma) {
                                    latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoCdma) cellInfo).getCellIdentity());
                                } else if (cellInfo instanceof CellInfoGsm) {
                                    latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoGsm) cellInfo).getCellIdentity());
                                } else if (cellInfo instanceof CellInfoLte) {
                                    latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoLte) cellInfo).getCellIdentity());
                                } else if (cellInfo instanceof CellInfoWcdma) {
                                    latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoWcdma) cellInfo).getCellIdentity());
                                } else {
                                    logw("Registered cellinfo is unrecognized");
                                    return;
                                }
                            }
                        }
                        break;
                    }
                    return;
                case 1:
                    WifiInfo currentWifiInfo = this.mWifiManager.getConnectionInfo();
                    if (currentWifiInfo != null) {
                        latencyBroadcast.putExtra(EXTRA_SSID, currentWifiInfo.getSSID());
                        latencyBroadcast.putExtra(EXTRA_BSSID, currentWifiInfo.getBSSID());
                        break;
                    }
                    logw("network info is TYPE_WIFI but no ConnectionInfo found");
                    return;
                default:
                    return;
            }
            latencyBroadcast.putExtra(EXTRA_CONNECTIVITY_TYPE, this.mNetworkAgentInfo.networkInfo.getType());
            latencyBroadcast.putExtra(EXTRA_RESPONSE_RECEIVED, responseReceived);
            latencyBroadcast.putExtra(EXTRA_REQUEST_TIMESTAMP_MS, requestTimestampMs);
            if (responseReceived) {
                latencyBroadcast.putExtra(EXTRA_IS_CAPTIVE_PORTAL, isCaptivePortal);
                latencyBroadcast.putExtra(EXTRA_RESPONSE_TIMESTAMP_MS, responseTimestampMs);
            }
            this.mContext.sendBroadcastAsUser(latencyBroadcast, UserHandle.CURRENT, PERMISSION_ACCESS_NETWORK_CONDITIONS);
        }
    }

    private void logNetworkEvent(int evtype) {
        this.mMetricsLog.log(new NetworkEvent(this.mNetId, evtype));
    }

    private void maybeLogEvaluationResult(int evtype) {
        if (this.mEvaluationTimer.isRunning()) {
            this.mMetricsLog.log(new NetworkEvent(this.mNetId, evtype, this.mEvaluationTimer.stop()));
            this.mEvaluationTimer.reset();
        }
    }

    private void logValidationProbe(long durationMs, int probeType, int probeResult) {
        this.mMetricsLog.log(new ValidationProbeEvent(this.mNetId, durationMs, probeType, probeResult));
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0064 A:{SYNTHETIC, Splitter: B:26:0x0064} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x006d A:{SYNTHETIC, Splitter: B:31:0x006d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isDupGateway() {
        Exception e;
        Throwable th;
        log("check isDupGateway");
        if (!this.mNetworkAgentInfo.networkCapabilities.hasTransport(1) || !this.mNetworkAgentInfo.networkInfo.isConnected()) {
            return false;
        }
        ArpPeer arpPeer = null;
        try {
            ArpPeer arpPeer2 = new ArpPeer(this.mContext, this.mNetworkAgentInfo.linkProperties.getInterfaceName(), null, null);
            try {
                if (arpPeer2.isRetryByArpStatus()) {
                    log("Dup. gateway. Wait for CMD_REEVALUATE");
                    int i = this.mReevaluateToken + 1;
                    this.mReevaluateToken = i;
                    sendMessageDelayed(obtainMessage(CMD_REEVALUATE, i, 0), 15000);
                    if (arpPeer2 != null) {
                        try {
                            arpPeer2.close();
                        } catch (Exception e2) {
                        }
                    }
                    return true;
                }
                if (arpPeer2 != null) {
                    try {
                        arpPeer2.close();
                    } catch (Exception e3) {
                    }
                }
                return false;
            } catch (Exception e4) {
                e = e4;
                arpPeer = arpPeer2;
                try {
                    e.printStackTrace();
                    if (arpPeer != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (arpPeer != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                arpPeer = arpPeer2;
                if (arpPeer != null) {
                    try {
                        arpPeer.close();
                    } catch (Exception e5) {
                    }
                }
                throw th;
            }
        } catch (Exception e6) {
            e = e6;
            e.printStackTrace();
            if (arpPeer != null) {
                try {
                    arpPeer.close();
                } catch (Exception e7) {
                }
            }
            return false;
        }
    }

    private boolean isExpRom() {
        return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
    }

    private boolean isNotChineseOperator() {
        ColorOSTelephonyManager colorOSTelephonyManager = ColorOSTelephonyManager.getDefault(this.mContext);
        String mcc = colorOSTelephonyManager.getNetworkOperatorGemini(colorOSTelephonyManager.colorGetDataSubscription());
        if (TextUtils.isEmpty(mcc)) {
            return isExpRom();
        } else {
            if (mcc.startsWith("460")) {
                return false;
            }
            Log.d(TAG, "isNotChineseOperator:not Chinese operator!");
            return true;
        }
    }

    private void sendNetworkAgentInfo() {
        if (this.mNetworkAgentInfo == null || this.mNetworkAgentInfo.networkInfo == null || this.mNetworkAgentInfo.networkInfo.getType() != 1) {
            Log.d(TAG, "do not send info for mobile");
            return;
        }
        Intent netIntent = new Intent(ACTION_WIFI_NETWORK_CONNECT);
        netIntent.addFlags(67108864);
        netIntent.putExtra(EXTRA_WIFI_LINK, this.mNetworkAgentInfo.linkProperties);
        netIntent.putExtra(EXTRA_WIFI_SSID, this.mNetworkAgentInfo.networkInfo.getExtraInfo());
        netIntent.putExtra(EXTRA_WIFI_NETWORK, this.mNetworkAgentInfo.network);
        netIntent.putExtra(EXTRA_WIFI_MANUAL, this.bManualConnect);
        this.mContext.sendBroadcastAsUser(netIntent, UserHandle.ALL);
    }

    private void sendNetworkAvailable(boolean isAvailable) {
        sendNetworkAvailable(isAvailable, null);
    }

    private void sendNetworkAvailable(boolean isAvailable, CaptivePortalProbeResult captiveInfo) {
        Log.d(TAG, "sendNetworkAvailable " + isAvailable);
        if (this.mNetworkAgentInfo == null || this.mNetworkAgentInfo.networkInfo == null || this.mNetworkAgentInfo.networkInfo.getType() != 1) {
            Log.d(TAG, "do not send netstate for mobile");
            return;
        }
        Intent captiveIntent = new Intent(ACTION_WIFI_NETWORK_STATE);
        captiveIntent.addFlags(67108864);
        captiveIntent.putExtra(EXTRA_WIFI_SSID, this.mNetworkAgentInfo.networkInfo.getExtraInfo());
        captiveIntent.putExtra(EXTRA_NETWORK_STATE, isAvailable);
        this.mContext.sendBroadcastAsUser(captiveIntent, UserHandle.ALL);
        if (!isAvailable && captiveInfo != null) {
            setAssistantNetStatistics(this.mNetworkAgentInfo, captiveInfo);
        }
    }

    private boolean hasWlanAssistant() {
        if (this.mContext == null) {
            return false;
        }
        return this.mContext.getPackageManager().hasSystemFeature("oppo.common_center.wlan.assistant") ? Global.getInt(this.mContext.getContentResolver(), "rom.update.wifi.assistant", 1) == 1 : false;
    }

    private boolean isSwitchEnable() {
        if (this.mContext != null) {
            return Global.getInt(this.mContext.getContentResolver(), WIFI_AUTO_CHANGE_ACCESS_POINT, 1) == 1;
        } else {
            return false;
        }
    }

    public String getRomUpdateValue(String key, String defaultVal) {
        if (this.mWifiRomUpdateForNet != null) {
            return this.mWifiRomUpdateForNet.getValue(key, defaultVal);
        }
        return defaultVal;
    }

    public Integer getRomUpdateIntegerValue(String key, Integer defaultVal) {
        if (this.mWifiRomUpdateForNet != null) {
            return this.mWifiRomUpdateForNet.getIntegerValue(key, defaultVal);
        }
        return defaultVal;
    }

    public boolean inSpecialUrlList(String url) {
        if (url == null) {
            log("url is null.");
            return false;
        }
        String value = getRomUpdateValue(WifiRomUpdateHelper.NETWORK_SPECIAL_REDIRECT_URL, DEFAULT_SPECIAL_URL);
        if (value == null) {
            log("Fail to getRomUpdateValue.");
            return false;
        }
        log("inSpecialUrlList(), url list: " + value);
        for (String name : value.split(",")) {
            if (url.contains(name)) {
                return true;
            }
        }
        return false;
    }

    private List<String> getPublicServers() {
        List<String> defaultServers = Arrays.asList(this.mPublicServers);
        String value = getRomUpdateValue(WifiRomUpdateHelper.NETWORK_PUBLIC_SERVERS_URL, null);
        if (value == null) {
            log("Fail to getRomUpdateValue, using default servers!");
            return defaultServers;
        }
        log("getPublicServers, updated servers: " + value);
        List<String> updatedServers = new ArrayList();
        for (String name : value.split(",")) {
            updatedServers.add(name);
        }
        if (updatedServers.size() >= 2) {
            return updatedServers;
        }
        log("updated Servers less than 2, using default servers!");
        return defaultServers;
    }

    private void setAssistantNetStatistics(NetworkAgentInfo agentInfo, CaptivePortalProbeResult captiveInfo) {
        if (agentInfo != null && agentInfo.networkInfo != null && captiveInfo != null) {
            HashMap<String, String> map = new HashMap();
            String ssid = agentInfo.networkInfo.getExtraInfo();
            StringBuilder nbuf = new StringBuilder();
            StringBuilder cbuf = new StringBuilder();
            nbuf.append(agentInfo.network).append(";");
            nbuf.append(ssid).append(";");
            cbuf.append(captiveInfo.detectUrl).append(";");
            cbuf.append(captiveInfo.mHttpResponseCode).append(";");
            map.put(KEY_NETWORK, nbuf.toString());
            map.put(KEY_CAPTIVE, cbuf.toString());
            OppoStatistics.onCommon(this.mContext, WIFI_STATISTIC_KEY, WIFI_ASSISTANT, map, false);
        }
    }
}
