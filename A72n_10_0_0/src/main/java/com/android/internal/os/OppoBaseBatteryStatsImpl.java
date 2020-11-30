package com.android.internal.os;

import android.app.AlarmManager;
import android.app.job.JobInfo;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.BatteryStats;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.OppoBaseBatteryStats;
import android.os.OppoThermalState;
import android.os.Parcel;
import android.os.SystemClock;
import android.telecom.ParcelableCallAnalytics;
import android.telephony.SignalStrength;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.os.BatteryStatsImpl;
import com.color.util.ColorTypeCastingHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

public abstract class OppoBaseBatteryStatsImpl extends BatteryStats {
    protected static int ACTIVITY_MONITOR_MIN_TIME = 20;
    protected static final String BATTERY_REALTIME_CAPACITY = "/sys/class/power_supply/battery/batt_rm";
    private static final long BYTES_PER_GB = 1073741824;
    private static final long BYTES_PER_KB = 1024;
    private static final long BYTES_PER_MB = 1048576;
    private static final boolean DEBUG = true;
    protected static boolean DEBUG_DETAIL = false;
    protected static boolean DEBUG_UID_SCREEN_BASIC = false;
    protected static boolean DEBUG_UID_SCREEN_DETAIL = false;
    static final long DELAY_SYSTEM_READY = 5000;
    private static final String DUMP_HISTORY_INTENT_O = "oppo.intent.action.ACTION_OPPO_POWER_CHECKIN_SAVED";
    protected static long HIGH_POWER_THRESHOLD = 800;
    private static final int MAX_NUM_UPLOAD = 3;
    protected static final long MILLISECONDS_IN_YEAR = 31536000000L;
    private static final int MIN_APP_NET_WAKEUP_CYCLE = 180000;
    private static final int MIN_RECORD_SENSOR_HELD_TIME = 30000;
    private static final long MIN_RECORD_TRAFFIC_BYTES = 1048576;
    private static final int MIN_RECORD_WAKELOCK_HELD_TIME = 30000;
    private static final int MIN_SCREEN_OFF_DURATION = 1800000;
    private static final int MIN_SENSOR_HELD_TIME = 720000;
    private static final int MIN_SIGNAL_INTERVAL = 300000;
    private static final long MIN_TRAFFIC_BYTES = 12582912;
    private static final int MIN_WAKELOCK_HELD_TIME = 720000;
    static final int MSG_CHECK_AVERAGE_CURRENT = 256;
    static final int MSG_SYSTEM_READY = 128;
    static final long ONE_HOUR_IN_MS = 3600000;
    private static final String TAG = "OppoBaseBatteryStatsImpl";
    protected static final String UPLOAD_ACTIVITY_BATTERY_RECORD = "activity_battery_record";
    protected static final String UPLOAD_BATTERYSTATS_RESET = "batterystats_reset";
    protected static final String UPLOAD_LOGTAG = "20089";
    boolean DEBUG_SUPPER_APP;
    final Comparator<StatisticsEntry> StatisticsComparator;
    protected Context mContext;
    private final StringBuilder mFormatBuilder;
    private final Formatter mFormatter;
    private List<Sensor> mListAllSensor;
    private Handler mLocalHandler;
    private OppoThermalStatsHelper mOppoThermalStatsHelper;
    public ScreenoffBatteryStats mScreenoffBatteryStats;
    private File mThermalRecFile;
    BatteryStatsImpl.Uid mUidTopActivity;
    int pausedBatteryLevel;
    long pausedElapsedRealtime;
    int resumedBatteryLevel;
    int resumedBatteryRealtimeCapacity;
    String resumedClass;
    long resumedElapsedRealtime;
    String resumedPackage;
    int resumedUid;

    public interface Clocks {
        long elapsedRealtime();

        long uptimeMillis();
    }

    public static abstract class Uid extends BatteryStats.Uid {
        BatteryStatsImpl.StopwatchTimer mBgCameraTurnedOnTimer;
        BatteryStatsImpl.LongSamplingCounter mBgCpuActiveTimeMs;
        BatteryStatsImpl.LongSamplingCounter[][] mBgCpuClusterSpeedTimesUs;
        BatteryStatsImpl.LongSamplingCounterArray mBgCpuClusterTimesMs;
        BatteryStatsImpl.Uid.Pkg mForegroundPkg;
        BatteryStatsImpl.StopwatchTimer[] mUidScreenBrightnessTimer;
    }

    /* access modifiers changed from: protected */
    public abstract AtomicFile getBatteryCheckinFile();

    /* access modifiers changed from: protected */
    public abstract long getClockElapsedRealtime();

    public abstract int getHistoryBufferSize();

    /* access modifiers changed from: protected */
    public abstract int getScreenState();

    public abstract int getWifiSignalStrengthBin();

    /* access modifiers changed from: protected */
    public abstract void onBatterySendBroadcast(Intent intent);

    /* access modifiers changed from: protected */
    public abstract void onNoteActivityResumed(int i);

    /* access modifiers changed from: protected */
    public abstract void onSchedulerUpdateCpu(long j);

    /* access modifiers changed from: protected */
    public abstract void onWriteSummaryToParcel(Parcel parcel, boolean z);

    /* access modifiers changed from: package-private */
    public class StatisticsEntry {
        final String mPkgName;
        final long mTime;
        final int mUid;

        StatisticsEntry(String pkgName, int uid, long time) {
            this.mPkgName = pkgName;
            this.mUid = uid;
            this.mTime = time;
        }
    }

    /* access modifiers changed from: package-private */
    public class WakeLockEntry extends StatisticsEntry {
        final int mCount;
        final String mTagName;

        WakeLockEntry(String tagName, String pkgName, int uid, long time, int count) {
            super(pkgName, uid, time);
            this.mTagName = tagName;
            this.mCount = count;
        }
    }

    /* access modifiers changed from: package-private */
    public class SensorEntry extends StatisticsEntry {
        final int mHandle;

        SensorEntry(int handle, String pkgName, int uid, long time) {
            super(pkgName, uid, time);
            this.mHandle = handle;
        }
    }

    /* access modifiers changed from: package-private */
    public class ScreenoffBatteryStats {
        long btRxTotalBytes;
        long btTxTotalBytes;
        HashMap<String, WakeLockEntry> mAndroidWakelocks = new HashMap<>();
        SparseArray<Long> mBtTraffic = new SparseArray<>();
        SparseArray<Long> mMobileWakeup = new SparseArray<>();
        HashMap<String, Long> mPhoneSignalLevels = new HashMap<>();
        long mScreenOffElapsedTime;
        SparseArray<SparseArray<Long>> mSensors = new SparseArray<>();
        HashMap<String, Long> mWifiSignalLevels = new HashMap<>();
        SparseArray<Long> mWifiWakeup = new SparseArray<>();
        private long mlastRcdTime = -1;

        ScreenoffBatteryStats() {
        }

        public void update() {
            long nowElapsed = SystemClock.elapsedRealtime();
            this.mScreenOffElapsedTime = nowElapsed;
            long j = this.mlastRcdTime;
            if (-1 == j || nowElapsed - j > 60000) {
                if (OppoBaseBatteryStatsImpl.DEBUG_DETAIL) {
                    Slog.d(OppoBaseBatteryStatsImpl.TAG, "ScreenoffBatteryStats: start.");
                }
                this.mlastRcdTime = nowElapsed;
                long uptimeMillis = SystemClock.uptimeMillis() * 1000;
                long rawRealtime = nowElapsed * 1000;
                updateNetWorkTraffic(0);
                updatePhoneSignalLevels(rawRealtime, 0);
                updateWifiSignalLevels(rawRealtime, 0);
                this.mAndroidWakelocks.clear();
                this.mSensors.clear();
                this.mMobileWakeup.clear();
                this.mWifiWakeup.clear();
                SparseArray<? extends BatteryStats.Uid> uidStats = OppoBaseBatteryStatsImpl.this.getUidStats();
                int iu = 0;
                for (int NU = uidStats.size(); iu < NU; NU = NU) {
                    BatteryStats.Uid u = (BatteryStats.Uid) uidStats.valueAt(iu);
                    int uid = u.getUid();
                    updataAndroidWakelockPerUid(u, uid, rawRealtime, 0);
                    updateSensorsGpsPerUid(u, uid, rawRealtime, 0);
                    updataTrafficPerUid(u, uid, rawRealtime, 0);
                    updateMobileWakeupPerUid(u, uid, rawRealtime, 0);
                    updateWifiWakeupPerUid(u, uid, rawRealtime, 0);
                    iu++;
                }
                if (OppoBaseBatteryStatsImpl.DEBUG_DETAIL) {
                    Slog.d(OppoBaseBatteryStatsImpl.TAG, "ScreenoffBatteryStats: exit.");
                }
            } else if (OppoBaseBatteryStatsImpl.DEBUG_DETAIL) {
                Slog.d(OppoBaseBatteryStatsImpl.TAG, "ScreenoffBatteryStats: interval too short. ignore.");
            }
        }

