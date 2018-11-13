package com.android.internal.telephony.cat;

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
/* compiled from: BipResponseData */
class OpenChannelResponseDataEx extends OpenChannelResponseData {
    DnsServerAddress mDnsServerAddress;
    int mProtocolType;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.cat.OpenChannelResponseDataEx.<init>(com.android.internal.telephony.cat.ChannelStatus, com.android.internal.telephony.cat.BearerDesc, int, int):void, dex:  in method: com.android.internal.telephony.cat.OpenChannelResponseDataEx.<init>(com.android.internal.telephony.cat.ChannelStatus, com.android.internal.telephony.cat.BearerDesc, int, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.cat.OpenChannelResponseDataEx.<init>(com.android.internal.telephony.cat.ChannelStatus, com.android.internal.telephony.cat.BearerDesc, int, int):void, dex: 
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
    OpenChannelResponseDataEx(com.android.internal.telephony.cat.ChannelStatus r1, com.android.internal.telephony.cat.BearerDesc r2, int r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.cat.OpenChannelResponseDataEx.<init>(com.android.internal.telephony.cat.ChannelStatus, com.android.internal.telephony.cat.BearerDesc, int, int):void, dex:  in method: com.android.internal.telephony.cat.OpenChannelResponseDataEx.<init>(com.android.internal.telephony.cat.ChannelStatus, com.android.internal.telephony.cat.BearerDesc, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.OpenChannelResponseDataEx.<init>(com.android.internal.telephony.cat.ChannelStatus, com.android.internal.telephony.cat.BearerDesc, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.cat.OpenChannelResponseDataEx.<init>(com.android.internal.telephony.cat.ChannelStatus, com.android.internal.telephony.cat.BearerDesc, int, com.android.internal.telephony.cat.DnsServerAddress):void, dex:  in method: com.android.internal.telephony.cat.OpenChannelResponseDataEx.<init>(com.android.internal.telephony.cat.ChannelStatus, com.android.internal.telephony.cat.BearerDesc, int, com.android.internal.telephony.cat.DnsServerAddress):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.cat.OpenChannelResponseDataEx.<init>(com.android.internal.telephony.cat.ChannelStatus, com.android.internal.telephony.cat.BearerDesc, int, com.android.internal.telephony.cat.DnsServerAddress):void, dex: 
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
    OpenChannelResponseDataEx(com.android.internal.telephony.cat.ChannelStatus r1, com.android.internal.telephony.cat.BearerDesc r2, int r3, com.android.internal.telephony.cat.DnsServerAddress r4) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.cat.OpenChannelResponseDataEx.<init>(com.android.internal.telephony.cat.ChannelStatus, com.android.internal.telephony.cat.BearerDesc, int, com.android.internal.telephony.cat.DnsServerAddress):void, dex:  in method: com.android.internal.telephony.cat.OpenChannelResponseDataEx.<init>(com.android.internal.telephony.cat.ChannelStatus, com.android.internal.telephony.cat.BearerDesc, int, com.android.internal.telephony.cat.DnsServerAddress):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.OpenChannelResponseDataEx.<init>(com.android.internal.telephony.cat.ChannelStatus, com.android.internal.telephony.cat.BearerDesc, int, com.android.internal.telephony.cat.DnsServerAddress):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.cat.OpenChannelResponseDataEx.format(java.io.ByteArrayOutputStream):void, dex: 
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
    public void format(java.io.ByteArrayOutputStream r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.cat.OpenChannelResponseDataEx.format(java.io.ByteArrayOutputStream):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.OpenChannelResponseDataEx.format(java.io.ByteArrayOutputStream):void");
    }
}
