package com.mediatek.xcap.client;

import android.os.SystemProperties;
import android.util.Log;
import com.mediatek.simservs.client.SimServs;
import com.mediatek.simservs.xcap.XcapElement;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XcapDebugParam {
    private static final String TAG = "XcapDebugParam";
    private static final String TAG_ROOT = "DebugParam";
    private static final String TAG_XCAP_AUID = "XcapAUID";
    private static final String TAG_XCAP_DISABLE_ETAG = "DisableETag";
    private static final String TAG_XCAP_DOCUMENT_NAME = "XcapDocumentName";
    private static final String TAG_XCAP_ENABLE_HTTP_LOG = "EnableHttpLog";
    private static final String TAG_XCAP_ENABLE_PREDEFINED_SIMSERV_QUERY_RESULT = "EnablePredefinedSimservQueryResult";
    private static final String TAG_XCAP_ENABLE_PREDEFINED_SIMSERV_SETTING = "EnablePredefinedSimservSetting";
    private static final String TAG_XCAP_ENABLE_SIMSERV_QUERY_WHOLE = "EnableSimservQueryWhole";
    private static final String TAG_XCAP_ENABLE_TRUST_ALL = "EnableXcapTrustAll";
    private static final String TAG_XCAP_HTTP_DIGEST_PASSWORD = "HttpDigestPassword";
    private static final String TAG_XCAP_HTTP_DIGEST_USERNAME = "HttpDigestUsername";
    private static final String TAG_XCAP_PUT_ELEMENT_MIME = "XcapPutElementMime";
    private static final String TAG_XCAP_ROOT = "XcapRoot";
    private static final String TAG_XCAP_USER_AGENT = "XcapUserAgent";
    private static final String TAG_XCAP_XUI = "XcapXui";
    private static boolean mDisableETag = false;
    private static boolean mEnableHttpLog = false;
    private static boolean mEnablePredefinedSimservQueryResult = false;
    private static boolean mEnablePredefinedSimservSetting = false;
    private static boolean mEnableSimservQueryWhole = false;
    private static boolean mEnableXcapTrustAll = false;
    private static String mHttpDigestPassword;
    private static String mHttpDigestUsername;
    private static String mXcapAUID;
    private static String mXcapDocumentName;
    private static String mXcapPutElementMime;
    private static String mXcapRoot;
    private static String mXcapUserAgent;
    private static String mXcapXui;
    private static XcapDebugParam sInstance;

    public static XcapDebugParam getInstance() {
        if (sInstance == null) {
            sInstance = new XcapDebugParam();
        }
        return sInstance;
    }

    public void load() {
        Log.d(TAG, "XcapDebugParam is loading");
        readProperty();
        String xmlContent = readXmlFromFile("/data/misc/xcapconfig.xml");
        if (xmlContent != null && !xmlContent.equals("")) {
            try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xmlContent));
                NodeList debugParamNode = db.parse(is).getElementsByTagName(TAG_ROOT);
                if (debugParamNode.getLength() > 0) {
                    instantiateFromXmlNode(debugParamNode.item(0));
                }
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            } catch (ParserConfigurationException e3) {
                e3.printStackTrace();
            }
        }
    }

    public String readXmlFromFile(String file) {
        String text = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fis);
            while (true) {
                String buf = dis.readLine();
                if (buf != null) {
                    text = text + buf;
                } else {
                    fis.close();
                    return text;
                }
            }
        } catch (IOException e) {
            return null;
        }
    }

    public void readProperty() {
        String val = SystemProperties.get("persist.vendor.xcap." + TAG_XCAP_ROOT.toLowerCase());
        String str = null;
        mXcapRoot = (val == null || val.isEmpty()) ? null : val;
        String val2 = SystemProperties.get("persist.vendor.xcap." + TAG_XCAP_USER_AGENT.toLowerCase());
        mXcapUserAgent = (val2 == null || val2.isEmpty()) ? null : val2;
        String val3 = SystemProperties.get("persist.vendor.xcap." + TAG_XCAP_XUI.toLowerCase());
        mXcapXui = (val3 == null || val3.isEmpty()) ? null : val3;
        mEnableSimservQueryWhole = SystemProperties.getBoolean("persist.vendor.xcap.simservquerywhole", false);
        mEnableXcapTrustAll = SystemProperties.getBoolean("persist.vendor.xcap." + TAG_XCAP_ENABLE_TRUST_ALL.toLowerCase(), false);
        String val4 = SystemProperties.get("persist.vendor.xcap." + TAG_XCAP_DOCUMENT_NAME.toLowerCase());
        mXcapDocumentName = (val4 == null || val4.isEmpty()) ? null : val4;
        String val5 = SystemProperties.get("persist.vendor.xcap." + TAG_XCAP_PUT_ELEMENT_MIME.toLowerCase());
        mXcapPutElementMime = (val5 == null || val5.isEmpty()) ? null : val5;
        String val6 = SystemProperties.get("persist.vendor.xcap." + TAG_XCAP_AUID.toLowerCase());
        if (val6 != null && !val6.isEmpty()) {
            str = val6;
        }
        mXcapAUID = str;
        mDisableETag = SystemProperties.getBoolean("persist.vendor.xcap." + TAG_XCAP_DISABLE_ETAG.toLowerCase(), false);
        Log.d(TAG, "readProperty mXcapRoot: " + mXcapRoot + "\nmXcapUserAgent: " + mXcapUserAgent + "\nmXcapXui: " + mXcapXui + "\nmEnableSimservQueryWhole: " + mEnableSimservQueryWhole + "\nmEnableXcapTrustAll: " + mEnableXcapTrustAll + "\nmXcapDocumentName: " + mXcapDocumentName + "\nmXcapPutElementMime: " + mXcapPutElementMime + "\nmXcapAUID: " + mXcapAUID + "\nmDisableETag: " + mDisableETag + "\n");
    }

    private void reset() {
        mXcapRoot = null;
        mXcapUserAgent = null;
        mXcapXui = null;
        mHttpDigestUsername = null;
        mHttpDigestPassword = null;
        mEnablePredefinedSimservQueryResult = false;
        mEnablePredefinedSimservSetting = false;
        mEnableSimservQueryWhole = false;
        mEnableHttpLog = false;
        mEnableXcapTrustAll = false;
        mXcapDocumentName = SimServs.SIMSERVS_FILENAME;
        mXcapPutElementMime = null;
        mXcapAUID = null;
        mDisableETag = false;
    }

    private void instantiateFromXmlNode(Node domNode) {
        Log.d(TAG, "instantiateFromXmlNode");
        Element domElement = (Element) domNode;
        NodeList node = domElement.getElementsByTagName(TAG_XCAP_ROOT);
        if (node.getLength() > 0) {
            mXcapRoot = ((Element) node.item(0)).getTextContent();
        }
        NodeList node2 = domElement.getElementsByTagName(TAG_XCAP_USER_AGENT);
        if (node2.getLength() > 0) {
            mXcapUserAgent = ((Element) node2.item(0)).getTextContent();
        }
        NodeList node3 = domElement.getElementsByTagName(TAG_XCAP_XUI);
        if (node3.getLength() > 0) {
            mXcapXui = ((Element) node3.item(0)).getTextContent();
        }
        NodeList node4 = domElement.getElementsByTagName(TAG_XCAP_HTTP_DIGEST_USERNAME);
        if (node4.getLength() > 0) {
            mHttpDigestUsername = ((Element) node4.item(0)).getTextContent();
        }
        NodeList node5 = domElement.getElementsByTagName(TAG_XCAP_HTTP_DIGEST_PASSWORD);
        if (node5.getLength() > 0) {
            mHttpDigestPassword = ((Element) node5.item(0)).getTextContent();
        }
        NodeList node6 = domElement.getElementsByTagName(TAG_XCAP_ENABLE_PREDEFINED_SIMSERV_QUERY_RESULT);
        if (node6.getLength() > 0) {
            if (XcapElement.TRUE.equalsIgnoreCase(((Element) node6.item(0)).getTextContent())) {
                mEnablePredefinedSimservQueryResult = true;
            } else {
                mEnablePredefinedSimservQueryResult = false;
            }
        }
        NodeList node7 = domElement.getElementsByTagName(TAG_XCAP_ENABLE_PREDEFINED_SIMSERV_SETTING);
        if (node7.getLength() > 0) {
            if (XcapElement.TRUE.equalsIgnoreCase(((Element) node7.item(0)).getTextContent())) {
                mEnablePredefinedSimservSetting = true;
            } else {
                mEnablePredefinedSimservSetting = false;
            }
        }
        NodeList node8 = domElement.getElementsByTagName(TAG_XCAP_ENABLE_SIMSERV_QUERY_WHOLE);
        if (node8.getLength() > 0) {
            if (XcapElement.TRUE.equalsIgnoreCase(((Element) node8.item(0)).getTextContent())) {
                mEnableSimservQueryWhole = true;
            } else {
                mEnableSimservQueryWhole = false;
            }
        }
        NodeList node9 = domElement.getElementsByTagName(TAG_XCAP_ENABLE_HTTP_LOG);
        if (node9.getLength() > 0) {
            if (XcapElement.TRUE.equalsIgnoreCase(((Element) node9.item(0)).getTextContent())) {
                mEnableHttpLog = true;
            } else {
                mEnableHttpLog = false;
            }
        }
        NodeList node10 = domElement.getElementsByTagName(TAG_XCAP_ENABLE_TRUST_ALL);
        if (node10.getLength() > 0) {
            if (XcapElement.TRUE.equalsIgnoreCase(((Element) node10.item(0)).getTextContent())) {
                mEnableXcapTrustAll = true;
            } else {
                mEnableXcapTrustAll = false;
            }
        }
        NodeList node11 = domElement.getElementsByTagName(TAG_XCAP_DOCUMENT_NAME);
        if (node11.getLength() > 0) {
            mXcapDocumentName = ((Element) node11.item(0)).getTextContent();
        }
        NodeList node12 = domElement.getElementsByTagName(TAG_XCAP_PUT_ELEMENT_MIME);
        if (node12.getLength() > 0) {
            mXcapPutElementMime = ((Element) node12.item(0)).getTextContent();
        }
        NodeList node13 = domElement.getElementsByTagName(TAG_XCAP_AUID);
        if (node13.getLength() > 0) {
            mXcapAUID = ((Element) node13.item(0)).getTextContent();
        }
        NodeList node14 = domElement.getElementsByTagName(TAG_XCAP_DISABLE_ETAG);
        if (node14.getLength() <= 0) {
            return;
        }
        if (XcapElement.TRUE.equalsIgnoreCase(((Element) node14.item(0)).getTextContent())) {
            mDisableETag = true;
        } else {
            mDisableETag = false;
        }
    }

    public String getXcapRoot() {
        return mXcapRoot;
    }

    public String getXcapUserAgent() {
        return mXcapUserAgent;
    }

    public String getXcapXui() {
        return mXcapXui;
    }

    public String getHttpDigestUsername() {
        return mHttpDigestUsername;
    }

    public String getHttpDigestPassword() {
        return mHttpDigestPassword;
    }

    public boolean getEnablePredefinedSimservQueryResult() {
        return mEnablePredefinedSimservQueryResult;
    }

    public boolean getEnablePredefinedSimservSetting() {
        return mEnablePredefinedSimservSetting;
    }

    public boolean getEnableSimservQueryWhole() {
        return mEnableSimservQueryWhole;
    }

    public boolean getEnableHttpLog() {
        return mEnableHttpLog;
    }

    public boolean getEnableXcapTrustAll() {
        return mEnableXcapTrustAll;
    }

    public String getXcapDocumentName() {
        return mXcapDocumentName;
    }

    public String getXcapPutElementMime() {
        return mXcapPutElementMime;
    }

    public String getXcapAUID() {
        return mXcapAUID;
    }

    public boolean getDisableETag() {
        return mDisableETag;
    }

    public String toString() {
        return "mXcapRoot: " + mXcapRoot + "\nmXcapUserAgent: " + mXcapUserAgent + "\nmXcapXui: " + mXcapXui + "\nmHttpDigestUsername: " + mHttpDigestUsername + "\nmHttpDigestPassword: " + mHttpDigestPassword + "\nmEnablePredefinedSimservQueryResult: " + mEnablePredefinedSimservQueryResult + "\nmEnablePredefinedSimservSetting: " + mEnablePredefinedSimservSetting + "\nmEnableSimservQueryWhole: " + mEnableSimservQueryWhole + "\nmEnableHttpLog: " + mEnableHttpLog + "\nmEnableXcapTrustAll: " + mEnableXcapTrustAll + "\nmXcapDocumentName: " + mXcapDocumentName + "\nmXcapPutElementMime: " + mXcapPutElementMime + "\nmXcapAUID: " + mXcapAUID + "\nmDisableETag: " + mDisableETag + "\n";
    }
}
