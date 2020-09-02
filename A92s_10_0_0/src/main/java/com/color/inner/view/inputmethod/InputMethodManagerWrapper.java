package com.color.inner.view.inputmethod;

import android.util.Log;
import android.view.inputmethod.InputMethodManager;

public class InputMethodManagerWrapper {
    private static final String TAG = "InputMethodManagerWrapper";

    private InputMethodManagerWrapper() {
    }

    public static int getInputMethodWindowVisibleHeight(InputMethodManager inputMethodManager) {
        try {
            return inputMethodManager.getInputMethodWindowVisibleHeight();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return 0;
        }
    }
}
