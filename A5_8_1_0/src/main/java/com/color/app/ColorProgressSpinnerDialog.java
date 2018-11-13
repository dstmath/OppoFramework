package com.color.app;

import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.color.widget.ColorTransferProgress;

public class ColorProgressSpinnerDialog extends ColorSpinnerDialog {
    private ColorTransferProgress mTransferProgress;

    public ColorProgressSpinnerDialog(Context context) {
        super(context);
    }

    ColorProgressSpinnerDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, ColorAlertDialog.resolveDialogTheme(context, 0));
    }

    public ColorProgressSpinnerDialog(Context context, int theme) {
        super(context, theme);
    }

    protected void onCreate(Bundle savedInstanceState) {
        View view = LayoutInflater.from(this.mContext).inflate(201917512, null);
        this.mProgress = (ColorTransferProgress) view.findViewById(201458931);
        this.mTransferProgress = (ColorTransferProgress) this.mProgress;
        setView(view);
        super.onCreate(savedInstanceState);
    }

    public void setProgress(int value) {
        if (this.mHasStarted) {
            this.mTransferProgress.setProgress(value);
        } else {
            this.mProgressVal = value;
        }
    }

    public int getProgress() {
        if (this.mTransferProgress != null) {
            return this.mTransferProgress.getProgress();
        }
        return this.mProgressVal;
    }

    public int getMax() {
        if (this.mTransferProgress != null) {
            return this.mTransferProgress.getMax();
        }
        return this.mMax;
    }

    public void setMax(int max) {
        if (this.mTransferProgress != null) {
            this.mTransferProgress.setMax(max);
        } else {
            this.mMax = max;
        }
    }
}
