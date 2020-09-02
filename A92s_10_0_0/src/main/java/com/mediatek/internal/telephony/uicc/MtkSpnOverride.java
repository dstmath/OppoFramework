package com.mediatek.internal.telephony.uicc;

import android.content.Context;
import android.os.Environment;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Xml;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.util.XmlUtils;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MtkSpnOverride {
    private static HashMap<String, String> CarrierVirtualSpnMapByEfGid1 = null;
    private static HashMap<String, String> CarrierVirtualSpnMapByEfPnn = null;
    private static HashMap<String, String> CarrierVirtualSpnMapByEfSpn = null;
    static final String LOG_TAG = "SpnOverride";
    static final String LOG_TAG_EX = "MtkSpnOverride";
    protected static final String OEM_SPN_OVERRIDE_PATH = "telephony/spn-conf.xml";
    protected static final String PARTNER_SPN_OVERRIDE_PATH = "etc/spn-conf.xml";
    private static final String PARTNER_VIRTUAL_SPN_BY_EF_GID1_OVERRIDE_PATH = "etc/virtual-spn-conf-by-efgid1.xml";
    private static final String PARTNER_VIRTUAL_SPN_BY_EF_PNN_OVERRIDE_PATH = "etc/virtual-spn-conf-by-efpnn.xml";
    private static final String PARTNER_VIRTUAL_SPN_BY_EF_SPN_OVERRIDE_PATH = "etc/virtual-spn-conf-by-efspn.xml";
    private static final String PARTNER_VIRTUAL_SPN_BY_IMSI_OVERRIDE_PATH = "etc/virtual-spn-conf-by-imsi.xml";
    static final Object sInstSync = new Object();
    private static MtkSpnOverride sInstance;
    private ArrayList CarrierVirtualSpnMapByImsi;
    protected HashMap<String, String> mCarrierSpnMap = new HashMap<>();

    public class VirtualSpnByImsi {
        public String name;
        public String pattern;

        public VirtualSpnByImsi(String pattern2, String name2) {
            this.pattern = pattern2;
            this.name = name2;
        }
    }

    public static MtkSpnOverride getInstance() {
        MtkSpnOverride mtkSpnOverride;
        synchronized (sInstSync) {
            if (sInstance == null) {
                sInstance = new MtkSpnOverride();
            }
            mtkSpnOverride = sInstance;
        }
        return mtkSpnOverride;
    }

    MtkSpnOverride() {
        loadSpnOverrides();
        CarrierVirtualSpnMapByEfSpn = new HashMap<>();
        loadVirtualSpnOverridesByEfSpn();
        this.CarrierVirtualSpnMapByImsi = new ArrayList();
        loadVirtualSpnOverridesByImsi();
        CarrierVirtualSpnMapByEfPnn = new HashMap<>();
        loadVirtualSpnOverridesByEfPnn();
        CarrierVirtualSpnMapByEfGid1 = new HashMap<>();
        loadVirtualSpnOverridesByEfGid1();
    }

    /* access modifiers changed from: protected */
    public void loadSpnOverrides() {
        File spnFile;
        Rlog.d(LOG_TAG_EX, "loadSpnOverrides");
        if (DataSubConstants.OPERATOR_OP09.equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, ""))) {
            spnFile = new File(Environment.getVendorDirectory(), "etc/spn-conf-op09.xml");
            if (!spnFile.exists()) {
                Rlog.d(LOG_TAG_EX, "No spn-conf-op09.xml file");
                spnFile = new File(Environment.getRootDirectory(), PARTNER_SPN_OVERRIDE_PATH);
            }
        } else {
            spnFile = new File(Environment.getRootDirectory(), PARTNER_SPN_OVERRIDE_PATH);
        }
        File oemSpnFile = new File(Environment.getOemDirectory(), OEM_SPN_OVERRIDE_PATH);
        if (oemSpnFile.exists()) {
            long oemSpnTime = oemSpnFile.lastModified();
            long sysSpnTime = spnFile.lastModified();
            Rlog.d(LOG_TAG_EX, "SPN Timestamp: oemTime = " + oemSpnTime + " sysTime = " + sysSpnTime);
            if (oemSpnTime > sysSpnTime) {
                Rlog.d(LOG_TAG_EX, "SPN in OEM image is newer than System image");
                spnFile = oemSpnFile;
            }
        } else {
            Rlog.d(LOG_TAG_EX, "No SPN in OEM image = " + oemSpnFile.getPath() + " Load SPN from system image");
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
                        return;
                    }
                    this.mCarrierSpnMap.put(parser.getAttributeValue(null, "numeric"), parser.getAttributeValue(null, "spn"));
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG_EX, "Exception in spn-conf parser " + e);
            } catch (IOException e2) {
                Rlog.w(LOG_TAG_EX, "Exception in spn-conf parser " + e2);
            }
        } catch (FileNotFoundException e3) {
            Rlog.w(LOG_TAG_EX, "Can not open " + spnFile.getAbsolutePath());
        }
    }

    private static void loadVirtualSpnOverridesByEfSpn() {
        Rlog.d(LOG_TAG_EX, "loadVirtualSpnOverridesByEfSpn");
        try {
            FileReader spnReader = new FileReader(new File(Environment.getVendorDirectory(), PARTNER_VIRTUAL_SPN_BY_EF_SPN_OVERRIDE_PATH));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(spnReader);
                XmlUtils.beginDocument(parser, "virtualSpnOverridesByEfSpn");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"virtualSpnOverride".equals(parser.getName())) {
                        spnReader.close();
                        return;
                    }
                    String mccmncspn = parser.getAttributeValue(null, "mccmncspn");
                    String spn = parser.getAttributeValue(null, "name");
                    Rlog.w(LOG_TAG_EX, "test mccmncspn = " + mccmncspn + ", name = " + spn);
                    CarrierVirtualSpnMapByEfSpn.put(mccmncspn, spn);
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG_EX, "Exception in virtual-spn-conf-by-efspn parser " + e);
            } catch (IOException e2) {
                Rlog.w(LOG_TAG_EX, "Exception in virtual-spn-conf-by-efspn parser " + e2);
            }
        } catch (FileNotFoundException e3) {
            Rlog.w(LOG_TAG_EX, "Can't open " + Environment.getVendorDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_EF_SPN_OVERRIDE_PATH);
        }
    }

    public String getSpnByEfSpn(String mccmnc, String spn) {
        if (mccmnc == null || spn == null || mccmnc.isEmpty() || spn.isEmpty()) {
            return null;
        }
        HashMap<String, String> hashMap = CarrierVirtualSpnMapByEfSpn;
        return hashMap.get(mccmnc + spn);
    }

    private void loadVirtualSpnOverridesByImsi() {
        Rlog.d(LOG_TAG_EX, "loadVirtualSpnOverridesByImsi");
        try {
            FileReader spnReader = new FileReader(new File(Environment.getVendorDirectory(), PARTNER_VIRTUAL_SPN_BY_IMSI_OVERRIDE_PATH));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(spnReader);
                XmlUtils.beginDocument(parser, "virtualSpnOverridesByImsi");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"virtualSpnOverride".equals(parser.getName())) {
                        spnReader.close();
                        return;
                    }
                    String imsipattern = parser.getAttributeValue(null, "imsipattern");
                    String spn = parser.getAttributeValue(null, "name");
                    Rlog.w(LOG_TAG_EX, "test imsipattern = " + imsipattern + ", name = " + spn);
                    this.CarrierVirtualSpnMapByImsi.add(new VirtualSpnByImsi(imsipattern, spn));
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG_EX, "Exception in virtual-spn-conf-by-imsi parser " + e);
            } catch (IOException e2) {
                Rlog.w(LOG_TAG_EX, "Exception in virtual-spn-conf-by-imsi parser " + e2);
            }
        } catch (FileNotFoundException e3) {
            Rlog.w(LOG_TAG_EX, "Can't open " + Environment.getVendorDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_IMSI_OVERRIDE_PATH);
        }
    }

    public String getSpnByImsi(String mccmnc, String imsi) {
        if (mccmnc == null || imsi == null || mccmnc.isEmpty() || imsi.isEmpty()) {
            return null;
        }
        for (int i = 0; i < this.CarrierVirtualSpnMapByImsi.size(); i++) {
            VirtualSpnByImsi vsbi = (VirtualSpnByImsi) this.CarrierVirtualSpnMapByImsi.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append("getSpnByImsi(): mccmnc = ");
            sb.append(mccmnc);
            sb.append(", imsi = ");
            sb.append(imsi.length() >= 6 ? imsi.substring(0, 6) : "xx");
            sb.append(", pattern = ");
            sb.append(vsbi.pattern);
            Rlog.d(LOG_TAG_EX, sb.toString());
            String str = vsbi.pattern;
            if (imsiMatches(str, mccmnc + imsi)) {
                return vsbi.name;
            }
        }
        return null;
    }

    public String isOperatorMvnoForImsi(String mccmnc, String imsi) {
        if (mccmnc == null || imsi == null || mccmnc.isEmpty() || imsi.isEmpty()) {
            return null;
        }
        for (int i = 0; i < this.CarrierVirtualSpnMapByImsi.size(); i++) {
            VirtualSpnByImsi vsbi = (VirtualSpnByImsi) this.CarrierVirtualSpnMapByImsi.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append("isOperatorMvnoForImsi(): mccmnc = ");
            sb.append(mccmnc);
            sb.append(", imsi = ");
            sb.append(imsi.length() >= 6 ? imsi.substring(0, 6) : "xx");
            sb.append(", pattern = ");
            sb.append(vsbi.pattern);
            Rlog.w(LOG_TAG_EX, sb.toString());
            String str = vsbi.pattern;
            if (imsiMatches(str, mccmnc + imsi)) {
                return vsbi.pattern;
            }
        }
        return null;
    }

    private boolean imsiMatches(String imsiDB, String imsiSIM) {
        int len = imsiDB.length();
        StringBuilder sb = new StringBuilder();
        sb.append("mvno match imsi = ");
        sb.append(imsiSIM == null ? "" : imsiSIM.length() >= 6 ? imsiSIM.substring(0, 6) : "xx");
        sb.append("pattern = ");
        sb.append(imsiDB);
        Rlog.d(LOG_TAG_EX, sb.toString());
        if (len <= 0 || imsiSIM == null || len > imsiSIM.length()) {
            return false;
        }
        for (int idx = 0; idx < len; idx++) {
            char c = imsiDB.charAt(idx);
            if (c != 'x' && c != 'X' && c != imsiSIM.charAt(idx)) {
                return false;
            }
        }
        return true;
    }

    private static void loadVirtualSpnOverridesByEfPnn() {
        Rlog.d(LOG_TAG_EX, "loadVirtualSpnOverridesByEfPnn");
        try {
            FileReader spnReader = new FileReader(new File(Environment.getVendorDirectory(), PARTNER_VIRTUAL_SPN_BY_EF_PNN_OVERRIDE_PATH));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(spnReader);
                XmlUtils.beginDocument(parser, "virtualSpnOverridesByEfPnn");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"virtualSpnOverride".equals(parser.getName())) {
                        spnReader.close();
                        return;
                    }
                    String mccmncpnn = parser.getAttributeValue(null, "mccmncpnn");
                    String spn = parser.getAttributeValue(null, "name");
                    Rlog.w(LOG_TAG_EX, "test mccmncpnn = " + mccmncpnn + ", name = " + spn);
                    CarrierVirtualSpnMapByEfPnn.put(mccmncpnn, spn);
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG_EX, "Exception in virtual-spn-conf-by-efpnn parser " + e);
            } catch (IOException e2) {
                Rlog.w(LOG_TAG_EX, "Exception in virtual-spn-conf-by-efpnn parser " + e2);
            }
        } catch (FileNotFoundException e3) {
            Rlog.w(LOG_TAG_EX, "Can't open " + Environment.getVendorDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_EF_PNN_OVERRIDE_PATH);
        }
    }

    public String getSpnByEfPnn(String mccmnc, String pnn) {
        if (mccmnc == null || pnn == null || mccmnc.isEmpty() || pnn.isEmpty()) {
            return null;
        }
        HashMap<String, String> hashMap = CarrierVirtualSpnMapByEfPnn;
        return hashMap.get(mccmnc + pnn);
    }

    private static void loadVirtualSpnOverridesByEfGid1() {
        Rlog.d(LOG_TAG_EX, "loadVirtualSpnOverridesByEfGid1");
        try {
            FileReader spnReader = new FileReader(new File(Environment.getVendorDirectory(), PARTNER_VIRTUAL_SPN_BY_EF_GID1_OVERRIDE_PATH));
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(spnReader);
                XmlUtils.beginDocument(parser, "virtualSpnOverridesByEfGid1");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"virtualSpnOverride".equals(parser.getName())) {
                        spnReader.close();
                        return;
                    }
                    String mccmncgid1 = parser.getAttributeValue(null, "mccmncgid1");
                    String spn = parser.getAttributeValue(null, "name");
                    Rlog.w(LOG_TAG_EX, "test mccmncgid1 = " + mccmncgid1 + ", name = " + spn);
                    CarrierVirtualSpnMapByEfGid1.put(mccmncgid1, spn);
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG_EX, "Exception in virtual-spn-conf-by-efgid1 parser " + e);
            } catch (IOException e2) {
                Rlog.w(LOG_TAG_EX, "Exception in virtual-spn-conf-by-efgid1 parser " + e2);
            }
        } catch (FileNotFoundException e3) {
            Rlog.w(LOG_TAG_EX, "Can't open " + Environment.getVendorDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_EF_GID1_OVERRIDE_PATH);
        }
    }

    public String getSpnByEfGid1(String mccmnc, String gid1) {
        if (mccmnc == null || gid1 == null || mccmnc.isEmpty() || gid1.isEmpty()) {
            return null;
        }
        HashMap<String, String> hashMap = CarrierVirtualSpnMapByEfGid1;
        return hashMap.get(mccmnc + gid1);
    }

    public String getSpnByPattern(int subId, String numeric) {
        Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId));
        String mvnoOperName = getSpnByEfSpn(numeric, ((MtkGsmCdmaPhone) phone).getMvnoPattern("spn"));
        Rlog.d(LOG_TAG_EX, "the result of searching mvnoOperName by EF_SPN: " + mvnoOperName);
        if (mvnoOperName == null) {
            mvnoOperName = getSpnByImsi(numeric, phone.getSubscriberId());
            Rlog.d(LOG_TAG_EX, "the result of searching mvnoOperName by IMSI: " + mvnoOperName);
        }
        if (mvnoOperName == null) {
            mvnoOperName = getSpnByEfPnn(numeric, ((MtkGsmCdmaPhone) phone).getMvnoPattern("pnn"));
            Rlog.d(LOG_TAG_EX, "the result of searching mvnoOperName by EF_PNN: " + mvnoOperName);
        }
        if (mvnoOperName != null) {
            return mvnoOperName;
        }
        String mvnoOperName2 = getSpnByEfGid1(numeric, ((MtkGsmCdmaPhone) phone).getMvnoPattern("gid"));
        Rlog.d(LOG_TAG_EX, "the result of searching mvnoOperName by EF_GID1: " + mvnoOperName2);
        return mvnoOperName2;
    }

    private boolean isForceGetCtSpnFromRes(int subId, String numeric, Context context, String mvnoOperName) {
        boolean getFromResource = false;
        Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId));
        String ctName = context.getText(134545503).toString();
        String simCarrierName = TelephonyManager.from(context).getSimOperatorName(subId);
        Rlog.d(LOG_TAG_EX, "ctName:" + ctName + ", simCarrierName:" + simCarrierName + ", subId:" + subId);
        if (ctName != null && (ctName.equals(mvnoOperName) || ctName.equals(simCarrierName))) {
            Rlog.d(LOG_TAG_EX, "Get from resource.");
            getFromResource = true;
        }
        if ((!"20404".equals(numeric) && !"45403".equals(numeric)) || phone.getPhoneType() != 2 || ctName == null || !ctName.equals(simCarrierName)) {
            return getFromResource;
        }
        Rlog.d(LOG_TAG_EX, "Special handle for roaming case!");
        return true;
    }

    public String getSpnByNumeric(String numeric, boolean desireLongName, Context context) {
        return getSpnByNumeric(numeric, desireLongName, context, false, true);
    }

    private String getSpnByNumeric(String numeric, boolean desireLongName, Context context, boolean getCtSpn, boolean getDefaultSpn) {
        if (desireLongName) {
            if (numeric.equals("46000") || numeric.equals("46002") || numeric.equals("46004") || numeric.equals("46007") || numeric.equals("46008")) {
                return context.getText(134545437).toString();
            }
            if (numeric.equals("46001") || numeric.equals("46009") || numeric.equals("45407")) {
                return context.getText(134545438).toString();
            }
            if (numeric.equals("46003") || numeric.equals("46011") || getCtSpn) {
                return context.getText(134545507).toString();
            }
            if (numeric.equals("46601")) {
                return context.getText(134545439).toString();
            }
            if (numeric.equals("46692")) {
                return context.getText(134545440).toString();
            }
            if (numeric.equals("46697")) {
                return context.getText(134545441).toString();
            }
            if (numeric.equals("99998")) {
                return context.getText(134545442).toString();
            }
            if (getDefaultSpn && containsCarrier(numeric)) {
                return getSpn(numeric);
            }
            Rlog.d(LOG_TAG_EX, "Can't find long operator name for " + numeric);
        } else if (!desireLongName) {
            if (numeric.equals("46000") || numeric.equals("46002") || numeric.equals("46004") || numeric.equals("46007") || numeric.equals("46008")) {
                return context.getText(134545444).toString();
            }
            if (numeric.equals("46001") || numeric.equals("46009") || numeric.equals("45407")) {
                return context.getText(134545445).toString();
            }
            if (numeric.equals("46003") || numeric.equals("46011") || getCtSpn) {
                return context.getText(134545508).toString();
            }
            if (numeric.equals("46601")) {
                return context.getText(134545446).toString();
            }
            if (numeric.equals("46692")) {
                return context.getText(134545447).toString();
            }
            if (numeric.equals("46697")) {
                return context.getText(134545448).toString();
            }
            if (numeric.equals("99997")) {
                return context.getText(134545449).toString();
            }
            if (getDefaultSpn && containsCarrier(numeric)) {
                return getSpn(numeric);
            }
            Rlog.d(LOG_TAG_EX, "Can't find short operator name for " + numeric);
        }
        return null;
    }

    public String lookupOperatorName(int subId, String numeric, boolean desireLongName, Context context, String defaultName) {
        if (PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId)) == null) {
            Rlog.w(LOG_TAG_EX, "lookupOperatorName getPhone null");
            return defaultName;
        }
        String operName = getSpnByPattern(subId, numeric);
        boolean getCtSpn = isForceGetCtSpnFromRes(subId, numeric, context, operName);
        if (operName == null || getCtSpn) {
            operName = getSpnByNumeric(numeric, desireLongName, context, getCtSpn, true);
        }
        return operName == null ? defaultName : operName;
    }

    public String lookupOperatorName(int subId, String numeric, boolean desireLongName, Context context) {
        return lookupOperatorName(subId, numeric, desireLongName, context, numeric);
    }

    public String lookupOperatorNameForDisplayName(int subId, String numeric, boolean desireLongName, Context context) {
        return lookupOperatorName(subId, numeric, desireLongName, context, null);
    }

    public boolean containsCarrier(String carrier) {
        return this.mCarrierSpnMap.containsKey(carrier);
    }

    public String getSpn(String carrier) {
        return this.mCarrierSpnMap.get(carrier);
    }

    public boolean containsCarrierEx(String carrier) {
        return containsCarrier(carrier);
    }

    public String getSpnEx(String carrier) {
        return getSpn(carrier);
    }
}
