package android.widget;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorOverScrollerHelper extends IOppoCommonFeature {
    public static final IColorOverScrollerHelper DEFAULT = new IColorOverScrollerHelper() {
        /* class android.widget.IColorOverScrollerHelper.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorOverScrollerHelper getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorOverScrollerHelper;
    }

    default int getFinalX(int x) {
        return x;
    }

    default int getFinalY(int y) {
        return y;
    }

    default boolean setFriction(float friction) {
        return false;
    }

    default boolean isFinished(boolean finished) {
        return finished;
    }

    default int getCurrX(int x) {
        return x;
    }

    default int getCurrY(int y) {
        return y;
    }
}
