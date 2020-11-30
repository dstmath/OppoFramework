package com.android.server.net;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.ColorServiceRegistry;
import com.android.server.display.ai.utils.ColorAILog;

public class ColorNetworkPolicyManagerServiceEx extends ColorDummyNetworkPolicyManagerServiceEx {
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final String TAG = "ColorNetworkPolicyManagerServiceEx";

    public ColorNetworkPolicyManagerServiceEx(Context context, NetworkPolicyManagerService npms) {
        super(context, npms);
        init(context, npms);
    }

    public void systemReady() {
        Slog.i(TAG, "systemReady");
        ColorServiceRegistry.getInstance().serviceReady(30);
    }

    public void onStart() {
        Slog.i(TAG, "onStart");
        ColorNetworkPolicyManagerServiceEx.super.onStart();
    }

    private void init(Context context, NetworkPolicyManagerService npms) {
        ColorServiceRegistry.getInstance().serviceInit(10, this);
    }
}
