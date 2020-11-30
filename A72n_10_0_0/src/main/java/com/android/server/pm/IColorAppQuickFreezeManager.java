package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.os.Handler;
import java.io.PrintWriter;
import java.util.List;

public interface IColorAppQuickFreezeManager extends IOppoCommonFeature {
    public static final IColorAppQuickFreezeManager DEFAULT = new IColorAppQuickFreezeManager() {
        /* class com.android.server.pm.IColorAppQuickFreezeManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorAppQuickFreezeManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAppQuickFreezeManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default int oppoFreezePackage(String pkgName, int userId, int freezeFlag, int flag, String callingPkg) {
        return 0;
    }

    default int oppoUnFreezePackage(String pkgName, int userId, int freezeFlag, int flag, String callingPkg) {
        return 0;
    }

    default int getOppoFreezePackageState(String pkgName, int userId) {
        return 0;
    }

    default boolean inOppoFreezePackageList(String pkgName, int userId) {
        return false;
    }

    default List<String> getOppoFreezedPackageList(int userId) {
        return null;
    }

    default int getOppoPackageFreezeFlag(String pkgName, int userId) {
        return 0;
    }

    default void dumpOppoFreezeInfo(PrintWriter pw, String[] args) {
    }

    default void deleteOldFreezeInfo() {
    }

    default int oppoFreezePackageInternal(String pkgName, int userId, int freezeFlag, int flags, String callingPackage) {
        return 0;
    }

    default int oppoUnFreezePackageInternal(String pkgName, int userId, int freezeFlag, int flags, String callingPackage) {
        return 0;
    }

    default void autoUnfreezePackage(String pkgName, int userId, String reason) {
    }

    default int adjustFreezeAppFlags(int flags) {
        return flags;
    }

    default boolean adjustSendNow(boolean prevValue, int flags) {
        return prevValue;
    }

    default void setApplicationEnabledSetting(boolean update, int[] updateUserIds, String packageName) {
    }

    default boolean customizeSendEmptyMessage(String className, Handler mHandler) {
        return false;
    }
}
