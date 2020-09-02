package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import com.android.server.pm.IColorPackageManagerServiceEx;
import com.color.content.ColorRuleInfo;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public interface IColorIntentInterceptManager extends IOppoCommonFeature {
    public static final IColorIntentInterceptManager DEFAULT = new IColorIntentInterceptManager() {
        /* class com.android.server.wm.IColorIntentInterceptManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorIntentInterceptManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorIntentInterceptManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityTaskManagerServiceEx amsEx, IColorPackageManagerServiceEx pmsEx) {
    }

    default Intent interceptGPIfNeeded(String resolvedType, int callingUid, int userId, String callingPackage, ActivityRecord aRecord, Intent intent, ActivityInfo aInfo) {
        return null;
    }

    default void markActivityInfoIfNeeded(ActivityInfo aInfo) {
    }

    default boolean interceptSougouSiteIfNeeded(String callingPackage, ActivityStack stack, Intent intent) {
        return false;
    }

    default void dump(PrintWriter pw, String[] args) {
    }

    default boolean setInterceptRuleInfos(List<ColorRuleInfo> list) {
        return false;
    }

    default List<ColorRuleInfo> getInterceptRuleInfos() {
        return new ArrayList();
    }
}
