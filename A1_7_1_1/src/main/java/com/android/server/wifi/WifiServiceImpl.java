package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.DhcpResults;
import android.net.Network;
import android.net.NetworkScorerAppManager;
import android.net.NetworkUtils;
import android.net.Uri;
import android.net.wifi.HotspotClient;
import android.net.wifi.IWifiManager.Stub;
import android.net.wifi.PPPOEInfo;
import android.net.wifi.PasspointManagementObjectDefinition;
import android.net.wifi.ScanResult;
import android.net.wifi.ScanSettings;
import android.net.wifi.WifiActivityEnergyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConnectionStatistics;
import android.net.wifi.WifiDevice;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiLinkLayerStats;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.AsyncChannel;
import com.android.server.am.BatteryStatsService;
import com.android.server.wifi.anqp.Constants;
import com.android.server.wifi.configparse.ConfigBuilder;
import com.mediatek.cta.CtaUtils;
import com.mediatek.server.wifi.WifiNvRamAgent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class WifiServiceImpl extends Stub {
    public static final String ACTION_MTKLOGGER_STATE_CHANGED = "com.mediatek.mtklogger.intent.action.LOG_STATE_CHANGED";
    private static final String BOOT_DEFAULT_WIFI_COUNTRY_CODE = "ro.boot.wificountrycode";
    private static boolean DBG = false;
    public static final String DEBUG_PROPERTY = "persist.sys.assert.panic";
    private static final int DISABLE_IGNORE = -1;
    private static final int DISABLE_WIFI_FLIGHTMODE = 1;
    private static final int DISABLE_WIFI_IPO = 2;
    private static final int DISABLE_WIFI_SETTING = 0;
    private static final String NORMAL_BOOT_ACTION = "android.intent.action.normal.boot";
    private static final String TAG = "WifiService";
    private static final boolean VDBG = false;
    private static final String WIFI_5G_BAND_SUPPORT = "wifi_5g_band_support";
    private static ConnectivityManager sCM;
    private final AppOpsManager mAppOps;
    private final IBatteryStats mBatteryStats;
    private final WifiCertManager mCertManager;
    private ClientHandler mClientHandler;
    private final Context mContext;
    private final WifiCountryCode mCountryCode;
    private final FrameworkFacade mFacade;
    boolean mInIdleMode;
    private boolean mIsFirstTime;
    private boolean mIsReceiverRegistered;
    private boolean mIsSim1Ready;
    private boolean mIsSim2Ready;
    private int mMulticastDisabled;
    private int mMulticastEnabled;
    private final List<Multicaster> mMulticasters;
    private String[] mNLPPackages;
    private WifiNotificationController mNotificationController;
    private final PowerManager mPowerManager;
    private final BroadcastReceiver mReceiver;
    boolean mScanPending;
    final WifiSettingsStore mSettingsStore;
    private WifiTrafficPoller mTrafficPoller;
    private final UserManager mUserManager;
    private boolean mVerboseLoggingEnabled;
    private int mVerboseLoggingLevel;
    private WifiController mWifiController;
    private final WifiInjector mWifiInjector;
    private boolean mWifiIpoOff;
    private final WifiLockManager mWifiLockManager;
    private final WifiMetrics mWifiMetrics;
    private int mWifiOffListenerCount;
    private boolean mWifiOffReported;
    private boolean mWifiOnDeffered;
    final WifiStateMachine mWifiStateMachine;
    private AsyncChannel mWifiStateMachineChannel;
    WifiStateMachineHandler mWifiStateMachineHandler;
    private int scanRequestCounter;

    private class ClientHandler extends Handler {
        ClientHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
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
                    new AsyncChannel().connect(WifiServiceImpl.this.mContext, this, msg.replyTo);
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
                case 151559:
                    WifiConfiguration config = msg.obj;
                    int networkId = msg.arg1;
                    if (msg.what == 151559) {
                        Slog.d("WiFiServiceImpl ", "SAVE nid=" + Integer.toString(networkId) + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                    }
                    if (msg.what == 151553) {
                        Slog.d("WiFiServiceImpl ", "CONNECT  nid=" + Integer.toString(networkId) + " uid=" + msg.sendingUid + " name=" + WifiServiceImpl.this.mContext.getPackageManager().getNameForUid(msg.sendingUid));
                    }
                    if (config != null && WifiServiceImpl.isValid(config)) {
                        if (WifiServiceImpl.DBG) {
                            Slog.d(WifiServiceImpl.TAG, "Connect with config" + config);
                        }
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    } else if (config != null || networkId == -1) {
                        Slog.e(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring invalid msg=" + msg);
                        if (msg.what == 151553) {
                            replyFailed(msg, 151554, 8);
                            return;
                        } else {
                            replyFailed(msg, 151560, 8);
                            return;
                        }
                    } else {
                        if (WifiServiceImpl.DBG) {
                            Slog.d(WifiServiceImpl.TAG, "Connect with networkId" + networkId);
                        }
                        WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                        return;
                    }
                case 151556:
                    WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                    return;
                case 151562:
                case 151566:
                case 151569:
                case 151572:
                case 151575:
                case 151578:
                case 151612:
                    WifiServiceImpl.this.mWifiStateMachine.sendMessage(Message.obtain(msg));
                    return;
                default:
                    Slog.d(WifiServiceImpl.TAG, "ClientHandler.handleMessage ignoring msg=" + msg);
                    return;
            }
        }

        private void replyFailed(Message msg, int what, int why) {
            Message reply = Message.obtain();
            reply.what = what;
            reply.arg1 = why;
            try {
                msg.replyTo.send(reply);
            } catch (RemoteException e) {
            }
        }
    }

    private class Multicaster implements DeathRecipient {
        IBinder mBinder;
        String mTag;
        int mUid = Binder.getCallingUid();

        Multicaster(String tag, IBinder binder) {
            this.mTag = tag;
            this.mBinder = binder;
            try {
                this.mBinder.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        public void binderDied() {
            Slog.e(WifiServiceImpl.TAG, "Multicaster binderDied");
            synchronized (WifiServiceImpl.this.mMulticasters) {
                int i = WifiServiceImpl.this.mMulticasters.indexOf(this);
                if (i != -1) {
                    WifiServiceImpl.this.removeMulticasterLocked(i, this.mUid);
                }
            }
        }

        void unlinkDeathRecipient() {
            this.mBinder.unlinkToDeath(this, 0);
        }

        public int getUid() {
            return this.mUid;
        }

        public String toString() {
            return "Multicaster{" + this.mTag + " uid=" + this.mUid + "}";
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

    private class WifiStateMachineHandler extends Handler {
        private AsyncChannel mWsmChannel = new AsyncChannel();

        WifiStateMachineHandler(Looper looper) {
            super(looper);
            this.mWsmChannel.connect(WifiServiceImpl.this.mContext, this, WifiServiceImpl.this.mWifiStateMachine.getHandler());
        }

        public void handleMessage(Message msg) {
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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiServiceImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiServiceImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiServiceImpl.<clinit>():void");
    }

    public WifiServiceImpl(Context context) {
        this.mMulticasters = new ArrayList();
        this.scanRequestCounter = 0;
        this.mIsSim1Ready = false;
        this.mIsSim2Ready = false;
        this.mIsFirstTime = true;
        this.mWifiOffReported = false;
        this.mWifiOnDeffered = false;
        this.mVerboseLoggingEnabled = false;
        this.mVerboseLoggingLevel = 0;
        this.mWifiIpoOff = false;
        this.mIsReceiverRegistered = false;
        String[] strArr = new String[3];
        strArr[0] = "com.baidu.map.location";
        strArr[1] = "com.amap.android.location";
        strArr[2] = "com.amap.android.ams";
        this.mNLPPackages = strArr;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int i = 1;
                String action = intent.getAction();
                if (!action.equals("android.net.wifi.STATE_CHANGE")) {
                    Slog.d(WifiServiceImpl.TAG, "onReceive, action:" + action);
                }
                WifiController -get8;
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
                    -get8 = WifiServiceImpl.this.mWifiController;
                    if (!emergencyMode) {
                        i = 0;
                    }
                    -get8.sendMessage(155649, i, 0);
                } else if (action.equals("android.intent.action.EMERGENCY_CALL_STATE_CHANGED")) {
                    boolean inCall = intent.getBooleanExtra("phoneInEmergencyCall", false);
                    -get8 = WifiServiceImpl.this.mWifiController;
                    if (!inCall) {
                        i = 0;
                    }
                    -get8.sendMessage(155662, i, 0);
                } else if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                    WifiServiceImpl.this.handleIdleModeChanged();
                } else if (action.equals("mediatek.intent.action.LOCATED_PLMN_CHANGED")) {
                    String plmn = (String) intent.getExtra("plmn");
                    String iso = (String) intent.getExtra("iso");
                    Log.d(WifiServiceImpl.TAG, "ACTION_LOCATED_PLMN_CHANGED: " + plmn + " iso =" + iso);
                    if (iso != null && plmn != null) {
                        WifiServiceImpl.this.mCountryCode.setCountryCode(iso, false);
                    }
                }
            }
        };
        this.mWifiOffListenerCount = 0;
        this.mContext = context;
        this.mWifiInjector = WifiInjector.getInstance();
        this.mFacade = new FrameworkFacade();
        HandlerThread wifiThread = new HandlerThread(TAG);
        wifiThread.start();
        this.mWifiMetrics = this.mWifiInjector.getWifiMetrics();
        this.mTrafficPoller = new WifiTrafficPoller(this.mContext, wifiThread.getLooper(), WifiNative.getWlanNativeInterface().getInterfaceName());
        this.mUserManager = UserManager.get(this.mContext);
        HandlerThread wifiStateMachineThread = new HandlerThread("WifiStateMachine");
        wifiStateMachineThread.start();
        this.mCountryCode = new WifiCountryCode(WifiNative.getWlanNativeInterface(), SystemProperties.get(BOOT_DEFAULT_WIFI_COUNTRY_CODE), this.mFacade.getStringSetting(this.mContext, "wifi_country_code"), this.mContext.getResources().getBoolean(17956890));
        this.mWifiStateMachine = new WifiStateMachine(this.mContext, this.mFacade, wifiStateMachineThread.getLooper(), this.mUserManager, this.mWifiInjector, new BackupManagerProxy(), this.mCountryCode);
        this.mSettingsStore = new WifiSettingsStore(this.mContext);
        this.mWifiStateMachine.enableRssiPolling(true);
        this.mBatteryStats = BatteryStatsService.getService();
        this.mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mCertManager = new WifiCertManager(this.mContext);
        this.mNotificationController = new WifiNotificationController(this.mContext, wifiThread.getLooper(), this.mWifiStateMachine, this.mFacade, null);
        this.mWifiLockManager = new WifiLockManager(this.mContext, this.mBatteryStats);
        this.mClientHandler = new ClientHandler(wifiThread.getLooper());
        this.mWifiStateMachineHandler = new WifiStateMachineHandler(wifiThread.getLooper());
        this.mWifiController = new WifiController(this.mContext, this.mWifiStateMachine, this.mSettingsStore, this.mWifiLockManager, wifiThread.getLooper(), this.mFacade);
        this.mWifiInjector.getWifiLastResortWatchdog().setWifiController(this.mWifiController);
        initializeExtra();
    }

    public void checkAndStartWifi() {
        boolean isAlarmBoot;
        this.mWifiStateMachine.initRomupdateHelperBroadcastReceiver();
        this.mWifiStateMachine.autoConnectInit();
        Slog.d(TAG, "mIsFirstTime: " + this.mIsFirstTime);
        if (this.mIsFirstTime) {
            Slog.d(TAG, "mWifiController.start");
            this.mWifiController.start();
            this.mIsFirstTime = false;
        }
        String bootReason = SystemProperties.get("sys.boot.reason");
        if (bootReason == null || !bootReason.equals("1")) {
            isAlarmBoot = false;
        } else {
            isAlarmBoot = true;
        }
        if (isAlarmBoot) {
            Slog.i(TAG, "isAlarmBoot = true don't start wifi");
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    Slog.i(WifiServiceImpl.TAG, "receive NORMAL_BOOT_ACTION for alarm boot");
                    WifiServiceImpl.this.mContext.unregisterReceiver(this);
                    WifiServiceImpl.this.checkAndStartWifi();
                }
            }, new IntentFilter(NORMAL_BOOT_ACTION));
            return;
        }
        boolean wifiEnabled = this.mSettingsStore.isWifiToggleEnabled();
        Slog.i(TAG, "WifiService starting up with Wi-Fi " + (wifiEnabled ? "enabled" : "disabled"));
        registerForScanModeChange();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
                Slog.i(WifiServiceImpl.TAG, "ACTION_AIRPLANE_MODE_CHANGED isAirplaneModeOn=" + isAirplaneModeOn);
                if (WifiServiceImpl.this.mSettingsStore.handleAirplaneModeToggled()) {
                    if (isAirplaneModeOn) {
                        if (WifiServiceImpl.this.mWifiOffListenerCount > 0) {
                            Slog.d(WifiServiceImpl.TAG, "mWifiOffListenerCount: " + WifiServiceImpl.this.mWifiOffListenerCount);
                            WifiServiceImpl.this.reportWifiOff(1);
                            Slog.d(WifiServiceImpl.TAG, "Let callback to call wifi off");
                            return;
                        }
                        Slog.d(WifiServiceImpl.TAG, "mWifiOffListenerCount < 0");
                    }
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
                int simSlot = intent.getIntExtra("slot", -1);
                Log.d(WifiServiceImpl.TAG, "onReceive ACTION_SIM_STATE_CHANGED iccState: " + state + ", simSlot: " + simSlot);
                if ("ABSENT".equals(state)) {
                    if (simSlot == 0) {
                        WifiServiceImpl.this.mIsSim1Ready = false;
                    } else if (1 == simSlot) {
                        WifiServiceImpl.this.mIsSim2Ready = false;
                    }
                    Log.d(WifiServiceImpl.TAG, "resetting networks because SIM was removed, simSlot: " + simSlot);
                    WifiServiceImpl.this.mWifiStateMachine.resetSimAuthNetworks(simSlot, false);
                    if (!WifiServiceImpl.this.mIsSim1Ready && !WifiServiceImpl.this.mIsSim2Ready) {
                        Log.d(WifiServiceImpl.TAG, "All sim card is absent, resetting country code because SIM is removed");
                        WifiServiceImpl.this.mCountryCode.simCardRemoved();
                    }
                } else if ("LOADED".equals(state)) {
                    Log.d(WifiServiceImpl.TAG, "resetting networks because SIM was loaded");
                    if (simSlot == 0) {
                        WifiServiceImpl.this.mIsSim1Ready = true;
                    } else if (1 == simSlot) {
                        WifiServiceImpl.this.mIsSim2Ready = true;
                    }
                    WifiServiceImpl.this.mWifiStateMachine.resetSimAuthNetworks(simSlot, true);
                }
            }
        }, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        registerForBroadcasts();
        registerForPackageOrUserRemoval();
        this.mInIdleMode = this.mPowerManager.isDeviceIdleMode();
        if (SystemProperties.getInt("persist.sys.wifi_disable", 0) == 1) {
            Slog.d(TAG, "checkAndStartWifi return directly for spaying");
            return;
        }
        if (wifiEnabled) {
            setWifiEnabled(wifiEnabled);
        } else {
            Intent intent = new Intent("android.net.wifi.WIFI_STATE_CHANGED");
            intent.addFlags(67108864);
            intent.putExtra("wifi_state", 1);
            intent.putExtra("previous_wifi_state", 1);
            this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            Log.d(TAG, "Send Broadcast WIFI_STATE_DISABLED when don't open wifi!");
        }
    }

    public void handleUserSwitch(int userId) {
        this.mWifiStateMachine.handleUserSwitch(userId);
    }

    public boolean pingSupplicant() {
        enforceAccessPermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncPingSupplicant(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    /* JADX WARNING: Missing block: B:20:0x0023, code:
            if (r8 == null) goto L_0x003b;
     */
    /* JADX WARNING: Missing block: B:21:0x0025, code:
            r2 = new android.net.wifi.ScanSettings(r8);
     */
    /* JADX WARNING: Missing block: B:22:0x002e, code:
            if (r2.isValid() != false) goto L_0x003a;
     */
    /* JADX WARNING: Missing block: B:23:0x0030, code:
            android.util.Slog.e(TAG, "invalid scan setting");
     */
    /* JADX WARNING: Missing block: B:24:0x0039, code:
            return;
     */
    /* JADX WARNING: Missing block: B:25:0x003a, code:
            r8 = r2;
     */
    /* JADX WARNING: Missing block: B:26:0x003b, code:
            if (r9 == null) goto L_0x0043;
     */
    /* JADX WARNING: Missing block: B:27:0x003d, code:
            enforceWorkSourcePermission();
            r9.clearNames();
     */
    /* JADX WARNING: Missing block: B:28:0x0043, code:
            if (r9 != null) goto L_0x0054;
     */
    /* JADX WARNING: Missing block: B:30:0x0049, code:
            if (android.os.Binder.getCallingUid() < 0) goto L_0x0054;
     */
    /* JADX WARNING: Missing block: B:31:0x004b, code:
            r9 = new android.os.WorkSource(android.os.Binder.getCallingUid());
     */
    /* JADX WARNING: Missing block: B:32:0x0054, code:
            r3 = r7.mWifiStateMachine;
            r4 = android.os.Binder.getCallingUid();
            r5 = r7.scanRequestCounter;
            r7.scanRequestCounter = r5 + 1;
            r3.startScan(r4, r5, r8, r9);
     */
    /* JADX WARNING: Missing block: B:33:0x0063, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startScan(ScanSettings settings, WorkSource workSource) {
        enforceChangePermission();
        synchronized (this) {
            if (this.mInIdleMode) {
                long callingIdentity = Binder.clearCallingIdentity();
                try {
                    this.mWifiStateMachine.sendScanResultsAvailableBroadcast(false);
                    Binder.restoreCallingIdentity(callingIdentity);
                    this.mScanPending = true;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingIdentity);
                }
            }
        }
    }

    public String getWpsNfcConfigurationToken(int netId) {
        enforceConnectivityInternalPermission();
        return this.mWifiStateMachine.syncGetWpsNfcConfigurationToken(netId);
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
            startScan(null, null);
        }
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

    private void enforceConnectivityInternalPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "ConnectivityService");
    }

    private void enforceTetheringRestriction() {
        UserManager um = UserManager.get(this.mContext);
        UserHandle userHandle = Binder.getCallingUserHandle();
        Slog.d(TAG, "setWifiApEnabled - calling userId: " + userHandle.getIdentifier());
        if (um.hasUserRestriction("no_config_tethering", userHandle)) {
            throw new SecurityException("DISALLOW_CONFIG_TETHERING is enabled for this user.");
        }
    }

    /* JADX WARNING: Missing block: B:31:0x00b6, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean setWifiEnabled(boolean enable) {
        int i = 0;
        synchronized (this) {
            enforceChangePermission();
            if (SystemProperties.getInt("persist.sys.wifi_disable", 0) == 1) {
                Slog.d(TAG, "setWifiEnabled return false for spaying");
                return false;
            }
            Slog.d(TAG, "setWifiEnabled: " + enable + " pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            if (enable && this.mWifiOffReported) {
                this.mWifiOnDeffered = true;
                if (this.mVerboseLoggingEnabled) {
                    Slog.d(TAG, "Wifi off is reported through WifiOffListener, record mWifiOnDeffered=true");
                }
            }
            int wiFiEnabledState = this.mWifiStateMachine.syncGetWifiState();
            if (enable && (wiFiEnabledState == 0 || wiFiEnabledState == 1)) {
                CtaUtils.enforceCheckPermission("com.mediatek.permission.CTA_ENABLE_WIFI", "Enable WiFi");
            }
            if (!enable) {
                if (this.mWifiOffListenerCount > 0) {
                    if (this.mVerboseLoggingEnabled) {
                        Slog.d(TAG, "mWifiOffListenerCount: " + this.mWifiOffListenerCount);
                    }
                    reportWifiOff(0);
                    if (this.mVerboseLoggingEnabled) {
                        Slog.d(TAG, "Let callback to call wifi off");
                    }
                } else if (this.mVerboseLoggingEnabled) {
                    Slog.d(TAG, "mWifiOffListenerCount < 0");
                }
            }
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mSettingsStore.handleWifiToggled(enable)) {
                    Binder.restoreCallingIdentity(ident);
                    WifiController wifiController = this.mWifiController;
                    if (this.mWifiIpoOff) {
                        i = 1;
                    }
                    wifiController.obtainMessage(155656, i, Binder.getCallingUid()).sendToTarget();
                    return true;
                }
                Binder.restoreCallingIdentity(ident);
                return true;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private boolean isOp01() {
        String optr = SystemProperties.get("persist.operator.optr");
        Slog.d(TAG, "isOp01: optr=" + optr);
        if (optr == null || !optr.equals("OP01")) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:16:0x007b, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:36:0x00b8, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean setWifiEnabledForIPO(boolean enable) {
        int i = 0;
        synchronized (this) {
            Slog.d(TAG, "setWifiEnabledForIPO:" + enable + ", pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
            enforceChangePermission();
            if (enable) {
                this.mWifiIpoOff = false;
            } else {
                this.mWifiIpoOff = true;
                this.mSettingsStore.setCheckSavedStateAtBoot(false);
            }
            if (!enable) {
                if (this.mWifiOffListenerCount > 0) {
                    if (this.mVerboseLoggingEnabled) {
                        Slog.d(TAG, "mWifiOffListenerCount: " + this.mWifiOffListenerCount);
                    }
                    reportWifiOff(2);
                    if (this.mVerboseLoggingEnabled) {
                        Slog.d(TAG, "Let callback to call wifi off");
                    }
                } else if (this.mVerboseLoggingEnabled) {
                    Slog.d(TAG, "mWifiOffListenerCount < 0");
                }
            }
            WifiController wifiController = this.mWifiController;
            if (this.mWifiIpoOff) {
                i = 1;
            }
            wifiController.obtainMessage(155656, i, Binder.getCallingUid()).sendToTarget();
            if (enable) {
                if (!this.mIsReceiverRegistered) {
                    registerForBroadcasts();
                    this.mIsReceiverRegistered = true;
                }
            } else if (this.mIsReceiverRegistered) {
                this.mContext.unregisterReceiver(this.mReceiver);
                this.mIsReceiverRegistered = false;
            }
        }
    }

    public int getWifiEnabledState() {
        enforceAccessPermission();
        return this.mWifiStateMachine.syncGetWifiState();
    }

    public void setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        int i = 1;
        Slog.d(TAG, "setWifiApEnabled:" + enabled + ", pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", wifiConfig:" + wifiConfig);
        enforceChangePermission();
        if (SystemProperties.getInt("persist.sys.ap_disable", 0) == 1) {
            Slog.d(TAG, "setWifiApEnabled return for spaying");
            return;
        }
        ConnectivityManager.enforceTetherChangePermission(this.mContext);
        enforceTetheringRestriction();
        Slog.d(TAG, "setWifiApEnabled - passed the config_tethering check");
        if (Binder.getCallingUserHandle().getIdentifier() != 0) {
            Slog.e(TAG, "Only the device owner can enable wifi tethering");
            return;
        }
        if (wifiConfig == null || isValid(wifiConfig)) {
            Slog.d(TAG, "setWifiApEnabled: " + enabled + " pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            WifiController wifiController = this.mWifiController;
            if (!enabled) {
                i = 0;
            }
            wifiController.obtainMessage(155658, i, 0, wifiConfig).sendToTarget();
        } else {
            Slog.e(TAG, "Invalid WifiConfiguration");
        }
    }

    public int getWifiApEnabledState() {
        enforceAccessPermission();
        return this.mWifiStateMachine.syncGetWifiApState();
    }

    public WifiConfiguration getWifiApConfiguration() {
        enforceAccessPermission();
        enforceTetheringRestriction();
        if (Binder.getCallingUserHandle().getIdentifier() == 0) {
            return this.mWifiStateMachine.syncGetWifiApConfiguration();
        }
        Slog.e(TAG, "Only the device owner can retrieve the ap config");
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0010 A:{ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception), Splitter: B:2:0x0009} */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0010 A:{ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception), Splitter: B:2:0x0009} */
    /* JADX WARNING: Missing block: B:5:0x0010, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:6:0x0011, code:
            android.util.Log.e(TAG, "Failed to parse wi-fi configuration: " + r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WifiConfiguration buildWifiConfig(String uriString, String mimeType, byte[] data) {
        if (mimeType.equals(ConfigBuilder.WifiConfigType)) {
            try {
                return ConfigBuilder.buildConfig(uriString, data, this.mContext);
            } catch (Exception e) {
            }
        } else {
            Log.i(TAG, "Unknown wi-fi config type: " + mimeType);
            return null;
        }
    }

    public void setWifiApConfiguration(WifiConfiguration wifiConfig) {
        Slog.d(TAG, "setWifiApConfiguration: " + wifiConfig);
        enforceChangePermission();
        enforceTetheringRestriction();
        if (Binder.getCallingUserHandle().getIdentifier() != 0) {
            Slog.e(TAG, "Only the device owner can set the ap config");
        } else if (wifiConfig != null) {
            if (isValid(wifiConfig)) {
                this.mWifiStateMachine.setWifiApConfiguration(wifiConfig);
            } else {
                Slog.e(TAG, "Invalid WifiConfiguration");
            }
        }
    }

    public boolean isScanAlwaysAvailable() {
        enforceAccessPermission();
        return this.mSettingsStore.isScanAlwaysAvailable();
    }

    public void disconnect() {
        Slog.d(TAG, "disconnect, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
        enforceChangePermission();
        this.mWifiStateMachine.disconnectCommand();
    }

    public void reconnect() {
        Slog.d(TAG, "reconnect, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
        enforceChangePermission();
        this.mWifiStateMachine.reconnectCommand();
    }

    public void reassociate() {
        Slog.d(TAG, "reassociate, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
        enforceChangePermission();
        this.mWifiStateMachine.reassociateCommand();
    }

    public int getSupportedFeatures() {
        enforceAccessPermission();
        if (this.mWifiStateMachineChannel != null) {
            int mWifiSupportedFeatures = this.mWifiStateMachine.syncGetSupportedFeatures(this.mWifiStateMachineChannel);
            if (1 == Global.getInt(this.mContext.getContentResolver(), WIFI_5G_BAND_SUPPORT, 0)) {
                mWifiSupportedFeatures |= 2;
            }
            return mWifiSupportedFeatures;
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return 0;
    }

    public void requestActivityInfo(ResultReceiver result) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("controller_activity", reportActivityInfo());
        result.send(0, bundle);
    }

    public WifiActivityEnergyInfo reportActivityInfo() {
        enforceAccessPermission();
        if ((getSupportedFeatures() & 65536) == 0) {
            return null;
        }
        WifiActivityEnergyInfo wifiActivityEnergyInfo = null;
        if (this.mWifiStateMachineChannel != null) {
            WifiLinkLayerStats stats = this.mWifiStateMachine.syncGetLinkLayerStats(this.mWifiStateMachineChannel);
            if (stats != null) {
                long[] txTimePerLevel;
                long rxIdleCurrent = (long) this.mContext.getResources().getInteger(17694780);
                long rxCurrent = (long) this.mContext.getResources().getInteger(17694781);
                long txCurrent = (long) this.mContext.getResources().getInteger(17694782);
                double voltage = ((double) this.mContext.getResources().getInteger(17694783)) / 1000.0d;
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
                wifiActivityEnergyInfo = new WifiActivityEnergyInfo(SystemClock.elapsedRealtime(), 3, (long) stats.tx_time, txTimePerLevel, (long) stats.rx_time, rxIdleTime, energyUsed);
            }
            if (wifiActivityEnergyInfo == null || !wifiActivityEnergyInfo.isValid()) {
                return null;
            }
            return wifiActivityEnergyInfo;
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public List<WifiConfiguration> getConfiguredNetworks() {
        enforceAccessPermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncGetConfiguredNetworks(Binder.getCallingUid(), this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public List<WifiConfiguration> getPrivilegedConfiguredNetworks() {
        enforceReadCredentialPermission();
        enforceAccessPermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncGetPrivilegedConfiguredNetwork(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public WifiConfiguration getMatchingWifiConfig(ScanResult scanResult) {
        enforceAccessPermission();
        return this.mWifiStateMachine.syncGetMatchingWifiConfig(scanResult, this.mWifiStateMachineChannel);
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x012b A:{ExcHandler: java.security.GeneralSecurityException (r1_0 'e' java.lang.Exception), Splitter: B:26:0x00e1} */
    /* JADX WARNING: Missing block: B:31:0x012b, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:32:0x012c, code:
            android.util.Slog.e(TAG, "Failed to verify certificate" + r2.getCaCertificate().getSubjectX500Principal() + ": " + r1);
     */
    /* JADX WARNING: Missing block: B:33:0x0159, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int addOrUpdateNetwork(WifiConfiguration config) {
        Slog.d(TAG, "addOrUpdateNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", config:" + config);
        enforceChangePermission();
        if (isValid(config) && isValidPasspoint(config)) {
            if (config.isPasspoint()) {
                config.allowedProtocols.set(1);
            }
            WifiEnterpriseConfig enterpriseConfig = config.enterpriseConfig;
            if (config.isPasspoint() && (enterpriseConfig.getEapMethod() == 1 || enterpriseConfig.getEapMethod() == 2)) {
                if (config.updateIdentifier != null) {
                    enforceAccessPermission();
                } else if (SystemProperties.get("persist.wifi.hs20.test.mode").equals("1")) {
                    Slog.d(TAG, "In HS20 test mode. do not need verifyCert");
                } else {
                    try {
                        verifyCert(enterpriseConfig.getCaCertificate());
                    } catch (CertPathValidatorException cpve) {
                        Slog.e(TAG, "CA Cert " + enterpriseConfig.getCaCertificate().getSubjectX500Principal() + " untrusted: " + cpve.getMessage() + " certificate path: " + cpve.getCertPath());
                        return -1;
                    } catch (Exception e) {
                    }
                }
            }
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
        }
        Slog.e(TAG, "bad network configuration");
        return -1;
    }

    public static void verifyCert(X509Certificate caCert) throws GeneralSecurityException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        CertPathValidator validator = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
        X509Certificate[] x509CertificateArr = new X509Certificate[1];
        x509CertificateArr[0] = caCert;
        CertPath path = factory.generateCertPath(Arrays.asList(x509CertificateArr));
        KeyStore ks = KeyStore.getInstance("AndroidCAStore");
        ks.load(null, null);
        PKIXParameters params = new PKIXParameters(ks);
        params.setRevocationEnabled(false);
        validator.validate(path, params);
    }

    public boolean removeNetwork(int netId) {
        Slog.d(TAG, "removeNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId);
        enforceChangePermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncRemoveNetwork(this.mWifiStateMachineChannel, netId);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public boolean enableNetwork(int netId, boolean disableOthers) {
        Slog.d(TAG, "enableNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId + ", disableOthers:" + disableOthers);
        enforceChangePermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncEnableNetwork(this.mWifiStateMachineChannel, netId, disableOthers);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public boolean disableNetwork(int netId) {
        Slog.d(TAG, "disableNetwork, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid() + ", netId:" + netId);
        enforceChangePermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncDisableNetwork(this.mWifiStateMachineChannel, netId);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public WifiInfo getConnectionInfo() {
        enforceAccessPermission();
        return this.mWifiStateMachine.syncRequestConnectionInfo();
    }

    public List<ScanResult> getScanResults(String callingPackage) {
        List arrayList;
        enforceAccessPermission();
        int userId = UserHandle.getCallingUserId();
        int uid = Binder.getCallingUid();
        boolean canReadPeerMacAddresses = checkPeersMacAddress();
        boolean isActiveNetworkScorer = NetworkScorerAppManager.isCallerActiveScorer(this.mContext, uid);
        boolean hasInteractUsersFull = checkInteractAcrossUsersFull();
        long ident = Binder.clearCallingIdentity();
        if (!(canReadPeerMacAddresses || isActiveNetworkScorer)) {
            if (!isLocationEnabled(callingPackage)) {
                arrayList = new ArrayList();
                Binder.restoreCallingIdentity(ident);
                return arrayList;
            }
        }
        if (!(canReadPeerMacAddresses || isActiveNetworkScorer)) {
            if (!checkCallerCanAccessScanResults(callingPackage, uid)) {
                arrayList = new ArrayList();
                Binder.restoreCallingIdentity(ident);
                return arrayList;
            }
        }
        try {
            List<ScanResult> arrayList2;
            if (this.mAppOps.noteOp(10, uid, callingPackage) != 0) {
                arrayList2 = new ArrayList();
                return arrayList2;
            } else if (isCurrentProfile(userId) || hasInteractUsersFull) {
                arrayList2 = this.mWifiStateMachine.syncGetScanResultsList();
                Binder.restoreCallingIdentity(ident);
                return arrayList2;
            } else {
                arrayList = new ArrayList();
                Binder.restoreCallingIdentity(ident);
                return arrayList;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public int addPasspointManagementObject(String mo) {
        return this.mWifiStateMachine.syncAddPasspointManagementObject(this.mWifiStateMachineChannel, mo);
    }

    public int modifyPasspointManagementObject(String fqdn, List<PasspointManagementObjectDefinition> mos) {
        return this.mWifiStateMachine.syncModifyPasspointManagementObject(this.mWifiStateMachineChannel, fqdn, mos);
    }

    public void queryPasspointIcon(long bssid, String fileName) {
        this.mWifiStateMachine.syncQueryPasspointIcon(this.mWifiStateMachineChannel, bssid, fileName);
    }

    public int matchProviderWithCurrentNetwork(String fqdn) {
        return this.mWifiStateMachine.matchProviderWithCurrentNetwork(this.mWifiStateMachineChannel, fqdn);
    }

    public void deauthenticateNetwork(long holdoff, boolean ess) {
        this.mWifiStateMachine.deauthenticateNetwork(this.mWifiStateMachineChannel, holdoff, ess);
    }

    private boolean isNLP(String callingPackage) {
        for (String name : this.mNLPPackages) {
            if (name.equals(callingPackage)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLocationEnabled(String callingPackage) {
        boolean legacyForegroundApp;
        if (isMApp(this.mContext, callingPackage)) {
            legacyForegroundApp = false;
        } else {
            legacyForegroundApp = isForegroundApp(callingPackage);
        }
        if (legacyForegroundApp || Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0) != 0) {
            return true;
        }
        return isNLP(callingPackage);
    }

    private boolean checkInteractAcrossUsersFull() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0;
    }

    private boolean checkPeersMacAddress() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.PEERS_MAC_ADDRESS") == 0;
    }

    private boolean isCurrentProfile(int userId) {
        int currentUser = ActivityManager.getCurrentUser();
        if (userId == currentUser || userId == 999) {
            return true;
        }
        for (UserInfo user : this.mUserManager.getProfiles(currentUser)) {
            if (userId == user.id) {
                return true;
            }
        }
        return false;
    }

    public boolean saveConfiguration() {
        Slog.d(TAG, "saveConfiguration, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
        enforceChangePermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncSaveConfig(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return false;
    }

    public void setCountryCode(String countryCode, boolean persist) {
        Slog.i(TAG, "WifiService trying to set country code to " + countryCode + " with persist set to " + persist);
        enforceConnectivityInternalPermission();
        long token = Binder.clearCallingIdentity();
        try {
            if (this.mCountryCode.setCountryCode(countryCode, persist) && persist) {
                this.mFacade.setStringSetting(this.mContext, "wifi_country_code", countryCode);
            }
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public String getCountryCode() {
        enforceConnectivityInternalPermission();
        return this.mCountryCode.getCountryCode();
    }

    public void setFrequencyBand(int band, boolean persist) {
        enforceChangePermission();
        if (isDualBandSupported()) {
            Slog.i(TAG, "WifiService trying to set frequency band to " + band + " with persist set to " + persist);
            long token = Binder.clearCallingIdentity();
            try {
                this.mWifiStateMachine.setFrequencyBand(band, persist);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    public int getFrequencyBand() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getFrequencyBand();
    }

    public boolean isDualBandSupported() {
        return this.mContext.getResources().getBoolean(17956886);
    }

    public DhcpInfo getDhcpInfo() {
        enforceAccessPermission();
        DhcpResults dhcpResults = this.mWifiStateMachine.syncGetDhcpResults();
        DhcpInfo info = new DhcpInfo();
        if (dhcpResults.ipAddress != null && (dhcpResults.ipAddress.getAddress() instanceof Inet4Address)) {
            info.ipAddress = NetworkUtils.inetAddressToInt((Inet4Address) dhcpResults.ipAddress.getAddress());
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

    public void addToBlacklist(String bssid) {
        enforceChangePermission();
        this.mWifiStateMachine.addToBlacklist(bssid);
    }

    public void clearBlacklist() {
        enforceChangePermission();
        this.mWifiStateMachine.clearBlacklist();
    }

    public void enableTdls(String remoteAddress, boolean enable) {
        if (remoteAddress == null) {
            throw new IllegalArgumentException("remoteAddress cannot be null");
        }
        TdlsTaskParams params = new TdlsTaskParams();
        params.remoteIpAddress = remoteAddress;
        params.enable = enable;
        TdlsTask tdlsTask = new TdlsTask();
        TdlsTaskParams[] tdlsTaskParamsArr = new TdlsTaskParams[1];
        tdlsTaskParamsArr[0] = params;
        tdlsTask.execute(tdlsTaskParamsArr);
    }

    public void enableTdlsWithMacAddress(String remoteMacAddress, boolean enable) {
        if (remoteMacAddress == null) {
            throw new IllegalArgumentException("remoteMacAddress cannot be null");
        }
        this.mWifiStateMachine.enableTdls(remoteMacAddress, enable);
    }

    public Messenger getWifiServiceMessenger() {
        Slog.d(TAG, "getWifiServiceMessenger, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
        enforceAccessPermission();
        enforceChangePermission();
        return new Messenger(this.mClientHandler);
    }

    public void disableEphemeralNetwork(String SSID) {
        enforceAccessPermission();
        enforceChangePermission();
        this.mWifiStateMachine.disableEphemeralNetwork(SSID);
    }

    public String getConfigFile() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getConfigFile();
    }

    private void registerForScanModeChange() {
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_scan_always_enabled"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                WifiServiceImpl.this.mSettingsStore.handleWifiScanAlwaysAvailableToggled();
                WifiServiceImpl.this.mWifiController.sendMessage(155655);
            }
        });
    }

    private void registerForBroadcasts() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        intentFilter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        intentFilter.addAction("mediatek.intent.action.LOCATED_PLMN_CHANGED");
        if (this.mContext.getResources().getBoolean(17956892)) {
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

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump WifiService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        if (args.length > 0 && WifiMetrics.PROTO_DUMP_ARG.equals(args[0])) {
            this.mWifiStateMachine.updateWifiMetrics();
            this.mWifiMetrics.dump(fd, pw, args);
        } else if (args.length <= 0 || !"ipmanager".equals(args[0])) {
            pw.println("Wi-Fi is " + this.mWifiStateMachine.syncGetWifiStateByName());
            pw.println("Stay-awake conditions: " + Global.getInt(this.mContext.getContentResolver(), "stay_on_while_plugged_in", 0));
            pw.println("mMulticastEnabled " + this.mMulticastEnabled);
            pw.println("mMulticastDisabled " + this.mMulticastDisabled);
            pw.println("mInIdleMode " + this.mInIdleMode);
            pw.println("mScanPending " + this.mScanPending);
            this.mWifiController.dump(fd, pw, args);
            this.mSettingsStore.dump(fd, pw, args);
            this.mNotificationController.dump(fd, pw, args);
            this.mTrafficPoller.dump(fd, pw, args);
            pw.println("Latest scan results:");
            List<ScanResult> scanResults = this.mWifiStateMachine.syncGetScanResultsList();
            long nowMs = System.currentTimeMillis();
            if (!(scanResults == null || scanResults.size() == 0)) {
                pw.println("    BSSID              Frequency  RSSI    Age      SSID                                 Flags");
                for (ScanResult r : scanResults) {
                    long ageSec = 0;
                    long ageMilli = 0;
                    if (nowMs > r.seen && r.seen > 0) {
                        ageSec = (nowMs - r.seen) / 1000;
                        ageMilli = (nowMs - r.seen) % 1000;
                    }
                    String candidate = " ";
                    if (r.isAutoJoinCandidate > 0) {
                        candidate = "+";
                    }
                    String str = "  %17s  %9d  %5d  %3d.%03d%s   %-32s  %s\n";
                    Integer[] numArr = new Object[8];
                    numArr[0] = r.BSSID;
                    numArr[1] = Integer.valueOf(r.frequency);
                    numArr[2] = Integer.valueOf(r.level);
                    numArr[3] = Long.valueOf(ageSec);
                    numArr[4] = Long.valueOf(ageMilli);
                    numArr[5] = candidate;
                    numArr[6] = r.SSID == null ? "" : r.SSID;
                    numArr[7] = r.capabilities;
                    pw.printf(str, numArr);
                }
            }
            pw.println();
            pw.println("Locks held:");
            this.mWifiLockManager.dump(pw);
            pw.println();
            pw.println("Multicast Locks held:");
            for (Multicaster l : this.mMulticasters) {
                pw.print("    ");
                pw.println(l);
            }
            pw.println();
            this.mWifiStateMachine.dump(fd, pw, args);
            pw.println();
        } else {
            String[] ipManagerArgs = new String[(args.length - 1)];
            System.arraycopy(args, 1, ipManagerArgs, 0, ipManagerArgs.length);
            this.mWifiStateMachine.dumpIpManager(fd, pw, ipManagerArgs);
        }
    }

    public boolean acquireWifiLock(IBinder binder, int lockMode, String tag, WorkSource ws) {
        if (!this.mWifiLockManager.acquireWifiLock(lockMode, tag, binder, ws)) {
            return false;
        }
        this.mWifiController.sendMessage(155654);
        return true;
    }

    public void updateWifiLockWorkSource(IBinder binder, WorkSource ws) {
        this.mWifiLockManager.updateWifiLockWorkSource(binder, ws);
    }

    public boolean releaseWifiLock(IBinder binder) {
        if (!this.mWifiLockManager.releaseWifiLock(binder)) {
            return false;
        }
        this.mWifiController.sendMessage(155654);
        return true;
    }

    public void initializeMulticastFiltering() {
        enforceMulticastChangePermission();
        synchronized (this.mMulticasters) {
            if (this.mMulticasters.size() != 0) {
                return;
            }
            this.mWifiStateMachine.startFilteringMulticastPackets();
        }
    }

    public void acquireMulticastLock(IBinder binder, String tag) {
        enforceMulticastChangePermission();
        synchronized (this.mMulticasters) {
            this.mMulticastEnabled++;
            this.mMulticasters.add(new Multicaster(tag, binder));
            this.mWifiStateMachine.stopFilteringMulticastPackets();
        }
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.noteWifiMulticastEnabled(uid);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void releaseMulticastLock() {
        Slog.d(TAG, "releaseMulticastLock, pid:" + Binder.getCallingPid() + ", uid:" + Binder.getCallingUid());
        enforceMulticastChangePermission();
        int uid = Binder.getCallingUid();
        synchronized (this.mMulticasters) {
            this.mMulticastDisabled++;
            for (int i = this.mMulticasters.size() - 1; i >= 0; i--) {
                Multicaster m = (Multicaster) this.mMulticasters.get(i);
                if (m != null && m.getUid() == uid) {
                    removeMulticasterLocked(i, uid);
                }
            }
        }
    }

    private void removeMulticasterLocked(int i, int uid) {
        Multicaster removed = (Multicaster) this.mMulticasters.remove(i);
        if (removed != null) {
            removed.unlinkDeathRecipient();
        }
        if (this.mMulticasters.size() == 0) {
            this.mWifiStateMachine.startFilteringMulticastPackets();
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mBatteryStats.noteWifiMulticastDisabled(uid);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public boolean isMulticastEnabled() {
        boolean z = false;
        enforceAccessPermission();
        synchronized (this.mMulticasters) {
            if (this.mMulticasters.size() > 0) {
                z = true;
            }
        }
        return z;
    }

    public void enableVerboseLogging(int verbose) {
        boolean z = true;
        enforceAccessPermission();
        if (verbose > 0) {
            DBG = true;
        } else {
            DBG = false;
        }
        this.mWifiStateMachine.enableVerboseLogging(verbose);
        this.mWifiLockManager.enableVerboseLogging(verbose);
        if (verbose <= 0) {
            z = false;
        }
        this.mVerboseLoggingEnabled = z;
        if (SystemProperties.get("ro.mtk_internal").equals("1")) {
            String str;
            String str2 = "persist.logmuch.detect";
            if (verbose > 0) {
                str = "false";
            } else {
                str = "true";
            }
            SystemProperties.set(str2, str);
        }
    }

    public int getVerboseLoggingLevel() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getVerboseLoggingLevel();
    }

    public void enableAggressiveHandover(int enabled) {
        enforceAccessPermission();
        this.mWifiStateMachine.enableAggressiveHandover(enabled);
    }

    public int getAggressiveHandover() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getAggressiveHandover();
    }

    public void setAllowScansWithTraffic(int enabled) {
        enforceAccessPermission();
        this.mWifiStateMachine.setAllowScansWithTraffic(enabled);
    }

    public int getAllowScansWithTraffic() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getAllowScansWithTraffic();
    }

    public boolean setEnableAutoJoinWhenAssociated(boolean enabled) {
        enforceChangePermission();
        return this.mWifiStateMachine.setEnableAutoJoinWhenAssociated(enabled);
    }

    public boolean getEnableAutoJoinWhenAssociated() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getEnableAutoJoinWhenAssociated();
    }

    public WifiConnectionStatistics getConnectionStatistics() {
        enforceAccessPermission();
        enforceReadCredentialPermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncGetConnectionStatistics(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public void factoryReset() {
        enforceConnectivityInternalPermission();
        if (!this.mUserManager.hasUserRestriction("no_network_reset")) {
            if (!this.mUserManager.hasUserRestriction("no_config_tethering")) {
                setWifiApEnabled(null, false);
            }
            if (!this.mUserManager.hasUserRestriction("no_config_wifi")) {
                setWifiEnabled(true);
                this.mWifiStateMachine.factoryReset(Binder.getCallingUid());
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

    public static boolean isValidPasspoint(WifiConfiguration config) {
        String validity = checkPasspointValidity(config);
        return validity != null ? logAndReturnFalse(validity) : true;
    }

    public static String checkValidity(WifiConfiguration config) {
        if (config.allowedKeyManagement == null) {
            return "allowed kmgmt";
        }
        if (config.allowedKeyManagement.cardinality() > 1) {
            if (config.allowedKeyManagement.cardinality() != 2) {
                return "cardinality != 2";
            }
            if (!config.allowedKeyManagement.get(2)) {
                return "not WPA_EAP";
            }
            if (!(config.allowedKeyManagement.get(3) || config.allowedKeyManagement.get(1))) {
                return "not PSK or 8021X";
            }
        }
        return null;
    }

    public static String checkPasspointValidity(WifiConfiguration config) {
        if (!TextUtils.isEmpty(config.FQDN)) {
            if (!TextUtils.isEmpty(config.SSID)) {
                return "SSID not expected for Passpoint: '" + config.SSID + "' FQDN " + toHexString(config.FQDN);
            }
            if (TextUtils.isEmpty(config.providerFriendlyName)) {
                return "no provider friendly name";
            }
            WifiEnterpriseConfig enterpriseConfig = config.enterpriseConfig;
            if (enterpriseConfig == null || enterpriseConfig.getEapMethod() == -1) {
                return "no enterprise config";
            }
            if ((enterpriseConfig.getEapMethod() == 1 || enterpriseConfig.getEapMethod() == 2 || enterpriseConfig.getEapMethod() == 0) && enterpriseConfig.getCaCertificate() == null) {
                return "no CA certificate";
            }
        }
        return null;
    }

    public Network getCurrentNetwork() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getCurrentNetwork();
    }

    public static String toHexString(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('\'').append(s).append('\'');
        for (int n = 0; n < s.length(); n++) {
            Object[] objArr = new Object[1];
            objArr[0] = Integer.valueOf(s.charAt(n) & Constants.SHORT_MASK);
            sb.append(String.format(" %02x", objArr));
        }
        return sb.toString();
    }

    private boolean checkCallerCanAccessScanResults(String callingPackage, int uid) {
        if (ActivityManager.checkUidPermission("android.permission.ACCESS_FINE_LOCATION", uid) == 0 && checkAppOppAllowed(1, callingPackage, uid)) {
            return true;
        }
        if (ActivityManager.checkUidPermission("android.permission.ACCESS_COARSE_LOCATION", uid) == 0 && checkAppOppAllowed(0, callingPackage, uid)) {
            return true;
        }
        if (!isMApp(this.mContext, callingPackage) && isForegroundApp(callingPackage)) {
            return true;
        }
        Log.e(TAG, "Permission denial: Need ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get scan results");
        return false;
    }

    private boolean checkAppOppAllowed(int op, String callingPackage, int uid) {
        return this.mAppOps.noteOp(op, uid, callingPackage) == 0;
    }

    private static boolean isMApp(Context context, String pkgName) {
        boolean z = true;
        try {
            if (context.getPackageManager().getApplicationInfo(pkgName, 0).targetSdkVersion < 23) {
                z = false;
            }
            return z;
        } catch (NameNotFoundException e) {
            return true;
        }
    }

    public void hideCertFromUnaffiliatedUsers(String alias) {
        this.mCertManager.hideCertFromUnaffiliatedUsers(alias);
    }

    public String[] listClientCertsForCurrentUser() {
        return this.mCertManager.listClientCertsForCurrentUser();
    }

    private boolean isForegroundApp(String pkgName) {
        List<RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        if (tasks.isEmpty()) {
            return false;
        }
        return pkgName.equals(((RunningTaskInfo) tasks.get(0)).topActivity.getPackageName());
    }

    public void enableWifiConnectivityManager(boolean enabled) {
        enforceConnectivityInternalPermission();
        this.mWifiStateMachine.enableWifiConnectivityManager(enabled);
    }

    public boolean doCtiaTestOn() {
        enforceChangePermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncDoCtiaTestOn(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    public boolean doCtiaTestOff() {
        enforceChangePermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncDoCtiaTestOff(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    public boolean doCtiaTestRate(int rate) {
        enforceChangePermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncDoCtiaTestRate(this.mWifiStateMachineChannel, rate);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    public boolean setTxPowerEnabled(boolean enable) {
        enforceChangePermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncSetTxPowerEnabled(this.mWifiStateMachineChannel, enable);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    public boolean setTxPower(int offset) {
        enforceChangePermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncSetTxPower(this.mWifiStateMachineChannel, offset);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x004e A:{SYNTHETIC, Splitter: B:20:0x004e} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x005a A:{SYNTHETIC, Splitter: B:27:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0048 A:{SYNTHETIC, Splitter: B:16:0x0048} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<String> readClientList(String filename) {
        IOException ex;
        Throwable th;
        FileInputStream fstream = null;
        ArrayList<String> list = new ArrayList();
        try {
            FileInputStream fstream2 = new FileInputStream(filename);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fstream2)));
                while (true) {
                    String s = br.readLine();
                    if (s != null && s.length() != 0) {
                        list.add(s);
                    } else if (fstream2 != null) {
                        try {
                            fstream2.close();
                        } catch (IOException e) {
                        }
                    }
                }
                if (fstream2 != null) {
                }
            } catch (IOException e2) {
                ex = e2;
                fstream = fstream2;
                try {
                    Slog.e(TAG, "IOException:" + ex);
                    if (fstream != null) {
                        try {
                            fstream.close();
                        } catch (IOException e3) {
                        }
                    }
                    return list;
                } catch (Throwable th2) {
                    th = th2;
                    if (fstream != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fstream = fstream2;
                if (fstream != null) {
                    try {
                        fstream.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
        } catch (IOException e5) {
            ex = e5;
            Slog.e(TAG, "IOException:" + ex);
            if (fstream != null) {
            }
            return list;
        }
        return list;
    }

    public List<HotspotClient> getHotspotClients() {
        enforceAccessPermission();
        if (sCM == null) {
            sCM = (ConnectivityManager) this.mContext.getSystemService("connectivity");
            if (sCM == null) {
                return null;
            }
        }
        List<WifiDevice> wdList = sCM.getTetherConnectedSta();
        List<HotspotClient> hpList = new ArrayList();
        if (wdList != null) {
            for (WifiDevice wd : wdList) {
                hpList.add(new HotspotClient(wd.deviceAddress, false));
            }
        }
        return hpList;
    }

    public String getClientIp(String deviceAddress) {
        enforceAccessPermission();
        if (deviceAddress == null || deviceAddress.isEmpty()) {
            return null;
        }
        for (String s : readClientList("/data/misc/dhcp/dnsmasq.leases")) {
            if (s.indexOf(deviceAddress) != -1) {
                String[] fields = s.split(" ");
                if (fields.length > 3) {
                    return fields[2];
                }
            }
        }
        return null;
    }

    public void addSimCardAuthenticationService(String name, IBinder binder) {
        enforceChangePermission();
        ServiceManager.addService(name, binder);
    }

    public void suspendNotification(int type) {
        enforceChangePermission();
        this.mWifiStateMachine.suspendNotification(type);
    }

    public boolean hasConnectableAp() {
        enforceAccessPermission();
        if (!this.mSettingsStore.hasConnectableAp()) {
            return false;
        }
        boolean result = this.mWifiStateMachine.hasConnectableAp();
        if (result) {
        }
        return result;
    }

    private void initializeExtra() {
        int i = 0;
        this.mWifiOffListenerCount = 0;
        if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
            i = 1;
        }
        this.mVerboseLoggingLevel = i;
        enableVerboseLogging(this.mVerboseLoggingLevel);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int i = 0;
                WifiServiceImpl wifiServiceImpl = WifiServiceImpl.this;
                if (SystemProperties.getBoolean("persist.sys.assert.panic", false)) {
                    i = 1;
                }
                wifiServiceImpl.mVerboseLoggingLevel = i;
                WifiServiceImpl.this.enableVerboseLogging(WifiServiceImpl.this.mVerboseLoggingLevel);
            }
        }, new IntentFilter(ACTION_MTKLOGGER_STATE_CHANGED));
    }

    public String getWifiStatus() {
        enforceAccessPermission();
        String result = "";
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.getWifiStatus(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return result;
    }

    public void setPowerSavingMode(boolean mode) {
        enforceAccessPermission();
        this.mWifiStateMachine.setPowerSavingMode(mode);
    }

    public int syncGetConnectingNetworkId() {
        return -1;
    }

    public PPPOEInfo getPPPOEInfo() {
        enforceAccessPermission();
        return this.mWifiStateMachine.syncGetPppoeInfo();
    }

    public boolean setWoWlanNormalMode() {
        enforceChangePermission();
        Slog.d(TAG, "setWoWlanNormalMode");
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncSetWoWlanNormalMode(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    public boolean setWoWlanMagicMode() {
        enforceChangePermission();
        Slog.d(TAG, "setWoWlanMagicMode");
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncSetWoWlanMagicMode(this.mWifiStateMachineChannel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized!");
        return false;
    }

    public boolean is5gBandSupported() {
        String MAC_ADDRESS_FILENAME = "/data/nvram/APCFG/APRDEB/WIFI";
        String NVRAM_AGENT_SERVICE = "NvRAMAgent";
        int wifi5gBandSupported = 0;
        try {
            byte[] buffer = WifiNvRamAgent.Stub.asInterface(ServiceManager.getService("NvRAMAgent")).readFileByName("/data/nvram/APCFG/APRDEB/WIFI");
            wifi5gBandSupported = buffer[197] & buffer[Constants.ANQP_IP_ADDR_AVAILABILITY];
            Log.i(TAG, "wifiSupport5g:" + wifi5gBandSupported + ":" + buffer[197] + ":" + buffer[Constants.ANQP_IP_ADDR_AVAILABILITY]);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException ee) {
            ee.printStackTrace();
        }
        if (wifi5gBandSupported == 1) {
            return true;
        }
        return false;
    }

    public boolean setHotspotOptimization(boolean enable) {
        this.mWifiStateMachine.setHotspotOptimization(enable);
        return true;
    }

    public String getTestEnv(int channel) {
        enforceAccessPermission();
        if (this.mWifiStateMachineChannel != null) {
            return this.mWifiStateMachine.syncGetTestEnv(this.mWifiStateMachineChannel, channel);
        }
        Slog.e(TAG, "mWifiStateMachineChannel is not initialized");
        return null;
    }

    public void setTdlsPowerSave(boolean enable) {
        this.mWifiStateMachine.setTdlsPowerSave(enable);
    }

    public boolean setWifiDisabled(int flag) {
        if (this.mVerboseLoggingEnabled) {
            Slog.d(TAG, "setWifiDisabled:" + flag);
        }
        if (this.mVerboseLoggingEnabled) {
            Slog.d(TAG, "set mWifiOffReported false");
        }
        this.mWifiOffReported = false;
        if (flag == 0) {
            if (this.mWifiOnDeffered) {
                if (this.mVerboseLoggingEnabled) {
                    Slog.d(TAG, "Ignore this setWifiDisabled. Due to wifi is toggled on during reporting wifi off");
                }
                this.mWifiOnDeffered = false;
                reportWifiOff(-1);
                return true;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                if (!this.mSettingsStore.handleWifiToggled(false)) {
                    return true;
                }
                int i;
                Binder.restoreCallingIdentity(ident);
                WifiController wifiController = this.mWifiController;
                if (this.mWifiIpoOff) {
                    i = 1;
                } else {
                    i = 0;
                }
                wifiController.obtainMessage(155656, i, 0).sendToTarget();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else if (flag == 1) {
            this.mWifiController.sendMessage(155657, 1);
        } else if (flag == 2) {
            if (this.mWifiIpoOff) {
                this.mWifiController.obtainMessage(155656, 1, 0).sendToTarget();
                if (this.mIsReceiverRegistered) {
                    this.mContext.unregisterReceiver(this.mReceiver);
                    this.mIsReceiverRegistered = false;
                }
            } else {
                Slog.d(TAG, "setWifiDisabled, wifi is enabled before IPO Off complete?");
                this.mWifiController.obtainMessage(155656, 2, 0).sendToTarget();
                return true;
            }
        }
        return true;
    }

    public void registerWifiOffListener() {
        this.mWifiOffListenerCount++;
        Slog.d(TAG, "registWifiOffListener, mWifiOffListenerCount: " + this.mWifiOffListenerCount);
    }

    public void unregisterWifiOffListener() {
        this.mWifiOffListenerCount--;
        Slog.d(TAG, "unregistWifiOffListener, mWifiOffListenerCount: " + this.mWifiOffListenerCount);
    }

    private void reportWifiOff(int reason) {
        String strreason;
        if (this.mVerboseLoggingEnabled) {
            Slog.d(TAG, "set mWifiOffReported true");
        }
        this.mWifiOffReported = true;
        if (reason == 2) {
            strreason = "IPO shutdown";
        } else if (reason == 1) {
            strreason = "airplane mode on";
        } else {
            strreason = "wifi setting";
        }
        if (this.mVerboseLoggingEnabled) {
            Slog.d(TAG, "reportWifiOff, reason: " + strreason);
        }
        long ident = Binder.clearCallingIdentity();
        try {
            Intent intent = new Intent("com.mediatek.android.wifi_off_notify");
            intent.putExtra("wifi_off_reason", reason);
            this.mContext.sendBroadcast(intent);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public String getWifiPowerEventCode() {
        enforceAccessPermission();
        return this.mWifiStateMachine.getWifiPowerEventCode();
    }
}
