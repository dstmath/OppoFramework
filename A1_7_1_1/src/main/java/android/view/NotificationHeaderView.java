package android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View.AccessibilityDelegate;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import java.util.ArrayList;

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
@RemoteView
public class NotificationHeaderView extends ViewGroup {
    public static final int NO_COLOR = -1;
    private View mAppName;
    private Drawable mBackground;
    private final int mChildMinWidth;
    private final int mContentEndMargin;
    private ImageView mExpandButton;
    private OnClickListener mExpandClickListener;
    final AccessibilityDelegate mExpandDelegate;
    private boolean mExpanded;
    private int mHeaderBackgroundHeight;
    private View mHeaderText;
    private View mIcon;
    private int mIconColor;
    private View mInfo;
    private ImageView mLineDivider;
    private int mOriginalNotificationColor;
    private View mProfileBadge;
    ViewOutlineProvider mProvider;
    private boolean mShowWorkBadgeAtEnd;
    private HeaderTouchListener mTouchListener;

    /* renamed from: android.view.NotificationHeaderView$2 */
    class AnonymousClass2 extends AccessibilityDelegate {
        final /* synthetic */ NotificationHeaderView this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.view.NotificationHeaderView.2.<init>(android.view.NotificationHeaderView):void, dex: 
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
        AnonymousClass2(android.view.NotificationHeaderView r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.view.NotificationHeaderView.2.<init>(android.view.NotificationHeaderView):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.NotificationHeaderView.2.<init>(android.view.NotificationHeaderView):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.view.NotificationHeaderView.2.onInitializeAccessibilityNodeInfo(android.view.View, android.view.accessibility.AccessibilityNodeInfo):void, dex:  in method: android.view.NotificationHeaderView.2.onInitializeAccessibilityNodeInfo(android.view.View, android.view.accessibility.AccessibilityNodeInfo):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.view.NotificationHeaderView.2.onInitializeAccessibilityNodeInfo(android.view.View, android.view.accessibility.AccessibilityNodeInfo):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void onInitializeAccessibilityNodeInfo(android.view.View r1, android.view.accessibility.AccessibilityNodeInfo r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.view.NotificationHeaderView.2.onInitializeAccessibilityNodeInfo(android.view.View, android.view.accessibility.AccessibilityNodeInfo):void, dex:  in method: android.view.NotificationHeaderView.2.onInitializeAccessibilityNodeInfo(android.view.View, android.view.accessibility.AccessibilityNodeInfo):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.NotificationHeaderView.2.onInitializeAccessibilityNodeInfo(android.view.View, android.view.accessibility.AccessibilityNodeInfo):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.view.NotificationHeaderView.2.performAccessibilityAction(android.view.View, int, android.os.Bundle):boolean, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.view.NotificationHeaderView.2.performAccessibilityAction(android.view.View, int, android.os.Bundle):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.NotificationHeaderView.2.performAccessibilityAction(android.view.View, int, android.os.Bundle):boolean");
        }
    }

    public class HeaderTouchListener implements OnTouchListener {
        private float mDownX;
        private float mDownY;
        private final ArrayList<Rect> mTouchRects;
        private int mTouchSlop;
        private boolean mTrackGesture;
        final /* synthetic */ NotificationHeaderView this$0;

        public HeaderTouchListener(NotificationHeaderView this$0) {
            this.this$0 = this$0;
            this.mTouchRects = new ArrayList();
        }

        public void bindTouchRects() {
            this.mTouchRects.clear();
            addRectAroundViewView(this.this$0.mIcon);
            addRectAroundViewView(this.this$0.mExpandButton);
            addWidthRect();
            this.mTouchSlop = ViewConfiguration.get(this.this$0.getContext()).getScaledTouchSlop();
        }

        private void addWidthRect() {
            Rect r = new Rect();
            r.top = 0;
            r.bottom = (int) (this.this$0.getResources().getDisplayMetrics().density * 32.0f);
            r.left = 0;
            r.right = this.this$0.getWidth();
            this.mTouchRects.add(r);
        }

        private void addRectAroundViewView(View view) {
            this.mTouchRects.add(getRectAroundView(view));
        }

