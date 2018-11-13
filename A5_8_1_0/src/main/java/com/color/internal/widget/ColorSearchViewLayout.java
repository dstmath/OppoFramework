package com.color.internal.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.color.util.ColorContextUtil;

public class ColorSearchViewLayout extends LinearLayout {
    private static final boolean DBG = false;
    private static final String TAG = "ColorSearchViewLayout";
    private int mCloseBtnPaddingEnd;
    private int mCloseBtnPaddingStart;
    private Context mContext;
    private int mFrameMargin;
    private int mLimitPaddingR;
    private int mSearchBarHeight;
    private int mSearchButtonId;
    private int mSearchCloseBtnId;
    private Drawable mSearchDrawable;
    private int mSearchEditFrameId;
    private int mSearchIconId;
    private int mSearchPlateId;
    private int mSearchSrcIconId;
    private int mSearchSrcId;
    private int mSearchSrcTextId;
    private int mSearchTextColor;
    private ColorStateList mSearchTextHintColor;

    public ColorSearchViewLayout(Context context) {
        this(context, null);
    }

    public ColorSearchViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSearchButtonId = -1;
        this.mSearchEditFrameId = -1;
        this.mSearchSrcTextId = -1;
        this.mSearchCloseBtnId = -1;
        this.mSearchIconId = -1;
        this.mSearchSrcIconId = -1;
        this.mSearchPlateId = -1;
        this.mSearchSrcId = -1;
        this.mSearchBarHeight = 0;
        this.mFrameMargin = 0;
        this.mSearchTextHintColor = null;
        this.mSearchTextColor = 0;
        this.mCloseBtnPaddingStart = 0;
        this.mCloseBtnPaddingEnd = 0;
        this.mSearchDrawable = null;
        this.mLimitPaddingR = 0;
        this.mContext = context;
        if (isOppoStyle()) {
            this.mSearchButtonId = ColorContextUtil.getResId(context, 201458915);
            this.mSearchEditFrameId = ColorContextUtil.getResId(context, 201458916);
            this.mSearchSrcTextId = ColorContextUtil.getResId(context, 201458917);
            this.mSearchCloseBtnId = ColorContextUtil.getResId(context, 201458918);
            this.mSearchIconId = 201458959;
            this.mSearchPlateId = ColorContextUtil.getResId(context, 201458960);
            this.mSearchSrcIconId = 201458964;
            this.mSearchSrcId = 201458965;
            this.mSearchBarHeight = getResources().getDimensionPixelSize(201655471);
            this.mFrameMargin = getResources().getDimensionPixelSize(201655472);
            this.mSearchTextHintColor = context.getColorStateList(201720863);
            this.mSearchTextColor = ColorContextUtil.getAttrColor(context, 201392716);
            this.mCloseBtnPaddingStart = getResources().getDimensionPixelSize(201655473);
            this.mCloseBtnPaddingEnd = getResources().getDimensionPixelSize(201655474);
            this.mSearchDrawable = context.getDrawable(201852174);
            this.mLimitPaddingR = getResources().getDimensionPixelSize(201655510);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        if (isOppoStyle()) {
            LayoutParams lp;
            LinearLayout searchPlate = (LinearLayout) findViewById(this.mSearchPlateId);
            LinearLayout searchSrc = new LinearLayout(this.mContext);
            searchSrc.setId(this.mSearchSrcId);
            if (!(searchSrc == null || searchPlate == null)) {
                lp = new LayoutParams(0, -2);
                lp.weight = 1.0f;
                lp.gravity = 8388627;
                searchSrc.setLayoutParams(lp);
                searchSrc.setPadding(0, 0, this.mLimitPaddingR, 0);
                searchPlate.addView(searchSrc, 0);
                searchSrc.setGravity(8388627);
            }
            ImageButton imageButton = new ImageButton(this.mContext);
            imageButton.setId(this.mSearchSrcIconId);
            if (!(imageButton == null || searchSrc == null)) {
                imageButton.setBackgroundDrawable(this.mSearchDrawable);
                lp = new LayoutParams(-2, -2);
                lp.gravity = 8388627;
                imageButton.setLayoutParams(lp);
                searchSrc.addView(imageButton, 0);
            }
            TextView searchSrcText = (TextView) findViewById(this.mSearchSrcTextId);
            searchPlate.removeView(searchSrcText);
            if (!(searchSrcText == null || searchSrc == null)) {
                LayoutParams lpSrcText = (LayoutParams) searchSrcText.getLayoutParams();
                if (lpSrcText != null) {
                    lpSrcText.gravity = 8388627;
                    lpSrcText.height = this.mSearchBarHeight;
                    lpSrcText.weight = 0.0f;
                    lpSrcText.width = -1;
                }
                searchSrcText.setPadding(0, 0, 0, 0);
                searchSrcText.setHintTextColor(this.mSearchTextHintColor);
                searchSrcText.setTextColor(this.mSearchTextColor);
                searchSrcText.setMinWidth(0);
                searchSrcText.setFocusable(DBG);
                searchSrc.addView(searchSrcText, 1);
            }
            ImageView searchCloseBtn = (ImageView) findViewById(this.mSearchCloseBtnId);
            searchCloseBtn.setPaddingRelative(this.mCloseBtnPaddingStart, 0, this.mCloseBtnPaddingEnd, 0);
            searchCloseBtn.setVisibility(8);
            LayoutParams lpEditFrame = (LayoutParams) ((LinearLayout) findViewById(this.mSearchEditFrameId)).getLayoutParams();
            if (lpEditFrame != null) {
                lpEditFrame.setMarginsRelative(this.mFrameMargin, this.mFrameMargin, this.mFrameMargin, this.mFrameMargin);
            }
            ((ImageView) findViewById(this.mSearchButtonId)).setBackground(null);
            ImageView searchImage = new ImageView(this.mContext);
            searchImage.setId(this.mSearchIconId);
            if (searchImage != null) {
                lp = new LayoutParams(-2, -2);
                lp.gravity = 16;
                searchImage.setLayoutParams(lp);
                searchImage.setPadding(this.mCloseBtnPaddingStart, 0, this.mCloseBtnPaddingEnd, 0);
                searchPlate.addView(searchImage);
                searchImage.setFocusable(true);
                searchImage.setVisibility(8);
            }
        }
    }
}
