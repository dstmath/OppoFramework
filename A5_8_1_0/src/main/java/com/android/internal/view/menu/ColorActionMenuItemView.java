package com.android.internal.view.menu;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import oppo.content.res.OppoFontUtils;

public class ColorActionMenuItemView extends ActionMenuItemView {
    private static final String TAG = "ColorActionMenuItemView";
    private float mDensity;
    private int mHorizontalPadding;
    private Typeface mMediumTypeface;
    private CharSequence mTitle;
    private int mVerticalPadding;

    public ColorActionMenuItemView(Context context) {
        this(context, null);
    }

    public ColorActionMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorActionMenuItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorActionMenuItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mVerticalPadding = 0;
        this.mHorizontalPadding = 0;
        this.mTitle = null;
        this.mDensity = 1.0f;
        this.mMediumTypeface = null;
        if (isOppoStyle()) {
            this.mDensity = context.getResources().getDisplayMetrics().density;
            this.mVerticalPadding = getResources().getDimensionPixelSize(201655296);
            this.mHorizontalPadding = getResources().getDimensionPixelSize(201655297);
            if (OppoFontUtils.isFlipFontUsed) {
                this.mMediumTypeface = Typeface.DEFAULT;
            } else {
                try {
                    this.mMediumTypeface = Typeface.createFromFile("/system/fonts/ColorOSUI-Medium.ttf");
                } catch (Exception e) {
                    this.mMediumTypeface = Typeface.DEFAULT;
                    Log.e(TAG, "create special typeface failed");
                }
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isOppoStyle()) {
            updatePadding();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isOppoStyle()) {
            superOnMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (isOppoStyle() && (isDummyItem() ^ 1) == 0) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    public boolean needsDividerBefore() {
        if (isOppoStyle()) {
            return false;
        }
        return super.needsDividerBefore();
    }

    public boolean needsDividerAfter() {
        if (isOppoStyle()) {
            return false;
        }
        return super.needsDividerAfter();
    }

    public void setIcon(Drawable icon) {
        super.setIcon(icon);
        if (isOppoStyle()) {
            updatePadding();
        }
    }

    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (isOppoStyle()) {
            this.mTitle = title;
            updatePadding();
        }
    }

    public void initialize(MenuItemImpl itemData, int menuType) {
        super.initialize(itemData, menuType);
        if (!(!isOppoStyle() || this.mMediumTypeface == null || itemData == null)) {
            setTypeface(itemData.showsTextAsAction() ? this.mMediumTypeface : Typeface.DEFAULT);
        }
    }

    public void setLayoutParams(LayoutParams params) {
        if (isOppoStyle()) {
            params.height = -1;
        }
        super.-wrap18(params);
    }

    private void updatePadding() {
        boolean visible = TextUtils.isEmpty(this.mTitle) ^ 1;
        int paddingHorizontal = visible ? this.mHorizontalPadding : 0;
        int paddingVertical = visible ? this.mVerticalPadding : 0;
        setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
    }

    private boolean isDummyItem() {
        if (getItemData().getIcon() == null && !hasText()) {
            return true;
        }
        return false;
    }

    void updateTextButtonVisibility() {
        if (!isOppoStyle()) {
            super.updateTextButtonVisibility();
        }
        setTitleText();
    }
}
