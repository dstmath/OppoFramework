package com.android.server;

import android.content.Context;
import com.android.server.display.DisplayManagerService;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.wm.WindowManagerService;

public class ColorDummySystemServerEx extends OppoDummySystemServerEx implements IColorSystemServerEx {
    public ColorDummySystemServerEx(Context context) {
        super(context);
    }

    @Override // com.android.server.IColorSystemServerEx
    public boolean startColorLightService() {
        return false;
    }

    @Override // com.android.server.IColorSystemServerEx
    public boolean startColorDeviceStorageMonitorService() {
        return false;
    }

    @Override // com.android.server.IColorSystemServerEx
    public boolean startColorAccessibilityService() {
        return false;
    }

    @Override // com.android.server.IColorSystemServerEx
    public boolean startColorStatusBarService(WindowManagerService wms) {
        return false;
    }

    @Override // com.android.server.IColorSystemServerEx
    public boolean startColorNotificationManagerService() {
        return false;
    }

    @Override // com.android.server.IColorSystemServerEx
    public PhoneWindowManager startPhoneWindowManager() {
        return new PhoneWindowManager();
    }

    @Override // com.android.server.IColorSystemServerEx
    public boolean startColorJobSchedulerService() {
        return false;
    }

    @Override // com.android.server.IColorSystemServerEx
    public void startColorScreenshotManagerService() {
    }

    @Override // com.android.server.IColorSystemServerEx
    public DisplayManagerService startColorDisplayManagerService() {
        return null;
    }
}
