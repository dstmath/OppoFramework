package com.color.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.SweepGradient;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OppoBezierInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import com.android.internal.widget.ColorViewExplorerByTouchHelper;
import com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction;
import com.color.util.ColorAccessibilityUtil;
import com.color.util.ColorChangeTextUtil;
import com.color.util.ColorContextUtil;
import oppo.R;

public class ColorGlobalActionView extends View {
    private static final Interpolator AUTODOWNINTER = new PathInterpolator(0.507f, 0.04f, 0.889f, 0.78f);
    private static final int AUTODOWNREBOUNCE = 150;
    private static final Interpolator AUTODOWNREBOUNCEINTER = new OppoBezierInterpolator(-0.10000000149011612d, 1.475000023841858d, 0.6740000247955322d, 0.8700000047683716d, false);
    private static final int AUTODOWNTIME = 475;
    private static final int AUTODOWN_FLAG = 1;
    private static final int AUTODOWN_REBOUNCE_FLAG = 2;
    private static final float BG_ALPHA = 0.9f;
    private static final int CANCELNODEID = 1;
    private static final int CANCEL_EXIT_FLAG = 3;
    private static final float CRITICAL_NUMBER = 0.8f;
    private static final float DEGREE_360 = 360.0f;
    private static final float DEGREE_90 = 90.0f;
    private static final int EMERGENODEID = 2;
    private static final Interpolator EXITAINM = new LinearInterpolator();
    private static final int EXITTIME = 250;
    private static final int LARGEARGANGLE = 13;
    private static final float LARGESTARTANGLE = 193.0f;
    private static final float LARGESWEEPANGLE = 154.0f;
    private static final int OPAQUE = 255;
    private static final Interpolator RESTOREPOSITION = new PathInterpolator(0.121f, 0.82f, 0.71f, 0.944f);
    private static final Interpolator RESTOREREBOUNCE = new DecelerateInterpolator();
    private static final int RESTORE_FIRST_REBOUCE = 5;
    private static final int RESTORE_INIT_FLAG = 4;
    private static final int RESTORE_SECOND_REBOUCE = 6;
    private static final int SHUDNODEID = 0;
    private static final Interpolator SHUTDOWNINTER = new PathInterpolator(0.121f, 0.82f, 0.71f, 0.944f);
    private static final int SHUTDOWNY = 200;
    private static final int SHUT_DOWN_UP_FLAG = 7;
    private static final int SMALLARGANGLE = 32;
    private static final int STATICARCTIME = 500;
    private static final int STATICBGTIME = 100;
    private static final int STATICLINETIME = 300;
    private static final int STATICSMALLARC = 350;
    private static final int STATIC_ARC_FLAG = 10;
    private static final int STATIC_BG_ALPHA = 8;
    private static final int STATIC_LINE_FLAG = 9;
    private static final String TAG = "ColorGlobalActionView";
    private static final int TEXTAPHA = 255;
    private String mAccessContent;
    private int mAutoDownReUp;
    private int mAutoRectHight;
    private int mAutonDownReDown;
    private float mBgAlpha;
    private ColorDrawable mBgColor;
    private float mCancelAutoEnd;
    private Drawable mCancelBg;
    private int mCancelBgAlpha;
    private int mCancelBgBottom;
    private float mCancelBgDimen;
    private int mCancelBgEnd;
    private int mCancelBgHeight;
    private int mCancelBgLeft;
    private int mCancelBgRight;
    private int mCancelBgStart;
    private int mCancelBgTop;
    private Drawable mCancelNormalBg;
    private TextPaint mCancelPaint;
    private Drawable mCancelPressBg;
    private String mCancelText;
    private int mCancelTextAlpha;
    private int mCancelTextColor;
    private float mCancelTextDimen;
    private int mCancelTextEnd;
    private int mCancelTextHeight;
    private int mCancelTextSize;
    private int mCancelTextStart;
    private int mCancelTextY;
    private float mCancleBgColorAlpha;
    private int mCircleX;
    private int mCircleY;
    private ColorViewTalkBalkInteraction mColorViewTalkBalkInteraction;
    private int mContentHeight;
    private int mContentWidth;
    private Drawable mDynamicBg;
    private Drawable mEmergencyBg;
    private int mEmergencyBgBottom;
    private int mEmergencyBgHeight;
    private int mEmergencyBgLeft;
    private int mEmergencyBgRight;
    private int mEmergencyBgTop;
    private Drawable mEmergencyNormalBg;
    private Drawable mEmergencyPressBg;
    private String mEmergencyText;
    private int mEmergencyTextY;
    private boolean mFirstBgAlpha;
    private boolean mFirstRebounceAnim;
    private ValueAnimator mFirstRebound;
    private boolean mIsClickCancelBg;
    private boolean mIsClickEmergencyBg;
    private boolean mIsExitAnim;
    private boolean mIsOrientationPortrait;
    private boolean mIsShutDown;
    private boolean mIsTouchShutBg;
    private int mLargeArcEndColor;
    private Paint mLargeArcPaint;
    private int mLargeArcRadius;
    private RectF mLargeArcRect;
    private int mLargeArcStartColor;
    private int mLineEndColor;
    private Paint mLinePaint;
    private final Path mLinePath;
    private int mLineStartColor;
    private OnCancelListener mOnCancelListener;
    private OnEmergencyListener mOnEmergencyListener;
    private int mPaintWidth;
    private int mRectBgEndColor;
    private int mRectBgStartColor;
    private ValueAnimator mRestoreYAnim;
    private ValueAnimator mSecondRebound;
    private float mShutDownBgAutoEnd;
    private float mShutDownBgDimen;
    private int mShutDownBgEnd;
    private int mShutDownBgStart;
    private int mShutDownHeight;
    private OnShutDownListener mShutDownLister;
    private TextPaint mShutDownPaint;
    private String mShutDownText;
    private int mShutDownTextColor;
    private float mShutDownTextDimen;
    private int mShutDownTextEnd;
    private int mShutDownTextHeight;
    private int mShutDownTextSize;
    private int mShutDownTextStart;
    private int mShutTextGap;
    private int mShutTextPadding;
    private float mSmallArcAngle;
    private int mSmallArcEndColor;
    private Paint mSmallArcPaint;
    private int mSmallArcRadius;
    private RectF mSmallArcRect;
    private int mSmallArcStartColor;
    private float mSmallArcSweepAngle;
    private int mStartDrawableAlpha;
    private boolean mStartStaticAlpha;
    private int mStartTextAlpha;
    private boolean mStateRestore;
    private float mStaticArcAlpha;
    private Drawable mStaticBg;
    private float mStaticBgAlpha;
    private int mStaticBgBottom;
    private int mStaticBgLeft;
    private int mStaticBgRight;
    private int mStaticBgTop;
    private float mStaticLineAlpha;
    private float mStaticLineStartY;
    private float mStaticMoveY;
    private float mTouchDownY;
    private ColorViewExplorerByTouchHelper mTouchHelper;
    private float mTouchMoveY;
    private float mTouchUpY;
    private String mUpLine;
    private Paint mViewRectBgPaint;

    private class AnimUpdateListener implements AnimatorUpdateListener {
        private int mFlag = 0;

