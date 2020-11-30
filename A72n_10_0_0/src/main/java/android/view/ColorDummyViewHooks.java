package android.view;

import android.content.Context;
import com.color.screenshot.ColorLongshotViewInfo;
import com.color.screenshot.IColorLongshotController;
import com.color.view.ColorScrollBarEffect;
import com.color.view.IColorScrollBarEffect;

public class ColorDummyViewHooks implements IColorViewHooks {
    @Override // com.color.view.IColorScrollBarEffect.ViewCallback, android.view.IColorViewHooks
    public boolean awakenScrollBars() {
        return false;
    }

    @Override // com.color.view.IColorScrollBarEffect.ViewCallback, android.view.IColorViewHooks
    public boolean isLayoutRtl() {
        return false;
    }

    @Override // android.view.IColorViewHooks
    public boolean isLongshotConnected() {
        return false;
    }

    @Override // android.view.IColorViewHooks
    public boolean isColorStyle() {
        return false;
    }

    @Override // android.view.IColorViewHooks
    public boolean isOppoStyle() {
        return false;
    }

    @Override // android.view.IColorViewHooks
    public void performClick() {
    }

    @Override // android.view.IColorViewHooks
    public int getOverScrollMode(int overScrollMode) {
        return overScrollMode;
    }

    @Override // android.view.IColorViewHooks
    public IColorScrollBarEffect getScrollBarEffect() {
        return ColorScrollBarEffect.NO_EFFECT;
    }

    @Override // android.view.IColorViewHooks
    public boolean findViewsLongshotInfo(ColorLongshotViewInfo info) {
        return false;
    }

    @Override // android.view.IColorViewHooks
    public IColorLongshotController getLongshotController() {
        return null;
    }

    @Override // android.view.IColorViewHooks
    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent, int oldScrollY, boolean result) {
        return false;
    }

    @Override // android.view.IColorViewHooks
    public IColorViewConfigHelper getColorViewConfigHelper(Context context) {
        return IColorViewConfigHelper.DEFAULT;
    }
}
