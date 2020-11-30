package com.color.widget.indicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.color.util.ColorContextUtil;
import com.color.util.ColorDarkModeUtil;
import com.oppo.internal.R;
import java.util.ArrayList;
import java.util.List;

public class ColorPageIndicator extends FrameLayout implements IPagerIndicator {
    private static final boolean DEBUG = false;
    private static final int MSG_START_TRACE_ANIMATION = 17;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_SETTLING = 2;
    private static final String TAG = "ColorPageIndicator2";
    private int mCurrentPosition;
    private int mDotColor;
    private int mDotCornerRadius;
    private int mDotSize;
    private int mDotSpacing;
    private int mDotStepDistance;
    private int mDotStrokeWidth;
    private int mDotsCount;
    private float mFinalLeft;
    private float mFinalRight;
    private Handler mHandler;
    private List<View> mIndicatorDots;
    private LinearLayout mIndicatorDotsParent;
    private boolean mIsAnimated;
    private boolean mIsAnimating;
    private boolean mIsAnimatorCanceled;
    private boolean mIsClickable;
    private boolean mIsPaused;
    private boolean mIsStrokeStyle;
    private int mLastPosition;
    private boolean mNeedSettlePositionTemp;
    private OnIndicatorDotClickListener mOnDotClickListener;
    private ValueAnimator mTraceAnimator;
    private int mTraceDotColor;
    private float mTraceLeft;
    private Paint mTracePaint;
    private RectF mTraceRect;
    private float mTraceRight;
    private boolean mTranceCutTailRight;
    private int mWidth;

    public interface OnIndicatorDotClickListener {
        void onClick(int i);
    }

    public ColorPageIndicator(Context context) {
        this(context, null);
    }

