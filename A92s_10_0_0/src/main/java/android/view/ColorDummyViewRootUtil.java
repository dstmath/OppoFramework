package android.view;

import android.content.Context;
import java.lang.ref.WeakReference;

public class ColorDummyViewRootUtil implements IColorViewRootUtil {
    @Override // android.view.IColorViewRootUtil
    public void initSwipState(Display display, Context context) {
    }

    @Override // android.view.IColorViewRootUtil
    public boolean needScale(int noncompatDensity, int density, Display display) {
        return false;
    }

    @Override // android.view.IColorViewRootUtil
    public boolean swipeFromBottom(MotionEvent event, int noncompatDensity, int density, Display display) {
        return false;
    }

    @Override // android.view.IColorViewRootUtil
    public float getCompactScale() {
        return 1.0f;
    }

    @Override // android.view.IColorViewRootUtil
    public int getScreenHeight() {
        return 1;
    }

    @Override // android.view.IColorViewRootUtil
    public int getScreenWidth() {
        return 1;
    }

    @Override // android.view.IColorViewRootUtil
    public void checkGestureConfig(Context context) {
    }

    @Override // android.view.IColorViewRootUtil
    public DisplayInfo getDisplayInfo() {
        return null;
    }

    @Override // android.view.IColorViewRootUtil
    public IColorLongshotViewHelper getColorLongshotViewHelper(WeakReference<ViewRootImpl> weakReference) {
        return null;
    }
}
