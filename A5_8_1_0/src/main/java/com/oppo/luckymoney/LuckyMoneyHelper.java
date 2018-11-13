package com.oppo.luckymoney;

import android.content.Context;
import android.util.ArrayMap;
import android.util.Xml;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class LuckyMoneyHelper extends RomUpdateHelper {
    public static final String ATT_MODE_ID = "id";
    public static final String ATT_VERSION_END = "end";
    public static final String ATT_VERSION_MODE = "mode";
    public static final String ATT_VERSION_START = "start";
    private static final String DATA_FILE_DIR = "data/system/sys_luckymoney_config_list.xml";
    public static final String FILTER_NAME = "sys_luckymoney_config_list";
    public static final String MODE_2_GET_HASH_MODE = "get_hash";
    public static final String MODE_2_HG_HASH = "hg_hash";
    private static final String SYS_FILE_DIR = "system/etc/sys_luckymoney_config_list.xml";

    private class LuckyMoneyUpdateInfo extends UpdateInfo {
        static final String BOOSTTIMEOUT = "boostTimeout";
        static final String DELAYTIMEOUT = "delayTimeout";
        static final String KEYDNSINFO = "keyDNSinfo";
        static final String KEYURLINFO = "keyURLinfo";
        static final String QMSG = "Qmsg";
        static final String QTAG = "Qtag";
        static final String TAG_MM_MODE = "MM_mode";
        static final String TAG_MM_VERSION = "MM_version";
        static final String TAG_MODE_ITEM = "mode";
        static final String TAG_QQ_MODE = "QQ_mode";
        static final String TAG_QQ_VERSION = "QQ_version";
        static final String TAG_VERSION_ITEM = "version";
        static final String VERSIONINFO = "info_version";
        int boostTimeout = 0;
        int delayTimeout = 0;
        ArrayMap<Long, Integer> delayTimeoutMap = new ArrayMap();
        String keyDNSInfo = null;
        String keyURLInfo = null;
        ArrayMap<Integer, ArrayMap<String, String>>[] modeMap = new ArrayMap[]{new ArrayMap(), new ArrayMap()};
        String qmsgInfo = null;
        String qtagInfo = null;
        ArrayList<VersionItem>[] versionList = new ArrayList[]{new ArrayList(), new ArrayList()};

        public LuckyMoneyUpdateInfo() {
            super();
        }

        /* JADX WARNING: Removed duplicated region for block: B:44:0x00e3  */
        /* JADX WARNING: Removed duplicated region for block: B:79:0x01b3  */
        /* JADX WARNING: Removed duplicated region for block: B:87:0x01db  */
        /* JADX WARNING: Missing block: B:10:0x002b, code:
            r13 = r14;
     */
        /* JADX WARNING: Missing block: B:11:0x002c, code:
            r11 = r15.next();
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void parseContentFromXML(String content) {
            XmlPullParserException e;
            Throwable th;
            IOException e2;
            if (content != null) {
                StringReader strReader = null;
                int lastMode = 0;
                VersionItem lastVi = null;
                int state = 0;
                int type = 0;
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    Reader stringReader = new StringReader(content);
                    Reader reader;
                    try {
                        parser.setInput(stringReader);
                        int eventType = parser.getEventType();
                        while (true) {
                            VersionItem lastVi2 = lastVi;
                            if (eventType != 1) {
                                switch (eventType) {
                                    case 0:
                                        lastVi = lastVi2;
                                        break;
                                    case 2:
                                        try {
                                            String startName = parser.getName();
                                            if (!KEYURLINFO.equals(startName)) {
                                                if (!VERSIONINFO.equals(startName)) {
                                                    if (!KEYDNSINFO.equals(startName)) {
                                                        if (!DELAYTIMEOUT.equals(startName)) {
                                                            if (startName != null) {
                                                                if (startName.startsWith(DELAYTIMEOUT)) {
                                                                    eventType = parser.next();
                                                                    try {
                                                                        int currentDelayTimeout = Integer.parseInt(parser.getText());
                                                                        String[] tempDelayVersion = startName.split("_");
                                                                        if (tempDelayVersion.length == 2) {
                                                                            this.delayTimeoutMap.put(Long.valueOf(Long.parseLong(tempDelayVersion[1])), Integer.valueOf(currentDelayTimeout));
                                                                        }
                                                                    } catch (RuntimeException e3) {
                                                                    }
                                                                }
                                                            }
                                                            if (!BOOSTTIMEOUT.equals(startName)) {
                                                                if (!QTAG.equals(startName)) {
                                                                    if (!QMSG.equals(startName)) {
                                                                        if (!TAG_MM_VERSION.equals(startName)) {
                                                                            if (TAG_MM_MODE.equals(startName)) {
                                                                                state = 2;
                                                                                type = 0;
                                                                                lastVi = lastVi2;
                                                                                break;
                                                                            }
                                                                        }
                                                                        state = 1;
                                                                        type = 0;
                                                                        lastVi = lastVi2;
                                                                        break;
                                                                    }
                                                                    eventType = parser.next();
                                                                    this.qmsgInfo = parser.getText();
                                                                } else {
                                                                    eventType = parser.next();
                                                                    this.qtagInfo = parser.getText();
                                                                }
                                                            } else {
                                                                eventType = parser.next();
                                                                try {
                                                                    this.boostTimeout = Integer.parseInt(parser.getText());
                                                                } catch (RuntimeException e4) {
                                                                }
                                                            }
                                                        } else {
                                                            eventType = parser.next();
                                                            try {
                                                                this.delayTimeout = Integer.parseInt(parser.getText());
                                                            } catch (RuntimeException e5) {
                                                            }
                                                        }
                                                    } else {
                                                        eventType = parser.next();
                                                        this.keyDNSInfo = parser.getText();
                                                    }
                                                } else {
                                                    eventType = parser.next();
                                                    try {
                                                        this.mVersion = Long.parseLong(parser.getText());
                                                    } catch (RuntimeException e6) {
                                                    }
                                                }
                                            } else {
                                                eventType = parser.next();
                                                this.keyURLInfo = parser.getText();
                                            }
                                            switch (state) {
                                                case 1:
                                                    if (!"version".equals(startName)) {
                                                        if (lastVi2 != null) {
                                                            lastVi2.specValues.put(startName, parser.nextText());
                                                            lastVi = lastVi2;
                                                            break;
                                                        }
                                                    }
                                                    lastVi = new VersionItem();
                                                    int vAttCount = parser.getAttributeCount();
                                                    for (int vii = 0; vii < vAttCount; vii++) {
                                                        lastVi.modeValues.put(parser.getAttributeName(vii), Integer.valueOf(parser.getAttributeValue(vii)));
                                                    }
                                                    this.versionList[type].add(lastVi);
                                                    break;
                                                    break;
                                                case 2:
                                                    if ("mode".equals(startName)) {
                                                        lastMode = Integer.valueOf(parser.getAttributeValue("", LuckyMoneyHelper.ATT_MODE_ID)).intValue();
                                                        this.modeMap[type].put(Integer.valueOf(lastMode), new ArrayMap());
                                                    } else {
                                                        ((ArrayMap) this.modeMap[type].get(Integer.valueOf(lastMode))).put(startName, parser.nextText());
                                                    }
                                                    lastVi = lastVi2;
                                                    break;
                                            }
                                        } catch (XmlPullParserException e7) {
                                            e = e7;
                                            lastVi = lastVi2;
                                            strReader = stringReader;
                                            try {
                                                LuckyMoneyHelper.this.log("Got execption parsing permissions.", e);
                                                if (strReader != null) {
                                                    strReader.close();
                                                }
                                            } catch (Throwable th2) {
                                                th = th2;
                                                if (strReader != null) {
                                                }
                                                throw th;
                                            }
                                        } catch (IOException e8) {
                                            e2 = e8;
                                            lastVi = lastVi2;
                                            strReader = stringReader;
                                            LuckyMoneyHelper.this.log("Got execption parsing permissions.", e2);
                                            if (strReader != null) {
                                                strReader.close();
                                            }
                                        } catch (Throwable th3) {
                                            th = th3;
                                            lastVi = lastVi2;
                                            strReader = stringReader;
                                            if (strReader != null) {
                                                strReader.close();
                                            }
                                            throw th;
                                        }
                                        break;
                                    case 3:
                                        String endName = parser.getName();
                                        if (TAG_MM_VERSION.equals(endName) || TAG_MM_MODE.equals(endName)) {
                                            state = 0;
                                            type = -1;
                                        }
                                        lastVi = lastVi2;
                                        break;
                                }
                            }
                            if (stringReader != null) {
                                stringReader.close();
                            }
                            reader = stringReader;
                        }
                    } catch (XmlPullParserException e9) {
                        e = e9;
                        reader = stringReader;
                    } catch (IOException e10) {
                        e2 = e10;
                        reader = stringReader;
                    } catch (Throwable th4) {
                        th = th4;
                        reader = stringReader;
                    }
                } catch (XmlPullParserException e11) {
                    e = e11;
                    LuckyMoneyHelper.this.log("Got execption parsing permissions.", e);
                    if (strReader != null) {
                    }
                } catch (IOException e12) {
                    e2 = e12;
                    LuckyMoneyHelper.this.log("Got execption parsing permissions.", e2);
                    if (strReader != null) {
                    }
                }
            }
        }

        public int getDelayTimeout() {
            int result = this.delayTimeout;
            if (this.delayTimeoutMap.containsKey(Long.valueOf(LuckyMoneyHelper.this.getSystemInfoVersion()))) {
                return ((Integer) this.delayTimeoutMap.get(Long.valueOf(LuckyMoneyHelper.this.getSystemInfoVersion()))).intValue();
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

        public String getLuckyMoneyInfo(int type) {
            switch (type) {
                case 0:
                    return this.keyURLInfo;
                case 1:
                    return this.keyDNSInfo;
                case 2:
                    return this.qtagInfo;
                case 3:
                    return this.qmsgInfo;
                default:
                    return "";
            }
        }

        public String dumpToString() {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("\nLucky Money Info:\n");
            strBuilder.append("Info Version: ").append(getVersion()).append("\n");
            strBuilder.append("keyURL info: ").append(this.keyURLInfo).append("\n");
            strBuilder.append("keyDNS Info: ").append(this.keyDNSInfo).append("\n");
            strBuilder.append("qTag Info: ").append(this.qtagInfo).append("\n");
            strBuilder.append("qMSG Info: ").append(this.qmsgInfo).append("\n");
            strBuilder.append("delayTimeout: ").append(getDelayTimeout()).append("\n");
            strBuilder.append("boostTimeout: ").append(this.boostTimeout).append("\n");
            for (int i = 0; i < this.modeMap.length; i++) {
                strBuilder.append("Type: ").append(i).append("\n");
                strBuilder.append("Mode: \n");
                for (Integer key : this.modeMap[i].keySet()) {
                    strBuilder.append("  ").append(key).append(": ");
                    strBuilder.append(((ArrayMap) this.modeMap[i].get(key)).toString()).append("\n");
                }
                strBuilder.append("\nVersion: \n");
                for (VersionItem vi : this.versionList[i]) {
                    strBuilder.append(vi.toString()).append("\n");
                }
                strBuilder.append("-----------------------------\n");
            }
            return strBuilder.toString();
        }
    }

    public static class VersionItem {
        ArrayMap<String, Integer> modeValues = new ArrayMap();
        ArrayMap<String, String> specValues = new ArrayMap();

        public int getMode() {
            if (this.modeValues.containsKey("mode")) {
                return ((Integer) this.modeValues.get("mode")).intValue();
            }
            return 0;
        }

        public int getStart() {
            if (this.modeValues.containsKey(LuckyMoneyHelper.ATT_VERSION_START)) {
                return ((Integer) this.modeValues.get(LuckyMoneyHelper.ATT_VERSION_START)).intValue();
            }
            return Integer.MIN_VALUE;
        }

        public int getEnd() {
            if (this.modeValues.containsKey(LuckyMoneyHelper.ATT_VERSION_END)) {
                return ((Integer) this.modeValues.get(LuckyMoneyHelper.ATT_VERSION_END)).intValue();
            }
            return Integer.MAX_VALUE;
        }

        public ArrayMap<String, String> getSpecValues() {
            return this.specValues;
        }

        public String toString() {
            return "[mode=" + getMode() + ", start=" + getStart() + ", end=" + getEnd() + "]\n" + "   " + this.specValues.toString() + "\n";
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

    public String dumpToString() {
        return ((LuckyMoneyUpdateInfo) getUpdateInfo(true)).dumpToString();
    }
}
