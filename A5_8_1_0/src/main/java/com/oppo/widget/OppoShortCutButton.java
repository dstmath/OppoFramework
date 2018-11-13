package com.oppo.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import java.util.ArrayList;
import oppo.R;

public class OppoShortCutButton extends LinearLayout implements OnClickListener {
    private static final String ACTION_IME_HIDE = "com.oppo.android.INPUT_WINDOW_HIDDED";
    private static final String ACTION_IME_SHOW = "com.oppo.android.INPUT_WINDOW_SHOWN";
    private static final boolean DEBUG = false;
    private static final int MAXCHILDCOUNT = 3;
    private static final int MINCHILDCOUNT = 1;
    private static final int NONECHILD = 0;
    private static final int ONECHILD = 1;
    private static final String TAG = "OppoShortCutButton";
    private static final int THREECHILD = 3;
    private static final int TWOECHILD = 2;
    private Drawable mBackground;
    private Drawable mBackgroundButton;
    private int mButtonColor;
    private ArrayList<Button> mButtonList;
    private Button mButtonNegative;
    private String mButtonNegativeText;
    private Button mButtonNeutral;
    private String mButtonNeutralText;
    private Button mButtonPositive;
    private String mButtonPositiveText;
    private int mButtonShadowColor;
    private int mButtonSize;
    private int mChildCount;
    private boolean mImeStateReceiverRegistered;
    private final BroadcastReceiver mInputMethodStateReceiver;
    private boolean mIsButtonNegativeAble;
    private boolean mIsButtonNeutralAble;
    private boolean mIsButtonPositiveAble;
    private boolean mIsShow;
    private Drawable mItemDefaultBgDrawable;
    private Drawable mItemLeftBgDrawable;
    private Drawable mItemMiddleBgDrawable;
    private Drawable mItemRightBgDrawable;
    private OnShortCutButtonListener mOnShortCutButtonListener;
    private AnimationSet mOppoButtonEnterAniSet;
    private AnimationSet mOppoButtonExitAniSet;
    private boolean mReceiverEnabled;
    private View mView;

    public interface OnShortCutButtonListener {
        void onShortCutButtonClick(View view);
    }

    public OppoShortCutButton(Context context) {
        this(context, null);
    }

