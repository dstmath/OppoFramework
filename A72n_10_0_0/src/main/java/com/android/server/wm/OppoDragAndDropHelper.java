package com.android.server.wm;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.os.SystemProperties;
import android.view.SurfaceControl;
import android.view.animation.Interpolator;
import com.android.server.display.OppoBrightUtils;

public class OppoDragAndDropHelper {
    private static final String ANIMATED_PROPERTY_ALPHA = "alpha";
    private static final String ANIMATED_PROPERTY_SCALE = "scale";
    private static final String ANIMATED_PROPERTY_X = "x";
    private static final String ANIMATED_PROPERTY_Y = "y";
    private static final long MAX_ANIMATION_DURATION_MS = 375;
    private static final long MIN_ANIMATION_DURATION_MS = 195;
    private int DRAG_STATE_HOLD = 1;
    private int DRAG_STATE_NORMAL = 0;
    private int DRAG_STATE_OFFSET = 2;
    private final String PERSIST_KEY = "persist.sys.oppo.screendrag";
    private final String PERSIST_KEY_METRICS = "persist.sys.oppo.displaymetrics";
    private final String PERSIST_KEY_STATE = "persist.sys.oppo.dragstate";
    private final String SPLIT_PROP = ",";
    private DragDropController mDragDropController;
    private boolean mOneHandModel = false;
    private float mScale = 1.0f;
    private float mXOffSet = OppoBrightUtils.MIN_LUX_LIMITI;
    private float mYOffSet = OppoBrightUtils.MIN_LUX_LIMITI;

    OppoDragAndDropHelper(DragDropController dragDropController) {
        this.mDragDropController = dragDropController;
    }

    /* access modifiers changed from: package-private */
    public void initialState() {
        this.mOneHandModel = isOffsetState();
        this.mXOffSet = (float) getOffsetX();
        this.mYOffSet = (float) getOffsetY();
        this.mScale = getScale();
    }

    /* access modifiers changed from: package-private */
    public void adjustInitialPosition(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, float touchX, float touchY, float thumbCenterX, float thumbCenterY) {
        float tmpY;
        float tmpX;
        if (this.mOneHandModel) {
            float f = this.mScale;
            transaction.setMatrix(surfaceControl, f, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, f);
            float f2 = this.mScale;
            float touchX2 = (touchX * f2) + this.mXOffSet;
            tmpX = touchX2 - (thumbCenterX * f2);
            tmpY = ((touchY * f2) + this.mYOffSet) - (f2 * thumbCenterY);
        } else {
            tmpX = touchX - thumbCenterX;
            tmpY = touchY - thumbCenterY;
        }
        transaction.setPosition(surfaceControl, tmpX, tmpY);
    }

    /* access modifiers changed from: package-private */
    public void adjustSurfacePosition(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, float x, float y, float thumbOffsetX, float thumbOffsetY) {
        float tmpY;
        float tmpX;
        if (this.mOneHandModel) {
            float f = this.mScale;
            tmpX = x - (thumbOffsetX * f);
            tmpY = y - (f * thumbOffsetY);
        } else {
            tmpX = x - thumbOffsetX;
            tmpY = y - thumbOffsetY;
        }
        transaction.setPosition(surfaceControl, tmpX, tmpY).apply();
    }

    /* access modifiers changed from: package-private */
    public float adjustXCoordinateToWholeSituation(float x) {
        if (!this.mOneHandModel) {
            return x;
        }
        float f = this.mScale;
        if (f != OppoBrightUtils.MIN_LUX_LIMITI) {
            return x + (this.mXOffSet / f);
        }
        return x;
    }

    /* access modifiers changed from: package-private */
    public float adjustYCoordinateToWholeSituation(float y) {
        if (!this.mOneHandModel) {
            return y;
        }
        float f = this.mScale;
        if (f != OppoBrightUtils.MIN_LUX_LIMITI) {
            return y + (this.mYOffSet / f);
        }
        return y;
    }

    /* access modifiers changed from: package-private */
    public float adjustXCoordinateToWindow(float x) {
        if (!this.mOneHandModel) {
            return x;
        }
        float f = this.mScale;
        if (f != OppoBrightUtils.MIN_LUX_LIMITI) {
            return (x - this.mXOffSet) / f;
        }
        return x;
    }

    /* access modifiers changed from: package-private */
    public float adjustYCoordinateToWindow(float y) {
        if (!this.mOneHandModel) {
            return y;
        }
        float f = this.mScale;
        if (f != OppoBrightUtils.MIN_LUX_LIMITI) {
            return (y - this.mYOffSet) / f;
        }
        return y;
    }

