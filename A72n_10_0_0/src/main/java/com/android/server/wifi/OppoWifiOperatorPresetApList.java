package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.telephony.ColorOSTelephonyManager;
import android.util.Log;
import com.android.server.wifi.util.XmlUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class OppoWifiOperatorPresetApList {
    private static final boolean DBG = true;
    private static final String PRE_INSTALLED_AP_XML_FILE_PATH = "/system/oppo/wifi/wifi_operator_preset_ap_list.xml";
    private static final String PRE_INSTALLED_AP_XML_FILE_ROOT_ITEM = "WifiOperatorPresetApList";
    private static final String TAG = "OppoWifiOperatorPresetApList";
    private static final boolean XML_DBG = false;
    private static OppoWifiOperatorPresetApList sInstance;
    private boolean bootCompleted = false;
    private ArrayList<WifiConfiguration> mAccessPointList = new ArrayList<>();
    private ColorOSTelephonyManager mColorOSTelephonyManager;
    private final Context mContext;
    private final String mListFilePath;
    private Map<String, String> mPropertyLogicMap = new HashMap();
    private Map<String, List<RequiredProp>> mPropertyMap = new HashMap();
    private Map<String, List<String>> mSimOperatorMap = new HashMap();

    public static class RequiredProp {
        public String property;
        public String value;

        private RequiredProp(String p, String v) {
            this.property = p;
            this.value = v;
        }
    }

    public OppoWifiOperatorPresetApList(Context context, String filePath) {
        Log.i(TAG, TAG);
        this.mContext = context;
        this.mColorOSTelephonyManager = ColorOSTelephonyManager.getDefault(this.mContext);
        this.mListFilePath = (filePath == null || filePath.equals("")) ? PRE_INSTALLED_AP_XML_FILE_PATH : filePath;
        loadApListFromXml();
    }

    public static OppoWifiOperatorPresetApList getInstance(Context context, String filePath) {
        if (sInstance == null) {
            synchronized (OppoWifiOperatorPresetApList.class) {
                if (sInstance == null) {
                    sInstance = new OppoWifiOperatorPresetApList(context, filePath);
                }
            }
        }
        return sInstance;
    }

    public String getListFilePath() {
        return this.mListFilePath;
    }

    public void handleBootCompleted() {
        Log.i(TAG, "Handle boot completed");
        this.bootCompleted = true;
        applyAllNetworks();
    }

    private List<WifiConfiguration> getPresetApList() {
        return this.mAccessPointList;
    }

    /* JADX INFO: Multiple debug info for r0v4 org.w3c.dom.Document: [D('factory' javax.xml.parsers.DocumentBuilderFactory), D('doc' org.w3c.dom.Document)] */
    private void loadApListFromXml() {
        Element root;
        Document doc;
        Element root2;
        Document doc2;
        try {
            Document doc3 = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(this.mListFilePath));
            if (doc3 == null) {
                Log.e(TAG, "loadApListFromXml, null doc!");
                return;
            }
            Element root3 = doc3.getDocumentElement();
            String rootText = root3.getTagName().trim();
            if (rootText != null) {
                if (rootText.equals(PRE_INSTALLED_AP_XML_FILE_ROOT_ITEM)) {
                    NodeList apList = root3.getChildNodes();
                    int apIndex = 0;
                    while (apIndex < apList.getLength()) {
                        Node ap = apList.item(apIndex);
                        if (ap instanceof Element) {
                            Element apElement = (Element) ap;
                            String apElementTagName = apElement.getTagName();
                            if (apElementTagName == null) {
                                doc = doc3;
                                root = root3;
                            } else if (!apElementTagName.equals("AccessPoint")) {
                                doc = doc3;
                                root = root3;
                            } else {
                                WifiConfiguration wifiConfig = null;
                                NodeList apFeatureList = apElement.getChildNodes();
                                int apFeatureIndex = 0;
                                while (apFeatureIndex < apFeatureList.getLength()) {
                                    Node apFeature = apFeatureList.item(apFeatureIndex);
                                    if (apFeature instanceof Element) {
                                        Element apFeatureElement = (Element) apFeature;
                                        String apFeatureTagName = apFeatureElement.getTagName();
                                        if (apFeatureTagName == null) {
                                            doc2 = doc3;
                                            root2 = root3;
                                        } else {
                                            doc2 = doc3;
                                            if (apFeatureTagName.equals("WifiConfiguration")) {
                                                wifiConfig = parseWifiConfiguration(apFeatureElement);
                                                root2 = root3;
                                            } else if (!apFeatureTagName.equals("PreCondition")) {
                                                root2 = root3;
                                            } else if (wifiConfig == null || wifiConfig.SSID == null) {
                                                root2 = root3;
                                            } else {
                                                root2 = root3;
                                                if (!wifiConfig.SSID.equals("\"\"")) {
                                                    parsePreCondition(apFeatureElement, wifiConfig);
                                                }
                                            }
                                        }
                                    } else {
                                        doc2 = doc3;
                                        root2 = root3;
                                    }
                                    apFeatureIndex++;
                                    doc3 = doc2;
                                    root3 = root2;
                                }
                                doc = doc3;
                                root = root3;
                                if (!(wifiConfig == null || wifiConfig.SSID == null)) {
                                    this.mAccessPointList.add(wifiConfig);
                                }
                            }
                            Log.e(TAG, "loadApListFromXml, apElementTagName is WRONG:" + apElementTagName);
                        } else {
                            doc = doc3;
                            root = root3;
                        }
                        apIndex++;
                        doc3 = doc;
                        root3 = root;
                    }
                    return;
                }
            }
            Log.e(TAG, "loadApListFromXml, xml root tag is not WifiOperatorPresetApList");
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "ParserConfigurationException:" + e);
        } catch (SAXException e2) {
            Log.e(TAG, "SAXException:" + e2);
        } catch (IOException e3) {
            Log.e(TAG, "IOException:" + e3);
        }
    }

    private WifiConfiguration parseWifiConfiguration(Element element) {
        Map<String, String> configMap = new HashMap<>();
        NodeList wifiConfigurationList = element.getChildNodes();
        for (int wifiConfigurationIndex = 0; wifiConfigurationIndex < wifiConfigurationList.getLength(); wifiConfigurationIndex++) {
            Node configItemNode = wifiConfigurationList.item(wifiConfigurationIndex);
            if (configItemNode instanceof Element) {
                Element configItemElement = (Element) configItemNode;
                String configItemTagName = configItemElement.getTagName();
                Text configItemTextNode = (Text) configItemElement.getFirstChild();
                if (!(configItemTagName == null || configItemTextNode == null)) {
                    configMap.put(configItemTagName, configItemTextNode.getData().trim());
                }
            }
        }
        WifiConfiguration config = new WifiConfiguration();
        String SSID = configMap.get(XmlUtil.WifiConfigurationXmlUtil.XML_TAG_SSID);
        if (SSID == null || SSID.equals("")) {
            Log.e(TAG, "parseWifiConfiguration, SSID is illegal, skip.");
            return null;
        }
        config.SSID = "\"" + SSID + "\"";
        String keyMgmt = configMap.get("keyMgmt");
        if (keyMgmt == null) {
            Log.e(TAG, "parseWifiConfiguration, keyMgmt is null, skip.");
            return null;
        }
        if (keyMgmt.equals("WPA_EAP")) {
            config.allowedKeyManagement.set(2);
            config.allowedKeyManagement.set(3);
            String eapConfig = configMap.get("eapConfig");
            WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
            if (eapConfig == null) {
                Log.e(TAG, "parseWifiConfiguration, eapConfig is null, skip.");
                return null;
            }
            if (eapConfig.equals("SIM")) {
                enterpriseConfig.setEapMethod(4);
            } else if (eapConfig.equals("AKA")) {
                enterpriseConfig.setEapMethod(5);
            } else if (eapConfig.equals("AKA_PRIME")) {
                enterpriseConfig.setEapMethod(6);
            } else {
                Log.e(TAG, "parseWifiConfiguration, eapConfig is not supported:" + eapConfig + ", skip.");
                return null;
            }
            config.enterpriseConfig = enterpriseConfig;
        } else if (keyMgmt.equals("NONE")) {
            config.allowedKeyManagement.set(0);
        } else {
            Log.e(TAG, "parseWifiConfiguration, keyMgmt is not supported:" + keyMgmt + ", skip.");
            return null;
        }
        String hidden = configMap.get("hidden");
        if (hidden != null && hidden.equals("1")) {
            config.hiddenSSID = true;
        }
        return config;
    }

    private void parsePreCondition(Element element, WifiConfiguration wifiConfig) {
        NodeList preConditionList;
        NodeList preConditionList2 = element.getChildNodes();
        List<RequiredProp> requiredPropList = new ArrayList<>();
        ArrayList<String> requiredSimOperatorList = new ArrayList<>();
        String requiredPropLogicStr = null;
        int preConditionIndex = 0;
        while (preConditionIndex < preConditionList2.getLength()) {
            Node preConditionNode = preConditionList2.item(preConditionIndex);
            if (preConditionNode instanceof Element) {
                Element preConditionElement = (Element) preConditionNode;
                String preConditionTagName = preConditionElement.getTagName();
                if (preConditionTagName == null) {
                    preConditionList = preConditionList2;
                } else if (preConditionTagName.equals("requiredProp")) {
                    requiredPropList.add(new RequiredProp(preConditionElement.getAttribute("prop"), preConditionElement.getAttribute("value")));
                    preConditionList = preConditionList2;
                } else if (preConditionTagName.equals("requiredPropLogic")) {
                    requiredPropLogicStr = preConditionElement.getAttribute("logic");
                    preConditionList = preConditionList2;
                } else if (preConditionTagName.equals("recheckIntent")) {
                    preConditionList = preConditionList2;
                    registerForBroadcast(wifiConfig, preConditionElement.getAttribute("intentAction"), preConditionElement.getAttribute("intentIntExtra"), preConditionElement.getAttribute("intentIntExtraDefaultValue"), preConditionElement.getAttribute("intentIntExtraExpectedValue"), preConditionElement.getAttribute("intentStringExtra"), preConditionElement.getAttribute("intentStringExtraExpectedValue"));
                } else {
                    preConditionList = preConditionList2;
                    if (preConditionTagName.equals("requiredSimOperator")) {
                        requiredSimOperatorList.add(preConditionElement.getAttribute("mccmnc"));
                    }
                }
            } else {
                preConditionList = preConditionList2;
            }
            preConditionIndex++;
            preConditionList2 = preConditionList;
        }
        if (requiredPropList.size() != 0) {
            this.mPropertyMap.put(wifiConfig.configKey(false), requiredPropList);
        }
        if (requiredPropLogicStr != null && (requiredPropLogicStr.equals("or") || requiredPropLogicStr.equals("and"))) {
            this.mPropertyLogicMap.put(wifiConfig.configKey(false), requiredPropLogicStr);
        }
        if (requiredSimOperatorList.size() != 0) {
            this.mSimOperatorMap.put(wifiConfig.configKey(false), requiredSimOperatorList);
        }
    }

    private void registerForBroadcast(final WifiConfiguration wifiConfig, final String intentAction, final String intentIntExtra, final String intentIntExtraDefaultValue, final String intentIntExtraExpectedValue, final String intentStringExtra, final String intentStringExtraExpectedValue) {
        Log.i(TAG, "registerForBroadcast enter, intentAction:" + intentAction + ", wifiConfig.configKey:" + wifiConfig.configKey(false) + ", intentIntExtra is " + intentIntExtra + ", intentIntExtraDefaultValue is " + intentIntExtraDefaultValue + ", intentIntExtraExpectedValue is " + intentIntExtraExpectedValue + ", intentStringExtra is " + intentStringExtra + ", intentStringExtraExpectedValue is " + intentStringExtraExpectedValue);
        if (intentAction != null && !intentAction.equals("")) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(intentAction);
            this.mContext.registerReceiver(new BroadcastReceiver() {
                /* class com.android.server.wifi.OppoWifiOperatorPresetApList.AnonymousClass1 */

                public void onReceive(Context context, Intent intent) {
                    String str;
                    String str2;
                    String str3;
                    if (OppoWifiOperatorPresetApList.this.bootCompleted) {
                        String action = intent.getAction();
                        Log.i(OppoWifiOperatorPresetApList.TAG, action + " received.");
                        if (action.equals(intentAction)) {
                            String str4 = intentIntExtra;
                            if (str4 == null || str4.equals("") || (str2 = intentIntExtraDefaultValue) == null || str2.equals("") || (str3 = intentIntExtraExpectedValue) == null || str3.equals("")) {
                                String str5 = intentStringExtra;
                                if (str5 != null && !str5.equals("") && (str = intentStringExtraExpectedValue) != null && !str.equals("")) {
                                    String stringExtraValue = intent.getStringExtra(intentStringExtra);
                                    Log.i(OppoWifiOperatorPresetApList.TAG, action + ", intentStringExtra is " + intentStringExtra + ", stringExtraValue is " + stringExtraValue);
                                    if (!intentStringExtraExpectedValue.equals(stringExtraValue)) {
                                        return;
                                    }
                                }
                            } else {
                                int intExtraExpectedValue = Integer.parseInt(intentIntExtraExpectedValue);
                                int intExtraValue = intent.getIntExtra(intentIntExtra, Integer.parseInt(intentIntExtraDefaultValue));
                                Log.i(OppoWifiOperatorPresetApList.TAG, action + ", intExtraExpectedValue is " + intExtraExpectedValue + ", intExtraValue is " + intExtraValue);
                                if (intExtraExpectedValue != intExtraValue) {
                                    return;
                                }
                            }
                            OppoWifiOperatorPresetApList.this.applySingleNetwork(wifiConfig);
                        }
                    }
                }
            }, new IntentFilter(filter));
        }
    }

    private void applyAllNetworks() {
        ArrayList<WifiConfiguration> arrayList = this.mAccessPointList;
        if (arrayList != null) {
            Iterator<WifiConfiguration> it = arrayList.iterator();
            while (it.hasNext()) {
                WifiConfiguration config = it.next();
                Log.i(TAG, "applyAllNetworks, checking network:" + config.configKey(false));
                addOrRemoveSingleNetwork(config);
            }
            return;
        }
        Log.i(TAG, "applyAllNetworks, mAccessPointList is null, no need to pre-install any AP");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void applySingleNetwork(WifiConfiguration wifiConfig) {
        ArrayList<WifiConfiguration> arrayList;
        if (wifiConfig != null && (arrayList = this.mAccessPointList) != null) {
            Iterator<WifiConfiguration> it = arrayList.iterator();
            while (it.hasNext()) {
                WifiConfiguration config = it.next();
                if (config != null && wifiConfig.configKey(false).equals(config.configKey(false))) {
                    Log.i(TAG, "applySingleNetwork, checking network:" + wifiConfig.configKey(false));
                    addOrRemoveSingleNetwork(wifiConfig);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:0x01a9  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x01f6  */
    private void addOrRemoveSingleNetwork(WifiConfiguration wifiConfig) {
        boolean shouldBeAdded;
        List<WifiConfiguration> allConfiguredNetworks;
        int netId;
        int netId2;
        int netId3;
        if (wifiConfig != null) {
            boolean res = true;
            boolean propertyMatch = false;
            boolean simOperatorMatch = false;
            List<RequiredProp> requiredPropList = this.mPropertyMap.get(wifiConfig.configKey(false));
            String requiredPropLogicStr = this.mPropertyLogicMap.get(wifiConfig.configKey(false));
            boolean requiredPropLogicIsAnd = requiredPropLogicStr != null && requiredPropLogicStr.equals("and");
            List<String> requiredSimOperatorList = this.mSimOperatorMap.get(wifiConfig.configKey(false));
            Log.i(TAG, "addOrRemoveSingleNetwork, wifiConfig.configKey:" + wifiConfig.configKey(false) + ", requiredPropLogicIsAnd:" + requiredPropLogicIsAnd);
            if (requiredPropList == null) {
                shouldBeAdded = true;
            } else if (requiredPropList.size() == 0) {
                shouldBeAdded = true;
            } else {
                if (requiredPropLogicIsAnd) {
                    propertyMatch = true;
                }
                for (RequiredProp requiredProp : requiredPropList) {
                    String currentValue = SystemProperties.get(requiredProp.property, "");
                    Log.i(TAG, "addOrRemoveSingleNetwork, wifiConfig.configKey:" + wifiConfig.configKey(false) + ", property:" + requiredProp.property + ", value:" + requiredProp.value + ", currentValue:" + currentValue);
                    boolean res2 = requiredProp.value != null && requiredProp.value.equals(currentValue);
                    if (requiredPropLogicIsAnd) {
                        propertyMatch &= res2;
                    } else {
                        propertyMatch |= res2;
                    }
                    res = res;
                }
                shouldBeAdded = res;
                if (requiredSimOperatorList == null && requiredSimOperatorList.size() != 0) {
                    ColorOSTelephonyManager colorOSTelephonyManager = this.mColorOSTelephonyManager;
                    String currentSim1 = null;
                    String currentSim0 = (colorOSTelephonyManager == null || !colorOSTelephonyManager.hasIccCardGemini(0)) ? null : this.mColorOSTelephonyManager.getSimOperatorGemini(0);
                    ColorOSTelephonyManager colorOSTelephonyManager2 = this.mColorOSTelephonyManager;
                    if (colorOSTelephonyManager2 != null && colorOSTelephonyManager2.hasIccCardGemini(1)) {
                        currentSim1 = this.mColorOSTelephonyManager.getSimOperatorGemini(1);
                    }
                    Iterator<String> it = requiredSimOperatorList.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        String mccmnc = it.next();
                        if (currentSim0 != null && currentSim0.equals(mccmnc)) {
                            wifiConfig.enterpriseConfig.setSimNum(1);
                            simOperatorMatch = true;
                            break;
                        }
                        if (currentSim1 != null && currentSim1.equals(mccmnc)) {
                            wifiConfig.enterpriseConfig.setSimNum(2);
                            simOperatorMatch = true;
                            break;
                        }
                    }
                } else {
                    Log.i(TAG, "addOrRemoveSingleNetwork, wifiConfig.configKey:" + wifiConfig.configKey(false) + ", no need to check SIM operator.");
                    simOperatorMatch = true;
                }
                if (!propertyMatch || !simOperatorMatch) {
                    Log.i(TAG, "addOrRemoveSingleNetwork, wifiConfig.configKey:" + wifiConfig.configKey(false) + " doesn't match pre-conditions, propertyMatch:" + propertyMatch + ", simOperatorMatch:" + simOperatorMatch);
                    shouldBeAdded = false;
                }
                int netId4 = -1;
                WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
                allConfiguredNetworks = wifiManager.getConfiguredNetworks();
                if (allConfiguredNetworks == null) {
                    for (WifiConfiguration config : allConfiguredNetworks) {
                        if (config != null) {
                            netId3 = netId4;
                            if (wifiConfig.configKey(false).equals(config.configKey(false))) {
                                netId = config.networkId;
                                Log.i(TAG, "addOrRemoveSingleNetwork, wifiConfig.configKey:" + wifiConfig.configKey(false) + " has already been added, netId:" + netId);
                                break;
                            }
                        } else {
                            netId3 = netId4;
                        }
                        netId4 = netId3;
                    }
                    netId2 = netId4;
                } else {
                    netId2 = -1;
                }
                netId = netId2;
                if (!shouldBeAdded && netId == -1) {
                    Log.i(TAG, "addOrRemoveSingleNetwork, now add new network:" + wifiConfig.configKey(false));
                    wifiManager.addNetwork(wifiConfig);
                    return;
                } else if (!shouldBeAdded && netId != -1) {
                    Log.i(TAG, "addOrRemoveSingleNetwork, now delete network:" + wifiConfig.configKey(false));
                    wifiManager.removeNetwork(netId);
                    return;
                }
            }
            Log.i(TAG, "addOrRemoveSingleNetwork, wifiConfig.configKey:" + wifiConfig.configKey(false) + ", requiredPropList is invalid.");
            if (requiredSimOperatorList == null) {
            }
            Log.i(TAG, "addOrRemoveSingleNetwork, wifiConfig.configKey:" + wifiConfig.configKey(false) + ", no need to check SIM operator.");
            simOperatorMatch = true;
            Log.i(TAG, "addOrRemoveSingleNetwork, wifiConfig.configKey:" + wifiConfig.configKey(false) + " doesn't match pre-conditions, propertyMatch:" + propertyMatch + ", simOperatorMatch:" + simOperatorMatch);
            shouldBeAdded = false;
            int netId42 = -1;
            WifiManager wifiManager2 = (WifiManager) this.mContext.getSystemService("wifi");
            allConfiguredNetworks = wifiManager2.getConfiguredNetworks();
            if (allConfiguredNetworks == null) {
            }
            netId = netId2;
            if (!shouldBeAdded) {
            }
            if (!shouldBeAdded) {
            }
        }
    }
}
