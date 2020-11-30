package com.oppo.enterprise.mdmcoreservice.utils;

import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PackageListXMLUtil {
    public static final String INTERACTION_INS_UNINS_APP_XML_NAME = "interaction_ins_unins_list.xml";
    public static final String SILENT_INS_UNINS_APP_XML_NAME = "slient_ins_unins_list.xml";
    private static final String TAG = "PackageListXMLUtil";

    /* JADX INFO: Multiple debug info for r1v1 java.io.FileOutputStream: [D('fileos' java.io.FileOutputStream), D('e' java.io.IOException)] */
    public static boolean saveListToFile(String path, String name, List<String> packageNames, List<String> attrs) {
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
            serializer.startTag(null, "packages");
            for (int i = 0; i < packageNames.size(); i++) {
                String pkg = packageNames.get(i);
                if (pkg != null) {
                    serializer.startTag(null, "package");
                    serializer.attribute(null, "cer", attrs.get(i));
                    serializer.text(pkg);
                    serializer.endTag(null, "package");
                }
            }
            serializer.endTag(null, "packages");
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

    public static Map<String, String> readXMLFile(String path, String name) {
        String str;
        StringBuilder sb;
        int type;
        Map<String, String> map = new HashMap<>();
        File file = new File(path, name);
        if (!file.exists()) {
            return map;
        }
        FileInputStream stream = null;
        try {
            FileInputStream stream2 = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream2, null);
            do {
                type = parser.next();
                if (type == 2 && "package".equals(parser.getName())) {
                    String att = parser.getAttributeValue(null, "cer");
                    String pkg = parser.nextText();
                    if (!(pkg == null || att == null)) {
                        map.put(pkg, att);
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
        return map;
        sb.append("Failed to close state FileInputStream ");
        sb.append(e);
        Log.i(str, sb.toString());
        return map;
    }
}
