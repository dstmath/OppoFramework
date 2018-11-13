package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.Environment;
import android.provider.oppo.CallLog.Calls;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Xml;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SpnOverride {
    static final String LOG_TAG = "SpnOverride";
    public static final String MVNO_TYPE_GID = "gid";
    public static final String MVNO_TYPE_IMSI = "imsi";
    public static final String MVNO_TYPE_NONE = "";
    public static final String MVNO_TYPE_PNN = "pnn";
    public static final String MVNO_TYPE_SPN = "spn";
    static final String OEM_SPN_OVERRIDE_PATH = "telephony/spn-conf.xml";
    static final String PARTNER_SPN_OVERRIDE_PATH = "etc/spn-conf.xml";
    private static final String PARTNER_VIRTUAL_SPN_BY_EF_GID1_OVERRIDE_PATH = "etc/virtual-spn-conf-by-efgid1.xml";
    private static final String PARTNER_VIRTUAL_SPN_BY_EF_PNN_OVERRIDE_PATH = "etc/virtual-spn-conf-by-efpnn.xml";
    private static final String PARTNER_VIRTUAL_SPN_BY_EF_SPN_OVERRIDE_PATH = "etc/virtual-spn-conf-by-efspn.xml";
    private static final String PARTNER_VIRTUAL_SPN_BY_IMSI_OVERRIDE_PATH = "etc/virtual-spn-conf-by-imsi.xml";
    private static HashMap<String, String> sCarrierVirtualSpnMapByEfGid1;
    private static HashMap<String, String> sCarrierVirtualSpnMapByEfPnn;
    private static HashMap<String, String> sCarrierVirtualSpnMapByEfSpn;
    static final Object sInstSync = new Object();
    private static SpnOverride sInstance;
    private ArrayList CarrierVirtualSpnMapByImsi;
    private HashMap<String, String> mCarrierSpnMap = new HashMap();

    public class VirtualSpnByImsi {
        public String name;
        public String pattern;

        public VirtualSpnByImsi(String pattern, String name) {
            this.pattern = pattern;
            this.name = name;
        }
    }

    SpnOverride() {
        loadSpnOverrides();
        if (sCarrierVirtualSpnMapByEfSpn == null) {
            sCarrierVirtualSpnMapByEfSpn = new HashMap();
        }
        loadVirtualSpnOverridesByEfSpn();
        this.CarrierVirtualSpnMapByImsi = new ArrayList();
        loadVirtualSpnOverridesByImsi();
        if (sCarrierVirtualSpnMapByEfPnn == null) {
            sCarrierVirtualSpnMapByEfPnn = new HashMap();
        }
        loadVirtualSpnOverridesByEfPnn();
        if (sCarrierVirtualSpnMapByEfGid1 == null) {
            sCarrierVirtualSpnMapByEfGid1 = new HashMap();
        }
        loadVirtualSpnOverridesByEfGid1();
    }

    boolean containsCarrier(String carrier) {
        if (this.mCarrierSpnMap == null) {
            return false;
        }
        return this.mCarrierSpnMap.containsKey(carrier);
    }

    String getSpn(String carrier) {
        if (this.mCarrierSpnMap == null) {
            return null;
        }
        return (String) this.mCarrierSpnMap.get(carrier);
    }

    private void loadSpnOverrides() {
        File spnFile = new File(Environment.getRootDirectory(), PARTNER_SPN_OVERRIDE_PATH);
        File oemSpnFile = new File(Environment.getOemDirectory(), OEM_SPN_OVERRIDE_PATH);
        if (oemSpnFile.exists()) {
            long oemSpnTime = oemSpnFile.lastModified();
            long sysSpnTime = spnFile.lastModified();
            Rlog.d(LOG_TAG, "SPN Timestamp: oemTime = " + oemSpnTime + " sysTime = " + sysSpnTime);
            if (oemSpnTime > sysSpnTime) {
                Rlog.d(LOG_TAG, "SPN in OEM image is newer than System image");
                spnFile = oemSpnFile;
            }
        } else {
            Rlog.d(LOG_TAG, "No SPN in OEM image = " + oemSpnFile.getPath() + " Load SPN from system image");
        }
        try {
            FileReader spnReader = new FileReader(spnFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(spnReader);
                XmlUtils.beginDocument(parser, "spnOverrides");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"spnOverride".equals(parser.getName())) {
                        spnReader.close();
                        break;
                    }
                    this.mCarrierSpnMap.put(parser.getAttributeValue(null, "numeric"), parser.getAttributeValue(null, MVNO_TYPE_SPN));
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG, "Exception in spn-conf parser " + e);
            } catch (IOException e2) {
                Rlog.w(LOG_TAG, "Exception in spn-conf parser " + e2);
            }
        } catch (FileNotFoundException e3) {
            Rlog.w(LOG_TAG, "Can not open " + spnFile.getAbsolutePath());
        }
    }

    public String lookupOperatorName(int subId, String numeric, boolean desireLongName, Context context) {
        String operName = numeric;
        Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId));
        if (phone == null) {
            Rlog.w(LOG_TAG, "lookupOperatorName getPhone null");
            return numeric;
        }
        String mvnoOperName = getSpnByEfSpn(numeric, phone.getMvnoPattern(MVNO_TYPE_SPN));
        Rlog.w(LOG_TAG, "the result of searching mvnoOperName by EF_SPN: " + mvnoOperName);
        if (mvnoOperName == null) {
            mvnoOperName = getSpnByImsi(numeric, phone.getSubscriberId());
        }
        Rlog.w(LOG_TAG, "the result of searching mvnoOperName by IMSI: " + mvnoOperName);
        if (mvnoOperName == null) {
            mvnoOperName = getSpnByEfPnn(numeric, phone.getMvnoPattern(MVNO_TYPE_PNN));
        }
        Rlog.w(LOG_TAG, "the result of searching mvnoOperName by EF_PNN: " + mvnoOperName);
        if (mvnoOperName == null) {
            mvnoOperName = getSpnByEfGid1(numeric, phone.getMvnoPattern(MVNO_TYPE_GID));
        }
        Rlog.w(LOG_TAG, "the result of searching mvnoOperName by EF_GID1: " + mvnoOperName);
        if (mvnoOperName != null) {
            if (mvnoOperName.startsWith("GT") && TelephonyManager.getTelephonyProperty(SubscriptionManager.getPhoneId(subId), "gsm.operator.isroaming", "false") == "true") {
                Rlog.d(LOG_TAG, "GT roaming don't want to show mvnoOperName in roaming state");
            } else {
                operName = mvnoOperName;
            }
        }
        return operName;
    }

    private static void loadVirtualSpnOverridesByEfSpn() {
        Rlog.d(LOG_TAG, "loadVirtualSpnOverridesByEfSpn");
        try {
            FileReader spnReader = new FileReader(new File(Environment.getRootDirectory(), PARTNER_VIRTUAL_SPN_BY_EF_SPN_OVERRIDE_PATH));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(spnReader);
                XmlUtils.beginDocument(parser, "virtualSpnOverridesByEfSpn");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"virtualSpnOverride".equals(parser.getName())) {
                        break;
                    }
                    String mccmncspn = parser.getAttributeValue(null, "mccmncspn");
                    String spn = parser.getAttributeValue(null, Calls.CACHED_NAME);
                    Rlog.w(LOG_TAG, "test mccmncspn = " + mccmncspn + ", name = " + spn);
                    if (sCarrierVirtualSpnMapByEfSpn != null) {
                        sCarrierVirtualSpnMapByEfSpn.put(mccmncspn, spn);
                    }
                }
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e) {
                    }
                }
            } catch (XmlPullParserException e2) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efspn parser " + e2);
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException e4) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efspn parser " + e4);
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (Exception e6) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efspn parser " + e6);
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e7) {
                    }
                }
            } catch (Throwable th) {
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e8) {
                    }
                }
            }
        } catch (FileNotFoundException e9) {
            Rlog.w(LOG_TAG, "Can't open " + Environment.getRootDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_EF_SPN_OVERRIDE_PATH);
        } catch (Exception e10) {
            Rlog.w(LOG_TAG, "open file error " + Environment.getRootDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_EF_SPN_OVERRIDE_PATH);
        }
    }

    public String getSpnByEfSpn(String mccmnc, String spn) {
        if (mccmnc == null || spn == null || mccmnc.isEmpty() || spn.isEmpty()) {
            return null;
        }
        return (String) sCarrierVirtualSpnMapByEfSpn.get(mccmnc + spn);
    }

    private void loadVirtualSpnOverridesByImsi() {
        Rlog.d(LOG_TAG, "loadVirtualSpnOverridesByImsi");
        try {
            FileReader spnReader = new FileReader(new File(Environment.getRootDirectory(), PARTNER_VIRTUAL_SPN_BY_IMSI_OVERRIDE_PATH));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(spnReader);
                XmlUtils.beginDocument(parser, "virtualSpnOverridesByImsi");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if ("virtualSpnOverride".equals(parser.getName())) {
                        String imsipattern = parser.getAttributeValue(null, "imsipattern");
                        String spn = parser.getAttributeValue(null, Calls.CACHED_NAME);
                        Rlog.w(LOG_TAG, "test imsipattern = " + imsipattern + ", name = " + spn);
                        this.CarrierVirtualSpnMapByImsi.add(new VirtualSpnByImsi(imsipattern, spn));
                    } else if (spnReader != null) {
                        try {
                            spnReader.close();
                        } catch (IOException e) {
                        }
                    }
                }
            } catch (XmlPullParserException e2) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-imsi parser " + e2);
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException e4) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-imsi parser " + e4);
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (Exception e6) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-imsi parser " + e6);
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e7) {
                    }
                }
            } catch (Throwable th) {
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e8) {
                    }
                }
            }
        } catch (FileNotFoundException e9) {
            Rlog.w(LOG_TAG, "Can't open " + Environment.getRootDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_IMSI_OVERRIDE_PATH);
        } catch (Exception e10) {
            Rlog.w(LOG_TAG, "open file error " + Environment.getRootDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_IMSI_OVERRIDE_PATH);
        }
    }

    public String getSpnByImsi(String mccmnc, String imsi) {
        if (mccmnc == null || imsi == null || mccmnc.isEmpty() || imsi.isEmpty()) {
            return null;
        }
        for (int i = 0; i < this.CarrierVirtualSpnMapByImsi.size(); i++) {
            VirtualSpnByImsi vsbi = (VirtualSpnByImsi) this.CarrierVirtualSpnMapByImsi.get(i);
            Rlog.w(LOG_TAG, "getSpnByImsi(): mccmnc = " + mccmnc + ", imsi = " + imsi + ", pattern = " + vsbi.pattern);
            if (imsiMatches(vsbi.pattern, mccmnc + imsi)) {
                return vsbi.name;
            }
        }
        return null;
    }

    private static void loadVirtualSpnOverridesByEfPnn() {
        Rlog.d(LOG_TAG, "loadVirtualSpnOverridesByEfPnn");
        try {
            FileReader spnReader = new FileReader(new File(Environment.getRootDirectory(), PARTNER_VIRTUAL_SPN_BY_EF_PNN_OVERRIDE_PATH));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(spnReader);
                XmlUtils.beginDocument(parser, "virtualSpnOverridesByEfPnn");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"virtualSpnOverride".equals(parser.getName())) {
                        break;
                    }
                    String mccmncpnn = parser.getAttributeValue(null, "mccmncpnn");
                    String spn = parser.getAttributeValue(null, Calls.CACHED_NAME);
                    Rlog.w(LOG_TAG, "test mccmncpnn = " + mccmncpnn + ", name = " + spn);
                    if (sCarrierVirtualSpnMapByEfPnn != null) {
                        sCarrierVirtualSpnMapByEfPnn.put(mccmncpnn, spn);
                    }
                }
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e) {
                    }
                }
            } catch (XmlPullParserException e2) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efpnn parser " + e2);
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException e4) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efpnn parser " + e4);
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (Exception e6) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efpnn parser " + e6);
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e7) {
                    }
                }
            } catch (Throwable th) {
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e8) {
                    }
                }
            }
        } catch (FileNotFoundException e9) {
            Rlog.w(LOG_TAG, "Can't open " + Environment.getRootDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_EF_PNN_OVERRIDE_PATH);
        } catch (Exception e10) {
            Rlog.w(LOG_TAG, "open file error " + Environment.getRootDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_EF_PNN_OVERRIDE_PATH);
        }
    }

    public String getSpnByEfPnn(String mccmnc, String pnn) {
        if (mccmnc == null || pnn == null || mccmnc.isEmpty() || pnn.isEmpty()) {
            return null;
        }
        return (String) sCarrierVirtualSpnMapByEfPnn.get(mccmnc + pnn);
    }

    private static void loadVirtualSpnOverridesByEfGid1() {
        Rlog.d(LOG_TAG, "loadVirtualSpnOverridesByEfGid1");
        try {
            FileReader spnReader = new FileReader(new File(Environment.getRootDirectory(), PARTNER_VIRTUAL_SPN_BY_EF_GID1_OVERRIDE_PATH));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(spnReader);
                XmlUtils.beginDocument(parser, "virtualSpnOverridesByEfGid1");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"virtualSpnOverride".equals(parser.getName())) {
                        break;
                    }
                    String mccmncgid1 = parser.getAttributeValue(null, "mccmncgid1");
                    String spn = parser.getAttributeValue(null, Calls.CACHED_NAME);
                    Rlog.w(LOG_TAG, "test mccmncgid1 = " + mccmncgid1 + ", name = " + spn);
                    if (sCarrierVirtualSpnMapByEfGid1 != null) {
                        sCarrierVirtualSpnMapByEfGid1.put(mccmncgid1, spn);
                    }
                }
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e) {
                    }
                }
            } catch (XmlPullParserException e2) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efgid1 parser " + e2);
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException e4) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efgid1 parser " + e4);
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e5) {
                    }
                }
            } catch (Exception e6) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efgid1 parser " + e6);
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e7) {
                    }
                }
            } catch (Throwable th) {
                if (spnReader != null) {
                    try {
                        spnReader.close();
                    } catch (IOException e8) {
                    }
                }
            }
        } catch (FileNotFoundException e9) {
            Rlog.w(LOG_TAG, "Can't open " + Environment.getRootDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_EF_GID1_OVERRIDE_PATH);
        } catch (Exception e10) {
            Rlog.w(LOG_TAG, "open file error " + Environment.getRootDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_EF_GID1_OVERRIDE_PATH);
        }
    }

    public String getSpnByEfGid1(String mccmnc, String gid1) {
        if (mccmnc == null || gid1 == null || mccmnc.isEmpty() || gid1.isEmpty()) {
            return null;
        }
        return (String) sCarrierVirtualSpnMapByEfGid1.get(mccmnc + gid1);
    }

    private boolean imsiMatches(String imsiDB, String imsiSIM) {
        int len = imsiDB.length();
        Rlog.w(LOG_TAG, "mvno match imsi = " + imsiSIM + "pattern = " + imsiDB);
        if (len <= 0 || len > imsiSIM.length()) {
            return false;
        }
        int idx = 0;
        while (idx < len) {
            char c = imsiDB.charAt(idx);
            if (c != 'x' && c != 'X' && c != imsiSIM.charAt(idx)) {
                return false;
            }
            idx++;
        }
        return true;
    }

    public String isOperatorMvnoForImsi(String mccmnc, String imsi) {
        if (mccmnc == null || imsi == null || mccmnc.isEmpty() || imsi.isEmpty()) {
            return null;
        }
        for (int i = 0; i < this.CarrierVirtualSpnMapByImsi.size(); i++) {
            VirtualSpnByImsi vsbi = (VirtualSpnByImsi) this.CarrierVirtualSpnMapByImsi.get(i);
            Rlog.w(LOG_TAG, "isOperatorMvnoForImsi(): mccmnc = " + mccmnc + ", imsi = " + imsi + ", pattern = " + vsbi.pattern);
            if (imsiMatches(vsbi.pattern, mccmnc + imsi)) {
                return vsbi.pattern;
            }
        }
        return null;
    }

    public static SpnOverride getInstance() {
        SpnOverride spnOverride;
        synchronized (sInstSync) {
            if (sInstance == null) {
                sInstance = new SpnOverride();
            }
            spnOverride = sInstance;
        }
        return spnOverride;
    }
}
