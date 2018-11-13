package com.color.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.IntArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import com.android.internal.widget.ExploreByTouchHelper;
import com.color.screenshot.ColorLongshotUnsupported;
import com.color.util.ColorContextUtil;
import com.color.util.ColorDialogUtil;
import com.color.view.ColorHapticFeedbackConstants;
import java.lang.reflect.Array;
import java.util.Locale;
import oppo.R;

public class ColorNumericKeyboard extends View implements ColorLongshotUnsupported {
    private static final int ANIMTIME = 150;
    public static final int CELL_COLUMN_COUNT = 3;
    public static final int CELL_ROW_COUNT = 4;
    private static final int ELEVEN = 11;
    public static final int EMPTY_NINE_AND_ELEVEN = 1;
    private static final int ENDALPHA = 38;
    private static final int NINE = 9;
    private static final int ONE = 1;
    public static final int RETAIN_ELEVEN = 3;
    public static final int RETAIN_NINE = 2;
    private static final int STARTALPHA = 76;
    private static final String TAG = "ColorNumericKeyboard";
    private static final int TEN = 10;
    private static final int TWO = 2;
    public int NUMERIC;
    public int WORD;
    private Drawable mBgDrawable;
    private int mBgHeight;
    private int mBgWidth;
    private int mCircleDiameter;
    private int mCircleRadius;
    private Context mContext;
    private boolean mDownState;
    private boolean mEnableHapticFeedback;
    private PatternExploreByTouchHelper mExploreByTouchHelper;
    private ValueAnimator mFadeAnimator;
    private int mIndex;
    private boolean mIsCanvasCir;
    private boolean mIsFinishVibility;
    private boolean mIsThailandLanguage;
    private float mKeyboardLetterPlusSize;
    private Drawable mKeyboardLetterStar;
    private int mKeyboardLetterTextColor;
    private float mKeyboardLetterTextSize;
    private Drawable mKeyboardLetterWellNumber;
    private String[] mKeyboardLetters;
    private int mKeyboardNumberTextColor;
    private float mKeyboardNumberTextSize;
    private int[] mKeyboardNumbers;
    FontMetricsInt mLetterTextFontMetrics;
    private TextPaint mLetterTextPaint;
    private int mLevelSpace;
    private float mLineHeight;
    private Paint mLinePaint;
    private float mLineSpace;
    private float mLineWidth;
    private boolean mNeedDoFade;
    private boolean mNeedDoFadeAfterShow;
    FontMetricsInt mNumberTextFontMetrics;
    private TextPaint mNumberTextPaint;
    private OnItemTouchListener mOnItemClickListener;
    private Paint mPaint;
    private int mPressedColor;
    private ValueAnimator mShowAnimator;
    private int mSpecialIndex;
    private String mStarString;
    private String mSure;
    private float mThreeLineTopOffset;
    private Cell mTouchCell;
    private OnTouchTextListener mTouchTextListener;
    private OnTouchUpListener mTouchUpListener;
    private int mType;
    private int mVerticalOffset;
    private int mVerticalSpace;
    private int mViewHeight;
    private int mViewWidth;
    private String mWellString;
    private int mWordTextColor;
    FontMetricsInt mWordTextFontMetrics;
    private int mWordTextNormalColor;
    private TextPaint mWordTextPaint;
    private int mWordTextPressColor;
    private float mWordTextSize;
    Cell[][] sCells;

    public class Cell {
        String cellLettersStr;
        String cellNumberStr;
        int column;
        int row;

        /* synthetic */ Cell(ColorNumericKeyboard this$0, int row, int column, Cell -this3) {
            this(row, column);
        }

        private Cell(int row, int column) {
            this.cellNumberStr = null;
            this.cellLettersStr = null;
            ColorNumericKeyboard.this.checkRange(row, column);
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return this.row;
        }

        public int getColumn() {
            return this.column;
        }

        public String toString() {
            return "row " + this.row + "column " + this.column;
        }
    }

    public interface OnItemTouchListener {
        void OnItemTouch();
    }

    public interface OnTouchTextListener {
        void onTouchText(int i);
    }

    public interface OnTouchUpListener {
        void OnTouchUp();
    }

    private final class PatternExploreByTouchHelper extends ExploreByTouchHelper {
        private Rect mTempRect = new Rect();
        private int mVirtualViewId = -1;

        public PatternExploreByTouchHelper(View forView) {
            super(forView);
        }

        protected int getVirtualViewAt(float x, float y) {
            return getVirtualViewIdForHit(x, y);
        }

