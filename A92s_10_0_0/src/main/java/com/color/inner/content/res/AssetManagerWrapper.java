package com.color.inner.content.res;

import android.content.res.AssetManager;
import android.util.Log;

public class AssetManagerWrapper {
    private static final String TAG = "AssetManagerWrapper";

    public static int addAssetPath(AssetManager asset, String path) {
        try {
            return asset.addAssetPath(path);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static AssetManager createAssetManager() {
        try {
            return new AssetManager();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
