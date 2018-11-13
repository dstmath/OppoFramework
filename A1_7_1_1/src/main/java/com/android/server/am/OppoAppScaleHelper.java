package com.android.server.am;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import com.oppo.RomUpdateHelper;
import com.oppo.RomUpdateHelper.UpdateInfo;
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

    private class OppoAppScaleInfo extends UpdateInfo {
        HashMap<String, Float> map = new HashMap();

        public OppoAppScaleInfo() {
            super(OppoAppScaleHelper.this);
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x006f A:{SYNTHETIC, Splitter: B:24:0x006f} */
        /* JADX WARNING: Removed duplicated region for block: B:37:0x008e A:{SYNTHETIC, Splitter: B:37:0x008e} */
        /* JADX WARNING: Removed duplicated region for block: B:46:0x00a9 A:{SYNTHETIC, Splitter: B:46:0x00a9} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void parseContentFromXML(String content) {
            IOException e;
            XmlPullParserException e2;
            Throwable th;
            if (content != null) {
                StringReader strReader = null;
                String name = null;
                this.map.clear();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader2 = new StringReader(content);
                    try {
                        parser.setInput(strReader2);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    if (!parser.getName().equals("PackageName")) {
                                        if (!parser.getName().equals("scale")) {
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
                        if (strReader2 != null) {
                            try {
                                strReader2.close();
                            } catch (IOException e3) {
                                OppoAppScaleHelper.this.log("Got execption close permReader.", e3);
                            }
                        }
                    } catch (XmlPullParserException e4) {
                        e2 = e4;
                        strReader = strReader2;
                        try {
                            OppoAppScaleHelper.this.log("Got execption parsing permissions.", e2);
                            if (strReader != null) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (strReader != null) {
                                try {
                                    strReader.close();
                                } catch (IOException e32) {
                                    OppoAppScaleHelper.this.log("Got execption close permReader.", e32);
                                }
                            }
                            throw th;
                        }
                    } catch (IOException e5) {
                        e32 = e5;
                        strReader = strReader2;
                        OppoAppScaleHelper.this.log("Got execption parsing permissions.", e32);
                        if (strReader != null) {
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        strReader = strReader2;
                        if (strReader != null) {
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e6) {
                    e2 = e6;
                    OppoAppScaleHelper.this.log("Got execption parsing permissions.", e2);
                    if (strReader != null) {
                        try {
                            strReader.close();
                        } catch (IOException e322) {
                            OppoAppScaleHelper.this.log("Got execption close permReader.", e322);
                        }
                    }
                } catch (IOException e7) {
                    e322 = e7;
                    OppoAppScaleHelper.this.log("Got execption parsing permissions.", e322);
                    if (strReader != null) {
                        try {
                            strReader.close();
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
    }

    public OppoAppScaleHelper(Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new OppoAppScaleInfo(), new OppoAppScaleInfo());
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float GetScale(String packageName) {
        return ((OppoAppScaleInfo) getUpdateInfo(true)).GetScale(packageName);
    }
}
