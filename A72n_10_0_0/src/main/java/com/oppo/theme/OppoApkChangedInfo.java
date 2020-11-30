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
    private static ArrayList<String> mAllPackageNames = new ArrayList<>();
    private static String mCurrentTag = null;
    private static boolean mParseError = false;

    /* access modifiers changed from: package-private */
    public static class IconXmlHandler extends DefaultHandler {
        IconXmlHandler() {
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            String packageName;
            String unused = OppoApkChangedInfo.mCurrentTag = localName;
            if (OppoApkChangedInfo.mCurrentTag.equalsIgnoreCase("package") && (packageName = attributes.getValue("name")) != null) {
                OppoApkChangedInfo.mAllPackageNames.add(packageName);
            }
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void characters(char[] ch, int start, int length) throws SAXException {
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void endElement(String uri, String localName, String name) throws SAXException {
            String unused = OppoApkChangedInfo.mCurrentTag = null;
        }
    }

    private static void parseXml(InputStream inStream) throws Exception {
        SAXParserFactory.newInstance().newSAXParser().parse(inStream, new IconXmlHandler());
        inStream.close();
    }

    public static boolean parseIconXml() {
        InputStream input;
        mAllPackageNames.clear();
        try {
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
        if (getApksNumbers() <= 0 && !parseIconXml()) {
            mParseError = true;
            return false;
        } else if (mAllPackageNames.contains(packageName)) {
            return true;
        } else {
            return false;
        }
    }

    public static int getApksNumbers() {
        return mAllPackageNames.size();
    }
}
