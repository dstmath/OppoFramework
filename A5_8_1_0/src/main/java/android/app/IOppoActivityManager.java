package android.app;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;
import com.color.util.ColorAccidentallyTouchData;
import com.color.util.ColorDisplayCompatData;
import com.color.util.ColorDisplayOptimizationData;
import com.color.util.ColorFormatterCompatibilityData;
import com.color.util.ColorPackageFreezeData;
import com.color.util.ColorResolveData;
import com.color.util.ColorSecureKeyboardData;
import com.oppo.app.IOppoAppFreezeController;
import com.oppo.app.IOppoAppStartController;
import com.oppo.app.IOppoGameSpaceController;
import java.util.ArrayList;
import java.util.List;

public interface IOppoActivityManager {
    public static final int ACCIDENTALLY_TOUCH_TRANSACTION = 10015;
    public static final int ADD_FAST_APP_WECHAT_PAY = 10038;
    public static final int ADD_FORMATER_APPCOMPAT_TRANSACTION = 10040;
    public static final int ADD_MINI_PROGRAM_SHARE = 10031;
    public static final int ADD_STAGE_PROTECT_INFO = 10021;
    public static final int DECREASE_RUTIL_USED_COUNT = 10010;
    public static final String DESCRIPTOR = "android.app.IActivityManager";
    public static final int DIRECT_REFLECT_TRANSACTION = 10037;
    public static final int DISPLAY_COMPAT_TRANSACTION = 10027;
    public static final int DISPLAY_OPTIMIZATION_TRANSACTION = 10023;
    public static final int GET_ALIAS_MULTIAPP = 10030;
    public static final int GET_ALLOWED_MULTIAPP = 10028;
    public static final int GET_ASSOCIATED_PROCESS_FOR_PID = 10017;
    public static final int GET_ASSOCIATED_PROCESS_FOR_PKG = 10018;
    public static final int GET_ASSOCIATE_ACTIVITY_APP_LIST = 10045;
    public static final int GET_CREATED_MULTIAPP = 10029;
    public static final int GET_GLOBAL_PKG_WHITE_LIST = 10019;
    public static final int GET_GLOBAL_PROCESS_WHITE_LIST = 10020;
    public static final int GET_RUNNING_PROCESSES = 10041;
    public static final int GET_STAGE_PROTECT_LIST_FROM_PKG = 10025;
    public static final int GET_TOP_ACTIVITY_COMPONENTNAME_TRANSACTION = 10007;
    public static final int GET_TOP_APPLICATION_INFO = 10011;
    public static final int GRANT_OPPO_PERMISSION_GROUP = 10012;
    public static final int HANDLE_APP_FOR_NOTIFICATION = 10014;
    public static final int HANDLE_APP_FROM_CONTROL_CENTER = 10026;
    public static final int INCREASE_RUTIL_USED_COUNT = 10009;
    public static final int IS_APP_CALL_REFUSE_MODE_TRANSACTION = 10035;
    public static final int IS_PERMISSION_INTERCEPT_ENABLED = 10005;
    public static final int KILL_PID_FORCE = 10008;
    public static final int LAUNCH_RUTILS_TRANSACTION = 10033;
    public static final int OPPO_CALL_TRANSACTION_INDEX = 10000;
    public static final int OPPO_FIRST_CALL_TRANSACTION = 10001;
    public static final int REMOVE_FAST_APP_WECHAT_PAY = 10039;
    public static final int REMOVE_MINI_PROGRAM_SHARE = 10032;
    public static final int REMOVE_STAGE_PROTECT_INFO = 10022;
    public static final int RESOLVE_TRANSACTION = 10034;
    public static final int REVOKE_OPPO_PERMISSION_GROUP = 10013;
    public static final int SECURE_KEYBOARD_TRANSACTION = 10024;
    public static final int SET_APP_CALL_REFUSE_MODE_TRANSACTION = 10036;
    public static final int SET_APP_FREEZE_CONTROLLER = 10044;
    public static final int SET_GAME_CONTROLLER_TRANSACTION = 10016;
    public static final int SET_PERMISSION_INTERCEPT_ENABLE = 10004;
    public static final int SET_PROPERTIES_TRANSACTION = 10006;
    public static final int SET_PRVENT_START_CONTROLLER = 10042;
    public static final int SET_SECURE_CONTROLLER_TRANSACTION = 10002;
    public static final int SET_START_MONITOR_CONTROLLER = 10043;
    public static final int UPDATE_PERMISSION_CHOICE = 10003;

