package com.color.screenshot;

import android.os.Handler;
import android.os.Looper;

public class ColorGlobalActionHandler extends Handler implements IColorScreenshotHelper {
    public ColorGlobalActionHandler(Looper looper) {
        super(looper);
    }

    public String getSource() {
        return "GlobalAction";
    }

    public boolean isGlobalAction() {
        return false;
    }
}