        private void updateNetWorkTraffic(int which) {
            this.btRxTotalBytes = OppoBaseBatteryStatsImpl.this.getNetworkActivityBytes(4, which);
            this.btTxTotalBytes = OppoBaseBatteryStatsImpl.this.getNetworkActivityBytes(5, which);
            if (OppoBaseBatteryStatsImpl.DEBUG_DETAIL) {
                Slog.d(OppoBaseBatteryStatsImpl.TAG, "ScreenoffBatteryStats. btRxTotalBytes=" + this.btRxTotalBytes + ", btTxTotalBytes=" + this.btTxTotalBytes);
            }
        }

        private void updatePhoneSignalLevels(long rawRealtime, int which) {
            this.mPhoneSignalLevels.clear();
            for (int i = 0; i < 5; i++) {
                long time = OppoBaseBatteryStatsImpl.this.getPhoneSignalStrengthTime(i, rawRealtime, which) / 1000;
                if (time > 0) {
                    if (OppoBaseBatteryStatsImpl.DEBUG_DETAIL) {
                        Slog.d(OppoBaseBatteryStatsImpl.TAG, "ScreenoffBatteryStats: PhoneSignalLevels. time=" + time + ", level=" + SignalStrength.SIGNAL_STRENGTH_NAMES[i]);
                    }
                    this.mPhoneSignalLevels.put(SignalStrength.SIGNAL_STRENGTH_NAMES[i], Long.valueOf(time));
                }
            }
        }

        private void updateWifiSignalLevels(long rawRealtime, int which) {
            this.mWifiSignalLevels.clear();
            for (int i = 0; i < 5; i++) {
                long time = OppoBaseBatteryStatsImpl.this.getWifiSignalStrengthTime(i, rawRealtime, which) / 1000;
                if (time > 0) {
                    String level = 1 != 0 ? SignalStrength.SIGNAL_STRENGTH_NAMES[i] : "level" + i;
                    if (OppoBaseBatteryStatsImpl.DEBUG_DETAIL) {
                        Slog.d(OppoBaseBatteryStatsImpl.TAG, "ScreenoffBatteryStats: WifiSignalLevels. time=" + time + ", level=" + level);
                    }
                    this.mWifiSignalLevels.put(level, Long.valueOf(time));
                }
            }
        }

        private void updataAndroidWakelockPerUid(BatteryStats.Uid u, int uid, long rawRealtime, int which) {
            ArrayMap<String, ? extends BatteryStats.Uid.Wakelock> wakelocks;
            ScreenoffBatteryStats screenoffBatteryStats = this;
            int i = which;
            ArrayMap<String, ? extends BatteryStats.Uid.Wakelock> wakelocks2 = u.getWakelockStats();
            int iw = wakelocks2.size() - 1;
            while (iw >= 0) {
                BatteryStats.Uid.Wakelock wl = (BatteryStats.Uid.Wakelock) wakelocks2.valueAt(iw);
                String tagName = wakelocks2.keyAt(iw);
                BatteryStats.Timer partialWakeTimer = wl.getWakeTime(0);
                if (partialWakeTimer == null) {
                    wakelocks = wakelocks2;
                } else {
                    long totalTimeMillis = (partialWakeTimer.getTotalTimeLocked(rawRealtime, i) + 500) / 1000;
                    if (totalTimeMillis <= JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS) {
                        wakelocks = wakelocks2;
                    } else {
                        int count = partialWakeTimer.getCountLocked(i);
                        wakelocks = wakelocks2;
                        screenoffBatteryStats.mAndroidWakelocks.put(tagName + uid, new WakeLockEntry(tagName, "android", uid, totalTimeMillis, count));
                        if (OppoBaseBatteryStatsImpl.DEBUG_DETAIL) {
                            Slog.d(OppoBaseBatteryStatsImpl.TAG, "ScreenoffBatteryStats: AndroidWakelocks=" + tagName + ", time=" + totalTimeMillis + ", uid=" + uid);
                        }
                    }
                }
                iw--;
                screenoffBatteryStats = this;
                i = which;
                wakelocks2 = wakelocks;
            }
        }

        private void updateSensorsGpsPerUid(BatteryStats.Uid u, int uid, long rawRealtime, int which) {
            SparseArray<? extends BatteryStats.Uid.Sensor> sensors;
            SparseArray<? extends BatteryStats.Uid.Sensor> sensors2 = u.getSensorStats();
            int NSE = sensors2.size();
            int ise = 0;
            while (ise < NSE) {
                BatteryStats.Uid.Sensor se = (BatteryStats.Uid.Sensor) sensors2.valueAt(ise);
                sensors2.keyAt(ise);
                int handle = se.getHandle();
                BatteryStats.Timer timer = se.getSensorTime();
                if (timer == null) {
                    sensors = sensors2;
                } else {
                    long totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                    if (totalTime <= JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS) {
                        sensors = sensors2;
                    } else {
                        SparseArray<Long> sensorsPerUid = this.mSensors.get(uid);
                        if (sensorsPerUid == null) {
                            SparseArray<Long> tmpSensors = new SparseArray<>();
                            sensors = sensors2;
                            tmpSensors.put(handle, Long.valueOf(totalTime));
                            this.mSensors.put(uid, tmpSensors);
                        } else {
                            sensors = sensors2;
                            sensorsPerUid.put(handle, Long.valueOf(totalTime));
                        }
                        if (OppoBaseBatteryStatsImpl.DEBUG_DETAIL) {
                            Slog.d(OppoBaseBatteryStatsImpl.TAG, "ScreenoffBatteryStats: sensors. handle=" + handle + ", time=" + totalTime + ", uid=" + uid);
                        }
                    }
                }
                ise++;
                sensors2 = sensors;
            }
        }

        private void updataTrafficPerUid(BatteryStats.Uid u, int uid, long rawRealtime, int which) {
            long btTraffic = u.getNetworkActivityBytes(4, which) + u.getNetworkActivityBytes(5, which);
            if (btTraffic > 1048576) {
                this.mBtTraffic.put(uid, Long.valueOf(btTraffic));
                if (OppoBaseBatteryStatsImpl.DEBUG_DETAIL) {
                    Slog.d(OppoBaseBatteryStatsImpl.TAG, "updataTrafficPerUid: BtTraffic. uid=" + uid + ", btTraffic=" + OppoBaseBatteryStatsImpl.this.formatBytesLocal(btTraffic));
                }
            }
        }

        private void updateMobileWakeupPerUid(BatteryStats.Uid u, int uid, long rawRealtime, int which) {
            long mobileWakeup = u.getMobileRadioApWakeupCount(which);
            if (mobileWakeup > 0) {
                this.mMobileWakeup.put(uid, Long.valueOf(mobileWakeup));
                if (OppoBaseBatteryStatsImpl.DEBUG_DETAIL) {
                    Slog.d(OppoBaseBatteryStatsImpl.TAG, "updateMobileWakeupPerUid: uid=" + uid + ", mobileWakeup=" + mobileWakeup);
                }
            }
        }

        private void updateWifiWakeupPerUid(BatteryStats.Uid u, int uid, long rawRealtime, int which) {
            long wifiWakeup = u.getWifiRadioApWakeupCount(which);
            if (wifiWakeup > 0) {
                this.mWifiWakeup.put(uid, Long.valueOf(wifiWakeup));
                if (OppoBaseBatteryStatsImpl.DEBUG_DETAIL) {
                    Slog.d(OppoBaseBatteryStatsImpl.TAG, "updateWifiWakeupPerUid: uid=" + uid + ", wifiWakeup=" + wifiWakeup);
                }
            }
        }
    }

    public OppoBaseBatteryStatsImpl() {
        this.mFormatBuilder = new StringBuilder(32);
        this.mFormatter = new Formatter(this.mFormatBuilder);
        this.mScreenoffBatteryStats = new ScreenoffBatteryStats();
        this.mLocalHandler = null;
        this.StatisticsComparator = new Comparator<StatisticsEntry>() {
            /* class com.android.internal.os.OppoBaseBatteryStatsImpl.AnonymousClass1 */

            public int compare(StatisticsEntry lhs, StatisticsEntry rhs) {
                long lhsTime = lhs.mTime;
                long rhsTime = rhs.mTime;
                if (lhsTime < rhsTime) {
                    return 1;
                }
                if (lhsTime > rhsTime) {
                    return -1;
                }
                return 0;
            }
        };
        this.mOppoThermalStatsHelper = null;
        this.mThermalRecFile = null;
        this.DEBUG_SUPPER_APP = true;
        this.resumedUid = -1;
        this.resumedBatteryLevel = -1;
        this.resumedElapsedRealtime = -1;
        this.resumedBatteryRealtimeCapacity = -1;
        this.resumedPackage = null;
        this.resumedClass = null;
        this.pausedBatteryLevel = -1;
        this.pausedElapsedRealtime = -1;
        this.mThermalRecFile = null;
    }

