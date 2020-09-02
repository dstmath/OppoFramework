package com.color.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.net.wifi.WifiEnterpriseConfig;
import android.view.View;
import com.color.util.ColorLog;
import com.color.util.ColorLogKey;

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

    public ColorAnimatorWrapper(Animator animation, OnSetValuesCallback callback) {
        this.mAnimation = animation;
        this.mCallback = callback;
    }

    public Animator getAnimation() {
        return this.mAnimation;
    }

    public void initialize() {
        View target;
        if (this.mCallback != null && (this.mAnimation instanceof ValueAnimator) && (target = getTarget()) != null) {
            target.setVisibility(0);
            this.mCallback.initialize(target);
            target.setPivotX(this.mCallback.getPivotXValue(target));
            target.setPivotY(this.mCallback.getPivotYValue(target));
            float startValue = this.mCallback.getStartValue(target);
            float endValue = this.mCallback.getEndValue(target);
            ((ValueAnimator) this.mAnimation).setFloatValues(startValue, endValue);
            if (ColorLog.getDebug(ColorLogKey.MultiChoice.ANIM)) {
                StringBuilder builder = new StringBuilder();
                if (this.mAnimation instanceof ObjectAnimator) {
                    builder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                    builder.append(((ObjectAnimator) this.mAnimation).getPropertyName());
                }
                ColorLog.d(this.mTagClass, "updateValues", builder.toString(), " : ", Float.valueOf(startValue), "=>", Float.valueOf(endValue));
            }
        }
    }

    private View getTarget() {
        Animator animator = this.mAnimation;
        if (!(animator instanceof ObjectAnimator)) {
            return null;
        }
        Object target = ((ObjectAnimator) animator).getTarget();
        if (target instanceof View) {
            return (View) target;
        }
        return null;
    }

    public static class OnSetValuesCallbackAdapter implements OnSetValuesCallback {
        @Override // com.color.animation.ColorAnimatorWrapper.OnSetValuesCallback
        public void initialize(View target) {
        }

        @Override // com.color.animation.ColorAnimatorWrapper.OnSetValuesCallback
        public float getPivotXValue(View target) {
            return target.getPivotX();
        }

        @Override // com.color.animation.ColorAnimatorWrapper.OnSetValuesCallback
        public float getPivotYValue(View target) {
            return target.getPivotY();
        }

        @Override // com.color.animation.ColorAnimatorWrapper.OnSetValuesCallback
        public float getStartValue(View target) {
            return 0.0f;
        }

        @Override // com.color.animation.ColorAnimatorWrapper.OnSetValuesCallback
        public float getEndValue(View target) {
            return 0.0f;
        }
    }
}
