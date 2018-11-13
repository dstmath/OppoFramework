package android.icu.impl.coll;

import android.icu.text.UTF16;

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
public final class CollationFCD {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f68-assertionsDisabled = false;
    private static final int[] lcccBits = null;
    private static final byte[] lcccIndex = null;
    private static final int[] tcccBits = null;
    private static final byte[] tcccIndex = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.coll.CollationFCD.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.coll.CollationFCD.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationFCD.<clinit>():void");
    }

    public static boolean hasLccc(int c) {
        if (!f68-assertionsDisabled) {
            if (!(c <= 65535)) {
                throw new AssertionError();
            }
        }
        if (c >= 768) {
            int i = lcccIndex[c >> 5];
            if (i != 0) {
                if ((lcccBits[i] & (1 << (c & 31))) != 0) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public static boolean hasTccc(int c) {
        if (!f68-assertionsDisabled) {
            if (!(c <= 65535)) {
                throw new AssertionError();
            }
        }
        if (c >= 192) {
            int i = tcccIndex[c >> 5];
            if (i != 0) {
                if ((tcccBits[i] & (1 << (c & 31))) != 0) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    static boolean mayHaveLccc(int c) {
        boolean z = true;
        if (c < 768) {
            return false;
        }
        if (c > 65535) {
            c = UTF16.getLeadSurrogate(c);
        }
        int i = lcccIndex[c >> 5];
        if (i == 0) {
            z = false;
        } else if ((lcccBits[i] & (1 << (c & 31))) == 0) {
            z = false;
        }
        return z;
    }

    static boolean maybeTibetanCompositeVowel(int c) {
        return (2096897 & c) == 3841;
    }

    static boolean isFCD16OfTibetanCompositeVowel(int fcd16) {
        return fcd16 == 33154 || fcd16 == 33156;
    }
}
