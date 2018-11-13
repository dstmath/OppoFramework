package com.qrd.wappush;

import android.content.Context;
import android.net.Uri;
import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.SAXException;

public interface IWapPushHandler {
    long getThreadID();

    Uri handleWapPush(InputStream inputStream, String str, Context context, int i, String str2) throws SAXException, IOException;
}
