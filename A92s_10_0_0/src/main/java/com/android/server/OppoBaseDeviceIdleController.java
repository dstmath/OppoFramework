package com.android.server;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.ArrayMap;
import com.android.server.UiModeManagerService;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.usage.AppStandbyController;

public abstract class OppoBaseDeviceIdleController extends SystemService {
    protected static final long ADVANCE_TIME = 10000;
    protected static final int ANY_MOTION = 2;
    protected static boolean DEBUG_OPPO = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    protected static final long DEFAULT_TOTAL_INTERVAL_TO_IDLE = 1800000;
    protected static final long DURATION_TRAFFIC_CHECK = 300000;
    protected static final long QUICK_ENTER_DEEPSLEEP_DRUATION = 180000;
    protected static final int SIGNIFICANT_MOTION = 1;
    private static final String TAG = "OppoBaseDeviceIdleController";
    IColorDeviceIdleControllerInner mColorDeviceICInner = null;
    IColorDeviceIdleControllerEx mColorDeviceIdleEx = null;
    IColorGoogleDozeRestrictInner mColorGoogleDRInner = null;
    private boolean mDeepIdle = false;
    protected boolean mDeepIdleAccordingToRus = true;
    private boolean mLightIdle = false;
    protected boolean mLightIdleAccordingToRus = true;
    /* access modifiers changed from: private */
    public final ArrayMap<String, Integer> mPowerSaveWhitelistUserApp = new ArrayMap<>();

    public OppoBaseDeviceIdleController(Context context) {
        super(context);
    }

    /* access modifiers changed from: package-private */
    public void updateDeviceIdleValue(boolean light, boolean deep) {
        this.mLightIdle = light;
        this.mDeepIdle = deep;
    }

    /* access modifiers changed from: package-private */
    public void putDeepIdleExtra(int state, int stateIdleMaintenance, Intent idleIntent, boolean always) {
        if (always || !this.mDeepIdle) {
            idleIntent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, state == stateIdleMaintenance ? "maintenance" : UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
        }
    }

    /* access modifiers changed from: package-private */
    public void putDeepIdleExtra(Intent idleIntent, String reason) {
        idleIntent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, reason != null ? reason : UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
    }

    /* access modifiers changed from: package-private */
    public void putLightIdleExtra(int state, int stateIdleMaintenance, int stateOverride, Intent idleIntent, boolean always) {
        if (always || !this.mLightIdle) {
            idleIntent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, state == stateIdleMaintenance ? "maintenance" : state == stateOverride ? "override" : UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
        }
    }

    /* access modifiers changed from: package-private */
    public void putLightIdleExtra(Intent idleIntent, String reason) {
        idleIntent.putExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, reason != null ? reason : UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN);
    }

    public class ColorBaseDeviceIdleControllerInner implements IColorDeviceIdleControllerInner {
        public ColorBaseDeviceIdleControllerInner() {
        }

        @Override // com.android.server.IColorDeviceIdleControllerInner
        public int getState() {
            return 0;
        }

        @Override // com.android.server.IColorDeviceIdleControllerInner
        public void setState(int value) {
        }

        @Override // com.android.server.IColorDeviceIdleControllerInner
        public boolean getDeepEnabled() {
            return true;
        }

        @Override // com.android.server.IColorDeviceIdleControllerInner
        public void setDeepEnabled(boolean value) {
        }

        @Override // com.android.server.IColorDeviceIdleControllerInner
        public long getNextIdlePendingDelay() {
            return 300000;
        }

        @Override // com.android.server.IColorDeviceIdleControllerInner
        public void setNextIdlePendingDelay(long value) {
        }

        @Override // com.android.server.IColorDeviceIdleControllerInner
        public long getNextIdleDelay() {
            return AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT;
        }

        @Override // com.android.server.IColorDeviceIdleControllerInner
        public void setNextIdleDelay(long value) {
        }

        @Override // com.android.server.IColorDeviceIdleControllerInner
        public void resetIdleManagementLocked() {
        }

        @Override // com.android.server.IColorDeviceIdleControllerInner
        public ArrayMap<String, Integer> getPowerSaveWhitelistUserApps() {
            return OppoBaseDeviceIdleController.this.mPowerSaveWhitelistUserApp;
        }
    }

    public class ColorBaseGoogleDozeRestrictInner implements IColorGoogleDozeRestrictInner {
        public ColorBaseGoogleDozeRestrictInner() {
        }

        @Override // com.android.server.IColorGoogleDozeRestrictInner
        public void reportPowerSaveWhitelistChangedLocked() {
        }

        @Override // com.android.server.IColorGoogleDozeRestrictInner
        public void updateWhitelistAppIdsLocked() {
        }
    }
}
