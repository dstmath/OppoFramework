package com.color.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class ColorSlideMenuItem {
    public static final int STYLE_HEAD = 0;
    public static final int STYLE_MilDDLE = 1;
    public static final int STYLE_TAIL = 2;
    private Drawable mBackground;
    int[] mBackgroundStyleId;
    private Context mContext;
    private Drawable mIcon;
    private CharSequence mText;
    private int mWidth;

    public ColorSlideMenuItem(Context context, CharSequence text, Drawable drawble) {
        this.mWidth = 54;
        this.mBackgroundStyleId = new int[]{201852139, 201852140, 201852141};
        this.mContext = context;
        this.mBackground = drawble;
        this.mText = text;
        this.mWidth = this.mContext.getResources().getDimensionPixelSize(201655452);
    }

    public ColorSlideMenuItem(Context context, int text, int id) {
        this(context, context.getResources().getString(text), context.getDrawable(id));
    }

    public ColorSlideMenuItem(Context context, int text, Drawable drawble) {
        this(context, context.getResources().getString(text), drawble);
    }

    public ColorSlideMenuItem(Context context, CharSequence text, int id) {
        this(context, text, context.getDrawable(id));
    }

    public ColorSlideMenuItem(Context context, int text) {
        this(context, text, 201852140);
    }

    public ColorSlideMenuItem(Context context, CharSequence text) {
        this(context, text, 201852140);
    }

    public ColorSlideMenuItem(Context context, Drawable icon) {
        this.mWidth = 54;
        this.mBackgroundStyleId = new int[]{201852139, 201852140, 201852141};
        this.mContext = context;
        this.mIcon = icon;
        this.mBackground = context.getDrawable(201852140);
        this.mText = null;
        this.mWidth = this.mContext.getResources().getDimensionPixelSize(201655452);
    }

    public Drawable getBackground() {
        return this.mBackground;
    }

    public void setBackground(Drawable drawble) {
        this.mBackground = drawble;
    }

    public void setBackground(int id) {
        setBackground(this.mContext.getDrawable(id));
    }

    public void setBackgroundStyle(int index) {
        setBackground(this.mContext.getDrawable(this.mBackgroundStyleId[index]));
    }

    public CharSequence getText() {
        return this.mText;
    }

    public void setText(CharSequence text) {
        this.mText = text;
    }

    public void setText(int text) {
        setText(this.mContext.getText(text));
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
    }

    public void setIcon(int iconResId) {
        this.mIcon = this.mContext.getDrawable(iconResId);
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public void setContentDescription(String text) {
    }
}
