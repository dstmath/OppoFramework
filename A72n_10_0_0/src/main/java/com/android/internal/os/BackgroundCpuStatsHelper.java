package com.android.internal.os;

import android.content.ComponentName;
import android.os.BatteryStats;
import android.os.Handler;
import android.os.OppoBaseBatteryStats;
import android.os.Parcel;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.os.BatteryStatsImpl;
import com.color.util.ColorTypeCastingHelper;
import java.util.Set;

public class BackgroundCpuStatsHelper {
    private static boolean DEBUG_DETAIL = false;
    private static final String TAG = "BackgroundCpuStatsHelper";
    private static final long TIME_CHECK_ACTIVITY_RESUME_DELAY = 5000;
    private ArrayMap<String, ActivityPauseRunnable> mActivityPauseRunnables = new ArrayMap<>();
    private BatteryChangeRunnable mBatteryChangeRunnable = new BatteryChangeRunnable();
    private BatteryStatsImpl mBsi;
    public ProcessCpuTracker mCpuTracker = new ProcessCpuTracker(false);
    private volatile String mLastTopPackageName = null;
    private volatile BatteryStatsImpl.Uid mLastTopUidObj;
    public volatile String mTopPackageName = null;
    private volatile BatteryStatsImpl.Uid mTopUidObj;

    private class ActivityPauseRunnable implements Runnable {
        String pkg;
        int uid;

        private ActivityPauseRunnable() {
            this.uid = -1;
            this.pkg = null;
        }

        public void setArgs(int uid2, String pkg2) {
            this.uid = uid2;
            this.pkg = pkg2;
        }

        public void run() {
            if (BackgroundCpuStatsHelper.this.mBsi != null) {
                synchronized (BackgroundCpuStatsHelper.this.mBsi) {
                    BatteryStatsImpl.Uid uidObj = BackgroundCpuStatsHelper.this.mBsi.getUidStatsLocked(this.uid);
                    for (int i = 0; i < uidObj.mProcessStats.size(); i++) {
                        OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc basePs = (OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc.class, uidObj.mProcessStats.valueAt(i));
                        if (basePs != null) {
                            basePs.getPackageList();
                            if (basePs.getPackageList().contains(this.pkg) && basePs.getCurrentPid() > 0) {
                                long cpuTime = BackgroundCpuStatsHelper.this.mCpuTracker.getCpuTimeForPid(basePs.getCurrentPid());
                                if (BackgroundCpuStatsHelper.DEBUG_DETAIL) {
                                    Slog.d(BackgroundCpuStatsHelper.TAG, "activity pause set bg cpu " + cpuTime + " uid = " + this.uid + ", pkg = " + this.pkg);
                                }
                                basePs.setCpuTimeWhenIntoBackground(cpuTime);
                            }
                        } else {
                            return;
                        }
                    }
                    if (!BackgroundCpuStatsHelper.this.mBsi.isScreenOn(BackgroundCpuStatsHelper.this.mBsi.mScreenState) || (BackgroundCpuStatsHelper.this.mTopPackageName != null && this.pkg.equals(BackgroundCpuStatsHelper.this.mTopPackageName))) {
                        BackgroundCpuStatsHelper.this.mTopPackageName = null;
                        BackgroundCpuStatsHelper.this.mTopUidObj = null;
                    }
                }
            }
        }
    }

    private class BatteryChangeRunnable implements Runnable {
        SparseArray<BatteryStatsImpl.Uid> mUidStats;
        boolean onBatteryNow;

        private BatteryChangeRunnable() {
            this.onBatteryNow = false;
            this.mUidStats = null;
        }

        public void setUidStats(SparseArray<BatteryStatsImpl.Uid> uidStats) {
            this.mUidStats = uidStats;
        }

        public void setOnBatteryNow(boolean onBatteryNow2) {
            this.onBatteryNow = onBatteryNow2;
        }

