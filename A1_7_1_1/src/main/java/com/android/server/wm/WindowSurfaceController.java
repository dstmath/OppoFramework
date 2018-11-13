package com.android.server.wm;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Debug;
import android.os.IBinder;
import android.util.Slog;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowContentFrameStats;
import com.android.server.display.OppoBrightUtils;
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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
class WindowSurfaceController {
    static final String TAG = null;
    final WindowStateAnimator mAnimator;
    private boolean mHiddenForCrop;
    private boolean mHiddenForOtherReasons;
    private float mSurfaceAlpha;
    SurfaceControl mSurfaceControl;
    float mSurfaceH;
    int mSurfaceLayer;
    private boolean mSurfaceShown;
    float mSurfaceW;
    private float mSurfaceX;
    private float mSurfaceY;
    private final String title;

    static class SurfaceTrace extends SurfaceControl {
        private static final String SURFACE_TAG = null;
        static final ArrayList<SurfaceTrace> sSurfaces = null;
        private final boolean LOG_SURFACE_TRACE;
        private float mDsdx;
        private float mDsdy;
        private float mDtdx;
        private float mDtdy;
        private final Rect mFinalCrop;
        private boolean mIsOpaque;
        private int mLayer;
        private int mLayerStack;
        private final String mName;
        private final PointF mPosition;
        private boolean mShown;
        private final Point mSize;
        private float mSurfaceTraceAlpha;
        private final Rect mWindowCrop;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WindowSurfaceController.SurfaceTrace.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WindowSurfaceController.SurfaceTrace.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowSurfaceController.SurfaceTrace.<clinit>():void");
        }

        public SurfaceTrace(SurfaceSession s, String name, int w, int h, int format, int flags) throws OutOfResourcesException {
            super(s, name, w, h, format, flags);
            this.LOG_SURFACE_TRACE = WindowManagerDebugConfig.DEBUG_SURFACE_TRACE;
            this.mSurfaceTraceAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
            this.mPosition = new PointF();
            this.mSize = new Point();
            this.mWindowCrop = new Rect();
            this.mFinalCrop = new Rect();
            this.mShown = false;
            if (name == null) {
                name = "Not named";
            }
            this.mName = name;
            this.mSize.set(w, h);
            if (this.LOG_SURFACE_TRACE) {
                Slog.v(SURFACE_TAG, "ctor: " + this + ". Called by " + Debug.getCallers(3));
            }
            synchronized (sSurfaces) {
                sSurfaces.add(0, this);
            }
        }

