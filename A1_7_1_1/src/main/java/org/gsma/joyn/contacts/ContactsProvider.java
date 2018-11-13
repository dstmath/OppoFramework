package org.gsma.joyn.contacts;

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
public class ContactsProvider {
    public static final String MIME_TYPE_DUPLEX = "vnd.android.cursor.item/org.gsma.joyn.ir94-duplex";
    public static final String MIME_TYPE_EXTENSIONS = "vnd.android.cursor.item/org.gsma.joyn.extensions";
    public static final String MIME_TYPE_FILE_TRANSFER = "vnd.android.cursor.item/org.gsma.joyn.file-transfer";
    public static final String MIME_TYPE_GEOLOC_PUSH = "vnd.android.cursor.item/org.gsma.joyn.geoloc-push";
    public static final String MIME_TYPE_IMAGE_SHARING = "vnd.android.cursor.item/org.gsma.joyn.image-sharing";
    public static final String MIME_TYPE_IM_SESSION = "vnd.android.cursor.item/org.gsma.joyn.im-session";
    public static final String MIME_TYPE_IP_VIDEO_CALL = "vnd.android.cursor.item/org.gsma.joyn.ip-video-call";
    public static final String MIME_TYPE_IP_VOICE_CALL = "vnd.android.cursor.item/org.gsma.joyn.ip-voice-call";
    public static final String MIME_TYPE_IR94_VIDEO_CALL = "vnd.android.cursor.item/org.gsma.joyn.ir94-video-call";
    public static final String MIME_TYPE_IR94_VOICE_CALL = "vnd.android.cursor.item/org.gsma.joyn.ir94-voice-call";
    public static final String MIME_TYPE_PHONE_NUMBER = "vnd.android.cursor.item/org.gsma.joyn.number";
    public static final String MIME_TYPE_REGISTRATION_STATE = "vnd.android.cursor.item/org.gsma.joyn.registration-state";
    public static final String MIME_TYPE_VIDEO_SHARING = "vnd.android.cursor.item/org.gsma.joyn.video-sharing";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.gsma.joyn.contacts.ContactsProvider.<init>():void, dex: 
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
    public ContactsProvider() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.gsma.joyn.contacts.ContactsProvider.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.contacts.ContactsProvider.<init>():void");
    }
}
