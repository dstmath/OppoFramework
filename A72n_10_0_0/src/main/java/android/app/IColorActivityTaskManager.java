package android.app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.IColorKeyEventObserver;
import android.os.RemoteException;
import com.color.app.ColorAppInfo;
import com.color.app.ColorAppSwitchConfig;
import com.color.app.IColorAppSwitchObserver;
import com.color.app.IColorFreeformConfigChangedListener;
import com.color.app.IColorZoomWindowConfigChangedListener;
import com.color.zoomwindow.ColorZoomWindowInfo;
import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import com.color.zoomwindow.IColorZoomWindowObserver;
import java.util.List;
import java.util.Map;

public interface IColorActivityTaskManager extends IColorBaseActivityTaskManager {
    public static final int ADD_FEEFORM_CONFIG_CHANGED_LISTENER = 10056;
    public static final int ADD_ZOOM_WINDOW_CONFIG_CHANGED_LISTENER = 10079;
    public static final int COLOR_START_ZOOM_WINDOW = 10068;
    public static final int COLOR_ZOOM_WINDOW_BUBBLE = 10072;
    public static final int COLOR_ZOOM_WINDOW_CONFIG = 10075;
    public static final int COLOR_ZOOM_WINDOW_GET_RUS_CONFIG = 10076;
    public static final int COLOR_ZOOM_WINDOW_HIDE = 10073;
    public static final int COLOR_ZOOM_WINDOW_SET_RUS_CONFIG = 10077;
    public static final int COLOR_ZOOM_WINDOW_STATE = 10071;
    public static final int GET_ALL_TOP_APP_INFOS = 10053;
    public static final int GET_FREEFORM_CONFIG_LIST = 10054;
    public static final int GET_SPLIT_WINDOW_STATE = 10074;
    public static final int GET_TOP_ACTIVITY_COMPONENTNAME_TRANSACTION = 10007;
    public static final int GET_TOP_APPLICATION_INFO = 10011;
    public static final int IS_APP_CALL_REFUSE_MODE_TRANSACTION = 10035;
    public static final int IS_FREEFORM_ENABLED = 10055;
    public static final int IS_SUPPORT_EDGE_TOUCH_PREVENT = 10086;
    public static final int IS_ZOOM_WINDOW_ENABLED = 10078;
    public static final int REGISTER_APP_SWITCH_OBSERVER = 10064;
    public static final int REGISTER_KEY_EVENT_OBSERVER = 10093;
    public static final int REGISTER_LOCKSCREEN_CALLBACK = 10090;
    public static final int REGISTER_ZOOM_OBSERVER = 10069;
    public static final int REMOVE_FREEFORM_CONFIG_CHANGED_LISTENER = 10057;
    public static final int REMOVE_ZOOM_WINDOW_CONFIG_CHANGED_LISTENER = 10080;
    public static final int RESET_DEFAULT_EDGE_TOUCH_PREVENT_PARAM = 10085;
    public static final int SET_ALLOW_LAUNCH_APPS_TRANSACTION = 10067;
    public static final int SET_APP_CALL_REFUSE_MODE_TRANSACTION = 10036;
    public static final int SET_CHILD_SPACE_MODE_TRANSACTION = 10066;
    public static final int SET_DEFAULT_EDGE_TOUCH_PREVENT_PARAM = 10084;
    public static final int SET_EDGE_TOUCH_CALL_RULES = 10087;
    public static final int SET_EDGE_TOUCH_PREVENT_PARAM = 10083;
    public static final int SET_GIMBAL_LAUNCH_PKG = 10092;
    public static final int SET_SECURE_CONTROLLER_TRANSACTION = 10002;
    public static final int SPLIT_SCREEN_FOR_FLOAT_ASSISTENT = 10088;
    public static final int START_LOCK_DEVICE_MODE_TRANSACTION = 10081;
    public static final int STOP_LOCK_DEVICE_MODE_TRANSACTION = 10082;
    public static final int SWAP_STACK = 10052;
    public static final int UNREGISTER_APP_SWITCH_OBSERVER = 10065;
    public static final int UNREGISTER_KEY_EVENT_OBSERVER = 10094;
    public static final int UNREGISTER_LOCKSCREEN_CALLBACK = 10091;
    public static final int UNREGISTER_ZOOM_OBSERVER = 10070;
    public static final int UPDATE_APP_STATE_FOR_INTERCEPT = 10089;

