package com.android.server.oppo;

import android.content.Context;
import android.os.OppoBaseEnvironment;
import android.util.Log;
import android.util.Xml;
import com.android.server.display.OppoBrightUtils;
import com.android.server.display.color.DisplayTransformManager;
import com.oppo.OppoRomUpdateHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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
    private static final String SYS_FILE_PRODUCT_DIR = (OppoBaseEnvironment.getOppoProductDirectory().getAbsolutePath() + "/vendor/etc/oppo_app_scale_list.xml");
    private static final String SYS_FILE_VERSION_DIR = (OppoBaseEnvironment.getOppoVersionDirectory().getAbsolutePath() + "/vendor/etc/oppo_app_scale_list.xml");
    private static final String TAG = "MMAppScale";
    private static final String TAG_VERSION = "version";
    private int dynamicRate;
    private int exitRate;
    private boolean gloableHBMOn = false;
    private float[] hbmLevel = new float[3];
    private int mHghBrightnessThresholdLimit = OppoBrightUtils.TEN_BITS_MAXBRIGHTNESS;
    private int mLowBrightnessThreshold = 100;
    private int mLowBrightnessThresholdLimit = DisplayTransformManager.LEVEL_COLOR_MATRIX_SATURATION;
    private float mReduceBrightnessRate = 1.0f;
    private float mReduceBrightnessRateSnd = 1.0f;
    private List<String> reduceBrightnessPackageList = new ArrayList();
    private int slowRate;
    private float staticRate;
    private int temp1;
    private int temp2;
    private int temp3;
    private int temp4;
    private int temp5;
    private int useReduceBrightness = 0;
    private float y1;
    private float y2;
    private float y3;
    private float y4;
    private float y5;

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

    /* access modifiers changed from: private */
    public class OppoAppScaleInfo extends OppoRomUpdateHelper.UpdateInfo {
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

        /* JADX WARNING: Removed duplicated region for block: B:215:0x05d0 A[SYNTHETIC, Splitter:B:215:0x05d0] */
        /* JADX WARNING: Removed duplicated region for block: B:220:0x05d9 A[Catch:{ IOException -> 0x05d4 }] */
        /* JADX WARNING: Removed duplicated region for block: B:226:0x05ec A[SYNTHETIC, Splitter:B:226:0x05ec] */
        /* JADX WARNING: Removed duplicated region for block: B:231:0x05f5 A[Catch:{ IOException -> 0x05f0 }] */
        /* JADX WARNING: Removed duplicated region for block: B:234:0x0601 A[SYNTHETIC, Splitter:B:234:0x0601] */
        /* JADX WARNING: Removed duplicated region for block: B:239:0x060a A[Catch:{ IOException -> 0x0605 }] */
        /* JADX WARNING: Removed duplicated region for block: B:275:? A[RETURN, SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:278:? A[RETURN, SYNTHETIC] */
        public void parseContentFromXML(String content) {
            XmlPullParserException e;
            XmlPullParserException e2;
            IOException e3;
            float scale;
            int i;
            int eventType;
            float scale2;
            int eventType2;
            if (content != null) {
                OppoAppScaleHelper.this.changeFilePermisson("data/system/oppo_app_scale_list.xml");
                FileReader xmlReader = null;
                StringReader strReader = null;
                String name = null;
                float scale3 = 1.0f;
                this.map.clear();
                String hbmlevelname = null;
                float hbmvalue = 10000.0f;
                OppoAppScaleHelper.this.reduceBrightnessPackageList.clear();
                String reduceBrightnessPackageName = null;
                String reduceBrightnessPackageName2 = null;
                float newscale = 1.0f;
                this.newmap.clear();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    try {
                        strReader = new StringReader(content);
                        try {
                            parser.setInput(strReader);
                            String name2 = null;
                            float newscale2 = 1.0f;
                            String newname = null;
                            String reduceBrightnessPackageName3 = null;
                            float hbmvalue2 = 10000.0f;
                            String hbmlevelname2 = null;
                            float scale4 = 1.0f;
                            for (int eventType3 = parser.getEventType(); eventType3 != 1; eventType3 = parser.next()) {
                                if (eventType3 == 0) {
                                    i = eventType3;
                                    scale = scale4;
                                } else if (eventType3 != 2) {
                                    i = eventType3;
                                    scale = scale4;
                                } else {
                                    try {
                                        i = eventType3;
                                        if (parser.getName().equals("NewPackageName")) {
                                            try {
                                                parser.next();
                                                newname = parser.getText();
                                            } catch (XmlPullParserException e4) {
                                                e2 = e4;
                                                Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e2);
                                                if (0 != 0) {
                                                }
                                                if (strReader != null) {
                                                }
                                            } catch (IOException e5) {
                                                e3 = e5;
                                                name = name2;
                                                scale3 = scale4;
                                                hbmlevelname = hbmlevelname2;
                                                hbmvalue = hbmvalue2;
                                                reduceBrightnessPackageName = reduceBrightnessPackageName3;
                                                reduceBrightnessPackageName2 = newname;
                                                newscale = newscale2;
                                                try {
                                                    Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e3);
                                                    if (0 != 0) {
                                                    }
                                                    if (strReader != null) {
                                                    }
                                                } catch (Throwable th) {
                                                    e = th;
                                                    if (0 != 0) {
                                                    }
                                                    if (strReader != null) {
                                                    }
                                                    throw e;
                                                }
                                            } catch (Throwable th2) {
                                                e = th2;
                                                if (0 != 0) {
                                                }
                                                if (strReader != null) {
                                                }
                                                throw e;
                                            }
                                        } else if (parser.getName().equals("Newscale")) {
                                            int eventType4 = parser.next();
                                            float newscale3 = Float.parseFloat(parser.getText());
                                            if (newname != null) {
                                                eventType2 = eventType4;
                                                try {
                                                    scale2 = scale4;
                                                    try {
                                                        this.newmap.put(newname, Float.valueOf(newscale3));
                                                    } catch (XmlPullParserException e6) {
                                                        e2 = e6;
                                                    } catch (IOException e7) {
                                                        name = name2;
                                                        hbmlevelname = hbmlevelname2;
                                                        hbmvalue = hbmvalue2;
                                                        reduceBrightnessPackageName = reduceBrightnessPackageName3;
                                                        reduceBrightnessPackageName2 = newname;
                                                        scale3 = scale2;
                                                        newscale = newscale3;
                                                        e3 = e7;
                                                        Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e3);
                                                        if (0 != 0) {
                                                        }
                                                        if (strReader != null) {
                                                        }
                                                    } catch (Throwable th3) {
                                                        e = th3;
                                                        if (0 != 0) {
                                                        }
                                                        if (strReader != null) {
                                                        }
                                                        throw e;
                                                    }
                                                } catch (XmlPullParserException e8) {
                                                    e2 = e8;
                                                    Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e2);
                                                    if (0 != 0) {
                                                    }
                                                    if (strReader != null) {
                                                    }
                                                } catch (IOException e9) {
                                                    name = name2;
                                                    hbmlevelname = hbmlevelname2;
                                                    hbmvalue = hbmvalue2;
                                                    reduceBrightnessPackageName = reduceBrightnessPackageName3;
                                                    reduceBrightnessPackageName2 = newname;
                                                    scale3 = scale4;
                                                    newscale = newscale3;
                                                    e3 = e9;
                                                    Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e3);
                                                    if (0 != 0) {
                                                    }
                                                    if (strReader != null) {
                                                    }
                                                } catch (Throwable th4) {
                                                    e = th4;
                                                    if (0 != 0) {
                                                    }
                                                    if (strReader != null) {
                                                    }
                                                    throw e;
                                                }
                                            } else {
                                                eventType2 = eventType4;
                                                scale2 = scale4;
                                            }
                                            newscale2 = newscale3;
                                            scale4 = scale2;
                                        } else {
                                            scale = scale4;
                                            try {
                                                if (parser.getName().equals("PackageName")) {
                                                    parser.next();
                                                    name2 = parser.getText();
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("scale")) {
                                                    int eventType5 = parser.next();
                                                    float scale5 = Float.parseFloat(parser.getText());
                                                    if (name2 != null) {
                                                        try {
                                                            eventType = eventType5;
                                                            this.map.put(name2, Float.valueOf(scale5));
                                                        } catch (XmlPullParserException e10) {
                                                            e2 = e10;
                                                        } catch (IOException e11) {
                                                            name = name2;
                                                            hbmlevelname = hbmlevelname2;
                                                            hbmvalue = hbmvalue2;
                                                            reduceBrightnessPackageName = reduceBrightnessPackageName3;
                                                            reduceBrightnessPackageName2 = newname;
                                                            newscale = newscale2;
                                                            scale3 = scale5;
                                                            e3 = e11;
                                                            Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e3);
                                                            if (0 != 0) {
                                                            }
                                                            if (strReader != null) {
                                                            }
                                                        } catch (Throwable th5) {
                                                            e = th5;
                                                            if (0 != 0) {
                                                            }
                                                            if (strReader != null) {
                                                            }
                                                            throw e;
                                                        }
                                                    } else {
                                                        eventType = eventType5;
                                                    }
                                                    scale4 = scale5;
                                                } else if (parser.getName().equals("hbmlevel")) {
                                                    parser.next();
                                                    hbmlevelname2 = parser.getText();
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("hbmvalue")) {
                                                    parser.next();
                                                    float hbmvalue3 = Float.parseFloat(parser.getText());
                                                    if (hbmlevelname2 != null) {
                                                        try {
                                                            if (hbmlevelname2.equals("hbmlevel1")) {
                                                                OppoAppScaleHelper.this.hbmLevel[0] = hbmvalue3;
                                                            } else if (hbmlevelname2.equals("hbmlevel2")) {
                                                                OppoAppScaleHelper.this.hbmLevel[1] = hbmvalue3;
                                                            } else if (hbmlevelname2.equals("hbmlevel3")) {
                                                                OppoAppScaleHelper.this.hbmLevel[2] = hbmvalue3;
                                                            }
                                                        } catch (XmlPullParserException e12) {
                                                            e2 = e12;
                                                            Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e2);
                                                            if (0 != 0) {
                                                            }
                                                            if (strReader != null) {
                                                            }
                                                        } catch (IOException e13) {
                                                            name = name2;
                                                            hbmlevelname = hbmlevelname2;
                                                            reduceBrightnessPackageName = reduceBrightnessPackageName3;
                                                            reduceBrightnessPackageName2 = newname;
                                                            newscale = newscale2;
                                                            scale3 = scale;
                                                            hbmvalue = hbmvalue3;
                                                            e3 = e13;
                                                            Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e3);
                                                            if (0 != 0) {
                                                            }
                                                            if (strReader != null) {
                                                            }
                                                        } catch (Throwable th6) {
                                                            e = th6;
                                                            if (0 != 0) {
                                                            }
                                                            if (strReader != null) {
                                                            }
                                                            throw e;
                                                        }
                                                    }
                                                    hbmvalue2 = hbmvalue3;
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("GloableHBMFeature")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.gloableHBMOn = parser.getText().equals(TemperatureProvider.SWITCH_ON);
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnessPackage")) {
                                                    parser.next();
                                                    String reduceBrightnessPackageName4 = parser.getText();
                                                    try {
                                                        OppoAppScaleHelper.this.reduceBrightnessPackageList.add(reduceBrightnessPackageName4);
                                                        reduceBrightnessPackageName3 = reduceBrightnessPackageName4;
                                                        scale4 = scale;
                                                    } catch (XmlPullParserException e14) {
                                                        e2 = e14;
                                                        Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e2);
                                                        if (0 != 0) {
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
                                                    } catch (IOException e16) {
                                                        name = name2;
                                                        hbmlevelname = hbmlevelname2;
                                                        hbmvalue = hbmvalue2;
                                                        reduceBrightnessPackageName2 = newname;
                                                        newscale = newscale2;
                                                        scale3 = scale;
                                                        reduceBrightnessPackageName = reduceBrightnessPackageName4;
                                                        e3 = e16;
                                                        Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e3);
                                                        if (0 != 0) {
                                                            try {
                                                                xmlReader.close();
                                                            } catch (IOException e17) {
                                                                Log.e(OppoAppScaleHelper.TAG, "Got execption close permReader.", e17);
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
                                                        if (0 != 0) {
                                                            try {
                                                                xmlReader.close();
                                                            } catch (IOException e18) {
                                                                Log.e(OppoAppScaleHelper.TAG, "Got execption close permReader.", e18);
                                                                throw e;
                                                            }
                                                        }
                                                        if (strReader != null) {
                                                            strReader.close();
                                                        }
                                                        throw e;
                                                    }
                                                } else if (parser.getName().equals("ReduceBrightnessRate")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.mReduceBrightnessRate = Float.parseFloat(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnessuse")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.useReduceBrightness = Integer.parseInt(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnessRateSnd")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.mReduceBrightnessRateSnd = Float.parseFloat(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnesstemp1")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.temp1 = Integer.parseInt(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnesstemp2")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.temp2 = Integer.parseInt(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnesstemp3")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.temp3 = Integer.parseInt(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnesstemp4")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.temp4 = Integer.parseInt(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnesstemp5")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.temp5 = Integer.parseInt(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnessexitRate")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.exitRate = Integer.parseInt(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnessstaticRate")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.staticRate = Float.parseFloat(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnessdynamicRate")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.dynamicRate = Integer.parseInt(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnessslowRate")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.slowRate = Integer.parseInt(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnessy1")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.y1 = Float.parseFloat(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnessy2")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.y2 = Float.parseFloat(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnessy3")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.y3 = Float.parseFloat(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnessy4")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.y4 = Float.parseFloat(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("ReduceBrightnessy5")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.y5 = Float.parseFloat(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("LowBrightnessThresholdLimit")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.mLowBrightnessThresholdLimit = Integer.parseInt(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("HghBrightnessThresholdLimit")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.mHghBrightnessThresholdLimit = Integer.parseInt(parser.getText());
                                                    scale4 = scale;
                                                } else if (parser.getName().equals("LowBrightnessThreshold")) {
                                                    parser.next();
                                                    OppoAppScaleHelper.this.mLowBrightnessThreshold = Integer.parseInt(parser.getText());
                                                    scale4 = scale;
                                                }
                                            } catch (XmlPullParserException e19) {
                                                e2 = e19;
                                                Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e2);
                                                if (0 != 0) {
                                                }
                                                if (strReader != null) {
                                                }
                                            } catch (IOException e20) {
                                                e3 = e20;
                                                name = name2;
                                                hbmlevelname = hbmlevelname2;
                                                hbmvalue = hbmvalue2;
                                                reduceBrightnessPackageName = reduceBrightnessPackageName3;
                                                reduceBrightnessPackageName2 = newname;
                                                newscale = newscale2;
                                                scale3 = scale;
                                                Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e3);
                                                if (0 != 0) {
                                                }
                                                if (strReader != null) {
                                                }
                                            } catch (Throwable th8) {
                                                e = th8;
                                                if (0 != 0) {
                                                }
                                                if (strReader != null) {
                                                }
                                                throw e;
                                            }
                                        }
                                    } catch (XmlPullParserException e21) {
                                        e2 = e21;
                                        Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e2);
                                        if (0 != 0) {
                                        }
                                        if (strReader != null) {
                                        }
                                    } catch (IOException e22) {
                                        e3 = e22;
                                        name = name2;
                                        hbmlevelname = hbmlevelname2;
                                        hbmvalue = hbmvalue2;
                                        reduceBrightnessPackageName = reduceBrightnessPackageName3;
                                        reduceBrightnessPackageName2 = newname;
                                        newscale = newscale2;
                                        scale3 = scale4;
                                        Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e3);
                                        if (0 != 0) {
                                        }
                                        if (strReader != null) {
                                        }
                                    } catch (Throwable th9) {
                                        e = th9;
                                        if (0 != 0) {
                                        }
                                        if (strReader != null) {
                                        }
                                        throw e;
                                    }
                                }
                                scale4 = scale;
                            }
                            startTimer();
                            if (0 != 0) {
                                try {
                                    xmlReader.close();
                                } catch (IOException e23) {
                                    Log.e(OppoAppScaleHelper.TAG, "Got execption close permReader.", e23);
                                    return;
                                }
                            }
                            strReader.close();
                        } catch (XmlPullParserException e24) {
                            e2 = e24;
                            Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e2);
                            if (0 != 0) {
                            }
                            if (strReader != null) {
                            }
                        } catch (IOException e25) {
                            e3 = e25;
                            Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e3);
                            if (0 != 0) {
                            }
                            if (strReader != null) {
                            }
                        }
                    } catch (XmlPullParserException e26) {
                        e2 = e26;
                        strReader = null;
                        Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e2);
                        if (0 != 0) {
                        }
                        if (strReader != null) {
                        }
                    } catch (IOException e27) {
                        e3 = e27;
                        strReader = null;
                        Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e3);
                        if (0 != 0) {
                        }
                        if (strReader != null) {
                        }
                    } catch (Throwable th10) {
                        e = th10;
                        strReader = null;
                        if (0 != 0) {
                        }
                        if (strReader != null) {
                        }
                        throw e;
                    }
                } catch (XmlPullParserException e28) {
                    e2 = e28;
                    Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e2);
                    if (0 != 0) {
                    }
                    if (strReader != null) {
                    }
                } catch (IOException e29) {
                    e3 = e29;
                    Log.e(OppoAppScaleHelper.TAG, "Got execption parsing permissions.", e3);
                    if (0 != 0) {
                    }
                    if (strReader != null) {
                    }
                } catch (Throwable th11) {
                    e = th11;
                    if (0 != 0) {
                    }
                    if (strReader != null) {
                    }
                    throw e;
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

        public int GetuseReduceBrightness() {
            return OppoAppScaleHelper.this.useReduceBrightness;
        }

        public int Gettemp1() {
            return OppoAppScaleHelper.this.temp1;
        }

        public int Gettemp2() {
            return OppoAppScaleHelper.this.temp2;
        }

        public int Gettemp3() {
            return OppoAppScaleHelper.this.temp3;
        }

        public int Gettemp4() {
            return OppoAppScaleHelper.this.temp4;
        }

        public int Gettemp5() {
            return OppoAppScaleHelper.this.temp5;
        }

        public int GetexitRate() {
            return OppoAppScaleHelper.this.exitRate;
        }

        public float GetstaticRate() {
            return OppoAppScaleHelper.this.staticRate;
        }

        public int GetslowRate() {
            return OppoAppScaleHelper.this.slowRate;
        }

        public int GetdynamicRate() {
            return OppoAppScaleHelper.this.dynamicRate;
        }

        public float Gety1() {
            return OppoAppScaleHelper.this.y1;
        }

        public float Gety2() {
            return OppoAppScaleHelper.this.y2;
        }

        public float Gety3() {
            return OppoAppScaleHelper.this.y3;
        }

        public float Gety4() {
            return OppoAppScaleHelper.this.y4;
        }

        public float Gety5() {
            return OppoAppScaleHelper.this.y5;
        }

        public float GetReduceBrightnessRateSnd() {
            return OppoAppScaleHelper.this.mReduceBrightnessRateSnd;
        }

        public int GetReduceBrightnessLowLimit() {
            return OppoAppScaleHelper.this.mLowBrightnessThresholdLimit;
        }

        public int GetReduceBrightnessHghLimit() {
            return OppoAppScaleHelper.this.mHghBrightnessThresholdLimit;
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
        /* access modifiers changed from: public */
        private void updateHBMVaule() {
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
        super(context, "oppo_app_scale_list", SYS_FILE_PRODUCT_DIR, "data/system/oppo_app_scale_list.xml");
        setUpdateInfo(new OppoAppScaleInfo(), new OppoAppScaleInfo());
        try {
            init();
            changeFilePermisson("data/system/oppo_app_scale_list.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        long sysversion;
        if (SYS_FILE_PRODUCT_DIR != null) {
            boolean bUseVersionDir = false;
            File datafile = new File("data/system/oppo_app_scale_list.xml");
            File sysfileProduct = new File(SYS_FILE_PRODUCT_DIR);
            File sysfileVersion = new File(SYS_FILE_VERSION_DIR);
            if (sysfileVersion.exists()) {
                bUseVersionDir = true;
            }
            if (!datafile.exists()) {
                Log.d(TAG, "apppp_scale datafile not exist try to load from system");
                if (bUseVersionDir) {
                    Log.d(TAG, "apppp_scale read version file");
                    parseContentFromXML(readFromFile(sysfileVersion));
                    return;
                }
                Log.d(TAG, "apppp_scale read product file");
                parseContentFromXML(readFromFile(sysfileProduct));
                return;
            }
            long dataversion = getConfigVersion("data/system/oppo_app_scale_list.xml", true);
            if (bUseVersionDir) {
                sysversion = getConfigVersion(SYS_FILE_VERSION_DIR, true);
            } else {
                sysversion = getConfigVersion(SYS_FILE_PRODUCT_DIR, true);
            }
            Log.d(TAG, "app_scale dataversion:" + dataversion + " sysversion:" + sysversion + ",bUseVersionDir=" + bUseVersionDir);
            if (dataversion >= sysversion) {
                parseContentFromXML(readFromFile(datafile));
            } else if (bUseVersionDir) {
                parseContentFromXML(readFromFile(sysfileVersion));
            } else {
                parseContentFromXML(readFromFile(sysfileProduct));
            }
        }
    }

    private long getConfigVersion(String content, boolean isPath) {
        Reader xmlReader;
        if (content == null) {
            return 0;
        }
        Log.d(TAG, "getConfigVersion content length:" + content.length() + "," + isPath);
        long version = 0;
        if (isPath) {
            try {
                xmlReader = new FileReader(content);
            } catch (FileNotFoundException e) {
                return 0;
            }
        } else {
            xmlReader = new StringReader(content);
        }
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(xmlReader);
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String tagName = parser.getName();
                        Log.d(TAG, "getConfigVersion called  tagname:" + tagName);
                        if ("version".equals(tagName)) {
                            parser.next();
                            String text = parser.getText();
                            if (text.length() > 8) {
                                text = text.substring(0, 8);
                            }
                            try {
                                version = Long.parseLong(text);
                            } catch (NumberFormatException e2) {
                                Log.e(TAG, "version convert fail");
                            }
                            try {
                                xmlReader.close();
                            } catch (IOException e3) {
                                Log.e(TAG, "" + e3);
                            }
                            return version;
                        }
                    }
                }
            }
            try {
                xmlReader.close();
            } catch (IOException e4) {
                Log.e(TAG, "" + e4);
            }
            return 0;
        } catch (XmlPullParserException e5) {
            Log.e(TAG, "" + e5);
            try {
                xmlReader.close();
            } catch (IOException e6) {
                Log.e(TAG, "" + e6);
            }
            return 0;
        } catch (Exception e7) {
            Log.e(TAG, "" + e7);
            try {
                xmlReader.close();
            } catch (IOException e8) {
                Log.e(TAG, "" + e8);
            }
            return 0;
        } catch (Throwable th) {
            try {
                xmlReader.close();
            } catch (IOException e9) {
                Log.e(TAG, "" + e9);
            }
            throw th;
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

    public int GetuseReduceBrightness() {
        return getUpdateInfo(true).GetuseReduceBrightness();
    }

    public float Gety1() {
        return getUpdateInfo(true).Gety1();
    }

    public float Gety2() {
        return getUpdateInfo(true).Gety2();
    }

    public float Gety3() {
        return getUpdateInfo(true).Gety3();
    }

    public float Gety4() {
        return getUpdateInfo(true).Gety4();
    }

    public float Gety5() {
        return getUpdateInfo(true).Gety5();
    }

    public int Gettemp1() {
        return getUpdateInfo(true).Gettemp1();
    }

    public int Gettemp2() {
        return getUpdateInfo(true).Gettemp2();
    }

    public int Gettemp3() {
        return getUpdateInfo(true).Gettemp3();
    }

    public int Gettemp4() {
        return getUpdateInfo(true).Gettemp4();
    }

    public int Gettemp5() {
        return getUpdateInfo(true).Gettemp5();
    }

    public int GetexitRate() {
        return getUpdateInfo(true).GetexitRate();
    }

    public float GetstaticRate() {
        return getUpdateInfo(true).GetstaticRate();
    }

    public int GetslowRate() {
        return getUpdateInfo(true).GetslowRate();
    }

    public int GetdynamicRate() {
        return getUpdateInfo(true).GetdynamicRate();
    }

    public float GetReduceBrightnessRateSnd() {
        return getUpdateInfo(true).GetReduceBrightnessRateSnd();
    }

    public int GetReduceBrightnessLowLimit() {
        return getUpdateInfo(true).GetReduceBrightnessLowLimit();
    }

    public int GetReduceBrightnessHghLimit() {
        return getUpdateInfo(true).GetReduceBrightnessHghLimit();
    }

    public int getLowBrightnessThreshold() {
        return getUpdateInfo(true).getLowBrightnessThreshold();
    }
}
