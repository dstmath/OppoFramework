package com.color.screenshot;

import android.app.ActivityManager;
import android.app.ColorStatusBarManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.OppoWhiteListManager;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import android.view.InputEvent;
import android.view.OppoWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.R;
import com.android.internal.notification.SystemNotificationChannels;
import com.color.util.ColorLog;
import java.lang.reflect.Method;
import java.util.List;

public final class ColorScreenshotCompatible {
    private static final String CHANNEL_ID = SystemNotificationChannels.FOREGROUND_SERVICE;
    private static final boolean DBG = ColorLongshotDump.DBG;
    private static final String TAG = "LongshotDump";
    private final ActivityManager mActivityManager;
    private final ColorStatusBarManager mColorStatusBarManager = new ColorStatusBarManager();
    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private final OppoWindowManager mOppoWindowManager = new OppoWindowManager();
    private final StatusBarManager mStatusBarManager;
    private final OppoWhiteListManager mWhiteListManager;

    public ColorScreenshotCompatible(Context context) {
        this.mContext = context;
        this.mActivityManager = (ActivityManager) context.getSystemService("activity");
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mStatusBarManager = (StatusBarManager) context.getSystemService(Context.STATUS_BAR_SERVICE);
        this.mWhiteListManager = new OppoWhiteListManager(context);
    }

    public boolean isInMultiWindowMode() {
        int dockSide = -1;
        try {
            dockSide = WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isInMultiWindowMode : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
        }
        return -1 != dockSide;
    }

    public void longshotNotifyConnected(boolean isConnected) {
        try {
            this.mOppoWindowManager.longshotNotifyConnected(isConnected);
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "longshotNotifyConnected : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
        }
    }

    public int getLongshotSurfaceLayer() {
        try {
            return this.mOppoWindowManager.getLongshotSurfaceLayer();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "getLongshotSurfaceLayer : " + e.toString());
            return 0;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return 0;
        }
    }

    public int getLongshotSurfaceLayerByType(int type) {
        try {
            return this.mOppoWindowManager.getLongshotSurfaceLayerByType(type);
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "getLongshotSurfaceLayerByType : " + e.toString());
            return 0;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return 0;
        }
    }

    public boolean isInputShow() {
        try {
            return this.mOppoWindowManager.isInputShow();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isInputShow : " + e.toString());
            return false;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return false;
        }
    }

    public boolean isVolumeShow() {
        try {
            return this.mOppoWindowManager.isVolumeShow();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isVolumeShow : " + e.toString());
            return false;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return false;
        }
    }

    public boolean isShortcutsPanelShow() {
        try {
            return this.mOppoWindowManager.isShortcutsPanelShow();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isShortcutsPanelShow : " + e.toString());
            return false;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return false;
        }
    }

    public boolean isStatusBarVisible() {
        try {
            return this.mOppoWindowManager.isStatusBarVisible();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isStatusBarVisible : " + e.toString());
            return false;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return false;
        }
    }

    public boolean isNavigationBarVisible() {
        try {
            return this.mOppoWindowManager.isNavigationBarVisible();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isNavigationBarVisible : " + e.toString());
            return false;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return false;
        }
    }

    public boolean isKeyguardShowingAndNotOccluded() {
        try {
            return this.mOppoWindowManager.isKeyguardShowingAndNotOccluded();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "isKeyguardShowingAndNotOccluded : " + e.toString());
            return false;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return false;
        }
    }

    public void getFocusedWindowFrame(Rect frame) {
        try {
            this.mOppoWindowManager.getFocusedWindowFrame(frame);
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "getFocusedWindowFrame : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
        }
    }

    public boolean injectInputEvent(InputEvent event, int mode) {
        try {
            this.mOppoWindowManager.longshotInjectInput(event, mode);
            return true;
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "injectInputEvent : " + e.toString());
            return true;
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
            return true;
        }
    }

    public void injectInputBegin() {
        try {
            this.mOppoWindowManager.longshotInjectInputBegin();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "injectInputBegin : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
        }
    }

    public void injectInputEnd() {
        try {
            this.mOppoWindowManager.longshotInjectInputEnd();
        } catch (RemoteException e) {
            boolean z = DBG;
            ColorLog.e(z, "LongshotDump", "injectInputEnd : " + e.toString());
        } catch (Exception e2) {
            ColorLog.e(DBG, "LongshotDump", Log.getStackTraceString(e2));
        }
    }

    @Deprecated
    public Notification.Builder createNotificationBuilder() {
        this.mNotificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, this.mContext.getString(R.string.notification_channel_foreground_service), 0));
        return new Notification.Builder(this.mContext, CHANNEL_ID);
    }

    @Deprecated
    public void cancelNotification(int notificatinID) {
        this.mNotificationManager.deleteNotificationChannel(CHANNEL_ID);
        this.mNotificationManager.cancel(notificatinID);
    }

    public void createNotificationChannel(String channelId, String channelName, int importance) {
        this.mNotificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, importance));
    }

    public void createNotificationChannel(String channelId, String channelName) {
        createNotificationChannel(channelId, channelName, 0);
    }

    public void deleteNotificationChannel(String channelId) {
        this.mNotificationManager.deleteNotificationChannel(channelId);
    }

    public Notification.Builder createNotificationBuilder(String channelId) {
        return new Notification.Builder(this.mContext, channelId);
    }

    public void showNavigationBar(boolean show) {
    }

    public void setShortcutsPanelState(boolean enable) {
    }

    public void addStageProtectInfo(String pkg, long timeout) {
        this.mWhiteListManager.addStageProtectInfo(pkg, timeout);
    }

    public void removeStageProtectInfo(String pkg) {
        this.mWhiteListManager.removeStageProtectInfo(pkg);
    }

    public ComponentName getTopActivity() {
        try {
            Method getTopAppName = this.mActivityManager.getClass().getMethod("getTopAppName", new Class[0]);
            getTopAppName.setAccessible(true);
            return (ComponentName) getTopAppName.invoke(this.mActivityManager, new Object[0]);
        } catch (Exception e) {
            return null;
        }
    }

    public String getTopPackage() {
        List<String> list = null;
        try {
            Method getAllTopPkgName = this.mActivityManager.getClass().getMethod("getAllTopPkgName", new Class[0]);
            getAllTopPkgName.setAccessible(true);
            list = (List) getAllTopPkgName.invoke(this.mActivityManager, new Object[0]);
        } catch (Exception e) {
        }
        if (list == null || list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }
}
