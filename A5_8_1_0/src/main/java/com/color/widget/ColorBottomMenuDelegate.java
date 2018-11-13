package com.color.widget;

import android.view.Menu;
import android.view.MenuItem;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.color.view.ColorMenuBuilder;
import com.color.view.ColorMenuItemImpl;
import java.util.ArrayList;
import java.util.List;

public class ColorBottomMenuDelegate {
    public static List<MenuItem> getMenuItems(Menu menu) {
        MenuBuilder builder = (MenuBuilder) menu;
        List<MenuItem> menuItems = new ArrayList();
        builder.flagActionItems();
        List<MenuItemImpl> visibleItems = builder.getVisibleItems();
        int itemCount = visibleItems.size();
        for (int i = 0; i < itemCount; i++) {
            MenuItemImpl item = (MenuItemImpl) visibleItems.get(i);
            if (item.isActionButton() && (item.requiresActionButton() ^ 1) != 0) {
                menuItems.add(item);
            }
        }
        return menuItems;
    }

    public static List<MenuItem> getSplitMenuItems(Menu menu) {
        ColorMenuBuilder builder = (ColorMenuBuilder) menu;
        List<MenuItem> menuItems = new ArrayList();
        builder.flagActionItems();
        List<ColorMenuItemImpl> visibleItems = builder.getVisibleItems();
        int itemCount = visibleItems.size();
        for (int i = 0; i < itemCount; i++) {
            ColorMenuItemImpl item = (ColorMenuItemImpl) visibleItems.get(i);
            if (item.isActionButton() && (item.requiresActionButton() ^ 1) != 0) {
                menuItems.add(item);
            }
        }
        return menuItems;
    }
}
