package com.android.server.am;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import com.oppo.RomUpdateHelper;
import com.oppo.RomUpdateHelper.UpdateInfo;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoAppScaleHelper extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/oppo_app_scale_list.xml";
    public static final String FILTER_NAME = "oppo_app_scale_list";
    private static final String SYS_FILE_DIR = "system/etc/oppo_app_scale_list.xml";
    private static final String TAG = "AppScale";
    private String mGiFTPackageName = "";

    private class OppoAppScaleInfo extends UpdateInfo {
        int giftopen = 0;
        HashMap<String, Float> map = new HashMap();
        HashMap<String, String> mapgift = new HashMap();

        public OppoAppScaleInfo() {
            super(OppoAppScaleHelper.this);
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x007e A:{SYNTHETIC, Splitter: B:24:0x007e} */
        /* JADX WARNING: Removed duplicated region for block: B:36:0x00aa A:{SYNTHETIC, Splitter: B:36:0x00aa} */
        /* JADX WARNING: Removed duplicated region for block: B:52:0x00e9 A:{SYNTHETIC, Splitter: B:52:0x00e9} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void parseContentFromXML(String content) {
            IOException e;
            XmlPullParserException e2;
            Throwable th;
            if (content != null) {
                OppoAppScaleHelper.this.changeFilePermisson(OppoAppScaleHelper.DATA_FILE_DIR);
                StringReader stringReader = null;
                String name = null;
                this.map.clear();
                String giftname = null;
                this.mapgift.clear();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader = new StringReader(content);
                    try {
                        parser.setInput(strReader);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    if (!parser.getName().equals("PackageName")) {
                                        if (!parser.getName().equals("scale")) {
                                            if (!parser.getName().equals("giftopen")) {
                                                if (!parser.getName().equals("giftname")) {
                                                    if (!parser.getName().equals("giftpara")) {
                                                        break;
                                                    }
                                                    eventType = parser.next();
                                                    String giftpara = parser.getText();
                                                    if (!(giftname == null || giftpara == null)) {
                                                        this.mapgift.put(giftname, giftpara);
                                                        break;
                                                    }
                                                }
                                                eventType = parser.next();
                                                giftname = parser.getText();
                                                break;
                                            }
                                            eventType = parser.next();
                                            this.giftopen = Integer.parseInt(parser.getText());
                                            break;
                                        }
                                        eventType = parser.next();
                                        float scale = Float.parseFloat(parser.getText());
                                        if (name == null) {
                                            break;
                                        }
                                        this.map.put(name, Float.valueOf(scale));
                                        break;
                                    }
                                    eventType = parser.next();
                                    name = parser.getText();
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (strReader != null) {
                            try {
                                strReader.close();
                            } catch (IOException e3) {
                                OppoAppScaleHelper.this.log("Got execption close permReader.", e3);
                            }
                        }
                    } catch (XmlPullParserException e4) {
                        e2 = e4;
                        stringReader = strReader;
                        try {
                            OppoAppScaleHelper.this.log("Got execption parsing permissions.", e2);
                            if (stringReader != null) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (stringReader != null) {
                                try {
                                    stringReader.close();
                                } catch (IOException e32) {
                                    OppoAppScaleHelper.this.log("Got execption close permReader.", e32);
                                }
                            }
                            throw th;
                        }
                    } catch (IOException e5) {
                        e32 = e5;
                        stringReader = strReader;
                        OppoAppScaleHelper.this.log("Got execption parsing permissions.", e32);
                        if (stringReader != null) {
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        stringReader = strReader;
                        if (stringReader != null) {
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e6) {
                    e2 = e6;
                    OppoAppScaleHelper.this.log("Got execption parsing permissions.", e2);
                    if (stringReader != null) {
                        try {
                            stringReader.close();
                        } catch (IOException e322) {
                            OppoAppScaleHelper.this.log("Got execption close permReader.", e322);
                        }
                    }
                } catch (IOException e7) {
                    e322 = e7;
                    OppoAppScaleHelper.this.log("Got execption parsing permissions.", e322);
                    if (stringReader != null) {
                        try {
                            stringReader.close();
                        } catch (IOException e3222) {
                            OppoAppScaleHelper.this.log("Got execption close permReader.", e3222);
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:4:0x000c, code:
            return 1.0f;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public float GetScale(String packageName) {
            if (packageName == null || this.map.isEmpty() || !this.map.containsKey(packageName)) {
                return 1.0f;
            }
            float scale = ((Float) this.map.get(packageName)).floatValue();
            Log.i(OppoAppScaleHelper.TAG, "packageName:" + packageName + " scale:" + scale);
            return scale;
        }

        public boolean getGiFTInit() {
            boolean tempopen = false;
            if (this.giftopen == 1) {
                tempopen = SystemProperties.getBoolean("sys.oppo.gift", false);
            }
            Log.i(OppoAppScaleHelper.TAG, "gitf feature is :" + tempopen);
            return tempopen;
        }

        public String getGiFTPara(String packageName) {
            if (packageName == null || this.mapgift.isEmpty()) {
                Log.i(OppoAppScaleHelper.TAG, "packageName:" + packageName);
                Log.i(OppoAppScaleHelper.TAG, "empty=" + this.mapgift.isEmpty());
                return null;
            } else if (this.mapgift.containsKey(packageName)) {
                return (String) this.mapgift.get(packageName);
            } else {
                return null;
            }
        }
    }

    private void changeFilePermisson(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            Log.i(TAG, "setReadable result :" + file.setReadable(true, false));
            return;
        }
        Log.i(TAG, "filename :" + filename + " is not exist");
    }

    public OppoAppScaleHelper(Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new OppoAppScaleInfo(), new OppoAppScaleInfo());
        try {
            init();
            changeFilePermisson(DATA_FILE_DIR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float GetScale(String packageName) {
        return ((OppoAppScaleInfo) getUpdateInfo(true)).GetScale(packageName);
    }

    public boolean getGiFTInit() {
        return ((OppoAppScaleInfo) getUpdateInfo(true)).getGiFTInit();
    }

    public String getGiFTPara(String packageName) {
        if (packageName != null && packageName == this.mGiFTPackageName) {
            return null;
        }
        this.mGiFTPackageName = packageName;
        return ((OppoAppScaleInfo) getUpdateInfo(true)).getGiFTPara(packageName);
    }
}
