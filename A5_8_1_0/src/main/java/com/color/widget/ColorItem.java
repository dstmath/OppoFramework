package com.color.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class ColorItem {
    public static final int ITEM_FIFTH = 4;
    public static final int ITEM_FIRST = 0;
    public static final int ITEM_FOURTH = 3;
    public static final int ITEM_SECOND = 1;
    public static final int ITEM_THIRD = 2;
    private Drawable mBackgroud;
    private Context mContext;
    private Drawable mIcon;
    private OnItemClickListener mOnItemClickListener;
    private String mText;

    public static class Builder {
        private ColorItem ci = new ColorItem();

        public Builder(Context context) {
            this.ci.mContext = context;
        }

        public Builder setText(String text) {
            this.ci.mText = text;
            return this;
        }

        public Builder setText(int textResId) {
            this.ci.mText = this.ci.getContext().getString(textResId);
            return this;
        }

        public Builder setIcon(Drawable icon) {
            this.ci.mIcon = icon;
            return this;
        }

        public Builder setIcon(int iconResId) {
            this.ci.mIcon = this.ci.getContext().getResources().getDrawable(iconResId);
            return this;
        }

        public Builder setBackgroud(Drawable background) {
            this.ci.mBackgroud = background;
            return this;
        }

        public Builder setBackgroud(int bgResId) {
            this.ci.mBackgroud = this.ci.getContext().getResources().getDrawable(bgResId);
            return this;
        }

        public Builder setOnItemClickListener(OnItemClickListener e) {
            this.ci.mOnItemClickListener = e;
            return this;
        }

        public ColorItem create() {
            return this.ci;
        }
    }

    public interface OnItemClickListener {
        void OnColorMenuItemClick(int i);
    }

    protected ColorItem() {
    }

    public String getText() {
        return this.mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public void setIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    public Drawable getBackgroud() {
        return this.mBackgroud;
    }

    public void setBackgroud(Drawable backgroud) {
        this.mBackgroud = backgroud;
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public OnItemClickListener getOnItemClickListener() {
        return this.mOnItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
}
