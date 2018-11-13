package com.android.server.wifi;

import android.content.Context;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiSsid;
import android.net.wifi.WpsInfo;
import android.net.wifi.WpsResult;
import android.net.wifi.WpsResult.Status;
import android.os.FileObserver;
import android.security.Credentials;
import android.security.KeyChain;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.util.TelephonyUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

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
public class WifiConfigStore {
    private static boolean DBG = false;
    public static final String ID_STRING_KEY_CONFIG_KEY = "configKey";
    public static final String ID_STRING_KEY_CREATOR_UID = "creatorUid";
    public static final String ID_STRING_KEY_FQDN = "fqdn";
    public static final String ID_STRING_VAR_NAME = "id_str";
    public static final int STORED_VALUE_FOR_REQUIRE_PMF = 2;
    public static final String SUPPLICANT_CONFIG_FILE = "/data/misc/wifi/wpa_supplicant.conf";
    public static final String SUPPLICANT_CONFIG_FILE_BACKUP = "/data/misc/wifi/wpa_supplicant.conf.tmp";
    public static final String TAG = "WifiConfigStore";
    private static boolean VDBG;
    private final BackupManagerProxy mBackupManagerProxy;
    private final HashSet<String> mBssidBlacklist;
    private final Context mContext;
    private final WpaConfigFileObserver mFileObserver;
    private final KeyStore mKeyStore;
    private final LocalLog mLocalLog;
    private boolean mShowNetworks;
    private final WifiNative mWifiNative;

    private class SupplicantLoader implements android.net.wifi.WifiEnterpriseConfig.SupplicantLoader {
        private final int mNetId;

        SupplicantLoader(int netId) {
            this.mNetId = netId;
        }

        public String loadValue(String key) {
            String value = WifiConfigStore.this.mWifiNative.getNetworkVariable(this.mNetId, key);
            if (TextUtils.isEmpty(value)) {
                return null;
            }
            if (!enterpriseConfigKeyShouldBeQuoted(key)) {
                value = WifiConfigStore.removeDoubleQuotes(value);
            }
            return value;
        }

        private boolean enterpriseConfigKeyShouldBeQuoted(String key) {
            if (key.equals("eap") || key.equals("engine")) {
                return false;
            }
            return true;
        }
    }

    private class SupplicantSaver implements android.net.wifi.WifiEnterpriseConfig.SupplicantSaver {
        private final int mNetId;
        private final String mSetterSSID;

        SupplicantSaver(int netId, String setterSSID) {
            this.mNetId = netId;
            this.mSetterSSID = setterSSID;
        }

        public boolean saveValue(String key, String value) {
            if ((key.equals("password") && value != null && value.equals("*")) || key.equals("realm") || key.equals("plmn")) {
                return true;
            }
            if (value == null) {
                value = "\"\"";
            }
            if (WifiConfigStore.this.mWifiNative.setNetworkVariable(this.mNetId, key, value)) {
                return true;
            }
            WifiConfigStore.this.loge(this.mSetterSSID + ": failed to set " + key + ": " + value);
            return false;
        }
    }

    private class WpaConfigFileObserver extends FileObserver {
        WpaConfigFileObserver() {
            super(WifiConfigStore.SUPPLICANT_CONFIG_FILE, 8);
        }