    public ColorPageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(21)
    public ColorPageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mDotStepDistance = 0;
        this.mTraceLeft = 0.0f;
        this.mTraceRight = 0.0f;
        this.mFinalLeft = 0.0f;
        this.mFinalRight = 0.0f;
        this.mTranceCutTailRight = false;
        this.mIsAnimated = false;
        this.mIsAnimating = false;
        this.mIsAnimatorCanceled = false;
        this.mIsPaused = false;
        this.mNeedSettlePositionTemp = false;
        this.mTraceRect = new RectF();
        ColorDarkModeUtil.setForceDarkAllow(this, false);
        this.mIndicatorDots = new ArrayList();
        this.mDotSize = dpToPx(10);
        this.mDotSpacing = dpToPx(5);
        this.mDotStrokeWidth = dpToPx(2);
        this.mDotCornerRadius = this.mDotSize / 2;
        this.mDotColor = -1;
        this.mTraceDotColor = ColorContextUtil.getAttrColor(context, 201392730);
        this.mIsClickable = true;
        this.mIsStrokeStyle = true;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorPageIndicator);
            this.mTraceDotColor = typedArray.getColor(1, this.mTraceDotColor);
            if (!ColorDarkModeUtil.isNightMode(context)) {
                this.mDotColor = typedArray.getColor(0, this.mDotColor);
            }
            this.mDotSize = (int) typedArray.getDimension(2, (float) this.mDotSize);
            this.mDotSpacing = (int) typedArray.getDimension(3, (float) this.mDotSpacing);
            this.mDotCornerRadius = (int) typedArray.getDimension(5, (float) (this.mDotSize / 2));
            this.mIsClickable = typedArray.getBoolean(6, this.mIsClickable);
            this.mDotStrokeWidth = (int) typedArray.getDimension(4, (float) this.mDotStrokeWidth);
            typedArray.recycle();
        }
        RectF rectF = this.mTraceRect;
        rectF.top = 0.0f;
        rectF.bottom = (float) this.mDotSize;
        this.mTraceAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mTraceAnimator.setDuration(240L);
        if (Build.VERSION.SDK_INT >= 21) {
            this.mTraceAnimator.setInterpolator(new PathInterpolator(0.25f, 0.1f, 0.25f, 1.0f));
        }
        this.mTraceAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.color.widget.indicator.ColorPageIndicator.AnonymousClass1 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                if (!ColorPageIndicator.this.mIsAnimatorCanceled) {
                    float diffLeft = ColorPageIndicator.this.mTraceLeft - ColorPageIndicator.this.mFinalLeft;
                    float diffRight = ColorPageIndicator.this.mTraceRight - ColorPageIndicator.this.mFinalRight;
                    float left = ColorPageIndicator.this.mTraceLeft - (diffLeft * value);
                    if (left > ColorPageIndicator.this.mTraceRect.right - ((float) ColorPageIndicator.this.mDotSize)) {
                        left = ColorPageIndicator.this.mTraceRect.right - ((float) ColorPageIndicator.this.mDotSize);
                    }
                    float right = ColorPageIndicator.this.mTraceRight - (diffRight * value);
                    if (right < ColorPageIndicator.this.mTraceRect.left + ((float) ColorPageIndicator.this.mDotSize)) {
                        right = ColorPageIndicator.this.mTraceLeft + ((float) ColorPageIndicator.this.mDotSize);
                    }
                    if (ColorPageIndicator.this.mNeedSettlePositionTemp) {
                        ColorPageIndicator.this.mTraceRect.left = left;
                        ColorPageIndicator.this.mTraceRect.right = right;
                    } else if (ColorPageIndicator.this.mTranceCutTailRight) {
                        ColorPageIndicator.this.mTraceRect.right = right;
                    } else {
                        ColorPageIndicator.this.mTraceRect.left = left;
                    }
                    ColorPageIndicator.this.invalidate();
                }
            }
        });
        this.mTraceAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.color.widget.indicator.ColorPageIndicator.AnonymousClass2 */

            @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!ColorPageIndicator.this.mIsAnimatorCanceled) {
                    ColorPageIndicator.this.mTraceRect.right = ColorPageIndicator.this.mTraceRect.left + ((float) ColorPageIndicator.this.mDotSize);
                    ColorPageIndicator.this.mNeedSettlePositionTemp = false;
                    ColorPageIndicator.this.mIsAnimated = true;
                    ColorPageIndicator.this.invalidate();
                }
                ColorPageIndicator.this.mIsAnimating = false;
            }

            @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                ColorPageIndicator.this.mIsAnimatorCanceled = false;
                ColorPageIndicator colorPageIndicator = ColorPageIndicator.this;
                colorPageIndicator.mTraceLeft = colorPageIndicator.mTraceRect.left;
                ColorPageIndicator colorPageIndicator2 = ColorPageIndicator.this;
                colorPageIndicator2.mTraceRight = colorPageIndicator2.mTraceRect.right;
            }
        });
        this.mTracePaint = new Paint(1);
        this.mTracePaint.setStyle(Paint.Style.FILL);
        this.mTracePaint.setColor(this.mTraceDotColor);
        this.mDotStepDistance = this.mDotSize + (this.mDotSpacing * 2);
        this.mHandler = new Handler() {
            /* class com.color.widget.indicator.ColorPageIndicator.AnonymousClass3 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what == 17) {
                    ColorPageIndicator.this.startTraceAnimator();
                }
                super.handleMessage(msg);
            }
        };
        this.mIndicatorDotsParent = new LinearLayout(context);
        this.mIndicatorDotsParent.setLayoutParams(new FrameLayout.LayoutParams(-2, -2));
        this.mIndicatorDotsParent.setOrientation(0);
        addView(this.mIndicatorDotsParent);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            /* class com.color.widget.indicator.ColorPageIndicator.AnonymousClass4 */

            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT > 16) {
                    ColorPageIndicator.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                ColorPageIndicator colorPageIndicator = ColorPageIndicator.this;
                colorPageIndicator.snapToPoition(colorPageIndicator.mCurrentPosition);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void snapToPoition(int position) {
        verifyFinalPosition(this.mCurrentPosition);
        RectF rectF = this.mTraceRect;
        rectF.left = this.mFinalLeft;
        rectF.right = this.mFinalRight;
        invalidate();
    }

    public void addDot() {
        this.mDotsCount++;
        verifyLayoutWidth();
        addIndicatorDots(1);
    }

    public void removeDot() {
        this.mDotsCount--;
        verifyLayoutWidth();
        removeIndicatorDots(1);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        RectF rectF = this.mTraceRect;
        int i = this.mDotCornerRadius;
        canvas.drawRoundRect(rectF, (float) i, (float) i, this.mTracePaint);
    }

    private void setupDotView(boolean stroke, View dot, int color) {
        GradientDrawable drawable = (GradientDrawable) dot.getBackground();
        if (stroke) {
            drawable.setStroke(this.mDotStrokeWidth, color);
        } else {
            drawable.setColor(color);
        }
        drawable.setCornerRadius((float) this.mDotCornerRadius);
    }

    @TargetApi(21)
    private View buildDot(boolean stroke, int color) {
        View dot = LayoutInflater.from(getContext()).inflate(201917603, (ViewGroup) this, false);
        View dotView = dot.findViewById(201459030);
        int i = 201852280;
        if (Build.VERSION.SDK_INT > 16) {
            Resources resources = getContext().getResources();
            if (!stroke) {
                i = 201852279;
            }
            dotView.setBackground(resources.getDrawable(i));
        } else {
            Resources resources2 = getContext().getResources();
            if (!stroke) {
                i = 201852279;
            }
            dotView.setBackgroundDrawable(resources2.getDrawable(i));
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) dotView.getLayoutParams();
        int i2 = this.mDotSize;
        params.height = i2;
        params.width = i2;
        dotView.setLayoutParams(params);
        int i3 = this.mDotSpacing;
        params.setMargins(i3, 0, i3, 0);
        setupDotView(stroke, dotView, color);
        return dot;
    }

    private void removeIndicatorDots(int count) {
        for (int i = 0; i < count; i++) {
            LinearLayout linearLayout = this.mIndicatorDotsParent;
            linearLayout.removeViewAt(linearLayout.getChildCount() - 1);
            List<View> list = this.mIndicatorDots;
            list.remove(list.size() - 1);
        }
    }

    private void addIndicatorDots(int count) {
        for (final int i = 0; i < count; i++) {
            View dot = buildDot(this.mIsStrokeStyle, this.mDotColor);
            if (this.mIsClickable) {
                dot.setOnClickListener(new View.OnClickListener() {
                    /* class com.color.widget.indicator.ColorPageIndicator.AnonymousClass5 */

                    @Override // android.view.View.OnClickListener
                    public void onClick(View v) {
                        if (ColorPageIndicator.this.mOnDotClickListener != null) {
                            ColorPageIndicator.this.mNeedSettlePositionTemp = true;
                            ColorPageIndicator.this.mIsAnimated = false;
                            ColorPageIndicator.this.stopTraceAnimator();
                            ColorPageIndicator.this.mOnDotClickListener.onClick(i);
                        }
                    }
                });
            }
            this.mIndicatorDots.add(dot.findViewById(201459030));
            this.mIndicatorDotsParent.addView(dot);
        }
    }

    private void verifyLayoutWidth() {
        int i = this.mDotsCount;
        if (i >= 1) {
            this.mWidth = this.mDotStepDistance * i;
            requestLayout();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startTraceAnimator() {
        if (this.mTraceAnimator != null) {
            stopTraceAnimator();
            this.mTraceAnimator.start();
        }
    }

    public void stopTraceAnimator() {
        if (!this.mIsAnimatorCanceled) {
            this.mIsAnimatorCanceled = true;
        }
        ValueAnimator valueAnimator = this.mTraceAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mTraceAnimator.cancel();
        }
    }

    private void pauseTrace() {
        this.mIsPaused = true;
    }

    private void resumeTrace() {
        this.mIsPaused = false;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(this.mWidth, this.mDotSize);
    }

    public void setDotsCount(int count) {
        setVisibility(count <= 1 ? 8 : 0);
        this.mDotsCount = count;
        verifyLayoutWidth();
        this.mIndicatorDots.clear();
        this.mIndicatorDotsParent.removeAllViews();
        addIndicatorDots(count);
    }

    public void setCurrentPosition(int position) {
        this.mCurrentPosition = position;
        this.mLastPosition = position;
        snapToPoition(position);
    }

    public void setTraceDotColor(int color) {
        this.mTraceDotColor = color;
        this.mTracePaint.setColor(color);
    }

    public void setPageIndicatorDotsColor(int color) {
        this.mDotColor = color;
        List<View> list = this.mIndicatorDots;
        if (!(list == null || list.isEmpty())) {
            for (View dot : this.mIndicatorDots) {
                setupDotView(this.mIsStrokeStyle, dot, color);
            }
        }
    }

    public void setOnDotClickListener(OnIndicatorDotClickListener onDotClickListener) {
        this.mOnDotClickListener = onDotClickListener;
    }

    @Override // com.color.widget.indicator.IPagerIndicator
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        boolean rtl = isLayoutRtl();
        boolean scrollLeft = true;
        int i = this.mCurrentPosition;
        if (!rtl ? i > position : i <= position) {
            scrollLeft = false;
        }
        if (scrollLeft) {
            if (rtl) {
                int i2 = this.mDotSpacing;
                int i3 = this.mDotStepDistance;
                this.mTraceRect.right = ((float) this.mWidth) - (((float) (i2 + (position * i3))) + (((float) i3) * positionOffset));
            } else {
                int i4 = this.mDotSpacing + this.mDotSize;
                int i5 = this.mDotStepDistance;
                this.mTraceRect.right = ((float) (i4 + (position * i5))) + (((float) i5) * positionOffset);
            }
            if (this.mIsPaused) {
                if (!this.mIsAnimating && this.mIsAnimated) {
                    RectF rectF = this.mTraceRect;
                    rectF.left = rectF.right - ((float) this.mDotSize);
                } else if (this.mTraceRect.right - this.mTraceRect.left < ((float) this.mDotSize)) {
                    RectF rectF2 = this.mTraceRect;
                    rectF2.left = rectF2.right - ((float) this.mDotSize);
                }
            } else if (this.mIsAnimated) {
                RectF rectF3 = this.mTraceRect;
                rectF3.left = rectF3.right - ((float) this.mDotSize);
            } else if (this.mTraceRect.right - this.mTraceRect.left < ((float) this.mDotSize)) {
                RectF rectF4 = this.mTraceRect;
                rectF4.left = rectF4.right - ((float) this.mDotSize);
            }
        } else {
            if (rtl) {
                this.mTraceRect.left = ((((float) this.mWidth) - (((float) this.mDotStepDistance) * (((float) position) + positionOffset))) - ((float) this.mDotSpacing)) - ((float) this.mDotSize);
            } else {
                this.mTraceRect.left = ((float) this.mDotSpacing) + (((float) this.mDotStepDistance) * (((float) position) + positionOffset));
            }
            if (this.mIsPaused) {
                if (!this.mIsAnimating && this.mIsAnimated) {
                    RectF rectF5 = this.mTraceRect;
                    rectF5.right = rectF5.left + ((float) this.mDotSize);
                } else if (this.mTraceRect.right - this.mTraceRect.left < ((float) this.mDotSize)) {
                    RectF rectF6 = this.mTraceRect;
                    rectF6.right = rectF6.left + ((float) this.mDotSize);
                }
            } else if (this.mIsAnimated) {
                RectF rectF7 = this.mTraceRect;
                rectF7.right = rectF7.left + ((float) this.mDotSize);
            } else if (this.mTraceRect.right - this.mTraceRect.left < ((float) this.mDotSize)) {
                RectF rectF8 = this.mTraceRect;
                rectF8.right = rectF8.left + ((float) this.mDotSize);
            }
        }
        this.mTraceLeft = this.mTraceRect.left;
        this.mTraceRight = this.mTraceRect.right;
        if (positionOffset == 0.0f) {
            this.mCurrentPosition = position;
        }
        invalidate();
    }

    private void verifyFinalPosition(int position) {
        if (isLayoutRtl()) {
            this.mFinalRight = (float) (this.mWidth - (this.mDotSpacing + (this.mDotStepDistance * position)));
            this.mFinalLeft = this.mFinalRight - ((float) this.mDotSize);
            return;
        }
        int i = this.mDotSpacing;
        int i2 = this.mDotSize;
        this.mFinalRight = (float) (i + i2 + (this.mDotStepDistance * position));
        this.mFinalLeft = this.mFinalRight - ((float) i2);
    }

    @Override // com.color.widget.indicator.IPagerIndicator
    public void onPageSelected(int position) {
        boolean z = false;
        if (this.mLastPosition != position && this.mIsAnimated) {
            this.mIsAnimated = false;
        }
        if (!isLayoutRtl() ? this.mLastPosition > position : this.mLastPosition <= position) {
            z = true;
        }
        this.mTranceCutTailRight = z;
        verifyFinalPosition(position);
        if (this.mLastPosition != position) {
            if (this.mHandler.hasMessages(17)) {
                this.mHandler.removeMessages(17);
            }
            stopTraceAnimator();
            this.mHandler.sendEmptyMessageDelayed(17, 100);
        } else if (this.mHandler.hasMessages(17)) {
            this.mHandler.removeMessages(17);
        }
        this.mLastPosition = position;
    }

    @Override // com.color.widget.indicator.IPagerIndicator
    public void onPageScrollStateChanged(int state) {
        if (state == 1) {
            pauseTrace();
            if (this.mIsAnimated) {
                this.mIsAnimated = false;
            }
        } else if (state == 2) {
            resumeTrace();
        }
    }

    @Override // android.view.View
    public boolean isLayoutRtl() {
        if (Build.VERSION.SDK_INT <= 16 || getLayoutDirection() != 1) {
            return false;
        }
        return true;
    }

    private int dpToPx(int dp) {
        return (int) (getContext().getResources().getDisplayMetrics().density * ((float) dp));
    }
}
