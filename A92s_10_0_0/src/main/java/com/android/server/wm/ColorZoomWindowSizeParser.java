package com.android.server.wm;

import android.util.Slog;
import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import com.color.zoomwindow.ColorZoomWindowSize;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class ColorZoomWindowSizeParser extends ColorBaseRUSParser {
    private static final String LANDSCAPE_HEIGHT_TAG = "landscape_height";
    private static final String LANDSCAPE_WIDTH_TAG = "landscape_width";
    private static final String PORTRAIT_HEIGHT_TAG = "portrait_height";
    private static final String PORTRAIT_WIDTH_TAG = "portrait_width";
    private static final String TAG = "ColorZoomWindowSizeParser";
    private static volatile ColorZoomWindowSizeParser sColorZoomWindowSizeParser = null;

    private ColorZoomWindowSizeParser() {
    }

    public static ColorZoomWindowSizeParser getInstance() {
        if (sColorZoomWindowSizeParser == null) {
            synchronized (ColorZoomWindowSizeParser.class) {
                if (sColorZoomWindowSizeParser == null) {
                    sColorZoomWindowSizeParser = new ColorZoomWindowSizeParser();
                }
            }
        }
        return sColorZoomWindowSizeParser;
    }

    public void parseZoomWindowSize(XmlPullParser parser, ColorZoomWindowRUSConfig config) {
        try {
            config.getColorZoomWindowSize().setZoomWindowSize(Integer.parseInt(parser.getAttributeValue(null, PORTRAIT_WIDTH_TAG)), Integer.parseInt(parser.getAttributeValue(null, PORTRAIT_HEIGHT_TAG)), Integer.parseInt(parser.getAttributeValue(null, LANDSCAPE_WIDTH_TAG)), Integer.parseInt(parser.getAttributeValue(null, LANDSCAPE_HEIGHT_TAG)));
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Failed to parse zoom window size : " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override // com.android.server.wm.ColorBaseRUSParser
    public void writeXMLFile(XmlSerializer serializer, ColorZoomWindowRUSConfig config) throws IOException {
        ColorZoomWindowSize size = config.getColorZoomWindowSize();
        serializer.attribute(null, PORTRAIT_WIDTH_TAG, Integer.toString(size.getPortraitWidth()));
        serializer.attribute(null, PORTRAIT_HEIGHT_TAG, Integer.toString(size.getPortraitHeight()));
        serializer.attribute(null, LANDSCAPE_WIDTH_TAG, Integer.toString(size.getLandScapeWidth()));
        serializer.attribute(null, LANDSCAPE_HEIGHT_TAG, Integer.toString(size.getLandScapeHeight()));
    }
}
