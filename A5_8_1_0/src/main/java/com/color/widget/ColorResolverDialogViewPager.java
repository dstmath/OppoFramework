package com.color.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.CheckBox;
import com.color.widget.ColorPagerAdapter.ColorResolverItemEventListener;
import java.util.List;

public class ColorResolverDialogViewPager extends ColorViewPager {
    public static final String TAG = "ColorResolverDialogViewPager";
    private Dialog mAlertDialog;
    private CheckBox mCheckBox;
    private List<ColorGridView> mColorGridViewList;
    private ColorResolverItemEventListener mColorResolverItemEventListener = null;
    private Context mContext;
    private Intent mOriginIntent;
    private ColorResolverPagerAdapter mPagerAdapter;
    public int mPagerSize = 4;
    private List<ResolveInfo> mRiList;

    public ColorResolverDialogViewPager(Context context) {
        super(context);
        this.mContext = context;
        setOverScrollMode(2);
    }

    public ColorResolverDialogViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        setOverScrollMode(2);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == 2) {
            this.mPagerSize = 4;
        } else {
            this.mPagerSize = 8;
        }
        if ((this.mContext instanceof Activity) && ((Activity) this.mContext).isInMultiWindowMode()) {
            this.mPagerSize = 4;
        }
        this.mPagerAdapter = new ColorResolverPagerAdapter(this.mContext, this.mColorGridViewList, this.mRiList, (int) Math.ceil(((double) this.mRiList.size()) / ((double) this.mPagerSize)), this.mOriginIntent, this.mCheckBox, this.mAlertDialog, false);
        if (this.mColorResolverItemEventListener != null) {
            this.mPagerAdapter.setColorResolverItemEventListener(this.mColorResolverItemEventListener);
        }
        setAdapter(this.mPagerAdapter);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(536870911, 0));
            int h = child.getMeasuredHeight();
            if (h > height) {
                height = h;
            }
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((getPaddingTop() + height) + getPaddingBottom(), 1073741824));
    }

    public void setColorGridViewList(List<ColorGridView> listColorGridView, List<ResolveInfo> riList, Intent intent, CheckBox checkbox, Dialog alertDialog) {
        this.mColorGridViewList = listColorGridView;
        this.mRiList = riList;
        this.mOriginIntent = intent;
        this.mCheckBox = checkbox;
        this.mAlertDialog = alertDialog;
    }

    public void updateIntent(Intent intent) {
        if (this.mPagerAdapter != null) {
            this.mPagerAdapter.updateIntent(intent);
        }
        this.mOriginIntent = intent;
    }

    public void setColorResolverItemEventListener(ColorResolverItemEventListener listener) {
        this.mColorResolverItemEventListener = listener;
        if (getAdapter() != null) {
            getAdapter().setColorResolverItemEventListener(this.mColorResolverItemEventListener);
        }
    }
}
