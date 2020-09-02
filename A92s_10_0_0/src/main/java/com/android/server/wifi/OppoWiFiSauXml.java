package com.android.server.wifi;

import android.util.Log;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoWiFiSauXml {
    private static final String TAG = "OppoWifiSauXml";

    private static void gotoStartTag(XmlPullParser in) throws XmlPullParserException, IOException {
        int type = in.getEventType();
        while (type != 2 && type != 1) {
            type = in.next();
        }
    }

    private static void gotoEndTag(XmlPullParser in) throws XmlPullParserException, IOException {
        int type = in.getEventType();
        while (type != 3 && type != 1) {
            type = in.next();
        }
    }

    public static void gotoDocumentStart(XmlPullParser in, String headerName) throws XmlPullParserException, IOException {
        XmlUtils.beginDocument(in, headerName);
    }

    public static boolean gotoNextSectionOrEnd(XmlPullParser in, String[] headerName, int outerDepth) throws XmlPullParserException, IOException {
        if (!XmlUtils.nextElementWithin(in, outerDepth)) {
            return false;
        }
        headerName[0] = in.getName();
        return true;
    }

    public static boolean gotoNextSectionWithNameOrEnd(XmlPullParser in, String expectedName, int outerDepth) throws XmlPullParserException, IOException {
        String[] headerName = new String[1];
        if (!gotoNextSectionOrEnd(in, headerName, outerDepth)) {
            return false;
        }
        if (headerName[0].equals(expectedName)) {
            return true;
        }
        throw new XmlPullParserException("Next section name does not match expected name: " + expectedName);
    }

    public static void gotoNextSectionWithName(XmlPullParser in, String expectedName, int outerDepth) throws XmlPullParserException, IOException {
        if (!gotoNextSectionWithNameOrEnd(in, expectedName, outerDepth)) {
            throw new XmlPullParserException("Section not found. Expected: " + expectedName);
        }
    }

    public static boolean isNextSectionEnd(XmlPullParser in, int sectionDepth) throws XmlPullParserException, IOException {
        return !XmlUtils.nextElementWithin(in, sectionDepth);
    }

    public static Object readCurrentValue(XmlPullParser in, String[] valueName) throws XmlPullParserException, IOException {
        Object value = XmlUtils.readValueXml(in, valueName);
        gotoEndTag(in);
        return value;
    }

    public static Object readNextValueWithName(XmlPullParser in, String expectedName) throws XmlPullParserException, IOException {
        String[] valueName = new String[1];
        XmlUtils.nextElement(in);
        Object value = readCurrentValue(in, valueName);
        if (valueName[0].equals(expectedName)) {
            return value;
        }
        throw new XmlPullParserException("Value not found. Expected: " + expectedName + ", but got: " + valueName[0]);
    }

    public static class WifiUpdateObjXmlUtil {
        public static final String XML_TAG_EFFECT_METHOD = "EffectMethod";
        public static final String XML_TAG_MD5 = "MD5";
        public static final String XML_TAG_NAME = "Name";
        public static final String XML_TAG_PLATFORM = "Platform";
        public static final String XML_TAG_PUSH_TIME = "PushTime";
        public static final String XML_TAG_REASON = "Reason";
        public static final String XML_TAG_TYPE = "Type";

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x005c, code lost:
            if (r5.equals(com.android.server.wifi.OppoWiFiSauXml.WifiUpdateObjXmlUtil.XML_TAG_TYPE) != false) goto L_0x0088;
         */
        public static OppoWifiUpdateObj parseFromXml(XmlPullParser in, int outerTagDepth) throws XmlPullParserException, IOException {
            OppoWifiUpdateObj obj = new OppoWifiUpdateObj();
            while (!OppoWiFiSauXml.isNextSectionEnd(in, outerTagDepth)) {
                char c = 1;
                String[] valueName = new String[1];
                Object value = OppoWiFiSauXml.readCurrentValue(in, valueName);
                if (valueName[0] != null) {
                    Log.d(OppoWiFiSauXml.TAG, " parseFromXml name: " + valueName[0] + "value: " + value);
                    String str = valueName[0];
                    switch (str.hashCode()) {
                        case -1851097500:
                            if (str.equals(XML_TAG_REASON)) {
                                c = 6;
                                break;
                            }
                            c = 65535;
                            break;
                        case -405431822:
                            if (str.equals(XML_TAG_EFFECT_METHOD)) {
                                c = 4;
                                break;
                            }
                            c = 65535;
                            break;
                        case 76158:
                            if (str.equals(XML_TAG_MD5)) {
                                c = 3;
                                break;
                            }
                            c = 65535;
                            break;
                        case 2420395:
                            if (str.equals(XML_TAG_NAME)) {
                                c = 2;
                                break;
                            }
                            c = 65535;
                            break;
                        case 2622298:
                            break;
                        case 1840626983:
                            if (str.equals(XML_TAG_PUSH_TIME)) {
                                c = 5;
                                break;
                            }
                            c = 65535;
                            break;
                        case 1939328147:
                            if (str.equals(XML_TAG_PLATFORM)) {
                                c = 0;
                                break;
                            }
                            c = 65535;
                            break;
                        default:
                            c = 65535;
                            break;
                    }
                    switch (c) {
                        case 0:
                            obj.platform = (String) value;
                            break;
                        case 1:
                            obj.fileType = (String) value;
                            break;
                        case 2:
                            obj.fileName = (String) value;
                            break;
                        case 3:
                            obj.md5 = (String) value;
                            break;
                        case 4:
                            obj.effectMethod = (String) value;
                            break;
                        case 5:
                            obj.pushTime = (String) value;
                            break;
                        case 6:
                            obj.pushReason = (String) value;
                            break;
                        default:
                            Log.w(OppoWiFiSauXml.TAG, "Unknown value name: " + valueName[0] + "value: " + value);
                            break;
                    }
                } else {
                    Log.d(OppoWiFiSauXml.TAG, " parseFromXml fail");
                    throw new XmlPullParserException("Missing value name");
                }
            }
            return obj;
        }
    }
}
