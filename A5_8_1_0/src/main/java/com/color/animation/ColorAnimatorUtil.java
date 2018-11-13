package com.color.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.text.TextUtils;
import com.color.util.ColorLog;

public class ColorAnimatorUtil {
    public static void dump(String key, Class<?> cls, Animator animation, Object... args) {
        String info;
        StringBuilder infoBuilder = new StringBuilder();
        StringBuilder propBuilder = new StringBuilder();
        if (animation instanceof ObjectAnimator) {
            ObjectAnimator objAnim = (ObjectAnimator) animation;
            propBuilder.append(objAnim.getPropertyName());
            propBuilder.append(", ");
            info = TextUtils.isEmpty(key) ? false : ColorLog.getDebug(key) ? animation.toString() : objAnim.getTarget().getClass().getSimpleName();
        } else {
            info = animation.toString();
        }
        infoBuilder.append(info);
        if (animation instanceof ValueAnimator) {
            ValueAnimator valAnim = (ValueAnimator) animation;
            infoBuilder.append(", value=");
            infoBuilder.append(valAnim.getAnimatedValue());
        }
        ColorLog.d((Class) cls, ColorLog.buildMessage(args), " : ", propBuilder.toString(), Long.valueOf(animation.getDuration()), "ms, ", infoBuilder.toString());
    }
}
