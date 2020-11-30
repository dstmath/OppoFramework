package android.app;

import android.os.RemoteException;

public interface IColorStatusBarManager {
    public static final int COLOR_CALL_TRANSACTION_INDEX = 10000;
    public static final int COLOR_FIRST_CALL_TRANSACTION = 10001;
    public static final String DESCRIPTOR = "android.app.IStatusBarManager";
    public static final int GET_TOP_IS_FULLSCREEN = 10006;
    public static final int NOTIFY_CLICK_TOP = 10004;
    public static final int NOTIFY_MULTIWINDOW_FOCUS_CHANGED = 10010;
    public static final int REGISTER_COLOR_CLICK_TOP = 10003;
    public static final int REGISTER_COLOR_STATUS_BAR = 10002;
    public static final int SET_STATUSBAR_FUNCTION = 10008;
    public static final int TOGGLE_SPLIT_SCREEN = 10007;
    public static final int TOP_IS_FULLSCREEN = 10009;
    public static final int UNREGISTER_COLOR_CLICK_TOP = 10005;

    boolean getTopIsFullscreen() throws RemoteException;

    void notifyClickTop() throws RemoteException;

    void notifyMultiWindowFocusChanged(int i) throws RemoteException;

    void registerColorClickTopCallback(IColorClickTopCallback iColorClickTopCallback) throws RemoteException;

    void registerColorStatusBar(IColorStatusBar iColorStatusBar) throws RemoteException;

    boolean setStatusBarFunction(int i, String str) throws RemoteException;

    void toggleSplitScreen(int i) throws RemoteException;

    void topIsFullscreen(boolean z) throws RemoteException;

    void unregisterColorClickTopCallback(IColorClickTopCallback iColorClickTopCallback) throws RemoteException;
}
