package android.widget;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.RecordingCanvas;

public interface IColorMagnifierHooks extends IOppoCommonFeature {
    public static final IColorMagnifierHooks DEFAULT = new IColorMagnifierHooks() {
        /* class android.widget.IColorMagnifierHooks.AnonymousClass1 */
    };

    @Override // android.common.IOppoCommonFeature
    default IColorMagnifierHooks getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorMagnifierHooks;
    }

    default int getMagnifierWidth(TypedArray a, Context context) {
        return a.getDimensionPixelSize(5, 0);
    }

    default int getMagnifierHeight(TypedArray a, Context context) {
        return a.getDimensionPixelSize(2, 0);
    }

    default float getMagnifierCornerRadius(TypedArray a, Context context) {
        return a.getDimension(0, 0.0f);
    }

    default void decodeShadowBitmap(Context context) {
    }

    default void recycleShadowBitmap() {
    }

    default void drawShadowBitmap(int contentWidth, int contentHeight, RecordingCanvas canvas, Paint paint) {
    }
}
