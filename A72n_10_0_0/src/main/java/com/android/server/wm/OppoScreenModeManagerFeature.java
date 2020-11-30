package com.android.server.wm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.Slog;

public class OppoScreenModeManagerFeature implements IOppoScreenModeManagerFeature {
    private static final String FEATURE_AUTO_RESOLUTION = "oppo.appautoresolution.support";
    private static final String FEATURE_RATE = "oppo.dynamicfpsswitch.feature.support";
    private static final String FEATURE_RATE120HZ = "oppo.display.screen.120hz.support";
    private static final String FEATURE_RATE90HZ = "oppo.display.screen.90hz.support";
    private static final String FEATURE_RESOLUTION = "oppo.resolutionswitch.feature.support";
    private static final String TAG = "OppoScreenModeManagerFeature";
    private static boolean mOppoDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static boolean sDynamicFPSSupport = false;
    private static boolean sResolutionSupport = false;
    private Context mContext;
    private boolean mInitialized;
    private OppoScreenModeService mModeService;
    private OppoScreenCompat mOppoScreenCompat;

    private OppoScreenModeManagerFeature() {
        this.mContext = null;
        this.mModeService = null;
        this.mOppoScreenCompat = null;
        this.mInitialized = false;
    }

    private static class InstanceHolder {
        static final OppoScreenModeManagerFeature INSTANCE = new OppoScreenModeManagerFeature();

        private InstanceHolder() {
        }
    }

    public static OppoScreenModeManagerFeature getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void init(WindowManagerService service, Context context) {
        if (!this.mInitialized) {
            if (context == null) {
                Slog.e(TAG, "init failed for context null!");
                return;
            }
            PackageManager pkgMgr = context.getPackageManager();
            if (pkgMgr == null) {
                Slog.e(TAG, "init failed for pkgMgr uninit!");
                return;
            }
            if (pkgMgr.hasSystemFeature(FEATURE_RATE) || pkgMgr.hasSystemFeature(FEATURE_RATE90HZ) || pkgMgr.hasSystemFeature(FEATURE_RATE120HZ)) {
                sDynamicFPSSupport = true;
            } else {
                sDynamicFPSSupport = false;
            }
            boolean switchEnable = false;
            if (pkgMgr.hasSystemFeature(FEATURE_RESOLUTION)) {
                sResolutionSupport = true;
                switchEnable = true;
            } else {
                sResolutionSupport = false;
            }
            if (pkgMgr.hasSystemFeature(FEATURE_AUTO_RESOLUTION)) {
                if (sResolutionSupport) {
                    Slog.e(TAG, "  don't add two resolution feature oppo.appautoresolution.support");
                    sResolutionSupport = false;
                } else {
                    sResolutionSupport = true;
                }
            }
            Slog.d(TAG, " init support sDynamicFPSSupport " + sDynamicFPSSupport + ",sResolutionSupport=" + sResolutionSupport);
            if (!sDynamicFPSSupport && !sResolutionSupport) {
                this.mInitialized = true;
            } else if (service == null) {
                Slog.e(TAG, "init failed for WindowManagerService uninit!");
            } else {
                if (this.mModeService == null) {
                    this.mModeService = new OppoScreenModeService();
                }
                this.mOppoScreenCompat = OppoScreenCompat.getInstance();
                OppoScreenModeService oppoScreenModeService = this.mModeService;
                if (oppoScreenModeService == null || oppoScreenModeService.mServiceReady) {
                    Slog.e(TAG, "init failed for mModeService init failed, must retry!");
                    this.mInitialized = false;
                    return;
                }
                this.mModeService.init(service, context, sDynamicFPSSupport, sResolutionSupport, switchEnable);
                this.mContext = context;
                this.mInitialized = true;
            }
        }
    }

    public void setRefreshRate(IBinder token, int rate) {
        OppoScreenModeService oppoScreenModeService;
        if (sDynamicFPSSupport && (oppoScreenModeService = this.mModeService) != null && oppoScreenModeService.mServiceReady) {
            this.mModeService.setRefreshRate(token, rate);
        }
    }

    public int getPreferredModeId(WindowState w, int candidateMode) {
        OppoScreenModeService oppoScreenModeService;
        if ((sDynamicFPSSupport || sResolutionSupport) && (oppoScreenModeService = this.mModeService) != null && oppoScreenModeService.mServiceReady) {
            return this.mModeService.getPreferredModeId(w, candidateMode);
        }
        return 0;
    }

