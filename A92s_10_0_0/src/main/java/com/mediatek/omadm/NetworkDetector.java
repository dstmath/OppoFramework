package com.mediatek.omadm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetworkDetector {
    private static final int NW_CONN_ADMIN = 1;
    private static final int NW_CONN_WIFI = 0;
    private static final int NW_DATA_ROAM = 2;
    private static final String TAG = "NetworkDetector";
    private ConnectivityManager mConnectivityManager = null;
    private BroadcastReceiver mNetEventsReceiver = null;
    private final OmadmServiceImpl mOmadmSrv;
    private TelephonyManager mTelephonyManager = null;

    public NetworkDetector(Context context, OmadmServiceImpl omadmServiceImpl) {
        this.mOmadmSrv = omadmServiceImpl;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
    }

    private void defaultStateNotify() {
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        if (connectivityManager == null) {
            Log.e(TAG, "defaultStateNotify(): NULL ConnectivityManager!!!");
            return;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        Feature feature = Feature.getFeature(networkInfo, this.mTelephonyManager.getPhoneType());
        int[] features = {0, 1, 2};
        for (int type : features) {
            if (feature == null || type != feature.type) {
                Log.d(TAG, "Notify omadm NM with type: " + type + ", status: false");
                this.mOmadmSrv.notifyOmadmNetworkManager(type, false);
            } else {
                Log.d(TAG, "Notify omadm NM with type: " + feature.type + ", status: " + feature.status);
                if (1 == feature.type) {
                    this.mOmadmSrv.notifyOmadmNetworkManager(2, networkInfo.isRoaming());
                }
                this.mOmadmSrv.notifyOmadmNetworkManager(feature.type, feature.status);
            }
        }
    }

    /* access modifiers changed from: private */
    public void networkChanged(NetworkInfo nwInfo) {
        Log.d(TAG, "networkChanged()");
        if (this.mTelephonyManager == null) {
            Log.e(TAG, "onReceive() NULL TelephonyManager !!!");
            return;
        }
        int type = nwInfo.getType();
        Log.d(TAG, "onReceive() - handle for type: " + type);
        Feature feature = Feature.getFeature(nwInfo, this.mTelephonyManager.getPhoneType());
        if (feature != null) {
            if (1 == feature.type) {
                this.mOmadmSrv.notifyOmadmNetworkManager(2, this.mTelephonyManager.isNetworkRoaming());
            }
            this.mOmadmSrv.notifyOmadmNetworkManager(feature.type, feature.status);
            return;
        }
        Log.d(TAG, "onReceive() - nothing to handle for type: " + type);
    }

    public void register(Context context) {
        defaultStateNotify();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mNetEventsReceiver = new NwEvntBroadcastReceiver();
        context.registerReceiver(this.mNetEventsReceiver, intentFilter);
    }

    public void unregister(Context context) {
        BroadcastReceiver broadcastReceiver = this.mNetEventsReceiver;
        if (broadcastReceiver != null) {
            context.unregisterReceiver(broadcastReceiver);
            this.mNetEventsReceiver = null;
        }
    }

    private class NwEvntBroadcastReceiver extends BroadcastReceiver {
        private NwEvntBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.d(NetworkDetector.TAG, "NwEvntBroadcastReceiver onReceive()");
            NetworkDetector.this.networkChanged((NetworkInfo) intent.getParcelableExtra("networkInfo"));
        }
    }

    private static class Feature {
        public boolean status;
        public int type;

        public Feature(int type2, boolean status2) {
            this.type = type2;
            this.status = status2;
        }

        public static Feature getFeature(NetworkInfo networkInfo, int phoneType) {
            if (networkInfo == null) {
                Log.d(NetworkDetector.TAG, "networkInfo is null");
                return null;
            }
            int type2 = networkInfo.getType();
            Log.d(NetworkDetector.TAG, "Phone type: " + phoneType + " Network type: " + type2 + " NetworkInfo content: " + networkInfo.toString());
            if (type2 != 0) {
                if (type2 == 1) {
                    return new Feature(0, networkInfo.isConnected());
                }
                if (type2 == 10) {
                    return new Feature(1, networkInfo.isConnected());
                }
            } else if (phoneType == 1) {
                return new Feature(1, networkInfo.isConnected());
            } else {
                if (phoneType != 2) {
                }
            }
            return null;
        }
    }
}
