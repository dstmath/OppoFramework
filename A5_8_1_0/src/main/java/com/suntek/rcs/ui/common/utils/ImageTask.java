package com.suntek.rcs.ui.common.utils;

import android.widget.ImageView;

public class ImageTask {
    private boolean canceled;
    private int emptyImgID;
    private int errorImgID;
    private ImageView imageView;
    private boolean loading;
    private String url;

    public ImageTask(String url, ImageView imageView) {
        this(url, imageView, -1, -1);
    }

    public ImageTask(String url, ImageView imageView, int emptyImgID, int errorImgID) {
        this.emptyImgID = -1;
        this.errorImgID = -1;
        this.url = url;
        this.imageView = imageView;
        this.emptyImgID = emptyImgID;
        this.errorImgID = errorImgID;
        this.canceled = false;
        this.loading = false;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ImageView getImageView() {
        return this.imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public int getEmptyImgID() {
        return this.emptyImgID;
    }

    public void setEmptyImgID(int emptyImgID) {
        this.emptyImgID = emptyImgID;
    }

    public int getErrorImgID() {
        return this.errorImgID;
    }

    public void setErrorImgID(int errorImgID) {
        this.errorImgID = errorImgID;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isLoading() {
        return this.loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }
}
