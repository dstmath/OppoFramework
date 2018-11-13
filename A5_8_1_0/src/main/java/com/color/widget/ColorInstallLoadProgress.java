package com.color.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ProgressBar;
import com.color.util.ColorChangeTextUtil;
import com.color.util.ColorContextUtil;
import oppo.R;

public class ColorInstallLoadProgress extends ColorLoadProgress {
    private final boolean DEBUG;
    private final String TAG;
    private final String mApostrophe;
    private ColorStateList mDefaultFailTextColor;
    private ColorStateList mDefaultTextColor;
    private int mDefaultTextSize;
    private int mFailTextColor;
    private FontMetricsInt mFmi;
    private int mHeight;
    private Drawable mInstallDownloadBg;
    private Drawable mInstallDownloadProgress;
    private Drawable mInstallFailBg;
    private Drawable mInstallFailPressed;
    private Drawable mInstallGiftBg;
    private Drawable mInstallGiftPressed;
    private ColorStateList mInstallTextColor;
    private boolean mIsChangeTextColor;
    private ColorStateList mProgressTextColor;
    private int mSpace;
    private int mTextColor;
    private int mTextPadding;
    private TextPaint mTextPaint;
    private String mTextView;
    private int mUserFailTextColor;
    private int mUserTextColor;
    private int mUserTextSize;
    private int mWidth;

    public ColorInstallLoadProgress(Context context) {
        this(context, null);
    }

