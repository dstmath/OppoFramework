package com.android.internal.statusbar;

import android.app.IColorStatusBar;
import android.os.RemoteException;

public interface IColorStatusBarService {
    public static final int COLOR_CALL_TRANSACTION_INDEX = 20000;
    public static final int COLOR_FIRST_CALL_TRANSACTION = 20001;
    public static final String DESCRIPTOR = "com.android.internal.statusbar.IStatusBarService";
    public static final int NOTIFY_CLICK_TOP = 20004;
    public static final int REGISTER_COLOR_CLICK_TOP = 20003;
    public static final int REGISTER_COLOR_STATUS_BAR = 20002;
    public static final int UNREGISTER_COLOR_CLICK_TOP = 20005;

    void registerColorStatusBar(IColorStatusBar iColorStatusBar) throws RemoteException;
}