        public void setAlpha(float alpha) {
            if (this.mSurfaceTraceAlpha != alpha) {
                if (this.LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setAlpha(" + alpha + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mSurfaceTraceAlpha = alpha;
            }
            super.setAlpha(alpha);
        }

        public void setLayer(int zorder) {
            if (zorder != this.mLayer) {
                if (this.LOG_SURFACE_TRACE) {
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
                if (this.LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setPosition(" + x + "," + y + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mPosition.set(x, y);
            }
            super.setPosition(x, y);
        }

        public void setGeometryAppliesWithResize() {
            if (this.LOG_SURFACE_TRACE) {
                Slog.v(SURFACE_TAG, "setGeometryAppliesWithResize(): OLD: " + this + ". Called by" + Debug.getCallers(3));
            }
            super.setGeometryAppliesWithResize();
        }

        public void setSize(int w, int h) {
            if (!(w == this.mSize.x && h == this.mSize.y)) {
                if (this.LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setSize(" + w + "," + h + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mSize.set(w, h);
            }
            super.setSize(w, h);
        }

        public void setWindowCrop(Rect crop) {
            if (!(crop == null || crop.equals(this.mWindowCrop))) {
                if (this.LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setWindowCrop(" + crop.toShortString() + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mWindowCrop.set(crop);
            }
            super.setWindowCrop(crop);
        }

        public void setFinalCrop(Rect crop) {
            if (!(crop == null || crop.equals(this.mFinalCrop))) {
                if (this.LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setFinalCrop(" + crop.toShortString() + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mFinalCrop.set(crop);
            }
            super.setFinalCrop(crop);
        }

        public void setLayerStack(int layerStack) {
            if (layerStack != this.mLayerStack) {
                if (this.LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "setLayerStack(" + layerStack + "): OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mLayerStack = layerStack;
            }
            super.setLayerStack(layerStack);
        }

        public void setOpaque(boolean isOpaque) {
            if (isOpaque != this.mIsOpaque) {
                if (this.LOG_SURFACE_TRACE) {
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
                if (this.LOG_SURFACE_TRACE) {
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
                if (this.LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "hide: OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mShown = false;
            }
            super.hide();
        }

        public void show() {
            if (!this.mShown) {
                if (this.LOG_SURFACE_TRACE) {
                    Slog.v(SURFACE_TAG, "show: OLD:" + this + ". Called by " + Debug.getCallers(3));
                }
                this.mShown = true;
            }
            super.show();
        }

        public void destroy() {
            super.destroy();
            if (this.LOG_SURFACE_TRACE) {
                Slog.v(SURFACE_TAG, "destroy: " + this + ". Called by " + Debug.getCallers(3));
            }
            synchronized (sSurfaces) {
                sSurfaces.remove(this);
            }
        }

        public void release() {
            super.release();
            if (this.LOG_SURFACE_TRACE) {
                Slog.v(SURFACE_TAG, "release: " + this + ". Called by " + Debug.getCallers(3));
            }
            synchronized (sSurfaces) {
                sSurfaces.remove(this);
            }
        }

        public void setTransparentRegionHint(Region region) {
            if (this.LOG_SURFACE_TRACE) {
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

    class SurfaceControlWithBackground extends SurfaceTrace {
        private boolean mAppForcedInvisible;
        private AppWindowToken mAppToken;
        private SurfaceControl mBackgroundControl;
        public int mLayer;
        private boolean mOpaque;
        public boolean mVisible;

        public SurfaceControlWithBackground(SurfaceSession s, String name, int w, int h, int format, int flags, AppWindowToken token) throws OutOfResourcesException {
            super(s, name, w, h, format, flags);
            this.mOpaque = true;
            this.mAppForcedInvisible = false;
            this.mVisible = false;
            this.mLayer = -1;
            this.mBackgroundControl = new SurfaceControl(s, name, w, h, -1, flags | DumpState.DUMP_INTENT_FILTER_VERIFIERS);
            this.mOpaque = (flags & 1024) != 0;
            this.mAppToken = token;
            this.mAppToken.addSurfaceViewBackground(this);
        }

        public void setAlpha(float alpha) {
            super.setAlpha(alpha);
            this.mBackgroundControl.setAlpha(alpha);
        }

        public void setLayer(int zorder) {
            super.setLayer(zorder);
            this.mBackgroundControl.setLayer(zorder - 1);
            if (this.mLayer != zorder) {
                this.mLayer = zorder;
                this.mAppToken.updateSurfaceViewBackgroundVisibilities();
            }
        }

        public void setPosition(float x, float y) {
            super.setPosition(x, y);
            this.mBackgroundControl.setPosition(x, y);
        }

        public void setSize(int w, int h) {
            super.setSize(w, h);
            this.mBackgroundControl.setSize(w, h);
        }

        public void setWindowCrop(Rect crop) {
            super.setWindowCrop(crop);
            this.mBackgroundControl.setWindowCrop(crop);
        }

        public void setFinalCrop(Rect crop) {
            super.setFinalCrop(crop);
            this.mBackgroundControl.setFinalCrop(crop);
        }

        public void setLayerStack(int layerStack) {
            super.setLayerStack(layerStack);
            this.mBackgroundControl.setLayerStack(layerStack);
        }

        public void setOpaque(boolean isOpaque) {
            super.setOpaque(isOpaque);
            this.mOpaque = isOpaque;
            updateBackgroundVisibility(this.mAppForcedInvisible);
        }

        public void setSecure(boolean isSecure) {
            super.setSecure(isSecure);
        }

        public void setMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
            super.setMatrix(dsdx, dtdx, dsdy, dtdy);
            this.mBackgroundControl.setMatrix(dsdx, dtdx, dsdy, dtdy);
        }

        public void hide() {
            super.hide();
            if (this.mVisible) {
                this.mVisible = false;
                this.mAppToken.updateSurfaceViewBackgroundVisibilities();
            }
        }

        public void show() {
            super.show();
            if (!this.mVisible) {
                this.mVisible = true;
                this.mAppToken.updateSurfaceViewBackgroundVisibilities();
            }
        }

        public void destroy() {
            super.destroy();
            this.mBackgroundControl.destroy();
            this.mAppToken.removeSurfaceViewBackground(this);
        }

        public void release() {
            super.release();
            this.mBackgroundControl.release();
        }

        public void setTransparentRegionHint(Region region) {
            super.setTransparentRegionHint(region);
            this.mBackgroundControl.setTransparentRegionHint(region);
        }

        public void deferTransactionUntil(IBinder handle, long frame) {
            super.deferTransactionUntil(handle, frame);
            this.mBackgroundControl.deferTransactionUntil(handle, frame);
        }

        void updateBackgroundVisibility(boolean forcedInvisible) {
            this.mAppForcedInvisible = forcedInvisible;
            if (this.mOpaque && this.mVisible && !this.mAppForcedInvisible) {
                this.mBackgroundControl.show();
            } else {
                this.mBackgroundControl.hide();
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WindowSurfaceController.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wm.WindowSurfaceController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WindowSurfaceController.<clinit>():void");
    }

    public WindowSurfaceController(SurfaceSession s, String name, int w, int h, int format, int flags, WindowStateAnimator animator) {
        this.mSurfaceShown = false;
        this.mSurfaceX = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mSurfaceY = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mSurfaceW = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mSurfaceH = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mSurfaceAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mSurfaceLayer = 0;
        this.mHiddenForCrop = false;
        this.mHiddenForOtherReasons = true;
        this.mAnimator = animator;
        this.mSurfaceW = (float) w;
        this.mSurfaceH = (float) h;
        this.title = name;
        if (animator.mWin.isChildWindow() && animator.mWin.mSubLayer < 0 && animator.mWin.mAppToken != null) {
            this.mSurfaceControl = new SurfaceControlWithBackground(s, name, w, h, format, flags, animator.mWin.mAppToken);
        } else if (WindowManagerDebugConfig.DEBUG_SURFACE_TRACE) {
            this.mSurfaceControl = new SurfaceTrace(s, name, w, h, format, flags);
        } else {
            this.mSurfaceControl = new SurfaceControl(s, name, w, h, format, flags);
        }
    }

    void logSurface(String msg, RuntimeException where) {
        String str = "  SURFACE " + msg + ": " + this.title;
        if (where != null) {
            Slog.i(TAG, str, where);
        } else {
            Slog.i(TAG, str);
        }
    }

    void hideInTransaction(String reason) {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerService.DEBUG_WMS) {
            logSurface("HIDE ( " + reason + " )", null);
        }
        this.mHiddenForOtherReasons = true;
        this.mAnimator.destroyPreservedSurfaceLocked();
        updateVisibility();
    }

    private void hideSurface() {
        if (this.mSurfaceControl != null) {
            this.mSurfaceShown = false;
            try {
                this.mSurfaceControl.hide();
            } catch (RuntimeException e) {
                Slog.w(TAG, "Exception hiding surface in " + this);
            }
        }
    }

    void setPositionAndLayer(float left, float top, int layerStack, int layer) {
        SurfaceControl.openTransaction();
        try {
            this.mSurfaceX = left;
            this.mSurfaceY = top;
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                logSurface("POS (setPositionAndLayer) @ (" + left + "," + top + ")", null);
            }
            this.mSurfaceControl.setPosition(left, top);
            this.mSurfaceControl.setLayerStack(layerStack);
            this.mSurfaceControl.setLayer(layer);
            this.mSurfaceControl.setAlpha(OppoBrightUtils.MIN_LUX_LIMITI);
            this.mSurfaceShown = false;
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error creating surface in " + this, e);
            this.mAnimator.reclaimSomeSurfaceMemory("create-init", true);
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i(TAG, "<<< CLOSE TRANSACTION setPositionAndLayer");
            }
        }
        SurfaceControl.closeTransaction();
        if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
            Slog.i(TAG, "<<< CLOSE TRANSACTION setPositionAndLayer");
        }
    }

    void destroyInTransaction() {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.SHOW_SURFACE_ALLOC || WindowManagerService.DEBUG_WMS) {
            Slog.i(TAG, "Destroying surface " + this + " called by " + Debug.getCallers(8));
        }
        try {
            if (this.mSurfaceControl != null) {
                this.mSurfaceControl.destroy();
            }
            this.mSurfaceShown = false;
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error destroying surface in: " + this, e);
            this.mSurfaceShown = false;
        } catch (Throwable th) {
            this.mSurfaceShown = false;
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
            SurfaceControl.openTransaction();
            try {
                this.mSurfaceControl.setLayer(layer);
            } finally {
                SurfaceControl.closeTransaction();
            }
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

    void setMatrixInTransaction(float dsdx, float dtdx, float dsdy, float dtdy, boolean recoveringMemory) {
        try {
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                logSurface("MATRIX [" + dsdx + "," + dtdx + "," + dsdy + "," + dtdy + "]", null);
            }
            this.mSurfaceControl.setMatrix(dsdx, dtdx, dsdy, dtdy);
        } catch (RuntimeException e) {
            Slog.e(TAG, "Error setting matrix on surface surface" + this.title + " MATRIX [" + dsdx + "," + dtdx + "," + dsdy + "," + dtdy + "]", null);
            if (!recoveringMemory) {
                this.mAnimator.reclaimSomeSurfaceMemory("matrix", true);
            }
        }
    }

    boolean setSizeInTransaction(int width, int height, boolean recoveringMemory) {
        boolean surfaceResized;
        if (this.mSurfaceW == ((float) width) && this.mSurfaceH == ((float) height)) {
            surfaceResized = false;
        } else {
            surfaceResized = true;
        }
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

    boolean prepareToShowInTransaction(float alpha, int layer, float dsdx, float dtdx, float dsdy, float dtdy, boolean recoveringMemory) {
        if (this.mSurfaceControl != null) {
            try {
                this.mSurfaceAlpha = alpha;
                this.mSurfaceControl.setAlpha(alpha);
                this.mSurfaceLayer = layer;
                this.mSurfaceControl.setLayer(layer);
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
        SurfaceControl.openTransaction();
        try {
            this.mSurfaceControl.setTransparentRegionHint(region);
        } finally {
            SurfaceControl.closeTransaction();
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
            SurfaceControl.openTransaction();
            try {
                this.mSurfaceControl.setOpaque(isOpaque);
            } finally {
                SurfaceControl.closeTransaction();
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
            SurfaceControl.openTransaction();
            try {
                this.mSurfaceControl.setSecure(isSecure);
            } finally {
                SurfaceControl.closeTransaction();
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                    Slog.i(TAG, "<<< CLOSE TRANSACTION setSecureLocked");
                }
            }
        }
    }

    boolean showRobustlyInTransaction() {
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerService.DEBUG_WMS) {
            logSurface("SHOW (performLayout)", null);
        }
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v(TAG, "Showing " + this + " during relayout");
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
            this.mSurfaceShown = true;
            this.mSurfaceControl.show();
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

    boolean getTransformToDisplayInverse() {
        return this.mSurfaceControl.getTransformToDisplayInverse();
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
        pw.println(this.mSurfaceH);
    }

    public String toString() {
        return this.mSurfaceControl.toString();
    }
}
