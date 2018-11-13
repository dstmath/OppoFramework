package com.android.server.face.sensetime.faceapi.model;

import com.android.server.content.SyncOperation;
import com.android.server.face.sensetime.SenseTimeThreshold;
import com.android.server.job.JobSchedulerShellCommand;

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
/*  JADX ERROR: NullPointerException in pass: EnumVisitor
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
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
public enum ResultCode {
    ;
    
    private static final String DESCRIPTION_E_ACTIVE_CODE_INVALID = "invalid active code";
    private static final String DESCRIPTION_E_ACTIVE_FAIL = "license active failed";
    private static final String DESCRIPTION_E_AUTH_EXPIRE = "date expired";
    private static final String DESCRIPTION_E_DELNOTFOUND = "define not found";
    private static final String DESCRIPTION_E_FAIL = "run in fail inside";
    private static final String DESCRIPTION_E_FILE_EXPIRE = "model out of date";
    private static final String DESCRIPTION_E_FILE_NOT_FOUND = "file no found";
    private static final String DESCRIPTION_E_HANDLE = "handle Error,may be cause by sdk out of date or model file incorrect";
    private static final String DESCRIPTION_E_INVALIDARG = "invalid argument";
    private static final String DESCRIPTION_E_INVALID_APPID = "invalid app id";
    private static final String DESCRIPTION_E_INVALID_AUTH = "invalid license";
    private static final String DESCRIPTION_E_INVALID_FILE_FORMAT = "model format error";
    private static final String DESCRIPTION_E_INVALID_PIXEL_FORMAT = "invalid pixel format";
    private static final String DESCRIPTION_E_LICENSE_IS_NOT_ACTIVABLE = "invalid active code";
    private static final String DESCRIPTION_E_MISS_LICENSE = "con't find license";
    private static final String DESCRIPTION_E_MULTI_CALLS = "multi calls init license";
    private static final String DESCRIPTION_E_ONLINE_AUTH_CONNECT_FAIL = "online auth connect fail";
    private static final String DESCRIPTION_E_ONLINE_AUTH_INVALID = "check online fail";
    private static final String DESCRIPTION_E_ONLINE_AUTH_TIMEOUT = "check online timeout";
    private static final String DESCRIPTION_E_OUTOFMEMORY = "out of memory";
    private static final String DESCRIPTION_E_UNSUPPORTED = "unsupported function called";
    private static final String DESCRIPTION_E_UUID_MISMATCH = "uuid mismatch";
    private static final String DESCRIPTION_OK = "OK";
    private final int resultCode;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.face.sensetime.faceapi.model.ResultCode.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.face.sensetime.faceapi.model.ResultCode.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.face.sensetime.faceapi.model.ResultCode.<clinit>():void");
    }

    private ResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public int getValue() {
        return this.resultCode;
    }

    public static String getDescription(int code) {
        switch (code) {
            case JobSchedulerShellCommand.CMD_ERR_CONSTRAINTS /*-1002*/:
                return DESCRIPTION_E_MULTI_CALLS;
            case JobSchedulerShellCommand.CMD_ERR_NO_JOB /*-1001*/:
                return DESCRIPTION_E_MISS_LICENSE;
            case JobSchedulerShellCommand.CMD_ERR_NO_PACKAGE /*-1000*/:
                return DESCRIPTION_E_UNSUPPORTED;
            case -22:
                return "invalid active code";
            case -21:
                return DESCRIPTION_E_ACTIVE_FAIL;
            case SenseTimeThreshold.UP_MINIMUM /*-20*/:
                return "invalid active code";
            case -19:
                return DESCRIPTION_E_ONLINE_AUTH_INVALID;
            case -18:
                return DESCRIPTION_E_ONLINE_AUTH_TIMEOUT;
            case -17:
                return DESCRIPTION_E_ONLINE_AUTH_CONNECT_FAIL;
            case -16:
                return DESCRIPTION_E_UUID_MISMATCH;
            case -15:
                return DESCRIPTION_E_AUTH_EXPIRE;
            case -14:
                return DESCRIPTION_E_INVALID_APPID;
            case -13:
                return DESCRIPTION_E_INVALID_AUTH;
            case -9:
                return DESCRIPTION_E_FILE_EXPIRE;
            case -8:
                return DESCRIPTION_E_INVALID_FILE_FORMAT;
            case SyncOperation.REASON_MASTER_SYNC_AUTO /*-7*/:
                return DESCRIPTION_E_FILE_NOT_FOUND;
            case SyncOperation.REASON_SYNC_AUTO /*-6*/:
                return DESCRIPTION_E_INVALID_PIXEL_FORMAT;
            case -5:
                return DESCRIPTION_E_DELNOTFOUND;
            case SyncOperation.REASON_PERIODIC /*-4*/:
                return DESCRIPTION_E_FAIL;
            case -3:
                return DESCRIPTION_E_OUTOFMEMORY;
            case -2:
                return DESCRIPTION_E_HANDLE;
            case -1:
                return DESCRIPTION_E_INVALIDARG;
            case 0:
                return "OK";
            default:
                return null;
        }
    }
}
