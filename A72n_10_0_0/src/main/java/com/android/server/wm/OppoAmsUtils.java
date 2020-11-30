package com.android.server.wm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import java.io.File;
import java.util.ArrayList;

public class OppoAmsUtils {
    private static boolean DEBUG_AMS = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String FEATURE_ACT_FREQ_CONTROL = "oppo.ams.act.freqcontrol";
    static final String TAG = "OppoAmsUtils";
    private static OppoAmsUtils mInstance = null;
    private ActStartFreqControler mActStartFreqControler = null;
    private ActStartupAccelerator mActStartupAccelerator = null;
    final ActivityTaskManagerService mAtm;
    private boolean mIsSupportActFreqCon = false;
    private boolean mIsSystemReady = false;
    private KeyguardServiceRestartDelay mKeyguardServiceRestartDelay = null;

    public static OppoAmsUtils getInstance(ActivityTaskManagerService service) {
        if (mInstance == null) {
            mInstance = new OppoAmsUtils(service);
        }
        return mInstance;
    }

    private OppoAmsUtils(ActivityTaskManagerService service) {
        this.mAtm = service;
    }

    public void systemReady() {
        initActStartFreqControl();
        this.mActStartupAccelerator = new ActStartupAccelerator();
        this.mKeyguardServiceRestartDelay = new KeyguardServiceRestartDelay();
        this.mIsSystemReady = true;
    }

    private void initActStartFreqControl() {
        this.mIsSupportActFreqCon = this.mAtm.mContext.getPackageManager().hasSystemFeature(FEATURE_ACT_FREQ_CONTROL);
        Slog.i(TAG, "mIsSupportActFreqCon:" + this.mIsSupportActFreqCon);
        if (this.mIsSupportActFreqCon) {
            this.mActStartFreqControler = new ActStartFreqControler();
        }
    }

    public boolean needToControlActivityStartFreq(Intent intent) {
        ActStartFreqControler actStartFreqControler;
        if (!this.mIsSystemReady || !this.mIsSupportActFreqCon || (actStartFreqControler = this.mActStartFreqControler) == null) {
            return false;
        }
        return actStartFreqControler.doControlActivityStartFreq(intent);
    }

    private boolean accelerateActStartup(Intent intent) {
        ActStartupAccelerator actStartupAccelerator;
        if (this.mIsSystemReady && (actStartupAccelerator = this.mActStartupAccelerator) != null) {
            return actStartupAccelerator.doAccelerateActStartup(intent);
        }
        return false;
    }

    public boolean shouldDelayKeyguardServiceRestart(String shortName, boolean shouldDelay) {
        KeyguardServiceRestartDelay keyguardServiceRestartDelay;
        if (this.mIsSystemReady && (keyguardServiceRestartDelay = this.mKeyguardServiceRestartDelay) != null) {
            return keyguardServiceRestartDelay.shouldDelay(shortName, shouldDelay);
        }
        return false;
    }

    public void speedupSpecialAct(ActivityRecord actRec) {
        ActStartupAccelerator actStartupAccelerator = this.mActStartupAccelerator;
        if (actStartupAccelerator != null) {
            boolean res = actStartupAccelerator.speedupStartForSpecialApp(actRec);
            if (DEBUG_AMS) {
                Slog.d(TAG, "speedupSpecialAct res:" + res);
            }
        }
    }

    public void DumpEnvironment() {
        SystemProperties.set("sys.dumpenvironment.finished", "0");
        SystemProperties.set("ctl.start", "dumpenvironment");
        long begin = SystemClock.elapsedRealtime();
        int wait_time = SystemProperties.getInt("ro.dumpenvironment.time", 4000);
        while (SystemProperties.getInt("sys.dumpenvironment.finished", 0) != 1 && SystemClock.elapsedRealtime() - begin < ((long) wait_time)) {
            SystemClock.sleep(100);
        }
    }

