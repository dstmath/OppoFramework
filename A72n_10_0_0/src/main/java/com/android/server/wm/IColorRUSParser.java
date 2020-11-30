package com.android.server.wm;

import java.io.IOException;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public interface IColorRUSParser {
    void parseRUSFile(XmlPullParser xmlPullParser, List<String> list) throws IOException, XmlPullParserException;
}
