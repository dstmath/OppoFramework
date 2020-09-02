package android.widget;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorListHooks extends IOppoCommonFeature {
    public static final IColorListHooks DEFAULT = new IColorListHooks() {
        /* class android.widget.IColorListHooks.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorListHooks getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorListHooks;
    }

    default FastScroller getFastScroller(AbsListView absListView, int style) {
        return new FastScroller(absListView, style);
    }
}
