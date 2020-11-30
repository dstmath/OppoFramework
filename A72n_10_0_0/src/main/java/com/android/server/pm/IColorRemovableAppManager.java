package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.pm.PackageParser;
import android.util.ArraySet;
import com.color.content.ColorRemovableAppInfo;
import java.io.PrintWriter;
import java.util.List;

public interface IColorRemovableAppManager extends IOppoCommonFeature {
    public static final IColorRemovableAppManager DEFAULT = new IColorRemovableAppManager() {
        /* class com.android.server.pm.IColorRemovableAppManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorRemovableAppManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorRemovableAppManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx ex) {
    }

    default void systemReady(Context context) {
    }

    default boolean shouldInterceptInScanStage(PackageParser.Package pkg) {
        return false;
    }

    default boolean install(String packageName) {
        return false;
    }

    default boolean uninstall(String packageName) {
        return false;
    }

    default boolean isRemovable(String packageName) {
        return false;
    }

    default boolean isUninstalled(String packageName) {
        return false;
    }

    default long getVersionCode(String packageName) {
        return -1;
    }

    default String getCodePath(String packageName) {
        return null;
    }

    default List<String> getRemovableAppList() {
        return null;
    }

    default List<ColorRemovableAppInfo> getRemovedAppInfos() {
        return null;
    }

    default List<ColorRemovableAppInfo> getRemovableAppInfos() {
        return null;
    }

    default ColorRemovableAppInfo getRemovableAppInfo(String packageName) {
        return null;
    }

    default void updateConfigurations() {
    }

    default void setDataPackageNameList(ArraySet<String> arraySet) {
    }

    default void scanPreinstalledApps() {
    }

    default void changePackageInstalledState(PackageSetting ps, String packageName, boolean install) {
    }

    default void dump(PrintWriter pw, String[] args) {
    }
}
