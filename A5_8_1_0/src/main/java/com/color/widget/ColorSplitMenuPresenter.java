package com.color.widget;

import android.content.Context;
import android.os.Parcelable;
import android.view.ViewGroup;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.MenuPresenter.Callback;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.SubMenuBuilder;
import java.util.List;

public class ColorSplitMenuPresenter implements MenuPresenter {
    private MenuBuilder mMenu = null;
    private ColorSplitMenuView mMenuView = null;

    public ColorSplitMenuPresenter(ColorSplitMenuView menuView) {
        this.mMenuView = menuView;
    }

    public void initForMenu(Context context, MenuBuilder menu) {
        this.mMenu = menu;
    }

    public MenuView getMenuView(ViewGroup root) {
        if (this.mMenuView instanceof MenuView) {
            return (MenuView) this.mMenuView;
        }
        return null;
    }

    public void updateMenuView(boolean cleared) {
        if (this.mMenuView != null && this.mMenu != null) {
            this.mMenuView.update(ColorBottomMenuDelegate.getMenuItems(this.mMenu), cleared);
        }
    }

    public void setCallback(Callback cb) {
    }

    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        return false;
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
    }

    public boolean flagActionItems() {
        if (this.mMenu == null) {
            return false;
        }
        List<MenuItemImpl> visibleItems = this.mMenu.getVisibleItems();
        int itemsSize = visibleItems.size();
        int bottomCount = 0;
        for (int i = 0; i < itemsSize; i++) {
            MenuItemImpl item = (MenuItemImpl) visibleItems.get(i);
            if (!item.requiresActionButton()) {
                boolean z;
                bottomCount++;
                if (bottomCount <= 5) {
                    z = true;
                } else {
                    z = false;
                }
                item.setIsActionButton(z);
            }
        }
        return true;
    }

    public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    public int getId() {
        return -1;
    }

    public Parcelable onSaveInstanceState() {
        return null;
    }

    public void onRestoreInstanceState(Parcelable state) {
    }
}
