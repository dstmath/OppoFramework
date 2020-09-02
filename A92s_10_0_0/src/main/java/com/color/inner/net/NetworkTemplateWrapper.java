package com.color.inner.net;

import android.net.NetworkTemplate;
import android.util.Log;

public class NetworkTemplateWrapper {
    private static final String TAG = "NetworkTemplateWrapper";
    NetworkTemplate mNetworkTemplate;

    private NetworkTemplateWrapper(NetworkTemplate networkTemplate) {
        this.mNetworkTemplate = networkTemplate;
    }

    public static NetworkTemplateWrapper buildTemplateMobileAll(String subscriberId) {
        try {
            return new NetworkTemplateWrapper(new NetworkTemplate(1, subscriberId, (String) null));
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
