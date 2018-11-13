package com.android.internal.telephony.cdma;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.util.Xml;
import com.android.internal.telephony.Phone;
import com.android.internal.util.XmlUtils;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
public class EriManager {
    private static final boolean DBG = true;
    static final int ERI_FROM_FILE_SYSTEM = 1;
    static final int ERI_FROM_MODEM = 2;
    public static final int ERI_FROM_XML = 0;
    private static final String LOG_TAG = "EriManager";
    private static final boolean VDBG = false;
    private Context mContext;
    private EriFile mEriFile;
    private int mEriFileSource;
    private boolean mIsEriFileLoaded;
    private final Phone mPhone;

    class EriDisplayInformation {
        int mEriIconIndex;
        int mEriIconMode;
        String mEriIconText;
        final /* synthetic */ EriManager this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cdma.EriManager.EriDisplayInformation.<init>(com.android.internal.telephony.cdma.EriManager, int, int, java.lang.String):void, dex: 
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
        EriDisplayInformation(com.android.internal.telephony.cdma.EriManager r1, int r2, int r3, java.lang.String r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cdma.EriManager.EriDisplayInformation.<init>(com.android.internal.telephony.cdma.EriManager, int, int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.EriManager.EriDisplayInformation.<init>(com.android.internal.telephony.cdma.EriManager, int, int, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cdma.EriManager.EriDisplayInformation.toString():java.lang.String, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cdma.EriManager.EriDisplayInformation.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.EriManager.EriDisplayInformation.toString():java.lang.String");
        }
    }

    class EriFile {
        String[] mCallPromptId;
        int mEriFileType;
        int mNumberOfEriEntries;
        HashMap<Integer, EriInfo> mRoamIndTable;
        int mVersionNumber;
        final /* synthetic */ EriManager this$0;

        EriFile(EriManager this$0) {
            this.this$0 = this$0;
            this.mVersionNumber = -1;
            this.mNumberOfEriEntries = 0;
            this.mEriFileType = -1;
            this.mCallPromptId = new String[]{UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET, UsimPBMemInfo.STRING_NOT_SET};
            this.mRoamIndTable = new HashMap();
        }
    }

    public EriManager(Phone phone, Context context, int eriFileSource) {
        this.mEriFileSource = 0;
        this.mPhone = phone;
        this.mContext = context;
        this.mEriFileSource = eriFileSource;
        this.mEriFile = new EriFile(this);
    }

    public void dispose() {
        this.mEriFile = new EriFile(this);
        this.mIsEriFileLoaded = false;
    }

    public void loadEriFile() {
        switch (this.mEriFileSource) {
            case 1:
                loadEriFileFromFileSystem();
                return;
            case 2:
                loadEriFileFromModem();
                return;
            default:
                loadEriFileFromXml();
                return;
        }
    }

    private void loadEriFileFromModem() {
    }

    private void loadEriFileFromFileSystem() {
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0204 A:{SYNTHETIC, Splitter: B:39:0x0204} */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0135 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0135 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0204 A:{SYNTHETIC, Splitter: B:39:0x0204} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x01d2 A:{PHI: r20 , ExcHandler: java.io.IOException (r14_0 'e' java.lang.Exception), Splitter: B:20:0x00b6} */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0204 A:{SYNTHETIC, Splitter: B:39:0x0204} */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0135 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0135 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0204 A:{SYNTHETIC, Splitter: B:39:0x0204} */
    /* JADX WARNING: Missing block: B:37:0x01d2, code:
            r14 = move-exception;
     */
    /* JADX WARNING: Missing block: B:38:0x01d3, code:
            android.telephony.Rlog.e(LOG_TAG, "loadEriFileFromXml: no parser for " + r16 + ". Exception = " + r14.toString());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadEriFileFromXml() {
        XmlPullParser parser;
        InputStream stream;
        int parsedEriEntries;
        String name;
        FileInputStream stream2 = null;
        Resources r = this.mContext.getResources();
        try {
            Rlog.d(LOG_TAG, "loadEriFileFromXml: check for alternate file");
            InputStream fileInputStream = new FileInputStream(r.getString(17040515));
            try {
                parser = Xml.newPullParser();
                parser.setInput(fileInputStream, null);
                Rlog.d(LOG_TAG, "loadEriFileFromXml: opened alternate file");
                stream2 = fileInputStream;
            } catch (FileNotFoundException e) {
                stream2 = fileInputStream;
                Rlog.d(LOG_TAG, "loadEriFileFromXml: no alternate file");
                parser = null;
                if (parser == null) {
                }
                XmlUtils.beginDocument(parser, "EriFile");
                this.mEriFile.mVersionNumber = Integer.parseInt(parser.getAttributeValue(null, "VersionNumber"));
                this.mEriFile.mNumberOfEriEntries = Integer.parseInt(parser.getAttributeValue(null, "NumberOfEriEntries"));
                this.mEriFile.mEriFileType = Integer.parseInt(parser.getAttributeValue(null, "EriFileType"));
                parsedEriEntries = 0;
                while (true) {
                    XmlUtils.nextElement(parser);
                    name = parser.getName();
                    if (name == null) {
                    }
                }
            } catch (XmlPullParserException e2) {
                stream2 = fileInputStream;
                Rlog.d(LOG_TAG, "loadEriFileFromXml: no parser for alternate file");
                parser = null;
                if (parser == null) {
                }
                XmlUtils.beginDocument(parser, "EriFile");
                this.mEriFile.mVersionNumber = Integer.parseInt(parser.getAttributeValue(null, "VersionNumber"));
                this.mEriFile.mNumberOfEriEntries = Integer.parseInt(parser.getAttributeValue(null, "NumberOfEriEntries"));
                this.mEriFile.mEriFileType = Integer.parseInt(parser.getAttributeValue(null, "EriFileType"));
                parsedEriEntries = 0;
                while (true) {
                    XmlUtils.nextElement(parser);
                    name = parser.getName();
                    if (name == null) {
                    }
                }
            }
        } catch (FileNotFoundException e3) {
            Rlog.d(LOG_TAG, "loadEriFileFromXml: no alternate file");
            parser = null;
            if (parser == null) {
            }
            XmlUtils.beginDocument(parser, "EriFile");
            this.mEriFile.mVersionNumber = Integer.parseInt(parser.getAttributeValue(null, "VersionNumber"));
            this.mEriFile.mNumberOfEriEntries = Integer.parseInt(parser.getAttributeValue(null, "NumberOfEriEntries"));
            this.mEriFile.mEriFileType = Integer.parseInt(parser.getAttributeValue(null, "EriFileType"));
            parsedEriEntries = 0;
            while (true) {
                XmlUtils.nextElement(parser);
                name = parser.getName();
                if (name == null) {
                }
            }
        } catch (XmlPullParserException e4) {
            Rlog.d(LOG_TAG, "loadEriFileFromXml: no parser for alternate file");
            parser = null;
            if (parser == null) {
            }
            XmlUtils.beginDocument(parser, "EriFile");
            this.mEriFile.mVersionNumber = Integer.parseInt(parser.getAttributeValue(null, "VersionNumber"));
            this.mEriFile.mNumberOfEriEntries = Integer.parseInt(parser.getAttributeValue(null, "NumberOfEriEntries"));
            this.mEriFile.mEriFileType = Integer.parseInt(parser.getAttributeValue(null, "EriFileType"));
            parsedEriEntries = 0;
            while (true) {
                XmlUtils.nextElement(parser);
                name = parser.getName();
                if (name == null) {
                }
            }
        }
        if (parser == null) {
            String eriFile = null;
            CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
            if (configManager != null) {
                PersistableBundle b = configManager.getConfigForSubId(this.mPhone.getSubId());
                if (b != null) {
                    eriFile = b.getString("carrier_eri_file_name_string");
                }
            }
            Rlog.d(LOG_TAG, "eriFile = " + eriFile);
            if (eriFile == null) {
                Rlog.e(LOG_TAG, "loadEriFileFromXml: Can't find ERI file to load");
                return;
            }
            try {
                parser = Xml.newPullParser();
                parser.setInput(this.mContext.getAssets().open(eriFile), null);
            } catch (Exception e5) {
            }
        }
        try {
            XmlUtils.beginDocument(parser, "EriFile");
            this.mEriFile.mVersionNumber = Integer.parseInt(parser.getAttributeValue(null, "VersionNumber"));
            this.mEriFile.mNumberOfEriEntries = Integer.parseInt(parser.getAttributeValue(null, "NumberOfEriEntries"));
            this.mEriFile.mEriFileType = Integer.parseInt(parser.getAttributeValue(null, "EriFileType"));
            parsedEriEntries = 0;
            while (true) {
                XmlUtils.nextElement(parser);
                name = parser.getName();
                if (name == null) {
                    if (parsedEriEntries != this.mEriFile.mNumberOfEriEntries) {
                        Rlog.e(LOG_TAG, "Error Parsing ERI file: " + this.mEriFile.mNumberOfEriEntries + " defined, " + parsedEriEntries + " parsed!");
                    }
                    Rlog.d(LOG_TAG, "loadEriFileFromXml: eri parsing successful, file loaded. ver = " + this.mEriFile.mVersionNumber + ", # of entries = " + this.mEriFile.mNumberOfEriEntries);
                    this.mIsEriFileLoaded = true;
                    if (parser instanceof XmlResourceParser) {
                        ((XmlResourceParser) parser).close();
                    }
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e6) {
                        }
                    }
                } else {
                    if (name.equals("CallPromptId")) {
                        int id = Integer.parseInt(parser.getAttributeValue(null, "Id"));
                        String text = parser.getAttributeValue(null, "CallPromptText");
                        if (id < 0 || id > 2) {
                            Rlog.e(LOG_TAG, "Error Parsing ERI file: found" + id + " CallPromptId");
                        } else {
                            this.mEriFile.mCallPromptId[id] = text;
                        }
                    } else {
                        if (name.equals("EriInfo")) {
                            int roamingIndicator = Integer.parseInt(parser.getAttributeValue(null, "RoamingIndicator"));
                            int iconIndex = Integer.parseInt(parser.getAttributeValue(null, "IconIndex"));
                            int iconMode = Integer.parseInt(parser.getAttributeValue(null, "IconMode"));
                            String eriText = parser.getAttributeValue(null, "EriText");
                            int callPromptId = Integer.parseInt(parser.getAttributeValue(null, "CallPromptId"));
                            int alertId = Integer.parseInt(parser.getAttributeValue(null, "AlertId"));
                            parsedEriEntries++;
                            HashMap hashMap = this.mEriFile.mRoamIndTable;
                            hashMap.put(Integer.valueOf(roamingIndicator), new EriInfo(roamingIndicator, iconIndex, iconMode, eriText, callPromptId, alertId));
                        }
                    }
                }
            }
        } catch (Exception e7) {
            Rlog.e(LOG_TAG, "Got exception while loading ERI file.", e7);
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (stream2 != null) {
                try {
                    stream2.close();
                } catch (IOException e8) {
                }
            }
        } catch (Throwable th) {
            if (parser instanceof XmlResourceParser) {
                ((XmlResourceParser) parser).close();
            }
            if (stream2 != null) {
                try {
                    stream2.close();
                } catch (IOException e9) {
                }
            }
        }
    }

    public int getEriFileVersion() {
        return this.mEriFile.mVersionNumber;
    }

    public int getEriNumberOfEntries() {
        return this.mEriFile.mNumberOfEriEntries;
    }

    public int getEriFileType() {
        return this.mEriFile.mEriFileType;
    }

    public boolean isEriFileLoaded() {
        return this.mIsEriFileLoaded;
    }

    private EriInfo getEriInfo(int roamingIndicator) {
        if (this.mEriFile.mRoamIndTable.containsKey(Integer.valueOf(roamingIndicator))) {
            return (EriInfo) this.mEriFile.mRoamIndTable.get(Integer.valueOf(roamingIndicator));
        }
        return null;
    }

    private EriDisplayInformation getEriDisplayInformation(int roamInd, int defRoamInd) {
        EriInfo eriInfo;
        EriDisplayInformation ret;
        if (this.mIsEriFileLoaded) {
            eriInfo = getEriInfo(roamInd);
            if (eriInfo != null) {
                return new EriDisplayInformation(this, eriInfo.iconIndex, eriInfo.iconMode, eriInfo.eriText);
            }
        }
        switch (roamInd) {
            case 0:
                ret = new EriDisplayInformation(this, 0, 0, this.mContext.getText(17039584).toString());
                break;
            case 1:
                ret = new EriDisplayInformation(this, 1, 0, this.mContext.getText(17039585).toString());
                break;
            case 2:
                ret = new EriDisplayInformation(this, 2, 1, this.mContext.getText(17039586).toString());
                break;
            case 3:
                ret = new EriDisplayInformation(this, roamInd, 0, this.mContext.getText(17039587).toString());
                break;
            case 4:
                ret = new EriDisplayInformation(this, roamInd, 0, this.mContext.getText(17039588).toString());
                break;
            case 5:
                ret = new EriDisplayInformation(this, roamInd, 0, this.mContext.getText(17039589).toString());
                break;
            case 6:
                ret = new EriDisplayInformation(this, roamInd, 0, this.mContext.getText(17039590).toString());
                break;
            case 7:
                ret = new EriDisplayInformation(this, roamInd, 0, this.mContext.getText(17039591).toString());
                break;
            case 8:
                ret = new EriDisplayInformation(this, roamInd, 0, this.mContext.getText(17039592).toString());
                break;
            case 9:
                ret = new EriDisplayInformation(this, roamInd, 0, this.mContext.getText(17039593).toString());
                break;
            case 10:
                ret = new EriDisplayInformation(this, roamInd, 0, this.mContext.getText(17039594).toString());
                break;
            case 11:
                ret = new EriDisplayInformation(this, roamInd, 0, this.mContext.getText(17039595).toString());
                break;
            case 12:
                ret = new EriDisplayInformation(this, roamInd, 0, this.mContext.getText(17039596).toString());
                break;
            default:
                if (!this.mIsEriFileLoaded) {
                    Rlog.d(LOG_TAG, "ERI File not loaded");
                    if (defRoamInd <= 2) {
                        switch (defRoamInd) {
                            case 0:
                                ret = new EriDisplayInformation(this, 0, 0, this.mContext.getText(17039584).toString());
                                break;
                            case 1:
                                ret = new EriDisplayInformation(this, 1, 0, this.mContext.getText(17039585).toString());
                                break;
                            case 2:
                                ret = new EriDisplayInformation(this, 2, 1, this.mContext.getText(17039586).toString());
                                break;
                            default:
                                ret = new EriDisplayInformation(this, -1, -1, "ERI text");
                                break;
                        }
                    }
                    ret = new EriDisplayInformation(this, 2, 1, this.mContext.getText(17039586).toString());
                    break;
                }
                eriInfo = getEriInfo(roamInd);
                EriInfo defEriInfo = getEriInfo(defRoamInd);
                if (eriInfo == null) {
                    if (defEriInfo != null) {
                        ret = new EriDisplayInformation(this, defEriInfo.iconIndex, defEriInfo.iconMode, defEriInfo.eriText);
                        break;
                    }
                    Rlog.e(LOG_TAG, "ERI defRoamInd " + defRoamInd + " not found in ERI file ...on");
                    ret = new EriDisplayInformation(this, 0, 0, this.mContext.getText(17039584).toString());
                    break;
                }
                ret = new EriDisplayInformation(this, eriInfo.iconIndex, eriInfo.iconMode, eriInfo.eriText);
                break;
        }
        return ret;
    }

    public int getCdmaEriIconIndex(int roamInd, int defRoamInd) {
        return getEriDisplayInformation(roamInd, defRoamInd).mEriIconIndex;
    }

    public int getCdmaEriIconMode(int roamInd, int defRoamInd) {
        return getEriDisplayInformation(roamInd, defRoamInd).mEriIconMode;
    }

    public String getCdmaEriText(int roamInd, int defRoamInd) {
        return getEriDisplayInformation(roamInd, defRoamInd).mEriIconText;
    }
}
