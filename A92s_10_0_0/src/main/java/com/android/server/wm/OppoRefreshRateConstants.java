package com.android.server.wm;

import java.util.Arrays;

class OppoRefreshRateConstants {
    static boolean DEBUG = false;
    static final int MAX_REFRESH_RATE_ID = 3;
    static final int MIN_REFRESH_RATE_ID = 1;
    static final String OPPO_SCREENMODE = "opposcreenmode";
    static final int REFRESH_RATE_120 = 3;
    static final int REFRESH_RATE_60 = 2;
    static final int REFRESH_RATE_90 = 1;
    static final int SETTING_MODE_120 = 3;
    static final int SETTING_MODE_60 = 2;
    static final int SETTING_MODE_90 = 1;
    static final int SETTING_MODE_AUTO = 0;
    static final String TAG = "RefreshRate";

    OppoRefreshRateConstants() {
    }

    /* access modifiers changed from: package-private */
    public static class PreferredRefreshRateData {
        int[] mPreferredRefreshRateId = {0, 0, 0, 0};

        PreferredRefreshRateData() {
        }

        /* access modifiers changed from: package-private */
        public void putPreferredRefreshRateId(int settingMode, int rateId) {
            checkRange(settingMode);
            this.mPreferredRefreshRateId[settingMode] = rateId;
        }

        /* access modifiers changed from: package-private */
        public int getPreferredRefreshRateId(int settingMode) {
            checkRange(settingMode);
            return this.mPreferredRefreshRateId[settingMode];
        }

        public String toString() {
            return "PreferredRefreshRateData{mPreferredRefreshRateId=" + Arrays.toString(this.mPreferredRefreshRateId) + '}';
        }

        private void checkRange(int index) {
            if (index < 0 || index >= this.mPreferredRefreshRateId.length) {
                throw new IllegalArgumentException("length 4, index:" + index);
            }
        }
    }
}
