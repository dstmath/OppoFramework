package com.color.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.telecom.Logging.Session;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.IntArray;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OppoBezierInterpolator;
import android.view.animation.PathInterpolator;
import com.android.internal.widget.ExploreByTouchHelper;
import com.color.util.ColorChangeTextUtil;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ColorGridView extends View {
    private static final float APPNAME_TEXT_FIRST_SCALEMULTIPLIER = 0.88f;
    private static final float APPNAME_TEXT_SECOND_SCALE_MULTIPLIER = 0.7744f;
    public static final int COLUMN_SIZE = 4;
    private static final int PFLAG_PREPRESSED = 33554432;
    private static final int PFLAG_PRESSED = 16384;
    private static final Interpolator POLATOR = new PathInterpolator(0.32f, 1.22f, 0.32f, 1.0f);
    public static final String TAG = "ColorGridView";
    private static final int TOUCH_END_DURATION = 300;
    private static final float TOUCH_MODE_BRIGHTNESS = 1.09f;
    private static final int TOUCH_START_DURATION = 66;
    private int dotViewHeight;
    /* access modifiers changed from: private */
    public boolean isSelected;
    private int mAppIconMarginBottom;
    private int mAppIconMarginTop;
    private Integer[][] mAppIcons;
    /* access modifiers changed from: private */
    public ColorItem[][] mAppInfos;
    private int mAppNameSize;
    private String[][] mAppNames;
    /* access modifiers changed from: private */
    public boolean[][] mCanDraw;
    private Runnable mCancelclickRunnable;
    private int mChineseLength;
    /* access modifiers changed from: private */
    public int mColumnCounts;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public float mCurrentBrightness;
    /* access modifiers changed from: private */
    public int mCurrentIconWidth;
    /* access modifiers changed from: private */
    public int mCurrentPosition;
    private float mDownX;
    private float mDownY;
    private int mEnglishLength;
    private GestureDetector mGestureDetector;
    private int mIconDistance;
    private int mIconHeight;
    private int mIconWidth;
    private boolean mIsLandscape;
    private int mItemBgPaddingRight;
    /* access modifiers changed from: private */
    public int mItemCounts;
    /* access modifiers changed from: private */
    public int mItemHeight;
    /* access modifiers changed from: private */
    public int mItemWidth;
    private Drawable mMoreIcon;
    /* access modifiers changed from: private */
    public int mMoreIconAlpha;
    private int mMoreIconIndex;
    private int mNavBarHeight;
    private boolean mNeedExpandAnim;
    private int mOShareIconMarginBottom;
    private int mOShareIconMarginTop;
    private OnItemClickListener mOnItemClickListener;
    /* access modifiers changed from: private */
    public Runnable mOnclickRunnable;
    /* access modifiers changed from: private */
    public int[][] mOpacity;
    private int mPaddingLeft;
    private int mPaddingTop;
    public int mPageNumber;
    private int mPagerSize;
    private TextPaint mPaint1;
    private Paint mPaint2;
    private int mPrivateFlags;
    private List<ResolveInfo> mResolveInfoList;
    private int mRowCounts;
    /* access modifiers changed from: private */
    public Float[][] mScale;
    private int mSelectColor;
    private ColorMatrixColorFilter mSelectColorFilter;
    private ColorMatrix mSelectColorMatrix;
    private int mSelectHeight;
    private int mSelectWidth;
    private int mTextColor;
    private int mTextPaddingBottom;
    private int mTextPaddingLeft;
    private int mTotalHeight;
    private OppoBezierInterpolator mTouchEndInterpolator;
    private final ColorViewTouchHelper mTouchHelper;
    private int mTouchModeWidth;
    private OppoBezierInterpolator mTouchStartInterpolator;
    private int mTwoLineDistance;
    /* access modifiers changed from: private */
    public Rect selRect;
    private Rect selRect2;
    private int selectX;
    private int selectY;

    public interface OnItemClickListener {
        void OnItemClick(int i);

        void OnItemLongClick(int i);
    }

    static /* synthetic */ int access$476(ColorGridView x0, int x1) {
        int i = x0.mPrivateFlags | x1;
        x0.mPrivateFlags = i;
        return i;
    }

    public ColorGridView(Context context) {
        this(context, null);
    }

    public ColorGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mPagerSize = 4;
        this.mColumnCounts = 4;
        this.mRowCounts = 2;
        this.mItemCounts = 0;
        this.mPageNumber = 1;
        this.isSelected = false;
        this.selRect = new Rect();
        this.selRect2 = new Rect();
        this.mSelectColorMatrix = new ColorMatrix();
        this.mResolveInfoList = new ArrayList();
        this.mAppInfos = null;
        Float valueOf = Float.valueOf(1.0f);
        this.mScale = new Float[][]{new Float[]{valueOf, valueOf, valueOf, valueOf}, new Float[]{valueOf, valueOf, valueOf, valueOf}};
        this.mOpacity = new int[][]{new int[]{255, 255, 255, 255}, new int[]{255, 255, 255, 255}};
        this.mMoreIconAlpha = 255;
        this.mCanDraw = (boolean[][]) Array.newInstance(boolean.class, 2, 4);
        this.mMoreIconIndex = 1;
        this.mTouchStartInterpolator = new OppoBezierInterpolator(0.25d, 0.1d, 0.1d, 1.0d, true);
        this.mTouchEndInterpolator = new OppoBezierInterpolator(0.25d, 0.1d, 0.25d, 1.0d, true);
        this.mCurrentBrightness = 1.0f;
        this.mOnclickRunnable = new Runnable() {
            /* class com.color.widget.ColorGridView.AnonymousClass6 */

            public void run() {
                ColorGridView.access$476(ColorGridView.this, -33554433);
                ColorGridView colorGridView = ColorGridView.this;
                colorGridView.invalidate(colorGridView.selRect);
            }
        };
        this.mCancelclickRunnable = new Runnable() {
            /* class com.color.widget.ColorGridView.AnonymousClass7 */

            public void run() {
                ColorGridView.access$476(ColorGridView.this, -33554433);
                ColorGridView colorGridView = ColorGridView.this;
                colorGridView.removeCallbacks(colorGridView.mOnclickRunnable);
                boolean unused = ColorGridView.this.isSelected = false;
                ColorGridView colorGridView2 = ColorGridView.this;
                colorGridView2.invalidate(colorGridView2.selRect);
            }
        };
        this.mCurrentPosition = -1;
        setLayerType(1, null);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.mContext = context;
        this.mContext.getResources().getConfiguration();
        initGetureDetecor();
        this.mSelectColor = getResources().getColor(201720851);
        this.mTextColor = getResources().getColor(201720854);
        this.mAppNameSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) getResources().getDimensionPixelSize(201655745), this.mContext.getResources().getConfiguration().fontScale, 2);
        this.dotViewHeight = (int) getResources().getDimension(201655408);
        this.mItemHeight = (int) getResources().getDimension(201655410);
        this.mSelectHeight = (int) getResources().getDimension(201655399);
        this.mSelectWidth = (int) getResources().getDimension(201655400);
        this.mPaddingLeft = (int) getResources().getDimension(201655411);
        this.mPaddingTop = (int) getResources().getDimension(201655412);
        this.mOShareIconMarginTop = (int) getResources().getDimension(201655705);
        this.mAppIconMarginTop = (int) getResources().getDimension(201655706);
        this.mIconHeight = getResources().getDimensionPixelSize(201655413);
        this.mIconWidth = getResources().getDimensionPixelSize(201655414);
        int i = this.mIconWidth;
        this.mCurrentIconWidth = i;
        this.mTouchModeWidth = i + dip2px(getContext(), 3.0f);
        this.mItemBgPaddingRight = getResources().getDimensionPixelSize(201655465);
        this.mTextPaddingBottom = getResources().getDimensionPixelSize(201655466);
        this.mTwoLineDistance = getResources().getDimensionPixelSize(201655467);
        this.mChineseLength = getResources().getDimensionPixelSize(201655494);
        this.mEnglishLength = getResources().getDimensionPixelSize(201655495);
        this.mMoreIcon = this.mContext.getDrawable(201852207);
        this.mSelectColorFilter = new ColorMatrixColorFilter(this.mSelectColorMatrix);
        this.mTouchHelper = new ColorViewTouchHelper(this);
        setAccessibilityDelegate(this.mTouchHelper);
        setImportantForAccessibility(1);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Configuration cfg = this.mContext.getResources().getConfiguration();
        if (this.mAppInfos != null) {
            if (cfg.orientation == 2) {
                this.mRowCounts = 1;
                this.mIsLandscape = true;
            } else {
                this.mRowCounts = Math.min(2, this.mAppInfos.length);
                this.mIsLandscape = false;
            }
            Context context = this.mContext;
            if ((context instanceof Activity) && ((Activity) context).isInMultiWindowMode()) {
                this.mRowCounts = 1;
                this.mIsLandscape = true;
            }
            this.mTextPaddingLeft = 0;
            this.mItemWidth = getWidth() / this.mColumnCounts;
            this.mPaint1 = new TextPaint();
            this.mPaint1.setColor(this.mTextColor);
            this.mPaint1.setTextSize((float) this.mAppNameSize);
            this.mPaint1.setTextAlign(Paint.Align.CENTER);
            this.mPaint1.setAntiAlias(true);
            this.mPaint1.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            this.mPaint2 = new Paint();
            this.mPaint2.setColor(this.mSelectColor);
            for (int i = 0; i < this.mRowCounts; i++) {
                for (int j = 0; j < this.mAppInfos[i].length; j++) {
                    int position = (this.mColumnCounts * i) + j;
                    getRect2(j, i, this.selRect2, position);
                    ColorItem[][] colorItemArr = this.mAppInfos;
                    if (colorItemArr[i][j] != null) {
                        colorItemArr[i][j].getIcon().setBounds(this.selRect2);
                        if (this.mNeedExpandAnim) {
                            if (position == this.mMoreIconIndex) {
                                if (position == this.mCurrentPosition) {
                                    ColorMatrix colorMatrix = this.mSelectColorMatrix;
                                    float f = this.mCurrentBrightness;
                                    colorMatrix.setScale(f, f, f, 1.0f);
                                    this.mSelectColorFilter.setColorMatrix(this.mSelectColorMatrix);
                                    this.mMoreIcon.setColorFilter(this.mSelectColorFilter);
                                } else {
                                    this.mSelectColorMatrix.setScale(1.0f, 1.0f, 1.0f, 1.0f);
                                    this.mSelectColorFilter.setColorMatrix(this.mSelectColorMatrix);
                                    this.mMoreIcon.setColorFilter(this.mSelectColorFilter);
                                }
                                this.mMoreIcon.setBounds(this.selRect2);
                                this.mMoreIcon.setAlpha(this.mMoreIconAlpha);
                                this.mMoreIcon.draw(canvas);
                                Log.d(TAG, "moreIcon = " + position + ", alpha = " + this.mMoreIconAlpha);
                            }
                            if (this.mCanDraw[i][j]) {
                                canvas.save();
                                float scale = this.mScale[i][j].floatValue();
                                canvas.scale(scale, scale, this.selRect2.exactCenterX(), this.selRect2.exactCenterY());
                                if (position == this.mCurrentPosition) {
                                    ColorMatrix colorMatrix2 = this.mSelectColorMatrix;
                                    float f2 = this.mCurrentBrightness;
                                    colorMatrix2.setScale(f2, f2, f2, 1.0f);
                                    this.mSelectColorFilter.setColorMatrix(this.mSelectColorMatrix);
                                    this.mAppInfos[i][j].getIcon().setColorFilter(this.mSelectColorFilter);
                                } else {
                                    this.mSelectColorMatrix.setScale(1.0f, 1.0f, 1.0f, 1.0f);
                                    this.mSelectColorFilter.setColorMatrix(this.mSelectColorMatrix);
                                    this.mAppInfos[i][j].getIcon().setColorFilter(this.mSelectColorFilter);
                                }
                                this.mAppInfos[i][j].getIcon().setAlpha(this.mOpacity[i][j]);
                                this.mAppInfos[i][j].getIcon().draw(canvas);
                                canvas.restore();
                                this.mPaint1.setAlpha((int) (((float) this.mOpacity[i][j]) * 0.7f));
                                drawText(canvas, this.mAppInfos[i][j].getText(), i, j);
                            }
                        } else {
                            if (position == this.mCurrentPosition) {
                                ColorMatrix colorMatrix3 = this.mSelectColorMatrix;
                                float f3 = this.mCurrentBrightness;
                                colorMatrix3.setScale(f3, f3, f3, 1.0f);
                                this.mSelectColorFilter.setColorMatrix(this.mSelectColorMatrix);
                                this.mAppInfos[i][j].getIcon().setColorFilter(this.mSelectColorFilter);
                            } else {
                                this.mSelectColorMatrix.setScale(1.0f, 1.0f, 1.0f, 1.0f);
                                this.mSelectColorFilter.setColorMatrix(this.mSelectColorMatrix);
                                this.mAppInfos[i][j].getIcon().setColorFilter(this.mSelectColorFilter);
                            }
                            this.mAppInfos[i][j].getIcon().setAlpha(255);
                            this.mAppInfos[i][j].getIcon().draw(canvas);
                            drawText(canvas, this.mAppInfos[i][j].getText(), i, j);
                        }
                    }
                }
            }
        }
    }

    public void setMoreIconIndex(int index) {
        this.mMoreIconIndex = index;
    }

    public void startExpandAnimation() {
        int i;
        this.mNeedExpandAnim = true;
        for (int i2 = 0; i2 < this.mRowCounts; i2++) {
            int j = 0;
            while (true) {
                ColorItem[][] colorItemArr = this.mAppInfos;
                if (j >= colorItemArr[i2].length) {
                    break;
                }
                int position = (this.mColumnCounts * i2) + j;
                this.mCanDraw[i2][j] = true;
                if (colorItemArr[i2][j] != null && position >= (i = this.mMoreIconIndex)) {
                    ValueAnimator opacityAnimation = getAlphaAnim(i2, j, position - i);
                    ValueAnimator scaleAnimation = getScaleAnim(i2, j, position - this.mMoreIconIndex);
                    ValueAnimator moreIconAnim = getMoreIconAnim();
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(opacityAnimation, scaleAnimation, moreIconAnim);
                    animatorSet.start();
                }
                j++;
            }
        }
    }

    private ValueAnimator getAlphaAnim(final int i, final int j, int position) {
        ValueAnimator opacityAnimation = ValueAnimator.ofInt(0, 255);
        this.mCanDraw[i][j] = false;
        opacityAnimation.addListener(new AnimatorListenerAdapter() {
            /* class com.color.widget.ColorGridView.AnonymousClass1 */

            @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
            public void onAnimationStart(Animator animation) {
                ColorGridView.this.mCanDraw[i][j] = true;
            }
        });
        opacityAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.color.widget.ColorGridView.AnonymousClass2 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animatior) {
                ColorGridView.this.mOpacity[i][j] = ((Integer) animatior.getAnimatedValue()).intValue();
                ColorGridView.this.invalidate();
            }
        });
        opacityAnimation.setDuration(150L);
        opacityAnimation.setInterpolator(new LinearInterpolator());
        opacityAnimation.setStartDelay((long) ((int) (((((double) position) - ((Math.floor((double) (position + 1)) * 2.0d) / 4.0d)) * 30.0d) + 100.0d)));
        return opacityAnimation;
    }

    private ValueAnimator getScaleAnim(final int i, final int j, int position) {
        ValueAnimator scaleAnimation = ValueAnimator.ofFloat(0.0f, 1.0f);
        scaleAnimation.addListener(new AnimatorListenerAdapter() {
            /* class com.color.widget.ColorGridView.AnonymousClass3 */

            @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
            public void onAnimationStart(Animator animation) {
                ColorGridView.this.mCanDraw[i][j] = true;
            }
        });
        scaleAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.color.widget.ColorGridView.AnonymousClass4 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animatior) {
                ColorGridView.this.mScale[i][j] = (Float) animatior.getAnimatedValue();
                ColorGridView.this.invalidate();
            }
        });
        scaleAnimation.setDuration(300L);
        scaleAnimation.setInterpolator(POLATOR);
        int delay = (int) (((((double) position) - ((Math.floor((double) (position + 1)) * 2.0d) / 4.0d)) * 30.0d) + 100.0d);
        scaleAnimation.setStartDelay((long) delay);
        Log.d(TAG, "getScaleAnim : " + i + ", " + j + ", position : " + position + ", delay : " + delay);
        return scaleAnimation;
    }

    private ValueAnimator getMoreIconAnim() {
        ValueAnimator animation = ValueAnimator.ofInt(255, 0);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.color.widget.ColorGridView.AnonymousClass5 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animatior) {
                int unused = ColorGridView.this.mMoreIconAlpha = ((Integer) animatior.getAnimatedValue()).intValue();
                ColorGridView.this.invalidate();
            }
        });
        animation.setDuration(150L);
        animation.setInterpolator(new LinearInterpolator());
        return animation;
    }

    private void drawText(Canvas canvas, String str, int i, int j) {
        String stringLine2New;
        if (isChinese(str)) {
            int drawLineWidth = this.mChineseLength;
            if (needFullSpaceForChinese(str)) {
                drawLineWidth = this.mEnglishLength;
            } else {
                handleTooLongAppNameStr(str, true, (float) this.mAppNameSize);
            }
            String string = this.mAppInfos[i][j].getText();
            int breakIndex = this.mPaint1.breakText(string, true, (float) drawLineWidth, null);
            if (breakIndex != string.length()) {
                if (getLayoutDirection() == 1) {
                    canvas.drawText(str.substring(0, breakIndex), (((float) getWidth()) - (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth))) - ((float) this.mTextPaddingLeft), (float) (((i + 1) * this.mItemHeight) - this.mTextPaddingBottom), this.mPaint1);
                } else {
                    canvas.drawText(str.substring(0, breakIndex), (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth)) + ((float) this.mTextPaddingLeft), (float) (((i + 1) * this.mItemHeight) - this.mTextPaddingBottom), this.mPaint1);
                }
                String stringLine2Old = str.substring(breakIndex);
                int breakIndex2 = this.mPaint1.breakText(stringLine2Old, true, (float) drawLineWidth, null);
                Paint.FontMetricsInt fmi = this.mPaint1.getFontMetricsInt();
                int textLineHeight = fmi.descent - fmi.ascent;
                if (breakIndex2 == stringLine2Old.length()) {
                    stringLine2New = stringLine2Old;
                } else {
                    stringLine2New = stringLine2Old.substring(0, breakIndex2) + Session.TRUNCATE_STRING;
                }
                if (getLayoutDirection() == 1) {
                    canvas.drawText(stringLine2New, (((float) getWidth()) - (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth))) - ((float) this.mTextPaddingLeft), (float) ((((i + 1) * this.mItemHeight) - this.mTextPaddingBottom) + textLineHeight), this.mPaint1);
                } else {
                    canvas.drawText(stringLine2New, (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth)) + ((float) this.mTextPaddingLeft), (float) ((((i + 1) * this.mItemHeight) - this.mTextPaddingBottom) + textLineHeight), this.mPaint1);
                }
            } else if (getLayoutDirection() == 1) {
                canvas.drawText(str, (((float) getWidth()) - (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth))) - ((float) this.mTextPaddingLeft), (float) (((i + 1) * this.mItemHeight) - this.mTextPaddingBottom), this.mPaint1);
            } else {
                canvas.drawText(str, (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth)) + ((float) this.mTextPaddingLeft), (float) (((i + 1) * this.mItemHeight) - this.mTextPaddingBottom), this.mPaint1);
            }
        } else {
            handleTooLongAppNameStr(str, false, (float) this.mAppNameSize);
            drawTextExp(canvas, str, i, j);
        }
    }

    private void drawTextExp(Canvas canvas, String str, int i, int j) {
        String line2;
        int breakIndex1 = this.mPaint1.breakText(str, true, (float) this.mEnglishLength, null);
        if (breakIndex1 != str.length()) {
            String line1 = str.substring(0, breakIndex1);
            int index = line1.lastIndexOf(32);
            Paint.FontMetricsInt fmi = this.mPaint1.getFontMetricsInt();
            int textLineHeight = fmi.descent - fmi.ascent;
            if (index > 0) {
                line1 = str.substring(0, index);
                String line22 = str.substring(index);
                int breakIndex2 = this.mPaint1.breakText(line22, true, (float) this.mEnglishLength, null);
                if (breakIndex2 == line22.length()) {
                    line2 = line22;
                } else {
                    line2 = line22.substring(0, breakIndex2) + Session.TRUNCATE_STRING;
                }
            } else {
                String line23 = str.substring(breakIndex1);
                int breakIndex22 = this.mPaint1.breakText(line23, true, (float) this.mEnglishLength, null);
                if (breakIndex22 == line23.length()) {
                    line2 = line23;
                } else {
                    line2 = line23.substring(0, breakIndex22) + Session.TRUNCATE_STRING;
                }
            }
            if (getLayoutDirection() == 1) {
                canvas.drawText(line1, (((float) getWidth()) - (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth))) - ((float) this.mTextPaddingLeft), (float) (((i + 1) * this.mItemHeight) - this.mTextPaddingBottom), this.mPaint1);
                canvas.drawText(line2, (((float) getWidth()) - (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth))) - ((float) this.mTextPaddingLeft), (float) ((((i + 1) * this.mItemHeight) - this.mTextPaddingBottom) + textLineHeight), this.mPaint1);
                return;
            }
            canvas.drawText(line1, (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth)) + ((float) this.mTextPaddingLeft), (float) (((i + 1) * this.mItemHeight) - this.mTextPaddingBottom), this.mPaint1);
            canvas.drawText(line2, (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth)) + ((float) this.mTextPaddingLeft), (float) ((((i + 1) * this.mItemHeight) - this.mTextPaddingBottom) + textLineHeight), this.mPaint1);
        } else if (getLayoutDirection() == 1) {
            canvas.drawText(str, (((float) getWidth()) - (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth))) - ((float) this.mTextPaddingLeft), (float) (((i + 1) * this.mItemHeight) - this.mTextPaddingBottom), this.mPaint1);
        } else {
            canvas.drawText(str, (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth)) + ((float) this.mTextPaddingLeft), (float) (((i + 1) * this.mItemHeight) - this.mTextPaddingBottom), this.mPaint1);
        }
    }

    private boolean needFullSpaceForChinese(String str) {
        this.mPaint1.setTextSize((float) this.mAppNameSize);
        int firstLinebreakIndex = this.mPaint1.breakText(str, true, (float) this.mChineseLength, null);
        if (firstLinebreakIndex >= str.length()) {
            return false;
        }
        String stringLine2Old = str.substring(firstLinebreakIndex);
        if (this.mPaint1.breakText(stringLine2Old, true, (float) this.mChineseLength, null) >= stringLine2Old.length()) {
            return false;
        }
        String stringLine2Old2 = str.substring(this.mPaint1.breakText(str, true, (float) this.mEnglishLength, null));
        if (this.mPaint1.breakText(stringLine2Old2, true, (float) this.mEnglishLength, null) == stringLine2Old2.length()) {
            return true;
        }
        return false;
    }

    private void handleTooLongAppNameStr(String str, boolean isChinese, float fontSize) {
        String line2;
        int breakIndex2;
        this.mPaint1.setTextSize(fontSize);
        if (((double) Math.abs(fontSize - (((float) this.mAppNameSize) * APPNAME_TEXT_SECOND_SCALE_MULTIPLIER))) >= 0.01d) {
            int breakIndex1 = this.mPaint1.breakText(str, true, (float) (isChinese ? this.mChineseLength : this.mEnglishLength), null);
            if (breakIndex1 < str.length()) {
                if (!isChinese) {
                    int index = str.substring(0, breakIndex1).lastIndexOf(32);
                    if (index > 0) {
                        line2 = str.substring(index);
                        breakIndex2 = this.mPaint1.breakText(line2, true, (float) this.mEnglishLength, null);
                    } else {
                        line2 = str.substring(breakIndex1);
                        breakIndex2 = this.mPaint1.breakText(line2, true, (float) this.mEnglishLength, null);
                    }
                } else {
                    line2 = str.substring(breakIndex1);
                    breakIndex2 = this.mPaint1.breakText(line2, true, (float) this.mChineseLength, null);
                }
                if (breakIndex2 < line2.length()) {
                    handleTooLongAppNameStr(str, isChinese, APPNAME_TEXT_FIRST_SCALEMULTIPLIER * fontSize);
                }
            }
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return true;
        }
        event.getPointerCount();
        this.mGestureDetector.onTouchEvent(event);
        int action = event.getAction();
        if (action == 0) {
            this.mPrivateFlags |= 33554432;
            performTouchStartAnim();
            return true;
        } else if (action == 1) {
            if ((this.mPrivateFlags & 33554432) == 0) {
                this.isSelected = true;
                invalidate(this.selRect);
            } else {
                this.isSelected = false;
                invalidate(this.selRect);
            }
            postDelayed(this.mCancelclickRunnable, (long) ViewConfiguration.getTapTimeout());
            this.selRect = new Rect();
            performTouchEndAnim();
            return true;
        } else if (action != 3) {
            return true;
        } else {
            this.isSelected = false;
            invalidate(this.selRect);
            this.mPrivateFlags |= -33554433;
            removeCallbacks(this.mOnclickRunnable);
            this.selRect = new Rect();
            performTouchEndAnim();
            return true;
        }
    }

    public void initGetureDetecor() {
        this.mGestureDetector = new GestureDetector(this.mContext, new GestureDetector.OnGestureListener() {
            /* class com.color.widget.ColorGridView.AnonymousClass8 */
            float downX;
            float downY;
            int position = -1;

            @Override // android.view.GestureDetector.OnGestureListener
            public boolean onDown(MotionEvent e) {
                this.downX = e.getX();
                this.downY = e.getY();
                if (ColorGridView.this.isLayoutRtl()) {
                    Log.d("View", "GestureDetector --> ondown getwidth = " + ColorGridView.this.getWidth() + " --> downX = " + this.downX);
                    this.position = (((int) (this.downY / ((float) ColorGridView.this.mItemHeight))) * ColorGridView.this.mColumnCounts) + ((int) ((((float) ColorGridView.this.getWidth()) - this.downX) / ((float) ColorGridView.this.mItemWidth)));
                    ColorGridView colorGridView = ColorGridView.this;
                    colorGridView.select((int) ((((float) colorGridView.getWidth()) - this.downX) / ((float) ColorGridView.this.mItemWidth)), (int) (this.downY / ((float) ColorGridView.this.mItemHeight)), ColorGridView.this.selRect);
                    if (ColorGridView.this.selRect.contains((int) this.downX, (int) this.downY)) {
                        boolean unused = ColorGridView.this.isSelected = true;
                    }
                } else {
                    ColorGridView colorGridView2 = ColorGridView.this;
                    colorGridView2.select((int) (this.downX / ((float) colorGridView2.mItemWidth)), (int) (this.downY / ((float) ColorGridView.this.mItemHeight)), ColorGridView.this.selRect);
                    Configuration cfg = ColorGridView.this.mContext.getResources().getConfiguration();
                    boolean unused2 = ColorGridView.this.isSelected = false;
                    if (cfg.orientation == 2 && ColorGridView.this.selRect.contains((int) this.downX, (int) this.downY)) {
                        boolean unused3 = ColorGridView.this.isSelected = true;
                        this.position = (((int) (this.downY / ((float) ColorGridView.this.mItemHeight))) * ColorGridView.this.mColumnCounts) + ((int) (this.downX / ((float) ColorGridView.this.mItemWidth)));
                    } else if (cfg.orientation == 1) {
                        boolean unused4 = ColorGridView.this.isSelected = true;
                        this.position = (((int) (this.downY / ((float) ColorGridView.this.mItemHeight))) * ColorGridView.this.mColumnCounts) + ((int) (this.downX / ((float) ColorGridView.this.mItemWidth)));
                    }
                }
                int unused5 = ColorGridView.this.mCurrentPosition = this.position;
                ColorGridView colorGridView3 = ColorGridView.this;
                colorGridView3.postDelayed(colorGridView3.mOnclickRunnable, (long) ViewConfiguration.getTapTimeout());
                return true;
            }

            @Override // android.view.GestureDetector.OnGestureListener
            public void onShowPress(MotionEvent e) {
            }

            @Override // android.view.GestureDetector.OnGestureListener
            public boolean onSingleTapUp(MotionEvent e) {
                ColorGridView.this.click(this.position, false);
                return false;
            }

            @Override // android.view.GestureDetector.OnGestureListener
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (ColorGridView.this.isSelected) {
                    boolean unused = ColorGridView.this.isSelected = false;
                    ColorGridView colorGridView = ColorGridView.this;
                    colorGridView.invalidate(colorGridView.selRect);
                }
                return false;
            }

            @Override // android.view.GestureDetector.OnGestureListener
            public void onLongPress(MotionEvent e) {
                ColorGridView.this.click(this.position, true);
            }

            @Override // android.view.GestureDetector.OnGestureListener
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (ColorGridView.this.isSelected) {
                    boolean unused = ColorGridView.this.isSelected = false;
                    ColorGridView colorGridView = ColorGridView.this;
                    colorGridView.invalidate(colorGridView.selRect);
                }
                return false;
            }
        });
    }

    /* access modifiers changed from: private */
    public void click(int position, boolean isLongClick) {
        Log.i(TAG, "Item click :position = " + position + "; isLongClick = " + isLongClick);
        if (position < this.mItemCounts && position >= 0) {
            int i = this.mPagerSize;
            int i2 = this.mPageNumber;
            if (((i2 - 1) * i) + position < 0) {
                return;
            }
            if (!isLongClick) {
                this.mOnItemClickListener.OnItemClick((i * (i2 - 1)) + position);
                this.mTouchHelper.sendEventForVirtualView(position, 1);
                return;
            }
            this.mOnItemClickListener.OnItemLongClick((i * (i2 - 1)) + position);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureHeight(int measureSpec) {
        int result = this.mTotalHeight;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        View.MeasureSpec.getSize(measureSpec);
        if (specMode == 1073741824) {
            return this.mTotalHeight;
        }
        if (specMode == Integer.MIN_VALUE) {
            return this.mTotalHeight;
        }
        return result;
    }

    private int measureWidth(int measureSpec) {
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        if (specMode == 1073741824) {
            return specSize;
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public void getRect(int x, int y, Rect rect) {
        int left;
        if (isLayoutRtl()) {
            left = getWidth() - (Math.min(x + 1, this.mColumnCounts) * this.mItemWidth);
        } else {
            left = this.mItemWidth * x;
        }
        int top = (this.mItemHeight * y) + this.mPaddingTop;
        rect.set(left, top, this.mItemWidth + left, this.mSelectHeight + top);
    }

    private void getRect2(int x, int y, Rect rect, int position) {
        int bottom;
        int left;
        int right;
        int top;
        if (position == this.mCurrentPosition) {
            int i = this.mItemWidth;
            left = (x * i) + ((int) ((((float) (i - this.mCurrentIconWidth)) * 1.0f) / 2.0f));
            if (isLayoutRtl()) {
                if (this.mIsLandscape) {
                    int width = getWidth();
                    int min = Math.min(x + 1, this.mColumnCounts);
                    int i2 = this.mItemWidth;
                    left = (width - (min * i2)) + ((i2 - this.mPaddingLeft) - this.mCurrentIconWidth);
                } else {
                    left = (getWidth() - (Math.min(x + 1, this.mColumnCounts) * this.mItemWidth)) + this.mPaddingLeft;
                }
            }
            int i3 = (this.mItemHeight * y) + this.mAppIconMarginTop;
            int i4 = this.mCurrentIconWidth;
            top = i3 - ((int) ((((float) (i4 - this.mIconWidth)) * 1.0f) / 2.0f));
            right = left + i4;
            bottom = i4 + top;
        } else {
            int top2 = this.mItemWidth;
            int left2 = (x * top2) + ((int) ((((float) (top2 - this.mIconWidth)) * 1.0f) / 2.0f));
            if (isLayoutRtl()) {
                if (this.mIsLandscape) {
                    int width2 = getWidth();
                    int min2 = Math.min(x + 1, this.mColumnCounts);
                    int i5 = this.mItemWidth;
                    left2 = (width2 - (min2 * i5)) + ((i5 - this.mPaddingLeft) - this.mIconWidth);
                } else {
                    left2 = (getWidth() - (Math.min(x + 1, this.mColumnCounts) * this.mItemWidth)) + this.mPaddingLeft;
                }
            }
            top = (this.mItemHeight * y) + this.mAppIconMarginTop;
            int i6 = this.mIconWidth;
            bottom = top + i6;
            right = left + i6;
        }
        rect.set(left, top, right, bottom);
    }

    /* access modifiers changed from: private */
    public void select(int x, int y, Rect rect) {
        this.selectX = Math.min(x, this.mColumnCounts - 1);
        this.selectY = Math.min(y, this.mRowCounts - 1);
        int i = this.selectY;
        int i2 = this.selectX;
        if ((this.mColumnCounts * i) + i2 < this.mItemCounts) {
            getRect(i2, i, rect);
        }
    }

    public void setPageCount(int pagecount) {
        this.mPageNumber = pagecount;
    }

    public void setPagerSize(int size) {
        this.mPagerSize = size;
    }

    public void setOnItemClickListener(OnItemClickListener e) {
        this.mOnItemClickListener = e;
    }

    public void setAppInfo(ColorItem[][] AppInfos) {
        this.mAppInfos = AppInfos;
        ColorItem[][] colorItemArr = this.mAppInfos;
        this.mRowCounts = colorItemArr.length;
        this.mItemCounts = get2DimenArrayCounts(colorItemArr);
        this.mTotalHeight = (this.mItemHeight * this.mRowCounts) + this.mPaddingTop;
        this.mMoreIcon.setAlpha(255);
        this.mNeedExpandAnim = false;
        this.mTouchHelper.invalidateRoot();
        Log.d(TAG, "mTotalHeight = " + this.mTotalHeight);
    }

    public ColorItem[][] getAppInfo() {
        return this.mAppInfos;
    }

    public static String trancateText(String msg, int maxWordsPerLine) {
        String[] lines = msg.split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line.length() > maxWordsPerLine) {
                sb.append(line.subSequence(0, maxWordsPerLine - 1));
                sb.append("...\n");
            } else {
                sb.append(line);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static int get2DimenArrayCounts(ColorItem[][] AppInfos) {
        int counts = 0;
        for (int i = 0; i < AppInfos.length; i++) {
            for (int j = 0; j < AppInfos[i].length; j++) {
                if (AppInfos[i][j] != null) {
                    counts++;
                }
            }
        }
        return counts;
    }

    public static int getNum(String text) {
        int result = 0;
        int english = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.toString(text.charAt(i)).matches("^[一-龥]{1}$")) {
                result++;
            }
            char c = text.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                english++;
            }
        }
        return (int) (((double) result) + Math.ceil((double) (english / 2)));
    }

    public static boolean isChinese(String text) {
        int chinese = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.toString(text.charAt(i)).matches("^[一-龥]{1}$")) {
                chinese++;
            }
        }
        if (chinese > 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mTouchHelper.dispatchHoverEvent(event)) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }

    /* access modifiers changed from: package-private */
    public ColorItem getAppinfo(int position) {
        ColorItem[][] colorItemArr = this.mAppInfos;
        if (colorItemArr == null || position <= -1 || position >= get2DimenArrayCounts(colorItemArr)) {
            return null;
        }
        ColorItem[][] colorItemArr2 = this.mAppInfos;
        return colorItemArr2[position / colorItemArr2[0].length][position % colorItemArr2[0].length];
    }

    /* access modifiers changed from: package-private */
    public ColorItem getAccessibilityFocus() {
        int position = this.mTouchHelper.getFocusedVirtualView();
        if (position >= 0) {
            return getAppinfo(position);
        }
        return null;
    }

    @Override // android.view.View
    public void clearAccessibilityFocus() {
        ColorViewTouchHelper colorViewTouchHelper = this.mTouchHelper;
        if (colorViewTouchHelper != null) {
            colorViewTouchHelper.clearFocusedVirtualView();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean restoreAccessibilityFocus(int position) {
        if (position < 0 || position >= this.mItemCounts) {
            return false;
        }
        ColorViewTouchHelper colorViewTouchHelper = this.mTouchHelper;
        if (colorViewTouchHelper == null) {
            return true;
        }
        colorViewTouchHelper.setFocusedVirtualView(position);
        return true;
    }

    /* access modifiers changed from: private */
    public CharSequence getItemDescription(int virtualViewId) {
        ColorItem drawItem;
        if (virtualViewId >= this.mItemCounts || (drawItem = getAppinfo(virtualViewId)) == null || drawItem.getText() == null) {
            return getClass().getSimpleName();
        }
        return drawItem.getText();
    }

    private class ColorViewTouchHelper extends ExploreByTouchHelper {
        private final Rect mTempRect = new Rect();

        public ColorViewTouchHelper(View host) {
            super(host);
        }

        public void setFocusedVirtualView(int virtualViewId) {
            getAccessibilityNodeProvider(ColorGridView.this).performAction(virtualViewId, 64, null);
        }

        public void clearFocusedVirtualView() {
            int focusedVirtualView = getFocusedVirtualView();
            if (focusedVirtualView != Integer.MIN_VALUE) {
                getAccessibilityNodeProvider(ColorGridView.this).performAction(focusedVirtualView, 128, null);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.widget.ExploreByTouchHelper
        public int getVirtualViewAt(float x, float y) {
            int position;
            Log.d("View", "getVirtualViewAt --> ondown getwidth = " + ColorGridView.this.getWidth() + " --> downX = " + x);
            if (ColorGridView.this.isLayoutRtl()) {
                position = (((int) (y / ((float) ColorGridView.this.mItemHeight))) * ColorGridView.this.mColumnCounts) + ((int) ((((float) ColorGridView.this.getWidth()) - x) / ((float) ColorGridView.this.mItemWidth)));
            } else {
                position = (((int) (y / ((float) ColorGridView.this.mItemHeight))) * ColorGridView.this.mColumnCounts) + ((int) (x / ((float) ColorGridView.this.mItemWidth)));
            }
            if (position >= 0) {
                return position;
            }
            return Integer.MIN_VALUE;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.widget.ExploreByTouchHelper
        public void getVisibleVirtualViews(IntArray virtualViewIds) {
            for (int day = 0; day < ColorGridView.this.mItemCounts; day++) {
                virtualViewIds.add(day);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.widget.ExploreByTouchHelper
        public void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.setContentDescription(ColorGridView.this.getItemDescription(virtualViewId));
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.widget.ExploreByTouchHelper
        public void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            getItemBounds(virtualViewId, this.mTempRect);
            node.setContentDescription(ColorGridView.this.getItemDescription(virtualViewId));
            node.setBoundsInParent(this.mTempRect);
            node.addAction(16);
            if (virtualViewId == ColorGridView.this.mCurrentPosition) {
                node.setSelected(true);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.widget.ExploreByTouchHelper
        public boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            if (action != 16) {
                return false;
            }
            ColorGridView.this.click(virtualViewId, false);
            return true;
        }

        private void getItemBounds(int position, Rect rect) {
            if (position >= 0 && position < ColorGridView.this.mItemCounts) {
                ColorGridView colorGridView = ColorGridView.this;
                colorGridView.getRect(position % colorGridView.mAppInfos[0].length, position / ColorGridView.this.mAppInfos[0].length, rect);
            }
        }
    }

    private int dip2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private void performTouchStartAnim() {
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofInt("widthHolder", this.mCurrentIconWidth, this.mTouchModeWidth), PropertyValuesHolder.ofFloat("brightnessHolder", this.mCurrentBrightness, 1.09f));
        animator.setInterpolator(this.mTouchStartInterpolator);
        animator.setDuration(66L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.color.widget.ColorGridView.AnonymousClass9 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                int unused = ColorGridView.this.mCurrentIconWidth = ((Integer) animation.getAnimatedValue("widthHolder")).intValue();
                float unused2 = ColorGridView.this.mCurrentBrightness = ((Float) animation.getAnimatedValue("brightnessHolder")).floatValue();
                ColorGridView.this.invalidate();
            }
        });
        animator.start();
    }

    private void performTouchEndAnim() {
        Log.i(TAG, "Item touched end,performTouchEndAnim.");
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofInt("widthHolder", this.mCurrentIconWidth, this.mIconWidth), PropertyValuesHolder.ofFloat("brightnessHolder", this.mCurrentBrightness, 1.0f));
        animator.setInterpolator(this.mTouchEndInterpolator);
        animator.setDuration(300L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.color.widget.ColorGridView.AnonymousClass10 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                int unused = ColorGridView.this.mCurrentIconWidth = ((Integer) animation.getAnimatedValue("widthHolder")).intValue();
                float unused2 = ColorGridView.this.mCurrentBrightness = ((Float) animation.getAnimatedValue("brightnessHolder")).floatValue();
                ColorGridView.this.invalidate();
            }
        });
        animator.start();
    }
}
