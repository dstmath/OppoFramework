package android.view;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import com.color.darkmode.IColorDarkModeListener;

public interface IColorWindowManager extends IColorBaseWindowManager {
    public static final int CREATE_MONITOR_INPUT_CONSUMER = 10040;
    public static final int DESTROY_MONITOR_INPUT_CONSUMER = 10041;
    public static final int GET_APK_UNLOCK_WINDOW = 10004;
    public static final int GET_CURRENT_FOCUS_WINDOW = 10025;
    public static final int GET_FLOATWINDOW_RECT = 10030;
    public static final int GET_FOCUSED_WINDOW_IGNORE_HOME_MENU_KEY = 10056;
    public static final int GET_FREEFORM_STACK_BOUNDS = 10050;
    public static final int GET_IMEBG_COLOR_FROM_ADAPTATION = 10054;
    public static final int GET_NAVBAR_COLOR_FROM_ADAPTATION = 10052;
    public static final int GET_STATUSBAR_COLOR_FROM_ADAPTATION = 10053;
    public static final int GET_TYPED_WINDOW_LAYER = 10055;
    public static final int ICOLORWINDOWMANAGER_INDEX = 10001;
    public static final int IS_ACTIVITY_NEED_PALETTE = 10051;
    public static final int IS_FLOAT_WINDOW_FORBIDDEN = 10014;
    public static final int IS_FULL_SCREEN = 10011;
    public static final int IS_INPUT_SHOW = 10010;
    public static final int IS_IN_FREEFORM_MODE = 10049;
    public static final int IS_LOCK_ON_SHOW = 10006;
    public static final int IS_LOCK_WNDSHOW = 10002;
    public static final int IS_ROTATING = 10013;
    public static final int IS_SIM_UNLOCK_RUNNING = 10007;
    public static final int IS_STATUSBAR_VISIBLE = 10012;
    public static final int IS_WINDOW_SHOWN_FOR_UID = 10018;
    public static final int KEYGUARD_SET_APK_LOCKSCREEN_SHOWING = 10003;
    public static final int KEYGUARD_SHOE_SECURE_APKLOCK = 10005;
    public static final int OPEN_KEYGUARD_SESSION = 10020;
    public static final int PILFER_POINTERS = 10060;
    public static final int REGISTAER_UIMODE_CHANGE_LISTENER = 10057;
    public static final int REGISTER_OPPO_WINDOW_STATE_OBSERVER = 10047;
    public static final int REQUEST_DISMISS_KEYGUARD = 10017;
    public static final int REQUEST_KEYGUARD = 10019;
    public static final int REQUEST_REMOVE_WINDOW_ON_KEYGUARD = 10022;
    public static final int SET_BOOTANIM_ROTATION_LOCK = 10059;
    public static final int SET_GESTURE_FOLLOW_ANIMATION = 10042;
    public static final int SET_MAGNIFICATION_SPEC_EX = 10015;
    public static final int SET_SPLIT_TIMEOUT = 10036;
    public static final int START_COLOR_DRAG_WINDOW = 10046;
    public static final int UNREGISTAER_UIMODE_CHANGE_LISTENER = 10058;
    public static final int UNREGISTER_OPPO_WINDOW_STATE_OBSERVER = 10048;

    boolean checkIsFloatWindowForbidden(String str, int i) throws RemoteException;

    void createMonitorInputConsumer(IBinder iBinder, String str, InputChannel inputChannel) throws RemoteException;

    boolean destroyMonitorInputConsumer(String str) throws RemoteException;

    IBinder getApkUnlockWindow() throws RemoteException;

    String getCurrentFocus() throws RemoteException;

    Rect getFloatWindowRect(int i) throws RemoteException;

    int getFocusedWindowIgnoreHomeMenuKey() throws RemoteException;

    void getFreeformStackBounds(Rect rect) throws RemoteException;

    int getImeBgColorFromAdaptation(String str) throws RemoteException;

    int getNavBarColorFromAdaptation(String str, String str2) throws RemoteException;

    int getStatusBarColorFromAdaptation(String str, String str2) throws RemoteException;

    int getTypedWindowLayer(int i) throws RemoteException;

    boolean isActivityNeedPalette(String str, String str2) throws RemoteException;

    boolean isFullScreen() throws RemoteException;

    boolean isInFreeformMode() throws RemoteException;

    boolean isInputShow() throws RemoteException;

    boolean isLockOnShow() throws RemoteException;

    boolean isLockWndShow() throws RemoteException;

    boolean isRotatingLw() throws RemoteException;

    boolean isSIMUnlockRunning() throws RemoteException;

    boolean isStatusBarVisible() throws RemoteException;

    boolean isWindowShownForUid(int i) throws RemoteException;

    void keyguardSetApkLockScreenShowing(boolean z) throws RemoteException;

    void keyguardShowSecureApkLock(boolean z) throws RemoteException;

    void pilferPointers(String str) throws RemoteException;

    void registerOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener iColorDarkModeListener) throws RemoteException;

    void registerOppoWindowStateObserver(IOppoWindowStateObserver iOppoWindowStateObserver) throws RemoteException;

    void removeWindowShownOnKeyguard() throws RemoteException;

    void requestDismissKeyguard() throws RemoteException;

    void requestKeyguard(String str) throws RemoteException;

    void setBootAnimationRotationLock(boolean z) throws RemoteException;

    void setGestureFollowAnimation(boolean z) throws RemoteException;

    void setMagnification(Bundle bundle) throws RemoteException;

    void setMagnificationSpecEx(MagnificationSpec magnificationSpec) throws RemoteException;

    void setSplitTimeout(int i) throws RemoteException;

    void startColorDragWindow(String str, int i, int i2, Bundle bundle) throws RemoteException;

    void unregisterOnUiModeConfigurationChangeFinishListener(IColorDarkModeListener iColorDarkModeListener) throws RemoteException;

    void unregisterOppoWindowStateObserver(IOppoWindowStateObserver iOppoWindowStateObserver) throws RemoteException;
}
