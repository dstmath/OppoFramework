package com.android.server.am;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.LongArrayQueue;
import android.util.SparseArray;
import com.android.server.am.ColorHansManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ColorHansPackageSelector {
    private static final int FILE_MODE_DEL = 2;
    private static final int FILE_MODE_SAVE = 1;
    private static final String KEY_ABNORMAL_CHECK_INTERVAL = "scrOffAbnormalCheckTime";
    private static final String KEY_ABNORMAL_KILL_WHITE_LIST = "abnormalKillWhiteList";
    private static final String KEY_ABNORMAL_NUM = "scrOffAbnormalCheckNum";
    private static final String KEY_ACTIVITY_CALLER_BLACK_LIST = "activityCallerBlackList";
    private static final String KEY_ACTIVITY_CALLER_WHITE_LIST = "activityCallerWhiteList";
    private static final String KEY_ACTIVITY_CPN_BLACK_LIST = "activityCpnBlackList";
    private static final String KEY_ACTIVITY_CPN_WHITE_LIST = "activityCpnWhiteList";
    private static final String KEY_ALARM_KEY_ACTION_WHITE_LIST = "alarmKeyActionWhiteList";
    private static final String KEY_ALARM_KEY_PKG_WHITE_LIST = "alarmKeyPkgWhiteList";
    private static final String KEY_ALARM_WHITE_LIST = "alarmWhiteList";
    private static final String KEY_ALL = "all";
    private static final String KEY_ALL_CONFIG = "allConfig";
    private static final String KEY_ASYNC_BINDER_CODE_WHITE_LIST = "asyncBinderCodeWhiteList";
    private static final String KEY_ASYNC_BINDER_PKG_WHITE_LIST = "asyncBinderPkgWhiteList";
    private static final String KEY_BROADCAST_CALLER_BLACK_LIST = "broadcastCallerBlackList";
    private static final String KEY_BROADCAST_CALLER_WHITE_LIST = "broadcastCallerWhiteList";
    private static final String KEY_BROADCAST_CPN_BLACK_LIST = "broadcastCpnBlackList";
    private static final String KEY_BROADCAST_CPN_WHITE_LIST = "broadcastCpnWhiteList";
    private static final String KEY_BROADCAST_PENDING_LIST = "broadcastPendingList";
    private static final String KEY_DOZE_SWITCH = "dozeSwitch";
    private static final String KEY_FIRST_CHECK_TIME = "firstCheckTime";
    private static final String KEY_FREEZE_LIST = "freezeList";
    private static final String KEY_GMS_LIST = "gmsList";
    private static final String KEY_GMS_SWITCH = "gmsSwitch";
    private static final String KEY_HANS_SWITCH = "hansSwitch";
    private static final String KEY_JOB_WHITE_LIST = "jobWhiteList";
    private static final String KEY_MODE = "mode";
    private static final String KEY_NET_PACKET_WHITE_LIST = "netPacketWhiteList";
    private static final String KEY_PKG_NAME = "pkgName";
    private static final String KEY_PROVIDER_CALLER_BLACK_LIST = "providerCallerBlackList";
    private static final String KEY_PROVIDER_CALLER_WHITE_LIST = "providerCallerWhiteList";
    private static final String KEY_PROVIDER_CPN_BLACK_LIST = "providerCpnBlackList";
    private static final String KEY_PROVIDER_CPN_WHITE_LIST = "providerCpnWhiteList";
    private static final String KEY_REPEAT_CHECK_TIME = "repeatCheckTime";
    private static final String KEY_SCREEN_OFF_PERIOD_CHECK_TIME = "screenOffPeriodCheckTime";
    private static final String KEY_SECOND_CHECK_TIME = "secondCheckTime";
    private static final String KEY_SERVICE_CALLER_BLACK_LIST = "serviceCallerBlackList";
    private static final String KEY_SERVICE_CALLER_WHITE_LIST = "serviceCallerWhiteList";
    private static final String KEY_SERVICE_CPN_BLACK_LIST = "serviceCpnBlackList";
    private static final String KEY_SERVICE_CPN_WHITE_LIST = "serviceCpnWhiteList";
    private static final String KEY_STATISTICS_SWITCH = "statisticsSwitch";
    private static final String KEY_SYNC_WHITE_LIST = "syncWhiteList";
    private static final String KEY_UID = "uid";
    private Context mContext = null;
    private DBConfig mDBConfig = null;
    private ColorHansManager mHansManager = null;
    /* access modifiers changed from: private */
    public PolicyConfig mPolicyConfig = null;

    public ColorHansPackageSelector(ColorHansManager hansManager, Context context) {
        this.mHansManager = hansManager;
        this.mContext = context;
        this.mDBConfig = new DBConfig();
        this.mPolicyConfig = new PolicyConfig();
    }

    public void init() {
    }

    public DBConfig getDBConfig() {
        return this.mDBConfig;
    }

    public PolicyConfig getPolicyConfig() {
        return this.mPolicyConfig;
    }

    /* access modifiers changed from: package-private */
    public final class PackageState {
        private ArrayList<Integer> frozenPidList;
        private int mCurState = -1;
        private long mEnterBgTime;
        private long mFreezeElapsedTime;
        private String mFreezeReason = "";
        private long mFreezeTime = 0;
        private int mFreezeType = 0;
        private boolean mFreezed = false;
        private boolean mHasForegroundService;
        private String mImportantReason = "";
        private int mJobWakelock;
        private long mLastUsedTime;
        private String mPkgName;
        private boolean mSharedUid = false;
        private int mSpecialType = -1;
        private int mUid;
        private LongArrayQueue mUnFreezeQueueTimes;
        private String mUnFreezeReason = "";
        private long mUnFreezeTime = 0;
        private int mVersionCode = -1;
        private String strUid;

        public PackageState(String pkgName, int uid) {
            this.mPkgName = pkgName;
            this.mUid = uid;
            this.strUid = String.valueOf(uid);
            this.mLastUsedTime = 0;
            this.mHasForegroundService = false;
            this.mEnterBgTime = 0;
            this.mFreezeElapsedTime = 0;
            this.frozenPidList = new ArrayList<>();
            this.mUnFreezeQueueTimes = new LongArrayQueue(5);
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

        public int getCurState() {
            return this.mCurState;
        }

        public void setCurState(int curState) {
            this.mCurState = curState;
        }

        public int getVersionCode() {
            return this.mVersionCode;
        }

        public void setVersionCode(int versionCode) {
            this.mVersionCode = versionCode;
        }

        public boolean isSharedUid() {
            return this.mSharedUid;
        }

        public void setSharedUid(boolean sharedUid) {
            this.mSharedUid = sharedUid;
        }

        public void setSpecialType(int type) {
            this.mSpecialType = type;
        }

        public int getSpecialType() {
            return this.mSpecialType;
        }

        public void setFreezed(boolean isFroze) {
            this.mFreezed = isFroze;
        }

        public boolean getFreezed() {
            return this.mFreezed;
        }

        public int getFreezeType() {
            return this.mFreezeType;
        }

        public void setFreezeType(int mfreezeType) {
            this.mFreezeType = mfreezeType;
        }

        public String getImportantReason() {
            return this.mImportantReason;
        }

        public void setImportantReason(String importantReason) {
            this.mImportantReason = importantReason;
        }

        public String getStrUid() {
            return this.strUid;
        }

        public void setStrUid() {
            this.strUid = this.strUid;
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
            return this.frozenPidList;
        }

        public boolean recordUnFreezeQueueTime(long time) {
            try {
                this.mUnFreezeQueueTimes.addLast(time);
                if (time - this.mUnFreezeQueueTimes.peekFirst() > ColorHansPackageSelector.this.mPolicyConfig.mAbnormalAppCheckIntervel) {
                    this.mUnFreezeQueueTimes.removeFirst();
                }
                if (this.mUnFreezeQueueTimes.size() >= ColorHansPackageSelector.this.mPolicyConfig.mAbnormalCheckNum) {
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

        public void setJobWakelock(int curState) {
            this.mJobWakelock = curState;
        }

        public int getJobWakelock() {
            return this.mJobWakelock;
        }

        public void setFrozenPidList(List<Integer> list) {
            ArrayList<Integer> arrayList = this.frozenPidList;
            if (arrayList != null) {
                arrayList.clear();
                this.frozenPidList.addAll(list);
            }
        }

        public String toString() {
            return "HansPackage{pkgName='" + this.mPkgName + ", uid=" + this.mUid + ", curState=" + this.mCurState + ", sharedUid=" + this.mSharedUid + ", freezed=" + this.mFreezed + ", freezedType=" + this.mFreezeType + ", importantReason=" + this.mImportantReason + ", strUid=" + this.strUid + '}';
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PackageState that = (PackageState) o;
            if (this.mUid != that.mUid || !this.mPkgName.equals(that.mPkgName)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.mPkgName, Integer.valueOf(this.mUid));
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

    private SparseArray<String> getConvertList(ArrayList<String> list) {
        SparseArray<String> List = new SparseArray<>();
        if (list == null || list.size() == 0) {
            return List;
        }
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String[] tmp = it.next().split("#");
            try {
                List.put(Integer.parseInt(tmp[0]), tmp[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return List;
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

    /* access modifiers changed from: package-private */
    public boolean updateHansConfig(Bundle data) {
        if (data == null) {
            return false;
        }
        boolean isAllConfig = data.getBoolean(KEY_ALL_CONFIG, true);
        if (this.mHansManager.getHansLogger() != null) {
            ColorHansManager.HansLogger hansLogger = this.mHansManager.getHansLogger();
            hansLogger.d("ColorHansPackageSelector -> updateHansConfig(), isAllConfig=" + isAllConfig);
        }
        if (isAllConfig) {
            this.mPolicyConfig.mHansFeature = data.getBoolean(KEY_HANS_SWITCH, true);
            this.mPolicyConfig.mDozeFeature = data.getBoolean(KEY_DOZE_SWITCH, false);
            this.mPolicyConfig.mGmsFeature = data.getBoolean(KEY_GMS_SWITCH, true);
            this.mPolicyConfig.mStatisticsSwitch = data.getBoolean(KEY_STATISTICS_SWITCH, false);
            this.mPolicyConfig.mScreenOffPeriodCheckTime = data.getInt(KEY_SCREEN_OFF_PERIOD_CHECK_TIME, 60000);
            this.mPolicyConfig.mFirstCheckTime = data.getInt(KEY_FIRST_CHECK_TIME, 15000);
            this.mPolicyConfig.mSecondCheckTime = data.getInt(KEY_SECOND_CHECK_TIME, 15000);
            this.mPolicyConfig.mRepeatTime = data.getInt(KEY_REPEAT_CHECK_TIME, 30000);
            this.mPolicyConfig.mAbnormalAppCheckIntervel = data.getLong(KEY_ABNORMAL_CHECK_INTERVAL, 300000);
            this.mPolicyConfig.mAbnormalCheckNum = data.getInt(KEY_ABNORMAL_NUM, 3);
            this.mHansManager.updatePolicyConfig();
            synchronized (this.mDBConfig.mSyncWhiteList) {
                updateArrayList(this.mDBConfig.mSyncWhiteList, data.getStringArrayList(KEY_SYNC_WHITE_LIST));
            }
            synchronized (this.mDBConfig.mJobWhiteList) {
                updateArrayList(this.mDBConfig.mJobWhiteList, data.getStringArrayList(KEY_JOB_WHITE_LIST));
            }
            synchronized (this.mDBConfig.mAlarmWhiteList) {
                updateArrayMap(this.mDBConfig.mAlarmWhiteList, getConvertMap(data.getStringArrayList(KEY_ALARM_WHITE_LIST)));
            }
            synchronized (this.mDBConfig.mAlarmKeyPkgWhiteList) {
                updateArrayList(this.mDBConfig.mAlarmKeyPkgWhiteList, data.getStringArrayList(KEY_ALARM_KEY_PKG_WHITE_LIST));
            }
            synchronized (this.mDBConfig.mAlarmkeyActionWhiteList) {
                updateArrayList(this.mDBConfig.mAlarmkeyActionWhiteList, data.getStringArrayList(KEY_ALARM_KEY_ACTION_WHITE_LIST));
            }
            synchronized (this.mDBConfig.mActivityCpnWhiteList) {
                updateArrayMap(this.mDBConfig.mActivityCpnWhiteList, getConvertMap(data.getStringArrayList(KEY_ACTIVITY_CPN_WHITE_LIST)));
            }
            synchronized (this.mDBConfig.mActivityCpnBlackList) {
                updateArrayMap(this.mDBConfig.mActivityCpnBlackList, getConvertMap(data.getStringArrayList(KEY_ACTIVITY_CPN_BLACK_LIST)));
            }
            synchronized (this.mDBConfig.mServiceCpnWhiteList) {
                updateArrayMap(this.mDBConfig.mServiceCpnWhiteList, getConvertMap(data.getStringArrayList(KEY_SERVICE_CPN_WHITE_LIST)));
            }
            synchronized (this.mDBConfig.mServiceCpnBlackList) {
                updateArrayMap(this.mDBConfig.mServiceCpnBlackList, getConvertMap(data.getStringArrayList(KEY_SERVICE_CPN_BLACK_LIST)));
            }
            synchronized (this.mDBConfig.mProviderCpnWhiteList) {
                updateArrayMap(this.mDBConfig.mProviderCpnWhiteList, getConvertMap(data.getStringArrayList(KEY_PROVIDER_CPN_WHITE_LIST)));
            }
            synchronized (this.mDBConfig.mProviderCpnBlackList) {
                updateArrayMap(this.mDBConfig.mProviderCpnBlackList, getConvertMap(data.getStringArrayList(KEY_PROVIDER_CPN_BLACK_LIST)));
            }
            synchronized (this.mDBConfig.mBroadcastCpnWhiteList) {
                updateArrayMap(this.mDBConfig.mBroadcastCpnWhiteList, getConvertMap(data.getStringArrayList(KEY_BROADCAST_CPN_WHITE_LIST)));
            }
            synchronized (this.mDBConfig.mBroadcastCpnBlackList) {
                updateArrayMap(this.mDBConfig.mBroadcastCpnBlackList, getConvertMap(data.getStringArrayList(KEY_BROADCAST_CPN_BLACK_LIST)));
            }
            synchronized (this.mDBConfig.mAsyncBinderPkgWhiteList) {
                updateArrayMap(this.mDBConfig.mAsyncBinderPkgWhiteList, getConvertMap(data.getStringArrayList(KEY_ASYNC_BINDER_PKG_WHITE_LIST)));
            }
            synchronized (this.mDBConfig.mAsyncBinderCodeWhiteList) {
                updateArrayMap(this.mDBConfig.mAsyncBinderCodeWhiteList, getConvertMap(data.getStringArrayList(KEY_ASYNC_BINDER_CODE_WHITE_LIST)));
            }
            synchronized (this.mDBConfig.mActivityCallerWhiteList) {
                updateArrayList(this.mDBConfig.mActivityCallerWhiteList, data.getStringArrayList(KEY_ACTIVITY_CALLER_WHITE_LIST));
            }
            synchronized (this.mDBConfig.mActivityCallerBlackList) {
                updateArrayList(this.mDBConfig.mActivityCallerBlackList, data.getStringArrayList(KEY_ACTIVITY_CALLER_BLACK_LIST));
            }
            synchronized (this.mDBConfig.mServiceCallerWhiteList) {
                updateArrayList(this.mDBConfig.mServiceCallerWhiteList, data.getStringArrayList(KEY_SERVICE_CALLER_WHITE_LIST));
            }
            synchronized (this.mDBConfig.mServiceCallerBlackList) {
                updateArrayList(this.mDBConfig.mServiceCallerBlackList, data.getStringArrayList(KEY_SERVICE_CALLER_BLACK_LIST));
            }
            synchronized (this.mDBConfig.mProviderCallerWhiteList) {
                updateArrayList(this.mDBConfig.mProviderCallerWhiteList, data.getStringArrayList(KEY_PROVIDER_CALLER_WHITE_LIST));
            }
            synchronized (this.mDBConfig.mProviderCallerBlackList) {
                updateArrayList(this.mDBConfig.mProviderCallerBlackList, data.getStringArrayList(KEY_PROVIDER_CALLER_BLACK_LIST));
            }
            synchronized (this.mDBConfig.mBroadcastCallerWhiteList) {
                updateArrayList(this.mDBConfig.mBroadcastCallerWhiteList, data.getStringArrayList(KEY_BROADCAST_CALLER_WHITE_LIST));
            }
            synchronized (this.mDBConfig.mBroadcastCallerBlackList) {
                updateArrayList(this.mDBConfig.mBroadcastCallerBlackList, data.getStringArrayList(KEY_BROADCAST_CALLER_BLACK_LIST));
            }
            synchronized (this.mDBConfig.mBrdPendingList) {
                updateArrayList(this.mDBConfig.mBrdPendingList, data.getStringArrayList(KEY_BROADCAST_PENDING_LIST));
            }
            this.mHansManager.updatePendingIntentList(this.mDBConfig.mBrdPendingList);
            synchronized (this.mDBConfig.mNetPacketWhiteList) {
                updateArrayList(this.mDBConfig.mNetPacketWhiteList, data.getStringArrayList(KEY_NET_PACKET_WHITE_LIST));
            }
            synchronized (this.mDBConfig.mAbnormalKillWhiteList) {
                updateArrayList(this.mDBConfig.mAbnormalKillWhiteList, data.getStringArrayList(KEY_ABNORMAL_KILL_WHITE_LIST));
            }
            updateAppStateMap(getConvertList(data.getStringArrayList(KEY_FREEZE_LIST)));
            updateGmsList(getConvertList(data.getStringArrayList(KEY_GMS_LIST)));
        } else {
            String pkgName = data.getString(KEY_PKG_NAME, "");
            int mode = data.getInt(KEY_MODE, -1);
            int uid = data.getInt(KEY_UID, -1);
            if (this.mHansManager.getHansLogger() != null) {
                ColorHansManager.HansLogger hansLogger2 = this.mHansManager.getHansLogger();
                hansLogger2.d("ColorHansPackageSelector -> single update freeze list, pkgName=" + pkgName + ",uid=" + uid + ",mode=" + mode);
            }
            if (!TextUtils.isEmpty(pkgName)) {
                if (mode == 1) {
                    synchronized (this.mDBConfig.mFreezeList) {
                        this.mDBConfig.mFreezeList.put(uid, pkgName);
                    }
                    this.mHansManager.addPackageState(uid, new PackageState(pkgName, uid));
                } else if (mode == 2) {
                    synchronized (this.mDBConfig.mFreezeList) {
                        this.mDBConfig.mFreezeList.remove(uid);
                    }
                    this.mHansManager.removePackageState(uid);
                }
            }
        }
        return true;
    }

    private void updateAppStateMap(SparseArray<String> newFreezeList) {
        synchronized (this.mDBConfig.mFreezeList) {
            for (int i = 0; i < this.mDBConfig.mFreezeList.size(); i++) {
                int uid = this.mDBConfig.mFreezeList.keyAt(i);
                if (newFreezeList.get(uid) == null) {
                    this.mHansManager.removePackageState(uid);
                }
            }
            for (int j = 0; j < newFreezeList.size(); j++) {
                int uid2 = newFreezeList.keyAt(j);
                String pkg = newFreezeList.valueAt(j);
                if (this.mDBConfig.mFreezeList.get(uid2) == null) {
                    this.mHansManager.addPackageState(uid2, new PackageState(pkg, uid2));
                }
            }
            this.mDBConfig.mFreezeList.clear();
            for (int k = 0; k < newFreezeList.size(); k++) {
                this.mDBConfig.mFreezeList.put(newFreezeList.keyAt(k), newFreezeList.valueAt(k));
            }
        }
    }

    private void updateGmsList(SparseArray<String> newGmsList) {
        if (this.mHansManager.getCommonConfig() != null) {
            synchronized (this.mDBConfig.mGmsList) {
                if (this.mHansManager.getCommonConfig().isRestrictGms()) {
                    for (int i = 0; i < this.mDBConfig.mGmsList.size(); i++) {
                        int uid = this.mDBConfig.mGmsList.keyAt(i);
                        if (newGmsList.get(uid) == null) {
                            this.mHansManager.removeGmsPkgState(uid);
                        }
                    }
                    for (int j = 0; j < newGmsList.size(); j++) {
                        int uid2 = newGmsList.keyAt(j);
                        String pkg = newGmsList.valueAt(j);
                        if (this.mDBConfig.mGmsList.get(uid2) == null) {
                            this.mHansManager.addGmsPkgState(uid2, new PackageState(pkg, uid2));
                        }
                    }
                }
                this.mDBConfig.mGmsList.clear();
                for (int k = 0; k < newGmsList.size(); k++) {
                    this.mDBConfig.mGmsList.put(newGmsList.keyAt(k), newGmsList.valueAt(k));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class PolicyConfig {
        long mAbnormalAppCheckIntervel = 300000;
        int mAbnormalCheckNum = 3;
        boolean mDozeFeature = false;
        int mFirstCheckTime = 15000;
        boolean mGmsFeature = true;
        boolean mHansFeature = true;
        int mRepeatTime = 30000;
        int mScreenOffPeriodCheckTime = 60000;
        int mSecondCheckTime = 15000;
        boolean mStatisticsSwitch = false;

        PolicyConfig() {
        }
    }

    class DBConfig {
        ArrayList<String> mAbnormalKillWhiteList = new ArrayList<>();
        ArrayList<String> mActivityCallerBlackList = new ArrayList<>();
        ArrayList<String> mActivityCallerWhiteList = new ArrayList<>();
        ArrayMap<String, ArrayList<String>> mActivityCpnBlackList = new ArrayMap<>();
        ArrayMap<String, ArrayList<String>> mActivityCpnWhiteList = new ArrayMap<>();
        ArrayList<String> mAlarmKeyPkgWhiteList = new ArrayList<>();
        ArrayMap<String, ArrayList<String>> mAlarmWhiteList = new ArrayMap<>();
        ArrayList<String> mAlarmkeyActionWhiteList = new ArrayList<>();
        ArrayMap<String, ArrayList<String>> mAsyncBinderCodeWhiteList = new ArrayMap<>();
        ArrayMap<String, ArrayList<String>> mAsyncBinderPkgWhiteList = new ArrayMap<>();
        ArrayList<String> mBrdPendingList = new ArrayList<>();
        ArrayList<String> mBroadcastCallerBlackList = new ArrayList<>();
        ArrayList<String> mBroadcastCallerWhiteList = new ArrayList<>();
        ArrayMap<String, ArrayList<String>> mBroadcastCpnBlackList = new ArrayMap<>();
        ArrayMap<String, ArrayList<String>> mBroadcastCpnWhiteList = new ArrayMap<>();
        SparseArray<String> mFreezeList = new SparseArray<>();
        SparseArray<String> mGmsList = new SparseArray<>();
        ArrayList<String> mJobWhiteList = new ArrayList<>();
        ArrayList<String> mNetPacketWhiteList = new ArrayList<>();
        ArrayList<String> mProviderCallerBlackList = new ArrayList<>();
        ArrayList<String> mProviderCallerWhiteList = new ArrayList<>();
        ArrayMap<String, ArrayList<String>> mProviderCpnBlackList = new ArrayMap<>();
        ArrayMap<String, ArrayList<String>> mProviderCpnWhiteList = new ArrayMap<>();
        ArrayList<String> mServiceCallerBlackList = new ArrayList<>();
        ArrayList<String> mServiceCallerWhiteList = new ArrayList<>();
        ArrayMap<String, ArrayList<String>> mServiceCpnBlackList = new ArrayMap<>();
        ArrayMap<String, ArrayList<String>> mServiceCpnWhiteList = new ArrayMap<>();
        ArrayList<String> mSyncWhiteList = new ArrayList<>();

        DBConfig() {
        }
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
        if (values.contains(KEY_ALL) || values.contains(value)) {
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
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        r1 = r5.mDBConfig.mAlarmkeyActionWhiteList;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0028, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r0 = r5.mDBConfig.mAlarmkeyActionWhiteList.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0035, code lost:
        if (r0.hasNext() == false) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0041, code lost:
        if (r7.contains(r0.next()) == false) goto L_0x0045;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0043, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0044, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0046, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0047, code lost:
        return false;
     */
    public boolean isInAlarmKeyWhiteList(String pkgName, String action) {
        synchronized (this.mDBConfig.mAlarmKeyPkgWhiteList) {
            Iterator<String> it = this.mDBConfig.mAlarmKeyPkgWhiteList.iterator();
            while (it.hasNext()) {
                if (pkgName.contains(it.next())) {
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
    public SparseArray<String> getGmsList() {
        SparseArray<String> list = new SparseArray<>();
        synchronized (this.mDBConfig.mGmsList) {
            for (int i = 0; i < this.mDBConfig.mGmsList.size(); i++) {
                list.put(this.mDBConfig.mGmsList.keyAt(i), this.mDBConfig.mGmsList.valueAt(i));
            }
        }
        return list;
    }

    /* access modifiers changed from: package-private */
    public boolean isGmsPackage(int uid, String pkgName) {
        boolean z;
        synchronized (this.mDBConfig.mGmsList) {
            z = this.mDBConfig.mGmsList.get(uid) != null;
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
