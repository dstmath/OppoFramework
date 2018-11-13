package org.gsma.joyn;

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
public class H264Config {
    public static final int BIT_RATE = 64000;
    public static final int CIF_HEIGHT = 288;
    public static final int CIF_WIDTH = 352;
    public static final int CLOCK_RATE = 90000;
    public static final String CODEC_NAME = "H264";
    public static final String CODEC_PARAMS = "profile-level-id=42900b;packetization-mode=1";
    public static final String CODEC_PARAM_PACKETIZATIONMODE = "packetization-mode";
    public static final String CODEC_PARAM_PROFILEID = "profile-level-id";
    public static final String CODEC_PARAM_SPROP_PARAMETER_SETS = "sprop-parameter-sets";
    public static final int FRAME_RATE = 15;
    public static final int QCIF_HEIGHT = 144;
    public static final int QCIF_WIDTH = 176;
    public static final int QVGA_HEIGHT = 240;
    public static final int QVGA_WIDTH = 320;
    public static final int VGA_HEIGHT = 480;
    public static final int VGA_WIDTH = 640;
    public static final int VIDEO_HEIGHT = 144;
    public static final int VIDEO_WIDTH = 176;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.gsma.joyn.H264Config.<init>():void, dex: 
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
    public H264Config() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.gsma.joyn.H264Config.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.H264Config.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.H264Config.getParameterValue(java.lang.String, java.lang.String):java.lang.String, dex: 
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
    private static java.lang.String getParameterValue(java.lang.String r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: org.gsma.joyn.H264Config.getParameterValue(java.lang.String, java.lang.String):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.H264Config.getParameterValue(java.lang.String, java.lang.String):java.lang.String");
    }

    public static int getCodecPacketizationMode(String codecParams) {
        int packetization_mode = 0;
        String valPackMode = getParameterValue(CODEC_PARAM_PACKETIZATIONMODE, codecParams);
        if (valPackMode == null) {
            return packetization_mode;
        }
        try {
            return Integer.parseInt(valPackMode);
        } catch (Exception e) {
            return packetization_mode;
        }
    }

    public static String getCodecProfileLevelId(String codecParams) {
        return getParameterValue(CODEC_PARAM_PROFILEID, codecParams);
    }
}
