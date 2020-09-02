package android.common;

import android.common.OppoFeatureList;

public interface IOppoCommonFeature {
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.End;
    }

    default IOppoCommonFeature getDefault() {
        return null;
    }
}
