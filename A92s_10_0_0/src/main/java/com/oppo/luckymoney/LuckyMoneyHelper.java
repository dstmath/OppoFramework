package com.oppo.luckymoney;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Contacts;
import android.telecom.Logging.Session;
import android.telephony.SmsManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Xml;
import com.oppo.luckymoney.RomUpdateHelper;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class LuckyMoneyHelper extends RomUpdateHelper {
    public static final String ATT_MODE_ID = "id";
    public static final String ATT_VERSION_END = "end";
    public static final String ATT_VERSION_MODE = "mode";
    public static final String ATT_VERSION_START = "start";
    private static final String DATA_FILE_DIR = "data/system/sys_luckymoney_config_list.xml";
    public static final String FILTER_NAME = "sys_luckymoney_config_list";
    /* access modifiers changed from: private */
    public static final boolean IS_OVERSEA_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    public static final String MODE_2_GET_HASH_MODE = "get_hash";
    public static final String MODE_2_HG_HASH = "hg_hash";
    private static final String SYS_FILE_DIR = "system/etc/sys_luckymoney_config_list.xml";
    private static final String TAG = "LuckyMoneyHelper";

    public static class VersionItem {
        ArrayMap<String, Integer> modeValues = new ArrayMap<>();
        ArrayMap<String, String> specValues = new ArrayMap<>();

        public int getMode() {
            if (this.modeValues.containsKey("mode")) {
                return this.modeValues.get("mode").intValue();
            }
            return 0;
        }

        public int getStart() {
            if (this.modeValues.containsKey("start")) {
                return this.modeValues.get("start").intValue();
            }
            return Integer.MIN_VALUE;
        }

        public int getEnd() {
            if (this.modeValues.containsKey("end")) {
                return this.modeValues.get("end").intValue();
            }
            return Integer.MAX_VALUE;
        }

        public ArrayMap<String, String> getSpecValues() {
            return this.specValues;
        }

        public String toString() {
            return "[mode=" + getMode() + ", start=" + getStart() + ", end=" + getEnd() + "]\n   " + this.specValues.toString() + "\n";
        }
    }

    public static class AppItem {
        int mAppType = 0;
        ArrayList<AppVersionItem> mAppVersionItems;
        String mChatView = null;
        String mKeyDNSinfo = null;
        String mKeyURLinfo = null;
        ArrayList<String> mOpenHbActivity;
        String mReceiverClass = null;

        public AppItem(int appType, String keyURLinfo, String keyDNSinfo, String chatView, String receiverClass, ArrayList<String> openHbActivity, ArrayList<AppVersionItem> appVersionItems) {
            this.mAppType = appType;
            this.mKeyURLinfo = keyURLinfo;
            this.mKeyDNSinfo = keyDNSinfo;
            this.mChatView = chatView;
            this.mOpenHbActivity = openHbActivity;
            this.mReceiverClass = receiverClass;
            this.mAppVersionItems = appVersionItems;
        }

        public int getAppType() {
            return this.mAppType;
        }

        public AppVersionItem getAppVersionItem(int versionCode) {
            AppVersionItem result = null;
            Iterator<AppVersionItem> it = this.mAppVersionItems.iterator();
            while (it.hasNext()) {
                AppVersionItem temp = it.next();
                if (versionCode >= temp.getStart() && versionCode < temp.getEnd()) {
                    result = temp;
                }
            }
            return result;
        }

        public String getReceiverClass() {
            return this.mReceiverClass;
        }

        public ArrayList<String> getOpenHbActivity() {
            return this.mOpenHbActivity;
        }

        public String getChatView() {
            return this.mChatView;
        }
    }

    public static class AppVersionItem {
        int mDefaultMode;
        int mEnd;
        ArrayList<LuckMoneyMode> mLuckMoneyMode;
        int mStart;

        public AppVersionItem(int start, int end, int defaultMode, ArrayList<LuckMoneyMode> luckMoneyMode) {
            this.mStart = start;
            this.mEnd = end;
            this.mDefaultMode = defaultMode;
            this.mLuckMoneyMode = luckMoneyMode;
        }

        public int getStart() {
            return this.mStart;
        }

        public int getEnd() {
            return this.mEnd;
        }

        public ArrayList<LuckMoneyMode> getLuckMoneyModes() {
            return this.mLuckMoneyMode;
        }

        public LuckMoneyMode getLuckMoneyMode(int defaultMode) {
            if (defaultMode != -1) {
                this.mDefaultMode = defaultMode;
            }
            LuckMoneyMode result = null;
            Iterator<LuckMoneyMode> it = this.mLuckMoneyMode.iterator();
            while (it.hasNext()) {
                LuckMoneyMode temp = it.next();
                if (defaultMode == -1) {
                    if (temp.getId() == this.mDefaultMode) {
                        result = temp;
                    }
                } else if (temp.getId() >= defaultMode && temp.getModeEnable()) {
                    return temp;
                }
            }
            return result;
        }
    }

    public class LuckMoneyMode {
        int mEnable;
        ArrayList<String> mHbHashs;
        ArrayList<Integer> mHbHeights;
        String mHbLayout;
        String mHbLayoutNodes;
        String mHbText;
        ArrayList<Integer> mHbWidths;
        int mId;

        public LuckMoneyMode(int id, int enable, ArrayList<Integer> hbHeights, ArrayList<Integer> hbWidths, ArrayList<String> hbHashs, String hbText, String hbLayout, String hbLayoutNodes) {
            this.mId = id;
            this.mEnable = enable;
            this.mHbHeights = hbHeights;
            this.mHbWidths = hbWidths;
            this.mHbHashs = hbHashs;
            this.mHbText = hbText;
            this.mHbLayout = hbLayout;
            this.mHbLayoutNodes = hbLayoutNodes;
        }

        public int getId() {
            return this.mId;
        }

        public boolean getModeEnable() {
            return this.mEnable == 1;
        }

        public ArrayList<Integer> getHbHeight() {
            return this.mHbHeights;
        }

        public ArrayList<Integer> getHbWidth() {
            return this.mHbWidths;
        }

        public ArrayList<Integer> getHbLayoutNodes() {
            String[] temp;
            ArrayList<Integer> tempHbLayoutNodes = new ArrayList<>();
            String str = this.mHbLayoutNodes;
            if (str != null) {
                for (String str2 : str.split(SmsManager.REGEX_PREFIX_DELIMITER)) {
                    tempHbLayoutNodes.add(Integer.valueOf(Integer.parseInt(str2)));
                }
            }
            return tempHbLayoutNodes;
        }

        public ArrayList<String> getHbLayout() {
            String[] temp;
            ArrayList<String> tempHbLayout = new ArrayList<>();
            String str = this.mHbLayout;
            if (str != null) {
                for (String str2 : str.split(SmsManager.REGEX_PREFIX_DELIMITER)) {
                    tempHbLayout.add(str2.trim());
                }
            }
            return tempHbLayout;
        }

        public String getHbText() {
            return this.mHbText;
        }

        public ArrayList<String> getHbHash() {
            return this.mHbHashs;
        }
    }

    private class LuckyMoneyUpdateInfo extends RomUpdateHelper.UpdateInfo {
        static final String BOOSTTIMEOUT = "boostTimeout";
        static final String DELAYTIMEOUT = "delayTimeout";
        static final String ISENABLE = "isOpen";
        static final String KEYDNSINFO = "keyDNSinfo";
        static final String KEYURLINFO = "keyURLinfo";
        static final String LUCKYMONEYVERSION = "luckymoney_version";
        static final String QMSG = "Qmsg";
        static final String QTAG = "Qtag";
        static final String SWITCH_MODE = "switch_mode";
        static final String TAG_MM_MODE = "MM_mode";
        static final String TAG_MM_VERSION = "MM_version";
        static final String TAG_MODE_ITEM = "mode";
        static final String TAG_QQ_MODE = "QQ_mode";
        static final String TAG_QQ_VERSION = "QQ_version";
        static final String TAG_VERSION_ITEM = "version";
        static final String VERSIONINFO = "info_version";
        ArrayList<AppItem> appitem = new ArrayList<>();
        int boostTimeout = 0;
        int delayTimeout = 0;
        ArrayMap<Long, Integer> delayTimeoutMap = new ArrayMap<>();
        String keyDNSInfo = null;
        String keyURLInfo = null;
        boolean luckyMoneyIsEnable = true;
        int luckyMoneyXMLVerion = -1;
        ArrayMap<Integer, ArrayMap<String, String>>[] modeMap = {new ArrayMap<>(), new ArrayMap<>()};
        String qmsgInfo = null;
        String qtagInfo = null;
        boolean switchModeEnable = true;
        ArrayList<VersionItem>[] versionList = {new ArrayList<>(), new ArrayList<>()};

        public LuckyMoneyUpdateInfo() {
            super();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:46:0x00af, code lost:
            if (r1 == null) goto L_0x00b2;
         */
        @Override // com.oppo.luckymoney.RomUpdateHelper.UpdateInfo
        public void parseContentFromXML(String content) {
            if (content != null) {
                StringReader strReader = null;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    strReader = new StringReader(content);
                    parser.setInput(strReader);
                    for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                        if (eventType != 0) {
                            if (eventType == 2) {
                                String startName = parser.getName();
                                if (LUCKYMONEYVERSION.equals(startName)) {
                                    parser.next();
                                    try {
                                        this.luckyMoneyXMLVerion = (int) Long.parseLong(parser.getText());
                                    } catch (RuntimeException e) {
                                    }
                                } else {
                                    boolean z = false;
                                    if (ISENABLE.equals(startName)) {
                                        parser.next();
                                        try {
                                            if (Integer.parseInt(parser.getText()) == 1) {
                                                z = true;
                                            }
                                            this.luckyMoneyIsEnable = z;
                                        } catch (RuntimeException e2) {
                                        }
                                    } else if (SWITCH_MODE.equals(startName)) {
                                        parser.next();
                                        try {
                                            if (Integer.parseInt(parser.getText()) == 1) {
                                                z = true;
                                            }
                                            this.switchModeEnable = z;
                                        } catch (RuntimeException e3) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (XmlPullParserException e4) {
                    Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                    e4.printStackTrace();
                } catch (IOException e5) {
                    Log.e(LuckyMoneyHelper.TAG, "IOException " + e5.toString());
                    if (strReader != null) {
                    }
                } catch (Throwable th) {
                    if (strReader != null) {
                        strReader.close();
                    }
                    throw th;
                }
                strReader.close();
                if (getLuckyMoneyXMLVerion() == 1) {
                    parseContentFromXMLInternalV1(content);
                } else {
                    parseContentFromXMLInternal(content);
                }
            }
        }

        /* JADX WARN: Failed to insert an additional move for type inference into block B:25:0x00a5 */
        /* JADX WARN: Failed to insert an additional move for type inference into block B:28:0x00bd */
        /* JADX INFO: Multiple debug info for r9v52 java.util.ArrayList<java.lang.Integer>: [D('openHbActivity' java.util.ArrayList<java.lang.String>), D('hbHeights' java.util.ArrayList<java.lang.Integer>)] */
        /* JADX INFO: Multiple debug info for r8v50 'hbWidths'  java.util.ArrayList<java.lang.Integer>: [D('appType' int), D('hbWidths' java.util.ArrayList<java.lang.Integer>)] */
        /* JADX INFO: Multiple debug info for r9v76 'openHbActivity'  java.util.ArrayList<java.lang.String>: [D('openHbActivity' java.util.ArrayList<java.lang.String>), D('hbHeights' java.util.ArrayList<java.lang.Integer>)] */
        /* JADX INFO: Multiple debug info for r11v35 'versionMap'  java.util.HashMap<java.lang.Integer, java.lang.Integer>: [D('versionMap' java.util.HashMap<java.lang.Integer, java.lang.Integer>), D('valueVersion' int)] */
        /* JADX WARN: Type inference failed for: r11v20, types: [java.lang.String] */
        /* JADX WARN: Type inference failed for: r11v52 */
        /* JADX WARN: Type inference failed for: r11v54 */
        /* JADX WARN: Type inference failed for: r11v56 */
        /* JADX WARN: Type inference failed for: r11v64, types: [java.util.ArrayList] */
        /* JADX WARNING: Removed duplicated region for block: B:388:0x0f66  */
        /* JADX WARNING: Removed duplicated region for block: B:400:0x0fbe  */
        /* JADX WARNING: Removed duplicated region for block: B:405:0x0fe2  */
        private void parseContentFromXMLInternalV1(String content) {
            StringReader strReader;
            Throwable th;
            XmlPullParserException e;
            int appType;
            ArrayList<String> arrayList;
            IOException e2;
            StringReader strReader2;
            String receiverClass;
            ArrayList<String> openHbActivity;
            HashMap<Integer, Integer> versionMap;
            String keyDNSinfo;
            String chatView;
            ArrayList<String> hbHashs;
            int appType2;
            String keyURLinfo;
            int i;
            ArrayList<AppVersionItem> appVersionItems;
            ArrayList<Integer> hbHeights;
            ArrayList<String> hbHashs2;
            int eventType;
            ArrayList<Integer> hbWidths;
            int appType3;
            int eventType2;
            ArrayList<Integer> hbHeights2;
            ArrayList<Integer> hbWidths2;
            int defaultMode;
            String str = DELAYTIMEOUT;
            int appType4 = 0;
            String keyURLinfo2 = null;
            keyURLinfo = null;
            keyURLinfo2 = null;
            String keyURLinfo3 = null;
            String keyDNSinfo2 = null;
            keyDNSinfo = null;
            keyDNSinfo2 = null;
            keyDNSinfo = null;
            keyDNSinfo2 = null;
            keyDNSinfo = null;
            keyDNSinfo2 = null;
            String keyDNSinfo3 = null;
            String chatView2 = null;
            chatView = null;
            chatView2 = null;
            chatView = null;
            chatView2 = null;
            chatView = null;
            chatView2 = null;
            String chatView3 = null;
            String receiverClass2 = null;
            receiverClass2 = null;
            receiverClass2 = null;
            receiverClass2 = null;
            int start = 0;
            start = 0;
            start = 0;
            start = 0;
            start = 0;
            start = 0;
            start = 0;
            int start2 = 0;
            int end = 0;
            end = 0;
            end = 0;
            end = 0;
            end = 0;
            end = 0;
            end = 0;
            end = 0;
            int modeId = 0;
            modeId = 0;
            modeId = 0;
            modeId = 0;
            modeId = 0;
            modeId = 0;
            modeId = 0;
            int modeId2 = 0;
            int modeEnable = 1;
            modeEnable = 1;
            modeEnable = 1;
            modeEnable = 1;
            modeEnable = 1;
            modeEnable = 1;
            modeEnable = 1;
            int modeEnable2 = 1;
            ArrayList<String> hbHashs3 = null;
            hbHashs = null;
            hbHashs3 = null;
            hbHashs = null;
            hbHashs3 = null;
            hbHashs = null;
            hbHashs3 = null;
            ArrayList<String> hbHashs4 = null;
            ArrayList<Integer> hbWidths3 = null;
            hbWidths3 = null;
            hbWidths3 = null;
            hbWidths3 = null;
            hbWidths3 = null;
            hbWidths3 = null;
            hbWidths3 = null;
            hbWidths3 = null;
            ArrayList<String> openHbActivity2 = new ArrayList<>();
            HashMap<Integer, Integer> versionMap2 = new HashMap<>();
            String hbText = null;
            hbText = null;
            hbText = null;
            hbText = null;
            hbText = null;
            hbText = null;
            hbText = null;
            String hbText2 = null;
            String hbLayout = null;
            hbLayout = null;
            hbLayout = null;
            hbLayout = null;
            hbLayout = null;
            hbLayout = null;
            hbLayout = null;
            String hbLayout2 = null;
            String hbLayoutNodes = null;
            hbLayoutNodes = null;
            hbLayoutNodes = null;
            hbLayoutNodes = null;
            hbLayoutNodes = null;
            hbLayoutNodes = null;
            hbLayoutNodes = null;
            String hbLayoutNodes2 = null;
            StringReader strReader3 = null;
            strReader = null;
            strReader3 = null;
            strReader = null;
            strReader3 = null;
            StringReader strReader4 = null;
            try {
                XmlPullParser parser = Xml.newPullParser();
                try {
                    try {
                        strReader2 = new StringReader(content);
                    } catch (XmlPullParserException e3) {
                        e = e3;
                        appType4 = 0;
                        keyURLinfo2 = null;
                        try {
                            Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                            e.printStackTrace();
                            if (strReader3 != null) {
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            strReader = strReader3;
                            if (strReader != null) {
                            }
                            throw th;
                        }
                    } catch (IOException e4) {
                        arrayList = openHbActivity2;
                        appType = 0;
                        keyURLinfo3 = null;
                        e2 = e4;
                        try {
                            StringBuilder sb = new StringBuilder();
                            try {
                                sb.append("IOException ");
                                sb.append(e2.toString());
                                Log.e(LuckyMoneyHelper.TAG, sb.toString());
                                if (strReader4 != null) {
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                strReader = strReader4;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            strReader = strReader4;
                            if (strReader != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        strReader = null;
                        if (strReader != null) {
                        }
                        throw th;
                    }
                } catch (XmlPullParserException e5) {
                    e = e5;
                    appType4 = 0;
                    Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                    e.printStackTrace();
                    if (strReader3 != null) {
                    }
                } catch (IOException e6) {
                    arrayList = openHbActivity2;
                    appType = 0;
                    e2 = e6;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("IOException ");
                    sb2.append(e2.toString());
                    Log.e(LuckyMoneyHelper.TAG, sb2.toString());
                    if (strReader4 != null) {
                    }
                } catch (Throwable th6) {
                    th = th6;
                    strReader = null;
                    if (strReader != null) {
                    }
                    throw th;
                }
                try {
                    parser.setInput(strReader2);
                    strReader = strReader2;
                    int modeId3 = 0;
                    int modeEnable3 = 1;
                    ArrayList<AppVersionItem> appVersionItems2 = null;
                    ArrayList<String> hbHashs5 = null;
                    ArrayList<Integer> hbWidths4 = null;
                    ArrayList<Integer> hbHeights3 = null;
                    String hbText3 = null;
                    String hbLayout3 = null;
                    String hbLayoutNodes3 = null;
                    int defaultMode2 = 0;
                    int end2 = 0;
                    ArrayList<LuckMoneyMode> luckMoneyMode = null;
                    String keyDNSinfo4 = null;
                    String chatView4 = null;
                    int defaultMode3 = 0;
                    int eventType3 = parser.getEventType();
                    String keyURLinfo4 = null;
                    String receiverClass3 = null;
                    appType = 0;
                    while (true) {
                        String str2 = str;
                        if (eventType3 != 1) {
                            if (eventType3 != 0) {
                                receiverClass = receiverClass3;
                                ? r11 = "MM";
                                chatView = chatView4;
                                keyDNSinfo = keyDNSinfo4;
                                if (eventType3 == 2) {
                                    try {
                                        String startName = parser.getName();
                                        appVersionItems = appVersionItems2;
                                        try {
                                            Log.d(LuckyMoneyHelper.TAG, startName);
                                            if (r11.equals(startName)) {
                                                try {
                                                    appType = 0;
                                                    appVersionItems2 = new ArrayList<>();
                                                    openHbActivity = openHbActivity2;
                                                    versionMap = versionMap2;
                                                    chatView4 = chatView;
                                                    keyDNSinfo4 = keyDNSinfo;
                                                    eventType3 = parser.next();
                                                    versionMap2 = versionMap;
                                                    openHbActivity2 = openHbActivity;
                                                    str = str2;
                                                    receiverClass3 = receiverClass;
                                                } catch (XmlPullParserException e7) {
                                                    e = e7;
                                                    keyURLinfo2 = keyURLinfo4;
                                                    appType4 = appType;
                                                    start = defaultMode2;
                                                    end = end2;
                                                    modeId = modeId3;
                                                    modeEnable = modeEnable3;
                                                    strReader3 = strReader;
                                                    hbHashs3 = hbHashs5;
                                                    hbWidths3 = hbWidths4;
                                                    hbText = hbText3;
                                                    hbLayout = hbLayout3;
                                                    hbLayoutNodes = hbLayoutNodes3;
                                                    receiverClass2 = receiverClass;
                                                    chatView2 = chatView;
                                                    keyDNSinfo2 = keyDNSinfo;
                                                    Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                    e.printStackTrace();
                                                    if (strReader3 != null) {
                                                    }
                                                } catch (IOException e8) {
                                                    e2 = e8;
                                                    keyURLinfo3 = keyURLinfo4;
                                                    start2 = defaultMode2;
                                                    end = end2;
                                                    arrayList = openHbActivity2;
                                                    modeId2 = modeId3;
                                                    modeEnable2 = modeEnable3;
                                                    strReader4 = strReader;
                                                    hbHashs4 = hbHashs5;
                                                    hbWidths3 = hbWidths4;
                                                    hbText2 = hbText3;
                                                    hbLayout2 = hbLayout3;
                                                    hbLayoutNodes2 = hbLayoutNodes3;
                                                    chatView3 = chatView;
                                                    keyDNSinfo3 = keyDNSinfo;
                                                    StringBuilder sb22 = new StringBuilder();
                                                    sb22.append("IOException ");
                                                    sb22.append(e2.toString());
                                                    Log.e(LuckyMoneyHelper.TAG, sb22.toString());
                                                    if (strReader4 != null) {
                                                    }
                                                } catch (Throwable th7) {
                                                    th = th7;
                                                    if (strReader != null) {
                                                    }
                                                    throw th;
                                                }
                                            } else {
                                                if (Contacts.ContactMethods.ProviderNames.QQ.equals(startName)) {
                                                    appType = 1;
                                                    appVersionItems2 = new ArrayList<>();
                                                    openHbActivity = openHbActivity2;
                                                    versionMap = versionMap2;
                                                    chatView4 = chatView;
                                                    keyDNSinfo4 = keyDNSinfo;
                                                } else if ("entry".equals(startName)) {
                                                    try {
                                                        int mmVersion = Integer.parseInt(parser.getAttributeValue(null, "version"));
                                                        int overseaVersion = Integer.parseInt(parser.getAttributeValue(null, "oversea"));
                                                        parser.next();
                                                        int valueVersion = LuckyMoneyHelper.IS_OVERSEA_VERSION ? overseaVersion : Integer.parseInt(parser.getText());
                                                        Integer valueOf = Integer.valueOf(mmVersion);
                                                        Integer valueOf2 = Integer.valueOf(valueVersion);
                                                        versionMap = versionMap2;
                                                        try {
                                                            versionMap.put(valueOf, valueOf2);
                                                            appVersionItems2 = appVersionItems;
                                                            openHbActivity = openHbActivity2;
                                                            chatView4 = chatView;
                                                            keyDNSinfo4 = keyDNSinfo;
                                                        } catch (XmlPullParserException e9) {
                                                            e = e9;
                                                            keyURLinfo2 = keyURLinfo4;
                                                            appType4 = appType;
                                                            start = defaultMode2;
                                                            end = end2;
                                                            modeId = modeId3;
                                                            modeEnable = modeEnable3;
                                                            strReader3 = strReader;
                                                            hbHashs3 = hbHashs5;
                                                            hbWidths3 = hbWidths4;
                                                            hbText = hbText3;
                                                            hbLayout = hbLayout3;
                                                            hbLayoutNodes = hbLayoutNodes3;
                                                            receiverClass2 = receiverClass;
                                                            chatView2 = chatView;
                                                            keyDNSinfo2 = keyDNSinfo;
                                                            Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                            e.printStackTrace();
                                                            if (strReader3 != null) {
                                                            }
                                                        } catch (IOException e10) {
                                                            e2 = e10;
                                                            keyURLinfo3 = keyURLinfo4;
                                                            start2 = defaultMode2;
                                                            end = end2;
                                                            arrayList = openHbActivity2;
                                                            modeId2 = modeId3;
                                                            modeEnable2 = modeEnable3;
                                                            strReader4 = strReader;
                                                            hbHashs4 = hbHashs5;
                                                            hbWidths3 = hbWidths4;
                                                            hbText2 = hbText3;
                                                            hbLayout2 = hbLayout3;
                                                            hbLayoutNodes2 = hbLayoutNodes3;
                                                            chatView3 = chatView;
                                                            keyDNSinfo3 = keyDNSinfo;
                                                            StringBuilder sb222 = new StringBuilder();
                                                            sb222.append("IOException ");
                                                            sb222.append(e2.toString());
                                                            Log.e(LuckyMoneyHelper.TAG, sb222.toString());
                                                            if (strReader4 != null) {
                                                            }
                                                        } catch (Throwable th8) {
                                                            th = th8;
                                                            if (strReader != null) {
                                                            }
                                                            throw th;
                                                        }
                                                    } catch (XmlPullParserException e11) {
                                                        e = e11;
                                                        keyURLinfo2 = keyURLinfo4;
                                                        appType4 = appType;
                                                        start = defaultMode2;
                                                        end = end2;
                                                        modeId = modeId3;
                                                        modeEnable = modeEnable3;
                                                        strReader3 = strReader;
                                                        hbHashs3 = hbHashs5;
                                                        hbWidths3 = hbWidths4;
                                                        hbText = hbText3;
                                                        hbLayout = hbLayout3;
                                                        hbLayoutNodes = hbLayoutNodes3;
                                                        receiverClass2 = receiverClass;
                                                        chatView2 = chatView;
                                                        keyDNSinfo2 = keyDNSinfo;
                                                        Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                        e.printStackTrace();
                                                        if (strReader3 != null) {
                                                        }
                                                    } catch (IOException e12) {
                                                        e2 = e12;
                                                        keyURLinfo3 = keyURLinfo4;
                                                        start2 = defaultMode2;
                                                        end = end2;
                                                        arrayList = openHbActivity2;
                                                        modeId2 = modeId3;
                                                        modeEnable2 = modeEnable3;
                                                        strReader4 = strReader;
                                                        hbHashs4 = hbHashs5;
                                                        hbWidths3 = hbWidths4;
                                                        hbText2 = hbText3;
                                                        hbLayout2 = hbLayout3;
                                                        hbLayoutNodes2 = hbLayoutNodes3;
                                                        chatView3 = chatView;
                                                        keyDNSinfo3 = keyDNSinfo;
                                                        StringBuilder sb2222 = new StringBuilder();
                                                        sb2222.append("IOException ");
                                                        sb2222.append(e2.toString());
                                                        Log.e(LuckyMoneyHelper.TAG, sb2222.toString());
                                                        if (strReader4 != null) {
                                                        }
                                                    } catch (Throwable th9) {
                                                        th = th9;
                                                        if (strReader != null) {
                                                        }
                                                        throw th;
                                                    }
                                                } else {
                                                    versionMap = versionMap2;
                                                    try {
                                                        if (VERSIONINFO.equals(startName)) {
                                                            parser.next();
                                                            try {
                                                                this.mVersion = Long.parseLong(parser.getText());
                                                            } catch (RuntimeException e13) {
                                                            }
                                                            appVersionItems2 = appVersionItems;
                                                            openHbActivity = openHbActivity2;
                                                            chatView4 = chatView;
                                                            keyDNSinfo4 = keyDNSinfo;
                                                        } else if (KEYURLINFO.equals(startName)) {
                                                            parser.next();
                                                            keyURLinfo4 = parser.getText();
                                                            appVersionItems2 = appVersionItems;
                                                            openHbActivity = openHbActivity2;
                                                            chatView4 = chatView;
                                                            keyDNSinfo4 = keyDNSinfo;
                                                        } else if (KEYDNSINFO.equals(startName)) {
                                                            parser.next();
                                                            keyDNSinfo4 = parser.getText();
                                                            appVersionItems2 = appVersionItems;
                                                            openHbActivity = openHbActivity2;
                                                            chatView4 = chatView;
                                                        } else if ("chatView".equals(startName)) {
                                                            parser.next();
                                                            chatView4 = parser.getText();
                                                            appVersionItems2 = appVersionItems;
                                                            openHbActivity = openHbActivity2;
                                                            keyDNSinfo4 = keyDNSinfo;
                                                        } else if ("receiverClass".equals(startName)) {
                                                            parser.next();
                                                            receiverClass = parser.getText();
                                                            appVersionItems2 = appVersionItems;
                                                            openHbActivity = openHbActivity2;
                                                            chatView4 = chatView;
                                                            keyDNSinfo4 = keyDNSinfo;
                                                        } else if ("openHbActivity".equals(startName)) {
                                                            try {
                                                                parser.next();
                                                                try {
                                                                    openHbActivity2.add(parser.getText());
                                                                    openHbActivity = openHbActivity2;
                                                                    appVersionItems2 = appVersionItems;
                                                                    chatView4 = chatView;
                                                                    keyDNSinfo4 = keyDNSinfo;
                                                                } catch (XmlPullParserException e14) {
                                                                    e = e14;
                                                                    keyURLinfo2 = keyURLinfo4;
                                                                    appType4 = appType;
                                                                    start = defaultMode2;
                                                                    end = end2;
                                                                    modeId = modeId3;
                                                                    modeEnable = modeEnable3;
                                                                    strReader3 = strReader;
                                                                    hbHashs3 = hbHashs5;
                                                                    hbWidths3 = hbWidths4;
                                                                    hbText = hbText3;
                                                                    hbLayout = hbLayout3;
                                                                    hbLayoutNodes = hbLayoutNodes3;
                                                                    receiverClass2 = receiverClass;
                                                                    chatView2 = chatView;
                                                                    keyDNSinfo2 = keyDNSinfo;
                                                                    Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                    e.printStackTrace();
                                                                    if (strReader3 != null) {
                                                                    }
                                                                } catch (IOException e15) {
                                                                    e2 = e15;
                                                                    keyURLinfo3 = keyURLinfo4;
                                                                    arrayList = openHbActivity2;
                                                                    start2 = defaultMode2;
                                                                    end = end2;
                                                                    modeId2 = modeId3;
                                                                    modeEnable2 = modeEnable3;
                                                                    strReader4 = strReader;
                                                                    hbHashs4 = hbHashs5;
                                                                    hbWidths3 = hbWidths4;
                                                                    hbText2 = hbText3;
                                                                    hbLayout2 = hbLayout3;
                                                                    hbLayoutNodes2 = hbLayoutNodes3;
                                                                    chatView3 = chatView;
                                                                    keyDNSinfo3 = keyDNSinfo;
                                                                    StringBuilder sb22222 = new StringBuilder();
                                                                    sb22222.append("IOException ");
                                                                    sb22222.append(e2.toString());
                                                                    Log.e(LuckyMoneyHelper.TAG, sb22222.toString());
                                                                    if (strReader4 != null) {
                                                                    }
                                                                } catch (Throwable th10) {
                                                                    th = th10;
                                                                    if (strReader != null) {
                                                                    }
                                                                    throw th;
                                                                }
                                                            } catch (XmlPullParserException e16) {
                                                                e = e16;
                                                                keyURLinfo2 = keyURLinfo4;
                                                                appType4 = appType;
                                                                start = defaultMode2;
                                                                end = end2;
                                                                modeId = modeId3;
                                                                modeEnable = modeEnable3;
                                                                strReader3 = strReader;
                                                                hbHashs3 = hbHashs5;
                                                                hbWidths3 = hbWidths4;
                                                                hbText = hbText3;
                                                                hbLayout = hbLayout3;
                                                                hbLayoutNodes = hbLayoutNodes3;
                                                                receiverClass2 = receiverClass;
                                                                chatView2 = chatView;
                                                                keyDNSinfo2 = keyDNSinfo;
                                                                Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                e.printStackTrace();
                                                                if (strReader3 != null) {
                                                                }
                                                            } catch (IOException e17) {
                                                                e2 = e17;
                                                                keyURLinfo3 = keyURLinfo4;
                                                                start2 = defaultMode2;
                                                                end = end2;
                                                                arrayList = openHbActivity2;
                                                                modeId2 = modeId3;
                                                                modeEnable2 = modeEnable3;
                                                                strReader4 = strReader;
                                                                hbHashs4 = hbHashs5;
                                                                hbWidths3 = hbWidths4;
                                                                hbText2 = hbText3;
                                                                hbLayout2 = hbLayout3;
                                                                hbLayoutNodes2 = hbLayoutNodes3;
                                                                chatView3 = chatView;
                                                                keyDNSinfo3 = keyDNSinfo;
                                                                StringBuilder sb222222 = new StringBuilder();
                                                                sb222222.append("IOException ");
                                                                sb222222.append(e2.toString());
                                                                Log.e(LuckyMoneyHelper.TAG, sb222222.toString());
                                                                if (strReader4 != null) {
                                                                }
                                                            } catch (Throwable th11) {
                                                                th = th11;
                                                                if (strReader != null) {
                                                                }
                                                                throw th;
                                                            }
                                                        } else {
                                                            try {
                                                                if (TAG_MM_VERSION.equals(startName)) {
                                                                    int start3 = Integer.parseInt(parser.getAttributeValue(null, "start"));
                                                                    try {
                                                                        int end3 = Integer.parseInt(parser.getAttributeValue(null, "end"));
                                                                        try {
                                                                            start3 = versionMap.getOrDefault(Integer.valueOf(start3), Integer.valueOf(start3)).intValue();
                                                                            end = versionMap.getOrDefault(Integer.valueOf(end3), Integer.valueOf(end3)).intValue();
                                                                            defaultMode = Integer.parseInt(parser.getAttributeValue(null, "defaultmode"));
                                                                        } catch (XmlPullParserException e18) {
                                                                            e = e18;
                                                                            keyURLinfo2 = keyURLinfo4;
                                                                            appType4 = appType;
                                                                            modeId = modeId3;
                                                                            modeEnable = modeEnable3;
                                                                            strReader3 = strReader;
                                                                            hbHashs3 = hbHashs5;
                                                                            hbWidths3 = hbWidths4;
                                                                            hbText = hbText3;
                                                                            hbLayout = hbLayout3;
                                                                            hbLayoutNodes = hbLayoutNodes3;
                                                                            receiverClass2 = receiverClass;
                                                                            chatView2 = chatView;
                                                                            keyDNSinfo2 = keyDNSinfo;
                                                                            start = start3;
                                                                            Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                            e.printStackTrace();
                                                                            if (strReader3 != null) {
                                                                            }
                                                                        } catch (IOException e19) {
                                                                            keyURLinfo3 = keyURLinfo4;
                                                                            arrayList = openHbActivity2;
                                                                            modeId2 = modeId3;
                                                                            modeEnable2 = modeEnable3;
                                                                            strReader4 = strReader;
                                                                            hbHashs4 = hbHashs5;
                                                                            hbWidths3 = hbWidths4;
                                                                            hbText2 = hbText3;
                                                                            hbLayout2 = hbLayout3;
                                                                            hbLayoutNodes2 = hbLayoutNodes3;
                                                                            chatView3 = chatView;
                                                                            keyDNSinfo3 = keyDNSinfo;
                                                                            start2 = start3;
                                                                            e2 = e19;
                                                                            StringBuilder sb2222222 = new StringBuilder();
                                                                            sb2222222.append("IOException ");
                                                                            sb2222222.append(e2.toString());
                                                                            Log.e(LuckyMoneyHelper.TAG, sb2222222.toString());
                                                                            if (strReader4 != null) {
                                                                            }
                                                                        } catch (Throwable th12) {
                                                                            th = th12;
                                                                            if (strReader != null) {
                                                                            }
                                                                            throw th;
                                                                        }
                                                                    } catch (XmlPullParserException e20) {
                                                                        e = e20;
                                                                        keyURLinfo2 = keyURLinfo4;
                                                                        appType4 = appType;
                                                                        end = end2;
                                                                        modeId = modeId3;
                                                                        modeEnable = modeEnable3;
                                                                        strReader3 = strReader;
                                                                        hbHashs3 = hbHashs5;
                                                                        hbWidths3 = hbWidths4;
                                                                        hbText = hbText3;
                                                                        hbLayout = hbLayout3;
                                                                        hbLayoutNodes = hbLayoutNodes3;
                                                                        receiverClass2 = receiverClass;
                                                                        chatView2 = chatView;
                                                                        keyDNSinfo2 = keyDNSinfo;
                                                                        start = start3;
                                                                        Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                        e.printStackTrace();
                                                                        if (strReader3 != null) {
                                                                            strReader3.close();
                                                                        }
                                                                    } catch (IOException e21) {
                                                                        keyURLinfo3 = keyURLinfo4;
                                                                        arrayList = openHbActivity2;
                                                                        end = end2;
                                                                        modeId2 = modeId3;
                                                                        modeEnable2 = modeEnable3;
                                                                        strReader4 = strReader;
                                                                        hbHashs4 = hbHashs5;
                                                                        hbWidths3 = hbWidths4;
                                                                        hbText2 = hbText3;
                                                                        hbLayout2 = hbLayout3;
                                                                        hbLayoutNodes2 = hbLayoutNodes3;
                                                                        chatView3 = chatView;
                                                                        keyDNSinfo3 = keyDNSinfo;
                                                                        start2 = start3;
                                                                        e2 = e21;
                                                                        StringBuilder sb22222222 = new StringBuilder();
                                                                        sb22222222.append("IOException ");
                                                                        sb22222222.append(e2.toString());
                                                                        Log.e(LuckyMoneyHelper.TAG, sb22222222.toString());
                                                                        if (strReader4 != null) {
                                                                            strReader4.close();
                                                                        }
                                                                    } catch (Throwable th13) {
                                                                        th = th13;
                                                                        if (strReader != null) {
                                                                            strReader.close();
                                                                        }
                                                                        throw th;
                                                                    }
                                                                    try {
                                                                        openHbActivity = openHbActivity2;
                                                                        defaultMode3 = defaultMode;
                                                                        luckMoneyMode = new ArrayList<>();
                                                                        appVersionItems2 = appVersionItems;
                                                                        keyDNSinfo4 = keyDNSinfo;
                                                                        defaultMode2 = start3;
                                                                        end2 = end;
                                                                        chatView4 = chatView;
                                                                    } catch (XmlPullParserException e22) {
                                                                        e = e22;
                                                                        keyURLinfo2 = keyURLinfo4;
                                                                        appType4 = appType;
                                                                        modeId = modeId3;
                                                                        modeEnable = modeEnable3;
                                                                        strReader3 = strReader;
                                                                        hbHashs3 = hbHashs5;
                                                                        hbWidths3 = hbWidths4;
                                                                        hbText = hbText3;
                                                                        hbLayout = hbLayout3;
                                                                        hbLayoutNodes = hbLayoutNodes3;
                                                                        receiverClass2 = receiverClass;
                                                                        chatView2 = chatView;
                                                                        keyDNSinfo2 = keyDNSinfo;
                                                                        start = start3;
                                                                        Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                        e.printStackTrace();
                                                                        if (strReader3 != null) {
                                                                        }
                                                                    } catch (IOException e23) {
                                                                        keyURLinfo3 = keyURLinfo4;
                                                                        arrayList = openHbActivity2;
                                                                        modeId2 = modeId3;
                                                                        modeEnable2 = modeEnable3;
                                                                        strReader4 = strReader;
                                                                        hbHashs4 = hbHashs5;
                                                                        hbWidths3 = hbWidths4;
                                                                        hbText2 = hbText3;
                                                                        hbLayout2 = hbLayout3;
                                                                        hbLayoutNodes2 = hbLayoutNodes3;
                                                                        chatView3 = chatView;
                                                                        keyDNSinfo3 = keyDNSinfo;
                                                                        start2 = start3;
                                                                        e2 = e23;
                                                                        StringBuilder sb222222222 = new StringBuilder();
                                                                        sb222222222.append("IOException ");
                                                                        sb222222222.append(e2.toString());
                                                                        Log.e(LuckyMoneyHelper.TAG, sb222222222.toString());
                                                                        if (strReader4 != null) {
                                                                        }
                                                                    } catch (Throwable th14) {
                                                                        th = th14;
                                                                        if (strReader != null) {
                                                                        }
                                                                        throw th;
                                                                    }
                                                                } else if ("Mode".equals(startName)) {
                                                                    int modeId4 = Integer.parseInt(parser.getAttributeValue(null, "id"));
                                                                    try {
                                                                        int modeEnable4 = Integer.parseInt(parser.getAttributeValue(null, "enable"));
                                                                        try {
                                                                            ArrayList<String> hbHashs6 = new ArrayList<>();
                                                                            try {
                                                                                hbWidths3 = new ArrayList<>();
                                                                                try {
                                                                                    modeEnable3 = modeEnable4;
                                                                                    openHbActivity = openHbActivity2;
                                                                                    hbHashs5 = hbHashs6;
                                                                                    modeId3 = modeId4;
                                                                                    hbWidths4 = hbWidths3;
                                                                                    appVersionItems2 = appVersionItems;
                                                                                    hbHeights3 = new ArrayList<>();
                                                                                    chatView4 = chatView;
                                                                                    keyDNSinfo4 = keyDNSinfo;
                                                                                } catch (XmlPullParserException e24) {
                                                                                    e = e24;
                                                                                    keyURLinfo2 = keyURLinfo4;
                                                                                    appType4 = appType;
                                                                                    start = defaultMode2;
                                                                                    modeId = modeId4;
                                                                                    strReader3 = strReader;
                                                                                    hbText = hbText3;
                                                                                    hbLayout = hbLayout3;
                                                                                    hbLayoutNodes = hbLayoutNodes3;
                                                                                    receiverClass2 = receiverClass;
                                                                                    chatView2 = chatView;
                                                                                    keyDNSinfo2 = keyDNSinfo;
                                                                                    hbHashs3 = hbHashs6;
                                                                                    end = end2;
                                                                                    modeEnable = modeEnable4;
                                                                                    Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                                    e.printStackTrace();
                                                                                    if (strReader3 != null) {
                                                                                    }
                                                                                } catch (IOException e25) {
                                                                                    keyURLinfo3 = keyURLinfo4;
                                                                                    arrayList = openHbActivity2;
                                                                                    start2 = defaultMode2;
                                                                                    modeId2 = modeId4;
                                                                                    strReader4 = strReader;
                                                                                    hbText2 = hbText3;
                                                                                    hbLayout2 = hbLayout3;
                                                                                    hbLayoutNodes2 = hbLayoutNodes3;
                                                                                    chatView3 = chatView;
                                                                                    keyDNSinfo3 = keyDNSinfo;
                                                                                    hbHashs4 = hbHashs6;
                                                                                    end = end2;
                                                                                    modeEnable2 = modeEnable4;
                                                                                    e2 = e25;
                                                                                    StringBuilder sb2222222222 = new StringBuilder();
                                                                                    sb2222222222.append("IOException ");
                                                                                    sb2222222222.append(e2.toString());
                                                                                    Log.e(LuckyMoneyHelper.TAG, sb2222222222.toString());
                                                                                    if (strReader4 != null) {
                                                                                    }
                                                                                } catch (Throwable th15) {
                                                                                    th = th15;
                                                                                    if (strReader != null) {
                                                                                    }
                                                                                    throw th;
                                                                                }
                                                                            } catch (XmlPullParserException e26) {
                                                                                e = e26;
                                                                                keyURLinfo2 = keyURLinfo4;
                                                                                appType4 = appType;
                                                                                start = defaultMode2;
                                                                                modeId = modeId4;
                                                                                strReader3 = strReader;
                                                                                hbWidths3 = hbWidths4;
                                                                                hbText = hbText3;
                                                                                hbLayout = hbLayout3;
                                                                                hbLayoutNodes = hbLayoutNodes3;
                                                                                receiverClass2 = receiverClass;
                                                                                chatView2 = chatView;
                                                                                keyDNSinfo2 = keyDNSinfo;
                                                                                hbHashs3 = hbHashs6;
                                                                                end = end2;
                                                                                modeEnable = modeEnable4;
                                                                                Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                                e.printStackTrace();
                                                                                if (strReader3 != null) {
                                                                                }
                                                                            } catch (IOException e27) {
                                                                                keyURLinfo3 = keyURLinfo4;
                                                                                arrayList = openHbActivity2;
                                                                                start2 = defaultMode2;
                                                                                modeId2 = modeId4;
                                                                                strReader4 = strReader;
                                                                                hbWidths3 = hbWidths4;
                                                                                hbText2 = hbText3;
                                                                                hbLayout2 = hbLayout3;
                                                                                hbLayoutNodes2 = hbLayoutNodes3;
                                                                                chatView3 = chatView;
                                                                                keyDNSinfo3 = keyDNSinfo;
                                                                                hbHashs4 = hbHashs6;
                                                                                end = end2;
                                                                                modeEnable2 = modeEnable4;
                                                                                e2 = e27;
                                                                                StringBuilder sb22222222222 = new StringBuilder();
                                                                                sb22222222222.append("IOException ");
                                                                                sb22222222222.append(e2.toString());
                                                                                Log.e(LuckyMoneyHelper.TAG, sb22222222222.toString());
                                                                                if (strReader4 != null) {
                                                                                }
                                                                            } catch (Throwable th16) {
                                                                                th = th16;
                                                                                if (strReader != null) {
                                                                                }
                                                                                throw th;
                                                                            }
                                                                        } catch (XmlPullParserException e28) {
                                                                            e = e28;
                                                                            keyURLinfo2 = keyURLinfo4;
                                                                            appType4 = appType;
                                                                            start = defaultMode2;
                                                                            end = end2;
                                                                            modeId = modeId4;
                                                                            strReader3 = strReader;
                                                                            hbHashs3 = hbHashs5;
                                                                            hbWidths3 = hbWidths4;
                                                                            hbText = hbText3;
                                                                            hbLayout = hbLayout3;
                                                                            hbLayoutNodes = hbLayoutNodes3;
                                                                            receiverClass2 = receiverClass;
                                                                            chatView2 = chatView;
                                                                            keyDNSinfo2 = keyDNSinfo;
                                                                            modeEnable = modeEnable4;
                                                                            Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                            e.printStackTrace();
                                                                            if (strReader3 != null) {
                                                                            }
                                                                        } catch (IOException e29) {
                                                                            keyURLinfo3 = keyURLinfo4;
                                                                            arrayList = openHbActivity2;
                                                                            start2 = defaultMode2;
                                                                            end = end2;
                                                                            modeId2 = modeId4;
                                                                            strReader4 = strReader;
                                                                            hbHashs4 = hbHashs5;
                                                                            hbWidths3 = hbWidths4;
                                                                            hbText2 = hbText3;
                                                                            hbLayout2 = hbLayout3;
                                                                            hbLayoutNodes2 = hbLayoutNodes3;
                                                                            chatView3 = chatView;
                                                                            keyDNSinfo3 = keyDNSinfo;
                                                                            modeEnable2 = modeEnable4;
                                                                            e2 = e29;
                                                                            StringBuilder sb222222222222 = new StringBuilder();
                                                                            sb222222222222.append("IOException ");
                                                                            sb222222222222.append(e2.toString());
                                                                            Log.e(LuckyMoneyHelper.TAG, sb222222222222.toString());
                                                                            if (strReader4 != null) {
                                                                            }
                                                                        } catch (Throwable th17) {
                                                                            th = th17;
                                                                            if (strReader != null) {
                                                                            }
                                                                            throw th;
                                                                        }
                                                                    } catch (XmlPullParserException e30) {
                                                                        e = e30;
                                                                        keyURLinfo2 = keyURLinfo4;
                                                                        appType4 = appType;
                                                                        start = defaultMode2;
                                                                        end = end2;
                                                                        modeId = modeId4;
                                                                        modeEnable = modeEnable3;
                                                                        strReader3 = strReader;
                                                                        hbHashs3 = hbHashs5;
                                                                        hbWidths3 = hbWidths4;
                                                                        hbText = hbText3;
                                                                        hbLayout = hbLayout3;
                                                                        hbLayoutNodes = hbLayoutNodes3;
                                                                        receiverClass2 = receiverClass;
                                                                        chatView2 = chatView;
                                                                        keyDNSinfo2 = keyDNSinfo;
                                                                        Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                        e.printStackTrace();
                                                                        if (strReader3 != null) {
                                                                        }
                                                                    } catch (IOException e31) {
                                                                        e2 = e31;
                                                                        keyURLinfo3 = keyURLinfo4;
                                                                        arrayList = openHbActivity2;
                                                                        start2 = defaultMode2;
                                                                        end = end2;
                                                                        modeId2 = modeId4;
                                                                        modeEnable2 = modeEnable3;
                                                                        strReader4 = strReader;
                                                                        hbHashs4 = hbHashs5;
                                                                        hbWidths3 = hbWidths4;
                                                                        hbText2 = hbText3;
                                                                        hbLayout2 = hbLayout3;
                                                                        hbLayoutNodes2 = hbLayoutNodes3;
                                                                        chatView3 = chatView;
                                                                        keyDNSinfo3 = keyDNSinfo;
                                                                        StringBuilder sb2222222222222 = new StringBuilder();
                                                                        sb2222222222222.append("IOException ");
                                                                        sb2222222222222.append(e2.toString());
                                                                        Log.e(LuckyMoneyHelper.TAG, sb2222222222222.toString());
                                                                        if (strReader4 != null) {
                                                                        }
                                                                    } catch (Throwable th18) {
                                                                        th = th18;
                                                                        if (strReader != null) {
                                                                        }
                                                                        throw th;
                                                                    }
                                                                } else if ("hb_hash".equals(startName)) {
                                                                    try {
                                                                        int width = Integer.parseInt(parser.getAttributeValue(null, "width"));
                                                                        int height = Integer.parseInt(parser.getAttributeValue(null, "height"));
                                                                        int eventType4 = parser.next();
                                                                        if (hbHashs5 != null) {
                                                                            appType3 = appType;
                                                                            hbWidths2 = hbWidths4;
                                                                            if (hbWidths2 != null) {
                                                                                openHbActivity = openHbActivity2;
                                                                                hbHeights2 = hbHeights3;
                                                                                if (hbHeights2 != null) {
                                                                                    eventType2 = eventType4;
                                                                                    try {
                                                                                        hbHashs5.add(parser.getText());
                                                                                        hbWidths2.add(Integer.valueOf(width));
                                                                                        hbHeights2.add(Integer.valueOf(height));
                                                                                        hbHashs5 = hbHashs5;
                                                                                        hbWidths4 = hbWidths2;
                                                                                        hbHeights3 = hbHeights2;
                                                                                        appVersionItems2 = appVersionItems;
                                                                                        keyURLinfo4 = keyURLinfo4;
                                                                                        appType = appType3;
                                                                                        chatView4 = chatView;
                                                                                        keyDNSinfo4 = keyDNSinfo;
                                                                                    } catch (XmlPullParserException e32) {
                                                                                        e = e32;
                                                                                        hbHashs3 = hbHashs5;
                                                                                        hbWidths3 = hbWidths2;
                                                                                        start = defaultMode2;
                                                                                        end = end2;
                                                                                        keyURLinfo2 = keyURLinfo4;
                                                                                        appType4 = appType3;
                                                                                        modeId = modeId3;
                                                                                        modeEnable = modeEnable3;
                                                                                        strReader3 = strReader;
                                                                                        hbText = hbText3;
                                                                                        hbLayout = hbLayout3;
                                                                                        hbLayoutNodes = hbLayoutNodes3;
                                                                                        receiverClass2 = receiverClass;
                                                                                        chatView2 = chatView;
                                                                                        keyDNSinfo2 = keyDNSinfo;
                                                                                        Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                                        e.printStackTrace();
                                                                                        if (strReader3 != null) {
                                                                                        }
                                                                                    } catch (IOException e33) {
                                                                                        e2 = e33;
                                                                                        hbHashs4 = hbHashs5;
                                                                                        hbWidths3 = hbWidths2;
                                                                                        start2 = defaultMode2;
                                                                                        end = end2;
                                                                                        keyURLinfo3 = keyURLinfo4;
                                                                                        appType = appType3;
                                                                                        arrayList = openHbActivity;
                                                                                        modeId2 = modeId3;
                                                                                        modeEnable2 = modeEnable3;
                                                                                        strReader4 = strReader;
                                                                                        hbText2 = hbText3;
                                                                                        hbLayout2 = hbLayout3;
                                                                                        hbLayoutNodes2 = hbLayoutNodes3;
                                                                                        chatView3 = chatView;
                                                                                        keyDNSinfo3 = keyDNSinfo;
                                                                                        StringBuilder sb22222222222222 = new StringBuilder();
                                                                                        sb22222222222222.append("IOException ");
                                                                                        sb22222222222222.append(e2.toString());
                                                                                        Log.e(LuckyMoneyHelper.TAG, sb22222222222222.toString());
                                                                                        if (strReader4 != null) {
                                                                                        }
                                                                                    } catch (Throwable th19) {
                                                                                        th = th19;
                                                                                        if (strReader != null) {
                                                                                        }
                                                                                        throw th;
                                                                                    }
                                                                                } else {
                                                                                    eventType2 = eventType4;
                                                                                }
                                                                            } else {
                                                                                openHbActivity = openHbActivity2;
                                                                                eventType2 = eventType4;
                                                                                hbHeights2 = hbHeights3;
                                                                            }
                                                                        } else {
                                                                            appType3 = appType;
                                                                            openHbActivity = openHbActivity2;
                                                                            eventType2 = eventType4;
                                                                            hbWidths2 = hbWidths4;
                                                                            hbHeights2 = hbHeights3;
                                                                        }
                                                                        Log.w(LuckyMoneyHelper.TAG, "End tag Mode didn't start correctly");
                                                                        hbHashs5 = hbHashs5;
                                                                        hbWidths4 = hbWidths2;
                                                                        hbHeights3 = hbHeights2;
                                                                        appVersionItems2 = appVersionItems;
                                                                        keyURLinfo4 = keyURLinfo4;
                                                                        appType = appType3;
                                                                        chatView4 = chatView;
                                                                        keyDNSinfo4 = keyDNSinfo;
                                                                    } catch (XmlPullParserException e34) {
                                                                        e = e34;
                                                                        hbHashs3 = hbHashs5;
                                                                        hbWidths3 = hbWidths4;
                                                                        start = defaultMode2;
                                                                        end = end2;
                                                                        keyURLinfo2 = keyURLinfo4;
                                                                        appType4 = appType;
                                                                        modeId = modeId3;
                                                                        modeEnable = modeEnable3;
                                                                        strReader3 = strReader;
                                                                        hbText = hbText3;
                                                                        hbLayout = hbLayout3;
                                                                        hbLayoutNodes = hbLayoutNodes3;
                                                                        receiverClass2 = receiverClass;
                                                                        chatView2 = chatView;
                                                                        keyDNSinfo2 = keyDNSinfo;
                                                                        Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                        e.printStackTrace();
                                                                        if (strReader3 != null) {
                                                                        }
                                                                    } catch (IOException e35) {
                                                                        e2 = e35;
                                                                        hbHashs4 = hbHashs5;
                                                                        hbWidths3 = hbWidths4;
                                                                        start2 = defaultMode2;
                                                                        end = end2;
                                                                        keyURLinfo3 = keyURLinfo4;
                                                                        appType = appType;
                                                                        arrayList = openHbActivity2;
                                                                        modeId2 = modeId3;
                                                                        modeEnable2 = modeEnable3;
                                                                        strReader4 = strReader;
                                                                        hbText2 = hbText3;
                                                                        hbLayout2 = hbLayout3;
                                                                        hbLayoutNodes2 = hbLayoutNodes3;
                                                                        chatView3 = chatView;
                                                                        keyDNSinfo3 = keyDNSinfo;
                                                                        StringBuilder sb222222222222222 = new StringBuilder();
                                                                        sb222222222222222.append("IOException ");
                                                                        sb222222222222222.append(e2.toString());
                                                                        Log.e(LuckyMoneyHelper.TAG, sb222222222222222.toString());
                                                                        if (strReader4 != null) {
                                                                        }
                                                                    } catch (Throwable th20) {
                                                                        th = th20;
                                                                        if (strReader != null) {
                                                                        }
                                                                        throw th;
                                                                    }
                                                                } else {
                                                                    i = eventType3;
                                                                    keyURLinfo = keyURLinfo4;
                                                                    appType2 = appType;
                                                                    openHbActivity = openHbActivity2;
                                                                    hbHeights = hbHeights3;
                                                                    try {
                                                                        if ("hb_text".equals(startName)) {
                                                                            parser.next();
                                                                            hbText3 = parser.getText();
                                                                            hbHashs5 = hbHashs5;
                                                                            hbWidths4 = hbWidths4;
                                                                            hbHeights3 = hbHeights;
                                                                            appVersionItems2 = appVersionItems;
                                                                            keyURLinfo4 = keyURLinfo;
                                                                            appType = appType2;
                                                                            chatView4 = chatView;
                                                                            keyDNSinfo4 = keyDNSinfo;
                                                                        } else if ("hb_layout".equals(startName)) {
                                                                            String hbLayoutNodes4 = parser.getAttributeValue(null, "nodes");
                                                                            try {
                                                                                parser.next();
                                                                                hbLayoutNodes3 = hbLayoutNodes4;
                                                                                hbHashs5 = hbHashs5;
                                                                                hbWidths4 = hbWidths4;
                                                                                hbHeights3 = hbHeights;
                                                                                hbLayout3 = parser.getText();
                                                                                appVersionItems2 = appVersionItems;
                                                                                keyURLinfo4 = keyURLinfo;
                                                                                appType = appType2;
                                                                                chatView4 = chatView;
                                                                                keyDNSinfo4 = keyDNSinfo;
                                                                            } catch (XmlPullParserException e36) {
                                                                                e = e36;
                                                                                hbHashs3 = hbHashs5;
                                                                                hbWidths3 = hbWidths4;
                                                                                start = defaultMode2;
                                                                                end = end2;
                                                                                keyURLinfo2 = keyURLinfo;
                                                                                appType4 = appType2;
                                                                                modeId = modeId3;
                                                                                modeEnable = modeEnable3;
                                                                                strReader3 = strReader;
                                                                                hbText = hbText3;
                                                                                hbLayout = hbLayout3;
                                                                                receiverClass2 = receiverClass;
                                                                                chatView2 = chatView;
                                                                                keyDNSinfo2 = keyDNSinfo;
                                                                                hbLayoutNodes = hbLayoutNodes4;
                                                                                Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                                e.printStackTrace();
                                                                                if (strReader3 != null) {
                                                                                }
                                                                            } catch (IOException e37) {
                                                                                hbHashs4 = hbHashs5;
                                                                                hbWidths3 = hbWidths4;
                                                                                start2 = defaultMode2;
                                                                                end = end2;
                                                                                keyURLinfo3 = keyURLinfo;
                                                                                appType = appType2;
                                                                                arrayList = openHbActivity;
                                                                                modeId2 = modeId3;
                                                                                modeEnable2 = modeEnable3;
                                                                                strReader4 = strReader;
                                                                                hbText2 = hbText3;
                                                                                hbLayout2 = hbLayout3;
                                                                                chatView3 = chatView;
                                                                                keyDNSinfo3 = keyDNSinfo;
                                                                                hbLayoutNodes2 = hbLayoutNodes4;
                                                                                e2 = e37;
                                                                                StringBuilder sb2222222222222222 = new StringBuilder();
                                                                                sb2222222222222222.append("IOException ");
                                                                                sb2222222222222222.append(e2.toString());
                                                                                Log.e(LuckyMoneyHelper.TAG, sb2222222222222222.toString());
                                                                                if (strReader4 != null) {
                                                                                }
                                                                            } catch (Throwable th21) {
                                                                                th = th21;
                                                                                if (strReader != null) {
                                                                                }
                                                                                throw th;
                                                                            }
                                                                        } else if (str2.equals(startName)) {
                                                                            parser.next();
                                                                            try {
                                                                                this.delayTimeout = Integer.parseInt(parser.getText());
                                                                            } catch (RuntimeException e38) {
                                                                            }
                                                                            str2 = str2;
                                                                            hbHashs5 = hbHashs5;
                                                                            hbWidths4 = hbWidths4;
                                                                            hbHeights3 = hbHeights;
                                                                            appVersionItems2 = appVersionItems;
                                                                            keyURLinfo4 = keyURLinfo;
                                                                            appType = appType2;
                                                                            chatView4 = chatView;
                                                                            keyDNSinfo4 = keyDNSinfo;
                                                                        } else if (startName == null || !startName.startsWith(str2)) {
                                                                            str2 = str2;
                                                                            hbHashs = hbHashs5;
                                                                            hbWidths3 = hbWidths4;
                                                                            try {
                                                                                if (BOOSTTIMEOUT.equals(startName)) {
                                                                                    parser.next();
                                                                                    try {
                                                                                        this.boostTimeout = Integer.parseInt(parser.getText());
                                                                                    } catch (RuntimeException e39) {
                                                                                    }
                                                                                    hbHeights3 = hbHeights;
                                                                                    hbWidths4 = hbWidths3;
                                                                                    appVersionItems2 = appVersionItems;
                                                                                    keyURLinfo4 = keyURLinfo;
                                                                                    appType = appType2;
                                                                                    hbHashs5 = hbHashs;
                                                                                    chatView4 = chatView;
                                                                                    keyDNSinfo4 = keyDNSinfo;
                                                                                }
                                                                            } catch (XmlPullParserException e40) {
                                                                                e = e40;
                                                                                start = defaultMode2;
                                                                                end = end2;
                                                                                keyURLinfo2 = keyURLinfo;
                                                                                appType4 = appType2;
                                                                                hbHashs3 = hbHashs;
                                                                                modeId = modeId3;
                                                                                modeEnable = modeEnable3;
                                                                                strReader3 = strReader;
                                                                                hbText = hbText3;
                                                                                hbLayout = hbLayout3;
                                                                                hbLayoutNodes = hbLayoutNodes3;
                                                                                receiverClass2 = receiverClass;
                                                                                chatView2 = chatView;
                                                                                keyDNSinfo2 = keyDNSinfo;
                                                                                Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                                e.printStackTrace();
                                                                                if (strReader3 != null) {
                                                                                }
                                                                            } catch (IOException e41) {
                                                                                e2 = e41;
                                                                                start2 = defaultMode2;
                                                                                end = end2;
                                                                                keyURLinfo3 = keyURLinfo;
                                                                                appType = appType2;
                                                                                arrayList = openHbActivity;
                                                                                hbHashs4 = hbHashs;
                                                                                modeId2 = modeId3;
                                                                                modeEnable2 = modeEnable3;
                                                                                strReader4 = strReader;
                                                                                hbText2 = hbText3;
                                                                                hbLayout2 = hbLayout3;
                                                                                hbLayoutNodes2 = hbLayoutNodes3;
                                                                                chatView3 = chatView;
                                                                                keyDNSinfo3 = keyDNSinfo;
                                                                                StringBuilder sb22222222222222222 = new StringBuilder();
                                                                                sb22222222222222222.append("IOException ");
                                                                                sb22222222222222222.append(e2.toString());
                                                                                Log.e(LuckyMoneyHelper.TAG, sb22222222222222222.toString());
                                                                                if (strReader4 != null) {
                                                                                }
                                                                            } catch (Throwable th22) {
                                                                                th = th22;
                                                                                if (strReader != null) {
                                                                                }
                                                                                throw th;
                                                                            }
                                                                        } else {
                                                                            int eventType5 = parser.next();
                                                                            try {
                                                                                int currentDelayTimeout = Integer.parseInt(parser.getText());
                                                                                str2 = str2;
                                                                                try {
                                                                                    String[] tempDelayVersion = startName.split(Session.SESSION_SEPARATION_CHAR_CHILD);
                                                                                    eventType = eventType5;
                                                                                    try {
                                                                                        hbHashs2 = hbHashs5;
                                                                                        if (tempDelayVersion.length == 2) {
                                                                                            try {
                                                                                                hbWidths = hbWidths4;
                                                                                                try {
                                                                                                    this.delayTimeoutMap.put(Long.valueOf(Long.parseLong(tempDelayVersion[1])), Integer.valueOf(currentDelayTimeout));
                                                                                                } catch (RuntimeException e42) {
                                                                                                }
                                                                                            } catch (RuntimeException e43) {
                                                                                                hbWidths = hbWidths4;
                                                                                            } catch (XmlPullParserException e44) {
                                                                                                hbWidths3 = hbWidths4;
                                                                                                e = e44;
                                                                                                start = defaultMode2;
                                                                                                end = end2;
                                                                                                keyURLinfo2 = keyURLinfo;
                                                                                                appType4 = appType2;
                                                                                                hbHashs3 = hbHashs2;
                                                                                                modeId = modeId3;
                                                                                                modeEnable = modeEnable3;
                                                                                                strReader3 = strReader;
                                                                                                hbText = hbText3;
                                                                                                hbLayout = hbLayout3;
                                                                                                hbLayoutNodes = hbLayoutNodes3;
                                                                                                receiverClass2 = receiverClass;
                                                                                                chatView2 = chatView;
                                                                                                keyDNSinfo2 = keyDNSinfo;
                                                                                                Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                                                e.printStackTrace();
                                                                                                if (strReader3 != null) {
                                                                                                }
                                                                                            } catch (IOException e45) {
                                                                                                hbWidths3 = hbWidths4;
                                                                                                e2 = e45;
                                                                                                start2 = defaultMode2;
                                                                                                end = end2;
                                                                                                keyURLinfo3 = keyURLinfo;
                                                                                                appType = appType2;
                                                                                                arrayList = openHbActivity;
                                                                                                hbHashs4 = hbHashs2;
                                                                                                modeId2 = modeId3;
                                                                                                modeEnable2 = modeEnable3;
                                                                                                strReader4 = strReader;
                                                                                                hbText2 = hbText3;
                                                                                                hbLayout2 = hbLayout3;
                                                                                                hbLayoutNodes2 = hbLayoutNodes3;
                                                                                                chatView3 = chatView;
                                                                                                keyDNSinfo3 = keyDNSinfo;
                                                                                                StringBuilder sb222222222222222222 = new StringBuilder();
                                                                                                sb222222222222222222.append("IOException ");
                                                                                                sb222222222222222222.append(e2.toString());
                                                                                                Log.e(LuckyMoneyHelper.TAG, sb222222222222222222.toString());
                                                                                                if (strReader4 != null) {
                                                                                                }
                                                                                            } catch (Throwable th23) {
                                                                                                th = th23;
                                                                                                if (strReader != null) {
                                                                                                }
                                                                                                throw th;
                                                                                            }
                                                                                        } else {
                                                                                            hbWidths = hbWidths4;
                                                                                        }
                                                                                    } catch (RuntimeException e46) {
                                                                                        hbHashs2 = hbHashs5;
                                                                                        hbWidths = hbWidths4;
                                                                                    }
                                                                                } catch (RuntimeException e47) {
                                                                                    eventType = eventType5;
                                                                                    hbHashs2 = hbHashs5;
                                                                                    hbWidths = hbWidths4;
                                                                                    hbHeights3 = hbHeights;
                                                                                    hbWidths4 = hbWidths;
                                                                                    appVersionItems2 = appVersionItems;
                                                                                    keyURLinfo4 = keyURLinfo;
                                                                                    appType = appType2;
                                                                                    hbHashs5 = hbHashs2;
                                                                                    chatView4 = chatView;
                                                                                    keyDNSinfo4 = keyDNSinfo;
                                                                                    eventType3 = parser.next();
                                                                                    versionMap2 = versionMap;
                                                                                    openHbActivity2 = openHbActivity;
                                                                                    str = str2;
                                                                                    receiverClass3 = receiverClass;
                                                                                }
                                                                            } catch (RuntimeException e48) {
                                                                                str2 = str2;
                                                                                eventType = eventType5;
                                                                                hbHashs2 = hbHashs5;
                                                                                hbWidths = hbWidths4;
                                                                                hbHeights3 = hbHeights;
                                                                                hbWidths4 = hbWidths;
                                                                                appVersionItems2 = appVersionItems;
                                                                                keyURLinfo4 = keyURLinfo;
                                                                                appType = appType2;
                                                                                hbHashs5 = hbHashs2;
                                                                                chatView4 = chatView;
                                                                                keyDNSinfo4 = keyDNSinfo;
                                                                                eventType3 = parser.next();
                                                                                versionMap2 = versionMap;
                                                                                openHbActivity2 = openHbActivity;
                                                                                str = str2;
                                                                                receiverClass3 = receiverClass;
                                                                            }
                                                                            hbHeights3 = hbHeights;
                                                                            hbWidths4 = hbWidths;
                                                                            appVersionItems2 = appVersionItems;
                                                                            keyURLinfo4 = keyURLinfo;
                                                                            appType = appType2;
                                                                            hbHashs5 = hbHashs2;
                                                                            chatView4 = chatView;
                                                                            keyDNSinfo4 = keyDNSinfo;
                                                                        }
                                                                    } catch (XmlPullParserException e49) {
                                                                        hbWidths3 = hbWidths4;
                                                                        e = e49;
                                                                        start = defaultMode2;
                                                                        end = end2;
                                                                        keyURLinfo2 = keyURLinfo;
                                                                        appType4 = appType2;
                                                                        hbHashs3 = hbHashs5;
                                                                        modeId = modeId3;
                                                                        modeEnable = modeEnable3;
                                                                        strReader3 = strReader;
                                                                        hbText = hbText3;
                                                                        hbLayout = hbLayout3;
                                                                        hbLayoutNodes = hbLayoutNodes3;
                                                                        receiverClass2 = receiverClass;
                                                                        chatView2 = chatView;
                                                                        keyDNSinfo2 = keyDNSinfo;
                                                                        Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                        e.printStackTrace();
                                                                        if (strReader3 != null) {
                                                                        }
                                                                    } catch (IOException e50) {
                                                                        hbWidths3 = hbWidths4;
                                                                        e2 = e50;
                                                                        start2 = defaultMode2;
                                                                        end = end2;
                                                                        keyURLinfo3 = keyURLinfo;
                                                                        appType = appType2;
                                                                        arrayList = openHbActivity;
                                                                        hbHashs4 = hbHashs5;
                                                                        modeId2 = modeId3;
                                                                        modeEnable2 = modeEnable3;
                                                                        strReader4 = strReader;
                                                                        hbText2 = hbText3;
                                                                        hbLayout2 = hbLayout3;
                                                                        hbLayoutNodes2 = hbLayoutNodes3;
                                                                        chatView3 = chatView;
                                                                        keyDNSinfo3 = keyDNSinfo;
                                                                        StringBuilder sb2222222222222222222 = new StringBuilder();
                                                                        sb2222222222222222222.append("IOException ");
                                                                        sb2222222222222222222.append(e2.toString());
                                                                        Log.e(LuckyMoneyHelper.TAG, sb2222222222222222222.toString());
                                                                        if (strReader4 != null) {
                                                                        }
                                                                    } catch (Throwable th24) {
                                                                        th = th24;
                                                                        if (strReader != null) {
                                                                        }
                                                                        throw th;
                                                                    }
                                                                }
                                                            } catch (XmlPullParserException e51) {
                                                                hbWidths3 = hbWidths4;
                                                                e = e51;
                                                                start = defaultMode2;
                                                                end = end2;
                                                                keyURLinfo2 = keyURLinfo4;
                                                                appType4 = appType;
                                                                hbHashs3 = hbHashs5;
                                                                modeId = modeId3;
                                                                modeEnable = modeEnable3;
                                                                strReader3 = strReader;
                                                                hbText = hbText3;
                                                                hbLayout = hbLayout3;
                                                                hbLayoutNodes = hbLayoutNodes3;
                                                                receiverClass2 = receiverClass;
                                                                chatView2 = chatView;
                                                                keyDNSinfo2 = keyDNSinfo;
                                                                Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                                e.printStackTrace();
                                                                if (strReader3 != null) {
                                                                }
                                                            } catch (IOException e52) {
                                                                hbWidths3 = hbWidths4;
                                                                e2 = e52;
                                                                start2 = defaultMode2;
                                                                end = end2;
                                                                keyURLinfo3 = keyURLinfo4;
                                                                arrayList = openHbActivity2;
                                                                hbHashs4 = hbHashs5;
                                                                modeId2 = modeId3;
                                                                modeEnable2 = modeEnable3;
                                                                strReader4 = strReader;
                                                                hbText2 = hbText3;
                                                                hbLayout2 = hbLayout3;
                                                                hbLayoutNodes2 = hbLayoutNodes3;
                                                                chatView3 = chatView;
                                                                keyDNSinfo3 = keyDNSinfo;
                                                                StringBuilder sb22222222222222222222 = new StringBuilder();
                                                                sb22222222222222222222.append("IOException ");
                                                                sb22222222222222222222.append(e2.toString());
                                                                Log.e(LuckyMoneyHelper.TAG, sb22222222222222222222.toString());
                                                                if (strReader4 != null) {
                                                                }
                                                            } catch (Throwable th25) {
                                                                th = th25;
                                                                if (strReader != null) {
                                                                }
                                                                throw th;
                                                            }
                                                        }
                                                    } catch (XmlPullParserException e53) {
                                                        hbWidths3 = hbWidths4;
                                                        e = e53;
                                                        start = defaultMode2;
                                                        end = end2;
                                                        keyURLinfo2 = keyURLinfo4;
                                                        appType4 = appType;
                                                        hbHashs3 = hbHashs5;
                                                        modeId = modeId3;
                                                        modeEnable = modeEnable3;
                                                        strReader3 = strReader;
                                                        hbText = hbText3;
                                                        hbLayout = hbLayout3;
                                                        hbLayoutNodes = hbLayoutNodes3;
                                                        receiverClass2 = receiverClass;
                                                        chatView2 = chatView;
                                                        keyDNSinfo2 = keyDNSinfo;
                                                        Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                        e.printStackTrace();
                                                        if (strReader3 != null) {
                                                        }
                                                    } catch (IOException e54) {
                                                        hbWidths3 = hbWidths4;
                                                        e2 = e54;
                                                        start2 = defaultMode2;
                                                        end = end2;
                                                        keyURLinfo3 = keyURLinfo4;
                                                        hbHashs4 = hbHashs5;
                                                        arrayList = openHbActivity2;
                                                        modeId2 = modeId3;
                                                        modeEnable2 = modeEnable3;
                                                        strReader4 = strReader;
                                                        hbText2 = hbText3;
                                                        hbLayout2 = hbLayout3;
                                                        hbLayoutNodes2 = hbLayoutNodes3;
                                                        chatView3 = chatView;
                                                        keyDNSinfo3 = keyDNSinfo;
                                                        StringBuilder sb222222222222222222222 = new StringBuilder();
                                                        sb222222222222222222222.append("IOException ");
                                                        sb222222222222222222222.append(e2.toString());
                                                        Log.e(LuckyMoneyHelper.TAG, sb222222222222222222222.toString());
                                                        if (strReader4 != null) {
                                                        }
                                                    } catch (Throwable th26) {
                                                        th = th26;
                                                        if (strReader != null) {
                                                        }
                                                        throw th;
                                                    }
                                                }
                                                eventType3 = parser.next();
                                                versionMap2 = versionMap;
                                                openHbActivity2 = openHbActivity;
                                                str = str2;
                                                receiverClass3 = receiverClass;
                                            }
                                        } catch (XmlPullParserException e55) {
                                            hbWidths3 = hbWidths4;
                                            e = e55;
                                            start = defaultMode2;
                                            end = end2;
                                            keyURLinfo2 = keyURLinfo4;
                                            appType4 = appType;
                                            hbHashs3 = hbHashs5;
                                            modeId = modeId3;
                                            modeEnable = modeEnable3;
                                            strReader3 = strReader;
                                            hbText = hbText3;
                                            hbLayout = hbLayout3;
                                            hbLayoutNodes = hbLayoutNodes3;
                                            receiverClass2 = receiverClass;
                                            chatView2 = chatView;
                                            keyDNSinfo2 = keyDNSinfo;
                                            Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                            e.printStackTrace();
                                            if (strReader3 != null) {
                                            }
                                        } catch (IOException e56) {
                                            hbWidths3 = hbWidths4;
                                            e2 = e56;
                                            start2 = defaultMode2;
                                            end = end2;
                                            keyURLinfo3 = keyURLinfo4;
                                            hbHashs4 = hbHashs5;
                                            arrayList = openHbActivity2;
                                            modeId2 = modeId3;
                                            modeEnable2 = modeEnable3;
                                            strReader4 = strReader;
                                            hbText2 = hbText3;
                                            hbLayout2 = hbLayout3;
                                            hbLayoutNodes2 = hbLayoutNodes3;
                                            chatView3 = chatView;
                                            keyDNSinfo3 = keyDNSinfo;
                                            StringBuilder sb2222222222222222222222 = new StringBuilder();
                                            sb2222222222222222222222.append("IOException ");
                                            sb2222222222222222222222.append(e2.toString());
                                            Log.e(LuckyMoneyHelper.TAG, sb2222222222222222222222.toString());
                                            if (strReader4 != null) {
                                            }
                                        } catch (Throwable th27) {
                                            th = th27;
                                            if (strReader != null) {
                                            }
                                            throw th;
                                        }
                                    } catch (XmlPullParserException e57) {
                                        hbWidths3 = hbWidths4;
                                        e = e57;
                                        start = defaultMode2;
                                        end = end2;
                                        keyURLinfo2 = keyURLinfo4;
                                        appType4 = appType;
                                        hbHashs3 = hbHashs5;
                                        modeId = modeId3;
                                        modeEnable = modeEnable3;
                                        strReader3 = strReader;
                                        hbText = hbText3;
                                        hbLayout = hbLayout3;
                                        hbLayoutNodes = hbLayoutNodes3;
                                        receiverClass2 = receiverClass;
                                        chatView2 = chatView;
                                        keyDNSinfo2 = keyDNSinfo;
                                        Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                        e.printStackTrace();
                                        if (strReader3 != null) {
                                        }
                                    } catch (IOException e58) {
                                        hbWidths3 = hbWidths4;
                                        e2 = e58;
                                        start2 = defaultMode2;
                                        end = end2;
                                        keyURLinfo3 = keyURLinfo4;
                                        hbHashs4 = hbHashs5;
                                        arrayList = openHbActivity2;
                                        modeId2 = modeId3;
                                        modeEnable2 = modeEnable3;
                                        strReader4 = strReader;
                                        hbText2 = hbText3;
                                        hbLayout2 = hbLayout3;
                                        hbLayoutNodes2 = hbLayoutNodes3;
                                        chatView3 = chatView;
                                        keyDNSinfo3 = keyDNSinfo;
                                        StringBuilder sb22222222222222222222222 = new StringBuilder();
                                        sb22222222222222222222222.append("IOException ");
                                        sb22222222222222222222222.append(e2.toString());
                                        Log.e(LuckyMoneyHelper.TAG, sb22222222222222222222222.toString());
                                        if (strReader4 != null) {
                                        }
                                    } catch (Throwable th28) {
                                        th = th28;
                                        if (strReader != null) {
                                        }
                                        throw th;
                                    }
                                } else if (eventType3 != 3) {
                                    appVersionItems = appVersionItems2;
                                    i = eventType3;
                                    keyURLinfo = keyURLinfo4;
                                    appType2 = appType;
                                    openHbActivity = openHbActivity2;
                                    versionMap = versionMap2;
                                    hbHashs = hbHashs5;
                                    hbWidths3 = hbWidths4;
                                    hbHeights = hbHeights3;
                                } else {
                                    try {
                                        String endName = parser.getName();
                                        if ("Mode".equals(endName)) {
                                            if (luckMoneyMode != null) {
                                                try {
                                                    r11 = luckMoneyMode;
                                                    try {
                                                        r11.add(new LuckMoneyMode(modeId3, modeEnable3, hbHeights3, hbWidths4, hbHashs5, hbText3, hbLayout3, hbLayoutNodes3));
                                                        appVersionItems = appVersionItems2;
                                                        i = eventType3;
                                                        keyURLinfo = keyURLinfo4;
                                                        appType2 = appType;
                                                        luckMoneyMode = r11;
                                                        openHbActivity = openHbActivity2;
                                                        versionMap = versionMap2;
                                                        hbHashs = hbHashs5;
                                                        hbWidths3 = hbWidths4;
                                                        hbHeights = hbHeights3;
                                                    } catch (XmlPullParserException e59) {
                                                        e = e59;
                                                        keyURLinfo2 = keyURLinfo4;
                                                        appType4 = appType;
                                                        start = defaultMode2;
                                                        end = end2;
                                                        modeId = modeId3;
                                                        modeEnable = modeEnable3;
                                                        strReader3 = strReader;
                                                        hbHashs3 = hbHashs5;
                                                        hbWidths3 = hbWidths4;
                                                        hbText = hbText3;
                                                        hbLayout = hbLayout3;
                                                        hbLayoutNodes = hbLayoutNodes3;
                                                        receiverClass2 = receiverClass;
                                                        chatView2 = chatView;
                                                        keyDNSinfo2 = keyDNSinfo;
                                                        Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                        e.printStackTrace();
                                                        if (strReader3 != null) {
                                                        }
                                                    } catch (IOException e60) {
                                                        e2 = e60;
                                                        keyURLinfo3 = keyURLinfo4;
                                                        start2 = defaultMode2;
                                                        end = end2;
                                                        arrayList = openHbActivity2;
                                                        modeId2 = modeId3;
                                                        modeEnable2 = modeEnable3;
                                                        strReader4 = strReader;
                                                        hbHashs4 = hbHashs5;
                                                        hbWidths3 = hbWidths4;
                                                        hbText2 = hbText3;
                                                        hbLayout2 = hbLayout3;
                                                        hbLayoutNodes2 = hbLayoutNodes3;
                                                        chatView3 = chatView;
                                                        keyDNSinfo3 = keyDNSinfo;
                                                        StringBuilder sb222222222222222222222222 = new StringBuilder();
                                                        sb222222222222222222222222.append("IOException ");
                                                        sb222222222222222222222222.append(e2.toString());
                                                        Log.e(LuckyMoneyHelper.TAG, sb222222222222222222222222.toString());
                                                        if (strReader4 != null) {
                                                        }
                                                    } catch (Throwable th29) {
                                                        th = th29;
                                                        if (strReader != null) {
                                                        }
                                                        throw th;
                                                    }
                                                } catch (XmlPullParserException e61) {
                                                    e = e61;
                                                    keyURLinfo2 = keyURLinfo4;
                                                    appType4 = appType;
                                                    start = defaultMode2;
                                                    end = end2;
                                                    modeId = modeId3;
                                                    modeEnable = modeEnable3;
                                                    strReader3 = strReader;
                                                    hbHashs3 = hbHashs5;
                                                    hbWidths3 = hbWidths4;
                                                    hbText = hbText3;
                                                    hbLayout = hbLayout3;
                                                    hbLayoutNodes = hbLayoutNodes3;
                                                    receiverClass2 = receiverClass;
                                                    chatView2 = chatView;
                                                    keyDNSinfo2 = keyDNSinfo;
                                                    Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                                    e.printStackTrace();
                                                    if (strReader3 != null) {
                                                    }
                                                } catch (IOException e62) {
                                                    e2 = e62;
                                                    keyURLinfo3 = keyURLinfo4;
                                                    start2 = defaultMode2;
                                                    end = end2;
                                                    arrayList = openHbActivity2;
                                                    modeId2 = modeId3;
                                                    modeEnable2 = modeEnable3;
                                                    strReader4 = strReader;
                                                    hbHashs4 = hbHashs5;
                                                    hbWidths3 = hbWidths4;
                                                    hbText2 = hbText3;
                                                    hbLayout2 = hbLayout3;
                                                    hbLayoutNodes2 = hbLayoutNodes3;
                                                    chatView3 = chatView;
                                                    keyDNSinfo3 = keyDNSinfo;
                                                    StringBuilder sb2222222222222222222222222 = new StringBuilder();
                                                    sb2222222222222222222222222.append("IOException ");
                                                    sb2222222222222222222222222.append(e2.toString());
                                                    Log.e(LuckyMoneyHelper.TAG, sb2222222222222222222222222.toString());
                                                    if (strReader4 != null) {
                                                    }
                                                } catch (Throwable th30) {
                                                    th = th30;
                                                    if (strReader != null) {
                                                    }
                                                    throw th;
                                                }
                                            } else {
                                                Log.w(LuckyMoneyHelper.TAG, "End tag MM_version didn't start correctly");
                                                appVersionItems = appVersionItems2;
                                                i = eventType3;
                                                keyURLinfo = keyURLinfo4;
                                                appType2 = appType;
                                                luckMoneyMode = luckMoneyMode;
                                                openHbActivity = openHbActivity2;
                                                versionMap = versionMap2;
                                                hbHashs = hbHashs5;
                                                hbWidths3 = hbWidths4;
                                                hbHeights = hbHeights3;
                                            }
                                        } else if (TAG_MM_VERSION.equals(endName)) {
                                            if (appVersionItems2 != null) {
                                                appVersionItems2.add(new AppVersionItem(defaultMode2, end2, defaultMode3, luckMoneyMode));
                                                appVersionItems = appVersionItems2;
                                                i = eventType3;
                                                keyURLinfo = keyURLinfo4;
                                                appType2 = appType;
                                                openHbActivity = openHbActivity2;
                                                versionMap = versionMap2;
                                                hbHashs = hbHashs5;
                                                hbWidths3 = hbWidths4;
                                                hbHeights = hbHeights3;
                                            } else {
                                                Log.w(LuckyMoneyHelper.TAG, "End tag MM didn't start correctly");
                                                appVersionItems = appVersionItems2;
                                                i = eventType3;
                                                keyURLinfo = keyURLinfo4;
                                                appType2 = appType;
                                                openHbActivity = openHbActivity2;
                                                versionMap = versionMap2;
                                                hbHashs = hbHashs5;
                                                hbWidths3 = hbWidths4;
                                                hbHeights = hbHeights3;
                                            }
                                        } else if (r11.equals(endName)) {
                                            this.appitem.add(new AppItem(appType, keyURLinfo4, keyDNSinfo, chatView, receiverClass, openHbActivity2, appVersionItems2));
                                            appVersionItems = appVersionItems2;
                                            i = eventType3;
                                            keyURLinfo = keyURLinfo4;
                                            appType2 = appType;
                                            openHbActivity = openHbActivity2;
                                            versionMap = versionMap2;
                                            hbHashs = hbHashs5;
                                            hbWidths3 = hbWidths4;
                                            hbHeights = hbHeights3;
                                        } else {
                                            Contacts.ContactMethods.ProviderNames.QQ.equals(endName);
                                            appVersionItems = appVersionItems2;
                                            i = eventType3;
                                            keyURLinfo = keyURLinfo4;
                                            appType2 = appType;
                                            openHbActivity = openHbActivity2;
                                            versionMap = versionMap2;
                                            hbHashs = hbHashs5;
                                            hbWidths3 = hbWidths4;
                                            hbHeights = hbHeights3;
                                        }
                                    } catch (XmlPullParserException e63) {
                                        e = e63;
                                        keyURLinfo2 = keyURLinfo4;
                                        appType4 = appType;
                                        start = defaultMode2;
                                        end = end2;
                                        modeId = modeId3;
                                        modeEnable = modeEnable3;
                                        strReader3 = strReader;
                                        hbHashs3 = hbHashs5;
                                        hbWidths3 = hbWidths4;
                                        hbText = hbText3;
                                        hbLayout = hbLayout3;
                                        hbLayoutNodes = hbLayoutNodes3;
                                        receiverClass2 = receiverClass;
                                        chatView2 = chatView;
                                        keyDNSinfo2 = keyDNSinfo;
                                        Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                        e.printStackTrace();
                                        if (strReader3 != null) {
                                        }
                                    } catch (IOException e64) {
                                        e2 = e64;
                                        keyURLinfo3 = keyURLinfo4;
                                        start2 = defaultMode2;
                                        end = end2;
                                        arrayList = openHbActivity2;
                                        modeId2 = modeId3;
                                        modeEnable2 = modeEnable3;
                                        strReader4 = strReader;
                                        hbHashs4 = hbHashs5;
                                        hbWidths3 = hbWidths4;
                                        hbText2 = hbText3;
                                        hbLayout2 = hbLayout3;
                                        hbLayoutNodes2 = hbLayoutNodes3;
                                        chatView3 = chatView;
                                        keyDNSinfo3 = keyDNSinfo;
                                        StringBuilder sb22222222222222222222222222 = new StringBuilder();
                                        sb22222222222222222222222222.append("IOException ");
                                        sb22222222222222222222222222.append(e2.toString());
                                        Log.e(LuckyMoneyHelper.TAG, sb22222222222222222222222222.toString());
                                        if (strReader4 != null) {
                                        }
                                    } catch (Throwable th31) {
                                        th = th31;
                                        if (strReader != null) {
                                        }
                                        throw th;
                                    }
                                }
                            } else {
                                appVersionItems = appVersionItems2;
                                i = eventType3;
                                keyURLinfo = keyURLinfo4;
                                appType2 = appType;
                                keyDNSinfo = keyDNSinfo4;
                                chatView = chatView4;
                                receiverClass = receiverClass3;
                                openHbActivity = openHbActivity2;
                                versionMap = versionMap2;
                                hbHashs = hbHashs5;
                                hbWidths3 = hbWidths4;
                                hbHeights = hbHeights3;
                            }
                            hbHeights3 = hbHeights;
                            hbWidths4 = hbWidths3;
                            appVersionItems2 = appVersionItems;
                            keyURLinfo4 = keyURLinfo;
                            appType = appType2;
                            hbHashs5 = hbHashs;
                            chatView4 = chatView;
                            keyDNSinfo4 = keyDNSinfo;
                            try {
                                eventType3 = parser.next();
                                versionMap2 = versionMap;
                                openHbActivity2 = openHbActivity;
                                str = str2;
                                receiverClass3 = receiverClass;
                            } catch (XmlPullParserException e65) {
                                e = e65;
                                keyURLinfo2 = keyURLinfo4;
                                appType4 = appType;
                                keyDNSinfo2 = keyDNSinfo4;
                                chatView2 = chatView4;
                                start = defaultMode2;
                                end = end2;
                                modeId = modeId3;
                                modeEnable = modeEnable3;
                                strReader3 = strReader;
                                hbHashs3 = hbHashs5;
                                hbWidths3 = hbWidths4;
                                hbText = hbText3;
                                hbLayout = hbLayout3;
                                hbLayoutNodes = hbLayoutNodes3;
                                receiverClass2 = receiverClass;
                                Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                                e.printStackTrace();
                                if (strReader3 != null) {
                                }
                            } catch (IOException e66) {
                                e2 = e66;
                                keyURLinfo3 = keyURLinfo4;
                                keyDNSinfo3 = keyDNSinfo4;
                                chatView3 = chatView4;
                                start2 = defaultMode2;
                                end = end2;
                                arrayList = openHbActivity;
                                modeId2 = modeId3;
                                modeEnable2 = modeEnable3;
                                strReader4 = strReader;
                                hbHashs4 = hbHashs5;
                                hbWidths3 = hbWidths4;
                                hbText2 = hbText3;
                                hbLayout2 = hbLayout3;
                                hbLayoutNodes2 = hbLayoutNodes3;
                                StringBuilder sb222222222222222222222222222 = new StringBuilder();
                                sb222222222222222222222222222.append("IOException ");
                                sb222222222222222222222222222.append(e2.toString());
                                Log.e(LuckyMoneyHelper.TAG, sb222222222222222222222222222.toString());
                                if (strReader4 != null) {
                                }
                            } catch (Throwable th32) {
                                th = th32;
                                if (strReader != null) {
                                }
                                throw th;
                            }
                        } else {
                            strReader.close();
                            return;
                        }
                    }
                } catch (XmlPullParserException e67) {
                    e = e67;
                    appType4 = 0;
                    keyURLinfo2 = null;
                    strReader3 = strReader2;
                    Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                    e.printStackTrace();
                    if (strReader3 != null) {
                    }
                } catch (IOException e68) {
                    arrayList = openHbActivity2;
                    appType = 0;
                    keyURLinfo3 = null;
                    strReader4 = strReader2;
                    e2 = e68;
                    StringBuilder sb2222222222222222222222222222 = new StringBuilder();
                    sb2222222222222222222222222222.append("IOException ");
                    sb2222222222222222222222222222.append(e2.toString());
                    Log.e(LuckyMoneyHelper.TAG, sb2222222222222222222222222222.toString());
                    if (strReader4 != null) {
                    }
                } catch (Throwable th33) {
                    strReader = strReader2;
                    th = th33;
                    if (strReader != null) {
                    }
                    throw th;
                }
            } catch (XmlPullParserException e69) {
                e = e69;
                Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                e.printStackTrace();
                if (strReader3 != null) {
                }
            } catch (IOException e70) {
                arrayList = openHbActivity2;
                appType = 0;
                e2 = e70;
                StringBuilder sb22222222222222222222222222222 = new StringBuilder();
                sb22222222222222222222222222222.append("IOException ");
                sb22222222222222222222222222222.append(e2.toString());
                Log.e(LuckyMoneyHelper.TAG, sb22222222222222222222222222222.toString());
                if (strReader4 != null) {
                }
            } catch (Throwable th34) {
                th = th34;
                strReader = null;
                if (strReader != null) {
                }
                throw th;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:103:0x020e A[Catch:{ XmlPullParserException -> 0x020f, IOException -> 0x01f0, all -> 0x01ec, all -> 0x01e6 }] */
        /* JADX WARNING: Removed duplicated region for block: B:109:0x0220  */
        /* JADX WARNING: Removed duplicated region for block: B:126:? A[RETURN, SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:127:? A[RETURN, SYNTHETIC] */
        private void parseContentFromXMLInternal(String content) {
            String str;
            int eventType;
            String str2 = DELAYTIMEOUT;
            StringReader strReader = null;
            int lastMode = 0;
            VersionItem lastVi = null;
            int state = 0;
            int type = 0;
            try {
                XmlPullParser parser = Xml.newPullParser();
                try {
                    strReader = new StringReader(content);
                    parser.setInput(strReader);
                    int eventType2 = parser.getEventType();
                    while (eventType2 != 1) {
                        if (eventType2 == 0) {
                            str = str2;
                        } else if (eventType2 == 2) {
                            String startName = parser.getName();
                            if (KEYURLINFO.equals(startName)) {
                                parser.next();
                                this.keyURLInfo = parser.getText();
                                str = str2;
                            } else {
                                if (VERSIONINFO.equals(startName)) {
                                    eventType = parser.next();
                                    try {
                                        this.mVersion = Long.parseLong(parser.getText());
                                    } catch (RuntimeException e) {
                                    }
                                } else if (KEYDNSINFO.equals(startName)) {
                                    parser.next();
                                    this.keyDNSInfo = parser.getText();
                                    str = str2;
                                } else if (str2.equals(startName)) {
                                    eventType = parser.next();
                                    try {
                                        this.delayTimeout = Integer.parseInt(parser.getText());
                                    } catch (RuntimeException e2) {
                                    }
                                } else if (startName == null || !startName.startsWith(str2)) {
                                    str = str2;
                                    if (BOOSTTIMEOUT.equals(startName)) {
                                        parser.next();
                                        try {
                                            this.boostTimeout = Integer.parseInt(parser.getText());
                                        } catch (RuntimeException e3) {
                                        }
                                    } else if (QTAG.equals(startName)) {
                                        parser.next();
                                        this.qtagInfo = parser.getText();
                                    } else if (QMSG.equals(startName)) {
                                        parser.next();
                                        this.qmsgInfo = parser.getText();
                                    } else if (TAG_MM_VERSION.equals(startName)) {
                                        type = 0;
                                        state = 1;
                                    } else if (TAG_MM_MODE.equals(startName)) {
                                        type = 0;
                                        state = 2;
                                    }
                                } else {
                                    eventType = parser.next();
                                    try {
                                        int currentDelayTimeout = Integer.parseInt(parser.getText());
                                        String[] tempDelayVersion = startName.split(Session.SESSION_SEPARATION_CHAR_CHILD);
                                        if (tempDelayVersion.length == 2) {
                                            str = str2;
                                            try {
                                                this.delayTimeoutMap.put(Long.valueOf(Long.parseLong(tempDelayVersion[1])), Integer.valueOf(currentDelayTimeout));
                                            } catch (RuntimeException e4) {
                                            }
                                        } else {
                                            str = str2;
                                        }
                                    } catch (RuntimeException e5) {
                                        str = str2;
                                    }
                                }
                                str = str2;
                            }
                            if (state != 1) {
                                if (state == 2) {
                                    if ("mode".equals(startName)) {
                                        lastMode = Integer.valueOf(parser.getAttributeValue("", "id")).intValue();
                                        this.modeMap[type].put(Integer.valueOf(lastMode), new ArrayMap<>());
                                    } else {
                                        this.modeMap[type].get(Integer.valueOf(lastMode)).put(startName, parser.nextText());
                                    }
                                }
                            } else if ("version".equals(startName)) {
                                lastVi = new VersionItem();
                                int vAttCount = parser.getAttributeCount();
                                for (int vii = 0; vii < vAttCount; vii++) {
                                    lastVi.modeValues.put(parser.getAttributeName(vii), Integer.valueOf(parser.getAttributeValue(vii)));
                                }
                                this.versionList[type].add(lastVi);
                            } else if (lastVi != null) {
                                lastVi.specValues.put(startName, parser.nextText());
                            }
                        } else if (eventType2 != 3) {
                            str = str2;
                        } else {
                            String endName = parser.getName();
                            if (TAG_MM_VERSION.equals(endName) || TAG_MM_MODE.equals(endName)) {
                                state = 0;
                                type = -1;
                                str = str2;
                            } else {
                                str = str2;
                            }
                        }
                        eventType2 = parser.next();
                        str2 = str;
                    }
                } catch (XmlPullParserException e6) {
                    e = e6;
                    Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                    e.printStackTrace();
                    if (strReader == null) {
                        return;
                    }
                    strReader.close();
                } catch (IOException e7) {
                    e = e7;
                    Log.e(LuckyMoneyHelper.TAG, "IOException " + e.toString());
                    if (strReader == null) {
                        strReader.close();
                    }
                    return;
                }
            } catch (XmlPullParserException e8) {
                e = e8;
                Log.e(LuckyMoneyHelper.TAG, "XmlPullParserException");
                e.printStackTrace();
                if (strReader == null) {
                }
                strReader.close();
            } catch (IOException e9) {
                e = e9;
                Log.e(LuckyMoneyHelper.TAG, "IOException " + e.toString());
                if (strReader == null) {
                }
            } catch (Throwable th) {
                th = th;
                if (strReader != null) {
                }
                throw th;
            }
            strReader.close();
        }

        public int getLuckyMoneyXMLVerion() {
            return this.luckyMoneyXMLVerion;
        }

        public boolean getLuckyMoneyIsEnable() {
            return this.luckyMoneyIsEnable;
        }

        public boolean getSwitchModeEnable() {
            return this.switchModeEnable;
        }

        public int getDelayTimeout() {
            int result = this.delayTimeout;
            if (this.delayTimeoutMap.containsKey(Long.valueOf(LuckyMoneyHelper.this.getSystemInfoVersion()))) {
                return this.delayTimeoutMap.get(Long.valueOf(LuckyMoneyHelper.this.getSystemInfoVersion())).intValue();
            }
            return result;
        }

        public int getBoostTimeout() {
            return this.boostTimeout;
        }

        public String getKeyURLInfo() {
            return this.keyURLInfo;
        }

        public String getkeyDNSInfo() {
            return this.keyDNSInfo;
        }

        public ArrayMap<Integer, ArrayMap<String, String>> getModeMap(int type) {
            return this.modeMap[type];
        }

        public ArrayList<VersionItem> getVersionList(int type) {
            return this.versionList[type];
        }

        public AppItem getAppItem(int type) {
            AppItem result = null;
            Iterator<AppItem> it = this.appitem.iterator();
            while (it.hasNext()) {
                AppItem tempItem = it.next();
                if (tempItem.getAppType() == type) {
                    result = tempItem;
                }
            }
            return result;
        }

        public String getLuckyMoneyInfo(int type) {
            if (type == 0) {
                return this.keyURLInfo;
            }
            if (type == 1) {
                return this.keyDNSInfo;
            }
            if (type == 2) {
                return this.qtagInfo;
            }
            if (type != 3) {
                return "";
            }
            return this.qmsgInfo;
        }

        public String dumpToString() {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("\nLucky Money Info:\n");
            strBuilder.append("Info Version: " + getVersion() + "\n");
            strBuilder.append("keyURL info: " + this.keyURLInfo + "\n");
            strBuilder.append("keyDNS Info: " + this.keyDNSInfo + "\n");
            strBuilder.append("qTag Info: " + this.qtagInfo + "\n");
            strBuilder.append("qMSG Info: " + this.qmsgInfo + "\n");
            strBuilder.append("delayTimeout: " + getDelayTimeout() + "\n");
            strBuilder.append("boostTimeout: " + this.boostTimeout + "\n");
            for (int i = 0; i < this.modeMap.length; i++) {
                strBuilder.append("Type: " + i + "\n");
                strBuilder.append("Mode: \n");
                for (Integer key : this.modeMap[i].keySet()) {
                    strBuilder.append("  " + key + ": ");
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.modeMap[i].get(key).toString());
                    sb.append("\n");
                    strBuilder.append(sb.toString());
                }
                strBuilder.append("\nVersion: \n");
                Iterator<VersionItem> it = this.versionList[i].iterator();
                while (it.hasNext()) {
                    strBuilder.append(it.next().toString() + "\n");
                }
                strBuilder.append("-----------------------------\n");
            }
            return strBuilder.toString();
        }
    }

    public LuckyMoneyHelper(Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new LuckyMoneyUpdateInfo(), new LuckyMoneyUpdateInfo());
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayMap<Integer, ArrayMap<String, String>> getModeMap(int type) {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).getModeMap(type);
    }

    public ArrayList<VersionItem> getVersionList(int type) {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).getVersionList(type);
    }

    public String getLuckyMoneyInfo(int type) {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).getLuckyMoneyInfo(type);
    }

    public int getDelayTimeout() {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).getDelayTimeout();
    }

    public int getBoostTimeout() {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).getBoostTimeout();
    }

    public String getKeyURLInfo() {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).getKeyURLInfo();
    }

    public String getkeyDNSInfo() {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).getkeyDNSInfo();
    }

    public int getLuckyMoneyXMLVerion() {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).getLuckyMoneyXMLVerion();
    }

    public boolean getLuckyMoneyIsEnable() {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).getLuckyMoneyIsEnable();
    }

    public boolean getSwitchModeEnable() {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).getSwitchModeEnable();
    }

    public AppItem getAppItem(int type) {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).getAppItem(type);
    }

    public String dumpToString() {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).dumpToString();
    }
}
