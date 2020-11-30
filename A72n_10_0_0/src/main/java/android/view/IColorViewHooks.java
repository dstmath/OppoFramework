package android.view;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import com.color.screenshot.ColorLongshotViewInfo;
import com.color.screenshot.IColorLongshotController;
import com.color.view.ColorScrollBarEffect;
import com.color.view.IColorScrollBarEffect;

public interface IColorViewHooks extends IColorScrollBarEffect.ViewCallback, IOppoCommonFeature {
    public static final IColorViewHooks DEFAULT = new IColorViewHooks() {
        /* class android.view.IColorViewHooks.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorViewHooks getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorViewHooks;
    }

    @Override // com.color.view.IColorScrollBarEffect.ViewCallback
    default boolean awakenScrollBars() {
        return false;
    }

    @Override // com.color.view.IColorScrollBarEffect.ViewCallback
    default boolean isLayoutRtl() {
        return false;
    }

    default boolean isLongshotConnected() {
        return false;
    }

    default boolean isColorStyle() {
        return false;
    }

    default boolean isOppoStyle() {
        return false;
    }

    default void performClick() {
    }

    default int getOverScrollMode(int overScrollMode) {
        return overScrollMode;
    }

    default IColorScrollBarEffect getScrollBarEffect() {
        return ColorScrollBarEffect.NO_EFFECT;
    }

    default boolean findViewsLongshotInfo(ColorLongshotViewInfo info) {
        return false;
    }

    default IColorLongshotController getLongshotController() {
        return null;
    }

    default boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent, int oldScrollY, boolean result) {
        return false;
    }

    default IColorViewConfigHelper getColorViewConfigHelper(Context context) {
        return IColorViewConfigHelper.DEFAULT;
    }
}
