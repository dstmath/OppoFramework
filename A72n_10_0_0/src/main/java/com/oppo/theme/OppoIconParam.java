package com.oppo.theme;

import android.util.Log;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OppoIconParam {
    private static final boolean LOGE = false;
    private static final String TAG = "ThemeDiscription";
    private static final String TAG_DETECT_MASK_BORDER_OFFSET = "DetectMaskBorderOffset";
    private static final String TAG_SCALE = "Scale";
    private static final String TAG_XOFFSETPCT = "XOffsetPCT";
    private static final String TAG_YOFFSETPCT = "YOffsetPCT";
    public String mCurrentTag;
    public float mDetectMaskBorderOffset = 0.065f;
    public String mPath;
    public float mScale = 0.0f;
    public float mXOffsetPCT = 0.0f;
    public float mYOffsetPCT = 0.0f;

    public OppoIconParam(String path) {
        this.mPath = path;
    }

    public void myLog(String str) {
    }

    public String getPath() {
        return this.mPath;
    }

    /* access modifiers changed from: package-private */
    public class ThemeXmlHandler extends DefaultHandler {
        ThemeXmlHandler() {
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            OppoIconParam.this.mCurrentTag = localName;
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void characters(char[] ch, int start, int length) throws SAXException {
            String value = new String(ch, start, length);
            if (OppoIconParam.TAG_SCALE.equals(OppoIconParam.this.mCurrentTag)) {
                OppoIconParam.this.mScale = Float.parseFloat(value);
            } else if (OppoIconParam.TAG_XOFFSETPCT.equals(OppoIconParam.this.mCurrentTag)) {
                OppoIconParam.this.mXOffsetPCT = Float.parseFloat(value);
            } else if (OppoIconParam.TAG_YOFFSETPCT.equals(OppoIconParam.this.mCurrentTag)) {
                OppoIconParam.this.mYOffsetPCT = Float.parseFloat(value);
            } else if (OppoIconParam.TAG_DETECT_MASK_BORDER_OFFSET.equals(OppoIconParam.this.mCurrentTag)) {
                OppoIconParam.this.mDetectMaskBorderOffset = Float.parseFloat(value);
            }
        }

        @Override // org.xml.sax.helpers.DefaultHandler, org.xml.sax.ContentHandler
        public void endElement(String uri, String localName, String name) throws SAXException {
            OppoIconParam.this.mCurrentTag = null;
        }
    }

    public boolean parseXml() {
        String path;
        if (!OppoThirdPartUtil.mIsDefaultTheme) {
            path = "/data/theme/";
        } else {
            path = "/system/media/theme/default/";
        }
        try {
            ZipFile param = new ZipFile(path + "com.oppo.launcher");
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
            return true;
        } catch (ZipException e) {
            myLog("parseXml:ZipFile is destroyed, mPath = " + this.mPath);
            return false;
        } catch (Exception ex) {
            Log.e(TAG, "parseXml. ex = " + ex);
            return false;
        }
    }

    public boolean parseXmlForUser(int userId) {
        String path;
        if (!OppoThirdPartUtil.mIsDefaultTheme) {
            path = OppoThirdPartUtil.getThemePathForUser(userId);
        } else {
            path = "/system/media/theme/default/";
        }
        try {
            ZipFile param = new ZipFile(path + "com.oppo.launcher");
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
            return true;
        } catch (ZipException e) {
            myLog("parseXml:ZipFile is destroyed, mPath = " + this.mPath);
            return false;
        } catch (Exception ex) {
            Log.e(TAG, "parseXml. ex = " + ex);
            return false;
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

    public float getDetectMaskBorderOffset() {
        return this.mDetectMaskBorderOffset;
    }
}
