package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface IColorHansManager extends IOppoCommonFeature {
    public static final IColorHansManager DEFAULT = new IColorHansManager() {
        /* class com.android.server.am.IColorHansManager.AnonymousClass1 */
    };
    public static final int FIREWALL_CHAIN_HANS = 4;
    public static final String FIREWALL_CHAIN_NAME_HANS = "hans";
    public static final String NAME = "ColorHansManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorHansManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx) {
    }

    default boolean hansActivityIfNeeded(int callingUid, String callingPackage, int uid, String pkgName, String cpnName) {
        return true;
    }

    default boolean hansTopActivityIfNeeded(int targetUid, String targetPkg) {
        return true;
    }

    default boolean hansServiceIfNeeded(int callingUid, String callingPackage, int uid, String pkgName, String cpnName, boolean isBind) {
        return true;
    }

    default boolean hansProviderIfNeeded(int callingUid, String callingPackage, int uid, String pkgName, String cpnName) {
        return true;
    }

    default boolean hansBroadcastIfNeeded(BroadcastRecord r, Object o) {
        return true;
    }

    default boolean hansSyncIfNeeded(int uid, String pkgName) {
        return true;
    }

    default boolean hansJobIfNeeded(int uid, String pkgName) {
        return true;
    }

    default boolean hansAlarmIfNeeded(String action, int uid, String pkgName) {
        return true;
    }

    default boolean hansMediaEventIfNeeded(int uid, String pkgName) {
        return true;
    }

    default boolean hansPackageTimeout(int uid, String pkgName) {
        return true;
    }

    default void hansBumpService(int uid, String pkgName) {
    }

    default void unfreezeForKernel(int type, int callerPid, int uid, String rpcName, int code) {
    }

    default void dumpHans(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    default void bootCompleted() {
    }

    default void unfreezeForWatchdog() {
    }

    default void dumpHansHistory(FileDescriptor fd, PrintWriter pw) {
    }

    default void hansUpdateForegroundServiceState(int uid, String pkgName, boolean isForeground) {
    }
}
