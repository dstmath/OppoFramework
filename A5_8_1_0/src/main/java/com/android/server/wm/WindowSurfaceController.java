package com.android.server.wm;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Debug;
import android.os.IBinder;
import android.os.Trace;
import android.util.Log;
import android.util.Slog;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowContentFrameStats;
import com.android.server.OppoBPMHelper;
import com.android.server.display.OppoBrightUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

class WindowSurfaceController {
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
    private SurfaceControlWithBackground mSurfaceControl;
    private float mSurfaceH = OppoBrightUtils.MIN_LUX_LIMITI;
    private int mSurfaceLayer = 0;
    private boolean mSurfaceShown = false;
    private float mSurfaceW = OppoBrightUtils.MIN_LUX_LIMITI;
    private float mSurfaceX = OppoBrightUtils.MIN_LUX_LIMITI;
    private float mSurfaceY = OppoBrightUtils.MIN_LUX_LIMITI;
    private final Session mWindowSession;
    private final int mWindowType;
    private final String title;

    static class SurfaceTrace extends SurfaceControl {
        private static final boolean LOG_SURFACE_TRACE = WindowManagerDebugConfig.DEBUG_SURFACE_TRACE;
        private static final String SURFACE_TAG = "WindowManager";
        static final ArrayList<SurfaceTrace> sSurfaces = new ArrayList();
        private float mDsdx;
        private float mDsdy;
        private float mDtdx;
        private float mDtdy;
        private final Rect mFinalCrop = new Rect();
        private boolean mIsOpaque;
        private int mLayer;
        private int mLayerStack;
        private final String mName;
        private final PointF mPosition = new PointF();
        private boolean mShown = false;
        private final Point mSize = new Point();
        private float mSurfaceTraceAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
        private final Rect mWindowCrop = new Rect();

        public SurfaceTrace(SurfaceSession s, String name, int w, int h, int format, int flags, int windowType, int ownerUid) throws OutOfResourcesException {
            super(s, name, w, h, format, flags, windowType, ownerUid);
            if (name == null) {
                name = "Not named";
            }
            this.mName = name;
            this.mSize.set(w, h);
            if (LOG_SURFACE_TRACE) {
                Slog.v(SURFACE_TAG, "ctor: " + this + ". Called by " + Debug.getCallers(3));
            }
            synchronized (sSurfaces) {
                sSurfaces.add(0, this);
            }
        }

        public SurfaceTrace(SurfaceSession s, String name, int w, int h, int format, int flags) {
            super(s, name, w, h, format, flags);
            if (name == null) {
                name = "Not named";
            }
            this.mName = name;
            this.mSize.set(w, h);
            if (LOG_SURFACE_TRACE) {
                Slog.v(SURFACE_TAG, "ctor: " + this + ". Called by " + Debug.getCallers(3));
            }
            synchronized (sSurfaces) {
                sSurfaces.add(0, this);
            }
        }

