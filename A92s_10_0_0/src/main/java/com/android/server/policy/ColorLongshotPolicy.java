package com.android.server.policy;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.color.view.ColorWindowUtils;

public class ColorLongshotPolicy {
    private Context mContext = null;
    private WindowManagerPolicy.WindowState mEdgeFloatBar = null;
    private WindowManagerPolicy.WindowState mEdgePanel = null;
    private WindowManagerPolicy.WindowState mFloatAssistBar = null;
    private WindowManagerPolicy.WindowState mFloatAssistPanel = null;
    private final DisplayMetrics mRealMetrics = new DisplayMetrics();
    private WindowManagerPolicy.WindowState mShortcutsPanel = null;
    private WindowManagerPolicy.WindowState mVolumeDialog = null;

    public void init(Context context) {
        this.mContext = context;
        context.getDisplay().getRealMetrics(this.mRealMetrics);
    }

    public int prepareAddWindowLw(WindowManagerPolicy.WindowState win, WindowManager.LayoutParams attrs) {
        CharSequence title = attrs.getTitle();
        int i = attrs.type;
        if (i == 2020 || i == 2024) {
            updateShortcutsPanel(win, title);
            updateVolumeDialog(win, title);
            return 0;
        } else if (i != 2314) {
            updateFloatAssist(win);
            return 0;
        } else {
            updateEdgePanel(win, title);
            return 0;
        }
    }

    public void removeWindowLw(WindowManagerPolicy.WindowState win) {
        if (this.mShortcutsPanel == win) {
            this.mShortcutsPanel = null;
        } else if (this.mVolumeDialog == win) {
            this.mVolumeDialog = null;
        } else if (this.mFloatAssistBar == win) {
            this.mFloatAssistBar = null;
        } else if (this.mFloatAssistPanel == win) {
            this.mFloatAssistPanel = null;
        } else if (this.mEdgeFloatBar == win) {
            this.mEdgeFloatBar = null;
        } else if (this.mEdgePanel == win) {
            this.mEdgePanel = null;
        }
    }

    public boolean isShortcutsPanelShow() {
        return isVisibleLw(this.mShortcutsPanel);
    }

    public boolean isVolumeShow() {
        return isVisibleLw(this.mVolumeDialog);
    }

    public boolean isEdgePanelExpand() {
        return isVisibleLw(this.mEdgePanel);
    }

    public boolean isFloatAssistExpand() {
        return isVisibleLw(this.mFloatAssistPanel);
    }

    private boolean isVisibleLw(WindowManagerPolicy.WindowState win) {
        if (win != null) {
            return win.isVisibleLw();
        }
        return false;
    }

    private boolean isRealSize(WindowManagerPolicy.WindowState win) {
        WindowManager.LayoutParams attrs = win.getAttrs();
        return attrs.width == this.mRealMetrics.widthPixels && attrs.height == this.mRealMetrics.heightPixels;
    }

    private boolean isFloatAssistant(WindowManagerPolicy.WindowState win) {
        if (ColorWindowUtils.isSystemWindow(win.getAttrs())) {
            return ColorWindowUtils.isFloatAssistant(win.getOwningPackage());
        }
        return false;
    }

    private void updateShortcutsPanel(WindowManagerPolicy.WindowState win, CharSequence title) {
        if (ColorWindowUtils.isShortcutsPanel(title)) {
            this.mShortcutsPanel = win;
        }
    }

    private void updateVolumeDialog(WindowManagerPolicy.WindowState win, CharSequence title) {
        if (ColorWindowUtils.isVolumeDialog(title)) {
            this.mVolumeDialog = win;
        }
    }

    private void updateEdgePanel(WindowManagerPolicy.WindowState win, CharSequence title) {
        if (ColorWindowUtils.isEdgeFloatBarTitle(title)) {
            this.mEdgeFloatBar = win;
        } else if (ColorWindowUtils.isEdgePanelTitle(title)) {
            this.mEdgePanel = win;
        }
    }

    private void updateFloatAssist(WindowManagerPolicy.WindowState win) {
        if (!isFloatAssistant(win)) {
            return;
        }
        if (isRealSize(win)) {
            this.mFloatAssistPanel = win;
        } else {
            this.mFloatAssistBar = win;
        }
    }
}
