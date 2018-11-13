package com.android.server.wm;

import android.app.IWallpaperManager;
import android.app.IWallpaperManager.Stub;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import android.view.DisplayInfo;
import com.android.server.LocationManagerService;
import com.android.server.display.OppoBrightUtils;
import com.mediatek.multiwindow.MultiWindowManager;
import java.io.PrintWriter;
import java.util.ArrayList;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class WallpaperController {
    private static final String TAG = null;
    private static final int WALLPAPER_DRAW_NORMAL = 0;
    private static final int WALLPAPER_DRAW_PENDING = 1;
    private static final long WALLPAPER_DRAW_PENDING_TIMEOUT_DURATION = 500;
    private static final int WALLPAPER_DRAW_TIMEOUT = 2;
    private static final long WALLPAPER_TIMEOUT = 150;
    private static final long WALLPAPER_TIMEOUT_RECOVERY = 10000;
    private WindowState mDeferredHideWallpaper;
    private final FindWallpaperTargetResult mFindResults;
    private int mLastWallpaperDisplayOffsetX;
    private int mLastWallpaperDisplayOffsetY;
    private long mLastWallpaperTimeoutTime;
    private float mLastWallpaperX;
    private float mLastWallpaperXStep;
    private float mLastWallpaperY;
    private float mLastWallpaperYStep;
    private WindowState mLowerWallpaperTarget;
    private final WindowManagerService mService;
    private WindowState mUpperWallpaperTarget;
    WindowState mWaitingOnWallpaper;
    private int mWallpaperAnimLayerAdjustment;
    private int mWallpaperDrawState;
    private IWallpaperManager mWallpaperManagerService;
    private WindowState mWallpaperTarget;
    private final ArrayList<WindowToken> mWallpaperTokens;

    private static final class FindWallpaperTargetResult {
        WindowState topWallpaper;
        int topWallpaperIndex;
        WindowState wallpaperTarget;
        int wallpaperTargetIndex;

        /* synthetic */ FindWallpaperTargetResult(FindWallpaperTargetResult findWallpaperTargetResult) {
            this();
        }

        private FindWallpaperTargetResult() {
            this.topWallpaperIndex = 0;
            this.topWallpaper = null;
            this.wallpaperTargetIndex = 0;
            this.wallpaperTarget = null;
        }

        void setTopWallpaper(WindowState win, int index) {
            this.topWallpaper = win;
            this.topWallpaperIndex = index;
        }

        void setWallpaperTarget(WindowState win, int index) {
            this.wallpaperTarget = win;
            this.wallpaperTargetIndex = index;
        }

        void reset() {
            this.topWallpaperIndex = 0;
            this.topWallpaper = null;
            this.wallpaperTargetIndex = 0;
            this.wallpaperTarget = null;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WallpaperController.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WallpaperController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WallpaperController.<clinit>():void");
    }

    public WallpaperController(WindowManagerService service) {
        this.mWallpaperTokens = new ArrayList();
        this.mWallpaperTarget = null;
        this.mLowerWallpaperTarget = null;
        this.mUpperWallpaperTarget = null;
        this.mLastWallpaperX = -1.0f;
        this.mLastWallpaperY = -1.0f;
        this.mLastWallpaperXStep = -1.0f;
        this.mLastWallpaperYStep = -1.0f;
        this.mLastWallpaperDisplayOffsetX = Integer.MIN_VALUE;
        this.mLastWallpaperDisplayOffsetY = Integer.MIN_VALUE;
        this.mDeferredHideWallpaper = null;
        this.mWallpaperDrawState = 0;
        this.mFindResults = new FindWallpaperTargetResult();
        this.mService = service;
    }

    WindowState getWallpaperTarget() {
        return this.mWallpaperTarget;
    }

    WindowState getLowerWallpaperTarget() {
        return this.mLowerWallpaperTarget;
    }

    WindowState getUpperWallpaperTarget() {
        return this.mUpperWallpaperTarget;
    }

    boolean isWallpaperTarget(WindowState win) {
        return win == this.mWallpaperTarget;
    }

    boolean isBelowWallpaperTarget(WindowState win) {
        return this.mWallpaperTarget != null && this.mWallpaperTarget.mLayer >= win.mBaseLayer;
    }

    boolean isWallpaperVisible() {
        return isWallpaperVisible(this.mWallpaperTarget);
    }

    private boolean isWallpaperVisible(WindowState wallpaperTarget) {
        if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
            Object obj;
            String str = TAG;
            StringBuilder append = new StringBuilder().append("Wallpaper vis: target ").append(wallpaperTarget).append(", obscured=").append(wallpaperTarget != null ? Boolean.toString(wallpaperTarget.mObscured) : "??").append(" anim=");
            if (wallpaperTarget == null || wallpaperTarget.mAppToken == null) {
                obj = null;
            } else {
                obj = wallpaperTarget.mAppToken.mAppAnimator.animation;
            }
            Slog.v(str, append.append(obj).append(" upper=").append(this.mUpperWallpaperTarget).append(" lower=").append(this.mLowerWallpaperTarget).toString());
        }
        if ((wallpaperTarget == null || (wallpaperTarget.mObscured && (wallpaperTarget.mAppToken == null || wallpaperTarget.mAppToken.mAppAnimator.animation == null))) && this.mUpperWallpaperTarget == null) {
            return this.mLowerWallpaperTarget != null;
        } else {
            return true;
        }
    }

    boolean isWallpaperTargetAnimating() {
        if (this.mWallpaperTarget == null || !this.mWallpaperTarget.mWinAnimator.isAnimationSet() || this.mWallpaperTarget.mWinAnimator.isDummyAnimation()) {
            return false;
        }
        return true;
    }

    void updateWallpaperVisibility() {
        DisplayContent displayContent = this.mWallpaperTarget.getDisplayContent();
        if (displayContent != null) {
            boolean visible = isWallpaperVisible(this.mWallpaperTarget);
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            int dw = displayInfo.logicalWidth;
            int dh = displayInfo.logicalHeight;
            for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
                WindowToken token = (WindowToken) this.mWallpaperTokens.get(curTokenNdx);
                if (token.hidden == visible) {
                    boolean z;
                    if (visible) {
                        z = false;
                    } else {
                        z = true;
                    }
                    token.hidden = z;
                    displayContent.layoutNeeded = true;
                }
                WindowList windows = token.windows;
                for (int wallpaperNdx = windows.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
                    WindowState wallpaper = (WindowState) windows.get(wallpaperNdx);
                    if (visible) {
                        updateWallpaperOffset(wallpaper, dw, dh, false);
                    }
                    dispatchWallpaperVisibility(wallpaper, visible);
                }
            }
        }
    }

    void hideDeferredWallpapersIfNeeded() {
        if (this.mDeferredHideWallpaper != null) {
            hideWallpapers(this.mDeferredHideWallpaper);
            this.mDeferredHideWallpaper = null;
        }
    }

    void hideWallpapers(WindowState winGoingAway) {
        if (this.mWallpaperTarget != null && (this.mWallpaperTarget != winGoingAway || this.mLowerWallpaperTarget != null)) {
            return;
        }
        if (this.mService.mAppTransition.isRunning()) {
            this.mDeferredHideWallpaper = winGoingAway;
            return;
        }
        boolean wasDeferred = this.mDeferredHideWallpaper == winGoingAway;
        for (int i = this.mWallpaperTokens.size() - 1; i >= 0; i--) {
            WindowToken token = (WindowToken) this.mWallpaperTokens.get(i);
            for (int j = token.windows.size() - 1; j >= 0; j--) {
                WindowState wallpaper = (WindowState) token.windows.get(j);
                WindowStateAnimator winAnimator = wallpaper.mWinAnimator;
                if (!winAnimator.mLastHidden || wasDeferred) {
                    winAnimator.hide("hideWallpapers");
                    dispatchWallpaperVisibility(wallpaper, false);
                    DisplayContent displayContent = wallpaper.getDisplayContent();
                    if (displayContent != null) {
                        displayContent.pendingLayoutChanges |= 4;
                    }
                }
            }
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT && !token.hidden) {
                Slog.d(TAG, "Hiding wallpaper " + token + " from " + winGoingAway + " target=" + this.mWallpaperTarget + " lower=" + this.mLowerWallpaperTarget + "\n" + Debug.getCallers(5, "  "));
            }
            token.hidden = true;
        }
    }

    void dispatchWallpaperVisibility(WindowState wallpaper, boolean visible) {
        if (wallpaper.mWallpaperVisible == visible) {
            return;
        }
        if (this.mDeferredHideWallpaper == null || visible) {
            wallpaper.mWallpaperVisible = visible;
            try {
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY || WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                    Slog.v(TAG, "Updating vis of wallpaper " + wallpaper + ": " + visible + " from:\n" + Debug.getCallers(4, "  "));
                }
                wallpaper.mClient.dispatchAppVisibility(visible);
                if (SystemProperties.get("ro.mtk_gmo_ram_optimize").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
                    if (this.mWallpaperManagerService == null) {
                        this.mWallpaperManagerService = Stub.asInterface(ServiceManager.getService("wallpaper"));
                    }
                    if (this.mWallpaperManagerService != null) {
                        this.mWallpaperManagerService.onVisibilityChanged(visible);
                    }
                }
            } catch (RemoteException e) {
            }
        }
    }

    boolean updateWallpaperOffset(WindowState wallpaperWin, int dw, int dh, boolean sync) {
        boolean rawChanged = false;
        float wpx = this.mLastWallpaperX >= OppoBrightUtils.MIN_LUX_LIMITI ? this.mLastWallpaperX : wallpaperWin.isRtl() ? 1.0f : OppoBrightUtils.MIN_LUX_LIMITI;
        float wpxs = this.mLastWallpaperXStep >= OppoBrightUtils.MIN_LUX_LIMITI ? this.mLastWallpaperXStep : -1.0f;
        int availw = (wallpaperWin.mFrame.right - wallpaperWin.mFrame.left) - dw;
        int offset = availw > 0 ? -((int) ((((float) availw) * wpx) + 0.5f)) : 0;
        if (this.mLastWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
            offset += this.mLastWallpaperDisplayOffsetX;
        }
        boolean changed = wallpaperWin.mXOffset != offset;
        if (changed) {
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                Slog.v(TAG, "Update wallpaper " + wallpaperWin + " x: " + offset);
            }
            wallpaperWin.mXOffset = offset;
        }
        if (!(wallpaperWin.mWallpaperX == wpx && wallpaperWin.mWallpaperXStep == wpxs)) {
            wallpaperWin.mWallpaperX = wpx;
            wallpaperWin.mWallpaperXStep = wpxs;
            rawChanged = true;
        }
        float wpy = this.mLastWallpaperY >= OppoBrightUtils.MIN_LUX_LIMITI ? this.mLastWallpaperY : 0.5f;
        float wpys = this.mLastWallpaperYStep >= OppoBrightUtils.MIN_LUX_LIMITI ? this.mLastWallpaperYStep : -1.0f;
        int availh = (wallpaperWin.mFrame.bottom - wallpaperWin.mFrame.top) - dh;
        offset = availh > 0 ? -((int) ((((float) availh) * wpy) + 0.5f)) : 0;
        if (this.mLastWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
            offset += this.mLastWallpaperDisplayOffsetY;
        }
        if (wallpaperWin.mYOffset != offset) {
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                Slog.v(TAG, "Update wallpaper " + wallpaperWin + " y: " + offset);
            }
            changed = true;
            wallpaperWin.mYOffset = offset;
        }
        if (!(wallpaperWin.mWallpaperY == wpy && wallpaperWin.mWallpaperYStep == wpys)) {
            wallpaperWin.mWallpaperY = wpy;
            wallpaperWin.mWallpaperYStep = wpys;
            rawChanged = true;
        }
        if (rawChanged && (wallpaperWin.mAttrs.privateFlags & 4) != 0) {
            try {
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                    Slog.v(TAG, "Report new wp offset " + wallpaperWin + " x=" + wallpaperWin.mWallpaperX + " y=" + wallpaperWin.mWallpaperY);
                }
                if (sync) {
                    this.mWaitingOnWallpaper = wallpaperWin;
                }
                wallpaperWin.mClient.dispatchWallpaperOffsets(wallpaperWin.mWallpaperX, wallpaperWin.mWallpaperY, wallpaperWin.mWallpaperXStep, wallpaperWin.mWallpaperYStep, sync);
                if (sync && this.mWaitingOnWallpaper != null) {
                    long start = SystemClock.uptimeMillis();
                    if (this.mLastWallpaperTimeoutTime + 10000 < start) {
                        try {
                            if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                                Slog.v(TAG, "Waiting for offset complete...");
                            }
                            this.mService.mWindowMap.wait(WALLPAPER_TIMEOUT);
                        } catch (InterruptedException e) {
                        }
                        if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                            Slog.v(TAG, "Offset complete!");
                        }
                        if (WALLPAPER_TIMEOUT + start < SystemClock.uptimeMillis()) {
                            Slog.i(TAG, "Timeout waiting for wallpaper to offset: " + wallpaperWin);
                            this.mLastWallpaperTimeoutTime = start;
                        }
                    }
                    this.mWaitingOnWallpaper = null;
                }
            } catch (RemoteException e2) {
            }
        }
        return changed;
    }

    void setWindowWallpaperPosition(WindowState window, float x, float y, float xStep, float yStep) {
        if (window.mWallpaperX != x || window.mWallpaperY != y) {
            window.mWallpaperX = x;
            window.mWallpaperY = y;
            window.mWallpaperXStep = xStep;
            window.mWallpaperYStep = yStep;
            updateWallpaperOffsetLocked(window, true);
        }
    }

    void setWindowWallpaperDisplayOffset(WindowState window, int x, int y) {
        if (window.mWallpaperDisplayOffsetX != x || window.mWallpaperDisplayOffsetY != y) {
            window.mWallpaperDisplayOffsetX = x;
            window.mWallpaperDisplayOffsetY = y;
            updateWallpaperOffsetLocked(window, true);
        }
    }

    Bundle sendWindowWallpaperCommand(WindowState window, String action, int x, int y, int z, Bundle extras, boolean sync) {
        if (window == this.mWallpaperTarget || window == this.mLowerWallpaperTarget || window == this.mUpperWallpaperTarget) {
            boolean doWait = sync;
            for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
                WindowList windows = ((WindowToken) this.mWallpaperTokens.get(curTokenNdx)).windows;
                for (int wallpaperNdx = windows.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
                    try {
                        ((WindowState) windows.get(wallpaperNdx)).mClient.dispatchWallpaperCommand(action, x, y, z, extras, sync);
                        sync = false;
                    } catch (RemoteException e) {
                    }
                }
            }
            if (doWait) {
            }
        }
        return null;
    }

    void updateWallpaperOffsetLocked(WindowState changingTarget, boolean sync) {
        DisplayContent displayContent = changingTarget.getDisplayContent();
        if (displayContent != null) {
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            int dw = displayInfo.logicalWidth;
            int dh = displayInfo.logicalHeight;
            WindowState target = this.mWallpaperTarget;
            if (target != null) {
                if (target.mWallpaperX >= OppoBrightUtils.MIN_LUX_LIMITI) {
                    this.mLastWallpaperX = target.mWallpaperX;
                } else if (changingTarget.mWallpaperX >= OppoBrightUtils.MIN_LUX_LIMITI) {
                    this.mLastWallpaperX = changingTarget.mWallpaperX;
                }
                if (target.mWallpaperY >= OppoBrightUtils.MIN_LUX_LIMITI) {
                    this.mLastWallpaperY = target.mWallpaperY;
                } else if (changingTarget.mWallpaperY >= OppoBrightUtils.MIN_LUX_LIMITI) {
                    this.mLastWallpaperY = changingTarget.mWallpaperY;
                }
                if (target.mWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
                    this.mLastWallpaperDisplayOffsetX = target.mWallpaperDisplayOffsetX;
                } else if (changingTarget.mWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
                    this.mLastWallpaperDisplayOffsetX = changingTarget.mWallpaperDisplayOffsetX;
                }
                if (target.mWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
                    this.mLastWallpaperDisplayOffsetY = target.mWallpaperDisplayOffsetY;
                } else if (changingTarget.mWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
                    this.mLastWallpaperDisplayOffsetY = changingTarget.mWallpaperDisplayOffsetY;
                }
                if (target.mWallpaperXStep >= OppoBrightUtils.MIN_LUX_LIMITI) {
                    this.mLastWallpaperXStep = target.mWallpaperXStep;
                } else if (changingTarget.mWallpaperXStep >= OppoBrightUtils.MIN_LUX_LIMITI) {
                    this.mLastWallpaperXStep = changingTarget.mWallpaperXStep;
                }
                if (target.mWallpaperYStep >= OppoBrightUtils.MIN_LUX_LIMITI) {
                    this.mLastWallpaperYStep = target.mWallpaperYStep;
                } else if (changingTarget.mWallpaperYStep >= OppoBrightUtils.MIN_LUX_LIMITI) {
                    this.mLastWallpaperYStep = changingTarget.mWallpaperYStep;
                }
            }
            for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
                WindowList windows = ((WindowToken) this.mWallpaperTokens.get(curTokenNdx)).windows;
                for (int wallpaperNdx = windows.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
                    WindowState wallpaper = (WindowState) windows.get(wallpaperNdx);
                    if (updateWallpaperOffset(wallpaper, dw, dh, sync)) {
                        WindowStateAnimator winAnimator = wallpaper.mWinAnimator;
                        winAnimator.computeShownFrameLocked();
                        winAnimator.setWallpaperOffset(wallpaper.mShownPosition);
                        sync = false;
                    }
                }
            }
        }
    }

    void clearLastWallpaperTimeoutTime() {
        this.mLastWallpaperTimeoutTime = 0;
    }

    void wallpaperCommandComplete(IBinder window) {
        if (this.mWaitingOnWallpaper != null && this.mWaitingOnWallpaper.mClient.asBinder() == window) {
            this.mWaitingOnWallpaper = null;
            this.mService.mWindowMap.notifyAll();
        }
    }

    void wallpaperOffsetsComplete(IBinder window) {
        if (this.mWaitingOnWallpaper != null && this.mWaitingOnWallpaper.mClient.asBinder() == window) {
            this.mWaitingOnWallpaper = null;
            this.mService.mWindowMap.notifyAll();
        }
    }

    int getAnimLayerAdjustment() {
        return this.mWallpaperAnimLayerAdjustment;
    }

    void setAnimLayerAdjustment(WindowState win, int adj) {
        if (win == this.mWallpaperTarget && this.mLowerWallpaperTarget == null) {
            if (WindowManagerDebugConfig.DEBUG_LAYERS || WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                Slog.v(TAG, "Setting wallpaper layer adj to " + adj);
            }
            this.mWallpaperAnimLayerAdjustment = adj;
            for (int i = this.mWallpaperTokens.size() - 1; i >= 0; i--) {
                WindowList windows = ((WindowToken) this.mWallpaperTokens.get(i)).windows;
                for (int j = windows.size() - 1; j >= 0; j--) {
                    WindowState wallpaper = (WindowState) windows.get(j);
                    wallpaper.mWinAnimator.mAnimLayer = wallpaper.mLayer + adj;
                    if (WindowManagerDebugConfig.DEBUG_LAYERS || WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                        Slog.v(TAG, "setWallpaper win " + wallpaper + " anim layer: " + wallpaper.mWinAnimator.mAnimLayer);
                    }
                }
            }
        }
    }

    private void findWallpaperTarget(WindowList windows, FindWallpaperTargetResult result) {
        WindowAnimator winAnimator = this.mService.mAnimator;
        result.reset();
        WindowState w = null;
        int windowDetachedI = -1;
        boolean resetTopWallpaper = false;
        boolean inFreeformSpace = false;
        boolean replacing = false;
        boolean keyguardGoingAwayWithWallpaper = false;
        for (int i = windows.size() - 1; i >= 0; i--) {
            w = (WindowState) windows.get(i);
            if (w.mAttrs.type != 2013) {
                resetTopWallpaper = true;
                if (w == winAnimator.mWindowDetachedWallpaper || w.mAppToken == null || !w.mAppToken.hidden || w.mAppToken.mAppAnimator.animation != null) {
                    int i2;
                    if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                        Slog.v(TAG, "Win #" + i + " " + w + ": isOnScreen=" + w.isOnScreen() + " mDrawState=" + w.mWinAnimator.mDrawState);
                    }
                    if (!inFreeformSpace) {
                        TaskStack stack = w.getStack();
                        inFreeformSpace = stack != null && stack.mStackId == 2;
                    }
                    replacing |= w.mWillReplaceWindow;
                    if (w.mAppToken != null) {
                        i2 = w.mWinAnimator.mKeyguardGoingAwayWithWallpaper;
                    } else {
                        i2 = 0;
                    }
                    keyguardGoingAwayWithWallpaper |= i2;
                    if (((w.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) && w.isOnScreen() && (this.mWallpaperTarget == w || w.isDrawFinishedLw())) {
                        if (!MultiWindowManager.isSupported() || !inFreeformSpace) {
                            if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                                Slog.v(TAG, "Found wallpaper target: #" + i + "=" + w);
                            }
                            result.setWallpaperTarget(w, i);
                            if (w != this.mWallpaperTarget || !w.mWinAnimator.isAnimationSet()) {
                                break;
                            } else if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                                Slog.v(TAG, "Win " + w + ": token animating, looking behind.");
                            }
                        }
                    } else if (w == winAnimator.mWindowDetachedWallpaper) {
                        windowDetachedI = i;
                    }
                } else if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                    Slog.v(TAG, "Skipping hidden and not animating token: " + w);
                }
            } else if (result.topWallpaper == null || resetTopWallpaper) {
                result.setTopWallpaper(w, i);
                resetTopWallpaper = false;
            }
        }
        if (result.wallpaperTarget == null) {
            if (windowDetachedI >= 0) {
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                    Slog.v(TAG, "Found animating detached wallpaper activity: #" + windowDetachedI + "=" + w);
                }
                result.setWallpaperTarget(w, windowDetachedI);
            } else if (inFreeformSpace || (replacing && this.mWallpaperTarget != null)) {
                result.setWallpaperTarget(result.topWallpaper, result.topWallpaperIndex);
            } else if (keyguardGoingAwayWithWallpaper) {
                result.setWallpaperTarget(result.topWallpaper, result.topWallpaperIndex);
            }
        }
    }

    private boolean updateWallpaperWindowsTarget(WindowList windows, FindWallpaperTargetResult result) {
        boolean targetChanged = false;
        WindowState wallpaperTarget = result.wallpaperTarget;
        int wallpaperTargetIndex = result.wallpaperTargetIndex;
        if (this.mWallpaperTarget != wallpaperTarget && (this.mLowerWallpaperTarget == null || this.mLowerWallpaperTarget != wallpaperTarget)) {
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                Slog.v(TAG, "New wallpaper target: " + wallpaperTarget + " oldTarget: " + this.mWallpaperTarget);
            }
            this.mLowerWallpaperTarget = null;
            this.mUpperWallpaperTarget = null;
            WindowState oldW = this.mWallpaperTarget;
            this.mWallpaperTarget = wallpaperTarget;
            targetChanged = true;
            if (!(wallpaperTarget == null || oldW == null)) {
                boolean oldAnim = oldW.isAnimatingLw();
                boolean foundAnim = wallpaperTarget.isAnimatingLw();
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                    Slog.v(TAG, "New animation: " + foundAnim + " old animation: " + oldAnim);
                }
                if (foundAnim && oldAnim) {
                    int oldI = windows.indexOf(oldW);
                    if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                        Slog.v(TAG, "New i: " + wallpaperTargetIndex + " old i: " + oldI);
                    }
                    if (oldI >= 0) {
                        boolean newTargetHidden = wallpaperTarget.mAppToken != null ? wallpaperTarget.mAppToken.hiddenRequested : false;
                        boolean oldTargetHidden = oldW.mAppToken != null ? oldW.mAppToken.hiddenRequested : false;
                        if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                            Slog.v(TAG, "Animating wallpapers: old#" + oldI + "=" + oldW + " hidden=" + oldTargetHidden + " new#" + wallpaperTargetIndex + "=" + wallpaperTarget + " hidden=" + newTargetHidden);
                        }
                        if (wallpaperTargetIndex > oldI) {
                            if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                                Slog.v(TAG, "Found target above old target.");
                            }
                            this.mUpperWallpaperTarget = wallpaperTarget;
                            this.mLowerWallpaperTarget = oldW;
                            wallpaperTarget = oldW;
                            wallpaperTargetIndex = oldI;
                        } else {
                            if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                                Slog.v(TAG, "Found target below old target.");
                            }
                            this.mUpperWallpaperTarget = oldW;
                            this.mLowerWallpaperTarget = wallpaperTarget;
                        }
                        if (newTargetHidden && !oldTargetHidden) {
                            if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                                Slog.v(TAG, "Old wallpaper still the target.");
                            }
                            this.mWallpaperTarget = oldW;
                        } else if (newTargetHidden == oldTargetHidden && !this.mService.mOpeningApps.contains(wallpaperTarget.mAppToken) && (this.mService.mOpeningApps.contains(oldW.mAppToken) || this.mService.mClosingApps.contains(oldW.mAppToken))) {
                            this.mWallpaperTarget = oldW;
                        }
                    }
                }
            }
        } else if (!(this.mLowerWallpaperTarget == null || (this.mLowerWallpaperTarget.isAnimatingLw() && this.mUpperWallpaperTarget.isAnimatingLw()))) {
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                Slog.v(TAG, "No longer animating wallpaper targets!");
            }
            this.mLowerWallpaperTarget = null;
            this.mUpperWallpaperTarget = null;
            this.mWallpaperTarget = wallpaperTarget;
            targetChanged = true;
        }
        result.setWallpaperTarget(wallpaperTarget, wallpaperTargetIndex);
        return targetChanged;
    }

    boolean updateWallpaperWindowsTargetByLayer(WindowList windows, FindWallpaperTargetResult result) {
        boolean visible;
        int i = 0;
        WindowState wallpaperTarget = result.wallpaperTarget;
        int wallpaperTargetIndex = result.wallpaperTargetIndex;
        if (wallpaperTarget != null) {
            visible = true;
        } else {
            visible = false;
        }
        if (visible) {
            visible = isWallpaperVisible(wallpaperTarget);
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                Slog.v(TAG, "Wallpaper visibility: " + visible);
            }
            if (this.mLowerWallpaperTarget == null && wallpaperTarget.mAppToken != null) {
                i = wallpaperTarget.mAppToken.mAppAnimator.animLayerAdjustment;
            }
            this.mWallpaperAnimLayerAdjustment = i;
            int maxLayer = (this.mService.mPolicy.getMaxWallpaperLayer() * 10000) + 1000;
            while (wallpaperTargetIndex > 0) {
                WindowState wb = (WindowState) windows.get(wallpaperTargetIndex - 1);
                if (wb.mBaseLayer < maxLayer && wb.mAttachedWindow != wallpaperTarget && ((wallpaperTarget.mAttachedWindow == null || wb.mAttachedWindow != wallpaperTarget.mAttachedWindow) && (wb.mAttrs.type != 3 || wallpaperTarget.mToken == null || wb.mToken != wallpaperTarget.mToken))) {
                    break;
                }
                wallpaperTarget = wb;
                wallpaperTargetIndex--;
            }
        } else if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
            Slog.v(TAG, "No wallpaper target");
        }
        result.setWallpaperTarget(wallpaperTarget, wallpaperTargetIndex);
        return visible;
    }

    boolean updateWallpaperWindowsPlacement(WindowList windows, WindowState wallpaperTarget, int wallpaperTargetIndex, boolean visible) {
        DisplayInfo displayInfo = this.mService.getDefaultDisplayContentLocked().getDisplayInfo();
        int dw = displayInfo.logicalWidth;
        int dh = displayInfo.logicalHeight;
        try {
            if (SystemProperties.get("ro.mtk_gmo_ram_optimize").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) && !hasAnyWallpaperLock() && visible) {
                if (this.mWallpaperManagerService == null) {
                    this.mWallpaperManagerService = Stub.asInterface(ServiceManager.getService("wallpaper"));
                }
                if (this.mWallpaperManagerService != null) {
                    this.mWallpaperManagerService.onVisibilityChanged(true);
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "WALLPAPER_SERVICE onVisibilityChanged error: ", e);
        }
        boolean changed = false;
        for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
            WindowToken token = (WindowToken) this.mWallpaperTokens.get(curTokenNdx);
            if (token.hidden == visible) {
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                    Slog.d(TAG, "Wallpaper token " + token + " hidden=" + (!visible));
                }
                token.hidden = !visible;
                this.mService.getDefaultDisplayContentLocked().layoutNeeded = true;
            }
            WindowList tokenWindows = token.windows;
            for (int wallpaperNdx = tokenWindows.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
                WindowState wallpaper = (WindowState) tokenWindows.get(wallpaperNdx);
                if (visible) {
                    updateWallpaperOffset(wallpaper, dw, dh, false);
                }
                dispatchWallpaperVisibility(wallpaper, visible);
                wallpaper.mWinAnimator.mAnimLayer = wallpaper.mLayer + this.mWallpaperAnimLayerAdjustment;
                if (WindowManagerDebugConfig.DEBUG_LAYERS || WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                    Slog.v(TAG, "adjustWallpaper win " + wallpaper + " anim layer: " + wallpaper.mWinAnimator.mAnimLayer);
                }
                if (wallpaper == wallpaperTarget) {
                    wallpaperTargetIndex--;
                    if (wallpaperTargetIndex > 0) {
                        wallpaperTarget = (WindowState) windows.get(wallpaperTargetIndex - 1);
                    } else {
                        wallpaperTarget = null;
                    }
                } else {
                    int oldIndex = windows.indexOf(wallpaper);
                    if (oldIndex >= 0) {
                        if (WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT) {
                            Slog.v(TAG, "Wallpaper removing at " + oldIndex + ": " + wallpaper);
                        }
                        windows.remove(oldIndex);
                        this.mService.mWindowsChanged = true;
                        if (oldIndex < wallpaperTargetIndex) {
                            wallpaperTargetIndex--;
                        }
                    }
                    int insertionIndex = 0;
                    if (visible && wallpaperTarget != null) {
                        int type = wallpaperTarget.mAttrs.type;
                        if ((wallpaperTarget.mAttrs.privateFlags & 1024) != 0 || type == 2029) {
                            insertionIndex = Math.min(windows.indexOf(wallpaperTarget), findLowestWindowOnScreen(windows));
                        }
                    }
                    if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT || WindowManagerDebugConfig.DEBUG_WINDOW_MOVEMENT || (WindowManagerDebugConfig.DEBUG_ADD_REMOVE && oldIndex != insertionIndex)) {
                        Slog.v(TAG, "Moving wallpaper " + wallpaper + " from " + oldIndex + " to " + insertionIndex);
                    }
                    windows.add(insertionIndex, wallpaper);
                    this.mService.mWindowsChanged = true;
                    changed = true;
                }
            }
        }
        return changed;
    }

    private int findLowestWindowOnScreen(WindowList windows) {
        int size = windows.size();
        for (int index = 0; index < size; index++) {
            if (((WindowState) windows.get(index)).isOnScreen()) {
                return index;
            }
        }
        return Integer.MAX_VALUE;
    }

    boolean adjustWallpaperWindows() {
        this.mService.mWindowPlacerLocked.mWallpaperMayChange = false;
        WindowList windows = this.mService.getDefaultWindowListLocked();
        findWallpaperTarget(windows, this.mFindResults);
        boolean targetChanged = updateWallpaperWindowsTarget(windows, this.mFindResults);
        boolean visible = updateWallpaperWindowsTargetByLayer(windows, this.mFindResults);
        WindowState wallpaperTarget = this.mFindResults.wallpaperTarget;
        int wallpaperTargetIndex = this.mFindResults.wallpaperTargetIndex;
        if (wallpaperTarget != null || this.mFindResults.topWallpaper == null) {
            wallpaperTarget = wallpaperTargetIndex > 0 ? (WindowState) windows.get(wallpaperTargetIndex - 1) : null;
        } else {
            wallpaperTarget = this.mFindResults.topWallpaper;
            wallpaperTargetIndex = this.mFindResults.topWallpaperIndex + 1;
        }
        if (visible) {
            if (this.mWallpaperTarget.mWallpaperX >= OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mLastWallpaperX = this.mWallpaperTarget.mWallpaperX;
                this.mLastWallpaperXStep = this.mWallpaperTarget.mWallpaperXStep;
            }
            if (this.mWallpaperTarget.mWallpaperY >= OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mLastWallpaperY = this.mWallpaperTarget.mWallpaperY;
                this.mLastWallpaperYStep = this.mWallpaperTarget.mWallpaperYStep;
            }
            if (this.mWallpaperTarget.mWallpaperDisplayOffsetX != Integer.MIN_VALUE) {
                this.mLastWallpaperDisplayOffsetX = this.mWallpaperTarget.mWallpaperDisplayOffsetX;
            }
            if (this.mWallpaperTarget.mWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
                this.mLastWallpaperDisplayOffsetY = this.mWallpaperTarget.mWallpaperDisplayOffsetY;
            }
        }
        boolean changed = updateWallpaperWindowsPlacement(windows, wallpaperTarget, wallpaperTargetIndex, visible);
        if (targetChanged && WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
            Slog.d(TAG, "New wallpaper: target=" + this.mWallpaperTarget + " lower=" + this.mLowerWallpaperTarget + " upper=" + this.mUpperWallpaperTarget);
        }
        return changed;
    }

    boolean processWallpaperDrawPendingTimeout() {
        if (this.mWallpaperDrawState != 1) {
            return false;
        }
        this.mWallpaperDrawState = 2;
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_WALLPAPER) {
            Slog.v(TAG, "*** WALLPAPER DRAW TIMEOUT");
        }
        return true;
    }

    boolean wallpaperTransitionReady() {
        boolean transitionReady = true;
        boolean wallpaperReady = true;
        for (int curTokenIndex = this.mWallpaperTokens.size() - 1; curTokenIndex >= 0 && wallpaperReady; curTokenIndex--) {
            WindowToken token = (WindowToken) this.mWallpaperTokens.get(curTokenIndex);
            int curWallpaperIndex = token.windows.size() - 1;
            while (curWallpaperIndex >= 0) {
                WindowState wallpaper = (WindowState) token.windows.get(curWallpaperIndex);
                if (!wallpaper.mWallpaperVisible || wallpaper.isDrawnLw()) {
                    curWallpaperIndex--;
                } else {
                    wallpaperReady = false;
                    if (this.mWallpaperDrawState != 2) {
                        transitionReady = false;
                    }
                    if (this.mWallpaperDrawState == 0) {
                        this.mWallpaperDrawState = 1;
                        this.mService.mH.removeMessages(39);
                        this.mService.mH.sendEmptyMessageDelayed(39, 500);
                    }
                    if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                        Slog.v(TAG, "Wallpaper should be visible but has not been drawn yet. mWallpaperDrawState=" + this.mWallpaperDrawState);
                    } else {
                    }
                }
            }
        }
        if (wallpaperReady) {
            this.mWallpaperDrawState = 0;
            this.mService.mH.removeMessages(39);
        }
        return transitionReady;
    }

    void addWallpaperToken(WindowToken token) {
        this.mWallpaperTokens.add(token);
    }

    void removeWallpaperToken(WindowToken token) {
        this.mWallpaperTokens.remove(token);
    }

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mWallpaperTarget=");
        pw.println(this.mWallpaperTarget);
        if (!(this.mLowerWallpaperTarget == null && this.mUpperWallpaperTarget == null)) {
            pw.print(prefix);
            pw.print("mLowerWallpaperTarget=");
            pw.println(this.mLowerWallpaperTarget);
            pw.print(prefix);
            pw.print("mUpperWallpaperTarget=");
            pw.println(this.mUpperWallpaperTarget);
        }
        pw.print(prefix);
        pw.print("mLastWallpaperX=");
        pw.print(this.mLastWallpaperX);
        pw.print(" mLastWallpaperY=");
        pw.println(this.mLastWallpaperY);
        if (this.mLastWallpaperDisplayOffsetX != Integer.MIN_VALUE || this.mLastWallpaperDisplayOffsetY != Integer.MIN_VALUE) {
            pw.print(prefix);
            pw.print("mLastWallpaperDisplayOffsetX=");
            pw.print(this.mLastWallpaperDisplayOffsetX);
            pw.print(" mLastWallpaperDisplayOffsetY=");
            pw.println(this.mLastWallpaperDisplayOffsetY);
        }
    }

    void dumpTokens(PrintWriter pw, String prefix, boolean dumpAll) {
        if (!this.mWallpaperTokens.isEmpty()) {
            pw.println();
            pw.print(prefix);
            pw.println("Wallpaper tokens:");
            for (int i = this.mWallpaperTokens.size() - 1; i >= 0; i--) {
                WindowToken token = (WindowToken) this.mWallpaperTokens.get(i);
                pw.print(prefix);
                pw.print("Wallpaper #");
                pw.print(i);
                pw.print(' ');
                pw.print(token);
                if (dumpAll) {
                    pw.println(':');
                    token.dump(pw, "    ");
                } else {
                    pw.println();
                }
            }
        }
    }

    private boolean hasAnyWallpaperLock() {
        for (int i = this.mWallpaperTokens.size(); i > 0; i--) {
            if (((WindowToken) this.mWallpaperTokens.get(i - 1)).windows.size() > 0) {
                return true;
            }
        }
        return false;
    }
}
