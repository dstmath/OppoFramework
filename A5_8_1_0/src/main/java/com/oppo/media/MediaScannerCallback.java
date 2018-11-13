package com.oppo.media;

public interface MediaScannerCallback {
    void processFileBegin(String str);

    void processFileEnd(String str);

    void scanMtpFile(String str, String str2, int i, int i2);

    void setLocale(String str);
}