        private Rect getRectAroundView(View view) {
            float size = 48.0f * this.this$0.getResources().getDisplayMetrics().density;
            Rect r = new Rect();
            if (view.getVisibility() == 8) {
                view = this.this$0.getFirstChildNotGone();
                r.left = (int) (((float) view.getLeft()) - (size / 2.0f));
            } else {
                r.left = (int) ((((float) (view.getLeft() + view.getRight())) / 2.0f) - (size / 2.0f));
            }
            r.top = (int) ((((float) (view.getTop() + view.getBottom())) / 2.0f) - (size / 2.0f));
            r.bottom = (int) (((float) r.top) + size);
            r.right = (int) (((float) r.left) + size);
            return r;
        }

        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getActionMasked() & 255) {
                case 0:
                    this.mTrackGesture = false;
                    if (isInside(x, y)) {
                        this.mTrackGesture = true;
                        return true;
                    }
                    break;
                case 1:
                    if (this.mTrackGesture) {
                        this.this$0.mExpandClickListener.onClick(this.this$0);
                        break;
                    }
                    break;
                case 2:
                    if (this.mTrackGesture && (Math.abs(this.mDownX - x) > ((float) this.mTouchSlop) || Math.abs(this.mDownY - y) > ((float) this.mTouchSlop))) {
                        this.mTrackGesture = false;
                        break;
                    }
            }
            return this.mTrackGesture;
        }

