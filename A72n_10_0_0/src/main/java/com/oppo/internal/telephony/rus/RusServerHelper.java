package com.oppo.internal.telephony.rus;

import android.os.SystemProperties;
import android.telephony.Rlog;
import android.util.Xml;
import com.android.internal.telephony.OemConstant;
import com.android.internal.util.XmlUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class RusServerHelper {
    public static final String OLD_OTA_VERSION_FILE = "old_ota_version.xml";
    private static final String TAG = "RusServerHelper";
    private static final String mAllConfig_end = "/versions>";
    private static final String mAllConfig_start = "<versions";
    private static String mNewNwAllConfigVer = null;
    private static String mNewNwAllConfigXml = null;
    private static final String mNwAllConfigFileName = "nw_all_config.xml";
    private static final String mNwAllConfig_start = "filter-config";
    private static String mOldNwAllConfigVer = null;
    private static String mOldNwAllConfigXml = null;
    private static final String mPath = "/data/data/com.android.phone/";
    protected static Object mStateMonitor = new Object();
    private FileUtils mFileUtils = new FileUtils();

    public RusServerHelper() {
        String str = mOldNwAllConfigXml;
        if (str == null || str.isEmpty()) {
            mOldNwAllConfigXml = readRusDataFromXml(mNwAllConfigFileName);
        }
        String str2 = mOldNwAllConfigVer;
        if (str2 == null || str2.isEmpty()) {
            mOldNwAllConfigVer = getNwAllConfigVer(mOldNwAllConfigXml);
        }
    }

    public void executeHelpRusAction(String newXml) {
        synchronized (mStateMonitor) {
            new HashMap();
            mNewNwAllConfigXml = newXml;
            mNewNwAllConfigVer = getNwAllConfigVer(mNewNwAllConfigXml);
            Map<String, String> needRusMap = getNeedRusMap();
            if (needRusMap != null) {
                try {
                    for (Map.Entry<String, String> entry : needRusMap.entrySet()) {
                        String key = entry.getKey();
                        String val = entry.getValue();
                        RusBase rusbase = getReflect(key);
                        if (rusbase != null) {
                            rusbase.executeBaseRusAction(key, val);
                        }
                    }
                } catch (Exception e) {
                    printLog(TAG, " this some wrong with executing");
                }
            }
        }
    }

    public void executeHelpRebootAction() {
        synchronized (mStateMonitor) {
            new HashMap();
            Map<String, String> rebootMap = getAllConfigAndVersionMap(getSubConfig(mOldNwAllConfigXml, mAllConfig_start, mAllConfig_end));
            if (rebootMap != null) {
                try {
                    for (Map.Entry<String, String> entry : rebootMap.entrySet()) {
                        String key = entry.getKey();
                        String val = entry.getValue();
                        RusBase rusbase = getReflect(key);
                        if (rusbase != null) {
                            rusbase.executeBaseRebootAction(key, val);
                        }
                        sleep(100);
                    }
                } catch (Exception e) {
                    printLog(TAG, " this some wrong with executing");
                }
            }
        }
    }

    public String readRusDataFromXml(String filename) {
        File path = new File(mPath + filename);
        if (!path.exists()) {
            printLog(TAG, mPath + filename + " not exist!");
            return null;
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(path);
            BufferedReader in = new BufferedReader(new InputStreamReader(is2));
            StringBuffer buffer = new StringBuffer();
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                buffer.append(line);
            }
            String stringBuffer = buffer.toString();
            try {
                is2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return stringBuffer;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (0 != 0) {
                is.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
        return null;
    }

    public void deleteExistFile(String fileName) {
        File file = new File(mPath + fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public File saveXmlToFile(String content, String destfile) {
        FileUtils fileUtils = this.mFileUtils;
        return fileUtils.saveToFile(content, mPath + destfile);
    }

    public XmlPullParser getXmlParser(String nwAllConfigXml, String beginString) {
        XmlPullParser strparser = null;
        if (nwAllConfigXml == null || nwAllConfigXml.isEmpty()) {
            return null;
        }
        StringReader strreader = new StringReader(nwAllConfigXml);
        try {
            strparser = Xml.newPullParser();
            strparser.setInput(strreader);
            if (beginString != null && !beginString.isEmpty()) {
                XmlUtils.beginDocument(strparser, beginString);
            }
        } catch (Exception e) {
            printLog(TAG, "Exception getXmlParser()" + e);
        }
        return strparser;
    }

    public String getSubConfig(String strAllConfig, String index_start, String index_end) {
        String subConfig = "";
        if (strAllConfig == null || strAllConfig.isEmpty()) {
            return "";
        }
        int start_index = strAllConfig.indexOf(index_start);
        int end_index = strAllConfig.indexOf(index_end);
        printLog(TAG, "getSubConfig() index_start :" + index_start + ",start_index=" + start_index + " index_end:" + index_end + ",end_index=" + end_index);
        if (start_index > 0 && end_index > 0) {
            subConfig = strAllConfig.substring(start_index, index_end.length() + end_index);
        }
        printLog(TAG, "getSubConfig():" + subConfig);
        return subConfig;
    }

    public String getNewOtaVer() {
        return SystemProperties.get("ro.build.version.ota", "ota_version");
    }

    public String getOldOtaVer() {
        return readRusDataFromXml(OLD_OTA_VERSION_FILE);
    }

    private String getNwAllConfigVer(String nwAllConfigXml) {
        if (nwAllConfigXml == null || nwAllConfigXml.isEmpty()) {
            return "";
        }
        try {
            XmlPullParser strparser = getXmlParser(nwAllConfigXml, mNwAllConfig_start);
            if (strparser == null) {
                return "";
            }
            XmlUtils.nextElement(strparser);
            if (strparser.getEventType() == 1 || !"version".equals(strparser.getName())) {
                return "";
            }
            strparser.next();
            return strparser.getText();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e2) {
            e2.printStackTrace();
            return "";
        }
    }

    private Map<String, String> getAllConfigAndVersionMap(String allConfigXml) {
        Map<String, String> allConfigMap = new HashMap<>();
        if (allConfigXml == null || allConfigXml.isEmpty()) {
            return null;
        }
        try {
            XmlPullParser strparser = getXmlParser(allConfigXml, "versions");
            if (strparser != null) {
                XmlUtils.nextElement(strparser);
                if (strparser.getEventType() != 1 && "version".equals(strparser.getName()) && strparser.getAttributeCount() > 0) {
                    for (int i = 0; i < strparser.getAttributeCount(); i++) {
                        String attrname = strparser.getAttributeName(i);
                        String attrvalue = strparser.getAttributeValue(i);
                        printLog(TAG, "parseRusXML attrname = " + attrname + ",attrvalue= " + attrvalue);
                        allConfigMap.put(attrname, attrvalue);
                    }
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return allConfigMap;
    }

    private Map<String, String> getNeedRusMap() {
        Map<String, String> needRusMap;
        new HashMap();
        new HashMap();
        new HashMap();
        try {
            if (mOldNwAllConfigVer == null || mOldNwAllConfigVer.isEmpty()) {
                this.mFileUtils.saveToFile(mNewNwAllConfigXml, "/data/data/com.android.phone/nw_all_config.xml");
                deleteExistFile(OLD_OTA_VERSION_FILE);
                this.mFileUtils.saveToFile(getNewOtaVer(), "/data/data/com.android.phone/old_ota_version.xml");
                mOldNwAllConfigXml = mNewNwAllConfigXml;
                mOldNwAllConfigVer = mNewNwAllConfigVer;
                mNewNwAllConfigVer = "";
                mNewNwAllConfigXml = "";
                needRusMap = getAllConfigAndVersionMap(getSubConfig(mOldNwAllConfigXml, mAllConfig_start, mAllConfig_end));
            } else if (mNewNwAllConfigVer.compareTo(mOldNwAllConfigVer) <= 0) {
                mNewNwAllConfigVer = "";
                mNewNwAllConfigXml = "";
                return null;
            } else {
                Map<String, String> oldversionMap = getAllConfigAndVersionMap(getSubConfig(mOldNwAllConfigXml, mAllConfig_start, mAllConfig_end));
                needRusMap = getAllConfigAndVersionMap(getSubConfig(mNewNwAllConfigXml, mAllConfig_start, mAllConfig_end));
                if (!(needRusMap == null || oldversionMap == null)) {
                    Iterator<Map.Entry<String, String>> iter = needRusMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, String> entry = iter.next();
                        String key = entry.getKey();
                        String val = entry.getValue();
                        if (oldversionMap.containsKey(key) && val.compareTo(oldversionMap.get(key)) <= 0) {
                            printLog(TAG, "getNeedRusList: begin remove");
                            iter.remove();
                            printLog(TAG, "getNeedRusList:end remove");
                        }
                    }
                }
                this.mFileUtils.saveToFile(mNewNwAllConfigXml, "/data/data/com.android.phone/nw_all_config.xml");
                deleteExistFile(OLD_OTA_VERSION_FILE);
                this.mFileUtils.saveToFile(getNewOtaVer(), "/data/data/com.android.phone/old_ota_version.xml");
                mOldNwAllConfigXml = mNewNwAllConfigXml;
                mOldNwAllConfigVer = mNewNwAllConfigVer;
                mNewNwAllConfigVer = "";
                mNewNwAllConfigXml = "";
            }
            saveNeedRusSubXml(needRusMap);
            return needRusMap;
        } catch (Exception e) {
            printLog(TAG, "getNeedRusList:" + e);
            return null;
        }
    }

    private void saveNeedRusSubXml(Map<String, String> needRusMap) {
        if (needRusMap != null) {
            try {
                for (Map.Entry<String, String> entry : needRusMap.entrySet()) {
                    String key = entry.getKey();
                    entry.getValue();
                    FileUtils fileUtils = this.mFileUtils;
                    String subConfig = getSubConfig(mOldNwAllConfigXml, "<" + key, "/" + key + ">");
                    fileUtils.saveToFile(subConfig, mPath + (key + ".xml"));
                }
            } catch (Exception e) {
                printLog(TAG, " this some wrong with executing");
            }
        }
    }

    private RusBase getReflect(String tagName) {
        if (tagName == null || tagName.length() == 0) {
            printLog(TAG, "this tagName is not exist");
            return null;
        }
        String fullClsName = "com.oppo.internal.telephony.rus.RusUpdate" + tagName;
        try {
            Class<?> cls = Class.forName(fullClsName);
            printLog(TAG, "cls =" + cls);
            Constructor custMethod = cls.getConstructor(new Class[0]);
            printLog(TAG, "custMehod =" + custMethod);
            return (RusBase) custMethod.newInstance(new Object[0]);
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
            printLog(TAG, "NoClassDefFoundError:" + fullClsName);
            return null;
        } catch (Exception e2) {
            e2.printStackTrace();
            printLog(TAG, "Exception:" + fullClsName);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void sleep(int ms) {
        printLog(TAG, "sleep sleep beg");
        try {
            Thread.sleep((long) ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        printLog(TAG, "sleep sleep end");
    }

    private void printLog(String tag, String msg) {
        if (OemConstant.SWITCH_LOG) {
            Rlog.d(tag, msg);
        }
    }
}