    /* access modifiers changed from: private */
    public class ActStartFreqControler {
        private static final boolean DEBUG_EMULATE = false;
        private static final boolean DEBUG_FREQ_CONTROL = false;
        private static final long DECT_TIME_MAX_INTERVEL = 30000;
        private static final long DECT_TIME_MIN_INTERVEL = 4000;
        private static final int MIN_START_DECT_COUNT = 10;
        private ArrayList<ActStartFreqData> mFreqControlActList = new ArrayList<>();

        public ActStartFreqControler() {
            initActList();
        }

        private void initActList() {
            this.mFreqControlActList.add(new ActStartFreqData("com.android.permissioncontroller", "com.android.packageinstaller.permission.ui.GrantPermissionsActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("com.lianlian", "com.lianlian.activity.RetryAnonymousLoginActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("tv.danmaku.bili", "tv.danmaku.bili.ui.video.VideoDetailsActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("com.yy.hiyo", "com.yy.hiyo.MainActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("com.cashtoutiao", "com.cashtoutiao.account.ui.LoginOneKeyActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("com.if831c2cf3041abe0", "com.iapp.app.run.mian"));
        }

        private ActStartFreqData getActStartFreqData(String pkgName, String className) {
            int size = this.mFreqControlActList.size();
            for (int index = 0; index < size; index++) {
                ActStartFreqData tmpData = this.mFreqControlActList.get(index);
                if (tmpData != null && tmpData.mPkgName.equals(pkgName) && tmpData.mClassName.equals(className)) {
                    return tmpData;
                }
            }
            return null;
        }

        public boolean doControlActivityStartFreq(Intent intent) {
            ComponentName cmpName;
            ActStartFreqData data;
            if (intent == null || (cmpName = intent.getComponent()) == null) {
                return false;
            }
            String pkgName = cmpName.getPackageName();
            String className = cmpName.getClassName();
            if (pkgName == null || className == null || (data = getActStartFreqData(pkgName, className)) == null) {
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
                } else if (data.mCount >= 10) {
                    data.mIsFrequent = true;
                    data.mLastDectTime = curTime;
                }
                return false;
            } else if (timeElapsed <= 30000) {
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

    /* access modifiers changed from: private */
    public class ActStartFreqData {
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

    /* access modifiers changed from: private */
    public class ActStartupAccelerator {
        private static final int ACCE_DURATION_TIME = 1000;
        private static final String DATA_PATH_FOR_GAME_01 = "Android/data/com.dzxy.cscy.nearme.gamecenter/files/main/data/res";
        private static final String GAME_01_MAIN_ACT_NAME = "com.dzxy.cscy.nearme.gamecenter/org.cocos2dx.lua.AppActivity";
        private static final int MAX_SPEEDUP_ACTION_REPEAT_COUNT_GAME01 = 5;
        private static final int MAX_TIME_FOR_ACTION_IO = 60000;
        private static final String PKGNAME_SPECIALGAME_01 = "com.dzxy.cscy.nearme.gamecenter";
        private static final String PROP_ENABLE_SPECIAL_SPEEDUP = "sys.oppo.speedup";
        private boolean DEBUG_ACT_ACCE = true;
        private ArrayList<String> mAccelerateActList = new ArrayList<>();
        private boolean mEnableSpeedUpForSpecialGame = SystemProperties.getBoolean(PROP_ENABLE_SPECIAL_SPEEDUP, true);
        private Handler mHandlerForSpecialSpeedup = null;
        private long mLastAcceTime = 0;
        private LocalPerformance mLocalPerformance = null;
        private int[] mPerformanceConfigList = {1077936128, 1, 1086324736, 1, 1090519040, 2, 1082130432, 1800};
        private int mRemainSpeedupTimeForGame01 = 0;
        private int mSpeedActionCountGame01 = 0;
        private Runnable mSpeepupCallbackForGame01 = new Runnable() {
            /* class com.android.server.wm.OppoAmsUtils.ActStartupAccelerator.AnonymousClass1 */

            public void run() {
                ActStartupAccelerator.access$108(ActStartupAccelerator.this);
                if (ActStartupAccelerator.this.mSpeedActionCountGame01 < 5) {
                    ActStartupAccelerator.this.mHandlerForSpecialSpeedup.postDelayed(ActStartupAccelerator.this.mSpeepupCallbackForGame01, 60000);
                }
            }
        };
        private boolean mSupportActAccelerate = true;

        static /* synthetic */ int access$108(ActStartupAccelerator x0) {
            int i = x0.mSpeedActionCountGame01;
            x0.mSpeedActionCountGame01 = i + 1;
            return i;
        }

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
                String tmpData = this.mAccelerateActList.get(index);
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
            if (!this.mSupportActAccelerate) {
                return false;
            }
            ComponentName cmpName = intent != null ? intent.getComponent() : null;
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
            } else if (!isInAcceList(shortName)) {
                if (this.DEBUG_ACT_ACCE) {
                    Slog.d(OppoAmsUtils.TAG, "doAccelerateActStartup data not found in list.");
                }
                return false;
            } else {
                long curTime = SystemClock.elapsedRealtime();
                long j = this.mLastAcceTime;
                if (j > 0 && curTime - j <= 1000) {
                    return false;
                }
                this.mLastAcceTime = curTime;
                this.mLocalPerformance.perfLockAcquire(1000, this.mPerformanceConfigList);
                return true;
            }
        }

        public boolean speedupStartForSpecialApp(ActivityRecord actRecord) {
            String pkgName;
            ComponentName realActivity;
            if (this.mEnableSpeedUpForSpecialGame && actRecord != null && (pkgName = actRecord.packageName) != null && !pkgName.isEmpty() && PKGNAME_SPECIALGAME_01.equals(pkgName) && (realActivity = actRecord.mActivityComponent) != null) {
                String actName = realActivity.flattenToString();
                if (OppoAmsUtils.DEBUG_AMS) {
                    Slog.d(OppoAmsUtils.TAG, "actName:" + actName);
                }
                if (actName != null && GAME_01_MAIN_ACT_NAME.equals(actName)) {
                    return speedupForSpecialGame01();
                }
            }
            return false;
        }

        private boolean speedupForSpecialGame01() {
            Context context = OppoAmsUtils.this.mAtm.mContext;
            File[] fileList = new Environment.UserEnvironment(0).getExternalDirs();
            if (OppoAmsUtils.DEBUG_AMS && fileList != null && fileList.length > 0) {
                for (File file : fileList) {
                    Slog.d(OppoAmsUtils.TAG, "speedupForGame, file path:" + file.getPath());
                }
            }
            if (fileList == null || fileList.length <= 0 || new File(fileList[0], DATA_PATH_FOR_GAME_01).exists()) {
                return false;
            }
            if (this.mHandlerForSpecialSpeedup == null) {
                this.mHandlerForSpecialSpeedup = OppoAmsUtils.this.mAtm.mH;
            }
            if (OppoAmsUtils.DEBUG_AMS) {
                Slog.d(OppoAmsUtils.TAG, "speedupForGame post first action.");
            }
            this.mSpeedActionCountGame01 = 0;
            this.mHandlerForSpecialSpeedup.postDelayed(this.mSpeepupCallbackForGame01, 500);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class LocalPerformance {
        public LocalPerformance() {
        }

        public int perfLockAcquire(int duration, int... list) {
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public class KeyguardServiceRestartDelay {
        private static final String KEYGUARD_SERVICE = "com.android.keyguard/.KeyguardService";
        private static final int MAX_DELAY_COUNT = 4;
        private static final long RESTART_DETECT_TIME = 6;
        private int mDelayCount = 0;
        private long mDelayDetectTime = 0;

        public KeyguardServiceRestartDelay() {
        }

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
            } else {
                this.mDelayDetectTime = curTime;
                this.mDelayCount = 0;
            }
            return shouldDelay;
        }
    }
}
