package com.android.internal.telephony.oem.rus;

import android.util.Xml;
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

public final class RusUpdateQcomMdt extends RusBase {
    private static final String MDT_TAG = "mdt_disable";
    private static final String TAG = "RusUpdateQcomMdt";
    private static int UPDATE_MDT = 6;
    private static final int mMdtIndex_count = 9;
    private static final String mMdtIndex_end = "/QcomMdt>";
    private static final String mMdtIndex_start = "<QcomMdt";
    private static final String mMdtInfoFileName = "qcom_update_mdt.xml";
    private static final String mSubPath = "/data/data/com.android.phone/";
    private String mMdtContent = null;

    public RusUpdateQcomMdt() {
        setPath(mSubPath);
    }

    public void execute() {
        ValiateAndUpateMdt();
    }

    private void updateNvToEFS(String mdt_value) {
        byte[] mdtArray = new byte[6];
        printLog(TAG, "on Succeed mdt_disable = " + mdt_value);
        if ("1".equals(mdt_value)) {
            mdtArray[0] = (byte) -1;
            mdtArray[1] = (byte) -1;
            mdtArray[2] = (byte) -17;
            mdtArray[3] = (byte) -1;
            mdtArray[4] = (byte) 1;
            mdtArray[5] = (byte) UPDATE_MDT;
        } else if ("0".equals(mdt_value)) {
            mdtArray[0] = (byte) -1;
            mdtArray[1] = (byte) -1;
            mdtArray[2] = (byte) -1;
            mdtArray[3] = (byte) -1;
            mdtArray[4] = (byte) 0;
            mdtArray[5] = (byte) UPDATE_MDT;
        } else {
            printLog(TAG, "updateNvToEFS input error, return");
            return;
        }
        try {
            this.mPhone.oppoUpdatePplmnList(mdtArray, null);
        } catch (Exception e) {
            printLog(TAG, "hanlder doNVwrite wrong");
            e.printStackTrace();
        }
    }

    private int ValiateAndUpateMdt() {
        printLog(TAG, "getMdtXmlFile");
        this.mMdtContent = createNetworkRomupdateXmlFile(getContent(), mMdtInfoFileName, mMdtIndex_start, mMdtIndex_end, 9);
        this.mFileUtils.saveToFile(this.mMdtContent, this.mPath + mMdtInfoFileName);
        return parseContentFromXML(this.mMdtContent);
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x00aa A:{SYNTHETIC, Splitter: B:47:0x00aa} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0082 A:{SYNTHETIC, Splitter: B:24:0x0082} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0099 A:{SYNTHETIC, Splitter: B:38:0x0099} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int parseContentFromXML(String content) {
        IOException e;
        XmlPullParserException e2;
        Throwable th;
        printLog(TAG, "getMdtXml:");
        if (content == null) {
            return 20;
        }
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
                            String value = parser.getText();
                            printLog(TAG, "name = " + name + ",value= " + value);
                            if (MDT_TAG.equals(name) && ("1".equals(value) || "0".equals(value))) {
                                updateNvToEFS(value);
                                break;
                            }
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
                return 21;
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
                    return 20;
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
                return 20;
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
            return 20;
        } catch (IOException e7) {
            e3222 = e7;
            e3222.printStackTrace();
            if (strReader != null) {
            }
            return 20;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0087 A:{SYNTHETIC, Splitter: B:32:0x0087} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0098 A:{SYNTHETIC, Splitter: B:40:0x0098} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String readFromFile() {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        File path = new File(this.mPath + mMdtInfoFileName);
        if (!path.exists()) {
            printLog(TAG, this.mPath + mMdtInfoFileName + " not exist!");
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
