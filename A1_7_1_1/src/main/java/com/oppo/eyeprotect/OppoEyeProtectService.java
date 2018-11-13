package com.oppo.eyeprotect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.FileObserver;
import android.os.Handler;
import android.util.SparseArray;
import android.util.Spline;
import android.view.Choreographer;
import com.oppo.eyeprotect.IOppoEyeProtectService.Stub;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class OppoEyeProtectService extends Stub {
    private static final String ACTION_LOCKED_BOOT_COMPLETED = "android.intent.action.LOCKED_BOOT_COMPLETED";
    private static final String ACTION_OPPO_DCS_PERIOD_UPLOAD = "android.intent.action.OPPO_DCS_PERIOD_UPLOAD";
    public static final String CLOSE_EVENT_ID = "close_protect_eyes_type";
    static boolean DEBUG_PANIC = false;
    public static final String EYEPROTECT_BEGIN_TIME = "protect_eyes_begin_time";
    public static final String EYEPROTECT_DURATION = "protect_eyes_duration";
    public static final String EYEPROTECT_END_TIME = "protect_eyes_end_time";
    public static final String EYEPROTECT_EVER_ENABLE = "protect_eyes_ever_enable";
    private static final String EYEPROTECT_GRAY_ENABLE = "gray_scale_on";
    private static final String EYEPROTECT_INVERSE_ENABLE = "inverse_on";
    public static final String EYEPROTECT_SINGLE_ENABLE_TIME_EVENT_ID = "protect_eyes_single_enable_time";
    public static final String EYEPROTECT_STATIC_DATA_EVENT_ID = "protect_eyes_static_data";
    public static final String EYEPROTECT_USER_ADJUSTMENT = "protect_eyes_user_adjustment";
    private static final String EYE_PROTECT_PROP = "sys.eyeprotect.enable";
    public static final String LOG_TAG = "20120";
    public static final String OPEN_EVENT_ID = "open_protect_eyes_type";
    public static final String PLATFORM_QUALCOMM = "oppo.hw.manufacturer.qualcomm";
    private static int SWITCH_ON = 0;
    private static final String TAG = "OppoEyeProtectService";
    public static final String TIMER_STATE = "protect_eyes_timer_state";
    public static final int UPDATE_DURATION = 1;
    public static final int UPDATE_ENABLE = 2;
    private static boolean mbQualcommPlatform;
    private final int ADJUSTMENT_INVERSE_OFF;
    private final int ADJUSTMENT_INVERSE_ON;
    private final double DISABLE_RATE;
    private final double ENABLE_RATE;
    private double USR_AJUST_LEVEL_RANGE;
    private double USR_AJUST_TEMP_DEFAULT;
    private double USR_AJUST_TEMP_MAX;
    private double USR_AJUST_TEMP_MIN;
    private double USR_AJUST_TEMP_RANGE;
    private double USR_PROGRESS_MID;
    private float[] ccts;
    private long disable_time;
    private boolean enable_prop_flag;
    private long enable_time;
    private boolean have_ever_enable;
    private float[] hours;
    private float[] levels;
    private double mAnimatedValue;
    private boolean mAnimating;
    private boolean mAnimatingCancled;
    private final Runnable mAnimationCallback;
    private boolean mAuto;
    private Spline mAutoSpline;
    private Spline mAutoSpline_base_cct;
    private BacklightListener mBacklightListener;
    private boolean mBootcompelete;
    Calendar mCalTimerActive;
    Calendar mCalUserSetEnable;
    private float mCct;
    private final Choreographer mChoreographer;
    private final Context mContext;
    private int[] mCount;
    private double mCurrentValue;
    private boolean mEnable;
    private boolean mEnableTimerSetting;
    private boolean mEnablebyuser;
    public Map<String, String> mEyeprotectStatisticsMap;
    private UpdateHandler mHandler;
    private int[] mIndex;
    private BroadcastReceiver mIntentScreenOnOffReceiver;
    private BroadcastReceiver mIntentTimeReceiver;
    private boolean mIsRegister;
    private HashMap<String, Integer> mModeMap;
    private boolean mNoanimation;
    private SparseArray<RGB> mRGBMap;
    private SparseArray<RGB> mRGBRampMap;
    private double mRate;
    private Object mRegister;
    private double mSaveValue;
    private double mTargetValue;
    private int mTimerActive;
    private double mUserLevel;
    private final double mVsyncTime;
    private Duration mduration;
    private Calendar pre_Calendar;
    SimpleDateFormat sdf;
    private boolean setfalsebytimestate;

    /* renamed from: com.oppo.eyeprotect.OppoEyeProtectService$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ OppoEyeProtectService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.eyeprotect.OppoEyeProtectService.1.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass1(com.oppo.eyeprotect.OppoEyeProtectService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.eyeprotect.OppoEyeProtectService.1.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.1.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.1.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: com.oppo.eyeprotect.OppoEyeProtectService$2 */
    class AnonymousClass2 extends BroadcastReceiver {
        final /* synthetic */ OppoEyeProtectService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.eyeprotect.OppoEyeProtectService.2.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass2(com.oppo.eyeprotect.OppoEyeProtectService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.eyeprotect.OppoEyeProtectService.2.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.2.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.2.onReceive(android.content.Context, android.content.Intent):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.2.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.2.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: com.oppo.eyeprotect.OppoEyeProtectService$3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ OppoEyeProtectService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.eyeprotect.OppoEyeProtectService.3.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass3(com.oppo.eyeprotect.OppoEyeProtectService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.eyeprotect.OppoEyeProtectService.3.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.3.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: c in method: com.oppo.eyeprotect.OppoEyeProtectService.3.run():void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.3.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: c in method: com.oppo.eyeprotect.OppoEyeProtectService.3.run():void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: c
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: c in method: com.oppo.eyeprotect.OppoEyeProtectService.3.run():void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.3.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.3.run():void");
        }
    }

    public class BacklightListener extends FileObserver {
        final /* synthetic */ OppoEyeProtectService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.eyeprotect.OppoEyeProtectService.BacklightListener.<init>(com.oppo.eyeprotect.OppoEyeProtectService, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public BacklightListener(com.oppo.eyeprotect.OppoEyeProtectService r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.eyeprotect.OppoEyeProtectService.BacklightListener.<init>(com.oppo.eyeprotect.OppoEyeProtectService, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.BacklightListener.<init>(com.oppo.eyeprotect.OppoEyeProtectService, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.BacklightListener.onEvent(int, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onEvent(int r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.BacklightListener.onEvent(int, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.BacklightListener.onEvent(int, java.lang.String):void");
        }
    }

    class Duration {
        private int hour_begin;
        private int hour_end;
        private int min_begin;
        private int min_end;
        final /* synthetic */ OppoEyeProtectService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get0(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get0 */
        static /* synthetic */ int m861-get0(com.oppo.eyeprotect.OppoEyeProtectService.Duration r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get0(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get0(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get1(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        /* renamed from: -get1 */
        static /* synthetic */ int m862-get1(com.oppo.eyeprotect.OppoEyeProtectService.Duration r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get1(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get1(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get2(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get2(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get2(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        /* renamed from: -get2 */
        static /* synthetic */ int m863-get2(com.oppo.eyeprotect.OppoEyeProtectService.Duration r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get2(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get2(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get2(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get3(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get3(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get3(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        /* renamed from: -get3 */
        static /* synthetic */ int m864-get3(com.oppo.eyeprotect.OppoEyeProtectService.Duration r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get3(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get3(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.-get3(com.oppo.eyeprotect.OppoEyeProtectService$Duration):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public Duration(com.oppo.eyeprotect.OppoEyeProtectService r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.IsInDuration(int, int):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean IsInDuration(int r1, int r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.IsInDuration(int, int):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.IsInDuration(int, int):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.IsValid():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean IsValid() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.IsValid():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.IsValid():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.SetDuration(int, int, int, int):void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.SetDuration(int, int, int, int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.SetDuration(int, int, int, int):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void SetDuration(int r1, int r2, int r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.SetDuration(int, int, int, int):void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.SetDuration(int, int, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.SetDuration(int, int, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.cover2day():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean cover2day() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.cover2day():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.cover2day():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.getBeginTimeString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String getBeginTimeString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.getBeginTimeString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.getBeginTimeString():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.getEndTimeString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String getEndTimeString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.getEndTimeString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.getEndTimeString():java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.getMinutes():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public int getMinutes() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.getMinutes():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.getMinutes():int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.toString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.oppo.eyeprotect.OppoEyeProtectService.Duration.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.Duration.toString():java.lang.String");
        }
    }

    private class UpdateHandler extends Handler {
        final /* synthetic */ OppoEyeProtectService this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.eyeprotect.OppoEyeProtectService.UpdateHandler.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private UpdateHandler(com.oppo.eyeprotect.OppoEyeProtectService r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.eyeprotect.OppoEyeProtectService.UpdateHandler.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.UpdateHandler.<init>(com.oppo.eyeprotect.OppoEyeProtectService):void");
        }

        /* synthetic */ UpdateHandler(OppoEyeProtectService this$0, UpdateHandler updateHandler) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: c in method: com.oppo.eyeprotect.OppoEyeProtectService.UpdateHandler.handleMessage(android.os.Message):void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.UpdateHandler.handleMessage(android.os.Message):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: c in method: com.oppo.eyeprotect.OppoEyeProtectService.UpdateHandler.handleMessage(android.os.Message):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: com.android.dex.DexException: bogus registerCount: c
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void handleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: c in method: com.oppo.eyeprotect.OppoEyeProtectService.UpdateHandler.handleMessage(android.os.Message):void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.UpdateHandler.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.UpdateHandler.handleMessage(android.os.Message):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: com.oppo.eyeprotect.OppoEyeProtectService.-get0(com.oppo.eyeprotect.OppoEyeProtectService):double, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get0 */
    static /* synthetic */ double m841-get0(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: com.oppo.eyeprotect.OppoEyeProtectService.-get0(com.oppo.eyeprotect.OppoEyeProtectService):double, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-get0(com.oppo.eyeprotect.OppoEyeProtectService):double");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.-get1(com.oppo.eyeprotect.OppoEyeProtectService):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get1 */
    static /* synthetic */ boolean m842-get1(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.-get1(com.oppo.eyeprotect.OppoEyeProtectService):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-get1(com.oppo.eyeprotect.OppoEyeProtectService):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.eyeprotect.OppoEyeProtectService.-get2(com.oppo.eyeprotect.OppoEyeProtectService):boolean, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.-get2(com.oppo.eyeprotect.OppoEyeProtectService):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.eyeprotect.OppoEyeProtectService.-get2(com.oppo.eyeprotect.OppoEyeProtectService):boolean, dex: 
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
    /* renamed from: -get2 */
    static /* synthetic */ boolean m843-get2(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.oppo.eyeprotect.OppoEyeProtectService.-get2(com.oppo.eyeprotect.OppoEyeProtectService):boolean, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.-get2(com.oppo.eyeprotect.OppoEyeProtectService):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-get2(com.oppo.eyeprotect.OppoEyeProtectService):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.-get3(com.oppo.eyeprotect.OppoEyeProtectService):android.content.Context, dex: 
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
    /* renamed from: -get3 */
    static /* synthetic */ android.content.Context m844-get3(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.-get3(com.oppo.eyeprotect.OppoEyeProtectService):android.content.Context, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-get3(com.oppo.eyeprotect.OppoEyeProtectService):android.content.Context");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: com.oppo.eyeprotect.OppoEyeProtectService.-get4(com.oppo.eyeprotect.OppoEyeProtectService):double, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get4 */
    static /* synthetic */ double m845-get4(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: com.oppo.eyeprotect.OppoEyeProtectService.-get4(com.oppo.eyeprotect.OppoEyeProtectService):double, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-get4(com.oppo.eyeprotect.OppoEyeProtectService):double");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.-get5(com.oppo.eyeprotect.OppoEyeProtectService):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get5 */
    static /* synthetic */ boolean m846-get5(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.-get5(com.oppo.eyeprotect.OppoEyeProtectService):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-get5(com.oppo.eyeprotect.OppoEyeProtectService):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.-get6(com.oppo.eyeprotect.OppoEyeProtectService):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get6 */
    static /* synthetic */ boolean m847-get6(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.-get6(com.oppo.eyeprotect.OppoEyeProtectService):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-get6(com.oppo.eyeprotect.OppoEyeProtectService):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: com.oppo.eyeprotect.OppoEyeProtectService.-get7(com.oppo.eyeprotect.OppoEyeProtectService):double, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get7 */
    static /* synthetic */ double m848-get7(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: com.oppo.eyeprotect.OppoEyeProtectService.-get7(com.oppo.eyeprotect.OppoEyeProtectService):double, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-get7(com.oppo.eyeprotect.OppoEyeProtectService):double");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: com.oppo.eyeprotect.OppoEyeProtectService.-get8(com.oppo.eyeprotect.OppoEyeProtectService):double, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get8 */
    static /* synthetic */ double m849-get8(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: com.oppo.eyeprotect.OppoEyeProtectService.-get8(com.oppo.eyeprotect.OppoEyeProtectService):double, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-get8(com.oppo.eyeprotect.OppoEyeProtectService):double");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: com.oppo.eyeprotect.OppoEyeProtectService.-get9(com.oppo.eyeprotect.OppoEyeProtectService):double, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get9 */
    static /* synthetic */ double m850-get9(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: com.oppo.eyeprotect.OppoEyeProtectService.-get9(com.oppo.eyeprotect.OppoEyeProtectService):double, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-get9(com.oppo.eyeprotect.OppoEyeProtectService):double");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e7 in method: com.oppo.eyeprotect.OppoEyeProtectService.-set0(com.oppo.eyeprotect.OppoEyeProtectService, double):double, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e7
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set0 */
    static /* synthetic */ double m851-set0(com.oppo.eyeprotect.OppoEyeProtectService r1, double r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e7 in method: com.oppo.eyeprotect.OppoEyeProtectService.-set0(com.oppo.eyeprotect.OppoEyeProtectService, double):double, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-set0(com.oppo.eyeprotect.OppoEyeProtectService, double):double");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.oppo.eyeprotect.OppoEyeProtectService.-set1(com.oppo.eyeprotect.OppoEyeProtectService, boolean):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set1 */
    static /* synthetic */ boolean m852-set1(com.oppo.eyeprotect.OppoEyeProtectService r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.oppo.eyeprotect.OppoEyeProtectService.-set1(com.oppo.eyeprotect.OppoEyeProtectService, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-set1(com.oppo.eyeprotect.OppoEyeProtectService, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e7 in method: com.oppo.eyeprotect.OppoEyeProtectService.-set2(com.oppo.eyeprotect.OppoEyeProtectService, double):double, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e7
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set2 */
    static /* synthetic */ double m853-set2(com.oppo.eyeprotect.OppoEyeProtectService r1, double r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e7 in method: com.oppo.eyeprotect.OppoEyeProtectService.-set2(com.oppo.eyeprotect.OppoEyeProtectService, double):double, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-set2(com.oppo.eyeprotect.OppoEyeProtectService, double):double");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.oppo.eyeprotect.OppoEyeProtectService.-set3(com.oppo.eyeprotect.OppoEyeProtectService, boolean):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set3 */
    static /* synthetic */ boolean m854-set3(com.oppo.eyeprotect.OppoEyeProtectService r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.oppo.eyeprotect.OppoEyeProtectService.-set3(com.oppo.eyeprotect.OppoEyeProtectService, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-set3(com.oppo.eyeprotect.OppoEyeProtectService, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.-wrap0(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
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
    /* renamed from: -wrap0 */
    static /* synthetic */ void m855-wrap0(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.-wrap0(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-wrap0(com.oppo.eyeprotect.OppoEyeProtectService):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.-wrap1(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
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
    /* renamed from: -wrap1 */
    static /* synthetic */ void m856-wrap1(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.-wrap1(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-wrap1(com.oppo.eyeprotect.OppoEyeProtectService):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.-wrap2(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
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
    /* renamed from: -wrap2 */
    static /* synthetic */ void m857-wrap2(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.-wrap2(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-wrap2(com.oppo.eyeprotect.OppoEyeProtectService):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.-wrap3(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
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
    /* renamed from: -wrap3 */
    static /* synthetic */ void m858-wrap3(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.-wrap3(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-wrap3(com.oppo.eyeprotect.OppoEyeProtectService):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.-wrap4(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
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
    /* renamed from: -wrap4 */
    static /* synthetic */ void m859-wrap4(com.oppo.eyeprotect.OppoEyeProtectService r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.-wrap4(com.oppo.eyeprotect.OppoEyeProtectService):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-wrap4(com.oppo.eyeprotect.OppoEyeProtectService):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.-wrap5(com.oppo.eyeprotect.OppoEyeProtectService, java.lang.String):void, dex: 
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
    /* renamed from: -wrap5 */
    static /* synthetic */ void m860-wrap5(com.oppo.eyeprotect.OppoEyeProtectService r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.-wrap5(com.oppo.eyeprotect.OppoEyeProtectService, java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.-wrap5(com.oppo.eyeprotect.OppoEyeProtectService, java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.eyeprotect.OppoEyeProtectService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 8 in method: com.oppo.eyeprotect.OppoEyeProtectService.<init>(android.content.Context):void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.<init>(android.content.Context):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 8 in method: com.oppo.eyeprotect.OppoEyeProtectService.<init>(android.content.Context):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus registerCount: 8
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public OppoEyeProtectService(android.content.Context r1) {
        /*
        // Can't load method instructions: Load method exception: bogus registerCount: 8 in method: com.oppo.eyeprotect.OppoEyeProtectService.<init>(android.content.Context):void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.<init>(android.content.Context):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.<init>(android.content.Context):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.caculate_ajust_level(double):double, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private double caculate_ajust_level(double r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.caculate_ajust_level(double):double, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.caculate_ajust_level(double):double");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.caculate_date():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void caculate_date() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.caculate_date():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.caculate_date():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.cancelAnimationCallback():void, dex: 
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
    private void cancelAnimationCallback() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.cancelAnimationCallback():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.cancelAnimationCallback():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: d in method: com.oppo.eyeprotect.OppoEyeProtectService.clearMap():void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.clearMap():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: d in method: com.oppo.eyeprotect.OppoEyeProtectService.clearMap():void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus registerCount: d
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void clearMap() {
        /*
        // Can't load method instructions: Load method exception: bogus registerCount: d in method: com.oppo.eyeprotect.OppoEyeProtectService.clearMap():void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.clearMap():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.clearMap():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.compareCalendar(java.util.Calendar, java.util.Calendar):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private boolean compareCalendar(java.util.Calendar r1, java.util.Calendar r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.compareCalendar(java.util.Calendar, java.util.Calendar):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.compareCalendar(java.util.Calendar, java.util.Calendar):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.isGraySacleOn():boolean, dex: 
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
    private boolean isGraySacleOn() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.isGraySacleOn():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.isGraySacleOn():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.isInverseOn():boolean, dex: 
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
    private boolean isInverseOn() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.isInverseOn():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.isInverseOn():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.isScreenOn():boolean, dex: 
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
    private boolean isScreenOn() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.isScreenOn():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.isScreenOn():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.level2RGB(double):com.oppo.eyeprotect.RGB, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private com.oppo.eyeprotect.RGB level2RGB(double r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.level2RGB(double):com.oppo.eyeprotect.RGB, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.level2RGB(double):com.oppo.eyeprotect.RGB");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.loadData():void, dex: 
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
    private void loadData() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.loadData():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.loadData():void");
    }

    private static native boolean native_IsEyeProtectEnable();

    private static native void native_SetEyeProtectEnable(boolean z);

    private static native int native_SetEyeProtectLevel(double d, double d2, double d3);

    private static native int native_SetEyeProtectLevel(int i);

    private static native void native_init();

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.postAnimationCallback():void, dex: 
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
    private void postAnimationCallback() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.postAnimationCallback():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.postAnimationCallback():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.regiestOtherReceiver():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void regiestOtherReceiver() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.regiestOtherReceiver():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.regiestOtherReceiver():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.regiestTimerReceiver():void, dex: 
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
    private void regiestTimerReceiver() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.regiestTimerReceiver():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.regiestTimerReceiver():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.saveData():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void saveData() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.saveData():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.saveData():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.saveTimer():void, dex: 
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
    private void saveTimer() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.saveTimer():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.saveTimer():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.sendDataToDcs(java.util.Map, java.lang.String):void, dex: 
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
    private void sendDataToDcs(java.util.Map<java.lang.String, java.lang.String> r1, java.lang.String r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.sendDataToDcs(java.util.Map, java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.sendDataToDcs(java.util.Map, java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.sendStaitcDataToDcs():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void sendStaitcDataToDcs() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.sendStaitcDataToDcs():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.sendStaitcDataToDcs():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.setCalendar(java.util.Calendar, java.util.Calendar):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void setCalendar(java.util.Calendar r1, java.util.Calendar r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.setCalendar(java.util.Calendar, java.util.Calendar):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.setCalendar(java.util.Calendar, java.util.Calendar):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.unregiestTimerReceiver():void, dex: 
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
    private void unregiestTimerReceiver() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.unregiestTimerReceiver():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.unregiestTimerReceiver():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.update_auto_level():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void update_auto_level() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.update_auto_level():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.update_auto_level():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.update_eye_protect(java.lang.String):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private void update_eye_protect(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.update_eye_protect(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.update_eye_protect(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e7 in method: com.oppo.eyeprotect.OppoEyeProtectService.animateTo(double, double, int):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e7
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public boolean animateTo(double r1, double r3, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e7 in method: com.oppo.eyeprotect.OppoEyeProtectService.animateTo(double, double, int):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.animateTo(double, double, int):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.init():void, dex: 
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
    public void init() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.eyeprotect.OppoEyeProtectService.init():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.init():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.eyeprotect.OppoEyeProtectService.setDuration(int, int, int, int):void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.setDuration(int, int, int, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.eyeprotect.OppoEyeProtectService.setDuration(int, int, int, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$20.decode(InstructionCodec.java:450)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void setDuration(int r1, int r2, int r3, int r4) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.oppo.eyeprotect.OppoEyeProtectService.setDuration(int, int, int, int):void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.setDuration(int, int, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.setDuration(int, int, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: 8 in method: com.oppo.eyeprotect.OppoEyeProtectService.setEyeProtectEnable(boolean, boolean, boolean):void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.setEyeProtectEnable(boolean, boolean, boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: 8 in method: com.oppo.eyeprotect.OppoEyeProtectService.setEyeProtectEnable(boolean, boolean, boolean):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: com.android.dex.DexException: bogus registerCount: 8
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void setEyeProtectEnable(boolean r1, boolean r2, boolean r3) {
        /*
        // Can't load method instructions: Load method exception: bogus registerCount: 8 in method: com.oppo.eyeprotect.OppoEyeProtectService.setEyeProtectEnable(boolean, boolean, boolean):void, dex:  in method: com.oppo.eyeprotect.OppoEyeProtectService.setEyeProtectEnable(boolean, boolean, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.setEyeProtectEnable(boolean, boolean, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e7 in method: com.oppo.eyeprotect.OppoEyeProtectService.setEyeProtectLevel(double):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e7
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public int setEyeProtectLevel(double r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e7 in method: com.oppo.eyeprotect.OppoEyeProtectService.setEyeProtectLevel(double):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.setEyeProtectLevel(double):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.setEyeProtectLevelbyUser(double, double):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setEyeProtectLevelbyUser(double r1, double r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.eyeprotect.OppoEyeProtectService.setEyeProtectLevelbyUser(double, double):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.setEyeProtectLevelbyUser(double, double):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.setTimerState(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setTimerState(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.eyeprotect.OppoEyeProtectService.setTimerState(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.eyeprotect.OppoEyeProtectService.setTimerState(boolean):void");
    }

    private boolean isValid(double level) {
        return level >= 0.0d && level <= 1.0d;
    }
}