        public void run() {
            BackgroundCpuStatsHelper.this.updateBackgroundCpuStats(this.mUidStats, this.onBatteryNow);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBackgroundCpuStats(SparseArray<BatteryStatsImpl.Uid> uidStats, boolean onBatteryNow) {
        if (this.mBsi != null) {
            long start = SystemClock.uptimeMillis();
            synchronized (this.mBsi) {
                for (int i = 0; i < uidStats.size(); i++) {
                    ArrayMap<String, ? extends BatteryStats.Uid.Proc> processs = uidStats.valueAt(i).getProcessStats();
                    int j = 0;
                    while (true) {
                        if (j >= processs.size()) {
                            break;
                        }
                        OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc basePs = (OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc.class, (BatteryStats.Uid.Proc) processs.valueAt(j));
                        if (basePs == null) {
                            break;
                        }
                        basePs.updateWhenBatteryChange(onBatteryNow);
                        j++;
                    }
                }
            }
            if (DEBUG_DETAIL) {
                Slog.d(TAG, "updateBackgroundCpuStats waste time = " + (SystemClock.uptimeMillis() - start));
            }
        }
    }

    public void setBatteryStats(BatteryStatsImpl bsi) {
        this.mBsi = bsi;
    }

    public void setDebugSwitch(boolean enable) {
        DEBUG_DETAIL = enable;
    }

    public void writeToParcelLocked(Parcel out, long bgCpuTime, Set<String> pkgList) {
        out.writeLong(bgCpuTime);
        out.writeInt(pkgList.size());
        for (String str : pkgList) {
            out.writeString(String.valueOf(str));
        }
    }

    public long readFromParcelLocked(Parcel in, Set<String> pkgList) {
        long bgCpuTime = in.readLong();
        int NP = in.readInt();
        for (int i = 0; i < NP; i++) {
            pkgList.add(in.readString());
        }
        return bgCpuTime;
    }

    public void updateBackgroundCpuTimeWhenOnBatteryChange(SparseArray<BatteryStatsImpl.Uid> uidStats, boolean onBattery) {
        BackgroundThread.getHandler().removeCallbacks(this.mBatteryChangeRunnable);
        this.mBatteryChangeRunnable.setOnBatteryNow(onBattery);
        this.mBatteryChangeRunnable.setUidStats(uidStats);
        BackgroundThread.getHandler().postDelayed(this.mBatteryChangeRunnable, 5000);
    }

    public void recordActivityInfoWhenResume(int uid, ComponentName component) {
        if (this.mBsi != null) {
            if (DEBUG_DETAIL) {
                StringBuilder sb = new StringBuilder();
                sb.append("recordActivityInfoWhenResume package = ");
                sb.append(component != null ? component.getPackageName() : " null ");
                sb.append(", isScreenOn : ");
                BatteryStatsImpl batteryStatsImpl = this.mBsi;
                sb.append(batteryStatsImpl.isScreenOn(batteryStatsImpl.mScreenState));
                Slog.d(TAG, sb.toString());
            }
            this.mLastTopPackageName = this.mTopPackageName;
            this.mLastTopUidObj = this.mTopUidObj;
            this.mTopUidObj = null;
            this.mTopPackageName = null;
            if (component != null) {
                BatteryStatsImpl.Uid uidObj = this.mBsi.getUidStatsLocked(uid);
                BatteryStatsImpl batteryStatsImpl2 = this.mBsi;
                if (batteryStatsImpl2.isScreenOn(batteryStatsImpl2.mScreenState)) {
                    this.mTopUidObj = uidObj;
                    this.mTopPackageName = component.getPackageName();
                }
            }
        }
    }

    public void updateBackgroundCpuTimeWhenActivityResume(int uid, ComponentName component, Handler handler, boolean isOnBattery) {
        OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc basePs;
        OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc basePs2;
        if (this.mBsi != null) {
            for (int i = 0; i < this.mActivityPauseRunnables.size(); i++) {
                ActivityPauseRunnable runnable = this.mActivityPauseRunnables.valueAt(i);
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
            }
            if (!isOnBattery) {
                return;
            }
            if (!(this.mTopPackageName == null && this.mLastTopPackageName == null) && !TextUtils.equals(this.mTopPackageName, this.mLastTopPackageName)) {
                if (this.mLastTopUidObj != null) {
                    int i2 = 0;
                    while (i2 < this.mLastTopUidObj.mProcessStats.size() && (basePs2 = (OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc.class, this.mLastTopUidObj.mProcessStats.valueAt(i2))) != null) {
                        basePs2.getPackageList();
                        if (this.mLastTopPackageName != null && basePs2.getPackageList().contains(this.mLastTopPackageName) && basePs2.getCurrentPid() > 0) {
                            long cpuTime = this.mCpuTracker.getCpuTimeForPid(basePs2.getCurrentPid());
                            if (DEBUG_DETAIL) {
                                Slog.d(TAG, "noteActivityResumedLocked  last package " + this.mLastTopPackageName + ", pid = " + basePs2.getCurrentPid() + " setCpuTimeWhenIntoBackground  = " + cpuTime);
                            }
                            basePs2.setCpuTimeWhenIntoBackground(cpuTime);
                        }
                        i2++;
                    }
                }
                if (this.mTopUidObj != null) {
                    this.mTopUidObj.getProcessStats();
                    int i3 = 0;
                    while (i3 < this.mTopUidObj.mProcessStats.size() && (basePs = (OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc.class, this.mTopUidObj.mProcessStats.valueAt(i3))) != null) {
                        if (this.mTopPackageName != null && basePs.getPackageList().contains(this.mTopPackageName) && basePs.getCurrentPid() > 0) {
                            long cpuTime2 = this.mCpuTracker.getCpuTimeForPid(basePs.getCurrentPid());
                            long cpuTimeWhenIntoBackground = basePs.getCpuTimeWhenIntoBackground();
                            if (cpuTimeWhenIntoBackground >= 0 && cpuTime2 > cpuTimeWhenIntoBackground) {
                                if (DEBUG_DETAIL) {
                                    Slog.d(TAG, "noteActivityResumedLocked top package " + this.mTopPackageName + ", pid = " + basePs.getCurrentPid() + " cpuTime  = " + cpuTime2 + ", cpuTimeWhenIntoBackground = " + cpuTimeWhenIntoBackground);
                                }
                                basePs.addBackgroundCpuTime(cpuTime2 - cpuTimeWhenIntoBackground);
                            }
                        }
                        basePs.setCpuTimeWhenIntoBackground(Long.MAX_VALUE);
                        i3++;
                    }
                }
            }
        }
    }

    public void updateBackgroundCpuTimeWhenActivityPause(int uid, ComponentName component, Handler handler, boolean isOnBattery) {
        if (this.mBsi != null && isOnBattery) {
            String pkg = component != null ? component.getPackageName() : null;
            if (pkg != null) {
                ActivityPauseRunnable runnable = this.mActivityPauseRunnables.get(pkg);
                if (runnable == null) {
                    runnable = new ActivityPauseRunnable();
                    runnable.setArgs(uid, pkg);
                    this.mActivityPauseRunnables.put(pkg, runnable);
                } else {
                    handler.removeCallbacks(runnable);
                }
                handler.postDelayed(runnable, 5000);
            }
        }
    }

    public void updateBackgroundCpuTimeWhenProcStart(BatteryStatsImpl.Uid.Proc proc) {
        OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc basePs = (OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc.class, proc);
        if (basePs != null) {
            Set<String> procPkgList = basePs.getPackageList();
            if ((this.mTopPackageName == null || !procPkgList.contains(this.mTopPackageName)) && basePs.getCpuTimeWhenIntoBackground() != 0 && basePs.getCurrentPid() > 0) {
                basePs.setCpuTimeWhenIntoBackground(0);
            }
        }
    }

    public void updateBackgroundCpuTimeWhenProcDied(BatteryStatsImpl.Uid u, int pid, String procName, boolean isOnBattery) {
        OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc basePs;
        int i = 0;
        while (i < u.mProcessStats.size() && (basePs = (OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.OppoBaseProc.class, u.mProcessStats.valueAt(i))) != null) {
            if ((pid == 0 || basePs.getCurrentPid() != pid) && (pid != 0 || basePs.getCurrentPid() <= 0 || basePs.getName() == null || !basePs.getName().equals(procName))) {
                i++;
            } else {
                if (isOnBattery && (this.mTopPackageName == null || !basePs.getPackageList().contains(this.mTopPackageName))) {
                    long currentCpuTime = this.mCpuTracker.getCpuTimeForPid(basePs.getCurrentPid());
                    long backCpuTime = basePs.getCpuTimeWhenIntoBackground();
                    if (DEBUG_DETAIL) {
                        Slog.d(TAG, "updateBackgroundCpuTimeWhenProcDied process " + basePs.getCurrentPid() + " ,currentCpuTime = " + currentCpuTime + ", cpuWhenBackground = " + backCpuTime);
                    }
                    if (backCpuTime == Long.MAX_VALUE) {
                        basePs.addBackgroundCpuTime(currentCpuTime);
                    } else if (backCpuTime >= 0 && currentCpuTime > backCpuTime) {
                        basePs.addBackgroundCpuTime(currentCpuTime - backCpuTime);
                    }
                }
                basePs.setCpuTimeWhenIntoBackground(Long.MAX_VALUE);
                basePs.setCurrentPid(0);
                return;
            }
        }
    }
}
