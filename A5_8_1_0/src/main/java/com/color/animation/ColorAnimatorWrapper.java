package com.color.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import com.color.util.ColorLog;
import com.color.util.ColorLogKey.MultiChoice;

public class ColorAnimatorWrapper {
    private final Animator mAnimation;
    private final OnSetValuesCallback mCallback;
    protected final Class<?> mTagClass = getClass();

    public interface OnSetValuesCallback {
        float getEndValue(View view);

        float getPivotXValue(View view);

        float getPivotYValue(View view);

        float getStartValue(View view);

        void initialize(View view);
    }

    public static class OnSetValuesCallbackAdapter implements OnSetValuesCallback {
        public void initialize(View target) {
        }

        public float getPivotXValue(View target) {
            return target.getPivotX();
        }

        public float getPivotYValue(View target) {
            return target.getPivotY();
        }

        public float getStartValue(View target) {
            return 0.0f;
        }

        public float getEndValue(View target) {
            return 0.0f;
        }
    }

    public ColorAnimatorWrapper(Animator animation, OnSetValuesCallback callback) {
        this.mAnimation = animation;
        this.mCallback = callback;
    }

    public Animator getAnimation() {
        return this.mAnimation;
    }

    public void initialize() {
        if (this.mCallback != null && (this.mAnimation instanceof ValueAnimator)) {
            View target = getTarget();
            if (target != null) {
                target.setVisibility(0);
                this.mCallback.initialize(target);
                target.setPivotX(this.mCallback.getPivotXValue(target));
                target.setPivotY(this.mCallback.getPivotYValue(target));
                float startValue = this.mCallback.getStartValue(target);
                float endValue = this.mCallback.getEndValue(target);
                ((ValueAnimator) this.mAnimation).setFloatValues(new float[]{startValue, endValue});
                if (ColorLog.getDebug(MultiChoice.ANIM)) {
                    StringBuilder builder = new StringBuilder();
                    if (this.mAnimation instanceof ObjectAnimator) {
                        builder.append(" ");
                        builder.append(((ObjectAnimator) this.mAnimation).getPropertyName());
                    }
                    ColorLog.d(this.mTagClass, "updateValues", builder.toString(), " : ", Float.valueOf(startValue), "=>", Float.valueOf(endValue));
                }
            }
        }
    }

    private View getTarget() {
        if (this.mAnimation instanceof ObjectAnimator) {
            Object target = ((ObjectAnimator) this.mAnimation).getTarget();
            if (target instanceof View) {
                return (View) target;
            }
        }
        return null;
    }
}