    public interface Service {
    }

    void addFastAppWechatPay(String str, String str2) throws RemoteException;

    void addMiniProgramShare(String str, String str2, String str3) throws RemoteException;

    void addStageProtectInfo(String str, String str2, long j) throws RemoteException;

    void decreaseRutilsUsedCount() throws RemoteException;

    ColorAccidentallyTouchData getAccidentallyTouchData() throws RemoteException;

    String getAliasByPackage(String str) throws RemoteException;

    List<String> getAllowedMultiApp() throws RemoteException;

    List<String> getAppAssociatedActivity(String str) throws RemoteException;

    List<String> getAppAssociatedProcess(int i) throws RemoteException;

    List<String> getAppAssociatedProcess(String str) throws RemoteException;

    List<String> getCreatedMultiApp() throws RemoteException;

    ColorDisplayCompatData getDisplayCompatData() throws RemoteException;

    ColorDisplayOptimizationData getDisplayOptimizationData() throws RemoteException;

    ColorFormatterCompatibilityData getFormatterCompatData() throws RemoteException;

    ArrayList<String> getGlobalPkgWhiteList(int i) throws RemoteException;

    ArrayList<String> getGlobalProcessWhiteList() throws RemoteException;

    ColorResolveData getResolveData() throws RemoteException;

    List<ColorPackageFreezeData> getRunningProcesses() throws RemoteException;

    ColorSecureKeyboardData getSecureKeyboardData() throws RemoteException;

    ArrayList<String> getStageProtectListFromPkg(String str, int i) throws RemoteException;

    ComponentName getTopActivityComponentName() throws RemoteException;

    ApplicationInfo getTopApplicationInfo() throws RemoteException;

    void grantOppoPermissionByGroup(String str, String str2) throws RemoteException;

    void handleAppForNotification(String str, int i, int i2) throws RemoteException;

    void handleAppFromControlCenter(String str, int i) throws RemoteException;

    void increaseRutilsUsedCount() throws RemoteException;

    boolean isAppCallRefuseMode() throws RemoteException;

    boolean isPermissionInterceptEnabled() throws RemoteException;

    void killPidForce(int i) throws RemoteException;

    void launchRutils() throws RemoteException;

    void removeFastAppWechatPay(String str, String str2) throws RemoteException;

    void removeMiniProgramShare(String str, String str2, String str3) throws RemoteException;

    void removeStageProtectInfo(String str, String str2) throws RemoteException;

    void revokeOppoPermissionByGroup(String str, String str2) throws RemoteException;

    void setAppCallRefuseMode(boolean z) throws RemoteException;

    void setAppFreezeController(IOppoAppFreezeController iOppoAppFreezeController) throws RemoteException;

    void setAppStartMonitorController(IOppoAppStartController iOppoAppStartController) throws RemoteException;

    void setGameSpaceController(IOppoGameSpaceController iOppoGameSpaceController) throws RemoteException;

    void setPermissionInterceptEnable(boolean z) throws RemoteException;

    void setPreventStartController(IOppoAppStartController iOppoAppStartController) throws RemoteException;

    void setSecureController(IActivityController iActivityController) throws RemoteException;

    void setSystemProperties(String str, String str2) throws RemoteException;

    void updatePermissionChoice(String str, String str2, int i) throws RemoteException;
}
