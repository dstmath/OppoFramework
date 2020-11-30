package com.android.internal.telephony.common;

import com.android.internal.telephony.common.OppoFeatureList;

public interface IOppoCommonFeature {
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.End;
    }

    default IOppoCommonFeature getDefault() {
        return null;
    }
}
