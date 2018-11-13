package com.android.server.am;

import android.content.ComponentName;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Slog;
import com.android.server.NetworkManagementService;
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
class OppoAmsUtils {
    private static final String FEATURE_ACT_FREQ_CONTROL = "oppo.ams.act.freqcontrol";
    static final String TAG = null;
    private static OppoAmsUtils mInstance;
    private ActStartFreqControler mActStartFreqControler;
    private ActStartupAccelerator mActStartupAccelerator;
    final ActivityManagerService mAm;
    private boolean mIsSupportActFreqCon;
    private boolean mIsSystemReady;
    private KeyguardServiceRestartDelay mKeyguardServiceRestartDelay;

    private class ActStartFreqControler {
        private static final boolean DEBUG_EMULATE = false;
        private static final boolean DEBUG_FREQ_CONTROL = false;
        private static final long DECT_TIME_MAX_INTERVEL = 30000;
        private static final long DECT_TIME_MIN_INTERVEL = 4000;
        private static final int MIN_START_DECT_COUNT = 3;
        private ArrayList<ActStartFreqData> mFreqControlActList = new ArrayList();

        public ActStartFreqControler() {
            initActList();
        }

        private void initActList() {
            this.mFreqControlActList.add(new ActStartFreqData("com.lianlian", "com.lianlian.activity.RetryAnonymousLoginActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("tv.danmaku.bili", "tv.danmaku.bili.ui.video.VideoDetailsActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("com.example.startactfreq", "com.example.startactfreq.FreqActivity"));
        }

        private ActStartFreqData getActStartFreqData(String pkgName, String className) {
            int size = this.mFreqControlActList.size();
            for (int index = 0; index < size; index++) {
                ActStartFreqData tmpData = (ActStartFreqData) this.mFreqControlActList.get(index);
                if (tmpData != null && tmpData.mPkgName.equals(pkgName) && tmpData.mClassName.equals(className)) {
                    return tmpData;
                }
            }
            return null;
        }

        public boolean doControlActivityStartFreq(Intent intent) {
            if (intent == null) {
                return false;
            }
            ComponentName cmpName = intent.getComponent();
            if (cmpName == null) {
                return false;
            }
            String pkgName = cmpName.getPackageName();
            String className = cmpName.getClassName();
            if (pkgName == null || className == null) {
                return false;
            }
            ActStartFreqData data = getActStartFreqData(pkgName, className);
            if (data == null) {
                return false;
            }
            long curTime = SystemClock.elapsedRealtime();
            long timeElapsed = curTime - data.mLastDectTime;
            data.mCount++;
            if (1 == data.mCount) {
                data.mLastDectTime = curTime;
                return false;
            } else if (!data.mIsFrequent) {
                if (timeElapsed > 4000) {
                    data.mLastDectTime = curTime;
                    data.mCount = 0;
                } else if (data.mCount >= 3) {
                    data.mIsFrequent = true;
                    data.mLastDectTime = curTime;
                }
                return false;
            } else if (timeElapsed <= DECT_TIME_MAX_INTERVEL) {
                data.mLastDectTime = curTime;
                return true;
            } else {
                data.mIsFrequent = false;
                data.mCount = 0;
                data.mLastDectTime = curTime;
                return false;
            }
        }
    }

    private class ActStartFreqData {
        String mClassName = null;
        int mCount = 0;
        boolean mIsFrequent = false;
        long mLastDectTime = 0;
        String mPkgName = null;

        public ActStartFreqData(String pkgName, String className) {
            this.mPkgName = pkgName;
            this.mClassName = className;
            this.mLastDectTime = SystemClock.elapsedRealtime();
            this.mCount = 0;
        }

        public String toShortString() {
            return "mPkgName:" + this.mPkgName + ", mClassName" + this.mClassName + ", c:" + this.mCount;
        }
    }

    private class ActStartupAccelerator {
        private static final int ACCE_DURATION_TIME = 1000;
        private boolean DEBUG_ACT_ACCE = true;
        private ArrayList<String> mAccelerateActList = new ArrayList();
        private long mLastAcceTime = 0;
        private LocalPerformance mLocalPerformance = null;
        private int[] mPerformanceConfigList = new int[]{1077936128, 1, 1086324736, 1, 1090519040, 2, 1082130432, NetworkManagementService.DNS_RESOLVER_DEFAULT_SAMPLE_VALIDITY_SECONDS};
        private boolean mSupportActAccelerate = true;

        public ActStartupAccelerator() {
            initAccelaerateActList();
            initPerformaceConfig();
        }

        private void initAccelaerateActList() {
            if (this.mSupportActAccelerate) {
                this.mAccelerateActList.add("{com.tencent.mm/com.tencent.mm.plugin.voip.ui.VideoActivity}");
            }
        }

        private void initPerformaceConfig() {
            if (this.mSupportActAccelerate) {
                this.mLocalPerformance = new LocalPerformance();
            }
        }

