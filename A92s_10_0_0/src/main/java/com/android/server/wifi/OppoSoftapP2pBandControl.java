package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiRomUpdateHelper;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.wifi.p2p.WifiP2pNative;
import com.android.server.wifi.util.ApConfigUtil;
import java.util.ArrayList;
import java.util.List;

public class OppoSoftapP2pBandControl {
    private static final String DEFAULT_P2P_EUROPE_COUNTRY = "ES,IT,FR,NL,PL,BE,GB,TR,IE,EE,BG,RO,CH,DE,KZ,KE";
    private static final String DEFAULT_P2P_ONLY2G_COUNTRY = "JP";
    private static final String DEFAULT_SOFTAP_EUROPE_COUNTRY = "ES,IT,FR,NL,PL,BE,GB,TR,IE,EE,BG,RO,CH,DE,KZ,KE";
    private static final String DEFAULT_SOFTAP_ONLY2G_COUNTRY = "JP";
    private static final String TAG = "OppoSoftapP2pBandControl";
    private static final List<String> mP2pEuropeCountryList = new ArrayList();
    private static final List<String> mP2pOnly2GCountryList = new ArrayList();
    private static final List<String> mSoftapEuropeCountryList = new ArrayList();
    private static final List<String> mSoftapOnly2GCountryList = new ArrayList();
    private boolean DEBUG = true;
    private final Context mContext;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.OppoSoftapP2pBandControl.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("oppo.intent.action.WIFI_ROM_UPDATE_CHANGED")) {
                OppoSoftapP2pBandControl.this.initP2pOnly2GCountyList();
                OppoSoftapP2pBandControl.this.initP2pEuropeCountyList();
                OppoSoftapP2pBandControl.this.initSoftapOnly2GCountyList();
                OppoSoftapP2pBandControl.this.initSoftapEuropeCountyList();
            }
        }
    };
    private final WifiCountryCode mWifiCountryCode;
    private final WifiNative mWifiNative;

    public enum BandType {
        BAND_2G_ONLY,
        BAND_5G_ONLY,
        BAND_5G_B1,
        BAND_5G_B2,
        BAND_5G_B3,
        BAND_5G_B4,
        BAND_ALL
    }

    private void registerForBroadcasts() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("oppo.intent.action.WIFI_ROM_UPDATE_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    public OppoSoftapP2pBandControl(Context context, WifiCountryCode wificountrycode, WifiNative wifinative) {
        this.mContext = context;
        this.mWifiCountryCode = wificountrycode;
        this.mWifiNative = wifinative;
        initP2pOnly2GCountyList();
        initP2pEuropeCountyList();
        initSoftapOnly2GCountyList();
        initSoftapEuropeCountyList();
        registerForBroadcasts();
    }

    public boolean canUseSoftap5GBand() {
        int[] allow5Gfreqlist = get5Gfreqlist();
        boolean has5GBand1Channel = have5GBand1Channel(allow5Gfreqlist);
        boolean has5GBand4Channel = have5GBand4Channel(allow5Gfreqlist);
        if (allow5Gfreqlist == null) {
            return false;
        }
        if (has5GBand4Channel) {
            setSoftapUseInBand4();
            return true;
        } else if (isSoftapInCERegion() || !has5GBand1Channel) {
            return false;
        } else {
            setSoftapUseInBand1();
            return true;
        }
    }

    private void setSoftapUseInBand4() {
        SystemProperties.set("oppo.softap.band", "band4");
    }

    private void setSoftapUseInBand1() {
        SystemProperties.set("oppo.softap.band", "band1");
    }

    private boolean have5GBand1Channel(int[] Allow5Gfreqlist) {
        if (Allow5Gfreqlist == null) {
            return false;
        }
        for (int i = 0; i < Allow5Gfreqlist.length; i++) {
            if (Allow5Gfreqlist[i] == 5180 || Allow5Gfreqlist[i] == 5200 || Allow5Gfreqlist[i] == 5220 || Allow5Gfreqlist[i] == 5240) {
                Log.d(TAG, "have 5g band1 channel = " + Allow5Gfreqlist[i]);
                return true;
            }
        }
        return false;
    }

    public boolean have5GBand4Channel(int[] Allow5Gfreqlist) {
        if (Allow5Gfreqlist == null) {
            return false;
        }
        for (int i = 0; i < Allow5Gfreqlist.length; i++) {
            if (Allow5Gfreqlist[i] == 5745 || Allow5Gfreqlist[i] == 5765 || Allow5Gfreqlist[i] == 5785 || Allow5Gfreqlist[i] == 5805) {
                Log.d(TAG, "have 5g band4 channel = " + Allow5Gfreqlist[i]);
                return true;
            }
        }
        return false;
    }

    public int[] isSupport5GBand4Channel(int[] Allow5Gfreqlist) {
        int[] channels = {0, 0, 0, 0};
        int count = 0;
        if (Allow5Gfreqlist != null) {
            for (int i = 0; i < Allow5Gfreqlist.length; i++) {
                if (Allow5Gfreqlist[i] == 5745 || Allow5Gfreqlist[i] == 5765 || Allow5Gfreqlist[i] == 5785 || Allow5Gfreqlist[i] == 5805) {
                    Log.d(TAG, "support 5Gb4 channel = " + Allow5Gfreqlist[i]);
                    if (count < 4) {
                        channels[count] = Allow5Gfreqlist[i];
                        count++;
                    }
                }
            }
        }
        return channels;
    }

    public int select5GBand4channel(int[] Allow5Gfreqlist) {
        if (Allow5Gfreqlist == null) {
            return 0;
        }
        for (int i = 0; i < Allow5Gfreqlist.length; i++) {
            if (Allow5Gfreqlist[i] == 5745 || Allow5Gfreqlist[i] == 5765 || Allow5Gfreqlist[i] == 5785 || Allow5Gfreqlist[i] == 5805 || Allow5Gfreqlist[i] == 5825) {
                int band4freq = Allow5Gfreqlist[i];
                Log.d(TAG, "select5GBand4channel = " + Allow5Gfreqlist[i]);
                return ApConfigUtil.convertFrequencyToChannel(band4freq);
            }
        }
        return 0;
    }

    public boolean isIn5GBand4channel(int Channel) {
        if (Channel == 149 || Channel == 153 || Channel == 157 || Channel == 161 || Channel == 165) {
            return true;
        }
        return false;
    }

    public int[] get5Gfreqlist() {
        int[] allow5Gfreqlist = null;
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative != null) {
            allow5Gfreqlist = wifiNative.getChannelsForBand(2);
        }
        if (allow5Gfreqlist != null) {
            for (int i = 0; i < allow5Gfreqlist.length; i++) {
                Log.d(TAG, "allow5Gfreqlist = " + allow5Gfreqlist[i]);
            }
        }
        return allow5Gfreqlist;
    }

    public boolean isP2pInCERegion() {
        String country = this.mWifiCountryCode.getCountryCode();
        if (country == null || country.isEmpty()) {
            Log.d(TAG, "country = null");
            return false;
        } else if (mP2pEuropeCountryList == null) {
            Log.e(TAG, "mCountryList = null");
            return false;
        } else {
            for (int i = 0; i < mP2pEuropeCountryList.size(); i++) {
                if (country.equalsIgnoreCase(mP2pEuropeCountryList.get(i))) {
                    Log.d(TAG, "is p2p in CE Region " + country);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isP2pInOnly2GRegion() {
        String country = this.mWifiCountryCode.getCountryCode();
        if (country == null || country.isEmpty()) {
            Log.d(TAG, "country = null");
            return false;
        } else if (mP2pOnly2GCountryList == null) {
            Log.e(TAG, "mCountryList = null");
            return false;
        } else {
            for (int i = 0; i < mP2pOnly2GCountryList.size(); i++) {
                if (country.equalsIgnoreCase(mP2pOnly2GCountryList.get(i))) {
                    Log.d(TAG, "is p2p in 2.4g only Region " + country);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isSoftapInCERegion() {
        String country = this.mWifiCountryCode.getCountryCode();
        if (country == null || country.isEmpty()) {
            Log.d(TAG, "country = null");
            return false;
        } else if (mSoftapEuropeCountryList == null) {
            Log.e(TAG, "mCountryList = null");
            return false;
        } else {
            for (int i = 0; i < mSoftapEuropeCountryList.size(); i++) {
                if (country.equalsIgnoreCase(mSoftapEuropeCountryList.get(i))) {
                    Log.d(TAG, "is softao in CE Region " + country);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isSoftapInOnly2GRegion() {
        String country = this.mWifiCountryCode.getCountryCode();
        if (country == null || country.isEmpty()) {
            Log.d(TAG, "country = null");
            return false;
        } else if (mSoftapOnly2GCountryList == null) {
            Log.e(TAG, "mCountryList = null");
            return false;
        } else {
            for (int i = 0; i < mSoftapOnly2GCountryList.size(); i++) {
                if (country.equalsIgnoreCase(mSoftapOnly2GCountryList.get(i))) {
                    Log.d(TAG, "is softap in 2.4g only Region " + country);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean needSoftapUse2GOnlyCountry() {
        return WifiRomUpdateHelper.getInstance(this.mContext).getBooleanValue("OPPO_BASIC_WIFI_CUSTOM_SOFTAP_ONLY2G_BADN_LIMIT", true);
    }

    public boolean needP2pLimit2GOnlyBand() {
        return WifiRomUpdateHelper.getInstance(this.mContext).getBooleanValue("OPPO_BASIC_WIFI_CUSTOM_P2P_ONLY2G_BADN_LIMIT", false);
    }

    public boolean needP2pLimitBand() {
        return WifiRomUpdateHelper.getInstance(this.mContext).getBooleanValue("OPPO_BASIC_WIFI_CUSTOM_P2P_BADN_LIMIT", false);
    }

    /* access modifiers changed from: private */
    public void initP2pEuropeCountyList() {
        String value = WifiRomUpdateHelper.getInstance(this.mContext).getValue("OPPO_BASIC_WIFI_CUSTOM_P2P_BAND4_COUNTEY_LISIT", "ES,IT,FR,NL,PL,BE,GB,TR,IE,EE,BG,RO,CH,DE,KZ,KE");
        if (value == null) {
            Log.e(TAG, "initEuropeCountyList failed error");
            return;
        }
        synchronized (mP2pEuropeCountryList) {
            if (!mP2pEuropeCountryList.isEmpty()) {
                mP2pEuropeCountryList.clear();
            }
            String[] split = value.split(",");
            for (String name : split) {
                mP2pEuropeCountryList.add(name.trim());
                Log.d(TAG, "initEuropeCountyList country =" + name);
            }
        }
    }

    /* access modifiers changed from: private */
    public void initP2pOnly2GCountyList() {
        String value = WifiRomUpdateHelper.getInstance(this.mContext).getValue("OPPO_BASIC_WIFI_CUSTOM_P2P_ONLY2G_COUNTEY_LISIT", "JP");
        if (value == null) {
            Log.e(TAG, "initEuropeCountyList failed error");
            return;
        }
        synchronized (mP2pOnly2GCountryList) {
            if (!mP2pOnly2GCountryList.isEmpty()) {
                mP2pOnly2GCountryList.clear();
            }
            String[] split = value.split(",");
            for (String name : split) {
                mP2pOnly2GCountryList.add(name.trim());
                Log.d(TAG, "init2GOnlyCountyList country =" + name);
            }
        }
    }

    /* access modifiers changed from: private */
    public void initSoftapEuropeCountyList() {
        String value = WifiRomUpdateHelper.getInstance(this.mContext).getValue("OPPO_BASIC_WIFI_CUSTOM_SOFTAP_BAND4_COUNTEY_LISIT", "ES,IT,FR,NL,PL,BE,GB,TR,IE,EE,BG,RO,CH,DE,KZ,KE");
        if (value == null) {
            Log.e(TAG, "initEuropeCountyList failed error");
            return;
        }
        synchronized (mSoftapEuropeCountryList) {
            if (!mSoftapEuropeCountryList.isEmpty()) {
                mSoftapEuropeCountryList.clear();
            }
            String[] split = value.split(",");
            for (String name : split) {
                mSoftapEuropeCountryList.add(name.trim());
                Log.d(TAG, "initEuropeCountyList country =" + name);
            }
        }
    }

    /* access modifiers changed from: private */
    public void initSoftapOnly2GCountyList() {
        String value = WifiRomUpdateHelper.getInstance(this.mContext).getValue("OPPO_BASIC_WIFI_CUSTOM_SOFTAP_ONLY2G_COUNTEY_LISIT", "JP");
        if (value == null) {
            Log.e(TAG, "initEuropeCountyList failed error");
            return;
        }
        synchronized (mSoftapOnly2GCountryList) {
            if (!mSoftapOnly2GCountryList.isEmpty()) {
                mSoftapOnly2GCountryList.clear();
            }
            String[] split = value.split(",");
            for (String name : split) {
                mSoftapOnly2GCountryList.add(name.trim());
                Log.d(TAG, "init2GOnlyCountyList country =" + name);
            }
        }
    }

    public BandType europe5GbandSelector() {
        boolean band4Support = false;
        boolean band1Support = false;
        String country = this.mWifiCountryCode.getCountryCode();
        if (country == null || country.isEmpty()) {
            Log.d(TAG, "no limit,due to country=null");
            return BandType.BAND_2G_ONLY;
        }
        int[] allowed5GFreqList = get5Gfreqlist();
        if (allowed5GFreqList != null) {
            for (int i = 0; i < allowed5GFreqList.length; i++) {
                if (allowed5GFreqList[i] == 5765 || allowed5GFreqList[i] == 5785 || allowed5GFreqList[i] == 5805 || allowed5GFreqList[i] == 5825) {
                    band4Support = true;
                    Log.d(TAG, "use in band4 channel = " + allowed5GFreqList[i]);
                }
                if (allowed5GFreqList[i] == 5180 || allowed5GFreqList[i] == 5200 || allowed5GFreqList[i] == 2520 || allowed5GFreqList[i] == 5240) {
                    band1Support = true;
                    Log.d(TAG, "use in band1 channel = " + allowed5GFreqList[i]);
                }
            }
            if (band4Support) {
                return BandType.BAND_5G_B4;
            }
            if (isP2pInCERegion()) {
                return BandType.BAND_2G_ONLY;
            }
            if (band1Support) {
                return BandType.BAND_5G_B1;
            }
            Log.d(TAG, "band4 band1 not support,not eurpe region ,limit in 2G=" + country);
            return BandType.BAND_2G_ONLY;
        }
        Log.d(TAG, "allowed5GFreqList == null,use 2G");
        return BandType.BAND_2G_ONLY;
    }

    public void p2pChangeBand() {
        WifiP2pNative wifiP2pNative = WifiInjector.getInstance().getWifiP2pNative();
        if (needP2pLimit2GOnlyBand() && isP2pInOnly2GRegion()) {
            wifiP2pNative.setP2pBandLIst(BandType.BAND_2G_ONLY);
        } else if (needP2pLimitBand()) {
            wifiP2pNative.setP2pBandLIst(europe5GbandSelector());
        } else {
            Log.d(TAG, "not in 2g only country also no need limit 5g band");
        }
    }
}
