package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;
import android.widget.ActionMenuView.LayoutParams;
import com.android.internal.view.menu.ActionMenuItemView;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.widget.ColorActionBarContextView;
import com.android.internal.widget.ColorActionBarView;
import com.color.widget.ColorOptionMenuPresenter;
import java.util.ArrayList;
import java.util.List;

public class ColorActionMenuView extends ActionMenuView {
    private static final String TAG = "ColorActionMenuView";
    private final int MAX_MENU_ITEM_COUNT;
    private boolean mIsSameSide;
    private int mItemSpacing;
    private MenuBuilder mMenu;
    private int mMenuViewPadding;
    private List<Class<?>> mPresenterClasses;

    public ColorActionMenuView(Context context) {
        this(context, null);
    }

    public ColorActionMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.MAX_MENU_ITEM_COUNT = 5;
        this.mMenu = null;
        this.mPresenterClasses = new ArrayList();
        this.mIsSameSide = true;
        this.mMenuViewPadding = 0;
        if (isOppoStyle()) {
            this.mMenuViewPadding = getResources().getDimensionPixelSize(201655457);
            this.mItemSpacing = getResources().getDimensionPixelSize(201655498);
            this.mPresenterClasses.add(ColorOptionMenuPresenter.class);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isOppoStyle() || this.mMenu == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        List<MenuPresenter> presenters = this.mMenu.removeMenuPresenters(this.mPresenterClasses);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mMenu.restoreMenuPresenters(presenters);
        int childCount = getChildCount();
        int padding = this.mItemSpacing;
        if (childCount > 0) {
            View child = getChildAt(childCount - 1);
            if ((child instanceof ActionMenuItemView) && ((ActionMenuItemView) child).hasText()) {
                padding = this.mMenuViewPadding;
            }
        }
        setPadding(padding, getPaddingTop(), padding, getPaddingBottom());
        if (getParent() instanceof ColorActionBarView) {
            ColorActionBarView view = (ColorActionBarView) getParent();
            if (view.getMainActionBar() || view.isUpViewVisible()) {
                this.mIsSameSide = true;
            } else {
                this.mIsSameSide = false;
            }
        } else if (getParent() instanceof ColorActionBarContextView) {
            this.mIsSameSide = false;
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (isOppoStyle() && (this.mFormatItems ^ 1) == 0) {
            int i;
            int visibleCount = 0;
            int count = getChildCount();
            for (i = 0; i < count; i++) {
                if (getChildAt(i).getVisibility() != 8) {
                    visibleCount++;
                }
            }
            if (visibleCount > 5) {
                super.onLayout(changed, left, top, right, bottom);
                return;
            }
            View v;
            int height;
            int t;
            int childCount = getChildCount();
            int midVertical = (top + bottom) / 2;
            int dividerWidth = getDividerWidth();
            int nonOverflowWidth = 0;
            int nonOverflowCount = 0;
            int widthRemaining = ((right - left) - getPaddingRight()) - getPaddingLeft();
            boolean hasOverflow = false;
            boolean isLayoutRtl = isLayoutRtl();
            for (i = 0; i < childCount; i++) {
                v = getChildAt(i);
                if (v.getVisibility() != 8) {
                    LayoutParams p = (LayoutParams) v.getLayoutParams();
                    if (p.isOverflowButton) {
                        int l;
                        int r;
                        int overflowWidth = v.getMeasuredWidth();
                        if (hasDividerBeforeChildAt(i)) {
                            overflowWidth += dividerWidth;
                        }
                        height = v.getMeasuredHeight();
                        if (isLayoutRtl) {
                            l = getPaddingLeft() + p.leftMargin;
                            r = l + overflowWidth;
                        } else {
                            r = (getWidth() - getPaddingRight()) - p.rightMargin;
                            l = r - overflowWidth;
                        }
                        t = midVertical - (height / 2);
                        v.layout(l, t, r, t + height);
                        widthRemaining -= overflowWidth;
                        hasOverflow = true;
                    } else {
                        int size = (v.getMeasuredWidth() + p.leftMargin) + p.rightMargin;
                        nonOverflowWidth += size;
                        widthRemaining -= size;
                        if (hasDividerBeforeChildAt(i)) {
                            nonOverflowWidth += dividerWidth;
                        }
                        nonOverflowCount++;
                    }
                }
            }
            int startRight;
            LayoutParams lp;
            int width;
            int startLeft;
            if (childCount != 1 || (hasOverflow ^ 1) == 0) {
                if (nonOverflowCount - (hasOverflow ? 0 : 1) <= 0) {
                    widthRemaining = 0;
                }
                int spacerSize = Math.max(0, widthRemaining);
                if (isLayoutRtl) {
                    startRight = getWidth() - getPaddingRight();
                    if (this.mIsSameSide) {
                        startRight = ((getWidth() - getPaddingRight()) - spacerSize) + (this.mItemSpacing * (count - 1));
                    }
                    for (i = 0; i < childCount; i++) {
                        v = getChildAt(i);
                        lp = (LayoutParams) v.getLayoutParams();
                        if (!(v.getVisibility() == 8 || lp.isOverflowButton)) {
                            startRight -= lp.rightMargin;
                            width = v.getMeasuredWidth();
                            height = v.getMeasuredHeight();
                            t = midVertical - (height / 2);
                            if (this.mIsSameSide) {
                                v.layout(startRight - width, t, startRight, t + height);
                                startRight -= (lp.leftMargin + width) + this.mItemSpacing;
                            } else {
                                v.layout(startRight - width, t, startRight, t + height);
                                startRight -= (lp.leftMargin + width) + getSpacerSize(i, childCount, spacerSize);
                            }
                        }
                    }
                } else {
                    startLeft = getPaddingLeft();
                    if (this.mIsSameSide) {
                        startLeft = (getPaddingLeft() + spacerSize) - (this.mItemSpacing * (count - 1));
                    }
                    for (i = 0; i < childCount; i++) {
                        v = getChildAt(i);
                        lp = (LayoutParams) v.getLayoutParams();
                        if (!(v.getVisibility() == 8 || lp.isOverflowButton)) {
                            startLeft += lp.leftMargin;
                            width = v.getMeasuredWidth();
                            height = v.getMeasuredHeight();
                            t = midVertical - (height / 2);
                            if (this.mIsSameSide) {
                                v.layout(startLeft, t, startLeft + width, t + height);
                                startLeft += (lp.rightMargin + width) + this.mItemSpacing;
                            } else {
                                v.layout(startLeft, t, startLeft + width, t + height);
                                startLeft += (lp.rightMargin + width) + getSpacerSize(i, childCount, spacerSize);
                            }
                        }
                    }
                }
                return;
            }
            v = getChildAt(0);
            width = v.getMeasuredWidth();
            height = v.getMeasuredHeight();
            t = midVertical - (height / 2);
            lp = (LayoutParams) v.getLayoutParams();
            if (isLayoutRtl) {
                startLeft = getPaddingLeft() + lp.leftMargin;
                v.layout(startLeft, t, startLeft + width, t + height);
            } else {
                startRight = (getWidth() - getPaddingRight()) - lp.rightMargin;
                v.layout(startRight - width, t, startRight, t + height);
            }
            return;
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    public Menu getMenu() {
        if (!isOppoStyle()) {
            return super.getMenu();
        }
        this.mMenu = (MenuBuilder) super.getMenu();
        return this.mMenu;
    }

    void onMeasureExactFormat(int widthMeasureSpec, int heightMeasureSpec) {
        if (isOppoStyle()) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
                lp.leftMargin = 0;
                lp.rightMargin = 0;
            }
            superOnMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        super.onMeasureExactFormat(widthMeasureSpec, heightMeasureSpec);
    }

    public void initialize(MenuBuilder menu) {
        if (isOppoStyle()) {
            this.mMenu = menu;
        }
        super.initialize(menu);
    }

    private void superOnMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getOrientation() == 1) {
            measureVertical(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private int getSpacerSize(int index, int count, int spacerSize) {
        int value = ((count - index) - 1) * 2;
        if (!(value == count || value == count + 1)) {
            spacerSize = 0;
        }
        if (count == 3) {
            if (index == 0) {
                spacerSize -= this.mItemSpacing;
            }
            if (index == 1) {
                spacerSize += this.mItemSpacing;
            }
        }
        if (count == 4) {
            if (index == 0 || index == 2) {
                spacerSize += this.mItemSpacing;
            }
            if (index == 1) {
                spacerSize -= this.mItemSpacing * 2;
            }
        }
        if (count != 5) {
            return spacerSize;
        }
        if (index == 0) {
            spacerSize += this.mItemSpacing;
        }
        if (index == 1) {
            spacerSize -= this.mItemSpacing * 3;
        }
        if (index == 2) {
            spacerSize += this.mItemSpacing;
        }
        if (index == 3) {
            return spacerSize + this.mItemSpacing;
        }
        return spacerSize;
    }
}
