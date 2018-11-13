package org.gsma.joyn;

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
public class Permissions {
    public static final String ANDROID_READ_CONTACTS = "android.permission.READ_CONTACTS";
    public static final String RCS_FILETRANSFER_READ = "org.gsma.joyn.RCS_FILETRANSFER_READ";
    public static final String RCS_FILETRANSFER_RECEIVE = "org.gsma.joyn.RCS_FILETRANSFER_RECEIVE";
    public static final String RCS_FILETRANSFER_SEND = "org.gsma.joyn.RCS_FILETRANSFER_SEND";
    public static final String RCS_IMAGESHARE_READ = "org.gsma.joyn.RCS_FILETRANSFER_READ";
    public static final String RCS_IMAGESHARE_RECEIVE = "org.gsma.joyn.RCS_IMAGESHARE_RECEIVE";
    public static final String RCS_IMAGESHARE_SEND = "org.gsma.joyn.RCS_IMAGESHARE_SEND";
    public static final String RCS_LOCATION_SEND = "org.gsma.joyn.RCS_LOCATION_SEND";
    public static final String RCS_READ_CAPABILITIES = "org.gsma.joyn.RCS_READ_CAPABILITIES";
    public static final String RCS_READ_CHAT = "org.gsma.joyn.RCS_READ_CHAT";
    public static final String RCS_READ_IPCALL = "org.gsma.joyn.RCS_READ_IPCALL";
    public static final String RCS_USE_CHAT = "org.gsma.joyn.RCS_USE_CHAT";
    public static final String RCS_USE_IPCALL = "org.gsma.joyn.RCS_USE_IPCALL";
    public static final String RCS_VIDEOSHARE_READ = "org.gsma.joyn.RCS_VIDEOSHARE_READ";
    public static final String RCS_VIDEOSHARE_RECEIVE = "org.gsma.joyn.RCS_VIDEOSHARE_RECEIVE";
    public static final String RCS_VIDEOSHARE_SEND = "org.gsma.joyn.RCS_VIDEOSHARE_SEND";
    public static final String READ_RCS_STATE = "org.gsma.joyn.READ_RCS_STATE";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.gsma.joyn.Permissions.<init>():void, dex: 
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
    public Permissions() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.gsma.joyn.Permissions.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.Permissions.<init>():void");
    }
}
