package com.android.server.wifi;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import java.util.ArrayList;

public class ActiveModeWardenForDeferRequest extends ActiveModeWarden implements Handler.Callback {
    private static final int BASE = 131572;
    @VisibleForTesting
    public static final int CMD_DEFER_OFF_TIMEOUT = 131574;
    @VisibleForTesting
    public static final int CMD_DEFER_WIFI_ON = 131579;
    @VisibleForTesting
    public static final int CMD_GO_SCAN_MODE = 131576;
    @VisibleForTesting
    public static final int CMD_GO_SHUT_DOWN = 131578;
    @VisibleForTesting
    public static final int CMD_GO_SOFT_AP = 131577;
    @VisibleForTesting
    public static final int CMD_GO_WIFI_OFF = 131575;
    @VisibleForTesting
    public static final int CMD_NOTIFY_GO = 131573;
    private static final String TAG = "ActiveModeWardenForDeferRequest";
    @VisibleForTesting
    public static final int TIMEOUT = 3000;
    private static boolean mWaitForEvent = false;
    private static WifiInjector mWifiInjector;
    private static WifiStaStateNotifier mWifiStaStateNotifier;
    private final Context mContext;
    private ArrayList<Message> mDeferredMsgInQueue = new ArrayList<>();
    private Handler mEventHandler;
    private boolean mShouldDeferDisableWifi = false;
    private SoftApModeConfiguration mWifiConfig;

