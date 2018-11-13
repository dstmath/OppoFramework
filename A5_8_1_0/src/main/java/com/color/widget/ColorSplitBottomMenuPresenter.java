package com.color.widget;

import android.content.Context;
import android.os.Parcelable;
import android.view.ViewGroup;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.SubMenuBuilder;
import com.color.view.ColorMenuBuilder;
import com.color.view.ColorMenuItemImpl;
import com.color.view.ColorMenuPresenter;
import com.color.view.ColorMenuPresenter.Callback;
import java.util.List;

public class ColorSplitBottomMenuPresenter implements ColorMenuPresenter {
    private ColorMenuBuilder mMenu = null;
    private ColorSplitMenuView mMenuView = null;

    public ColorSplitBottomMenuPresenter(ColorSplitMenuView menuView) {
        this.mMenuView = menuView;
    }

    public void initForMenu(Context context, ColorMenuBuilder menu) {
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
            this.mMenuView.update(ColorBottomMenuDelegate.getSplitMenuItems(this.mMenu), cleared);
        }
    }

    public void setCallback(Callback cb) {
    }

    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        return false;
    }

    public void onCloseMenu(ColorMenuBuilder menu, boolean allMenusAreClosing) {
    }

    public boolean flagActionItems() {
        if (this.mMenu == null) {
            return false;
        }
        List<ColorMenuItemImpl> visibleItems = this.mMenu.getVisibleItems();
        int itemsSize = visibleItems.size();
        int bottomCount = 0;
        for (int i = 0; i < itemsSize; i++) {
            ColorMenuItemImpl item = (ColorMenuItemImpl) visibleItems.get(i);
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

    public boolean expandItemActionView(ColorMenuBuilder menu, ColorMenuItemImpl item) {
        return false;
    }

    public boolean collapseItemActionView(ColorMenuBuilder menu, ColorMenuItemImpl item) {
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
