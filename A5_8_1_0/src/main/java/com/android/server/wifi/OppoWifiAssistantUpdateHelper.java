package com.android.server.wifi;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import com.oppo.RomUpdateHelper;
import com.oppo.RomUpdateHelper.UpdateInfo;
import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoWifiAssistantUpdateHelper extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/wifi_assistant_config.xml";
    public static final String FILTER_NAME = "wifi_assistant_config";
    private static final String SYS_FILE_DIR = "system/etc/wifi_assistant_config.xml";
    private static final String TAG = "WifiAssistantHelper";
    private OppoWifiAssistantStateTraker mWnst;

    private class OppoWifiAssistantHelperInfo extends UpdateInfo {
        public OppoWifiAssistantHelperInfo() {
            super(OppoWifiAssistantUpdateHelper.this);
        }

        /* JADX WARNING: Removed duplicated region for block: B:39:0x009b A:{SYNTHETIC, Splitter: B:39:0x009b} */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x0042 A:{SYNTHETIC, Splitter: B:17:0x0042} */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0080 A:{SYNTHETIC, Splitter: B:31:0x0080} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void parseContentFromXML(String content) {
            IOException e;
            XmlPullParserException e2;
            Throwable th;
            if (content != null) {
                StringReader strReader = null;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader2 = new StringReader(content);
                    try {
                        parser.setInput(strReader2);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    String name = parser.getName();
                                    eventType = parser.next();
                                    OppoWifiAssistantUpdateHelper.this.mWnst.updateWifiNetworkConfig(name, parser.getText());
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (strReader2 != null) {
                            try {
                                strReader2.close();
                            } catch (IOException e3) {
                                OppoWifiAssistantUpdateHelper.this.log("Got execption close permReader.", e3);
                            }
                        }
                    } catch (XmlPullParserException e4) {
                        e2 = e4;
                        strReader = strReader2;
                        try {
                            OppoWifiAssistantUpdateHelper.this.log("Got execption parsing permissions.", e2);
                            if (strReader != null) {
                                try {
                                    strReader.close();
                                } catch (IOException e32) {
                                    OppoWifiAssistantUpdateHelper.this.log("Got execption close permReader.", e32);
                                }
                            }
                            Log.d(OppoWifiAssistantUpdateHelper.TAG, "parseContentFromXML " + dumpToString());
                        } catch (Throwable th2) {
                            th = th2;
                            if (strReader != null) {
                            }
                            throw th;
                        }
                    } catch (IOException e5) {
                        e32 = e5;
                        strReader = strReader2;
                        OppoWifiAssistantUpdateHelper.this.log("Got execption parsing permissions.", e32);
                        if (strReader != null) {
                            try {
                                strReader.close();
                            } catch (IOException e322) {
                                OppoWifiAssistantUpdateHelper.this.log("Got execption close permReader.", e322);
                            }
                        }
                        Log.d(OppoWifiAssistantUpdateHelper.TAG, "parseContentFromXML " + dumpToString());
                    } catch (Throwable th3) {
                        th = th3;
                        strReader = strReader2;
                        if (strReader != null) {
                            try {
                                strReader.close();
                            } catch (IOException e3222) {
                                OppoWifiAssistantUpdateHelper.this.log("Got execption close permReader.", e3222);
                            }
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e6) {
                    e2 = e6;
                    OppoWifiAssistantUpdateHelper.this.log("Got execption parsing permissions.", e2);
                    if (strReader != null) {
                    }
                    Log.d(OppoWifiAssistantUpdateHelper.TAG, "parseContentFromXML " + dumpToString());
                } catch (IOException e7) {
                    e3222 = e7;
                    OppoWifiAssistantUpdateHelper.this.log("Got execption parsing permissions.", e3222);
                    if (strReader != null) {
                    }
                    Log.d(OppoWifiAssistantUpdateHelper.TAG, "parseContentFromXML " + dumpToString());
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
        return ((OppoWifiAssistantHelperInfo) getUpdateInfo(true)).dumpToString();
    }
}
