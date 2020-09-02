package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.text.format.Time;
import android.util.Log;
import com.android.server.pm.PackageManagerService;
import com.android.server.wifi.hotspot2.anqp.NAIRealmData;
import java.util.Iterator;
import java.util.LinkedList;

public class OppoHistoryRecordQueue {
    public static final String ACTION_UPDATE_ASSISTANT_INFO = "ACTION_UPDATE_ASSISTANT_INFO";
    public static final String ACTION_UPDATE_HISTORY_RECORD = "ACTION_UPDATE_HISTORY_RECORD";
    public static final String EXTRA_ASSISTANT_INFO = "EXTRA_ASSISTANT_INFO";
    public static final String EXTRA_HISTORY_RECORD_INFO = "EXTRA_HISTORY_RECORD_INFO";
    public static final int HISOTRY_LIMIT = 7;
    public static final String TAG = "OppoHistoryRecordQueue";
    public static final String TEST_APK_NAME = "com.example.a80082890.historyrecordhelper";
    public static boolean sDebug = false;
    private String mBssid;
    private Context mContext;
    private LinkedList<OppoHistoryRecord> mHistoryRecordQueue = new LinkedList<>();
    private boolean mIs5G;

    public OppoHistoryRecordQueue(Context context, String bssid, boolean is5G) {
        this.mContext = context;
        this.mBssid = bssid;
        this.mIs5G = is5G;
    }

    private void offer(OppoHistoryRecord historyRecord) {
        if (this.mHistoryRecordQueue.size() >= 7) {
            this.mHistoryRecordQueue.poll();
        }
        this.mHistoryRecordQueue.offer(historyRecord);
    }

    private OppoHistoryRecord getLast() {
        if (this.mHistoryRecordQueue.size() > 0) {
            return this.mHistoryRecordQueue.getLast();
        }
        return null;
    }

    private OppoHistoryRecord get(int index) {
        if (index < this.mHistoryRecordQueue.size()) {
            return this.mHistoryRecordQueue.get(index);
        }
        return null;
    }

    public void recordTxWithRssi(int rssi, int txgood, int txbad) {
        Time time = new Time();
        time.setToNow();
        int yearDay = time.yearDay;
        synchronized (this.mHistoryRecordQueue) {
            OppoHistoryRecord historyRecord = getLast();
            if (historyRecord != null) {
                if (historyRecord.mYearDay == yearDay) {
                    historyRecord.updateTxWithRssi(rssi, txgood, txbad);
                }
            }
            OppoHistoryRecord curHistoryRecord = new OppoHistoryRecord(yearDay);
            curHistoryRecord.updateTxWithRssi(rssi, txgood, txbad);
            offer(curHistoryRecord);
        }
        dumpState();
    }

    public int getTxBadWithRssi(int rssi) {
        int txBad;
        synchronized (this.mHistoryRecordQueue) {
            txBad = 0;
            Iterator<OppoHistoryRecord> it = this.mHistoryRecordQueue.iterator();
            while (it.hasNext()) {
                txBad += it.next().getTxBadWithRssi(rssi);
            }
        }
        return txBad;
    }

    public int getTxTotalWithRssi(int rssi) {
        int txTotal;
        synchronized (this.mHistoryRecordQueue) {
            txTotal = 0;
            Iterator<OppoHistoryRecord> it = this.mHistoryRecordQueue.iterator();
            while (it.hasNext()) {
                txTotal += it.next().getTxTotalWithRssi(rssi);
            }
        }
        return txTotal;
    }

    public double getLossWithRssi(int rssi) {
        int txTotal = 0;
        int txBad = 0;
        synchronized (this.mHistoryRecordQueue) {
            Iterator<OppoHistoryRecord> it = this.mHistoryRecordQueue.iterator();
            while (it.hasNext()) {
                OppoHistoryRecord historyRecord = it.next();
                txTotal += historyRecord.getTxTotalWithRssi(rssi);
                txBad += historyRecord.getTxBadWithRssi(rssi);
            }
        }
        if (txTotal > 0) {
            return ((double) txBad) / ((double) txTotal);
        }
        return 0.0d;
    }

    public int getRssiWithLossUnderThreshold(double lossThreshold) {
        int rssi = -126;
        double lastLoss = 0.0d;
        while (rssi < 0) {
            double loss = getLossWithRssi(rssi);
            if (loss < lossThreshold && lastLoss >= lossThreshold) {
                break;
            }
            lastLoss = loss;
            rssi += 3;
        }
        return rssi;
    }

