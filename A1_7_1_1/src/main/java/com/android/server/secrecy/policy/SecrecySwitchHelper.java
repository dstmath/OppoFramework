package com.android.server.secrecy.policy;

import android.content.Context;
import android.util.Xml;
import com.android.server.secrecy.policy.util.LogUtil;
import com.android.server.secrecy.work.ActivityConfig;
import com.oppo.RomUpdateHelper;
import com.oppo.RomUpdateHelper.UpdateInfo;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SecrecySwitchHelper extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/sys_secrecy_switch_list.xml";
    public static final String FILTER_NAME = "sys_secrecy_switch_list";
    private static final String SYS_FILE_DIR = "system/etc/sys_secrecy_switch_list.xml";
    private final String TAG = "SecrecyService.SecrecySwitchHelper";
    private final ActivityConfig mActivityCofig = ActivityConfig.getInstance();
    private final ISwitchUpdateListener mISwitchUpdateListener;

    public interface ISwitchUpdateListener {
        void onSecrecySwitchUpdate();

        void onSecrecyUpdateFromProvider();
    }

    private class SecrecySwitchUpdateInfo extends UpdateInfo {
        static final String SWITCH_ACTIVITY = "activity_sw";
        static final String SWITCH_ADB = "adb_sw";
        static final String SWITCH_ID_TIMEOUT = "id_timeout";
        static final String SWITCH_LOG = "log_sw";
        static final String SWITCH_MAC_TIMEOUT = "mac_timeout";
        static final String SWITCH_SUPPORT = "support";
        static final String SWITCH_TEST = "test_sw";
        private final Object TAG_ACTIVITY = "activity";
        private boolean mActivitySwitch;
        private boolean mAdbSwitch;
        private final ISwitchUpdateListener mISwitchUpdateListener;
        private int mIdTimeout;
        private boolean mLogSwitch;
        private int mMacTimeout;
        private boolean mSupportSwitch;
        private boolean mTestSwitch;

        public SecrecySwitchUpdateInfo(ISwitchUpdateListener switchUpdateListener) {
            super(SecrecySwitchHelper.this);
            this.mISwitchUpdateListener = switchUpdateListener;
        }

        /* JADX WARNING: Removed duplicated region for block: B:59:0x0143 A:{SYNTHETIC, Splitter: B:59:0x0143} */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x012a A:{SYNTHETIC, Splitter: B:51:0x012a} */
        /* JADX WARNING: Removed duplicated region for block: B:65:0x0154 A:{SYNTHETIC, Splitter: B:65:0x0154} */
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
                        SecrecySwitchHelper.this.mActivityCofig.clearActivityInfo();
                        for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                            switch (eventType) {
                                case 2:
                                    String tmp = parser.getName();
                                    if (!SWITCH_SUPPORT.equals(tmp)) {
                                        if (!SWITCH_ACTIVITY.equals(tmp)) {
                                            if (!SWITCH_LOG.equals(tmp)) {
                                                if (!SWITCH_ADB.equals(tmp)) {
                                                    if (!SWITCH_TEST.equals(tmp)) {
                                                        if (!SWITCH_MAC_TIMEOUT.equals(tmp)) {
                                                            if (!SWITCH_ID_TIMEOUT.equals(tmp)) {
                                                                if (!this.TAG_ACTIVITY.equals(tmp)) {
                                                                    break;
                                                                }
                                                                SecrecySwitchHelper.this.mActivityCofig.parseActivityInfo(parser);
                                                                updated = true;
                                                                break;
                                                            }
                                                            eventType = parser.next();
                                                            this.mIdTimeout = Integer.parseInt(parser.getText());
                                                            updated = true;
                                                            break;
                                                        }
                                                        eventType = parser.next();
                                                        this.mMacTimeout = Integer.parseInt(parser.getText());
                                                        updated = true;
                                                        break;
                                                    }
                                                    eventType = parser.next();
                                                    this.mTestSwitch = "true".equals(parser.getText());
                                                    updated = true;
                                                    break;
                                                }
                                                eventType = parser.next();
                                                this.mAdbSwitch = "true".equals(parser.getText());
                                                updated = true;
                                                break;
                                            }
                                            eventType = parser.next();
                                            this.mLogSwitch = "true".equals(parser.getText());
                                            updated = true;
                                            break;
                                        }
                                        eventType = parser.next();
                                        this.mActivitySwitch = "true".equals(parser.getText());
                                        updated = true;
                                        break;
                                    }
                                    eventType = parser.next();
                                    this.mSupportSwitch = "true".equals(parser.getText());
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
                                SecrecySwitchHelper.this.log("Got execption close permReader.", e3);
                            }
                        }
                        strReader = strReader2;
                    } catch (XmlPullParserException e4) {
                        e2 = e4;
                        strReader = strReader2;
                        SecrecySwitchHelper.this.log("Got execption parsing permissions.", e2);
                        if (strReader != null) {
                        }
                        this.mISwitchUpdateListener.onSecrecySwitchUpdate();
                    } catch (IOException e5) {
                        e3 = e5;
                        strReader = strReader2;
                        try {
                            SecrecySwitchHelper.this.log("Got execption parsing permissions.", e3);
                            if (strReader != null) {
                            }
                            this.mISwitchUpdateListener.onSecrecySwitchUpdate();
                        } catch (Throwable th2) {
                            th = th2;
                            if (strReader != null) {
                                try {
                                    strReader.close();
                                } catch (IOException e32) {
                                    SecrecySwitchHelper.this.log("Got execption close permReader.", e32);
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
                    SecrecySwitchHelper.this.log("Got execption parsing permissions.", e2);
                    if (strReader != null) {
                        try {
                            strReader.close();
                        } catch (IOException e322) {
                            SecrecySwitchHelper.this.log("Got execption close permReader.", e322);
                        }
                    }
                    this.mISwitchUpdateListener.onSecrecySwitchUpdate();
                } catch (IOException e7) {
                    e322 = e7;
                    SecrecySwitchHelper.this.log("Got execption parsing permissions.", e322);
                    if (strReader != null) {
                        try {
                            strReader.close();
                        } catch (IOException e3222) {
                            SecrecySwitchHelper.this.log("Got execption close permReader.", e3222);
                        }
                    }
                    this.mISwitchUpdateListener.onSecrecySwitchUpdate();
                }
                if (updated && this.mISwitchUpdateListener != null) {
                    this.mISwitchUpdateListener.onSecrecySwitchUpdate();
                }
            }
        }

        public boolean getSupportSwitch() {
            return this.mSupportSwitch;
        }

        public boolean getActivitySwitch() {
            return this.mActivitySwitch;
        }

        public boolean getLogSwitch() {
            return this.mLogSwitch;
        }

        public boolean getAdbSwitch() {
            return this.mAdbSwitch;
        }

        public boolean getTestSwitch() {
            return this.mTestSwitch;
        }

        public int getMacTimeout() {
            return this.mMacTimeout;
        }

        public int getIdTimeout() {
            return this.mIdTimeout;
        }

        public String dumpToString() {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("\nSecrecy Switch Info:\n");
            strBuilder.append("support : ").append(this.mSupportSwitch).append("\n");
            strBuilder.append("activity : ").append(this.mActivitySwitch).append("\n");
            strBuilder.append("log : ").append(this.mLogSwitch).append("\n");
            strBuilder.append("adb : ").append(this.mAdbSwitch).append("\n");
            strBuilder.append("test : ").append(this.mTestSwitch).append("\n");
            strBuilder.append("mac_timeout : ").append(this.mMacTimeout).append("\n");
            strBuilder.append("id_timeout : ").append(this.mIdTimeout).append("\n");
            return strBuilder.toString();
        }
    }

    public SecrecySwitchHelper(Context context, ISwitchUpdateListener switchUpdateListener) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new SecrecySwitchUpdateInfo(switchUpdateListener), new SecrecySwitchUpdateInfo(switchUpdateListener));
        this.mISwitchUpdateListener = switchUpdateListener;
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getSupportSwitch() {
        return ((SecrecySwitchUpdateInfo) getUpdateInfo(true)).getSupportSwitch();
    }

    public boolean getActivitySwitch() {
        return ((SecrecySwitchUpdateInfo) getUpdateInfo(true)).getActivitySwitch();
    }

    public boolean getLogSwitch() {
        return ((SecrecySwitchUpdateInfo) getUpdateInfo(true)).getLogSwitch();
    }

    public boolean getAdbSwitch() {
        return ((SecrecySwitchUpdateInfo) getUpdateInfo(true)).getAdbSwitch();
    }

    public boolean getTestSwitch() {
        return ((SecrecySwitchUpdateInfo) getUpdateInfo(true)).getTestSwitch();
    }

    public int getMacTimeout() {
        return ((SecrecySwitchUpdateInfo) getUpdateInfo(true)).getMacTimeout();
    }

    public int getIdTimeout() {
        return ((SecrecySwitchUpdateInfo) getUpdateInfo(true)).getIdTimeout();
    }

    public String dumpToString() {
        return ((SecrecySwitchUpdateInfo) getUpdateInfo(true)).dumpToString();
    }

    public void getUpdateFromProvider() {
        super.getUpdateFromProvider();
        if (this.mISwitchUpdateListener != null) {
            this.mISwitchUpdateListener.onSecrecyUpdateFromProvider();
        }
        LogUtil.v("SecrecyService.SecrecySwitchHelper", "update SecrecySwitchHelper config");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println("SecrecySwitchHelper dump");
        prefix = prefix + "    ";
        pw.print(prefix);
        pw.println("support = " + getSupportSwitch());
        pw.print(prefix);
        pw.println("activityswitch = " + getActivitySwitch());
        pw.print(prefix);
        pw.println("logswitch = " + getLogSwitch());
        pw.print(prefix);
        pw.println("adb = " + getAdbSwitch());
        pw.print(prefix);
        pw.println("mac_timeout = " + getMacTimeout());
        pw.print(prefix);
        pw.println("id_timeout = " + getIdTimeout());
    }
}