    public int updateGlobalModeId(int modeId) {
        OppoScreenModeService oppoScreenModeService;
        if ((sDynamicFPSSupport || sResolutionSupport) && (oppoScreenModeService = this.mModeService) != null && oppoScreenModeService.mServiceReady) {
            return this.mModeService.updateGlobalModeId(modeId);
        }
        return modeId;
    }

    public void onSetDensityForUser(int density, int userId) {
        OppoScreenModeService oppoScreenModeService;
        if (sResolutionSupport && (oppoScreenModeService = this.mModeService) != null && oppoScreenModeService.mServiceReady) {
            this.mModeService.onSetDensityForUser(density, userId);
        }
    }

    public int adjustDensityForUser(int density, int userId) {
        OppoScreenModeService oppoScreenModeService;
        if (sResolutionSupport && (oppoScreenModeService = this.mModeService) != null && oppoScreenModeService.mServiceReady) {
            return this.mModeService.adjustDensityForUser(density, userId);
        }
        return density;
    }

    public void setCurrentUser(int userId) {
        OppoScreenModeService oppoScreenModeService;
        if (sResolutionSupport && (oppoScreenModeService = this.mModeService) != null && oppoScreenModeService.mServiceReady) {
            this.mModeService.setCurrentUser(userId);
        }
    }

    public void updateScreenSplitMode(boolean mode) {
        OppoScreenModeService oppoScreenModeService;
        if (sDynamicFPSSupport && (oppoScreenModeService = this.mModeService) != null && oppoScreenModeService.mServiceReady) {
            this.mModeService.updateScreenSplitMode(mode);
        }
    }

    public void startAnimation(boolean start) {
        OppoScreenModeService oppoScreenModeService;
        if (sDynamicFPSSupport && (oppoScreenModeService = this.mModeService) != null && oppoScreenModeService.mServiceReady) {
            this.mModeService.startAnimation(start);
        }
    }

    public void enterDCAndLowBrightnessMode(boolean enter) {
        OppoScreenModeService oppoScreenModeService;
        if (sDynamicFPSSupport && (oppoScreenModeService = this.mModeService) != null && oppoScreenModeService.mServiceReady) {
            this.mModeService.enterDCAndLowBrightnessMode(enter);
        }
    }

    public void enterPSMode(boolean enter) {
        OppoScreenModeService oppoScreenModeService;
        if (sDynamicFPSSupport && (oppoScreenModeService = this.mModeService) != null && oppoScreenModeService.mServiceReady) {
            this.mModeService.enterPSMode(enter);
        }
    }

    public void enterPSModeOnRate(boolean enter, int rate) {
        OppoScreenModeService oppoScreenModeService;
        if (sDynamicFPSSupport && (oppoScreenModeService = this.mModeService) != null && oppoScreenModeService.mServiceReady) {
            this.mModeService.enterPSModeOnRate(enter, rate);
        }
    }

    public void overrideCompatInfoIfNeed(ApplicationInfo ai) {
        OppoScreenCompat oppoScreenCompat;
        if (sResolutionSupport && (oppoScreenCompat = this.mOppoScreenCompat) != null) {
            oppoScreenCompat.overrideCompatInfoIfNeed(ai);
        }
    }

    public boolean isDisplayCompat(String packageName, int uid) {
        OppoScreenCompat oppoScreenCompat;
        if (sResolutionSupport && (oppoScreenCompat = this.mOppoScreenCompat) != null) {
            return oppoScreenCompat.isDisplayCompat(packageName, uid);
        }
        return false;
    }

    public float overrideScaleIfNeed(WindowState win) {
        if (!sResolutionSupport) {
            return win.mGlobalScale;
        }
        OppoScreenCompat oppoScreenCompat = this.mOppoScreenCompat;
        if (oppoScreenCompat != null) {
            return oppoScreenCompat.overrideScaleIfNeed(win);
        }
        return win.mGlobalScale;
    }

    public void updateInputMethod(boolean state) {
        OppoScreenModeService oppoScreenModeService;
        if (sDynamicFPSSupport && (oppoScreenModeService = this.mModeService) != null && oppoScreenModeService.mServiceReady) {
            this.mModeService.updateInputMethod(state);
        }
    }
}
