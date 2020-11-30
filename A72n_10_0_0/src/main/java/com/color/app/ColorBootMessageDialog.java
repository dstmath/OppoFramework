package com.color.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import com.color.util.ColorContextUtil;
import com.color.util.ColorLog;

public class ColorBootMessageDialog extends ProgressDialog {
    private static final boolean DBG = false;
    private static final int SPLIT_COUNT = 2;
    private static final String TAG = "ColorBootMessageDialog";
    private final int mIdProgressPercent = ColorContextUtil.getResId(getContext(), 201458881);

    protected ColorBootMessageDialog(Context context) {
        super(context, 201524226);
        ColorLog.i(TAG, "new ColorBootMessageDialog");
    }

    @Override // android.app.Dialog, android.view.Window.Callback
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    @Override // android.app.Dialog, android.view.Window.Callback
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return true;
    }

    @Override // android.app.Dialog, android.view.Window.Callback
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override // android.app.Dialog, android.view.Window.Callback
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        return true;
    }

    @Override // android.app.Dialog, android.view.Window.Callback
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        return true;
    }

    @Override // android.app.Dialog, android.view.Window.Callback
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.app.AlertDialog, android.app.ProgressDialog, android.app.Dialog
    public void onCreate(Bundle savedInstanceState) {
        initWindow(getWindow());
        super.onCreate(savedInstanceState);
    }

    @Override // android.app.AlertDialog, android.app.ProgressDialog
    public void setMessage(CharSequence msg) {
        if (msg != null) {
            String[] msgs = msg.toString().split("\\|");
            int length = msgs.length;
            if (length > 2) {
                try {
                    int current = Integer.parseInt(msgs[0].trim());
                    int total = Integer.parseInt(msgs[1].trim());
                    if (total > 0) {
                        setProgress((getMax() * current) / total);
                    } else {
                        ColorLog.e(TAG, "setProgress ERROR : current=" + current + ", total=" + total);
                    }
                } catch (NumberFormatException e) {
                    ColorLog.e(TAG, e.toString());
                }
            }
            if (length > 0) {
                int viewId = isMessage(length) ? 16908299 : this.mIdProgressPercent;
                String text = msgs[length - 1].trim();
                TextView textView = (TextView) findViewById(viewId);
                if (textView != null) {
                    textView.setText(text);
                }
            }
        }
    }

    public static ProgressDialog create(Context context) {
        ProgressDialog dialog = new ColorBootMessageDialog(context);
        dialog.setProgressStyle(1);
        dialog.setCancelable(false);
        return dialog;
    }

    /* access modifiers changed from: protected */
    public void onInitWindowParams(WindowManager.LayoutParams lp) {
        lp.type = 2021;
    }

    /* access modifiers changed from: protected */
    public int getWindowFlags() {
        return 1280;
    }

    private void initWindow(Window window) {
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = 119;
        lp.screenOrientation = 5;
        onInitWindowParams(lp);
        window.setAttributes(lp);
        window.addFlags(getWindowFlags());
    }

    private boolean isMessage(int length) {
        if (length <= 2 && getProgress() >= getMax()) {
            return false;
        }
        return true;
    }
}