        public AnimUpdateListener(int flag) {
            this.mFlag = flag;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            float value = ((Float) animation.getAnimatedValue()).floatValue();
            if (this.mFlag == 1) {
                ColorGlobalActionView.this.mShutDownTextDimen = ((float) ColorGlobalActionView.this.mShutDownTextStart) + (((float) (ColorGlobalActionView.this.mShutDownTextEnd - ColorGlobalActionView.this.mShutDownTextStart)) * value);
                ColorGlobalActionView.this.mShutDownBgDimen = ((float) ColorGlobalActionView.this.mShutDownBgStart) + (((float) (ColorGlobalActionView.this.mShutDownBgEnd - ColorGlobalActionView.this.mShutDownBgStart)) * value);
                ColorGlobalActionView.this.mCancelBgDimen = ((1.0f - value) * ((float) (ColorGlobalActionView.this.mCancelBgHeight + ColorGlobalActionView.this.mCancelBgStart))) + (((float) ((ColorGlobalActionView.this.mCancelBgEnd + ColorGlobalActionView.this.mCancelTextHeight) + ColorGlobalActionView.this.mCancelBgHeight)) * value);
                ColorGlobalActionView.this.mCancelTextDimen = ((1.0f - value) * ((float) ColorGlobalActionView.this.mCancelTextStart)) + (((float) (ColorGlobalActionView.this.mCancelTextEnd + ColorGlobalActionView.this.mCancelTextHeight)) * value);
                ColorGlobalActionView.this.mStartDrawableAlpha = (int) (((1.0f - value) * 0.0f) + (value * 255.0f));
                ColorGlobalActionView.this.mStartTextAlpha = (int) (((1.0f - value) * 0.0f) + (value * 255.0f));
            }
            if (this.mFlag == 2) {
                ColorGlobalActionView.this.mShutDownBgDimen = ((1.0f - value) * (ColorGlobalActionView.this.mShutDownBgAutoEnd - ((float) ColorGlobalActionView.this.mAutoDownReUp))) + ((ColorGlobalActionView.this.mShutDownBgAutoEnd - ((float) ColorGlobalActionView.this.mAutonDownReDown)) * value);
                ColorGlobalActionView.this.mCancelBgDimen = ((1.0f - value) * (ColorGlobalActionView.this.mCancelAutoEnd - ((float) ColorGlobalActionView.this.mAutoDownReUp))) + ((ColorGlobalActionView.this.mCancelAutoEnd - ((float) ColorGlobalActionView.this.mAutonDownReDown)) * value);
            }
            if (this.mFlag == 3) {
                ColorGlobalActionView.this.mCancelBgAlpha = (int) (((1.0f - value) * 255.0f) + (0.0f * value));
                ColorGlobalActionView.this.mCancelTextAlpha = (int) (((1.0f - value) * 255.0f) + (0.0f * value));
                ColorGlobalActionView.this.mCancleBgColorAlpha = (float) ((int) ((((1.0f - value) * 255.0f) * ColorGlobalActionView.BG_ALPHA) + (0.0f * value)));
            }
            if (this.mFlag == 4) {
                ColorGlobalActionView.this.mTouchMoveY = ((1.0f - value) * ColorGlobalActionView.this.mTouchUpY) + (ColorGlobalActionView.this.mTouchDownY * value);
                ColorGlobalActionView.this.mStaticMoveY = ((1.0f - value) * ColorGlobalActionView.this.mTouchUpY) + ((ColorGlobalActionView.this.mTouchDownY - ((float) ColorGlobalActionView.this.mAutonDownReDown)) * value);
            }
            if (this.mFlag == 5) {
                ColorGlobalActionView.this.mStaticMoveY = ((1.0f - value) * (ColorGlobalActionView.this.mTouchDownY - ((float) ColorGlobalActionView.this.mAutonDownReDown))) + (((ColorGlobalActionView.this.mTouchDownY - ((float) ColorGlobalActionView.this.mAutonDownReDown)) + ((float) ColorGlobalActionView.this.mAutoDownReUp)) * value);
            }
            if (this.mFlag == 6) {
                ColorGlobalActionView.this.mStaticMoveY = ((1.0f - value) * ((ColorGlobalActionView.this.mTouchDownY - ((float) ColorGlobalActionView.this.mAutonDownReDown)) + ((float) ColorGlobalActionView.this.mAutoDownReUp))) + (ColorGlobalActionView.this.mTouchDownY * value);
            }
            if (this.mFlag == 7) {
                ColorGlobalActionView.this.mStaticMoveY = ((1.0f - value) * ColorGlobalActionView.this.mTouchUpY) + (((((float) ColorGlobalActionView.this.mShutDownHeight) * ColorGlobalActionView.CRITICAL_NUMBER) + ColorGlobalActionView.this.mTouchDownY) * value);
            }
            if (this.mFlag == 8) {
                ColorGlobalActionView.this.mStaticBgAlpha = (float) ((int) (((1.0f - value) * 255.0f) + (0.0f * value)));
            }
            if (this.mFlag == 9) {
                ColorGlobalActionView.this.mStaticLineAlpha = (float) ((int) (((1.0f - value) * 255.0f) + (0.0f * value)));
                ColorGlobalActionView.this.mStaticLineStartY = (float) ((int) (((1.0f - value) * ((float) ColorGlobalActionView.this.mSmallArcRadius)) + (0.0f * value)));
            }
            if (this.mFlag == 10) {
                ColorGlobalActionView.this.mStaticArcAlpha = (float) ((int) (((1.0f - value) * 255.0f) + (0.0f * value)));
                ColorGlobalActionView.this.mSmallArcAngle = (ColorGlobalActionView.DEGREE_360 * value) + ((1.0f - value) * 0.0f);
                ColorGlobalActionView.this.mSmallArcSweepAngle = (296.0f * value) + ((1.0f - value) * 0.0f);
            }
            ColorGlobalActionView.this.invalidate();
        }
    }

    public interface OnCancelListener {
        void onCancel();
    }

    public interface OnEmergencyListener {
        void onEmergency();
    }

    public interface OnShutDownListener {
        void onShutDown();
    }

    public ColorGlobalActionView(Context context) {
        this(context, null);
    }

