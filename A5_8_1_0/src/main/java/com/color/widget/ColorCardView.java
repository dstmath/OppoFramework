package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.oppo.internal.R;

public class ColorCardView extends FrameLayout {
    private boolean mCompatPadding;
    private final Rect mContentPadding = new Rect();
    private boolean mPreventCornerOverlap;
    private final Rect mShadowBounds = new Rect();

    public ColorCardView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ColorCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ColorCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    public void setPadding(int left, int top, int right, int bottom) {
    }

    public void setPaddingRelative(int start, int top, int end, int bottom) {
    }

    public boolean getUseCompatPadding() {
        return this.mCompatPadding;
    }

    public void setUseCompatPadding(boolean useCompatPadding) {
        if (this.mCompatPadding != useCompatPadding) {
            this.mCompatPadding = useCompatPadding;
            onCompatPaddingChanged();
        }
    }

    public void setContentPadding(int left, int top, int right, int bottom) {
        this.mContentPadding.set(left, top, right, bottom);
        updatePadding();
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorCardView, defStyleAttr, 0);
        int backgroundColor = a.getColor(0, 0);
        float radius = a.getDimension(1, 0.0f);
        float elevation = a.getDimension(2, 0.0f);
        float maxElevation = a.getDimension(3, 0.0f);
        this.mCompatPadding = a.getBoolean(4, false);
        this.mPreventCornerOverlap = a.getBoolean(5, true);
        int defaultPadding = a.getDimensionPixelSize(6, 0);
        this.mContentPadding.left = a.getDimensionPixelSize(7, defaultPadding);
        this.mContentPadding.top = a.getDimensionPixelSize(9, defaultPadding);
        this.mContentPadding.right = a.getDimensionPixelSize(8, defaultPadding);
        this.mContentPadding.bottom = a.getDimensionPixelSize(10, defaultPadding);
        if (elevation > maxElevation) {
            maxElevation = elevation;
        }
        a.recycle();
        setBackground(new ColorRoundRectDrawable(backgroundColor, radius));
        setClipToOutline(true);
        setElevation(elevation);
        setMaxElevation(maxElevation);
    }

    public void setCardBackgroundColor(int color) {
        setBackgroundColor(color);
    }

    public void setBackgroundColor(int color) {
        ((ColorRoundRectDrawable) getBackground()).setColor(color);
    }

    public int getContentPaddingLeft() {
        return this.mContentPadding.left;
    }

    public int getContentPaddingRight() {
        return this.mContentPadding.right;
    }

    public int getContentPaddingTop() {
        return this.mContentPadding.top;
    }

    public int getContentPaddingBottom() {
        return this.mContentPadding.bottom;
    }

    public void setRadius(float radius) {
        ((ColorRoundRectDrawable) getBackground()).setRadius(radius);
    }

    public float getRadius() {
        return ((ColorRoundRectDrawable) getBackground()).getRadius();
    }

    public void setShadowPadding(int left, int top, int right, int bottom) {
        this.mShadowBounds.set(left, top, right, bottom);
        super.setPadding(this.mContentPadding.left + left, this.mContentPadding.top + top, this.mContentPadding.right + right, this.mContentPadding.bottom + bottom);
    }

    public void setCardElevation(float radius) {
        setElevation(radius);
    }

    public float getCardElevation() {
        return getElevation();
    }

    public void setMaxCardElevation(float radius) {
        setMaxElevation(radius);
    }

    public void setMaxElevation(float radius) {
        ((ColorRoundRectDrawable) getBackground()).setPadding(radius, getUseCompatPadding(), getPreventCornerOverlap());
        updatePadding();
    }

    public void updatePadding() {
        if (getUseCompatPadding()) {
            float elevation = getMaxElevation();
            float radius = getRadius();
            int hPadding = (int) Math.ceil((double) ColorRoundRectDrawable.calculateHorizontalPadding(elevation, radius, getPreventCornerOverlap()));
            int vPadding = (int) Math.ceil((double) ColorRoundRectDrawable.calculateVerticalPadding(elevation, radius, getPreventCornerOverlap()));
            setShadowPadding(hPadding, vPadding, hPadding, vPadding);
            return;
        }
        setShadowPadding(0, 0, 0, 0);
    }

    public float getMaxCardElevation() {
        return getMaxElevation();
    }

    public float getMaxElevation() {
        return ((ColorRoundRectDrawable) getBackground()).getPadding();
    }

    public boolean getPreventCornerOverlap() {
        return this.mPreventCornerOverlap;
    }

    public void setPreventCornerOverlap(boolean preventCornerOverlap) {
        if (preventCornerOverlap != this.mPreventCornerOverlap) {
            this.mPreventCornerOverlap = preventCornerOverlap;
            onPreventCornerOverlapChanged();
        }
    }

    public void onCompatPaddingChanged() {
        setMaxElevation(getMaxElevation());
    }

    public void onPreventCornerOverlapChanged() {
        setMaxElevation(getMaxElevation());
    }
}
