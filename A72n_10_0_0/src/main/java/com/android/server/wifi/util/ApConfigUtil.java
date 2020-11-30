package com.android.server.wifi.util;

import android.net.wifi.WifiConfiguration;
import android.util.Log;
import com.android.server.wifi.WifiNative;
import java.util.ArrayList;
import java.util.Random;

public class ApConfigUtil {
    public static final int DEFAULT_AP_BAND = 0;
    public static final int DEFAULT_AP_CHANNEL = 6;
    public static final int ERROR_GENERIC = 2;
    public static final int ERROR_NO_CHANNEL = 1;
    public static final int SUCCESS = 0;
    private static final String TAG = "ApConfigUtil";
    private static final Random sRandom = new Random();

    public static int convertFrequencyToChannel(int frequency) {
        if (frequency >= 2412 && frequency <= 2472) {
            return ((frequency - 2412) / 5) + 1;
        }
        if (frequency == 2484) {
            return 14;
        }
        if (frequency < 5170 || frequency > 5825) {
            return -1;
        }
        return ((frequency - 5170) / 5) + 34;
    }

    public static int chooseApChannel(int apBand, ArrayList<Integer> allowed2GChannels, int[] allowed5GFreqList) {
        if (apBand != 0 && apBand != 1 && apBand != -1) {
            Log.e(TAG, "Invalid band: " + apBand);
            return -1;
        } else if (apBand == 0 || apBand == -1) {
            if (allowed2GChannels != null && allowed2GChannels.size() != 0) {
                return allowed2GChannels.get(sRandom.nextInt(allowed2GChannels.size())).intValue();
            }
            Log.d(TAG, "2GHz allowed channel list not specified");
            return 6;
        } else if (allowed5GFreqList != null && allowed5GFreqList.length > 0) {
            return convertFrequencyToChannel(allowed5GFreqList[sRandom.nextInt(allowed5GFreqList.length)]);
        } else {
            Log.e(TAG, "No available channels on 5GHz band");
            return -1;
        }
    }

    public static int updateApChannelConfig(WifiNative wifiNative, String countryCode, ArrayList<Integer> allowed2GChannels, WifiConfiguration config) {
        return OppoApConfigUtil.updateApChannelConfig(wifiNative, countryCode, allowed2GChannels, config);
    }

    public static void isConfigurationSupportBw40(WifiNative wifiNative, WifiConfiguration config) {
        OppoApConfigUtil.isConfigurationSupportBw40(wifiNative, config);
    }
}
