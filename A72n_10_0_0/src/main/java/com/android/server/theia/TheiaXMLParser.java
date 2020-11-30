package com.android.server.theia;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import com.android.server.SystemService;
import com.android.server.usage.AppStandbyController;
import com.oppo.util.RomUpdateHelper;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class TheiaXMLParser extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/bsp_stability_theia_switch.xml";
    public static final String FILTER_NAME = "bsp_stability_theia_switch";
    private static final String SYS_FILE_DIR = "system/etc/bsp_stability_theia_switch.xml";
    private static final String TAG = "TheiaXMLParser";
    private static volatile TheiaXMLParser mTheiaXMLParser;
    private OppoUpdateInfo mOppoUpdateInfo;

    public TheiaXMLParser(Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        this.mOppoUpdateInfo = null;
        this.mOppoUpdateInfo = new OppoUpdateInfo();
        OppoUpdateInfo oppoUpdateInfo = this.mOppoUpdateInfo;
        setUpdateInfo(oppoUpdateInfo, oppoUpdateInfo);
        try {
            init();
            changeFilePermisson(DATA_FILE_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TheiaXMLParser getInstance(Context context) {
        if (mTheiaXMLParser == null) {
            synchronized (TheiaXMLParser.class) {
                if (mTheiaXMLParser == null) {
                    mTheiaXMLParser = new TheiaXMLParser(context);
                }
            }
        }
        return mTheiaXMLParser;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeFilePermisson(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            boolean result = file.setReadable(true, false);
            Log.i(TAG, "setReadable result :" + result);
            return;
        }
        Log.i(TAG, "filename :" + filename + " is not exist");
    }

    public boolean isOpen() {
        return this.mOppoUpdateInfo.isOpen == 1;
    }

    public boolean getNoFocusWindowEnable() {
        return this.mOppoUpdateInfo.noFocusWindow == 1;
    }

    public boolean getUITimeoutEnable() {
        return this.mOppoUpdateInfo.uiTimeout == 1;
    }

    public boolean getAppNotRespondingEnable() {
        return this.mOppoUpdateInfo.appNotResponding == 1;
    }

    public boolean getBootFailedEnable() {
        return this.mOppoUpdateInfo.bootFailed == 1;
    }

    public boolean getBackKeyEnable() {
        return this.mOppoUpdateInfo.backKey == 1;
    }

    public boolean getUICrashEnable() {
        return this.mOppoUpdateInfo.systemUiCrash == 1;
    }

    /* access modifiers changed from: private */
    public class OppoUpdateInfo extends RomUpdateHelper.UpdateInfo {
        private int appNotResponding = 0;
        private int backKey = 0;
        private int bootFailed = 0;
        int isOpen = 0;
        private int noFocusWindow = 0;
        private int systemUiCrash = 0;
        private int uiTimeout = 0;
        private int waManualBrightness = SystemService.PHASE_THIRD_PARTY_APPS_CAN_START;
        private int waSensorLux = 60;
        private long waSleepTime = AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT;

        public OppoUpdateInfo() {
            super(TheiaXMLParser.this);
        }

        public void parseContentFromXML(String content) {
            if (content != null) {
                TheiaXMLParser.this.changeFilePermisson(TheiaXMLParser.DATA_FILE_DIR);
                FileReader xmlReader = null;
                StringReader strReader = null;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader2 = new StringReader(content);
                    parser.setInput(strReader2);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType != 0) {
                            if (eventType == 2) {
                                if (parser.getName().equals("noFocusWindow")) {
                                    parser.next();
                                    this.noFocusWindow = Integer.parseInt(parser.getText());
                                    Log.i(TheiaXMLParser.TAG, "noFocusWindow config noFocusWindow = " + this.noFocusWindow);
                                } else if (parser.getName().equals("uiTimeout")) {
                                    parser.next();
                                    this.uiTimeout = Integer.parseInt(parser.getText());
                                    Log.i(TheiaXMLParser.TAG, "uiTimeout config uiTimeout = " + this.uiTimeout);
                                } else if (parser.getName().equals("appNotResponding")) {
                                    parser.next();
                                    this.appNotResponding = Integer.parseInt(parser.getText());
                                    Log.i(TheiaXMLParser.TAG, "appNotResponding config appNotResponding = " + this.appNotResponding);
                                } else if (parser.getName().equals("bootFailed")) {
                                    parser.next();
                                    this.bootFailed = Integer.parseInt(parser.getText());
                                    Log.i(TheiaXMLParser.TAG, "bootFailed config bootFailed = " + this.bootFailed);
                                } else if (parser.getName().equals("backKey")) {
                                    parser.next();
                                    this.backKey = Integer.parseInt(parser.getText());
                                    Log.i(TheiaXMLParser.TAG, "backKey config backKey = " + this.backKey);
                                } else if (parser.getName().equals("systemUiCrash")) {
                                    parser.next();
                                    this.systemUiCrash = Integer.parseInt(parser.getText());
                                    Log.i(TheiaXMLParser.TAG, "systemUiCrash config systemUiCrash = " + this.systemUiCrash);
                                }
                            }
                        }
                    }
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e) {
                            Log.e(TheiaXMLParser.TAG, "IOException close permReader : " + e.toString());
                            return;
                        }
                    }
                    strReader2.close();
                } catch (XmlPullParserException e2) {
                    Log.e(TheiaXMLParser.TAG, "XmlPullParserException : " + e2.toString());
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e3) {
                            Log.e(TheiaXMLParser.TAG, "IOException close permReader : " + e3.toString());
                            return;
                        }
                    }
                    if (0 != 0) {
                        strReader.close();
                    }
                } catch (IOException e4) {
                    Log.e(TheiaXMLParser.TAG, "IOException : " + e4.toString());
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e5) {
                            Log.e(TheiaXMLParser.TAG, "IOException close permReader : " + e5.toString());
                            return;
                        }
                    }
                    if (0 != 0) {
                        strReader.close();
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e6) {
                            Log.e(TheiaXMLParser.TAG, "IOException close permReader : " + e6.toString());
                            throw th;
                        }
                    }
                    if (0 != 0) {
                        strReader.close();
                    }
                    throw th;
                }
            }
        }
    }
}
