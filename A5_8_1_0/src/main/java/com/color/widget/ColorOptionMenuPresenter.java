package com.color.widget;

import android.animation.Animator;
import android.animation.AnimatorSet.Builder;
import android.content.Context;
import android.os.Parcelable;
import android.view.ViewGroup;
import com.android.internal.view.menu.BaseMenuPresenter;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.MenuView.ItemView;

public class ColorOptionMenuPresenter extends BaseMenuPresenter {
    public ColorOptionMenuPresenter(Context context) {
        super(context, 201917527, 0);
    }

    public MenuView getMenuView(ViewGroup root) {
        MenuView result = super.getMenuView(root);
        ((ColorOptionMenuView) result).setPresenter(this);
        return result;
    }

    public void updateMenuView(boolean cleared) {
        updateMenuView(cleared, null);
    }

    public void bindItemView(MenuItemImpl item, ItemView itemView) {
    }

    public void onRestoreInstanceState(Parcelable state) {
    }

    public Parcelable onSaveInstanceState() {
        return null;
    }

    public void updateMenuView(boolean cleared, Builder b) {
        if (this.mMenuView != null && this.mMenu != null) {
            Animator anim = ((ColorOptionMenuView) this.mMenuView).getUpdater(ColorBottomMenuDelegate.getMenuItems(this.mMenu), cleared);
            if (anim != null) {
                if (b != null) {
                    b.with(anim);
                } else {
                    anim.start();
                }
            }
        }
    }
}
