package android.app;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import com.color.util.ColorAccidentallyTouchData;
import com.color.util.ColorDisplayCompatData;
import com.color.util.ColorDisplayOptimizationData;
import com.color.util.ColorSecureKeyboardData;
import com.oppo.app.IOppoGameSpaceController;
import java.util.ArrayList;
import java.util.List;

public interface IOppoActivityManager {
    public static final int ACCIDENTALLY_TOUCH_TRANSACTION = 10016;
    public static final int ADD_MINI_PROGRAM_SHARE = 10029;
    public static final int ADD_STAGE_PROTECT_INFO = 10022;
    public static final int DECREASE_RUTIL_USED_COUNT = 10010;
    public static final int DISPLAY_COMPAT_TRANSACTION = 10028;
    public static final int DISPLAY_OPTIMIZATION_TRANSACTION = 10024;
    public static final int GET_ASSOCIATED_PROCESS_FOR_PID = 10018;
    public static final int GET_ASSOCIATED_PROCESS_FOR_PKG = 10019;
    public static final int GET_GLOBAL_PKG_WHITE_LIST = 10020;
    public static final int GET_GLOBAL_PROCESS_WHITE_LIST = 10021;
    public static final int GET_STAGE_PROTECT_LIST_FROM_PKG = 10026;
    public static final int GET_TOP_ACTIVITY_COMPONENTNAME_TRANSACTION = 10007;
    public static final int GET_TOP_APPLICATION_INFO = 10012;
    public static final int GRANT_OPPO_PERMISSION_GROUP = 10013;
    public static final int HANDLE_APP_FOR_NOTIFICATION = 10015;
    public static final int HANDLE_APP_FROM_CONTROL_CENTER = 10027;
    public static final int INCREASE_RUTIL_USED_COUNT = 10009;
    public static final int IS_BROWSER_INTERCEPT_WHITELIST_TRANSACTION = 10011;
    public static final int IS_PERMISSION_INTERCEPT_ENABLED = 10005;
    public static final int KILL_PID_FORCE = 10008;
    public static final int LAUNCH_RUTILS_TRANSACTION = 10031;
    public static final int OPPO_CALL_TRANSACTION_INDEX = 10000;
    public static final int OPPO_FIRST_CALL_TRANSACTION = 10001;
    public static final int REMOVE_MINI_PROGRAM_SHARE = 10030;
    public static final int REMOVE_STAGE_PROTECT_INFO = 10023;
    public static final int REVOKE_OPPO_PERMISSION_GROUP = 10014;
    public static final int SECURE_KEYBOARD_TRANSACTION = 10025;
    public static final int SET_GAME_CONTROLLER_TRANSACTION = 10017;
    public static final int SET_PERMISSION_INTERCEPT_ENABLE = 10004;
    public static final int SET_PROPERTIES_TRANSACTION = 10006;
    public static final int SET_SECURE_CONTROLLER_TRANSACTION = 10002;
    public static final int UPDATE_PERMISSION_CHOICE = 10003;

    public interface Service {
        int checkPermissionForProc(String str, int i, int i2, int i3, Object obj);
    }

    void addMiniProgramShare(String str, String str2, String str3) throws RemoteException;

    void addStageProtectInfo(String str, String str2, long j) throws RemoteException;

    void decreaseRutilsUsedCount() throws RemoteException;

    ColorAccidentallyTouchData getAccidentallyTouchData() throws RemoteException;

    List<String> getAppAssociatedProcess(int i) throws RemoteException;

    List<String> getAppAssociatedProcess(String str) throws RemoteException;

    ColorDisplayCompatData getDisplayCompatData() throws RemoteException;

    ColorDisplayOptimizationData getDisplayOptimizationData() throws RemoteException;

    ArrayList<String> getGlobalPkgWhiteList(int i) throws RemoteException;

    ArrayList<String> getGlobalProcessWhiteList() throws RemoteException;

    ColorSecureKeyboardData getSecureKeyboardData() throws RemoteException;

    ArrayList<String> getStageProtectListFromPkg(String str, int i) throws RemoteException;

    ComponentName getTopActivityComponentName() throws RemoteException;

    ApplicationInfo getTopApplicationInfo() throws RemoteException;

    void grantOppoPermissionByGroup(String str, String str2) throws RemoteException;

    void handleAppForNotification(String str, int i, int i2) throws RemoteException;

    void handleAppFromControlCenter(String str, int i) throws RemoteException;

    void increaseRutilsUsedCount() throws RemoteException;

    boolean isInBrowserInterceptWhiteList(String str) throws RemoteException;

    boolean isPermissionInterceptEnabled() throws RemoteException;

    void killPidForce(int i) throws RemoteException;

    void launchRutils() throws RemoteException;

    void removeMiniProgramShare(String str, String str2, String str3) throws RemoteException;

    void removeStageProtectInfo(String str, String str2) throws RemoteException;

    void revokeOppoPermissionByGroup(String str, String str2) throws RemoteException;

    void setGameSpaceController(IOppoGameSpaceController iOppoGameSpaceController) throws RemoteException;

    void setPermissionInterceptEnable(boolean z) throws RemoteException;

    void setSecureController(IActivityController iActivityController) throws RemoteException;

    void setSystemProperties(String str, String str2) throws RemoteException;

    void updatePermissionChoice(String str, String str2, int i) throws RemoteException;
}
