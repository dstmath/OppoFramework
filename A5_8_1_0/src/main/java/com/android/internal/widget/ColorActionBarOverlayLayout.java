package com.android.internal.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowInsets;
import android.widget.Toolbar;
import com.color.util.ColorContextUtil;
import com.color.util.ColorLog;
import oppo.R;

public class ColorActionBarOverlayLayout extends ActionBarOverlayLayout {
    static final int[] COLOR_ATTRS = new int[]{201392729, 201392728, 201392216, 201392732};
    private boolean isAddTranslucentDecorBgView;
    private ActionBarContainer mActionBarBottom;
    private int mActionBarHeight;
    private ActionBarContainer mActionBarTop;
    private View mBottomMenuView;
    private Drawable mColorBottomWindowContentOverlay;
    private Drawable mColorWindowContentOverlay;
    private View mContent;
    private DecorToolbar mDecorToolbar;
    private boolean mHasNonEmbeddedTabs;
    private int mIdActionBar;
    private int mIdActionBarTop;
    private int mIdActionContextBar;
    private int mIdSplitActionBar;
    private boolean mIgnoreColorBottomWindowContentOverlay;
    private boolean mIgnoreColorWindowContentOverlay;
    private boolean mIsSplitActionBarOverlay;
    private int mStatusBarHeight;
    protected final Class<?> mTagClass;
    private Drawable mTranslucentDecorBackground;
    private View mTranslucentDecorBgView;

    public ColorActionBarOverlayLayout(Context context) {
        this(context, null);
    }

