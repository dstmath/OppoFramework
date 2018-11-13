package com.android.internal.telephony.cat;

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
/* compiled from: BipCommandParams */
class OpenChannelParams extends CommandParams {
    public BearerDesc bearerDesc;
    public int bufferSize;
    public OtherAddress dataDestinationAddress;
    public GprsParams gprsParams;
    public OtherAddress localAddress;
    public TextMessage textMsg;
    public TransportProtocol transportProtocol;

    /* compiled from: BipCommandParams */
    public class GprsParams {
        public String accessPointName;
        final /* synthetic */ OpenChannelParams this$0;
        public String userLogin;
        public String userPwd;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.cat.OpenChannelParams.GprsParams.<init>(com.android.internal.telephony.cat.OpenChannelParams, java.lang.String, java.lang.String, java.lang.String):void, dex:  in method: com.android.internal.telephony.cat.OpenChannelParams.GprsParams.<init>(com.android.internal.telephony.cat.OpenChannelParams, java.lang.String, java.lang.String, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.cat.OpenChannelParams.GprsParams.<init>(com.android.internal.telephony.cat.OpenChannelParams, java.lang.String, java.lang.String, java.lang.String):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        GprsParams(com.android.internal.telephony.cat.OpenChannelParams r1, java.lang.String r2, java.lang.String r3, java.lang.String r4) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.cat.OpenChannelParams.GprsParams.<init>(com.android.internal.telephony.cat.OpenChannelParams, java.lang.String, java.lang.String, java.lang.String):void, dex:  in method: com.android.internal.telephony.cat.OpenChannelParams.GprsParams.<init>(com.android.internal.telephony.cat.OpenChannelParams, java.lang.String, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.OpenChannelParams.GprsParams.<init>(com.android.internal.telephony.cat.OpenChannelParams, java.lang.String, java.lang.String, java.lang.String):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.cat.OpenChannelParams.<init>(com.android.internal.telephony.cat.CommandDetails, com.android.internal.telephony.cat.BearerDesc, int, com.android.internal.telephony.cat.OtherAddress, com.android.internal.telephony.cat.TransportProtocol, com.android.internal.telephony.cat.OtherAddress, java.lang.String, java.lang.String, java.lang.String, com.android.internal.telephony.cat.TextMessage):void, dex:  in method: com.android.internal.telephony.cat.OpenChannelParams.<init>(com.android.internal.telephony.cat.CommandDetails, com.android.internal.telephony.cat.BearerDesc, int, com.android.internal.telephony.cat.OtherAddress, com.android.internal.telephony.cat.TransportProtocol, com.android.internal.telephony.cat.OtherAddress, java.lang.String, java.lang.String, java.lang.String, com.android.internal.telephony.cat.TextMessage):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.cat.OpenChannelParams.<init>(com.android.internal.telephony.cat.CommandDetails, com.android.internal.telephony.cat.BearerDesc, int, com.android.internal.telephony.cat.OtherAddress, com.android.internal.telephony.cat.TransportProtocol, com.android.internal.telephony.cat.OtherAddress, java.lang.String, java.lang.String, java.lang.String, com.android.internal.telephony.cat.TextMessage):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    OpenChannelParams(com.android.internal.telephony.cat.CommandDetails r1, com.android.internal.telephony.cat.BearerDesc r2, int r3, com.android.internal.telephony.cat.OtherAddress r4, com.android.internal.telephony.cat.TransportProtocol r5, com.android.internal.telephony.cat.OtherAddress r6, java.lang.String r7, java.lang.String r8, java.lang.String r9, com.android.internal.telephony.cat.TextMessage r10) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.cat.OpenChannelParams.<init>(com.android.internal.telephony.cat.CommandDetails, com.android.internal.telephony.cat.BearerDesc, int, com.android.internal.telephony.cat.OtherAddress, com.android.internal.telephony.cat.TransportProtocol, com.android.internal.telephony.cat.OtherAddress, java.lang.String, java.lang.String, java.lang.String, com.android.internal.telephony.cat.TextMessage):void, dex:  in method: com.android.internal.telephony.cat.OpenChannelParams.<init>(com.android.internal.telephony.cat.CommandDetails, com.android.internal.telephony.cat.BearerDesc, int, com.android.internal.telephony.cat.OtherAddress, com.android.internal.telephony.cat.TransportProtocol, com.android.internal.telephony.cat.OtherAddress, java.lang.String, java.lang.String, java.lang.String, com.android.internal.telephony.cat.TextMessage):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.OpenChannelParams.<init>(com.android.internal.telephony.cat.CommandDetails, com.android.internal.telephony.cat.BearerDesc, int, com.android.internal.telephony.cat.OtherAddress, com.android.internal.telephony.cat.TransportProtocol, com.android.internal.telephony.cat.OtherAddress, java.lang.String, java.lang.String, java.lang.String, com.android.internal.telephony.cat.TextMessage):void");
    }
}