    public OppoBaseBatteryStatsImpl(File systemDir, Handler handler) {
        this.mFormatBuilder = new StringBuilder(32);
        this.mFormatter = new Formatter(this.mFormatBuilder);
        this.mScreenoffBatteryStats = new ScreenoffBatteryStats();
        this.mLocalHandler = null;
        this.StatisticsComparator = new Comparator<StatisticsEntry>() {
            /* class com.android.internal.os.OppoBaseBatteryStatsImpl.AnonymousClass1 */

            public int compare(StatisticsEntry lhs, StatisticsEntry rhs) {
                long lhsTime = lhs.mTime;
                long rhsTime = rhs.mTime;
                if (lhsTime < rhsTime) {
                    return 1;
                }
                if (lhsTime > rhsTime) {
                    return -1;
                }
                return 0;
            }
        };
        this.mOppoThermalStatsHelper = null;
        this.mThermalRecFile = null;
        this.DEBUG_SUPPER_APP = true;
        this.resumedUid = -1;
        this.resumedBatteryLevel = -1;
        this.resumedElapsedRealtime = -1;
        this.resumedBatteryRealtimeCapacity = -1;
        this.resumedPackage = null;
        this.resumedClass = null;
        this.pausedBatteryLevel = -1;
        this.pausedElapsedRealtime = -1;
        if (systemDir == null) {
            this.mThermalRecFile = null;
            Slog.d(TAG, "systemDir IS NULL");
        } else {
            this.mThermalRecFile = new File(systemDir, "thermalstats.bin");
            File file = this.mThermalRecFile;
            if (file == null || !file.exists()) {
                Slog.d(TAG, "thermalstats.bin creat failed");
            } else {
                Slog.d(TAG, "thermalstats.bin creat success");
            }
        }
        this.mLocalHandler = handler;
        this.mOppoThermalStatsHelper = new OppoThermalStatsHelper(this, handler, this.mThermalRecFile, systemDir);
    }

    public void noteBaseScreenStateLocked(int state, int oldState, long elapsedRealtime, SparseArray<BatteryStatsImpl.Uid> uidStats) {
        if (isScreenOn(state)) {
            if (DEBUG_UID_SCREEN_BASIC && this.mUidTopActivity != null) {
                Slog.d(TAG, "noteScreenStateLocked: STATE_ON. UidTopActivity uid=" + this.mUidTopActivity.mUid);
            }
            dumpUidScreenBrightnessLocked("SCREEN_ON", uidStats);
        } else if (isScreenOn(oldState)) {
            if (DEBUG_UID_SCREEN_BASIC && this.mUidTopActivity != null) {
                Slog.d(TAG, "noteScreenStateLocked: STATE_OFF. UidTopActivity uid=" + this.mUidTopActivity.mUid);
            }
            screenBrightnessOffLocked(elapsedRealtime, uidStats);
            screenoffStatsRcd();
        }
    }

    public void noteBaseScreenBrightnessLocked(int bin, long elapsedRealtime, int mScreenBrightnessBin) {
        if (this.mUidTopActivity != null) {
            if (DEBUG_UID_SCREEN_BASIC) {
                Slog.d(TAG, "noteScreenBrightnessLocked: bin=" + bin);
            }
            OppoBaseBatteryStats.OppoBaseUid oppoBaseUid = typeCastingToBaseUid(this.mUidTopActivity);
            if (oppoBaseUid != null) {
                oppoBaseUid.noteScreenBrightnessLocked(elapsedRealtime, mScreenBrightnessBin, bin);
            }
        }
    }

    public void dumpBaseScreenOffIdleLocked(Context context, PrintWriter pw, List<Sensor> listAllSensor) {
        OppoBaseBatteryStatsImpl oppoBaseBatteryStatsImpl = this;
        PrintWriter printWriter = pw;
        long uptimeMillis = SystemClock.uptimeMillis() * 1000;
        long rawRealtime = SystemClock.elapsedRealtime() * 1000;
        long screenoffInterval = SystemClock.elapsedRealtime() - oppoBaseBatteryStatsImpl.mScreenoffBatteryStats.mScreenOffElapsedTime;
        if (screenoffInterval < AlarmManager.INTERVAL_HALF_HOUR) {
            if (DEBUG_DETAIL) {
                Slog.d(TAG, "dumpScreenOffIdleLocked: screenoffInterval=" + screenoffInterval + "ms, ignore.");
            }
            printWriter.println("ScreenOffIntervalShort");
            return;
        }
        StringBuilder sb = new StringBuilder(128);
        printWriter.println("ScreenOffBatteryStats");
        dumpNetworkTraffic(pw, sb, rawRealtime, 0);
        dumpPhoneSignalLevels(pw, sb, rawRealtime, 0);
        dumpWifiSignalLevels(pw, sb, rawRealtime, 0);
        SparseArray<? extends BatteryStats.Uid> uidStats = getUidStats();
        int NU = uidStats.size();
        ArrayList<WakeLockEntry> listAndroidWakelock = new ArrayList<>();
        ArrayList<WakeLockEntry> listAndroidWakelock2 = new ArrayList<>();
        ArrayList<SensorEntry> listGps = new ArrayList<>();
        ArrayList<StatisticsEntry> listBtTraffic = new ArrayList<>();
        ArrayList<StatisticsEntry> listMobileWakeup = new ArrayList<>();
        ArrayList<StatisticsEntry> listWifiWakeup = new ArrayList<>();
        int iu = 0;
        while (iu < NU) {
            BatteryStats.Uid u = (BatteryStats.Uid) uidStats.valueAt(iu);
            getAndroidWakelockPerUid(rawRealtime, 0, u, listAndroidWakelock);
            getSensorsGpsPerUid(rawRealtime, 0, u, listAndroidWakelock2, listGps);
            getTrafficPerUid(rawRealtime, 0, u, listBtTraffic);
            getMobileWakeupPerUid(rawRealtime, 0, u, listMobileWakeup);
            getWifiWakeupPerUid(rawRealtime, 0, u, listWifiWakeup);
            iu++;
            sb = sb;
            oppoBaseBatteryStatsImpl = oppoBaseBatteryStatsImpl;
            printWriter = printWriter;
            listBtTraffic = listBtTraffic;
            listGps = listGps;
            uidStats = uidStats;
            NU = NU;
            screenoffInterval = screenoffInterval;
            listAndroidWakelock = listAndroidWakelock;
            listAndroidWakelock2 = listAndroidWakelock2;
        }
        oppoBaseBatteryStatsImpl.dumpAndroidWakelock(listAndroidWakelock, sb, printWriter);
        oppoBaseBatteryStatsImpl.dumpSensors(listAndroidWakelock2, sb, printWriter, listAllSensor);
        oppoBaseBatteryStatsImpl.dumpGps(listGps, sb, printWriter);
        oppoBaseBatteryStatsImpl.dumpTraffic(listBtTraffic, sb, printWriter, "AppBtTraffic:  ");
        dumpAppNetWakeup(listMobileWakeup, sb, pw, screenoffInterval, "AppModemWakeupCycle:  ");
        dumpAppNetWakeup(listWifiWakeup, sb, pw, screenoffInterval, "AppWifiWakeupCycle:  ");
    }

    public boolean isScreenOn() {
        return isScreenOn(getScreenState());
    }

    private boolean isScreenOn(int state) {
        return state == 2 || state == 5 || state == 6;
    }

    private void dumpNetworkTraffic(PrintWriter pw, StringBuilder sb, long rawRealtime, int which) {
        long btTraffic = (getNetworkActivityBytes(4, which) + getNetworkActivityBytes(5, which)) - (this.mScreenoffBatteryStats.btRxTotalBytes + this.mScreenoffBatteryStats.btTxTotalBytes);
        if (DEBUG_DETAIL) {
            Slog.d(TAG, "dumpNetworkTraffic: btTraffic=" + btTraffic);
        }
        if (btTraffic > MIN_TRAFFIC_BYTES) {
            sb.setLength(0);
            sb.append("BtTraffic:  ");
            sb.append(formatBytesLocal(btTraffic));
            pw.println(sb.toString());
        }
    }

