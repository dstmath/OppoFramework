package android.renderscript;

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
public final class ScriptIntrinsicBLAS extends ScriptIntrinsic {
    public static final int CONJ_TRANSPOSE = 113;
    public static final int LEFT = 141;
    public static final int LOWER = 122;
    public static final int NON_UNIT = 131;
    public static final int NO_TRANSPOSE = 111;
    public static final int RIGHT = 142;
    private static final int RsBlas_bnnm = 1000;
    private static final int RsBlas_caxpy = 29;
    private static final int RsBlas_ccopy = 28;
    private static final int RsBlas_cdotc_sub = 6;
    private static final int RsBlas_cdotu_sub = 5;
    private static final int RsBlas_cgbmv = 64;
    private static final int RsBlas_cgemm = 125;
    private static final int RsBlas_cgemv = 63;
    private static final int RsBlas_cgerc = 99;
    private static final int RsBlas_cgeru = 98;
    private static final int RsBlas_chbmv = 96;
    private static final int RsBlas_chemm = 137;
    private static final int RsBlas_chemv = 95;
    private static final int RsBlas_cher = 100;
    private static final int RsBlas_cher2 = 102;
    private static final int RsBlas_cher2k = 139;
    private static final int RsBlas_cherk = 138;
    private static final int RsBlas_chpmv = 97;
    private static final int RsBlas_chpr = 101;
    private static final int RsBlas_chpr2 = 103;
    private static final int RsBlas_cscal = 43;
    private static final int RsBlas_csscal = 45;
    private static final int RsBlas_cswap = 27;
    private static final int RsBlas_csymm = 126;
    private static final int RsBlas_csyr2k = 128;
    private static final int RsBlas_csyrk = 127;
    private static final int RsBlas_ctbmv = 66;
    private static final int RsBlas_ctbsv = 69;
    private static final int RsBlas_ctpmv = 67;
    private static final int RsBlas_ctpsv = 70;
    private static final int RsBlas_ctrmm = 129;
    private static final int RsBlas_ctrmv = 65;
    private static final int RsBlas_ctrsm = 130;
    private static final int RsBlas_ctrsv = 68;
    private static final int RsBlas_dasum = 12;
    private static final int RsBlas_daxpy = 26;
    private static final int RsBlas_dcopy = 25;
    private static final int RsBlas_ddot = 4;
    private static final int RsBlas_dgbmv = 56;
    private static final int RsBlas_dgemm = 119;
    private static final int RsBlas_dgemv = 55;
    private static final int RsBlas_dger = 90;
    private static final int RsBlas_dnrm2 = 11;
    private static final int RsBlas_drot = 39;
    private static final int RsBlas_drotg = 37;
    private static final int RsBlas_drotm = 40;
    private static final int RsBlas_drotmg = 38;
    private static final int RsBlas_dsbmv = 88;
    private static final int RsBlas_dscal = 42;
    private static final int RsBlas_dsdot = 2;
    private static final int RsBlas_dspmv = 89;
    private static final int RsBlas_dspr = 92;
    private static final int RsBlas_dspr2 = 94;
    private static final int RsBlas_dswap = 24;
    private static final int RsBlas_dsymm = 120;
    private static final int RsBlas_dsymv = 87;
    private static final int RsBlas_dsyr = 91;
    private static final int RsBlas_dsyr2 = 93;
    private static final int RsBlas_dsyr2k = 122;
    private static final int RsBlas_dsyrk = 121;
    private static final int RsBlas_dtbmv = 58;
    private static final int RsBlas_dtbsv = 61;
    private static final int RsBlas_dtpmv = 59;
    private static final int RsBlas_dtpsv = 62;
    private static final int RsBlas_dtrmm = 123;
    private static final int RsBlas_dtrmv = 57;
    private static final int RsBlas_dtrsm = 124;
    private static final int RsBlas_dtrsv = 60;
    private static final int RsBlas_dzasum = 16;
    private static final int RsBlas_dznrm2 = 15;
    private static final int RsBlas_icamax = 19;
    private static final int RsBlas_idamax = 18;
    private static final int RsBlas_isamax = 17;
    private static final int RsBlas_izamax = 20;
    private static final int RsBlas_sasum = 10;
    private static final int RsBlas_saxpy = 23;
    private static final int RsBlas_scasum = 14;
    private static final int RsBlas_scnrm2 = 13;
    private static final int RsBlas_scopy = 22;
    private static final int RsBlas_sdot = 3;
    private static final int RsBlas_sdsdot = 1;
    private static final int RsBlas_sgbmv = 48;
    private static final int RsBlas_sgemm = 113;
    private static final int RsBlas_sgemv = 47;
    private static final int RsBlas_sger = 82;
    private static final int RsBlas_snrm2 = 9;
    private static final int RsBlas_srot = 35;
    private static final int RsBlas_srotg = 33;
    private static final int RsBlas_srotm = 36;
    private static final int RsBlas_srotmg = 34;
    private static final int RsBlas_ssbmv = 80;
    private static final int RsBlas_sscal = 41;
    private static final int RsBlas_sspmv = 81;
    private static final int RsBlas_sspr = 84;
    private static final int RsBlas_sspr2 = 86;
    private static final int RsBlas_sswap = 21;
    private static final int RsBlas_ssymm = 114;
    private static final int RsBlas_ssymv = 79;
    private static final int RsBlas_ssyr = 83;
    private static final int RsBlas_ssyr2 = 85;
    private static final int RsBlas_ssyr2k = 116;
    private static final int RsBlas_ssyrk = 115;
    private static final int RsBlas_stbmv = 50;
    private static final int RsBlas_stbsv = 53;
    private static final int RsBlas_stpmv = 51;
    private static final int RsBlas_stpsv = 54;
    private static final int RsBlas_strmm = 117;
    private static final int RsBlas_strmv = 49;
    private static final int RsBlas_strsm = 118;
    private static final int RsBlas_strsv = 52;
    private static final int RsBlas_zaxpy = 32;
    private static final int RsBlas_zcopy = 31;
    private static final int RsBlas_zdotc_sub = 8;
    private static final int RsBlas_zdotu_sub = 7;
    private static final int RsBlas_zdscal = 46;
    private static final int RsBlas_zgbmv = 72;
    private static final int RsBlas_zgemm = 131;
    private static final int RsBlas_zgemv = 71;
    private static final int RsBlas_zgerc = 108;
    private static final int RsBlas_zgeru = 107;
    private static final int RsBlas_zhbmv = 105;
    private static final int RsBlas_zhemm = 140;
    private static final int RsBlas_zhemv = 104;
    private static final int RsBlas_zher = 109;
    private static final int RsBlas_zher2 = 111;
    private static final int RsBlas_zher2k = 142;
    private static final int RsBlas_zherk = 141;
    private static final int RsBlas_zhpmv = 106;
    private static final int RsBlas_zhpr = 110;
    private static final int RsBlas_zhpr2 = 112;
    private static final int RsBlas_zscal = 44;
    private static final int RsBlas_zswap = 30;
    private static final int RsBlas_zsymm = 132;
    private static final int RsBlas_zsyr2k = 134;
    private static final int RsBlas_zsyrk = 133;
    private static final int RsBlas_ztbmv = 74;
    private static final int RsBlas_ztbsv = 77;
    private static final int RsBlas_ztpmv = 75;
    private static final int RsBlas_ztpsv = 78;
    private static final int RsBlas_ztrmm = 135;
    private static final int RsBlas_ztrmv = 73;
    private static final int RsBlas_ztrsm = 136;
    private static final int RsBlas_ztrsv = 76;
    public static final int TRANSPOSE = 112;
    public static final int UNIT = 132;
    public static final int UPPER = 121;
    private Allocation mLUT;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptIntrinsicBLAS.<init>(long, android.renderscript.RenderScript):void, dex: 
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
    private ScriptIntrinsicBLAS(long r1, android.renderscript.RenderScript r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptIntrinsicBLAS.<init>(long, android.renderscript.RenderScript):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.<init>(long, android.renderscript.RenderScript):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.create(android.renderscript.RenderScript):android.renderscript.ScriptIntrinsicBLAS, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static android.renderscript.ScriptIntrinsicBLAS create(android.renderscript.RenderScript r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.create(android.renderscript.RenderScript):android.renderscript.ScriptIntrinsicBLAS, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.create(android.renderscript.RenderScript):android.renderscript.ScriptIntrinsicBLAS");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptIntrinsicBLAS.validateConjTranspose(int):void, dex: 
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
    static void validateConjTranspose(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptIntrinsicBLAS.validateConjTranspose(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateConjTranspose(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptIntrinsicBLAS.validateDiag(int):void, dex: 
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
    static void validateDiag(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptIntrinsicBLAS.validateDiag(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateDiag(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateGEMV(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void validateGEMV(android.renderscript.Element r1, int r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateGEMV(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateGEMV(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateGER(android.renderscript.Element, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void validateGER(android.renderscript.Element r1, android.renderscript.Allocation r2, int r3, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateGER(android.renderscript.Element, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateGER(android.renderscript.Element, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateGERU(android.renderscript.Element, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void validateGERU(android.renderscript.Element r1, android.renderscript.Allocation r2, int r3, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateGERU(android.renderscript.Element, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateGERU(android.renderscript.Element, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateHEMM(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void validateHEMM(android.renderscript.Element r1, int r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateHEMM(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateHEMM(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateHER2K(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void validateHER2K(android.renderscript.Element r1, int r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateHER2K(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateHER2K(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateHERK(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void validateHERK(android.renderscript.Element r1, int r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateHERK(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateHERK(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateL3(android.renderscript.Element, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void validateL3(android.renderscript.Element r1, int r2, int r3, int r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateL3(android.renderscript.Element, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateL3(android.renderscript.Element, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSPMV(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Allocation, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static int validateSPMV(android.renderscript.Element r1, int r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSPMV(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Allocation, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateSPMV(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Allocation, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSPR(android.renderscript.Element, int, android.renderscript.Allocation, int, android.renderscript.Allocation):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static int validateSPR(android.renderscript.Element r1, int r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSPR(android.renderscript.Element, int, android.renderscript.Allocation, int, android.renderscript.Allocation):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateSPR(android.renderscript.Element, int, android.renderscript.Allocation, int, android.renderscript.Allocation):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSPR2(android.renderscript.Element, int, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static int validateSPR2(android.renderscript.Element r1, int r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5, int r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSPR2(android.renderscript.Element, int, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateSPR2(android.renderscript.Element, int, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSYMV(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation, int, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static int validateSYMV(android.renderscript.Element r1, int r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSYMV(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation, int, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateSYMV(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation, int, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSYR(android.renderscript.Element, int, android.renderscript.Allocation, int, android.renderscript.Allocation):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static int validateSYR(android.renderscript.Element r1, int r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSYR(android.renderscript.Element, int, android.renderscript.Allocation, int, android.renderscript.Allocation):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateSYR(android.renderscript.Element, int, android.renderscript.Allocation, int, android.renderscript.Allocation):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSYR2(android.renderscript.Element, int, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static int validateSYR2(android.renderscript.Element r1, int r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5, int r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSYR2(android.renderscript.Element, int, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateSYR2(android.renderscript.Element, int, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSYR2K(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void validateSYR2K(android.renderscript.Element r1, int r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateSYR2K(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateSYR2K(android.renderscript.Element, int, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptIntrinsicBLAS.validateSide(int):void, dex: 
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
    static void validateSide(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptIntrinsicBLAS.validateSide(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateSide(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateTPMV(android.renderscript.Element, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static int validateTPMV(android.renderscript.Element r1, int r2, int r3, int r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateTPMV(android.renderscript.Element, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateTPMV(android.renderscript.Element, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateTRMM(android.renderscript.Element, int, int, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void validateTRMM(android.renderscript.Element r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateTRMM(android.renderscript.Element, int, int, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateTRMM(android.renderscript.Element, int, int, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateTRMV(android.renderscript.Element, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void validateTRMV(android.renderscript.Element r1, int r2, int r3, int r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateTRMV(android.renderscript.Element, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateTRMV(android.renderscript.Element, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateTRSM(android.renderscript.Element, int, int, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static void validateTRSM(android.renderscript.Element r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.renderscript.ScriptIntrinsicBLAS.validateTRSM(android.renderscript.Element, int, int, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateTRSM(android.renderscript.Element, int, int, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptIntrinsicBLAS.validateTranspose(int):void, dex: 
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
    static void validateTranspose(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptIntrinsicBLAS.validateTranspose(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateTranspose(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptIntrinsicBLAS.validateUplo(int):void, dex: 
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
    static void validateUplo(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.renderscript.ScriptIntrinsicBLAS.validateUplo(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.validateUplo(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.BNNM(android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation, int, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void BNNM(android.renderscript.Allocation r1, int r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5, int r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.BNNM(android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.BNNM(android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CGBMV(int, int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CGBMV(int r1, int r2, int r3, android.renderscript.Float2 r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7, android.renderscript.Float2 r8, android.renderscript.Allocation r9, int r10) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CGBMV(int, int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CGBMV(int, int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CGEMM(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CGEMM(int r1, int r2, android.renderscript.Float2 r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, android.renderscript.Float2 r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CGEMM(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CGEMM(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CGEMV(int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CGEMV(int r1, android.renderscript.Float2 r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, int r5, android.renderscript.Float2 r6, android.renderscript.Allocation r7, int r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CGEMV(int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CGEMV(int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CGERC(android.renderscript.Float2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CGERC(android.renderscript.Float2 r1, android.renderscript.Allocation r2, int r3, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CGERC(android.renderscript.Float2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CGERC(android.renderscript.Float2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CGERU(android.renderscript.Float2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CGERU(android.renderscript.Float2 r1, android.renderscript.Allocation r2, int r3, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CGERU(android.renderscript.Float2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CGERU(android.renderscript.Float2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHBMV(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CHBMV(int r1, int r2, android.renderscript.Float2 r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6, android.renderscript.Float2 r7, android.renderscript.Allocation r8, int r9) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHBMV(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CHBMV(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHEMM(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CHEMM(int r1, int r2, android.renderscript.Float2 r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, android.renderscript.Float2 r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHEMM(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CHEMM(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHEMV(int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CHEMV(int r1, android.renderscript.Float2 r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, int r5, android.renderscript.Float2 r6, android.renderscript.Allocation r7, int r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHEMV(int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CHEMV(int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHER(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CHER(int r1, float r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHER(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CHER(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHER2(int, android.renderscript.Float2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CHER2(int r1, android.renderscript.Float2 r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5, int r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHER2(int, android.renderscript.Float2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CHER2(int, android.renderscript.Float2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.CHER2K(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.CHER2K(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.CHER2K(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void CHER2K(int r1, int r2, android.renderscript.Float2 r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, float r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.CHER2K(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.CHER2K(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CHER2K(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHERK(int, int, float, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CHERK(int r1, int r2, float r3, android.renderscript.Allocation r4, float r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHERK(int, int, float, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CHERK(int, int, float, android.renderscript.Allocation, float, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHPMV(int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CHPMV(int r1, android.renderscript.Float2 r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, int r5, android.renderscript.Float2 r6, android.renderscript.Allocation r7, int r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHPMV(int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CHPMV(int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Float2, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHPR(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CHPR(int r1, float r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHPR(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CHPR(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHPR2(int, android.renderscript.Float2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CHPR2(int r1, android.renderscript.Float2 r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5, int r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CHPR2(int, android.renderscript.Float2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CHPR2(int, android.renderscript.Float2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.renderscript.ScriptIntrinsicBLAS.CSYMM(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CSYMM(int r1, int r2, android.renderscript.Float2 r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, android.renderscript.Float2 r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.renderscript.ScriptIntrinsicBLAS.CSYMM(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CSYMM(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CSYR2K(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CSYR2K(int r1, int r2, android.renderscript.Float2 r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, android.renderscript.Float2 r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CSYR2K(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CSYR2K(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CSYRK(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CSYRK(int r1, int r2, android.renderscript.Float2 r3, android.renderscript.Allocation r4, android.renderscript.Float2 r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CSYRK(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CSYRK(int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Float2, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CTBMV(int r1, int r2, int r3, int r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CTBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CTBSV(int r1, int r2, int r3, int r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CTBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CTPMV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CTPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CTPSV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CTPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTRMM(int, int, int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CTRMM(int r1, int r2, int r3, int r4, android.renderscript.Float2 r5, android.renderscript.Allocation r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTRMM(int, int, int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CTRMM(int, int, int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CTRMV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CTRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTRSM(int, int, int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CTRSM(int r1, int r2, int r3, int r4, android.renderscript.Float2 r5, android.renderscript.Allocation r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTRSM(int, int, int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CTRSM(int, int, int, int, android.renderscript.Float2, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void CTRSV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.CTRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.CTRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DGBMV(int, int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DGBMV(int r1, int r2, int r3, double r4, android.renderscript.Allocation r6, android.renderscript.Allocation r7, int r8, double r9, android.renderscript.Allocation r11, int r12) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DGBMV(int, int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DGBMV(int, int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DGEMM(int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DGEMM(int r1, int r2, double r3, android.renderscript.Allocation r5, android.renderscript.Allocation r6, double r7, android.renderscript.Allocation r9) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DGEMM(int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DGEMM(int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DGEMV(int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DGEMV(int r1, double r2, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6, double r7, android.renderscript.Allocation r9, int r10) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DGEMV(int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DGEMV(int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.renderscript.ScriptIntrinsicBLAS.DGER(double, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DGER(double r1, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5, int r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.renderscript.ScriptIntrinsicBLAS.DGER(double, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DGER(double, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSBMV(int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DSBMV(int r1, int r2, double r3, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7, double r8, android.renderscript.Allocation r10, int r11) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSBMV(int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DSBMV(int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSPMV(int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DSPMV(int r1, double r2, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6, double r7, android.renderscript.Allocation r9, int r10) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSPMV(int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DSPMV(int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSPR(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DSPR(int r1, double r2, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSPR(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DSPR(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSPR2(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DSPR2(int r1, double r2, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6, int r7, android.renderscript.Allocation r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSPR2(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DSPR2(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.renderscript.ScriptIntrinsicBLAS.DSYMM(int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DSYMM(int r1, int r2, double r3, android.renderscript.Allocation r5, android.renderscript.Allocation r6, double r7, android.renderscript.Allocation r9) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.renderscript.ScriptIntrinsicBLAS.DSYMM(int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DSYMM(int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSYMV(int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DSYMV(int r1, double r2, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6, double r7, android.renderscript.Allocation r9, int r10) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSYMV(int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DSYMV(int, double, android.renderscript.Allocation, android.renderscript.Allocation, int, double, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSYR(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DSYR(int r1, double r2, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSYR(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DSYR(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSYR2(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DSYR2(int r1, double r2, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6, int r7, android.renderscript.Allocation r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSYR2(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DSYR2(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSYR2K(int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DSYR2K(int r1, int r2, double r3, android.renderscript.Allocation r5, android.renderscript.Allocation r6, double r7, android.renderscript.Allocation r9) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSYR2K(int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DSYR2K(int, int, double, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSYRK(int, int, double, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DSYRK(int r1, int r2, double r3, android.renderscript.Allocation r5, double r6, android.renderscript.Allocation r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DSYRK(int, int, double, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DSYRK(int, int, double, android.renderscript.Allocation, double, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DTBMV(int r1, int r2, int r3, int r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DTBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DTBSV(int r1, int r2, int r3, int r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DTBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DTPMV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DTPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DTPSV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DTPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTRMM(int, int, int, int, double, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DTRMM(int r1, int r2, int r3, int r4, double r5, android.renderscript.Allocation r7, android.renderscript.Allocation r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTRMM(int, int, int, int, double, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DTRMM(int, int, int, int, double, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DTRMV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DTRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTRSM(int, int, int, int, double, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DTRSM(int r1, int r2, int r3, int r4, double r5, android.renderscript.Allocation r7, android.renderscript.Allocation r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTRSM(int, int, int, int, double, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DTRSM(int, int, int, int, double, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void DTRSV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.DTRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.DTRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SGBMV(int, int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SGBMV(int, int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.SGBMV(int, int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void SGBMV(int r1, int r2, int r3, float r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7, float r8, android.renderscript.Allocation r9, int r10) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SGBMV(int, int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SGBMV(int, int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SGBMV(int, int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.SGEMM(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void SGEMM(int r1, int r2, float r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, float r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.SGEMM(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SGEMM(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SGEMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SGEMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.SGEMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void SGEMV(int r1, float r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, int r5, float r6, android.renderscript.Allocation r7, int r8) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SGEMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SGEMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SGEMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SGER(float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SGER(float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.SGER(float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void SGER(float r1, android.renderscript.Allocation r2, int r3, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SGER(float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SGER(float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SGER(float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SSBMV(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SSBMV(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.SSBMV(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void SSBMV(int r1, int r2, float r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6, float r7, android.renderscript.Allocation r8, int r9) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SSBMV(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SSBMV(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SSBMV(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SSPMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SSPMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.SSPMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void SSPMV(int r1, float r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, int r5, float r6, android.renderscript.Allocation r7, int r8) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SSPMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SSPMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SSPMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.SSPR(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void SSPR(int r1, float r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.SSPR(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SSPR(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SSPR2(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SSPR2(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.SSPR2(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void SSPR2(int r1, float r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5, int r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SSPR2(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SSPR2(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SSPR2(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SSYMM(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SSYMM(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.SSYMM(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void SSYMM(int r1, int r2, float r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, float r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SSYMM(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SSYMM(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SSYMM(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SSYMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SSYMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.SSYMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void SSYMV(int r1, float r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, int r5, float r6, android.renderscript.Allocation r7, int r8) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SSYMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SSYMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SSYMV(int, float, android.renderscript.Allocation, android.renderscript.Allocation, int, float, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.SSYR(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void SSYR(int r1, float r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.SSYR(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SSYR(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SSYR2(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SSYR2(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.SSYR2(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void SSYR2(int r1, float r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5, int r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.SSYR2(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.SSYR2(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SSYR2(int, float, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.SSYR2K(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void SSYR2K(int r1, int r2, float r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, float r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.SSYR2K(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SSYR2K(int, int, float, android.renderscript.Allocation, android.renderscript.Allocation, float, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.SSYRK(int, int, float, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void SSYRK(int r1, int r2, float r3, android.renderscript.Allocation r4, float r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.SSYRK(int, int, float, android.renderscript.Allocation, float, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.SSYRK(int, int, float, android.renderscript.Allocation, float, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.STBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.STBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.STBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void STBMV(int r1, int r2, int r3, int r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.STBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.STBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.STBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.STBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.STBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.STBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void STBSV(int r1, int r2, int r3, int r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.STBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.STBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.STBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.STPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.STPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.STPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void STPMV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.STPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.STPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.STPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.STPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.STPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.STPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void STPSV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.STPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.STPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.STPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.STRMM(int, int, int, int, float, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void STRMM(int r1, int r2, int r3, int r4, float r5, android.renderscript.Allocation r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.STRMM(int, int, int, int, float, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.STRMM(int, int, int, int, float, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.STRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.STRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.STRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void STRMV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.STRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.STRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.STRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.STRSM(int, int, int, int, float, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void STRSM(int r1, int r2, int r3, int r4, float r5, android.renderscript.Allocation r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.STRSM(int, int, int, int, float, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.STRSM(int, int, int, int, float, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.STRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.STRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.STRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void STRSV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.STRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.STRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.STRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZGBMV(int, int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZGBMV(int r1, int r2, int r3, android.renderscript.Double2 r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7, android.renderscript.Double2 r8, android.renderscript.Allocation r9, int r10) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZGBMV(int, int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZGBMV(int, int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZGEMM(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZGEMM(int r1, int r2, android.renderscript.Double2 r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, android.renderscript.Double2 r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZGEMM(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZGEMM(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZGEMV(int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZGEMV(int r1, android.renderscript.Double2 r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, int r5, android.renderscript.Double2 r6, android.renderscript.Allocation r7, int r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZGEMV(int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZGEMV(int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZGERC(android.renderscript.Double2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZGERC(android.renderscript.Double2 r1, android.renderscript.Allocation r2, int r3, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZGERC(android.renderscript.Double2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZGERC(android.renderscript.Double2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZGERU(android.renderscript.Double2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZGERU(android.renderscript.Double2 r1, android.renderscript.Allocation r2, int r3, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZGERU(android.renderscript.Double2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZGERU(android.renderscript.Double2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHBMV(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZHBMV(int r1, int r2, android.renderscript.Double2 r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6, android.renderscript.Double2 r7, android.renderscript.Allocation r8, int r9) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHBMV(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZHBMV(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHEMM(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZHEMM(int r1, int r2, android.renderscript.Double2 r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, android.renderscript.Double2 r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHEMM(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZHEMM(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHEMV(int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZHEMV(int r1, android.renderscript.Double2 r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, int r5, android.renderscript.Double2 r6, android.renderscript.Allocation r7, int r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHEMV(int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZHEMV(int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHER(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZHER(int r1, double r2, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHER(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZHER(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHER2(int, android.renderscript.Double2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZHER2(int r1, android.renderscript.Double2 r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5, int r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHER2(int, android.renderscript.Double2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZHER2(int, android.renderscript.Double2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.ZHER2K(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.ZHER2K(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.renderscript.ScriptIntrinsicBLAS.ZHER2K(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void ZHER2K(int r1, int r2, android.renderscript.Double2 r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, double r6, android.renderscript.Allocation r8) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.renderscript.ScriptIntrinsicBLAS.ZHER2K(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex:  in method: android.renderscript.ScriptIntrinsicBLAS.ZHER2K(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZHER2K(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, double, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHERK(int, int, double, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZHERK(int r1, int r2, double r3, android.renderscript.Allocation r5, double r6, android.renderscript.Allocation r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHERK(int, int, double, android.renderscript.Allocation, double, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZHERK(int, int, double, android.renderscript.Allocation, double, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHPMV(int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZHPMV(int r1, android.renderscript.Double2 r2, android.renderscript.Allocation r3, android.renderscript.Allocation r4, int r5, android.renderscript.Double2 r6, android.renderscript.Allocation r7, int r8) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHPMV(int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZHPMV(int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, int, android.renderscript.Double2, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHPR(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZHPR(int r1, double r2, android.renderscript.Allocation r4, int r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHPR(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZHPR(int, double, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHPR2(int, android.renderscript.Double2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZHPR2(int r1, android.renderscript.Double2 r2, android.renderscript.Allocation r3, int r4, android.renderscript.Allocation r5, int r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZHPR2(int, android.renderscript.Double2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZHPR2(int, android.renderscript.Double2, android.renderscript.Allocation, int, android.renderscript.Allocation, int, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: android.renderscript.ScriptIntrinsicBLAS.ZSYMM(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZSYMM(int r1, int r2, android.renderscript.Double2 r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, android.renderscript.Double2 r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: android.renderscript.ScriptIntrinsicBLAS.ZSYMM(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZSYMM(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZSYR2K(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZSYR2K(int r1, int r2, android.renderscript.Double2 r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, android.renderscript.Double2 r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZSYR2K(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZSYR2K(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZSYRK(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZSYRK(int r1, int r2, android.renderscript.Double2 r3, android.renderscript.Allocation r4, android.renderscript.Double2 r5, android.renderscript.Allocation r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZSYRK(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZSYRK(int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Double2, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZTBMV(int r1, int r2, int r3, int r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZTBMV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZTBSV(int r1, int r2, int r3, int r4, android.renderscript.Allocation r5, android.renderscript.Allocation r6, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZTBSV(int, int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZTPMV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZTPMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZTPSV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZTPSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTRMM(int, int, int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZTRMM(int r1, int r2, int r3, int r4, android.renderscript.Double2 r5, android.renderscript.Allocation r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTRMM(int, int, int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZTRMM(int, int, int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZTRMV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZTRMV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTRSM(int, int, int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZTRSM(int r1, int r2, int r3, int r4, android.renderscript.Double2 r5, android.renderscript.Allocation r6, android.renderscript.Allocation r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTRSM(int, int, int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZTRSM(int, int, int, int, android.renderscript.Double2, android.renderscript.Allocation, android.renderscript.Allocation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void ZTRSV(int r1, int r2, int r3, android.renderscript.Allocation r4, android.renderscript.Allocation r5, int r6) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.renderscript.ScriptIntrinsicBLAS.ZTRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.renderscript.ScriptIntrinsicBLAS.ZTRSV(int, int, int, android.renderscript.Allocation, android.renderscript.Allocation, int):void");
    }
}
