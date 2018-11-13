package com.oppo.eyeprotect;

import android.util.Spline;
import java.util.List;

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
public class RGBCoeffAlgorithm {
    private static final int BRIGHTNESS_HIGH = 0;
    public static final String BRIGHTNESS_LEVEL_PATH = "/sys/class/leds/lcd-backlight/brightness";
    private static final int BRIGHTNESS_LOW = 2;
    private static final int BRIGHTNESS_MID = 1;
    private static boolean DEBUG_PANIC = false;
    private static final double DEFAULT_COEFFICIENT = 1.0d;
    private static final float K_COEFFICIENT = 0.08163f;
    private static final int MAX_BRIGHTNESS = 0;
    public static final String MAX_BRIGHTNESS_PATH = "/sys/class/leds/lcd-backlight/max_brightness";
    public static final double PI = 3.141592653589793d;
    private static final String TAG = "RGBCoeffAlgorithm";
    public static final int TYPE_X = 0;
    public static final int TYPE_Y = 1;
    public static final int TYPE_Z = 2;
    public static final String XML_PATH = null;
    public static int brightness_index;
    public static List<Node> list;

    static class Node {
        public float[] blue;
        public Spline blue_spline;
        public int brightness;
        public float[] color_temp;
        public float[] green;
        public Spline green_spline;
        public float[] red;
        public Spline red_spline;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.Node.<init>():void, dex:  in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.Node.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.Node.<init>():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public Node() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.Node.<init>():void, dex:  in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.Node.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.Node.<init>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.<init>():void, dex: 
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
    public RGBCoeffAlgorithm() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.XYZ2RGB(float, float, float):android.graphics.Matrix, dex: 
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
    public static android.graphics.Matrix XYZ2RGB(float r1, float r2, float r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.XYZ2RGB(float, float, float):android.graphics.Matrix, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.XYZ2RGB(float, float, float):android.graphics.Matrix");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.cct2level(float):float, dex: 
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
    public static float cct2level(float r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.cct2level(float):float, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.cct2level(float):float");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.getBrightnessLevel():int, dex: 
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
    public static int getBrightnessLevel() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.getBrightnessLevel():int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.getBrightnessLevel():int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.getCurrentMin():int, dex: 
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
    public static int getCurrentMin() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.getCurrentMin():int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.getCurrentMin():int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.getDataPath():java.lang.String, dex: 
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
    private static java.lang.String getDataPath() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.getDataPath():java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.getDataPath():java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.getMaxBrightness():int, dex: 
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
    public static int getMaxBrightness() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.getMaxBrightness():int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.getMaxBrightness():int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.getNormalCCT():int, dex: 
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
    public static int getNormalCCT() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.getNormalCCT():int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.getNormalCCT():int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.initSpline():void, dex: 
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
    public static void initSpline() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.initSpline():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.initSpline():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.interpolate(float):com.oppo.eyeprotect.RGB, dex: 
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
    public static com.oppo.eyeprotect.RGB interpolate(float r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.interpolate(float):com.oppo.eyeprotect.RGB, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.interpolate(float):com.oppo.eyeprotect.RGB");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.inverseXYX2RGB():android.graphics.Matrix, dex: 
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
    public static android.graphics.Matrix inverseXYX2RGB() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.inverseXYX2RGB():android.graphics.Matrix, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.inverseXYX2RGB():android.graphics.Matrix");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.parserXmlData():java.util.List<com.oppo.eyeprotect.RGBCoeffAlgorithm$Node>, dex: 
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
    private static java.util.List<com.oppo.eyeprotect.RGBCoeffAlgorithm.Node> parserXmlData() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.parserXmlData():java.util.List<com.oppo.eyeprotect.RGBCoeffAlgorithm$Node>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.parserXmlData():java.util.List<com.oppo.eyeprotect.RGBCoeffAlgorithm$Node>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.readFromFile(java.io.File):java.lang.String, dex: 
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
    private static java.lang.String readFromFile(java.io.File r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.RGBCoeffAlgorithm.readFromFile(java.io.File):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.RGBCoeffAlgorithm.readFromFile(java.io.File):java.lang.String");
    }

    public static double calculate_B_Coeff(int blueV, int N, int mode) {
        if (mode == 0) {
            return (((0.38814d / ((double) N)) * ((double) blueV)) + 0.32343d) * 0.88d;
        }
        if (mode != 1 && mode == 2) {
        }
        return DEFAULT_COEFFICIENT;
    }

    public static double calculate_G_Coeff(int blueV, double blue, int N, int mode) {
        if (mode == 0) {
            return (((0.234d / ((double) N)) * ((double) blueV)) + 0.6464d) * 0.87d;
        }
        if (mode != 1 && mode == 2) {
        }
        return DEFAULT_COEFFICIENT;
    }

    public static double calculate_R_Coeff(int blueV, double blue, int N, int mode) {
        if (mode == 0) {
            return (((0.79d - Math.log(2.508281828459045d)) / ((double) N)) * ((double) blueV)) + Math.log(2.618281828459045d);
        }
        if (mode != 1 && mode == 2) {
        }
        return DEFAULT_COEFFICIENT;
    }

    public static float[] normalizedXYZ(float X, float Y, float Z) {
        float[] newXYZ = new float[3];
        newXYZ[0] = X * K_COEFFICIENT;
        newXYZ[1] = Y * K_COEFFICIENT;
        newXYZ[2] = Z * K_COEFFICIENT;
        return newXYZ;
    }

    public static int[] xyz2rgb(float X, float Y, float Z) {
        float var_X = X / 100.0f;
        float var_Y = Y / 100.0f;
        float var_Z = Z / 100.0f;
        float var_R = ((3.2406f * var_X) + (-1.5372f * var_Y)) + (-0.4986f * var_Z);
        float var_G = ((-0.9689f * var_X) + (1.8758f * var_Y)) + (0.0415f * var_Z);
        float var_B = ((0.0557f * var_X) + (-0.204f * var_Y)) + (1.057f * var_Z);
        if (((double) var_R) > 0.0031308d) {
            var_R = (((float) Math.pow((double) var_R, 0.4166666567325592d)) * 1.055f) - 0.055f;
        } else {
            var_R *= 12.92f;
        }
        if (((double) var_G) > 0.0031308d) {
            var_G = (((float) Math.pow((double) var_G, 0.4166666567325592d)) * 1.055f) - 0.055f;
        } else {
            var_G *= 12.92f;
        }
        if (((double) var_B) > 0.0031308d) {
            var_B = (((float) Math.pow((double) var_B, 0.4166666567325592d)) * 1.055f) - 0.055f;
        } else {
            var_B *= 12.92f;
        }
        int G = (int) (255.0f * var_G);
        int B = (int) (255.0f * var_B);
        int[] RGB = new int[3];
        RGB[0] = (int) (255.0f * var_R);
        RGB[1] = G;
        RGB[2] = B;
        return RGB;
    }

    public static float xy2CCT(float x, float y) {
        float n = (x - 0.332f) / (0.1858f - y);
        return (float) ((((Math.pow((double) n, 3.0d) * 437.0d) + (Math.pow((double) n, 2.0d) * 3601.0d)) + ((double) (6831.0f * n))) + 5517.0d);
    }

    public static int time4CCT(int hour, int min) {
        return time4CCT(hourMin4Min(hour, min));
    }

    public static int hourMin4Min(int hour, int min) {
        if (hour > 24 || hour < 0) {
            hour = 0;
        }
        if (min > 60 || min < 0) {
            min = 0;
        }
        return (hour * 60) + min;
    }

    public static int time4CCT(int timeMin) {
        if (timeMin >= 0 && timeMin < 120) {
            return 2950;
        }
        if (timeMin >= 120 && timeMin < 600) {
            return (int) ((((Math.pow((double) timeMin, 3.0d) * -3.55971E-5d) + (Math.pow((double) timeMin, 2.0d) * 0.042264746d)) - (((double) timeMin) * 7.5642060969d)) + 3318.3864366689d);
        }
        if (timeMin >= 600 && timeMin <= 960) {
            return 6300;
        }
        if (timeMin > 960 && timeMin <= 1320) {
            return (int) (((((Math.pow((double) timeMin, 4.0d) * 1.634E-7d) - (Math.pow((double) timeMin, 3.0d) * 6.65698E-4d)) + (Math.pow((double) timeMin, 2.0d) * 1.0004234776d)) - (((double) timeMin) * 663.6963176875d)) + 171651.9766941347d);
        }
        if (timeMin <= 1320 || timeMin > 1440) {
            return 4300;
        }
        return (int) (((Math.pow((double) timeMin, 2.0d) * 0.04956058d) - ((double) (((float) timeMin) * 143.14984f))) + 106319.1580056127d);
    }

    private static float[] StringArray2FloatArray(String[] str) {
        int len = str.length;
        float[] a = new float[len];
        for (int i = 0; i < len; i++) {
            a[i] = Float.parseFloat(str[i]);
        }
        return a;
    }
}
