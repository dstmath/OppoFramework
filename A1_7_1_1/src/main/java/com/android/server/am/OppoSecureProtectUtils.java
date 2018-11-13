package com.android.server.am;

import android.app.ActivityOptions;
import android.app.IActivityController;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;

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
class OppoSecureProtectUtils {
    private static final int MONKEY_CONTROLLER = 1;
    private static final int NO_CONTROLLER = 0;
    private static final int SECURE_CONTROLLER = 2;
    private static final String TAG = "OppoSecureProtectUtils";
    private static int mControllerType;
    private int mRequestCodeTemp;
    private ActivityRecord mResultToTemp;
    private String mResultWhoTemp;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoSecureProtectUtils.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoSecureProtectUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoSecureProtectUtils.<clinit>():void");
    }

    OppoSecureProtectUtils() {
    }

    static final void setSecureControllerLocked(ActivityManagerService ams, IActivityController controller) {
        setControllerLocked(ams, controller, controller == null ? 0 : 2);
    }

    static final void setMonkeyControllerLocked(ActivityManagerService ams, IActivityController controller) {
        setControllerLocked(ams, controller, controller == null ? 0 : 1);
    }

    static final void setControllerLocked(ActivityManagerService ams, IActivityController controller, int type) {
        Log.d(TAG, "Controller type = " + type);
        ams.mController = controller;
        mControllerType = type;
    }

    static final boolean isMonkeyController(ActivityManagerService ams) {
        boolean z = true;
        if (ams.mController == null) {
            return false;
        }
        if (mControllerType != 1) {
            z = false;
        }
        return z;
    }

    void setTempValue(ActivityRecord resultToTemp, String resultWhoTemp, int requestCodeTemp) {
        this.mResultToTemp = resultToTemp;
        this.mResultWhoTemp = resultWhoTemp;
        this.mRequestCodeTemp = requestCodeTemp;
    }

    ActivityRecord handleStartActivityLocked(ActivityManagerService service, ProcessRecord caller, int launchedFromUid, String callingPackage, Intent intent, String resolvedType, ActivityInfo aInfo, ActivityRecord resultTo, String resultWho, int reqCode, boolean componentSpecified, boolean rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityContainer container, ActivityOptions options, ActivityRecord sourceRecord) {
        if (this.mResultToTemp == null || ((intent.getFlags() & Integer.MIN_VALUE) == 0 && (intent.getFlags() & 1024) == 0 && (intent.getFlags() & 512) == 0)) {
            return new ActivityRecord(service, caller, launchedFromUid, callingPackage, intent, resolvedType, aInfo, service.mConfiguration, resultTo, resultWho, reqCode, componentSpecified, rootVoiceInteraction, supervisor, container, options, sourceRecord);
        }
        ActivityRecord r = new ActivityRecord(service, caller, launchedFromUid, callingPackage, intent, resolvedType, aInfo, service.mConfiguration, this.mResultToTemp, this.mResultWhoTemp, this.mRequestCodeTemp, componentSpecified, rootVoiceInteraction, supervisor, container, options, sourceRecord);
        setTempValue(null, null, 0);
        return r;
    }

    void handleFinishActivityLocked(ActivityStack stack) {
        if (this.mResultToTemp != null) {
            stack.sendActivityResultLocked(-1, this.mResultToTemp, this.mResultWhoTemp, this.mRequestCodeTemp, 0, null);
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isSecureController(ActivityStackSupervisor stack) {
        boolean z = false;
        if (stack.mWindowManager == null || !stack.mWindowManager.inKeyguardRestrictedInputMode() || stack.mService.mController == null) {
            return false;
        }
        if (mControllerType == 2) {
            z = true;
        }
        return z;
    }

    static final void nofityMonkeyFinish(ActivityManagerService ams) {
        Log.i(TAG, "nofityMonkeyFinish!");
        ams.mContext.sendBroadcast(new Intent("android.intent.action.OPPO_SECURE_MONKEY_FINISH"));
    }

    public ActivityRecord getValue() {
        return this.mResultToTemp;
    }

    public boolean isNeedReplaceActivityRequest(ActivityRecord newResultTo, String newResultWho, int newRequestCode) {
        if (this.mResultToTemp == newResultTo && this.mResultWhoTemp != null && this.mResultWhoTemp.equals(newResultWho) && this.mRequestCodeTemp == newRequestCode) {
            return false;
        }
        return true;
    }
}
