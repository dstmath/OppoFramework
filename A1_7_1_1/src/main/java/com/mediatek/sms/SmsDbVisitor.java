package com.mediatek.sms;

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
public class SmsDbVisitor {
    private static final String APST_PACKAGE_NAME = "com.mediatek.apst.target";
    private static final String BACKUPRESTOR_PACKAGE_NAME = "com.mediatek.backuprestore";
    private static final String DATATRANSFER_PACKAGE_NAME = "com.mediatek.datatransfer";
    private static final boolean DBG = true;
    private static final String ESNTRACK_PACKAGE_NAME = "com.mediatek.esntrack";
    private static final String LOG_TAG = "[SmsDbVisitor]";
    private static final String OP18_PACKAGE_NAME = "com.mediatek.op18.plugin";
    private static final String PHONEPRIVACY_PACKAGE_NAME = "com.mediatek.ppl";
    private static final String RCSMESSAGE_PACKAGE_NAME = "com.mediatek.rcs.messageservice";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.sms.SmsDbVisitor.<init>():void, dex: 
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
    public SmsDbVisitor() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.sms.SmsDbVisitor.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.sms.SmsDbVisitor.<init>():void");
    }

    public static String[] getPackageNames() {
        return new String[]{DATATRANSFER_PACKAGE_NAME, APST_PACKAGE_NAME, PHONEPRIVACY_PACKAGE_NAME, BACKUPRESTOR_PACKAGE_NAME, RCSMESSAGE_PACKAGE_NAME, ESNTRACK_PACKAGE_NAME, OP18_PACKAGE_NAME};
    }
}
