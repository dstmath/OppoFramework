package javax.sip;

import java.util.EventObject;
import javax.sip.message.Request;

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
public class RequestEvent extends EventObject {
    private Dialog mDialog;
    private Request mRequest;
    private ServerTransaction mServerTransaction;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: javax.sip.RequestEvent.<init>(java.lang.Object, javax.sip.ServerTransaction, javax.sip.Dialog, javax.sip.message.Request):void, dex:  in method: javax.sip.RequestEvent.<init>(java.lang.Object, javax.sip.ServerTransaction, javax.sip.Dialog, javax.sip.message.Request):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: javax.sip.RequestEvent.<init>(java.lang.Object, javax.sip.ServerTransaction, javax.sip.Dialog, javax.sip.message.Request):void, dex: 
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
    public RequestEvent(java.lang.Object r1, javax.sip.ServerTransaction r2, javax.sip.Dialog r3, javax.sip.message.Request r4) {
        /*
        // Can't load method instructions: Load method exception: null in method: javax.sip.RequestEvent.<init>(java.lang.Object, javax.sip.ServerTransaction, javax.sip.Dialog, javax.sip.message.Request):void, dex:  in method: javax.sip.RequestEvent.<init>(java.lang.Object, javax.sip.ServerTransaction, javax.sip.Dialog, javax.sip.message.Request):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.sip.RequestEvent.<init>(java.lang.Object, javax.sip.ServerTransaction, javax.sip.Dialog, javax.sip.message.Request):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: javax.sip.RequestEvent.getDialog():javax.sip.Dialog, dex: 
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
    public javax.sip.Dialog getDialog() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: javax.sip.RequestEvent.getDialog():javax.sip.Dialog, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.sip.RequestEvent.getDialog():javax.sip.Dialog");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: javax.sip.RequestEvent.getRequest():javax.sip.message.Request, dex: 
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
    public javax.sip.message.Request getRequest() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: javax.sip.RequestEvent.getRequest():javax.sip.message.Request, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.sip.RequestEvent.getRequest():javax.sip.message.Request");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: javax.sip.RequestEvent.getServerTransaction():javax.sip.ServerTransaction, dex:  in method: javax.sip.RequestEvent.getServerTransaction():javax.sip.ServerTransaction, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: javax.sip.RequestEvent.getServerTransaction():javax.sip.ServerTransaction, dex: 
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
    public javax.sip.ServerTransaction getServerTransaction() {
        /*
        // Can't load method instructions: Load method exception: null in method: javax.sip.RequestEvent.getServerTransaction():javax.sip.ServerTransaction, dex:  in method: javax.sip.RequestEvent.getServerTransaction():javax.sip.ServerTransaction, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.sip.RequestEvent.getServerTransaction():javax.sip.ServerTransaction");
    }
}
