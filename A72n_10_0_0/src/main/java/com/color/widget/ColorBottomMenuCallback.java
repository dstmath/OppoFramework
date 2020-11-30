package com.color.widget;

import android.animation.Animator;

public interface ColorBottomMenuCallback extends ColorPagerCallback {
    public static final int INVALID_POSITION = -1;
    public static final int UPDATE_MODE_ANIMATE = 1;
    public static final int UPDATE_MODE_DIRECT = 0;
    public static final int UPDATE_MODE_OUTER = 2;

    public interface Updater {
        Animator getUpdater(int i, int i2);

        boolean visibleFirst();
    }

    void lockMenuUpdate();

    void setMenuUpdateMode(int i);

    void unlockMenuUpdate();

    void updateMenuScrollData();

    void updateMenuScrollPosition(int i, float f);

    void updateMenuScrollState(int i);
}