        protected void getVisibleVirtualViews(IntArray virtualViewIds) {
            for (int i = 0; i < getItemCounts(); i++) {
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
            node.setClickable(true);
            node.setBoundsInParent(getBoundsForVirtualView(virtualViewId));
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
            invalidateVirtualView(index);
            if (!(ColorNumericKeyboard.this.mIndex == -1 || ColorNumericKeyboard.this.mOnItemClickListener == null)) {
                if (ColorNumericKeyboard.this.isEnabled()) {
                    ColorNumericKeyboard.this.mOnItemClickListener.OnItemTouch();
                }
                ColorNumericKeyboard.this.announceForAccessibility(getItemDescription(index));
            }
            if (!(ColorNumericKeyboard.this.mTouchUpListener == null || ColorNumericKeyboard.this.mIndex == -1)) {
                ColorNumericKeyboard.this.mTouchUpListener.OnTouchUp();
            }
            if (ColorNumericKeyboard.this.mTouchTextListener != null) {
                if (ColorNumericKeyboard.this.mIndex != -1) {
                    if (ColorNumericKeyboard.this.isEnabled()) {
                        ColorNumericKeyboard.this.mTouchTextListener.onTouchText(ColorNumericKeyboard.this.NUMERIC);
                    }
                    ColorNumericKeyboard.this.announceForAccessibility(getItemDescription(index));
                }
                if (ColorNumericKeyboard.this.mSpecialIndex != -1) {
                    if (ColorNumericKeyboard.this.isEnabled()) {
                        ColorNumericKeyboard.this.mTouchTextListener.onTouchText(ColorNumericKeyboard.this.WORD);
                    }
                    ColorNumericKeyboard.this.announceForAccessibility(getItemDescription(index));
                }
            }
            sendEventForVirtualView(index, 1);
            return true;
        }

        private Rect getBoundsForVirtualView(int virtualViewId) {
            Rect bounds = this.mTempRect;
            int startX = 0;
            int startY = 0;
            Cell cell;
            if (ColorNumericKeyboard.this.mType != 1) {
                cell = ColorNumericKeyboard.this.of(virtualViewId / 3, virtualViewId % 3);
                if (cell != null) {
                    startX = (int) ColorNumericKeyboard.this.getCenterXForColumn(cell.column);
                    startY = (int) ColorNumericKeyboard.this.getCenterYForRow(cell.row);
                }
            } else if (virtualViewId == 9) {
                startX = (int) ColorNumericKeyboard.this.getCenterXForColumn(1);
                startY = (int) ColorNumericKeyboard.this.getCenterYForRow(3);
            } else if (virtualViewId == 10) {
                startX = (int) ColorNumericKeyboard.this.getCenterXForColumn(2);
                startY = (int) ColorNumericKeyboard.this.getCenterYForRow(3);
            } else {
                cell = ColorNumericKeyboard.this.of(virtualViewId / 3, virtualViewId % 3);
                if (cell != null) {
                    startX = (int) ColorNumericKeyboard.this.getCenterXForColumn(cell.column);
                    startY = (int) ColorNumericKeyboard.this.getCenterYForRow(cell.row);
                }
            }
            bounds.left = startX - ColorNumericKeyboard.this.mCircleRadius;
            bounds.right = ColorNumericKeyboard.this.mCircleRadius + startX;
            bounds.top = startY - ColorNumericKeyboard.this.mCircleRadius;
            bounds.bottom = ColorNumericKeyboard.this.mCircleRadius + startY;
            return bounds;
        }

        private int getVirtualViewIdForHit(float x, float y) {
            Cell cell = ColorNumericKeyboard.this.detectAndAddHit(x, y);
            int index = -1;
            if (cell != null) {
                index = (cell.row * 3) + cell.column;
            }
            if (ColorNumericKeyboard.this.mType == 1) {
                if (ColorNumericKeyboard.this.mIsFinishVibility) {
                    if (index == 10) {
                        index = 9;
                    } else if (index == 9) {
                        index = -1;
                    } else if (index == ColorNumericKeyboard.ELEVEN) {
                        index = 10;
                    }
                } else if (index == 10) {
                    index = 9;
                } else if (index == 9 || index == ColorNumericKeyboard.ELEVEN) {
                    index = -1;
                }
            }
            this.mVirtualViewId = index;
            return index;
        }

        public int getItemCounts() {
            int count = ColorNumericKeyboard.this.sCells.length * ColorNumericKeyboard.this.sCells[0].length;
            if (ColorNumericKeyboard.this.mType != 1) {
                return count;
            }
            if (ColorNumericKeyboard.this.mIsFinishVibility) {
                return ColorNumericKeyboard.ELEVEN;
            }
            return 10;
        }

        public CharSequence getItemDescription(int virtualViewId) {
            CharSequence description;
            if (ColorNumericKeyboard.this.mType == 1) {
                if (virtualViewId == 9) {
                    description = ColorNumericKeyboard.this.mKeyboardNumbers[10] + " ";
                } else if (virtualViewId == 10) {
                    description = ColorNumericKeyboard.this.mSure;
                } else {
                    description = ColorNumericKeyboard.this.mKeyboardNumbers[virtualViewId] + " ";
                }
            } else if (virtualViewId == 9) {
                description = ColorNumericKeyboard.this.mStarString;
            } else if (virtualViewId == ColorNumericKeyboard.ELEVEN) {
                description = ColorNumericKeyboard.this.mWellString;
            } else {
                description = ColorNumericKeyboard.this.mKeyboardNumbers[virtualViewId] + " ";
            }
            if (description != null) {
                return description;
            }
            return getClass().getSimpleName();
        }
    }

