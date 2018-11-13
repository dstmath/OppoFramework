package com.android.internal.telephony.oem.rus;

import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;

public final class RusUpdateJioSecodary extends RusBase {
    private static final String TAG = "RusUpdateJioSecodary";
    private static final int mJioSecodaryIndex_count = 13;
    private static final String mJioSecodaryIndex_end = "/JioSecodary>";
    private static final String mJioSecodaryIndex_start = "<JioSecodary";
    private static final String mJioSecodaryInfoFileName = "rom_update_jio_settings.xml";
    private static final String mSubPath = "/data/data/com.android.phone/";
    private String mJioSecodaryContent = null;

    public RusUpdateJioSecodary() {
        setPath(mSubPath);
    }

    public void execute() {
        sendEmptyMessage(ValiateAndUpateJioSecodary());
    }

    public void onSucceed(Message msg) {
        try {
            int JioSecodaryInt = ((Integer) msg.obj).intValue();
            Log.d(TAG, "JioSecodaryInt:" + JioSecodaryInt);
            this.mPhone.setFactoryModeModemGPIO(45, JioSecodaryInt, null);
        } catch (Exception e) {
            Log.d(TAG, "hanlder doNVwrite wrong");
            e.printStackTrace();
        }
    }

    public int ValiateAndUpateJioSecodary() {
        Throwable th;
        Log.d(TAG, "getJioSecodaryXml");
        FileReader confreader = null;
        int JioSecodaryInt = 0;
        try {
            this.mJioSecodaryContent = createNetworkRomupdateXmlFile(getContent(), mJioSecodaryInfoFileName, mJioSecodaryIndex_start, mJioSecodaryIndex_end, 13);
            FileReader confreader2 = new FileReader(this.mFileUtils.saveToFile(this.mJioSecodaryContent, this.mPath + mJioSecodaryInfoFileName));
            try {
                XmlPullParser confparser = Xml.newPullParser();
                confparser.setInput(confreader2);
                if (confparser == null) {
                    Log.d(TAG, "confparser==null");
                    if (confreader2 != null) {
                        try {
                            confreader2.close();
                        } catch (IOException e) {
                        }
                    }
                    return 20;
                }
                XmlUtils.beginDocument(confparser, "JioSecodary");
                XmlUtils.nextElement(confparser);
                while (confparser.getEventType() != 1) {
                    if ("jio_setting".equals(confparser.getName())) {
                        JioSecodaryInt = Integer.parseInt(confparser.getAttributeValue(null, "jio_secodary"));
                        Log.d(TAG, " the JioSecodaryInt is :" + JioSecodaryInt);
                        SystemProperties.set("persist.radio.jio.secondary", Integer.toString(JioSecodaryInt));
                        Log.d(TAG, "this indicate the JioSecodary values set succuss!");
                        XmlUtils.nextElement(confparser);
                    } else {
                        Log.d(TAG, "this JioSecodary_values tag is not match");
                        if (confreader2 != null) {
                            try {
                                confreader2.close();
                            } catch (IOException e2) {
                            }
                        }
                        return 20;
                    }
                }
                setJioSecodary(JioSecodaryInt);
                if (confreader2 != null) {
                    try {
                        confreader2.close();
                    } catch (IOException e3) {
                    }
                }
                return 21;
            } catch (FileNotFoundException e4) {
                confreader = confreader2;
            } catch (Exception e5) {
                confreader = confreader2;
            } catch (Throwable th2) {
                th = th2;
                confreader = confreader2;
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
            try {
                Log.d(TAG, "getXmlFile Exception while parsing");
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e9) {
                    }
                }
                return 20;
            } catch (Throwable th3) {
                th = th3;
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e10) {
                    }
                }
                throw th;
            }
        }
    }

    private void setJioSecodary(int JioSecodaryInt) {
        Message msg = obtainSucceedMsg();
        msg.obj = Integer.valueOf(JioSecodaryInt);
        sendMessage(msg);
    }
}
