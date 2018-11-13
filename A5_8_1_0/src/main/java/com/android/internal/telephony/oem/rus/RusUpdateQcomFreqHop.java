package com.android.internal.telephony.oem.rus;

import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;

public final class RusUpdateQcomFreqHop extends RusBase {
    private static final String TAG = "RusUpdateQcomFreqHop";
    private static final int mModemLogIndex_count = 13;
    private static final String mModemLogIndex_end = "/QcomFreqHop>";
    private static final String mModemLogIndex_start = "<QcomFreqHop";
    private static final String mModemLogInfoFileName = "qual_update_freqhop.xml";
    private static final String mSubPath = "/data/data/com.android.phone/";
    private String mModemlogContent = null;

    public RusUpdateQcomFreqHop() {
        setPath(mSubPath);
    }

    public void execute() {
        valiateAndLoadFreqHopConfig();
    }

    public void valiateAndLoadFreqHopConfig() {
        Throwable th;
        printLog(TAG, "valiateAndLoadFreqHopConfig");
        FileReader confreader = null;
        String tempStr = getContent();
        printLog(TAG, "the provider content is=" + tempStr);
        try {
            this.mModemlogContent = createNetworkRomupdateXmlFile(tempStr, mModemLogInfoFileName, mModemLogIndex_start, mModemLogIndex_end, 13);
            FileReader confreader2 = new FileReader(this.mFileUtils.saveToFile(this.mModemlogContent, this.mPath + mModemLogInfoFileName));
            try {
                printLog(TAG, "confparser==null");
                XmlPullParser confparser = Xml.newPullParser();
                confparser.setInput(confreader2);
                XmlUtils.beginDocument(confparser, "QcomFreqHop");
                XmlUtils.nextElement(confparser);
                while (confparser.getEventType() != 1) {
                    if ("switch".equals(confparser.getName())) {
                        boolean freqHopEnable = Integer.parseInt(confparser.getAttributeValue(null, "freq_hop_enable")) == 1;
                        if (this.mPhone != null) {
                            this.mPhone.updateFreqHopEnable(freqHopEnable);
                        } else {
                            printLog(TAG, "mPhone  is null!");
                        }
                        XmlUtils.nextElement(confparser);
                    } else {
                        printLog(TAG, "this QcomFreqHop  tag is not match");
                        if (confreader2 != null) {
                            try {
                                confreader2.close();
                            } catch (IOException e) {
                            }
                        }
                        return;
                    }
                }
                if (confreader2 != null) {
                    try {
                        confreader2.close();
                    } catch (IOException e2) {
                    }
                }
            } catch (FileNotFoundException e3) {
                confreader = confreader2;
            } catch (Exception e4) {
                confreader = confreader2;
            } catch (Throwable th2) {
                th = th2;
                confreader = confreader2;
            }
        } catch (FileNotFoundException e5) {
            try {
                printLog(TAG, "FileNotFoundException");
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e6) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e7) {
                    }
                }
                throw th;
            }
        } catch (Exception e8) {
            printLog(TAG, "getXmlFile Exception while parsing");
            if (confreader != null) {
                try {
                    confreader.close();
                } catch (IOException e9) {
                }
            }
        }
    }
}
