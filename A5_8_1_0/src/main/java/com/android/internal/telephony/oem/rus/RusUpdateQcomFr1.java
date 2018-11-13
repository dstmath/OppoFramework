package com.android.internal.telephony.oem.rus;

import android.os.Message;
import android.os.SystemProperties;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;

public final class RusUpdateQcomFr1 extends RusBase {
    private static final String TAG = "RusUpdateQcomFr1";
    private static final String mSubPath = "/data/data/com.android.phone/";
    private static final int mVolteFr1Index_count = 9;
    private static final String mVolteFr1Index_end = "/QcomFr1>";
    private static final String mVolteFr1Index_start = "<QcomFr1";
    private static final String mVolteFr1InfoFileName = "qual_update_voltefr1.xml";
    private String mVolteFr1Content = null;

    public RusUpdateQcomFr1() {
        setPath(mSubPath);
    }

    public void execute() {
        sendEmptyMessage(valiateAndUpateVolteFr1());
    }

    public void onSucceed(Message msg) {
        try {
            int fr1Values = ((Integer) msg.obj).intValue();
            printLog(TAG, "fr1Values:" + fr1Values);
            this.mPhone.oppoNoticeUpdateVolteFr(fr1Values, null);
        } catch (Exception e) {
            printLog(TAG, "hanlder doNVwrite wrong");
            e.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x0195 A:{SYNTHETIC, Splitter: B:55:0x0195} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x015b A:{SYNTHETIC, Splitter: B:31:0x015b} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x018a A:{SYNTHETIC, Splitter: B:48:0x018a} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int valiateAndUpateVolteFr1() {
        Throwable th;
        printLog(TAG, "getVolteFr1Xml");
        FileReader confreader = null;
        try {
            this.mVolteFr1Content = createNetworkRomupdateXmlFile(getContent(), mVolteFr1InfoFileName, mVolteFr1Index_start, mVolteFr1Index_end, 9);
            FileReader confreader2 = new FileReader(this.mFileUtils.saveToFile(this.mVolteFr1Content, this.mPath + mVolteFr1InfoFileName));
            try {
                XmlPullParser confparser = Xml.newPullParser();
                if (confparser != null) {
                    confparser.setInput(confreader2);
                    XmlUtils.beginDocument(confparser, "QcomFr1");
                    XmlUtils.nextElement(confparser);
                    while (confparser.getEventType() != 1) {
                        if ("fr1_values".equals(confparser.getName())) {
                            int fr1RsrpStart = Integer.parseInt(confparser.getAttributeValue(null, "fr1_rsrp_start"));
                            int fr1RsrpEnd = Integer.parseInt(confparser.getAttributeValue(null, "fr1_rsrp_end"));
                            int fr1Rsrq = Integer.parseInt(confparser.getAttributeValue(null, "fr1_rsrq"));
                            int fr1Timer = Integer.parseInt(confparser.getAttributeValue(null, "fr1_timer"));
                            printLog(TAG, " the volte fr1RsrpStart is :" + fr1RsrpStart + "fr1RsrpEnd is:" + fr1RsrpEnd + "fr1Rsrq is:" + fr1Rsrq + "fr1Timer is:" + fr1Timer);
                            String fr1Values = intToHex(fr1RsrpStart) + intToHex(fr1RsrpEnd) + intToHex(fr1Rsrq) + intToHex(fr1Timer);
                            if (fr1RsrpStart == 0 && fr1RsrpEnd == 0 && fr1Rsrq == 0 && fr1Timer == 0) {
                                fr1Values = "0";
                            }
                            SystemProperties.set("persist.radio.volte_timer", fr1Values);
                            printLog(TAG, "this indicate the fr1 values set succuss!");
                            XmlUtils.nextElement(confparser);
                        } else {
                            printLog(TAG, "this fr1_values tag is not match");
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
                setVolteFr1(1);
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
                    try {
                        confreader.close();
                    } catch (IOException e4) {
                    }
                }
                return 20;
            } catch (Exception e5) {
                confreader = confreader2;
                try {
                    printLog(TAG, "getXmlFile Exception while parsing");
                    if (confreader != null) {
                        try {
                            confreader.close();
                        } catch (IOException e6) {
                        }
                    }
                    return 20;
                } catch (Throwable th2) {
                    th = th2;
                    if (confreader != null) {
                        try {
                            confreader.close();
                        } catch (IOException e7) {
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
        } catch (FileNotFoundException e8) {
            if (confreader != null) {
            }
            return 20;
        } catch (Exception e9) {
            printLog(TAG, "getXmlFile Exception while parsing");
            if (confreader != null) {
            }
            return 20;
        }
    }

    private String intToHex(int n) {
        char[] ch = new char[20];
        int nIndex = 0;
        while (true) {
            int m = n / 16;
            int k = n % 16;
            if (k == 15) {
                ch[nIndex] = 'f';
            } else if (k == 14) {
                ch[nIndex] = 'e';
            } else if (k == 13) {
                ch[nIndex] = 'd';
            } else if (k == 12) {
                ch[nIndex] = 'c';
            } else if (k == 11) {
                ch[nIndex] = 'b';
            } else if (k == 10) {
                ch[nIndex] = 'a';
            } else {
                ch[nIndex] = (char) (k + 48);
            }
            nIndex++;
            if (m == 0) {
                break;
            }
            n = m;
        }
        StringBuffer sb;
        if (1 == nIndex) {
            sb = new StringBuffer();
            sb.append(ch, 0, nIndex);
            sb.reverse();
            return new String("0") + sb.toString();
        }
        sb = new StringBuffer();
        sb.append(ch, 0, nIndex);
        sb.reverse();
        return sb.toString();
    }

    private void setVolteFr1(int fr1Values) {
        Message msg = obtainSucceedMsg();
        msg.obj = Integer.valueOf(fr1Values);
        sendMessage(msg);
    }
}
