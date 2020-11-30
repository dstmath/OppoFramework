package com.oppo.screenmode;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.OppoApplicationInfoEx;
import android.content.pm.PackageManager;
import android.content.res.CompatibilityInfo;
import android.graphics.Point;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.OppoBaseView;
import android.view.View;
import android.view.ViewRootImpl;
import com.color.util.ColorTypeCastingHelper;
import com.oppo.screenmode.IOppoScreenMode;

public class PswScreenModeFeature implements IPswScreenModeFeature {
    public static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.compat.debug", false);
    static final int DEFAULT_RATE = 0;
    private static final String FEATURE_RATE = "oppo.dynamicfpsswitch.feature.support";
    private static final String FEATURE_RATE120HZ = "oppo.display.screen.120hz.support";
    private static final String FEATURE_RATE90HZ = "oppo.display.screen.90hz.support";
    private static final String OPPO_SCREENMODE_SERVICE_NAME = "opposcreenmode";
    static final int RATE_120 = 3;
    static final int RATE_60 = 2;
    static final int RATE_90 = 1;
    static final int RATE_AUTO = 0;
    private static final String TAG = "PswScreenModeFeature";
    private static boolean mOppoDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static int sCompatDensity = 420;
    public static boolean sIsDisplayCompatApp = false;
    private static boolean sScreenRateSupport = false;
    private float appInvScale;
    private float appScale;
    private Context mContext;
    private int mCurrentRate;
    private boolean mInitialized;
    private IOppoScreenMode mScreenModeService;
    private Binder mToken;

    private PswScreenModeFeature() {
        this.mContext = null;
        this.mScreenModeService = null;
        this.mToken = null;
        this.mCurrentRate = 0;
        this.appScale = 1.0f;
        this.appInvScale = 1.0f;
        this.mInitialized = false;
        if (mOppoDebug) {
            Log.d(TAG, "PswScreenModeFeature create");
        }
    }

    private static class InstanceHolder {
        static final PswScreenModeFeature INSTANCE = new PswScreenModeFeature();

        private InstanceHolder() {
        }
    }

    public static PswScreenModeFeature getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void init(Context context) {
        if (mOppoDebug) {
            Log.d(TAG, "PswScreenModeFeature init.");
        }
        if (!this.mInitialized) {
            if (context == null) {
                Log.e(TAG, "PswScreenModeFeature init failed for context empty!");
                return;
            }
            this.mContext = context;
            PackageManager pkgMgr = this.mContext.getPackageManager();
            if (pkgMgr == null) {
                Log.w(TAG, "PswScreenModeFeature init failed for pkgMgr uninit!");
                return;
            }
            if (pkgMgr.hasSystemFeature(FEATURE_RATE) || pkgMgr.hasSystemFeature(FEATURE_RATE90HZ) || pkgMgr.hasSystemFeature(FEATURE_RATE120HZ)) {
                sScreenRateSupport = true;
            } else {
                sScreenRateSupport = false;
            }
            if (mOppoDebug) {
                Log.d(TAG, "PswScreenModeFeature init support 120hz:" + sScreenRateSupport);
            }
            if (this.mScreenModeService == null) {
                IBinder binder = ServiceManager.getService(OPPO_SCREENMODE_SERVICE_NAME);
                if (binder == null) {
                    Log.e(TAG, "failed to get oppposcreenmode service:binder null");
                } else {
                    this.mScreenModeService = IOppoScreenMode.Stub.asInterface(binder);
                }
            }
            if (this.mScreenModeService != null) {
                this.mInitialized = true;
                this.mToken = new Binder();
                return;
            }
            Log.e(TAG, "failed to get oppposcreenmode service:interface null.");
        }
    }

