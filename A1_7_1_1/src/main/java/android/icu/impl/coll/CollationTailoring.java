package android.icu.impl.coll;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Trie2_32;
import android.icu.impl.coll.SharedObject.Reference;
import android.icu.text.UnicodeSet;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import android.icu.util.VersionInfo;
import java.util.Map;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public final class CollationTailoring {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f74-assertionsDisabled = false;
    public ULocale actualLocale;
    public CollationData data;
    public Map<Integer, Integer> maxExpansions;
    CollationData ownedData;
    private String rules;
    private UResourceBundle rulesResource;
    public Reference<CollationSettings> settings;
    Trie2_32 trie;
    UnicodeSet unsafeBackwardSet;
    public int version;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.coll.CollationTailoring.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.coll.CollationTailoring.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationTailoring.<clinit>():void");
    }

    CollationTailoring(Reference<CollationSettings> baseSettings) {
        this.actualLocale = ULocale.ROOT;
        this.version = 0;
        if (baseSettings != null) {
            if (!f74-assertionsDisabled) {
                if ((((CollationSettings) baseSettings.readOnly()).reorderCodes.length == 0 ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (!f74-assertionsDisabled) {
                if ((((CollationSettings) baseSettings.readOnly()).reorderTable == null ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (!f74-assertionsDisabled) {
                if ((((CollationSettings) baseSettings.readOnly()).minHighNoReorder == 0 ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            this.settings = baseSettings.clone();
            return;
        }
        this.settings = new Reference(new CollationSettings());
    }

    void ensureOwnedData() {
        if (this.ownedData == null) {
            this.ownedData = new CollationData(Norm2AllModes.getNFCInstance().impl);
        }
        this.data = this.ownedData;
    }

    void setRules(String r) {
        Object obj = null;
        if (!f74-assertionsDisabled) {
            if (this.rules == null && this.rulesResource == null) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        this.rules = r;
    }

    void setRulesResource(UResourceBundle res) {
        Object obj = null;
        if (!f74-assertionsDisabled) {
            if (this.rules == null && this.rulesResource == null) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        this.rulesResource = res;
    }

    public String getRules() {
        if (this.rules != null) {
            return this.rules;
        }
        if (this.rulesResource != null) {
            return this.rulesResource.getString();
        }
        return "";
    }

    static VersionInfo makeBaseVersion(VersionInfo ucaVersion) {
        return VersionInfo.getInstance(VersionInfo.UCOL_BUILDER_VERSION.getMajor(), (ucaVersion.getMajor() << 3) + ucaVersion.getMinor(), ucaVersion.getMilli() << 6, 0);
    }

    void setVersion(int baseVersion, int rulesVersion) {
        int r = (rulesVersion >> 16) & Normalizer2Impl.JAMO_VT;
        int s = (rulesVersion >> 16) & 255;
        int q = rulesVersion & 255;
        this.version = (((VersionInfo.UCOL_BUILDER_VERSION.getMajor() << 24) | (16760832 & baseVersion)) | (((r >> 6) + r) & 16128)) | ((((((s << 3) + (s >> 5)) + ((rulesVersion >> 8) & 255)) + (q << 4)) + (q >> 4)) & 255);
    }

    int getUCAVersion() {
        return ((this.version >> 12) & 4080) | ((this.version >> 14) & 3);
    }
}