    boolean addFreeformConfigChangedListener(IColorFreeformConfigChangedListener iColorFreeformConfigChangedListener) throws RemoteException;

    boolean addZoomWindowConfigChangedListener(IColorZoomWindowConfigChangedListener iColorZoomWindowConfigChangedListener) throws RemoteException;

    List<ColorAppInfo> getAllTopAppInfos() throws RemoteException;

    ColorZoomWindowInfo getCurrentZoomWindowState() throws RemoteException;

    List<String> getFreeformConfigList(int i) throws RemoteException;

    int getSplitScreenState(Intent intent) throws RemoteException;

    ComponentName getTopActivityComponentName() throws RemoteException;

    ApplicationInfo getTopApplicationInfo() throws RemoteException;

    List<String> getZoomAppConfigList(int i) throws RemoteException;

    ColorZoomWindowRUSConfig getZoomWindowConfig() throws RemoteException;

    void hideZoomWindow(int i) throws RemoteException;

    boolean isAppCallRefuseMode() throws RemoteException;

    boolean isFreeformEnabled() throws RemoteException;

    boolean isSupportEdgeTouchPrevent() throws RemoteException;

    boolean isSupportZoomWindowMode() throws RemoteException;

    boolean registerAppSwitchObserver(String str, IColorAppSwitchObserver iColorAppSwitchObserver, ColorAppSwitchConfig colorAppSwitchConfig) throws RemoteException;

    boolean registerKeyEventObserver(String str, IColorKeyEventObserver iColorKeyEventObserver, int i) throws RemoteException;

    boolean registerZoomWindowObserver(IColorZoomWindowObserver iColorZoomWindowObserver) throws RemoteException;

    boolean removeFreeformConfigChangedListener(IColorFreeformConfigChangedListener iColorFreeformConfigChangedListener) throws RemoteException;

    boolean removeZoomWindowConfigChangedListener(IColorZoomWindowConfigChangedListener iColorZoomWindowConfigChangedListener) throws RemoteException;

    boolean resetDefaultEdgeTouchPreventParam(String str) throws RemoteException;

    void setAllowLaunchApps(List<String> list) throws RemoteException;

    void setAppCallRefuseMode(boolean z) throws RemoteException;

    void setBubbleMode(boolean z) throws RemoteException;

    void setChildSpaceMode(boolean z) throws RemoteException;

    void setDefaultEdgeTouchPreventParam(String str, List<String> list) throws RemoteException;

    void setEdgeTouchCallRules(String str, Map<String, List<String>> map) throws RemoteException;

    void setGimbalLaunchPkg(String str) throws RemoteException;

    void setSecureController(IActivityController iActivityController) throws RemoteException;

    void setZoomWindowConfig(ColorZoomWindowRUSConfig colorZoomWindowRUSConfig) throws RemoteException;

    int startZoomWindow(Intent intent, Bundle bundle, int i, String str) throws RemoteException;

    void swapDockedFullscreenStack() throws RemoteException;

    boolean unregisterAppSwitchObserver(String str, ColorAppSwitchConfig colorAppSwitchConfig) throws RemoteException;

    boolean unregisterKeyEventObserver(String str) throws RemoteException;

    boolean unregisterZoomWindowObserver(IColorZoomWindowObserver iColorZoomWindowObserver) throws RemoteException;

    boolean writeEdgeTouchPreventParam(String str, String str2, List<String> list) throws RemoteException;

    default int splitScreenForEdgePanel(Intent intent, int userId) throws RemoteException {
        return 0;
    }
}
