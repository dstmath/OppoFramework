package com.android.server.wm;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import com.android.server.wm.LocalAnimationAdapter;
import java.io.PrintWriter;

public class ColorZoomWindowAnimationSpec implements LocalAnimationAdapter.AnimationSpec {
    private static final String TAG = "ColorZoomWindowAnimationSpec";
    private Animation mAnimation;
    private final boolean mCanSkipFirstFrame;
    private final boolean mIsAppAnimation;
    private final Point mPosition;
    private final Rect mStackBounds;
    private int mStackClipMode;
    private final Rect mTaskBounds;
    private final ThreadLocal<TmpValues> mThreadLocalTmps;
    private final Rect mTmpRect;
    private final float mWindowCornerRadius;

    static /* synthetic */ TmpValues lambda$new$0() {
        return new TmpValues();
    }

    public ColorZoomWindowAnimationSpec(Animation animation, Point position, boolean canSkipFirstFrame, float windowCornerRadius) {
        this(animation, position, null, null, canSkipFirstFrame, 2, false, windowCornerRadius);
    }

    public ColorZoomWindowAnimationSpec(Animation animation, Point position, Rect stackBounds, Rect taskBounds, boolean canSkipFirstFrame, int stackClipMode, boolean isAppAnimation, float windowCornerRadius) {
        this.mPosition = new Point();
        this.mThreadLocalTmps = ThreadLocal.withInitial($$Lambda$ColorZoomWindowAnimationSpec$2qyXGpWgtulim_6aSC3dwHVbrRY.INSTANCE);
        this.mStackBounds = new Rect();
        this.mTaskBounds = new Rect();
        this.mTmpRect = new Rect();
        this.mAnimation = animation;
        if (position != null) {
            this.mPosition.set(position.x, position.y);
        }
        this.mWindowCornerRadius = windowCornerRadius;
        this.mCanSkipFirstFrame = canSkipFirstFrame;
        this.mIsAppAnimation = isAppAnimation;
        this.mStackClipMode = stackClipMode;
        if (stackBounds != null) {
            this.mStackBounds.set(stackBounds);
        }
        if (taskBounds != null) {
            this.mTaskBounds.set(taskBounds);
        }
    }

    public boolean getShowWallpaper() {
        return this.mAnimation.getShowWallpaper();
    }

    public int getBackgroundColor() {
        return this.mAnimation.getBackgroundColor();
    }

    public long getDuration() {
        return this.mAnimation.computeDurationHint();
    }

    public void apply(SurfaceControl.Transaction t, SurfaceControl leash, long currentPlayTime) {
        TmpValues tmp = this.mThreadLocalTmps.get();
        tmp.transformation.clear();
        this.mAnimation.getTransformation(currentPlayTime, tmp.transformation);
        tmp.transformation.getMatrix().postTranslate((float) this.mPosition.x, (float) this.mPosition.y);
        t.setMatrix(leash, tmp.transformation.getMatrix(), tmp.floats);
        t.setAlpha(leash, tmp.transformation.getAlpha());
        Slog.i(TAG, "apply transformation=" + tmp.transformation + " position = (" + this.mPosition.x + "," + this.mPosition.y + " mStackClipMode = " + this.mStackClipMode + " stackBounds = " + this.mStackBounds);
        boolean cropSet = false;
        if (this.mStackClipMode != 2) {
            this.mTmpRect.set(this.mStackBounds);
            if (tmp.transformation.hasClipRect()) {
                this.mTmpRect.intersect(tmp.transformation.getClipRect());
            }
            Slog.i(TAG, "ater mTmpRect = " + this.mTmpRect + " taskBounds = " + this.mTaskBounds + " stackBounds = " + this.mStackBounds);
            t.setWindowCrop(leash, this.mTmpRect);
            cropSet = true;
        } else if (tmp.transformation.hasClipRect()) {
            t.setWindowCrop(leash, tmp.transformation.getClipRect());
            cropSet = true;
        }
        if (cropSet) {
            float f = this.mWindowCornerRadius;
            if (f > 0.0f) {
                t.setCornerRadius(leash, f);
            }
        }
    }

    public long calculateStatusBarTransitionStartTime() {
        TranslateAnimation openTranslateAnimation = findTranslateAnimation(this.mAnimation);
        if (openTranslateAnimation == null) {
            return SystemClock.uptimeMillis();
        }
        return ((SystemClock.uptimeMillis() + openTranslateAnimation.getStartOffset()) + ((long) (((float) openTranslateAnimation.getDuration()) * findAlmostThereFraction(openTranslateAnimation.getInterpolator())))) - 120;
    }

    public boolean canSkipFirstFrame() {
        return this.mCanSkipFirstFrame;
    }

    public boolean needsEarlyWakeup() {
        return this.mIsAppAnimation;
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.println(this.mAnimation);
    }

    public void writeToProtoInner(ProtoOutputStream proto) {
        long token = proto.start(1146756268033L);
        proto.write(1138166333441L, this.mAnimation.toString());
        proto.end(token);
    }

    private static TranslateAnimation findTranslateAnimation(Animation animation) {
        if (animation instanceof TranslateAnimation) {
            return (TranslateAnimation) animation;
        }
        if (!(animation instanceof AnimationSet)) {
            return null;
        }
        AnimationSet set = (AnimationSet) animation;
        for (int i = 0; i < set.getAnimations().size(); i++) {
            Animation a = set.getAnimations().get(i);
            if (a instanceof TranslateAnimation) {
                return (TranslateAnimation) a;
            }
        }
        return null;
    }

    private static float findAlmostThereFraction(Interpolator interpolator) {
        float val = 0.5f;
        for (float adj = 0.25f; adj >= 0.01f; adj /= 2.0f) {
            if (interpolator.getInterpolation(val) < 0.99f) {
                val += adj;
            } else {
                val -= adj;
            }
        }
        return val;
    }

    /* access modifiers changed from: private */
    public static class TmpValues {
        final float[] floats;
        final Transformation transformation;

        private TmpValues() {
            this.transformation = new Transformation();
            this.floats = new float[9];
        }
    }
}
