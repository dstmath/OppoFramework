package com.android.internal.telephony.oem.rus;

import android.telephony.Rlog;
import android.util.Log;
import android.util.Xml;
import com.android.internal.telephony.OemConstant;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParser;

public class RusServerHelper {
    private static final String TAG = "RusServerHelper";
    private static final int mConfigIndex_count = 12;
    private static final String mConfigIndex_end = "/all_config>";
    private static final String mConfigIndex_start = "<all_config";
    private static final String mConfigInfoFileName = "all_config.xml";
    private static final String mPath = "/data/data/com.android.phone/";
    private static final int mVersionsIndex_count = 10;
    private static final String mVersionsIndex_end = "/versions>";
    private static final String mVersionsIndex_start = "<versions";
    private static final String mVersionsInfoFileName = "all_versions.xml";
    private String mConfigContent = null;
    private FileUtils mFileUtils = new FileUtils();
    private List<String> mNewConfigversionList = new ArrayList();
    private List<String> mOldConfigversionList = new ArrayList();
    private String mProviderContent = null;
    private String mVersionContent = null;

    public void executeRus() {
        List<RusBase> rusList = new ArrayList();
        List<String> needRusList = getUpdateConfigVersionList();
        if (needRusList != null) {
            for (String list : needRusList) {
                RusBase rusbase = getReflect(list);
                if (rusbase != null) {
                    rusbase.setContent(getProviderContent());
                    if (rusbase.mPath == null) {
                        rusbase.mPath = mPath;
                    }
                    printLog(TAG, "this is a ruslist=" + rusbase);
                    rusList.add(rusbase);
                }
            }
            for (RusBase rus : rusList) {
                try {
                    rus.execute();
                } catch (Exception e) {
                    printLog(TAG, " this some wrong with executing");
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x0117 A:{SYNTHETIC, Splitter: B:51:0x0117} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00e0 A:{SYNTHETIC, Splitter: B:29:0x00e0} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x010b A:{SYNTHETIC, Splitter: B:43:0x010b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<String> getConfigList(File configListFile) {
        FileNotFoundException e;
        Throwable th;
        Exception e2;
        printLog(TAG, "this is start to get config list");
        FileReader confreader = null;
        List<String> configList = new ArrayList();
        try {
            FileReader confreader2 = new FileReader(configListFile);
            try {
                XmlPullParser parser = getXmlParser(confreader2, "all_config");
                if (parser != null) {
                    XmlUtils.nextElement(parser);
                    while (parser.getEventType() != 1) {
                        if ("config".equals(parser.getName())) {
                            int configCount = Integer.parseInt(parser.getAttributeValue(null, "config_count"));
                            printLog(TAG, "The count of config is =" + configCount);
                            for (int i = 0; i < configCount; i++) {
                                String config = parser.getAttributeValue(null, "config_" + String.valueOf(i));
                                printLog(TAG, " The config_" + String.valueOf(i) + "is=" + config);
                                configList.add(config);
                            }
                            XmlUtils.nextElement(parser);
                        } else {
                            printLog(TAG, "this first tag is not match");
                            if (confreader2 != null) {
                                try {
                                    confreader2.close();
                                } catch (IOException e3) {
                                }
                            }
                            return null;
                        }
                    }
                }
                if (confreader2 != null) {
                    try {
                        confreader2.close();
                    } catch (IOException e4) {
                    }
                }
                return configList;
            } catch (FileNotFoundException e5) {
                e = e5;
                confreader = confreader2;
                try {
                    printLog(TAG, "filenotfoundException" + e);
                    if (confreader != null) {
                        try {
                            confreader.close();
                        } catch (IOException e6) {
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (confreader != null) {
                    }
                    throw th;
                }
            } catch (Exception e7) {
                e2 = e7;
                confreader = confreader2;
                printLog(TAG, "exceptin while parsing" + e2);
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e8) {
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                confreader = confreader2;
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e9) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e10) {
            e = e10;
            printLog(TAG, "filenotfoundException" + e);
            if (confreader != null) {
            }
            return null;
        } catch (Exception e11) {
            e2 = e11;
            printLog(TAG, "exceptin while parsing" + e2);
            if (confreader != null) {
            }
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x00ed A:{SYNTHETIC, Splitter: B:55:0x00ed} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00b5 A:{SYNTHETIC, Splitter: B:32:0x00b5} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00e1 A:{SYNTHETIC, Splitter: B:47:0x00e1} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Map<String, Integer> getVersionMapList(List<String> configList, File versionFile) {
        FileNotFoundException e;
        Throwable th;
        Exception e2;
        FileReader confreader = null;
        List<String> configlist = configList;
        try {
            FileReader confreader2 = new FileReader(versionFile);
            try {
                XmlPullParser parser = getXmlParser(confreader2, "versions");
                Map<String, Integer> versionMap = new HashMap();
                if (parser != null) {
                    XmlUtils.nextElement(parser);
                    while (parser.getEventType() != 1) {
                        if ("version".equals(parser.getName())) {
                            for (int i = 0; i < configList.size(); i++) {
                                String config = (String) configList.get(i);
                                int version = Integer.parseInt(parser.getAttributeValue(null, (String) configList.get(i)));
                                printLog(TAG, " The version_" + version + "config is =" + config);
                                versionMap.put(config, Integer.valueOf(version));
                            }
                            XmlUtils.nextElement(parser);
                        } else {
                            printLog(TAG, "this first tag is not match");
                            if (confreader2 != null) {
                                try {
                                    confreader2.close();
                                } catch (IOException e3) {
                                }
                            }
                            return null;
                        }
                    }
                }
                if (confreader2 != null) {
                    try {
                        confreader2.close();
                    } catch (IOException e4) {
                    }
                }
                return versionMap;
            } catch (FileNotFoundException e5) {
                e = e5;
                confreader = confreader2;
                try {
                    printLog(TAG, "filenotfoundException" + e);
                    if (confreader != null) {
                        try {
                            confreader.close();
                        } catch (IOException e6) {
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (confreader != null) {
                    }
                    throw th;
                }
            } catch (Exception e7) {
                e2 = e7;
                confreader = confreader2;
                printLog(TAG, "exceptin while parsing" + e2);
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e8) {
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                confreader = confreader2;
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e9) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e10) {
            e = e10;
            printLog(TAG, "filenotfoundException" + e);
            if (confreader != null) {
            }
            return null;
        } catch (Exception e11) {
            e2 = e11;
            printLog(TAG, "exceptin while parsing" + e2);
            if (confreader != null) {
            }
            return null;
        }
    }

    private void getUpdateConfig() {
        Log.d(TAG, "this is start to get update config version list");
        String versionProviderContent = getProviderContent();
        if (versionProviderContent == null) {
            printLog(TAG, "this data from romupate is null, error.please check romupdate");
            return;
        }
        File oldConfigFile = new File("/data/data/com.android.phone/all_config.xml");
        try {
            if (oldConfigFile.exists()) {
                this.mOldConfigversionList = getConfigList(oldConfigFile);
                this.mConfigContent = createNetworkRomupdateXmlFile(versionProviderContent, mConfigInfoFileName, mConfigIndex_start, mConfigIndex_end, 12);
                this.mNewConfigversionList = getConfigList(this.mFileUtils.saveToFile(this.mConfigContent, "/data/data/com.android.phone/all_config.xml"));
            } else {
                printLog(TAG, "this is the first time to update all file");
                this.mConfigContent = createNetworkRomupdateXmlFile(versionProviderContent, mConfigInfoFileName, mConfigIndex_start, mConfigIndex_end, 12);
                this.mNewConfigversionList = getConfigList(this.mFileUtils.saveToFile(this.mConfigContent, "/data/data/com.android.phone/all_config.xml"));
            }
        } catch (Exception e) {
            printLog(TAG, "exceptin while parsing" + e);
        }
        return;
    }

    private void getNeedUpdate(Map<String, Integer> oldversionMap, Map<String, Integer> newversionMap, List<String> versionList) {
        Integer val = Integer.valueOf(0);
        for (Entry<String, Integer> entry : newversionMap.entrySet()) {
            String key = (String) entry.getKey();
            val = (Integer) entry.getValue();
            boolean isNew = true;
            if (oldversionMap != null && oldversionMap.containsKey(key) && val.intValue() <= ((Integer) oldversionMap.get(key)).intValue()) {
                isNew = false;
            }
            if (isNew) {
                versionList.add(key);
            }
            printLog(TAG, "this key:" + key + ";val:" + val + ";" + isNew);
        }
    }

    private List<String> getUpdateConfigVersionList() {
        printLog(TAG, "this is start to get update config version list");
        List<String> versionList = new ArrayList();
        Map<String, Integer> oldversionMap = new HashMap();
        Map<String, Integer> newversionMap = new HashMap();
        getUpdateConfig();
        String versionProviderContent = getProviderContent();
        if (versionProviderContent == null) {
            printLog(TAG, "this data from romupate is null, error.please check romupdate");
            return null;
        }
        File oldVersionFile = new File("/data/data/com.android.phone/all_versions.xml");
        try {
            if (oldVersionFile.exists()) {
                oldversionMap = getVersionMapList(this.mOldConfigversionList, oldVersionFile);
                this.mVersionContent = createNetworkRomupdateXmlFile(versionProviderContent, mVersionsInfoFileName, mVersionsIndex_start, mVersionsIndex_end, 10);
                newversionMap = getVersionMapList(this.mNewConfigversionList, this.mFileUtils.saveToFile(this.mVersionContent, "/data/data/com.android.phone/all_versions.xml"));
                if (newversionMap != null) {
                    getNeedUpdate(oldversionMap, newversionMap, versionList);
                } else {
                    printLog(TAG, "newversionMap is null");
                }
            } else {
                printLog(TAG, "this is the first time to update all file");
                this.mVersionContent = createNetworkRomupdateXmlFile(versionProviderContent, mVersionsInfoFileName, mVersionsIndex_start, mVersionsIndex_end, 10);
                this.mFileUtils.saveSpnToFile(this.mVersionContent, "/data/data/com.android.phone/all_versions.xml");
                for (String list : this.mNewConfigversionList) {
                    printLog(TAG, "this new update list is =" + list);
                    versionList.add(list);
                }
            }
            return versionList;
        } catch (Exception e) {
            printLog(TAG, "exceptin while parsing" + e);
            return null;
        }
    }

    private RusBase getReflect(String tagName) {
        if (tagName == null || tagName.length() == 0) {
            printLog(TAG, "this tagName is not exist");
            return null;
        }
        try {
            Class<?> cls = Class.forName("com.android.internal.telephony.oem.rus.RusUpdate" + tagName);
            printLog(TAG, "cls =" + cls);
            Constructor custMethod = cls.getConstructor(new Class[0]);
            printLog(TAG, "custMehod =" + custMethod);
            return (RusBase) custMethod.newInstance(new Object[0]);
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
            printLog(TAG, "error in loading class");
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            printLog(TAG, "error loading class");
            return null;
        }
    }

    private void printLog(String tag, String msg) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(tag, msg);
        }
    }

    private XmlPullParser getXmlParser(FileReader confreader, String beginString) {
        XmlPullParser xmlPullParser = null;
        try {
            xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(confreader);
            XmlUtils.beginDocument(xmlPullParser, beginString);
            return xmlPullParser;
        } catch (Exception e) {
            printLog(TAG, "Exception while parsing network xml file" + e);
            return xmlPullParser;
        }
    }

    public void setProviderContent(String providercontent) {
        this.mProviderContent = providercontent;
    }

    private String getProviderContent() {
        return this.mProviderContent;
    }

    private String createNetworkRomupdateXmlFile(String providerString, String networkInfoFileName, String networkIndexStart, String networkIndexEnd, int subStringCount) {
        printLog(TAG, "romupdate networkInfoFileName path=" + (mPath + networkInfoFileName));
        int networkStartIndex = providerString.indexOf(networkIndexStart);
        int networkEndIndex = providerString.indexOf(networkIndexEnd);
        printLog(TAG, "network_index start=" + networkStartIndex + " network_indext end" + networkEndIndex);
        String networkContent = providerString.substring(networkStartIndex, networkEndIndex + subStringCount);
        printLog(TAG, "Network xml string is:" + networkContent);
        return networkContent;
    }
}
