package com.android.server.wm;

import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import java.io.IOException;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public abstract class ColorBaseRUSParser implements IColorRUSParser {
    private static final String PKG_TAG = "pkg";

    @Override // com.android.server.wm.IColorRUSParser
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
            if (!(type == 3 || type == 4 || !parser.getName().equals(PKG_TAG))) {
                list.add(parser.nextText());
            }
        }
    }

    public void writeXMLFile(XmlSerializer serializer, ColorZoomWindowRUSConfig config) throws IOException {
        List<String> list = config.getPkgList();
        for (int i = 0; i < list.size(); i++) {
            serializer.startTag(null, PKG_TAG);
            serializer.text(list.get(i));
            serializer.endTag(null, PKG_TAG);
        }
    }
}
