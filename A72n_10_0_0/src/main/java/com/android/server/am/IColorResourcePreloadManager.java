package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public interface IColorResourcePreloadManager extends IOppoCommonFeature {
    public static final IColorResourcePreloadManager DEFAULT = new IColorResourcePreloadManager() {
        /* class com.android.server.am.IColorResourcePreloadManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorResourcePreloadManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorResourcePreloadManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx) {
    }

    default void bootCompleted() {
    }

    default void launchEmptyProcess(ActivityInfo aInfo, Intent intent) {
    }

    default void preloadPkgsForAI(ArrayList<String> arrayList) {
    }

    default boolean isPkgPreload(String pkgName, int userId) {
        return false;
    }

    default void handleProcessStart(ProcessRecord app) {
    }

    default void handleProcessKilled(ProcessRecord app, int pid, String reason) {
    }

    default void handleProcessDied(ProcessRecord app) {
    }

    default void handleActivityStart(String pkgName, String processName, int uid) {
    }

    default void notifyLaunchTime(ApplicationInfo appInfo, String shortComponentName, long launchTime) {
    }

    default void notifyBindApplicationFinished(String pkgName, int userId, int pid) {
    }

    default boolean preloadServiceBlock(int callerPid, int callerUid, String callerPackage, int calleeUid, String calleePackage, String calleeProcessName, String cpnName, boolean isBind) {
        return false;
    }

    default boolean preloadProviderBlock(int callerPid, int callerUid, String callerPackage, int calleeUid, String calleePackage, String calleeProcessName, String cpnName) {
        return false;
    }

    default boolean preloadBroadcastBlock(BroadcastRecord r, Object info) {
        return false;
    }

    default boolean preloadSyncBlock(int uid, String pkgName) {
        return false;
    }

    default boolean preloadJobBlock(int uid, String pkgName) {
        return false;
    }

    default boolean preloadAlarmBlock(String action, int uid, String pkgName) {
        return false;
    }

    default void dumpPreload(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    default boolean preloadAllowServiceStart(String pkgName, int uid) {
        return false;
    }
}
