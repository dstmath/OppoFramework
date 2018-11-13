package com.color.screenshot;

import android.content.Context;
import com.color.screenshot.IColorLongshot.Stub;

public abstract class ColorLongshotService extends Stub {
    protected final Context mContext;
    protected final boolean mNavBarVisible;
    protected final boolean mStatusBarVisible;

    public ColorLongshotService(Context context, boolean statusBarVisible, boolean navBarVisible) {
        this.mContext = context;
        this.mStatusBarVisible = statusBarVisible;
        this.mNavBarVisible = navBarVisible;
    }

    public void start(IColorLongshotCallback callback) {
    }

    public void stop() {
    }

    public void notifyOverScroll(ColorLongshotEvent event) {
    }
}
