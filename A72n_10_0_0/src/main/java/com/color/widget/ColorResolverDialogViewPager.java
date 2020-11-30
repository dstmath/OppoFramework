package com.color.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.media.MediaMetadataEditor;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import com.color.widget.ColorPagerAdapter;
import java.util.List;

public class ColorResolverDialogViewPager extends ColorViewPager {
    public ColorResolverDialogViewPager(Context context) {
        super(context);
        setOverScrollMode(2);
    }

    public ColorResolverDialogViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOverScrollMode(2);
    }

    @Override // com.color.widget.ColorViewPager
    public int getCurrentItem() {
        return super.getCurrentItem();
    }

    /* access modifiers changed from: protected */
    @Override // com.color.widget.ColorViewPager, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(MediaMetadataEditor.KEY_EDITABLE_MASK, 0));
            int h = child.getMeasuredHeight();
            if (h > height) {
                height = h;
            }
        }
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), getPaddingTop() + height + getPaddingBottom());
    }

    @Deprecated
    public void setColorGridViewList(List<ColorGridView> list, List<ResolveInfo> list2, Intent intent, CheckBox checkbox, Dialog alertDialog) {
    }

    public void setColorResolverItemEventListener(ColorPagerAdapter.ColorResolverItemEventListener listener) {
        ColorPagerAdapter adapter = getAdapter();
        if (adapter != null) {
            adapter.setColorResolverItemEventListener(listener);
        }
    }
}