    public void setRefreshRate(IBinder token, int rate) {
        if (mOppoDebug) {
            Log.d(TAG, "setRefreshRate with token, sScreenRateSupport:" + sScreenRateSupport);
        }
        if (sScreenRateSupport) {
            if (this.mScreenModeService != null) {
                try {
                    if (rate != this.mCurrentRate) {
                        if (mOppoDebug) {
                            Log.d(TAG, "setRefreshRate token " + token + " rate " + rate + " mCurrentRate " + this.mCurrentRate);
                        }
                        this.mScreenModeService.setClientRefreshRate(token, rate);
                        this.mCurrentRate = rate;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "setRefreshRate failed!", e);
                }
            } else {
                Log.w(TAG, "setRefreshRate service unavailable!!");
            }
        }
    }

    public void setRefreshRate(View view, int rate) {
        if (mOppoDebug) {
            Log.d(TAG, "setRefreshRate with view, sScreenRateSupport:" + sScreenRateSupport);
        }
        if (sScreenRateSupport) {
            if (this.mScreenModeService == null || view == null) {
                Log.w(TAG, "setRefreshRate service unavailable!!");
                return;
            }
            int tokenRate = getTokenRate(view, getViewRate(view, rate));
            ViewRootImpl viewRoot = view.getViewRootImpl();
            if (viewRoot != null) {
                setRefreshRate(viewRoot.mWindowAttributes.token, tokenRate);
            } else {
                Log.w(TAG, "setRefreshRate ignore for viewRoot empty.");
            }
        }
    }

    public boolean requestRefreshRate(boolean open, int rate) {
        if (mOppoDebug) {
            Log.d(TAG, "requestRefreshRate, sScreenRateSupport:" + sScreenRateSupport);
        }
        if (!sScreenRateSupport) {
            return false;
        }
        if (this.mScreenModeService != null) {
            try {
                if (mOppoDebug) {
                    Log.d(TAG, "requestRefreshRate open " + open + " rate " + rate);
                }
                return this.mScreenModeService.requestRefreshRateWithToken(open, rate, this.mToken);
            } catch (RemoteException e) {
                Log.e(TAG, "requestRefreshRate failed!", e);
                return false;
            }
        } else {
            Log.w(TAG, "requestRefreshRate service unavailable!!");
            return false;
        }
    }

    public boolean setHighTemperatureStatus(int status, int rate) {
        if (mOppoDebug) {
            Log.d(TAG, "setHighTemperatureStatus, sScreenRateSupport:" + sScreenRateSupport);
        }
        if (!sScreenRateSupport) {
            return false;
        }
        if (this.mScreenModeService != null) {
            try {
                if (mOppoDebug) {
                    Log.d(TAG, "setHighTemperatureStatus status " + status + " rate " + rate);
                }
                return this.mScreenModeService.setHighTemperatureStatus(status, rate);
            } catch (RemoteException e) {
                Log.e(TAG, "requestRefreshRate failed!", e);
                return false;
            }
        } else {
            Log.w(TAG, "setHighTemperatureStatus service unavailable!!");
            return false;
        }
    }

    public void enterDCAndLowBrightnessMode(boolean enter) {
        if (mOppoDebug) {
            Log.d(TAG, " enterDCAndLowBrightnessMode, enter:" + enter);
        }
        if (sScreenRateSupport) {
            if (this.mScreenModeService != null) {
                try {
                    if (mOppoDebug) {
                        Log.d(TAG, "enterDCAndLowBrightnessMode enter " + enter);
                    }
                    this.mScreenModeService.enterDCAndLowBrightnessMode(enter);
                } catch (RemoteException e) {
                    Log.e(TAG, "enterDCAndLowBrightnessMode failed!", e);
                }
            } else {
                Log.w(TAG, "enterDCAndLowBrightnessMode service unavailable!!");
            }
        }
    }

    public boolean isDisplayCompat(String packageName, int uid) {
        if (mOppoDebug) {
            Log.d(TAG, " isDisplayCompat, pkg:" + packageName);
        }
        IOppoScreenMode iOppoScreenMode = this.mScreenModeService;
        if (iOppoScreenMode != null) {
            try {
                return iOppoScreenMode.isDisplayCompat(packageName, uid);
            } catch (RemoteException e) {
                Log.e(TAG, "isDisplayCompat failed!", e);
                return false;
            }
        } else {
            Log.w(TAG, "isDisplayCompat service unavailable!!");
            return false;
        }
    }

