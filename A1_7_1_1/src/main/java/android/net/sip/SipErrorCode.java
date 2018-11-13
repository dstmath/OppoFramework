package android.net.sip;

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
public class SipErrorCode {
    public static final int CLIENT_ERROR = -4;
    public static final int CROSS_DOMAIN_AUTHENTICATION = -11;
    public static final int DATA_CONNECTION_LOST = -10;
    public static final int INVALID_CREDENTIALS = -8;
    public static final int INVALID_REMOTE_URI = -6;
    public static final int IN_PROGRESS = -9;
    public static final int NO_ERROR = 0;
    public static final int PEER_NOT_REACHABLE = -7;
    public static final int SERVER_ERROR = -2;
    public static final int SERVER_UNREACHABLE = -12;
    public static final int SOCKET_ERROR = -1;
    public static final int TIME_OUT = -5;
    public static final int TRANSACTION_TERMINTED = -3;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.sip.SipErrorCode.<init>():void, dex: 
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
    private SipErrorCode() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.sip.SipErrorCode.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.sip.SipErrorCode.<init>():void");
    }

    public static String toString(int errorCode) {
        switch (errorCode) {
            case SERVER_UNREACHABLE /*-12*/:
                return "SERVER_UNREACHABLE";
            case CROSS_DOMAIN_AUTHENTICATION /*-11*/:
                return "CROSS_DOMAIN_AUTHENTICATION";
            case DATA_CONNECTION_LOST /*-10*/:
                return "DATA_CONNECTION_LOST";
            case IN_PROGRESS /*-9*/:
                return "IN_PROGRESS";
            case INVALID_CREDENTIALS /*-8*/:
                return "INVALID_CREDENTIALS";
            case PEER_NOT_REACHABLE /*-7*/:
                return "PEER_NOT_REACHABLE";
            case INVALID_REMOTE_URI /*-6*/:
                return "INVALID_REMOTE_URI";
            case TIME_OUT /*-5*/:
                return "TIME_OUT";
            case CLIENT_ERROR /*-4*/:
                return "CLIENT_ERROR";
            case TRANSACTION_TERMINTED /*-3*/:
                return "TRANSACTION_TERMINTED";
            case SERVER_ERROR /*-2*/:
                return "SERVER_ERROR";
            case SOCKET_ERROR /*-1*/:
                return "SOCKET_ERROR";
            case 0:
                return "NO_ERROR";
            default:
                return "UNKNOWN";
        }
    }
}
