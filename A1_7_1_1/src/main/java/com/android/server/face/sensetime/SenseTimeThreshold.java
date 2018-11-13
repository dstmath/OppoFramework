package com.android.server.face.sensetime;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class SenseTimeThreshold {
    public static final String DIR = null;
    public static final int DOWN_MAXIMUM = 20;
    public static final int DOWN_MINIMUM = 5;
    public static final int ENROLL_MAX_PITCH = 13;
    public static final int ENROLL_MAX_YAW = 8;
    public static final int ENROLL_MIN_PITCH = -8;
    public static final int ENROLL_MIN_YAW = -8;
    public static final String HACKER_MODEL = "M_Liveness_Antispoofing_General_6.0.6_14700201_24320401_half.model";
    public static final float HACKER_THRESHOLD = 0.95f;
    public static final int LEFT_MAXIMUM = -10;
    public static final int LEFT_MINIMUM = -45;
    public static final float MAX_BRIGHT = 0.83f;
    public static final float MAX_ENROLL_ROLL_ANGLE = -45.0f;
    public static final float MAX_FACE_SCALE = 0.84f;
    public static final int MAX_PITCH = 25;
    public static final float MAX_VERIFY_ROLL_ANGLE = 45.0f;
    public static final int MAX_YAW = 45;
    public static final float MIN_BRIGHT = 0.17f;
    public static final float MIN_ENROLL_ROLL_ANGLE = -135.0f;
    public static final float MIN_FACE_SCALE = 0.33f;
    public static final int MIN_PITCH = -30;
    public static final float MIN_QUALITY = 2.0f;
    public static final float MIN_VERIFY_ROLL_ANGLE = -45.0f;
    public static final int RIGHT_MAXIMUM = 45;
    public static final int RIGHT_MINIMUM = 10;
    public static final int TRACK_INTERVAL = 1;
    public static final int UP_MAXIMUM = -5;
    public static final int UP_MINIMUM = -20;
    public static final String VERIFY_MODEL = "M_Verify_MimicRes_Common_3.4.0_10000101_50001201_half.model";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.face.sensetime.SenseTimeThreshold.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.face.sensetime.SenseTimeThreshold.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.face.sensetime.SenseTimeThreshold.<clinit>():void");
    }
}
