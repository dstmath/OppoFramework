package com.color.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import com.android.internal.view.menu.BaseMenuPresenter;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuBuilder.ItemInvoker;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuView;
import com.android.internal.widget.ColorViewExplorerByTouchHelper;
import com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction;
import com.color.widget.ColorBottomMenuView.DrawItem;

public class ColorOptionMenuView extends ColorBottomMenuView implements ItemInvoker, MenuView {
    private ColorViewTalkBalkInteraction mColorViewTalkBalkInteraction;
    private MenuBuilder mMenu;
    private BaseMenuPresenter mPresenter;
    private ColorViewExplorerByTouchHelper mTouchHelper;

    public ColorOptionMenuView(Context context) {
        this(context, null);
    }

    public ColorOptionMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393240);
    }

    public ColorOptionMenuView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPresenter = null;
        this.mMenu = null;
        this.mColorViewTalkBalkInteraction = new ColorViewTalkBalkInteraction() {
            private int mVirtualViewAt = -1;

            public int getVirtualViewAt(float x, float y) {
                int day = ColorOptionMenuView.this.getTouchedPosition((int) x, (int) y);
                this.mVirtualViewAt = day;
                return day;
            }

            public void getItemBounds(int position, Rect rect) {
                ColorOptionMenuView.this.setRectBounds(position, rect);
            }

            public CharSequence getItemDescription(int virtualViewId) {
                return ColorOptionMenuView.this.getMenuTitle(virtualViewId);
            }

            public void performAction(int virtualViewId, int actiontype, boolean resolvePara) {
                ColorOptionMenuView.this.performClick(virtualViewId);
                ColorOptionMenuView.this.mTouchHelper.sendEventForVirtualView(virtualViewId, 1);
            }

            public int getCurrentPosition() {
                return ColorOptionMenuView.this.mDownTouchedPosition;
            }

            public int getItemCounts() {
                return ColorOptionMenuView.this.mCurrItems.size();
            }

            public CharSequence getClassName() {
                return Button.class.getName();
            }

            public int getDisablePosition() {
                MenuItem item = null;
                if (this.mVirtualViewAt >= 0) {
                    DrawItem drawItem = ColorOptionMenuView.this.getDrawItem(this.mVirtualViewAt);
                    if (drawItem != null) {
                        item = drawItem.getMenuItem();
                    }
                }
                if (item == null || (item.isEnabled() ^ 1) == 0) {
                    return -1;
                }
                return this.mVirtualViewAt;
            }
        };
        this.mTouchHelper = new ColorViewExplorerByTouchHelper(this);
        this.mTouchHelper.setColorViewTalkBalkInteraction(this.mColorViewTalkBalkInteraction);
        setAccessibilityDelegate(this.mTouchHelper);
    }

    boolean performItemClick(MenuItem item) {
        if (peekMenu() != null) {
            return invokeItem((MenuItemImpl) item);
        }
        return false;
    }

    public boolean invokeItem(MenuItemImpl item) {
        return this.mMenu.performItemAction(item, 0);
    }

    public int getWindowAnimations() {
        return 0;
    }

    public void setPresenter(BaseMenuPresenter presenter) {
        this.mPresenter = presenter;
    }

    public BaseMenuPresenter getPresenter() {
        return this.mPresenter;
    }

    public MenuBuilder peekMenu() {
        return this.mMenu;
    }

    public void initialize(MenuBuilder menu) {
        this.mMenu = menu;
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mTouchHelper == null || !this.mTouchHelper.dispatchHoverEvent(event)) {
            return super.dispatchHoverEvent(event);
        }
        return true;
    }

    public void clearAccessibilityFocus() {
        if (this.mTouchHelper != null) {
            this.mTouchHelper.clearFocusedVirtualView();
        }
    }
}
