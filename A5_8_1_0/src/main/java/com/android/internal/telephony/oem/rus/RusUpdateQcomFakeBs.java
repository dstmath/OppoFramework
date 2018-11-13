package com.android.internal.telephony.oem.rus;

import android.os.Message;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;

public final class RusUpdateQcomFakeBs extends RusBase {
    private static final String TAG = "RusUpdateQcomFakeBs";
    private static final int mFakebsIndex_count = 12;
    private static final String mFakebsIndex_end = "/QcomFakeBs>";
    private static final String mFakebsIndex_start = "<QcomFakeBs";
    private static final String mFakebsInfoFileName = "qual_update_fakebs.xml";
    private static final String mSubPath = "/data/data/com.android.phone/";
    public final int FAKEBS_WEIGHT_LENGTH = 10;
    private String mFakebsContent = null;
    int[] mFakebsWeightData = new int[10];

    public RusUpdateQcomFakeBs() {
        setPath(mSubPath);
    }

    public void execute() {
        sendEmptyMessage(valiateAndUpateFakebsWeight());
    }

    public void onSucceed(Message msg) {
        try {
            this.mPhone.oppoUpdateFakeBsWeight(msg.obj, null);
        } catch (Exception e) {
            printLog(TAG, "hanlder doNVwrite wrong");
            e.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x00fa A:{SYNTHETIC, Splitter: B:27:0x00fa} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0122 A:{SYNTHETIC, Splitter: B:43:0x0122} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x012d A:{SYNTHETIC, Splitter: B:50:0x012d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int valiateAndUpateFakebsWeight() {
        Throwable th;
        printLog(TAG, "getFakebsWeightXml");
        FileReader confreader = null;
        String tempStr = getContent();
        printLog(TAG, "the provider content is=" + tempStr);
        try {
            this.mFakebsContent = createNetworkRomupdateXmlFile(tempStr, mFakebsInfoFileName, mFakebsIndex_start, mFakebsIndex_end, 12);
            FileReader confreader2 = new FileReader(this.mFileUtils.saveToFile(this.mFakebsContent, this.mPath + mFakebsInfoFileName));
            try {
                XmlPullParser confparser = Xml.newPullParser();
                if (confparser != null) {
                    confparser.setInput(confreader2);
                    XmlUtils.beginDocument(confparser, "QcomFakeBs");
                    XmlUtils.nextElement(confparser);
                    while (confparser.getEventType() != 1) {
                        if ("weight".equals(confparser.getName())) {
                            for (int i = 0; i < 10; i++) {
                                this.mFakebsWeightData[i] = Integer.parseInt(confparser.getAttributeValue(null, "weight_" + String.valueOf(i)));
                                printLog(TAG, " The mFakebsWeightData[" + String.valueOf(i) + "] is=" + this.mFakebsWeightData[i]);
                            }
                            XmlUtils.nextElement(confparser);
                        } else {
                            printLog(TAG, "this fakebs weight tag is not match");
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
                setFakebsWeight(this.mFakebsWeightData);
                if (confreader2 != null) {
                    try {
                        confreader2.close();
                    } catch (IOException e2) {
                    }
                }
                return 21;
            } catch (FileNotFoundException e3) {
                confreader = confreader2;
                if (confreader != null) {
                }
                return 20;
            } catch (Exception e4) {
                confreader = confreader2;
                try {
                    printLog(TAG, "getXmlFile Exception while parsing");
                    if (confreader != null) {
                    }
                    return 20;
                } catch (Throwable th2) {
                    th = th2;
                    if (confreader != null) {
                        try {
                            confreader.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                confreader = confreader2;
                if (confreader != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            if (confreader != null) {
                try {
                    confreader.close();
                } catch (IOException e7) {
                }
            }
            return 20;
        } catch (Exception e8) {
            printLog(TAG, "getXmlFile Exception while parsing");
            if (confreader != null) {
                try {
                    confreader.close();
                } catch (IOException e9) {
                }
            }
            return 20;
        }
    }

    private void setFakebsWeight(int[] fakebsweightData) {
        Message msg = obtainSucceedMsg();
        msg.obj = fakebsweightData;
        sendMessage(msg);
    }
}
