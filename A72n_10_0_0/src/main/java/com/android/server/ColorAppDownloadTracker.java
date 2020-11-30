package com.android.server;

import android.app.ActivityManager;
import android.content.Context;
import android.net.TrafficStats;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Pair;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ColorAppDownloadTracker extends ColorAppActionTracker {
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final int DOWNLOAD_SIZE_PER_PACKAGE = 500;
    private static final int DOWNLOAD_SPEED_PER_SECOND = 8192;
    private static final int MAX_CACHE_RECORD_COUNT = 10;
    private static final String TAG = "ColorAppDownloadTracker";
    private static final int WEIGHT_DOWNLOAD_MARKED = 3;
    private static final int WEIGHT_DOWNLOAD_MAX = 4;
    private static final int WEIGHT_DOWNLOAD_PREPARED = 1;
    private static final int WEIGHT_DOWNLOAD_STOP = 0;
    private static volatile ColorAppDownloadTracker mInstance = null;
    private HashSet<Integer> mDownloadApps = new HashSet<>();
    List<Integer> mDownloadFromScreenOn = new ArrayList();
    private ArrayMap<Integer, Integer> mDownloadUploadUidWeight = new ArrayMap<>();
    TrafficMonitor mMonitor = null;
    private long mTrafficUpdateTime = 0;
    private Runnable mTrafficUpdater = new Runnable() {
        /* class com.android.server.ColorAppDownloadTracker.AnonymousClass1 */

        public void run() {
            ColorAppDownloadTracker.this.mMonitor.updateTraffic();
            ColorAppDownloadTracker.this.mDownloadApps.clear();
            ColorAppDownloadTracker.this.mTrafficUpdateTime = SystemClock.elapsedRealtime();
            if (ColorAppDownloadTracker.this.updateDownloadUploadList() && ColorAppDownloadTracker.this.mCallback != null) {
                ColorAppDownloadTracker.this.mCallback.updateAppActionChange();
            }
            if (ColorAppDownloadTracker.this.mStart) {
                ColorAppDownloadTracker.this.mHandler.postDelayed(this, 60000);
            }
        }
    };

    public static ColorAppDownloadTracker getInstance(Context context) {
        if (mInstance == null) {
            synchronized (ColorAppDownloadTracker.class) {
                if (mInstance == null) {
                    mInstance = new ColorAppDownloadTracker(context);
                }
            }
        }
        return mInstance;
    }

    public ColorAppDownloadTracker(Context context) {
        super(context);
    }

    public ArrayMap<Integer, Integer> getTrackWhiteList(boolean startFromScreenOn) {
        ArrayMap<Integer, Integer> result = new ArrayMap<>();
        synchronized (this.mDownloadUploadUidWeight) {
            for (int i = 0; i < this.mDownloadUploadUidWeight.size(); i++) {
                int weight = this.mDownloadUploadUidWeight.valueAt(i).intValue();
                int uid = this.mDownloadUploadUidWeight.keyAt(i).intValue();
                if (DEBUG) {
                    Slog.d(TAG, "getTrackWhiteList uid = " + uid + ", weight = " + weight);
                }
                if (weight >= 3) {
                    if (!startFromScreenOn || (startFromScreenOn && this.mDownloadFromScreenOn.contains(Integer.valueOf(uid)))) {
                        result.put(Integer.valueOf(uid), 1);
                    }
                } else if (weight >= 1 && (!startFromScreenOn || (startFromScreenOn && this.mDownloadFromScreenOn.contains(Integer.valueOf(uid))))) {
                    result.put(Integer.valueOf(uid), 0);
                }
            }
        }
        return result;
    }

    public List<Integer> getTrafficList() {
        List<Integer> list = new ArrayList<>();
        synchronized (this.mDownloadUploadUidWeight) {
            for (int i = 0; i < this.mDownloadUploadUidWeight.size(); i++) {
                int weight = this.mDownloadUploadUidWeight.valueAt(i).intValue();
                int uid = this.mDownloadUploadUidWeight.keyAt(i).intValue();
                if (DEBUG) {
                    Slog.d(TAG, "getTrafficList uid = " + uid + ", weight = " + weight);
                }
                if (weight >= 3) {
                    list.add(Integer.valueOf(uid));
                }
            }
        }
        return list;
    }

    public boolean isDownloadingApp(int uid) {
        synchronized (this.mDownloadApps) {
            if (this.mDownloadApps.contains(Integer.valueOf(uid))) {
                return true;
            }
            return DEBUG;
        }
    }

    public long getTrafficUpdateTime() {
        return this.mTrafficUpdateTime;
    }

    public void handleScreenOn() {
        this.mDownloadFromScreenOn.clear();
        if (DEBUG) {
            Slog.d(TAG, "handleScreenOn");
        }
    }

    public void handleScreenOff() {
        if (DEBUG) {
            Slog.d(TAG, "handleScreenOff");
        }
    }

    public void onStart() {
        if (DEBUG) {
            Slog.d(TAG, "app download tracker start");
        }
        this.mMonitor = new TrafficMonitor();
        this.mHandler.post(this.mTrafficUpdater);
    }

    public void onStop() {
        if (DEBUG) {
            Slog.d(TAG, "app download tracker stop");
        }
        this.mHandler.removeCallbacks(this.mTrafficUpdater);
        synchronized (this.mDownloadUploadUidWeight) {
            this.mDownloadUploadUidWeight.clear();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x008e  */
    private boolean updateDownloadUploadList() {
        boolean recordBetweenScreenOff;
        int i;
        int recordSize;
        int weight;
        boolean recordBetweenScreenOff2;
        Throwable th;
        int recordSize2 = this.mMonitor.mTrafficRecord.size();
        if (recordSize2 < 2) {
            if (DEBUG) {
                Slog.d(TAG, "record size less than 2, wait for a minute");
            }
            return DEBUG;
        }
        TrafficRecord nowRecord = this.mMonitor.mTrafficRecord.get(recordSize2 - 1);
        TrafficRecord preRecord = this.mMonitor.mTrafficRecord.get(recordSize2 - 2);
        ArrayMap<Integer, Pair<Long, Long>> diffs = nowRecord.calcTrafficRecordDiff(preRecord);
        long interval = nowRecord.timeStamp - preRecord.timeStamp;
        boolean screenOff = this.mLastScreenOffTime > this.mLastScreenOnTime;
        if (screenOff && this.mLastScreenOffTime - preRecord.timeStamp >= 0) {
            if (nowRecord.timeStamp - this.mLastScreenOffTime >= 0) {
                recordBetweenScreenOff = true;
                if (DEBUG) {
                    Slog.d(TAG, "screenOff = " + screenOff + ", recordBetweenScreenOff = " + recordBetweenScreenOff);
                }
                boolean notify = DEBUG;
                i = 0;
                while (i < diffs.size()) {
                    int uid = diffs.keyAt(i).intValue();
                    boolean download = isAppDownloadBaseOnDiff(diffs.valueAt(i), interval);
                    if (DEBUG && download) {
                        Slog.d(TAG, "uid = " + uid + " is download " + download);
                    }
                    if (download) {
                        synchronized (this.mDownloadApps) {
                            try {
                                recordSize = recordSize2;
                                this.mDownloadApps.add(Integer.valueOf(uid));
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                    } else {
                        recordSize = recordSize2;
                    }
                    if (download && (!screenOff || recordBetweenScreenOff)) {
                        this.mDownloadFromScreenOn.add(Integer.valueOf(uid));
                    }
                    int weight2 = 0;
                    Integer weightValue = this.mDownloadUploadUidWeight.get(Integer.valueOf(uid));
                    if (weightValue != null) {
                        weight2 = weightValue.intValue();
                    }
                    if (download) {
                        weight = weight2 + 1;
                    } else {
                        weight = weight2 - 1;
                    }
                    if (download || screenOff || weight2 - weight <= 0) {
                        recordBetweenScreenOff2 = recordBetweenScreenOff;
                    } else {
                        recordBetweenScreenOff2 = recordBetweenScreenOff;
                        if (this.mDownloadFromScreenOn.contains(Integer.valueOf(uid))) {
                            this.mDownloadFromScreenOn.remove(Integer.valueOf(uid));
                        }
                    }
                    int weight3 = Math.min(4, Math.max(weight, 0));
                    this.mDownloadUploadUidWeight.put(Integer.valueOf(uid), Integer.valueOf(weight3));
                    if ((weight2 >= 3 ? true : DEBUG) != (weight3 >= 3 ? true : DEBUG)) {
                        notify = true;
                    }
                    i++;
                    preRecord = preRecord;
                    recordSize2 = recordSize;
                    recordBetweenScreenOff = recordBetweenScreenOff2;
                }
                return notify;
            }
        }
        recordBetweenScreenOff = DEBUG;
        if (DEBUG) {
        }
        boolean notify2 = DEBUG;
        i = 0;
        while (i < diffs.size()) {
        }
        return notify2;
    }

    private boolean isAppDownloadBaseOnDiff(Pair<Long, Long> stats, long interval) {
        if (interval <= 0 || (((Long) stats.first).longValue() * 1000) / interval <= 8192 || ((Long) stats.second).longValue() <= 0 || ((int) (((Long) stats.first).longValue() / ((Long) stats.second).longValue())) <= DOWNLOAD_SIZE_PER_PACKAGE) {
            return DEBUG;
        }
        return true;
    }

    private void reset() {
        this.mDownloadUploadUidWeight.clear();
        this.mMonitor.mTrafficRecord.clear();
        this.mDownloadFromScreenOn.clear();
    }

    public void startTrafficUpdater(long delay) {
        if (delay > 0) {
            this.mHandler.postDelayed(this.mTrafficUpdater, delay);
        } else {
            this.mHandler.post(this.mTrafficUpdater);
        }
    }

    /* access modifiers changed from: private */
    public class TrafficMonitor {
        List<TrafficRecord> mTrafficRecord;

        private TrafficMonitor() {
            this.mTrafficRecord = new ArrayList();
        }

        public void updateTraffic() {
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = ((ActivityManager) ColorAppDownloadTracker.this.mContext.getSystemService("activity")).getRunningAppProcesses();
            TrafficRecord record = new TrafficRecord(SystemClock.elapsedRealtime());
            record.updateWholeTraffic(TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes(), TrafficStats.getTotalRxPackets() + TrafficStats.getTotalTxPackets());
            ArrayList<Integer> dealedUids = new ArrayList<>();
            for (ActivityManager.RunningAppProcessInfo info : runningAppProcessInfos) {
                if (info.uid >= 10000 && !dealedUids.contains(Integer.valueOf(info.uid))) {
                    dealedUids.add(Integer.valueOf(info.uid));
                    record.updateUidTraffic(UserHandle.getAppId(info.uid), getUidTrafficByte(info.uid), getUidTrafficPackage(info.uid));
                }
            }
            this.mTrafficRecord.add(record);
            if (this.mTrafficRecord.size() >= 10) {
                this.mTrafficRecord.remove(0);
            }
        }

        private long getUidTrafficByte(int uid) {
            return TrafficStats.getUidTxBytes(uid) + TrafficStats.getUidRxBytes(uid);
        }

        private long getUidTrafficPackage(int uid) {
            return TrafficStats.getUidTxPackets(uid) + TrafficStats.getUidRxPackets(uid);
        }
    }

    /* access modifiers changed from: private */
    public class TrafficRecord {
        long timeStamp = 0;
        ArrayMap<Integer, Pair<Long, Long>> trafficStats = null;
        long wholeTrafficBytes = 0;
        long wholeTrafficPackages = 0;

        public TrafficRecord(long timeStamp2) {
            this.timeStamp = timeStamp2;
            this.trafficStats = new ArrayMap<>();
        }

        public void updateWholeTraffic(long trafficBytes, long trafficPackages) {
            this.wholeTrafficBytes = trafficBytes;
            this.wholeTrafficPackages = trafficPackages;
        }

        public void updateUidTraffic(int uid, long rtBytes, long rtPackages) {
            Pair<Long, Long> rawValue = this.trafficStats.get(Integer.valueOf(uid));
            long preRTBytes = 0;
            long preRTPackages = 0;
            if (rawValue != null) {
                preRTBytes = ((Long) rawValue.first).longValue();
                preRTPackages = ((Long) rawValue.second).longValue();
            }
            this.trafficStats.put(Integer.valueOf(uid), Pair.create(Long.valueOf(preRTBytes + rtBytes), Long.valueOf(preRTPackages + rtPackages)));
        }

        public ArrayMap<Integer, Pair<Long, Long>> calcTrafficRecordDiff(TrafficRecord preRecord) {
            long subRTBytes;
            long subRTPackages;
            boolean preNull = (preRecord == null || preRecord.trafficStats.size() == 0) ? true : ColorAppDownloadTracker.DEBUG;
            ArrayMap<Integer, Pair<Long, Long>> result = new ArrayMap<>();
            for (int i = 0; i < this.trafficStats.size(); i++) {
                int uid = this.trafficStats.keyAt(i).intValue();
                Pair<Long, Long> nowValue = this.trafficStats.valueAt(i);
                Pair<Long, Long> preValue = preNull ? null : preRecord.trafficStats.get(Integer.valueOf(uid));
                if (preValue != null) {
                    subRTBytes = ((Long) nowValue.first).longValue() - ((Long) preValue.first).longValue();
                    subRTPackages = ((Long) nowValue.second).longValue() - ((Long) preValue.second).longValue();
                } else {
                    subRTBytes = ((Long) nowValue.first).longValue();
                    subRTPackages = ((Long) nowValue.second).longValue();
                }
                result.put(Integer.valueOf(uid), Pair.create(Long.valueOf(subRTBytes), Long.valueOf(subRTPackages)));
            }
            return result;
        }
    }
}