    private void dumpPhoneSignalLevels(PrintWriter pw, StringBuilder sb, long rawRealtime, int which) {
        sb.setLength(0);
        sb.append("PhoneSignalLevels:  ");
        boolean dataValid = false;
        HashMap<String, Long> phoneSignalLevelsBak = this.mScreenoffBatteryStats.mPhoneSignalLevels;
        for (int i = 0; i < 5; i++) {
            String levelStr = SignalStrength.SIGNAL_STRENGTH_NAMES[i];
            long time = getPhoneSignalStrengthTime(i, rawRealtime, which) / 1000;
            if (time > 0) {
                long timeBak = 0;
                if (!(phoneSignalLevelsBak == null || phoneSignalLevelsBak.get(levelStr) == null)) {
                    timeBak = phoneSignalLevelsBak.get(levelStr).longValue();
                }
                long timeDelta = time - timeBak;
                if (timeDelta > 0) {
                    sb.append("[" + levelStr + "] (");
                    formatTimeMs(sb, timeDelta);
                    sb.append("), ");
                    if (i <= 2 && timeDelta > ParcelableCallAnalytics.MILLIS_IN_5_MINUTES) {
                        dataValid = true;
                    }
                    if (DEBUG_DETAIL) {
                        Slog.d(TAG, "dumpPhoneSignalLevels: levelStr=" + levelStr + ", timeBak=" + timeBak + ", time=" + time + ", timeDelta=" + timeDelta);
                    }
                }
            }
        }
        if (dataValid) {
            pw.println(sb.toString());
        }
    }

    private void dumpUidScreenBrightnessLocked(String state, SparseArray<BatteryStatsImpl.Uid> uidStats) {
        int NU = uidStats.size();
        for (int iu = 0; iu < NU; iu++) {
            BatteryStatsImpl.Uid uid = uidStats.valueAt(iu);
            Uid baseImplUid = (Uid) ColorTypeCastingHelper.typeCasting(Uid.class, uid);
            if (baseImplUid != null) {
                for (int j = 0; j < 5; j++) {
                    BatteryStatsImpl.StopwatchTimer timer = baseImplUid.mUidScreenBrightnessTimer[j];
                    if (timer != null && timer.isRunningLocked()) {
                        Slog.d(TAG, "dumpUidScreenBrightnessLocked: " + state + ", uid=" + uid.mUid + ", mUidScreenBrightnessTimer[" + j + "] is running");
                    }
                }
            }
            if (uid.mForegroundActivityTimer != null && uid.mForegroundActivityTimer.isRunningLocked()) {
                Slog.d(TAG, "dumpUidScreenBrightnessLocked: " + state + ", uid(" + uid.mUid + "), mForegroundActivityTimer is running.");
            }
            OppoBaseBatteryStats.OppoBaseUid oppoBaseUid = typeCastingToBaseUid(uid);
            if (oppoBaseUid != null) {
                oppoBaseUid.dumpApkScreenBrightnessLocked(state);
            }
        }
    }

    /* JADX INFO: Multiple debug info for r2v6 long: [D('timeDelta' long), D('usePhoneSignalStr' boolean)] */
    private void dumpWifiSignalLevels(PrintWriter pw, StringBuilder sb, long rawRealtime, int which) {
        String levelStr;
        HashMap<String, Long> wifiSignalLevelsBak;
        boolean usePhoneSignalStr;
        sb.setLength(0);
        sb.append("WifiSignalLevels:  ");
        boolean usePhoneSignalStr2 = true;
        HashMap<String, Long> wifiSignalLevelsBak2 = this.mScreenoffBatteryStats.mWifiSignalLevels;
        boolean dataValid = false;
        int i = 0;
        while (i < 5) {
            if (usePhoneSignalStr2) {
                levelStr = SignalStrength.SIGNAL_STRENGTH_NAMES[i];
            } else {
                levelStr = "level" + i;
            }
            long time = getWifiSignalStrengthTime(i, rawRealtime, which) / 1000;
            if (time <= 0) {
                usePhoneSignalStr = usePhoneSignalStr2;
                wifiSignalLevelsBak = wifiSignalLevelsBak2;
            } else {
                long timeBak = 0;
                if (!(wifiSignalLevelsBak2 == null || wifiSignalLevelsBak2.get(levelStr) == null)) {
                    timeBak = wifiSignalLevelsBak2.get(levelStr).longValue();
                }
                usePhoneSignalStr = usePhoneSignalStr2;
                wifiSignalLevelsBak = wifiSignalLevelsBak2;
                long timeDelta = time - timeBak;
                if (timeDelta > 0) {
                    sb.append("[" + levelStr + "] (");
                    formatTimeMs(sb, timeDelta);
                    sb.append("), ");
                    if (i <= 2 && timeDelta > ParcelableCallAnalytics.MILLIS_IN_5_MINUTES) {
                        dataValid = true;
                    }
                    if (DEBUG_DETAIL) {
                        Slog.d(TAG, "dumpWifiSignalLevels: levelStr=" + levelStr + ", timeBak=" + timeBak + ", time=" + time + ", timeDelta=" + timeDelta);
                    }
                }
            }
            i++;
            usePhoneSignalStr2 = usePhoneSignalStr;
            wifiSignalLevelsBak2 = wifiSignalLevelsBak;
        }
        if (dataValid) {
            pw.println(sb.toString());
        }
    }

    /* JADX INFO: Multiple debug info for r12v3 long: [D('heldTime' long), D('androidWakelocksBak' java.util.HashMap<java.lang.String, com.android.internal.os.OppoBaseBatteryStatsImpl$WakeLockEntry>)] */
    private void getAndroidWakelockPerUid(long rawRealtime, int which, BatteryStats.Uid u, ArrayList<WakeLockEntry> listWakelock) {
        int iw;
        ArrayMap<String, ? extends BatteryStats.Uid.Wakelock> wakelocks;
        HashMap<String, WakeLockEntry> androidWakelocksBak;
        long totalTimeMillis;
        int countBak;
        OppoBaseBatteryStatsImpl oppoBaseBatteryStatsImpl = this;
        int i = which;
        BatteryStats.Uid uid = u;
        if (listWakelock != null && uid != null) {
            HashMap<String, WakeLockEntry> androidWakelocksBak2 = oppoBaseBatteryStatsImpl.mScreenoffBatteryStats.mAndroidWakelocks;
            ArrayMap<String, ? extends BatteryStats.Uid.Wakelock> wakelocks2 = u.getWakelockStats();
            int uid2 = u.getUid();
            int iw2 = wakelocks2.size() - 1;
            while (iw2 >= 0) {
                BatteryStats.Uid.Wakelock wl = (BatteryStats.Uid.Wakelock) wakelocks2.valueAt(iw2);
                String tagName = wakelocks2.keyAt(iw2);
                BatteryStats.Timer partialWakeTimer = wl.getWakeTime(0);
                if (partialWakeTimer == null) {
                    androidWakelocksBak = androidWakelocksBak2;
                    wakelocks = wakelocks2;
                    iw = iw2;
                } else {
                    long totalTimeMillis2 = (partialWakeTimer.getTotalTimeLocked(rawRealtime, i) + 500) / 1000;
                    long timeBak = 0;
                    if (totalTimeMillis2 <= 0) {
                        androidWakelocksBak = androidWakelocksBak2;
                        wakelocks = wakelocks2;
                        iw = iw2;
                    } else {
                        WakeLockEntry wlEntry = androidWakelocksBak2.get(tagName + uid2);
                        if (wlEntry != null) {
                            timeBak = wlEntry.mTime;
                        }
                        androidWakelocksBak = androidWakelocksBak2;
                        wakelocks = wakelocks2;
                        long heldTime = totalTimeMillis2 - timeBak;
                        if (heldTime <= 720000) {
                            iw = iw2;
                        } else {
                            if (wlEntry != null) {
                                totalTimeMillis = totalTimeMillis2;
                                countBak = wlEntry.mCount;
                            } else {
                                totalTimeMillis = totalTimeMillis2;
                                countBak = 0;
                            }
                            int count = partialWakeTimer.getCountLocked(i) - countBak;
                            iw = iw2;
                            listWakelock.add(new WakeLockEntry(tagName, oppoBaseBatteryStatsImpl.getPackageName(uid, uid2), uid2, heldTime, count));
                            if (DEBUG_DETAIL) {
                                Slog.d(TAG, "dumpAndroidWakelockPerUid: tag=" + tagName + ", timeBak=" + timeBak + ", totalTimeMillis=" + totalTimeMillis + ", heldTime=" + heldTime + ", uid=" + uid2 + ", count=" + count);
                            }
                        }
                    }
                }
                iw2 = iw - 1;
                oppoBaseBatteryStatsImpl = this;
                i = which;
                uid = u;
                androidWakelocksBak2 = androidWakelocksBak;
                wakelocks2 = wakelocks;
            }
        }
    }