    public synchronized Cell of(int row, int column) {
        checkRange(row, column);
        return this.sCells[row][column];
    }

    private void checkRange(int row, int column) {
        if (row < 0 || row > 3) {
            throw new IllegalArgumentException("row must be in range 0-3");
        } else if (column < 0 || column > 2) {
            throw new IllegalArgumentException("column must be in range 0-2");
        }
    }

    public ColorNumericKeyboard(Context context) {
        this(context, null);
    }

    public ColorNumericKeyboard(Context context, AttributeSet attrs) {
        this(context, attrs, 201393302);
    }

    public ColorNumericKeyboard(Context context, AttributeSet attrs, int defStyle) {
        int displayWidth;
        super(context, attrs, defStyle);
        this.mBgDrawable = null;
        this.mPaint = null;
        this.mTouchCell = null;
        this.mFadeAnimator = null;
        this.mShowAnimator = null;
        this.mIndex = -1;
        this.mOnItemClickListener = null;
        this.mTouchUpListener = null;
        this.mTouchTextListener = null;
        this.mType = -1;
        this.mEnableHapticFeedback = true;
        this.NUMERIC = 1;
        this.WORD = 2;
        this.mDownState = false;
        this.sCells = (Cell[][]) Array.newInstance(Cell.class, new int[]{4, 3});
        this.mKeyboardLetters = null;
        this.mKeyboardLetterStar = null;
        this.mKeyboardLetterWellNumber = null;
        this.mKeyboardNumbers = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, -1, 0, -1};
        this.mNumberTextPaint = new TextPaint();
        this.mLetterTextPaint = new TextPaint();
        this.mNumberTextFontMetrics = null;
        this.mLetterTextFontMetrics = null;
        this.mWordTextFontMetrics = null;
        this.mLinePaint = new Paint();
        this.mLineWidth = -1.0f;
        this.mLineHeight = -1.0f;
        this.mKeyboardNumberTextSize = -1.0f;
        this.mKeyboardNumberTextColor = -1;
        this.mKeyboardLetterTextSize = -1.0f;
        this.mKeyboardLetterPlusSize = -1.0f;
        this.mKeyboardLetterTextColor = -1;
        this.mLineSpace = -1.0f;
        this.mThreeLineTopOffset = 0.0f;
        this.mIsThailandLanguage = false;
        this.mSure = null;
        this.mWordTextPaint = new TextPaint();
        this.mWordTextSize = -1.0f;
        this.mWordTextNormalColor = -1;
        this.mWordTextPressColor = -1;
        this.mWordTextColor = -1;
        this.mSpecialIndex = -1;
        this.mIsCanvasCir = false;
        this.mIsFinishVibility = false;
        this.mVerticalOffset = -1;
        this.mWellString = null;
        this.mStarString = null;
        this.mContext = null;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm.widthPixels > dm.heightPixels) {
            displayWidth = dm.heightPixels;
        } else {
            displayWidth = dm.widthPixels;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorNumericKeyboard, defStyle, 0);
        this.mCircleDiameter = getDimensionOrFraction(a, 2, displayWidth, displayWidth);
        this.mLevelSpace = getDimensionOrFraction(a, 3, displayWidth, displayWidth);
        this.mVerticalSpace = getDimensionOrFraction(a, 4, displayWidth, displayWidth);
        this.mPressedColor = a.getColor(1, 0);
        this.mLineHeight = (float) getDimensionOrFraction(a, 5, displayWidth, displayWidth);
        this.mLineWidth = context.getResources().getDimension(201655579);
        a.recycle();
        this.mExploreByTouchHelper = new PatternExploreByTouchHelper(this);
        setAccessibilityDelegate(this.mExploreByTouchHelper);
        setImportantForAccessibility(1);
        this.mContext = context;
        this.mExploreByTouchHelper.invalidateRoot();
        this.mKeyboardLetters = context.getResources().getStringArray(201785351);
        this.mKeyboardLetterStar = context.getResources().getDrawable(201851143);
        this.mKeyboardLetterWellNumber = context.getResources().getDrawable(201851144);
        this.mKeyboardNumberTextSize = context.getResources().getDimension(201654434);
        this.mKeyboardNumberTextColor = ColorContextUtil.getAttrColor(this.mContext, 201392708);
        this.mKeyboardLetterTextSize = context.getResources().getDimension(201654406);
        this.mKeyboardLetterPlusSize = context.getResources().getDimension(201654408);
        this.mIsThailandLanguage = Locale.getDefault().toString().equals("th_TH");
        this.mLineSpace = context.getResources().getDimension(201654457);
        this.mKeyboardLetterTextColor = ColorContextUtil.getAttrColor(this.mContext, 201392708);
        this.mBgHeight = (int) context.getResources().getDimension(201654467);
        this.mBgWidth = (int) context.getResources().getDimension(201654466);
        this.mVerticalOffset = (int) context.getResources().getDimension(201655580);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                this.sCells[i][j] = new Cell(this, i, j, null);
                this.sCells[i][j].cellLettersStr = this.mKeyboardLetters[(i * 3) + j];
                if (this.sCells[i][j].cellLettersStr.contains("\n")) {
                    this.mThreeLineTopOffset = context.getResources().getDimension(201654455) + context.getResources().getDimension(201654456);
                }
                if (this.mKeyboardNumbers[(i * 3) + j] > -1) {
                    this.sCells[i][j].cellNumberStr = String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(number)});
                }
            }
        }
        this.mSure = context.getResources().getString(201590161);
        this.mWordTextSize = context.getResources().getDimension(201654416);
        this.mWordTextNormalColor = ColorContextUtil.getAttrColor(this.mContext, 201392708);
        this.mWordTextPressColor = context.getColor(201720906);
        this.mWordTextColor = this.mWordTextNormalColor;
        this.mWellString = context.getResources().getString(201590165);
        this.mStarString = context.getResources().getString(201590166);
    }

    static int getDimensionOrFraction(TypedArray a, int index, int base, int defValue) {
        TypedValue value = a.peekValue(index);
        if (value == null) {
            return defValue;
        }
        if (value.type == 5) {
            return a.getDimensionPixelOffset(index, defValue);
        }
        if (value.type == 6) {
            return Math.round(a.getFraction(index, base, base, (float) defValue));
        }
        return defValue;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        init();
        this.mViewWidth = this.mBgWidth + this.mLevelSpace;
        this.mViewHeight = this.mBgHeight + this.mVerticalSpace;
        setMeasuredDimension(this.mViewWidth, this.mViewHeight);
    }

    private void init() {
        this.mCircleRadius = this.mCircleDiameter / 2;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(this.mPressedColor);
        this.mPaint.setAlpha(0);
        this.mNumberTextPaint.setTextSize(this.mKeyboardNumberTextSize);
        this.mNumberTextPaint.setColor(this.mKeyboardNumberTextColor);
        this.mNumberTextPaint.setAntiAlias(true);
        this.mNumberTextFontMetrics = this.mNumberTextPaint.getFontMetricsInt();
        this.mNumberTextPaint.setAlpha(230);
        this.mLetterTextPaint.setTextSize(this.mKeyboardLetterTextSize);
        this.mLetterTextPaint.setColor(this.mKeyboardLetterTextColor);
        this.mLetterTextPaint.setAntiAlias(true);
        this.mLetterTextPaint.setAlpha(179);
        this.mLetterTextFontMetrics = this.mLetterTextPaint.getFontMetricsInt();
        this.mLinePaint.setColor(this.mKeyboardNumberTextColor);
        this.mLinePaint.setAlpha(13);
        this.mLinePaint.setAntiAlias(true);
        this.mLinePaint.setStyle(Style.STROKE);
        this.mLinePaint.setStrokeWidth(this.mLineHeight);
        this.mWordTextPaint.setColor(this.mWordTextColor);
        this.mWordTextPaint.setTextSize(this.mWordTextSize);
        this.mWordTextPaint.setFakeBoldText(true);
        this.mWordTextPaint.setAntiAlias(true);
        this.mWordTextFontMetrics = this.mWordTextPaint.getFontMetricsInt();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (isMultiPointerEvent(event)) {
            return true;
        }
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case 0:
                this.mDownState = true;
                handleActionDown(event);
                break;
            case 1:
                this.mDownState = false;
                handleActionUp(event);
                break;
            case 3:
                this.mDownState = false;
                handleActionUp(event);
                break;
            case 6:
                this.mDownState = false;
                handleActionUp(event);
                break;
        }
        return true;
    }

    private boolean isMultiPointerEvent(MotionEvent event) {
        if (event.getPointerId(event.getActionIndex()) > 0) {
            return true;
        }
        return false;
    }

    private void setTouchFeedback() {
        performHapticFeedback(ColorHapticFeedbackConstants.KEYBOARD_TOUCH_FEEDBACK);
    }

    private void handleActionUp(MotionEvent event) {
        if (AccessibilityManager.getInstance(this.mContext).isTouchExplorationEnabled()) {
            this.mTouchCell = detectAndAddHit(event.getX(), event.getY());
            this.mSpecialIndex = -1;
            if (this.mTouchCell != null) {
                setTouchIndex(this.mTouchCell);
                this.mExploreByTouchHelper.invalidateRoot();
                if (this.mType == 1 && this.mSpecialIndex == ELEVEN) {
                    this.mIsCanvasCir = true;
                }
                if (!(this.mIndex == -1 || this.mOnItemClickListener == null)) {
                    this.mOnItemClickListener.OnItemTouch();
                }
                if (this.mEnableHapticFeedback && ((this.mIndex != -1 && this.mType == 1) || (this.mSpecialIndex != -1 && this.mType == 1))) {
                    setTouchFeedback();
                }
            }
        }
        this.mFadeAnimator = initAnimator(this.mFadeAnimator, ENDALPHA, 0);
        fade();
        if (!(this.mTouchUpListener == null || this.mIndex == -1)) {
            this.mTouchUpListener.OnTouchUp();
        }
        if (this.mTouchTextListener != null) {
            if (this.mIndex != -1) {
                this.mTouchTextListener.onTouchText(this.NUMERIC);
            }
            if (this.mSpecialIndex != -1) {
                this.mTouchTextListener.onTouchText(this.WORD);
            }
        }
        if (this.mType == 1 && this.mSpecialIndex == ELEVEN) {
            this.mIsCanvasCir = false;
        }
        invalidate();
    }

    private void handleActionDown(MotionEvent event) {
        if (!AccessibilityManager.getInstance(this.mContext).isTouchExplorationEnabled()) {
            this.mTouchCell = detectAndAddHit(event.getX(), event.getY());
            this.mSpecialIndex = -1;
            if (this.mTouchCell != null) {
                setTouchIndex(this.mTouchCell);
                this.mExploreByTouchHelper.invalidateRoot();
                if (this.mType == 1 && this.mSpecialIndex == ELEVEN) {
                    this.mIsCanvasCir = true;
                }
                if (!(this.mIndex == -1 || this.mOnItemClickListener == null)) {
                    this.mOnItemClickListener.OnItemTouch();
                }
                if (this.mEnableHapticFeedback && ((this.mIndex != -1 && this.mType == 1) || (this.mSpecialIndex != -1 && this.mType == 1))) {
                    setTouchFeedback();
                }
            }
        }
        this.mShowAnimator = initAnimator(this.mShowAnimator, STARTALPHA, ENDALPHA);
        show();
        invalidate();
    }

    private int setTouchIndex(Cell cell) {
        this.mIndex = (cell.getRow() * 3) + cell.getColumn();
        if (this.mType == 1) {
            if (this.mIndex == ELEVEN && this.mIsFinishVibility) {
                this.mSpecialIndex = ELEVEN;
            }
            if (this.mIndex == 9 || this.mIndex == ELEVEN) {
                this.mIndex = -1;
            }
            if (this.mIndex == 10) {
                this.mIndex = 9;
            }
        }
        return this.mIndex;
    }

    private void fade() {
        if (!this.mNeedDoFade) {
            if (this.mShowAnimator == null || !this.mShowAnimator.isRunning()) {
                if (this.mFadeAnimator != null) {
                    this.mFadeAnimator.start();
                }
                if (this.mShowAnimator != null) {
                    this.mShowAnimator.end();
                    this.mShowAnimator = null;
                }
            } else {
                this.mNeedDoFadeAfterShow = true;
            }
            this.mNeedDoFade = true;
        }
    }

    private void show() {
        this.mNeedDoFade = false;
        this.mNeedDoFadeAfterShow = false;
        if (this.mShowAnimator != null && (this.mShowAnimator.isRunning() ^ 1) != 0) {
            this.mShowAnimator.start();
            if (this.mFadeAnimator != null) {
                this.mFadeAnimator.end();
                this.mFadeAnimator = null;
            }
        }
    }

    private ValueAnimator initAnimator(ValueAnimator animator, final int startAlpha, final int endAlpha) {
        if (animator != null) {
            return animator;
        }
        animator = ValueAnimator.ofInt(new int[]{startAlpha, endAlpha});
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animator) {
                ColorNumericKeyboard.this.changeBackagegroundAlpha(((Integer) animator.getAnimatedValue()).intValue());
            }
        });
        animator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animator) {
                ColorNumericKeyboard.this.changeBackagegroundAlpha(startAlpha);
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                if (ColorNumericKeyboard.this.mNeedDoFadeAfterShow && ColorNumericKeyboard.this.mFadeAnimator != null) {
                    ColorNumericKeyboard.this.mFadeAnimator.start();
                }
                ColorNumericKeyboard.this.mNeedDoFadeAfterShow = false;
                ColorNumericKeyboard.this.changeBackagegroundAlpha(endAlpha);
            }

            public void onAnimationCancel(Animator animator) {
                ColorNumericKeyboard.this.changeBackagegroundAlpha(endAlpha);
            }
        });
        animator.setDuration(150);
        return animator;
    }

    private void changeBackagegroundAlpha(int alpha) {
        if (this.mPaint != null) {
            this.mPaint.setAlpha(alpha);
            invalidate();
        }
    }

    private float getCenterXForColumn(int column) {
        float squareWidth = (float) (this.mCircleDiameter + this.mLevelSpace);
        return (((float) this.mPaddingLeft) + (squareWidth / 2.0f)) + (((float) column) * squareWidth);
    }

    private float getCenterYForRow(int row) {
        float squareHeight = (float) (this.mCircleDiameter + this.mVerticalSpace);
        return (((float) this.mPaddingTop) + (squareHeight / 2.0f)) + (((float) row) * squareHeight);
    }

    private Cell detectAndAddHit(float x, float y) {
        return checkForNewHit(x, y);
    }

    private Cell checkForNewHit(float x, float y) {
        int rowHit = getRowHit(y);
        if (rowHit < 0) {
            return null;
        }
        int columnHit = getColumnHit(x);
        if (columnHit < 0) {
            return null;
        }
        return of(rowHit, columnHit);
    }

    private int getRowHit(float y) {
        float squareHeight = (float) (this.mCircleDiameter + this.mVerticalSpace);
        for (int i = 0; i < 4; i++) {
            float hitTop = squareHeight * ((float) i);
            if (y >= hitTop && y <= hitTop + squareHeight) {
                return i;
            }
        }
        return -1;
    }

    private int getColumnHit(float x) {
        float squareWidth = (float) (this.mCircleDiameter + this.mLevelSpace);
        for (int i = 0; i < 3; i++) {
            float hitLeft = squareWidth * ((float) i);
            if (x >= hitLeft && x <= hitLeft + squareWidth) {
                return i;
            }
        }
        return -1;
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mFadeAnimator != null) {
            this.mFadeAnimator.end();
            this.mFadeAnimator = null;
        }
        if (this.mShowAnimator != null) {
            this.mShowAnimator.end();
            this.mShowAnimator = null;
        }
        if (this.mPaint != null) {
            this.mPaint = null;
        }
        if (this.mTouchCell != null) {
            this.mTouchCell = null;
        }
        this.mDownState = false;
    }

    public void onDraw(Canvas canvas) {
        int i;
        super.onDraw(canvas);
        drawPressCircle(canvas);
        for (i = 0; i < 3; i++) {
            drawLine(canvas, i);
        }
        for (i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                drawCell(canvas, j, i);
            }
        }
    }

    private void drawPressCircle(Canvas canvas) {
        int radius = 0;
        if (this.mShowAnimator != null) {
            radius = ((76 - ((Integer) this.mShowAnimator.getAnimatedValue()).intValue()) * this.mCircleRadius) / Math.abs(ENDALPHA);
        }
        if (this.mFadeAnimator != null && this.mShowAnimator == null) {
            radius = this.mCircleRadius;
        }
        if (this.mTouchCell != null) {
            float startX = getCenterXForColumn(this.mTouchCell.column);
            float startY = getCenterYForRow(this.mTouchCell.row);
            if (setTouchIndex(this.mTouchCell) != -1 && this.mPaint != null) {
                canvas.drawCircle(startX, startY, (float) radius, this.mPaint);
            }
        }
    }

    private void drawLine(Canvas canvas, int column) {
        float startX = (((float) this.mViewWidth) - this.mLineWidth) / 2.0f;
        float startY = (float) ((this.mCircleDiameter + this.mVerticalSpace) * (column + 1));
        canvas.drawLine(startX, startY, startX + this.mLineWidth, startY, this.mLinePaint);
    }

    private void drawCell(Canvas canvas, int column, int row) {
        Cell cell = this.sCells[row][column];
        float startX = getCenterXForColumn(column);
        float startY = getCenterYForRow(row);
        if (this.mType != 1) {
            int left;
            int top;
            if ((row * 3) + column == 9 && this.mKeyboardLetterStar != null) {
                left = ((int) startX) - (this.mKeyboardLetterStar.getIntrinsicWidth() / 2);
                top = (((int) startY) - (this.mKeyboardLetterStar.getIntrinsicHeight() / 2)) - this.mVerticalOffset;
                this.mKeyboardLetterStar.setBounds(left, top, left + this.mKeyboardLetterStar.getIntrinsicWidth(), top + this.mKeyboardLetterStar.getIntrinsicHeight());
                this.mKeyboardLetterStar.draw(canvas);
            }
            if ((row * 3) + column == ELEVEN && this.mKeyboardLetterWellNumber != null) {
                left = ((int) startX) - (this.mKeyboardLetterWellNumber.getIntrinsicWidth() / 2);
                top = (((int) startY) - (this.mKeyboardLetterWellNumber.getIntrinsicHeight() / 2)) - this.mVerticalOffset;
                this.mKeyboardLetterWellNumber.setBounds(left, top, left + this.mKeyboardLetterWellNumber.getIntrinsicWidth(), top + this.mKeyboardLetterWellNumber.getIntrinsicHeight());
                this.mKeyboardLetterWellNumber.draw(canvas);
            }
        } else if (this.mIsFinishVibility && this.mType == 1 && (row * 3) + column == ELEVEN) {
            if (this.mIsCanvasCir) {
                int radius = 0;
                if (this.mShowAnimator != null) {
                    radius = ((76 - ((Integer) this.mShowAnimator.getAnimatedValue()).intValue()) * this.mCircleRadius) / Math.abs(ENDALPHA);
                }
                if (this.mFadeAnimator != null && this.mShowAnimator == null) {
                    radius = this.mCircleRadius;
                }
                canvas.drawCircle(getCenterXForColumn(2), getCenterYForRow(3), (float) radius, this.mPaint);
            }
            if (this.mSure != null) {
                this.mWordTextPaint.setColor(this.mWordTextColor);
                float textX = startX - (this.mWordTextPaint.measureText(this.mSure) / 2.0f);
                float textY = ((startY - ((float) ((this.mWordTextFontMetrics.descent - this.mWordTextFontMetrics.ascent) / 2))) - ((float) this.mWordTextFontMetrics.ascent)) - ((float) this.mVerticalOffset);
                canvas.drawText(this.mSure, textX, textY, this.mWordTextPaint);
            }
        }
        float numberStrBaseLine = (((startY - ((float) ((((this.mNumberTextFontMetrics.bottom - this.mNumberTextFontMetrics.top) + this.mLetterTextFontMetrics.bottom) - this.mLetterTextFontMetrics.top) / 2))) - this.mThreeLineTopOffset) - ((float) this.mNumberTextFontMetrics.top)) - ((float) (this.mNumberTextFontMetrics.descent / 4));
        float letterStrBaseLine = ((((float) this.mNumberTextFontMetrics.bottom) + numberStrBaseLine) - this.mLineSpace) - ((float) this.mLetterTextFontMetrics.top);
        if (!TextUtils.isEmpty(cell.cellNumberStr)) {
            float textWidth = this.mNumberTextPaint.measureText(cell.cellNumberStr);
            if ((row * 3) + column == 10) {
                if (TextUtils.isEmpty(cell.cellLettersStr)) {
                    numberStrBaseLine = ((startY - ((float) ((this.mNumberTextFontMetrics.descent - this.mNumberTextFontMetrics.ascent) / 2))) - ((float) this.mNumberTextFontMetrics.ascent)) - ((float) this.mVerticalOffset);
                } else if (cell.cellLettersStr.lastIndexOf(10) <= 0) {
                    numberStrBaseLine += this.mThreeLineTopOffset;
                }
                letterStrBaseLine = ((((float) this.mNumberTextFontMetrics.bottom) + numberStrBaseLine) - this.mLineSpace) - ((float) this.mLetterTextFontMetrics.top);
            }
            canvas.drawText(cell.cellNumberStr, startX - (textWidth / 2.0f), numberStrBaseLine, this.mNumberTextPaint);
        }
        if (!TextUtils.isEmpty(cell.cellLettersStr)) {
            float letterLine1TextHeight = (float) ((this.mLetterTextFontMetrics.descent - this.mLetterTextFontMetrics.ascent) + this.mLetterTextFontMetrics.leading);
            int index = cell.cellLettersStr.lastIndexOf(10);
            String line1 = cell.cellLettersStr;
            String line2 = null;
            if (index > 0) {
                line1 = cell.cellLettersStr.substring(0, index);
                line2 = cell.cellLettersStr.substring(index);
            }
            float textWidthLine1 = this.mLetterTextPaint.measureText(line1);
            if ((row * 3) + column == 10) {
                if (!this.mIsThailandLanguage) {
                    this.mLetterTextPaint.setTextSize(this.mKeyboardLetterPlusSize);
                }
                canvas.drawText(line1, startX - (textWidthLine1 / 2.0f), letterStrBaseLine, this.mLetterTextPaint);
                this.mLetterTextPaint.setTextSize(this.mKeyboardLetterTextSize);
            } else {
                canvas.drawText(line1, startX - (textWidthLine1 / 2.0f), letterStrBaseLine, this.mLetterTextPaint);
            }
            if (!TextUtils.isEmpty(line2)) {
                canvas.drawText(line2, startX - (this.mLetterTextPaint.measureText(line2) / 2.0f), letterStrBaseLine + letterLine1TextHeight, this.mLetterTextPaint);
            }
        }
    }

    public void setEnabled(boolean enable) {
        if (!enable && this.mDownState) {
            if (this.mFadeAnimator != null && this.mFadeAnimator.isRunning()) {
                this.mFadeAnimator.end();
            }
            if (this.mShowAnimator != null && this.mShowAnimator.isRunning()) {
                this.mShowAnimator.end();
            }
            if (this.mPaint != null) {
                this.mPaint.setAlpha(0);
                this.mDownState = false;
                invalidate();
            }
        }
        super.setEnabled(enable);
    }

    public void setType(int type) {
        this.mType = type;
    }

    public void setItemTouchListener(OnItemTouchListener itemClick) {
        this.mOnItemClickListener = itemClick;
    }

    public void setTouchTextListener(OnTouchTextListener itemClick) {
        this.mTouchTextListener = itemClick;
    }

    public void setTouchUpListener(OnTouchUpListener touchUp) {
        this.mTouchUpListener = touchUp;
    }

    public int getTouchIndex() {
        if (this.mIndex >= 0) {
            return this.mIndex;
        }
        return -1;
    }

    public boolean isTactileFeedbackEnabled() {
        return this.mEnableHapticFeedback;
    }

    public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
        this.mEnableHapticFeedback = tactileFeedbackEnabled;
    }

    public void setHasFinishButton(boolean isVisibility) {
        this.mIsFinishVibility = isVisibility;
        invalidate();
    }

    public boolean isLongshotUnsupported() {
        return true;
    }

    public boolean onHoverEvent(MotionEvent event) {
        boolean a = AccessibilityManager.getInstance(this.mContext).isTouchExplorationEnabled();
        if (AccessibilityManager.getInstance(this.mContext).isTouchExplorationEnabled()) {
            int action = event.getAction();
            switch (action) {
                case 7:
                    event.setAction(2);
                    break;
                case 9:
                    event.setAction(0);
                    break;
                case 10:
                    event.setAction(1);
                    break;
            }
            onTouchEvent(event);
            event.setAction(action);
        }
        return super.onHoverEvent(event);
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        return super.dispatchHoverEvent(event) | this.mExploreByTouchHelper.dispatchHoverEvent(event);
    }
}
