package com.mediatek.net.http;

import android.net.http.HttpResponseCache;
import android.os.Build;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import com.mediatek.net.connectivity.IMtkIpConnectivityMetrics;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class HttpCacheExt {
    private static final HttpCacheExt INSTANCE = new HttpCacheExt();
    private static final boolean VDBG;
    private static HttpResponseCache sCache;
    private static IMtkIpConnectivityMetrics sIpConnectivityMetrics;

    static {
        boolean z = false;
        if (SystemProperties.getInt("persist.vendor.log.tel_dbg", 0) == 1) {
            z = true;
        }
        VDBG = z;
    }

    private static void log(String info) {
        if (VDBG) {
            System.out.println(info);
        }
    }

    private static void loge(String info) {
        if (!Build.IS_USER) {
            System.out.println(info);
        }
    }

    public static void checkUrl(URL httpUrl) {
        if (httpUrl != null) {
            log("checkUrl: " + httpUrl);
            if (INSTANCE.isSecurityUrl(httpUrl.toString())) {
                INSTANCE.doAction();
            }
        }
    }

    private boolean isSecurityUrl(String httpUrl) {
        if (!httpUrl.endsWith(".png") || !httpUrl.contains("hongbao")) {
            return false;
        }
        return true;
    }

    private void doAction() {
        try {
            if (sCache == null) {
                log("Init cache");
                File cacheDir = new File(System.getProperty("java.io.tmpdir"), "HttpCache");
                log("Init cache:" + cacheDir);
                sCache = HttpResponseCache.install(cacheDir, 2147483647L);
            }
        } catch (IOException ioe) {
            loge("do1:" + ioe);
        }
        if (isInteractive()) {
            speedDownload();
        }
    }

    private boolean isInteractive() {
        try {
            return IPowerManager.Stub.asInterface(ServiceManager.getService("power")).isInteractive();
        } catch (Exception e) {
            loge("isInteractive:" + e);
            return false;
        }
    }

    private void speedDownload() {
        try {
            if (sIpConnectivityMetrics == null) {
                sIpConnectivityMetrics = IMtkIpConnectivityMetrics.Stub.asInterface(ServiceManager.getService("mtkconnmetrics"));
            }
            if (sIpConnectivityMetrics != null) {
                log("setSpeedDownload");
                sIpConnectivityMetrics.setSpeedDownload(15000);
            }
        } catch (Exception e) {
            loge("do2:" + e);
        }
    }
}
