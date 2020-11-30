package com.android.server;

import android.common.OppoFeatureList;
import com.android.server.display.DisplayManagerService;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.wm.WindowManagerService;

public interface IColorSystemServerEx extends IOppoSystemServerEx {
    public static final IColorSystemServerEx DEFAULT = new IColorSystemServerEx() {
        /* class com.android.server.IColorSystemServerEx.AnonymousClass1 */
    };
    public static final String NAME = "IColorSystemServerEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorSystemServerEx;
    }

    default IColorSystemServerEx getDefault() {
        return DEFAULT;
    }

    default boolean startColorLightService() {
        return false;
    }

    default boolean startColorDeviceStorageMonitorService() {
        return false;
    }

    default boolean startColorAccessibilityService() {
        return false;
    }

    default boolean startColorStatusBarService(WindowManagerService wms) {
        return false;
    }

    default boolean startColorNotificationManagerService() {
        return false;
    }

    default PhoneWindowManager startPhoneWindowManager() {
        return new PhoneWindowManager();
    }

    default boolean startColorJobSchedulerService() {
        return false;
    }

    default void startColorScreenshotManagerService() {
    }

    default DisplayManagerService startColorDisplayManagerService() {
        return null;
    }
}
