package com.android.server.biometrics.fingerprint;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Xml;
import com.android.server.biometrics.fingerprint.tool.RomUpdateHelper;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.oppo.TemperatureProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class FingerprintSwitchHelper extends RomUpdateHelper {
    static final String AllOW_PACKAGE = "allowpackage";
    private static final String DATA_FILE_DIR = "data/system/fingerprint_switch_list.xml";
    public static final String FILTER_NAME = "fingerprint_switch_list";
    private static final String FP_OVERSEA_WHITELISTE_ENABLE = "fp_oversea_whitelist_enable";
    private static boolean IS_OVERSEA_DEVICE = false;
    private static final String OPTICAL_FINGERPRINT_FEATURE = "oppo.hardware.fingerprint.optical.support";
    private static final String OVERSEA_PROPERTIES = "ro.oppo.version";
    public static final String PROP_NAME_PSENSOR_SWITCH = "persist.sys.oppo.fp_psensor";
    public static final String PROP_NAME_TP_PROTECT_SWITCH = "persist.sys.oppo.fp_tpprotecet";
    private static final String SYS_FILE_DIR = "system/etc/fingerprint_switch_list.xml";
    public static final String TAG = "FingerprintSwitchHelper";
    public static final String VERSION_NAME = "version";
    public static boolean isHardwareNeed = false;
    private final Context mContext;
    private boolean mOpticalFingerprintSupport = false;
    private WhiteListChangeObserver mWhiteListChangeObserver;
    private int mWhiteListEnabled = 1;

    public interface ISwitchUpdateListener {
        void onFingerprintSwitchUpdate();
    }

    /* access modifiers changed from: private */
    public class FingerprintSwitchUpdateInfo extends RomUpdateHelper.UpdateInfo {
        static final String SWITCH_PSENSOR = "psensor";
        static final String SWITCH_TPPROTECT = "tpprotecet";
        private ISwitchUpdateListener mISwitchUpdateListener;
        private ArrayList<String> mOverSeaWhiteList = new ArrayList<>();
        private boolean mPsensorSwitch;
        private boolean mTPProtectSwitch;

        public FingerprintSwitchUpdateInfo(ISwitchUpdateListener switchUpdateListener) {
            super();
            this.mISwitchUpdateListener = switchUpdateListener;
        }

        @Override // com.android.server.biometrics.fingerprint.tool.RomUpdateHelper.UpdateInfo
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
                                if (SWITCH_PSENSOR.equals(tmp)) {
                                    parser.next();
                                    this.mPsensorSwitch = TemperatureProvider.SWITCH_ON.equals(parser.getText());
                                    updated = true;
                                } else if (SWITCH_TPPROTECT.equals(tmp)) {
                                    parser.next();
                                    this.mTPProtectSwitch = TemperatureProvider.SWITCH_ON.equals(parser.getText());
                                    updated = true;
                                } else if (FingerprintSwitchHelper.AllOW_PACKAGE.equals(tmp)) {
                                    int eventType2 = parser.next();
                                    String text = parser.getText();
                                    LogUtil.d(FingerprintSwitchHelper.TAG, "eventType = " + eventType2 + ", text = " + text);
                                    this.mOverSeaWhiteList.add(text);
                                } else if ("version".equals(tmp)) {
                                    parser.next();
                                    updateConfigVersion(tmp, parser.getText());
                                }
                            }
                        }
                    }
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e) {
                            FingerprintSwitchHelper.this.log("Got execption close permReader.", e);
                        }
                    }
                    strReader2.close();
                } catch (XmlPullParserException e2) {
                    FingerprintSwitchHelper.this.log("Got execption parsing permissions.", e2);
                    if (0 != 0) {
                        xmlReader.close();
                    }
                    if (0 != 0) {
                        strReader.close();
                    }
                } catch (IOException e3) {
                    FingerprintSwitchHelper.this.log("Got execption parsing permissions.", e3);
                    if (0 != 0) {
                        xmlReader.close();
                    }
                    if (0 != 0) {
                        strReader.close();
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            xmlReader.close();
                        } catch (IOException e4) {
                            FingerprintSwitchHelper.this.log("Got execption close permReader.", e4);
                            throw th;
                        }
                    }
                    if (0 != 0) {
                        strReader.close();
                    }
                    throw th;
                }
                if (updated && (iSwitchUpdateListener = this.mISwitchUpdateListener) != null) {
                    iSwitchUpdateListener.onFingerprintSwitchUpdate();
                }
            }
        }

        private void updateConfigVersion(String type, String value) {
            LogUtil.d(FingerprintSwitchHelper.TAG, hashCode() + " updateConfigVersion, type = " + type + ", value = " + value);
            if ("version".equals(type)) {
                this.mVersion = (long) Integer.parseInt(value);
            }
        }

        @Override // com.android.server.biometrics.fingerprint.tool.RomUpdateHelper.UpdateInfo
        public boolean updateToLowerVersion(String content) {
            long newVersion = getContentVersion(content);
            LogUtil.d(FingerprintSwitchHelper.TAG, "upateToLowerVersion, newVersion = " + newVersion + ", mVersion = " + this.mVersion);
            return newVersion < this.mVersion;
        }

        private long getContentVersion(String content) {
            long version = -1;
            if (content == null) {
                return -1;
            }
            FileReader xmlReader = null;
            StringReader strReader = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                StringReader strReader2 = new StringReader(content);
                parser.setInput(strReader2);
                int eventType = parser.getEventType();
                boolean found = false;
                while (true) {
                    if (eventType == 1) {
                        break;
                    }
                    if (eventType != 0) {
                        if (eventType != 2) {
                            if (eventType != 3) {
                            }
                        } else if ("version".equals(parser.getName())) {
                            int eventType2 = parser.next();
                            String text = parser.getText();
                            LogUtil.d(FingerprintSwitchHelper.TAG, "eventType = " + eventType2 + ", text = " + text);
                            version = (long) Integer.parseInt(parser.getText());
                            found = true;
                        }
                    }
                    if (found) {
                        break;
                    }
                    eventType = parser.next();
                }
                if (0 != 0) {
                    try {
                        xmlReader.close();
                    } catch (IOException e) {
                        FingerprintSwitchHelper.this.log("Got execption close permReader.", e);
                    }
                }
                strReader2.close();
                return version;
            } catch (XmlPullParserException e2) {
                FingerprintSwitchHelper.this.log("Got execption parsing permissions.", e2);
                if (0 != 0) {
                    try {
                        xmlReader.close();
                    } catch (IOException e3) {
                        FingerprintSwitchHelper.this.log("Got execption close permReader.", e3);
                        return -1;
                    }
                }
                if (0 == 0) {
                    return -1;
                }
                strReader.close();
                return -1;
            } catch (IOException e4) {
                FingerprintSwitchHelper.this.log("Got execption parsing permissions.", e4);
                if (0 != 0) {
                    try {
                        xmlReader.close();
                    } catch (IOException e5) {
                        FingerprintSwitchHelper.this.log("Got execption close permReader.", e5);
                        return -1;
                    }
                }
                if (0 == 0) {
                    return -1;
                }
                strReader.close();
                return -1;
            } catch (Throwable th) {
                if (0 != 0) {
                    try {
                        xmlReader.close();
                    } catch (IOException e6) {
                        FingerprintSwitchHelper.this.log("Got execption close permReader.", e6);
                        throw th;
                    }
                }
                if (0 != 0) {
                    strReader.close();
                }
                throw th;
            }
        }

        public ArrayList<String> getOverSeaWhiteList() {
            return this.mOverSeaWhiteList;
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
            strBuilder.append("mPsensorSwitch : " + this.mPsensorSwitch + StringUtils.LF);
            strBuilder.append("mTPProtectSwitch : " + this.mTPProtectSwitch + StringUtils.LF);
            strBuilder.append("\nFingerprint overseas allow whitelist Info:\n");
            Iterator<String> it = this.mOverSeaWhiteList.iterator();
            while (it.hasNext()) {
                strBuilder.append("package:" + it.next() + StringUtils.LF);
            }
            return strBuilder.toString();
        }
    }

    public FingerprintSwitchHelper(Context context, ISwitchUpdateListener switchUpdateListener) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new FingerprintSwitchUpdateInfo(switchUpdateListener), new FingerprintSwitchUpdateInfo(switchUpdateListener));
        this.mContext = context;
        if (!"CN".equals(SystemProperties.get(OVERSEA_PROPERTIES, "CN"))) {
            IS_OVERSEA_DEVICE = true;
        }
        Context context2 = this.mContext;
        if (context2 != null) {
            this.mOpticalFingerprintSupport = context2.getPackageManager().hasSystemFeature("oppo.hardware.fingerprint.optical.support");
            if (this.mContext.getContentResolver() != null) {
                this.mWhiteListChangeObserver = new WhiteListChangeObserver();
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(FP_OVERSEA_WHITELISTE_ENABLE), true, this.mWhiteListChangeObserver);
            }
        }
        String result = readFileByLines("/proc/fp_id");
        LogUtil.d(TAG, "FingerprintSwitchHelper init fp_id = " + result);
        if (result != null && !result.contains("G_OPTICAL_")) {
            isHardwareNeed = true;
        }
    }

    public static String readFileByLines(String fileName) {
        StringBuilder sb;
        BufferedReader reader = null;
        String tempString = null;
        try {
            reader = new BufferedReader(new FileReader(new File(fileName)));
            tempString = reader.readLine();
            LogUtil.d(TAG, "readFileByLines tempString:" + tempString);
            try {
                reader.close();
            } catch (IOException e) {
                e1 = e;
                sb = new StringBuilder();
            }
        } catch (IOException e2) {
            LogUtil.e(TAG, "readFileByLines io exception:" + e2.getMessage());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    e1 = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    LogUtil.e(TAG, "readFileByLines io close exception :" + e1.getMessage());
                }
            }
            throw th;
        }
        return tempString;
        sb.append("readFileByLines io close exception :");
        sb.append(e1.getMessage());
        LogUtil.e(TAG, sb.toString());
        return tempString;
    }

    public void initConfig() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isInOverSeasWhiteList(String pkg) {
        if (!isHardwareNeed || !this.mOpticalFingerprintSupport || !IS_OVERSEA_DEVICE) {
            return true;
        }
        if (this.mWhiteListEnabled == 0 || SystemProperties.getBoolean("persist.oppooversea.fplist.debug", false)) {
            LogUtil.d(TAG, "isInOverSeasWhiteList   mWhiteListEnabled == 0 | persist.oppooversea.fplist.debug=true");
            return true;
        }
        FingerprintSwitchUpdateInfo tempInfo = (FingerprintSwitchUpdateInfo) getUpdateInfo(true);
        if (tempInfo == null) {
            LogUtil.d(TAG, "can not get FingerprintSwitchUpdateInfo");
            return true;
        }
        ArrayList<String> tmp = tempInfo.getOverSeaWhiteList();
        if (tmp == null || !tmp.contains(pkg)) {
            return false;
        }
        return true;
    }

    private class WhiteListChangeObserver extends ContentObserver {
        public WhiteListChangeObserver(Handler handler) {
            super(handler);
        }

        public WhiteListChangeObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (selfChange) {
                FingerprintSwitchHelper fingerprintSwitchHelper = FingerprintSwitchHelper.this;
                fingerprintSwitchHelper.mWhiteListEnabled = Settings.Global.getInt(fingerprintSwitchHelper.mContext.getContentResolver(), FingerprintSwitchHelper.FP_OVERSEA_WHITELISTE_ENABLE, 1);
                LogUtil.d(FingerprintSwitchHelper.TAG, "WhiteListChangeObserver --> mWhiteListEnabled =" + FingerprintSwitchHelper.this.mWhiteListEnabled);
            }
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