    public ColorActionBarOverlayLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTagClass = getClass();
        this.mIsSplitActionBarOverlay = false;
        this.mActionBarHeight = 0;
        this.mIdActionBar = -1;
        this.mIdSplitActionBar = -1;
        this.mIdActionContextBar = -1;
        this.mIdActionBarTop = -1;
        this.mContent = null;
        this.mDecorToolbar = null;
        this.mActionBarBottom = null;
        this.mActionBarTop = null;
        this.mColorWindowContentOverlay = null;
        this.mColorBottomWindowContentOverlay = null;
        this.mIgnoreColorWindowContentOverlay = false;
        this.mIgnoreColorBottomWindowContentOverlay = false;
        this.mBottomMenuView = null;
        this.mTranslucentDecorBgView = null;
        this.mTranslucentDecorBackground = null;
        this.isAddTranslucentDecorBgView = false;
        this.mStatusBarHeight = 0;
        init(context);
    }

    private void init(Context context) {
        boolean z = true;
        if (isOppoStyle()) {
            TypedArray ta = context.getTheme().obtainStyledAttributes(ATTRS);
            this.mActionBarHeight = ta.getDimensionPixelSize(0, 0);
            ta.recycle();
            TypedArray tb = context.obtainStyledAttributes(R.styleable.OppoTheme);
            this.mIsSplitActionBarOverlay = tb.getBoolean(1, false);
            tb.recycle();
            this.mIdActionBar = ColorContextUtil.getResId(context, 201458895);
            this.mIdSplitActionBar = ColorContextUtil.getResId(context, 201458896);
            this.mIdActionContextBar = ColorContextUtil.getResId(context, 201458900);
            this.mIdActionBarTop = ColorContextUtil.getResId(context, 201458899);
            TypedArray tc = context.getTheme().obtainStyledAttributes(COLOR_ATTRS);
            this.isAddTranslucentDecorBgView = tc.getBoolean(0, false);
            this.mTranslucentDecorBackground = tc.getDrawable(1);
            this.mColorWindowContentOverlay = tc.getDrawable(2);
            this.mColorBottomWindowContentOverlay = tc.getDrawable(3);
            if (!(this.mColorWindowContentOverlay == null && this.mColorBottomWindowContentOverlay == null)) {
                z = false;
            }
            setWillNotDraw(z);
            tc.recycle();
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        init(getContext());
    }

    boolean applyInsets(View view, Rect insets, boolean left, boolean top, boolean bottom, boolean right) {
        if (!isOppoStyle()) {
            return super.applyInsets(view, insets, left, top, bottom, right);
        }
        if (view == this.mContent) {
            boolean stable = (getWindowSystemUiVisibility() & 256) != 0;
            int offset = 0;
            if (stable && this.mHasNonEmbeddedTabs && this.mActionBarTop != null) {
                insets.top += this.mActionBarTop.getMeasuredHeight() - (this.mActionBarHeight * 2);
            }
            if (this.mDecorToolbar.isSplit() && this.mActionBarBottom != null && stable) {
                offset = this.mActionBarBottom.getMeasuredHeight() - this.mActionBarHeight;
            }
            if (isInOverlayMode() || (stable ^ 1) == 0) {
                Rect rect = this.mInnerInsets;
                rect.bottom += offset;
            } else {
                insets.bottom += offset;
            }
            if (this.mDecorToolbar.isSplit() && this.mActionBarBottom != null && this.mIsSplitActionBarOverlay) {
                insets.bottom -= this.mActionBarBottom.getMeasuredHeight();
            }
        }
        updateStatusBarActionBarBg(-1, (this.mActionBarTop != null ? this.mActionBarTop.getMeasuredHeight() : 0) + this.mStatusBarHeight);
        return super.applyInsets(view, insets, left, top, bottom, right);
    }

    void pullChildren() {
        super.pullChildren();
        if (isOppoStyle() && this.mContent == null) {
            this.mContent = findViewById(16908290);
            this.mDecorToolbar = getDecorToolbar(findViewById(this.mIdActionBar));
            this.mActionBarBottom = (ActionBarContainer) findViewById(this.mIdSplitActionBar);
            this.mActionBarTop = (ActionBarContainer) findViewById(this.mIdActionBarTop);
        }
    }

    public void setUiOptions(int uiOptions) {
        if (isOppoStyle()) {
            boolean splitActionBar = false;
            boolean splitWhenNarrow = (uiOptions & 1) != 0;
            if (splitWhenNarrow) {
                splitActionBar = true;
            }
            if (splitActionBar) {
                pullChildren();
                if (this.mActionBarBottom != null && this.mDecorToolbar.canSplit()) {
                    this.mDecorToolbar.setSplitView(this.mActionBarBottom);
                    this.mDecorToolbar.setSplitToolbar(splitActionBar);
                    this.mDecorToolbar.setSplitWhenNarrow(splitWhenNarrow);
                    ActionBarContextView cab = (ActionBarContextView) findViewById(this.mIdActionContextBar);
                    cab.setSplitView(this.mActionBarBottom);
                    cab.setSplitToolbar(splitActionBar);
                    cab.setSplitWhenNarrow(splitWhenNarrow);
                } else if (splitActionBar) {
                    ColorLog.e(this.mTagClass, "Requested split action bar with incompatible window decor! Ignoring request.");
                }
            }
            return;
        }
        super.setUiOptions(uiOptions);
    }

    public void setSplitActionBarOverlay(boolean overlay) {
        this.mIsSplitActionBarOverlay = overlay;
    }

    public void setHasNonEmbeddedTabs(boolean hasNonEmbeddedTabs) {
        this.mHasNonEmbeddedTabs = hasNonEmbeddedTabs;
        super.setHasNonEmbeddedTabs(hasNonEmbeddedTabs);
    }

    private DecorToolbar getDecorToolbar(View view) {
        if (view instanceof DecorToolbar) {
            return (DecorToolbar) view;
        }
        if (view instanceof Toolbar) {
            return ((Toolbar) view).getWrapper();
        }
        throw new IllegalStateException("Can't make a decor toolbar out of " + view.getClass().getSimpleName());
    }

    private void updateStatusBarActionBarBg(int width, int height) {
        if (!this.isAddTranslucentDecorBgView) {
            return;
        }
        if (this.mTranslucentDecorBgView == null) {
            View view = new View(this.mContext);
            view.setVisibility(this.mActionBarTop != null ? this.mActionBarTop.getVisibility() : 0);
            view.setBackgroundDrawable(this.mTranslucentDecorBackground);
            addView(view, 1, (LayoutParams) new ActionBarOverlayLayout.LayoutParams(-1, height));
            this.mTranslucentDecorBgView = view;
            this.mTranslucentDecorBgView.setId(201458966);
            return;
        }
        ((ActionBarOverlayLayout.LayoutParams) this.mTranslucentDecorBgView.getLayoutParams()).height = height;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mTranslucentDecorBgView != null) {
            measureChildWithMargins(this.mTranslucentDecorBgView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mStatusBarHeight = insets.getSystemWindowInsetTop();
        return super.onApplyWindowInsets(insets);
    }

    public void draw(Canvas c) {
        super.draw(c);
    }

    public void setColorWindowContentOverlay(Drawable overlay) {
        this.mColorWindowContentOverlay = overlay;
    }

    public void setColorBottomWindowContentOverlay(Drawable overlay) {
        this.mColorBottomWindowContentOverlay = overlay;
    }

    public void setIgnoreColorWindowContentOverlay(boolean isIgnore) {
        this.mIgnoreColorWindowContentOverlay = isIgnore;
        invalidate();
    }

    public void setIgnoreColorBottomWindowContentOverlay(boolean isIgnore) {
        this.mIgnoreColorBottomWindowContentOverlay = isIgnore;
        invalidate();
    }

    public void setStatusBarActionBarBg(Drawable bg) {
        if (this.isAddTranslucentDecorBgView) {
            this.mTranslucentDecorBackground = bg;
            if (this.mTranslucentDecorBgView != null) {
                this.mTranslucentDecorBgView.setBackgroundDrawable(this.mTranslucentDecorBackground);
            }
        }
    }
}
