package com.color.sau;

import android.content.Context;
import com.color.app.ColorRotatingSpinnerDialog;

public class SauWaitProgressDialog {
    Context mContext;
    ColorRotatingSpinnerDialog mDialog;

    public SauWaitProgressDialog(Context context) {
        this.mContext = context;
        String upgradeRunningText = this.mContext.getResources().getString(201590106);
        this.mDialog = new ColorRotatingSpinnerDialog(context, 201523207);
        this.mDialog.setIconAttribute(16843605);
        this.mDialog.setTitle(upgradeRunningText);
        this.mDialog.setCancelable(false);
    }

    public void show() {
        if (this.mDialog != null) {
            this.mDialog.show();
        }
    }
}
