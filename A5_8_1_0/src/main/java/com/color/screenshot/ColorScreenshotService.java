package com.color.screenshot;

import android.content.Context;
import android.os.Bundle;
import com.color.screenshot.IColorScreenshot.Stub;

public abstract class ColorScreenshotService extends Stub {
    protected final Context mContext;
    protected final Bundle mExtras;

    public ColorScreenshotService(Context context, Bundle extras) {
        this.mContext = context;
        this.mExtras = extras;
    }

    public void start(IColorScreenshotCallback callback) {
    }

    public void stop() {
    }

    public boolean isEdit() {
        return false;
    }
}
