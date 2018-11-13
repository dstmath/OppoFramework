package com.oppo.theme;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OppoApkChangedInfo {
    public static final String APK_CHANGED = "ApkChanged.xml";
    private static ArrayList<String> mAllPackageNames = new ArrayList();
    private static String mCurrentTag = null;
    private static boolean mParseError = false;

    static class IconXmlHandler extends DefaultHandler {
        IconXmlHandler() {
        }

        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            OppoApkChangedInfo.mCurrentTag = localName;
            if (OppoApkChangedInfo.mCurrentTag.equalsIgnoreCase("package")) {
                String packageName = attributes.getValue("name");
                if (packageName != null) {
                    OppoApkChangedInfo.mAllPackageNames.add(packageName);
                }
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
        }

        public void endElement(String uri, String localName, String name) throws SAXException {
            OppoApkChangedInfo.mCurrentTag = null;
        }
    }

    private static void parseXml(InputStream inStream) throws Exception {
        SAXParserFactory.newInstance().newSAXParser().parse(inStream, new IconXmlHandler());
        inStream.close();
    }

    public static boolean parseIconXml() {
        mAllPackageNames.clear();
        try {
            InputStream input;
            File file = new File("/data/theme/ApkChanged.xml");
            if (file.exists()) {
                input = new FileInputStream(file);
            } else {
                input = new FileInputStream("/system/media/theme/default/ApkChanged.xml");
            }
            parseXml(input);
            input.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean themeChangeEnable(String packageName) {
        if (mAllPackageNames.contains(packageName)) {
            return true;
        }
        return false;
    }

    public static boolean themeChangeEnable(boolean hasValue, String packageName) {
        if (packageName != null && packageName.equalsIgnoreCase("com.android.systemui")) {
            return true;
        }
        if (!hasValue || mParseError) {
            return false;
        }
        if (getApksNumbers() > 0 || parseIconXml()) {
            return mAllPackageNames.contains(packageName);
        } else {
            mParseError = true;
            return false;
        }
    }

    public static int getApksNumbers() {
        return mAllPackageNames.size();
    }
}
