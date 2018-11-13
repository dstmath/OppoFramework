package com.color.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import com.color.animation.ColorAnimatorUtil;
import com.color.util.ColorLog;
import com.color.util.ColorLogKey.BottomMenu;
import com.color.widget.ColorBottomMenuCallback.Updater;

public class ColorBottomMenuAnimator implements Updater {
    private static final int ALL_ITEMS_UPDATE = -1;
    private static final long ANIM_DURATION = 200;
    private static final long ITEM_DELAY = 60;
    private static final String TAG_DOWN = "Down";
    private static final String TAG_UP = "Up";
    private final ColorBottomMenuCallback mCallback;
    protected final Class<?> mTagClass = getClass();

    private class RunListener extends AnimatorListenerAdapter {
        private final float mEndValue;
        private final int mIndex;
        private final String mTag;
        private final boolean mUpdate;

        public RunListener(String tag, int index, boolean update, float endValue) {
            this.mTag = tag + "." + index;
            this.mIndex = index;
            this.mUpdate = update;
            this.mEndValue = endValue;
        }

        public void onAnimationStart(Animator animation) {
            ColorBottomMenuAnimator.this.dumpAnimation(animation, this.mTag, "onAnimationStart");
        }

        public void onAnimationEnd(Animator animation) {
            ColorBottomMenuAnimator.this.dumpAnimation(animation, this.mTag, "onAnimationEnd");
            if (ColorBottomMenuAnimator.this.mCallback != null) {
                ColorBottomMenuAnimator.this.mCallback.updateMenuScrollPosition(this.mIndex, this.mEndValue);
                if (this.mUpdate) {
                    ColorBottomMenuAnimator.this.mCallback.updateMenuScrollData();
                }
            }
        }
    }

    private class SetListener extends AnimatorListenerAdapter {
        private final String mTag;

        public SetListener(String tag) {
            this.mTag = tag;
        }

        public void onAnimationEnd(Animator animation) {
            ColorBottomMenuAnimator.this.dumpAnimation(animation, this.mTag, "onAnimationEnd");
            if (ColorBottomMenuAnimator.this.mCallback != null) {
                ColorBottomMenuAnimator.this.mCallback.updateMenuScrollState(0);
            }
        }
    }

    private class UpdateListener implements AnimatorUpdateListener {
        private final int mIndex;
        private final String mTag;

        public UpdateListener(String tag, int index) {
            this.mTag = tag + "." + index;
            this.mIndex = index;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            if (ColorBottomMenuAnimator.this.mCallback != null) {
                ColorBottomMenuAnimator.this.mCallback.updateMenuScrollPosition(this.mIndex, ((Float) animation.getAnimatedValue()).floatValue());
            }
        }
    }

    public ColorBottomMenuAnimator(ColorBottomMenuCallback callback) {
        this.mCallback = callback;
    }

    public Animator getUpdater(int currCount, int nextCount) {
        ColorLog.d(BottomMenu.UPDATE, this.mTagClass, "----------------------------getAnimatorUpdater : ", this.mCallback.getClass().getSimpleName(), " : ", Integer.valueOf(currCount), "=>", Integer.valueOf(nextCount));
        if (currCount > 0 || nextCount > 0) {
            String tag;
            AnimatorSet animSet = new AnimatorSet();
            if (currCount <= 0) {
                animSet.play(getItemUp(nextCount)).after(getViewUp());
                tag = "MenuShow";
            } else if (nextCount <= 0) {
                animSet.play(getViewDown());
                tag = "MenuHide";
            } else {
                animSet.play(getItemUp(nextCount)).after(getItemDown(currCount));
                tag = "MenuSwitch";
            }
            animSet.addListener(new SetListener(tag));
            return animSet;
        }
        if (this.mCallback != null) {
            this.mCallback.updateMenuScrollState(0);
        }
        return null;
    }

    public boolean visibleFirst() {
        return true;
    }

    private Animator createAnimator(String tag, int index, float startValue, float endValue, long delay, boolean update) {
        ValueAnimator anim = ValueAnimator.ofFloat(new float[]{startValue, endValue});
        anim.addUpdateListener(new UpdateListener(tag, index));
        anim.addListener(new RunListener(tag, index, update, endValue));
        anim.setDuration(ANIM_DURATION);
        anim.setStartDelay(delay);
        anim.setInterpolator(ColorBottomMenuCallback.ANIMATOR_INTERPOLATOR);
        anim.setRepeatCount(0);
        return anim;
    }

    private Animator getViewAnim(boolean in) {
        return createAnimator("View" + (in ? TAG_UP : TAG_DOWN), -1, in ? 0.0f : 1.0f, in ? 1.0f : 0.0f, 0, in);
    }

    private Animator getViewUp() {
        return getViewAnim(true);
    }

    private Animator getViewDown() {
        return getViewAnim(false);
    }

    private Animator getItemAnim(int count, boolean in) {
        String tag = "Item" + (in ? TAG_UP : TAG_DOWN);
        float startValue = in ? 1.0f : 0.0f;
        float endValue = in ? 0.0f : 1.0f;
        AnimatorSet animSet = new AnimatorSet();
        Builder builder = null;
        for (int i = 0; i < count; i++) {
            Animator anim = createAnimator(tag, i, startValue, endValue, in ? ITEM_DELAY * ((long) i) : 0, in ^ 1);
            if (builder == null) {
                builder = animSet.play(anim);
            } else {
                builder.with(anim);
            }
        }
        return animSet;
    }

    private Animator getItemUp(int count) {
        return getItemAnim(count, true);
    }

    private Animator getItemDown(int count) {
        return getItemAnim(count, false);
    }

    private String getMessage(String tag, String message) {
        StringBuilder builder = new StringBuilder();
        builder.append(message);
        builder.append(" ");
        builder.append(tag);
        if (this.mCallback != null) {
            builder.append(" : ");
            builder.append(this.mCallback.getClass().getSimpleName());
        }
        return builder.toString();
    }

    private void dumpAnimation(Animator anim, String tag, String msg) {
        if (ColorLog.getDebug(BottomMenu.ANIM)) {
            ColorAnimatorUtil.dump(BottomMenu.ANIM2, this.mTagClass, anim, getMessage(tag, msg));
        }
    }
}
