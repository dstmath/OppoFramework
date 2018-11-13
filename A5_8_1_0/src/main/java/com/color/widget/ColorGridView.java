package com.color.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.IntArray;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
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
    private int dotViewHeight;
    private boolean isSelected;
    private Integer[][] mAppIcons;
    private ColorItem[][] mAppInfos;
    private int mAppNameSize;
    private String[][] mAppNames;
    private boolean[][] mCanDraw;
    private Runnable mCancelclickRunnable;
    private int mChineseLength;
    private int mColumnCounts;
    private Context mContext;
    private int mCurrentPosition;
    private float mDownX;
    private float mDownY;
    private int mEnglishLength;
    private GestureDetector mGestureDetector;
    private int mIconDistance;
    private int mIconHeight;
    private int mIconWidth;
    private boolean mIsLandscape;
    private int mItemBgPaddingRight;
    private int mItemCounts;
    private int mItemHeight;
    private int mItemWidth;
    private Drawable mMoreIcon;
    private int mMoreIconAlpha;
    private int mMoreIconIndex;
    private int mNavBarHeight;
    private boolean mNeedExpandAnim;
    private OnItemClickListener mOnItemClickListener;
    private Runnable mOnclickRunnable;
    private int[][] mOpacity;
    private int mPaddingBottom;
    private int mPaddingLeft;
    private int mPaddingTop;
    public int mPageNumber;
    private int mPagerSize;
    private TextPaint mPaint1;
    private Paint mPaint2;
    private int mPrivateFlags;
    private List<ResolveInfo> mResolveInfoList;
    private int mRowCounts;
    private Float[][] mScale;
    private int mSelectColor;
    private int mSelectHeight;
    private int mSelectWidth;
    private int mTextColor;
    private int mTextPaddingBottom;
    private int mTextPaddingLeft;
    private int mTotalHeight;
    private final ColorViewTouchHelper mTouchHelper;
    private int mTwoLineDistance;
    private Rect selRect;
    private Rect selRect2;
    private int selectX;
    private int selectY;

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

        protected int getVirtualViewAt(float x, float y) {
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

        protected void getVisibleVirtualViews(IntArray virtualViewIds) {
            for (int day = 0; day < ColorGridView.this.mItemCounts; day++) {
                virtualViewIds.add(day);
            }
        }

        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.setContentDescription(ColorGridView.this.getItemDescription(virtualViewId));
        }

        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            getItemBounds(virtualViewId, this.mTempRect);
            node.setContentDescription(ColorGridView.this.getItemDescription(virtualViewId));
            node.setBoundsInParent(this.mTempRect);
            node.addAction(16);
            if (virtualViewId == ColorGridView.this.mCurrentPosition) {
                node.setSelected(true);
            }
        }

        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            switch (action) {
                case 16:
                    ColorGridView.this.click(virtualViewId, false);
                    return true;
                default:
                    return false;
            }
        }

        private void getItemBounds(int position, Rect rect) {
            if (position >= 0 && position < ColorGridView.this.mItemCounts) {
                ColorGridView.this.getRect(position % ColorGridView.this.mAppInfos[0].length, position / ColorGridView.this.mAppInfos[0].length, rect);
            }
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(int i);

        void OnItemLongClick(int i);
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
        this.mResolveInfoList = new ArrayList();
        this.mAppInfos = null;
        r4 = new Float[2][];
        r4[0] = new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)};
        r4[1] = new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)};
        this.mScale = r4;
        this.mOpacity = new int[][]{new int[]{255, 255, 255, 255}, new int[]{255, 255, 255, 255}};
        this.mMoreIconAlpha = 255;
        this.mCanDraw = (boolean[][]) Array.newInstance(Boolean.TYPE, new int[]{2, 4});
        this.mMoreIconIndex = 1;
        this.mOnclickRunnable = new Runnable() {
            public void run() {
                ColorGridView colorGridView = ColorGridView.this;
                colorGridView.mPrivateFlags = colorGridView.mPrivateFlags | -33554433;
                ColorGridView.this.invalidate(ColorGridView.this.selRect);
            }
        };
        this.mCancelclickRunnable = new Runnable() {
            public void run() {
                ColorGridView colorGridView = ColorGridView.this;
                colorGridView.mPrivateFlags = colorGridView.mPrivateFlags | -33554433;
                ColorGridView.this.removeCallbacks(ColorGridView.this.mOnclickRunnable);
                ColorGridView.this.isSelected = false;
                ColorGridView.this.invalidate(ColorGridView.this.selRect);
            }
        };
        this.mCurrentPosition = -1;
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.mContext = context;
        Configuration cfg = this.mContext.getResources().getConfiguration();
        initGetureDetecor();
        this.mSelectColor = getResources().getColor(201720851);
        this.mTextColor = getResources().getColor(201720854);
        int textSize = getResources().getDimensionPixelSize(201654407);
        this.mAppNameSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) textSize, this.mContext.getResources().getConfiguration().fontScale, 2);
        this.dotViewHeight = (int) getResources().getDimension(201655408);
        this.mItemHeight = (int) getResources().getDimension(201655410);
        this.mSelectHeight = (int) getResources().getDimension(201655399);
        this.mSelectWidth = (int) getResources().getDimension(201655400);
        this.mPaddingLeft = (int) getResources().getDimension(201655411);
        this.mPaddingTop = (int) getResources().getDimension(201655412);
        this.mIconHeight = getResources().getDimensionPixelSize(201655413);
        this.mIconWidth = getResources().getDimensionPixelSize(201655414);
        this.mItemBgPaddingRight = getResources().getDimensionPixelSize(201655465);
        this.mTextPaddingBottom = getResources().getDimensionPixelSize(201655466);
        this.mTwoLineDistance = getResources().getDimensionPixelSize(201655467);
        this.mChineseLength = getResources().getDimensionPixelSize(201655494);
        this.mEnglishLength = getResources().getDimensionPixelSize(201655495);
        this.mMoreIcon = this.mContext.getDrawable(201852207);
        this.mTouchHelper = new ColorViewTouchHelper(this);
        setAccessibilityDelegate(this.mTouchHelper);
        setImportantForAccessibility(1);
    }

    protected void onDraw(Canvas canvas) {
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
            if ((this.mContext instanceof Activity) && ((Activity) this.mContext).isInMultiWindowMode()) {
                this.mRowCounts = 1;
                this.mIsLandscape = true;
            }
            this.mItemWidth = getWidth() / this.mColumnCounts;
            this.mTextPaddingLeft = 0;
            this.mIconDistance = (getWidth() / this.mColumnCounts) - this.mIconWidth;
            this.mPaddingBottom = Math.abs(this.mItemHeight - this.mItemWidth);
            this.mPaint1 = new TextPaint();
            this.mPaint1.setColor(this.mTextColor);
            this.mPaint1.setTextSize((float) this.mAppNameSize);
            this.mPaint1.setTextAlign(Align.CENTER);
            this.mPaint1.setAntiAlias(true);
            this.mPaint1.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
            this.mPaint2 = new Paint();
            this.mPaint2.setColor(this.mSelectColor);
            if (this.isSelected) {
                canvas.drawRect(this.selRect, this.mPaint2);
            }
            for (int i = 0; i < this.mRowCounts; i++) {
                for (int j = 0; j < this.mAppInfos[i].length; j++) {
                    getRect2(j, i, this.selRect2);
                    int position = (this.mColumnCounts * i) + j;
                    if (this.mAppInfos[i][j] != null) {
                        this.mAppInfos[i][j].getIcon().setBounds(this.selRect2);
                        if (this.mNeedExpandAnim) {
                            if (position == this.mMoreIconIndex) {
                                this.mMoreIcon.setBounds(this.selRect2);
                                this.mMoreIcon.setAlpha(this.mMoreIconAlpha);
                                this.mMoreIcon.draw(canvas);
                                Log.d(TAG, "moreIcon = " + position + ", alpha = " + this.mMoreIconAlpha);
                            }
                            if (this.mCanDraw[i][j]) {
                                canvas.save();
                                float scale = this.mScale[i][j].floatValue();
                                canvas.scale(scale, scale, this.selRect2.exactCenterX(), this.selRect2.exactCenterY());
                                this.mAppInfos[i][j].getIcon().setAlpha(this.mOpacity[i][j]);
                                this.mAppInfos[i][j].getIcon().draw(canvas);
                                canvas.restore();
                                this.mPaint1.setAlpha(this.mOpacity[i][j]);
                                drawText(canvas, this.mAppInfos[i][j].getText(), i, j);
                            }
                        } else {
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
        this.mNeedExpandAnim = true;
        for (int i = 0; i < this.mRowCounts; i++) {
            for (int j = 0; j < this.mAppInfos[i].length; j++) {
                int position = (this.mColumnCounts * i) + j;
                this.mCanDraw[i][j] = true;
                if (this.mAppInfos[i][j] != null && position >= this.mMoreIconIndex) {
                    ValueAnimator opacityAnimation = getAlphaAnim(i, j, position - this.mMoreIconIndex);
                    ValueAnimator scaleAnimation = getScaleAnim(i, j, position - this.mMoreIconIndex);
                    ValueAnimator moreIconAnim = getMoreIconAnim();
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(new Animator[]{opacityAnimation, scaleAnimation, moreIconAnim});
                    animatorSet.start();
                }
            }
        }
    }

    private ValueAnimator getAlphaAnim(final int i, final int j, int position) {
        ValueAnimator opacityAnimation = ValueAnimator.ofInt(new int[]{0, 255});
        this.mCanDraw[i][j] = false;
        opacityAnimation.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                ColorGridView.this.mCanDraw[i][j] = true;
            }
        });
        opacityAnimation.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animatior) {
                ColorGridView.this.mOpacity[i][j] = ((Integer) animatior.getAnimatedValue()).intValue();
                ColorGridView.this.invalidate();
            }
        });
        opacityAnimation.setDuration(150);
        opacityAnimation.setInterpolator(new LinearInterpolator());
        opacityAnimation.setStartDelay((long) ((int) (((((double) position) - ((Math.floor((double) (position + 1)) * 2.0d) / 4.0d)) * 30.0d) + 100.0d)));
        return opacityAnimation;
    }

    private ValueAnimator getScaleAnim(final int i, final int j, int position) {
        ValueAnimator scaleAnimation = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        scaleAnimation.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                ColorGridView.this.mCanDraw[i][j] = true;
            }
        });
        scaleAnimation.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animatior) {
                ColorGridView.this.mScale[i][j] = (Float) animatior.getAnimatedValue();
                ColorGridView.this.invalidate();
            }
        });
        scaleAnimation.setDuration(300);
        scaleAnimation.setInterpolator(POLATOR);
        int delay = (int) (((((double) position) - ((Math.floor((double) (position + 1)) * 2.0d) / 4.0d)) * 30.0d) + 100.0d);
        scaleAnimation.setStartDelay((long) delay);
        Log.d(TAG, "getScaleAnim : " + i + ", " + j + ", position : " + position + ", delay : " + delay);
        return scaleAnimation;
    }

    private ValueAnimator getMoreIconAnim() {
        ValueAnimator animation = ValueAnimator.ofInt(new int[]{255, 0});
        animation.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animatior) {
                ColorGridView.this.mMoreIconAlpha = ((Integer) animatior.getAnimatedValue()).intValue();
                ColorGridView.this.invalidate();
            }
        });
        animation.setDuration(150);
        animation.setInterpolator(new LinearInterpolator());
        return animation;
    }

    private void drawText(Canvas canvas, String str, int i, int j) {
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
                String stringLine2New;
                if (getLayoutDirection() == 1) {
                    canvas.drawText(str.substring(0, breakIndex), (((float) getWidth()) - (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth))) - ((float) this.mTextPaddingLeft), (float) (((i + 1) * this.mItemHeight) - this.mTextPaddingBottom), this.mPaint1);
                } else {
                    canvas.drawText(str.substring(0, breakIndex), (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth)) + ((float) this.mTextPaddingLeft), (float) (((i + 1) * this.mItemHeight) - this.mTextPaddingBottom), this.mPaint1);
                }
                String stringLine2Old = str.substring(breakIndex);
                int breakIndex2 = this.mPaint1.breakText(stringLine2Old, true, (float) drawLineWidth, null);
                FontMetricsInt fmi = this.mPaint1.getFontMetricsInt();
                int textLineHeight = fmi.descent - fmi.ascent;
                if (breakIndex2 == stringLine2Old.length()) {
                    stringLine2New = stringLine2Old;
                } else {
                    stringLine2New = stringLine2Old.substring(0, breakIndex2) + "...";
                }
                if (getLayoutDirection() == 1) {
                    canvas.drawText(stringLine2New, (((float) getWidth()) - (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth))) - ((float) this.mTextPaddingLeft), (float) ((((i + 1) * this.mItemHeight) - this.mTextPaddingBottom) + textLineHeight), this.mPaint1);
                    return;
                }
                canvas.drawText(stringLine2New, (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth)) + ((float) this.mTextPaddingLeft), (float) ((((i + 1) * this.mItemHeight) - this.mTextPaddingBottom) + textLineHeight), this.mPaint1);
                return;
            } else if (getLayoutDirection() == 1) {
                canvas.drawText(str, (((float) getWidth()) - (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth))) - ((float) this.mTextPaddingLeft), (float) (((i + 1) * this.mItemHeight) - this.mTextPaddingBottom), this.mPaint1);
                return;
            } else {
                canvas.drawText(str, (((float) (((double) j) + 0.5d)) * ((float) this.mItemWidth)) + ((float) this.mTextPaddingLeft), (float) (((i + 1) * this.mItemHeight) - this.mTextPaddingBottom), this.mPaint1);
                return;
            }
        }
        handleTooLongAppNameStr(str, false, (float) this.mAppNameSize);
        drawTextExp(canvas, str, i, j);
    }

    private void drawTextExp(Canvas canvas, String str, int i, int j) {
        int breakIndex1 = this.mPaint1.breakText(str, true, (float) this.mEnglishLength, null);
        if (breakIndex1 != str.length()) {
            String line2;
            String line1 = str.substring(0, breakIndex1);
            int index = line1.lastIndexOf(32);
            FontMetricsInt fmi = this.mPaint1.getFontMetricsInt();
            int textLineHeight = fmi.descent - fmi.ascent;
            int breakIndex2;
            if (index > 0) {
                line1 = str.substring(0, index);
                line2 = str.substring(index);
                breakIndex2 = this.mPaint1.breakText(line2, true, (float) this.mEnglishLength, null);
                if (breakIndex2 != line2.length()) {
                    line2 = line2.substring(0, breakIndex2) + "...";
                }
            } else {
                line2 = str.substring(breakIndex1);
                breakIndex2 = this.mPaint1.breakText(line2, true, (float) this.mEnglishLength, null);
                if (breakIndex2 != line2.length()) {
                    line2 = line2.substring(0, breakIndex2) + "...";
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
        if (firstLinebreakIndex < str.length()) {
            String stringLine2Old = str.substring(firstLinebreakIndex);
            if (this.mPaint1.breakText(stringLine2Old, true, (float) this.mChineseLength, null) < stringLine2Old.length()) {
                stringLine2Old = str.substring(this.mPaint1.breakText(str, true, (float) this.mEnglishLength, null));
                if (this.mPaint1.breakText(stringLine2Old, true, (float) this.mEnglishLength, null) == stringLine2Old.length()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleTooLongAppNameStr(String str, boolean isChinese, float fontSize) {
        this.mPaint1.setTextSize(fontSize);
        if (((double) Math.abs(fontSize - (((float) this.mAppNameSize) * APPNAME_TEXT_SECOND_SCALE_MULTIPLIER))) >= 0.01d) {
            int breakIndex1 = this.mPaint1.breakText(str, true, (float) (isChinese ? this.mChineseLength : this.mEnglishLength), null);
            if (breakIndex1 < str.length()) {
                String line2;
                int breakIndex2;
                if (isChinese) {
                    line2 = str.substring(breakIndex1);
                    breakIndex2 = this.mPaint1.breakText(line2, true, (float) this.mChineseLength, null);
                } else {
                    int index = str.substring(0, breakIndex1).lastIndexOf(32);
                    if (index > 0) {
                        line2 = str.substring(index);
                        breakIndex2 = this.mPaint1.breakText(line2, true, (float) this.mEnglishLength, null);
                    } else {
                        line2 = str.substring(breakIndex1);
                        breakIndex2 = this.mPaint1.breakText(line2, true, (float) this.mEnglishLength, null);
                    }
                }
                if (breakIndex2 < line2.length()) {
                    handleTooLongAppNameStr(str, isChinese, APPNAME_TEXT_FIRST_SCALEMULTIPLIER * fontSize);
                }
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return true;
        }
        int pointerCount = event.getPointerCount();
        this.mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case 0:
                this.mPrivateFlags |= PFLAG_PREPRESSED;
                return true;
            case 1:
                if ((this.mPrivateFlags & PFLAG_PREPRESSED) == 0) {
                    this.isSelected = true;
                    invalidate(this.selRect);
                } else {
                    this.isSelected = false;
                    invalidate(this.selRect);
                }
                postDelayed(this.mCancelclickRunnable, (long) ViewConfiguration.getTapTimeout());
                this.selRect = new Rect();
                return true;
            case 3:
                this.isSelected = false;
                invalidate(this.selRect);
                this.mPrivateFlags |= -33554433;
                removeCallbacks(this.mOnclickRunnable);
                this.selRect = new Rect();
                return true;
            default:
                return true;
        }
    }

    public void initGetureDetecor() {
        this.mGestureDetector = new GestureDetector(this.mContext, new OnGestureListener() {
            float downX;
            float downY;
            int position = -1;

            public boolean onDown(MotionEvent e) {
                this.downX = e.getX();
                this.downY = e.getY();
                if (ColorGridView.this.isLayoutRtl()) {
                    Log.d("View", "GestureDetector --> ondown getwidth = " + ColorGridView.this.getWidth() + " --> downX = " + this.downX);
                    this.position = (((int) (this.downY / ((float) ColorGridView.this.mItemHeight))) * ColorGridView.this.mColumnCounts) + ((int) ((((float) ColorGridView.this.getWidth()) - this.downX) / ((float) ColorGridView.this.mItemWidth)));
                    ColorGridView.this.select((int) ((((float) ColorGridView.this.getWidth()) - this.downX) / ((float) ColorGridView.this.mItemWidth)), (int) (this.downY / ((float) ColorGridView.this.mItemHeight)), ColorGridView.this.selRect);
                    if (ColorGridView.this.selRect.contains((int) this.downX, (int) this.downY)) {
                        ColorGridView.this.isSelected = true;
                    }
                } else {
                    ColorGridView.this.select((int) (this.downX / ((float) ColorGridView.this.mItemWidth)), (int) (this.downY / ((float) ColorGridView.this.mItemHeight)), ColorGridView.this.selRect);
                    Configuration cfg = ColorGridView.this.mContext.getResources().getConfiguration();
                    ColorGridView.this.isSelected = false;
                    if (cfg.orientation == 2 && ColorGridView.this.selRect.contains((int) this.downX, (int) this.downY)) {
                        ColorGridView.this.isSelected = true;
                        this.position = (((int) (this.downY / ((float) ColorGridView.this.mItemHeight))) * ColorGridView.this.mColumnCounts) + ((int) (this.downX / ((float) ColorGridView.this.mItemWidth)));
                    } else if (cfg.orientation == 1) {
                        ColorGridView.this.isSelected = true;
                        this.position = (((int) (this.downY / ((float) ColorGridView.this.mItemHeight))) * ColorGridView.this.mColumnCounts) + ((int) (this.downX / ((float) ColorGridView.this.mItemWidth)));
                    }
                }
                ColorGridView.this.mCurrentPosition = this.position;
                ColorGridView.this.postDelayed(ColorGridView.this.mOnclickRunnable, (long) ViewConfiguration.getTapTimeout());
                return true;
            }

            public void onShowPress(MotionEvent e) {
            }

            public boolean onSingleTapUp(MotionEvent e) {
                ColorGridView.this.postDelayed(new Runnable() {
                    public void run() {
                        ColorGridView.this.click(AnonymousClass8.this.position, false);
                    }
                }, (long) ViewConfiguration.getTapTimeout());
                return false;
            }

            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (ColorGridView.this.isSelected) {
                    ColorGridView.this.isSelected = false;
                    ColorGridView.this.invalidate(ColorGridView.this.selRect);
                }
                return false;
            }

            public void onLongPress(MotionEvent e) {
                ColorGridView.this.click(this.position, true);
                ColorGridView.this.postDelayed(new Runnable() {
                    public void run() {
                        boolean -get0 = ColorGridView.this.isSelected;
                    }
                }, 100);
            }

            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (ColorGridView.this.isSelected) {
                    ColorGridView.this.isSelected = false;
                    ColorGridView.this.invalidate(ColorGridView.this.selRect);
                }
                return false;
            }
        });
    }

    private void click(int position, boolean isLongClick) {
        if (position < this.mItemCounts && position >= 0 && (this.mPagerSize * (this.mPageNumber - 1)) + position >= 0) {
            if (isLongClick) {
                this.mOnItemClickListener.OnItemLongClick((this.mPagerSize * (this.mPageNumber - 1)) + position);
                return;
            }
            this.mOnItemClickListener.OnItemClick((this.mPagerSize * (this.mPageNumber - 1)) + position);
            this.mTouchHelper.sendEventForVirtualView(position, 1);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureHeight(int measureSpec) {
        int result = this.mTotalHeight;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == 1073741824) {
            return this.mTotalHeight;
        }
        if (specMode == Integer.MIN_VALUE) {
            return this.mTotalHeight;
        }
        return result;
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == 1073741824) {
            return specSize;
        }
        return 0;
    }

    private void getRect(int x, int y, Rect rect) {
        int left;
        if (isLayoutRtl()) {
            left = getWidth() - (Math.min(x + 1, this.mColumnCounts) * this.mItemWidth);
        } else {
            left = x * this.mItemWidth;
        }
        int top = (this.mItemHeight * y) + this.mPaddingTop;
        rect.set(left, top, left + this.mItemWidth, top + this.mSelectHeight);
    }

    private void getRect2(int x, int y, Rect rect) {
        int left = (this.mItemWidth * x) + (this.mIconDistance / 2);
        if (isLayoutRtl()) {
            if (this.mIsLandscape) {
                left = (getWidth() - (Math.min(x + 1, this.mColumnCounts) * this.mItemWidth)) + ((this.mItemWidth - this.mPaddingLeft) - this.mIconWidth);
            } else {
                left = (getWidth() - (Math.min(x + 1, this.mColumnCounts) * this.mItemWidth)) + this.mPaddingLeft;
            }
        }
        int top = (this.mItemHeight * y) + this.mPaddingTop;
        rect.set(left, top, left + this.mIconWidth, top + this.mIconHeight);
    }

    private void select(int x, int y, Rect rect) {
        this.selectX = Math.min(x, this.mColumnCounts - 1);
        this.selectY = Math.min(y, this.mRowCounts - 1);
        if ((this.selectY * this.mColumnCounts) + this.selectX < this.mItemCounts) {
            getRect(this.selectX, this.selectY, rect);
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
        this.mRowCounts = this.mAppInfos.length;
        this.mItemCounts = get2DimenArrayCounts(this.mAppInfos);
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
        String[] lines = msg.split(" ");
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
            for (ColorItem colorItem : AppInfos[i]) {
                if (colorItem != null) {
                    counts++;
                }
            }
        }
        return counts;
    }

    public static int getNum(String text) {
        String Reg = "^[一-龥]{1}$";
        int result = 0;
        int english = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.toString(text.charAt(i)).matches(Reg)) {
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
        String Reg = "^[一-龥]{1}$";
        int chinese = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.toString(text.charAt(i)).matches(Reg)) {
                chinese++;
            }
        }
        if (chinese > 0) {
            return true;
        }
        return false;
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mTouchHelper.dispatchHoverEvent(event)) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }

    ColorItem getAppinfo(int position) {
        if (this.mAppInfos == null || position <= -1 || position >= get2DimenArrayCounts(this.mAppInfos)) {
            return null;
        }
        return this.mAppInfos[position / this.mAppInfos[0].length][position % this.mAppInfos[0].length];
    }

    ColorItem getAccessibilityFocus() {
        int position = this.mTouchHelper.getFocusedVirtualView();
        if (position >= 0) {
            return getAppinfo(position);
        }
        return null;
    }

    public void clearAccessibilityFocus() {
        if (this.mTouchHelper != null) {
            this.mTouchHelper.clearFocusedVirtualView();
        }
    }

    boolean restoreAccessibilityFocus(int position) {
        if (position < 0 || position >= this.mItemCounts) {
            return false;
        }
        if (this.mTouchHelper != null) {
            this.mTouchHelper.setFocusedVirtualView(position);
        }
        return true;
    }

    private CharSequence getItemDescription(int virtualViewId) {
        if (virtualViewId < this.mItemCounts) {
            ColorItem drawItem = getAppinfo(virtualViewId);
            if (!(drawItem == null || drawItem == null || drawItem.getText() == null)) {
                return drawItem.getText();
            }
        }
        return getClass().getSimpleName();
    }
}
