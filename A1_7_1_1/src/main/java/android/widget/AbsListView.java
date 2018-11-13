package android.widget;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.os.StrictMode;
import android.os.StrictMode.Span;
import android.os.SystemProperties;
import android.os.Trace;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.StateSet;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.BaseSavedState;
import android.view.ViewConfiguration;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.IntToString;
import android.view.ViewHierarchyEncoder;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.animation.Interpolator;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputContentInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Filter.FilterListener;
import android.widget.RemoteViews.OnClickHandler;
import android.widget.RemoteViewsAdapter.RemoteAdapterConnectionCallback;
import com.android.internal.R;
import com.android.internal.telephony.PhoneConstants;
import com.oppo.luckymoney.LMManager;
import java.util.ArrayList;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class AbsListView extends AdapterView<ListAdapter> implements TextWatcher, OnGlobalLayoutListener, FilterListener, OnTouchModeChangeListener, RemoteAdapterConnectionCallback {
    private static final int CHECK_POSITION_SEARCH_DISTANCE = 20;
    public static final int CHOICE_MODE_MULTIPLE = 2;
    public static final int CHOICE_MODE_MULTIPLE_MODAL = 3;
    public static final int CHOICE_MODE_NONE = 0;
    public static final int CHOICE_MODE_SINGLE = 1;
    private static final int DBG_DATACHANGE_FLAG = 2;
    private static final int DBG_DRAW_FLAG = 16;
    private static final int DBG_FLAG = 32;
    private static final int DBG_LAYOUT_FLAG = 4;
    private static final int DBG_MOTION_FLAG = 1;
    private static final int DBG_OOM_VALUE = 50;
    private static final int DBG_RECYCLE_FLAG = 8;
    private static final int DBG_SELECTOR_FLAG = 64;
    private static final int DBG_TIMEOUT_VALUE = 200;
    static final int EXCEPTION_NUM = 100;
    static final int EXCEPTION_TIME_GAP = 50;
    private static final int INVALID_POINTER = -1;
    private static final boolean IS_ENG_BUILD = false;
    static final int LAYOUT_FORCE_BOTTOM = 3;
    static final int LAYOUT_FORCE_TOP = 1;
    static final int LAYOUT_MOVE_SELECTION = 6;
    static final int LAYOUT_NORMAL = 0;
    static final int LAYOUT_SET_SELECTION = 2;
    static final int LAYOUT_SPECIFIC = 4;
    static final int LAYOUT_SYNC = 5;
    private static final String LOG_PROPERTY_NAME = "debug.listview.dumpinfo";
    static final int[] LONG_FORMAT = null;
    private static final int OVERFLING_VELOCITY_MAGN = 80;
    static final int OVERSCROLL_LIMIT_DIVISOR = 3;
    private static final boolean PROFILE_FLINGING = false;
    private static final boolean PROFILE_SCROLLING = false;
    private static final String TAG = "AbsListView";
    static final int TOUCH_MODE_DONE_WAITING = 2;
    static final int TOUCH_MODE_DOWN = 0;
    static final int TOUCH_MODE_FLING = 4;
    private static final int TOUCH_MODE_OFF = 1;
    private static final int TOUCH_MODE_ON = 0;
    static final int TOUCH_MODE_OVERFLING = 6;
    static final int TOUCH_MODE_OVERSCROLL = 5;
    static final int TOUCH_MODE_REST = -1;
    static final int TOUCH_MODE_SCROLL = 3;
    static final int TOUCH_MODE_TAP = 1;
    private static final int TOUCH_MODE_UNKNOWN = -1;
    public static final int TRANSCRIPT_MODE_ALWAYS_SCROLL = 2;
    public static final int TRANSCRIPT_MODE_DISABLED = 0;
    public static final int TRANSCRIPT_MODE_NORMAL = 1;
    static long constantEndFlingNum;
    static boolean isEnableEndFlingProtect;
    static long lastEndFlingTime;
    private static boolean sDbg;
    private static boolean sDbgDataChange;
    private static boolean sDbgDraw;
    private static boolean sDbgLayout;
    private static boolean sDbgMotion;
    private static boolean sDbgRecycle;
    private static boolean sDbgSelector;
    static final Interpolator sLinearInterpolator = null;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Hairong.Zou@Plf.SDK : Add for ListView back to top", property = OppoRomType.ROM)
    private final int BACK_TO_TOP_SPEED;
    private ListItemAccessibilityDelegate mAccessibilityDelegate;
    private int mActivePointerId;
    ListAdapter mAdapter;
    boolean mAdapterHasStableIds;
    private int mCacheColorHint;
    boolean mCachingActive;
    boolean mCachingStarted;
    SparseBooleanArray mCheckStates;
    LongSparseArray<Integer> mCheckedIdStates;
    int mCheckedItemCount;
    ActionMode mChoiceActionMode;
    int mChoiceMode;
    private Runnable mClearScrollingCache;
    private ContextMenuInfo mContextMenuInfo;
    AdapterDataSetObserver mDataSetObserver;
    private InputConnection mDefInputConnection;
    private boolean mDeferNotifyDataSetChanged;
    private float mDensityScale;
    private int mDirection;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Xiaokang.Feng@Plf.SDK : Add for prevent touch", property = OppoRomType.ROM)
    private int mDownX;
    boolean mDrawSelectorOnTop;
    private EdgeEffect mEdgeGlowBottom;
    private EdgeEffect mEdgeGlowTop;
    private FastScroller mFastScroll;
    boolean mFastScrollAlwaysVisible;
    boolean mFastScrollEnabled;
    private int mFastScrollStyle;
    private boolean mFiltered;
    private int mFirstPositionDistanceGuess;
    private boolean mFlingProfilingStarted;
    private FlingRunnable mFlingRunnable;
    private Span mFlingStrictSpan;
    private boolean mForceTranscriptScroll;
    private boolean mGlobalLayoutListenerAddedFilter;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Xiaokang.Feng@Plf.SDK : Add for prevent touch", property = OppoRomType.ROM)
    private boolean mHasIgnore;
    private boolean mHasPerformedLongPress;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Xiaokang.Feng@Plf.SDK : Add for prevent touch", property = OppoRomType.ROM)
    private int mIgnoreLeft;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Xiaokang.Feng@Plf.SDK : Add for prevent touch", property = OppoRomType.ROM)
    private int mIgnoreRight;
    private boolean mIsChildViewEnabled;
    private boolean mIsDetaching;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoKang.Feng@Plf.SDK : Add for prevent touch", property = OppoRomType.ROM)
    private boolean mIsMutiPoint;
    final boolean[] mIsScrap;
    private int mLastAccessibilityScrollEventFromIndex;
    private int mLastAccessibilityScrollEventToIndex;
    private int mLastHandledItemCount;
    private int mLastPositionDistanceGuess;
    private int mLastScrollState;
    private int mLastTouchMode;
    int mLastY;
    int mLayoutMode;
    Rect mListPadding;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    int mMotionCorrection;
    int mMotionPosition;
    int mMotionViewNewTop;
    int mMotionViewOriginalTop;
    int mMotionX;
    int mMotionY;
    MultiChoiceModeWrapper mMultiChoiceModeCallback;
    private int mNestedYOffset;
    private OnScrollListener mOnScrollListener;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "YaoJun.Luo@Plf.SDK : Add for oppo property mOverflingDistance and mOverscrollDistance add a scale", property = OppoRomType.ROM)
    private float mOverScale;
    int mOverflingDistance;
    int mOverscrollDistance;
    int mOverscrollMax;
    private final Thread mOwnerThread;
    private CheckForKeyLongPress mPendingCheckForKeyLongPress;
    private CheckForLongPress mPendingCheckForLongPress;
    private CheckForTap mPendingCheckForTap;
    private SavedState mPendingSync;
    private PerformClick mPerformClick;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Xiaokang.Feng@Plf.SDK : Add for prevent touch", property = OppoRomType.ROM)
    private int mPointDownX;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Xiaokang.Feng@Plf.SDK : Add for prevent touch", property = OppoRomType.ROM)
    private int mPointUpX;
    PopupWindow mPopup;
    private boolean mPopupHidden;
    Runnable mPositionScrollAfterLayout;
    AbsPositionScroller mPositionScroller;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Xiaokang.Feng@Plf.SDK : Add for prevent touch", property = OppoRomType.ROM)
    private View mPresedChild;
    private InputConnectionWrapper mPublicInputConnection;
    final RecycleBin mRecycler;
    private RemoteViewsAdapter mRemoteAdapter;
    int mResurrectToPosition;
    private final int[] mScrollConsumed;
    View mScrollDown;
    private final int[] mScrollOffset;
    private boolean mScrollProfilingStarted;
    private Span mScrollStrictSpan;
    View mScrollUp;
    boolean mScrollingCacheEnabled;
    int mSelectedTop;
    int mSelectionBottomPadding;
    int mSelectionLeftPadding;
    int mSelectionRightPadding;
    int mSelectionTopPadding;
    Drawable mSelector;
    int mSelectorPosition;
    Rect mSelectorRect;
    private int[] mSelectorState;
    private boolean mSmoothScrollbarEnabled;
    boolean mStackFromBottom;
    EditText mTextFilter;
    private boolean mTextFilterEnabled;
    private final float[] mTmpPoint;
    private Rect mTouchFrame;
    int mTouchMode;
    private Runnable mTouchModeReset;
    private int mTouchSlop;
    private int mTranscriptMode;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Xiaokang.Feng@Plf.SDK : Add for prevent touch", property = OppoRomType.ROM)
    private int mUpX;
    private float mVelocityScale;
    private VelocityTracker mVelocityTracker;
    int mWidthMeasureSpec;

    /* renamed from: android.widget.AbsListView$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ AbsListView this$0;
        final /* synthetic */ boolean val$enabled;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.1.<init>(android.widget.AbsListView, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass1(android.widget.AbsListView r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.1.<init>(android.widget.AbsListView, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.1.<init>(android.widget.AbsListView, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.1.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.1.run():void");
        }
    }

    /* renamed from: android.widget.AbsListView$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ AbsListView this$0;
        final /* synthetic */ boolean val$alwaysShow;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.2.<init>(android.widget.AbsListView, boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass2(android.widget.AbsListView r1, boolean r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.2.<init>(android.widget.AbsListView, boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.2.<init>(android.widget.AbsListView, boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.2.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.2.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.2.run():void");
        }
    }

    /* renamed from: android.widget.AbsListView$4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ AbsListView this$0;

        AnonymousClass4(AbsListView this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            if (this.this$0.mCachingStarted) {
                AbsListView absListView = this.this$0;
                this.this$0.mCachingActive = false;
                absListView.mCachingStarted = false;
                this.this$0.setChildrenDrawnWithCacheEnabled(false);
                if ((this.this$0.mPersistentDrawingCache & 2) == 0) {
                    this.this$0.setChildrenDrawingCacheEnabled(false);
                }
                if (!this.this$0.isAlwaysDrawnWithCacheEnabled()) {
                    this.this$0.invalidate();
                }
            }
        }
    }

    static abstract class AbsPositionScroller {
        public abstract void start(int i);

        public abstract void start(int i, int i2);

        public abstract void startWithOffset(int i, int i2);

        public abstract void startWithOffset(int i, int i2, int i3);

        public abstract void stop();

        AbsPositionScroller() {
        }
    }

    class AdapterDataSetObserver extends AdapterDataSetObserver {
        final /* synthetic */ AbsListView this$0;

        AdapterDataSetObserver(AbsListView this$0) {
            this.this$0 = this$0;
            super();
        }

        public void onChanged() {
            super.onChanged();
            if (this.this$0.mFastScroll != null) {
                this.this$0.mFastScroll.onSectionsChanged();
            }
        }

        public void onInvalidated() {
            super.onInvalidated();
            if (this.this$0.mFastScroll != null) {
                this.this$0.mFastScroll.onSectionsChanged();
            }
        }
    }

    private class WindowRunnnable {
        private int mOriginalAttachCount;
        final /* synthetic */ AbsListView this$0;

        /* synthetic */ WindowRunnnable(AbsListView this$0, WindowRunnnable windowRunnnable) {
            this(this$0);
        }

        private WindowRunnnable(AbsListView this$0) {
            this.this$0 = this$0;
        }

        public void rememberWindowAttachCount() {
            this.mOriginalAttachCount = this.this$0.getWindowAttachCount();
        }

        public boolean sameWindow() {
            return this.this$0.getWindowAttachCount() == this.mOriginalAttachCount;
        }
    }

    private class CheckForKeyLongPress extends WindowRunnnable implements Runnable {
        final /* synthetic */ AbsListView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.CheckForKeyLongPress.<init>(android.widget.AbsListView):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        private CheckForKeyLongPress(android.widget.AbsListView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.CheckForKeyLongPress.<init>(android.widget.AbsListView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.CheckForKeyLongPress.<init>(android.widget.AbsListView):void");
        }

        /* synthetic */ CheckForKeyLongPress(AbsListView this$0, CheckForKeyLongPress checkForKeyLongPress) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.CheckForKeyLongPress.run():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.CheckForKeyLongPress.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.CheckForKeyLongPress.run():void");
        }
    }

    private class CheckForLongPress extends WindowRunnnable implements Runnable {
        private static final int INVALID_COORD = -1;
        private float mX;
        private float mY;
        final /* synthetic */ AbsListView this$0;

        /* synthetic */ CheckForLongPress(AbsListView this$0, CheckForLongPress checkForLongPress) {
            this(this$0);
        }

        private CheckForLongPress(AbsListView this$0) {
            this.this$0 = this$0;
            super(this$0, null);
            this.mX = -1.0f;
            this.mY = -1.0f;
        }

        private void setCoords(float x, float y) {
            this.mX = x;
            this.mY = y;
        }

        public void run() {
            View child = this.this$0.getChildAt(this.this$0.mMotionPosition - this.this$0.mFirstPosition);
            if (child != null) {
                int longPressPosition = this.this$0.mMotionPosition;
                long longPressId = this.this$0.mAdapter.getItemId(this.this$0.mMotionPosition);
                boolean handled = false;
                if (sameWindow() && !this.this$0.mDataChanged) {
                    handled = (this.mX == -1.0f || this.mY == -1.0f) ? this.this$0.performLongPress(child, longPressPosition, longPressId) : this.this$0.performLongPress(child, longPressPosition, longPressId, this.mX, this.mY);
                }
                if (handled) {
                    this.this$0.mHasPerformedLongPress = true;
                    this.this$0.mTouchMode = -1;
                    this.this$0.setPressed(false);
                    child.setPressed(false);
                    return;
                }
                this.this$0.mTouchMode = 2;
            }
        }
    }

    private final class CheckForTap implements Runnable {
        final /* synthetic */ AbsListView this$0;
        float x;
        float y;

        /* synthetic */ CheckForTap(AbsListView this$0, CheckForTap checkForTap) {
            this(this$0);
        }

        private CheckForTap(AbsListView this$0) {
            this.this$0 = this$0;
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "XiaoKang.Feng@Plf.SDK, 2016-12-25 : Modify for prevent touch", property = OppoRomType.ROM)
        public void run() {
            if (this.this$0.mTouchMode == 0) {
                this.this$0.mTouchMode = 1;
                View child = this.this$0.getChildAt(this.this$0.mMotionPosition - this.this$0.mFirstPosition);
                this.this$0.mPresedChild = child;
                if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                    Log.d(AbsListView.TAG, "CheckForTap:mFirstPosition = " + this.this$0.mFirstPosition + ",mMotionPosition = " + this.this$0.mMotionPosition + ",child = " + child);
                }
                if (child != null && !child.hasFocusable()) {
                    this.this$0.setListLayoutMode(0);
                    boolean runTap = this.this$0.mIsMutiPoint ? this.this$0.isInIgnoreArea(this.this$0.mDownX) && !this.this$0.isInIgnoreArea(this.this$0.mPointDownX) : !this.this$0.isInIgnoreArea(this.this$0.mDownX);
                    if (this.this$0.mDataChanged || (this.this$0.isOppoStyle() && this.this$0.mHasIgnore && !runTap)) {
                        this.this$0.mTouchMode = 2;
                        return;
                    }
                    float[] point = this.this$0.mTmpPoint;
                    point[0] = this.x;
                    point[1] = this.y;
                    this.this$0.transformPointToViewLocal(point, child);
                    child.drawableHotspotChanged(point[0], point[1]);
                    child.setPressed(true);
                    this.this$0.setPressed(true);
                    this.this$0.layoutChildren();
                    this.this$0.positionSelector(this.this$0.mMotionPosition, child);
                    this.this$0.refreshDrawableState();
                    int longPressTimeout = ViewConfiguration.getLongPressTimeout();
                    boolean longClickable = this.this$0.isLongClickable();
                    if (this.this$0.mSelector != null) {
                        Drawable d = this.this$0.mSelector.getCurrent();
                        if (d != null && (d instanceof TransitionDrawable)) {
                            if (longClickable) {
                                ((TransitionDrawable) d).startTransition(longPressTimeout);
                            } else {
                                ((TransitionDrawable) d).resetTransition();
                            }
                        }
                        this.this$0.mSelector.setHotspot(this.x, this.y);
                    }
                    if (longClickable) {
                        if (this.this$0.mPendingCheckForLongPress == null) {
                            this.this$0.mPendingCheckForLongPress = new CheckForLongPress(this.this$0, null);
                        }
                        this.this$0.mPendingCheckForLongPress.setCoords(this.x, this.y);
                        this.this$0.mPendingCheckForLongPress.rememberWindowAttachCount();
                        this.this$0.postDelayed(this.this$0.mPendingCheckForLongPress, (long) longPressTimeout);
                        return;
                    }
                    this.this$0.mTouchMode = 2;
                }
            }
        }
    }

    private class FlingRunnable implements Runnable {
        private static final int FLYWHEEL_TIMEOUT = 40;
        private final Runnable mCheckFlywheel;
        private int mLastFlingY;
        private final OverScroller mScroller;
        @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoLang.Feng@Plf.SDK : Modify for Scroll Interpolator", property = OppoRomType.ROM)
        private boolean mSmoothScroll;
        final /* synthetic */ AbsListView this$0;

        /* renamed from: android.widget.AbsListView$FlingRunnable$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ FlingRunnable this$1;

            AnonymousClass1(FlingRunnable this$1) {
                this.this$1 = this$1;
            }

            public void run() {
                int activeId = this.this$1.this$0.mActivePointerId;
                VelocityTracker vt = this.this$1.this$0.mVelocityTracker;
                OverScroller scroller = this.this$1.mScroller;
                if (vt == null || activeId == -1) {
                    if (AbsListView.IS_ENG_BUILD && AbsListView.sDbg) {
                        Log.d(AbsListView.TAG, "Direct return : vt = " + vt + ",activeId = " + activeId + ",mScrollY = " + this.this$1.this$0.mScrollY);
                    }
                    return;
                }
                vt.computeCurrentVelocity(1000, (float) this.this$1.this$0.mMaximumVelocity);
                float yvel = -vt.getYVelocity(activeId);
                if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                    Log.d(AbsListView.TAG, "Fly wheel: yvel = " + yvel + ",activeId = " + activeId + ",mScrollY = " + this.this$1.this$0.mScrollY + ",mTouchMode = " + this.this$1.this$0.mTouchMode + ",mMinimumVelocity = " + this.this$1.this$0.mMinimumVelocity + ",ListView = " + this.this$1.this$0);
                }
                if (Math.abs(yvel) < ((float) this.this$1.this$0.mMinimumVelocity) || !scroller.isScrollingInDirection(0.0f, yvel)) {
                    this.this$1.endFling();
                    this.this$1.this$0.mTouchMode = 3;
                    this.this$1.this$0.reportScrollStateChange(1);
                } else {
                    this.this$1.this$0.postDelayed(this, 40);
                }
            }
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "LiangJing.Fu@Plf.Framework : modify for mScroller of OppoOverScroller", property = OppoRomType.ROM)
        FlingRunnable(AbsListView this$0) {
            this.this$0 = this$0;
            this.mCheckFlywheel = new AnonymousClass1(this);
            this.mSmoothScroll = false;
            this.mScroller = OppoOverScroller.newInstance(this$0.getContext());
        }

        void start(int initialVelocity) {
            int initialY;
            if (initialVelocity < 0) {
                initialY = Integer.MAX_VALUE;
            } else {
                initialY = 0;
            }
            this.mLastFlingY = initialY;
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                Log.d(AbsListView.TAG, "start: touch mode = " + this.this$0.mTouchMode + ",mScrollY = " + this.this$0.mScrollY + ",initialVelocity = " + initialVelocity + ",mLastFlingY = " + this.mLastFlingY);
            }
            this.mScroller.setInterpolator(null);
            this.mScroller.fling(0, initialY, 0, initialVelocity, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            this.this$0.mTouchMode = 4;
            this.this$0.postOnAnimation(this);
            if (this.this$0.mFlingStrictSpan == null) {
                this.this$0.mFlingStrictSpan = StrictMode.enterCriticalSpan("AbsListView-fling");
            }
        }

        void startSpringback() {
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                Log.d(AbsListView.TAG, "startSpringback: touch mode = " + this.this$0.mTouchMode + ",mScrollY = " + this.this$0.mScrollY);
            }
            if (this.mScroller.springBack(0, this.this$0.mScrollY, 0, 0, 0, 0)) {
                this.this$0.mTouchMode = 6;
                this.this$0.invalidate();
                this.this$0.postOnAnimation(this);
                return;
            }
            this.this$0.mTouchMode = -1;
            this.this$0.reportScrollStateChange(0);
        }

        void startOverfling(int initialVelocity) {
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                Log.d(AbsListView.TAG, "startOverfling: touch mode = " + this.this$0.mTouchMode + ",mScrollY = " + this.this$0.mScrollY + ",initialVelocity = " + initialVelocity + ",mMinimumVelocity = " + this.this$0.mMinimumVelocity + ",getHeight() = " + this.this$0.getHeight());
            }
            this.mScroller.setInterpolator(null);
            this.mScroller.fling(0, this.this$0.mScrollY, 0, initialVelocity, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, this.this$0.getHeight());
            this.this$0.mTouchMode = 6;
            this.this$0.invalidate();
            this.this$0.postOnAnimation(this);
        }

        void edgeReached(int delta) {
            this.mScroller.notifyVerticalEdgeReached(this.this$0.mScrollY, 0, this.this$0.mOverflingDistance);
            int overscrollMode = this.this$0.getOverScrollMode();
            if (overscrollMode == 0 || (overscrollMode == 1 && !this.this$0.contentFits())) {
                this.this$0.mTouchMode = 6;
                int vel = (int) this.mScroller.getCurrVelocity();
                if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                    Log.d(AbsListView.TAG, "edgeReached when fling: touch mode = " + this.this$0.mTouchMode + ",mScrollY = " + this.this$0.mScrollY + ",delta = " + delta + ",vel = " + vel);
                }
                if (delta > 0) {
                    this.this$0.mEdgeGlowTop.onAbsorb(vel);
                } else {
                    this.this$0.mEdgeGlowBottom.onAbsorb(vel);
                }
            } else {
                this.this$0.mTouchMode = -1;
                if (this.this$0.mPositionScroller != null) {
                    this.this$0.mPositionScroller.stop();
                }
            }
            this.this$0.invalidate();
            this.this$0.postOnAnimation(this);
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "XiaoLang.Feng@Plf.SDK : Modify for Scroll Interpolator", property = OppoRomType.ROM)
        void startScroll(int distance, int duration, boolean linear) {
            startScroll(distance, duration, null, linear);
        }

        @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK : Add for  Scroll Interpolator", property = OppoRomType.ROM)
        void startScroll(int distance, int duration, Interpolator interpolator, boolean linear) {
            int initialY;
            Interpolator interpolator2 = null;
            if (distance < 0) {
                initialY = Integer.MAX_VALUE;
            } else {
                initialY = 0;
            }
            this.mLastFlingY = initialY;
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                Log.d(AbsListView.TAG, "startScroll: touch mode = " + this.this$0.mTouchMode + ",mScrollY = " + this.this$0.mScrollY + ",distance = " + distance + ",mLastFlingY = " + this.mLastFlingY);
            }
            if (interpolator != null) {
                this.mSmoothScroll = true;
                this.mScroller.setInterpolator(interpolator);
            } else {
                this.mSmoothScroll = false;
                OverScroller overScroller = this.mScroller;
                if (linear) {
                    interpolator2 = AbsListView.sLinearInterpolator;
                }
                overScroller.setInterpolator(interpolator2);
            }
            this.mScroller.startScroll(0, initialY, 0, distance, duration);
            this.this$0.mTouchMode = 4;
            this.this$0.postOnAnimation(this);
        }

        void endFling() {
            if (AbsListView.IS_ENG_BUILD) {
                if (AbsListView.sDbgMotion) {
                    Log.d(AbsListView.TAG, "endFling+: mScrollY = " + this.this$0.mScrollY + ",mTouchMode = " + this.this$0.mTouchMode + ",mFirstPosition = " + this.this$0.mFirstPosition, new Throwable("endFling"));
                } else {
                    Log.d(AbsListView.TAG, "endFling+: mScrollY = " + this.this$0.mScrollY + ",mTouchMode = " + this.this$0.mTouchMode + ",mFirstPosition = " + this.this$0.mFirstPosition);
                }
            }
            this.this$0.mTouchMode = -1;
            this.mSmoothScroll = false;
            this.this$0.removeCallbacks(this);
            this.this$0.removeCallbacks(this.mCheckFlywheel);
            this.this$0.reportScrollStateChange(0);
            this.this$0.clearScrollingCache();
            this.mScroller.abortAnimation();
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                Log.d(AbsListView.TAG, "endFling-: mScrollY = " + this.this$0.mScrollY + ",mTouchMode = " + this.this$0.mTouchMode + ",mFirstPosition = " + this.this$0.mFirstPosition);
            }
            if (this.this$0.mFlingStrictSpan != null) {
                this.this$0.mFlingStrictSpan.finish();
                this.this$0.mFlingStrictSpan = null;
            }
            if (AbsListView.isEnableEndFlingProtect) {
                long curTime = System.currentTimeMillis();
                if (curTime - AbsListView.lastEndFlingTime < 50) {
                    AbsListView.constantEndFlingNum++;
                    if (AbsListView.constantEndFlingNum >= 100) {
                        long[] oom_adj = new long[1];
                        int pid = Process.myPid();
                        Process.readProcFile("/proc/" + pid + "/oom_adj", AbsListView.LONG_FORMAT, null, oom_adj, null);
                        if (oom_adj[0] > 1) {
                            Log.d(AbsListView.TAG, "pid=" + pid + " killed");
                            Process.sendSignal(pid, 9);
                        } else {
                            Log.d(AbsListView.TAG, "waiting pid=" + pid + " to be background");
                        }
                    }
                } else {
                    AbsListView.constantEndFlingNum = 0;
                }
                AbsListView.lastEndFlingTime = curTime;
            }
        }

        void flywheelTouch() {
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                Log.d(AbsListView.TAG, "flywheelTouch: touch mode = " + this.this$0.mTouchMode + ",mScrollY = " + this.this$0.mScrollY + ",this = " + this.this$0);
            }
            this.this$0.postDelayed(this.mCheckFlywheel, 40);
        }

        @OppoHook(level = OppoHookType.CHANGE_CODE, note = "LiangJing.Fu@Plf.SDK : Modify for use the main thread when scrolling", property = OppoRomType.ROM)
        public void run() {
            if (this.this$0.mIsDetaching || this.this$0.mAdapter == null) {
                if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgDataChange) {
                    Log.d(AbsListView.TAG, "FlingRunnable: touch mode = " + this.this$0.mTouchMode + ",mAdapter = " + this.this$0.mAdapter + ",mFirstPosition = " + this.this$0.mFirstPosition + ",mDataChanged = " + this.this$0.mDataChanged + ",adatper size = " + this.this$0.mItemCount + ",this = " + this.this$0);
                }
                endFling();
                return;
            }
            OverScroller scroller;
            switch (this.this$0.mTouchMode) {
                case 3:
                    if (this.mScroller.isFinished()) {
                        return;
                    }
                    break;
                case 4:
                    break;
                case 6:
                    scroller = this.mScroller;
                    if (!scroller.computeScrollOffset()) {
                        if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                            Log.d(AbsListView.TAG, "FlingRunnable overfling intend to endfling: mScrollY = " + this.this$0.mScrollY);
                        }
                        endFling();
                        break;
                    }
                    int scrollY = this.this$0.mScrollY;
                    int currY = scroller.getCurrY();
                    int deltaY = currY - scrollY;
                    if (!this.this$0.overScrollBy(0, deltaY, 0, scrollY, 0, 0, 0, this.this$0.mOverflingDistance, false)) {
                        if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                            Log.d(AbsListView.TAG, "FlingRunnable overScrollBy return false: mScrollY = " + this.this$0.mScrollY + ", old scrollY = " + scrollY + ", mScroller.getCurrVelocity() = " + this.mScroller.getCurrVelocity());
                        }
                        this.this$0.invalidate();
                        this.this$0.postOnAnimation(this);
                        break;
                    }
                    boolean crossDown = scrollY <= 0 && currY > 0;
                    boolean crossUp = scrollY >= 0 && currY < 0;
                    if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                        Log.d(AbsListView.TAG, "FlingRunnable touch mode overfling: crossDown = " + crossDown + ",crossUp = " + crossUp + ",mScrollY = " + this.this$0.mScrollY + ",scrollY = " + scrollY + ",deltaY = " + deltaY);
                    }
                    if (!crossDown && !crossUp) {
                        startSpringback();
                        break;
                    }
                    int velocity = (int) scroller.getCurrVelocity();
                    if (crossUp) {
                        velocity = -velocity;
                    }
                    scroller.abortAnimation();
                    start(velocity);
                    break;
                    break;
                default:
                    endFling();
                    return;
            }
            if (this.this$0.mDataChanged) {
                this.this$0.layoutChildren();
            }
            if (this.this$0.mItemCount == 0 || this.this$0.getChildCount() == 0) {
                endFling();
                return;
            }
            scroller = this.mScroller;
            boolean more = scroller.computeScrollOffset();
            int y = scroller.getCurrY();
            int delta = this.mLastFlingY - y;
            if (delta > 0) {
                this.this$0.mMotionPosition = this.this$0.mFirstPosition;
                this.this$0.mMotionViewOriginalTop = this.this$0.getChildAt(0).getTop();
                delta = Math.min(((this.this$0.getHeight() - this.this$0.mPaddingBottom) - this.this$0.mPaddingTop) - 1, delta);
            } else {
                int offsetToLast = this.this$0.getChildCount() - 1;
                this.this$0.mMotionPosition = this.this$0.mFirstPosition + offsetToLast;
                this.this$0.mMotionViewOriginalTop = this.this$0.getChildAt(offsetToLast).getTop();
                delta = Math.max(-(((this.this$0.getHeight() - this.this$0.mPaddingBottom) - this.this$0.mPaddingTop) - 1), delta);
            }
            View motionView = this.this$0.getChildAt(this.this$0.mMotionPosition - this.this$0.mFirstPosition);
            int oldTop = 0;
            if (motionView != null) {
                oldTop = motionView.getTop();
            }
            boolean atEdge = this.this$0.trackMotionScroll(delta, delta);
            boolean atEnd = atEdge && delta != 0;
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgMotion) {
                Log.d(AbsListView.TAG, "FlingRunnable mode = " + this.this$0.mTouchMode + " mScrollY = " + this.this$0.mScrollY + ",atEnd = " + atEnd + ",delta = " + delta + ",more = " + more + ",y = " + y + ",mLastFlingY = " + this.mLastFlingY);
            }
            if (atEnd) {
                if (motionView != null) {
                    this.this$0.overScrollBy(0, -(delta - (motionView.getTop() - oldTop)), 0, this.this$0.mScrollY, 0, 0, 0, this.this$0.mOverflingDistance, false);
                }
                if (more) {
                    edgeReached(delta);
                }
            } else if (!more || atEnd) {
                endFling();
            } else {
                if (atEdge) {
                    this.this$0.invalidate();
                }
                this.mLastFlingY = y;
                if (!this.this$0.isOppoStyle()) {
                    this.this$0.postOnAnimation(this);
                } else if (this.this$0.mTouchMode != 4 || this.mSmoothScroll) {
                    this.this$0.postOnAnimation(this);
                }
            }
        }
    }

    private class InputConnectionWrapper implements InputConnection {
        private final EditorInfo mOutAttrs;
        private InputConnection mTarget;
        final /* synthetic */ AbsListView this$0;

        public InputConnectionWrapper(AbsListView this$0, EditorInfo outAttrs) {
            this.this$0 = this$0;
            this.mOutAttrs = outAttrs;
        }

        private InputConnection getTarget() {
            if (this.mTarget == null) {
                this.mTarget = this.this$0.getTextFilterInput().onCreateInputConnection(this.mOutAttrs);
            }
            return this.mTarget;
        }

        public boolean reportFullscreenMode(boolean enabled) {
            return this.this$0.mDefInputConnection.reportFullscreenMode(enabled);
        }

        public boolean performEditorAction(int editorAction) {
            if (editorAction != 6) {
                return false;
            }
            InputMethodManager imm = (InputMethodManager) this.this$0.getContext().getSystemService(InputMethodManager.class);
            if (imm != null) {
                imm.hideSoftInputFromWindow(this.this$0.getWindowToken(), 0);
            }
            return true;
        }

        public boolean sendKeyEvent(KeyEvent event) {
            return this.this$0.mDefInputConnection.sendKeyEvent(event);
        }

        public CharSequence getTextBeforeCursor(int n, int flags) {
            if (this.mTarget == null) {
                return PhoneConstants.MVNO_TYPE_NONE;
            }
            return this.mTarget.getTextBeforeCursor(n, flags);
        }

        public CharSequence getTextAfterCursor(int n, int flags) {
            if (this.mTarget == null) {
                return PhoneConstants.MVNO_TYPE_NONE;
            }
            return this.mTarget.getTextAfterCursor(n, flags);
        }

        public CharSequence getSelectedText(int flags) {
            if (this.mTarget == null) {
                return PhoneConstants.MVNO_TYPE_NONE;
            }
            return this.mTarget.getSelectedText(flags);
        }

        public int getCursorCapsMode(int reqModes) {
            if (this.mTarget == null) {
                return 16384;
            }
            return this.mTarget.getCursorCapsMode(reqModes);
        }

        public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
            return getTarget().getExtractedText(request, flags);
        }

        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            return getTarget().deleteSurroundingText(beforeLength, afterLength);
        }

        public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
            return getTarget().deleteSurroundingTextInCodePoints(beforeLength, afterLength);
        }

        public boolean setComposingText(CharSequence text, int newCursorPosition) {
            return getTarget().setComposingText(text, newCursorPosition);
        }

        public boolean setComposingRegion(int start, int end) {
            return getTarget().setComposingRegion(start, end);
        }

        public boolean finishComposingText() {
            return this.mTarget != null ? this.mTarget.finishComposingText() : true;
        }

        public boolean commitText(CharSequence text, int newCursorPosition) {
            return getTarget().commitText(text, newCursorPosition);
        }

        public boolean commitCompletion(CompletionInfo text) {
            return getTarget().commitCompletion(text);
        }

        public boolean commitCorrection(CorrectionInfo correctionInfo) {
            return getTarget().commitCorrection(correctionInfo);
        }

        public boolean setSelection(int start, int end) {
            return getTarget().setSelection(start, end);
        }

        public boolean performContextMenuAction(int id) {
            return getTarget().performContextMenuAction(id);
        }

        public boolean beginBatchEdit() {
            return getTarget().beginBatchEdit();
        }

        public boolean endBatchEdit() {
            return getTarget().endBatchEdit();
        }

        public boolean clearMetaKeyStates(int states) {
            return getTarget().clearMetaKeyStates(states);
        }

        public boolean performPrivateCommand(String action, Bundle data) {
            return getTarget().performPrivateCommand(action, data);
        }

        public boolean requestCursorUpdates(int cursorUpdateMode) {
            return getTarget().requestCursorUpdates(cursorUpdateMode);
        }

        public Handler getHandler() {
            return getTarget().getHandler();
        }

        public void closeConnection() {
            getTarget().closeConnection();
        }

        public boolean commitContent(InputContentInfo inputContentInfo, int flags, Bundle opts) {
            return getTarget().commitContent(inputContentInfo, flags, opts);
        }
    }

    public static class LayoutParams extends android.view.ViewGroup.LayoutParams {
        @ExportedProperty(category = "list")
        boolean forceAdd;
        boolean isEnabled;
        long itemId;
        @ExportedProperty(category = "list")
        boolean recycledHeaderFooter;
        int scrappedFromPosition;
        @ExportedProperty(category = "list", mapping = {@IntToString(from = -1, to = "ITEM_VIEW_TYPE_IGNORE"), @IntToString(from = -2, to = "ITEM_VIEW_TYPE_HEADER_OR_FOOTER")})
        int viewType;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.itemId = -1;
        }

        public LayoutParams(int w, int h) {
            super(w, h);
            this.itemId = -1;
        }

        public LayoutParams(int w, int h, int viewType) {
            super(w, h);
            this.itemId = -1;
            this.viewType = viewType;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
            this.itemId = -1;
        }

        protected void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("list:viewType", this.viewType);
            encoder.addProperty("list:recycledHeaderFooter", this.recycledHeaderFooter);
            encoder.addProperty("list:forceAdd", this.forceAdd);
            encoder.addProperty("list:isEnabled", this.isEnabled);
        }
    }

    class ListItemAccessibilityDelegate extends AccessibilityDelegate {
        final /* synthetic */ AbsListView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.ListItemAccessibilityDelegate.<init>(android.widget.AbsListView):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        ListItemAccessibilityDelegate(android.widget.AbsListView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.ListItemAccessibilityDelegate.<init>(android.widget.AbsListView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.ListItemAccessibilityDelegate.<init>(android.widget.AbsListView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.ListItemAccessibilityDelegate.onInitializeAccessibilityNodeInfo(android.view.View, android.view.accessibility.AccessibilityNodeInfo):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void onInitializeAccessibilityNodeInfo(android.view.View r1, android.view.accessibility.AccessibilityNodeInfo r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.ListItemAccessibilityDelegate.onInitializeAccessibilityNodeInfo(android.view.View, android.view.accessibility.AccessibilityNodeInfo):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.ListItemAccessibilityDelegate.onInitializeAccessibilityNodeInfo(android.view.View, android.view.accessibility.AccessibilityNodeInfo):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.ListItemAccessibilityDelegate.performAccessibilityAction(android.view.View, int, android.os.Bundle):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean performAccessibilityAction(android.view.View r1, int r2, android.os.Bundle r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.ListItemAccessibilityDelegate.performAccessibilityAction(android.view.View, int, android.os.Bundle):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.ListItemAccessibilityDelegate.performAccessibilityAction(android.view.View, int, android.os.Bundle):boolean");
        }
    }

    public interface MultiChoiceModeListener extends Callback {
        void onItemCheckedStateChanged(ActionMode actionMode, int i, long j, boolean z);
    }

    class MultiChoiceModeWrapper implements MultiChoiceModeListener {
        private MultiChoiceModeListener mWrapped;
        final /* synthetic */ AbsListView this$0;

        MultiChoiceModeWrapper(AbsListView this$0) {
            this.this$0 = this$0;
        }

        public void setWrapped(MultiChoiceModeListener wrapped) {
            this.mWrapped = wrapped;
        }

        public boolean hasWrappedCallback() {
            return this.mWrapped != null;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (!this.mWrapped.onCreateActionMode(mode, menu)) {
                return false;
            }
            this.this$0.setLongClickable(false);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return this.mWrapped.onPrepareActionMode(mode, menu);
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return this.mWrapped.onActionItemClicked(mode, item);
        }

        public void onDestroyActionMode(ActionMode mode) {
            this.mWrapped.onDestroyActionMode(mode);
            this.this$0.mChoiceActionMode = null;
            this.this$0.clearChoices();
            this.this$0.setDataChanged(true);
            this.this$0.rememberSyncState();
            this.this$0.requestLayout();
            this.this$0.setLongClickable(true);
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            this.mWrapped.onItemCheckedStateChanged(mode, position, id, checked);
            if (this.this$0.getCheckedItemCount() == 0) {
                mode.finish();
            }
        }
    }

    public interface OnScrollListener {
        public static final int SCROLL_STATE_FLING = 2;
        public static final int SCROLL_STATE_IDLE = 0;
        public static final int SCROLL_STATE_TOUCH_SCROLL = 1;

        void onScroll(AbsListView absListView, int i, int i2, int i3);

        void onScrollStateChanged(AbsListView absListView, int i);
    }

    private class PerformClick extends WindowRunnnable implements Runnable {
        int mClickMotionPosition;
        final /* synthetic */ AbsListView this$0;

        /* synthetic */ PerformClick(AbsListView this$0, PerformClick performClick) {
            this(this$0);
        }

        private PerformClick(AbsListView this$0) {
            this.this$0 = this$0;
            super(this$0, null);
        }

        public void run() {
            ListAdapter adapter = this.this$0.mAdapter;
            int motionPosition = this.mClickMotionPosition;
            if (!this.this$0.mDataChanged && !this.this$0.mIsDetaching && this.this$0.isAttachedToWindow() && adapter != null && this.this$0.mItemCount > 0 && motionPosition != -1 && motionPosition < adapter.getCount() && sameWindow() && adapter.isEnabled(motionPosition)) {
                View view = this.this$0.getChildAt(motionPosition - this.this$0.mFirstPosition);
                if (view != null) {
                    this.this$0.performItemClick(view, motionPosition, adapter.getItemId(motionPosition));
                    return;
                }
            }
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbg) {
                Log.d(AbsListView.TAG, "PerformClick error mDataChanged = " + this.this$0.mDataChanged + ", isAttachedToWindow = " + this.this$0.isAttachedToWindow() + ", adapter = " + adapter + ", mItemCount = " + this.this$0.mItemCount + ", motionPosition = " + motionPosition + ", sameWindow = " + sameWindow());
            }
        }
    }

    class PositionScroller extends AbsPositionScroller implements Runnable {
        private static final int MOVE_DOWN_BOUND = 3;
        private static final int MOVE_DOWN_POS = 1;
        private static final int MOVE_OFFSET = 5;
        private static final int MOVE_UP_BOUND = 4;
        private static final int MOVE_UP_POS = 2;
        private static final int SCROLL_DURATION = 200;
        private int mBoundPos;
        private final int mExtraScroll;
        private int mLastSeenPos;
        private int mMode;
        private int mOffsetFromTop;
        private int mScrollDuration;
        private int mTargetPos;
        final /* synthetic */ AbsListView this$0;

        /* renamed from: android.widget.AbsListView$PositionScroller$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ PositionScroller this$1;
            final /* synthetic */ int val$position;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.PositionScroller.1.<init>(android.widget.AbsListView$PositionScroller, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            AnonymousClass1(android.widget.AbsListView.PositionScroller r1, int r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.PositionScroller.1.<init>(android.widget.AbsListView$PositionScroller, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.PositionScroller.1.<init>(android.widget.AbsListView$PositionScroller, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.PositionScroller.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.PositionScroller.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.PositionScroller.1.run():void");
            }
        }

        /* renamed from: android.widget.AbsListView$PositionScroller$2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ PositionScroller this$1;
            final /* synthetic */ int val$boundPosition;
            final /* synthetic */ int val$position;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.PositionScroller.2.<init>(android.widget.AbsListView$PositionScroller, int, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            AnonymousClass2(android.widget.AbsListView.PositionScroller r1, int r2, int r3) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.PositionScroller.2.<init>(android.widget.AbsListView$PositionScroller, int, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.PositionScroller.2.<init>(android.widget.AbsListView$PositionScroller, int, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.PositionScroller.2.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.PositionScroller.2.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.PositionScroller.2.run():void");
            }
        }

        /* renamed from: android.widget.AbsListView$PositionScroller$3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ PositionScroller this$1;
            final /* synthetic */ int val$duration;
            final /* synthetic */ int val$position;
            final /* synthetic */ int val$postOffset;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.PositionScroller.3.<init>(android.widget.AbsListView$PositionScroller, int, int, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            AnonymousClass3(android.widget.AbsListView.PositionScroller r1, int r2, int r3, int r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.AbsListView.PositionScroller.3.<init>(android.widget.AbsListView$PositionScroller, int, int, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.PositionScroller.3.<init>(android.widget.AbsListView$PositionScroller, int, int, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.PositionScroller.3.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.AbsListView.PositionScroller.3.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.PositionScroller.3.run():void");
            }
        }

        PositionScroller(AbsListView this$0) {
            this.this$0 = this$0;
            this.mExtraScroll = ViewConfiguration.get(this$0.mContext).getScaledFadingEdgeLength();
        }

        /*  JADX ERROR: NullPointerException in pass: ModVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
            	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
            	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
            	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
            	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
            	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public void start(int r10) {
            /*
            r9 = this;
            r8 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
            r7 = 0;
            r6 = -1;
            r9.stop();
            r5 = r9.this$0;
            r5 = r5.mDataChanged;
            if (r5 == 0) goto L_0x0017;
        L_0x000d:
            r5 = r9.this$0;
            r6 = new android.widget.AbsListView$PositionScroller$1;
            r6.<init>(r9, r10);
            r5.mPositionScrollAfterLayout = r6;
            return;
        L_0x0017:
            r5 = r9.this$0;
            r0 = r5.getChildCount();
            if (r0 != 0) goto L_0x0020;
        L_0x001f:
            return;
        L_0x0020:
            r5 = r9.this$0;
            r2 = r5.mFirstPosition;
            r5 = r2 + r0;
            r3 = r5 + -1;
            r5 = r9.this$0;
            r5 = r5.getCount();
            r5 = r5 + -1;
            r5 = java.lang.Math.min(r5, r10);
            r1 = java.lang.Math.max(r7, r5);
            if (r1 >= r2) goto L_0x0053;
        L_0x003a:
            r5 = r2 - r1;
            r4 = r5 + 1;
            r5 = 2;
            r9.mMode = r5;
        L_0x0041:
            if (r4 <= 0) goto L_0x0061;
        L_0x0043:
            r5 = r8 / r4;
            r9.mScrollDuration = r5;
        L_0x0047:
            r9.mTargetPos = r1;
            r9.mBoundPos = r6;
            r9.mLastSeenPos = r6;
            r5 = r9.this$0;
            r5.postOnAnimation(r9);
            return;
        L_0x0053:
            if (r1 <= r3) goto L_0x005d;
        L_0x0055:
            r5 = r1 - r3;
            r4 = r5 + 1;
            r5 = 1;
            r9.mMode = r5;
            goto L_0x0041;
        L_0x005d:
            r9.scrollToVisible(r1, r6, r8);
            return;
        L_0x0061:
            r9.mScrollDuration = r8;
            goto L_0x0047;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.PositionScroller.start(int):void");
        }

        /*  JADX ERROR: NullPointerException in pass: ModVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
            	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
            	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
            	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
            	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
            	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public void start(int r12, int r13) {
            /*
            r11 = this;
            r11.stop();
            r9 = -1;
            if (r13 != r9) goto L_0x000a;
        L_0x0006:
            r11.start(r12);
            return;
        L_0x000a:
            r9 = r11.this$0;
            r9 = r9.mDataChanged;
            if (r9 == 0) goto L_0x001a;
        L_0x0010:
            r9 = r11.this$0;
            r10 = new android.widget.AbsListView$PositionScroller$2;
            r10.<init>(r11, r12, r13);
            r9.mPositionScrollAfterLayout = r10;
            return;
        L_0x001a:
            r9 = r11.this$0;
            r3 = r9.getChildCount();
            if (r3 != 0) goto L_0x0023;
        L_0x0022:
            return;
        L_0x0023:
            r9 = r11.this$0;
            r5 = r9.mFirstPosition;
            r9 = r5 + r3;
            r6 = r9 + -1;
            r9 = r11.this$0;
            r9 = r9.getCount();
            r9 = r9 + -1;
            r9 = java.lang.Math.min(r9, r12);
            r10 = 0;
            r4 = java.lang.Math.max(r10, r9);
            if (r4 >= r5) goto L_0x0069;
        L_0x003e:
            r1 = r6 - r13;
            r9 = 1;
            if (r1 >= r9) goto L_0x0044;
        L_0x0043:
            return;
        L_0x0044:
            r9 = r5 - r4;
            r7 = r9 + 1;
            r2 = r1 + -1;
            if (r2 >= r7) goto L_0x0064;
        L_0x004c:
            r8 = r2;
            r9 = 4;
            r11.mMode = r9;
        L_0x0050:
            if (r8 <= 0) goto L_0x0089;
        L_0x0052:
            r9 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
            r9 = r9 / r8;
            r11.mScrollDuration = r9;
        L_0x0057:
            r11.mTargetPos = r4;
            r11.mBoundPos = r13;
            r9 = -1;
            r11.mLastSeenPos = r9;
            r9 = r11.this$0;
            r9.postOnAnimation(r11);
            return;
        L_0x0064:
            r8 = r7;
            r9 = 2;
            r11.mMode = r9;
            goto L_0x0050;
        L_0x0069:
            if (r4 <= r6) goto L_0x0083;
        L_0x006b:
            r0 = r13 - r5;
            r9 = 1;
            if (r0 >= r9) goto L_0x0071;
        L_0x0070:
            return;
        L_0x0071:
            r9 = r4 - r6;
            r7 = r9 + 1;
            r2 = r0 + -1;
            if (r2 >= r7) goto L_0x007e;
        L_0x0079:
            r8 = r2;
            r9 = 3;
            r11.mMode = r9;
            goto L_0x0050;
        L_0x007e:
            r8 = r7;
            r9 = 1;
            r11.mMode = r9;
            goto L_0x0050;
        L_0x0083:
            r9 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
            r11.scrollToVisible(r4, r13, r9);
            return;
        L_0x0089:
            r9 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
            r11.mScrollDuration = r9;
            goto L_0x0057;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.PositionScroller.start(int, int):void");
        }

        public void startWithOffset(int position, int offset) {
            startWithOffset(position, offset, 200);
        }

        /*  JADX ERROR: NullPointerException in pass: ModVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
            	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
            	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
            	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
            	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
            	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public void startWithOffset(int r11, int r12, int r13) {
            /*
            r10 = this;
            r8 = 0;
            r9 = -1;
            r10.stop();
            r7 = r10.this$0;
            r7 = r7.mDataChanged;
            if (r7 == 0) goto L_0x0016;
        L_0x000b:
            r3 = r12;
            r7 = r10.this$0;
            r8 = new android.widget.AbsListView$PositionScroller$3;
            r8.<init>(r10, r11, r12, r13);
            r7.mPositionScrollAfterLayout = r8;
            return;
        L_0x0016:
            r7 = r10.this$0;
            r0 = r7.getChildCount();
            if (r0 != 0) goto L_0x001f;
        L_0x001e:
            return;
        L_0x001f:
            r7 = r10.this$0;
            r7 = r7.getPaddingTop();
            r12 = r12 + r7;
            r7 = r10.this$0;
            r7 = r7.getCount();
            r7 = r7 + -1;
            r7 = java.lang.Math.min(r7, r11);
            r7 = java.lang.Math.max(r8, r7);
            r10.mTargetPos = r7;
            r10.mOffsetFromTop = r12;
            r10.mBoundPos = r9;
            r10.mLastSeenPos = r9;
            r7 = 5;
            r10.mMode = r7;
            r7 = r10.this$0;
            r1 = r7.mFirstPosition;
            r7 = r1 + r0;
            r2 = r7 + -1;
            r7 = r10.mTargetPos;
            if (r7 >= r1) goto L_0x0065;
        L_0x004d:
            r7 = r10.mTargetPos;
            r6 = r1 - r7;
        L_0x0051:
            r7 = (float) r6;
            r8 = (float) r0;
            r4 = r7 / r8;
            r7 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
            r7 = (r4 > r7 ? 1 : (r4 == r7 ? 0 : -1));
            if (r7 >= 0) goto L_0x0084;
        L_0x005b:
            r10.mScrollDuration = r13;
            r10.mLastSeenPos = r9;
            r7 = r10.this$0;
            r7.postOnAnimation(r10);
            return;
        L_0x0065:
            r7 = r10.mTargetPos;
            if (r7 <= r2) goto L_0x006e;
        L_0x0069:
            r7 = r10.mTargetPos;
            r6 = r7 - r2;
            goto L_0x0051;
        L_0x006e:
            r7 = r10.this$0;
            r8 = r10.mTargetPos;
            r8 = r8 - r1;
            r7 = r7.getChildAt(r8);
            r5 = r7.getTop();
            r7 = r10.this$0;
            r8 = r5 - r12;
            r9 = 1;
            r7.smoothScrollBy(r8, r13, r9);
            return;
        L_0x0084:
            r7 = (float) r13;
            r7 = r7 / r4;
            r13 = (int) r7;
            goto L_0x005b;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.PositionScroller.startWithOffset(int, int, int):void");
        }

        private void scrollToVisible(int targetPos, int boundPos, int duration) {
            int firstPos = this.this$0.mFirstPosition;
            int lastPos = (firstPos + this.this$0.getChildCount()) - 1;
            int paddedTop = this.this$0.mListPadding.top;
            int paddedBottom = this.this$0.getHeight() - this.this$0.mListPadding.bottom;
            if (targetPos < firstPos || targetPos > lastPos) {
                Log.w(AbsListView.TAG, "scrollToVisible called with targetPos " + targetPos + " not visible [" + firstPos + ", " + lastPos + "]");
            }
            if (boundPos < firstPos || boundPos > lastPos) {
                boundPos = -1;
            }
            View targetChild = this.this$0.getChildAt(targetPos - firstPos);
            int targetTop = targetChild.getTop();
            int targetBottom = targetChild.getBottom();
            int scrollBy = 0;
            if (targetBottom > paddedBottom) {
                scrollBy = targetBottom - paddedBottom;
            }
            if (targetTop < paddedTop) {
                scrollBy = targetTop - paddedTop;
            }
            if (scrollBy != 0) {
                if (boundPos >= 0) {
                    View boundChild = this.this$0.getChildAt(boundPos - firstPos);
                    int boundTop = boundChild.getTop();
                    int boundBottom = boundChild.getBottom();
                    int absScroll = Math.abs(scrollBy);
                    if (scrollBy < 0 && boundBottom + absScroll > paddedBottom) {
                        scrollBy = Math.max(0, boundBottom - paddedBottom);
                    } else if (scrollBy > 0 && boundTop - absScroll < paddedTop) {
                        scrollBy = Math.min(0, boundTop - paddedTop);
                    }
                }
                this.this$0.smoothScrollBy(scrollBy, duration);
            }
        }

        public void stop() {
            this.this$0.removeCallbacks(this);
        }

        public void run() {
            int listHeight = this.this$0.getHeight();
            int firstPos = this.this$0.mFirstPosition;
            int lastViewIndex;
            int lastPos;
            View lastView;
            int lastViewHeight;
            int lastViewPixelsShowing;
            int extraScroll;
            int childCount;
            switch (this.mMode) {
                case 1:
                    lastViewIndex = this.this$0.getChildCount() - 1;
                    lastPos = firstPos + lastViewIndex;
                    if (lastViewIndex >= 0) {
                        if (lastPos != this.mLastSeenPos) {
                            lastView = this.this$0.getChildAt(lastViewIndex);
                            lastViewHeight = lastView.getHeight();
                            lastViewPixelsShowing = listHeight - lastView.getTop();
                            if (lastPos < this.this$0.mItemCount - 1) {
                                extraScroll = Math.max(this.this$0.mListPadding.bottom, this.mExtraScroll);
                            } else {
                                extraScroll = this.this$0.mListPadding.bottom;
                            }
                            this.this$0.smoothScrollBy((lastViewHeight - lastViewPixelsShowing) + extraScroll, this.mScrollDuration, true);
                            this.mLastSeenPos = lastPos;
                            if (lastPos < this.mTargetPos) {
                                this.this$0.postOnAnimation(this);
                                break;
                            }
                        }
                        this.this$0.postOnAnimation(this);
                        return;
                    }
                    return;
                    break;
                case 2:
                    if (firstPos != this.mLastSeenPos) {
                        View firstView = this.this$0.getChildAt(0);
                        if (firstView != null) {
                            this.this$0.smoothScrollBy(firstView.getTop() - (firstPos > 0 ? Math.max(this.mExtraScroll, this.this$0.mListPadding.top) : this.this$0.mListPadding.top), this.mScrollDuration, true);
                            this.mLastSeenPos = firstPos;
                            if (firstPos > this.mTargetPos) {
                                this.this$0.postOnAnimation(this);
                                break;
                            }
                        }
                        return;
                    }
                    this.this$0.postOnAnimation(this);
                    return;
                    break;
                case 3:
                    childCount = this.this$0.getChildCount();
                    if (firstPos != this.mBoundPos && childCount > 1 && firstPos + childCount < this.this$0.mItemCount) {
                        int nextPos = firstPos + 1;
                        if (nextPos != this.mLastSeenPos) {
                            View nextView = this.this$0.getChildAt(1);
                            int nextViewHeight = nextView.getHeight();
                            int nextViewTop = nextView.getTop();
                            extraScroll = Math.max(this.this$0.mListPadding.bottom, this.mExtraScroll);
                            if (nextPos >= this.mBoundPos) {
                                if (nextViewTop > extraScroll) {
                                    this.this$0.smoothScrollBy(nextViewTop - extraScroll, this.mScrollDuration, true);
                                    break;
                                }
                            }
                            this.this$0.smoothScrollBy(Math.max(0, (nextViewHeight + nextViewTop) - extraScroll), this.mScrollDuration, true);
                            this.mLastSeenPos = nextPos;
                            this.this$0.postOnAnimation(this);
                            break;
                        }
                        this.this$0.postOnAnimation(this);
                        return;
                    }
                    return;
                    break;
                case 4:
                    lastViewIndex = this.this$0.getChildCount() - 2;
                    if (lastViewIndex >= 0) {
                        lastPos = firstPos + lastViewIndex;
                        if (lastPos != this.mLastSeenPos) {
                            lastView = this.this$0.getChildAt(lastViewIndex);
                            lastViewHeight = lastView.getHeight();
                            int lastViewTop = lastView.getTop();
                            lastViewPixelsShowing = listHeight - lastViewTop;
                            extraScroll = Math.max(this.this$0.mListPadding.top, this.mExtraScroll);
                            this.mLastSeenPos = lastPos;
                            if (lastPos <= this.mBoundPos) {
                                int bottom = listHeight - extraScroll;
                                int lastViewBottom = lastViewTop + lastViewHeight;
                                if (bottom > lastViewBottom) {
                                    this.this$0.smoothScrollBy(-(bottom - lastViewBottom), this.mScrollDuration, true);
                                    break;
                                }
                            }
                            this.this$0.smoothScrollBy(-(lastViewPixelsShowing - extraScroll), this.mScrollDuration, true);
                            this.this$0.postOnAnimation(this);
                            break;
                        }
                        this.this$0.postOnAnimation(this);
                        return;
                    }
                    return;
                    break;
                case 5:
                    if (this.mLastSeenPos != firstPos) {
                        this.mLastSeenPos = firstPos;
                        childCount = this.this$0.getChildCount();
                        int position = this.mTargetPos;
                        lastPos = (firstPos + childCount) - 1;
                        int viewTravelCount = 0;
                        if (position < firstPos) {
                            viewTravelCount = (firstPos - position) + 1;
                        } else if (position > lastPos) {
                            viewTravelCount = position - lastPos;
                        }
                        float modifier = Math.min(Math.abs(((float) viewTravelCount) / ((float) childCount)), 1.0f);
                        if (position >= firstPos) {
                            if (position <= lastPos) {
                                int distance = this.this$0.getChildAt(position - firstPos).getTop() - this.mOffsetFromTop;
                                this.this$0.smoothScrollBy(distance, (int) (((float) this.mScrollDuration) * (((float) Math.abs(distance)) / ((float) this.this$0.getHeight()))), true);
                                break;
                            }
                            this.this$0.smoothScrollBy((int) (((float) this.this$0.getHeight()) * modifier), (int) (((float) this.mScrollDuration) * modifier), true);
                            this.this$0.postOnAnimation(this);
                            break;
                        }
                        this.this$0.smoothScrollBy((int) (((float) (-this.this$0.getHeight())) * modifier), (int) (((float) this.mScrollDuration) * modifier), true);
                        this.this$0.postOnAnimation(this);
                        break;
                    }
                    this.this$0.postOnAnimation(this);
                    return;
            }
        }
    }

    class RecycleBin {
        private View[] mActiveViews;
        private ArrayList<View> mCurrentScrap;
        private int mFirstActivePosition;
        private RecyclerListener mRecyclerListener;
        private ArrayList<View>[] mScrapViews;
        private ArrayList<View> mSkippedScrap;
        private SparseArray<View> mTransientStateViews;
        private LongSparseArray<View> mTransientStateViewsById;
        private int mViewTypeCount;
        final /* synthetic */ AbsListView this$0;

        RecycleBin(AbsListView this$0) {
            this.this$0 = this$0;
            this.mActiveViews = new View[0];
        }

        public void setViewTypeCount(int viewTypeCount) {
            if (viewTypeCount < 1) {
                throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
            }
            ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];
            for (int i = 0; i < viewTypeCount; i++) {
                scrapViews[i] = new ArrayList();
            }
            this.mViewTypeCount = viewTypeCount;
            this.mCurrentScrap = scrapViews[0];
            this.mScrapViews = scrapViews;
        }

        public void markChildrenDirty() {
            int i;
            int count;
            ArrayList<View> scrap;
            int scrapCount;
            if (this.mViewTypeCount == 1) {
                scrap = this.mCurrentScrap;
                scrapCount = scrap.size();
                for (i = 0; i < scrapCount; i++) {
                    ((View) scrap.get(i)).forceLayout();
                }
            } else {
                int typeCount = this.mViewTypeCount;
                for (i = 0; i < typeCount; i++) {
                    scrap = this.mScrapViews[i];
                    scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        ((View) scrap.get(j)).forceLayout();
                    }
                }
            }
            if (this.mTransientStateViews != null) {
                count = this.mTransientStateViews.size();
                for (i = 0; i < count; i++) {
                    ((View) this.mTransientStateViews.valueAt(i)).forceLayout();
                }
            }
            if (this.mTransientStateViewsById != null) {
                count = this.mTransientStateViewsById.size();
                for (i = 0; i < count; i++) {
                    ((View) this.mTransientStateViewsById.valueAt(i)).forceLayout();
                }
            }
        }

        public boolean shouldRecycleViewType(int viewType) {
            return viewType >= 0;
        }

        void clear() {
            if (this.mViewTypeCount == 1) {
                clearScrap(this.mCurrentScrap);
            } else {
                int typeCount = this.mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    ArrayList<View> scrap = this.mScrapViews[i];
                    clearScrap(scrap);
                    if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgRecycle) {
                        Log.d(AbsListView.TAG, "Recycle clear end: i = " + i + ", scrap size = " + scrap.size() + ", active size = " + this.mActiveViews.length + ", this = " + this);
                    }
                }
            }
            clearTransientStateViews();
        }

        void fillActiveViews(int childCount, int firstActivePosition) {
            if (this.mActiveViews.length < childCount) {
                this.mActiveViews = new View[childCount];
            }
            this.mFirstActivePosition = firstActivePosition;
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgRecycle) {
                Log.d(AbsListView.TAG, "fillActiveViews: childCount = " + childCount + ",firstActivePosition = " + firstActivePosition + ",this = " + this.this$0);
            }
            View[] activeViews = this.mActiveViews;
            for (int i = 0; i < childCount; i++) {
                View child = this.this$0.getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (!(lp == null || lp.viewType == -2)) {
                    activeViews[i] = child;
                    lp.scrappedFromPosition = firstActivePosition + i;
                }
            }
        }

        View getActiveView(int position) {
            int index = position - this.mFirstActivePosition;
            View[] activeViews = this.mActiveViews;
            if (index < 0 || index >= activeViews.length) {
                return null;
            }
            View match = activeViews[index];
            activeViews[index] = null;
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgRecycle) {
                Log.d(AbsListView.TAG, "getActiveView success: position = " + position + ",index = " + index + ",match = " + match + ",this = " + this.this$0);
            }
            return match;
        }

        View getTransientStateView(int position) {
            View result;
            if (this.this$0.mAdapter == null || !this.this$0.mAdapterHasStableIds || this.mTransientStateViewsById == null) {
                if (this.mTransientStateViews != null) {
                    int index = this.mTransientStateViews.indexOfKey(position);
                    if (index >= 0) {
                        result = (View) this.mTransientStateViews.valueAt(index);
                        this.mTransientStateViews.removeAt(index);
                        return result;
                    }
                }
                return null;
            }
            long id = this.this$0.mAdapter.getItemId(position);
            result = (View) this.mTransientStateViewsById.get(id);
            this.mTransientStateViewsById.remove(id);
            return result;
        }

        void clearTransientStateViews() {
            int N;
            int i;
            SparseArray<View> viewsByPos = this.mTransientStateViews;
            if (viewsByPos != null) {
                N = viewsByPos.size();
                for (i = 0; i < N; i++) {
                    removeDetachedView((View) viewsByPos.valueAt(i), false);
                }
                viewsByPos.clear();
            }
            LongSparseArray<View> viewsById = this.mTransientStateViewsById;
            if (viewsById != null) {
                N = viewsById.size();
                for (i = 0; i < N; i++) {
                    removeDetachedView((View) viewsById.valueAt(i), false);
                }
                viewsById.clear();
            }
        }

        View getScrapView(int position) {
            int whichScrap = this.this$0.mAdapter.getItemViewType(position);
            if (whichScrap < 0) {
                return null;
            }
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgRecycle) {
                Log.d(AbsListView.TAG, "getScrapView: position = " + position + ",mViewTypeCount = " + this.mViewTypeCount + ",mCurrentScrap = " + this.mCurrentScrap + ",this = " + this);
            }
            if (this.mViewTypeCount == 1) {
                return retrieveFromScrap(this.mCurrentScrap, position);
            }
            if (whichScrap < this.mScrapViews.length) {
                return retrieveFromScrap(this.mScrapViews[whichScrap], position);
            }
            return null;
        }

        void addScrapView(View scrap, int position) {
            LayoutParams lp = (LayoutParams) scrap.getLayoutParams();
            if (lp != null) {
                lp.scrappedFromPosition = position;
                int viewType = lp.viewType;
                if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgRecycle) {
                    Log.d(AbsListView.TAG, "addScrapView: scrap = " + scrap + ",position = " + position + ",scrap width = " + scrap.getMeasuredWidth() + ",height = " + scrap.getMeasuredHeight() + ",mViewTypeCount = " + this.mViewTypeCount + ",viewType = " + viewType + ",this = " + this);
                }
                if (shouldRecycleViewType(viewType)) {
                    if (scrap.isAttachedToWindow()) {
                        scrap.dispatchStartTemporaryDetach();
                        this.this$0.notifyViewAccessibilityStateChangedIfNeeded(1);
                    }
                    if (!scrap.hasTransientState()) {
                        int scrapsCount;
                        if (this.mViewTypeCount == 1) {
                            this.mCurrentScrap.add(scrap);
                            scrapsCount = this.mCurrentScrap.size();
                        } else {
                            this.mScrapViews[viewType].add(scrap);
                            scrapsCount = this.mScrapViews[viewType].size();
                        }
                        if (AbsListView.IS_ENG_BUILD && scrapsCount > 0 && scrapsCount % 50 == 0) {
                            Log.d(AbsListView.TAG, "[OOM warning] total = " + scrapsCount + ", scrap = " + scrap + ", position = " + position + ", scrap width = " + scrap.getMeasuredWidth() + ", scrap height = " + scrap.getMeasuredHeight() + ", mViewTypeCount = " + this.mViewTypeCount + ", viewType = " + viewType + ", this = " + this);
                        }
                        if (this.mRecyclerListener != null) {
                            this.mRecyclerListener.onMovedToScrapHeap(scrap);
                        }
                    } else if (this.this$0.mAdapter != null && this.this$0.mAdapterHasStableIds) {
                        if (this.mTransientStateViewsById == null) {
                            this.mTransientStateViewsById = new LongSparseArray();
                        }
                        this.mTransientStateViewsById.put(lp.itemId, scrap);
                    } else if (this.this$0.mDataChanged) {
                        getSkippedScrap().add(scrap);
                    } else {
                        if (this.mTransientStateViews == null) {
                            this.mTransientStateViews = new SparseArray();
                        }
                        this.mTransientStateViews.put(position, scrap);
                    }
                    return;
                }
                if (viewType != -2) {
                    getSkippedScrap().add(scrap);
                }
            }
        }

        private ArrayList<View> getSkippedScrap() {
            if (this.mSkippedScrap == null) {
                this.mSkippedScrap = new ArrayList();
            }
            return this.mSkippedScrap;
        }

        void removeSkippedScrap() {
            if (this.mSkippedScrap != null) {
                int count = this.mSkippedScrap.size();
                for (int i = 0; i < count; i++) {
                    removeDetachedView((View) this.mSkippedScrap.get(i), false);
                }
                this.mSkippedScrap.clear();
            }
        }

        void scrapActiveViews() {
            View[] activeViews = this.mActiveViews;
            boolean hasListener = this.mRecyclerListener != null;
            boolean multipleScraps = this.mViewTypeCount > 1;
            ArrayList<View> scrapViews = this.mCurrentScrap;
            int count = activeViews.length;
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgRecycle) {
                Log.d(AbsListView.TAG, "scrapActiveViews: mActiveViews = " + this.mActiveViews + ",hasListener = " + hasListener + ",mViewTypeCount = " + this.mViewTypeCount + ",mCurrentScrap = " + this.mCurrentScrap + ",count = " + count + ",this = " + this);
            }
            for (int i = count - 1; i >= 0; i--) {
                View victim = activeViews[i];
                if (victim != null) {
                    LayoutParams lp = (LayoutParams) victim.getLayoutParams();
                    int whichScrap = lp.viewType;
                    activeViews[i] = null;
                    if (victim.hasTransientState()) {
                        victim.dispatchStartTemporaryDetach();
                        if (this.this$0.mAdapter != null && this.this$0.mAdapterHasStableIds) {
                            if (this.mTransientStateViewsById == null) {
                                this.mTransientStateViewsById = new LongSparseArray();
                            }
                            this.mTransientStateViewsById.put(this.this$0.mAdapter.getItemId(this.mFirstActivePosition + i), victim);
                        } else if (!this.this$0.mDataChanged) {
                            if (this.mTransientStateViews == null) {
                                this.mTransientStateViews = new SparseArray();
                            }
                            this.mTransientStateViews.put(this.mFirstActivePosition + i, victim);
                        } else if (whichScrap != -2) {
                            removeDetachedView(victim, false);
                        }
                    } else if (shouldRecycleViewType(whichScrap)) {
                        if (multipleScraps) {
                            scrapViews = this.mScrapViews[whichScrap];
                        }
                        lp.scrappedFromPosition = this.mFirstActivePosition + i;
                        removeDetachedView(victim, false);
                        scrapViews.add(victim);
                        if (hasListener) {
                            this.mRecyclerListener.onMovedToScrapHeap(victim);
                        }
                    } else if (whichScrap != -2) {
                        removeDetachedView(victim, false);
                    }
                }
            }
            pruneScrapViews();
        }

        void fullyDetachScrapViews() {
            int viewTypeCount = this.mViewTypeCount;
            ArrayList<View>[] scrapViews = this.mScrapViews;
            for (int i = 0; i < viewTypeCount; i++) {
                ArrayList<View> scrapPile = scrapViews[i];
                for (int j = scrapPile.size() - 1; j >= 0; j--) {
                    View view = (View) scrapPile.get(j);
                    if (view.isTemporarilyDetached()) {
                        removeDetachedView(view, false);
                    }
                }
            }
        }

        private void pruneScrapViews() {
            int i;
            View v;
            int maxViews = this.mActiveViews.length;
            int viewTypeCount = this.mViewTypeCount;
            ArrayList<View>[] scrapViews = this.mScrapViews;
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgRecycle) {
                Log.d(AbsListView.TAG, "pruneScrapViews: maxViews = " + maxViews + ",viewTypeCount = " + viewTypeCount + ",mScrapViews = " + this.mScrapViews + ",this = " + this);
            }
            for (i = 0; i < viewTypeCount; i++) {
                ArrayList<View> scrapPile = scrapViews[i];
                int size = scrapPile.size();
                while (size > maxViews) {
                    size--;
                    scrapPile.remove(size);
                }
            }
            SparseArray<View> transViewsByPos = this.mTransientStateViews;
            if (transViewsByPos != null) {
                i = 0;
                while (i < transViewsByPos.size()) {
                    v = (View) transViewsByPos.valueAt(i);
                    if (!v.hasTransientState()) {
                        removeDetachedView(v, false);
                        transViewsByPos.removeAt(i);
                        i--;
                    }
                    i++;
                }
            }
            LongSparseArray<View> transViewsById = this.mTransientStateViewsById;
            if (transViewsById != null) {
                i = 0;
                while (i < transViewsById.size()) {
                    v = (View) transViewsById.valueAt(i);
                    if (!v.hasTransientState()) {
                        removeDetachedView(v, false);
                        transViewsById.removeAt(i);
                        i--;
                    }
                    i++;
                }
            }
        }

        void reclaimScrapViews(List<View> views) {
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgRecycle) {
                Log.d(AbsListView.TAG, "reclaimScrapViews: views = " + views + ",mViewTypeCount = " + this.mViewTypeCount + ",mCurrentScrap = " + this.mCurrentScrap + ",this = " + this);
            }
            if (this.mViewTypeCount == 1) {
                views.addAll(this.mCurrentScrap);
                return;
            }
            int viewTypeCount = this.mViewTypeCount;
            ArrayList<View>[] scrapViews = this.mScrapViews;
            for (int i = 0; i < viewTypeCount; i++) {
                views.addAll(scrapViews[i]);
            }
        }

        void setCacheColorHint(int color) {
            int i;
            ArrayList<View> scrap;
            int scrapCount;
            if (this.mViewTypeCount == 1) {
                scrap = this.mCurrentScrap;
                scrapCount = scrap.size();
                for (i = 0; i < scrapCount; i++) {
                    ((View) scrap.get(i)).setDrawingCacheBackgroundColor(color);
                }
            } else {
                int typeCount = this.mViewTypeCount;
                for (i = 0; i < typeCount; i++) {
                    scrap = this.mScrapViews[i];
                    scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        ((View) scrap.get(j)).setDrawingCacheBackgroundColor(color);
                    }
                }
            }
            for (View victim : this.mActiveViews) {
                if (victim != null) {
                    victim.setDrawingCacheBackgroundColor(color);
                }
            }
        }

        private View retrieveFromScrap(ArrayList<View> scrapViews, int position) {
            int size = scrapViews.size();
            if (AbsListView.IS_ENG_BUILD && AbsListView.sDbgRecycle && AbsListView.sDbg) {
                Log.d(AbsListView.TAG, "retrieveFromScrap: size = " + size + ", position = " + position + ", scrapViews = " + scrapViews);
            }
            if (size <= 0) {
                return null;
            }
            View scrap;
            for (int i = size - 1; i >= 0; i--) {
                LayoutParams params = (LayoutParams) ((View) scrapViews.get(i)).getLayoutParams();
                if (this.this$0.mAdapterHasStableIds) {
                    if (this.this$0.mAdapter.getItemId(position) == params.itemId) {
                        return (View) scrapViews.remove(i);
                    }
                } else if (params.scrappedFromPosition == position) {
                    scrap = (View) scrapViews.remove(i);
                    clearAccessibilityFromScrap(scrap);
                    return scrap;
                }
            }
            scrap = (View) scrapViews.remove(size - 1);
            clearAccessibilityFromScrap(scrap);
            return scrap;
        }

        private void clearScrap(ArrayList<View> scrap) {
            int scrapCount = scrap.size();
            for (int j = 0; j < scrapCount; j++) {
                removeDetachedView((View) scrap.remove((scrapCount - 1) - j), false);
            }
        }

        private void clearAccessibilityFromScrap(View view) {
            view.clearAccessibilityFocus();
            view.setAccessibilityDelegate(null);
        }

        private void removeDetachedView(View child, boolean animate) {
            child.setAccessibilityDelegate(null);
            this.this$0.-wrap0(child, animate);
        }
    }

    public interface RecyclerListener {
        void onMovedToScrapHeap(View view);
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        LongSparseArray<Integer> checkIdState;
        SparseBooleanArray checkState;
        int checkedItemCount;
        String filter;
        long firstId;
        int height;
        boolean inActionMode;
        int position;
        long selectedId;
        int viewTop;

        /* renamed from: android.widget.AbsListView$SavedState$1 */
        static class AnonymousClass1 implements Creator<SavedState> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public /* bridge */ /* synthetic */ Object[] newArray(int size) {
                return newArray(size);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.AbsListView.SavedState.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.AbsListView.SavedState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.SavedState.<clinit>():void");
        }

        /* synthetic */ SavedState(Parcel in, SavedState savedState) {
            this(in);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            boolean z = false;
            super(in);
            this.selectedId = in.readLong();
            this.firstId = in.readLong();
            this.viewTop = in.readInt();
            this.position = in.readInt();
            this.height = in.readInt();
            this.filter = in.readString();
            if (in.readByte() != (byte) 0) {
                z = true;
            }
            this.inActionMode = z;
            this.checkedItemCount = in.readInt();
            this.checkState = in.readSparseBooleanArray();
            int N = in.readInt();
            if (N > 0) {
                this.checkIdState = new LongSparseArray();
                for (int i = 0; i < N; i++) {
                    this.checkIdState.put(in.readLong(), Integer.valueOf(in.readInt()));
                }
            }
        }

        public void writeToParcel(Parcel out, int flags) {
            int i;
            int N;
            super.writeToParcel(out, flags);
            out.writeLong(this.selectedId);
            out.writeLong(this.firstId);
            out.writeInt(this.viewTop);
            out.writeInt(this.position);
            out.writeInt(this.height);
            out.writeString(this.filter);
            if (this.inActionMode) {
                i = 1;
            } else {
                i = 0;
            }
            out.writeByte((byte) i);
            out.writeInt(this.checkedItemCount);
            out.writeSparseBooleanArray(this.checkState);
            if (this.checkIdState != null) {
                N = this.checkIdState.size();
            } else {
                N = 0;
            }
            out.writeInt(N);
            for (int i2 = 0; i2 < N; i2++) {
                out.writeLong(this.checkIdState.keyAt(i2));
                out.writeInt(((Integer) this.checkIdState.valueAt(i2)).intValue());
            }
        }

        public String toString() {
            return "AbsListView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " selectedId=" + this.selectedId + " firstId=" + this.firstId + " viewTop=" + this.viewTop + " position=" + this.position + " height=" + this.height + " filter=" + this.filter + " checkState=" + this.checkState + "}";
        }
    }

    public interface SelectionBoundsAdjuster {
        void adjustListItemSelectionBounds(Rect rect);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.AbsListView.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.AbsListView.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.<clinit>():void");
    }

    abstract void fillGap(boolean z);

    abstract int findMotionRow(int i);

    abstract void setSelectionInt(int i);

    public AbsListView(Context context) {
        super(context);
        this.mChoiceMode = 0;
        this.mLayoutMode = 0;
        this.mDeferNotifyDataSetChanged = false;
        this.mDrawSelectorOnTop = false;
        this.mSelectorPosition = -1;
        this.mSelectorRect = new Rect();
        this.mRecycler = new RecycleBin(this);
        this.mSelectionLeftPadding = 0;
        this.mSelectionTopPadding = 0;
        this.mSelectionRightPadding = 0;
        this.mSelectionBottomPadding = 0;
        this.mListPadding = new Rect();
        this.mWidthMeasureSpec = 0;
        this.mTouchMode = -1;
        this.mSelectedTop = 0;
        this.mSmoothScrollbarEnabled = true;
        this.mResurrectToPosition = -1;
        this.mContextMenuInfo = null;
        this.mLastTouchMode = -1;
        this.mScrollProfilingStarted = false;
        this.mFlingProfilingStarted = false;
        this.mScrollStrictSpan = null;
        this.mFlingStrictSpan = null;
        this.mLastScrollState = 0;
        this.mVelocityScale = 1.0f;
        this.mIsScrap = new boolean[1];
        this.mScrollOffset = new int[2];
        this.mScrollConsumed = new int[2];
        this.mTmpPoint = new float[2];
        this.mNestedYOffset = 0;
        this.mActivePointerId = -1;
        this.mDirection = 0;
        this.mOverScale = 1.0f;
        this.BACK_TO_TOP_SPEED = 4500;
        this.mHasIgnore = false;
        this.mIsMutiPoint = false;
        initAbsListView();
        this.mOwnerThread = Thread.currentThread();
        setVerticalScrollBarEnabled(true);
        TypedArray a = context.obtainStyledAttributes(R.styleable.View);
        initializeScrollbarsInternal(a);
        a.recycle();
    }

    public AbsListView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.absListViewStyle);
    }

    public AbsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "LiangJing.Fu@Plf.SDK, 2013-01-21 : Add for initialization oppo property", property = OppoRomType.ROM)
    public AbsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mChoiceMode = 0;
        this.mLayoutMode = 0;
        this.mDeferNotifyDataSetChanged = false;
        this.mDrawSelectorOnTop = false;
        this.mSelectorPosition = -1;
        this.mSelectorRect = new Rect();
        this.mRecycler = new RecycleBin(this);
        this.mSelectionLeftPadding = 0;
        this.mSelectionTopPadding = 0;
        this.mSelectionRightPadding = 0;
        this.mSelectionBottomPadding = 0;
        this.mListPadding = new Rect();
        this.mWidthMeasureSpec = 0;
        this.mTouchMode = -1;
        this.mSelectedTop = 0;
        this.mSmoothScrollbarEnabled = true;
        this.mResurrectToPosition = -1;
        this.mContextMenuInfo = null;
        this.mLastTouchMode = -1;
        this.mScrollProfilingStarted = false;
        this.mFlingProfilingStarted = false;
        this.mScrollStrictSpan = null;
        this.mFlingStrictSpan = null;
        this.mLastScrollState = 0;
        this.mVelocityScale = 1.0f;
        this.mIsScrap = new boolean[1];
        this.mScrollOffset = new int[2];
        this.mScrollConsumed = new int[2];
        this.mTmpPoint = new float[2];
        this.mNestedYOffset = 0;
        this.mActivePointerId = -1;
        this.mDirection = 0;
        this.mOverScale = 1.0f;
        this.BACK_TO_TOP_SPEED = 4500;
        this.mHasIgnore = false;
        this.mIsMutiPoint = false;
        initAbsListView();
        this.mOwnerThread = Thread.currentThread();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AbsListView, defStyleAttr, defStyleRes);
        Drawable selector = a.getDrawable(0);
        if (selector != null) {
            setSelector(selector);
        }
        this.mDrawSelectorOnTop = a.getBoolean(1, false);
        setStackFromBottom(a.getBoolean(2, false));
        setScrollingCacheEnabled(a.getBoolean(3, true));
        setTextFilterEnabled(a.getBoolean(4, false));
        setTranscriptMode(a.getInt(5, 0));
        setCacheColorHint(a.getColor(6, 0));
        setSmoothScrollbarEnabled(a.getBoolean(9, true));
        setChoiceMode(a.getInt(7, 0));
        setFastScrollEnabled(a.getBoolean(8, false));
        setFastScrollStyle(a.getResourceId(11, 0));
        setFastScrollAlwaysVisible(a.getBoolean(10, false));
        a.recycle();
        if (IS_ENG_BUILD) {
            checkAbsListViewlLogProperty();
        }
        initOppoProperty(context);
    }

    private void initAbsListView() {
        setClickable(true);
        setFocusableInTouchMode(true);
        setWillNotDraw(false);
        setAlwaysDrawnWithCacheEnabled(false);
        setScrollingCacheEnabled(true);
        ViewConfiguration configuration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mOverscrollDistance = configuration.getScaledOverscrollDistance();
        this.mOverflingDistance = configuration.getScaledOverflingDistance();
        this.mDensityScale = getContext().getResources().getDisplayMetrics().density;
        String packageName = getContext().getPackageName();
        if (packageName.equals(LMManager.MM_PACKAGENAME) || packageName.equals("gavin.example.abslistviewtest")) {
            isEnableEndFlingProtect = true;
        }
        if (IS_ENG_BUILD && sDbgLayout) {
            Log.d(TAG, "initAbsListView here: this = " + this);
        }
    }

    public void setOverScrollMode(int mode) {
        if (mode == 2) {
            this.mEdgeGlowTop = null;
            this.mEdgeGlowBottom = null;
        } else if (this.mEdgeGlowTop == null) {
            Context context = getContext();
            this.mEdgeGlowTop = new EdgeEffect(context);
            this.mEdgeGlowBottom = new EdgeEffect(context);
        }
        super.setOverScrollMode(mode);
    }

    public /* bridge */ /* synthetic */ void setAdapter(Adapter adapter) {
        setAdapter((ListAdapter) adapter);
    }

    public void setAdapter(ListAdapter adapter) {
        if (adapter != null) {
            this.mAdapterHasStableIds = this.mAdapter.hasStableIds();
            if (this.mChoiceMode != 0 && this.mAdapterHasStableIds && this.mCheckedIdStates == null) {
                this.mCheckedIdStates = new LongSparseArray();
            }
        }
        if (this.mCheckStates != null) {
            this.mCheckStates.clear();
        }
        if (this.mCheckedIdStates != null) {
            this.mCheckedIdStates.clear();
        }
    }

    public int getCheckedItemCount() {
        return this.mCheckedItemCount;
    }

    public boolean isItemChecked(int position) {
        if (this.mChoiceMode == 0 || this.mCheckStates == null) {
            return false;
        }
        return this.mCheckStates.get(position);
    }

    public int getCheckedItemPosition() {
        if (this.mChoiceMode == 1 && this.mCheckStates != null && this.mCheckStates.size() == 1) {
            return this.mCheckStates.keyAt(0);
        }
        return -1;
    }

    public SparseBooleanArray getCheckedItemPositions() {
        if (this.mChoiceMode != 0) {
            return this.mCheckStates;
        }
        return null;
    }

    public long[] getCheckedItemIds() {
        if (this.mChoiceMode == 0 || this.mCheckedIdStates == null || this.mAdapter == null) {
            return new long[0];
        }
        LongSparseArray<Integer> idStates = this.mCheckedIdStates;
        int count = idStates.size();
        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = idStates.keyAt(i);
        }
        return ids;
    }

    public void clearChoices() {
        if (this.mCheckStates != null) {
            this.mCheckStates.clear();
        }
        if (this.mCheckedIdStates != null) {
            this.mCheckedIdStates.clear();
        }
        this.mCheckedItemCount = 0;
    }

    public void setItemChecked(int position, boolean value) {
        if (this.mChoiceMode != 0) {
            if (value && this.mChoiceMode == 3 && this.mChoiceActionMode == null) {
                if (this.mMultiChoiceModeCallback == null || !this.mMultiChoiceModeCallback.hasWrappedCallback()) {
                    throw new IllegalStateException("AbsListView: attempted to start selection mode for CHOICE_MODE_MULTIPLE_MODAL but no choice mode callback was supplied. Call setMultiChoiceModeListener to set a callback.");
                }
                this.mChoiceActionMode = startActionMode(this.mMultiChoiceModeCallback);
            }
            boolean itemCheckChanged;
            if (this.mChoiceMode == 2 || this.mChoiceMode == 3) {
                boolean oldValue = this.mCheckStates.get(position);
                this.mCheckStates.put(position, value);
                if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                    if (value) {
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    } else {
                        this.mCheckedIdStates.delete(this.mAdapter.getItemId(position));
                    }
                }
                if (oldValue != value) {
                    itemCheckChanged = true;
                } else {
                    itemCheckChanged = false;
                }
                if (itemCheckChanged) {
                    if (value) {
                        this.mCheckedItemCount++;
                    } else {
                        this.mCheckedItemCount--;
                    }
                }
                if (this.mChoiceActionMode != null) {
                    this.mMultiChoiceModeCallback.onItemCheckedStateChanged(this.mChoiceActionMode, position, this.mAdapter.getItemId(position), value);
                }
            } else {
                boolean updateIds = this.mCheckedIdStates != null ? this.mAdapter.hasStableIds() : false;
                itemCheckChanged = isItemChecked(position) != value;
                if (value || isItemChecked(position)) {
                    this.mCheckStates.clear();
                    if (updateIds) {
                        this.mCheckedIdStates.clear();
                    }
                }
                if (value) {
                    this.mCheckStates.put(position, true);
                    if (updateIds) {
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    }
                    this.mCheckedItemCount = 1;
                } else if (this.mCheckStates.size() == 0 || !this.mCheckStates.valueAt(0)) {
                    this.mCheckedItemCount = 0;
                }
            }
            if (!(this.mInLayout || this.mBlockLayoutRequests || !itemCheckChanged)) {
                setDataChanged(true);
                rememberSyncState();
                requestLayout();
            }
        }
    }

    public boolean performItemClick(View view, int position, long id) {
        boolean handled = false;
        boolean dispatchItemClick = true;
        if (this.mChoiceMode != 0) {
            handled = true;
            boolean checkedStateChanged = false;
            boolean checked;
            if (this.mChoiceMode == 2 || (this.mChoiceMode == 3 && this.mChoiceActionMode != null)) {
                checked = !this.mCheckStates.get(position, false);
                this.mCheckStates.put(position, checked);
                if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                    if (checked) {
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    } else {
                        this.mCheckedIdStates.delete(this.mAdapter.getItemId(position));
                    }
                }
                if (checked) {
                    this.mCheckedItemCount++;
                } else {
                    this.mCheckedItemCount--;
                }
                if (this.mChoiceActionMode != null) {
                    this.mMultiChoiceModeCallback.onItemCheckedStateChanged(this.mChoiceActionMode, position, id, checked);
                    dispatchItemClick = false;
                }
                checkedStateChanged = true;
            } else if (this.mChoiceMode == 1) {
                if (this.mCheckStates.get(position, false)) {
                    checked = false;
                } else {
                    checked = true;
                }
                if (checked) {
                    this.mCheckStates.clear();
                    this.mCheckStates.put(position, true);
                    if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                        this.mCheckedIdStates.clear();
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    }
                    this.mCheckedItemCount = 1;
                } else if (this.mCheckStates.size() == 0 || !this.mCheckStates.valueAt(0)) {
                    this.mCheckedItemCount = 0;
                }
                checkedStateChanged = true;
            }
            if (checkedStateChanged) {
                updateOnScreenCheckedViews();
            }
        }
        if (IS_ENG_BUILD && sDbg) {
            Log.d(TAG, "performItemClick view=" + view + ", position=" + position + ", id=" + id + ", mChoiceMode=" + this.mChoiceMode + ", dispatchItemClick=" + dispatchItemClick);
        }
        if (dispatchItemClick) {
            return handled | super.performItemClick(view, position, id);
        }
        return handled;
    }

    private void updateOnScreenCheckedViews() {
        int firstPos = this.mFirstPosition;
        int count = getChildCount();
        boolean useActivated = getContext().getApplicationInfo().targetSdkVersion >= 11;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int position = firstPos + i;
            if (child instanceof Checkable) {
                ((Checkable) child).setChecked(this.mCheckStates.get(position));
            } else if (useActivated) {
                child.setActivated(this.mCheckStates.get(position));
            }
        }
    }

    public int getChoiceMode() {
        return this.mChoiceMode;
    }

    public void setChoiceMode(int choiceMode) {
        this.mChoiceMode = choiceMode;
        if (this.mChoiceActionMode != null) {
            this.mChoiceActionMode.finish();
            this.mChoiceActionMode = null;
        }
        if (this.mChoiceMode != 0) {
            if (this.mCheckStates == null) {
                this.mCheckStates = new SparseBooleanArray(0);
            }
            if (this.mCheckedIdStates == null && this.mAdapter != null && this.mAdapter.hasStableIds()) {
                this.mCheckedIdStates = new LongSparseArray(0);
            }
            if (this.mChoiceMode == 3) {
                clearChoices();
                setLongClickable(true);
            }
        }
    }

    public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
        if (this.mMultiChoiceModeCallback == null) {
            this.mMultiChoiceModeCallback = new MultiChoiceModeWrapper(this);
        }
        this.mMultiChoiceModeCallback.setWrapped(listener);
    }

    private boolean contentFits() {
        boolean z = true;
        int childCount = getChildCount();
        if (childCount == 0) {
            return true;
        }
        if (childCount != this.mItemCount) {
            return false;
        }
        if (IS_ENG_BUILD && sDbg) {
            Log.d(TAG, "contentFits:childCount = " + childCount + ",mItemCount = " + this.mItemCount + ",first child top = " + getChildAt(0).getTop() + ",last child bottom = " + getChildAt(childCount - 1).getBottom() + ",getHeight() = " + getHeight() + ",mListPadding = " + this.mListPadding);
        }
        if (getChildAt(0).getTop() < this.mListPadding.top) {
            z = false;
        } else if (getChildAt(childCount - 1).getBottom() > getHeight() - this.mListPadding.bottom) {
            z = false;
        }
        return z;
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public void setFastScrollEnabled(boolean r2) {
        /*
        r1 = this;
        r0 = r1.mFastScrollEnabled;
        if (r0 == r2) goto L_0x000f;
    L_0x0004:
        r1.mFastScrollEnabled = r2;
        r0 = r1.isOwnerThread();
        if (r0 == 0) goto L_0x0010;
    L_0x000c:
        r1.setFastScrollerEnabledUiThread(r2);
    L_0x000f:
        return;
    L_0x0010:
        r0 = new android.widget.AbsListView$1;
        r0.<init>(r1, r2);
        r1.post(r0);
        goto L_0x000f;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.setFastScrollEnabled(boolean):void");
    }

    private void setFastScrollerEnabledUiThread(boolean enabled) {
        if (this.mFastScroll != null) {
            this.mFastScroll.setEnabled(enabled);
        } else if (enabled) {
            this.mFastScroll = new FastScroller(this, this.mFastScrollStyle);
            this.mFastScroll.setEnabled(true);
        }
        resolvePadding();
        if (this.mFastScroll != null) {
            this.mFastScroll.updateLayout();
        }
    }

    public void setFastScrollStyle(int styleResId) {
        if (this.mFastScroll == null) {
            this.mFastScrollStyle = styleResId;
        } else {
            this.mFastScroll.setStyle(styleResId);
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public void setFastScrollAlwaysVisible(boolean r2) {
        /*
        r1 = this;
        r0 = r1.mFastScrollAlwaysVisible;
        if (r0 == r2) goto L_0x0015;
    L_0x0004:
        if (r2 == 0) goto L_0x000a;
    L_0x0006:
        r0 = r1.mFastScrollEnabled;
        if (r0 == 0) goto L_0x0016;
    L_0x000a:
        r1.mFastScrollAlwaysVisible = r2;
        r0 = r1.isOwnerThread();
        if (r0 == 0) goto L_0x001b;
    L_0x0012:
        r1.setFastScrollerAlwaysVisibleUiThread(r2);
    L_0x0015:
        return;
    L_0x0016:
        r0 = 1;
        r1.setFastScrollEnabled(r0);
        goto L_0x000a;
    L_0x001b:
        r0 = new android.widget.AbsListView$2;
        r0.<init>(r1, r2);
        r1.post(r0);
        goto L_0x0015;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.AbsListView.setFastScrollAlwaysVisible(boolean):void");
    }

    private void setFastScrollerAlwaysVisibleUiThread(boolean alwaysShow) {
        if (this.mFastScroll != null) {
            this.mFastScroll.setAlwaysShow(alwaysShow);
        }
    }

    private boolean isOwnerThread() {
        return this.mOwnerThread == Thread.currentThread();
    }

    public boolean isFastScrollAlwaysVisible() {
        boolean z = false;
        if (this.mFastScroll == null) {
            if (this.mFastScrollEnabled) {
                z = this.mFastScrollAlwaysVisible;
            }
            return z;
        }
        if (this.mFastScroll.isEnabled()) {
            z = this.mFastScroll.isAlwaysShowEnabled();
        }
        return z;
    }

    public int getVerticalScrollbarWidth() {
        if (this.mFastScroll == null || !this.mFastScroll.isEnabled()) {
            return super.getVerticalScrollbarWidth();
        }
        return Math.max(super.getVerticalScrollbarWidth(), this.mFastScroll.getWidth());
    }

    @ExportedProperty
    public boolean isFastScrollEnabled() {
        if (this.mFastScroll == null) {
            return this.mFastScrollEnabled;
        }
        return this.mFastScroll.isEnabled();
    }

    public void setVerticalScrollbarPosition(int position) {
        super.setVerticalScrollbarPosition(position);
        if (this.mFastScroll != null) {
            this.mFastScroll.setScrollbarPosition(position);
        }
    }

    public void setScrollBarStyle(int style) {
        super.setScrollBarStyle(style);
        if (this.mFastScroll != null) {
            this.mFastScroll.setScrollBarStyle(style);
        }
    }

    protected boolean isVerticalScrollBarHidden() {
        return isFastScrollEnabled();
    }

    public void setSmoothScrollbarEnabled(boolean enabled) {
        this.mSmoothScrollbarEnabled = enabled;
    }

    @ExportedProperty
    public boolean isSmoothScrollbarEnabled() {
        return this.mSmoothScrollbarEnabled;
    }

    public void setOnScrollListener(OnScrollListener l) {
        if (IS_ENG_BUILD && sDbg) {
            Log.d(TAG, "setOnScrollListener: OnScrollListener=" + l + ", this=" + this);
        }
        this.mOnScrollListener = l;
        invokeOnItemScrollListener();
    }

    void invokeOnItemScrollListener() {
        if (this.mFastScroll != null) {
            this.mFastScroll.onScroll(this.mFirstPosition, getChildCount(), this.mItemCount);
        }
        if (this.mOnScrollListener != null) {
            this.mOnScrollListener.onScroll(this, this.mFirstPosition, getChildCount(), this.mItemCount);
        }
        onScrollChanged(0, 0, 0, 0);
    }

    public void sendAccessibilityEventInternal(int eventType) {
        if (eventType == 4096) {
            int firstVisiblePosition = getFirstVisiblePosition();
            int lastVisiblePosition = getLastVisiblePosition();
            if (this.mLastAccessibilityScrollEventFromIndex != firstVisiblePosition || this.mLastAccessibilityScrollEventToIndex != lastVisiblePosition) {
                this.mLastAccessibilityScrollEventFromIndex = firstVisiblePosition;
                this.mLastAccessibilityScrollEventToIndex = lastVisiblePosition;
            } else {
                return;
            }
        }
        super.sendAccessibilityEventInternal(eventType);
    }

    public CharSequence getAccessibilityClassName() {
        return AbsListView.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (isEnabled()) {
            if (canScrollUp()) {
                info.addAction(AccessibilityAction.ACTION_SCROLL_BACKWARD);
                info.addAction(AccessibilityAction.ACTION_SCROLL_UP);
                info.setScrollable(true);
            }
            if (canScrollDown()) {
                info.addAction(AccessibilityAction.ACTION_SCROLL_FORWARD);
                info.addAction(AccessibilityAction.ACTION_SCROLL_DOWN);
                info.setScrollable(true);
            }
        }
        info.removeAction(AccessibilityAction.ACTION_CLICK);
        info.setClickable(false);
    }

    int getSelectionModeForAccessibility() {
        switch (getChoiceMode()) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
            case 3:
                return 2;
            default:
                return 0;
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        switch (action) {
            case 4096:
            case R.id.accessibilityActionScrollDown /*16908346*/:
                if (!isEnabled() || !canScrollDown()) {
                    return false;
                }
                smoothScrollBy((getHeight() - this.mListPadding.top) - this.mListPadding.bottom, 200);
                return true;
            case 8192:
            case R.id.accessibilityActionScrollUp /*16908344*/:
                if (!isEnabled() || !canScrollUp()) {
                    return false;
                }
                smoothScrollBy(-((getHeight() - this.mListPadding.top) - this.mListPadding.bottom), 200);
                return true;
            default:
                return false;
        }
    }

    public View findViewByAccessibilityIdTraversal(int accessibilityId) {
        if (accessibilityId == getAccessibilityViewId()) {
            return this;
        }
        return super.findViewByAccessibilityIdTraversal(accessibilityId);
    }

    @ExportedProperty
    public boolean isScrollingCacheEnabled() {
        return this.mScrollingCacheEnabled;
    }

    public void setScrollingCacheEnabled(boolean enabled) {
        if (this.mScrollingCacheEnabled && !enabled) {
            clearScrollingCache();
        }
        this.mScrollingCacheEnabled = enabled;
    }

    public void setTextFilterEnabled(boolean textFilterEnabled) {
        this.mTextFilterEnabled = textFilterEnabled;
    }

    @ExportedProperty
    public boolean isTextFilterEnabled() {
        return this.mTextFilterEnabled;
    }

    public void getFocusedRect(Rect r) {
        View view = getSelectedView();
        if (view == null || view.getParent() != this) {
            super.getFocusedRect(r);
            return;
        }
        view.getFocusedRect(r);
        offsetDescendantRectToMyCoords(view, r);
    }

    private void useDefaultSelector() {
        setSelector(getContext().getDrawable(R.drawable.list_selector_background));
    }

    @ExportedProperty
    public boolean isStackFromBottom() {
        return this.mStackFromBottom;
    }

    public void setStackFromBottom(boolean stackFromBottom) {
        if (this.mStackFromBottom != stackFromBottom) {
            this.mStackFromBottom = stackFromBottom;
            requestLayoutIfNecessary();
        }
    }

    void requestLayoutIfNecessary() {
        if (getChildCount() > 0) {
            resetList();
            requestLayout();
            invalidate();
        }
    }

    public Parcelable onSaveInstanceState() {
        dismissPopup();
        SavedState ss = new SavedState(super.-wrap0());
        if (this.mPendingSync != null) {
            ss.selectedId = this.mPendingSync.selectedId;
            ss.firstId = this.mPendingSync.firstId;
            ss.viewTop = this.mPendingSync.viewTop;
            ss.position = this.mPendingSync.position;
            ss.height = this.mPendingSync.height;
            ss.filter = this.mPendingSync.filter;
            ss.inActionMode = this.mPendingSync.inActionMode;
            ss.checkedItemCount = this.mPendingSync.checkedItemCount;
            ss.checkState = this.mPendingSync.checkState;
            ss.checkIdState = this.mPendingSync.checkIdState;
            return ss;
        }
        boolean haveChildren = getChildCount() > 0 && this.mItemCount > 0;
        long selectedId = getSelectedItemId();
        ss.selectedId = selectedId;
        ss.height = getHeight();
        if (selectedId >= 0) {
            ss.viewTop = this.mSelectedTop;
            ss.position = getSelectedItemPosition();
            ss.firstId = -1;
        } else if (!haveChildren || this.mFirstPosition <= 0) {
            ss.viewTop = 0;
            ss.firstId = -1;
            ss.position = 0;
        } else {
            ss.viewTop = getChildAt(0).getTop();
            int firstPos = this.mFirstPosition;
            if (firstPos >= this.mItemCount) {
                firstPos = this.mItemCount - 1;
            }
            ss.position = firstPos;
            ss.firstId = this.mAdapter.getItemId(firstPos);
        }
        ss.filter = null;
        if (this.mFiltered) {
            EditText textFilter = this.mTextFilter;
            if (textFilter != null) {
                Editable filterText = textFilter.getText();
                if (filterText != null) {
                    ss.filter = filterText.toString();
                }
            }
        }
        boolean z = this.mChoiceMode == 3 && this.mChoiceActionMode != null;
        ss.inActionMode = z;
        if (this.mCheckStates != null) {
            ss.checkState = this.mCheckStates.clone();
        }
        if (this.mCheckedIdStates != null) {
            LongSparseArray<Integer> idState = new LongSparseArray();
            int count = this.mCheckedIdStates.size();
            for (int i = 0; i < count; i++) {
                idState.put(this.mCheckedIdStates.keyAt(i), (Integer) this.mCheckedIdStates.valueAt(i));
            }
            ss.checkIdState = idState;
        }
        ss.checkedItemCount = this.mCheckedItemCount;
        if (this.mRemoteAdapter != null) {
            this.mRemoteAdapter.saveRemoteViewsCache();
        }
        if (IS_ENG_BUILD && sDbgLayout) {
            Log.d(TAG, "onSaveInstanceState: ss = " + ss + ",this = " + this);
        }
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.-wrap2(ss.getSuperState());
        setDataChanged(true);
        this.mSyncHeight = (long) ss.height;
        if (ss.selectedId >= 0) {
            this.mNeedSync = true;
            this.mPendingSync = ss;
            this.mSyncRowId = ss.selectedId;
            setSyncPosition(ss.position);
            this.mSpecificTop = ss.viewTop;
            this.mSyncMode = 0;
        } else if (ss.firstId >= 0) {
            setSelectedPositionInt(-1);
            setNextSelectedPositionInt(-1);
            this.mSelectorPosition = -1;
            this.mNeedSync = true;
            this.mPendingSync = ss;
            this.mSyncRowId = ss.firstId;
            setSyncPosition(ss.position);
            this.mSpecificTop = ss.viewTop;
            this.mSyncMode = 1;
        }
        setFilterText(ss.filter);
        if (ss.checkState != null) {
            this.mCheckStates = ss.checkState;
        }
        if (ss.checkIdState != null) {
            this.mCheckedIdStates = ss.checkIdState;
        }
        this.mCheckedItemCount = ss.checkedItemCount;
        if (ss.inActionMode && this.mChoiceMode == 3 && this.mMultiChoiceModeCallback != null) {
            this.mChoiceActionMode = startActionMode(this.mMultiChoiceModeCallback);
        }
        if (IS_ENG_BUILD && sDbgLayout) {
            Log.d(TAG, "onRestoreInstanceState: ss = " + ss + ",this = " + this);
        }
        requestLayout();
    }

    private boolean acceptFilter() {
        if (!this.mTextFilterEnabled || !(getAdapter() instanceof Filterable)) {
            return false;
        }
        if (((Filterable) getAdapter()).getFilter() != null) {
            return true;
        }
        return false;
    }

    public void setFilterText(String filterText) {
        if (this.mTextFilterEnabled && !TextUtils.isEmpty(filterText)) {
            createTextFilter(false);
            this.mTextFilter.setText((CharSequence) filterText);
            if (this.mAdapter instanceof Filterable) {
                if (this.mPopup == null) {
                    ((Filterable) this.mAdapter).getFilter().filter(filterText);
                }
                this.mFiltered = true;
                this.mDataSetObserver.clearSavedState();
            }
        }
    }

    public CharSequence getTextFilter() {
        if (!this.mTextFilterEnabled || this.mTextFilter == null) {
            return null;
        }
        return this.mTextFilter.getText();
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && this.mSelectedPosition < 0 && !isInTouchMode()) {
            if (!(isAttachedToWindow() || this.mAdapter == null)) {
                setDataChanged(true);
                this.mOldItemCount = this.mItemCount;
                this.mItemCount = this.mAdapter.getCount();
            }
            resurrectSelection();
        }
    }

    public void requestLayout() {
        if (!this.mBlockLayoutRequests && !this.mInLayout) {
            super.requestLayout();
        }
    }

    void resetList() {
        removeAllViewsInLayout();
        this.mFirstPosition = 0;
        this.mDataChanged = false;
        this.mPositionScrollAfterLayout = null;
        this.mNeedSync = false;
        this.mPendingSync = null;
        this.mOldSelectedPosition = -1;
        this.mOldSelectedRowId = Long.MIN_VALUE;
        setSelectedPositionInt(-1);
        setNextSelectedPositionInt(-1);
        this.mSelectedTop = 0;
        this.mSelectorPosition = -1;
        this.mSelectorRect.setEmpty();
        if (IS_ENG_BUILD && sDbgSelector) {
            Log.d(TAG, "mSelectorRect.setEmpty in resetList this=" + this, new Throwable("resetList"));
        }
        invalidate();
    }

    protected int computeVerticalScrollExtent() {
        int count = getChildCount();
        if (count <= 0) {
            return 0;
        }
        if (!this.mSmoothScrollbarEnabled) {
            return 1;
        }
        int extent = count * 100;
        View view = getChildAt(0);
        int top = view.getTop();
        int height = view.getHeight();
        if (height > 0) {
            extent += (top * 100) / height;
        }
        view = getChildAt(count - 1);
        int bottom = view.getBottom();
        height = view.getHeight();
        if (height > 0) {
            extent -= ((bottom - getHeight()) * 100) / height;
        }
        return extent;
    }

    protected int computeVerticalScrollOffset() {
        int firstPosition = this.mFirstPosition;
        int childCount = getChildCount();
        if (firstPosition >= 0 && childCount > 0) {
            if (this.mSmoothScrollbarEnabled) {
                View view = getChildAt(0);
                int top = view.getTop();
                int height = view.getHeight();
                if (height > 0) {
                    return Math.max(((firstPosition * 100) - ((top * 100) / height)) + ((int) (((((float) this.mScrollY) / ((float) getHeight())) * ((float) this.mItemCount)) * 100.0f)), 0);
                }
            }
            int index;
            int count = this.mItemCount;
            if (firstPosition == 0) {
                index = 0;
            } else if (firstPosition + childCount == count) {
                index = count;
            } else {
                index = firstPosition + (childCount / 2);
            }
            return (int) (((float) firstPosition) + (((float) childCount) * (((float) index) / ((float) count))));
        }
        return 0;
    }

    protected int computeVerticalScrollRange() {
        if (!this.mSmoothScrollbarEnabled) {
            return this.mItemCount;
        }
        int result = Math.max(this.mItemCount * 100, 0);
        if (this.mScrollY != 0) {
            return result + Math.abs((int) (((((float) this.mScrollY) / ((float) getHeight())) * ((float) this.mItemCount)) * 100.0f));
        }
        return result;
    }

    protected float getTopFadingEdgeStrength() {
        int count = getChildCount();
        float fadeEdge = super.getTopFadingEdgeStrength();
        if (count == 0) {
            return fadeEdge;
        }
        if (this.mFirstPosition > 0) {
            return 1.0f;
        }
        int top = getChildAt(0).getTop();
        float fadeLength = (float) getVerticalFadingEdgeLength();
        if (top < this.mPaddingTop) {
            fadeEdge = ((float) (-(top - this.mPaddingTop))) / fadeLength;
        }
        return fadeEdge;
    }

    protected float getBottomFadingEdgeStrength() {
        int count = getChildCount();
        float fadeEdge = super.getBottomFadingEdgeStrength();
        if (count == 0) {
            return fadeEdge;
        }
        if ((this.mFirstPosition + count) - 1 < this.mItemCount - 1) {
            return 1.0f;
        }
        int bottom = getChildAt(count - 1).getBottom();
        int height = getHeight();
        float fadeLength = (float) getVerticalFadingEdgeLength();
        if (bottom > height - this.mPaddingBottom) {
            fadeEdge = ((float) ((bottom - height) + this.mPaddingBottom)) / fadeLength;
        }
        return fadeEdge;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean z = true;
        if (this.mSelector == null) {
            useDefaultSelector();
        }
        Rect listPadding = this.mListPadding;
        listPadding.left = this.mSelectionLeftPadding + this.mPaddingLeft;
        listPadding.top = this.mSelectionTopPadding + this.mPaddingTop;
        listPadding.right = this.mSelectionRightPadding + this.mPaddingRight;
        listPadding.bottom = this.mSelectionBottomPadding + this.mPaddingBottom;
        if (this.mTranscriptMode == 1) {
            int childCount = getChildCount();
            int listBottom = getHeight() - getPaddingBottom();
            View lastChild = getChildAt(childCount - 1);
            int lastBottom = lastChild != null ? lastChild.getBottom() : listBottom;
            if (this.mFirstPosition + childCount < this.mLastHandledItemCount) {
                z = false;
            } else if (lastBottom > listBottom) {
                z = false;
            }
            this.mForceTranscriptScroll = z;
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int i = -1;
        super.onLayout(changed, l, t, r, b);
        this.mInLayout = true;
        int childCount = getChildCount();
        if (changed) {
            for (int i2 = 0; i2 < childCount; i2++) {
                getChildAt(i2).forceLayout();
            }
            this.mRecycler.markChildrenDirty();
        }
        if (IS_ENG_BUILD && sDbgLayout) {
            Log.d(TAG, "++onLayout mDataChanged=" + this.mDataChanged + ", mOldItemCount=" + this.mOldItemCount + ", mItemCount=" + this.mItemCount + ", countFromAdapter=" + (this.mAdapter != null ? this.mAdapter.getCount() : -1) + ", mLayoutMode=" + this.mLayoutMode + ", mSpecificTop=" + this.mSpecificTop + ", mSyncPosition=" + this.mSyncPosition + ", mSyncRowId=" + this.mSyncRowId + ", mSyncHeight=" + this.mSyncHeight + ", mNeedSync=" + this.mNeedSync + ", mSyncMode=" + this.mSyncMode + ", mResurrectToPosition=" + this.mResurrectToPosition + ", mSelectedTop=" + this.mSelectedTop + ", mAdapter=" + this.mAdapter + ", this=" + this);
        }
        layoutChildren();
        if (IS_ENG_BUILD && sDbgLayout) {
            String str = TAG;
            StringBuilder append = new StringBuilder().append("--onLayout mDataChanged=").append(this.mDataChanged).append(", mOldItemCount=").append(this.mOldItemCount).append(", mItemCount=").append(this.mItemCount).append(", countFromAdapter=");
            if (this.mAdapter != null) {
                i = this.mAdapter.getCount();
            }
            Log.d(str, append.append(i).append(", mLayoutMode=").append(this.mLayoutMode).append(", mSpecificTop=").append(this.mSpecificTop).append(", mSyncPosition=").append(this.mSyncPosition).append(", mSyncRowId=").append(this.mSyncRowId).append(", mSyncHeight=").append(this.mSyncHeight).append(", mNeedSync=").append(this.mNeedSync).append(", mSyncMode=").append(this.mSyncMode).append(", mResurrectToPosition=").append(this.mResurrectToPosition).append(", mSelectedTop=").append(this.mSelectedTop).append(", mAdapter=").append(this.mAdapter).append(", this=").append(this).toString());
        }
        this.mOverscrollMax = (b - t) / 3;
        if (this.mFastScroll != null) {
            this.mFastScroll.onItemCountChanged(getChildCount(), this.mItemCount);
            if (isLayoutRequested()) {
                this.mInLayout = true;
                layoutChildren();
                this.mInLayout = false;
            }
        }
        this.mInLayout = false;
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean changed = super.setFrame(left, top, right, bottom);
        if (changed) {
            boolean visible = getWindowVisibility() == 0;
            if (this.mFiltered && visible && this.mPopup != null && this.mPopup.isShowing()) {
                positionPopup();
            }
        }
        return changed;
    }

    protected void layoutChildren() {
    }

    View getAccessibilityFocusedChild(View focusedView) {
        View viewParent = focusedView.getParent();
        while ((viewParent instanceof View) && viewParent != this) {
            focusedView = viewParent;
            viewParent = viewParent.getParent();
        }
        if (viewParent instanceof View) {
            return focusedView;
        }
        return null;
    }

    void updateScrollIndicators() {
        int i = 0;
        if (this.mScrollUp != null) {
            int i2;
            View view = this.mScrollUp;
            if (canScrollUp()) {
                i2 = 0;
            } else {
                i2 = 4;
            }
            view.setVisibility(i2);
        }
        if (this.mScrollDown != null) {
            View view2 = this.mScrollDown;
            if (!canScrollDown()) {
                i = 4;
            }
            view2.setVisibility(i);
        }
    }

    private boolean canScrollUp() {
        boolean canScrollUp = this.mFirstPosition > 0;
        if (canScrollUp || getChildCount() <= 0) {
            return canScrollUp;
        }
        return getChildAt(0).getTop() < this.mListPadding.top;
    }

    private boolean canScrollDown() {
        boolean canScrollDown = false;
        int count = getChildCount();
        if (this.mFirstPosition + count < this.mItemCount) {
            canScrollDown = true;
        }
        if (canScrollDown || count <= 0) {
            return canScrollDown;
        }
        return getChildAt(count + -1).getBottom() > this.mBottom - this.mListPadding.bottom;
    }

    @ExportedProperty
    public View getSelectedView() {
        if (this.mItemCount <= 0 || this.mSelectedPosition < 0) {
            return null;
        }
        return getChildAt(this.mSelectedPosition - this.mFirstPosition);
    }

    public int getListPaddingTop() {
        return this.mListPadding.top;
    }

    public int getListPaddingBottom() {
        return this.mListPadding.bottom;
    }

    public int getListPaddingLeft() {
        return this.mListPadding.left;
    }

    public int getListPaddingRight() {
        return this.mListPadding.right;
    }

    View obtainView(int position, boolean[] outMetadata) {
        Trace.traceBegin(8, "obtainView");
        outMetadata[0] = false;
        View transientView = this.mRecycler.getTransientStateView(position);
        long logTime;
        long nowTime;
        if (transientView != null) {
            if (((LayoutParams) transientView.getLayoutParams()).viewType == this.mAdapter.getItemViewType(position)) {
                logTime = System.currentTimeMillis();
                View updatedView = this.mAdapter.getView(position, transientView, this);
                nowTime = System.currentTimeMillis();
                if (IS_ENG_BUILD && nowTime - logTime > 200) {
                    Log.d(TAG, "[ANR Warning] getView time too long, adapter = " + this.mAdapter + ", position = " + position + ", updatedView = " + updatedView + ", time = " + (nowTime - logTime));
                }
                if (IS_ENG_BUILD && sDbgLayout) {
                    Log.d(TAG, "obtainView1: position = " + position + ", transientView = " + transientView + ", updatedView = " + updatedView + ", this = " + this);
                    if (sDbg) {
                        updatedView.debug();
                    }
                }
                if (updatedView != transientView) {
                    setItemViewLayoutParams(updatedView, position);
                    this.mRecycler.addScrapView(updatedView, position);
                }
            }
            outMetadata[0] = true;
            transientView.dispatchFinishTemporaryDetach();
            return transientView;
        }
        View scrapView = this.mRecycler.getScrapView(position);
        logTime = System.currentTimeMillis();
        View child = this.mAdapter.getView(position, scrapView, this);
        nowTime = System.currentTimeMillis();
        if (IS_ENG_BUILD && nowTime - logTime > 200) {
            Log.d(TAG, "[ANR Warning] getView time too long, adapter = " + this.mAdapter + ", position = " + position + ", child = " + child + ", time = " + (nowTime - logTime));
        }
        if (IS_ENG_BUILD && sDbgLayout) {
            Log.d(TAG, "obtainView2: position = " + position + ",scrapView = " + scrapView + ",child = " + child + ",this = " + this);
            if (sDbg) {
                child.debug();
            }
        }
        if (scrapView != null) {
            if (child != scrapView) {
                this.mRecycler.addScrapView(scrapView, position);
            } else if (child.isTemporarilyDetached()) {
                outMetadata[0] = true;
                child.dispatchFinishTemporaryDetach();
            }
        }
        if (this.mCacheColorHint != 0) {
            child.setDrawingCacheBackgroundColor(this.mCacheColorHint);
        }
        if (child.getImportantForAccessibility() == 0) {
            child.setImportantForAccessibility(1);
        }
        setItemViewLayoutParams(child, position);
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            if (this.mAccessibilityDelegate == null) {
                this.mAccessibilityDelegate = new ListItemAccessibilityDelegate(this);
            }
            if (child.getAccessibilityDelegate() == null) {
                child.setAccessibilityDelegate(this.mAccessibilityDelegate);
            }
        }
        Trace.traceEnd(8);
        return child;
    }

    private void setItemViewLayoutParams(View child, int position) {
        android.view.ViewGroup.LayoutParams lp;
        android.view.ViewGroup.LayoutParams vlp = child.getLayoutParams();
        LayoutParams lp2;
        if (vlp == null) {
            lp2 = (LayoutParams) generateDefaultLayoutParams();
        } else if (checkLayoutParams(vlp)) {
            lp2 = (LayoutParams) vlp;
        } else {
            lp2 = (LayoutParams) generateLayoutParams(vlp);
        }
        if (this.mAdapterHasStableIds) {
            lp2.itemId = this.mAdapter.getItemId(position);
        }
        lp2.viewType = this.mAdapter.getItemViewType(position);
        lp2.isEnabled = this.mAdapter.isEnabled(position);
        if (lp2 != vlp) {
            child.setLayoutParams(lp2);
        }
    }

    public void onInitializeAccessibilityNodeInfoForItem(View view, int position, AccessibilityNodeInfo info) {
        ListAdapter adapter = (ListAdapter) getAdapter();
        if (position != -1) {
            android.view.ViewGroup.LayoutParams lp = view.getLayoutParams();
            boolean isItemEnabled;
            if (lp instanceof LayoutParams) {
                isItemEnabled = ((LayoutParams) lp).isEnabled;
            } else {
                isItemEnabled = false;
            }
            if (isEnabled() && isItemEnabled) {
                if (position == getSelectedItemPosition()) {
                    info.setSelected(true);
                    info.addAction(AccessibilityAction.ACTION_CLEAR_SELECTION);
                } else {
                    info.addAction(AccessibilityAction.ACTION_SELECT);
                }
                if (isItemClickable(view)) {
                    info.addAction(AccessibilityAction.ACTION_CLICK);
                    info.setClickable(true);
                }
                if (isLongClickable()) {
                    info.addAction(AccessibilityAction.ACTION_LONG_CLICK);
                    info.setLongClickable(true);
                }
                return;
            }
            info.setEnabled(false);
        }
    }

    private boolean isItemClickable(View view) {
        return !view.hasFocusable();
    }

    void positionSelectorLikeTouch(int position, View sel, float x, float y) {
        positionSelector(position, sel, true, x, y);
    }

    void positionSelectorLikeFocus(int position, View sel) {
        if (this.mSelector == null || this.mSelectorPosition == position || position == -1) {
            positionSelector(position, sel);
            return;
        }
        Rect bounds = this.mSelectorRect;
        positionSelector(position, sel, true, bounds.exactCenterX(), bounds.exactCenterY());
    }

    void positionSelector(int position, View sel) {
        positionSelector(position, sel, false, -1.0f, -1.0f);
    }

    private void positionSelector(int position, View sel, boolean manageHotspot, float x, float y) {
        boolean positionChanged = position != this.mSelectorPosition;
        if (position != -1) {
            this.mSelectorPosition = position;
        }
        Rect selectorRect = this.mSelectorRect;
        selectorRect.set(sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom());
        if (sel instanceof SelectionBoundsAdjuster) {
            ((SelectionBoundsAdjuster) sel).adjustListItemSelectionBounds(selectorRect);
        }
        selectorRect.left -= this.mSelectionLeftPadding;
        selectorRect.top -= this.mSelectionTopPadding;
        selectorRect.right += this.mSelectionRightPadding;
        selectorRect.bottom += this.mSelectionBottomPadding;
        boolean isChildViewEnabled = sel.isEnabled();
        if (this.mIsChildViewEnabled != isChildViewEnabled) {
            this.mIsChildViewEnabled = isChildViewEnabled;
        }
        Drawable selector = this.mSelector;
        if (selector != null) {
            if (positionChanged) {
                selector.setVisible(false, false);
                selector.setState(StateSet.NOTHING);
            }
            selector.setBounds(selectorRect);
            if (positionChanged) {
                if (getVisibility() == 0) {
                    selector.setVisible(true, false);
                }
                updateSelectorState();
            }
            if (manageHotspot) {
                selector.setHotspot(x, y);
            }
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        int saveCount = 0;
        boolean clipToPadding = (this.mGroupFlags & 34) == 34;
        if (IS_ENG_BUILD && sDbgDraw) {
            Log.d(TAG, "dispatchDraw: mScrollY = " + this.mScrollY + ",clipToPadding = " + clipToPadding + ",this = " + this);
        }
        if (clipToPadding) {
            saveCount = canvas.save();
            int scrollX = this.mScrollX;
            int scrollY = this.mScrollY;
            canvas.clipRect(this.mPaddingLeft + scrollX, this.mPaddingTop + scrollY, ((this.mRight + scrollX) - this.mLeft) - this.mPaddingRight, ((this.mBottom + scrollY) - this.mTop) - this.mPaddingBottom);
            this.mGroupFlags &= -35;
        }
        boolean drawSelectorOnTop = this.mDrawSelectorOnTop;
        if (!drawSelectorOnTop) {
            drawSelector(canvas);
        }
        super.dispatchDraw(canvas);
        if (drawSelectorOnTop) {
            drawSelector(canvas);
        }
        if (clipToPadding) {
            canvas.restoreToCount(saveCount);
            this.mGroupFlags |= 34;
        }
    }

    protected boolean isPaddingOffsetRequired() {
        return (this.mGroupFlags & 34) != 34;
    }

    protected int getLeftPaddingOffset() {
        return (this.mGroupFlags & 34) == 34 ? 0 : -this.mPaddingLeft;
    }

    protected int getTopPaddingOffset() {
        return (this.mGroupFlags & 34) == 34 ? 0 : -this.mPaddingTop;
    }

    protected int getRightPaddingOffset() {
        return (this.mGroupFlags & 34) == 34 ? 0 : this.mPaddingRight;
    }

    protected int getBottomPaddingOffset() {
        return (this.mGroupFlags & 34) == 34 ? 0 : this.mPaddingBottom;
    }

    protected void internalSetPadding(int left, int top, int right, int bottom) {
        super.internalSetPadding(left, top, right, bottom);
        if (isLayoutRequested()) {
            handleBoundsChange();
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (IS_ENG_BUILD && sDbg) {
            Log.d(TAG, "onSizeChanged w=" + w + ", h=" + h + ", oldw=" + oldw + ", oldh=" + oldh + ", getChildCount=" + getChildCount() + ", mSyncPosition=" + this.mSyncPosition + ", mNeedSync=" + this.mNeedSync + ", mSyncMode=" + this.mSyncMode + ", mAdapter" + this.mAdapter + ", this=" + this + ", callstack=" + Debug.getCallers(10));
        }
        handleBoundsChange();
        if (this.mFastScroll != null) {
            this.mFastScroll.onSizeChanged(w, h, oldw, oldh);
        }
    }

    void handleBoundsChange() {
        if (!this.mInLayout) {
            int childCount = getChildCount();
            if (childCount > 0) {
                setDataChanged(true);
                rememberSyncState();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    android.view.ViewGroup.LayoutParams lp = child.getLayoutParams();
                    if (lp == null || lp.width < 1 || lp.height < 1) {
                        child.forceLayout();
                    }
                }
            }
        }
    }

    boolean touchModeDrawsInPressedState() {
        switch (this.mTouchMode) {
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }

    boolean shouldShowSelector() {
        if (!isFocused() || isInTouchMode()) {
            return touchModeDrawsInPressedState() ? isPressed() : false;
        } else {
            return true;
        }
    }

    private void drawSelector(Canvas canvas) {
        if (IS_ENG_BUILD && sDbgSelector) {
            Log.d(TAG, "drawSelector (" + this.mSelectorRect.left + ", " + this.mSelectorRect.top + ", " + this.mSelectorRect.right + ", " + this.mSelectorRect.bottom + "), this = " + this);
            if (sDbgDraw) {
                return;
            }
        }
        if (!this.mSelectorRect.isEmpty()) {
            Drawable selector = this.mSelector;
            selector.setBounds(this.mSelectorRect);
            selector.draw(canvas);
        }
    }

    public void setDrawSelectorOnTop(boolean onTop) {
        this.mDrawSelectorOnTop = onTop;
    }

    public void setSelector(int resID) {
        setSelector(getContext().getDrawable(resID));
    }

    public void setSelector(Drawable sel) {
        if (this.mSelector != null) {
            this.mSelector.setCallback(null);
            unscheduleDrawable(this.mSelector);
        }
        this.mSelector = sel;
        Rect padding = new Rect();
        sel.getPadding(padding);
        this.mSelectionLeftPadding = padding.left;
        this.mSelectionTopPadding = padding.top;
        this.mSelectionRightPadding = padding.right;
        this.mSelectionBottomPadding = padding.bottom;
        sel.setCallback(this);
        updateSelectorState();
    }

    public Drawable getSelector() {
        return this.mSelector;
    }

    void keyPressed() {
        if (isEnabled() && isClickable()) {
            Drawable selector = this.mSelector;
            Rect selectorRect = this.mSelectorRect;
            if (selector != null && ((isFocused() || touchModeDrawsInPressedState()) && !selectorRect.isEmpty())) {
                View v = getChildAt(this.mSelectedPosition - this.mFirstPosition);
                if (v != null) {
                    if (!v.hasFocusable()) {
                        v.setPressed(true);
                    } else {
                        return;
                    }
                }
                setPressed(true);
                boolean longClickable = isLongClickable();
                Drawable d = selector.getCurrent();
                if (d != null && (d instanceof TransitionDrawable)) {
                    if (longClickable) {
                        ((TransitionDrawable) d).startTransition(ViewConfiguration.getLongPressTimeout());
                    } else {
                        ((TransitionDrawable) d).resetTransition();
                    }
                }
                if (longClickable && !this.mDataChanged) {
                    if (this.mPendingCheckForKeyLongPress == null) {
                        this.mPendingCheckForKeyLongPress = new CheckForKeyLongPress(this, null);
                    }
                    this.mPendingCheckForKeyLongPress.rememberWindowAttachCount();
                    postDelayed(this.mPendingCheckForKeyLongPress, (long) ViewConfiguration.getLongPressTimeout());
                }
            }
        }
    }

    public void setScrollIndicators(View up, View down) {
        this.mScrollUp = up;
        this.mScrollDown = down;
    }

    void updateSelectorState() {
        Drawable selector = this.mSelector;
        if (selector != null && selector.isStateful()) {
            if (IS_ENG_BUILD && sDbgSelector) {
                Log.d(TAG, "updateSelectorState shouldShowSelector=" + shouldShowSelector() + ", getDrawableState=" + getDrawableState() + ", this=" + this);
            }
            if (!shouldShowSelector()) {
                selector.setState(StateSet.NOTHING);
            } else if (selector.setState(getDrawableStateForSelector())) {
                invalidateDrawable(selector);
            }
        }
        if (!shouldShowSelector() && this.mPresedChild != null) {
            this.mPresedChild.setPressed(false);
            this.mPresedChild.postInvalidate();
        }
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateSelectorState();
    }

    private int[] getDrawableStateForSelector() {
        if (this.mIsChildViewEnabled) {
            return super.getDrawableState();
        }
        int enabledState = ENABLED_STATE_SET[0];
        int[] state = onCreateDrawableState(1);
        int enabledPos = -1;
        for (int i = state.length - 1; i >= 0; i--) {
            if (state[i] == enabledState) {
                enabledPos = i;
                break;
            }
        }
        if (enabledPos >= 0) {
            System.arraycopy(state, enabledPos + 1, state, enabledPos, (state.length - enabledPos) - 1);
        }
        return state;
    }

    public boolean verifyDrawable(Drawable dr) {
        return this.mSelector != dr ? super.verifyDrawable(dr) : true;
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mSelector != null) {
            this.mSelector.jumpToCurrentState();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewTreeObserver treeObserver = getViewTreeObserver();
        treeObserver.addOnTouchModeChangeListener(this);
        if (!(!this.mTextFilterEnabled || this.mPopup == null || this.mGlobalLayoutListenerAddedFilter)) {
            treeObserver.addOnGlobalLayoutListener(this);
        }
        if (this.mAdapter != null && this.mDataSetObserver == null) {
            this.mDataSetObserver = new AdapterDataSetObserver(this);
            this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
            setDataChanged(true);
            this.mOldItemCount = this.mItemCount;
            this.mItemCount = this.mAdapter.getCount();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mIsDetaching = true;
        dismissPopup();
        this.mRecycler.clear();
        ViewTreeObserver treeObserver = getViewTreeObserver();
        treeObserver.removeOnTouchModeChangeListener(this);
        if (this.mTextFilterEnabled && this.mPopup != null) {
            treeObserver.removeOnGlobalLayoutListener(this);
            this.mGlobalLayoutListenerAddedFilter = false;
        }
        if (!(this.mAdapter == null || this.mDataSetObserver == null)) {
            this.mAdapter.unregisterDataSetObserver(this.mDataSetObserver);
            this.mDataSetObserver = null;
        }
        if (this.mScrollStrictSpan != null) {
            this.mScrollStrictSpan.finish();
            this.mScrollStrictSpan = null;
        }
        if (this.mFlingStrictSpan != null) {
            this.mFlingStrictSpan.finish();
            this.mFlingStrictSpan = null;
        }
        if (this.mFlingRunnable != null) {
            removeCallbacks(this.mFlingRunnable);
        }
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        if (this.mClearScrollingCache != null) {
            removeCallbacks(this.mClearScrollingCache);
        }
        if (this.mPerformClick != null) {
            removeCallbacks(this.mPerformClick);
        }
        if (this.mTouchModeReset != null) {
            removeCallbacks(this.mTouchModeReset);
            this.mTouchModeReset.run();
        }
        this.mIsDetaching = false;
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (IS_ENG_BUILD && sDbg) {
            Log.d(TAG, "onWindowFocusChanged: hasWindowFocus=" + hasWindowFocus + ", this=" + this);
        }
        int touchMode = isInTouchMode() ? 0 : 1;
        if (hasWindowFocus) {
            if (isShown() && this.mFiltered && !this.mPopupHidden) {
                showPopup();
            }
            if (!(touchMode == this.mLastTouchMode || this.mLastTouchMode == -1)) {
                if (touchMode == 1) {
                    resurrectSelection();
                } else {
                    hideSelector();
                    setListLayoutMode(0);
                    layoutChildren();
                }
            }
        } else {
            setChildrenDrawingCacheEnabled(false);
            if (this.mFlingRunnable != null) {
                removeCallbacks(this.mFlingRunnable);
                this.mFlingRunnable.endFling();
                if (this.mPositionScroller != null) {
                    this.mPositionScroller.stop();
                }
                if (this.mScrollY != 0) {
                    this.mScrollY = 0;
                    invalidateParentCaches();
                    finishGlows();
                    invalidate();
                }
            }
            dismissPopup();
            if (touchMode == 1) {
                this.mResurrectToPosition = this.mSelectedPosition;
            }
        }
        this.mLastTouchMode = touchMode;
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (this.mFastScroll != null) {
            this.mFastScroll.setScrollbarPosition(getVerticalScrollbarPosition());
        }
    }

    ContextMenuInfo createContextMenuInfo(View view, int position, long id) {
        return new AdapterContextMenuInfo(view, position, id);
    }

    public void onCancelPendingInputEvents() {
        super.onCancelPendingInputEvents();
        if (this.mPerformClick != null) {
            removeCallbacks(this.mPerformClick);
        }
        if (this.mPendingCheckForTap != null) {
            removeCallbacks(this.mPendingCheckForTap);
        }
        if (this.mPendingCheckForLongPress != null) {
            removeCallbacks(this.mPendingCheckForLongPress);
        }
        if (this.mPendingCheckForKeyLongPress != null) {
            removeCallbacks(this.mPendingCheckForKeyLongPress);
        }
    }

    private boolean performStylusButtonPressAction(MotionEvent ev) {
        if (this.mChoiceMode == 3 && this.mChoiceActionMode == null) {
            View child = getChildAt(this.mMotionPosition - this.mFirstPosition);
            if (child != null && performLongPress(child, this.mMotionPosition, this.mAdapter.getItemId(this.mMotionPosition))) {
                this.mTouchMode = -1;
                setPressed(false);
                child.setPressed(false);
                return true;
            }
        }
        return false;
    }

    boolean performLongPress(View child, int longPressPosition, long longPressId) {
        return performLongPress(child, longPressPosition, longPressId, -1.0f, -1.0f);
    }

    boolean performLongPress(View child, int longPressPosition, long longPressId, float x, float y) {
        if (this.mChoiceMode == 3) {
            if (this.mChoiceActionMode == null) {
                ActionMode startActionMode = startActionMode(this.mMultiChoiceModeCallback);
                this.mChoiceActionMode = startActionMode;
                if (startActionMode != null) {
                    setItemChecked(longPressPosition, true);
                    performHapticFeedback(0);
                }
            }
            return true;
        }
        boolean handled = false;
        if (this.mOnItemLongClickListener != null) {
            handled = this.mOnItemLongClickListener.onItemLongClick(this, child, longPressPosition, longPressId);
        }
        if (!handled) {
            this.mContextMenuInfo = createContextMenuInfo(child, longPressPosition, longPressId);
            if (x == -1.0f || y == -1.0f) {
                handled = super.showContextMenuForChild(this);
            } else {
                handled = super.showContextMenuForChild(this, x, y);
            }
        }
        if (handled) {
            performHapticFeedback(0);
        }
        return handled;
    }

    protected ContextMenuInfo getContextMenuInfo() {
        return this.mContextMenuInfo;
    }

    public boolean showContextMenu() {
        return showContextMenuInternal(0.0f, 0.0f, false);
    }

    public boolean showContextMenu(float x, float y) {
        return showContextMenuInternal(x, y, true);
    }

    private boolean showContextMenuInternal(float x, float y, boolean useOffsets) {
        int position = pointToPosition((int) x, (int) y);
        if (position != -1) {
            long id = this.mAdapter.getItemId(position);
            View child = getChildAt(position - this.mFirstPosition);
            if (child != null) {
                this.mContextMenuInfo = createContextMenuInfo(child, position, id);
                if (useOffsets) {
                    return super.showContextMenuForChild(this, x, y);
                }
                return super.showContextMenuForChild(this);
            }
        }
        if (useOffsets) {
            return super.showContextMenu(x, y);
        }
        return super.showContextMenu();
    }

    public boolean showContextMenuForChild(View originalView) {
        if (isShowingContextMenuWithCoords()) {
            return false;
        }
        return showContextMenuForChildInternal(originalView, 0.0f, 0.0f, false);
    }

    public boolean showContextMenuForChild(View originalView, float x, float y) {
        return showContextMenuForChildInternal(originalView, x, y, true);
    }

    private boolean showContextMenuForChildInternal(View originalView, float x, float y, boolean useOffsets) {
        int longPressPosition = getPositionForView(originalView);
        if (longPressPosition < 0) {
            return false;
        }
        long longPressId = this.mAdapter.getItemId(longPressPosition);
        boolean handled = false;
        if (this.mOnItemLongClickListener != null) {
            handled = this.mOnItemLongClickListener.onItemLongClick(this, originalView, longPressPosition, longPressId);
        }
        if (!handled) {
            this.mContextMenuInfo = createContextMenuInfo(getChildAt(longPressPosition - this.mFirstPosition), longPressPosition, longPressId);
            if (useOffsets) {
                handled = super.showContextMenuForChild(originalView, x, y);
            } else {
                handled = super.showContextMenuForChild(originalView);
            }
        }
        return handled;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (KeyEvent.isConfirmKey(keyCode)) {
            if (!isEnabled()) {
                return true;
            }
            if (isClickable() && isPressed() && this.mSelectedPosition >= 0 && this.mAdapter != null && this.mSelectedPosition < this.mAdapter.getCount()) {
                View view = getChildAt(this.mSelectedPosition - this.mFirstPosition);
                if (view != null) {
                    performItemClick(view, this.mSelectedPosition, this.mSelectedRowId);
                    view.setPressed(false);
                }
                setPressed(false);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    protected void dispatchSetPressed(boolean pressed) {
    }

    public void dispatchDrawableHotspotChanged(float x, float y) {
    }

    public int pointToPosition(int x, int y) {
        Rect frame = this.mTouchFrame;
        if (frame == null) {
            this.mTouchFrame = new Rect();
            frame = this.mTouchFrame;
        }
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return this.mFirstPosition + i;
                }
            }
        }
        return -1;
    }

    public long pointToRowId(int x, int y) {
        int position = pointToPosition(x, y);
        if (position >= 0) {
            return this.mAdapter.getItemId(position);
        }
        return Long.MIN_VALUE;
    }

    private boolean startScrollIfNeeded(int x, int y, MotionEvent vtev) {
        int deltaY = y - this.mMotionY;
        int distance = Math.abs(deltaY);
        boolean overscroll = this.mScrollY != 0;
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "startScrollIfNeeded:deltaY = " + deltaY + ",mScrollY = " + this.mScrollY + ",mTouchSlop = " + this.mTouchSlop + ",mTouchMode = " + this.mTouchMode);
        }
        if ((!overscroll && distance <= this.mTouchSlop) || (getNestedScrollAxes() & 2) != 0) {
            return false;
        }
        createScrollingCache();
        if (overscroll) {
            this.mTouchMode = 5;
            this.mMotionCorrection = 0;
        } else {
            this.mTouchMode = 3;
            this.mMotionCorrection = deltaY > 0 ? this.mTouchSlop : -this.mTouchSlop;
        }
        removeCallbacks(this.mPendingCheckForLongPress);
        setPressed(false);
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child.isPressed()) {
                child.setPressed(false);
            }
        }
        reportScrollStateChange(1);
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        scrollIfNeeded(x, y, vtev);
        return true;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "LiangJing.Fu@Plf.SDK, 2013-01-21 : Modify for spring effect", property = OppoRomType.ROM)
    private void scrollIfNeeded(int x, int y, MotionEvent vtev) {
        int i;
        int rawDeltaY = y - this.mMotionY;
        int scrollOffsetCorrection = 0;
        int scrollConsumedCorrection = 0;
        if (this.mLastY == Integer.MIN_VALUE) {
            rawDeltaY -= this.mMotionCorrection;
        }
        if (this.mLastY != Integer.MIN_VALUE) {
            i = this.mLastY - y;
        } else {
            i = -rawDeltaY;
        }
        if (dispatchNestedPreScroll(0, i, this.mScrollConsumed, this.mScrollOffset)) {
            rawDeltaY += this.mScrollConsumed[1];
            scrollOffsetCorrection = -this.mScrollOffset[1];
            scrollConsumedCorrection = this.mScrollConsumed[1];
            if (vtev != null) {
                vtev.offsetLocation(0.0f, (float) this.mScrollOffset[1]);
                this.mNestedYOffset += this.mScrollOffset[1];
            }
        }
        int deltaY = rawDeltaY;
        int incrementalDeltaY = this.mLastY != Integer.MIN_VALUE ? (y - this.mLastY) + scrollConsumedCorrection : deltaY;
        int lastYCorrection = 0;
        View motionView;
        int overscrollMode;
        if (this.mTouchMode == 3) {
            if (this.mScrollStrictSpan == null) {
                this.mScrollStrictSpan = StrictMode.enterCriticalSpan("AbsListView-scroll");
            }
            if (y != this.mLastY) {
                int motionIndex;
                if ((this.mGroupFlags & 524288) == 0 && Math.abs(rawDeltaY) > this.mTouchSlop) {
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (this.mMotionPosition >= 0) {
                    motionIndex = this.mMotionPosition - this.mFirstPosition;
                } else {
                    motionIndex = getChildCount() / 2;
                }
                int motionViewPrevTop = 0;
                motionView = getChildAt(motionIndex);
                if (motionView != null) {
                    motionViewPrevTop = motionView.getTop();
                }
                boolean atEdge = false;
                if (incrementalDeltaY != 0) {
                    atEdge = trackMotionScroll(deltaY, incrementalDeltaY);
                }
                motionView = getChildAt(motionIndex);
                if (IS_ENG_BUILD && sDbgMotion) {
                    Log.d(TAG, "Touch scroll: incrementalDeltaY = " + incrementalDeltaY + ", mScrollY = " + this.mScrollY + ", atEdge = " + atEdge + ", motionIndex = " + motionIndex + ", motionView = " + motionView + ", overscrollMode = " + getOverScrollMode());
                }
                if (motionView != null) {
                    int motionViewRealTop = motionView.getTop();
                    if (atEdge) {
                        int overscroll = (-incrementalDeltaY) - (motionViewRealTop - motionViewPrevTop);
                        if (dispatchNestedScroll(0, overscroll - incrementalDeltaY, 0, overscroll, this.mScrollOffset)) {
                            lastYCorrection = 0 - this.mScrollOffset[1];
                            if (vtev != null) {
                                vtev.offsetLocation(0.0f, (float) this.mScrollOffset[1]);
                                this.mNestedYOffset += this.mScrollOffset[1];
                            }
                        } else {
                            boolean atOverscrollEdge = overScrollBy(0, overscroll, 0, this.mScrollY, 0, 0, 0, this.mOverscrollDistance, true);
                            if (atOverscrollEdge && this.mVelocityTracker != null) {
                                this.mVelocityTracker.clear();
                            }
                            overscrollMode = getOverScrollMode();
                            if (overscrollMode == 0 || (overscrollMode == 1 && !contentFits())) {
                                if (!atOverscrollEdge) {
                                    this.mDirection = 0;
                                    this.mTouchMode = 5;
                                }
                                if (incrementalDeltaY > 0) {
                                    this.mEdgeGlowTop.onPull(((float) (-overscroll)) / ((float) getHeight()), ((float) x) / ((float) getWidth()));
                                    if (!this.mEdgeGlowBottom.isFinished()) {
                                        this.mEdgeGlowBottom.onRelease();
                                    }
                                    invalidateTopGlow();
                                } else if (incrementalDeltaY < 0) {
                                    this.mEdgeGlowBottom.onPull(((float) overscroll) / ((float) getHeight()), 1.0f - (((float) x) / ((float) getWidth())));
                                    if (!this.mEdgeGlowTop.isFinished()) {
                                        this.mEdgeGlowTop.onRelease();
                                    }
                                    invalidateBottomGlow();
                                }
                            }
                        }
                    }
                    this.mMotionY = (y + lastYCorrection) + scrollOffsetCorrection;
                }
                this.mLastY = (y + lastYCorrection) + scrollOffsetCorrection;
            }
        } else if (this.mTouchMode == 5 && y != this.mLastY) {
            int oldScroll = this.mScrollY;
            int newScroll = oldScroll - incrementalDeltaY;
            int newDirection = y > this.mLastY ? 1 : -1;
            if (IS_ENG_BUILD && sDbgMotion) {
                Log.d(TAG, "Touch over scroll: incrementalDeltaY = " + incrementalDeltaY + ", mScrollY = " + this.mScrollY + ", newScroll = " + newScroll + ", newDirection = " + newDirection + ", mDirection = " + this.mDirection + ", y = " + y + ", mLastY = " + this.mLastY + ", mFirstPosition = " + this.mFirstPosition);
            }
            if (this.mDirection == 0) {
                this.mDirection = newDirection;
            }
            int overScrollDistance = -incrementalDeltaY;
            overScrollDistance = calcRealOverScrollDist(-incrementalDeltaY);
            if ((newScroll >= 0 || oldScroll < 0) && (newScroll <= 0 || oldScroll > 0)) {
                incrementalDeltaY = 0;
            } else {
                overScrollDistance = -oldScroll;
                incrementalDeltaY += overScrollDistance;
            }
            if (overScrollDistance != 0) {
                overScrollBy(0, overScrollDistance, 0, this.mScrollY, 0, 0, 0, this.mOverscrollDistance, true);
                overscrollMode = getOverScrollMode();
                if (overscrollMode == 0 || (overscrollMode == 1 && !contentFits())) {
                    if (rawDeltaY > 0) {
                        this.mEdgeGlowTop.onPull(((float) overScrollDistance) / ((float) getHeight()), ((float) x) / ((float) getWidth()));
                        if (!this.mEdgeGlowBottom.isFinished()) {
                            this.mEdgeGlowBottom.onRelease();
                        }
                        invalidateTopGlow();
                    } else if (rawDeltaY < 0) {
                        this.mEdgeGlowBottom.onPull(((float) overScrollDistance) / ((float) getHeight()), 1.0f - (((float) x) / ((float) getWidth())));
                        if (!this.mEdgeGlowTop.isFinished()) {
                            this.mEdgeGlowTop.onRelease();
                        }
                    }
                    invalidate();
                }
            }
            if (incrementalDeltaY != 0) {
                if (IS_ENG_BUILD && sDbgMotion) {
                    Log.d(TAG, "Coming back to 'real' list scrolling: incrementalDeltaY = " + incrementalDeltaY + ", mScrollY = " + this.mScrollY + ", mFirstPosition = " + this.mFirstPosition);
                }
                if (this.mScrollY != 0) {
                    this.mScrollY = 0;
                    invalidateParentIfNeeded();
                }
                trackMotionScroll(incrementalDeltaY, incrementalDeltaY);
                this.mTouchMode = 3;
                int motionPosition = findClosestMotionRow(y);
                this.mMotionCorrection = 0;
                motionView = getChildAt(motionPosition - this.mFirstPosition);
                this.mMotionViewOriginalTop = motionView != null ? motionView.getTop() : 0;
                this.mMotionY = y + scrollOffsetCorrection;
                this.mMotionPosition = motionPosition;
            }
            this.mLastY = (y + 0) + scrollOffsetCorrection;
            this.mDirection = newDirection;
        }
    }

    private void invalidateTopGlow() {
        if (this.mEdgeGlowTop != null) {
            boolean clipToPadding = getClipToPadding();
            int top = clipToPadding ? this.mPaddingTop : 0;
            invalidate(clipToPadding ? this.mPaddingLeft : 0, top, clipToPadding ? getWidth() - this.mPaddingRight : getWidth(), this.mEdgeGlowTop.getMaxHeight() + top);
        }
    }

    private void invalidateBottomGlow() {
        if (this.mEdgeGlowBottom != null) {
            boolean clipToPadding = getClipToPadding();
            int bottom = clipToPadding ? getHeight() - this.mPaddingBottom : getHeight();
            invalidate(clipToPadding ? this.mPaddingLeft : 0, bottom - this.mEdgeGlowBottom.getMaxHeight(), clipToPadding ? getWidth() - this.mPaddingRight : getWidth(), bottom);
        }
    }

    public void onTouchModeChanged(boolean isInTouchMode) {
        if (isInTouchMode) {
            hideSelector();
            if (getHeight() > 0 && getChildCount() > 0) {
                layoutChildren();
            }
            updateSelectorState();
            return;
        }
        int touchMode = this.mTouchMode;
        if (touchMode == 5 || touchMode == 6) {
            if (this.mFlingRunnable != null) {
                this.mFlingRunnable.endFling();
            }
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }
            if (this.mScrollY != 0) {
                this.mScrollY = 0;
                invalidateParentCaches();
                finishGlows();
                invalidate();
            }
        }
    }

    protected boolean handleScrollBarDragging(MotionEvent event) {
        return false;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "XiaoKang.Feng@Plf.SDK, 2016-12-25 : Modify for prevent touch", property = OppoRomType.ROM)
    public boolean onTouchEvent(MotionEvent ev) {
        if (isEnabled()) {
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }
            if (this.mIsDetaching || !isAttachedToWindow()) {
                return false;
            }
            startNestedScroll(2);
            if (this.mFastScroll != null && this.mFastScroll.onTouchEvent(ev)) {
                return true;
            }
            int action = ev.getAction();
            if (getChildCount() == 0 || (this.mDataChanged && this.mFirstPosition > this.mItemCount)) {
                int countFromAdapter = -1;
                if (this.mAdapter != null) {
                    countFromAdapter = this.mAdapter.getCount();
                }
                if (IS_ENG_BUILD && sDbgMotion) {
                    Log.w(TAG, "Intent to operate on non-exist data, childcount=" + getChildCount() + ", mFirstPosition=" + this.mFirstPosition + ", mItemCount=" + this.mItemCount + ", adapter count=" + countFromAdapter + ", mAdapter=" + this.mAdapter + ", action=" + action + ", mActivePointerId=" + this.mActivePointerId + ", mScrollY=" + this.mScrollY + ", this=" + this);
                }
                if ((action & 255) == 6) {
                    onSecondaryPointerUp(ev);
                }
                if (this.mPendingCheckForTap != null) {
                    removeCallbacks(this.mPendingCheckForTap);
                }
                if (this.mPendingCheckForLongPress != null) {
                    removeCallbacks(this.mPendingCheckForLongPress);
                }
                return true;
            }
            initVelocityTrackerIfNotExists();
            MotionEvent vtev = MotionEvent.obtain(ev);
            int actionMasked = ev.getActionMasked();
            if (actionMasked == 0) {
                this.mNestedYOffset = 0;
            }
            vtev.offsetLocation(0.0f, (float) this.mNestedYOffset);
            int x;
            int y;
            int motionPosition;
            switch (actionMasked) {
                case 0:
                    Trace.traceBegin(8, "listDown");
                    if (isOppoStyle()) {
                        initDown(ev);
                    }
                    onTouchDown(ev);
                    Trace.traceEnd(8);
                    break;
                case 1:
                    Trace.traceBegin(8, "listUp");
                    onTouchUp(ev);
                    Trace.traceEnd(8);
                    if (isOppoStyle()) {
                        reSetDownEvent();
                        break;
                    }
                    break;
                case 2:
                    Trace.traceBegin(8, "listMove");
                    if (!this.mHasIgnore || !isOppoStyle()) {
                        onTouchMove(ev, vtev);
                    } else if (this.mIsMutiPoint) {
                        int count = ev.getPointerCount();
                        if (count == 1) {
                            if (!isInIgnoreArea((int) ev.getX(0))) {
                                onTouchMove(ev, vtev);
                            }
                        }
                        if (count > 1) {
                            onTouchMove(ev, vtev);
                        }
                    } else {
                        onTouchMove(ev, vtev);
                    }
                    Trace.traceEnd(8);
                    break;
                case 3:
                    Trace.traceBegin(8, "listCancel");
                    onTouchCancel();
                    Trace.traceEnd(8);
                    if (isOppoStyle()) {
                        reSetDownEvent();
                        break;
                    }
                    break;
                case 5:
                    int index = ev.getActionIndex();
                    int id = ev.getPointerId(index);
                    x = Math.round(ev.getX(index));
                    y = Math.round(ev.getY(index));
                    if (IS_ENG_BUILD && (sDbgMotion || sDbgDataChange)) {
                        Log.d(TAG, "Touch second pointer down, touch mode = " + this.mTouchMode + ", mScrollY = " + this.mScrollY + ", mActivePointerId = " + this.mActivePointerId + ", id = " + id + ", index = " + index + ", mFirstPosition = " + this.mFirstPosition + ", mDataChanged = " + this.mDataChanged + ", adatper size = " + this.mItemCount + ", this = " + this);
                    }
                    this.mMotionCorrection = 0;
                    this.mActivePointerId = id;
                    this.mMotionX = x;
                    this.mMotionY = y;
                    motionPosition = pointToPosition(x, y);
                    if (motionPosition >= 0) {
                        this.mMotionViewOriginalTop = getChildAt(motionPosition - this.mFirstPosition).getTop();
                        this.mMotionPosition = motionPosition;
                    }
                    boolean onTouchDown = (!this.mIsMutiPoint || isInIgnoreArea(x)) ? false : isInIgnoreArea(this.mDownX);
                    if (!this.mHasIgnore || !onTouchDown) {
                        if (this.mHasIgnore) {
                            this.mIsMutiPoint = true;
                        }
                        this.mLastY = y;
                        break;
                    }
                    ev.setEdgeFlags(0);
                    ev.setLocation((float) x, (float) y);
                    this.mIsMutiPoint = true;
                    this.mPointDownX = x;
                    onTouchDown(ev);
                    break;
                    break;
                case 6:
                    View v = getChildAt(this.mMotionPosition - this.mFirstPosition);
                    if (!(v == null || v.hasFocusable() || !v.isPressed())) {
                        v.setPressed(false);
                    }
                    if (IS_ENG_BUILD && sDbgDataChange) {
                        Log.d(TAG, "Touch second pointer up, touch mode = " + this.mTouchMode + ",mScrollY = " + this.mScrollY + ",mActivePointerId = " + this.mActivePointerId + ",mFirstPosition = " + this.mFirstPosition + ",mDataChanged = " + this.mDataChanged + ",adatper size = " + this.mItemCount + ",this = " + this);
                    }
                    onSecondaryPointerUp(ev);
                    x = this.mMotionX;
                    y = this.mMotionY;
                    if (this.mHasIgnore && isOppoStyle()) {
                        int pointerIndex = (ev.getAction() & 65280) >> 8;
                        x = (int) ev.getX(pointerIndex);
                        y = (int) ev.getY(pointerIndex);
                    }
                    motionPosition = pointToPosition(x, y);
                    if (motionPosition >= 0) {
                        this.mMotionViewOriginalTop = getChildAt(motionPosition - this.mFirstPosition).getTop();
                        this.mMotionPosition = motionPosition;
                    }
                    boolean onTouchUP = (!this.mIsMutiPoint || isInIgnoreArea(x)) ? false : isInIgnoreArea(this.mDownX);
                    if (!isOppoStyle() || !this.mHasIgnore || !onTouchUP) {
                        this.mLastY = y;
                        break;
                    }
                    this.mMotionX = x;
                    this.mMotionY = y;
                    this.mPointUpX = x;
                    ev.setLocation((float) x, (float) y);
                    onTouchUp(ev);
                    break;
                    break;
            }
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.addMovement(vtev);
            }
            vtev.recycle();
            return true;
        }
        return !isClickable() ? isLongClickable() : true;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "XiaoKang.Feng@Plf.SDK, 2016-12-25 : Modify for prevent touch", property = OppoRomType.ROM)
    private void onTouchDown(MotionEvent ev) {
        this.mHasPerformedLongPress = false;
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "Touch down: touch mode = " + this.mTouchMode + ",mScrollY = " + this.mScrollY + ",y = " + ev.getY() + ",mFirstPosition = " + this.mFirstPosition + ",mActivePointerId = " + this.mActivePointerId + ",mDataChanged = " + this.mDataChanged + ",adatper size = " + this.mItemCount + ",this = " + this);
        }
        this.mActivePointerId = ev.getPointerId(0);
        if (isOppoStyle() && this.mIsMutiPoint && this.mHasIgnore) {
            this.mActivePointerId = ev.getPointerId(1);
        }
        if (this.mTouchMode == 6) {
            this.mFlingRunnable.endFling();
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }
            this.mTouchMode = 5;
            this.mMotionX = (int) ev.getX();
            this.mMotionY = (int) ev.getY();
            this.mLastY = this.mMotionY;
            this.mMotionCorrection = 0;
            this.mDirection = 0;
        } else {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            int motionPosition = pointToPosition(x, y);
            if (!this.mDataChanged) {
                if (this.mTouchMode == 4) {
                    createScrollingCache();
                    this.mTouchMode = 3;
                    this.mMotionCorrection = 0;
                    motionPosition = findMotionRow(y);
                    this.mFlingRunnable.flywheelTouch();
                } else if (motionPosition >= 0 && ((ListAdapter) getAdapter()).isEnabled(motionPosition)) {
                    this.mTouchMode = 0;
                    if (this.mPendingCheckForTap == null) {
                        this.mPendingCheckForTap = new CheckForTap(this, null);
                    }
                    this.mPendingCheckForTap.x = ev.getX();
                    this.mPendingCheckForTap.y = ev.getY();
                    postDelayed(this.mPendingCheckForTap, (long) ViewConfiguration.getTapTimeout());
                } else if (motionPosition >= 0 && this.mIsMutiPoint && this.mHasIgnore) {
                    this.mTouchMode = 0;
                }
            }
            if (motionPosition >= 0) {
                this.mMotionViewOriginalTop = getChildAt(motionPosition - this.mFirstPosition).getTop();
            }
            this.mMotionX = x;
            this.mMotionY = y;
            this.mMotionPosition = motionPosition;
            this.mLastY = Integer.MIN_VALUE;
            if (IS_ENG_BUILD && sDbgMotion) {
                Log.d(TAG, "Touch down: touch mode = " + this.mTouchMode + ", mMotionY = " + this.mMotionY + ", mMotionPosition = " + this.mMotionPosition + ", mFirstPosition = " + this.mFirstPosition);
            }
        }
        if (this.mTouchMode == 0 && this.mMotionPosition != -1 && performButtonActionOnTouchDown(ev)) {
            removeCallbacks(this.mPendingCheckForTap);
        }
    }

    private void onTouchMove(MotionEvent ev, MotionEvent vtev) {
        if (!this.mHasPerformedLongPress) {
            int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
            if (pointerIndex == -1) {
                pointerIndex = 0;
                this.mActivePointerId = ev.getPointerId(0);
            }
            if (this.mDataChanged) {
                layoutChildren();
            }
            int y = (int) ev.getY(pointerIndex);
            if (IS_ENG_BUILD && (sDbgMotion || sDbgDataChange)) {
                Log.d(TAG, "Touch move: touch mode = " + this.mTouchMode + ",mScrollY = " + this.mScrollY + ",mLastY = " + this.mLastY + ",y = " + y + ",mActivePointerId = " + this.mActivePointerId + ",pointerIndex = " + pointerIndex + ",mMotionPosition = " + this.mMotionPosition + ",mFirstPosition = " + this.mFirstPosition + ",mDataChanged = " + this.mDataChanged + ",adatper size = " + this.mItemCount + ",this = " + this);
            }
            switch (this.mTouchMode) {
                case 0:
                case 1:
                case 2:
                    if (!startScrollIfNeeded((int) ev.getX(pointerIndex), y, vtev)) {
                        View motionView = getChildAt(this.mMotionPosition - this.mFirstPosition);
                        float x = ev.getX(pointerIndex);
                        if (pointInView(x, (float) y, (float) this.mTouchSlop)) {
                            if (motionView != null) {
                                float[] point = this.mTmpPoint;
                                point[0] = x;
                                point[1] = (float) y;
                                transformPointToViewLocal(point, motionView);
                                motionView.drawableHotspotChanged(point[0], point[1]);
                                break;
                            }
                        }
                        setPressed(false);
                        if (motionView != null) {
                            motionView.setPressed(false);
                        }
                        removeCallbacks(this.mTouchMode == 0 ? this.mPendingCheckForTap : this.mPendingCheckForLongPress);
                        this.mTouchMode = 2;
                        updateSelectorState();
                        break;
                    }
                    break;
                case 3:
                case 5:
                    scrollIfNeeded((int) ev.getX(pointerIndex), y, vtev);
                    break;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x01a5  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x019d  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x01a5  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "XiaoKang.Feng@Plf.SDK, 2016-12-25 : Modify for prevent touch", property = OppoRomType.ROM)
    private void onTouchUp(MotionEvent ev) {
        if (IS_ENG_BUILD) {
            Log.d(TAG, "Touch up: touch mode = " + this.mTouchMode + ",mScrollY = " + this.mScrollY + ",mLastY = " + this.mLastY + ",mMotionPosition = " + this.mMotionPosition + ",mFirstPosition = " + this.mFirstPosition + ",mDataChanged = " + this.mDataChanged + ",adatper size = " + this.mItemCount + ",this = " + this);
        }
        VelocityTracker velocityTracker;
        int initialVelocity;
        switch (this.mTouchMode) {
            case -1:
                View lostChild = getChildAt(this.mMotionPosition - this.mFirstPosition);
                if (IS_ENG_BUILD && sDbgMotion) {
                    Log.d(TAG, "Touch cancel:mMotionPosition = " + this.mMotionPosition + ",mFirstPosition = " + this.mFirstPosition + ",lostChild = " + lostChild + ",this = " + this);
                }
                if (!(lostChild == null || lostChild.hasFocusable() || !lostChild.isPressed())) {
                    lostChild.setPressed(false);
                    break;
                }
            case 0:
            case 1:
            case 2:
                int motionPosition = this.mMotionPosition;
                final View child = getChildAt(motionPosition - this.mFirstPosition);
                if (child != null) {
                    boolean perform;
                    if (this.mTouchMode != 0) {
                        child.setPressed(false);
                    }
                    float x = ev.getX();
                    boolean inList = x > ((float) this.mListPadding.left) && x < ((float) (getWidth() - this.mListPadding.right));
                    this.mUpX = (int) x;
                    if (!isInIgnoreArea(this.mUpX)) {
                        if (!isInIgnoreArea(this.mDownX)) {
                            perform = true;
                            if (!inList && !child.hasFocusable() && (!isOppoStyle() || !this.mHasIgnore || perform)) {
                                if (this.mPerformClick == null) {
                                    this.mPerformClick = new PerformClick(this, null);
                                }
                                PerformClick performClick = this.mPerformClick;
                                performClick.mClickMotionPosition = motionPosition;
                                performClick.rememberWindowAttachCount();
                                this.mResurrectToPosition = motionPosition;
                                if (this.mTouchMode == 0 || this.mTouchMode == 1) {
                                    CheckForTap checkForTap;
                                    if (this.mTouchMode == 0) {
                                        checkForTap = this.mPendingCheckForTap;
                                    } else {
                                        Object checkForTap2 = this.mPendingCheckForLongPress;
                                    }
                                    removeCallbacks(checkForTap2);
                                    setListLayoutMode(0);
                                    if (this.mDataChanged || !this.mAdapter.isEnabled(motionPosition)) {
                                        this.mTouchMode = -1;
                                        updateSelectorState();
                                    } else {
                                        this.mTouchMode = 1;
                                        setSelectedPositionInt(this.mMotionPosition);
                                        layoutChildren();
                                        child.setPressed(true);
                                        positionSelector(this.mMotionPosition, child);
                                        setPressed(true);
                                        if (this.mSelector != null) {
                                            Drawable d = this.mSelector.getCurrent();
                                            if (d != null && (d instanceof TransitionDrawable)) {
                                                ((TransitionDrawable) d).resetTransition();
                                            }
                                            this.mSelector.setHotspot(x, ev.getY());
                                        }
                                        if (this.mTouchModeReset != null) {
                                            removeCallbacks(this.mTouchModeReset);
                                        }
                                        final PerformClick performClick2 = performClick;
                                        this.mTouchModeReset = new Runnable(this) {
                                            final /* synthetic */ AbsListView this$0;

                                            public void run() {
                                                this.this$0.mTouchModeReset = null;
                                                this.this$0.mTouchMode = -1;
                                                child.setPressed(false);
                                                this.this$0.setPressed(false);
                                                performClick2.run();
                                            }
                                        };
                                        postDelayed(this.mTouchModeReset, (long) ViewConfiguration.getPressedStateDuration());
                                    }
                                    return;
                                } else if (!this.mDataChanged && this.mAdapter.isEnabled(motionPosition)) {
                                    performClick.run();
                                }
                            } else if (!inList) {
                                if (IS_ENG_BUILD && sDbgMotion) {
                                    Log.d(TAG, "Touch up out of list: mTouchMode = " + this.mTouchMode + ", pressed = " + isPressed());
                                }
                                if (this.mTouchMode != 0) {
                                    setPressed(false);
                                    if (child != null) {
                                        child.setPressed(false);
                                    }
                                }
                            }
                        }
                    }
                    if (this.mIsMutiPoint) {
                        if (!isInIgnoreArea(this.mPointUpX)) {
                            if (!isInIgnoreArea(this.mPointDownX)) {
                                perform = true;
                                if (!inList) {
                                    break;
                                }
                                if (inList) {
                                }
                            }
                        }
                    }
                    perform = false;
                    if (!inList) {
                    }
                    if (inList) {
                    }
                }
                this.mTouchMode = -1;
                updateSelectorState();
                break;
            case 3:
                int childCount = getChildCount();
                if (childCount <= 0) {
                    this.mTouchMode = -1;
                    reportScrollStateChange(0);
                    break;
                }
                int firstChildTop = getChildAt(0).getTop();
                int lastChildBottom = getChildAt(childCount - 1).getBottom();
                int contentTop = this.mListPadding.top;
                int contentBottom = getHeight() - this.mListPadding.bottom;
                if (this.mFirstPosition == 0 && firstChildTop >= contentTop && this.mFirstPosition + childCount < this.mItemCount && lastChildBottom <= getHeight() - contentBottom) {
                    this.mTouchMode = -1;
                    reportScrollStateChange(0);
                    break;
                }
                velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                initialVelocity = (int) (velocityTracker.getYVelocity(this.mActivePointerId) * this.mVelocityScale);
                if (IS_ENG_BUILD && sDbgMotion) {
                    Log.d(TAG, "Fling from scroll with initialVelocity = " + initialVelocity + ",mActivePointerId = " + this.mActivePointerId + ",mFirstPosition = " + this.mFirstPosition + ",firstChildTop = " + firstChildTop + ",this = " + this);
                }
                boolean flingVelocity = Math.abs(initialVelocity) > this.mMinimumVelocity;
                if (flingVelocity && ((this.mFirstPosition != 0 || firstChildTop != contentTop - this.mOverscrollDistance) && (this.mFirstPosition + childCount != this.mItemCount || lastChildBottom != this.mOverscrollDistance + contentBottom))) {
                    if (!dispatchNestedPreFling(0.0f, (float) (-initialVelocity))) {
                        if (this.mFlingRunnable == null) {
                            this.mFlingRunnable = new FlingRunnable(this);
                        }
                        reportScrollStateChange(2);
                        this.mFlingRunnable.start(-initialVelocity);
                        dispatchNestedFling(0.0f, (float) (-initialVelocity), true);
                        break;
                    }
                    this.mTouchMode = -1;
                    reportScrollStateChange(0);
                    break;
                }
                this.mTouchMode = -1;
                reportScrollStateChange(0);
                if (this.mFlingRunnable != null) {
                    this.mFlingRunnable.endFling();
                }
                if (this.mPositionScroller != null) {
                    this.mPositionScroller.stop();
                }
                if (flingVelocity) {
                    if (!dispatchNestedPreFling(0.0f, (float) (-initialVelocity))) {
                        dispatchNestedFling(0.0f, (float) (-initialVelocity), false);
                        break;
                    }
                }
                break;
            case 5:
                if (this.mFlingRunnable == null) {
                    this.mFlingRunnable = new FlingRunnable(this);
                }
                velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                initialVelocity = (int) velocityTracker.getYVelocity(this.mActivePointerId);
                reportScrollStateChange(2);
                if (Math.abs(initialVelocity) <= this.mMinimumVelocity) {
                    this.mFlingRunnable.startSpringback();
                    break;
                } else {
                    this.mFlingRunnable.startOverfling(-initialVelocity);
                    break;
                }
        }
        setPressed(false);
        if (this.mEdgeGlowTop != null) {
            this.mEdgeGlowTop.onRelease();
            this.mEdgeGlowBottom.onRelease();
        }
        invalidate();
        removeCallbacks(this.mPendingCheckForLongPress);
        recycleVelocityTracker();
        this.mActivePointerId = -1;
        if (this.mScrollStrictSpan != null) {
            this.mScrollStrictSpan.finish();
            this.mScrollStrictSpan = null;
        }
    }

    private void onTouchCancel() {
        if (IS_ENG_BUILD && (sDbgMotion || sDbgDataChange)) {
            Log.d(TAG, "Touch cancel: touch mode = " + this.mTouchMode + ", mScrollY = " + this.mScrollY + ", mActivePointerId = " + this.mActivePointerId + ", mMotionPosition = " + this.mMotionPosition + ", mFirstPosition = " + this.mFirstPosition + ", mDataChanged = " + this.mDataChanged + ", adatper size = " + this.mItemCount + ", this = " + this);
        }
        switch (this.mTouchMode) {
            case 5:
                if (this.mFlingRunnable == null) {
                    this.mFlingRunnable = new FlingRunnable(this);
                }
                this.mFlingRunnable.startSpringback();
                break;
            case 6:
                break;
            default:
                this.mTouchMode = -1;
                setPressed(false);
                View motionView = getChildAt(this.mMotionPosition - this.mFirstPosition);
                if (motionView != null) {
                    motionView.setPressed(false);
                }
                clearScrollingCache();
                removeCallbacks(this.mPendingCheckForLongPress);
                recycleVelocityTracker();
                reportScrollStateChange(0);
                break;
        }
        if (this.mEdgeGlowTop != null) {
            this.mEdgeGlowTop.onRelease();
            this.mEdgeGlowBottom.onRelease();
        }
        this.mActivePointerId = -1;
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "onOverScrolled: mScrollY = " + this.mScrollY + ",scrollY = " + scrollY + ",clampedY = " + clampedY + ",this = " + this);
        }
        if (this.mScrollY != scrollY) {
            onScrollChanged(this.mScrollX, scrollY, this.mScrollX, this.mScrollY);
            this.mScrollY = scrollY;
            invalidateParentIfNeeded();
            awakenScrollBars();
        }
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "onGenericMotionEvent: mScrollY = " + this.mScrollY + ",event = " + event + ",this = " + this);
        }
        if ((event.getSource() & 2) != 0) {
            switch (event.getAction()) {
                case 8:
                    if (this.mTouchMode == -1) {
                        float vscroll = event.getAxisValue(9);
                        if (vscroll != 0.0f) {
                            int delta = (int) (getVerticalScrollFactor() * vscroll);
                            if (!trackMotionScroll(delta, delta)) {
                                return true;
                            }
                        }
                    }
                    break;
                case 11:
                    int actionButton = event.getActionButton();
                    if ((actionButton == 32 || actionButton == 2) && ((this.mTouchMode == 0 || this.mTouchMode == 1) && performStylusButtonPressAction(event))) {
                        removeCallbacks(this.mPendingCheckForLongPress);
                        removeCallbacks(this.mPendingCheckForTap);
                        break;
                    }
            }
        }
        return super.onGenericMotionEvent(event);
    }

    public void fling(int velocityY) {
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable(this);
        }
        reportScrollStateChange(2);
        this.mFlingRunnable.start(velocityY);
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & 2) != 0;
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(2);
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        View motionView = getChildAt(getChildCount() / 2);
        int oldTop = motionView != null ? motionView.getTop() : 0;
        if (motionView == null || trackMotionScroll(-dyUnconsumed, -dyUnconsumed)) {
            int myUnconsumed = dyUnconsumed;
            int myConsumed = 0;
            if (motionView != null) {
                myConsumed = motionView.getTop() - oldTop;
                myUnconsumed = dyUnconsumed - myConsumed;
            }
            dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null);
        }
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        int childCount = getChildCount();
        if (consumed || childCount <= 0 || !canScrollList((int) velocityY) || Math.abs(velocityY) <= ((float) this.mMinimumVelocity)) {
            return dispatchNestedFling(velocityX, velocityY, consumed);
        }
        reportScrollStateChange(2);
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable(this);
        }
        if (!dispatchNestedPreFling(0.0f, velocityY)) {
            this.mFlingRunnable.start((int) velocityY);
        }
        return true;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "LiangJing.Fu@Plf.SDK : Add for ignore EdgeEffect if is oppoStyle", property = OppoRomType.ROM)
    public void draw(Canvas canvas) {
        int i = 0;
        super.draw(canvas);
        if (isOppoStyle() && this.mTouchMode == 4 && this.mFlingRunnable != null) {
            this.mFlingRunnable.run();
        }
        if (this.mEdgeGlowTop != null && !isColorStyle()) {
            int width;
            int height;
            int translateX;
            int translateY;
            int restoreCount;
            int scrollY = this.mScrollY;
            boolean clipToPadding = getClipToPadding();
            if (clipToPadding) {
                width = (getWidth() - this.mPaddingLeft) - this.mPaddingRight;
                height = (getHeight() - this.mPaddingTop) - this.mPaddingBottom;
                translateX = this.mPaddingLeft;
                translateY = this.mPaddingTop;
            } else {
                width = getWidth();
                height = getHeight();
                translateX = 0;
                translateY = 0;
            }
            if (!this.mEdgeGlowTop.isFinished()) {
                restoreCount = canvas.save();
                canvas.clipRect(translateX, translateY, translateX + width, this.mEdgeGlowTop.getMaxHeight() + translateY);
                canvas.translate((float) translateX, (float) (Math.min(0, this.mFirstPositionDistanceGuess + scrollY) + translateY));
                this.mEdgeGlowTop.setSize(width, height);
                if (this.mEdgeGlowTop.draw(canvas)) {
                    invalidateTopGlow();
                }
                canvas.restoreToCount(restoreCount);
            }
            if (!this.mEdgeGlowBottom.isFinished()) {
                restoreCount = canvas.save();
                canvas.clipRect(translateX, (translateY + height) - this.mEdgeGlowBottom.getMaxHeight(), translateX + width, translateY + height);
                int edgeX = (-width) + translateX;
                int max = Math.max(getHeight(), this.mLastPositionDistanceGuess + scrollY);
                if (clipToPadding) {
                    i = this.mPaddingBottom;
                }
                canvas.translate((float) edgeX, (float) (max - i));
                canvas.rotate(180.0f, (float) width, 0.0f);
                this.mEdgeGlowBottom.setSize(width, height);
                if (this.mEdgeGlowBottom.draw(canvas)) {
                    invalidateBottomGlow();
                }
                canvas.restoreToCount(restoreCount);
            }
        }
    }

    private void initOrResetVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            this.mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public boolean onInterceptHoverEvent(MotionEvent event) {
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "onInterceptHoverEvent: mFastScroll = " + this.mFastScroll + ", this = " + this);
        }
        if (this.mFastScroll == null || !this.mFastScroll.onInterceptHoverEvent(event)) {
            return super.onInterceptHoverEvent(event);
        }
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "FastScroller interceptHoverEvent: action = " + event.getAction() + ", x = " + event.getX() + ", y = " + event.getY() + ", mTouchMode = " + this.mTouchMode + ", mFastScroll = " + this.mFastScroll + ", this = " + this);
        }
        return true;
    }

    @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "JinPeng@Plf.SDK, 2013-12-23 :Add for can scroll when touch blank area", property = OppoRomType.ROM)
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int actionMasked = ev.getActionMasked();
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "onInterceptTouchEvent: actionMasked = " + actionMasked + ", x = " + ev.getX() + ", y = " + ev.getY() + ", mTouchMode = " + this.mTouchMode + ", mFastScroll = " + this.mFastScroll + ", this = " + this);
        }
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        if (this.mIsDetaching || !isAttachedToWindow()) {
            return false;
        }
        if (this.mFastScroll == null || !this.mFastScroll.onInterceptTouchEvent(ev)) {
            int y;
            switch (actionMasked) {
                case 0:
                    int touchMode = this.mTouchMode;
                    if (touchMode == 6 || touchMode == 5) {
                        this.mMotionCorrection = 0;
                        return true;
                    }
                    int x = Math.round(ev.getX());
                    y = Math.round(ev.getY());
                    this.mActivePointerId = ev.getPointerId(0);
                    int motionPosition = findMotionRow(y);
                    if (touchMode != 4 && motionPosition >= 0) {
                        this.mMotionViewOriginalTop = getChildAt(motionPosition - this.mFirstPosition).getTop();
                        this.mMotionX = x;
                        this.mMotionY = y;
                        this.mMotionPosition = motionPosition;
                        this.mTouchMode = 0;
                        clearScrollingCache();
                    }
                    handleTouchBlankArea(ev, touchMode, motionPosition);
                    this.mLastY = Integer.MIN_VALUE;
                    initOrResetVelocityTracker();
                    this.mVelocityTracker.addMovement(ev);
                    this.mNestedYOffset = 0;
                    if (IS_ENG_BUILD && sDbgMotion) {
                        Log.d(TAG, "onInterceptTouchEvent touch down: motionPosition = " + motionPosition + ",mMotionPosition = " + this.mMotionPosition + ",this = " + this);
                    }
                    startNestedScroll(2);
                    if (touchMode == 4) {
                        return true;
                    }
                    break;
                case 1:
                case 3:
                    this.mTouchMode = -1;
                    this.mActivePointerId = -1;
                    recycleVelocityTracker();
                    reportScrollStateChange(0);
                    stopNestedScroll();
                    break;
                case 2:
                    switch (this.mTouchMode) {
                        case 0:
                            int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                            if (pointerIndex == -1) {
                                pointerIndex = 0;
                                this.mActivePointerId = ev.getPointerId(0);
                            }
                            y = Math.round(ev.getY(pointerIndex));
                            initVelocityTrackerIfNotExists();
                            this.mVelocityTracker.addMovement(ev);
                            if (startScrollIfNeeded((int) ev.getX(pointerIndex), y, null)) {
                                return true;
                            }
                            break;
                    }
                    break;
                case 6:
                    onSecondaryPointerUp(ev);
                    break;
            }
            return false;
        }
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "FastScroller interceptTouchEvent: actionMasked = " + actionMasked + ", x = " + ev.getX() + ", y = " + ev.getY() + ", mTouchMode = " + this.mTouchMode + ", mFastScroll = " + this.mFastScroll + ",this = " + this);
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = (ev.getAction() & 65280) >> 8;
        int pointerId = ev.getPointerId(pointerIndex);
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "onSecondaryPointerUp:pointerIndex = " + pointerIndex + ",mActivePointerId = " + this.mActivePointerId + ",pointerId = " + pointerId);
        }
        if (pointerId == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mMotionX = Math.round(ev.getX(newPointerIndex));
            this.mMotionY = Math.round(ev.getY(newPointerIndex));
            this.mMotionCorrection = 0;
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    public void addTouchables(ArrayList<View> views) {
        int count = getChildCount();
        int firstPosition = this.mFirstPosition;
        ListAdapter adapter = this.mAdapter;
        if (adapter != null) {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (adapter.isEnabled(firstPosition + i)) {
                    views.add(child);
                }
                child.addTouchables(views);
            }
        }
    }

    void reportScrollStateChange(int newState) {
        if (IS_ENG_BUILD && sDbg) {
            Log.d(TAG, "reportScrollStateChange: newState = " + newState + ", mLastScrollState = " + this.mLastScrollState + ", mOnScrollListener = " + this.mOnScrollListener + ", mScrollY = " + this.mScrollY + ", mTouchMode = " + this.mTouchMode + ", mFirstPosition = " + this.mFirstPosition);
        }
        if (newState != this.mLastScrollState && this.mOnScrollListener != null) {
            this.mLastScrollState = newState;
            this.mOnScrollListener.onScrollStateChanged(this, newState);
        }
    }

    public void setFriction(float friction) {
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable(this);
        }
        this.mFlingRunnable.mScroller.setFriction(friction);
    }

    public void setVelocityScale(float scale) {
        this.mVelocityScale = scale;
    }

    AbsPositionScroller createPositionScroller() {
        return new PositionScroller(this);
    }

    public void smoothScrollToPosition(int position) {
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "smoothScrollToPosition: position = " + position + ",mScrollY = " + this.mScrollY + ",this = " + this);
        }
        if (this.mPositionScroller == null) {
            this.mPositionScroller = createPositionScroller();
        }
        this.mPositionScroller.start(position);
    }

    public void smoothScrollToPositionFromTop(int position, int offset, int duration) {
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "smoothScrollToPositionFromTop: position = " + position + ", offset = " + offset + ", duration = " + duration + ", mScrollY = " + this.mScrollY + ", this = " + this);
        }
        if (this.mPositionScroller == null) {
            this.mPositionScroller = createPositionScroller();
        }
        this.mPositionScroller.startWithOffset(position, offset, duration);
    }

    public void smoothScrollToPositionFromTop(int position, int offset) {
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "smoothScrollToPositionFromTop: position = " + position + ",offset = " + offset + ",mPositionScroller = " + this.mPositionScroller + ",mScrollY = " + this.mScrollY + ",this = " + this);
        }
        if (this.mPositionScroller == null) {
            this.mPositionScroller = createPositionScroller();
        }
        this.mPositionScroller.startWithOffset(position, offset);
    }

    public void smoothScrollToPosition(int position, int boundPosition) {
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "smoothScrollToPosition: position = " + position + ",boundPosition = " + boundPosition + ",mPositionScroller = " + this.mPositionScroller + ",mScrollY = " + this.mScrollY + ",this = " + this);
        }
        if (this.mPositionScroller == null) {
            this.mPositionScroller = createPositionScroller();
        }
        this.mPositionScroller.start(position, boundPosition);
    }

    public void smoothScrollBy(int distance, int duration) {
        smoothScrollBy(distance, duration, false);
    }

    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoLang.Feng@Plf.SDK : Modify for Scroll Interpolator", property = OppoRomType.ROM)
    public void smoothScrollBy(int distance, int duration, Interpolator interpolator) {
        smoothScrollBy(distance, duration, interpolator, false);
    }

    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoLang.Feng@Plf.SDK : Modify for Scroll Interpolator", property = OppoRomType.ROM)
    void smoothScrollBy(int distance, int duration, boolean linear) {
        smoothScrollBy(distance, duration, null, linear);
    }

    @OppoHook(level = OppoHookType.NEW_FIELD, note = "XiaoLang.Feng@Plf.SDK : Modify for Scroll Interpolator", property = OppoRomType.ROM)
    void smoothScrollBy(int distance, int duration, Interpolator interpolator, boolean linear) {
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable(this);
        }
        int firstPos = this.mFirstPosition;
        int childCount = getChildCount();
        int lastPos = firstPos + childCount;
        int topLimit = getPaddingTop();
        int bottomLimit = getHeight() - getPaddingBottom();
        if (distance == 0 || this.mItemCount == 0 || childCount == 0 || ((firstPos == 0 && getChildAt(0).getTop() == topLimit && distance < 0) || (lastPos == this.mItemCount && getChildAt(childCount - 1).getBottom() == bottomLimit && distance > 0))) {
            if (IS_ENG_BUILD && sDbgMotion) {
                Log.d(TAG, "smoothScrollBy: mScrollY = " + this.mScrollY + ",distance = " + distance + ",mItemCount = " + this.mItemCount + ",childCount = " + childCount);
            }
            this.mFlingRunnable.endFling();
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
                return;
            }
            return;
        }
        reportScrollStateChange(2);
        this.mFlingRunnable.startScroll(distance, duration, interpolator, linear);
    }

    void smoothScrollByOffset(int position) {
        int index = -1;
        if (position < 0) {
            index = getFirstVisiblePosition();
        } else if (position > 0) {
            index = getLastVisiblePosition();
        }
        if (index > -1) {
            View child = getChildAt(index - getFirstVisiblePosition());
            if (child != null) {
                Rect visibleRect = new Rect();
                if (child.getGlobalVisibleRect(visibleRect)) {
                    float visibleArea = ((float) (visibleRect.width() * visibleRect.height())) / ((float) (child.getWidth() * child.getHeight()));
                    if (position < 0 && visibleArea < 0.75f) {
                        index++;
                    } else if (position > 0 && visibleArea < 0.75f) {
                        index--;
                    }
                }
                smoothScrollToPosition(Math.max(0, Math.min(getCount(), index + position)));
            }
        }
    }

    private void createScrollingCache() {
        if (this.mScrollingCacheEnabled && !this.mCachingStarted && !isHardwareAccelerated()) {
            setChildrenDrawnWithCacheEnabled(true);
            setChildrenDrawingCacheEnabled(true);
            this.mCachingActive = true;
            this.mCachingStarted = true;
        }
    }

    private void clearScrollingCache() {
        if (!isHardwareAccelerated()) {
            if (this.mClearScrollingCache == null) {
                this.mClearScrollingCache = new AnonymousClass4(this);
            }
            post(this.mClearScrollingCache);
        }
    }

    public void scrollListBy(int y) {
        trackMotionScroll(-y, -y);
    }

    public boolean canScrollList(int direction) {
        boolean z = true;
        int childCount = getChildCount();
        if (childCount == 0) {
            return false;
        }
        int firstPosition = this.mFirstPosition;
        Rect listPadding = this.mListPadding;
        if (direction > 0) {
            int lastBottom = getChildAt(childCount - 1).getBottom();
            if (firstPosition + childCount >= this.mItemCount && lastBottom <= getHeight() - listPadding.bottom) {
                z = false;
            }
            return z;
        }
        int firstTop = getChildAt(0).getTop();
        if (firstPosition <= 0 && firstTop >= listPadding.top) {
            z = false;
        }
        return z;
    }

    boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return true;
        }
        int firstTop = getChildAt(0).getTop();
        int lastBottom = getChildAt(childCount - 1).getBottom();
        Rect listPadding = this.mListPadding;
        int effectivePaddingTop = 0;
        int effectivePaddingBottom = 0;
        if ((this.mGroupFlags & 34) == 34) {
            effectivePaddingTop = listPadding.top;
            effectivePaddingBottom = listPadding.bottom;
        }
        int spaceAbove = effectivePaddingTop - firstTop;
        int spaceBelow = lastBottom - (getHeight() - effectivePaddingBottom);
        int height = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
        if (IS_ENG_BUILD && sDbgMotion) {
            Log.d(TAG, "trackMotionScroll: deltaY = " + deltaY + ",mScrollY = " + this.mScrollY + ",incrementalDeltaY = " + incrementalDeltaY + ",firstTop = " + firstTop + ",lastBottom = " + lastBottom + ",height = " + height + ",childCount = " + childCount + ",spaceAbove = " + spaceAbove + ",this = " + this);
        }
        if (deltaY < 0) {
            deltaY = Math.max(-(height - 1), deltaY);
        } else {
            deltaY = Math.min(height - 1, deltaY);
        }
        if (incrementalDeltaY < 0) {
            incrementalDeltaY = Math.max(-(height - 1), incrementalDeltaY);
        } else {
            incrementalDeltaY = Math.min(height - 1, incrementalDeltaY);
        }
        int firstPosition = this.mFirstPosition;
        if (firstPosition == 0) {
            this.mFirstPositionDistanceGuess = firstTop - listPadding.top;
        } else {
            this.mFirstPositionDistanceGuess += incrementalDeltaY;
        }
        if (firstPosition + childCount == this.mItemCount) {
            this.mLastPositionDistanceGuess = listPadding.bottom + lastBottom;
        } else {
            this.mLastPositionDistanceGuess += incrementalDeltaY;
        }
        boolean cannotScrollDown = (firstPosition != 0 || firstTop < listPadding.top) ? false : incrementalDeltaY >= 0;
        boolean cannotScrollUp = (firstPosition + childCount != this.mItemCount || lastBottom > getHeight() - listPadding.bottom) ? false : incrementalDeltaY <= 0;
        if (cannotScrollDown || cannotScrollUp) {
            boolean z;
            if (incrementalDeltaY != 0) {
                z = true;
            } else {
                z = false;
            }
            return z;
        }
        boolean down = incrementalDeltaY < 0;
        boolean inTouchMode = isInTouchMode();
        if (inTouchMode) {
            hideSelector();
        }
        int headerViewsCount = getHeaderViewsCount();
        int footerViewsStart = this.mItemCount - getFooterViewsCount();
        int start = 0;
        int count = 0;
        int i;
        View child;
        int position;
        if (!down) {
            int bottom = getHeight() - incrementalDeltaY;
            if ((this.mGroupFlags & 34) == 34) {
                bottom -= listPadding.bottom;
            }
            for (i = childCount - 1; i >= 0; i--) {
                child = getChildAt(i);
                if (child.getTop() <= bottom) {
                    break;
                }
                start = i;
                count++;
                position = firstPosition + i;
                if (position >= headerViewsCount && position < footerViewsStart) {
                    child.clearAccessibilityFocus();
                    this.mRecycler.addScrapView(child, position);
                }
            }
        } else {
            int top = -incrementalDeltaY;
            if ((this.mGroupFlags & 34) == 34) {
                top += listPadding.top;
            }
            for (i = 0; i < childCount; i++) {
                child = getChildAt(i);
                if (child.getBottom() >= top) {
                    break;
                }
                count++;
                position = firstPosition + i;
                if (position >= headerViewsCount && position < footerViewsStart) {
                    child.clearAccessibilityFocus();
                    this.mRecycler.addScrapView(child, position);
                }
            }
        }
        this.mMotionViewNewTop = this.mMotionViewOriginalTop + deltaY;
        this.mBlockLayoutRequests = true;
        if (count > 0) {
            detachViewsFromParent(start, count);
            this.mRecycler.removeSkippedScrap();
        }
        if (!awakenScrollBars()) {
            invalidate();
        }
        offsetChildrenTopAndBottom(incrementalDeltaY);
        if (down) {
            this.mFirstPosition += count;
        }
        int absIncrementalDeltaY = Math.abs(incrementalDeltaY);
        if (spaceAbove < absIncrementalDeltaY || spaceBelow < absIncrementalDeltaY) {
            fillGap(down);
        }
        this.mRecycler.fullyDetachScrapViews();
        int childIndex;
        if (!inTouchMode && this.mSelectedPosition != -1) {
            childIndex = this.mSelectedPosition - this.mFirstPosition;
            if (childIndex >= 0 && childIndex < getChildCount()) {
                positionSelector(this.mSelectedPosition, getChildAt(childIndex));
            }
        } else if (this.mSelectorPosition != -1) {
            childIndex = this.mSelectorPosition - this.mFirstPosition;
            if (childIndex >= 0 && childIndex < getChildCount()) {
                positionSelector(-1, getChildAt(childIndex));
            }
        } else {
            this.mSelectorRect.setEmpty();
            if (IS_ENG_BUILD && sDbgSelector) {
                Log.d(TAG, "mSelectorRect.setEmpty in trackMotionScroll this = " + this, new Throwable("trackMotionScroll"));
            }
        }
        this.mBlockLayoutRequests = false;
        invokeOnItemScrollListener();
        return false;
    }

    int getHeaderViewsCount() {
        return 0;
    }

    int getFooterViewsCount() {
        return 0;
    }

    void hideSelector() {
        if (this.mSelectedPosition != -1) {
            if (this.mLayoutMode != 4) {
                this.mResurrectToPosition = this.mSelectedPosition;
            }
            if (this.mNextSelectedPosition >= 0 && this.mNextSelectedPosition != this.mSelectedPosition) {
                this.mResurrectToPosition = this.mNextSelectedPosition;
            }
            setSelectedPositionInt(-1);
            setNextSelectedPositionInt(-1);
            this.mSelectedTop = 0;
        }
    }

    int reconcileSelectedPosition() {
        int position = this.mSelectedPosition;
        if (position < 0) {
            position = this.mResurrectToPosition;
        }
        return Math.min(Math.max(0, position), this.mItemCount - 1);
    }

    int findClosestMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return -1;
        }
        int motionRow = findMotionRow(y);
        if (motionRow == -1) {
            motionRow = (this.mFirstPosition + childCount) - 1;
        }
        return motionRow;
    }

    public void invalidateViews() {
        setDataChanged(true);
        rememberSyncState();
        requestLayout();
        invalidate();
    }

    boolean resurrectSelectionIfNeeded() {
        if (this.mSelectedPosition >= 0 || !resurrectSelection()) {
            return false;
        }
        updateSelectorState();
        return true;
    }

    boolean resurrectSelection() {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return false;
        }
        int selectedPos;
        boolean z;
        int selectedTop = 0;
        int childrenTop = this.mListPadding.top;
        int childrenBottom = (this.mBottom - this.mTop) - this.mListPadding.bottom;
        int firstPosition = this.mFirstPosition;
        int toPosition = this.mResurrectToPosition;
        boolean down = true;
        int i;
        int top;
        if (toPosition >= firstPosition && toPosition < firstPosition + childCount) {
            selectedPos = toPosition;
            View selected = getChildAt(toPosition - this.mFirstPosition);
            selectedTop = selected.getTop();
            int selectedBottom = selected.getBottom();
            if (selectedTop < childrenTop) {
                selectedTop = childrenTop + getVerticalFadingEdgeLength();
            } else if (selectedBottom > childrenBottom) {
                selectedTop = (childrenBottom - selected.getMeasuredHeight()) - getVerticalFadingEdgeLength();
            }
        } else if (toPosition < firstPosition) {
            selectedPos = firstPosition;
            for (i = 0; i < childCount; i++) {
                top = getChildAt(i).getTop();
                if (i == 0) {
                    selectedTop = top;
                    if (firstPosition > 0 || top < childrenTop) {
                        childrenTop += getVerticalFadingEdgeLength();
                    }
                }
                if (top >= childrenTop) {
                    selectedPos = firstPosition + i;
                    selectedTop = top;
                    break;
                }
            }
        } else {
            int itemCount = this.mItemCount;
            down = false;
            selectedPos = (firstPosition + childCount) - 1;
            for (i = childCount - 1; i >= 0; i--) {
                View v = getChildAt(i);
                top = v.getTop();
                int bottom = v.getBottom();
                if (i == childCount - 1) {
                    selectedTop = top;
                    if (firstPosition + childCount < itemCount || bottom > childrenBottom) {
                        childrenBottom -= getVerticalFadingEdgeLength();
                    }
                }
                if (bottom <= childrenBottom) {
                    selectedPos = firstPosition + i;
                    selectedTop = top;
                    break;
                }
            }
        }
        this.mResurrectToPosition = -1;
        removeCallbacks(this.mFlingRunnable);
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        this.mTouchMode = -1;
        clearScrollingCache();
        this.mSpecificTop = selectedTop;
        selectedPos = lookForSelectablePosition(selectedPos, down);
        if (selectedPos < firstPosition || selectedPos > getLastVisiblePosition()) {
            selectedPos = -1;
        } else {
            setListLayoutMode(4);
            updateSelectorState();
            setSelectionInt(selectedPos);
            invokeOnItemScrollListener();
        }
        reportScrollStateChange(0);
        if (selectedPos >= 0) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    void confirmCheckedPositionsById() {
        this.mCheckStates.clear();
        boolean checkedCountChanged = false;
        int checkedIndex = 0;
        while (checkedIndex < this.mCheckedIdStates.size()) {
            long id = this.mCheckedIdStates.keyAt(checkedIndex);
            int lastPos = ((Integer) this.mCheckedIdStates.valueAt(checkedIndex)).intValue();
            if (id != this.mAdapter.getItemId(lastPos)) {
                int start = Math.max(0, lastPos - 20);
                int end = Math.min(lastPos + 20, this.mItemCount);
                boolean found = false;
                for (int searchPos = start; searchPos < end; searchPos++) {
                    if (id == this.mAdapter.getItemId(searchPos)) {
                        found = true;
                        this.mCheckStates.put(searchPos, true);
                        this.mCheckedIdStates.setValueAt(checkedIndex, Integer.valueOf(searchPos));
                        break;
                    }
                }
                if (!found) {
                    this.mCheckedIdStates.delete(id);
                    checkedIndex--;
                    this.mCheckedItemCount--;
                    checkedCountChanged = true;
                    if (!(this.mChoiceActionMode == null || this.mMultiChoiceModeCallback == null)) {
                        this.mMultiChoiceModeCallback.onItemCheckedStateChanged(this.mChoiceActionMode, lastPos, id, false);
                    }
                }
            } else {
                this.mCheckStates.put(lastPos, true);
            }
            checkedIndex++;
        }
        if (checkedCountChanged && this.mChoiceActionMode != null) {
            this.mChoiceActionMode.invalidate();
        }
    }

    protected void handleDataChanged() {
        int i = 3;
        int count = this.mItemCount;
        int lastHandledItemCount = this.mLastHandledItemCount;
        this.mLastHandledItemCount = this.mItemCount;
        if (!(this.mChoiceMode == 0 || this.mAdapter == null || !this.mAdapter.hasStableIds())) {
            confirmCheckedPositionsById();
        }
        this.mRecycler.clearTransientStateViews();
        if (count > 0) {
            int newPos;
            if (this.mNeedSync) {
                this.mNeedSync = false;
                this.mPendingSync = null;
                if (this.mTranscriptMode == 2) {
                    setListLayoutMode(3);
                    return;
                }
                if (this.mTranscriptMode == 1) {
                    if (this.mForceTranscriptScroll) {
                        this.mForceTranscriptScroll = false;
                        setListLayoutMode(3);
                        return;
                    }
                    int childCount = getChildCount();
                    int listBottom = getHeight() - getPaddingBottom();
                    View lastChild = getChildAt(childCount - 1);
                    int lastBottom = lastChild != null ? lastChild.getBottom() : listBottom;
                    if (this.mFirstPosition + childCount < lastHandledItemCount || lastBottom > listBottom) {
                        awakenScrollBars();
                    } else {
                        setListLayoutMode(3);
                        return;
                    }
                }
                switch (this.mSyncMode) {
                    case 0:
                        if (isInTouchMode()) {
                            setListLayoutMode(5);
                            setSyncPosition(Math.min(Math.max(0, this.mSyncPosition), count - 1));
                            return;
                        }
                        newPos = findSyncPosition();
                        if (newPos >= 0 && lookForSelectablePosition(newPos, true) == newPos) {
                            setSyncPosition(newPos);
                            if (this.mSyncHeight == ((long) getHeight())) {
                                setListLayoutMode(5);
                            } else {
                                setListLayoutMode(2);
                            }
                            setNextSelectedPositionInt(newPos);
                            return;
                        }
                    case 1:
                        setListLayoutMode(5);
                        setSyncPosition(Math.min(Math.max(0, this.mSyncPosition), count - 1));
                        return;
                }
            }
            if (!isInTouchMode()) {
                newPos = getSelectedItemPosition();
                if (newPos >= count) {
                    newPos = count - 1;
                }
                if (newPos < 0) {
                    newPos = 0;
                }
                int selectablePos = lookForSelectablePosition(newPos, true);
                if (selectablePos >= 0) {
                    setNextSelectedPositionInt(selectablePos);
                    return;
                }
                selectablePos = lookForSelectablePosition(newPos, false);
                if (selectablePos >= 0) {
                    setNextSelectedPositionInt(selectablePos);
                    return;
                }
            } else if (this.mResurrectToPosition >= 0) {
                return;
            }
        }
        if (!this.mStackFromBottom) {
            i = 1;
        }
        setListLayoutMode(i);
        this.mSelectedPosition = -1;
        this.mSelectedRowId = Long.MIN_VALUE;
        this.mNextSelectedPosition = -1;
        this.mNextSelectedRowId = Long.MIN_VALUE;
        this.mNeedSync = false;
        this.mPendingSync = null;
        this.mSelectorPosition = -1;
        checkSelectionChanged();
    }

    protected void onDisplayHint(int hint) {
        boolean z;
        super.onDisplayHint(hint);
        switch (hint) {
            case 0:
                if (!(!this.mFiltered || this.mPopup == null || this.mPopup.isShowing())) {
                    showPopup();
                    break;
                }
            case 4:
                if (this.mPopup != null && this.mPopup.isShowing()) {
                    dismissPopup();
                    break;
                }
        }
        if (hint == 4) {
            z = true;
        } else {
            z = false;
        }
        this.mPopupHidden = z;
    }

    private void dismissPopup() {
        if (this.mPopup != null) {
            this.mPopup.dismiss();
        }
    }

    private void showPopup() {
        if (getWindowVisibility() == 0) {
            createTextFilter(true);
            positionPopup();
            checkFocus();
        }
    }

    private void positionPopup() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int[] xy = new int[2];
        getLocationOnScreen(xy);
        int bottomGap = ((screenHeight - xy[1]) - getHeight()) + ((int) (this.mDensityScale * 20.0f));
        if (this.mPopup.isShowing()) {
            this.mPopup.update(xy[0], bottomGap, -1, -1);
        } else {
            this.mPopup.showAtLocation((View) this, 81, xy[0], bottomGap);
        }
    }

    static int getDistance(Rect source, Rect dest, int direction) {
        int sX;
        int sY;
        int dX;
        int dY;
        switch (direction) {
            case 1:
            case 2:
                sX = source.right + (source.width() / 2);
                sY = source.top + (source.height() / 2);
                dX = dest.left + (dest.width() / 2);
                dY = dest.top + (dest.height() / 2);
                break;
            case 17:
                sX = source.left;
                sY = source.top + (source.height() / 2);
                dX = dest.right;
                dY = dest.top + (dest.height() / 2);
                break;
            case 33:
                sX = source.left + (source.width() / 2);
                sY = source.top;
                dX = dest.left + (dest.width() / 2);
                dY = dest.bottom;
                break;
            case 66:
                sX = source.right;
                sY = source.top + (source.height() / 2);
                dX = dest.left;
                dY = dest.top + (dest.height() / 2);
                break;
            case 130:
                sX = source.left + (source.width() / 2);
                sY = source.bottom;
                dX = dest.left + (dest.width() / 2);
                dY = dest.top;
                break;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
        }
        int deltaX = dX - sX;
        int deltaY = dY - sY;
        return (deltaY * deltaY) + (deltaX * deltaX);
    }

    protected boolean isInFilterMode() {
        return this.mFiltered;
    }

    boolean sendToTextFilter(int keyCode, int count, KeyEvent event) {
        if (!acceptFilter()) {
            return false;
        }
        boolean handled = false;
        boolean okToSend = true;
        switch (keyCode) {
            case 4:
                if (this.mFiltered && this.mPopup != null && this.mPopup.isShowing()) {
                    if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                        DispatcherState state = getKeyDispatcherState();
                        if (state != null) {
                            state.startTracking(event, this);
                        }
                        handled = true;
                    } else if (event.getAction() == 1 && event.isTracking() && !event.isCanceled()) {
                        handled = true;
                        this.mTextFilter.setText(PhoneConstants.MVNO_TYPE_NONE);
                    }
                }
                okToSend = false;
                break;
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 66:
                okToSend = false;
                break;
            case 62:
                okToSend = this.mFiltered;
                break;
        }
        if (okToSend) {
            createTextFilter(true);
            KeyEvent forwardEvent = event;
            if (event.getRepeatCount() > 0) {
                forwardEvent = KeyEvent.changeTimeRepeat(event, event.getEventTime(), 0);
            }
            switch (event.getAction()) {
                case 0:
                    handled = this.mTextFilter.onKeyDown(keyCode, forwardEvent);
                    break;
                case 1:
                    handled = this.mTextFilter.onKeyUp(keyCode, forwardEvent);
                    break;
                case 2:
                    handled = this.mTextFilter.onKeyMultiple(keyCode, count, event);
                    break;
            }
        }
        return handled;
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (!isTextFilterEnabled()) {
            return null;
        }
        if (this.mPublicInputConnection == null) {
            this.mDefInputConnection = new BaseInputConnection((View) this, false);
            this.mPublicInputConnection = new InputConnectionWrapper(this, outAttrs);
        }
        outAttrs.inputType = 177;
        outAttrs.imeOptions = 6;
        return this.mPublicInputConnection;
    }

    public boolean checkInputConnectionProxy(View view) {
        return view == this.mTextFilter;
    }

    private void createTextFilter(boolean animateEntrance) {
        if (this.mPopup == null) {
            PopupWindow p = new PopupWindow(getContext());
            p.setFocusable(false);
            p.setTouchable(false);
            p.setInputMethodMode(2);
            p.setContentView(getTextFilterInput());
            p.setWidth(-2);
            p.setHeight(-2);
            p.setBackgroundDrawable(null);
            this.mPopup = p;
            getViewTreeObserver().addOnGlobalLayoutListener(this);
            this.mGlobalLayoutListenerAddedFilter = true;
        }
        if (animateEntrance) {
            this.mPopup.setAnimationStyle(R.style.Animation_TypingFilter);
        } else {
            this.mPopup.setAnimationStyle(R.style.Animation_TypingFilterRestore);
        }
    }

    private EditText getTextFilterInput() {
        if (this.mTextFilter == null) {
            this.mTextFilter = (EditText) LayoutInflater.from(getContext()).inflate((int) R.layout.typing_filter, null);
            this.mTextFilter.setRawInputType(177);
            this.mTextFilter.setImeOptions(268435456);
            this.mTextFilter.addTextChangedListener(this);
        }
        return this.mTextFilter;
    }

    public void clearTextFilter() {
        if (this.mFiltered) {
            getTextFilterInput().setText(PhoneConstants.MVNO_TYPE_NONE);
            this.mFiltered = false;
            if (this.mPopup != null && this.mPopup.isShowing()) {
                dismissPopup();
            }
        }
    }

    public boolean hasTextFilter() {
        return this.mFiltered;
    }

    public void onGlobalLayout() {
        if (isShown()) {
            if (this.mFiltered && this.mPopup != null && !this.mPopup.isShowing() && !this.mPopupHidden) {
                showPopup();
            }
        } else if (this.mPopup != null && this.mPopup.isShowing()) {
            dismissPopup();
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isTextFilterEnabled()) {
            createTextFilter(true);
            int length = s.length();
            boolean showing = this.mPopup.isShowing();
            if (!showing && length > 0) {
                showPopup();
                this.mFiltered = true;
            } else if (showing && length == 0) {
                dismissPopup();
                this.mFiltered = false;
            }
            if (this.mAdapter instanceof Filterable) {
                Filter f = ((Filterable) this.mAdapter).getFilter();
                if (f != null) {
                    f.filter(s, this);
                    return;
                }
                throw new IllegalStateException("You cannot call onTextChanged with a non filterable adapter");
            }
        }
    }

    public void afterTextChanged(Editable s) {
    }

    public void onFilterComplete(int count) {
        if (this.mSelectedPosition < 0 && count > 0) {
            this.mResurrectToPosition = -1;
            resurrectSelection();
        }
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -2, 0);
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public /* bridge */ /* synthetic */ android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return generateLayoutParams(attrs);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public void setTranscriptMode(int mode) {
        this.mTranscriptMode = mode;
    }

    public int getTranscriptMode() {
        return this.mTranscriptMode;
    }

    public int getSolidColor() {
        return this.mCacheColorHint;
    }

    public void setCacheColorHint(int color) {
        if (color != this.mCacheColorHint) {
            this.mCacheColorHint = color;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).setDrawingCacheBackgroundColor(color);
            }
            this.mRecycler.setCacheColorHint(color);
        }
    }

    @ExportedProperty(category = "drawing")
    public int getCacheColorHint() {
        return this.mCacheColorHint;
    }

    public void reclaimViews(List<View> views) {
        int childCount = getChildCount();
        RecyclerListener listener = this.mRecycler.mRecyclerListener;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp != null && this.mRecycler.shouldRecycleViewType(lp.viewType)) {
                views.add(child);
                child.setAccessibilityDelegate(null);
                if (listener != null) {
                    listener.onMovedToScrapHeap(child);
                }
            }
        }
        this.mRecycler.reclaimScrapViews(views);
        removeAllViewsInLayout();
    }

    private void finishGlows() {
        if (this.mEdgeGlowTop != null) {
            this.mEdgeGlowTop.finish();
            this.mEdgeGlowBottom.finish();
        }
    }

    public void setRemoteViewsAdapter(Intent intent) {
        if (this.mRemoteAdapter == null || !new FilterComparison(intent).equals(new FilterComparison(this.mRemoteAdapter.getRemoteViewsServiceIntent()))) {
            this.mDeferNotifyDataSetChanged = false;
            this.mRemoteAdapter = new RemoteViewsAdapter(getContext(), intent, this);
            if (this.mRemoteAdapter.isDataReady()) {
                setAdapter(this.mRemoteAdapter);
            }
        }
    }

    public void setRemoteViewsOnClickHandler(OnClickHandler handler) {
        if (this.mRemoteAdapter != null) {
            this.mRemoteAdapter.setRemoteViewsOnClickHandler(handler);
        }
    }

    public void deferNotifyDataSetChanged() {
        this.mDeferNotifyDataSetChanged = true;
    }

    public boolean onRemoteAdapterConnected() {
        if (this.mRemoteAdapter != this.mAdapter) {
            setAdapter(this.mRemoteAdapter);
            if (this.mDeferNotifyDataSetChanged) {
                this.mRemoteAdapter.notifyDataSetChanged();
                this.mDeferNotifyDataSetChanged = false;
            }
            return false;
        } else if (this.mRemoteAdapter == null) {
            return false;
        } else {
            this.mRemoteAdapter.superNotifyDataSetChanged();
            return true;
        }
    }

    public void onRemoteAdapterDisconnected() {
    }

    void setVisibleRangeHint(int start, int end) {
        if (this.mRemoteAdapter != null) {
            this.mRemoteAdapter.setVisibleRangeHint(start, end);
        }
    }

    public void setRecyclerListener(RecyclerListener listener) {
        this.mRecycler.mRecyclerListener = listener;
    }

    int getHeightForPosition(int position) {
        int firstVisiblePosition = getFirstVisiblePosition();
        int childCount = getChildCount();
        int index = position - firstVisiblePosition;
        if (index >= 0 && index < childCount) {
            return getChildAt(index).getHeight();
        }
        View view = obtainView(position, this.mIsScrap);
        view.measure(this.mWidthMeasureSpec, 0);
        int height = view.getMeasuredHeight();
        this.mRecycler.addScrapView(view, position);
        return height;
    }

    public void setSelectionFromTop(int position, int y) {
        if (this.mAdapter != null) {
            if (isInTouchMode()) {
                this.mResurrectToPosition = position;
            } else {
                position = lookForSelectablePosition(position, true);
                if (position >= 0) {
                    setNextSelectedPositionInt(position);
                }
            }
            if (position >= 0) {
                this.mLayoutMode = 4;
                this.mSpecificTop = this.mListPadding.top + y;
                if (this.mNeedSync) {
                    this.mSyncPosition = position;
                    this.mSyncRowId = this.mAdapter.getItemId(position);
                }
                if (this.mPositionScroller != null) {
                    this.mPositionScroller.stop();
                }
                requestLayout();
            }
        }
    }

    protected void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("drawing:cacheColorHint", getCacheColorHint());
        encoder.addProperty("list:fastScrollEnabled", isFastScrollEnabled());
        encoder.addProperty("list:scrollingCacheEnabled", isScrollingCacheEnabled());
        encoder.addProperty("list:smoothScrollbarEnabled", isSmoothScrollbarEnabled());
        encoder.addProperty("list:stackFromBottom", isStackFromBottom());
        encoder.addProperty("list:textFilterEnabled", isTextFilterEnabled());
        View selectedView = getSelectedView();
        if (selectedView != null) {
            encoder.addPropertyKey("selectedView");
            selectedView.encode(encoder);
        }
    }

    boolean setDataChanged(boolean changed) {
        if (IS_ENG_BUILD && sDbgDataChange) {
            Log.d(TAG, "setDataChanged oldDataChanged=" + this.mDataChanged + ", mDataChanged=" + changed + ", mOldItemCount=" + this.mOldItemCount + ", mItemCount=" + this.mItemCount + ", countFromAdapter=" + (this.mAdapter != null ? this.mAdapter.getCount() : -1) + ", mAdapter=" + this.mAdapter + ", this=" + this + ", callstack=" + Debug.getCallers(10));
        }
        return super.setDataChanged(changed);
    }

    boolean setListLayoutMode(int mode) {
        if (IS_ENG_BUILD && sDbgLayout) {
            Log.d(TAG, "setListLayoutMode oldLayoutMode=" + this.mLayoutMode + ", mLayoutMode=" + mode + ", mSpecificTop=" + this.mSpecificTop + ", mSyncPosition=" + this.mSyncPosition + ", mSyncRowId=" + this.mSyncRowId + ", mSyncHeight=" + this.mSyncHeight + ", mNeedSync=" + this.mNeedSync + ", mSyncMode=" + this.mSyncMode + ", mResurrectToPosition=" + this.mResurrectToPosition + ", mSelectedTop=" + this.mSelectedTop + ", mAdapter=" + this.mAdapter + ", this=" + this + ", callstack=" + Debug.getCallers(10));
        }
        boolean ret = this.mLayoutMode != mode;
        this.mLayoutMode = mode;
        return ret;
    }

    boolean setSyncPosition(int pos) {
        if (IS_ENG_BUILD && sDbgLayout) {
            Log.d(TAG, "setSyncPosition oldSyncPosition=" + this.mSyncPosition + ", mSyncPosition=" + pos + ", mSpecificTop=" + this.mSpecificTop + ", mSyncRowId=" + this.mSyncRowId + ", mSyncHeight=" + this.mSyncHeight + ", mNeedSync=" + this.mNeedSync + ", mSyncMode=" + this.mSyncMode + ", this=" + this + ", callstack=" + Debug.getCallers(10));
        }
        return super.setSyncPosition(pos);
    }

    private static void checkAbsListViewlLogProperty() {
        boolean z = true;
        String dumpString = SystemProperties.get(LOG_PROPERTY_NAME);
        if (dumpString != null) {
            if (dumpString.length() <= 0 || dumpString.length() > 7) {
                Log.d(TAG, "checkAbsListViewlLogProperty get invalid command");
                return;
            }
            boolean z2;
            int logFilter = 0;
            try {
                logFilter = Integer.parseInt(dumpString, 2);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid format of propery string: " + dumpString);
            }
            if ((logFilter & 1) == 1) {
                z2 = true;
            } else {
                z2 = false;
            }
            sDbgMotion = z2;
            if ((logFilter & 2) == 2) {
                z2 = true;
            } else {
                z2 = false;
            }
            sDbgDataChange = z2;
            if ((logFilter & 4) == 4) {
                z2 = true;
            } else {
                z2 = false;
            }
            sDbgLayout = z2;
            if ((logFilter & 8) == 8) {
                z2 = true;
            } else {
                z2 = false;
            }
            sDbgRecycle = z2;
            if ((logFilter & 16) == 16) {
                z2 = true;
            } else {
                z2 = false;
            }
            sDbgDraw = z2;
            if ((logFilter & 32) == 32) {
                z2 = true;
            } else {
                z2 = false;
            }
            sDbg = z2;
            if ((logFilter & 64) != 64) {
                z = false;
            }
            sDbgSelector = z;
            Log.d(TAG, "checkAbsListViewlLogProperty debug filter: sDbgMotion=" + sDbgMotion + ", sDbgDataChange=" + sDbgDataChange + ", sDbgLayout=" + sDbgLayout + ", sDbgRecycle=" + sDbgRecycle + ", sDbgDraw=" + sDbgDraw + ", sDbg=" + sDbg + ", sDbgSelector=" + sDbgSelector);
        }
    }

    protected void destroyHardwareResources() {
        super.destroyHardwareResources();
        this.mRecycler.clear();
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "LiangJing.Fu@Plf.SDK : Add for initialization oppo property", property = OppoRomType.ROM)
    public void initOppoProperty(Context context) {
        if (isColorStyle()) {
            int i = context.getResources().getDisplayMetrics().heightPixels;
            this.mOverscrollDistance = i;
            this.mOverflingDistance = i;
        }
        if (isOppoStyle()) {
            this.mIgnoreLeft = context.getResources().getDimensionPixelSize(201655492);
            this.mIgnoreRight = context.getResources().getDimensionPixelSize(201655493);
        }
        if (context.getResources().getBoolean(202114059)) {
            setBackground(context.getResources().getDrawable(201850884));
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YaoJun.Luo@Plf.SDK : Add for oppo property mOverflingDistance and mOverscrollDistance add a scale", property = OppoRomType.ROM)
    public void initScaleForOverDistance(Context context, float scale) {
        if (isColorStyle()) {
            if (this.mOverScale != scale) {
                this.mOverScale = scale;
            }
            this.mOverflingDistance = (int) (((float) this.mOverflingDistance) * this.mOverScale);
            this.mOverscrollDistance = (int) (((float) this.mOverscrollDistance) * this.mOverScale);
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "LiangJing.Fu@Plf.SDK : Add for re-calculate the overscroll distance,XiaoKang.Feng@Plf.SDK : Modify for oppoStyle", property = OppoRomType.ROM)
    private int calcRealOverScrollDist(int dist) {
        if (isColorStyle()) {
            return (int) ((((float) dist) * (1.0f - ((((float) Math.abs(this.mScrollY)) * 1.0f) / ((float) this.mOverscrollDistance)))) / 3.0f);
        }
        return dist;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "JinPeng@Plf.SDK, add for scroll when touch blank area", property = OppoRomType.ROM)
    private void handleTouchBlankArea(MotionEvent ev, int touchMode, int motionPosition) {
        if (touchMode != 4 && motionPosition == -1 && isColorStyle()) {
            this.mMotionX = (int) ev.getX();
            this.mMotionY = (int) ev.getY();
            this.mTouchMode = 0;
            clearScrollingCache();
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "Hairong.Zou@Plf.SDK : Add for ListView back to top", property = OppoRomType.ROM)
    public void startFlingToTop() {
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable(this);
        }
        this.mFlingRunnable.start(-4500);
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK : Add for get TouchMode", property = OppoRomType.ROM)
    public int getTouchMode() {
        return this.mTouchMode;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK : Add for set TouchMode", property = OppoRomType.ROM)
    public void setTouchMode(int mode) {
        this.mTouchMode = mode;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK : Add for prevent touch", property = OppoRomType.ROM)
    private boolean isInIgnoreArea(int x) {
        return x < this.mIgnoreLeft || x > getWidth() - this.mIgnoreRight;
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK : Add for prevent touch", property = OppoRomType.ROM)
    private void initDown(MotionEvent ev) {
        this.mIsMutiPoint = false;
        this.mDownX = (int) ev.getX();
        if (isInIgnoreArea(this.mDownX)) {
            this.mHasIgnore = true;
        } else {
            this.mHasIgnore = false;
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "XiaoKang.Feng@Plf.SDK : Add for prevent touch", property = OppoRomType.ROM)
    private void reSetDownEvent() {
        this.mIsMutiPoint = false;
        this.mPointDownX = 0;
        this.mDownX = 0;
    }

    public void colorStartSpringback() {
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new FlingRunnable(this);
        }
        this.mFlingRunnable.startSpringback();
    }
}
