package com.color.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.LinearLayout;
import com.oppo.internal.R;

public class ColorResolverAlertLinearLayout extends LinearLayout {
    private static final int DEFAULT_DISPLAY_ID = 0;
    private Drawable mBackgroundDrawable;
    /* access modifiers changed from: private */
    public int mBackgroundRadius;
    /* access modifiers changed from: private */
    public int mFixedBottom;
    /* access modifiers changed from: private */
    public int mFixedLeft;
    /* access modifiers changed from: private */
    public int mFixedRight;
    /* access modifiers changed from: private */
    public int mFixedTop;
    private int mLandscapeMaxHeight;
    private boolean mNeedClip;
    private ContentObserver mObserver;
    private int mPortraitMaxHeight;
    private int mShadowBottom;
    private Drawable mShadowDrawable;
    private int mShadowLeft;
    private int mShadowRight;
    private int mShadowTop;
    private View mSpaceView;

    public ColorResolverAlertLinearLayout(Context context) {
        this(context, null);
    }

    public ColorResolverAlertLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorResolverAlertLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mShadowLeft = 0;
        this.mShadowTop = 0;
        this.mShadowRight = 0;
        this.mShadowBottom = 0;
        this.mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            /* class com.color.widget.ColorResolverAlertLinearLayout.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                ColorResolverAlertLinearLayout.this.updateSpaceView();
            }
        };
        int defaultRadius = context.getResources().getDimensionPixelSize(201655676);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ColorAlertLinearLayout, defStyleAttr, 0);
        this.mBackgroundRadius = array.getDimensionPixelSize(0, defaultRadius);
        this.mShadowDrawable = context.getResources().getDrawable(201852324);
        if (array.hasValue(1)) {
            this.mShadowDrawable = array.getDrawable(1);
        }
        array.recycle();
        TypedArray array2 = context.obtainStyledAttributes(attrs, R.styleable.ColorResolverAlertLinearLayout, defStyleAttr, 0);
        this.mBackgroundDrawable = getContext().getDrawable(201852326);
        if (array2.hasValue(0)) {
            this.mBackgroundDrawable = array2.getDrawable(0);
        }
        array2.recycle();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorMaxLinearLayout);
        this.mPortraitMaxHeight = typedArray.getDimensionPixelSize(0, getResources().getDimensionPixelSize(201655298));
        this.mLandscapeMaxHeight = typedArray.getDimensionPixelSize(1, getResources().getDimensionPixelSize(201655811));
        typedArray.recycle();
        this.mSpaceView = new View(this.mContext);
        this.mSpaceView.setLayoutParams(new LinearLayout.LayoutParams(-1, this.mContext.getResources().getDimensionPixelSize(201655804)));
    }

    private void setHasShadow(boolean hasShadow) {
        if (hasShadow) {
            setBackground(this.mShadowDrawable);
            this.mShadowLeft = getPaddingLeft();
            this.mShadowRight = getPaddingRight();
            this.mShadowTop = getPaddingTop();
            this.mShadowBottom = getPaddingBottom();
        } else {
            setBackground(null);
            setPadding(0, 0, 0, 0);
            this.mShadowLeft = 0;
            this.mShadowTop = 0;
            this.mShadowRight = 0;
            this.mShadowBottom = 0;
        }
        requestLayout();
    }

    private void setNeedClip(boolean needClip) {
        this.mNeedClip = needClip;
    }

    private void clipBackground() {
        ViewOutlineProvider provider = new ViewOutlineProvider() {
            /* class com.color.widget.ColorResolverAlertLinearLayout.AnonymousClass2 */

            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(ColorResolverAlertLinearLayout.this.mFixedLeft, ColorResolverAlertLinearLayout.this.mFixedTop, ColorResolverAlertLinearLayout.this.mFixedRight, ColorResolverAlertLinearLayout.this.mFixedBottom, (float) ColorResolverAlertLinearLayout.this.mBackgroundRadius);
            }
        };
        setClipToOutline(true);
        setOutlineProvider(provider);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void dispatchDraw(Canvas canvas) {
        canvas.save();
        this.mBackgroundDrawable.setBounds(this.mFixedLeft, this.mFixedTop, this.mFixedRight, this.mFixedBottom);
        this.mBackgroundDrawable.draw(canvas);
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateConfigChange();
        observeHideNavigationBar();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int maxHeight = isPortrait() ? this.mPortraitMaxHeight : this.mLandscapeMaxHeight;
        if (height > maxHeight) {
            super.onMeasure(View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(maxHeight, 1073741824));
        }
        this.mFixedLeft = this.mShadowLeft;
        this.mFixedTop = this.mShadowTop;
        this.mFixedRight = getMeasuredWidth() - this.mShadowRight;
        this.mFixedBottom = getMeasuredHeight() - this.mShadowBottom;
        if (this.mNeedClip) {
            clipBackground();
        } else {
            setClipToOutline(false);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateConfigChange();
    }

    private void updateConfigChange() {
        if (isPortrait()) {
            setNeedClip(false);
            setHasShadow(false);
        } else {
            setNeedClip(true);
            setHasShadow(true);
        }
        updateSpaceView();
    }

    private boolean isPortrait() {
        Point point = new Point();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
        return point.x < point.y;
    }

    private void observeHideNavigationBar() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("manual_hide_navigationbar"), false, this.mObserver);
    }

    private boolean isNavigationBarShow() {
        if (!supportNavigationBar()) {
            return false;
        }
        int navigationBarStatus = Settings.Secure.getInt(this.mContext.getContentResolver(), "hide_navigationbar_enable", 0);
        int navigationBarHideStatus = Settings.Secure.getInt(this.mContext.getContentResolver(), "manual_hide_navigationbar", 0);
        if (navigationBarStatus == 0 || (navigationBarStatus == 1 && navigationBarHideStatus == 0)) {
            return true;
        }
        return false;
    }

    private boolean supportNavigationBar() {
        try {
            return WindowManagerGlobal.getWindowManagerService().hasNavigationBar(0);
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void updateSpaceView() {
        if (!isPortrait() || isNavigationBarShow()) {
            removeView(this.mSpaceView);
        } else if (this.mSpaceView.getParent() == null) {
            addView(this.mSpaceView, getChildCount());
        }
    }
}
