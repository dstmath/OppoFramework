package com.color.favorite;

public class ColorFavoriteData {
    public static final String DATA_TITLE = "data_title";
    public static final String DATA_URL = "data_url";
    private String mTitle = null;
    private String mUrl = null;

    public String toString() {
        return "FavoriteData{mTitle:" + this.mTitle + ", mUrl=" + this.mUrl + "}";
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getUrl() {
        return this.mUrl;
    }
}
