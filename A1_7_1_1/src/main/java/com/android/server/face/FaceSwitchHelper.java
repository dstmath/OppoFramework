package com.android.server.face;

import android.content.Context;
import android.hardware.face.FaceRusNativeData;
import android.util.Xml;
import com.android.server.face.utils.LogUtil;
import com.oppo.RomUpdateHelper;
import com.oppo.RomUpdateHelper.UpdateInfo;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FaceSwitchHelper extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/sys_face_switch_list.xml";
    public static final String FILTER_NAME = "sys_face_switch_list";
    private static final String SYS_FILE_DIR = "system/etc/sys_face_switch_list.xml";
    private final String TAG = "FaceService.Main.FaceSwitchHelper";
    private final ISwitchUpdateListener mISwitchUpdateListener;

    public interface ISwitchUpdateListener {
        void onFaceSwitchUpdate();

        void onFaceUpdateFromProvider();
    }

    private class FaceSwitchUpdateInfo extends UpdateInfo {
        static final String HAND_RAISE_SUPPORT = "handRaise_support";
        static final String THRESHOLD_HACKERNESS = "threshold_hackerness";
        private boolean mHandRaise_Support;
        private final ISwitchUpdateListener mISwitchUpdateListener;
        private float mThresHold_HackNess;

        public FaceSwitchUpdateInfo(ISwitchUpdateListener switchUpdateListener) {
            super(FaceSwitchHelper.this);
            this.mISwitchUpdateListener = switchUpdateListener;
        }

        /* JADX WARNING: Removed duplicated region for block: B:41:0x009a A:{SYNTHETIC, Splitter: B:41:0x009a} */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x0081 A:{SYNTHETIC, Splitter: B:33:0x0081} */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x00ab A:{SYNTHETIC, Splitter: B:47:0x00ab} */
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
                                    if (!HAND_RAISE_SUPPORT.equals(tmp)) {
                                        if (!THRESHOLD_HACKERNESS.equals(tmp)) {
                                            break;
                                        }
                                        eventType = parser.next();
                                        this.mThresHold_HackNess = Float.parseFloat(parser.getText());
                                        updated = true;
                                        break;
                                    }
                                    eventType = parser.next();
                                    this.mHandRaise_Support = "true".equals(parser.getText());
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
                                FaceSwitchHelper.this.log("Got execption close permReader.", e3);
                            }
                        }
                        strReader = strReader2;
                    } catch (XmlPullParserException e4) {
                        e2 = e4;
                        strReader = strReader2;
                        FaceSwitchHelper.this.log("Got execption parsing permissions.", e2);
                        if (strReader != null) {
                            try {
                                strReader.close();
                            } catch (IOException e32) {
                                FaceSwitchHelper.this.log("Got execption close permReader.", e32);
                            }
                        }
                        this.mISwitchUpdateListener.onFaceSwitchUpdate();
                    } catch (IOException e5) {
                        e32 = e5;
                        strReader = strReader2;
                        try {
                            FaceSwitchHelper.this.log("Got execption parsing permissions.", e32);
                            if (strReader != null) {
                                try {
                                    strReader.close();
                                } catch (IOException e322) {
                                    FaceSwitchHelper.this.log("Got execption close permReader.", e322);
                                }
                            }
                            this.mISwitchUpdateListener.onFaceSwitchUpdate();
                        } catch (Throwable th2) {
                            th = th2;
                            if (strReader != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        strReader = strReader2;
                        if (strReader != null) {
                            try {
                                strReader.close();
                            } catch (IOException e3222) {
                                FaceSwitchHelper.this.log("Got execption close permReader.", e3222);
                            }
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e6) {
                    e2 = e6;
                    FaceSwitchHelper.this.log("Got execption parsing permissions.", e2);
                    if (strReader != null) {
                    }
                    this.mISwitchUpdateListener.onFaceSwitchUpdate();
                } catch (IOException e7) {
                    e3222 = e7;
                    FaceSwitchHelper.this.log("Got execption parsing permissions.", e3222);
                    if (strReader != null) {
                    }
                    this.mISwitchUpdateListener.onFaceSwitchUpdate();
                }
                if (updated && this.mISwitchUpdateListener != null) {
                    this.mISwitchUpdateListener.onFaceSwitchUpdate();
                }
            }
        }

        public float getHackNessThresHold() {
            return this.mThresHold_HackNess;
        }

        public boolean getHandRaiseSupport() {
            return this.mHandRaise_Support;
        }

        public String dumpToString() {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("\nFace Switch Info:\n");
            strBuilder.append("handRaise_Support : ").append(this.mHandRaise_Support).append("\n");
            strBuilder.append("hackNess_thresHold : ").append(this.mThresHold_HackNess).append("\n");
            return strBuilder.toString();
        }
    }

    public FaceSwitchHelper(Context context, ISwitchUpdateListener switchUpdateListener) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new FaceSwitchUpdateInfo(switchUpdateListener), new FaceSwitchUpdateInfo(switchUpdateListener));
        this.mISwitchUpdateListener = switchUpdateListener;
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float getHackNessThresHold() {
        return ((FaceSwitchUpdateInfo) getUpdateInfo(true)).getHackNessThresHold();
    }

    public boolean getHandRaiseSupport() {
        return ((FaceSwitchUpdateInfo) getUpdateInfo(true)).getHandRaiseSupport();
    }

    public FaceRusNativeData getRusNativeData() {
        return new FaceRusNativeData(getHackNessThresHold());
    }

    public String dumpToString() {
        return ((FaceSwitchUpdateInfo) getUpdateInfo(true)).dumpToString();
    }

    public void getUpdateFromProvider() {
        super.getUpdateFromProvider();
        if (this.mISwitchUpdateListener != null) {
            this.mISwitchUpdateListener.onFaceUpdateFromProvider();
        }
        LogUtil.v("FaceService.Main.FaceSwitchHelper", "update FaceSwitchHelper config");
    }

    public void dump(PrintWriter pw, String[] args, String prefix) {
        pw.print(prefix);
        pw.println("FaceSwitchHelper dump");
        prefix = prefix + "    ";
        pw.print(prefix);
        pw.println("handeRaise_support = " + getHandRaiseSupport());
        pw.print(prefix);
        pw.println("hackness_thresHold = " + getHackNessThresHold());
    }
}
