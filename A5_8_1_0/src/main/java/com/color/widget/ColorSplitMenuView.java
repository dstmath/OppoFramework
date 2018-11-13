package com.color.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.IntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.widget.Button;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.widget.ExploreByTouchHelper;
import com.color.util.ColorDialogUtil;
import com.color.util.ColorLog;
import com.color.view.ColorMenuBuilder;
import com.color.view.ColorMenuInflater;
import com.color.view.ColorMenuItemImpl;
import com.color.view.ColorMenuPresenter;
import com.color.widget.ColorBottomMenuView.DrawItem;
import java.util.ArrayList;
import java.util.List;

public class ColorSplitMenuView extends ColorBottomMenuView {
    private Context mContext;
    ColorSplitMenuViewDrawHelper mDrawHelper;
    private boolean mForcePerformItemClick;
    private int mLastTabItemPosition;
    private List<MenuItem> mMenuItems;
    private MenuPresenter mMenuPresenter;
    private OnItemClickListener mOnItemClickListener;
    private OnPrepareMenuListener mOnPrepareMenuListener;
    private ColorMenuPresenter mSplitMenuPresenter;
    private TabSelectedCallback mTabSelectedCallback;
    private int mTabSelectedPosition;
    private final SplitMenuViewTouchHelper mTouchHelper;
    private String mUpdateString1;
    private String mUpdateString2;

    public interface OnItemClickListener {
        void onItemClick(MenuItem menuItem);
    }

    public interface OnPrepareMenuListener {
        void onPrepareMenu(Menu menu);
    }

    private final class SplitMenuViewTouchHelper extends ExploreByTouchHelper {
        private Rect mTempRect = new Rect();
        private int mVirtualViewAt = -1;

        public SplitMenuViewTouchHelper(View forView) {
            super(forView);
        }

        protected int getVirtualViewAt(float x, float y) {
            int id = ColorSplitMenuView.this.getTouchedPosition((int) x, (int) y);
            this.mVirtualViewAt = id;
            return id;
        }

