package com.oppo.enterprise.mdmcoreservice.utils;

import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class WifiProfileXmlUtils {
    public static final String DEBUG_TAG = "wifi_debug_";
    private static final String TAG = "wifi_debug_WlanListXmlUtils";
    public static final String WIFI_PROFILE_LIST_XML = "wifi_profile_list.xml";
    private static final String XML_ATTRIBUTE_TAG = "attr";
    private static final String XML_ITEM_START_TAG = "profile";
    private static final String XML_START_TAG = "WifiProfile";

    /* JADX INFO: Multiple debug info for r2v1 java.io.FileOutputStream: [D('fileos' java.io.FileOutputStream), D('e' java.io.IOException)] */
    public static boolean saveProfileToFile(String path, String name, HashMap<String, String> configMap) {
        if (configMap == null) {
            Log.i(TAG, "no profile");
            return false;
        }
        File file = new File(path, name);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    return false;
                }
            } catch (IOException e) {
                Log.i(TAG, "failed create file " + e);
            }
        }
        FileOutputStream fileos = null;
        try {
            FileOutputStream fileos2 = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fileos2, "UTF-8");
            serializer.startDocument(null, true);
            serializer.startTag(null, XML_START_TAG);
            for (String key : configMap.keySet()) {
                String packageName = configMap.get(key);
                if (packageName != null) {
                    serializer.startTag(null, XML_ITEM_START_TAG);
                    serializer.attribute(null, XML_ATTRIBUTE_TAG, key);
                    serializer.text(packageName);
                    serializer.endTag(null, XML_ITEM_START_TAG);
                }
            }
            serializer.endTag(null, XML_START_TAG);
            serializer.endDocument();
            serializer.flush();
            try {
                fileos2.close();
            } catch (IOException e2) {
                Log.i(TAG, "failed close stream " + e2);
            }
            return true;
        } catch (Exception e3) {
            Log.i(TAG, "failed write file " + e3);
            if (0 != 0) {
                try {
                    fileos.close();
                } catch (IOException e4) {
                    Log.i(TAG, "failed close stream " + e4);
                }
            }
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    fileos.close();
                } catch (IOException e5) {
                    Log.i(TAG, "failed close stream " + e5);
                }
            }
            throw th;
        }
    }

    public static HashMap<String, String> readXMLFile(String path, String name) {
        String str;
        StringBuilder sb;
        int type;
        HashMap<String, String> configMap = new HashMap<>();
        File file = new File(path, name);
        if (!file.exists()) {
            return configMap;
        }
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && XML_ITEM_START_TAG.equals(parser.getName())) {
                    String configKey = parser.getAttributeValue(null, XML_ATTRIBUTE_TAG);
                    String packageName = parser.nextText();
                    if (!(configKey == null || packageName == null)) {
                        configMap.put(configKey, packageName);
                    }
                }
            } while (type != 1);
            try {
                stream2.close();
            } catch (IOException e) {
                e = e;
                str = TAG;
                sb = new StringBuilder();
            }
        } catch (NullPointerException e2) {
            Log.i(TAG, "failed parsing " + e2);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e3) {
                    e = e3;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (NumberFormatException e4) {
            Log.i(TAG, "failed parsing " + e4);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e5) {
                    e = e5;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (XmlPullParserException e6) {
            Log.i(TAG, "failed parsing " + e6);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e7) {
                    e = e7;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e8) {
            Log.i(TAG, "failed IOException " + e8);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e9) {
                    e = e9;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (IndexOutOfBoundsException e10) {
            Log.i(TAG, "failed parsing " + e10);
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e11) {
                    e = e11;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    stream.close();
                } catch (IOException e12) {
                    Log.i(TAG, "Failed to close state FileInputStream " + e12);
                }
            }
            throw th;
        }
        return configMap;
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Log.i(str, sb.toString());
        return configMap;
    }

    public static boolean addProfileToXml(String path, String name, String packageName, String configKey) {
        Log.d(TAG, "addProfileToXml() path:" + path + ",name:" + name + ",packageName:" + packageName + ",configKey:" + configKey);
        HashMap<String, String> configMap = readXMLFile(path, name);
        if (configMap.isEmpty()) {
            configMap.put(configKey, packageName);
            return saveProfileToFile(path, name, configMap);
        }
        if (configMap.containsKey(configKey)) {
            String str = configMap.get(configKey);
            if (str.indexOf(packageName) >= 0) {
                return true;
            }
            configMap.put(configKey, str + "," + packageName);
        } else {
            configMap.put(configKey, packageName);
        }
        File file = new File(path, name);
        if (file.exists()) {
            file.delete();
        }
        return saveProfileToFile(path, name, configMap);
    }

    public static boolean removeProfileToXml(String path, String name, String packageName, String configKey) {
        HashMap<String, String> configMap = readXMLFile(path, name);
        if (!configMap.containsKey(configKey)) {
            Log.w(TAG, "removeProfileToXml() it is failed, and config is not contains");
            return false;
        }
        String str = configMap.get(configKey);
        if (packageName == null) {
            configMap.remove(configKey);
        } else {
            Log.d(TAG, "removeProfileToXml()--packageName str:" + str);
            int index = str.indexOf(packageName);
            int targetLength = packageName.length();
            if (index == 0) {
                str = str.substring(targetLength);
            } else if (index > 0) {
                str = str.substring(0, index - 1) + str.substring(index + targetLength);
            }
            if (str.length() == 0) {
                configMap.remove(configKey);
            } else {
                if (str.startsWith(",")) {
                    str = str.substring(1);
                }
                configMap.put(configKey, str);
            }
        }
        File file = new File(path, name);
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception e) {
                Log.e(TAG, "removeProfileToXml: delete old file =" + e);
                return false;
            }
        }
        if (configMap.isEmpty()) {
            return true;
        }
        return saveProfileToFile(path, name, configMap);
    }
}
