package com.mediatek.internal.telephony.uicc;

import java.util.ArrayList;

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
public final class MmsIccFileFetcher extends IccFileFetcherBase {
    private static final String MMS_CONFIG_INFO = "ef_mms_config_info";
    private static final int MMS_ICP_AI_TAG = 133;
    private static final int MMS_ICP_AM_TAG = 132;
    private static final int MMS_ICP_CP_TAG = 171;
    private static final int MMS_ICP_G_TAG = 131;
    private static final int MMS_ICP_ICBI_TAG = 130;
    private static final String MMS_ICP_INFO = "ef_mms_icp_info";
    private static final int MMS_ICP_INVALID_TAG = 255;
    private static final int MMS_ICP_I_TAG = 128;
    private static final int MMS_ICP_RS_TAG = 129;
    private static final String TAG = "MmsIccFileFetcher";
    ArrayList<String> mFileList;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.<init>(android.content.Context, com.android.internal.telephony.Phone):void, dex: 
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
    public MmsIccFileFetcher(android.content.Context r1, com.android.internal.telephony.Phone r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.<init>(android.content.Context, com.android.internal.telephony.Phone):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.<init>(android.content.Context, com.android.internal.telephony.Phone):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.decodeGateWay(com.mediatek.internal.telephony.MmsIcpInfo, byte[], int, int):void, dex: 
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
    void decodeGateWay(com.mediatek.internal.telephony.MmsIcpInfo r1, byte[] r2, int r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.decodeGateWay(com.mediatek.internal.telephony.MmsIcpInfo, byte[], int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.decodeGateWay(com.mediatek.internal.telephony.MmsIcpInfo, byte[], int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.decodeMmsImplementation(com.mediatek.internal.telephony.MmsIcpInfo, byte[], int, int):void, dex:  in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.decodeMmsImplementation(com.mediatek.internal.telephony.MmsIcpInfo, byte[], int, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.decodeMmsImplementation(com.mediatek.internal.telephony.MmsIcpInfo, byte[], int, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    void decodeMmsImplementation(com.mediatek.internal.telephony.MmsIcpInfo r1, byte[] r2, int r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.decodeMmsImplementation(com.mediatek.internal.telephony.MmsIcpInfo, byte[], int, int):void, dex:  in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.decodeMmsImplementation(com.mediatek.internal.telephony.MmsIcpInfo, byte[], int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.decodeMmsImplementation(com.mediatek.internal.telephony.MmsIcpInfo, byte[], int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.dumpBytes(byte[]):java.lang.String, dex: 
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
    java.lang.String dumpBytes(byte[] r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.dumpBytes(byte[]):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.dumpBytes(byte[]):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.getMmsConfigInfo():com.mediatek.internal.telephony.MmsConfigInfo, dex: 
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
    protected com.mediatek.internal.telephony.MmsConfigInfo getMmsConfigInfo() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.getMmsConfigInfo():com.mediatek.internal.telephony.MmsConfigInfo, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.getMmsConfigInfo():com.mediatek.internal.telephony.MmsConfigInfo");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.getMmsIcpInfo():com.mediatek.internal.telephony.MmsIcpInfo, dex: 
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
    protected com.mediatek.internal.telephony.MmsIcpInfo getMmsIcpInfo() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.getMmsIcpInfo():com.mediatek.internal.telephony.MmsIcpInfo, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.getMmsIcpInfo():com.mediatek.internal.telephony.MmsIcpInfo");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.isValideIcpInfo(com.mediatek.internal.telephony.MmsIcpInfo):boolean, dex: 
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
    boolean isValideIcpInfo(com.mediatek.internal.telephony.MmsIcpInfo r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.isValideIcpInfo(com.mediatek.internal.telephony.MmsIcpInfo):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.isValideIcpInfo(com.mediatek.internal.telephony.MmsIcpInfo):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.onGetFilePara(java.lang.String):com.mediatek.internal.telephony.uicc.IccFileRequest, dex: 
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
    public com.mediatek.internal.telephony.uicc.IccFileRequest onGetFilePara(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.onGetFilePara(java.lang.String):com.mediatek.internal.telephony.uicc.IccFileRequest, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.onGetFilePara(java.lang.String):com.mediatek.internal.telephony.uicc.IccFileRequest");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.onGetKeys():java.util.ArrayList<java.lang.String>, dex: 
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
    public java.util.ArrayList<java.lang.String> onGetKeys() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.onGetKeys():java.util.ArrayList<java.lang.String>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.onGetKeys():java.util.ArrayList<java.lang.String>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.onParseResult(java.lang.String, byte[], java.util.ArrayList):void, dex: 
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
    public void onParseResult(java.lang.String r1, byte[] r2, java.util.ArrayList<byte[]> r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.onParseResult(java.lang.String, byte[], java.util.ArrayList):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.onParseResult(java.lang.String, byte[], java.util.ArrayList):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.parseMmsConfigInfo(byte[]):com.mediatek.internal.telephony.MmsConfigInfo, dex: 
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
    com.mediatek.internal.telephony.MmsConfigInfo parseMmsConfigInfo(byte[] r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.parseMmsConfigInfo(byte[]):com.mediatek.internal.telephony.MmsConfigInfo, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.parseMmsConfigInfo(byte[]):com.mediatek.internal.telephony.MmsConfigInfo");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.parseMmsIcpInfo(byte[]):com.mediatek.internal.telephony.MmsIcpInfo, dex: 
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
    com.mediatek.internal.telephony.MmsIcpInfo parseMmsIcpInfo(byte[] r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.parseMmsIcpInfo(byte[]):com.mediatek.internal.telephony.MmsIcpInfo, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.internal.telephony.uicc.MmsIccFileFetcher.parseMmsIcpInfo(byte[]):com.mediatek.internal.telephony.MmsIcpInfo");
    }
}