        public void onEvent(int event, String path) {
            if (event == 8) {
                File file = new File(WifiConfigStore.SUPPLICANT_CONFIG_FILE);
                if (WifiConfigStore.VDBG) {
                    WifiConfigStore.this.localLog("wpa_supplicant.conf changed; new size = " + file.length());
                }
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiConfigStore.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.WifiConfigStore.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiConfigStore.<clinit>():void");
    }

    WifiConfigStore(Context context, WifiNative wifiNative, KeyStore keyStore, LocalLog localLog, boolean showNetworks, boolean verboseDebug) {
        this.mBssidBlacklist = new HashSet();
        this.mContext = context;
        this.mWifiNative = wifiNative;
        this.mKeyStore = keyStore;
        this.mShowNetworks = showNetworks;
        this.mBackupManagerProxy = new BackupManagerProxy();
        this.mLocalLog = localLog;
        this.mFileObserver = new WpaConfigFileObserver();
        if (this.mShowNetworks) {
            this.mFileObserver.startWatching();
        }
        VDBG = verboseDebug;
    }

    private static String removeDoubleQuotes(String string) {
        int length = string.length();
        if (length > 1 && string.charAt(0) == '\"' && string.charAt(length - 1) == '\"') {
            return string.substring(1, length - 1);
        }
        return string;
    }

    private static String makeString(BitSet set, String[] strings) {
        return makeStringWithException(set, strings, null);
    }

    private static String makeStringWithException(BitSet set, String[] strings, String exception) {
        StringBuilder result = new StringBuilder();
        BitSet trimmedSet = set.get(0, strings.length);
        List<String> valueSet = new ArrayList();
        for (int bit = trimmedSet.nextSetBit(0); bit >= 0; bit = trimmedSet.nextSetBit(bit + 1)) {
            String currentName = strings[bit];
            if (exception == null || !currentName.equals(exception)) {
                valueSet.add(currentName.replace('_', '-'));
            } else {
                valueSet.add(currentName);
            }
        }
        return TextUtils.join(" ", valueSet);
    }

    private static String encodeSSID(String str) {
        return Utils.toHex(removeDoubleQuotes(str).getBytes(StandardCharsets.UTF_8));
    }

    private static String encodeSSID(String str, boolean isGbkEncoding) {
        Log.d(TAG, "isGbkEncoding: " + isGbkEncoding);
        if (isGbkEncoding) {
            try {
                return Utils.toHex(removeDoubleQuotes(str).getBytes("GBK"));
            } catch (Exception e) {
                Log.d(TAG, "UnsupportedEncodingException: " + e.toString());
            }
        }
        return Utils.toHex(removeDoubleQuotes(str).getBytes(StandardCharsets.UTF_8));
    }

    private static boolean needsKeyStore(WifiEnterpriseConfig config) {
        return (config.getClientCertificate() == null && config.getCaCertificate() == null) ? false : true;
    }

    private static boolean isHardwareBackedKey(PrivateKey key) {
        return KeyChain.isBoundKeyAlgorithm(key.getAlgorithm());
    }

    private static boolean hasHardwareBackedKey(Certificate certificate) {
        return KeyChain.isBoundKeyAlgorithm(certificate.getPublicKey().getAlgorithm());
    }

    private static boolean needsSoftwareBackedKeyStore(WifiEnterpriseConfig config) {
        if (TextUtils.isEmpty(config.getClientCertificateAlias())) {
            return false;
        }
        return true;
    }

    private int lookupString(String string, String[] strings) {
        int size = strings.length;
        string = string.replace('-', '_');
        for (int i = 0; i < size; i++) {
            if (string.equals(strings[i])) {
                return i;
            }
        }
        loge("Failed to look-up a string: " + string);
        return -1;
    }

    private void readNetworkBitsetVariable(int netId, BitSet variable, String varName, String[] strings) {
        String value = this.mWifiNative.getNetworkVariable(netId, varName);
        if (!TextUtils.isEmpty(value)) {
            variable.clear();
            for (String val : value.split(" ")) {
                int index = lookupString(val, strings);
                if (index >= 0) {
                    variable.set(index);
                }
            }
        }
    }

    public void readNetworkVariables(WifiConfiguration config) {
        boolean z = true;
        if (config != null) {
            if (VDBG) {
                localLog("readNetworkVariables: " + config.networkId);
            }
            int netId = config.networkId;
            if (netId >= 0) {
                String value = this.mWifiNative.getNetworkVariable(netId, "ssid");
                if (TextUtils.isEmpty(value)) {
                    config.SSID = null;
                } else if (value.charAt(0) != '\"') {
                    config.SSID = "\"" + WifiSsid.createFromHex(value).toString() + "\"";
                } else {
                    config.SSID = value;
                }
                value = this.mWifiNative.getNetworkVariable(netId, "bssid");
                if (TextUtils.isEmpty(value)) {
                    config.getNetworkSelectionStatus().setNetworkSelectionBSSID(null);
                    config.BSSID = null;
                } else {
                    config.getNetworkSelectionStatus().setNetworkSelectionBSSID(value);
                    config.BSSID = value;
                }
                value = this.mWifiNative.getNetworkVariable(netId, "priority");
                config.priority = -1;
                if (!TextUtils.isEmpty(value)) {
                    try {
                        config.priority = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                    }
                }
                value = this.mWifiNative.getNetworkVariable(netId, "scan_ssid");
                config.hiddenSSID = false;
                if (!TextUtils.isEmpty(value)) {
                    try {
                        config.hiddenSSID = Integer.parseInt(value) != 0;
                    } catch (NumberFormatException e2) {
                    }
                }
                value = this.mWifiNative.getNetworkVariable(netId, "ieee80211w");
                config.requirePMF = false;
                if (!TextUtils.isEmpty(value)) {
                    try {
                        if (Integer.parseInt(value) != 2) {
                            z = false;
                        }
                        config.requirePMF = z;
                    } catch (NumberFormatException e3) {
                    }
                }
                value = this.mWifiNative.getNetworkVariable(netId, "wep_tx_keyidx");
                config.wepTxKeyIndex = -1;
                if (!TextUtils.isEmpty(value)) {
                    try {
                        config.wepTxKeyIndex = Integer.parseInt(value);
                    } catch (NumberFormatException e4) {
                    }
                }
                for (int i = 0; i < 4; i++) {
                    value = this.mWifiNative.getNetworkVariable(netId, WifiConfiguration.wepKeyVarNames[i]);
                    if (TextUtils.isEmpty(value)) {
                        config.wepKeys[i] = null;
                    } else {
                        config.wepKeys[i] = value;
                    }
                }
                value = this.mWifiNative.getNetworkVariable(netId, "psk");
                if (TextUtils.isEmpty(value)) {
                    config.preSharedKey = null;
                } else {
                    config.preSharedKey = value;
                }
                readNetworkBitsetVariable(config.networkId, config.allowedProtocols, "proto", Protocol.strings);
                readNetworkBitsetVariable(config.networkId, config.allowedKeyManagement, "key_mgmt", KeyMgmt.strings);
                readNetworkBitsetVariable(config.networkId, config.allowedAuthAlgorithms, "auth_alg", AuthAlgorithm.strings);
                readNetworkBitsetVariable(config.networkId, config.allowedPairwiseCiphers, "pairwise", PairwiseCipher.strings);
                readNetworkBitsetVariable(config.networkId, config.allowedGroupCiphers, "group", GroupCipher.strings);
                if (config.enterpriseConfig == null) {
                    config.enterpriseConfig = new WifiEnterpriseConfig();
                }
                config.enterpriseConfig.loadFromSupplicant(new SupplicantLoader(netId));
                value = this.mWifiNative.getNetworkVariable(netId, "sim_num");
                if (TextUtils.isEmpty(value)) {
                    config.simSlot = null;
                } else {
                    config.simSlot = value;
                }
                value = this.mWifiNative.getNetworkVariable(netId, "pac_file");
                if (TextUtils.isEmpty(value)) {
                    config.pacFile = null;
                } else {
                    config.pacFile = value;
                }
                value = this.mWifiNative.getNetworkVariable(netId, "phase1");
                if (TextUtils.isEmpty(value)) {
                    config.phase1 = null;
                } else {
                    config.phase1 = value;
                }
            }
        }
    }

    public int loadNetworks(Map<String, WifiConfiguration> configs, SparseArray<Map<String, String>> networkExtras) {
        int lastPriority = 0;
        int last_id = -1;
        boolean done = false;
        while (!done) {
            String listStr = this.mWifiNative.listNetworks(last_id);
            if (listStr == null) {
                return lastPriority;
            }
            String[] lines = listStr.split("\n");
            if (this.mShowNetworks) {
                localLog("loadNetworks:  ");
                for (String net : lines) {
                    localLog(net);
                }
            }
            for (int i = 1; i < lines.length; i++) {
                String[] result = lines[i].split("\t");
                WifiConfiguration config = new WifiConfiguration();
                try {
                    config.networkId = Integer.parseInt(result[0]);
                    last_id = config.networkId;
                    config.status = 1;
                    readNetworkVariables(config);
                    Map<String, String> extras = this.mWifiNative.getNetworkExtra(config.networkId, ID_STRING_VAR_NAME);
                    if (extras == null) {
                        extras = new HashMap();
                        String fqdn = Utils.unquote(this.mWifiNative.getNetworkVariable(config.networkId, ID_STRING_VAR_NAME));
                        if (fqdn != null) {
                            extras.put(ID_STRING_KEY_FQDN, fqdn);
                            config.FQDN = fqdn;
                            config.providerFriendlyName = "";
                        }
                    }
                    networkExtras.put(config.networkId, extras);
                    if (config.priority > lastPriority) {
                        lastPriority = config.priority;
                    }
                    config.setIpAssignment(IpAssignment.DHCP);
                    config.setProxySettings(ProxySettings.NONE);
                    if (WifiServiceImpl.isValid(config)) {
                        String configKey = (String) extras.get(ID_STRING_KEY_CONFIG_KEY);
                        if (configKey == null) {
                            configKey = config.configKey();
                            saveNetworkMetadata(config);
                        }
                        WifiConfiguration duplicateConfig = (WifiConfiguration) configs.put(configKey, config);
                        if (duplicateConfig != null) {
                            if (this.mShowNetworks) {
                                localLog("Replacing duplicate network " + duplicateConfig.networkId + " with " + config.networkId + ".");
                            }
                            this.mWifiNative.removeNetwork(duplicateConfig.networkId);
                        }
                    } else if (this.mShowNetworks) {
                        localLog("Ignoring network " + config.networkId + " because configuration " + "loaded from wpa_supplicant.conf is not valid.");
                    }
                } catch (NumberFormatException e) {
                    loge("Failed to read network-id '" + result[0] + "'");
                }
            }
            done = lines.length == 1;
        }
        return lastPriority;
    }

    private boolean installKeys(WifiEnterpriseConfig existingConfig, WifiEnterpriseConfig config, String name) {
        boolean ret = true;
        String privKeyName = "USRPKEY_" + name;
        String userCertName = "USRCERT_" + name;
        if (config.getClientCertificate() != null) {
            byte[] privKeyData = config.getClientPrivateKey().getEncoded();
            if (DBG) {
                if (isHardwareBackedKey(config.getClientPrivateKey())) {
                    Log.d(TAG, "importing keys " + name + " in hardware backed store");
                } else {
                    Log.d(TAG, "importing keys " + name + " in software backed store");
                }
            }
            ret = this.mKeyStore.importKey(privKeyName, privKeyData, 1010, 0);
            if (!ret) {
                return ret;
            }
            ret = putCertInKeyStore(userCertName, config.getClientCertificate());
            if (!ret) {
                this.mKeyStore.delete(privKeyName, 1010);
                return ret;
            }
        }
        X509Certificate[] caCertificates = config.getCaCertificates();
        Set<String> oldCaCertificatesToRemove = new ArraySet();
        if (!(existingConfig == null || existingConfig.getCaCertificateAliases() == null)) {
            oldCaCertificatesToRemove.addAll(Arrays.asList(existingConfig.getCaCertificateAliases()));
        }
        List caCertificateAliases = null;
        if (caCertificates != null) {
            List<String> caCertificateAliases2 = new ArrayList();
            int i = 0;
            while (i < caCertificates.length) {
                String alias;
                if (caCertificates.length == 1) {
                    alias = name;
                } else {
                    Object[] objArr = new Object[2];
                    objArr[0] = name;
                    objArr[1] = Integer.valueOf(i);
                    alias = String.format("%s_%d", objArr);
                }
                oldCaCertificatesToRemove.remove(alias);
                ret = putCertInKeyStore("CACERT_" + alias, caCertificates[i]);
                if (ret) {
                    caCertificateAliases2.add(alias);
                    i++;
                } else {
                    if (config.getClientCertificate() != null) {
                        this.mKeyStore.delete(privKeyName, 1010);
                        this.mKeyStore.delete(userCertName, 1010);
                    }
                    for (String addedAlias : caCertificateAliases2) {
                        this.mKeyStore.delete("CACERT_" + addedAlias, 1010);
                    }
                    return ret;
                }
            }
        }
        for (String oldAlias : oldCaCertificatesToRemove) {
            this.mKeyStore.delete("CACERT_" + oldAlias, 1010);
        }
        if (config.getClientCertificate() != null) {
            config.setClientCertificateAlias(name);
            config.resetClientKeyEntry();
        }
        if (caCertificates != null) {
            config.setCaCertificateAliases((String[]) caCertificateAliases2.toArray(new String[caCertificateAliases2.size()]));
            config.resetCaCertificate();
        }
        String privKeyName2 = "keystore://WAPIUSERCERT_" + name;
        String userCertName2 = "WAPIUSERCERT_" + name;
        String caCertName2 = "WAPISERVERCERT_" + name;
        if (config.getClientCertificate() != null) {
            byte[] privKeyData2 = config.getClientPrivateKey().getEncoded();
            if (isHardwareBackedKey(config.getClientPrivateKey())) {
                if (DBG) {
                    Log.d(TAG, "importing keys " + name + " in hardware backed store");
                }
                ret = this.mKeyStore.importKey(privKeyName2, privKeyData2, 1010, 0);
            } else {
                if (DBG) {
                    Log.d(TAG, "importing keys " + name + " in software backed store");
                }
                ret = this.mKeyStore.importKey(privKeyName, privKeyData2, 1010, 1);
            }
            if (!ret) {
                return ret;
            }
            ret = putCertInKeyStore(userCertName2, config.getClientCertificate());
            if (!ret) {
                return ret;
            }
        }
        if (config.getCaCertificate() != null) {
            ret = putCertInKeyStore(caCertName2, config.getCaCertificate());
            if (!ret) {
                if (config.getClientCertificate() != null) {
                    this.mKeyStore.delete(userCertName2, 1010);
                }
                return ret;
            }
        }
        if (config.getClientCertificate() != null) {
            config.setClientCertificateWapiAlias(name);
            config.resetClientKeyEntry();
        }
        if (config.getCaCertificate() != null) {
            config.setCaCertificateWapiAlias(name);
            config.resetCaCertificate();
        }
        return ret;
    }

    private boolean putCertInKeyStore(String name, Certificate cert) {
        try {
            Certificate[] certificateArr = new Certificate[1];
            certificateArr[0] = cert;
            byte[] certData = Credentials.convertToPem(certificateArr);
            if (DBG) {
                Log.d(TAG, "putting certificate " + name + " in keystore");
            }
            return this.mKeyStore.put(name, certData, 1010, 0);
        } catch (IOException e) {
            return false;
        } catch (CertificateException e2) {
            return false;
        }
    }

    private void removeKeys(WifiEnterpriseConfig config) {
        String client = config.getClientCertificateAlias();
        if (!TextUtils.isEmpty(client)) {
            if (DBG) {
                Log.d(TAG, "removing client private key and user cert");
            }
            this.mKeyStore.delete("USRPKEY_" + client, 1010);
            this.mKeyStore.delete("USRCERT_" + client, 1010);
        }
        String[] aliases = config.getCaCertificateAliases();
        if (aliases != null) {
            for (String ca : aliases) {
                if (!TextUtils.isEmpty(ca)) {
                    if (DBG) {
                        Log.d(TAG, "removing CA cert: " + ca);
                    }
                    this.mKeyStore.delete("CACERT_" + ca, 1010);
                }
            }
        }
        String client2 = config.getClientCertificateWapiAlias();
        if (!TextUtils.isEmpty(client2)) {
            this.mKeyStore.delete("WAPIUSERCERT_" + client2, 1010);
        }
        String ca2 = config.getCaCertificateWapiAlias();
        if (!TextUtils.isEmpty(ca2)) {
            this.mKeyStore.delete("WAPISERVERCERT_" + ca2, 1010);
        }
    }

    public boolean saveNetworkMetadata(WifiConfiguration config) {
        Map<String, String> metadata = new HashMap();
        if (config.isPasspoint()) {
            metadata.put(ID_STRING_KEY_FQDN, config.FQDN);
        }
        metadata.put(ID_STRING_KEY_CONFIG_KEY, config.configKey());
        metadata.put(ID_STRING_KEY_CREATOR_UID, Integer.toString(config.creatorUid));
        if (this.mWifiNative.setNetworkExtra(config.networkId, ID_STRING_VAR_NAME, metadata)) {
            return true;
        }
        loge("failed to set id_str: " + metadata.toString());
        return false;
    }

    private boolean saveNetwork(WifiConfiguration config, int netId) {
        if (config == null) {
            return false;
        }
        if (VDBG) {
            localLog("saveNetwork: " + netId);
        }
        if (config.BSSID != null && !this.mWifiNative.setNetworkVariable(netId, "bssid", config.BSSID)) {
            loge("failed to set BSSID: " + config.BSSID);
            return false;
        } else if (config.SSID != null && !this.mWifiNative.setNetworkVariable(netId, "ssid", encodeSSID(config.SSID, config.isGbkEncoding))) {
            loge("failed to set SSID: " + config.SSID);
            return false;
        } else if (!saveNetworkMetadata(config)) {
            return false;
        } else {
            String allowedKeyManagementString = makeString(config.allowedKeyManagement, KeyMgmt.strings);
            if (config.allowedKeyManagement.cardinality() == 0 || this.mWifiNative.setNetworkVariable(netId, "key_mgmt", allowedKeyManagementString)) {
                String allowedProtocolsString = makeString(config.allowedProtocols, Protocol.strings);
                if (config.allowedProtocols.cardinality() == 0 || this.mWifiNative.setNetworkVariable(netId, "proto", allowedProtocolsString)) {
                    String allowedAuthAlgorithmsString = makeString(config.allowedAuthAlgorithms, AuthAlgorithm.strings);
                    if (config.allowedAuthAlgorithms.cardinality() == 0 || this.mWifiNative.setNetworkVariable(netId, "auth_alg", allowedAuthAlgorithmsString)) {
                        String allowedPairwiseCiphersString = makeString(config.allowedPairwiseCiphers, PairwiseCipher.strings);
                        if (config.allowedPairwiseCiphers.cardinality() == 0 || this.mWifiNative.setNetworkVariable(netId, "pairwise", allowedPairwiseCiphersString)) {
                            String allowedGroupCiphersString = makeStringWithException(config.allowedGroupCiphers, GroupCipher.strings, GroupCipher.strings[4]);
                            if (config.allowedGroupCiphers.cardinality() != 0 && !this.mWifiNative.setNetworkVariable(netId, "group", allowedGroupCiphersString)) {
                                loge("failed to set group: " + allowedGroupCiphersString);
                                return false;
                            } else if (config.preSharedKey == null || config.preSharedKey.equals("*") || this.mWifiNative.setNetworkVariable(netId, "psk", config.preSharedKey)) {
                                boolean hasSetKey = false;
                                if (config.wepKeys != null) {
                                    int i = 0;
                                    while (i < config.wepKeys.length) {
                                        if (!(config.wepKeys[i] == null || config.wepKeys[i].equals("*") || config.wepKeys[i].equals(""))) {
                                            if (this.mWifiNative.setNetworkVariable(netId, WifiConfiguration.wepKeyVarNames[i], config.wepKeys[i])) {
                                                hasSetKey = true;
                                            } else {
                                                loge("failed to set wep_key" + i + ": " + config.wepKeys[i]);
                                                return false;
                                            }
                                        }
                                        i++;
                                    }
                                }
                                if (hasSetKey && !this.mWifiNative.setNetworkVariable(netId, "wep_tx_keyidx", Integer.toString(config.wepTxKeyIndex))) {
                                    loge("failed to set wep_tx_keyidx: " + config.wepTxKeyIndex);
                                    return false;
                                } else if (this.mWifiNative.setNetworkVariable(netId, "priority", Integer.toString(config.priority))) {
                                    if (config.hiddenSSID) {
                                        if (!this.mWifiNative.setNetworkVariable(netId, "scan_ssid", Integer.toString(config.hiddenSSID ? 1 : 0))) {
                                            loge(config.SSID + ": failed to set hiddenSSID: " + config.hiddenSSID);
                                            return false;
                                        }
                                    }
                                    if (config.requirePMF && !this.mWifiNative.setNetworkVariable(netId, "ieee80211w", Integer.toString(2))) {
                                        loge(config.SSID + ": failed to set requirePMF: " + config.requirePMF);
                                        return false;
                                    } else if (config.updateIdentifier == null || this.mWifiNative.setNetworkVariable(netId, "update_identifier", config.updateIdentifier)) {
                                        return true;
                                    } else {
                                        loge(config.SSID + ": failed to set updateIdentifier: " + config.updateIdentifier);
                                        return false;
                                    }
                                } else {
                                    loge(config.SSID + ": failed to set priority: " + config.priority);
                                    return false;
                                }
                            } else {
                                loge("failed to set psk");
                                return false;
                            }
                        }
                        loge("failed to set pairwise: " + allowedPairwiseCiphersString);
                        return false;
                    }
                    loge("failed to set auth_alg: " + allowedAuthAlgorithmsString);
                    return false;
                }
                loge("failed to set proto: " + allowedProtocolsString);
                return false;
            }
            loge("failed to set key_mgmt: " + allowedKeyManagementString);
            return false;
        }
    }

    private boolean updateNetworkKeys(WifiConfiguration config, WifiConfiguration existingConfig) {
        WifiEnterpriseConfig wifiEnterpriseConfig = null;
        WifiEnterpriseConfig enterpriseConfig = config.enterpriseConfig;
        if (needsKeyStore(enterpriseConfig)) {
            try {
                String keyId = config.getKeyIdForCredentials(existingConfig);
                if (existingConfig != null) {
                    wifiEnterpriseConfig = existingConfig.enterpriseConfig;
                }
                if (!installKeys(wifiEnterpriseConfig, enterpriseConfig, keyId)) {
                    loge(config.SSID + ": failed to install keys");
                    return false;
                }
            } catch (IllegalStateException e) {
                loge(config.SSID + " invalid config for key installation: " + e.getMessage());
                return false;
            }
        }
        if (config.simSlot != null && !this.mWifiNative.setNetworkVariable(config.networkId, "sim_num", removeDoubleQuotes(config.simSlot))) {
            Log.e(TAG, "failed to set simSlot: " + removeDoubleQuotes(config.simSlot));
            return false;
        } else if (config.pacFile != null && !this.mWifiNative.setNetworkVariable(config.networkId, "pac_file", config.pacFile)) {
            Log.e(TAG, "failed to set pacFile: " + config.pacFile);
            return false;
        } else if (config.phase1 != null && !this.mWifiNative.setNetworkVariable(config.networkId, "phase1", config.phase1)) {
            Log.e(TAG, "failed to set phase1: " + config.phase1);
            return false;
        } else if (enterpriseConfig.saveToSupplicant(new SupplicantSaver(config.networkId, config.SSID), config)) {
            return true;
        } else {
            removeKeys(enterpriseConfig);
            return false;
        }
    }

    public boolean addOrUpdateNetwork(WifiConfiguration config, WifiConfiguration existingConfig) {
        if (config == null) {
            return false;
        }
        if (VDBG) {
            localLog("addOrUpdateNetwork: " + config.networkId);
        }
        int netId = config.networkId;
        boolean newNetwork = false;
        if (netId == -1) {
            newNetwork = true;
            netId = this.mWifiNative.addNetwork();
            if (netId < 0) {
                loge("Failed to add a network!");
                return false;
            }
            logi("addOrUpdateNetwork created netId=" + netId);
            config.networkId = netId;
        }
        if (!saveNetwork(config, netId)) {
            if (newNetwork) {
                this.mWifiNative.removeNetwork(netId);
                loge("Failed to set a network variable, removed network: " + netId);
            }
            return false;
        } else if ((config.enterpriseConfig != null && config.enterpriseConfig.getEapMethod() != -1) || config.isWapi()) {
            return updateNetworkKeys(config, existingConfig);
        } else {
            this.mBackupManagerProxy.notifyDataChanged();
            return true;
        }
    }

    public boolean removeNetwork(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        if (VDBG) {
            localLog("removeNetwork: " + config.networkId);
        }
        if (this.mWifiNative.removeNetwork(config.networkId)) {
            if (config.enterpriseConfig != null) {
                removeKeys(config.enterpriseConfig);
            }
            this.mBackupManagerProxy.notifyDataChanged();
            return true;
        }
        loge("Remove network in wpa_supplicant failed on " + config.networkId);
        return false;
    }

    public boolean selectNetwork(WifiConfiguration config, Collection<WifiConfiguration> configs) {
        if (config == null) {
            return false;
        }
        if (VDBG) {
            localLog("selectNetwork: " + config.networkId);
        }
        if (this.mWifiNative.selectNetwork(config.networkId)) {
            config.status = 2;
            markAllNetworksDisabledExcept(config.networkId, configs);
            return true;
        }
        loge("Select network in wpa_supplicant failed on " + config.networkId);
        return false;
    }

    boolean disableNetwork(WifiConfiguration config) {
        if (config == null) {
            return false;
        }
        if (VDBG) {
            localLog("disableNetwork: " + config.networkId);
        }
        if (this.mWifiNative.disableNetwork(config.networkId)) {
            config.status = 1;
            return true;
        }
        loge("Disable network in wpa_supplicant failed on " + config.networkId);
        return false;
    }

    public boolean setNetworkPriority(WifiConfiguration config, int priority) {
        if (config == null) {
            return false;
        }
        if (VDBG) {
            localLog("setNetworkPriority: " + config.networkId);
        }
        if (this.mWifiNative.setNetworkVariable(config.networkId, "priority", Integer.toString(priority))) {
            config.priority = priority;
            return true;
        }
        loge("Set priority of network in wpa_supplicant failed on " + config.networkId);
        return false;
    }

    public boolean setNetworkSSID(WifiConfiguration config, String ssid) {
        if (config == null) {
            return false;
        }
        if (VDBG) {
            localLog("setNetworkSSID: " + config.networkId);
        }
        if (this.mWifiNative.setNetworkVariable(config.networkId, "ssid", encodeSSID(ssid))) {
            config.SSID = ssid;
            return true;
        }
        loge("Set SSID of network in wpa_supplicant failed on " + config.networkId);
        return false;
    }

    public boolean setNetworkBSSID(WifiConfiguration config, String bssid) {
        if (config == null || (config.networkId == -1 && config.SSID == null)) {
            return false;
        }
        if (config.BSSID != null && config.BSSID != WifiLastResortWatchdog.BSSID_ANY) {
            return false;
        }
        if (VDBG) {
            localLog("setNetworkBSSID: " + config.networkId);
        }
        if (this.mWifiNative.setNetworkVariable(config.networkId, "bssid", bssid)) {
            config.getNetworkSelectionStatus().setNetworkSelectionBSSID(bssid);
            return true;
        }
        loge("Set BSSID of network in wpa_supplicant failed on " + config.networkId);
        return false;
    }

    public void enableHS20(boolean enable) {
        this.mWifiNative.setHs20(enable);
    }

    public boolean disableAllNetworks(Collection<WifiConfiguration> configs) {
        if (VDBG) {
            localLog("disableAllNetworks");
        }
        boolean networkDisabled = false;
        for (WifiConfiguration enabled : configs) {
            if (disableNetwork(enabled)) {
                networkDisabled = true;
            }
        }
        saveConfig();
        return networkDisabled;
    }

    public boolean saveConfig() {
        return this.mWifiNative.saveConfig();
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0040 A:{Catch:{ all -> 0x00ba }} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0059 A:{SYNTHETIC, Splitter: B:20:0x0059} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00bd A:{SYNTHETIC, Splitter: B:40:0x00bd} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Map<String, String> readNetworkVariablesFromSupplicantFile(String key) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        Map<String, String> result = new HashMap();
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(SUPPLICANT_CONFIG_FILE));
            try {
                result = readNetworkVariablesFromReader(reader2, key);
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e3) {
                        if (VDBG) {
                            loge("Could not close reader for /data/misc/wifi/wpa_supplicant.conf, " + e3);
                        }
                    }
                }
                reader = reader2;
            } catch (FileNotFoundException e4) {
                e2 = e4;
                reader = reader2;
            } catch (IOException e5) {
                e3 = e5;
                reader = reader2;
                try {
                    if (VDBG) {
                    }
                    if (reader != null) {
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e32) {
                            if (VDBG) {
                                loge("Could not close reader for /data/misc/wifi/wpa_supplicant.conf, " + e32);
                            }
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            if (VDBG) {
                loge("Could not open /data/misc/wifi/wpa_supplicant.conf, " + e2);
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e322) {
                    if (VDBG) {
                        loge("Could not close reader for /data/misc/wifi/wpa_supplicant.conf, " + e322);
                    }
                }
            }
            return result;
        } catch (IOException e7) {
            e322 = e7;
            if (VDBG) {
                loge("Could not read /data/misc/wifi/wpa_supplicant.conf, " + e322);
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3222) {
                    if (VDBG) {
                        loge("Could not close reader for /data/misc/wifi/wpa_supplicant.conf, " + e3222);
                    }
                }
            }
            return result;
        }
        return result;
    }

    public Map<String, String> readNetworkVariablesFromReader(BufferedReader reader, String key) throws IOException {
        Map<String, String> result = new HashMap();
        if (VDBG) {
            localLog("readNetworkVariablesFromReader key=" + key);
        }
        boolean found = false;
        String configKey = null;
        String value = null;
        String line = reader.readLine();
        while (line != null) {
            if (line.matches("[ \\t]*network=\\{")) {
                found = true;
                configKey = null;
                value = null;
            } else if (line.matches("[ \\t]*\\}")) {
                found = false;
                configKey = null;
                value = null;
            }
            if (found) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("id_str=")) {
                    try {
                        JSONObject json = new JSONObject(URLDecoder.decode(trimmedLine.substring(8, trimmedLine.length() - 1), "UTF-8"));
                        if (json.has(ID_STRING_KEY_CONFIG_KEY)) {
                            Object configKeyFromJson = json.get(ID_STRING_KEY_CONFIG_KEY);
                            if (configKeyFromJson instanceof String) {
                                configKey = (String) configKeyFromJson;
                            }
                        }
                    } catch (JSONException e) {
                        if (VDBG) {
                            loge("Could not get configKey, " + e);
                        }
                    }
                }
                if (trimmedLine.startsWith(key + "=")) {
                    value = trimmedLine.substring(key.length() + 1);
                }
                if (!(configKey == null || value == null)) {
                    result.put(configKey, value);
                }
            }
            line = reader.readLine();
        }
        return result;
    }

    public void resetSimNetworks(Collection<WifiConfiguration> configs, int simSlot) {
        if (VDBG) {
            localLog("resetSimNetworks, simSlot: " + simSlot);
        }
        for (WifiConfiguration config : configs) {
            if (TelephonyUtil.isSimConfig(config) && WifiConfigurationUtil.getIntSimSlot(config) == simSlot) {
                String currentIdentity = TelephonyUtil.getSimIdentity(this.mContext, config.enterpriseConfig.getEapMethod(), TelephonyUtil.getSubId(simSlot));
                String supplicantIdentity = this.mWifiNative.getNetworkVariable(config.networkId, "identity");
                if (supplicantIdentity != null) {
                    supplicantIdentity = removeDoubleQuotes(supplicantIdentity);
                }
                if (currentIdentity == null || !currentIdentity.equals(supplicantIdentity)) {
                    this.mWifiNative.setNetworkVariable(config.networkId, "identity", "NULL");
                    this.mWifiNative.setNetworkVariable(config.networkId, "anonymous_identity", "NULL");
                }
                config.enterpriseConfig.setIdentity(currentIdentity);
                config.enterpriseConfig.setAnonymousIdentity("");
            }
        }
    }

    public void clearBssidBlacklist() {
        if (VDBG) {
            localLog("clearBlacklist");
        }
        this.mBssidBlacklist.clear();
        this.mWifiNative.clearBlacklist();
        this.mWifiNative.setBssidBlacklist(null);
    }

    public void blackListBssid(String bssid) {
        if (bssid != null) {
            if (VDBG) {
                localLog("blackListBssid: " + bssid);
            }
            this.mBssidBlacklist.add(bssid);
            this.mWifiNative.addToBlacklist(bssid);
            this.mWifiNative.setBssidBlacklist((String[]) this.mBssidBlacklist.toArray(new String[this.mBssidBlacklist.size()]));
        }
    }

    public boolean isBssidBlacklisted(String bssid) {
        return this.mBssidBlacklist.contains(bssid);
    }

    private void markAllNetworksDisabledExcept(int netId, Collection<WifiConfiguration> configs) {
        for (WifiConfiguration config : configs) {
            if (!(config == null || config.networkId == netId || config.status == 1)) {
                config.status = 1;
            }
        }
    }

    private void markAllNetworksDisabled(Collection<WifiConfiguration> configs) {
        markAllNetworksDisabledExcept(-1, configs);
    }

    public WpsResult startWpsWithPinFromAccessPoint(WpsInfo config, Collection<WifiConfiguration> configs) {
        WpsResult result = new WpsResult();
        if (this.mWifiNative.startWpsRegistrar(config.BSSID, config.pin)) {
            markAllNetworksDisabled(configs);
            result.status = Status.SUCCESS;
        } else {
            loge("Failed to start WPS pin method configuration");
            result.status = Status.FAILURE;
        }
        return result;
    }

    public WpsResult startWpsWithPinFromDevice(WpsInfo config, Collection<WifiConfiguration> configs) {
        WpsResult result = new WpsResult();
        result.pin = this.mWifiNative.startWpsPinDisplay(config.BSSID);
        if (TextUtils.isEmpty(result.pin)) {
            loge("Failed to start WPS pin method configuration");
            result.status = Status.FAILURE;
        } else {
            markAllNetworksDisabled(configs);
            result.status = Status.SUCCESS;
        }
        return result;
    }

    public WpsResult startWpsPbc(WpsInfo config, Collection<WifiConfiguration> configs) {
        WpsResult result = new WpsResult();
        if (this.mWifiNative.startWpsPbc(config.BSSID)) {
            markAllNetworksDisabled(configs);
            result.status = Status.SUCCESS;
        } else {
            loge("Failed to start WPS push button configuration");
            result.status = Status.FAILURE;
        }
        return result;
    }

    protected void logd(String s) {
        Log.d(TAG, s);
    }

    protected void logi(String s) {
        Log.i(TAG, s);
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

    protected void log(String s) {
        Log.d(TAG, s);
    }

    private void localLog(String s) {
        if (this.mLocalLog != null) {
            this.mLocalLog.log("WifiConfigStore: " + s);
        }
        Log.d(TAG, s);
    }

    private void localLogAndLogcat(String s) {
        localLog(s);
        Log.d(TAG, s);
    }

    private boolean isWapiConfig(WifiConfiguration config) {
        boolean isWapi = false;
        int p = 0;
        while (p < config.allowedProtocols.size()) {
            if (config.allowedProtocols.get(p) && p == 3) {
                Log.e(TAG, "this is WAPI");
                isWapi = true;
            }
            p++;
        }
        return isWapi;
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            DBG = true;
            VDBG = true;
            this.mShowNetworks = true;
            this.mFileObserver.startWatching();
            return;
        }
        DBG = false;
        VDBG = false;
        this.mFileObserver.stopWatching();
        this.mShowNetworks = false;
    }
}
