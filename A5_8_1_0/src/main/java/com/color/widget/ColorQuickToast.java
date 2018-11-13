package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import javax.microedition.khronos.opengles.GL10;
import oppo.R;

public class ColorQuickToast {
    private static final int LENGTH_LONG = 3500;
    private static final int LENGTH_SHORT = 2000;
    private static final String TAG = "ColorQuickToast";
    private final LayoutParams mAttributes = new LayoutParams();
    private final Runnable mCancelRunnable = new Runnable() {
        public void run() {
            if (ColorQuickToast.this.mView.getParent() != null) {
                ColorQuickToast.this.mWindowManager.removeViewImmediate(ColorQuickToast.this.mView);
            }
        }
    };
    private final Context mContext;
    private int mGravity = 0;
    private final Handler mHandler = new Handler();
    private int mLayout = 0;
    private TextView mTextView = null;
    private View mView = null;
    private WindowManager mWindowManager = null;
    private int mX = 0;
    private int mY = 0;

    public ColorQuickToast(Context context) {
        this.mContext = new ContextThemeWrapper(context, 201523234);
        initResources();
        initWindow();
        initLayout();
    }

    public static ColorQuickToast getInstance(Context context) {
        return new ColorQuickToast(context);
    }

    public static void recycle() {
    }

    public void show(int resID) {
        show(this.mContext.getResources().getString(resID));
    }

    public void show(CharSequence text) {
        this.mTextView.setText(text);
        cancel();
        this.mWindowManager.addView(this.mView, this.mAttributes);
        this.mHandler.postDelayed(this.mCancelRunnable, 2000);
    }

    public void showLong(int resID) {
        showLong(this.mContext.getResources().getString(resID));
    }

    public void showLong(CharSequence text) {
        this.mTextView.setText(text);
        cancel();
        this.mWindowManager.addView(this.mView, this.mAttributes);
        this.mHandler.postDelayed(this.mCancelRunnable, 3500);
    }

    public void cancel() {
        this.mHandler.removeCallbacks(this.mCancelRunnable);
        this.mCancelRunnable.run();
    }

    public void setOffsetX(int x) {
        this.mAttributes.x = x;
    }

    public void setOffsetY(int y) {
        this.mAttributes.y = y;
    }

    public void setGravity(int gravity, int xOffset, int yOffset) {
        this.mAttributes.gravity = gravity;
        this.mAttributes.x = xOffset;
        this.mAttributes.y = yOffset;
    }

    public void setWindowAnimations(int windowAnimations) {
        this.mAttributes.windowAnimations = windowAnimations;
    }

    private void initResources() {
        TypedArray a = this.mContext.obtainStyledAttributes(R.styleable.ColorToastTheme);
        int ap = a.getResourceId(0, -1);
        a.recycle();
        if (ap != -1) {
            TypedArray b = this.mContext.obtainStyledAttributes(ap, R.styleable.ColorToast);
            this.mY = b.getDimensionPixelSize(2, 0);
            this.mGravity = b.getInteger(0, 0);
            this.mLayout = b.getResourceId(1, 0);
            b.recycle();
        }
    }

    private void initWindow() {
        this.mAttributes.flags = 152;
        this.mAttributes.format = -3;
        this.mAttributes.type = GL10.GL_CCW;
        this.mAttributes.width = -2;
        this.mAttributes.height = -2;
        this.mAttributes.setTitle(TAG);
        setGravity(this.mGravity, this.mX, this.mY);
        setWindowAnimations(0);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
    }

    private void initLayout() {
        this.mView = LayoutInflater.from(this.mContext).inflate(this.mLayout, null);
        this.mTextView = (TextView) this.mView.findViewById(16908299);
    }
}
