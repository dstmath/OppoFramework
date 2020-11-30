package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.database.ContentObserver;
import android.hardware.wifi.V1_3.IWifiChip;
import android.net.DhcpInfo;
import android.net.DhcpResults;
import android.net.Network;
import android.net.NetworkUtils;
import android.net.Uri;
import android.net.wifi.IDppCallback;
import android.net.wifi.INetworkRequestMatchCallback;
import android.net.wifi.IOnWifiUsabilityStatsListener;
import android.net.wifi.ISoftApCallback;
import android.net.wifi.IStaStateCallback;
import android.net.wifi.ITrafficStateCallback;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiActivityEnergyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.net.wifi.WifiSsid;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.MutableInt;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.PowerProfile;
import com.android.internal.util.AsyncChannel;
import com.android.server.PswServiceFactory;
import com.android.server.location.interfaces.IPswLocationStatistics;
import com.android.server.wifi.LocalOnlyHotspotRequestInfo;
import com.android.server.wifi.WifiServiceImpl;
import com.android.server.wifi.hotspot2.PasspointProvider;
import com.android.server.wifi.util.ExternalCallbackTracker;
import com.android.server.wifi.util.GeneralUtil;
import com.android.server.wifi.util.WifiHandler;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import com.mediatek.cta.CtaManagerFactory;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WifiServiceImpl extends OppoWifiServiceImpl {
    public static final String ACTION_MTKLOGGER_STATE_CHANGED = "com.debug.loggerui.intent.action.LOG_STATE_CHANGED";
    private static final int BACKGROUND_IMPORTANCE_CUTOFF = 125;
    public static final String DEBUG_PROPERTY = "persist.sys.assert.panic";
    private static final long DEFAULT_SCAN_BACKGROUND_THROTTLE_INTERVAL_MS = 1800000;
    private static final int RUN_WITH_SCISSORS_TIMEOUT_MILLIS = 4000;
    private static final String TAG = "WifiService";
    private static final boolean VDBG = false;
    private static String sOperator = SystemProperties.get("ro.oppo.operator", "");
    private static boolean sOptEnabled = SystemProperties.getBoolean("ro.oppo.opt_enabled", false);
    private static String sRegion = SystemProperties.get("ro.oppo.regionmark", "");
    final ActiveModeWarden mActiveModeWarden;
    private final ActivityManager mActivityManager;
    private final AppOpsManager mAppOps;
    private AsyncChannelExternalClientHandler mAsyncChannelExternalClientHandler;
    final ClientModeImpl mClientModeImpl;
    @VisibleForTesting
    AsyncChannel mClientModeImplChannel;
    ClientModeImplHandler mClientModeImplHandler;
    private final Clock mClock;
    private final Context mContext;
    private final WifiCountryCode mCountryCode;
    private final DppManager mDppManager;
    private final FrameworkFacade mFacade;
    private final FrameworkFacade mFrameworkFacade;
    @GuardedBy({"mLocalOnlyHotspotRequests"})
    private final ConcurrentHashMap<String, Integer> mIfaceIpModes;
    boolean mInIdleMode;
    @GuardedBy({"mLocalOnlyHotspotRequests"})
    private WifiConfiguration mLocalOnlyHotspotConfig = null;
    @GuardedBy({"mLocalOnlyHotspotRequests"})
    private final HashMap<Integer, LocalOnlyHotspotRequestInfo> mLocalOnlyHotspotRequests;
    private WifiLog mLog;
    final OppoClientModeImpl2 mOppoClientModeImpl2;
    private OppoConnectivityTmobileParser mOppoConnectivityTmobileParser = null;
    private OppoConnectivityVodafoneParser mOppoConnectivityVodafoneParser = null;
    final OppoScanResultsProxy mOppoScanResultsProxy;
    private OppoWifiOperatorPresetApList mOppoWifiOperatorPresetApList = null;
    private final PowerManager mPowerManager;
    PowerProfile mPowerProfile;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass5 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.USER_REMOVED")) {
                WifiServiceImpl.this.mClientModeImpl.removeUserConfigs(intent.getIntExtra("android.intent.extra.user_handle", 0));
            } else if (action.equals("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")) {
                WifiServiceImpl.this.mClientModeImpl.sendBluetoothAdapterStateChange(intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", 0));
            } else if (action.equals("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED")) {
                WifiServiceImpl.this.mWifiController.sendMessage(155649, intent.getBooleanExtra("phoneinECMState", false) ? 1 : 0, 0);
            } else if (action.equals("android.intent.action.EMERGENCY_CALL_STATE_CHANGED")) {
                WifiServiceImpl.this.mWifiController.sendMessage(155662, intent.getBooleanExtra("phoneInEmergencyCall", false) ? 1 : 0, 0);
            } else if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                WifiServiceImpl.this.handleIdleModeChanged();
            }
        }
    };
    private final ExternalCallbackTracker<ISoftApCallback> mRegisteredSoftApCallbacks;
    boolean mScanPending;
    final ScanRequestProxy mScanRequestProxy;
    final WifiSettingsStore mSettingsStore;
    private int mSoftApNumClients = 0;
    private int mSoftApState = 11;
    private final UserManager mUserManager;
    private boolean mVerboseLoggingEnabled = false;
    private int mVerboseLoggingLevel = 0;
    private WifiApConfigStore mWifiApConfigStore;
    private int mWifiApState = 11;
    private final WifiBackupRestore mWifiBackupRestore;
    private WifiController mWifiController;
    private final WifiInjector mWifiInjector;
    private final WifiLockManager mWifiLockManager;
    private final WifiMetrics mWifiMetrics;
    private final WifiMulticastLockManager mWifiMulticastLockManager;
    private final WifiNetworkSuggestionsManager mWifiNetworkSuggestionsManager;
    private WifiPermissionsUtil mWifiPermissionsUtil;
    private WifiStaStateNotifier mWifiStaStateNotifier;
    private WifiTrafficPoller mWifiTrafficPoller;
    private int scanRequestCounter = 0;

    public final class LocalOnlyRequestorCallback implements LocalOnlyHotspotRequestInfo.RequestingApplicationDeathCallback {
        public LocalOnlyRequestorCallback() {
        }

        @Override // com.android.server.wifi.LocalOnlyHotspotRequestInfo.RequestingApplicationDeathCallback
        public void onLocalOnlyHotspotRequestorDeath(LocalOnlyHotspotRequestInfo requestor) {
            WifiServiceImpl.this.unregisterCallingAppAndStopLocalOnlyHotspot(requestor);
        }
    }

    private class AsyncChannelExternalClientHandler extends WifiHandler {
        AsyncChannelExternalClientHandler(String tag, Looper looper) {
            super(tag, looper);
        }

        @Override // com.android.server.wifi.util.WifiHandler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 69633:
                    WifiServiceImpl.this.mFrameworkFacade.makeWifiAsyncChannel(WifiServiceImpl.TAG).connect(WifiServiceImpl.this.mContext, this, msg.replyTo);
                    return;
                case 151553:
                    if (checkPrivilegedPermissionsAndReplyIfNotAuthorized(msg, 151554)) {
                        WifiConfiguration config = (WifiConfiguration) msg.obj;
                        int networkId = msg.arg1;
                        Slog.d(WifiServiceImpl.TAG, "CONNECT  nid=" + Integer.toString(networkId) + " config=" + config + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        if (config != null) {
                            WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                            if (msg.sendingUid == 1000 && !WifiServiceImpl.this.mClientModeImpl.isNetworkConnected(config.networkId)) {
                                WifiServiceImpl.this.mClientModeImpl.notifyDisconnectPktName("com.coloros.wirelesssettings", msg.sendingUid, 0);
                                return;
                            }
                            return;
                        } else if (config != null || networkId == -1) {
                            Slog.e(WifiServiceImpl.TAG, "AsyncChannelExternalClientHandler.handleMessage ignoring invalid msg=" + msg);
                            replyFailed(msg, 151554, 8);
                            return;
                        } else {
                            WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                            if (msg.sendingUid == 1000 && !WifiServiceImpl.this.mClientModeImpl.isNetworkConnected(networkId)) {
                                WifiServiceImpl.this.mClientModeImpl.notifyDisconnectPktName("com.coloros.wirelesssettings", msg.sendingUid, 0);
                                return;
                            }
                            return;
                        }
                    } else {
                        return;
                    }
                case 151556:
                    if (checkPrivilegedPermissionsAndReplyIfNotAuthorized(msg, 151557)) {
                        WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                        if (msg.sendingUid != 1000) {
                            return;
                        }
                        if (WifiServiceImpl.this.mClientModeImpl.isNetworkConnected(msg.arg1) || WifiServiceImpl.this.mClientModeImpl.isNetworkConnecting(msg.arg1)) {
                            WifiServiceImpl.this.mClientModeImpl.notifyDisconnectPktName("com.coloros.wirelesssettings", msg.sendingUid, 0);
                            return;
                        }
                        return;
                    }
                    return;
                case 151559:
                    if (checkPrivilegedPermissionsAndReplyIfNotAuthorized(msg, 151560)) {
                        WifiConfiguration config2 = (WifiConfiguration) msg.obj;
                        int networkId2 = msg.arg1;
                        Slog.d(WifiServiceImpl.TAG, "SAVE nid=" + Integer.toString(networkId2) + " config=" + config2 + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        if (config2 != null) {
                            WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                            return;
                        }
                        Slog.e(WifiServiceImpl.TAG, "AsyncChannelExternalClientHandler.handleMessage ignoring invalid msg=" + msg);
                        replyFailed(msg, 151560, 8);
                        return;
                    }
                    return;
                case 151562:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151564)) {
                        WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                        return;
                    }
                    return;
                case 151566:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151567)) {
                        WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                        return;
                    }
                    return;
                case 151569:
                    if (checkPrivilegedPermissionsAndReplyIfNotAuthorized(msg, 151570)) {
                        WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                        if (msg.sendingUid != 1000) {
                            return;
                        }
                        if (WifiServiceImpl.this.mClientModeImpl.isNetworkConnected(msg.arg1) || WifiServiceImpl.this.mClientModeImpl.isNetworkConnecting(msg.arg1)) {
                            WifiServiceImpl.this.mClientModeImpl.notifyDisconnectPktName("com.coloros.wirelesssettings", msg.sendingUid, 0);
                            return;
                        }
                        return;
                    }
                    return;
                case 151572:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151574)) {
                        WifiServiceImpl.this.mClientModeImpl.sendMessage(Message.obtain(msg));
                        return;
                    }
                    return;
                default:
                    Slog.d(WifiServiceImpl.TAG, "AsyncChannelExternalClientHandler.handleMessage ignoring msg=" + msg);
                    return;
            }
        }

        private boolean checkChangePermissionAndReplyIfNotAuthorized(Message msg, int replyWhat) {
            if (WifiServiceImpl.this.mWifiPermissionsUtil.checkChangePermission(msg.sendingUid)) {
                return true;
            }
            Slog.e(WifiServiceImpl.TAG, "AsyncChannelExternalClientHandler.handleMessage ignoring unauthorized msg=" + msg);
            replyFailed(msg, replyWhat, 9);
            return false;
        }

        private boolean checkPrivilegedPermissionsAndReplyIfNotAuthorized(Message msg, int replyWhat) {
            if (WifiServiceImpl.this.isPrivileged(-1, msg.sendingUid)) {
                return true;
            }
            Slog.e(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring unauthorized msg=" + msg);
            replyFailed(msg, replyWhat, 9);
            return false;
        }

        private void replyFailed(Message msg, int what, int why) {
            if (msg.replyTo != null) {
                Message reply = Message.obtain();
                reply.what = what;
                reply.arg1 = why;
                try {
                    msg.replyTo.send(reply);
                } catch (RemoteException e) {
                }
            }
        }
    }

    private class ClientModeImplHandler extends WifiHandler {
        private AsyncChannel mCmiChannel;

        ClientModeImplHandler(String tag, Looper looper, AsyncChannel asyncChannel) {
            super(tag, looper);
            this.mCmiChannel = asyncChannel;
            this.mCmiChannel.connect(WifiServiceImpl.this.mContext, this, WifiServiceImpl.this.mClientModeImpl.getHandler());
        }

        @Override // com.android.server.wifi.util.WifiHandler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i != 69632) {
                if (i != 69636) {
                    Slog.d(WifiServiceImpl.TAG, "ClientModeImplHandler.handleMessage ignoring msg=" + msg);
                    return;
                }
                Slog.e(WifiServiceImpl.TAG, "ClientModeImpl channel lost, msg.arg1 =" + msg.arg1);
                WifiServiceImpl wifiServiceImpl = WifiServiceImpl.this;
                wifiServiceImpl.mClientModeImplChannel = null;
                this.mCmiChannel.connect(wifiServiceImpl.mContext, this, WifiServiceImpl.this.mClientModeImpl.getHandler());
            } else if (msg.arg1 == 0) {
                WifiServiceImpl.this.mClientModeImplChannel = this.mCmiChannel;
            } else {
                Slog.e(WifiServiceImpl.TAG, "ClientModeImpl connection failure, error=" + msg.arg1);
                WifiServiceImpl.this.mClientModeImplChannel = null;
            }
        }
    }

    public WifiServiceImpl(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        super(context, wifiInjector, asyncChannel);
        this.mContext = context;
        this.mWifiInjector = wifiInjector;
        this.mClock = wifiInjector.getClock();
        this.mFacade = this.mWifiInjector.getFrameworkFacade();
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mWifiTrafficPoller = this.mWifiInjector.getWifiTrafficPoller();
        this.mUserManager = this.mWifiInjector.getUserManager();
        this.mCountryCode = this.mWifiInjector.getWifiCountryCode();
        this.mClientModeImpl = this.mWifiInjector.getClientModeImpl();
        this.mActiveModeWarden = this.mWifiInjector.getActiveModeWarden();
        this.mClientModeImpl.enableRssiPolling(true);
        this.mScanRequestProxy = this.mWifiInjector.getScanRequestProxy();
        this.mOppoScanResultsProxy = this.mWifiInjector.getOppoScanResultsProxy();
        this.mSettingsStore = this.mWifiInjector.getWifiSettingsStore();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mWifiLockManager = this.mWifiInjector.getWifiLockManager();
        this.mWifiMulticastLockManager = this.mWifiInjector.getWifiMulticastLockManager();
        HandlerThread wifiServiceHandlerThread = this.mWifiInjector.getWifiServiceHandlerThread();
        this.mAsyncChannelExternalClientHandler = new AsyncChannelExternalClientHandler(TAG, wifiServiceHandlerThread.getLooper());
        this.mClientModeImplHandler = new ClientModeImplHandler(TAG, wifiServiceHandlerThread.getLooper(), asyncChannel);
        this.mWifiController = this.mWifiInjector.getWifiController();
        this.mWifiBackupRestore = this.mWifiInjector.getWifiBackupRestore();
        this.mWifiApConfigStore = this.mWifiInjector.getWifiApConfigStore();
        this.mWifiPermissionsUtil = this.mWifiInjector.getWifiPermissionsUtil();
        this.mLog = this.mWifiInjector.makeLog(TAG);
        this.mFrameworkFacade = wifiInjector.getFrameworkFacade();
        this.mIfaceIpModes = new ConcurrentHashMap<>();
        this.mLocalOnlyHotspotRequests = new HashMap<>();
        this.mOppoClientModeImpl2 = this.mWifiInjector.getOppoClientModeImpl2();
        this.mVerboseLoggingEnabled = SystemProperties.getBoolean(DEBUG_PROPERTY, false);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                WifiServiceImpl.this.mVerboseLoggingEnabled = SystemProperties.getBoolean(WifiServiceImpl.DEBUG_PROPERTY, false);
                Slog.d(WifiServiceImpl.TAG, "mVerboseLoggingEnabled change " + WifiServiceImpl.this.mVerboseLoggingEnabled);
                WifiServiceImpl wifiServiceImpl = WifiServiceImpl.this;
                wifiServiceImpl.enableVerboseLogging(wifiServiceImpl.mVerboseLoggingEnabled ? 1 : 0);
            }
        }, new IntentFilter(ACTION_MTKLOGGER_STATE_CHANGED));
        enableVerboseLoggingInternal(getVerboseLoggingLevel());
        if (getVerboseLoggingLevel() > 0) {
            this.mWifiInjector.getPropertyService().set("persist.vendor.logmuch", "false");
        }
        this.mRegisteredSoftApCallbacks = new ExternalCallbackTracker<>(this.mClientModeImplHandler);
        this.mWifiInjector.getActiveModeWarden().registerSoftApCallback(new SoftApCallbackImpl());
        this.mPowerProfile = this.mWifiInjector.getPowerProfile();
        this.mWifiNetworkSuggestionsManager = this.mWifiInjector.getWifiNetworkSuggestionsManager();
        this.mDppManager = this.mWifiInjector.getDppManager();
        this.mWifiStaStateNotifier = this.mWifiInjector.getWifiStaStateNotifier();
        Slog.i(TAG, "WifiServiceImpl, sOperator:" + sOperator + ", sOptEnabled:" + sOptEnabled);
        String str = sOperator;
        if ((str != null && !str.equals("")) || sOptEnabled) {
            Slog.i(TAG, "Start OppoWifiOperatorPresetApList");
            this.mOppoWifiOperatorPresetApList = OppoWifiOperatorPresetApList.getInstance(this.mContext, "");
        }
        if (!sOptEnabled) {
            return;
        }
        if ("VODAFONE_EEA".equals(sOperator)) {
            Slog.i(TAG, "Start OppoConnectivityVodafoneParser");
            this.mOppoConnectivityVodafoneParser = OppoConnectivityVodafoneParser.getInstance(this.mContext, "");
        } else if ("".equals(sOperator) && "EUEX".equals(sRegion)) {
            Slog.i(TAG, "Start OppoConnectivityTmobileParser");
            this.mOppoConnectivityTmobileParser = OppoConnectivityTmobileParser.getInstance(this.mContext);
        }
    }

    @VisibleForTesting
    public void setWifiHandlerLogForTest(WifiLog log) {
        this.mAsyncChannelExternalClientHandler.setWifiLog(log);
    }

    public void checkAndStartWifi() {
        if (this.mFrameworkFacade.inStorageManagerCryptKeeperBounce()) {
            Log.d(TAG, "Device still encrypted. Need to restart SystemServer.  Do not start wifi.");
            return;
        }
        this.mClientModeImpl.initRomupdateHelperBroadcastReceiver();
        boolean wifiEnabled = this.mSettingsStore.isWifiToggleEnabled();
        StringBuilder sb = new StringBuilder();
        sb.append("WifiService starting up with Wi-Fi ");
        sb.append(wifiEnabled ? "enabled" : "disabled");
        Slog.i(TAG, sb.toString());
        if (SystemProperties.getInt("persist.sys.wifi_policy", 2) == 5) {
            wifiEnabled = true;
            Slog.i(TAG, "wifi always enable");
        }
        registerForScanModeChange();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                if (WifiServiceImpl.this.mSettingsStore.handleAirplaneModeToggled()) {
                    WifiServiceImpl.this.mWifiController.sendMessage(155657);
                }
                if (WifiServiceImpl.this.mSettingsStore.isAirplaneModeOn()) {
                    Log.d(WifiServiceImpl.TAG, "resetting country code because Airplane mode is ON");
                    WifiServiceImpl.this.mCountryCode.airplaneModeEnabled();
                }
            }
        }, new IntentFilter("android.intent.action.AIRPLANE_MODE"));
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                String state = intent.getStringExtra("ss");
                int simSlot = intent.getIntExtra("phone", -1);
                if (simSlot == -1) {
                    simSlot = 0;
                }
                if ("ABSENT".equals(state)) {
                    Log.d(WifiServiceImpl.TAG, "resetting networks because SIM" + simSlot + " was removed");
                    WifiServiceImpl.this.mClientModeImpl.resetSimAuthNetworks(false, simSlot);
                } else if ("LOADED".equals(state)) {
                    Log.d(WifiServiceImpl.TAG, "resetting networks because SIM" + simSlot + " was loaded");
                    WifiServiceImpl.this.mClientModeImpl.resetSimAuthNetworks(true, simSlot);
                }
            }
        }, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        if (this.mWifiInjector.getOppoWifiSharingManager().isWifiSharingSupported()) {
            filter.addAction("oppo.intent.action.wifi.WIFI_SHARING_STATE_CHANGED");
        }
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass4 */

            public void onReceive(Context context, Intent intent) {
                WifiServiceImpl.this.handleWifiApStateChange(intent.getIntExtra("wifi_state", 11), intent.getIntExtra("previous_wifi_state", 11), intent.getIntExtra("wifi_ap_error_code", -1), intent.getStringExtra("wifi_ap_interface_name"), intent.getIntExtra("wifi_ap_mode", -1));
            }
        }, filter);
        registerForBroadcasts();
        this.mInIdleMode = this.mPowerManager.isDeviceIdleMode();
        if (!this.mClientModeImpl.syncInitialize(this.mClientModeImplChannel)) {
            Log.wtf(TAG, "Failed to initialize ClientModeImpl");
        }
        this.mWifiController.start();
        if (wifiEnabled) {
            setWifiEnabled(this.mContext.getPackageName(), wifiEnabled);
        }
        Settings.Global.putInt(this.mContext.getContentResolver(), "tether_enable_legacy_dhcp_server", 1);
        Log.d(TAG, "checkAndStartWifi done.");
    }

    public void handleBootCompleted() {
        OppoWifiOperatorPresetApList oppoWifiOperatorPresetApList;
        Log.d(TAG, "Handle boot completed");
        this.mClientModeImpl.handleBootCompleted();
        OppoClientModeImpl2 oppoClientModeImpl2 = this.mOppoClientModeImpl2;
        if (oppoClientModeImpl2 != null) {
            oppoClientModeImpl2.handleBootCompleted();
        } else {
            Log.e(TAG, "handleBootCompleted mOppoClientModeImpl2==null!!!");
        }
        String str = sOperator;
        if (((str != null && !str.equals("")) || sOptEnabled) && (oppoWifiOperatorPresetApList = this.mOppoWifiOperatorPresetApList) != null) {
            oppoWifiOperatorPresetApList.handleBootCompleted();
        }
    }

    public void handleUserSwitch(int userId) {
        Log.d(TAG, "Handle user switch " + userId);
        this.mClientModeImpl.handleUserSwitch(userId);
    }

    public void handleUserUnlock(int userId) {
        Log.d(TAG, "Handle user unlock " + userId);
        this.mClientModeImpl.handleUserUnlock(userId);
    }

    public void handleUserStop(int userId) {
        Log.d(TAG, "Handle user stop " + userId);
        this.mClientModeImpl.handleUserStop(userId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r11.mWifiPermissionsUtil.enforceCanAccessScanResults(r12, r0);
        r4 = new com.android.server.wifi.util.GeneralUtil.Mutable<>();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x006f, code lost:
        if (r11.mWifiInjector.getClientModeImplHandler().runWithScissors(new com.android.server.wifi.$$Lambda$WifiServiceImpl$71KWGZ9o3U1lf_2vP7tmY9cz4qQ(r11, r4, r0, r12), 4000) != false) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0071, code lost:
        android.util.Log.e(com.android.server.wifi.WifiServiceImpl.TAG, "Failed to post runnable to start scan");
        sendFailedScanBroadcast();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x007f, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0088, code lost:
        if (r4.value.booleanValue() != false) goto L_0x0096;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x008a, code lost:
        android.util.Log.e(com.android.server.wifi.WifiServiceImpl.TAG, "Failed to start scan");
        android.os.Binder.restoreCallingIdentity(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0095, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0096, code lost:
        android.os.Binder.restoreCallingIdentity(r2);
        com.android.server.PswServiceFactory.getInstance().getFeature(com.android.server.location.interfaces.IPswLocationStatistics.DEFAULT, new java.lang.Object[0]).recordNlpScanWifiSucceed(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00ab, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00ac, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00ae, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        android.util.Slog.e(com.android.server.wifi.WifiServiceImpl.TAG, "Permission violation - startScan not allowed for uid=" + r0 + ", packageName=" + r12 + ", reason=" + r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00d9, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00da, code lost:
        android.os.Binder.restoreCallingIdentity(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00dd, code lost:
        throw r1;
     */
    public boolean startScan(String packageName) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(69, Binder.getCallingUid());
        PswServiceFactory.getInstance().getFeature(IPswLocationStatistics.DEFAULT, new Object[0]).recordNlpScanWifiTotal(packageName);
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        int callingUid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        this.mLog.info("startScan uid=%").c((long) callingUid).flush();
        synchronized (this) {
            if (this.mInIdleMode) {
                sendFailedScanBroadcast();
                this.mScanPending = true;
                return false;
            }
        }
    }

    public /* synthetic */ void lambda$startScan$0$WifiServiceImpl(GeneralUtil.Mutable scanSuccess, int callingUid, String packageName) {
        scanSuccess.value = (E) Boolean.valueOf(this.mClientModeImpl.startScan(callingUid, packageName));
    }

    private void sendFailedScanBroadcast() {
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
            intent.addFlags(67108864);
            intent.putExtra("resultsUpdated", false);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    public String getCurrentNetworkWpsNfcConfigurationToken() {
        enforceConnectivityInternalPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getCurrentNetworkWpsNfcConfigurationToken uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mClientModeImpl.syncGetCurrentNetworkWpsNfcConfigurationToken();
    }

    /* access modifiers changed from: package-private */
    public void handleIdleModeChanged() {
        boolean doScan = false;
        synchronized (this) {
            boolean idle = this.mPowerManager.isDeviceIdleMode();
            if (this.mInIdleMode != idle) {
                this.mInIdleMode = idle;
                if (!idle && this.mScanPending) {
                    this.mScanPending = false;
                    doScan = true;
                }
            }
        }
        if (doScan) {
            startScan(this.mContext.getOpPackageName());
        }
    }

    private boolean checkNetworkSettingsPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_SETTINGS", pid, uid) == 0;
    }

    private boolean checkNetworkSetupWizardPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_SETUP_WIZARD", pid, uid) == 0;
    }

    private boolean checkNetworkStackPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_STACK", pid, uid) == 0;
    }

    private boolean checkNetworkManagedProvisioningPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_MANAGED_PROVISIONING", pid, uid) == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPrivileged(int pid, int uid) {
        return checkNetworkSettingsPermission(pid, uid) || checkNetworkSetupWizardPermission(pid, uid) || checkNetworkStackPermission(pid, uid) || checkNetworkManagedProvisioningPermission(pid, uid);
    }

    private boolean isSettingsOrSuw(int pid, int uid) {
        return checkNetworkSettingsPermission(pid, uid) || checkNetworkSetupWizardPermission(pid, uid);
    }

    private boolean isSystem(String packageName) {
        int callingUid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        boolean z = false;
        try {
            ApplicationInfo info = this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 0, UserHandle.getUserId(callingUid));
            if (info.isSystemApp() || info.isUpdatedSystemApp()) {
                z = true;
            }
            return z;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean isDeviceOrProfileOwner(int uid) {
        DevicePolicyManagerInternal dpmi = this.mWifiInjector.getWifiPermissionsWrapper().getDevicePolicyManagerInternal();
        if (dpmi == null) {
            return false;
        }
        if (dpmi.isActiveAdminWithPolicy(uid, -2) || dpmi.isActiveAdminWithPolicy(uid, -1)) {
            return true;
        }
        return false;
    }

    private void enforceNetworkSettingsPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_SETTINGS", TAG);
    }

    private void enforceNetworkStackPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.NETWORK_STACK", TAG);
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", TAG);
    }

    private int enforceChangePermission(String callingPackage) {
        this.mAppOps.checkPackage(Binder.getCallingUid(), callingPackage);
        if (checkNetworkSettingsPermission(Binder.getCallingPid(), Binder.getCallingUid())) {
            return 0;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", TAG);
        return this.mAppOps.noteOp("android:change_wifi_state", Binder.getCallingUid(), callingPackage);
    }

    private void enforceLocationHardwarePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.LOCATION_HARDWARE", "LocationHardware");
    }

    private void enforceReadCredentialPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.READ_WIFI_CREDENTIAL", TAG);
    }

    private void enforceWorkSourcePermission() {
        this.mContext.enforceCallingPermission("android.permission.UPDATE_DEVICE_STATS", TAG);
    }

    private void enforceMulticastChangePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_MULTICAST_STATE", TAG);
    }

    private void enforceConnectivityInternalPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "ConnectivityService");
    }

    private void enforceLocationPermission(String pkgName, int uid) {
        this.mWifiPermissionsUtil.enforceLocationPermission(pkgName, uid);
    }

    private boolean isTargetSdkLessThanQOrPrivileged(String packageName, int pid, int uid) {
        return this.mWifiPermissionsUtil.isTargetSdkLessThan(packageName, 29) || isPrivileged(pid, uid) || isDeviceOrProfileOwner(uid) || isSystem(packageName) || this.mWifiPermissionsUtil.checkSystemAlertWindowPermission(uid, packageName);
    }

    public synchronized boolean setWifiEnabled(String packageName, boolean enable) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(62, Binder.getCallingUid());
        if (SystemProperties.getBoolean("persist.sys.cta", false)) {
            Log.d("ctaifs", "set wifi enable " + enable + " ,calling package is " + packageName);
        }
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        int wifiPolicy = SystemProperties.getInt("persist.sys.wifi_policy", 2);
        if (wifiPolicy == 0) {
            Slog.d(TAG, "disable wifi return false for spaying");
            this.mSettingsStore.handleWifiToggled(false);
            return false;
        } else if (wifiPolicy == 5 && getWifiEnabledState() == 3) {
            Slog.d(TAG, "always enable wifi return false for spaying");
            return false;
        } else {
            boolean isPrivileged = isPrivileged(Binder.getCallingPid(), Binder.getCallingUid());
            if (!isPrivileged && !this.mWifiPermissionsUtil.isTargetSdkLessThan(packageName, 29)) {
                this.mLog.info("setWifiEnabled not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
                return false;
            } else if (!this.mSettingsStore.isAirplaneModeOn() || isPrivileged) {
                if (!(this.mWifiApState == 13) || isPrivileged) {
                    this.mLog.info("setWifiEnabled package=% uid=% enable=%").c(packageName).c((long) Binder.getCallingUid()).c(enable).flush();
                    long ident = Binder.clearCallingIdentity();
                    try {
                        if (!this.mSettingsStore.handleWifiToggled(enable)) {
                            return true;
                        }
                        Binder.restoreCallingIdentity(ident);
                        this.mWifiMetrics.incrementNumWifiToggles(isPrivileged, enable);
                        if (CtaManagerFactory.getInstance().makeCtaManager().isCtaSupported() && !isPrivileged) {
                            int wiFiEnabledState = getWifiEnabledState();
                            if (enable) {
                                if ((wiFiEnabledState == 0 || wiFiEnabledState == 1) && startConsentUi(packageName, Binder.getCallingUid(), "android.net.wifi.action.REQUEST_ENABLE")) {
                                    return true;
                                }
                            } else if ((wiFiEnabledState == 2 || wiFiEnabledState == 3) && startConsentUi(packageName, Binder.getCallingUid(), "android.net.wifi.action.REQUEST_DISABLE")) {
                                return true;
                            }
                        }
                        if (this.mWifiInjector.getOppoWifiSharingManager().isWifiSharingSupported()) {
                            int apEnabledState = this.mWifiController.getSoftApState();
                            if (!enable && (apEnabledState == 112 || apEnabledState == 113)) {
                                Slog.d(TAG, "close sharing first when set wifi disable");
                                disableWifiSharing();
                            }
                            int myPid = Process.myPid();
                            int userPid = Binder.getCallingPid();
                            if (!enable && myPid != userPid) {
                                this.mWifiInjector.getOppoWifiSharingManager().setWifiClosedByUser(true);
                            }
                        }
                        if (!enable) {
                            this.mClientModeImpl.notifyDisconnectPktName(packageName, Binder.getCallingUid(), 3);
                        }
                        if (!packageName.contains("engineermode") || !enable) {
                            SystemProperties.set("sys.eng.mod", "false");
                        } else {
                            SystemProperties.set("sys.eng.mod", "true");
                        }
                        if (enable) {
                            OppoAutoConnectManager.getInstance().enableAisAutoConnect();
                        }
                        this.mWifiController.sendMessage(155656);
                        if (enable) {
                            OppoWifiSwitchStats.getInstance(this.mContext).enqueueCallerPkg(packageName);
                        }
                        OppoWifiSwitchStats.getInstance(this.mContext).monitorWifiSwitchIssue(enable);
                        if (this.mOppoScanResultsProxy != null) {
                            this.mOppoScanResultsProxy.checkTurnOnWifi(packageName, enable);
                        }
                        return true;
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    this.mLog.err("setWifiEnabled SoftAp enabled: only Settings can toggle wifi").flush();
                    return false;
                }
            } else {
                this.mLog.err("setWifiEnabled in Airplane mode: only Settings can toggle wifi").flush();
                return false;
            }
        }
    }

    private boolean startConsentUi(String packageName, int callingUid, String intentAction) {
        if (UserHandle.getAppId(callingUid) == 1000) {
            return false;
        }
        try {
            if (this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, IWifiChip.ChipCapabilityMask.DUAL_BAND_DUAL_CHANNEL, UserHandle.getUserId(callingUid)).uid == callingUid) {
                Intent intent = new Intent(intentAction);
                intent.addFlags(276824064);
                intent.putExtra("android.intent.extra.PACKAGE_NAME", packageName);
                this.mContext.startActivity(intent);
                return true;
            }
            throw new SecurityException("Package " + packageName + " not in uid " + callingUid);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RemoteException(e.getMessage()).rethrowFromSystemServer();
        }
    }

    public int getWifiEnabledState() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getWifiEnabledState uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mClientModeImpl.syncGetWifiState();
    }

    public int getWifiApEnabledState() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getWifiApEnabledState uid=%").c((long) Binder.getCallingUid()).flush();
        }
        MutableInt apState = new MutableInt(11);
        this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(apState) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$Tk4v3H_jLeO4POzFwYzi9LRyPtE */
            private final /* synthetic */ MutableInt f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$getWifiApEnabledState$1$WifiServiceImpl(this.f$1);
            }
        }, 4000);
        return apState.value;
    }

    public /* synthetic */ void lambda$getWifiApEnabledState$1$WifiServiceImpl(MutableInt apState) {
        apState.value = this.mWifiApState;
    }

    public void updateInterfaceIpState(String ifaceName, int mode) {
        enforceNetworkStackPermission();
        this.mLog.info("updateInterfaceIpState uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(ifaceName, mode) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$UQ9JbF5sXBV77FhG4oE7wjNFgek */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$updateInterfaceIpState$2$WifiServiceImpl(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: updateInterfaceIpStateInternal */
    public void lambda$updateInterfaceIpState$2$WifiServiceImpl(String ifaceName, int mode) {
        synchronized (this.mLocalOnlyHotspotRequests) {
            int previousMode = -1;
            if (ifaceName != null) {
                previousMode = this.mIfaceIpModes.put(ifaceName, Integer.valueOf(mode));
            }
            Slog.d(TAG, "updateInterfaceIpState: ifaceName=" + ifaceName + " mode=" + mode + " previous mode= " + previousMode);
            if (mode != -1) {
                if (mode == 0) {
                    Slog.d(TAG, "IP mode config error - need to clean up");
                    if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                        Slog.d(TAG, "no LOHS requests, stop softap");
                        stopSoftAp();
                    } else {
                        Slog.d(TAG, "we have LOHS requests, clean them up");
                        sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(2);
                    }
                    lambda$updateInterfaceIpState$2$WifiServiceImpl(null, -1);
                } else if (mode != 1) {
                    if (mode != 2) {
                        this.mLog.warn("updateInterfaceIpStateInternal: unknown mode %").c((long) mode).flush();
                    } else if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                        stopSoftAp();
                        lambda$updateInterfaceIpState$2$WifiServiceImpl(null, -1);
                    } else {
                        sendHotspotStartedMessageToAllLOHSRequestInfoEntriesLocked();
                    }
                } else if (!isConcurrentLohsAndTetheringSupported()) {
                    sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(3);
                }
            } else if (ifaceName == null) {
                this.mIfaceIpModes.clear();
            }
        }
    }

    public boolean startSoftAp(WifiConfiguration wifiConfig) {
        enforceNetworkStackPermission();
        if (SystemProperties.getInt("persist.sys.wifi_ap_policy", 0) == 1) {
            Slog.d(TAG, "startSoftAp return for spaying");
            return false;
        }
        Slog.d(TAG, "startSoftAp:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        this.mLog.info("startSoftAp uid=%").c((long) Binder.getCallingUid()).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            OppoWifiSharingManager sharingManager = this.mWifiInjector.getOppoWifiSharingManager();
            if (sharingManager.isWifiSharingEnabledState() || 13 != getWifiApEnabledState() || !this.mIfaceIpModes.contains(1)) {
                disableWifiSharing();
                sharingManager.setWifiTetheringType(0);
                sharingManager.setWifiClosedByUser(true);
                this.mWifiInjector.getWifiController().setStaSoftApConcurrencyForSharing(false);
                if (!isConcurrentLohsAndTetheringSupported() && !this.mLocalOnlyHotspotRequests.isEmpty()) {
                    stopSoftApInternal(2);
                }
                return startSoftApInternal(wifiConfig, 1);
            }
            this.mLog.err("Tethering is already active.").flush();
            return false;
        }
    }

    private boolean startSoftApInternal(WifiConfiguration wifiConfig, int mode) {
        this.mLog.trace("startSoftApInternal uid=% mode=%").c((long) Binder.getCallingUid()).c((long) mode).flush();
        if (wifiConfig == null || WifiApConfigStore.validateApWifiConfiguration(wifiConfig)) {
            this.mWifiController.sendMessage(155658, 1, 0, new SoftApModeConfiguration(mode, wifiConfig));
            return true;
        }
        Slog.e(TAG, "Invalid WifiConfiguration");
        return false;
    }

    public boolean stopSoftAp() {
        boolean stopSoftApInternal;
        enforceNetworkStackPermission();
        if (SystemProperties.getInt("persist.sys.wifi_ap_policy", 0) == 2) {
            Slog.d(TAG, "stopSoftAp return for spaying");
            return false;
        }
        Slog.d(TAG, "stopSoftAp:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (!this.mLocalOnlyHotspotRequests.isEmpty()) {
                this.mLog.trace("Call to stop Tethering while LOHS is active, Registered LOHS callers will be updated when softap stopped.").flush();
            }
            stopSoftApInternal = stopSoftApInternal(1);
        }
        return stopSoftApInternal;
    }

    private boolean stopSoftApInternal(int mode) {
        this.mLog.trace("stopSoftApInternal uid=%").c((long) Binder.getCallingUid()).flush();
        if (!this.mFrameworkFacade.inStorageManagerCryptKeeperBounce()) {
            this.mWifiController.sendMessage(155658, 0, mode);
            return true;
        }
        Log.d(TAG, "Device still encrypted. Skip stop softap request.");
        return true;
    }

    private final class SoftApCallbackImpl implements WifiManager.SoftApCallback {
        private SoftApCallbackImpl() {
        }

        public void onStateChanged(int state, int failureReason) {
            WifiServiceImpl.this.mSoftApState = state;
            Iterator<ISoftApCallback> iterator = WifiServiceImpl.this.mRegisteredSoftApCallbacks.getCallbacks().iterator();
            while (iterator.hasNext()) {
                try {
                    iterator.next().onStateChanged(state, failureReason);
                } catch (RemoteException e) {
                    Log.e(WifiServiceImpl.TAG, "onStateChanged: remote exception -- " + e);
                    iterator.remove();
                }
            }
        }

        public void onNumClientsChanged(int numClients) {
            WifiServiceImpl.this.mSoftApNumClients = numClients;
            Iterator<ISoftApCallback> iterator = WifiServiceImpl.this.mRegisteredSoftApCallbacks.getCallbacks().iterator();
            while (iterator.hasNext()) {
                try {
                    iterator.next().onNumClientsChanged(numClients);
                } catch (RemoteException e) {
                    Log.e(WifiServiceImpl.TAG, "onNumClientsChanged: remote exception -- " + e);
                    iterator.remove();
                }
            }
        }
    }

    public void registerSoftApCallback(IBinder binder, ISoftApCallback callback, int callbackIdentifier) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (callback != null) {
            enforceNetworkSettingsPermission();
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("registerSoftApCallback uid=%").c((long) Binder.getCallingUid()).flush();
            }
            this.mWifiInjector.getClientModeImplHandler().post(new Runnable(binder, callback, callbackIdentifier) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$WH1yXObMcpzajFG1KwwEOakTA7o */
                private final /* synthetic */ IBinder f$1;
                private final /* synthetic */ ISoftApCallback f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    WifiServiceImpl.this.lambda$registerSoftApCallback$3$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            });
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    public /* synthetic */ void lambda$registerSoftApCallback$3$WifiServiceImpl(IBinder binder, ISoftApCallback callback, int callbackIdentifier) {
        if (!this.mRegisteredSoftApCallbacks.add(binder, callback, callbackIdentifier)) {
            Log.e(TAG, "registerSoftApCallback: Failed to add callback");
            return;
        }
        try {
            callback.onStateChanged(this.mSoftApState, 0);
            callback.onNumClientsChanged(this.mSoftApNumClients);
        } catch (RemoteException e) {
            Log.e(TAG, "registerSoftApCallback: remote exception -- " + e);
        }
    }

    public void unregisterSoftApCallback(int callbackIdentifier) {
        enforceNetworkSettingsPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("unregisterSoftApCallback uid=%").c((long) Binder.getCallingUid()).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(callbackIdentifier) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$RmshU723eQairQK6HNmdtEWCoRA */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$unregisterSoftApCallback$4$WifiServiceImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$unregisterSoftApCallback$4$WifiServiceImpl(int callbackIdentifier) {
        this.mRegisteredSoftApCallbacks.remove(callbackIdentifier);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiApStateChange(int currentState, int previousState, int errorCode, String ifaceName, int mode) {
        Slog.d(TAG, "handleWifiApStateChange: currentState=" + currentState + " previousState=" + previousState + " errorCode= " + errorCode + " ifaceName=" + ifaceName + " mode=" + mode);
        this.mWifiApState = currentState;
        if (currentState == 14 || currentState == 114) {
            synchronized (this.mLocalOnlyHotspotRequests) {
                int errorToReport = 2;
                if (errorCode == 1) {
                    errorToReport = 1;
                }
                sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(errorToReport);
                lambda$updateInterfaceIpState$2$WifiServiceImpl(null, -1);
            }
        } else if (currentState == 10 || currentState == 11 || currentState == 110 || currentState == 111) {
            synchronized (this.mLocalOnlyHotspotRequests) {
                if (this.mIfaceIpModes.getOrDefault(ifaceName, -1).intValue() == 2) {
                    sendHotspotStoppedMessageToAllLOHSRequestInfoEntriesLocked();
                } else if (!isConcurrentLohsAndTetheringSupported()) {
                    sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(2);
                }
                lambda$updateInterfaceIpState$2$WifiServiceImpl(null, -1);
            }
        }
    }

    @GuardedBy({"mLocalOnlyHotspotRequests"})
    private void sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(int arg1) {
        for (LocalOnlyHotspotRequestInfo requestor : this.mLocalOnlyHotspotRequests.values()) {
            try {
                requestor.sendHotspotFailedMessage(arg1);
                requestor.unlinkDeathRecipient();
            } catch (RemoteException e) {
            }
        }
        this.mLocalOnlyHotspotRequests.clear();
    }

    @GuardedBy({"mLocalOnlyHotspotRequests"})
    private void sendHotspotStoppedMessageToAllLOHSRequestInfoEntriesLocked() {
        for (LocalOnlyHotspotRequestInfo requestor : this.mLocalOnlyHotspotRequests.values()) {
            try {
                requestor.sendHotspotStoppedMessage();
                requestor.unlinkDeathRecipient();
            } catch (RemoteException e) {
            }
        }
        this.mLocalOnlyHotspotRequests.clear();
    }

    @GuardedBy({"mLocalOnlyHotspotRequests"})
    private void sendHotspotStartedMessageToAllLOHSRequestInfoEntriesLocked() {
        for (LocalOnlyHotspotRequestInfo requestor : this.mLocalOnlyHotspotRequests.values()) {
            try {
                requestor.sendHotspotStartedMessage(this.mLocalOnlyHotspotConfig);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void registerLOHSForTest(int pid, LocalOnlyHotspotRequestInfo request) {
        this.mLocalOnlyHotspotRequests.put(Integer.valueOf(pid), request);
    }

    /* JADX INFO: finally extract failed */
    public int startLocalOnlyHotspot(Messenger messenger, IBinder binder, String packageName) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        if (enforceChangePermission(packageName) != 0) {
            return 2;
        }
        enforceLocationPermission(packageName, uid);
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mWifiPermissionsUtil.isLocationModeEnabled()) {
                Binder.restoreCallingIdentity(ident);
                if (this.mUserManager.hasUserRestriction("no_config_tethering")) {
                    return 4;
                }
                if (!this.mFrameworkFacade.isAppForeground(uid)) {
                    return 3;
                }
                this.mLog.info("startLocalOnlyHotspot uid=% pid=%").c((long) uid).c((long) pid).flush();
                synchronized (this.mLocalOnlyHotspotRequests) {
                    int i = 1;
                    if (!isConcurrentLohsAndTetheringSupported() && this.mIfaceIpModes.contains(1)) {
                        this.mLog.info("Cannot start localOnlyHotspot when WiFi Tethering is active.").flush();
                        return 3;
                    } else if (this.mLocalOnlyHotspotRequests.get(Integer.valueOf(pid)) == null) {
                        LocalOnlyHotspotRequestInfo request = new LocalOnlyHotspotRequestInfo(binder, messenger, new LocalOnlyRequestorCallback());
                        if (this.mIfaceIpModes.contains(2)) {
                            try {
                                this.mLog.trace("LOHS already up, trigger onStarted callback").flush();
                                request.sendHotspotStartedMessage(this.mLocalOnlyHotspotConfig);
                            } catch (RemoteException e) {
                                return 2;
                            }
                        } else if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                            OppoWifiSharingManager sharingManager = this.mWifiInjector.getOppoWifiSharingManager();
                            disableWifiSharing();
                            sharingManager.setWifiTetheringType(0);
                            sharingManager.setWifiClosedByUser(true);
                            this.mWifiInjector.getWifiController().setStaSoftApConcurrencyForSharing(false);
                            boolean is5Ghz = hasAutomotiveFeature(this.mContext) && this.mContext.getResources().getBoolean(17891588) && is5GhzSupported();
                            Context context = this.mContext;
                            if (!is5Ghz) {
                                i = 0;
                            }
                            this.mLocalOnlyHotspotConfig = WifiApConfigStore.generateLocalOnlyHotspotConfig(context, i);
                            startSoftApInternal(this.mLocalOnlyHotspotConfig, 2);
                        }
                        this.mLocalOnlyHotspotRequests.put(Integer.valueOf(pid), request);
                        return 0;
                    } else {
                        this.mLog.trace("caller already has an active request").flush();
                        throw new IllegalStateException("Caller already has an active LocalOnlyHotspot request");
                    }
                }
            } else {
                throw new SecurityException("Location mode is not enabled.");
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    public void stopLocalOnlyHotspot() {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        this.mLog.info("stopLocalOnlyHotspot uid=% pid=%").c((long) uid).c((long) pid).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            LocalOnlyHotspotRequestInfo requestInfo = this.mLocalOnlyHotspotRequests.get(Integer.valueOf(pid));
            if (requestInfo != null) {
                requestInfo.unlinkDeathRecipient();
                unregisterCallingAppAndStopLocalOnlyHotspot(requestInfo);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterCallingAppAndStopLocalOnlyHotspot(LocalOnlyHotspotRequestInfo request) {
        this.mLog.trace("unregisterCallingAppAndStopLocalOnlyHotspot pid=%").c((long) request.getPid()).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (this.mLocalOnlyHotspotRequests.remove(Integer.valueOf(request.getPid())) == null) {
                this.mLog.trace("LocalOnlyHotspotRequestInfo not found to remove").flush();
                return;
            }
            if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                this.mLocalOnlyHotspotConfig = null;
                lambda$updateInterfaceIpState$2$WifiServiceImpl(null, -1);
                long identity = Binder.clearCallingIdentity();
                try {
                    stopSoftApInternal(2);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    public void startWatchLocalOnlyHotspot(Messenger messenger, IBinder binder) {
        enforceNetworkSettingsPermission();
        throw new UnsupportedOperationException("LocalOnlyHotspot is still in development");
    }

    public void stopWatchLocalOnlyHotspot() {
        enforceNetworkSettingsPermission();
        throw new UnsupportedOperationException("LocalOnlyHotspot is still in development");
    }

    public WifiConfiguration getWifiApConfiguration() {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        if (this.mWifiPermissionsUtil.checkConfigOverridePermission(uid)) {
            this.mLog.info("getWifiApConfiguration uid=%").c((long) uid).flush();
            GeneralUtil.Mutable<WifiConfiguration> config = new GeneralUtil.Mutable<>();
            if (this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(config) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$YyW97EISRuEDFxd28e1BPgstanY */
                private final /* synthetic */ GeneralUtil.Mutable f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    WifiServiceImpl.this.lambda$getWifiApConfiguration$5$WifiServiceImpl(this.f$1);
                }
            }, 4000)) {
                return config.value;
            }
            Log.e(TAG, "Failed to post runnable to fetch ap config");
            return new WifiConfiguration();
        }
        throw new SecurityException("App not allowed to read or update stored WiFi Ap config (uid = " + uid + ")");
    }

    public /* synthetic */ void lambda$getWifiApConfiguration$5$WifiServiceImpl(GeneralUtil.Mutable config) {
        config.value = (E) this.mWifiApConfigStore.getApConfiguration();
    }

    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig, String packageName) {
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        int uid = Binder.getCallingUid();
        if (this.mWifiPermissionsUtil.checkConfigOverridePermission(uid)) {
            this.mLog.info("setWifiApConfiguration uid=%").c((long) uid).flush();
            if (wifiConfig == null) {
                return false;
            }
            if (WifiApConfigStore.validateApWifiConfiguration(wifiConfig)) {
                this.mClientModeImplHandler.post(new Runnable(wifiConfig) {
                    /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$1NcQfkLN2A3TgWX_qB4iMe_lh4 */
                    private final /* synthetic */ WifiConfiguration f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        WifiServiceImpl.this.lambda$setWifiApConfiguration$6$WifiServiceImpl(this.f$1);
                    }
                });
                return true;
            }
            Slog.e(TAG, "Invalid WifiConfiguration");
            return false;
        }
        throw new SecurityException("App not allowed to read or update stored WiFi AP config (uid = " + uid + ")");
    }

    public /* synthetic */ void lambda$setWifiApConfiguration$6$WifiServiceImpl(WifiConfiguration wifiConfig) {
        this.mWifiApConfigStore.setApConfiguration(wifiConfig);
    }

    public void notifyUserOfApBandConversion(String packageName) {
        enforceNetworkSettingsPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("notifyUserOfApBandConversion uid=% packageName=%").c((long) Binder.getCallingUid()).c(packageName).flush();
        }
        this.mWifiApConfigStore.notifyUserOfApBandConversion(packageName);
    }

    public boolean isScanAlwaysAvailable() {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(57, Binder.getCallingUid());
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("isScanAlwaysAvailable uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mSettingsStore.isScanAlwaysAvailable();
    }

    public boolean disconnect(String packageName) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(66, Binder.getCallingUid());
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("disconnect not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return false;
        }
        this.mLog.info("disconnect uid=%").c((long) Binder.getCallingUid()).flush();
        this.mClientModeImpl.disconnectCommand();
        this.mClientModeImpl.notifyDisconnectPktName(packageName, Binder.getCallingUid(), 0);
        return true;
    }

    public boolean reconnect(String packageName) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(67, Binder.getCallingUid());
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("reconnect not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return false;
        }
        this.mLog.info("reconnect uid=%").c((long) Binder.getCallingUid()).flush();
        this.mClientModeImpl.reconnectCommand(new WorkSource(Binder.getCallingUid()));
        return true;
    }

    public boolean reassociate(String packageName) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(65, Binder.getCallingUid());
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("reassociate not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return false;
        }
        this.mLog.info("reassociate uid=%").c((long) Binder.getCallingUid()).flush();
        this.mClientModeImpl.reassociateCommand();
        return true;
    }

    public long getSupportedFeatures() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getSupportedFeatures uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return getSupportedFeaturesInternal();
    }

    public void requestActivityInfo(ResultReceiver result) {
        Bundle bundle = new Bundle();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("requestActivityInfo uid=%").c((long) Binder.getCallingUid()).flush();
        }
        bundle.putParcelable("controller_activity", reportActivityInfo());
        result.send(0, bundle);
    }

    /* JADX INFO: Multiple debug info for r7v5 double: [D('txCurrent' double), D('rxIdleCurrent' double)] */
    public WifiActivityEnergyInfo reportActivityInfo() {
        WifiActivityEnergyInfo energyInfo;
        long[] txTimePerLevel;
        double rxIdleCurrent;
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("reportActivityInfo uid=%").c((long) Binder.getCallingUid()).flush();
        }
        if ((getSupportedFeatures() & 65536) == 0) {
            return null;
        }
        WifiActivityEnergyInfo energyInfo2 = null;
        AsyncChannel asyncChannel = this.mClientModeImplChannel;
        if (asyncChannel != null) {
            WifiLinkLayerStats stats = this.mClientModeImpl.syncGetLinkLayerStats(asyncChannel);
            if (stats != null) {
                double rxIdleCurrent2 = this.mPowerProfile.getAveragePower("wifi.controller.idle");
                double rxCurrent = this.mPowerProfile.getAveragePower("wifi.controller.rx");
                double txCurrent = this.mPowerProfile.getAveragePower("wifi.controller.tx");
                double voltage = this.mPowerProfile.getAveragePower("wifi.controller.voltage") / 1000.0d;
                long rxIdleTime = (long) ((stats.on_time - stats.tx_time) - stats.rx_time);
                if (stats.tx_time_per_level != null) {
                    long[] txTimePerLevel2 = new long[stats.tx_time_per_level.length];
                    int i = 0;
                    while (i < txTimePerLevel2.length) {
                        txTimePerLevel2[i] = (long) stats.tx_time_per_level[i];
                        i++;
                        energyInfo2 = energyInfo2;
                    }
                    txTimePerLevel = txTimePerLevel2;
                } else {
                    txTimePerLevel = new long[0];
                }
                long energyUsed = (long) (((((double) stats.tx_time) * txCurrent) + (((double) stats.rx_time) * rxCurrent) + (((double) rxIdleTime) * rxIdleCurrent2)) * voltage);
                if (rxIdleTime < 0 || stats.on_time < 0 || stats.tx_time < 0 || stats.rx_time < 0 || stats.on_time_scan < 0 || energyUsed < 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" rxIdleCur=" + rxIdleCurrent2);
                    sb.append(" rxCur=" + rxCurrent);
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(" txCur=");
                    rxIdleCurrent = txCurrent;
                    sb2.append(rxIdleCurrent);
                    sb.append(sb2.toString());
                    sb.append(" voltage=" + voltage);
                    sb.append(" on_time=" + stats.on_time);
                    sb.append(" tx_time=" + stats.tx_time);
                    sb.append(" tx_time_per_level=" + Arrays.toString(txTimePerLevel));
                    sb.append(" rx_time=" + stats.rx_time);
                    sb.append(" rxIdleTime=" + rxIdleTime);
                    sb.append(" scan_time=" + stats.on_time_scan);
                    sb.append(" energy=" + energyUsed);
                    Log.d(TAG, " reportActivityInfo: " + sb.toString());
                } else {
                    rxIdleCurrent = txCurrent;
                }
                energyInfo = new WifiActivityEnergyInfo(this.mClock.getElapsedSinceBootMillis(), 3, (long) stats.tx_time, txTimePerLevel, (long) stats.rx_time, (long) stats.on_time_scan, rxIdleTime, energyUsed);
            } else {
                energyInfo = null;
            }
            if (energyInfo == null || !energyInfo.isValid()) {
                return null;
            }
            return energyInfo;
        }
        Slog.e(TAG, "mClientModeImplChannel is not initialized");
        return null;
    }

    public ParceledListSlice<WifiConfiguration> getConfiguredNetworks(String packageName) {
        boolean isCarrierApp = false;
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(60, Binder.getCallingUid());
        enforceAccessPermission();
        int callingUid = Binder.getCallingUid();
        if (!(callingUid == 2000 || callingUid == 0)) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mWifiPermissionsUtil.enforceCanAccessScanResults(packageName, callingUid);
            } catch (SecurityException e) {
                Slog.e(TAG, "Permission violation - getConfiguredNetworks not allowed for uid=" + callingUid + ", packageName=" + packageName + ", reason=" + e);
                return new ParceledListSlice<>(new ArrayList());
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        boolean isTargetSdkLessThanQOrPrivileged = isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), callingUid);
        if (this.mWifiInjector.makeTelephonyManager().checkCarrierPrivilegesForPackageAnyPhone(packageName) == 1) {
            isCarrierApp = true;
        }
        if (isTargetSdkLessThanQOrPrivileged || isCarrierApp) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getConfiguredNetworks uid=%").c((long) callingUid).flush();
            }
            int targetConfigUid = -1;
            if (isPrivileged(getCallingPid(), callingUid) || isDeviceOrProfileOwner(callingUid)) {
                targetConfigUid = 1010;
            } else if (isCarrierApp) {
                targetConfigUid = callingUid;
            }
            AsyncChannel asyncChannel = this.mClientModeImplChannel;
            if (asyncChannel != null) {
                List<WifiConfiguration> configs = this.mClientModeImpl.syncGetConfiguredNetworks(callingUid, asyncChannel, targetConfigUid);
                if (configs == null) {
                    return null;
                }
                if (isTargetSdkLessThanQOrPrivileged) {
                    return new ParceledListSlice<>(configs);
                }
                List<WifiConfiguration> creatorConfigs = new ArrayList<>();
                for (WifiConfiguration config : configs) {
                    if (config.creatorUid == callingUid) {
                        creatorConfigs.add(config);
                    }
                }
                return new ParceledListSlice<>(creatorConfigs);
            }
            Slog.e(TAG, "mClientModeImplChannel is not initialized");
            return null;
        }
        this.mLog.info("getConfiguredNetworks not allowed for uid=%").c((long) callingUid).flush();
        return new ParceledListSlice<>(new ArrayList());
    }

    public ParceledListSlice<WifiConfiguration> getPrivilegedConfiguredNetworks(String packageName) {
        enforceReadCredentialPermission();
        enforceAccessPermission();
        int callingUid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            this.mWifiPermissionsUtil.enforceCanAccessScanResults(packageName, callingUid);
            Binder.restoreCallingIdentity(ident);
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getPrivilegedConfiguredNetworks uid=%").c((long) callingUid).flush();
            }
            AsyncChannel asyncChannel = this.mClientModeImplChannel;
            if (asyncChannel != null) {
                List<WifiConfiguration> configs = this.mClientModeImpl.syncGetPrivilegedConfiguredNetwork(asyncChannel);
                if (configs != null) {
                    return new ParceledListSlice<>(configs);
                }
            } else {
                Slog.e(TAG, "mClientModeImplChannel is not initialized");
            }
            return null;
        } catch (SecurityException e) {
            Slog.e(TAG, "Permission violation - getPrivilegedConfiguredNetworks not allowed for uid=" + callingUid + ", packageName=" + packageName + ", reason=" + e);
            Binder.restoreCallingIdentity(ident);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    public Map<String, Map<Integer, List<ScanResult>>> getAllMatchingFqdnsForScanResults(List<ScanResult> scanResults) {
        if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getMatchingPasspointConfigurations uid=%").c((long) Binder.getCallingUid()).flush();
            }
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                return new HashMap();
            }
            return this.mClientModeImpl.syncGetAllMatchingFqdnsForScanResults(scanResults, this.mClientModeImplChannel);
        }
        throw new SecurityException("WifiService: Permission denied");
    }

    public Map<OsuProvider, List<ScanResult>> getMatchingOsuProviders(List<ScanResult> scanResults) {
        if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getMatchingOsuProviders uid=%").c((long) Binder.getCallingUid()).flush();
            }
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                return new HashMap();
            }
            return this.mClientModeImpl.syncGetMatchingOsuProviders(scanResults, this.mClientModeImplChannel);
        }
        throw new SecurityException("WifiService: Permission denied");
    }

    public Map<OsuProvider, PasspointConfiguration> getMatchingPasspointConfigsForOsuProviders(List<OsuProvider> osuProviders) {
        if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getMatchingPasspointConfigsForOsuProviders uid=%").c((long) Binder.getCallingUid()).flush();
            }
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                return new HashMap();
            }
            if (osuProviders != null) {
                return this.mClientModeImpl.syncGetMatchingPasspointConfigsForOsuProviders(osuProviders, this.mClientModeImplChannel);
            }
            Log.e(TAG, "Attempt to retrieve Passpoint configuration with null osuProviders");
            return new HashMap();
        }
        throw new SecurityException("WifiService: Permission denied");
    }

    public List<WifiConfiguration> getWifiConfigsForPasspointProfiles(List<String> fqdnList) {
        if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getWifiConfigsForPasspointProfiles uid=%").c((long) Binder.getCallingUid()).flush();
            }
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                return new ArrayList();
            }
            if (fqdnList != null) {
                return this.mClientModeImpl.syncGetWifiConfigsForPasspointProfiles(fqdnList, this.mClientModeImplChannel);
            }
            Log.e(TAG, "Attempt to retrieve WifiConfiguration with null fqdn List");
            return new ArrayList();
        }
        throw new SecurityException("WifiService: Permission denied");
    }

    public int addOrUpdateNetwork(WifiConfiguration config, String packageName) {
        if (enforceChangePermission(packageName) != 0) {
            return -1;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("addOrUpdateNetwork not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return -1;
        }
        this.mLog.info("addOrUpdateNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        if (config == null) {
            Slog.e(TAG, "bad network configuration");
            return -1;
        }
        this.mWifiMetrics.incrementNumAddOrUpdateNetworkCalls();
        if (config.isPasspoint()) {
            PasspointConfiguration passpointConfig = PasspointProvider.convertFromWifiConfig(config);
            if (passpointConfig.getCredential() == null) {
                Slog.e(TAG, "Missing credential for Passpoint profile");
                return -1;
            }
            X509Certificate[] x509Certificates = null;
            if (config.enterpriseConfig.getCaCertificate() != null) {
                x509Certificates = new X509Certificate[]{config.enterpriseConfig.getCaCertificate()};
            }
            passpointConfig.getCredential().setCaCertificates(x509Certificates);
            passpointConfig.getCredential().setClientCertificateChain(config.enterpriseConfig.getClientCertificateChain());
            passpointConfig.getCredential().setClientPrivateKey(config.enterpriseConfig.getClientPrivateKey());
            if (addOrUpdatePasspointConfiguration(passpointConfig, packageName)) {
                return 0;
            }
            Slog.e(TAG, "Failed to add Passpoint profile");
            return -1;
        }
        Slog.i("addOrUpdateNetwork", " uid = " + Integer.toString(Binder.getCallingUid()) + " SSID " + config.SSID + " nid=" + Integer.toString(config.networkId));
        if (config.networkId == -1) {
            config.creatorUid = Binder.getCallingUid();
        } else {
            config.lastUpdateUid = Binder.getCallingUid();
        }
        AsyncChannel asyncChannel = this.mClientModeImplChannel;
        if (asyncChannel != null) {
            return this.mClientModeImpl.syncAddOrUpdateNetwork(asyncChannel, config);
        }
        Slog.e(TAG, "mClientModeImplChannel is not initialized");
        return -1;
    }

    public static void verifyCert(X509Certificate caCert) throws GeneralSecurityException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        CertPathValidator validator = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
        CertPath path = factory.generateCertPath(Arrays.asList(caCert));
        KeyStore ks = KeyStore.getInstance("AndroidCAStore");
        ks.load(null, null);
        PKIXParameters params = new PKIXParameters(ks);
        params.setRevocationEnabled(false);
        validator.validate(path, params);
    }

    public boolean removeNetwork(int netId, String packageName) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(63, Binder.getCallingUid());
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("removeNetwork not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return false;
        }
        this.mLog.info("removeNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mClientModeImplChannel != null) {
            if (!this.mClientModeImpl.isThirdApp(Binder.getCallingUid()) && (this.mClientModeImpl.isNetworkConnected(netId) || this.mClientModeImpl.isNetworkConnecting(netId))) {
                this.mClientModeImpl.notifyDisconnectPktName(packageName, Binder.getCallingUid(), 0);
            }
            return this.mClientModeImpl.syncRemoveNetwork(this.mClientModeImplChannel, netId);
        }
        Slog.e(TAG, "mClientModeImplChannel is not initialized");
        return false;
    }

    public boolean enableNetwork(int netId, boolean disableOthers, String packageName) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(61, Binder.getCallingUid());
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("enableNetwork not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return false;
        }
        this.mLog.info("enableNetwork uid=% disableOthers=%").c((long) Binder.getCallingUid()).c(disableOthers).flush();
        this.mWifiMetrics.incrementNumEnableNetworkCalls();
        AsyncChannel asyncChannel = this.mClientModeImplChannel;
        if (asyncChannel != null) {
            return this.mClientModeImpl.syncEnableNetwork(asyncChannel, netId, disableOthers);
        }
        Slog.e(TAG, "mClientModeImplChannel is not initialized");
        return false;
    }

    public boolean disableNetwork(int netId, String packageName) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(64, Binder.getCallingUid());
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        if (!isTargetSdkLessThanQOrPrivileged(packageName, Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("disableNetwork not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return false;
        }
        this.mLog.info("disableNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mClientModeImplChannel != null) {
            if (!this.mClientModeImpl.isThirdApp(Binder.getCallingUid()) && (this.mClientModeImpl.isNetworkConnected(netId) || this.mClientModeImpl.isNetworkConnecting(netId))) {
                this.mClientModeImpl.notifyDisconnectPktName(packageName, Binder.getCallingUid(), 0);
            }
            return this.mClientModeImpl.syncDisableNetwork(this.mClientModeImplChannel, netId);
        }
        Slog.e(TAG, "mClientModeImplChannel is not initialized");
        return false;
    }

    public WifiInfo getConnectionInfo(String callingPackage) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(58, Binder.getCallingUid());
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getConnectionInfo uid=%").c((long) uid).flush();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            WifiInfo result = this.mClientModeImpl.syncRequestConnectionInfo();
            boolean hideDefaultMacAddress = true;
            boolean hideBssidSsidAndNetworkId = true;
            try {
                if (this.mWifiInjector.getWifiPermissionsWrapper().getLocalMacAddressPermission(uid) == 0) {
                    hideDefaultMacAddress = false;
                }
                this.mWifiPermissionsUtil.enforceCanAccessScanResults(callingPackage, uid);
                hideBssidSsidAndNetworkId = false;
            } catch (RemoteException e) {
                Log.e(TAG, "Error checking receiver permission", e);
            } catch (SecurityException e2) {
            }
            if (hideDefaultMacAddress) {
                result.setMacAddress("02:00:00:00:00:00");
            }
            if (hideBssidSsidAndNetworkId) {
                result.setBSSID("02:00:00:00:00:00");
                result.setSSID(WifiSsid.createFromHex((String) null));
                result.setNetworkId(-1);
            }
            if (this.mVerboseLoggingEnabled && (hideBssidSsidAndNetworkId || hideDefaultMacAddress)) {
                WifiLog wifiLog = this.mLog;
                wifiLog.v("getConnectionInfo: hideBssidSsidAndNetworkId=" + hideBssidSsidAndNetworkId + ", hideDefaultMacAddress=" + hideDefaultMacAddress);
            }
            return result;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public List<ScanResult> getScanResults(String callingPackage) {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getScanResults uid=%").c((long) uid).flush();
        }
        try {
            this.mWifiPermissionsUtil.enforceCanAccessScanResults(callingPackage, uid);
            List<ScanResult> scanResults = new ArrayList<>();
            if (!this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(scanResults, callingPackage, uid) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$DSePlowdbT3hjogjTAA0UgXtbsI */
                private final /* synthetic */ List f$1;
                private final /* synthetic */ String f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    WifiServiceImpl.this.lambda$getScanResults$7$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            }, 4000)) {
                Log.e(TAG, "Failed to post runnable to fetch scan results");
                return new ArrayList();
            }
            Binder.restoreCallingIdentity(ident);
            return scanResults;
        } catch (SecurityException e) {
            Slog.e(TAG, "Permission violation - getScanResults not allowed for uid=" + uid + ", packageName=" + callingPackage + ", reason=" + e);
            return new ArrayList();
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public /* synthetic */ void lambda$getScanResults$7$WifiServiceImpl(List scanResults, String callingPackage, int uid) {
        scanResults.addAll(this.mOppoScanResultsProxy.getScanResults(callingPackage, uid));
    }

    public boolean addOrUpdatePasspointConfiguration(PasspointConfiguration config, String packageName) {
        if (enforceChangePermission(packageName) != 0) {
            return false;
        }
        this.mLog.info("addorUpdatePasspointConfiguration uid=%").c((long) Binder.getCallingUid()).flush();
        if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return false;
        }
        return this.mClientModeImpl.syncAddOrUpdatePasspointConfig(this.mClientModeImplChannel, config, Binder.getCallingUid(), packageName);
    }

    public boolean removePasspointConfiguration(String fqdn, String packageName) {
        int uid = Binder.getCallingUid();
        if (this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid) || this.mWifiPermissionsUtil.checkNetworkCarrierProvisioningPermission(uid)) {
            this.mLog.info("removePasspointConfiguration uid=%").c((long) Binder.getCallingUid()).flush();
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                return false;
            }
            return this.mClientModeImpl.syncRemovePasspointConfig(this.mClientModeImplChannel, fqdn);
        } else if (this.mWifiPermissionsUtil.isTargetSdkLessThan(packageName, 29)) {
            return false;
        } else {
            throw new SecurityException("WifiService: Permission denied");
        }
    }

    public List<PasspointConfiguration> getPasspointConfigurations(String packageName) {
        int uid = Binder.getCallingUid();
        this.mAppOps.checkPackage(uid, packageName);
        if (this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid) || this.mWifiPermissionsUtil.checkNetworkSetupWizardPermission(uid)) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("getPasspointConfigurations uid=%").c((long) Binder.getCallingUid()).flush();
            }
            if (!this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
                return new ArrayList();
            }
            return this.mClientModeImpl.syncGetPasspointConfigs(this.mClientModeImplChannel);
        } else if (this.mWifiPermissionsUtil.isTargetSdkLessThan(packageName, 29)) {
            return new ArrayList();
        } else {
            throw new SecurityException("WifiService: Permission denied");
        }
    }

    public void queryPasspointIcon(long bssid, String fileName) {
        enforceAccessPermission();
        this.mLog.info("queryPasspointIcon uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            this.mClientModeImpl.syncQueryPasspointIcon(this.mClientModeImplChannel, bssid, fileName);
            return;
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public int matchProviderWithCurrentNetwork(String fqdn) {
        this.mLog.info("matchProviderWithCurrentNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mClientModeImpl.matchProviderWithCurrentNetwork(this.mClientModeImplChannel, fqdn);
    }

    public void deauthenticateNetwork(long holdoff, boolean ess) {
        this.mLog.info("deauthenticateNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        this.mClientModeImpl.deauthenticateNetwork(this.mClientModeImplChannel, holdoff, ess);
    }

    public void setCountryCode(String countryCode) {
        Slog.i(TAG, "WifiService trying to set country code to " + countryCode);
        enforceConnectivityInternalPermission();
        this.mLog.info("setCountryCode uid=%").c((long) Binder.getCallingUid()).flush();
        long token = Binder.clearCallingIdentity();
        this.mCountryCode.setCountryCode(countryCode);
        Binder.restoreCallingIdentity(token);
    }

    public String getCountryCode() {
        enforceConnectivityInternalPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getCountryCode uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mCountryCode.getCountryCode();
    }

    @Override // com.android.server.wifi.OppoWifiServiceImpl
    public boolean isP2p5GSupported() {
        OppoSoftapP2pBandControl mOppoSoftapP2pBandControl = WifiInjector.getInstance().getOppoSoftapP2pBandControl();
        return !mOppoSoftapP2pBandControl.isP2pInOnly2GRegion() || !mOppoSoftapP2pBandControl.needP2pLimit2GOnlyBand();
    }

    @Override // com.android.server.wifi.OppoWifiServiceImpl
    public boolean isSoftap5GSupported() {
        OppoSoftapP2pBandControl mOppoSoftapP2pBandControl = WifiInjector.getInstance().getOppoSoftapP2pBandControl();
        return !mOppoSoftapP2pBandControl.isSoftapInOnly2GRegion() || !mOppoSoftapP2pBandControl.needSoftapUse2GOnlyCountry();
    }

    public boolean isDualBandSupported() {
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("isDualBandSupported uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_5g_band_support", 0) == 1;
    }

    private int getMaxApInterfacesCount() {
        return this.mContext.getResources().getInteger(17694959);
    }

    private boolean isConcurrentLohsAndTetheringSupported() {
        return getMaxApInterfacesCount() >= 2;
    }

    public boolean needs5GHzToAnyApBandConversion() {
        enforceNetworkSettingsPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("needs5GHzToAnyApBandConversion uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mContext.getResources().getBoolean(17891577);
    }

    @Deprecated
    public DhcpInfo getDhcpInfo() {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(59, Binder.getCallingUid());
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getDhcpInfo uid=%").c((long) Binder.getCallingUid()).flush();
        }
        DhcpResults dhcpResults = this.mClientModeImpl.syncGetDhcpResults();
        DhcpInfo info = new DhcpInfo();
        if (dhcpResults.ipAddress != null && (dhcpResults.ipAddress.getAddress() instanceof Inet4Address)) {
            info.ipAddress = NetworkUtils.inetAddressToInt((Inet4Address) dhcpResults.ipAddress.getAddress());
        }
        if (dhcpResults.gateway != null) {
            info.gateway = NetworkUtils.inetAddressToInt((Inet4Address) dhcpResults.gateway);
        }
        int dnsFound = 0;
        Iterator it = dhcpResults.dnsServers.iterator();
        while (it.hasNext()) {
            InetAddress dns = (InetAddress) it.next();
            if (dns instanceof Inet4Address) {
                if (dnsFound == 0) {
                    info.dns1 = NetworkUtils.inetAddressToInt((Inet4Address) dns);
                } else {
                    info.dns2 = NetworkUtils.inetAddressToInt((Inet4Address) dns);
                }
                dnsFound++;
                if (dnsFound > 1) {
                    break;
                }
            }
        }
        Inet4Address serverAddress = dhcpResults.serverAddress;
        if (serverAddress != null) {
            info.serverAddress = NetworkUtils.inetAddressToInt(serverAddress);
        }
        info.leaseDuration = dhcpResults.leaseDuration;
        return info;
    }

    /* access modifiers changed from: package-private */
    public class TdlsTaskParams {
        public boolean enable;
        public String remoteIpAddress;

        TdlsTaskParams() {
        }
    }

    class TdlsTask extends AsyncTask<TdlsTaskParams, Integer, Integer> {
        TdlsTask() {
        }

        /* access modifiers changed from: protected */
        public Integer doInBackground(TdlsTaskParams... params) {
            TdlsTaskParams param = params[0];
            String remoteIpAddress = param.remoteIpAddress.trim();
            boolean enable = param.enable;
            String macAddress = null;
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader("/proc/net/arp"));
                reader2.readLine();
                while (true) {
                    String line = reader2.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] tokens = line.split("[ ]+");
                    if (tokens.length >= 6) {
                        String ip = tokens[0];
                        String mac = tokens[3];
                        if (remoteIpAddress.equals(ip)) {
                            macAddress = mac;
                            break;
                        }
                    }
                }
                if (macAddress == null) {
                    Slog.w(WifiServiceImpl.TAG, "Did not find remoteAddress {" + remoteIpAddress + "} in /proc/net/arp");
                } else {
                    WifiServiceImpl.this.enableTdlsWithMacAddress(macAddress, enable);
                }
                try {
                    reader2.close();
                } catch (IOException e) {
                }
            } catch (FileNotFoundException e2) {
                Slog.e(WifiServiceImpl.TAG, "Could not open /proc/net/arp to lookup mac address");
                if (0 != 0) {
                    reader.close();
                }
            } catch (IOException e3) {
                Slog.e(WifiServiceImpl.TAG, "Could not read /proc/net/arp to lookup mac address");
                if (0 != 0) {
                    reader.close();
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        reader.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
            return 0;
        }
    }

    public void enableTdls(String remoteAddress, boolean enable) {
        if (remoteAddress != null) {
            this.mLog.info("enableTdls uid=% enable=%").c((long) Binder.getCallingUid()).c(enable).flush();
            TdlsTaskParams params = new TdlsTaskParams();
            params.remoteIpAddress = remoteAddress;
            params.enable = enable;
            new TdlsTask().execute(params);
            return;
        }
        throw new IllegalArgumentException("remoteAddress cannot be null");
    }

    public void enableTdlsWithMacAddress(String remoteMacAddress, boolean enable) {
        this.mLog.info("enableTdlsWithMacAddress uid=% enable=%").c((long) Binder.getCallingUid()).c(enable).flush();
        if (remoteMacAddress != null) {
            this.mClientModeImpl.enableTdls(remoteMacAddress, enable);
            return;
        }
        throw new IllegalArgumentException("remoteMacAddress cannot be null");
    }

    public Messenger getWifiServiceMessenger(String packageName) {
        enforceAccessPermission();
        if (enforceChangePermission(packageName) == 0) {
            this.mLog.info("getWifiServiceMessenger uid=%").c((long) Binder.getCallingUid()).flush();
            return new Messenger(this.mAsyncChannelExternalClientHandler);
        }
        throw new SecurityException("Could not create wifi service messenger");
    }

    public void disableEphemeralNetwork(String SSID, String packageName) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", TAG);
        if (!isPrivileged(Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mLog.info("disableEphemeralNetwork not allowed for uid=%").c((long) Binder.getCallingUid()).flush();
            return;
        }
        this.mLog.info("disableEphemeralNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        this.mClientModeImpl.disableEphemeralNetwork(SSID);
    }

    private void registerForScanModeChange() {
        this.mFrameworkFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_scan_always_enabled"), false, new ContentObserver(null) {
            /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass6 */

            public void onChange(boolean selfChange) {
                WifiServiceImpl.this.mSettingsStore.handleWifiScanAlwaysAvailableToggled();
                WifiServiceImpl.this.mWifiController.sendMessage(155655);
            }
        });
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        intentFilter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        if (this.mContext.getResources().getBoolean(17891594)) {
            intentFilter.addAction("android.intent.action.EMERGENCY_CALL_STATE_CHANGED");
        }
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_FULLY_REMOVED");
        intentFilter2.addDataScheme("package");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.WifiServiceImpl.AnonymousClass7 */

            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.PACKAGE_FULLY_REMOVED")) {
                    int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                    Uri uri = intent.getData();
                    if (uid != -1 && uri != null) {
                        String pkgName = uri.getSchemeSpecificPart();
                        WifiServiceImpl.this.mClientModeImpl.removeAppConfigs(pkgName, uid);
                        WifiServiceImpl.this.mWifiInjector.getClientModeImplHandler().post(new Runnable(pkgName, uid) {
                            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$7$eMYau17vdgiWx_KHNvkXGvjAy0M */
                            private final /* synthetic */ String f$1;
                            private final /* synthetic */ int f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void run() {
                                WifiServiceImpl.AnonymousClass7.this.lambda$onReceive$0$WifiServiceImpl$7(this.f$1, this.f$2);
                            }
                        });
                    }
                }
            }

            public /* synthetic */ void lambda$onReceive$0$WifiServiceImpl$7(String pkgName, int uid) {
                WifiServiceImpl.this.mScanRequestProxy.clearScanRequestTimestampsForApp(pkgName, uid);
                WifiServiceImpl.this.mWifiNetworkSuggestionsManager.removeApp(pkgName);
                WifiServiceImpl.this.mClientModeImpl.removeNetworkRequestUserApprovedAccessPointsForApp(pkgName);
                WifiServiceImpl.this.mWifiInjector.getPasspointManager().removePasspointProviderWithPackage(pkgName);
            }
        }, intentFilter2);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.wifi.WifiServiceImpl */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new WifiShellCommand(this.mWifiInjector).exec(this, in, out, err, args, callback, resultReceiver);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump WifiService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        } else if (args != null && args.length > 0 && WifiMetrics.PROTO_DUMP_ARG.equals(args[0])) {
            this.mClientModeImpl.updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
        } else if (args != null && args.length > 0 && "ipclient".equals(args[0])) {
            String[] ipClientArgs = new String[(args.length - 1)];
            System.arraycopy(args, 1, ipClientArgs, 0, ipClientArgs.length);
            this.mClientModeImpl.dumpIpClient(fd, pw, ipClientArgs);
        } else if (args != null && args.length > 0 && WifiScoreReport.DUMP_ARG.equals(args[0])) {
            WifiScoreReport wifiScoreReport = this.mClientModeImpl.getWifiScoreReport();
            if (wifiScoreReport != null) {
                wifiScoreReport.dump(fd, pw, args);
            }
        } else if (args == null || args.length <= 0 || !WifiScoreCard.DUMP_ARG.equals(args[0])) {
            this.mClientModeImpl.updateLinkLayerStatsRssiAndScoreReport();
            pw.println("Wi-Fi is " + this.mClientModeImpl.syncGetWifiStateByName());
            StringBuilder sb = new StringBuilder();
            sb.append("Verbose logging is ");
            sb.append(this.mVerboseLoggingEnabled ? "on" : "off");
            pw.println(sb.toString());
            pw.println("Stay-awake conditions: " + this.mFacade.getIntegerSetting(this.mContext, "stay_on_while_plugged_in", 0));
            pw.println("mInIdleMode " + this.mInIdleMode);
            pw.println("mScanPending " + this.mScanPending);
            this.mWifiController.dump(fd, pw, args);
            this.mSettingsStore.dump(fd, pw, args);
            this.mWifiTrafficPoller.dump(fd, pw, args);
            pw.println();
            pw.println("Locks held:");
            this.mWifiLockManager.dump(pw);
            pw.println();
            this.mWifiMulticastLockManager.dump(pw);
            pw.println();
            this.mActiveModeWarden.dump(fd, pw, args);
            pw.println();
            this.mClientModeImpl.dump(fd, pw, args);
            pw.println();
            this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(pw) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$13az8HpQ6rE2LvEgUJf1zT6uqg0 */
                private final /* synthetic */ PrintWriter f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    WifiServiceImpl.this.lambda$dump$9$WifiServiceImpl(this.f$1);
                }
            }, 4000);
            this.mClientModeImpl.updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
            pw.println();
            this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(fd, pw, args) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$LcYN2fDH9ln7kJ6dwOPrjBsCO8 */
                private final /* synthetic */ FileDescriptor f$1;
                private final /* synthetic */ PrintWriter f$2;
                private final /* synthetic */ String[] f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    WifiServiceImpl.this.lambda$dump$10$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            }, 4000);
            this.mWifiBackupRestore.dump(fd, pw, args);
            pw.println();
            pw.println("ScoringParams: settings put global wifi_score_params " + this.mWifiInjector.getScoringParams());
            pw.println();
            WifiScoreReport wifiScoreReport2 = this.mClientModeImpl.getWifiScoreReport();
            if (wifiScoreReport2 != null) {
                pw.println("WifiScoreReport:");
                wifiScoreReport2.dump(fd, pw, args);
            }
            pw.println();
            SarManager sarManager = this.mWifiInjector.getSarManager();
            if (sarManager != null) {
                sarManager.dump(fd, pw, args);
            }
            pw.println();
        } else {
            this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(pw) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$30hAwmJfIirsTRMV7XyzBiqYQ */
                private final /* synthetic */ PrintWriter f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    WifiServiceImpl.this.lambda$dump$8$WifiServiceImpl(this.f$1);
                }
            }, 4000);
        }
    }

    public /* synthetic */ void lambda$dump$8$WifiServiceImpl(PrintWriter pw) {
        WifiScoreCard wifiScoreCard = this.mWifiInjector.getWifiScoreCard();
        if (wifiScoreCard != null) {
            pw.println(wifiScoreCard.getNetworkListBase64(true));
        }
    }

    public /* synthetic */ void lambda$dump$9$WifiServiceImpl(PrintWriter pw) {
        WifiScoreCard wifiScoreCard = this.mWifiInjector.getWifiScoreCard();
        if (wifiScoreCard != null) {
            pw.println("WifiScoreCard:");
            pw.println(wifiScoreCard.getNetworkListBase64(true));
        }
    }

    public /* synthetic */ void lambda$dump$10$WifiServiceImpl(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mWifiNetworkSuggestionsManager.dump(fd, pw, args);
        pw.println();
    }

    public boolean acquireWifiLock(IBinder binder, int lockMode, String tag, WorkSource ws) {
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("acquireWifiLock uid=% lockMode=%").c((long) Binder.getCallingUid()).c((long) lockMode).flush();
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
        WorkSource updatedWs = (ws == null || ws.isEmpty()) ? new WorkSource(Binder.getCallingUid()) : ws;
        GeneralUtil.Mutable<Boolean> lockSuccess = new GeneralUtil.Mutable<>();
        if (this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(lockSuccess, lockMode, tag, binder, updatedWs) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$n3KWmNlfRCBZERhxLXED7cgCptg */
            private final /* synthetic */ GeneralUtil.Mutable f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ String f$3;
            private final /* synthetic */ IBinder f$4;
            private final /* synthetic */ WorkSource f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$acquireWifiLock$11$WifiServiceImpl(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        }, 4000)) {
            return lockSuccess.value.booleanValue();
        }
        Log.e(TAG, "Failed to post runnable to acquireWifiLock");
        return false;
    }

    public /* synthetic */ void lambda$acquireWifiLock$11$WifiServiceImpl(GeneralUtil.Mutable lockSuccess, int lockMode, String tag, IBinder binder, WorkSource updatedWs) {
        lockSuccess.value = (E) Boolean.valueOf(this.mWifiLockManager.acquireWifiLock(lockMode, tag, binder, updatedWs));
    }

    public void updateWifiLockWorkSource(IBinder binder, WorkSource ws) {
        this.mLog.info("updateWifiLockWorkSource uid=%").c((long) Binder.getCallingUid()).flush();
        this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
        if (!this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(binder, (ws == null || ws.isEmpty()) ? new WorkSource(Binder.getCallingUid()) : ws) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$dSBCyEfgSgPcVuPtwH4UCtTcks */
            private final /* synthetic */ IBinder f$1;
            private final /* synthetic */ WorkSource f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$updateWifiLockWorkSource$12$WifiServiceImpl(this.f$1, this.f$2);
            }
        }, 4000)) {
            Log.e(TAG, "Failed to post runnable to updateWifiLockWorkSource");
        }
    }

    public /* synthetic */ void lambda$updateWifiLockWorkSource$12$WifiServiceImpl(IBinder binder, WorkSource updatedWs) {
        this.mWifiLockManager.updateWifiLockWorkSource(binder, updatedWs);
    }

    public boolean releaseWifiLock(IBinder binder) {
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("releaseWifiLock uid=%").c((long) Binder.getCallingUid()).flush();
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
        GeneralUtil.Mutable<Boolean> lockSuccess = new GeneralUtil.Mutable<>();
        if (this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(lockSuccess, binder) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$BEFXJX98bKCvmAvi2e5Iwmres */
            private final /* synthetic */ GeneralUtil.Mutable f$1;
            private final /* synthetic */ IBinder f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$releaseWifiLock$13$WifiServiceImpl(this.f$1, this.f$2);
            }
        }, 4000)) {
            return lockSuccess.value.booleanValue();
        }
        Log.e(TAG, "Failed to post runnable to releaseWifiLock");
        return false;
    }

    public /* synthetic */ void lambda$releaseWifiLock$13$WifiServiceImpl(GeneralUtil.Mutable lockSuccess, IBinder binder) {
        lockSuccess.value = (E) Boolean.valueOf(this.mWifiLockManager.releaseWifiLock(binder));
    }

    public void initializeMulticastFiltering() {
        enforceMulticastChangePermission();
        this.mLog.info("initializeMulticastFiltering uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiMulticastLockManager.initializeFiltering();
    }

    public void acquireMulticastLock(IBinder binder, String tag) {
        enforceMulticastChangePermission();
        this.mLog.info("acquireMulticastLock uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiMulticastLockManager.acquireLock(binder, tag);
    }

    public void releaseMulticastLock(String tag) {
        enforceMulticastChangePermission();
        this.mLog.info("releaseMulticastLock uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiMulticastLockManager.releaseLock(tag);
    }

    public boolean isMulticastEnabled() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("isMulticastEnabled uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mWifiMulticastLockManager.isMulticastEnabled();
    }

    public void enableVerboseLogging(int verbose) {
        enforceAccessPermission();
        enforceNetworkSettingsPermission();
        enableOppoWifiServiceLogging(verbose);
        this.mLog.info("enableVerboseLogging uid=% verbose=%").c((long) Binder.getCallingUid()).c((long) verbose).flush();
        this.mFacade.setIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", verbose);
        enableVerboseLoggingInternal(verbose);
        this.mWifiInjector.getPropertyService().set("persist.vendor.logmuch", verbose > 0 ? "false" : "true");
    }

    /* access modifiers changed from: package-private */
    public void enableVerboseLoggingInternal(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
        this.mClientModeImpl.enableVerboseLogging(verbose);
        this.mWifiLockManager.enableVerboseLogging(verbose);
        this.mWifiMulticastLockManager.enableVerboseLogging(verbose);
        this.mWifiInjector.enableVerboseLogging(verbose);
        this.mOppoClientModeImpl2.enableVerboseLogging(verbose);
        ScanRequestProxy scanRequestProxy = this.mScanRequestProxy;
        if (scanRequestProxy != null) {
            scanRequestProxy.enableVerboseLogging(verbose);
        }
        this.mWifiController.enableVerboseLogging(verbose);
    }

    /* JADX WARN: Type inference failed for: r1v1, types: [boolean] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public int getVerboseLoggingLevel() {
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getVerboseLoggingLevel uid=%").c((long) Binder.getCallingUid()).flush();
        }
        ?? r1 = SystemProperties.getBoolean(DEBUG_PROPERTY, false);
        if (r1 > 0) {
            return r1 == true ? 1 : 0;
        }
        return this.mFacade.getIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", 0);
    }

    public void factoryReset(String packageName) {
        List<PasspointConfiguration> configs;
        enforceConnectivityInternalPermission();
        if (enforceChangePermission(packageName) == 0) {
            this.mLog.info("factoryReset uid=%").c((long) Binder.getCallingUid()).flush();
            if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
                if (!this.mUserManager.hasUserRestriction("no_config_tethering")) {
                    stopSoftApInternal(-1);
                }
                if (!this.mUserManager.hasUserRestriction("no_config_wifi")) {
                    if (this.mClientModeImplChannel != null) {
                        List<WifiConfiguration> networks = this.mClientModeImpl.syncGetConfiguredNetworks(Binder.getCallingUid(), this.mClientModeImplChannel, 1010);
                        if (networks != null) {
                            for (WifiConfiguration config : networks) {
                                removeNetwork(config.networkId, packageName);
                            }
                        }
                        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint") && (configs = this.mClientModeImpl.syncGetPasspointConfigs(this.mClientModeImplChannel)) != null) {
                            for (PasspointConfiguration config2 : configs) {
                                removePasspointConfiguration(config2.getHomeSp().getFqdn(), packageName);
                            }
                        }
                    }
                    this.mWifiInjector.getClientModeImplHandler().post(new Runnable() {
                        /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$JkApogTbDiBTzeZ_bOproLuiHLQ */

                        public final void run() {
                            WifiServiceImpl.this.lambda$factoryReset$14$WifiServiceImpl();
                        }
                    });
                }
            }
        }
    }

    public /* synthetic */ void lambda$factoryReset$14$WifiServiceImpl() {
        this.mWifiInjector.getWifiConfigManager().clearDeletedEphemeralNetworks();
        this.mClientModeImpl.clearNetworkRequestUserApprovedAccessPoints();
        this.mWifiNetworkSuggestionsManager.clear();
        this.mWifiInjector.getWifiScoreCard().clear();
    }

    static boolean logAndReturnFalse(String s) {
        Log.d(TAG, s);
        return false;
    }

    public Network getCurrentNetwork() {
        enforceAccessPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getCurrentNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mClientModeImpl.getCurrentNetwork();
    }

    public static String toHexString(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('\'');
        sb.append(s);
        sb.append('\'');
        for (int n = 0; n < s.length(); n++) {
            sb.append(String.format(" %02x", Integer.valueOf(s.charAt(n) & 65535)));
        }
        return sb.toString();
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        enforceConnectivityInternalPermission();
        this.mLog.info("enableWifiConnectivityManager uid=% enabled=%").c((long) Binder.getCallingUid()).c(enabled).flush();
        this.mClientModeImpl.enableWifiConnectivityManager(enabled);
    }

    public byte[] retrieveBackupData() {
        enforceNetworkSettingsPermission();
        this.mLog.info("retrieveBackupData uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mClientModeImplChannel == null) {
            Slog.e(TAG, "mClientModeImplChannel is not initialized");
            return null;
        }
        Slog.d(TAG, "Retrieving backup data");
        byte[] backupData = this.mWifiBackupRestore.retrieveBackupDataFromConfigurations(this.mClientModeImpl.syncGetPrivilegedConfiguredNetwork(this.mClientModeImplChannel));
        Slog.d(TAG, "Retrieved backup data");
        return backupData;
    }

    private void restoreNetworks(List<WifiConfiguration> configurations) {
        if (configurations == null) {
            Slog.e(TAG, "Backup data parse failed");
            return;
        }
        for (WifiConfiguration configuration : configurations) {
            int networkId = this.mClientModeImpl.syncAddOrUpdateNetwork(this.mClientModeImplChannel, configuration);
            if (networkId == -1) {
                Slog.e(TAG, "Restore network failed: " + configuration.configKey());
            } else {
                this.mClientModeImpl.syncEnableNetwork(this.mClientModeImplChannel, networkId, false);
            }
        }
    }

    public void restoreBackupData(byte[] data) {
        enforceNetworkSettingsPermission();
        this.mLog.info("restoreBackupData uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mClientModeImplChannel == null) {
            Slog.e(TAG, "mClientModeImplChannel is not initialized");
            return;
        }
        Slog.d(TAG, "Restoring backup data");
        restoreNetworks(this.mWifiBackupRestore.retrieveConfigurationsFromBackupData(data));
        Slog.d(TAG, "Restored backup data");
    }

    public void restoreSupplicantBackupData(byte[] supplicantData, byte[] ipConfigData) {
        enforceNetworkSettingsPermission();
        this.mLog.trace("restoreSupplicantBackupData uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mClientModeImplChannel == null) {
            Slog.e(TAG, "mClientModeImplChannel is not initialized");
            return;
        }
        Slog.d(TAG, "Restoring supplicant backup data");
        restoreNetworks(this.mWifiBackupRestore.retrieveConfigurationsFromSupplicantBackupData(supplicantData, ipConfigData));
        Slog.d(TAG, "Restored supplicant backup data");
    }

    public boolean setSlaAppState(String pkgName, boolean enabled) {
        enforceChangePermission(pkgName);
        return OppoSlaManager.getInstance(this.mContext).setSlaAppState(pkgName, enabled);
    }

    public boolean getSlaAppState(String pkgName) {
        enforceAccessPermission();
        return OppoSlaManager.getInstance(this.mContext).getSlaAppState(pkgName);
    }

    public String[] getAllSlaAppsAndStates() {
        enforceAccessPermission();
        return OppoSlaManager.getInstance(this.mContext).getAllSlaAppsAndStates();
    }

    public String[] getAllSlaAcceleratedApps() {
        enforceAccessPermission();
        return OppoSlaManager.getInstance(this.mContext).getAllSlaAcceleratedApps();
    }

    public boolean isSlaSupported() {
        enforceAccessPermission();
        return OppoSlaManager.getInstance(this.mContext).isSlaSupported();
    }

    public void setPowerSavingMode(boolean mode) {
        enforceAccessPermission();
        this.mClientModeImpl.setPowerSavingMode(mode);
    }

    public void startSubscriptionProvisioning(OsuProvider provider, IProvisioningCallback callback) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider must not be null");
        } else if (callback == null) {
            throw new IllegalArgumentException("Callback must not be null");
        } else if (!isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
            throw new SecurityException("WifiService: Permission denied");
        } else if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            int uid = Binder.getCallingUid();
            this.mLog.trace("startSubscriptionProvisioning uid=%").c((long) uid).flush();
            if (this.mClientModeImpl.syncStartSubscriptionProvisioning(uid, provider, callback, this.mClientModeImplChannel)) {
                this.mLog.trace("Subscription provisioning started with %").c(provider.toString()).flush();
            }
        } else {
            throw new UnsupportedOperationException("Passpoint not enabled");
        }
    }

    public void registerTrafficStateCallback(IBinder binder, ITrafficStateCallback callback, int callbackIdentifier) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (callback != null) {
            enforceNetworkSettingsPermission();
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("registerTrafficStateCallback uid=%").c((long) Binder.getCallingUid()).flush();
            }
            this.mWifiInjector.getClientModeImplHandler().post(new Runnable(binder, callback, callbackIdentifier) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$GMBdpFwdpKl2pT7F3MX6UEWslIU */
                private final /* synthetic */ IBinder f$1;
                private final /* synthetic */ ITrafficStateCallback f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    WifiServiceImpl.this.lambda$registerTrafficStateCallback$15$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            });
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    public /* synthetic */ void lambda$registerTrafficStateCallback$15$WifiServiceImpl(IBinder binder, ITrafficStateCallback callback, int callbackIdentifier) {
        this.mWifiTrafficPoller.addCallback(binder, callback, callbackIdentifier);
    }

    public void unregisterTrafficStateCallback(int callbackIdentifier) {
        enforceNetworkSettingsPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("unregisterTrafficStateCallback uid=%").c((long) Binder.getCallingUid()).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(callbackIdentifier) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$NuILk8rXYdroC92Oftkmd9NOS8 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$unregisterTrafficStateCallback$16$WifiServiceImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$unregisterTrafficStateCallback$16$WifiServiceImpl(int callbackIdentifier) {
        this.mWifiTrafficPoller.removeCallback(callbackIdentifier);
    }

    public void registerStaStateCallback(IBinder binder, IStaStateCallback callback, int callbackIdentifier) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (callback != null) {
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("registerStaStateCallback uid=%").c((long) Binder.getCallingUid()).flush();
            }
            this.mWifiInjector.getClientModeImplHandler().post(new Runnable(binder, callback, callbackIdentifier) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$RBJ1TWC9ERn4iYYmFc0ndr7BWb8 */
                private final /* synthetic */ IBinder f$1;
                private final /* synthetic */ IStaStateCallback f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    WifiServiceImpl.this.lambda$registerStaStateCallback$17$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            });
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    public /* synthetic */ void lambda$registerStaStateCallback$17$WifiServiceImpl(IBinder binder, IStaStateCallback callback, int callbackIdentifier) {
        this.mWifiStaStateNotifier.addCallback(binder, callback, callbackIdentifier);
    }

    public void unregisterStaStateCallback(int callbackIdentifier) {
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("unregisterStaStateCallback uid=%").c((long) Binder.getCallingUid()).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(callbackIdentifier) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$HbmfsL_OhSsNbj8jem1lxlf2rzE */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$unregisterStaStateCallback$18$WifiServiceImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$unregisterStaStateCallback$18$WifiServiceImpl(int callbackIdentifier) {
        this.mWifiStaStateNotifier.removeCallback(callbackIdentifier);
    }

    private boolean is5GhzSupported() {
        return (getSupportedFeaturesInternal() & 2) == 2;
    }

    private long getSupportedFeaturesInternal() {
        AsyncChannel channel = this.mClientModeImplChannel;
        if (channel != null) {
            long mWifiSupportedFeatures = this.mClientModeImpl.syncGetSupportedFeatures(channel);
            if (1 == Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_5g_band_support", 0)) {
                return mWifiSupportedFeatures | 2;
            }
            return mWifiSupportedFeatures;
        }
        Slog.e(TAG, "mClientModeImplChannel is not initialized");
        return 0;
    }

    private static boolean hasAutomotiveFeature(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.type.automotive");
    }

    public void registerNetworkRequestMatchCallback(IBinder binder, INetworkRequestMatchCallback callback, int callbackIdentifier) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (callback != null) {
            enforceNetworkSettingsPermission();
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("registerNetworkRequestMatchCallback uid=%").c((long) Binder.getCallingUid()).flush();
            }
            this.mWifiInjector.getClientModeImplHandler().post(new Runnable(binder, callback, callbackIdentifier) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$jmmlwuKT1u8TK5iVETSsNHFw_c */
                private final /* synthetic */ IBinder f$1;
                private final /* synthetic */ INetworkRequestMatchCallback f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    WifiServiceImpl.this.lambda$registerNetworkRequestMatchCallback$19$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            });
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    public /* synthetic */ void lambda$registerNetworkRequestMatchCallback$19$WifiServiceImpl(IBinder binder, INetworkRequestMatchCallback callback, int callbackIdentifier) {
        this.mClientModeImpl.addNetworkRequestMatchCallback(binder, callback, callbackIdentifier);
    }

    public void unregisterNetworkRequestMatchCallback(int callbackIdentifier) {
        enforceNetworkSettingsPermission();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("unregisterNetworkRequestMatchCallback uid=%").c((long) Binder.getCallingUid()).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(callbackIdentifier) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$5Kt3n0HG2Nx_6BARbwyDj2vTgDk */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$unregisterNetworkRequestMatchCallback$20$WifiServiceImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$unregisterNetworkRequestMatchCallback$20$WifiServiceImpl(int callbackIdentifier) {
        this.mClientModeImpl.removeNetworkRequestMatchCallback(callbackIdentifier);
    }

    public int addNetworkSuggestions(List<WifiNetworkSuggestion> networkSuggestions, String callingPackageName) {
        if (enforceChangePermission(callingPackageName) != 0) {
            return 2;
        }
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("addNetworkSuggestions uid=%").c((long) Binder.getCallingUid()).flush();
        }
        int callingUid = Binder.getCallingUid();
        GeneralUtil.Mutable<Integer> success = new GeneralUtil.Mutable<>();
        if (!this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(success, networkSuggestions, callingUid, callingPackageName) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$uZHbfApZmSkgQJmzkUQkiqpp7pY */
            private final /* synthetic */ GeneralUtil.Mutable f$1;
            private final /* synthetic */ List f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ String f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$addNetworkSuggestions$21$WifiServiceImpl(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        }, 4000)) {
            Log.e(TAG, "Failed to post runnable to add network suggestions");
            return 1;
        }
        if (success.value.intValue() != 0) {
            Log.e(TAG, "Failed to add network suggestions");
        }
        return success.value.intValue();
    }

    public /* synthetic */ void lambda$addNetworkSuggestions$21$WifiServiceImpl(GeneralUtil.Mutable success, List networkSuggestions, int callingUid, String callingPackageName) {
        success.value = (E) Integer.valueOf(this.mWifiNetworkSuggestionsManager.add(networkSuggestions, callingUid, callingPackageName));
    }

    public int removeNetworkSuggestions(List<WifiNetworkSuggestion> networkSuggestions, String callingPackageName) {
        if (enforceChangePermission(callingPackageName) != 0) {
            return 2;
        }
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("removeNetworkSuggestions uid=%").c((long) Binder.getCallingUid()).flush();
        }
        GeneralUtil.Mutable<Integer> success = new GeneralUtil.Mutable<>();
        if (!this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(success, networkSuggestions, callingPackageName) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$q2WAz1fIqjSuFNlaeJRlIZeU00 */
            private final /* synthetic */ GeneralUtil.Mutable f$1;
            private final /* synthetic */ List f$2;
            private final /* synthetic */ String f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$removeNetworkSuggestions$22$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
            }
        }, 4000)) {
            Log.e(TAG, "Failed to post runnable to remove network suggestions");
            return 1;
        }
        if (success.value.intValue() != 0) {
            Log.e(TAG, "Failed to remove network suggestions");
        }
        return success.value.intValue();
    }

    public /* synthetic */ void lambda$removeNetworkSuggestions$22$WifiServiceImpl(GeneralUtil.Mutable success, List networkSuggestions, String callingPackageName) {
        success.value = (E) Integer.valueOf(this.mWifiNetworkSuggestionsManager.remove(networkSuggestions, callingPackageName));
    }

    public String[] getFactoryMacAddresses() {
        int uid = Binder.getCallingUid();
        if (this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
            List<String> result = new ArrayList<>();
            if (!this.mWifiInjector.getClientModeImplHandler().runWithScissors(new Runnable(result) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$lQnWEG4uBH8rQuGaN5i6DeZ2plo */
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    WifiServiceImpl.this.lambda$getFactoryMacAddresses$23$WifiServiceImpl(this.f$1);
                }
            }, 4000) || result.isEmpty()) {
                return null;
            }
            return (String[]) result.stream().toArray($$Lambda$WifiServiceImpl$EfgfTvi04qWi6e59wo2Ap33XY.INSTANCE);
        }
        throw new SecurityException("App not allowed to get Wi-Fi factory MAC address (uid = " + uid + ")");
    }

    public /* synthetic */ void lambda$getFactoryMacAddresses$23$WifiServiceImpl(List result) {
        String mac = this.mClientModeImpl.getFactoryMacAddress();
        if (mac != null) {
            result.add(mac);
        }
    }

    static /* synthetic */ String[] lambda$getFactoryMacAddresses$24(int x$0) {
        return new String[x$0];
    }

    public void setDeviceMobilityState(int state) {
        this.mContext.enforceCallingPermission("android.permission.WIFI_SET_DEVICE_MOBILITY_STATE", TAG);
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("setDeviceMobilityState uid=% state=%").c((long) Binder.getCallingUid()).c((long) state).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(state) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$2VE1CwNfiLLvsmvFlNa4JMQxp0 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$setDeviceMobilityState$25$WifiServiceImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setDeviceMobilityState$25$WifiServiceImpl(int state) {
        this.mClientModeImpl.setDeviceMobilityState(state);
    }

    public int getMockableCallingUid() {
        return getCallingUid();
    }

    public void startDppAsConfiguratorInitiator(IBinder binder, String enrolleeUri, int selectedNetworkId, int netRole, IDppCallback callback) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (TextUtils.isEmpty(enrolleeUri)) {
            throw new IllegalArgumentException("Enrollee URI must not be null or empty");
        } else if (selectedNetworkId < 0) {
            throw new IllegalArgumentException("Selected network ID invalid");
        } else if (callback != null) {
            int uid = getMockableCallingUid();
            if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
                this.mDppManager.mHandler.post(new Runnable(uid, binder, enrolleeUri, selectedNetworkId, netRole, callback) {
                    /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$BnuvYd3kgsQsz2awpnB66yeV8 */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ IBinder f$2;
                    private final /* synthetic */ String f$3;
                    private final /* synthetic */ int f$4;
                    private final /* synthetic */ int f$5;
                    private final /* synthetic */ IDppCallback f$6;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                        this.f$5 = r6;
                        this.f$6 = r7;
                    }

                    public final void run() {
                        WifiServiceImpl.this.lambda$startDppAsConfiguratorInitiator$26$WifiServiceImpl(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
                    }
                });
                return;
            }
            throw new SecurityException("WifiService: Permission denied");
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    public /* synthetic */ void lambda$startDppAsConfiguratorInitiator$26$WifiServiceImpl(int uid, IBinder binder, String enrolleeUri, int selectedNetworkId, int netRole, IDppCallback callback) {
        this.mDppManager.startDppAsConfiguratorInitiator(uid, binder, enrolleeUri, selectedNetworkId, netRole, callback);
    }

    public void startDppAsEnrolleeInitiator(IBinder binder, String configuratorUri, IDppCallback callback) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (TextUtils.isEmpty(configuratorUri)) {
            throw new IllegalArgumentException("Enrollee URI must not be null or empty");
        } else if (callback != null) {
            int uid = getMockableCallingUid();
            if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
                this.mDppManager.mHandler.post(new Runnable(uid, binder, configuratorUri, callback) {
                    /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$1Wbw_8QENted8X24_fBNuSdWuqg */
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ IBinder f$2;
                    private final /* synthetic */ String f$3;
                    private final /* synthetic */ IDppCallback f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                    }

                    public final void run() {
                        WifiServiceImpl.this.lambda$startDppAsEnrolleeInitiator$27$WifiServiceImpl(this.f$1, this.f$2, this.f$3, this.f$4);
                    }
                });
                return;
            }
            throw new SecurityException("WifiService: Permission denied");
        } else {
            throw new IllegalArgumentException("Callback must not be null");
        }
    }

    public /* synthetic */ void lambda$startDppAsEnrolleeInitiator$27$WifiServiceImpl(int uid, IBinder binder, String configuratorUri, IDppCallback callback) {
        this.mDppManager.startDppAsEnrolleeInitiator(uid, binder, configuratorUri, callback);
    }

    public void stopDppSession() throws RemoteException {
        if (isSettingsOrSuw(Binder.getCallingPid(), Binder.getCallingUid())) {
            this.mDppManager.mHandler.post(new Runnable(getMockableCallingUid()) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$s_zDKbvm8vj9RTKGTZ1m3lY5ZUg */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    WifiServiceImpl.this.lambda$stopDppSession$28$WifiServiceImpl(this.f$1);
                }
            });
            return;
        }
        throw new SecurityException("WifiService: Permission denied");
    }

    public /* synthetic */ void lambda$stopDppSession$28$WifiServiceImpl(int uid) {
        this.mDppManager.stopDppSession(uid);
    }

    public void addOnWifiUsabilityStatsListener(IBinder binder, IOnWifiUsabilityStatsListener listener, int listenerIdentifier) {
        if (binder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (listener != null) {
            this.mContext.enforceCallingPermission("android.permission.WIFI_UPDATE_USABILITY_STATS_SCORE", TAG);
            if (this.mVerboseLoggingEnabled) {
                this.mLog.info("addOnWifiUsabilityStatsListener uid=%").c((long) Binder.getCallingUid()).flush();
            }
            this.mWifiInjector.getClientModeImplHandler().post(new Runnable(binder, listener, listenerIdentifier) {
                /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$9NMuPFnF4_IQgmZZH3Cqzck_s0 */
                private final /* synthetic */ IBinder f$1;
                private final /* synthetic */ IOnWifiUsabilityStatsListener f$2;
                private final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    WifiServiceImpl.this.lambda$addOnWifiUsabilityStatsListener$29$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
                }
            });
        } else {
            throw new IllegalArgumentException("Listener must not be null");
        }
    }

    public /* synthetic */ void lambda$addOnWifiUsabilityStatsListener$29$WifiServiceImpl(IBinder binder, IOnWifiUsabilityStatsListener listener, int listenerIdentifier) {
        this.mWifiMetrics.addOnWifiUsabilityListener(binder, listener, listenerIdentifier);
    }

    public void removeOnWifiUsabilityStatsListener(int listenerIdentifier) {
        this.mContext.enforceCallingPermission("android.permission.WIFI_UPDATE_USABILITY_STATS_SCORE", TAG);
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("removeOnWifiUsabilityStatsListener uid=%").c((long) Binder.getCallingUid()).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(listenerIdentifier) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$9ybBmqGMfiJAsHk3qlFw3QtPOlc */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$removeOnWifiUsabilityStatsListener$30$WifiServiceImpl(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$removeOnWifiUsabilityStatsListener$30$WifiServiceImpl(int listenerIdentifier) {
        this.mWifiMetrics.removeOnWifiUsabilityListener(listenerIdentifier);
    }

    public void updateWifiUsabilityScore(int seqNum, int score, int predictionHorizonSec) {
        this.mContext.enforceCallingPermission("android.permission.WIFI_UPDATE_USABILITY_STATS_SCORE", TAG);
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("updateWifiUsabilityScore uid=% seqNum=% score=% predictionHorizonSec=%").c((long) Binder.getCallingUid()).c((long) seqNum).c((long) score).c((long) predictionHorizonSec).flush();
        }
        this.mWifiInjector.getClientModeImplHandler().post(new Runnable(seqNum, score, predictionHorizonSec) {
            /* class com.android.server.wifi.$$Lambda$WifiServiceImpl$PleaSmIvNWOr3_GBtDmeF11eo */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                WifiServiceImpl.this.lambda$updateWifiUsabilityScore$31$WifiServiceImpl(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$updateWifiUsabilityScore$31$WifiServiceImpl(int seqNum, int score, int predictionHorizonSec) {
        this.mClientModeImpl.updateWifiUsabilityScore(seqNum, score, predictionHorizonSec);
    }

    public void setNetworkCaptiveState(boolean captiveState) {
        enforceAccessPermission();
        WifiConfigManager wifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        if (wifiConfigManager != null) {
            wifiConfigManager.setConfiguredNetworkCaptiveState(captiveState);
        }
    }

    public boolean getNetworkEverCaptiveState() {
        enforceAccessPermission();
        WifiConfigManager wifiConfigManager = this.mWifiInjector.getWifiConfigManager();
        if (wifiConfigManager != null) {
            return wifiConfigManager.getNetworkEverCaptiveState();
        }
        return false;
    }

    public boolean isDualStaSupported() {
        enforceAccessPermission();
        return OppoSlaManager.getInstance(this.mContext).isDualStaSupported();
    }

    public String[] getAllDualStaApps() {
        enforceAccessPermission();
        return OppoSlaManager.getInstance(this.mContext).getAllDualStaApps();
    }

    public int enableDualSta() {
        return enableDualStaByForce(false);
    }

    public int enableDualStaByForce(boolean forceEnable) {
        int uid = Binder.getCallingUid();
        if (uid != 1000) {
            Slog.w(TAG, "enableDualStaByForce invalid caller uid:" + uid);
            return -1;
        }
        Slog.d(TAG, "enableDualStaByForce getCallingUid=" + uid + ", forceEnable=" + forceEnable);
        OppoSlaManager.getInstance(this.mContext).setManualAcCount();
        OppoClientModeImpl2 oppoClientModeImpl2 = this.mOppoClientModeImpl2;
        if (oppoClientModeImpl2 != null) {
            oppoClientModeImpl2.clearDisable();
        }
        return OppoWifiAssistantUtils.getInstance(this.mContext).enableDualSta(forceEnable);
    }

    public void disableDualSta() {
        int uid = Binder.getCallingUid();
        if (UserHandle.getAppId(uid) != 1000) {
            Slog.w(TAG, "disableDualSta invalid caller uid:" + uid);
            return;
        }
        Slog.d(TAG, "disableDualSta getCallingUid=" + uid);
        OppoSlaManager.getInstance(this.mContext).setManualDisableCount();
        this.mWifiInjector.getWifiController().disableOppoWifiSta2();
    }

    public WifiInfo getOppoSta2ConnectionInfo(String callingPackage) {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        if (this.mVerboseLoggingEnabled) {
            this.mLog.info("getConnectionInfo uid=%").c((long) uid).flush();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            WifiInfo result = this.mOppoClientModeImpl2.syncRequestConnectionInfo();
            boolean hideDefaultMacAddress = true;
            boolean hideBssidAndSsid = true;
            try {
                if (this.mWifiInjector.getWifiPermissionsWrapper().getLocalMacAddressPermission(uid) == 0) {
                    hideDefaultMacAddress = false;
                }
                this.mWifiPermissionsUtil.enforceCanAccessScanResults(callingPackage, uid);
                hideBssidAndSsid = false;
            } catch (RemoteException e) {
                Log.e(TAG, "Error checking receiver permission", e);
            } catch (SecurityException e2) {
            }
            if (hideDefaultMacAddress) {
                result.setMacAddress("02:00:00:00:00:00");
            }
            if (hideBssidAndSsid) {
                result.setBSSID("02:00:00:00:00:00");
                result.setSSID(WifiSsid.createFromHex((String) null));
            }
            if (this.mVerboseLoggingEnabled && (hideBssidAndSsid || hideDefaultMacAddress)) {
                WifiLog wifiLog = this.mLog;
                wifiLog.v("getConnectionInfo: hideBssidAndSSid=" + hideBssidAndSsid + ", hideDefaultMacAddress=" + hideDefaultMacAddress);
            }
            return result;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public String getOppoSta2CurConfigKey() {
        enforceAccessPermission();
        OppoClientModeImpl2 oppoClientModeImpl2 = this.mOppoClientModeImpl2;
        if (oppoClientModeImpl2 != null) {
            return oppoClientModeImpl2.getOppoSta2CurConfigKey();
        }
        return null;
    }

    public boolean isOppoSta2DupDhcp() {
        enforceAccessPermission();
        return this.mOppoClientModeImpl2.isDupDhcp();
    }

    public void notifyGameModeState(boolean state, String packageName) {
        StringBuilder sb = new StringBuilder();
        sb.append("notifyGameModeState ");
        sb.append(state ? "enter " : "exit ");
        sb.append(packageName);
        Log.d(TAG, sb.toString());
    }
}
