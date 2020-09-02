package com.android.server.oppo;

import android.content.Context;
import android.os.OppoBaseEnvironment;
import android.util.Log;
import android.util.Xml;
import com.oppo.OppoRomUpdateHelper;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoAppScaleHelper extends OppoRomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/oppo_app_scale_list.xml";
    public static final String FILTER_NAME = "oppo_app_scale_list";
    private static final String SYS_FILE_DIR = (OppoBaseEnvironment.getOppoProductDirectory().getAbsolutePath() + "/vendor/etc/oppo_app_scale_list.xml");
    private static final String TAG = "AppScale";
    /* access modifiers changed from: private */
    public boolean gloableHBMOn = false;
    /* access modifiers changed from: private */
    public float[] hbmLevel = new float[3];
    /* access modifiers changed from: private */
    public int mLowBrightnessThreshold = 100;
    /* access modifiers changed from: private */
    public float mReduceBrightnessRate = 1.0f;
    /* access modifiers changed from: private */
    public List<String> reduceBrightnessPackageList = new ArrayList();

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

    private class OppoAppScaleInfo extends OppoRomUpdateHelper.UpdateInfo {
        TimerTask mHBMTask;
        Timer mHBMTimer;
        HashMap<String, Float> map = new HashMap<>();
        HashMap<String, Float> newmap = new HashMap<>();

        public OppoAppScaleInfo() {
            super(OppoAppScaleHelper.this);
            OppoAppScaleHelper.this.hbmLevel[0] = 10000.0f;
            OppoAppScaleHelper.this.hbmLevel[1] = 20000.0f;
            OppoAppScaleHelper.this.hbmLevel[2] = 30000.0f;
        }

        /* JADX WARNING: Removed duplicated region for block: B:126:0x028d A[SYNTHETIC, Splitter:B:126:0x028d] */
        /* JADX WARNING: Removed duplicated region for block: B:131:0x0295 A[Catch:{ IOException -> 0x0291 }] */
        /* JADX WARNING: Removed duplicated region for block: B:136:0x02a6 A[SYNTHETIC, Splitter:B:136:0x02a6] */
        /* JADX WARNING: Removed duplicated region for block: B:141:0x02ae A[Catch:{ IOException -> 0x02aa }] */
        /* JADX WARNING: Removed duplicated region for block: B:144:0x02ba A[SYNTHETIC, Splitter:B:144:0x02ba] */
        /* JADX WARNING: Removed duplicated region for block: B:149:0x02c2 A[Catch:{ IOException -> 0x02be }] */
        /* JADX WARNING: Removed duplicated region for block: B:166:? A[RETURN, SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:169:? A[RETURN, SYNTHETIC] */
        public void parseContentFromXML(String content) {
            XmlPullParserException e;
            float hbmvalue;
            float scale;
            float hbmvalue2;
            if (content != null) {
                OppoAppScaleHelper.this.changeFilePermisson(OppoAppScaleHelper.DATA_FILE_DIR);
                FileReader xmlReader = null;
                StringReader strReader = null;
                String name = null;
                float scale2 = 1.0f;
                this.map.clear();
                String hbmlevelname = null;
                float hbmvalue3 = 10000.0f;
                OppoAppScaleHelper.this.reduceBrightnessPackageList.clear();
                String newname = null;
                this.newmap.clear();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    strReader = new StringReader(content);
                    parser.setInput(strReader);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType == 0) {
                            scale = scale2;
                            hbmvalue = hbmvalue3;
                        } else if (eventType != 2) {
                            scale = scale2;
                            hbmvalue = hbmvalue3;
                        } else {
                            try {
                                scale = scale2;
                                try {
                                    if (parser.getName().equals("NewPackageName")) {
                                        try {
                                            parser.next();
                                            newname = parser.getText();
                                            scale2 = scale;
                                        } catch (XmlPullParserException e2) {
                                            e = e2;
                                            Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                                            if (xmlReader != null) {
                                            }
                                            if (strReader == null) {
                                            }
                                        } catch (IOException e3) {
                                            e = e3;
                                            scale2 = scale;
                                            try {
                                                Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                                                if (xmlReader != null) {
                                                }
                                                if (strReader != null) {
                                                }
                                            } catch (Throwable th) {
                                                e = th;
                                                if (xmlReader != null) {
                                                }
                                                if (strReader != null) {
                                                }
                                                throw e;
                                            }
                                        } catch (Throwable th2) {
                                            e = th2;
                                            if (xmlReader != null) {
                                            }
                                            if (strReader != null) {
                                            }
                                            throw e;
                                        }
                                    } else {
                                        if (parser.getName().equals("Newscale")) {
                                            parser.next();
                                            float newscale = Float.parseFloat(parser.getText());
                                            if (newname != null) {
                                                this.newmap.put(newname, Float.valueOf(newscale));
                                            }
                                        } else {
                                            if (parser.getName().equals("PackageName")) {
                                                parser.next();
                                                name = parser.getText();
                                                scale2 = scale;
                                            } else if (parser.getName().equals("scale")) {
                                                parser.next();
                                                scale2 = Float.parseFloat(parser.getText());
                                                if (name != null) {
                                                    try {
                                                        hbmvalue2 = hbmvalue3;
                                                    } catch (XmlPullParserException e4) {
                                                        e = e4;
                                                        Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                                                        if (xmlReader != null) {
                                                        }
                                                        if (strReader == null) {
                                                        }
                                                    } catch (IOException e5) {
                                                        e = e5;
                                                        Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                                                        if (xmlReader != null) {
                                                        }
                                                        if (strReader != null) {
                                                        }
                                                    } catch (Throwable th3) {
                                                        e = th3;
                                                        if (xmlReader != null) {
                                                        }
                                                        if (strReader != null) {
                                                        }
                                                        throw e;
                                                    }
                                                    try {
                                                        this.map.put(name, Float.valueOf(scale2));
                                                    } catch (XmlPullParserException e6) {
                                                        e = e6;
                                                    } catch (IOException e7) {
                                                        e = e7;
                                                        hbmvalue3 = hbmvalue2;
                                                        Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                                                        if (xmlReader != null) {
                                                        }
                                                        if (strReader != null) {
                                                        }
                                                    } catch (Throwable th4) {
                                                        e = th4;
                                                        if (xmlReader != null) {
                                                        }
                                                        if (strReader != null) {
                                                        }
                                                        throw e;
                                                    }
                                                } else {
                                                    hbmvalue2 = hbmvalue3;
                                                }
                                                hbmvalue3 = hbmvalue2;
                                            } else {
                                                hbmvalue = hbmvalue3;
                                                try {
                                                    if (parser.getName().equals("hbmlevel")) {
                                                        parser.next();
                                                        hbmlevelname = parser.getText();
                                                        scale2 = scale;
                                                        hbmvalue3 = hbmvalue;
                                                    } else if (parser.getName().equals("hbmvalue")) {
                                                        parser.next();
                                                        hbmvalue3 = Float.parseFloat(parser.getText());
                                                        if (hbmlevelname != null) {
                                                            if (hbmlevelname.equals("hbmlevel1")) {
                                                                OppoAppScaleHelper.this.hbmLevel[0] = hbmvalue3;
                                                            } else if (hbmlevelname.equals("hbmlevel2")) {
                                                                OppoAppScaleHelper.this.hbmLevel[1] = hbmvalue3;
                                                            } else if (hbmlevelname.equals("hbmlevel3")) {
                                                                OppoAppScaleHelper.this.hbmLevel[2] = hbmvalue3;
                                                            }
                                                        }
                                                    } else if (parser.getName().equals("GloableHBMFeature")) {
                                                        parser.next();
                                                        boolean unused = OppoAppScaleHelper.this.gloableHBMOn = parser.getText().equals(TemperatureProvider.SWITCH_ON);
                                                        scale2 = scale;
                                                        hbmvalue3 = hbmvalue;
                                                    } else if (parser.getName().equals("ReduceBrightnessPackage")) {
                                                        parser.next();
                                                        OppoAppScaleHelper.this.reduceBrightnessPackageList.add(parser.getText());
                                                        scale2 = scale;
                                                        hbmvalue3 = hbmvalue;
                                                    } else if (parser.getName().equals("ReduceBrightnessRate")) {
                                                        parser.next();
                                                        float unused2 = OppoAppScaleHelper.this.mReduceBrightnessRate = Float.parseFloat(parser.getText());
                                                        scale2 = scale;
                                                        hbmvalue3 = hbmvalue;
                                                    } else if (parser.getName().equals("LowBrightnessThreshold")) {
                                                        parser.next();
                                                        int unused3 = OppoAppScaleHelper.this.mLowBrightnessThreshold = Integer.parseInt(parser.getText());
                                                        scale2 = scale;
                                                        hbmvalue3 = hbmvalue;
                                                    }
                                                } catch (XmlPullParserException e8) {
                                                    e = e8;
                                                    Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                                                    if (xmlReader != null) {
                                                    }
                                                    if (strReader == null) {
                                                    }
                                                } catch (IOException e9) {
                                                    e = e9;
                                                    scale2 = scale;
                                                    hbmvalue3 = hbmvalue;
                                                    Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                                                    if (xmlReader != null) {
                                                    }
                                                    if (strReader != null) {
                                                    }
                                                } catch (Throwable th5) {
                                                    e = th5;
                                                    if (xmlReader != null) {
                                                    }
                                                    if (strReader != null) {
                                                    }
                                                    throw e;
                                                }
                                            }
                                        }
                                        scale2 = scale;
                                    }
                                } catch (XmlPullParserException e10) {
                                    e = e10;
                                    Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                                    if (xmlReader != null) {
                                    }
                                    if (strReader == null) {
                                    }
                                } catch (IOException e11) {
                                    e = e11;
                                    scale2 = scale;
                                    Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                                    if (xmlReader != null) {
                                    }
                                    if (strReader != null) {
                                    }
                                } catch (Throwable th6) {
                                    e = th6;
                                    if (xmlReader != null) {
                                    }
                                    if (strReader != null) {
                                    }
                                    throw e;
                                }
                            } catch (XmlPullParserException e12) {
                                e = e12;
                                Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                                if (xmlReader != null) {
                                    try {
                                        xmlReader.close();
                                    } catch (IOException e13) {
                                        Log.e(OppoAppScaleHelper.TAG, "Got execption close permReader.", e13);
                                        return;
                                    }
                                }
                                if (strReader == null) {
                                    strReader.close();
                                    return;
                                }
                                return;
                            } catch (IOException e14) {
                                e = e14;
                                Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                                if (xmlReader != null) {
                                    try {
                                        xmlReader.close();
                                    } catch (IOException e15) {
                                        Log.e(OppoAppScaleHelper.TAG, "Got execption close permReader.", e15);
                                        return;
                                    }
                                }
                                if (strReader != null) {
                                    strReader.close();
                                    return;
                                }
                                return;
                            } catch (Throwable th7) {
                                e = th7;
                                if (xmlReader != null) {
                                    try {
                                        xmlReader.close();
                                    } catch (IOException e16) {
                                        Log.e(OppoAppScaleHelper.TAG, "Got execption close permReader.", e16);
                                        throw e;
                                    }
                                }
                                if (strReader != null) {
                                    strReader.close();
                                }
                                throw e;
                            }
                        }
                        scale2 = scale;
                        hbmvalue3 = hbmvalue;
                    }
                    startTimer();
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e17) {
                            Log.e(OppoAppScaleHelper.TAG, "Got execption close permReader.", e17);
                            return;
                        }
                    }
                    strReader.close();
                } catch (XmlPullParserException e18) {
                    e = e18;
                    Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                    if (xmlReader != null) {
                    }
                    if (strReader == null) {
                    }
                } catch (IOException e19) {
                    e = e19;
                    Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e);
                    if (xmlReader != null) {
                    }
                    if (strReader != null) {
                    }
                }
            }
        }

        public float GetNewScale(String packageName) {
            if (packageName == null || this.newmap.isEmpty() || !this.newmap.containsKey(packageName)) {
                return 1.0f;
            }
            float scale = this.newmap.get(packageName).floatValue();
            Log.i(OppoAppScaleHelper.TAG, "newscalepackageName:" + packageName + " newscalescale:" + scale);
            return scale;
        }

        public List<String> GetReduceBrightnessPackage() {
            return OppoAppScaleHelper.this.reduceBrightnessPackageList;
        }

        public float GetReduceBrightnessRate() {
            return OppoAppScaleHelper.this.mReduceBrightnessRate;
        }

        public float GetScale(String packageName) {
            if (packageName == null || this.map.isEmpty() || !this.map.containsKey(packageName)) {
                return 1.0f;
            }
            float scale = this.map.get(packageName).floatValue();
            Log.i(OppoAppScaleHelper.TAG, "packageName:" + packageName + " scale:" + scale);
            return scale;
        }

        /* access modifiers changed from: private */
        public void updateHBMVaule() {
        }

        public int getLowBrightnessThreshold() {
            return OppoAppScaleHelper.this.mLowBrightnessThreshold;
        }

        private void startTimer() {
            synchronized (this) {
                if (this.mHBMTimer == null) {
                    this.mHBMTimer = new Timer();
                }
                if (this.mHBMTask == null) {
                    this.mHBMTask = new TimerTask() {
                        /* class com.android.server.oppo.OppoAppScaleHelper.OppoAppScaleInfo.AnonymousClass1 */

                        public void run() {
                            OppoAppScaleInfo.this.updateHBMVaule();
                        }
                    };
                }
                if (!(this.mHBMTimer == null || this.mHBMTask == null)) {
                    this.mHBMTimer.schedule(this.mHBMTask, 240000);
                }
            }
        }
    }

    public float GetNewScale(String packageName) {
        return getUpdateInfo(true).GetNewScale(packageName);
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
        return getUpdateInfo(true).GetScale(packageName);
    }

    public List<String> GetReduceBrightnessPackage() {
        return getUpdateInfo(true).GetReduceBrightnessPackage();
    }

    public float GetReduceBrightnessRate() {
        return getUpdateInfo(true).GetReduceBrightnessRate();
    }

    public int getLowBrightnessThreshold() {
        return getUpdateInfo(true).getLowBrightnessThreshold();
    }
}