        private boolean isInside(float x, float y) {
            for (int i = 0; i < this.mTouchRects.size(); i++) {
                if (((Rect) this.mTouchRects.get(i)).contains((int) x, (int) y)) {
                    this.mDownX = x;
                    this.mDownY = y;
                    return true;
                }
            }
            return false;
        }
    }

    public NotificationHeaderView(Context context) {
        this(context, null);
    }

    public NotificationHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NotificationHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NotificationHeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTouchListener = new HeaderTouchListener(this);
        this.mProvider = new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                if (NotificationHeaderView.this.mBackground != null) {
                    outline.setRect(0, 0, NotificationHeaderView.this.getWidth(), NotificationHeaderView.this.mHeaderBackgroundHeight);
                    outline.setAlpha(1.0f);
                }
            }
        };
        this.mExpandDelegate = new AnonymousClass2(this);
        this.mChildMinWidth = getResources().getDimensionPixelSize(R.dimen.notification_header_shrink_min_width);
        this.mContentEndMargin = getResources().getDimensionPixelSize(R.dimen.notification_content_margin_end);
        this.mHeaderBackgroundHeight = getResources().getDimensionPixelSize(R.dimen.notification_header_background_height);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mAppName = findViewById(R.id.app_name_text);
        this.mHeaderText = findViewById(R.id.header_text);
        this.mExpandButton = (ImageView) findViewById(R.id.expand_button);
        if (this.mExpandButton != null) {
            this.mExpandButton.setAccessibilityDelegate(this.mExpandDelegate);
        }
        this.mIcon = findViewById(R.id.icon);
        this.mProfileBadge = findViewById(R.id.profile_badge);
        this.mLineDivider = (ImageView) findViewById(R.id.line_divider);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int givenWidth = MeasureSpec.getSize(widthMeasureSpec);
        int givenHeight = MeasureSpec.getSize(heightMeasureSpec);
        int wrapContentWidthSpec = MeasureSpec.makeMeasureSpec(givenWidth, Integer.MIN_VALUE);
        int wrapContentHeightSpec = MeasureSpec.makeMeasureSpec(givenHeight, Integer.MIN_VALUE);
        int totalWidth = getPaddingStart() + getPaddingEnd();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                child.measure(ViewGroup.getChildMeasureSpec(wrapContentWidthSpec, lp.leftMargin + lp.rightMargin, lp.width), ViewGroup.getChildMeasureSpec(wrapContentHeightSpec, lp.topMargin + lp.bottomMargin, lp.height));
                totalWidth += (lp.leftMargin + lp.rightMargin) + child.getMeasuredWidth();
            }
        }
        if (totalWidth > givenWidth) {
            int overFlow = totalWidth - givenWidth;
            int appWidth = this.mAppName.getMeasuredWidth();
            if (overFlow > 0 && this.mAppName.getVisibility() != 8 && appWidth > this.mChildMinWidth) {
                int newSize = appWidth - Math.min(appWidth - this.mChildMinWidth, overFlow);
                this.mAppName.measure(MeasureSpec.makeMeasureSpec(newSize, Integer.MIN_VALUE), wrapContentHeightSpec);
                overFlow -= appWidth - newSize;
            }
            if (overFlow > 0 && this.mHeaderText.getVisibility() != 8) {
                this.mHeaderText.measure(MeasureSpec.makeMeasureSpec(Math.max(0, this.mHeaderText.getMeasuredWidth() - overFlow), Integer.MIN_VALUE), wrapContentHeightSpec);
            }
        }
        setMeasuredDimension(givenWidth, givenHeight);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingStart();
        int childCount = getChildCount();
        int ownHeight = (getHeight() - getPaddingTop()) - getPaddingBottom();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                int childHeight = child.getMeasuredHeight();
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                left += params.getMarginStart();
                int right = left + child.getMeasuredWidth();
                int top = (int) (((float) getPaddingTop()) + (((float) (ownHeight - childHeight)) / 2.0f));
                int bottom = top + childHeight;
                int layoutLeft = left;
                int layoutRight = right;
                if (child == this.mProfileBadge) {
                    int paddingEnd = params.getMarginEnd();
                    if (this.mShowWorkBadgeAtEnd) {
                        paddingEnd = this.mContentEndMargin;
                    }
                    layoutRight = getWidth() - paddingEnd;
                    layoutLeft = layoutRight - child.getMeasuredWidth();
                }
                if (getLayoutDirection() == 1) {
                    int ltrLeft = layoutLeft;
                    layoutLeft = getWidth() - layoutRight;
                    layoutRight = getWidth() - ltrLeft;
                }
                if (child == this.mLineDivider) {
                    child.layout(l, 0, r, childHeight);
                } else {
                    child.layout(layoutLeft, top, layoutRight, bottom);
                }
                left = right + params.getMarginEnd();
            }
        }
        updateTouchListener();
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    public void setHeaderBackgroundDrawable(Drawable drawable) {
        if (drawable != null) {
            setWillNotDraw(false);
            this.mBackground = drawable;
            this.mBackground.setCallback(this);
            setOutlineProvider(this.mProvider);
        } else {
            setWillNotDraw(true);
            this.mBackground = null;
            setOutlineProvider(null);
        }
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        if (this.mBackground != null) {
            this.mBackground.setBounds(0, 0, getWidth(), this.mHeaderBackgroundHeight);
            this.mBackground.draw(canvas);
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mBackground;
    }

    protected void drawableStateChanged() {
        if (this.mBackground != null && this.mBackground.isStateful()) {
            this.mBackground.setState(getDrawableState());
        }
    }

    private void updateTouchListener() {
        if (this.mExpandClickListener != null) {
            this.mTouchListener.bindTouchRects();
        }
    }

    public void setOnClickListener(OnClickListener l) {
        OnTouchListener onTouchListener = null;
        this.mExpandClickListener = l;
        if (this.mExpandClickListener != null) {
            onTouchListener = this.mTouchListener;
        }
        setOnTouchListener(onTouchListener);
        this.mExpandButton.setOnClickListener(this.mExpandClickListener);
        updateTouchListener();
    }

    @RemotableViewMethod
    public void setOriginalIconColor(int color) {
        this.mIconColor = color;
    }

    public int getOriginalIconColor() {
        return this.mIconColor;
    }

    @RemotableViewMethod
    public void setOriginalNotificationColor(int color) {
        this.mOriginalNotificationColor = color;
    }

    public int getOriginalNotificationColor() {
        return this.mOriginalNotificationColor;
    }

    @RemotableViewMethod
    public void setExpanded(boolean expanded) {
        this.mExpanded = expanded;
        updateExpandButton();
    }

    private void updateExpandButton() {
        int drawableId;
        if (this.mExpanded) {
            drawableId = R.drawable.ic_collapse_notification;
        } else {
            drawableId = R.drawable.ic_expand_notification;
        }
        this.mExpandButton.setImageDrawable(getContext().getDrawable(drawableId));
        this.mExpandButton.setColorFilter(this.mOriginalNotificationColor);
    }

    public void setShowWorkBadgeAtEnd(boolean showWorkBadgeAtEnd) {
        if (showWorkBadgeAtEnd != this.mShowWorkBadgeAtEnd) {
            setClipToPadding(!showWorkBadgeAtEnd);
            this.mShowWorkBadgeAtEnd = showWorkBadgeAtEnd;
        }
    }

    public View getWorkProfileIcon() {
        return this.mProfileBadge;
    }

    private View getFirstChildNotGone() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                return child;
            }
        }
        return this;
    }

    public ImageView getExpandButton() {
        return this.mExpandButton;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean isInTouchRect(float x, float y) {
        if (this.mExpandClickListener == null) {
            return false;
        }
        return this.mTouchListener.isInside(x, y);
    }
}
