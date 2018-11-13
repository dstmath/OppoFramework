package com.mediatek.hdmi;

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
public class HdmiDef {
    public static final int AUDIO_OUTPUT_MULTICHANNEL = 6;
    public static final int AUDIO_OUTPUT_STEREO = 2;
    public static final int AUTO = 100;
    public static final int CAPABILITY_MUTEX_CALL = 4;
    public static final int CAPABILITY_RDMA_LIMIT = 2;
    public static final int CAPABILITY_SCALE_ADJUST = 1;
    public static final int DISPLAY_TYPE_HDMI = 0;
    public static final int DISPLAY_TYPE_MHL = 2;
    public static final int DISPLAY_TYPE_SLIMPORT = 3;
    public static final int DISPLAY_TYPE_SMB = 1;
    public static final int HDMI_MAX_BITWIDTH = 3072;
    public static final int HDMI_MAX_BITWIDTH_OFFSETS = 10;
    public static final int HDMI_MAX_CHANNEL = 120;
    public static final int HDMI_MAX_CHANNEL_OFFSETS = 3;
    public static final int HDMI_MAX_SAMPLERATE = 896;
    public static final int HDMI_MAX_SAMPLERATE_OFFSETS = 7;
    public static final int RESOLUTION_1280X720P3D_50HZ = 14;
    public static final int RESOLUTION_1280X720P3D_60HZ = 13;
    public static final int RESOLUTION_1280X720P_50HZ = 3;
    public static final int RESOLUTION_1280X720P_60HZ = 2;
    public static final int RESOLUTION_1920X1080I3D_50HZ = 16;
    public static final int RESOLUTION_1920X1080I3D_60HZ = 15;
    public static final int RESOLUTION_1920X1080I_50HZ = 5;
    public static final int RESOLUTION_1920X1080I_60HZ = 4;
    public static final int RESOLUTION_1920X1080P3D_23HZ = 18;
    public static final int RESOLUTION_1920X1080P3D_24HZ = 17;
    public static final int RESOLUTION_1920X1080P_23HZ = 9;
    public static final int RESOLUTION_1920X1080P_24HZ = 8;
    public static final int RESOLUTION_1920X1080P_25HZ = 7;
    public static final int RESOLUTION_1920X1080P_29HZ = 10;
    public static final int RESOLUTION_1920X1080P_30HZ = 6;
    public static final int RESOLUTION_1920X1080P_50HZ = 12;
    public static final int RESOLUTION_1920X1080P_60HZ = 11;
    public static final int RESOLUTION_3840X2160P_24HZ = 20;
    public static final int RESOLUTION_3840X2160P_30HZ = 19;
    public static final int RESOLUTION_720X480P_60HZ = 0;
    public static final int RESOLUTION_720X576P_50HZ = 1;
    public static final int SINK_1080I50 = 4096;
    public static final int SINK_1080I60 = 4;
    public static final int SINK_1080P23976 = 2097152;
    public static final int SINK_1080P24 = 1048576;
    public static final int SINK_1080P25 = 524288;
    public static final int SINK_1080P2997 = 4194304;
    public static final int SINK_1080P30 = 512;
    public static final int SINK_1080P50 = 8192;
    public static final int SINK_1080P60 = 8;
    public static final int SINK_480I = 64;
    public static final int SINK_480I_1440 = 128;
    public static final int SINK_480I_2880 = 256;
    public static final int SINK_480P = 1;
    public static final int SINK_480P_1440 = 16;
    public static final int SINK_480P_2880 = 32;
    public static final int SINK_4KP24 = 16777216;
    public static final int SINK_4KP30 = 8388608;
    public static final int SINK_576I = 65536;
    public static final int SINK_576I_1440 = 131072;
    public static final int SINK_576I_2880 = 262144;
    public static final int SINK_576P = 1024;
    public static final int SINK_576P_1440 = 16384;
    public static final int SINK_576P_2880 = 32768;
    public static final int SINK_720P50 = 2048;
    public static final int SINK_720P60 = 2;
    public static int[] sResolutionMask;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.hdmi.HdmiDef.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.hdmi.HdmiDef.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.hdmi.HdmiDef.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.hdmi.HdmiDef.<init>():void, dex: 
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
    public HdmiDef() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.hdmi.HdmiDef.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.hdmi.HdmiDef.<init>():void");
    }

    public static int[] getAllResolutions() {
        return new int[]{19, 20, 11, 12, 6, 7, 8, 9, 4, 5, 2, 3, 0, 1};
    }

    public static int[] getTabletAllResolutions() {
        return new int[]{11, 12, 6, 7, 8, 9, 4, 5, 2, 3, 0, 1};
    }

    public static int[] getDefaultResolutions(int index) {
        if (index == 0) {
            return new int[]{2, 3, 8, 9};
        }
        if (1 == index) {
            return getAllResolutions();
        }
        if (2 == index) {
            return new int[]{6, 2, 0};
        }
        if (4 == index) {
            return getTabletAllResolutions();
        }
        return new int[]{6, 11, 2, 0};
    }

    public static int[] getPreferedResolutions(int index) {
        if (index == 0) {
            return new int[]{102, 103, 100, 101};
        }
        if (1 == index) {
            return new int[]{111, 112, 106, 107, 108, 109, 104, 105, 102, 103, 100, 101};
        }
        if (2 == index) {
            return new int[]{102, 106, 100};
        }
        return new int[]{119, 120, 111, 112, 106, 107, 108, 109, 104, 105, 102, 103, 101, 100};
    }
}
