package android.view;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import java.lang.ref.WeakReference;

public interface IColorViewRootUtil extends IOppoCommonFeature {
    public static final IColorViewRootUtil DEFAULT = new IColorViewRootUtil() {
        /* class android.view.IColorViewRootUtil.AnonymousClass1 */
    };
    public static final String NAME = "ColorViewRootUtil";

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorViewRootUtil;
    }

    @Override // android.common.IOppoCommonFeature
    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void initSwipState(Display display, Context context) {
    }

    default void initSwipState(Display display, Context context, boolean isDisplayCompatApp) {
    }

    default boolean needScale(int noncompatDensity, int density, Display display) {
        return false;
    }

    default boolean swipeFromBottom(MotionEvent event, int noncompatDensity, int density, Display display) {
        return false;
    }

    default float getCompactScale() {
        return 1.0f;
    }

    default int getScreenHeight() {
        return 1;
    }

    default int getScreenWidth() {
        return 1;
    }

    default void checkGestureConfig(Context context) {
    }

    default DisplayInfo getDisplayInfo() {
        return null;
    }

    default IColorLongshotViewHelper getColorLongshotViewHelper(WeakReference<ViewRootImpl> weakReference) {
        return null;
    }
}
