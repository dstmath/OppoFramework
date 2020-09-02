package com.color.favorite;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import java.util.List;

public class ColorDummyFavoriteManager implements IColorFavoriteManager {
    private static final boolean DEBUG = true;
    private static final String TAG = "ColorDummyFavoriteManager";
    private static volatile ColorDummyFavoriteManager sInstance = null;

    public static ColorDummyFavoriteManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorDummyFavoriteManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorDummyFavoriteManager();
                }
            }
        }
        return sInstance;
    }

    @Override // com.color.favorite.IColorFavoriteManager, com.color.favorite.IColorBaseFavoriteManager
    public void init(Context context) {
    }

    @Override // com.color.favorite.IColorFavoriteManager, com.color.favorite.IColorBaseFavoriteManager
    public void release() {
    }

    @Override // com.color.favorite.IColorFavoriteManager, com.color.favorite.IColorBaseFavoriteManager
    public void processClick(View clickView, ColorFavoriteCallback callback) {
    }

    @Override // com.color.favorite.IColorFavoriteManager, com.color.favorite.IColorBaseFavoriteManager
    public void processCrawl(View rootView, ColorFavoriteCallback callback) {
    }

    @Override // com.color.favorite.IColorFavoriteManager, com.color.favorite.IColorBaseFavoriteManager
    public void processSave(View rootView, ColorFavoriteCallback callback) {
    }

    @Override // com.color.favorite.IColorFavoriteManager, com.color.favorite.IColorBaseFavoriteManager
    public void logActivityInfo(Activity activity) {
    }

    @Override // com.color.favorite.IColorFavoriteManager, com.color.favorite.IColorBaseFavoriteManager
    public void logViewInfo(View view) {
    }

    @Override // com.color.favorite.IColorFavoriteManager, com.color.favorite.IColorBaseFavoriteManager
    public Handler getWorkHandler() {
        return null;
    }

    @Override // com.color.favorite.IColorFavoriteManager, com.color.favorite.IColorBaseFavoriteManager
    public void setEngine(ColorFavoriteEngines engine) {
    }

    @Override // com.color.favorite.IColorFavoriteManager, com.color.favorite.IColorBaseFavoriteManager
    public boolean isSaved(Context context, String packageName, List<Bundle> list) {
        return false;
    }
}
