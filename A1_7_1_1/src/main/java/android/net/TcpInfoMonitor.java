package android.net;

import android.content.Context;
import android.os.Handler;
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class TcpInfoMonitor {
    private static final String ATO_STR = "ato=";
    private static final String CA_STATE_STR = "ca_state=";
    private static boolean DBG = false;
    private static final String LAST_A_R_STR = "last_a_r=";
    private static final String LAST_D_R_STR = "last_d_r=";
    private static final String LAST_D_S_STR = "last_d_s=";
    private static final String OPTION_STR = "options=";
    private static final String PRINT_CMD = "cat proc/sys/net/ipv4/tcp_info_print";
    private static final String RCV_SPACE_STR = "rcv_space=";
    private static final String RTO_STR = "rto=";
    private static final String RTT_STR = "rtt=";
    private static final String STATE_STR = "state=";
    private static final String TAG = "TcpInfoMonitor";
    private static final long TCP_AGE_THRESHOLD = 30000;
    public static final int TCP_LINK_FAIR = 17;
    public static final int TCP_LINK_GOOD = 16;
    public static final int TCP_LINK_LOST = 2;
    public static final int TCP_LINK_OFF = 1;
    public static final int TCP_LINK_POOR = 18;
    public static final int TCP_LINK_RESTRICTED = 3;
    public static final int TCP_LINK_UNSPECIFIED = 0;
    private static final long TCP_RTO_THRESHOLD = 1000000;
    private static final long TCP_RTT_THRESHOLD = 1000000;
    private static final String T_RETRANS_STR = "t_retrans=";
    private static final String UNACKED_STR = "unacked=";
    public static final int VAILD_LINK_COUNT = 3;
    private Context mContext;
    private int mStatus;
    private Handler mTcpHandler;
    private List<TcpSocketInfo> mTcpSocketInfo;

    /* renamed from: android.net.TcpInfoMonitor$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ TcpInfoMonitor this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.TcpInfoMonitor.1.<init>(android.net.TcpInfoMonitor):void, dex: 
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
        AnonymousClass1(android.net.TcpInfoMonitor r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.TcpInfoMonitor.1.<init>(android.net.TcpInfoMonitor):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.1.<init>(android.net.TcpInfoMonitor):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.TcpInfoMonitor.1.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.TcpInfoMonitor.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.1.run():void");
        }
    }

    private class TcpSocketInfo {
        private static final int TCP_CA_CWR = 2;
        private static final int TCP_CA_Disorder = 1;
        private static final int TCP_CA_Loss = 4;
        private static final int TCP_CA_Open = 0;
        private static final int TCP_CA_Recovery = 3;
        private static final int TCP_CLOSE = 7;
        private static final int TCP_CLOSE_WAIT = 8;
        private static final int TCP_CLOSING = 11;
        private static final int TCP_ESTABLISHED = 1;
        private static final int TCP_FIN_WAIT1 = 4;
        private static final int TCP_FIN_WAIT2 = 5;
        private static final int TCP_LAST_ACK = 9;
        private static final int TCP_LISTEN = 10;
        private static final int TCP_SYN_RECV = 3;
        private static final int TCP_SYN_SENT = 2;
        private static final int TCP_TIME_WAIT = 6;
        private long ato;
        private int ca_state;
        private long last_ack_recv;
        private long last_data_recv;
        private long last_data_sent;
        private int option;
        private long rcv_spac;
        private long rto;
        private long rtt;
        private int state;
        final /* synthetic */ TcpInfoMonitor this$0;
        private long total_retrans;
        private long unacked;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get0(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get0 */
        static /* synthetic */ long m510-get0(android.net.TcpInfoMonitor.TcpSocketInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get0(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.TcpSocketInfo.-get0(android.net.TcpInfoMonitor$TcpSocketInfo):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get1(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex:  in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get1(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get1(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        /* renamed from: -get1 */
        static /* synthetic */ long m511-get1(android.net.TcpInfoMonitor.TcpSocketInfo r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get1(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex:  in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get1(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.TcpSocketInfo.-get1(android.net.TcpInfoMonitor$TcpSocketInfo):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get10(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get10 */
        static /* synthetic */ long m512-get10(android.net.TcpInfoMonitor.TcpSocketInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get10(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.TcpSocketInfo.-get10(android.net.TcpInfoMonitor$TcpSocketInfo):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get2(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get2 */
        static /* synthetic */ long m513-get2(android.net.TcpInfoMonitor.TcpSocketInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get2(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.TcpSocketInfo.-get2(android.net.TcpInfoMonitor$TcpSocketInfo):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get3(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get3 */
        static /* synthetic */ long m514-get3(android.net.TcpInfoMonitor.TcpSocketInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get3(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.TcpSocketInfo.-get3(android.net.TcpInfoMonitor$TcpSocketInfo):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get4(android.net.TcpInfoMonitor$TcpSocketInfo):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        /* renamed from: -get4 */
        static /* synthetic */ int m515-get4(android.net.TcpInfoMonitor.TcpSocketInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get4(android.net.TcpInfoMonitor$TcpSocketInfo):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.TcpSocketInfo.-get4(android.net.TcpInfoMonitor$TcpSocketInfo):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get5(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get5 */
        static /* synthetic */ long m516-get5(android.net.TcpInfoMonitor.TcpSocketInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get5(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.TcpSocketInfo.-get5(android.net.TcpInfoMonitor$TcpSocketInfo):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get6(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get6 */
        static /* synthetic */ long m517-get6(android.net.TcpInfoMonitor.TcpSocketInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get6(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.TcpSocketInfo.-get6(android.net.TcpInfoMonitor$TcpSocketInfo):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get7(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get7 */
        static /* synthetic */ long m518-get7(android.net.TcpInfoMonitor.TcpSocketInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get7(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.TcpSocketInfo.-get7(android.net.TcpInfoMonitor$TcpSocketInfo):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get8(android.net.TcpInfoMonitor$TcpSocketInfo):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        /* renamed from: -get8 */
        static /* synthetic */ int m519-get8(android.net.TcpInfoMonitor.TcpSocketInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get8(android.net.TcpInfoMonitor$TcpSocketInfo):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.TcpSocketInfo.-get8(android.net.TcpInfoMonitor$TcpSocketInfo):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get9(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get9 */
        static /* synthetic */ long m520-get9(android.net.TcpInfoMonitor.TcpSocketInfo r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.net.TcpInfoMonitor.TcpSocketInfo.-get9(android.net.TcpInfoMonitor$TcpSocketInfo):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.TcpSocketInfo.-get9(android.net.TcpInfoMonitor$TcpSocketInfo):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.TcpInfoMonitor.TcpSocketInfo.<init>(android.net.TcpInfoMonitor, int, int, int, long, long, long, long, long, long, long, long, long):void, dex:  in method: android.net.TcpInfoMonitor.TcpSocketInfo.<init>(android.net.TcpInfoMonitor, int, int, int, long, long, long, long, long, long, long, long, long):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.TcpInfoMonitor.TcpSocketInfo.<init>(android.net.TcpInfoMonitor, int, int, int, long, long, long, long, long, long, long, long, long):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private TcpSocketInfo(android.net.TcpInfoMonitor r1, int r2, int r3, int r4, long r5, long r7, long r9, long r11, long r13, long r15, long r17, long r19, long r21) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.TcpInfoMonitor.TcpSocketInfo.<init>(android.net.TcpInfoMonitor, int, int, int, long, long, long, long, long, long, long, long, long):void, dex:  in method: android.net.TcpInfoMonitor.TcpSocketInfo.<init>(android.net.TcpInfoMonitor, int, int, int, long, long, long, long, long, long, long, long, long):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.TcpSocketInfo.<init>(android.net.TcpInfoMonitor, int, int, int, long, long, long, long, long, long, long, long, long):void");
        }

        /* synthetic */ TcpSocketInfo(TcpInfoMonitor this$0, int state, int ca_state, int option, long rto, long ato, long unacked, long last_data_sent, long last_data_recv, long last_ack_recv, long rtt, long rcv_spac, long total_retrans, TcpSocketInfo tcpSocketInfo) {
            this(this$0, state, ca_state, option, rto, ato, unacked, last_data_sent, last_data_recv, last_ack_recv, rtt, rcv_spac, total_retrans);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.TcpInfoMonitor.-get0(android.net.TcpInfoMonitor):java.util.List, dex: 
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
    /* renamed from: -get0 */
    static /* synthetic */ java.util.List m507-get0(android.net.TcpInfoMonitor r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.TcpInfoMonitor.-get0(android.net.TcpInfoMonitor):java.util.List, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.-get0(android.net.TcpInfoMonitor):java.util.List");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.TcpInfoMonitor.-set0(android.net.TcpInfoMonitor, int):int, dex:  in method: android.net.TcpInfoMonitor.-set0(android.net.TcpInfoMonitor, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.TcpInfoMonitor.-set0(android.net.TcpInfoMonitor, int):int, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -set0 */
    static /* synthetic */ int m508-set0(android.net.TcpInfoMonitor r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: android.net.TcpInfoMonitor.-set0(android.net.TcpInfoMonitor, int):int, dex:  in method: android.net.TcpInfoMonitor.-set0(android.net.TcpInfoMonitor, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.-set0(android.net.TcpInfoMonitor, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.TcpInfoMonitor.-wrap1(android.net.TcpInfoMonitor, java.lang.String):void, dex: 
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
    /* renamed from: -wrap1 */
    static /* synthetic */ void m509-wrap1(android.net.TcpInfoMonitor r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.TcpInfoMonitor.-wrap1(android.net.TcpInfoMonitor, java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.-wrap1(android.net.TcpInfoMonitor, java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.TcpInfoMonitor.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.TcpInfoMonitor.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.TcpInfoMonitor.<init>(android.content.Context):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public TcpInfoMonitor(android.content.Context r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.TcpInfoMonitor.<init>(android.content.Context):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.<init>(android.content.Context):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.TcpInfoMonitor.Logd(java.lang.String):void, dex: 
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
    private void Logd(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.TcpInfoMonitor.Logd(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.Logd(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.TcpInfoMonitor.catchTcpSocketInfo():boolean, dex: 
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
    private boolean catchTcpSocketInfo() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.TcpInfoMonitor.catchTcpSocketInfo():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.catchTcpSocketInfo():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.net.TcpInfoMonitor.needToMonitorTcpInfo():boolean, dex: 
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
    private boolean needToMonitorTcpInfo() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.net.TcpInfoMonitor.needToMonitorTcpInfo():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.needToMonitorTcpInfo():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.TcpInfoMonitor.parseIntValueByString(java.lang.String, java.lang.String):int, dex: 
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
    private int parseIntValueByString(java.lang.String r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.TcpInfoMonitor.parseIntValueByString(java.lang.String, java.lang.String):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.parseIntValueByString(java.lang.String, java.lang.String):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.TcpInfoMonitor.parseLongValueByString(java.lang.String, java.lang.String):long, dex: 
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
    private long parseLongValueByString(java.lang.String r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.TcpInfoMonitor.parseLongValueByString(java.lang.String, java.lang.String):long, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.parseLongValueByString(java.lang.String, java.lang.String):long");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.TcpInfoMonitor.enableVerboseLogging(int):void, dex: 
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
    public void enableVerboseLogging(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.TcpInfoMonitor.enableVerboseLogging(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.enableVerboseLogging(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.TcpInfoMonitor.getCurrentTcpLinkStatus():int, dex:  in method: android.net.TcpInfoMonitor.getCurrentTcpLinkStatus():int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.TcpInfoMonitor.getCurrentTcpLinkStatus():int, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public int getCurrentTcpLinkStatus() {
        /*
        // Can't load method instructions: Load method exception: null in method: android.net.TcpInfoMonitor.getCurrentTcpLinkStatus():int, dex:  in method: android.net.TcpInfoMonitor.getCurrentTcpLinkStatus():int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.getCurrentTcpLinkStatus():int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.TcpInfoMonitor.resetTcpLinkStatus():void, dex:  in method: android.net.TcpInfoMonitor.resetTcpLinkStatus():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.TcpInfoMonitor.resetTcpLinkStatus():void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void resetTcpLinkStatus() {
        /*
        // Can't load method instructions: Load method exception: null in method: android.net.TcpInfoMonitor.resetTcpLinkStatus():void, dex:  in method: android.net.TcpInfoMonitor.resetTcpLinkStatus():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.TcpInfoMonitor.resetTcpLinkStatus():void");
    }
}
