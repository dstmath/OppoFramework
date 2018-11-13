package com.suntek.rcs.ui.common.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface ImageLoaderListener {
    void onEndLoad();

    void onLoaded(String str, Bitmap bitmap, ImageView imageView);

    boolean onStartLoad();
}
