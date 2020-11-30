package com.android.server.wm;

import android.common.OppoFeatureCache;
import com.android.server.am.IColorGameSpaceManager;

public abstract class OppoBaseSystemGesturesPointerEventListener {
    /* access modifiers changed from: protected */
    public void setSystemGesturePointerPosition(int screenWidth, int statusBarHeight, float x, float y) {
        OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).setSystemGesturePointerPosition(screenWidth, statusBarHeight, x, y);
    }
}