    public String toStorageString() {
        StringBuilder sbuf = new StringBuilder();
        synchronized (this.mHistoryRecordQueue) {
            Iterator<OppoHistoryRecord> it = this.mHistoryRecordQueue.iterator();
            while (it.hasNext()) {
                sbuf.append(it.next().toStorageString() + NAIRealmData.NAI_REALM_STRING_SEPARATOR);
            }
        }
        return sbuf.toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00c6, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00db, code lost:
        r22 = r5;
        r5 = 0;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00c6 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:29:0x00be] */
    public void readStorageData(String historyData) {
        String[] recordList;
        int yearDay;
        Time time;
        int i;
        String[] recordList2;
        int txbad;
        Time time2 = new Time();
        time2.setToNow();
        int yearDay2 = time2.yearDay;
        String[] recordList3 = historyData.split(NAIRealmData.NAI_REALM_STRING_SEPARATOR);
        synchronized (this.mHistoryRecordQueue) {
            try {
                int length = recordList3.length;
                int i2 = 0;
                int i3 = 0;
                while (i3 < length) {
                    String record = recordList3[i3];
                    int recordYearday = 0;
                    int yearDayIndex = record.indexOf(":");
                    if (yearDayIndex > 0) {
                        try {
                            recordYearday = Integer.parseInt(record.substring(i2, yearDayIndex));
                        } catch (NumberFormatException e) {
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                        int yearDayDiff = yearDay2 - recordYearday;
                        log("record: " + record + " nowYearDay:" + yearDay2 + " recordYearDay:" + recordYearday);
                        if (yearDayDiff < 0 || yearDayDiff >= 7) {
                            time = time2;
                            yearDay = yearDay2;
                            recordList = recordList3;
                            i = i2;
                        } else if (this.mHistoryRecordQueue.size() < 7) {
                            OppoHistoryRecord curHistoryRecord = new OppoHistoryRecord(recordYearday);
                            String[] rssiDataList = record.substring(yearDayIndex + 1).split(",");
                            int length2 = rssiDataList.length;
                            time = time2;
                            int i4 = 0;
                            while (i4 < length2) {
                                try {
                                    String rssiData = rssiDataList[i4];
                                    try {
                                        String[] txData = rssiData.split("-");
                                        log("recordStorage:" + rssiData);
                                        if (txData.length == 3) {
                                            int rssi = 0;
                                            try {
                                                rssi = Integer.parseInt(txData[0]);
                                            } catch (NumberFormatException e2) {
                                            } catch (Throwable th2) {
                                            }
                                            recordList2 = recordList3;
                                            int txgood = Integer.parseInt(txData[1]);
                                            try {
                                                txbad = Integer.parseInt(txData[2]);
                                            } catch (NumberFormatException e3) {
                                                txbad = 0;
                                            }
                                            curHistoryRecord.updateTxWithRssi(0 - rssi, txgood, txbad);
                                        } else {
                                            recordList2 = recordList3;
                                        }
                                        i4++;
                                        yearDay2 = yearDay2;
                                        recordList3 = recordList2;
                                    } catch (Throwable th3) {
                                        th = th3;
                                        throw th;
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    throw th;
                                }
                            }
                            yearDay = yearDay2;
                            recordList = recordList3;
                            i = 0;
                            offer(curHistoryRecord);
                        } else {
                            time = time2;
                            yearDay = yearDay2;
                            recordList = recordList3;
                            i = i2;
                        }
                    } else {
                        time = time2;
                        yearDay = yearDay2;
                        recordList = recordList3;
                        i = i2;
                    }
                    i3++;
                    i2 = i;
                    time2 = time;
                    yearDay2 = yearDay;
                    recordList3 = recordList;
                }
            } catch (Throwable th5) {
                th = th5;
                throw th;
            }
        }
    }

    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            sDebug = true;
        } else {
            sDebug = false;
        }
    }

    private void log(String logout) {
        if (sDebug) {
            Log.d(TAG, logout);
        }
    }

    public void dumpState() {
        if (sDebug) {
            StringBuilder sbuf = new StringBuilder();
            int i = 1;
            sbuf.append("Bssid: " + this.mBssid + " is5G: " + this.mIs5G);
            sbuf.append("\n");
            Iterator<OppoHistoryRecord> it = this.mHistoryRecordQueue.iterator();
            while (it.hasNext()) {
                OppoHistoryRecord historyRecord = it.next();
                sbuf.append("Day" + i + ":");
                sbuf.append(historyRecord.toString());
                sbuf.append("\n");
                log("AP: " + this.mBssid + " is5G: " + this.mIs5G + " yearDay" + historyRecord.mYearDay + ": " + historyRecord);
                i++;
            }
            broadcastInfotoTestapk(sbuf.toString(), ACTION_UPDATE_HISTORY_RECORD);
        }
    }

    public void broadcastInfotoTestapk(String info, String type) {
        PackageManagerService pm = ServiceManager.getService("package");
        if (pm == null || pm.getPackageUid(TEST_APK_NAME, 65536, 0) >= 1000) {
            Intent broadIntent = null;
            if (ACTION_UPDATE_HISTORY_RECORD.equals(type)) {
                broadIntent = new Intent(ACTION_UPDATE_HISTORY_RECORD);
                log("broadcast = " + info);
                broadIntent.putExtra(EXTRA_HISTORY_RECORD_INFO, info);
            } else if (ACTION_UPDATE_ASSISTANT_INFO.equals(type)) {
                broadIntent = new Intent(ACTION_UPDATE_ASSISTANT_INFO);
                log("broadcast = " + info);
                broadIntent.putExtra(EXTRA_ASSISTANT_INFO, info);
            }
            if (broadIntent != null) {
                this.mContext.sendStickyBroadcastAsUser(broadIntent, UserHandle.ALL);
            }
        }
    }
}
