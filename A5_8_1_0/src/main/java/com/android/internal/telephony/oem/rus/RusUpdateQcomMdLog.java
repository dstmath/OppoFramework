package com.android.internal.telephony.oem.rus;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import com.android.internal.telephony.OppoModemLogManager;
import com.android.internal.util.XmlUtils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;

public final class RusUpdateQcomMdLog extends RusBase {
    private static final String TAG = "RusUpdateQcomMdLog";
    private static final int mModemLogIndex_count = 11;
    private static final String mModemLogIndex_end = "/QcomMdLog>";
    private static final String mModemLogIndex_start = "<QcomMdLog";
    private static final String mModemLogInfoFileName = "qual_update_mdlog.xml";
    private static final String mSubPath = "/data/data/com.android.phone/";
    private String mModemlogContent = null;

    public RusUpdateQcomMdLog() {
        setPath(mSubPath);
    }

    public void execute() {
        valiateAndLoadMdLogConfig();
    }

    public void valiateAndLoadMdLogConfig() {
        Throwable th;
        printLog(TAG, "valiateAndLoadMdLogConfig");
        FileReader confreader = null;
        String tempStr = getContent();
        printLog(TAG, "the provider content is=" + tempStr);
        try {
            this.mModemlogContent = createNetworkRomupdateXmlFile(tempStr, mModemLogInfoFileName, mModemLogIndex_start, mModemLogIndex_end, 11);
            FileReader confreader2 = new FileReader(this.mFileUtils.saveToFile(this.mModemlogContent, this.mPath + mModemLogInfoFileName));
            try {
                printLog(TAG, "confparser==null");
                XmlPullParser confparser = Xml.newPullParser();
                confparser.setInput(confreader2);
                XmlUtils.beginDocument(confparser, "QcomMdLog");
                XmlUtils.nextElement(confparser);
                while (confparser.getEventType() != 1) {
                    if ("switch".equals(confparser.getName())) {
                        int mdlogpb = Integer.parseInt(confparser.getAttributeValue(null, "mdlog_pb"));
                        Log.d(TAG, "mdlogpb:" + mdlogpb);
                        Context context = this.mPhone.getContext();
                        if (context != null) {
                            OppoModemLogManager.updateMdlogType(context, mdlogpb);
                        } else {
                            printLog(TAG, "context  is null!");
                        }
                        int dumpPb = Integer.parseInt(confparser.getAttributeValue(null, "dump_pb"));
                        OppoModemLogManager.enableModemDumpPostBack(dumpPb == 1);
                        printLog(TAG, "dumpPb:" + dumpPb);
                        XmlUtils.nextElement(confparser);
                    } else {
                        printLog(TAG, "this MdLog  tag is not match");
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
