package com.android.server.face.power;

import android.content.Context;
import android.hardware.face.FaceInternal;
import android.os.PowerManager;
import android.os.SystemClock;
import com.android.server.face.utils.TimeUtils;

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
public class FacePowerManager {
    private static final String TAG = "FaceService.PowerManager";
    private static Object mMutex;
    private static FacePowerManager mSingleInstance;
    private final Context mContext;
    private final IPowerCallback mIPowerCallback;
    private final LocalService mLocalService;

    public interface IPowerCallback {
        void onGoToSleep();

        void onGoToSleepFinish();

        void onScreenOnUnBlockedByOther(String str);

        void onWakeUp(String str);

        void onWakeUpFinish();
    }

    private final class LocalService extends FaceInternal {
        /* synthetic */ LocalService(FacePowerManager this$0, LocalService localService) {
            this();
        }

        private LocalService() {
        }

        public void onWakeUp(String wakupReason) {
            long startTime = SystemClock.uptimeMillis();
            FacePowerManager.this.mIPowerCallback.onWakeUp(wakupReason);
            TimeUtils.calculateTime(FacePowerManager.TAG, "onWakeUp", SystemClock.uptimeMillis() - startTime);
        }

        public void onGoToSleep() {
            FacePowerManager.this.mIPowerCallback.onGoToSleep();
        }

        public void onWakeUpFinish() {
            FacePowerManager.this.mIPowerCallback.onWakeUpFinish();
        }

        public void onGoToSleepFinish() {
            FacePowerManager.this.mIPowerCallback.onGoToSleepFinish();
        }

        public void onScreenOnUnBlockedByOther(String unBlockedReason) {
            FacePowerManager.this.mIPowerCallback.onScreenOnUnBlockedByOther(unBlockedReason);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.face.power.FacePowerManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.face.power.FacePowerManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.face.power.FacePowerManager.<clinit>():void");
    }

    public FacePowerManager(Context context, IPowerCallback powerCallback) {
        this.mContext = context;
        this.mIPowerCallback = powerCallback;
        this.mLocalService = new LocalService(this, null);
    }

    public static void initFacePms(Context c, IPowerCallback powerCallback) {
        getFacePowerManager(c, powerCallback);
    }

    public static FacePowerManager getFacePowerManager(Context c, IPowerCallback powerCallback) {
        synchronized (mMutex) {
            if (mSingleInstance == null) {
                mSingleInstance = new FacePowerManager(c, powerCallback);
            }
        }
        return mSingleInstance;
    }

    public static FacePowerManager getFacePowerManager() {
        return mSingleInstance;
    }

    public boolean isScreenOFF() {
        if (((PowerManager) this.mContext.getSystemService("power")).getScreenState() == 0) {
            return true;
        }
        return false;
    }

    public LocalService getFaceLocalService() {
        return this.mLocalService;
    }
}
