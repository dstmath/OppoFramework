package com.android.internal.view.menu;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.view.menu.MenuPresenter.Callback;
import com.android.internal.view.menu.MenuView.ItemView;
import com.color.util.ColorContextUtil;
import java.util.ArrayList;

public abstract class BaseMenuPresenter implements MenuPresenter {
    private Callback mCallback;
    protected Context mContext;
    private int mId;
    protected LayoutInflater mInflater;
    @OppoHook(level = OppoHookType.NEW_FIELD, note = "Changwei.Li@Plf.SDK, 2015-05-21 : Modify for SplitMenu", property = OppoRomType.ROM)
    protected boolean mIsOppoStyle = false;
    private int mItemLayoutRes;
    protected MenuBuilder mMenu;
    private int mMenuLayoutRes;
    protected MenuView mMenuView;
    protected Context mSystemContext;
    protected LayoutInflater mSystemInflater;

    public abstract void bindItemView(MenuItemImpl menuItemImpl, ItemView itemView);

    public BaseMenuPresenter(Context context, int menuLayoutRes, int itemLayoutRes) {
        this.mSystemContext = context;
        this.mSystemInflater = LayoutInflater.from(context);
        this.mMenuLayoutRes = menuLayoutRes;
        this.mItemLayoutRes = itemLayoutRes;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Changwei.Li@Plf.SDK, 2015-06-09 : Modify for SplitMenu", property = OppoRomType.ROM)
    public void initForMenu(Context context, MenuBuilder menu) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        this.mMenu = menu;
        this.mIsOppoStyle = ColorContextUtil.isOppoStyle(this.mContext);
    }

    public MenuView getMenuView(ViewGroup root) {
        if (this.mMenuView == null) {
            this.mMenuView = (MenuView) this.mSystemInflater.inflate(this.mMenuLayoutRes, root, false);
            this.mMenuView.initialize(this.mMenu);
            updateMenuView(true);
        }
        return this.mMenuView;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Changwei.Li@Plf.SDK, 2015-06-26 : Modify for SplitMenu", property = OppoRomType.ROM)
    public void updateMenuView(boolean cleared) {
        ViewGroup parent = this.mMenuView;
        if (parent != null) {
            int childIndex = 0;
            if (this.mMenu != null) {
                this.mMenu.flagActionItems();
                ArrayList<MenuItemImpl> visibleItems = this.mMenu.getVisibleItems();
                int itemCount = visibleItems.size();
                for (int i = 0; i < itemCount; i++) {
                    MenuItemImpl item = (MenuItemImpl) visibleItems.get(i);
                    View convertView;
                    MenuItemImpl oldItem;
                    View itemView;
                    if (this.mIsOppoStyle) {
                        if (shouldIncludeItem(childIndex, item) && item.requiresActionButton()) {
                            convertView = parent.getChildAt(childIndex);
                            oldItem = convertView instanceof ItemView ? ((ItemView) convertView).getItemData() : null;
                            itemView = getItemView(item, convertView, parent);
                            if (item != oldItem) {
                                itemView.setPressed(false);
                                itemView.jumpDrawablesToCurrentState();
                            }
                            if (itemView != convertView) {
                                addItemView(itemView, childIndex);
                            }
                            childIndex++;
                        }
                    } else if (shouldIncludeItem(childIndex, item)) {
                        convertView = parent.getChildAt(childIndex);
                        oldItem = convertView instanceof ItemView ? ((ItemView) convertView).getItemData() : null;
                        itemView = getItemView(item, convertView, parent);
                        if (item != oldItem) {
                            itemView.setPressed(false);
                            itemView.jumpDrawablesToCurrentState();
                        }
                        if (itemView != convertView) {
                            addItemView(itemView, childIndex);
                        }
                        childIndex++;
                    }
                }
            }
            while (childIndex < parent.getChildCount()) {
                if (!filterLeftoverView(parent, childIndex)) {
                    childIndex++;
                }
            }
        }
    }

    protected void addItemView(View itemView, int childIndex) {
        ViewGroup currentParent = (ViewGroup) itemView.getParent();
        if (currentParent != null) {
            currentParent.removeView(itemView);
        }
        ((ViewGroup) this.mMenuView).addView(itemView, childIndex);
    }

    protected boolean filterLeftoverView(ViewGroup parent, int childIndex) {
        parent.removeViewAt(childIndex);
        return true;
    }

    public void setCallback(Callback cb) {
        this.mCallback = cb;
    }

    public Callback getCallback() {
        return this.mCallback;
    }

    public ItemView createItemView(ViewGroup parent) {
        return (ItemView) this.mSystemInflater.inflate(this.mItemLayoutRes, parent, false);
    }

    public View getItemView(MenuItemImpl item, View convertView, ViewGroup parent) {
        ItemView itemView;
        if (convertView instanceof ItemView) {
            itemView = (ItemView) convertView;
        } else {
            itemView = createItemView(parent);
        }
        bindItemView(item, itemView);
        return (View) itemView;
    }

    public boolean shouldIncludeItem(int childIndex, MenuItemImpl item) {
        return true;
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (this.mCallback != null) {
            this.mCallback.onCloseMenu(menu, allMenusAreClosing);
        }
    }

    public boolean onSubMenuSelected(SubMenuBuilder menu) {
        if (this.mCallback != null) {
            return this.mCallback.onOpenSubMenu(menu);
        }
        return false;
    }

    public boolean flagActionItems() {
        return false;
    }

    public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    public int getId() {
        return this.mId;
    }

    public void setId(int id) {
        this.mId = id;
    }
}
