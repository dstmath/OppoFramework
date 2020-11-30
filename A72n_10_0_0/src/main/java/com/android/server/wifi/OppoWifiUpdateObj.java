package com.android.server.wifi;

import com.android.server.wifi.OppoWifiOCloudImpl;

public class OppoWifiUpdateObj {
    private static final String TAG = "OppoWifiUpdateObj";
    public String effectMethod = "general";
    public String fileName = OppoWifiSauConfig.PLATFORM_NONE;
    public String fileType = OppoWifiSauConfig.PLATFORM_NONE;
    public String md5 = OppoWifiSauConfig.PLATFORM_NONE;
    public String platform = OppoWifiSauConfig.PLATFORM_NONE;
    public String pushReason = OppoWifiOCloudImpl.SimpleWifiConfig.UPDATE;
    public String pushTime = "20200220";
}
