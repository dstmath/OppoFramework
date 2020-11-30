package android.view;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorViewConfigHelper extends IOppoCommonFeature {
    public static final IColorViewConfigHelper DEFAULT = new IColorViewConfigHelper() {
        /* class android.view.IColorViewConfigHelper.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorViewConfigHelper getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorViewConfigHelper;
    }

    default int getScaledOverscrollDistance(int dist) {
        return dist;
    }

    default int getScaledOverflingDistance(int dist) {
        return dist;
    }

    default int calcRealOverScrollDist(int dist, int scrollY) {
        return dist;
    }

    default int calcRealOverScrollDist(int dist, int scrollY, int range) {
        return dist;
    }
}
