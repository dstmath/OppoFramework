package com.android.server.am;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.LongArrayQueue;
import android.util.SparseArray;
import com.android.server.ColorSmartDozeHelper;
import com.android.server.am.ColorHansManager;
import com.android.server.am.HansLcdOnScene;
import com.android.server.wm.ColorAppSwitchManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ColorHansPackageSelector {
    static final int CPN_UNFREEZE_DEFAULT = 1;
    static final int CPN_UNFREEZE_FOR_ALARM = 4;
    static final int CPN_UNFREEZE_FOR_ALL = 2;
    static final int CPN_UNFREEZE_FOR_NET = 8;
    static final int CPN_UNFREEZE_FOR_SYNC = 16;
    static final int FREEZE_LEVEL_FOUR = 4;
    static final int FREEZE_LEVEL_ONE = 1;
    static final int FREEZE_LEVEL_THREE = 3;
    static final int FREEZE_LEVEL_TWO = 2;
    static final int HANS_GMS_APP_LIST = 256;
    static final int HANS_OPPO_WHITELIST = 4;
    static final int HANS_THIRD_APP_LIST = 1;
    static final int HANS_THIRD_WHITELIST = 2;
    static final int TARGETMAP_UPDATETYPE_ALL = 1;
    static final int TARGETMAP_UPDATETYPE_GMS = 16;
    static final int TARGETMAP_UPDATETYPE_NORMAL_SINGLE_APP = 8;
    static final int TARGETMAP_UPDATETYPE_OPPO_SINGLE_APP = 2;
    static final int TARGETMAP_UPDATETYPE_WHITE_SINGLE_APP = 4;
    private static volatile ColorHansPackageSelector sInstance = null;
    private Context mContext;
    private DBConfig mDBConfig;
    private final SparseArray<HansPackage> mGMSList;
    private final Object mHansLock;
    private final SparseArray<HansPackage> mOppoWhiteList;
    private final SparseArray<HansPackage> mThirdAppList;
    private final SparseArray<HansPackage> mThirdWhiteList;
    final SparseArray<HansPackage> mTmpAddGMSList;
    final SparseArray<HansPackage> mTmpAddOppoWhiteList;
    final SparseArray<HansPackage> mTmpAddThirdAppList;
    final SparseArray<HansPackage> mTmpAddThirdWhiteList;
    final SparseArray<HansPackage> mTmpRmGMSList;
    final SparseArray<HansPackage> mTmpRmOppoWhiteList;
    final SparseArray<HansPackage> mTmpRmThirdAppList;
    final SparseArray<HansPackage> mTmpRmThirdWhiteList;

    private ColorHansPackageSelector() {
        this.mContext = null;
        this.mDBConfig = null;
        this.mHansLock = ColorHansManager.getInstance().getHansLock();
        this.mThirdAppList = new SparseArray<>();
        this.mThirdWhiteList = new SparseArray<>();
        this.mOppoWhiteList = new SparseArray<>();
        this.mGMSList = new SparseArray<>();
        this.mTmpAddThirdAppList = new SparseArray<>();
        this.mTmpRmThirdAppList = new SparseArray<>();
        this.mTmpAddThirdWhiteList = new SparseArray<>();
        this.mTmpRmThirdWhiteList = new SparseArray<>();
        this.mTmpAddOppoWhiteList = new SparseArray<>();
        this.mTmpRmOppoWhiteList = new SparseArray<>();
        this.mTmpAddGMSList = new SparseArray<>();
        this.mTmpRmGMSList = new SparseArray<>();
        this.mDBConfig = new DBConfig();
    }

    public static ColorHansPackageSelector getInstance() {
        if (sInstance == null) {
            synchronized (ColorHansPackageSelector.class) {
                if (sInstance == null) {
                    sInstance = new ColorHansPackageSelector();
                }
            }
        }
        return sInstance;
    }

    public SparseArray<HansPackage> getHansPackageMap(int flag) {
        SparseArray<HansPackage> targetMap = new SparseArray<>();
        synchronized (this.mHansLock) {
            if ((flag & 1) != 0) {
                for (int i = 0; i < this.mThirdAppList.size(); i++) {
                    try {
                        targetMap.append(this.mThirdAppList.keyAt(i), this.mThirdAppList.valueAt(i));
                    } finally {
                    }
                }
            }
            if ((flag & 2) != 0) {
                for (int i2 = 0; i2 < this.mThirdWhiteList.size(); i2++) {
                    targetMap.append(this.mThirdWhiteList.keyAt(i2), this.mThirdWhiteList.valueAt(i2));
                }
            }
            if ((flag & 4) != 0) {
                for (int i3 = 0; i3 < this.mOppoWhiteList.size(); i3++) {
                    targetMap.append(this.mOppoWhiteList.keyAt(i3), this.mOppoWhiteList.valueAt(i3));
                }
            }
            if ((flag & HANS_GMS_APP_LIST) != 0) {
                for (int i4 = 0; i4 < this.mGMSList.size(); i4++) {
                    targetMap.append(this.mGMSList.keyAt(i4), this.mGMSList.valueAt(i4));
                }
            }
        }
        return targetMap;
    }

    public boolean updateHansPackage(int uid, String pkgName, int freezeLevel, boolean isAdd) {
        synchronized (this.mHansLock) {
            HansPackage hansPackage = this.mThirdAppList.get(uid);
            if (hansPackage == null || !hansPackage.getPkgName().equals(pkgName)) {
                HansPackage hansPackage2 = this.mThirdWhiteList.get(uid);
                if (hansPackage2 == null || !hansPackage2.getPkgName().equals(pkgName)) {
                    HansPackage hansPackage3 = this.mOppoWhiteList.get(uid);
                    if (hansPackage3 == null || !hansPackage3.getPkgName().equals(pkgName)) {
                        return false;
                    }
                    if (isAdd) {
                        hansPackage3.setLastFreezeLevel(hansPackage3.getFreezeLevel());
                        hansPackage3.setFreezeLevel(freezeLevel);
                    } else {
                        hansPackage3.setFreezeLevel(hansPackage3.getLastFreezeLevel());
                    }
                    return true;
                }
                if (isAdd) {
                    hansPackage2.setLastFreezeLevel(hansPackage2.getFreezeLevel());
                    hansPackage2.setFreezeLevel(freezeLevel);
                } else {
                    hansPackage2.setFreezeLevel(hansPackage2.getLastFreezeLevel());
                }
                return true;
            }
            if (isAdd) {
                hansPackage.setLastFreezeLevel(hansPackage.getFreezeLevel());
                hansPackage.setFreezeLevel(freezeLevel);
            } else {
                hansPackage.setFreezeLevel(hansPackage.getLastFreezeLevel());
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHansPackageMap(SparseArray<HansPackage> oppoWhiteList, SparseArray<HansPackage> thirdWhiteList, SparseArray<HansPackage> freezeList, SparseArray<HansPackage> gpsList) {
        synchronized (this.mHansLock) {
            if (oppoWhiteList != null) {
                try {
                    updateHansAppMap(this.mOppoWhiteList, oppoWhiteList, this.mTmpAddOppoWhiteList, this.mTmpRmOppoWhiteList);
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (thirdWhiteList != null) {
                updateHansAppMap(this.mThirdWhiteList, thirdWhiteList, this.mTmpAddThirdWhiteList, this.mTmpRmThirdWhiteList);
            }
            if (freezeList != null) {
                updateHansAppMap(this.mThirdAppList, freezeList, this.mTmpAddThirdAppList, this.mTmpRmThirdAppList);
            }
            if (gpsList != null) {
                updateHansAppMap(this.mGMSList, gpsList, this.mTmpAddGMSList, this.mTmpRmGMSList);
            }
            ColorHansManager.getInstance().updateTargetMapForScenes(1, null);
            clearTemMap();
        }
    }

    private void updateHansAppMap(SparseArray<HansPackage> oldList, SparseArray<HansPackage> newList, SparseArray<HansPackage> addList, SparseArray<HansPackage> rmList) {
        for (int i = oldList.size() - 1; i >= 0; i--) {
            int uid = oldList.keyAt(i);
            if (newList.get(uid) == null) {
                rmList.put(uid, oldList.valueAt(i));
                oldList.remove(uid);
            }
        }
        for (int j = 0; j < newList.size(); j++) {
            int uid2 = newList.keyAt(j);
            if (oldList.get(uid2) == null) {
                HansPackage hansPackage = newList.valueAt(j);
                addList.put(uid2, hansPackage);
                oldList.put(uid2, hansPackage);
            }
        }
    }

    private void clearTemMap() {
        this.mTmpAddThirdAppList.clear();
        this.mTmpRmThirdAppList.clear();
        this.mTmpAddThirdWhiteList.clear();
        this.mTmpRmThirdWhiteList.clear();
        this.mTmpAddOppoWhiteList.clear();
        this.mTmpRmOppoWhiteList.clear();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSingleHansPackage(HansPackage hansPackage) {
        synchronized (this.mHansLock) {
            int appClass = hansPackage.getAppClass();
            if (appClass == 1) {
                updateTargetListForScenes(hansPackage, this.mOppoWhiteList, 2);
            } else if (appClass == 2) {
                updateTargetListForScenes(hansPackage, this.mThirdWhiteList, 4);
            } else if (appClass == 3) {
                updateTargetListForScenes(hansPackage, this.mThirdAppList, 8);
            }
        }
    }

    private void updateTargetListForScenes(HansPackage targetPkg, SparseArray<HansPackage> targetList, int targetUpdateFlag) {
        if (targetPkg.getSaveMode() == 8) {
            if (targetList.get(targetPkg.getUid()) != null) {
                ColorHansManager.getInstance().updateTargetMapForScenes(targetUpdateFlag, targetPkg);
                targetList.remove(targetPkg.getUid());
            }
        } else if (targetPkg.getSaveMode() == 4) {
            if (targetList.get(targetPkg.getUid()) == null) {
                targetList.put(targetPkg.getUid(), targetPkg);
                ColorHansManager.getInstance().updateTargetMapForScenes(targetUpdateFlag, targetPkg);
            }
        } else if (targetPkg.getSaveMode() == 1) {
            if (targetList.get(targetPkg.getUid()) == null) {
                ColorHansManager.getInstance().updateTargetMapForScenes(targetUpdateFlag, targetPkg);
                targetList.put(targetPkg.getUid(), targetPkg);
            }
            if (this.mThirdAppList.get(targetPkg.getUid()) != null) {
                this.mThirdAppList.remove(targetPkg.getUid());
            }
        } else if (targetPkg.getSaveMode() == 2) {
            if (this.mThirdAppList.get(targetPkg.getUid()) == null) {
                this.mThirdAppList.put(targetPkg.getUid(), targetPkg);
                ColorHansManager.getInstance().updateTargetMapForScenes(targetUpdateFlag, targetPkg);
            }
            if (this.mOppoWhiteList.get(targetPkg.getUid()) != null) {
                this.mOppoWhiteList.remove(targetPkg.getUid());
            }
            if (this.mThirdWhiteList.get(targetPkg.getUid()) != null) {
                this.mThirdWhiteList.remove(targetPkg.getUid());
            }
        } else if (targetPkg.getSaveMode() == 16 && targetList.get(targetPkg.getUid()) != null) {
            ColorHansManager.getInstance().updateTargetMapForScenes(targetUpdateFlag, targetPkg);
        }
    }

    /* access modifiers changed from: package-private */
    public final class HansPackage {
        private boolean isPendUFZ;
        private int mAllowCpnType;
        private int mAppClass;
        private int mAppType;
        private long mEnterBgTime = 0;
        private long mFreezeElapsedTime = 0;
        private int mFreezeLevel = -1;
        private String mFreezeReason = "";
        private long mFreezeTime = 0;
        private boolean mFreezed = false;
        private ArrayList<Integer> mFrozenPidList = new ArrayList<>();
        private boolean mHasForegroundService = false;
        private String mImportantReason = "";
        private int mJobWakelock;
        private int mLastFreezeLevel;
        private long mLastUsedTime = 0;
        private String mPkgName;
        private int mSaveMode;
        private int mScene = -1;
        private boolean mSharedUid = false;
        HansLcdOnScene.HansStateMachine mStateMachine;
        private int mUid;
        private LongArrayQueue mUnFreezeQueueTimes;
        private String mUnFreezeReason = "";
        private long mUnFreezeTime = 0;
        private String pendingReason;
        private String strUid;

        public HansPackage(String pkgName, int uid) {
            this.mPkgName = pkgName;
            this.mUid = uid;
            HansLcdOnScene hansLcdOnScene = ColorHansManager.getInstance().hansLcdOnScene;
            Objects.requireNonNull(hansLcdOnScene);
            this.mStateMachine = new HansLcdOnScene.HansStateMachine();
            this.mAppType = -1;
            this.mAllowCpnType = 0;
            this.isPendUFZ = false;
            this.pendingReason = "";
            this.mAppClass = -1;
            this.strUid = String.valueOf(uid);
            this.mUnFreezeQueueTimes = new LongArrayQueue(5);
        }

        public String getPendingReason() {
            return this.pendingReason;
        }

        public void setPendingReason(String pendingReason2) {
            this.pendingReason = pendingReason2;
        }

        public boolean isPendUFZ() {
            return this.isPendUFZ;
        }

        public void setPendUFZ(boolean pendUFZ) {
            this.isPendUFZ = pendUFZ;
        }

        public HansLcdOnScene.HansStateMachine getStateMachine() {
            return this.mStateMachine;
        }

        public void setStateMachine(HansLcdOnScene.HansStateMachine stateMachine) {
            this.mStateMachine = stateMachine;
        }

        public String getPkgName() {
            return this.mPkgName;
        }

        public int getUid() {
            return this.mUid;
        }

        public long getFreezeTime() {
            return this.mFreezeTime;
        }

        public void setFreezeTime(long freezeTime) {
            this.mFreezeTime = freezeTime;
        }

        public long getUnFreezeTime() {
            return this.mUnFreezeTime;
        }

        public void setUnFreezeTime(long unFreezeTime) {
            this.mUnFreezeTime = unFreezeTime;
        }

        public String getFreezeReason() {
            return this.mFreezeReason;
        }

        public void setFreezeReason(String freezeReason) {
            this.mFreezeReason = freezeReason;
        }

        public String getUnFreezeReason() {
            return this.mUnFreezeReason;
        }

        public void setUnFreezeReason(String unFreezeReason) {
            this.mUnFreezeReason = unFreezeReason;
        }

        public boolean isSharedUid() {
            return this.mSharedUid;
        }

        public void setSharedUid(boolean sharedUid) {
            this.mSharedUid = sharedUid;
        }

        public void setFreezed(boolean isFroze) {
            this.mFreezed = isFroze;
        }

        public boolean getFreezed() {
            return this.mFreezed;
        }

        public String getImportantReason() {
            return this.mImportantReason;
        }

        public void setImportantReason(String importantReason) {
            this.mImportantReason = importantReason;
        }

        public long getLastUsedTime() {
            return this.mLastUsedTime;
        }

        public void setLastUsedTime(long usedTime) {
            this.mLastUsedTime = usedTime;
        }

        public void setFgService(boolean isForeground) {
            this.mHasForegroundService = isForeground;
        }

        public boolean getFgService() {
            return this.mHasForegroundService;
        }

        public long getEnterBgTime() {
            return this.mEnterBgTime;
        }

        public void setEnterBgTime(long time) {
            this.mEnterBgTime = time;
        }

        public long getFreezeElapsedTime() {
            return this.mFreezeElapsedTime;
        }

        public void setFreezeElapsedTime(long time) {
            this.mFreezeElapsedTime = time;
        }

        public ArrayList<Integer> getFrozenPidList() {
            return this.mFrozenPidList;
        }

        public void setFrozenPidList(List<Integer> list) {
            ArrayList<Integer> arrayList = this.mFrozenPidList;
            if (arrayList != null) {
                arrayList.clear();
                this.mFrozenPidList.addAll(list);
            }
        }

        public int getScene() {
            return this.mScene;
        }

        public void setScene(int scene) {
            this.mScene = scene;
        }

        public int getFreezeLevel() {
            return this.mFreezeLevel;
        }

        public void setFreezeLevel(int freezeLevel) {
            this.mFreezeLevel = freezeLevel;
        }

        public void setAppType(int appType) {
            this.mAppType = appType;
        }

        public int getAppType() {
            return this.mAppType;
        }

        public void setAllowCpnType(int type) {
            this.mAllowCpnType = type;
        }

        public int getAllowCpnType() {
            return this.mAllowCpnType;
        }

        public void setAppClass(int appClass) {
            this.mAppClass = appClass;
        }

        public int getAppClass() {
            return this.mAppClass;
        }

        public void setSaveMode(int mode) {
            this.mSaveMode = mode;
        }

        public int getSaveMode() {
            return this.mSaveMode;
        }

        public String getStrUid() {
            return this.strUid;
        }

        public void setLastFreezeLevel(int level) {
            this.mLastFreezeLevel = level;
        }

        public int getLastFreezeLevel() {
            return this.mLastFreezeLevel;
        }

        public void setJobWakelock(int curState) {
            this.mJobWakelock = curState;
        }

        public int getJobWakelock() {
            return this.mJobWakelock;
        }

        public boolean recordUnFreezeQueueTime(long time) {
            try {
                this.mUnFreezeQueueTimes.addLast(time);
                if (time - this.mUnFreezeQueueTimes.peekFirst() > ColorHansPackageSelector.this.mDBConfig.mAbnormalAppCheckIntervel) {
                    this.mUnFreezeQueueTimes.removeFirst();
                }
                if (this.mUnFreezeQueueTimes.size() >= ColorHansPackageSelector.this.mDBConfig.mAbnormalCheckNum) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }

        public void clearUnFreezeQueueTime() {
            if (this.mUnFreezeQueueTimes.size() > 0) {
                this.mUnFreezeQueueTimes.clear();
            }
        }

        public String toString() {
            return "{pkgName=" + this.mPkgName + ", uid=" + this.mUid + ", appType=" + this.mAppType + ", freezeLevel=" + this.mFreezeLevel + ", freezed=" + this.mFreezed + '}';
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HansPackage that = (HansPackage) o;
            if (this.mUid != that.mUid || !this.mPkgName.equals(that.mPkgName)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.mPkgName, Integer.valueOf(this.mUid));
        }

        public void cloneHans(HansPackage source) {
            this.mUid = source.getUid();
            this.mPkgName = source.getPkgName();
            this.mFreezeLevel = source.getFreezeLevel();
            this.mAppType = source.getAppType();
            this.mAllowCpnType = source.getAllowCpnType();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateHansConfig(Bundle data) {
        return this.mDBConfig.updateHansConfig(data);
    }

    public DBConfig getDBConfig() {
        return this.mDBConfig;
    }

    /* access modifiers changed from: package-private */
    public final class DBConfig {
        static final int FILE_MODE_ADD = 4;
        static final int FILE_MODE_FORBID = 2;
        static final int FILE_MODE_REMOVE = 8;
        static final int FILE_MODE_RUNNING = 1;
        static final int FILE_MODE_UPDATE = 16;
        static final String KEY_ABNORMAL_CHECK_INTERVAL = "scrOffAbnormalCheckTime";
        static final String KEY_ABNORMAL_KILL_WHITE_LIST = "abnormalKillWhiteList";
        static final String KEY_ABNORMAL_NUM = "scrOffAbnormalCheckNum";
        static final String KEY_ACTIVITY_CALLER_BLACK_LIST = "activityCallerBlackList";
        static final String KEY_ACTIVITY_CALLER_WHITE_LIST = "activityCallerWhiteList";
        static final String KEY_ACTIVITY_CPN_BLACK_LIST = "activityCpnBlackList";
        static final String KEY_ACTIVITY_CPN_WHITE_LIST = "activityCpnWhiteList";
        static final String KEY_ALARM_KEY_ACTION_WHITE_LIST = "alarmKeyActionWhiteList";
        static final String KEY_ALARM_KEY_PKG_WHITE_LIST = "alarmKeyPkgWhiteList";
        static final String KEY_ALARM_WHITE_LIST = "alarmWhiteList";
        static final String KEY_ALL = "all";
        static final String KEY_ALL_CONFIG = "allConfig";
        static final String KEY_APP_ALLOW_CPNTYPE = "allowCpnType";
        static final String KEY_APP_CLASS = "appClass";
        static final String KEY_APP_FREEZELEVEL = "freezeLevel";
        static final String KEY_APP_TYPE = "appType";
        static final String KEY_ASYNC_BINDER_CODE_WHITE_LIST = "asyncBinderCodeWhiteList";
        static final String KEY_ASYNC_BINDER_PKG_WHITE_LIST = "asyncBinderPkgWhiteList";
        static final String KEY_BROADCAST_CALLER_BLACK_LIST = "broadcastCallerBlackList";
        static final String KEY_BROADCAST_CALLER_WHITE_LIST = "broadcastCallerWhiteList";
        static final String KEY_BROADCAST_CPN_BLACK_LIST = "broadcastCpnBlackList";
        static final String KEY_BROADCAST_CPN_WHITE_LIST = "broadcastCpnWhiteList";
        static final String KEY_BROADCAST_PENDING_LIST = "broadcastPendingList";
        static final String KEY_FASTFREEZE_TIMEOUT = "fastFreezeTimeout";
        static final String KEY_FREEZE_LIST = "freezeList";
        static final String KEY_GMS_LIST = "gmsList";
        static final String KEY_GMS_SWITCH = "gmsSwitch";
        static final String KEY_HANS_SWITCH = "hansSwitch";
        static final String KEY_IMPORTANT_CHECKTIME = "importantCheckTime";
        static final String KEY_JOB_WHITE_LIST = "jobWhiteList";
        static final String KEY_LCDOFF_CHECKTIME = "lcdOffCheckTime";
        static final String KEY_LCDOFF_REPEATTIME = "lcdOffRepeatTime";
        static final String KEY_MODE = "mode";
        static final String KEY_NET_PACKET_WHITE_LIST = "netPacketWhiteList";
        static final String KEY_NIGHT_REPEATTIME = "nightRepeatTime";
        static final String KEY_OPPO_APP = "OPPOWhiteAppList";
        static final String KEY_OTHER_WHITE_APP = "OtherWhiteAppList";
        static final String KEY_PKG_NAME = "pkgName";
        static final String KEY_PROVIDER_CALLER_BLACK_LIST = "providerCallerBlackList";
        static final String KEY_PROVIDER_CALLER_WHITE_LIST = "providerCallerWhiteList";
        static final String KEY_PROVIDER_CPN_BLACK_LIST = "providerCpnBlackList";
        static final String KEY_PROVIDER_CPN_WHITE_LIST = "providerCpnWhiteList";
        static final String KEY_SERVICE_CALLER_BLACK_LIST = "serviceCallerBlackList";
        static final String KEY_SERVICE_CALLER_WHITE_LIST = "serviceCallerWhiteList";
        static final String KEY_SERVICE_CPN_BLACK_LIST = "serviceCpnBlackList";
        static final String KEY_SERVICE_CPN_WHITE_LIST = "serviceCpnWhiteList";
        static final String KEY_STATE_CHANGE_CHECKTIME = "stateChangeCheckTime";
        static final String KEY_STATISTICS_SWITCH = "statisticsSwitch";
        static final String KEY_SYNC_WHITE_LIST = "syncWhiteList";
        static final String KEY_UID = "uid";
        static final int OPPO_WHITE_APP = 1;
        static final int OTHER_APP = 3;
        static final int THIRD_WHITE_APP = 2;
        private long mAbnormalAppCheckIntervel = ColorSmartDozeHelper.DEBUG_GPS_EXEPTION_TIME;
        private int mAbnormalCheckNum = 3;
        private final ArrayList<String> mAbnormalKillWhiteList = new ArrayList<>();
        private final ArrayList<String> mActivityCallerBlackList = new ArrayList<>();
        private final ArrayList<String> mActivityCallerWhiteList = new ArrayList<>();
        private final ArrayMap<String, ArrayList<String>> mActivityCpnBlackList = new ArrayMap<>();
        private final ArrayMap<String, ArrayList<String>> mActivityCpnWhiteList = new ArrayMap<>();
        private final ArrayList<String> mAlarmKeyPkgWhiteList = new ArrayList<>();
        private final ArrayMap<String, ArrayList<String>> mAlarmWhiteList = new ArrayMap<>();
        private final ArrayList<String> mAlarmkeyActionWhiteList = new ArrayList<>();
        private final ArrayMap<String, ArrayList<String>> mAsyncBinderCodeWhiteList = new ArrayMap<>();
        private final ArrayMap<String, ArrayList<String>> mAsyncBinderPkgWhiteList = new ArrayMap<>();
        private final ArrayList<String> mBrdPendingList = new ArrayList<>();
        private final ArrayList<String> mBroadcastCallerBlackList = new ArrayList<>();
        private final ArrayList<String> mBroadcastCallerWhiteList = new ArrayList<>();
        private final ArrayMap<String, ArrayList<String>> mBroadcastCpnBlackList = new ArrayMap<>();
        private final ArrayMap<String, ArrayList<String>> mBroadcastCpnWhiteList = new ArrayMap<>();
        private long mFastFreezeTimeout = 1500;
        private boolean mGmsEnable = true;
        private boolean mHansEnable = true;
        private long mImportantRepeatTime = ColorAppSwitchManager.INTERVAL;
        private final ArrayList<String> mJobWhiteList = new ArrayList<>();
        private long mLcdOffSceneInterval = 5000;
        private long mLcdOffSceneRepeatInterval = 60000;
        private final ArrayList<String> mNetPacketWhiteList = new ArrayList<>();
        private long mNightSceneRepeatInterval = ColorAppSwitchManager.INTERVAL;
        private final ArrayList<String> mProviderCallerBlackList = new ArrayList<>();
        private final ArrayList<String> mProviderCallerWhiteList = new ArrayList<>();
        private final ArrayMap<String, ArrayList<String>> mProviderCpnBlackList = new ArrayMap<>();
        private final ArrayMap<String, ArrayList<String>> mProviderCpnWhiteList = new ArrayMap<>();
        private final ArrayList<String> mServiceCallerBlackList = new ArrayList<>();
        private final ArrayList<String> mServiceCallerWhiteList = new ArrayList<>();
        private final ArrayMap<String, ArrayList<String>> mServiceCpnBlackList = new ArrayMap<>();
        private final ArrayMap<String, ArrayList<String>> mServiceCpnWhiteList = new ArrayMap<>();
        private long mStateChangeDelayTime = 15000;
        private boolean mStatisticsSwitch = false;
        private final ArrayList<String> mSyncWhiteList = new ArrayList<>();

        public DBConfig() {
        }

        public boolean updateHansConfig(Bundle data) {
            if (data == null) {
                return false;
            }
            boolean isAllConfig = data.getBoolean(KEY_ALL_CONFIG, true);
            if (ColorHansManager.getInstance().getHansLogger() != null) {
                ColorHansManager.HansLogger hansLogger = ColorHansManager.getInstance().getHansLogger();
                hansLogger.d("ColorHansPackageSelector -> updateHansConfig(), isAllConfig=" + isAllConfig);
            }
            if (isAllConfig) {
                this.mHansEnable = data.getBoolean(KEY_HANS_SWITCH, true);
                this.mGmsEnable = data.getBoolean(KEY_GMS_SWITCH, true);
                this.mStatisticsSwitch = data.getBoolean(KEY_STATISTICS_SWITCH, false);
                this.mStateChangeDelayTime = data.getLong(KEY_STATE_CHANGE_CHECKTIME, 15000);
                this.mImportantRepeatTime = data.getLong(KEY_IMPORTANT_CHECKTIME, ColorAppSwitchManager.INTERVAL);
                this.mFastFreezeTimeout = data.getLong(KEY_FASTFREEZE_TIMEOUT, 1500);
                this.mLcdOffSceneInterval = data.getLong(KEY_LCDOFF_CHECKTIME, 5000);
                this.mLcdOffSceneRepeatInterval = data.getLong(KEY_LCDOFF_REPEATTIME, 60000);
                this.mNightSceneRepeatInterval = data.getLong(KEY_NIGHT_REPEATTIME, ColorAppSwitchManager.INTERVAL);
                this.mAbnormalAppCheckIntervel = data.getLong(KEY_ABNORMAL_CHECK_INTERVAL, ColorSmartDozeHelper.DEBUG_GPS_EXEPTION_TIME);
                this.mAbnormalCheckNum = data.getInt(KEY_ABNORMAL_NUM, 3);
                ColorHansManager.getInstance().updatePolicyConfig();
                synchronized (this.mSyncWhiteList) {
                    updateArrayList(this.mSyncWhiteList, data.getStringArrayList(KEY_SYNC_WHITE_LIST));
                }
                synchronized (this.mJobWhiteList) {
                    updateArrayList(this.mJobWhiteList, data.getStringArrayList(KEY_JOB_WHITE_LIST));
                }
                synchronized (this.mAlarmWhiteList) {
                    updateArrayMap(this.mAlarmWhiteList, getConvertMap(data.getStringArrayList(KEY_ALARM_WHITE_LIST)));
                }
                synchronized (this.mAlarmKeyPkgWhiteList) {
                    updateArrayList(this.mAlarmKeyPkgWhiteList, data.getStringArrayList(KEY_ALARM_KEY_PKG_WHITE_LIST));
                }
                synchronized (this.mAlarmkeyActionWhiteList) {
                    updateArrayList(this.mAlarmkeyActionWhiteList, data.getStringArrayList(KEY_ALARM_KEY_ACTION_WHITE_LIST));
                }
                synchronized (this.mActivityCpnWhiteList) {
                    updateArrayMap(this.mActivityCpnWhiteList, getConvertMap(data.getStringArrayList(KEY_ACTIVITY_CPN_WHITE_LIST)));
                }
                synchronized (this.mActivityCpnBlackList) {
                    updateArrayMap(this.mActivityCpnBlackList, getConvertMap(data.getStringArrayList(KEY_ACTIVITY_CPN_BLACK_LIST)));
                }
                synchronized (this.mServiceCpnWhiteList) {
                    updateArrayMap(this.mServiceCpnWhiteList, getConvertMap(data.getStringArrayList(KEY_SERVICE_CPN_WHITE_LIST)));
                }
                synchronized (this.mServiceCpnBlackList) {
                    updateArrayMap(this.mServiceCpnBlackList, getConvertMap(data.getStringArrayList(KEY_SERVICE_CPN_BLACK_LIST)));
                }
                synchronized (this.mProviderCpnWhiteList) {
                    updateArrayMap(this.mProviderCpnWhiteList, getConvertMap(data.getStringArrayList(KEY_PROVIDER_CPN_WHITE_LIST)));
                }
                synchronized (this.mProviderCpnBlackList) {
                    updateArrayMap(this.mProviderCpnBlackList, getConvertMap(data.getStringArrayList(KEY_PROVIDER_CPN_BLACK_LIST)));
                }
                synchronized (this.mBroadcastCpnWhiteList) {
                    updateArrayMap(this.mBroadcastCpnWhiteList, getConvertMap(data.getStringArrayList(KEY_BROADCAST_CPN_WHITE_LIST)));
                }
                synchronized (this.mBroadcastCpnBlackList) {
                    updateArrayMap(this.mBroadcastCpnBlackList, getConvertMap(data.getStringArrayList(KEY_BROADCAST_CPN_BLACK_LIST)));
                }
                synchronized (this.mAsyncBinderPkgWhiteList) {
                    updateArrayMap(this.mAsyncBinderPkgWhiteList, getConvertMap(data.getStringArrayList(KEY_ASYNC_BINDER_PKG_WHITE_LIST)));
                }
                synchronized (this.mAsyncBinderCodeWhiteList) {
                    updateArrayMap(this.mAsyncBinderCodeWhiteList, getConvertMap(data.getStringArrayList(KEY_ASYNC_BINDER_CODE_WHITE_LIST)));
                }
                synchronized (this.mActivityCallerWhiteList) {
                    updateArrayList(this.mActivityCallerWhiteList, data.getStringArrayList(KEY_ACTIVITY_CALLER_WHITE_LIST));
                }
                synchronized (this.mActivityCallerBlackList) {
                    updateArrayList(this.mActivityCallerBlackList, data.getStringArrayList(KEY_ACTIVITY_CALLER_BLACK_LIST));
                }
                synchronized (this.mServiceCallerWhiteList) {
                    updateArrayList(this.mServiceCallerWhiteList, data.getStringArrayList(KEY_SERVICE_CALLER_WHITE_LIST));
                }
                synchronized (this.mServiceCallerBlackList) {
                    updateArrayList(this.mServiceCallerBlackList, data.getStringArrayList(KEY_SERVICE_CALLER_BLACK_LIST));
                }
                synchronized (this.mProviderCallerWhiteList) {
                    updateArrayList(this.mProviderCallerWhiteList, data.getStringArrayList(KEY_PROVIDER_CALLER_WHITE_LIST));
                }
                synchronized (this.mProviderCallerBlackList) {
                    updateArrayList(this.mProviderCallerBlackList, data.getStringArrayList(KEY_PROVIDER_CALLER_BLACK_LIST));
                }
                synchronized (this.mBroadcastCallerWhiteList) {
                    updateArrayList(this.mBroadcastCallerWhiteList, data.getStringArrayList(KEY_BROADCAST_CALLER_WHITE_LIST));
                }
                synchronized (this.mBroadcastCallerBlackList) {
                    updateArrayList(this.mBroadcastCallerBlackList, data.getStringArrayList(KEY_BROADCAST_CALLER_BLACK_LIST));
                }
                synchronized (this.mBrdPendingList) {
                    updateArrayList(this.mBrdPendingList, data.getStringArrayList(KEY_BROADCAST_PENDING_LIST));
                }
                if (ColorHansManager.getInstance().getHansBroadcastProxy() != null) {
                    ColorHansManager.getInstance().getHansBroadcastProxy().updatePendingIntentList(this.mBrdPendingList);
                }
                synchronized (this.mNetPacketWhiteList) {
                    updateArrayList(this.mNetPacketWhiteList, data.getStringArrayList(KEY_NET_PACKET_WHITE_LIST));
                }
                synchronized (this.mAbnormalKillWhiteList) {
                    updateArrayList(this.mAbnormalKillWhiteList, data.getStringArrayList(KEY_ABNORMAL_KILL_WHITE_LIST));
                }
                updateAppStateMap(data.getParcelableArrayList(KEY_OPPO_APP), data.getParcelableArrayList(KEY_OTHER_WHITE_APP), data.getParcelableArrayList(KEY_FREEZE_LIST), data.getParcelableArrayList(KEY_GMS_LIST));
            } else {
                updateSingleAppState(data);
            }
            return false;
        }

        private void updateAppStateMap(ArrayList<Bundle> oppoWhiteList, ArrayList<Bundle> thirdWhiteList, ArrayList<Bundle> freezeList, ArrayList<Bundle> gmsList) {
            SparseArray<HansPackage> oppoWhiteMap = new SparseArray<>();
            SparseArray<HansPackage> thirdWhiteMap = new SparseArray<>();
            SparseArray<HansPackage> freezeAppMap = new SparseArray<>();
            SparseArray<HansPackage> gmsAppMap = new SparseArray<>();
            if (oppoWhiteList != null) {
                Iterator<Bundle> it = oppoWhiteList.iterator();
                while (it.hasNext()) {
                    HansPackage hansPackage = getHansPackage(it.next());
                    if (hansPackage != null) {
                        oppoWhiteMap.put(hansPackage.getUid(), hansPackage);
                    }
                }
            }
            if (thirdWhiteList != null) {
                Iterator<Bundle> it2 = thirdWhiteList.iterator();
                while (it2.hasNext()) {
                    HansPackage hansPackage2 = getHansPackage(it2.next());
                    if (hansPackage2 != null) {
                        thirdWhiteMap.put(hansPackage2.getUid(), hansPackage2);
                    }
                }
            }
            if (freezeList != null) {
                Iterator<Bundle> it3 = freezeList.iterator();
                while (it3.hasNext()) {
                    HansPackage hansPackage3 = getHansPackage(it3.next());
                    if (hansPackage3 != null) {
                        freezeAppMap.put(hansPackage3.getUid(), hansPackage3);
                    }
                }
            }
            if (gmsList != null) {
                Iterator<Bundle> it4 = gmsList.iterator();
                while (it4.hasNext()) {
                    HansPackage hansPackage4 = getHansPackage(it4.next());
                    if (hansPackage4 != null) {
                        gmsAppMap.put(hansPackage4.getUid(), hansPackage4);
                    }
                }
            }
            ColorHansPackageSelector.this.updateHansPackageMap(oppoWhiteMap, thirdWhiteMap, freezeAppMap, gmsAppMap);
        }

        public void updateSingleAppState(Bundle data) {
            HansPackage hansPackage = getHansPackage(data);
            if (hansPackage != null) {
                hansPackage.setAppClass(data.getInt(KEY_APP_CLASS, 0));
                hansPackage.setSaveMode(data.getInt(KEY_MODE, 0));
                ColorHansPackageSelector.this.updateSingleHansPackage(hansPackage);
            }
        }

        private HansPackage getHansPackage(Bundle data) {
            if (data == null) {
                return null;
            }
            int uid = data.getInt("uid", 0);
            String pkgName = data.getString(KEY_PKG_NAME, "");
            int appType = data.getInt(KEY_APP_TYPE, -1);
            int freezeLevel = data.getInt(KEY_APP_FREEZELEVEL, 2);
            int allowCpnType = data.getInt(KEY_APP_ALLOW_CPNTYPE, 0);
            if (TextUtils.isEmpty(pkgName) || uid <= 10000) {
                return null;
            }
            HansPackage hansPackage = new HansPackage(pkgName, uid);
            hansPackage.setAppType(appType);
            hansPackage.setFreezeLevel(freezeLevel);
            hansPackage.setAllowCpnType(allowCpnType);
            return hansPackage;
        }

        private void updateArrayList(ArrayList<String> originList, ArrayList<String> newList) {
            if (originList != null && newList != null) {
                originList.clear();
                originList.addAll(newList);
            }
        }

        private void updateArrayMap(ArrayMap<String, ArrayList<String>> originMap, ArrayMap<String, ArrayList<String>> newMap) {
            if (originMap != null && newMap != null) {
                originMap.clear();
                originMap.putAll((ArrayMap<? extends String, ? extends ArrayList<String>>) newMap);
            }
        }

        private ArrayMap<String, ArrayList<String>> getConvertMap(ArrayList<String> list) {
            ArrayMap<String, ArrayList<String>> map = new ArrayMap<>();
            if (list == null || list.size() == 0) {
                return map;
            }
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String[] tmp = it.next().split("#");
                String key = tmp[0];
                ArrayList<String> value = new ArrayList<>();
                for (int i = 1; i < tmp.length; i++) {
                    value.add(tmp[i]);
                }
                map.put(key, value);
            }
            return map;
        }

        public void dumpList() {
            ColorHansManager.HansLogger hansLogger = ColorHansManager.getInstance().getHansLogger();
            hansLogger.i("mHansEnable= " + this.mHansEnable + " mGmsEnable= " + this.mGmsEnable + " mStatisticsSwitch= " + this.mStatisticsSwitch);
            ColorHansManager.HansLogger hansLogger2 = ColorHansManager.getInstance().getHansLogger();
            hansLogger2.i("mStateChangeDelayTime= " + this.mStateChangeDelayTime + " mImportantRepeatTime= " + this.mImportantRepeatTime + " mImportantRepeatTime= " + this.mImportantRepeatTime + " mLcdOffSceneInterval= " + this.mLcdOffSceneInterval + " mLcdOffSceneRepeatInterval= " + this.mLcdOffSceneRepeatInterval + " mNightRepeatTime= " + this.mNightSceneRepeatInterval);
            ColorHansManager.HansLogger hansLogger3 = ColorHansManager.getInstance().getHansLogger();
            StringBuilder sb = new StringBuilder();
            sb.append("oppoWhiteList=");
            sb.append(ColorHansPackageSelector.this.mOppoWhiteList.size());
            sb.append(" ");
            sb.append(ColorHansPackageSelector.this.mOppoWhiteList);
            hansLogger3.i(sb.toString());
            ColorHansManager.HansLogger hansLogger4 = ColorHansManager.getInstance().getHansLogger();
            hansLogger4.i("thirdWhiteList=" + ColorHansPackageSelector.this.mThirdWhiteList.size() + " " + ColorHansPackageSelector.this.mThirdWhiteList);
            ColorHansManager.HansLogger hansLogger5 = ColorHansManager.getInstance().getHansLogger();
            hansLogger5.i("thirdList=" + ColorHansPackageSelector.this.mThirdAppList.size() + " " + ColorHansPackageSelector.this.mThirdAppList);
            ColorHansManager.HansLogger hansLogger6 = ColorHansManager.getInstance().getHansLogger();
            hansLogger6.i("gmsList=" + ColorHansPackageSelector.this.mGMSList.size() + " " + ColorHansPackageSelector.this.mGMSList);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isHansFreezeEnable() {
        return this.mDBConfig.mHansEnable;
    }

    /* access modifiers changed from: package-private */
    public boolean isFreezeGmsEnable() {
        return this.mDBConfig.mGmsEnable;
    }

    /* access modifiers changed from: package-private */
    public boolean isFreezeStatisticsEnable() {
        return this.mDBConfig.mStatisticsSwitch;
    }

    /* access modifiers changed from: package-private */
    public long getImportantRepeatTime() {
        return this.mDBConfig.mImportantRepeatTime;
    }

    /* access modifiers changed from: package-private */
    public long getStateChangeDelayTime() {
        return this.mDBConfig.mStateChangeDelayTime;
    }

    /* access modifiers changed from: package-private */
    public long getFastFreezeTimeout() {
        return this.mDBConfig.mFastFreezeTimeout;
    }

    /* access modifiers changed from: package-private */
    public long getLcdOffSceneInterval() {
        return this.mDBConfig.mLcdOffSceneInterval;
    }

    /* access modifiers changed from: package-private */
    public long getLcdOffRepeatTime() {
        return this.mDBConfig.mLcdOffSceneRepeatInterval;
    }

    /* access modifiers changed from: package-private */
    public long getNightRepeatTime() {
        return this.mDBConfig.mNightSceneRepeatInterval;
    }

    /* access modifiers changed from: package-private */
    public boolean isInSyncWhiteList(String pkgName) {
        boolean contains;
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        synchronized (this.mDBConfig.mSyncWhiteList) {
            contains = this.mDBConfig.mSyncWhiteList.contains(pkgName);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public boolean isInJobWhiteList(String pkgName) {
        boolean contains;
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        synchronized (this.mDBConfig.mJobWhiteList) {
            contains = this.mDBConfig.mJobWhiteList.contains(pkgName);
        }
        return contains;
    }

    private boolean isInList(ArrayMap<String, ArrayList<String>> map, String key, String value) {
        List<String> values;
        if (map == null || TextUtils.isEmpty(key) || TextUtils.isEmpty(value) || (values = map.get(key)) == null || values.size() <= 0) {
            return false;
        }
        DBConfig dBConfig = this.mDBConfig;
        if (values.contains("all") || values.contains(value)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isInAlarmWhiteList(String pkgName, String action) {
        boolean isInList;
        synchronized (this.mDBConfig.mAlarmWhiteList) {
            isInList = isInList(this.mDBConfig.mAlarmWhiteList, pkgName, action);
        }
        return isInList;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0029, code lost:
        if (r8 != null) goto L_0x002c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002b, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002c, code lost:
        r1 = r6.mDBConfig.mAlarmkeyActionWhiteList;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0032, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r2 = r6.mDBConfig.mAlarmkeyActionWhiteList.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0041, code lost:
        if (r2.hasNext() == false) goto L_0x0052;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004d, code lost:
        if (r8.contains((java.lang.String) r2.next()) == false) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004f, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0050, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0052, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0053, code lost:
        return false;
     */
    public boolean isInAlarmKeyWhiteList(String pkgName, String action) {
        synchronized (this.mDBConfig.mAlarmKeyPkgWhiteList) {
            Iterator it = this.mDBConfig.mAlarmKeyPkgWhiteList.iterator();
            while (it.hasNext()) {
                if (pkgName.contains((String) it.next())) {
                    return true;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInActivityCpnWhiteList(String pkgName, String cpnName) {
        boolean isInList;
        synchronized (this.mDBConfig.mActivityCpnWhiteList) {
            isInList = isInList(this.mDBConfig.mActivityCpnWhiteList, pkgName, cpnName);
        }
        return isInList;
    }

    /* access modifiers changed from: package-private */
    public boolean isInActivityCpnBlackList(String pkgName, String cpnName) {
        boolean isInList;
        synchronized (this.mDBConfig.mActivityCpnBlackList) {
            isInList = isInList(this.mDBConfig.mActivityCpnBlackList, pkgName, cpnName);
        }
        return isInList;
    }

    /* access modifiers changed from: package-private */
    public boolean isInServiceCpnWhiteList(String pkgName, String cpnName) {
        boolean isInList;
        synchronized (this.mDBConfig.mServiceCpnWhiteList) {
            isInList = isInList(this.mDBConfig.mServiceCpnWhiteList, pkgName, cpnName);
        }
        return isInList;
    }

    /* access modifiers changed from: package-private */
    public boolean isInServiceCpnBlackList(String pkgName, String cpnName) {
        boolean isInList;
        synchronized (this.mDBConfig.mServiceCpnBlackList) {
            isInList = isInList(this.mDBConfig.mServiceCpnBlackList, pkgName, cpnName);
        }
        return isInList;
    }

    /* access modifiers changed from: package-private */
    public boolean isInProviderCpnWhiteList(String pkgName, String cpnName) {
        boolean isInList;
        synchronized (this.mDBConfig.mProviderCpnWhiteList) {
            isInList = isInList(this.mDBConfig.mProviderCpnWhiteList, pkgName, cpnName);
        }
        return isInList;
    }

    /* access modifiers changed from: package-private */
    public boolean isInProviderCpnBlackList(String pkgName, String cpnName) {
        boolean isInList;
        synchronized (this.mDBConfig.mProviderCpnBlackList) {
            isInList = isInList(this.mDBConfig.mProviderCpnBlackList, pkgName, cpnName);
        }
        return isInList;
    }

    /* access modifiers changed from: package-private */
    public boolean isInBroadcastCpnWhiteList(String pkgName, String action) {
        boolean isInList;
        synchronized (this.mDBConfig.mBroadcastCpnWhiteList) {
            isInList = isInList(this.mDBConfig.mBroadcastCpnWhiteList, action, pkgName);
        }
        return isInList;
    }

    /* access modifiers changed from: package-private */
    public boolean isInBroadcastCpnBlackList(String pkgName, String action) {
        boolean isInList;
        synchronized (this.mDBConfig.mBroadcastCpnBlackList) {
            isInList = isInList(this.mDBConfig.mBroadcastCpnBlackList, action, pkgName);
        }
        return isInList;
    }

    /* access modifiers changed from: package-private */
    public boolean isInAsyncBinderPkgWhiteList(String aidlName, String pkgName) {
        boolean isInList;
        synchronized (this.mDBConfig.mAsyncBinderPkgWhiteList) {
            isInList = isInList(this.mDBConfig.mAsyncBinderPkgWhiteList, aidlName, pkgName);
        }
        return isInList;
    }

    /* access modifiers changed from: package-private */
    public boolean isInAsyncBinderCodeWhiteList(String aidlName, String code) {
        boolean isInList;
        synchronized (this.mDBConfig.mAsyncBinderCodeWhiteList) {
            isInList = isInList(this.mDBConfig.mAsyncBinderCodeWhiteList, aidlName, code);
        }
        return isInList;
    }

    /* access modifiers changed from: package-private */
    public boolean isInActivityCallerWhiteList(String callerPkg) {
        boolean contains;
        if (TextUtils.isEmpty(callerPkg)) {
            return false;
        }
        synchronized (this.mDBConfig.mActivityCallerWhiteList) {
            contains = this.mDBConfig.mActivityCallerWhiteList.contains(callerPkg);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public boolean isInActivityCallerBlackList(String callerPkg) {
        boolean contains;
        if (TextUtils.isEmpty(callerPkg)) {
            return false;
        }
        synchronized (this.mDBConfig.mActivityCallerBlackList) {
            contains = this.mDBConfig.mActivityCallerBlackList.contains(callerPkg);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public boolean isInServiceCallerWhiteList(String callerPkg) {
        boolean contains;
        if (TextUtils.isEmpty(callerPkg)) {
            return false;
        }
        synchronized (this.mDBConfig.mServiceCallerWhiteList) {
            contains = this.mDBConfig.mServiceCallerWhiteList.contains(callerPkg);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public boolean isInServiceCallerBlackList(String callerPkg) {
        boolean contains;
        if (TextUtils.isEmpty(callerPkg)) {
            return false;
        }
        synchronized (this.mDBConfig.mServiceCallerBlackList) {
            contains = this.mDBConfig.mServiceCallerBlackList.contains(callerPkg);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public boolean isInProviderCallerWhiteList(String callerPkg) {
        boolean contains;
        if (TextUtils.isEmpty(callerPkg)) {
            return false;
        }
        synchronized (this.mDBConfig.mProviderCallerWhiteList) {
            contains = this.mDBConfig.mProviderCallerWhiteList.contains(callerPkg);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public boolean isInProviderCallerBlackList(String callerPkg) {
        boolean contains;
        if (TextUtils.isEmpty(callerPkg)) {
            return false;
        }
        synchronized (this.mDBConfig.mProviderCallerBlackList) {
            contains = this.mDBConfig.mProviderCallerBlackList.contains(callerPkg);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public boolean isInBroadcastCallerWhiteList(String callerPkg) {
        boolean contains;
        if (TextUtils.isEmpty(callerPkg)) {
            return false;
        }
        synchronized (this.mDBConfig.mBroadcastCallerWhiteList) {
            contains = this.mDBConfig.mBroadcastCallerWhiteList.contains(callerPkg);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public boolean isInBroadcastCallerBlackList(String callerPkg) {
        boolean contains;
        if (TextUtils.isEmpty(callerPkg)) {
            return false;
        }
        synchronized (this.mDBConfig.mBroadcastCallerBlackList) {
            contains = this.mDBConfig.mBroadcastCallerBlackList.contains(callerPkg);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public boolean isInNetPacketWhiteList(String packageName) {
        boolean contains;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        synchronized (this.mDBConfig.mNetPacketWhiteList) {
            contains = this.mDBConfig.mNetPacketWhiteList.contains(packageName);
        }
        return contains;
    }

    /* access modifiers changed from: package-private */
    public boolean isGmsPackage(int uid, String pkgName) {
        boolean z;
        synchronized (this.mHansLock) {
            z = this.mGMSList.get(uid) != null;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isAbnormalKillWhiteList(String packageName) {
        boolean contains;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        synchronized (this.mDBConfig.mAbnormalKillWhiteList) {
            contains = this.mDBConfig.mAbnormalKillWhiteList.contains(packageName);
        }
        return contains;
    }
}
