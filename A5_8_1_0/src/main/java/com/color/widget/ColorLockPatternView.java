package com.color.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.IntArray;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.internal.widget.ExploreByTouchHelper;
import com.android.internal.widget.LockPatternUtils;
import com.color.screenshot.ColorLongshotUnsupported;
import com.color.util.ColorContextUtil;
import com.color.util.ColorDialogUtil;
import com.oppo.widget.OppoFingerWrongIn;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import oppo.R;

public class ColorLockPatternView extends View implements ColorAnimation, ColorLongshotUnsupported {
    private static final int ASPECT_LOCK_HEIGHT = 2;
    private static final int ASPECT_LOCK_WIDTH = 1;
    private static final int ASPECT_SQUARE = 0;
    private static final boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final float DRAG_THRESHHOLD = 0.0f;
    private static final float ENDALPHA = 0.0f;
    private static final int LONGTIME = 200;
    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;
    private static final int MORELONGTIME = 300;
    private static final boolean PROFILE_DRAWING = false;
    private static final int SHORTTIME = 100;
    private static final float STARTALPHA = 0.3f;
    static final int STATUS_BAR_HEIGHT = 25;
    private static final String TAG = "ColorLockPatternView";
    public static final int VIRTUAL_BASE_VIEW_ID = 1;
    private final Interpolator TRUELARGE;
    private long mAnimatingPeriodStart;
    private int mAspect;
    private AudioManager mAudioManager;
    private float mCenterX;
    private float mCenterY;
    private float mCirLargeWidth;
    private Paint mCirclePaint;
    private final Path mCurrentPath;
    private int mDefaultCircleColor;
    private int mDefaultColorBlue;
    private int mDefaultColorGreen;
    private int mDefaultColorRed;
    private float mDefaultDiameterFactor;
    private float mDefaultFingerRadius;
    private float mDefaultRadius;
    private float mDiameterFactor;
    private boolean mDrawingProfilingStarted;
    private boolean mEnableHapticFeedback;
    private int mEndAlpha;
    private PatternExploreByTouchHelper mExploreByTouchHelper;
    private Paint mFingerPaint;
    private float mFingerRadius;
    private float mHitFactor;
    private float mInProgressX;
    private float mInProgressY;
    private boolean mInStealthMode;
    private boolean mInputEnabled;
    private final Rect mInvalidate;
    private boolean mIsClearPattern;
    private boolean mIsDrag;
    private boolean mIsDrawLadder;
    private boolean mIsSetPassword;
    private boolean mIsShrinkAnim;
    private int mLadderEndColor;
    private int mLadderStartColor;
    private float mLargeFingerRadius;
    private float mLargeRingRadius;
    private float mLargeRingWidth;
    private float mLineEndWidth;
    private float mLineStartWidth;
    private final int mMinMumber;
    private boolean[][] mNotDraw;
    private OnPatternListener mOnPatternListener;
    private Paint mPaint;
    private int[][] mPaintAlpha;
    private Paint mPathPaint;
    private int mPathTrueColor;
    private int mPathWrongColor;
    private ArrayList<Cell> mPattern;
    private DisplayMode mPatternDisplayMode;
    private boolean[][] mPatternDrawLookup;
    private boolean mPatternInProgress;
    private Paint mRingPaint;
    private float mSetLockPathWidth;
    private double mShrinkAngle;
    private Paint mShrinkPaint;
    private Shader mShrinkShader;
    private float mSmallRingRadius;
    private float mSmallRingWidth;
    private float mSquareHeight;
    private float mSquareWidth;
    private int mStartAlpha;
    float[] mStretch1;
    float[] mStretch2;
    float[] mStretch3;
    float[] mStretch4;
    private double mStretchAngle;
    private Paint mStretchPaint;
    private Shader mStretchShader;
    private final int mStrokeAlpha;
    private final Rect mTmpInvalidateRect;
    private float[][] mTranslateY;
    private int mTrueCircleColor;
    private int mTrueColorBlue;
    private int mTrueColorGreen;
    private int mTrueColorRed;
    private int mTrueLargeTime;
    private int mTruePauseTime;
    private int mViewHeight;
    private int mViewWidth;
    private int mWrongCircleColor;
    private int mWrongColorBlue;
    private int mWrongColorGreen;
    private int mWrongColorRed;
    private OppoFingerWrongIn mWrongInterpolator;
    private int mWrongPathAlpha;
    private int mWrongTotalTime;
    private int mZoomOutPositon;

    private class AnimUpdateListener implements AnimatorUpdateListener {
        private final Cell mCell;

        public AnimUpdateListener(Cell cell) {
            this.mCell = cell;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            float value = ((Float) animation.getAnimatedValue()).floatValue();
            this.mCell.ringRadius = ColorLockPatternView.this.mSmallRingRadius + ((ColorLockPatternView.this.mLargeRingRadius - ColorLockPatternView.this.mSmallRingRadius) * value);
            this.mCell.ringWidth = ColorLockPatternView.this.mLargeRingWidth - ((ColorLockPatternView.this.mLargeRingWidth - ColorLockPatternView.this.mSmallRingWidth) * value);
            if (ColorLockPatternView.this.mPatternDisplayMode == DisplayMode.Wrong) {
                this.mCell.wrongRadius = ColorLockPatternView.this.mDefaultRadius + ((ColorLockPatternView.this.mCirLargeWidth - ColorLockPatternView.this.mDefaultRadius) * value);
                if (!ColorLockPatternView.this.mIsSetPassword) {
                    this.mCell.wrongColorRed = (int) (((float) ColorLockPatternView.this.mDefaultColorRed) + (((float) (ColorLockPatternView.this.mWrongColorRed - ColorLockPatternView.this.mDefaultColorRed)) * value));
                    this.mCell.wrongColorGreen = (int) (((float) ColorLockPatternView.this.mDefaultColorGreen) + (((float) (ColorLockPatternView.this.mWrongColorGreen - ColorLockPatternView.this.mDefaultColorGreen)) * value));
                    this.mCell.wrongColorBlue = (int) (((float) ColorLockPatternView.this.mDefaultColorBlue) + (((float) (ColorLockPatternView.this.mWrongColorBlue - ColorLockPatternView.this.mDefaultColorBlue)) * value));
                } else if (ColorLockPatternView.this.mIsClearPattern) {
                    this.mCell.wrongColorRed = (int) (((float) ColorLockPatternView.this.mDefaultColorRed) + (((float) (ColorLockPatternView.this.mWrongColorRed - ColorLockPatternView.this.mDefaultColorRed)) * value));
                    this.mCell.wrongColorGreen = (int) (((float) ColorLockPatternView.this.mDefaultColorGreen) + (((float) (ColorLockPatternView.this.mWrongColorGreen - ColorLockPatternView.this.mDefaultColorGreen)) * value));
                    this.mCell.wrongColorBlue = (int) (((float) ColorLockPatternView.this.mDefaultColorBlue) + (((float) (ColorLockPatternView.this.mWrongColorBlue - ColorLockPatternView.this.mDefaultColorBlue)) * value));
                } else {
                    this.mCell.wrongColorRed = (int) (((float) ColorLockPatternView.this.mTrueColorRed) + (((float) (ColorLockPatternView.this.mWrongColorRed - ColorLockPatternView.this.mTrueColorRed)) * value));
                    this.mCell.wrongColorGreen = (int) (((float) ColorLockPatternView.this.mTrueColorGreen) + (((float) (ColorLockPatternView.this.mWrongColorGreen - ColorLockPatternView.this.mTrueColorGreen)) * value));
                    this.mCell.wrongColorBlue = (int) (((float) ColorLockPatternView.this.mTrueColorBlue) + (((float) (ColorLockPatternView.this.mWrongColorBlue - ColorLockPatternView.this.mTrueColorBlue)) * value));
                }
            }
            ColorLockPatternView.this.invalidate();
        }
    }

    public static class Cell extends com.android.internal.widget.LockPatternView.Cell {
        private static final Cell[][] sCells = createCells();
        float coordinateX;
        boolean isfillInGapCell = false;
        float ringRadius;
        float ringWidth;
        float[] shrink1 = new float[2];
        float[] shrink2 = new float[2];
        float[] shrink3 = new float[2];
        float[] shrink4 = new float[2];
        ValueAnimator shrinkAnim = null;
        ShrinkAnimListener shrinkAnimListener = new ShrinkAnimListener(this, null);
        int trueColorBlue;
        int trueColorGreen;
        int trueColorRed;
        int wrongColorBlue;
        int wrongColorGreen;
        int wrongColorRed;
        float wrongRadius;
        ValueAnimator zoomInAnim = null;
        ZoomInAnimListener zoomInAnimListener = null;
        boolean zoomInEnd = false;
        boolean zoomInStart = false;
        boolean zoomOut = false;
        ValueAnimator zoomOutAnim = null;
        ZoomOutAnimListener zoomOutAnimListener = null;
        ValueAnimator zoomOutWrongAnim = null;

        private class ShrinkAnimListener extends AnimatorListenerAdapter {
            /* synthetic */ ShrinkAnimListener(Cell this$1, ShrinkAnimListener -this1) {
                this();
            }

            private ShrinkAnimListener() {
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void updateParms(float startX, float endX, AnimatorUpdateListener updateListener) {
            }

            public void onAnimationStart(Animator animation) {
            }
        }

        private class ZoomInAnimListener extends AnimatorListenerAdapter {
            ColorAnimation anim;
            Cell cell;
            AnimatorUpdateListener updateListener;

            public ZoomInAnimListener(Cell cell, ColorAnimation anim) {
                this.cell = cell;
                this.anim = anim;
            }

            public void onAnimationStart(Animator animation) {
                this.cell.zoomInStart = true;
            }

            public void onAnimationEnd(Animator animation) {
                this.cell.startChangeColor(this.anim, this.cell);
            }
        }

        private class ZoomOutAnimListener extends AnimatorListenerAdapter {
            ColorAnimation anim;
            Cell cell;
            float endValue;
            float startValue;
            AnimatorUpdateListener updateListener;
            Cell zoomInCell;

            public ZoomOutAnimListener(Cell cell, ColorAnimation anim) {
                this.cell = cell;
                this.anim = anim;
            }

            public void onAnimationCancel(Animator animation) {
                this.startValue = ((Float) ((ValueAnimator) animation).getAnimatedValue()).floatValue();
                Cell.this.startZoomInInternal(this.startValue, this.endValue, this.updateListener, this.zoomInCell, this.anim);
            }

