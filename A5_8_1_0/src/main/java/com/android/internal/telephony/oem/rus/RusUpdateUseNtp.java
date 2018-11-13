package com.android.internal.telephony.oem.rus;

import android.util.Xml;
import com.android.internal.telephony.ServiceStateTracker;
import com.android.internal.telephony.uicc.SpnOverride;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public final class RusUpdateUseNtp extends RusBase {
    private static final String TAG = "RusUpdateUseNtp";
    private static final String mSubPath = "/data/data/com.android.phone/";
    private static final int mUseNtpIndex_count = 7;
    private static final String mUseNtpIndex_end = "/UseNtp>";
    private static final String mUseNtpIndex_start = "<UseNtp";
    private static final String mUseNtpInfoFileName = "use_ntp_DC.xml";
    private final ServiceStateTracker mSst;
    private String mUseNtpContent = null;

    public RusUpdateUseNtp() {
        setPath(mSubPath);
        this.mSst = this.mPhone.getServiceStateTracker();
    }

    public void execute() {
        ValiateAndUpateUseNtp();
    }

    private void ValiateAndUpateUseNtp() {
        this.mUseNtpContent = createNetworkRomupdateXmlFile(getContent(), mUseNtpInfoFileName, mUseNtpIndex_start, mUseNtpIndex_end, 7);
        this.mFileUtils.saveToFile(this.mUseNtpContent, this.mPath + mUseNtpInfoFileName);
        parseContentFromXML(this.mUseNtpContent);
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x0069 A:{SYNTHETIC, Splitter: B:39:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0042 A:{SYNTHETIC, Splitter: B:18:0x0042} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0058 A:{SYNTHETIC, Splitter: B:31:0x0058} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void parseContentFromXML(String content) {
        IOException e;
        XmlPullParserException e2;
        Throwable th;
        printLog(TAG, "getUseNtpXml:");
        if (content != null) {
            StringReader strReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                StringReader strReader2 = new StringReader(content);
                try {
                    parser.setInput(strReader2);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        switch (eventType) {
                            case 2:
                                String name = parser.getName();
                                eventType = parser.next();
                                this.mSst.setUseNtpTime(name, parser.getText());
                                break;
                            default:
                                break;
                        }
                    }
                    if (strReader2 != null) {
                        try {
                            strReader2.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                } catch (XmlPullParserException e4) {
                    e2 = e4;
                    strReader = strReader2;
                    try {
                        e2.printStackTrace();
                        if (strReader != null) {
                            try {
                                strReader.close();
                            } catch (IOException e32) {
                                e32.printStackTrace();
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (strReader != null) {
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e32 = e5;
                    strReader = strReader2;
                    e32.printStackTrace();
                    if (strReader != null) {
                        try {
                            strReader.close();
                        } catch (IOException e322) {
                            e322.printStackTrace();
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    strReader = strReader2;
                    if (strReader != null) {
                        try {
                            strReader.close();
                        } catch (IOException e3222) {
                            e3222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (XmlPullParserException e6) {
                e2 = e6;
                e2.printStackTrace();
                if (strReader != null) {
                }
            } catch (IOException e7) {
                e3222 = e7;
                e3222.printStackTrace();
                if (strReader != null) {
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0087 A:{SYNTHETIC, Splitter: B:32:0x0087} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0098 A:{SYNTHETIC, Splitter: B:40:0x0098} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String readFromFile() {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        File path = new File(this.mPath + mUseNtpInfoFileName);
        if (!path.exists()) {
            printLog(TAG, this.mPath + mUseNtpInfoFileName + " not exist!");
        }
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(path);
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(is2));
                StringBuffer buffer = new StringBuffer();
                String str = SpnOverride.MVNO_TYPE_NONE;
                while (true) {
                    str = in.readLine();
                    if (str == null) {
                        break;
                    }
                    buffer.append(str);
                }
                String stringBuffer = buffer.toString();
                if (is2 != null) {
                    try {
                        is2.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                return stringBuffer;
            } catch (FileNotFoundException e4) {
                e2 = e4;
                is = is2;
            } catch (IOException e5) {
                e3 = e5;
                is = is2;
                e3.printStackTrace();
                if (is != null) {
                }
                return null;
            } catch (Throwable th2) {
                th = th2;
                is = is2;
                if (is != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            try {
                e2.printStackTrace();
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e322) {
                        e322.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (IOException e7) {
            e322 = e7;
            e322.printStackTrace();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3222) {
                    e3222.printStackTrace();
                }
            }
            return null;
        }
    }
}
