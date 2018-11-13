package android.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.IntProperty;
import android.util.MathUtils;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroupOverlay;
import android.widget.ImageView.ScaleType;
import com.android.internal.R;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class FastScroller {
    private static Property<View, Integer> BOTTOM = null;
    private static final int COLOR_MIN_PAGES = 3;
    private static final int COLOR_STATE_HIDE = 3;
    private static final int DURATION_CROSS_FADE = 50;
    private static final int DURATION_FADE_IN = 150;
    private static final int DURATION_FADE_OUT = 300;
    private static final int DURATION_RESIZE = 100;
    private static final long FADE_TIMEOUT = 1500;
    private static Property<View, Integer> LEFT = null;
    private static final int MIN_PAGES = 4;
    private static final int OVERLAY_ABOVE_THUMB = 2;
    private static final int OVERLAY_AT_THUMB = 1;
    private static final int OVERLAY_FLOATING = 0;
    private static final int PREVIEW_LEFT = 0;
    private static final int PREVIEW_RIGHT = 1;
    private static Property<View, Integer> RIGHT = null;
    private static final int STATE_DRAGGING = 2;
    private static final int STATE_NONE = 0;
    private static final int STATE_VISIBLE = 1;
    private static final String TAG = "FastScroller";
    private static final long TAP_TIMEOUT = 0;
    private static final int THUMB_POSITION_INSIDE = 1;
    private static final int THUMB_POSITION_MIDPOINT = 0;
    private static Property<View, Integer> TOP;
    private boolean mAlwaysShow;
    private final Rect mContainerRect;
    private int mCurrentSection;
    private AnimatorSet mDecorAnimation;
    private final Runnable mDeferHide;
    private boolean mEnabled;
    private int mFirstVisibleItem;
    private int mHeaderCount;
    private float mInitialTouchY;
    private boolean mIsOppoStyle;
    private boolean mLayoutFromRight;
    private final AbsListView mList;
    private Adapter mListAdapter;
    private boolean mLongList;
    private boolean mMatchDragPosition;
    private final int mMinimumTouchTarget;
    private int mOldChildCount;
    private int mOldItemCount;
    private final ViewGroupOverlay mOverlay;
    private int mOverlayPosition;
    private long mPendingDrag;
    private AnimatorSet mPreviewAnimation;
    private final View mPreviewImage;
    private int mPreviewMinHeight;
    private int mPreviewMinWidth;
    private int mPreviewPadding;
    private final int[] mPreviewResId;
    private final TextView mPrimaryText;
    private int mScaledTouchSlop;
    private int mScrollBarStyle;
    private boolean mScrollCompleted;
    private int mScrollbarPosition;
    private final TextView mSecondaryText;
    private SectionIndexer mSectionIndexer;
    private Object[] mSections;
    private boolean mShowingPreview;
    private boolean mShowingPrimary;
    private int mState;
    private final AnimatorListener mSwitchPrimaryListener;
    private final Rect mTempBounds;
    private final Rect mTempMargins;
    private int mTextAppearance;
    private ColorStateList mTextColor;
    private float mTextSize;
    private Drawable mThumbDrawable;
    private final ImageView mThumbImage;
    private int mThumbMinHeight;
    private int mThumbMinWidth;
    private float mThumbOffset;
    private int mThumbPosition;
    private float mThumbRange;
    private Drawable mTrackDrawable;
    private final ImageView mTrackImage;
    private boolean mUpdatingLayout;
    private int mWidth;

    /* renamed from: android.widget.FastScroller$3 */
    static class AnonymousClass3 extends IntProperty<View> {
        AnonymousClass3(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, int value) {
            object.setLeft(value);
        }

        public Integer get(View object) {
            return Integer.valueOf(object.getLeft());
        }
    }

    /* renamed from: android.widget.FastScroller$4 */
    static class AnonymousClass4 extends IntProperty<View> {
        AnonymousClass4(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, int value) {
            object.setTop(value);
        }

        public Integer get(View object) {
            return Integer.valueOf(object.getTop());
        }
    }

    /* renamed from: android.widget.FastScroller$5 */
    static class AnonymousClass5 extends IntProperty<View> {
        AnonymousClass5(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, int value) {
            object.setRight(value);
        }

        public Integer get(View object) {
            return Integer.valueOf(object.getRight());
        }
    }

    /* renamed from: android.widget.FastScroller$6 */
    static class AnonymousClass6 extends IntProperty<View> {
        AnonymousClass6(String $anonymous0) {
            super($anonymous0);
        }

        public void setValue(View object, int value) {
            object.setBottom(value);
        }

        public Integer get(View object) {
            return Integer.valueOf(object.getBottom());
        }
    }

    /* renamed from: android.widget.FastScroller$7 */
    class AnonymousClass7 extends AnimatorListenerAdapter {
        final /* synthetic */ FastScroller this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.FastScroller.7.<init>(android.widget.FastScroller):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass7(android.widget.FastScroller r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.FastScroller.7.<init>(android.widget.FastScroller):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.FastScroller.7.<init>(android.widget.FastScroller):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.FastScroller.7.onAnimationEnd(android.animation.Animator):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onAnimationEnd(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.FastScroller.7.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.FastScroller.7.onAnimationEnd(android.animation.Animator):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.FastScroller.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.FastScroller.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.FastScroller.<clinit>():void");
    }

    public FastScroller(AbsListView listView, int styleResId) {
        boolean z = true;
        this.mTempBounds = new Rect();
        this.mTempMargins = new Rect();
        this.mContainerRect = new Rect();
        this.mPreviewResId = new int[2];
        this.mCurrentSection = -1;
        this.mScrollbarPosition = -1;
        this.mPendingDrag = -1;
        this.mDeferHide = new Runnable() {
            public void run() {
                if (FastScroller.this.mIsOppoStyle) {
                    FastScroller.this.setState(3);
                } else {
                    FastScroller.this.setState(0);
                }
            }
        };
        this.mSwitchPrimaryListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                FastScroller.this.mShowingPrimary = !FastScroller.this.mShowingPrimary;
            }
        };
        this.mList = listView;
        this.mOldItemCount = listView.getCount();
        this.mOldChildCount = listView.getChildCount();
        Context context = listView.getContext();
        this.mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mScrollBarStyle = listView.getScrollBarStyle();
        this.mScrollCompleted = true;
        this.mState = 1;
        if (context.getApplicationInfo().targetSdkVersion < 11) {
            z = false;
        }
        this.mMatchDragPosition = z;
        this.mTrackImage = new ImageView(context);
        this.mTrackImage.setScaleType(ScaleType.FIT_XY);
        this.mThumbImage = new ImageView(context);
        this.mThumbImage.setScaleType(ScaleType.FIT_XY);
        this.mPreviewImage = new View(context);
        this.mIsOppoStyle = context.isOppoStyle();
        if (this.mIsOppoStyle) {
            this.mThumbImage.setAlpha(0.0f);
        }
        this.mPreviewImage.setAlpha(0.0f);
        this.mPrimaryText = createPreviewTextView(context);
        this.mSecondaryText = createPreviewTextView(context);
        this.mMinimumTouchTarget = listView.getResources().getDimensionPixelSize(R.dimen.fast_scroller_minimum_touch_target);
        setStyle(styleResId);
        ViewGroupOverlay overlay = listView.getOverlay();
        this.mOverlay = overlay;
        overlay.add(this.mTrackImage);
        overlay.add(this.mThumbImage);
        overlay.add(this.mPreviewImage);
        overlay.add(this.mPrimaryText);
        overlay.add(this.mSecondaryText);
        getSectionsFromIndexer();
        updateLongList(this.mOldChildCount, this.mOldItemCount);
        setScrollbarPosition(listView.getVerticalScrollbarPosition());
        postAutoHide();
    }

    private void updateAppearance() {
        int width = 0;
        this.mTrackImage.setImageDrawable(this.mTrackDrawable);
        if (this.mTrackDrawable != null) {
            width = Math.max(0, this.mTrackDrawable.getIntrinsicWidth());
        }
        this.mThumbImage.setImageDrawable(this.mThumbDrawable);
        this.mThumbImage.setMinimumWidth(this.mThumbMinWidth);
        this.mThumbImage.setMinimumHeight(this.mThumbMinHeight);
        if (this.mThumbDrawable != null) {
            width = Math.max(width, this.mThumbDrawable.getIntrinsicWidth());
        }
        this.mWidth = Math.max(width, this.mThumbMinWidth);
        if (this.mTextAppearance != 0) {
            this.mPrimaryText.setTextAppearance(this.mTextAppearance);
            this.mSecondaryText.setTextAppearance(this.mTextAppearance);
        }
        if (this.mTextColor != null) {
            this.mPrimaryText.setTextColor(this.mTextColor);
            this.mSecondaryText.setTextColor(this.mTextColor);
        }
        if (this.mTextSize > 0.0f) {
            this.mPrimaryText.setTextSize(0, this.mTextSize);
            this.mSecondaryText.setTextSize(0, this.mTextSize);
        }
        int padding = this.mPreviewPadding;
        this.mPrimaryText.setIncludeFontPadding(false);
        this.mPrimaryText.setPadding(padding, padding, padding, padding);
        this.mSecondaryText.setIncludeFontPadding(false);
        this.mSecondaryText.setPadding(padding, padding, padding, padding);
        refreshDrawablePressedState();
    }

    public void setStyle(int resId) {
        TypedArray ta = this.mList.getContext().obtainStyledAttributes(null, R.styleable.FastScroll, R.attr.fastScrollStyle, resId);
        int N = ta.getIndexCount();
        for (int i = 0; i < N; i++) {
            int index = ta.getIndex(i);
            switch (index) {
                case 0:
                    this.mTextAppearance = ta.getResourceId(index, 0);
                    break;
                case 1:
                    this.mTextSize = (float) ta.getDimensionPixelSize(index, 0);
                    break;
                case 2:
                    this.mTextColor = ta.getColorStateList(index);
                    break;
                case 3:
                    this.mPreviewPadding = ta.getDimensionPixelSize(index, 0);
                    break;
                case 4:
                    this.mPreviewMinWidth = ta.getDimensionPixelSize(index, 0);
                    break;
                case 5:
                    this.mPreviewMinHeight = ta.getDimensionPixelSize(index, 0);
                    break;
                case 6:
                    this.mThumbPosition = ta.getInt(index, 0);
                    break;
                case 7:
                    this.mThumbDrawable = ta.getDrawable(index);
                    break;
                case 8:
                    this.mThumbMinWidth = ta.getDimensionPixelSize(index, 0);
                    break;
                case 9:
                    this.mThumbMinHeight = ta.getDimensionPixelSize(index, 0);
                    break;
                case 10:
                    this.mTrackDrawable = ta.getDrawable(index);
                    break;
                case 11:
                    this.mPreviewResId[1] = ta.getResourceId(index, 0);
                    break;
                case 12:
                    this.mPreviewResId[0] = ta.getResourceId(index, 0);
                    break;
                case 13:
                    this.mOverlayPosition = ta.getInt(index, 0);
                    break;
                default:
                    break;
            }
        }
        ta.recycle();
        updateAppearance();
    }

    public void remove() {
        this.mOverlay.remove(this.mTrackImage);
        this.mOverlay.remove(this.mThumbImage);
        this.mOverlay.remove(this.mPreviewImage);
        this.mOverlay.remove(this.mPrimaryText);
        this.mOverlay.remove(this.mSecondaryText);
    }

    public void setEnabled(boolean enabled) {
        if (this.mEnabled != enabled) {
            this.mEnabled = enabled;
            onStateDependencyChanged(true);
        }
    }

    public boolean isEnabled() {
        if (this.mEnabled) {
            return !this.mLongList ? this.mAlwaysShow : true;
        } else {
            return false;
        }
    }

    public void setAlwaysShow(boolean alwaysShow) {
        if (this.mAlwaysShow != alwaysShow) {
            this.mAlwaysShow = alwaysShow;
            onStateDependencyChanged(false);
        }
    }

    public boolean isAlwaysShowEnabled() {
        return this.mAlwaysShow;
    }

    private void onStateDependencyChanged(boolean peekIfEnabled) {
        if (!isEnabled()) {
            stop();
        } else if (isAlwaysShowEnabled()) {
            setState(1);
        } else if (this.mState == 1) {
            postAutoHide();
        } else if (peekIfEnabled) {
            setState(1);
            postAutoHide();
        }
        this.mList.resolvePadding();
    }

    public void setScrollBarStyle(int style) {
        if (this.mScrollBarStyle != style) {
            this.mScrollBarStyle = style;
            updateLayout();
        }
    }

    public void stop() {
        setState(0);
    }

    public void setScrollbarPosition(int position) {
        int i = 1;
        if (position == 0) {
            position = this.mList.isLayoutRtl() ? 1 : 2;
        }
        if (this.mScrollbarPosition != position) {
            boolean z;
            this.mScrollbarPosition = position;
            if (position != 1) {
                z = true;
            } else {
                z = false;
            }
            this.mLayoutFromRight = z;
            int[] iArr = this.mPreviewResId;
            if (!this.mLayoutFromRight) {
                i = 0;
            }
            this.mPreviewImage.setBackgroundResource(iArr[i]);
            int textMinWidth = Math.max(0, (this.mPreviewMinWidth - this.mPreviewImage.getPaddingLeft()) - this.mPreviewImage.getPaddingRight());
            this.mPrimaryText.setMinimumWidth(textMinWidth);
            this.mSecondaryText.setMinimumWidth(textMinWidth);
            int textMinHeight = Math.max(0, (this.mPreviewMinHeight - this.mPreviewImage.getPaddingTop()) - this.mPreviewImage.getPaddingBottom());
            this.mPrimaryText.setMinimumHeight(textMinHeight);
            this.mSecondaryText.setMinimumHeight(textMinHeight);
            updateLayout();
        }
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateLayout();
    }

    public void onItemCountChanged(int childCount, int itemCount) {
        boolean hasMoreItems = false;
        if (this.mOldItemCount != itemCount || (this.mOldChildCount != childCount && !this.mIsOppoStyle)) {
            this.mOldItemCount = itemCount;
            this.mOldChildCount = childCount;
            if (itemCount - childCount > 0) {
                hasMoreItems = true;
            }
            if (hasMoreItems && this.mState != 2) {
                setThumbPos(getPosFromItemCount(this.mList.getFirstVisiblePosition(), childCount, itemCount));
            }
            updateLongList(childCount, itemCount);
        }
    }

    private void updateLongList(int childCount, int itemCount) {
        boolean longList = this.mIsOppoStyle ? childCount > 0 && itemCount / childCount >= 3 : childCount > 0 && itemCount / childCount >= 4;
        if (this.mLongList != longList) {
            this.mLongList = longList;
            onStateDependencyChanged(false);
        }
    }

    private TextView createPreviewTextView(Context context) {
        LayoutParams params = new LayoutParams(-2, -2);
        TextView textView = new TextView(context);
        textView.setLayoutParams(params);
        textView.setSingleLine(true);
        textView.setEllipsize(TruncateAt.MIDDLE);
        textView.setGravity(17);
        textView.setAlpha(0.0f);
        textView.setLayoutDirection(this.mList.getLayoutDirection());
        return textView;
    }

    public void updateLayout() {
        if (!this.mUpdatingLayout) {
            this.mUpdatingLayout = true;
            updateContainerRect();
            layoutThumb();
            layoutTrack();
            updateOffsetAndRange();
            Rect bounds = this.mTempBounds;
            measurePreview(this.mPrimaryText, bounds);
            applyLayout(this.mPrimaryText, bounds);
            measurePreview(this.mSecondaryText, bounds);
            applyLayout(this.mSecondaryText, bounds);
            if (this.mPreviewImage != null) {
                bounds.left -= this.mPreviewImage.getPaddingLeft();
                bounds.top -= this.mPreviewImage.getPaddingTop();
                bounds.right += this.mPreviewImage.getPaddingRight();
                bounds.bottom += this.mPreviewImage.getPaddingBottom();
                applyLayout(this.mPreviewImage, bounds);
            }
            this.mUpdatingLayout = false;
        }
    }

    private void applyLayout(View view, Rect bounds) {
        view.layout(bounds.left, bounds.top, bounds.right, bounds.bottom);
        view.setPivotX((float) (this.mLayoutFromRight ? bounds.right - bounds.left : 0));
    }

    private void measurePreview(View v, Rect out) {
        Rect margins = this.mTempMargins;
        margins.left = this.mPreviewImage.getPaddingLeft();
        margins.top = this.mPreviewImage.getPaddingTop();
        margins.right = this.mPreviewImage.getPaddingRight();
        margins.bottom = this.mPreviewImage.getPaddingBottom();
        if (this.mOverlayPosition == 0) {
            measureFloating(v, margins, out);
        } else {
            measureViewToSide(v, this.mThumbImage, margins, out);
        }
    }

    private void measureViewToSide(View view, View adjacent, Rect margins, Rect out) {
        int marginLeft;
        int marginTop;
        int marginRight;
        int maxWidth;
        int right;
        int left;
        if (margins == null) {
            marginLeft = 0;
            marginTop = 0;
            marginRight = 0;
        } else {
            marginLeft = margins.left;
            marginTop = margins.top;
            marginRight = margins.right;
        }
        Rect container = this.mContainerRect;
        int containerWidth = container.width();
        if (adjacent == null) {
            maxWidth = containerWidth;
        } else if (this.mLayoutFromRight) {
            maxWidth = adjacent.getLeft();
        } else {
            maxWidth = containerWidth - adjacent.getRight();
        }
        int adjMaxHeight = Math.max(0, container.height());
        int adjMaxWidth = Math.max(0, (maxWidth - marginLeft) - marginRight);
        view.measure(MeasureSpec.makeMeasureSpec(adjMaxWidth, Integer.MIN_VALUE), MeasureSpec.makeSafeMeasureSpec(adjMaxHeight, 0));
        int width = Math.min(adjMaxWidth, view.getMeasuredWidth());
        if (this.mLayoutFromRight) {
            right = (adjacent == null ? container.right : adjacent.getLeft()) - marginRight;
            left = right - width;
        } else {
            left = (adjacent == null ? container.left : adjacent.getRight()) + marginLeft;
            right = left + width;
        }
        int top = marginTop;
        out.set(left, top, right, top + view.getMeasuredHeight());
    }

    private void measureFloating(View preview, Rect margins, Rect out) {
        int marginLeft;
        int marginTop;
        int marginRight;
        if (margins == null) {
            marginLeft = 0;
            marginTop = 0;
            marginRight = 0;
        } else {
            marginLeft = margins.left;
            marginTop = margins.top;
            marginRight = margins.right;
        }
        Rect container = this.mContainerRect;
        int containerWidth = container.width();
        int adjMaxHeight = Math.max(0, container.height());
        preview.measure(MeasureSpec.makeMeasureSpec(Math.max(0, (containerWidth - marginLeft) - marginRight), Integer.MIN_VALUE), MeasureSpec.makeSafeMeasureSpec(adjMaxHeight, 0));
        int containerHeight = container.height();
        int width = preview.getMeasuredWidth();
        int top = ((containerHeight / 10) + marginTop) + container.top;
        int left = ((containerWidth - width) / 2) + container.left;
        Rect rect = out;
        rect.set(left, top, left + width, top + preview.getMeasuredHeight());
    }

    private void updateContainerRect() {
        AbsListView list = this.mList;
        list.resolvePadding();
        Rect container = this.mContainerRect;
        container.left = 0;
        container.top = 0;
        container.right = list.getWidth();
        container.bottom = list.getHeight();
        int scrollbarStyle = this.mScrollBarStyle;
        if (scrollbarStyle == 16777216 || scrollbarStyle == 0) {
            container.left += list.getPaddingLeft();
            container.top += list.getPaddingTop();
            container.right -= list.getPaddingRight();
            container.bottom -= list.getPaddingBottom();
            if (scrollbarStyle == 16777216) {
                int width = getWidth();
                if (this.mScrollbarPosition == 2) {
                    container.right += width;
                } else {
                    container.left -= width;
                }
            }
        }
    }

    private void layoutThumb() {
        Rect bounds = this.mTempBounds;
        measureViewToSide(this.mThumbImage, null, null, bounds);
        applyLayout(this.mThumbImage, bounds);
    }

    private void layoutTrack() {
        int top;
        int bottom;
        View track = this.mTrackImage;
        View thumb = this.mThumbImage;
        Rect container = this.mContainerRect;
        track.measure(MeasureSpec.makeMeasureSpec(Math.max(0, container.width()), Integer.MIN_VALUE), MeasureSpec.makeSafeMeasureSpec(Math.max(0, container.height()), 0));
        if (this.mThumbPosition == 1) {
            top = container.top;
            bottom = container.bottom;
        } else {
            int thumbHalfHeight = thumb.getHeight() / 2;
            top = container.top + thumbHalfHeight;
            bottom = container.bottom - thumbHalfHeight;
        }
        int trackWidth = track.getMeasuredWidth();
        int left = thumb.getLeft() + ((thumb.getWidth() - trackWidth) / 2);
        track.layout(left, top, left + trackWidth, bottom);
    }

    private void updateOffsetAndRange() {
        float min;
        float max;
        View trackImage = this.mTrackImage;
        View thumbImage = this.mThumbImage;
        if (this.mThumbPosition == 1) {
            float halfThumbHeight = ((float) thumbImage.getHeight()) / 2.0f;
            min = ((float) trackImage.getTop()) + halfThumbHeight;
            max = ((float) trackImage.getBottom()) - halfThumbHeight;
        } else {
            min = (float) trackImage.getTop();
            max = (float) trackImage.getBottom();
        }
        this.mThumbOffset = min;
        this.mThumbRange = max - min;
    }

    private void setState(int state) {
        this.mList.removeCallbacks(this.mDeferHide);
        if (this.mAlwaysShow && state == 0) {
            state = 1;
        }
        if (state != this.mState) {
            switch (state) {
                case 0:
                case 3:
                    transitionToHidden();
                    break;
                case 1:
                    transitionToVisible();
                    break;
                case 2:
                    if (!transitionPreviewLayout(this.mCurrentSection)) {
                        transitionToVisible();
                        break;
                    } else {
                        transitionToDragging();
                        break;
                    }
            }
            this.mState = state;
            refreshDrawablePressedState();
        }
    }

    private void refreshDrawablePressedState() {
        boolean isPressed = this.mState == 2;
        this.mThumbImage.setPressed(isPressed);
        this.mTrackImage.setPressed(isPressed);
    }

    private void transitionToHidden() {
        int width;
        if (this.mDecorAnimation != null) {
            this.mDecorAnimation.cancel();
        }
        Property property = View.ALPHA;
        View[] viewArr = new View[5];
        viewArr[0] = this.mThumbImage;
        viewArr[1] = this.mTrackImage;
        viewArr[2] = this.mPreviewImage;
        viewArr[3] = this.mPrimaryText;
        viewArr[4] = this.mSecondaryText;
        Animator fadeOut = groupAnimatorOfFloat(property, 0.0f, viewArr).setDuration(300);
        if (this.mLayoutFromRight) {
            width = this.mThumbImage.getWidth();
        } else {
            width = -this.mThumbImage.getWidth();
        }
        float offset = (float) width;
        property = View.TRANSLATION_X;
        viewArr = new View[2];
        viewArr[0] = this.mThumbImage;
        viewArr[1] = this.mTrackImage;
        Animator slideOut = groupAnimatorOfFloat(property, offset, viewArr).setDuration(300);
        this.mDecorAnimation = new AnimatorSet();
        AnimatorSet animatorSet;
        Animator[] animatorArr;
        if (this.mIsOppoStyle) {
            animatorSet = this.mDecorAnimation;
            animatorArr = new Animator[1];
            animatorArr[0] = fadeOut;
            animatorSet.playTogether(animatorArr);
            this.mDecorAnimation.addListener(new AnonymousClass7(this));
        } else {
            animatorSet = this.mDecorAnimation;
            animatorArr = new Animator[2];
            animatorArr[0] = fadeOut;
            animatorArr[1] = slideOut;
            animatorSet.playTogether(animatorArr);
        }
        this.mDecorAnimation.start();
        this.mShowingPreview = false;
    }

    private void transitionToVisible() {
        if (this.mDecorAnimation != null) {
            this.mDecorAnimation.cancel();
        }
        Property property = View.ALPHA;
        View[] viewArr = new View[2];
        viewArr[0] = this.mThumbImage;
        viewArr[1] = this.mTrackImage;
        Animator fadeIn = groupAnimatorOfFloat(property, 1.0f, viewArr).setDuration(150);
        property = View.ALPHA;
        viewArr = new View[3];
        viewArr[0] = this.mPreviewImage;
        viewArr[1] = this.mPrimaryText;
        viewArr[2] = this.mSecondaryText;
        Animator fadeOut = groupAnimatorOfFloat(property, 0.0f, viewArr).setDuration(300);
        property = View.TRANSLATION_X;
        viewArr = new View[2];
        viewArr[0] = this.mThumbImage;
        viewArr[1] = this.mTrackImage;
        Animator slideIn = groupAnimatorOfFloat(property, 0.0f, viewArr).setDuration(150);
        this.mDecorAnimation = new AnimatorSet();
        AnimatorSet animatorSet;
        Animator[] animatorArr;
        if (this.mIsOppoStyle) {
            animatorSet = this.mDecorAnimation;
            animatorArr = new Animator[2];
            animatorArr[0] = fadeIn;
            animatorArr[1] = fadeOut;
            animatorSet.playTogether(animatorArr);
        } else {
            animatorSet = this.mDecorAnimation;
            animatorArr = new Animator[3];
            animatorArr[0] = fadeIn;
            animatorArr[1] = fadeOut;
            animatorArr[2] = slideIn;
            animatorSet.playTogether(animatorArr);
        }
        this.mDecorAnimation.start();
        this.mShowingPreview = false;
    }

    private void transitionToDragging() {
        if (this.mDecorAnimation != null) {
            this.mDecorAnimation.cancel();
        }
        Property property = View.ALPHA;
        View[] viewArr = new View[3];
        viewArr[0] = this.mThumbImage;
        viewArr[1] = this.mTrackImage;
        viewArr[2] = this.mPreviewImage;
        Animator fadeIn = groupAnimatorOfFloat(property, 1.0f, viewArr).setDuration(150);
        property = View.TRANSLATION_X;
        viewArr = new View[2];
        viewArr[0] = this.mThumbImage;
        viewArr[1] = this.mTrackImage;
        Animator slideIn = groupAnimatorOfFloat(property, 0.0f, viewArr).setDuration(150);
        this.mDecorAnimation = new AnimatorSet();
        AnimatorSet animatorSet;
        Animator[] animatorArr;
        if (this.mIsOppoStyle) {
            animatorSet = this.mDecorAnimation;
            animatorArr = new Animator[1];
            animatorArr[0] = fadeIn;
            animatorSet.playTogether(animatorArr);
        } else {
            animatorSet = this.mDecorAnimation;
            animatorArr = new Animator[2];
            animatorArr[0] = fadeIn;
            animatorArr[1] = slideIn;
            animatorSet.playTogether(animatorArr);
        }
        this.mDecorAnimation.start();
        this.mShowingPreview = true;
    }

    private void postAutoHide() {
        this.mList.removeCallbacks(this.mDeferHide);
        this.mList.postDelayed(this.mDeferHide, FADE_TIMEOUT);
    }

    public void onScroll(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean hasMoreItems = false;
        if (isEnabled()) {
            if (totalItemCount - visibleItemCount > 0) {
                hasMoreItems = true;
            }
            if (hasMoreItems && this.mState != 2) {
                setThumbPos(getPosFromItemCount(firstVisibleItem, visibleItemCount, totalItemCount));
            }
            this.mScrollCompleted = true;
            if (this.mFirstVisibleItem != firstVisibleItem) {
                this.mFirstVisibleItem = firstVisibleItem;
                if (this.mState != 2) {
                    setState(1);
                    postAutoHide();
                }
            }
            return;
        }
        setState(0);
    }

    private void getSectionsFromIndexer() {
        this.mSectionIndexer = null;
        Adapter adapter = this.mList.getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            this.mHeaderCount = ((HeaderViewListAdapter) adapter).getHeadersCount();
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        if (adapter instanceof ExpandableListConnector) {
            ExpandableListAdapter expAdapter = ((ExpandableListConnector) adapter).getAdapter();
            if (expAdapter instanceof SectionIndexer) {
                this.mSectionIndexer = (SectionIndexer) expAdapter;
                this.mListAdapter = adapter;
                this.mSections = this.mSectionIndexer.getSections();
            }
        } else if (adapter instanceof SectionIndexer) {
            this.mListAdapter = adapter;
            this.mSectionIndexer = (SectionIndexer) adapter;
            this.mSections = this.mSectionIndexer.getSections();
        } else {
            this.mListAdapter = adapter;
            this.mSections = null;
        }
    }

    public void onSectionsChanged() {
        this.mListAdapter = null;
    }

    private void scrollTo(float position) {
        int sectionIndex;
        this.mScrollCompleted = false;
        int count = this.mList.getCount();
        Object[] sections = this.mSections;
        int sectionCount = sections == null ? 0 : sections.length;
        ExpandableListView expList;
        if (sections == null || sectionCount <= 1) {
            int index;
            int offsetY = 0;
            if (this.mIsOppoStyle) {
                int childCount = this.mList.getChildCount();
                View firstChild = this.mList.getChildAt(0);
                View lastChild = this.mList.getChildAt(childCount - 1);
                float offsetPostion = 0.0f;
                if (!(firstChild == null || firstChild.getHeight() == 0)) {
                    offsetPostion = (((float) (firstChild.getTop() - this.mList.getTop())) + ((float) (this.mList.getBottom() - lastChild.getBottom()))) / ((float) firstChild.getHeight());
                }
                int oppoindex = MathUtils.constrain((int) (((((float) (count - childCount)) - offsetPostion) * position) * 1000.0f), 0, (count - 1) * 1000);
                index = oppoindex / 1000;
                if (!(firstChild == null || firstChild.getHeight() == 0)) {
                    offsetY = firstChild.getHeight() - ((firstChild.getHeight() * (oppoindex % 1000)) / 1000);
                    index++;
                }
            } else {
                index = MathUtils.constrain((int) (((float) count) * position), 0, count - 1);
            }
            if (this.mList instanceof ExpandableListView) {
                expList = (ExpandableListView) this.mList;
                expList.setSelectionFromTop(expList.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(this.mHeaderCount + index)), 0);
            } else if (!(this.mList instanceof ListView)) {
                this.mList.setSelection(this.mHeaderCount + index);
            } else if (!this.mIsOppoStyle) {
                ((ListView) this.mList).setSelectionFromTop(this.mHeaderCount + index, 0);
            } else if (position < 1.0f || offsetY == 0) {
                ((ListView) this.mList).setSelectionFromTop(this.mHeaderCount + index, offsetY);
            } else {
                ((ListView) this.mList).setSelectionFromTop((index + 1) + this.mHeaderCount, 0);
            }
            sectionIndex = -1;
        } else {
            float prevPosition;
            float nextPosition;
            int exactSection = MathUtils.constrain((int) (((float) sectionCount) * position), 0, sectionCount - 1);
            int targetSection = exactSection;
            int targetIndex = this.mSectionIndexer.getPositionForSection(exactSection);
            sectionIndex = exactSection;
            int nextIndex = count;
            int prevIndex = targetIndex;
            int prevSection = exactSection;
            int nextSection = exactSection + 1;
            if (exactSection < sectionCount - 1) {
                nextIndex = this.mSectionIndexer.getPositionForSection(exactSection + 1);
            }
            if (nextIndex == targetIndex) {
                while (targetSection > 0) {
                    targetSection--;
                    prevIndex = this.mSectionIndexer.getPositionForSection(targetSection);
                    if (prevIndex == targetIndex) {
                        if (targetSection == 0) {
                            sectionIndex = 0;
                            break;
                        }
                    }
                    prevSection = targetSection;
                    sectionIndex = targetSection;
                    break;
                }
            }
            while (true) {
                int nextNextSection = nextSection + 1;
                if (nextNextSection >= sectionCount || this.mSectionIndexer.getPositionForSection(nextNextSection) != nextIndex) {
                    prevPosition = ((float) prevSection) / ((float) sectionCount);
                    nextPosition = ((float) nextSection) / ((float) sectionCount);
                } else {
                    nextNextSection++;
                }
            }
            prevPosition = ((float) prevSection) / ((float) sectionCount);
            nextPosition = ((float) nextSection) / ((float) sectionCount);
            float snapThreshold = count == 0 ? Float.MAX_VALUE : 0.125f / ((float) count);
            if (prevSection != exactSection || position - prevPosition >= snapThreshold) {
                targetIndex = prevIndex + ((int) ((((float) (nextIndex - prevIndex)) * (position - prevPosition)) / (nextPosition - prevPosition)));
            } else {
                targetIndex = prevIndex;
            }
            targetIndex = MathUtils.constrain(targetIndex, 0, count - 1);
            if (this.mList instanceof ExpandableListView) {
                expList = this.mList;
                expList.setSelectionFromTop(expList.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(this.mHeaderCount + targetIndex)), 0);
            } else if (this.mList instanceof ListView) {
                ((ListView) this.mList).setSelectionFromTop(this.mHeaderCount + targetIndex, 0);
            } else {
                this.mList.setSelection(this.mHeaderCount + targetIndex);
            }
        }
        if (this.mCurrentSection != sectionIndex) {
            this.mCurrentSection = sectionIndex;
            boolean hasPreview = transitionPreviewLayout(sectionIndex);
            if (!this.mShowingPreview && hasPreview) {
                transitionToDragging();
            } else if (this.mShowingPreview && !hasPreview) {
                transitionToVisible();
            }
        }
    }

    private boolean transitionPreviewLayout(int sectionIndex) {
        TextView showing;
        View target;
        Object[] sections = this.mSections;
        CharSequence text = null;
        if (sections != null && sectionIndex >= 0 && sectionIndex < sections.length) {
            Object section = sections[sectionIndex];
            if (section != null) {
                text = section.toString();
            }
        }
        Rect bounds = this.mTempBounds;
        View preview = this.mPreviewImage;
        if (this.mShowingPrimary) {
            showing = this.mPrimaryText;
            target = this.mSecondaryText;
        } else {
            showing = this.mSecondaryText;
            target = this.mPrimaryText;
        }
        target.setText(text);
        measurePreview(target, bounds);
        applyLayout(target, bounds);
        if (this.mPreviewAnimation != null) {
            this.mPreviewAnimation.cancel();
        }
        Animator showTarget = animateAlpha(target, 1.0f).setDuration(50);
        Animator hideShowing = animateAlpha(showing, 0.0f).setDuration(50);
        hideShowing.addListener(this.mSwitchPrimaryListener);
        bounds.left -= preview.getPaddingLeft();
        bounds.top -= preview.getPaddingTop();
        bounds.right += preview.getPaddingRight();
        bounds.bottom += preview.getPaddingBottom();
        Animator resizePreview = animateBounds(preview, bounds);
        resizePreview.setDuration(100);
        this.mPreviewAnimation = new AnimatorSet();
        Builder builder = this.mPreviewAnimation.play(hideShowing).with(showTarget);
        builder.with(resizePreview);
        int previewWidth = (preview.getWidth() - preview.getPaddingLeft()) - preview.getPaddingRight();
        int targetWidth = target.getWidth();
        if (targetWidth > previewWidth) {
            target.setScaleX(((float) previewWidth) / ((float) targetWidth));
            builder.with(animateScaleX(target, 1.0f).setDuration(100));
        } else {
            target.setScaleX(1.0f);
        }
        int showingWidth = showing.getWidth();
        if (showingWidth > targetWidth) {
            builder.with(animateScaleX(showing, ((float) targetWidth) / ((float) showingWidth)).setDuration(100));
        }
        this.mPreviewAnimation.start();
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        return true;
    }

    private void setThumbPos(float position) {
        float previewPos;
        float thumbMiddle = (this.mThumbRange * position) + this.mThumbOffset;
        this.mThumbImage.setTranslationY(thumbMiddle - (((float) this.mThumbImage.getHeight()) / 2.0f));
        View previewImage = this.mPreviewImage;
        float previewHalfHeight = ((float) previewImage.getHeight()) / 2.0f;
        switch (this.mOverlayPosition) {
            case 1:
                previewPos = thumbMiddle;
                break;
            case 2:
                previewPos = thumbMiddle - previewHalfHeight;
                break;
            default:
                previewPos = 0.0f;
                break;
        }
        Rect container = this.mContainerRect;
        float previewTop = MathUtils.constrain(previewPos, ((float) container.top) + previewHalfHeight, ((float) container.bottom) - previewHalfHeight) - previewHalfHeight;
        previewImage.setTranslationY(previewTop);
        this.mPrimaryText.setTranslationY(previewTop);
        this.mSecondaryText.setTranslationY(previewTop);
    }

    private float getPosFromMotionEvent(float y) {
        if (this.mThumbRange <= 0.0f) {
            return 0.0f;
        }
        return MathUtils.constrain((y - this.mThumbOffset) / this.mThumbRange, 0.0f, 1.0f);
    }

    private float getPosFromItemCount(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        SectionIndexer sectionIndexer = this.mSectionIndexer;
        if (sectionIndexer == null || this.mListAdapter == null) {
            getSectionsFromIndexer();
        }
        if (visibleItemCount == 0 || totalItemCount == 0) {
            return 0.0f;
        }
        boolean hasSections;
        if (sectionIndexer == null || this.mSections == null) {
            hasSections = false;
        } else {
            hasSections = this.mSections.length > 0;
        }
        View child;
        if (hasSections && this.mMatchDragPosition) {
            firstVisibleItem -= this.mHeaderCount;
            if (firstVisibleItem < 0) {
                return 0.0f;
            }
            float incrementalPos;
            int positionsInSection;
            float posWithinSection;
            totalItemCount -= this.mHeaderCount;
            child = this.mList.getChildAt(0);
            if (child == null || child.getHeight() == 0) {
                incrementalPos = 0.0f;
            } else {
                incrementalPos = ((float) (this.mList.getPaddingTop() - child.getTop())) / ((float) child.getHeight());
            }
            int section = sectionIndexer.getSectionForPosition(firstVisibleItem);
            int sectionPos = sectionIndexer.getPositionForSection(section);
            int sectionCount = this.mSections.length;
            if (section < sectionCount - 1) {
                int nextSectionPos;
                if (section + 1 < sectionCount) {
                    nextSectionPos = sectionIndexer.getPositionForSection(section + 1);
                } else {
                    nextSectionPos = totalItemCount - 1;
                }
                positionsInSection = nextSectionPos - sectionPos;
            } else {
                positionsInSection = totalItemCount - sectionPos;
            }
            if (positionsInSection == 0) {
                posWithinSection = 0.0f;
            } else {
                posWithinSection = ((((float) firstVisibleItem) + incrementalPos) - ((float) sectionPos)) / ((float) positionsInSection);
            }
            float result = (((float) section) + posWithinSection) / ((float) sectionCount);
            if (firstVisibleItem > 0 && firstVisibleItem + visibleItemCount == totalItemCount) {
                int maxSize;
                int currentVisibleSize;
                View lastChild = this.mList.getChildAt(visibleItemCount - 1);
                int bottomPadding = this.mList.getPaddingBottom();
                if (this.mList.getClipToPadding()) {
                    maxSize = lastChild.getHeight();
                    currentVisibleSize = (this.mList.getHeight() - bottomPadding) - lastChild.getTop();
                } else {
                    maxSize = lastChild.getHeight() + bottomPadding;
                    currentVisibleSize = this.mList.getHeight() - lastChild.getTop();
                }
                if (currentVisibleSize > 0 && maxSize > 0) {
                    result += (1.0f - result) * (((float) currentVisibleSize) / ((float) maxSize));
                }
            }
            return result;
        } else if (visibleItemCount == totalItemCount) {
            return 0.0f;
        } else {
            if (!this.mIsOppoStyle) {
                return ((float) firstVisibleItem) / ((float) (totalItemCount - visibleItemCount));
            }
            float opporesult;
            child = this.mList.getChildAt(0);
            if (child == null || child.getHeight() == 0) {
                opporesult = (((float) firstVisibleItem) + 0.0f) / ((float) (totalItemCount - visibleItemCount));
            } else {
                opporesult = (((float) firstVisibleItem) + (((float) (this.mList.getPaddingTop() - child.getTop())) / ((float) child.getHeight()))) / ((float) (totalItemCount - (this.mList.getHeight() / child.getHeight())));
            }
            if (opporesult > 1.0f) {
                return 1.0f;
            }
            return opporesult;
        }
    }

    private void cancelFling() {
        MotionEvent cancelFling = MotionEvent.obtain(0, 0, 3, 0.0f, 0.0f, 0);
        this.mList.onTouchEvent(cancelFling);
        cancelFling.recycle();
    }

    private void cancelPendingDrag() {
        this.mPendingDrag = -1;
    }

    private void startPendingDrag() {
        this.mPendingDrag = SystemClock.uptimeMillis() + TAP_TIMEOUT;
    }

    private void beginDrag() {
        this.mPendingDrag = -1;
        setState(2);
        if (this.mListAdapter == null && this.mList != null) {
            getSectionsFromIndexer();
        }
        if (this.mList != null) {
            this.mList.requestDisallowInterceptTouchEvent(true);
            this.mList.reportScrollStateChange(1);
        }
        cancelFling();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        switch (ev.getActionMasked()) {
            case 0:
                if (isPointInside(ev.getX(), ev.getY())) {
                    if (this.mList.isInScrollingContainer() || (this.mState == 0 && this.mIsOppoStyle)) {
                        this.mInitialTouchY = ev.getY();
                        startPendingDrag();
                        break;
                    }
                    return true;
                }
                break;
            case 1:
            case 3:
                cancelPendingDrag();
                break;
            case 2:
                if (!isPointInside(ev.getX(), ev.getY())) {
                    cancelPendingDrag();
                    break;
                } else if (this.mPendingDrag >= 0 && this.mPendingDrag <= SystemClock.uptimeMillis()) {
                    beginDrag();
                    scrollTo(getPosFromMotionEvent(this.mInitialTouchY));
                    return onTouchEvent(ev);
                }
        }
        return false;
    }

    public boolean onInterceptHoverEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        int actionMasked = ev.getActionMasked();
        if ((actionMasked == 9 || actionMasked == 7) && this.mState == 0 && isPointInside(ev.getX(), ev.getY())) {
            setState(1);
            postAutoHide();
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent me) {
        if (!isEnabled()) {
            return false;
        }
        float pos;
        switch (me.getActionMasked()) {
            case 0:
                if (!(!isPointInside(me.getX(), me.getY()) || this.mList.isInScrollingContainer() || (this.mState == 0 && this.mIsOppoStyle))) {
                    beginDrag();
                    return true;
                }
            case 1:
                if (this.mPendingDrag >= 0 && (!this.mIsOppoStyle || this.mState == 1 || this.mState == 2)) {
                    beginDrag();
                    pos = getPosFromMotionEvent(me.getY());
                    setThumbPos(pos);
                    scrollTo(pos);
                }
                if (this.mState == 2) {
                    if (this.mList != null) {
                        this.mList.requestDisallowInterceptTouchEvent(false);
                        this.mList.reportScrollStateChange(0);
                    }
                    setState(1);
                    postAutoHide();
                    return true;
                }
                break;
            case 2:
                if (this.mPendingDrag >= 0 && Math.abs(me.getY() - this.mInitialTouchY) > ((float) this.mScaledTouchSlop)) {
                    beginDrag();
                }
                if (this.mState == 2) {
                    pos = getPosFromMotionEvent(me.getY());
                    setThumbPos(pos);
                    if (this.mScrollCompleted) {
                        scrollTo(pos);
                    }
                    return true;
                }
                break;
            case 3:
                cancelPendingDrag();
                break;
        }
        return false;
    }

    private boolean isPointInside(float x, float y) {
        if (isPointInsideX(x)) {
            return this.mTrackDrawable == null ? isPointInsideY(y) : true;
        } else {
            return false;
        }
    }

    private boolean isPointInsideX(float x) {
        boolean z = true;
        float offset = this.mThumbImage.getTranslationX();
        float targetSizeDiff = ((float) this.mMinimumTouchTarget) - ((((float) this.mThumbImage.getRight()) + offset) - (((float) this.mThumbImage.getLeft()) + offset));
        float adjust = targetSizeDiff > 0.0f ? targetSizeDiff : 0.0f;
        if (this.mLayoutFromRight) {
            if (x < ((float) this.mThumbImage.getLeft()) - adjust) {
                z = false;
            }
            return z;
        }
        if (x > ((float) this.mThumbImage.getRight()) + adjust) {
            z = false;
        }
        return z;
    }

    private boolean isPointInsideY(float y) {
        float adjust = 0.0f;
        float offset = this.mThumbImage.getTranslationY();
        float top = ((float) this.mThumbImage.getTop()) + offset;
        float bottom = ((float) this.mThumbImage.getBottom()) + offset;
        float targetSizeDiff = ((float) this.mMinimumTouchTarget) - (bottom - top);
        if (targetSizeDiff > 0.0f) {
            adjust = targetSizeDiff / 2.0f;
        }
        if (y < top - adjust || y > bottom + adjust) {
            return false;
        }
        return true;
    }

    private static Animator groupAnimatorOfFloat(Property<View, Float> property, float value, View... views) {
        AnimatorSet animSet = new AnimatorSet();
        Builder builder = null;
        for (int i = views.length - 1; i >= 0; i--) {
            Object obj = views[i];
            float[] fArr = new float[1];
            fArr[0] = value;
            Animator anim = ObjectAnimator.ofFloat(obj, property, fArr);
            if (builder == null) {
                builder = animSet.play(anim);
            } else {
                builder.with(anim);
            }
        }
        return animSet;
    }

    private static Animator animateScaleX(View v, float target) {
        Property property = View.SCALE_X;
        float[] fArr = new float[1];
        fArr[0] = target;
        return ObjectAnimator.ofFloat(v, property, fArr);
    }

    private static Animator animateAlpha(View v, float alpha) {
        Property property = View.ALPHA;
        float[] fArr = new float[1];
        fArr[0] = alpha;
        return ObjectAnimator.ofFloat(v, property, fArr);
    }

    private static Animator animateBounds(View v, Rect bounds) {
        Property property = LEFT;
        int[] iArr = new int[1];
        iArr[0] = bounds.left;
        PropertyValuesHolder left = PropertyValuesHolder.ofInt(property, iArr);
        property = TOP;
        iArr = new int[1];
        iArr[0] = bounds.top;
        PropertyValuesHolder top = PropertyValuesHolder.ofInt(property, iArr);
        property = RIGHT;
        iArr = new int[1];
        iArr[0] = bounds.right;
        PropertyValuesHolder right = PropertyValuesHolder.ofInt(property, iArr);
        property = BOTTOM;
        iArr = new int[1];
        iArr[0] = bounds.bottom;
        PropertyValuesHolder bottom = PropertyValuesHolder.ofInt(property, iArr);
        PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[4];
        propertyValuesHolderArr[0] = left;
        propertyValuesHolderArr[1] = top;
        propertyValuesHolderArr[2] = right;
        propertyValuesHolderArr[3] = bottom;
        return ObjectAnimator.ofPropertyValuesHolder(v, propertyValuesHolderArr);
    }
}
