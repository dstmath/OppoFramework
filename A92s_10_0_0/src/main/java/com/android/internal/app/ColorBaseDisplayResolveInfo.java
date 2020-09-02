package com.android.internal.app;

import android.content.Intent;

public class ColorBaseDisplayResolveInfo {
    private boolean mIsMultiApp = false;

    public boolean getIsMultiApp() {
        return this.mIsMultiApp;
    }

    public void setIsMultiApp(boolean isMultiApp) {
        this.mIsMultiApp = isMultiApp;
    }

    public void fixIntent(Intent intent) {
        if (this.mIsMultiApp) {
            intent.addCategory("com.multiple.launcher");
        }
    }
}
