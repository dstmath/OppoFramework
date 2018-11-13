package com.android.server.wifi;

import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.UserInfo;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.NetworkInfo.DetailedState;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.PasspointManagementObjectDefinition;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner.PnoSettings.PnoNetwork;
import android.net.wifi.WpsInfo;
import android.net.wifi.WpsResult;
import android.os.OppoManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.LocalServices;
import com.android.server.net.DelayedDiskWrite;
import com.android.server.net.DelayedDiskWrite.Writer;
import com.android.server.net.IpConfigStore;
import com.android.server.pm.PackageManagerService;
import com.android.server.wifi.OppoNetworkRecordHelper.NetworkRecord;
import com.android.server.wifi.anqp.ANQPElement;
import com.android.server.wifi.anqp.ANQPFactory;
import com.android.server.wifi.anqp.Constants.ANQPElementType;
import com.android.server.wifi.hotspot2.ANQPData;
import com.android.server.wifi.hotspot2.AnqpCache;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.PasspointMatch;
import com.android.server.wifi.hotspot2.SupplicantBridge;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import com.android.server.wifi.hotspot2.pps.Credential;
import com.android.server.wifi.hotspot2.pps.HomeSP;
import com.mediatek.common.wifi.IWifiFwkExt;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

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
public class WifiConfigManager {
    /* renamed from: -android-net-IpConfiguration$IpAssignmentSwitchesValues */
    private static final /* synthetic */ int[] f0-android-net-IpConfiguration$IpAssignmentSwitchesValues = null;
    /* renamed from: -android-net-IpConfiguration$ProxySettingsSwitchesValues */
    private static final /* synthetic */ int[] f1-android-net-IpConfiguration$ProxySettingsSwitchesValues = null;
    /* renamed from: -android-net-NetworkInfo$DetailedStateSwitchesValues */
    private static final /* synthetic */ int[] f2-android-net-NetworkInfo$DetailedStateSwitchesValues = null;
    private static final int DATE_EQUAL = 0;
    private static final int DATE_LARGER = 1;
    private static final int DATE_SMALLER = -1;
    private static final int DATE_UNKNOWN = -100;
    private static boolean DBG = false;
    private static final int DEFAULT_MAX_DHCP_RETRIES = 9;
    private static final String DELETED_CONFIG_PSK = "Mjkd86jEMGn79KhKll298Uu7-deleted";
    private static final String DISABLE_ALERT = "android.net.wifi.DISABLE_ALERT_NETWORKS";
    private static final Boolean ENABLE_REMOVE_NETWORK_WITH_WRONGKEY = null;
    private static final String IP_CONFIG_FILE = null;
    public static final int MAX_NUM_SCAN_CACHE_ENTRIES = 128;
    public static final int MAX_RX_PACKET_FOR_FULL_SCANS = 16;
    public static final int MAX_RX_PACKET_FOR_PARTIAL_SCANS = 80;
    public static final int MAX_TX_PACKET_FOR_FULL_SCANS = 8;
    public static final int MAX_TX_PACKET_FOR_PARTIAL_SCANS = 40;
    private static final int[] NETWORK_SELECTION_DISABLE_THRESHOLD = null;
    private static final int[] NETWORK_SELECTION_DISABLE_TIMEOUT = null;
    private static final String PPS_FILE = "/data/misc/wifi/PerProviderSubscription.conf";
    private static final int REJECT_CODE_MANY_CLIENTS = 17;
    private static final String RESTORE_FILE_PATH = "/data/misc/wifi/reports/";
    public static final boolean ROAM_ON_ANY = false;
    public static final String TAG = "WifiConfigManager";
    private static final int TRIGGER_DUMP_COUNT = 1;
    private static final String WIFI_VERBOSE_LOGS_KEY = "WIFI_VERBOSE_LOGS";
    private static final String ctsPkg = "com.android.cts.verifier";
    private static final PnoListComparator sConnectedPnoListComparator = null;
    private static final PnoListComparator sDisconnectedPnoListComparator = null;
    private static boolean sVDBG;
    private static boolean sVVDBG;
    private String lastRecord;
    private ScanDetail mActiveScanDetail;
    private final Object mActiveScanDetailLock;
    public final AtomicInteger mAlwaysEnableScansWhileAssociated;
    private final AnqpCache mAnqpCache;
    private boolean mAutoJoinSwitch;
    public int mBadLinkSpeed24;
    public int mBadLinkSpeed5;
    public AtomicInteger mBandAward5Ghz;
    private Clock mClock;
    private final ConfigurationMap mConfiguredNetworks;
    private HashMap<String, Integer> mConnectFailData;
    private Context mContext;
    public AtomicInteger mCurrentNetworkBoost;
    private int mCurrentUserId;
    public Set<String> mDeletedEphemeralSSIDs;
    private List<Integer> mDisconnectNetworks;
    public final AtomicBoolean mEnableAutoJoinWhenAssociated;
    public final AtomicBoolean mEnableChipWakeUpWhenAssociated;
    public boolean mEnableLinkDebouncing;
    private final boolean mEnableOsuQueries;
    public final AtomicBoolean mEnableRssiPollWhenAssociated;
    public final AtomicInteger mEnableVerboseLogging;
    public boolean mEnableWifiCellularHandoverUserTriggeredAdjustment;
    private FrameworkFacade mFacade;
    public int mGoodLinkSpeed24;
    public int mGoodLinkSpeed5;
    private IpConfigStore mIpconfigStore;
    private final KeyStore mKeyStore;
    private int mLastPriority;
    private String mLastSelectedConfiguration;
    private long mLastSelectedTimeStamp;
    private final LocalLog mLocalLog;
    private HashSet<String> mLostConfigsDbg;
    private final PasspointManagementObjectManager mMOManager;
    public final AtomicInteger mMaxNumActiveChannelsForPartialScans;
    OppoNetworkRecordHelper mNetworkRecordHelper;
    public int mNetworkSwitchingBlackListPeriodMs;
    boolean mNewNetwork;
    private String mOldDate;
    private boolean mOnlyLinkSameCredentialConfigurations;
    public int mReasonCode;
    private final SIMAccessor mSIMAccessor;
    private ConcurrentHashMap<Integer, ScanDetailCache> mScanDetailCaches;
    private List<ScanResult> mScanResults;
    private SimpleDateFormat mSdf;
    private boolean mShowNetworks;
    private final SupplicantBridge mSupplicantBridge;
    private final SupplicantBridgeCallbacks mSupplicantBridgeCallbacks;
    public AtomicInteger mThresholdMinimumRssi24;
    public AtomicInteger mThresholdMinimumRssi5;
    public final AtomicInteger mThresholdQualifiedRssi24;
    public AtomicInteger mThresholdQualifiedRssi5;
    public AtomicInteger mThresholdSaturatedRssi24;
    public final AtomicInteger mThresholdSaturatedRssi5;
    private final UserManager mUserManager;
    private WifiNetworkAvailable mWifiAvailable;
    private final WifiConfigStore mWifiConfigStore;
    private IWifiFwkExt mWifiFwkExt;
    private final WifiNative mWifiNative;
    private final WifiNetworkHistory mWifiNetworkHistory;
    WifiStateMachine mWifiStateMachine;
    private WifiNetworkStateTraker mWifiStateTracker;
    private DelayedDiskWrite mWriter;
    public int scanResultRssiAssocReject;

    private static class PnoListComparator implements Comparator<WifiConfiguration> {
        public final int ENABLED_NETWORK_SCORE;
        public final int PERMANENTLY_DISABLED_NETWORK_SCORE;
        public final int TEMPORARY_DISABLED_NETWORK_SCORE;

        /* synthetic */ PnoListComparator(PnoListComparator pnoListComparator) {
            this();
        }

        private PnoListComparator() {
            this.ENABLED_NETWORK_SCORE = 3;
            this.TEMPORARY_DISABLED_NETWORK_SCORE = 2;
            this.PERMANENTLY_DISABLED_NETWORK_SCORE = 1;
        }

        public int compare(WifiConfiguration a, WifiConfiguration b) {
            int configAScore = getPnoNetworkSortScore(a);
            int configBScore = getPnoNetworkSortScore(b);
            if (configAScore == configBScore) {
                return compareConfigurations(a, b);
            }
            return Integer.compare(configBScore, configAScore);
        }

        public int compareConfigurations(WifiConfiguration a, WifiConfiguration b) {
            return 0;
        }

        private int getPnoNetworkSortScore(WifiConfiguration config) {
            if (config.getNetworkSelectionStatus().isNetworkEnabled()) {
                return 3;
            }
            if (config.getNetworkSelectionStatus().isNetworkTemporaryDisabled()) {
                return 2;
            }
            return 1;
        }
    }

    private class SupplicantBridgeCallbacks implements com.android.server.wifi.hotspot2.SupplicantBridge.SupplicantBridgeCallbacks {
        /* synthetic */ SupplicantBridgeCallbacks(WifiConfigManager this$0, SupplicantBridgeCallbacks supplicantBridgeCallbacks) {
            this();
        }

        private SupplicantBridgeCallbacks() {
        }

        public void notifyANQPResponse(ScanDetail scanDetail, Map<ANQPElementType, ANQPElement> anqpElements) {
            WifiConfigManager.this.updateAnqpCache(scanDetail, anqpElements);
            if (anqpElements != null && !anqpElements.isEmpty()) {
                scanDetail.propagateANQPInfo(anqpElements);
                Map<HomeSP, PasspointMatch> matches = WifiConfigManager.this.matchNetwork(scanDetail, false);
                Log.d(Utils.hs2LogTag(getClass()), scanDetail.getSSID() + " pass 2 matches: " + WifiConfigManager.toMatchString(matches));
                WifiConfigManager.this.cacheScanResultForPasspointConfigs(scanDetail, matches, null);
            }
        }

