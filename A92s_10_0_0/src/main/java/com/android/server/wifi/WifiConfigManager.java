package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.IpConfiguration;
import android.net.MacAddress;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.Looper;
import android.os.OppoManager;
import android.os.Parcelable;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.WifiConfigurationUtil;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import com.mediatek.server.wifi.MtkWapi;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

public class WifiConfigManager {
    private static final MacAddress DEFAULT_MAC_ADDRESS = MacAddress.fromString("02:00:00:00:00:00");
    @VisibleForTesting
    public static final long DELETED_EPHEMERAL_SSID_EXPIRY_MS = 86400000;
    private static final int DISABLED_ASSOCIATION_REJECT = 4;
    private static final int DISABLED_AUTH_FAILURE = 3;
    private static final int DISABLED_UNKNOWN_REASON = 0;
    private static final int DISABLED_WRONG_KEY = 6;
    private static final String DISABLE_ALERT = "android.net.wifi.DISABLE_ALERT_NETWORKS";
    private static final Boolean ENABLE_REMOVE_NETWORK_WITH_WRONGKEY = true;
    @VisibleForTesting
    public static final int LINK_CONFIGURATION_BSSID_MATCH_LENGTH = 16;
    @VisibleForTesting
    public static final int LINK_CONFIGURATION_MAX_SCAN_CACHE_ENTRIES = 6;
    @VisibleForTesting
    public static final long MAX_PNO_SCAN_FREQUENCY_AGE_MS = 2592000000L;
    @VisibleForTesting
    public static final int[] NETWORK_SELECTION_DISABLE_THRESHOLD = {-1, 1, 12, 5, 5, 5, 1, 1, 6, 1, 1, 1, 1, 4, 1};
    @VisibleForTesting
    public static final int[] NETWORK_SELECTION_DISABLE_TIMEOUT_MS = {ScoringParams.Values.MAX_EXPID, 900000, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, 600000, 0, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID, ScoringParams.Values.MAX_EXPID};
    @VisibleForTesting
    public static final String PASSWORD_MASK = "*";
    @VisibleForTesting
    public static final int SCAN_CACHE_ENTRIES_MAX_SIZE = 192;
    @VisibleForTesting
    public static final int SCAN_CACHE_ENTRIES_TRIM_SIZE = 128;
    private static final int SCAN_RESULT_MAXIMUM_AGE_MS = 40000;
    @VisibleForTesting
    public static final String SYSUI_PACKAGE_NAME = "com.android.systemui";
    private static final String TAG = "WifiConfigManager";
    private static final String WIFI_DIAGNOSIS_CONNECT_APK_NAME = "wifi_diagnosis_conpkgname";
    private static final String WIFI_DIAGNOSIS_CONNECT_FAIL_REASON = "wifi_diagnosis_failreason";
    private static final String WIFI_DIAGNOSIS_CONNECT_RESULT = "wifi_diagnosis_conresult";
    private static final String WIFI_DIAGNOSIS_STATISTIC_KEY = "060203";
    private static final int WIFI_PNO_FREQUENCY_CULLING_ENABLED_DEFAULT = 1;
    private static final int WIFI_PNO_RECENCY_SORTING_ENABLED_DEFAULT = 1;
    private static final String ctsPkg = "com.android.cts.verifier";
    private static final WifiConfigurationUtil.WifiConfigurationComparator sScanListComparator = new WifiConfigurationUtil.WifiConfigurationComparator() {
        /* class com.android.server.wifi.WifiConfigManager.AnonymousClass1 */

        @Override // com.android.server.wifi.WifiConfigurationUtil.WifiConfigurationComparator
        public int compareNetworksWithSameStatus(WifiConfiguration a, WifiConfiguration b) {
            return Long.compare(b.lastConnected, a.lastConnected);
        }
    };
    private boolean mAutoJoinSwitch;
    private final BackupManagerProxy mBackupManagerProxy;
    public ClientModeImpl mClientModeImpl;
    private final Clock mClock;
    private final ConfigurationMap mConfiguredNetworks;
    private final Context mContext;
    private int mCurrentUserId;
    private boolean mDeferredUserUnlockRead;
    private final DeletedEphemeralSsidsStoreData mDeletedEphemeralSsidsStoreData;
    private final Map<String, Long> mDeletedEphemeralSsidsToTimeMap;
    private final FrameworkFacade mFrameworkFacade;
    private boolean mLastConnectIsManual;
    private int mLastSelectedNetworkId;
    private long mLastSelectedTimeStamp;
    private List<OnSavedNetworkUpdateListener> mListeners;
    private final LocalLog mLocalLog;
    private final int mMaxNumActiveChannelsForPartialScans;
    private final NetworkListSharedStoreData mNetworkListSharedStoreData;
    private final NetworkListUserStoreData mNetworkListUserStoreData;
    OppoNetworkRecordHelper mNetworkRecordHelper;
    private boolean mNewNetwork;
    private int mNextNetworkId;
    private final boolean mOnlyLinkSameCredentialConfigurations;
    private OppoWifiConfigManagerUtil mOppoWifiConfigManagerUtil;
    private OppoWifiOCloudImpl mOppoWifiOCloudImpl;
    private boolean mPendingStoreRead;
    private boolean mPendingUnlockStoreRead;
    private boolean mPnoFrequencyCullingEnabled;
    private boolean mPnoRecencySortingEnabled;
    private final Map<String, String> mRandomizedMacAddressMapping;
    private final RandomizedMacStoreData mRandomizedMacStoreData;
    private final Map<Integer, ScanDetailCache> mScanDetailCaches;
    private List<Boolean> mSimPresent;
    private String mStartConPkgName;
    private final SubscriptionManager mSubscriptionManager;
    private int mSystemUiUid;
    private final TelephonyManager mTelephonyManager;
    private final UserManager mUserManager;
    private boolean mVerboseLoggingEnabled;
    private OppoWifiNetworkSwitchEnhance mWifiAvailable;
    private final WifiConfigStore mWifiConfigStore;
    private final WifiInjector mWifiInjector;
    private final WifiKeyStore mWifiKeyStore;
    private final WifiPermissionsUtil mWifiPermissionsUtil;
    private final WifiPermissionsWrapper mWifiPermissionsWrapper;
    private OppoWifiAssistantStateTraker mWifiStateTracker;

    public interface OnSavedNetworkUpdateListener {
        void onSavedNetworkAdded(int i);

        void onSavedNetworkEnabled(int i);

        void onSavedNetworkPermanentlyDisabled(int i, int i2);

        void onSavedNetworkRemoved(int i);

        void onSavedNetworkTemporarilyDisabled(int i, int i2);

        void onSavedNetworkUpdated(int i);
    }

    WifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, SubscriptionManager subscriptionManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, WifiInjector wifiInjector, NetworkListSharedStoreData networkListSharedStoreData, NetworkListUserStoreData networkListUserStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData, RandomizedMacStoreData randomizedMacStoreData, FrameworkFacade frameworkFacade, Looper looper) {
        this.mLocalLog = new LocalLog(ActivityManager.isLowRamDeviceStatic() ? 128 : 256);
        this.mVerboseLoggingEnabled = false;
        this.mCurrentUserId = 0;
        this.mPendingUnlockStoreRead = true;
        this.mPendingStoreRead = true;
        this.mDeferredUserUnlockRead = false;
        this.mNextNetworkId = 0;
        this.mSystemUiUid = -1;
        this.mLastSelectedNetworkId = -1;
        this.mLastSelectedTimeStamp = -1;
        this.mListeners = new ArrayList();
        this.mOppoWifiOCloudImpl = null;
        this.mPnoFrequencyCullingEnabled = false;
        this.mPnoRecencySortingEnabled = false;
        this.mNewNetwork = false;
        this.mLastConnectIsManual = false;
        this.mStartConPkgName = null;
        this.mContext = context;
        this.mClock = clock;
        this.mUserManager = userManager;
        this.mBackupManagerProxy = new BackupManagerProxy();
        this.mTelephonyManager = telephonyManager;
        this.mSubscriptionManager = subscriptionManager;
        this.mWifiKeyStore = wifiKeyStore;
        this.mWifiConfigStore = wifiConfigStore;
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
        this.mWifiPermissionsWrapper = wifiPermissionsWrapper;
        this.mWifiInjector = wifiInjector;
        this.mConfiguredNetworks = new ConfigurationMap(userManager);
        this.mScanDetailCaches = new HashMap(16, 0.75f);
        this.mDeletedEphemeralSsidsToTimeMap = new HashMap();
        this.mRandomizedMacAddressMapping = new HashMap();
        this.mNetworkListSharedStoreData = networkListSharedStoreData;
        this.mNetworkListUserStoreData = networkListUserStoreData;
        this.mDeletedEphemeralSsidsStoreData = deletedEphemeralSsidsStoreData;
        this.mRandomizedMacStoreData = randomizedMacStoreData;
        this.mWifiConfigStore.registerStoreData(this.mNetworkListSharedStoreData);
        this.mWifiConfigStore.registerStoreData(this.mNetworkListUserStoreData);
        this.mWifiConfigStore.registerStoreData(this.mDeletedEphemeralSsidsStoreData);
        this.mWifiConfigStore.registerStoreData(this.mRandomizedMacStoreData);
        this.mOnlyLinkSameCredentialConfigurations = this.mContext.getResources().getBoolean(17891589);
        this.mMaxNumActiveChannelsForPartialScans = this.mContext.getResources().getInteger(17694929);
        this.mFrameworkFacade = frameworkFacade;
        this.mFrameworkFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_pno_frequency_culling_enabled"), false, new ContentObserver(new Handler(looper)) {
            /* class com.android.server.wifi.WifiConfigManager.AnonymousClass2 */

            public void onChange(boolean selfChange) {
                WifiConfigManager.this.updatePnoFrequencyCullingSetting();
            }
        });
        updatePnoFrequencyCullingSetting();
        this.mFrameworkFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_pno_recency_sorting_enabled"), false, new ContentObserver(new Handler(looper)) {
            /* class com.android.server.wifi.WifiConfigManager.AnonymousClass3 */

            public void onChange(boolean selfChange) {
                WifiConfigManager.this.updatePnoRecencySortingSetting();
            }
        });
        updatePnoRecencySortingSetting();
        try {
            this.mSystemUiUid = this.mContext.getPackageManager().getPackageUidAsUser(SYSUI_PACKAGE_NAME, 1048576, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to resolve SystemUI's UID.");
        }
        this.mSimPresent = new ArrayList(1);
        this.mSimPresent.add(false);
        this.mOppoWifiOCloudImpl = new OppoWifiOCloudImpl(this.mContext, this);
        this.mNetworkRecordHelper = new OppoNetworkRecordHelper(this);
        this.mOppoWifiConfigManagerUtil = new OppoWifiConfigManagerUtil(context, this);
    }

    @VisibleForTesting
    public static String createDebugTimeStampString(long wallClockMillis) {
        StringBuilder sb = new StringBuilder();
        sb.append("time=");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(wallClockMillis);
        sb.append(String.format("%tm-%td %tH:%tM:%tS.%tL", c, c, c, c, c, c));
        return sb.toString();
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
        } else {
            this.mVerboseLoggingEnabled = false;
        }
        this.mWifiConfigStore.enableVerboseLogging(this.mVerboseLoggingEnabled);
        this.mWifiKeyStore.enableVerboseLogging(this.mVerboseLoggingEnabled);
        OppoNetworkRecordHelper oppoNetworkRecordHelper = this.mNetworkRecordHelper;
        OppoNetworkRecordHelper.enableVerboseLogging(verbose);
        this.mOppoWifiConfigManagerUtil.enableVerboseLogging(this.mVerboseLoggingEnabled);
    }

    /* access modifiers changed from: private */
    public void updatePnoFrequencyCullingSetting() {
        boolean z = true;
        if (this.mFrameworkFacade.getIntegerSetting(this.mContext, "wifi_pno_frequency_culling_enabled", 1) != 1) {
            z = false;
        }
        this.mPnoFrequencyCullingEnabled = z;
    }

    /* access modifiers changed from: private */
    public void updatePnoRecencySortingSetting() {
        boolean z = true;
        if (this.mFrameworkFacade.getIntegerSetting(this.mContext, "wifi_pno_recency_sorting_enabled", 1) != 1) {
            z = false;
        }
        this.mPnoRecencySortingEnabled = z;
    }

    private void maskPasswordsInWifiConfiguration(WifiConfiguration configuration) {
        if (!TextUtils.isEmpty(configuration.preSharedKey)) {
            configuration.preSharedKey = "*";
        }
        if (configuration.wepKeys != null) {
            for (int i = 0; i < configuration.wepKeys.length; i++) {
                if (!TextUtils.isEmpty(configuration.wepKeys[i])) {
                    configuration.wepKeys[i] = "*";
                }
            }
        }
        if (!TextUtils.isEmpty(configuration.enterpriseConfig.getPassword())) {
            configuration.enterpriseConfig.setPassword("*");
        }
    }

    private void maskRandomizedMacAddressInWifiConfiguration(WifiConfiguration configuration) {
        configuration.setRandomizedMacAddress(DEFAULT_MAC_ADDRESS);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getRandomizedMacAddressMappingSize() {
        return this.mRandomizedMacAddressMapping.size();
    }

    private MacAddress getPersistentMacAddress(WifiConfiguration config) {
        String persistentMacString = this.mRandomizedMacAddressMapping.get(config.getSsidAndSecurityTypeString());
        if (persistentMacString != null) {
            try {
                return MacAddress.fromString(persistentMacString);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error creating randomized MAC address from stored value.");
                this.mRandomizedMacAddressMapping.remove(config.getSsidAndSecurityTypeString());
            }
        }
        MacAddress result = WifiConfigurationUtil.calculatePersistentMacForConfiguration(config, WifiConfigurationUtil.obtainMacRandHashFunction(1010));
        if (result == null) {
            result = WifiConfigurationUtil.calculatePersistentMacForConfiguration(config, WifiConfigurationUtil.obtainMacRandHashFunction(1010));
        }
        if (result != null) {
            return result;
        }
        Log.wtf(TAG, "Failed to generate MAC address from KeyStore even after retrying. Using locally generated MAC address instead.");
        return MacAddress.createRandomUnicastAddress();
    }

    private MacAddress setRandomizedMacToPersistentMac(WifiConfiguration config) {
        MacAddress persistentMac = getPersistentMacAddress(config);
        if (persistentMac == null || persistentMac.equals(config.getRandomizedMacAddress())) {
            return persistentMac;
        }
        getInternalConfiguredNetwork(config.networkId).setRandomizedMacAddress(persistentMac);
        return persistentMac;
    }

    private WifiConfiguration createExternalWifiConfiguration(WifiConfiguration configuration, boolean maskPasswords, int targetUid) {
        WifiConfiguration network = new WifiConfiguration(configuration);
        if (maskPasswords) {
            maskPasswordsInWifiConfiguration(network);
        }
        if (!(targetUid == 1010 || targetUid == 1000 || targetUid == configuration.creatorUid)) {
            maskRandomizedMacAddressInWifiConfiguration(network);
        }
        return network;
    }

    private List<WifiConfiguration> getConfiguredNetworks(boolean savedOnly, boolean maskPasswords, int targetUid) {
        List<WifiConfiguration> networks = new ArrayList<>();
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (!savedOnly || (!config.ephemeral && !config.isPasspoint())) {
                networks.add(createExternalWifiConfiguration(config, maskPasswords, targetUid));
            }
        }
        return networks;
    }

    public List<WifiConfiguration> getConfiguredNetworks() {
        return getConfiguredNetworks(false, true, 1010);
    }

    public List<WifiConfiguration> getConfiguredNetworksWithPasswords() {
        return getConfiguredNetworks(false, false, 1010);
    }

    public List<WifiConfiguration> getSavedNetworks(int targetUid) {
        return getConfiguredNetworks(true, true, targetUid);
    }

    public WifiConfiguration getConfiguredNetwork(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        return createExternalWifiConfiguration(config, true, 1010);
    }

    public WifiConfiguration getConfiguredNetwork(String configKey) {
        WifiConfiguration config = getInternalConfiguredNetwork(configKey);
        if (config == null) {
            return null;
        }
        return createExternalWifiConfiguration(config, true, 1010);
    }

    public WifiConfiguration getConfiguredNetworkWithPassword(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        return createExternalWifiConfiguration(config, false, 1010);
    }

    public WifiConfiguration getConfiguredNetworkWithoutMasking(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        return new WifiConfiguration(config);
    }

    private Collection<WifiConfiguration> getInternalConfiguredNetworks() {
        return this.mConfiguredNetworks.valuesForCurrentUser();
    }

    private WifiConfiguration getInternalConfiguredNetwork(WifiConfiguration config) {
        WifiConfiguration internalConfig = this.mConfiguredNetworks.getForCurrentUser(config.networkId);
        if (internalConfig != null) {
            return internalConfig;
        }
        WifiConfiguration internalConfig2 = this.mConfiguredNetworks.getByConfigKeyForCurrentUser(config.configKey());
        if (internalConfig2 == null) {
            Log.e(TAG, "Cannot find network with networkId " + config.networkId + " or configKey " + config.configKey());
        }
        return internalConfig2;
    }

    private WifiConfiguration getInternalConfiguredNetwork(int networkId) {
        if (networkId == -1) {
            return null;
        }
        WifiConfiguration internalConfig = this.mConfiguredNetworks.getForCurrentUser(networkId);
        if (internalConfig == null) {
            Log.e(TAG, "Cannot find network with networkId " + networkId);
        }
        return internalConfig;
    }

    private WifiConfiguration getInternalConfiguredNetwork(String configKey) {
        WifiConfiguration internalConfig = this.mConfiguredNetworks.getByConfigKeyForCurrentUser(configKey);
        if (internalConfig == null) {
            Log.e(TAG, "Cannot find network with configKey " + configKey);
        }
        return internalConfig;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    private void sendConfiguredNetworkChangedBroadcast(WifiConfiguration network, int reason) {
        if (network == null) {
            Log.e(TAG, " network is null! do not send broadcast.");
            return;
        }
        Intent intent = new Intent("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("multipleChanges", false);
        WifiConfiguration broadcastNetwork = new WifiConfiguration(network);
        maskPasswordsInWifiConfiguration(broadcastNetwork);
        intent.putExtra("wifiConfiguration", broadcastNetwork);
        intent.putExtra("changeReason", reason);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
     arg types: [java.lang.String, int]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
    public void sendConfiguredNetworksChangedBroadcast() {
        Intent intent = new Intent("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("multipleChanges", true);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean canModifyNetwork(WifiConfiguration config, int uid) {
        if (uid == 1000 || this.mOppoWifiConfigManagerUtil.checkPermission(uid)) {
            return true;
        }
        if (config.isPasspoint() && uid == 1010) {
            return true;
        }
        if (config.enterpriseConfig != null && uid == 1010 && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())) {
            return true;
        }
        DevicePolicyManagerInternal dpmi = this.mWifiPermissionsWrapper.getDevicePolicyManagerInternal();
        if (dpmi != null && dpmi.isActiveAdminWithPolicy(uid, -2)) {
            return true;
        }
        boolean isCreator = config.creatorUid == uid;
        if (!this.mContext.getPackageManager().hasSystemFeature("android.software.device_admin") || dpmi != null) {
            if (dpmi != null && dpmi.isActiveAdminWithPolicy(config.creatorUid, -2)) {
                if ((Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_device_owner_configs_lockdown", 0) != 0) || !this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
                    return false;
                }
                return true;
            } else if (isCreator || this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
                return true;
            } else {
                return false;
            }
        } else {
            Log.w(TAG, "Error retrieving DPMI service.");
            return false;
        }
    }

    private boolean doesUidBelongToCurrentUser(int uid) {
        if (uid == 1000 || uid == this.mSystemUiUid) {
            return true;
        }
        return WifiConfigurationUtil.doesUidBelongToAnyProfile(uid, this.mUserManager.getProfiles(this.mCurrentUserId));
    }

    private void mergeWithInternalWifiConfiguration(WifiConfiguration internalConfig, WifiConfiguration externalConfig) {
        if (externalConfig.SSID != null) {
            internalConfig.SSID = externalConfig.SSID;
        }
        if (externalConfig.BSSID != null) {
            internalConfig.BSSID = externalConfig.BSSID.toLowerCase();
        }
        internalConfig.hiddenSSID = externalConfig.hiddenSSID;
        internalConfig.requirePMF = externalConfig.requirePMF;
        if (externalConfig.preSharedKey != null && !externalConfig.preSharedKey.equals("*")) {
            internalConfig.preSharedKey = externalConfig.preSharedKey;
        }
        if (externalConfig.wepKeys != null) {
            boolean hasWepKey = false;
            for (int i = 0; i < internalConfig.wepKeys.length; i++) {
                if (externalConfig.wepKeys[i] != null && !externalConfig.wepKeys[i].equals("*")) {
                    internalConfig.wepKeys[i] = externalConfig.wepKeys[i];
                    hasWepKey = true;
                }
            }
            if (hasWepKey) {
                internalConfig.wepTxKeyIndex = externalConfig.wepTxKeyIndex;
            }
        }
        if (externalConfig.FQDN != null) {
            internalConfig.FQDN = externalConfig.FQDN;
        }
        if (externalConfig.providerFriendlyName != null) {
            internalConfig.providerFriendlyName = externalConfig.providerFriendlyName;
        }
        if (externalConfig.roamingConsortiumIds != null) {
            internalConfig.roamingConsortiumIds = (long[]) externalConfig.roamingConsortiumIds.clone();
        }
        if (externalConfig.allowedAuthAlgorithms != null && !externalConfig.allowedAuthAlgorithms.isEmpty()) {
            internalConfig.allowedAuthAlgorithms = (BitSet) externalConfig.allowedAuthAlgorithms.clone();
        }
        if (externalConfig.allowedProtocols != null && !externalConfig.allowedProtocols.isEmpty()) {
            internalConfig.allowedProtocols = (BitSet) externalConfig.allowedProtocols.clone();
        }
        if (externalConfig.allowedKeyManagement != null && !externalConfig.allowedKeyManagement.isEmpty()) {
            internalConfig.allowedKeyManagement = (BitSet) externalConfig.allowedKeyManagement.clone();
            if (MtkWapi.isConfigForWapiNetwork(internalConfig)) {
                internalConfig.allowedProtocols.clear();
                internalConfig.allowedProtocols.set(3);
            }
        }
        if (externalConfig.allowedPairwiseCiphers != null && !externalConfig.allowedPairwiseCiphers.isEmpty()) {
            internalConfig.allowedPairwiseCiphers = (BitSet) externalConfig.allowedPairwiseCiphers.clone();
        }
        if (externalConfig.allowedGroupCiphers != null && !externalConfig.allowedGroupCiphers.isEmpty()) {
            internalConfig.allowedGroupCiphers = (BitSet) externalConfig.allowedGroupCiphers.clone();
        }
        if (externalConfig.allowedGroupManagementCiphers != null && !externalConfig.allowedGroupManagementCiphers.isEmpty()) {
            internalConfig.allowedGroupManagementCiphers = (BitSet) externalConfig.allowedGroupManagementCiphers.clone();
        }
        if (externalConfig.getIpConfiguration() != null) {
            IpConfiguration.IpAssignment ipAssignment = externalConfig.getIpAssignment();
            if (ipAssignment != IpConfiguration.IpAssignment.UNASSIGNED) {
                internalConfig.setIpAssignment(ipAssignment);
                if (ipAssignment == IpConfiguration.IpAssignment.STATIC) {
                    internalConfig.setStaticIpConfiguration(new StaticIpConfiguration(externalConfig.getStaticIpConfiguration()));
                }
            }
            IpConfiguration.ProxySettings proxySettings = externalConfig.getProxySettings();
            if (proxySettings != IpConfiguration.ProxySettings.UNASSIGNED) {
                internalConfig.setProxySettings(proxySettings);
                if (proxySettings == IpConfiguration.ProxySettings.PAC || proxySettings == IpConfiguration.ProxySettings.STATIC) {
                    internalConfig.setHttpProxy(new ProxyInfo(externalConfig.getHttpProxy()));
                }
            }
        }
        if (externalConfig.enterpriseConfig != null) {
            internalConfig.enterpriseConfig.copyFromExternal(externalConfig.enterpriseConfig, "*");
        }
        internalConfig.meteredHint = externalConfig.meteredHint;
        internalConfig.meteredOverride = externalConfig.meteredOverride;
        internalConfig.macRandomizationSetting = externalConfig.macRandomizationSetting;
        internalConfig.wapiCertSelMode = externalConfig.wapiCertSelMode;
        internalConfig.wapiCertSel = externalConfig.wapiCertSel;
        internalConfig.wapiPskType = externalConfig.wapiPskType;
        if (externalConfig.wapiPsk != null) {
            if (internalConfig.wapiPskType == 0) {
                internalConfig.wapiPsk = "\"" + externalConfig.wapiPsk + "\"";
                internalConfig.preSharedKey = "\"" + externalConfig.wapiPsk + "\"";
            } else {
                internalConfig.wapiPsk = externalConfig.wapiPsk;
                internalConfig.preSharedKey = externalConfig.wapiPsk;
            }
        }
        Log.d(TAG, "iG: " + internalConfig.globalId + " eG: " + externalConfig.globalId);
        if ("NULL".equals(internalConfig.globalId)) {
            internalConfig.globalId = externalConfig.globalId;
        } else if (!"NULL".equals(externalConfig.globalId)) {
            String delGlobalId = null;
            if (internalConfig.globalId.compareTo(externalConfig.globalId) > 0) {
                delGlobalId = internalConfig.globalId;
                internalConfig.globalId = externalConfig.globalId;
            } else if (!internalConfig.globalId.equals(externalConfig.globalId)) {
                delGlobalId = externalConfig.globalId;
            }
            if (delGlobalId != null) {
                this.mOppoWifiOCloudImpl.removeServerEndDuplicateNetwork(internalConfig.configKey(), internalConfig.preSharedKey, internalConfig.networkId, delGlobalId);
            }
        }
    }

    private void setDefaultsInWifiConfiguration(WifiConfiguration configuration) {
        configuration.allowedAuthAlgorithms.set(0);
        configuration.allowedProtocols.set(1);
        configuration.allowedProtocols.set(0);
        configuration.allowedKeyManagement.set(1);
        configuration.allowedKeyManagement.set(2);
        configuration.allowedPairwiseCiphers.set(2);
        configuration.allowedPairwiseCiphers.set(1);
        configuration.allowedGroupCiphers.set(3);
        configuration.allowedGroupCiphers.set(2);
        configuration.allowedGroupCiphers.set(0);
        configuration.allowedGroupCiphers.set(1);
        configuration.setIpAssignment(IpConfiguration.IpAssignment.DHCP);
        configuration.setProxySettings(IpConfiguration.ProxySettings.NONE);
        configuration.status = 1;
        configuration.getNetworkSelectionStatus().setNetworkSelectionStatus(2);
        configuration.getNetworkSelectionStatus().setNetworkSelectionDisableReason(11);
    }

    private WifiConfiguration createNewInternalWifiConfigurationFromExternal(WifiConfiguration externalConfig, int uid, String packageName) {
        WifiConfiguration newInternalConfig = new WifiConfiguration();
        int i = this.mNextNetworkId;
        this.mNextNetworkId = i + 1;
        newInternalConfig.networkId = i;
        setDefaultsInWifiConfiguration(newInternalConfig);
        mergeWithInternalWifiConfiguration(newInternalConfig, externalConfig);
        newInternalConfig.requirePMF = externalConfig.requirePMF;
        newInternalConfig.noInternetAccessExpected = externalConfig.noInternetAccessExpected;
        newInternalConfig.ephemeral = externalConfig.ephemeral;
        newInternalConfig.osu = externalConfig.osu;
        newInternalConfig.trusted = externalConfig.trusted;
        newInternalConfig.fromWifiNetworkSuggestion = externalConfig.fromWifiNetworkSuggestion;
        newInternalConfig.fromWifiNetworkSpecifier = externalConfig.fromWifiNetworkSpecifier;
        newInternalConfig.useExternalScores = externalConfig.useExternalScores;
        newInternalConfig.shared = externalConfig.shared;
        newInternalConfig.updateIdentifier = externalConfig.updateIdentifier;
        newInternalConfig.lastUpdateUid = uid;
        newInternalConfig.creatorUid = uid;
        String nameForUid = packageName != null ? packageName : this.mContext.getPackageManager().getNameForUid(uid);
        newInternalConfig.lastUpdateName = nameForUid;
        newInternalConfig.creatorName = nameForUid;
        String createDebugTimeStampString = createDebugTimeStampString(this.mClock.getWallClockMillis());
        newInternalConfig.updateTime = createDebugTimeStampString;
        newInternalConfig.creationTime = createDebugTimeStampString;
        MacAddress randomizedMac = getPersistentMacAddress(newInternalConfig);
        if (randomizedMac != null) {
            newInternalConfig.setRandomizedMacAddress(randomizedMac);
        }
        return newInternalConfig;
    }

    private WifiConfiguration updateExistingInternalWifiConfigurationFromExternal(WifiConfiguration internalConfig, WifiConfiguration externalConfig, int uid, String packageName) {
        WifiConfiguration newInternalConfig = new WifiConfiguration(internalConfig);
        mergeWithInternalWifiConfiguration(newInternalConfig, externalConfig);
        newInternalConfig.lastUpdateUid = uid;
        newInternalConfig.lastUpdateName = packageName != null ? packageName : this.mContext.getPackageManager().getNameForUid(uid);
        newInternalConfig.updateTime = createDebugTimeStampString(this.mClock.getWallClockMillis());
        return newInternalConfig;
    }

    private NetworkUpdateResult addOrUpdateNetworkInternal(WifiConfiguration config, int uid, String packageName) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Adding/Updating network " + config.getPrintableSsid());
        }
        WifiConfiguration newInternalConfig = null;
        WifiConfiguration existingInternalConfig = getInternalConfiguredNetwork(config);
        boolean hasCredentialChanged = true;
        if (existingInternalConfig == null) {
            if (!WifiConfigurationUtil.validate(config, true)) {
                Log.e(TAG, "Cannot add network with invalid config");
                return new NetworkUpdateResult(-1);
            }
            newInternalConfig = createNewInternalWifiConfigurationFromExternal(config, uid, packageName);
            existingInternalConfig = getInternalConfiguredNetwork(newInternalConfig.configKey());
        }
        if (existingInternalConfig != null) {
            if (!WifiConfigurationUtil.validate(config, false)) {
                Log.e(TAG, "Cannot update network with invalid config");
                return new NetworkUpdateResult(-1);
            } else if (!canModifyNetwork(existingInternalConfig, uid)) {
                Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + config.configKey());
                return new NetworkUpdateResult(-1);
            } else {
                newInternalConfig = updateExistingInternalWifiConfigurationFromExternal(existingInternalConfig, config, uid, packageName);
            }
        }
        if (WifiConfigurationUtil.hasProxyChanged(existingInternalConfig, newInternalConfig) && !canModifyProxySettings(uid)) {
            Log.e(TAG, "UID " + uid + " does not have permission to modify proxy Settings " + config.configKey() + ". Must have NETWORK_SETTINGS, or be device or profile owner.");
            return new NetworkUpdateResult(-1);
        } else if (WifiConfigurationUtil.hasMacRandomizationSettingsChanged(existingInternalConfig, newInternalConfig) && !this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid) && !this.mWifiPermissionsUtil.checkNetworkSetupWizardPermission(uid)) {
            Log.e(TAG, "UID " + uid + " does not have permission to modify MAC randomization Settings " + config.getSsidAndSecurityTypeString() + ". Must have NETWORK_SETTINGS or NETWORK_SETUP_WIZARD.");
            return new NetworkUpdateResult(-1);
        } else if (config.enterpriseConfig != null && config.enterpriseConfig.getEapMethod() != -1 && !config.isPasspoint() && !this.mWifiKeyStore.updateNetworkKeys(newInternalConfig, existingInternalConfig)) {
            return new NetworkUpdateResult(-1);
        } else {
            if (isAutoConnectSwitchEnable()) {
                if (config.userApproved == 1001) {
                    newInternalConfig.status = 1;
                    newInternalConfig.userApproved = 1001;
                } else if (config.userApproved == 1002) {
                    newInternalConfig.status = 2;
                    newInternalConfig.userApproved = 1002;
                }
            }
            boolean newNetwork = existingInternalConfig == null;
            this.mNewNetwork = newNetwork;
            boolean hasIpChanged = newNetwork || WifiConfigurationUtil.hasIpChanged(existingInternalConfig, newInternalConfig);
            boolean hasProxyChanged = newNetwork || WifiConfigurationUtil.hasProxyChanged(existingInternalConfig, newInternalConfig);
            if (!newNetwork && !WifiConfigurationUtil.hasCredentialChanged(existingInternalConfig, newInternalConfig)) {
                hasCredentialChanged = false;
            }
            if (hasCredentialChanged) {
                newInternalConfig.getNetworkSelectionStatus().setHasEverConnected(false);
            }
            try {
                this.mConfiguredNetworks.put(newInternalConfig);
                if (this.mDeletedEphemeralSsidsToTimeMap.remove(config.SSID) != null && this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Removed from ephemeral blacklist: " + config.SSID);
                }
                this.mBackupManagerProxy.notifyDataChanged();
                NetworkUpdateResult result = new NetworkUpdateResult(hasIpChanged, hasProxyChanged, hasCredentialChanged);
                result.setIsNewNetwork(newNetwork);
                result.setNetworkId(newInternalConfig.networkId);
                localLog("addOrUpdateNetworkInternal: added/updated config. netId=" + newInternalConfig.networkId + " configKey=" + newInternalConfig.configKey() + " uid=" + Integer.toString(newInternalConfig.creatorUid) + " name=" + newInternalConfig.creatorName);
                return result;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to add network to config map", e);
                return new NetworkUpdateResult(-1);
            }
        }
    }

    public NetworkUpdateResult addOrUpdateNetwork(WifiConfiguration config, int uid, String packageName) {
        int i;
        WifiConfiguration existingConfig;
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return new NetworkUpdateResult(-1);
        } else if (config == null) {
            Log.e(TAG, "Cannot add/update network with null config");
            return new NetworkUpdateResult(-1);
        } else if (this.mPendingStoreRead) {
            Log.e(TAG, "Cannot add/update network before store is read!");
            return new NetworkUpdateResult(-1);
        } else {
            if (!config.isEphemeral() && (existingConfig = getConfiguredNetwork(config.configKey())) != null && existingConfig.isEphemeral()) {
                removeNetwork(existingConfig.networkId, this.mSystemUiUid);
            }
            NetworkUpdateResult result = addOrUpdateNetworkInternal(config, uid, packageName);
            if (!result.isSuccess()) {
                Log.e(TAG, "Failed to add/update network " + config.getPrintableSsid());
                return result;
            }
            WifiConfiguration newConfig = getInternalConfiguredNetwork(result.getNetworkId());
            if (result.isNewNetwork()) {
                i = 0;
            } else {
                i = 2;
            }
            sendConfiguredNetworkChangedBroadcast(newConfig, i);
            if (!config.ephemeral && !config.isPasspoint()) {
                saveToStore(true);
                if (!(this.mListeners == null || newConfig == null)) {
                    if (result.isNewNetwork()) {
                        forSavedNetworkUpdateListeners("onSavedNetworkAdded", newConfig.networkId);
                    } else {
                        forSavedNetworkUpdateListeners("onSavedNetworkUpdated", newConfig.networkId);
                    }
                }
            }
            return result;
        }
    }

    public NetworkUpdateResult addOrUpdateNetwork(WifiConfiguration config, int uid) {
        return addOrUpdateNetwork(config, uid, null);
    }

    private boolean isExpRom() {
        return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
    }

    private boolean removeNetworkInternal(WifiConfiguration config, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Removing network " + config.getPrintableSsid());
        }
        if (!isExpRom() && !config.isPasspoint() && config.enterpriseConfig != null && config.enterpriseConfig.getEapMethod() != -1) {
            this.mWifiKeyStore.removeKeys(config.enterpriseConfig);
        }
        removeConnectChoiceFromAllNetworks(config.configKey());
        this.mConfiguredNetworks.remove(config.networkId);
        this.mScanDetailCaches.remove(Integer.valueOf(config.networkId));
        this.mBackupManagerProxy.notifyDataChanged();
        localLog("removeNetworkInternal: removed config. netId=" + config.networkId + " configKey=" + config.configKey() + " uid=" + Integer.toString(uid) + " name=" + this.mContext.getPackageManager().getNameForUid(uid));
        return true;
    }

    public boolean removeNetwork(int networkId, int uid) {
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return false;
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        if (!canModifyNetwork(config, uid)) {
            Log.e(TAG, "UID " + uid + " does not have permission to delete configuration " + config.configKey());
            return false;
        }
        ClientModeImpl clientModeImpl = this.mClientModeImpl;
        if (clientModeImpl != null) {
            clientModeImpl.resetVerbose();
        }
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().handleNetworkDeleted(networkId);
        }
        OppoWifiNetworkSwitchEnhance oppoWifiNetworkSwitchEnhance = this.mWifiAvailable;
        if (oppoWifiNetworkSwitchEnhance != null) {
            oppoWifiNetworkSwitchEnhance.removeNetworkAvailable(networkId);
        } else {
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiStateTracker;
            if (oppoWifiAssistantStateTraker != null) {
                oppoWifiAssistantStateTraker.rmConfUpdateRecord(networkId);
            }
        }
        sendNetworkDeletedEvt(networkId);
        if (!removeNetworkInternal(config, uid)) {
            Log.e(TAG, "Failed to remove network " + config.getPrintableSsid());
            return false;
        }
        if (networkId == this.mLastSelectedNetworkId) {
            clearLastSelectedNetwork();
        }
        sendConfiguredNetworkChangedBroadcast(config, 1);
        if (!config.ephemeral && !config.isPasspoint()) {
            saveToStore(true);
            if (this.mListeners != null) {
                forSavedNetworkUpdateListeners("onSavedNetworkRemoved", networkId);
            }
        }
        return true;
    }

    private String getCreatorPackageName(WifiConfiguration config) {
        String creatorName = config.creatorName;
        if (!creatorName.contains(":")) {
            return creatorName;
        }
        return creatorName.substring(0, creatorName.indexOf(":"));
    }

    public Set<Integer> removeNetworksForApp(ApplicationInfo app) {
        if (app == null || app.packageName == null) {
            return Collections.emptySet();
        }
        Log.d(TAG, "Remove all networks for app " + app);
        Set<Integer> removedNetworks = new ArraySet<>();
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        for (WifiConfiguration config : copiedConfigs) {
            if (app.uid == config.creatorUid && app.packageName.equals(getCreatorPackageName(config))) {
                localLog("Removing network " + config.SSID + ", application \"" + app.packageName + "\" uninstalled from user " + UserHandle.getUserId(app.uid));
                if (removeNetwork(config.networkId, this.mSystemUiUid)) {
                    removedNetworks.add(Integer.valueOf(config.networkId));
                }
            }
        }
        return removedNetworks;
    }

    /* access modifiers changed from: package-private */
    public Set<Integer> removeNetworksForUser(int userId) {
        Log.d(TAG, "Remove all networks for user " + userId);
        Set<Integer> removedNetworks = new ArraySet<>();
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        for (WifiConfiguration config : copiedConfigs) {
            if (userId == UserHandle.getUserId(config.creatorUid)) {
                localLog("Removing network " + config.SSID + ", user " + userId + " removed");
                if (removeNetwork(config.networkId, this.mSystemUiUid)) {
                    removedNetworks.add(Integer.valueOf(config.networkId));
                }
            }
        }
        return removedNetworks;
    }

    public boolean removeAllEphemeralOrPasspointConfiguredNetworks() {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Removing all passpoint or ephemeral configured networks");
        }
        boolean didRemove = false;
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        for (WifiConfiguration config : copiedConfigs) {
            if (config.isPasspoint()) {
                Log.d(TAG, "Removing passpoint network config " + config.configKey());
                removeNetwork(config.networkId, this.mSystemUiUid);
                didRemove = true;
            } else if (config.ephemeral) {
                Log.d(TAG, "Removing ephemeral network config " + config.configKey());
                removeNetwork(config.networkId, this.mSystemUiUid);
                didRemove = true;
            }
        }
        return didRemove;
    }

    public boolean removePasspointConfiguredNetwork(String fqdn) {
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        int length = copiedConfigs.length;
        int i = 0;
        while (i < length) {
            WifiConfiguration config = copiedConfigs[i];
            if (!config.isPasspoint() || !TextUtils.equals(fqdn, config.FQDN)) {
                i++;
            } else {
                Log.d(TAG, "Removing passpoint network config " + config.configKey());
                removeNetwork(config.networkId, this.mSystemUiUid);
                return true;
            }
        }
        return false;
    }

    private void setNetworkSelectionEnabled(WifiConfiguration config) {
        WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(0);
        status.setDisableTime(-1);
        status.setNetworkSelectionDisableReason(0);
        status.clearDisableReasonCounter();
        if (this.mListeners != null) {
            forSavedNetworkUpdateListeners("onSavedNetworkEnabled", config.networkId);
        }
    }

    private void setNetworkSelectionTemporarilyDisabled(WifiConfiguration config, int disableReason) {
        WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(1);
        status.setDisableTime(this.mClock.getElapsedSinceBootMillis());
        status.setNetworkSelectionDisableReason(disableReason);
        List<OnSavedNetworkUpdateListener> list = this.mListeners;
        if (list != null) {
            for (OnSavedNetworkUpdateListener listener : list) {
                listener.onSavedNetworkTemporarilyDisabled(config.networkId, disableReason);
            }
        }
    }

    private void setNetworkSelectionPermanentlyDisabled(WifiConfiguration config, int disableReason) {
        WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(2);
        status.setDisableTime(-1);
        status.setNetworkSelectionDisableReason(disableReason);
        List<OnSavedNetworkUpdateListener> list = this.mListeners;
        if (list != null) {
            for (OnSavedNetworkUpdateListener listener : list) {
                listener.onSavedNetworkPermanentlyDisabled(config.networkId, disableReason);
            }
        }
    }

    private void setNetworkStatus(WifiConfiguration config, int status) {
        if (!isAutoConnectSwitchEnable() || config.userApproved != 1001 || (status != 2 && status != 0)) {
            config.status = status;
            sendConfiguredNetworkChangedBroadcast(config, 2);
        }
    }

    private boolean setNetworkSelectionStatus(WifiConfiguration config, int reason) {
        WifiConfiguration.NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason < 0 || reason >= 15) {
            Log.e(TAG, "Invalid Network disable reason " + reason);
            return false;
        }
        if (reason == 0) {
            setNetworkSelectionEnabled(config);
            setNetworkStatus(config, 2);
        } else if (reason < 8) {
            setNetworkSelectionTemporarilyDisabled(config, reason);
            setNetworkStatus(config, 1);
        } else {
            setNetworkSelectionPermanentlyDisabled(config, reason);
            setNetworkStatus(config, 1);
        }
        localLog("setNetworkSelectionStatus: configKey=" + config.configKey() + " networkStatus=" + networkStatus.getNetworkStatusString() + " disableReason=" + networkStatus.getNetworkDisableReasonString() + " at=" + createDebugTimeStampString(this.mClock.getWallClockMillis()));
        saveToStore(false);
        return true;
    }

    private boolean updateNetworkSelectionStatus(WifiConfiguration config, int reason) {
        if (OppoManuConnectManager.getInstance() != null) {
            this.mLastConnectIsManual = OppoManuConnectManager.getInstance().isManuConnect();
        }
        if (!(reason == 0 || config.status == 1)) {
            sendUpdateNetworkDisabledCountEvt(config.networkId, reason);
        }
        if (reason < 0 || reason >= 15) {
            if (this.mVerboseLoggingEnabled) {
                localLog("Invalid Network disable reason:" + reason + "return false");
            }
            return false;
        }
        if (isThirdAppConnect() && (reason == 3 || reason == 2 || reason == 13 || reason == 102 || reason == 4)) {
            storethirdAPKWifiDiagnosis(false, reason);
        }
        WifiConfiguration.NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason != 0) {
            if ((reason == 2 || reason == 3 || reason == 4) && this.mWifiInjector.getWifiLastResortWatchdog().shouldIgnoreSsidUpdate()) {
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Ignore update network selection status since Watchdog trigger is activated");
                }
                return false;
            }
            networkStatus.incrementDisableReasonCounter(reason);
            int disableReasonCounter = networkStatus.getDisableReasonCounter(reason);
            int disableReasonThreshold = NETWORK_SELECTION_DISABLE_THRESHOLD[reason];
            if (disableReasonCounter < disableReasonThreshold) {
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Disable counter for network " + config.getPrintableSsid() + " for reason " + WifiConfiguration.NetworkSelectionStatus.getNetworkDisableReasonString(reason) + " is " + networkStatus.getDisableReasonCounter(reason) + " and threshold is " + disableReasonThreshold);
                }
                return true;
            }
            if (OppoManuConnectManager.getInstance() != null) {
                OppoManuConnectManager.getInstance().handleNetworkDisabled(config.networkId);
            }
            sendNetworkDisabledEvt(config.networkId, reason);
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiStateTracker;
            if (!(oppoWifiAssistantStateTraker == null || reason == 102 || reason == 5 || reason == 11)) {
                oppoWifiAssistantStateTraker.rmOrupdateRecordStatus(config.configKey(false), false);
            }
            if (reason == 2) {
                this.mOppoWifiConfigManagerUtil.handleReasonRejection(config, this.mClientModeImpl);
            } else if (reason == 4) {
                this.mOppoWifiConfigManagerUtil.handleReasonDhcpFail(config, reason, this.mClientModeImpl);
            }
        }
        return setNetworkSelectionStatus(config, reason);
    }

    public boolean updateNetworkSelectionStatus(int networkId, int reason) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        return updateNetworkSelectionStatus(config, reason);
    }

    public boolean updateNetworkNotRecommended(int networkId, boolean notRecommended) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.getNetworkSelectionStatus().setNotRecommended(notRecommended);
        if (this.mVerboseLoggingEnabled) {
            localLog("updateNetworkRecommendation: configKey=" + config.configKey() + " notRecommended=" + notRecommended);
        }
        saveToStore(false);
        return true;
    }

    private boolean tryEnableNetwork(WifiConfiguration config) {
        WifiConfiguration.NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (networkStatus.isNetworkTemporaryDisabled()) {
            if (this.mClock.getElapsedSinceBootMillis() - networkStatus.getDisableTime() >= ((long) NETWORK_SELECTION_DISABLE_TIMEOUT_MS[networkStatus.getNetworkSelectionDisableReason()])) {
                return updateNetworkSelectionStatus(config, 0);
            }
        } else if (networkStatus.isDisabledByReason(12)) {
            return updateNetworkSelectionStatus(config, 0);
        }
        return false;
    }

    public boolean tryEnableNetwork(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        return tryEnableNetwork(config);
    }

    public boolean enableNetwork(int networkId, boolean disableOthers, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Enabling network " + networkId + " (disableOthers " + disableOthers + ")");
        }
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return false;
        } else if (getInternalConfiguredNetwork(networkId) == null) {
            return false;
        } else {
            if (disableOthers) {
                setLastSelectedNetwork(networkId);
            }
            if (!updateNetworkSelectionStatus(networkId, 0)) {
                return false;
            }
            saveToStore(true);
            return true;
        }
    }

    public boolean disableNetwork(int networkId, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Disabling network " + networkId);
        }
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return false;
        } else if (getInternalConfiguredNetwork(networkId) == null) {
            return false;
        } else {
            if (networkId == this.mLastSelectedNetworkId) {
                clearLastSelectedNetwork();
            }
            if (!updateNetworkSelectionStatus(networkId, 11)) {
                return false;
            }
            saveToStore(true);
            return true;
        }
    }

    public boolean updateLastConnectUid(int networkId, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Update network last connect UID for " + networkId);
        }
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return false;
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.lastConnectUid = uid;
        return true;
    }

    public boolean updateNetworkAfterConnect(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Update network after connect for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.lastConnected = this.mClock.getWallClockMillis();
        config.numAssociation++;
        config.getNetworkSelectionStatus().clearDisableReasonCounter();
        config.getNetworkSelectionStatus().setHasEverConnected(true);
        setNetworkStatus(config, 0);
        int sizeForAllUsers = this.mConfiguredNetworks.sizeForAllUsers();
        OppoNetworkRecordHelper oppoNetworkRecordHelper = this.mNetworkRecordHelper;
        if (sizeForAllUsers > 100) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, " clear the obsolete networks from updateNetworkAfterConnect().");
            }
            this.mNetworkRecordHelper.clearObsoleteNetworks();
        }
        saveToStore(false);
        return true;
    }

    public boolean updateNetworkAfterDisconnect(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Update network after disconnect for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.lastDisconnected = this.mClock.getWallClockMillis();
        if (config.status == 0) {
            setNetworkStatus(config, 2);
        }
        saveToStore(false);
        return true;
    }

    public boolean setNetworkDefaultGwMacAddress(int networkId, String macAddress) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.defaultGwMacAddress = macAddress;
        return true;
    }

    public boolean setNetworkRandomizedMacAddress(int networkId, MacAddress macAddress) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.setRandomizedMacAddress(macAddress);
        return true;
    }

    public boolean clearNetworkCandidateScanResult(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Clear network candidate scan result for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.getNetworkSelectionStatus().setCandidate((ScanResult) null);
        config.getNetworkSelectionStatus().setCandidateScore(Integer.MIN_VALUE);
        config.getNetworkSelectionStatus().setSeenInLastQualifiedNetworkSelection(false);
        return true;
    }

    public boolean setNetworkCandidateScanResult(int networkId, ScanResult scanResult, int score) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Set network candidate scan result " + scanResult + " for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            Log.e(TAG, "Cannot find network for " + networkId);
            return false;
        }
        config.getNetworkSelectionStatus().setCandidate(scanResult);
        config.getNetworkSelectionStatus().setCandidateScore(score);
        config.getNetworkSelectionStatus().setSeenInLastQualifiedNetworkSelection(true);
        return true;
    }

    private void removeConnectChoiceFromAllNetworks(String connectChoiceConfigKey) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Removing connect choice from all networks " + connectChoiceConfigKey);
        }
        if (connectChoiceConfigKey != null) {
            for (WifiConfiguration config : this.mConfiguredNetworks.valuesForCurrentUser()) {
                String connectChoice = config.getNetworkSelectionStatus().getConnectChoice();
                if (TextUtils.equals(connectChoice, connectChoiceConfigKey)) {
                    Log.d(TAG, "remove connect choice:" + connectChoice + " from " + config.SSID + " : " + config.networkId);
                    clearNetworkConnectChoice(config.networkId);
                }
            }
        }
    }

    public boolean clearNetworkConnectChoice(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Clear network connect choice for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.getNetworkSelectionStatus().setConnectChoice((String) null);
        config.getNetworkSelectionStatus().setConnectChoiceTimestamp(-1);
        saveToStore(false);
        return true;
    }

    public boolean setNetworkConnectChoice(int networkId, String connectChoiceConfigKey, long timestamp) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Set network connect choice " + connectChoiceConfigKey + " for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.getNetworkSelectionStatus().setConnectChoice(connectChoiceConfigKey);
        config.getNetworkSelectionStatus().setConnectChoiceTimestamp(timestamp);
        saveToStore(false);
        return true;
    }

    public boolean incrementNetworkNoInternetAccessReports(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.numNoInternetAccessReports++;
        return true;
    }

    public boolean setNetworkValidatedInternetAccess(int networkId, boolean validated) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.validatedInternetAccess = validated;
        config.numNoInternetAccessReports = 0;
        saveToStore(false);
        return true;
    }

    public boolean setNetworkNoInternetAccessExpected(int networkId, boolean expected) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.noInternetAccessExpected = expected;
        return true;
    }

    private void clearLastSelectedNetwork() {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Clearing last selected network");
        }
        this.mLastSelectedNetworkId = -1;
        this.mLastSelectedTimeStamp = -1;
    }

    private void setLastSelectedNetwork(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Setting last selected network to " + networkId);
        }
        this.mLastSelectedNetworkId = networkId;
        this.mLastSelectedTimeStamp = this.mClock.getElapsedSinceBootMillis();
    }

    public int getLastSelectedNetwork() {
        return this.mLastSelectedNetworkId;
    }

    public String getLastSelectedNetworkConfigKey() {
        WifiConfiguration config;
        int i = this.mLastSelectedNetworkId;
        if (i == -1 || (config = getInternalConfiguredNetwork(i)) == null) {
            return "";
        }
        return config.configKey();
    }

    public long getLastSelectedTimeStamp() {
        return this.mLastSelectedTimeStamp;
    }

    public ScanDetailCache getScanDetailCacheForNetwork(int networkId) {
        return this.mScanDetailCaches.get(Integer.valueOf(networkId));
    }

    private ScanDetailCache getOrCreateScanDetailCacheForNetwork(WifiConfiguration config) {
        if (config == null) {
            return null;
        }
        ScanDetailCache cache = getScanDetailCacheForNetwork(config.networkId);
        if (cache != null || config.networkId == -1) {
            return cache;
        }
        ScanDetailCache cache2 = new ScanDetailCache(config, SCAN_CACHE_ENTRIES_MAX_SIZE, 128);
        this.mScanDetailCaches.put(Integer.valueOf(config.networkId), cache2);
        return cache2;
    }

    private void saveToScanDetailCacheForNetwork(WifiConfiguration config, ScanDetail scanDetail) {
        ScanResult scanResult = scanDetail.getScanResult();
        ScanDetailCache scanDetailCache = getOrCreateScanDetailCacheForNetwork(config);
        if (scanDetailCache == null) {
            Log.e(TAG, "Could not allocate scan cache for " + config.getPrintableSsid());
            return;
        }
        if (config.ephemeral) {
            scanResult.untrusted = true;
        }
        scanDetailCache.put(scanDetail);
        attemptNetworkLinking(config);
    }

    public WifiConfiguration getConfiguredNetworkForScanDetail(ScanDetail scanDetail) {
        ScanResult scanResult = scanDetail.getScanResult();
        if (scanResult == null) {
            Log.e(TAG, "No scan result found in scan detail");
            return null;
        }
        WifiConfiguration config = null;
        try {
            config = this.mConfiguredNetworks.getByScanResultForCurrentUser(scanResult);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to lookup network from config map", e);
        }
        if (config != null && this.mVerboseLoggingEnabled) {
            Log.v(TAG, "getSavedNetworkFromScanDetail Found " + config.configKey() + " for " + scanResult.SSID + "[" + scanResult.capabilities + "]");
        }
        return config;
    }

    public WifiConfiguration getConfiguredNetworkForScanDetailAndCache(ScanDetail scanDetail) {
        WifiConfiguration network = getConfiguredNetworkForScanDetail(scanDetail);
        if (network == null) {
            return null;
        }
        saveToScanDetailCacheForNetwork(network, scanDetail);
        if (scanDetail.getNetworkDetail() != null && scanDetail.getNetworkDetail().getDtimInterval() > 0) {
            network.dtimInterval = scanDetail.getNetworkDetail().getDtimInterval();
        }
        return createExternalWifiConfiguration(network, true, 1010);
    }

    public void updateScanDetailCacheFromWifiInfo(WifiInfo info) {
        WifiConfiguration config = getInternalConfiguredNetwork(info.getNetworkId());
        ScanDetailCache scanDetailCache = getScanDetailCacheForNetwork(info.getNetworkId());
        if (config != null && scanDetailCache != null) {
            ScanDetail scanDetail = scanDetailCache.getScanDetail(info.getBSSID());
            if (scanDetail != null) {
                ScanResult result = scanDetail.getScanResult();
                long previousSeen = result.seen;
                int previousRssi = result.level;
                scanDetail.setSeen();
                result.level = info.getRssi();
                long age = result.seen - previousSeen;
                if (previousSeen > 0 && age > 0 && age < 40000 / 2) {
                    double alpha = 0.5d - (((double) age) / ((double) 40000));
                    result.level = (int) ((((double) result.level) * (1.0d - alpha)) + (((double) previousRssi) * alpha));
                }
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Updating scan detail cache freq=" + result.frequency + " BSSID=" + result.BSSID + " RSSI=" + result.level + " for " + config.configKey());
                }
            }
        }
    }

    public void updateScanDetailForNetwork(int networkId, ScanDetail scanDetail) {
        WifiConfiguration network = getInternalConfiguredNetwork(networkId);
        if (network != null) {
            saveToScanDetailCacheForNetwork(network, scanDetail);
        }
    }

    private boolean shouldNetworksBeLinked(WifiConfiguration network1, WifiConfiguration network2, ScanDetailCache scanDetailCache1, ScanDetailCache scanDetailCache2) {
        if (this.mOnlyLinkSameCredentialConfigurations && !TextUtils.equals(network1.preSharedKey, network2.preSharedKey)) {
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "shouldNetworksBeLinked unlink due to password mismatch");
            }
            return false;
        } else if (network1.defaultGwMacAddress == null || network2.defaultGwMacAddress == null) {
            if (scanDetailCache1 == null || scanDetailCache2 == null) {
                return false;
            }
            for (String abssid : scanDetailCache1.keySet()) {
                for (String bbssid : scanDetailCache2.keySet()) {
                    if (abssid.regionMatches(true, 0, bbssid, 0, 16)) {
                        if (this.mVerboseLoggingEnabled) {
                            Log.v(TAG, "shouldNetworksBeLinked link due to DBDC BSSID match " + network2.SSID + " and " + network1.SSID + " bssida " + abssid + " bssidb " + bbssid);
                        }
                        return true;
                    }
                }
            }
            return false;
        } else if (!network1.defaultGwMacAddress.equals(network2.defaultGwMacAddress)) {
            return false;
        } else {
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "shouldNetworksBeLinked link due to same gw " + network2.SSID + " and " + network1.SSID + " GW " + network1.defaultGwMacAddress);
            }
            return true;
        }
    }

    private void linkNetworks(WifiConfiguration network1, WifiConfiguration network2) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "linkNetworks will link " + network2.configKey() + " and " + network1.configKey());
        }
        if (network2.linkedConfigurations == null) {
            network2.linkedConfigurations = new HashMap();
        }
        if (network1.linkedConfigurations == null) {
            network1.linkedConfigurations = new HashMap();
        }
        network2.linkedConfigurations.put(network1.configKey(), 1);
        network1.linkedConfigurations.put(network2.configKey(), 1);
    }

    private void unlinkNetworks(WifiConfiguration network1, WifiConfiguration network2) {
        if (!(network2.linkedConfigurations == null || network2.linkedConfigurations.get(network1.configKey()) == null)) {
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "unlinkNetworks un-link " + network1.configKey() + " from " + network2.configKey());
            }
            network2.linkedConfigurations.remove(network1.configKey());
        }
        if (network1.linkedConfigurations != null && network1.linkedConfigurations.get(network2.configKey()) != null) {
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "unlinkNetworks un-link " + network2.configKey() + " from " + network1.configKey());
            }
            network1.linkedConfigurations.remove(network2.configKey());
        }
    }

    private void attemptNetworkLinking(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(1)) {
            ScanDetailCache scanDetailCache = getScanDetailCacheForNetwork(config.networkId);
            if (scanDetailCache == null || scanDetailCache.size() <= 6) {
                for (WifiConfiguration linkConfig : getInternalConfiguredNetworks()) {
                    if (!linkConfig.configKey().equals(config.configKey()) && !linkConfig.ephemeral && linkConfig.allowedKeyManagement.get(1)) {
                        ScanDetailCache linkScanDetailCache = getScanDetailCacheForNetwork(linkConfig.networkId);
                        if (linkScanDetailCache == null || linkScanDetailCache.size() <= 6) {
                            if (shouldNetworksBeLinked(config, linkConfig, scanDetailCache, linkScanDetailCache)) {
                                linkNetworks(config, linkConfig);
                            } else {
                                unlinkNetworks(config, linkConfig);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean addToChannelSetForNetworkFromScanDetailCache(Set<Integer> channelSet, ScanDetailCache scanDetailCache, long nowInMillis, long ageInMillis, int maxChannelSetSize) {
        if (scanDetailCache != null && scanDetailCache.size() > 0) {
            for (ScanDetail scanDetail : scanDetailCache.values()) {
                ScanResult result = scanDetail.getScanResult();
                boolean valid = nowInMillis - result.seen < ageInMillis;
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "fetchChannelSetForNetwork has " + result.BSSID + " freq " + result.frequency + " age " + (nowInMillis - result.seen) + " ?=" + valid);
                }
                if (valid) {
                    channelSet.add(Integer.valueOf(result.frequency));
                }
                if (channelSet.size() >= maxChannelSetSize) {
                    return false;
                }
            }
        }
        return true;
    }

    public Set<Integer> fetchChannelSetForNetworkForPartialScan(int networkId, long ageInMillis, int homeChannelFreq) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        ScanDetailCache scanDetailCache = getScanDetailCacheForNetwork(networkId);
        if (scanDetailCache == null && config.linkedConfigurations == null) {
            Log.i(TAG, "No scan detail and linked configs associated with networkId " + networkId);
            return null;
        }
        if (this.mVerboseLoggingEnabled) {
            StringBuilder dbg = new StringBuilder();
            dbg.append("fetchChannelSetForNetworkForPartialScan ageInMillis ");
            dbg.append(ageInMillis);
            dbg.append(" for ");
            dbg.append(config.configKey());
            dbg.append(" max ");
            dbg.append(this.mMaxNumActiveChannelsForPartialScans);
            if (scanDetailCache != null) {
                dbg.append(" bssids " + scanDetailCache.size());
            }
            if (config.linkedConfigurations != null) {
                dbg.append(" linked " + config.linkedConfigurations.size());
            }
            Log.v(TAG, dbg.toString());
        }
        Set<Integer> channelSet = new HashSet<>();
        if (homeChannelFreq > 0) {
            channelSet.add(Integer.valueOf(homeChannelFreq));
            if (channelSet.size() >= this.mMaxNumActiveChannelsForPartialScans) {
                return channelSet;
            }
        }
        long nowInMillis = this.mClock.getWallClockMillis();
        if (addToChannelSetForNetworkFromScanDetailCache(channelSet, scanDetailCache, nowInMillis, ageInMillis, this.mMaxNumActiveChannelsForPartialScans) && config.linkedConfigurations != null) {
            Iterator it = config.linkedConfigurations.keySet().iterator();
            while (it.hasNext() && ((linkedConfig = getInternalConfiguredNetwork((String) it.next())) == null || addToChannelSetForNetworkFromScanDetailCache(channelSet, getScanDetailCacheForNetwork(linkedConfig.networkId), nowInMillis, ageInMillis, this.mMaxNumActiveChannelsForPartialScans))) {
            }
        }
        return channelSet;
    }

    private Set<Integer> fetchChannelSetForNetworkForPnoScan(int networkId, long ageInMillis) {
        ScanDetailCache scanDetailCache;
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null || (scanDetailCache = getScanDetailCacheForNetwork(networkId)) == null) {
            return null;
        }
        if (this.mVerboseLoggingEnabled) {
            StringBuilder sb = new StringBuilder("fetchChannelSetForNetworkForPnoScan ageInMillis ");
            sb.append(ageInMillis);
            sb.append(" for ");
            sb.append(config.configKey());
            sb.append(" bssids " + scanDetailCache.size());
            Log.v(TAG, sb.toString());
        }
        Set<Integer> channelSet = new HashSet<>();
        addToChannelSetForNetworkFromScanDetailCache(channelSet, scanDetailCache, this.mClock.getWallClockMillis(), ageInMillis, ScoringParams.Values.MAX_EXPID);
        return channelSet;
    }

    public List<WifiScanner.PnoSettings.PnoNetwork> retrievePnoNetworkList() {
        List<WifiScanner.PnoSettings.PnoNetwork> pnoList = new ArrayList<>();
        List<WifiConfiguration> networks = new ArrayList<>(getInternalConfiguredNetworks());
        Iterator<WifiConfiguration> iter = networks.iterator();
        while (iter.hasNext()) {
            WifiConfiguration config = iter.next();
            if (config.ephemeral || config.isPasspoint() || ((isAutoConnectSwitchEnable() && config.userApproved == 1001) || config.getNetworkSelectionStatus().isNetworkPermanentlyDisabled() || config.getNetworkSelectionStatus().isNetworkTemporaryDisabled())) {
                iter.remove();
            }
        }
        if (networks.isEmpty()) {
            return pnoList;
        }
        Collections.sort(networks, sScanListComparator);
        if (this.mPnoRecencySortingEnabled) {
            WifiConfiguration lastConnectedNetwork = networks.stream().max(Comparator.comparing($$Lambda$WifiConfigManager$IQAd8DT29bH7BRNkSq57y94BdXA.INSTANCE)).get();
            if (lastConnectedNetwork.lastConnected != 0) {
                networks.remove(networks.indexOf(lastConnectedNetwork));
                networks.add(0, lastConnectedNetwork);
            }
        }
        for (WifiConfiguration config2 : networks) {
            WifiScanner.PnoSettings.PnoNetwork pnoNetwork = WifiConfigurationUtil.createPnoNetwork(config2);
            pnoList.add(pnoNetwork);
            if (this.mPnoFrequencyCullingEnabled) {
                Set<Integer> channelSet = fetchChannelSetForNetworkForPnoScan(config2.networkId, MAX_PNO_SCAN_FREQUENCY_AGE_MS);
                if (channelSet != null) {
                    pnoNetwork.frequencies = channelSet.stream().mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
                }
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "retrievePnoNetworkList " + pnoNetwork.ssid + ":" + Arrays.toString(pnoNetwork.frequencies));
                }
            }
        }
        return pnoList;
    }

    public List<WifiScanner.ScanSettings.HiddenNetwork> retrieveHiddenNetworkList() {
        List<WifiScanner.ScanSettings.HiddenNetwork> hiddenList = new ArrayList<>();
        List<WifiConfiguration> networks = new ArrayList<>(getInternalConfiguredNetworks());
        Iterator<WifiConfiguration> iter = networks.iterator();
        while (iter.hasNext()) {
            if (!iter.next().hiddenSSID) {
                iter.remove();
            }
        }
        Collections.sort(networks, sScanListComparator);
        this.mOppoWifiConfigManagerUtil.limitHiddenNetworkList(hiddenList, networks);
        return hiddenList;
    }

    public boolean wasEphemeralNetworkDeleted(String ssid) {
        if (!this.mDeletedEphemeralSsidsToTimeMap.containsKey(ssid)) {
            return false;
        }
        if (this.mClock.getWallClockMillis() - this.mDeletedEphemeralSsidsToTimeMap.get(ssid).longValue() <= 86400000) {
            return true;
        }
        this.mDeletedEphemeralSsidsToTimeMap.remove(ssid);
        return false;
    }

    public WifiConfiguration disableEphemeralNetwork(String ssid) {
        if (ssid == null) {
            return null;
        }
        WifiConfiguration foundConfig = null;
        Iterator<WifiConfiguration> it = getInternalConfiguredNetworks().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            WifiConfiguration config = it.next();
            if ((config.ephemeral || config.isPasspoint()) && TextUtils.equals(config.SSID, ssid)) {
                foundConfig = config;
                break;
            }
        }
        if (foundConfig == null) {
            return null;
        }
        this.mDeletedEphemeralSsidsToTimeMap.put(ssid, Long.valueOf(this.mClock.getWallClockMillis()));
        Log.d(TAG, "Forget ephemeral SSID " + ssid + " num=" + this.mDeletedEphemeralSsidsToTimeMap.size());
        if (foundConfig.ephemeral) {
            Log.d(TAG, "Found ephemeral config in disableEphemeralNetwork: " + foundConfig.networkId);
        } else if (foundConfig.isPasspoint()) {
            Log.d(TAG, "Found Passpoint config in disableEphemeralNetwork: " + foundConfig.networkId + ", FQDN: " + foundConfig.FQDN);
        }
        removeConnectChoiceFromAllNetworks(foundConfig.configKey());
        return foundConfig;
    }

    @VisibleForTesting
    public void clearDeletedEphemeralNetworks() {
        this.mDeletedEphemeralSsidsToTimeMap.clear();
    }

    public void resetSimNetworks(boolean simPresent, int simSlot) {
        if (this.mVerboseLoggingEnabled) {
            localLog("resetSimNetworks");
        }
        for (int i = this.mSimPresent.size(); i < this.mTelephonyManager.getPhoneCount(); i++) {
            this.mSimPresent.add(false);
            Log.d(TAG, "mSimPresent size has changed to " + this.mSimPresent.size());
        }
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (TelephonyUtil.isSimConfig(config) && TelephonyUtil.getSimSlot(config) == simSlot) {
                if (config.enterpriseConfig.getEapMethod() == 0) {
                    Pair<String, String> currentIdentity = TelephonyUtil.getSimIdentity(this.mTelephonyManager, this.mSubscriptionManager, new TelephonyUtil(), config, this.mWifiInjector.getCarrierNetworkConfig());
                    if (this.mVerboseLoggingEnabled) {
                        Log.d(TAG, "New identity for config " + config + ": " + currentIdentity);
                    }
                    if (currentIdentity == null) {
                        Log.d(TAG, "Identity is null");
                    } else {
                        config.enterpriseConfig.setIdentity((String) currentIdentity.first);
                    }
                } else {
                    config.enterpriseConfig.setIdentity("");
                    if (!TelephonyUtil.isAnonymousAtRealmIdentity(config.enterpriseConfig.getAnonymousIdentity())) {
                        config.enterpriseConfig.setAnonymousIdentity("");
                    }
                }
            }
        }
        this.mSimPresent.set(simSlot, Boolean.valueOf(simPresent));
    }

    public boolean isSimPresent(int simSlot) {
        if (simSlot >= this.mSimPresent.size()) {
            return false;
        }
        return this.mSimPresent.get(simSlot).booleanValue();
    }

    public boolean isSimPresent() {
        for (int i = 0; i < this.mSimPresent.size(); i++) {
            if (this.mSimPresent.get(i).booleanValue()) {
                return true;
            }
        }
        return false;
    }

    private void handleUserUnlockOrSwitch(int userId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Loading from store after user switch/unlock for " + userId);
        }
        if (loadFromUserStoreAfterUnlockOrSwitch(userId)) {
            saveToStore(true);
            this.mPendingUnlockStoreRead = false;
        }
    }

    public Set<Integer> handleUserSwitch(int userId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Handling user switch for " + userId);
        }
        int i = this.mCurrentUserId;
        if (userId == i) {
            Log.w(TAG, "User already in foreground " + userId);
            return new HashSet();
        } else if (this.mPendingStoreRead) {
            Log.w(TAG, "User switch before store is read!");
            this.mConfiguredNetworks.setNewUser(userId);
            this.mCurrentUserId = userId;
            this.mDeferredUserUnlockRead = false;
            this.mPendingUnlockStoreRead = true;
            return new HashSet();
        } else {
            if (this.mUserManager.isUserUnlockingOrUnlocked(i)) {
                saveToStore(true);
            }
            Set<Integer> removedNetworkIds = clearInternalUserData(this.mCurrentUserId);
            this.mConfiguredNetworks.setNewUser(userId);
            this.mCurrentUserId = userId;
            if (this.mUserManager.isUserUnlockingOrUnlocked(this.mCurrentUserId)) {
                handleUserUnlockOrSwitch(this.mCurrentUserId);
            } else {
                this.mPendingUnlockStoreRead = true;
                Log.i(TAG, "Waiting for user unlock to load from store");
            }
            return removedNetworkIds;
        }
    }

    public void handleUserUnlock(int userId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Handling user unlock for " + userId);
        }
        int i = this.mCurrentUserId;
        if (userId != i) {
            Log.e(TAG, "Ignore user unlock for non current user " + userId);
        } else if (this.mPendingStoreRead) {
            Log.w(TAG, "Ignore user unlock until store is read!");
            this.mDeferredUserUnlockRead = true;
        } else if (this.mPendingUnlockStoreRead) {
            handleUserUnlockOrSwitch(i);
        }
    }

    public void handleUserStop(int userId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Handling user stop for " + userId);
        }
        int i = this.mCurrentUserId;
        if (userId == i && this.mUserManager.isUserUnlockingOrUnlocked(i)) {
            saveToStore(true);
            clearInternalUserData(this.mCurrentUserId);
        }
    }

    private void clearInternalData() {
        localLog("clearInternalData: Clearing all internal data");
        this.mConfiguredNetworks.clear();
        this.mDeletedEphemeralSsidsToTimeMap.clear();
        this.mRandomizedMacAddressMapping.clear();
        this.mScanDetailCaches.clear();
        clearLastSelectedNetwork();
    }

    private Set<Integer> clearInternalUserData(int userId) {
        localLog("clearInternalUserData: Clearing user internal data for " + userId);
        Set<Integer> removedNetworkIds = new HashSet<>();
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (!config.shared && WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(userId))) {
                removedNetworkIds.add(Integer.valueOf(config.networkId));
                localLog("clearInternalUserData: removed config. netId=" + config.networkId + " configKey=" + config.configKey());
                this.mConfiguredNetworks.remove(config.networkId);
            }
        }
        this.mDeletedEphemeralSsidsToTimeMap.clear();
        this.mScanDetailCaches.clear();
        clearLastSelectedNetwork();
        return removedNetworkIds;
    }

    private void loadInternalDataFromSharedStore(List<WifiConfiguration> configurations, Map<String, String> macAddressMapping) {
        for (WifiConfiguration configuration : configurations) {
            int i = this.mNextNetworkId;
            this.mNextNetworkId = i + 1;
            configuration.networkId = i;
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "Adding network from shared store " + configuration.configKey());
            }
            try {
                this.mConfiguredNetworks.put(configuration);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to add network to config map", e);
            }
        }
        this.mRandomizedMacAddressMapping.putAll(macAddressMapping);
    }

    private void loadInternalDataFromUserStore(List<WifiConfiguration> configurations, Map<String, Long> deletedEphemeralSsidsToTimeMap) {
        for (WifiConfiguration configuration : configurations) {
            int i = this.mNextNetworkId;
            this.mNextNetworkId = i + 1;
            configuration.networkId = i;
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "Adding network from user store " + configuration.configKey());
            }
            try {
                this.mConfiguredNetworks.put(configuration);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to add network to config map", e);
            }
        }
        this.mDeletedEphemeralSsidsToTimeMap.putAll(deletedEphemeralSsidsToTimeMap);
    }

    private void generateRandomizedMacAddresses() {
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (DEFAULT_MAC_ADDRESS.equals(config.getRandomizedMacAddress())) {
                setRandomizedMacToPersistentMac(config);
            }
        }
    }

    private void loadInternalData(List<WifiConfiguration> sharedConfigurations, List<WifiConfiguration> userConfigurations, Map<String, Long> deletedEphemeralSsidsToTimeMap, Map<String, String> macAddressMapping) {
        clearInternalData();
        loadInternalDataFromSharedStore(sharedConfigurations, macAddressMapping);
        loadInternalDataFromUserStore(userConfigurations, deletedEphemeralSsidsToTimeMap);
        generateRandomizedMacAddresses();
        if (this.mConfiguredNetworks.sizeForAllUsers() == 0) {
            Log.w(TAG, "No stored networks found.");
        }
        for (int i = 0; i < this.mSimPresent.size(); i++) {
            resetSimNetworks(this.mSimPresent.get(i).booleanValue(), i);
        }
        sendConfiguredNetworksChangedBroadcast();
        this.mPendingStoreRead = false;
    }

    public boolean loadFromStore() {
        if (this.mDeferredUserUnlockRead) {
            Log.i(TAG, "Handling user unlock before loading from store.");
            List<WifiConfigStore.StoreFile> userStoreFiles = WifiConfigStore.createUserFiles(this.mCurrentUserId, UserManager.get(this.mContext));
            if (userStoreFiles == null) {
                Log.wtf(TAG, "Failed to create user store files");
                return false;
            }
            this.mWifiConfigStore.setUserStores(userStoreFiles);
            this.mDeferredUserUnlockRead = false;
        }
        if (OppoNetworkRecordHelper.isNetworkRecordTxtPresent()) {
            this.mNetworkRecordHelper.loadAllNetworkRecords();
        }
        try {
            this.mWifiConfigStore.read();
            loadInternalData(this.mNetworkListSharedStoreData.getConfigurations(), this.mNetworkListUserStoreData.getConfigurations(), this.mDeletedEphemeralSsidsStoreData.getSsidToTimeMap(), this.mRandomizedMacStoreData.getMacMapping());
            addOrUpdateAutoConnectAp();
            OppoWifiOCloudImpl oppoWifiOCloudImpl = this.mOppoWifiOCloudImpl;
            if (oppoWifiOCloudImpl == null) {
                return true;
            }
            oppoWifiOCloudImpl.notifyLoadFromStore();
            return true;
        } catch (IOException e) {
            Log.wtf(TAG, "Reading from new store failed. All saved networks are lost!", e);
            loadInternalData(this.mNetworkListSharedStoreData.getConfigurations(), this.mNetworkListUserStoreData.getConfigurations(), this.mDeletedEphemeralSsidsStoreData.getSsidToTimeMap(), this.mRandomizedMacStoreData.getMacMapping());
            return false;
        } catch (XmlPullParserException e2) {
            Log.wtf(TAG, "XML deserialization of store failed. All saved networks are lost!", e2);
            loadInternalData(this.mNetworkListSharedStoreData.getConfigurations(), this.mNetworkListUserStoreData.getConfigurations(), this.mDeletedEphemeralSsidsStoreData.getSsidToTimeMap(), this.mRandomizedMacStoreData.getMacMapping());
            return false;
        }
    }

    private boolean loadFromUserStoreAfterUnlockOrSwitch(int userId) {
        try {
            List<WifiConfigStore.StoreFile> userStoreFiles = WifiConfigStore.createUserFiles(userId, UserManager.get(this.mContext));
            if (userStoreFiles == null) {
                Log.e(TAG, "Failed to create user store files");
                return false;
            }
            this.mWifiConfigStore.switchUserStoresAndRead(userStoreFiles);
            loadInternalDataFromUserStore(this.mNetworkListUserStoreData.getConfigurations(), this.mDeletedEphemeralSsidsStoreData.getSsidToTimeMap());
            return true;
        } catch (IOException e) {
            Log.wtf(TAG, "Reading from new store failed. All saved private networks are lost!", e);
            return false;
        } catch (XmlPullParserException e2) {
            Log.wtf(TAG, "XML deserialization of store failed. All saved private networks arelost!", e2);
            return false;
        }
    }

    public boolean saveToStore(boolean forceWrite) {
        if (this.mPendingStoreRead) {
            Log.e(TAG, "Cannot save to store before store is read!");
            return false;
        }
        ArrayList<WifiConfiguration> sharedConfigurations = new ArrayList<>();
        ArrayList<WifiConfiguration> userConfigurations = new ArrayList<>();
        List<Integer> legacyPasspointNetId = new ArrayList<>();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForAllUsers()) {
            if (!config.ephemeral && (!config.isPasspoint() || config.isLegacyPasspointConfig)) {
                if (config.isLegacyPasspointConfig && WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(this.mCurrentUserId))) {
                    legacyPasspointNetId.add(Integer.valueOf(config.networkId));
                    if (!PasspointManager.addLegacyPasspointConfig(config)) {
                        Log.e(TAG, "Failed to migrate legacy Passpoint config: " + config.FQDN);
                    }
                } else if (config.shared || !WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(this.mCurrentUserId))) {
                    sharedConfigurations.add(config);
                } else {
                    userConfigurations.add(config);
                }
            }
        }
        for (Integer num : legacyPasspointNetId) {
            this.mConfiguredNetworks.remove(num.intValue());
        }
        this.mNetworkListSharedStoreData.setConfigurations(sharedConfigurations);
        this.mNetworkListUserStoreData.setConfigurations(userConfigurations);
        this.mDeletedEphemeralSsidsStoreData.setSsidToTimeMap(this.mDeletedEphemeralSsidsToTimeMap);
        this.mRandomizedMacStoreData.setMacMapping(this.mRandomizedMacAddressMapping);
        try {
            this.mWifiConfigStore.write(forceWrite);
            return true;
        } catch (IOException e) {
            Log.wtf(TAG, "Writing to store failed. Saved networks maybe lost!", e);
            return false;
        } catch (XmlPullParserException e2) {
            Log.wtf(TAG, "XML serialization for store failed. Saved networks maybe lost!", e2);
            return false;
        }
    }

    private void localLog(String s) {
        LocalLog localLog = this.mLocalLog;
        if (localLog != null) {
            localLog.log(s);
        }
        Log.d(TAG, s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiConfigManager");
        pw.println("WifiConfigManager - Log Begin ----");
        this.mLocalLog.dump(fd, pw, args);
        pw.println("WifiConfigManager - Log End ----");
        pw.println("WifiConfigManager - Configured networks Begin ----");
        for (WifiConfiguration network : getInternalConfiguredNetworks()) {
            pw.println(network);
        }
        pw.println("WifiConfigManager - Configured networks End ----");
        pw.println("WifiConfigManager - Next network ID to be allocated " + this.mNextNetworkId);
        pw.println("WifiConfigManager - Last selected network ID " + this.mLastSelectedNetworkId);
        pw.println("WifiConfigManager - PNO scan frequency culling enabled = " + this.mPnoFrequencyCullingEnabled);
        pw.println("WifiConfigManager - PNO scan recency sorting enabled = " + this.mPnoRecencySortingEnabled);
        this.mWifiConfigStore.dump(fd, pw, args);
    }

    private boolean canModifyProxySettings(int uid) {
        DevicePolicyManagerInternal dpmi = this.mWifiPermissionsWrapper.getDevicePolicyManagerInternal();
        boolean isUidProfileOwner = dpmi != null && dpmi.isActiveAdminWithPolicy(uid, -1);
        boolean isUidDeviceOwner = dpmi != null && dpmi.isActiveAdminWithPolicy(uid, -2);
        boolean hasNetworkSettingsPermission = this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid);
        boolean hasNetworkSetupWizardPermission = this.mWifiPermissionsUtil.checkNetworkSetupWizardPermission(uid);
        if (isUidDeviceOwner || isUidProfileOwner || hasNetworkSettingsPermission || hasNetworkSetupWizardPermission) {
            return true;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "UID: " + uid + " cannot modify WifiConfiguration proxy settings. hasNetworkSettings=" + hasNetworkSettingsPermission + " hasNetworkSetupWizard=" + hasNetworkSetupWizardPermission + " DeviceOwner=" + isUidDeviceOwner + " ProfileOwner=" + isUidProfileOwner);
        }
        return false;
    }

    public void setOnSavedNetworkUpdateListener(OnSavedNetworkUpdateListener listener) {
        List<OnSavedNetworkUpdateListener> list = this.mListeners;
        if (list != null) {
            list.add(listener);
        }
    }

    public void setRecentFailureAssociationStatus(int netId, int reason) {
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (config != null) {
            config.recentFailure.setAssociationStatus(reason);
        }
    }

    public void clearRecentFailureReason(int netId) {
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (config != null) {
            config.recentFailure.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean disableAndRemoveNetwork(int netId, int uid, int reason) {
        if (OppoManuConnectManager.getInstance() != null) {
            this.mLastConnectIsManual = OppoManuConnectManager.getInstance().isManuConnect();
            OppoManuConnectManager.getInstance().handleNetworkDisabled(netId);
        }
        sendNetworkDisabledEvt(netId, reason);
        boolean ret = disableNetwork(netId, uid);
        WifiConfiguration network = null;
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (config != null) {
            config.disableReason = reason;
            network = config;
            if (this.mVerboseLoggingEnabled) {
                Log.e(TAG, "disableAndRemoveNetwork netId=" + Integer.toString(netId) + " SSID=" + config.SSID + " reason=" + Integer.toString(reason));
            }
        }
        if (network != null) {
            sendAlertNetworksChangedBroadcast(netId, network, reason, SupplicantState.DISCONNECTED);
            sendConfiguredNetworkChangedBroadcast(network, 2);
        }
        return ret;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
     arg types: [java.lang.String, android.net.wifi.SupplicantState]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent} */
    /* access modifiers changed from: package-private */
    public void sendAlertNetworksChangedBroadcast(int netId, WifiConfiguration network, int reason, SupplicantState state) {
        ClientModeImpl clientModeImpl;
        Intent intent = new Intent(DISABLE_ALERT);
        ServiceManager.getService("package");
        int disableReasontoWifiSetting = 0;
        if (!(network == null || OppoManuConnectManager.getInstance() == null)) {
            network.BSSID = OppoManuConnectManager.getInstance().getManuConnectBssid();
        }
        if (reason == 3) {
            disableReasontoWifiSetting = 3;
        } else if (reason == 13) {
            disableReasontoWifiSetting = 6;
        } else if (reason == 102) {
            disableReasontoWifiSetting = 0;
        } else if (reason == 2) {
            disableReasontoWifiSetting = 4;
        }
        if (reason == 3 || reason == 2 || reason == 13 || reason == 102) {
            checkRestoreConnectFailInfo(network, reason);
        }
        if (!(reason == -1 || (clientModeImpl = this.mClientModeImpl) == null)) {
            clientModeImpl.resetVerbose();
        }
        if (this.mVerboseLoggingEnabled && network != null) {
            Log.d(TAG, "sendAlertNetworksChangedBroadcast- configssid: " + network.configKey() + ",reason = " + reason + ",disableReasontoWifiSetting = " + disableReasontoWifiSetting + ",state = " + state);
        }
        intent.addFlags(67108864);
        intent.putExtra("wifiConfiguration", network);
        intent.putExtra("changeReason", disableReasontoWifiSetting);
        intent.putExtra("newState", (Parcelable) state);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        if (state == SupplicantState.DISCONNECTED && this.mClientModeImpl.getRomUpdateBooleanValue("CONNECT_ENABLE_REMOVE_NETWORK_WITH_WRONGKEY", ENABLE_REMOVE_NETWORK_WITH_WRONGKEY).booleanValue()) {
            if ((reason == 13 || reason == 102) && this.mNewNetwork) {
                OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiStateTracker;
                if (!(oppoWifiAssistantStateTraker == null || network == null)) {
                    oppoWifiAssistantStateTraker.rmOrupdateRecordStatus(network.configKey(false), true);
                }
                removeNetwork(netId, 1000);
                this.mNewNetwork = false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sendAlertNetworksChangedBroadcast(int netId, WifiConfiguration network, int reason) {
        if (network == null) {
            Log.d(TAG, "sendAlertNetworksChangedBroadcast- network is null!");
            return;
        }
        Intent intent = new Intent(DISABLE_ALERT);
        int disableReasontoWifiSetting = 0;
        if (reason == 3) {
            disableReasontoWifiSetting = 3;
        } else if (reason == 13) {
            disableReasontoWifiSetting = 6;
        } else if (reason == 102) {
            disableReasontoWifiSetting = 0;
        } else if (reason == 2) {
            disableReasontoWifiSetting = 4;
        }
        if (reason == 3 || reason == 2 || reason == 13 || reason == 102) {
            checkRestoreConnectFailInfo(network, reason);
        }
        this.mClientModeImpl.resetVerbose();
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "sendAlertNetworksChangedBroadcast- configssid: " + network.configKey() + ",reason = " + reason + ",disableReasontoWifiSetting = " + disableReasontoWifiSetting);
        }
        intent.addFlags(67108864);
        intent.putExtra("wifiConfiguration", network);
        intent.putExtra("changeReason", disableReasontoWifiSetting);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: package-private */
    public boolean disableNetwork(int netId, int uid, int reason) {
        if (this.mVerboseLoggingEnabled) {
            localLog("disableNetwork netid= " + netId);
        }
        boolean ret = disableNetwork(netId, uid);
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().handleNetworkDisabled(netId);
        }
        WifiConfiguration network = null;
        WifiConfiguration config = this.mConfiguredNetworks.getForCurrentUser(netId);
        if (this.mVerboseLoggingEnabled && config != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("disableNetwork netId=");
            sb.append(Integer.toString(netId));
            sb.append(" SSID=");
            sb.append(config.SSID);
            sb.append(" disabled=");
            sb.append(config.status == 1);
            sb.append(" reason=");
            sb.append(Integer.toString(config.disableReason));
            Log.e(TAG, sb.toString());
        }
        if (config != null) {
            if (config.status != 1) {
                config.status = 1;
                config.disableReason = reason;
                network = config;
            }
            config.disableReason = reason;
            if (reason == 11) {
                config.status = 1;
            }
        }
        if (network != null) {
            sendConfiguredNetworkChangedBroadcast(network, 2);
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiStateTracker;
            if (!(oppoWifiAssistantStateTraker == null || reason == 102 || reason == 5 || reason == 11)) {
                oppoWifiAssistantStateTraker.rmOrupdateRecordStatus(network.configKey(false), false);
            }
            sendNetworkDisabledEvt(network.networkId, reason);
        } else if (config != null && reason == 10) {
            sendNetworkDisabledEvt(config.networkId, reason);
        }
        return ret;
    }

    public List<WifiConfiguration> getSavedNetworksAll() {
        if (this.mConfiguredNetworks == null) {
            Log.e(TAG, "mConfiguredNetworks is null");
            return null;
        }
        List<WifiConfiguration> wcList = new ArrayList<>();
        wcList.addAll(this.mConfiguredNetworks.valuesForAllUsers());
        return wcList;
    }

    public void setIsNewNetwork(boolean isNew) {
        this.mNewNetwork = isNew;
    }

    public WifiConfiguration getWifiConfigurationForAll(int netId) {
        return this.mConfiguredNetworks.getForAllUsers(netId);
    }

    public void clearConfiguredNetworkBssid(int netId) {
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (config != null) {
            config.BSSID = "any";
        }
        saveToStore(true);
    }

    public void clearDisableReasonCounter(int netId) {
        WifiConfiguration.NetworkSelectionStatus status;
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (config != null && (status = config.getNetworkSelectionStatus()) != null) {
            try {
                status.clearDisableReasonCounter();
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "setDisableReasonCounter with invalid reasonCode!");
            }
        }
    }

    public void clearDisableReasonCounter(int reason, int netId) {
        WifiConfiguration.NetworkSelectionStatus status;
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (config != null && (status = config.getNetworkSelectionStatus()) != null) {
            try {
                status.clearDisableReasonCounter(reason);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "clearDisableReasonCounter with invalid reasonCode!");
            }
        }
    }

    public void setDisableReasonCounter(int reason, int value, int netId) {
        WifiConfiguration.NetworkSelectionStatus status;
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (config != null && (status = config.getNetworkSelectionStatus()) != null) {
            try {
                status.setDisableReasonCounter(reason, value);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "setDisableReasonCounter with invalid reasonCode!");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setWifiNetwork(OppoWifiNetworkSwitchEnhance wa) {
        this.mWifiAvailable = wa;
    }

    /* access modifiers changed from: package-private */
    public void setWifiNetworkAvailable(OppoWifiAssistantStateTraker wst) {
        this.mWifiStateTracker = wst;
        this.mOppoWifiConfigManagerUtil.setWifiNetworkAvailable(wst);
    }

    /* access modifiers changed from: package-private */
    public void setWifiAutoSwitch(boolean autoSwitch) {
        this.mAutoJoinSwitch = autoSwitch;
    }

    public void oppoSetLastSelectedNetwork(int networkId) {
        setLastSelectedNetwork(networkId);
    }

    private void addOrUpdateAutoConnectAp() {
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().addOrUpdateAutoConnectAp();
        }
    }

    /* access modifiers changed from: package-private */
    public void enableAllNetworks() {
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().sendEnableAllNetworksEvt();
        }
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

    /* access modifiers changed from: package-private */
    public boolean enableNetworkEx(int networkId, boolean disableOthers, int uid, boolean sendBroadcast) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Enabling network " + networkId + " (disableOthers " + disableOthers + ")");
        }
        if (!doesUidBelongToCurrentUser(uid)) {
            Log.e(TAG, "UID " + uid + " not visible to the current user");
            return false;
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        if (!canModifyNetwork(config, uid)) {
            Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + config.configKey());
            return false;
        } else if (!isAutoConnectSwitchEnable() || config.userApproved != 1001) {
            config.status = 2;
            WifiConfiguration.NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
            if (networkStatus != null) {
                networkStatus.setNetworkSelectionStatus(0);
            }
            OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiStateTracker;
            if (oppoWifiAssistantStateTraker != null) {
                oppoWifiAssistantStateTraker.resetConnExp(config.configKey(false));
            }
            if (disableOthers) {
                setLastSelectedNetwork(networkId);
            }
            saveToStore(true);
            return true;
        } else {
            Log.e(TAG, "UID" + uid + "[BUGID:1803910] disable by user:WLAN details added to the auto connect switch");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean disableNetworkEx(int networkId, int reason, boolean sendBroadcast) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Disabling network " + networkId);
        }
        this.mClientModeImpl.resetVerbose();
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        setNetworkSelectionTemporarilyDisabled(config, 1);
        setNetworkStatus(config, 1);
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().handleNetworkDisabled(networkId);
        }
        sendNetworkDisabledEvt(networkId, reason);
        OppoWifiAssistantStateTraker oppoWifiAssistantStateTraker = this.mWifiStateTracker;
        if (!(oppoWifiAssistantStateTraker == null || reason == 102 || reason == 5 || reason == 11)) {
            oppoWifiAssistantStateTraker.rmOrupdateRecordStatus(config.configKey(false), false);
        }
        if (networkId == this.mLastSelectedNetworkId) {
            clearLastSelectedNetwork();
        }
        saveToStore(true);
        return true;
    }

    public void storethirdAPKWifiDiagnosis(boolean isSuccess, int failReason) {
        HashMap<String, String> resultmap = new HashMap<>();
        String str = this.mStartConPkgName;
        if (str != null) {
            resultmap.put(WIFI_DIAGNOSIS_CONNECT_APK_NAME, str);
            resultmap.put(WIFI_DIAGNOSIS_CONNECT_RESULT, String.valueOf(isSuccess));
            resultmap.put(WIFI_DIAGNOSIS_CONNECT_FAIL_REASON, String.valueOf(failReason));
            OppoManager.onStamp(WIFI_DIAGNOSIS_STATISTIC_KEY, resultmap);
            this.mStartConPkgName = null;
        }
    }

    public void statisticsStartConPkgName(int connectUid) {
        String[] pkgs;
        if (connectUid >= 0) {
            PackageManager mPackageManager = this.mContext.getPackageManager();
            this.mStartConPkgName = null;
            if (mPackageManager != null && (pkgs = mPackageManager.getPackagesForUid(connectUid)) != null) {
                for (String mPkgName : pkgs) {
                    if (mPkgName != null) {
                        try {
                            ApplicationInfo applicationInfo = mPackageManager.getApplicationInfo(mPkgName, 0);
                            if (applicationInfo != null) {
                                this.mStartConPkgName = mPackageManager.getApplicationLabel(applicationInfo).toString();
                                return;
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private boolean isThirdAppConnect() {
        if (OppoManuConnectManager.getInstance() == null) {
            return false;
        }
        if (OppoManuConnectManager.getInstance().getType() == 1) {
            return true;
        }
        int operatorUid = OppoManuConnectManager.getInstance().getManuConnectUid();
        if (operatorUid == 1000 || operatorUid == 1010) {
            return false;
        }
        return true;
    }

    public boolean removeNetworkWithoutBroadcast(int netId) {
        if (!removeConfigWithoutBroadcast(netId)) {
            return false;
        }
        return true;
    }

    private boolean removeConfigWithoutBroadcast(int netId) {
        WifiConfiguration config = this.mConfiguredNetworks.getForAllUsers(netId);
        if (config != null) {
            if (this.mVerboseLoggingEnabled) {
                Log.e(TAG, "removeConfigWithoutBroadcast " + Integer.toString(netId) + " key=" + config.configKey() + " config.id=" + Integer.toString(config.networkId));
            }
            if (config.networkId == this.mLastSelectedNetworkId) {
                clearLastSelectedNetwork();
            }
            if (config.enterpriseConfig != null) {
                this.mWifiKeyStore.removeKeys(config.enterpriseConfig);
            }
            this.mConfiguredNetworks.remove(netId);
            return true;
        }
        Log.e(TAG, "removeConfigWithoutBroadcast no config found!!");
        return false;
    }

    public OppoNetworkRecordHelper getOppoNetworkRecordHelper() {
        return this.mNetworkRecordHelper;
    }

    public void setReasonCode(int reason) {
        this.mOppoWifiConfigManagerUtil.setReasonCode(reason);
    }

    private ScanResult findHomonyAPFromScanResults(WifiConfiguration config) {
        return this.mOppoWifiConfigManagerUtil.findHomonyAPFromScanResults(config, this.mClientModeImpl);
    }

    private void checkRestoreConnectFailInfo(WifiConfiguration network, int reason) {
        this.mOppoWifiConfigManagerUtil.checkRestoreConnectFailInfo(network, reason, this.mClientModeImpl);
    }

    public boolean isAutoConnectSwitchEnable() {
        return this.mContext.getPackageManager().hasSystemFeature("oppo.common.wifi.choose.auto_reconnect");
    }

    private void forSavedNetworkUpdateListeners(String methon, int networkId) {
        List<OnSavedNetworkUpdateListener> list = this.mListeners;
        if (list == null || list.size() == 0) {
            Log.e(TAG, "no listener found!");
            return;
        }
        for (OnSavedNetworkUpdateListener listener : this.mListeners) {
            if (listener != null) {
                char c = 65535;
                switch (methon.hashCode()) {
                    case -1360944303:
                        if (methon.equals("onSavedNetworkPermanentlyDisabled")) {
                            c = 2;
                            break;
                        }
                        break;
                    case -1095694155:
                        if (methon.equals("onSavedNetworkUpdated")) {
                            c = 5;
                            break;
                        }
                        break;
                    case -55722118:
                        if (methon.equals("onSavedNetworkAdded")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 230572122:
                        if (methon.equals("onSavedNetworkRemoved")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1733112100:
                        if (methon.equals("onSavedNetworkTemporarilyDisabled")) {
                            c = 4;
                            break;
                        }
                        break;
                    case 1824109371:
                        if (methon.equals("onSavedNetworkEnabled")) {
                            c = 1;
                            break;
                        }
                        break;
                }
                if (c == 0) {
                    listener.onSavedNetworkAdded(networkId);
                } else if (c == 1) {
                    listener.onSavedNetworkEnabled(networkId);
                } else if (c != 2) {
                    if (c == 3) {
                        listener.onSavedNetworkRemoved(networkId);
                    } else if (c != 4) {
                        if (c != 5) {
                            Log.e(TAG, "unknown listener methon: " + methon);
                        } else {
                            listener.onSavedNetworkUpdated(networkId);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void clearWifiOCloudData(boolean hardDelete) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "clear wifi ocloud data, hard delete: " + hardDelete);
        }
        Collection<WifiConfiguration> configsReference = this.mConfiguredNetworks.valuesForAllUsers();
        if (hardDelete) {
            this.mConfiguredNetworks.clear();
            this.mOppoWifiOCloudImpl.clear();
            sendConfiguredNetworksChangedBroadcast();
        } else if (configsReference != null) {
            for (WifiConfiguration config : configsReference) {
                config.globalId = "NULL";
                this.mOppoWifiOCloudImpl.setDirtyFlag(config.configKey(), true, true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public List<String> getWifiOCloudData(boolean isDirtyOnly) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "getting ocloud dirty data...");
        }
        return this.mOppoWifiOCloudImpl.getWifiOCloudData(isDirtyOnly);
    }

    /* access modifiers changed from: protected */
    public void updateGlobalId(int networkId, String globalId) {
        WifiConfiguration internalConfig = getInternalConfiguredNetwork(networkId);
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "updating global id: " + globalId + " for network: " + networkId + " " + internalConfig.configKey());
        }
        this.mOppoWifiOCloudImpl.updateGlobalId(internalConfig, globalId);
    }

    /* access modifiers changed from: protected */
    public void removeNetworkByGlobalId(String configKey, String globalId, boolean hardDelete) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "removing Network by globalId " + globalId + " cfg: " + configKey);
        }
        this.mOppoWifiOCloudImpl.removeNetworkByGlobalId(configKey, globalId, hardDelete);
        WifiConfiguration marked = null;
        Iterator<WifiConfiguration> it = this.mConfiguredNetworks.valuesForAllUsers().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            WifiConfiguration internalConfig = it.next();
            if (internalConfig.globalId.equals(globalId)) {
                marked = internalConfig;
                break;
            }
        }
        if (marked != null) {
            Log.d(TAG, "deleteing from mConfiguredNetworks: " + marked.configKey());
            if (marked.status == 0) {
                this.mClientModeImpl.disconnectCommand();
            }
            WifiConfiguration removed = this.mConfiguredNetworks.remove(marked.networkId);
            if (removed != null) {
                sendConfiguredNetworkChangedBroadcast(removed, 1);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setDirtyFlag(String globalId, boolean value) {
        this.mOppoWifiOCloudImpl.setDirtyFlag(globalId, value);
    }

    /* access modifiers changed from: protected */
    public boolean hasOCloudDirtyData() {
        return this.mOppoWifiOCloudImpl.hasOCloudDirtyData();
    }

    public boolean setConfiguredNetworkCaptiveState(boolean captiveState) {
        WifiConfiguration config;
        WifiInfo mWifiInfo = this.mClientModeImpl.getWifiInfo();
        if (mWifiInfo != null && (config = getInternalConfiguredNetwork(mWifiInfo.getNetworkId())) != null) {
            config.everLogin = captiveState;
            saveToStore(true);
            return true;
        } else if (!this.mVerboseLoggingEnabled) {
            return false;
        } else {
            Log.v(TAG, "setConfiguredNetworkCaptiveState fail");
            return false;
        }
    }

    public boolean getNetworkEverCaptiveState() {
        WifiConfiguration config;
        WifiInfo mWifiInfo = this.mClientModeImpl.getWifiInfo();
        if (mWifiInfo != null && (config = getInternalConfiguredNetwork(mWifiInfo.getNetworkId())) != null && config.everLogin && OppoAutoConnectManager.getInstance() != null && !OppoAutoConnectManager.getInstance().isWlanAssistAutoConnectNetwork() && OppoManuConnectManager.getInstance() != null && !OppoManuConnectManager.getInstance().isManuConnect()) {
            return true;
        }
        if (!this.mVerboseLoggingEnabled) {
            return false;
        }
        Log.v(TAG, "getNetworkEverCaptiveState fail or false");
        return false;
    }
}
