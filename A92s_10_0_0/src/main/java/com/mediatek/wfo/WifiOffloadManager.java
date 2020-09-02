package com.mediatek.wfo;

import com.mediatek.wfo.IWifiOffloadListener;

public class WifiOffloadManager {
    public static final String ACTION_NOTIFY_CONNECTION_ERROR = "android.intent.action.NOTIFY_CONNECTION_ERROR";
    public static final String ACTION_ROVE_OUT_ALERT = "android.intent.action.ROVE_OUT_ALERT";
    public static final int CALL_STATE_ACTIVE = 1;
    public static final int CALL_STATE_END = 0;
    public static final int CALL_STATE_ESTABLISHING = 2;
    public static final int CALL_TYPE_VIDEO = 2;
    public static final int CALL_TYPE_VOICE = 1;
    public static final String EXTRA_ERROR_CODE = "error_code";
    public static final int HANDOVER_END = 1;
    public static final int HANDOVER_FAILED = -1;
    public static final int HANDOVER_START = 0;
    public static final int RAN_TYPE_DENY = -1;
    public static final int RAN_TYPE_MOBILE_3GPP = 1;
    public static final int RAN_TYPE_MOBILE_3GPP2 = 3;
    public static final int RAN_TYPE_NONE = -2;
    public static final int RAN_TYPE_UNSPEC = 0;
    public static final int RAN_TYPE_WIFI = 2;
    public static final String WFO_SERVICE = "wfo";

    public static abstract class Listener extends IWifiOffloadListener.Stub {
        @Override // com.mediatek.wfo.IWifiOffloadListener
        public void onHandover(int simIdx, int stage, int ratType) {
        }

        @Override // com.mediatek.wfo.IWifiOffloadListener
        public void onRoveOut(int simIdx, boolean roveOut, int rssi) {
        }

        @Override // com.mediatek.wfo.IWifiOffloadListener
        public void onRequestImsSwitch(int simIdx, boolean isImsOn) {
        }

        @Override // com.mediatek.wfo.IWifiOffloadListener
        public void onWifiPdnOOSStateChanged(int simIdx, int oosState) {
        }

        @Override // com.mediatek.wfo.IWifiOffloadListener
        public void onAllowWifiOff() {
        }

        @Override // com.mediatek.wfo.IWifiOffloadListener
        public void onWfcStateChanged(int simIdx, int state) {
        }
    }
}
