package com.suntek.rcs.ui.common;

import android.content.Context;
import com.suntek.mway.rcs.client.api.ClientApi;
import com.suntek.mway.rcs.client.api.ServiceListener;
import com.suntek.mway.rcs.client.api.support.SupportApi;

public class RcsApiManager {
    private static ServiceListener mPluginApiListener = new ServiceListener() {
        public void onServiceDisconnected() {
            RcsLog.d("pluginApi disconnected");
        }

        public void onServiceConnected() {
            RcsLog.d("pluginApi connected");
        }
    };
    private static ServiceListener mServiceApiListener = new ServiceListener() {
        public void onServiceDisconnected() {
            RcsLog.d("serviceApi disconnected");
        }

        public void onServiceConnected() {
            RcsLog.d("serviceApi connected");
        }
    };

    public static void init(Context context) {
        SupportApi.getInstance().initApi(context);
        new ClientApi().init(context, mServiceApiListener, mPluginApiListener);
    }
}
