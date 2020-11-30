package com.android.server.wifi.util;

import android.net.wifi.WifiConfiguration;
import android.util.Log;
import com.android.server.wifi.WifiNative;
import java.util.ArrayList;
import java.util.Random;

public class OppoApConfigUtil {
    public static final int DEFAULT_AP_BAND = 0;
    public static final int DEFAULT_AP_CHANNEL = 6;
    public static final int ERROR_GENERIC = 2;
    public static final int ERROR_NO_CHANNEL = 1;
    public static final int SUCCESS = 0;
    private static final String TAG = "ApConfigUtil";
    private static final Random sRandom = new Random();

    private static int chooseOptimalApChannel(int apChannel, int apBand, int[] allowed2GFreqList, int[] allowed5GFreqList) {
        if (apBand != 0 && apBand != 1) {
            Log.e(TAG, "Invalid band: " + apBand);
            return -1;
        } else if (apBand == 0) {
            if (allowed2GFreqList == null || allowed2GFreqList.length == 0) {
                Log.d(TAG, "2GHz allowed channel list not specified");
                return 6;
            }
            for (int i : allowed2GFreqList) {
                if (apChannel == convertFrequencyToChannel(i)) {
                    Log.d(TAG, "chooseOptimalApChannel 2.4 = " + apChannel);
                    return apChannel;
                }
            }
            return convertFrequencyToChannel(allowed2GFreqList[sRandom.nextInt(allowed2GFreqList.length)]);
        } else if (allowed5GFreqList == null || allowed5GFreqList.length <= 0) {
            Log.e(TAG, "No available channels on 5GHz band");
            return -1;
        } else {
            for (int i2 : allowed5GFreqList) {
                if (apChannel == convertFrequencyToChannel(i2)) {
                    return apChannel;
                }
            }
            return convertFrequencyToChannel(allowed5GFreqList[sRandom.nextInt(allowed5GFreqList.length)]);
        }
    }

    public static int updateApChannelConfig(WifiNative wifiNative, String countryCode, ArrayList<Integer> allowed2GChannels, WifiConfiguration config) {
        if (!wifiNative.isHalStarted()) {
            config.apBand = 0;
            config.apChannel = 6;
            return 0;
        } else if (config.apBand == 1 && countryCode == null) {
            Log.e(TAG, "5GHz band is not allowed without country code");
            return 2;
        } else {
            if (config.apChannel == 0) {
                config.apChannel = chooseApChannel(config.apBand, allowed2GChannels, wifiNative.getChannelsForBand(2));
            } else {
                config.apChannel = chooseOptimalApChannel(config.apChannel, config.apBand, wifiNative.getChannelsForBand(1), wifiNative.getChannelsForBand(2));
            }
            if (config.apChannel == -1) {
                Log.e(TAG, "Failed to get available channel.");
            }
            return 0;
        }
    }

    public static int convertFrequencyToChannel(int frequency) {
        if (frequency >= 2412 && frequency <= 2472) {
            return ((frequency - 2412) / 5) + 1;
        }
        if (frequency == 2484) {
            return 14;
        }
        if (frequency < 5170 || frequency > 5865) {
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
        } else if (allowed5GFreqList == null || allowed5GFreqList.length <= 0) {
            Log.e(TAG, "No available channels on 5GHz band");
            return -1;
        } else {
            Log.d(TAG, "convertFrequencyToChannel allowed5GFreqList = " + convertFrequencyToChannel(allowed5GFreqList[sRandom.nextInt(allowed5GFreqList.length)]));
            return convertFrequencyToChannel(allowed5GFreqList[sRandom.nextInt(allowed5GFreqList.length)]);
        }
    }

    public static int isChannelSupportBw40(int channel) {
        if (channel >= 1 && channel < 5) {
            return 1;
        }
        if (channel >= 5 && channel <= 9) {
            return 1;
        }
        if (channel > 9 && channel <= 13) {
            return 2;
        }
        if (channel == 36 || channel == 44 || channel == 52 || channel == 60 || channel == 100 || channel == 108 || channel == 116 || channel == 124 || channel == 132 || channel == 140 || channel == 149 || channel == 157) {
            return 1;
        }
        if (channel == 40 || channel == 48 || channel == 56 || channel == 64 || channel == 104 || channel == 112 || channel == 120 || channel == 128 || channel == 136 || channel == 144 || channel == 153 || channel == 161) {
            return 2;
        }
        return 0;
    }

    public static int convertChannelToFrequency(int channel) {
        if (channel >= 0 && channel < 14) {
            return ((channel - 1) * 5) + 2412;
        }
        if (channel == 14) {
            return 2484;
        }
        if (channel >= 36 && channel <= 165) {
            return ((channel - 36) * 5) + 5180;
        }
        Log.d(TAG, "channel " + channel + "is not a wifi channel");
        return -1;
    }

    public static void isConfigurationSupportBw40(WifiNative wifiNative, WifiConfiguration config) {
        int secondary_channel_plus;
        int secondary_channel_plus2;
        if (!wifiNative.isHalStarted()) {
            config.apSecondaryChannel = 0;
        } else if (config.apChannel > 0) {
            if (config.apBand != 1) {
                config.apSecondaryChannel = 0;
            } else {
                int secondary_channel = isChannelSupportBw40(config.apChannel);
                if (secondary_channel == 0) {
                    config.apChannel = 0;
                    config.apSecondaryChannel = 0;
                    Log.d(TAG, "current channel not support bandwith 40 /80M, allow to enable hostapd's ACS.");
                } else {
                    int freq = convertChannelToFrequency(config.apChannel);
                    int[] allowed5GFreqList = wifiNative.getChannelsForBand(2);
                    if (secondary_channel == 1) {
                        secondary_channel_plus2 = freq + 20;
                        secondary_channel_plus = 0;
                    } else {
                        secondary_channel_plus2 = 0;
                        secondary_channel_plus = freq - 20;
                    }
                    for (int i = 0; i < allowed5GFreqList.length; i++) {
                        if (secondary_channel_plus2 == allowed5GFreqList[i] || secondary_channel_plus == allowed5GFreqList[i]) {
                            config.apSecondaryChannel = secondary_channel;
                        }
                    }
                    if (config.apSecondaryChannel == 0) {
                        config.apChannel = 0;
                        Log.d(TAG, "the secondary channel not found in allowed 5G Freq List , allow to enable hostapd's ACS.");
                    }
                }
            }
            Log.d(TAG, " SoftAp Channel =" + config.apChannel + " SoftAp Secondary Channel =" + config.apSecondaryChannel);
        } else {
            Log.d(TAG, "invalid channel " + config.apChannel);
        }
    }
}