        private boolean isInAcceList(String shortCmpName) {
            int size = this.mAccelerateActList.size();
            for (int index = 0; index < size; index++) {
                String tmpData = (String) this.mAccelerateActList.get(index);
                if (this.DEBUG_ACT_ACCE) {
                    Slog.d(OppoAmsUtils.TAG, "isInAcceList, index:" + index + ", shortCmpName:" + shortCmpName + ", data:" + tmpData);
                }
                if (tmpData != null && tmpData.equals(shortCmpName)) {
                    return true;
                }
            }
            return false;
        }

        public boolean doAccelerateActStartup(Intent intent) {
            ComponentName cmpName = null;
            if (!this.mSupportActAccelerate) {
                return false;
            }
            if (intent != null) {
                cmpName = intent.getComponent();
            }
            if (cmpName == null) {
                if (this.DEBUG_ACT_ACCE) {
                    Slog.d(OppoAmsUtils.TAG, "doAccelerateActStartup cmpName null, not control");
                }
                return false;
            }
            String shortName = cmpName.toShortString();
            if (shortName == null || shortName.length() <= 0) {
                if (this.DEBUG_ACT_ACCE) {
                    Slog.d(OppoAmsUtils.TAG, "doAccelerateActStartup shortName empty, not control");
                }
                return false;
            } else if (isInAcceList(shortName)) {
                long curTime = SystemClock.elapsedRealtime();
                if (this.mLastAcceTime > 0 && curTime - this.mLastAcceTime <= 1000) {
                    return false;
                }
                this.mLastAcceTime = curTime;
                this.mLocalPerformance.perfLockAcquire(1000, this.mPerformanceConfigList);
                return true;
            } else {
                if (this.DEBUG_ACT_ACCE) {
                    Slog.d(OppoAmsUtils.TAG, "doAccelerateActStartup data not found in list.");
                }
                return false;
            }
        }
    }

    private class KeyguardServiceRestartDelay {
        private static final String KEYGUARD_SERVICE = "com.android.keyguard/.KeyguardService";
        private static final int MAX_DELAY_COUNT = 4;
        private static final long RESTART_DETECT_TIME = 6;
        private int mDelayCount = 0;
        private long mDelayDetectTime = 0;

        public boolean shouldDelay(String shortName, boolean shouldDelay) {
            if (!shouldDelay) {
                return false;
            }
            if (!KEYGUARD_SERVICE.equals(shortName)) {
                return shouldDelay;
            }
            if (this.mDelayCount == 0) {
                this.mDelayDetectTime = SystemClock.elapsedRealtime();
                this.mDelayCount++;
                return shouldDelay;
            }
            long curTime = SystemClock.elapsedRealtime();
            if (curTime - this.mDelayDetectTime <= RESTART_DETECT_TIME) {
                this.mDelayCount++;
                if (this.mDelayCount >= 4) {
                    this.mDelayDetectTime = curTime;
                    this.mDelayCount = 0;
                    return false;
                }
            }
            this.mDelayDetectTime = curTime;
            this.mDelayCount = 0;
            return shouldDelay;
        }
    }

    private class LocalPerformance {
        public int perfLockAcquire(int duration, int... list) {
            return 0;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAmsUtils.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.am.OppoAmsUtils.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.OppoAmsUtils.<clinit>():void");
    }

    public static OppoAmsUtils getInstance(ActivityManagerService service) {
        if (mInstance == null) {
            mInstance = new OppoAmsUtils(service);
        }
        return mInstance;
    }

    private OppoAmsUtils(ActivityManagerService service) {
        this.mIsSystemReady = false;
        this.mIsSupportActFreqCon = false;
        this.mActStartFreqControler = null;
        this.mActStartupAccelerator = null;
        this.mKeyguardServiceRestartDelay = null;
        this.mAm = service;
    }

    protected void systemReady() {
        initActStartFreqControl();
        this.mActStartupAccelerator = new ActStartupAccelerator();
        this.mKeyguardServiceRestartDelay = new KeyguardServiceRestartDelay();
        this.mIsSystemReady = true;
    }

    private void initActStartFreqControl() {
        this.mIsSupportActFreqCon = this.mAm.mContext.getPackageManager().hasSystemFeature(FEATURE_ACT_FREQ_CONTROL);
        Slog.i(TAG, "mIsSupportActFreqCon:" + this.mIsSupportActFreqCon);
        if (this.mIsSupportActFreqCon) {
            this.mActStartFreqControler = new ActStartFreqControler();
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000e, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean needToControlActivityStartFreq(Intent intent) {
        if (this.mIsSystemReady && this.mIsSupportActFreqCon && this.mActStartFreqControler != null) {
            return this.mActStartFreqControler.doControlActivityStartFreq(intent);
        }
        return false;
    }

    public boolean accelerateActStartup(Intent intent) {
        if (this.mIsSystemReady && this.mActStartupAccelerator != null) {
            return this.mActStartupAccelerator.doAccelerateActStartup(intent);
        }
        return false;
    }

    public boolean shouldDelayKeyguardServiceRestart(String shortName, boolean shouldDelay) {
        if (this.mIsSystemReady && this.mKeyguardServiceRestartDelay != null) {
            return this.mKeyguardServiceRestartDelay.shouldDelay(shortName, shouldDelay);
        }
        return false;
    }
}