        public void setAlpha(float alpha) {
            if (this.mSurfaceTraceAlpha != alpha) {
                if (LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setAlpha(" + alpha + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mSurfaceTraceAlpha = alpha;
            }
            super.setAlpha(alpha);
        }

        public void setLayer(int zorder) {
            if (zorder != this.mLayer) {
                if (LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setLayer(" + zorder + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mLayer = zorder;
            }
            super.setLayer(zorder);
            synchronized (sSurfaces) {
                sSurfaces.remove(this);
                int i = sSurfaces.size() - 1;
                while (i >= 0 && ((SurfaceTrace) sSurfaces.get(i)).mLayer >= zorder) {
                    i--;
                }
                sSurfaces.add(i + 1, this);
            }
        }

        public void setPosition(float x, float y) {
            if (!(x == this.mPosition.x && y == this.mPosition.y)) {
                if (LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setPosition(" + x + "," + y + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mPosition.set(x, y);
            }
            super.setPosition(x, y);
        }

        public void setGeometryAppliesWithResize() {
            if (LOG_SURFACE_TRACE) {
                Slog.v(SURFACE_TAG, "setGeometryAppliesWithResize(): OLD: " + this + ". Called by" + Debug.getCallers(3));
            }
            super.setGeometryAppliesWithResize();
        }

        public void setSize(int w, int h) {
            if (!(w == this.mSize.x && h == this.mSize.y)) {
                if (LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setSize(" + w + "," + h + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mSize.set(w, h);
            }
            super.setSize(w, h);
        }

        public void setWindowCrop(Rect crop) {
            if (!(crop == null || crop.equals(this.mWindowCrop))) {
                if (LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setWindowCrop(" + crop.toShortString() + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mWindowCrop.set(crop);
            }
            super.setWindowCrop(crop);
        }

        public void setFinalCrop(Rect crop) {
            if (!(crop == null || crop.equals(this.mFinalCrop))) {
                if (LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setFinalCrop(" + crop.toShortString() + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mFinalCrop.set(crop);
            }
            super.setFinalCrop(crop);
        }

        public void setLayerStack(int layerStack) {
            if (layerStack != this.mLayerStack) {
                if (LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setLayerStack(" + layerStack + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mLayerStack = layerStack;
            }
            super.setLayerStack(layerStack);
        }

        public void setOpaque(boolean isOpaque) {
            if (isOpaque != this.mIsOpaque) {
                if (LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setOpaque(" + isOpaque + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mIsOpaque = isOpaque;
            }
            super.setOpaque(isOpaque);
        }

        public void setSecure(boolean isSecure) {
            super.setSecure(isSecure);
        }

        public void setMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
            if (!(dsdx == this.mDsdx && dtdx == this.mDtdx && dsdy == this.mDsdy && dtdy == this.mDtdy)) {
                if (LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setMatrix(" + dsdx + "," + dtdx + "," + dsdy + "," + dtdy + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mDsdx = dsdx;
                this.mDtdx = dtdx;
                this.mDsdy = dsdy;
                this.mDtdy = dtdy;
            }
            super.setMatrix(dsdx, dtdx, dsdy, dtdy);
        }

        public void hide() {
            if (this.mShown) {
                if (LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "hide: OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mShown = false;
            }
            super.hide();
        }

        public void show() {
            if (!this.mShown) {
                if (LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "show: OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mShown = true;
            }
            super.show();
        }

        public void destroy() {
            super.destroy();
            if (LOG_SURFACE_TRACE) {
                Slog.v(SURFACE_TAG, "destroy: " + this + ". Called by " + Debug.getCallers(3));
            }
            synchronized (sSurfaces) {
                sSurfaces.remove(this);
            }
        }

        public void release() {
            super.release();
            if (LOG_SURFACE_TRACE) {
                Slog.v(SURFACE_TAG, "release: " + this + ". Called by " + Debug.getCallers(3));
            }
            synchronized (sSurfaces) {
                sSurfaces.remove(this);
            }
        }

        public void setTransparentRegionHint(Region region) {
            if (LOG_SURFACE_TRACE) {
                Slog.v(SURFACE_TAG, "setTransparentRegionHint(" + region + "): OLD: " + this + " . Called by " + Debug.getCallers(3));
            }
            super.setTransparentRegionHint(region);
        }

        static void dumpAllSurfaces(PrintWriter pw, String header) {
            synchronized (sSurfaces) {
                int N = sSurfaces.size();
                if (N <= 0) {
                    return;
                }
                if (header != null) {
                    pw.println(header);
                }
                pw.println("WINDOW MANAGER SURFACES (dumpsys window surfaces)");
                for (int i = 0; i < N; i++) {
                    SurfaceTrace s = (SurfaceTrace) sSurfaces.get(i);
                    pw.print("  Surface #");
                    pw.print(i);
                    pw.print(": #");
                    pw.print(Integer.toHexString(System.identityHashCode(s)));
                    pw.print(" ");
                    pw.println(s.mName);
                    pw.print("    mLayerStack=");
                    pw.print(s.mLayerStack);
                    pw.print(" mLayer=");
                    pw.println(s.mLayer);
                    pw.print("    mShown=");
                    pw.print(s.mShown);
                    pw.print(" mAlpha=");
                    pw.print(s.mSurfaceTraceAlpha);
                    pw.print(" mIsOpaque=");
                    pw.println(s.mIsOpaque);
                    pw.print("    mPosition=");
                    pw.print(s.mPosition.x);
                    pw.print(",");
                    pw.print(s.mPosition.y);
                    pw.print(" mSize=");
                    pw.print(s.mSize.x);
                    pw.print("x");
                    pw.println(s.mSize.y);
                    pw.print("    mCrop=");
                    s.mWindowCrop.printShortString(pw);
                    pw.println();
                    pw.print("    mFinalCrop=");
                    s.mFinalCrop.printShortString(pw);
                    pw.println();
                    pw.print("    Transform: (");
                    pw.print(s.mDsdx);
                    pw.print(", ");
                    pw.print(s.mDtdx);
                    pw.print(", ");
                    pw.print(s.mDsdy);
                    pw.print(", ");
                    pw.print(s.mDtdy);
                    pw.println(")");
                }
            }
        }

        public String toString() {
            return "Surface " + Integer.toHexString(System.identityHashCode(this)) + " " + this.mName + " (" + this.mLayerStack + "): shown=" + this.mShown + " layer=" + this.mLayer + " alpha=" + this.mSurfaceTraceAlpha + " " + this.mPosition.x + "," + this.mPosition.y + " " + this.mSize.x + "x" + this.mSize.y + " crop=" + this.mWindowCrop.toShortString() + " opaque=" + this.mIsOpaque + " (" + this.mDsdx + "," + this.mDtdx + "," + this.mDsdy + "," + this.mDtdy + ")";
        }
    }

    public WindowSurfaceController(SurfaceSession s, String name, int w, int h, int format, int flags, WindowStateAnimator animator, int windowType, int ownerUid) {
        this.mAnimator = animator;
        this.mSurfaceW = (float) w;
        this.mSurfaceH = (float) h;
        this.title = name;
        this.mService = animator.mService;
        WindowState win = animator.mWin;
        this.mWindowType = windowType;
        this.mWindowSession = win.mSession;
        Trace.traceBegin(32, "new SurfaceControl");
        this.mSurfaceControl = new SurfaceControlWithBackground(s, name, w, h, format, flags, windowType, ownerUid, this);
        Trace.traceEnd(32);
        if (this.mService.mRoot.mSurfaceTraceEnabled) {
            this.mSurfaceControl = new RemoteSurfaceTrace(this.mService.mRoot.mSurfaceTraceFd.getFileDescriptor(), this.mSurfaceControl, win);
        }
    }

    void installRemoteTrace(FileDescriptor fd) {
        this.mSurfaceControl = new RemoteSurfaceTrace(fd, this.mSurfaceControl, this.mAnimator.mWin);
    }

    void removeRemoteTrace() {
        this.mSurfaceControl = new SurfaceControlWithBackground(this.mSurfaceControl);
    }

    private void logSurface(String msg, RuntimeException where) {
        String str = "  SURFACE " + msg + ": " + this.title;
        if (where != null) {
            Slog.i(TAG, str, where);
        } else {
            Slog.i(TAG, str);
        }
    }

    void reparentChildrenInTransaction(WindowSurfaceController other) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            Slog.i(TAG, "REPARENT from: " + this + " to: " + other);
        }
        if (this.mSurfaceControl != null && other.mSurfaceControl != null) {
            this.mSurfaceControl.reparentChildren(other.getHandle());
        }
    }

    void detachChildren() {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            Slog.i(TAG, "SEVER CHILDREN");
        }
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.detachChildren();
        }
    }

    void hideInTransaction(String reason) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.DEBUG_WMS) {
            logSurface("HIDE ( " + reason + " )", null);
        }
        this.mHiddenForOtherReasons = true;
        this.mAnimator.destroyPreservedSurfaceLocked();
        updateVisibility();
    }

    private void hideSurface() {
        if (this.mSurfaceControl != null) {
            setShown(false);
            try {
                if (!((!WindowManagerDebugConfig.DEBUG_WMS && !WindowManagerDebugConfig.SHOW_TRANSACTIONS) || DetectBlack.getInstance().isLastCheckBlack() || (DetectBlack.getInstance().checkHasSurface(this) ^ 1) == 0)) {
                    Slog.i("DetectBlack", "No surface, maybe black! with hideSurface() surface:" + this);
                    if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                        Slog.i("DetectBlack", Log.getStackTraceString(new Throwable()));
                    }
                }
                this.mSurfaceControl.hide();
            } catch (RuntimeException e) {
                Slog.w(TAG, "Exception hiding surface in " + this);
            }
        }
    }

    void destroyInTransaction() {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.SHOW_SURFACE_ALLOC || WindowManagerDebugConfig.DEBUG_WMS) {
            Slog.i(TAG, "Destroying surface " + this + " called by " + Debug.getCallers(8));
        }
        try {
            if (this.mSurfaceControl != null) {
                if (!((!WindowManagerDebugConfig.DEBUG_WMS && !WindowManagerDebugConfig.SHOW_TRANSACTIONS) || DetectBlack.getInstance().isLastCheckBlack() || (DetectBlack.getInstance().checkHasSurface(this) ^ 1) == 0)) {
                    Slog.i("DetectBlack", "No surface, maybe black! with destroyInTransaction() surface:" + this);
                    if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                        Slog.i("DetectBlack", Log.getStackTraceString(new Throwable()));
                    }
                }
                this.mSurfaceControl.destroy();
            }
            setShown(false);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error destroying surface in: " + this, e);
            setShown(false);
        } catch (Throwable th) {
            setShown(false);
            this.mSurfaceControl = null;
        }
        this.mSurfaceControl = null;
    }

    void disconnectInTransaction() {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.SHOW_SURFACE_ALLOC) {
            Slog.i(TAG, "Disconnecting client: " + this);
        }
        try {
            if (this.mSurfaceControl != null) {
                this.mSurfaceControl.disconnect();
            }
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error disconnecting surface in: " + this, e);
        }
    }

    void setCropInTransaction(Rect clipRect, boolean recoveringMemory) {
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
            this.mSurfaceControl.setWindowCrop(clipRect);
            this.mHiddenForCrop = false;
            updateVisibility();
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error setting crop surface of " + this + " crop=" + clipRect.toShortString(), e);
            if (!recoveringMemory) {
                this.mAnimator.reclaimSomeSurfaceMemory("crop", true);
            }
        }
    }

    void clearCropInTransaction(boolean recoveringMemory) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            logSurface("CLEAR CROP", null);
        }
        try {
            this.mSurfaceControl.setWindowCrop(new Rect(0, 0, -1, -1));
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error setting clearing crop of " + this, e);
            if (!recoveringMemory) {
                this.mAnimator.reclaimSomeSurfaceMemory("crop", true);
            }
        }
    }

    void setFinalCropInTransaction(Rect clipRect) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            logSurface("FINAL CROP " + clipRect.toShortString(), null);
        }
        try {
            this.mSurfaceControl.setFinalCrop(clipRect);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error disconnecting surface in: " + this, e);
        }
    }

    void setLayer(int layer) {
        if (this.mSurfaceControl != null) {
            this.mService.openSurfaceTransaction();
            try {
                if (this.mAnimator.mWin.usesRelativeZOrdering()) {
                    this.mSurfaceControl.setRelativeLayer(this.mAnimator.mWin.getParentWindow().mWinAnimator.mSurfaceController.mSurfaceControl.getHandle(), -1);
                } else {
                    this.mSurfaceLayer = layer;
                    this.mSurfaceControl.setLayer(layer);
                }
                this.mService.closeSurfaceTransaction();
            } catch (Throwable th) {
                this.mService.closeSurfaceTransaction();
            }
        }
    }

    void setLayerStackInTransaction(int layerStack) {
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.setLayerStack(layerStack);
        }
    }

    void setPositionInTransaction(float left, float top, boolean recoveringMemory) {
        boolean surfaceMoved = (this.mSurfaceX == left && this.mSurfaceY == top) ? false : true;
        if (surfaceMoved) {
            this.mSurfaceX = left;
            this.mSurfaceY = top;
            try {
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    logSurface("POS (setPositionInTransaction) @ (" + left + "," + top + ")", null);
                }
                this.mSurfaceControl.setPosition(left, top);
            } catch (RuntimeException e) {
                Slog.w(TAG, "Error positioning surface of " + this + " pos=(" + left + "," + top + ")", e);
                if (!recoveringMemory) {
                    this.mAnimator.reclaimSomeSurfaceMemory("position", true);
                }
            }
        }
    }

    void setGeometryAppliesWithResizeInTransaction(boolean recoveringMemory) {
        this.mSurfaceControl.setGeometryAppliesWithResize();
    }

    void setMatrixInTransaction(float dsdx, float dtdx, float dtdy, float dsdy, boolean recoveringMemory) {
        boolean matrixChanged = (this.mLastDsdx == dsdx && this.mLastDtdx == dtdx && this.mLastDtdy == dtdy) ? this.mLastDsdy != dsdy : true;
        if (matrixChanged) {
            this.mLastDsdx = dsdx;
            this.mLastDtdx = dtdx;
            this.mLastDtdy = dtdy;
            this.mLastDsdy = dsdy;
            try {
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    logSurface("MATRIX [" + dsdx + "," + dtdx + "," + dtdy + "," + dsdy + "]", null);
                }
                this.mSurfaceControl.setMatrix(dsdx, dtdx, dtdy, dsdy);
            } catch (RuntimeException e) {
                Slog.e(TAG, "Error setting matrix on surface surface" + this.title + " MATRIX [" + dsdx + "," + dtdx + "," + dtdy + "," + dsdy + "]", null);
                if (!recoveringMemory) {
                    this.mAnimator.reclaimSomeSurfaceMemory("matrix", true);
                }
            }
        }
    }

