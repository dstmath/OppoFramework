package com.android.server.wm;

import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import java.io.IOException;
import java.util.List;
import org.xmlpull.v1.XmlSerializer;

public class ColorZoomWindowSupportReplyListParser extends ColorBaseRUSParser {
    private static final String PKG_TAG = "pkg";
    private static volatile ColorZoomWindowSupportReplyListParser sColorZoomWindowSupportReplyListParser = null;

    private ColorZoomWindowSupportReplyListParser() {
    }

    public static ColorZoomWindowSupportReplyListParser getInstance() {
        if (sColorZoomWindowSupportReplyListParser == null) {
            synchronized (ColorZoomWindowSupportReplyListParser.class) {
                if (sColorZoomWindowSupportReplyListParser == null) {
                    sColorZoomWindowSupportReplyListParser = new ColorZoomWindowSupportReplyListParser();
                }
            }
        }
        return sColorZoomWindowSupportReplyListParser;
    }

    @Override // com.android.server.wm.ColorBaseRUSParser
    public void writeXMLFile(XmlSerializer serializer, ColorZoomWindowRUSConfig config) throws IOException {
        List<String> list = config.getReplyPkgList();
        for (int i = 0; i < list.size(); i++) {
            serializer.startTag(null, PKG_TAG);
            serializer.text(list.get(i));
            serializer.endTag(null, PKG_TAG);
        }
    }
}
