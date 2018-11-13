package com.color.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;
import com.android.internal.R;
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

    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        return true;
    }

    public boolean dispatchTrackballEvent(MotionEvent ev) {
        return true;
    }

    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        return true;
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return true;
    }

    protected void onCreate(Bundle savedInstanceState) {
        initWindow(getWindow());
        super.onCreate(savedInstanceState);
    }

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
                int viewId = isMessage(length) ? R.id.message : this.mIdProgressPercent;
                CharSequence text = msgs[length - 1].trim();
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

    protected void onInitWindowParams(LayoutParams lp) {
        lp.type = LayoutParams.TYPE_BOOT_PROGRESS;
    }

    protected int getWindowFlags() {
        return 1280;
    }

    private void initWindow(Window window) {
        LayoutParams lp = window.getAttributes();
        lp.gravity = 119;
        lp.screenOrientation = 5;
        onInitWindowParams(lp);
        window.setAttributes(lp);
        window.addFlags(getWindowFlags());
    }

    private boolean isMessage(int length) {
        boolean z = true;
        if (length > 2) {
            return true;
        }
        if (getProgress() >= getMax()) {
            z = false;
        }
        return z;
    }
}
