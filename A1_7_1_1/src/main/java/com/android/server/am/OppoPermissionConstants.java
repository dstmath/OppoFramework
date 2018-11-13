package com.android.server.am;

import java.util.List;
import java.util.Map;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoPermissionConstants {
    private static final List<String> CALENDAR_PERMISSIONS = null;
    private static final List<String> CAMERA_PERMISSIONS = null;
    private static final List<String> CONTACTS_PERMISSIONS = null;
    private static final List<String> LOCATION_PERMISSIONS = null;
    public static final Map<String, List<String>> MAP_DANGEROUS_PERMISSON_GROUP = null;
    public static final Map<String, String> MAP_OPPO_DEFINED_TO_ORIGINAL = null;
    public static final long MASK_BIT_FIRST = 1;
    private static final List<String> MICROPHONE_PERMISSIONS = null;
    public static final int PERMISSION_ACCEPT = 1;
    public static final String PERMISSION_ACCESS = "android.permission.ACCESS_FINE_LOCATION";
    public static final String PERMISSION_ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL";
    public static final String PERMISSION_BLUETOOTH = "android.permission.BLUETOOTH_ADMIN";
    public static final String PERMISSION_CALL_PHONE = "android.permission.CALL_PHONE";
    public static final String PERMISSION_CAMERA = "android.permission.CAMERA";
    public static final String PERMISSION_DELETE_CALENDAR = "android.permission.WRITE_CALENDAR_DELETE";
    public static final String PERMISSION_DELETE_CALL = "android.permission.WRITE_CALL_LOG_DELETE";
    public static final String PERMISSION_DELETE_CONTACTS = "android.permission.WRITE_CONTACTS_DELETE";
    public static final String PERMISSION_DELETE_MMS = "android.permission.WRITE_MMS_DELETE";
    public static final String PERMISSION_DELETE_SMS = "android.permission.WRITE_SMS_DELETE";
    public static final String PERMISSION_EXTERNAL_STORAGE = "android.permission.WR_EXTERNAL_STORAGE";
    public static final String PERMISSION_GET_ACCOUNTS = "android.permission.GET_ACCOUNTS";
    public static final String PERMISSION_GPRS = "android.permission.CHANGE_NETWORK_STATE";
    public static final String PERMISSION_NFC = "android.permission.NFC";
    public static final int PERMISSION_NONE = 3;
    public static final String PERMISSION_PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS";
    public static final int PERMISSION_PROMPT = 0;
    public static final String PERMISSION_READ_CALENDAR = "android.permission.READ_CALENDAR";
    public static final String PERMISSION_READ_CALL_LOG = "android.permission.READ_CALL_LOG";
    public static final String PERMISSION_READ_CONTACTS = "android.permission.READ_CONTACTS";
    public static final String PERMISSION_READ_HISTORY_BOOKMARKS = "com.android.browser.permission.READ_HISTORY_BOOKMARKS";
    public static final String PERMISSION_READ_MMS = "android.permission.READ_MMS";
    public static final String PERMISSION_READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    public static final String PERMISSION_READ_SMS = "android.permission.READ_SMS";
    public static final String PERMISSION_RECEIVE_MMS = "android.permission.RECEIVE_MMS";
    public static final String PERMISSION_RECEIVE_SMS = "android.permission.RECEIVE_SMS";
    public static final String PERMISSION_RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH";
    public static final String PERMISSION_RECORD_AUDIO = "android.permission.RECORD_AUDIO";
    public static final int PERMISSION_REJECT = 2;
    public static final String PERMISSION_SEND_MMS = "android.permission.SEND_MMS";
    public static final String PERMISSION_SEND_MMS_INTERNET = "android.permission.INTERNET";
    public static final String PERMISSION_SEND_SMS = "android.permission.SEND_SMS";
    public static final String PERMISSION_SENSORS = "android.permission.BODY_SENSORS";
    public static final String PERMISSION_USE_SIP = "android.permission.USE_SIP";
    public static final String PERMISSION_WIFI = "android.permission.CHANGE_WIFI_STATE";
    public static final String PERMISSION_WRITE_CALENDAR = "android.permission.WRITE_CALENDAR";
    public static final String PERMISSION_WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG";
    public static final String PERMISSION_WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    public static final String PERMISSION_WRITE_MMS = "android.permission.WRITE_MMS";
    public static final String PERMISSION_WRITE_SMS = "android.permission.WRITE_SMS";
    private static final List<String> PHONE_PERMISSIONS = null;
    private static final List<String> SENSORS_PERMISSIONS = null;
    private static final List<String> SMS_PERMISSIONS = null;
    private static final List<String> STORAGE_PERMISSIONS = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoPermissionConstants.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoPermissionConstants.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoPermissionConstants.<clinit>():void");
    }
}
