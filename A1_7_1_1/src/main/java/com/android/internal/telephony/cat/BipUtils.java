package com.android.internal.telephony.cat;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class BipUtils {
    public static final int ADDRESS_IPV4_LENGTH = 4;
    public static final int ADDRESS_TYPE_IPV4 = 33;
    public static final int ADDRESS_TYPE_IPV6 = 87;
    public static final int ADDRESS_TYPE_UNKNOWN = 0;
    public static final int ADDRRES_IPV6_LENGTH = 16;
    public static final int BEARER_TYPE_CSD = 1;
    public static final int BEARER_TYPE_DEFAULT = 3;
    public static final int BEARER_TYPE_EUTRAN = 11;
    public static final int BEARER_TYPE_GPRS = 2;
    public static final int BEARER_TYPE_UNKNOWN = 0;
    public static final int BEARER_TYPE_UTRAN = 9;
    public static final int CHANNEL_STATUS_CLOSE = 2;
    public static final int CHANNEL_STATUS_ERROR = 7;
    public static final int CHANNEL_STATUS_LINK_DROPPED = 5;
    public static final int CHANNEL_STATUS_ONDEMAND = 1;
    public static final int CHANNEL_STATUS_OPEN = 4;
    public static final int CHANNEL_STATUS_SERVER_CLOSE = 3;
    public static final int CHANNEL_STATUS_TIMEOUT = 6;
    public static final int CHANNEL_STATUS_UNKNOWN = 0;
    public static final int DEFAULT_BUFFERSIZE_TCP = 1024;
    public static final int DEFAULT_BUFFERSIZE_UDP = 1024;
    public static final String KEY_QOS_CID = "cid";
    public static final String KEY_QOS_DELAY = "delay";
    public static final String KEY_QOS_MEAN = "mean";
    public static final String KEY_QOS_PEAK = "peak";
    public static final String KEY_QOS_PRECEDENCE = "precedence";
    public static final String KEY_QOS_RELIABILITY = "reliability";
    public static final int LINK_ESTABLISHMENT_MODE_IMMEDIATE = 0;
    public static final int LINK_ESTABLISHMENT_MODE_ONDEMMAND = 1;
    public static final int MAX_APDU_SIZE = 237;
    public static final int MAX_BUFFERSIZE_TCP = 1400;
    public static final int MAX_BUFFERSIZE_UDP = 1400;
    public static final int MAX_CHANNELS_CSD_ALLOWED = 1;
    public static final int MAX_CHANNELS_GPRS_ALLOWED = 3;
    public static final int MAX_CHANNEL_ID = 1;
    public static final int MIN_BUFFERSIZE_TCP = 255;
    public static final int MIN_BUFFERSIZE_UDP = 255;
    public static final int MIN_CHANNEL_ID = 1;
    public static final int SEND_DATA_MODE_IMMEDIATE = 1;
    public static final int SEND_DATA_MODE_STORED = 0;
    public static final byte TCP_STATUS_CLOSE = (byte) 0;
    public static final byte TCP_STATUS_ESTABLISHED = Byte.MIN_VALUE;
    public static final byte TCP_STATUS_LISTEN = (byte) 64;
    public static final int TRANSPORT_PROTOCOL_SERVER = 3;
    public static final int TRANSPORT_PROTOCOL_TCP_LOCAL = 5;
    public static final int TRANSPORT_PROTOCOL_TCP_REMOTE = 2;
    public static final int TRANSPORT_PROTOCOL_UDP_LOCAL = 4;
    public static final int TRANSPORT_PROTOCOL_UDP_REMOTE = 1;
    public static final int TRANSPORT_PROTOCOL_UNKNOWN = 0;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.cat.BipUtils.<init>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    public BipUtils() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.cat.BipUtils.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipUtils.<init>():void");
    }
}
