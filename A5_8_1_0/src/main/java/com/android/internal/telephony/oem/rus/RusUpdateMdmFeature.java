package com.android.internal.telephony.oem.rus;

import android.os.Message;
import android.util.Xml;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.util.XmlUtils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;

public final class RusUpdateMdmFeature extends RusBase {
    private static final String TAG = "RusUpdateMdmFeature";
    private static final int mMdmFeatureIndex_count = 12;
    private static final String mMdmFeatureIndex_end = "/MdmFeature>";
    private static final String mMdmFeatureIndex_start = "<MdmFeature";
    private static final String mMdmFeatureInfoFileName = "qual_update_mdmfeature.xml";
    private static final String mSubPath = "/data/data/com.android.phone/";
    private String mMdmFeatureContent = null;
    private String mValueStr = null;

    public RusUpdateMdmFeature() {
        setPath(mSubPath);
    }

    public void execute() {
        sendEmptyMessage(valiateAndUpateMdmFeature());
    }

    public void onSucceed(Message msg) {
        int[] intarray = msg.obj;
        int paramone = intarray[0];
        int paramtwo = intarray[1];
        int paramthree = intarray[2];
        printLog(TAG, "paramone:" + paramone + "paramtwo" + paramtwo + "paramthree" + paramthree + "paramfour" + intarray[3]);
        try {
            this.mPhone.oppoCtlModemFeature(intarray, null);
        } catch (Exception e) {
            printLog(TAG, "hanlder doNVwrite wrong");
            e.printStackTrace();
        }
    }

    public int valiateAndUpateMdmFeature() {
        Throwable th;
        printLog(TAG, "MdmFeature");
        FileReader confreader = null;
        try {
            this.mMdmFeatureContent = createNetworkRomupdateXmlFile(getContent(), mMdmFeatureInfoFileName, mMdmFeatureIndex_start, mMdmFeatureIndex_end, 12);
            FileReader confreader2 = new FileReader(this.mFileUtils.saveToFile(this.mMdmFeatureContent, this.mPath + mMdmFeatureInfoFileName));
            try {
                XmlPullParser confparser = Xml.newPullParser();
                if (confparser != null) {
                    confparser.setInput(confreader2);
                    XmlUtils.beginDocument(confparser, "MdmFeature");
                    XmlUtils.nextElement(confparser);
                    while (confparser.getEventType() != 1) {
                        if ("feature".equals(confparser.getName())) {
                            this.mValueStr = confparser.getAttributeValue(null, "feature_value");
                            printLog(TAG, " the mValueStr is :" + this.mValueStr);
                            XmlUtils.nextElement(confparser);
                        } else {
                            printLog(TAG, "this feature tag is not match");
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
                setVolteFr2(evaluate(this.mValueStr));
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

    private int[] evaluate(String startInt) {
        int[] mdmFeatureData = new int[4];
        int start = parseUnsignedInt(startInt.replace("0x", SpnOverride.MVNO_TYPE_NONE));
        int startA = (start >> 24) & 255;
        int startR = (start >> 16) & 255;
        int startG = (start >> 8) & 255;
        int startB = start & 255;
        printLog(TAG, " the mValueStr is :" + startA + "startR" + startR + "startG" + startG + "startB" + startB);
        mdmFeatureData[0] = startA;
        mdmFeatureData[1] = startR;
        mdmFeatureData[2] = startG;
        mdmFeatureData[3] = startB;
        return mdmFeatureData;
    }

    private int parseUnsignedInt(String in) throws NumberFormatException {
        long t = Long.parseLong(in, 16);
        if ((-4294967296L & t) == 0) {
            return (int) (4294967295L & t);
        }
        throw new NumberFormatException();
    }
}
