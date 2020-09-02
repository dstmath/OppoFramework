package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import java.util.List;

public interface IColorDefaultAppPolicyManager extends IOppoCommonFeature {
    public static final IColorDefaultAppPolicyManager DEFAULT = new IColorDefaultAppPolicyManager() {
        /* class com.android.server.pm.IColorDefaultAppPolicyManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorDefaultAppPolicyManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDefaultAppPolicyManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean isGotDefaultAppBeforeAddPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
        return false;
    }

    default ResolveInfo getForceAppBeforeChooseBestActivity(Intent intent, List<ResolveInfo> list) {
        return null;
    }

    default ResolveInfo getDefaultAppAfterChooseBestActivity(Intent intent, List<ResolveInfo> list, ResolveInfo ri) {
        return null;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default void systemReady() {
    }

    default void addBrowserToDefaultPackageList() {
    }

    default boolean isDefaultAppEnabled(Intent intent) {
        return false;
    }

    default boolean isQueryListContainsDefaultPkg(List<ResolveInfo> list) {
        return false;
    }

    default void addExpBrowserToDefaultPackageList(boolean firsBoot) {
    }

    default List<ResolveInfo> queryColorFilteredIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
        return null;
    }

    default boolean forbiddenSetPreferredActivity(IntentFilter filter) {
        return false;
    }
}
