package com.android.server.biometrics.face;

import android.content.Context;
import android.hardware.face.FaceRusNativeData;
import android.util.Xml;
import com.android.server.biometrics.face.utils.LogUtil;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.oppo.TemperatureProvider;
import com.oppo.RomUpdateHelper;
import java.io.FileReader;
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

    private class FaceSwitchUpdateInfo extends RomUpdateHelper.UpdateInfo {
        static final String HAND_RAISE_SUPPORT = "handRaise_support";
        static final String THRESHOLD_HACKERNESS = "threshold_hackerness";
        private boolean mHandRaiseSupport;
        private final ISwitchUpdateListener mISwitchUpdateListener;
        private float mThresHoldHackNess;

        public FaceSwitchUpdateInfo(ISwitchUpdateListener switchUpdateListener) {
            super(FaceSwitchHelper.this);
            this.mISwitchUpdateListener = switchUpdateListener;
        }

        public void parseContentFromXML(String content) {
            ISwitchUpdateListener iSwitchUpdateListener;
            if (content != null) {
                FileReader xmlReader = null;
                StringReader strReader = null;
                boolean updated = false;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    StringReader strReader2 = new StringReader(content);
                    parser.setInput(strReader2);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType != 0) {
                            if (eventType == 2) {
                                String tmp = parser.getName();
                                if (HAND_RAISE_SUPPORT.equals(tmp)) {
                                    parser.next();
                                    this.mHandRaiseSupport = TemperatureProvider.SWITCH_ON.equals(parser.getText());
                                    updated = true;
                                } else if (THRESHOLD_HACKERNESS.equals(tmp)) {
                                    parser.next();
                                    this.mThresHoldHackNess = Float.parseFloat(parser.getText());
                                    updated = true;
                                }
                            }
                        }
                    }
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e) {
                            FaceSwitchHelper.this.log("Got execption close permReader.", e);
                        }
                    }
                    strReader2.close();
                } catch (XmlPullParserException e2) {
                    FaceSwitchHelper.this.log("Got execption parsing permissions.", e2);
                    if (xmlReader != null) {
                        xmlReader.close();
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (IOException e3) {
                    FaceSwitchHelper.this.log("Got execption parsing permissions.", e3);
                    if (xmlReader != null) {
                        xmlReader.close();
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                } catch (Throwable th) {
                    if (xmlReader != null) {
                        try {
                            xmlReader.close();
                        } catch (IOException e4) {
                            FaceSwitchHelper.this.log("Got execption close permReader.", e4);
                            throw th;
                        }
                    }
                    if (strReader != null) {
                        strReader.close();
                    }
                    throw th;
                }
                if (updated && (iSwitchUpdateListener = this.mISwitchUpdateListener) != null) {
                    iSwitchUpdateListener.onFaceSwitchUpdate();
                }
            }
        }

        public float getHackNessThresHold() {
            return this.mThresHoldHackNess;
        }

        public boolean getHandRaiseSupport() {
            return this.mHandRaiseSupport;
        }

        public String dumpToString() {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("\nFace Switch Info:\n");
            strBuilder.append("handRaise_Support : " + this.mHandRaiseSupport + StringUtils.LF);
            strBuilder.append("hackNess_thresHold : " + this.mThresHoldHackNess + StringUtils.LF);
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
        return getUpdateInfo(true).getHackNessThresHold();
    }

    public boolean getHandRaiseSupport() {
        return getUpdateInfo(true).getHandRaiseSupport();
    }

    public FaceRusNativeData getRusNativeData() {
        return new FaceRusNativeData(getHackNessThresHold());
    }

    public String dumpToString() {
        return getUpdateInfo(true).dumpToString();
    }

    public void getUpdateFromProvider() {
        FaceSwitchHelper.super.getUpdateFromProvider();
        ISwitchUpdateListener iSwitchUpdateListener = this.mISwitchUpdateListener;
        if (iSwitchUpdateListener != null) {
            iSwitchUpdateListener.onFaceUpdateFromProvider();
        }
        LogUtil.v("FaceService.Main.FaceSwitchHelper", "update FaceSwitchHelper config");
    }

    public void dump(PrintWriter pw, String[] args, String prefix) {
        pw.print(prefix);
        pw.println("FaceSwitchHelper dump");
        String prefix2 = prefix + "    ";
        pw.print(prefix2);
        pw.println("handeRaise_support = " + getHandRaiseSupport());
        pw.print(prefix2);
        pw.println("hackness_thresHold = " + getHackNessThresHold());
    }
}
