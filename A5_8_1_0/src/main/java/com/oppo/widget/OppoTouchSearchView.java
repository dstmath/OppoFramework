package com.oppo.widget;

import android.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import com.color.util.ColorChangeTextUtil;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class OppoTouchSearchView extends View implements OnClickListener {
    private static final int BG_ALIGN_MIDDLE = 0;
    private static final int BG_ALIGN_RIGHT = 2;
    public static final Comparator<CharSequence> CHAR_COMPARATOR = new Comparator<CharSequence>() {
        public final int compare(CharSequence a, CharSequence b) {
            return OppoTouchSearchView.sCollator.compare(a, b);
        }
    };
    private static final boolean DEBUG = false;
    private static final int ENABLED = 0;
    private static final int ENABLED_MASK = 32;
    private static final int INVALID_POINTER = -1;
    public static final int KEY_PADDING_X = 27;
    private static final int MAX_NAME_NUM = 7;
    public static final int MAX_SECTIONS_NUM = 27;
    public static final int MAX_SECTIONS_NUM_WITH_DOT = 23;
    public static final int MIN_SECTIONS_NUM = 5;
    private static final int PFLAG_DRAWABLE_STATE_DIRTY = 1024;
    private static final int PFLAG_PRESSED = 16384;
    private static final int SEARCH_OFFSET = 1;
    private static int STYLEABLE_LENGTH = R.styleable.ViewDrawableStates.length;
    private static final String TAG = "OppoTouchSearchView";
    private static int[][] VIEW_SETS = null;
    private static final int VIEW_STATE_ACCELERATED = 64;
    private static final int VIEW_STATE_ACTIVATED = 32;
    private static final int VIEW_STATE_DRAG_CAN_ACCEPT = 256;
    private static final int VIEW_STATE_DRAG_HOVERED = 512;
    private static final int VIEW_STATE_ENABLED = 8;
    private static final int VIEW_STATE_FOCUSED = 4;
    private static final int VIEW_STATE_HOVERED = 128;
    private static final int[] VIEW_STATE_IDS = new int[]{16842909, 1, 16842913, 2, 16842908, 4, 16842910, 8, 16842919, 16, 16843518, 32, 16843547, VIEW_STATE_ACCELERATED, 16843623, VIEW_STATE_HOVERED, 16843624, VIEW_STATE_DRAG_CAN_ACCEPT, 16843625, VIEW_STATE_DRAG_HOVERED};
    private static final int VIEW_STATE_PRESSED = 16;
    private static final int VIEW_STATE_SELECTED = 2;
    private static int[][][] VIEW_STATE_SETS = null;
    private static final int VIEW_STATE_WINDOW_FOCUSED = 1;
    private static final int WELL_DRAWABLE_POSITION = 0;
    private static final Collator sCollator = Collator.getInstance();
    private String[] KEYS;
    private String[] UNIONKEYS;
    private int keyIndices;
    private int lastKeyIndices;
    private LinearLayout layout;
    private int mActivePointerId;
    private int mBackgroundAlignMode;
    private int mBackgroundLeftMargin;
    private int mBackgroundRightMargin;
    private int mBackgroundWidth;
    private int mCellHeight;
    private int mCellWidth;
    private boolean mCollectHighLight;
    private Context mContext;
    private ColorStateList mDefaultTextColor;
    private int mDefaultTextSize;
    private CharSequence mDisplayKey;
    private CharSequence mDot;
    private Drawable mDotDrawable;
    private int mDotDrawableHeight;
    private int mDotDrawableWidth;
    private Drawable[] mDotDrawables;
    private boolean mFirstIsCharacter;
    private boolean mFirstLayout;
    private Typeface mFontFace;
    private boolean mFrameChanged;
    private List<int[]> mIconState;
    private boolean mInnerClosing;
    private ArrayList<Key> mKey;
    private Drawable mKeyCollectDrawable;
    private int mKeyDrawableHeight;
    private String[] mKeyDrawableNames;
    private int mKeyDrawableOffset;
    private int mKeyDrawableWidth;
    private Drawable[] mKeyDrawables;
    private int mKeyPaddingX;
    private int mKeyPaddingY;
    private Drawable[] mKeyPressedDrawables;
    private ArrayList<Key> mKeyText;
    private Key[] mKeys;
    private int mPopupDefaultHeight;
    private TextView mPopupTextView;
    private int mPopupWinSubHeight;
    private PopupWindow mPopupWindow;
    private int mPopupWindowHeight;
    private int mPopupWindowLocalx;
    private int mPopupWindowLocaly;
    private int mPopupWindowMinTop;
    private int mPopupWindowRightMargin;
    private int mPopupWindowTextColor;
    private int mPopupWindowTextSize;
    private int mPopupWindowTopMargin;
    private int mPopupWindowWidth;
    private Rect mPositionRect;
    private int mPreviousIndex;
    protected List<Integer> mPrivateFlags;
    private ScrollView mScrollView;
    private String[] mSections;
    private ColorStateList mTextColor;
    private boolean mTouchFlag;
    private TouchSearchActionListener mTouchSearchActionListener;
    private Drawable mTouchWellDrawable;
    private boolean mUnionEnable;
    private ColorStateList mUserTextColor;
    private int mUserTextSize;
    private boolean mWhetherDrawDot;
    private boolean mWhetherUnion;
    private CharSequence surname;

    private class Key {
        public Drawable icon = null;
        public CharSequence keyOne = null;
        public CharSequence keyTwo = null;
        public int left;
        public CharSequence mKeyLabel;
        public String text = null;
        private TextPaint textPaint = null;
        public int top;

        public Key(Drawable keydrawable, String text) {
            this.icon = keydrawable;
            this.text = text;
            this.textPaint = new TextPaint(33);
            int textSize = OppoTouchSearchView.this.mUserTextSize;
            if (textSize == 0) {
                textSize = OppoTouchSearchView.this.mDefaultTextSize;
            }
            this.textPaint.setTextSize((float) textSize);
            OppoTouchSearchView.this.mTextColor = OppoTouchSearchView.this.mUserTextColor;
            if (OppoTouchSearchView.this.mTextColor == null) {
                OppoTouchSearchView.this.mTextColor = OppoTouchSearchView.this.mDefaultTextColor;
            }
            if (OppoTouchSearchView.this.mFontFace != null) {
                this.textPaint.setTypeface(OppoTouchSearchView.this.mFontFace);
            }
        }

        public Drawable getIcon() {
            if (this.icon != null) {
                return this.icon;
            }
            return null;
        }

        public String getText() {
            if (this.text != null) {
                return this.text;
            }
            return null;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getLeft() {
            return this.left;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public int getTop() {
            return this.top;
        }

        public CharSequence getTextToDisplay(int x, int y, int height, CharSequence compare) {
            if (!this.mKeyLabel.equals(compare)) {
                return this.mKeyLabel;
            }
            if (this.keyTwo == null) {
                return this.keyOne;
            }
            if (y >= this.top && y <= this.top + (height >> 1)) {
                return this.keyOne;
            }
            if (y > this.top + (height >> 1)) {
                return this.keyTwo;
            }
            return this.keyOne;
        }
    }

    public interface TouchSearchActionListener {
        void onKey(CharSequence charSequence);

        void onLongKey(CharSequence charSequence);

        void onNameKey(CharSequence charSequence);
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

    public OppoTouchSearchView(Context context) {
        this(context, null);
    }

    public OppoTouchSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393262);
    }

    public OppoTouchSearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mKeyDrawableOffset = 0;
        this.mWhetherDrawDot = DEBUG;
        this.mFirstLayout = true;
        this.mTouchFlag = DEBUG;
        this.mFrameChanged = DEBUG;
        this.mWhetherUnion = DEBUG;
        this.mUnionEnable = DEBUG;
        this.mDisplayKey = "";
        this.mActivePointerId = -1;
        this.mBackgroundWidth = -1;
        this.keyIndices = -1;
        this.lastKeyIndices = -1;
        this.mKeyCollectDrawable = null;
        this.mTouchWellDrawable = null;
        this.mKey = new ArrayList();
        this.mKeyText = new ArrayList();
        this.mPreviousIndex = -1;
        this.mFirstIsCharacter = DEBUG;
        this.mDefaultTextColor = null;
        this.mUserTextColor = null;
        this.mTextColor = null;
        this.mDefaultTextSize = 0;
        this.mUserTextSize = 0;
        this.mFontFace = null;
        this.mCollectHighLight = DEBUG;
        this.mInnerClosing = DEBUG;
        this.mPrivateFlags = new ArrayList();
        this.mIconState = new ArrayList();
        this.mContext = context;
        Resources resources = getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, oppo.R.styleable.OppoTouchSearchView, defStyle, 0);
        this.mUnionEnable = a.getBoolean(4, true);
        this.mBackgroundAlignMode = a.getInt(1, 0);
        this.mBackgroundLeftMargin = a.getDimensionPixelOffset(3, 0);
        this.mBackgroundRightMargin = a.getDimensionPixelOffset(2, 0);
        this.mPopupWindowWidth = a.getDimensionPixelOffset(5, -1);
        if (-1 == this.mPopupWindowWidth) {
            this.mPopupWindowWidth = resources.getDimensionPixelOffset(201655299);
        }
        this.mPopupWindowHeight = a.getDimensionPixelOffset(6, -1);
        if (-1 == this.mPopupWindowHeight) {
            this.mPopupWindowHeight = resources.getDimensionPixelOffset(201655300);
            this.mPopupDefaultHeight = this.mPopupWindowHeight;
        }
        this.mPopupWindowMinTop = a.getInteger(7, -1);
        if (-1 == this.mPopupWindowMinTop) {
            this.mPopupWindowMinTop = resources.getInteger(202179588);
        }
        this.mPopupWindowTextSize = a.getDimensionPixelSize(8, -1);
        this.mPopupWindowTextColor = resources.getColor(201720833);
        this.mPopupWindowTextColor = a.getColor(9, this.mPopupWindowTextColor);
        this.mBackgroundRightMargin += resources.getDimensionPixelOffset(201655302);
        this.mPopupWindowTopMargin = resources.getDimensionPixelSize(201655304);
        this.mPopupWindowRightMargin = resources.getDimensionPixelSize(201655303);
        this.mKeyDrawableOffset = resources.getDimensionPixelSize(201655347);
        this.mPopupWinSubHeight = resources.getDimensionPixelSize(201655350);
        this.mPopupWindowLocalx = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getWidth() - this.mPopupWindowRightMargin;
        this.mPopupWindowLocaly = this.mPopupWindowTopMargin;
        this.mDot = resources.getString(201589767);
        this.mDotDrawable = resources.getDrawable(201851919);
        this.mKeyCollectDrawable = a.getDrawable(11);
        this.mTouchWellDrawable = a.getDrawable(12);
        this.mDefaultTextColor = a.getColorStateList(14);
        this.mFirstIsCharacter = a.getBoolean(10, DEBUG);
        if (this.mKeyCollectDrawable != null) {
            this.mKeyDrawableWidth = this.mKeyCollectDrawable.getIntrinsicWidth();
            this.mKeyDrawableHeight = this.mKeyCollectDrawable.getIntrinsicHeight();
        }
        this.mDefaultTextSize = a.getDimensionPixelSize(13, -1);
        if (-1 == this.mDefaultTextSize) {
            this.mDefaultTextSize = resources.getDimensionPixelSize(201655437);
        }
        if (-1 == this.mBackgroundWidth) {
            this.mBackgroundWidth = resources.getDimensionPixelOffset(201655456);
        }
        if (this.mFirstIsCharacter) {
            this.KEYS = resources.getStringArray(201786370);
        } else {
            this.KEYS = resources.getStringArray(201786368);
        }
        this.UNIONKEYS = resources.getStringArray(201786369);
        ViewGroup mPopupContent = (ViewGroup) ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(201917449, null);
        this.mPopupWindow = new PopupWindow(context);
        this.mPopupWindow.setWidth(this.mPopupWindowWidth);
        this.mPopupWindow.setHeight(this.mPopupWindowHeight);
        this.mPopupWindow.setContentView(mPopupContent);
        this.mPopupWindow.setAnimationStyle(201524228);
        this.mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
        this.mPopupTextView = (TextView) mPopupContent.findViewById(201458690);
        int textSize = this.mContext.getResources().getDimensionPixelSize(201654425);
        this.mPopupTextView.setTextSize(0, (float) ((int) ColorChangeTextUtil.getSuitableFontSize((float) textSize, this.mContext.getResources().getConfiguration().fontScale, 4)));
        this.mScrollView = (ScrollView) mPopupContent.findViewById(201458865);
        this.layout = (LinearLayout) mPopupContent.findViewById(201458691);
        a.recycle();
        if (this.mWhetherUnion) {
            initUnionState();
        } else {
            initIconState();
        }
    }

    private void initUnionState() {
        int length = this.UNIONKEYS.length;
        if (length >= 0) {
            int i;
            if (!(this.mFirstIsCharacter || this.mKeyCollectDrawable == null)) {
                this.mKeyText.add(new Key(this.mKeyCollectDrawable, null));
            }
            for (i = getCharacterStartIndex(); i < length - 1; i += 2) {
                this.mKeyText.add(new Key(null, this.UNIONKEYS[i]));
            }
            if (this.mDotDrawable != null) {
                for (i = getCharacterStartIndex() + 1; i < length - 2; i += 2) {
                    this.mKeyText.add(new Key(this.mDotDrawable, null));
                    Key key = new Key();
                    switch (i) {
                        case 2:
                            key.keyOne = "B";
                            key.keyTwo = "C";
                            break;
                        case 4:
                            key.keyOne = "E";
                            key.keyTwo = "F";
                            break;
                        case 6:
                            key.keyOne = "H";
                            break;
                        case 8:
                            key.keyOne = "J";
                            key.keyTwo = "K";
                            break;
                        case 10:
                            key.keyOne = "M";
                            key.keyTwo = "N";
                            break;
                        case 12:
                            key.keyOne = "P";
                            key.keyTwo = "Q";
                            break;
                        case 14:
                            key.keyOne = "S";
                            break;
                        case 16:
                            key.keyOne = "U";
                            key.keyTwo = "V";
                            break;
                        case 18:
                            key.keyOne = "X";
                            key.keyTwo = "Y";
                            break;
                        default:
                            break;
                    }
                }
            }
            if (this.mTouchWellDrawable != null) {
                this.mKeyText.add(new Key(this.mTouchWellDrawable, null));
            }
        }
    }

    private void initIconState() {
        int length = this.KEYS.length;
        if (length >= 0) {
            int i;
            for (i = 0; i < length; i++) {
                this.mKey.add(new Key());
            }
            this.mFontFace = Typeface.DEFAULT;
            this.mKeyText.clear();
            if (!(this.mFirstIsCharacter || this.mKeyCollectDrawable == null)) {
                this.mKeyText.add(new Key(this.mKeyCollectDrawable, null));
            }
            for (i = getCharacterStartIndex(); i < length; i++) {
                this.mKeyText.add(new Key(null, this.KEYS[i]));
            }
            if (this.mTouchWellDrawable != null) {
                this.mKeyText.add(new Key(this.mTouchWellDrawable, null));
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
                refreshIconState(i, ((Key) this.mKeyText.get(i)).getIcon());
                if (this.mTextColor != null) {
                    ((Key) this.mKeyText.get(i)).textPaint.setColor(this.mTextColor.getColorForState(getIconState(i), this.mTextColor.getDefaultColor()));
                }
            }
        }
    }

    protected void refreshIconState(int index, Drawable icon) {
        this.mPrivateFlags.set(index, Integer.valueOf(((Integer) this.mPrivateFlags.get(index)).intValue() | PFLAG_DRAWABLE_STATE_DIRTY));
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
        if ((privateFlags & PFLAG_DRAWABLE_STATE_DIRTY) != 0) {
            this.mIconState.set(index, onCreateIconState(index, 0));
            this.mPrivateFlags.set(index, Integer.valueOf(privateFlags & -1025));
        }
        return (int[]) this.mIconState.get(index);
    }

    protected int[] onCreateIconState(int index, int extraSpace) {
        int mViewFlags = ((Integer) this.mPrivateFlags.get(index)).intValue();
        int viewStateIndex = 0;
        if ((((Integer) this.mPrivateFlags.get(index)).intValue() & PFLAG_PRESSED) != 0) {
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
            privateFlags |= PFLAG_PRESSED;
        } else {
            privateFlags &= -16385;
        }
        this.mPrivateFlags.set(index, Integer.valueOf(privateFlags));
    }

    private void setItemRestore(int index) {
        setIconPressed(index, DEBUG);
        Drawable icon = ((Key) this.mKeyText.get(index)).getIcon();
        String text = ((Key) this.mKeyText.get(index)).getText();
        refreshIconState(index, icon);
        update();
        requestLayout();
        if (text != null && this.mTextColor != null) {
            ((Key) this.mKeyText.get(index)).textPaint.setColor(this.mTextColor.getColorForState(getIconState(index), this.mTextColor.getDefaultColor()));
            update();
            requestLayout();
        }
    }

    private int getCharacterStartIndex() {
        if (this.mFirstIsCharacter) {
            return 0;
        }
        return 1;
    }

    public void onClick(View view) {
        if (view instanceof TextView) {
            this.surname = ((TextView) view).getText();
            this.mTouchSearchActionListener.onNameKey(this.surname);
        }
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        if (!(this.mLeft == left && this.mRight == right && this.mTop == top && this.mBottom == bottom)) {
            this.mFrameChanged = true;
        }
        return super.setFrame(left, top, right, bottom);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        closing();
    }

    static int getCharPositionInArray(String[] charArray, String whichChar) {
        if (whichChar == null || "".equals(whichChar) || charArray == null) {
            return -1;
        }
        int charCount = charArray.length;
        for (int i = 0; i < charCount; i++) {
            if (whichChar.toUpperCase(Locale.ENGLISH).equals(charArray[i])) {
                return i;
            }
        }
        return -1;
    }

    static int getCharPositionInArray(String[] charArray, int from, int to, String whichChar) {
        if (charArray == null || from < 0 || to < 0 || whichChar == null || "".equals(whichChar)) {
            Log.w(TAG, "getCharPositionInArray --- error,  return -1");
            return -1;
        } else if (whichChar.equals("#")) {
            return 0;
        } else {
            if (from > to) {
                Log.w(TAG, "getCharPositionInArray --- not find , return -1");
                return -1;
            }
            int middle = (from + to) / 2;
            if (middle > to || charArray.length == middle) {
                return -1;
            }
            CharSequence lowerCharSequence = whichChar.toUpperCase(Locale.ENGLISH);
            if (sCollator.compare(lowerCharSequence, charArray[middle]) == 0) {
                return middle;
            }
            if (sCollator.compare(lowerCharSequence, charArray[middle]) > 0) {
                return getCharPositionInArray(charArray, middle + 1, to, whichChar);
            }
            return getCharPositionInArray(charArray, from, middle - 1, whichChar);
        }
    }

    private void whetherUnion() {
        if (this.mUnionEnable) {
            int exactHeight = (getHeight() - this.mPaddingTop) - this.mPaddingBottom;
            this.mCellWidth = getWidth();
            this.mCellHeight = exactHeight / this.KEYS.length;
            if (this.mCellHeight >= this.mKeyDrawableHeight || this.mCellHeight >= 0) {
                this.mWhetherUnion = DEBUG;
            } else {
                this.mKeyDrawableHeight = this.mCellHeight;
                this.mKeyDrawableWidth = this.mCellHeight;
                this.mWhetherUnion = DEBUG;
            }
            return;
        }
        this.mWhetherUnion = DEBUG;
    }

    private void update() {
        whetherUnion();
        if (isSectionsValidate()) {
            int keysCount = this.KEYS.length;
            int topPadding = this.mPaddingTop;
            int exactHeight = (getHeight() - this.mPaddingTop) - this.mPaddingBottom;
            this.mCellWidth = getWidth();
            this.mCellHeight = exactHeight / keysCount;
            topPadding += (exactHeight % keysCount) >> 1;
            this.mKeyPaddingY = (this.mCellHeight - this.mKeyDrawableHeight) / 2;
            if (this.mPositionRect != null) {
                this.mKeyPaddingX = this.mPositionRect.left + (((this.mPositionRect.right - this.mPositionRect.left) - this.mKeyDrawableWidth) / 2);
            }
            int y = topPadding;
            for (int i = 0; i < keysCount; i++) {
                ((Key) this.mKey.get(i)).setLeft(this.mKeyPaddingX + 0);
                ((Key) this.mKey.get(i)).setTop(this.mKeyPaddingY + y);
                y += this.mCellHeight;
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0033, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setPopupTextView(String character) {
        if (!this.mPopupWindow.isShowing()) {
            this.mPopupWindow.showAtLocation(this, 0, this.mPopupWindowLocalx, this.mPopupWindowLocaly);
        }
        this.mPopupTextView.setText(character);
        this.keyIndices = (character.charAt(0) - 65) + 2;
        if (character.equals("#")) {
            this.keyIndices = 1;
        }
        int keysCount = this.KEYS.length;
        if (this.keyIndices >= 0 && this.keyIndices <= keysCount - 1 && !this.mWhetherUnion) {
            update();
            requestLayout();
        }
    }

    public void setPopText(String character, String name) {
        if (this.mPopupWindow.isShowing()) {
            this.mPopupWindow.update(this.mPopupWindowWidth, this.mPopupDefaultHeight);
        } else {
            this.mPopupWindow.showAtLocation(this, 0, this.mPopupWindowLocalx, this.mPopupWindowLocaly);
        }
        this.mPopupTextView.setText(name);
        this.keyIndices = (character.charAt(0) - 65) + 2;
        if (character.equals("#")) {
            this.keyIndices = 1;
        }
        int keysCount = this.KEYS.length;
        if (this.keyIndices >= 0 && this.keyIndices <= keysCount - 1) {
            if (!(this.lastKeyIndices == this.keyIndices || (this.mWhetherUnion ^ 1) == 0)) {
                update();
                requestLayout();
            }
            this.lastKeyIndices = this.keyIndices;
        }
    }

    public PopupWindow getPopupWindow() {
        return this.mPopupWindow;
    }

    public void startPostDelayed() {
    }

    public void setTouchSearchActionListener(TouchSearchActionListener listener) {
        this.mTouchSearchActionListener = listener;
    }

    public TouchSearchActionListener getTouchSearchActionListener() {
        return this.mTouchSearchActionListener;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mFirstLayout || this.mFrameChanged) {
            if (this.mSections != null) {
                Log.d(TAG, "the mSections is" + Arrays.toString(this.mSections));
            }
            updateBackGroundBound();
            update();
            if (this.mFirstLayout) {
                this.mFirstLayout = DEBUG;
            }
            if (this.mFrameChanged) {
                this.mFrameChanged = DEBUG;
            }
        }
        if (isLayoutRtl()) {
            this.mPopupWindowLocalx = this.mPopupWindowRightMargin - this.mPopupWindowWidth;
        }
    }

    private void updateBackGroundBound() {
        int exactleft;
        int exactright;
        if (this.mBackgroundAlignMode == 0) {
            exactleft = (getWidth() - this.mBackgroundWidth) / 2;
            exactright = exactleft + this.mBackgroundWidth;
        } else if (this.mBackgroundAlignMode == 2) {
            exactright = getWidth() - this.mBackgroundRightMargin;
            exactleft = exactright - this.mBackgroundWidth;
        } else {
            exactleft = this.mBackgroundLeftMargin;
            exactright = exactleft + this.mBackgroundWidth;
        }
        this.mPositionRect = new Rect(exactleft, 0, exactright, this.mBottom - this.mTop);
    }

    private int findCell(int x, int y, int start, int end, ArrayList<Key> mkeys) {
        if (start > end) {
            return -1;
        }
        int middle = (start + end) / 2;
        int top = ((Key) mkeys.get(middle)).getTop() - this.mKeyPaddingY;
        int bottom = top + this.mCellHeight;
        if (y >= top && y < bottom) {
            return middle;
        }
        if (y < top) {
            return findCell(x, y, start, middle - 1, mkeys);
        }
        return findCell(x, y, middle + 1, end, mkeys);
    }

    private int getKeyIndices(int x, int y, ArrayList<Key> mkeys) {
        int keyCount = this.KEYS.length;
        int primaryIndex = findCell(x, y, 0, keyCount - 1, mkeys);
        if (-1 != primaryIndex) {
            return primaryIndex;
        }
        if (y < ((Key) mkeys.get(0)).getTop() - this.mKeyPaddingY) {
            return 0;
        }
        if (y > ((Key) mkeys.get(keyCount - 1)).getTop() - this.mKeyPaddingY) {
            return keyCount - 1;
        }
        if (y <= ((Key) mkeys.get(0)).getTop() - this.mKeyPaddingY || y >= ((Key) mkeys.get(keyCount - 1)).getTop() - this.mKeyPaddingY) {
            return primaryIndex;
        }
        return keyCount / 2;
    }

    public boolean onTouchEvent(MotionEvent me) {
        if (me.getPointerId(me.getActionIndex()) > 0) {
            return DEBUG;
        }
        switch (me.getAction() & 255) {
            case 0:
                this.mTouchFlag = true;
                this.mActivePointerId = me.getPointerId(0);
                invalidate();
                break;
            case 1:
            case 3:
                this.mActivePointerId = -1;
                this.mTouchFlag = DEBUG;
                this.mDisplayKey = "";
                invalidate();
                break;
            case 2:
                break;
            case 6:
                onSecondaryPointerUp(me);
                break;
        }
        int pointerIndex = me.findPointerIndex(this.mActivePointerId);
        invalidateKey((int) me.getX(pointerIndex), (int) me.getY(pointerIndex));
        return true;
    }

    private void invalidateKey(int x, int y) {
        if (isSectionsValidate()) {
            int index;
            CharSequence willDisplay;
            if (this.mWhetherUnion) {
                index = getKeyIndices(x, y, this.mKey);
            } else {
                index = getKeyIndices(x, y, this.mKey);
            }
            if (this.mWhetherUnion) {
                Key key = new Key();
                this.keyIndices = index;
                key.mKeyLabel = this.UNIONKEYS[this.keyIndices];
                willDisplay = key.getTextToDisplay(x, y, this.mCellHeight, this.mDot);
            } else {
                this.keyIndices = index;
                willDisplay = this.KEYS[this.keyIndices];
            }
            if (!(willDisplay == null || (willDisplay.equals(this.mDot) ^ 1) == 0)) {
                onKeyChanged(willDisplay.toString(), ((Key) this.mKey.get(this.keyIndices)).getLeft() - this.mKeyPaddingX, ((Key) this.mKey.get(this.keyIndices)).getTop() - this.mKeyPaddingY);
                this.mDisplayKey = willDisplay.toString();
                if (this.mTouchSearchActionListener != null) {
                    this.mTouchSearchActionListener.onKey(this.mDisplayKey);
                }
                if (!this.mWhetherUnion) {
                    int keysCount = this.KEYS.length;
                    if (!(this.keyIndices == this.mPreviousIndex || -1 == this.keyIndices)) {
                        this.mCollectHighLight = true;
                        setIconPressed(this.keyIndices, true);
                        Drawable icon = ((Key) this.mKeyText.get(this.keyIndices)).getIcon();
                        String text = ((Key) this.mKeyText.get(this.keyIndices)).getText();
                        refreshIconState(this.keyIndices, icon);
                        update();
                        requestLayout();
                        if (!(text == null || this.mTextColor == null)) {
                            ((Key) this.mKeyText.get(this.keyIndices)).textPaint.setColor(this.mTextColor.getColorForState(getIconState(this.keyIndices), this.mTextColor.getDefaultColor()));
                            invalidate();
                            update();
                            requestLayout();
                        }
                    }
                    if (!(-1 == this.mPreviousIndex || this.keyIndices == this.mPreviousIndex || this.mPreviousIndex >= this.KEYS.length)) {
                        setItemRestore(this.mPreviousIndex);
                    }
                    this.mPreviousIndex = this.keyIndices;
                }
            }
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = (ev.getAction() & 65280) >> 8;
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            this.mActivePointerId = ev.getPointerId(pointerIndex == 0 ? 1 : 0);
        }
    }

    private void onKeyChanged(CharSequence display, int key_x, int key_y) {
        if (this.mPopupWindow != null) {
            this.mPopupTextView.setText(display);
            int[] coordinate = new int[2];
            getLocationInWindow(coordinate);
            int localx = (coordinate[0] + key_x) - this.mPopupWindowWidth;
            if ((coordinate[1] + key_y) - (this.mPopupWindowHeight >> 1) < this.mPopupWindowMinTop) {
                int localy = this.mPopupWindowMinTop;
            }
            if (display.equals("*")) {
                int index = this.mPreviousIndex;
                this.mInnerClosing = true;
                closing();
                this.mInnerClosing = DEBUG;
                this.mPreviousIndex = index;
            } else if (this.mPopupWindow.isShowing()) {
                this.mPopupWindow.update(this.mPopupWindowLocalx, this.mPopupWindowLocaly, this.mPopupWindowWidth, this.mPopupWindowHeight);
            } else {
                this.mPopupWindow.showAtLocation(this, 0, this.mPopupWindowLocalx, this.mPopupWindowLocaly);
            }
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mWhetherUnion) {
            drawUnionKeys(canvas);
        } else {
            drawKeys(canvas);
        }
    }

    private void drawUnionKeys(Canvas canvas) {
        int left;
        int top;
        int i;
        if (!(this.mFirstIsCharacter || ((Key) this.mKeyText.get(0)).getIcon() == null)) {
            left = ((Key) this.mKey.get(0)).getLeft();
            top = ((Key) this.mKey.get(0)).getTop();
            this.mKeyCollectDrawable.setBounds(left, top, this.mKeyDrawableWidth + left, this.mKeyDrawableHeight + top);
            this.mKeyCollectDrawable.draw(canvas);
        }
        int keysCount = this.UNIONKEYS.length;
        for (i = getCharacterStartIndex(); i < keysCount - 1; i += 2) {
            FontMetricsInt mFmi = ((Key) this.mKeyText.get(i)).textPaint.getFontMetricsInt();
            TextPaint paint = ((Key) this.mKeyText.get(i)).textPaint;
            String character = this.KEYS[i];
            if (character != null) {
                Canvas canvas2 = canvas;
                canvas2.drawText(character, (float) (((Key) this.mKey.get(i)).getLeft() + ((this.mKeyDrawableWidth - ((int) paint.measureText(character))) / 2)), (float) (((Key) this.mKey.get(i)).getTop() + (((this.mKeyDrawableHeight - (mFmi.bottom - mFmi.top)) / 2) - mFmi.top)), paint);
            }
        }
        for (i = getCharacterStartIndex() + 1; i < keysCount - 2; i += 2) {
            if (((Key) this.mKeyText.get(i)).getIcon() != null) {
                left = ((Key) this.mKey.get(i)).getLeft();
                top = ((Key) this.mKey.get(i)).getTop();
                this.mDotDrawable.setBounds(left, top, this.mKeyDrawableWidth + left, this.mKeyDrawableHeight + top);
                this.mDotDrawable.draw(canvas);
            }
        }
        int j = keysCount - 1;
        if (((Key) this.mKeyText.get(j)).getIcon() != null) {
            left = ((Key) this.mKey.get(j)).getLeft();
            top = ((Key) this.mKey.get(j)).getTop();
            this.mTouchWellDrawable.setBounds(left, top, this.mKeyDrawableWidth + left, this.mKeyDrawableHeight + top);
            this.mTouchWellDrawable.draw(canvas);
        }
    }

    private void drawKeys(Canvas canvas) {
        if (isSectionsValidate()) {
            int left;
            int top;
            if (!(this.mFirstIsCharacter || ((Key) this.mKeyText.get(0)).getIcon() == null)) {
                left = ((Key) this.mKey.get(0)).getLeft();
                top = ((Key) this.mKey.get(0)).getTop();
                this.mKeyCollectDrawable.setBounds(left, top, this.mKeyDrawableWidth + left, this.mKeyDrawableHeight + top);
                this.mKeyCollectDrawable.draw(canvas);
            }
            int keysCount = this.KEYS.length;
            for (int i = getCharacterStartIndex(); i < keysCount; i++) {
                FontMetricsInt mFmi = ((Key) this.mKeyText.get(i)).textPaint.getFontMetricsInt();
                TextPaint paint = ((Key) this.mKeyText.get(i)).textPaint;
                String character = this.KEYS[i];
                if (character != null) {
                    Canvas canvas2 = canvas;
                    canvas2.drawText(character, (float) (((Key) this.mKey.get(i)).getLeft() + ((this.mKeyDrawableWidth - ((int) paint.measureText(character))) / 2)), (float) (((Key) this.mKey.get(i)).getTop() + (((this.mKeyDrawableHeight - (mFmi.bottom - mFmi.top)) / 2) - mFmi.top)), paint);
                }
            }
            int j = keysCount - 1;
            if (((Key) this.mKeyText.get(j)).getIcon() != null) {
                left = ((Key) this.mKey.get(j)).getLeft();
                top = ((Key) this.mKey.get(j)).getTop();
                this.mTouchWellDrawable.setBounds(left, top, this.mKeyDrawableWidth + left, this.mKeyDrawableHeight + top);
                this.mTouchWellDrawable.draw(canvas);
            }
        }
    }

    public void closing() {
        int size;
        if (!(-1 == this.mPreviousIndex || this.keyIndices == this.mPreviousIndex || this.mPreviousIndex >= this.KEYS.length)) {
            setItemRestore(this.mPreviousIndex);
        }
        if (!(this.mPopupWindow.isShowing() || this.mWhetherUnion)) {
            size = this.KEYS.length;
            if (this.keyIndices > -1 && this.keyIndices < size && this.KEYS[this.keyIndices] != null && "*".equals(this.KEYS[this.keyIndices]) && this.mCollectHighLight && (this.mInnerClosing ^ 1) != 0) {
                setItemRestore(this.keyIndices);
                this.mCollectHighLight = DEBUG;
                this.mPreviousIndex = -1;
            }
        }
        if (this.mPopupWindow.isShowing()) {
            if (!this.mWhetherUnion) {
                size = this.KEYS.length;
                if (this.keyIndices > -1 && this.keyIndices < size) {
                    setItemRestore(this.keyIndices);
                    update();
                    requestLayout();
                    this.mPreviousIndex = -1;
                }
            }
            this.mPopupWindow.dismiss();
        }
    }

    public void setName(String[] firstname) {
        Resources resources = getResources();
        if (length > 0) {
            this.mPopupTextView.setBackgroundDrawable(this.mContext.getDrawable(201852063));
            this.mScrollView.setVisibility(0);
            this.layout.setVisibility(0);
        } else {
            this.mPopupTextView.setBackgroundDrawable(this.mContext.getDrawable(201852064));
            this.mScrollView.setVisibility(8);
            this.layout.setVisibility(8);
        }
        this.layout.removeAllViews();
        LayoutInflater inflate = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        LayoutParams layoutParams = new LayoutParams(this.mPopupWindowWidth, this.mPopupWinSubHeight);
        for (CharSequence text : firstname) {
            TextView textview = (TextView) inflate.inflate(201917520, null);
            textview.setTextSize(0, (float) ((int) ColorChangeTextUtil.getSuitableFontSize((float) this.mContext.getResources().getDimensionPixelSize(201654425), this.mContext.getResources().getConfiguration().fontScale, 4)));
            textview.setText(text);
            this.layout.addView(textview, layoutParams);
            textview.setOnClickListener(this);
            textview.setBackgroundResource(201852142);
        }
        if (length > 7) {
            this.mPopupWindowHeight = (this.mPopupDefaultHeight + (this.mPopupWinSubHeight * 7)) + (this.mPopupWinSubHeight / 2);
        } else {
            this.mPopupWindowHeight = this.mPopupDefaultHeight + (this.mPopupWinSubHeight * length);
        }
        if (this.mPopupWindow.isShowing()) {
            this.mPopupWindow.update(this.mPopupWindowWidth, this.mPopupWindowHeight);
        }
    }

    public void setBackgroundDrawable(Drawable d) {
        super.setBackgroundDrawable(null);
    }

    public void setTouchBackgroundDrawable(Drawable d) {
    }

    public void setUnionEnable(boolean unionEnable) {
        if (this.mUnionEnable != unionEnable) {
            this.mUnionEnable = unionEnable;
            update();
            invalidate();
        }
    }

    public void setPopupWindowSize(int width, int height) {
        if (this.mPopupWindowWidth != width || this.mPopupWindowHeight != height) {
            this.mPopupWindowWidth = width;
            this.mPopupWindowHeight = height;
            this.mPopupWindow.setWidth(this.mPopupWindowWidth);
            this.mPopupWindow.setHeight(this.mPopupWindowHeight);
            invalidate();
        }
    }

    public void setPopupWindowTopMinCoordinate(int top) {
        if (this.mPopupWindowMinTop != top) {
            this.mPopupWindowMinTop = top;
        }
    }

    public void setPopupWindowTextSize(int textSize) {
        if (this.mPopupWindowTextSize != textSize) {
            this.mPopupWindowTextSize = textSize;
            this.mPopupTextView.setTextSize((float) this.mPopupWindowTextSize);
            invalidate();
        }
    }

    public void setPopupWindowTextColor(int textColor) {
        if (this.mPopupWindowTextColor != textColor) {
            this.mPopupWindowTextColor = textColor;
            this.mPopupTextView.setTextColor(this.mPopupWindowTextColor);
            invalidate();
        }
    }

    public void setCharTextColor(ColorStateList colors) {
        if (colors != null) {
            this.mUserTextColor = colors;
        }
    }

    public void setCharTextSize(int size) {
        if (size != 0) {
            this.mUserTextSize = size;
        }
    }

    private boolean isSectionsValidate() {
        if (this.mSections == null) {
            return true;
        }
        if (this.mSections == null || (this.mSections[0].equals(" ") ^ 1) == 0 || this.mSections.length < 5) {
            return DEBUG;
        }
        return true;
    }

    public void setSmartShowMode(Object[] sections, int[] counts) {
        if (sections == null || counts == null || ((String) sections[0]).equals(" ")) {
            this.mSections = new String[]{" "};
            invalidate();
            return;
        }
        int secLength = sections.length;
        int cntLength = counts.length;
        int start;
        if (secLength > 27) {
            this.mWhetherDrawDot = true;
            this.mSections = new String[45];
            this.mSections[0] = (String) sections[0];
            this.mSections[1] = this.mDot.toString();
            this.mSections[44] = (String) sections[secLength - 1];
            start = 2;
            cntLength--;
            int[] cloneCnt = (int[]) counts.clone();
            for (int length = 21; length > 0; length--) {
                int pos = 0;
                int max = 0;
                for (int i = 1; i < cntLength; i++) {
                    if (cloneCnt[i] > max) {
                        max = cloneCnt[i];
                        pos = i;
                    }
                }
                cloneCnt[pos] = 0;
            }
            for (int j = 1; j < cntLength; j++) {
                if (cloneCnt[j] == 0) {
                    this.mSections[start] = (String) sections[j];
                    this.mSections[start + 1] = this.mDot.toString();
                    start += 2;
                }
            }
        } else {
            int secStart;
            this.mWhetherDrawDot = DEBUG;
            int i2 = secLength;
            this.mSections = new String[(secLength + 0)];
            int secStart2 = 0;
            int start2 = 0;
            while (secStart2 < secLength) {
                start = start2 + 1;
                secStart = secStart2 + 1;
                this.mSections[start2] = (String) sections[secStart2];
                secStart2 = secStart;
                start2 = start;
            }
            secStart = secStart2;
            start = start2;
        }
        this.KEYS = this.mSections;
        initIconState();
        update();
        invalidate();
    }
}
