package android.inputmethodservice;

import android.R;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard.Key;
import android.media.AudioManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.IntArray;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.widget.ColorPopupWindow;
import android.widget.ColorPopupWindow.OnPreInvokePopupListener;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.android.internal.widget.ExploreByTouchHelper;
import com.color.util.ColorAccessibilityUtil;
import com.color.util.ColorContextUtil;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class KeyboardView extends View implements OnClickListener {
    public static final int CELL_COLUMN_COUNT = 3;
    public static final int CELL_ROW_COUNT = 4;
    public static final int COMMON_KEY = 0;
    private static final int DEBOUNCE_TIME = 70;
    private static final boolean DEBUG = false;
    private static final boolean DEBUGTAG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int DELAY_AFTER_PREVIEW = 70;
    private static final int DELAY_BEFORE_PREVIEW = 0;
    private static final int DELAY_DISSMISS_POPUPWINDOW = 120;
    public static final int DELETE_KEY = 1;
    private static final int ENABLED = 0;
    private static final int ENABLED_MASK = 32;
    private static final int[] KEY_DELETE = new int[]{-5};
    private static final String LOG_TAG = "KeyboardView";
    private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
    private static final int[] LONG_PRESSABLE_STATE_SET = new int[]{R.attr.state_long_pressable};
    private static int MAX_NEARBY_KEYS = 12;
    private static final int MSG_LONGPRESS = 4;
    private static final int MSG_REMOVE_POPUPWINDOW = 5;
    private static final int MSG_REMOVE_PREVIEW = 2;
    private static final int MSG_REPEAT = 3;
    private static final int MSG_SHOW_PREVIEW = 1;
    private static final int MULTITAP_INTERVAL = 800;
    private static final int NOT_A_KEY = -1;
    public static final int OK_KEY = 2;
    private static final int PFLAG_DRAWABLE_STATE_DIRTY = 1024;
    private static final int PFLAG_PRESSED = 16384;
    private static final int REPEAT_INTERVAL = 50;
    private static final int REPEAT_START_DELAY = 400;
    public static final int SECURITYKEYBOARD = 1;
    private static int STYLEABLE_LENGTH = R.styleable.ViewDrawableStates.length;
    private static final int TARNSLATION = 80;
    private static final int TARNSLATIONTOTAL = 144;
    public static final int UNLOCKKEYBOARD = 2;
    private static int[][] VIEW_SETS = null;
    private static final int VIEW_STATE_ACCELERATED = 64;
    private static final int VIEW_STATE_ACTIVATED = 32;
    private static final int VIEW_STATE_DRAG_CAN_ACCEPT = 256;
    private static final int VIEW_STATE_DRAG_HOVERED = 512;
    private static final int VIEW_STATE_ENABLED = 8;
    private static final int VIEW_STATE_FOCUSED = 4;
    private static final int VIEW_STATE_HOVERED = 128;
    private static final int[] VIEW_STATE_IDS = new int[]{R.attr.state_window_focused, 1, R.attr.state_selected, 2, R.attr.state_focused, 4, R.attr.state_enabled, 8, R.attr.state_pressed, 16, R.attr.state_activated, 32, R.attr.state_accelerated, 64, R.attr.state_hovered, 128, R.attr.state_drag_can_accept, 256, R.attr.state_drag_hovered, 512};
    private static final int VIEW_STATE_PRESSED = 16;
    private static final int VIEW_STATE_SELECTED = 2;
    private static int[][][] VIEW_STATE_SETS = null;
    private static final int VIEW_STATE_WINDOW_FOCUSED = 1;
    private final int DOWNFLAG;
    private final int MOVEFLAG;
    private boolean mAbortKey;
    private String mAccessSureKey;
    private AccessibilityManager mAccessibilityManager;
    private AudioManager mAudioManager;
    private float mAverageWidth;
    private float mBackgroundDimAmount;
    private int mBgBottomOffset;
    private int mBgColor;
    private int mBgHeight;
    private Paint mBgPaint;
    private int mBgTopOffset;
    private int mBgWidth;
    private Bitmap mBuffer;
    private Canvas mCanvas;
    private float mCellHeight;
    private float mCellWidth;
    private Rect mClipRegion;
    private Drawable mColorEndKeyBg;
    private ColorStateList mColorGoTextColor;
    private Drawable mColorLockDeleteKey;
    private Drawable mColorLockShiftKey;
    private Drawable mColorLockSpaceKey;
    private ColorPopupWindow mColorPopup;
    private Drawable mColorPopupBg;
    private int mColorPopupHeight;
    private int mColorPopupWidth;
    private int mColorSecurityBg;
    private int mColorSecurityBgHeight;
    private int mColorSecurityBgWidth;
    private Drawable mColorSecurityKeyBg;
    private int mColorSecurityOffset1;
    private int mColorSecurityOffset2;
    private Drawable mColorSpecialKeyBg;
    private ColorStateList mColorTextColor;
    private String mCompleteLetters;
    private int mCompleteTextColor;
    FontMetricsInt mCompleteTextFontMetrics;
    private TextPaint mCompleteTextPaint;
    private float mCompleteTextSize;
    private final int[] mCoordinates;
    private int mCurrentKey;
    private int mCurrentKeyIndex;
    private long mCurrentKeyTime;
    private Rect mDirtyRect;
    private boolean mDisambiguateSwipe;
    private int[] mDistances;
    private int mDownKey;
    private long mDownTime;
    private boolean mDrawPending;
    private int mEndLabelSize;
    private GestureDetector mGestureDetector;
    Handler mHandler;
    private boolean mHasText;
    private boolean mHeadsetRequiredToHearPasswordsAnnounced;
    private int mHorizontalGap;
    private List<int[]> mIconState;
    private boolean mInMultiTap;
    private Key mInvalidatedKey;
    private boolean mIsDownFlag;
    private boolean mIsEnable;
    private boolean mIsSecurityNumeric;
    private ArrayList<Item> mItem;
    private ArrayList<Drawable> mItemBg;
    private int mItemBgOffset;
    private ColorStateList mItemTextColor;
    private Drawable mKeyBackground;
    private int mKeyBoardType;
    private int[] mKeyIndices;
    private TextView mKeyPreview;
    private int mKeyTextColor;
    private int mKeyTextSize;
    private Keyboard mKeyboard;
    private OnKeyboardActionListener mKeyboardActionListener;
    private boolean mKeyboardChanged;
    private OnKeyboardCharListener mKeyboardCharListener;
    private Drawable mKeyboardDeleteButton;
    private String[] mKeyboardLetters;
    private int[] mKeyboardNumbers;
    private Drawable mKeyboardWellButton;
    private Key[] mKeys;
    private int mLabelTextSize;
    private int mLastCodeX;
    private int mLastCodeY;
    private int mLastKey;
    private long mLastKeyTime;
    private long mLastMoveTime;
    private int mLastSentIndex;
    private long mLastTapTime;
    private int mLastX;
    private int mLastY;
    private int mLetterTextColor;
    FontMetricsInt mLetterTextFontMetrics;
    private TextPaint mLetterTextPaint;
    private float mLetterTextSize;
    private int mLineColor;
    private float mLineOffset;
    private Paint mLinePaint;
    private float mLineWidth;
    private int mLowerLetterSize;
    private KeyboardView mMiniKeyboard;
    private Map<Key, View> mMiniKeyboardCache;
    private View mMiniKeyboardContainer;
    private int mMiniKeyboardOffsetX;
    private int mMiniKeyboardOffsetY;
    private boolean mMiniKeyboardOnScreen;
    private boolean mNeedDrawThreeLines;
    private boolean mNeedShowCompleteKey;
    private boolean mNeedShowWellNumber;
    private float mNumberAndLetterOverlap;
    private int mNumberTextColor;
    FontMetricsInt mNumberTextFontMetrics;
    private TextPaint mNumberTextPaint;
    private float mNumberTextSize;
    private float mNumberTextTopoffOneline;
    private float mNumberTextTopoffThreeLine;
    private float mNumberTextTopoffTwoLine;
    private int mOldPointerCount;
    private float mOldPointerX;
    private float mOldPointerY;
    private Rect mPadding;
    private Paint mPaint;
    private int mPopVerticalOffset;
    private PopupWindow mPopupKeyboard;
    private int mPopupLayout;
    private View mPopupParent;
    private int mPopupPreviewX;
    private int mPopupPreviewY;
    private int mPopupX;
    private int mPopupY;
    private boolean mPossiblePoly;
    private ColorStateList mPressedColor;
    private boolean mPreviewCentered;
    private int mPreviewHeight;
    private StringBuilder mPreviewLabel;
    private int mPreviewOffset;
    private PopupWindow mPreviewPopup;
    private TextView mPreviewText;
    private int mPreviewTextSizeLarge;
    private int mPreviousIndex;
    private int mPreviousKey;
    protected List<Integer> mPrivateFlags;
    private boolean mProximityCorrectOn;
    private int mProximityThreshold;
    private int mRepeatKeyIndex;
    private int mShadowColor;
    private float mShadowRadius;
    private boolean mShowPreview;
    private boolean mShowTouchPoints;
    private int mSpaceLabelSize;
    private ColorStateList mSpecialColor;
    private Drawable mSpecialItemBg;
    private int mSpecialItemSize;
    private int mSpecialKeyHeight;
    private int mSpecialKeyNormalColor;
    private int mSpecialKeywidth;
    private Drawable mSpecialNormalBg;
    private String[] mSpecialSymbols;
    private TextPaint mSpecialTextPaint;
    private int mStartX;
    private int mStartY;
    private String mSureString;
    private int mSwipeThreshold;
    private SwipeTracker mSwipeTracker;
    private int mTapCount;
    private NumberKeyboardExploreTouchHelper mTouchHelper;
    private Typeface mTypeface;
    private CharSequence mUpdateText;
    private int mVerticalCorrection;
    private int mVerticalGap;
    Cell[][] sCells;

    public interface OnKeyboardCharListener {
        void onCharacter(String str, int i);
    }

    public interface OnKeyboardActionListener {
        void onKey(int i, int[] iArr);

        void onPress(int i);

        void onRelease(int i);

        void onText(CharSequence charSequence);

        void swipeDown();

        void swipeLeft();

        void swipeRight();

        void swipeUp();
    }

    public class Cell {
        String cellLettersStr;
        String cellNumberStr;
        int column;
        int row;

        /* synthetic */ Cell(KeyboardView this$0, int row, int column, Cell -this3) {
            this(row, column);
        }

        private Cell(int row, int column) {
            this.cellNumberStr = null;
            this.cellLettersStr = null;
            KeyboardView.this.checkRange(row, column);
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

    private class Item {
        public Drawable itemBg;
        private float mBottom;
        private int mLeft;
        private int mRight;
        private TextPaint mSpecialTextPaint;
        private float mTop;
        public String text;

        public Item() {
            this.mLeft = 0;
            this.mRight = 0;
            this.mTop = 0.0f;
            this.mBottom = 0.0f;
            this.text = null;
            this.itemBg = null;
        }

        public Item(Drawable itemBg, String text) {
            this.mLeft = 0;
            this.mRight = 0;
            this.mTop = 0.0f;
            this.mBottom = 0.0f;
            this.text = null;
            this.itemBg = null;
            this.mSpecialTextPaint = new TextPaint(1);
            this.mSpecialTextPaint.setAntiAlias(true);
            this.mSpecialTextPaint.setTextSize((float) KeyboardView.this.mSpecialItemSize);
            this.text = text;
            this.itemBg = itemBg;
        }

        public void setTop(float Top) {
            this.mTop = Top;
        }

        public float getTop() {
            return this.mTop;
        }

        public void setBottom(float Bottom) {
            this.mBottom = Bottom;
        }

        public float getBottom() {
            return this.mBottom;
        }

        public String getText() {
            if (this.text != null) {
                return this.text;
            }
            return null;
        }

        public Drawable getItemBg() {
            if (this.itemBg != null) {
                return this.itemBg;
            }
            return null;
        }
    }

    private final class NumberKeyboardExploreTouchHelper extends ExploreByTouchHelper {
        private static final int MAX_KEY_ID = 12;
        private static final int SPECIAL_KEY_ID = 10;
        private static final int VIRTUAL_BASE_VIEW_ID = 1;
        int[] codes = new int[KeyboardView.MAX_NEARBY_KEYS];
        private int[] mKeyCodes = new int[]{49, 50, 51, 52, 53, 54, 55, 56, 57, -1, 48, -5};
        private final SparseArray<String> mStrArray = new SparseArray();
        private Rect mTempRect = new Rect();

        public NumberKeyboardExploreTouchHelper(View forView) {
            super(forView);
            for (int i = 1; i < 13; i++) {
                this.mStrArray.put(i, getTextForVirtualView(i));
            }
        }

        protected int getVirtualViewAt(float x, float y) {
            int id = getVirtualViewIdForHover(x, y);
            if (KeyboardView.this.mNeedShowCompleteKey || KeyboardView.this.mNeedShowWellNumber || id != 10) {
                return id;
            }
            return Integer.MIN_VALUE;
        }

        protected void getVisibleVirtualViews(IntArray virtualViewIds) {
            int i = 1;
            while (i < 13) {
                if (KeyboardView.this.mNeedShowCompleteKey || KeyboardView.this.mNeedShowWellNumber || i != 10) {
                    virtualViewIds.add(i);
                }
                i++;
            }
        }

        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.getText().add((String) this.mStrArray.get(virtualViewId));
        }

        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            node.setContentDescription(getTextForVirtualView(virtualViewId));
            node.setText(getTextForVirtualView(virtualViewId));
            node.addAction(AccessibilityAction.ACTION_CLICK);
            node.setFocusable(true);
            node.setClickable(true);
            node.setBoundsInParent(getBoundsForVirtualView(virtualViewId));
        }

        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle bundle) {
            if (action == 16) {
                return onItemClicked(virtualViewId);
            }
            return false;
        }

        boolean onItemClicked(int index) {
            invalidateVirtualView(index);
            sendEventForVirtualView(index, 1);
            int code = this.mKeyCodes[index - 1];
            if (KeyboardView.this.mNeedShowCompleteKey) {
                if (index == 10) {
                    KeyboardView.this.mKeyboardActionListener.onKey(-5, this.codes);
                    KeyboardView.this.mKeyboardActionListener.onRelease(-5);
                } else if (index == 12) {
                    KeyboardView.this.mKeyboardActionListener.onKey(-4, this.codes);
                    KeyboardView.this.mKeyboardActionListener.onRelease(-4);
                } else {
                    KeyboardView.this.mKeyboardActionListener.onKey(code, this.codes);
                    KeyboardView.this.mKeyboardActionListener.onRelease(code);
                }
            } else if (KeyboardView.this.mNeedShowCompleteKey || !KeyboardView.this.mNeedShowWellNumber) {
                if (index != 10) {
                    KeyboardView.this.mKeyboardActionListener.onKey(code, this.codes);
                    KeyboardView.this.mKeyboardActionListener.onRelease(code);
                }
            } else if (index == 10) {
                KeyboardView.this.mKeyboardActionListener.onKey(18, this.codes);
                KeyboardView.this.mKeyboardActionListener.onRelease(18);
            } else {
                KeyboardView.this.mKeyboardActionListener.onKey(code, this.codes);
                KeyboardView.this.mKeyboardActionListener.onRelease(code);
            }
            KeyboardView.this.announceForAccessibility(getTextForVirtualView(index));
            return true;
        }

        private String getTextForVirtualView(int virtualViewId) {
            switch (virtualViewId) {
                case 10:
                    if (KeyboardView.this.mNeedShowWellNumber) {
                        return KeyboardView.this.getContext().getString(201590165);
                    }
                    if (KeyboardView.this.mNeedShowCompleteKey) {
                        return KeyboardView.this.getContext().getString(17039782);
                    }
                    return null;
                case 12:
                    if (KeyboardView.this.mNeedShowCompleteKey) {
                        return KeyboardView.this.mCompleteLetters;
                    }
                    return KeyboardView.this.getContext().getString(17039782);
                default:
                    return String.valueOf(KeyboardView.this.mKeyboardNumbers[virtualViewId - 1]);
            }
        }

        private int getVirtualViewIdForHover(float x, float y) {
            int row = KeyboardView.this.getHoverRow(y);
            return ((row * 3) + KeyboardView.this.getHoverColumn(x)) + 1;
        }

        private Rect getBoundsForVirtualView(int virtualViewId) {
            Rect bounds = this.mTempRect;
            int index = virtualViewId - 1;
            int row = index / 3;
            int column = index % 3;
            bounds.left = (int) (KeyboardView.this.mCellWidth * ((float) column));
            bounds.right = (int) (KeyboardView.this.mCellWidth * ((float) (column + 1)));
            bounds.top = (int) (KeyboardView.this.mCellHeight * ((float) row));
            bounds.bottom = (int) (KeyboardView.this.mCellHeight * ((float) (row + 1)));
            return bounds;
        }
    }

    private static class SwipeTracker {
        static final int LONGEST_PAST_TIME = 200;
        static final int NUM_PAST = 4;
        final long[] mPastTime;
        final float[] mPastX;
        final float[] mPastY;
        float mXVelocity;
        float mYVelocity;

        /* synthetic */ SwipeTracker(SwipeTracker -this0) {
            this();
        }

        private SwipeTracker() {
            this.mPastX = new float[4];
            this.mPastY = new float[4];
            this.mPastTime = new long[4];
        }

        public void clear() {
            this.mPastTime[0] = 0;
        }

        public void addMovement(MotionEvent ev) {
            long time = ev.getEventTime();
            int N = ev.getHistorySize();
            for (int i = 0; i < N; i++) {
                addPoint(ev.getHistoricalX(i), ev.getHistoricalY(i), ev.getHistoricalEventTime(i));
            }
            addPoint(ev.getX(), ev.getY(), time);
        }

        private void addPoint(float x, float y, long time) {
            int drop = -1;
            long[] pastTime = this.mPastTime;
            int i = 0;
            while (i < 4 && pastTime[i] != 0) {
                if (pastTime[i] < time - 200) {
                    drop = i;
                }
                i++;
            }
            if (i == 4 && drop < 0) {
                drop = 0;
            }
            if (drop == i) {
                drop--;
            }
            float[] pastX = this.mPastX;
            float[] pastY = this.mPastY;
            if (drop >= 0) {
                int start = drop + 1;
                int count = (4 - drop) - 1;
                System.arraycopy(pastX, start, pastX, 0, count);
                System.arraycopy(pastY, start, pastY, 0, count);
                System.arraycopy(pastTime, start, pastTime, 0, count);
                i -= drop + 1;
            }
            pastX[i] = x;
            pastY[i] = y;
            pastTime[i] = time;
            i++;
            if (i < 4) {
                pastTime[i] = 0;
            }
        }

        public void computeCurrentVelocity(int units) {
            computeCurrentVelocity(units, Float.MAX_VALUE);
        }

        public void computeCurrentVelocity(int units, float maxVelocity) {
            float max;
            float[] pastX = this.mPastX;
            float[] pastY = this.mPastY;
            long[] pastTime = this.mPastTime;
            float oldestX = pastX[0];
            float oldestY = pastY[0];
            long oldestTime = pastTime[0];
            float accumX = 0.0f;
            float accumY = 0.0f;
            int N = 0;
            while (N < 4 && pastTime[N] != 0) {
                N++;
            }
            for (int i = 1; i < N; i++) {
                int dur = (int) (pastTime[i] - oldestTime);
                if (dur != 0) {
                    float vel = ((pastX[i] - oldestX) / ((float) dur)) * ((float) units);
                    if (accumX == 0.0f) {
                        accumX = vel;
                    } else {
                        accumX = (accumX + vel) * 0.5f;
                    }
                    vel = ((pastY[i] - oldestY) / ((float) dur)) * ((float) units);
                    if (accumY == 0.0f) {
                        accumY = vel;
                    } else {
                        accumY = (accumY + vel) * 0.5f;
                    }
                }
            }
            if (accumX < 0.0f) {
                max = Math.max(accumX, -maxVelocity);
            } else {
                max = Math.min(accumX, maxVelocity);
            }
            this.mXVelocity = max;
            if (accumY < 0.0f) {
                max = Math.max(accumY, -maxVelocity);
            } else {
                max = Math.min(accumY, maxVelocity);
            }
            this.mYVelocity = max;
        }

        public float getXVelocity() {
            return this.mXVelocity;
        }

        public float getYVelocity() {
            return this.mYVelocity;
        }
    }

    static {
        int NUM_BITS = VIEW_STATE_IDS.length / 2;
        if (NUM_BITS != STYLEABLE_LENGTH) {
            throw new IllegalStateException("VIEW_STATE_IDS array length does not match ViewDrawableStates style array");
        }
        int i;
        int j;
        int[] orderedIds = new int[VIEW_STATE_IDS.length];
        for (i = 0; i < STYLEABLE_LENGTH; i++) {
            int viewState = R.styleable.ViewDrawableStates[i];
            for (j = 0; j < VIEW_STATE_IDS.length; j += 2) {
                if (VIEW_STATE_IDS[j] == viewState) {
                    orderedIds[i * 2] = viewState;
                    orderedIds[(i * 2) + 1] = VIEW_STATE_IDS[j + 1];
                }
            }
        }
        VIEW_STATE_SETS = new int[(1 << NUM_BITS)][][];
        VIEW_SETS = new int[(1 << NUM_BITS)][];
        for (i = 0; i < VIEW_SETS.length; i++) {
            VIEW_SETS[i] = new int[Integer.bitCount(i)];
            int pos = 0;
            for (j = 0; j < orderedIds.length; j += 2) {
                if ((orderedIds[j + 1] & i) != 0) {
                    int pos2 = pos + 1;
                    VIEW_SETS[i][pos] = orderedIds[j];
                    pos = pos2;
                }
            }
        }
    }

    private void checkRange(int row, int column) {
        if (row < 0 || row > 3) {
            throw new IllegalArgumentException("row must in 0-3");
        } else if (column < 0 || column > 2) {
            throw new IllegalArgumentException("column myst in 0-2");
        }
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 17891409);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Add for 3.0", property = OppoRomType.ROM)
    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        int i;
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mCurrentKeyIndex = -1;
        this.mCoordinates = new int[2];
        this.mPreviewCentered = false;
        this.mShowPreview = true;
        this.mShowTouchPoints = true;
        this.mCurrentKey = -1;
        this.mDownKey = -1;
        this.mKeyIndices = new int[12];
        this.mRepeatKeyIndex = -1;
        this.mClipRegion = new Rect(0, 0, 0, 0);
        this.mSwipeTracker = new SwipeTracker();
        this.mOldPointerCount = 1;
        this.mDistances = new int[MAX_NEARBY_KEYS];
        this.mPreviewLabel = new StringBuilder(1);
        this.mDirtyRect = new Rect();
        this.mPressedColor = null;
        this.mBgWidth = 0;
        this.mBgHeight = 0;
        this.mSpecialColor = null;
        this.mSpecialKeyNormalColor = -1;
        this.mKeyBoardType = 0;
        this.mColorSecurityKeyBg = null;
        this.mColorSpecialKeyBg = null;
        this.mColorEndKeyBg = null;
        this.mTypeface = null;
        this.mColorPopupBg = null;
        this.mHasText = false;
        this.mUpdateText = null;
        this.mLowerLetterSize = 0;
        this.mSpaceLabelSize = 0;
        this.mEndLabelSize = 0;
        this.mColorLockDeleteKey = null;
        this.mColorLockShiftKey = null;
        this.mColorLockSpaceKey = null;
        this.mIsEnable = true;
        this.mPreviousKey = -1;
        this.mIsDownFlag = false;
        this.mPopVerticalOffset = -1;
        this.sCells = (Cell[][]) Array.newInstance(Cell.class, new int[]{4, 3});
        this.mKeyboardNumbers = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, -1, 0, -1};
        this.mKeyboardLetters = null;
        this.mKeyboardDeleteButton = null;
        this.mCompleteLetters = null;
        this.mCellWidth = -1.0f;
        this.mCellHeight = -1.0f;
        this.mNumberTextTopoffOneline = -1.0f;
        this.mNumberTextTopoffTwoLine = -1.0f;
        this.mNumberTextTopoffThreeLine = -1.0f;
        this.mNumberAndLetterOverlap = -1.0f;
        this.mLinePaint = new Paint();
        this.mLineWidth = -1.0f;
        this.mLineColor = -1;
        this.mNumberTextPaint = new TextPaint();
        this.mNumberTextColor = -1;
        this.mNumberTextSize = -1.0f;
        this.mNumberTextFontMetrics = null;
        this.mLetterTextPaint = new TextPaint();
        this.mLetterTextColor = -1;
        this.mLetterTextSize = -1.0f;
        this.mLetterTextFontMetrics = null;
        this.mCompleteTextPaint = new TextPaint();
        this.mCompleteTextColor = -1;
        this.mCompleteTextSize = -1.0f;
        this.mCompleteTextFontMetrics = null;
        this.mNeedShowCompleteKey = false;
        this.mNeedDrawThreeLines = false;
        this.DOWNFLAG = 0;
        this.MOVEFLAG = 1;
        this.mNeedShowWellNumber = false;
        this.mKeyboardWellButton = null;
        this.mSureString = null;
        this.mIsSecurityNumeric = false;
        this.mVerticalGap = -1;
        this.mHorizontalGap = -1;
        this.mSpecialKeywidth = -1;
        this.mSpecialKeyHeight = -1;
        this.mLineOffset = -1.0f;
        this.mSpecialSymbols = null;
        this.mSpecialNormalBg = null;
        this.mBgPaint = new Paint();
        this.mSpecialTextPaint = new TextPaint();
        this.mItemBgOffset = 1;
        this.mSpecialItemSize = -1;
        this.mBgTopOffset = 2;
        this.mBgBottomOffset = 6;
        this.mPreviousIndex = -1;
        this.mAverageWidth = 0.0f;
        this.mItem = new ArrayList();
        this.mItemBg = new ArrayList();
        this.mPrivateFlags = new ArrayList();
        this.mIconState = new ArrayList();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KeyboardView, defStyleAttr, defStyleRes);
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int previewLayout = 0;
        int n = a.getIndexCount();
        for (i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    this.mShadowColor = a.getColor(attr, 0);
                    break;
                case 1:
                    this.mShadowRadius = a.getFloat(attr, 0.0f);
                    break;
                case 2:
                    this.mKeyBackground = a.getDrawable(attr);
                    break;
                case 3:
                    this.mKeyTextSize = a.getDimensionPixelSize(attr, 18);
                    break;
                case 4:
                    this.mLabelTextSize = a.getDimensionPixelSize(attr, 14);
                    break;
                case 5:
                    this.mKeyTextColor = a.getColor(attr, -16777216);
                    break;
                case 6:
                    previewLayout = a.getResourceId(attr, 0);
                    break;
                case 7:
                    this.mPreviewOffset = a.getDimensionPixelOffset(attr, 0);
                    break;
                case 8:
                    this.mPreviewHeight = a.getDimensionPixelSize(attr, 80);
                    break;
                case 9:
                    this.mVerticalCorrection = a.getDimensionPixelOffset(attr, 0);
                    break;
                case 10:
                    this.mPopupLayout = a.getResourceId(attr, 0);
                    break;
                default:
                    break;
            }
        }
        this.mBackgroundDimAmount = this.mContext.obtainStyledAttributes(com.android.internal.R.styleable.Theme).getFloat(2, 0.5f);
        this.mPreviewPopup = new PopupWindow(context);
        if (previewLayout != 0) {
            this.mPreviewText = (TextView) inflate.inflate(previewLayout, null);
            this.mPreviewTextSizeLarge = (int) this.mPreviewText.getTextSize();
            this.mPreviewPopup.setContentView(this.mPreviewText);
            this.mPreviewPopup.setBackgroundDrawable(null);
        } else {
            this.mShowPreview = false;
        }
        this.mPreviewPopup.setTouchable(false);
        this.mPopupKeyboard = new PopupWindow(context);
        this.mPopupKeyboard.setBackgroundDrawable(null);
        this.mPopupParent = this;
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setTextSize(0.0f);
        this.mPaint.setTextAlign(Align.CENTER);
        this.mPaint.setAlpha(255);
        this.mPadding = new Rect(0, 0, 0, 0);
        this.mMiniKeyboardCache = new HashMap();
        if (this.mKeyBackground != null) {
            this.mKeyBackground.getPadding(this.mPadding);
        }
        this.mSwipeThreshold = (int) (getResources().getDisplayMetrics().density * 500.0f);
        this.mDisambiguateSwipe = getResources().getBoolean(17957039);
        this.mAccessibilityManager = AccessibilityManager.getInstance(context);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        resetMultiTap();
        if (ColorContextUtil.isOppoStyle(getContext())) {
            this.mBgColor = getResources().getColor(201720909);
            this.mPressedColor = getResources().getColorStateList(201720870);
            this.mSpecialColor = getResources().getColorStateList(201720871);
            this.mSpecialKeyNormalColor = getResources().getColor(201720908);
            this.mBgWidth = (int) getResources().getDimension(201655568);
            this.mBgHeight = (int) getResources().getDimension(201655569);
            this.mCellWidth = (float) (this.mBgWidth / 3);
            this.mCellHeight = (float) (this.mBgHeight / 4);
            this.mKeyboardLetters = getResources().getStringArray(201785351);
            this.mCompleteLetters = context.getString(17040068);
            this.mKeyboardDeleteButton = getResources().getDrawable(201852209);
            this.mNumberTextTopoffOneline = getResources().getDimension(201655571);
            this.mNumberTextTopoffTwoLine = getResources().getDimension(201655572);
            this.mNumberTextTopoffThreeLine = getResources().getDimension(201655573);
            this.mNumberAndLetterOverlap = getResources().getDimension(201655574);
            this.mLineWidth = getResources().getDimension(201655570);
            this.mLineColor = getResources().getColor(201720907);
            this.mLinePaint.setStrokeWidth(this.mLineWidth);
            this.mLinePaint.setColor(this.mLineColor);
            this.mNumberTextSize = getResources().getDimension(201654434);
            this.mNumberTextColor = getResources().getColor(201720910);
            this.mNumberTextPaint.setTextSize(this.mNumberTextSize);
            this.mNumberTextPaint.setColor(this.mNumberTextColor);
            this.mNumberTextPaint.setAntiAlias(true);
            this.mNumberTextFontMetrics = this.mNumberTextPaint.getFontMetricsInt();
            this.mLetterTextSize = getResources().getDimension(201654402);
            this.mLetterTextColor = this.mNumberTextColor;
            this.mLetterTextPaint.setTextSize(this.mLetterTextSize);
            this.mLetterTextPaint.setColor(this.mLetterTextColor);
            this.mLetterTextPaint.setAntiAlias(true);
            this.mLetterTextFontMetrics = this.mLetterTextPaint.getFontMetricsInt();
            this.mCompleteTextSize = getResources().getDimension(201654416);
            this.mCompleteTextColor = this.mNumberTextColor;
            this.mCompleteTextPaint.setTextSize(this.mCompleteTextSize);
            this.mCompleteTextPaint.setColor(this.mCompleteTextColor);
            this.mCompleteTextPaint.setAntiAlias(true);
            this.mCompleteTextPaint.setFakeBoldText(true);
            this.mCompleteTextFontMetrics = this.mCompleteTextPaint.getFontMetricsInt();
            this.mKeyboardWellButton = getResources().getDrawable(201852210);
            for (i = 0; i < 4; i++) {
                for (int j = 0; j < 3; j++) {
                    this.sCells[i][j] = new Cell(this, i, j, null);
                    this.sCells[i][j].cellLettersStr = this.mKeyboardLetters[(i * 3) + j];
                    if (this.sCells[i][j].cellLettersStr.contains("\n")) {
                        this.mNeedDrawThreeLines = true;
                    }
                    if (this.mKeyboardNumbers[(i * 3) + j] > -1) {
                        this.sCells[i][j].cellNumberStr = String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(number)});
                    }
                }
            }
        }
        if (isNumberUnLockKeyboard()) {
            this.mTouchHelper = new NumberKeyboardExploreTouchHelper(this);
            setAccessibilityDelegate(this.mTouchHelper);
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the security poopupWindow", property = OppoRomType.ROM)
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initGestureDetector();
        if (this.mHandler == null) {
            this.mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            KeyboardView.this.showKey(msg.arg1);
                            return;
                        case 2:
                            KeyboardView.this.mPreviewText.setVisibility(4);
                            return;
                        case 3:
                            if (KeyboardView.this.repeatKey()) {
                                sendMessageDelayed(Message.obtain((Handler) this, 3), 50);
                                return;
                            }
                            return;
                        case 4:
                            KeyboardView.this.openPopupIfRequired((MotionEvent) msg.obj);
                            return;
                        case 5:
                            if (KeyboardView.DEBUGTAG) {
                                Log.i(KeyboardView.LOG_TAG, "onAttachedToWindow");
                            }
                            if (KeyboardView.this.isSecurityKeyboard()) {
                                KeyboardView.this.dismissPopupWindow();
                                return;
                            }
                            return;
                        default:
                            return;
                    }
                }
            };
        }
    }

    private void initGestureDetector() {
        if (this.mGestureDetector == null) {
            this.mGestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
                public boolean onFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
                    if (KeyboardView.this.mPossiblePoly) {
                        return false;
                    }
                    float absX = Math.abs(velocityX);
                    float absY = Math.abs(velocityY);
                    float deltaX = me2.getX() - me1.getX();
                    float deltaY = me2.getY() - me1.getY();
                    int travelX = KeyboardView.this.getWidth() / 2;
                    int travelY = KeyboardView.this.getHeight() / 2;
                    KeyboardView.this.mSwipeTracker.computeCurrentVelocity(1000);
                    float endingVelocityX = KeyboardView.this.mSwipeTracker.getXVelocity();
                    float endingVelocityY = KeyboardView.this.mSwipeTracker.getYVelocity();
                    boolean sendDownKey = false;
                    if (velocityX <= ((float) KeyboardView.this.mSwipeThreshold) || absY >= absX || deltaX <= ((float) travelX)) {
                        if (velocityX >= ((float) (-KeyboardView.this.mSwipeThreshold)) || absY >= absX || deltaX >= ((float) (-travelX))) {
                            if (velocityY >= ((float) (-KeyboardView.this.mSwipeThreshold)) || absX >= absY || deltaY >= ((float) (-travelY))) {
                                if (velocityY > ((float) KeyboardView.this.mSwipeThreshold) && absX < absY / 2.0f && deltaY > ((float) travelY)) {
                                    if (!KeyboardView.this.mDisambiguateSwipe || endingVelocityY >= velocityY / 4.0f) {
                                        KeyboardView.this.swipeDown();
                                        return true;
                                    }
                                    sendDownKey = true;
                                }
                            } else if (!KeyboardView.this.mDisambiguateSwipe || endingVelocityY <= velocityY / 4.0f) {
                                KeyboardView.this.swipeUp();
                                return true;
                            } else {
                                sendDownKey = true;
                            }
                        } else if (!KeyboardView.this.mDisambiguateSwipe || endingVelocityX <= velocityX / 4.0f) {
                            KeyboardView.this.swipeLeft();
                            return true;
                        } else {
                            sendDownKey = true;
                        }
                    } else if (!KeyboardView.this.mDisambiguateSwipe || endingVelocityX >= velocityX / 4.0f) {
                        KeyboardView.this.swipeRight();
                        return true;
                    } else {
                        sendDownKey = true;
                    }
                    if (sendDownKey) {
                        KeyboardView.this.detectAndSendKey(KeyboardView.this.mDownKey, KeyboardView.this.mStartX, KeyboardView.this.mStartY, me1.getEventTime());
                    }
                    return false;
                }
            });
            this.mGestureDetector.setIsLongpressEnabled(false);
        }
    }

    public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
        this.mKeyboardActionListener = listener;
    }

    protected OnKeyboardActionListener getOnKeyboardActionListener() {
        return this.mKeyboardActionListener;
    }

    public void setKeyboard(Keyboard keyboard) {
        if (this.mKeyboard != null) {
            showPreview(-1);
        }
        removeMessages();
        this.mKeyboard = keyboard;
        List<Key> keys = this.mKeyboard.getKeys();
        this.mKeys = (Key[]) keys.toArray(new Key[keys.size()]);
        requestLayout();
        this.mKeyboardChanged = true;
        invalidateAllKeys();
        computeProximityThreshold(keyboard);
        this.mMiniKeyboardCache.clear();
        this.mAbortKey = true;
    }

    public Keyboard getKeyboard() {
        return this.mKeyboard;
    }

    public boolean setShifted(boolean shifted) {
        if (this.mKeyboard == null || !this.mKeyboard.setShifted(shifted)) {
            return false;
        }
        invalidateAllKeys();
        return true;
    }

    public boolean isShifted() {
        if (this.mKeyboard != null) {
            return this.mKeyboard.isShifted();
        }
        return false;
    }

    public void setPreviewEnabled(boolean previewEnabled) {
        this.mShowPreview = previewEnabled;
    }

    public boolean isPreviewEnabled() {
        return this.mShowPreview;
    }

    public void setVerticalCorrection(int verticalOffset) {
    }

    public void setPopupParent(View v) {
        this.mPopupParent = v;
    }

    public void setPopupOffset(int x, int y) {
        this.mMiniKeyboardOffsetX = x;
        this.mMiniKeyboardOffsetY = y;
        if (this.mPreviewPopup.isShowing()) {
            this.mPreviewPopup.dismiss();
        }
    }

    public void setProximityCorrectionEnabled(boolean enabled) {
        this.mProximityCorrectOn = enabled;
    }

    public boolean isProximityCorrectionEnabled() {
        return this.mProximityCorrectOn;
    }

    public void onClick(View v) {
        dismissPopupKeyboard();
    }

    private CharSequence adjustCase(CharSequence label) {
        if (!this.mKeyboard.isShifted() || label == null || label.length() >= 3 || !Character.isLowerCase(label.charAt(0))) {
            return label;
        }
        return label.toString().toUpperCase();
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for security window", property = OppoRomType.ROM)
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mKeyboard == null) {
            setMeasuredDimension(this.mPaddingLeft + this.mPaddingRight, this.mPaddingTop + this.mPaddingBottom);
            return;
        }
        int width = (this.mKeyboard.getMinWidth() + this.mPaddingLeft) + this.mPaddingRight;
        if (isSecurityKeyboard() && width < this.mColorSecurityBgWidth) {
            width = this.mColorSecurityBgWidth;
        }
        if (MeasureSpec.getSize(widthMeasureSpec) < width + 10) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = (this.mKeyboard.getHeight() + this.mPaddingTop) + this.mPaddingBottom;
        if (isSecurityKeyboard() && height < this.mColorSecurityBgHeight) {
            height = this.mColorSecurityBgHeight;
        }
        setMeasuredDimension(width, height);
    }

    private void computeProximityThreshold(Keyboard keyboard) {
        if (keyboard != null) {
            Key[] keys = this.mKeys;
            if (keys != null) {
                int dimensionSum = 0;
                for (Key key : keys) {
                    dimensionSum += Math.min(key.width, key.height) + key.gap;
                }
                if (dimensionSum >= 0 && length != 0) {
                    this.mProximityThreshold = (int) ((((float) dimensionSum) * 1.4f) / ((float) length));
                    this.mProximityThreshold *= this.mProximityThreshold;
                }
            }
        }
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mKeyboard != null) {
            this.mKeyboard.resize(w, h);
        }
        this.mBuffer = null;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for 3.0", property = OppoRomType.ROM)
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isSecurityKeyboard()) {
            canvas.save();
            canvas.clipRect(0, 0, this.mColorSecurityBgWidth, this.mColorSecurityBgHeight);
            canvas.drawColor(this.mColorSecurityBg);
            canvas.restore();
        }
        if (isNumberUnLockKeyboard()) {
            canvas.save();
            canvas.clipRect(0, 0, this.mBgWidth, this.mBgHeight);
            canvas.drawColor(this.mBgColor);
            canvas.restore();
        }
        if (this.mDrawPending || this.mBuffer == null || this.mKeyboardChanged) {
            onBufferDraw();
        }
        canvas.drawBitmap(this.mBuffer, 0.0f, 0.0f, null);
        if (isNumberUnLockKeyboard()) {
            drawBackgroumd(canvas);
            drawLine(canvas);
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 3; j++) {
                    drawCell(canvas, i, j);
                }
            }
        }
        if (!isSecurityKeyboard() || !isSecurityNumericKeyboard()) {
            return;
        }
        if (!getContext().getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism")) {
            drawSpecialSymbol(canvas, 0);
        } else if (getContext().getResources().getConfiguration().orientation == 2) {
            drawSpecialSymbol(canvas, 80);
        } else {
            drawSpecialSymbol(canvas, 0);
        }
    }

    private void drawBackgroumd(Canvas canvas) {
        Key[] keys = this.mKeys;
        if (keys != null) {
            int length = keys.length;
            int i = 0;
            while (i < length) {
                int left;
                int top;
                int[] drawableState = keys[i].getCurrentDrawableState();
                int colums = i % 3;
                int row = i / 3;
                canvas.save();
                canvas.clipRect(((float) colums) * this.mCellWidth, ((float) row) * this.mCellHeight, ((float) (colums + 1)) * this.mCellWidth, ((float) (row + 1)) * this.mCellHeight);
                if (!(i == 9 || i == 11 || this.mPressedColor == null)) {
                    canvas.drawColor(this.mPressedColor.getColorForState(drawableState, 0));
                }
                if (i == 11 && this.mSpecialColor != null) {
                    canvas.drawColor(this.mSpecialKeyNormalColor);
                    canvas.drawColor(this.mSpecialColor.getColorForState(drawableState, 0));
                    if (!this.mNeedShowCompleteKey) {
                        left = ((int) getCenterXForColumn(2)) - (this.mKeyboardDeleteButton.getIntrinsicWidth() / 2);
                        top = (((int) getCenterYForRow(3)) - (this.mKeyboardDeleteButton.getIntrinsicHeight() / 2)) - (((int) this.mNumberAndLetterOverlap) / 6);
                        this.mKeyboardDeleteButton.setBounds(left, top, left + this.mKeyboardDeleteButton.getIntrinsicWidth(), top + this.mKeyboardDeleteButton.getIntrinsicHeight());
                        this.mKeyboardDeleteButton.draw(canvas);
                    }
                }
                if (i == 9) {
                    canvas.drawColor(this.mSpecialKeyNormalColor);
                    if (this.mNeedShowCompleteKey) {
                        canvas.drawColor(this.mSpecialColor.getColorForState(drawableState, 0));
                        left = ((int) getCenterXForColumn(0)) - (this.mKeyboardDeleteButton.getIntrinsicWidth() / 2);
                        top = (((int) getCenterYForRow(3)) - (this.mKeyboardDeleteButton.getIntrinsicHeight() / 2)) - (((int) this.mNumberAndLetterOverlap) / 6);
                        this.mKeyboardDeleteButton.setBounds(left, top, left + this.mKeyboardDeleteButton.getIntrinsicWidth(), top + this.mKeyboardDeleteButton.getIntrinsicHeight());
                        this.mKeyboardDeleteButton.draw(canvas);
                    }
                    if (this.mNeedShowWellNumber && (this.mNeedShowCompleteKey ^ 1) != 0) {
                        canvas.drawColor(this.mSpecialColor.getColorForState(drawableState, 0));
                        left = ((int) getCenterXForColumn(0)) - (this.mKeyboardWellButton.getIntrinsicWidth() / 2);
                        top = (((int) getCenterYForRow(3)) - (this.mKeyboardWellButton.getIntrinsicHeight() / 2)) - (((int) this.mNumberAndLetterOverlap) / 6);
                        this.mKeyboardWellButton.setBounds(left, top, left + this.mKeyboardWellButton.getIntrinsicWidth(), top + this.mKeyboardWellButton.getIntrinsicHeight());
                        this.mKeyboardWellButton.draw(canvas);
                    }
                }
                canvas.restore();
                i++;
            }
        }
    }

    private void drawLine(Canvas canvas) {
        int i;
        int cellWidth = this.mBgWidth / 3;
        int cellHeight = this.mBgHeight / 4;
        for (i = 0; i < 4; i++) {
            canvas.drawLine(0.0f, (float) (cellHeight * i), (float) this.mBgWidth, (float) (cellHeight * i), this.mLinePaint);
        }
        for (i = 0; i < 3; i++) {
            canvas.drawLine((float) ((i + 1) * cellWidth), 0.0f, (float) ((i + 1) * cellWidth), (float) this.mBgHeight, this.mLinePaint);
        }
    }

    private void drawCell(Canvas canvas, int row, int column) {
        float startX = getCenterXForColumn(column);
        float startY = getCenterYForRow(row);
        Cell cell = this.sCells[row][column];
        float numberStrBaseLine = (((((float) row) * this.mCellHeight) + this.mNumberTextTopoffTwoLine) + ((float) ((this.mNumberTextFontMetrics.bottom - this.mNumberTextFontMetrics.top) / 2))) - ((float) this.mNumberTextFontMetrics.bottom);
        if (this.mNeedDrawThreeLines) {
            numberStrBaseLine = (((((float) row) * this.mCellHeight) + this.mNumberTextTopoffThreeLine) + ((float) ((this.mNumberTextFontMetrics.bottom - this.mNumberTextFontMetrics.top) / 2))) - ((float) this.mNumberTextFontMetrics.bottom);
        }
        if (!TextUtils.isEmpty(cell.cellNumberStr)) {
            if ((row * 3) + column == 10 && TextUtils.isEmpty(cell.cellLettersStr)) {
                numberStrBaseLine = (((((float) row) * this.mCellHeight) + this.mNumberTextTopoffOneline) + ((float) ((this.mNumberTextFontMetrics.bottom - this.mNumberTextFontMetrics.top) / 2))) - ((float) this.mNumberTextFontMetrics.bottom);
            }
            float numberStrWidth = this.mNumberTextPaint.measureText(cell.cellNumberStr);
            canvas.drawText(cell.cellNumberStr, startX - (numberStrWidth / 2.0f), numberStrBaseLine, this.mNumberTextPaint);
        }
        float letterStrBaseLine = ((((float) this.mNumberTextFontMetrics.bottom) + numberStrBaseLine) - this.mNumberAndLetterOverlap) - ((float) this.mLetterTextFontMetrics.top);
        if (!TextUtils.isEmpty(cell.cellLettersStr)) {
            float letterLineTextHeight = (float) ((this.mLetterTextFontMetrics.descent - this.mLetterTextFontMetrics.ascent) + this.mLetterTextFontMetrics.leading);
            int index = cell.cellLettersStr.lastIndexOf(10);
            String line1 = cell.cellLettersStr;
            String line2 = null;
            if (index > 0) {
                line1 = cell.cellLettersStr.substring(0, index);
                line2 = cell.cellLettersStr.substring(index);
            }
            canvas.drawText(line1, startX - (this.mLetterTextPaint.measureText(line1) / 2.0f), letterStrBaseLine, this.mLetterTextPaint);
            if (!TextUtils.isEmpty(line2)) {
                canvas.drawText(line2, startX - (this.mLetterTextPaint.measureText(line2) / 2.0f), letterStrBaseLine + letterLineTextHeight, this.mLetterTextPaint);
            }
        }
        if (this.mNeedShowCompleteKey && (row * 3) + column == 11) {
            float baseLine = (((((float) row) * this.mCellHeight) + this.mNumberTextTopoffOneline) + ((float) ((this.mCompleteTextFontMetrics.bottom - this.mCompleteTextFontMetrics.top) / 2))) - ((float) this.mCompleteTextFontMetrics.bottom);
            float completeTextWidth = this.mCompleteTextPaint.measureText(this.mCompleteLetters);
            canvas.drawText(this.mCompleteLetters, startX - (completeTextWidth / 2.0f), baseLine, this.mCompleteTextPaint);
        }
    }

    private float getCenterXForColumn(int column) {
        return (this.mCellWidth / 2.0f) + (this.mCellWidth * ((float) column));
    }

    private float getCenterYForRow(int row) {
        return (this.mCellHeight / 2.0f) + (this.mCellHeight * ((float) row));
    }

    protected void setVariableLength(boolean flag) {
        this.mNeedShowCompleteKey = flag;
    }

    protected void setShowWellKey(boolean flag) {
        this.mNeedShowWellNumber = flag;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the new GUI of the 3.0", property = OppoRomType.ROM)
    private void onBufferDraw() {
        if (this.mBuffer == null || this.mKeyboardChanged) {
            if (this.mBuffer == null || (this.mKeyboardChanged && !(this.mBuffer.getWidth() == getWidth() && this.mBuffer.getHeight() == getHeight()))) {
                this.mBuffer = Bitmap.createBitmap(Math.max(1, getWidth()), Math.max(1, getHeight()), Config.ARGB_8888);
                this.mCanvas = new Canvas(this.mBuffer);
            }
            invalidateAllKeys();
            this.mKeyboardChanged = false;
        }
        Canvas canvas = this.mCanvas;
        canvas.clipRect(this.mDirtyRect, Op.REPLACE);
        if (this.mKeyboard != null) {
            Paint paint = this.mPaint;
            Drawable keyBackground = this.mKeyBackground;
            Rect clipRegion = this.mClipRegion;
            Rect padding = this.mPadding;
            int kbdPaddingLeft = this.mPaddingLeft;
            int kbdPaddingTop = this.mPaddingTop;
            Key[] keys = this.mKeys;
            Key invalidKey = this.mInvalidatedKey;
            paint.setColor(this.mKeyTextColor);
            boolean drawSingleKey = false;
            if (invalidKey != null && canvas.getClipBounds(clipRegion) && (invalidKey.x + kbdPaddingLeft) - 1 <= clipRegion.left && (invalidKey.y + kbdPaddingTop) - 1 <= clipRegion.top && ((invalidKey.x + invalidKey.width) + kbdPaddingLeft) + 1 >= clipRegion.right && ((invalidKey.y + invalidKey.height) + kbdPaddingTop) + 1 >= clipRegion.bottom) {
                drawSingleKey = true;
            }
            canvas.drawColor(0, Mode.CLEAR);
            int keyCount = keys.length;
            int i = 0;
            while (i < keyCount) {
                Key key = keys[i];
                if (!drawSingleKey || invalidKey == key) {
                    String label;
                    int[] drawableState = key.getCurrentDrawableState();
                    if (isSecurityKeyboard()) {
                        int color;
                        if (this.mColorSpecialKeyBg != null && (key.codes[0] == -1 || key.codes[0] == -5 || key.codes[0] == -2 || key.codes[0] == -6 || (isSecurityNumericKeyboard() && (i == keyCount - 2 || i == keyCount - 6 || i == keyCount - 10)))) {
                            keyBackground = this.mColorSpecialKeyBg;
                        } else if (this.mColorSecurityKeyBg != null && key.codes[0] != 10) {
                            keyBackground = this.mColorSecurityKeyBg;
                        } else if (key.codes[0] == 10 && this.mColorEndKeyBg != null) {
                            keyBackground = this.mColorEndKeyBg;
                        }
                        if (this.mUpdateText != null && i == keyCount - 1) {
                            if (key.icon != null) {
                                key.icon = null;
                            }
                            key.label = this.mUpdateText;
                        }
                        if (i == keyCount - 1) {
                            color = this.mColorGoTextColor.getColorForState(drawableState, 0);
                        } else {
                            color = this.mColorTextColor.getColorForState(drawableState, 0);
                        }
                        paint.setColor(color);
                    }
                    if (keyBackground != null) {
                        keyBackground.setState(drawableState);
                    }
                    if (key.label == null) {
                        label = null;
                    } else {
                        label = adjustCase(key.label).toString();
                    }
                    if (keyBackground != null) {
                        Rect bounds = keyBackground.getBounds();
                        if (!(key.width == bounds.right && key.height == bounds.bottom)) {
                            keyBackground.setBounds(0, 0, key.width, key.height);
                        }
                    }
                    canvas.translate((float) (key.x + kbdPaddingLeft), (float) (key.y + kbdPaddingTop));
                    if (keyBackground != null) {
                        keyBackground.draw(canvas);
                    }
                    if (label != null) {
                        float textY;
                        if (isSecurityKeyboard()) {
                            if (Character.isLowerCase(label.charAt(0)) && key.codes[0] != 32) {
                                paint.setTextSize((float) this.mLowerLetterSize);
                                paint.setFakeBoldText(false);
                            } else if (key.codes[0] == 32) {
                                paint.setTextSize((float) this.mSpaceLabelSize);
                                paint.setFakeBoldText(false);
                            } else if (key.codes[0] == -2 || key.codes[0] == 10 || key.codes[0] == -1 || key.codes[0] == -6) {
                                paint.setTextSize((float) this.mEndLabelSize);
                                if (key.codes[0] == 10 || key.codes[0] == -2 || key.codes[0] == -6) {
                                    paint.setFakeBoldText(true);
                                } else {
                                    paint.setFakeBoldText(false);
                                }
                            } else {
                                paint.setTextSize((float) this.mKeyTextSize);
                                if (Character.isDigit(label.charAt(0))) {
                                    paint.setFakeBoldText(true);
                                } else {
                                    paint.setFakeBoldText(false);
                                }
                            }
                            if (this.mTypeface != null) {
                                paint.setTypeface(this.mTypeface);
                            }
                        } else if (label.length() <= 1 || key.codes.length >= 2) {
                            paint.setTextSize((float) this.mKeyTextSize);
                            paint.setTypeface(Typeface.DEFAULT);
                        } else {
                            paint.setTextSize((float) this.mLabelTextSize);
                            paint.setTypeface(Typeface.DEFAULT_BOLD);
                        }
                        if (!isSecurityKeyboard()) {
                            paint.setShadowLayer(this.mShadowRadius, 0.0f, 0.0f, this.mShadowColor);
                        }
                        FontMetricsInt fmi = paint.getFontMetricsInt();
                        if (isSecurityKeyboard()) {
                            textY = (float) ((((key.height - (fmi.bottom - fmi.top)) / 2) - fmi.top) + ((padding.bottom - padding.top) / 2));
                            if (!(key.codes[0] == -2 || key.codes[0] == 10 || key.codes[0] == -6)) {
                                textY = Character.isLowerCase(label.charAt(0)) ? textY - ((float) this.mColorSecurityOffset2) : textY - ((float) this.mColorSecurityOffset1);
                            }
                        } else {
                            textY = (((float) (((key.height - padding.top) - padding.bottom) / 2)) + ((paint.getTextSize() - paint.descent()) / 2.0f)) + ((float) padding.top);
                        }
                        if (isUnLockKeyboard() && i < 10 && DEBUGTAG) {
                            Log.d(LOG_TAG, "ondraw label : " + label + ", key.x = " + key.x + ", " + kbdPaddingLeft);
                        }
                        canvas.drawText(label, (float) ((((key.width - padding.left) - padding.right) / 2) + padding.left), textY, paint);
                        if (!isSecurityKeyboard()) {
                            paint.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                        }
                    } else if (key.icon != null) {
                        if (isUnLockKeyboard() && key.codes[0] == -5 && this.mColorLockDeleteKey != null) {
                            key.icon = this.mColorLockDeleteKey;
                        }
                        if (isUnLockKeyboard() && key.codes[0] == -1 && this.mColorLockShiftKey != null && (this.mKeyboard.isShifted() ^ 1) != 0) {
                            key.icon = this.mColorLockShiftKey;
                        }
                        if (isUnLockKeyboard() && key.codes[0] == 32 && this.mColorLockSpaceKey != null) {
                            key.icon = this.mColorLockSpaceKey;
                        }
                        int drawableX = ((((key.width - padding.left) - padding.right) - key.icon.getIntrinsicWidth()) / 2) + padding.left;
                        int drawableY = ((((key.height - padding.top) - padding.bottom) - key.icon.getIntrinsicHeight()) / 2) + padding.top;
                        if (isSecurityKeyboard()) {
                            drawableY = (key.height - key.icon.getIntrinsicHeight()) / 2;
                        }
                        canvas.translate((float) drawableX, (float) drawableY);
                        key.icon.setBounds(0, 0, key.icon.getIntrinsicWidth(), key.icon.getIntrinsicHeight());
                        key.icon.draw(canvas);
                        canvas.translate((float) (-drawableX), (float) (-drawableY));
                    }
                    canvas.translate((float) ((-key.x) - kbdPaddingLeft), (float) ((-key.y) - kbdPaddingTop));
                }
                i++;
            }
            this.mInvalidatedKey = null;
            if (this.mMiniKeyboardOnScreen) {
                paint.setColor(((int) (this.mBackgroundDimAmount * 255.0f)) << 24);
                canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), paint);
            }
            this.mDrawPending = false;
            this.mDirtyRect.setEmpty();
        }
    }

    /* JADX WARNING: Missing block: B:22:0x0091, code:
            if (r7 < r22.mProximityThreshold) goto L_0x0093;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the BUG:949796 ", property = OppoRomType.ROM)
    private int getKeyIndices(int x, int y, int[] allKeys) {
        Key[] keys = this.mKeys;
        int primaryIndex = -1;
        int closestKey = -1;
        int closestKeyDist = this.mProximityThreshold + 1;
        Arrays.fill(this.mDistances, Integer.MAX_VALUE);
        int[] nearestKeyIndices = this.mKeyboard.getNearestKeys(x, y);
        int keyCount = nearestKeyIndices.length;
        for (int i = 0; i < keyCount; i++) {
            boolean isProximity;
            Key key = keys[nearestKeyIndices[i]];
            int dist = 0;
            boolean isInside = key.isInside(x, y);
            if (isInside) {
                primaryIndex = nearestKeyIndices[i];
            }
            if (isSecurityKeyboard()) {
                if (this.mProximityCorrectOn) {
                    dist = key.squaredDistanceFrom(x, y);
                    if (dist < this.mProximityThreshold) {
                        isProximity = true;
                    }
                }
                isProximity = isInside;
            } else {
                if (this.mProximityCorrectOn) {
                    dist = key.squaredDistanceFrom(x, y);
                }
                if (!isInside) {
                    isProximity = false;
                }
                isProximity = key.codes[0] > 32;
            }
            if (isProximity) {
                int nCodes = key.codes.length;
                if (dist < closestKeyDist) {
                    closestKeyDist = dist;
                    closestKey = nearestKeyIndices[i];
                }
                if (allKeys != null) {
                    int j = 0;
                    while (j < this.mDistances.length) {
                        if (this.mDistances[j] > dist) {
                            System.arraycopy(this.mDistances, j, this.mDistances, j + nCodes, (this.mDistances.length - j) - nCodes);
                            System.arraycopy(allKeys, j, allKeys, j + nCodes, (allKeys.length - j) - nCodes);
                            for (int c = 0; c < nCodes; c++) {
                                allKeys[j + c] = key.codes[c];
                                this.mDistances[j + c] = dist;
                            }
                        } else {
                            j++;
                        }
                    }
                }
            }
        }
        if (primaryIndex == -1) {
            primaryIndex = closestKey;
        }
        if (!isSecurityKeyboard() || !isSecurityNumericKeyboard() || x > this.mSpecialKeywidth + this.mHorizontalGap || y > this.mVerticalGap + this.mSpecialKeyHeight) {
            return primaryIndex;
        }
        return -1;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for delete the c code", property = OppoRomType.ROM)
    private void detectAndSendKey(int index, int x, int y, long eventTime) {
        if (index != -1 && index < this.mKeys.length) {
            Key key = this.mKeys[index];
            if (key.text != null) {
                this.mKeyboardActionListener.onText(key.text);
                this.mKeyboardActionListener.onRelease(-1);
            } else {
                int code = key.codes[0];
                int[] codes = new int[MAX_NEARBY_KEYS];
                Arrays.fill(codes, -1);
                getKeyIndices(x, y, codes);
                if (this.mInMultiTap) {
                    if (this.mTapCount != -1) {
                        this.mKeyboardActionListener.onKey(-5, KEY_DELETE);
                        if (isSecurityKeyboard()) {
                            sendCharToTarget(code, key);
                        }
                    } else {
                        this.mTapCount = 0;
                    }
                    code = key.codes[this.mTapCount];
                }
                if (isNumberUnLockKeyboard()) {
                    if (this.mNeedShowCompleteKey) {
                        if (index == 9) {
                            this.mKeyboardActionListener.onKey(-5, codes);
                            this.mKeyboardActionListener.onRelease(-5);
                        } else if (index == 11) {
                            this.mKeyboardActionListener.onKey(-4, codes);
                            this.mKeyboardActionListener.onRelease(-4);
                        } else {
                            this.mKeyboardActionListener.onKey(code, codes);
                            this.mKeyboardActionListener.onRelease(code);
                        }
                    } else if (this.mNeedShowCompleteKey || !this.mNeedShowWellNumber) {
                        if (index != 9) {
                            this.mKeyboardActionListener.onKey(code, codes);
                            this.mKeyboardActionListener.onRelease(code);
                        }
                    } else if (index == 9) {
                        this.mKeyboardActionListener.onKey(18, codes);
                        this.mKeyboardActionListener.onRelease(18);
                    } else {
                        this.mKeyboardActionListener.onKey(code, codes);
                        this.mKeyboardActionListener.onRelease(code);
                    }
                } else if (isSecurityKeyboard()) {
                    sendCharToTarget(code, key);
                    this.mKeyboardActionListener.onKey(code, codes);
                    this.mKeyboardActionListener.onRelease(code);
                } else {
                    this.mKeyboardActionListener.onKey(code, codes);
                    this.mKeyboardActionListener.onRelease(code);
                }
            }
            this.mLastSentIndex = index;
            this.mLastTapTime = eventTime;
        }
    }

    private CharSequence getPreviewText(Key key) {
        int i = 0;
        if (!this.mInMultiTap) {
            return adjustCase(key.label);
        }
        this.mPreviewLabel.setLength(0);
        StringBuilder stringBuilder = this.mPreviewLabel;
        int[] iArr = key.codes;
        if (this.mTapCount >= 0) {
            i = this.mTapCount;
        }
        stringBuilder.append((char) iArr[i]);
        return adjustCase(this.mPreviewLabel);
    }

    private void showPreview(int keyIndex) {
        int oldKeyIndex = this.mCurrentKeyIndex;
        PopupWindow previewPopup = this.mPreviewPopup;
        this.mCurrentKeyIndex = keyIndex;
        Key[] keys = this.mKeys;
        if (oldKeyIndex != this.mCurrentKeyIndex) {
            int keyCode;
            if (oldKeyIndex != -1 && keys.length > oldKeyIndex) {
                boolean z;
                Key oldKey = keys[oldKeyIndex];
                if (this.mCurrentKeyIndex == -1) {
                    z = true;
                } else {
                    z = false;
                }
                oldKey.onReleased(z);
                invalidateKey(oldKeyIndex);
                keyCode = oldKey.codes[0];
                sendAccessibilityEventForUnicodeCharacter(256, keyCode);
                sendAccessibilityEventForUnicodeCharacter(65536, keyCode);
            }
            if (this.mCurrentKeyIndex != -1 && keys.length > this.mCurrentKeyIndex) {
                Key newKey = keys[this.mCurrentKeyIndex];
                newKey.onPressed();
                invalidateKey(this.mCurrentKeyIndex);
                keyCode = newKey.codes[0];
                if (isSecurityKeyboard()) {
                    sendAccessibilityEventForUnicodeCharacter(128, keyCode, newKey);
                    sendAccessibilityEventForUnicodeCharacter(32768, keyCode, newKey);
                } else {
                    sendAccessibilityEventForUnicodeCharacter(128, keyCode);
                    sendAccessibilityEventForUnicodeCharacter(32768, keyCode);
                }
            }
        }
        if (oldKeyIndex != this.mCurrentKeyIndex && this.mShowPreview) {
            this.mHandler.removeMessages(1);
            if (previewPopup.isShowing() && keyIndex == -1) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 70);
            }
            if (keyIndex == -1) {
                return;
            }
            if (previewPopup.isShowing() && this.mPreviewText.getVisibility() == 0) {
                showKey(keyIndex);
            } else {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, keyIndex, 0), 0);
            }
        }
    }

    private void showKey(int keyIndex) {
        PopupWindow previewPopup = this.mPreviewPopup;
        Key[] keys = this.mKeys;
        if (keyIndex >= 0 && keyIndex < this.mKeys.length) {
            Key key = keys[keyIndex];
            if (key.icon != null) {
                this.mPreviewText.setCompoundDrawables(null, null, null, key.iconPreview != null ? key.iconPreview : key.icon);
                this.mPreviewText.setText(null);
            } else {
                this.mPreviewText.setCompoundDrawables(null, null, null, null);
                this.mPreviewText.setText(getPreviewText(key));
                if (key.label.length() <= 1 || key.codes.length >= 2) {
                    this.mPreviewText.setTextSize(0, (float) this.mPreviewTextSizeLarge);
                    this.mPreviewText.setTypeface(Typeface.DEFAULT);
                } else {
                    this.mPreviewText.setTextSize(0, (float) this.mKeyTextSize);
                    this.mPreviewText.setTypeface(Typeface.DEFAULT_BOLD);
                }
            }
            this.mPreviewText.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            int popupWidth = Math.max(this.mPreviewText.getMeasuredWidth(), (key.width + this.mPreviewText.getPaddingLeft()) + this.mPreviewText.getPaddingRight());
            int popupHeight = this.mPreviewHeight;
            LayoutParams lp = this.mPreviewText.getLayoutParams();
            if (lp != null) {
                lp.width = popupWidth;
                lp.height = popupHeight;
            }
            if (this.mPreviewCentered) {
                this.mPopupPreviewX = 160 - (this.mPreviewText.getMeasuredWidth() / 2);
                this.mPopupPreviewY = -this.mPreviewText.getMeasuredHeight();
            } else {
                this.mPopupPreviewX = (key.x - this.mPreviewText.getPaddingLeft()) + this.mPaddingLeft;
                this.mPopupPreviewY = (key.y - popupHeight) + this.mPreviewOffset;
            }
            this.mHandler.removeMessages(2);
            getLocationInWindow(this.mCoordinates);
            int[] iArr = this.mCoordinates;
            iArr[0] = iArr[0] + this.mMiniKeyboardOffsetX;
            iArr = this.mCoordinates;
            iArr[1] = iArr[1] + this.mMiniKeyboardOffsetY;
            this.mPreviewText.getBackground().setState(key.popupResId != 0 ? LONG_PRESSABLE_STATE_SET : EMPTY_STATE_SET);
            this.mPopupPreviewX += this.mCoordinates[0];
            this.mPopupPreviewY += this.mCoordinates[1];
            getLocationOnScreen(this.mCoordinates);
            if (this.mPopupPreviewY + this.mCoordinates[1] < 0) {
                if (key.x + key.width <= getWidth() / 2) {
                    this.mPopupPreviewX += (int) (((double) key.width) * 2.5d);
                } else {
                    this.mPopupPreviewX -= (int) (((double) key.width) * 2.5d);
                }
                this.mPopupPreviewY += popupHeight;
            }
            if (previewPopup.isShowing()) {
                previewPopup.update(this.mPopupPreviewX, this.mPopupPreviewY, popupWidth, popupHeight);
            } else {
                previewPopup.setWidth(popupWidth);
                previewPopup.setHeight(popupHeight);
                previewPopup.showAtLocation(this.mPopupParent, 0, this.mPopupPreviewX, this.mPopupPreviewY);
            }
            this.mPreviewText.setVisibility(0);
        }
    }

    private void sendAccessibilityEventForUnicodeCharacter(int eventType, int code) {
        if (this.mAccessibilityManager.isEnabled()) {
            String text;
            AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            onInitializeAccessibilityEvent(event);
            switch (code) {
                case -6:
                    text = this.mContext.getString(17040065);
                    break;
                case -5:
                    text = this.mContext.getString(17040067);
                    break;
                case -4:
                    text = this.mContext.getString(17040068);
                    break;
                case -3:
                    text = this.mContext.getString(17040066);
                    break;
                case -2:
                    text = this.mContext.getString(17040070);
                    break;
                case -1:
                    text = this.mContext.getString(17040071);
                    break;
                case 10:
                    text = this.mContext.getString(17040069);
                    break;
                default:
                    text = String.valueOf((char) code);
                    break;
            }
            if (!isNumberUnLockKeyboard()) {
                event.getText().add(text);
                this.mAccessibilityManager.sendAccessibilityEvent(event);
            }
        }
    }

    private void sendAccessibilityEventForUnicodeCharacter(int eventType, int code, Key key) {
        if (this.mAccessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            onInitializeAccessibilityEvent(event);
            CharSequence text = null;
            String label = key.label == null ? null : adjustCase(key.label).toString();
            switch (code) {
                case -6:
                    if (label == null || !label.equals("ABC")) {
                        if (label != null && label.equals("123")) {
                            text = this.mContext.getString(201590201);
                            break;
                        }
                    }
                    text = this.mContext.getString(201590181);
                    break;
                    break;
                case -5:
                    text = this.mContext.getString(17039782);
                    break;
                case -2:
                    if (label == null || !label.equals("ABC")) {
                        if (label != null && label.equals("?#+=")) {
                            text = this.mContext.getString(201590182);
                            break;
                        }
                    }
                    text = this.mContext.getString(201590181);
                    break;
                    break;
                case -1:
                    if (!isShifted()) {
                        text = this.mContext.getString(201590179);
                        break;
                    } else {
                        text = this.mContext.getString(201590180);
                        break;
                    }
                case 10:
                    if (this.mKeyBoardType != 1) {
                        if (this.mAccessSureKey == null) {
                            text = this.mContext.getString(17040069);
                            break;
                        } else {
                            text = this.mAccessSureKey;
                            break;
                        }
                    } else if (this.mAccessSureKey != null) {
                        text = this.mAccessSureKey;
                        break;
                    } else {
                        text = this.mSureString;
                        break;
                    }
                default:
                    text = String.valueOf((char) code);
                    break;
            }
            if (isSecurityKeyboard() && (code == -5 || code == -2 || code == -1 || code == 10 || code == -6)) {
                announceForAccessibility(text);
                return;
            }
            event.getText().add(text);
            this.mAccessibilityManager.sendAccessibilityEvent(event);
        }
    }

    public void invalidateAllKeys() {
        this.mDirtyRect.union(0, 0, getWidth(), getHeight());
        this.mDrawPending = true;
        invalidate();
    }

    public void invalidateKey(int keyIndex) {
        if (this.mKeys != null && keyIndex >= 0 && keyIndex < this.mKeys.length) {
            Key key = this.mKeys[keyIndex];
            this.mInvalidatedKey = key;
            this.mDirtyRect.union(key.x + this.mPaddingLeft, key.y + this.mPaddingTop, (key.x + key.width) + this.mPaddingLeft, (key.y + key.height) + this.mPaddingTop);
            onBufferDraw();
            invalidate(key.x + this.mPaddingLeft, key.y + this.mPaddingTop, (key.x + key.width) + this.mPaddingLeft, (key.y + key.height) + this.mPaddingTop);
        }
    }

    private boolean openPopupIfRequired(MotionEvent me) {
        if (this.mPopupLayout == 0 || this.mCurrentKey < 0 || this.mCurrentKey >= this.mKeys.length) {
            return false;
        }
        boolean result = onLongPress(this.mKeys[this.mCurrentKey]);
        if (result) {
            this.mAbortKey = true;
            showPreview(-1);
        }
        return result;
    }

    protected boolean onLongPress(Key popupKey) {
        int popupKeyboardId = popupKey.popupResId;
        if (popupKeyboardId == 0) {
            return false;
        }
        int i;
        this.mMiniKeyboardContainer = (View) this.mMiniKeyboardCache.get(popupKey);
        if (this.mMiniKeyboardContainer == null) {
            Keyboard keyboard;
            this.mMiniKeyboardContainer = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(this.mPopupLayout, null);
            this.mMiniKeyboard = (KeyboardView) this.mMiniKeyboardContainer.findViewById(R.id.keyboardView);
            View closeButton = this.mMiniKeyboardContainer.findViewById(R.id.closeButton);
            if (closeButton != null) {
                closeButton.setOnClickListener(this);
            }
            this.mMiniKeyboard.setOnKeyboardActionListener(new OnKeyboardActionListener() {
                public void onKey(int primaryCode, int[] keyCodes) {
                    KeyboardView.this.mKeyboardActionListener.onKey(primaryCode, keyCodes);
                    KeyboardView.this.dismissPopupKeyboard();
                }

                public void onText(CharSequence text) {
                    KeyboardView.this.mKeyboardActionListener.onText(text);
                    KeyboardView.this.dismissPopupKeyboard();
                }

                public void swipeLeft() {
                }

                public void swipeRight() {
                }

                public void swipeUp() {
                }

                public void swipeDown() {
                }

                public void onPress(int primaryCode) {
                    KeyboardView.this.mKeyboardActionListener.onPress(primaryCode);
                }

                public void onRelease(int primaryCode) {
                    KeyboardView.this.mKeyboardActionListener.onRelease(primaryCode);
                }
            });
            if (popupKey.popupCharacters != null) {
                keyboard = new Keyboard(getContext(), popupKeyboardId, popupKey.popupCharacters, -1, getPaddingRight() + getPaddingLeft());
            } else {
                keyboard = new Keyboard(getContext(), popupKeyboardId);
            }
            this.mMiniKeyboard.setKeyboard(keyboard);
            this.mMiniKeyboard.setPopupParent(this);
            this.mMiniKeyboardContainer.measure(MeasureSpec.makeMeasureSpec(getWidth(), Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(getHeight(), Integer.MIN_VALUE));
            this.mMiniKeyboardCache.put(popupKey, this.mMiniKeyboardContainer);
        } else {
            this.mMiniKeyboard = (KeyboardView) this.mMiniKeyboardContainer.findViewById(R.id.keyboardView);
        }
        getLocationInWindow(this.mCoordinates);
        this.mPopupX = popupKey.x + this.mPaddingLeft;
        this.mPopupY = popupKey.y + this.mPaddingTop;
        this.mPopupX = (this.mPopupX + popupKey.width) - this.mMiniKeyboardContainer.getMeasuredWidth();
        this.mPopupY -= this.mMiniKeyboardContainer.getMeasuredHeight();
        int x = (this.mPopupX + this.mMiniKeyboardContainer.getPaddingRight()) + this.mCoordinates[0];
        int y = (this.mPopupY + this.mMiniKeyboardContainer.getPaddingBottom()) + this.mCoordinates[1];
        KeyboardView keyboardView = this.mMiniKeyboard;
        if (x < 0) {
            i = 0;
        } else {
            i = x;
        }
        keyboardView.setPopupOffset(i, y);
        this.mMiniKeyboard.setShifted(isShifted());
        this.mPopupKeyboard.setContentView(this.mMiniKeyboardContainer);
        this.mPopupKeyboard.setWidth(this.mMiniKeyboardContainer.getMeasuredWidth());
        this.mPopupKeyboard.setHeight(this.mMiniKeyboardContainer.getMeasuredHeight());
        this.mPopupKeyboard.showAtLocation(this, 0, x, y);
        this.mMiniKeyboardOnScreen = true;
        invalidateAllKeys();
        return true;
    }

    public boolean onHoverEvent(MotionEvent event) {
        if (this.mAccessibilityManager.isTouchExplorationEnabled() && event.getPointerCount() == 1) {
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

    public boolean onTouchEvent(MotionEvent me) {
        boolean result;
        int pointerCount = me.getPointerCount();
        int action = me.getAction();
        long now = me.getEventTime();
        if (pointerCount != this.mOldPointerCount) {
            if (pointerCount == 1) {
                MotionEvent down = MotionEvent.obtain(now, now, 0, me.getX(), me.getY(), me.getMetaState());
                result = onModifiedTouchEvent(down, false);
                down.recycle();
                if (action == 1) {
                    result = onModifiedTouchEvent(me, true);
                }
            } else {
                MotionEvent up = MotionEvent.obtain(now, now, 1, this.mOldPointerX, this.mOldPointerY, me.getMetaState());
                result = onModifiedTouchEvent(up, true);
                up.recycle();
            }
        } else if (pointerCount == 1) {
            result = onModifiedTouchEvent(me, false);
            this.mOldPointerX = me.getX();
            this.mOldPointerY = me.getY();
        } else {
            result = true;
        }
        this.mOldPointerCount = pointerCount;
        return result;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for delete the c code", property = OppoRomType.ROM)
    private boolean onModifiedTouchEvent(MotionEvent me, boolean possiblePoly) {
        int touchX = ((int) me.getX()) - this.mPaddingLeft;
        int touchY = ((int) me.getY()) - this.mPaddingTop;
        if (touchY >= (-this.mVerticalCorrection)) {
            touchY += this.mVerticalCorrection;
        }
        int action = me.getAction();
        long eventTime = me.getEventTime();
        int keyIndex = getKeyIndices(touchX, touchY, null);
        if (isNumberUnLockKeyboard() && (isKeyboardViewEnabled() ^ 1) != 0) {
            return false;
        }
        if (!isUnLockKeyboard() || (isKeyboardViewEnabled() ^ 1) == 0 || keyIndex == this.mKeys.length - 1) {
            this.mPossiblePoly = possiblePoly;
            if (action == 0) {
                this.mSwipeTracker.clear();
            }
            this.mSwipeTracker.addMovement(me);
            if (this.mAbortKey && action != 0 && action != 3) {
                return true;
            }
            if (this.mGestureDetector.onTouchEvent(me)) {
                showPreview(-1);
                if (isSecurityKeyboard()) {
                    dismissPopupWindow();
                }
                this.mHandler.removeMessages(3);
                this.mHandler.removeMessages(4);
                return true;
            } else if (this.mMiniKeyboardOnScreen && action != 3) {
                return true;
            } else {
                if (DEBUGTAG) {
                    Log.i(LOG_TAG, "onTouchEvent  action= " + action);
                }
                int x = (int) me.getX();
                int y = (int) me.getY();
                int touchIndex = getIndexIndices(x, y);
                Drawable icon;
                String text;
                switch (action) {
                    case 0:
                        this.mAbortKey = false;
                        this.mStartX = touchX;
                        this.mStartY = touchY;
                        this.mLastCodeX = touchX;
                        this.mLastCodeY = touchY;
                        this.mLastKeyTime = 0;
                        this.mCurrentKeyTime = 0;
                        this.mLastKey = -1;
                        this.mCurrentKey = keyIndex;
                        this.mDownKey = keyIndex;
                        this.mDownTime = me.getEventTime();
                        this.mLastMoveTime = this.mDownTime;
                        checkMultiTap(eventTime, keyIndex);
                        if (!isNumberUnLockKeyboard()) {
                            this.mKeyboardActionListener.onPress(keyIndex != -1 ? this.mKeys[keyIndex].codes[0] : 0);
                        } else if (this.mNeedShowCompleteKey || this.mNeedShowWellNumber) {
                            this.mKeyboardActionListener.onPress(keyIndex != -1 ? this.mKeys[keyIndex].codes[0] : 0);
                        } else if (keyIndex != 9) {
                            this.mKeyboardActionListener.onPress(keyIndex != -1 ? this.mKeys[keyIndex].codes[0] : 0);
                        }
                        this.mHandler.removeMessages(5);
                        if (isSecurityKeyboard() && this.mCurrentKey != -1) {
                            setPopWindowShow(0);
                            if (this.mPreviousKey == -1) {
                                showPopUpWindow(this.mCurrentKey);
                            }
                            this.mPreviousKey = this.mCurrentKey;
                        }
                        if (isUnLockKeyboard() && this.mCurrentKey == this.mKeys.length - 1) {
                            this.mIsDownFlag = true;
                        }
                        if (this.mCurrentKey >= 0 && this.mKeys[this.mCurrentKey].repeatable) {
                            this.mRepeatKeyIndex = this.mCurrentKey;
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 400);
                            repeatKey();
                            if (this.mAbortKey) {
                                this.mRepeatKeyIndex = -1;
                                break;
                            }
                        }
                        if (this.mCurrentKey != -1) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4, me), (long) LONGPRESS_TIMEOUT);
                        }
                        showPreview(keyIndex);
                        if (isSecurityNumericKeyboard() && -1 != touchIndex && touchIndex < this.mSpecialSymbols.length) {
                            this.mPreviousIndex = touchIndex;
                            setIconPressed(touchIndex, true);
                            icon = ((Item) this.mItem.get(touchIndex)).getItemBg();
                            text = ((Item) this.mItem.get(touchIndex)).getText();
                            refreshIconState(touchIndex, icon);
                            invalidate();
                            if (!(text == null || this.mItemTextColor == null)) {
                                ((Item) this.mItem.get(touchIndex)).mSpecialTextPaint.setColor(this.mItemTextColor.getColorForState(getIconState(touchIndex), this.mItemTextColor.getDefaultColor()));
                                invalidate();
                                break;
                            }
                        }
                        break;
                    case 1:
                        removeMessages();
                        if (keyIndex == this.mCurrentKey) {
                            this.mCurrentKeyTime += eventTime - this.mLastMoveTime;
                        } else {
                            resetMultiTap();
                            this.mLastKey = this.mCurrentKey;
                            this.mLastKeyTime = (this.mCurrentKeyTime + eventTime) - this.mLastMoveTime;
                            this.mCurrentKey = keyIndex;
                            this.mCurrentKeyTime = 0;
                        }
                        if (this.mCurrentKeyTime < this.mLastKeyTime && this.mCurrentKeyTime < 70 && this.mLastKey != -1) {
                            this.mCurrentKey = this.mLastKey;
                            touchX = this.mLastCodeX;
                            touchY = this.mLastCodeY;
                        }
                        showPreview(-1);
                        Arrays.fill(this.mKeyIndices, -1);
                        if (!(this.mRepeatKeyIndex != -1 || (this.mMiniKeyboardOnScreen ^ 1) == 0 || (this.mAbortKey ^ 1) == 0)) {
                            detectAndSendKey(this.mCurrentKey, touchX, touchY, eventTime);
                        }
                        invalidateKey(keyIndex);
                        this.mRepeatKeyIndex = -1;
                        if (isUnLockKeyboard() && this.mCurrentKey == this.mKeys.length - 1) {
                            this.mIsDownFlag = false;
                        }
                        if (isSecurityKeyboard()) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5), 120);
                        }
                        if (isSecurityNumericKeyboard()) {
                            touchIndex = getIndexIndices(x, y);
                            if (-1 != touchIndex && touchIndex < this.mSpecialSymbols.length) {
                                setItemRestore(touchIndex);
                                if (!isUnLockKeyboard()) {
                                    if (this.mKeyboardCharListener != null) {
                                        this.mKeyboardCharListener.onCharacter(this.mSpecialSymbols[touchIndex], 0);
                                        break;
                                    }
                                }
                                this.mKeyboardActionListener.onKey(this.mSpecialSymbols[touchIndex].charAt(0), null);
                                this.mKeyboardActionListener.onRelease(this.mSpecialSymbols[touchIndex].charAt(0));
                                break;
                            }
                        }
                        break;
                    case 2:
                        boolean continueLongPress = false;
                        if (keyIndex != -1) {
                            if (this.mCurrentKey == -1) {
                                this.mCurrentKey = keyIndex;
                                this.mCurrentKeyTime = eventTime - this.mDownTime;
                            } else if (keyIndex == this.mCurrentKey) {
                                this.mCurrentKeyTime += eventTime - this.mLastMoveTime;
                                continueLongPress = true;
                            } else if (this.mRepeatKeyIndex == -1) {
                                resetMultiTap();
                                this.mLastKey = this.mCurrentKey;
                                this.mLastCodeX = this.mLastX;
                                this.mLastCodeY = this.mLastY;
                                this.mLastKeyTime = (this.mCurrentKeyTime + eventTime) - this.mLastMoveTime;
                                this.mCurrentKey = keyIndex;
                                this.mCurrentKeyTime = 0;
                            }
                        }
                        if (!continueLongPress) {
                            this.mHandler.removeMessages(4);
                            if (keyIndex != -1) {
                                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4, me), (long) LONGPRESS_TIMEOUT);
                            }
                        }
                        this.mHandler.removeMessages(5);
                        if (isSecurityKeyboard() && this.mCurrentKey != -1) {
                            setPopWindowShow(1);
                            this.mPreviousKey = this.mCurrentKey;
                        }
                        showPreview(this.mCurrentKey);
                        this.mLastMoveTime = eventTime;
                        if (isSecurityNumericKeyboard()) {
                            if (!(touchIndex == this.mPreviousIndex || -1 == touchIndex || touchIndex >= this.mSpecialSymbols.length)) {
                                setIconPressed(touchIndex, true);
                                icon = ((Item) this.mItem.get(touchIndex)).getItemBg();
                                text = ((Item) this.mItem.get(touchIndex)).getText();
                                refreshIconState(touchIndex, icon);
                                invalidate();
                                if (!(text == null || this.mItemTextColor == null)) {
                                    ((Item) this.mItem.get(touchIndex)).mSpecialTextPaint.setColor(this.mItemTextColor.getColorForState(getIconState(touchIndex), this.mItemTextColor.getDefaultColor()));
                                    invalidate();
                                }
                            }
                            if (!(-1 == this.mPreviousIndex || touchIndex == this.mPreviousIndex || this.mPreviousIndex >= this.mSpecialSymbols.length)) {
                                setItemRestore(this.mPreviousIndex);
                            }
                            this.mPreviousIndex = touchIndex;
                            break;
                        }
                        break;
                    case 3:
                        removeMessages();
                        dismissPopupKeyboard();
                        this.mAbortKey = true;
                        showPreview(-1);
                        invalidateKey(this.mCurrentKey);
                        if (isSecurityKeyboard()) {
                            dismissPopupWindow();
                        }
                        if (isSecurityNumericKeyboard()) {
                            touchIndex = getIndexIndices(x, y);
                            if (-1 != touchIndex && touchIndex < this.mSpecialSymbols.length) {
                                setItemRestore(touchIndex);
                                break;
                            }
                        }
                        break;
                    default:
                        if (isSecurityKeyboard()) {
                            dismissPopupWindow();
                            break;
                        }
                        break;
                }
                this.mLastX = touchX;
                this.mLastY = touchY;
                return true;
            }
        }
        if (this.mIsDownFlag && this.mPreviousKey != -1 && this.mPreviousKey == this.mKeys.length - 1) {
            if (this.mKeys[this.mPreviousKey].pressed) {
                this.mKeys[this.mPreviousKey].onReleased(this.mCurrentKeyIndex == -1);
                this.mCurrentKeyIndex = -1;
                this.mIsDownFlag = false;
            }
            invalidateKey(this.mPreviousKey);
        }
        return false;
    }

    private boolean repeatKey() {
        Key key = this.mKeys[this.mRepeatKeyIndex];
        detectAndSendKey(this.mCurrentKey, key.x, key.y, this.mLastTapTime);
        return true;
    }

    protected void swipeRight() {
        this.mKeyboardActionListener.swipeRight();
    }

    protected void swipeLeft() {
        this.mKeyboardActionListener.swipeLeft();
    }

    protected void swipeUp() {
        this.mKeyboardActionListener.swipeUp();
    }

    protected void swipeDown() {
        this.mKeyboardActionListener.swipeDown();
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Suying.You@Plf.SDK : Modify for the security popupwindow", property = OppoRomType.ROM)
    public void closing() {
        if (this.mPreviewPopup.isShowing()) {
            this.mPreviewPopup.dismiss();
        }
        if (isSecurityKeyboard()) {
            dismissPopupWindow();
        }
        this.mPreviousKey = -1;
        removeMessages();
        dismissPopupKeyboard();
        this.mBuffer = null;
        this.mCanvas = null;
        this.mMiniKeyboardCache.clear();
    }

    private void removeMessages() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(3);
            this.mHandler.removeMessages(4);
            this.mHandler.removeMessages(1);
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        closing();
    }

    private void dismissPopupKeyboard() {
        if (this.mPopupKeyboard.isShowing()) {
            this.mPopupKeyboard.dismiss();
            this.mMiniKeyboardOnScreen = false;
            invalidateAllKeys();
        }
    }

    public boolean handleBack() {
        if (!this.mPopupKeyboard.isShowing()) {
            return false;
        }
        dismissPopupKeyboard();
        return true;
    }

    private void resetMultiTap() {
        this.mLastSentIndex = -1;
        this.mTapCount = 0;
        this.mLastTapTime = -1;
        this.mInMultiTap = false;
    }

    private void checkMultiTap(long eventTime, int keyIndex) {
        if (keyIndex != -1) {
            Key key = this.mKeys[keyIndex];
            if (key.codes.length > 1) {
                this.mInMultiTap = true;
                if (eventTime >= this.mLastTapTime + 800 || keyIndex != this.mLastSentIndex) {
                    this.mTapCount = -1;
                    return;
                } else {
                    this.mTapCount = (this.mTapCount + 1) % key.codes.length;
                    return;
                }
            }
            if (eventTime > this.mLastTapTime + 800 || keyIndex != this.mLastSentIndex) {
                resetMultiTap();
            }
        }
    }

    private void drawSpecialSymbol(Canvas canvas, int translation) {
        int i;
        if (this.mSpecialNormalBg != null) {
            int bgLeft = this.mHorizontalGap + translation;
            int bgTop = this.mVerticalGap;
            this.mSpecialNormalBg.setBounds(bgLeft, bgTop, bgLeft + this.mSpecialKeywidth, bgTop + this.mSpecialKeyHeight);
            this.mSpecialNormalBg.draw(canvas);
        }
        for (i = 0; i < this.mSpecialSymbols.length; i++) {
            Drawable itemBg = ((Item) this.mItem.get(i)).getItemBg();
            if (itemBg != null) {
                int itemBgLeft = this.mHorizontalGap + translation;
                int itemBgTop = ((int) ((((float) this.mVerticalGap) + (this.mAverageWidth * ((float) i))) + (((float) i) * this.mLineWidth))) + this.mBgBottomOffset;
                float touchTop = ((float) (this.mVerticalGap + this.mBgTopOffset)) + (((float) i) * this.mAverageWidth);
                float touchBottom = touchTop + this.mAverageWidth;
                itemBg.setBounds(itemBgLeft, itemBgTop, itemBgLeft + this.mSpecialKeywidth, ((int) ((((float) ((this.mBgTopOffset + itemBgTop) + this.mBgBottomOffset)) + this.mAverageWidth) + (((float) i) * this.mLineWidth))) - (this.mBgBottomOffset * 2));
                itemBg.draw(canvas);
                ((Item) this.mItem.get(i)).setBottom(touchBottom);
                ((Item) this.mItem.get(i)).setTop(touchTop);
            }
        }
        int lineLeft = (int) ((((float) this.mHorizontalGap) + this.mLineOffset) + ((float) translation));
        int lineRight = (int) (((float) (this.mSpecialKeywidth + lineLeft)) - (this.mLineOffset * 2.0f));
        for (i = 0; i < this.mSpecialSymbols.length - 1; i++) {
            float lineTop = (((float) (this.mVerticalGap + this.mBgTopOffset)) + (((float) i) * this.mLineWidth)) + (((float) (i + 1)) * this.mAverageWidth);
            float lintBottom = lineTop;
            canvas.drawLine((float) lineLeft, lineTop, (float) lineRight, lineTop, this.mLinePaint);
        }
        for (i = 0; i < this.mSpecialSymbols.length; i++) {
            Paint paint = ((Item) this.mItem.get(i)).mSpecialTextPaint;
            FontMetricsInt wordTextFontMetrics = paint.getFontMetricsInt();
            String character = this.mSpecialSymbols[i];
            if (character != null) {
                Canvas canvas2 = canvas;
                canvas2.drawText(this.mSpecialSymbols[i], (float) ((this.mHorizontalGap + ((this.mSpecialKeywidth - ((int) paint.measureText(character))) / 2)) + translation), (float) ((int) ((((((float) (this.mVerticalGap + this.mBgTopOffset)) + (((float) i) * (this.mAverageWidth + this.mLineWidth))) + (this.mAverageWidth / 2.0f)) - ((float) ((wordTextFontMetrics.descent - wordTextFontMetrics.ascent) / 2))) - ((float) wordTextFontMetrics.ascent))), paint);
            }
        }
    }

    private void setPopWindowShow(int flag) {
        if (this.mCurrentKey == this.mPreviousKey && flag == 0 && (this.mColorPopup == null || !this.mColorPopup.isShowing())) {
            showPopUpWindow(this.mCurrentKey);
        }
        if (this.mCurrentKey != this.mPreviousKey) {
            showPopUpWindow(this.mCurrentKey);
        }
    }

    private void showPopUpWindow(int keyIndex) {
        if (this.mAccessibilityManager == null || !this.mAccessibilityManager.isEnabled() || !this.mAccessibilityManager.isOppoTouchExplorationEnabled() || !ColorAccessibilityUtil.isTalkbackEnabled(getContext())) {
            int codes = this.mKeys[keyIndex].codes[0];
            CharSequence label = this.mKeys[keyIndex].label;
            int[] coordinates = new int[2];
            getLocationInWindow(coordinates);
            if (label != null && codes != -1 && codes != -5 && codes != -2 && codes != 10 && codes != 32 && codes != -6 && (isSecurityNumericKeyboard() ^ 1) != 0) {
                if (this.mKeyPreview != null) {
                    this.mKeyPreview.setVisibility(0);
                    this.mKeyPreview.setText(getPreviewText(this.mKeys[keyIndex]));
                }
                int popupWindowLocalx = (this.mKeys[keyIndex].x + (this.mKeys[keyIndex].width / 2)) - (this.mColorPopupWidth / 2);
                int popupWindowLocaly = (this.mKeys[keyIndex].y - this.mColorPopupHeight) + coordinates[1];
                if (this.mKeyBoardType == 1) {
                    popupWindowLocaly -= this.mPopVerticalOffset;
                }
                if (this.mColorPopup != null && this.mColorPopup.isShowing()) {
                    this.mColorPopup.update(popupWindowLocalx, popupWindowLocaly, this.mColorPopupWidth, this.mColorPopupHeight);
                } else if (this.mColorPopup != null) {
                    this.mColorPopup.showAtLocation(this, 0, popupWindowLocalx, popupWindowLocaly);
                }
            } else if (codes == -1 || codes == -5 || codes == -2 || codes == 10 || codes == 32 || (isSecurityNumericKeyboard() ^ 1) != 0 || codes == -6) {
                dismissPopupWindow();
            }
        }
    }

    public void dismissPopupWindow() {
        if (this.mAccessibilityManager == null || !this.mAccessibilityManager.isEnabled() || !this.mAccessibilityManager.isOppoTouchExplorationEnabled() || !ColorAccessibilityUtil.isTalkbackEnabled(getContext())) {
            if (this.mColorPopup != null && DEBUGTAG) {
                Log.i(LOG_TAG, "dismissPopupWindow mColorPopup.isShowing()= " + this.mColorPopup.isShowing());
            }
            if (this.mColorPopup != null && this.mColorPopup.isShowing()) {
                this.mColorPopup.dismiss();
            }
        }
    }

    private void sendCharToTarget(int code, Key key) {
        if (this.mKeyboardCharListener != null && code != -1 && code != -2 && code != -6) {
            if (code == 10) {
                this.mKeyboardCharListener.onCharacter(null, 2);
            } else if (this.mKeyBoardType == 1 && code == 32) {
                this.mKeyboardCharListener.onCharacter(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER, 0);
            } else if (this.mKeyBoardType == 1 && code != 10 && code != -5) {
                String label = key.label == null ? null : adjustCase(key.label).toString();
                if (label != null) {
                    this.mKeyboardCharListener.onCharacter(label, 0);
                }
            } else if (this.mKeyBoardType == 1 && code == -5) {
                this.mKeyboardCharListener.onCharacter(null, 1);
            }
        }
    }

    private boolean isNumberUnLockKeyboard() {
        if (!ColorContextUtil.isOppoStyle(getContext()) || this.mKeyBoardType == 1) {
            return false;
        }
        return this.mKeyBoardType != 2;
    }

    private boolean isSecurityKeyboard() {
        if (this.mKeyBoardType == 1) {
            return true;
        }
        if (this.mKeyBoardType == 2) {
            return ColorContextUtil.isOppoStyle(getContext());
        }
        return false;
    }

    private boolean isUnLockKeyboard() {
        return this.mKeyBoardType == 2 ? ColorContextUtil.isOppoStyle(getContext()) : false;
    }

    private void initState() {
        int length = this.mSpecialSymbols.length;
        if (length >= 0) {
            int i;
            for (i = 0; i < length; i++) {
                this.mItemBg.add(this.mSpecialItemBg.getConstantState().newDrawable());
                this.mItem.add(new Item((Drawable) this.mItemBg.get(i), this.mSpecialSymbols[i]));
            }
            for (i = 0; i < length; i++) {
                VIEW_STATE_SETS[i] = new int[VIEW_SETS.length][];
                System.arraycopy(VIEW_SETS, 0, VIEW_STATE_SETS[i], 0, VIEW_SETS.length);
            }
            this.mIconState.clear();
            this.mPrivateFlags.clear();
            for (i = 0; i < length; i++) {
                this.mIconState.add(new int[STYLEABLE_LENGTH]);
                this.mPrivateFlags.add(new Integer(0));
                refreshIconState(i, ((Item) this.mItem.get(i)).getItemBg());
                if (this.mItemTextColor != null) {
                    ((Item) this.mItem.get(i)).mSpecialTextPaint.setColor(this.mItemTextColor.getColorForState(getIconState(i), this.mItemTextColor.getDefaultColor()));
                }
            }
        }
    }

    protected void refreshIconState(int index, Drawable icon) {
        this.mPrivateFlags.set(index, Integer.valueOf(((Integer) this.mPrivateFlags.get(index)).intValue() | 1024));
        iconStateChanged(index, icon);
    }

    protected void iconStateChanged(int index, Drawable icon) {
        int[] state = getIconState(index);
        if (icon != null && icon.isStateful()) {
            icon.setState(state);
        }
    }

    protected int[] getIconState(int index) {
        int privateFlags = ((Integer) this.mPrivateFlags.get(index)).intValue();
        if ((privateFlags & 1024) != 0) {
            this.mIconState.set(index, onCreateIconState(index, 0));
            this.mPrivateFlags.set(index, Integer.valueOf(privateFlags & -1025));
        }
        return (int[]) this.mIconState.get(index);
    }

    protected int[] onCreateIconState(int index, int extraSpace) {
        int mViewFlags = ((Integer) this.mPrivateFlags.get(index)).intValue();
        int viewStateIndex = 0;
        if ((((Integer) this.mPrivateFlags.get(index)).intValue() & 16384) != 0) {
            viewStateIndex = 16;
        }
        if ((mViewFlags & 32) == 0) {
            viewStateIndex |= 8;
        }
        if (hasWindowFocus()) {
            viewStateIndex |= 1;
        }
        int[] IconState = VIEW_STATE_SETS[index][viewStateIndex];
        if (extraSpace == 0) {
            return IconState;
        }
        int[] fullState;
        if (IconState != null) {
            fullState = new int[(IconState.length + extraSpace)];
            System.arraycopy(IconState, 0, fullState, 0, IconState.length);
        } else {
            fullState = new int[extraSpace];
        }
        return fullState;
    }

    private void setIconPressed(int index, boolean pressed) {
        int privateFlags = ((Integer) this.mPrivateFlags.get(index)).intValue();
        if (pressed) {
            privateFlags |= 16384;
        } else {
            privateFlags &= -16385;
        }
        this.mPrivateFlags.set(index, Integer.valueOf(privateFlags));
    }

    private int getIndexIndices(int x, int y) {
        if (!isSecurityNumericKeyboard() || this.mSpecialSymbols == null) {
            return -1;
        }
        int count = this.mSpecialSymbols.length;
        if (count <= 0) {
            return -1;
        }
        int i = 0;
        while (i < count) {
            if (x >= this.mHorizontalGap && x <= this.mHorizontalGap + this.mSpecialKeywidth && ((float) y) >= ((Item) this.mItem.get(i)).getTop() && ((float) y) <= ((Item) this.mItem.get(i)).getBottom()) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private void setItemRestore(int index) {
        setIconPressed(index, false);
        Drawable icon = ((Item) this.mItem.get(index)).getItemBg();
        String text = ((Item) this.mItem.get(index)).getText();
        refreshIconState(index, icon);
        if (text != null && this.mItemTextColor != null) {
            ((Item) this.mItem.get(index)).mSpecialTextPaint.setColor(this.mItemTextColor.getColorForState(getIconState(index), this.mItemTextColor.getDefaultColor()));
            invalidate();
        }
    }

    public void setKeyboardViewEnabled(boolean enabled) {
        this.mIsEnable = enabled;
    }

    public boolean isKeyboardViewEnabled() {
        return this.mIsEnable;
    }

    public void setOnKeyboardCharListener(OnKeyboardCharListener listener) {
        this.mKeyboardCharListener = listener;
    }

    public void updateOkKey(CharSequence text) {
        CharSequence temp = getResources().getText(17040038);
        if (text == null || this.mKeyBoardType != 1) {
            if (text != null && isUnLockKeyboard()) {
                this.mUpdateText = text;
            }
        } else if (temp.toString().equals(text.toString())) {
            this.mUpdateText = this.mSureString;
        } else {
            this.mUpdateText = text;
        }
        if (this.mKeys != null && this.mKeys.length > 0) {
            invalidateKey(this.mKeys.length - 1);
        }
        this.mAccessSureKey = (String) this.mUpdateText;
    }

    public void setKeyboardType(int keyboardType) {
        int tempWidth;
        this.mKeyBoardType = keyboardType;
        if (isSecurityKeyboard()) {
            LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mPaddingTop = 0;
            this.mLowerLetterSize = getResources().getDimensionPixelOffset(201655544);
            this.mSpaceLabelSize = getResources().getDimensionPixelOffset(201655545);
            this.mEndLabelSize = getResources().getDimensionPixelOffset(201655546);
            this.mSureString = getResources().getString(201590183);
            this.mSpecialItemSize = getResources().getDimensionPixelOffset(201655590);
            try {
                this.mTypeface = Typeface.createFromFile("/system/fonts/Roboto-Regular.ttf");
            } catch (RuntimeException e) {
                this.mTypeface = Typeface.createFromFile(Typeface.LIGHT_PATH);
            } catch (Exception e2) {
                this.mTypeface = Typeface.DEFAULT;
            }
            this.mKeyPreview = (TextView) inflate.inflate(201917584, null);
            this.mKeyPreview.setTypeface(this.mTypeface);
            this.mColorPopup = new ColorPopupWindow(getContext());
            this.mColorPopup.setOnPreInvokePopupListener(new OnPreInvokePopupListener() {
                public void onPreInvokePopup(WindowManager.LayoutParams params) {
                    params.flags |= 8192;
                    params.setTitle("ColorSecurityPopupWindow");
                }
            });
            this.mLineWidth = getResources().getDimension(201655570);
            this.mLineOffset = getResources().getDimension(201655589);
            this.mSpecialSymbols = getResources().getStringArray(201786396);
        }
        if (this.mKeyBoardType == 1) {
            this.mColorSecurityBgWidth = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
            tempWidth = this.mColorSecurityBgWidth;
            if (getContext().getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism") && getContext().getResources().getConfiguration().orientation == 2) {
                tempWidth = this.mColorSecurityBgWidth - 160;
            }
            this.mColorSecurityBgHeight = getResources().getDimensionPixelSize(201655539);
            this.mColorSecurityOffset1 = getResources().getDimensionPixelSize(201655547);
            this.mColorSecurityOffset2 = getResources().getDimensionPixelSize(201655548);
            this.mPopVerticalOffset = getResources().getDimensionPixelSize(201655578);
            this.mColorTextColor = getResources().getColorStateList(201720897);
            this.mColorGoTextColor = getResources().getColorStateList(201720901);
            this.mColorSecurityBg = getResources().getColor(201720898);
            this.mColorSecurityKeyBg = getResources().getDrawable(201852180);
            this.mColorSpecialKeyBg = getResources().getDrawable(201852181);
            this.mColorEndKeyBg = getResources().getDrawable(201852188);
            Drawable popupBg = getResources().getDrawable(201852211);
            this.mVerticalGap = getResources().getDimensionPixelSize(201655581);
            this.mHorizontalGap = (int) (((float) tempWidth) * getResources().getFraction(201655582, 1, 1));
            this.mSpecialKeywidth = (int) (((float) tempWidth) * getResources().getFraction(201655583, 1, 1));
            this.mSpecialKeyHeight = getResources().getDimensionPixelSize(201655584);
            this.mLineColor = getResources().getColor(201720912);
            this.mSpecialNormalBg = getResources().getDrawable(201852215);
            this.mItemTextColor = getResources().getColorStateList(201720914);
            this.mSpecialItemBg = getResources().getDrawable(201852217);
            this.mColorPopupWidth = popupBg.getIntrinsicWidth();
            this.mColorPopupHeight = popupBg.getIntrinsicHeight();
            this.mColorPopup.setWidth(this.mColorPopupWidth);
            this.mColorPopup.setHeight(this.mColorPopupHeight);
            this.mColorPopup.setContentView(this.mKeyPreview);
            this.mColorPopup.setBackgroundDrawable(null);
            this.mColorPopup.setTouchable(false);
        }
        if (isUnLockKeyboard()) {
            this.mColorSecurityBgWidth = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
            tempWidth = this.mColorSecurityBgWidth;
            if (getContext().getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism") && getContext().getResources().getConfiguration().orientation == 2) {
                tempWidth = this.mColorSecurityBgWidth - 160;
            }
            this.mColorSecurityBgHeight = getResources().getDimensionPixelSize(201655549);
            this.mColorSecurityOffset1 = getResources().getDimensionPixelSize(201655550);
            this.mColorSecurityOffset2 = getResources().getDimensionPixelSize(201655551);
            this.mColorPopupBg = getResources().getDrawable(201852182);
            this.mColorTextColor = getResources().getColorStateList(201720903);
            this.mColorGoTextColor = getResources().getColorStateList(201720903);
            this.mColorSecurityBg = 0;
            this.mColorSecurityKeyBg = getResources().getDrawable(201852192);
            this.mColorSpecialKeyBg = getResources().getDrawable(201852192);
            this.mColorEndKeyBg = getResources().getDrawable(201852196);
            this.mColorLockDeleteKey = getResources().getDrawable(201852195);
            this.mColorLockSpaceKey = getResources().getDrawable(201852199);
            this.mColorLockShiftKey = getResources().getDrawable(201852193);
            this.mVerticalGap = getResources().getDimensionPixelSize(201655585);
            this.mHorizontalGap = (int) (((float) tempWidth) * getResources().getFraction(201655586, 1, 1));
            this.mSpecialKeywidth = (int) (((float) tempWidth) * getResources().getFraction(201655587, 1, 1));
            this.mSpecialKeyHeight = getResources().getDimensionPixelSize(201655588);
            this.mLineColor = getResources().getColor(201720913);
            this.mSpecialNormalBg = getResources().getDrawable(201852216);
            this.mItemTextColor = getResources().getColorStateList(201720915);
            this.mSpecialItemBg = getResources().getDrawable(201852218);
            if (this.mKeyPreview != null) {
                this.mKeyPreview.setBackgroundDrawable(this.mColorPopupBg);
                this.mKeyPreview.setTextColor(this.mColorTextColor);
            }
            this.mColorPopupWidth = getResources().getDimensionPixelOffset(201655540);
            this.mColorPopupHeight = getResources().getDimensionPixelOffset(201655541);
            this.mColorPopup.setWidth(this.mColorPopupWidth);
            this.mColorPopup.setHeight(this.mColorPopupHeight);
            this.mColorPopup.setContentView(this.mKeyPreview);
            this.mColorPopup.setBackgroundDrawable(null);
            this.mColorPopup.setTouchable(false);
        }
        this.mLinePaint.setStrokeWidth(this.mLineWidth);
        this.mLinePaint.setColor(this.mLineColor);
        this.mAverageWidth = (((float) ((this.mSpecialKeyHeight - this.mBgTopOffset) - this.mBgBottomOffset)) - (((float) (this.mSpecialSymbols.length - 1)) * this.mLineWidth)) / ((float) this.mSpecialSymbols.length);
        initState();
    }

    public void setSecurityNumericKeyboard(boolean isSecurityNumeric) {
        this.mIsSecurityNumeric = isSecurityNumeric;
    }

    public boolean isSecurityNumericKeyboard() {
        return this.mIsSecurityNumeric;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Suying.You@Plf.SDK, 2018-02-06 : Modify for BUG:1270117", property = OppoRomType.ROM)
    private static boolean isNavigationBarHide(Context context) {
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            if (WifiEnterpriseConfig.ENGINE_ENABLE.equals((String) systemPropertiesClass.getMethod("get", new Class[]{String.class}).invoke(systemPropertiesClass, new Object[]{"oppo.hide.navigationbar"}))) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        boolean handled = super.dispatchHoverEvent(event);
        if (!isNumberUnLockKeyboard() || this.mTouchHelper == null) {
            return handled;
        }
        return handled | this.mTouchHelper.dispatchHoverEvent(event);
    }

    private int getHoverColumn(float x) {
        float rectWidth = this.mCellWidth;
        int i = 0;
        while (i < 3) {
            if (x >= ((float) i) * rectWidth && x <= ((float) (i + 1)) * rectWidth) {
                return i;
            }
            i++;
        }
        return 4;
    }

    private int getHoverRow(float y) {
        float rectHeight = this.mCellHeight;
        int i = 0;
        while (i < 4) {
            if (y >= ((float) i) * rectHeight && y <= ((float) (i + 1)) * rectHeight) {
                return i;
            }
            i++;
        }
        return 3;
    }
}
