package com.color.app;

import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class ColorSpinnerDialog extends ColorAlertDialog {
    private static final int BIT_BUTTON_NEGATIVE = 2;
    private static final int BIT_BUTTON_NEUTRAL = 4;
    private static final int BIT_BUTTON_POSITIVE = 1;
    private Button mButtonNegative;
    private Button mButtonNeutral;
    private Button mButtonPositive;
    protected boolean mHasStarted;
    protected int mMax;
    protected CharSequence mMessage;
    protected TextView mMessageView;
    protected View mProgress;
    protected int mProgressVal;

    public ColorSpinnerDialog(Context context) {
        super(context);
    }

    void createDialog(int deleteDialogOption) {
        this.mAlert = new ColorLoadingAlertController(getContext(), this, getWindow());
        setCanceledOnTouchOutside(false);
    }

    ColorSpinnerDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, ColorAlertDialog.resolveDialogTheme(context, 0));
        setCancelable(cancelable);
        setOnCancelListener(cancelListener);
        this.mAlert = new ColorLoadingAlertController(context, this, getWindow());
    }

    public ColorSpinnerDialog(Context context, int theme) {
        super(context, theme);
    }

    protected void onCreate(Bundle savedInstanceState) {
        if (this.mMax > 0) {
            setMax(this.mMax);
        }
        if (this.mProgressVal > 0) {
            setProgress(this.mProgressVal);
        }
        if (this.mMessage != null) {
            setMessage(this.mMessage);
        }
        super.onCreate(savedInstanceState);
    }

    public void show() {
        super.show();
        setButtonPadding();
    }

    public void onStart() {
        super.onStart();
        this.mHasStarted = true;
    }

    protected void onStop() {
        super.onStop();
        this.mHasStarted = false;
    }

    public void setProgress(int value) {
    }

    public int getProgress() {
        return -1;
    }

    public int getMax() {
        return -1;
    }

    public void setMax(int max) {
    }

    public void setMessage(CharSequence message) {
    }

    public void setButtonPadding() {
        try {
            this.mButtonPositive = (Button) this.mWindow.findViewById(201458947);
            this.mButtonNegative = (Button) this.mWindow.findViewById(201458945);
            this.mButtonNeutral = (Button) this.mWindow.findViewById(201458946);
            if (this.mButtonPositive != null || this.mButtonNegative != null || this.mButtonNeutral != null) {
                int whichButtons = 0;
                if (this.mButtonPositive.getVisibility() == 0) {
                    whichButtons = 1;
                }
                if (this.mButtonNegative.getVisibility() == 0) {
                    whichButtons |= 2;
                }
                if (this.mButtonNeutral.getVisibility() == 0) {
                    whichButtons |= 4;
                }
                switch (whichButtons) {
                    case 1:
                        LayoutParams lp = (LayoutParams) this.mButtonPositive.getLayoutParams();
                        lp.leftMargin = 0;
                        lp.rightMargin = 0;
                        this.mButtonPositive.setLayoutParams(lp);
                        break;
                    case 2:
                        LayoutParams ln = (LayoutParams) this.mButtonNegative.getLayoutParams();
                        ln.leftMargin = 0;
                        ln.rightMargin = 0;
                        this.mButtonNegative.setLayoutParams(ln);
                        break;
                    case 4:
                        LayoutParams lne = (LayoutParams) this.mButtonNeutral.getLayoutParams();
                        lne.leftMargin = 0;
                        lne.rightMargin = 0;
                        this.mButtonNegative.setLayoutParams(lne);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
