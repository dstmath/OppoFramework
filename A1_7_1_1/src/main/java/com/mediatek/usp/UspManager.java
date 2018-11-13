package com.mediatek.usp;

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
public class UspManager {
    private static final boolean DBG = true;
    public static final String OPERATOR_CONFIGURATION_CHANGED_ACTION = "com.mediatek.usp.action.configured";
    public static final int SRV_CONFIG_STATE_IDLE = 2;
    public static final int SRV_CONFIG_STATE_INIT = 0;
    public static final int SRV_CONFIG_STATE_WAIT = 1;
    private static final String TAG = "UspManager";
    public static final String USER_CONFIRMATION_CONFIG_ACTION = "com.mediatek.usp.action.userinterface";
    private final IUspService mService;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.usp.UspManager.<init>(com.mediatek.usp.IUspService):void, dex: 
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
    public UspManager(com.mediatek.usp.IUspService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.usp.UspManager.<init>(com.mediatek.usp.IUspService):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.usp.UspManager.<init>(com.mediatek.usp.IUspService):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.usp.UspManager.getActiveOpPack():java.lang.String, dex: 
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
    public java.lang.String getActiveOpPack() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.usp.UspManager.getActiveOpPack():java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.usp.UspManager.getActiveOpPack():java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.usp.UspManager.getAllOpPackList():java.util.Map<java.lang.String, java.lang.String>, dex: 
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
    public java.util.Map<java.lang.String, java.lang.String> getAllOpPackList() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.usp.UspManager.getAllOpPackList():java.util.Map<java.lang.String, java.lang.String>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.usp.UspManager.getAllOpPackList():java.util.Map<java.lang.String, java.lang.String>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: c in method: com.mediatek.usp.UspManager.getOpPackFromSimInfo(java.lang.String):java.lang.String, dex:  in method: com.mediatek.usp.UspManager.getOpPackFromSimInfo(java.lang.String):java.lang.String, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: c in method: com.mediatek.usp.UspManager.getOpPackFromSimInfo(java.lang.String):java.lang.String, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus registerCount: c
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public java.lang.String getOpPackFromSimInfo(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus registerCount: c in method: com.mediatek.usp.UspManager.getOpPackFromSimInfo(java.lang.String):java.lang.String, dex:  in method: com.mediatek.usp.UspManager.getOpPackFromSimInfo(java.lang.String):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.usp.UspManager.getOpPackFromSimInfo(java.lang.String):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: c in method: com.mediatek.usp.UspManager.setOpPackActive(java.lang.String):void, dex:  in method: com.mediatek.usp.UspManager.setOpPackActive(java.lang.String):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: c in method: com.mediatek.usp.UspManager.setOpPackActive(java.lang.String):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus registerCount: c
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void setOpPackActive(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus registerCount: c in method: com.mediatek.usp.UspManager.setOpPackActive(java.lang.String):void, dex:  in method: com.mediatek.usp.UspManager.setOpPackActive(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.usp.UspManager.setOpPackActive(java.lang.String):void");
    }
}
