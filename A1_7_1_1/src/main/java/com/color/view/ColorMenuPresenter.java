package com.color.view;

import android.content.Context;
import android.os.Parcelable;
import android.view.ViewGroup;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.SubMenuBuilder;

public interface ColorMenuPresenter {

    public interface Callback {
        void onCloseMenu(ColorMenuBuilder colorMenuBuilder, boolean z);

        boolean onOpenSubMenu(ColorMenuBuilder colorMenuBuilder);
    }

    boolean collapseItemActionView(ColorMenuBuilder colorMenuBuilder, ColorMenuItemImpl colorMenuItemImpl);

    boolean expandItemActionView(ColorMenuBuilder colorMenuBuilder, ColorMenuItemImpl colorMenuItemImpl);

    boolean flagActionItems();

    int getId();

    MenuView getMenuView(ViewGroup viewGroup);

    void initForMenu(Context context, ColorMenuBuilder colorMenuBuilder);

    void onCloseMenu(ColorMenuBuilder colorMenuBuilder, boolean z);

    void onRestoreInstanceState(Parcelable parcelable);

    Parcelable onSaveInstanceState();

    boolean onSubMenuSelected(SubMenuBuilder subMenuBuilder);

    void setCallback(Callback callback);

    void updateMenuView(boolean z);
}
