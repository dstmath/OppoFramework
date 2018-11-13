package com.android.internal.telephony.oem.rus;

import android.os.Message;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;

public final class RusUpdateQcomFr2To3 extends RusBase {
    private static final String TAG = "RusUpdateQcomFr2To3";
    private static final String mSubPath = "/data/data/com.android.phone/";
    private static final int mVolteFr2Index_count = 11;
    private static final String mVolteFr2Index_end = "/QcomFr2_3>";
    private static final String mVolteFr2Index_start = "<QcomFr2_3";
    private static final String mVolteFr2InfoFileName = "qual_update_voltefr2.xml";
    public final int VOLTE_FR2_LENGTH = 4;
    private String mVolteFr2Content = null;
    int[] mVolteFr2Data = new int[4];

    public RusUpdateQcomFr2To3() {
        setPath(mSubPath);
    }

    public void execute() {
        sendEmptyMessage(valiateAndUpateVolteFr2());
    }

    public void onSucceed(Message msg) {
        int[] intarray = msg.obj;
        int fr2Flags = intarray[0];
        int fr2Thresh = intarray[1];
        int fr2Rsrp = intarray[2];
        int fr2Adaj = intarray[3];
        printLog(TAG, "fr2Flags:" + fr2Flags + "fr2Thresh" + fr2Thresh + "fr2Rsrp" + fr2Rsrp + "fr2Adaj" + fr2Adaj);
        try {
            this.mPhone.oppoUpdateVolteFr2(fr2Flags, fr2Thresh, fr2Rsrp, fr2Adaj, null);
        } catch (Exception e) {
            printLog(TAG, "hanlder doNVwrite wrong");
            e.printStackTrace();
        }
    }

    public int valiateAndUpateVolteFr2() {
        Throwable th;
        printLog(TAG, "getVolteFr2Xml");
        FileReader confreader = null;
        try {
            this.mVolteFr2Content = createNetworkRomupdateXmlFile(getContent(), mVolteFr2InfoFileName, mVolteFr2Index_start, mVolteFr2Index_end, 11);
            FileReader confreader2 = new FileReader(this.mFileUtils.saveToFile(this.mVolteFr2Content, this.mPath + mVolteFr2InfoFileName));
            try {
                XmlPullParser confparser = Xml.newPullParser();
                if (confparser != null) {
                    confparser.setInput(confreader2);
                    XmlUtils.beginDocument(confparser, "QcomFr2_3");
                    XmlUtils.nextElement(confparser);
                    while (confparser.getEventType() != 1) {
                        if ("fr2_values".equals(confparser.getName())) {
                            this.mVolteFr2Data[0] = Integer.parseInt(confparser.getAttributeValue(null, "fr2_flags"));
                            this.mVolteFr2Data[1] = Integer.parseInt(confparser.getAttributeValue(null, "fr2_thresh"));
                            this.mVolteFr2Data[2] = Integer.parseInt(confparser.getAttributeValue(null, "fr2_rsrp"));
                            this.mVolteFr2Data[3] = Integer.parseInt(confparser.getAttributeValue(null, "fr2_adaj"));
                            printLog(TAG, " the volte fr2 flags is :" + this.mVolteFr2Data[0] + "fr2_thresh is:" + this.mVolteFr2Data[1] + "fr2_rsrp is:" + this.mVolteFr2Data[2] + "fr2_adaj is:" + this.mVolteFr2Data[3]);
                            XmlUtils.nextElement(confparser);
                        } else {
                            printLog(TAG, "this fr2_values tag is not match");
                            if (confreader2 != null) {
                                try {
                                    confreader2.close();
                                } catch (IOException e) {
                                }
                            }
                            return 20;
                        }
                    }
                }
                printLog(TAG, "confparser==null");
                setVolteFr2(this.mVolteFr2Data);
                if (confreader2 != null) {
                    try {
                        confreader2.close();
                    } catch (IOException e2) {
                    }
                }
                return 21;
            } catch (FileNotFoundException e3) {
                confreader = confreader2;
            } catch (Exception e4) {
                confreader = confreader2;
            } catch (Throwable th2) {
                th = th2;
                confreader = confreader2;
            }
        } catch (FileNotFoundException e5) {
            if (confreader != null) {
                try {
                    confreader.close();
                } catch (IOException e6) {
                }
            }
            return 20;
        } catch (Exception e7) {
            try {
                printLog(TAG, "getXmlFile Exception while parsing");
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e8) {
                    }
                }
                return 20;
            } catch (Throwable th3) {
                th = th3;
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e9) {
                    }
                }
                throw th;
            }
        }
    }

    private void setVolteFr2(int[] voltefr2Data) {
        Message msg = obtainSucceedMsg();
        msg.obj = voltefr2Data;
        sendMessage(msg);
    }
}
