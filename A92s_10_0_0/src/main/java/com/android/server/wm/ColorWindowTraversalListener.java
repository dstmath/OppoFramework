package com.android.server.wm;

import android.view.IWindow;
import android.view.WindowManager;

public interface ColorWindowTraversalListener {
    void collectFloatWindows(IWindow iWindow, CharSequence charSequence, int i, WindowManager.LayoutParams layoutParams);

    void collectSystemWindows(IWindow iWindow, CharSequence charSequence, int i, WindowManager.LayoutParams layoutParams);

    boolean hasSystemDocorView(IWindow iWindow);

    void printDetect(String str, CharSequence charSequence);

    void printWindow(String str, CharSequence charSequence);
}
