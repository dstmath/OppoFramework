package com.android.internal.telephony;

import android.os.Message;
import com.android.internal.telephony.common.IOppoCommonFeature;
import com.android.internal.telephony.common.OppoFeatureList;
import com.android.internal.telephony.uicc.AdnRecord;

public interface IOppoAdnRecordCache extends IOppoCommonFeature {
    public static final IOppoAdnRecordCache DEFAULT = new IOppoAdnRecordCache() {
        /* class com.android.internal.telephony.IOppoAdnRecordCache.AnonymousClass1 */
    };
    public static final String TAG = "IOppoAdnRecordCache";

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoAdnRecordCache;
    }

    @Override // com.android.internal.telephony.common.IOppoCommonFeature
    default IOppoAdnRecordCache getDefault() {
        return DEFAULT;
    }

    default boolean hasCmdInProgress(int efid) {
        return false;
    }

    default int oppoUpdateAdnBySearch(int efid, AdnRecord oldAdn, AdnRecord newAdn, String pin2, Message response) {
        return -1;
    }

    default void oppoUpdateAdnByIndex(int efid, int extensionEF, AdnRecord adn, int recordIndex, String pin2, Message response) {
    }
}
