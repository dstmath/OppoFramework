package com.suntek.rcs.ui.common.utils;

public abstract class ImageGetter implements Runnable {
    protected ImageTask imageTask;
    protected ImageLoaderListener listener;

    public abstract void loadImage(String str);

    public ImageGetter(ImageTask imageTask, ImageLoaderListener listener) {
        this.imageTask = imageTask;
        this.listener = listener;
    }

    public void run() {
        if (this.listener.onStartLoad()) {
            loadImage(this.imageTask.getUrl());
        }
        this.listener.onEndLoad();
    }
}
