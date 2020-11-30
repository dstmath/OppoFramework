package com.android.server.wifi;

import java.text.DecimalFormat;
import java.util.HashMap;

public class OppoHistoryRecord {
    private static final String BSSID_STR = "bssid=";
    private static final String HISTORY_TX = "historyTx=";
    public static final int INVALID_RSSI = -127;
    public static final int RSSI_INTERVAL = 3;
    public static final int RSSI_INTERVAL_COUNT = 43;
    public static final int RSSI_MAX = 0;
    public static final int RSSI_MIN = -127;
    public static final String TAG = "OppoHistoryRecord";
    public HashMap<Integer, Integer> mHistoryTxBad = new HashMap<>();
    public HashMap<Integer, Integer> mHistoryTxGood = new HashMap<>();
    public int mYearDay;

    public OppoHistoryRecord(int yearDay) {
        this.mYearDay = yearDay;
    }

    public void updateTxWithRssi(int rssi, int txgood, int txbad) {
        if (!(txgood == 0 && txbad == 0) && txgood >= 0 && txbad >= 0) {
            int index = getIndexWithRssi(rssi);
            if (checkIndex(index)) {
                double d = ((double) txbad) / ((double) (txbad + txgood));
                new DecimalFormat("#.##");
                Integer historyTxGood = this.mHistoryTxGood.get(Integer.valueOf(index));
                Integer historyTxBad = this.mHistoryTxBad.get(Integer.valueOf(index));
                if (historyTxGood == null || historyTxBad == null) {
                    this.mHistoryTxGood.put(Integer.valueOf(index), Integer.valueOf(txgood));
                    this.mHistoryTxBad.put(Integer.valueOf(index), Integer.valueOf(txbad));
                    return;
                }
                Integer historyTxGood2 = Integer.valueOf(historyTxGood.intValue() + txgood);
                Integer historyTxBad2 = Integer.valueOf(historyTxBad.intValue() + txbad);
                this.mHistoryTxGood.put(Integer.valueOf(index), historyTxGood2);
                this.mHistoryTxBad.put(Integer.valueOf(index), historyTxBad2);
            }
        }
    }

    public int getTxBadWithRssi(int rssi) {
        Integer historyTxBad = this.mHistoryTxBad.get(Integer.valueOf(getIndexWithRssi(rssi)));
        if (historyTxBad != null) {
            return historyTxBad.intValue();
        }
        return 0;
    }

    public int getTxGoodWithRssi(int rssi) {
        Integer historyTxGood = this.mHistoryTxGood.get(Integer.valueOf(getIndexWithRssi(rssi)));
        if (historyTxGood != null) {
            return historyTxGood.intValue();
        }
        return 0;
    }

    public int getTxTotalWithRssi(int rssi) {
        return getTxGoodWithRssi(rssi) + getTxBadWithRssi(rssi);
    }

    public int getIndexWithRssi(int rssi) {
        if (rssi >= 0 || rssi <= -127) {
            return -1;
        }
        int index = (-rssi) / 3;
        if (!checkIndex(index)) {
            return -1;
        }
        return index;
    }

    public boolean checkIndex(int index) {
        if (index < 0 || index >= 43) {
            return false;
        }
        return true;
    }

    public String toString() {
        DecimalFormat df = new DecimalFormat("#.##");
        StringBuilder sbuf = new StringBuilder();
        for (int i = 0; i < 43; i++) {
            if (this.mHistoryTxGood.get(Integer.valueOf(i)) != null) {
                int txgood = this.mHistoryTxGood.get(Integer.valueOf(i)).intValue();
                int txbad = this.mHistoryTxBad.get(Integer.valueOf(i)).intValue();
                sbuf.append("[" + (i * 3) + ":" + txgood + ",");
                StringBuilder sb = new StringBuilder();
                sb.append(txbad);
                sb.append(",");
                sb.append(df.format(100.0d * (((double) txbad) / ((double) (txgood + txbad)))));
                sb.append("]");
                sbuf.append(sb.toString());
            }
        }
        return sbuf.toString();
    }

    public String toStorageString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(this.mYearDay + ":");
        for (int i = 0; i < 43; i++) {
            if (this.mHistoryTxGood.get(Integer.valueOf(i)) != null) {
                int txgood = this.mHistoryTxGood.get(Integer.valueOf(i)).intValue();
                int txbad = this.mHistoryTxBad.get(Integer.valueOf(i)).intValue();
                sbuf.append((i * 3) + "-" + txgood + "-" + txbad + ",");
            }
        }
        return sbuf.toString();
    }
}
