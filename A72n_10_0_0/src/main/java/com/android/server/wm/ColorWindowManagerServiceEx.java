package com.android.server.wm;

import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.common.OppoFeatureCache;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import android.view.IColorAccidentallyTouchHelper;
import android.view.IOppoWindowStateObserver;
import android.view.IWindow;
import android.view.InputChannel;
import android.view.WindowManager;
import com.android.server.ColorServiceRegistry;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.darkmode.IColorDarkModeListener;
import com.color.util.ColorTypeCastingHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class ColorWindowManagerServiceEx extends ColorDummyWindowManagerServiceEx {
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String TAG = "ColorWindowManagerServiceEx";
    private ColorDragWindowHelper mColorDragWindowHelper;
    private final RemoteCallbackList<IColorDarkModeListener> mDarkModeListener = new RemoteCallbackList<>();
    private ColorDirectWindowHelper mDirectHelper;
    private ColorLongshotWindowHelper mLongshotHelper;
    private ColorWindowManagerServiceMessageHelper mMessageHelper;
    protected final Object mOppoWindowLock = new Object();
    protected final RemoteCallbackList<IOppoWindowStateObserver> mOppoWindowStateObservers = new RemoteCallbackList<>();
    private ColorWindowManagerServiceTransactionHelper mTransactionHelper;

    /* JADX DEBUG: Multi-variable search result rejected for r0v5, resolved type: android.app.AppOpsManager */
    /* JADX WARN: Multi-variable type inference failed */
    public ColorWindowManagerServiceEx(Context context, WindowManagerService wms) {
        super(context, wms);
        init(context, wms);
        AppOpsManager appOps = (AppOpsManager) context.getSystemService("appops");
        AppOpsManager.OnOpChangedInternalListener opListener = new AppOpsManager.OnOpChangedInternalListener() {
            /* class com.android.server.wm.ColorWindowManagerServiceEx.AnonymousClass1 */

            public void onOpChanged(int op, String packageName) {
                ColorToastHelper.getInstance().updateWindowSet(op, packageName);
            }
        };
        appOps.startWatchingMode(24, null, opListener);
        appOps.startWatchingMode(45, null, opListener);
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        ColorServiceRegistry.getInstance().serviceReady(25);
        notifyWmsReadyToColorFullScreenDisplayManager();
    }

    public void onStart() {
        Slog.i(TAG, "onStart");
        ColorWindowManagerServiceEx.super.onStart();
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (DEBUG) {
            Slog.i(TAG, "onTransact code = " + code);
        }
        ColorDirectWindowHelper colorDirectWindowHelper = this.mDirectHelper;
        if (colorDirectWindowHelper != null && colorDirectWindowHelper.onTransact(code, data, reply, flags)) {
            return true;
        }
        ColorLongshotWindowHelper colorLongshotWindowHelper = this.mLongshotHelper;
        if (colorLongshotWindowHelper != null && colorLongshotWindowHelper.onTransact(code, data, reply, flags)) {
            return true;
        }
        ColorWindowManagerServiceTransactionHelper colorWindowManagerServiceTransactionHelper = this.mTransactionHelper;
        if (colorWindowManagerServiceTransactionHelper != null) {
            return colorWindowManagerServiceTransactionHelper.onTransact(code, data, reply, flags);
        }
        return ColorWindowManagerServiceEx.super.onTransact(code, data, reply, flags);
    }

    public void handleMessage(Message msg, int whichHandler) {
        if (DEBUG) {
            Slog.i(TAG, "handleMessage msg = " + msg + " handler = " + whichHandler);
        }
        ColorWindowManagerServiceMessageHelper colorWindowManagerServiceMessageHelper = this.mMessageHelper;
        if (colorWindowManagerServiceMessageHelper != null) {
            colorWindowManagerServiceMessageHelper.handleMessage(msg, whichHandler);
        }
    }

    public void getInterceptWindowAudioPids(Context context, Session session, WindowManager.LayoutParams attrs) {
        ColorInterceptWindow.getInstance().getAudioPids(this.mContext, session, attrs);
    }

    public boolean execInterceptWindow(Context context, WindowState win) {
        return ColorInterceptWindow.getInstance().interceptWindow(context, win);
    }

    public IColorWindowManagerServiceInner getColorWindowManagerServiceInner() {
        OppoBaseWindowManagerService baseWms = typeCasting(this.mWms);
        if (baseWms != null) {
            return baseWms.mColorWmsInner;
        }
        return null;
    }

    public IColorTaskPositionerEx getColorTaskPositionerEx(WindowManagerService wms) {
        return new ColorTaskPositionerEx(wms);
    }

    public void notifyWindowStateChange(Bundle options) {
        try {
            int size = this.mOppoWindowStateObservers.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    this.mOppoWindowStateObservers.getBroadcastItem(i).onWindowStateChange(options);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error notifyWindowStateChange changed event.", e);
                }
            }
            this.mOppoWindowStateObservers.finishBroadcast();
        } catch (Exception e2) {
            Slog.e(TAG, "Exception notifyWindowStateChange changed event.", e2);
        }
    }

    public void registerOppoWindowStateObserver(IOppoWindowStateObserver observer) {
        synchronized (this.mOppoWindowLock) {
            this.mOppoWindowStateObservers.register(observer);
        }
    }

    public void unregisterOppoWindowStateObserver(IOppoWindowStateObserver observer) {
        synchronized (this.mOppoWindowLock) {
            this.mOppoWindowStateObservers.unregister(observer);
        }
    }

    public void createMonitorInputConsumer(WindowManagerService ws, IBinder token, String name, InputChannel inputChannel) {
        ColorInputConsumerManager.getInstance().createMonitorInputConsumer(ws, token, name, inputChannel);
    }

    public boolean destroyMonitorInputConsumer(String name) {
        return ColorInputConsumerManager.getInstance().destroyMonitorInputConsumer(name);
    }

    public void handleWindow(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, WindowState win, Session session, IWindow client, int type) {
        ColorToastHelper.getInstance().handleWindow(win, session, client, type);
    }

    public void sendPopUpNotifyMessage(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, WindowState win) {
        ColorToastHelper.getInstance().sendPopUpNotifyMessage(win);
    }

    public void sendBroadcastForFloatWindow(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, boolean state, String packageName) {
        ColorToastHelper.getInstance().sendBroadcastForFloatWindow(state, packageName);
    }

    public boolean isPackageToastClosed(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, String packageName) {
        ColorToastHelper.getInstance();
        return ColorToastHelper.isPackageToastClosed(packageName);
    }

    public void updateFloatWindowState(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, WindowState preWin, WindowState nextWin) {
        ColorToastHelper.getInstance().updateFloatWindowState(preWin, nextWin);
    }

    public void handlePackageIntent(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, Intent intent) {
        ColorToastHelper.getInstance().handlePackageIntent(intent);
    }

    public void handleAlertWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, int mode, WindowState win) {
        ColorToastHelper.getInstance().handleAlertWindowSet(mode, win);
    }

    public void handleFloatWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, boolean b, WindowState win) {
        ColorToastHelper.getInstance().handleFloatWindowSet(b, win);
    }

    public boolean checkFloatWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, String packageName) {
        return ColorToastHelper.getInstance().checkFloatWindowSet(packageName);
    }

    public void removeFromFloatWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, String packageName) {
        ColorToastHelper.getInstance().removeFromFloatWindowSet(packageName);
    }

    public void addToFloatWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, String packageName) {
        ColorToastHelper.getInstance().addToFloatWindowSet(packageName);
    }

    public boolean checkEnableSetGestureExclusion(Context context, WindowState win) {
        String pkg = win.getOwningPackage();
        String activity = "";
        int activityIndex = win.getAttrs().getTitle().toString().indexOf("/");
        if (activityIndex != -1) {
            activity = win.getAttrs().getTitle().toString().substring(activityIndex + 1);
        }
        boolean enable = GestureExclusionHelper.getInstance().checkGestureExclusion(context, pkg, activity);
        if (DEBUG) {
            Slog.i(TAG, "checkEnableSetGestureExclusion contain pkg:" + pkg + " ,act:" + activity + " ,enable:" + enable);
        }
        return enable;
    }

    public IColorDisplayPolicyEx getColorDisplayPolicyEx(DisplayPolicy displayPolicy) {
        return new ColorDisplayPolicyEx(displayPolicy);
    }

    private void init(Context context, WindowManagerService wms) {
        this.mMessageHelper = new ColorWindowManagerServiceMessageHelper(context, this);
        this.mTransactionHelper = new ColorWindowManagerServiceTransactionHelper(context, this);
        ColorServiceRegistry.getInstance().serviceInit(5, this);
        this.mLongshotHelper = new ColorLongshotWindowHelper(context, getWindowManagerService());
        this.mDirectHelper = new ColorDirectWindowHelper(context, getWindowManagerService());
        ColorToastHelper.getInstance().init(wms, this.mContext);
    }

    private void notifyWmsReadyToColorFullScreenDisplayManager() {
        OppoFeatureCache.get(IColorFullScreenDisplayManager.DEFAULT).sendMessageToWmService(1304, -1);
    }

    public void setFileDescriptorForScreenShot(FileDescriptor fd) {
        ColorLongshotWindowHelper colorLongshotWindowHelper = this.mLongshotHelper;
        if (colorLongshotWindowHelper != null) {
            colorLongshotWindowHelper.setFileDescriptor(fd);
        }
    }

    public boolean dumpWindowsForScreenShot(PrintWriter pw, String name, String[] args) {
        ColorLongshotWindowHelper colorLongshotWindowHelper = this.mLongshotHelper;
        if (colorLongshotWindowHelper == null || !colorLongshotWindowHelper.dumpWindows(pw, name, args)) {
            return DEBUG;
        }
        return true;
    }

    public void updataeAccidentPreventionState(Context context, boolean enable, int rotation) {
        OppoFeatureCache.getOrCreate(IColorAccidentallyTouchHelper.DEFAULT, new Object[0]).updataeAccidentPreventionState(context, enable, rotation);
    }

    public synchronized void registerOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) {
        this.mDarkModeListener.register(listener);
    }

    public synchronized void unregisterOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) {
        this.mDarkModeListener.unregister(listener);
    }

    public synchronized void notifyDarkModeListener() {
        try {
            int size = this.mDarkModeListener.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    this.mDarkModeListener.getBroadcastItem(i).onUiModeConfigurationChangeFinish();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mDarkModeListener.finishBroadcast();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return;
    }

    private static OppoBaseWindowManagerService typeCasting(WindowManagerService wms) {
        if (wms != null) {
            return (OppoBaseWindowManagerService) ColorTypeCastingHelper.typeCasting(OppoBaseWindowManagerService.class, wms);
        }
        return null;
    }

    public void pilferPointers(String name) {
        ColorInputConsumerManager.getInstance().pilferPointers(name);
    }
}