    boolean setSizeInTransaction(int width, int height, boolean recoveringMemory) {
        boolean surfaceResized = (this.mSurfaceW == ((float) width) && this.mSurfaceH == ((float) height)) ? false : true;
        if (!surfaceResized) {
            return false;
        }
        this.mSurfaceW = (float) width;
        this.mSurfaceH = (float) height;
        try {
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                logSurface("SIZE " + width + "x" + height, null);
            }
            this.mSurfaceControl.setSize(width, height);
            return true;
        } catch (RuntimeException e) {
            Slog.e(TAG, "Error resizing surface of " + this.title + " size=(" + width + "x" + height + ")", e);
            if (!recoveringMemory) {
                this.mAnimator.reclaimSomeSurfaceMemory("size", true);
            }
            return false;
        }
    }

    boolean prepareToShowInTransaction(float alpha, float dsdx, float dtdx, float dsdy, float dtdy, boolean recoveringMemory) {
        if (this.mSurfaceControl != null) {
            try {
                this.mSurfaceAlpha = alpha;
                this.mSurfaceControl.setAlpha(alpha);
                this.mLastDsdx = dsdx;
                this.mLastDtdx = dtdx;
                this.mLastDsdy = dsdy;
                this.mLastDtdy = dtdy;
                this.mSurfaceControl.setMatrix(dsdx, dtdx, dsdy, dtdy);
            } catch (RuntimeException e) {
                Slog.w(TAG, "Error updating surface in " + this.title, e);
                if (!recoveringMemory) {
                    this.mAnimator.reclaimSomeSurfaceMemory("update", true);
                }
                return false;
            }
        }
        return true;
    }

