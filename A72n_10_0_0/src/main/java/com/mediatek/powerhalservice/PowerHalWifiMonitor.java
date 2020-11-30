package com.mediatek.powerhalservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.mediatek.powerhalwrapper.PowerHalWrapper;

public class PowerHalWifiMonitor {
    private static final String APP_EVENT_BUNDLE_KEY_STATE = "STATE";
    private static final int APP_EVENT_DUPLICATE_PACKET_PREDICTION_BUSY = -1;
    private static final int APP_EVENT_DUPLICATE_PACKET_PREDICTION_OFF = 0;
    private static final int APP_EVENT_DUPLICATE_PACKET_PREDICTION_ON = 1;
    private static final int APP_EVENT_WIFI_UNAVAILABLE = -1;
    private static final int CMD_CALLBACK_APP_EVENT = 0;
    private static final int DRIVER_EVENT_TX_DUP_CERT_CHANGE = 102;
    private static final int DRIVER_EVENT_TX_DUP_OFF = 100;
    private static final int DRIVER_EVENT_TX_DUP_ON = 101;
    private static final int DUPLICATE_PACKET_PREDICTION_BUSY_TIMEOUT = 5;
    private static PowerHalWrapper mPowerHalWrap = null;
    private static PowerHalWifiMonitor sInstance = null;
    private final String TAG = "PowerHalWifiMonitor";
    private Context mContext;
    private int mDppPowerHdl = 0;
    private boolean mDppStarted;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private final RemoteCallbackList<IRemoteCallback> mListeners = new RemoteCallbackList<>();
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.mediatek.powerhalservice.PowerHalWifiMonitor.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            PowerHalWifiMonitor powerHalWifiMonitor = PowerHalWifiMonitor.this;
            powerHalWifiMonitor.logd("onReceive action:" + intent.getAction());
            if (intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                PowerHalWifiMonitor.this.onWifiStateChange(intent.getIntExtra("wifi_state", 4));
            } else if (intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
                PowerHalWifiMonitor.this.onWifiConnectionStateChange((NetworkInfo) intent.getParcelableExtra("networkInfo"));
            } else if (intent.getAction().equals("com.mediatek.npp.ev.a")) {
                PowerHalWifiMonitor.this.postStateChange(1);
            } else if (intent.getAction().equals("com.mediatek.npp.ev.b")) {
                PowerHalWifiMonitor.this.postStateChange(0);
            }
        }
    };
    private boolean mWifiConnected;
    private boolean mWifiEnabled;

    public PowerHalWifiMonitor(Context context) {
        this.mContext = context;
        mPowerHalWrap = PowerHalWrapper.getInstance();
        registerForBroadcast();
        initHandlerThread();
        sInstance = this;
    }

    public static PowerHalWifiMonitor getInstance() {
        return sInstance;
    }

    public void startDuplicatePacketPrediction() {
        logd("startDuplicatePacketPrediction() mDppStarted:" + this.mDppStarted + ", mWifiEnabled:" + this.mWifiEnabled);
        int[] rscList = {41959680, 1, 54525952, 2};
        if (!this.mDppStarted) {
            this.mDppStarted = true;
            if (this.mWifiEnabled) {
                int i = this.mDppPowerHdl;
                if (i != 0) {
                    mPowerHalWrap.perfLockRelease(i);
                    this.mDppPowerHdl = 0;
                }
                this.mDppPowerHdl = mPowerHalWrap.perfLockAcquire(this.mDppPowerHdl, 0, rscList);
            }
            if (!this.mWifiEnabled || !this.mWifiConnected) {
                postStateChange(-1);
            }
        }
    }

    public void stopDuplicatePacketPrediction() {
        logd("stopDuplicatePacketPrediction() mDppStarted:" + this.mDppStarted + ", mWifiEnabled:" + this.mWifiEnabled);
        if (this.mDppStarted) {
            this.mDppStarted = false;
            if (this.mWifiEnabled) {
                mPowerHalWrap.perfLockRelease(this.mDppPowerHdl);
                this.mDppPowerHdl = 0;
            }
        }
    }

    public boolean isDupPacketPredictionStarted() {
        logd("isDupPacketPredictionStarted() mDppStarted:" + this.mDppStarted);
        return this.mDppStarted;
    }

    public boolean registerDuplicatePacketPredictionEvent(IRemoteCallback listener) {
        if (listener == null) {
            return false;
        }
        synchronized (this.mListeners) {
            logd("registerDuplicatePacketPredictionEvent() " + listener.getClass().toString());
            this.mListeners.register(listener);
        }
        return true;
    }

    public boolean unregisterDuplicatePacketPredictionEvent(IRemoteCallback listener) {
        if (listener == null) {
            return false;
        }
        synchronized (this.mListeners) {
            logd("unregisterDuplicatePacketPredictionEvent() " + listener.getClass().toString());
            this.mListeners.unregister(listener);
        }
        return true;
    }

    private void registerForBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("com.mediatek.npp.ev.a");
        filter.addAction("com.mediatek.npp.ev.b");
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void resetDuplicatePacketPrediction() {
        logd("resetDuplicatePacketPrediction(), mDppStarted:" + this.mDppStarted);
        this.mDppStarted = false;
        mPowerHalWrap.setSysInfo(8, "DELETE_ALL");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postStateChange(int event) {
        logd("postStateChange(), event:" + event);
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(0, event, 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void callbackOnStateChanged(int event) {
        synchronized (this.mListeners) {
            int i = this.mListeners.beginBroadcast();
            logOut("callbackOnStateChanged() " + i + " event:" + event);
            Bundle bundle = new Bundle();
            bundle.putInt(APP_EVENT_BUNDLE_KEY_STATE, event);
            while (i > 0) {
                i--;
                try {
                    this.mListeners.getBroadcastItem(i).sendResult(bundle);
                } catch (RemoteException e) {
                }
            }
            this.mListeners.finishBroadcast();
        }
    }

    private void reStartWifiDriver() {
        logd("reStartWifiDriver()");
        int[] rscList = {41959680, 1, 54525952, 2};
        int i = this.mDppPowerHdl;
        if (i != 0) {
            mPowerHalWrap.perfLockRelease(i);
            this.mDppPowerHdl = 0;
        }
        this.mDppPowerHdl = mPowerHalWrap.perfLockAcquire(this.mDppPowerHdl, 0, rscList);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onWifiStateChange(int wifiState) {
        boolean wifiEnable = wifiState == 3;
        if (this.mWifiEnabled != wifiEnable) {
            this.mWifiEnabled = wifiEnable;
            logd("onWifiStateChange(), mWifiEnabled:" + this.mWifiEnabled + ", mDppStarted:" + this.mDppStarted);
            if (this.mWifiEnabled) {
                if (this.mDppStarted) {
                    reStartWifiDriver();
                }
            } else if (this.mDppStarted) {
                postStateChange(-1);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onWifiConnectionStateChange(NetworkInfo info) {
        boolean wifiConnected = info.isConnected();
        if (this.mWifiConnected != wifiConnected) {
            this.mWifiConnected = wifiConnected;
            logd("onWifiConnectionStateChange(), mWifiConnected:" + this.mWifiConnected + ", mDppStarted:" + this.mDppStarted);
            if (!this.mWifiConnected) {
                if (this.mDppStarted) {
                    postStateChange(-1);
                }
            } else if (this.mDppStarted) {
                reStartWifiDriver();
            }
        }
    }

    private boolean checkIfDuplicatePacketPredictionBusy(int timeEvent) {
        long nowMs = System.currentTimeMillis();
        int now = (int) ((((nowMs / 1000) % 60) * 1000) + (nowMs % 1000));
        if (now - timeEvent <= DUPLICATE_PACKET_PREDICTION_BUSY_TIMEOUT) {
            return false;
        }
        logd("checkIfDuplicatePacketPredictionBusy(), now: " + now + ", drv:" + timeEvent);
        return false;
    }

    public void supplicantHalCallback(int event) {
        int dupEvent = event / 100000;
        boolean busy = checkIfDuplicatePacketPredictionBusy(event % 100000);
        int i = -1;
        switch (dupEvent) {
            case DRIVER_EVENT_TX_DUP_OFF /* 100 */:
                if (this.mDppStarted && this.mWifiConnected) {
                    if (!busy) {
                        i = 0;
                    }
                    callbackOnStateChanged(i);
                    return;
                }
                return;
            case DRIVER_EVENT_TX_DUP_ON /* 101 */:
                if (this.mDppStarted && this.mWifiConnected) {
                    if (!busy) {
                        i = 1;
                    }
                    callbackOnStateChanged(i);
                    return;
                }
                return;
            case DRIVER_EVENT_TX_DUP_CERT_CHANGE /* 102 */:
                if (!mPowerHalWrap.getRildCap(-1)) {
                    resetDuplicatePacketPrediction();
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void initHandlerThread() {
        this.mHandlerThread = new HandlerThread("PowerHalWifiMonitor");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            /* class com.mediatek.powerhalservice.PowerHalWifiMonitor.AnonymousClass2 */

            public void handleMessage(Message msg) {
                PowerHalWifiMonitor powerHalWifiMonitor = PowerHalWifiMonitor.this;
                powerHalWifiMonitor.logd("handleMessage: " + messageToString(msg) + " " + msg.arg1);
                if (msg.what == 0) {
                    PowerHalWifiMonitor.this.callbackOnStateChanged(msg.arg1);
                }
            }

            private String messageToString(Message msg) {
                if (msg.what != 0) {
                    return Integer.toString(msg.what);
                }
                return "CMD_CALLBACK_APP_EVENT";
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String info) {
        Log.d("PowerHalWifiMonitor", info);
    }

    private void logOut(String info) {
        Log.i("NPP", info);
    }
}
