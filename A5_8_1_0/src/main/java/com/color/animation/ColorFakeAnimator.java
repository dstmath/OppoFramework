package com.color.animation;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.TimeInterpolator;
import java.util.ArrayList;
import java.util.List;

public class ColorFakeAnimator extends Animator {
    private static final boolean DBG = false;
    private static final String TAG = "ColorFakeAnimator";

    public void start() {
        ArrayList<AnimatorListener> listeners = getListeners();
        onAnimationStart(listeners);
        onStart();
        onAnimationEnd(listeners);
    }

    public void cancel() {
        onAnimationCancel(getListeners());
    }

    public void end() {
        onAnimationEnd(getListeners());
    }

    public boolean isRunning() {
        return false;
    }

    public void setInterpolator(TimeInterpolator value) {
    }

    public long getDuration() {
        return 0;
    }

    public Animator setDuration(long duration) {
        return this;
    }

    public void setStartDelay(long startDelay) {
    }

    public long getStartDelay() {
        return 0;
    }

    protected void onStart() {
    }

    private void onAnimationStart(List<AnimatorListener> listeners) {
        if (listeners != null) {
            for (AnimatorListener listener : listeners) {
                listener.onAnimationStart(this);
            }
        }
    }

    private void onAnimationCancel(List<AnimatorListener> listeners) {
        if (listeners != null) {
            for (AnimatorListener listener : listeners) {
                listener.onAnimationCancel(this);
            }
        }
    }

    private void onAnimationEnd(List<AnimatorListener> listeners) {
        if (listeners != null) {
            for (AnimatorListener listener : listeners) {
                listener.onAnimationEnd(this);
            }
        }
    }
}
