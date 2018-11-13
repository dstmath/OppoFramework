package com.android.server.wifi.util;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.hotspot2.NetworkDetail;

public class ScanResultUtil {
    private ScanResultUtil() {
    }

    public static ScanDetail toScanDetail(ScanResult scanResult) {
        return new ScanDetail(scanResult, new NetworkDetail(scanResult.BSSID, scanResult.informationElements, scanResult.anqpLines, scanResult.frequency));
    }

    public static boolean isScanResultForPskNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("PSK");
    }

    public static boolean isScanResultForWapiPskNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("WAPI-KEY");
    }

    public static boolean isScanResultForWapiCertNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("WAPI-CERT");
    }

    public static boolean isScanResultForEapNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("EAP");
    }

    public static boolean isScanResultForWepNetwork(ScanResult scanResult) {
        return scanResult.capabilities.contains("WEP");
    }

    public static boolean isScanResultForFilsSha256Network(ScanResult scanResult) {
        return scanResult.capabilities.contains("FILS-SHA256");
    }

    public static boolean isScanResultForFilsSha384Network(ScanResult scanResult) {
        return scanResult.capabilities.contains("FILS-SHA384");
    }

    public static boolean isScanResultForOpenNetwork(ScanResult scanResult) {
        int i;
        if (isScanResultForWepNetwork(scanResult) || isScanResultForPskNetwork(scanResult) || isScanResultForWapiPskNetwork(scanResult) || isScanResultForWapiCertNetwork(scanResult)) {
            i = 1;
        } else {
            i = isScanResultForEapNetwork(scanResult);
        }
        return i ^ 1;
    }

    public static String createQuotedSSID(String ssid) {
        return "\"" + ssid + "\"";
    }

    public static WifiConfiguration createNetworkFromScanResult(ScanResult scanResult) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = createQuotedSSID(scanResult.SSID);
        setAllowedKeyManagementFromScanResult(scanResult, config);
        return config;
    }

    public static void setAllowedKeyManagementFromScanResult(ScanResult scanResult, WifiConfiguration config) {
        if (isScanResultForPskNetwork(scanResult)) {
            config.allowedKeyManagement.set(1);
        } else if (isScanResultForWapiPskNetwork(scanResult)) {
            config.allowedKeyManagement.set(190);
        } else if (isScanResultForWapiCertNetwork(scanResult)) {
            config.allowedKeyManagement.set(191);
        } else if (isScanResultForEapNetwork(scanResult)) {
            config.allowedKeyManagement.set(2);
            config.allowedKeyManagement.set(3);
            if (isScanResultForFilsSha256Network(scanResult)) {
                config.allowedKeyManagement.set(8);
            }
            if (isScanResultForFilsSha384Network(scanResult)) {
                config.allowedKeyManagement.set(9);
            }
            config.allowedKeyManagement.set(8);
            config.allowedKeyManagement.set(9);
        } else if (isScanResultForWepNetwork(scanResult)) {
            config.allowedKeyManagement.set(0);
            config.allowedAuthAlgorithms.set(0);
            config.allowedAuthAlgorithms.set(1);
        } else {
            config.allowedKeyManagement.set(0);
        }
    }
}