    /* JADX INFO: Multiple debug info for r14v3 long: [D('NSE' int), D('heldTime' long)] */
    private void getSensorsGpsPerUid(long rawRealtime, int which, BatteryStats.Uid u, ArrayList<SensorEntry> listSensor, ArrayList<SensorEntry> listGps) {
        SparseArray<? extends BatteryStats.Uid.Sensor> sensors;
        SparseArray<Long> sensorsPerUidBak;
        int ise;
        int NSE;
        long timeBak;
        int handle;
        OppoBaseBatteryStatsImpl oppoBaseBatteryStatsImpl = this;
        BatteryStats.Uid uid = u;
        if (listSensor != null && listGps != null && uid != null) {
            int uid2 = u.getUid();
            SparseArray<Long> sensorsPerUidBak2 = oppoBaseBatteryStatsImpl.mScreenoffBatteryStats.mSensors.get(uid2);
            SparseArray<? extends BatteryStats.Uid.Sensor> sensors2 = u.getSensorStats();
            int NSE2 = sensors2.size();
            int ise2 = 0;
            while (ise2 < NSE2) {
                BatteryStats.Uid.Sensor se = (BatteryStats.Uid.Sensor) sensors2.valueAt(ise2);
                int handle2 = se.getHandle();
                BatteryStats.Timer timer = se.getSensorTime();
                if (timer == null) {
                    sensorsPerUidBak = sensorsPerUidBak2;
                    sensors = sensors2;
                    NSE = NSE2;
                    ise = ise2;
                } else {
                    long totalTime = (timer.getTotalTimeLocked(rawRealtime, which) + 500) / 1000;
                    if (totalTime <= 0) {
                        sensorsPerUidBak = sensorsPerUidBak2;
                        sensors = sensors2;
                        NSE = NSE2;
                        ise = ise2;
                    } else {
                        if (sensorsPerUidBak2 == null || sensorsPerUidBak2.get(handle2) == null) {
                            sensorsPerUidBak = sensorsPerUidBak2;
                            sensors = sensors2;
                            timeBak = 0;
                        } else {
                            sensorsPerUidBak = sensorsPerUidBak2;
                            sensors = sensors2;
                            timeBak = sensorsPerUidBak2.get(handle2).longValue();
                        }
                        NSE = NSE2;
                        ise = ise2;
                        long heldTime = totalTime - timeBak;
                        if (heldTime > 720000) {
                            String pkgName = oppoBaseBatteryStatsImpl.getPackageName(uid, uid2);
                            if (-10000 == handle2) {
                                handle = handle2;
                                listGps.add(new SensorEntry(handle2, pkgName, uid2, heldTime));
                            } else {
                                handle = handle2;
                                listSensor.add(new SensorEntry(handle, pkgName, uid2, heldTime));
                            }
                            if (DEBUG_DETAIL) {
                                Slog.d(TAG, "getSensorsGpsPerUid: handle=" + handle + ", timeBak=" + timeBak + ", totalTime=" + totalTime + ", heldTime=" + heldTime + ", uid=" + uid2);
                            }
                        }
                    }
                }
                ise2 = ise + 1;
                oppoBaseBatteryStatsImpl = this;
                uid = u;
                NSE2 = NSE;
                sensorsPerUidBak2 = sensorsPerUidBak;
                sensors2 = sensors;
            }
        }
    }

    private void getTrafficPerUid(long rawRealtime, int which, BatteryStats.Uid u, ArrayList<StatisticsEntry> listBtTraffic) {
        long btTrafficBak;
        String pkgName;
        if (u != null && listBtTraffic != null) {
            long btRxBytes = u.getNetworkActivityBytes(4, which);
            long btTxBytes = u.getNetworkActivityBytes(5, which);
            int uid = u.getUid();
            if (this.mScreenoffBatteryStats.mBtTraffic == null || this.mScreenoffBatteryStats.mBtTraffic.get(uid) == null) {
                btTrafficBak = 0;
            } else {
                btTrafficBak = this.mScreenoffBatteryStats.mBtTraffic.get(uid).longValue();
            }
            long btTraffic = (btRxBytes + btTxBytes) - btTrafficBak;
            if (btTraffic >= MIN_TRAFFIC_BYTES) {
                if (0 == 0) {
                    pkgName = getPackageName(u, uid);
                } else {
                    pkgName = null;
                }
                listBtTraffic.add(new StatisticsEntry(pkgName, uid, btTraffic));
            }
        }
    }

    private void getMobileWakeupPerUid(long rawRealtime, int which, BatteryStats.Uid u, ArrayList<StatisticsEntry> listMobileWakeup) {
        long mobileWakeupBak;
        if (u == null) {
            return;
        }
        if (listMobileWakeup != null) {
            long mobileWakeupNow = u.getMobileRadioApWakeupCount(which);
            int uid = u.getUid();
            if (this.mScreenoffBatteryStats.mMobileWakeup == null || this.mScreenoffBatteryStats.mMobileWakeup.get(uid) == null) {
                mobileWakeupBak = 0;
            } else {
                mobileWakeupBak = this.mScreenoffBatteryStats.mMobileWakeup.get(uid).longValue();
            }
            long mobileWakeUp = mobileWakeupNow - mobileWakeupBak;
            if (mobileWakeUp > 0) {
                listMobileWakeup.add(new StatisticsEntry(getPackageName(u, uid), uid, mobileWakeUp));
                if (DEBUG_DETAIL) {
                    Slog.d(TAG, "getMobileWakeupPerUid: uid=" + uid + ", mobileWakeUp=" + mobileWakeUp + ", mobileWakeupNow=" + mobileWakeupNow + ", mobileWakeupBak=" + mobileWakeupBak);
                }
            }
        }
    }

    private void getWifiWakeupPerUid(long rawRealtime, int which, BatteryStats.Uid u, ArrayList<StatisticsEntry> listWifiWakeup) {
        long wifiWakeupBak;
        if (u == null) {
            return;
        }
        if (listWifiWakeup != null) {
            long wifiWakeupNow = u.getWifiRadioApWakeupCount(which);
            int uid = u.getUid();
            if (this.mScreenoffBatteryStats.mWifiWakeup == null || this.mScreenoffBatteryStats.mWifiWakeup.get(uid) == null) {
                wifiWakeupBak = 0;
            } else {
                wifiWakeupBak = this.mScreenoffBatteryStats.mWifiWakeup.get(uid).longValue();
            }
            long wifiWakeUp = wifiWakeupNow - wifiWakeupBak;
            if (wifiWakeUp > 0) {
                listWifiWakeup.add(new StatisticsEntry(getPackageName(u, uid), uid, wifiWakeUp));
                if (DEBUG_DETAIL) {
                    Slog.d(TAG, "getWifiWakeupPerUid: uid=" + uid + ", wifiWakeUp=" + wifiWakeUp + ", wifiWakeupNow=" + wifiWakeupNow + ", wifiWakeupBak=" + wifiWakeupBak);
                }
            }
        }
    }

    private void dumpAndroidWakelock(ArrayList<WakeLockEntry> listAndroidWakelock, StringBuilder sb, PrintWriter pw) {
        if (listAndroidWakelock.size() > 0) {
            Collections.sort(listAndroidWakelock, this.StatisticsComparator);
            sb.setLength(0);
            sb.append("AndroidWakeLocks:  ");
            for (int i = 0; i < listAndroidWakelock.size(); i++) {
                WakeLockEntry entry = listAndroidWakelock.get(i);
                if (i >= 3) {
                    break;
                }
                sb.append("<");
                sb.append(entry.mUid);
                sb.append("> {");
                sb.append(entry.mPkgName);
                sb.append("} [");
                sb.append(entry.mTagName);
                sb.append("] (");
                formatTimeMs(sb, entry.mTime);
                sb.append(") #");
                sb.append(entry.mCount);
                sb.append("#, ");
            }
            pw.println(sb.toString());
        }
    }

    private void dumpSensors(ArrayList<SensorEntry> listSensor, StringBuilder sb, PrintWriter pw, List<Sensor> listAllSensor) {
        if (listSensor.size() > 0) {
            Collections.sort(listSensor, this.StatisticsComparator);
            sb.setLength(0);
            sb.append("Sensors:  ");
            int cnt = 0;
            for (int i = 0; i < listSensor.size(); i++) {
                SensorEntry entry = listSensor.get(i);
                if (cnt >= 3) {
                    break;
                }
                Sensor sensor = getSensorForHandle(entry.mHandle, listAllSensor);
                if (sensor != null) {
                    int type = sensor.getType();
                    String name = sensor.getName();
                    if (!(type == 19 || type == 18 || type == 17 || name == null)) {
                        sb.append("<");
                        sb.append(type);
                        sb.append("> {");
                        sb.append(entry.mPkgName);
                        sb.append("} [");
                        sb.append(name);
                        sb.append("] (");
                        formatTimeMs(sb, entry.mTime);
                        sb.append("), ");
                        cnt++;
                    }
                }
            }
            if (cnt > 0) {
                pw.println(sb.toString());
            }
        }
    }

