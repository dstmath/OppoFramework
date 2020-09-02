package com.android.server.wm;

import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import java.io.IOException;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ColorZoomWindowUnSupportCpnListParser extends ColorBaseRUSParser {
    private static final String CPN_PKG_TAG = "cpn";
    private static final String TAG = "ColorZoomWindowUnSupportCpnListParser";
    private static volatile ColorZoomWindowUnSupportCpnListParser sColorZoomWindowUnSupportCpnListParser = null;

    private ColorZoomWindowUnSupportCpnListParser() {
    }

    public static ColorZoomWindowUnSupportCpnListParser getInstance() {
        if (sColorZoomWindowUnSupportCpnListParser == null) {
            synchronized (ColorZoomWindowUnSupportCpnListParser.class) {
                if (sColorZoomWindowUnSupportCpnListParser == null) {
                    sColorZoomWindowUnSupportCpnListParser = new ColorZoomWindowUnSupportCpnListParser();
                }
            }
        }
        return sColorZoomWindowUnSupportCpnListParser;
    }

    @Override // com.android.server.wm.ColorBaseRUSParser, com.android.server.wm.IColorRUSParser
    public void parseRUSFile(XmlPullParser parser, List<String> list) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(CPN_PKG_TAG))) {
                list.add(parser.nextText());
            }
        }
    }

    @Override // com.android.server.wm.ColorBaseRUSParser
    public void writeXMLFile(XmlSerializer serializer, ColorZoomWindowRUSConfig config) throws IOException {
        List<String> list = config.getUnSupportCpnList();
        for (int i = 0; i < list.size(); i++) {
            serializer.startTag(null, CPN_PKG_TAG);
            serializer.text(list.get(i));
            serializer.endTag(null, CPN_PKG_TAG);
        }
    }
}
