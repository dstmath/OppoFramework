package com.android.server.fingerprint.power;

import android.content.Context;
import android.hardware.fingerprint.FingerprintInternal;
import android.hardware.fingerprint.FingerprintManager.ScreenOnCallback;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import com.android.server.LocationManagerService;
import com.android.server.fingerprint.util.LogUtil;
import com.android.server.fingerprint.wakeup.IFingerprintSensorEventListener;

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
public class FingerprintPowerManager {
    public static final String PROP_NAME_OPEN_ALL_FRAMES = "debug.screenoff.unlock";
    private static final String TAG = "FingerprintService.PowerManager";
    private static Object mMutex;
    private static FingerprintPowerManager mSingleInstance;
    private Context mContext;
    private Handler mHandler;
    private LocalService mLocalService;
    private ScreenOnCallback mScreenOnCallback;
    private IFingerprintSensorEventListener mUnlockController;

    private final class LocalService extends FingerprintInternal {
        /* synthetic */ LocalService(FingerprintPowerManager this$0, LocalService localService) {
            this();
        }

        private LocalService() {
        }

        public void setOnVerifyMonitor(ScreenOnCallback callback) {
            FingerprintPowerManager.this.mScreenOnCallback = callback;
            LogUtil.d(FingerprintPowerManager.TAG, "setOnVerifyMonitor --> mScreenOnCallback = " + FingerprintPowerManager.this.mScreenOnCallback);
        }

        public void notifyPowerKeyPressed() {
            long startTime = SystemClock.uptimeMillis();
            FingerprintPowerManager.this.mUnlockController.dispatchPowerKeyPressed();
            FingerprintPowerManager.this.clearAllFramesDrawForKeyguard();
            FingerprintPowerManager.this.calculateTime("notifyPowerKeyPressed", SystemClock.uptimeMillis() - startTime);
        }

        public void onWakeUpFinish() {
            FingerprintPowerManager.this.clearAllFramesDrawForKeyguard();
            FingerprintPowerManager.this.mUnlockController.onWakeUpFinish();
        }

        public void onGoToSleep() {
            FingerprintPowerManager.this.mUnlockController.onGoToSleep();
        }

        public void onWakeUp(boolean isWakeUpByFingerprint) {
            FingerprintPowerManager.this.mUnlockController.onWakeUp(isWakeUpByFingerprint);
        }

        public void onGoToSleepFinish() {
            FingerprintPowerManager.this.mUnlockController.onGoToSleepFinish();
        }

        public void onHomeKeyDown() {
            FingerprintPowerManager.this.mUnlockController.dispatchHomeKeyDown();
        }

        public void onHomeKeyUp() {
            FingerprintPowerManager.this.mUnlockController.dispatchHomeKeyUp();
        }

        public void onScreenOnUnBlockedByOther() {
            FingerprintPowerManager.this.mUnlockController.onScreenOnUnBlockedByOther();
        }

        public void onLightScreenOnFinish() {
            FingerprintPowerManager.this.mUnlockController.onLightScreenOnFinish();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.power.FingerprintPowerManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.fingerprint.power.FingerprintPowerManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.fingerprint.power.FingerprintPowerManager.<clinit>():void");
    }

    public FingerprintPowerManager(Context context, IFingerprintSensorEventListener controller) {
        this.mScreenOnCallback = null;
        this.mContext = context;
        this.mUnlockController = controller;
        this.mHandler = new Handler();
        this.mLocalService = new LocalService(this, null);
    }

    public static void initFPM(Context c, IFingerprintSensorEventListener controller) {
        getFingerprintPowerManager(c, controller);
    }

    public static FingerprintPowerManager getFingerprintPowerManager(Context c, IFingerprintSensorEventListener controller) {
        synchronized (mMutex) {
            if (mSingleInstance == null) {
                mSingleInstance = new FingerprintPowerManager(c, controller);
            }
        }
        return mSingleInstance;
    }

    public static FingerprintPowerManager getFingerprintPowerManager() {
        return mSingleInstance;
    }

    public boolean isScreenOFF() {
        if (((PowerManager) this.mContext.getSystemService("power")).getScreenState() == 0) {
            return true;
        }
        return false;
    }

    private void calculateTime(String mode, long interval) {
    }

    public void userActivity() {
        this.mHandler.post(new Runnable() {
            public void run() {
                PowerManager pm = (PowerManager) FingerprintPowerManager.this.mContext.getSystemService("power");
                long now = SystemClock.uptimeMillis();
                LogUtil.d(FingerprintPowerManager.TAG, "userActivity");
                pm.userActivity(now, 2, 0);
            }
        });
    }

    public void wakeup(int verifyCode) {
        long startTime = SystemClock.uptimeMillis();
        if (this.mScreenOnCallback != null) {
            LogUtil.d(TAG, "onVerifyDone");
            Trace.traceBegin(4, "FingerprintScreenOn");
            this.mScreenOnCallback.onVerifyDone(verifyCode);
            Trace.traceEnd(4);
        } else {
            LogUtil.w(TAG, "mScreenOnCallback = null wakeupNormal");
            wakeupNormal();
        }
        calculateTime("wakeup", SystemClock.uptimeMillis() - startTime);
    }

    public void wakeupNormal() {
        long startTime = SystemClock.uptimeMillis();
        PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
        Trace.traceBegin(4, "FingerprintScreenOnNormal");
        if (pm != null) {
            LogUtil.d(TAG, "wakeupNormal");
            pm.wakeUp(SystemClock.uptimeMillis());
        }
        Trace.traceEnd(4);
        calculateTime("wakeupNormal", SystemClock.uptimeMillis() - startTime);
    }

    public void wakeUpByReason(String reason) {
        long startTime = SystemClock.uptimeMillis();
        PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
        if (pm != null) {
            LogUtil.d(TAG, "wakeUpBy Reason = " + reason);
            pm.wakeUp(SystemClock.uptimeMillis(), reason);
        }
        calculateTime("wakeUpByReason", SystemClock.uptimeMillis() - startTime);
    }

    public void gotoSleep() {
        LogUtil.d(TAG, "gotoSleep, isScreenOFF = " + isScreenOFF());
        if (!isScreenOFF()) {
            return;
        }
        if (this.mScreenOnCallback != null) {
            LogUtil.d(TAG, "onVerifyDone");
            this.mScreenOnCallback.onVerifyDone(0);
            return;
        }
        LogUtil.e(TAG, "gotoSleep mScreenOnCallback = null");
    }

    public void openAllFramesDrawForKeyguard() {
        SystemProperties.set(PROP_NAME_OPEN_ALL_FRAMES, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        LogUtil.d(TAG, "openAllFramesDrawForKeyguard, " + SystemProperties.getBoolean(PROP_NAME_OPEN_ALL_FRAMES, false));
    }

    public void clearAllFramesDrawForKeyguard() {
        SystemProperties.set(PROP_NAME_OPEN_ALL_FRAMES, "0");
        LogUtil.d(TAG, "clearAllFramesDrawForKeyguard, " + SystemProperties.getBoolean(PROP_NAME_OPEN_ALL_FRAMES, false));
    }

    public LocalService getFingerprintLocalService() {
        return this.mLocalService;
    }

    public ScreenOnCallback getScreenOnCallback() {
        return this.mScreenOnCallback;
    }
}
