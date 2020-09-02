package com.android.server.wm;

import com.android.server.wm.OppoRefreshRateConstants;

class OppoRefreshRateData {
    private int mOverrideRefreshRateId = 0;
    private OppoRefreshRateConstants.PreferredRefreshRateData mPreferredRefreshRateId;

    OppoRefreshRateData() {
    }

    /* access modifiers changed from: package-private */
    public void init(OppoRefreshRateConstants.PreferredRefreshRateData preferredId) {
        this.mPreferredRefreshRateId = preferredId;
    }

    /* access modifiers changed from: package-private */
    public void overrideRefreshRateId(int id) {
        this.mOverrideRefreshRateId = id;
    }

    /* access modifiers changed from: package-private */
    public int getOverrideRefreshRateId() {
        return this.mOverrideRefreshRateId;
    }

    /* access modifiers changed from: package-private */
    public void clearOverrideId() {
        this.mOverrideRefreshRateId = 0;
    }

    /* access modifiers changed from: package-private */
    public int getPreferredRefreshRateId(int settingMode) {
        int i = this.mOverrideRefreshRateId;
        if (i > 0) {
            return i;
        }
        OppoRefreshRateConstants.PreferredRefreshRateData preferredRefreshRateData = this.mPreferredRefreshRateId;
        if (preferredRefreshRateData != null) {
            return preferredRefreshRateData.getPreferredRefreshRateId(settingMode);
        }
        return 0;
    }
}
