package com.color.favorite;

import android.app.Activity;
import android.common.OppoFeatureList;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.View;
import java.util.List;

public interface IColorFavoriteManager extends IColorBaseFavoriteManager {
    public static final boolean DBG;
    public static final IColorFavoriteManager DEFAULT = new IColorFavoriteManager() {
        /* class com.color.favorite.IColorFavoriteManager.AnonymousClass1 */
    };
    public static final boolean LOG_DEBUG = SystemProperties.getBoolean("log.favorite.debug", false);
    public static final boolean LOG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);

    static {
        boolean z = false;
        if (LOG_PANIC || LOG_DEBUG) {
            z = true;
        }
        DBG = z;
    }

    @Override // android.common.IOppoCommonFeature
    default IColorFavoriteManager getDefault() {
        return DEFAULT;
    }

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorFavoriteManager;
    }

    @Override // com.color.favorite.IColorBaseFavoriteManager
    default void init(Context context) {
    }

    @Override // com.color.favorite.IColorBaseFavoriteManager
    default void release() {
    }

    @Override // com.color.favorite.IColorBaseFavoriteManager
    default void processClick(View clickView, ColorFavoriteCallback callback) {
    }

    @Override // com.color.favorite.IColorBaseFavoriteManager
    default void processCrawl(View rootView, ColorFavoriteCallback callback) {
    }

    @Override // com.color.favorite.IColorBaseFavoriteManager
    default void processSave(View rootView, ColorFavoriteCallback callback) {
    }

    @Override // com.color.favorite.IColorBaseFavoriteManager
    default void logActivityInfo(Activity activity) {
    }

    @Override // com.color.favorite.IColorBaseFavoriteManager
    default void logViewInfo(View view) {
    }

    @Override // com.color.favorite.IColorBaseFavoriteManager
    default Handler getWorkHandler() {
        return null;
    }

    @Override // com.color.favorite.IColorBaseFavoriteManager
    default void setEngine(ColorFavoriteEngines engine) {
    }

    @Override // com.color.favorite.IColorBaseFavoriteManager
    default boolean isSaved(Context context, String packageName, List<Bundle> list) {
        return false;
    }
}
