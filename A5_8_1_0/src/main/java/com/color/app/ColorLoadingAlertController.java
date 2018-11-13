package com.color.app;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Window;
import android.widget.Button;

public class ColorLoadingAlertController extends ColorAlertController {
    public ColorLoadingAlertController(Context context, DialogInterface di, Window window) {
        super(context, di, window);
    }

    public int selectContentView() {
        this.mAlertDialogLayout = 201917511;
        return this.mAlertDialogLayout;
    }

    protected void centerButton(Button button) {
    }
}
