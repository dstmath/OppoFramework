package gov.nist.javax.sip;

import gov.nist.javax.sip.stack.SIPTransaction;
import gov.nist.javax.sip.stack.ServerRequestInterface;
import gov.nist.javax.sip.stack.ServerResponseInterface;

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
class DialogFilter implements ServerRequestInterface, ServerResponseInterface {
    protected ListeningPointImpl listeningPoint;
    private SipStackImpl sipStack;
    protected SIPTransaction transactionChannel;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: gov.nist.javax.sip.DialogFilter.<init>(gov.nist.javax.sip.SipStackImpl):void, dex: 
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
    public DialogFilter(gov.nist.javax.sip.SipStackImpl r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: gov.nist.javax.sip.DialogFilter.<init>(gov.nist.javax.sip.SipStackImpl):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.DialogFilter.<init>(gov.nist.javax.sip.SipStackImpl):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.DialogFilter.sendBadRequestResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction, java.lang.String):void, dex: 
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
    private void sendBadRequestResponse(gov.nist.javax.sip.message.SIPRequest r1, gov.nist.javax.sip.stack.SIPServerTransaction r2, java.lang.String r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.DialogFilter.sendBadRequestResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction, java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.DialogFilter.sendBadRequestResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction, java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.DialogFilter.sendCallOrTransactionDoesNotExistResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction):void, dex: 
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
    private void sendCallOrTransactionDoesNotExistResponse(gov.nist.javax.sip.message.SIPRequest r1, gov.nist.javax.sip.stack.SIPServerTransaction r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.DialogFilter.sendCallOrTransactionDoesNotExistResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.DialogFilter.sendCallOrTransactionDoesNotExistResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.DialogFilter.sendLoopDetectedResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction):void, dex: 
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
    private void sendLoopDetectedResponse(gov.nist.javax.sip.message.SIPRequest r1, gov.nist.javax.sip.stack.SIPServerTransaction r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.DialogFilter.sendLoopDetectedResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.DialogFilter.sendLoopDetectedResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.DialogFilter.sendRequestPendingResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction):void, dex: 
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
    private void sendRequestPendingResponse(gov.nist.javax.sip.message.SIPRequest r1, gov.nist.javax.sip.stack.SIPServerTransaction r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.DialogFilter.sendRequestPendingResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.DialogFilter.sendRequestPendingResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.DialogFilter.sendServerInternalErrorResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction):void, dex: 
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
    private void sendServerInternalErrorResponse(gov.nist.javax.sip.message.SIPRequest r1, gov.nist.javax.sip.stack.SIPServerTransaction r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.DialogFilter.sendServerInternalErrorResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.DialogFilter.sendServerInternalErrorResponse(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.SIPServerTransaction):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: f in method: gov.nist.javax.sip.DialogFilter.processRequest(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.MessageChannel):void, dex:  in method: gov.nist.javax.sip.DialogFilter.processRequest(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.MessageChannel):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: f in method: gov.nist.javax.sip.DialogFilter.processRequest(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.MessageChannel):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus registerCount: f
        	at com.android.dx.io.instructions.InstructionCodec$32.decode(InstructionCodec.java:693)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void processRequest(gov.nist.javax.sip.message.SIPRequest r1, gov.nist.javax.sip.stack.MessageChannel r2) {
        /*
        // Can't load method instructions: Load method exception: bogus registerCount: f in method: gov.nist.javax.sip.DialogFilter.processRequest(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.MessageChannel):void, dex:  in method: gov.nist.javax.sip.DialogFilter.processRequest(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.MessageChannel):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.DialogFilter.processRequest(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.MessageChannel):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.DialogFilter.processResponse(gov.nist.javax.sip.message.SIPResponse, gov.nist.javax.sip.stack.MessageChannel):void, dex: 
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
    public void processResponse(gov.nist.javax.sip.message.SIPResponse r1, gov.nist.javax.sip.stack.MessageChannel r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.DialogFilter.processResponse(gov.nist.javax.sip.message.SIPResponse, gov.nist.javax.sip.stack.MessageChannel):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.DialogFilter.processResponse(gov.nist.javax.sip.message.SIPResponse, gov.nist.javax.sip.stack.MessageChannel):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.DialogFilter.processResponse(gov.nist.javax.sip.message.SIPResponse, gov.nist.javax.sip.stack.MessageChannel, gov.nist.javax.sip.stack.SIPDialog):void, dex: 
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
    public void processResponse(gov.nist.javax.sip.message.SIPResponse r1, gov.nist.javax.sip.stack.MessageChannel r2, gov.nist.javax.sip.stack.SIPDialog r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.DialogFilter.processResponse(gov.nist.javax.sip.message.SIPResponse, gov.nist.javax.sip.stack.MessageChannel, gov.nist.javax.sip.stack.SIPDialog):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.DialogFilter.processResponse(gov.nist.javax.sip.message.SIPResponse, gov.nist.javax.sip.stack.MessageChannel, gov.nist.javax.sip.stack.SIPDialog):void");
    }

    public String getProcessingInfo() {
        return null;
    }
}