            public void onAnimationStart(Animator animation) {
                this.cell.zoomOut = true;
            }

            public void onAnimationEnd(Animator animation) {
                this.cell.zoomOut = false;
            }

            public void updateZoomParams(float startValue, float endValue, AnimatorUpdateListener updateListener, Cell cell) {
                this.startValue = startValue;
                this.endValue = endValue;
                this.updateListener = updateListener;
                this.zoomInCell = cell;
            }
        }

        private static Cell[][] createCells() {
            Cell[][] res = (Cell[][]) Array.newInstance(Cell.class, new int[]{3, 3});
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    res[i][j] = new Cell(i, j);
                }
            }
            return res;
        }

        public Cell(int row, int column) {
            checkRange(row, column);
            this.row = row;
            this.column = column;
        }

        public static Cell of(int row, int column) {
            checkRange(row, column);
            return sCells[row][column];
        }

        private static void checkRange(int row, int column) {
            if (row < 0 || row > 2) {
                throw new IllegalArgumentException("row must be in range 0-2");
            } else if (column < 0 || column > 2) {
                throw new IllegalArgumentException("column must be in range 0-2");
            }
        }

        public void startZoomOut(float startValue, float endValue, AnimatorUpdateListener updateListener, Cell cell, ColorAnimation anim) {
            this.zoomOutAnimListener = new ZoomOutAnimListener(cell, anim);
            this.zoomOutAnim = ValueAnimator.ofFloat(new float[]{startValue, endValue});
            this.zoomOutAnim.addUpdateListener(updateListener);
            this.zoomOutAnim.addListener(this.zoomOutAnimListener);
            this.zoomOutAnim.setDuration(200);
            this.zoomOutAnim.start();
        }

        public void setShrinkAnim(float startX, float endX, AnimatorUpdateListener updateListener) {
            if (this.shrinkAnim != null && this.shrinkAnim.isRunning()) {
                this.shrinkAnim.cancel();
            }
            startShrinkAnim(startX, endX, updateListener);
        }

        private void startShrinkAnim(float startX, float endX, AnimatorUpdateListener updateListener) {
            this.shrinkAnim = ValueAnimator.ofFloat(new float[]{startX, endX});
            this.shrinkAnim.addUpdateListener(updateListener);
            this.shrinkAnim.addListener(this.shrinkAnimListener);
            this.shrinkAnim.setDuration(300);
            this.shrinkAnim.start();
        }

        public void startZoomOutWrong(float startValue, float endValue, AnimatorUpdateListener updateListener) {
            this.zoomOutWrongAnim = ValueAnimator.ofFloat(new float[]{startValue, endValue});
            this.zoomOutWrongAnim.addUpdateListener(updateListener);
            this.zoomOutWrongAnim.setDuration(100);
            this.zoomOutWrongAnim.start();
        }

        public void startZoomIn(float startValue, float endValue, AnimatorUpdateListener updateListener, Cell cell, ColorAnimation anim) {
            if (this.zoomOutAnim == null || !this.zoomOutAnim.isRunning()) {
                startZoomInInternal(startValue, endValue, updateListener, cell, anim);
                return;
            }
            this.zoomOutAnimListener.updateZoomParams(startValue, endValue, updateListener, cell);
            this.zoomOutAnim.cancel();
        }

        private void startZoomInInternal(float startValue, float endValue, AnimatorUpdateListener updateListener, Cell cell, ColorAnimation anim) {
            this.zoomInAnimListener = new ZoomInAnimListener(cell, anim);
            this.zoomInAnim = ValueAnimator.ofFloat(new float[]{startValue, endValue});
            this.zoomInAnim.addUpdateListener(updateListener);
            this.zoomInAnim.addListener(this.zoomInAnimListener);
            this.zoomInAnim.setDuration(200);
            this.zoomInAnim.start();
        }

