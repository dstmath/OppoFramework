package com.color.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import com.android.internal.app.AlertController.AlertParams;

public class ColorCustomAlertDialog extends AlertDialog {
    private final AlertParams mParams = new AlertParams(getContext());

    public ColorCustomAlertDialog(Context context, int themeResId, int deleteOption) {
        super(context, themeResId, true, deleteOption);
    }

    public void applyParams() {
        this.mParams.apply(this.mAlert);
    }

    public void setPositiveButton(int textId, OnClickListener listener) {
        this.mParams.mPositiveButtonText = getContext().getText(textId);
        this.mParams.mPositiveButtonListener = listener;
    }

    public void setNegativeButton(int textId, OnClickListener listener) {
        this.mParams.mNegativeButtonText = getContext().getText(textId);
        this.mParams.mNegativeButtonListener = listener;
    }

    public void setNeutralButton(int textId, OnClickListener listener) {
        this.mParams.mNeutralButtonText = getContext().getText(textId);
        this.mParams.mNeutralButtonListener = listener;
    }

    public void setItems(int itemsId, OnClickListener listener) {
        this.mParams.mItems = getContext().getResources().getTextArray(itemsId);
        this.mParams.mOnClickListener = listener;
    }
}
