package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.database.ContentObserver;
import android.net.DhcpInfo;
import android.net.DhcpResults;
import android.net.IpConfiguration.IpAssignment;
import android.net.Network;
import android.net.NetworkUtils;
import android.net.StaticIpConfiguration;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.ScanSettings;
import android.net.wifi.WifiActivityEnergyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConnectionStatistics;
import android.net.wifi.WifiDevice;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiLinkLayerStats;
import android.net.wifi.WifiScanner;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.AsyncChannel;
import com.android.server.wifi.LocalOnlyHotspotRequestInfo.RequestingApplicationDeathCallback;
import com.android.server.wifi.hotspot2.PasspointProvider;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.util.WifiHandler;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.mediatek.server.wifi.OppoWifiServiceImpl;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WifiServiceImpl extends OppoWifiServiceImpl {
    private static final int BACKGROUND_IMPORTANCE_CUTOFF = 125;
    private static boolean DBG = false;
    private static final long DEFAULT_SCAN_BACKGROUND_THROTTLE_INTERVAL_MS = 1800000;
    private static final String DUMP_ARG_SET_IPREACH_DISCONNECT = "set-ipreach-disconnect";
    private static final String DUMP_ARG_SET_IPREACH_DISCONNECT_DISABLED = "disabled";
    private static final String DUMP_ARG_SET_IPREACH_DISCONNECT_ENABLED = "enabled";
    private static final String TAG = "WifiService";
    private static final boolean VDBG = false;
    private final ActivityManager mActivityManager;
    private final AppOpsManager mAppOps;
    private long mBackgroundThrottleInterval;
    private final ArraySet<String> mBackgroundThrottlePackageWhitelist = new ArraySet();
    private final WifiCertManager mCertManager;
    private ClientHandler mClientHandler;
    private final Clock mClock;
    private final Context mContext;
    private final WifiCountryCode mCountryCode;
    private final FrameworkFacade mFacade;
    private final FrameworkFacade mFrameworkFacade;
    @GuardedBy("mLocalOnlyHotspotRequests")
    private final ConcurrentHashMap<String, Integer> mIfaceIpModes;
    boolean mInIdleMode;
    private boolean mIsControllerStarted = false;
    private boolean mIsFactoryResetOn = false;
    private final ArrayMap<String, Long> mLastScanTimestamps;
    @GuardedBy("mLocalOnlyHotspotRequests")
    private WifiConfiguration mLocalOnlyHotspotConfig = null;
    @GuardedBy("mLocalOnlyHotspotRequests")
    private final HashMap<Integer, LocalOnlyHotspotRequestInfo> mLocalOnlyHotspotRequests;
    private WifiLog mLog;
    private final boolean mPermissionReviewRequired;
    private final PowerManager mPowerManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int i = 1;
            String action = intent.getAction();
            WifiController -get6;
            if (action.equals("android.intent.action.SCREEN_ON")) {
                WifiServiceImpl.this.mWifiController.sendMessage(155650);
            } else if (action.equals("android.intent.action.USER_PRESENT")) {
                WifiServiceImpl.this.mWifiController.sendMessage(155660);
            } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                WifiServiceImpl.this.mWifiController.sendMessage(155651);
            } else if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                WifiServiceImpl.this.mWifiController.sendMessage(155652, intent.getIntExtra("plugged", 0), 0, null);
            } else if (action.equals("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED")) {
                WifiServiceImpl.this.mWifiStateMachine.sendBluetoothAdapterStateChange(intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", 0));
            } else if (action.equals("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED")) {
                boolean emergencyMode = intent.getBooleanExtra("phoneinECMState", false);
                -get6 = WifiServiceImpl.this.mWifiController;
                if (!emergencyMode) {
                    i = 0;
                }
                -get6.sendMessage(155649, i, 0);
            } else if (action.equals("android.intent.action.EMERGENCY_CALL_STATE_CHANGED")) {
                boolean inCall = intent.getBooleanExtra("phoneInEmergencyCall", false);
                -get6 = WifiServiceImpl.this.mWifiController;
                if (!inCall) {
                    i = 0;
                }
                -get6.sendMessage(155662, i, 0);
            } else if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                WifiServiceImpl.this.handleIdleModeChanged();
            } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED") && intent.getIntExtra("wifi_state", 4) == 3 && WifiServiceImpl.this.mIsFactoryResetOn) {
                WifiServiceImpl.this.resetWifiNetworks();
                WifiServiceImpl.this.mIsFactoryResetOn = false;
            }
        }
    };
    boolean mScanPending;
    final WifiSettingsStore mSettingsStore;
    private SoftApStateMachine mSoftApStateMachine;
    private WifiTrafficPoller mTrafficPoller;
    private final UserManager mUserManager;
    private WifiApConfigStore mWifiApConfigStore;
    private final WifiBackupRestore mWifiBackupRestore;
    private WifiController mWifiController;
    private final WifiInjector mWifiInjector;
    private final WifiLockManager mWifiLockManager;
    private final WifiMetrics mWifiMetrics;
    private final WifiMulticastLockManager mWifiMulticastLockManager;
    private WifiPermissionsUtil mWifiPermissionsUtil;
    private WifiScanner mWifiScanner;
    final WifiStateMachine mWifiStateMachine;
    private AsyncChannel mWifiStateMachineChannel;
    WifiStateMachineHandler mWifiStateMachineHandler;
    private int scanRequestCounter = 0;

    private class ClientHandler extends WifiHandler {
        ClientHandler(String tag, Looper looper) {
            super(tag, looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WifiConfiguration config;
            switch (msg.what) {
                case 69632:
                    if (msg.arg1 == 0) {
                        if (WifiServiceImpl.DBG) {
                            Slog.d(WifiServiceImpl.TAG, "New client listening to asynchronous messages");
                        }
                        WifiServiceImpl.this.mTrafficPoller.addClient(msg.replyTo);
                        return;
                    }
                    Slog.e(WifiServiceImpl.TAG, "Client connection failure, error=" + msg.arg1);
                    return;
                case 69633:
                    WifiServiceImpl.this.mFrameworkFacade.makeWifiAsyncChannel(WifiServiceImpl.TAG).connect(WifiServiceImpl.this.mContext, this, msg.replyTo);
                    return;
                case 69636:
                    if (msg.arg1 == 2) {
                        if (WifiServiceImpl.DBG) {
                            Slog.d(WifiServiceImpl.TAG, "Send failed, client connection lost");
                        }
                    } else if (WifiServiceImpl.DBG) {
                        Slog.d(WifiServiceImpl.TAG, "Client connection lost with reason: " + msg.arg1);
                    }
                    WifiServiceImpl.this.mTrafficPoller.removeClient(msg.replyTo);
                    return;
                case 151553:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151554)) {
                        config = msg.obj;
                        int networkId = msg.arg1;
                        Slog.d(WifiServiceImpl.TAG, "CONNECT  nid=" + Integer.toString(networkId) + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        if (config != null) {
                            if (WifiServiceImpl.DBG) {
                                Slog.d(WifiServiceImpl.TAG, "Connect with config " + config);
                            }
                            WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                            return;
                        } else if (config != null || networkId == -1) {
                            Slog.e(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring invalid msg=" + msg);
                            replyFailed(msg, 151554, 8);
                            return;
                        } else {
                            if (WifiServiceImpl.DBG) {
                                Slog.d(WifiServiceImpl.TAG, "Connect with networkId " + networkId);
                            }
                            WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                            return;
                        }
                    }
                    return;
                case 151556:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151557)) {
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                    return;
                case 151559:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151560)) {
                        config = (WifiConfiguration) msg.obj;
                        Slog.d(WifiServiceImpl.TAG, "SAVE nid=" + Integer.toString(msg.arg1) + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                        if (config != null) {
                            if (WifiServiceImpl.DBG) {
                                Slog.d(WifiServiceImpl.TAG, "Save network with config " + config);
                            }
                            WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                            return;
                        }
                        Slog.e(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring invalid msg=" + msg);
                        replyFailed(msg, 151560, 8);
                        return;
                    }
                    return;
                case 151562:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151564)) {
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                    return;
                case 151566:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151567)) {
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                    return;
                case 151569:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151570)) {
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                    return;
                case 151572:
                    if (checkChangePermissionAndReplyIfNotAuthorized(msg, 151574)) {
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                    return;
                default:
                    Slog.d(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring msg=" + msg);
                    return;
            }
        }

        private boolean checkChangePermissionAndReplyIfNotAuthorized(Message msg, int replyWhat) {
            if (WifiServiceImpl.this.mWifiPermissionsUtil.checkChangePermission(msg.sendingUid)) {
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

    public final class LocalOnlyRequestorCallback implements RequestingApplicationDeathCallback {
        public void onLocalOnlyHotspotRequestorDeath(LocalOnlyHotspotRequestInfo requestor) {
            WifiServiceImpl.this.unregisterCallingAppAndStopLocalOnlyHotspot(requestor);
        }
    }

    class TdlsTask extends AsyncTask<TdlsTaskParams, Integer, Integer> {
        TdlsTask() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:35:0x0099 A:{SYNTHETIC, Splitter: B:35:0x0099} */
        /* JADX WARNING: Removed duplicated region for block: B:40:0x00a2 A:{SYNTHETIC, Splitter: B:40:0x00a2} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected Integer doInBackground(TdlsTaskParams... params) {
            Throwable th;
            TdlsTaskParams param = params[0];
            String remoteIpAddress = param.remoteIpAddress.trim();
            boolean enable = param.enable;
            String macAddress = null;
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader("/proc/net/arp"));
                try {
                    String readLine = reader2.readLine();
                    while (true) {
                        readLine = reader2.readLine();
                        if (readLine == null) {
                            break;
                        }
                        String[] tokens = readLine.split("[ ]+");
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
                        Slog.w(WifiServiceImpl.TAG, "Did not find remoteAddress {" + remoteIpAddress + "} in " + "/proc/net/arp");
                    } else {
                        WifiServiceImpl.this.enableTdlsWithMacAddress(macAddress, enable);
                    }
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e) {
                        }
                    }
                    reader = reader2;
                } catch (FileNotFoundException e2) {
                    reader = reader2;
                } catch (IOException e3) {
                    reader = reader2;
                    Slog.e(WifiServiceImpl.TAG, "Could not read /proc/net/arp to lookup mac address");
                    if (reader != null) {
                    }
                    return Integer.valueOf(0);
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                try {
                    Slog.e(WifiServiceImpl.TAG, "Could not open /proc/net/arp to lookup mac address");
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e6) {
                        }
                    }
                    return Integer.valueOf(0);
                } catch (Throwable th3) {
                    th = th3;
                    if (reader != null) {
                    }
                    throw th;
                }
            } catch (IOException e7) {
                Slog.e(WifiServiceImpl.TAG, "Could not read /proc/net/arp to lookup mac address");
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e8) {
                    }
                }
                return Integer.valueOf(0);
            }
            return Integer.valueOf(0);
        }
    }

    class TdlsTaskParams {
        public boolean enable;
        public String remoteIpAddress;

        TdlsTaskParams() {
        }
    }

    private class WifiStateMachineHandler extends WifiHandler {
        private AsyncChannel mWsmChannel;

        WifiStateMachineHandler(String tag, Looper looper, AsyncChannel asyncChannel) {
            super(tag, looper);
            this.mWsmChannel = asyncChannel;
            this.mWsmChannel.connect(WifiServiceImpl.this.mContext, this, WifiServiceImpl.this.mWifiStateMachine.getHandler());
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 69632:
                    if (msg.arg1 == 0) {
                        WifiServiceImpl.this.mWifiStateMachineChannel = this.mWsmChannel;
                        return;
                    }
                    Slog.e(WifiServiceImpl.TAG, "WifiStateMachine connection failure, error=" + msg.arg1);
                    WifiServiceImpl.this.mWifiStateMachineChannel = null;
                    return;
                case 69636:
                    Slog.e(WifiServiceImpl.TAG, "WifiStateMachine channel lost, msg.arg1 =" + msg.arg1);
                    WifiServiceImpl.this.mWifiStateMachineChannel = null;
                    this.mWsmChannel.connect(WifiServiceImpl.this.mContext, this, WifiServiceImpl.this.mWifiStateMachine.getHandler());
                    return;
                default:
                    Slog.d(WifiServiceImpl.TAG, "WifiStateMachineHandler.handleMessage ignoring msg=" + msg);
                    return;
            }
        }
    }

    public WifiServiceImpl(Context context, WifiInjector wifiInjector, AsyncChannel asyncChannel) {
        boolean z;
        super(context, wifiInjector, asyncChannel);
        this.mContext = context;
        this.mWifiInjector = wifiInjector;
        this.mClock = wifiInjector.getClock();
        this.mFacade = this.mWifiInjector.getFrameworkFacade();
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mTrafficPoller = this.mWifiInjector.getWifiTrafficPoller();
        this.mUserManager = this.mWifiInjector.getUserManager();
        this.mCountryCode = this.mWifiInjector.getWifiCountryCode();
        this.mWifiStateMachine = this.mWifiInjector.getWifiStateMachine();
        this.mWifiStateMachine.setTrafficPoller(this.mTrafficPoller);
        this.mWifiStateMachine.enableRssiPolling(true);
        this.mSettingsStore = this.mWifiInjector.getWifiSettingsStore();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mCertManager = this.mWifiInjector.getWifiCertManager();
        this.mWifiLockManager = this.mWifiInjector.getWifiLockManager();
        this.mWifiMulticastLockManager = this.mWifiInjector.getWifiMulticastLockManager();
        HandlerThread wifiServiceHandlerThread = this.mWifiInjector.getWifiServiceHandlerThread();
        this.mClientHandler = new ClientHandler(TAG, wifiServiceHandlerThread.getLooper());
        this.mWifiStateMachineHandler = new WifiStateMachineHandler(TAG, wifiServiceHandlerThread.getLooper(), asyncChannel);
        this.mWifiController = this.mWifiInjector.getWifiController();
        this.mWifiBackupRestore = this.mWifiInjector.getWifiBackupRestore();
        this.mWifiApConfigStore = this.mWifiInjector.getWifiApConfigStore();
        if (this.mWifiApConfigStore.getStaSapConcurrency()) {
            this.mWifiStateMachine.setStaSoftApConcurrency(true);
            this.mSoftApStateMachine = this.mWifiStateMachine.getSoftApStateMachine();
            if (this.mWifiApConfigStore.getSapInterface() != null) {
                this.mSoftApStateMachine.setSoftApInterfaceName(this.mWifiApConfigStore.getSapInterface());
            }
            this.mSoftApStateMachine.setSoftApChannel(this.mWifiApConfigStore.getConfigFileChannel());
            this.mWifiController.setSoftApStateMachine(this.mSoftApStateMachine, true);
        } else if (this.mWifiApConfigStore.isSapNewIntfRequired() && this.mWifiApConfigStore.getSapInterface() != null) {
            this.mWifiStateMachine.setNewSapInterface(this.mWifiApConfigStore.getSapInterface());
        }
        if (Build.PERMISSIONS_REVIEW_REQUIRED) {
            z = true;
        } else {
            z = context.getResources().getBoolean(17956992);
        }
        this.mPermissionReviewRequired = z;
        this.mWifiPermissionsUtil = this.mWifiInjector.getWifiPermissionsUtil();
        this.mLog = this.mWifiInjector.makeLog(TAG);
        this.mFrameworkFacade = wifiInjector.getFrameworkFacade();
        this.mLastScanTimestamps = new ArrayMap();
        updateBackgroundThrottleInterval();
        updateBackgroundThrottlingWhitelist();
        this.mIfaceIpModes = new ConcurrentHashMap();
        this.mLocalOnlyHotspotRequests = new HashMap();
        enableVerboseLoggingInternal(getVerboseLoggingLevel());
    }

    public void setWifiHandlerLogForTest(WifiLog log) {
        this.mClientHandler.setWifiLog(log);
    }

    private boolean checkDualSimActive() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm == null) {
            Log.i(TAG, "checkDualSimActive() is false, tm == null");
            return false;
        }
        int phoneCount = tm.getPhoneCount();
        if (phoneCount < 2) {
            Log.i(TAG, "checkDualSimActive() is false, phoneCount=" + phoneCount + " slot0State=" + tm.getSimState(0));
            return false;
        }
        int slot0State = tm.getSimState(0);
        int slot1State = tm.getSimState(1);
        if (slot0State == 5 || slot1State == 5) {
            Log.i(TAG, "checkDualSimActive() is true, phoneCount=" + phoneCount + " slot0State=" + slot0State + " slot1State=" + slot1State);
            return true;
        }
        Log.i(TAG, "checkDualSimActive() is false, slot0State=" + slot0State + " slot1State=" + slot1State);
        return false;
    }

    public void checkAndStartWifi() {
        if (this.mFrameworkFacade.inStorageManagerCryptKeeperBounce()) {
            Log.d(TAG, "Device still encrypted. Need to restart SystemServer.  Do not start wifi.");
            return;
        }
        this.mWifiStateMachine.initRomupdateHelperBroadcastReceiver();
        boolean wifiEnabled = this.mSettingsStore.isWifiToggleEnabled();
        Slog.i(TAG, "WifiService starting up with Wi-Fi " + (wifiEnabled ? DUMP_ARG_SET_IPREACH_DISCONNECT_ENABLED : DUMP_ARG_SET_IPREACH_DISCONNECT_DISABLED));
        registerForScanModeChange();
        registerForBackgroundThrottleChanges();
        this.mContext.registerReceiver(new BroadcastReceiver() {
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
            public void onReceive(Context context, Intent intent) {
                String state = intent.getStringExtra("ss");
                if ("ABSENT".equals(state)) {
                    if (WifiServiceImpl.this.checkDualSimActive()) {
                        Log.d(WifiServiceImpl.TAG, "Not resetting networks as other SIM may active");
                        return;
                    }
                    Log.d(WifiServiceImpl.TAG, "resetting networks because SIM was removed");
                    WifiServiceImpl.this.mWifiStateMachine.resetSimAuthNetworks(false);
                    Log.d(WifiServiceImpl.TAG, "resetting country code because SIM is removed");
                    WifiServiceImpl.this.mCountryCode.simCardRemoved();
                } else if ("LOADED".equals(state)) {
                    Log.d(WifiServiceImpl.TAG, "resetting networks because SIM was loaded");
                    WifiServiceImpl.this.mWifiStateMachine.resetSimAuthNetworks(true);
                }
            }
        }, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WifiServiceImpl.this.handleWifiApStateChange(intent.getIntExtra("wifi_state", 11), intent.getIntExtra("previous_wifi_state", 11), intent.getIntExtra("wifi_ap_error_code", -1), intent.getStringExtra("wifi_ap_interface_name"), intent.getIntExtra("wifi_ap_mode", -1));
            }
        }, new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED"));
        registerForBroadcasts();
        registerForPackageOrUserRemoval();
        this.mInIdleMode = this.mPowerManager.isDeviceIdleMode();
        Log.d(TAG, "=qcdbg= mWifiStateMachine.syncInitialize()");
        if (!this.mWifiStateMachine.syncInitialize(this.mWifiStateMachineChannel)) {
            Log.wtf(TAG, "Failed to initialize WifiStateMachine");
        }
        this.mWifiController.start();
        this.mIsControllerStarted = true;
        if (wifiEnabled) {
            try {
                setWifiEnabled(this.mContext.getPackageName(), wifiEnabled);
            } catch (RemoteException e) {
            }
        }
    }

    public void handleUserSwitch(int userId) {
        this.mWifiStateMachine.handleUserSwitch(userId);
    }

    public void handleUserUnlock(int userId) {
        this.mWifiStateMachine.handleUserUnlock(userId);
    }

    public void handleUserStop(int userId) {
        this.mWifiStateMachine.handleUserStop(userId);
    }

    /* JADX WARNING: Missing block: B:20:0x0069, code:
            if (r11 == null) goto L_0x0084;
     */
    /* JADX WARNING: Missing block: B:21:0x006b, code:
            r4 = new android.net.wifi.ScanSettings(r11);
     */
    /* JADX WARNING: Missing block: B:22:0x0074, code:
            if (r4.isValid() != false) goto L_0x0083;
     */
    /* JADX WARNING: Missing block: B:23:0x0076, code:
            android.util.Slog.e(TAG, "invalid scan setting");
     */
    /* JADX WARNING: Missing block: B:24:0x007f, code:
            return;
     */
    /* JADX WARNING: Missing block: B:28:0x0083, code:
            r11 = r4;
     */
    /* JADX WARNING: Missing block: B:29:0x0084, code:
            if (r12 == null) goto L_0x008c;
     */
    /* JADX WARNING: Missing block: B:30:0x0086, code:
            enforceWorkSourcePermission();
            r12.clearNames();
     */
    /* JADX WARNING: Missing block: B:31:0x008c, code:
            if (r12 != null) goto L_0x009d;
     */
    /* JADX WARNING: Missing block: B:33:0x0092, code:
            if (android.os.Binder.getCallingUid() < 0) goto L_0x009d;
     */
    /* JADX WARNING: Missing block: B:34:0x0094, code:
            r12 = new android.os.WorkSource(android.os.Binder.getCallingUid());
     */
    /* JADX WARNING: Missing block: B:35:0x009d, code:
            r5 = r10.mWifiStateMachine;
            r6 = android.os.Binder.getCallingUid();
            r7 = r10.scanRequestCounter;
            r10.scanRequestCounter = r7 + 1;
            r5.startScan(r6, r7, r11, r12);
     */
    /* JADX WARNING: Missing block: B:36:0x00ac, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startScan(ScanSettings settings, WorkSource workSource, String packageName) {
        enforceChangePermission();
        this.mLog.info("startScan uid=%").c((long) Binder.getCallingUid()).flush();
        if (isRequestFromBackground(packageName)) {
            long lastScanMs = ((Long) this.mLastScanTimestamps.getOrDefault(packageName, Long.valueOf(0))).longValue();
            long elapsedRealtime = this.mClock.getElapsedSinceBootMillis();
            if (lastScanMs == 0 || elapsedRealtime - lastScanMs >= this.mBackgroundThrottleInterval) {
                this.mLastScanTimestamps.put(packageName, Long.valueOf(elapsedRealtime));
            } else {
                sendFailedScanBroadcast();
                return;
            }
        }
        synchronized (this) {
            if (this.mWifiScanner == null) {
                this.mWifiScanner = this.mWifiInjector.getWifiScanner();
            }
            if (this.mInIdleMode) {
                sendFailedScanBroadcast();
                this.mScanPending = true;
            }
        }
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

    private boolean isRequestFromBackground(String packageName) {
        boolean z = false;
        if (Binder.getCallingUid() == OppoManuConnectManager.UID_DEFAULT || Binder.getCallingUid() == 1010) {
            return false;
        }
        this.mAppOps.checkPackage(Binder.getCallingUid(), packageName);
        if (this.mBackgroundThrottlePackageWhitelist.contains(packageName)) {
            return false;
        }
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            if (this.mActivityManager.getPackageImportance(packageName) > BACKGROUND_IMPORTANCE_CUTOFF) {
                z = true;
            }
            Binder.restoreCallingIdentity(callingIdentity);
            return z;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    public String getCurrentNetworkWpsNfcConfigurationToken() {
        enforceConnectivityInternalPermission();
        this.mLog.info("getCurrentNetworkWpsNfcConfigurationToken uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.syncGetCurrentNetworkWpsNfcConfigurationToken();
    }

    void handleIdleModeChanged() {
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
            startScan(null, null, "");
        }
    }

    private boolean checkNetworkSettingsPermission(int pid, int uid) {
        return this.mContext.checkPermission("android.permission.NETWORK_SETTINGS", pid, uid) == 0;
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

    private void enforceChangePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", TAG);
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

    private boolean isStrictOpEnable() {
        return SystemProperties.getBoolean("persist.vendor.strict_op_enable", false);
    }

    private void enforceConnectivityInternalPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "ConnectivityService");
    }

    private void enforceLocationPermission(String pkgName, int uid) {
        this.mWifiPermissionsUtil.enforceLocationPermission(pkgName, uid);
    }

    public synchronized boolean setWifiEnabled(String packageName, boolean enable) throws RemoteException {
        enforceChangePermission();
        if (SystemProperties.getInt("persist.sys.wifi_disable", 0) == 1) {
            Slog.d(TAG, "setWifiEnabled return false for spaying");
            return false;
        }
        Slog.d(TAG, "setWifiEnabled: " + enable + " pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + ", package=" + packageName);
        this.mLog.info("setWifiEnabled package=% uid=% enable=%").c(packageName).c((long) Binder.getCallingUid()).c(enable).flush();
        boolean isFromSettings = checkNetworkSettingsPermission(Binder.getCallingPid(), Binder.getCallingUid());
        if (!this.mSettingsStore.isAirplaneModeOn() || (isFromSettings ^ 1) == 0) {
            boolean apEnabled = this.mWifiStateMachine.syncGetWifiApState() != 11;
            if (!apEnabled || (isFromSettings ^ 1) == 0) {
                if (isStrictOpEnable()) {
                    String callPackage = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
                    if (enable && Binder.getCallingUid() >= 10000 && (callPackage.startsWith("android.uid.systemui:") ^ 1) != 0 && (callPackage.startsWith("android.uid.system:") ^ 1) != 0 && ((AppOpsManager) this.mContext.getSystemService(AppOpsManager.class)).noteOp(70, Binder.getCallingUid(), callPackage) == 1) {
                        return false;
                    }
                }
                long ident = Binder.clearCallingIdentity();
                try {
                    if (this.mSettingsStore.handleWifiToggled(enable)) {
                        Binder.restoreCallingIdentity(ident);
                        if (this.mIsControllerStarted) {
                            if (this.mPermissionReviewRequired) {
                                int wiFiEnabledState = getWifiEnabledState();
                                if (enable) {
                                    if ((wiFiEnabledState == 0 || wiFiEnabledState == 1) && startConsentUi(packageName, Binder.getCallingUid(), "android.net.wifi.action.REQUEST_ENABLE")) {
                                        return true;
                                    }
                                } else if (wiFiEnabledState == 2 || wiFiEnabledState == 3) {
                                    if (startConsentUi(packageName, Binder.getCallingUid(), "android.net.wifi.action.REQUEST_DISABLE")) {
                                        return true;
                                    }
                                }
                            }
                            if (enable) {
                                if (this.mWifiApConfigStore.getDualSapStatus()) {
                                    stopSoftAp();
                                }
                            }
                            if (apEnabled && isExtendingNetworkCoverage()) {
                                this.mWifiController.sendMessage(155658, 0, 0);
                            }
                            this.mWifiController.sendMessage(155656);
                            return true;
                        }
                        Slog.e(TAG, "WifiController is not yet started, abort setWifiEnabled");
                        return false;
                    }
                    Binder.restoreCallingIdentity(ident);
                    return true;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                this.mLog.info("setWifiEnabled SoftAp not disabled: only Settings can enable wifi").flush();
                return false;
            }
        }
        this.mLog.info("setWifiEnabled in Airplane mode: only Settings can enable wifi").flush();
        return false;
    }

    public int getWifiEnabledState() {
        enforceAccessPermission();
        if (DBG) {
            this.mLog.info("getWifiEnabledState uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mWifiStateMachine.syncGetWifiState();
    }

    public void setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        int i = 1;
        enforceChangePermission();
        if (SystemProperties.getInt("persist.sys.ap_disable", 0) == 1) {
            Slog.d(TAG, "setWifiApEnabled return for spaying");
            return;
        }
        this.mWifiPermissionsUtil.enforceTetherChangePermission(this.mContext);
        this.mLog.info("setWifiApEnabled uid=% enable=%").c((long) Binder.getCallingUid()).c(enabled).flush();
        if (this.mUserManager.hasUserRestriction("no_config_tethering")) {
            throw new SecurityException("DISALLOW_CONFIG_TETHERING is enabled for this user.");
        }
        startDualSapMode(enabled);
        if (wifiConfig == null || isValid(wifiConfig)) {
            Slog.d(TAG, "setWifiApEnabled: " + enabled + " pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            SoftApModeConfiguration softApConfig = new SoftApModeConfiguration(-1, wifiConfig);
            WifiController wifiController = this.mWifiController;
            if (!enabled) {
                i = 0;
            }
            wifiController.sendMessage(155658, i, 0, softApConfig);
        } else {
            Slog.e(TAG, "Invalid WifiConfiguration");
        }
    }

    public int getWifiApEnabledState() {
        enforceAccessPermission();
        this.mLog.info("getWifiApEnabledState uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.syncGetWifiApState();
    }

    public void updateInterfaceIpState(String ifaceName, int mode) {
        enforceNetworkStackPermission();
        this.mClientHandler.post(new -$Lambda$ajG-TMshB5D_BrXpSiGRWlXJ7-M((byte) 0, mode, this, ifaceName));
    }

    /* JADX WARNING: Missing block: B:11:0x005b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateInterfaceIpStateInternal(String ifaceName, int mode) {
        synchronized (this.mLocalOnlyHotspotRequests) {
            Integer previousMode = Integer.valueOf(-1);
            if (ifaceName != null) {
                previousMode = (Integer) this.mIfaceIpModes.put(ifaceName, Integer.valueOf(mode));
            }
            Slog.d(TAG, "updateInterfaceIpState: ifaceName=" + ifaceName + " mode=" + mode + " previous mode= " + previousMode);
            switch (mode) {
                case -1:
                    if (ifaceName == null) {
                        this.mIfaceIpModes.clear();
                        return;
                    }
                    break;
                case 0:
                    sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(2);
                    updateInterfaceIpStateInternal(null, -1);
                    break;
                case 1:
                    sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(3);
                    break;
                case 2:
                    if (!this.mLocalOnlyHotspotRequests.isEmpty()) {
                        sendHotspotStartedMessageToAllLOHSRequestInfoEntriesLocked();
                        break;
                    }
                    stopSoftAp();
                    updateInterfaceIpStateInternal(null, -1);
                    return;
                default:
                    this.mLog.warn("updateInterfaceIpStateInternal: unknown mode %").c((long) mode).flush();
                    break;
            }
        }
    }

    public boolean startSoftAp(WifiConfiguration wifiConfig) {
        enforceNetworkStackPermission();
        if (SystemProperties.getInt("persist.sys.ap_disable", 0) == 1) {
            Slog.d(TAG, "setWifiApEnabled return for spaying");
            return false;
        }
        boolean startSoftApInternal;
        Slog.d(TAG, "startSoftAp:  pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (!this.mLocalOnlyHotspotRequests.isEmpty()) {
                stopSoftApInternal();
            }
            startSoftApInternal = startSoftApInternal(wifiConfig, 1);
        }
        return startSoftApInternal;
    }

    private boolean startSoftApInternal(WifiConfiguration wifiConfig, int mode) {
        this.mLog.trace("startSoftApInternal uid=% mode=%").c((long) Binder.getCallingUid()).c((long) mode).flush();
        startDualSapMode(true);
        if (wifiConfig == null || isValid(wifiConfig)) {
            this.mWifiController.sendMessage(155658, 1, 0, new SoftApModeConfiguration(mode, wifiConfig));
            return true;
        }
        Slog.e(TAG, "Invalid WifiConfiguration");
        return false;
    }

    public boolean stopSoftAp() {
        boolean stopSoftApInternal;
        enforceNetworkStackPermission();
        this.mLog.info("stopSoftAp uid=%").c((long) Binder.getCallingUid()).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (!this.mLocalOnlyHotspotRequests.isEmpty()) {
                this.mLog.trace("Call to stop Tethering while LOHS is active, Registered LOHS callers will be updated when softap stopped.");
            }
            stopSoftApInternal = stopSoftApInternal();
        }
        return stopSoftApInternal;
    }

    private boolean stopSoftApInternal() {
        this.mLog.trace("stopSoftApInternal uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiApConfigStore.getDualSapStatus()) {
            return startDualSapMode(false);
        }
        this.mWifiController.sendMessage(155658, 0, 0);
        return true;
    }

    private void handleWifiApStateChange(int currentState, int previousState, int errorCode, String ifaceName, int mode) {
        Slog.d(TAG, "handleWifiApStateChange: currentState=" + currentState + " previousState=" + previousState + " errorCode= " + errorCode + " ifaceName=" + ifaceName + " mode=" + mode);
        if (currentState == 14) {
            synchronized (this.mLocalOnlyHotspotRequests) {
                int errorToReport = 2;
                if (errorCode == 1) {
                    errorToReport = 1;
                }
                sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(errorToReport);
                updateInterfaceIpStateInternal(null, -1);
            }
        } else if (currentState == 10 || currentState == 11) {
            synchronized (this.mLocalOnlyHotspotRequests) {
                if (this.mIfaceIpModes.contains(Integer.valueOf(2))) {
                    sendHotspotStoppedMessageToAllLOHSRequestInfoEntriesLocked();
                } else {
                    sendHotspotFailedMessageToAllLOHSRequestInfoEntriesLocked(2);
                }
                updateInterfaceIpState(null, -1);
            }
        }
    }

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

    private void sendHotspotStartedMessageToAllLOHSRequestInfoEntriesLocked() {
        for (LocalOnlyHotspotRequestInfo requestor : this.mLocalOnlyHotspotRequests.values()) {
            try {
                requestor.sendHotspotStartedMessage(this.mLocalOnlyHotspotConfig);
            } catch (RemoteException e) {
            }
        }
    }

    void registerLOHSForTest(int pid, LocalOnlyHotspotRequestInfo request) {
        this.mLocalOnlyHotspotRequests.put(Integer.valueOf(pid), request);
    }

    public int startLocalOnlyHotspot(Messenger messenger, IBinder binder, String packageName) {
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        enforceChangePermission();
        enforceLocationPermission(packageName, uid);
        if (this.mSettingsStore.getLocationModeSetting(this.mContext) == 0) {
            throw new SecurityException("Location mode is not enabled.");
        } else if (this.mUserManager.hasUserRestriction("no_config_tethering")) {
            return 4;
        } else {
            try {
                if (!this.mFrameworkFacade.isAppForeground(uid)) {
                    return 3;
                }
                this.mLog.info("startLocalOnlyHotspot uid=% pid=%").c((long) uid).c((long) pid).flush();
                synchronized (this.mLocalOnlyHotspotRequests) {
                    if (this.mIfaceIpModes.contains(Integer.valueOf(1))) {
                        this.mLog.info("Cannot start localOnlyHotspot when WiFi Tethering is active.");
                        return 3;
                    } else if (((LocalOnlyHotspotRequestInfo) this.mLocalOnlyHotspotRequests.get(Integer.valueOf(pid))) != null) {
                        this.mLog.trace("caller already has an active request");
                        throw new IllegalStateException("Caller already has an active LocalOnlyHotspot request");
                    } else {
                        LocalOnlyHotspotRequestInfo request = new LocalOnlyHotspotRequestInfo(binder, messenger, new LocalOnlyRequestorCallback());
                        if (this.mIfaceIpModes.contains(Integer.valueOf(2))) {
                            try {
                                this.mLog.trace("LOHS already up, trigger onStarted callback");
                                request.sendHotspotStartedMessage(this.mLocalOnlyHotspotConfig);
                            } catch (RemoteException e) {
                                return 2;
                            }
                        } else if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                            this.mLocalOnlyHotspotConfig = WifiApConfigStore.generateLocalOnlyHotspotConfig(this.mContext);
                            startSoftApInternal(this.mLocalOnlyHotspotConfig, 2);
                        }
                        this.mLocalOnlyHotspotRequests.put(Integer.valueOf(pid), request);
                        return 0;
                    }
                }
            } catch (RemoteException e2) {
                this.mLog.warn("RemoteException during isAppForeground when calling startLOHS");
                return 3;
            }
        }
    }

    public void stopLocalOnlyHotspot() {
        enforceChangePermission();
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        this.mLog.info("stopLocalOnlyHotspot uid=% pid=%").c((long) uid).c((long) pid).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            LocalOnlyHotspotRequestInfo requestInfo = (LocalOnlyHotspotRequestInfo) this.mLocalOnlyHotspotRequests.get(Integer.valueOf(pid));
            if (requestInfo == null) {
                return;
            }
            requestInfo.unlinkDeathRecipient();
            unregisterCallingAppAndStopLocalOnlyHotspot(requestInfo);
        }
    }

    /* JADX WARNING: Missing block: B:17:0x004d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void unregisterCallingAppAndStopLocalOnlyHotspot(LocalOnlyHotspotRequestInfo request) {
        this.mLog.trace("unregisterCallingAppAndStopLocalOnlyHotspot pid=%").c((long) request.getPid()).flush();
        synchronized (this.mLocalOnlyHotspotRequests) {
            if (this.mLocalOnlyHotspotRequests.remove(Integer.valueOf(request.getPid())) == null) {
                this.mLog.trace("LocalOnlyHotspotRequestInfo not found to remove");
            } else if (this.mLocalOnlyHotspotRequests.isEmpty()) {
                this.mLocalOnlyHotspotConfig = null;
                updateInterfaceIpStateInternal(null, -1);
                long identity = Binder.clearCallingIdentity();
                try {
                    stopSoftApInternal();
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    public void startWatchLocalOnlyHotspot(Messenger messenger, IBinder binder) {
        String packageName = this.mContext.getOpPackageName();
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
            return this.mWifiStateMachine.syncGetWifiApConfiguration();
        }
        throw new SecurityException("App not allowed to read or update stored WiFi Ap config (uid = " + uid + ")");
    }

    public void setWifiApConfiguration(WifiConfiguration wifiConfig) {
        enforceChangePermission();
        int uid = Binder.getCallingUid();
        if (this.mWifiPermissionsUtil.checkConfigOverridePermission(uid)) {
            this.mLog.info("setWifiApConfiguration uid=%").c((long) uid).flush();
            if (wifiConfig != null) {
                if (isValid(wifiConfig)) {
                    this.mWifiStateMachine.setWifiApConfiguration(wifiConfig);
                } else {
                    Slog.e(TAG, "Invalid WifiConfiguration");
                }
                return;
            }
            return;
        }
        throw new SecurityException("App not allowed to read or update stored WiFi AP config (uid = " + uid + ")");
    }

    public boolean isScanAlwaysAvailable() {
        enforceAccessPermission();
        if (DBG) {
            this.mLog.info("isScanAlwaysAvailable uid=%").c((long) Binder.getCallingUid()).flush();
        }
        return this.mSettingsStore.isScanAlwaysAvailable();
    }

    public void disconnect() {
        enforceChangePermission();
        this.mLog.info("disconnect uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiStateMachine.disconnectCommand();
    }

    public void reconnect() {
        enforceChangePermission();
        this.mLog.info("reconnect uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiStateMachine.reconnectCommand(new WorkSource(Binder.getCallingUid()));
    }

    public void reassociate() {
        enforceChangePermission();
        this.mLog.info("reassociate uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiStateMachine.reassociateCommand();
    }

    public int getSupportedFeatures() {
        enforceAccessPermission();
        this.mLog.info("getSupportedFeatures uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            int mWifiSupportedFeatures = this.mWifiStateMachine.syncGetSupportedFeatures(this.mWifiStateMachineChannel);
            if (1 == Global.getInt(this.mContext.getContentResolver(), "wifi_5g_band_support", 0)) {
                mWifiSupportedFeatures |= 2;
            }
            return mWifiSupportedFeatures;
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return 0;
    }

    public void requestActivityInfo(ResultReceiver result) {
        Bundle bundle = new Bundle();
        this.mLog.info("requestActivityInfo uid=%").c((long) Binder.getCallingUid()).flush();
        bundle.putParcelable("controller_activity", reportActivityInfo());
        result.send(0, bundle);
    }

    public WifiActivityEnergyInfo reportActivityInfo() {
        enforceAccessPermission();
        this.mLog.info("reportActivityInfo uid=%").c((long) Binder.getCallingUid()).flush();
        if ((getSupportedFeatures() & 65536) == 0) {
            return null;
        }
        WifiActivityEnergyInfo wifiActivityEnergyInfo = null;
        if (this.mWifiStateMachineChannel != null) {
            WifiLinkLayerStats stats = this.mWifiStateMachine.syncGetLinkLayerStats(this.mWifiStateMachineChannel);
            if (stats != null) {
                long[] txTimePerLevel;
                long rxIdleCurrent = (long) this.mContext.getResources().getInteger(17694914);
                long rxCurrent = (long) this.mContext.getResources().getInteger(17694872);
                long txCurrent = (long) this.mContext.getResources().getInteger(17694922);
                double voltage = ((double) this.mContext.getResources().getInteger(17694919)) / 1000.0d;
                long rxIdleTime = (long) ((stats.on_time - stats.tx_time) - stats.rx_time);
                if (stats.tx_time_per_level != null) {
                    txTimePerLevel = new long[stats.tx_time_per_level.length];
                    for (int i = 0; i < txTimePerLevel.length; i++) {
                        txTimePerLevel[i] = (long) stats.tx_time_per_level[i];
                    }
                } else {
                    txTimePerLevel = new long[0];
                }
                long energyUsed = (long) (((double) (((((long) stats.tx_time) * txCurrent) + (((long) stats.rx_time) * rxCurrent)) + (rxIdleTime * rxIdleCurrent))) * voltage);
                if (rxIdleTime < 0 || stats.on_time < 0 || stats.tx_time < 0 || stats.rx_time < 0 || energyUsed < 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(" rxIdleCur=").append(rxIdleCurrent);
                    sb.append(" rxCur=").append(rxCurrent);
                    sb.append(" txCur=").append(txCurrent);
                    sb.append(" voltage=").append(voltage);
                    sb.append(" on_time=").append(stats.on_time);
                    sb.append(" tx_time=").append(stats.tx_time);
                    sb.append(" tx_time_per_level=").append(Arrays.toString(txTimePerLevel));
                    sb.append(" rx_time=").append(stats.rx_time);
                    sb.append(" rxIdleTime=").append(rxIdleTime);
                    sb.append(" energy=").append(energyUsed);
                    Log.d(TAG, " reportActivityInfo: " + sb.toString());
                }
                wifiActivityEnergyInfo = new WifiActivityEnergyInfo(this.mClock.getElapsedSinceBootMillis(), 3, (long) stats.tx_time, txTimePerLevel, (long) stats.rx_time, rxIdleTime, energyUsed);
            }
            if (wifiActivityEnergyInfo == null || !wifiActivityEnergyInfo.isValid()) {
                return null;
            }
            return wifiActivityEnergyInfo;
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public ParceledListSlice<WifiConfiguration> getConfiguredNetworks() {
        enforceAccessPermission();
        this.mLog.info("getConfiguredNetworks uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            List<WifiConfiguration> configs = this.mWifiStateMachine.syncGetConfiguredNetworks(Binder.getCallingUid(), this.mWifiStateMachineChannel);
            if (configs != null) {
                return new ParceledListSlice(configs);
            }
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public ParceledListSlice<WifiConfiguration> getPrivilegedConfiguredNetworks() {
        enforceReadCredentialPermission();
        enforceAccessPermission();
        this.mLog.info("getPrivilegedConfiguredNetworks uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            List<WifiConfiguration> configs = this.mWifiStateMachine.syncGetPrivilegedConfiguredNetwork(this.mWifiStateMachineChannel);
            if (configs != null) {
                return new ParceledListSlice(configs);
            }
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public WifiConfiguration getMatchingWifiConfig(ScanResult scanResult) {
        enforceAccessPermission();
        this.mLog.info("getMatchingWifiConfig uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return this.mWifiStateMachine.syncGetMatchingWifiConfig(scanResult, this.mWifiStateMachineChannel);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public List<OsuProvider> getMatchingOsuProviders(ScanResult scanResult) {
        enforceAccessPermission();
        this.mLog.info("getMatchingOsuProviders uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return this.mWifiStateMachine.syncGetMatchingOsuProviders(scanResult, this.mWifiStateMachineChannel);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public int addOrUpdateNetwork(WifiConfiguration config) {
        enforceChangePermission();
        this.mLog.info("addOrUpdateNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        if (config.isPasspoint()) {
            PasspointConfiguration passpointConfig = PasspointProvider.convertFromWifiConfig(config);
            if (passpointConfig.getCredential() == null) {
                Slog.e(TAG, "Missing credential for Passpoint profile");
                return -1;
            }
            passpointConfig.getCredential().setCaCertificate(config.enterpriseConfig.getCaCertificate());
            passpointConfig.getCredential().setClientCertificateChain(config.enterpriseConfig.getClientCertificateChain());
            passpointConfig.getCredential().setClientPrivateKey(config.enterpriseConfig.getClientPrivateKey());
            if (addOrUpdatePasspointConfiguration(passpointConfig)) {
                return 0;
            }
            Slog.e(TAG, "Failed to add Passpoint profile");
            return -1;
        } else if (config != null) {
            Slog.i("addOrUpdateNetwork", " uid = " + Integer.toString(Binder.getCallingUid()) + " SSID " + config.SSID + " nid=" + Integer.toString(config.networkId));
            if (config.networkId == -1) {
                config.creatorUid = Binder.getCallingUid();
            } else {
                config.lastUpdateUid = Binder.getCallingUid();
            }
            if (this.mWifiStateMachineChannel != null) {
                return this.mWifiStateMachine.syncAddOrUpdateNetwork(this.mWifiStateMachineChannel, config);
            }
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return -1;
        } else {
            Slog.e(TAG, "bad network configuration");
            return -1;
        }
    }

    public static void verifyCert(X509Certificate caCert) throws GeneralSecurityException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        CertPathValidator validator = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
        CertPath path = factory.generateCertPath(Arrays.asList(new X509Certificate[]{caCert}));
        KeyStore ks = KeyStore.getInstance("AndroidCAStore");
        ks.load(null, null);
        PKIXParameters params = new PKIXParameters(ks);
        params.setRevocationEnabled(false);
        validator.validate(path, params);
    }

    public boolean removeNetwork(int netId) {
        enforceChangePermission();
        this.mLog.info("removeNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncRemoveNetwork(this.mWifiStateMachineChannel, netId);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public boolean enableNetwork(int netId, boolean disableOthers) {
        enforceChangePermission();
        this.mLog.info("enableNetwork uid=% disableOthers=%").c((long) Binder.getCallingUid()).c(disableOthers).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncEnableNetwork(this.mWifiStateMachineChannel, netId, disableOthers);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public boolean disableNetwork(int netId) {
        enforceChangePermission();
        this.mLog.info("disableNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncDisableNetwork(this.mWifiStateMachineChannel, netId);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public WifiInfo getConnectionInfo(String callingPackage) {
        enforceAccessPermission();
        this.mLog.info("getConnectionInfo uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.syncRequestConnectionInfo(callingPackage);
    }

    public List<ScanResult> getScanResults(String callingPackage) {
        enforceAccessPermission();
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            List<ScanResult> singleScanResults;
            if (this.mWifiPermissionsUtil.canAccessScanResults(callingPackage, uid, 23)) {
                if (this.mWifiScanner == null) {
                    this.mWifiScanner = this.mWifiInjector.getWifiScanner();
                }
                singleScanResults = this.mWifiScanner.getSingleScanResults();
                Binder.restoreCallingIdentity(ident);
                return singleScanResults;
            }
            singleScanResults = new ArrayList();
            return singleScanResults;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean addOrUpdatePasspointConfiguration(PasspointConfiguration config) {
        enforceChangePermission();
        this.mLog.info("addorUpdatePasspointConfiguration uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return this.mWifiStateMachine.syncAddOrUpdatePasspointConfig(this.mWifiStateMachineChannel, config, Binder.getCallingUid());
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public boolean removePasspointConfiguration(String fqdn) {
        enforceChangePermission();
        this.mLog.info("removePasspointConfiguration uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return this.mWifiStateMachine.syncRemovePasspointConfig(this.mWifiStateMachineChannel, fqdn);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public List<PasspointConfiguration> getPasspointConfigurations() {
        enforceAccessPermission();
        this.mLog.info("getPasspointConfigurations uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            return this.mWifiStateMachine.syncGetPasspointConfigs(this.mWifiStateMachineChannel);
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public void queryPasspointIcon(long bssid, String fileName) {
        enforceAccessPermission();
        this.mLog.info("queryPasspointIcon uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint")) {
            this.mWifiStateMachine.syncQueryPasspointIcon(this.mWifiStateMachineChannel, bssid, fileName);
            return;
        }
        throw new UnsupportedOperationException("Passpoint not enabled");
    }

    public int matchProviderWithCurrentNetwork(String fqdn) {
        this.mLog.info("matchProviderWithCurrentNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.matchProviderWithCurrentNetwork(this.mWifiStateMachineChannel, fqdn);
    }

    public void deauthenticateNetwork(long holdoff, boolean ess) {
        this.mLog.info("deauthenticateNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiStateMachine.deauthenticateNetwork(this.mWifiStateMachineChannel, holdoff, ess);
    }

    public boolean saveConfiguration() {
        enforceChangePermission();
        this.mLog.info("saveConfiguration uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncSaveConfig(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public void setCountryCode(String countryCode, boolean persist) {
        Slog.i(TAG, "WifiService trying to set country code to " + countryCode + " with persist set to " + persist);
        enforceConnectivityInternalPermission();
        this.mLog.info("setCountryCode uid=%").c((long) Binder.getCallingUid()).flush();
        long token = Binder.clearCallingIdentity();
        this.mCountryCode.setCountryCode(countryCode);
        Binder.restoreCallingIdentity(token);
    }

    public String getCountryCode() {
        enforceConnectivityInternalPermission();
        this.mLog.info("getCountryCode uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mCountryCode.getCountryCode();
    }

    public boolean isDualBandSupported() {
        this.mLog.info("isDualBandSupported uid=%").c((long) Binder.getCallingUid()).flush();
        if (Global.getInt(this.mContext.getContentResolver(), "wifi_5g_band_support", 0) == 1) {
            return true;
        }
        return false;
    }

    @Deprecated
    public DhcpInfo getDhcpInfo() {
        enforceAccessPermission();
        this.mLog.info("getDhcpInfo uid=%").c((long) Binder.getCallingUid()).flush();
        DhcpResults dhcpResults = this.mWifiStateMachine.syncGetDhcpResults();
        DhcpInfo info = new DhcpInfo();
        if (dhcpResults.ipAddress != null && (dhcpResults.ipAddress.getAddress() instanceof Inet4Address)) {
            info.ipAddress = NetworkUtils.inetAddressToInt((Inet4Address) dhcpResults.ipAddress.getAddress());
            info.netmask = NetworkUtils.prefixLengthToNetmaskInt(dhcpResults.ipAddress.getNetworkPrefixLength());
        }
        if (dhcpResults.gateway != null) {
            info.gateway = NetworkUtils.inetAddressToInt((Inet4Address) dhcpResults.gateway);
        }
        int dnsFound = 0;
        for (InetAddress dns : dhcpResults.dnsServers) {
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

    public void enableTdls(String remoteAddress, boolean enable) {
        if (remoteAddress == null) {
            throw new IllegalArgumentException("remoteAddress cannot be null");
        }
        this.mLog.info("enableTdls uid=% enable=%").c((long) Binder.getCallingUid()).c(enable).flush();
        TdlsTaskParams params = new TdlsTaskParams();
        params.remoteIpAddress = remoteAddress;
        params.enable = enable;
        new TdlsTask().execute(new TdlsTaskParams[]{params});
    }

    public void enableTdlsWithMacAddress(String remoteMacAddress, boolean enable) {
        this.mLog.info("enableTdlsWithMacAddress uid=% enable=%").c((long) Binder.getCallingUid()).c(enable).flush();
        if (remoteMacAddress == null) {
            throw new IllegalArgumentException("remoteMacAddress cannot be null");
        }
        this.mWifiStateMachine.enableTdls(remoteMacAddress, enable);
    }

    public Messenger getWifiServiceMessenger() {
        enforceAccessPermission();
        enforceChangePermission();
        this.mLog.info("getWifiServiceMessenger uid=%").c((long) Binder.getCallingUid()).flush();
        return new Messenger(this.mClientHandler);
    }

    public void disableEphemeralNetwork(String SSID) {
        enforceAccessPermission();
        enforceChangePermission();
        this.mLog.info("disableEphemeralNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiStateMachine.disableEphemeralNetwork(SSID);
    }

    private boolean startConsentUi(String packageName, int callingUid, String intentAction) throws RemoteException {
        if (UserHandle.getAppId(callingUid) == OppoManuConnectManager.UID_DEFAULT) {
            return false;
        }
        try {
            if (this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 268435456, UserHandle.getUserId(callingUid)).uid != callingUid) {
                throw new SecurityException("Package " + callingUid + " not in uid " + callingUid);
            }
            Intent intent = new Intent(intentAction);
            intent.addFlags(276824064);
            intent.putExtra("android.intent.extra.PACKAGE_NAME", packageName);
            this.mContext.startActivity(intent);
            return true;
        } catch (NameNotFoundException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    private void registerForScanModeChange() {
        this.mFrameworkFacade.registerContentObserver(this.mContext, Global.getUriFor("wifi_scan_always_enabled"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                WifiServiceImpl.this.mSettingsStore.handleWifiScanAlwaysAvailableToggled();
                WifiServiceImpl.this.mWifiController.sendMessage(155655);
            }
        });
    }

    private void registerForBackgroundThrottleChanges() {
        this.mFrameworkFacade.registerContentObserver(this.mContext, Global.getUriFor("wifi_scan_background_throttle_interval_ms"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                WifiServiceImpl.this.updateBackgroundThrottleInterval();
            }
        });
        this.mFrameworkFacade.registerContentObserver(this.mContext, Global.getUriFor("wifi_scan_background_throttle_package_whitelist"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                WifiServiceImpl.this.updateBackgroundThrottlingWhitelist();
            }
        });
    }

    private void updateBackgroundThrottleInterval() {
        this.mBackgroundThrottleInterval = this.mFrameworkFacade.getLongSetting(this.mContext, "wifi_scan_background_throttle_interval_ms", DEFAULT_SCAN_BACKGROUND_THROTTLE_INTERVAL_MS);
    }

    private void updateBackgroundThrottlingWhitelist() {
        String setting = this.mFrameworkFacade.getStringSetting(this.mContext, "wifi_scan_background_throttle_package_whitelist");
        this.mBackgroundThrottlePackageWhitelist.clear();
        if (setting != null) {
            this.mBackgroundThrottlePackageWhitelist.addAll(Arrays.asList(setting.split(",")));
        }
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        intentFilter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        if (this.mContext.getResources().getBoolean(17957074)) {
            intentFilter.addAction("android.intent.action.EMERGENCY_CALL_STATE_CHANGED");
        }
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
    }

    private void registerForPackageOrUserRemoval() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                        int uid = intent.getIntExtra("android.intent.extra.UID", -1);
                        Uri uri = intent.getData();
                        if (uid != -1 && uri != null) {
                            WifiServiceImpl.this.mWifiStateMachine.removeAppConfigs(uri.getSchemeSpecificPart(), uid);
                        }
                    }
                } else if (action.equals("android.intent.action.USER_REMOVED")) {
                    WifiServiceImpl.this.mWifiStateMachine.removeUserConfigs(intent.getIntExtra("android.intent.extra.user_handle", 0));
                }
            }
        }, UserHandle.ALL, intentFilter, null, null);
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new WifiShellCommand(this.mWifiStateMachine).exec(this, in, out, err, args, callback, resultReceiver);
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump WifiService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        WifiScoreReport wifiScoreReport;
        if (args != null && args.length > 0 && WifiMetrics.PROTO_DUMP_ARG.equals(args[0])) {
            this.mWifiStateMachine.updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
        } else if (args != null && args.length > 0 && "ipmanager".equals(args[0])) {
            String[] ipManagerArgs = new String[(args.length - 1)];
            System.arraycopy(args, 1, ipManagerArgs, 0, ipManagerArgs.length);
            this.mWifiStateMachine.dumpIpManager(fd, pw, ipManagerArgs);
        } else if (args == null || args.length <= 0 || !WifiScoreReport.DUMP_ARG.equals(args[0])) {
            pw.println("Wi-Fi is " + this.mWifiStateMachine.syncGetWifiStateByName());
            pw.println("Stay-awake conditions: " + this.mFacade.getIntegerSetting(this.mContext, "stay_on_while_plugged_in", 0));
            pw.println("mInIdleMode " + this.mInIdleMode);
            pw.println("mScanPending " + this.mScanPending);
            this.mWifiController.dump(fd, pw, args);
            this.mSettingsStore.dump(fd, pw, args);
            this.mTrafficPoller.dump(fd, pw, args);
            pw.println();
            pw.println("Locks held:");
            this.mWifiLockManager.dump(pw);
            pw.println();
            this.mWifiMulticastLockManager.dump(pw);
            pw.println();
            this.mWifiStateMachine.dump(fd, pw, args);
            pw.println();
            this.mWifiStateMachine.updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
            pw.println();
            this.mWifiBackupRestore.dump(fd, pw, args);
            pw.println();
            wifiScoreReport = this.mWifiStateMachine.getWifiScoreReport();
            if (wifiScoreReport != null) {
                pw.println("WifiScoreReport:");
                wifiScoreReport.dump(fd, pw, args);
            }
            pw.println();
        } else {
            wifiScoreReport = this.mWifiStateMachine.getWifiScoreReport();
            if (wifiScoreReport != null) {
                wifiScoreReport.dump(fd, pw, args);
            }
        }
    }

    public boolean acquireWifiLock(IBinder binder, int lockMode, String tag, WorkSource ws) {
        this.mLog.info("acquireWifiLock uid=% lockMode=%").c((long) Binder.getCallingUid()).c((long) lockMode).flush();
        if (!this.mWifiLockManager.acquireWifiLock(lockMode, tag, binder, ws)) {
            return false;
        }
        this.mWifiController.sendMessage(155654);
        return true;
    }

    public void updateWifiLockWorkSource(IBinder binder, WorkSource ws) {
        this.mLog.info("updateWifiLockWorkSource uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiLockManager.updateWifiLockWorkSource(binder, ws);
    }

    public boolean releaseWifiLock(IBinder binder) {
        this.mLog.info("releaseWifiLock uid=%").c((long) Binder.getCallingUid()).flush();
        if (!this.mWifiLockManager.releaseWifiLock(binder)) {
            return false;
        }
        this.mWifiController.sendMessage(155654);
        return true;
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

    public void releaseMulticastLock() {
        enforceMulticastChangePermission();
        this.mLog.info("releaseMulticastLock uid=%").c((long) Binder.getCallingUid()).flush();
        this.mWifiMulticastLockManager.releaseLock();
    }

    public boolean isMulticastEnabled() {
        enforceAccessPermission();
        this.mLog.info("isMulticastEnabled uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiMulticastLockManager.isMulticastEnabled();
    }

    public void enableVerboseLogging(int verbose) {
        enforceAccessPermission();
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
        this.mWifiController.enableVerboseLogging(verbose);
        enableOppoWifiServiceLogging(verbose);
        this.mLog.info("enableVerboseLogging uid=% verbose=%").c((long) Binder.getCallingUid()).c((long) verbose).flush();
        this.mFacade.setIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", verbose);
        enableVerboseLoggingInternal(verbose);
    }

    void enableVerboseLoggingInternal(int verbose) {
        this.mWifiStateMachine.enableVerboseLogging(verbose);
        this.mWifiLockManager.enableVerboseLogging(verbose);
        this.mWifiMulticastLockManager.enableVerboseLogging(verbose);
        this.mWifiInjector.getWifiLastResortWatchdog().enableVerboseLogging(verbose);
        this.mWifiInjector.getWifiBackupRestore().enableVerboseLogging(verbose);
        LogcatLog.enableVerboseLogging(verbose);
        this.mWifiController.enableVerboseLogging(SystemProperties.getInt("vendor.qcom.wifi.debug", 0));
    }

    public int getVerboseLoggingLevel() {
        enforceAccessPermission();
        this.mLog.info("getVerboseLoggingLevel uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mFacade.getIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", 0);
    }

    public void enableAggressiveHandover(int enabled) {
        enforceAccessPermission();
        this.mLog.info("enableAggressiveHandover uid=% enabled=%").c((long) Binder.getCallingUid()).c((long) enabled).flush();
        this.mWifiStateMachine.enableAggressiveHandover(enabled);
    }

    public int getAggressiveHandover() {
        enforceAccessPermission();
        this.mLog.info("getAggressiveHandover uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.getAggressiveHandover();
    }

    public void setAllowScansWithTraffic(int enabled) {
        enforceAccessPermission();
        this.mLog.info("setAllowScansWithTraffic uid=% enabled=%").c((long) Binder.getCallingUid()).c((long) enabled).flush();
        this.mWifiStateMachine.setAllowScansWithTraffic(enabled);
    }

    public int getAllowScansWithTraffic() {
        enforceAccessPermission();
        this.mLog.info("getAllowScansWithTraffic uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.getAllowScansWithTraffic();
    }

    public boolean setEnableAutoJoinWhenAssociated(boolean enabled) {
        enforceChangePermission();
        this.mLog.info("setEnableAutoJoinWhenAssociated uid=% enabled=%").c((long) Binder.getCallingUid()).c(enabled).flush();
        return this.mWifiStateMachine.setEnableAutoJoinWhenAssociated(enabled);
    }

    public boolean getEnableAutoJoinWhenAssociated() {
        enforceAccessPermission();
        this.mLog.info("getEnableAutoJoinWhenAssociated uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.getEnableAutoJoinWhenAssociated();
    }

    public WifiConnectionStatistics getConnectionStatistics() {
        enforceAccessPermission();
        enforceReadCredentialPermission();
        this.mLog.info("getConnectionStatistics uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncGetConnectionStatistics(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public boolean getWifiStaSapConcurrency() {
        return this.mWifiApConfigStore.getStaSapConcurrency();
    }

    public boolean isExtendingNetworkCoverage() {
        return this.mWifiStateMachine.isExtendingNetworkCoverage();
    }

    private boolean startDualSapMode(boolean enable) {
        WifiConfiguration apConfig = this.mWifiInjector.getWifiApConfigStore().getApConfiguration();
        if (enable && apConfig.apBand != 2) {
            Slog.e(TAG, "Continue with Single SAP Mode.");
            return false;
        } else if (!this.mWifiApConfigStore.isDualSapSupported() || this.mWifiApConfigStore.getBridgeInterface() == null) {
            Slog.e(TAG, "Dual SAP Mode is not supported.");
            return false;
        } else {
            this.mLog.trace("startDualSapMode uid=% enable=%").c((long) Binder.getCallingUid()).c(enable).flush();
            if (enable && this.mWifiApConfigStore.getDualSapStatus()) {
                Slog.d(TAG, "DUAL Sap Mode already enabled. Do nothing!!");
                return true;
            }
            boolean apEnabled = this.mWifiStateMachine.syncGetWifiApState() != 12 ? this.mWifiStateMachine.syncGetWifiApState() == 13 : true;
            boolean staEnabled = this.mWifiStateMachine.syncGetWifiState() != 2 ? this.mWifiStateMachine.syncGetWifiState() == 3 : true;
            if (!enable || (enable && apEnabled)) {
                this.mWifiController.sendMessage(155658, 0, 0);
            }
            if (enable && staEnabled) {
                this.mSettingsStore.setWifiSavedState(1);
                this.mWifiController.sendMessage(155656);
            }
            if (enable) {
                this.mWifiStateMachine.setDualSapMode(true);
                if (this.mWifiApConfigStore.getStaSapConcurrency()) {
                    this.mWifiController.setSoftApStateMachine(null, false);
                }
            }
            return true;
        }
    }

    private void resetWifiNetworks() {
        if (this.mWifiStateMachineChannel != null) {
            List<WifiConfiguration> networks = this.mWifiStateMachine.syncGetConfiguredNetworks(Binder.getCallingUid(), this.mWifiStateMachineChannel);
            if (networks != null) {
                for (WifiConfiguration config : networks) {
                    removeNetwork(config.networkId);
                }
                saveConfiguration();
            }
        }
    }

    public void factoryReset() {
        enforceConnectivityInternalPermission();
        this.mLog.info("factoryReset uid=%").c((long) Binder.getCallingUid()).flush();
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            if (!this.mUserManager.hasUserRestriction("no_config_tethering")) {
                stopSoftApInternal();
            }
            if (!this.mUserManager.hasUserRestriction("no_config_wifi")) {
                if (getWifiEnabledState() == 3) {
                    resetWifiNetworks();
                } else {
                    this.mIsFactoryResetOn = true;
                    try {
                        setWifiEnabled(this.mContext.getOpPackageName(), true);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
    }

    static boolean logAndReturnFalse(String s) {
        Log.d(TAG, s);
        return false;
    }

    public static boolean isValid(WifiConfiguration config) {
        String validity = checkValidity(config);
        return validity != null ? logAndReturnFalse(validity) : true;
    }

    public static String checkValidity(WifiConfiguration config) {
        if (config.allowedKeyManagement == null) {
            return "allowed kmgmt";
        }
        if (config.allowedKeyManagement.cardinality() > 1) {
            if (config.allowedKeyManagement.cardinality() > 4) {
                return "cardinality > 4";
            }
            if (!config.allowedKeyManagement.get(2)) {
                return "not WPA_EAP";
            }
            if (!(config.allowedKeyManagement.get(3) || (config.allowedKeyManagement.get(1) ^ 1) == 0)) {
                return "not PSK or 8021X";
            }
        }
        if (config.getIpAssignment() == IpAssignment.STATIC) {
            StaticIpConfiguration staticIpConf = config.getStaticIpConfiguration();
            if (staticIpConf == null) {
                return "null StaticIpConfiguration";
            }
            if (staticIpConf.ipAddress == null) {
                return "null static ip Address";
            }
        }
        return null;
    }

    public Network getCurrentNetwork() {
        enforceAccessPermission();
        this.mLog.info("getCurrentNetwork uid=%").c((long) Binder.getCallingUid()).flush();
        return this.mWifiStateMachine.getCurrentNetwork();
    }

    public static String toHexString(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('\'').append(s).append('\'');
        for (int n = 0; n < s.length(); n++) {
            sb.append(String.format(" %02x", new Object[]{Integer.valueOf(s.charAt(n) & Constants.SHORT_MASK)}));
        }
        return sb.toString();
    }

    public void hideCertFromUnaffiliatedUsers(String alias) {
        this.mCertManager.hideCertFromUnaffiliatedUsers(alias);
    }

    public String[] listClientCertsForCurrentUser() {
        return this.mCertManager.listClientCertsForCurrentUser();
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        enforceConnectivityInternalPermission();
        this.mLog.info("enableWifiConnectivityManager uid=% enabled=%").c((long) Binder.getCallingUid()).c(enabled).flush();
        this.mWifiStateMachine.enableWifiConnectivityManager(enabled);
    }

    public byte[] retrieveBackupData() {
        enforceNetworkSettingsPermission();
        this.mLog.info("retrieveBackupData uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel == null) {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return null;
        }
        Slog.d(TAG, "Retrieving backup data");
        byte[] backupData = this.mWifiBackupRestore.retrieveBackupDataFromConfigurations(this.mWifiStateMachine.syncGetPrivilegedConfiguredNetwork(this.mWifiStateMachineChannel));
        Slog.d(TAG, "Retrieved backup data");
        return backupData;
    }

    private void restoreNetworks(List<WifiConfiguration> configurations) {
        if (configurations == null) {
            Slog.e(TAG, "Backup data parse failed");
            return;
        }
        for (WifiConfiguration configuration : configurations) {
            int networkId = this.mWifiStateMachine.syncAddOrUpdateNetwork(this.mWifiStateMachineChannel, configuration);
            if (networkId == -1) {
                Slog.e(TAG, "Restore network failed: " + configuration.configKey());
            } else {
                this.mWifiStateMachine.syncEnableNetwork(this.mWifiStateMachineChannel, networkId, false);
            }
        }
    }

    public void restoreBackupData(byte[] data) {
        enforceNetworkSettingsPermission();
        this.mLog.info("restoreBackupData uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel == null) {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return;
        }
        Slog.d(TAG, "Restoring backup data");
        restoreNetworks(this.mWifiBackupRestore.retrieveConfigurationsFromBackupData(data));
        Slog.d(TAG, "Restored backup data");
    }

    public void restoreSupplicantBackupData(byte[] supplicantData, byte[] ipConfigData) {
        enforceNetworkSettingsPermission();
        this.mLog.trace("restoreSupplicantBackupData uid=%").c((long) Binder.getCallingUid()).flush();
        if (this.mWifiStateMachineChannel == null) {
            Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
            return;
        }
        Slog.d(TAG, "Restoring supplicant backup data");
        restoreNetworks(this.mWifiBackupRestore.retrieveConfigurationsFromSupplicantBackupData(supplicantData, ipConfigData));
        Slog.d(TAG, "Restored supplicant backup data");
    }

    public List<WifiDevice> getConnectedStations() {
        if (this.mContext.getResources().getBoolean(17957022)) {
            return WifiSoftApNotificationManager.getInstance(this.mContext).getConnectedStations();
        }
        return Collections.emptyList();
    }

    public boolean startRxSensTest(WifiConfiguration config, String ip) {
        Slog.d(TAG, "startRxSensTest config=" + config + " ip=" + ip);
        if (config == null || ip == null || ip.equals("")) {
            Slog.e(TAG, "startRxSensTest failed: invalid arguments");
            return false;
        }
        try {
            setWifiEnabled(this.mContext.getOpPackageName(), false);
        } catch (RemoteException e) {
        }
        return this.mWifiStateMachine.startRxSensTest(config, ip);
    }

    public void stopRxSensTest() {
        Slog.d(TAG, "startRxSensTest");
        this.mWifiStateMachine.stopRxSensTest();
    }

    public void setPowerSavingMode(boolean mode) {
        enforceAccessPermission();
        this.mWifiStateMachine.setPowerSavingMode(mode);
    }

    public String getWifiPowerEventCode() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getWifiPowerEventCode();
    }
}
