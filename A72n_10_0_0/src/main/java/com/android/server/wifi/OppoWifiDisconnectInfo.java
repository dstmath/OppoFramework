package com.android.server.wifi;

import android.util.Log;
import com.android.server.wifi.scanner.OppoWiFiScanBlockPolicy;

public class OppoWifiDisconnectInfo {
    private static boolean DBG = false;
    public static int REASONCODE_BMISS_BASE = 1000;
    public static int REASONCODE_UPLAYER_BASE = OppoWiFiScanBlockPolicy.BACKGROUND_SCAN_RESULTS_INTERVAL;
    private static final String TAG = "OppoWifiDisconnectInfo";
    public int mChannel;
    public int mDataRate;
    public int mLen = -1;
    public int mReasonCode = -1;
    public int mReasonType = -1;
    public int mRssi = -127;

    public void init() {
        this.mReasonType = -1;
        this.mReasonCode = -1;
        this.mRssi = -127;
        this.mDataRate = 0;
        this.mChannel = 0;
    }

    public void parseData(String data) {
        int n = data.length() / 2;
        if (n >= 11) {
            int[] anArray = new int[n];
            int j = 0;
            for (int i = 0; i < n; i++) {
                anArray[i] = Integer.parseInt(data.substring(j, j + 2), 16);
                j += 2;
            }
            this.mLen = (anArray[3] * 256) + anArray[2];
            this.mReasonType = anArray[4];
            this.mReasonCode = (anArray[6] * 256) + anArray[5];
            int i2 = this.mReasonType;
            if (i2 == 0) {
                this.mReasonCode += REASONCODE_BMISS_BASE;
            } else if (i2 == 2) {
                this.mReasonCode += REASONCODE_UPLAYER_BASE;
            }
            this.mRssi = anArray[7] - 256;
            this.mDataRate = (anArray[9] * 256) + anArray[8];
            this.mChannel = anArray[10];
            return;
        }
        Log.d(TAG, "parseData: wrong data array length " + n);
    }
}