    public ColorGlobalActionView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393291);
    }

    public ColorGlobalActionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mLinePath = new Path();
        this.mStaticBg = null;
        this.mCancelBg = null;
        this.mCancelNormalBg = null;
        this.mCancelPressBg = null;
        this.mDynamicBg = null;
        this.mEmergencyTextY = 0;
        this.mIsClickEmergencyBg = false;
        this.mEmergencyBgHeight = 0;
        this.mEmergencyBgLeft = 0;
        this.mEmergencyBgRight = 0;
        this.mEmergencyBgTop = 0;
        this.mEmergencyBgBottom = 0;
        this.mShutDownTextColor = 0;
        this.mCancelTextColor = 0;
        this.mBgColor = null;
        this.mShutDownTextSize = 0;
        this.mCancelTextSize = 0;
        this.mShutDownText = null;
        this.mCancelText = null;
        this.mContentWidth = 0;
        this.mContentHeight = 0;
        this.mShutDownHeight = 0;
        this.mShutDownPaint = null;
        this.mCancelPaint = null;
        this.mSmallArcPaint = null;
        this.mViewRectBgPaint = null;
        this.mLargeArcPaint = null;
        this.mSmallArcStartColor = 0;
        this.mSmallArcEndColor = 0;
        this.mLargeArcStartColor = 0;
        this.mLargeArcEndColor = 0;
        this.mLinePaint = null;
        this.mLineStartColor = 0;
        this.mLineEndColor = 0;
        this.mPaintWidth = 0;
        this.mCircleY = 0;
        this.mCancelTextHeight = 0;
        this.mSmallArcRadius = 0;
        this.mShutDownTextStart = 0;
        this.mShutDownTextEnd = 0;
        this.mShutDownTextDimen = 0.0f;
        this.mShutDownBgStart = 0;
        this.mShutDownBgEnd = 0;
        this.mShutDownBgDimen = 0.0f;
        this.mCancelTextStart = 0;
        this.mCancelTextEnd = 0;
        this.mCancelTextDimen = 0.0f;
        this.mCancelBgStart = 0;
        this.mCancelBgEnd = 0;
        this.mCancelBgDimen = 0.0f;
        this.mCancelTextY = 0;
        this.mAutoRectHight = 0;
        this.mCancelBgLeft = 0;
        this.mCancelBgRight = 0;
        this.mAutoDownReUp = 0;
        this.mAutonDownReDown = 0;
        this.mCancelBgTop = 0;
        this.mCancelBgBottom = 0;
        this.mSmallArcRect = new RectF();
        this.mLargeArcRect = new RectF();
        this.mCancelBgHeight = 0;
        this.mFirstRebounceAnim = false;
        this.mIsClickCancelBg = false;
        this.mIsTouchShutBg = false;
        this.mIsExitAnim = false;
        this.mShutDownBgAutoEnd = 0.0f;
        this.mCancelAutoEnd = 0.0f;
        this.mCancelTextAlpha = 0;
        this.mCancelBgAlpha = 0;
        this.mStaticBgLeft = 0;
        this.mStaticBgRight = 0;
        this.mStaticBgBottom = 0;
        this.mStaticBgTop = 0;
        this.mShutDownTextHeight = 0;
        this.mLargeArcRadius = 0;
        this.mTouchDownY = 0.0f;
        this.mTouchMoveY = 0.0f;
        this.mStaticMoveY = 0.0f;
        this.mTouchUpY = 0.0f;
        this.mStateRestore = false;
        this.mFirstBgAlpha = true;
        this.mIsShutDown = false;
        this.mStartStaticAlpha = false;
        this.mStaticBgAlpha = 0.0f;
        this.mStaticLineAlpha = 0.0f;
        this.mStaticArcAlpha = 0.0f;
        this.mBgAlpha = 0.0f;
        this.mStaticLineStartY = 0.0f;
        this.mSmallArcAngle = 0.0f;
        this.mSmallArcSweepAngle = 0.0f;
        this.mRectBgStartColor = 0;
        this.mRectBgEndColor = 0;
        this.mStartDrawableAlpha = 0;
        this.mStartTextAlpha = 0;
        this.mIsOrientationPortrait = true;
        this.mCircleX = 0;
        this.mSecondRebound = null;
        this.mFirstRebound = null;
        this.mRestoreYAnim = null;
        this.mCancleBgColorAlpha = 229.5f;
        this.mOnCancelListener = null;
        this.mShutDownLister = null;
        this.mOnEmergencyListener = null;
        this.mAccessContent = null;
        this.mShutTextPadding = 0;
        this.mShutTextGap = 0;
        this.mUpLine = null;
        this.mColorViewTalkBalkInteraction = new ColorViewTalkBalkInteraction() {
            public void getItemBounds(int position, Rect rect) {
                if (position == 0) {
                    rect.set(ColorGlobalActionView.this.mStaticBgLeft, ColorGlobalActionView.this.mStaticBgTop, ColorGlobalActionView.this.mStaticBgRight, ColorGlobalActionView.this.mStaticBgBottom);
                } else if (position == 1) {
                    rect.set(ColorGlobalActionView.this.mCancelBgLeft, ColorGlobalActionView.this.mCancelBgTop, ColorGlobalActionView.this.mCancelBgRight, ColorGlobalActionView.this.mCancelBgBottom);
                } else if (position == 2) {
                    rect.set(ColorGlobalActionView.this.mEmergencyBgLeft, ColorGlobalActionView.this.mEmergencyBgTop, ColorGlobalActionView.this.mEmergencyBgRight, ColorGlobalActionView.this.mEmergencyBgBottom);
                }
            }

            public void performAction(int virtualViewId, int actiontype, boolean resolvePara) {
                if (ColorGlobalActionView.isIndiaRegion()) {
                    if (virtualViewId == 0) {
                        if (ColorGlobalActionView.this.mShutDownLister != null) {
                            ColorGlobalActionView.this.mShutDownLister.onShutDown();
                        }
                    } else if (virtualViewId == 1) {
                        ColorGlobalActionView.this.setQuitView();
                    } else if (virtualViewId == 2) {
                        if (ColorGlobalActionView.this.mOnEmergencyListener != null) {
                            ColorGlobalActionView.this.mOnEmergencyListener.onEmergency();
                        }
                        ColorGlobalActionView.this.mTouchHelper.sendEventForVirtualView(2, 1);
                    } else if (ColorGlobalActionView.this.mShutDownLister != null) {
                        ColorGlobalActionView.this.mShutDownLister.onShutDown();
                    }
                } else if (virtualViewId == 0) {
                    if (ColorGlobalActionView.this.mShutDownLister != null) {
                        ColorGlobalActionView.this.mShutDownLister.onShutDown();
                    }
                } else if (virtualViewId == 1) {
                    ColorGlobalActionView.this.setQuitView();
                } else if (ColorGlobalActionView.this.mShutDownLister != null) {
                    ColorGlobalActionView.this.mShutDownLister.onShutDown();
                }
            }

            public int getCurrentPosition() {
                return -2;
            }

            public int getItemCounts() {
                if (ColorGlobalActionView.isIndiaRegion()) {
                    return 3;
                }
                return 2;
            }

            public int getVirtualViewAt(float x, float y) {
                if (ColorGlobalActionView.isIndiaRegion()) {
                    if (x >= 0.0f && x <= ((float) ColorGlobalActionView.this.mContentWidth) && y >= 0.0f && y <= ((float) (ColorGlobalActionView.this.mStaticBgBottom * 2)) && ColorGlobalActionView.this.mFirstRebounceAnim) {
                        return 0;
                    }
                    if (x >= ((float) ColorGlobalActionView.this.mCancelBgLeft) && x <= ((float) ColorGlobalActionView.this.mCancelBgRight) && y >= ((float) ColorGlobalActionView.this.mCancelBgTop) && y <= ((float) ColorGlobalActionView.this.mCancelBgBottom) && ColorGlobalActionView.this.mFirstRebounceAnim) {
                        return 1;
                    }
                    if (x >= ((float) ColorGlobalActionView.this.mEmergencyBgLeft) && x <= ((float) ColorGlobalActionView.this.mEmergencyBgRight) && y >= ((float) ColorGlobalActionView.this.mEmergencyBgTop) && y <= ((float) ColorGlobalActionView.this.mEmergencyBgBottom) && ColorGlobalActionView.this.mFirstRebounceAnim) {
                        return 2;
                    }
                } else if (x >= 0.0f && x <= ((float) ColorGlobalActionView.this.mContentWidth) && y >= 0.0f && y <= ((float) (ColorGlobalActionView.this.mStaticBgBottom * 2)) && ColorGlobalActionView.this.mFirstRebounceAnim) {
                    return 0;
                } else {
                    if (x >= ((float) ColorGlobalActionView.this.mCancelBgLeft) && x <= ((float) ColorGlobalActionView.this.mCancelBgRight) && y >= ((float) ColorGlobalActionView.this.mCancelBgTop) && y <= ((float) ColorGlobalActionView.this.mCancelBgBottom) && ColorGlobalActionView.this.mFirstRebounceAnim) {
                        return 1;
                    }
                }
                return -1;
            }

            public CharSequence getItemDescription(int virtualViewId) {
                if (virtualViewId == 0) {
                    return ColorGlobalActionView.this.mAccessContent;
                }
                if (virtualViewId == 1) {
                    return ColorGlobalActionView.this.mCancelText;
                }
                if (virtualViewId == 2) {
                    return ColorGlobalActionView.this.mEmergencyText;
                }
                return getClass().getSimpleName();
            }

            public CharSequence getClassName() {
                return Button.class.getName();
            }

            public int getDisablePosition() {
                return -2;
            }
        };
        this.mIsOrientationPortrait = isOrientationPortrait(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorGlobalActionView, defStyle, 0);
        this.mStaticBg = a.getDrawable(0);
        this.mCancelNormalBg = a.getDrawable(1);
        this.mCancelPressBg = a.getDrawable(5);
        this.mCancelTextSize = a.getDimensionPixelSize(2, 0);
        this.mShutDownText = a.getString(4);
        this.mCancelText = a.getString(3);
        this.mEmergencyNormalBg = a.getDrawable(6);
        this.mEmergencyPressBg = a.getDrawable(7);
        this.mEmergencyText = a.getString(8);
        a.recycle();
        if (this.mIsOrientationPortrait) {
            this.mDynamicBg = context.getDrawable(201852178);
        } else {
            this.mDynamicBg = context.getDrawable(201852186);
        }
        this.mShutDownTextColor = context.getColor(201720899);
        this.mCancelTextColor = context.getColor(201720900);
        this.mBgColor = new ColorDrawable(ColorContextUtil.getAttrColor(context, 201392717));
        this.mRectBgStartColor = ColorContextUtil.getAttrColor(context, 201392717);
        this.mRectBgEndColor = context.getColor(201720890);
        this.mShutDownTextSize = getResources().getDimensionPixelSize(201655524);
        float fontScale = getResources().getConfiguration().fontScale;
        this.mShutDownTextSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) this.mShutDownTextSize, fontScale, 2);
        this.mCancelTextSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) this.mCancelTextSize, fontScale, 2);
        this.mSmallArcStartColor = context.getColor(201720891);
        this.mSmallArcEndColor = context.getColor(201720892);
        this.mLineStartColor = context.getColor(201720893);
        this.mLineEndColor = context.getColor(201720894);
        this.mLargeArcStartColor = context.getColor(201720895);
        this.mLargeArcEndColor = context.getColor(201720896);
        if (this.mCancelNormalBg != null) {
            this.mCancelBgHeight = this.mCancelNormalBg.getIntrinsicHeight();
            this.mCancelBg = this.mCancelNormalBg;
            this.mCancelBg.setAlpha(255);
        }
        if (this.mEmergencyNormalBg != null) {
            this.mEmergencyBgHeight = this.mEmergencyNormalBg.getIntrinsicHeight();
            this.mEmergencyBg = this.mEmergencyNormalBg;
            this.mEmergencyBg.setAlpha(255);
        }
        animationData();
        initPaint();
        this.mTouchHelper = new ColorViewExplorerByTouchHelper(this);
        this.mTouchHelper.setColorViewTalkBalkInteraction(this.mColorViewTalkBalkInteraction);
        setAccessibilityDelegate(this.mTouchHelper);
        setImportantForAccessibility(1);
        this.mAccessContent = context.getString(201590175);
        this.mShutTextPadding = getResources().getDimensionPixelSize(201655576);
        this.mShutTextGap = getResources().getDimensionPixelSize(201655577);
        initString();
    }

    private boolean isOrientationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == 1;
    }

    private void initPaint() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        this.mContentWidth = displayMetrics.widthPixels;
        this.mContentHeight = displayMetrics.heightPixels;
        this.mShutDownPaint = new TextPaint(1);
        this.mShutDownPaint.setAntiAlias(true);
        this.mShutDownPaint.setTextSize((float) this.mShutDownTextSize);
        this.mShutDownPaint.setColor(this.mShutDownTextColor);
        this.mCancelPaint = new TextPaint(1);
        this.mCancelPaint.setAntiAlias(true);
        this.mCancelPaint.setTextSize((float) this.mCancelTextSize);
        this.mCancelPaint.setColor(this.mCancelTextColor);
        this.mSmallArcPaint = new Paint();
        this.mSmallArcPaint.setStyle(Style.STROKE);
        this.mSmallArcPaint.setStrokeWidth((float) this.mPaintWidth);
        this.mSmallArcPaint.setAntiAlias(true);
        this.mSmallArcPaint.setDither(true);
        this.mSmallArcPaint.setStrokeJoin(Join.ROUND);
        this.mSmallArcPaint.setStrokeCap(Cap.ROUND);
        this.mLargeArcPaint = new Paint();
        this.mLargeArcPaint.setStyle(Style.STROKE);
        this.mLargeArcPaint.setStrokeWidth((float) this.mPaintWidth);
        this.mLargeArcPaint.setAntiAlias(true);
        this.mLargeArcPaint.setDither(true);
        this.mLargeArcPaint.setStrokeJoin(Join.ROUND);
        this.mLargeArcPaint.setStrokeCap(Cap.ROUND);
        this.mLinePaint = new Paint();
        this.mLinePaint.setStyle(Style.STROKE);
        this.mLinePaint.setStrokeWidth((float) this.mPaintWidth);
        this.mLinePaint.setAntiAlias(true);
        this.mLinePaint.setDither(true);
        this.mLinePaint.setStrokeJoin(Join.ROUND);
        this.mLinePaint.setStrokeCap(Cap.ROUND);
        this.mLinePaint.setColor(this.mLineStartColor);
        this.mStaticBg.setAlpha(255);
    }

    private void animationData() {
        this.mShutDownTextStart = getResources().getDimensionPixelSize(201655531);
        this.mShutDownTextEnd = getResources().getDimensionPixelSize(201655532);
        this.mShutDownBgStart = getResources().getDimensionPixelSize(201655533);
        this.mShutDownBgEnd = getResources().getDimensionPixelSize(201655534);
        this.mCancelTextStart = getResources().getDimensionPixelSize(201655562);
        this.mCancelTextEnd = getResources().getDimensionPixelSize(201655535);
        this.mCancelBgStart = getResources().getDimensionPixelSize(201655536);
        this.mCancelBgEnd = getResources().getDimensionPixelSize(201655537);
        this.mPaintWidth = getResources().getDimensionPixelSize(201655525);
        this.mSmallArcRadius = getResources().getDimensionPixelSize(201655526);
        this.mAutoDownReUp = getResources().getDimensionPixelSize(201655527);
        this.mAutonDownReDown = getResources().getDimensionPixelSize(201655528);
        this.mAutoRectHight = getResources().getDimensionPixelSize(201655529);
        this.mLargeArcRadius = getResources().getDimensionPixelSize(201655530);
    }

    private void initString() {
        int textCanvasWidth;
        if (this.mIsOrientationPortrait) {
            textCanvasWidth = this.mContentWidth - (this.mShutTextPadding * 2);
        } else {
            textCanvasWidth = this.mContentHeight - (this.mShutTextPadding * 2);
        }
        int breakIndex = this.mShutDownPaint.breakText(this.mShutDownText, true, (float) textCanvasWidth, null);
        if (breakIndex < this.mShutDownText.length()) {
            this.mUpLine = this.mShutDownText.substring(0, breakIndex);
        }
        if (!TextUtils.isEmpty(this.mUpLine)) {
            int index = this.mUpLine.lastIndexOf(32);
            if (index > 0) {
                this.mUpLine = this.mShutDownText.substring(0, index);
                this.mShutDownText = this.mShutDownText.substring(index);
                return;
            }
            this.mShutDownText = this.mShutDownText.substring(breakIndex);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this.mContentWidth, this.mContentHeight);
    }

    /* JADX WARNING: Removed duplicated region for block: B:160:0x077b  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x031f  */
    /* JADX WARNING: Removed duplicated region for block: B:162:0x07a8  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x04aa  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x04cc  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x04dd  */
    /* JADX WARNING: Removed duplicated region for block: B:163:0x07ae  */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x04f7  */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x07d8  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x050e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onDraw(Canvas canvas) {
        FontMetricsInt fmi;
        float textX;
        float f;
        int smallRectTop;
        Shader sweepGradient;
        Shader lineShader;
        float secondLineX = 0.0f;
        int staticBgHeight = 0;
        float secondTextY = 0.0f;
        Drawable circleBg = this.mStaticBg;
        if (this.mTouchMoveY <= this.mTouchDownY) {
            this.mTouchMoveY = this.mTouchDownY;
        }
        if (this.mShutDownHeight == 0) {
            this.mBgAlpha = 229.5f;
        } else {
            float value = (this.mTouchMoveY - this.mTouchDownY) / (((float) this.mShutDownHeight) * CRITICAL_NUMBER);
            this.mBgAlpha = (((1.0f - value) * 255.0f) * BG_ALPHA) + (255.0f * value);
            if (this.mBgAlpha >= 255.0f) {
                this.mBgAlpha = 255.0f;
            }
        }
        if (this.mIsExitAnim) {
            this.mBgColor.setAlpha((int) this.mCancleBgColorAlpha);
        } else {
            this.mBgColor.setAlpha((int) this.mBgAlpha);
        }
        setBackgroundDrawable(this.mBgColor);
        if (!(this.mShutDownText == null || (this.mIsTouchShutBg ^ 1) == 0)) {
            float shutDownTextY;
            fmi = this.mShutDownPaint.getFontMetricsInt();
            this.mShutDownTextHeight = fmi.bottom - fmi.top;
            if (this.mIsOrientationPortrait) {
                if (TextUtils.isEmpty(this.mUpLine)) {
                    textX = (float) ((this.mContentWidth - ((int) this.mShutDownPaint.measureText(this.mShutDownText))) / 2);
                    shutDownTextY = this.mShutDownTextDimen - ((float) fmi.top);
                } else {
                    textX = (float) ((((this.mContentWidth - (this.mShutTextPadding * 2)) - ((int) this.mShutDownPaint.measureText(this.mUpLine))) / 2) + this.mShutTextPadding);
                    shutDownTextY = ((this.mShutDownTextDimen - ((float) this.mShutDownTextHeight)) - ((float) this.mShutTextGap)) - ((float) fmi.top);
                    secondLineX = (float) ((((this.mContentWidth - (this.mShutTextPadding * 2)) - ((int) this.mShutDownPaint.measureText(this.mShutDownText))) / 2) + this.mShutTextPadding);
                    secondTextY = this.mShutDownTextDimen - ((float) fmi.top);
                }
            } else if (TextUtils.isEmpty(this.mUpLine)) {
                textX = this.mShutDownTextDimen - ((float) fmi.top);
                shutDownTextY = (float) ((this.mContentHeight / 2) + (((int) this.mShutDownPaint.measureText(this.mShutDownText)) / 2));
            } else {
                textX = ((this.mShutDownTextDimen - ((float) this.mShutDownTextHeight)) - ((float) this.mShutTextGap)) - ((float) fmi.top);
                shutDownTextY = (float) ((((this.mContentHeight - (this.mShutTextPadding * 2)) / 2) + (((int) this.mShutDownPaint.measureText(this.mUpLine)) / 2)) + this.mShutTextPadding);
                secondLineX = this.mShutDownTextDimen - ((float) fmi.top);
                secondTextY = (float) ((((this.mContentHeight - (this.mShutTextPadding * 2)) / 2) + (((int) this.mShutDownPaint.measureText(this.mShutDownText)) / 2)) + this.mShutTextPadding);
            }
            if (this.mIsExitAnim) {
                this.mShutDownPaint.setAlpha(this.mCancelTextAlpha);
            }
            if (this.mFirstBgAlpha) {
                this.mShutDownPaint.setAlpha(this.mStartTextAlpha);
            }
            if (this.mIsOrientationPortrait) {
                if (TextUtils.isEmpty(this.mUpLine)) {
                    canvas.drawText(this.mShutDownText, textX, shutDownTextY, this.mShutDownPaint);
                } else {
                    canvas.drawText(this.mUpLine, textX, shutDownTextY, this.mShutDownPaint);
                    canvas.drawText(this.mShutDownText, secondLineX, secondTextY, this.mShutDownPaint);
                }
            } else if (TextUtils.isEmpty(this.mUpLine)) {
                canvas.save();
                canvas.rotate(-90.0f, textX, shutDownTextY);
                canvas.drawText(this.mShutDownText, textX, shutDownTextY, this.mShutDownPaint);
                canvas.restore();
            } else {
                canvas.save();
                canvas.rotate(-90.0f, textX, shutDownTextY);
                canvas.drawText(this.mUpLine, textX, shutDownTextY, this.mShutDownPaint);
                canvas.restore();
                canvas.save();
                canvas.rotate(-90.0f, secondLineX, secondTextY);
                canvas.drawText(this.mShutDownText, secondLineX, secondTextY, this.mShutDownPaint);
                canvas.restore();
            }
        }
        if (this.mIsTouchShutBg && (this.mStateRestore ^ 1) != 0 && (this.mIsShutDown ^ 1) != 0) {
            circleBg = this.mDynamicBg;
        } else if ((this.mIsTouchShutBg && this.mStateRestore) || (this.mIsTouchShutBg && this.mIsShutDown)) {
            circleBg = this.mStaticBg;
        }
        if (circleBg != null) {
            int staticBgWidth = this.mStaticBg.getIntrinsicWidth();
            staticBgHeight = this.mStaticBg.getIntrinsicHeight();
            if (!this.mStateRestore && this.mStaticMoveY <= this.mTouchDownY) {
                this.mStaticMoveY = this.mTouchDownY;
            }
            if (this.mIsOrientationPortrait) {
                this.mStaticBgLeft = (this.mContentWidth - staticBgWidth) / 2;
                this.mStaticBgTop = (int) ((this.mShutDownBgDimen + (this.mStaticMoveY - this.mTouchDownY)) + ((float) this.mShutDownTextHeight));
            } else {
                this.mStaticBgLeft = (int) ((this.mShutDownBgDimen + (this.mStaticMoveY - this.mTouchDownY)) + ((float) this.mShutDownTextHeight));
                this.mStaticBgTop = (this.mContentHeight - staticBgWidth) / 2;
            }
            this.mStaticBgRight = this.mStaticBgLeft + staticBgWidth;
            this.mStaticBgBottom = this.mStaticBgTop + staticBgHeight;
            circleBg.setBounds(this.mStaticBgLeft, this.mStaticBgTop, this.mStaticBgRight, this.mStaticBgBottom);
            if (this.mFirstBgAlpha) {
                circleBg.setAlpha(this.mStartDrawableAlpha);
            }
            if (this.mStartStaticAlpha) {
                circleBg.setAlpha((int) this.mStaticBgAlpha);
            }
            if (this.mIsExitAnim) {
                circleBg.setAlpha(this.mCancelBgAlpha);
            }
            circleBg.draw(canvas);
        }
        if (this.mIsOrientationPortrait) {
            this.mCircleX = this.mContentWidth / 2;
            this.mCircleY = this.mStaticBgBottom - (staticBgHeight / 2);
        } else {
            this.mCircleX = this.mStaticBgRight - (staticBgHeight / 2);
            this.mCircleY = this.mContentHeight / 2;
        }
        if (this.mIsTouchShutBg && !(this.mIsTouchShutBg && this.mStateRestore)) {
            int temp;
            if (!this.mIsTouchShutBg) {
                f = 0.0f;
            } else if (!this.mIsShutDown) {
                f = 0.0f;
            }
            float f2;
            float f3;
            if (this.mIsTouchShutBg) {
                f2 = 0.0f;
                f3 = 0.0f;
            } else if ((this.mStateRestore ^ 1) != 0) {
                int largeRectTop = this.mCircleY - this.mLargeArcRadius;
                this.mLargeArcRect.set((float) (this.mCircleX - this.mLargeArcRadius), (float) largeRectTop, (float) (this.mCircleX + this.mLargeArcRadius), (float) (this.mCircleY + this.mLargeArcRadius));
                this.mLargeArcPaint.setShader(new SweepGradient((float) this.mCircleX, (float) this.mCircleY, new int[]{this.mLargeArcStartColor, this.mLargeArcEndColor, this.mLargeArcStartColor}, null));
                float reduceAngle = ((this.mTouchMoveY - this.mTouchDownY) * LARGESWEEPANGLE) / (((float) this.mShutDownHeight) * CRITICAL_NUMBER);
                if (this.mTouchMoveY - this.mTouchDownY >= ((float) this.mShutDownHeight) * CRITICAL_NUMBER) {
                    temp = (int) ((1.0f - (((this.mTouchMoveY - this.mTouchDownY) - (((float) this.mShutDownHeight) * CRITICAL_NUMBER)) / (((float) this.mShutDownHeight) * 0.19999999f))) * 255.0f);
                    if (temp <= 0) {
                        temp = 0;
                    }
                    this.mLargeArcPaint.setAlpha(temp);
                }
                if (reduceAngle >= LARGESWEEPANGLE) {
                    reduceAngle = LARGESWEEPANGLE;
                }
                if (reduceAngle <= 0.0f) {
                    reduceAngle = 0.0f;
                }
                f2 = LARGESWEEPANGLE - reduceAngle;
                f3 = LARGESTARTANGLE + reduceAngle;
                if (this.mIsOrientationPortrait) {
                    canvas.drawArc(this.mLargeArcRect, f3, f2, false, this.mLargeArcPaint);
                } else {
                    canvas.save();
                    canvas.rotate(-90.0f, (float) this.mCircleX, (float) this.mCircleY);
                    canvas.drawArc(this.mLargeArcRect, f3, f2, false, this.mLargeArcPaint);
                    canvas.restore();
                }
            } else {
                f2 = 0.0f;
                f3 = 0.0f;
            }
            if (!this.mIsTouchShutBg || ((this.mIsTouchShutBg && this.mStateRestore) || (this.mIsTouchShutBg && this.mIsShutDown))) {
                float smallArcAngle;
                float smallArcSweepAngle;
                smallRectTop = this.mCircleY - this.mSmallArcRadius;
                this.mSmallArcRect.set((float) (this.mCircleX - this.mSmallArcRadius), (float) smallRectTop, (float) (this.mCircleX + this.mSmallArcRadius), (float) (this.mCircleY + this.mSmallArcRadius));
                sweepGradient = new SweepGradient((float) this.mCircleX, (float) this.mCircleY, new int[]{this.mSmallArcStartColor, this.mSmallArcEndColor, this.mSmallArcStartColor}, null);
                if (this.mStartStaticAlpha) {
                    smallArcAngle = 302.0f;
                    smallArcSweepAngle = 296.0f;
                } else {
                    this.mSmallArcPaint.setAlpha((int) this.mStaticArcAlpha);
                    smallArcAngle = 302.0f + this.mSmallArcAngle;
                    smallArcSweepAngle = 296.0f - this.mSmallArcSweepAngle;
                }
                if (this.mFirstBgAlpha) {
                    this.mSmallArcPaint.setAlpha(this.mStartDrawableAlpha);
                }
                if (this.mIsExitAnim) {
                    this.mSmallArcPaint.setAlpha(this.mCancelBgAlpha);
                }
                this.mSmallArcPaint.setShader(sweepGradient);
                if (this.mIsOrientationPortrait) {
                    canvas.save();
                    canvas.rotate(-90.0f, (float) this.mCircleX, (float) this.mCircleY);
                    canvas.drawArc(this.mSmallArcRect, smallArcAngle, smallArcSweepAngle, false, this.mSmallArcPaint);
                    canvas.restore();
                } else {
                    canvas.drawArc(this.mSmallArcRect, smallArcAngle, smallArcSweepAngle, false, this.mSmallArcPaint);
                }
            }
            if (isIndiaRegion()) {
                if (this.mCancelBg != null) {
                    int cancelBgWidth = this.mCancelBg.getIntrinsicWidth();
                    if (this.mIsOrientationPortrait) {
                        this.mCancelBgLeft = (this.mContentWidth - cancelBgWidth) / 2;
                        this.mCancelBgTop = (int) (((float) this.mContentHeight) - this.mCancelBgDimen);
                    } else {
                        this.mCancelBgLeft = (int) (((float) this.mContentWidth) - this.mCancelBgDimen);
                        this.mCancelBgTop = (this.mContentHeight - cancelBgWidth) / 2;
                    }
                    this.mCancelBgRight = this.mCancelBgLeft + cancelBgWidth;
                    this.mCancelBgBottom = this.mCancelBgHeight + this.mCancelBgTop;
                    this.mCancelBg.setBounds(this.mCancelBgLeft, this.mCancelBgTop, this.mCancelBgRight, this.mCancelBgBottom);
                    temp = (int) ((1.0f - ((this.mTouchMoveY - this.mTouchDownY) / ((float) (((double) this.mShutDownHeight) * 0.5d)))) * 255.0f);
                    Log.i(TAG, "ColorGlobalActionView onDraw mShutDownHeight= " + this.mShutDownHeight + " temp= " + temp + " mTouchMoveY= " + this.mTouchMoveY + " mTouchDownY= " + this.mTouchDownY + " mIsTouchShutBg= " + this.mIsTouchShutBg + " mStateRestore= " + this.mStateRestore + " mIsShutDown= " + this.mIsShutDown);
                    if (temp <= 0) {
                        temp = 0;
                    }
                    if (this.mIsTouchShutBg) {
                        this.mCancelBg.setAlpha(temp);
                    }
                    if (this.mIsExitAnim) {
                        this.mCancelBg.setAlpha(this.mCancelBgAlpha);
                    }
                    if (this.mFirstBgAlpha) {
                        this.mCancelBg.setAlpha(this.mStartDrawableAlpha);
                    }
                    this.mCancelBg.draw(canvas);
                }
                if (this.mCancelText != null) {
                    fmi = this.mCancelPaint.getFontMetricsInt();
                    int cancelTextWidth = (int) this.mCancelPaint.measureText(this.mCancelText);
                    this.mCancelTextHeight = fmi.bottom - fmi.top;
                    if (this.mIsOrientationPortrait) {
                        textX = (float) ((this.mContentWidth - cancelTextWidth) / 2);
                        this.mCancelTextY = (int) (((float) (this.mContentHeight - fmi.top)) - this.mCancelTextDimen);
                    } else {
                        textX = (float) ((int) (((float) (this.mContentWidth - fmi.top)) - this.mCancelTextDimen));
                        this.mCancelTextY = (this.mContentHeight + cancelTextWidth) / 2;
                    }
                    temp = (int) ((1.0f - ((this.mTouchMoveY - this.mTouchDownY) / ((float) (((double) this.mShutDownHeight) * 0.5d)))) * 255.0f);
                    if (temp <= 0) {
                        temp = 0;
                    }
                    if (this.mFirstBgAlpha) {
                        this.mShutDownPaint.setAlpha(this.mStartTextAlpha);
                    }
                    if (this.mIsTouchShutBg) {
                        this.mCancelPaint.setAlpha(temp);
                    }
                    if (this.mIsExitAnim) {
                        this.mCancelPaint.setAlpha(this.mCancelTextAlpha);
                    }
                    if (this.mIsOrientationPortrait) {
                        canvas.drawText(this.mCancelText, textX, (float) this.mCancelTextY, this.mCancelPaint);
                        return;
                    }
                    canvas.save();
                    canvas.rotate(-90.0f, textX, (float) this.mCancelTextY);
                    canvas.drawText(this.mCancelText, textX, (float) this.mCancelTextY, this.mCancelPaint);
                    canvas.restore();
                    return;
                }
                return;
            }
            drawIndiaCancelBg(canvas);
            return;
        }
        if (this.mIsOrientationPortrait) {
            float startY;
            if (this.mStartStaticAlpha) {
                startY = ((float) this.mCircleY) - this.mStaticLineStartY;
            } else {
                startY = (float) (this.mCircleY - this.mSmallArcRadius);
            }
            this.mLinePath.rewind();
            this.mLinePath.moveTo((float) this.mCircleX, startY);
            this.mLinePath.lineTo((float) this.mCircleX, (float) this.mCircleY);
            lineShader = new LinearGradient((float) this.mCircleX, startY, (float) this.mCircleX, (float) this.mCircleY, this.mLineStartColor, this.mLineEndColor, TileMode.CLAMP);
            f = 0.0f;
        } else {
            if (this.mStartStaticAlpha) {
                f = ((float) this.mCircleX) - this.mStaticLineStartY;
            } else {
                f = (float) (this.mCircleX - this.mSmallArcRadius);
            }
            this.mLinePath.rewind();
            this.mLinePath.moveTo(f, (float) this.mCircleY);
            this.mLinePath.lineTo((float) this.mCircleX, (float) this.mCircleY);
            Shader linearGradient = new LinearGradient(f, (float) this.mCircleY, (float) this.mCircleX, (float) this.mCircleY, this.mLineStartColor, this.mLineEndColor, TileMode.CLAMP);
        }
        if (this.mStartStaticAlpha) {
            this.mLinePaint.setAlpha((int) this.mStaticLineAlpha);
        }
        if (this.mIsExitAnim) {
            this.mLinePaint.setAlpha(this.mCancelBgAlpha);
        }
        if (this.mFirstBgAlpha) {
            this.mLinePaint.setAlpha(this.mStartDrawableAlpha);
        }
        this.mLinePaint.setShader(lineShader);
        canvas.drawPath(this.mLinePath, this.mLinePaint);
        if (this.mIsTouchShutBg) {
        }
        smallRectTop = this.mCircleY - this.mSmallArcRadius;
        this.mSmallArcRect.set((float) (this.mCircleX - this.mSmallArcRadius), (float) smallRectTop, (float) (this.mCircleX + this.mSmallArcRadius), (float) (this.mCircleY + this.mSmallArcRadius));
        sweepGradient = new SweepGradient((float) this.mCircleX, (float) this.mCircleY, new int[]{this.mSmallArcStartColor, this.mSmallArcEndColor, this.mSmallArcStartColor}, null);
        if (this.mStartStaticAlpha) {
        }
        if (this.mFirstBgAlpha) {
        }
        if (this.mIsExitAnim) {
        }
        this.mSmallArcPaint.setShader(sweepGradient);
        if (this.mIsOrientationPortrait) {
        }
        if (isIndiaRegion()) {
        }
    }

    private void startAutoDownReboundAnim() {
        ValueAnimator rebounceAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        rebounceAnim.addUpdateListener(new AnimUpdateListener(2));
        rebounceAnim.setDuration(150);
        rebounceAnim.setInterpolator(AUTODOWNREBOUNCEINTER);
        rebounceAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                ColorGlobalActionView.this.mFirstRebounceAnim = false;
            }

            public void onAnimationEnd(Animator animation) {
                ColorGlobalActionView.this.mFirstRebounceAnim = true;
                ColorGlobalActionView.this.mFirstBgAlpha = false;
                ColorGlobalActionView.this.mTouchHelper.sendEventForVirtualView(0, 8);
            }
        });
        rebounceAnim.start();
    }

    private boolean detectionIsClickCancelBg(float x, float y) {
        if (x < ((float) this.mCancelBgLeft) || x > ((float) this.mCancelBgRight) || y < ((float) this.mCancelBgTop) || y > ((float) this.mCancelBgBottom) || !this.mFirstRebounceAnim) {
            return false;
        }
        return true;
    }

    private boolean detectionIsTouchShutDownBg(float x, float y) {
        if (x < ((float) this.mStaticBgLeft) || x > ((float) this.mStaticBgRight) || y < ((float) this.mStaticBgTop) || y > ((float) this.mStaticBgBottom) || !this.mFirstRebounceAnim) {
            return false;
        }
        return true;
    }

    private boolean detectionAccessTouch(float x, float y) {
        if (x < 0.0f || x > ((float) this.mContentWidth) || y < 0.0f || y > ((float) (this.mStaticBgBottom * 2)) || !this.mFirstRebounceAnim) {
            return false;
        }
        return true;
    }

    private void startExitAnim() {
        ValueAnimator exitAlpha = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        exitAlpha.addUpdateListener(new AnimUpdateListener(3));
        exitAlpha.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorGlobalActionView.this.setQuitView();
            }
        });
        exitAlpha.setDuration(250);
        exitAlpha.setInterpolator(EXITAINM);
        exitAlpha.start();
    }

    private void setQuitView() {
        if (this.mOnCancelListener != null) {
            this.mOnCancelListener.onCancel();
        }
        this.mTouchHelper.sendEventForVirtualView(1, 1);
    }

    private void startReturnInitialPosition() {
        double time = (double) (((this.mTouchUpY - this.mTouchDownY) / (((float) this.mShutDownHeight) * CRITICAL_NUMBER)) * 500.0f);
        this.mRestoreYAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mRestoreYAnim.addUpdateListener(new AnimUpdateListener(4));
        this.mRestoreYAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorGlobalActionView.this.startRestoreFirstRebound();
            }
        });
        this.mRestoreYAnim.setDuration((long) time);
        this.mRestoreYAnim.setInterpolator(RESTOREPOSITION);
        this.mRestoreYAnim.start();
    }

    private void startCancelToInitialPosition() {
        this.mRestoreYAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mRestoreYAnim.addUpdateListener(new AnimUpdateListener(4));
        this.mRestoreYAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorGlobalActionView.this.startRestoreFirstRebound();
            }
        });
        this.mRestoreYAnim.setDuration(100);
        this.mRestoreYAnim.setInterpolator(RESTOREPOSITION);
        this.mRestoreYAnim.start();
    }

    private void startRestoreFirstRebound() {
        this.mFirstRebound = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mFirstRebound.addUpdateListener(new AnimUpdateListener(5));
        this.mFirstRebound.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorGlobalActionView.this.startRestoreSecondRebound();
            }
        });
        this.mFirstRebound.setDuration(150);
        this.mFirstRebound.setInterpolator(RESTOREREBOUNCE);
        this.mFirstRebound.start();
    }

    private void startRestoreSecondRebound() {
        this.mSecondRebound = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mSecondRebound.addUpdateListener(new AnimUpdateListener(6));
        this.mSecondRebound.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorGlobalActionView.this.mTouchMoveY = 0.0f;
                ColorGlobalActionView.this.mTouchDownY = 0.0f;
                ColorGlobalActionView.this.mStaticMoveY = 0.0f;
                ColorGlobalActionView.this.mStateRestore = false;
                ColorGlobalActionView.this.mIsTouchShutBg = false;
            }
        });
        this.mSecondRebound.setDuration(150);
        this.mSecondRebound.setInterpolator(RESTOREREBOUNCE);
        this.mSecondRebound.start();
    }

    private void startShutDownYAnim() {
        double time = (((double) ((this.mTouchUpY - this.mTouchDownY) - (((float) this.mShutDownHeight) * CRITICAL_NUMBER))) / (((double) this.mShutDownHeight) * 0.25d)) * 200.0d;
        ValueAnimator shutDownYAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        shutDownYAnim.addUpdateListener(new AnimUpdateListener(7));
        shutDownYAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorGlobalActionView.this.startStaticBgAlphaAnim();
            }
        });
        shutDownYAnim.setDuration((long) time);
        shutDownYAnim.setInterpolator(SHUTDOWNINTER);
        shutDownYAnim.start();
    }

    private void startStaticBgAlphaAnim() {
        ValueAnimator staticBgAlphaAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        staticBgAlphaAnim.addUpdateListener(new AnimUpdateListener(8));
        staticBgAlphaAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                ColorGlobalActionView.this.mStartStaticAlpha = true;
                ColorGlobalActionView.this.startStaticLineAnim();
                ColorGlobalActionView.this.startStaticArcAnim();
            }
        });
        staticBgAlphaAnim.setDuration(100);
        staticBgAlphaAnim.setInterpolator(EXITAINM);
        staticBgAlphaAnim.start();
    }

    private void startStaticLineAnim() {
        ValueAnimator staticLineAlphaAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        staticLineAlphaAnim.addUpdateListener(new AnimUpdateListener(9));
        staticLineAlphaAnim.setDuration(300);
        staticLineAlphaAnim.setInterpolator(EXITAINM);
        staticLineAlphaAnim.start();
    }

    private void startStaticArcAnim() {
        ValueAnimator staticArcAlphaAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        staticArcAlphaAnim.addUpdateListener(new AnimUpdateListener(10));
        staticArcAlphaAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (ColorGlobalActionView.this.mShutDownLister != null) {
                    ColorGlobalActionView.this.mShutDownLister.onShutDown();
                }
            }
        });
        staticArcAlphaAnim.setDuration(350);
        staticArcAlphaAnim.setInterpolator(EXITAINM);
        staticArcAlphaAnim.start();
    }

    private boolean isMultiPointerEvent(MotionEvent event) {
        if (event.getPointerId(event.getActionIndex()) > 0) {
            return true;
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case 0:
                this.mIsClickCancelBg = detectionIsClickCancelBg(x, y);
                if (ColorAccessibilityUtil.isTalkbackEnabled(getContext()) && AccessibilityManager.getInstance(getContext()).isTouchExplorationEnabled()) {
                    this.mIsTouchShutBg = detectionAccessTouch(x, y);
                } else {
                    this.mIsTouchShutBg = detectionIsTouchShutDownBg(x, y);
                }
                if (this.mSecondRebound != null && this.mSecondRebound.isRunning()) {
                    this.mSecondRebound.end();
                    this.mIsTouchShutBg = false;
                }
                if (this.mFirstRebound != null && this.mFirstRebound.isRunning()) {
                    this.mFirstRebound.end();
                    this.mIsTouchShutBg = false;
                }
                if (this.mRestoreYAnim != null && this.mRestoreYAnim.isRunning()) {
                    this.mRestoreYAnim.end();
                    this.mIsTouchShutBg = false;
                }
                if (this.mIsOrientationPortrait) {
                    this.mShutDownHeight = this.mCancelBgTop - (this.mStaticBgTop + (this.mStaticBg.getIntrinsicHeight() / 2));
                } else {
                    this.mShutDownHeight = this.mCancelBgLeft - (this.mStaticBgLeft + (this.mStaticBg.getIntrinsicHeight() / 2));
                }
                if (this.mIsTouchShutBg && this.mIsOrientationPortrait) {
                    this.mTouchDownY = event.getY();
                } else if (this.mIsTouchShutBg && (this.mIsOrientationPortrait ^ 1) != 0) {
                    this.mTouchDownY = event.getX();
                }
                if (this.mIsClickCancelBg) {
                    this.mCancelBg = this.mCancelPressBg;
                }
                this.mIsClickEmergencyBg = detectionIsClickEmergencyBg(x, y);
                if (this.mIsClickEmergencyBg) {
                    this.mEmergencyBg = this.mEmergencyPressBg;
                }
                invalidate();
                break;
            case 1:
                if (this.mIsTouchShutBg) {
                    this.mTouchUpY = this.mTouchMoveY;
                    if (this.mTouchUpY - this.mTouchDownY < ((float) this.mShutDownHeight) * CRITICAL_NUMBER && this.mTouchUpY > this.mTouchDownY) {
                        this.mStateRestore = true;
                        startReturnInitialPosition();
                    }
                    if (this.mTouchUpY - this.mTouchDownY >= ((float) this.mShutDownHeight) * CRITICAL_NUMBER && this.mTouchUpY > this.mTouchDownY) {
                        if (this.mTouchUpY - this.mTouchDownY > ((float) this.mShutDownHeight) && this.mTouchUpY > this.mTouchDownY) {
                            this.mTouchUpY = this.mTouchDownY + ((float) this.mShutDownHeight);
                        }
                        this.mIsShutDown = true;
                        setEnabled(false);
                        startShutDownYAnim();
                    }
                    if (this.mTouchUpY <= this.mTouchDownY) {
                        this.mTouchUpY = this.mTouchDownY;
                        this.mStateRestore = false;
                        this.mIsTouchShutBg = false;
                    }
                    invalidate();
                }
                if (this.mIsClickCancelBg) {
                    this.mCancelBg = this.mCancelNormalBg;
                    this.mIsExitAnim = this.mIsClickCancelBg;
                    startExitAnim();
                    invalidate();
                }
                if (this.mIsClickEmergencyBg) {
                    this.mEmergencyBg = this.mEmergencyNormalBg;
                    this.mIsExitAnim = this.mIsClickEmergencyBg;
                    startEmergencyExitAnim();
                    invalidate();
                    break;
                }
                break;
            case 2:
                if (this.mIsTouchShutBg) {
                    float dimen;
                    if (this.mIsOrientationPortrait) {
                        this.mTouchMoveY = event.getY();
                        if (((double) (this.mTouchMoveY - this.mTouchDownY)) > ((double) this.mShutDownHeight) * 0.8d) {
                            dimen = (float) ((((double) this.mTouchMoveY) - (((double) this.mShutDownHeight) * 0.8d)) - ((double) (this.mCancelBgTop - this.mShutDownHeight)));
                            this.mTouchMoveY = (float) ((((double) (this.mCancelBgTop - this.mShutDownHeight)) + (((double) this.mShutDownHeight) * 0.8d)) + ((double) ((float) (((((double) this.mShutDownHeight) * 0.4d) * ((double) dimen)) / ((((double) this.mShutDownHeight) * 0.4d) + ((double) dimen))))));
                        }
                    } else {
                        this.mTouchMoveY = event.getX();
                        if (((double) (this.mTouchMoveY - this.mTouchDownY)) > ((double) this.mShutDownHeight) * 0.8d) {
                            dimen = (float) ((((double) this.mTouchMoveY) - (((double) this.mShutDownHeight) * 0.8d)) - ((double) (this.mCancelBgLeft - this.mShutDownHeight)));
                            this.mTouchMoveY = (float) ((((double) (this.mCancelBgLeft - this.mShutDownHeight)) + (((double) this.mShutDownHeight) * 0.8d)) + ((double) ((float) (((((double) this.mShutDownHeight) * 0.4d) * ((double) dimen)) / ((((double) this.mShutDownHeight) * 0.4d) + ((double) dimen))))));
                        }
                    }
                    if (this.mTouchMoveY - this.mTouchDownY > ((float) this.mShutDownHeight) && this.mTouchMoveY > this.mTouchDownY) {
                        this.mTouchMoveY = this.mTouchDownY + ((float) this.mShutDownHeight);
                    }
                    this.mStaticMoveY = this.mTouchMoveY;
                    invalidate();
                    break;
                }
                break;
            case 3:
                Log.i(TAG, "ColorGlobalActionView ACTION_CANCEL mTouchDownY = " + this.mTouchDownY + ", mTouchUpY = " + this.mTouchUpY + " event.getPointerCount()= " + event.getPointerCount() + " mTouchMoveY= " + this.mTouchMoveY + " mIsClickCancelBg= " + this.mIsClickCancelBg + " mIsClickEmergencyBg= " + this.mIsClickEmergencyBg + " mIsTouchShutBg= " + this.mIsTouchShutBg + " mShutDownHeight= " + this.mShutDownHeight);
                if (2 < event.getPointerCount()) {
                    this.mTouchUpY = this.mTouchDownY;
                    this.mIsTouchShutBg = false;
                    this.mStateRestore = true;
                    startCancelToInitialPosition();
                } else if (this.mIsTouchShutBg) {
                    this.mTouchUpY = this.mTouchMoveY;
                    if (this.mTouchUpY - this.mTouchDownY < ((float) this.mShutDownHeight) * CRITICAL_NUMBER && this.mTouchUpY > this.mTouchDownY) {
                        this.mStateRestore = true;
                        startReturnInitialPosition();
                    }
                    if (this.mTouchUpY - this.mTouchDownY >= ((float) this.mShutDownHeight) * CRITICAL_NUMBER && this.mTouchUpY > this.mTouchDownY) {
                        if (this.mTouchUpY - this.mTouchDownY > ((float) this.mShutDownHeight) && this.mTouchUpY > this.mTouchDownY) {
                            this.mTouchUpY = this.mTouchDownY + ((float) this.mShutDownHeight);
                        }
                        this.mIsShutDown = true;
                        setEnabled(false);
                        startShutDownYAnim();
                    }
                    if (this.mTouchUpY <= this.mTouchDownY) {
                        this.mTouchUpY = this.mTouchDownY;
                        this.mStateRestore = false;
                        this.mIsTouchShutBg = false;
                    }
                }
                if (this.mIsClickCancelBg) {
                    this.mCancelBg = this.mCancelNormalBg;
                    this.mIsExitAnim = this.mIsClickCancelBg;
                    startExitAnim();
                }
                if (this.mIsClickEmergencyBg) {
                    this.mEmergencyBg = this.mEmergencyNormalBg;
                    this.mIsExitAnim = this.mIsClickEmergencyBg;
                    startEmergencyExitAnim();
                }
                invalidate();
                break;
        }
        return true;
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mSecondRebound = null;
        this.mFirstRebound = null;
        this.mRestoreYAnim = null;
        this.mStartStaticAlpha = false;
    }

    public void startAutoDownAnim() {
        setEnabled(true);
        ValueAnimator autoDownAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        autoDownAnim.addUpdateListener(new AnimUpdateListener(1));
        autoDownAnim.setDuration(475);
        autoDownAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorGlobalActionView.this.mShutDownBgAutoEnd = ColorGlobalActionView.this.mShutDownBgDimen;
                ColorGlobalActionView.this.mCancelAutoEnd = ColorGlobalActionView.this.mCancelBgDimen;
                ColorGlobalActionView.this.startAutoDownReboundAnim();
            }
        });
        autoDownAnim.setInterpolator(AUTODOWNINTER);
        autoDownAnim.start();
    }

    public void setOnCancelListener(OnCancelListener cancel) {
        this.mOnCancelListener = cancel;
    }

    public void setOnShutDownListener(OnShutDownListener shutDown) {
        this.mShutDownLister = shutDown;
    }

    private boolean detectionIsClickEmergencyBg(float x, float y) {
        if (x < ((float) this.mEmergencyBgLeft) || x > ((float) this.mEmergencyBgRight) || y < ((float) this.mEmergencyBgTop) || y > ((float) this.mEmergencyBgBottom) || !this.mFirstRebounceAnim) {
            return false;
        }
        return true;
    }

    private void startEmergencyExitAnim() {
        ValueAnimator exitAlpha = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        exitAlpha.addUpdateListener(new AnimUpdateListener(3));
        exitAlpha.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (ColorGlobalActionView.this.mOnEmergencyListener != null) {
                    ColorGlobalActionView.this.mOnEmergencyListener.onEmergency();
                }
                if (ColorGlobalActionView.isIndiaRegion()) {
                    ColorGlobalActionView.this.mTouchHelper.sendEventForVirtualView(2, 1);
                }
            }
        });
        exitAlpha.setDuration(250);
        exitAlpha.setInterpolator(EXITAINM);
        exitAlpha.start();
    }

    public void setOnEmergencyListener(OnEmergencyListener emergency) {
        this.mOnEmergencyListener = emergency;
    }

    public static boolean isIndiaRegion() {
        if ("IN".equalsIgnoreCase(SystemProperties.get("persist.sys.oppo.region", "CN"))) {
            return true;
        }
        return false;
    }

    public void drawIndiaCancelBg(Canvas canvas) {
        int temp;
        if (!(this.mCancelBg == null || this.mEmergencyBg == null)) {
            int cancelBgWidth = this.mCancelBg.getIntrinsicWidth();
            if (this.mIsOrientationPortrait) {
                this.mCancelBgLeft = (this.mContentWidth / 2) + (((this.mContentWidth / 2) - cancelBgWidth) / 2);
                this.mEmergencyBgLeft = ((this.mContentWidth / 2) - cancelBgWidth) / 2;
                this.mEmergencyBgTop = (int) (((float) this.mContentHeight) - this.mCancelBgDimen);
                this.mCancelBgTop = this.mEmergencyBgTop;
            } else {
                this.mEmergencyBgLeft = (int) (((float) this.mContentWidth) - this.mCancelBgDimen);
                this.mCancelBgLeft = this.mEmergencyBgLeft;
                this.mCancelBgTop = ((this.mContentHeight / 2) - cancelBgWidth) / 2;
                this.mEmergencyBgTop = (this.mContentHeight / 2) + (((this.mContentHeight / 2) - cancelBgWidth) / 2);
            }
            this.mCancelBgRight = this.mCancelBgLeft + cancelBgWidth;
            this.mCancelBgBottom = this.mCancelBgHeight + this.mCancelBgTop;
            this.mCancelBg.setBounds(this.mCancelBgLeft, this.mCancelBgTop, this.mCancelBgRight, this.mCancelBgBottom);
            this.mEmergencyBgRight = this.mEmergencyBgLeft + cancelBgWidth;
            this.mEmergencyBgBottom = this.mCancelBgHeight + this.mEmergencyBgTop;
            this.mEmergencyBg.setBounds(this.mEmergencyBgLeft, this.mEmergencyBgTop, this.mEmergencyBgRight, this.mEmergencyBgBottom);
            temp = (int) ((1.0f - ((this.mTouchMoveY - this.mTouchDownY) / ((float) (((double) this.mShutDownHeight) * 0.5d)))) * 255.0f);
            if (temp <= 0) {
                temp = 0;
            }
            if (this.mIsTouchShutBg) {
                this.mCancelBg.setAlpha(temp);
                this.mEmergencyBg.setAlpha(temp);
            }
            if (this.mIsExitAnim) {
                this.mCancelBg.setAlpha(this.mCancelBgAlpha);
                this.mEmergencyBg.setAlpha(this.mCancelBgAlpha);
            }
            if (this.mFirstBgAlpha) {
                this.mCancelBg.setAlpha(this.mStartDrawableAlpha);
                this.mEmergencyBg.setAlpha(this.mStartDrawableAlpha);
            }
            this.mCancelBg.draw(canvas);
            this.mEmergencyBg.draw(canvas);
        }
        if (this.mCancelText != null && this.mEmergencyText != null) {
            float cancelTextX;
            float emergencyTextX;
            FontMetricsInt fmi = this.mCancelPaint.getFontMetricsInt();
            int cancelTextWidth = (int) this.mCancelPaint.measureText(this.mCancelText);
            int emergencyTextWidth = (int) this.mCancelPaint.measureText(this.mEmergencyText);
            this.mCancelTextHeight = fmi.bottom - fmi.top;
            if (this.mIsOrientationPortrait) {
                cancelTextX = (float) ((this.mContentWidth / 2) + (((this.mContentWidth / 2) - cancelTextWidth) / 2));
                emergencyTextX = (float) (((this.mContentWidth / 2) - emergencyTextWidth) / 2);
                this.mEmergencyTextY = (int) (((float) (this.mContentHeight - fmi.top)) - this.mCancelTextDimen);
                this.mCancelTextY = this.mEmergencyTextY;
            } else {
                emergencyTextX = (float) ((int) (((float) (this.mContentWidth - fmi.top)) - this.mCancelTextDimen));
                cancelTextX = emergencyTextX;
                this.mCancelTextY = ((this.mContentHeight / 2) + cancelTextWidth) / 2;
                this.mEmergencyTextY = (this.mContentHeight / 2) + (((this.mContentHeight / 2) + emergencyTextWidth) / 2);
            }
            temp = (int) ((1.0f - ((this.mTouchMoveY - this.mTouchDownY) / ((float) (((double) this.mShutDownHeight) * 0.5d)))) * 255.0f);
            if (temp <= 0) {
                temp = 0;
            }
            if (this.mFirstBgAlpha) {
                this.mShutDownPaint.setAlpha(this.mStartTextAlpha);
            }
            if (this.mIsTouchShutBg) {
                this.mCancelPaint.setAlpha(temp);
            }
            if (this.mIsExitAnim) {
                this.mCancelPaint.setAlpha(this.mCancelTextAlpha);
            }
            if (this.mIsOrientationPortrait) {
                canvas.drawText(this.mCancelText, cancelTextX, (float) this.mCancelTextY, this.mCancelPaint);
                canvas.drawText(this.mEmergencyText, emergencyTextX, (float) this.mEmergencyTextY, this.mCancelPaint);
                return;
            }
            canvas.save();
            canvas.rotate(-90.0f, cancelTextX, (float) this.mCancelTextY);
            canvas.drawText(this.mCancelText, cancelTextX, (float) this.mCancelTextY, this.mCancelPaint);
            canvas.restore();
            canvas.save();
            canvas.rotate(-90.0f, emergencyTextX, (float) this.mEmergencyTextY);
            canvas.drawText(this.mEmergencyText, emergencyTextX, (float) this.mEmergencyTextY, this.mCancelPaint);
            canvas.restore();
        }
    }

    public void clearAccessibilityFocus() {
        if (this.mTouchHelper != null) {
            this.mTouchHelper.clearFocusedVirtualView();
        }
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mTouchHelper == null || !this.mTouchHelper.dispatchHoverEvent(event)) {
            return super.dispatchHoverEvent(event);
        }
        return true;
    }
}
