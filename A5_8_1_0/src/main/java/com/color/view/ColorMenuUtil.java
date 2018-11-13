package com.color.view;

import android.view.Menu;
import com.android.internal.view.menu.MenuBuilder;

public class ColorMenuUtil {
    private static final boolean DBG = true;
    private static final String TAG = "ColorMenuUtil";

    public static void stopDispatchingItemsChanged(Menu menu) {
        ((MenuBuilder) menu).stopDispatchingItemsChanged();
    }

    public static void startDispatchingItemsChanged(Menu menu) {
        ((MenuBuilder) menu).startDispatchingItemsChanged();
    }

    public static void startDispatchingItemsChanged(Menu menu, boolean structureChanged) {
        ((MenuBuilder) menu).startDispatchingItemsChanged(structureChanged);
    }
}
