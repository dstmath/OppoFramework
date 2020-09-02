package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import com.color.util.ColorProcDependData;
import java.util.List;

public interface IColorAthenaAmManager extends IOppoCommonFeature {
    public static final IColorAthenaAmManager DEFAULT = new IColorAthenaAmManager() {
        /* class com.android.server.am.IColorAthenaAmManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorAthenaAmManager";
    public static final int TYPE_KILL = 3;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_NOT_KILL_PKG = 1;
    public static final int TYPE_NOT_KILL_PROC = 2;

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAthenaAmManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx) {
    }

    default List<ColorProcDependData> getProcDependency(int pid) {
        return null;
    }

    default List<ColorProcDependData> getProcDependency(String pkgName, int userId) {
        return null;
    }

    default void forceTrimAppMemory(int level) {
    }
}
