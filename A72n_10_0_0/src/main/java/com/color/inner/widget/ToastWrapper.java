package com.color.inner.widget;

import android.view.WindowManager;
import android.widget.Toast;

public class ToastWrapper {
    private Toast mToast;

    public ToastWrapper(Toast toast) {
        this.mToast = toast;
    }

    public static WindowManager.LayoutParams getWindowParams(Toast toast) {
        return toast.getWindowParams();
    }
}
