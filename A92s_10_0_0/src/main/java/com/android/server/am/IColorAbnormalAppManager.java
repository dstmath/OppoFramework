package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.ActivityInfo;
import android.os.SystemProperties;
import android.util.ArraySet;
import java.util.List;

public interface IColorAbnormalAppManager extends IOppoCommonFeature {
    public static final boolean DEBUG_DETAIL = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final IColorAbnormalAppManager DEFAULT = new IColorAbnormalAppManager() {
        /* class com.android.server.am.IColorAbnormalAppManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorAbnormalAppManager";
    public static final String TAG = "ColorAbnormalAppManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAbnormalAppManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean getDynamicDebug() {
        return false;
    }

    default void handleStartAppInfo(ProcessRecord app, String startType) {
    }

    default boolean isPackageRestricted(String packageName) {
        return false;
    }

    default void setPackageUnRestricted(String packageName) {
    }

    default void updateScreenStatus(boolean isScreenOn) {
    }

    default void setAms(ActivityManagerService ams) {
    }

    default boolean inRestrictAppList(String packageName, int userId) {
        return false;
    }

    default boolean isScreenOffRestrictApp(String packageName, int userId) {
        return false;
    }

    default boolean isScreenOffRestrict() {
        return false;
    }

    default boolean isNotRestrictApp(String packageName) {
        return false;
    }

    default boolean validStartProvider(ContentProviderRecord cpr) {
        return false;
    }

    default boolean validStartActivity(ActivityInfo aInfo) {
        return false;
    }

    default boolean validRestartProcess(int uid, ArraySet<String> arraySet) {
        return false;
    }

    default boolean validStartService(String pkgName, int userId) {
        return false;
    }

    default boolean validStartBroadcast(String pkgName, int userId) {
        return false;
    }

    default void updateScreenOffRestrictedList(List<String> list, int userId) {
    }

    default void updateStartInfoWhiteList(List<String> list) {
    }

    default void updateNotRestrictedList(List<String> list) {
    }
}
