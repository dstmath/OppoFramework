package android.app;

import android.animation.LayoutTransition;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.R;

@Deprecated
public class FragmentBreadCrumbs extends ViewGroup implements FragmentManager.OnBackStackChangedListener {
    private static final int DEFAULT_GRAVITY = 8388627;
    Activity mActivity;
    LinearLayout mContainer;
    private int mGravity;
    LayoutInflater mInflater;
    private int mLayoutResId;
    int mMaxVisible;
    /* access modifiers changed from: private */
    public OnBreadCrumbClickListener mOnBreadCrumbClickListener;
    private View.OnClickListener mOnClickListener;
    /* access modifiers changed from: private */
    public View.OnClickListener mParentClickListener;
    BackStackRecord mParentEntry;
    private int mTextColor;
    BackStackRecord mTopEntry;

    @Deprecated
    public interface OnBreadCrumbClickListener {
        boolean onBreadCrumbClick(FragmentManager.BackStackEntry backStackEntry, int i);
    }

    public FragmentBreadCrumbs(Context context) {
        this(context, null);
    }

    public FragmentBreadCrumbs(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.fragmentBreadCrumbsStyle);
    }

    public FragmentBreadCrumbs(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FragmentBreadCrumbs(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mMaxVisible = -1;
        this.mOnClickListener = new View.OnClickListener() {
            /* class android.app.FragmentBreadCrumbs.AnonymousClass1 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (v.getTag() instanceof FragmentManager.BackStackEntry) {
                    FragmentManager.BackStackEntry bse = (FragmentManager.BackStackEntry) v.getTag();
                    if (bse != FragmentBreadCrumbs.this.mParentEntry) {
                        if (FragmentBreadCrumbs.this.mOnBreadCrumbClickListener != null) {
                            if (FragmentBreadCrumbs.this.mOnBreadCrumbClickListener.onBreadCrumbClick(bse == FragmentBreadCrumbs.this.mTopEntry ? null : bse, 0)) {
                                return;
                            }
                        }
                        if (bse == FragmentBreadCrumbs.this.mTopEntry) {
                            FragmentBreadCrumbs.this.mActivity.getFragmentManager().popBackStack();
                        } else {
                            FragmentBreadCrumbs.this.mActivity.getFragmentManager().popBackStack(bse.getId(), 0);
                        }
                    } else if (FragmentBreadCrumbs.this.mParentClickListener != null) {
                        FragmentBreadCrumbs.this.mParentClickListener.onClick(v);
                    }
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FragmentBreadCrumbs, defStyleAttr, defStyleRes);
        this.mGravity = a.getInt(0, DEFAULT_GRAVITY);
        this.mLayoutResId = a.getResourceId(2, R.layout.fragment_bread_crumb_item);
        this.mTextColor = a.getColor(1, 0);
        a.recycle();
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View
     arg types: [int, android.app.FragmentBreadCrumbs, int]
     candidates:
      android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View
      android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View */
    public void setActivity(Activity a) {
        this.mActivity = a;
        this.mInflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContainer = (LinearLayout) this.mInflater.inflate(R.layout.fragment_bread_crumbs, (ViewGroup) this, false);
        addView(this.mContainer);
        a.getFragmentManager().addOnBackStackChangedListener(this);
        updateCrumbs();
        setLayoutTransition(new LayoutTransition());
    }

    public void setMaxVisible(int visibleCrumbs) {
        if (visibleCrumbs >= 1) {
            this.mMaxVisible = visibleCrumbs;
            return;
        }
        throw new IllegalArgumentException("visibleCrumbs must be greater than zero");
    }

    public void setParentTitle(CharSequence title, CharSequence shortTitle, View.OnClickListener listener) {
        this.mParentEntry = createBackStackEntry(title, shortTitle);
        this.mParentClickListener = listener;
        updateCrumbs();
    }

    public void setOnBreadCrumbClickListener(OnBreadCrumbClickListener listener) {
        this.mOnBreadCrumbClickListener = listener;
    }

    private BackStackRecord createBackStackEntry(CharSequence title, CharSequence shortTitle) {
        if (title == null) {
            return null;
        }
        BackStackRecord entry = new BackStackRecord((FragmentManagerImpl) this.mActivity.getFragmentManager());
        entry.setBreadCrumbTitle(title);
        entry.setBreadCrumbShortTitle(shortTitle);
        return entry;
    }

    public void setTitle(CharSequence title, CharSequence shortTitle) {
        this.mTopEntry = createBackStackEntry(title, shortTitle);
        updateCrumbs();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int childRight;
        int childLeft;
        if (getChildCount() != 0) {
            View child = getChildAt(0);
            int childTop = this.mPaddingTop;
            int childBottom = (this.mPaddingTop + child.getMeasuredHeight()) - this.mPaddingBottom;
            int absoluteGravity = Gravity.getAbsoluteGravity(this.mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK, getLayoutDirection());
            if (absoluteGravity == 1) {
                childLeft = this.mPaddingLeft + (((this.mRight - this.mLeft) - child.getMeasuredWidth()) / 2);
                childRight = child.getMeasuredWidth() + childLeft;
            } else if (absoluteGravity != 5) {
                childLeft = this.mPaddingLeft;
                childRight = child.getMeasuredWidth() + childLeft;
            } else {
                childRight = (this.mRight - this.mLeft) - this.mPaddingRight;
                childLeft = childRight - child.getMeasuredWidth();
            }
            if (childLeft < this.mPaddingLeft) {
                childLeft = this.mPaddingLeft;
            }
            if (childRight > (this.mRight - this.mLeft) - this.mPaddingRight) {
                childRight = (this.mRight - this.mLeft) - this.mPaddingRight;
            }
            child.layout(childLeft, childTop, childRight, childBottom);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxHeight = 0;
        int maxWidth = 0;
        int measuredChildState = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
                measuredChildState = combineMeasuredStates(measuredChildState, child.getMeasuredState());
            }
        }
        setMeasuredDimension(resolveSizeAndState(Math.max(maxWidth + this.mPaddingLeft + this.mPaddingRight, getSuggestedMinimumWidth()), widthMeasureSpec, measuredChildState), resolveSizeAndState(Math.max(maxHeight + this.mPaddingTop + this.mPaddingBottom, getSuggestedMinimumHeight()), heightMeasureSpec, measuredChildState << 16));
    }

    @Override // android.app.FragmentManager.OnBackStackChangedListener
    public void onBackStackChanged() {
        updateCrumbs();
    }

    private int getPreEntryCount() {
        int i = 1;
        int i2 = this.mTopEntry != null ? 1 : 0;
        if (this.mParentEntry == null) {
            i = 0;
        }
        return i2 + i;
    }

    private FragmentManager.BackStackEntry getPreEntry(int index) {
        BackStackRecord backStackRecord = this.mParentEntry;
        if (backStackRecord != null) {
            return index == 0 ? backStackRecord : this.mTopEntry;
        }
        return this.mTopEntry;
    }

    /* JADX INFO: Multiple debug info for r4v2 int: [D('viewI' int), D('i' int)] */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View
     arg types: [int, android.app.FragmentBreadCrumbs, int]
     candidates:
      android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View
      android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View */
    /* access modifiers changed from: package-private */
    public void updateCrumbs() {
        int i;
        FragmentManager.BackStackEntry bse;
        FragmentManager fm = this.mActivity.getFragmentManager();
        int numEntries = fm.getBackStackEntryCount();
        int numPreEntries = getPreEntryCount();
        int numViews = this.mContainer.getChildCount();
        for (int i2 = 0; i2 < numEntries + numPreEntries; i2++) {
            if (i2 < numPreEntries) {
                bse = getPreEntry(i2);
            } else {
                bse = fm.getBackStackEntryAt(i2 - numPreEntries);
            }
            if (i2 < numViews && this.mContainer.getChildAt(i2).getTag() != bse) {
                for (int j = i2; j < numViews; j++) {
                    this.mContainer.removeViewAt(i2);
                }
                numViews = i2;
            }
            if (i2 >= numViews) {
                View item = this.mInflater.inflate(this.mLayoutResId, (ViewGroup) this, false);
                TextView text = (TextView) item.findViewById(16908310);
                text.setText(bse.getBreadCrumbTitle());
                text.setTag(bse);
                text.setTextColor(this.mTextColor);
                if (i2 == 0) {
                    item.findViewById(R.id.left_icon).setVisibility(8);
                }
                this.mContainer.addView(item);
                text.setOnClickListener(this.mOnClickListener);
            }
        }
        int i3 = numEntries + numPreEntries;
        int numViews2 = this.mContainer.getChildCount();
        while (numViews2 > i3) {
            this.mContainer.removeViewAt(numViews2 - 1);
            numViews2--;
        }
        int i4 = 0;
        while (i4 < numViews2) {
            View child = this.mContainer.getChildAt(i4);
            child.findViewById(16908310).setEnabled(i4 < numViews2 + -1);
            int i5 = this.mMaxVisible;
            if (i5 > 0) {
                child.setVisibility(i4 < numViews2 - i5 ? 8 : 0);
                View leftIcon = child.findViewById(R.id.left_icon);
                if (i4 <= numViews2 - this.mMaxVisible || i4 == 0) {
                    i = 8;
                } else {
                    i = 0;
                }
                leftIcon.setVisibility(i);
            }
            i4++;
        }
    }
}
