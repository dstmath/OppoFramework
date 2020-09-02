package com.color.favorite;

import java.util.ArrayList;

public class ColorFavoriteResult {
    public static final String ERROR_NOT_FOUND = "Not found";
    public static final String ERROR_NOT_INIT = "Not init";
    public static final String ERROR_NO_VIEW = "No view";
    public static final String ERROR_SAVE_FAILED = "Save failed";
    public static final String ERROR_SETTING_OFF = "Setting off";
    public static final String ERROR_UNSUPPORT = "Unsupported";
    private final ArrayList<ColorFavoriteData> mData = new ArrayList<>();
    private String mError = null;
    private String mPackageName = null;

    public void setData(ArrayList<ColorFavoriteData> data) {
        synchronized (this.mData) {
            this.mData.clear();
            if (data != null) {
                synchronized (data) {
                    this.mData.addAll(data);
                }
            }
        }
    }

    public void setError(String error) {
        this.mError = error;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public ArrayList<ColorFavoriteData> getData() {
        ArrayList<ColorFavoriteData> arrayList;
        synchronized (this.mData) {
            arrayList = this.mData;
        }
        return arrayList;
    }

    public String getError() {
        return this.mError;
    }

    public String getPackageName() {
        return this.mPackageName;
    }
}
