package androidx.core.view;

import android.view.View;

public class NestedScrollingParentHelper {
    private int mNestedScrollAxes;

    public void onNestedScrollAccepted(View child, View target, int axes, int type) {
        this.mNestedScrollAxes = axes;
    }

    public int getNestedScrollAxes() {
        return this.mNestedScrollAxes;
    }

    public void onStopNestedScroll(View target, int type) {
        this.mNestedScrollAxes = 0;
    }
}
