package com.color.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class ColorItem {
    public static final int ITEM_FIFTH = 4;
    public static final int ITEM_FIRST = 0;
    public static final int ITEM_FOURTH = 3;
    public static final int ITEM_SECOND = 1;
    public static final int ITEM_THIRD = 2;
    /* access modifiers changed from: private */
    public Drawable mBackgroud;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Drawable mIcon;
    /* access modifiers changed from: private */
    public OnItemClickListener mOnItemClickListener;
    /* access modifiers changed from: private */
    public String mText;

    public interface OnItemClickListener {
        void OnColorMenuItemClick(int i);
    }

    protected ColorItem() {
    }

    public static class Builder {
        private ColorItem ci = new ColorItem();

        public Builder(Context context) {
            Context unused = this.ci.mContext = context;
        }

        public Builder setText(String text) {
            String unused = this.ci.mText = text;
            return this;
        }

        public Builder setText(int textResId) {
            ColorItem colorItem = this.ci;
            String unused = colorItem.mText = colorItem.getContext().getString(textResId);
            return this;
        }

        public Builder setIcon(Drawable icon) {
            Drawable unused = this.ci.mIcon = icon;
            return this;
        }

        public Builder setIcon(int iconResId) {
            ColorItem colorItem = this.ci;
            Drawable unused = colorItem.mIcon = colorItem.getContext().getResources().getDrawable(iconResId);
            return this;
        }

        public Builder setBackgroud(Drawable background) {
            Drawable unused = this.ci.mBackgroud = background;
            return this;
        }

        public Builder setBackgroud(int bgResId) {
            ColorItem colorItem = this.ci;
            Drawable unused = colorItem.mBackgroud = colorItem.getContext().getResources().getDrawable(bgResId);
            return this;
        }

        public Builder setOnItemClickListener(OnItemClickListener e) {
            OnItemClickListener unused = this.ci.mOnItemClickListener = e;
            return this;
        }

        public ColorItem create() {
            return this.ci;
        }
    }

    public String getText() {
        return this.mText;
    }

    public void setText(String mText2) {
        this.mText = mText2;
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public void setIcon(Drawable mIcon2) {
        this.mIcon = mIcon2;
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
