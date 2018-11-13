package com.mediatek.server.am;

import android.content.Context;
import android.content.pm.PackageManagerInternal;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.mediatek.aal.AalUtils;
import com.mediatek.aee.ExceptionLog;
import com.mediatek.alarm.PowerOffAlarmUtility;
import com.mediatek.am.AMEventHookAction;
import com.mediatek.am.AMEventHookData.ActivityThreadResumedDone;
import com.mediatek.am.AMEventHookData.AfterActivityDestroyed;
import com.mediatek.am.AMEventHookData.AfterActivityPaused;
import com.mediatek.am.AMEventHookData.AfterActivityResumed;
import com.mediatek.am.AMEventHookData.AfterActivityStopped;
import com.mediatek.am.AMEventHookData.AfterPostEnableScreenAfterBoot;
import com.mediatek.am.AMEventHookData.BeforeActivitySwitch;
import com.mediatek.am.AMEventHookData.BeforeActivitySwitch.Index;
import com.mediatek.am.AMEventHookData.BeforeGoHomeWhenNoActivities;
import com.mediatek.am.AMEventHookData.BeforeSendBootCompleted;
import com.mediatek.am.AMEventHookData.BeforeSendBroadcast;
import com.mediatek.am.AMEventHookData.BeforeShowAppErrorDialog;
import com.mediatek.am.AMEventHookData.EndOfAMSCtor;
import com.mediatek.am.AMEventHookData.EndOfActivityIdle;
import com.mediatek.am.AMEventHookData.EndOfErrorDumpThread;
import com.mediatek.am.AMEventHookData.PackageStoppedStatusChanged;
import com.mediatek.am.AMEventHookData.ReadyToGetProvider;
import com.mediatek.am.AMEventHookData.ReadyToStartComponent;
import com.mediatek.am.AMEventHookData.ReadyToStartDynamicReceiver;
import com.mediatek.am.AMEventHookData.ReadyToStartService;
import com.mediatek.am.AMEventHookData.ReadyToStartStaticReceiver;
import com.mediatek.am.AMEventHookData.SkipStartActivity;
import com.mediatek.am.AMEventHookData.StartProcessForActivity;
import com.mediatek.am.AMEventHookData.SystemReady;
import com.mediatek.am.AMEventHookData.SystemUserUnlock;
import com.mediatek.am.AMEventHookData.UpdateSleep;
import com.mediatek.am.AMEventHookData.WakefulnessChanged;
import com.mediatek.am.AMEventHookData.WindowsVisible;
import com.mediatek.am.AMEventHookResult;
import com.mediatek.apm.frc.FocusRelationshipChainPolicy;
import com.mediatek.apm.suppression.SuppressionAction;
import com.mediatek.appworkingset.AWSManager;
import com.mediatek.ipomanager.ActivityManagerPlus;
import com.mediatek.perfservice.PerfServiceWrapper;
import com.mediatek.runningbooster.RunningBoosterService;
import com.mediatek.stk.IdleScreen;
import java.util.ArrayList;

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
public final class AMEventHook {
    /* renamed from: -com-mediatek-server-am-AMEventHook$EventSwitchesValues */
    private static final /* synthetic */ int[] f5-com-mediatek-server-am-AMEventHook$EventSwitchesValues = null;
    private static boolean DEBUG = false;
    private static boolean DEBUG_EVENT_DETAIL = false;
    private static boolean DEBUG_FLOW = false;
    private static final boolean IS_USER_BUILD = false;
    private static final boolean IS_USER_DEBUG_BUILD = false;
    private static final String TAG = "AMEventHook";
    private ExceptionLog exceptionLog;
    FocusRelationshipChainPolicy frcPolicy;
    AWSManager mAWSManager;
    private ActivityManagerPlus mActivityManagerPlus;
    private IdleScreen mIdleScreen;
    private PerfServiceWrapper mPerfService;
    private PowerOffAlarmUtility mPowerOffAlarmUtility;
    RunningBoosterService runningBoosterService;
    SuppressionAction suppressionAction;

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum Event {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.server.am.AMEventHook.Event.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.server.am.AMEventHook.Event.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.server.am.AMEventHook.Event.<clinit>():void");
        }
    }

    /* renamed from: -getcom-mediatek-server-am-AMEventHook$EventSwitchesValues */
    private static /* synthetic */ int[] m16-getcom-mediatek-server-am-AMEventHook$EventSwitchesValues() {
        if (f5-com-mediatek-server-am-AMEventHook$EventSwitchesValues != null) {
            return f5-com-mediatek-server-am-AMEventHook$EventSwitchesValues;
        }
        int[] iArr = new int[Event.values().length];
        try {
            iArr[Event.AM_ActivityThreadResumedDone.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Event.AM_AfterActivityDestroyed.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Event.AM_AfterActivityPaused.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Event.AM_AfterActivityResumed.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Event.AM_AfterActivityStopped.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Event.AM_AfterPostEnableScreenAfterBoot.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Event.AM_BeforeActivitySwitch.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Event.AM_BeforeGoHomeWhenNoActivities.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Event.AM_BeforeSendBootCompleted.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Event.AM_BeforeSendBroadcast.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Event.AM_BeforeShowAppErrorDialog.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Event.AM_EndOfAMSCtor.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Event.AM_EndOfActivityIdle.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Event.AM_EndOfErrorDumpThread.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Event.AM_PackageStoppedStatusChanged.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Event.AM_ReadyToGetProvider.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Event.AM_ReadyToStartComponent.ordinal()] = 17;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Event.AM_ReadyToStartDynamicReceiver.ordinal()] = 18;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Event.AM_ReadyToStartService.ordinal()] = 19;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Event.AM_ReadyToStartStaticReceiver.ordinal()] = 20;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Event.AM_SkipStartActivity.ordinal()] = 21;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Event.AM_StartProcessForActivity.ordinal()] = 22;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Event.AM_SystemReady.ordinal()] = 23;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Event.AM_SystemUserUnlock.ordinal()] = 24;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Event.AM_UpdateSleep.ordinal()] = 25;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Event.AM_WakefulnessChanged.ordinal()] = 26;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Event.AM_WindowsVisible.ordinal()] = 27;
        } catch (NoSuchFieldError e27) {
        }
        f5-com-mediatek-server-am-AMEventHook$EventSwitchesValues = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.mediatek.server.am.AMEventHook.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.mediatek.server.am.AMEventHook.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mediatek.server.am.AMEventHook.<clinit>():void");
    }

    public static AMEventHook createInstance() {
        AMEventHook aMEventHook;
        synchronized (AMEventHook.class) {
            aMEventHook = new AMEventHook();
        }
        return aMEventHook;
    }

    public static void setDebug(boolean on) {
        DEBUG = on;
        DEBUG_FLOW = on;
    }

    public static void setEventDetailDebug(boolean on) {
        DEBUG = on;
        DEBUG_FLOW = on;
        DEBUG_EVENT_DETAIL = on;
    }

    private void showLogForBeforeActivitySwitch(BeforeActivitySwitch data) {
        String lastResumedActivityName = data.getString(Index.lastResumedActivityName);
        String nextResumedActivityName = data.getString(Index.nextResumedActivityName);
        String lastResumedPackageName = data.getString(Index.lastResumedPackageName);
        String nextResumedPackageName = data.getString(Index.nextResumedPackageName);
        int lastResumedActivityType = data.getInt(Index.lastResumedActivityType);
        int nextResumedActivityType = data.getInt(Index.nextResumedActivityType);
        ArrayList<String> nextTaskPackageList = (ArrayList) data.get(Index.nextTaskPackageList);
        Slog.v(TAG, "onBeforeActivitySwitch, from: (" + lastResumedPackageName + ", " + lastResumedActivityName + ", " + lastResumedActivityType + "), to: (" + nextResumedPackageName + ", " + nextResumedActivityName + ", " + nextResumedActivityType + "), isNeedToPauseActivityFirst: " + data.getBoolean(Index.isNeedToPauseActivityFirst));
        if (nextTaskPackageList != null) {
            for (int i = 0; i < nextTaskPackageList.size(); i++) {
                Slog.v(TAG, "onBeforeActivitySwitch, nextTaskPackageList[" + i + "] = " + ((String) nextTaskPackageList.get(i)));
            }
        }
    }

    private void showLogForWakefulnessChanged(WakefulnessChanged data) {
        Slog.v(TAG, "onWakefulnessChanged, wakefulness: " + data.getInt(WakefulnessChanged.Index.wakefulness));
    }

    private void showLogForAfterActivityResumed(AfterActivityResumed data) {
        int pid = data.getInt(AfterActivityResumed.Index.pid);
        String activityName = data.getString(AfterActivityResumed.Index.activityName);
        String pkgName = data.getString(AfterActivityResumed.Index.packageName);
        int activityType = data.getInt(AfterActivityResumed.Index.activityType);
        int processUid = data.getInt(AfterActivityResumed.Index.processUid);
        Slog.v(TAG, "onAfterActivityResumed, Activity:(" + pid + ", " + pkgName + "," + activityName + ", " + activityType + ", processUid=" + processUid + ", callerUid=" + data.getInt(AfterActivityResumed.Index.callerUid) + ")");
    }

    private void showLogForActivityThreadResumedDone(ActivityThreadResumedDone data) {
        Slog.v(TAG, "onActivityThreadResumedDone, Activity package: " + data.getString(ActivityThreadResumedDone.Index.packageName));
    }

    private void showLogForSystemUserUnlock(SystemUserUnlock data) {
        Slog.v(TAG, "onSystemUserUnlock, uid is " + data.getInt(SystemUserUnlock.Index.uid));
    }

    private void showStartProcessForActivity(StartProcessForActivity data) {
        String reason = data.getString(StartProcessForActivity.Index.reason);
        Slog.v(TAG, "onStartProcessForActivity, reason is " + reason + " package: " + data.getString(StartProcessForActivity.Index.packageName));
    }

    boolean isDebuggableMessage(Event event) {
        switch (m16-getcom-mediatek-server-am-AMEventHook$EventSwitchesValues()[event.ordinal()]) {
            case 1:
                return false;
            case 4:
                return false;
            case 15:
                return false;
            case 16:
                return false;
            case 17:
                return false;
            case 18:
                return false;
            case 19:
                return false;
            case 20:
                return false;
            case 22:
                return false;
            case H.DO_ANIMATION_CALLBACK /*26*/:
                return false;
            default:
                return true;
        }
    }

    public AMEventHookResult hook(Event event, Object data) {
        return onEvent(event, data);
    }

    public AMEventHook() {
        this.exceptionLog = null;
        this.mPowerOffAlarmUtility = null;
        this.mIdleScreen = null;
        this.mPerfService = null;
        this.mActivityManagerPlus = null;
        this.frcPolicy = null;
        this.suppressionAction = null;
        this.runningBoosterService = null;
        this.mAWSManager = null;
        if (DEBUG_FLOW) {
            Slog.d(TAG, "AMEventHook()", new Throwable());
        } else if (DEBUG) {
            Slog.d(TAG, "AMEventHook()");
        }
    }

    private AMEventHookResult onEvent(Event event, Object data) {
        if (DEBUG_FLOW) {
            Slog.d(TAG, "onEvent: " + event, new Throwable());
        } else if (DEBUG || (!IS_USER_BUILD && isDebuggableMessage(event))) {
            Slog.d(TAG, "onEvent: " + event);
        }
        AMEventHookResult result = null;
        switch (m16-getcom-mediatek-server-am-AMEventHook$EventSwitchesValues()[event.ordinal()]) {
            case 1:
                result = onActivityThreadResumedDone((ActivityThreadResumedDone) data);
                break;
            case 2:
                result = onAfterActivityDestroyed((AfterActivityDestroyed) data);
                break;
            case 3:
                result = onAfterActivityPaused((AfterActivityPaused) data);
                break;
            case 4:
                result = onAfterActivityResumed((AfterActivityResumed) data);
                break;
            case 5:
                result = onAfterActivityStopped((AfterActivityStopped) data);
                break;
            case 6:
                result = onAfterPostEnableScreenAfterBoot((AfterPostEnableScreenAfterBoot) data);
                break;
            case 7:
                result = onBeforeActivitySwitch((BeforeActivitySwitch) data);
                break;
            case 8:
                result = onBeforeGoHomeWhenNoActivities((BeforeGoHomeWhenNoActivities) data);
                break;
            case 9:
                result = onBeforeSendBootCompleted((BeforeSendBootCompleted) data);
                break;
            case 10:
                result = onBeforeSendBroadcast((BeforeSendBroadcast) data);
                break;
            case 11:
                result = onBeforeShowAppErrorDialog((BeforeShowAppErrorDialog) data);
                break;
            case 12:
                result = onEndOfAMSCtor((EndOfAMSCtor) data);
                break;
            case 13:
                result = onEndOfActivityIdle((EndOfActivityIdle) data);
                break;
            case 14:
                result = onEndOfErrorDumpThread((EndOfErrorDumpThread) data);
                break;
            case 15:
                result = onPackageStoppedStatusChanged((PackageStoppedStatusChanged) data);
                break;
            case 16:
                result = onReadyToGetProvider((ReadyToGetProvider) data);
                break;
            case 17:
                result = onReadyToStartComponent((ReadyToStartComponent) data);
                break;
            case 18:
                result = onReadyToStartDynamicReceiver((ReadyToStartDynamicReceiver) data);
                break;
            case 19:
                result = onReadyToStartService((ReadyToStartService) data);
                break;
            case 20:
                result = onReadyToStartStaticReceiver((ReadyToStartStaticReceiver) data);
                break;
            case 21:
                result = onSkipStartActivity((SkipStartActivity) data);
                break;
            case 22:
                result = onStartProcessForActivity((StartProcessForActivity) data);
                break;
            case 23:
                result = onSystemReady((SystemReady) data);
                break;
            case 24:
                result = onSystemUserUnlock((SystemUserUnlock) data);
                break;
            case 25:
                result = onUpdateSleep((UpdateSleep) data);
                break;
            case H.DO_ANIMATION_CALLBACK /*26*/:
                result = onWakefulnessChanged((WakefulnessChanged) data);
                break;
            case 27:
                result = onWindowsVisible((WindowsVisible) data);
                break;
            default:
                Slog.w(TAG, "Unknown event: " + event);
                break;
        }
        if (DEBUG) {
            Slog.d(TAG, "onEvent result: " + result);
        }
        return result;
    }

    private AMEventHookResult onEndOfAMSCtor(EndOfAMSCtor data) {
        if (this.exceptionLog == null && SystemProperties.get("ro.have_aee_feature").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            this.exceptionLog = new ExceptionLog();
        }
        return null;
    }

    private AMEventHookResult onEndOfErrorDumpThread(EndOfErrorDumpThread data) {
        if (this.exceptionLog != null) {
            this.exceptionLog.onEndOfErrorDumpThread(data);
        }
        return null;
    }

    private AMEventHookResult onBeforeSendBootCompleted(BeforeSendBootCompleted data) {
        AMEventHookResult result = new AMEventHookResult();
        if (PowerOffAlarmUtility.isAlarmBoot()) {
            result.addAction(AMEventHookAction.AM_Interrupt);
        }
        return result;
    }

    private AMEventHookResult onSystemReady(SystemReady data) {
        AMEventHookResult result = new AMEventHookResult();
        if (this.mPowerOffAlarmUtility == null) {
            this.mPowerOffAlarmUtility = PowerOffAlarmUtility.getInstance(data);
        }
        this.mPowerOffAlarmUtility.onSystemReady(data, result);
        if (this.mActivityManagerPlus == null) {
            this.mActivityManagerPlus = ActivityManagerPlus.getInstance(data);
        }
        if (this.mIdleScreen == null) {
            this.mIdleScreen = new IdleScreen();
        }
        this.mIdleScreen.onSystemReady(data);
        if (this.mPerfService == null) {
            this.mPerfService = new PerfServiceWrapper(null);
        }
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) {
            if (this.frcPolicy == null) {
                this.frcPolicy = FocusRelationshipChainPolicy.getInstance();
            }
            if (this.suppressionAction == null) {
                this.suppressionAction = SuppressionAction.getInstance((Context) data.get(SystemReady.Index.context));
            }
        }
        if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support"))) {
            this.runningBoosterService = RunningBoosterService.getInstance(data);
        }
        if (SystemProperties.get("ro.mtk_aws_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && this.mAWSManager == null) {
            this.mAWSManager = AWSManager.getInstance(data);
        }
        return result;
    }

    private AMEventHookResult onAfterPostEnableScreenAfterBoot(AfterPostEnableScreenAfterBoot data) {
        AMEventHookResult result = new AMEventHookResult();
        if (this.mPowerOffAlarmUtility != null) {
            this.mPowerOffAlarmUtility.onAfterPostEnableScreenAfterBoot(data, result);
        }
        return result;
    }

    private AMEventHookResult onSkipStartActivity(SkipStartActivity data) {
        AMEventHookResult result = new AMEventHookResult();
        if (PowerOffAlarmUtility.isAlarmBoot()) {
            Slog.d(TAG, "Skip by alarm boot");
            result.addAction(AMEventHookAction.AM_SkipStartActivity);
        }
        return result;
    }

    private AMEventHookResult onBeforeGoHomeWhenNoActivities(BeforeGoHomeWhenNoActivities data) {
        AMEventHookResult result = new AMEventHookResult();
        if (PowerOffAlarmUtility.isAlarmBoot()) {
            Slog.v(TAG, "Skip to resume home activity!!");
            result.addAction(AMEventHookAction.AM_SkipHomeActivityLaunching);
        }
        return result;
    }

    private AMEventHookResult onEndOfActivityIdle(EndOfActivityIdle data) {
        if (this.mIdleScreen != null) {
            this.mIdleScreen.onEndOfActivityIdle(data);
        }
        return null;
    }

    private AMEventHookResult onBeforeShowAppErrorDialog(BeforeShowAppErrorDialog data) {
        AMEventHookResult result = new AMEventHookResult();
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).initMtkPermErrorDialog(data, result);
        return result;
    }

    private AMEventHookResult onBeforeSendBroadcast(BeforeSendBroadcast data) {
        AMEventHookResult result = new AMEventHookResult();
        if (this.mActivityManagerPlus != null) {
            result = this.mActivityManagerPlus.filterBroadcast(data, result);
        }
        if ((LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) && this.suppressionAction != null) {
            return this.suppressionAction.onBeforeSendBroadcast(data, result);
        }
        return result;
    }

    private AMEventHookResult onBeforeActivitySwitch(BeforeActivitySwitch data) {
        if (DEBUG_EVENT_DETAIL) {
            showLogForBeforeActivitySwitch(data);
        }
        if (this.mPerfService != null) {
            this.mPerfService.amsBoostResume(data);
        }
        if (this.runningBoosterService != null) {
            this.runningBoosterService.onBeforeActivitySwitch(data);
        }
        if (this.mAWSManager != null) {
            this.mAWSManager.onBeforeActivitySwitch(data);
        }
        if (this.frcPolicy != null) {
            this.frcPolicy.onStartActivity(data);
        }
        return null;
    }

    private AMEventHookResult onAfterActivityResumed(AfterActivityResumed data) {
        if (DEBUG_EVENT_DETAIL) {
            showLogForAfterActivityResumed(data);
        }
        if (this.mPerfService != null) {
            this.mPerfService.onAfterActivityResumed(data);
        }
        if (this.runningBoosterService != null) {
            this.runningBoosterService.onAfterActivityResumed(data);
        }
        if (AalUtils.isSupported()) {
            AalUtils.getInstance(true).onAfterActivityResumed(data);
        }
        return null;
    }

    private AMEventHookResult onAfterActivityPaused(AfterActivityPaused data) {
        if (this.mPerfService != null) {
            this.mPerfService.onAfterActivityPaused(data);
        }
        return null;
    }

    private AMEventHookResult onAfterActivityStopped(AfterActivityStopped data) {
        if (this.mPerfService != null) {
            this.mPerfService.onAfterActivityStopped(data);
        }
        return null;
    }

    private AMEventHookResult onAfterActivityDestroyed(AfterActivityDestroyed data) {
        if (this.mPerfService != null) {
            this.mPerfService.onAfterActivityDestroyed(data);
        }
        return null;
    }

    private AMEventHookResult onWindowsVisible(WindowsVisible data) {
        return null;
    }

    private AMEventHookResult onWakefulnessChanged(WakefulnessChanged data) {
        if (DEBUG_EVENT_DETAIL) {
            showLogForWakefulnessChanged(data);
        }
        if (this.runningBoosterService != null) {
            this.runningBoosterService.onWakefulnessChanged(data);
        }
        return null;
    }

    private AMEventHookResult onReadyToStartService(ReadyToStartService data) {
        if (this.frcPolicy != null) {
            this.frcPolicy.onStartService(data);
        }
        return null;
    }

    private AMEventHookResult onReadyToGetProvider(ReadyToGetProvider data) {
        if (this.frcPolicy != null) {
            this.frcPolicy.onStartProvider(data);
        }
        return null;
    }

    private AMEventHookResult onReadyToStartDynamicReceiver(ReadyToStartDynamicReceiver data) {
        if (this.frcPolicy != null) {
            this.frcPolicy.onStartDynamicReceiver(data);
        }
        return null;
    }

    private AMEventHookResult onReadyToStartStaticReceiver(ReadyToStartStaticReceiver data) {
        if (this.frcPolicy != null) {
            this.frcPolicy.onStartStaticReceiver(data);
        }
        return null;
    }

    private AMEventHookResult onReadyToStartComponent(ReadyToStartComponent data) {
        if ((LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) && this.suppressionAction != null) {
            this.suppressionAction.onReadyToStartComponent(data);
        }
        return null;
    }

    private AMEventHookResult onPackageStoppedStatusChanged(PackageStoppedStatusChanged data) {
        if ((LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("persist.runningbooster.support")) || LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("ro.mtk_aws_support"))) && this.suppressionAction != null) {
            this.suppressionAction.onPackageStoppedStatusChanged(data);
        }
        return null;
    }

    private AMEventHookResult onActivityThreadResumedDone(ActivityThreadResumedDone data) {
        if (DEBUG_EVENT_DETAIL) {
            showLogForActivityThreadResumedDone(data);
        }
        if (this.runningBoosterService != null) {
            this.runningBoosterService.onActivityThreadResumedDone(data);
        }
        return null;
    }

    private AMEventHookResult onSystemUserUnlock(SystemUserUnlock data) {
        if (DEBUG_EVENT_DETAIL) {
            showLogForSystemUserUnlock(data);
        }
        if (this.runningBoosterService != null) {
            this.runningBoosterService.onSystemUserUnlock(data);
        }
        return null;
    }

    private AMEventHookResult onUpdateSleep(UpdateSleep data) {
        if (AalUtils.isSupported()) {
            AalUtils.getInstance(true).onUpdateSleep(data);
        }
        return null;
    }

    private AMEventHookResult onStartProcessForActivity(StartProcessForActivity data) {
        if (DEBUG_EVENT_DETAIL) {
            showStartProcessForActivity(data);
        }
        if (this.mPerfService != null) {
            this.mPerfService.amsBoostProcessCreate(data);
        }
        return null;
    }
}
