package com.android.server;

import android.common.OppoFeatureCache;
import android.content.Context;
import com.android.server.display.DisplayManagerService;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.wm.WindowManagerService;

public final class OppoSystemServerHelper {
    private static final String TAG = "OppoSystemServerHelper";
    private final IColorSystemServerEx mColorSystemServerEx;
    private final Context mContext;
    private final IPswSystemServerEx mPswSystemServerEx;

    public OppoSystemServerHelper(Context context) {
        this.mContext = context;
        OppoFeatureCache.addFactory(ColorServiceFactory.getInstance());
        OppoFeatureCache.addFactory(PswServiceFactory.getInstance());
        this.mColorSystemServerEx = ColorServiceFactory.getInstance().getFeature(IColorSystemServerEx.DEFAULT, new Object[]{context});
        this.mPswSystemServerEx = PswServiceFactory.getInstance().getFeature(IPswSystemServerEx.DEFAULT, new Object[]{context});
    }

    public void startBootstrapServices() {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            iColorSystemServerEx.startBootstrapServices();
        }
        IPswSystemServerEx iPswSystemServerEx = this.mPswSystemServerEx;
        if (iPswSystemServerEx != null) {
            iPswSystemServerEx.startBootstrapServices();
        }
    }

    public void startCoreServices() {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            iColorSystemServerEx.startCoreServices();
        }
        IPswSystemServerEx iPswSystemServerEx = this.mPswSystemServerEx;
        if (iPswSystemServerEx != null) {
            iPswSystemServerEx.startCoreServices();
        }
    }

    /* access modifiers changed from: package-private */
    public void startOtherServices() {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            iColorSystemServerEx.startOtherServices();
        }
        IPswSystemServerEx iPswSystemServerEx = this.mPswSystemServerEx;
        if (iPswSystemServerEx != null) {
            iPswSystemServerEx.startOtherServices();
        }
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            iColorSystemServerEx.systemReady();
        }
        IPswSystemServerEx iPswSystemServerEx = this.mPswSystemServerEx;
        if (iPswSystemServerEx != null) {
            iPswSystemServerEx.systemReady();
        }
    }

    /* access modifiers changed from: package-private */
    public void systemRunning() {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            iColorSystemServerEx.systemRunning();
        }
        IPswSystemServerEx iPswSystemServerEx = this.mPswSystemServerEx;
        if (iPswSystemServerEx != null) {
            iPswSystemServerEx.systemRunning();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean startColorLightService() {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            return iColorSystemServerEx.startColorLightService();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean startColorAccessibilityService() {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            return iColorSystemServerEx.startColorAccessibilityService();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean startColorDeviceStorageMonitorService() {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            return iColorSystemServerEx.startColorDeviceStorageMonitorService();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean startColorStatusBarService(WindowManagerService wms) {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            return iColorSystemServerEx.startColorStatusBarService(wms);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean startColorNotificationManagerService() {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            return iColorSystemServerEx.startColorNotificationManagerService();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public PhoneWindowManager startPhoneWindowManager() {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            return iColorSystemServerEx.startPhoneWindowManager();
        }
        return new PhoneWindowManager();
    }

    /* access modifiers changed from: package-private */
    public boolean startColorJobSchedulerService() {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            return iColorSystemServerEx.startColorJobSchedulerService();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public DisplayManagerService startColorDisplayManagerService() {
        IColorSystemServerEx iColorSystemServerEx = this.mColorSystemServerEx;
        if (iColorSystemServerEx != null) {
            return iColorSystemServerEx.startColorDisplayManagerService();
        }
        return null;
    }
}
