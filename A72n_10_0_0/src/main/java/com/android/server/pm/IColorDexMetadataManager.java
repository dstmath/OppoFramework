package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.PackageParser;

public interface IColorDexMetadataManager extends IOppoCommonFeature {
    public static final IColorDexMetadataManager DEFAULT = new IColorDexMetadataManager() {
        /* class com.android.server.pm.IColorDexMetadataManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorDexMetadataManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDexMetadataManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default void onStart(IPackageManager pm, Installer installer, Object installLock) {
    }

    default void onSystemReady(Context context) {
    }

    default boolean dumpProfile(PackageParser.Package pkg) {
        return false;
    }

    default boolean createProfile(PackageParser.Package pkg) {
        return false;
    }

    default boolean mergeProfile(PackageParser.Package pkg) {
        return false;
    }

    default boolean isPrecompileEnable() {
        return false;
    }

    default void registerToCompile(String packageName) {
    }

    default boolean isNeedToDexOptForce(String packageName) {
        return false;
    }

    default void recordCompiledApp(String packageName) {
    }

    default void removeCompiledApp(String packageName) {
    }

    default boolean switchOfUploadDcsForDexMetadata() {
        return false;
    }
}