    private void dumpGps(ArrayList<SensorEntry> listGps, StringBuilder sb, PrintWriter pw) {
        if (listGps.size() > 0) {
            Collections.sort(listGps, this.StatisticsComparator);
            sb.setLength(0);
            sb.append("Gps:  ");
            for (int i = 0; i < listGps.size(); i++) {
                SensorEntry entry = listGps.get(i);
                if (i >= 3) {
                    break;
                }
                sb.append("<");
                sb.append(entry.mUid);
                sb.append("> {");
                sb.append(entry.mPkgName);
                sb.append("} (");
                formatTimeMs(sb, entry.mTime);
                sb.append("), ");
            }
            pw.println(sb.toString());
        }
    }

    private void dumpTraffic(ArrayList<StatisticsEntry> listTraffic, StringBuilder sb, PrintWriter pw, String tag) {
        if (listTraffic.size() > 0) {
            Collections.sort(listTraffic, this.StatisticsComparator);
            sb.setLength(0);
            sb.append(tag);
            for (int i = 0; i < listTraffic.size(); i++) {
                StatisticsEntry entry = listTraffic.get(i);
                if (i >= 3) {
                    break;
                }
                sb.append("<");
                sb.append(entry.mUid);
                sb.append("> {");
                sb.append(entry.mPkgName);
                sb.append("} (");
                sb.append(formatBytesLocal(entry.mTime));
                sb.append("), ");
            }
            pw.println(sb.toString());
        }
    }

    private void dumpAppNetWakeup(ArrayList<StatisticsEntry> listWakeup, StringBuilder sb, PrintWriter pw, long screenoffInterval, String keyTag) {
        if (!(listWakeup == null || listWakeup.isEmpty())) {
            Collections.sort(listWakeup, this.StatisticsComparator);
            boolean valid = false;
            sb.setLength(0);
            sb.append(keyTag);
            for (int i = 0; i < listWakeup.size(); i++) {
                StatisticsEntry entry = listWakeup.get(i);
                long cycle = screenoffInterval / entry.mTime;
                if (i >= 3 || cycle > 180000) {
                    break;
                }
                sb.append("<");
                sb.append(entry.mUid);
                sb.append("> {");
                sb.append(entry.mPkgName);
                sb.append("} (");
                formatTimeMs(sb, cycle);
                sb.append("), ");
                valid = true;
            }
            if (valid) {
                pw.println(sb.toString());
            }
        }
    }

    private void screenBrightnessOffLocked(long elapsedRealtime, SparseArray<BatteryStatsImpl.Uid> uidStats) {
        int NU = uidStats.size();
        for (int iu = 0; iu < NU; iu++) {
            BatteryStatsImpl.Uid uid = uidStats.valueAt(iu);
            Uid baseImplUid = (Uid) ColorTypeCastingHelper.typeCasting(Uid.class, uid);
            int i = 1;
            if (baseImplUid != null) {
                int j = 0;
                while (j < 5) {
                    BatteryStatsImpl.StopwatchTimer timer = baseImplUid.mUidScreenBrightnessTimer[j];
                    if (timer != null && timer.isRunningLocked()) {
                        int nesting = timer.mNesting;
                        timer.mNesting = i;
                        timer.stopRunningLocked(elapsedRealtime);
                        if (DEBUG_UID_SCREEN_BASIC) {
                            Slog.d(TAG, "screenBrightnessOffLocked: uid(" + uid.mUid + "), BrightnessTimer[" + j + "] is running., mNesting=" + nesting + ", stop it now.");
                        }
                    }
                    j++;
                    i = 1;
                }
            }
            if (uid.mForegroundActivityTimer != null && uid.mForegroundActivityTimer.isRunningLocked()) {
                int nesting2 = uid.mForegroundActivityTimer.mNesting;
                uid.mForegroundActivityTimer.mNesting = 1;
                uid.mForegroundActivityTimer.stopRunningLocked(elapsedRealtime);
                if (DEBUG_UID_SCREEN_BASIC) {
                    Slog.d(TAG, "screenBrightnessOffLocked: uid(" + uid.mUid + "), mForegroundActivityTimer is running., mNesting=" + nesting2 + ", stop it now.");
                }
            }
            OppoBaseBatteryStats.OppoBaseUid oppoBaseUid = typeCastingToBaseUid(uid);
            if (oppoBaseUid != null) {
                oppoBaseUid.screenOffScreenPowerApkHandleLocked(elapsedRealtime);
            }
        }
        this.mUidTopActivity = null;
    }

    private String getPackageName(BatteryStats.Uid u, int uid) {
        String pkgName = null;
        if (uid == 1000) {
            pkgName = "Android";
        } else {
            ArrayMap<String, ? extends BatteryStats.Uid.Pkg> packageStats = u.getPackageStats();
            int ipkg = packageStats.size() - 1;
            while (true) {
                if (ipkg < 0) {
                    break;
                }
                String pkg = packageStats.keyAt(ipkg);
                if (!"android".equals(pkg)) {
                    pkgName = pkg;
                    break;
                }
                ipkg--;
            }
            if (pkgName == null) {
                ArrayMap<String, ? extends BatteryStats.Uid.Proc> processStats = u.getProcessStats();
                int ipr = processStats.size() - 1;
                while (true) {
                    if (ipr < 0) {
                        break;
                    }
                    String procName = processStats.keyAt(ipr);
                    if (!"*wakelock*".equals(procName)) {
                        pkgName = procName;
                        break;
                    }
                    ipr--;
                }
            }
        }
        if (pkgName != null) {
            return pkgName;
        }
        return "uid(" + uid + ")";
    }

    private Sensor getSensorForHandle(int handle, List<Sensor> listAllSensor) {
        if (listAllSensor == null) {
            return null;
        }
        for (int k = 0; k < listAllSensor.size(); k++) {
            Sensor s = listAllSensor.get(k);
            if (s.getHandle() == handle) {
                return s;
            }
        }
        return null;
    }

