package com.android.internal.util;

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
public class Protocol {
    public static final int BASE_CONNECTIVITY_MANAGER = 524288;
    public static final int BASE_DATA_CONNECTION = 262144;
    public static final int BASE_DATA_CONNECTION_AC = 266240;
    public static final int BASE_DATA_CONNECTION_TRACKER = 270336;
    public static final int BASE_DHCP = 196608;
    public static final int BASE_ETHERNET = 540672;
    public static final int BASE_NETWORK_AGENT = 528384;
    public static final int BASE_NETWORK_FACTORY = 536576;
    public static final int BASE_NETWORK_MONITOR = 532480;
    public static final int BASE_NETWORK_STATE_TRACKER = 458752;
    public static final int BASE_NSD_MANAGER = 393216;
    public static final int BASE_SYSTEM_ASYNC_CHANNEL = 69632;
    public static final int BASE_SYSTEM_RESERVED = 65536;
    public static final int BASE_TETHERING = 327680;
    public static final int BASE_WIFI = 131072;
    public static final int BASE_WIFI_CONTROLLER = 155648;
    public static final int BASE_WIFI_LOGGER = 164608;
    public static final int BASE_WIFI_MANAGER = 151552;
    public static final int BASE_WIFI_MONITOR = 147456;
    public static final int BASE_WIFI_P2P_MANAGER = 139264;
    public static final int BASE_WIFI_P2P_SERVICE = 143360;
    public static final int BASE_WIFI_PASSPOINT_MANAGER = 163840;
    public static final int BASE_WIFI_PASSPOINT_SERVICE = 164096;
    public static final int BASE_WIFI_RTT_MANAGER = 160256;
    public static final int BASE_WIFI_RTT_SERVICE = 160512;
    public static final int BASE_WIFI_SCANNER = 159744;
    public static final int BASE_WIFI_SCANNER_SERVICE = 160000;
    public static final int BASE_WIFI_WATCHDOG = 135168;
    public static final int MAX_MESSAGE = 65535;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.util.Protocol.<init>():void, dex: 
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
    public Protocol() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.util.Protocol.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.Protocol.<init>():void");
    }
}
