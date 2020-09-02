package com.color.screenshot;

import android.content.Context;
import com.color.screenshot.IColorLongshot;

public abstract class ColorLongshotService extends IColorLongshot.Stub {
    protected final Context mContext;
    protected final boolean mNavBarVisible;
    protected final boolean mStatusBarVisible;

    public ColorLongshotService(Context context, boolean statusBarVisible, boolean navBarVisible) {
        this.mContext = context;
        this.mStatusBarVisible = statusBarVisible;
        this.mNavBarVisible = navBarVisible;
    }

    @Override // com.color.screenshot.IColorLongshot
    public void start(IColorLongshotCallback callback) {
    }

    @Override // com.color.screenshot.IColorLongshot
    public void stop() {
    }

    @Override // com.color.screenshot.IColorLongshot
    public void notifyOverScroll(ColorLongshotEvent event) {
    }
}
