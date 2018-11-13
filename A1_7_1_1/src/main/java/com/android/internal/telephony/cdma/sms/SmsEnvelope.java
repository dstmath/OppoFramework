package com.android.internal.telephony.cdma.sms;

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
public final class SmsEnvelope {
    public static final int MESSAGE_TYPE_ACKNOWLEDGE = 2;
    public static final int MESSAGE_TYPE_BROADCAST = 1;
    public static final int MESSAGE_TYPE_POINT_TO_POINT = 0;
    public static final int SERVICE_CATEGORY_CMAS_CHILD_ABDUCTION_EMERGENCY = 4099;
    public static final int SERVICE_CATEGORY_CMAS_EXTREME_THREAT = 4097;
    public static final int SERVICE_CATEGORY_CMAS_LAST_RESERVED_VALUE = 4351;
    public static final int SERVICE_CATEGORY_CMAS_PRESIDENTIAL_LEVEL_ALERT = 4096;
    public static final int SERVICE_CATEGORY_CMAS_SEVERE_THREAT = 4098;
    public static final int SERVICE_CATEGORY_CMAS_TEST_MESSAGE = 4100;
    public static final int TELESERVICE_MWI = 262144;
    public static final int TELESERVICE_NOT_SET = 0;
    public static final int TELESERVICE_SCPT = 4102;
    public static final int TELESERVICE_VMN = 4099;
    public static final int TELESERVICE_WAP = 4100;
    public static final int TELESERVICE_WEMT = 4101;
    public static final int TELESERVICE_WMT = 4098;
    public byte[] bearerData;
    public int bearerReply;
    public byte causeCode;
    public CdmaSmsAddress destAddress;
    public byte errorClass;
    public int messageType;
    public CdmaSmsAddress origAddress;
    public CdmaSmsSubaddress origSubaddress;
    public byte replySeqNo;
    public int serviceCategory;
    public int teleService;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.cdma.sms.SmsEnvelope.<init>():void, dex:  in method: com.android.internal.telephony.cdma.sms.SmsEnvelope.<init>():void, dex: 
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
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.cdma.sms.SmsEnvelope.<init>():void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 9 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 10 more
        */
    public SmsEnvelope() {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.cdma.sms.SmsEnvelope.<init>():void, dex:  in method: com.android.internal.telephony.cdma.sms.SmsEnvelope.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.sms.SmsEnvelope.<init>():void");
    }
}
