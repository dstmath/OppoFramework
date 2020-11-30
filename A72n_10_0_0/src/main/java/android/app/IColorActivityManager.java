package android.app;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import com.color.app.IColorHansListener;
import com.color.util.ColorAccidentallyTouchData;
import com.color.util.ColorDisplayCompatData;
import com.color.util.ColorDisplayOptimizationData;
import com.color.util.ColorPackageFreezeData;
import com.color.util.ColorProcDependData;
import com.color.util.ColorReflectData;
import com.color.util.ColorResolveData;
import com.color.util.ColorSecureKeyboardData;
import com.oppo.app.IOppoAppStartController;
import com.oppo.app.IOppoGameSpaceController;
import com.oppo.app.IOppoPermissionRecordController;
import java.util.ArrayList;
import java.util.List;

public interface IColorActivityManager extends IColorBaseActivityManager {
    public static final int ACCIDENTALLY_TOUCH_TRANSACTION = 10015;
    public static final int ADD_BACKGROUND_RESTRICTED_INFO = 10049;
    public static final int ADD_FAST_APP_THIRD_LOGIN = 10046;
    public static final int ADD_FAST_APP_WECHAT_PAY = 10038;
    public static final int ADD_MINI_PROGRAM_SHARE = 10031;
    public static final int ADD_PREVENT_INDULGE_LIST = 10051;
    public static final int ADD_STAGE_PROTECT_INFO = 10021;
    public static final int COLOR_ANIMATION_TRANSACTION = 10058;
    public static final int DARKMODE_GET_APP_DATA = 10077;
    public static final int DARKMODE_IS_IN_UPAPP_LIST = 10068;
    public static final int DECREASE_RUTIL_USED_COUNT = 10010;
    public static final int DIRECT_REFLECT_TRANSACTION = 10037;
    public static final int DISPLAY_COMPAT_TRANSACTION = 10027;
    public static final int DISPLAY_OPTIMIZATION_TRANSACTION = 10023;
    public static final int FAVORITE_QUERY_RULE = 10048;
    public static final int FINISH_NOT_ORDER_RECEIVER_TRANSACTION = 10081;
    public static final int FORCE_TRIM_APP_MEMORY = 10067;
    public static final int GET_ALIAS_MULTIAPP = 10030;
    public static final int GET_ALLOWED_MULTIAPP = 10028;
    public static final int GET_ASSOCIATED_PROCESS_FOR_PID = 10017;
    public static final int GET_ASSOCIATED_PROCESS_FOR_PKG = 10018;
    public static final int GET_ASSOCIATE_ACTIVITY_APP_LIST = 10045;
    public static final int GET_CONFIG_INFO = 10063;
    public static final int GET_CPU_WORKING_STATS = 10061;
    public static final int GET_CREATED_MULTIAPP = 10029;
    public static final int GET_GLOBAL_PKG_WHITE_LIST = 10019;
    public static final int GET_GLOBAL_PROCESS_WHITE_LIST = 10020;
    public static final int GET_IS_SUPPORT_MULTIAPP = 10059;
    public static final int GET_PROC_CMDLINE = 10079;
    public static final int GET_PROC_COMMON_INFO_LIST = 10070;
    public static final int GET_PROC_DEPENDENCE_PACKAGE = 10072;
    public static final int GET_PROC_DEPENDENCE_PID = 10071;
    public static final int GET_RUNNING_PROCESSES = 10041;
    public static final int GET_STAGE_PROTECT_LIST_FROM_PKG = 10025;
    public static final int GET_TASK_PACKAGE_LIST = 10073;
    public static final int GET_UID_CPU_WORKING_STATS = 10076;
    public static final int GRANT_OPPO_PERMISSION_GROUP = 10012;
    public static final int HANDLE_APP_FOR_NOTIFICATION = 10014;
    public static final int HANDLE_APP_FROM_CONTROL_CENTER = 10026;
    public static final int HANS_NATIVE_UNFREEZE_TRANSACTION = 10040;
    public static final int INCREASE_RUTIL_USED_COUNT = 10009;
    public static final int IS_PERMISSION_INTERCEPT_ENABLED = 10005;
    public static final int KILL_PID_FORCE = 10008;
    public static final int LAUNCH_RUTILS_TRANSACTION = 10033;
    public static final int PUT_CONFIG_INFO = 10062;
    public static final int REGISTER_HANS_LISTENER = 10083;
    public static final int REMOVE_FAST_APP_THIRD_LOGIN = 10047;
    public static final int REMOVE_FAST_APP_WECHAT_PAY = 10039;
    public static final int REMOVE_MINI_PROGRAM_SHARE = 10032;
    public static final int REMOVE_STAGE_PROTECT_INFO = 10022;
    public static final int REPORT_SKIPPED_FRAMES = 10082;
    public static final int REPORT_SKIPPED_FRAMES_WITH_FLAG = 10085;
    public static final int RESOLVE_TRANSACTION = 10034;
    public static final int REVOKE_OPPO_PERMISSION_GROUP = 10013;
    public static final int SECURE_KEYBOARD_TRANSACTION = 10024;
    public static final int SET_APP_FREEZE = 10078;
    public static final int SET_APP_FREEZE_CONTROLLER = 10044;
    public static final int SET_GAME_CONTROLLER_TRANSACTION = 10016;
    public static final int SET_PERMISSION_INTERCEPT_ENABLE = 10004;
    public static final int SET_PERMISSION_RECORD_CONTROLLER = 10066;
    public static final int SET_PREVENT_INDULGE_CONTROLLER = 10050;
    public static final int SET_PROPERTIES_TRANSACTION = 10006;
    public static final int SET_PRVENT_START_CONTROLLER = 10042;
    public static final int SET_START_MONITOR_CONTROLLER = 10043;
    public static final int SYNC_PERMISSION_RECORD = 10074;
    public static final int SYSTEM_DUMP_PROC_PERF_DATA = 10069;
    public static final int TRIGGER_PROC_ACTIVE_GC = 10080;
    public static final int UNREGISTER_HANS_LISTENER = 10084;
    public static final int UPDATE_CPU_TRACKER = 10060;
    public static final int UPDATE_PERMISSION_CHOICE = 10003;
    public static final int UPDATE_UID_CPU_TRACKER = 10075;

