package com.android.internal.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Comparator;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
@RemoteView
public class NotificationActionListLayout extends LinearLayout {
    public static final Comparator<Pair<Integer, TextView>> MEASURE_ORDER_COMPARATOR = null;
    private Drawable mDefaultBackground;
    private int mDefaultPaddingEnd;
    private boolean mMeasureLinearly;
    private ArrayList<View> mMeasureOrderOther;
    private ArrayList<Pair<Integer, TextView>> mMeasureOrderTextViews;
    private int mTotalWidth;

    final /* synthetic */ class -void__clinit___LambdaImpl0 implements Comparator {
        public int compare(Object arg0, Object arg1) {
            return ((Integer) ((Pair) arg0).first).compareTo((Integer) ((Pair) arg1).first);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.widget.NotificationActionListLayout.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.widget.NotificationActionListLayout.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.NotificationActionListLayout.<clinit>():void");
    }

    public NotificationActionListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTotalWidth = 0;
        this.mMeasureOrderTextViews = new ArrayList();
        this.mMeasureOrderOther = new ArrayList();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mMeasureLinearly) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int i;
        View c;
        MarginLayoutParams lp;
        int N = getChildCount();
        int textViews = 0;
        int otherViews = 0;
        int notGoneChildren = 0;
        View lastNotGoneChild = null;
        for (i = 0; i < N; i++) {
            c = getChildAt(i);
            if (c instanceof TextView) {
                textViews++;
            } else {
                otherViews++;
            }
            if (c.getVisibility() != 8) {
                notGoneChildren++;
                lastNotGoneChild = c;
            }
        }
        boolean needRebuild = false;
        if (!(textViews == this.mMeasureOrderTextViews.size() && otherViews == this.mMeasureOrderOther.size())) {
            needRebuild = true;
        }
        if (!needRebuild) {
            int size = this.mMeasureOrderTextViews.size();
            for (i = 0; i < size; i++) {
                Pair<Integer, TextView> pair = (Pair) this.mMeasureOrderTextViews.get(i);
                if (((Integer) pair.first).intValue() != ((TextView) pair.second).getText().length()) {
                    needRebuild = true;
                }
            }
        }
        if (notGoneChildren > 1 && needRebuild) {
            rebuildMeasureOrder(textViews, otherViews);
        }
        boolean constrained = MeasureSpec.getMode(widthMeasureSpec) != 0;
        int innerWidth = (MeasureSpec.getSize(widthMeasureSpec) - this.mPaddingLeft) - this.mPaddingRight;
        int otherSize = this.mMeasureOrderOther.size();
        int usedWidth = 0;
        int measuredChildren = 0;
        for (i = 0; i < N && notGoneChildren > 1; i++) {
            if (i < otherSize) {
                c = (View) this.mMeasureOrderOther.get(i);
            } else {
                c = (View) ((Pair) this.mMeasureOrderTextViews.get(i - otherSize)).second;
            }
            if (c.getVisibility() != 8) {
                lp = (MarginLayoutParams) c.getLayoutParams();
                int usedWidthForChild = usedWidth;
                if (constrained) {
                    usedWidthForChild = innerWidth - ((innerWidth - usedWidth) / (notGoneChildren - measuredChildren));
                }
                measureChildWithMargins(c, widthMeasureSpec, usedWidthForChild, heightMeasureSpec, 0);
                usedWidth += (c.getMeasuredWidth() + lp.rightMargin) + lp.leftMargin;
                measuredChildren++;
            }
        }
        if (lastNotGoneChild != null && ((constrained && usedWidth < innerWidth) || notGoneChildren == 1)) {
            lp = (MarginLayoutParams) lastNotGoneChild.getLayoutParams();
            if (notGoneChildren > 1) {
                usedWidth -= (lastNotGoneChild.getMeasuredWidth() + lp.rightMargin) + lp.leftMargin;
            }
            int originalWidth = lp.width;
            lp.width = -1;
            measureChildWithMargins(lastNotGoneChild, widthMeasureSpec, usedWidth, heightMeasureSpec, 0);
            lp.width = originalWidth;
            usedWidth += (lastNotGoneChild.getMeasuredWidth() + lp.rightMargin) + lp.leftMargin;
        }
        this.mTotalWidth = (this.mPaddingRight + usedWidth) + this.mPaddingLeft;
        setMeasuredDimension(View.resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec), View.resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec));
    }

    private void rebuildMeasureOrder(int capacityText, int capacityOther) {
        clearMeasureOrder();
        this.mMeasureOrderTextViews.ensureCapacity(capacityText);
        this.mMeasureOrderOther.ensureCapacity(capacityOther);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View c = getChildAt(i);
            if (!(c instanceof TextView) || ((TextView) c).getText().length() <= 0) {
                this.mMeasureOrderOther.add(c);
            } else {
                this.mMeasureOrderTextViews.add(Pair.create(Integer.valueOf(((TextView) c).getText().length()), (TextView) c));
            }
        }
        this.mMeasureOrderTextViews.sort(MEASURE_ORDER_COMPARATOR);
    }

    private void clearMeasureOrder() {
        this.mMeasureOrderOther.clear();
        this.mMeasureOrderTextViews.clear();
    }

    public void onViewAdded(View child) {
        super.onViewAdded(child);
        clearMeasureOrder();
    }

    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        clearMeasureOrder();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mMeasureLinearly) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        int childLeft;
        boolean isLayoutRtl = isLayoutRtl();
        int paddingTop = this.mPaddingTop;
        int innerHeight = ((bottom - top) - paddingTop) - this.mPaddingBottom;
        int count = getChildCount();
        switch (Gravity.getAbsoluteGravity(Gravity.START, getLayoutDirection())) {
            case 5:
                childLeft = ((this.mPaddingLeft + right) - left) - this.mTotalWidth;
                break;
            default:
                childLeft = this.mPaddingLeft;
                break;
        }
        int start = 0;
        int dir = 1;
        if (isLayoutRtl) {
            start = count - 1;
            dir = -1;
        }
        for (int i = 0; i < count; i++) {
            View child = getChildAt(start + (dir * i));
            if (child.getVisibility() != 8) {
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int childTop = ((((innerHeight - childHeight) / 2) + paddingTop) + lp.topMargin) - lp.bottomMargin;
                childLeft += lp.leftMargin;
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
                childLeft += lp.rightMargin + childWidth;
            }
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDefaultPaddingEnd = getPaddingEnd();
        this.mDefaultBackground = getBackground();
    }

    @RemotableViewMethod
    public void setEmphasizedMode(boolean emphasizedMode) {
        this.mMeasureLinearly = emphasizedMode;
        setPaddingRelative(getPaddingStart(), getPaddingTop(), emphasizedMode ? 0 : this.mDefaultPaddingEnd, getPaddingBottom());
        setBackground(emphasizedMode ? null : this.mDefaultBackground);
        requestLayout();
    }
}
