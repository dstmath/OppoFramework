package android.net.wifi;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.net.IpConfiguration;
import android.net.MacAddress;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.security.keystore.KeyProperties;
import android.telecom.Logging.Session;
import android.text.TextUtils;
import android.util.BackupUtils;
import android.util.Log;
import android.util.TimeUtils;
import com.android.internal.content.NativeLibraryHelper;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;

@Deprecated
public class WifiConfiguration implements Parcelable {
    public static final int AP_BAND_2GHZ = 0;
    public static final int AP_BAND_5GHZ = 1;
    public static final int AP_BAND_ANY = -1;
    private static final int BACKUP_VERSION = 3;
    @UnsupportedAppUsage
    public static final Parcelable.Creator<WifiConfiguration> CREATOR = new Parcelable.Creator<WifiConfiguration>() {
        /* class android.net.wifi.WifiConfiguration.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WifiConfiguration createFromParcel(Parcel in) {
            WifiConfiguration config = new WifiConfiguration();
            config.networkId = in.readInt();
            config.status = in.readInt();
            config.mNetworkSelectionStatus.readFromParcel(in);
            config.SSID = in.readString();
            config.BSSID = in.readString();
            config.apBand = in.readInt();
            config.apChannel = in.readInt();
            config.FQDN = in.readString();
            config.providerFriendlyName = in.readString();
            boolean z = false;
            config.isHomeProviderNetwork = in.readInt() != 0;
            int numRoamingConsortiumIds = in.readInt();
            config.roamingConsortiumIds = new long[numRoamingConsortiumIds];
            for (int i = 0; i < numRoamingConsortiumIds; i++) {
                config.roamingConsortiumIds[i] = in.readLong();
            }
            config.preSharedKey = in.readString();
            for (int i2 = 0; i2 < config.wepKeys.length; i2++) {
                config.wepKeys[i2] = in.readString();
            }
            config.wepTxKeyIndex = in.readInt();
            config.priority = in.readInt();
            config.hiddenSSID = in.readInt() != 0;
            config.requirePMF = in.readInt() != 0;
            config.updateIdentifier = in.readString();
            config.allowedKeyManagement = WifiConfiguration.readBitSet(in);
            config.allowedProtocols = WifiConfiguration.readBitSet(in);
            config.allowedAuthAlgorithms = WifiConfiguration.readBitSet(in);
            config.allowedPairwiseCiphers = WifiConfiguration.readBitSet(in);
            config.allowedGroupCiphers = WifiConfiguration.readBitSet(in);
            config.allowedGroupManagementCiphers = WifiConfiguration.readBitSet(in);
            config.allowedSuiteBCiphers = WifiConfiguration.readBitSet(in);
            config.enterpriseConfig = (WifiEnterpriseConfig) in.readParcelable(null);
            config.setIpConfiguration((IpConfiguration) in.readParcelable(null));
            config.dhcpServer = in.readString();
            config.defaultGwMacAddress = in.readString();
            config.selfAdded = in.readInt() != 0;
            config.didSelfAdd = in.readInt() != 0;
            config.validatedInternetAccess = in.readInt() != 0;
            config.isLegacyPasspointConfig = in.readInt() != 0;
            config.ephemeral = in.readInt() != 0;
            config.trusted = in.readInt() != 0;
            config.fromWifiNetworkSuggestion = in.readInt() != 0;
            config.fromWifiNetworkSpecifier = in.readInt() != 0;
            config.meteredHint = in.readInt() != 0;
            config.meteredOverride = in.readInt();
            config.useExternalScores = in.readInt() != 0;
            config.creatorUid = in.readInt();
            config.lastConnectUid = in.readInt();
            config.lastUpdateUid = in.readInt();
            config.creatorName = in.readString();
            config.lastUpdateName = in.readString();
            config.numScorerOverride = in.readInt();
            config.numScorerOverrideAndSwitchedNetwork = in.readInt();
            config.numAssociation = in.readInt();
            config.userApproved = in.readInt();
            config.numNoInternetAccessReports = in.readInt();
            config.noInternetAccessExpected = in.readInt() != 0;
            config.shared = in.readInt() != 0;
            config.mPasspointManagementObjectTree = in.readString();
            config.recentFailure.setAssociationStatus(in.readInt());
            config.mRandomizedMacAddress = (MacAddress) in.readParcelable(null);
            config.macRandomizationSetting = in.readInt();
            if (in.readInt() != 0) {
                z = true;
            }
            config.osu = z;
            config.wapiCertSelMode = in.readInt();
            config.wapiCertSel = in.readString();
            config.wapiPskType = in.readInt();
            config.wapiPsk = in.readString();
            config.lastConnected = in.readLong();
            config.globalId = in.readString();
            return config;
        }

        @Override // android.os.Parcelable.Creator
        public WifiConfiguration[] newArray(int size) {
            return new WifiConfiguration[size];
        }
    };
    public static final int HOME_NETWORK_RSSI_BOOST = 5;
    public static final int INVALID_NETWORK_ID = -1;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static int INVALID_RSSI = -127;
    public static final int LOCAL_ONLY_NETWORK_ID = -2;
    private static final int MAXIMUM_RANDOM_MAC_GENERATION_RETRY = 3;
    public static final int METERED_OVERRIDE_METERED = 1;
    public static final int METERED_OVERRIDE_NONE = 0;
    public static final int METERED_OVERRIDE_NOT_METERED = 2;
    public static final int RANDOMIZATION_NONE = 0;
    public static final int RANDOMIZATION_PERSISTENT = 1;
    public static final int SECURITY_TYPE_EAP = 3;
    public static final int SECURITY_TYPE_EAP_SUITE_B = 5;
    public static final int SECURITY_TYPE_OPEN = 0;
    public static final int SECURITY_TYPE_OWE = 6;
    public static final int SECURITY_TYPE_PSK = 2;
    public static final int SECURITY_TYPE_SAE = 4;
    public static final int SECURITY_TYPE_WAPI = 7;
    public static final int SECURITY_TYPE_WEP = 1;
    public static final String[] SecurityLevelString = {KeyProperties.DIGEST_NONE, "WEP", "WPA", "EAP"};
    private static final String TAG = "WifiConfiguration";
    public static final int UNKNOWN_UID = -1;
    public static final int USER_APPROVED = 1;
    public static final int USER_BANNED = 2;
    public static final int USER_DISABLE_AUTO_CONNECT = 1001;
    public static final int USER_ENABLE_AUTO_CONNECT = 1002;
    public static final int USER_OPPO_BASE = 1000;
    public static final int USER_PENDING = 3;
    public static final int USER_UNSPECIFIED = 0;
    public static final String bssidVarName = "bssid";
    public static final String hiddenSSIDVarName = "scan_ssid";
    public static final String pmfVarName = "ieee80211w";
    public static final String priorityVarName = "priority";
    public static final String pskVarName = "psk";
    public static final String ssidVarName = "ssid";
    public static final String updateIdentiferVarName = "update_identifier";
    @UnsupportedAppUsage
    @Deprecated
    public static final String[] wepKeyVarNames = {"wep_key0", "wep_key1", "wep_key2", "wep_key3"};
    @Deprecated
    public static final String wepTxKeyIdxVarName = "wep_tx_keyidx";
    public String BSSID;
    public String FQDN;
    public String SSID;
    public BitSet allowedAuthAlgorithms;
    public BitSet allowedGroupCiphers;
    public BitSet allowedGroupManagementCiphers;
    public BitSet allowedKeyManagement;
    public BitSet allowedPairwiseCiphers;
    public BitSet allowedProtocols;
    public BitSet allowedSuiteBCiphers;
    @UnsupportedAppUsage
    public int apBand = 0;
    @UnsupportedAppUsage
    public int apChannel = 0;
    public int apSecondaryChannel = 0;
    public String creationTime;
    @SystemApi
    public String creatorName;
    @SystemApi
    public int creatorUid;
    @UnsupportedAppUsage
    public String defaultGwMacAddress;
    public String dhcpServer;
    public boolean didSelfAdd;
    public int disableReason;
    public int dtimInterval = 0;
    public WifiEnterpriseConfig enterpriseConfig;
    public boolean ephemeral;
    public boolean everLogin;
    public boolean fromWifiNetworkSpecifier;
    public boolean fromWifiNetworkSuggestion;
    public String globalId;
    public boolean hiddenSSID;
    public boolean isHomeProviderNetwork;
    public boolean isLegacyPasspointConfig = false;
    @UnsupportedAppUsage
    public int lastConnectUid;
    public long lastConnected;
    public long lastDisconnected;
    @SystemApi
    public String lastUpdateName;
    @SystemApi
    public int lastUpdateUid;
    public HashMap<String, Integer> linkedConfigurations;
    String mCachedConfigKey;
    @UnsupportedAppUsage
    private IpConfiguration mIpConfiguration;
    private NetworkSelectionStatus mNetworkSelectionStatus;
    private String mPasspointManagementObjectTree;
    private MacAddress mRandomizedMacAddress;
    public int macRandomizationSetting;
    @SystemApi
    public boolean meteredHint;
    public int meteredOverride = 0;
    public int networkId;
    @UnsupportedAppUsage
    public boolean noInternetAccessExpected;
    @SystemApi
    public int numAssociation;
    @UnsupportedAppUsage
    public int numNoInternetAccessReports;
    @SystemApi
    public int numScorerOverride;
    @SystemApi
    public int numScorerOverrideAndSwitchedNetwork;
    public boolean osu;
    public String peerWifiConfiguration;
    public String preSharedKey;
    @Deprecated
    public int priority;
    public String providerFriendlyName;
    public final RecentFailure recentFailure;
    public boolean requirePMF;
    public long[] roamingConsortiumIds;
    @UnsupportedAppUsage
    public boolean selfAdded;
    @UnsupportedAppUsage
    public boolean shared;
    public int status;
    public boolean trusted;
    public String updateIdentifier;
    public String updateTime;
    @SystemApi
    public boolean useExternalScores;
    public int userApproved = 0;
    @UnsupportedAppUsage
    public boolean validatedInternetAccess;
    public String wapiCertSel;
    public int wapiCertSelMode;
    public String wapiPsk;
    public int wapiPskType;
    @Deprecated
    public String[] wepKeys;
    @Deprecated
    public int wepTxKeyIndex;

    public enum SecurityLevel {
        OPEN,
        WEP,
        WPA,
        EAP
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SecurityType {
    }

    public static class KeyMgmt {
        public static final int FT_EAP = 7;
        public static final int FT_PSK = 6;
        public static final int IEEE8021X = 3;
        public static final int NONE = 0;
        public static final int OSEN = 5;
        public static final int OWE = 9;
        public static final int SAE = 8;
        public static final int SUITE_B_192 = 10;
        public static final int WAPI_CERT = 14;
        public static final int WAPI_PSK = 13;
        @SystemApi
        public static final int WPA2_PSK = 4;
        public static final int WPA_EAP = 2;
        public static final int WPA_EAP_SHA256 = 12;
        public static final int WPA_PSK = 1;
        public static final int WPA_PSK_SHA256 = 11;
        public static final String[] strings = {KeyProperties.DIGEST_NONE, "WPA_PSK", "WPA_EAP", "IEEE8021X", "WPA2_PSK", "OSEN", "FT_PSK", "FT_EAP", "SAE", "OWE", "SUITE_B_192", "WPA_PSK_SHA256", "WPA_EAP_SHA256", "WAPI_PSK", "WAPI_CERT"};
        public static final String varName = "key_mgmt";

        private KeyMgmt() {
        }

        public static int getWapiPSK() {
            return 13;
        }

        public static int getWapiCERT() {
            return 14;
        }
    }

    public static class Protocol {
        public static final int OSEN = 2;
        public static final int RSN = 1;
        public static final int WAPI = 3;
        @Deprecated
        public static final int WPA = 0;
        public static final String[] strings = {"WPA", "RSN", "OSEN", "WAPI"};
        public static final String varName = "proto";

        private Protocol() {
        }
    }

    public static class AuthAlgorithm {
        public static final int LEAP = 2;
        public static final int OPEN = 0;
        @Deprecated
        public static final int SHARED = 1;
        public static final String[] strings = {"OPEN", "SHARED", "LEAP"};
        public static final String varName = "auth_alg";

        private AuthAlgorithm() {
        }
    }

    public static class PairwiseCipher {
        public static final int CCMP = 2;
        public static final int GCMP_256 = 3;
        public static final int NONE = 0;
        @Deprecated
        public static final int TKIP = 1;
        public static final String[] strings = {KeyProperties.DIGEST_NONE, "TKIP", "CCMP", "GCMP_256"};
        public static final String varName = "pairwise";

        private PairwiseCipher() {
        }
    }

    public static class GroupCipher {
        public static final int CCMP = 3;
        public static final int GCMP_256 = 5;
        public static final int GTK_NOT_USED = 4;
        public static final int TKIP = 2;
        @Deprecated
        public static final int WEP104 = 1;
        @Deprecated
        public static final int WEP40 = 0;
        public static final String[] strings = {"WEP40", "WEP104", "TKIP", "CCMP", "GTK_NOT_USED", "GCMP_256"};
        public static final String varName = "group";

        private GroupCipher() {
        }
    }

    public static class GroupMgmtCipher {
        public static final int BIP_CMAC_256 = 0;
        public static final int BIP_GMAC_128 = 1;
        public static final int BIP_GMAC_256 = 2;
        private static final String[] strings = {"BIP_CMAC_256", "BIP_GMAC_128", "BIP_GMAC_256"};
        private static final String varName = "groupMgmt";

        private GroupMgmtCipher() {
        }
    }

    public static class SuiteBCipher {
        public static final int ECDHE_ECDSA = 0;
        public static final int ECDHE_RSA = 1;
        private static final String[] strings = {"ECDHE_ECDSA", "ECDHE_RSA"};
        private static final String varName = "SuiteB";

        private SuiteBCipher() {
        }
    }

    public static class Status {
        public static final int CURRENT = 0;
        public static final int DISABLED = 1;
        public static final int ENABLED = 2;
        public static final String[] strings = {"current", "disabled", "enabled"};

        private Status() {
        }
    }

    public void setSecurityParams(int securityType) {
        this.allowedKeyManagement.clear();
        this.allowedProtocols.clear();
        this.allowedAuthAlgorithms.clear();
        this.allowedPairwiseCiphers.clear();
        this.allowedGroupCiphers.clear();
        this.allowedGroupManagementCiphers.clear();
        this.allowedSuiteBCiphers.clear();
        switch (securityType) {
            case 0:
                this.allowedKeyManagement.set(0);
                return;
            case 1:
                this.allowedKeyManagement.set(0);
                this.allowedAuthAlgorithms.set(0);
                this.allowedAuthAlgorithms.set(1);
                return;
            case 2:
                this.allowedKeyManagement.set(1);
                return;
            case 3:
                this.allowedKeyManagement.set(2);
                this.allowedKeyManagement.set(3);
                return;
            case 4:
                this.allowedKeyManagement.set(8);
                this.requirePMF = true;
                return;
            case 5:
                this.allowedKeyManagement.set(10);
                this.allowedGroupCiphers.set(5);
                this.allowedGroupManagementCiphers.set(2);
                this.requirePMF = true;
                return;
            case 6:
                this.allowedKeyManagement.set(9);
                this.requirePMF = true;
                return;
            default:
                throw new IllegalArgumentException("unknown security type " + securityType);
        }
    }

    @SystemApi
    public boolean hasNoInternetAccess() {
        return this.numNoInternetAccessReports > 0 && !this.validatedInternetAccess;
    }

    @SystemApi
    public boolean isNoInternetAccessExpected() {
        return this.noInternetAccessExpected;
    }

    @SystemApi
    public boolean isEphemeral() {
        return this.ephemeral;
    }

    public static boolean isMetered(WifiConfiguration config, WifiInfo info) {
        boolean metered = false;
        if (info != null && info.getMeteredHint()) {
            metered = true;
        }
        if (config != null && config.meteredHint) {
            metered = true;
        }
        if (config != null && config.meteredOverride == 1) {
            metered = true;
        }
        if (config == null || config.meteredOverride != 2) {
            return metered;
        }
        return false;
    }

    public boolean isOpenNetwork() {
        int cardinality = this.allowedKeyManagement.cardinality();
        boolean hasNoKeyMgmt = cardinality == 0 || (cardinality == 1 && (this.allowedKeyManagement.get(0) || this.allowedKeyManagement.get(9)));
        boolean hasNoWepKeys = true;
        if (this.wepKeys != null) {
            int i = 0;
            while (true) {
                String[] strArr = this.wepKeys;
                if (i >= strArr.length) {
                    break;
                } else if (strArr[i] != null) {
                    hasNoWepKeys = false;
                    break;
                } else {
                    i++;
                }
            }
        }
        return hasNoKeyMgmt && hasNoWepKeys;
    }

    private static boolean isNotChineseOperator() {
        String mcc = SystemProperties.get("android.telephony.mcc_change", "");
        String mcc2 = SystemProperties.get("android.telephony.mcc_change2", "");
        if (TextUtils.isEmpty(mcc) && TextUtils.isEmpty(mcc2)) {
            return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
        }
        if ("460".equals(mcc) || "460".equals(mcc2)) {
            return false;
        }
        return true;
    }

    public static boolean isValidMacAddressForRandomization(MacAddress mac) {
        return mac != null && !mac.isMulticastAddress() && mac.isLocallyAssigned() && !MacAddress.fromString("02:00:00:00:00:00").equals(mac);
    }

    public MacAddress getOrCreateRandomizedMacAddress() {
        int randomMacGenerationCount = 0;
        while (!isValidMacAddressForRandomization(this.mRandomizedMacAddress) && randomMacGenerationCount < 3) {
            this.mRandomizedMacAddress = MacAddress.createRandomUnicastAddress();
            randomMacGenerationCount++;
        }
        if (!isValidMacAddressForRandomization(this.mRandomizedMacAddress)) {
            this.mRandomizedMacAddress = MacAddress.fromString("02:00:00:00:00:00");
        }
        return this.mRandomizedMacAddress;
    }

    public MacAddress getRandomizedMacAddress() {
        return this.mRandomizedMacAddress;
    }

    public void setRandomizedMacAddress(MacAddress mac) {
        if (mac == null) {
            Log.e(TAG, "setRandomizedMacAddress received null MacAddress.");
        } else {
            this.mRandomizedMacAddress = mac;
        }
    }

    public static class NetworkSelectionStatus {
        private static final int CONNECT_CHOICE_EXISTS = 1;
        private static final int CONNECT_CHOICE_NOT_EXISTS = -1;
        public static final int DISABLED_ASSOCIATION_REJECT = 2;
        public static final int DISABLED_ASSOCIATION_REJECTION = 2;
        public static final int DISABLED_AUTHENTICATION_FAILURE = 3;
        public static final int DISABLED_AUTHENTICATION_NO_CREDENTIALS = 9;
        public static final int DISABLED_AUTHENTICATION_NO_SUBSCRIPTION = 14;
        public static final int DISABLED_AUTH_FAILURE = 3;
        public static final int DISABLED_BAD_LINK = 1;
        public static final int DISABLED_BY_WIFI_MANAGER = 11;
        public static final int DISABLED_BY_WRONG_PASSWORD = 13;
        public static final int DISABLED_DHCP_FAILURE = 4;
        public static final int DISABLED_DNS_FAILURE = 5;
        public static final int DISABLED_DUE_TO_USER_SWITCH = 12;
        public static final int DISABLED_NO_INTERNET_PERMANENT = 10;
        public static final int DISABLED_NO_INTERNET_TEMPORARY = 6;
        public static final int DISABLED_TLS_VERSION_MISMATCH = 8;
        public static final int DISABLED_UNKNOWN_REASON = 102;
        public static final int DISABLED_WPS_START = 7;
        public static final int DISABLED_WRONG_KEY = 13;
        public static final long INVALID_NETWORK_SELECTION_DISABLE_TIMESTAMP = -1;
        public static final int NETWORK_SELECTION_DISABLED_MAX = 15;
        public static final int NETWORK_SELECTION_DISABLED_STARTING_INDEX = 1;
        public static final int NETWORK_SELECTION_ENABLE = 0;
        public static final int NETWORK_SELECTION_ENABLED = 0;
        public static final int NETWORK_SELECTION_PERMANENTLY_DISABLED = 2;
        public static final int NETWORK_SELECTION_STATUS_MAX = 3;
        public static final int NETWORK_SELECTION_TEMPORARY_DISABLED = 1;
        public static final String[] QUALITY_NETWORK_SELECTION_DISABLE_REASON = {"NETWORK_SELECTION_ENABLE", "NETWORK_SELECTION_DISABLED_BAD_LINK", "NETWORK_SELECTION_DISABLED_ASSOCIATION_REJECTION ", "NETWORK_SELECTION_DISABLED_AUTHENTICATION_FAILURE", "NETWORK_SELECTION_DISABLED_DHCP_FAILURE", "NETWORK_SELECTION_DISABLED_DNS_FAILURE", "NETWORK_SELECTION_DISABLED_NO_INTERNET_TEMPORARY", "NETWORK_SELECTION_DISABLED_WPS_START", "NETWORK_SELECTION_DISABLED_TLS_VERSION", "NETWORK_SELECTION_DISABLED_AUTHENTICATION_NO_CREDENTIALS", "NETWORK_SELECTION_DISABLED_NO_INTERNET_PERMANENT", "NETWORK_SELECTION_DISABLED_BY_WIFI_MANAGER", "NETWORK_SELECTION_DISABLED_BY_USER_SWITCH", "NETWORK_SELECTION_DISABLED_BY_WRONG_PASSWORD", "NETWORK_SELECTION_DISABLED_AUTHENTICATION_NO_SUBSCRIPTION"};
        public static final String[] QUALITY_NETWORK_SELECTION_STATUS = {"NETWORK_SELECTION_ENABLED", "NETWORK_SELECTION_TEMPORARY_DISABLED", "NETWORK_SELECTION_PERMANENTLY_DISABLED"};
        private ScanResult mCandidate;
        private int mCandidateScore;
        private String mConnectChoice;
        private long mConnectChoiceTimestamp = -1;
        private boolean mHasEverConnected = false;
        private int[] mNetworkSeclectionDisableCounter = new int[15];
        private String mNetworkSelectionBSSID;
        private int mNetworkSelectionDisableReason;
        private boolean mNotRecommended;
        private boolean mSeenInLastQualifiedNetworkSelection;
        private int mStatus;
        private long mTemporarilyDisabledTimestamp = -1;

        public void setNotRecommended(boolean notRecommended) {
            this.mNotRecommended = notRecommended;
        }

        public boolean isNotRecommended() {
            return this.mNotRecommended;
        }

        public void setSeenInLastQualifiedNetworkSelection(boolean seen) {
            this.mSeenInLastQualifiedNetworkSelection = seen;
        }

        public boolean getSeenInLastQualifiedNetworkSelection() {
            return this.mSeenInLastQualifiedNetworkSelection;
        }

        public void setCandidate(ScanResult scanCandidate) {
            this.mCandidate = scanCandidate;
        }

        public ScanResult getCandidate() {
            return this.mCandidate;
        }

        public void setCandidateScore(int score) {
            this.mCandidateScore = score;
        }

        public int getCandidateScore() {
            return this.mCandidateScore;
        }

        public String getConnectChoice() {
            return this.mConnectChoice;
        }

        public void setConnectChoice(String newConnectChoice) {
            this.mConnectChoice = newConnectChoice;
        }

        public long getConnectChoiceTimestamp() {
            return this.mConnectChoiceTimestamp;
        }

        public void setConnectChoiceTimestamp(long timeStamp) {
            this.mConnectChoiceTimestamp = timeStamp;
        }

        public String getNetworkStatusString() {
            return QUALITY_NETWORK_SELECTION_STATUS[this.mStatus];
        }

        public void setHasEverConnected(boolean value) {
            this.mHasEverConnected = value;
        }

        public boolean getHasEverConnected() {
            return this.mHasEverConnected;
        }

        public static String getNetworkDisableReasonString(int reason) {
            if (reason < 0 || reason >= 15) {
                return null;
            }
            return QUALITY_NETWORK_SELECTION_DISABLE_REASON[reason];
        }

        public String getNetworkDisableReasonString() {
            return QUALITY_NETWORK_SELECTION_DISABLE_REASON[this.mNetworkSelectionDisableReason];
        }

        public int getNetworkSelectionStatus() {
            return this.mStatus;
        }

        public boolean isNetworkEnabled() {
            return this.mStatus == 0;
        }

        public boolean isNetworkTemporaryDisabled() {
            return this.mStatus == 1;
        }

        public boolean isNetworkPermanentlyDisabled() {
            return this.mStatus == 2;
        }

        public void setNetworkSelectionStatus(int status) {
            if (status >= 0 && status < 3) {
                this.mStatus = status;
            }
        }

        public int getNetworkSelectionDisableReason() {
            return this.mNetworkSelectionDisableReason;
        }

        public void setNetworkSelectionDisableReason(int reason) {
            if (reason < 0 || reason >= 15) {
                throw new IllegalArgumentException("Illegal reason value: " + reason);
            }
            this.mNetworkSelectionDisableReason = reason;
        }

        public boolean isDisabledByReason(int reason) {
            return this.mNetworkSelectionDisableReason == reason;
        }

        public void setDisableTime(long timeStamp) {
            this.mTemporarilyDisabledTimestamp = timeStamp;
        }

        public long getDisableTime() {
            return this.mTemporarilyDisabledTimestamp;
        }

        public int getDisableReasonCounter(int reason) {
            if (reason >= 0 && reason < 15) {
                return this.mNetworkSeclectionDisableCounter[reason];
            }
            throw new IllegalArgumentException("Illegal reason value: " + reason);
        }

        public void setDisableReasonCounter(int reason, int value) {
            if (reason < 0 || reason >= 15) {
                throw new IllegalArgumentException("Illegal reason value: " + reason);
            }
            this.mNetworkSeclectionDisableCounter[reason] = value;
        }

        public void incrementDisableReasonCounter(int reason) {
            if (reason < 0 || reason >= 15) {
                throw new IllegalArgumentException("Illegal reason value: " + reason);
            }
            int[] iArr = this.mNetworkSeclectionDisableCounter;
            iArr[reason] = iArr[reason] + 1;
        }

        public void clearDisableReasonCounter(int reason) {
            if (reason < 0 || reason >= 15) {
                throw new IllegalArgumentException("Illegal reason value: " + reason);
            }
            this.mNetworkSeclectionDisableCounter[reason] = 0;
        }

        public void clearDisableReasonCounter() {
            Arrays.fill(this.mNetworkSeclectionDisableCounter, 0);
        }

        public String getNetworkSelectionBSSID() {
            return this.mNetworkSelectionBSSID;
        }

        public void setNetworkSelectionBSSID(String bssid) {
            this.mNetworkSelectionBSSID = bssid;
        }

        public void copy(NetworkSelectionStatus source) {
            this.mStatus = source.mStatus;
            this.mNetworkSelectionDisableReason = source.mNetworkSelectionDisableReason;
            for (int index = 0; index < 15; index++) {
                this.mNetworkSeclectionDisableCounter[index] = source.mNetworkSeclectionDisableCounter[index];
            }
            this.mTemporarilyDisabledTimestamp = source.mTemporarilyDisabledTimestamp;
            this.mNetworkSelectionBSSID = source.mNetworkSelectionBSSID;
            setSeenInLastQualifiedNetworkSelection(source.getSeenInLastQualifiedNetworkSelection());
            setCandidate(source.getCandidate());
            setCandidateScore(source.getCandidateScore());
            setConnectChoice(source.getConnectChoice());
            setConnectChoiceTimestamp(source.getConnectChoiceTimestamp());
            setHasEverConnected(source.getHasEverConnected());
            setNotRecommended(source.isNotRecommended());
        }

        public void writeToParcel(Parcel dest) {
            dest.writeInt(getNetworkSelectionStatus());
            dest.writeInt(getNetworkSelectionDisableReason());
            for (int index = 0; index < 15; index++) {
                dest.writeInt(getDisableReasonCounter(index));
            }
            dest.writeLong(getDisableTime());
            dest.writeString(getNetworkSelectionBSSID());
            if (getConnectChoice() != null) {
                dest.writeInt(1);
                dest.writeString(getConnectChoice());
                dest.writeLong(getConnectChoiceTimestamp());
            } else {
                dest.writeInt(-1);
            }
            dest.writeInt(getHasEverConnected() ? 1 : 0);
            dest.writeInt(isNotRecommended() ? 1 : 0);
        }

        public void readFromParcel(Parcel in) {
            setNetworkSelectionStatus(in.readInt());
            setNetworkSelectionDisableReason(in.readInt());
            for (int index = 0; index < 15; index++) {
                setDisableReasonCounter(index, in.readInt());
            }
            setDisableTime(in.readLong());
            setNetworkSelectionBSSID(in.readString());
            boolean z = true;
            if (in.readInt() == 1) {
                setConnectChoice(in.readString());
                setConnectChoiceTimestamp(in.readLong());
            } else {
                setConnectChoice(null);
                setConnectChoiceTimestamp(-1);
            }
            setHasEverConnected(in.readInt() != 0);
            if (in.readInt() == 0) {
                z = false;
            }
            setNotRecommended(z);
        }
    }

    public static class RecentFailure {
        public static final int NONE = 0;
        public static final int STATUS_AP_UNABLE_TO_HANDLE_NEW_STA = 17;
        private int mAssociationStatus = 0;

        public void setAssociationStatus(int status) {
            this.mAssociationStatus = status;
        }

        public void clear() {
            this.mAssociationStatus = 0;
        }

        public int getAssociationStatus() {
            return this.mAssociationStatus;
        }
    }

    public NetworkSelectionStatus getNetworkSelectionStatus() {
        return this.mNetworkSelectionStatus;
    }

    public void setNetworkSelectionStatus(NetworkSelectionStatus status2) {
        this.mNetworkSelectionStatus = status2;
    }

    public WifiConfiguration() {
        int i;
        if (isNotChineseOperator()) {
            i = 1;
        } else {
            i = SystemProperties.getInt("persist.sys.wifi.mac_randomization", 0);
        }
        this.macRandomizationSetting = i;
        this.mNetworkSelectionStatus = new NetworkSelectionStatus();
        this.recentFailure = new RecentFailure();
        this.networkId = -1;
        this.SSID = null;
        this.BSSID = null;
        this.FQDN = null;
        this.roamingConsortiumIds = new long[0];
        this.priority = 0;
        this.hiddenSSID = false;
        this.allowedKeyManagement = new BitSet();
        this.allowedProtocols = new BitSet();
        this.allowedAuthAlgorithms = new BitSet();
        this.allowedPairwiseCiphers = new BitSet();
        this.allowedGroupCiphers = new BitSet();
        this.allowedGroupManagementCiphers = new BitSet();
        this.allowedSuiteBCiphers = new BitSet();
        this.wepKeys = new String[4];
        int i2 = 0;
        while (true) {
            String[] strArr = this.wepKeys;
            if (i2 < strArr.length) {
                strArr[i2] = null;
                i2++;
            } else {
                this.enterpriseConfig = new WifiEnterpriseConfig();
                this.selfAdded = false;
                this.didSelfAdd = false;
                this.ephemeral = false;
                this.osu = false;
                this.trusted = true;
                this.fromWifiNetworkSuggestion = false;
                this.fromWifiNetworkSpecifier = false;
                this.meteredHint = false;
                this.meteredOverride = 0;
                this.useExternalScores = false;
                this.validatedInternetAccess = false;
                this.mIpConfiguration = new IpConfiguration();
                this.lastUpdateUid = -1;
                this.creatorUid = -1;
                this.shared = true;
                this.dtimInterval = 0;
                this.mRandomizedMacAddress = MacAddress.fromString("02:00:00:00:00:00");
                this.lastConnected = 0;
                this.globalId = WifiEnterpriseConfig.EMPTY_VALUE;
                this.everLogin = false;
                return;
            }
        }
    }

    public boolean isPasspoint() {
        WifiEnterpriseConfig wifiEnterpriseConfig;
        return !TextUtils.isEmpty(this.FQDN) && !TextUtils.isEmpty(this.providerFriendlyName) && (wifiEnterpriseConfig = this.enterpriseConfig) != null && wifiEnterpriseConfig.getEapMethod() != -1;
    }

    public boolean isLinked(WifiConfiguration config) {
        HashMap<String, Integer> hashMap;
        if (config == null || (hashMap = config.linkedConfigurations) == null || this.linkedConfigurations == null || hashMap.get(configKey()) == null || this.linkedConfigurations.get(config.configKey()) == null) {
            return false;
        }
        return true;
    }

    @UnsupportedAppUsage
    public boolean isEnterprise() {
        WifiEnterpriseConfig wifiEnterpriseConfig;
        return ((!this.allowedKeyManagement.get(2) && !this.allowedKeyManagement.get(3) && !this.allowedKeyManagement.get(10)) || (wifiEnterpriseConfig = this.enterpriseConfig) == null || wifiEnterpriseConfig.getEapMethod() == -1) ? false : true;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        int i = this.status;
        if (i == 0) {
            sbuf.append("* ");
        } else if (i == 1) {
            sbuf.append("- DSBLE ");
        }
        sbuf.append("ID: ");
        sbuf.append(this.networkId);
        sbuf.append(" SSID: ");
        sbuf.append(this.SSID);
        sbuf.append(" PROVIDER-NAME: ");
        sbuf.append(this.providerFriendlyName);
        sbuf.append(" BSSID: ");
        sbuf.append(this.BSSID);
        sbuf.append(" FQDN: ");
        sbuf.append(this.FQDN);
        sbuf.append(" PRIO: ");
        sbuf.append(this.priority);
        sbuf.append(" HIDDEN: ");
        sbuf.append(this.hiddenSSID);
        sbuf.append(" PMF: ");
        sbuf.append(this.requirePMF);
        sbuf.append('\n');
        sbuf.append(" NetworkSelectionStatus ");
        sbuf.append(this.mNetworkSelectionStatus.getNetworkStatusString() + "\n");
        if (this.mNetworkSelectionStatus.getNetworkSelectionDisableReason() > 0) {
            sbuf.append(" mNetworkSelectionDisableReason ");
            sbuf.append(this.mNetworkSelectionStatus.getNetworkDisableReasonString() + "\n");
            NetworkSelectionStatus networkSelectionStatus = this.mNetworkSelectionStatus;
            int index = 0;
            while (true) {
                NetworkSelectionStatus networkSelectionStatus2 = this.mNetworkSelectionStatus;
                if (index >= 15) {
                    break;
                }
                if (networkSelectionStatus2.getDisableReasonCounter(index) != 0) {
                    sbuf.append(NetworkSelectionStatus.getNetworkDisableReasonString(index) + " counter:" + this.mNetworkSelectionStatus.getDisableReasonCounter(index) + "\n");
                }
                index++;
            }
        }
        if (this.mNetworkSelectionStatus.getConnectChoice() != null) {
            sbuf.append(" connect choice: ");
            sbuf.append(this.mNetworkSelectionStatus.getConnectChoice());
            sbuf.append(" connect choice set time: ");
            sbuf.append(TimeUtils.logTimeOfDay(this.mNetworkSelectionStatus.getConnectChoiceTimestamp()));
        }
        sbuf.append(" hasEverConnected: ");
        sbuf.append(this.mNetworkSelectionStatus.getHasEverConnected());
        sbuf.append("\n");
        if (this.numAssociation > 0) {
            sbuf.append(" numAssociation ");
            sbuf.append(this.numAssociation);
            sbuf.append("\n");
        }
        if (this.numNoInternetAccessReports > 0) {
            sbuf.append(" numNoInternetAccessReports ");
            sbuf.append(this.numNoInternetAccessReports);
            sbuf.append("\n");
        }
        if (this.updateTime != null) {
            sbuf.append(" update ");
            sbuf.append(this.updateTime);
            sbuf.append("\n");
        }
        if (this.creationTime != null) {
            sbuf.append(" creation ");
            sbuf.append(this.creationTime);
            sbuf.append("\n");
        }
        if (this.didSelfAdd) {
            sbuf.append(" didSelfAdd");
        }
        if (this.selfAdded) {
            sbuf.append(" selfAdded");
        }
        if (this.validatedInternetAccess) {
            sbuf.append(" validatedInternetAccess");
        }
        if (this.ephemeral) {
            sbuf.append(" ephemeral");
        }
        if (this.osu) {
            sbuf.append(" osu");
        }
        if (this.trusted) {
            sbuf.append(" trusted");
        }
        if (this.fromWifiNetworkSuggestion) {
            sbuf.append(" fromWifiNetworkSuggestion");
        }
        if (this.fromWifiNetworkSpecifier) {
            sbuf.append(" fromWifiNetworkSpecifier");
        }
        if (this.meteredHint) {
            sbuf.append(" meteredHint");
        }
        if (this.useExternalScores) {
            sbuf.append(" useExternalScores");
        }
        if (this.didSelfAdd || this.selfAdded || this.validatedInternetAccess || this.ephemeral || this.trusted || this.fromWifiNetworkSuggestion || this.fromWifiNetworkSpecifier || this.meteredHint || this.useExternalScores) {
            sbuf.append("\n");
        }
        if (this.meteredOverride != 0) {
            sbuf.append(" meteredOverride ");
            sbuf.append(this.meteredOverride);
            sbuf.append("\n");
        }
        sbuf.append(" macRandomizationSetting: ");
        sbuf.append(this.macRandomizationSetting);
        sbuf.append("\n");
        sbuf.append(" mRandomizedMacAddress: ");
        sbuf.append(this.mRandomizedMacAddress);
        sbuf.append("\n");
        sbuf.append(" KeyMgmt:");
        for (int k = 0; k < this.allowedKeyManagement.size(); k++) {
            if (this.allowedKeyManagement.get(k)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (k < KeyMgmt.strings.length) {
                    sbuf.append(KeyMgmt.strings[k]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append(" Protocols:");
        for (int p = 0; p < this.allowedProtocols.size(); p++) {
            if (this.allowedProtocols.get(p)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (p < Protocol.strings.length) {
                    sbuf.append(Protocol.strings[p]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n');
        sbuf.append(" AuthAlgorithms:");
        for (int a = 0; a < this.allowedAuthAlgorithms.size(); a++) {
            if (this.allowedAuthAlgorithms.get(a)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (a < AuthAlgorithm.strings.length) {
                    sbuf.append(AuthAlgorithm.strings[a]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n');
        sbuf.append(" PairwiseCiphers:");
        for (int pc = 0; pc < this.allowedPairwiseCiphers.size(); pc++) {
            if (this.allowedPairwiseCiphers.get(pc)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (pc < PairwiseCipher.strings.length) {
                    sbuf.append(PairwiseCipher.strings[pc]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n');
        sbuf.append(" GroupCiphers:");
        for (int gc = 0; gc < this.allowedGroupCiphers.size(); gc++) {
            if (this.allowedGroupCiphers.get(gc)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (gc < GroupCipher.strings.length) {
                    sbuf.append(GroupCipher.strings[gc]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n');
        sbuf.append(" GroupMgmtCiphers:");
        for (int gmc = 0; gmc < this.allowedGroupManagementCiphers.size(); gmc++) {
            if (this.allowedGroupManagementCiphers.get(gmc)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (gmc < GroupMgmtCipher.strings.length) {
                    sbuf.append(GroupMgmtCipher.strings[gmc]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n');
        sbuf.append(" SuiteBCiphers:");
        for (int sbc = 0; sbc < this.allowedSuiteBCiphers.size(); sbc++) {
            if (this.allowedSuiteBCiphers.get(sbc)) {
                sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                if (sbc < SuiteBCipher.strings.length) {
                    sbuf.append(SuiteBCipher.strings[sbc]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        sbuf.append('\n');
        sbuf.append(" PSK/SAE: ");
        if (this.preSharedKey != null) {
            sbuf.append('*');
        }
        sbuf.append("\nEnterprise config:\n");
        sbuf.append(this.enterpriseConfig);
        sbuf.append("IP config:\n");
        sbuf.append(this.mIpConfiguration.toString());
        if (this.mNetworkSelectionStatus.getNetworkSelectionBSSID() != null) {
            sbuf.append(" networkSelectionBSSID=" + this.mNetworkSelectionStatus.getNetworkSelectionBSSID());
        }
        long now_ms = SystemClock.elapsedRealtime();
        if (this.mNetworkSelectionStatus.getDisableTime() != -1) {
            sbuf.append('\n');
            long diff = now_ms - this.mNetworkSelectionStatus.getDisableTime();
            if (diff <= 0) {
                sbuf.append(" blackListed since <incorrect>");
            } else {
                sbuf.append(" blackListed: ");
                sbuf.append(Long.toString(diff / 1000));
                sbuf.append("sec ");
            }
        }
        if (this.creatorUid != 0) {
            sbuf.append(" cuid=" + this.creatorUid);
        }
        if (this.creatorName != null) {
            sbuf.append(" cname=" + this.creatorName);
        }
        if (this.lastUpdateUid != 0) {
            sbuf.append(" luid=" + this.lastUpdateUid);
        }
        if (this.lastUpdateName != null) {
            sbuf.append(" lname=" + this.lastUpdateName);
        }
        if (this.updateIdentifier != null) {
            sbuf.append(" updateIdentifier=" + this.updateIdentifier);
        }
        sbuf.append(" lcuid=" + this.lastConnectUid);
        sbuf.append(" userApproved=" + userApprovedAsString(this.userApproved));
        sbuf.append(" noInternetAccessExpected=" + this.noInternetAccessExpected);
        sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        if (this.lastConnected != 0) {
            sbuf.append('\n');
            sbuf.append("lastConnected: ");
            sbuf.append(TimeUtils.logTimeOfDay(this.lastConnected));
            sbuf.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        }
        sbuf.append('\n');
        HashMap<String, Integer> hashMap = this.linkedConfigurations;
        if (hashMap != null) {
            for (String key : hashMap.keySet()) {
                sbuf.append(" linked: ");
                sbuf.append(key);
                sbuf.append('\n');
            }
        }
        sbuf.append("recentFailure: ");
        sbuf.append("Association Rejection code: ");
        sbuf.append(this.recentFailure.getAssociationStatus());
        sbuf.append("\n");
        return sbuf.toString();
    }

    @UnsupportedAppUsage
    public String getPrintableSsid() {
        String str = this.SSID;
        if (str == null) {
            return "";
        }
        int length = str.length();
        if (length > 2 && this.SSID.charAt(0) == '\"' && this.SSID.charAt(length - 1) == '\"') {
            return this.SSID.substring(1, length - 1);
        }
        if (length > 3 && this.SSID.charAt(0) == 'P' && this.SSID.charAt(1) == '\"' && this.SSID.charAt(length - 1) == '\"') {
            return WifiSsid.createFromAsciiEncoded(this.SSID.substring(2, length - 1)).toString();
        }
        return this.SSID;
    }

    public static String userApprovedAsString(int userApproved2) {
        if (userApproved2 == 0) {
            return "USER_UNSPECIFIED";
        }
        if (userApproved2 == 1) {
            return "USER_APPROVED";
        }
        if (userApproved2 != 2) {
            return "INVALID";
        }
        return "USER_BANNED";
    }

    public String getKeyIdForCredentials(WifiConfiguration current) {
        String keyMgmt = "";
        try {
            if (TextUtils.isEmpty(this.SSID)) {
                this.SSID = current.SSID;
            }
            if (this.allowedKeyManagement.cardinality() == 0) {
                this.allowedKeyManagement = current.allowedKeyManagement;
            }
            if (this.allowedKeyManagement.get(2)) {
                keyMgmt = keyMgmt + KeyMgmt.strings[2];
            }
            if (this.allowedKeyManagement.get(5)) {
                keyMgmt = keyMgmt + KeyMgmt.strings[5];
            }
            if (this.allowedKeyManagement.get(3)) {
                keyMgmt = keyMgmt + KeyMgmt.strings[3];
            }
            if (this.allowedKeyManagement.get(10)) {
                keyMgmt = keyMgmt + KeyMgmt.strings[10];
            }
            if (!TextUtils.isEmpty(keyMgmt)) {
                StringBuilder sb = new StringBuilder();
                sb.append(trimStringForKeyId(this.SSID));
                sb.append(Session.SESSION_SEPARATION_CHAR_CHILD);
                sb.append(keyMgmt);
                sb.append(Session.SESSION_SEPARATION_CHAR_CHILD);
                sb.append(trimStringForKeyId(this.enterpriseConfig.getKeyId(current != null ? current.enterpriseConfig : null)));
                String keyId = sb.toString();
                if (!this.fromWifiNetworkSuggestion) {
                    return keyId;
                }
                return keyId + Session.SESSION_SEPARATION_CHAR_CHILD + trimStringForKeyId(this.BSSID) + Session.SESSION_SEPARATION_CHAR_CHILD + trimStringForKeyId(this.creatorName);
            }
            throw new IllegalStateException("Not an EAP network");
        } catch (NullPointerException e) {
            throw new IllegalStateException("Invalid config details");
        }
    }

    private String trimStringForKeyId(String string) {
        if (string == null) {
            return "";
        }
        return string.replace("\"", "").replace(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, "");
    }

    /* access modifiers changed from: private */
    public static BitSet readBitSet(Parcel src) {
        int cardinality = src.readInt();
        BitSet set = new BitSet();
        for (int i = 0; i < cardinality; i++) {
            set.set(src.readInt());
        }
        return set;
    }

    private static void writeBitSet(Parcel dest, BitSet src) {
        int nextSetBit = -1;
        BitSet set = (BitSet) src.clone();
        dest.writeInt(set.cardinality());
        while (true) {
            int nextSetBit2 = set.nextSetBit(nextSetBit + 1);
            nextSetBit = nextSetBit2;
            if (nextSetBit2 != -1) {
                dest.writeInt(nextSetBit);
            } else {
                return;
            }
        }
    }

    @UnsupportedAppUsage
    public int getAuthType() {
        if (this.allowedKeyManagement.cardinality() > 1) {
            throw new IllegalStateException("More than one auth type set");
        } else if (this.allowedKeyManagement.get(1)) {
            return 1;
        } else {
            if (this.allowedKeyManagement.get(4)) {
                return 4;
            }
            if (this.allowedKeyManagement.get(2)) {
                return 2;
            }
            if (this.allowedKeyManagement.get(3)) {
                return 3;
            }
            if (this.allowedKeyManagement.get(8)) {
                return 8;
            }
            if (this.allowedKeyManagement.get(9)) {
                return 9;
            }
            if (this.allowedKeyManagement.get(10)) {
                return 10;
            }
            if (this.allowedKeyManagement.get(13)) {
                return 13;
            }
            if (this.allowedKeyManagement.get(14)) {
                return 14;
            }
            return 0;
        }
    }

    public String configKey(boolean allowCached) {
        if (allowCached && this.mCachedConfigKey != null) {
            return this.mCachedConfigKey;
        }
        if (this.providerFriendlyName != null) {
            String key = this.FQDN + KeyMgmt.strings[2];
            if (this.shared) {
                return key;
            }
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + Integer.toString(UserHandle.getUserId(this.creatorUid));
        }
        String key2 = getSsidAndSecurityTypeString();
        if (!this.shared) {
            key2 = key2 + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + Integer.toString(UserHandle.getUserId(this.creatorUid));
        }
        this.mCachedConfigKey = key2;
        return key2;
    }

    public String getSsidAndSecurityTypeString() {
        String key = this.SSID;
        if (this.allowedKeyManagement.get(1)) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[1];
        } else if (this.allowedKeyManagement.get(2) || this.allowedKeyManagement.get(3)) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[2];
        } else if (this.wepKeys[0] != null) {
            return key + "-WEP";
        } else if (this.allowedKeyManagement.get(9)) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[9];
        } else if (this.allowedKeyManagement.get(8)) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[8];
        } else if (this.allowedKeyManagement.get(10)) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[10];
        } else if (this.allowedKeyManagement.get(13)) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[13];
        } else if (this.allowedKeyManagement.get(14)) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[14];
        } else {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[0];
        }
    }

    public String configKey() {
        return configKey(false);
    }

    public static String configKey(ScanResult result) {
        String key = "\"" + result.SSID + "\"";
        if (result.capabilities.contains("WEP")) {
            return key + "-WEP";
        } else if (result.capabilities.contains("SAE")) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[8];
        } else if (result.capabilities.contains("WAPI-PSK")) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[13];
        } else if (result.capabilities.contains("PSK")) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[1];
        } else if (result.capabilities.contains("EAP") || result.capabilities.contains("IEEE8021X")) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[2];
        } else if (result.capabilities.contains("WAPI-KEY")) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[13];
        } else if (result.capabilities.contains("WAPI-CERT")) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[14];
        } else if (result.capabilities.contains("OWE")) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[9];
        } else if (result.capabilities.contains("FT_PSK")) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[6];
        } else if (result.capabilities.contains("FT_EAP")) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[7];
        } else if (result.capabilities.contains("WPA_PSK_SHA256")) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[11];
        } else if (result.capabilities.contains("WPA_EAP_SHA256")) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[12];
        } else if (result.capabilities.contains("SUITE_B_192")) {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[10];
        } else {
            return key + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + KeyMgmt.strings[0];
        }
    }

    @UnsupportedAppUsage
    public IpConfiguration getIpConfiguration() {
        return this.mIpConfiguration;
    }

    @UnsupportedAppUsage
    public void setIpConfiguration(IpConfiguration ipConfiguration) {
        if (ipConfiguration == null) {
            ipConfiguration = new IpConfiguration();
        }
        this.mIpConfiguration = ipConfiguration;
    }

    @UnsupportedAppUsage
    public StaticIpConfiguration getStaticIpConfiguration() {
        return this.mIpConfiguration.getStaticIpConfiguration();
    }

    @UnsupportedAppUsage
    public void setStaticIpConfiguration(StaticIpConfiguration staticIpConfiguration) {
        this.mIpConfiguration.setStaticIpConfiguration(staticIpConfiguration);
    }

    @UnsupportedAppUsage
    public IpConfiguration.IpAssignment getIpAssignment() {
        return this.mIpConfiguration.ipAssignment;
    }

    @UnsupportedAppUsage
    public void setIpAssignment(IpConfiguration.IpAssignment ipAssignment) {
        this.mIpConfiguration.ipAssignment = ipAssignment;
    }

    @UnsupportedAppUsage
    public IpConfiguration.ProxySettings getProxySettings() {
        return this.mIpConfiguration.proxySettings;
    }

    @UnsupportedAppUsage
    public void setProxySettings(IpConfiguration.ProxySettings proxySettings) {
        this.mIpConfiguration.proxySettings = proxySettings;
    }

    public ProxyInfo getHttpProxy() {
        if (this.mIpConfiguration.proxySettings == IpConfiguration.ProxySettings.NONE) {
            return null;
        }
        return new ProxyInfo(this.mIpConfiguration.httpProxy);
    }

    public void setHttpProxy(ProxyInfo httpProxy) {
        ProxyInfo httpProxyCopy;
        IpConfiguration.ProxySettings proxySettingCopy;
        if (httpProxy == null) {
            this.mIpConfiguration.setProxySettings(IpConfiguration.ProxySettings.NONE);
            this.mIpConfiguration.setHttpProxy(null);
            return;
        }
        if (!Uri.EMPTY.equals(httpProxy.getPacFileUrl())) {
            proxySettingCopy = IpConfiguration.ProxySettings.PAC;
            httpProxyCopy = new ProxyInfo(httpProxy.getPacFileUrl(), httpProxy.getPort());
        } else {
            proxySettingCopy = IpConfiguration.ProxySettings.STATIC;
            httpProxyCopy = new ProxyInfo(httpProxy.getHost(), httpProxy.getPort(), httpProxy.getExclusionListAsString());
        }
        if (httpProxyCopy.isValid()) {
            this.mIpConfiguration.setProxySettings(proxySettingCopy);
            this.mIpConfiguration.setHttpProxy(httpProxyCopy);
            return;
        }
        throw new IllegalArgumentException("Invalid ProxyInfo: " + httpProxyCopy.toString());
    }

    @UnsupportedAppUsage
    public void setProxy(IpConfiguration.ProxySettings settings, ProxyInfo proxy) {
        IpConfiguration ipConfiguration = this.mIpConfiguration;
        ipConfiguration.proxySettings = settings;
        ipConfiguration.httpProxy = proxy;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public void setPasspointManagementObjectTree(String passpointManagementObjectTree) {
        this.mPasspointManagementObjectTree = passpointManagementObjectTree;
    }

    public String getMoTree() {
        return this.mPasspointManagementObjectTree;
    }

    @UnsupportedAppUsage
    public WifiConfiguration(WifiConfiguration source) {
        int i;
        if (isNotChineseOperator()) {
            i = 1;
        } else {
            i = SystemProperties.getInt("persist.sys.wifi.mac_randomization", 0);
        }
        this.macRandomizationSetting = i;
        this.mNetworkSelectionStatus = new NetworkSelectionStatus();
        this.recentFailure = new RecentFailure();
        if (source != null) {
            this.networkId = source.networkId;
            this.status = source.status;
            this.SSID = source.SSID;
            this.BSSID = source.BSSID;
            this.FQDN = source.FQDN;
            this.roamingConsortiumIds = (long[]) source.roamingConsortiumIds.clone();
            this.providerFriendlyName = source.providerFriendlyName;
            this.isHomeProviderNetwork = source.isHomeProviderNetwork;
            this.preSharedKey = source.preSharedKey;
            this.mNetworkSelectionStatus.copy(source.getNetworkSelectionStatus());
            this.apBand = source.apBand;
            this.apChannel = source.apChannel;
            this.wepKeys = new String[4];
            int i2 = 0;
            while (true) {
                String[] strArr = this.wepKeys;
                if (i2 >= strArr.length) {
                    break;
                }
                strArr[i2] = source.wepKeys[i2];
                i2++;
            }
            this.wepTxKeyIndex = source.wepTxKeyIndex;
            this.priority = source.priority;
            this.hiddenSSID = source.hiddenSSID;
            this.allowedKeyManagement = (BitSet) source.allowedKeyManagement.clone();
            this.allowedProtocols = (BitSet) source.allowedProtocols.clone();
            this.allowedAuthAlgorithms = (BitSet) source.allowedAuthAlgorithms.clone();
            this.allowedPairwiseCiphers = (BitSet) source.allowedPairwiseCiphers.clone();
            this.allowedGroupCiphers = (BitSet) source.allowedGroupCiphers.clone();
            this.allowedGroupManagementCiphers = (BitSet) source.allowedGroupManagementCiphers.clone();
            this.allowedSuiteBCiphers = (BitSet) source.allowedSuiteBCiphers.clone();
            this.enterpriseConfig = new WifiEnterpriseConfig(source.enterpriseConfig);
            this.defaultGwMacAddress = source.defaultGwMacAddress;
            this.mIpConfiguration = new IpConfiguration(source.mIpConfiguration);
            HashMap<String, Integer> hashMap = source.linkedConfigurations;
            if (hashMap != null && hashMap.size() > 0) {
                this.linkedConfigurations = new HashMap<>();
                this.linkedConfigurations.putAll(source.linkedConfigurations);
            }
            this.mCachedConfigKey = null;
            this.selfAdded = source.selfAdded;
            this.validatedInternetAccess = source.validatedInternetAccess;
            this.isLegacyPasspointConfig = source.isLegacyPasspointConfig;
            this.ephemeral = source.ephemeral;
            this.osu = source.osu;
            this.trusted = source.trusted;
            this.fromWifiNetworkSuggestion = source.fromWifiNetworkSuggestion;
            this.fromWifiNetworkSpecifier = source.fromWifiNetworkSpecifier;
            this.meteredHint = source.meteredHint;
            this.meteredOverride = source.meteredOverride;
            this.useExternalScores = source.useExternalScores;
            this.didSelfAdd = source.didSelfAdd;
            this.lastConnectUid = source.lastConnectUid;
            this.lastUpdateUid = source.lastUpdateUid;
            this.creatorUid = source.creatorUid;
            this.creatorName = source.creatorName;
            this.lastUpdateName = source.lastUpdateName;
            this.peerWifiConfiguration = source.peerWifiConfiguration;
            this.lastConnected = source.lastConnected;
            this.lastDisconnected = source.lastDisconnected;
            this.globalId = source.globalId;
            this.numScorerOverride = source.numScorerOverride;
            this.numScorerOverrideAndSwitchedNetwork = source.numScorerOverrideAndSwitchedNetwork;
            this.numAssociation = source.numAssociation;
            this.userApproved = source.userApproved;
            this.numNoInternetAccessReports = source.numNoInternetAccessReports;
            this.noInternetAccessExpected = source.noInternetAccessExpected;
            this.creationTime = source.creationTime;
            this.updateTime = source.updateTime;
            this.shared = source.shared;
            this.recentFailure.setAssociationStatus(source.recentFailure.getAssociationStatus());
            this.mRandomizedMacAddress = source.mRandomizedMacAddress;
            this.macRandomizationSetting = source.macRandomizationSetting;
            this.requirePMF = source.requirePMF;
            this.updateIdentifier = source.updateIdentifier;
            this.wapiCertSelMode = source.wapiCertSelMode;
            this.wapiCertSel = source.wapiCertSel;
            this.wapiPskType = source.wapiPskType;
            this.wapiPsk = source.wapiPsk;
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.networkId);
        dest.writeInt(this.status);
        this.mNetworkSelectionStatus.writeToParcel(dest);
        dest.writeString(this.SSID);
        dest.writeString(this.BSSID);
        dest.writeInt(this.apBand);
        dest.writeInt(this.apChannel);
        dest.writeString(this.FQDN);
        dest.writeString(this.providerFriendlyName);
        dest.writeInt(this.isHomeProviderNetwork ? 1 : 0);
        dest.writeInt(this.roamingConsortiumIds.length);
        for (long roamingConsortiumId : this.roamingConsortiumIds) {
            dest.writeLong(roamingConsortiumId);
        }
        dest.writeString(this.preSharedKey);
        for (String wepKey : this.wepKeys) {
            dest.writeString(wepKey);
        }
        dest.writeInt(this.wepTxKeyIndex);
        dest.writeInt(this.priority);
        dest.writeInt(this.hiddenSSID ? 1 : 0);
        dest.writeInt(this.requirePMF ? 1 : 0);
        dest.writeString(this.updateIdentifier);
        writeBitSet(dest, this.allowedKeyManagement);
        writeBitSet(dest, this.allowedProtocols);
        writeBitSet(dest, this.allowedAuthAlgorithms);
        writeBitSet(dest, this.allowedPairwiseCiphers);
        writeBitSet(dest, this.allowedGroupCiphers);
        writeBitSet(dest, this.allowedGroupManagementCiphers);
        writeBitSet(dest, this.allowedSuiteBCiphers);
        dest.writeParcelable(this.enterpriseConfig, flags);
        dest.writeParcelable(this.mIpConfiguration, flags);
        dest.writeString(this.dhcpServer);
        dest.writeString(this.defaultGwMacAddress);
        dest.writeInt(this.selfAdded ? 1 : 0);
        dest.writeInt(this.didSelfAdd ? 1 : 0);
        dest.writeInt(this.validatedInternetAccess ? 1 : 0);
        dest.writeInt(this.isLegacyPasspointConfig ? 1 : 0);
        dest.writeInt(this.ephemeral ? 1 : 0);
        dest.writeInt(this.trusted ? 1 : 0);
        dest.writeInt(this.fromWifiNetworkSuggestion ? 1 : 0);
        dest.writeInt(this.fromWifiNetworkSpecifier ? 1 : 0);
        dest.writeInt(this.meteredHint ? 1 : 0);
        dest.writeInt(this.meteredOverride);
        dest.writeInt(this.useExternalScores ? 1 : 0);
        dest.writeInt(this.creatorUid);
        dest.writeInt(this.lastConnectUid);
        dest.writeInt(this.lastUpdateUid);
        dest.writeString(this.creatorName);
        dest.writeString(this.lastUpdateName);
        dest.writeInt(this.numScorerOverride);
        dest.writeInt(this.numScorerOverrideAndSwitchedNetwork);
        dest.writeInt(this.numAssociation);
        dest.writeInt(this.userApproved);
        dest.writeInt(this.numNoInternetAccessReports);
        dest.writeInt(this.noInternetAccessExpected ? 1 : 0);
        dest.writeInt(this.shared ? 1 : 0);
        dest.writeString(this.mPasspointManagementObjectTree);
        dest.writeInt(this.recentFailure.getAssociationStatus());
        dest.writeParcelable(this.mRandomizedMacAddress, flags);
        dest.writeInt(this.macRandomizationSetting);
        dest.writeInt(this.osu ? 1 : 0);
        dest.writeInt(this.wapiCertSelMode);
        dest.writeString(this.wapiCertSel);
        dest.writeInt(this.wapiPskType);
        dest.writeString(this.wapiPsk);
        dest.writeLong(this.lastConnected);
        dest.writeString(this.globalId);
    }

    public byte[] getBytesForBackup() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeInt(3);
        BackupUtils.writeString(out, this.SSID);
        out.writeInt(this.apBand);
        out.writeInt(this.apChannel);
        BackupUtils.writeString(out, this.preSharedKey);
        out.writeInt(getAuthType());
        out.writeBoolean(this.hiddenSSID);
        return baos.toByteArray();
    }

    public static WifiConfiguration getWifiConfigFromBackup(DataInputStream in) throws IOException, BackupUtils.BadVersionException {
        WifiConfiguration config = new WifiConfiguration();
        int version = in.readInt();
        if (version < 1 || version > 3) {
            throw new BackupUtils.BadVersionException("Unknown Backup Serialization Version");
        } else if (version == 1) {
            return null;
        } else {
            config.SSID = BackupUtils.readString(in);
            config.apBand = in.readInt();
            config.apChannel = in.readInt();
            config.preSharedKey = BackupUtils.readString(in);
            config.allowedKeyManagement.set(in.readInt());
            if (version >= 3) {
                config.hiddenSSID = in.readBoolean();
            }
            return config;
        }
    }

    public boolean checkWifiSecureLevel() {
        boolean result = true;
        int level = SystemProperties.getInt("persist.sys.wifi_secure_level", -1);
        if (level < SecurityLevel.OPEN.ordinal()) {
            Log.d(TAG, "level is default = " + level);
            return true;
        } else if (level > SecurityLevel.EAP.ordinal()) {
            Log.d(TAG, "level is higher than normal = " + level);
            return false;
        } else {
            String sConfigKey = configKey();
            if (sConfigKey == null) {
                Log.d(TAG, "sConfigKey = null");
                return false;
            }
            int secureLevel = -1;
            for (int i = SecurityLevel.OPEN.ordinal(); i <= SecurityLevel.EAP.ordinal(); i++) {
                if (sConfigKey.contains(SecurityLevelString[i])) {
                    secureLevel = i;
                }
            }
            if (secureLevel < level) {
                result = false;
            }
            Log.d(TAG, "result = " + result + " wifi_secure_level = " + level + " authtype = " + getAuthType() + " secureLevel = " + secureLevel);
            return result;
        }
    }
}
