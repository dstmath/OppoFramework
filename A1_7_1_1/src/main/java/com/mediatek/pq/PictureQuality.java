package com.mediatek.pq;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class PictureQuality {
    private static final String BLUELIGHT_DEFAULT_PROPERTY_NAME = "persist.sys.bluelight.default";
    public static final int CAPABILITY_MASK_COLOR = 1;
    public static final int CAPABILITY_MASK_DC = 8;
    public static final int CAPABILITY_MASK_GAMMA = 4;
    public static final int CAPABILITY_MASK_OD = 16;
    public static final int CAPABILITY_MASK_SHARPNESS = 2;
    private static final String CHAMELEON_DEFAULT_PROPERTY_NAME = "persist.sys.chameleon.default";
    public static final int DCHIST_INFO_NUM = 20;
    private static final String GAMMA_INDEX_PROPERTY_NAME = "persist.sys.gamma.index";
    public static final int GAMMA_LUT_SIZE = 512;
    public static final int MODE_CAMERA = 1;
    public static final int MODE_MASK = 1;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_VIDEO = 2;
    public static final int PIC_MODE_STANDARD = 0;
    public static final int PIC_MODE_USER_DEF = 2;
    public static final int PIC_MODE_VIVID = 1;
    static boolean sLibStatus;

    public static class GammaLut {
        public int hwid;
        public int[] lut;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.pq.PictureQuality.GammaLut.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public GammaLut() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.pq.PictureQuality.GammaLut.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.GammaLut.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.mediatek.pq.PictureQuality.GammaLut.set(int, int):void, dex:  in method: com.mediatek.pq.PictureQuality.GammaLut.set(int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.mediatek.pq.PictureQuality.GammaLut.set(int, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:61)
            	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void set(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.mediatek.pq.PictureQuality.GammaLut.set(int, int):void, dex:  in method: com.mediatek.pq.PictureQuality.GammaLut.set(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.GammaLut.set(int, int):void");
        }
    }

    public static class Hist {
        public int[] info;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.pq.PictureQuality.Hist.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public Hist() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.pq.PictureQuality.Hist.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.Hist.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.mediatek.pq.PictureQuality.Hist.set(int, int):void, dex:  in method: com.mediatek.pq.PictureQuality.Hist.set(int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.mediatek.pq.PictureQuality.Hist.set(int, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:61)
            	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void set(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.mediatek.pq.PictureQuality.Hist.set(int, int):void, dex:  in method: com.mediatek.pq.PictureQuality.Hist.set(int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.Hist.set(int, int):void");
        }
    }

    public static class Range {
        public int defaultValue;
        public int max;
        public int min;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.pq.PictureQuality.Range.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public Range() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.pq.PictureQuality.Range.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.Range.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.mediatek.pq.PictureQuality.Range.set(int, int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void set(int r1, int r2, int r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.mediatek.pq.PictureQuality.Range.set(int, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.Range.set(int, int, int):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.<init>():void, dex: 
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
    public PictureQuality() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.pq.PictureQuality.getBlueLightStrengthRange():com.mediatek.pq.PictureQuality$Range, dex: 
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
    public static com.mediatek.pq.PictureQuality.Range getBlueLightStrengthRange() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.pq.PictureQuality.getBlueLightStrengthRange():com.mediatek.pq.PictureQuality$Range, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.getBlueLightStrengthRange():com.mediatek.pq.PictureQuality$Range");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.pq.PictureQuality.getChameleonStrengthRange():com.mediatek.pq.PictureQuality$Range, dex: 
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
    public static com.mediatek.pq.PictureQuality.Range getChameleonStrengthRange() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.pq.PictureQuality.getChameleonStrengthRange():com.mediatek.pq.PictureQuality$Range, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.getChameleonStrengthRange():com.mediatek.pq.PictureQuality$Range");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.mediatek.pq.PictureQuality.getGammaIndex():int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public static int getGammaIndex() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.mediatek.pq.PictureQuality.getGammaIndex():int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.getGammaIndex():int");
    }

    private static native boolean nativeEnableBlueLight(boolean z);

    private static native boolean nativeEnableChameleon(boolean z);

    private static native boolean nativeEnableColor(int i);

    private static native boolean nativeEnableColorEffect(int i);

    private static native boolean nativeEnableContentColor(int i);

    private static native boolean nativeEnableDynamicContrast(int i);

    private static native boolean nativeEnableDynamicSharpness(int i);

    private static native boolean nativeEnableGamma(int i);

    private static native boolean nativeEnableISOAdaptiveSharpness(int i);

    private static native boolean nativeEnableOD(int i);

    private static native boolean nativeEnablePQ(int i);

    private static native boolean nativeEnableSharpness(int i);

    private static native boolean nativeEnableUltraResolution(int i);

    private static native boolean nativeEnableVideoHDR(boolean z);

    private static native int nativeGetBlueLightStrength();

    private static native int nativeGetCapability();

    private static native int nativeGetChameleonStrength();

    private static native int nativeGetColorEffectIndex();

    private static native void nativeGetColorEffectIndexRange(Range range);

    private static native int nativeGetContrastIndex();

    private static native void nativeGetContrastIndexRange(Range range);

    private static native int nativeGetDefaultTransitionStep();

    private static native void nativeGetDynamicContrastHistogram(byte[] bArr, int i, int i2, Hist hist);

    private static native int nativeGetDynamicContrastIndex();

    private static native void nativeGetDynamicContrastIndexRange(Range range);

    private static native void nativeGetGammaIndexRange(Range range);

    private static native int nativeGetGlobalPQStrength();

    private static native int nativeGetGlobalPQStrengthRange();

    private static native int nativeGetGlobalPQSwitch();

    private static native int nativeGetPicBrightnessIndex();

    private static native void nativeGetPicBrightnessIndexRange(Range range);

    private static native int nativeGetPictureMode();

    private static native int nativeGetSaturationIndex();

    private static native void nativeGetSaturationIndexRange(Range range);

    private static native int nativeGetSharpnessIndex();

    private static native void nativeGetSharpnessIndexRange(Range range);

    private static native boolean nativeIsBlueLightEnabled();

    private static native boolean nativeIsChameleonEnabled();

    private static native boolean nativeIsVideoHDREnabled();

    private static native void nativeSetAALFunction(int i);

    private static native boolean nativeSetBlueLightStrength(int i, int i2);

    private static native void nativeSetCameraPreviewMode();

    private static native boolean nativeSetChameleonStrength(int i, int i2);

    private static native void nativeSetColorEffectIndex(int i);

    private static native boolean nativeSetColorRegion(int i, int i2, int i3, int i4, int i5);

    private static native void nativeSetContrastIndex(int i, int i2);

    private static native void nativeSetDynamicContrastIndex(int i);

    private static native void nativeSetGalleryNormalMode();

    private static native void nativeSetGammaIndex(int i);

    private static native boolean nativeSetGlobalPQStrength(int i);

    private static native boolean nativeSetGlobalPQSwitch(int i);

    private static native void nativeSetPicBrightnessIndex(int i, int i2);

    private static native boolean nativeSetPictureMode(int i, int i2);

    private static native boolean nativeSetRGBGain(int i, int i2, int i3, int i4);

    private static native void nativeSetSaturationIndex(int i, int i2);

    private static native void nativeSetSharpnessIndex(int i);

    private static native void nativeSetVideoPlaybackMode();

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setAALFunction(int):void, dex: 
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
    public static void setAALFunction(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setAALFunction(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.setAALFunction(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setColorEffectIndex(int):void, dex: 
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
    public static void setColorEffectIndex(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setColorEffectIndex(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.setColorEffectIndex(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setContrastIndex(int):void, dex: 
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
    public static void setContrastIndex(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setContrastIndex(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.setContrastIndex(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setContrastIndex(int, int):void, dex: 
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
    public static void setContrastIndex(int r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setContrastIndex(int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.setContrastIndex(int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setDynamicContrastIndex(int):void, dex: 
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
    public static void setDynamicContrastIndex(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setDynamicContrastIndex(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.setDynamicContrastIndex(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setGammaIndex(int):void, dex: 
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
    public static void setGammaIndex(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setGammaIndex(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.setGammaIndex(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setPicBrightnessIndex(int):void, dex: 
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
    public static void setPicBrightnessIndex(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setPicBrightnessIndex(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.setPicBrightnessIndex(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setPicBrightnessIndex(int, int):void, dex: 
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
    public static void setPicBrightnessIndex(int r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setPicBrightnessIndex(int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.setPicBrightnessIndex(int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setSaturationIndex(int):void, dex: 
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
    public static void setSaturationIndex(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setSaturationIndex(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.setSaturationIndex(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setSaturationIndex(int, int):void, dex: 
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
    public static void setSaturationIndex(int r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setSaturationIndex(int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.setSaturationIndex(int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setSharpnessIndex(int):void, dex: 
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
    public static void setSharpnessIndex(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.pq.PictureQuality.setSharpnessIndex(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.pq.PictureQuality.setSharpnessIndex(int):void");
    }

    public static boolean getLibStatus() {
        return sLibStatus;
    }

    public static int getCapability() {
        return nativeGetCapability();
    }

    public static String setMode(int mode) {
        if (mode == 1) {
            nativeSetCameraPreviewMode();
        } else if (mode == 2) {
            nativeSetVideoPlaybackMode();
        } else {
            nativeSetGalleryNormalMode();
        }
        return null;
    }

    public static Hist getDynamicContrastHistogram(byte[] srcBuffer, int srcWidth, int srcHeight) {
        Hist outHist = new Hist();
        nativeGetDynamicContrastHistogram(srcBuffer, srcWidth, srcHeight, outHist);
        return outHist;
    }

    public static boolean enablePQ(int isEnable) {
        return nativeEnablePQ(isEnable);
    }

    public static boolean enableColor(int isEnable) {
        return nativeEnableColor(isEnable);
    }

    public static boolean enableContentColor(int isEnable) {
        return nativeEnableContentColor(isEnable);
    }

    public static boolean enableSharpness(int isEnable) {
        return nativeEnableSharpness(isEnable);
    }

    public static boolean enableDynamicContrast(int isEnable) {
        return nativeEnableDynamicContrast(isEnable);
    }

    public static boolean enableDynamicSharpness(int isEnable) {
        return nativeEnableDynamicSharpness(isEnable);
    }

    public static boolean enableColorEffect(int isEnable) {
        return nativeEnableColorEffect(isEnable);
    }

    public static boolean enableGamma(int isEnable) {
        return nativeEnableGamma(isEnable);
    }

    public static boolean enableOD(int isEnable) {
        return nativeEnableOD(isEnable);
    }

    public static boolean enableISOAdaptiveSharpness(int isEnable) {
        return nativeEnableISOAdaptiveSharpness(isEnable);
    }

    public static boolean enableUltraResolution(int isEnable) {
        return nativeEnableUltraResolution(isEnable);
    }

    public static int getPictureMode() {
        return nativeGetPictureMode();
    }

    public static boolean setPictureMode(int mode, int step) {
        return nativeSetPictureMode(mode, step);
    }

    public static boolean setPictureMode(int mode) {
        return nativeSetPictureMode(mode, getDefaultTransitionStep());
    }

    public static boolean setColorRegion(int isEnable, int startX, int startY, int endX, int endY) {
        return nativeSetColorRegion(isEnable, startX, startY, endX, endY);
    }

    public static Range getContrastIndexRange() {
        Range r = new Range();
        nativeGetContrastIndexRange(r);
        return r;
    }

    public static int getContrastIndex() {
        return nativeGetContrastIndex();
    }

    public static Range getSaturationIndexRange() {
        Range r = new Range();
        nativeGetSaturationIndexRange(r);
        return r;
    }

    public static int getSaturationIndex() {
        return nativeGetSaturationIndex();
    }

    public static Range getPicBrightnessIndexRange() {
        Range r = new Range();
        nativeGetPicBrightnessIndexRange(r);
        return r;
    }

    public static int getPicBrightnessIndex() {
        return nativeGetPicBrightnessIndex();
    }

    public static Range getSharpnessIndexRange() {
        Range r = new Range();
        nativeGetSharpnessIndexRange(r);
        return r;
    }

    public static int getSharpnessIndex() {
        return nativeGetSharpnessIndex();
    }

    public static Range getDynamicContrastIndexRange() {
        Range r = new Range();
        nativeGetDynamicContrastIndexRange(r);
        return r;
    }

    public static int getDynamicContrastIndex() {
        return nativeGetDynamicContrastIndex();
    }

    public static Range getColorEffectIndexRange() {
        Range r = new Range();
        nativeGetColorEffectIndexRange(r);
        return r;
    }

    public static int getColorEffectIndex() {
        return nativeGetColorEffectIndex();
    }

    public static Range getGammaIndexRange() {
        Range r = new Range();
        nativeGetGammaIndexRange(r);
        return r;
    }

    public static boolean setBlueLightStrength(int strength, int step) {
        return nativeSetBlueLightStrength(strength, step);
    }

    public static boolean setBlueLightStrength(int strength) {
        return nativeSetBlueLightStrength(strength, getDefaultTransitionStep());
    }

    public static int getBlueLightStrength() {
        return nativeGetBlueLightStrength();
    }

    public static boolean enableBlueLight(boolean enable) {
        return nativeEnableBlueLight(enable);
    }

    public static boolean isBlueLightEnabled() {
        return nativeIsBlueLightEnabled();
    }

    public static boolean setChameleonStrength(int strength, int step) {
        return nativeSetChameleonStrength(strength, step);
    }

    public static boolean setChameleonStrength(int strength) {
        return nativeSetChameleonStrength(strength, getDefaultTransitionStep());
    }

    public static int getDefaultTransitionStep() {
        return nativeGetDefaultTransitionStep();
    }

    public static int getChameleonStrength() {
        return nativeGetChameleonStrength();
    }

    public static boolean enableChameleon(boolean enable) {
        return nativeEnableChameleon(enable);
    }

    public static boolean isChameleonEnabled() {
        return nativeIsChameleonEnabled();
    }

    public static boolean setGlobalPQSwitch(int globalPQSwitch) {
        return nativeSetGlobalPQSwitch(globalPQSwitch);
    }

    public static int getGlobalPQSwitch() {
        return nativeGetGlobalPQSwitch();
    }

    public static boolean setGlobalPQStrength(int globalPQStrength) {
        return nativeSetGlobalPQStrength(globalPQStrength);
    }

    public static int getGlobalPQStrength() {
        return nativeGetGlobalPQStrength();
    }

    public static int getGlobalPQStrengthRange() {
        return nativeGetGlobalPQStrengthRange();
    }

    public static boolean enableVideoHDR(boolean enable) {
        return nativeEnableVideoHDR(enable);
    }

    public static boolean isVideoHDREnabled() {
        return nativeIsVideoHDREnabled();
    }

    public static boolean setRGBGain(double r_gain, double g_gain, double b_gain, int step) {
        return nativeSetRGBGain((int) (r_gain * 1024.0d), (int) (g_gain * 1024.0d), (int) (b_gain * 1024.0d), step);
    }
}