    public ActiveModeWardenForDeferRequest(WifiInjector wifiInjector, Context context, Looper looper, WifiNative wifiNative, DefaultModeManager defaultModeManager, IBatteryStats batteryStats) {
        super(wifiInjector, context, looper, wifiNative, defaultModeManager, batteryStats);
        this.mEventHandler = new Handler(looper, this);
        this.mContext = context;
        mWifiInjector = wifiInjector;
        mWifiStaStateNotifier = mWifiInjector.getWifiStaStateNotifier();
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case CMD_NOTIFY_GO /*{ENCODED_INT: 131573}*/:
                break;
            case CMD_DEFER_OFF_TIMEOUT /*{ENCODED_INT: 131574}*/:
                Log.d(TAG, "Defer Wi-Fi off timeout");
                break;
            case CMD_GO_WIFI_OFF /*{ENCODED_INT: 131575}*/:
                super.disableWifi();
                return true;
            case CMD_GO_SCAN_MODE /*{ENCODED_INT: 131576}*/:
                super.enterScanOnlyMode();
                return true;
            case CMD_GO_SOFT_AP /*{ENCODED_INT: 131577}*/:
                super.enterSoftAPMode(this.mWifiConfig);
                return true;
            case CMD_GO_SHUT_DOWN /*{ENCODED_INT: 131578}*/:
                super.shutdownWifi();
                return true;
            case CMD_DEFER_WIFI_ON /*{ENCODED_INT: 131579}*/:
                super.enterClientMode();
                return true;
            default:
                Log.e(TAG, "Unhandle message");
                return true;
        }
        for (int i = 0; i < this.mDeferredMsgInQueue.size(); i++) {
            Log.d(TAG, "mDeferredMsgInQueue: " + this.mDeferredMsgInQueue.get(i));
            Message copyMsg = this.mEventHandler.obtainMessage();
            copyMsg.copyFrom(this.mDeferredMsgInQueue.get(i));
            this.mEventHandler.sendMessage(copyMsg);
        }
        this.mDeferredMsgInQueue.clear();
        mWaitForEvent = false;
        return true;
    }

    @Override // com.android.server.wifi.ActiveModeWarden
    public void enterClientMode() {
        if (!mWaitForEvent) {
            super.enterClientMode();
            return;
        }
        Log.d(TAG, "enterClientMode, mWaitForEvent " + mWaitForEvent);
        this.mDeferredMsgInQueue.add(this.mEventHandler.obtainMessage(CMD_DEFER_WIFI_ON));
    }

    @Override // com.android.server.wifi.ActiveModeWarden
    public void disableWifi() {
        if (!this.mShouldDeferDisableWifi) {
            super.disableWifi();
            return;
        }
        Log.d(TAG, "disableWifi, mShouldDeferDisableWifi: " + this.mShouldDeferDisableWifi);
        mWaitForEvent = true;
        notifyStaToBeOff();
        this.mDeferredMsgInQueue.add(this.mEventHandler.obtainMessage(CMD_GO_WIFI_OFF));
        Handler handler = this.mEventHandler;
        handler.sendMessageDelayed(handler.obtainMessage(CMD_DEFER_OFF_TIMEOUT), WifiMetrics.TIMEOUT_RSSI_DELTA_MILLIS);
    }

    @Override // com.android.server.wifi.ActiveModeWarden
    public void enterScanOnlyMode() {
        if (!this.mShouldDeferDisableWifi) {
            super.enterScanOnlyMode();
            return;
        }
        Log.d(TAG, "enterScanOnlyMode, mShouldDeferDisableWifi: " + this.mShouldDeferDisableWifi);
        mWaitForEvent = true;
        notifyStaToBeOff();
        this.mDeferredMsgInQueue.add(this.mEventHandler.obtainMessage(CMD_GO_SCAN_MODE));
        Handler handler = this.mEventHandler;
        handler.sendMessageDelayed(handler.obtainMessage(CMD_DEFER_OFF_TIMEOUT), WifiMetrics.TIMEOUT_RSSI_DELTA_MILLIS);
    }

    @Override // com.android.server.wifi.ActiveModeWarden
    public void enterSoftAPMode(SoftApModeConfiguration wifiConfig) {
        boolean isApStaConcurrencySupport = mWifiInjector.getHalDeviceManager().isConcurrentStaPlusApSupported();
        Log.d(TAG, "isApStaConcurrencySupport: " + isApStaConcurrencySupport);
        if (isApStaConcurrencySupport) {
            super.enterSoftAPMode(wifiConfig);
        } else if (!this.mShouldDeferDisableWifi) {
            super.enterSoftAPMode(wifiConfig);
        } else {
            Log.d(TAG, "enterSoftAPMode, mShouldDeferDisableWifi: " + this.mShouldDeferDisableWifi);
            mWaitForEvent = true;
            this.mWifiConfig = wifiConfig;
            notifyStaToBeOff();
            this.mDeferredMsgInQueue.add(this.mEventHandler.obtainMessage(CMD_GO_SOFT_AP));
            Handler handler = this.mEventHandler;
            handler.sendMessageDelayed(handler.obtainMessage(CMD_DEFER_OFF_TIMEOUT), WifiMetrics.TIMEOUT_RSSI_DELTA_MILLIS);
        }
    }

    @Override // com.android.server.wifi.ActiveModeWarden
    public void shutdownWifi() {
        if (!this.mShouldDeferDisableWifi) {
            super.shutdownWifi();
            return;
        }
        Log.d(TAG, "shutdownWifi, mShouldDeferDisableWifi: " + this.mShouldDeferDisableWifi);
        mWaitForEvent = true;
        notifyStaToBeOff();
        this.mDeferredMsgInQueue.add(this.mEventHandler.obtainMessage(CMD_GO_SHUT_DOWN));
        Handler handler = this.mEventHandler;
        handler.sendMessageDelayed(handler.obtainMessage(CMD_DEFER_OFF_TIMEOUT), WifiMetrics.TIMEOUT_RSSI_DELTA_MILLIS);
    }

    @Override // com.android.server.wifi.ActiveModeWarden
    public void registerStaEventCallback() {
        this.mShouldDeferDisableWifi = true;
    }

    @Override // com.android.server.wifi.ActiveModeWarden
    public void unregisterStaEventCallback() {
        notifyDeferEventGo();
        this.mShouldDeferDisableWifi = false;
    }

    @VisibleForTesting
    public void notifyStaToBeOff() {
        mWifiStaStateNotifier.onStaToBeOff();
    }

    @VisibleForTesting
    public void notifyDeferEventGo() {
        if (this.mShouldDeferDisableWifi) {
            Handler handler = this.mEventHandler;
            handler.sendMessage(handler.obtainMessage(CMD_NOTIFY_GO));
        }
    }

    @VisibleForTesting
    public ArrayList<Message> getDeferredMsgInQueue() {
        return this.mDeferredMsgInQueue;
    }
}
