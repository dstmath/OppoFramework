package com.android.server.am;

import android.app.ApplicationErrorReport;
import android.app.BroadcastOptions;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemProperties;
import com.android.server.wm.IColorActivityRecordEx;
import com.oppo.app.IOppoAppStartController;
import java.util.List;

public interface IColorAppStartupManager extends IOppoCommonFeature {
    public static final String APP_START_BY_ASSOCIATE = "associate";
    public static final String APP_START_BY_BOOTSTART = "bootstart";
    public static final String APP_START_BY_CLICK = "click";
    public static final String APP_START_BY_OTHER = "other";
    public static final String APP_START_BY_SAMEAPP = "sameapp";
    public static final String APP_START_BY_WIDGET = "widget";
    public static final boolean DEBUG_DETAIL = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final IColorAppStartupManager DEFAULT = new IColorAppStartupManager() {
        /* class com.android.server.am.IColorAppStartupManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorAppStartupManager";
    public static final String REBIND_FORCE_STOP = "forceStop";
    public static final String REBIND_OTHER_ALLOW = "otherAllow";
    public static final String REBIND_SETTING_CHANGE = "settingChange";
    public static final String REBIND_USER_UNLOCK = "userUnlock";
    public static final String START_PROCESS_FROM_ALARM = "system[alarmManger]";
    public static final String START_PROCESS_FROM_JOB = "system[jobScheduler]";
    public static final String START_PROCESS_FROM_LOCATION = "system[location]";
    public static final String START_PROCESS_FROM_NOTIFICATION_LISTENER = "system[notificationListener]";
    public static final String TAG = "ColorAppStartupManager";
    public static final String TYPE_ACTIVITY = "activity";
    public static final String TYPE_BIND_SERVICE = "bs";
    public static final String TYPE_BIND_SERVICE_ACTION = "bsa";
    public static final String TYPE_BIND_SERVICE_FORM_NOTIFICATION = "bsfn";
    public static final String TYPE_BIND_SERVICE_FROM_JOB = "bsfj";
    public static final String TYPE_BIND_SERVICE_FROM_SYNC = "bsfs";
    public static final String TYPE_BIND_SERVICE_FROM_SYSTEMUI = "bs_systemui";
    public static final String TYPE_BROADCAST = "broadcast";
    public static final String TYPE_BROADCAST_ACTION = "broadcast_action";
    public static final String TYPE_DIALOG = "dialog";
    public static final String TYPE_PROVIDER = "provider";
    public static final String TYPE_SERVICE = "service";
    public static final String TYPE_START_PROCEESS_LOECKED = "startProcessLocked";
    public static final String TYPE_START_SERVICE = "ss";
    public static final String TYPE_START_SERVICE_ACTION = "ssa";
    public static final String TYPE_START_SERVICE_CALL_NULL = "sscn";
    public static final String TYPE_START_SERVICE_FROM_ALARM = "ssfa";
    public static final String UPLOAD_BLACK_EVENTID = "intercept_black";
    public static final String UPLOAD_GAME_PAY = "game_pay";
    public static final String UPLOAD_GAME_PAY_MONITOR = "game_pay_monitor";
    public static final String UPLOAD_KEY_CALLED_CPN = "cpn";
    public static final String UPLOAD_KEY_CALLED_PKG = "calledPkg";
    public static final String UPLOAD_KEY_CALLER_APP = "callerapp";
    public static final String UPLOAD_KEY_CALLER_CPN = "callerCpn";
    public static final String UPLOAD_KEY_CALLER_PID = "callerpid";
    public static final String UPLOAD_KEY_CALLER_PKG = "callerPkg";
    public static final String UPLOAD_KEY_HOST_TYPE = "host_type";
    public static final String UPLOAD_KEY_POPUP_TYPE = "popup_type";
    public static final String UPLOAD_KEY_PROC_COUNT = "count";
    public static final String UPLOAD_KEY_PROC_NAME = "proc";
    public static final String UPLOAD_KEY_SCREEN_STATE = "screenState";
    public static final String UPLOAD_KEY_START_MODE = "start_mode";
    public static final String UPLOAD_KEY_TOP_PKG = "topPkg";
    public static final String UPLOAD_LOGTAG = "20089";
    public static final String UPLOAD_THIRD_EVENTID = "startup_third_app";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAppStartupManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean getSwitchMonitor() {
        return false;
    }

    default boolean getDynamicDebug() {
        return false;
    }

    default String getPackageNameForUid(int uid) {
        return null;
    }

    default String composePackage(String pre, String suf) {
        return null;
    }

    default boolean inAssociateStartWhiteList(String packageName, int userId) {
        return false;
    }

    default void init(IColorActivityManagerServiceEx amsEx) {
    }

    default boolean shouldPreventProcessBroadcast(BroadcastRecord record, ResolveInfo info, String type) {
        return false;
    }

    default boolean shouldPreventStartProvider(ProcessRecord proc, ContentProviderRecord providerRecord, ApplicationInfo appInfo) {
        return false;
    }

    default boolean shouldPreventStartService(ServiceRecord record, int callingPid, int callingUid, ProcessRecord callerApp, String callerPkg, Intent service, String type) {
        return false;
    }

    default boolean shouldPreventStartActivity(Intent intent, Intent ephemeralIntent, ActivityInfo activityInfo, String callingPackage, int callingPid, int callingUid, String reason, int userId) {
        return false;
    }

    default boolean shouldPreventNotification(ComponentName component, String rebindType, int userId) {
        return false;
    }

    default void monitorAppStartupInfo(int callingPid, int callingUid, ProcessRecord callerApp, Intent intent, ApplicationInfo appInfo, String hostType) {
    }

    default void handleBroadcastIncludeForceStop(Intent intent, ProcessRecord record) {
    }

    default boolean isRemovePendingJob(String packageName, int userId) {
        return false;
    }

    default boolean checkPreventIndulge(IColorActivityRecordEx record) {
        return false;
    }

    default void handleApplicationCrash(ProcessRecord record, ApplicationErrorReport.CrashInfo info) {
    }

    default Intent handleInterceptActivity(ActivityInfo aInfo, String callingPackage, int callingUid, int userId, Intent intent, String resolvedType, IColorActivityRecordEx resultRecord) {
        return null;
    }

    default boolean isFromControlCenterPkg(String callingPackage) {
        return false;
    }

    default boolean isTenIntencept(String callingPackage, Intent intent) {
        return false;
    }

    default List<String> getSougouSite() {
        return null;
    }

    default void uploadServiceConnected(ProcessRecord record) {
    }

    default void uploadRemoveServiceConnection(ProcessRecord record, long costTime) {
    }

    default void handleResumeActivity(IColorActivityRecordEx record) {
    }

    default void resetSpecServiceRestartTime(ServiceRecord r, long now, long minDuration) {
    }

    default boolean isCoreApp(ProcessRecord app) {
        return false;
    }

    default void sendOppoSiteMsg(String keyword) {
    }

    default void handOppoSiteMsg(Message msg) {
    }

    default boolean isAllowBackgroundRestrict(String callingPackage, Intent service) {
        return false;
    }

    default void monitorActivityStartInfo(String allowStartActivityType) {
    }

    default void cleanProviders(ProcessRecord app) {
    }

    default List adjustQueueOrderedBroadcastLocked(BroadcastQueue _queue, Intent _intent, ProcessRecord _callerApp, String _callerPackage, int _callingPid, int _callingUid, boolean _callerInstantApp, String _resolvedType, String[] _requiredPermissions, int _appOp, BroadcastOptions _options, List _receivers, IIntentReceiver _resultTo, int _resultCode, String _resultData, Bundle _resultExtras, boolean _serialized, boolean _sticky, boolean _initialSticky, int _userId, boolean _allowBackgroundActivityStarts, boolean _timeoutExempt) {
        return _receivers;
    }

    default void scheduleNextDispatch(Intent intent) {
    }

    default void handleAppStartForbidden(String packageName) {
    }

    default String getDialogTitleText() {
        return null;
    }

    default String getDialogContentText() {
        return null;
    }

    default String getDialogButtonText() {
        return null;
    }

    default void resetDialogShowText() {
    }

    default void setAppStartMonitorController(IOppoAppStartController controller) {
    }

    default void setPreventIndulgeController(IOppoAppStartController controller) {
    }

    default void addPreventIndulgeList(List<String> list) {
    }

    default void associateWhiteFileChanage(int userId) {
    }

    default void startupManagerFileChange() {
    }

    default void startupMonitorFileChange() {
    }

    default void collectWechatInfo(String name, String duration, String activiteTime) {
    }

    default void handleMonitorRestrictedBackgroundWhitelist(String callerPkg, List<String> list) {
    }
}
