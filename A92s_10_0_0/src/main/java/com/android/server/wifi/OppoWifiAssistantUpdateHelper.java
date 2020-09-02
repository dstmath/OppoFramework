package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.RomUpdateHelper;
import android.util.Log;
import android.util.Xml;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoWifiAssistantUpdateHelper extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/wifi_assistant_config.xml";
    public static final String FILTER_NAME = "wifi_assistant_config";
    private static final String SYS_FILE_DIR = "system/etc/wifi_assistant_config.xml";
    private static final String TAG = "WifiAssistantHelper";
    /* access modifiers changed from: private */
    public OppoWifiAssistantStateTraker mWnst;

    private class OppoWifiAssistantHelperInfo extends RomUpdateHelper.UpdateInfo {
        public OppoWifiAssistantHelperInfo() {
            super(OppoWifiAssistantUpdateHelper.this);
        }

        public void parseContentFromXML(String content) {
            if (content != null) {
                FileReader xmlReader = null;
                StringReader strReader = null;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader2 = new StringReader(content);
                    parser.setInput(strReader2);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType != 0) {
                            if (eventType == 2) {
                                String name = parser.getName();
                                parser.next();
                                OppoWifiAssistantUpdateHelper.this.mWnst.updateWifiNetworkConfig(name, parser.getText());
                            }
                        }
                    }
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e) {
                            OppoWifiAssistantUpdateHelper.this.log("Got execption close permReader.", e);
                        }
                    }
                    strReader2.close();
                } catch (XmlPullParserException e2) {
                    OppoWifiAssistantUpdateHelper.this.log("Got execption parsing permissions.", e2);
                    if (xmlReader != null) {
                        xmlReader.close();
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (IOException e3) {
                    OppoWifiAssistantUpdateHelper.this.log("Got execption parsing permissions.", e3);
                    if (xmlReader != null) {
                        xmlReader.close();
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (Throwable th) {
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e4) {
                            OppoWifiAssistantUpdateHelper.this.log("Got execption close permReader.", e4);
                            throw th;
                        }
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                    throw th;
                }
                Log.d(OppoWifiAssistantUpdateHelper.TAG, "parseContentFromXML " + dumpToString());
            }
        }

        public String dumpToString() {
            return new StringBuilder().toString();
        }
    }

    public OppoWifiAssistantUpdateHelper(Context context, OppoWifiAssistantStateTraker wnst) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        this.mWnst = wnst;
        setUpdateInfo(new OppoWifiAssistantHelperInfo(), new OppoWifiAssistantHelperInfo());
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initUpdateBroadcastReceiver();
    }

    public String dumpToString() {
        return getUpdateInfo(true).dumpToString();
    }
}
