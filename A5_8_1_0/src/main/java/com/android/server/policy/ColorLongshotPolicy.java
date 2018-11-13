package com.android.server.policy;

import android.content.Context;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy.WindowState;

public class ColorLongshotPolicy {
    private static final String SHORTCUTS_PANEL = "ShortcutsPanel";
    private Context mContext = null;
    private WindowState mShortcutsPanel = null;

    public void init(Context context) {
        this.mContext = context;
    }

    public int prepareAddWindowLw(WindowState win, LayoutParams attrs) {
        switch (attrs.type) {
            case 2024:
                if (SHORTCUTS_PANEL.equals(attrs.getTitle())) {
                    this.mShortcutsPanel = win;
                    break;
                }
                break;
        }
        return 0;
    }

    public void removeWindowLw(WindowState win) {
        if (this.mShortcutsPanel == win) {
            this.mShortcutsPanel = null;
        }
    }

    public boolean isShortcutsPanelShow() {
        if (this.mShortcutsPanel != null) {
            return this.mShortcutsPanel.isVisibleLw();
        }
        return false;
    }
}
