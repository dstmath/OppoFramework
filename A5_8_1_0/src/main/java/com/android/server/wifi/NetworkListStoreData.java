package com.android.server.wifi;

import android.content.Context;
import android.net.IpConfiguration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import android.util.Pair;
import com.android.server.wifi.WifiConfigStore.StoreData;
import com.android.server.wifi.util.XmlUtil;
import com.android.server.wifi.util.XmlUtil.IpConfigurationXmlUtil;
import com.android.server.wifi.util.XmlUtil.NetworkSelectionStatusXmlUtil;
import com.android.server.wifi.util.XmlUtil.WifiConfigurationXmlUtil;
import com.android.server.wifi.util.XmlUtil.WifiEnterpriseConfigXmlUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class NetworkListStoreData implements StoreData {
    private static final String TAG = "NetworkListStoreData";
    private static final String XML_TAG_SECTION_HEADER_IP_CONFIGURATION = "IpConfiguration";
    private static final String XML_TAG_SECTION_HEADER_NETWORK = "Network";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_LIST = "NetworkList";
    private static final String XML_TAG_SECTION_HEADER_NETWORK_STATUS = "NetworkStatus";
    private static final String XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION = "WifiConfiguration";
    private static final String XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION = "WifiEnterpriseConfiguration";
    private final Context mContext;
    private List<WifiConfiguration> mSharedConfigurations;
    private List<WifiConfiguration> mUserConfigurations;

    NetworkListStoreData(Context context) {
        this.mContext = context;
    }

    public void serializeData(XmlSerializer out, boolean shared) throws XmlPullParserException, IOException {
        if (shared) {
            serializeNetworkList(out, this.mSharedConfigurations);
        } else {
            serializeNetworkList(out, this.mUserConfigurations);
        }
    }

    public void deserializeData(XmlPullParser in, int outerTagDepth, boolean shared) throws XmlPullParserException, IOException {
        if (shared) {
            this.mSharedConfigurations = parseNetworkList(in, outerTagDepth);
        } else {
            this.mUserConfigurations = parseNetworkList(in, outerTagDepth);
        }
    }

    public void resetData(boolean shared) {
        if (shared) {
            this.mSharedConfigurations = null;
        } else {
            this.mUserConfigurations = null;
        }
    }

    public String getName() {
        return XML_TAG_SECTION_HEADER_NETWORK_LIST;
    }

    public boolean supportShareData() {
        return true;
    }

    public void setSharedConfigurations(List<WifiConfiguration> configs) {
        this.mSharedConfigurations = configs;
    }

    public List<WifiConfiguration> getSharedConfigurations() {
        if (this.mSharedConfigurations == null) {
            return new ArrayList();
        }
        return this.mSharedConfigurations;
    }

    public void setUserConfigurations(List<WifiConfiguration> configs) {
        this.mUserConfigurations = configs;
    }

    public List<WifiConfiguration> getUserConfigurations() {
        if (this.mUserConfigurations == null) {
            return new ArrayList();
        }
        return this.mUserConfigurations;
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
        WifiConfigurationXmlUtil.writeToXmlForConfigStore(out, config);
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION);
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_NETWORK_STATUS);
        NetworkSelectionStatusXmlUtil.writeToXml(out, config.getNetworkSelectionStatus());
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_NETWORK_STATUS);
        XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_IP_CONFIGURATION);
        IpConfigurationXmlUtil.writeToXml(out, config.getIpConfiguration());
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_IP_CONFIGURATION);
        if (!(config.enterpriseConfig == null || config.enterpriseConfig.getEapMethod() == -1)) {
            XmlUtil.writeNextSectionStart(out, XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION);
            WifiEnterpriseConfigXmlUtil.writeToXml(out, config.enterpriseConfig);
            XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION);
        }
        XmlUtil.writeNextSectionEnd(out, XML_TAG_SECTION_HEADER_NETWORK);
    }

    private List<WifiConfiguration> parseNetworkList(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
        List<WifiConfiguration> networkList = new ArrayList();
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
        Pair parsedConfig = null;
        NetworkSelectionStatus status = null;
        IpConfiguration ipConfiguration = null;
        WifiEnterpriseConfig enterpriseConfig = null;
        String[] headerName = new String[1];
        while (XmlUtil.gotoNextSectionOrEnd(in, headerName, outerTagDepth)) {
            String str = headerName[0];
            if (str.equals(XML_TAG_SECTION_HEADER_WIFI_CONFIGURATION)) {
                if (parsedConfig != null) {
                    Log.e(TAG, "Detected duplicate tag for: WifiConfiguration");
                    return null;
                }
                try {
                    parsedConfig = WifiConfigurationXmlUtil.parseFromXml(in, outerTagDepth + 1);
                } catch (XmlPullParserException e) {
                    Log.e(TAG, "parse WifiConfiguration abnormally");
                    parsedConfig = null;
                }
            } else if (str.equals(XML_TAG_SECTION_HEADER_NETWORK_STATUS)) {
                if (status != null) {
                    Log.e(TAG, "Detected duplicate tag for: NetworkStatus");
                    return null;
                }
                try {
                    status = NetworkSelectionStatusXmlUtil.parseFromXml(in, outerTagDepth + 1);
                } catch (XmlPullParserException e2) {
                    Log.e(TAG, "parse status abnormally");
                    return null;
                }
            } else if (str.equals(XML_TAG_SECTION_HEADER_IP_CONFIGURATION)) {
                if (ipConfiguration != null) {
                    Log.d(TAG, "Detected duplicate tag for: IpConfiguration");
                    return null;
                }
                try {
                    ipConfiguration = IpConfigurationXmlUtil.parseFromXml(in, outerTagDepth + 1);
                } catch (XmlPullParserException e3) {
                    Log.e(TAG, "parse ipConfiguration abnormally");
                    return null;
                }
            } else if (!str.equals(XML_TAG_SECTION_HEADER_WIFI_ENTERPRISE_CONFIGURATION)) {
                return null;
            } else {
                if (enterpriseConfig != null) {
                    Log.d(TAG, "Detected duplicate tag for: WifiEnterpriseConfiguration");
                    return null;
                }
                try {
                    enterpriseConfig = WifiEnterpriseConfigXmlUtil.parseFromXml(in, outerTagDepth + 1);
                } catch (XmlPullParserException e4) {
                    Log.e(TAG, "parse enterpriseConfig abnormally");
                    return null;
                }
            }
        }
        if (parsedConfig == null || parsedConfig.first == null || parsedConfig.second == null) {
            Log.e(TAG, "XML parsing of wifi configuration failed");
            return null;
        }
        String configKeyParsed = parsedConfig.first;
        WifiConfiguration configuration = parsedConfig.second;
        String configKeyCalculated = configuration.configKey();
        if (configKeyParsed.equals(configKeyCalculated)) {
            String creatorName = this.mContext.getPackageManager().getNameForUid(configuration.creatorUid);
            if (creatorName == null) {
                Log.e(TAG, "Invalid creatorUid for saved network " + configuration.configKey() + ", creatorUid=" + configuration.creatorUid);
                configuration.creatorUid = OppoManuConnectManager.UID_DEFAULT;
                configuration.creatorName = creatorName;
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
        Log.e(TAG, "Configuration key does not match. Retrieved: " + configKeyParsed + ", Calculated: " + configKeyCalculated);
        return null;
    }
}
