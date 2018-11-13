package android.support.v4.widget;

import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowInsets;

class DrawerLayoutCompatApi21 {

    static class InsetsListener implements OnApplyWindowInsetsListener {
        InsetsListener() {
        }

        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
            boolean z = false;
            DrawerLayoutImpl drawerLayout = (DrawerLayoutImpl) v;
            if (insets.getSystemWindowInsetTop() > 0) {
                z = true;
            }
            drawerLayout.setChildInsets(insets, z);
            return insets.consumeSystemWindowInsets();
        }
    }

    DrawerLayoutCompatApi21() {
    }

    public static void configureApplyInsets(View drawerLayout) {
        if (drawerLayout instanceof DrawerLayoutImpl) {
            drawerLayout.setOnApplyWindowInsetsListener(new InsetsListener());
            drawerLayout.setSystemUiVisibility(1280);
        }
    }

    public static void dispatchChildInsets(View child, Object insets, int gravity) {
        WindowInsets wi = (WindowInsets) insets;
        if (gravity == 3) {
            wi = wi.replaceSystemWindowInsets(wi.getSystemWindowInsetLeft(), wi.getSystemWindowInsetTop(), 0, wi.getSystemWindowInsetBottom());
        } else if (gravity == 5) {
            wi = wi.replaceSystemWindowInsets(0, wi.getSystemWindowInsetTop(), wi.getSystemWindowInsetRight(), wi.getSystemWindowInsetBottom());
        }
        child.dispatchApplyWindowInsets(wi);
    }

    public static void applyMarginInsets(MarginLayoutParams lp, Object insets, int gravity) {
        WindowInsets wi = (WindowInsets) insets;
        if (gravity == 3) {
            wi = wi.replaceSystemWindowInsets(wi.getSystemWindowInsetLeft(), wi.getSystemWindowInsetTop(), 0, wi.getSystemWindowInsetBottom());
        } else if (gravity == 5) {
            wi = wi.replaceSystemWindowInsets(0, wi.getSystemWindowInsetTop(), wi.getSystemWindowInsetRight(), wi.getSystemWindowInsetBottom());
        }
        lp.leftMargin = wi.getSystemWindowInsetLeft();
        lp.topMargin = wi.getSystemWindowInsetTop();
        lp.rightMargin = wi.getSystemWindowInsetRight();
        lp.bottomMargin = wi.getSystemWindowInsetBottom();
    }

    public static int getTopInset(Object insets) {
        return insets != null ? ((WindowInsets) insets).getSystemWindowInsetTop() : 0;
    }
}
