package com.color.inner.app;

import android.app.OppoMirrorStatusBarManager;
import android.app.StatusBarManager;
import android.content.Context;
import android.util.Log;
import java.lang.reflect.Constructor;

public class StatusBarManagerWrapper {
    private static final String TAG = "StatusBarManagerWrapper";
    private StatusBarManager mManager;

    public StatusBarManagerWrapper(Context context) {
        try {
            Constructor<?> constructor = StatusBarManager.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            this.mManager = (StatusBarManager) constructor.newInstance(context);
        } catch (Exception e) {
            Log.e(TAG, "StatusBarManagerWrapper: " + e.toString());
        }
    }

    public void collapsePanels() {
        try {
            this.mManager.collapsePanels();
        } catch (Exception e) {
            Log.e(TAG, "collapsePanels: " + e.toString());
        }
    }

    public void expandNotificationsPanel() {
        try {
            this.mManager.expandNotificationsPanel();
        } catch (Exception e) {
            Log.e(TAG, "expandNotificationsPanel: " + e.toString());
        }
    }

    public void setShortcutsPanelState(int state) {
        try {
            OppoMirrorStatusBarManager.setShortcutsPanelState.call(this.mManager, new Object[]{Integer.valueOf(state)});
        } catch (Exception e) {
            Log.e(TAG, "setShortcutsPanelState: " + e.toString());
        }
    }
}
