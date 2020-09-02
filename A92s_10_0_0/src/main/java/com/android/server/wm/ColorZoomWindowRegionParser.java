package com.android.server.wm;

import android.graphics.Rect;
import android.util.Slog;
import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import java.io.IOException;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ColorZoomWindowRegionParser extends ColorBaseRUSParser {
    private static final String BOTTOM_TAG = "bottom";
    private static final String LEFT_TAG = "left";
    private static final String RECT_TAG = "rect";
    private static final String RIGHT_TAG = "right";
    private static final String TAG = "ColorZoomWindowRegionParser";
    private static final String TOP_TAG = "top";
    private static volatile ColorZoomWindowRegionParser sColorZoomWindowRegionParser = null;

    private ColorZoomWindowRegionParser() {
    }

    public static ColorZoomWindowRegionParser getInstance() {
        if (sColorZoomWindowRegionParser == null) {
            synchronized (ColorZoomWindowRegionParser.class) {
                if (sColorZoomWindowRegionParser == null) {
                    sColorZoomWindowRegionParser = new ColorZoomWindowRegionParser();
                }
            }
        }
        return sColorZoomWindowRegionParser;
    }

    private Rect parseRect(XmlPullParser parser) {
        Rect rect = new Rect();
        try {
            int left = Integer.parseInt(parser.getAttributeValue(null, LEFT_TAG));
            int top = Integer.parseInt(parser.getAttributeValue(null, TOP_TAG));
            int right = Integer.parseInt(parser.getAttributeValue(null, RIGHT_TAG));
            int bottom = Integer.parseInt(parser.getAttributeValue(null, BOTTOM_TAG));
            if (left <= right && top <= bottom) {
                rect.set(left, top, right, bottom);
            }
            return rect;
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Failed to parse zoom window rect : " + e.getLocalizedMessage());
            e.printStackTrace();
            return rect;
        }
    }

    public void parseZoomWindowRegion(XmlPullParser parser, ColorZoomWindowRUSConfig config) throws IOException, XmlPullParserException {
        List<Rect> rectList = config.getColorZoomWindowRegion().getRectList();
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4 || !parser.getName().equals(RECT_TAG))) {
                rectList.add(parseRect(parser));
            }
        }
    }

    @Override // com.android.server.wm.ColorBaseRUSParser
    public void writeXMLFile(XmlSerializer serializer, ColorZoomWindowRUSConfig config) throws IOException {
        List<Rect> list = config.getColorZoomWindowRegion().getRectList();
        for (int i = 0; i < list.size(); i++) {
            Rect rect = list.get(i);
            serializer.startTag(null, RECT_TAG);
            serializer.attribute(null, LEFT_TAG, Integer.toString(rect.left));
            serializer.attribute(null, TOP_TAG, Integer.toString(rect.top));
            serializer.attribute(null, RIGHT_TAG, Integer.toString(rect.right));
            serializer.attribute(null, BOTTOM_TAG, Integer.toString(rect.bottom));
            serializer.endTag(null, RECT_TAG);
        }
    }
}
