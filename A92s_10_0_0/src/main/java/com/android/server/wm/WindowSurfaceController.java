package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Debug;
import android.os.IBinder;
import android.os.Trace;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowContentFrameStats;
import com.android.server.am.IColorCommonListManager;
import com.android.server.display.OppoBrightUtils;
import com.android.server.util.ColorZoomWindowManagerHelper;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public class WindowSurfaceController {
    static final String TAG = "WindowManager";
    final WindowStateAnimator mAnimator;
    private boolean mHiddenForCrop = false;
    private boolean mHiddenForOtherReasons = true;
    private float mLastDsdx = 1.0f;
    private float mLastDsdy = OppoBrightUtils.MIN_LUX_LIMITI;
    private float mLastDtdx = OppoBrightUtils.MIN_LUX_LIMITI;
    private float mLastDtdy = 1.0f;
    private final WindowManagerService mService;
    private float mSurfaceAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
    SurfaceControl mSurfaceControl;
    private Rect mSurfaceCrop = new Rect(0, 0, -1, -1);
    private int mSurfaceH = 0;
    private int mSurfaceLayer = 0;
    private boolean mSurfaceShown = false;
    private int mSurfaceW = 0;
    private float mSurfaceX = OppoBrightUtils.MIN_LUX_LIMITI;
    private float mSurfaceY = OppoBrightUtils.MIN_LUX_LIMITI;
    private final SurfaceControl.Transaction mTmpTransaction = new SurfaceControl.Transaction();
    private final Session mWindowSession;
    private final int mWindowType;
    private final String title;

    public WindowSurfaceController(SurfaceSession s, String name, int w, int h, int format, int flags, WindowStateAnimator animator, int windowType, int ownerUid) {
        this.mAnimator = animator;
        this.mSurfaceW = w;
        this.mSurfaceH = h;
        this.title = name;
        this.mService = animator.mService;
        WindowState win = animator.mWin;
        this.mWindowType = windowType;
        this.mWindowSession = win.mSession;
        Trace.traceBegin(32, "new SurfaceControl");
        this.mSurfaceControl = win.makeSurface().setParent(win.getSurfaceControl()).setName(name).setBufferSize(w, h).setFormat(format).setFlags(flags).setMetadata(2, windowType).setMetadata(1, ownerUid).build();
        Trace.traceEnd(32);
    }

    private void logSurface(String msg, RuntimeException where) {
        String str = "  SURFACE " + msg + ": " + this.title;
        if (where != null) {
            Slog.i("WindowManager", str, where);
        } else {
            Slog.i("WindowManager", str);
        }
    }

    /* access modifiers changed from: package-private */
    public void reparentChildrenInTransaction(WindowSurfaceController other) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            Slog.i("WindowManager", "REPARENT from: " + this + " to: " + other);
        }
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null && other.mSurfaceControl != null) {
            surfaceControl.reparentChildren(other.getHandle());
        }
    }

    /* access modifiers changed from: package-private */
    public void detachChildren() {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            Slog.i("WindowManager", "SEVER CHILDREN");
        }
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.detachChildren();
        }
    }

    /* access modifiers changed from: package-private */
    public void hide(SurfaceControl.Transaction transaction, String reason) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            logSurface("HIDE ( " + reason + " )", null);
        }
        this.mHiddenForOtherReasons = true;
        this.mAnimator.destroyPreservedSurfaceLocked();
        if (this.mSurfaceShown) {
            hideSurface(transaction);
        }
    }

    private void hideSurface(SurfaceControl.Transaction transaction) {
        if (this.mSurfaceControl != null) {
            setShown(false);
            try {
                transaction.hide(this.mSurfaceControl);
            } catch (RuntimeException e) {
                Slog.w("WindowManager", "Exception hiding surface in " + this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void destroyNotInTransaction() {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.SHOW_SURFACE_ALLOC || WindowManagerDebugConfig.DEBUG_WMS) {
            Slog.i("WindowManager", "Destroying surface " + this + " called by " + Debug.getCallers(8));
        }
        try {
            if (this.mSurfaceControl != null) {
                this.mSurfaceControl.remove();
            }
        } catch (RuntimeException e) {
            Slog.w("WindowManager", "Error destroying surface in: " + this, e);
        } catch (Throwable th) {
            setShown(false);
            this.mSurfaceControl = null;
            throw th;
        }
        setShown(false);
        this.mSurfaceControl = null;
    }

    /* access modifiers changed from: package-private */
    public void setCropInTransaction(Rect clipRect, boolean recoveringMemory) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            logSurface("CROP " + clipRect.toShortString(), null);
        }
        try {
            if (clipRect.width() <= 0 || clipRect.height() <= 0) {
                this.mHiddenForCrop = true;
                this.mAnimator.destroyPreservedSurfaceLocked();
                updateVisibility();
                return;
            }
            if (!clipRect.equals(this.mSurfaceCrop)) {
                this.mSurfaceControl.setWindowCrop(clipRect);
                this.mSurfaceCrop.set(clipRect);
            }
            this.mHiddenForCrop = false;
            updateVisibility();
        } catch (RuntimeException e) {
            Slog.w("WindowManager", "Error setting crop surface of " + this + " crop=" + clipRect.toShortString(), e);
            if (!recoveringMemory) {
                this.mAnimator.reclaimSomeSurfaceMemory("crop", true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearCropInTransaction(boolean recoveringMemory) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            logSurface("CLEAR CROP", null);
        }
        try {
            Rect clipRect = new Rect(0, 0, -1, -1);
            if (!this.mSurfaceCrop.equals(clipRect)) {
                this.mSurfaceControl.setWindowCrop(clipRect);
                this.mSurfaceCrop.set(clipRect);
            }
        } catch (RuntimeException e) {
            Slog.w("WindowManager", "Error setting clearing crop of " + this, e);
            if (!recoveringMemory) {
                this.mAnimator.reclaimSomeSurfaceMemory("crop", true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPositionInTransaction(float left, float top, boolean recoveringMemory) {
        setPosition(null, left, top, recoveringMemory);
    }

    /* access modifiers changed from: package-private */
    public void setPosition(SurfaceControl.Transaction t, float left, float top, boolean recoveringMemory) {
        if ((this.mSurfaceX == left && this.mSurfaceY == top) ? false : true) {
            this.mSurfaceX = left;
            this.mSurfaceY = top;
            try {
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    logSurface("POS (setPositionInTransaction) @ (" + left + "," + top + ")", null);
                }
                if (t == null) {
                    this.mSurfaceControl.setPosition(left, top);
                } else {
                    t.setPosition(this.mSurfaceControl, left, top);
                }
            } catch (RuntimeException e) {
                Slog.w("WindowManager", "Error positioning surface of " + this + " pos=(" + left + "," + top + ")", e);
                if (!recoveringMemory) {
                    this.mAnimator.reclaimSomeSurfaceMemory("position", true);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setGeometryAppliesWithResizeInTransaction(boolean recoveringMemory) {
        this.mSurfaceControl.setGeometryAppliesWithResize();
    }

    /* access modifiers changed from: package-private */
    public void setMatrixInTransaction(float dsdx, float dtdx, float dtdy, float dsdy, boolean recoveringMemory) {
        setMatrix(null, dsdx, dtdx, dtdy, dsdy, false);
    }

    /* access modifiers changed from: package-private */
    public void setMatrix(SurfaceControl.Transaction t, float dsdx, float dtdx, float dtdy, float dsdy, boolean recoveringMemory) {
        Throwable th;
        if ((this.mLastDsdx == dsdx && this.mLastDtdx == dtdx && this.mLastDtdy == dtdy && this.mLastDsdy == dsdy) ? false : true) {
            this.mLastDsdx = dsdx;
            this.mLastDtdx = dtdx;
            this.mLastDtdy = dtdy;
            this.mLastDsdy = dsdy;
            try {
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    logSurface("MATRIX [" + dsdx + "," + dtdx + "," + dtdy + "," + dsdy + "]", null);
                }
                if (t == null) {
                    this.mSurfaceControl.setMatrix(dsdx, dtdx, dtdy, dsdy);
                    return;
                }
                th = null;
                try {
                    t.setMatrix(this.mSurfaceControl, dsdx, dtdx, dtdy, dsdy);
                } catch (RuntimeException e) {
                }
            } catch (RuntimeException e2) {
                th = null;
                Slog.e("WindowManager", "Error setting matrix on surface surface" + this.title + " MATRIX [" + dsdx + "," + dtdx + "," + dtdy + "," + dsdy + "]", th);
                if (!recoveringMemory) {
                    this.mAnimator.reclaimSomeSurfaceMemory("matrix", true);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setBufferSizeInTransaction(int width, int height, boolean recoveringMemory) {
        if (!((this.mSurfaceW == width && this.mSurfaceH == height) ? false : true)) {
            return false;
        }
        this.mSurfaceW = width;
        this.mSurfaceH = height;
        try {
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                logSurface("SIZE " + width + "x" + height, null);
            }
            this.mSurfaceControl.setBufferSize(width, height);
            return true;
        } catch (RuntimeException e) {
            Slog.e("WindowManager", "Error resizing surface of " + this.title + " size=(" + width + "x" + height + ")", e);
            if (!recoveringMemory) {
                this.mAnimator.reclaimSomeSurfaceMemory("size", true);
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean prepareToShowInTransaction(float alpha, float dsdx, float dtdx, float dsdy, float dtdy, boolean recoveringMemory) {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            try {
                this.mSurfaceAlpha = alpha;
                surfaceControl.setAlpha(alpha);
                this.mLastDsdx = dsdx;
                this.mLastDtdx = dtdx;
                this.mLastDsdy = dsdy;
                this.mLastDtdy = dtdy;
                this.mSurfaceControl.setMatrix(dsdx, dtdx, dsdy, dtdy);
            } catch (RuntimeException e) {
                Slog.w("WindowManager", "Error updating surface in " + this.title, e);
                if (recoveringMemory) {
                    return false;
                }
                this.mAnimator.reclaimSomeSurfaceMemory("update", true);
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setTransparentRegionHint(Region region) {
        if (this.mSurfaceControl == null) {
            Slog.w("WindowManager", "setTransparentRegionHint: null mSurface after mHasSurface true");
            return;
        }
        if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
            Slog.i("WindowManager", ">>> OPEN TRANSACTION setTransparentRegion");
        }
        this.mService.openSurfaceTransaction();
        try {
            this.mSurfaceControl.setTransparentRegionHint(region);
        } finally {
            this.mService.closeSurfaceTransaction("setTransparentRegion");
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", "<<< CLOSE TRANSACTION setTransparentRegion");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setOpaque(boolean isOpaque) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            logSurface("isOpaque=" + isOpaque, null);
        }
        if (this.mSurfaceControl != null) {
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", ">>> OPEN TRANSACTION setOpaqueLocked");
            }
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setOpaque(isOpaque);
            } finally {
                this.mService.closeSurfaceTransaction("setOpaqueLocked");
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION setOpaqueLocked");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setSecure(boolean isSecure) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            logSurface("isSecure=" + isSecure, null);
        }
        if (this.mSurfaceControl != null) {
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", ">>> OPEN TRANSACTION setSecureLocked");
            }
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setSecure(isSecure);
            } finally {
                this.mService.closeSurfaceTransaction("setSecure");
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION setSecureLocked");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setColorSpaceAgnostic(boolean agnostic) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            logSurface("isColorSpaceAgnostic=" + agnostic, null);
        }
        if (this.mSurfaceControl != null) {
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", ">>> OPEN TRANSACTION setColorSpaceAgnosticLocked");
            }
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setColorSpaceAgnostic(agnostic);
            } finally {
                this.mService.closeSurfaceTransaction("setColorSpaceAgnostic");
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i("WindowManager", "<<< CLOSE TRANSACTION setColorSpaceAgnosticLocked");
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void getContainerRect(Rect rect) {
        this.mAnimator.getContainerRect(rect);
    }

    /* access modifiers changed from: package-private */
    public boolean showRobustlyInTransaction() {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            logSurface("SHOW (performLayout)", null);
        }
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v("WindowManager", "Showing " + this + " during relayout");
        }
        this.mHiddenForOtherReasons = false;
        return updateVisibility();
    }

    private boolean updateVisibility() {
        if (this.mHiddenForCrop || this.mHiddenForOtherReasons) {
            if (!this.mSurfaceShown) {
                return false;
            }
            hideSurface(this.mTmpTransaction);
            SurfaceControl.mergeToGlobalTransaction(this.mTmpTransaction);
            return false;
        } else if (!this.mSurfaceShown) {
            return showSurface();
        } else {
            return true;
        }
    }

    private boolean showSurface() {
        try {
            setShown(true);
            this.mSurfaceControl.show();
            return true;
        } catch (RuntimeException e) {
            Slog.w("WindowManager", "Failure showing surface " + this.mSurfaceControl + " in " + this, e);
            this.mAnimator.reclaimSomeSurfaceMemory("show", true);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void deferTransactionUntil(IBinder handle, long frame) {
        this.mSurfaceControl.deferTransactionUntil(handle, frame);
    }

    /* access modifiers changed from: package-private */
    public void forceScaleableInTransaction(boolean force) {
        this.mSurfaceControl.setOverrideScalingMode(force ? 1 : -1);
    }

    /* access modifiers changed from: package-private */
    public boolean clearWindowContentFrameStats() {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl == null) {
            return false;
        }
        return surfaceControl.clearContentFrameStats();
    }

    /* access modifiers changed from: package-private */
    public boolean getWindowContentFrameStats(WindowContentFrameStats outStats) {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl == null) {
            return false;
        }
        return surfaceControl.getContentFrameStats(outStats);
    }

    /* access modifiers changed from: package-private */
    public boolean hasSurface() {
        return this.mSurfaceControl != null;
    }

    /* access modifiers changed from: package-private */
    public IBinder getHandle() {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl == null) {
            return null;
        }
        return surfaceControl.getHandle();
    }

    /* access modifiers changed from: package-private */
    public void getSurfaceControl(SurfaceControl outSurfaceControl) {
        outSurfaceControl.copyFrom(this.mSurfaceControl);
    }

    /* access modifiers changed from: package-private */
    public int getLayer() {
        return this.mSurfaceLayer;
    }

    /* access modifiers changed from: package-private */
    public boolean getShown() {
        return this.mSurfaceShown;
    }

    /* access modifiers changed from: package-private */
    public void setShown(boolean surfaceShown) {
        Session session;
        this.mSurfaceShown = surfaceShown;
        this.mService.updateNonSystemOverlayWindowsVisibilityIfNeeded(this.mAnimator.mWin, surfaceShown);
        Session session2 = this.mWindowSession;
        if (session2 != null) {
            session2.onWindowSurfaceVisibilityChanged(this, this.mSurfaceShown, this.mWindowType);
        }
        if (!(this.mAnimator.mWin == null || (session = this.mWindowSession) == null || session.mUid <= 10000)) {
            try {
                OppoFeatureCache.get(IColorCommonListManager.DEFAULT).updateWindowState(this.mWindowSession.mUid, this.mWindowSession.mPid, this.mAnimator.mWin.hashCode(), this.mAnimator.mWin.mAttrs.type, this.mAnimator.mWin.getHasSurface(), surfaceShown);
            } catch (Exception e) {
            }
        }
        WindowStateAnimator windowStateAnimator = this.mAnimator;
        if (windowStateAnimator != null && windowStateAnimator.mWin != null) {
            this.mAnimator.mWin.notifyImeWindowStateChange(surfaceShown);
            ColorZoomWindowManagerHelper.getInstance();
            ColorZoomWindowManagerHelper.getZoomWindowManager().updateInputVisibility(this.mAnimator.mWin, surfaceShown);
        }
    }

    /* access modifiers changed from: package-private */
    public float getX() {
        return this.mSurfaceX;
    }

    /* access modifiers changed from: package-private */
    public float getY() {
        return this.mSurfaceY;
    }

    /* access modifiers changed from: package-private */
    public int getWidth() {
        return this.mSurfaceW;
    }

    /* access modifiers changed from: package-private */
    public int getHeight() {
        return this.mSurfaceH;
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1133871366145L, this.mSurfaceShown);
        proto.write(1120986464258L, this.mSurfaceLayer);
        proto.end(token);
    }

    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mSurface=");
            pw.println(this.mSurfaceControl);
        }
        pw.print(prefix);
        pw.print("Surface: shown=");
        pw.print(this.mSurfaceShown);
        pw.print(" layer=");
        pw.print(this.mSurfaceLayer);
        pw.print(" alpha=");
        pw.print(this.mSurfaceAlpha);
        pw.print(" rect=(");
        pw.print(this.mSurfaceX);
        pw.print(",");
        pw.print(this.mSurfaceY);
        pw.print(") ");
        pw.print(this.mSurfaceW);
        pw.print(" x ");
        pw.print(this.mSurfaceH);
        pw.print(" transform=(");
        pw.print(this.mLastDsdx);
        pw.print(", ");
        pw.print(this.mLastDtdx);
        pw.print(", ");
        pw.print(this.mLastDsdy);
        pw.print(", ");
        pw.print(this.mLastDtdy);
        pw.println(")");
    }

    public String toString() {
        return this.mSurfaceControl.toString();
    }
}
