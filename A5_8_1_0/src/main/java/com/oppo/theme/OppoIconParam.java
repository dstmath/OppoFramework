package com.oppo.theme;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OppoIconParam {
    private static final String TAG = "ThemeDiscription";
    private static final String TAG_SCALE = "Scale";
    private static final String TAG_XOFFSETPCT = "XOffsetPCT";
    private static final String TAG_YOFFSETPCT = "YOffsetPCT";
    private final boolean LOGE = false;
    String mCurrentTag;
    public String mPath;
    public float mScale = 0.0f;
    public float mXOffsetPCT = 0.0f;
    public float mYOffsetPCT = 0.0f;

    class ThemeXmlHandler extends DefaultHandler {
        ThemeXmlHandler() {
        }

        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            OppoIconParam.this.mCurrentTag = localName;
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            String value = new String(ch, start, length);
            if (OppoIconParam.TAG_SCALE.equals(OppoIconParam.this.mCurrentTag)) {
                OppoIconParam.this.mScale = Float.parseFloat(value);
            } else if (OppoIconParam.TAG_XOFFSETPCT.equals(OppoIconParam.this.mCurrentTag)) {
                OppoIconParam.this.mXOffsetPCT = Float.parseFloat(value);
            } else if (OppoIconParam.TAG_YOFFSETPCT.equals(OppoIconParam.this.mCurrentTag)) {
                OppoIconParam.this.mYOffsetPCT = Float.parseFloat(value);
            }
        }

        public void endElement(String uri, String localName, String name) throws SAXException {
            OppoIconParam.this.mCurrentTag = null;
        }
    }

    public OppoIconParam(String path) {
        this.mPath = path;
    }

    public void myLog(String str) {
    }

    public String getPath() {
        return this.mPath;
    }

    public boolean parseXml() {
        String path;
        if (OppoThirdPartUtil.mIsDefaultTheme) {
            path = "/system/media/theme/default/";
        } else {
            path = "/data/theme/";
        }
        try {
            ZipFile param = new ZipFile(path + OppoThirdPartUtil.ZIPLAUNCHER);
            try {
                ZipEntry entry = param.getEntry(this.mPath);
                if (entry == null) {
                    param.close();
                    myLog("parseXml:entry is null");
                    return false;
                }
                InputStream input = param.getInputStream(entry);
                if (input == null) {
                    param.close();
                    myLog("parseXml:input is null");
                    return false;
                }
                SAXParserFactory.newInstance().newSAXParser().parse(input, new ThemeXmlHandler());
                input.close();
                param.close();
                ZipFile zipFile = param;
                return true;
            } catch (ZipException e) {
                myLog("parseXml:ZipFile is destroyed, mPath = " + this.mPath);
                return false;
            } catch (Exception e2) {
            }
        } catch (ZipException e3) {
            myLog("parseXml:ZipFile is destroyed, mPath = " + this.mPath);
            return false;
        } catch (Exception e4) {
        }
    }

    public float getScale() {
        return this.mScale;
    }

    public float getXOffset() {
        return this.mXOffsetPCT;
    }

    public float getYOffset() {
        return this.mYOffsetPCT;
    }
}
