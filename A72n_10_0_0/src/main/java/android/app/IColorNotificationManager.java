package android.app;

import android.os.RemoteException;

public interface IColorNotificationManager {
    public static final int CHECK_GET_OPENID_TRANSACTION = 10018;
    public static final int CLEAR_OPENID_TRANSACTION = 10011;
    public static final int COLOR_CALL_TRANSACTION_INDEX = 10000;
    public static final int COLOR_FIRST_CALL_TRANSACTION = 10001;
    public static final String DESCRIPTOR = "android.app.INotificationManager";
    public static final int GET_BADGE_OPTION_TRANSACTION = 10013;
    public static final int GET_ENABLE_NAVIGATION_APPS_TRANSACTION = 10007;
    public static final int GET_NAVIGATION_MODE_TRANSACTION = 10004;
    public static final int GET_OPENID_TRANSACTION = 10010;
    public static final int GET_STOW_OPTION_TRANSACTION = 10016;
    public static final int IS_DRIVE_NAVIGATION_MODE_TRANSACTION = 10005;
    public static final int IS_NAVIGATION_MODE_TRANSACTION = 10006;
    public static final int IS_NUMBADGE_SUPPORT_TRANSACTION = 10014;
    public static final int IS_SUPPRESSED_BY_DRIVEMODE_TRANSACTION = 10008;
    public static final int SET_BADGE_OPTION_TRANSACTION = 10012;
    public static final int SET_NUMBADGE_SUPPORT_TRANSACTION = 10015;
    public static final int SET_STOW_OPTION_TRANSACTION = 10017;
    public static final int SET_SUPPRESSED_BY_DRIVEMODE_TRANSACTION = 10009;
    public static final int SHOULD_INTERCEPT_SOUND_TRANSACTION = 10002;
    public static final int SHOULD_KEEP_ALIVE_TRANSACTION = 10003;

    boolean checkGetOpenid(String str, int i, String str2) throws RemoteException;

    void clearOpenid(String str, int i, String str2) throws RemoteException;

    int getBadgeOption(String str, int i) throws RemoteException;

    String[] getEnableNavigationApps(int i) throws RemoteException;

    int getNavigationMode(String str, int i) throws RemoteException;

    String getOpenid(String str, int i, String str2) throws RemoteException;

    int getStowOption(String str, int i) throws RemoteException;

    boolean isDriveNavigationMode(String str, int i) throws RemoteException;

    boolean isNavigationMode(int i) throws RemoteException;

    boolean isNumbadgeSupport(String str, int i) throws RemoteException;

    boolean isSuppressedByDriveMode(int i) throws RemoteException;

    void setBadgeOption(String str, int i, int i2) throws RemoteException;

    void setNumbadgeSupport(String str, int i, boolean z) throws RemoteException;

    void setStowOption(String str, int i, int i2) throws RemoteException;

    void setSuppressedByDriveMode(boolean z, int i) throws RemoteException;

    boolean shouldInterceptSound(String str, int i) throws RemoteException;

    boolean shouldKeepAlive(String str, int i) throws RemoteException;
}
