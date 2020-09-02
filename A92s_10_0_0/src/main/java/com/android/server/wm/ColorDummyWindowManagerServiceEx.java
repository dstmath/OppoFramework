package com.android.server.wm;

import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.IOppoWindowStateObserver;
import android.view.IWindow;
import android.view.InputChannel;
import android.view.WindowManager;
import com.color.darkmode.IColorDarkModeListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ColorDummyWindowManagerServiceEx extends OppoDummyWindowManagerServiceEx implements IColorWindowManagerServiceEx {
    private static final String TAG = "ColorDummyWindowManagerServiceEx";

    public ColorDummyWindowManagerServiceEx(Context context, WindowManagerService wms) {
        super(context, wms);
    }

    @Override // com.android.server.IOppoCommonManagerServiceEx, com.android.server.OppoDummyCommonManagerServiceEx
    public void systemReady() {
        registerDummyColorCustomManager();
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void getInterceptWindowAudioPids(Context context, Session session, WindowManager.LayoutParams attrs) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public boolean execInterceptWindow(Context context, WindowState win) {
        return false;
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public IColorWindowManagerServiceInner getColorWindowManagerServiceInner() {
        return null;
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public IColorTaskPositionerEx getColorTaskPositionerEx(WindowManagerService wms) {
        return new ColorDummyColorTaskPositionerEx(wms);
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void notifyWindowStateChange(Bundle options) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void registerOppoWindowStateObserver(IOppoWindowStateObserver observer) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void unregisterOppoWindowStateObserver(IOppoWindowStateObserver observer) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void createMonitorInputConsumer(WindowManagerService ws, IBinder token, String name, InputChannel inputChannel) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public boolean destroyMonitorInputConsumer(String name) {
        return false;
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void handleWindow(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, WindowState win, Session session, IWindow client, int type) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void sendPopUpNotifyMessage(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, WindowState win) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void sendBroadcastForFloatWindow(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, boolean state, String packageName) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public boolean isPackageToastClosed(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, String packageName) {
        return false;
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void updateFloatWindowState(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, WindowState preWin, WindowState nextWin) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void handlePackageIntent(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, Intent intent) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void handleAlertWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, int mode, WindowState win) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void handleFloatWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, boolean b, WindowState win) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public boolean checkFloatWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, String packageName) {
        return false;
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void removeFromFloatWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, String packageName) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void addToFloatWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, String packageName) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public boolean checkEnableSetGestureExclusion(Context context, WindowState win) {
        return false;
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public IColorDisplayPolicyEx getColorDisplayPolicyEx(DisplayPolicy displayPolicy) {
        return new ColorDummyDisplayPolicyEx(displayPolicy);
    }

    private void registerDummyColorCustomManager() {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void setFileDescriptorForScreenShot(FileDescriptor fd) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public boolean dumpWindowsForScreenShot(PrintWriter pw, String name, String[] args) {
        return false;
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void updataeAccidentPreventionState(Context context, boolean enable, int rotation) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void registerOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void unregisterOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) {
    }

    @Override // com.android.server.wm.IColorWindowManagerServiceEx
    public void notifyDarkModeListener() {
    }
}
