package com.android.server.display;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import com.android.server.SystemService;
import com.android.server.usage.AppStandbyController;
import com.oppo.RomUpdateHelper;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoBrightnessWAArgsHelper extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/oppo_brightness_wa_args_cfg.xml";
    public static final String FILTER_NAME = "oppo_brightness_wa_args_cfg";
    private static final String SYS_FILE_DIR = "system/etc/oppo_brightness_wa_args_cfg.xml";
    private static final String TAG = "WABrightness";
    private OppoUpdateInfo mOppoUpdateInfo;

    public OppoBrightnessWAArgsHelper(Context context) {
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

    /* access modifiers changed from: private */
    public void changeFilePermisson(String filename) {
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

    public long getWASleepTime() {
        return this.mOppoUpdateInfo.waSleepTime;
    }

    public int getWAManualBrightness() {
        return this.mOppoUpdateInfo.waManualBrightness;
    }

    public int getWASensorLux() {
        return this.mOppoUpdateInfo.waSensorLux;
    }

    private class OppoUpdateInfo extends RomUpdateHelper.UpdateInfo {
        int isOpen = 0;
        /* access modifiers changed from: private */
        public int waManualBrightness = SystemService.PHASE_THIRD_PARTY_APPS_CAN_START;
        /* access modifiers changed from: private */
        public int waSensorLux = 60;
        /* access modifiers changed from: private */
        public long waSleepTime = AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT;

        public OppoUpdateInfo() {
            super(OppoBrightnessWAArgsHelper.this);
        }

        public void parseContentFromXML(String content) {
            if (content != null) {
                OppoBrightnessWAArgsHelper.this.changeFilePermisson(OppoBrightnessWAArgsHelper.DATA_FILE_DIR);
                FileReader xmlReader = null;
                StringReader strReader = null;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader2 = new StringReader(content);
                    parser.setInput(strReader2);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType != 0) {
                            if (eventType == 2) {
                                if (parser.getName().equals("ManualBrightness")) {
                                    parser.next();
                                    this.waManualBrightness = Integer.parseInt(parser.getText());
                                    Log.i(OppoBrightnessWAArgsHelper.TAG, "wa brightness config waManualBrightness = " + this.waManualBrightness);
                                } else if (parser.getName().equals("SensorLux")) {
                                    parser.next();
                                    this.waSensorLux = Integer.parseInt(parser.getText());
                                    Log.i(OppoBrightnessWAArgsHelper.TAG, "wa brightness config waSensorLux = " + this.waSensorLux);
                                } else if (parser.getName().equals("SleepTime")) {
                                    parser.next();
                                    this.waSleepTime = Long.parseLong(parser.getText());
                                    Log.i(OppoBrightnessWAArgsHelper.TAG, "wa brightness config waSleepTime = " + this.waSleepTime);
                                } else if (parser.getName().equals("isOpen")) {
                                    parser.next();
                                    this.isOpen = Integer.parseInt(parser.getText());
                                }
                            }
                        }
                    }
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e) {
                            Log.e(OppoBrightnessWAArgsHelper.TAG, "IOException close permReader : " + e.toString());
                            return;
                        }
                    }
                    strReader2.close();
                } catch (XmlPullParserException e2) {
                    Log.e(OppoBrightnessWAArgsHelper.TAG, "XmlPullParserException : " + e2.toString());
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e3) {
                            Log.e(OppoBrightnessWAArgsHelper.TAG, "IOException close permReader : " + e3.toString());
                            return;
                        }
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (IOException e4) {
                    Log.e(OppoBrightnessWAArgsHelper.TAG, "IOException : " + e4.toString());
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e5) {
                            Log.e(OppoBrightnessWAArgsHelper.TAG, "IOException close permReader : " + e5.toString());
                            return;
                        }
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (Throwable th) {
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e6) {
                            Log.e(OppoBrightnessWAArgsHelper.TAG, "IOException close permReader : " + e6.toString());
                            throw th;
                        }
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                    throw th;
                }
            }
        }
    }
}
