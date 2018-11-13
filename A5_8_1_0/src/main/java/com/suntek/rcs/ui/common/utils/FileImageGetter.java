package com.suntek.rcs.ui.common.utils;

import android.graphics.BitmapFactory;

public class FileImageGetter extends ImageGetter {
    public FileImageGetter(ImageTask imageTask, ImageLoaderListener listener) {
        super(imageTask, listener);
    }

    public void loadImage(String path) {
        this.imageTask.setLoading(true);
        this.listener.onLoaded(path, BitmapFactory.decodeFile(path), this.imageTask.getImageView());
        this.imageTask.setLoading(false);
    }
}