    /* access modifiers changed from: package-private */
    public boolean inOneHandModel() {
        return this.mOneHandModel;
    }

    /* access modifiers changed from: package-private */
    public ValueAnimator createReturnAnimationLockedinOneHandModel(float currentX, float thumbOffsetX, float originalX, float currentY, float thumbOffsetY, float originalY, float originalAlpha, Point displaySize, DragState dragState, Interpolator cubicEaseOutInterpolator, SurfaceControl surfaceControl) {
        float f = this.mScale;
        float tmpOriginalX = ((originalX * f) + this.mXOffSet) - (thumbOffsetX * f);
        float f2 = this.mScale;
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_X, currentX - (thumbOffsetX * f), tmpOriginalX), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_Y, currentY - (thumbOffsetY * f), ((originalY * f) + this.mYOffSet) - (f * thumbOffsetY)), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_SCALE, f2, f2), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_ALPHA, originalAlpha, originalAlpha / 2.0f));
        float f3 = this.mScale;
        float translateX = ((originalX * f3) + this.mXOffSet) - currentX;
        float translateY = ((f3 * originalY) + this.mYOffSet) - currentY;
        long duration = ((long) ((Math.sqrt((double) ((translateX * translateX) + (translateY * translateY))) / Math.sqrt((double) ((displaySize.x * displaySize.x) + (displaySize.y * displaySize.y)))) * 180.0d)) + MIN_ANIMATION_DURATION_MS;
        AnimationListener listener = new AnimationListener(dragState, surfaceControl);
        animator.setDuration(duration);
        animator.setInterpolator(cubicEaseOutInterpolator);
        animator.addListener(listener);
        animator.addUpdateListener(listener);
        dragState.mService.mAnimationHandler.post(new Runnable(animator) {
            /* class com.android.server.wm.$$Lambda$OppoDragAndDropHelper$x2TGwQ_LSPcp3gaQ_3sI4jUxeJ0 */
            private final /* synthetic */ ValueAnimator f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                OppoDragAndDropHelper.lambda$createReturnAnimationLockedinOneHandModel$0(this.f$0);
            }
        });
        return animator;
    }

    private class AnimationListener implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private DragState mDragState;
        private SurfaceControl mSurfaceControl;

        AnimationListener(DragState dragState, SurfaceControl surfaceControl) {
            this.mDragState = dragState;
            this.mSurfaceControl = surfaceControl;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
            r1.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x005f, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0060, code lost:
            r0.addSuppressed(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0063, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x005a, code lost:
            r2 = move-exception;
         */
        public void onAnimationUpdate(ValueAnimator animation) {
            SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
            transaction.setPosition(this.mSurfaceControl, ((Float) animation.getAnimatedValue(OppoDragAndDropHelper.ANIMATED_PROPERTY_X)).floatValue(), ((Float) animation.getAnimatedValue(OppoDragAndDropHelper.ANIMATED_PROPERTY_Y)).floatValue());
            transaction.setAlpha(this.mSurfaceControl, ((Float) animation.getAnimatedValue(OppoDragAndDropHelper.ANIMATED_PROPERTY_ALPHA)).floatValue());
            transaction.setMatrix(this.mSurfaceControl, ((Float) animation.getAnimatedValue(OppoDragAndDropHelper.ANIMATED_PROPERTY_SCALE)).floatValue(), OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, ((Float) animation.getAnimatedValue(OppoDragAndDropHelper.ANIMATED_PROPERTY_SCALE)).floatValue());
            transaction.apply();
            transaction.close();
        }

        public void onAnimationStart(Animator animator) {
        }

        public void onAnimationCancel(Animator animator) {
        }

        public void onAnimationRepeat(Animator animator) {
        }

        public void onAnimationEnd(Animator animator) {
            this.mDragState.mAnimationCompleted = true;
            OppoDragAndDropHelper.this.mDragDropController.sendHandlerMessage(2, null);
        }
    }

    private boolean isOffsetState() {
        return this.DRAG_STATE_OFFSET == getScreenDragState();
    }

    private int getScreenDragState() {
        return Integer.parseInt(SystemProperties.get("persist.sys.oppo.screendrag", "0,0,0,0").split(",")[0]);
    }

    private int getOffsetX() {
        return Integer.parseInt(SystemProperties.get("persist.sys.oppo.screendrag", "0,0,0,0").split(",")[1]);
    }

    private int getOffsetY() {
        return Integer.parseInt(SystemProperties.get("persist.sys.oppo.screendrag", "0,0,0,0").split(",")[2]);
    }

    private float getScale() {
        return Float.parseFloat(SystemProperties.get("persist.sys.oppo.screendrag", "0,0,0,0").split(",")[3]);
    }
}