        protected void getVisibleVirtualViews(IntArray virtualViewIds) {
            for (int i = 0; i < ColorSplitMenuView.this.mCurrItems.size(); i++) {
                virtualViewIds.add(i);
            }
        }

        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.getText().add(getItemDescription(virtualViewId));
        }

        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(host, event);
        }

        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            node.setContentDescription(getItemDescription(virtualViewId));
            node.addAction(AccessibilityAction.ACTION_CLICK);
            ColorSplitMenuView.this.setRectBounds(virtualViewId, this.mTempRect);
            node.setBoundsInParent(this.mTempRect);
            node.setClassName(Button.class.getName());
            if (virtualViewId == ColorSplitMenuView.this.mTabSelectedPosition) {
                node.setSelected(true);
            }
            if (virtualViewId == getDisablePosition(virtualViewId)) {
                node.setEnabled(false);
            }
        }

        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            switch (action) {
                case ColorDialogUtil.BIT_FOUSED_BUTTON_NEGATIVE /*16*/:
                    return onItemClicked(virtualViewId);
                default:
                    return false;
            }
        }

        boolean onItemClicked(int index) {
            invalidateVirtualView(index);
            ColorSplitMenuView.this.performClick(index);
            sendEventForVirtualView(index, 1);
            return false;
        }

        public int getDisablePosition(int virtualViewId) {
            MenuItem item = null;
            if (virtualViewId >= 0) {
                DrawItem drawItem = ColorSplitMenuView.this.getDrawItem(virtualViewId);
                if (drawItem != null) {
                    item = drawItem.getMenuItem();
                }
            }
            if (item == null || (item.isEnabled() ^ 1) == 0) {
                return -1;
            }
            return virtualViewId;
        }

        public CharSequence getItemDescription(int virtualViewId) {
            CharSequence temp = "";
            int mode = -1;
            MenuItem item = null;
            if (virtualViewId >= 0) {
                DrawItem drawItem = ColorSplitMenuView.this.getDrawItem(virtualViewId);
                if (drawItem != null) {
                    item = drawItem.getMenuItem();
                    if (item != null && (item instanceof ColorMenuItemImpl)) {
                        mode = ((ColorMenuItemImpl) item).getPointMode();
                    }
                }
            }
            if (mode == 1) {
                temp = ColorSplitMenuView.this.mUpdateString2;
            } else if (item != null && (item instanceof ColorMenuItemImpl) && mode == 2) {
                temp = ((ColorMenuItemImpl) item).getPointNumber() + "" + ColorSplitMenuView.this.mUpdateString1;
            }
            return ColorSplitMenuView.this.getMenuTitle(virtualViewId) + "" + temp;
        }
    }

    public interface TabSelectedCallback {
        int getSelectedTab();
    }

    public ColorSplitMenuView(Context context) {
        this(context, null);
    }

    public ColorSplitMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393240);
    }

    public ColorSplitMenuView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDrawHelper = null;
        this.mOnItemClickListener = null;
        this.mOnPrepareMenuListener = null;
        this.mTabSelectedCallback = null;
        this.mMenuItems = new ArrayList();
        this.mMenuPresenter = new ColorSplitMenuPresenter(this);
        this.mSplitMenuPresenter = new ColorSplitBottomMenuPresenter(this);
        this.mTabSelectedPosition = -1;
        this.mLastTabItemPosition = -1;
        this.mForcePerformItemClick = false;
        this.mContext = null;
        this.mUpdateString1 = null;
        this.mUpdateString2 = null;
        this.mDrawHelper = new ColorSplitMenuViewDrawHelper(context, this.mCurrItems);
        this.mTouchHelper = new SplitMenuViewTouchHelper(this);
        setAccessibilityDelegate(this.mTouchHelper);
        this.mUpdateString1 = context.getString(201590168);
        this.mUpdateString2 = context.getString(201590169);
        this.mContext = context;
    }

    public void update(List<MenuItem> menuItems) {
        update((List) menuItems, true);
    }

    public void update(int menuRes, boolean cleared) {
        ColorMenuBuilder menu = new ColorMenuBuilder(getContext());
        new ColorMenuInflater(getContext()).inflate(menuRes, menu);
        bindSplitMenu(menu);
        if (this.mOnPrepareMenuListener != null) {
            this.mOnPrepareMenuListener.onPrepareMenu(menu);
        }
        update(ColorBottomMenuDelegate.getSplitMenuItems(menu), cleared);
    }

    public void update(int menuRes) {
        update(menuRes, true);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnPrepareMenuListener(OnPrepareMenuListener listener) {
        this.mOnPrepareMenuListener = listener;
    }

    public void setTabSelectedCallback(TabSelectedCallback callback) {
        this.mTabSelectedCallback = callback;
    }

    public List<MenuItem> getMenuItems() {
        return this.mMenuItems;
    }

    public void setForcePerformItemClick(boolean force) {
        this.mForcePerformItemClick = force;
    }

    public void update(List<MenuItem> menuItems, boolean cleared) {
        if (!(this.mTabSelectedCallback == null || menuItems == null)) {
            int count = menuItems.size();
            int selected = this.mTabSelectedCallback.getSelectedTab();
            if (selected >= 0 && selected < count) {
                for (int i = 0; i < count; i++) {
                    MenuItem menuItem = (MenuItem) menuItems.get(i);
                    if (menuItem.isEnabled() && menuItem.isCheckable()) {
                        boolean z;
                        if (i == selected) {
                            z = true;
                        } else {
                            z = false;
                        }
                        menuItem.setChecked(z);
                    }
                }
            }
        }
        super.update(menuItems, cleared);
    }

    boolean performItemClick(MenuItem item) {
        if (this.mOnItemClickListener == null) {
            return false;
        }
        this.mOnItemClickListener.onItemClick(item);
        return true;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mDrawHelper != null) {
            this.mDrawHelper.draw(canvas);
        }
    }

    void updateNextItems(List<MenuItem> menuItems) {
        int selectedPosition = -1;
        if (menuItems != this.mMenuItems) {
            this.mMenuItems.clear();
        }
        if (menuItems != null) {
            if (menuItems != this.mMenuItems) {
                this.mMenuItems.addAll(menuItems);
            }
            int count = menuItems.size();
            boolean isTabMode = false;
            for (int i = 0; i < count; i++) {
                MenuItem item = (MenuItem) menuItems.get(i);
                if (item.isEnabled()) {
                    if (item.isCheckable()) {
                        isTabMode = true;
                        this.mLastTabItemPosition = i;
                    }
                    if (item.isChecked()) {
                        this.mTabSelectedPosition = i;
                    }
                }
            }
            if (isTabMode) {
                if (this.mTabSelectedPosition < 0) {
                    this.mTabSelectedPosition = this.mLastTabItemPosition;
                } else {
                    selectedPosition = this.mTabSelectedPosition;
                }
            }
        }
        super.updateNextItems(menuItems);
        setItemSelectedInternal(getDrawItemInternal(this.mNextItems, selectedPosition), true, false);
    }

    void onPerformClick(int position) {
        DrawItem drawItem = getDrawItem(position);
        if (drawItem == null) {
            ColorLog.d("log.key.bottom_menu.press", this.mTagClass, new Object[]{"not valid position!"});
        } else if (position != this.mDownTouchedPosition) {
            ColorLog.d("log.key.bottom_menu.press", this.mTagClass, new Object[]{"selected position different from the down touched!"});
        } else if (isItemEnabledInternal(drawItem)) {
            if (isTabItemInternal(drawItem)) {
                ColorLog.d("log.key.bottom_menu.press", this.mTagClass, new Object[]{"is tab item!"});
                if (position != this.mTabSelectedPosition || this.mForcePerformItemClick) {
                    setItemSelected(this.mTabSelectedPosition, false);
                    this.mTabSelectedPosition = position;
                    setItemSelected(this.mTabSelectedPosition, true);
                    super.onPerformClick(position);
                }
            } else {
                ColorLog.d("log.key.bottom_menu.press", this.mTagClass, new Object[]{"not tab item!"});
                super.onPerformClick(position);
            }
        } else {
            ColorLog.d("log.key.bottom_menu.press", this.mTagClass, new Object[]{"not enabled item!"});
        }
    }

    public void unbindMenu(Menu menu) {
        ((MenuBuilder) menu).removeMenuPresenter(this.mMenuPresenter);
    }

    public void bindMenu(Menu menu) {
        ((MenuBuilder) menu).addMenuPresenter(this.mMenuPresenter);
    }

    public void unbindSplitMenu(Menu menu) {
        ((ColorMenuBuilder) menu).removeMenuPresenter(this.mSplitMenuPresenter);
    }

    public void bindSplitMenu(Menu menu) {
        ((ColorMenuBuilder) menu).addMenuPresenter(this.mSplitMenuPresenter);
    }

    private boolean isTabItemInternal(DrawItem drawItem) {
        if (drawItem == null) {
            return false;
        }
        return drawItem.getMenuItem().isCheckable();
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mTouchHelper == null || !this.mTouchHelper.dispatchHoverEvent(event)) {
            return super.dispatchHoverEvent(event);
        }
        return true;
    }
}