    public ColorInstallLoadProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 201393251);
    }

    public ColorInstallLoadProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.TAG = "ColorInstallLoadProgress";
        this.DEBUG = false;
        this.mApostrophe = "...";
        this.mTextPaint = null;
        this.mUserTextSize = 0;
        this.mUserTextColor = -1;
        this.mUserFailTextColor = -1;
        this.mTextColor = -1;
        this.mFailTextColor = -1;
        this.mInstallDownloadBg = null;
        this.mInstallDownloadProgress = null;
        this.mInstallFailBg = null;
        this.mInstallFailPressed = null;
        this.mInstallGiftBg = null;
        this.mInstallGiftPressed = null;
        this.mFmi = null;
        this.mSpace = 0;
        this.mIsChangeTextColor = false;
        this.mTextPadding = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorLoadProgress, defStyle, 0);
        Drawable d = a.getDrawable(0);
        if (d != null) {
            setButtonDrawable(d);
        }
        setState(a.getInteger(1, 0));
        a.recycle();
        int textSize = getResources().getDimensionPixelSize(201655394);
        TypedArray b = context.obtainStyledAttributes(attrs, R.styleable.ColorInstallLoadProgress, defStyle, 0);
        this.mTextView = b.getString(0);
        this.mDefaultTextSize = b.getDimensionPixelSize(1, textSize);
        this.mInstallDownloadBg = b.getDrawable(3);
        this.mInstallDownloadProgress = b.getDrawable(4);
        this.mInstallFailBg = b.getDrawable(6);
        this.mInstallFailPressed = b.getDrawable(7);
        this.mInstallGiftBg = b.getDrawable(9);
        this.mInstallGiftPressed = b.getDrawable(10);
        this.mHeight = b.getDimensionPixelSize(8, 0);
        this.mWidth = b.getDimensionPixelOffset(2, 0);
        this.mDefaultTextColor = b.getColorStateList(11);
        this.mDefaultFailTextColor = b.getColorStateList(13);
        this.mProgressTextColor = b.getColorStateList(12);
        this.mInstallTextColor = this.mDefaultTextColor;
        b.recycle();
        if (this.mInstallDownloadBg != null && this.mInstallDownloadBg.isStateful()) {
            this.mInstallDownloadBg.setState(getDrawableState());
        }
        this.mDefaultTextSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) this.mDefaultTextSize, getResources().getConfiguration().fontScale, 4);
        this.mTextPadding = getResources().getDimensionPixelSize(201655575);
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable drawable = this.mInstallDownloadBg;
        if (drawable != null && drawable.isStateful()) {
            drawable.setState(getDrawableState());
        }
        Drawable failDrawable = this.mInstallFailBg;
        if (failDrawable != null && failDrawable.isStateful()) {
            failDrawable.setState(getDrawableState());
        }
        Drawable progressDrawable = this.mInstallDownloadProgress;
        if (progressDrawable != null && progressDrawable.isStateful()) {
            progressDrawable.setState(getDrawableState());
        }
    }

    public void setTextId(int stringId) {
        setText(getResources().getString(stringId));
    }

    public void setText(String text) {
        if (!text.equals(this.mTextView)) {
            this.mTextView = text;
            invalidate();
        }
    }

    public void setTextSize(int textSize) {
        if (textSize != 0) {
            this.mUserTextSize = textSize;
        }
    }

    public void setFailTextColor(int failTextColor) {
        if (failTextColor != 0) {
            this.mUserFailTextColor = failTextColor;
        }
    }

    public void setViewWidth(int width) {
        this.mWidth = width;
        requestLayout();
    }

    public void setTextColor(int textColor) {
        if (textColor != 0) {
            this.mUserTextColor = textColor;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this.mWidth, this.mHeight);
        init();
    }

    private void init() {
        this.mTextPaint = new TextPaint(1);
        this.mTextPaint.setAntiAlias(true);
        int textSize = this.mUserTextSize;
        if (textSize == 0) {
            textSize = this.mDefaultTextSize;
        }
        this.mTextColor = this.mUserTextColor;
        if (this.mTextColor == -1) {
            this.mTextColor = this.mDefaultTextColor.getColorForState(getDrawableState(), ColorContextUtil.getAttrColor(getContext(), 201392701));
        }
        this.mFailTextColor = this.mUserFailTextColor;
        if (this.mFailTextColor == -1) {
            this.mFailTextColor = this.mDefaultFailTextColor.getColorForState(getDrawableState(), ColorContextUtil.getAttrColor(getContext(), 201392720));
        }
        this.mTextPaint.setTextSize((float) textSize);
        this.mTextPaint.setFakeBoldText(true);
        this.mFmi = this.mTextPaint.getFontMetricsInt();
        boolean isChinese = isChinese(this.mTextView);
        String temp = getDisplayText(this.mTextView, this.mWidth - (this.mTextPadding * 2));
        if (temp.length() < this.mTextView.length()) {
            this.mTextView = isEnglish(getDisplayText(temp, (this.mWidth - (this.mTextPadding * 2)) - ((int) this.mTextPaint.measureText("...")))) + "...";
            return;
        }
        this.mTextView = temp;
    }

    private String getDisplayText(String rawString, int maxWidth) {
        int breakIndex = this.mTextPaint.breakText(rawString, true, (float) maxWidth, null);
        if (breakIndex == rawString.length()) {
            return rawString;
        }
        return rawString.substring(0, breakIndex - 1);
    }

    private String isEnglish(String text) {
        String temp = text;
        if (isChinese(text)) {
            return temp;
        }
        int index = text.lastIndexOf(32);
        if (index > 0) {
            return text.substring(0, index);
        }
        return temp;
    }

    private static boolean isChinese(String text) {
        String Reg = "^[一-龥]{1}$";
        int chinese = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.toString(text.charAt(i)).matches(Reg)) {
                chinese++;
            }
        }
        if (chinese > 0) {
            return true;
        }
        return false;
    }

    private void onDrawBg(Canvas canvas, int left, int top, int right, int bottom, Drawable drawable) {
        drawable.setBounds(left, top, right, bottom);
        drawable.draw(canvas);
    }

    private void onDrawText(Canvas canvas, int top, int buttonDrawableWidth, int buttonDrawableHeight, int left) {
        if (this.mTextView != null) {
            int textX = (this.mTextPadding + top) + (((buttonDrawableWidth - ((int) this.mTextPaint.measureText(this.mTextView))) - (this.mTextPadding * 2)) / 2);
            int textY = ((buttonDrawableHeight - (this.mFmi.bottom - this.mFmi.top)) / 2) - this.mFmi.top;
            canvas.drawText(this.mTextView, (float) textX, (float) textY, this.mTextPaint);
            if (this.mIsChangeTextColor) {
                this.mTextPaint.setColor(this.mProgressTextColor.getColorForState(getDrawableState(), ColorContextUtil.getAttrColor(getContext(), 201392708)));
                canvas.save();
                if (isLayoutRtl()) {
                    canvas.clipRect(buttonDrawableWidth - this.mSpace, top, buttonDrawableWidth, buttonDrawableHeight);
                } else {
                    canvas.clipRect(left, top, this.mSpace, buttonDrawableHeight);
                }
                canvas.drawText(this.mTextView, (float) textX, (float) textY, this.mTextPaint);
                canvas.restore();
                this.mIsChangeTextColor = false;
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int right = this.mWidth;
        int bottom = this.mHeight;
        int buttonDrawableWidth = right + 0;
        int buttonDrawableHeight = bottom + 0;
        if (this.mInstallFailBg != null && this.mState == 3) {
            onDrawBg(canvas, 0, 0, right, bottom, this.mInstallFailBg);
            this.mTextPaint.setColor(this.mDefaultFailTextColor.getColorForState(getDrawableState(), ColorContextUtil.getAttrColor(getContext(), 201392720)));
            this.mIsChangeTextColor = false;
            onDrawText(canvas, 0, buttonDrawableWidth, buttonDrawableHeight, 0);
        }
        if (this.mInstallGiftBg != null && this.mState == 4) {
            onDrawBg(canvas, 0, 0, right, bottom, this.mInstallGiftBg);
            this.mTextPaint.setColor(this.mTextColor);
            this.mIsChangeTextColor = false;
            onDrawText(canvas, 0, buttonDrawableWidth, buttonDrawableHeight, 0);
        }
        if (this.mInstallDownloadBg != null && this.mState != 3 && this.mState != 4) {
            onDrawBg(canvas, 0, 0, right, bottom, this.mInstallDownloadBg);
            this.mSpace = (int) (((float) buttonDrawableWidth) * (((float) this.mProgress) / ((float) this.mMax)));
            if ((this.mState == 1 || this.mState == 2) && this.mInstallDownloadProgress != null) {
                int min = this.mInstallDownloadProgress.getIntrinsicWidth();
                int height = buttonDrawableHeight;
                canvas.save();
                if (isLayoutRtl()) {
                    canvas.clipRect(right - this.mSpace, 0, right, buttonDrawableHeight);
                } else {
                    canvas.clipRect(0, 0, this.mSpace, buttonDrawableHeight);
                }
                this.mInstallDownloadProgress.setBounds(0, 0, right, bottom);
                this.mInstallDownloadProgress.draw(canvas);
                canvas.restore();
            }
            this.mIsChangeTextColor = true;
            this.mTextPaint.setColor(this.mInstallTextColor.getColorForState(getDrawableState(), ColorContextUtil.getAttrColor(getContext(), 201392708)));
            onDrawText(canvas, 0, buttonDrawableWidth, buttonDrawableHeight, 0);
        }
    }

    public CharSequence getAccessibilityClassName() {
        return ProgressBar.class.getName();
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setItemCount(this.mMax);
        event.setCurrentItemIndex(this.mProgress);
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if ((this.mState == 0 || this.mState == 3 || this.mState == 2) && this.mTextView != null) {
            info.setContentDescription(this.mTextView);
        }
    }
}
