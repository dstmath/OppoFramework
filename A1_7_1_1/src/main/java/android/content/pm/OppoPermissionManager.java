package android.content.pm;

import android.net.Uri;
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
public class OppoPermissionManager {
    public static final int ACCEPT = 0;
    private static final String AUTHORITY = "com.coloros.provider.PermissionProvider";
    public static final int FIRST_MASK = 1;
    public static final int INVALID_RES = 3;
    public static final List<String> OPPO_DEFINED_PERMISSIONS = null;
    public static final Uri PERMISSIONS_PROVIDER_URI = null;
    private static final String PERMISSION_ACCESS_MEDIA_PROVIDER = "android.permission.ACCESS_MEDIA_PROVIDER";
    private static final String PERMISSION_DELETE_CALENDAR = "android.permission.WRITE_CALENDAR_DELETE";
    private static final String PERMISSION_DELETE_CALL = "android.permission.WRITE_CALL_LOG_DELETE";
    private static final String PERMISSION_DELETE_CONTACTS = "android.permission.WRITE_CONTACTS_DELETE";
    private static final String PERMISSION_DELETE_MMS = "android.permission.WRITE_MMS_DELETE";
    private static final String PERMISSION_DELETE_SMS = "android.permission.WRITE_SMS_DELETE";
    private static final String PERMISSION_WR_EXTERNAL_STORAGE = "android.permission.WR_EXTERNAL_STORAGE";
    public static final int PROMPT = 2;
    private static final String READ_MMS_PERMISSION = "android.permission.READ_MMS";
    public static final int REJECT = 1;
    private static final String SEND_MMS_PERMISSION = "android.permission.SEND_MMS";
    private static final String WRITE_MMS_PERMISSION = "android.permission.WRITE_MMS";
    public static final List<String> WRITE_PERMISSIONS = null;
    public static final List<String> sAlwaysInterceptingPermissions = null;
    public static final List<String> sInterceptingPermissions = null;
    public static final int[] sPermissionsDefaultChoices = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.content.pm.OppoPermissionManager.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.content.pm.OppoPermissionManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.OppoPermissionManager.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.content.pm.OppoPermissionManager.<init>():void, dex: 
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
    public OppoPermissionManager() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.content.pm.OppoPermissionManager.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.OppoPermissionManager.<init>():void");
    }
}
