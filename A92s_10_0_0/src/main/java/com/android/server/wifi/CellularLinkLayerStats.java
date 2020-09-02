package com.android.server.wifi;

import com.android.server.wifi.ScoringParams;

public class CellularLinkLayerStats {
    private int mDataNetworkType = 0;
    private boolean mIsSameRegisteredCell = false;
    private int mSignalStrengthDb = ScoringParams.Values.MAX_EXPID;
    private int mSignalStrengthDbm = ScoringParams.Values.MAX_EXPID;

    public void setDataNetworkType(int dataNetworkType) {
        this.mDataNetworkType = dataNetworkType;
    }

    public void setSignalStrengthDbm(int signalStrengthDbm) {
        this.mSignalStrengthDbm = signalStrengthDbm;
    }

    public void setIsSameRegisteredCell(boolean isSameRegisteredCell) {
        this.mIsSameRegisteredCell = isSameRegisteredCell;
    }

    public void setSignalStrengthDb(int signalStrengthDb) {
        this.mSignalStrengthDb = signalStrengthDb;
    }

    public int getDataNetworkType() {
        return this.mDataNetworkType;
    }

    public boolean getIsSameRegisteredCell() {
        return this.mIsSameRegisteredCell;
    }

    public int getSignalStrengthDb() {
        return this.mSignalStrengthDb;
    }

    public int getSignalStrengthDbm() {
        return this.mSignalStrengthDbm;
    }

    public String toString() {
        return " CellularLinkLayerStats: " + 10 + " Data Network Type: " + this.mDataNetworkType + 10 + " Signal Strength in dBm: " + this.mSignalStrengthDbm + 10 + " Signal Strength in dB: " + this.mSignalStrengthDb + 10 + " Is it the same registered cell? " + this.mIsSameRegisteredCell + 10;
    }
}
