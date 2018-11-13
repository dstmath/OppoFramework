package com.color.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.IntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import com.android.internal.widget.ExploreByTouchHelper;
import com.color.util.ColorDialogUtil;
import com.color.view.ColorHapticFeedbackConstants;
import java.util.LinkedList;
import oppo.R;

public class ColorSimpleLock extends View {
    public static final int DEFAULTTYPE = 0;
    private static final int FOURCIRCLE = 4;
    private static final int FOURINTERVAL = 3;
    private static final int SIXCIRCLE = 6;
    public static final int SIXCIRCLETYPE = 1;
    private static final int SIXINTERVAL = 5;
    private static final String TAG = "ColorSimpleLock";
    private final int ADD_ANIMATION;
    private final int CLEAR_ALL_ANIMATION;
    private final int DELETE_ANIMATION;
    private final int DRAW_ALL_ANIMATION;
    private final int FAILED_ANIMATION;
    private final int MORPHING_CIRCLE_TO_LINE_TIME;
    private final int MORPHING_LINE_TO_CIRCLE_TIME;
    private final int TRANSLATION_X_TIME;
    private int animationMode;
    private boolean ctl_lastDraw;
    private boolean fail_lastDraw;
    private boolean isFingerprintMode;
    private boolean ltc_lastDraw;
    private ValueAnimator mAddAnimator;
    private Drawable mCircleDrawable;
    private int mCircleNum;
    private int mCirclePadding;
    private int mCircleType;
    private int mCirclesNumber;
    private int mCirclesWidth;
    private int mCodeImageStart;
    public int mCodeNumber;
    private int mContentWidth;
    private String mDecription;
    private ValueAnimator mDeleteAnimator;
    private boolean mDrawFailedAnimation;
    private Drawable mDrawable;
    private int mDrawableHeight;
    private int mDrawableWidth;
    private Animator mFailedAnimator;
    private boolean mIsVibrator;
    private Drawable mLineDrawable;
    private LinkedList<String> mNumberStrList;
    private float mScaleX;
    private float mScaleY;
    private SimpleLockTouchHelper mTouchHelper;
    private float mTransitionX;
    private final int mTranslationX_far;
    private final int mTranslationX_near;
    private final int mTranslationxMiddle;

    private final class SimpleLockTouchHelper extends ExploreByTouchHelper {
        private Rect mTempRect = new Rect();

        public SimpleLockTouchHelper(View forView) {
            super(forView);
        }

        protected int getVirtualViewAt(float x, float y) {
            if (x < 0.0f || x > ((float) ColorSimpleLock.this.mContentWidth) || y < 0.0f || y > ((float) ColorSimpleLock.this.mDrawableHeight)) {
                return -2;
            }
            return 0;
        }