    private void screenoffStatsRcd() {
        this.mScreenoffBatteryStats.update();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String formatBytesLocal(long bytes) {
        this.mFormatBuilder.setLength(0);
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1048576) {
            this.mFormatter.format("%.2fKB", Double.valueOf(((double) bytes) / 1024.0d));
            return this.mFormatBuilder.toString();
        } else if (bytes < 1073741824) {
            this.mFormatter.format("%.2fMB", Double.valueOf(((double) bytes) / 1048576.0d));
            return this.mFormatBuilder.toString();
        } else {
            this.mFormatter.format("%.2fGB", Double.valueOf(((double) bytes) / 1.073741824E9d));
            return this.mFormatBuilder.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public Uid typeCasting(BatteryStatsImpl.Uid uidTopActivity) {
        return (Uid) ColorTypeCastingHelper.typeCasting(Uid.class, uidTopActivity);
    }

    /* access modifiers changed from: package-private */
    public OppoBaseBatteryStats.OppoBaseUid typeCastingToBaseUid(BatteryStatsImpl.Uid uidTopActivity) {
        return (OppoBaseBatteryStats.OppoBaseUid) ColorTypeCastingHelper.typeCasting(OppoBaseBatteryStats.OppoBaseUid.class, uidTopActivity);
    }

    /* access modifiers changed from: protected */
    public boolean onBatteryStatsMessageHandle(Message msg) {
        if (msg == null) {
            return false;
        }
        int i = msg.what;
        if (i == 128) {
            getAllSensorList();
            OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
            if (oppoThermalStatsHelper != null) {
                oppoThermalStatsHelper.setBatteryStatsReady(true);
            }
            return true;
        } else if (i != 256) {
            return false;
        } else {
            long resumedTimeInMs = getClockElapsedRealtime() - this.resumedElapsedRealtime;
            int deltaBC = this.resumedBatteryRealtimeCapacity - getBatteryRealtimeCapacity();
            long averageCurrent = (((long) deltaBC) * 3600000) / resumedTimeInMs;
            if (this.DEBUG_SUPPER_APP) {
                Slog.d(TAG, "resumedTimeInMs=" + resumedTimeInMs + "  deltaBC=" + deltaBC + "  averageCurrent=" + averageCurrent);
            }
            if (averageCurrent > HIGH_POWER_THRESHOLD) {
                final Intent highPowerSceneIntent = new Intent("oppo.intent.action.ACTION_HIGH_POWER_SCENE");
                highPowerSceneIntent.putExtra("appName", this.resumedPackage);
                highPowerSceneIntent.putExtra("averageCurrent", averageCurrent);
                highPowerSceneIntent.setPackage("com.oppo.oppopowermonitor");
                new Thread(new Runnable() {
                    /* class com.android.internal.os.OppoBaseBatteryStatsImpl.AnonymousClass2 */

                    public void run() {
                        if (OppoBaseBatteryStatsImpl.this.mContext != null) {
                            OppoBaseBatteryStatsImpl.this.mContext.sendBroadcast(highPowerSceneIntent);
                        }
                    }
                }).start();
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void addThermalForeProc(long elapsedRealTime, long uptime, String procName, int uid, int code) {
        OppoThermalStatsHelper oppoThermalStatsHelper;
        if ((-1 == code || code == 32776 || code == 32771) && (oppoThermalStatsHelper = this.mOppoThermalStatsHelper) != null) {
            oppoThermalStatsHelper.addThermalForeProc(elapsedRealTime, uptime, procName, uid);
        }
    }

    public void noteConnectivityChangedLocked(int type, String extra, long elapsedRealtime, long uptime) {
        int netConnectType = -1;
        if (extra.equals("DISCONNECTED")) {
            netConnectType = -1;
            OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
            if (oppoThermalStatsHelper != null) {
                oppoThermalStatsHelper.addThermalConnectType(elapsedRealtime, uptime, (byte) 0);
            }
        } else if (extra.equals("CONNECTED") && type == 1) {
            netConnectType = 1;
            OppoThermalStatsHelper oppoThermalStatsHelper2 = this.mOppoThermalStatsHelper;
            if (oppoThermalStatsHelper2 != null) {
                oppoThermalStatsHelper2.addThermalConnectType(elapsedRealtime, uptime, (byte) 1);
            }
        } else if (extra.equals("CONNECTED") && type == 0) {
            netConnectType = 0;
        }
        OppoThermalStatsHelper oppoThermalStatsHelper3 = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper3 != null) {
            oppoThermalStatsHelper3.setConnectyType(netConnectType);
        }
    }

    /* access modifiers changed from: protected */
    public int getBatteryRealtimeCapacity() {
        int realtimeBatteryCapacity = 0;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(BATTERY_REALTIME_CAPACITY)));
            while (true) {
                String tempString = reader2.readLine();
                if (tempString == null) {
                    try {
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (tempString.length() != 0) {
                    realtimeBatteryCapacity = Integer.parseInt(tempString.trim());
                }
            }
            reader2.close();
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            realtimeBatteryCapacity = -1;
            if (0 != 0) {
                reader.close();
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            realtimeBatteryCapacity = -1;
            if (0 != 0) {
                reader.close();
            }
        } catch (NumberFormatException e4) {
            e4.printStackTrace();
            realtimeBatteryCapacity = -1;
            if (0 != 0) {
                reader.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
        return realtimeBatteryCapacity;
    }

    public void onSystemServicesReady(Context context) {
        Slog.d(TAG, "systemReady.......");
        this.mLocalHandler.sendEmptyMessageDelayed(128, 5000);
        this.mContext = context;
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.onSystemReady(context);
        }
    }

    /* access modifiers changed from: protected */
    public void collectCheckinFile() {
        if (getLowDischargeAmountSinceCharge() >= 20) {
            final Parcel parcel = Parcel.obtain();
            onWriteSummaryToParcel(parcel, true);
            BackgroundThread.getHandler().post(new Runnable() {
                /* class com.android.internal.os.OppoBaseBatteryStatsImpl.AnonymousClass3 */

                public void run() {
                    Parcel parcel;
                    AtomicFile checkinFile = OppoBaseBatteryStatsImpl.this.getBatteryCheckinFile();
                    if (checkinFile == null) {
                        Slog.e(OppoBaseBatteryStatsImpl.TAG, "fatal exception:collectCheckinFile failed to get checkinFile");
                        return;
                    }
                    synchronized (checkinFile) {
                        FileOutputStream stream = null;
                        try {
                            stream = checkinFile.startWrite();
                            stream.write(parcel.marshall());
                            stream.flush();
                            FileUtils.sync(stream);
                            stream.close();
                            checkinFile.finishWrite(stream);
                            Intent statusIntent = new Intent(OppoBaseBatteryStatsImpl.DUMP_HISTORY_INTENT_O);
                            statusIntent.setPackage("com.oppo.oppopowermonitor");
                            OppoBaseBatteryStatsImpl.this.onBatterySendBroadcast(statusIntent);
                            parcel = parcel;
                        } catch (IOException e) {
                            Slog.w("BatteryStats", "Error writing checkin battery statistics", e);
                            checkinFile.failWrite(stream);
                            parcel = parcel;
                        } catch (Throwable th) {
                            parcel.recycle();
                            throw th;
                        }
                        parcel.recycle();
                    }
                }
            });
        }
    }

    private void getAllSensorList() {
        Context context = this.mContext;
        if (context != null) {
            SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (sm == null) {
                Slog.d(TAG, "getAllSensorList: SensorManager is null.");
                return;
            }
            this.mListAllSensor = sm.getSensorList(-1);
            if (DEBUG_DETAIL) {
                for (int k = 0; k < this.mListAllSensor.size(); k++) {
                    Sensor sensor = this.mListAllSensor.get(k);
                    Slog.d(TAG, "getAllSensorList: sensor handle(" + sensor.getHandle() + "), type(" + sensor.getType() + "), name(" + sensor.getName() + ")");
                }
            }
        }
    }

    public void oppoLogSwitch(boolean en) {
        DEBUG_DETAIL = en;
        DEBUG_UID_SCREEN_BASIC = en;
        DEBUG_UID_SCREEN_DETAIL = en;
        Slog.d(TAG, "oppoLogSwitch: en=" + en);
    }

    public void dumpScreenOffIdleLocked(Context context, PrintWriter pw) {
        dumpBaseScreenOffIdleLocked(context, pw, this.mListAllSensor);
    }

    public boolean startIteratingThermalHistoryLocked() {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            return oppoThermalStatsHelper.startIteratingThermalHistoryLocked();
        }
        return false;
    }

    public void finishIteratingThermalHistoryLocked() {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.finishIteratingThermalHistoryLocked();
        }
    }

    public int getThermalHistoryUsedSize() {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            return oppoThermalStatsHelper.getThermalHistoryUsedSize();
        }
        return -1;
    }

    public boolean getNextThermalHistoryLocked(OppoBaseBatteryStats.ThermalItem out, long histStart) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            return oppoThermalStatsHelper.getNextThermalHistoryLocked(out, histStart);
        }
        return false;
    }

