package com.android.server.wifi.p2p;

import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.OppoSoftapP2pBandControl;
import java.util.HashMap;

public class OppoWifiP2pConnectionHelper {
    private static final String TAG = OppoWifiP2pConnectionHelper.class.getSimpleName();
    private static boolean isNfcTriggered = false;
    private static boolean mVerboseLoggingEnabled = false;

    public static String getSsidPostfix(String deviceName) {
        int utfCount = 0;
        int strLen = 0;
        byte[] bChar = deviceName.getBytes();
        if (TextUtils.isEmpty(deviceName) || bChar.length <= 22) {
            return deviceName;
        }
        int i = 0;
        while (true) {
            if (i > deviceName.length()) {
                break;
            }
            byte b0 = bChar[utfCount];
            if (mVerboseLoggingEnabled) {
                String str = TAG;
                Log.d(str, "b0=" + ((int) b0) + ", i=" + i + ", utfCount=" + utfCount);
            }
            if ((b0 & 128) == 0) {
                utfCount++;
            } else if (b0 >= -4 && b0 <= -3) {
                utfCount += 6;
            } else if (b0 >= -8) {
                utfCount += 5;
            } else if (b0 >= -16) {
                utfCount += 4;
            } else if (b0 >= -32) {
                utfCount += 3;
            } else if (b0 >= -64) {
                utfCount += 2;
            }
            if (utfCount > 22) {
                strLen = i;
                if (mVerboseLoggingEnabled) {
                    String str2 = TAG;
                    Log.d(str2, "break: utfCount=" + utfCount + ", strLen=" + strLen);
                }
            } else {
                i++;
            }
        }
        return deviceName.substring(0, strLen);
    }

    public WifiP2pGroup addPersistentGroup(HashMap<String, String> variables, WifiP2pNative wifip2pNative, WifiP2pGroupList wifiP2pGroupList) {
        int netId = wifip2pNative.addNetwork();
        if (mVerboseLoggingEnabled) {
            String str = TAG;
            Log.d(str, "addPersistentGroup netId=" + netId);
        }
        for (String key : variables.keySet()) {
            if (mVerboseLoggingEnabled) {
                String str2 = TAG;
                Log.d(str2, "addPersistentGroup variable=" + key + " : " + variables.get(key));
            }
            wifip2pNative.setNetworkVariable(netId, key, variables.get(key));
        }
        for (WifiP2pGroup group : wifiP2pGroupList.getGroupList()) {
            if (netId == group.getNetworkId()) {
                return group;
            }
        }
        if (!mVerboseLoggingEnabled) {
            return null;
        }
        Log.d(TAG, "addPersistentGroup failed.");
        return null;
    }

    public boolean setP2pBandList(int p2pbandtype, WifiP2pNative wifip2pNative) {
        if (mVerboseLoggingEnabled) {
            String str = TAG;
            Log.d(str, "setP2pBandList " + p2pbandtype);
        }
        if (p2pbandtype == 1) {
            return wifip2pNative.setP2pBandLIst(OppoSoftapP2pBandControl.BandType.BAND_2G_ONLY);
        }
        if (p2pbandtype == 2) {
            return wifip2pNative.setP2pBandLIst(OppoSoftapP2pBandControl.BandType.BAND_5G_ONLY);
        }
        if (p2pbandtype != 3) {
            return false;
        }
        return wifip2pNative.setP2pBandLIst(OppoSoftapP2pBandControl.BandType.BAND_ALL);
    }

    public static boolean isNfcTriggered() {
        return isNfcTriggered;
    }

    public static void setNfcTriggered(boolean nfcTriggered) {
        isNfcTriggered = nfcTriggered;
    }

    public static void enableVerboseLogging(boolean enableVerboseLogging) {
        mVerboseLoggingEnabled = enableVerboseLogging;
    }
}