        protected void getVisibleVirtualViews(IntArray virtualViewIds) {
            for (int i = 0; i < 1; i++) {
                virtualViewIds.add(i);
            }
        }

        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.getText().add(getItemDescription(virtualViewId));
        }

        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(host, event);
        }

        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            node.setContentDescription(getItemDescription(virtualViewId));
            node.addAction(AccessibilityAction.ACTION_CLICK);
            setRectBounds(virtualViewId, this.mTempRect);
            node.setBoundsInParent(this.mTempRect);
        }

        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            switch (action) {
                case ColorDialogUtil.BIT_FOUSED_BUTTON_NEGATIVE /*16*/:
                    return onItemClicked(virtualViewId);
                default:
                    return false;
            }
        }

        boolean onItemClicked(int index) {
            sendEventForVirtualView(index, 1);
            return false;
        }

        public CharSequence getItemDescription(int virtualViewId) {
            if (ColorSimpleLock.this.mDecription == null || ColorSimpleLock.this.mNumberStrList == null) {
                return getClass().getSimpleName();
            }
            ColorSimpleLock.this.mDecription = ColorSimpleLock.this.mDecription.replace('y', String.valueOf(ColorSimpleLock.this.mCircleNum).charAt(0));
            return ColorSimpleLock.this.mDecription.replace('x', String.valueOf(ColorSimpleLock.this.mNumberStrList.size()).charAt(0));
        }

        public void setRectBounds(int position, Rect rect) {
            if (position >= 0 && position < 1) {
                rect.set(0, 0, ColorSimpleLock.this.mContentWidth, ColorSimpleLock.this.mDrawableHeight);
            }
        }
    }

    public ColorSimpleLock(Context context) {
        this(context, null);
    }

    public ColorSimpleLock(Context context, AttributeSet attrs) {
        this(context, attrs, 201393298);
    }

    public ColorSimpleLock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCodeNumber = -1;
        this.DELETE_ANIMATION = 1;
        this.ADD_ANIMATION = 2;
        this.CLEAR_ALL_ANIMATION = 3;
        this.DRAW_ALL_ANIMATION = 4;
        this.FAILED_ANIMATION = 5;
        this.mTranslationX_far = 111;
        this.mTranslationX_near = 30;
        this.mTranslationxMiddle = 70;
        this.MORPHING_LINE_TO_CIRCLE_TIME = 200;
        this.MORPHING_CIRCLE_TO_LINE_TIME = 150;
        this.TRANSLATION_X_TIME = ColorHapticFeedbackConstants.LONG_VIBRATE;
        this.mCirclesWidth = 0;
        this.mDrawable = null;
        this.ctl_lastDraw = false;
        this.ltc_lastDraw = false;
        this.fail_lastDraw = false;
        this.animationMode = 0;
        this.mDrawFailedAnimation = false;
        this.mAddAnimator = null;
        this.mDeleteAnimator = null;
        this.mFailedAnimator = null;
        this.mScaleX = 0.0f;
        this.mScaleY = 0.0f;
        this.mTransitionX = 0.0f;
        this.isFingerprintMode = false;
        this.mCircleType = -1;
        this.mCircleNum = -1;
        this.mNumberStrList = null;
        this.mTouchHelper = null;
        this.mDecription = null;
        this.mIsVibrator = true;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorSimpleLock, defStyle, 0);
        this.mCirclePadding = a.getDimensionPixelSize(1, 0);
        this.mCircleDrawable = a.getDrawable(2);
        this.mLineDrawable = a.getDrawable(3);
        this.mCircleType = a.getInteger(0, 0);
        a.recycle();
        if (this.mLineDrawable != null) {
            this.mDrawable = this.mLineDrawable;
            this.mDrawableWidth = this.mDrawable.getIntrinsicWidth();
            this.mDrawableHeight = this.mDrawable.getIntrinsicHeight();
            if (this.mCircleType == 0) {
                this.mCircleNum = 4;
                this.mCirclesWidth = (this.mDrawableWidth * 4) + (this.mCirclePadding * 3);
            } else if (this.mCircleType == 1) {
                this.mCircleNum = 6;
                this.mCirclesWidth = (this.mDrawableWidth * 6) + (this.mCirclePadding * 5);
            }
        }
        this.mTouchHelper = new SimpleLockTouchHelper(this);
        setAccessibilityDelegate(this.mTouchHelper);
        this.mNumberStrList = new LinkedList();
        this.mNumberStrList.clear();
        this.mDecription = context.getResources().getString(201590176);
        setImportantForAccessibility(1);
    }

    protected void onDraw(Canvas canvas) {
        switch (this.animationMode) {
            case 1:
                drawCircleToLine(canvas, this.mCodeNumber + 1);
                return;
            case 2:
                drawLineToCircle(canvas, this.mCodeNumber);
                return;
            case 3:
                drawClearAllAnimation(canvas, this.mCirclesNumber);
                return;
            case 4:
                drawAllCodeAnimation(canvas, this.mCirclesNumber);
                return;
            case 5:
                drawFailedAnimation(canvas, this.mCodeNumber);
                return;
            default:
                drawPreviousState(canvas, this.mCodeNumber);
                return;
        }
    }

    private int judgeType() {
        if (this.mCircleNum == 4) {
            return 4;
        }
        if (this.mCircleNum == 6) {
            return 6;
        }
        return -1;
    }

    private void drawAllCodeAnimation(Canvas canvas, int number) {
        int left = this.mCodeImageStart;
        int bottom = this.mDrawableHeight + 0;
        if (this.ltc_lastDraw) {
            drawPreviousState(canvas, this.mCodeNumber);
            this.animationMode = 0;
            return;
        }
        int count = judgeType();
        for (int i = 0; i < count; i++) {
            int right = left + this.mDrawableWidth;
            if (i <= number) {
                drawCircle(canvas, left, 0, right, bottom);
            }
            if (i > number) {
                drawScaleCircle(canvas, 0, left, right, bottom, this.mScaleX, this.mScaleY);
            }
            left = right + this.mCirclePadding;
        }
    }

    private void drawClearAllAnimation(Canvas canvas, int number) {
        int left = this.mCodeImageStart;
        int bottom = this.mDrawableHeight + 0;
        if (this.ctl_lastDraw) {
            drawPreviousState(canvas, this.mCodeNumber);
            this.animationMode = 0;
            return;
        }
        int count = judgeType();
        for (int i = 0; i < count; i++) {
            int right = left + this.mDrawableWidth;
            if (i <= number) {
                drawScaleCircle(canvas, 0, left, right, bottom, this.mScaleX, this.mScaleY);
            }
            if (i > number) {
                drawLine(canvas, left, 0, right, bottom);
            }
            left = right + this.mCirclePadding;
        }
    }

    public Animator getFailedAnimator() {
        this.mIsVibrator = true;
        return createFailedAnimator();
    }

    public Animator getAddAnimator() {
        return createMorphingAnimationLineToCircle();
    }

    public Animator getDeleteAnimator() {
        return createMorphingAnimationCircleToLine();
    }

    private void drawPreviousState(Canvas canvas, int number) {
        int left = this.mCodeImageStart;
        int bottom = this.mDrawableHeight + 0;
        int count = judgeType();
        for (int i = 0; i < count; i++) {
            int right = left + this.mDrawableWidth;
            if (i <= number) {
                drawCircle(canvas, left, 0, right, bottom);
            }
            if (i > number) {
                drawLine(canvas, left, 0, right, bottom);
            }
            left = right + this.mCirclePadding;
        }
    }

    public void setFingerprintRecognition(boolean isFingerprint) {
        this.isFingerprintMode = isFingerprint;
    }

    private void drawFailedAnimation(Canvas canvas, int number) {
        int left = this.mCodeImageStart;
        int bottom = this.mDrawableHeight + 0;
        float farTransition = (float) ((int) (this.mTransitionX * 111.0f));
        float nearTransition = (float) ((int) (this.mTransitionX * 30.0f));
        float middleTransition = (float) ((int) (this.mTransitionX * 70.0f));
        if (this.fail_lastDraw) {
            this.animationMode = 0;
            this.mDrawFailedAnimation = false;
            this.mCodeNumber = -1;
            drawPreviousState(canvas, this.mCodeNumber);
            return;
        }
        int count = judgeType();
        for (int i = 0; i < count; i++) {
            int leftTmp = getFailedLeftPos(i, farTransition, middleTransition, nearTransition, left, 0);
            drawScaleCircle(canvas, 0, leftTmp, leftTmp + this.mDrawableWidth, bottom, this.mScaleX, this.mScaleY);
            left = (this.mDrawableWidth + left) + this.mCirclePadding;
        }
    }

    private int getFailedLeftPos(int i, float farTransition, float middleTransition, float nearTransition, int left, int leftTmp) {
        if (this.mCircleNum == 4) {
            switch (i) {
                case 0:
                    return (int) (((float) left) - farTransition);
                case 1:
                    return (int) (((float) left) - nearTransition);
                case 2:
                    return (int) (((float) left) + nearTransition);
                case 3:
                    return (int) (((float) left) + farTransition);
                default:
                    return leftTmp;
            }
        } else if (this.mCircleNum != 6) {
            return leftTmp;
        } else {
            switch (i) {
                case 0:
                    return (int) (((float) left) - farTransition);
                case 1:
                    return (int) (((float) left) - middleTransition);
                case 2:
                    return (int) (((float) left) - nearTransition);
                case 3:
                    return (int) (((float) left) + nearTransition);
                case 4:
                    return (int) (((float) left) + middleTransition);
                case 5:
                    return (int) (((float) left) + farTransition);
                default:
                    return leftTmp;
            }
        }
    }

    private void drawCircleToLine(Canvas canvas, int number) {
        int left = this.mCodeImageStart;
        int bottom = this.mDrawableHeight + 0;
        if (this.ctl_lastDraw) {
            this.animationMode = 0;
            drawPreviousState(canvas, this.mCodeNumber);
            return;
        }
        int count = judgeType();
        for (int i = 0; i < count; i++) {
            int right = left + this.mDrawableWidth;
            if (i < number) {
                drawCircle(canvas, left, 0, right, bottom);
            }
            if (i > number) {
                drawLine(canvas, left, 0, right, bottom);
            }
            if (i == number) {
                drawScaleCircle(canvas, 0, left, right, bottom, this.mScaleX, this.mScaleY);
            }
            left = right + this.mCirclePadding;
        }
    }

    private void drawCircle(Canvas canvas, int left, int top, int right, int bottom) {
        this.mDrawable = this.mCircleDrawable;
        this.mDrawable.setBounds(left, top, right, bottom);
        this.mDrawable.draw(canvas);
    }

    private void drawLine(Canvas canvas, int left, int top, int right, int bottom) {
        this.mDrawable = this.mLineDrawable;
        this.mDrawable.setBounds(left, top, right, bottom);
        this.mDrawable.draw(canvas);
    }

    private void drawLineToCircle(Canvas canvas, int number) {
        int left = this.mCodeImageStart;
        int bottom = this.mDrawableHeight + 0;
        if (this.ltc_lastDraw) {
            this.animationMode = 0;
            drawPreviousState(canvas, this.mCodeNumber);
            return;
        }
        int count = judgeType();
        for (int i = 0; i < count; i++) {
            int right = left + this.mDrawableWidth;
            if (i < number) {
                drawCircle(canvas, left, 0, right, bottom);
            }
            if (i > number) {
                drawLine(canvas, left, 0, right, bottom);
            }
            if (i == number) {
                drawScaleCircle(canvas, 0, left, right, bottom, this.mScaleX, this.mScaleY);
            }
            left = (this.mDrawableWidth + left) + this.mCirclePadding;
        }
    }

    private void drawScaleCircle(Canvas canvas, int top, int left, int right, int bottom, float scaleX, float scaleY) {
        this.mDrawable = this.mCircleDrawable;
        int centerX = (left + right) / 2;
        int centerY = (top + bottom) / 2;
        int xRetract = (int) ((((float) this.mDrawableWidth) * scaleX) / 2.0f);
        int yRetract = (int) ((((float) this.mDrawableHeight) * scaleY) / 2.0f);
        top = centerY - yRetract;
        this.mDrawable.setBounds(centerX - xRetract, top, centerX + xRetract, centerY + yRetract);
        this.mDrawable.draw(canvas);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mContentWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.mCodeImageStart = (this.mContentWidth - this.mCirclesWidth) / 2;
        setMeasuredDimension(this.mContentWidth, this.mDrawableHeight);
    }

    public void setOneCode(int number) {
        if (this.mCircleNum == 4) {
            if (this.mCodeNumber > 3) {
                return;
            }
        } else if (this.mCircleNum == 6 && this.mCodeNumber > 5) {
            return;
        }
        if (this.mDrawFailedAnimation) {
            this.mCodeNumber = -1;
            this.mDrawFailedAnimation = false;
            if (this.mFailedAnimator != null && this.animationMode == 5) {
                this.mFailedAnimator.end();
            }
        }
        if (this.mCircleNum == 4) {
            if (this.mCodeNumber == 3) {
                this.mCodeNumber = -1;
            }
        } else if (this.mCircleNum == 6 && this.mCodeNumber == 5) {
            this.mCodeNumber = -1;
        }
        if (this.mDeleteAnimator != null && this.mDeleteAnimator.isRunning()) {
            this.mDeleteAnimator.end();
        }
        if (this.mAddAnimator != null && this.mAddAnimator.isRunning()) {
            this.mAddAnimator.end();
        }
        this.animationMode = 2;
        this.mCodeNumber++;
        this.mAddAnimator = createMorphingAnimationLineToCircle();
        this.mAddAnimator.start();
        if (this.mNumberStrList != null) {
            String numberStr = String.valueOf(number);
            if (this.mCodeNumber != this.mCircleNum - 1) {
                this.mNumberStrList.addFirst(numberStr);
            } else {
                this.mNumberStrList.clear();
            }
        }
    }

    public void setAllCode(boolean drawAll) {
        if (this.mCircleNum == 4) {
            if (this.mDrawFailedAnimation || this.mCodeNumber >= 3 || (this.mFailedAnimator != null && this.mFailedAnimator.isRunning())) {
                return;
            }
        } else if (this.mCircleNum == 6 && (this.mDrawFailedAnimation || this.mCodeNumber >= 5 || (this.mFailedAnimator != null && this.mFailedAnimator.isRunning()))) {
            return;
        }
        if (drawAll) {
            if (this.mDeleteAnimator != null && this.mDeleteAnimator.isRunning()) {
                this.mDeleteAnimator.end();
            }
            if (this.mAddAnimator != null && this.mAddAnimator.isRunning()) {
                this.mAddAnimator.end();
            }
            this.animationMode = 4;
            this.mCirclesNumber = this.mCodeNumber;
            if (this.mCircleNum == 4) {
                this.mCodeNumber = 3;
            } else if (this.mCircleNum == 6) {
                this.mCodeNumber = 5;
            }
            this.mAddAnimator = createMorphingAnimationLineToCircle();
            this.mAddAnimator.start();
        }
    }

    public void setFailed(boolean failed) {
        this.mDrawFailedAnimation = failed;
    }

    public void setDeleteLast(boolean delete) {
        if (!(this.mNumberStrList == null || (this.mNumberStrList.isEmpty() ^ 1) == 0)) {
            String numberStr = (String) this.mNumberStrList.removeFirst();
            if (!(this.mDecription == null || this.mNumberStrList == null)) {
                this.mDecription = this.mDecription.replace('y', String.valueOf(this.mCircleNum).charAt(0));
                announceForAccessibility(this.mDecription.replace('x', String.valueOf(this.mNumberStrList.size()).charAt(0)));
            }
        }
        if (this.mCircleNum == 4) {
            if (this.mCodeNumber == -1 || this.mDrawFailedAnimation || this.mCodeNumber >= 3 || (delete ^ 1) != 0 || (this.mFailedAnimator != null && this.mFailedAnimator.isRunning())) {
                return;
            }
        } else if (this.mCircleNum == 6 && (this.mCodeNumber == -1 || this.mDrawFailedAnimation || this.mCodeNumber >= 5 || (delete ^ 1) != 0 || (this.mFailedAnimator != null && this.mFailedAnimator.isRunning()))) {
            return;
        }
        this.mCodeNumber--;
        if (this.mCodeNumber >= -1) {
            if (this.mDeleteAnimator != null && this.mDeleteAnimator.isRunning()) {
                this.mDeleteAnimator.end();
            }
            if (this.mAddAnimator != null && this.mAddAnimator.isRunning()) {
                this.mAddAnimator.end();
            }
            this.animationMode = 1;
            this.mDeleteAnimator = createMorphingAnimationCircleToLine();
            this.mDeleteAnimator.start();
        } else {
            this.mCodeNumber = -1;
        }
    }

    public void setClearAll(boolean clear) {
        if (this.mCircleNum == 4) {
            if (this.mCodeNumber == -1 || this.mDrawFailedAnimation || this.mCodeNumber > 3 || (clear ^ 1) != 0 || (this.mFailedAnimator != null && this.mFailedAnimator.isRunning())) {
                return;
            }
        } else if (this.mCircleNum == 6 && (this.mCodeNumber == -1 || this.mDrawFailedAnimation || this.mCodeNumber > 5 || (clear ^ 1) != 0 || (this.mFailedAnimator != null && this.mFailedAnimator.isRunning()))) {
            return;
        }
        if (this.mDeleteAnimator != null && this.mDeleteAnimator.isRunning()) {
            this.mDeleteAnimator.end();
        }
        if (this.mAddAnimator != null && this.mAddAnimator.isRunning()) {
            this.mAddAnimator.end();
        }
        if (this.mNumberStrList != null) {
            this.mNumberStrList.clear();
        }
        this.animationMode = 3;
        this.mCirclesNumber = this.mCodeNumber;
        this.mCodeNumber = -1;
        this.mDeleteAnimator = createMorphingAnimationCircleToLine();
        this.mDeleteAnimator.start();
    }

    public void setScaleX(float scaleX) {
        this.mScaleX = scaleX;
    }

    public void setScaleY(float scaleY) {
        this.mScaleY = scaleY;
    }

    public void setTranslationX(float translationX) {
        this.mTransitionX = translationX;
    }

    public float translationValueForTranslant(float input) {
        if (input < 0.5f) {
            return (((input - 0.5f) * -2.0f) * (input - 0.5f)) + 0.5f;
        }
        if (input > 0.8f) {
            return (input - 1.3f) * input;
        }
        return ((input - 0.6f) * (input - 3.7f)) + 0.18f;
    }

    public float translationValueForScale(float input) {
        if (input < 0.5f) {
            return ((-(input - 0.5f)) * (input - 0.5f)) + 0.25f;
        }
        if (input > 0.8f) {
            return (((input - 0.8f) * 3.12f) * (input - 0.8f)) - 0.08f;
        }
        return ((input - 0.6f) * (input - 1.8f)) + 0.12f;
    }

    private ValueAnimator createMorphingAnimationLineToCircle() {
        if (this.mAddAnimator != null) {
            return this.mAddAnimator;
        }
        this.mAddAnimator = ValueAnimator.ofFloat(new float[]{0.3f, 1.6f});
        this.mAddAnimator.setDuration(200);
        this.mAddAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = ((Float) animation.getAnimatedValue()).floatValue();
                if (scale > 1.0f && scale <= 1.3f) {
                    ColorSimpleLock.this.setScaleY(scale);
                    ColorSimpleLock.this.setScaleX(scale);
                } else if (scale > 1.3f) {
                    ColorSimpleLock.this.setScaleY(2.6f - scale);
                    ColorSimpleLock.this.setScaleX(2.6f - scale);
                } else {
                    ColorSimpleLock.this.setScaleY(scale);
                    ColorSimpleLock.this.setScaleX(2.2f - (1.2f * scale));
                }
                ColorSimpleLock.this.invalidate();
            }
        });
        this.mAddAnimator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                ColorSimpleLock.this.ltc_lastDraw = false;
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                ColorSimpleLock.this.ltc_lastDraw = true;
                ColorSimpleLock.this.invalidate();
                if (ColorSimpleLock.this.mDrawFailedAnimation) {
                    ColorSimpleLock.this.animationMode = 5;
                    ColorSimpleLock.this.mFailedAnimator = ColorSimpleLock.this.createFailedAnimator();
                    ColorSimpleLock.this.mFailedAnimator.start();
                    ColorSimpleLock.this.mIsVibrator = true;
                }
            }

            public void onAnimationCancel(Animator animation) {
            }
        });
        return this.mAddAnimator;
    }

    private ValueAnimator createMorphingAnimationCircleToLine() {
        if (this.mDeleteAnimator != null) {
            return this.mDeleteAnimator;
        }
        this.mDeleteAnimator = ValueAnimator.ofFloat(new float[]{1.0f, 0.3f});
        this.mDeleteAnimator.setDuration(150);
        this.mDeleteAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = ((Float) animation.getAnimatedValue()).floatValue();
                ColorSimpleLock.this.setScaleY(scale);
                ColorSimpleLock.this.setScaleX(2.2f - (1.2f * scale));
                ColorSimpleLock.this.invalidate();
            }
        });
        this.mDeleteAnimator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                ColorSimpleLock.this.ctl_lastDraw = false;
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                ColorSimpleLock.this.ctl_lastDraw = true;
                ColorSimpleLock.this.invalidate();
            }

            public void onAnimationCancel(Animator animation) {
            }
        });
        return this.mDeleteAnimator;
    }

    public Animator createFailedAnimator() {
        if (this.mFailedAnimator != null) {
            return this.mFailedAnimator;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{0.0f, 1.3f});
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animator) {
                Float value = (Float) animator.getAnimatedValue();
                ColorSimpleLock.this.setTranslationX(ColorSimpleLock.this.translationValueForTranslant(value.floatValue()) * 2.0f);
                Float scaleValue = Float.valueOf(ColorSimpleLock.this.translationValueForScale(value.floatValue()));
                ColorSimpleLock.this.setScaleX((scaleValue.floatValue() * 1.2f) + 1.0f);
                ColorSimpleLock.this.setScaleY(1.0f - scaleValue.floatValue());
                ColorSimpleLock.this.invalidate();
            }
        });
        animator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator arg0) {
                ColorSimpleLock.this.animationMode = 5;
                ColorSimpleLock.this.setTranslationX(0.0f);
                ColorSimpleLock.this.setScaleX(1.0f);
                ColorSimpleLock.this.setScaleY(1.0f);
                ColorSimpleLock.this.fail_lastDraw = false;
                ColorSimpleLock.this.mDrawFailedAnimation = true;
                if (ColorSimpleLock.this.isFingerprintMode) {
                    ColorSimpleLock.this.isFingerprintMode = false;
                } else if (ColorSimpleLock.this.mIsVibrator) {
                    ColorSimpleLock.this.performHapticFeedback(ColorHapticFeedbackConstants.LONG_VIBRATE);
                    ColorSimpleLock.this.mIsVibrator = false;
                }
            }

            public void onAnimationRepeat(Animator arg0) {
            }

            public void onAnimationEnd(Animator arg0) {
                ColorSimpleLock.this.setTranslationX(0.0f);
                ColorSimpleLock.this.setScaleY(1.0f);
                ColorSimpleLock.this.setScaleX(1.0f);
                ColorSimpleLock.this.fail_lastDraw = true;
                ColorSimpleLock.this.mCodeNumber = -1;
                ColorSimpleLock.this.mDrawFailedAnimation = false;
                ColorSimpleLock.this.invalidate();
            }

            public void onAnimationCancel(Animator arg0) {
            }
        });
        animator.setDuration(300);
        return animator;
    }

    public void setSimpleLockType(int flag) {
        if (flag == 0) {
            this.mCircleNum = 4;
            this.mCirclesWidth = (this.mDrawableWidth * 4) + (this.mCirclePadding * 3);
        } else if (flag == 1) {
            this.mCircleNum = 6;
            this.mCirclesWidth = (this.mDrawableWidth * 6) + (this.mCirclePadding * 5);
        }
        this.mCodeImageStart = (this.mContentWidth - this.mCirclesWidth) / 2;
        invalidate();
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mTouchHelper == null || !this.mTouchHelper.dispatchHoverEvent(event)) {
            return super.dispatchHoverEvent(event);
        }
        return true;
    }
}
