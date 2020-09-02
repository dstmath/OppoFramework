package com.oppo.internal.telephony.emergency;

import android.telephony.Rlog;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public final class OppoEccUpdater {
    private static final boolean DBG = true;
    static final String ECC_XML_PATH = "/system/etc/ecc_list.xml";
    static final String LOG_TAG = "OppoEccUpdater";
    private static OppoEccUpdater instance;
    static ArrayList<EccEntry> sTable = new ArrayList<>();

    private OppoEccUpdater() {
    }

    public static synchronized OppoEccUpdater getInstance() {
        OppoEccUpdater oppoEccUpdater;
        synchronized (OppoEccUpdater.class) {
            if (instance == null) {
                instance = new OppoEccUpdater();
            }
            oppoEccUpdater = instance;
        }
        return oppoEccUpdater;
    }

    private synchronized void parseFromXml(String path, ArrayList<EccEntry> arrayList) {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            if (parser == null) {
                log("[parseFromXml] XmlPullParserFactory.newPullParser() return null");
                return;
            }
            FileReader fileReader = new FileReader(path);
            parser.setInput(fileReader);
            EccEntry record = null;
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                if (eventType != 2) {
                    if (eventType == 3 && parser.getName().equals(EccEntry.ECC_ENTRY_TAG) && record != null) {
                        log("add EccEntry = " + record);
                        sTable.add(record);
                    }
                } else if (parser.getName().equals(EccEntry.ECC_ENTRY_TAG)) {
                    record = new EccEntry();
                    int attrNum = parser.getAttributeCount();
                    for (int i = 0; i < attrNum; i++) {
                        String name = parser.getAttributeName(i);
                        String value = parser.getAttributeValue(i);
                        if (name.equals(EccEntry.ECC_ATTR_MCC)) {
                            record.setMcc(Integer.parseInt(value));
                        } else if (name.equals(EccEntry.ECC_ATTR_MNC)) {
                            record.setMnc(Integer.parseInt(value));
                        } else if (name.equals(EccEntry.ECC_ATTR_MASK)) {
                            record.setMask(Integer.parseInt(value));
                        } else if (name.equals(EccEntry.ECC_ATTR_ECC)) {
                            record.setEcc(value);
                        } else if (name.equals(EccEntry.ECC_ATTR_CAT_LEN)) {
                            record.setCatlen(Integer.parseInt(value));
                        } else if (name.equals(EccEntry.ECC_ATTR_CAT_VAL)) {
                            record.setCatval(Integer.parseInt(value));
                        } else if (name.equals(EccEntry.ECC_ATTR_MODE)) {
                            record.setMode(Integer.parseInt(value));
                        } else if (name.equals(EccEntry.ECC_ATTR_SPECIAL)) {
                            record.setSpecial(Integer.parseInt(value));
                        }
                    }
                }
            }
            fileReader.close();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public synchronized void parseEccList() {
        sTable.clear();
        parseFromXml(ECC_XML_PATH, sTable);
        Collections.sort(sTable);
    }

    public static synchronized void addEccEntry(int mcc, int mnc, int mask, String ecc, int catlen, int catval, int mode, int special) {
        synchronized (OppoEccUpdater.class) {
            sTable.add(new EccEntry(mcc, mnc, mask, ecc, catlen, catval, mode, special));
        }
    }

    public static EccEntry[] getEccEntryTable() {
        ArrayList<EccEntry> arrayList = sTable;
        return (EccEntry[]) arrayList.toArray(new EccEntry[arrayList.size()]);
    }

    public static int getEccEntryTableLength() {
        return sTable.size();
    }

    public static void clearEccEntryTable() {
        sTable.clear();
    }

    private static void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }
}
