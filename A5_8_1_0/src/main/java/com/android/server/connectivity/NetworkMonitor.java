package com.android.server.connectivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.CaptivePortal;
import android.net.DhcpInfo;
import android.net.ICaptivePortal.Stub;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.arp.OppoArpPeer;
import android.net.arp.OppoArpPeer.ArpPeerChangeCallback;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import oppo.util.OppoStatistics;

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
    public static final int CMD_LAUNCH_CAPTIVE_PORTAL_APP = 532491;
    public static final int CMD_NETWORK_CONNECTED = 532481;
    public static final int CMD_NETWORK_DISCONNECTED = 532487;
    private static final int CMD_REECAPTIVE = 532580;
    private static final int CMD_REEVALUATE = 532486;
    private static final int CMD_TRYSENDLOGININTENT = 532581;
    private static final String CONNECT_MODE_CHANGE_ACTION = "android.net.wifi.CONNECT_MODE_CHANGE";
    private static final boolean DBG = true;
    private static final int DEFALUT_CRL_READ_TIME = 60;
    private static final String DEFAULT_FALLBACK_URL = "http://www.google.com/gen_204";
    private static final String DEFAULT_HTTPS_URL = "https://www.google.com/generate_204";
    private static final String DEFAULT_HTTP_URL = "http://connectivitycheck.gstatic.com/generate_204";
    private static final String DEFAULT_HTTP_URL_IN_CHINA = "http://www.baidu.com";
    private static final String DEFAULT_OTHER_FALLBACK_URLS = "http://play.googleapis.com/generate_204";
    private static final String DEFAULT_SERVER = "connectivitycheck.gstatic.com";
    private static final String DEFAULT_SPECIAL_URL = "360.cn";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.32 Safari/537.36";
    private static final long EVALUATION_TIMEOUT_MS = 30000;
    public static final int EVENT_NETWORK_TESTED = 532482;
    public static final int EVENT_PROVISIONING_NOTIFICATION = 532490;
    public static final String EXTRA_BSSID = "extra_bssid";
    public static final String EXTRA_CELL_ID = "extra_cellid";
    public static final String EXTRA_CONNECTIVITY_TYPE = "extra_connectivity_type";
    private static final String EXTRA_CONNECT_MODE = "connectMode";
    private static final String EXTRA_CONNECT_UID = "connectUid";
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
    private static final String KEY_CAPTIVE_RESULT = "key_captive_result";
    private static final String KEY_NETWORK = "key_network";
    private static final String KEY_NETWORK_MONITOR_AVAILABLE = "oppo.comm.network.monitor.available";
    private static final String KEY_NETWORK_MONITOR_PORTAL = "oppo.comm.network.monitor.portal";
    private static final String KEY_NETWORK_MONITOR_SSID = "oppo.comm.network.monitor.ssid";
    private static final String KEY_UA = "key_ua";
    private static final int MAX_REEVALUATE_DELAY_MS = 600000;
    public static final int NETWORK_TEST_RESULT_INVALID = 1;
    public static final int NETWORK_TEST_RESULT_VALID = 0;
    private static final String PERMISSION_ACCESS_NETWORK_CONDITIONS = "android.permission.ACCESS_NETWORK_CONDITIONS";
    private static final int PROBE_TIMEOUT_MS = 3000;
    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final String TAG = NetworkMonitor.class.getSimpleName();
    private static final boolean VDBG = false;
    private static final String WIFI_ASSISTANT = "wifi_assistant";
    private static final String WIFI_AUTO_CHANGE_ACCESS_POINT = "wifi_auto_change_access_point";
    private static final String WIFI_CAPTIVE_RESULT = "wifi_captive_result";
    private static final String WIFI_DUPLICATE_GATEWAY = "wifi_duplicate_gateway";
    private static final String WIFI_SMART_CONNECT_FEATURE = "oppo.common_center.wifi.smart_connect";
    private static final String WIFI_SMART_CONNECT_PACKAGE_NAME = "com.coloros.wifisecuredetect";
    private static final String WIFI_SMART_CONNECT_SWITCH = "oppo_wifi_smart_connect";
    private static final String WIFI_STATISTIC_KEY = "wifi_fool_proof";
    private static final String captiveServer1 = "conn1.oppomobile.com";
    private static final String captiveServer2 = "conn2.oppomobile.com";
    private static HashMap<String, Boolean> mSentCaptiveResult = new HashMap();
    private static boolean mSkipNetworkValidation = true;
    private final String DEFAULT_UNEXPECTED_MSG;
    private final int MAX_CAPTIVEPORTAL_RESULT;
    private final int SENT_CAPTIVIE_RESULT_MAX;
    private boolean bManualConnect;
    private String[] capServer;
    private String[] capServerExp;
    private boolean haveSendmsg;
    private final AlarmManager mAlarmManager;
    private OppoArpPeer mArpPeer;
    private final int mAutoCaptiveMax;
    private final int mCaptiveMax;
    private final URL[] mCaptivePortalFallbackUrls;
    private final URL mCaptivePortalHttpUrl;
    private final URL mCaptivePortalHttpsUrl;
    private List<CaptivePortalProbeResult> mCaptivePortalResults;
    private final State mCaptivePortalState;
    private final String mCaptivePortalUserAgent;
    private int mConnectUid;
    private final Handler mConnectivityServiceHandler;
    private final Context mContext;
    private final NetworkRequest mDefaultRequest;
    private final State mDefaultState;
    private boolean mDontDisplaySigninNotification;
    private final State mEvaluatingState;
    private final Stopwatch mEvaluationTimer;
    private String[] mFallbackHttpServers;
    private Inet4Address mIPv4Gateway;
    private Inet4Address mIPv4Self;
    private String mInterfaceName;
    protected boolean mIsCaptivePortalCheckEnabled;
    private CaptivePortalProbeResult mLastPortalProbeResult;
    private CustomIntentReceiver mLaunchCaptivePortalAppBroadcastReceiver;
    private BroadcastReceiver mManualReceiver;
    private final int mManulCaptiveMax;
    private final State mMaybeNotifyState;
    private final IpConnectivityLog mMetricsLog;
    private final String[] mNeedValiOperator;
    private final int mNetId;
    private final Network mNetwork;
    private final NetworkAgentInfo mNetworkAgentInfo;
    private int mNextFallbackUrlIndex;
    private String[] mPublicHttpsServers;
    private Random mRandom;
    private int mReecaptiveToken;
    private int mReevaluateToken;
    private String mSSID;
    private boolean mSetUserAgent;
    private final TelephonyManager mTelephonyManager;
    private int mUidResponsibleForReeval;
    private boolean mUseHttps;
    private boolean mUserDoesNotWant;
    private final State mValidatedState;
    private int mValidations;
    private final WifiManager mWifiManager;
    private WifiRomUpdateHelper mWifiRomUpdateForNet;
    public boolean systemReady;
    private final LocalLog validationLogs;

    /* renamed from: com.android.server.connectivity.NetworkMonitor$1ProbeThread */
    final class AnonymousClass1ProbeThread extends Thread {
        private volatile CaptivePortalProbeResult mResult = CaptivePortalProbeResult.FAILED;
        private final URL mUrl;
        final /* synthetic */ CountDownLatch val$latch;

        public AnonymousClass1ProbeThread(URL url, CountDownLatch countDownLatch) {
            this.val$latch = countDownLatch;
            this.mUrl = url;
        }

        public CaptivePortalProbeResult result() {
            return this.mResult;
        }

        public void run() {
            this.mResult = NetworkMonitor.this.sendOPPOHttpProbe(this.mUrl, 1);
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
            } else if (this.mResult.mUnexpectedEnd && (NetworkMonitor.this.mSetUserAgent ^ 1) != 0) {
                this.val$latch.countDown();
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
        final /* synthetic */ ProxyInfo val$proxy;

        public AnonymousClass2ProbeThread(boolean isHttps, ProxyInfo proxyInfo, URL url, URL url2, CountDownLatch countDownLatch) {
            this.val$proxy = proxyInfo;
            this.val$httpsUrl = url;
            this.val$httpUrl = url2;
            this.val$latch = countDownLatch;
            this.mIsHttps = isHttps;
        }

        public CaptivePortalProbeResult result() {
            return this.mResult;
        }

        public void run() {
            if (this.mIsHttps) {
                this.mResult = NetworkMonitor.this.sendDnsAndHttpProbes(this.val$proxy, this.val$httpsUrl, 2);
            } else {
                this.mResult = NetworkMonitor.this.sendDnsAndHttpProbes(this.val$proxy, this.val$httpUrl, 1);
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
        static final CaptivePortalProbeResult FAILED = new CaptivePortalProbeResult(FAILED_CODE);
        static final int FAILED_CODE = 599;
        static final int MAX_UA_LENTH = 10;
        static final CaptivePortalProbeResult SUCCESS = new CaptivePortalProbeResult(SUCCESS_CODE);
        static final int SUCCESS_CODE = 204;
        final String detectUrl;
        String mDetailMessage;
        private final int mHttpResponseCode;
        String mSSID;
        private String mUA;
        boolean mUnexpectedEnd;
        public boolean mUnknownHostException;
        final String redirectUrl;

        public CaptivePortalProbeResult(int httpResponseCode, String redirectUrl, String detectUrl) {
            this.mUnknownHostException = false;
            this.mUnexpectedEnd = false;
            this.mDetailMessage = null;
            this.mUA = null;
            this.mSSID = null;
            this.mHttpResponseCode = httpResponseCode;
            this.redirectUrl = redirectUrl;
            this.detectUrl = detectUrl;
        }

        public CaptivePortalProbeResult(int httpResponseCode) {
            this(httpResponseCode, null, null);
        }

        boolean isSuccessful() {
            return this.mHttpResponseCode == SUCCESS_CODE;
        }

        boolean isPortal() {
            return !isSuccessful() && this.mHttpResponseCode >= 200 && this.mHttpResponseCode <= 399;
        }

        boolean isFailed() {
            return !isSuccessful() ? isPortal() ^ 1 : false;
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
        Intent backupIntent;
        boolean haveStartedActivity;
        int mAtempCaptive;
        int mDetectCount;

        /* synthetic */ CaptivePortalState(NetworkMonitor this$0, CaptivePortalState -this1) {
            this();
        }

        private CaptivePortalState() {
            this.mAtempCaptive = 1;
            this.mDetectCount = 0;
            this.haveStartedActivity = false;
        }

        public void enter() {
            NetworkMonitor.this.maybeLogEvaluationResult(NetworkMonitor.this.networkEventType(NetworkMonitor.this.validationStage(), EvaluationResult.CAPTIVE_PORTAL));
            if (!NetworkMonitor.this.mDontDisplaySigninNotification) {
                String url;
                Intent loginIntent;
                if (!TextUtils.isEmpty(NetworkMonitor.this.mLastPortalProbeResult.redirectUrl) && NetworkMonitor.this.mLastPortalProbeResult.isRedirection()) {
                    url = NetworkMonitor.this.mLastPortalProbeResult.redirectUrl;
                } else if (NetworkMonitor.this.isNotChineseOperator()) {
                    url = NetworkMonitor.DEFAULT_HTTP_URL;
                } else {
                    url = NetworkMonitor.DEFAULT_HTTP_URL_IN_CHINA;
                }
                Intent loginIntentFromWizard = new Intent("com.oppo.browser.action.WIFI_LOGIN", Uri.parse(url));
                if ((Global.getInt(NetworkMonitor.this.mContext.getContentResolver(), "device_provisioned", 0) == 1) || loginIntentFromWizard.resolveActivity(NetworkMonitor.this.mContext.getPackageManager()) == null) {
                    loginIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    String targetPackage = "com.android.browser";
                    String region = SystemProperties.get("ro.oppo.region.netlock", "");
                    boolean isExp = NetworkMonitor.this.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
                    boolean hasFeature = NetworkMonitor.this.mContext.getPackageManager().hasSystemFeature("oppo.exp.default.browser");
                    NetworkMonitor.this.log("isExp=" + isExp + ", hasFeature=" + hasFeature + ", region=" + region);
                    if (isExp && hasFeature && ("IN".equals(region) ^ 1) != 0) {
                        targetPackage = "com.android.chrome";
                    }
                    boolean hasTargetPackage = false;
                    try {
                        hasTargetPackage = NetworkMonitor.this.mContext.getPackageManager().getPackageInfo(targetPackage, 0) != null;
                    } catch (NameNotFoundException e) {
                        NetworkMonitor.this.log("targetPackage " + targetPackage + " not found");
                    }
                    if (hasTargetPackage) {
                        loginIntent.setPackage(targetPackage);
                    }
                } else {
                    loginIntent = loginIntentFromWizard;
                }
                loginIntent.setFlags(272629760);
                WifiInfo wifiInfo = NetworkMonitor.this.mWifiManager.getConnectionInfo();
                loginIntent.putExtra("ap_name", wifiInfo == null ? "" : wifiInfo.getSSID());
                this.backupIntent = new Intent(loginIntent);
                NetworkMonitor.this.log("CaptivePortalState visit with URL:" + url + " action=" + loginIntent.getAction() + " SSID=" + (wifiInfo == null ? "" : wifiInfo.getSSID()));
                if (NetworkMonitor.this.mNetworkAgentInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() != 1) {
                    NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, 1, NetworkMonitor.this.mNetworkAgentInfo.network.netId, PendingIntent.getActivity(NetworkMonitor.this.mContext, 0, loginIntent, 0)));
                } else {
                    boolean flag = true;
                    if (NetworkMonitor.this.isWifiSmartConnect()) {
                        NetworkMonitor.this.log("isWifiSmartConnect don't start captive portal!");
                    } else if (NetworkMonitor.this.bManualConnect) {
                        this.haveStartedActivity = true;
                        NetworkMonitor.this.mContext.startActivityAsUser(loginIntent, UserHandle.CURRENT);
                    } else {
                        if (NetworkMonitor.this.hasWlanAssistant() ? NetworkMonitor.this.isSwitchEnable() : false) {
                            flag = false;
                        } else {
                            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, 1, NetworkMonitor.this.mNetworkAgentInfo.network.netId, PendingIntent.getActivity(NetworkMonitor.this.mContext, 0, loginIntent, 0)));
                        }
                    }
                    if (flag) {
                        Global.putString(NetworkMonitor.this.mContext.getContentResolver(), NetworkMonitor.KEY_NETWORK_MONITOR_SSID, NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getExtraInfo());
                        Global.putInt(NetworkMonitor.this.mContext.getContentResolver(), NetworkMonitor.KEY_NETWORK_MONITOR_AVAILABLE, 0);
                        Global.putInt(NetworkMonitor.this.mContext.getContentResolver(), NetworkMonitor.KEY_NETWORK_MONITOR_PORTAL, 1);
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
                networkMonitor = NetworkMonitor.this;
                networkMonitor.mValidations = networkMonitor.mValidations + 1;
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
                case NetworkMonitor.CMD_TRYSENDLOGININTENT /*532581*/:
                    tryStartCaptivePortalActivity(this.backupIntent);
                    return true;
                default:
                    return false;
            }
        }

        private void tryStartCaptivePortalActivity(Intent intent) {
            if (!(intent == null || NetworkMonitor.this.mNetworkAgentInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() != 1 || !NetworkMonitor.this.bManualConnect || (this.haveStartedActivity ^ 1) == 0)) {
                this.haveStartedActivity = true;
                NetworkMonitor.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
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
            this.mAction = action + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + NetworkMonitor.this.mNetId + LocationManagerService.OPPO_FAKE_LOCATION_SPLIT + token;
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
        /* synthetic */ DefaultState(NetworkMonitor this$0, DefaultState -this1) {
            this();
        }

        private DefaultState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_NETWORK_CONNECTED /*532481*/:
                    NetworkMonitor.this.logNetworkEvent(1);
                    NetworkMonitor.this.sendNetworkAgentInfo();
                    NetworkMonitor.this.setCRLReadTimeout(NetworkMonitor.this.getRomUpdateIntegerValue(WifiRomUpdateHelper.NETWORK_CRL_READ_TIMEOUT, Integer.valueOf(60)).intValue());
                    NetworkMonitor.this.transitionTo(NetworkMonitor.this.mEvaluatingState);
                    return true;
                case NetworkMonitor.CMD_NETWORK_DISCONNECTED /*532487*/:
                    NetworkMonitor.this.logNetworkEvent(7);
                    if (NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver != null) {
                        NetworkMonitor.this.mContext.unregisterReceiver(NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver);
                        NetworkMonitor.this.mLaunchCaptivePortalAppBroadcastReceiver = null;
                    }
                    NetworkMonitor.this.setCRLReadTimeout(0);
                    if (NetworkMonitor.this.mNetworkAgentInfo.networkInfo != null && NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() == 1) {
                        try {
                            NetworkMonitor.this.mContext.unregisterReceiver(NetworkMonitor.this.mManualReceiver);
                        } catch (IllegalArgumentException e) {
                        }
                    }
                    if (!(NetworkMonitor.this.mNetworkAgentInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() != 1)) {
                        Global.putString(NetworkMonitor.this.mContext.getContentResolver(), NetworkMonitor.KEY_NETWORK_MONITOR_SSID, NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getExtraInfo());
                        Global.putInt(NetworkMonitor.this.mContext.getContentResolver(), NetworkMonitor.KEY_NETWORK_MONITOR_AVAILABLE, 0);
                        Global.putInt(NetworkMonitor.this.mContext.getContentResolver(), NetworkMonitor.KEY_NETWORK_MONITOR_PORTAL, 0);
                    }
                    if (NetworkMonitor.this.mArpPeer != null) {
                        NetworkMonitor.this.mArpPeer.close();
                        NetworkMonitor.this.mArpPeer = null;
                    }
                    NetworkMonitor.this.mSetUserAgent = false;
                    if (NetworkMonitor.this.mWifiRomUpdateForNet != null && NetworkMonitor.this.mWifiRomUpdateForNet.getBooleanValue(WifiRomUpdateHelper.NETWORK_COLLECT_CAPTIVERESULT, true)) {
                        NetworkMonitor.this.triggerCaptiveResultBack();
                        NetworkMonitor.this.clearCaptivePortalResults();
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
        private ArpPeerChangeCallback mArpResponseCallback;
        private int mAttempts;
        private long mEevaluateStartTime;
        private int mReevaluateDelayMs;

        /* synthetic */ EvaluatingState(NetworkMonitor this$0, EvaluatingState -this1) {
            this();
        }

        private EvaluatingState() {
            this.mEevaluateStartTime = 0;
            this.mArpResponseCallback = new ArpPeerChangeCallback() {
                public void onArpReponseChanged(int arpResponseReceieved) {
                    if (arpResponseReceieved == 1) {
                        NetworkMonitor networkMonitor = NetworkMonitor.this;
                        NetworkMonitor networkMonitor2 = NetworkMonitor.this;
                        networkMonitor.sendMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0);
                    }
                }
            };
        }

        public void enter() {
            if (!NetworkMonitor.this.mEvaluationTimer.isStarted()) {
                NetworkMonitor.this.mEvaluationTimer.start();
            }
            if (NetworkMonitor.this.mWifiRomUpdateForNet != null && NetworkMonitor.this.mWifiRomUpdateForNet.getBooleanValue(WifiRomUpdateHelper.NETWORK_HANDLE_GATEWAY_CONFLICT, false)) {
                NetworkMonitor.this.mArpPeer = new OppoArpPeer(NetworkMonitor.this.mContext, NetworkMonitor.this.mNetworkAgentInfo.network, this.mArpResponseCallback);
            }
            NetworkMonitor networkMonitor;
            NetworkMonitor networkMonitor2;
            if (NetworkMonitor.this.mArpPeer != null) {
                NetworkMonitor.this.setPreConditionForGatewayCheck();
                long delay = 0;
                if (NetworkMonitor.this.needToCheckGateway() && NetworkMonitor.this.probeGateway() && NetworkMonitor.this.mArpPeer.fetchGatewayMacFromRoute() == null) {
                    delay = 2000;
                }
                networkMonitor = NetworkMonitor.this;
                networkMonitor2 = NetworkMonitor.this;
                networkMonitor.sendMessageDelayed(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, delay);
            } else {
                networkMonitor = NetworkMonitor.this;
                networkMonitor2 = NetworkMonitor.this;
                networkMonitor.sendMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0);
            }
            if (NetworkMonitor.this.mUidResponsibleForReeval != -1) {
                TrafficStats.setThreadStatsUid(NetworkMonitor.this.mUidResponsibleForReeval);
                NetworkMonitor.this.mUidResponsibleForReeval = -1;
            }
            this.mReevaluateDelayMs = 1000;
            this.mAttempts = 0;
            NetworkMonitor.this.haveSendmsg = false;
        }

        public boolean processMessage(Message message) {
            boolean z = false;
            switch (message.what) {
                case NetworkMonitor.CMD_REEVALUATE /*532486*/:
                    if (message.arg1 != NetworkMonitor.this.mReevaluateToken) {
                        return true;
                    }
                    if (!NetworkMonitor.this.mDefaultRequest.networkCapabilities.satisfiedByNetworkCapabilities(NetworkMonitor.this.mNetworkAgentInfo.networkCapabilities) || NetworkMonitor.this.mNetworkAgentInfo.networkCapabilities.hasCapability(0)) {
                        NetworkMonitor.this.validationLog("Network would not satisfy default request, not validating");
                        NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                        return true;
                    }
                    boolean z2;
                    this.mAttempts++;
                    if (this.mAttempts == 1) {
                        this.mEevaluateStartTime = SystemClock.elapsedRealtime();
                    }
                    if (NetworkMonitor.this.isNeedValidateOperator()) {
                        z2 = false;
                    } else {
                        z2 = NetworkMonitor.this.mNetworkAgentInfo.networkCapabilities.hasTransport(0);
                    }
                    NetworkMonitor.mSkipNetworkValidation = z2;
                    if (NetworkMonitor.mSkipNetworkValidation) {
                        NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                        NetworkMonitor.this.log(" oppo modify transfer to mValidatedState");
                        return true;
                    }
                    if (NetworkMonitor.this.needToCheckGateway()) {
                        NetworkMonitor.this.mArpPeer.prepareNextAvailbeGateway();
                    }
                    CaptivePortalProbeResult probeResult = NetworkMonitor.this.isCaptivePortal();
                    if (!(NetworkMonitor.this.mNetworkAgentInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() != 1)) {
                        Network network = NetworkMonitor.this.mWifiManager.getCurrentNetwork();
                        if (network == null || (network.equals(NetworkMonitor.this.mNetworkAgentInfo.network) ^ 1) != 0) {
                            NetworkMonitor.this.log("networkmonitor net id has changed");
                            return true;
                        }
                    }
                    NetworkMonitor networkMonitor;
                    NetworkMonitor networkMonitor2;
                    if (probeResult.isSuccessful()) {
                        NetworkMonitor.this.transitionTo(NetworkMonitor.this.mValidatedState);
                    } else if (probeResult.isPortal()) {
                        if ((!NetworkMonitor.this.bManualConnect || (NetworkMonitor.this.shallAccountForDupGateway() ^ 1) == 0) && this.mAttempts <= 1) {
                            networkMonitor = NetworkMonitor.this;
                            networkMonitor2 = NetworkMonitor.this;
                            NetworkMonitor.this.sendMessageDelayed(networkMonitor.obtainMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0), (long) this.mReevaluateDelayMs);
                        } else {
                            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, probeResult.redirectUrl));
                            NetworkMonitor.this.mLastPortalProbeResult = probeResult;
                            NetworkMonitor.this.transitionTo(NetworkMonitor.this.mCaptivePortalState);
                        }
                    } else if (!probeResult.isRouteNotReady() || this.mAttempts > 3) {
                        if (!(NetworkMonitor.this.mNetworkAgentInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo == null || NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() != 1)) {
                            if (NetworkMonitor.this.shallAccountForDupGateway() && this.mAttempts == 1) {
                                networkMonitor = NetworkMonitor.this;
                                networkMonitor2 = NetworkMonitor.this;
                                NetworkMonitor.this.sendMessage(networkMonitor.obtainMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0));
                                return true;
                            }
                            NetworkMonitor.this.log("mAttempts=" + this.mAttempts + ", mEevaluateStartTime=" + this.mEevaluateStartTime + ", mUnknownHostException" + probeResult.mUnknownHostException);
                            if (NetworkMonitor.this.hasWlanAssistant() && (NetworkMonitor.this.haveSendmsg ^ 1) != 0) {
                                if (this.mAttempts > 3 || (NetworkMonitor.this.bManualConnect && (needReevaluate(probeResult) ^ 1) != 0)) {
                                    NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 1, NetworkMonitor.this.mNetId, probeResult.redirectUrl));
                                    NetworkMonitor.this.sendNetworkAvailable(false);
                                    NetworkMonitor.this.haveSendmsg = true;
                                } else {
                                    if (probeResult.mUnexpectedEnd) {
                                        NetworkMonitor.this.mSetUserAgent = true;
                                    }
                                    networkMonitor = NetworkMonitor.this;
                                    networkMonitor2 = NetworkMonitor.this;
                                    NetworkMonitor.this.sendMessageDelayed(networkMonitor.obtainMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0), (long) this.mReevaluateDelayMs);
                                    this.mReevaluateDelayMs *= 2;
                                }
                                return true;
                            } else if (!NetworkMonitor.this.hasWlanAssistant()) {
                                if (this.mAttempts > 3 || (needReevaluate(probeResult) ^ 1) != 0) {
                                    NetworkMonitor.this.sendNetworkAvailable(false);
                                } else {
                                    if (probeResult.mUnexpectedEnd) {
                                        NetworkMonitor.this.mSetUserAgent = true;
                                    }
                                    networkMonitor = NetworkMonitor.this;
                                    networkMonitor2 = NetworkMonitor.this;
                                    NetworkMonitor.this.sendMessageDelayed(networkMonitor.obtainMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0), (long) this.mReevaluateDelayMs);
                                    this.mReevaluateDelayMs *= 2;
                                }
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
                    } else {
                        networkMonitor = NetworkMonitor.this;
                        networkMonitor2 = NetworkMonitor.this;
                        NetworkMonitor.this.sendMessageDelayed(networkMonitor.obtainMessage(NetworkMonitor.CMD_REEVALUATE, networkMonitor2.mReevaluateToken = networkMonitor2.mReevaluateToken + 1, 0), (long) this.mReevaluateDelayMs);
                    }
                    return true;
                case NetworkMonitor.CMD_FORCE_REEVALUATION /*532488*/:
                    if (this.mAttempts < 5) {
                        z = true;
                    }
                    return z;
                default:
                    return false;
            }
        }

        private boolean needReevaluate(CaptivePortalProbeResult probeResult) {
            if (probeResult.mUnknownHostException || SystemClock.elapsedRealtime() - this.mEevaluateStartTime >= 30000) {
                return false;
            }
            return true;
        }

        public void exit() {
            TrafficStats.clearThreadStatsUid();
        }
    }

    enum EvaluationResult {
        VALIDATED(true),
        CAPTIVE_PORTAL(false);
        
        final boolean isValidated;

        private EvaluationResult(boolean isValidated) {
            this.isValidated = isValidated;
        }
    }

    private class MaybeNotifyState extends State {
        /* synthetic */ MaybeNotifyState(NetworkMonitor this$0, MaybeNotifyState -this1) {
            this();
        }

        private MaybeNotifyState() {
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case NetworkMonitor.CMD_LAUNCH_CAPTIVE_PORTAL_APP /*532491*/:
                    Intent intent = new Intent("android.net.conn.CAPTIVE_PORTAL");
                    intent.putExtra("android.net.extra.NETWORK", new Network(NetworkMonitor.this.mNetwork));
                    intent.putExtra("android.net.extra.CAPTIVE_PORTAL", new CaptivePortal(new Stub() {
                        public void appResponse(int response) {
                            if (response == 2) {
                                NetworkMonitor.this.mContext.enforceCallingPermission("android.permission.CONNECTIVITY_INTERNAL", "CaptivePortal");
                            }
                            NetworkMonitor.this.sendMessage(NetworkMonitor.CMD_CAPTIVE_PORTAL_APP_FINISHED, response);
                        }
                    }));
                    intent.putExtra("android.net.extra.CAPTIVE_PORTAL_URL", NetworkMonitor.this.mLastPortalProbeResult.detectUrl);
                    intent.putExtra("android.net.extra.CAPTIVE_PORTAL_USER_AGENT", NetworkMonitor.this.mCaptivePortalUserAgent);
                    intent.setFlags(272629760);
                    NetworkMonitor.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                    return true;
                default:
                    return false;
            }
        }

        public void exit() {
            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_PROVISIONING_NOTIFICATION, 0, NetworkMonitor.this.mNetId, null));
        }
    }

    private static class OneAddressPerFamilyNetwork extends Network {
        public OneAddressPerFamilyNetwork(Network network) {
            super(network);
        }

        public InetAddress[] getAllByName(String host) throws UnknownHostException {
            List<InetAddress> addrs = Arrays.asList(super.getAllByName(host));
            LinkedHashMap<Class, InetAddress> addressByFamily = new LinkedHashMap();
            addressByFamily.put(((InetAddress) addrs.get(0)).getClass(), (InetAddress) addrs.get(0));
            Collections.shuffle(addrs);
            for (InetAddress addr : addrs) {
                addressByFamily.put(addr.getClass(), addr);
            }
            return (InetAddress[]) addressByFamily.values().toArray(new InetAddress[addressByFamily.size()]);
        }
    }

    private class ValidatedState extends State {
        /* synthetic */ ValidatedState(NetworkMonitor this$0, ValidatedState -this1) {
            this();
        }

        private ValidatedState() {
        }

        public void enter() {
            NetworkMonitor.this.maybeLogEvaluationResult(NetworkMonitor.this.networkEventType(NetworkMonitor.this.validationStage(), EvaluationResult.VALIDATED));
            NetworkMonitor.this.mConnectivityServiceHandler.sendMessage(NetworkMonitor.this.obtainMessage(NetworkMonitor.EVENT_NETWORK_TESTED, 0, NetworkMonitor.this.mNetId, null));
            NetworkMonitor networkMonitor = NetworkMonitor.this;
            networkMonitor.mValidations = networkMonitor.mValidations + 1;
            NetworkMonitor.this.sendNetworkAvailable(true);
            if (NetworkMonitor.this.mNetworkAgentInfo != null && NetworkMonitor.this.mNetworkAgentInfo.networkInfo != null && NetworkMonitor.this.mNetworkAgentInfo.networkInfo.getType() == 1 && NetworkMonitor.this.shallAccountForDupGateway()) {
                NetworkMonitor.this.setDuplicateGatewayStatics();
            }
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

    enum ValidationStage {
        FIRST_VALIDATION(true),
        REVALIDATION(false);
        
        final boolean isFirstValidation;

        private ValidationStage(boolean isFirstValidation) {
            this.isFirstValidation = isFirstValidation;
        }
    }

    public NetworkMonitor(Context context, Handler handler, NetworkAgentInfo networkAgentInfo, NetworkRequest defaultRequest) {
        this(context, handler, networkAgentInfo, defaultRequest, new IpConnectivityLog());
    }

    protected NetworkMonitor(Context context, Handler handler, NetworkAgentInfo networkAgentInfo, NetworkRequest defaultRequest, IpConnectivityLog logger) {
        boolean z;
        super(TAG + networkAgentInfo.name());
        this.mReevaluateToken = 0;
        this.mUidResponsibleForReeval = -1;
        this.mValidations = 0;
        this.mUserDoesNotWant = false;
        this.mDontDisplaySigninNotification = false;
        this.systemReady = false;
        this.mDefaultState = new DefaultState(this, null);
        this.mValidatedState = new ValidatedState(this, null);
        this.mMaybeNotifyState = new MaybeNotifyState(this, null);
        this.mEvaluatingState = new EvaluatingState(this, null);
        this.mCaptivePortalState = new CaptivePortalState(this, null);
        this.mNeedValiOperator = new String[]{"505"};
        this.mLaunchCaptivePortalAppBroadcastReceiver = null;
        this.validationLogs = new LocalLog(20);
        this.mEvaluationTimer = new Stopwatch();
        this.mLastPortalProbeResult = CaptivePortalProbeResult.FAILED;
        this.mNextFallbackUrlIndex = 0;
        this.mReecaptiveToken = 0;
        this.mCaptiveMax = 3;
        this.mAutoCaptiveMax = 3;
        this.mManulCaptiveMax = 12;
        this.capServer = new String[]{captiveServer1, captiveServer2};
        this.mPublicHttpsServers = new String[]{"https://m.baidu.com", "https://sina.cn", "https://m.sohu.com"};
        this.mFallbackHttpServers = new String[]{"http://info.3g.qq.com", "http://www.google.cn/generate_204", "http://developers.google.cn/generate_204"};
        this.mRandom = new Random(Calendar.getInstance().getTimeInMillis());
        this.haveSendmsg = false;
        this.mWifiRomUpdateForNet = null;
        this.capServerExp = new String[]{DEFAULT_SERVER, captiveServer1};
        this.bManualConnect = false;
        this.mConnectUid = 1000;
        this.mManualReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkMonitor.this.bManualConnect = intent.getBooleanExtra(NetworkMonitor.EXTRA_CONNECT_MODE, false);
                if (NetworkMonitor.this.bManualConnect) {
                    NetworkMonitor.this.removeMessages(NetworkMonitor.CMD_TRYSENDLOGININTENT);
                    NetworkMonitor.this.sendMessage(NetworkMonitor.CMD_TRYSENDLOGININTENT);
                }
                NetworkMonitor.this.mConnectUid = intent.getIntExtra(NetworkMonitor.EXTRA_CONNECT_UID, 1000);
            }
        };
        this.mArpPeer = null;
        this.mIPv4Gateway = null;
        this.mIPv4Self = null;
        this.mInterfaceName = null;
        this.DEFAULT_UNEXPECTED_MSG = "unexpected end of stream on ";
        this.MAX_CAPTIVEPORTAL_RESULT = 6;
        this.SENT_CAPTIVIE_RESULT_MAX = 1000;
        this.mSetUserAgent = false;
        this.mCaptivePortalResults = new ArrayList(6);
        this.mSSID = null;
        this.mContext = context;
        this.mMetricsLog = logger;
        this.mConnectivityServiceHandler = handler;
        this.mNetworkAgentInfo = networkAgentInfo;
        this.mNetwork = new OneAddressPerFamilyNetwork(networkAgentInfo.network);
        this.mNetId = this.mNetwork.netId;
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
        if (Global.getInt(this.mContext.getContentResolver(), "captive_portal_mode", 1) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsCaptivePortalCheckEnabled = z;
        this.mUseHttps = Global.getInt(this.mContext.getContentResolver(), "captive_portal_use_https", 0) == 1;
        this.mWifiRomUpdateForNet = new WifiRomUpdateHelper(this.mContext);
        this.mCaptivePortalUserAgent = getCaptivePortalUserAgent(context);
        List<String> updatedServers = tryUpdateCaptiveServer();
        if (this.mNetworkAgentInfo == null || this.mNetworkAgentInfo.networkInfo == null || this.mNetworkAgentInfo.networkInfo.getType() != 1) {
            this.mCaptivePortalHttpsUrl = makeURL(getCaptivePortalServerHttpsUrl(this.mContext));
            this.mCaptivePortalHttpUrl = makeURL(getCaptivePortalServerHttpUrl(this.mContext));
        } else {
            List<String> publichHttpsServers = getPublicHttpsServers();
            Collections.shuffle(publichHttpsServers);
            this.mCaptivePortalHttpsUrl = makeURL((String) publichHttpsServers.get(0));
            this.mCaptivePortalHttpUrl = makeURL("http://" + ((String) updatedServers.get(this.mRandom.nextInt(updatedServers.size()) % updatedServers.size())) + "/generate_204");
        }
        this.mCaptivePortalFallbackUrls = makeCaptivePortalFallbackUrls(context);
        start();
        if (this.mNetworkAgentInfo.networkInfo != null && this.mNetworkAgentInfo.networkInfo.getType() == 1) {
            this.mContext.registerReceiverAsUser(this.mManualReceiver, UserHandle.ALL, new IntentFilter(CONNECT_MODE_CHANGE_ACTION), null, null);
        }
        if (this.mNetworkAgentInfo != null && this.mNetworkAgentInfo.networkInfo != null) {
            this.mSSID = this.mNetworkAgentInfo.networkInfo.getExtraInfo();
        }
    }

    protected void log(String s) {
        Log.d(TAG + "/" + this.mNetworkAgentInfo.name(), s);
    }

    private void validationLog(int probeType, Object url, String msg) {
        validationLog(String.format("%s %s %s", new Object[]{ValidationProbeEvent.getProbeName(probeType), url, msg}));
    }

    private void validationLog(String s) {
        log(s);
        this.validationLogs.log(s);
    }

    public ReadOnlyLocalLog getValidationLogs() {
        return this.validationLogs.readOnlyLocalLog();
    }

    private ValidationStage validationStage() {
        return this.mValidations == 0 ? ValidationStage.FIRST_VALIDATION : ValidationStage.REVALIDATION;
    }

    private boolean isWifiSmartConnect() {
        boolean wifiSmartConnectFeature = false;
        int wifiSmartConnectSwitch = 0;
        String[] packageNames = null;
        if (this.mContext != null) {
            wifiSmartConnectFeature = this.mContext.getPackageManager().hasSystemFeature(WIFI_SMART_CONNECT_FEATURE);
            wifiSmartConnectSwitch = Global.getInt(this.mContext.getContentResolver(), WIFI_SMART_CONNECT_SWITCH, 0);
            if (this.mConnectUid == 1000 || this.mConnectUid == 1010) {
                packageNames = null;
            } else {
                packageNames = this.mContext.getPackageManager().getPackagesForUid(this.mConnectUid);
            }
        }
        if (wifiSmartConnectFeature && wifiSmartConnectSwitch == 1 && packageNames != null) {
            for (String pkName : packageNames) {
                if (WIFI_SMART_CONNECT_PACKAGE_NAME.equals(pkName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getCaptivePortalServerHttpsUrl(Context context) {
        return getSetting(context, "captive_portal_https_url", DEFAULT_HTTPS_URL);
    }

    public static String getCaptivePortalServerHttpUrl(Context context) {
        return getSetting(context, "captive_portal_http_url", DEFAULT_HTTP_URL);
    }

    private URL[] makeCaptivePortalFallbackUrls(Context context) {
        String separator = ",";
        String joinedUrls = getSetting(context, "captive_portal_fallback_url", DEFAULT_FALLBACK_URL) + separator + getSetting(context, "captive_portal_other_fallback_urls", DEFAULT_OTHER_FALLBACK_URLS);
        List<URL> urls = new ArrayList();
        for (String s : joinedUrls.split(separator)) {
            URL u = makeURL(s);
            if (u != null) {
                urls.add(u);
            }
        }
        if (urls.isEmpty()) {
            Log.e(TAG, String.format("could not create any url from %s", new Object[]{joinedUrls}));
        }
        return (URL[]) urls.toArray(new URL[urls.size()]);
    }

    private static String getCaptivePortalUserAgent(Context context) {
        return getSetting(context, "captive_portal_user_agent", DEFAULT_USER_AGENT);
    }

    private static String getSetting(Context context, String symbol, String defaultValue) {
        String value = Global.getString(context.getContentResolver(), symbol);
        return value != null ? value : defaultValue;
    }

    private URL nextFallbackUrl() {
        if (this.mCaptivePortalFallbackUrls.length == 0) {
            return null;
        }
        int idx = Math.abs(this.mNextFallbackUrlIndex) % this.mCaptivePortalFallbackUrls.length;
        this.mNextFallbackUrlIndex += new Random().nextInt();
        return this.mCaptivePortalFallbackUrls[idx];
    }

    protected CaptivePortalProbeResult isCaptivePortal() {
        if (this.mIsCaptivePortalCheckEnabled) {
            URL pacUrl = null;
            URL httpsUrl = this.mCaptivePortalHttpsUrl;
            URL httpUrl = this.mCaptivePortalHttpUrl;
            ProxyInfo proxyInfo = this.mNetworkAgentInfo.linkProperties.getHttpProxy();
            if (!(proxyInfo == null || (Uri.EMPTY.equals(proxyInfo.getPacFileUrl()) ^ 1) == 0)) {
                pacUrl = makeURL(proxyInfo.getPacFileUrl().toString());
                if (pacUrl == null) {
                    return CaptivePortalProbeResult.FAILED;
                }
            }
            if (pacUrl == null && (httpUrl == null || httpsUrl == null)) {
                return CaptivePortalProbeResult.FAILED;
            }
            CaptivePortalProbeResult result;
            long startTime = SystemClock.elapsedRealtime();
            if (pacUrl != null) {
                result = sendDnsAndHttpProbes(null, pacUrl, 3);
            } else if (this.mUseHttps) {
                result = sendParallelHttpProbes(proxyInfo, httpsUrl, httpUrl);
            } else {
                result = sendOPPOParallelHttpProbes(proxyInfo, httpsUrl, httpUrl);
            }
            sendNetworkConditionsBroadcast(true, result.isPortal(), startTime, SystemClock.elapsedRealtime());
            return result;
        }
        validationLog("Validation disabled.");
        return CaptivePortalProbeResult.SUCCESS;
    }

    private CaptivePortalProbeResult sendDnsAndHttpProbes(ProxyInfo proxy, URL url, int probeType) {
        sendDnsProbe(proxy != null ? proxy.getHost() : url.getHost());
        return sendHttpProbe(url, probeType);
    }

    private void sendDnsProbe(String host) {
        if (!TextUtils.isEmpty(host)) {
            int result;
            String connectInfo;
            String name = ValidationProbeEvent.getProbeName(0);
            Stopwatch watch = new Stopwatch().start();
            try {
                InetAddress[] addresses = this.mNetwork.getAllByName(host);
                StringBuffer buffer = new StringBuffer();
                for (InetAddress address : addresses) {
                    buffer.append(',').append(address.getHostAddress());
                }
                result = 1;
                connectInfo = "OK " + buffer.substring(1);
            } catch (UnknownHostException e) {
                result = 0;
                connectInfo = "FAIL";
            }
            validationLog(0, host, String.format("%dms %s", new Object[]{Long.valueOf(watch.stop()), connectInfo}));
            logValidationProbe(latency, 0, result);
        }
    }

    protected CaptivePortalProbeResult sendHttpProbe(URL url, int probeType) {
        HttpURLConnection urlConnection = null;
        int httpResponseCode = 599;
        String redirectUrl = null;
        Stopwatch probeTimer = new Stopwatch().start();
        int oldTag = TrafficStats.getAndSetThreadStatsTag(-190);
        try {
            urlConnection = (HttpURLConnection) this.mNetwork.openConnection(url);
            urlConnection.setInstanceFollowRedirects(probeType == 3);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setUseCaches(false);
            if (this.mCaptivePortalUserAgent != null) {
                urlConnection.setRequestProperty("User-Agent", this.mCaptivePortalUserAgent);
            }
            String requestHeader = urlConnection.getRequestProperties().toString();
            long requestTimestamp = SystemClock.elapsedRealtime();
            httpResponseCode = urlConnection.getResponseCode();
            redirectUrl = urlConnection.getHeaderField("location");
            validationLog(probeType, url, "time=" + (SystemClock.elapsedRealtime() - requestTimestamp) + "ms" + " ret=" + httpResponseCode + " request=" + requestHeader + " headers=" + urlConnection.getHeaderFields());
            if (httpResponseCode == 200) {
                if (probeType == 3) {
                    validationLog(probeType, url, "PAC fetch 200 response interpreted as 204 response.");
                    httpResponseCode = 204;
                } else if (urlConnection.getContentLengthLong() == 0) {
                    validationLog(probeType, url, "200 response with Content-length=0 interpreted as 204 response.");
                    httpResponseCode = 204;
                } else if (urlConnection.getContentLengthLong() == -1 && urlConnection.getInputStream().read() == -1) {
                    validationLog(probeType, url, "Empty 200 response interpreted as 204 response.");
                    httpResponseCode = 204;
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
        } catch (IOException e) {
            validationLog(probeType, url, "Probe failed with exception " + e);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
        } catch (Throwable th) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
            throw th;
        }
        logValidationProbe(probeTimer.stop(), probeType, httpResponseCode);
        return new CaptivePortalProbeResult(httpResponseCode, redirectUrl, url.toString());
    }

    protected CaptivePortalProbeResult sendOPPOHttpProbe(URL url, int probeType) {
        CaptivePortalProbeResult captivePortalProbeResult;
        HttpURLConnection httpURLConnection = null;
        int httpResponseCode = 599;
        String redirectUrl = null;
        Stopwatch probeTimer = new Stopwatch().start();
        int oldTag = TrafficStats.getAndSetThreadStatsTag(-190);
        boolean unknownHostException = false;
        boolean unexpectedEnd = false;
        String detailMessage = null;
        String ua = "Dalvik";
        Stopwatch dnsTimer = new Stopwatch().start();
        try {
            String hostToResolve = url.getHost();
            if (!TextUtils.isEmpty(hostToResolve)) {
                InetAddress[] addresses = this.mNetworkAgentInfo.network.getAllByName(hostToResolve);
                DhcpInfo di = this.mWifiManager.getDhcpInfo();
                StringBuffer sb = new StringBuffer();
                if (di != null) {
                    sb.append(di.gateway & 255).append(".");
                    sb.append((di.gateway >> 8) & 255).append(".");
                    sb.append((di.gateway >> 16) & 255).append(".");
                    sb.append((di.gateway >> 24) & 255);
                }
                Log.d(TAG, "gateway:" + sb.toString());
                if (addresses.length == 1 && sb.toString().equals(addresses[0].getHostAddress())) {
                    Log.d(TAG, "got response from gateway but not from server:" + url.toString());
                    dnsTimer.stop();
                    logValidationProbe(probeTimer.stop(), probeType, VoldResponseCode.OpFailedVolNotMounted);
                    captivePortalProbeResult = new CaptivePortalProbeResult(VoldResponseCode.OpFailedVolNotMounted, null, url.toString());
                    captivePortalProbeResult.mUnknownHostException = true;
                    TrafficStats.setThreadStatsTag(oldTag);
                    return captivePortalProbeResult;
                }
            }
            if (shallAccountForDupGateway()) {
                httpURLConnection = (HttpURLConnection) this.mNetwork.openConnectionWithoutConnectionPool(url, Proxy.NO_PROXY);
            } else {
                httpURLConnection = (HttpURLConnection) this.mNetwork.openConnection(url);
            }
            httpURLConnection.setInstanceFollowRedirects(probeType == 3);
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setUseCaches(false);
            if (this.mCaptivePortalUserAgent != null && this.mSetUserAgent) {
                httpURLConnection.setRequestProperty("User-Agent", this.mCaptivePortalUserAgent);
                ua = this.mCaptivePortalUserAgent.substring(0, this.mCaptivePortalUserAgent.length() <= 10 ? this.mCaptivePortalUserAgent.length() : 10);
            }
            String requestHeader = httpURLConnection.getRequestProperties().toString();
            long requestTimestamp = SystemClock.elapsedRealtime();
            httpResponseCode = httpURLConnection.getResponseCode();
            redirectUrl = httpURLConnection.getHeaderField("location");
            validationLog(probeType, url, "time=" + (SystemClock.elapsedRealtime() - requestTimestamp) + "ms" + " ret=" + httpResponseCode + " request=" + requestHeader + " headers=" + httpURLConnection.getHeaderFields());
            if (httpResponseCode == 200) {
                if (probeType == 3) {
                    validationLog(probeType, url, "PAC fetch 200 response interpreted as 204 response.");
                    httpResponseCode = 204;
                } else if (httpURLConnection.getContentLengthLong() == 0) {
                    validationLog(probeType, url, "200 response with Content-length=0 interpreted as 204 response.");
                    httpResponseCode = 204;
                } else if (httpURLConnection.getHeaderField("Connection") != null && httpURLConnection.getHeaderField("Connection").equalsIgnoreCase("Keep-Alive")) {
                    Log.d(TAG, "response 200 Connection - Alive.");
                    httpResponseCode = 204;
                } else if (httpURLConnection.getContentLengthLong() == -1 && httpURLConnection.getInputStream().read() == -1) {
                    validationLog(probeType, url, "Empty 200 response interpreted as 204 response.");
                    httpResponseCode = 204;
                }
            }
            String contentType = httpURLConnection.getContentType();
            if (contentType == null) {
                log("contentType is null, httpResponseCode = " + httpResponseCode);
            } else if (contentType.contains("text/html")) {
                String line = new BufferedReader(new InputStreamReader((InputStream) httpURLConnection.getContent())).readLine();
                if (line != null && httpResponseCode == 200 && line.contains("Success")) {
                    httpResponseCode = 204;
                    log("Internet detected!");
                }
            }
            if (httpResponseCode >= 300 && httpResponseCode <= 399 && redirectUrl != null && inSpecialUrlList(redirectUrl)) {
                Log.d(TAG, "response 302 with special redirect url.");
                httpResponseCode = 204;
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
        } catch (UnknownHostException e) {
            if (dnsTimer.stop() < 300) {
                httpResponseCode = 444;
                log("DNS UnknownHostException immediately, Probably route not ready!");
            } else {
                unknownHostException = true;
            }
            validationLog("Probably not a portal: exception " + e);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
        } catch (NoRouteToHostException e2) {
            if (dnsTimer.stop() < 300) {
                httpResponseCode = 444;
                log("NoRouteToHostException, Probably route not ready!");
            }
            validationLog("Probably not a portal: exception " + e2);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
        } catch (IOException e3) {
            detailMessage = e3.getMessage();
            String unExpectedMsg = "unexpected end of stream on ";
            if (this.mWifiRomUpdateForNet != null) {
                unExpectedMsg = this.mWifiRomUpdateForNet.getValue(WifiRomUpdateHelper.NETWORK_UNEXPECTED_IO_MSG, "unexpected end of stream on ");
            }
            if (detailMessage != null && detailMessage.startsWith(unExpectedMsg)) {
                unexpectedEnd = true;
            }
            validationLog(probeType, url, "Probe failed with exception " + e3);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
        } catch (IllegalArgumentException e4) {
            log("this is not right: exception " + e4);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
        } catch (RuntimeException runtimeException) {
            log("this is not right: exception " + runtimeException);
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            TrafficStats.setThreadStatsTag(oldTag);
            throw th;
        }
        dnsTimer.stop();
        logValidationProbe(probeTimer.stop(), probeType, httpResponseCode);
        captivePortalProbeResult = new CaptivePortalProbeResult(httpResponseCode, redirectUrl, url.toString());
        if (unknownHostException) {
            captivePortalProbeResult.mUnknownHostException = unknownHostException;
        }
        if (unexpectedEnd) {
            captivePortalProbeResult.mUnexpectedEnd = true;
        }
        if (detailMessage != null) {
            captivePortalProbeResult.mDetailMessage = detailMessage;
        }
        captivePortalProbeResult.mUA = ua;
        if (this.mWifiRomUpdateForNet != null && this.mWifiRomUpdateForNet.getBooleanValue(WifiRomUpdateHelper.NETWORK_COLLECT_CAPTIVERESULT, true)) {
            addCaptiveResult(captivePortalProbeResult);
        }
        return captivePortalProbeResult;
    }

    private CaptivePortalProbeResult sendOPPOParallelHttpProbes(ProxyInfo proxy, URL httpsUrl, URL httpUrl) {
        URL fallbackUrl;
        CountDownLatch latch = new CountDownLatch(3);
        if (isNotChineseOperator()) {
            fallbackUrl = makeURL(DEFAULT_HTTP_URL);
        } else {
            List<String> fallbackServers = getFallbackServers();
            Collections.shuffle(fallbackServers);
            fallbackUrl = makeURL((String) fallbackServers.get(0));
        }
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
            if (httpProbe1Result.mUnknownHostException && httpProbe2Result.mUnknownHostException && httpProbe3Result.mUnknownHostException) {
                httpProbe1Result.mUnknownHostException = true;
            } else {
                httpProbe1Result.mUnknownHostException = false;
            }
            if (httpProbe2Result.mUnexpectedEnd || httpProbe3Result.mUnexpectedEnd) {
                httpProbe1Result.mUnexpectedEnd = true;
            }
            return httpProbe1Result;
        } catch (InterruptedException e) {
            log("Error: probe wait interrupted!");
            return CaptivePortalProbeResult.FAILED;
        }
    }

    private CaptivePortalProbeResult sendParallelHttpProbes(ProxyInfo proxy, URL httpsUrl, URL httpUrl) {
        CountDownLatch latch = new CountDownLatch(2);
        AnonymousClass2ProbeThread httpsProbe = new AnonymousClass2ProbeThread(true, proxy, httpsUrl, httpUrl, latch);
        AnonymousClass2ProbeThread httpProbe = new AnonymousClass2ProbeThread(false, proxy, httpsUrl, httpUrl, latch);
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
            URL fallbackUrl = nextFallbackUrl();
            if (fallbackUrl != null) {
                CaptivePortalProbeResult result = sendHttpProbe(fallbackUrl, 4);
                if (result.isPortal()) {
                    return result;
                }
            }
            try {
                httpProbe.join();
                if (httpProbe.result().isPortal()) {
                    return httpProbe.result();
                }
                httpsProbe.join();
                return httpsProbe.result();
            } catch (InterruptedException e) {
                validationLog("Error: http or https probe wait interrupted!");
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
                                if (numRegisteredCellInfo <= 1) {
                                    if (cellInfo instanceof CellInfoCdma) {
                                        latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoCdma) cellInfo).getCellIdentity());
                                    } else if (cellInfo instanceof CellInfoGsm) {
                                        latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoGsm) cellInfo).getCellIdentity());
                                    } else if (cellInfo instanceof CellInfoLte) {
                                        latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoLte) cellInfo).getCellIdentity());
                                    } else if (cellInfo instanceof CellInfoWcdma) {
                                        latencyBroadcast.putExtra(EXTRA_CELL_ID, ((CellInfoWcdma) cellInfo).getCellIdentity());
                                    } else {
                                        return;
                                    }
                                }
                                return;
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

    private int networkEventType(ValidationStage s, EvaluationResult r) {
        if (s.isFirstValidation) {
            if (r.isValidated) {
                return 8;
            }
            return 10;
        } else if (r.isValidated) {
            return 9;
        } else {
            return 11;
        }
    }

    private void maybeLogEvaluationResult(int evtype) {
        if (this.mEvaluationTimer.isRunning()) {
            this.mMetricsLog.log(new NetworkEvent(this.mNetId, evtype, this.mEvaluationTimer.stop()));
            this.mEvaluationTimer.reset();
        }
    }

    private void logValidationProbe(long durationMs, int probeType, int probeResult) {
        int[] transports = this.mNetworkAgentInfo.networkCapabilities.getTransportTypes();
        boolean isFirstValidation = validationStage().isFirstValidation;
        ValidationProbeEvent ev = new ValidationProbeEvent();
        ev.probeType = ValidationProbeEvent.makeProbeType(probeType, isFirstValidation);
        ev.returnCode = probeResult;
        ev.durationMs = durationMs;
        this.mMetricsLog.log(this.mNetId, transports, ev);
    }

    private boolean isExpRom() {
        return SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN") ^ 1;
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
        int i = 1;
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
        Global.putString(this.mContext.getContentResolver(), KEY_NETWORK_MONITOR_SSID, this.mNetworkAgentInfo.networkInfo.getExtraInfo());
        ContentResolver contentResolver = this.mContext.getContentResolver();
        String str = KEY_NETWORK_MONITOR_AVAILABLE;
        if (!isAvailable) {
            i = 0;
        }
        Global.putInt(contentResolver, str, i);
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

    private List<String> getPublicHttpsServers() {
        List<String> defaultServers = Arrays.asList(this.mPublicHttpsServers);
        String value = getRomUpdateValue(WifiRomUpdateHelper.NETWORK_PUBLIC_HTTPS_SERVERS_URL, null);
        if (value == null) {
            log("Fail to getRomUpdateValue, using default servers!");
            return defaultServers;
        }
        log("getPublicHttpsServers, updated servers: " + value);
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

    private List<String> getFallbackServers() {
        List<String> defaultServers = Arrays.asList(this.mFallbackHttpServers);
        String value = getRomUpdateValue(WifiRomUpdateHelper.NETWORK_FALLBACK_HTTP_SERVERS_URL, null);
        if (value == null) {
            return defaultServers;
        }
        List<String> updatedServers = new ArrayList();
        for (String name : value.split(",")) {
            updatedServers.add(name);
        }
        if (updatedServers.size() >= 2) {
            return updatedServers;
        }
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

    private List<String> tryUpdateCaptiveServer() {
        List<String> updatedServers = new ArrayList();
        String updateCaptiveServer1 = getRomUpdateValue(WifiRomUpdateHelper.NETWORK_CAPTIVE_SERVER_FIRST_URL, null);
        if (updateCaptiveServer1 != null) {
            updatedServers.add(updateCaptiveServer1);
        } else {
            updatedServers.add(captiveServer1);
        }
        String updateCaptiveServer2 = getRomUpdateValue(WifiRomUpdateHelper.NETWORK_CAPTIVE_SERVER_SECOND_URL, null);
        if (updateCaptiveServer2 != null) {
            updatedServers.add(updateCaptiveServer2);
        } else {
            updatedServers.add(captiveServer2);
        }
        return updatedServers;
    }

    private boolean isNotChineseOperator() {
        ColorOSTelephonyManager colorOSTelephonyManager = ColorOSTelephonyManager.getDefault(this.mContext);
        String mcc = colorOSTelephonyManager.getNetworkOperatorGemini(colorOSTelephonyManager.colorGetDataSubscription());
        if (TextUtils.isEmpty(mcc)) {
            return isExpRom();
        }
        return mcc.startsWith("460") ^ 1;
    }

    private boolean isNeedValidateOperator() {
        ColorOSTelephonyManager colorOSTelephonyManager = ColorOSTelephonyManager.getDefault(this.mContext);
        String mcc = colorOSTelephonyManager.getNetworkOperatorGemini(colorOSTelephonyManager.colorGetDataSubscription());
        if (!TextUtils.isEmpty(mcc)) {
            for (String startsWith : this.mNeedValiOperator) {
                if (mcc.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shallAccountForDupGateway() {
        return this.mArpPeer != null ? this.mArpPeer.hasDupGateway() : false;
    }

    private void setPreConditionForGatewayCheck() {
        if (this.mNetworkAgentInfo != null && this.mNetworkAgentInfo.networkInfo != null && this.mNetworkAgentInfo.networkInfo.getType() == 1) {
            LinkProperties linkproperties = this.mNetworkAgentInfo.linkProperties;
            String interfaceName = linkproperties.getInterfaceName();
            Inet4Address ipv4address = null;
            Inet4Address ipv4gateway = null;
            for (RouteInfo routeInfo : linkproperties.getRoutes()) {
                if (routeInfo.hasGateway()) {
                    InetAddress gateway = routeInfo.getGateway();
                    if (gateway instanceof Inet4Address) {
                        ipv4gateway = (Inet4Address) gateway;
                        break;
                    }
                }
            }
            for (InetAddress address : linkproperties.getAddresses()) {
                if (address instanceof Inet4Address) {
                    ipv4address = (Inet4Address) address;
                    break;
                }
            }
            this.mIPv4Gateway = ipv4gateway;
            this.mIPv4Self = ipv4address;
            this.mInterfaceName = interfaceName;
        }
    }

    private boolean probeGateway() {
        return this.mArpPeer.doDupArp(this.mInterfaceName, this.mIPv4Self, this.mIPv4Gateway);
    }

    private boolean needToCheckGateway() {
        return (this.mArpPeer == null || this.mIPv4Gateway == null) ? false : true;
    }

    private void setDuplicateGatewayStatics() {
        if (this.mNetworkAgentInfo != null && this.mNetworkAgentInfo.networkInfo != null) {
            HashMap<String, String> map = new HashMap();
            String ssid = this.mNetworkAgentInfo.networkInfo.getExtraInfo();
            StringBuilder message = new StringBuilder();
            message.append(this.mNetworkAgentInfo.network).append(";");
            message.append(ssid).append(";");
            map.put(KEY_NETWORK, message.toString());
            OppoStatistics.onCommon(this.mContext, WIFI_STATISTIC_KEY, WIFI_DUPLICATE_GATEWAY, map, false);
        }
    }

    private void setCRLReadTimeout(int timeout) {
        if (this.mNetworkAgentInfo == null || this.mNetworkAgentInfo.networkInfo == null || this.mNetworkAgentInfo.networkInfo.getType() != 1) {
            Log.d(TAG, "do not set CRL read timeout for mobile");
            return;
        }
        Log.d(TAG, "set crl.read.timeout to : " + timeout);
        Security.setProperty("crl.read.timeout", Integer.toString(timeout));
    }

    private void addCaptiveResult(CaptivePortalProbeResult captiveResult) {
        synchronized (this.mCaptivePortalResults) {
            if (this.mCaptivePortalResults.size() < 6) {
                this.mCaptivePortalResults.add(captiveResult);
            }
        }
    }

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void triggerCaptiveResultBack() {
        if (this.mNetworkAgentInfo != null && this.mNetworkAgentInfo.networkInfo != null && this.mNetworkAgentInfo.networkInfo.getType() == 1 && this.mSSID != null) {
            Boolean sent = (Boolean) mSentCaptiveResult.get(this.mSSID);
            if ((sent != null && sent.booleanValue()) || this.mCaptivePortalResults.size() == 0) {
                log("triggerCaptiveResult, not necessary");
            } else if (mSentCaptiveResult.size() >= 1000) {
                log("triggerCaptiveResult, not necessary,have sent more than 1000");
            } else {
                HashMap<String, String> map = new HashMap();
                StringBuilder nbuf = new StringBuilder();
                nbuf.append(this.mNetworkAgentInfo.network).append(";");
                nbuf.append(this.mSSID).append(";");
                map.put(KEY_NETWORK, nbuf.toString());
                int i = 0;
                synchronized (this.mCaptivePortalResults) {
                    for (CaptivePortalProbeResult captiveResult : this.mCaptivePortalResults) {
                        StringBuilder cbuf = new StringBuilder();
                        cbuf.append(captiveResult.detectUrl).append(";");
                        cbuf.append(captiveResult.mHttpResponseCode).append(";");
                        cbuf.append(captiveResult.mUA).append(";");
                        if (captiveResult.mDetailMessage != null) {
                            cbuf.append(captiveResult.mDetailMessage).append(";");
                        }
                        map.put(KEY_CAPTIVE_RESULT + i, cbuf.toString());
                        i++;
                    }
                }
                OppoStatistics.onCommon(this.mContext, WIFI_STATISTIC_KEY, WIFI_CAPTIVE_RESULT, map, false);
                mSentCaptiveResult.put(this.mSSID, Boolean.TRUE);
            }
        }
    }

    private void clearCaptivePortalResults() {
        synchronized (this.mCaptivePortalResults) {
            this.mCaptivePortalResults.clear();
        }
    }
}
