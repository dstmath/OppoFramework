package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.IPackageDeleteObserver;
import android.graphics.Bitmap;
import java.util.Map;

public interface IColorIconCachesManager extends IOppoCommonFeature {
    public static final IColorIconCachesManager DEFAULT = new IColorIconCachesManager() {
        /* class com.android.server.pm.IColorIconCachesManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorIconCachesManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorIconCachesManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx pmsEx) {
    }

    default Bitmap getAppIconBitmap(String packageName) {
        return null;
    }

    default Map getAppIconsCache(boolean compress) {
        return null;
    }

    default Map getActivityIconsCache(IPackageDeleteObserver observer) {
        return null;
    }

    default void onPackageRemoved(String packageName) {
    }

    default void onPackageAdded(String packageName) {
    }

    default void cacheAppIconsData() {
    }

    default void cacheActivityIconsData(String packageName) {
    }

    default void systemReady() {
    }

    default void clearIconCache() {
    }
}