    public OppoBaseBatteryStats.ThermalItem getThermalHistoryFromFile(BufferedReader reader, PrintWriter pw, OppoBaseBatteryStats.ThermalHistoryPrinter printer) throws IOException {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            return oppoThermalStatsHelper.getThermalHistoryFromFile(reader, pw, printer);
        }
        return null;
    }

    public void getThermalRawHistoryFromFile(BufferedReader reader, PrintWriter pw) throws IOException {
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                return;
            }
            if (pw != null) {
                pw.print(line);
                pw.println();
            }
        }
    }

    public boolean getBatteryStatsReady() {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            return oppoThermalStatsHelper.getBatteryStatsReadyStatus();
        }
        return false;
    }

    public void noteScreenBrightnessModeChangedLock(boolean isAuto) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.noteScreenBrightnessModeChangedLock(isAuto);
        }
    }

    public void setThermalState(OppoThermalState thermalState) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.setThermalState(thermalState);
        }
    }

    public void schedulerUpdateCpu(long delay) {
        onSchedulerUpdateCpu(delay);
    }

    public void dumpThemalRawLocked(PrintWriter pw, long histStart) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.writeThermalRecFile();
        }
        dumpThemalRawLockedInner(pw, histStart);
    }

    public void setThermalCpuLoading(int load1, int load5, int load15, int cpuLoading, int maxCpu, String cpuProc, String simpleTopProc) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.setThermalCpuLoading(load1, load5, load15, cpuLoading, maxCpu, cpuProc, simpleTopProc);
        }
    }

    public OppoThermalStatsHelper getOppoThermalStatsHelper() {
        return this.mOppoThermalStatsHelper;
    }

    public void clearThermalAllHistory() {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.clearThermalAllHistory();
        }
    }

    public void toggleThermalDebugSwith(PrintWriter pw, int on) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.toggleThermalDebugSwith(pw, on);
        }
    }

    public void updateCpuStatsNow(PrintWriter pw) {
        schedulerUpdateCpu(0);
    }

    public void setThermalHeatThreshold(PrintWriter pw, int threshold) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.setThermalHeatThreshold(pw, threshold);
        }
    }

    public void setHeatBetweenTime(PrintWriter pw, int time) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.setHeatBetweenTime(pw, time);
        }
    }

    public void setMonitorAppLimitTime(PrintWriter pw, int limitTime) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.setMonitorAppLimitTime(pw, limitTime);
        }
    }

    public void getMonitorAppLocked(PrintWriter pw) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.getMonitorAppLocked(pw);
        }
    }

    public void backupThermalStatsFile() {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.backupThermalStatsFile();
        }
    }

    public void dumpThemalHeatDetailLocked(PrintWriter pw) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.dumpThemalHeatDetailLocked(pw);
        }
    }

    public void getPhoneTemp(PrintWriter pw) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.getPhoneTemp(pw);
        }
    }

    public void printThermalUploadTemp(PrintWriter pw) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.printThermalUploadTemp(pw);
        }
    }

    public void printChargeMapLocked(PrintWriter pw) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.printChargeMapLocked(pw);
        }
    }

    public void printThermalHeatThreshold(PrintWriter pw) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.printThermalHeatThreshold(pw);
        }
    }

    public void backupThermalLogFile() {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.backupThermalLogFile();
        }
    }

    public void setThermalConfig() {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.setThermalConfig();
        }
    }

    public void addThermalnetSyncProc(long elapsedRealtime, long uptime, String procName) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.addThermalnetSyncProc(elapsedRealtime, uptime, procName);
        }
    }

    public void addThermalJobProc(long elapsedRealtime, long uptime, String procName) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.addThermalJobProc(elapsedRealtime, uptime, procName);
        }
    }

    public void addThermalOnOffEvent(int eventType, long elapsedRealtime, long uptime, boolean on) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.addThermalOnOffEvent(eventType, elapsedRealtime, uptime, on);
        }
    }

    public void addThermalScreenBrightnessEvent(long elapsedRealtime, long uptime, int backlight, int delayTime) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.addThermalScreenBrightnessEvent(elapsedRealtime, uptime, backlight, delayTime);
        }
    }

    public void setScreenBrightness(int value) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.setScreenBrightness(value);
        }
    }

    public void addThermalNetState(long elapsedRealtime, long uptime, boolean netState) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.addThermalNetState(elapsedRealtime, uptime, netState);
        }
    }

    public void addThermalPhoneOnOff(long elapsedRealtime, long uptime, boolean onOff) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.addThermalPhoneOnOff(elapsedRealtime, uptime, onOff);
        }
    }

    public void addThermalPhoneSignal(long elapsedRealtime, long uptime, byte signal) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.addThermalPhoneSignal(elapsedRealtime, uptime, signal);
        }
    }

    public void addThermalPhoneState(long elapsedRealtime, long uptime, byte state) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.addThermalPhoneState(elapsedRealtime, uptime, state);
        }
    }

    public void notePhoneDataConnectionStateLocked(long elapsedTime, long upTime, int dataType) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.notePhoneDataConnectionStateLocked(elapsedTime, upTime, dataType);
        }
    }

    public void addThermalWifiStatus(long elapsedRealtime, long uptime, int status) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.addThermalWifiStatus(elapsedRealtime, uptime, status);
        }
    }

    public void addThermalWifiRssi(long elapsedRealtime, long uptime, int wifiSignalStrengthBin) {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.addThermalWifiRssi(elapsedRealtime, uptime, wifiSignalStrengthBin);
        }
    }

    public void writeThermalRecFile() {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.writeThermalRecFile();
        }
    }

    public void clearThermalStatsBuffer() {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper != null) {
            oppoThermalStatsHelper.clearThermalStatsBuffer();
        }
    }

    public int getScreenBrightness() {
        OppoThermalStatsHelper oppoThermalStatsHelper = this.mOppoThermalStatsHelper;
        if (oppoThermalStatsHelper == null) {
            return 0;
        }
        return oppoThermalStatsHelper.getScreenBrightness();
    }

    public boolean readThermalRecFile() {
        boolean needReadThermalRecFile;
        File file = this.mThermalRecFile;
        if (file == null || !file.exists()) {
            needReadThermalRecFile = false;
        } else {
            needReadThermalRecFile = true;
        }
        if (!needReadThermalRecFile) {
            return false;
        }
        BufferedReader reader = null;
        try {
            long time = System.currentTimeMillis();
            BufferedReader reader2 = new BufferedReader(new FileReader(this.mThermalRecFile));
            getThermalHistoryFromFile(reader2, null, null);
            reader2.close();
            reader = null;
            Slog.i(TAG, "readThermal history file lost time = " + (System.currentTimeMillis() - time));
            if (0 == 0) {
                return true;
            }
            try {
                reader.close();
                return true;
            } catch (Exception e) {
                return true;
            }
        } catch (Exception e2) {
            Slog.e("BatteryStats", "Error reading thermalFile statistics", e2);
            clearThermalStatsBuffer();
            if (reader == null) {
                return true;
            }
            reader.close();
            return true;
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e3) {
                }
            }
            throw th;
        }
    }

    public void dumpThemalLocked(PrintWriter pw, long histStart) {
        long historyTotalSize = (long) getHistoryTotalSize();
        long historyUsedSize = (long) getThermalHistoryUsedSize();
        if (startIteratingThermalHistoryLocked()) {
            try {
                pw.print("Thermal History (");
                pw.print((100 * historyUsedSize) / historyTotalSize);
                pw.print("% used, ");
                printSizeValueLocal(pw, historyUsedSize);
                pw.print(" used of ");
                printSizeValueLocal(pw, historyTotalSize);
                pw.println("):");
                dumpThermalHistoryLocked(pw, histStart);
                pw.println();
            } finally {
                finishIteratingThermalHistoryLocked();
            }
        }
    }

    private void dumpThemalRawLockedInner(PrintWriter pw, long histStart) {
        if (startIteratingThermalHistoryLocked()) {
            try {
                File thermalRecFile = new File("/data/system/thermalstats.bin");
                if (!thermalRecFile.exists()) {
                    pw.println("no raw file");
                    finishIteratingThermalHistoryLocked();
                    finishIteratingThermalHistoryLocked();
                    return;
                }
                BufferedReader reader = new BufferedReader(new FileReader(thermalRecFile));
                try {
                    getThermalRawHistoryFromFile(reader, pw);
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finishIteratingThermalHistoryLocked();
            } catch (Exception e2) {
                Slog.e("BatteryStats", "Error reading thermalFile statistics", e2);
            } catch (Throwable th) {
                finishIteratingThermalHistoryLocked();
                throw th;
            }
        }
    }

    public void dumpThemalRecLocked(Context context, PrintWriter pw, int flags, int reqUid, long histStart) {
        if (startIteratingThermalHistoryLocked()) {
            try {
                File thermalDir = new File("/data/system/thermal/dcs");
                if (!thermalDir.isDirectory() || !thermalDir.exists()) {
                    pw.println("no history file");
                    finishIteratingThermalHistoryLocked();
                    finishIteratingThermalHistoryLocked();
                    return;
                }
                File[] files = thermalDir.listFiles();
                if (files.length <= 0) {
                    pw.println("no history file");
                    finishIteratingThermalHistoryLocked();
                    finishIteratingThermalHistoryLocked();
                    return;
                }
                for (File file : files) {
                    OppoBaseBatteryStats.ThermalHistoryPrinter hprinter = new OppoBaseBatteryStats.ThermalHistoryPrinter();
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    try {
                        getThermalHistoryFromFile(reader, pw, hprinter);
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                finishIteratingThermalHistoryLocked();
            } catch (Exception e2) {
                Slog.e("BatteryStats", "Error reading thermalFile statistics", e2);
            } catch (Throwable th) {
                finishIteratingThermalHistoryLocked();
                throw th;
            }
        }
    }

    private void dumpThermalHistoryLocked(PrintWriter pw, long histStart) {
        OppoBaseBatteryStats.ThermalHistoryPrinter hprinter = new OppoBaseBatteryStats.ThermalHistoryPrinter();
        OppoBaseBatteryStats.ThermalItem rec = new OppoBaseBatteryStats.ThermalItem();
        while (getNextThermalHistoryLocked(rec, histStart)) {
            if (rec.elapsedRealtime >= histStart) {
                hprinter.printNextItem(pw, rec);
            }
        }
    }

    private void printSizeValueLocal(PrintWriter pw, long size) {
        float result = (float) size;
        String suffix = "";
        if (result >= 10240.0f) {
            suffix = "KB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "MB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "GB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "TB";
            result /= 1024.0f;
        }
        if (result >= 10240.0f) {
            suffix = "PB";
            result /= 1024.0f;
        }
        pw.print((int) result);
        pw.print(suffix);
    }
}
