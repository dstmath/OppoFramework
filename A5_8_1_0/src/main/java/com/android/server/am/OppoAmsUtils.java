package com.android.server.am;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment.UserEnvironment;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import java.io.File;
import java.util.ArrayList;

class OppoAmsUtils {
    private static boolean DEBUG_AMS = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final String FEATURE_ACT_FREQ_CONTROL = "oppo.ams.act.freqcontrol";
    static final String TAG = "OppoAmsUtils";
    private static OppoAmsUtils mInstance = null;
    private ActStartFreqControler mActStartFreqControler = null;
    private ActStartupAccelerator mActStartupAccelerator = null;
    final ActivityManagerService mAm;
    private boolean mIsSupportActFreqCon = false;
    private boolean mIsSystemReady = false;
    private KeyguardServiceRestartDelay mKeyguardServiceRestartDelay = null;

    private class ActStartFreqControler {
        private static final boolean DEBUG_EMULATE = false;
        private static final boolean DEBUG_FREQ_CONTROL = false;
        private static final long DECT_TIME_MAX_INTERVEL = 30000;
        private static final long DECT_TIME_MIN_INTERVEL = 4000;
        private static final int MIN_START_DECT_COUNT = 10;
        private ArrayList<ActStartFreqData> mFreqControlActList = new ArrayList();

        public ActStartFreqControler() {
            initActList();
        }

        private void initActList() {
            this.mFreqControlActList.add(new ActStartFreqData("com.android.packageinstaller", "com.android.packageinstaller.permission.ui.GrantPermissionsActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("com.lianlian", "com.lianlian.activity.RetryAnonymousLoginActivity"));
            this.mFreqControlActList.add(new ActStartFreqData("tv.danmaku.bili", "tv.danmaku.bili.ui.video.VideoDetailsActivity"));
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
        private static final String DATA_PATH_FOR_GAME_01 = "Android/data/com.dzxy.cscy.nearme.gamecenter/files/main/data/res";
        private static final String GAME_01_MAIN_ACT_NAME = "com.dzxy.cscy.nearme.gamecenter/org.cocos2dx.lua.AppActivity";
        private static final int MAX_SPEEDUP_ACTION_REPEAT_COUNT_GAME01 = 5;
        private static final int MAX_TIME_FOR_ACTION_IO = 60000;
        private static final String PKGNAME_SPECIALGAME_01 = "com.dzxy.cscy.nearme.gamecenter";
        private static final String PROP_ENABLE_SPECIAL_SPEEDUP = "sys.oppo.speedup";
        private boolean DEBUG_ACT_ACCE = true;
        private ArrayList<String> mAccelerateActList = new ArrayList();
        private boolean mEnableSpeedUpForSpecialGame = SystemProperties.getBoolean(PROP_ENABLE_SPECIAL_SPEEDUP, true);
        private Handler mHandlerForSpecialSpeedup = null;
        private long mLastAcceTime = 0;
        private LocalPerformance mLocalPerformance = null;
        private int[] mPerformanceConfigList = new int[]{1077936128, 1, 1086324736, 1, 1090519040, 2, 1082130432, 1800};
        private int mRemainSpeedupTimeForGame01 = 0;
        private int mSpeedActionCountGame01 = 0;
        private Runnable mSpeepupCallbackForGame01 = new Runnable() {
            public void run() {
                ActStartupAccelerator actStartupAccelerator = ActStartupAccelerator.this;
                actStartupAccelerator.mSpeedActionCountGame01 = actStartupAccelerator.mSpeedActionCountGame01 + 1;
                if (ActStartupAccelerator.this.mSpeedActionCountGame01 < 5) {
                    ActStartupAccelerator.this.mHandlerForSpecialSpeedup.postDelayed(ActStartupAccelerator.this.mSpeepupCallbackForGame01, 60000);
                }
            }
        };
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

        /* JADX WARNING: Missing block: B:9:0x0013, code:
            return false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean speedupStartForSpecialApp(ActivityRecord actRecord) {
            if (!this.mEnableSpeedUpForSpecialGame || actRecord == null) {
                return false;
            }
            String pkgName = actRecord.packageName;
            if (!(pkgName == null || pkgName.isEmpty() || !PKGNAME_SPECIALGAME_01.equals(pkgName))) {
                ComponentName realActivity = actRecord.realActivity;
                if (realActivity != null) {
                    String actName = realActivity.flattenToString();
                    if (OppoAmsUtils.DEBUG_AMS) {
                        Slog.d(OppoAmsUtils.TAG, "actName:" + actName);
                    }
                    if (actName != null && GAME_01_MAIN_ACT_NAME.equals(actName)) {
                        return speedupForSpecialGame01();
                    }
                }
            }
            return false;
        }

        /* JADX WARNING: Missing block: B:11:0x0047, code:
            return false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean speedupForSpecialGame01() {
            Context context = OppoAmsUtils.this.mAm.mContext;
            File[] fileList = new UserEnvironment(0).getExternalDirs();
            if (OppoAmsUtils.DEBUG_AMS && fileList != null && fileList.length > 0) {
                for (File file : fileList) {
                    Slog.d(OppoAmsUtils.TAG, "speedupForGame, file path:" + file.getPath());
                }
            }
            if (fileList == null || fileList.length <= 0 || new File(fileList[0], DATA_PATH_FOR_GAME_01).exists()) {
                return false;
            }
            if (this.mHandlerForSpecialSpeedup == null) {
                this.mHandlerForSpecialSpeedup = OppoAmsUtils.this.mAm.mHandler;
            }
            if (OppoAmsUtils.DEBUG_AMS) {
                Slog.d(OppoAmsUtils.TAG, "speedupForGame post first action.");
            }
            this.mSpeedActionCountGame01 = 0;
            this.mHandlerForSpecialSpeedup.postDelayed(this.mSpeepupCallbackForGame01, 500);
            return true;
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

    public static OppoAmsUtils getInstance(ActivityManagerService service) {
        if (mInstance == null) {
            mInstance = new OppoAmsUtils(service);
        }
        return mInstance;
    }

    private OppoAmsUtils(ActivityManagerService service) {
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

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean needToControlActivityStartFreq(Intent intent) {
        if (this.mIsSystemReady && (this.mIsSupportActFreqCon ^ 1) == 0 && this.mActStartFreqControler != null) {
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

    public void speedupSpecialAct(ActivityRecord actRec) {
        if (this.mActStartupAccelerator != null) {
            boolean res = this.mActStartupAccelerator.speedupStartForSpecialApp(actRec);
            if (DEBUG_AMS) {
                Slog.d(TAG, "speedupSpecialAct res:" + res);
            }
        }
    }
}
