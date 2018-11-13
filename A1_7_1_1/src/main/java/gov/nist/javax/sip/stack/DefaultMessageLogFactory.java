package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.LogRecord;
import gov.nist.javax.sip.LogRecordFactory;

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
public class DefaultMessageLogFactory implements LogRecordFactory {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: gov.nist.javax.sip.stack.DefaultMessageLogFactory.<init>():void, dex: 
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
    public DefaultMessageLogFactory() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: gov.nist.javax.sip.stack.DefaultMessageLogFactory.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.stack.DefaultMessageLogFactory.<init>():void");
    }

    public LogRecord createLogRecord(String message, String source, String destination, String timeStamp, boolean isSender, String firstLine, String tid, String callId, long tsHeaderValue) {
        return new MessageLog(message, source, destination, timeStamp, isSender, firstLine, tid, callId, tsHeaderValue);
    }

    public LogRecord createLogRecord(String message, String source, String destination, long timeStamp, boolean isSender, String firstLine, String tid, String callId, long timestampVal) {
        return new MessageLog(message, source, destination, timeStamp, isSender, firstLine, tid, callId, timestampVal);
    }
}
