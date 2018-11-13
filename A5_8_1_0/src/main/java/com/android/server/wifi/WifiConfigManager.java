package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIfaceCallback.StatusCode;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner.PnoSettings.PnoNetwork;
import android.net.wifi.WifiScanner.ScanSettings.HiddenNetwork;
import android.os.OppoManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.LocalServices;
import com.android.server.pm.PackageManagerService;
import com.android.server.wifi.WifiConfigStoreLegacy.WifiConfigStoreDataLegacy;
import com.android.server.wifi.WifiConfigurationUtil.WifiConfigurationComparator;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParserException;

public class WifiConfigManager {
    private static final boolean ALLOW_LOCKDOWN_CHECK_BYPASS = true;
    private static final int ASSOC_REJECT_ALGORITHM_NOT_SUPPORTED = 13;
    private static final int ASSOC_REJECT_AP_OVERLOAD = 17;
    private static final int ASSOC_REJECT_CHALLENGE_FAIL = 15;
    private static final int ASSOC_REJECT_OUT_OF_SEQ = 14;
    private static final int DISABLED_ASSOCIATION_REJECT = 4;
    private static final int DISABLED_AUTH_FAILURE = 3;
    private static final int DISABLED_UNKNOWN_REASON = 0;
    private static final int DISABLED_WRONG_KEY = 6;
    private static final String DISABLE_ALERT = "android.net.wifi.DISABLE_ALERT_NETWORKS";
    private static final boolean DISALLOW_LOCKDOWN_CHECK_BYPASS = false;
    private static final Boolean ENABLE_REMOVE_NETWORK_WITH_WRONGKEY = Boolean.valueOf(true);
    private static final int HIDDEN_AP_MAX_SCAN_COUNTS = 16;
    public static final int LINK_CONFIGURATION_BSSID_MATCH_LENGTH = 16;
    public static final int LINK_CONFIGURATION_MAX_SCAN_CACHE_ENTRIES = 6;
    public static final int[] NETWORK_SELECTION_DISABLE_THRESHOLD = new int[]{-1, 1, 12, 5, 5, 5, 1, 6, 1, 1, 1, 1, 4};
    public static final int[] NETWORK_SELECTION_DISABLE_TIMEOUT_MS = new int[]{Integer.MAX_VALUE, 900000, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, WifiConnectivityManager.BSSID_BLACKLIST_EXPIRE_TIME_MS, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
    public static final String PASSWORD_MASK = "*";
    public static final int SCAN_CACHE_ENTRIES_MAX_SIZE = 192;
    public static final int SCAN_CACHE_ENTRIES_TRIM_SIZE = 128;
    private static final int SCAN_RESULT_MAXIMUM_AGE_MS = 40000;
    public static final String SYSUI_PACKAGE_NAME = "com.android.systemui";
    private static final String TAG = "WifiConfigManager";
    private static final String ctsPkg = "com.android.cts.verifier";
    private static final WifiConfigurationComparator sScanListComparator = new WifiConfigurationComparator() {
        public int compareNetworksWithSameStatus(WifiConfiguration a, WifiConfiguration b) {
            return Long.compare(b.lastConnected, a.lastConnected);
        }
    };
    private boolean mAutoJoinSwitch;
    private final BackupManagerProxy mBackupManagerProxy;
    private final Clock mClock;
    private final ConfigurationMap mConfiguredNetworks;
    private final Context mContext;
    private int mCurrentUserId;
    private boolean mDeferredUserUnlockRead;
    private final Set<String> mDeletedEphemeralSSIDs;
    private final DeletedEphemeralSsidsStoreData mDeletedEphemeralSsidsStoreData;
    private int mLastSelectedNetworkId;
    private long mLastSelectedTimeStamp;
    private OnSavedNetworkUpdateListener mListener;
    private final LocalLog mLocalLog;
    private final int mMaxNumActiveChannelsForPartialScans;
    private final NetworkListStoreData mNetworkListStoreData;
    private OppoNetworkRecordHelper mNetworkRecordHelper;
    private boolean mNewNetwork;
    private int mNextNetworkId;
    private final boolean mOnlyLinkSameCredentialConfigurations;
    private boolean mPendingStoreRead;
    private boolean mPendingUnlockStoreRead;
    private int mReasonCode;
    private final Map<Integer, ScanDetailCache> mScanDetailCaches;
    private int mScanResultRssiAssocReject;
    private List<ScanResult> mScanResults;
    private boolean mSimPresent;
    private int mSystemUiUid;
    private final TelephonyManager mTelephonyManager;
    private final UserManager mUserManager;
    private boolean mVerboseLoggingEnabled;
    private OppoWifiNetworkSwitchEnhance mWifiAvailable;
    private final WifiConfigStore mWifiConfigStore;
    private final WifiConfigStoreLegacy mWifiConfigStoreLegacy;
    private final WifiKeyStore mWifiKeyStore;
    private final WifiPermissionsUtil mWifiPermissionsUtil;
    private final WifiPermissionsWrapper mWifiPermissionsWrapper;
    public WifiStateMachine mWifiStateMachine;
    private OppoWifiAssistantStateTraker mWifiStateTracker;

    public interface OnSavedNetworkUpdateListener {
        void onSavedNetworkAdded(int i);

        void onSavedNetworkEnabled(int i);

        void onSavedNetworkPermanentlyDisabled(int i);

        void onSavedNetworkRemoved(int i);

        void onSavedNetworkTemporarilyDisabled(int i);

        void onSavedNetworkUpdated(int i);
    }

    WifiConfigManager(Context context, Clock clock, UserManager userManager, TelephonyManager telephonyManager, WifiKeyStore wifiKeyStore, WifiConfigStore wifiConfigStore, WifiConfigStoreLegacy wifiConfigStoreLegacy, WifiPermissionsUtil wifiPermissionsUtil, WifiPermissionsWrapper wifiPermissionsWrapper, NetworkListStoreData networkListStoreData, DeletedEphemeralSsidsStoreData deletedEphemeralSsidsStoreData) {
        this.mLocalLog = new LocalLog(ActivityManager.isLowRamDeviceStatic() ? 128 : 256);
        this.mVerboseLoggingEnabled = false;
        this.mCurrentUserId = 0;
        this.mPendingUnlockStoreRead = true;
        this.mPendingStoreRead = true;
        this.mDeferredUserUnlockRead = false;
        this.mSimPresent = false;
        this.mNextNetworkId = 0;
        this.mSystemUiUid = -1;
        this.mLastSelectedNetworkId = -1;
        this.mLastSelectedTimeStamp = -1;
        this.mListener = null;
        this.mNewNetwork = false;
        this.mScanResults = new ArrayList();
        this.mScanResultRssiAssocReject = -80;
        this.mReasonCode = 0;
        this.mContext = context;
        this.mClock = clock;
        this.mUserManager = userManager;
        this.mBackupManagerProxy = new BackupManagerProxy();
        this.mTelephonyManager = telephonyManager;
        this.mWifiKeyStore = wifiKeyStore;
        this.mWifiConfigStore = wifiConfigStore;
        this.mWifiConfigStoreLegacy = wifiConfigStoreLegacy;
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
        this.mWifiPermissionsWrapper = wifiPermissionsWrapper;
        this.mConfiguredNetworks = new ConfigurationMap(userManager);
        this.mScanDetailCaches = new HashMap(16, 0.75f);
        this.mDeletedEphemeralSSIDs = new HashSet();
        this.mNetworkListStoreData = networkListStoreData;
        this.mDeletedEphemeralSsidsStoreData = deletedEphemeralSsidsStoreData;
        this.mWifiConfigStore.registerStoreData(this.mNetworkListStoreData);
        this.mWifiConfigStore.registerStoreData(this.mDeletedEphemeralSsidsStoreData);
        this.mOnlyLinkSameCredentialConfigurations = this.mContext.getResources().getBoolean(17957072);
        this.mMaxNumActiveChannelsForPartialScans = this.mContext.getResources().getInteger(17694889);
        try {
            this.mSystemUiUid = this.mContext.getPackageManager().getPackageUidAsUser(SYSUI_PACKAGE_NAME, 1048576, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Unable to resolve SystemUI's UID.");
        }
        this.mNetworkRecordHelper = new OppoNetworkRecordHelper(this);
    }

    public static String createDebugTimeStampString(long wallClockMillis) {
        StringBuilder sb = new StringBuilder();
        sb.append("time=");
        Calendar.getInstance().setTimeInMillis(wallClockMillis);
        sb.append(String.format("%tm-%td %tH:%tM:%tS.%tL", new Object[]{c, c, c, c, c, c}));
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
        this.mNetworkRecordHelper.enableVerboseLogging(verbose);
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

    private WifiConfiguration createExternalWifiConfiguration(WifiConfiguration configuration, boolean maskPasswords) {
        WifiConfiguration network = new WifiConfiguration(configuration);
        if (maskPasswords) {
            maskPasswordsInWifiConfiguration(network);
        }
        return network;
    }

    private List<WifiConfiguration> getConfiguredNetworks(boolean savedOnly, boolean maskPasswords) {
        List<WifiConfiguration> networks = new ArrayList();
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (!savedOnly || !config.ephemeral) {
                networks.add(createExternalWifiConfiguration(config, maskPasswords));
            }
        }
        return networks;
    }

    public List<WifiConfiguration> getConfiguredNetworks() {
        return getConfiguredNetworks(false, true);
    }

    public List<WifiConfiguration> getConfiguredNetworksWithPasswords() {
        return getConfiguredNetworks(false, false);
    }

    public List<WifiConfiguration> getSavedNetworks() {
        return getConfiguredNetworks(true, true);
    }

    public WifiConfiguration getConfiguredNetwork(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        return createExternalWifiConfiguration(config, true);
    }

    public WifiConfiguration getConfiguredNetwork(String configKey) {
        WifiConfiguration config = getInternalConfiguredNetwork(configKey);
        if (config == null) {
            return null;
        }
        return createExternalWifiConfiguration(config, true);
    }

    public WifiConfiguration getConfiguredNetworkWithPassword(int networkId) {
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return null;
        }
        return createExternalWifiConfiguration(config, false);
    }

    private Collection<WifiConfiguration> getInternalConfiguredNetworks() {
        return this.mConfiguredNetworks.valuesForCurrentUser();
    }

    private WifiConfiguration getInternalConfiguredNetwork(WifiConfiguration config) {
        WifiConfiguration internalConfig = this.mConfiguredNetworks.getForCurrentUser(config.networkId);
        if (internalConfig != null) {
            return internalConfig;
        }
        internalConfig = this.mConfiguredNetworks.getByConfigKeyForCurrentUser(config.configKey());
        if (internalConfig == null) {
            Log.e(TAG, "Cannot find network with networkId " + config.networkId + " or configKey " + config.configKey());
        }
        return internalConfig;
    }

    private WifiConfiguration getInternalConfiguredNetwork(int networkId) {
        if (networkId == -1) {
            Log.w(TAG, "Looking up network with invalid networkId -1");
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

    void sendConfiguredNetworksChangedBroadcast() {
        Intent intent = new Intent("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        intent.addFlags(67108864);
        intent.putExtra("multipleChanges", true);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean canModifyNetwork(WifiConfiguration config, int uid, boolean ignoreLockdown) {
        boolean z = true;
        if (uid == OppoManuConnectManager.UID_DEFAULT) {
            return true;
        }
        String pkgName = null;
        if (this.mContext != null) {
            pkgName = this.mContext.getPackageManager().getNameForUid(uid);
        }
        if (pkgName != null && pkgName.contains("com.coloros.wifisecuredetect")) {
            Log.d(TAG, "wifisecuredetect---fliter");
            return true;
        } else if (config.isPasspoint() && uid == 1010) {
            return true;
        } else {
            if (config.enterpriseConfig != null && uid == 1010 && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())) {
                return true;
            }
            DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
            if (dpmi != null ? dpmi.isActiveAdminWithPolicy(uid, -2) : false) {
                return true;
            }
            boolean isCreator = config.creatorUid == uid;
            if (ignoreLockdown) {
                return this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid);
            }
            if (this.mContext.getPackageManager().hasSystemFeature("android.software.device_admin") && dpmi == null) {
                Log.w(TAG, "Error retrieving DPMI service.");
                return false;
            }
            if (dpmi != null ? dpmi.isActiveAdminWithPolicy(config.creatorUid, -2) : false) {
                if (Global.getInt(this.mContext.getContentResolver(), "wifi_device_owner_configs_lockdown", 0) != 0) {
                    z = false;
                } else {
                    z = this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid);
                }
                return z;
            }
            if (!isCreator) {
                z = this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid);
            }
            return z;
        }
    }

    private boolean doesUidBelongToCurrentUser(int uid) {
        if (uid == OppoManuConnectManager.UID_DEFAULT || uid == this.mSystemUiUid) {
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
        if (!(externalConfig.preSharedKey == null || (externalConfig.preSharedKey.equals("*") ^ 1) == 0)) {
            internalConfig.preSharedKey = externalConfig.preSharedKey;
        }
        if (externalConfig.wepKeys != null) {
            boolean hasWepKey = false;
            int i = 0;
            while (i < internalConfig.wepKeys.length) {
                if (!(externalConfig.wepKeys[i] == null || (externalConfig.wepKeys[i].equals("*") ^ 1) == 0)) {
                    internalConfig.wepKeys[i] = externalConfig.wepKeys[i];
                    hasWepKey = true;
                }
                i++;
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
        if (!(externalConfig.allowedAuthAlgorithms == null || (externalConfig.allowedAuthAlgorithms.isEmpty() ^ 1) == 0)) {
            internalConfig.allowedAuthAlgorithms = (BitSet) externalConfig.allowedAuthAlgorithms.clone();
        }
        if (!(externalConfig.allowedProtocols == null || (externalConfig.allowedProtocols.isEmpty() ^ 1) == 0)) {
            internalConfig.allowedProtocols = (BitSet) externalConfig.allowedProtocols.clone();
        }
        if (!(externalConfig.allowedKeyManagement == null || (externalConfig.allowedKeyManagement.isEmpty() ^ 1) == 0)) {
            internalConfig.allowedKeyManagement = (BitSet) externalConfig.allowedKeyManagement.clone();
        }
        if (!(externalConfig.allowedPairwiseCiphers == null || (externalConfig.allowedPairwiseCiphers.isEmpty() ^ 1) == 0)) {
            internalConfig.allowedPairwiseCiphers = (BitSet) externalConfig.allowedPairwiseCiphers.clone();
        }
        if (!(externalConfig.allowedGroupCiphers == null || (externalConfig.allowedGroupCiphers.isEmpty() ^ 1) == 0)) {
            internalConfig.allowedGroupCiphers = (BitSet) externalConfig.allowedGroupCiphers.clone();
        }
        if (externalConfig.allowedKeyManagement.get(190)) {
            internalConfig.wapiPskType = externalConfig.wapiPskType;
            if (!(externalConfig.wapiPsk == null || (externalConfig.wapiPsk.equals("*") ^ 1) == 0)) {
                internalConfig.wapiPsk = externalConfig.wapiPsk;
            }
        }
        if (externalConfig.allowedKeyManagement.get(191)) {
            internalConfig.wapiCertSelMode = externalConfig.wapiCertSelMode;
            internalConfig.wapiCertSel = externalConfig.wapiCertSel;
        }
        if (externalConfig.getIpConfiguration() != null) {
            IpAssignment ipAssignment = externalConfig.getIpAssignment();
            if (ipAssignment != IpAssignment.UNASSIGNED) {
                internalConfig.setIpAssignment(ipAssignment);
                if (ipAssignment == IpAssignment.STATIC) {
                    internalConfig.setStaticIpConfiguration(new StaticIpConfiguration(externalConfig.getStaticIpConfiguration()));
                }
            }
            ProxySettings proxySettings = externalConfig.getProxySettings();
            if (proxySettings != ProxySettings.UNASSIGNED) {
                internalConfig.setProxySettings(proxySettings);
                if (proxySettings == ProxySettings.PAC || proxySettings == ProxySettings.STATIC) {
                    internalConfig.setHttpProxy(new ProxyInfo(externalConfig.getHttpProxy()));
                }
            }
        }
        internalConfig.shareThisAp = externalConfig.shareThisAp;
        if (externalConfig.enterpriseConfig != null) {
            internalConfig.enterpriseConfig.copyFromExternal(externalConfig.enterpriseConfig, "*");
        }
        internalConfig.meteredHint = externalConfig.meteredHint;
        internalConfig.meteredOverride = externalConfig.meteredOverride;
    }

    private void setDefaultsInWifiConfiguration(WifiConfiguration configuration) {
        configuration.allowedAuthAlgorithms.set(0);
        configuration.allowedProtocols.set(1);
        configuration.allowedProtocols.set(0);
        configuration.allowedProtocols.set(3);
        configuration.allowedKeyManagement.set(1);
        configuration.allowedKeyManagement.set(2);
        configuration.allowedPairwiseCiphers.set(2);
        configuration.allowedPairwiseCiphers.set(1);
        configuration.allowedGroupCiphers.set(3);
        configuration.allowedGroupCiphers.set(2);
        configuration.allowedGroupCiphers.set(0);
        configuration.allowedGroupCiphers.set(1);
        configuration.setIpAssignment(IpAssignment.DHCP);
        configuration.setProxySettings(ProxySettings.NONE);
        configuration.status = 1;
        configuration.getNetworkSelectionStatus().setNetworkSelectionStatus(2);
    }

    private WifiConfiguration createNewInternalWifiConfigurationFromExternal(WifiConfiguration externalConfig, int uid) {
        WifiConfiguration newInternalConfig = new WifiConfiguration();
        int i = this.mNextNetworkId;
        this.mNextNetworkId = i + 1;
        newInternalConfig.networkId = i;
        setDefaultsInWifiConfiguration(newInternalConfig);
        mergeWithInternalWifiConfiguration(newInternalConfig, externalConfig);
        newInternalConfig.requirePMF = externalConfig.requirePMF;
        newInternalConfig.noInternetAccessExpected = externalConfig.noInternetAccessExpected;
        newInternalConfig.ephemeral = externalConfig.ephemeral;
        newInternalConfig.useExternalScores = externalConfig.useExternalScores;
        newInternalConfig.shared = externalConfig.shared;
        newInternalConfig.lastUpdateUid = uid;
        newInternalConfig.creatorUid = uid;
        String nameForUid = this.mContext.getPackageManager().getNameForUid(uid);
        newInternalConfig.lastUpdateName = nameForUid;
        newInternalConfig.creatorName = nameForUid;
        nameForUid = createDebugTimeStampString(this.mClock.getWallClockMillis());
        newInternalConfig.updateTime = nameForUid;
        newInternalConfig.creationTime = nameForUid;
        return newInternalConfig;
    }

    private WifiConfiguration updateExistingInternalWifiConfigurationFromExternal(WifiConfiguration internalConfig, WifiConfiguration externalConfig, int uid) {
        WifiConfiguration newInternalConfig = new WifiConfiguration(internalConfig);
        mergeWithInternalWifiConfiguration(newInternalConfig, externalConfig);
        newInternalConfig.lastUpdateUid = uid;
        newInternalConfig.lastUpdateName = this.mContext.getPackageManager().getNameForUid(uid);
        newInternalConfig.updateTime = createDebugTimeStampString(this.mClock.getWallClockMillis());
        return newInternalConfig;
    }

    private NetworkUpdateResult addOrUpdateNetworkInternal(WifiConfiguration config, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Adding/Updating network " + config.getPrintableSsid());
        }
        WifiConfiguration newInternalConfig = null;
        WifiConfiguration existingInternalConfig = getInternalConfiguredNetwork(config);
        if (existingInternalConfig == null) {
            if (WifiConfigurationUtil.validate(config, true)) {
                newInternalConfig = createNewInternalWifiConfigurationFromExternal(config, uid);
                existingInternalConfig = getInternalConfiguredNetwork(newInternalConfig.configKey());
            } else {
                Log.e(TAG, "Cannot add network with invalid config");
                return new NetworkUpdateResult(-1);
            }
        }
        if (existingInternalConfig != null) {
            if (!WifiConfigurationUtil.validate(config, false)) {
                Log.e(TAG, "Cannot update network with invalid config");
                return new NetworkUpdateResult(-1);
            } else if (canModifyNetwork(existingInternalConfig, uid, false)) {
                newInternalConfig = updateExistingInternalWifiConfigurationFromExternal(existingInternalConfig, config, uid);
            } else {
                Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + config.configKey());
                return new NetworkUpdateResult(-1);
            }
        }
        if (WifiConfigurationUtil.hasProxyChanged(existingInternalConfig, newInternalConfig) && (canModifyProxySettings(uid) ^ 1) != 0) {
            Log.e(TAG, "UID " + uid + " does not have permission to modify proxy Settings " + config.configKey() + ". Must have NETWORK_SETTINGS," + " or be device or profile owner.");
            return new NetworkUpdateResult(-1);
        } else if (config.enterpriseConfig != null && config.enterpriseConfig.getEapMethod() != -1 && (config.isPasspoint() ^ 1) != 0 && !this.mWifiKeyStore.updateNetworkKeys(newInternalConfig, existingInternalConfig)) {
            return new NetworkUpdateResult(-1);
        } else {
            boolean newNetwork = existingInternalConfig == null;
            this.mNewNetwork = newNetwork;
            boolean hasIpChanged = !newNetwork ? WifiConfigurationUtil.hasIpChanged(existingInternalConfig, newInternalConfig) : true;
            boolean hasProxyChanged = !newNetwork ? WifiConfigurationUtil.hasProxyChanged(existingInternalConfig, newInternalConfig) : true;
            boolean hasCredentialChanged = !newNetwork ? WifiConfigurationUtil.hasCredentialChanged(existingInternalConfig, newInternalConfig) : true;
            if (hasCredentialChanged) {
                newInternalConfig.getNetworkSelectionStatus().setHasEverConnected(false);
            }
            try {
                this.mConfiguredNetworks.put(newInternalConfig);
                if (this.mDeletedEphemeralSSIDs.remove(config.SSID) && this.mVerboseLoggingEnabled) {
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

    public NetworkUpdateResult addOrUpdateNetwork(WifiConfiguration config, int uid) {
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
            NetworkUpdateResult result = addOrUpdateNetworkInternal(config, uid);
            if (result.isSuccess()) {
                int i;
                WifiConfiguration newConfig = getInternalConfiguredNetwork(result.getNetworkId());
                if (result.isNewNetwork()) {
                    i = 0;
                } else {
                    i = 2;
                }
                sendConfiguredNetworkChangedBroadcast(newConfig, i);
                if (!(config.ephemeral || (config.isPasspoint() ^ 1) == 0)) {
                    saveToStore(true);
                    if (this.mListener != null) {
                        if (result.isNewNetwork()) {
                            this.mListener.onSavedNetworkAdded(newConfig.networkId);
                        } else {
                            this.mListener.onSavedNetworkUpdated(newConfig.networkId);
                        }
                    }
                }
                return result;
            }
            Log.e(TAG, "Failed to add/update network " + config.getPrintableSsid());
            return result;
        }
    }

    private boolean isExpRom() {
        return SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN") ^ 1;
    }

    private boolean removeNetworkInternal(WifiConfiguration config) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Removing network " + config.getPrintableSsid());
        }
        if (!(isExpRom() || config.isPasspoint() || config.enterpriseConfig == null || config.enterpriseConfig.getEapMethod() == -1)) {
            this.mWifiKeyStore.removeKeys(config.enterpriseConfig);
        }
        removeConnectChoiceFromAllNetworks(config.configKey());
        this.mConfiguredNetworks.remove(config.networkId);
        this.mScanDetailCaches.remove(Integer.valueOf(config.networkId));
        this.mBackupManagerProxy.notifyDataChanged();
        localLog("removeNetworkInternal: removed config. netId=" + config.networkId + " configKey=" + config.configKey());
        return true;
    }

    public boolean removeNetwork(int networkId, int uid) {
        if (doesUidBelongToCurrentUser(uid)) {
            WifiConfiguration config = getInternalConfiguredNetwork(networkId);
            if (config == null) {
                return false;
            }
            if (canModifyNetwork(config, uid, false)) {
                if (OppoManuConnectManager.getInstance() != null) {
                    OppoManuConnectManager.getInstance().handleNetworkDeleted(networkId);
                }
                if (this.mWifiAvailable != null) {
                    this.mWifiAvailable.removeNetworkAvailable(networkId);
                } else if (this.mWifiStateTracker != null) {
                    this.mWifiStateTracker.rmConfUpdateRecord(networkId);
                }
                sendNetworkDeletedEvt(networkId);
                if (removeNetworkInternal(config)) {
                    if (networkId == this.mLastSelectedNetworkId) {
                        clearLastSelectedNetwork();
                    }
                    sendConfiguredNetworkChangedBroadcast(config, 1);
                    if (!(config.ephemeral || (config.isPasspoint() ^ 1) == 0)) {
                        saveToStore(true);
                        if (this.mListener != null) {
                            this.mListener.onSavedNetworkRemoved(networkId);
                        }
                    }
                    return true;
                }
                Log.e(TAG, "Failed to remove network " + config.getPrintableSsid());
                return false;
            }
            Log.e(TAG, "UID " + uid + " does not have permission to delete configuration " + config.configKey());
            return false;
        }
        Log.e(TAG, "UID " + uid + " not visible to the current user");
        return false;
    }

    public Set<Integer> removeNetworksForApp(ApplicationInfo app) {
        int i = 0;
        if (app == null || app.packageName == null) {
            return Collections.emptySet();
        }
        Log.d(TAG, "Remove all networks for app " + app);
        Set<Integer> removedNetworks = new ArraySet();
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        int length = copiedConfigs.length;
        while (i < length) {
            WifiConfiguration config = copiedConfigs[i];
            if (app.uid == config.creatorUid && (app.packageName.equals(config.creatorName) ^ 1) == 0) {
                localLog("Removing network " + config.SSID + ", application \"" + app.packageName + "\" uninstalled" + " from user " + UserHandle.getUserId(app.uid));
                if (removeNetwork(config.networkId, this.mSystemUiUid)) {
                    removedNetworks.add(Integer.valueOf(config.networkId));
                }
            }
            i++;
        }
        return removedNetworks;
    }

    Set<Integer> removeNetworksForUser(int userId) {
        int i = 0;
        Log.d(TAG, "Remove all networks for user " + userId);
        Set<Integer> removedNetworks = new ArraySet();
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        int length = copiedConfigs.length;
        while (i < length) {
            WifiConfiguration config = copiedConfigs[i];
            if (userId == UserHandle.getUserId(config.creatorUid)) {
                localLog("Removing network " + config.SSID + ", user " + userId + " removed");
                if (removeNetwork(config.networkId, this.mSystemUiUid)) {
                    removedNetworks.add(Integer.valueOf(config.networkId));
                }
            }
            i++;
        }
        return removedNetworks;
    }

    public boolean removeAllEphemeralOrPasspointConfiguredNetworks() {
        int i = 0;
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Removing all passpoint or ephemeral configured networks");
        }
        boolean didRemove = false;
        WifiConfiguration[] copiedConfigs = (WifiConfiguration[]) this.mConfiguredNetworks.valuesForAllUsers().toArray(new WifiConfiguration[0]);
        int length = copiedConfigs.length;
        while (i < length) {
            WifiConfiguration config = copiedConfigs[i];
            if (config.isPasspoint()) {
                Log.d(TAG, "Removing passpoint network config " + config.configKey());
                removeNetwork(config.networkId, this.mSystemUiUid);
                didRemove = true;
            } else if (config.ephemeral) {
                Log.d(TAG, "Removing ephemeral network config " + config.configKey());
                removeNetwork(config.networkId, this.mSystemUiUid);
                didRemove = true;
            }
            i++;
        }
        return didRemove;
    }

    private void setNetworkSelectionEnabled(WifiConfiguration config) {
        NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(0);
        status.setDisableTime(-1);
        status.setNetworkSelectionDisableReason(0);
        status.clearDisableReasonCounter();
        if (this.mListener != null) {
            this.mListener.onSavedNetworkEnabled(config.networkId);
        }
    }

    private void setNetworkSelectionTemporarilyDisabled(WifiConfiguration config, int disableReason) {
        NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(1);
        status.setDisableTime(this.mClock.getElapsedSinceBootMillis());
        status.setNetworkSelectionDisableReason(disableReason);
        if (this.mListener != null) {
            this.mListener.onSavedNetworkTemporarilyDisabled(config.networkId);
        }
    }

    private void setNetworkSelectionPermanentlyDisabled(WifiConfiguration config, int disableReason) {
        NetworkSelectionStatus status = config.getNetworkSelectionStatus();
        status.setNetworkSelectionStatus(2);
        status.setDisableTime(-1);
        status.setNetworkSelectionDisableReason(disableReason);
        if (this.mListener != null) {
            this.mListener.onSavedNetworkPermanentlyDisabled(config.networkId);
        }
    }

    private void setNetworkStatus(WifiConfiguration config, int status) {
        config.status = status;
        sendConfiguredNetworkChangedBroadcast(config, 2);
    }

    private boolean setNetworkSelectionStatus(WifiConfiguration config, int reason) {
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason < 0 || reason >= 13) {
            Log.e(TAG, "Invalid Network disable reason " + reason);
            return false;
        }
        if (reason == 0) {
            setNetworkSelectionEnabled(config);
            setNetworkStatus(config, 2);
        } else if (reason < 7) {
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
        if (!(reason == 0 || config.status == 1)) {
            sendUpdateNetworkDisabledCountEvt(config.networkId, reason);
        }
        if (reason < 0 || reason >= 13) {
            if (this.mVerboseLoggingEnabled) {
                localLog("Invalid Network disable reason:" + reason + "return false");
            }
            return false;
        }
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (reason != 0) {
            networkStatus.incrementDisableReasonCounter(reason);
            int disableReasonCounter = networkStatus.getDisableReasonCounter(reason);
            int disableReasonThreshold = NETWORK_SELECTION_DISABLE_THRESHOLD[reason];
            if (disableReasonCounter < disableReasonThreshold) {
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Disable counter for network " + config.getPrintableSsid() + " for reason " + NetworkSelectionStatus.getNetworkDisableReasonString(reason) + " is " + networkStatus.getDisableReasonCounter(reason) + " and threshold is " + disableReasonThreshold);
                }
                return true;
            }
            if (OppoManuConnectManager.getInstance() != null) {
                OppoManuConnectManager.getInstance().handleNetworkDisabled(config.networkId);
            }
            sendNetworkDisabledEvt(config.networkId, reason);
            if (!(this.mWifiStateTracker == null || reason == StatusCode.MCCA_TRACK_LIMIT_EXCEEDED || reason == 5 || reason == 10)) {
                this.mWifiStateTracker.rmOrupdateRecordStatus(config.configKey(false), false);
            }
            if (reason == 2) {
                ScanResult sr = findHomonyAPFromScanResults(config);
                int rssi;
                if (sr == null) {
                    rssi = 0;
                    Log.e(TAG, "chuck writeLogToPartition:TYPE_WIFI_ASSOC_REJECT-can't get rssi");
                } else {
                    rssi = sr.level;
                    Log.e(TAG, "chuck writeLogToPartition:TYPE_WIFI_ASSOC_REJECT rssi = " + rssi);
                }
                if (this.mContext != null) {
                    if (this.mReasonCode == 13 || this.mReasonCode == 14 || this.mReasonCode == 15 || this.mReasonCode == 17 || rssi < this.mScanResultRssiAssocReject) {
                        Log.e(TAG, "chuck writeLogToPartition:not really failed");
                    } else {
                        Log.e(TAG, "chuck writeLogToPartition:TYPE_WIFI_CONNECT_FAILED");
                        OppoManager.writeLogToPartition(OppoManager.TYPE_WIFI_CONNECT_FAILED, null, "CONNECTIVITY", "wifi_connecting_failure", this.mContext.getResources().getString(17041009));
                    }
                }
                this.mReasonCode = 0;
            } else if (reason == 4) {
                if (this.mVerboseLoggingEnabled) {
                    Log.e(TAG, "chuck writeLogToPartition:TYPE_WIFI_CONNECT_FAILED");
                }
                OppoManager.writeLogToPartition(OppoManager.TYPE_WIFI_CONNECT_FAILED, null, "CONNECTIVITY", "wifi_connecting_failure", this.mContext.getResources().getString(17041009));
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
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (networkStatus.isNetworkTemporaryDisabled()) {
            if (this.mClock.getElapsedSinceBootMillis() - networkStatus.getDisableTime() >= ((long) NETWORK_SELECTION_DISABLE_TIMEOUT_MS[networkStatus.getNetworkSelectionDisableReason()])) {
                return updateNetworkSelectionStatus(config, 0);
            }
        } else if (networkStatus.isDisabledByReason(11)) {
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
        } else if (getInternalConfiguredNetwork(networkId) == null || !updateNetworkSelectionStatus(networkId, 0)) {
            return false;
        } else {
            if (disableOthers) {
                setLastSelectedNetwork(networkId);
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
        } else if (getInternalConfiguredNetwork(networkId) == null || !updateNetworkSelectionStatus(networkId, 10)) {
            return false;
        } else {
            if (networkId == this.mLastSelectedNetworkId) {
                clearLastSelectedNetwork();
            }
            saveToStore(true);
            return true;
        }
    }

    public boolean checkAndUpdateLastConnectUid(int networkId, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Update network last connect UID for " + networkId);
        }
        if (doesUidBelongToCurrentUser(uid)) {
            WifiConfiguration config = getInternalConfiguredNetwork(networkId);
            if (config == null) {
                return false;
            }
            if (canModifyNetwork(config, uid, true)) {
                config.lastConnectUid = uid;
                return true;
            }
            Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + config.configKey());
            return false;
        }
        Log.e(TAG, "UID " + uid + " not visible to the current user");
        return false;
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
        if (this.mConfiguredNetworks.sizeForAllUsers() > 100) {
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

    public boolean clearNetworkCandidateScanResult(int networkId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Clear network candidate scan result for " + networkId);
        }
        WifiConfiguration config = getInternalConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        config.getNetworkSelectionStatus().setCandidate(null);
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
        config.getNetworkSelectionStatus().setConnectChoice(null);
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
        if (this.mLastSelectedNetworkId == -1) {
            return "";
        }
        WifiConfiguration config = getInternalConfiguredNetwork(this.mLastSelectedNetworkId);
        if (config == null) {
            return "";
        }
        return config.configKey();
    }

    public long getLastSelectedTimeStamp() {
        return this.mLastSelectedTimeStamp;
    }

    public ScanDetailCache getScanDetailCacheForNetwork(int networkId) {
        return (ScanDetailCache) this.mScanDetailCaches.get(Integer.valueOf(networkId));
    }

    private ScanDetailCache getOrCreateScanDetailCacheForNetwork(WifiConfiguration config) {
        if (config == null) {
            return null;
        }
        ScanDetailCache cache = getScanDetailCacheForNetwork(config.networkId);
        if (cache == null && config.networkId != -1) {
            cache = new ScanDetailCache(config, SCAN_CACHE_ENTRIES_MAX_SIZE, 128);
            this.mScanDetailCaches.put(Integer.valueOf(config.networkId), cache);
        }
        return cache;
    }

    private void saveToScanDetailCacheForNetwork(WifiConfiguration config, ScanDetail scanDetail) {
        ScanResult scanResult = scanDetail.getScanResult();
        ScanDetailCache scanDetailCache = getOrCreateScanDetailCacheForNetwork(config);
        if (scanDetailCache == null) {
            Log.e(TAG, "Could not allocate scan cache for " + config.getPrintableSsid());
            return;
        }
        ScanResult result = scanDetailCache.getScanResult(scanResult.BSSID);
        if (result != null) {
            scanResult.blackListTimestamp = result.blackListTimestamp;
            scanResult.numIpConfigFailures = result.numIpConfigFailures;
            scanResult.numConnection = result.numConnection;
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
        return createExternalWifiConfiguration(network, true);
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
                result.averageRssi(previousRssi, previousSeen, SCAN_RESULT_MAXIMUM_AGE_MS);
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
        if (!this.mOnlyLinkSameCredentialConfigurations || TextUtils.equals(network1.preSharedKey, network2.preSharedKey)) {
            if (network1.defaultGwMacAddress == null || network2.defaultGwMacAddress == null) {
                if (!(scanDetailCache1 == null || scanDetailCache2 == null)) {
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
                }
            } else if (network1.defaultGwMacAddress.equals(network2.defaultGwMacAddress)) {
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "shouldNetworksBeLinked link due to same gw " + network2.SSID + " and " + network1.SSID + " GW " + network1.defaultGwMacAddress);
                }
                return true;
            }
            return false;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "shouldNetworksBeLinked unlink due to password mismatch");
        }
        return false;
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
        network2.linkedConfigurations.put(network1.configKey(), Integer.valueOf(1));
        network1.linkedConfigurations.put(network2.configKey(), Integer.valueOf(1));
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
                    if (!(linkConfig.configKey().equals(config.configKey()) || linkConfig.ephemeral || !linkConfig.allowedKeyManagement.get(1))) {
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
            dbg.append("fetchChannelSetForNetworkForPartialScan ageInMillis ").append(ageInMillis).append(" for ").append(config.configKey()).append(" max ").append(this.mMaxNumActiveChannelsForPartialScans);
            if (scanDetailCache != null) {
                dbg.append(" bssids ").append(scanDetailCache.size());
            }
            if (config.linkedConfigurations != null) {
                dbg.append(" linked ").append(config.linkedConfigurations.size());
            }
            Log.v(TAG, dbg.toString());
        }
        Set<Integer> channelSet = new HashSet();
        if (homeChannelFreq > 0) {
            channelSet.add(Integer.valueOf(homeChannelFreq));
            if (channelSet.size() >= this.mMaxNumActiveChannelsForPartialScans) {
                return channelSet;
            }
        }
        long nowInMillis = this.mClock.getWallClockMillis();
        if (addToChannelSetForNetworkFromScanDetailCache(channelSet, scanDetailCache, nowInMillis, ageInMillis, this.mMaxNumActiveChannelsForPartialScans) && config.linkedConfigurations != null) {
            for (String configKey : config.linkedConfigurations.keySet()) {
                WifiConfiguration linkedConfig = getInternalConfiguredNetwork(configKey);
                if (linkedConfig != null) {
                    if (!addToChannelSetForNetworkFromScanDetailCache(channelSet, getScanDetailCacheForNetwork(linkedConfig.networkId), nowInMillis, ageInMillis, this.mMaxNumActiveChannelsForPartialScans)) {
                        break;
                    }
                }
            }
        }
        return channelSet;
    }

    public List<PnoNetwork> retrievePnoNetworkList() {
        WifiConfiguration config;
        List<PnoNetwork> pnoList = new ArrayList();
        List<WifiConfiguration> networks = new ArrayList(getInternalConfiguredNetworks());
        Iterator<WifiConfiguration> iter = networks.iterator();
        while (iter.hasNext()) {
            config = (WifiConfiguration) iter.next();
            if (config.ephemeral || config.isPasspoint() || config.getNetworkSelectionStatus().isNetworkPermanentlyDisabled() || config.getNetworkSelectionStatus().isNetworkTemporaryDisabled()) {
                iter.remove();
            }
        }
        Collections.sort(networks, sScanListComparator);
        int priority = networks.size() - 1;
        for (WifiConfiguration config2 : networks) {
            pnoList.add(WifiConfigurationUtil.createPnoNetwork(config2, priority));
            priority--;
        }
        return pnoList;
    }

    public List<HiddenNetwork> retrieveHiddenNetworkList() {
        List<HiddenNetwork> hiddenList = new ArrayList();
        List<WifiConfiguration> networks = new ArrayList(getInternalConfiguredNetworks());
        Iterator<WifiConfiguration> iter = networks.iterator();
        while (iter.hasNext()) {
            if (!((WifiConfiguration) iter.next()).hiddenSSID) {
                iter.remove();
            }
        }
        Collections.sort(networks, sScanListComparator);
        int priority = networks.size() - 1;
        int maxSsidCount = 16;
        for (WifiConfiguration config : networks) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "hidden add ssid = " + config.SSID + " numassoc = " + config.numAssociation + " lastconncted = " + config.lastConnected);
            }
            hiddenList.add(new HiddenNetwork(config.SSID));
            priority--;
            maxSsidCount--;
            if (maxSsidCount <= 0) {
                if (this.mVerboseLoggingEnabled) {
                    Log.d(TAG, "discard scan " + ((priority + 1) - maxSsidCount) + " ssids");
                }
                return hiddenList;
            }
        }
        return hiddenList;
    }

    public boolean wasEphemeralNetworkDeleted(String ssid) {
        return this.mDeletedEphemeralSSIDs.contains(ssid);
    }

    public WifiConfiguration disableEphemeralNetwork(String ssid) {
        if (ssid == null) {
            return null;
        }
        WifiConfiguration foundConfig = null;
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (config.ephemeral && TextUtils.equals(config.SSID, ssid)) {
                foundConfig = config;
                break;
            }
        }
        this.mDeletedEphemeralSSIDs.add(ssid);
        Log.d(TAG, "Forget ephemeral SSID " + ssid + " num=" + this.mDeletedEphemeralSSIDs.size());
        if (foundConfig != null) {
            Log.d(TAG, "Found ephemeral config in disableEphemeralNetwork: " + foundConfig.networkId);
        }
        return foundConfig;
    }

    public void resetSimNetworks(boolean simPresent) {
        if (this.mVerboseLoggingEnabled) {
            localLog("resetSimNetworks");
        }
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (TelephonyUtil.isSimConfig(config)) {
                String currentIdentity = null;
                if (simPresent) {
                    currentIdentity = TelephonyUtil.getSimIdentity(this.mTelephonyManager, config);
                }
                config.enterpriseConfig.setIdentity(currentIdentity);
                if (config.enterpriseConfig.getEapMethod() != 0) {
                    config.enterpriseConfig.setAnonymousIdentity("");
                }
            }
        }
        this.mSimPresent = simPresent;
    }

    public boolean isSimPresent() {
        return this.mSimPresent;
    }

    public boolean needsUnlockedKeyStore() {
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (WifiConfigurationUtil.isConfigForEapNetwork(config)) {
                WifiKeyStore wifiKeyStore = this.mWifiKeyStore;
                if (WifiKeyStore.needsSoftwareBackedKeyStore(config.enterpriseConfig)) {
                    return true;
                }
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
        if (userId == this.mCurrentUserId) {
            Log.w(TAG, "User already in foreground " + userId);
            return new HashSet();
        } else if (this.mPendingStoreRead) {
            Log.wtf(TAG, "Unexpected user switch before store is read!");
            return new HashSet();
        } else {
            if (this.mUserManager.isUserUnlockingOrUnlocked(this.mCurrentUserId)) {
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
        if (this.mPendingStoreRead) {
            Log.w(TAG, "Ignore user unlock until store is read!");
            this.mDeferredUserUnlockRead = true;
            return;
        }
        if (userId == this.mCurrentUserId && this.mPendingUnlockStoreRead) {
            handleUserUnlockOrSwitch(this.mCurrentUserId);
        }
    }

    public void handleUserStop(int userId) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Handling user stop for " + userId);
        }
        if (userId == this.mCurrentUserId && this.mUserManager.isUserUnlockingOrUnlocked(this.mCurrentUserId)) {
            saveToStore(true);
            clearInternalUserData(this.mCurrentUserId);
        }
    }

    private void clearInternalData() {
        localLog("clearInternalData: Clearing all internal data");
        this.mConfiguredNetworks.clear();
        this.mDeletedEphemeralSSIDs.clear();
        this.mScanDetailCaches.clear();
        clearLastSelectedNetwork();
    }

    private Set<Integer> clearInternalUserData(int userId) {
        localLog("clearInternalUserData: Clearing user internal data for " + userId);
        Set<Integer> removedNetworkIds = new HashSet();
        for (WifiConfiguration config : getInternalConfiguredNetworks()) {
            if (!config.shared && WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(userId))) {
                removedNetworkIds.add(Integer.valueOf(config.networkId));
                localLog("clearInternalUserData: removed config. netId=" + config.networkId + " configKey=" + config.configKey());
                this.mConfiguredNetworks.remove(config.networkId);
            }
        }
        this.mDeletedEphemeralSSIDs.clear();
        this.mScanDetailCaches.clear();
        clearLastSelectedNetwork();
        return removedNetworkIds;
    }

    private void loadInternalDataFromSharedStore(List<WifiConfiguration> configurations) {
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
    }

    private void loadInternalDataFromUserStore(List<WifiConfiguration> configurations, Set<String> deletedEphemeralSSIDs) {
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
        for (String ssid : deletedEphemeralSSIDs) {
            this.mDeletedEphemeralSSIDs.add(ssid);
        }
    }

    private void loadInternalData(List<WifiConfiguration> sharedConfigurations, List<WifiConfiguration> userConfigurations, Set<String> deletedEphemeralSSIDs) {
        clearInternalData();
        loadInternalDataFromSharedStore(sharedConfigurations);
        loadInternalDataFromUserStore(userConfigurations, deletedEphemeralSSIDs);
        if (this.mConfiguredNetworks.sizeForAllUsers() == 0) {
            Log.w(TAG, "No stored networks found.");
        }
        sendConfiguredNetworksChangedBroadcast();
        this.mPendingStoreRead = false;
    }

    public boolean migrateFromLegacyStore() {
        if (!this.mWifiConfigStoreLegacy.areStoresPresent()) {
            Log.d(TAG, "Legacy store files not found. No migration needed!");
            return true;
        } else if (this.mWifiConfigStore.areStoresPresent()) {
            Log.d(TAG, "New store files found. No migration needed! Remove legacy store files");
            this.mWifiConfigStoreLegacy.removeStores();
            return true;
        } else {
            WifiConfigStoreDataLegacy storeData = this.mWifiConfigStoreLegacy.read();
            Log.d(TAG, "Reading from legacy store completed");
            loadInternalData(storeData.getConfigurations(), new ArrayList(), storeData.getDeletedEphemeralSSIDs());
            if (this.mDeferredUserUnlockRead) {
                this.mWifiConfigStore.setUserStore(WifiConfigStore.createUserFile(this.mCurrentUserId));
                this.mDeferredUserUnlockRead = false;
            }
            if (!saveToStore(true)) {
                return false;
            }
            this.mWifiConfigStoreLegacy.removeStores();
            Log.d(TAG, "Migration from legacy store completed");
            return true;
        }
    }

    public boolean loadFromStore() {
        boolean NetworkRecordPresent = OppoNetworkRecordHelper.isNetworkRecordTxtPresent();
        if (NetworkRecordPresent) {
            this.mNetworkRecordHelper.loadAllNetworkRecords();
        }
        if (this.mWifiConfigStore.areStoresPresent()) {
            if (this.mDeferredUserUnlockRead) {
                Log.i(TAG, "Handling user unlock before loading from store.");
                this.mWifiConfigStore.setUserStore(WifiConfigStore.createUserFile(this.mCurrentUserId));
                this.mDeferredUserUnlockRead = false;
            }
            try {
                this.mWifiConfigStore.read();
                loadInternalData(this.mNetworkListStoreData.getSharedConfigurations(), this.mNetworkListStoreData.getUserConfigurations(), this.mDeletedEphemeralSsidsStoreData.getSsidList());
                if (NetworkRecordPresent) {
                    this.mNetworkRecordHelper.deleteNetworkRecordTxt();
                }
                if (this.mConfiguredNetworks.sizeForAllUsers() > 100) {
                    Log.d(TAG, " clear the obsolete networks from loadFromStore()");
                    this.mNetworkRecordHelper.clearObsoleteNetworks();
                }
                addOrUpdateSingtelAp();
                return true;
            } catch (IOException e) {
                Log.wtf(TAG, "Reading from new store failed. All saved networks are lost!", e);
                loadInternalData(this.mNetworkListStoreData.getSharedConfigurations(), this.mNetworkListStoreData.getUserConfigurations(), this.mDeletedEphemeralSsidsStoreData.getSsidList());
                return false;
            } catch (XmlPullParserException e2) {
                Log.wtf(TAG, "XML deserialization of store failed. All saved networks are lost!", e2);
                loadInternalData(this.mNetworkListStoreData.getSharedConfigurations(), this.mNetworkListStoreData.getUserConfigurations(), this.mDeletedEphemeralSsidsStoreData.getSsidList());
                return false;
            }
        }
        Log.d(TAG, "New store files not found. No saved networks loaded!");
        if (!this.mWifiConfigStoreLegacy.areStoresPresent()) {
            this.mPendingStoreRead = false;
        }
        addOrUpdateSingtelAp();
        return true;
    }

    public boolean loadFromUserStoreAfterUnlockOrSwitch(int userId) {
        try {
            this.mWifiConfigStore.switchUserStoreAndRead(WifiConfigStore.createUserFile(userId));
            loadInternalDataFromUserStore(this.mNetworkListStoreData.getUserConfigurations(), this.mDeletedEphemeralSsidsStoreData.getSsidList());
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
        ArrayList<WifiConfiguration> sharedConfigurations = new ArrayList();
        ArrayList<WifiConfiguration> userConfigurations = new ArrayList();
        List<Integer> legacyPasspointNetId = new ArrayList();
        for (WifiConfiguration config : this.mConfiguredNetworks.valuesForAllUsers()) {
            if (!config.ephemeral && (!config.isPasspoint() || (config.isLegacyPasspointConfig ^ 1) == 0)) {
                if (config.isLegacyPasspointConfig && WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(this.mCurrentUserId))) {
                    legacyPasspointNetId.add(Integer.valueOf(config.networkId));
                    if (!PasspointManager.addLegacyPasspointConfig(config)) {
                        Log.e(TAG, "Failed to migrate legacy Passpoint config: " + config.FQDN);
                    }
                } else if (config.shared || (WifiConfigurationUtil.doesUidBelongToAnyProfile(config.creatorUid, this.mUserManager.getProfiles(this.mCurrentUserId)) ^ 1) != 0) {
                    sharedConfigurations.add(config);
                } else {
                    userConfigurations.add(config);
                }
            }
        }
        for (Integer intValue : legacyPasspointNetId) {
            this.mConfiguredNetworks.remove(intValue.intValue());
        }
        this.mNetworkListStoreData.setSharedConfigurations(sharedConfigurations);
        this.mNetworkListStoreData.setUserConfigurations(userConfigurations);
        this.mDeletedEphemeralSsidsStoreData.setSsidList(this.mDeletedEphemeralSSIDs);
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
        if (this.mLocalLog != null) {
            this.mLocalLog.log(s);
        }
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
    }

    private boolean canModifyProxySettings(int uid) {
        DevicePolicyManagerInternal dpmi = this.mWifiPermissionsWrapper.getDevicePolicyManagerInternal();
        boolean isUidProfileOwner = dpmi != null ? dpmi.isActiveAdminWithPolicy(uid, -1) : false;
        boolean isUidDeviceOwner = dpmi != null ? dpmi.isActiveAdminWithPolicy(uid, -2) : false;
        boolean hasNetworkSettingsPermission = this.mWifiPermissionsUtil.checkNetworkSettingsPermission(uid);
        if (isUidDeviceOwner || isUidProfileOwner || hasNetworkSettingsPermission) {
            return true;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "UID: " + uid + " cannot modify WifiConfiguration proxy settings." + " ConfigOverride=" + hasNetworkSettingsPermission + " DeviceOwner=" + isUidDeviceOwner + " ProfileOwner=" + isUidProfileOwner);
        }
        return false;
    }

    public void setOnSavedNetworkUpdateListener(OnSavedNetworkUpdateListener listener) {
        this.mListener = listener;
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

    boolean disableAndRemoveNetwork(int netId, int uid, int reason) {
        if (OppoManuConnectManager.getInstance() != null) {
            OppoManuConnectManager.getInstance().handleNetworkDisabled(netId);
        }
        sendNetworkDisabledEvt(netId, reason);
        boolean ret = disableNetwork(netId, uid);
        WifiConfiguration network = null;
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (this.mVerboseLoggingEnabled && config != null) {
            Log.e(TAG, "disableAndRemoveNetwork netId=" + Integer.toString(netId) + " SSID=" + config.SSID + " reason=" + Integer.toString(reason));
        }
        if (config != null) {
            config.disableReason = reason;
            network = config;
        }
        if (network != null) {
            sendAlertNetworksChangedBroadcast(netId, network, reason, SupplicantState.DISCONNECTED);
            sendConfiguredNetworkChangedBroadcast(network, 2);
        }
        return ret;
    }

    void sendAlertNetworksChangedBroadcast(int netId, WifiConfiguration network, int reason, SupplicantState state) {
        Intent intent = new Intent(DISABLE_ALERT);
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService("package");
        int disableReasontoWifiSetting = 0;
        if (!(network == null || OppoManuConnectManager.getInstance() == null)) {
            network.BSSID = OppoManuConnectManager.getInstance().getManuConnectBssid();
        }
        if (reason == 3) {
            disableReasontoWifiSetting = 3;
        } else if (reason == 12) {
            disableReasontoWifiSetting = 6;
        } else if (reason == StatusCode.MCCA_TRACK_LIMIT_EXCEEDED) {
            disableReasontoWifiSetting = 0;
        } else if (reason == 2) {
            disableReasontoWifiSetting = 4;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "sendAlertNetworksChangedBroadcast- configssid: " + network.configKey() + ",reason = " + reason + ",disableReasontoWifiSetting = " + disableReasontoWifiSetting + ",state = " + state);
        }
        intent.addFlags(67108864);
        intent.putExtra("wifiConfiguration", network);
        intent.putExtra("changeReason", disableReasontoWifiSetting);
        intent.putExtra("newState", state);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        if (state == SupplicantState.DISCONNECTED && this.mWifiStateMachine.getRomUpdateBooleanValue("CONNECT_ENABLE_REMOVE_NETWORK_WITH_WRONGKEY", ENABLE_REMOVE_NETWORK_WITH_WRONGKEY).booleanValue() && ((reason == 12 || reason == StatusCode.MCCA_TRACK_LIMIT_EXCEEDED) && this.mNewNetwork)) {
            if (this.mWifiStateTracker != null) {
                this.mWifiStateTracker.rmOrupdateRecordStatus(network.configKey(false), true);
            }
            removeNetwork(netId, OppoManuConnectManager.UID_DEFAULT);
            this.mNewNetwork = false;
        } else if (state == SupplicantState.DISCONNECTED && this.mWifiStateTracker != null && reason != 5 && reason != 10) {
            this.mWifiStateTracker.rmOrupdateRecordStatus(network.configKey(false), false);
        }
    }

    void sendAlertNetworksChangedBroadcast(int netId, WifiConfiguration network, int reason) {
        if (network == null) {
            Log.d(TAG, "sendAlertNetworksChangedBroadcast- network is null!");
            return;
        }
        Intent intent = new Intent(DISABLE_ALERT);
        int disableReasontoWifiSetting = 0;
        if (reason == 3) {
            disableReasontoWifiSetting = 3;
        } else if (reason == 12) {
            disableReasontoWifiSetting = 6;
        } else if (reason == StatusCode.MCCA_TRACK_LIMIT_EXCEEDED) {
            disableReasontoWifiSetting = 0;
        } else if (reason == 2) {
            disableReasontoWifiSetting = 4;
        }
        if (network != null && this.mVerboseLoggingEnabled) {
            Log.d(TAG, "sendAlertNetworksChangedBroadcast- configssid: " + network.configKey() + ",reason = " + reason + ",disableReasontoWifiSetting = " + disableReasontoWifiSetting);
        }
        intent.addFlags(67108864);
        intent.putExtra("wifiConfiguration", network);
        intent.putExtra("changeReason", disableReasontoWifiSetting);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    boolean disableNetwork(int netId, int uid, int reason) {
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
            boolean z;
            String str = TAG;
            StringBuilder append = new StringBuilder().append("disableNetwork netId=").append(Integer.toString(netId)).append(" SSID=").append(config.SSID).append(" disabled=");
            if (config.status == 1) {
                z = true;
            } else {
                z = false;
            }
            Log.e(str, append.append(z).append(" reason=").append(Integer.toString(config.disableReason)).toString());
        }
        if (config != null) {
            if (config.status != 1) {
                config.status = 1;
                config.disableReason = reason;
                network = config;
            }
            config.disableReason = reason;
            if (reason == 10) {
                config.status = 1;
            }
        }
        if (network != null) {
            sendConfiguredNetworkChangedBroadcast(network, 2);
            if (!(this.mWifiStateTracker == null || reason == StatusCode.MCCA_TRACK_LIMIT_EXCEEDED || reason == 5 || reason == 10)) {
                this.mWifiStateTracker.rmOrupdateRecordStatus(network.configKey(false), false);
            }
            sendNetworkDisabledEvt(network.networkId, reason);
        } else if (config != null && reason == 9) {
            sendNetworkDisabledEvt(config.networkId, reason);
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

    public void setIsNewNetwork(boolean isNew) {
        this.mNewNetwork = isNew;
    }

    void enableAllNetworks() {
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

    boolean enableNetworkEx(int networkId, boolean disableOthers, int uid, boolean sendBroadcast) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Enabling network " + networkId + " (disableOthers " + disableOthers + ")");
        }
        if (doesUidBelongToCurrentUser(uid)) {
            WifiConfiguration config = getInternalConfiguredNetwork(networkId);
            if (config == null) {
                return false;
            }
            if (canModifyNetwork(config, uid, false)) {
                config.status = 2;
                NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
                if (networkStatus != null) {
                    networkStatus.setNetworkSelectionStatus(0);
                }
                if (this.mWifiStateTracker != null) {
                    this.mWifiStateTracker.resetConnExp(config.configKey(false));
                }
                if (disableOthers) {
                    setLastSelectedNetwork(networkId);
                }
                saveToStore(true);
                return true;
            }
            Log.e(TAG, "UID " + uid + " does not have permission to update configuration " + config.configKey());
            return false;
        }
        Log.e(TAG, "UID " + uid + " not visible to the current user");
        return false;
    }

    boolean disableNetworkEx(int networkId, int reason, boolean sendBroadcast) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Disabling network " + networkId);
        }
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
        if (!(this.mWifiStateTracker == null || reason == StatusCode.MCCA_TRACK_LIMIT_EXCEEDED || reason == 5 || reason == 10)) {
            this.mWifiStateTracker.rmOrupdateRecordStatus(config.configKey(false), false);
        }
        if (networkId == this.mLastSelectedNetworkId) {
            clearLastSelectedNetwork();
        }
        saveToStore(true);
        return true;
    }

    public WifiConfiguration getWifiConfigurationForAll(int netId) {
        return this.mConfiguredNetworks.getForAllUsers(netId);
    }

    public void clearConfiguredNetworkBssid(int netId) {
        getInternalConfiguredNetwork(netId).BSSID = "any";
        saveToStore(true);
    }

    public void clearDisableReasonCounter(int netId) {
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (config != null) {
            NetworkSelectionStatus status = config.getNetworkSelectionStatus();
            if (status != null) {
                try {
                    status.clearDisableReasonCounter();
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "setDisableReasonCounter with invalid reasonCode!");
                }
            }
        }
    }

    public void clearDisableReasonCounter(int reason, int netId) {
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (config != null) {
            NetworkSelectionStatus status = config.getNetworkSelectionStatus();
            if (status != null) {
                try {
                    status.clearDisableReasonCounter(reason);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "clearDisableReasonCounter with invalid reasonCode!");
                }
            }
        }
    }

    public void setDisableReasonCounter(int reason, int value, int netId) {
        WifiConfiguration config = getInternalConfiguredNetwork(netId);
        if (config != null) {
            NetworkSelectionStatus status = config.getNetworkSelectionStatus();
            if (status != null) {
                try {
                    status.setDisableReasonCounter(reason, value);
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "setDisableReasonCounter with invalid reasonCode!");
                }
            }
        }
    }

    private void addOrUpdateSingtelAp() {
        if (OppoAutoConnectManager.getInstance() != null) {
            OppoAutoConnectManager.getInstance().addOrUpdateSingtelAp();
        }
    }

    void setWifiNetwork(OppoWifiNetworkSwitchEnhance wa) {
        this.mWifiAvailable = wa;
    }

    void setWifiNetworkAvailable(OppoWifiAssistantStateTraker wst) {
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
                    Log.d(TAG, "conn-track, found a Homony AP scanResult = " + scanResult);
                    tmpResult = scanResult;
                } else {
                    Log.d(TAG, "conn-track, ESS found more Homony AP scanResult = " + scanResult);
                    if (scanResult.level > tmpResult.level) {
                        tmpResult = scanResult;
                    }
                }
            }
        }
        Log.d(TAG, "conn-track, tmpResult = " + tmpResult);
        return tmpResult;
    }

    public boolean removeNetworkWithoutBroadcast(int netId) {
        if (removeConfigWithoutBroadcast(netId)) {
            return true;
        }
        return false;
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
}
