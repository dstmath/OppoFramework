package android.view;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.res.Configuration;
import com.color.util.ColorAccidentallyTouchData;

public interface IColorAccidentallyTouchHelper extends IOppoCommonFeature {
    public static final IColorAccidentallyTouchHelper DEFAULT = new IColorAccidentallyTouchHelper() {
        /* class android.view.IColorAccidentallyTouchHelper.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorAccidentallyTouchHelper getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAccidentallyTouchHelper;
    }

    default void initOnAmsReady() {
    }

    default ColorAccidentallyTouchData getAccidentallyTouchData() {
        return new ColorAccidentallyTouchData();
    }

    default MotionEvent updatePointerEvent(MotionEvent event, View mView, Configuration mLastConfiguration) {
        return event;
    }

    default void updataeAccidentPreventionState(Context context, boolean enable, int rotation) {
    }

    default void initData(Context context) {
    }

    default boolean getEdgeEnable() {
        return false;
    }

    default int getEdgeT1() {
        return 10;
    }

    default int getEdgeT2() {
        return 30;
    }

    default int getOriEdgeT1() {
        return 10;
    }
}
