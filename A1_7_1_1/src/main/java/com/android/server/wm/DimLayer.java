package com.android.server.wm;

import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import com.android.server.display.OppoBrightUtils;
import java.io.PrintWriter;

public class DimLayer {
    private String TAG = "WindowManager";
    private float mAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
    private final Rect mBounds = new Rect();
    private boolean mDestroyed = false;
    private SurfaceControl mDimSurface;
    private final int mDisplayId;
    private long mDuration;
    private final Rect mLastBounds = new Rect();
    private int mLayer = -1;
    private final String mName;
    private final WindowManagerService mService;
    private boolean mShowing = false;
    private float mStartAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
    private long mStartTime;
    private float mTargetAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
    private final DimLayerUser mUser;

    interface DimLayerUser {
        boolean dimFullscreen();

        void getDimBounds(Rect rect);

        DisplayInfo getDisplayInfo();

        String toShortString();
    }

    DimLayer(WindowManagerService service, DimLayerUser user, int displayId, String name) {
        this.TAG = "WindowManager.DimLayer." + user.toShortString();
        this.mUser = user;
        this.mDisplayId = displayId;
        this.mService = service;
        this.mName = name;
        if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
            Slog.v(this.TAG, "Ctor: displayId=" + displayId);
        }
    }

    private void constructSurface(WindowManagerService service) {
        SurfaceControl.openTransaction();
        try {
            if (WindowManagerDebugConfig.DEBUG_SURFACE_TRACE) {
                this.mDimSurface = new SurfaceTrace(service.mFxSession, "DimSurface", 16, 16, -1, 131076);
            } else {
                this.mDimSurface = new SurfaceControl(service.mFxSession, this.mName, 16, 16, -1, 131076);
            }
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.SHOW_SURFACE_ALLOC) {
                Slog.i(this.TAG, "  DIM " + this.mDimSurface + ": CREATE");
            }
            this.mDimSurface.setLayerStack(this.mDisplayId);
            adjustBounds();
            adjustAlpha(this.mAlpha);
            adjustLayer(this.mLayer);
        } catch (Exception e) {
            Slog.e("WindowManager", "Exception creating Dim surface", e);
        } finally {
            SurfaceControl.closeTransaction();
        }
    }

    boolean isDimming() {
        return this.mTargetAlpha != OppoBrightUtils.MIN_LUX_LIMITI;
    }

    boolean isAnimating() {
        return this.mTargetAlpha != this.mAlpha;
    }

    float getTargetAlpha() {
        return this.mTargetAlpha;
    }

    void setLayer(int layer) {
        if (this.mLayer != layer) {
            this.mLayer = layer;
            adjustLayer(layer);
        }
    }

    private void adjustLayer(int layer) {
        if (this.mDimSurface != null) {
            this.mDimSurface.setLayer(layer);
        }
    }

    int getLayer() {
        return this.mLayer;
    }

    private void setAlpha(float alpha) {
        if (this.mAlpha != alpha) {
            this.mAlpha = alpha;
            adjustAlpha(alpha);
        }
    }

    private void adjustAlpha(float alpha) {
        if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
            Slog.v(this.TAG, "setAlpha alpha=" + alpha);
        }
        try {
            if (this.mDimSurface != null) {
                this.mDimSurface.setAlpha(alpha);
            }
            if (alpha == OppoBrightUtils.MIN_LUX_LIMITI && this.mShowing) {
                if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
                    Slog.v(this.TAG, "setAlpha hiding");
                }
                if (this.mDimSurface != null) {
                    this.mDimSurface.hide();
                    this.mShowing = false;
                }
            } else if (alpha > OppoBrightUtils.MIN_LUX_LIMITI && !this.mShowing) {
                if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
                    Slog.v(this.TAG, "setAlpha showing");
                }
                if (this.mDimSurface != null) {
                    this.mDimSurface.show();
                    this.mShowing = true;
                }
            }
        } catch (RuntimeException e) {
            Slog.w(this.TAG, "Failure setting alpha immediately", e);
        }
    }

    private void adjustBounds() {
        if (this.mUser.dimFullscreen()) {
            getBoundsForFullscreen(this.mBounds);
        }
        if (this.mDimSurface != null) {
            this.mDimSurface.setPosition((float) this.mBounds.left, (float) this.mBounds.top);
            this.mDimSurface.setSize(this.mBounds.width(), this.mBounds.height());
            if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
                Slog.v(this.TAG, "adjustBounds user=" + this.mUser.toShortString() + " mBounds=" + this.mBounds);
            }
        }
        this.mLastBounds.set(this.mBounds);
    }

    private void getBoundsForFullscreen(Rect outBounds) {
        DisplayInfo info = this.mUser.getDisplayInfo();
        int dw = (int) (((double) info.logicalWidth) * 1.5d);
        int dh = (int) (((double) info.logicalHeight) * 1.5d);
        float xPos = (float) ((dw * -1) / 6);
        float yPos = (float) ((dh * -1) / 6);
        outBounds.set((int) xPos, (int) yPos, ((int) xPos) + dw, ((int) yPos) + dh);
    }

    void setBoundsForFullscreen() {
        getBoundsForFullscreen(this.mBounds);
        setBounds(this.mBounds);
    }

    void setBounds(Rect bounds) {
        this.mBounds.set(bounds);
        if (isDimming() && !this.mLastBounds.equals(bounds)) {
            try {
                SurfaceControl.openTransaction();
                adjustBounds();
            } catch (RuntimeException e) {
                Slog.w(this.TAG, "Failure setting size", e);
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
    }

    private boolean durationEndsEarlier(long duration) {
        return SystemClock.uptimeMillis() + duration < this.mStartTime + this.mDuration;
    }

    void show() {
        if (isAnimating()) {
            if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
                Slog.v(this.TAG, "show: immediate");
            }
            show(this.mLayer, this.mTargetAlpha, 0);
        }
    }

    void show(int layer, float alpha, long duration) {
        if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
            Slog.v(this.TAG, "show: layer=" + layer + " alpha=" + alpha + " duration=" + duration + ", mDestroyed=" + this.mDestroyed);
        }
        if (this.mDestroyed) {
            Slog.e(this.TAG, "show: no Surface");
            this.mAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
            this.mTargetAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
            return;
        }
        if (this.mDimSurface == null) {
            constructSurface(this.mService);
        }
        if (!this.mLastBounds.equals(this.mBounds)) {
            adjustBounds();
        }
        setLayer(layer);
        long curTime = SystemClock.uptimeMillis();
        boolean animating = isAnimating();
        if ((animating && (this.mTargetAlpha != alpha || durationEndsEarlier(duration))) || !(animating || this.mAlpha == alpha)) {
            if (duration <= 0) {
                setAlpha(alpha);
            } else {
                this.mStartAlpha = this.mAlpha;
                this.mStartTime = curTime;
                this.mDuration = duration;
            }
        }
        this.mTargetAlpha = alpha;
        if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
            Slog.v(this.TAG, "show: mStartAlpha=" + this.mStartAlpha + " mStartTime=" + this.mStartTime + " mTargetAlpha=" + this.mTargetAlpha);
        }
    }

    void hide() {
        if (this.mShowing) {
            if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
                Slog.v(this.TAG, "hide: immediate");
            }
            hide(0);
        }
    }

    void hide(long duration) {
        if (!this.mShowing) {
            return;
        }
        if (this.mTargetAlpha != OppoBrightUtils.MIN_LUX_LIMITI || durationEndsEarlier(duration)) {
            if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
                Slog.v(this.TAG, "hide: duration=" + duration);
            }
            show(this.mLayer, OppoBrightUtils.MIN_LUX_LIMITI, duration);
        }
    }

    boolean stepAnimation() {
        if (this.mDestroyed) {
            Slog.e(this.TAG, "stepAnimation: surface destroyed");
            this.mAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
            this.mTargetAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
            return false;
        }
        if (isAnimating()) {
            long curTime = SystemClock.uptimeMillis();
            float alphaDelta = this.mTargetAlpha - this.mStartAlpha;
            float alpha = this.mStartAlpha + ((((float) (curTime - this.mStartTime)) * alphaDelta) / ((float) this.mDuration));
            if ((alphaDelta > OppoBrightUtils.MIN_LUX_LIMITI && alpha > this.mTargetAlpha) || (alphaDelta < OppoBrightUtils.MIN_LUX_LIMITI && alpha < this.mTargetAlpha)) {
                alpha = this.mTargetAlpha;
            }
            if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
                Slog.v(this.TAG, "stepAnimation: curTime=" + curTime + " alpha=" + alpha);
            }
            setAlpha(alpha);
        }
        return isAnimating();
    }

    void destroySurface() {
        if (WindowManagerDebugConfig.DEBUG_DIM_LAYER) {
            Slog.v(this.TAG, "destroySurface.");
        }
        if (this.mDimSurface != null) {
            this.mDimSurface.destroy();
            this.mDimSurface = null;
        }
        this.mDestroyed = true;
    }

    public void printTo(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("mDimSurface=");
        pw.print(this.mDimSurface);
        pw.print(" mLayer=");
        pw.print(this.mLayer);
        pw.print(" mAlpha=");
        pw.println(this.mAlpha);
        pw.print(prefix);
        pw.print("mLastBounds=");
        pw.print(this.mLastBounds.toShortString());
        pw.print(" mBounds=");
        pw.println(this.mBounds.toShortString());
        pw.print(prefix);
        pw.print("Last animation: ");
        pw.print(" mDuration=");
        pw.print(this.mDuration);
        pw.print(" mStartTime=");
        pw.print(this.mStartTime);
        pw.print(" curTime=");
        pw.println(SystemClock.uptimeMillis());
        pw.print(prefix);
        pw.print(" mStartAlpha=");
        pw.print(this.mStartAlpha);
        pw.print(" mTargetAlpha=");
        pw.println(this.mTargetAlpha);
    }
}
