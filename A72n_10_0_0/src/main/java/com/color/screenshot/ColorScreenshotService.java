package com.color.screenshot;

import android.content.Context;
import android.os.Bundle;
import com.color.screenshot.IColorScreenshot;

public abstract class ColorScreenshotService extends IColorScreenshot.Stub {
    protected final Context mContext;
    protected final Bundle mExtras;

    public ColorScreenshotService(Context context, Bundle extras) {
        this.mContext = context;
        this.mExtras = extras;
    }

    @Override // com.color.screenshot.IColorScreenshot
    public void start(IColorScreenshotCallback callback) {
    }

    @Override // com.color.screenshot.IColorScreenshot
    public void stop() {
    }

    @Override // com.color.screenshot.IColorScreenshot
    public boolean isEdit() {
        return false;
    }
}
