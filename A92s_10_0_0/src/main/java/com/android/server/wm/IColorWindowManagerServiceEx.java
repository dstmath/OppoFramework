package com.android.server.wm;

import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.IOppoWindowStateObserver;
import android.view.IWindow;
import android.view.InputChannel;
import android.view.WindowManager;
import com.color.darkmode.IColorDarkModeListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface IColorWindowManagerServiceEx extends IOppoWindowManagerServiceEx {
    public static final IColorWindowManagerServiceEx DEFAULT = new IColorWindowManagerServiceEx() {
        /* class com.android.server.wm.IColorWindowManagerServiceEx.AnonymousClass1 */
    };
    public static final String NAME = "IColorPackageManagerServiceEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorWindowManagerServiceEx;
    }

    default IColorWindowManagerServiceEx getDefault() {
        return DEFAULT;
    }

    default void getInterceptWindowAudioPids(Context context, Session session, WindowManager.LayoutParams attrs) {
    }

    default boolean execInterceptWindow(Context context, WindowState win) {
        return false;
    }

    default IColorWindowManagerServiceInner getColorWindowManagerServiceInner() {
        return new IColorWindowManagerServiceInner() {
            /* class com.android.server.wm.IColorWindowManagerServiceEx.AnonymousClass2 */
        };
    }

    default IColorTaskPositionerEx getColorTaskPositionerEx(WindowManagerService wms) {
        return new IColorTaskPositionerEx() {
            /* class com.android.server.wm.IColorWindowManagerServiceEx.AnonymousClass3 */
        };
    }

    default void notifyWindowStateChange(Bundle options) {
    }

    default void registerOppoWindowStateObserver(IOppoWindowStateObserver observer) {
    }

    default void unregisterOppoWindowStateObserver(IOppoWindowStateObserver observer) {
    }

    default void createMonitorInputConsumer(WindowManagerService ws, IBinder token, String name, InputChannel inputChannel) {
    }

    default boolean destroyMonitorInputConsumer(String name) {
        return false;
    }

    default void handleWindow(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, WindowState win, Session session, IWindow client, int type) {
    }

    default void sendPopUpNotifyMessage(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, WindowState win) {
    }

    default void sendBroadcastForFloatWindow(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, boolean state, String packageName) {
    }

    default boolean isPackageToastClosed(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, String packageName) {
        return false;
    }

    default void updateFloatWindowState(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, WindowState preWin, WindowState nextWin) {
    }

    default void handlePackageIntent(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, Intent intent) {
    }

    default void handleAlertWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, int mode, WindowState win) {
    }

    default void handleFloatWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, boolean b, WindowState win) {
    }

    default boolean checkFloatWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, String packageName) {
        return false;
    }

    default void removeFromFloatWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, String packageName) {
    }

    default void addToFloatWindowSet(WindowManagerService ws, AppOpsManager appOps, IActivityManager activityManager, Context context, String packageName) {
    }

    default void setFileDescriptorForScreenShot(FileDescriptor fd) {
    }

    default boolean dumpWindowsForScreenShot(PrintWriter pw, String name, String[] args) {
        return false;
    }

    default boolean checkEnableSetGestureExclusion(Context context, WindowState win) {
        return false;
    }

    default IColorDisplayPolicyEx getColorDisplayPolicyEx(DisplayPolicy displayPolicy) {
        Log.i("IColorWindowManagerServiceEx", "getColorDisplayPolicyEx here.");
        return new ColorDummyDisplayPolicyEx(displayPolicy);
    }

    default void updataeAccidentPreventionState(Context context, boolean enable, int rotation) {
    }

    default void registerOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) {
    }

    default void unregisterOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener listener) {
    }

    default void notifyDarkModeListener() {
    }
}
