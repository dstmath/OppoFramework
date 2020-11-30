package com.mediatek.net.tethering;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.connectivity.Tethering;
import vendor.mediatek.hardware.netdagent.V1_0.INetdagent;

public class MtkTethering {
    private static final boolean DBG = false;
    static final int EVENT_BOOTUP = 1;
    private static final String TAG = "MtkTethering";
    private static Tethering sTethering;
    private final Context mContext;
    private final InternalHandler mHandler;
    protected final HandlerThread mHandlerThread = new HandlerThread("TetheringInternalHandler");

    public MtkTethering(Context context, Tethering tethering) {
        this.mContext = context;
        sTethering = tethering;
        this.mHandlerThread.start();
        this.mHandler = new InternalHandler(this.mHandlerThread.getLooper());
        this.mHandler.sendEmptyMessage(1);
    }

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                Log.e(MtkTethering.TAG, "Invalid message: " + msg.what);
                return;
            }
            MtkTethering.this.checkEmSetting();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkEmSetting() {
        if (SystemProperties.getBoolean("persist.vendor.radio.bgdata.disabled", (boolean) DBG)) {
            try {
                INetdagent netagent = INetdagent.getService();
                if (netagent == null) {
                    Log.e(TAG, "netagent is null");
                    return;
                }
                Log.d(TAG, "setIotFirewall");
                netagent.dispatchNetdagentCmd("netdagent firewall set_nsiot_firewall");
            } catch (Exception e) {
                Log.d(TAG, "Exception:" + e);
            }
        }
    }
}
