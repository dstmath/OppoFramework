package android.view;

import android.app.IColorKeyguardSessionCallback;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.RemoteException;

public interface IOppoWindowManager extends IColorLongshotWindowManager {
    public static final String DESCRIPTOR = "android.view.IWindowManager";
    public static final int GET_APK_UNLOCK_WINDOW = 10004;
    public static final int GET_CURRENT_FOCUS_WINDOW = 10025;
    public static final int GET_FLOATWINDOW_RECT = 10030;
    public static final int GET_FOCUSED_WINDOW_FRAME = 10016;
    public static final int GET_LONGSHOT_SURFACE_LAYER = 10023;
    public static final int GET_LONGSHOT_SURFACE_LAYER_BY_TYPE = 10024;
    public static final int IS_FLOAT_WINDOW_FORBIDDEN = 10014;
    public static final int IS_FULL_SCREEN = 10011;
    public static final int IS_INPUT_SHOW = 10010;
    public static final int IS_KEYGUARD_SHOWING_AND_NOT_OCCLUDED = 10029;
    public static final int IS_LOCK_ON_SHOW = 10006;
    public static final int IS_LOCK_WNDSHOW = 10002;
    public static final int IS_NAVIGATIONBAR_VISIBLE = 10028;
    public static final int IS_ROTATING = 10013;
    public static final int IS_SHORTCUTS_PANEL_SHOW = 10031;
    public static final int IS_SIM_UNLOCK_RUNNING = 10007;
    public static final int IS_STATUSBAR_VISIBLE = 10012;
    public static final int IS_WINDOW_SHOWN_FOR_UID = 10018;
    public static final int KEYGUARD_SET_APK_LOCKSCREEN_SHOWING = 10003;
    public static final int KEYGUARD_SHOE_SECURE_APKLOCK = 10005;
    public static final int LONGSHOT_INJECT_INPUT = 10021;
    public static final int LONGSHOT_INJECT_INPUT_BEGIN = 10032;
    public static final int LONGSHOT_INJECT_INPUT_END = 10033;
    public static final int LONGSHOT_NOTIFY_CONNECTED = 10027;
    public static final int OPEN_KEYGUARD_SESSION = 10020;
    public static final int OPPO_CALL_TRANSACTION_INDEX = 10000;
    public static final int OPPO_FIRST_CALL_TRANSACTION = 10001;
    public static final int REQUEST_DISMISS_KEYGUARD = 10017;
    public static final int REQUEST_KEYGUARD = 10019;
    public static final int REQUEST_REMOVE_WINDOW_ON_KEYGUARD = 10022;
    public static final int SET_MAGNIFICATION_SPEC_EX = 10015;

    boolean checkIsFloatWindowForbidden(String str, int i) throws RemoteException;

    String getCurrentFocus() throws RemoteException;

    Rect getFloatWindowRect(int i) throws RemoteException;

    boolean isFullScreen() throws RemoteException;

    boolean isInputShow() throws RemoteException;

    boolean isRotatingLw() throws RemoteException;

    boolean isStatusBarVisible() throws RemoteException;

    boolean isWindowShownForUid(int i) throws RemoteException;

    boolean openKeyguardSession(IColorKeyguardSessionCallback iColorKeyguardSessionCallback, IBinder iBinder, String str) throws RemoteException;

    void removeWindowShownOnKeyguard() throws RemoteException;

    void requestDismissKeyguard() throws RemoteException;

    void requestKeyguard(String str) throws RemoteException;

    void setMagnificationSpecEx(MagnificationSpec magnificationSpec) throws RemoteException;
}
