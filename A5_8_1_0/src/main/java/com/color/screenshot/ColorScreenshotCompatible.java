package com.color.screenshot;

import android.app.ActivityManager;
import android.app.ColorStatusBarManager;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.OppoWhiteListManager;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import android.view.IOppoWindowManagerImpl;
import android.view.InputEvent;
import android.view.WindowManagerGlobal;
import com.android.internal.R;
import com.android.internal.notification.SystemNotificationChannels;
import com.color.util.ColorLog;
import java.util.List;

public final class ColorScreenshotCompatible {
    private static final String CHANNEL_ID = SystemNotificationChannels.FOREGROUND_SERVICE;
    private static final boolean DBG = ColorLongshotDump.DBG;
    private static final String TAG = "LongshotDump";
    private final ActivityManager mActivityManager;
    private final ColorStatusBarManager mColorStatusBarManager = new ColorStatusBarManager();
    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private final IOppoWindowManagerImpl mOppoWindowManager = new IOppoWindowManagerImpl();
    private final StatusBarManager mStatusBarManager;
    private final OppoWhiteListManager mWhiteListManager;

    public ColorScreenshotCompatible(Context context) {
        this.mContext = context;
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        this.mWhiteListManager = new OppoWhiteListManager(context);
    }

    public boolean isInMultiWindowMode() {
        int dockSide = -1;
        try {
            dockSide = WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "isInMultiWindowMode : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
        }
        return -1 != dockSide;
    }

    public void longshotNotifyConnected(boolean isConnected) {
        try {
            this.mOppoWindowManager.longshotNotifyConnected(isConnected);
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "longshotNotifyConnected : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
        }
    }

    public int getLongshotSurfaceLayer() {
        int layer = 0;
        try {
            return this.mOppoWindowManager.getLongshotSurfaceLayer();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "getLongshotSurfaceLayer : " + e.toString());
            return layer;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return layer;
        }
    }

    public int getLongshotSurfaceLayerByType(int type) {
        int layer = 0;
        try {
            return this.mOppoWindowManager.getLongshotSurfaceLayerByType(type);
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "getLongshotSurfaceLayerByType : " + e.toString());
            return layer;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return layer;
        }
    }

    public boolean isInputShow() {
        boolean result = false;
        try {
            return this.mOppoWindowManager.isInputShow();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "isInputShow : " + e.toString());
            return result;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return result;
        }
    }

    public boolean isShortcutsPanelShow() {
        boolean result = false;
        try {
            return this.mOppoWindowManager.isShortcutsPanelShow();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "isShortcutsPanelShow : " + e.toString());
            return result;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return result;
        }
    }

    public boolean isStatusBarVisible() {
        boolean result = false;
        try {
            return this.mOppoWindowManager.isStatusBarVisible();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "isStatusBarVisible : " + e.toString());
            return result;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return result;
        }
    }

    public boolean isNavigationBarVisible() {
        boolean result = false;
        try {
            return this.mOppoWindowManager.isNavigationBarVisible();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "isNavigationBarVisible : " + e.toString());
            return result;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return result;
        }
    }

    public boolean isKeyguardShowingAndNotOccluded() {
        boolean result = false;
        try {
            return this.mOppoWindowManager.isKeyguardShowingAndNotOccluded();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "isKeyguardShowingAndNotOccluded : " + e.toString());
            return result;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return result;
        }
    }

    public void getFocusedWindowFrame(Rect frame) {
        try {
            this.mOppoWindowManager.getFocusedWindowFrame(frame);
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "getFocusedWindowFrame : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
        }
    }

    public boolean injectInputEvent(InputEvent event, int mode) {
        try {
            this.mOppoWindowManager.longshotInjectInput(event, mode);
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "injectInputEvent : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
        }
        return true;
    }

    public void injectInputBegin() {
        try {
            this.mOppoWindowManager.longshotInjectInputBegin();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "injectInputBegin : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
        }
    }

    public void injectInputEnd() {
        try {
            this.mOppoWindowManager.longshotInjectInputEnd();
        } catch (RemoteException e) {
            ColorLog.e(DBG, "LongshotDump", "injectInputEnd : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
        }
    }

    @Deprecated
    public Builder createNotificationBuilder() {
        this.mNotificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, this.mContext.getString(R.string.notification_channel_foreground_service), 0));
        return new Builder(this.mContext, CHANNEL_ID);
    }

    @Deprecated
    public void cancelNotification(int notificatinID) {
        this.mNotificationManager.deleteNotificationChannel(CHANNEL_ID);
        this.mNotificationManager.cancel(notificatinID);
    }

    public void createNotificationChannel(String channelId, String channelName) {
        this.mNotificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, 0));
    }

    public void deleteNotificationChannel(String channelId) {
        this.mNotificationManager.deleteNotificationChannel(channelId);
    }

    public Builder createNotificationBuilder(String channelId) {
        return new Builder(this.mContext, channelId);
    }

    public void showNavigationBar(boolean show) {
        if (show) {
            this.mColorStatusBarManager.showNavigationBar();
        } else {
            this.mColorStatusBarManager.hideNavigationBar();
        }
    }

    public void setShortcutsPanelState(boolean enable) {
        this.mStatusBarManager.setShortcutsPanelState(enable ? 0 : 1);
    }

    public void addStageProtectInfo(String pkg, long timeout) {
        this.mWhiteListManager.addStageProtectInfo(pkg, timeout);
    }

    public void removeStageProtectInfo(String pkg) {
        this.mWhiteListManager.removeStageProtectInfo(pkg);
    }

    public ComponentName getTopActivity() {
        return this.mActivityManager.getTopAppName();
    }

    public String getTopPackage() {
        List<String> list = this.mActivityManager.getAllTopPkgName();
        if (list == null || list.size() <= 0) {
            return null;
        }
        return (String) list.get(0);
    }
}