    public OppoShortCutButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mImeStateReceiverRegistered = DEBUG;
        this.mChildCount = 0;
        this.mBackground = null;
        this.mBackgroundButton = null;
        this.mButtonPositiveText = null;
        this.mButtonNegativeText = null;
        this.mButtonNeutralText = null;
        this.mIsButtonPositiveAble = true;
        this.mIsButtonNegativeAble = true;
        this.mIsButtonNeutralAble = true;
        this.mButtonPositive = null;
        this.mButtonNegative = null;
        this.mButtonNeutral = null;
        this.mButtonList = null;
        this.mOppoButtonEnterAniSet = null;
        this.mOppoButtonExitAniSet = null;
        this.mIsShow = DEBUG;
        this.mView = null;
        this.mOnShortCutButtonListener = null;
        this.mReceiverEnabled = true;
        this.mItemDefaultBgDrawable = null;
        this.mItemLeftBgDrawable = null;
        this.mItemMiddleBgDrawable = null;
        this.mItemRightBgDrawable = null;
        this.mInputMethodStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (OppoShortCutButton.this.mReceiverEnabled) {
                    if (intent.getAction().equals(OppoShortCutButton.ACTION_IME_HIDE)) {
                        if (OppoShortCutButton.this.mIsShow) {
                            OppoShortCutButton.this.mIsShow = OppoShortCutButton.DEBUG;
                            OppoShortCutButton.this.setVisibility(0);
                            OppoShortCutButton.this.startAnimation(OppoShortCutButton.this.mOppoButtonEnterAniSet);
                        }
                    } else if (intent.getAction().equals(OppoShortCutButton.ACTION_IME_SHOW) && OppoShortCutButton.this.mView.getVisibility() == 0) {
                        OppoShortCutButton.this.mIsShow = true;
                        OppoShortCutButton.this.setVisibility(8);
                        OppoShortCutButton.this.startAnimation(OppoShortCutButton.this.mOppoButtonExitAniSet);
                    }
                }
            }
        };
        this.mView = this;
        this.mItemDefaultBgDrawable = getResources().getDrawable(201850978);
        this.mItemLeftBgDrawable = getResources().getDrawable(201850983);
        this.mItemMiddleBgDrawable = getResources().getDrawable(201850985);
        this.mItemRightBgDrawable = getResources().getDrawable(201850984);
        this.mButtonList = new ArrayList();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OppoShortCutButton, 0, 0);
        if (a != null) {
            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                switch (a.getIndex(i)) {
                    case 0:
                        this.mChildCount = a.getInt(0, 1);
                        break;
                    case 1:
                        this.mBackground = a.getDrawable(1);
                        break;
                    case 2:
                        this.mBackgroundButton = a.getDrawable(2);
                        break;
                    case 3:
                        this.mButtonSize = a.getDimensionPixelSize(3, this.mButtonSize);
                        break;
                    case 4:
                        this.mButtonColor = a.getColor(4, this.mButtonColor);
                        break;
                    case 5:
                        this.mButtonNegativeText = a.getString(5);
                        break;
                    case 6:
                        this.mButtonNeutralText = a.getString(6);
                        break;
                    case 7:
                        this.mButtonPositiveText = a.getString(7);
                        break;
                    case 8:
                        this.mIsButtonNegativeAble = a.getBoolean(8, true);
                        break;
                    case 9:
                        this.mIsButtonNeutralAble = a.getBoolean(9, true);
                        break;
                    case 10:
                        this.mIsButtonPositiveAble = a.getBoolean(10, true);
                        break;
                    case 11:
                        this.mReceiverEnabled = a.getBoolean(11, true);
                        break;
                    case 12:
                        this.mButtonShadowColor = a.getColor(12, this.mButtonShadowColor);
                        break;
                    default:
                        break;
                }
            }
            a.recycle();
        }
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(201917450, this, true);
        this.mChildCount = getCountChild(this.mChildCount);
        this.mButtonPositive = (Button) findViewById(201458697);
        if (this.mButtonPositiveText != null) {
            this.mButtonPositive.setText(this.mButtonPositiveText);
        }
        this.mButtonPositive.setEnabled(this.mIsButtonPositiveAble);
        this.mButtonPositive.setOnClickListener(this);
        this.mButtonPositive.setId(1);
        this.mButtonNegative = (Button) findViewById(201458698);
        if (this.mButtonNegativeText != null) {
            this.mButtonNegative.setText(this.mButtonNegativeText);
        }
        this.mButtonNegative.setEnabled(this.mIsButtonNegativeAble);
        this.mButtonNegative.setOnClickListener(this);
        this.mButtonNegative.setId(2);
        this.mButtonNeutral = (Button) findViewById(201458699);
        if (this.mButtonNeutralText != null) {
            this.mButtonNeutral.setText(this.mButtonNeutralText);
        }
        this.mButtonNeutral.setEnabled(this.mIsButtonNeutralAble);
        this.mButtonNeutral.setOnClickListener(this);
        this.mButtonNeutral.setId(3);
        if (this.mBackground != null) {
            setBackgroundDrawable(this.mBackground);
        }
        this.mButtonList.clear();
        switch (this.mChildCount) {
            case 1:
                this.mButtonPositive.setVisibility(0);
                this.mButtonList.add(this.mButtonPositive);
                this.mButtonPositive.setBackgroundDrawable(this.mItemDefaultBgDrawable);
                break;
            case 2:
                this.mButtonPositive.setVisibility(0);
                this.mButtonList.add(this.mButtonPositive);
                this.mButtonNegative.setVisibility(0);
                this.mButtonList.add(this.mButtonNegative);
                this.mButtonPositive.setBackgroundDrawable(this.mItemLeftBgDrawable);
                this.mButtonNegative.setBackgroundDrawable(this.mItemRightBgDrawable);
                break;
            case 3:
                this.mButtonPositive.setVisibility(0);
                this.mButtonList.add(this.mButtonPositive);
                this.mButtonNegative.setVisibility(0);
                this.mButtonList.add(this.mButtonNegative);
                this.mButtonNeutral.setVisibility(0);
                this.mButtonList.add(this.mButtonNeutral);
                this.mButtonPositive.setBackgroundDrawable(this.mItemLeftBgDrawable);
                this.mButtonNegative.setBackgroundDrawable(this.mItemMiddleBgDrawable);
                this.mButtonNeutral.setBackgroundDrawable(this.mItemRightBgDrawable);
                break;
        }
        prepareTabLayoutAnim();
    }

    public int getCountChild(int count) {
        if (count >= 1 && count <= 3) {
            return count;
        }
        if (count < 1) {
            return 1;
        }
        if (count > 3) {
            return 3;
        }
        return 0;
    }

    public boolean setButtonText(String[] string) {
        int count = 0;
        if (string == null || string.length < this.mChildCount) {
            return DEBUG;
        }
        if (string.length >= this.mChildCount) {
            count = this.mChildCount;
        }
        for (int i = 0; i < count; i++) {
            ((Button) this.mButtonList.get(i)).setText(string[i]);
        }
        return true;
    }

    public boolean setButtonPositiveEnbale(boolean enable) {
        this.mButtonPositive.setEnabled(enable);
        return true;
    }

    public boolean setButtonNegativeEnbale(boolean enable) {
        this.mButtonNegative.setEnabled(enable);
        return true;
    }

    public boolean setButtonNeutralEnbale(boolean enable) {
        this.mButtonNeutral.setEnabled(enable);
        return true;
    }

    public boolean setButtonEnable(boolean[] enable) {
        int count = 0;
        if (enable.length < this.mChildCount) {
            return DEBUG;
        }
        if (enable.length >= this.mChildCount) {
            count = this.mChildCount;
        }
        for (int i = 0; i < count; i++) {
            ((Button) this.mButtonList.get(i)).setEnabled(enable[i]);
        }
        return true;
    }

    public boolean setButtonPositiveText(String text) {
        this.mButtonPositive.setText(text);
        return true;
    }

    public boolean setButtonPositiveVisible(int enable) {
        this.mButtonPositive.setVisibility(enable);
        return true;
    }

    public boolean setButtonNegativeText(String text) {
        this.mButtonNegative.setText(text);
        return true;
    }

    public boolean setButtonNegativeVisible(int enable) {
        this.mButtonNegative.setVisibility(enable);
        return true;
    }

    public boolean setButtonNeutralText(String text) {
        this.mButtonNeutral.setText(text);
        return true;
    }

    public boolean setButtonNeutralVisible(int enable) {
        this.mButtonNeutral.setVisibility(enable);
        return true;
    }

    public boolean setShortCutButtonBackground(Drawable drawable) {
        setBackgroundDrawable(drawable);
        return true;
    }

    public boolean setButtonBackground(Drawable drawable) {
        this.mButtonPositive.setBackgroundDrawable(drawable);
        this.mButtonNegative.setBackgroundDrawable(drawable);
        this.mButtonNeutral.setBackgroundDrawable(drawable);
        return true;
    }

    public boolean setShortCutButtonTextSize(int size) {
        this.mButtonPositive.setTextSize((float) size);
        this.mButtonNegative.setTextSize((float) size);
        this.mButtonNeutral.setTextSize((float) size);
        return true;
    }

    public boolean setPositiveButtonTextSize(int size) {
        this.mButtonPositive.setTextSize((float) size);
        return true;
    }

    public boolean setNegativeButtonTextSize(int size) {
        this.mButtonNegative.setTextSize((float) size);
        return true;
    }

    public boolean setNeutralButtonTextSize(int size) {
        this.mButtonNeutral.setTextSize((float) size);
        return true;
    }

    public boolean setShortCutButtonTextColor(int color) {
        this.mButtonPositive.setTextColor(color);
        this.mButtonNegative.setTextColor(color);
        this.mButtonNeutral.setTextColor(color);
        return true;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public void setShortButtonListener(OnShortCutButtonListener mListener) {
        this.mOnShortCutButtonListener = mListener;
    }

    public void onClick(View view) {
        if (this.mOnShortCutButtonListener != null) {
            this.mOnShortCutButtonListener.onShortCutButtonClick(view);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(ACTION_IME_HIDE);
        iFilter.addAction(ACTION_IME_SHOW);
        getContext().registerReceiver(this.mInputMethodStateReceiver, iFilter);
        this.mImeStateReceiverRegistered = true;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mInputMethodStateReceiver != null && this.mImeStateReceiverRegistered) {
            getContext().unregisterReceiver(this.mInputMethodStateReceiver);
        }
        this.mImeStateReceiverRegistered = DEBUG;
    }

    private void prepareTabLayoutAnim() {
        this.mOppoButtonEnterAniSet = new AnimationSet(true);
        Animation animation = new AlphaAnimation(0.5f, 1.0f);
        animation.setDuration(50);
        this.mOppoButtonEnterAniSet.addAnimation(animation);
        this.mOppoButtonExitAniSet = new AnimationSet(true);
        animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(50);
        this.mOppoButtonExitAniSet.addAnimation(animation);
        animation = new TranslateAnimation(1, 0.0f, 1, 0.0f, 2, 1.0f, 2, 0.5f);
        animation.setDuration(50);
        this.mOppoButtonExitAniSet.addAnimation(animation);
    }

    public void setBroadcastEnabled(boolean isEnabled) {
        this.mReceiverEnabled = isEnabled;
    }
}
