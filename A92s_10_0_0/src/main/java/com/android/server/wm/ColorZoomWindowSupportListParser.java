package com.android.server.wm;

public class ColorZoomWindowSupportListParser extends ColorBaseRUSParser {
    private static volatile ColorZoomWindowSupportListParser sColorZoomWindowSupportListParser = null;

    private ColorZoomWindowSupportListParser() {
    }

    public static ColorZoomWindowSupportListParser getInstance() {
        if (sColorZoomWindowSupportListParser == null) {
            synchronized (ColorZoomWindowSupportListParser.class) {
                if (sColorZoomWindowSupportListParser == null) {
                    sColorZoomWindowSupportListParser = new ColorZoomWindowSupportListParser();
                }
            }
        }
        return sColorZoomWindowSupportListParser;
    }
}
