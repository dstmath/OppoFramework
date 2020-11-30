package com.android.server.am;

import android.app.ContentProviderHolder;
import android.app.IApplicationThread;
import android.os.IBinder;
import android.os.IProgressListener;

interface IColorActivityManagerServiceInner {
    default ContentProviderHolder getContentProviderImpl(IApplicationThread caller, String name, IBinder token, int callingUid, String callingPackage, String callingTag, boolean stable, int userId) {
        return null;
    }

    default int getCurrentUserIdLU() {
        return -1;
    }

    default void removeUriPermissionsForPackage(String packageName, int userHandle, boolean persistable, boolean targetOnly) {
    }

    default void removeRecentTasksByPackageName(String packageName, int userId) {
    }

    default void cleanupDisabledPackageComponentsLocked(String packageName, int userId, String[] changedClasses) {
    }

    default void killPackageProcessesLocked(String packageName, int appId, int userId, int minOomAdj, String reason) {
    }

    default boolean forceStopPackageLocked(String packageName, int appId, boolean callerWillRestart, boolean purgeCache, boolean doit, boolean evenPersistent, boolean uninstalling, int userId, String reason) {
        return false;
    }

    default void forceStopPackageLocked(String packageName, int userId) {
    }

    default boolean startUser(int userId, boolean foreground, IProgressListener unlockListener) {
        return false;
    }

    default void removeDyingProviderLocked(ProcessRecord proc, ContentProviderRecord cpr, boolean always) {
    }

    default ProcessRecord getTopAppLockedForBroadcast() {
        return null;
    }

    default void dynamicalConfigLog(String categoryTypeName, IApplicationThread thread, boolean on) {
    }

    default boolean isInRestartingServicesList(String pkgName, int uid) {
        return false;
    }
}
