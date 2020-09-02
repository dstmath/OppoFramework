package com.android.internal.widget;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.IColorFtHooks;
import android.widget.ListView;

public class ColorFtHooksImpl implements IColorFtHooks {
    private static int mConvertViewPosition;
    private static int mOverFlowMenuCount;

    public int getMinOverscrollSize() {
        return 3;
    }

    public int getMaxOverscrollSize() {
        return 3;
    }

    public Drawable getArrowDrawable(Context context) {
        return context.getResources().getDrawable(201852313, context.getTheme());
    }

    public Drawable getOverflowDrawable(Context context) {
        return context.getResources().getDrawable(201852320, context.getTheme());
    }

    public AnimatedVectorDrawable getToArrowAnim(Context context) {
        return (AnimatedVectorDrawable) context.getResources().getDrawable(201852321, context.getTheme());
    }

    public AnimatedVectorDrawable getToOverflowAnim(Context context) {
        return (AnimatedVectorDrawable) context.getResources().getDrawable(201852322, context.getTheme());
    }

    public int getFirstItemPaddingStart(Context context, int paddingStart) {
        return context.getResources().getDimensionPixelSize(201655793) + paddingStart;
    }

    public int getLastItemPaddingEnd(Context context, int paddingEnd) {
        return context.getResources().getDimensionPixelSize(201655794) + paddingEnd;
    }

    public void setOverflowMenuCount(int count) {
        mOverFlowMenuCount = count;
    }

    public int calOverflowExtension(int lineHeight) {
        return 0;
    }

    public int getOverflowButtonRes() {
        return 201917613;
    }

    public void setOverflowScrollBarSize(ListView listview) {
        setListViewBackground(listview);
        listview.setScrollBarSize(listview.getContext().getResources().getDimensionPixelSize(201655797));
    }

    public void setConvertViewPosition(int position) {
        mConvertViewPosition = position;
    }

    public void setConvertViewPadding(View convertView, boolean openOverflowUpward, int sidePadding, int minimumWidth) {
        convertView.setMinimumWidth(Math.max(convertView.getResources().getDimensionPixelSize(201655798), minimumWidth));
        if (openOverflowUpward && mConvertViewPosition == 0) {
            convertView.setPadding(sidePadding, 18, sidePadding, 0);
        } else if (openOverflowUpward || mConvertViewPosition != mOverFlowMenuCount - 1) {
            convertView.setPadding(sidePadding, 0, sidePadding, 0);
        } else {
            convertView.setPadding(sidePadding, 0, sidePadding, 18);
        }
    }

    public void setScrollIndicators(ListView listview) {
    }

    public int getMenuItemButtonRes() {
        return 201917615;
    }

    public int getButtonTextId() {
        return 16908954;
    }

    public int getButtonIconId() {
        return 16908952;
    }

    public int getContentContainerRes() {
        return 201917614;
    }

    private void setListViewBackground(ListView listview) {
        listview.setSelector(new ColorDrawable(16777215));
    }

    public int getFloatingToolBarHeightRes() {
        return 201655799;
    }
}
