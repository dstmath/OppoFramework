package com.color.app;

import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

public class ColorRotatingSpinnerDialog extends ColorSpinnerDialog {
    private int mIncrementBy;
    private int mIncrementSecondaryBy;
    private boolean mIndeterminate;
    private Drawable mIndeterminateDrawable;
    private ProgressBar mProgressBar;
    private Drawable mProgressDrawable;

    public ColorRotatingSpinnerDialog(Context context) {
        super(context);
    }

    ColorRotatingSpinnerDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, ColorAlertDialog.resolveDialogTheme(context, 0));
    }

    public ColorRotatingSpinnerDialog(Context context, int theme) {
        super(context, theme);
    }

    protected void onCreate(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(201917513, null);
        this.mProgress = view.findViewById(201458931);
        this.mProgressBar = (ProgressBar) this.mProgress;
        setView(view);
        if (this.mIncrementBy > 0) {
            incrementProgressBy(this.mIncrementBy);
        }
        setIndeterminate(this.mIndeterminate);
        super.onCreate(savedInstanceState);
    }

    public void setProgress(int value) {
        if (this.mHasStarted) {
            this.mProgressBar.setProgress(value);
        } else {
            this.mProgressVal = value;
        }
    }

    public int getProgress() {
        if (this.mProgressBar != null) {
            return this.mProgressBar.getProgress();
        }
        return this.mProgressVal;
    }

    public int getMax() {
        if (this.mProgressBar != null) {
            return this.mProgressBar.getMax();
        }
        return this.mMax;
    }

    public void setMax(int max) {
        if (this.mProgressBar != null) {
            this.mProgressBar.setMax(max);
        } else {
            this.mMax = max;
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        if (this.mProgressBar != null) {
            this.mProgressBar.setIndeterminate(indeterminate);
        } else {
            this.mIndeterminate = indeterminate;
        }
    }

    public void incrementProgressBy(int diff) {
        if (this.mProgressBar != null) {
            this.mProgressBar.incrementProgressBy(diff);
        } else {
            this.mIncrementBy += diff;
        }
    }

    public boolean isIndeterminate() {
        if (this.mProgressBar != null) {
            return this.mProgressBar.isIndeterminate();
        }
        return this.mIndeterminate;
    }

    public void setProgressDrawable(Drawable d) {
        if (this.mProgressBar != null) {
            this.mProgressBar.setProgressDrawable(d);
        } else {
            this.mProgressDrawable = d;
        }
    }

    public void setIndeterminateDrawable(Drawable d) {
        if (this.mProgressBar != null) {
            this.mProgressBar.setIndeterminateDrawable(d);
        } else {
            this.mIndeterminateDrawable = d;
        }
    }
}
