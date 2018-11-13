package com.android.internal.telephony.uicc;

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
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SpnOverride {
    private static HashMap<String, String> CarrierVirtualSpnMapByEfGid1 = null;
    private static HashMap<String, String> CarrierVirtualSpnMapByEfPnn = null;
    private static HashMap<String, String> CarrierVirtualSpnMapByEfSpn = null;
    static final String LOG_TAG = "SpnOverride";
    static final String OEM_SPN_OVERRIDE_PATH = "telephony/spn-conf.xml";
    static final String PARTNER_SPN_OVERRIDE_PATH = "etc/spn-conf.xml";
    private static final String PARTNER_VIRTUAL_SPN_BY_EF_GID1_OVERRIDE_PATH = "etc/virtual-spn-conf-by-efgid1.xml";
    private static final String PARTNER_VIRTUAL_SPN_BY_EF_PNN_OVERRIDE_PATH = "etc/virtual-spn-conf-by-efpnn.xml";
    private static final String PARTNER_VIRTUAL_SPN_BY_EF_SPN_OVERRIDE_PATH = "etc/virtual-spn-conf-by-efspn.xml";
    private static final String PARTNER_VIRTUAL_SPN_BY_IMSI_OVERRIDE_PATH = "etc/virtual-spn-conf-by-imsi.xml";
    static final Object sInstSync = null;
    private static SpnOverride sInstance;
    private ArrayList CarrierVirtualSpnMapByImsi;
    private HashMap<String, String> mCarrierSpnMap;

    public class VirtualSpnByImsi {
        public String name;
        public String pattern;
        final /* synthetic */ SpnOverride this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.SpnOverride.VirtualSpnByImsi.<init>(com.android.internal.telephony.uicc.SpnOverride, java.lang.String, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public VirtualSpnByImsi(com.android.internal.telephony.uicc.SpnOverride r1, java.lang.String r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.SpnOverride.VirtualSpnByImsi.<init>(com.android.internal.telephony.uicc.SpnOverride, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SpnOverride.VirtualSpnByImsi.<init>(com.android.internal.telephony.uicc.SpnOverride, java.lang.String, java.lang.String):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.SpnOverride.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.SpnOverride.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.SpnOverride.<clinit>():void");
    }

    public static SpnOverride getInstance() {
        synchronized (sInstSync) {
            if (sInstance == null) {
                sInstance = new SpnOverride();
            }
        }
        return sInstance;
    }

    SpnOverride() {
        this.mCarrierSpnMap = new HashMap();
        loadSpnOverrides();
        CarrierVirtualSpnMapByEfSpn = new HashMap();
        loadVirtualSpnOverridesByEfSpn();
        this.CarrierVirtualSpnMapByImsi = new ArrayList();
        loadVirtualSpnOverridesByImsi();
        CarrierVirtualSpnMapByEfPnn = new HashMap();
        loadVirtualSpnOverridesByEfPnn();
        CarrierVirtualSpnMapByEfGid1 = new HashMap();
        loadVirtualSpnOverridesByEfGid1();
    }

    boolean containsCarrier(String carrier) {
        return this.mCarrierSpnMap.containsKey(carrier);
    }

    String getSpn(String carrier) {
        return (String) this.mCarrierSpnMap.get(carrier);
    }

    private void loadSpnOverrides() {
        File spnFile;
        Rlog.d(LOG_TAG, "loadSpnOverrides");
        if ("OP09".equals(SystemProperties.get("persist.operator.optr", UsimPBMemInfo.STRING_NOT_SET))) {
            spnFile = new File(Environment.getVendorDirectory(), "etc/spn-conf-op09.xml");
            if (!spnFile.exists()) {
                Rlog.d(LOG_TAG, "No spn-conf-op09.xml file");
                spnFile = new File(Environment.getRootDirectory(), PARTNER_SPN_OVERRIDE_PATH);
            }
        } else {
            spnFile = new File(Environment.getRootDirectory(), PARTNER_SPN_OVERRIDE_PATH);
        }
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
                    this.mCarrierSpnMap.put(parser.getAttributeValue(null, "numeric"), parser.getAttributeValue(null, "spn"));
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

    private static void loadVirtualSpnOverridesByEfSpn() {
        Rlog.d(LOG_TAG, "loadVirtualSpnOverridesByEfSpn");
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
                        break;
                    }
                    String mccmncspn = parser.getAttributeValue(null, "mccmncspn");
                    String spn = parser.getAttributeValue(null, "name");
                    Rlog.w(LOG_TAG, "test mccmncspn = " + mccmncspn + ", name = " + spn);
                    CarrierVirtualSpnMapByEfSpn.put(mccmncspn, spn);
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efspn parser " + e);
            } catch (IOException e2) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efspn parser " + e2);
            }
        } catch (FileNotFoundException e3) {
            Rlog.w(LOG_TAG, "Can't open " + Environment.getVendorDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_EF_SPN_OVERRIDE_PATH);
        }
    }

    public String getSpnByEfSpn(String mccmnc, String spn) {
        if (mccmnc == null || spn == null || mccmnc.isEmpty() || spn.isEmpty()) {
            return null;
        }
        return (String) CarrierVirtualSpnMapByEfSpn.get(mccmnc + spn);
    }

    private void loadVirtualSpnOverridesByImsi() {
        Rlog.d(LOG_TAG, "loadVirtualSpnOverridesByImsi");
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
                        break;
                    }
                    String imsipattern = parser.getAttributeValue(null, "imsipattern");
                    String spn = parser.getAttributeValue(null, "name");
                    Rlog.w(LOG_TAG, "test imsipattern = " + imsipattern + ", name = " + spn);
                    this.CarrierVirtualSpnMapByImsi.add(new VirtualSpnByImsi(this, imsipattern, spn));
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-imsi parser " + e);
            } catch (IOException e2) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-imsi parser " + e2);
            }
        } catch (FileNotFoundException e3) {
            Rlog.w(LOG_TAG, "Can't open " + Environment.getVendorDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_IMSI_OVERRIDE_PATH);
        }
    }

    public String getSpnByImsi(String mccmnc, String imsi) {
        if (mccmnc == null || imsi == null || mccmnc.isEmpty() || imsi.isEmpty()) {
            return null;
        }
        for (int i = 0; i < this.CarrierVirtualSpnMapByImsi.size(); i++) {
            VirtualSpnByImsi vsbi = (VirtualSpnByImsi) this.CarrierVirtualSpnMapByImsi.get(i);
            String str = LOG_TAG;
            StringBuilder append = new StringBuilder().append("getSpnByImsi(): mccmnc = ").append(mccmnc).append(", imsi = ");
            String substring = imsi == null ? UsimPBMemInfo.STRING_NOT_SET : imsi.length() >= 6 ? imsi.substring(0, 6) : "xx";
            Rlog.d(str, append.append(substring).append(", pattern = ").append(vsbi.pattern).toString());
            if (imsiMatches(vsbi.pattern, mccmnc + imsi)) {
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
            Rlog.w(LOG_TAG, "isOperatorMvnoForImsi(): mccmnc = " + mccmnc + ", imsi = " + imsi + ", pattern = " + vsbi.pattern);
            if (imsiMatches(vsbi.pattern, mccmnc + imsi)) {
                return vsbi.pattern;
            }
        }
        return null;
    }

    private boolean imsiMatches(String imsiDB, String imsiSIM) {
        int len = imsiDB.length();
        String str = LOG_TAG;
        StringBuilder append = new StringBuilder().append("mvno match imsi = ");
        String substring = imsiSIM == null ? UsimPBMemInfo.STRING_NOT_SET : imsiSIM.length() >= 6 ? imsiSIM.substring(0, 6) : "xx";
        Rlog.d(str, append.append(substring).append("pattern = ").append(imsiDB).toString());
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

    private static void loadVirtualSpnOverridesByEfPnn() {
        Rlog.d(LOG_TAG, "loadVirtualSpnOverridesByEfPnn");
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
                        break;
                    }
                    String mccmncpnn = parser.getAttributeValue(null, "mccmncpnn");
                    String spn = parser.getAttributeValue(null, "name");
                    Rlog.w(LOG_TAG, "test mccmncpnn = " + mccmncpnn + ", name = " + spn);
                    CarrierVirtualSpnMapByEfPnn.put(mccmncpnn, spn);
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efpnn parser " + e);
            } catch (IOException e2) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efpnn parser " + e2);
            }
        } catch (FileNotFoundException e3) {
            Rlog.w(LOG_TAG, "Can't open " + Environment.getVendorDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_EF_PNN_OVERRIDE_PATH);
        }
    }

    public String getSpnByEfPnn(String mccmnc, String pnn) {
        if (mccmnc == null || pnn == null || mccmnc.isEmpty() || pnn.isEmpty()) {
            return null;
        }
        return (String) CarrierVirtualSpnMapByEfPnn.get(mccmnc + pnn);
    }

    private static void loadVirtualSpnOverridesByEfGid1() {
        Rlog.d(LOG_TAG, "loadVirtualSpnOverridesByEfGid1");
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
                        break;
                    }
                    String mccmncgid1 = parser.getAttributeValue(null, "mccmncgid1");
                    String spn = parser.getAttributeValue(null, "name");
                    Rlog.w(LOG_TAG, "test mccmncgid1 = " + mccmncgid1 + ", name = " + spn);
                    CarrierVirtualSpnMapByEfGid1.put(mccmncgid1, spn);
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efgid1 parser " + e);
            } catch (IOException e2) {
                Rlog.w(LOG_TAG, "Exception in virtual-spn-conf-by-efgid1 parser " + e2);
            }
        } catch (FileNotFoundException e3) {
            Rlog.w(LOG_TAG, "Can't open " + Environment.getVendorDirectory() + "/" + PARTNER_VIRTUAL_SPN_BY_EF_GID1_OVERRIDE_PATH);
        }
    }

    public String getSpnByEfGid1(String mccmnc, String gid1) {
        if (mccmnc == null || gid1 == null || mccmnc.isEmpty() || gid1.isEmpty()) {
            return null;
        }
        return (String) CarrierVirtualSpnMapByEfGid1.get(mccmnc + gid1);
    }

    public String lookupOperatorName(int subId, String numeric, boolean desireLongName, Context context) {
        String operName = numeric;
        Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId));
        if (phone == null) {
            Rlog.w(LOG_TAG, "lookupOperatorName getPhone null");
            return numeric;
        }
        Object mvnoOperName;
        String mvnoOperName2 = getSpnByEfSpn(numeric, phone.getMvnoPattern("spn"));
        Rlog.d(LOG_TAG, "the result of searching mvnoOperName by EF_SPN: " + mvnoOperName2);
        if (mvnoOperName2 == null) {
            mvnoOperName2 = getSpnByImsi(numeric, phone.getSubscriberId());
        }
        Rlog.d(LOG_TAG, "the result of searching mvnoOperName by IMSI: " + mvnoOperName2);
        if (mvnoOperName2 == null) {
            mvnoOperName2 = getSpnByEfPnn(numeric, phone.getMvnoPattern("pnn"));
        }
        Rlog.d(LOG_TAG, "the result of searching mvnoOperName by EF_PNN: " + mvnoOperName2);
        if (mvnoOperName2 == null) {
            mvnoOperName2 = getSpnByEfGid1(numeric, phone.getMvnoPattern("gid"));
        }
        Rlog.d(LOG_TAG, "the result of searching mvnoOperName by EF_GID1: " + mvnoOperName2);
        if (mvnoOperName2 != null) {
            if (mvnoOperName2.startsWith("GT") && TelephonyManager.getTelephonyProperty(SubscriptionManager.getPhoneId(subId), "gsm.operator.isroaming", "false") == "true") {
                Rlog.d(LOG_TAG, "GT roaming don't want to show mvnoOperName in roaming state");
                mvnoOperName = null;
            } else {
                operName = mvnoOperName2;
            }
        }
        boolean getFromResource = false;
        String ctName = context.getText(134545644).toString();
        String simCarrierName = TelephonyManager.from(context).getSimOperatorName(subId);
        Rlog.d(LOG_TAG, "ctName:" + ctName + ", simCarrierName:" + simCarrierName + ", subId:" + subId);
        if (ctName != null && ctName.equals(mvnoOperName)) {
            Rlog.d(LOG_TAG, "Get from resource.");
            getFromResource = true;
            mvnoOperName = null;
        }
        if (("20404".equals(numeric) || "45403".equals(numeric)) && phone.getPhoneType() == 2 && ctName != null && ctName.equals(simCarrierName)) {
            Rlog.d(LOG_TAG, "Special handle for roaming case!");
            getFromResource = true;
            mvnoOperName = null;
        }
        if (mvnoOperName == null && desireLongName) {
            if (numeric.equals("46000") || numeric.equals("46002") || numeric.equals("46004") || numeric.equals("46007") || numeric.equals("46008")) {
                operName = context.getText(134545437).toString();
            } else if (numeric.equals("46001") || numeric.equals("46009") || numeric.equals("45407")) {
                operName = context.getText(134545438).toString();
            } else if (numeric.equals("46003") || numeric.equals("46011") || getFromResource) {
                operName = context.getText(134545507).toString();
            } else if (numeric.equals("46601")) {
                operName = context.getText(134545439).toString();
            } else if (numeric.equals("46692")) {
                operName = context.getText(134545440).toString();
            } else if (numeric.equals("46697")) {
                operName = context.getText(134545441).toString();
            } else if (numeric.equals("99998")) {
                operName = context.getText(134545442).toString();
            } else if (numeric.equals("99999")) {
                operName = context.getText(134545443).toString();
            } else if (containsCarrier(numeric)) {
                operName = getSpn(numeric);
            } else {
                Rlog.d(LOG_TAG, "Can't find long operator name for " + numeric);
            }
        } else if (mvnoOperName == null && !desireLongName) {
            if (numeric.equals("46000") || numeric.equals("46002") || numeric.equals("46004") || numeric.equals("46007") || numeric.equals("46008")) {
                operName = context.getText(134545444).toString();
            } else if (numeric.equals("46001") || numeric.equals("46009") || numeric.equals("45407")) {
                operName = context.getText(134545445).toString();
            } else if (numeric.equals("46003") || numeric.equals("46011") || getFromResource) {
                operName = context.getText(134545508).toString();
            } else if (numeric.equals("46601")) {
                operName = context.getText(134545446).toString();
            } else if (numeric.equals("46692")) {
                operName = context.getText(134545447).toString();
            } else if (numeric.equals("46697")) {
                operName = context.getText(134545448).toString();
            } else if (numeric.equals("99997")) {
                operName = context.getText(134545449).toString();
            } else if (numeric.equals("99999")) {
                operName = context.getText(134545450).toString();
            } else if (containsCarrier(numeric)) {
                operName = getSpn(numeric);
            } else {
                Rlog.d(LOG_TAG, "Can't find short operator name for " + numeric);
            }
        }
        return operName;
    }

    public String lookupOperatorNameForDisplayName(int subId, String numeric, boolean desireLongName, Context context) {
        String operName = null;
        Phone phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(subId));
        if (phone == null) {
            Rlog.w(LOG_TAG, "lookupOperatorName getPhone null");
            return null;
        }
        String mvnoOperName = getSpnByEfSpn(numeric, phone.getMvnoPattern("spn"));
        Rlog.w(LOG_TAG, "the result of searching mvnoOperName by EF_SPN: " + mvnoOperName);
        if (mvnoOperName == null) {
            mvnoOperName = getSpnByImsi(numeric, phone.getSubscriberId());
        }
        Rlog.w(LOG_TAG, "the result of searching mvnoOperName by IMSI: " + mvnoOperName);
        if (mvnoOperName == null) {
            mvnoOperName = getSpnByEfPnn(numeric, phone.getMvnoPattern("pnn"));
        }
        Rlog.w(LOG_TAG, "the result of searching mvnoOperName by EF_PNN: " + mvnoOperName);
        if (mvnoOperName == null) {
            mvnoOperName = getSpnByEfGid1(numeric, phone.getMvnoPattern("gid"));
        }
        Rlog.w(LOG_TAG, "the result of searching mvnoOperName by EF_GID1: " + mvnoOperName);
        if (mvnoOperName != null) {
            operName = mvnoOperName;
        }
        boolean getFromResource = false;
        String ctName = context.getText(134545644).toString();
        String simCarrierName = TelephonyManager.from(context).getSimOperatorName(subId);
        Rlog.d(LOG_TAG, "ctName:" + ctName + ", simCarrierName:" + simCarrierName + ", subId:" + subId);
        if (ctName != null && ctName.equals(mvnoOperName)) {
            Rlog.d(LOG_TAG, "Get from resource.");
            getFromResource = true;
            mvnoOperName = null;
        }
        if (("20404".equals(numeric) || "45403".equals(numeric)) && phone.getPhoneType() == 2 && ctName != null && ctName.equals(simCarrierName)) {
            Rlog.d(LOG_TAG, "Special handle for roaming case!");
            getFromResource = true;
            mvnoOperName = null;
        }
        if (mvnoOperName == null && desireLongName) {
            if (numeric.equals("46000") || numeric.equals("46002") || numeric.equals("46004") || numeric.equals("46007") || numeric.equals("46008")) {
                operName = context.getText(134545437).toString();
            } else if (numeric.equals("46001") || numeric.equals("46009") || numeric.equals("45407")) {
                operName = context.getText(134545438).toString();
            } else if (numeric.equals("46003") || numeric.equals("46011") || getFromResource) {
                operName = context.getText(134545507).toString();
            } else if (numeric.equals("46601")) {
                operName = context.getText(134545439).toString();
            } else if (numeric.equals("46692")) {
                operName = context.getText(134545440).toString();
            } else if (numeric.equals("46697")) {
                operName = context.getText(134545441).toString();
            } else if (numeric.equals("99998")) {
                operName = context.getText(134545442).toString();
            } else if (numeric.equals("99999")) {
                operName = context.getText(134545443).toString();
            } else if (containsCarrier(numeric)) {
                operName = getSpn(numeric);
            } else {
                Rlog.w(LOG_TAG, "Can't find long operator name for " + numeric);
            }
        } else if (mvnoOperName == null && !desireLongName) {
            if (numeric.equals("46000") || numeric.equals("46002") || numeric.equals("46004") || numeric.equals("46007") || numeric.equals("46008")) {
                operName = context.getText(134545444).toString();
            } else if (numeric.equals("46001") || numeric.equals("46009") || numeric.equals("45407")) {
                operName = context.getText(134545445).toString();
            } else if (numeric.equals("46003") || numeric.equals("46011") || getFromResource) {
                operName = context.getText(134545508).toString();
            } else if (numeric.equals("46601")) {
                operName = context.getText(134545446).toString();
            } else if (numeric.equals("46692")) {
                operName = context.getText(134545447).toString();
            } else if (numeric.equals("46697")) {
                operName = context.getText(134545448).toString();
            } else if (numeric.equals("99997")) {
                operName = context.getText(134545449).toString();
            } else if (numeric.equals("99999")) {
                operName = context.getText(134545450).toString();
            } else {
                Rlog.w(LOG_TAG, "Can't find short operator name for " + numeric);
            }
        }
        return operName;
    }

    public boolean containsCarrierEx(String carrier) {
        return containsCarrier(carrier);
    }

    public String getSpnEx(String carrier) {
        return getSpn(carrier);
    }
}