    void activeGc(int[] iArr) throws RemoteException;

    void addBackgroundRestrictedInfo(String str, List<String> list) throws RemoteException;

    void addFastAppThirdLogin(String str, String str2) throws RemoteException;

    void addFastAppWechatPay(String str, String str2) throws RemoteException;

    void addMiniProgramShare(String str, String str2, String str3) throws RemoteException;

    void addPreventIndulgeList(List<String> list) throws RemoteException;

    void addStageProtectInfo(String str, String str2, long j) throws RemoteException;

    void decreaseRutilsUsedCount() throws RemoteException;

    boolean dumpProcPerfData(Bundle bundle) throws RemoteException;

    void finishNotOrderReceiver(IBinder iBinder, int i, int i2, String str, Bundle bundle, boolean z) throws RemoteException;

    void forceTrimAppMemory(int i) throws RemoteException;

    ColorAccidentallyTouchData getAccidentallyTouchData() throws RemoteException;

    String getAliasMultiApp(String str) throws RemoteException;

    List<String> getAllowedMultiApp() throws RemoteException;

    List<String> getAppAssociatedActivity(String str) throws RemoteException;

    List<String> getAppAssociatedProcess(int i) throws RemoteException;

    List<String> getAppAssociatedProcess(String str) throws RemoteException;

    Bundle getConfigInfo(String str, int i, int i2) throws RemoteException;

    List<String> getCreatedMultiApp() throws RemoteException;

    ColorDisplayCompatData getDisplayCompatData() throws RemoteException;

    ColorDisplayOptimizationData getDisplayOptimizationData() throws RemoteException;

    ArrayList<String> getGlobalPkgWhiteList(int i) throws RemoteException;

    ArrayList<String> getGlobalProcessWhiteList() throws RemoteException;

    List<String> getProcCmdline(int[] iArr) throws RemoteException;

    List<String> getProcCommonInfoList(int i) throws RemoteException;

    List<ColorProcDependData> getProcDependency(int i) throws RemoteException;

    List<ColorProcDependData> getProcDependency(String str, int i) throws RemoteException;

    ColorReflectData getReflectData() throws RemoteException;

    ColorResolveData getResolveData() throws RemoteException;

    List<ColorPackageFreezeData> getRunningProcesses() throws RemoteException;

    ColorSecureKeyboardData getSecureKeyboardData() throws RemoteException;

    ArrayList<String> getStageProtectListFromPkg(String str, int i) throws RemoteException;

    List<String> getTaskPkgList(int i) throws RemoteException;

    void grantOppoPermissionByGroup(String str, String str2) throws RemoteException;

    void handleAppForNotification(String str, int i, int i2) throws RemoteException;

    void handleAppFromControlCenter(String str, int i) throws RemoteException;

    void increaseRutilsUsedCount() throws RemoteException;

    boolean isPermissionInterceptEnabled() throws RemoteException;

    void killPidForce(int i) throws RemoteException;

    void launchRutils() throws RemoteException;

    boolean putConfigInfo(String str, Bundle bundle, int i, int i2) throws RemoteException;

    boolean registerHansListener(String str, IColorHansListener iColorHansListener) throws RemoteException;

    void removeFastAppThirdLogin(String str, String str2) throws RemoteException;

    void removeFastAppWechatPay(String str, String str2) throws RemoteException;

    void removeMiniProgramShare(String str, String str2, String str3) throws RemoteException;

    void removeStageProtectInfo(String str, String str2) throws RemoteException;

    void reportSkippedFrames(long j, long j2) throws RemoteException;

    void reportSkippedFrames(long j, boolean z, boolean z2, long j2) throws RemoteException;

    void revokeOppoPermissionByGroup(String str, String str2) throws RemoteException;

    boolean setAppFreeze(String str, Bundle bundle) throws RemoteException;

    void setAppStartMonitorController(IOppoAppStartController iOppoAppStartController) throws RemoteException;

    void setGameSpaceController(IOppoGameSpaceController iOppoGameSpaceController) throws RemoteException;

    void setPermissionInterceptEnable(boolean z) throws RemoteException;

    void setPermissionRecordController(IOppoPermissionRecordController iOppoPermissionRecordController) throws RemoteException;

    void setPreventIndulgeController(IOppoAppStartController iOppoAppStartController) throws RemoteException;

    void setPreventStartController(IOppoAppStartController iOppoAppStartController) throws RemoteException;

    void setSystemProperties(String str, String str2) throws RemoteException;

    void syncPermissionRecord() throws RemoteException;

    boolean unregisterHansListener(String str, IColorHansListener iColorHansListener) throws RemoteException;

    void updatePermissionChoice(String str, String str2, int i) throws RemoteException;
}
