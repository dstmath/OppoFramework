package com.android.server.wm;

import android.content.Context;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import java.io.FileWriter;
import java.io.IOException;

public class OppoDisplayModeManager {
    private DisplayContent mDisplayContent;
    private DisplayInfo mDisplayInfo;
    private boolean mFeatureSupport;
    private PickRefreshRateData mPickRefreshRateData = new PickRefreshRateData();
    private String mTopFullscreenPkg;
    private TouchPanelNotifier mTouchPanelNotifier = new TouchPanelNotifier();
    private OppoDisplayModeService sService;

    OppoDisplayModeManager(Context context, DisplayContent dc, Display display) {
        this.mFeatureSupport = context.getPackageManager().hasSystemFeature("oppo.display.screen.90hz.support") || context.getPackageManager().hasSystemFeature("oppo.display.screen.120hz.support");
        if (this.mFeatureSupport) {
            this.mDisplayContent = dc;
            this.sService = OppoDisplayModeService.getInstance();
            this.sService.init(context);
            this.mDisplayInfo = new DisplayInfo();
            display.getDisplayInfo(this.mDisplayInfo);
            if (display.getDisplayId() == 0) {
                this.sService.setDefaultDisplayInfo(this.mDisplayInfo);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setVendorPreferredModeId(WindowState w) {
        if (this.mFeatureSupport) {
            this.sService.setVendorPreferredRefreshRate(w);
        }
    }

    /* access modifiers changed from: package-private */
    public int getPreferredModeId() {
        int useRefreshRateId;
        int i;
        boolean splitMode = false;
        if (!this.mFeatureSupport) {
            return 0;
        }
        AppWindowToken focus = this.mDisplayContent.mFocusedApp;
        if (focus != null && focus.inSplitScreenWindowingMode()) {
            splitMode = true;
        }
        boolean keyguardShown = this.mDisplayContent.mWmService.isKeyguardShowingAndNotOccluded();
        int settingMode = this.sService.getCurrentSettingMode();
        int windowPreferredId = 0;
        if (keyguardShown) {
            try {
                windowPreferredId = this.mDisplayContent.getWindow($$Lambda$OppoDisplayModeManager$Dgquo90RAIZCU2PjCLXyrVEif8.INSTANCE).mRefreshRateData.getPreferredRefreshRateId(settingMode);
            } catch (Exception e) {
            }
        } else {
            if (splitMode) {
                i = 2;
            } else {
                i = this.mPickRefreshRateData.mPreferredId != 0 ? this.mPickRefreshRateData.mPreferredId : this.mPickRefreshRateData.mLastPreferredId;
            }
            windowPreferredId = i;
        }
        if (windowPreferredId != 0) {
            useRefreshRateId = windowPreferredId;
        } else {
            useRefreshRateId = OppoRefreshRateUtils.getDefaultRefreshRateId(settingMode);
        }
        if (OppoRefreshRateConstants.DEBUG) {
            Slog.w("RefreshRate", "getPreferredModeId: topFullscreenPkg=" + this.mTopFullscreenPkg + ": windowPreferredId=" + windowPreferredId + ": keyguardShown=" + keyguardShown + ": splitMode=" + splitMode + ": useRefreshRateId=" + useRefreshRateId + ": settingMode=" + settingMode);
        }
        return this.sService.getPreferredModeId(useRefreshRateId, this.mDisplayInfo);
    }

    static /* synthetic */ boolean lambda$getPreferredModeId$0(WindowState w) {
        return w.mAttrs.type == 2000;
    }

    /* access modifiers changed from: package-private */
    public void overrideVendorPreferredModeIdIfNeed(WindowState w, int refreshRateId) {
        if (this.mFeatureSupport) {
            int old = w.mRefreshRateData.getOverrideRefreshRateId();
            if (refreshRateId > 0) {
                w.mRefreshRateData.overrideRefreshRateId(refreshRateId);
            } else {
                w.mRefreshRateData.clearOverrideId();
            }
            if (old != w.mRefreshRateData.getOverrideRefreshRateId()) {
                this.mDisplayContent.mWmService.requestTraversal();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resetModeId() {
        this.mPickRefreshRateData.reset();
    }

    /* access modifiers changed from: package-private */
    public void applyPreferredMode(WindowState w, boolean topFullScreen) {
        if (!this.mFeatureSupport || this.mPickRefreshRateData.mPreferredId != 0) {
            return;
        }
        if (topFullScreen || w.mIsImWindow) {
            this.mTopFullscreenPkg = w.getOwningPackage();
            boolean isAnimating = true;
            if ((w.getOwningPackage() != null && w.getOwningPackage().contains("android.view.cts")) || !w.isAnimating()) {
                isAnimating = false;
            }
            if (isAnimating) {
                try {
                    this.mPickRefreshRateData.mPreferredId = this.mPickRefreshRateData.mLastPreferredId;
                } catch (Exception e) {
                }
            } else {
                int settingMode = this.sService.getCurrentSettingMode();
                int candidate = w.mIsImWindow ? 2 : w.mRefreshRateData.getPreferredRefreshRateId(settingMode);
                PickRefreshRateData pickRefreshRateData = this.mPickRefreshRateData;
                PickRefreshRateData pickRefreshRateData2 = this.mPickRefreshRateData;
                int defaultRefreshRateId = candidate != 0 ? candidate : OppoRefreshRateUtils.getDefaultRefreshRateId(settingMode);
                pickRefreshRateData2.mLastPreferredId = defaultRefreshRateId;
                pickRefreshRateData.mPreferredId = defaultRefreshRateId;
            }
            if (OppoRefreshRateConstants.DEBUG) {
                Slog.w("RefreshRate", "applyPreferredMode: win=" + w + ": isAnimating=" + isAnimating + ": refreshId=" + this.mPickRefreshRateData.mPreferredId);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class PickRefreshRateData {
        int mLastPreferredId;
        int mPreferredId;

        private PickRefreshRateData() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.mPreferredId = 0;
        }
    }

    private class TouchPanelNotifier {
        private int mLastReportValue;

        private TouchPanelNotifier() {
            this.mLastReportValue = -1;
        }

        /* access modifiers changed from: package-private */
        public void update(String pkg, int refreshRateId) {
            final int value = (OppoDisplayModeManager.this.sService == null || !OppoDisplayModeManager.this.sService.needTpBoost(pkg) || refreshRateId != 3) ? 0 : 1;
            if (this.mLastReportValue != value) {
                new Thread(new Runnable() {
                    /* class com.android.server.wm.OppoDisplayModeManager.TouchPanelNotifier.AnonymousClass1 */

                    public void run() {
                        TouchPanelNotifier.this.mLastReportValue = value;
                        TouchPanelNotifier.this.sendToTouchPanel(value);
                    }
                }).start();
            }
        }

        /* access modifiers changed from: package-private */
        public void sendToTouchPanel(int data) {
            FileWriter writer = null;
            try {
                writer = new FileWriter("/proc/touchpanel/report_rate_white_list");
                writer.write(Integer.toString(data));
                try {
                    writer.close();
                } catch (IOException e) {
                }
            } catch (IOException e2) {
                Slog.e("RefreshRate", "fail to send data to tp" + e2);
                if (writer != null) {
                    writer.close();
                }
            } catch (Throwable th) {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e3) {
                    }
                }
                throw th;
            }
        }
    }
}