        private void startChangeColor(final ColorAnimation anim, final Cell cell) {
            ValueAnimator changeColorAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            changeColorAnim.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    anim.update(((Float) animation.getAnimatedValue()).floatValue(), cell);
                }
            });
            changeColorAnim.setDuration(100);
            changeColorAnim.start();
        }
    }

    public enum DisplayMode {
        Correct,
        Animate,
        Wrong,
        FingerprintMatch,
        FingerprintNoMatch
    }

    public interface OnPatternListener {
        void onPatternCellAdded(List<Cell> list);

        void onPatternCleared();

        void onPatternDetected(List<Cell> list);

        void onPatternStart();
    }

    private final class PatternExploreByTouchHelper extends ExploreByTouchHelper {
        private final SparseArray<VirtualViewContainer> mItems = new SparseArray();
        private Rect mTempRect = new Rect();

        class VirtualViewContainer {
            CharSequence description;

            public VirtualViewContainer(CharSequence description) {
                this.description = description;
            }
        }

        public PatternExploreByTouchHelper(View forView) {
            super(forView);
            for (int i = 1; i < 10; i++) {
                this.mItems.put(i, new VirtualViewContainer(getTextForVirtualView(i)));
            }
        }

        protected int getVirtualViewAt(float x, float y) {
            return getVirtualViewIdForHit(x, y);
        }

        protected void getVisibleVirtualViews(IntArray virtualViewIds) {
            if (ColorLockPatternView.DEBUG) {
                Log.v(ColorLockPatternView.TAG, "getVisibleVirtualViews(len=" + virtualViewIds.size() + ")");
            }
            if (ColorLockPatternView.this.mPatternInProgress) {
                for (int i = 1; i < 10; i++) {
                    virtualViewIds.add(i);
                }
            }
        }

        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            if (ColorLockPatternView.DEBUG) {
                Log.v(ColorLockPatternView.TAG, "onPopulateEventForVirtualView(" + virtualViewId + ")");
            }
            VirtualViewContainer container = (VirtualViewContainer) this.mItems.get(virtualViewId);
            if (container != null) {
                event.getText().add(container.description);
            }
        }

        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(host, event);
            if (!ColorLockPatternView.this.mPatternInProgress) {
                event.setContentDescription(ColorLockPatternView.this.getContext().getText(17040158));
            }
        }

        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            if (ColorLockPatternView.DEBUG) {
                Log.v(ColorLockPatternView.TAG, "onPopulateNodeForVirtualView(view=" + virtualViewId + ")");
            }
            node.setText(getTextForVirtualView(virtualViewId));
            node.setContentDescription(getTextForVirtualView(virtualViewId));
            if (ColorLockPatternView.this.mPatternInProgress) {
                node.setFocusable(true);
                if (isClickable(virtualViewId)) {
                    node.addAction(AccessibilityAction.ACTION_CLICK);
                    node.setClickable(isClickable(virtualViewId));
                }
            }
            Rect bounds = getBoundsForVirtualView(virtualViewId);
            if (ColorLockPatternView.DEBUG) {
                Log.v(ColorLockPatternView.TAG, "bounds:" + bounds.toString());
            }
            node.setBoundsInParent(bounds);
        }

        private boolean isClickable(int virtualViewId) {
            if (virtualViewId == Integer.MIN_VALUE) {
                return false;
            }
            return ColorLockPatternView.this.mPatternDrawLookup[(virtualViewId - 1) / 3][(virtualViewId - 1) % 3] ^ 1;
        }

        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            if (ColorLockPatternView.DEBUG) {
                Log.v(ColorLockPatternView.TAG, "onPerformActionForVirtualView(id=" + virtualViewId + ", action=" + action);
            }
            switch (action) {
                case ColorDialogUtil.BIT_FOUSED_BUTTON_NEGATIVE /*16*/:
                    return onItemClicked(virtualViewId);
                default:
                    if (ColorLockPatternView.DEBUG) {
                        Log.v(ColorLockPatternView.TAG, "*** action not handled in onPerformActionForVirtualView(viewId=" + virtualViewId + "action=" + action + ")");
                    }
                    return false;
            }
        }

        boolean onItemClicked(int index) {
            if (ColorLockPatternView.DEBUG) {
                Log.v(ColorLockPatternView.TAG, "onItemClicked(" + index + ")");
            }
            invalidateVirtualView(index);
            sendEventForVirtualView(index, 1);
            return true;
        }

        private Rect getBoundsForVirtualView(int virtualViewId) {
            int ordinal = virtualViewId - 1;
            Rect bounds = this.mTempRect;
            int row = ordinal / 3;
            int col = ordinal % 3;
            Cell cell = Cell.sCells[row][col];
            float centerX = ColorLockPatternView.this.getCenterXForColumn(col);
            float centerY = ColorLockPatternView.this.getCenterYForRow(row);
            float cellheight = (ColorLockPatternView.this.mSquareHeight * ColorLockPatternView.this.mHitFactor) * 0.5f;
            float cellwidth = (ColorLockPatternView.this.mSquareWidth * ColorLockPatternView.this.mHitFactor) * 0.5f;
            bounds.left = (int) (centerX - cellwidth);
            bounds.right = (int) (centerX + cellwidth);
            bounds.top = (int) (centerY - cellheight);
            bounds.bottom = (int) (centerY + cellheight);
            return bounds;
        }

        private CharSequence getTextForVirtualView(int virtualViewId) {
            return ColorLockPatternView.this.getResources().getString(201590118, new Object[]{Integer.valueOf(virtualViewId)});
        }

        private int getVirtualViewIdForHit(float x, float y) {
            int rowHit = ColorLockPatternView.this.getRowHit(y);
            if (rowHit < 0) {
                return Integer.MIN_VALUE;
            }
            int columnHit = ColorLockPatternView.this.getColumnHit(x);
            if (columnHit < 0) {
                return Integer.MIN_VALUE;
            }
            boolean dotAvailable = ColorLockPatternView.this.mPatternDrawLookup[rowHit][columnHit];
            int view = dotAvailable ? ((rowHit * 3) + columnHit) + 1 : Integer.MIN_VALUE;
            if (ColorLockPatternView.DEBUG) {
                Log.v(ColorLockPatternView.TAG, "getVirtualViewIdForHit(" + x + "," + y + ") => " + view + "avail =" + dotAvailable);
            }
            return view;
        }
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final int mDisplayMode;
        private final boolean mInStealthMode;
        private final boolean mInputEnabled;
        private final String mSerializedPattern;
        private final boolean mTactileFeedbackEnabled;

        /* synthetic */ SavedState(Parcel in, SavedState -this1) {
            this(in);
        }

        /* synthetic */ SavedState(Parcelable superState, String serializedPattern, int displayMode, boolean inputEnabled, boolean inStealthMode, boolean tactileFeedbackEnabled, SavedState -this6) {
            this(superState, serializedPattern, displayMode, inputEnabled, inStealthMode, tactileFeedbackEnabled);
        }

        private SavedState(Parcelable superState, String serializedPattern, int displayMode, boolean inputEnabled, boolean inStealthMode, boolean tactileFeedbackEnabled) {
            super(superState);
            this.mSerializedPattern = serializedPattern;
            this.mDisplayMode = displayMode;
            this.mInputEnabled = inputEnabled;
            this.mInStealthMode = inStealthMode;
            this.mTactileFeedbackEnabled = tactileFeedbackEnabled;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mSerializedPattern = in.readString();
            this.mDisplayMode = in.readInt();
            this.mInputEnabled = ((Boolean) in.readValue(null)).booleanValue();
            this.mInStealthMode = ((Boolean) in.readValue(null)).booleanValue();
            this.mTactileFeedbackEnabled = ((Boolean) in.readValue(null)).booleanValue();
        }

        public String getSerializedPattern() {
            return this.mSerializedPattern;
        }

        public int getDisplayMode() {
            return this.mDisplayMode;
        }

        public boolean isInputEnabled() {
            return this.mInputEnabled;
        }

        public boolean isInStealthMode() {
            return this.mInStealthMode;
        }

        public boolean isTactileFeedbackEnabled() {
            return this.mTactileFeedbackEnabled;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.mSerializedPattern);
            dest.writeInt(this.mDisplayMode);
            dest.writeValue(Boolean.valueOf(this.mInputEnabled));
            dest.writeValue(Boolean.valueOf(this.mInStealthMode));
            dest.writeValue(Boolean.valueOf(this.mTactileFeedbackEnabled));
        }
    }

    private class ShrinkdateListener implements AnimatorUpdateListener {
        private final Cell mCell;

        public ShrinkdateListener(Cell cell) {
            this.mCell = cell;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            this.mCell.coordinateX = ((Float) animation.getAnimatedValue()).floatValue();
            ColorLockPatternView.this.invalidate();
        }
    }

    public ColorLockPatternView(Context context) {
        this(context, null);
    }

    public ColorLockPatternView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393223);
    }

    public ColorLockPatternView(Context context, AttributeSet attrs, int defStyle) {
        int displayWidth;
        super(context, attrs, defStyle);
        this.mDrawingProfilingStarted = false;
        this.mPaint = new Paint();
        this.mPathPaint = new Paint();
        this.mPattern = new ArrayList(9);
        this.mPatternDrawLookup = (boolean[][]) Array.newInstance(Boolean.TYPE, new int[]{3, 3});
        this.mInProgressX = -1.0f;
        this.mInProgressY = -1.0f;
        this.mPatternDisplayMode = DisplayMode.Correct;
        this.mInputEnabled = true;
        this.mInStealthMode = false;
        this.mEnableHapticFeedback = true;
        this.mPatternInProgress = false;
        this.mDiameterFactor = 0.0f;
        this.mDefaultDiameterFactor = 0.2f;
        this.mStrokeAlpha = 127;
        this.mHitFactor = 0.6f;
        this.mCurrentPath = new Path();
        this.mInvalidate = new Rect();
        this.mTmpInvalidateRect = new Rect();
        this.mViewWidth = 0;
        this.mViewHeight = 0;
        this.mDefaultCircleColor = 0;
        this.mWrongCircleColor = 0;
        this.mTrueCircleColor = 0;
        this.mDefaultFingerRadius = 0.0f;
        this.mLargeFingerRadius = 0.0f;
        this.mFingerPaint = null;
        this.mCenterX = 0.0f;
        this.mCenterY = 0.0f;
        this.mFingerRadius = 0.0f;
        this.mWrongTotalTime = 395;
        this.mWrongInterpolator = new OppoFingerWrongIn();
        this.TRUELARGE = new PathInterpolator(0.555f, 0.0f, 0.555f, 1.0f);
        this.mTruePauseTime = 208;
        this.mTrueLargeTime = 375;
        this.mMinMumber = 1;
        this.mDefaultRadius = 0.0f;
        this.mCirclePaint = new Paint();
        this.mIsSetPassword = false;
        this.mIsDrag = false;
        this.mRingPaint = new Paint();
        this.mSetLockPathWidth = 0.0f;
        this.mCirLargeWidth = 0.0f;
        this.mStartAlpha = 0;
        this.mEndAlpha = 255;
        this.mWrongPathAlpha = 0;
        this.mStretchPaint = new Paint();
        this.mShrinkPaint = new Paint();
        this.mIsDrawLadder = false;
        this.mLineEndWidth = 0.0f;
        this.mLineStartWidth = 0.0f;
        this.mStretch1 = new float[2];
        this.mStretch2 = new float[2];
        this.mStretch3 = new float[2];
        this.mStretch4 = new float[2];
        this.mStretchAngle = 0.0d;
        this.mShrinkAngle = 0.0d;
        this.mZoomOutPositon = 0;
        this.mStretchShader = null;
        this.mShrinkShader = null;
        this.mIsShrinkAnim = false;
        this.mWrongColorRed = 0;
        this.mWrongColorGreen = 0;
        this.mWrongColorBlue = 0;
        this.mDefaultColorRed = 0;
        this.mDefaultColorGreen = 0;
        this.mDefaultColorBlue = 0;
        this.mTrueColorRed = 0;
        this.mTrueColorGreen = 0;
        this.mTrueColorBlue = 0;
        this.mIsClearPattern = false;
        this.mNotDraw = (boolean[][]) Array.newInstance(Boolean.TYPE, new int[]{3, 3});
        this.mTranslateY = (float[][]) Array.newInstance(Float.TYPE, new int[]{3, 3});
        this.mPaintAlpha = new int[][]{new int[]{255, 255, 255}, new int[]{255, 255, 255}, new int[]{255, 255, 255}};
        Resources resources = getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OppoLockPatternView, defStyle, 0);
        String aspect = a.getString(0);
        this.mViewWidth = resources.getDimensionPixelOffset(201655446);
        this.mViewHeight = resources.getDimensionPixelOffset(201655447);
        int defaultTrueColor = resources.getColor(201720873);
        int defaultWrongColor = resources.getColor(201720874);
        this.mDefaultCircleColor = a.getColor(1, resources.getColor(201720834));
        this.mDiameterFactor = a.getFloat(2, this.mDefaultDiameterFactor);
        this.mPathTrueColor = a.getColor(3, defaultTrueColor);
        this.mPathWrongColor = a.getColor(4, defaultWrongColor);
        this.mWrongCircleColor = ColorContextUtil.getAttrColor(context, 201392720);
        this.mTrueCircleColor = ColorContextUtil.getAttrColor(context, 201392700);
        this.mLineStartWidth = (float) resources.getDimensionPixelOffset(201655485);
        int defaultFingerDia = resources.getDimensionPixelOffset(201655461);
        int largeFingerDia = resources.getDimensionPixelOffset(201655462);
        this.mSetLockPathWidth = (float) resources.getDimensionPixelOffset(201655486);
        this.mDefaultFingerRadius = ((float) defaultFingerDia) / 2.0f;
        this.mLargeFingerRadius = ((float) largeFingerDia) / 2.0f;
        this.mFingerRadius = this.mDefaultFingerRadius;
        this.mDefaultRadius = this.mDefaultFingerRadius;
        this.mLargeRingRadius = ((float) resources.getDimensionPixelOffset(201655487)) / 2.0f;
        this.mSmallRingRadius = ((float) resources.getDimensionPixelOffset(201655488)) / 2.0f;
        this.mSmallRingWidth = (float) resources.getDimensionPixelOffset(201655489);
        this.mLargeRingWidth = (float) resources.getDimensionPixelOffset(201655490);
        this.mCirLargeWidth = ((float) resources.getDimensionPixelOffset(201655491)) / 2.0f;
        this.mLadderStartColor = context.getColor(201720881);
        this.mLadderEndColor = ColorContextUtil.getAttrColor(context, 201392700);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (isOrientationPortrait(context)) {
            displayWidth = dm.widthPixels;
        } else {
            displayWidth = dm.heightPixels;
        }
        this.mLineEndWidth = (float) getDimensionOrFraction(a, 5, displayWidth, displayWidth);
        a.recycle();
        if ("square".equals(aspect)) {
            this.mAspect = 0;
        } else if ("lock_width".equals(aspect)) {
            this.mAspect = 1;
        } else if ("lock_height".equals(aspect)) {
            this.mAspect = 2;
        } else {
            this.mAspect = 0;
        }
        setClickable(true);
        initPaint();
        initAlphaColor();
        this.mExploreByTouchHelper = new PatternExploreByTouchHelper(this);
        setAccessibilityDelegate(this.mExploreByTouchHelper);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
    }

    public boolean isInStealthMode() {
        return this.mInStealthMode;
    }

    public boolean isTactileFeedbackEnabled() {
        return this.mEnableHapticFeedback;
    }

    public void setInStealthMode(boolean inStealthMode) {
        this.mInStealthMode = inStealthMode;
    }

    public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
        this.mEnableHapticFeedback = tactileFeedbackEnabled;
    }

    public void setOnPatternListener(OnPatternListener onPatternListener) {
        this.mOnPatternListener = onPatternListener;
    }

    public void setPattern(DisplayMode displayMode, List<Cell> pattern) {
        this.mPattern.clear();
        this.mPattern.addAll(pattern);
        clearPatternDrawLookup();
        for (Cell cell : pattern) {
            this.mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        }
        setDisplayMode(displayMode);
    }

    public void setDisplayMode(DisplayMode displayMode) {
        if (displayMode == DisplayMode.Wrong && this.mPattern.size() > 1) {
            performHapticFeedback(300);
        }
        if (displayMode == DisplayMode.Wrong && !this.mPattern.isEmpty()) {
            int count = this.mPattern.size();
            for (int i = 0; i < count; i++) {
                Cell cell = (Cell) this.mPattern.get(i);
                if (cell.zoomInAnim != null && cell.zoomInAnim.isRunning()) {
                    cell.zoomInAnim.end();
                }
                cell.startZoomOutWrong(0.0f, 1.0f, new AnimUpdateListener(cell));
            }
            this.mIsClearPattern = false;
            startWrongPathAnim(0.0f, STARTALPHA);
            if (DEBUG) {
                Log.d(TAG, "display mode is wrong, pattern.size() = " + count);
            }
        }
        this.mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.Animate) {
            if (this.mPattern.size() == 0) {
                throw new IllegalStateException("you must have a pattern to animate if you want to set the display mode to animate");
            }
            this.mAnimatingPeriodStart = SystemClock.elapsedRealtime();
            Cell first = (Cell) this.mPattern.get(0);
            this.mInProgressX = getCenterXForColumn(first.getColumn());
            this.mInProgressY = getCenterYForRow(first.getRow());
            clearPatternDrawLookup();
        }
        invalidate();
    }

    private void notifyCellAdded() {
        if (this.mOnPatternListener != null) {
            this.mOnPatternListener.onPatternCellAdded(this.mPattern);
        }
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private void notifyPatternStarted() {
        sendAccessEvent(201589852);
        if (this.mOnPatternListener != null) {
            this.mOnPatternListener.onPatternStart();
            if (DEBUG) {
                Log.d(TAG, "onPatternStart");
            }
        }
    }

    private void notifyPatternDetected() {
        sendAccessEvent(201589853);
        if (this.mOnPatternListener != null) {
            this.mOnPatternListener.onPatternDetected(this.mPattern);
            if (DEBUG) {
                Log.d(TAG, "onPatternDetected = " + this.mPattern.size());
            }
        }
    }

    private void notifyPatternCleared() {
        sendAccessEvent(201589854);
        if (this.mOnPatternListener != null) {
            this.mOnPatternListener.onPatternCleared();
            if (DEBUG) {
                Log.d(TAG, "onPatternCleared");
            }
        }
    }

    public void clearPattern() {
        resetPattern();
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        return super.dispatchHoverEvent(event) | this.mExploreByTouchHelper.dispatchHoverEvent(event);
    }

    private void resetPattern() {
        this.mPattern.clear();
        clearPatternDrawLookup();
        this.mPatternDisplayMode = DisplayMode.Correct;
        this.mIsDrag = false;
        this.mIsDrawLadder = false;
        this.mStretchAngle = 0.0d;
        this.mZoomOutPositon = 0;
        this.mIsClearPattern = false;
        invalidate();
    }

    private void clearPatternDrawLookup() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.mPatternDrawLookup[i][j] = false;
                Cell.sCells[i][j].zoomInAnim = null;
                Cell.sCells[i][j].zoomOutAnim = null;
                Cell.sCells[i][j].isfillInGapCell = false;
                Cell.sCells[i][j].zoomOutWrongAnim = null;
                Cell.sCells[i][j].shrinkAnim = null;
                Cell.sCells[i][j].zoomInStart = false;
                Cell.sCells[i][j].zoomOut = false;
            }
        }
    }

    public void disableInput() {
        this.mInputEnabled = false;
    }

    public void enableInput() {
        this.mInputEnabled = true;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int width = (w - this.mPaddingLeft) - this.mPaddingRight;
        this.mSquareWidth = ((float) this.mViewWidth) / 3.0f;
        this.mSquareHeight = ((float) ((h - this.mPaddingTop) - this.mPaddingBottom)) / 3.0f;
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case Integer.MIN_VALUE:
                return Math.max(specSize, desired);
            case 0:
                return desired;
            default:
                return specSize;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minimumWidth = getSuggestedMinimumWidth();
        int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);
        switch (this.mAspect) {
            case 0:
                viewWidth = Math.min(viewWidth, viewHeight);
                break;
            case 1:
                viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case 2:
                viewWidth = Math.min(viewWidth, viewHeight);
                break;
        }
        setMeasuredDimension(this.mViewWidth, this.mViewHeight);
    }

    private Cell detectAndAddHit(float x, float y) {
        Cell cell = checkForNewHit(x, y);
        if (cell == null) {
            return null;
        }
        if (DEBUG) {
            Log.d(TAG, "detectAndAddHit cell = " + cell);
        }
        cell.isfillInGapCell = false;
        Cell fillInGapCell = null;
        ArrayList<Cell> pattern = this.mPattern;
        if (!pattern.isEmpty()) {
            Cell lastCell = (Cell) pattern.get(pattern.size() - 1);
            int dRow = cell.row - lastCell.row;
            int dColumn = cell.column - lastCell.column;
            int fillInRow = lastCell.row;
            int fillInColumn = lastCell.column;
            if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                fillInRow = lastCell.row + (dRow > 0 ? 1 : -1);
            }
            if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                fillInColumn = lastCell.column + (dColumn > 0 ? 1 : -1);
            }
            fillInGapCell = Cell.of(fillInRow, fillInColumn);
        }
        if (!(fillInGapCell == null || (this.mPatternDrawLookup[fillInGapCell.row][fillInGapCell.column] ^ 1) == 0)) {
            fillInGapCell.isfillInGapCell = true;
            addCellToPattern(fillInGapCell);
        }
        addCellToPattern(cell);
        if (this.mEnableHapticFeedback) {
            performHapticFeedback(1, 3);
        }
        return cell;
    }

    private void addCellToPattern(Cell newCell) {
        this.mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
        this.mPattern.add(newCell);
        if (DEBUG) {
            Log.d(TAG, "addCellToPattern = " + newCell + ", size = " + this.mPattern.size());
        }
        notifyCellAdded();
    }

    private Cell checkForNewHit(float x, float y) {
        int rowHit = getRowHit(y);
        if (rowHit < 0) {
            return null;
        }
        int columnHit = getColumnHit(x);
        if (columnHit >= 0 && !this.mPatternDrawLookup[rowHit][columnHit]) {
            return Cell.of(rowHit, columnHit);
        }
        return null;
    }

    private int getRowHit(float y) {
        float squareHeight = this.mSquareHeight;
        float hitSize = squareHeight * this.mHitFactor;
        float offset = ((float) this.mPaddingTop) + ((squareHeight - hitSize) / 2.0f);
        for (int i = 0; i < 3; i++) {
            float hitTop = offset + (((float) i) * squareHeight);
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i;
            }
        }
        return -1;
    }

    private int getColumnHit(float x) {
        float squareWidth = this.mSquareWidth;
        float hitSize = squareWidth * this.mHitFactor;
        float offset = ((float) this.mPaddingLeft) + ((squareWidth - hitSize) / 2.0f);
        for (int i = 0; i < 3; i++) {
            float hitLeft = offset + (((float) i) * squareWidth);
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }
        return -1;
    }

    public boolean onHoverEvent(MotionEvent event) {
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

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mInputEnabled || (isEnabled() ^ 1) != 0) {
            return false;
        }
        switch (event.getAction()) {
            case 0:
                handleActionDown(event);
                return true;
            case 1:
                handleActionUp();
                return true;
            case 2:
                handleActionMove(event);
                return true;
            case 3:
                if (this.mPatternInProgress) {
                    setPatternInProgress(false);
                    resetPattern();
                    notifyPatternCleared();
                }
                return true;
            default:
                return false;
        }
    }

    private void setPatternInProgress(boolean progress) {
        this.mPatternInProgress = progress;
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private void handleActionMove(MotionEvent event) {
        int historySize = event.getHistorySize();
        int i = 0;
        while (i < historySize + 1) {
            Cell cell;
            float x = i < historySize ? event.getHistoricalX(i) : event.getX();
            float y = i < historySize ? event.getHistoricalY(i) : event.getY();
            int patternSizePreHitDetect = this.mPattern.size();
            Cell hitCell = detectAndAddHit(x, y);
            if (hitCell != null) {
                hitCell.startZoomOut(0.0f, 1.0f, new AnimUpdateListener(hitCell), hitCell, this);
                Cell cell2 = null;
                cell = null;
                if (this.mPattern.size() - 2 >= 0) {
                    cell2 = (Cell) this.mPattern.get(this.mPattern.size() - 2);
                }
                if (this.mPattern.size() - 3 >= 0) {
                    cell = (Cell) this.mPattern.get(this.mPattern.size() - 3);
                }
                if (!(cell == null || cell2 == null || !cell2.isfillInGapCell)) {
                    cell.startZoomIn(1.0f, 0.0f, new AnimUpdateListener(cell), cell, this);
                }
                if (cell2 != null) {
                    cell2.startZoomIn(1.0f, 0.0f, new AnimUpdateListener(cell2), cell2, this);
                }
            }
            int patternSize = this.mPattern.size();
            if (hitCell != null && patternSize == 1) {
                this.mPatternInProgress = true;
                notifyPatternStarted();
            }
            float dx = Math.abs(x - this.mInProgressX);
            float dy = Math.abs(y - this.mInProgressY);
            if (dx > 0.0f || dy > 0.0f) {
                this.mIsDrag = true;
                float oldX = this.mInProgressX;
                float oldY = this.mInProgressY;
                this.mInProgressX = x;
                this.mInProgressY = y;
                if (this.mPatternInProgress && patternSize > 0) {
                    float left;
                    float right;
                    float top;
                    float bottom;
                    ArrayList<Cell> pattern = this.mPattern;
                    float radius = (this.mSquareWidth * this.mDiameterFactor) * 0.5f;
                    cell = (Cell) pattern.get(patternSize - 1);
                    float startX = getCenterXForColumn(cell.column);
                    float startY = getCenterYForRow(cell.row);
                    Rect invalidateRect = this.mInvalidate;
                    if (startX < x) {
                        left = startX;
                        right = x;
                    } else {
                        left = x;
                        right = startX;
                    }
                    if (startY < y) {
                        top = startY;
                        bottom = y;
                    } else {
                        top = y;
                        bottom = startY;
                    }
                    invalidateRect.set((int) (left - radius), (int) (top - radius), (int) (right + radius), (int) (bottom + radius));
                    if (startX < oldX) {
                        left = startX;
                        right = oldX;
                    } else {
                        left = oldX;
                        right = startX;
                    }
                    if (startY < oldY) {
                        top = startY;
                        bottom = oldY;
                    } else {
                        top = oldY;
                        bottom = startY;
                    }
                    invalidateRect.union((int) (left - radius), (int) (top - radius), (int) (right + radius), (int) (bottom + radius));
                    if (hitCell != null) {
                        startX = getCenterXForColumn(hitCell.column);
                        startY = getCenterYForRow(hitCell.row);
                        if (patternSize >= 2) {
                            hitCell = (Cell) pattern.get((patternSize - 1) - (patternSize - patternSizePreHitDetect));
                            oldX = getCenterXForColumn(hitCell.column);
                            oldY = getCenterYForRow(hitCell.row);
                            if (startX < oldX) {
                                left = startX;
                                right = oldX;
                            } else {
                                left = oldX;
                                right = startX;
                            }
                            if (startY < oldY) {
                                top = startY;
                                bottom = oldY;
                            } else {
                                top = oldY;
                                bottom = startY;
                            }
                        } else {
                            right = startX;
                            left = startX;
                            bottom = startY;
                            top = startY;
                        }
                        float widthOffset = this.mSquareWidth / 2.0f;
                        float heightOffset = this.mSquareHeight / 2.0f;
                        invalidateRect.set((int) (left - widthOffset), (int) (top - heightOffset), (int) (right + widthOffset), (int) (bottom + heightOffset));
                    }
                }
                invalidate();
            }
            i++;
        }
    }

    private void sendAccessEvent(int resId) {
        announceForAccessibility(this.mContext.getString(resId));
    }

    private void handleActionUp() {
        Cell cell = null;
        if (!this.mPattern.isEmpty() && this.mPattern.size() - 1 >= 0) {
            cell = (Cell) this.mPattern.get(this.mPattern.size() - 1);
        }
        if (cell != null) {
            cell.startZoomIn(1.0f, 0.0f, new AnimUpdateListener(cell), cell, this);
        }
        if (!this.mPattern.isEmpty()) {
            setPatternInProgress(false);
            notifyPatternDetected();
            invalidate();
        }
    }

    private void handleActionDown(MotionEvent event) {
        resetPattern();
        float x = event.getX();
        float y = event.getY();
        Cell hitCell = detectAndAddHit(x, y);
        if (hitCell != null) {
            hitCell.startZoomOut(0.0f, 1.0f, new AnimUpdateListener(hitCell), hitCell, this);
        }
        if (hitCell != null) {
            setPatternInProgress(true);
            this.mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        } else if (this.mPatternInProgress) {
            setPatternInProgress(false);
            notifyPatternCleared();
        }
        if (hitCell != null) {
            float startX = getCenterXForColumn(hitCell.column);
            float startY = getCenterYForRow(hitCell.row);
            float widthOffset = this.mSquareWidth / 2.0f;
            float heightOffset = this.mSquareHeight / 2.0f;
            invalidate((int) (startX - widthOffset), (int) (startY - heightOffset), (int) (startX + widthOffset), (int) (startY + heightOffset));
        }
        this.mInProgressX = x;
        this.mInProgressY = y;
    }

    private float getCenterXForColumn(int column) {
        return (((float) this.mPaddingLeft) + (((float) column) * this.mSquareWidth)) + (this.mSquareWidth / 2.0f);
    }

    private float getCenterYForRow(int row) {
        return (((float) this.mPaddingTop) + (((float) row) * this.mSquareHeight)) + (this.mSquareHeight / 2.0f);
    }

    protected void onDraw(Canvas canvas) {
        int i;
        Cell cell;
        float centerX;
        float centerY;
        float topY;
        changePathPaintColor();
        ArrayList<Cell> pattern = this.mPattern;
        int count = pattern.size();
        boolean[][] drawLookup = this.mPatternDrawLookup;
        ArrayList<Float> point = new ArrayList();
        ArrayList<Float> point1 = new ArrayList();
        if (this.mPatternDisplayMode == DisplayMode.Animate) {
            int spotInCycle = ((int) (SystemClock.elapsedRealtime() - this.mAnimatingPeriodStart)) % ((count + 1) * MILLIS_PER_CIRCLE_ANIMATING);
            int numCircles = spotInCycle / MILLIS_PER_CIRCLE_ANIMATING;
            clearPatternDrawLookup();
            for (i = 0; i < numCircles; i++) {
                cell = (Cell) pattern.get(i);
                drawLookup[cell.getRow()][cell.getColumn()] = true;
            }
            boolean needToUpdateInProgressPoint = numCircles > 0 && numCircles < count;
            if (needToUpdateInProgressPoint) {
                float percentageOfNextCircle = ((float) (spotInCycle % MILLIS_PER_CIRCLE_ANIMATING)) / 700.0f;
                Cell currentCell = (Cell) pattern.get(numCircles - 1);
                centerX = getCenterXForColumn(currentCell.column);
                centerY = getCenterYForRow(currentCell.row);
                Cell nextCell = (Cell) pattern.get(numCircles);
                float dy = percentageOfNextCircle * (getCenterYForRow(nextCell.row) - centerY);
                this.mInProgressX = centerX + (percentageOfNextCircle * (getCenterXForColumn(nextCell.column) - centerX));
                this.mInProgressY = centerY + dy;
            }
            invalidate();
        }
        float squareWidth = this.mSquareWidth;
        float squareHeight = this.mSquareHeight;
        Path currentPath = this.mCurrentPath;
        currentPath.rewind();
        int paddingTop = this.mPaddingTop;
        int paddingLeft = this.mPaddingLeft;
        for (i = 0; i < 3; i++) {
            topY = ((float) paddingTop) + (((float) i) * squareHeight);
            for (int j = 0; j < 3; j++) {
                drawCircle(canvas, (int) (((float) paddingLeft) + (((float) j) * squareWidth)), (int) topY, i, j, drawLookup[i][j]);
            }
        }
        boolean drawPath = !this.mInStealthMode || this.mPatternDisplayMode == DisplayMode.Wrong;
        if (drawPath) {
            for (i = 0; i < count - 1; i++) {
                cell = (Cell) pattern.get(i);
                Cell next = (Cell) pattern.get(i + 1);
                if (!drawLookup[next.row][next.column]) {
                    break;
                }
                float f = ((float) paddingLeft) + (((float) cell.column) * squareWidth);
                topY = ((float) paddingTop) + (((float) cell.row) * squareHeight);
            }
        }
        if (drawPath && this.mIsDrag) {
            boolean anyCircles = false;
            for (i = 0; i < count; i++) {
                cell = (Cell) pattern.get(i);
                if (!drawLookup[cell.row][cell.column]) {
                    break;
                }
                anyCircles = true;
                centerX = getCenterXForColumn(cell.column);
                centerY = getCenterYForRow(cell.row);
                if (this.mPatternDisplayMode != DisplayMode.Wrong || this.mPatternDisplayMode == DisplayMode.FingerprintMatch || this.mPatternDisplayMode == DisplayMode.FingerprintNoMatch) {
                    if (!((!this.mPatternInProgress && this.mPatternDisplayMode != DisplayMode.Correct && this.mPatternDisplayMode != DisplayMode.Animate) || this.mPatternDisplayMode == DisplayMode.FingerprintMatch || this.mPatternDisplayMode == DisplayMode.FingerprintNoMatch)) {
                        if (this.mIsSetPassword) {
                            drawLinePath(cell, point, point1, centerX, centerY, currentPath, i, true, canvas, pattern, count);
                        } else {
                            this.mIsDrawLadder = true;
                            drawLadder(cell, point1, centerX, centerY, currentPath, i, true, canvas, pattern, count);
                        }
                    }
                } else if (i == 0) {
                    currentPath.moveTo(centerX, centerY);
                } else {
                    currentPath.lineTo(centerX, centerY);
                }
            }
            if ((!this.mIsDrawLadder || this.mPatternDisplayMode == DisplayMode.Wrong) && this.mPatternDisplayMode != DisplayMode.FingerprintMatch && this.mPatternDisplayMode != DisplayMode.FingerprintNoMatch) {
                if ((this.mPatternInProgress || this.mPatternDisplayMode == DisplayMode.Animate) && anyCircles) {
                    currentPath.lineTo(this.mInProgressX, this.mInProgressY);
                }
                canvas.drawPath(currentPath, this.mPathPaint);
            } else if ((this.mPatternInProgress || this.mPatternDisplayMode == DisplayMode.Animate) && anyCircles && this.mPatternDisplayMode != DisplayMode.FingerprintMatch && this.mPatternDisplayMode != DisplayMode.FingerprintNoMatch) {
                this.mStretchPaint.setShader(this.mStretchShader);
                currentPath.lineTo(this.mStretch3[0], this.mStretch3[1]);
                currentPath.lineTo(this.mStretch4[0], this.mStretch4[1]);
                currentPath.lineTo(this.mStretch1[0], this.mStretch1[1]);
                canvas.save();
                canvas.rotate((float) ((this.mStretchAngle * 180.0d) / 3.141592653589793d), getCenterXForColumn(((Cell) pattern.get(this.mZoomOutPositon)).column), getCenterYForRow(((Cell) pattern.get(this.mZoomOutPositon)).row));
                canvas.drawPath(currentPath, this.mStretchPaint);
                canvas.restore();
            }
        }
    }

    private void drawCircle(Canvas canvas, int leftX, int topY, int i, int j, boolean partOfPattern) {
        float value = this.mTranslateY[i][j];
        float squareWidth = this.mSquareWidth;
        int offsetY = (int) ((this.mSquareHeight - (this.mDefaultRadius * 2.0f)) / 2.0f);
        this.mCenterX = ((float) (leftX + ((int) ((squareWidth - (this.mDefaultRadius * 2.0f)) / 2.0f)))) + this.mDefaultRadius;
        this.mCenterY = (((float) (topY + offsetY)) + this.mDefaultRadius) + value;
        if (this.mPatternDisplayMode == DisplayMode.FingerprintMatch) {
            drawFingerCircle(canvas);
        } else if (this.mPatternDisplayMode == DisplayMode.FingerprintNoMatch) {
            drawFingerCircle(canvas);
        } else if (!partOfPattern || (this.mInStealthMode && this.mPatternDisplayMode != DisplayMode.Wrong)) {
            this.mCirclePaint.setColor(this.mDefaultCircleColor);
            if (!this.mNotDraw[i][j]) {
                this.mPaint.setAlpha(this.mPaintAlpha[i][j]);
                drawCircle(canvas);
            }
        } else if (this.mPatternInProgress) {
            this.mCirclePaint.setColor(this.mTrueCircleColor);
            this.mRingPaint.setColor(this.mTrueCircleColor);
            if (!this.mNotDraw[i][j]) {
                this.mPaint.setAlpha(this.mPaintAlpha[i][j]);
                if (this.mIsDrag) {
                    drawCircularRing(canvas, i, j);
                } else {
                    drawCircle(canvas);
                }
            }
        } else if (this.mPatternDisplayMode == DisplayMode.Wrong) {
            if (!this.mNotDraw[i][j]) {
                this.mPaint.setAlpha(this.mPaintAlpha[i][j]);
                drawWrongCircle(canvas, i, j);
            }
        } else if (this.mPatternDisplayMode == DisplayMode.Correct || this.mPatternDisplayMode == DisplayMode.Animate) {
            this.mCirclePaint.setColor(this.mTrueCircleColor);
            this.mRingPaint.setColor(this.mTrueCircleColor);
            if (!this.mNotDraw[i][j]) {
                this.mPaint.setAlpha(this.mPaintAlpha[i][j]);
                if (this.mIsDrag) {
                    drawCircularRing(canvas, i, j);
                } else {
                    drawCircle(canvas);
                }
            }
        } else {
            throw new IllegalStateException("unknown display mode " + this.mPatternDisplayMode);
        }
    }

    private void drawFingerCircle(Canvas canvas) {
        canvas.drawCircle(this.mCenterX, this.mCenterY, this.mFingerRadius, this.mFingerPaint);
    }

    private void drawCircle(Canvas canvas) {
        canvas.drawCircle(this.mCenterX, this.mCenterY, this.mDefaultRadius, this.mCirclePaint);
    }

    private void drawCircularRing(Canvas canvas, int i, int j) {
        this.mRingPaint.setStrokeWidth(Cell.sCells[i][j].ringWidth);
        if (!(Cell.sCells[i][j].zoomInAnim == null || (Cell.sCells[i][j].zoomInAnim.isRunning() ^ 1) == 0 || (this.mIsSetPassword ^ 1) == 0)) {
            this.mRingPaint.setColor(Color.rgb(Cell.sCells[i][j].trueColorRed, Cell.sCells[i][j].trueColorGreen, Cell.sCells[i][j].trueColorBlue));
        }
        canvas.drawCircle(this.mCenterX, this.mCenterY, Cell.sCells[i][j].ringRadius, this.mRingPaint);
    }

    private void drawWrongCircle(Canvas canvas, int i, int j) {
        this.mCirclePaint.setColor(Color.rgb(Cell.sCells[i][j].wrongColorRed, Cell.sCells[i][j].wrongColorGreen, Cell.sCells[i][j].wrongColorBlue));
        canvas.drawCircle(this.mCenterX, this.mCenterY, Cell.sCells[i][j].wrongRadius, this.mCirclePaint);
    }

    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), LockPatternUtils.patternToString(this.mPattern), this.mPatternDisplayMode.ordinal(), this.mInputEnabled, this.mInStealthMode, this.mEnableHapticFeedback, null);
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setPattern(DisplayMode.Correct, ColorLockPatternUtils.stringToPattern(ss.getSerializedPattern()));
        this.mPatternDisplayMode = DisplayMode.values()[ss.getDisplayMode()];
        this.mInputEnabled = ss.isInputEnabled();
        this.mInStealthMode = ss.isInStealthMode();
        this.mEnableHapticFeedback = ss.isTactileFeedbackEnabled();
    }

    private void changePathPaintColor() {
        if (this.mPatternDisplayMode == DisplayMode.Wrong) {
            this.mPathPaint.setColor(this.mPathWrongColor);
            this.mPathPaint.setAlpha(this.mWrongPathAlpha);
        } else if (this.mIsSetPassword) {
            this.mPathPaint.setColor(this.mPathTrueColor);
        } else {
            this.mPathPaint.setColor(this.mLadderEndColor);
            this.mPathPaint.setAlpha(this.mEndAlpha);
        }
    }

    public Animator getFailAnimator() {
        this.mFingerPaint.setColor(this.mWrongCircleColor);
        ValueAnimator animation = ValueAnimator.ofFloat(new float[]{this.mDefaultFingerRadius, this.mLargeFingerRadius});
        animation.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator anim) {
                ColorLockPatternView.this.mFingerRadius = ((Float) anim.getAnimatedValue()).floatValue();
                ColorLockPatternView.this.mFingerPaint.setStrokeWidth(ColorLockPatternView.this.mFingerRadius);
                ColorLockPatternView.this.invalidate();
            }
        });
        animation.setDuration((long) this.mWrongTotalTime);
        animation.setInterpolator(this.mWrongInterpolator);
        return animation;
    }

    public void setSuccessFinger() {
        this.mFingerPaint.setColor(this.mTrueCircleColor);
        this.mFingerRadius = this.mDefaultFingerRadius;
        this.mFingerPaint.setStrokeWidth(this.mFingerRadius);
        invalidate();
    }

    public Animator getSuccessAnimator() {
        this.mFingerPaint.setColor(this.mTrueCircleColor);
        ValueAnimator animation = ValueAnimator.ofFloat(new float[]{this.mDefaultFingerRadius, this.mLargeFingerRadius});
        animation.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator anim) {
                ColorLockPatternView.this.mFingerRadius = ((Float) anim.getAnimatedValue()).floatValue();
                ColorLockPatternView.this.mFingerPaint.setStrokeWidth(ColorLockPatternView.this.mFingerRadius);
                ColorLockPatternView.this.invalidate();
            }
        });
        ValueAnimator colorAnim = ValueAnimator.ofInt(new int[]{255, 0});
        colorAnim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator anim) {
                ColorLockPatternView.this.mFingerPaint.setAlpha(((Integer) anim.getAnimatedValue()).intValue());
                ColorLockPatternView.this.invalidate();
            }
        });
        AnimatorSet animSet = new AnimatorSet();
        animSet.setStartDelay((long) this.mTruePauseTime);
        animSet.play(animation).with(colorAnim);
        animSet.setDuration((long) this.mTrueLargeTime);
        animSet.setInterpolator(this.TRUELARGE);
        return animSet;
    }

    public void setBitmapNotDraw() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.mNotDraw[i][j] = true;
            }
        }
    }

    public void setAlphaAnimation(ValueAnimator alphaAnimation, final int i, final int j) {
        int m = i;
        int n = j;
        alphaAnimation.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animatior) {
                ColorLockPatternView.this.mPaintAlpha[i][j] = ((Integer) animatior.getAnimatedValue()).intValue();
                ColorLockPatternView.this.invalidate();
            }
        });
    }

    public void setTranslateAnimation(ValueAnimator translateAnimation, final int i, final int j) {
        int m = i;
        int n = j;
        translateAnimation.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                ColorLockPatternView.this.mNotDraw[i][j] = false;
            }
        });
        translateAnimation.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animatior) {
                ColorLockPatternView.this.mTranslateY[i][j] = ((Float) animatior.getAnimatedValue()).floatValue();
                ColorLockPatternView.this.invalidate();
            }
        });
    }

    public boolean isLongshotUnsupported() {
        return true;
    }

    private void initAlphaColor() {
        this.mWrongColorRed = Color.red(this.mWrongCircleColor);
        this.mWrongColorGreen = Color.green(this.mWrongCircleColor);
        this.mWrongColorBlue = Color.blue(this.mWrongCircleColor);
        this.mDefaultColorRed = Color.red(this.mDefaultCircleColor);
        this.mDefaultColorGreen = Color.green(this.mDefaultCircleColor);
        this.mDefaultColorBlue = Color.blue(this.mDefaultCircleColor);
        this.mTrueColorRed = Color.red(this.mTrueCircleColor);
        this.mTrueColorGreen = Color.green(this.mTrueCircleColor);
        this.mTrueColorBlue = Color.blue(this.mTrueCircleColor);
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

    private boolean isOrientationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == 1;
    }

    private void initPaint() {
        this.mPathPaint.setAntiAlias(true);
        this.mPathPaint.setDither(true);
        this.mPathPaint.setStrokeWidth(this.mSetLockPathWidth);
        this.mPathPaint.setStyle(Style.STROKE);
        this.mPathPaint.setStrokeJoin(Join.ROUND);
        this.mPathPaint.setStrokeCap(Cap.ROUND);
        this.mCirclePaint.setAntiAlias(true);
        this.mCirclePaint.setStrokeWidth(this.mDefaultRadius);
        this.mCirclePaint.setColor(this.mDefaultCircleColor);
        this.mRingPaint.setAntiAlias(true);
        this.mRingPaint.setStyle(Style.STROKE);
        this.mFingerPaint = new Paint();
        this.mFingerPaint.setAntiAlias(true);
        this.mFingerPaint.setStrokeWidth(this.mDefaultFingerRadius);
        this.mShrinkPaint.setStyle(Style.FILL);
        this.mShrinkPaint.setStrokeWidth(1.0f);
        this.mShrinkPaint.setAntiAlias(true);
        this.mShrinkPaint.setDither(true);
        this.mStretchPaint.setStyle(Style.FILL);
        this.mStretchPaint.setStrokeWidth(1.0f);
        this.mStretchPaint.setAntiAlias(true);
        this.mStretchPaint.setDither(true);
        this.mStretchPaint.setStrokeJoin(Join.ROUND);
        this.mStretchPaint.setStrokeCap(Cap.ROUND);
        this.mShrinkPaint.setColor(this.mLadderEndColor);
        this.mStretchPaint.setColor(this.mLadderEndColor);
    }

    private void startWrongPathAnim(float start, float end) {
        ValueAnimator alphaAnim = ValueAnimator.ofFloat(new float[]{start, end});
        alphaAnim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator anim) {
                ColorLockPatternView.this.mWrongPathAlpha = (int) ((((Float) anim.getAnimatedValue()).floatValue() * ((float) (ColorLockPatternView.this.mEndAlpha - ColorLockPatternView.this.mStartAlpha))) + ((float) ColorLockPatternView.this.mStartAlpha));
                ColorLockPatternView.this.invalidate();
            }
        });
        alphaAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (ColorLockPatternView.this.mIsClearPattern) {
                    ColorLockPatternView.this.mPattern.clear();
                    ColorLockPatternView.this.clearPatternDrawLookup();
                    ColorLockPatternView.this.mPatternDisplayMode = DisplayMode.Correct;
                    ColorLockPatternView.this.mIsDrag = false;
                    ColorLockPatternView.this.mIsDrawLadder = false;
                    ColorLockPatternView.this.mStretchAngle = 0.0d;
                    ColorLockPatternView.this.mZoomOutPositon = 0;
                    ColorLockPatternView.this.mIsClearPattern = false;
                    ColorLockPatternView.this.invalidate();
                }
            }
        });
        alphaAnim.setDuration(100);
        alphaAnim.start();
    }

    private void drawLinePath(Cell cell, ArrayList<Float> point, ArrayList<Float> point1, float centerX, float centerY, Path currentPath, int i, boolean anyCircles, Canvas canvas, ArrayList<Cell> pattern, int count) {
        float ringWidth = cell.ringRadius + (cell.ringWidth / 2.0f);
        point.clear();
        point = calculateStart(cell, this.mInProgressX, this.mInProgressY, centerX, centerY, point, ringWidth);
        float frontCenterX;
        if (i != 0) {
            Cell cell2 = cell;
            float f = centerX;
            float f2 = centerY;
            point1 = calculateEnd(cell2, f, f2, getCenterXForColumn(((Cell) pattern.get(i - 1)).column), getCenterYForRow(((Cell) pattern.get(i - 1)).row), point1, ringWidth);
            currentPath.lineTo(((Float) point1.get(0)).floatValue(), ((Float) point1.get(1)).floatValue());
            point1.clear();
            if (cell.zoomInAnim != null && cell.zoomInAnim.isStarted() && i + 1 < count) {
                frontCenterX = getCenterXForColumn(((Cell) pattern.get(i + 1)).column);
                point1 = calculateStart(cell, frontCenterX, getCenterYForRow(((Cell) pattern.get(i + 1)).row), centerX, centerY, point1, ringWidth);
                currentPath.moveTo(((Float) point1.get(0)).floatValue(), ((Float) point1.get(1)).floatValue());
                point1.clear();
            } else if (cell.zoomInAnim == null || (cell.zoomInAnim.isRunning() ^ 1) == 0) {
                currentPath.moveTo(((Float) point.get(0)).floatValue(), ((Float) point.get(1)).floatValue());
                point.clear();
            } else {
                currentPath.moveTo(centerX, centerY);
            }
        } else if (cell.zoomInAnim != null && cell.zoomInAnim.isStarted() && i + 1 < count) {
            frontCenterX = getCenterXForColumn(((Cell) pattern.get(i + 1)).column);
            point1 = calculateStart(cell, frontCenterX, getCenterYForRow(((Cell) pattern.get(i + 1)).row), centerX, centerY, point1, ringWidth);
            currentPath.moveTo(((Float) point1.get(0)).floatValue(), ((Float) point1.get(1)).floatValue());
            point1.clear();
        } else if (cell.zoomInAnim == null || (cell.zoomInAnim.isRunning() ^ 1) == 0) {
            currentPath.moveTo(((Float) point.get(0)).floatValue(), ((Float) point.get(1)).floatValue());
            point.clear();
        } else {
            currentPath.moveTo(centerX, centerY);
        }
    }

    /* JADX WARNING: Missing block: B:29:0x00e8, code:
            if (((com.color.widget.ColorLockPatternView.Cell) r39.get(r36 - 1)).zoomInAnim.isStarted() == false) goto L_0x00ea;
     */
    /* JADX WARNING: Missing block: B:33:0x010a, code:
            if ((((com.color.widget.ColorLockPatternView.Cell) r39.get(r36 - 1)).zoomInAnim.isRunning() ^ 1) != 0) goto L_0x010c;
     */
    /* JADX WARNING: Missing block: B:34:0x010c, code:
            r38.save();
            r25 = ((com.color.widget.ColorLockPatternView.Cell) r39.get(r36 - 1)).column;
            r28 = ((com.color.widget.ColorLockPatternView.Cell) r39.get(r36 - 1)).row;
            r38.rotate((float) ((r30.mShrinkAngle * 180.0d) / 3.141592653589793d), getCenterXForColumn(r25), getCenterYForRow(r28));
            r30.mShrinkPaint.setShader(r30.mShrinkShader);
            r38.drawPath(r29, r30.mShrinkPaint);
            r38.restore();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void drawLadder(Cell cell, ArrayList<Float> point1, float centerX, float centerY, Path path, int i, boolean anyCircles, Canvas canvas, ArrayList<Cell> pattern, int count) {
        Path shrinkPath = new Path();
        float ringWidth = cell.ringRadius + (cell.ringWidth / 2.0f);
        float halfEndWidth = this.mLineEndWidth / 2.0f;
        float halfStartWidth = this.mLineStartWidth / 2.0f;
        Cell frontCell;
        float frontCenterX;
        if (i == 0) {
            if (i + 1 >= count || ((cell.zoomInAnim == null || !cell.zoomInAnim.isStarted()) && (cell.zoomInAnim == null || (cell.zoomInAnim.isRunning() ^ 1) == 0))) {
                calculateStartPoints(this.mInProgressX, this.mInProgressY, centerX, centerY, halfEndWidth, ringWidth, cell, false);
                path.moveTo(this.mStretch1[0], this.mStretch1[1]);
                path.lineTo(this.mStretch2[0], this.mStretch2[1]);
                this.mZoomOutPositon = i;
            } else {
                frontCell = (Cell) pattern.get(i + 1);
                if (cell.zoomInStart) {
                    cell.zoomInStart = false;
                    this.mIsShrinkAnim = true;
                }
                frontCenterX = getCenterXForColumn(((Cell) pattern.get(i + 1)).column);
                calculateStartPoints(frontCenterX, getCenterYForRow(((Cell) pattern.get(i + 1)).row), centerX, centerY, halfEndWidth, ringWidth, frontCell, true);
            }
        } else if (i < 1) {
            Log.i(TAG, "drawLadder i= " + i);
            return;
        } else {
            float previousX = getCenterXForColumn(((Cell) pattern.get(i - 1)).column);
            float previousY = getCenterYForRow(((Cell) pattern.get(i - 1)).row);
            point1.clear();
            point1 = calculateEnd(cell, centerX, centerY, previousX, previousY, point1, ringWidth);
            calculateProgressPoints(previousX, previousY, ((Float) point1.get(0)).floatValue(), ((Float) point1.get(1)).floatValue(), halfStartWidth, cell, true);
            if (this.mIsShrinkAnim) {
                float endX;
                ValueAnimator valueAnimator = cell.zoomOutAnim;
                if (ValueAnimator.getDurationScale() != 0.0f) {
                    if (cell.isfillInGapCell) {
                        endX = addFillInGapCellX(previousX, previousY, ((Float) point1.get(0)).floatValue(), ((Float) point1.get(1)).floatValue(), cell);
                    } else {
                        endX = cell.shrink3[0];
                    }
                } else if (cell.isfillInGapCell) {
                    endX = cell.shrink3[0];
                } else {
                    endX = addFillInGapCellX(previousX, previousY, ((Float) point1.get(0)).floatValue(), ((Float) point1.get(1)).floatValue(), cell);
                }
                point1.clear();
                cell.setShrinkAnim(cell.shrink1[0], endX, new ShrinkdateListener(cell));
                this.mIsShrinkAnim = false;
            }
            getShrinkShader(previousX, previousY, cell);
            shrinkPath.moveTo(cell.coordinateX, cell.shrink1[1]);
            shrinkPath.lineTo(cell.coordinateX, cell.shrink2[1]);
            shrinkPath.lineTo(cell.shrink3[0], cell.shrink3[1]);
            shrinkPath.lineTo(cell.shrink4[0], cell.shrink4[1]);
            shrinkPath.lineTo(cell.coordinateX, cell.shrink1[1]);
            if (i + 1 >= count || ((cell.zoomInAnim == null || !cell.zoomInAnim.isStarted()) && (cell.zoomInAnim == null || (cell.zoomInAnim.isRunning() ^ 1) == 0))) {
                calculateStartPoints(this.mInProgressX, this.mInProgressY, centerX, centerY, halfEndWidth, ringWidth, cell, false);
                path.rewind();
                path.moveTo(this.mStretch1[0], this.mStretch1[1]);
                path.lineTo(this.mStretch2[0], this.mStretch2[1]);
                this.mZoomOutPositon = i;
            } else {
                frontCell = (Cell) pattern.get(i + 1);
                if (cell.zoomInStart) {
                    cell.zoomInStart = false;
                    this.mIsShrinkAnim = true;
                }
                frontCenterX = getCenterXForColumn(((Cell) pattern.get(i + 1)).column);
                calculateStartPoints(frontCenterX, getCenterYForRow(((Cell) pattern.get(i + 1)).row), centerX, centerY, halfEndWidth, ringWidth, frontCell, true);
            }
        }
        if ((this.mPatternInProgress || this.mPatternDisplayMode == DisplayMode.Animate) && anyCircles) {
            calculateProgressPoints(centerX, centerY, this.mInProgressX, this.mInProgressY, halfStartWidth, cell, false);
        }
        if (i >= 1 && i < count) {
            if (((Cell) pattern.get(i - 1)).zoomInAnim != null) {
            }
            if (((Cell) pattern.get(i - 1)).zoomInAnim != null) {
            }
        }
    }

    private void getShrinkShader(float centerX, float centerY, Cell cell) {
        this.mShrinkShader = new LinearGradient(cell.coordinateX, centerY, cell.shrink3[0], centerY, this.mLadderStartColor, this.mLadderEndColor, TileMode.REPEAT);
    }

    private float addFillInGapCellX(float centerX, float centerY, float progressX, float progressY, Cell cell) {
        if (progressX - centerX == 0.0f) {
            return cell.shrink3[0] + this.mSmallRingWidth;
        }
        if (progressY - centerY == 0.0f) {
            if (progressX - centerX > 0.0f) {
                return cell.shrink3[0] + this.mSmallRingWidth;
            }
            return cell.shrink3[0] - this.mSmallRingWidth;
        } else if (progressX - centerX > 0.0f && progressY - centerY > 0.0f) {
            return cell.shrink3[0] + this.mSmallRingWidth;
        } else {
            if (progressX - centerX > 0.0f && progressY - centerY < 0.0f) {
                return cell.shrink3[0] + this.mSmallRingWidth;
            }
            if (progressX - centerX < 0.0f && progressY - centerY > 0.0f) {
                return cell.shrink3[0] - this.mSmallRingWidth;
            }
            if (progressX - centerX >= 0.0f || progressY - centerY >= 0.0f) {
                return 0.0f;
            }
            return cell.shrink3[0] - this.mSmallRingWidth;
        }
    }

    private void calculateProgressPoints(float centerX, float centerY, float progressX, float progressY, float halfStartWidth, Cell cell, boolean flag) {
        double angle = 0.0d;
        float[] temp3 = new float[2];
        float[] temp4 = new float[2];
        if (progressX - centerX == 0.0f) {
            if (progressY - centerY > 0.0f) {
                angle = 1.5707963267948966d;
                temp3[1] = centerY - halfStartWidth;
                temp4[1] = centerY + halfStartWidth;
                temp3[0] = Math.abs(progressY - centerY) + centerX;
            } else {
                angle = -1.5707963267948966d;
                temp3[1] = centerY - halfStartWidth;
                temp4[1] = centerY + halfStartWidth;
                temp3[0] = Math.abs(progressY - centerY) + centerX;
            }
            temp4[0] = temp3[0];
        } else if (progressY - centerY == 0.0f) {
            angle = 0.0d;
            if (progressX - centerX > 0.0f) {
                temp3[1] = progressY - halfStartWidth;
                temp4[1] = progressY + halfStartWidth;
                temp3[0] = progressX;
            } else {
                temp3[1] = progressY + halfStartWidth;
                temp4[1] = progressY - halfStartWidth;
                temp3[0] = progressX;
            }
            temp4[0] = temp3[0];
        } else if (progressX - centerX > 0.0f && progressY - centerY > 0.0f) {
            angle = Math.atan((double) (Math.abs(progressY - centerY) / Math.abs(progressX - centerX)));
            temp3[1] = centerY - halfStartWidth;
            temp4[1] = centerY + halfStartWidth;
            temp3[0] = ((float) Math.sqrt((double) (((progressY - centerY) * (progressY - centerY)) + ((progressX - centerX) * (progressX - centerX))))) + centerX;
            temp4[0] = temp3[0];
        } else if (progressX - centerX > 0.0f && progressY - centerY < 0.0f) {
            angle = -Math.atan((double) (Math.abs(progressY - centerY) / Math.abs(progressX - centerX)));
            temp3[1] = centerY - halfStartWidth;
            temp4[1] = centerY + halfStartWidth;
            temp3[0] = ((float) Math.sqrt((double) (((progressY - centerY) * (progressY - centerY)) + ((progressX - centerX) * (progressX - centerX))))) + centerX;
            temp4[0] = temp3[0];
        } else if (progressX - centerX < 0.0f && progressY - centerY > 0.0f) {
            angle = -Math.atan((double) (Math.abs(progressY - centerY) / Math.abs(progressX - centerX)));
            temp3[1] = centerY + halfStartWidth;
            temp4[1] = centerY - halfStartWidth;
            temp3[0] = centerX - ((float) Math.sqrt((double) (((progressY - centerY) * (progressY - centerY)) + ((progressX - centerX) * (progressX - centerX)))));
            temp4[0] = temp3[0];
        } else if (progressX - centerX < 0.0f && progressY - centerY < 0.0f) {
            angle = Math.atan((double) (Math.abs(progressY - centerY) / Math.abs(progressX - centerX)));
            temp3[1] = centerY + halfStartWidth;
            temp4[1] = centerY - halfStartWidth;
            temp3[0] = centerX - ((float) Math.sqrt((double) (((progressY - centerY) * (progressY - centerY)) + ((progressX - centerX) * (progressX - centerX)))));
            temp4[0] = temp3[0];
        }
        if (flag) {
            cell.shrink3[0] = temp3[0];
            cell.shrink3[1] = temp3[1];
            cell.shrink4[0] = temp4[0];
            cell.shrink4[1] = temp4[1];
            this.mShrinkAngle = angle;
            return;
        }
        this.mStretch3[0] = temp3[0];
        this.mStretch3[1] = temp3[1];
        this.mStretch4[0] = temp4[0];
        this.mStretch4[1] = temp4[1];
        this.mStretchAngle = angle;
        this.mStretchShader = new LinearGradient(this.mStretch1[0], centerY, temp3[0], centerY, this.mLadderStartColor, this.mLadderEndColor, TileMode.REPEAT);
    }

    private void calculateStartPoints(float currentX, float currentY, float previousX, float previousY, float halfEndWidth, float width, Cell cell, boolean flag) {
        float[] temp1 = new float[2];
        float[] temp2 = new float[2];
        if (currentX - previousX == 0.0f) {
            temp1[1] = previousY + halfEndWidth;
            temp2[1] = previousY - halfEndWidth;
            temp1[0] = previousX + width;
            temp2[0] = temp1[0];
        } else if (currentY - previousY == 0.0f) {
            if (currentX - previousX > 0.0f) {
                temp1[1] = previousY + halfEndWidth;
                temp2[1] = previousY - halfEndWidth;
                temp1[0] = previousX + width;
                temp2[0] = temp1[0];
            } else {
                temp1[1] = previousY - halfEndWidth;
                temp2[1] = previousY + halfEndWidth;
                temp1[0] = previousX - width;
                temp2[0] = temp1[0];
            }
        } else if ((currentX - previousX > 0.0f && currentY - previousY > 0.0f) || (currentX - previousX > 0.0f && currentY - previousY < 0.0f)) {
            temp1[1] = previousY + halfEndWidth;
            temp2[1] = previousY - halfEndWidth;
            temp1[0] = previousX + width;
            temp2[0] = temp1[0];
        } else if ((currentX - previousX < 0.0f && currentY - previousY > 0.0f) || (currentX - previousX < 0.0f && currentY - previousY < 0.0f)) {
            temp1[1] = previousY - halfEndWidth;
            temp2[1] = previousY + halfEndWidth;
            temp1[0] = previousX - width;
            temp2[0] = temp1[0];
        }
        if (flag) {
            cell.shrink1[0] = temp1[0];
            cell.shrink1[1] = temp1[1];
            cell.shrink2[0] = temp2[0];
            cell.shrink2[1] = temp2[1];
            return;
        }
        this.mStretch1[0] = temp1[0];
        this.mStretch1[1] = temp1[1];
        this.mStretch2[0] = temp2[0];
        this.mStretch2[1] = temp2[1];
    }

    private ArrayList<Float> calculateEnd(Cell cell, float currentX, float currentY, float previousX, float previousY, ArrayList<Float> point, float ringWidth) {
        float x = 0.0f;
        float y = 0.0f;
        if (currentX - previousX == 0.0f) {
            if (currentY - previousY > 0.0f) {
                y = currentY - ringWidth;
            } else {
                y = currentY + ringWidth;
            }
            x = currentX;
        } else if (currentY - previousY == 0.0f) {
            if (currentX - previousX > 0.0f) {
                x = currentX - ringWidth;
            } else {
                x = currentX + ringWidth;
            }
            y = currentY;
        } else {
            double angle = Math.atan((double) (Math.abs(currentY - previousY) / Math.abs(currentX - previousX)));
            double tempX = Math.cos(angle) * ((double) ringWidth);
            double tempY = Math.sin(angle) * ((double) ringWidth);
            if (currentX - previousX > 0.0f && currentY - previousY > 0.0f) {
                x = (float) (((double) currentX) - tempX);
                y = (float) (((double) currentY) - tempY);
            } else if (currentX - previousX > 0.0f && currentY - previousY < 0.0f) {
                x = (float) (((double) currentX) - tempX);
                y = (float) (((double) currentY) + tempY);
            } else if (currentX - previousX < 0.0f && currentY - previousY > 0.0f) {
                x = (float) (((double) currentX) + tempX);
                y = (float) (((double) currentY) - tempY);
            } else if (currentX - previousX < 0.0f && currentY - previousY < 0.0f) {
                x = (float) (((double) currentX) + tempX);
                y = (float) (((double) currentY) + tempY);
            }
        }
        point.add(Float.valueOf(x));
        point.add(Float.valueOf(y));
        return point;
    }

    private ArrayList<Float> calculateStart(Cell cell, float currentX, float currentY, float previousX, float previousY, ArrayList<Float> point, float ringWidth) {
        float x = 0.0f;
        float y = 0.0f;
        if (currentX - previousX == 0.0f) {
            if (currentY - previousY > 0.0f) {
                y = previousY + ringWidth;
            } else {
                y = previousY - ringWidth;
            }
            x = previousX;
        } else if (currentY - previousY == 0.0f) {
            if (currentX - previousX > 0.0f) {
                x = previousX + ringWidth;
            } else {
                x = previousX - ringWidth;
            }
            y = previousY;
        } else {
            double angle = Math.atan((double) (Math.abs(currentY - previousY) / Math.abs(currentX - previousX)));
            double tempX = Math.cos(angle) * ((double) ringWidth);
            double tempY = Math.sin(angle) * ((double) ringWidth);
            if (currentX - previousX > 0.0f && currentY - previousY > 0.0f) {
                x = (float) (((double) previousX) + tempX);
                y = (float) (((double) previousY) + tempY);
            } else if (currentX - previousX > 0.0f && currentY - previousY < 0.0f) {
                x = (float) (((double) previousX) + tempX);
                y = (float) (((double) previousY) - tempY);
            } else if (currentX - previousX < 0.0f && currentY - previousY > 0.0f) {
                x = (float) (((double) previousX) - tempX);
                y = (float) (((double) previousY) + tempY);
            } else if (currentX - previousX < 0.0f && currentY - previousY < 0.0f) {
                x = (float) (((double) previousX) - tempX);
                y = (float) (((double) previousY) - tempY);
            }
        }
        point.add(Float.valueOf(x));
        point.add(Float.valueOf(y));
        return point;
    }

    public void update(float value, Cell cell) {
        cell.trueColorRed = (int) (((float) this.mTrueColorRed) + (((float) (this.mDefaultColorRed - this.mTrueColorRed)) * value));
        cell.trueColorGreen = (int) (((float) this.mTrueColorGreen) + (((float) (this.mDefaultColorGreen - this.mTrueColorGreen)) * value));
        cell.trueColorBlue = (int) (((float) this.mTrueColorBlue) + (((float) (this.mDefaultColorBlue - this.mTrueColorBlue)) * value));
        invalidate();
    }

    public boolean isSetLockPassword() {
        return this.mIsSetPassword;
    }

    public void setLockPassword(boolean setPassword) {
        this.mIsSetPassword = setPassword;
    }

    public void clearPattern(boolean isAnimation) {
        if (!isAnimation) {
            resetPattern();
        } else if (this.mPatternDisplayMode == DisplayMode.Wrong && !this.mPattern.isEmpty()) {
            int count = this.mPattern.size();
            this.mIsClearPattern = true;
            for (int i = 0; i < count; i++) {
                Cell cell = (Cell) this.mPattern.get(i);
                if (!(cell.zoomOutWrongAnim == null || (cell.zoomOutWrongAnim.isRunning() ^ 1) == 0)) {
                    cell.startZoomOutWrong(1.0f, 0.0f, new AnimUpdateListener(cell));
                }
            }
            startWrongPathAnim(STARTALPHA, 0.0f);
        }
    }
}
