package com.android.server.Fingerprint;

import android.content.Context;
import android.util.Xml;
import com.oppo.RomUpdateHelper;
import com.oppo.RomUpdateHelper.UpdateInfo;
import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FingerprintSwitchHelper extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/fingerprint_switch_list.xml";
    public static final String FILTER_NAME = "fingerprint_switch_list";
    public static final String PROP_NAME_PSENSOR_SWITCH = "persist.sys.oppo.fp_psensor";
    public static final String PROP_NAME_TP_PROTECT_SWITCH = "persist.sys.oppo.fp_tpprotecet";
    private static final String SYS_FILE_DIR = "system/etc/fingerprint_switch_list.xml";

    private class FingerprintSwitchUpdateInfo extends UpdateInfo {
        static final String SWITCH_PSENSOR = "psensor";
        static final String SWITCH_TPPROTECT = "tpprotecet";
        private ISwitchUpdateListener mISwitchUpdateListener;
        private boolean mPsensorSwitch;
        private boolean mTPProtectSwitch;

        public FingerprintSwitchUpdateInfo(ISwitchUpdateListener switchUpdateListener) {
            super(FingerprintSwitchHelper.this);
            this.mISwitchUpdateListener = switchUpdateListener;
        }

        /* JADX WARNING: Removed duplicated region for block: B:41:0x009d A:{SYNTHETIC, Splitter: B:41:0x009d} */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x0084 A:{SYNTHETIC, Splitter: B:33:0x0084} */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x00ae A:{SYNTHETIC, Splitter: B:47:0x00ae} */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void parseContentFromXML(String content) {
            IOException e;
            XmlPullParserException e2;
            Throwable th;
            if (content != null) {
                StringReader strReader = null;
                boolean updated = false;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader2 = new StringReader(content);
                    try {
                        parser.setInput(strReader2);
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    String tmp = parser.getName();
                                    if (!SWITCH_PSENSOR.equals(tmp)) {
                                        if (!SWITCH_TPPROTECT.equals(tmp)) {
                                            break;
                                        }
                                        eventType = parser.next();
                                        this.mTPProtectSwitch = "true".equals(parser.getText());
                                        updated = true;
                                        break;
                                    }
                                    eventType = parser.next();
                                    this.mPsensorSwitch = "true".equals(parser.getText());
                                    updated = true;
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (strReader2 != null) {
                            try {
                                strReader2.close();
                            } catch (IOException e3) {
                                FingerprintSwitchHelper.this.log("Got execption close permReader.", e3);
                            }
                        }
                        strReader = strReader2;
                    } catch (XmlPullParserException e4) {
                        e2 = e4;
                        strReader = strReader2;
                        FingerprintSwitchHelper.this.log("Got execption parsing permissions.", e2);
                        if (strReader != null) {
                        }
                        this.mISwitchUpdateListener.onFingerprintSwitchUpdate();
                    } catch (IOException e5) {
                        e3 = e5;
                        strReader = strReader2;
                        try {
                            FingerprintSwitchHelper.this.log("Got execption parsing permissions.", e3);
                            if (strReader != null) {
                            }
                            this.mISwitchUpdateListener.onFingerprintSwitchUpdate();
                        } catch (Throwable th2) {
                            th = th2;
                            if (strReader != null) {
                                try {
                                    strReader.close();
                                } catch (IOException e32) {
                                    FingerprintSwitchHelper.this.log("Got execption close permReader.", e32);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        strReader = strReader2;
                        if (strReader != null) {
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e6) {
                    e2 = e6;
                    FingerprintSwitchHelper.this.log("Got execption parsing permissions.", e2);
                    if (strReader != null) {
                        try {
                            strReader.close();
                        } catch (IOException e322) {
                            FingerprintSwitchHelper.this.log("Got execption close permReader.", e322);
                        }
                    }
                    this.mISwitchUpdateListener.onFingerprintSwitchUpdate();
                } catch (IOException e7) {
                    e322 = e7;
                    FingerprintSwitchHelper.this.log("Got execption parsing permissions.", e322);
                    if (strReader != null) {
                        try {
                            strReader.close();
                        } catch (IOException e3222) {
                            FingerprintSwitchHelper.this.log("Got execption close permReader.", e3222);
                        }
                    }
                    this.mISwitchUpdateListener.onFingerprintSwitchUpdate();
                }
                if (updated && this.mISwitchUpdateListener != null) {
                    this.mISwitchUpdateListener.onFingerprintSwitchUpdate();
                }
            }
        }

        public boolean getPsensorSwitch() {
            return this.mPsensorSwitch;
        }

        public boolean getTPProtectSwitch() {
            return this.mTPProtectSwitch;
        }

        public String dumpToString() {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("\nFingerprint Switch Info:\n");
            strBuilder.append("mPsensorSwitch : ").append(this.mPsensorSwitch).append("\n");
            strBuilder.append("mTPProtectSwitch : ").append(this.mTPProtectSwitch).append("\n");
            return strBuilder.toString();
        }
    }

    public interface ISwitchUpdateListener {
        void onFingerprintSwitchUpdate();
    }

    public FingerprintSwitchHelper(Context context, ISwitchUpdateListener switchUpdateListener) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new FingerprintSwitchUpdateInfo(switchUpdateListener), new FingerprintSwitchUpdateInfo(switchUpdateListener));
    }

    public void initConfig() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getPsensorSwitch() {
        return ((FingerprintSwitchUpdateInfo) getUpdateInfo(true)).getPsensorSwitch();
    }

    public boolean getTPProtectSwitch() {
        return ((FingerprintSwitchUpdateInfo) getUpdateInfo(true)).getTPProtectSwitch();
    }

    public String dumpToString() {
        return ((FingerprintSwitchUpdateInfo) getUpdateInfo(true)).dumpToString();
    }
}
