package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiSsid;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.net.DelayedDiskWrite;
import com.android.server.net.DelayedDiskWrite.Writer;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WifiNetworkHistory {
    private static final String AUTH_KEY = "AUTH";
    private static final String BSSID_KEY = "BSSID";
    private static final String BSSID_KEY_END = "/BSSID";
    private static final String BSSID_STATUS_KEY = "BSSID_STATUS";
    private static final String CHOICE_KEY = "CHOICE";
    private static final String CHOICE_TIME_KEY = "CHOICE_TIME";
    private static final String CONFIG_BSSID_KEY = "CONFIG_BSSID";
    static final String CONFIG_KEY = "CONFIG";
    private static final String CONNECT_UID_KEY = "CONNECT_UID_KEY";
    private static final String CREATION_TIME_KEY = "CREATION_TIME";
    private static final String CREATOR_NAME_KEY = "CREATOR_NAME";
    static final String CREATOR_UID_KEY = "CREATOR_UID_KEY";
    private static final String DATE_KEY = "DATE";
    private static final boolean DBG = true;
    private static final String DEFAULT_GW_KEY = "DEFAULT_GW";
    private static final String DELETED_EPHEMERAL_KEY = "DELETED_EPHEMERAL";
    private static final String DID_SELF_ADD_KEY = "DID_SELF_ADD";
    private static final String EPHEMERAL_KEY = "EPHEMERAL";
    private static final String FQDN_KEY = "FQDN";
    private static final String FREQ_KEY = "FREQ";
    private static final String HAS_EVER_CONNECTED_KEY = "HAS_EVER_CONNECTED";
    private static final String LINK_KEY = "LINK";
    private static final String METERED_HINT_KEY = "METERED_HINT";
    private static final String METERED_OVERRIDE_KEY = "METERED_OVERRIDE";
    private static final String MILLI_KEY = "MILLI";
    static final String NETWORK_HISTORY_CONFIG_FILE = (Environment.getDataDirectory() + "/misc/wifi/networkHistory.txt");
    private static final String NETWORK_ID_KEY = "ID";
    private static final String NETWORK_SELECTION_DISABLE_REASON_KEY = "NETWORK_SELECTION_DISABLE_REASON";
    private static final String NETWORK_SELECTION_STATUS_KEY = "NETWORK_SELECTION_STATUS";
    private static final String NL = "\n";
    private static final String NO_INTERNET_ACCESS_EXPECTED_KEY = "NO_INTERNET_ACCESS_EXPECTED";
    private static final String NO_INTERNET_ACCESS_REPORTS_KEY = "NO_INTERNET_ACCESS_REPORTS";
    private static final String NUM_ASSOCIATION_KEY = "NUM_ASSOCIATION";
    private static final String PEER_CONFIGURATION_KEY = "PEER_CONFIGURATION";
    private static final String PRIORITY_KEY = "PRIORITY";
    private static final String RSSI_KEY = "RSSI";
    private static final String SCORER_OVERRIDE_AND_SWITCH_KEY = "SCORER_OVERRIDE_AND_SWITCH";
    private static final String SCORER_OVERRIDE_KEY = "SCORER_OVERRIDE";
    private static final String SELF_ADDED_KEY = "SELF_ADDED";
    private static final String SEPARATOR = ":  ";
    static final String SHARED_KEY = "SHARED";
    private static final String SSID_KEY = "SSID";
    public static final String TAG = "WifiNetworkHistory";
    private static final String UPDATE_NAME_KEY = "UPDATE_NAME";
    private static final String UPDATE_TIME_KEY = "UPDATE_TIME";
    private static final String UPDATE_UID_KEY = "UPDATE_UID";
    private static final String USER_APPROVED_KEY = "USER_APPROVED";
    private static final String USE_EXTERNAL_SCORES_KEY = "USE_EXTERNAL_SCORES";
    private static final String VALIDATED_INTERNET_ACCESS_KEY = "VALIDATED_INTERNET_ACCESS";
    private static final boolean VDBG = true;
    Context mContext;
    HashSet<String> mLostConfigsDbg = new HashSet();
    protected final DelayedDiskWrite mWriter;

    public WifiNetworkHistory(Context c, DelayedDiskWrite writer) {
        this.mContext = c;
        this.mWriter = writer;
    }

    public void writeKnownNetworkHistory(final List<WifiConfiguration> networks, final ConcurrentHashMap<Integer, ScanDetailCache> scanDetailCaches, final Set<String> deletedEphemeralSSIDs) {
        this.mWriter.write(NETWORK_HISTORY_CONFIG_FILE, new Writer() {
            public void onWriteCalled(DataOutputStream out) throws IOException {
                for (WifiConfiguration config : networks) {
                    String disableTime;
                    NetworkSelectionStatus status = config.getNetworkSelectionStatus();
                    int numlink = 0;
                    if (config.linkedConfigurations != null) {
                        numlink = config.linkedConfigurations.size();
                    }
                    if (config.getNetworkSelectionStatus().isNetworkEnabled()) {
                        disableTime = "";
                    } else {
                        disableTime = "Disable time: " + DateFormat.getInstance().format(Long.valueOf(config.getNetworkSelectionStatus().getDisableTime()));
                    }
                    WifiNetworkHistory.this.logd("saving network history: " + config.configKey() + " gw: " + config.defaultGwMacAddress + " Network Selection-status: " + status.getNetworkStatusString() + disableTime + " ephemeral=" + config.ephemeral + " choice:" + status.getConnectChoice() + " link:" + numlink + " status:" + config.status + " nid:" + config.networkId + " hasEverConnected: " + status.getHasEverConnected());
                    if (WifiNetworkHistory.this.isValid(config)) {
                        if (config.SSID == null) {
                            WifiNetworkHistory.this.logv("writeKnownNetworkHistory trying to write config with null SSID");
                        } else {
                            WifiNetworkHistory.this.logv("writeKnownNetworkHistory write config " + config.configKey());
                            out.writeUTF("CONFIG:  " + config.configKey() + WifiNetworkHistory.NL);
                            if (config.SSID != null) {
                                out.writeUTF("SSID:  " + config.SSID + WifiNetworkHistory.NL);
                            }
                            if (config.BSSID != null) {
                                out.writeUTF("CONFIG_BSSID:  " + config.BSSID + WifiNetworkHistory.NL);
                            } else {
                                out.writeUTF("CONFIG_BSSID:  null\n");
                            }
                            if (config.FQDN != null) {
                                out.writeUTF("FQDN:  " + config.FQDN + WifiNetworkHistory.NL);
                            }
                            out.writeUTF("PRIORITY:  " + Integer.toString(config.priority) + WifiNetworkHistory.NL);
                            out.writeUTF("ID:  " + Integer.toString(config.networkId) + WifiNetworkHistory.NL);
                            out.writeUTF("SELF_ADDED:  " + Boolean.toString(config.selfAdded) + WifiNetworkHistory.NL);
                            out.writeUTF("DID_SELF_ADD:  " + Boolean.toString(config.didSelfAdd) + WifiNetworkHistory.NL);
                            out.writeUTF("NO_INTERNET_ACCESS_REPORTS:  " + Integer.toString(config.numNoInternetAccessReports) + WifiNetworkHistory.NL);
                            out.writeUTF("VALIDATED_INTERNET_ACCESS:  " + Boolean.toString(config.validatedInternetAccess) + WifiNetworkHistory.NL);
                            out.writeUTF("NO_INTERNET_ACCESS_EXPECTED:  " + Boolean.toString(config.noInternetAccessExpected) + WifiNetworkHistory.NL);
                            out.writeUTF("EPHEMERAL:  " + Boolean.toString(config.ephemeral) + WifiNetworkHistory.NL);
                            out.writeUTF("METERED_HINT:  " + Boolean.toString(config.meteredHint) + WifiNetworkHistory.NL);
                            out.writeUTF("METERED_OVERRIDE:  " + Integer.toString(config.meteredOverride) + WifiNetworkHistory.NL);
                            out.writeUTF("USE_EXTERNAL_SCORES:  " + Boolean.toString(config.useExternalScores) + WifiNetworkHistory.NL);
                            if (config.creationTime != null) {
                                out.writeUTF("CREATION_TIME:  " + config.creationTime + WifiNetworkHistory.NL);
                            }
                            if (config.updateTime != null) {
                                out.writeUTF("UPDATE_TIME:  " + config.updateTime + WifiNetworkHistory.NL);
                            }
                            if (config.peerWifiConfiguration != null) {
                                out.writeUTF("PEER_CONFIGURATION:  " + config.peerWifiConfiguration + WifiNetworkHistory.NL);
                            }
                            out.writeUTF("SCORER_OVERRIDE:  " + Integer.toString(config.numScorerOverride) + WifiNetworkHistory.NL);
                            out.writeUTF("SCORER_OVERRIDE_AND_SWITCH:  " + Integer.toString(config.numScorerOverrideAndSwitchedNetwork) + WifiNetworkHistory.NL);
                            out.writeUTF("NUM_ASSOCIATION:  " + Integer.toString(config.numAssociation) + WifiNetworkHistory.NL);
                            out.writeUTF("CREATOR_UID_KEY:  " + Integer.toString(config.creatorUid) + WifiNetworkHistory.NL);
                            out.writeUTF("CONNECT_UID_KEY:  " + Integer.toString(config.lastConnectUid) + WifiNetworkHistory.NL);
                            out.writeUTF("UPDATE_UID:  " + Integer.toString(config.lastUpdateUid) + WifiNetworkHistory.NL);
                            out.writeUTF("CREATOR_NAME:  " + config.creatorName + WifiNetworkHistory.NL);
                            out.writeUTF("UPDATE_NAME:  " + config.lastUpdateName + WifiNetworkHistory.NL);
                            out.writeUTF("USER_APPROVED:  " + Integer.toString(config.userApproved) + WifiNetworkHistory.NL);
                            out.writeUTF("SHARED:  " + Boolean.toString(config.shared) + WifiNetworkHistory.NL);
                            String allowedKeyManagementString = WifiNetworkHistory.makeString(config.allowedKeyManagement, KeyMgmt.strings);
                            if (config.allowedKeyManagement.get(190)) {
                                allowedKeyManagementString = "WAPI_PSK".replace('_', '-');
                            } else if (config.allowedKeyManagement.get(191)) {
                                allowedKeyManagementString = "WAPI_CERT".replace('_', '-');
                            }
                            out.writeUTF("AUTH:  " + allowedKeyManagementString + WifiNetworkHistory.NL);
                            out.writeUTF("NETWORK_SELECTION_STATUS:  " + status.getNetworkSelectionStatus() + WifiNetworkHistory.NL);
                            out.writeUTF("NETWORK_SELECTION_DISABLE_REASON:  " + status.getNetworkSelectionDisableReason() + WifiNetworkHistory.NL);
                            if (status.getConnectChoice() != null) {
                                out.writeUTF("CHOICE:  " + status.getConnectChoice() + WifiNetworkHistory.NL);
                                out.writeUTF("CHOICE_TIME:  " + status.getConnectChoiceTimestamp() + WifiNetworkHistory.NL);
                            }
                            if (config.linkedConfigurations != null) {
                                WifiNetworkHistory.this.log("writeKnownNetworkHistory write linked " + config.linkedConfigurations.size());
                                for (String key : config.linkedConfigurations.keySet()) {
                                    out.writeUTF("LINK:  " + key + WifiNetworkHistory.NL);
                                }
                            }
                            String macAddress = config.defaultGwMacAddress;
                            if (macAddress != null) {
                                out.writeUTF("DEFAULT_GW:  " + macAddress + WifiNetworkHistory.NL);
                            }
                            if (WifiNetworkHistory.this.getScanDetailCache(config, scanDetailCaches) != null) {
                                for (ScanDetail scanDetail : WifiNetworkHistory.this.getScanDetailCache(config, scanDetailCaches).values()) {
                                    ScanResult result = scanDetail.getScanResult();
                                    out.writeUTF("BSSID:  " + result.BSSID + WifiNetworkHistory.NL);
                                    out.writeUTF("FREQ:  " + Integer.toString(result.frequency) + WifiNetworkHistory.NL);
                                    out.writeUTF("RSSI:  " + Integer.toString(result.level) + WifiNetworkHistory.NL);
                                    out.writeUTF("/BSSID\n");
                                }
                            }
                            out.writeUTF("HAS_EVER_CONNECTED:  " + Boolean.toString(status.getHasEverConnected()) + WifiNetworkHistory.NL);
                            out.writeUTF(WifiNetworkHistory.NL);
                            out.writeUTF(WifiNetworkHistory.NL);
                            out.writeUTF(WifiNetworkHistory.NL);
                        }
                    }
                }
                if (deletedEphemeralSSIDs != null && deletedEphemeralSSIDs.size() > 0) {
                    for (String ssid : deletedEphemeralSSIDs) {
                        out.writeUTF(WifiNetworkHistory.DELETED_EPHEMERAL_KEY);
                        out.writeUTF(ssid);
                        out.writeUTF(WifiNetworkHistory.NL);
                    }
                }
            }
        });
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00bb A:{SYNTHETIC, Splitter: B:33:0x00bb} */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x04a0 A:{SYNTHETIC, Splitter: B:199:0x04a0} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00c0 A:{SYNTHETIC, Splitter: B:36:0x00c0} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readNetworkHistory(Map<String, WifiConfiguration> configs, Map<Integer, ScanDetailCache> scanDetailCaches, Set<String> deletedEphemeralSSIDs) {
        FileNotFoundException e;
        Throwable e2;
        Throwable e3;
        Throwable e4;
        Throwable th;
        Throwable th2;
        Throwable th3 = null;
        DataInputStream in = null;
        try {
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(NETWORK_HISTORY_CONFIG_FILE)));
            String bssid = null;
            String ssid = null;
            int freq = 0;
            long seen = 0;
            try {
                int rssi = WifiConfiguration.INVALID_RSSI;
                String caps = null;
                WifiConfiguration config = null;
                while (true) {
                    String line = dataInputStream.readUTF();
                    if (line == null) {
                        break;
                    }
                    int colon = line.indexOf(58);
                    if (colon >= 0) {
                        String key = line.substring(0, colon).trim();
                        String value = line.substring(colon + 1).trim();
                        if (key.equals(CONFIG_KEY)) {
                            config = (WifiConfiguration) configs.get(value);
                            if (config == null) {
                                Log.e(TAG, "readNetworkHistory didnt find netid for hash=" + Integer.toString(value.hashCode()) + " key: " + value);
                                this.mLostConfigsDbg.add(value);
                            } else if (config.creatorName == null || config.lastUpdateName == null) {
                                config.creatorName = this.mContext.getPackageManager().getNameForUid(OppoManuConnectManager.UID_DEFAULT);
                                config.lastUpdateName = config.creatorName;
                                Log.w(TAG, "Upgrading network " + config.networkId + " to " + config.creatorName);
                            }
                        } else if (config != null) {
                            NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
                            if (!key.equals("SSID")) {
                                if (key.equals(CONFIG_BSSID_KEY)) {
                                    if (value.equals("null")) {
                                        value = null;
                                    }
                                    config.BSSID = value;
                                } else {
                                    if (key.equals("FQDN")) {
                                        if (value.equals("null")) {
                                            value = null;
                                        }
                                        config.FQDN = value;
                                    } else {
                                        if (key.equals(DEFAULT_GW_KEY)) {
                                            config.defaultGwMacAddress = value;
                                        } else {
                                            if (key.equals(SELF_ADDED_KEY)) {
                                                config.selfAdded = Boolean.parseBoolean(value);
                                            } else {
                                                if (key.equals(DID_SELF_ADD_KEY)) {
                                                    config.didSelfAdd = Boolean.parseBoolean(value);
                                                } else {
                                                    if (key.equals(NO_INTERNET_ACCESS_REPORTS_KEY)) {
                                                        config.numNoInternetAccessReports = Integer.parseInt(value);
                                                    } else {
                                                        if (key.equals(VALIDATED_INTERNET_ACCESS_KEY)) {
                                                            config.validatedInternetAccess = Boolean.parseBoolean(value);
                                                        } else {
                                                            if (key.equals(NO_INTERNET_ACCESS_EXPECTED_KEY)) {
                                                                config.noInternetAccessExpected = Boolean.parseBoolean(value);
                                                            } else {
                                                                if (key.equals(CREATION_TIME_KEY)) {
                                                                    config.creationTime = value;
                                                                } else {
                                                                    if (key.equals(UPDATE_TIME_KEY)) {
                                                                        config.updateTime = value;
                                                                    } else {
                                                                        if (key.equals(EPHEMERAL_KEY)) {
                                                                            config.ephemeral = Boolean.parseBoolean(value);
                                                                        } else {
                                                                            if (key.equals(METERED_HINT_KEY)) {
                                                                                config.meteredHint = Boolean.parseBoolean(value);
                                                                            } else {
                                                                                if (key.equals(METERED_OVERRIDE_KEY)) {
                                                                                    config.meteredOverride = Integer.parseInt(value);
                                                                                } else {
                                                                                    if (key.equals(USE_EXTERNAL_SCORES_KEY)) {
                                                                                        config.useExternalScores = Boolean.parseBoolean(value);
                                                                                    } else {
                                                                                        if (key.equals(CREATOR_UID_KEY)) {
                                                                                            config.creatorUid = Integer.parseInt(value);
                                                                                        } else {
                                                                                            if (key.equals(SCORER_OVERRIDE_KEY)) {
                                                                                                config.numScorerOverride = Integer.parseInt(value);
                                                                                            } else {
                                                                                                if (key.equals(SCORER_OVERRIDE_AND_SWITCH_KEY)) {
                                                                                                    config.numScorerOverrideAndSwitchedNetwork = Integer.parseInt(value);
                                                                                                } else {
                                                                                                    if (key.equals(NUM_ASSOCIATION_KEY)) {
                                                                                                        config.numAssociation = Integer.parseInt(value);
                                                                                                    } else {
                                                                                                        if (key.equals(CONNECT_UID_KEY)) {
                                                                                                            config.lastConnectUid = Integer.parseInt(value);
                                                                                                        } else {
                                                                                                            if (key.equals(UPDATE_UID_KEY)) {
                                                                                                                config.lastUpdateUid = Integer.parseInt(value);
                                                                                                            } else {
                                                                                                                if (key.equals(PEER_CONFIGURATION_KEY)) {
                                                                                                                    config.peerWifiConfiguration = value;
                                                                                                                } else {
                                                                                                                    if (key.equals(NETWORK_SELECTION_STATUS_KEY)) {
                                                                                                                        int networkStatusValue = Integer.parseInt(value);
                                                                                                                        if (networkStatusValue == 1) {
                                                                                                                            networkStatusValue = 0;
                                                                                                                        }
                                                                                                                        networkStatus.setNetworkSelectionStatus(networkStatusValue);
                                                                                                                    } else {
                                                                                                                        if (key.equals(NETWORK_SELECTION_DISABLE_REASON_KEY)) {
                                                                                                                            networkStatus.setNetworkSelectionDisableReason(Integer.parseInt(value));
                                                                                                                        } else {
                                                                                                                            if (key.equals(CHOICE_KEY)) {
                                                                                                                                networkStatus.setConnectChoice(value);
                                                                                                                            } else {
                                                                                                                                if (key.equals(CHOICE_TIME_KEY)) {
                                                                                                                                    networkStatus.setConnectChoiceTimestamp(Long.parseLong(value));
                                                                                                                                } else {
                                                                                                                                    if (!key.equals(LINK_KEY)) {
                                                                                                                                        if (key.equals("BSSID")) {
                                                                                                                                            ssid = null;
                                                                                                                                            bssid = null;
                                                                                                                                            freq = 0;
                                                                                                                                            seen = 0;
                                                                                                                                            rssi = WifiConfiguration.INVALID_RSSI;
                                                                                                                                            caps = "";
                                                                                                                                        } else {
                                                                                                                                            if (key.equals(RSSI_KEY)) {
                                                                                                                                                rssi = Integer.parseInt(value);
                                                                                                                                            } else {
                                                                                                                                                if (key.equals(FREQ_KEY)) {
                                                                                                                                                    freq = Integer.parseInt(value);
                                                                                                                                                } else {
                                                                                                                                                    if (!key.equals(DATE_KEY)) {
                                                                                                                                                        if (!key.equals(BSSID_KEY_END)) {
                                                                                                                                                            if (!key.equals(DELETED_EPHEMERAL_KEY)) {
                                                                                                                                                                if (key.equals(CREATOR_NAME_KEY)) {
                                                                                                                                                                    config.creatorName = value;
                                                                                                                                                                } else {
                                                                                                                                                                    if (key.equals(UPDATE_NAME_KEY)) {
                                                                                                                                                                        config.lastUpdateName = value;
                                                                                                                                                                    } else {
                                                                                                                                                                        if (key.equals(USER_APPROVED_KEY)) {
                                                                                                                                                                            config.userApproved = Integer.parseInt(value);
                                                                                                                                                                        } else {
                                                                                                                                                                            if (key.equals(SHARED_KEY)) {
                                                                                                                                                                                config.shared = Boolean.parseBoolean(value);
                                                                                                                                                                            } else {
                                                                                                                                                                                if (key.equals(HAS_EVER_CONNECTED_KEY)) {
                                                                                                                                                                                    networkStatus.setHasEverConnected(Boolean.parseBoolean(value));
                                                                                                                                                                                }
                                                                                                                                                                            }
                                                                                                                                                                        }
                                                                                                                                                                    }
                                                                                                                                                                }
                                                                                                                                                            } else if (!TextUtils.isEmpty(value)) {
                                                                                                                                                                deletedEphemeralSSIDs.add(value);
                                                                                                                                                            }
                                                                                                                                                        } else if (!(null == null || ssid == null || getScanDetailCache(config, scanDetailCaches) == null)) {
                                                                                                                                                            getScanDetailCache(config, scanDetailCaches).put(new ScanDetail(WifiSsid.createFromAsciiEncoded(ssid), bssid, caps, rssi, freq, 0, seen));
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    } else if (config.linkedConfigurations == null) {
                                                                                                                                        config.linkedConfigurations = new HashMap();
                                                                                                                                    } else {
                                                                                                                                        config.linkedConfigurations.put(value, Integer.valueOf(-1));
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (!config.isPasspoint()) {
                                ssid = value;
                                if (config.SSID == null || (config.SSID.equals(value) ^ 1) == 0) {
                                    config.SSID = value;
                                } else {
                                    loge("Error parsing network history file, mismatched SSIDs");
                                    config = null;
                                    ssid = null;
                                }
                            }
                        }
                    }
                }
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (Throwable th4) {
                        th3 = th4;
                    }
                }
                if (th3 != null) {
                    try {
                        throw th3;
                    } catch (EOFException e5) {
                        in = dataInputStream;
                        return;
                    } catch (FileNotFoundException e6) {
                        e = e6;
                        in = dataInputStream;
                        Log.i(TAG, "readNetworkHistory: no config file, " + e);
                        return;
                    } catch (NumberFormatException e7) {
                        e2 = e7;
                        in = dataInputStream;
                        Log.e(TAG, "readNetworkHistory: failed to parse, " + e2, e2);
                        return;
                    } catch (IOException e8) {
                        e3 = e8;
                        in = dataInputStream;
                        Log.e(TAG, "readNetworkHistory: failed to read, " + e3, e3);
                        return;
                    } catch (StringIndexOutOfBoundsException e9) {
                        e4 = e9;
                        in = dataInputStream;
                        Log.e(TAG, "readNetworkHistory: read file line length error, " + e4, e4);
                        return;
                    }
                }
                in = dataInputStream;
            } catch (Throwable th5) {
                th = th5;
                in = dataInputStream;
                th2 = null;
                if (in != null) {
                }
                if (th2 == null) {
                }
            }
        } catch (Throwable th6) {
            th = th6;
            th2 = null;
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable th32) {
                    if (th2 == null) {
                        th2 = th32;
                    } else if (th2 != th32) {
                        th2.addSuppressed(th32);
                    }
                }
            }
            if (th2 == null) {
                try {
                    throw th2;
                } catch (EOFException e10) {
                    return;
                } catch (FileNotFoundException e11) {
                    e = e11;
                    Log.i(TAG, "readNetworkHistory: no config file, " + e);
                    return;
                } catch (NumberFormatException e12) {
                    e2 = e12;
                    Log.e(TAG, "readNetworkHistory: failed to parse, " + e2, e2);
                    return;
                } catch (IOException e13) {
                    e3 = e13;
                    Log.e(TAG, "readNetworkHistory: failed to read, " + e3, e3);
                    return;
                } catch (StringIndexOutOfBoundsException e14) {
                    e4 = e14;
                    Log.e(TAG, "readNetworkHistory: read file line length error, " + e4, e4);
                    return;
                }
            }
            throw th;
        }
    }

    public boolean isValid(WifiConfiguration config) {
        if (config.allowedKeyManagement == null) {
            return false;
        }
        if (config.allowedKeyManagement.cardinality() > 1) {
            if (config.allowedKeyManagement.cardinality() == 2 && config.allowedKeyManagement.get(2)) {
                return config.allowedKeyManagement.get(3) || (config.allowedKeyManagement.get(1) ^ 1) == 0;
            } else {
                return false;
            }
        }
    }

    private static String makeString(BitSet set, String[] strings) {
        StringBuffer buf = new StringBuffer();
        int nextSetBit = -1;
        set = set.get(0, strings.length);
        while (true) {
            nextSetBit = set.nextSetBit(nextSetBit + 1);
            if (nextSetBit == -1) {
                break;
            }
            buf.append(strings[nextSetBit].replace('_', '-')).append(' ');
        }
        if (set.cardinality() > 0) {
            buf.setLength(buf.length() - 1);
        }
        return buf.toString();
    }

    protected void logv(String s) {
        Log.v(TAG, s);
    }

    protected void logd(String s) {
        Log.d(TAG, s);
    }

    protected void log(String s) {
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

    private ScanDetailCache getScanDetailCache(WifiConfiguration config, Map<Integer, ScanDetailCache> scanDetailCaches) {
        if (config == null || scanDetailCaches == null) {
            return null;
        }
        ScanDetailCache cache = (ScanDetailCache) scanDetailCaches.get(Integer.valueOf(config.networkId));
        if (cache == null && config.networkId != -1) {
            cache = new ScanDetailCache(config, WifiConfigManager.SCAN_CACHE_ENTRIES_MAX_SIZE, 128);
            scanDetailCaches.put(Integer.valueOf(config.networkId), cache);
        }
        return cache;
    }
}