    public void enterPSMode(boolean enter) {
        if (mOppoDebug) {
            Log.d(TAG, " performance spec mode, enter:" + enter);
        }
        if (sScreenRateSupport) {
            IOppoScreenMode iOppoScreenMode = this.mScreenModeService;
            if (iOppoScreenMode != null) {
                try {
                    iOppoScreenMode.enterPSMode(enter);
                } catch (RemoteException e) {
                    Log.e(TAG, "performance spec mode failed!", e);
                }
            } else {
                Log.w(TAG, "performance spec mode service unavailable!!");
            }
        }
    }

    public void enterPSModeOnRate(boolean enter, int rate) {
        if (mOppoDebug) {
            Log.d(TAG, " performance spec mode, enter:" + enter);
        }
        if (sScreenRateSupport) {
            IOppoScreenMode iOppoScreenMode = this.mScreenModeService;
            if (iOppoScreenMode != null) {
                try {
                    iOppoScreenMode.enterPSModeOnRateWithToken(enter, rate, this.mToken);
                } catch (RemoteException e) {
                    Log.e(TAG, "performance spec mode failed!", e);
                }
            } else {
                Log.w(TAG, "performance spec mode service unavailable!!");
            }
        }
    }

    public boolean getGameList(Bundle outBundle) {
        if (mOppoDebug) {
            Log.d(TAG, " game list  sScreenRateSupport=" + sScreenRateSupport);
        }
        if (!sScreenRateSupport) {
            return false;
        }
        IOppoScreenMode iOppoScreenMode = this.mScreenModeService;
        if (iOppoScreenMode != null) {
            try {
                return iOppoScreenMode.getGameList(outBundle);
            } catch (RemoteException e) {
                Log.e(TAG, "game list failed!", e);
                return false;
            }
        } else {
            Log.w(TAG, "game list mode service unavailable!!");
            return false;
        }
    }

    private int getViewRate(View view, int rate) {
        if (rate != -1) {
            return rate;
        }
        Point point = new Point();
        Display display = view.getDisplay();
        if (display == null) {
            return 0;
        }
        display.getRealSize(point);
        int shortSide = point.x < point.y ? point.x : point.y;
        if (shortSide <= 0 || !view.isShown() || view.getHeight() * view.getWidth() < shortSide * shortSide) {
            return 0;
        }
        return RATE_60;
    }

    private int getTokenRate(View view, int rate) {
        OppoBaseView baseView;
        String viewName = view.getClass().getName() + "-" + System.identityHashCode(view);
        if (view.getViewRootImpl() == null || (baseView = (OppoBaseView) ColorTypeCastingHelper.typeCasting(OppoBaseView.class, view)) == null) {
            return 0;
        }
        boolean containInList = baseView.containInScreenModeViewList(viewName);
        if (rate == RATE_60 && !containInList) {
            baseView.addViewToScreenModeViewList(viewName);
        } else if (rate == 0 && containInList) {
            baseView.removeViewFromScreenModeViewList(viewName);
        }
        if (baseView.isScreenModeViewListEmpty()) {
            return 0;
        }
        return RATE_60;
    }

    public void overrideDisplayMetricsIfNeed(DisplayMetrics inoutDm) {
        if (inoutDm == null) {
            Log.e(TAG, "overrideDisplayMetricsIfNeed failed for inoutDm null.");
            return;
        }
        int i = inoutDm.densityDpi;
        int i2 = sCompatDensity;
        if (i != i2) {
            float invertedRatio = this.appInvScale;
            float f = ((float) i2) * 0.00625f;
            inoutDm.density = f;
            inoutDm.scaledDensity = f;
            inoutDm.densityDpi = i2;
            inoutDm.xdpi = inoutDm.noncompatXdpi * invertedRatio;
            inoutDm.ydpi = inoutDm.noncompatYdpi * invertedRatio;
            inoutDm.widthPixels = (int) (((float) inoutDm.noncompatWidthPixels) * invertedRatio);
            inoutDm.heightPixels = (int) (((float) inoutDm.noncompatHeightPixels) * invertedRatio);
            if (DEBUG) {
                Log.d(TAG, "DisplayCompat: applyToDisplayMetrics0, inoutDm=" + inoutDm + " noncompatDensityDpi=" + inoutDm.noncompatDensityDpi + " caller:" + Debug.getCallers(10));
            }
        }
    }

