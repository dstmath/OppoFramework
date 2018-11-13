package com.android.internal.telephony.cat;

import android.os.Handler;
import com.android.internal.telephony.uicc.IccFileHandler;
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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class BipCommandParamsFactory extends Handler {
    /* renamed from: -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues */
    private static final /* synthetic */ int[] f32x72eb89a2 = null;
    private static final boolean ENG = false;
    static final int LOAD_MULTI_ICONS = 2;
    static final int LOAD_NO_ICON = 0;
    static final int LOAD_SINGLE_ICON = 1;
    static final int MSG_ID_LOAD_ICON_DONE = 1;
    private static BipCommandParamsFactory sInstance;
    private BipRilMessageDecoder mCaller;
    private CommandParams mCmdParams;
    private int mIconLoadState;
    private BipIconLoader mIconLoader;
    int tlvIndex;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.-getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues():int[], dex: 
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
    /* renamed from: -getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues */
    private static /* synthetic */ int[] m119xe796fd46() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.-getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues():int[], dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.-getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues():int[]");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.cat.BipCommandParamsFactory.<init>(com.android.internal.telephony.cat.BipRilMessageDecoder, com.android.internal.telephony.uicc.IccFileHandler):void, dex:  in method: com.android.internal.telephony.cat.BipCommandParamsFactory.<init>(com.android.internal.telephony.cat.BipRilMessageDecoder, com.android.internal.telephony.uicc.IccFileHandler):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.cat.BipCommandParamsFactory.<init>(com.android.internal.telephony.cat.BipRilMessageDecoder, com.android.internal.telephony.uicc.IccFileHandler):void, dex: 
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
    private BipCommandParamsFactory(com.android.internal.telephony.cat.BipRilMessageDecoder r1, com.android.internal.telephony.uicc.IccFileHandler r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.cat.BipCommandParamsFactory.<init>(com.android.internal.telephony.cat.BipRilMessageDecoder, com.android.internal.telephony.uicc.IccFileHandler):void, dex:  in method: com.android.internal.telephony.cat.BipCommandParamsFactory.<init>(com.android.internal.telephony.cat.BipRilMessageDecoder, com.android.internal.telephony.uicc.IccFileHandler):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.<init>(com.android.internal.telephony.cat.BipRilMessageDecoder, com.android.internal.telephony.uicc.IccFileHandler):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.processCloseChannel(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean, dex: 
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
    private boolean processCloseChannel(com.android.internal.telephony.cat.CommandDetails r1, java.util.List<com.android.internal.telephony.cat.ComprehensionTlv> r2) throws com.android.internal.telephony.cat.ResultException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.processCloseChannel(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.processCloseChannel(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.processGetChannelStatus(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean, dex: 
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
    private boolean processGetChannelStatus(com.android.internal.telephony.cat.CommandDetails r1, java.util.List<com.android.internal.telephony.cat.ComprehensionTlv> r2) throws com.android.internal.telephony.cat.ResultException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.processGetChannelStatus(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.processGetChannelStatus(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.processOpenChannel(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private boolean processOpenChannel(com.android.internal.telephony.cat.CommandDetails r1, java.util.List<com.android.internal.telephony.cat.ComprehensionTlv> r2) throws com.android.internal.telephony.cat.ResultException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.processOpenChannel(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.processOpenChannel(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.processReceiveData(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean, dex: 
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
    private boolean processReceiveData(com.android.internal.telephony.cat.CommandDetails r1, java.util.List<com.android.internal.telephony.cat.ComprehensionTlv> r2) throws com.android.internal.telephony.cat.ResultException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.processReceiveData(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.processReceiveData(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.processSendData(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private boolean processSendData(com.android.internal.telephony.cat.CommandDetails r1, java.util.List<com.android.internal.telephony.cat.ComprehensionTlv> r2) throws com.android.internal.telephony.cat.ResultException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.processSendData(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.processSendData(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.processSetUpEventList(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean, dex: 
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
    private boolean processSetUpEventList(com.android.internal.telephony.cat.CommandDetails r1, java.util.List<com.android.internal.telephony.cat.ComprehensionTlv> r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.processSetUpEventList(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.processSetUpEventList(com.android.internal.telephony.cat.CommandDetails, java.util.List):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.telephony.cat.BipCommandParamsFactory.resetTlvIndex():void, dex:  in method: com.android.internal.telephony.cat.BipCommandParamsFactory.resetTlvIndex():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.telephony.cat.BipCommandParamsFactory.resetTlvIndex():void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void resetTlvIndex() {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.internal.telephony.cat.BipCommandParamsFactory.resetTlvIndex():void, dex:  in method: com.android.internal.telephony.cat.BipCommandParamsFactory.resetTlvIndex():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.resetTlvIndex():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.searchForNextTag(com.android.internal.telephony.cat.ComprehensionTlvTag, java.util.Iterator):com.android.internal.telephony.cat.ComprehensionTlv, dex: 
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
    private com.android.internal.telephony.cat.ComprehensionTlv searchForNextTag(com.android.internal.telephony.cat.ComprehensionTlvTag r1, java.util.Iterator<com.android.internal.telephony.cat.ComprehensionTlv> r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.searchForNextTag(com.android.internal.telephony.cat.ComprehensionTlvTag, java.util.Iterator):com.android.internal.telephony.cat.ComprehensionTlv, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.searchForNextTag(com.android.internal.telephony.cat.ComprehensionTlvTag, java.util.Iterator):com.android.internal.telephony.cat.ComprehensionTlv");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.searchForNextTagAndIndex(com.android.internal.telephony.cat.ComprehensionTlvTag, java.util.Iterator):com.android.internal.telephony.cat.ComprehensionTlv, dex: 
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
    private com.android.internal.telephony.cat.ComprehensionTlv searchForNextTagAndIndex(com.android.internal.telephony.cat.ComprehensionTlvTag r1, java.util.Iterator<com.android.internal.telephony.cat.ComprehensionTlv> r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.searchForNextTagAndIndex(com.android.internal.telephony.cat.ComprehensionTlvTag, java.util.Iterator):com.android.internal.telephony.cat.ComprehensionTlv, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.searchForNextTagAndIndex(com.android.internal.telephony.cat.ComprehensionTlvTag, java.util.Iterator):com.android.internal.telephony.cat.ComprehensionTlv");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.sendCmdParams(com.android.internal.telephony.cat.ResultCode):void, dex: 
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
    private void sendCmdParams(com.android.internal.telephony.cat.ResultCode r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.sendCmdParams(com.android.internal.telephony.cat.ResultCode):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.sendCmdParams(com.android.internal.telephony.cat.ResultCode):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.setIcons(java.lang.Object):com.android.internal.telephony.cat.ResultCode, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private com.android.internal.telephony.cat.ResultCode setIcons(java.lang.Object r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.setIcons(java.lang.Object):com.android.internal.telephony.cat.ResultCode, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.setIcons(java.lang.Object):com.android.internal.telephony.cat.ResultCode");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.dispose():void, dex: 
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
    public void dispose() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.dispose():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.dispose():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.handleMessage(android.os.Message):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void handleMessage(android.os.Message r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.handleMessage(android.os.Message):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.handleMessage(android.os.Message):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.make(com.android.internal.telephony.cat.BerTlv):void, dex: 
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
    void make(com.android.internal.telephony.cat.BerTlv r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.cat.BipCommandParamsFactory.make(com.android.internal.telephony.cat.BerTlv):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cat.BipCommandParamsFactory.make(com.android.internal.telephony.cat.BerTlv):void");
    }

    static synchronized BipCommandParamsFactory getInstance(BipRilMessageDecoder caller, IccFileHandler fh) {
        synchronized (BipCommandParamsFactory.class) {
            BipCommandParamsFactory bipCommandParamsFactory;
            if (sInstance != null) {
                bipCommandParamsFactory = sInstance;
                return bipCommandParamsFactory;
            } else if (fh != null) {
                bipCommandParamsFactory = new BipCommandParamsFactory(caller, fh);
                return bipCommandParamsFactory;
            } else {
                return null;
            }
        }
    }

    private CommandDetails processCommandDetails(List<ComprehensionTlv> ctlvs) throws ResultException {
        if (ctlvs == null) {
            return null;
        }
        ComprehensionTlv ctlvCmdDet = searchForTag(ComprehensionTlvTag.COMMAND_DETAILS, ctlvs);
        if (ctlvCmdDet == null) {
            return null;
        }
        try {
            return ValueParser.retrieveCommandDetails(ctlvCmdDet);
        } catch (ResultException e) {
            CatLog.d((Object) this, "Failed to procees command details");
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
    }

    private ComprehensionTlv searchForTag(ComprehensionTlvTag tag, List<ComprehensionTlv> ctlvs) {
        return searchForNextTag(tag, ctlvs.iterator());
    }

    private ComprehensionTlv searchForTagAndIndex(ComprehensionTlvTag tag, List<ComprehensionTlv> ctlvs) {
        resetTlvIndex();
        return searchForNextTagAndIndex(tag, ctlvs.iterator());
    }
}
