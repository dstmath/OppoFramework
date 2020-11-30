package com.android.server.wm;

import android.os.Bundle;

public class ColorDummyBreenoManager implements IColorBreenoManager {
    @Override // com.android.server.wm.IColorBreenoManager
    public void init(IColorWindowManagerServiceEx wmsEx) {
    }

    @Override // com.android.server.wm.IColorBreenoManager
    public boolean isBreeno() {
        return false;
    }

    @Override // com.android.server.wm.IColorBreenoManager
    public boolean inDragWindowing() {
        return false;
    }

    @Override // com.android.server.wm.IColorBreenoManager
    public boolean hasColorDragWindowAnimation() {
        return false;
    }

    @Override // com.android.server.wm.IColorBreenoManager
    public boolean stepAnimation(long currentTime) {
        return false;
    }

    @Override // com.android.server.wm.IColorBreenoManager
    public void startColorDragWindow(String packageName, int resId, int mode, Bundle options) {
    }

    @Override // com.android.server.wm.IColorBreenoManager
    public void setBreenoState(String winName) {
    }

    @Override // com.android.server.wm.IColorBreenoManager
    public boolean canMagnificationSpec(WindowState win) {
        return false;
    }

    @Override // com.android.server.wm.IColorBreenoManager
    public void recoveryState() {
    }
}