    public void applyCompatInfo(CompatibilityInfo compatInfo, DisplayMetrics outMetrics) {
        if (compatInfo == null || outMetrics == null) {
            Log.e(TAG, "applyCompatInfo failed for param null.");
        } else if (sIsDisplayCompatApp && outMetrics.densityDpi != sCompatDensity) {
            if (DEBUG) {
                Log.d(TAG, "DisplayCompat: applyCompatInfo, change out=" + outMetrics + " to " + sCompatDensity + " caller:" + Debug.getCallers(10));
            }
            compatInfo.applyToDisplayMetrics(outMetrics);
        }
    }

    public void updateCompatDensityIfNeed(int density) {
        int toDensity;
        if (sIsDisplayCompatApp && sCompatDensity != (toDensity = (int) (((float) density) * this.appInvScale))) {
            sCompatDensity = toDensity;
            if (DEBUG) {
                Log.i(TAG, "DisplayCompat: updateCompatDensityIfNeed from " + density + " to density=" + toDensity + " callers=" + Debug.getCallers(5));
            }
        }
    }

    public boolean supportDisplayCompat(String pkg, int uid) {
        IOppoScreenMode iOppoScreenMode = this.mScreenModeService;
        if (iOppoScreenMode != null) {
            try {
                sIsDisplayCompatApp = iOppoScreenMode.supportDisplayCompat(pkg, uid);
                if (DEBUG) {
                    Log.d(TAG, "supportDisplayCompat pkg " + pkg + ", " + sIsDisplayCompatApp);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "supportDisplayCompat failed!", e);
                sIsDisplayCompatApp = false;
            }
        } else {
            Log.w(TAG, "supportDisplayCompat service unavailable!!");
            sIsDisplayCompatApp = false;
        }
        return sIsDisplayCompatApp;
    }

    public boolean supportDisplayCompat() {
        if (DEBUG) {
            Log.d("IPswScreenModeFeature", "supportDisplayCompat  ");
        }
        return sIsDisplayCompatApp;
    }

    public int displayCompatDensity(int density) {
        if (DEBUG) {
            Log.d("IPswScreenModeFeature", "displayCompatDensity sCompatDensity=" + sCompatDensity);
        }
        return sCompatDensity;
    }

    public void setSupportDisplayCompat(boolean support) {
        if (DEBUG) {
            Log.d("IPswScreenModeFeature", "setSupportDisplayCompat support=" + support);
        }
        sIsDisplayCompatApp = support;
    }

    public void initDisplayCompat(ApplicationInfo ai) {
        OppoApplicationInfoEx oppoAppInfoEx = OppoApplicationInfoEx.getOppoAppInfoExFromAppInfoRef(ai);
        if (oppoAppInfoEx != null) {
            this.appScale = oppoAppInfoEx.getAppScale();
            this.appInvScale = oppoAppInfoEx.getAppInvScale();
            sCompatDensity = oppoAppInfoEx.getCompatDensity();
        }
        Log.d("IPswScreenModeFeature", "initDisplayCompat " + this.appScale + "," + this.appInvScale + "," + sCompatDensity);
    }

    public void updateCompatRealSize(DisplayInfo displayInfo, Point outSize) {
        if (sIsDisplayCompatApp) {
            outSize.x = (int) (((float) displayInfo.logicalWidth) * this.appInvScale);
            outSize.y = (int) (((float) displayInfo.logicalHeight) * this.appInvScale);
        }
    }
}
