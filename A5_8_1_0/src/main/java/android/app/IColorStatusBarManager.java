package android.app;

import android.os.RemoteException;

public interface IColorStatusBarManager {
    public static final int COLOR_CALL_TRANSACTION_INDEX = 10000;
    public static final int COLOR_FIRST_CALL_TRANSACTION = 10001;
    public static final String DESCRIPTOR = "com.android.internal.statusbar.IStatusBarService";
    public static final int HIDE_NAVIGATION_BAR = 10003;
    public static final int SHOW_NAVIGATION_BAR = 10002;
    public static final int UPDATE_TRANSITION_VIEW = 10004;

    void hideNavigationBar() throws RemoteException;

    void showNavigationBar() throws RemoteException;

    void updateTransitionView(int i) throws RemoteException;
}
