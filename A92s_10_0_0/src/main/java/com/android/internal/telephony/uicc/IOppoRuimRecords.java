package com.android.internal.telephony.uicc;

import android.os.Message;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;

public interface IOppoRuimRecords extends IOppoCommonFeature {
    public static final IOppoRuimRecords DEFAULT = new IOppoRuimRecords() {
        /* class com.android.internal.telephony.uicc.IOppoRuimRecords.AnonymousClass1 */
    };
    public static final String TAG = "IOppoRuimRecords";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoRuimRecords;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoRuimRecords getDefault() {
        return DEFAULT;
    }

    default void onEfCsimImsimRecordLoaded(String mImsi) {
    }

    default void handleMessage(Message msg) {
    }

    default void onAllRecordsLoaded() {
    }

    default void dispose() {
    }
}
