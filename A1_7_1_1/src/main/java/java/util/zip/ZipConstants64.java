package java.util.zip;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class ZipConstants64 {
    static final int EFS = 2048;
    static final int ZIP64_ENDDSK = 20;
    static final int ZIP64_ENDEXT = 56;
    static final int ZIP64_ENDHDR = 56;
    static final int ZIP64_ENDLEN = 4;
    static final int ZIP64_ENDNMD = 16;
    static final int ZIP64_ENDOFF = 48;
    static final long ZIP64_ENDSIG = 101075792;
    static final int ZIP64_ENDSIZ = 40;
    static final int ZIP64_ENDTOD = 24;
    static final int ZIP64_ENDTOT = 32;
    static final int ZIP64_ENDVEM = 12;
    static final int ZIP64_ENDVER = 14;
    static final int ZIP64_EXTCRC = 4;
    static final int ZIP64_EXTHDR = 24;
    static final int ZIP64_EXTID = 1;
    static final int ZIP64_EXTLEN = 16;
    static final int ZIP64_EXTSIZ = 8;
    static final int ZIP64_LOCDSK = 4;
    static final int ZIP64_LOCHDR = 20;
    static final int ZIP64_LOCOFF = 8;
    static final long ZIP64_LOCSIG = 117853008;
    static final int ZIP64_LOCTOT = 16;
    static final int ZIP64_MAGICCOUNT = 65535;
    static final long ZIP64_MAGICVAL = 4294967295L;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.zip.ZipConstants64.<init>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private ZipConstants64() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.zip.ZipConstants64.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.zip.ZipConstants64.<init>():void");
    }
}
