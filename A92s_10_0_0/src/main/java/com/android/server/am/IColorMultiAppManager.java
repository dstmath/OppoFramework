package com.android.server.am;

import android.app.ContentProviderHolder;
import android.app.IApplicationThread;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageUserState;
import android.content.pm.ParceledListSlice;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.UserHandle;
import com.android.server.pm.IColorPackageManagerServiceEx;
import com.android.server.pm.PackageSetting;
import com.android.server.uri.GrantUri;
import com.android.server.wm.ActivityStackSupervisor;
import com.android.server.wm.IColorActivityRecordEx;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface IColorMultiAppManager extends IOppoCommonFeature {
    public static final IColorMultiAppManager DEFAULT = new IColorMultiAppManager() {
        /* class com.android.server.am.IColorMultiAppManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorMultiAppManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorMultiAppManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx, IColorPackageManagerServiceEx pmsEx) {
    }

    default boolean isSupportMultiApp() {
        return false;
    }

    default void createUser(UserInfo info) {
    }

    default void removeUser(UserInfo info) {
    }

    default boolean isCurrentProfile(int userId) {
        return false;
    }

    default void addToCreatedMultiApp(String pkgName) {
    }

    default void removeFromCreatedMultiApp(String pkgName) {
    }

    default List<String> getAllowedMultiApp() {
        return Collections.emptyList();
    }

    default List<String> getCreatedMultiApp() {
        return Collections.emptyList();
    }

    default String getAliasMultiApp(String pkgName) {
        return null;
    }

    default void startUnlockMultiUser(int userId) {
    }

    default boolean enforceCrossUserPermission(int callingUid, int userId) {
        return false;
    }

    default boolean isMultiAllowedApp(String pkgName) {
        return false;
    }

    default boolean isMultiApp(String pkgName) {
        return false;
    }

    default boolean isMultiApp(int userId, String pkgName) {
        return false;
    }

    default boolean isMultiAppUserId(int userId) {
        return false;
    }

    default int[] getMultiUserGids(ProcessRecord processRecord, int[] gids) {
        return gids;
    }

    default boolean shouldUseLastTargetUid(int callingUid, int lastTargetUid, GrantUri grantUri, IPackageManager pm) {
        return false;
    }

    default boolean shouldUseLastTargetUid(int callingUid, int lastTargetUid, String authority) {
        return false;
    }

    default boolean shouldUseLastTargetUid(int callingUid, String targetPkg) {
        return false;
    }

    default ContentProviderRecord getCorrectCpr(ContentProviderRecord cpr, ProviderMap map, String name, int userId) {
        return null;
    }

    default ProviderInfo getCorrectCpi(ProviderInfo cpi, String name, int userId) {
        return null;
    }

    default boolean shouldStopBroadcast(int cmd, int userId) {
        return false;
    }

    default boolean shouldChangeHolder(ContentProviderHolder holder, int userId, int callingUid) {
        return false;
    }

    default boolean shouldFilterPackageInfo(int flags, int userId, String packageName) {
        return false;
    }

    default boolean shouldSkipLeaveUser(Intent resultData, IColorActivityRecordEx record) {
        return false;
    }

    default int getCorrectUserId(int userId) {
        return userId;
    }

    default int getCorrectUserId(int userId, Intent service) {
        return userId;
    }

    default int getCorrectUserId(int userId, String packageName) {
        return userId;
    }

    default int getCorrectUserId(int userId, String packageName, boolean created) {
        return userId;
    }

    default int getCorrectUserId(int flags, int userId, String packageName) {
        return userId;
    }

    default int correctUserId(int userId, ActivityInfo aInfo) {
        return userId;
    }

    default int getCorrectUserIdByFlags(int userId, int flags) {
        return userId;
    }

    default int getCorrectUid(int uid) {
        return uid;
    }

    default int getCorrectUid2(int uid) {
        return uid;
    }

    default int getCorrectUid(int uid, String packageName) {
        return uid;
    }

    default UserInfo getUserInfo(int userId) {
        return null;
    }

    default UserInfo getUserInfoByFlags(int flags) {
        return null;
    }

    default UserHandle getCorrectUserHandle(UserHandle user, int flags, boolean isCtsAppInstall) {
        return user;
    }

    default PackageUserState getPackageUserState(int userId, PackageSetting ps) {
        return null;
    }

    default int getDeleteFlags(int deleteFlags, int userId, String packageName) {
        return deleteFlags;
    }

    default ContentProviderHolder fixGetContentProvider(ContentProviderHolder holder, IApplicationThread caller, String name, IBinder token, int callingUid, String callingPackage, String callingTag, boolean stable, int userId) {
        return holder;
    }

    default void handleMultiAppPackageRemove(Intent intent, String action) {
    }

    default boolean shouldSkipReceiver(int userId, int callingUserId, String callerPackage) {
        return false;
    }

    default boolean shouldAccessProfile(int callingUserId, int targetUserId) {
        return false;
    }

    default int filterReceiver(int userId, int callingUserId, String packageName, List<ResolveInfo> list, int index) {
        return index;
    }

    default void filterReceivers(int userId, int callingUserId, List<ResolveInfo> list) {
    }

    default int[] changeUsers(int callingUid, int[] users, String callerPackage) {
        return users;
    }

    default int handleBroadCastIntent(Intent intent, int callintUid, String callerPackage, int userId) {
        return userId;
    }

    default boolean shouldFilterResult(IColorActivityRecordEx r, Intent resultData) {
        return false;
    }

    default int checkCategory(int userId, Intent intent) {
        return userId;
    }

    default int checkSpecialApp(int userId, String callingPackage, boolean componentSpecified, Intent intent, ActivityStackSupervisor mSupervisor) {
        return userId;
    }

    default int checkIntent(int userId, String callingPackage, boolean componentSpecified, Intent intent) {
        return userId;
    }

    default void addRecentTaskUsers(int aimUserId, Set<Integer> set) {
    }

    default boolean shouldFilterTask(boolean userSetupComplete, int userId) {
        return false;
    }

    default boolean isCustomDataApp(String packageName) {
        return false;
    }

    default boolean shouldInstall(boolean shouldInstall, int userHandle, String packageName) {
        return shouldInstall;
    }

    default boolean shouldChangeUserHandle(int uid, String msg) {
        return false;
    }

    default void onExternalStoragePolicyChanged(String packageName, PackageSetting ps, int uid) {
    }

    default void onExternalStoragePolicyChanged(int userId, String packageName, int uid) {
    }

    default boolean shouldSkipNotification(int userId, String packageName) {
        return false;
    }

    default boolean shouldSkipPermissionCheck() {
        return false;
    }

    default boolean shouldSkipResetPermission(int userId, String packageName) {
        return false;
    }

    default boolean startActivity(ActivityInfo aInfo, Intent intent, IColorActivityRecordEx sourceRecord, IColorActivityRecordEx resultRecord, String callingPackage, int callingUid) {
        return false;
    }

    default boolean startActivity(ActivityInfo aInfo, Intent intent, Bundle bundle, IColorActivityRecordEx sourceRecord, IColorActivityRecordEx resultRecord, String callingPackage, int callingUid) {
        return false;
    }

    default void handleChooseActivityMsg(Message msg) {
    }

    default void intentFixUris(int mCallingUid, Intent mIntent, IColorActivityRecordEx sourceRecord) {
    }

    default void setOppoUserId(ActivityInfo aInfo, Intent intent, int userId) {
    }

    default void handleInstallApp(int userId, String packageName) {
    }

    default void handleUninstallPkg(int userId, String packageName) {
    }

    default ServiceInfo fixServiceInfo(ServiceInfo info, int userId, int callingUid, PackageParser.Service s, int flags, PackageSetting ps) {
        return info;
    }

    default int fixIntentAndUserId(int userId, Intent intent, String pkg) {
        return userId;
    }

    default ActivityInfo fixActivityInfo(ActivityInfo ai, int userId, PackageParser.Activity activity, int mFlags, PackageSetting ps) {
        return ai;
    }

    default ApplicationInfo fixApplicationInfo(ApplicationInfo ai, String packageName, int flags) {
        return ai;
    }

    default ResolveInfo fixResolveInfo(ResolveInfo info, int userId, Intent intent, String resolvedType, int flags, boolean resolveForStart, int filterCallingUid) {
        return info;
    }

    default ParceledListSlice<ResolveInfo> fixIntentReceivers(int uid, ParceledListSlice<ResolveInfo> parceledList, Intent intent, String resolvedType, int flags, int userId, boolean allowDynamicSplits) {
        return parceledList;
    }

    default ParceledListSlice<ResolveInfo> fixIntentServices(ParceledListSlice<ResolveInfo> parceledList, int userId, Intent intent, String resolvedType, int flags, int callingUid, boolean includeInstantApps) {
        return parceledList;
    }

    default ParceledListSlice<ResolveInfo> fixIntentContentProviders(int uid, int userId, ParceledListSlice<ResolveInfo> parceledList, Intent intent, String resolvedType, int flags) {
        return parceledList;
    }

    default boolean shouldFilterPackage(String callingPkg, String getPkg, int userId) {
        return false;
    }

    default int getLaunchedFromUid(String packageName, int fromUid) {
        return fromUid;
    }

    default boolean isMultiUserInstallApp(String pkgName) {
        return false;
    }

    default void updateMultiUserInstallAppState(String name, boolean mainInstalled, boolean multiInstalled) {
    }

    default void checkMultiUserInstallApp() {
    }

    default void syncPermissionsAfterOta() {
    }

    default int[] noticeMultiApp(String action, int userId, int[] users) {
        return null;
    }
}