    void setTransparentRegionHint(Region region) {
        if (this.mSurfaceControl == null) {
            Slog.w(TAG, "setTransparentRegionHint: null mSurface after mHasSurface true");
            return;
        }
        if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
            Slog.i(TAG, ">>> OPEN TRANSACTION setTransparentRegion");
        }
        this.mService.openSurfaceTransaction();
        try {
            this.mSurfaceControl.setTransparentRegionHint(region);
        } finally {
            this.mService.closeSurfaceTransaction();
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, "<<< CLOSE TRANSACTION setTransparentRegion");
            }
        }
    }

    void setOpaque(boolean isOpaque) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            logSurface("isOpaque=" + isOpaque, null);
        }
        if (this.mSurfaceControl != null) {
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, ">>> OPEN TRANSACTION setOpaqueLocked");
            }
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setOpaque(isOpaque);
            } finally {
                this.mService.closeSurfaceTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i(TAG, "<<< CLOSE TRANSACTION setOpaqueLocked");
                }
            }
        }
    }

    void setSecure(boolean isSecure) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            logSurface("isSecure=" + isSecure, null);
        }
        if (this.mSurfaceControl != null) {
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, ">>> OPEN TRANSACTION setSecureLocked");
            }
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setSecure(isSecure);
            } finally {
                this.mService.closeSurfaceTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i(TAG, "<<< CLOSE TRANSACTION setSecureLocked");
                }
            }
        }
    }

    void getContainerRect(Rect rect) {
        this.mAnimator.getContainerRect(rect);
    }

    boolean showRobustlyInTransaction() {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.DEBUG_WMS) {
            logSurface("SHOW (performLayout)", null);
        }
        this.mHiddenForOtherReasons = false;
        return updateVisibility();
    }

    private boolean updateVisibility() {
        if (this.mHiddenForCrop || this.mHiddenForOtherReasons) {
            if (this.mSurfaceShown) {
                hideSurface();
            }
            return false;
        } else if (this.mSurfaceShown) {
            return true;
        } else {
            return showSurface();
        }
    }

    private boolean showSurface() {
        try {
            setShown(true);
            this.mSurfaceControl.show();
            if ((WindowManagerDebugConfig.DEBUG_WMS || WindowManagerDebugConfig.SHOW_TRANSACTIONS) && DetectBlack.getInstance().isLastCheckBlack() && DetectBlack.getInstance().isWindowSurfaceContrlInScreenCenter(this) && DetectBlack.getInstance().checkHasSurface()) {
                Slog.i("DetectBlack", "Resume from black! showSurface surface:" + this);
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i("DetectBlack", Log.getStackTraceString(new Throwable()));
                }
            }
            return true;
        } catch (RuntimeException e) {
            Slog.w(TAG, "Failure showing surface " + this.mSurfaceControl + " in " + this, e);
            this.mAnimator.reclaimSomeSurfaceMemory("show", true);
            return false;
        }
    }

    void deferTransactionUntil(IBinder handle, long frame) {
        this.mSurfaceControl.deferTransactionUntil(handle, frame);
    }

    void forceScaleableInTransaction(boolean force) {
        this.mSurfaceControl.setOverrideScalingMode(force ? 1 : -1);
    }

    boolean clearWindowContentFrameStats() {
        if (this.mSurfaceControl == null) {
            return false;
        }
        return this.mSurfaceControl.clearContentFrameStats();
    }

    boolean getWindowContentFrameStats(WindowContentFrameStats outStats) {
        if (this.mSurfaceControl == null) {
            return false;
        }
        return this.mSurfaceControl.getContentFrameStats(outStats);
    }

    boolean hasSurface() {
        return this.mSurfaceControl != null;
    }

    IBinder getHandle() {
        if (this.mSurfaceControl == null) {
            return null;
        }
        return this.mSurfaceControl.getHandle();
    }

    void getSurface(Surface outSurface) {
        outSurface.copyFrom(this.mSurfaceControl);
    }

    int getLayer() {
        return this.mSurfaceLayer;
    }

    boolean getShown() {
        return this.mSurfaceShown;
    }

    void setShown(boolean surfaceShown) {
        this.mSurfaceShown = surfaceShown;
        this.mService.updateNonSystemOverlayWindowsVisibilityIfNeeded(this.mAnimator.mWin, surfaceShown);
        if (this.mWindowSession != null) {
            this.mWindowSession.onWindowSurfaceVisibilityChanged(this, this.mSurfaceShown, this.mWindowType);
        }
        if (this.mAnimator.mWin != null && this.mWindowSession != null && this.mWindowSession.mUid > 10000) {
            try {
                OppoBPMHelper.noteWindowStateChange(this.mWindowSession.mUid, this.mWindowSession.mPid, this.mAnimator.mWin.hashCode(), this.mAnimator.mWin.mAttrs.type, this.mAnimator.mWin.getHasSurface(), surfaceShown);
            } catch (Exception e) {
            }
        }
    }

    float getX() {
        return this.mSurfaceX;
    }

    float getY() {
        return this.mSurfaceY;
    }

    float getWidth() {
        return this.mSurfaceW;
    }

    float getHeight() {
        return this.mSurfaceH;
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
