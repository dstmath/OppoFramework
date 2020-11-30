package com.android.server.wifi;

import android.content.Context;
import android.net.IpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.WifiConfigStore;
import com.android.server.wifi.util.XmlUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public abstract class NetworkListStoreData implements WifiConfigStore.StoreData {
    private static final String TAG = "NetworkListStoreData";
    private static final String XML_TAG_SECTION_HEADER_IP_CONFIGURATION = "IpConfiguration";
    private static final String XML_TAG_SECTION_HEADER_NETWORK = "Network";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_LIST = "NetworkList";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_STATUS = "NetworkStatus";
    private static final String XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION = "WifiConfiguration";
    private static final String XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION = "WifiEnterpriseConfiguration";
    private List<WifiConfiguration> mConfigurations;
    private final Context mContext;

    NetworkListStoreData(Context context) {
        this.mContext = context;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void serializeData(XmlSerializer out) throws XmlPullParserException, IOException {
        serializeNetworkList(out, this.mConfigurations);
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void deserializeData(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        if (in != null) {
            this.mConfigurations = parseNetworkList(in, outerTagDepth);
        }
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public void resetData() {
        this.mConfigurations = null;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public boolean hasNewDataToSerialize() {
        return true;
    }

    @Override // com.android.server.wifi.WifiConfigStore.StoreData
    public String getName() {
        return XML_TAG_SECTION_HEADER_NETWORK_LIST;
    }

    public void setConfigurations(List<WifiConfiguration> configs) {
        this.mConfigurations = configs;
    }

    public List<WifiConfiguration> getConfigurations() {
        List<WifiConfiguration> list = this.mConfigurations;
        if (list == null) {
            return new ArrayList();
        }
        return list;
    }

    private void serializeNetworkList(XmlSerializer out, List<WifiConfiguration> networkList) throws XmlPullParserException, IOException {
        if (networkList != null) {
            for (WifiConfiguration network : networkList) {
                serializeNetwork(out, network);
            }
        }
    }

    private void serializeNetwork(XmlSerializer out, WifiConfiguration config) throws XmlPullParserException, IOException {
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_NETWORK);
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION);
        XmlUtil.WifiConfigurationXmlUtil.writeToXmlForConfigStore(out, config);
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION);
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_NETWORK_STATUS);
        XmlUtil.NetworkSelectionStatusXmlUtil.writeToXml(out, config.getNetworkSelectionStatus());
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_NETWORK_STATUS);
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_IP_CONFIGURATION);
        XmlUtil.IpConfigurationXmlUtil.writeToXml(out, config.getIpConfiguration());
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_IP_CONFIGURATION);
        if (!(config.enterpriseConfig == null || config.enterpriseConfig.getEapMethod() == -1)) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION);
            XmlUtil.WifiEnterpriseConfigXmlUtil.writeToXml(out, config.enterpriseConfig);
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION);
        }
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_NETWORK);
    }

    private List<WifiConfiguration> parseNetworkList(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        List<WifiConfiguration> networkList = new ArrayList<>();
        while (XmlUtil.gotoNextSectionWithNameOrEnd(in, XML_TAG_SECTION_HEADER_NETWORK, outerTagDepth)) {
            try {
                WifiConfiguration config = parseNetwork(in, outerTagDepth + 1);
                if (config != null) {
                    networkList.add(config);
                } else {
                    Log.e(TAG, "This config is invalid!");
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to parse network config. Skipping...", e);
            } catch (Exception e2) {
                Log.e(TAG, "parse network abnormally.", e2);
            } catch (Throwable th) {
            }
        }
        return networkList;
    }

    private WifiConfiguration parseNetwork(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        String[] headerName = new String[1];
        WifiEnterpriseConfig enterpriseConfig = null;
        IpConfiguration ipConfiguration = null;
        WifiConfiguration.NetworkSelectionStatus status = null;
        Pair<String, WifiConfiguration> parsedConfig = null;
        while (XmlUtil.gotoNextSectionOrEnd(in, headerName, outerTagDepth)) {
            String str = headerName[0];
            char c = 65535;
            switch (str.hashCode()) {
                case -148477024:
                    if (str.equals(XML_TAG_SECTION_HEADER_NETWORK_STATUS)) {
                        c = 1;
                        break;
                    }
                    break;
                case 46473153:
                    if (str.equals(XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION)) {
                        c = 0;
                        break;
                    }
                    break;
                case 325854959:
                    if (str.equals(XML_TAG_SECTION_HEADER_IP_CONFIGURATION)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1285464096:
                    if (str.equals(XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION)) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c != 0) {
                if (c != 1) {
                    if (c != 2) {
                        if (c != 3) {
                            return null;
                        }
                        if (enterpriseConfig != null) {
                            Log.d(TAG, "Detected duplicate tag for: WifiEnterpriseConfiguration");
                            return null;
                        }
                        try {
                            enterpriseConfig = XmlUtil.WifiEnterpriseConfigXmlUtil.parseFromXml(in, outerTagDepth + 1);
                        } catch (XmlPullParserException e) {
                            Log.e(TAG, "parse enterpriseConfig abnormally");
                            return null;
                        }
                    } else if (ipConfiguration != null) {
                        Log.d(TAG, "Detected duplicate tag for: IpConfiguration");
                        return null;
                    } else {
                        try {
                            ipConfiguration = XmlUtil.IpConfigurationXmlUtil.parseFromXml(in, outerTagDepth + 1);
                        } catch (XmlPullParserException e2) {
                            Log.e(TAG, "parse ipConfiguration abnormally");
                            return null;
                        }
                    }
                } else if (status != null) {
                    Log.e(TAG, "Detected duplicate tag for: NetworkStatus");
                    return null;
                } else {
                    try {
                        status = XmlUtil.NetworkSelectionStatusXmlUtil.parseFromXml(in, outerTagDepth + 1);
                    } catch (XmlPullParserException e3) {
                        Log.e(TAG, "parse status abnormally");
                        return null;
                    }
                }
            } else if (parsedConfig != null) {
                Log.e(TAG, "Detected duplicate tag for: WifiConfiguration");
                return null;
            } else {
                try {
                    parsedConfig = XmlUtil.WifiConfigurationXmlUtil.parseFromXml(in, outerTagDepth + 1);
                } catch (XmlPullParserException e4) {
                    Log.e(TAG, "parse WifiConfiguration abnormally");
                    parsedConfig = null;
                }
            }
        }
        if (parsedConfig == null || parsedConfig.first == null || parsedConfig.second == null) {
            Log.e(TAG, "XML parsing of wifi configuration failed");
            return null;
        }
        String configKeyParsed = (String) parsedConfig.first;
        WifiConfiguration configuration = (WifiConfiguration) parsedConfig.second;
        String configKeyCalculated = configuration.configKey();
        if (!configKeyParsed.equals(configKeyCalculated)) {
            Log.e(TAG, "Configuration key does not match. Retrieved: " + configKeyParsed + ", Calculated: " + configKeyCalculated);
            return null;
        }
        String creatorName = this.mContext.getPackageManager().getNameForUid(configuration.creatorUid);
        if (creatorName == null) {
            Log.e(TAG, "Invalid creatorUid for saved network " + configuration.configKey() + ", creatorUid=" + configuration.creatorUid);
            configuration.creatorUid = 1000;
            configuration.creatorName = this.mContext.getPackageManager().getNameForUid(1000);
        } else if (!creatorName.equals(configuration.creatorName)) {
            Log.w(TAG, "Invalid creatorName for saved network " + configuration.configKey() + ", creatorUid=" + configuration.creatorUid + ", creatorName=" + configuration.creatorName);
            configuration.creatorName = creatorName;
        }
        configuration.setNetworkSelectionStatus(status);
        configuration.setIpConfiguration(ipConfiguration);
        if (enterpriseConfig != null) {
            configuration.enterpriseConfig = enterpriseConfig;
        }
        return configuration;
    }
}
