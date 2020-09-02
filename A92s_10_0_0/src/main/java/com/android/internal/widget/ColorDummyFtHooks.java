package com.android.internal.widget;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.IColorFtHooks;
import android.widget.ListView;
import com.android.internal.R;

public class ColorDummyFtHooks implements IColorFtHooks {
    @Override // android.widget.IColorFtHooks
    public int getMinOverscrollSize() {
        return 2;
    }

    @Override // android.widget.IColorFtHooks
    public int getMaxOverscrollSize() {
        return 4;
    }

    @Override // android.widget.IColorFtHooks
    public Drawable getArrowDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.ft_avd_tooverflow, context.getTheme());
    }

    @Override // android.widget.IColorFtHooks
    public Drawable getOverflowDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.ft_avd_toarrow, context.getTheme());
    }

    @Override // android.widget.IColorFtHooks
    public AnimatedVectorDrawable getToArrowAnim(Context context) {
        return (AnimatedVectorDrawable) context.getResources().getDrawable(R.drawable.ft_avd_toarrow_animation, context.getTheme());
    }

    @Override // android.widget.IColorFtHooks
    public AnimatedVectorDrawable getToOverflowAnim(Context context) {
        return (AnimatedVectorDrawable) context.getResources().getDrawable(R.drawable.ft_avd_tooverflow_animation, context.getTheme());
    }

    @Override // android.widget.IColorFtHooks
    public int getFirstItemPaddingStart(Context context, int paddingStart) {
        return (int) (((double) paddingStart) * 1.5d);
    }

    @Override // android.widget.IColorFtHooks
    public int getLastItemPaddingEnd(Context context, int paddingEnd) {
        return (int) (((double) paddingEnd) * 1.5d);
    }

    @Override // android.widget.IColorFtHooks
    public void setOverflowMenuCount(int count) {
    }

    @Override // android.widget.IColorFtHooks
    public int calOverflowExtension(int lineHeight) {
        return (int) (((float) lineHeight) * 0.5f);
    }

    @Override // android.widget.IColorFtHooks
    public int getOverflowButtonRes() {
        return R.layout.floating_popup_overflow_button;
    }

    @Override // android.widget.IColorFtHooks
    public void setOverflowScrollBarSize(ListView listview) {
    }

    @Override // android.widget.IColorFtHooks
    public void setConvertViewPosition(int position) {
    }

    @Override // android.widget.IColorFtHooks
    public void setConvertViewPadding(View convertView, boolean openOverflowUpward, int sidePadding, int minimumWidth) {
    }

    @Override // android.widget.IColorFtHooks
    public void setScrollIndicators(ListView listview) {
        listview.setScrollIndicators(3);
    }

    @Override // android.widget.IColorFtHooks
    public int getMenuItemButtonRes() {
        return R.layout.floating_popup_menu_button;
    }

    @Override // android.widget.IColorFtHooks
    public int getButtonTextId() {
        return R.id.floating_toolbar_menu_item_text;
    }

    @Override // android.widget.IColorFtHooks
    public int getButtonIconId() {
        return R.id.floating_toolbar_menu_item_image;
    }

    @Override // android.widget.IColorFtHooks
    public int getContentContainerRes() {
        return R.layout.floating_popup_container;
    }

    @Override // android.widget.IColorFtHooks
    public int getFloatingToolBarHeightRes() {
        return R.dimen.floating_toolbar_height;
    }
}
