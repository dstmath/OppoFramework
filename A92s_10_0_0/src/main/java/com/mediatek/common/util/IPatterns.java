package com.mediatek.common.util;

public interface IPatterns {
    UrlData getWebUrl(String str, int i, int i2);

    public static class UrlData {
        public int end;
        public int start;
        public String urlStr;

        public UrlData(String url, int s, int e) {
            this.urlStr = url;
            this.start = s;
            this.end = e;
        }
    }
}
