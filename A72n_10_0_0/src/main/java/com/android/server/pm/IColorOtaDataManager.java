package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.util.ArraySet;
import java.io.PrintWriter;

public interface IColorOtaDataManager extends IOppoCommonFeature {
    public static final IColorOtaDataManager DEFAULT = new IColorOtaDataManager() {
        /* class com.android.server.pm.IColorOtaDataManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorOtaDataManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorOtaDataManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default void initAppList() {
    }

    default void update() {
    }

    default void systemReady() {
    }

    default void setDataPackageNameList(ArraySet<String> arraySet) {
    }

    default void addManualPackageOperationState(PackageSetting ps, String packageName, boolean install) {
    }

    default void dump(PrintWriter pw, String[] args) {
    }
}
