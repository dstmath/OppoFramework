package android.net.wimax;

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
public class WimaxManagerConstants {
    public static final String EXTRA_4G_STATE = "4g_state";
    public static final String EXTRA_NEW_SIGNAL_LEVEL = "newSignalLevel";
    public static final String EXTRA_WIMAX_STATE = "WimaxState";
    public static final String EXTRA_WIMAX_STATE_DETAIL = "WimaxStateDetail";
    public static final String EXTRA_WIMAX_STATE_INT = "WimaxStateInt";
    public static final String EXTRA_WIMAX_STATUS = "wimax_status";
    public static final String NET_4G_STATE_CHANGED_ACTION = "android.net.fourG.NET_4G_STATE_CHANGED";
    public static final int NET_4G_STATE_DISABLED = 1;
    public static final int NET_4G_STATE_ENABLED = 3;
    public static final int NET_4G_STATE_UNKNOWN = 4;
    public static final String SIGNAL_LEVEL_CHANGED_ACTION = "android.net.wimax.SIGNAL_LEVEL_CHANGED";
    public static final int WIMAX_DEREGISTRATION = 8;
    public static final int WIMAX_IDLE = 6;
    public static final String WIMAX_NETWORK_STATE_CHANGED_ACTION = "android.net.fourG.wimax.WIMAX_NETWORK_STATE_CHANGED";
    public static final String WIMAX_SERVICE = "WiMax";
    public static final int WIMAX_STATE_CONNECTED = 7;
    public static final int WIMAX_STATE_DISCONNECTED = 9;
    public static final int WIMAX_STATE_UNKNOWN = 0;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wimax.WimaxManagerConstants.<init>():void, dex: 
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
    public WimaxManagerConstants() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wimax.WimaxManagerConstants.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wimax.WimaxManagerConstants.<init>():void");
    }
}