        public void notifyIconFailed(long bssid) {
            Intent intent = new Intent("android.net.wifi.PASSPOINT_ICON_RECEIVED");
            intent.addFlags(67108864);
            intent.putExtra("bssid", bssid);
            WifiConfigManager.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    /* renamed from: -getandroid-net-IpConfiguration$IpAssignmentSwitchesValues */
    private static /* synthetic */ int[] m0-getandroid-net-IpConfiguration$IpAssignmentSwitchesValues() {
        if (f0-android-net-IpConfiguration$IpAssignmentSwitchesValues != null) {
            return f0-android-net-IpConfiguration$IpAssignmentSwitchesValues;
        }
        int[] iArr = new int[IpAssignment.values().length];
        try {
            iArr[IpAssignment.DHCP.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[IpAssignment.STATIC.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[IpAssignment.UNASSIGNED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        f0-android-net-IpConfiguration$IpAssignmentSwitchesValues = iArr;
        return iArr;
    }

    /* renamed from: -getandroid-net-IpConfiguration$ProxySettingsSwitchesValues */
    private static /* synthetic */ int[] m1-getandroid-net-IpConfiguration$ProxySettingsSwitchesValues() {
        if (f1-android-net-IpConfiguration$ProxySettingsSwitchesValues != null) {
            return f1-android-net-IpConfiguration$ProxySettingsSwitchesValues;
        }
        int[] iArr = new int[ProxySettings.values().length];
        try {
            iArr[ProxySettings.NONE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ProxySettings.PAC.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ProxySettings.STATIC.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ProxySettings.UNASSIGNED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        f1-android-net-IpConfiguration$ProxySettingsSwitchesValues = iArr;
        return iArr;
    }

    /* renamed from: -getandroid-net-NetworkInfo$DetailedStateSwitchesValues */
    private static /* synthetic */ int[] m2-getandroid-net-NetworkInfo$DetailedStateSwitchesValues() {
        if (f2-android-net-NetworkInfo$DetailedStateSwitchesValues != null) {
            return f2-android-net-NetworkInfo$DetailedStateSwitchesValues;
        }
        int[] iArr = new int[DetailedState.values().length];
        try {
            iArr[DetailedState.AUTHENTICATING.ordinal()] = 10;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DetailedState.BLOCKED.ordinal()] = 11;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = 12;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DetailedState.CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[DetailedState.CONNECTING.ordinal()] = 13;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[DetailedState.DISCONNECTED.ordinal()] = 2;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[DetailedState.DISCONNECTING.ordinal()] = 14;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[DetailedState.FAILED.ordinal()] = 15;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[DetailedState.IDLE.ordinal()] = 16;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[DetailedState.OBTAINING_IPADDR.ordinal()] = 17;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[DetailedState.SCANNING.ordinal()] = 18;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[DetailedState.SUSPENDED.ordinal()] = 19;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[DetailedState.VERIFYING_POOR_LINK.ordinal()] = 20;
        } catch (NoSuchFieldError e13) {
        }
        f2-android-net-NetworkInfo$DetailedStateSwitchesValues = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiConfigManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiConfigManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiConfigManager.<clinit>():void");
    }

    void updatelastRecord(int netId) {
        if (netId != -1) {
            WifiConfiguration config = this.mConfiguredNetworks.getForAllUsers(netId);
            if (config != null) {
                if (config.SSID.equals("Singtel WIFI") || config.SSID.equals("\"Singtel WIFI\"")) {
                    this.lastRecord = "Singtel WIFI";
                }
                if (config.SSID.equals("Wireless@SGx") || config.SSID.equals("\"Wireless@SGx\"")) {
                    this.lastRecord = "Wireless@SGx";
                }
                Log.d(TAG, "zjm:updatelastRecord() -- lastRecord=" + this.lastRecord + ",config ssid=" + config.SSID);
            }
        }
    }

    void emptylastRecord() {
        this.lastRecord = "NONE";
    }

    void setlastRecord(String ssid) {
        this.lastRecord = ssid;
    }

    void changeRecord() {
        if (!this.lastRecord.equalsIgnoreCase("NONE")) {
            if (this.lastRecord.equals("Singtel WIFI")) {
                this.lastRecord = "Wireless@SGx";
            } else {
                this.lastRecord = "Singtel WIFI";
            }
        }
    }

    String getlastRecord() {
        return this.lastRecord;
    }

    WifiConfigManager(Context context, WifiNative wifiNative, FrameworkFacade facade, Clock clock, UserManager userManager, KeyStore keyStore) {
        this.mEnableAutoJoinWhenAssociated = new AtomicBoolean();
        this.mEnableChipWakeUpWhenAssociated = new AtomicBoolean(true);
        this.mEnableRssiPollWhenAssociated = new AtomicBoolean(true);
        this.mThresholdSaturatedRssi5 = new AtomicInteger();
        this.mThresholdQualifiedRssi24 = new AtomicInteger();
        this.mEnableVerboseLogging = new AtomicInteger(0);
        this.mAlwaysEnableScansWhileAssociated = new AtomicInteger(0);
        this.mMaxNumActiveChannelsForPartialScans = new AtomicInteger();
        this.mThresholdQualifiedRssi5 = new AtomicInteger();
        this.mThresholdMinimumRssi5 = new AtomicInteger();
        this.mThresholdSaturatedRssi24 = new AtomicInteger();
        this.mThresholdMinimumRssi24 = new AtomicInteger();
        this.mCurrentNetworkBoost = new AtomicInteger();
        this.mBandAward5Ghz = new AtomicInteger();
        this.mDeletedEphemeralSSIDs = new HashSet();
        this.mActiveScanDetailLock = new Object();
        this.mShowNetworks = false;
        this.mCurrentUserId = 0;
        this.mDisconnectNetworks = new ArrayList();
        this.mLastPriority = -1;
        this.mLastSelectedConfiguration = null;
        this.mLastSelectedTimeStamp = -1;
        this.mLostConfigsDbg = new HashSet();
        this.mNewNetwork = false;
        this.lastRecord = "NONE";
        this.mSdf = new SimpleDateFormat("yyyy-MM-dd");
        this.mConnectFailData = new HashMap();
        this.mOldDate = "";
        this.mScanResults = new ArrayList();
        this.scanResultRssiAssocReject = -80;
        this.mReasonCode = 0;
        this.mContext = context;
        this.mFacade = facade;
        this.mClock = clock;
        this.mKeyStore = keyStore;
        this.mUserManager = userManager;
        this.mWifiNative = wifiNative;
        this.mLocalLog = wifiNative.getLocalLog();
        this.mOnlyLinkSameCredentialConfigurations = this.mContext.getResources().getBoolean(17956895);
        this.mMaxNumActiveChannelsForPartialScans.set(this.mContext.getResources().getInteger(17694777));
        this.mEnableLinkDebouncing = this.mContext.getResources().getBoolean(17956889);
        this.mBandAward5Ghz.set(this.mContext.getResources().getInteger(17694743));
        this.mThresholdMinimumRssi5.set(this.mContext.getResources().getInteger(17694751));
        this.mThresholdQualifiedRssi5.set(this.mContext.getResources().getInteger(17694752));
        this.mThresholdSaturatedRssi5.set(this.mContext.getResources().getInteger(17694753));
        this.mThresholdMinimumRssi24.set(this.mContext.getResources().getInteger(17694754));
        this.mThresholdQualifiedRssi24.set(this.mContext.getResources().getInteger(17694755));
        this.mThresholdSaturatedRssi24.set(this.mContext.getResources().getInteger(17694756));
        this.mEnableWifiCellularHandoverUserTriggeredAdjustment = this.mContext.getResources().getBoolean(17956893);
        this.mBadLinkSpeed24 = this.mContext.getResources().getInteger(17694757);
        this.mBadLinkSpeed5 = this.mContext.getResources().getInteger(17694758);
        this.mGoodLinkSpeed24 = this.mContext.getResources().getInteger(17694759);
        this.mGoodLinkSpeed5 = this.mContext.getResources().getInteger(17694760);
        this.mEnableAutoJoinWhenAssociated.set(this.mContext.getResources().getBoolean(17956894));
        this.mCurrentNetworkBoost.set(this.mContext.getResources().getInteger(17694778));
        this.mNetworkSwitchingBlackListPeriodMs = this.mContext.getResources().getInteger(17694768);
        this.mNetworkRecordHelper = new OppoNetworkRecordHelper(this);
        boolean hs2on = this.mContext.getResources().getBoolean(17956887);
        Log.d(Utils.hs2LogTag(getClass()), "Passpoint is " + (hs2on ? "enabled" : "disabled"));
        if (SystemProperties.get("persist.wifi.hs20.test.mode").equals("1")) {
            log("In HS20 test mode. enable hs2on");
            hs2on = true;
        }
        this.mConfiguredNetworks = new ConfigurationMap(userManager);
        this.mMOManager = new PasspointManagementObjectManager(new File(PPS_FILE), hs2on);
        this.mEnableOsuQueries = true;
        this.mAnqpCache = new AnqpCache(this.mClock);
        this.mSupplicantBridgeCallbacks = new SupplicantBridgeCallbacks(this, null);
        this.mSupplicantBridge = new SupplicantBridge(wifiNative, this.mSupplicantBridgeCallbacks);
        this.mScanDetailCaches = new ConcurrentHashMap(16, 0.75f, 2);
        this.mSIMAccessor = new SIMAccessor(this.mContext);
        this.mWriter = new DelayedDiskWrite();
        this.mIpconfigStore = new IpConfigStore(this.mWriter);
        this.mWifiNetworkHistory = new WifiNetworkHistory(context, this.mLocalLog, this.mWriter);
        this.mWifiConfigStore = new WifiConfigStore(context, wifiNative, this.mKeyStore, this.mLocalLog, this.mShowNetworks, true);
        this.mOldDate = this.mSdf.format(Long.valueOf(System.currentTimeMillis()));
    }

    public void trimANQPCache(boolean all) {
        this.mAnqpCache.clear(all, DBG);
    }

    void enableVerboseLogging(int verbose) {
        this.mEnableVerboseLogging.set(verbose);
        if (verbose > 0) {
            DBG = true;
            sVDBG = true;
            this.mShowNetworks = true;
        } else {
            DBG = false;
            this.mShowNetworks = false;
            sVDBG = false;
        }
        if (verbose > 0) {
            sVVDBG = true;
        } else {
            sVVDBG = false;
        }
        this.mShowNetworks = true;
        sVDBG = true;
        this.mWifiConfigStore.enableVerboseLogging(verbose);
        this.mNetworkRecordHelper.enableVerboseLogging(verbose);
    }

    void loadAndEnableAllNetworks() {
        if (DBG) {
            log("Loading config and enabling all networks ");
        }
        loadConfiguredNetworks();
        synchronized (this.mDisconnectNetworks) {
            this.mDisconnectNetworks.clear();
        }
        enableAllNetworks();
        this.mNetworkRecordHelper.loadAllNetworkRecords();
        for (WifiConfiguration config : getSavedNetworks()) {
            if (this.mNetworkRecordHelper.getNetworkRecord(config.networkId) == null) {
                if (DBG) {
                    Log.w(TAG, "loadAndEnableAllNetworks found config not recorded:" + config.networkId + " " + config.SSID);
                }
                this.mNetworkRecordHelper.addOrUpdateNetworkRecord(config.networkId, config.SSID);
            }
        }
        if (this.mWifiFwkExt != null && this.mWifiFwkExt.hasNetworkSelection() == 3) {
            removeUserSelectionPreference("ALL");
        }
    }

    int getConfiguredNetworksSize() {
        return this.mConfiguredNetworks.sizeForCurrentUser();
    }

    List<WifiConfiguration> getConfiguredNetworks() {
        List<WifiConfiguration> networks = new ArrayList();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            WifiConfiguration newConfig = new WifiConfiguration(config);
            if (!config.ephemeral) {
                networks.add(newConfig);
            }
        }
        return networks;
    }

    private List<WifiConfiguration> getSavedNetworks(Map<String, String> pskMap) {
        List<WifiConfiguration> networks = new ArrayList();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            WifiConfiguration newConfig = new WifiConfiguration(config);
            if (!config.ephemeral) {
                if (newConfig.SSID == null) {
                    log("WifiConfiguration.SSID should not be null,skip it!");
                } else {
                    if (pskMap != null && config.allowedKeyManagement != null && config.allowedKeyManagement.get(1) && pskMap.containsKey(config.configKey(true))) {
                        newConfig.preSharedKey = (String) pskMap.get(config.configKey(true));
                    }
                    networks.add(newConfig);
                }
            }
        }
        return networks;
    }

    private List<WifiConfiguration> getAllConfiguredNetworks() {
        List<WifiConfiguration> networks = new ArrayList();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            networks.add(new WifiConfiguration(config));
        }
        return networks;
    }

    public List<WifiConfiguration> getSavedNetworks() {
        return getSavedNetworks(null);
    }

    List<WifiConfiguration> getPrivilegedSavedNetworks() {
        List<WifiConfiguration> configurations = getSavedNetworks(getCredentialsByConfigKeyMap());
        for (WifiConfiguration configuration : configurations) {
            try {
                configuration.setPasspointManagementObjectTree(this.mMOManager.getMOTree(configuration.FQDN));
            } catch (IOException ioe) {
                Log.w(TAG, "Failed to parse MO from " + configuration.FQDN + ": " + ioe);
            }
        }
        return configurations;
    }

    public Set<Integer> getHiddenConfiguredNetworkIds() {
        return this.mConfiguredNetworks.getHiddenNetworkIdsForCurrentUser();
    }

    WifiConfiguration getMatchingConfig(ScanResult scanResult) {
        if (scanResult == null) {
            return null;
        }
        for (Entry entry : this.mScanDetailCaches.entrySet()) {
            ScanDetailCache cache = (ScanDetailCache) entry.getValue();
            WifiConfiguration config = getWifiConfiguration(((Integer) entry.getKey()).intValue());
            if (config != null && cache.get(scanResult.BSSID) != null) {
                return config;
            }
        }
        return null;
    }

    private Map<String, String> getCredentialsByConfigKeyMap() {
        return readNetworkVariablesFromSupplicantFile("psk");
    }

    List<WifiConfiguration> getRecentSavedNetworks(int scanResultAgeMs, boolean copy) {
        List<WifiConfiguration> networks = new ArrayList();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            if (!config.ephemeral) {
                ScanDetailCache cache = getScanDetailCache(config);
                if (cache != null) {
                    config.setVisibility(cache.getVisibility((long) scanResultAgeMs));
                    if (!(config.visibility == null || (config.visibility.rssi5 == WifiConfiguration.INVALID_RSSI && config.visibility.rssi24 == WifiConfiguration.INVALID_RSSI))) {
                        if (copy) {
                            networks.add(new WifiConfiguration(config));
                        } else {
                            networks.add(config);
                        }
                    }
                }
            }
        }
        return networks;
    }

    void updateConfiguration(WifiInfo info) {
        WifiConfiguration config = getWifiConfiguration(info.getNetworkId());
        if (config != null && getScanDetailCache(config) != null) {
            ScanDetail scanDetail = getScanDetailCache(config).getScanDetail(info.getBSSID());
            if (scanDetail != null) {
                ScanResult result = scanDetail.getScanResult();
                long previousSeen = result.seen;
                int previousRssi = result.level;
                scanDetail.setSeen();
                result.level = info.getRssi();
                result.averageRssi(previousRssi, previousSeen, WifiQualifiedNetworkSelector.SCAN_RESULT_MAXIMUNM_AGE);
                if (sVDBG) {
                    logd("updateConfiguration freq=" + result.frequency + " BSSID=" + result.BSSID + " RSSI=" + result.level + " " + config.configKey());
                }
            }
        }
    }

    public WifiConfiguration getWifiConfiguration(int netId) {
        return this.mConfiguredNetworks.getForCurrentUser(netId);
    }

    public WifiConfiguration getWifiConfiguration(String key) {
        return this.mConfiguredNetworks.getByConfigKeyForCurrentUser(key);
    }

    void enableAllNetworks() {
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendEnableAllNetworksEvt();
            return;
        }
        boolean networkEnabledStateChanged = false;
        if (this.mAutoJoinSwitch) {
            Iterable mSortConfig = null;
            if (this.mWifiAvailable != null) {
                mSortConfig = this.mWifiAvailable.getValidSortConfigByRssi();
            } else if (this.mWifiStateTracker != null) {
                mSortConfig = this.mWifiStateTracker.getValidSortConfig();
            }
            if (mSortConfig != null) {
                for (WifiConfiguration config : mSortConfig) {
                    boolean z;
                    StringBuilder append = new StringBuilder().append("enableAllSortConfig netId=").append(Integer.toString(config.networkId)).append(" SSID=").append(config.SSID).append(" disabled=");
                    if (config.status == 1) {
                        z = true;
                    } else {
                        z = false;
                    }
                    log(append.append(z).toString());
                    WifiConfiguration sortConf = getWifiConfiguration(config.networkId);
                    if (sortConf != null && sortConf.status == 1 && enableNetwork(sortConf, false, 1000)) {
                        networkEnabledStateChanged = true;
                        sortConf.status = 2;
                        updateNetworkSelectionStatus(config.networkId, 0);
                    }
                }
            }
        }
        for (WifiConfiguration config2 : this.mConfiguredNetworks.valuesForCurrentUser()) {
            if (config2 != null && !config2.ephemeral && config2.status == 1 && this.mWifiNative.enableNetwork(config2.networkId)) {
                config2.status = 2;
                networkEnabledStateChanged = true;
                updateNetworkSelectionStatus(config2.networkId, 0);
            }
        }
        if (networkEnabledStateChanged) {
            saveConfig();
            sendConfiguredNetworksChangedBroadcast();
        }
    }

    private boolean setNetworkPriorityNative(WifiConfiguration config, int priority) {
        return this.mWifiConfigStore.setNetworkPriority(config, priority);
    }

    private boolean setSSIDNative(WifiConfiguration config, String ssid) {
        return this.mWifiConfigStore.setNetworkSSID(config, ssid);
    }

    public boolean updateLastConnectUid(WifiConfiguration config, int uid) {
        if (config == null || config.lastConnectUid == uid) {
            return false;
        }
        config.lastConnectUid = uid;
        return true;
    }

    boolean selectNetwork(WifiConfiguration config, boolean updatePriorities, int uid) {
        if (sVDBG) {
            localLogNetwork("selectNetwork", config.networkId);
        }
        if (config.networkId == -1) {
            return false;
        }
        if (WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
            if (this.mLastPriority == -1 || this.mLastPriority > 1000000) {
                if (updatePriorities) {
                    for (WifiConfiguration config2 : this.mConfiguredNetworks.valuesForCurrentUser()) {
                        if (config2.networkId != -1) {
                            setNetworkPriorityNative(config2, 0);
                        }
                    }
                }
                this.mLastPriority = 0;
            }
            if (updatePriorities) {
                int i = this.mLastPriority + 1;
                this.mLastPriority = i;
                setNetworkPriorityNative(config, i);
            }
            if (config.isPasspoint()) {
                logd("Setting SSID for WPA supplicant network " + config.networkId + " to " + config.SSID);
                setSSIDNative(config, config.SSID);
            }
            this.mWifiConfigStore.enableHS20(config.isPasspoint());
            if (updatePriorities) {
                saveConfig();
            }
            updateLastConnectUid(config, uid);
            writeKnownNetworkHistory();
            selectNetworkWithoutBroadcast(config.networkId);
            return true;
        }
        loge("selectNetwork " + Integer.toString(config.networkId) + ": Network config is not " + "visible to current user.");
        return false;
    }

    NetworkUpdateResult saveNetwork(WifiConfiguration config, int uid) {
        int i = 0;
        if (config == null || (config.networkId == -1 && config.SSID == null)) {
            return new NetworkUpdateResult(-1);
        }
        if (!WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
            return new NetworkUpdateResult(-1);
        }
        boolean newNetwork;
        if (sVDBG) {
            localLogNetwork("WifiConfigManager: saveNetwork netId", config.networkId);
        }
        if (sVDBG) {
            logd("WifiConfigManager saveNetwork, size=" + Integer.toString(this.mConfiguredNetworks.sizeForAllUsers()) + " (for all users)" + " SSID=" + config.SSID + " Uid=" + Integer.toString(config.creatorUid) + "/" + Integer.toString(config.lastUpdateUid));
        }
        if (this.mDeletedEphemeralSSIDs.remove(config.SSID) && sVDBG) {
            logd("WifiConfigManager: removed from ephemeral blacklist: " + config.SSID);
        }
        if (config.networkId == -1) {
            newNetwork = true;
        } else {
            newNetwork = false;
        }
        this.mNewNetwork = newNetwork;
        NetworkUpdateResult result = addOrUpdateNetworkNative(config, uid);
        int netId = result.getNetworkId();
        if (sVDBG) {
            localLogNetwork("WifiConfigManager: saveNetwork got it back netId=", netId);
        }
        WifiConfiguration conf = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (conf != null) {
            if (!conf.getNetworkSelectionStatus().isNetworkEnabled()) {
                if (sVDBG) {
                    localLog("WifiConfigManager: re-enabling: " + conf.SSID);
                }
                updateNetworkSelectionStatus(netId, 0);
            }
            if (sVDBG) {
                logd("WifiConfigManager: saveNetwork got config back netId=" + Integer.toString(netId) + " uid=" + Integer.toString(config.creatorUid));
            }
        }
        saveConfig();
        if (!result.isNewNetwork()) {
            i = 2;
        }
        sendConfiguredNetworksChangedBroadcast(conf, i);
        return result;
    }

    boolean disableAndRemoveNetwork(int netId, int reason) {
        sendNetworkDisabledEvt(netId, reason);
        boolean ret = disableNetwork(netId);
        WifiConfiguration network = null;
        WifiConfiguration config = getWifiConfiguration(netId);
        if (DBG && config != null) {
            loge("disableAndRemoveNetwork netId=" + Integer.toString(netId) + " SSID=" + config.SSID + " reason=" + Integer.toString(reason));
        }
        if (config != null) {
            config.disableReason = reason;
            network = config;
        }
        if (network != null) {
            sendAlertNetworksChangedBroadcast(netId, network, reason, SupplicantState.DISCONNECTED);
            sendConfiguredNetworksChangedBroadcast(network, 2);
        }
        return ret;
    }

    void sendAlertNetworksChangedBroadcast(int netId, WifiConfiguration network, int reason, SupplicantState state) {
        Intent intent = new Intent(DISABLE_ALERT);
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService("package");
        int uid = -2;
        int disableReasontoWifiSetting = 0;
        if (pm != null) {
            uid = pm.getPackageUid(ctsPkg, 65536, 0);
        }
        WifiConfiguration wc = this.mWifiStateMachine.getLastManuConnectConfiguration();
        if (wc != null) {
            if (DBG) {
                Log.d(TAG, "send last manu connect bssid:" + wc.BSSID);
            }
            network.BSSID = wc.BSSID;
        }
        if (reason == 3) {
            disableReasontoWifiSetting = 3;
        }
        if (reason == 101) {
            disableReasontoWifiSetting = 6;
        }
        if (reason == 102) {
            disableReasontoWifiSetting = 0;
        }
        if (reason == 2) {
            disableReasontoWifiSetting = 4;
        }
        if (reason == 3 || reason == 2) {
            checkRestoreConnectFailInfo(network, reason);
        }
        if (reason != -1) {
            this.mWifiStateMachine.resetVerbose();
        }
        if (DBG) {
            Log.d(TAG, "sendAlertNetworksChangedBroadcast- configssid: " + network.configKey() + ",reason = " + reason + ",disableReasontoWifiSetting = " + disableReasontoWifiSetting + ",state = " + state + ", uid= " + uid);
        }
        intent.addFlags(67108864);
        intent.putExtra("wifiConfiguration", network);
        intent.putExtra("changeReason", disableReasontoWifiSetting);
        intent.putExtra("newState", state);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        if (state == SupplicantState.DISCONNECTED && this.mWifiStateMachine.getRomUpdateBooleanValue("CONNECT_ENABLE_REMOVE_NETWORK_WITH_WRONGKEY", ENABLE_REMOVE_NETWORK_WITH_WRONGKEY).booleanValue() && ((reason == 101 || reason == 102) && !this.mWifiStateMachine.isSingtelWIFI(netId) && uid < 1000 && this.mNewNetwork)) {
            if (this.mWifiStateTracker != null) {
                this.mWifiStateTracker.rmOrupdateRecordStatus(network.configKey(false), true);
            }
            forgetNetwork(netId);
            this.mNewNetwork = false;
        } else if (state == SupplicantState.DISCONNECTED && this.mWifiStateTracker != null && reason != 5 && reason != 9) {
            this.mWifiStateTracker.rmOrupdateRecordStatus(network.configKey(false), false);
        }
    }

    void sendAlertNetworksChangedBroadcast(int netId, WifiConfiguration network, int reason) {
        Intent intent = new Intent(DISABLE_ALERT);
        int disableReasontoWifiSetting = 0;
        if (reason == 3) {
            disableReasontoWifiSetting = 3;
        }
        if (reason == 101) {
            disableReasontoWifiSetting = 6;
        }
        if (reason == 102) {
            disableReasontoWifiSetting = 0;
        }
        if (reason == 2) {
            disableReasontoWifiSetting = 4;
        }
        if (reason == 3 || reason == 2) {
            checkRestoreConnectFailInfo(network, reason);
        }
        this.mWifiStateMachine.resetVerbose();
        if (network != null && DBG) {
            Log.d(TAG, "sendAlertNetworksChangedBroadcast- configssid: " + network.configKey() + ",reason = " + reason + ",disableReasontoWifiSetting = " + disableReasontoWifiSetting);
        }
        intent.addFlags(67108864);
        intent.putExtra("wifiConfiguration", network);
        intent.putExtra("changeReason", disableReasontoWifiSetting);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* JADX WARNING: Missing block: B:63:0x014b, code:
            return;
     */
    /* JADX WARNING: Missing block: B:69:0x0173, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkRestoreConnectFailInfo(WifiConfiguration network, int reason) {
        if (SystemProperties.get("ro.secure").equals("0")) {
            if (DBG) {
                Log.d(TAG, "dbg version, no need info");
            }
        } else if (reason == 2 && this.mReasonCode == 17) {
            if (DBG) {
                Log.d(TAG, "reject. no need info");
            }
        } else {
            ScanResult sr = findHomonyAPFromScanResults(network);
            if (sr == null) {
                if (DBG) {
                    Log.d(TAG, "cannot scan. no need info");
                }
            } else if (sr.level < this.scanResultRssiAssocReject) {
                if (DBG) {
                    Log.d(TAG, "rssi is weak. no need info");
                }
            } else {
                String date = this.mSdf.format(Long.valueOf(System.currentTimeMillis()));
                if (!TextUtils.isEmpty(date)) {
                    String reasonDate = date + "-" + reason;
                    synchronized (this.mConnectFailData) {
                        int recordCount = 1;
                        int compareResult = compareDate(date, this.mOldDate);
                        if (DBG) {
                            Log.d(TAG, "date = " + compareResult);
                        }
                        if (compareResult == DATE_UNKNOWN) {
                            return;
                        }
                        if (compareResult == 1 || compareResult == -1) {
                            this.mOldDate = date;
                            this.mConnectFailData.clear();
                        }
                        Log.d(TAG, "tag:" + reasonDate);
                        if (this.mConnectFailData.containsKey(reasonDate)) {
                            Integer count = (Integer) this.mConnectFailData.get(reasonDate);
                            if (count == null) {
                                Log.d(TAG, "get data exp");
                                return;
                            }
                            recordCount = count.intValue() + 1;
                        }
                        if (DBG) {
                            Log.d(TAG, "recordCount = " + recordCount);
                        }
                        this.mConnectFailData.put(reasonDate, Integer.valueOf(recordCount));
                        if (recordCount <= this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_TRIGGER_DUMPINFO_THRESHOLD", Integer.valueOf(1)).intValue()) {
                            Log.d(TAG, "< trigger brd >");
                            if (this.mWifiStateMachine.getRomUpdateBooleanValue("CONNECT_DUMPWIFI_WITH_SCREENSHOT", Boolean.valueOf(false)).booleanValue()) {
                                SystemProperties.set("ctl.start", "dumpwifiscreen");
                            } else {
                                SystemProperties.set("ctl.start", "dumpwifi");
                            }
                        } else if (DBG) {
                            Log.d(TAG, "same type");
                        }
                    }
                }
            }
        }
    }

    private int compareDate(String currentDate, String oldDate) {
        Date date = null;
        Date lastDate = null;
        try {
            date = this.mSdf.parse(currentDate);
            lastDate = this.mSdf.parse(oldDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null || lastDate == null) {
            return DATE_UNKNOWN;
        }
        int result;
        if (date.getTime() > lastDate.getTime()) {
            result = 1;
        } else if (date.getTime() < lastDate.getTime()) {
            result = -1;
        } else {
            result = 0;
        }
        return result;
    }

    boolean disableNetwork(int netId, int reason) {
        if (DBG) {
            localLog("disableNetwork netid= " + netId);
        }
        boolean ret = this.mWifiNative.disableNetwork(netId);
        WifiConfiguration network = null;
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (DBG && config != null) {
            boolean z;
            StringBuilder append = new StringBuilder().append("disableNetwork netId=").append(Integer.toString(netId)).append(" SSID=").append(config.SSID).append(" disabled=");
            if (config.status == 1) {
                z = true;
            } else {
                z = false;
            }
            loge(append.append(z).append(" reason=").append(Integer.toString(config.disableReason)).toString());
        }
        if (config != null) {
            if (config.status != 1) {
                config.status = 1;
                config.disableReason = reason;
                network = config;
            }
            config.disableReason = reason;
            if (reason == 9) {
                config.status = 1;
            }
        }
        if (network != null) {
            sendConfiguredNetworksChangedBroadcast(network, 2);
            if (!(this.mWifiStateTracker == null || reason == 102 || reason == 5 || reason == 9)) {
                this.mWifiStateTracker.rmOrupdateRecordStatus(network.configKey(false), false);
            }
            sendNetworkDisabledEvt(network.networkId, reason);
        } else if (config != null && reason == 8) {
            sendNetworkDisabledEvt(config.networkId, reason);
        }
        return ret;
    }

    private void sendUpdateNetworkDisabledCountEvt(int netId, int reason) {
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendUpdateNetworkDisabledCountEvt(netId, reason);
        }
    }

    private void sendNetworkDisabledEvt(int netId, int reason) {
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendNetworkDisabledEvt(netId, reason);
        }
    }

    private void sendNetworkDeletedEvt(int netId) {
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendNetworkDeletedEvt(netId);
        }
    }

    boolean enableNetworkEx(int netId, boolean disableOthres, int uid, boolean sendBroadcast) {
        return enableNetworkEx(getWifiConfiguration(netId), disableOthres, uid, sendBroadcast);
    }

    boolean enableNetworkEx(WifiConfiguration config, boolean disableOthers, int uid, boolean sendBroadcast) {
        if (config == null) {
            Log.d(TAG, "config is null!!");
            return false;
        }
        Log.d(TAG, "enabel network netid:" + config.networkId + "disableOthers:" + disableOthers);
        setLatestUserSelectedConfiguration(config);
        boolean ret = true;
        if (this.mWifiNative.enableNetworkWithoutConnect(config.networkId)) {
            if (this.mWifiStateTracker != null) {
                this.mWifiStateTracker.resetConnExp(config.configKey(false));
            }
            config.status = 2;
        }
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (networkStatus != null) {
            networkStatus.setNetworkSelectionStatus(0);
        }
        if (disableOthers) {
            ret = selectNetworkWithoutBroadcast(config.networkId);
            updateLastConnectUid(config, uid);
            writeKnownNetworkHistory();
            sendConfiguredNetworksChangedBroadcast();
        } else if (sendBroadcast) {
            sendConfiguredNetworksChangedBroadcast(config, 2);
        }
        return ret;
    }

    boolean disableNetworkEx(int netId, int reason, boolean sendBroadcast) {
        return disableNetworkEx(getWifiConfiguration(netId), reason, sendBroadcast);
    }

    boolean disableNetworkEx(WifiConfiguration config, int reason, boolean sendBroadcast) {
        this.mWifiStateMachine.resetVerbose();
        if (config == null) {
            Log.d(TAG, "config is null");
            return false;
        }
        if (DBG) {
            Log.d(TAG, "disableNetworkEx netid= " + config.networkId);
        }
        boolean ret = this.mWifiNative.disableNetwork(config.networkId);
        WifiConfiguration network = null;
        if (config.status != 1) {
            sendNetworkDisabledEvt(config.networkId, reason);
            config.status = 1;
            config.disableReason = reason;
            network = config;
        }
        updateNetworkStatus(config, reason);
        if (network != null && sendBroadcast) {
            sendConfiguredNetworksChangedBroadcast(network, 2);
        }
        return ret;
    }

    public List<WifiConfiguration> getSavedNetworksAll() {
        if (this.mConfiguredNetworks == null) {
            Log.e(TAG, "mConfiguredNetworks is null");
            return null;
        }
        List<WifiConfiguration> wcList = new ArrayList();
        wcList.addAll(this.mConfiguredNetworks.valuesForAllUsers());
        return wcList;
    }

    public WifiConfiguration getWifiConfigurationForAll(int netId) {
        return this.mConfiguredNetworks.getForAllUsers(netId);
    }

    void noteRoamingFailure(WifiConfiguration config, int reason) {
        if (config != null) {
            config.lastRoamingFailure = this.mClock.currentTimeMillis();
            config.roamingFailureBlackListTimeMilli = (config.roamingFailureBlackListTimeMilli + 1000) * 2;
            if (config.roamingFailureBlackListTimeMilli > ((long) this.mNetworkSwitchingBlackListPeriodMs)) {
                config.roamingFailureBlackListTimeMilli = (long) this.mNetworkSwitchingBlackListPeriodMs;
            }
            config.lastRoamingFailureReason = reason;
        }
    }

    void saveWifiConfigBSSID(WifiConfiguration config, String bssid) {
        this.mWifiConfigStore.setNetworkBSSID(config, bssid);
    }

    void updateStatus(int netId, DetailedState state) {
        if (netId != -1) {
            WifiConfiguration config = this.mConfiguredNetworks.getForAllUsers(netId);
            if (config != null) {
                switch (m2-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[state.ordinal()]) {
                    case 1:
                        config.status = 0;
                        updateNetworkSelectionStatus(netId, 0);
                        break;
                    case 2:
                        if (config.status == 0) {
                            config.status = 2;
                            break;
                        }
                        break;
                }
            }
        }
    }

    WifiConfiguration disableEphemeralNetwork(String ssid) {
        if (ssid == null) {
            return null;
        }
        WifiConfiguration foundConfig = this.mConfiguredNetworks.getEphemeralForCurrentUser(ssid);
        this.mDeletedEphemeralSSIDs.add(ssid);
        logd("Forget ephemeral SSID " + ssid + " num=" + this.mDeletedEphemeralSSIDs.size());
        if (foundConfig != null) {
            logd("Found ephemeral config in disableEphemeralNetwork: " + foundConfig.networkId);
        }
        writeKnownNetworkHistory();
        return foundConfig;
    }

    boolean forgetNetwork(int netId) {
        if (this.mWifiAvailable != null) {
            this.mWifiAvailable.removeNetworkAvailable(netId);
        } else if (this.mWifiStateTracker != null) {
            this.mWifiStateTracker.rmConfUpdateRecord(netId);
        }
        this.mWifiStateMachine.resetVerbose();
        sendNetworkDeletedEvt(netId);
        if (this.mShowNetworks) {
            localLogNetwork("forgetNetwork", netId);
        }
        if (removeNetwork(netId)) {
            saveConfig();
            writeKnownNetworkHistory();
            this.mNetworkRecordHelper.removeNetworkRecordWithoutSaving(netId);
            return true;
        }
        loge("Failed to forget network " + netId);
        return false;
    }

    int addOrUpdateNetwork(WifiConfiguration config, int uid) {
        if (config == null || !WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
            return -1;
        }
        if (this.mShowNetworks) {
            localLogNetwork("addOrUpdateNetwork id=", config.networkId);
        }
        if (config.isPasspoint()) {
            config.SSID = getChecksum(config.FQDN).toString();
            config.enterpriseConfig.setDomainSuffixMatch(config.FQDN);
        }
        NetworkUpdateResult result = addOrUpdateNetworkNative(config, uid);
        if (result.getNetworkId() != -1) {
            WifiConfiguration conf = this.mConfiguredNetworks.getForCurrentUser(result.getNetworkId());
            if (conf != null) {
                int i;
                if (result.isNewNetwork) {
                    i = 0;
                } else {
                    i = 2;
                }
                sendConfiguredNetworksChangedBroadcast(conf, i);
            }
        }
        return result.getNetworkId();
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0007 A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:6:0x0009, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int addPasspointManagementObject(String managementObject) {
        try {
            this.mMOManager.addSP(managementObject);
            return 0;
        } catch (IOException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0007 A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:0:0x0000} */
    /* JADX WARNING: Missing block: B:5:0x0009, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int modifyPasspointMo(String fqdn, List<PasspointManagementObjectDefinition> mos) {
        try {
            return this.mMOManager.modifySP(fqdn, mos);
        } catch (IOException e) {
        }
    }

    public boolean queryPasspointIcon(long bssid, String fileName) {
        return this.mSupplicantBridge.doIconQuery(bssid, fileName);
    }

    public int matchProviderWithCurrentNetwork(String fqdn) {
        ScanDetail scanDetail;
        synchronized (this.mActiveScanDetailLock) {
            scanDetail = this.mActiveScanDetail;
        }
        if (scanDetail == null) {
            return PasspointMatch.None.ordinal();
        }
        HomeSP homeSP = this.mMOManager.getHomeSP(fqdn);
        if (homeSP == null) {
            return PasspointMatch.None.ordinal();
        }
        ANQPData anqpData = this.mAnqpCache.getEntry(scanDetail.getNetworkDetail());
        return homeSP.match(scanDetail.getNetworkDetail(), anqpData != null ? anqpData.getANQPElements() : null, this.mSIMAccessor).ordinal();
    }

    public ArrayList<PnoNetwork> retrieveDisconnectedPnoNetworkList() {
        return retrievePnoNetworkList(sDisconnectedPnoListComparator);
    }

    public ArrayList<PnoNetwork> retrieveConnectedPnoNetworkList() {
        return retrievePnoNetworkList(sConnectedPnoListComparator);
    }

    private static PnoNetwork createPnoNetworkFromWifiConfiguration(WifiConfiguration config, int newPriority) {
        PnoNetwork pnoNetwork = new PnoNetwork(config.SSID);
        pnoNetwork.networkId = config.networkId;
        pnoNetwork.priority = newPriority;
        if (config.hiddenSSID) {
            pnoNetwork.flags = (byte) (pnoNetwork.flags | 1);
        }
        pnoNetwork.flags = (byte) (pnoNetwork.flags | 2);
        pnoNetwork.flags = (byte) (pnoNetwork.flags | 4);
        if (config.allowedKeyManagement.get(1)) {
            pnoNetwork.authBitField = (byte) (pnoNetwork.authBitField | 2);
        } else if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
            pnoNetwork.authBitField = (byte) (pnoNetwork.authBitField | 4);
        } else {
            pnoNetwork.authBitField = (byte) (pnoNetwork.authBitField | 1);
        }
        return pnoNetwork;
    }

    private ArrayList<PnoNetwork> retrievePnoNetworkList(PnoListComparator pnoListComparator) {
        ArrayList<PnoNetwork> pnoList = new ArrayList();
        ArrayList<WifiConfiguration> wifiConfigurations = new ArrayList(this.mConfiguredNetworks.valuesForCurrentUser());
        Collections.sort(wifiConfigurations, pnoListComparator);
        int priority = wifiConfigurations.size();
        for (WifiConfiguration config : wifiConfigurations) {
            if (1 != config.status) {
                pnoList.add(createPnoNetworkFromWifiConfiguration(config, priority));
                priority--;
            }
        }
        return pnoList;
    }

    boolean removeNetwork(int netId) {
        if (this.mWifiAvailable != null) {
            this.mWifiAvailable.removeNetworkAvailable(netId);
        } else if (this.mWifiStateTracker != null) {
            this.mWifiStateTracker.rmConfUpdateRecord(netId);
        }
        this.mWifiStateMachine.resetVerbose();
        sendNetworkDeletedEvt(netId);
        if (this.mShowNetworks) {
            localLogNetwork("removeNetwork", netId);
        }
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (!removeConfigAndSendBroadcastIfNeeded(config)) {
            return false;
        }
        this.mNetworkRecordHelper.removeNetworkRecordWithoutSaving(netId);
        if (config.isPasspoint()) {
            writePasspointConfigs(config.FQDN, null);
        }
        return true;
    }

    private static Long getChecksum(String source) {
        Checksum csum = new CRC32();
        csum.update(source.getBytes(), 0, source.getBytes().length);
        return Long.valueOf(csum.getValue());
    }

    private boolean removeConfigWithoutBroadcast(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        if (this.mWifiConfigStore.removeNetwork(config)) {
            if (config.configKey().equals(this.mLastSelectedConfiguration)) {
                this.mLastSelectedConfiguration = null;
            }
            this.mConfiguredNetworks.remove(config.networkId);
            this.mScanDetailCaches.remove(Integer.valueOf(config.networkId));
            return true;
        }
        loge("Failed to remove network " + config.networkId);
        return false;
    }

    private boolean removeConfigAndSendBroadcastIfNeeded(WifiConfiguration config) {
        if (!removeConfigWithoutBroadcast(config)) {
            return false;
        }
        String key = config.configKey();
        if (sVDBG) {
            logd("removeNetwork  key=" + key + " config.id=" + config.networkId);
        }
        writeIpAndProxyConfigurations();
        sendConfiguredNetworksChangedBroadcast(config, 1);
        if (!config.ephemeral) {
            removeUserSelectionPreference(key);
        }
        writeKnownNetworkHistory();
        return true;
    }

    private void removeUserSelectionPreference(String configKey) {
        if (DBG) {
            Log.d(TAG, "removeUserSelectionPreference: key is " + configKey);
        }
        if (configKey != null) {
            for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
                NetworkSelectionStatus status = config.getNetworkSelectionStatus();
                String connectChoice = status.getConnectChoice();
                if (connectChoice != null && (configKey.equals("ALL") || connectChoice.equals(configKey))) {
                    Log.d(TAG, "remove connect choice:" + connectChoice + " from " + config.SSID + " : " + config.networkId);
                    status.setConnectChoice(null);
                    status.setConnectChoiceTimestamp(-1);
                }
            }
        }
    }

    boolean removeNetworksForApp(ApplicationInfo app) {
        int i = 0;
        if (app == null || app.packageName == null) {
            return false;
        }
        boolean success = true;
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForCurrentUser().toArray(new WifiConfiguration[0]);
        int length = copiedConfigs.length;
        while (i < length) {
            WifiConfiguration config = copiedConfigs[i];
            if (app.uid == config.creatorUid && app.packageName.equals(config.creatorName)) {
                if (this.mShowNetworks) {
                    localLog("Removing network " + config.SSID + ", application \"" + app.packageName + "\" uninstalled" + " from user " + UserHandle.getUserId(app.uid));
                }
                success &= removeNetwork(config.networkId);
            }
            i++;
        }
        saveConfig();
        return success;
    }

    boolean removeNetworksForUser(int userId) {
        int i = 0;
        boolean success = true;
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        int length = copiedConfigs.length;
        while (i < length) {
            WifiConfiguration config = copiedConfigs[i];
            if (userId == UserHandle.getUserId(config.creatorUid)) {
                success &= removeNetwork(config.networkId);
                if (this.mShowNetworks) {
                    localLog("Removing network " + config.SSID + ", user " + userId + " removed");
                }
            }
            i++;
        }
        saveConfig();
        return success;
    }

    boolean enableNetwork(WifiConfiguration config, boolean disableOthers, int uid) {
        if (config == null) {
            return false;
        }
        updateNetworkSelectionStatus(config, 0);
        setLatestUserSelectedConfiguration(config);
        boolean ret = true;
        if (this.mWifiNative.enableNetwork(config.networkId)) {
            config.status = 2;
        }
        if (disableOthers) {
            ret = selectNetworkWithoutBroadcast(config.networkId);
            if (sVDBG) {
                localLogNetwork("enableNetwork(disableOthers=true, uid=" + uid + ") ", config.networkId);
            }
            updateLastConnectUid(config, uid);
            writeKnownNetworkHistory();
            sendConfiguredNetworksChangedBroadcast();
        } else {
            if (sVDBG) {
                localLogNetwork("enableNetwork(disableOthers=false) ", config.networkId);
            }
            sendConfiguredNetworksChangedBroadcast(config, 2);
        }
        return ret;
    }

    boolean selectNetworkWithoutBroadcast(int netId) {
        return this.mWifiConfigStore.selectNetwork(this.mConfiguredNetworks.getForCurrentUser(netId), this.mConfiguredNetworks.valuesForCurrentUser());
    }

    boolean disableNetworkNative(WifiConfiguration config) {
        return this.mWifiConfigStore.disableNetwork(config);
    }

    void disableAllNetworksNative() {
        this.mWifiConfigStore.disableAllNetworks(this.mConfiguredNetworks.valuesForCurrentUser());
    }

    boolean disableNetwork(int netId) {
        return this.mWifiConfigStore.disableNetwork(this.mConfiguredNetworks.getForCurrentUser(netId));
    }

    boolean updateNetworkSelectionStatus(int netId, int reason) {
        return updateNetworkSelectionStatus(getWifiConfiguration(netId), reason);
    }

    boolean updateNetworkSelectionStatus(WifiConfiguration config, int reason) {
        if (config == null) {
            return false;
        }
        if (!(reason == 0 || config.status == 1)) {
            sendUpdateNetworkDisabledCountEvt(config.networkId, reason);
        }
        if (reason < 0 || reason >= 11) {
            if (DBG) {
                localLog("Invalid Network disable reason:" + reason + "return false");
            }
            return false;
        }
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason == 0) {
            updateNetworkStatus(config, 0);
            localLog("Enable network:" + config.configKey());
            return true;
        }
        networkStatus.incrementDisableReasonCounter(reason);
        if (DBG) {
            localLog("Network:" + config.SSID + "disable counter of " + NetworkSelectionStatus.getNetworkDisableReasonString(reason) + " is: " + networkStatus.getDisableReasonCounter(reason) + "and threshold is: " + NETWORK_SELECTION_DISABLE_THRESHOLD[reason]);
        }
        if (networkStatus.getDisableReasonCounter(reason) >= NETWORK_SELECTION_DISABLE_THRESHOLD[reason]) {
            return updateNetworkStatus(config, reason);
        }
        return true;
    }

    public boolean tryEnableQualifiedNetwork(int networkId) {
        WifiConfiguration config = getWifiConfiguration(networkId);
        if (config != null) {
            return tryEnableQualifiedNetwork(config);
        }
        localLog("updateQualifiedNetworkstatus invalid network.");
        return false;
    }

    private boolean tryEnableQualifiedNetwork(WifiConfiguration config) {
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (networkStatus.isNetworkTemporaryDisabled()) {
            long timeDifference = ((this.mClock.elapsedRealtime() - networkStatus.getDisableTime()) / 1000) / 60;
            if (timeDifference < 0 || timeDifference >= ((long) NETWORK_SELECTION_DISABLE_TIMEOUT[networkStatus.getNetworkSelectionDisableReason()])) {
                updateNetworkSelectionStatus(config.networkId, 0);
                return true;
            }
        }
        return false;
    }

    boolean updateNetworkStatus(WifiConfiguration config, int reason) {
        localLog("updateNetworkStatus:" + (config == null ? null : config.SSID));
        if (config == null) {
            return false;
        }
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason < 0 || reason >= 11) {
            localLog("Invalid Network disable reason:" + reason);
            return false;
        }
        if (reason == 0) {
            if (networkStatus.isNetworkEnabled()) {
                if (DBG) {
                    localLog("Need not change Qualified network Selection status since already enabled");
                }
                return false;
            }
            networkStatus.setNetworkSelectionStatus(0);
            networkStatus.setNetworkSelectionDisableReason(reason);
            networkStatus.setDisableTime(-1);
            networkStatus.clearDisableReasonCounter();
            String disableTime = DateFormat.getDateTimeInstance().format(new Date());
            if (DBG) {
                localLog("Re-enable network: " + config.SSID + " at " + disableTime);
            }
            sendConfiguredNetworksChangedBroadcast(config, 2);
        } else if (networkStatus.isNetworkPermanentlyDisabled()) {
            if (DBG) {
                localLog("Do nothing. Alreay permanent disabled! " + NetworkSelectionStatus.getNetworkDisableReasonString(reason));
            }
            return false;
        } else if (!networkStatus.isNetworkTemporaryDisabled() || reason >= 6) {
            if (networkStatus.isNetworkEnabled()) {
                sendNetworkDisabledEvt(config.networkId, reason);
                disableNetworkNative(config);
                if (OppoAutoConnectManager.getInstance() != null) {
                    OppoAutoConnectManager.getInstance().sendEnableAllNetworksEvt();
                }
                if (!(this.mWifiStateTracker == null || reason == 102 || reason == 5 || reason == 9)) {
                    this.mWifiStateTracker.rmOrupdateRecordStatus(config.configKey(false), false);
                }
                if (reason == 2) {
                    ScanResult sr = findHomonyAPFromScanResults(config);
                    int rssi;
                    if (sr == null) {
                        rssi = 0;
                        loge("chuck writeLogToPartition:TYPE_WIFI_ASSOC_REJECT-can't get rssi");
                    } else {
                        rssi = sr.level;
                        loge("chuck writeLogToPartition:TYPE_WIFI_ASSOC_REJECT rssi = " + rssi);
                    }
                    if (this.mContext != null) {
                        if (this.mReasonCode == 13 || this.mReasonCode == 14 || this.mReasonCode == 15 || this.mReasonCode == 17 || rssi < this.scanResultRssiAssocReject) {
                            loge("chuck writeLogToPartition:not really failed");
                        } else {
                            loge("chuck writeLogToPartition:TYPE_WIFI_CONNECT_FAILED");
                            OppoManager.writeLogToPartition(OppoManager.TYPE_WIFI_CONNECT_FAILED, null, "CONNECTIVITY", "wifi_connecting_failure", this.mContext.getResources().getString(17040939));
                        }
                    }
                    this.mReasonCode = 0;
                } else if (reason == 4) {
                    checkRestoreConnectFailInfo(config, reason);
                    this.mWifiStateMachine.resetVerbose();
                    if (DBG) {
                        loge("chuck writeLogToPartition:TYPE_WIFI_CONNECT_FAILED");
                    }
                    OppoManager.writeLogToPartition(OppoManager.TYPE_WIFI_CONNECT_FAILED, null, "CONNECTIVITY", "wifi_connecting_failure", this.mContext.getResources().getString(17040939));
                }
                sendConfiguredNetworksChangedBroadcast(config, 2);
                localLog("Disable network " + config.SSID + " reason:" + NetworkSelectionStatus.getNetworkDisableReasonString(reason));
            }
            if (reason < 6) {
                networkStatus.setNetworkSelectionStatus(1);
                networkStatus.setDisableTime(this.mClock.elapsedRealtime());
            } else {
                networkStatus.setNetworkSelectionStatus(2);
            }
            networkStatus.setNetworkSelectionDisableReason(reason);
            if (DBG) {
                localLog("Network:" + config.SSID + "Configure new status:" + networkStatus.getNetworkStatusString() + " with reason:" + networkStatus.getNetworkDisableReasonString() + " at: " + DateFormat.getDateTimeInstance().format(new Date()));
            }
        } else {
            if (DBG) {
                localLog("Do nothing. Already temporarily disabled! " + NetworkSelectionStatus.getNetworkDisableReasonString(reason));
            }
            return false;
        }
        return true;
    }

    boolean saveConfig() {
        return this.mWifiConfigStore.saveConfig();
    }

    WpsResult startWpsWithPinFromAccessPoint(WpsInfo config) {
        return this.mWifiConfigStore.startWpsWithPinFromAccessPoint(config, this.mConfiguredNetworks.valuesForCurrentUser());
    }

    WpsResult startWpsWithPinFromDevice(WpsInfo config) {
        return this.mWifiConfigStore.startWpsWithPinFromDevice(config, this.mConfiguredNetworks.valuesForCurrentUser());
    }

    WpsResult startWpsPbc(WpsInfo config) {
        return this.mWifiConfigStore.startWpsPbc(config, this.mConfiguredNetworks.valuesForCurrentUser());
    }

    StaticIpConfiguration getStaticIpConfiguration(int netId) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (config != null) {
            return config.getStaticIpConfiguration();
        }
        return null;
    }

    void setStaticIpConfiguration(int netId, StaticIpConfiguration staticIpConfiguration) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (config != null) {
            config.setStaticIpConfiguration(staticIpConfiguration);
        }
    }

    void setDefaultGwMacAddress(int netId, String macAddress) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (config != null) {
            config.defaultGwMacAddress = macAddress;
        }
    }

    ProxyInfo getProxyProperties(int netId) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (config != null) {
            return config.getHttpProxy();
        }
        return null;
    }

    boolean isUsingStaticIp(int netId) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (config == null || config.getIpAssignment() != IpAssignment.STATIC) {
            return false;
        }
        return true;
    }

    boolean isEphemeral(int netId) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        return config != null ? config.ephemeral : false;
    }

    boolean getMeteredHint(int netId) {
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        return config != null ? config.meteredHint : false;
    }

    private void sendConfiguredNetworksChangedBroadcast(WifiConfiguration network, int reason) {
        Intent intent = new Intent("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("multipleChanges", false);
        intent.putExtra("wifiConfiguration", network);
        intent.putExtra("changeReason", reason);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    void sendConfiguredNetworksChangedBroadcast() {
        Intent intent = new Intent("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("multipleChanges", true);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    void loadConfiguredNetworks() {
        Map<String, WifiConfiguration> configs = new HashMap();
        SparseArray<Map<String, String>> networkExtras = new SparseArray();
        this.mLastPriority = this.mWifiConfigStore.loadNetworks(configs, networkExtras);
        readNetworkHistory(configs);
        readPasspointConfig(configs, networkExtras);
        this.mConfiguredNetworks.clear();
        this.mScanDetailCaches.clear();
        for (Entry<String, WifiConfiguration> entry : configs.entrySet()) {
            WifiConfiguration config = (WifiConfiguration) entry.getValue();
            if (((String) entry.getKey()).equals(config.configKey())) {
                this.mConfiguredNetworks.put(config);
            } else {
                if (this.mShowNetworks) {
                    log("Ignoring network " + config.networkId + " because the configKey loaded " + "from wpa_supplicant.conf is not valid.");
                }
                this.mWifiConfigStore.removeNetwork(config);
            }
        }
        readIpAndProxyConfigurations();
        sendConfiguredNetworksChangedBroadcast();
        if (this.mShowNetworks) {
            localLog("loadConfiguredNetworks loaded " + this.mConfiguredNetworks.sizeForAllUsers() + " networks (for all users)");
        }
        if (this.mConfiguredNetworks.sizeForAllUsers() == 0) {
            logKernelTime();
            logContents(WifiConfigStore.SUPPLICANT_CONFIG_FILE);
            logContents(WifiConfigStore.SUPPLICANT_CONFIG_FILE_BACKUP);
            logContents(WifiNetworkHistory.NETWORK_HISTORY_CONFIG_FILE);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x00f8 A:{SYNTHETIC, Splitter: B:26:0x00f8} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x00a8 A:{SYNTHETIC, Splitter: B:19:0x00a8} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0103 A:{SYNTHETIC, Splitter: B:31:0x0103} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void logContents(String file) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        localLogAndLogcat("--- Begin " + file + " ---");
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(file));
            try {
                for (String line = reader2.readLine(); line != null; line = reader2.readLine()) {
                    localLogAndLogcat(line);
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e3) {
                    }
                }
                reader = reader2;
            } catch (FileNotFoundException e4) {
                e = e4;
                reader = reader2;
                localLog("Could not open " + file + ", " + e);
                Log.w(TAG, "Could not open " + file + ", " + e);
                if (reader != null) {
                }
                localLogAndLogcat("--- End " + file + " Contents ---");
            } catch (IOException e5) {
                e2 = e5;
                reader = reader2;
                try {
                    localLog("Could not read " + file + ", " + e2);
                    Log.w(TAG, "Could not read " + file + ", " + e2);
                    if (reader != null) {
                    }
                    localLogAndLogcat("--- End " + file + " Contents ---");
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e6) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            localLog("Could not open " + file + ", " + e);
            Log.w(TAG, "Could not open " + file + ", " + e);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e8) {
                }
            }
            localLogAndLogcat("--- End " + file + " Contents ---");
        } catch (IOException e9) {
            e2 = e9;
            localLog("Could not read " + file + ", " + e2);
            Log.w(TAG, "Could not read " + file + ", " + e2);
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e10) {
                }
            }
            localLogAndLogcat("--- End " + file + " Contents ---");
        }
        localLogAndLogcat("--- End " + file + " Contents ---");
    }

    private Map<String, String> readNetworkVariablesFromSupplicantFile(String key) {
        return this.mWifiConfigStore.readNetworkVariablesFromSupplicantFile(key);
    }

    private String readNetworkVariableFromSupplicantFile(String configKey, String key) {
        long start = SystemClock.elapsedRealtimeNanos();
        Map<String, String> data = this.mWifiConfigStore.readNetworkVariablesFromSupplicantFile(key);
        long end = SystemClock.elapsedRealtimeNanos();
        if (sVDBG) {
            localLog("readNetworkVariableFromSupplicantFile configKey=[" + configKey + "] key=" + key + " duration=" + (end - start));
        }
        return (String) data.get(configKey);
    }

    boolean needsUnlockedKeyStore() {
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            if (config.allowedKeyManagement.get(2) && config.allowedKeyManagement.get(3) && needsSoftwareBackedKeyStore(config.enterpriseConfig)) {
                return true;
            }
        }
        return false;
    }

    void readPasspointConfig(Map<String, WifiConfiguration> configs, SparseArray<Map<String, String>> networkExtras) {
        try {
            int matchedConfigs = 0;
            for (HomeSP homeSp : this.mMOManager.loadAllSPs()) {
                String fqdn = homeSp.getFQDN();
                Log.d(TAG, "Looking for " + fqdn);
                for (WifiConfiguration config : configs.values()) {
                    Log.d(TAG, "Testing " + config.SSID);
                    if (config.enterpriseConfig != null) {
                        String configFqdn = (String) ((Map) networkExtras.get(config.networkId)).get(WifiConfigStore.ID_STRING_KEY_FQDN);
                        if (configFqdn != null && configFqdn.equals(fqdn)) {
                            Log.d(TAG, "Matched " + configFqdn + " with " + config.networkId);
                            matchedConfigs++;
                            config.FQDN = fqdn;
                            config.providerFriendlyName = homeSp.getFriendlyName();
                            HashSet<Long> roamingConsortiumIds = homeSp.getRoamingConsortiums();
                            config.roamingConsortiumIds = new long[roamingConsortiumIds.size()];
                            int i = 0;
                            for (Long longValue : roamingConsortiumIds) {
                                config.roamingConsortiumIds[i] = longValue.longValue();
                                i++;
                            }
                            IMSIParameter imsiParameter = homeSp.getCredential().getImsi();
                            config.enterpriseConfig.setPlmn(imsiParameter != null ? imsiParameter.toString() : null);
                            config.enterpriseConfig.setRealm(homeSp.getCredential().getRealm());
                        }
                    }
                }
            }
            Log.d(TAG, "loaded " + matchedConfigs + " passpoint configs");
        } catch (IOException e) {
            loge("Could not read /data/misc/wifi/PerProviderSubscription.conf : " + e);
        }
    }

    public void writePasspointConfigs(final String fqdn, final HomeSP homeSP) {
        this.mWriter.write(PPS_FILE, new Writer() {
            public void onWriteCalled(DataOutputStream out) throws IOException {
                try {
                    if (homeSP != null) {
                        WifiConfigManager.this.mMOManager.addSP(homeSP);
                    } else {
                        WifiConfigManager.this.mMOManager.removeSP(fqdn);
                    }
                } catch (IOException e) {
                    WifiConfigManager.this.loge("Could not write /data/misc/wifi/PerProviderSubscription.conf : " + e);
                }
            }
        }, false);
    }

    private void readNetworkHistory(Map<String, WifiConfiguration> configs) {
        this.mWifiNetworkHistory.readNetworkHistory(configs, this.mScanDetailCaches, this.mDeletedEphemeralSSIDs);
    }

    public void writeKnownNetworkHistory() {
        List<WifiConfiguration> networks = new ArrayList();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForAllUsers()) {
            networks.add(new WifiConfiguration(config));
        }
        this.mWifiNetworkHistory.writeKnownNetworkHistory(networks, this.mScanDetailCaches, this.mDeletedEphemeralSSIDs);
    }

    public void setAndEnableLastSelectedConfiguration(int netId) {
        if (sVDBG) {
            logd("setLastSelectedConfiguration " + Integer.toString(netId));
        }
        if (netId == -1) {
            this.mLastSelectedConfiguration = null;
            this.mLastSelectedTimeStamp = -1;
            return;
        }
        WifiConfiguration selected = getWifiConfiguration(netId);
        if (selected == null) {
            this.mLastSelectedConfiguration = null;
            this.mLastSelectedTimeStamp = -1;
            return;
        }
        this.mLastSelectedConfiguration = selected.configKey();
        this.mLastSelectedTimeStamp = this.mClock.elapsedRealtime();
        updateNetworkSelectionStatus(netId, 0);
        if (sVDBG) {
            logd("setLastSelectedConfiguration now: " + this.mLastSelectedConfiguration);
        }
    }

    public void setLatestUserSelectedConfiguration(WifiConfiguration network) {
        if (network != null) {
            this.mLastSelectedConfiguration = network.configKey();
            this.mLastSelectedTimeStamp = this.mClock.elapsedRealtime();
        }
    }

    public String getLastSelectedConfiguration() {
        return this.mLastSelectedConfiguration;
    }

    public long getLastSelectedTimeStamp() {
        return this.mLastSelectedTimeStamp;
    }

    public boolean isLastSelectedConfiguration(WifiConfiguration config) {
        if (this.mLastSelectedConfiguration == null || config == null) {
            return false;
        }
        return this.mLastSelectedConfiguration.equals(config.configKey());
    }

    void writeIpAndProxyConfigurations() {
        SparseArray<IpConfiguration> networks = new SparseArray();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForAllUsers()) {
            if (!config.ephemeral) {
                networks.put(configKey(config), config.getIpConfiguration());
            }
        }
        this.mIpconfigStore.writeIpAndProxyConfigurations(IP_CONFIG_FILE, networks);
    }

    private void readIpAndProxyConfigurations() {
        SparseArray<IpConfiguration> networks = this.mIpconfigStore.readIpAndProxyConfigurations(IP_CONFIG_FILE);
        if (networks != null && networks.size() != 0) {
            for (int i = 0; i < networks.size(); i++) {
                int id = networks.keyAt(i);
                WifiConfiguration config = this.mConfiguredNetworks.getByConfigKeyIDForAllUsers(id);
                if (config == null || config.ephemeral) {
                    logd("configuration found for missing network, nid=" + id + ", ignored, networks.size=" + Integer.toString(networks.size()));
                } else {
                    config.setIpConfiguration((IpConfiguration) networks.valueAt(i));
                }
            }
        }
    }

    private NetworkUpdateResult addOrUpdateNetworkNative(WifiConfiguration config, int uid) {
        IOException ioe;
        if (sVDBG) {
            localLog("addOrUpdateNetworkNative " + config.getPrintableSsid());
        }
        if (!config.isPasspoint() || this.mMOManager.isEnabled()) {
            WifiConfiguration currentConfig;
            boolean newNetwork = false;
            boolean existingMO = false;
            if (config.networkId == -1) {
                currentConfig = this.mConfiguredNetworks.getByConfigKeyForCurrentUser(config.configKey());
                if (currentConfig != null) {
                    config.networkId = currentConfig.networkId;
                } else {
                    if (this.mMOManager.getHomeSP(config.FQDN) != null) {
                        logd("addOrUpdateNetworkNative passpoint " + config.FQDN + " was found, but no network Id");
                        existingMO = true;
                    }
                    newNetwork = true;
                }
            } else {
                currentConfig = this.mConfiguredNetworks.getForCurrentUser(config.networkId);
            }
            WifiConfiguration wifiConfiguration = new WifiConfiguration(currentConfig);
            if (!this.mWifiConfigStore.addOrUpdateNetwork(config, currentConfig)) {
                return new NetworkUpdateResult(-1);
            }
            HomeSP homeSP;
            int netId = config.networkId;
            String savedConfigKey = config.configKey();
            boolean simSlotChanged = false;
            String lastSimSlot = null;
            if (currentConfig != null) {
                lastSimSlot = currentConfig.simSlot;
            }
            if (currentConfig == null) {
                currentConfig = new WifiConfiguration();
                currentConfig.setIpAssignment(IpAssignment.DHCP);
                currentConfig.setProxySettings(ProxySettings.NONE);
                currentConfig.networkId = netId;
                if (config != null) {
                    currentConfig.selfAdded = config.selfAdded;
                    currentConfig.didSelfAdd = config.didSelfAdd;
                    currentConfig.ephemeral = config.ephemeral;
                    currentConfig.meteredHint = config.meteredHint;
                    currentConfig.useExternalScores = config.useExternalScores;
                    currentConfig.lastConnectUid = config.lastConnectUid;
                    currentConfig.lastUpdateUid = config.lastUpdateUid;
                    currentConfig.creatorUid = config.creatorUid;
                    currentConfig.creatorName = config.creatorName;
                    currentConfig.lastUpdateName = config.lastUpdateName;
                    currentConfig.peerWifiConfiguration = config.peerWifiConfiguration;
                    currentConfig.FQDN = config.FQDN;
                    currentConfig.providerFriendlyName = config.providerFriendlyName;
                    currentConfig.roamingConsortiumIds = config.roamingConsortiumIds;
                    currentConfig.validatedInternetAccess = config.validatedInternetAccess;
                    currentConfig.numNoInternetAccessReports = config.numNoInternetAccessReports;
                    currentConfig.updateTime = config.updateTime;
                    currentConfig.creationTime = config.creationTime;
                    currentConfig.shared = config.shared;
                }
                if (DBG) {
                    log("created new config netId=" + Integer.toString(netId) + " uid=" + Integer.toString(currentConfig.creatorUid) + " name=" + currentConfig.creatorName);
                }
            }
            if (existingMO) {
                homeSP = null;
            } else if (config.isPasspoint()) {
                try {
                    if (config.updateIdentifier == null) {
                        Credential credential = new Credential(config.enterpriseConfig, this.mKeyStore, !newNetwork);
                        HashSet<Long> roamingConsortiumIds = new HashSet();
                        for (long valueOf : config.roamingConsortiumIds) {
                            roamingConsortiumIds.add(Long.valueOf(valueOf));
                        }
                        homeSP = new HomeSP(Collections.emptyMap(), config.FQDN, roamingConsortiumIds, Collections.emptySet(), Collections.emptySet(), Collections.emptyList(), config.providerFriendlyName, null, credential);
                        try {
                            log("created a homeSP object for " + config.networkId + ":" + config.SSID);
                        } catch (IOException e) {
                            ioe = e;
                            Log.e(TAG, "Failed to create Passpoint config: " + ioe);
                            return new NetworkUpdateResult(-1);
                        }
                    }
                    homeSP = null;
                    currentConfig.enterpriseConfig.setRealm(config.enterpriseConfig.getRealm());
                    currentConfig.enterpriseConfig.setPlmn(config.enterpriseConfig.getPlmn());
                } catch (IOException e2) {
                    ioe = e2;
                    homeSP = null;
                    Log.e(TAG, "Failed to create Passpoint config: " + ioe);
                    return new NetworkUpdateResult(-1);
                }
            } else {
                homeSP = null;
            }
            if (uid != -1) {
                if (newNetwork) {
                    currentConfig.creatorUid = uid;
                } else {
                    currentConfig.lastUpdateUid = uid;
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("time=");
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(this.mClock.currentTimeMillis());
            Object[] objArr = new Object[6];
            objArr[0] = c;
            objArr[1] = c;
            objArr[2] = c;
            objArr[3] = c;
            objArr[4] = c;
            objArr[5] = c;
            sb.append(String.format("%tm-%td %tH:%tM:%tS.%tL", objArr));
            if (newNetwork) {
                currentConfig.creationTime = sb.toString();
            } else {
                currentConfig.updateTime = sb.toString();
            }
            if (currentConfig.status == 2) {
                updateNetworkSelectionStatus(currentConfig.networkId, 0);
            }
            if (currentConfig.configKey().equals(getLastSelectedConfiguration()) && currentConfig.ephemeral) {
                currentConfig.ephemeral = false;
                if (DBG) {
                    log("remove ephemeral status netId=" + Integer.toString(netId) + " " + currentConfig.configKey());
                }
            }
            if (sVDBG) {
                log("will read network variables netId=" + Integer.toString(netId));
            }
            readNetworkVariables(currentConfig);
            if (savedConfigKey.equals(currentConfig.configKey()) || this.mWifiConfigStore.saveNetworkMetadata(currentConfig)) {
                boolean passwordChanged = false;
                if (!(newNetwork || config.preSharedKey == null || config.preSharedKey.equals("*"))) {
                    passwordChanged = true;
                }
                if (newNetwork || passwordChanged || wasCredentialChange(wifiConfiguration, currentConfig)) {
                    currentConfig.getNetworkSelectionStatus().setHasEverConnected(false);
                }
                if (config.lastUpdateName != null) {
                    currentConfig.lastUpdateName = config.lastUpdateName;
                }
                if (config.lastUpdateUid != -1) {
                    currentConfig.lastUpdateUid = config.lastUpdateUid;
                }
                if (!newNetwork) {
                    if (currentConfig.simSlot != null && currentConfig.simSlot.equals(lastSimSlot)) {
                        simSlotChanged = false;
                    } else if (currentConfig.simSlot == null && lastSimSlot == null) {
                        simSlotChanged = false;
                    } else {
                        simSlotChanged = true;
                    }
                }
                this.mConfiguredNetworks.put(currentConfig);
                if (newNetwork) {
                    this.mNetworkRecordHelper.addOrUpdateNetworkRecord(netId, config.SSID);
                }
                if (this.mConfiguredNetworks.sizeForAllUsers() > 100) {
                    this.mNetworkRecordHelper.clearObsoleteNetworks();
                }
                NetworkUpdateResult result = writeIpAndProxyConfigurationsOnChange(currentConfig, config, newNetwork);
                result.setIsNewNetwork(newNetwork);
                result.setNetworkId(netId);
                result.setSimslotChanged(simSlotChanged);
                if (homeSP != null) {
                    writePasspointConfigs(null, homeSP);
                }
                saveConfig();
                writeKnownNetworkHistory();
                return result;
            }
            loge("Failed to set network metadata. Removing config " + config.networkId);
            this.mWifiConfigStore.removeNetwork(config);
            return new NetworkUpdateResult(-1);
        }
        Log.e(TAG, "Passpoint is not enabled");
        return new NetworkUpdateResult(-1);
    }

    private boolean wasBitSetUpdated(BitSet originalBitSet, BitSet currentBitSet) {
        if (originalBitSet == null || currentBitSet == null) {
            if (!(originalBitSet == null && currentBitSet == null)) {
                return true;
            }
        } else if (!originalBitSet.equals(currentBitSet)) {
            return true;
        }
        return false;
    }

    private boolean wasCredentialChange(WifiConfiguration originalConfig, WifiConfiguration currentConfig) {
        if (originalConfig == null || wasBitSetUpdated(originalConfig.allowedKeyManagement, currentConfig.allowedKeyManagement) || wasBitSetUpdated(originalConfig.allowedProtocols, currentConfig.allowedProtocols) || wasBitSetUpdated(originalConfig.allowedAuthAlgorithms, currentConfig.allowedAuthAlgorithms) || wasBitSetUpdated(originalConfig.allowedPairwiseCiphers, currentConfig.allowedPairwiseCiphers) || wasBitSetUpdated(originalConfig.allowedGroupCiphers, currentConfig.allowedGroupCiphers)) {
            return true;
        }
        if (!(originalConfig.wepKeys == null || currentConfig.wepKeys == null)) {
            if (originalConfig.wepKeys.length != currentConfig.wepKeys.length) {
                return true;
            }
            for (int i = 0; i < originalConfig.wepKeys.length; i++) {
                if (!Objects.equals(originalConfig.wepKeys[i], currentConfig.wepKeys[i])) {
                    return true;
                }
            }
        }
        if (originalConfig.hiddenSSID == currentConfig.hiddenSSID && originalConfig.requirePMF == currentConfig.requirePMF && !wasEnterpriseConfigChange(originalConfig.enterpriseConfig, currentConfig.enterpriseConfig)) {
            return false;
        }
        return true;
    }

    protected boolean wasEnterpriseConfigChange(WifiEnterpriseConfig originalEnterpriseConfig, WifiEnterpriseConfig currentEnterpriseConfig) {
        if (originalEnterpriseConfig == null || currentEnterpriseConfig == null) {
            if (!(originalEnterpriseConfig == null && currentEnterpriseConfig == null)) {
                return true;
            }
        } else if (originalEnterpriseConfig.getEapMethod() != currentEnterpriseConfig.getEapMethod() || originalEnterpriseConfig.getPhase2Method() != currentEnterpriseConfig.getPhase2Method()) {
            return true;
        } else {
            X509Certificate[] originalCaCerts = originalEnterpriseConfig.getCaCertificates();
            X509Certificate[] currentCaCerts = currentEnterpriseConfig.getCaCertificates();
            if (originalCaCerts == null || currentCaCerts == null) {
                if (!(originalCaCerts == null && currentCaCerts == null)) {
                    return true;
                }
            } else if (originalCaCerts.length != currentCaCerts.length) {
                return true;
            } else {
                for (int i = 0; i < originalCaCerts.length; i++) {
                    if (!originalCaCerts[i].equals(currentCaCerts[i])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public WifiConfiguration getWifiConfigForHomeSP(HomeSP homeSP) {
        WifiConfiguration config = this.mConfiguredNetworks.getByFQDNForCurrentUser(homeSP.getFQDN());
        if (config == null) {
            Log.e(TAG, "Could not find network for homeSP " + homeSP.getFQDN());
        }
        return config;
    }

    public HomeSP getHomeSPForConfig(WifiConfiguration config) {
        WifiConfiguration storedConfig = this.mConfiguredNetworks.getForCurrentUser(config.networkId);
        if (storedConfig == null || !storedConfig.isPasspoint()) {
            return null;
        }
        return this.mMOManager.getHomeSP(storedConfig.FQDN);
    }

    public ScanDetailCache getScanDetailCache(WifiConfiguration config) {
        if (config == null) {
            return null;
        }
        ScanDetailCache cache = (ScanDetailCache) this.mScanDetailCaches.get(Integer.valueOf(config.networkId));
        if (cache == null && config.networkId != -1) {
            cache = new ScanDetailCache(config);
            this.mScanDetailCaches.put(Integer.valueOf(config.networkId), cache);
        }
        return cache;
    }

    public void linkConfiguration(WifiConfiguration config) {
        if (!WifiConfigurationUtil.isVisibleToAnyProfile(config, this.mUserManager.getProfiles(this.mCurrentUserId))) {
            logd("linkConfiguration: Attempting to link config " + config.configKey() + " that is not visible to the current user.");
        } else if ((getScanDetailCache(config) == null || getScanDetailCache(config).size() <= 6) && config.allowedKeyManagement.get(1)) {
            Map<String, String> psk_data = readNetworkVariablesFromSupplicantFile("psk");
            for (WifiConfiguration link : this.mConfiguredNetworks.valuesForCurrentUser()) {
                boolean doLink = false;
                if (!(link.configKey().equals(config.configKey()) || link.ephemeral || !link.allowedKeyManagement.equals(config.allowedKeyManagement))) {
                    ScanDetailCache linkedScanDetailCache = getScanDetailCache(link);
                    if (linkedScanDetailCache == null || linkedScanDetailCache.size() <= 6) {
                        if (config.defaultGwMacAddress == null || link.defaultGwMacAddress == null) {
                            if (getScanDetailCache(config) != null && getScanDetailCache(config).size() <= 6) {
                                for (String abssid : getScanDetailCache(config).keySet()) {
                                    for (String bbssid : linkedScanDetailCache.keySet()) {
                                        if (sVVDBG) {
                                            logd("linkConfiguration try to link due to DBDC BSSID match " + link.SSID + " and " + config.SSID + " bssida " + abssid + " bssidb " + bbssid);
                                        }
                                        if (abssid.regionMatches(true, 0, bbssid, 0, 16)) {
                                            doLink = true;
                                        }
                                    }
                                }
                            }
                        } else if (config.defaultGwMacAddress.equals(link.defaultGwMacAddress)) {
                            if (sVDBG) {
                                logd("linkConfiguration link due to same gw " + link.SSID + " and " + config.SSID + " GW " + config.defaultGwMacAddress);
                            }
                            doLink = true;
                        }
                        if (doLink && this.mOnlyLinkSameCredentialConfigurations) {
                            if (DBG) {
                                log("readNetworkVariableFromSupplicantFile ssid=[" + link.configKey() + "] key=psk");
                            }
                            if (DBG) {
                                log("readNetworkVariableFromSupplicantFile ssid=[" + config.configKey() + "] key=psk");
                            }
                            String apsk = (String) psk_data.get(link.configKey());
                            String bpsk = (String) psk_data.get(config.configKey());
                            if (apsk == null || bpsk == null || TextUtils.isEmpty(apsk) || TextUtils.isEmpty(apsk) || apsk.equals("*") || apsk.equals(DELETED_CONFIG_PSK) || !apsk.equals(bpsk)) {
                                doLink = false;
                            }
                        }
                        if (doLink) {
                            if (sVDBG) {
                                logd("linkConfiguration: will link " + link.configKey() + " and " + config.configKey());
                            }
                            if (link.linkedConfigurations == null) {
                                link.linkedConfigurations = new HashMap();
                            }
                            if (config.linkedConfigurations == null) {
                                config.linkedConfigurations = new HashMap();
                            }
                            if (link.linkedConfigurations.get(config.configKey()) == null) {
                                link.linkedConfigurations.put(config.configKey(), Integer.valueOf(1));
                            }
                            if (config.linkedConfigurations.get(link.configKey()) == null) {
                                config.linkedConfigurations.put(link.configKey(), Integer.valueOf(1));
                            }
                        } else {
                            if (!(link.linkedConfigurations == null || link.linkedConfigurations.get(config.configKey()) == null)) {
                                if (sVDBG) {
                                    logd("linkConfiguration: un-link " + config.configKey() + " from " + link.configKey());
                                }
                                link.linkedConfigurations.remove(config.configKey());
                            }
                            if (!(config.linkedConfigurations == null || config.linkedConfigurations.get(link.configKey()) == null)) {
                                if (sVDBG) {
                                    logd("linkConfiguration: un-link " + link.configKey() + " from " + config.configKey());
                                }
                                config.linkedConfigurations.remove(link.configKey());
                            }
                        }
                    }
                }
            }
        }
    }

    public HashSet<Integer> makeChannelList(WifiConfiguration config, int age) {
        if (config == null) {
            return null;
        }
        long now_ms = this.mClock.currentTimeMillis();
        HashSet<Integer> channels = new HashSet();
        if (getScanDetailCache(config) == null && config.linkedConfigurations == null) {
            return null;
        }
        ScanResult result;
        if (sVDBG) {
            StringBuilder dbg = new StringBuilder();
            dbg.append("makeChannelList age=").append(Integer.toString(age)).append(" for ").append(config.configKey()).append(" max=").append(this.mMaxNumActiveChannelsForPartialScans);
            if (getScanDetailCache(config) != null) {
                dbg.append(" bssids=").append(getScanDetailCache(config).size());
            }
            if (config.linkedConfigurations != null) {
                dbg.append(" linked=").append(config.linkedConfigurations.size());
            }
            logd(dbg.toString());
        }
        int numChannels = 0;
        if (getScanDetailCache(config) != null && getScanDetailCache(config).size() > 0) {
            for (ScanDetail scanDetail : getScanDetailCache(config).values()) {
                result = scanDetail.getScanResult();
                if (numChannels > this.mMaxNumActiveChannelsForPartialScans.get()) {
                    break;
                }
                if (sVDBG) {
                    logd("has " + result.BSSID + " freq=" + Integer.toString(result.frequency) + " age=" + Long.toString(now_ms - result.seen) + " ?=" + (now_ms - result.seen < ((long) age)));
                }
                if (now_ms - result.seen < ((long) age)) {
                    channels.add(Integer.valueOf(result.frequency));
                    numChannels++;
                }
            }
        }
        if (config.linkedConfigurations != null) {
            for (String key : config.linkedConfigurations.keySet()) {
                WifiConfiguration linked = getWifiConfiguration(key);
                if (linked != null && getScanDetailCache(linked) != null) {
                    for (ScanDetail scanDetail2 : getScanDetailCache(linked).values()) {
                        result = scanDetail2.getScanResult();
                        if (sVDBG) {
                            logd("has link: " + result.BSSID + " freq=" + Integer.toString(result.frequency) + " age=" + Long.toString(now_ms - result.seen));
                        }
                        if (numChannels > this.mMaxNumActiveChannelsForPartialScans.get()) {
                            break;
                        } else if (now_ms - result.seen < ((long) age)) {
                            channels.add(Integer.valueOf(result.frequency));
                            numChannels++;
                        }
                    }
                }
            }
        }
        return channels;
    }

    private Map<HomeSP, PasspointMatch> matchPasspointNetworks(ScanDetail scanDetail) {
        if (!this.mMOManager.isConfigured()) {
            return null;
        }
        NetworkDetail networkDetail = scanDetail.getNetworkDetail();
        if (!networkDetail.hasInterworking()) {
            return null;
        }
        updateAnqpCache(scanDetail, networkDetail.getANQPElements());
        Map<HomeSP, PasspointMatch> matches = matchNetwork(scanDetail, true);
        Log.d(Utils.hs2LogTag(getClass()), scanDetail.getSSID() + " pass 1 matches: " + toMatchString(matches));
        return matches;
    }

    private Map<HomeSP, PasspointMatch> matchNetwork(ScanDetail scanDetail, boolean query) {
        NetworkDetail networkDetail = scanDetail.getNetworkDetail();
        ANQPData anqpData = this.mAnqpCache.getEntry(networkDetail);
        Map anqpElements = anqpData != null ? anqpData.getANQPElements() : null;
        boolean queried = !query;
        Collection<HomeSP> homeSPs = this.mMOManager.getLoadedSPs().values();
        Map<HomeSP, PasspointMatch> matches = new HashMap(homeSPs.size());
        Log.d(Utils.hs2LogTag(getClass()), "match nwk " + scanDetail.toKeyString() + ", anqp " + (anqpData != null ? "present" : "missing") + ", query " + query + ", home sps: " + homeSPs.size());
        for (HomeSP homeSP : homeSPs) {
            PasspointMatch match = homeSP.match(networkDetail, anqpElements, this.mSIMAccessor);
            Log.d(Utils.hs2LogTag(getClass()), " -- " + homeSP.getFQDN() + ": match " + match + ", queried " + queried);
            if ((match == PasspointMatch.Incomplete || this.mEnableOsuQueries) && !queried) {
                List<ANQPElementType> querySet = ANQPFactory.buildQueryList(networkDetail, match == PasspointMatch.Incomplete, this.mEnableOsuQueries);
                if (networkDetail.queriable(querySet)) {
                    querySet = this.mAnqpCache.initiate(networkDetail, querySet);
                    if (querySet != null) {
                        this.mSupplicantBridge.startANQP(scanDetail, querySet);
                    }
                }
                queried = true;
            }
            matches.put(homeSP, match);
        }
        return matches;
    }

    public Map<ANQPElementType, ANQPElement> getANQPData(NetworkDetail network) {
        ANQPData data = this.mAnqpCache.getEntry(network);
        if (data != null) {
            return data.getANQPElements();
        }
        return null;
    }

    public SIMAccessor getSIMAccessor() {
        return this.mSIMAccessor;
    }

    public void notifyANQPDone(Long bssid, boolean success) {
        this.mSupplicantBridge.notifyANQPDone(bssid, success);
    }

    public void notifyIconReceived(IconEvent iconEvent) {
        Intent intent = new Intent("android.net.wifi.PASSPOINT_ICON_RECEIVED");
        intent.addFlags(67108864);
        intent.putExtra("bssid", iconEvent.getBSSID());
        intent.putExtra("file", iconEvent.getFileName());
        try {
            intent.putExtra("icon", this.mSupplicantBridge.retrieveIcon(iconEvent));
        } catch (IOException e) {
        }
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void updateAnqpCache(ScanDetail scanDetail, Map<ANQPElementType, ANQPElement> anqpElements) {
        NetworkDetail networkDetail = scanDetail.getNetworkDetail();
        if (anqpElements == null) {
            ANQPData data = this.mAnqpCache.getEntry(networkDetail);
            if (data != null) {
                scanDetail.propagateANQPInfo(data.getANQPElements());
            }
            return;
        }
        this.mAnqpCache.update(networkDetail, anqpElements);
    }

    private static String toMatchString(Map<HomeSP, PasspointMatch> matches) {
        StringBuilder sb = new StringBuilder();
        for (Entry<HomeSP, PasspointMatch> entry : matches.entrySet()) {
            sb.append(' ').append(((HomeSP) entry.getKey()).getFQDN()).append("->").append(entry.getValue());
        }
        return sb.toString();
    }

    private void cacheScanResultForPasspointConfigs(ScanDetail scanDetail, Map<HomeSP, PasspointMatch> matches, List<WifiConfiguration> associatedWifiConfigurations) {
        for (Entry<HomeSP, PasspointMatch> entry : matches.entrySet()) {
            PasspointMatch match = (PasspointMatch) entry.getValue();
            if (match == PasspointMatch.HomeProvider || match == PasspointMatch.RoamingProvider) {
                WifiConfiguration config = getWifiConfigForHomeSP((HomeSP) entry.getKey());
                if (config != null) {
                    cacheScanResultForConfig(config, scanDetail, (PasspointMatch) entry.getValue());
                    if (associatedWifiConfigurations != null) {
                        associatedWifiConfigurations.add(config);
                    }
                } else {
                    Log.w(Utils.hs2LogTag(getClass()), "Failed to find config for '" + ((HomeSP) entry.getKey()).getFQDN() + "'");
                }
            }
        }
    }

    private void cacheScanResultForConfig(WifiConfiguration config, ScanDetail scanDetail, PasspointMatch passpointMatch) {
        ScanResult scanResult = scanDetail.getScanResult();
        ScanDetailCache scanDetailCache = getScanDetailCache(config);
        if (scanDetailCache == null) {
            Log.w(TAG, "Could not allocate scan cache for " + config.SSID);
            return;
        }
        ScanResult result = scanDetailCache.get(scanResult.BSSID);
        if (result != null) {
            scanResult.blackListTimestamp = result.blackListTimestamp;
            scanResult.numIpConfigFailures = result.numIpConfigFailures;
            scanResult.numConnection = result.numConnection;
            scanResult.isAutoJoinCandidate = result.isAutoJoinCandidate;
        }
        if (config.ephemeral) {
            scanResult.untrusted = true;
        }
        if (scanDetailCache.size() > 192) {
            long now_dbg = 0;
            if (sVVDBG) {
                logd(" Will trim config " + config.configKey() + " size " + scanDetailCache.size());
                for (ScanDetail sd : scanDetailCache.values()) {
                    logd("     " + sd.getBSSIDString() + " " + sd.getSeen());
                }
                now_dbg = SystemClock.elapsedRealtimeNanos();
            }
            scanDetailCache.trim(128);
            if (sVVDBG) {
                logd(" Finished trimming config, time(ns) " + (SystemClock.elapsedRealtimeNanos() - now_dbg));
                for (ScanDetail sd2 : scanDetailCache.values()) {
                    logd("     " + sd2.getBSSIDString() + " " + sd2.getSeen());
                }
            }
        }
        if (passpointMatch != null) {
            scanDetailCache.put(scanDetail, passpointMatch, getHomeSPForConfig(config));
        } else {
            scanDetailCache.put(scanDetail);
        }
        linkConfiguration(config);
    }

    private boolean isEncryptionWep(String encryption) {
        return encryption.contains("WEP");
    }

    private boolean isEncryptionPsk(String encryption) {
        return encryption.contains("PSK");
    }

    private boolean isEncryptionEap(String encryption) {
        return encryption.contains("EAP");
    }

    private boolean isEncryptionWapi(String encryption) {
        return encryption.contains("WAPI");
    }

    public boolean isOpenNetwork(String encryption) {
        if (isEncryptionWep(encryption) || isEncryptionPsk(encryption) || isEncryptionEap(encryption) || isEncryptionWapi(encryption)) {
            return false;
        }
        return true;
    }

    public boolean isOpenNetwork(ScanResult scan) {
        return isOpenNetwork(scan.capabilities);
    }

    public boolean isOpenNetwork(WifiConfiguration config) {
        return isOpenNetwork(config.configKey());
    }

    public List<WifiConfiguration> getSavedNetworkFromScanDetail(ScanDetail scanDetail) {
        ScanResult scanResult = scanDetail.getScanResult();
        if (scanResult == null) {
            return null;
        }
        List<WifiConfiguration> savedWifiConfigurations = new ArrayList();
        String ssid = "\"" + scanResult.SSID + "\"";
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            if (config.SSID != null && config.SSID.equals(ssid)) {
                String scanResultEncrypt = scanResult.capabilities;
                String configEncrypt = config.configKey();
                if ((isEncryptionWep(scanResultEncrypt) && isEncryptionWep(configEncrypt)) || ((isEncryptionPsk(scanResultEncrypt) && isEncryptionPsk(configEncrypt)) || ((isEncryptionEap(scanResultEncrypt) && isEncryptionEap(configEncrypt)) || ((isOpenNetwork(scanResultEncrypt) && isOpenNetwork(configEncrypt)) || this.mWifiFwkExt.getSecurity(scanResult) == this.mWifiFwkExt.getSecurity(config))))) {
                    savedWifiConfigurations.add(config);
                }
            }
        }
        return savedWifiConfigurations;
    }

    public List<WifiConfiguration> updateSavedNetworkWithNewScanDetail(ScanDetail scanDetail, boolean isConnectingOrConnected) {
        if (scanDetail.getScanResult() == null) {
            return null;
        }
        NetworkDetail networkDetail = scanDetail.getNetworkDetail();
        List<WifiConfiguration> associatedWifiConfigurations = new ArrayList();
        if (networkDetail.hasInterworking() && !isConnectingOrConnected) {
            Map<HomeSP, PasspointMatch> matches = matchPasspointNetworks(scanDetail);
            if (matches != null) {
                cacheScanResultForPasspointConfigs(scanDetail, matches, associatedWifiConfigurations);
            }
        }
        List<WifiConfiguration> savedConfigurations = getSavedNetworkFromScanDetail(scanDetail);
        if (savedConfigurations != null) {
            for (WifiConfiguration config : savedConfigurations) {
                cacheScanResultForConfig(config, scanDetail, null);
                associatedWifiConfigurations.add(config);
            }
        }
        if (associatedWifiConfigurations.size() == 0) {
            return null;
        }
        return associatedWifiConfigurations;
    }

    public void handleUserSwitch(int userId) {
        this.mCurrentUserId = userId;
        Set<WifiConfiguration> ephemeralConfigs = new HashSet();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
            if (config.ephemeral) {
                ephemeralConfigs.add(config);
            }
        }
        if (!ephemeralConfigs.isEmpty()) {
            for (WifiConfiguration config2 : ephemeralConfigs) {
                removeConfigWithoutBroadcast(config2);
            }
            saveConfig();
            writeKnownNetworkHistory();
        }
        for (WifiConfiguration network : this.mConfiguredNetworks.handleUserSwitch(this.mCurrentUserId)) {
            disableNetworkNative(network);
        }
        enableAllNetworks();
        sendConfiguredNetworksChangedBroadcast();
    }

    public int getCurrentUserId() {
        return this.mCurrentUserId;
    }

    public boolean isCurrentUserProfile(int userId) {
        boolean z = true;
        if (userId == this.mCurrentUserId) {
            return true;
        }
        UserInfo parent = this.mUserManager.getProfileParent(userId);
        if (parent == null || parent.id != this.mCurrentUserId) {
            z = false;
        }
        return z;
    }

    private NetworkUpdateResult writeIpAndProxyConfigurationsOnChange(WifiConfiguration currentConfig, WifiConfiguration newConfig, boolean isNewNetwork) {
        boolean ipChanged = false;
        boolean proxyChanged = false;
        switch (m0-getandroid-net-IpConfiguration$IpAssignmentSwitchesValues()[newConfig.getIpAssignment().ordinal()]) {
            case 1:
                if (currentConfig.getIpAssignment() != newConfig.getIpAssignment()) {
                    ipChanged = true;
                    break;
                }
                break;
            case 2:
                if (currentConfig.getIpAssignment() == newConfig.getIpAssignment()) {
                    if (!Objects.equals(currentConfig.getStaticIpConfiguration(), newConfig.getStaticIpConfiguration())) {
                        ipChanged = true;
                        break;
                    }
                    ipChanged = false;
                    break;
                }
                ipChanged = true;
                break;
            case 3:
                break;
            default:
                loge("Ignore invalid ip assignment during write");
                break;
        }
        switch (m1-getandroid-net-IpConfiguration$ProxySettingsSwitchesValues()[newConfig.getProxySettings().ordinal()]) {
            case 1:
                if (currentConfig.getProxySettings() != newConfig.getProxySettings()) {
                    proxyChanged = true;
                    break;
                }
                break;
            case 2:
            case 3:
                ProxyInfo newHttpProxy = newConfig.getHttpProxy();
                ProxyInfo currentHttpProxy = currentConfig.getHttpProxy();
                if (newHttpProxy == null) {
                    if (currentHttpProxy == null) {
                        proxyChanged = false;
                        break;
                    }
                    proxyChanged = true;
                    break;
                } else if (!newHttpProxy.equals(currentHttpProxy)) {
                    proxyChanged = true;
                    break;
                } else {
                    proxyChanged = false;
                    break;
                }
            case 4:
                break;
            default:
                loge("Ignore invalid proxy configuration during write");
                break;
        }
        if (ipChanged) {
            currentConfig.setIpAssignment(newConfig.getIpAssignment());
            currentConfig.setStaticIpConfiguration(newConfig.getStaticIpConfiguration());
            log("IP config changed SSID = " + currentConfig.SSID);
            if (currentConfig.getStaticIpConfiguration() != null) {
                log(" static configuration: " + currentConfig.getStaticIpConfiguration().toString());
            }
        }
        if (proxyChanged) {
            currentConfig.setProxySettings(newConfig.getProxySettings());
            currentConfig.setHttpProxy(newConfig.getHttpProxy());
            log("proxy changed SSID = " + currentConfig.SSID);
            if (currentConfig.getHttpProxy() != null) {
                log(" proxyProperties: " + currentConfig.getHttpProxy().toString());
            }
        }
        if (ipChanged || proxyChanged || isNewNetwork) {
            if (sVDBG) {
                logd("writeIpAndProxyConfigurationsOnChange: " + currentConfig.SSID + " -> " + newConfig.SSID + " path: " + IP_CONFIG_FILE);
            }
            writeIpAndProxyConfigurations();
        }
        return new NetworkUpdateResult(ipChanged, proxyChanged);
    }

    private void readNetworkVariables(WifiConfiguration config) {
        this.mWifiConfigStore.readNetworkVariables(config);
    }

    public WifiConfiguration wifiConfigurationFromScanResult(ScanResult result) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + result.SSID + "\"";
        if (sVDBG) {
            logd("WifiConfiguration from scan results " + config.SSID + " cap " + result.capabilities);
        }
        if (result.capabilities.contains("PSK") || result.capabilities.contains("EAP") || result.capabilities.contains("WEP")) {
            if (result.capabilities.contains("PSK")) {
                config.allowedKeyManagement.set(1);
            }
            if (result.capabilities.contains("EAP")) {
                config.allowedKeyManagement.set(2);
                config.allowedKeyManagement.set(3);
            }
            if (result.capabilities.contains("WEP")) {
                config.allowedKeyManagement.set(0);
                config.allowedAuthAlgorithms.set(0);
                config.allowedAuthAlgorithms.set(1);
            }
        } else {
            config.allowedKeyManagement.set(0);
        }
        return config;
    }

    public WifiConfiguration wifiConfigurationFromScanResult(ScanDetail scanDetail) {
        return wifiConfigurationFromScanResult(scanDetail.getScanResult());
    }

    private static int configKey(WifiConfiguration config) {
        return config.configKey().hashCode();
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiConfigManager");
        pw.println("mLastPriority " + this.mLastPriority);
        pw.println("Configured networks");
        for (WifiConfiguration conf : getAllConfiguredNetworks()) {
            pw.println(conf);
        }
        pw.println();
        pw.println("Network records");
        for (NetworkRecord record : this.mNetworkRecordHelper.getNetworkRecords()) {
            pw.println("dump record:" + record + " changed=" + record.changed);
        }
        pw.println();
        if (this.mLostConfigsDbg != null && this.mLostConfigsDbg.size() > 0) {
            pw.println("LostConfigs: ");
            for (String s : this.mLostConfigsDbg) {
                pw.println(s);
            }
        }
        if (this.mMOManager.isConfigured()) {
            pw.println("Begin dump of ANQP Cache");
            this.mAnqpCache.dump(pw);
            pw.println("End dump of ANQP Cache");
        }
    }

    public String getConfigFile() {
        return IP_CONFIG_FILE;
    }

    protected void logd(String s) {
        Log.d(TAG, s);
    }

    protected void loge(String s) {
        loge(s, false);
    }

    protected void loge(String s, boolean stack) {
        if (stack) {
            Log.e(TAG, s + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName() + " - " + Thread.currentThread().getStackTrace()[3].getMethodName() + " - " + Thread.currentThread().getStackTrace()[4].getMethodName() + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        } else {
            Log.e(TAG, s);
        }
    }

    private void logKernelTime() {
        long kernelTimeMs = System.nanoTime() / 1000000;
        StringBuilder builder = new StringBuilder();
        builder.append("kernel time = ").append(kernelTimeMs / 1000).append(".").append(kernelTimeMs % 1000).append("\n");
        localLog(builder.toString());
    }

    protected void log(String s) {
        Log.d(TAG, s);
    }

    private void localLog(String s) {
        if (this.mLocalLog != null) {
            this.mLocalLog.log(s);
        }
        Log.d(TAG, s);
    }

    private void localLogAndLogcat(String s) {
        localLog(s);
        Log.d(TAG, s);
    }

    private void localLogNetwork(String s, int netId) {
        if (this.mLocalLog != null) {
            WifiConfiguration config;
            synchronized (this.mConfiguredNetworks) {
                config = this.mConfiguredNetworks.getForAllUsers(netId);
            }
            if (config != null) {
                localLogAndLogcat(s + " " + config.getPrintableSsid() + " " + netId + " status=" + config.status + " key=" + config.configKey());
            } else {
                localLogAndLogcat(s + " " + netId);
            }
        }
    }

    static boolean needsSoftwareBackedKeyStore(WifiEnterpriseConfig config) {
        if (TextUtils.isEmpty(config.getClientCertificateAlias())) {
            return false;
        }
        return true;
    }

    public void resetSimNetworks(int simSlot) {
        this.mWifiConfigStore.resetSimNetworks(this.mConfiguredNetworks.valuesForCurrentUser(), simSlot);
    }

    boolean isNetworkConfigured(WifiConfiguration config) {
        boolean z = true;
        if (config.networkId != -1) {
            if (this.mConfiguredNetworks.getForCurrentUser(config.networkId) == null) {
                z = false;
            }
            return z;
        }
        if (this.mConfiguredNetworks.getByConfigKeyForCurrentUser(config.configKey()) == null) {
            z = false;
        }
        return z;
    }

    boolean canModifyNetwork(int uid, int networkId, boolean onlyAnnotate) {
        boolean z = true;
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(networkId);
        if (config == null) {
            loge("canModifyNetwork: cannot find config networkId " + networkId);
            return false;
        } else if (this.mWifiStateMachine.isSingtelWIFI(networkId)) {
            return true;
        } else {
            boolean isUidDeviceOwner;
            DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
            if (dpmi != null) {
                isUidDeviceOwner = dpmi.isActiveAdminWithPolicy(uid, -2);
            } else {
                isUidDeviceOwner = false;
            }
            if (isUidDeviceOwner) {
                return true;
            }
            boolean isCreator = config.creatorUid == uid;
            if (onlyAnnotate) {
                if (!isCreator) {
                    z = checkConfigOverridePermission(uid);
                }
                return z;
            } else if (this.mContext.getPackageManager().hasSystemFeature("android.software.device_admin") && dpmi == null) {
                return false;
            } else {
                boolean isConfigEligibleForLockdown;
                if (dpmi != null) {
                    isConfigEligibleForLockdown = dpmi.isActiveAdminWithPolicy(config.creatorUid, -2);
                } else {
                    isConfigEligibleForLockdown = false;
                }
                if (isConfigEligibleForLockdown) {
                    boolean isLockdownFeatureEnabled;
                    if (Global.getInt(this.mContext.getContentResolver(), "wifi_device_owner_configs_lockdown", 0) != 0) {
                        isLockdownFeatureEnabled = true;
                    } else {
                        isLockdownFeatureEnabled = false;
                    }
                    if (isLockdownFeatureEnabled) {
                        z = false;
                    } else {
                        z = checkConfigOverridePermission(uid);
                    }
                    return z;
                }
                if (!isCreator) {
                    z = checkConfigOverridePermission(uid);
                }
                return z;
            }
        }
    }

    boolean canModifyNetwork(int uid, WifiConfiguration config, boolean onlyAnnotate) {
        if (config == null) {
            loge("canModifyNetowrk recieved null configuration");
            return false;
        }
        int netid;
        if (config.networkId != -1) {
            netid = config.networkId;
        } else {
            WifiConfiguration test = this.mConfiguredNetworks.getByConfigKeyForCurrentUser(config.configKey());
            if (test == null) {
                return false;
            }
            netid = test.networkId;
        }
        return canModifyNetwork(uid, netid, onlyAnnotate);
    }

    boolean checkConfigOverridePermission(int uid) {
        boolean z = false;
        try {
            if (this.mFacade.checkUidPermission("android.permission.OVERRIDE_WIFI_CONFIG", uid) == 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            return false;
        }
    }

    int getMaxDhcpRetries() {
        return this.mWifiStateMachine.getRomUpdateIntegerValue("CONNECT_DEFAULT_MAX_DHCP_RETRIES", Integer.valueOf(9)).intValue();
    }

    void clearBssidBlacklist() {
        this.mWifiConfigStore.clearBssidBlacklist();
    }

    void blackListBssid(String bssid) {
        this.mWifiConfigStore.blackListBssid(bssid);
    }

    public boolean isBssidBlacklisted(String bssid) {
        return this.mWifiConfigStore.isBssidBlacklisted(bssid);
    }

    public boolean getEnableAutoJoinWhenAssociated() {
        return this.mEnableAutoJoinWhenAssociated.get();
    }

    public void setEnableAutoJoinWhenAssociated(boolean enabled) {
        this.mEnableAutoJoinWhenAssociated.set(enabled);
    }

    public void setActiveScanDetail(ScanDetail activeScanDetail) {
        synchronized (this.mActiveScanDetailLock) {
            this.mActiveScanDetail = activeScanDetail;
        }
    }

    public boolean wasEphemeralNetworkDeleted(String ssid) {
        return this.mDeletedEphemeralSSIDs.contains(ssid);
    }

    public boolean addOrUpdateNetworkRecord(int networkId, String SSID) {
        return this.mNetworkRecordHelper.addOrUpdateNetworkRecord(networkId, SSID);
    }

    boolean removeNetworkWithoutBroadcast(int netId) {
        if (!removeConfigWithoutBroadcast(netId)) {
            return false;
        }
        if (this.mWifiNative.removeNetwork(netId)) {
            this.mNetworkRecordHelper.removeNetworkRecordWithoutSaving(netId);
            return true;
        }
        loge("Failed to remove network " + netId);
        return false;
    }

    private boolean removeConfigWithoutBroadcast(int netId) {
        WifiConfiguration config = this.mConfiguredNetworks.getForAllUsers(netId);
        if (config != null) {
            if (DBG) {
                loge("removeConfigWithoutBroadcast " + Integer.toString(netId) + " key=" + config.configKey() + " config.id=" + Integer.toString(config.networkId));
            }
            if (config.configKey().equals(this.mLastSelectedConfiguration)) {
                this.mLastSelectedConfiguration = null;
            }
            if (config.enterpriseConfig != null) {
                removeKeys(config.enterpriseConfig);
            }
            this.mConfiguredNetworks.remove(netId);
            return true;
        }
        loge("removeConfigWithoutBroadcast no config found!!");
        return false;
    }

    WifiConfiguration getConfiguredNetwork(int networkId) {
        return this.mConfiguredNetworks.getForAllUsers(networkId);
    }

    void removeKeys(WifiEnterpriseConfig config) {
        String client = config.getClientCertificateAlias();
        if (!TextUtils.isEmpty(client)) {
            if (DBG) {
                Log.d(TAG, "removing client private key and user cert");
            }
            this.mKeyStore.delete("USRPKEY_" + client, 1010);
            this.mKeyStore.delete("USRCERT_" + client, 1010);
        }
        String ca = config.getCaCertificateAlias();
        if (!TextUtils.isEmpty(ca)) {
            if (DBG) {
                Log.d(TAG, "removing CA cert");
            }
            this.mKeyStore.delete("CACERT_" + ca, 1010);
        }
    }

    public void resetSimNetwork(WifiConfiguration config) {
        ArrayList<WifiConfiguration> configs = new ArrayList();
        configs.add(config);
        this.mWifiConfigStore.resetSimNetworks(configs, WifiConfigurationUtil.getIntSimSlot(config));
    }

    void addDisconnectNetwork(int netId) {
        synchronized (this.mDisconnectNetworks) {
            this.mDisconnectNetworks.add(Integer.valueOf(netId));
        }
    }

    void removeDisconnectNetwork(int netId) {
        synchronized (this.mDisconnectNetworks) {
            this.mDisconnectNetworks.remove(Integer.valueOf(netId));
        }
    }

    List<Integer> getDisconnectNetworks() {
        List<Integer> networks = new ArrayList();
        synchronized (this.mDisconnectNetworks) {
            for (Integer netId : this.mDisconnectNetworks) {
                networks.add(netId);
            }
        }
        return networks;
    }

    void setWifiFwkExt(IWifiFwkExt wifiExt) {
        this.mWifiFwkExt = wifiExt;
    }

    public void setIsNewNetwork(boolean isNew) {
        this.mNewNetwork = isNew;
    }

    void setWifiNetwork(WifiNetworkAvailable wa) {
        this.mWifiAvailable = wa;
    }

    void setWifiNetworkAvailable(WifiNetworkStateTraker wst) {
        this.mWifiStateTracker = wst;
    }

    void setWifiAutoSwitch(boolean autoSwitch) {
        this.mAutoJoinSwitch = autoSwitch;
    }

    public void setReasonCode(int reason) {
        this.mReasonCode = reason;
    }

    private ScanResult findHomonyAPFromScanResults(WifiConfiguration config) {
        if (config == null) {
            return null;
        }
        String cfgKey = config.configKey();
        if (cfgKey == null) {
            return null;
        }
        this.mScanResults = this.mWifiStateMachine.syncGetScanResultsList();
        if (this.mScanResults.size() <= 0) {
            return null;
        }
        ScanResult tmpResult = null;
        for (int i = 0; i < this.mScanResults.size(); i++) {
            ScanResult scanResult = (ScanResult) this.mScanResults.get(i);
            if (cfgKey.equals(WifiConfiguration.configKey(scanResult))) {
                if (tmpResult == null) {
                    if (DBG) {
                        log("conn-track, found a Homony AP scanResult = " + scanResult);
                    }
                    tmpResult = scanResult;
                } else {
                    if (DBG) {
                        log("conn-track, ESS found more Homony AP scanResult = " + scanResult);
                    }
                    if (scanResult.level > tmpResult.level) {
                        tmpResult = scanResult;
                    }
                }
            }
        }
        log("conn-track, tmpResult = " + tmpResult);
        return tmpResult;
    }
}
